/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "ProcessManager"

#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <dirent.h>

#include "jni.h"
#include "JNIHelp.h"
#include "utils/Log.h"
#include "AndroidSystemNatives.h"

/** Environment variables. */
extern char **environ;

static jmethodID onExitMethod = NULL;
static jfieldID descriptorField = NULL;

#ifdef ANDROID
// Keeps track of the system properties fd so we don't close it.
static int androidSystemPropertiesFd = -1;
#endif

/*
 * These are constants shared with the higher level code in
 * ProcessManager.java.
 */
#define WAIT_STATUS_UNKNOWN (-1)       // unknown child status
#define WAIT_STATUS_NO_CHILDREN (-2)   // no children to wait for
#define WAIT_STATUS_STRANGE_ERRNO (-3) // observed an undocumented errno

/** Closes a file descriptor. */
static int closeNow(int fd) {
    int result;
    do {
        result = close(fd);
    } while (result == -1 && errno == EINTR);
    return result;
}

/** Closes a file descriptor. */
static void java_lang_ProcessManager_close(JNIEnv* env,
        jclass clazz, jobject javaDescriptor) {
    int fd = (*env)->GetIntField(env, javaDescriptor, descriptorField);
    if (closeNow(fd) == -1) {
        jclass ioException = (*env)->FindClass(env, "java/io/IOException");
        (*env)->ThrowNew(env, ioException, strerror(errno));
    }
}

/**
 * Kills process with the given ID.
 */
static void java_lang_ProcessManager_kill(JNIEnv* env, jclass clazz, jint pid) {
    int result = kill((pid_t) pid, SIGKILL);
    if (result == -1) {
        jniThrowIOException(env, errno);
    }
}

/**
 * Loops indefinitely and calls ProcessManager.onExit() when children exit.
 */
static void java_lang_ProcessManager_watchChildren(JNIEnv* env, jobject o) {
    if (onExitMethod == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException",
                "staticInitialize() must run first.");
    }

    while (1) {
        int status;

        /* wait for children in our process group */
        pid_t pid = waitpid(0, &status, 0);

        if (pid >= 0) {
            // Extract real status.
            if (WIFEXITED(status)) {
                status = WEXITSTATUS(status);
            } else if (WIFSIGNALED(status)) {
                status = WTERMSIG(status);
            } else if (WIFSTOPPED(status)) {
                status = WSTOPSIG(status);
            } else {
                status = WAIT_STATUS_UNKNOWN;
            }
        } else {
            /*
             * The pid should be -1 already, but force it here just in case
             * we somehow end up with some other negative value.
             */
            pid = -1;

            switch (errno) {
                case ECHILD: {
                    /*
                     * Expected errno: There are no children to wait()
                     * for. The callback will sleep until it is
                     * informed of another child coming to life.
                     */
                    status = WAIT_STATUS_NO_CHILDREN;
                    break;
                }
                case EINTR: {
                    /*
                     * An unblocked signal came in while waiting; just
                     * retry the wait().
                     */
                    continue;
                }
                default: {
                    /*
                     * Unexpected errno, so squawk! Note: Per the
                     * Linux docs, there are no errnos defined for
                     * wait() other than the two that are handled
                     * immediately above.
                     */
                    LOGE("Error %d calling wait(): %s", errno,
                            strerror(errno));
                    status = WAIT_STATUS_STRANGE_ERRNO;
                    break;
                }
            }
        }

        (*env)->CallVoidMethod(env, o, onExitMethod, pid, status);

        if ((*env)->ExceptionOccurred(env)) {
            /*
             * The callback threw, so break out of the loop and return,
             * letting the exception percolate up.
             */
            break;
        }
    }
}

/** Close all open fds > 2 (i.e. everything but stdin/out/err). */
static void closeNonStandardFds(int skipFd) {
    DIR* dir = opendir("/proc/self/fd");

    if (dir == NULL) {
        // Print message to standard err. The parent process can read this
        // from Process.getErrorStream().
        perror("opendir");
        return;
    }

    struct dirent* entry;
    int dirFd = dirfd(dir);
    while ((entry = readdir(dir)) != NULL) {
        int fd = atoi(entry->d_name);
        if (fd > 2 && fd != dirFd && fd != skipFd
#ifdef ANDROID
                && fd != androidSystemPropertiesFd
#endif
                ) {
            close(fd);
        }        
    }

    closedir(dir);
}

#define PIPE_COUNT (4) // number of pipes used to communicate with child proc

/** Closes all pipes in the given array. */
static void closePipes(int pipes[], int skipFd) {
    int i;
    for (i = 0; i < PIPE_COUNT * 2; i++) {
        int fd = pipes[i];
        if (fd == -1) {
            return;
        }
        if (fd != skipFd) {
            close(pipes[i]);
        }
    }
}

/** Executes a command in a child process. */
static pid_t executeProcess(JNIEnv* env, char** commands, char** environment,
        const char* workingDirectory, jobject inDescriptor,
        jobject outDescriptor, jobject errDescriptor) {
    int i, result, error;

    // Create 4 pipes: stdin, stdout, stderr, and an exec() status pipe.
    int pipes[PIPE_COUNT * 2] = { -1, -1, -1, -1, -1, -1, -1, -1 };
    for (i = 0; i < PIPE_COUNT; i++) {
        if (pipe(pipes + i * 2) == -1) {
            jniThrowIOException(env, errno);
            closePipes(pipes, -1);
            return -1;
        }
    }
    int stdinIn = pipes[0];
    int stdinOut = pipes[1];
    int stdoutIn = pipes[2];
    int stdoutOut = pipes[3];
    int stderrIn = pipes[4];
    int stderrOut = pipes[5];
    int statusIn = pipes[6];
    int statusOut = pipes[7];

    pid_t childPid = fork();

    // If fork() failed...
    if (childPid == -1) {
        jniThrowIOException(env, errno);
        closePipes(pipes, -1);
        return -1;
    }

    // If this is the child process...
    if (childPid == 0) {
        // Replace stdin, out, and err with pipes.
        dup2(stdinIn, 0);
        dup2(stdoutOut, 1);
        dup2(stderrOut, 2);

        // Close all but statusOut. This saves some work in the next step.
        closePipes(pipes, statusOut);

        // Make statusOut automatically close if execvp() succeeds.
        fcntl(statusOut, F_SETFD, FD_CLOEXEC);

        // Close remaining open fds with the exception of statusOut.
        closeNonStandardFds(statusOut);

        // Switch to working directory.
        if (workingDirectory != NULL) {
            if (chdir(workingDirectory) == -1) {
                goto execFailed;
            }
        }

        // Set up environment.
        if (environment != NULL) {
            environ = environment;
        }

        // Execute process. By convention, the first argument in the arg array
        // should be the command itself. In fact, I get segfaults when this
        // isn't the case.
        execvp(commands[0], commands);

        // If we got here, execvp() failed or the working dir was invalid.
        execFailed:
            error = errno;
            write(statusOut, &error, sizeof(int));
            close(statusOut);
            exit(error);
    }

    // This is the parent process.

    // Close child's pipe ends.
    close(stdinIn);
    close(stdoutOut);
    close(stderrOut);
    close(statusOut);

    // Check status pipe for an error code. If execvp() succeeds, the other
    // end of the pipe should automatically close, in which case, we'll read
    // nothing.
    int count = read(statusIn, &result, sizeof(int));
    close(statusIn);
    if (count > 0) {
        jniThrowIOException(env, result);

        close(stdoutIn);
        close(stdinOut);
        close(stderrIn);

        return -1;
    }

    // Fill in file descriptor wrappers.
    jniSetFileDescriptorOfFD(env, inDescriptor, stdoutIn);
    jniSetFileDescriptorOfFD(env, outDescriptor, stdinOut);
    jniSetFileDescriptorOfFD(env, errDescriptor, stderrIn);

    return childPid;
}

/** Converts a Java String[] to a 0-terminated char**. */
static char** convertStrings(JNIEnv* env, jobjectArray javaArray) {
    if (javaArray == NULL) {
        return NULL;
    }

    char** array = NULL;
    jsize length = (*env)->GetArrayLength(env, javaArray);
    array = (char**) malloc(sizeof(char*) * (length + 1));
    array[length] = 0;
    jsize index;
    for (index = 0; index < length; index++) {
        jstring javaEntry = (jstring) (*env)->GetObjectArrayElement(
                env, javaArray, index);
        char* entry = (char*) (*env)->GetStringUTFChars(
                env, javaEntry, NULL);
        array[index] = entry;
    }

    return array;
}

/** Frees a char** which was converted from a Java String[]. */
static void freeStrings(JNIEnv* env, jobjectArray javaArray, char** array) {
    if (javaArray == NULL) {
        return;
    }

    jsize length = (*env)->GetArrayLength(env, javaArray);
    jsize index;
    for (index = 0; index < length; index++) {
        jstring javaEntry = (jstring) (*env)->GetObjectArrayElement(
                env, javaArray, index);
        (*env)->ReleaseStringUTFChars(env, javaEntry, array[index]);
    }

    free(array);
}

/**
 * Converts Java String[] to char** and delegates to executeProcess().
 */
static pid_t java_lang_ProcessManager_exec(
        JNIEnv* env, jclass clazz, jobjectArray javaCommands,
        jobjectArray javaEnvironment, jstring javaWorkingDirectory,
        jobject inDescriptor, jobject outDescriptor, jobject errDescriptor) {

    // Copy commands into char*[].
    char** commands = convertStrings(env, javaCommands);

    // Extract working directory string.
    const char* workingDirectory = NULL;
    if (javaWorkingDirectory != NULL) {
        workingDirectory = (const char*) (*env)->GetStringUTFChars(
                env, javaWorkingDirectory, NULL);
    }

    // Convert environment array.
    char** environment = convertStrings(env, javaEnvironment);

    pid_t result = executeProcess(
            env, commands, environment, workingDirectory, 
            inDescriptor, outDescriptor, errDescriptor);

    // Temporarily clear exception so we can clean up.
    jthrowable exception = (*env)->ExceptionOccurred(env);
    (*env)->ExceptionClear(env);

    freeStrings(env, javaEnvironment, environment);

    // Clean up working directory string.
    if (javaWorkingDirectory != NULL) {
        (*env)->ReleaseStringUTFChars(
                env, javaWorkingDirectory, workingDirectory);
    }

    freeStrings(env, javaCommands, commands);

    // Re-throw exception if present.
    if (exception != NULL) {
        if ((*env)->Throw(env, exception) < 0) {
            LOGE("Error rethrowing exception!");
        }
    }

    return result;
}

/**
 * Looks up Java members.
 */
static void java_lang_ProcessManager_staticInitialize(JNIEnv* env,
        jclass clazz) {
#ifdef ANDROID
    char* fdString = getenv("ANDROID_PROPERTY_WORKSPACE");
    if (fdString) {
        androidSystemPropertiesFd = atoi(fdString);
    }
#endif

    onExitMethod = (*env)->GetMethodID(env, clazz, "onExit", "(II)V");
    if (onExitMethod == NULL) {
        return;
    }

    jclass fileDescriptorClass
            = (*env)->FindClass(env, "java/io/FileDescriptor");
    if (fileDescriptorClass == NULL) {
        return;
    }
    descriptorField = (*env)->GetFieldID(env, fileDescriptorClass,
            "descriptor", "I");
    if (descriptorField == NULL) {
        return;
    }
}

static JNINativeMethod methods[] = {
    { "kill", "(I)V", (void*) java_lang_ProcessManager_kill },
    { "watchChildren", "()V", (void*) java_lang_ProcessManager_watchChildren },
    { "exec", "([Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;"
        "Ljava/io/FileDescriptor;Ljava/io/FileDescriptor;"
        "Ljava/io/FileDescriptor;)I", (void*) java_lang_ProcessManager_exec },
    { "staticInitialize", "()V",
        (void*) java_lang_ProcessManager_staticInitialize },
    { "close", "(Ljava/io/FileDescriptor;)V",
        (void*) java_lang_ProcessManager_close },
};

int register_java_lang_ProcessManager(JNIEnv* env) {
    return jniRegisterNativeMethods(
            env, "java/lang/ProcessManager", methods, NELEM(methods));
}

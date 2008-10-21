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
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include "jni.h"
#include "JNIHelp.h"
#include "utils/Log.h"
#include "AndroidSystemNatives.h"

/** Environment variables. */
extern char **environ;

static jmethodID onExitMethod = NULL;
static jfieldID descriptorField = NULL;

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
    int result = kill((pid_t) pid, SIGTERM);
    if (result == -1) {
        LOGW("Error killing process %d: %s", pid, strerror(errno));
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

        pid_t pid = wait(&status);

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

/**
 * Executes a command in a child process.
 */
static pid_t executeProcess(JNIEnv* env,
        char** commands, char** environment,
        const char* workingDirectory,
        jobject inDescriptor, jobject outDescriptor, jobject errDescriptor) {
    int inPipe[2];
    int outPipe[2];
    int errPipe[2];

    // TODO: Ensure these pipe() calls succeed. Clean up if one call fails.
    pipe(inPipe);
    pipe(outPipe);
    pipe(errPipe);

    pid_t childPid = fork();

    // If fork() failed...
    if (childPid == -1) {
        LOGE("fork() failed: %s", strerror(errno));

        jniThrowIOException(env, errno);

        // Close pipes.
        close(inPipe[0]);
        close(inPipe[1]);
        close(outPipe[0]);
        close(outPipe[1]);
        close(errPipe[0]);
        close(errPipe[1]);

        return -1;
    }

    // If this is the child process...
    if (childPid == 0) {
        // Replace stdin, out, and err with pipes.
        dup2(inPipe[0], 0);
        dup2(outPipe[1], 1);
        dup2(errPipe[1], 2);

        // Switch to working directory.
        if (workingDirectory != NULL) {
            if (chdir(workingDirectory) == -1) {
                LOGE("Invalid working directory: %s", workingDirectory);
                exit(-1);
            }
        }

        // Set up environment.
        if (environment != NULL) {
            LOGI("Setting environment: %s", environment[0]);
            environ = environment;
        }

        // Execute process. By convention, the first argument in the arg array
        // should be the command itself. In fact, I get segfaults when this
        // isn't the case.
        int result = execvp(commands[0], commands);

        LOGE("Error running %s: %s", commands[0], strerror(errno));

        // If we got here, exec() failed.
        exit(result);
    }

    // This is the parent process.

    // TODO: Check results of close() calls.
    // Close child's fds.
    close(inPipe[0]);
    close(outPipe[1]);
    close(errPipe[1]);

    // Fill in file descriptors. inDescriptor read's child's stdout.
    // outDescriptor writes to child's stdin.
    jniSetFileDescriptorOfFD(env, inDescriptor, outPipe[0]);
    jniSetFileDescriptorOfFD(env, outDescriptor, inPipe[1]);
    jniSetFileDescriptorOfFD(env, errDescriptor, errPipe[0]);

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
    LOGV("*** Registering ProcessManager natives.");
    return jniRegisterNativeMethods(
            env, "java/lang/ProcessManager", methods, NELEM(methods));
}

/*
 * Copyright (C) 2006 The Android Open Source Project
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

/*
 * JNI helper functions.
 */
#define LOG_TAG "JNIHelp"
#include "JNIHelp.h"
#include "utils/Log.h"

#include <stdlib.h>
#include <string.h>
#include <assert.h>

/*
 * Register native JNI-callable methods.
 *
 * "className" looks like "java/lang/String".
 */
int jniRegisterNativeMethods(JNIEnv* env, const char* className,
    const JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    LOGV("Registering %s natives\n", className);
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s', aborting\n",
            className);
        abort();
    }

    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s', aborting\n", className);
        abort();
    }

    (*env)->DeleteLocalRef(env, clazz);
    return 0;
}

/*
 * Get a human-readable summary of an exception object.  The buffer will
 * be populated with the "binary" class name and, if present, the
 * exception message.
 */
static char* getExceptionSummary(JNIEnv* env, jthrowable exception) {
    char* result = NULL;

    /* get the name of the exception's class */
    jclass exceptionClazz = (*env)->GetObjectClass(env, exception); // can't fail
    jclass classClazz = (*env)->GetObjectClass(env, exceptionClazz); // java.lang.Class, can't fail
    jmethodID classGetNameMethod = (*env)->GetMethodID(
            env, classClazz, "getName", "()Ljava/lang/String;");
    jstring classNameStr = (*env)->CallObjectMethod(env, exceptionClazz, classGetNameMethod);
    if (classNameStr != NULL) {
        /* get printable string */
        const char* classNameChars = (*env)->GetStringUTFChars(env, classNameStr, NULL);
        if (classNameChars != NULL) {
            /* if the exception has a message string, get that */
            jmethodID throwableGetMessageMethod = (*env)->GetMethodID(
                    env, exceptionClazz, "getMessage", "()Ljava/lang/String;");
            jstring messageStr = (*env)->CallObjectMethod(
                    env, exception, throwableGetMessageMethod);

            if (messageStr == NULL) {
                result = strdup(classNameChars);
            } else {
                const char* messageChars = (*env)->GetStringUTFChars(env, messageStr, NULL);
                if (messageChars != NULL) {
                    asprintf(&result, "%s: %s", classNameChars, messageChars);
                    (*env)->ReleaseStringUTFChars(env, messageStr, messageChars);
                } else {
                    (*env)->ExceptionClear(env); // clear OOM
                    asprintf(&result, "%s: <error getting message>", classNameChars);
                }
                (*env)->DeleteLocalRef(env, messageStr);
            }

            (*env)->ReleaseStringUTFChars(env, classNameStr, classNameChars);
        }
        (*env)->DeleteLocalRef(env, classNameStr);
    }
    (*env)->DeleteLocalRef(env, classClazz);
    (*env)->DeleteLocalRef(env, exceptionClazz);

    if (result == NULL) {
        (*env)->ExceptionClear(env);
        result = strdup("<error getting class name>");
    }

    return result;
}

/*
 * Formats an exception as a string with its stack trace.
 */
static char* printStackTrace(JNIEnv* env, jthrowable exception) {
    char* result = NULL;

    jclass stringWriterClazz = (*env)->FindClass(env, "java/io/StringWriter");
    if (stringWriterClazz != NULL) {
        jmethodID stringWriterCtor = (*env)->GetMethodID(env, stringWriterClazz, "<init>", "()V");
        jmethodID stringWriterToStringMethod = (*env)->GetMethodID(env, stringWriterClazz,
                "toString", "()Ljava/lang/String;");

        jclass printWriterClazz = (*env)->FindClass(env, "java/io/PrintWriter");
        if (printWriterClazz != NULL) {
            jmethodID printWriterCtor = (*env)->GetMethodID(env, printWriterClazz,
                    "<init>", "(Ljava/io/Writer;)V");

            jobject stringWriterObj = (*env)->NewObject(env, stringWriterClazz, stringWriterCtor);
            if (stringWriterObj != NULL) {
                jobject printWriterObj = (*env)->NewObject(env, printWriterClazz, printWriterCtor,
                        stringWriterObj);
                if (printWriterObj != NULL) {
                    jclass exceptionClazz = (*env)->GetObjectClass(env, exception); // can't fail
                    jmethodID printStackTraceMethod = (*env)->GetMethodID(
                            env, exceptionClazz, "printStackTrace", "(Ljava/io/PrintWriter;)V");

                    (*env)->CallVoidMethod(env, exception, printStackTraceMethod, printWriterObj);
                    if (! (*env)->ExceptionCheck(env)) {
                        jstring messageStr = (*env)->CallObjectMethod(
                                env, stringWriterObj, stringWriterToStringMethod);
                        if (messageStr != NULL) {
                            const char* utfChars = (*env)->GetStringUTFChars(env, messageStr, NULL);
                            if (utfChars != NULL) {
                                result = strdup(utfChars);
                                (*env)->ReleaseStringUTFChars(env, messageStr, utfChars);
                            }
                            (*env)->DeleteLocalRef(env, messageStr);
                        }
                    }
                    (*env)->DeleteLocalRef(env, exceptionClazz);
                    (*env)->DeleteLocalRef(env, printWriterObj);
                }
                (*env)->DeleteLocalRef(env, stringWriterObj);
            }
            (*env)->DeleteLocalRef(env, printWriterClazz);
        }
        (*env)->DeleteLocalRef(env, stringWriterClazz);
    }

    if (result == NULL) {
        (*env)->ExceptionClear(env);
        result = getExceptionSummary(env, exception);
    }

    return result;
}

/*
 * Throw an exception with the specified class and an optional message.
 *
 * If an exception is currently pending, we log a warning message and
 * clear it.
 *
 * Returns 0 if the specified exception was successfully thrown.  (Some
 * sort of exception will always be pending when this returns.)
 */
int jniThrowException(JNIEnv* env, const char* className, const char* msg) {
    jclass exceptionClass;

    if ((*env)->ExceptionCheck(env)) {
        /* TODO: consider creating the new exception with this as "cause" */
        jthrowable exception = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionClear(env);

        if (exception != NULL) {
            char* text = getExceptionSummary(env, exception);
            LOGW("Discarding pending exception (%s) to throw %s", text, className);
            free(text);
            (*env)->DeleteLocalRef(env, exception);
        }
    }

    exceptionClass = (*env)->FindClass(env, className);
    if (exceptionClass == NULL) {
        LOGE("Unable to find exception class %s\n", className);
        /* ClassNotFoundException now pending */
        return -1;
    }

    int result = 0;
    if ((*env)->ThrowNew(env, exceptionClass, msg) != JNI_OK) {
        LOGE("Failed throwing '%s' '%s'\n", className, msg);
        /* an exception, most likely OOM, will now be pending */
        result = -1;
    }

    (*env)->DeleteLocalRef(env, exceptionClass);
    return result;
}

int jniThrowExceptionFmt(JNIEnv* env, const char* className, const char* fmt, va_list args) {
    char msgBuf[512];
    vsnprintf(msgBuf, sizeof(msgBuf), fmt, args);
    return jniThrowException(env, className, msgBuf);
}

/*
 * Throw a java.lang.NullPointerException, with an optional message.
 */
int jniThrowNullPointerException(JNIEnv* env, const char* msg) {
    return jniThrowException(env, "java/lang/NullPointerException", msg);
}

/*
 * Throw a java.lang.RuntimeException, with an optional message.
 */
int jniThrowRuntimeException(JNIEnv* env, const char* msg) {
    return jniThrowException(env, "java/lang/RuntimeException", msg);
}

/*
 * Throw a java.io.IOException, generating the message from errno.
 */
int jniThrowIOException(JNIEnv* env, int errnum) {
    char buffer[80];
    const char* message = jniStrError(errnum, buffer, sizeof(buffer));
    return jniThrowException(env, "java/io/IOException", message);
}

/*
 * Log an exception.
 * If exception is NULL, logs the current exception in the JNI environment, if any.
 */
void jniLogException(JNIEnv* env, int priority, const char* tag, jthrowable exception) {
    int currentException = 0;
    if (exception == NULL) {
        exception = (*env)->ExceptionOccurred(env);
        if (exception == NULL) {
            return;
        }

        (*env)->ExceptionClear(env);
        currentException = 1;
    }

    char* buffer = printStackTrace(env, exception);
    __android_log_write(priority, tag, buffer);
    free(buffer);

    if (currentException) {
        (*env)->Throw(env, exception); // rethrow
        (*env)->DeleteLocalRef(env, exception);
    }
}

const char* jniStrError(int errnum, char* buf, size_t buflen) {
    // note: glibc has a nonstandard strerror_r that returns char* rather
    // than POSIX's int.
    // char *strerror_r(int errnum, char *buf, size_t n);
    char* ret = (char*) strerror_r(errnum, buf, buflen);
    if (((int)ret) == 0) {
        //POSIX strerror_r, success
        return buf;
    } else if (((int)ret) == -1) {
        //POSIX strerror_r, failure
        // (Strictly, POSIX only guarantees a value other than 0. The safest
        // way to implement this function is to use C++ and overload on the
        // type of strerror_r to accurately distinguish GNU from POSIX. But
        // realistic implementations will always return -1.)
        snprintf(buf, buflen, "errno %d", errnum);
        return buf;
    } else {
        //glibc strerror_r returning a string
        return ret;
    }
}

static struct CachedFields {
    jclass fileDescriptorClass;
    jmethodID fileDescriptorCtor;
    jfieldID descriptorField;
} gCachedFields;

int registerJniHelp(JNIEnv* env) {
    gCachedFields.fileDescriptorClass =
            (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/io/FileDescriptor"));
    if (gCachedFields.fileDescriptorClass == NULL) {
        return -1;
    }

    gCachedFields.fileDescriptorCtor =
            (*env)->GetMethodID(env, gCachedFields.fileDescriptorClass, "<init>", "()V");
    if (gCachedFields.fileDescriptorCtor == NULL) {
        return -1;
    }

    gCachedFields.descriptorField =
            (*env)->GetFieldID(env, gCachedFields.fileDescriptorClass, "descriptor", "I");
    if (gCachedFields.descriptorField == NULL) {
        return -1;
    }

    return 0;
}

/*
 * Create a java.io.FileDescriptor given an integer fd
 */
jobject jniCreateFileDescriptor(JNIEnv* env, int fd) {
    jobject fileDescriptor = (*env)->NewObject(env,
            gCachedFields.fileDescriptorClass, gCachedFields.fileDescriptorCtor);
    jniSetFileDescriptorOfFD(env, fileDescriptor, fd);
    return fileDescriptor;
}

/*
 * Get an int file descriptor from a java.io.FileDescriptor
 */
int jniGetFDFromFileDescriptor(JNIEnv* env, jobject fileDescriptor) {
    return (*env)->GetIntField(env, fileDescriptor, gCachedFields.descriptorField);
}

/*
 * Set the descriptor of a java.io.FileDescriptor
 */
void jniSetFileDescriptorOfFD(JNIEnv* env, jobject fileDescriptor, int value) {
    (*env)->SetIntField(env, fileDescriptor, gCachedFields.descriptorField, value);
}

/*
 * DO NOT USE THIS FUNCTION
 *
 * Get a pointer to the elements of a non-movable array.
 *
 * The semantics are similar to GetDirectBufferAddress.  Specifically, the VM
 * guarantees that the array will not move, and the caller must ensure that
 * it does not continue to use the pointer after the object is collected.
 *
 * We currently use an illegal sequence that trips up CheckJNI when
 * the "forcecopy" mode is enabled.  We pass in a magic value to work
 * around the problem.
 *
 * Returns NULL if the array is movable.
 */
jbyte* jniGetNonMovableArrayElements(JNIEnv* env, jarray arrayObj) {
#define kNoCopyMagic 0xd5aab57f     /* also in CheckJni.c */

    /*
     * Normally the "isCopy" parameter is for a return value only, so the
     * non-CheckJNI VM will ignore whatever we pass in.
     */
    uint32_t noCopy = kNoCopyMagic;
    jbyte *addr = (*env)->GetByteArrayElements(env, arrayObj,
            (jboolean*)&noCopy);

    /*
     * The non-CheckJNI implementation only cares about the array object,
     * so we can replace the element pointer with the magic value.
     */
    (*env)->ReleaseByteArrayElements(env, arrayObj, (jbyte*) kNoCopyMagic, 0);
    return addr;
}

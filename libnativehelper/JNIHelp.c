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
        LOGE("Native registration unable to find class '%s'\n", className);
        return -1;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'\n", className);
        return -1;
    }
    return 0;
}

/*
 * Get a human-readable summary of an exception object.  The buffer will
 * be populated with the "binary" class name and, if present, the
 * exception message.
 */
static void getExceptionSummary(JNIEnv* env, jthrowable excep, char* buf,
    size_t bufLen)
{
    if (excep == NULL)
        return;

    /* get the name of the exception's class; none of these should fail */
    jclass clazz = (*env)->GetObjectClass(env, excep); // exception's class
    jclass jlc = (*env)->GetObjectClass(env, clazz);   // java.lang.Class
    jmethodID getNameMethod =
        (*env)->GetMethodID(env, jlc, "getName", "()Ljava/lang/String;");
    jstring className = (*env)->CallObjectMethod(env, clazz, getNameMethod);

    /* get printable string */
    const char* nameStr = (*env)->GetStringUTFChars(env, className, NULL);
    if (nameStr == NULL) {
        snprintf(buf, bufLen, "%s", "out of memory generating summary");
        (*env)->ExceptionClear(env);            // clear OOM
        return;
    }

    /* if the exception has a message string, get that */
    jmethodID getThrowableMessage =
        (*env)->GetMethodID(env, clazz, "getMessage", "()Ljava/lang/String;");
    jstring message = (*env)->CallObjectMethod(env, excep, getThrowableMessage);

    if (message != NULL) {
        const char* messageStr = (*env)->GetStringUTFChars(env, message, NULL);
        snprintf(buf, bufLen, "%s: %s", nameStr, messageStr);
        if (messageStr != NULL)
            (*env)->ReleaseStringUTFChars(env, message, messageStr);
        else
            (*env)->ExceptionClear(env);        // clear OOM
    } else {
        strncpy(buf, nameStr, bufLen);
        buf[bufLen-1] = '\0';
    }

    (*env)->ReleaseStringUTFChars(env, className, nameStr);
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
int jniThrowException(JNIEnv* env, const char* className, const char* msg)
{
    jclass exceptionClass;

    if ((*env)->ExceptionCheck(env)) {
        /* TODO: consider creating the new exception with this as "cause" */
        char buf[256];

        jthrowable excep = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionClear(env);
        getExceptionSummary(env, excep, buf, sizeof(buf));
        LOGW("Discarding pending exception (%s) to throw %s\n",
            buf, className);
    }

    exceptionClass = (*env)->FindClass(env, className);
    if (exceptionClass == NULL) {
        LOGE("Unable to find exception class %s\n", className);
        /* ClassNotFoundException now pending */
        return -1;
    }

    if ((*env)->ThrowNew(env, exceptionClass, msg) != JNI_OK) {
        LOGE("Failed throwing '%s' '%s'\n", className, msg);
        /* an exception, most likely OOM, will now be pending */
        return -1;
    }
    return 0;
}

/*
 * Throw a java.lang.RuntimeException, with an optional message.
 */
int jniThrowRuntimeException(JNIEnv* env, const char* msg)
{
    return jniThrowException(env, "java/lang/RuntimeException", msg);
}

/*
 * Throw a java.io.IOException, generating the message from errno.
 */
int jniThrowIOException(JNIEnv* env, int errnum)
{
    char buffer[80];
    const char* message = jniStrError(errnum, buffer, sizeof(buffer));
    return jniThrowException(env, "java/io/IOException", message);
}

const char* jniStrError(int errnum, char* buf, size_t buflen)
{
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

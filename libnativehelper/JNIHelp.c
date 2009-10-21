/*
 * Copyright 2006 The Android Open Source Project
 *
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
 * Throw an exception with the specified class and an optional message.
 */
int jniThrowException(JNIEnv* env, const char* className, const char* msg)
{
    jclass exceptionClass;

    exceptionClass = (*env)->FindClass(env, className);
    if (exceptionClass == NULL) {
        LOGE("Unable to find exception class %s\n", className);
        assert(0);      /* fatal during dev; should always be fatal? */
        return -1;
    }

    if ((*env)->ThrowNew(env, exceptionClass, msg) != JNI_OK) {
        LOGE("Failed throwing '%s' '%s'\n", className, msg);
        assert(!"failed to throw");
    }
    return 0;
}

/*
 * Throw a java.IO.IOException, generating the message from errno.
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

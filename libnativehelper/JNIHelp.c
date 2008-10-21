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
    // note: glibc has a nonstandard
    // strerror_r that looks like this:
    // char *strerror_r(int errnum, char *buf, size_t n);

    const char* message;
    char buffer[80];
    char* ret;

    buffer[0] = 0;
    ret = (char*) strerror_r(errnum, buffer, sizeof(buffer));

    if (((int)ret) == 0) {
        //POSIX strerror_r, success
        message = buffer;
    } else if (((int)ret) == -1) {
        //POSIX strerror_r, failure

        snprintf (buffer, sizeof(buffer), "errno %d", errnum);
        message = buffer;
    } else {
        //glibc strerror_r returning a string
        message = ret;
    }

    return jniThrowException(env, "java/io/IOException", message);
}


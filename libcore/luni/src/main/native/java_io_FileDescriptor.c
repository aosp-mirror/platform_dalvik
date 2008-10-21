/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
#include "JNIHelp.h"

#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <assert.h>
#include <sys/ioctl.h>

/*
 * These are JNI field IDs for the stuff we're interested in.  They're
 * computed when the class is loaded.
 */
static struct {
    jfieldID    descriptor;       /* int */
    jmethodID   constructorInt;
    jmethodID   setFD;
    jclass      clazz;
} gCachedFields;

/*
 * Internal helper function.
 *
 * Get the file descriptor.
 */
static inline int getFd(JNIEnv* env, jobject obj)
{
    return (*env)->GetIntField(env, obj, gCachedFields.descriptor);
}

/*
 * Internal helper function.
 *
 * Set the file descriptor.
 */
static inline void setFd(JNIEnv* env, jobject obj, jint value)
{
    (*env)->SetIntField(env, obj, gCachedFields.descriptor, value);
}

/*
 * native private static void nativeClassInit()
 *
 * Perform one-time initialization.  If the class is unloaded and re-loaded,
 * this will be called again.
 */
static void nativeClassInit(JNIEnv* env, jclass clazz)
{
    gCachedFields.clazz = (*env)->NewGlobalRef(env, clazz);

    gCachedFields.descriptor =
        (*env)->GetFieldID(env, clazz, "descriptor", "I");

    if(gCachedFields.descriptor == NULL) {
        jniThrowException(env, "java/lang/NoSuchFieldError", "FileDescriptor");
        return;
    }

    gCachedFields.constructorInt =
        (*env)->GetMethodID(env, clazz, "<init>", "()V");

    if(gCachedFields.constructorInt == NULL) {
        jniThrowException(env, "java/lang/NoSuchMethodError", "<init>()V");
        return;
    }
}

/*
 * public native void sync()
 */
static void fd_sync(JNIEnv* env, jobject obj) {
    int fd = getFd(env, obj);

    if (fsync(fd) != 0) {
        /*
         * If fd is a socket, then fsync(fd) is defined to fail with
         * errno EINVAL. This isn't actually cause for concern.
         * TODO: Look into not bothering to call fsync() at all if
         * we know we are dealing with a socket.
         */
        if (errno != EINVAL) {
            jniThrowException(env, "java/io/SyncFailedException", "");
        }
    }
}

/*
 * public native boolean valid()
 */
static jboolean fd_valid(JNIEnv* env, jobject obj) {
    int fd = getFd(env, obj);
    struct stat sb;

    if(fstat(fd, &sb) == 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

/* checks to see if class is inited and inits if needed, returning -1
 * on fail and 0 on success
 */
static int checkClassInit (JNIEnv *env) {
    if(gCachedFields.clazz == NULL) {
        /* this should cause the class to be inited and
         * our static variables to be filled in
         *
         * (Note that FindClass just loads the class; it doesn't get
         * initialized until we try to do something with it.)
         */
        jclass clazz;
        clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
        if(clazz == NULL) {
            jniThrowException(env, "java/lang/ClassNotFoundException", 
                                    "java.io.FileDescriptor");
            return -1;
        }

        jfieldID readWriteId;
        readWriteId = (*env)->GetStaticFieldID(env, clazz, "in", 
                "Ljava/io/FileDescriptor;");
        if(readWriteId == NULL) {
            jniThrowException(env, "java/lang/NoSuchFieldException", 
                                    "FileDescriptor.readOnly(Z)");
            return -1;
        }

        (void) (*env)->GetStaticObjectField(env, clazz, readWriteId);
    }

    return 0;
}


/*
 * For JNIHelp.c
 * Create a java.io.FileDescriptor given an integer fd
 */

jobject jniCreateFileDescriptor (JNIEnv *env, int fd) {
    jobject ret;

    /* the class may not have been loaded yet */
    if(checkClassInit(env) < 0) {
        return NULL;
    }

    ret = (*env)->NewObject(env, gCachedFields.clazz,
            gCachedFields.constructorInt);
    
    (*env)->SetIntField(env, ret, gCachedFields.descriptor, fd);

    return ret;
}

/* 
 * For JNIHelp.c
 * Get an int file descriptor from a java.io.FileDescriptor
 */

int jniGetFDFromFileDescriptor (JNIEnv* env, jobject fileDescriptor) {
    /* should already be initialized if it's an actual FileDescriptor */
    assert(fileDescriptor != NULL);
    assert(gCachedFields.clazz != NULL);

    return getFd(env, fileDescriptor);
}

/*
 * For JNIHelp.c
 * Set the descriptor of a java.io.FileDescriptor
 */

void jniSetFileDescriptorOfFD (JNIEnv* env, jobject fileDescriptor, int value) {
    /* should already be initialized if it's an actual FileDescriptor */
    assert(fileDescriptor != NULL);
    assert(gCachedFields.clazz != NULL);

    setFd(env, fileDescriptor, value);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "oneTimeInitialization", "()V",              nativeClassInit },
    { "syncImpl",           "()V",                 fd_sync },
    { "valid",          "()Z",                     fd_valid }
};
int register_java_io_FileDescriptor(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/io/FileDescriptor",
        gMethods, NELEM(gMethods));
}

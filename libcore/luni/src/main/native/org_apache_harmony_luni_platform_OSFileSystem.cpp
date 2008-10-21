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
/*
 * Common natives supporting the file system interface.
 */

#define HyMaxPath 1024
#define HyOpenRead    1       /* Values for HyFileOpen */
#define HyOpenWrite   2
#define HyOpenCreate  4
#define HyOpenTruncate  8
#define HyOpenAppend  16
#define HyOpenText    32

/* Use this flag with HyOpenCreate, if this flag is specified then
 * trying to create an existing file will fail 
 */
#define HyOpenCreateNew 64
#define HyOpenSync		128
#define SHARED_LOCK_TYPE 1L

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <sys/sendfile.h>
#include <sys/uio.h>
#include <fcntl.h>
#include <sys/ioctl.h>

typedef struct socket_struct {
    int sock;
    unsigned short family;
} socket_struct;

static void convertToPlatform(char *path) {
    char *pathIndex;

    pathIndex = path;
    while (*pathIndex != '\0') {
        if(*pathIndex == '\\') {
            *pathIndex = '/';
        }
        pathIndex++;
    }
}

static int
EsTranslateOpenFlags(int flags) {
    int realFlags = 0;

    if(flags & HyOpenAppend) {
        realFlags |= O_APPEND;
    }
    if(flags & HyOpenTruncate) {
        realFlags |= O_TRUNC;
    }
    if(flags & HyOpenCreate) {
        realFlags |= O_CREAT;
    }
    if(flags & HyOpenCreateNew) {
        realFlags |= O_EXCL | O_CREAT;
    }
#ifdef O_SYNC
	if(flags & HyOpenSync) {
		realFlags |= O_SYNC;
	}
#endif    
    if(flags & HyOpenRead) {
        if(flags & HyOpenWrite) {
            return (O_RDWR | realFlags);
        }
        return (O_RDONLY | realFlags);
    }
    if(flags & HyOpenWrite) {
        return (O_WRONLY | realFlags);
    }
    return -1;
}

/**
 * Lock the file identified by the given handle.
 * The range and lock type are given.
 */
static jint harmony_io_lockImpl(JNIEnv * env, jobject thiz, jint handle, 
        jlong start, jlong length, jint typeFlag, jboolean waitFlag) {

    int rc;
    int waitMode = (waitFlag) ? F_SETLKW : F_SETLK;
    struct flock lock;

    memset(&lock, 0, sizeof(lock));

    // If start or length overflow the max values we can represent, then max them out.
    if(start > 0x7fffffffL) {
        start = 0x7fffffffL;
    }
    if(length > 0x7fffffffL) {
        length = 0x7fffffffL;
    }

    lock.l_whence = SEEK_SET;
    lock.l_start = start;
    lock.l_len = length;

    if((typeFlag & SHARED_LOCK_TYPE) == SHARED_LOCK_TYPE) {
        lock.l_type = F_RDLCK;
    } else {
        lock.l_type = F_WRLCK;
    }

    do {
        rc = fcntl(handle, waitMode, &lock);
    } while ((rc < 0) && (errno == EINTR));

    return (rc == -1) ? -1 : 0;
}

/**
 * Unlocks the specified region of the file.
 */
static jint harmony_io_unlockImpl(JNIEnv * env, jobject thiz, jint handle, 
        jlong start, jlong length) {

    int rc;
    struct flock lock;

    memset(&lock, 0, sizeof(lock));

    // If start or length overflow the max values we can represent, then max them out.
    if(start > 0x7fffffffL) {
        start = 0x7fffffffL;
    }
    if(length > 0x7fffffffL) {
        length = 0x7fffffffL;
    }

    lock.l_whence = SEEK_SET;
    lock.l_start = start;
    lock.l_len = length;
    lock.l_type = F_UNLCK;

    do {
        rc = fcntl(handle, F_SETLKW, &lock);
    } while ((rc < 0) && (errno == EINTR));

    return (rc == -1) ? -1 : 0;
}

/**
 * Returns the granularity of the starting address for virtual memory allocation.
 * (It's the same as the page size.)
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    getAllocGranularity
 * Signature: ()I
 */
static jint harmony_io_getAllocGranularity(JNIEnv * env, jobject thiz) {
    static int allocGranularity = 0;
    if(allocGranularity == 0) {
        allocGranularity = getpagesize();
    }
    return allocGranularity;
}

/*
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    readvImpl
 * Signature: (I[J[I[I)J
 */
static jlong harmony_io_readvImpl(JNIEnv *env, jobject thiz, jint fd, 
        jintArray jbuffers, jintArray joffsets, jintArray jlengths, jint size) {

    jboolean bufsCopied = JNI_FALSE;
    jboolean offsetsCopied = JNI_FALSE;
    jboolean lengthsCopied = JNI_FALSE;
    jint *bufs; 
    jint *offsets;
    jint *lengths;
    int i = 0;
    long totalRead = 0;  
    struct iovec *vectors = (struct iovec *)malloc(size * sizeof(struct iovec));
    if(vectors == NULL) {
        return -1;
    }
    bufs = env->GetIntArrayElements(jbuffers, &bufsCopied);
    offsets = env->GetIntArrayElements(joffsets, &offsetsCopied);
    lengths = env->GetIntArrayElements(jlengths, &lengthsCopied);
    while(i < size) {
        vectors[i].iov_base = (void *)((int)(bufs[i]+offsets[i]));
        vectors[i].iov_len = lengths[i];
        i++;
    }
    totalRead = readv(fd, vectors, size);
    if(bufsCopied) {
        env->ReleaseIntArrayElements(jbuffers, bufs, JNI_ABORT);
    }
    if(offsetsCopied) {
        env->ReleaseIntArrayElements(joffsets, offsets, JNI_ABORT);
    }
    if(lengthsCopied) {
        env->ReleaseIntArrayElements(jlengths, lengths, JNI_ABORT);
    }
    free(vectors);
    return totalRead;
}

/*
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    writevImpl
 * Signature: (I[J[I[I)J
 */
static jlong harmony_io_writevImpl(JNIEnv *env, jobject thiz, jint fd, 
        jintArray jbuffers, jintArray joffsets, jintArray jlengths, jint size) {

    jboolean bufsCopied = JNI_FALSE;
    jboolean offsetsCopied = JNI_FALSE;
    jboolean lengthsCopied = JNI_FALSE;
    jint *bufs; 
    jint *offsets;
    jint *lengths;
    int i = 0;
    long totalRead = 0;  
    struct iovec *vectors = (struct iovec *)malloc(size * sizeof(struct iovec));
    if(vectors == NULL) {
        return -1;
    }
    bufs = env->GetIntArrayElements(jbuffers, &bufsCopied);
    offsets = env->GetIntArrayElements(joffsets, &offsetsCopied);
    lengths = env->GetIntArrayElements(jlengths, &lengthsCopied);
    while(i < size) {
        vectors[i].iov_base = (void *)((int)(bufs[i]+offsets[i]));
        vectors[i].iov_len = lengths[i];
        i++;
    }
    totalRead = writev(fd, vectors, size);
    if(bufsCopied) {
        env->ReleaseIntArrayElements(jbuffers, bufs, JNI_ABORT);
    }
    if(offsetsCopied) {
        env->ReleaseIntArrayElements(joffsets, offsets, JNI_ABORT);
    }
    if(lengthsCopied) {
        env->ReleaseIntArrayElements(jlengths, lengths, JNI_ABORT);
    }
    free(vectors);
    return totalRead;
}

/*
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    transferImpl
 * Signature: (IJJ)J
 */
static jlong harmony_io_transferImpl(JNIEnv *env, jobject thiz, jint fd, 
        jobject sd, jlong offset, jlong count) {

    int socket;
    off_t off;

    socket = jniGetFDFromFileDescriptor(env, sd);
    if(socket == 0 || socket == -1) {
        return -1;
    }

    /* Value of offset is checked in jint scope (checked in java layer)
       The conversion here is to guarantee no value lost when converting offset to off_t
     */
    off = offset;

    return sendfile(socket,(int)fd,(off_t *)&off,(size_t)count);
}

/*
 * Class:     org_apache_harmony_io
 * Method:    readDirectImpl
 * Signature: (IJI)J
 */
static jlong harmony_io_readDirectImpl(JNIEnv * env, jobject thiz, jint fd, 
        jint buf, jint offset, jint nbytes) {
    jint result;
    if(nbytes == 0) {
        return (jlong) 0;
    }

    result = read(fd, (void *) ((jint *)(buf+offset)), (int) nbytes);
    if(result == 0) {
        return (jlong) -1;
    } else {
        return (jlong) result;
    }
}

/*
 * Class:     org_apache_harmony_io
 * Method:    writeDirectImpl
 * Signature: (IJI)J
 */
static jlong harmony_io_writeDirectImpl(JNIEnv * env, jobject thiz, jint fd, 
        jint buf, jint offset, jint nbytes) {


    int rc = 0;

    /* write will just do the right thing for HYPORT_TTY_OUT and HYPORT_TTY_ERR */
    rc = write (fd, (const void *) ((jint *)(buf+offset)), (int) nbytes);

    if(rc == -1) {
        jniThrowException(env, "java/io/IOException", strerror(errno));
        return -2;
    }
    return (jlong) rc;

}

// BEGIN android-changed
/*
 * Class:     org_apache_harmony_io
 * Method:    readImpl
 * Signature: (I[BII)J
 */
static jlong harmony_io_readImpl(JNIEnv * env, jobject thiz, jint fd, 
        jbyteArray byteArray, jint offset, jint nbytes) {

    jboolean isCopy;
    jbyte *bytes;
    jlong result;

    if (nbytes == 0) {
        return 0;
    }

    bytes = env->GetByteArrayElements(byteArray, &isCopy);

    for (;;) {
        result = read(fd, (void *) (bytes + offset), (int) nbytes);

        if ((result != -1) || (errno != EINTR)) {
            break;
        }

        /*
         * If we didn't break above, that means that the read() call
         * returned due to EINTR. We shield Java code from this
         * possibility by trying again. Note that this is different
         * from EAGAIN, which should result in this code throwing
         * an InterruptedIOException.
         */
    }

    env->ReleaseByteArrayElements(byteArray, bytes, 0);

    if (result == 0) {
        return -1;
    }
    
    if (result == -1) {
        if (errno == EAGAIN) {
            jniThrowException(env, "java/io/InterruptedIOException",
                    "Read timed out");
        } else {
        jniThrowException(env, "java/io/IOException", strerror(errno));
        }
    }

    return result;
}

/*
 * Class:     org_apache_harmony_io
 * Method:    writeImpl
 * Signature: (I[BII)J
 */
static jlong harmony_io_writeImpl(JNIEnv * env, jobject thiz, jint fd, 
        jbyteArray byteArray, jint offset, jint nbytes) {

    jboolean isCopy;
    jbyte *bytes = env->GetByteArrayElements(byteArray, &isCopy);
    jlong result;

    for (;;) {
        result = write(fd, (const char *) bytes + offset, (int) nbytes);
        
        if ((result != -1) || (errno != EINTR)) {
            break;
        }

        /*
         * If we didn't break above, that means that the read() call
         * returned due to EINTR. We shield Java code from this
         * possibility by trying again. Note that this is different
         * from EAGAIN, which should result in this code throwing
         * an InterruptedIOException.
         */
    }

    env->ReleaseByteArrayElements(byteArray, bytes, JNI_ABORT);

    if (result == -1) {
        if (errno == EAGAIN) {
            jniThrowException(env, "java/io/InterruptedIOException",
                    "Write timed out");
        } else {
        jniThrowException(env, "java/io/IOException", strerror(errno));
        }
    }

    return result;
}
// END android-changed

/**
 * Seeks a file descriptor to a given file position.
 * 
 * @param env pointer to Java environment
 * @param thiz pointer to object receiving the message
 * @param fd handle of file to be seeked
 * @param offset distance of movement in bytes relative to whence arg
 * @param whence enum value indicating from where the offset is relative
 * The valid values are defined in fsconstants.h.
 * @return the new file position from the beginning of the file, in bytes;
 * or -1 if a problem occurs.
 */
static jlong harmony_io_seekImpl(JNIEnv * env, jobject thiz, jint fd, 
        jlong offset, jint whence) {

    int mywhence = 0;

    /* Convert whence argument */
    switch (whence) {
        case 1:
                mywhence = 0;
                break;
        case 2:
                mywhence = 1;
                break;
        case 4:
                mywhence = 2;
                break;
        default:
                return -1;
    }


    off_t localOffset = (int) offset;

    if((mywhence < 0) || (mywhence > 2)) {
        return -1;
    }

    /* If file offsets are 32 bit, truncate the seek to that range */
    if(sizeof (off_t) < sizeof (jlong)) {
        if(offset > 0x7FFFFFFF) {
            localOffset = 0x7FFFFFFF;
        } else if(offset < -0x7FFFFFFF) {
            localOffset = -0x7FFFFFFF;
        }
    }

    return (jlong) lseek(fd, localOffset, mywhence);
}

/**
 * Flushes a file state to disk.
 *
 * @param env pointer to Java environment
 * @param thiz pointer to object receiving the message
 * @param fd handle of file to be flushed
 * @param metadata if true also flush metadata, 
 *         otherwise just flush data is possible.
 * @return zero on success and -1 on failure
 *
 * Method:    fflushImpl
 * Signature: (IZ)I
 */
static jint harmony_io_fflushImpl(JNIEnv * env, jobject thiz, jint fd, 
        jboolean metadata) {
    return (jint) fsync(fd);
}

// BEGIN android-changed
/**
 * Closes the given file handle
 * 
 * @param env pointer to Java environment
 * @param thiz pointer to object receiving the message
 * @param fd handle of file to be closed
 * @return zero on success and -1 on failure
 *
 * Class:     org_apache_harmony_io
 * Method:    closeImpl
 * Signature: (I)I
 */
static jint harmony_io_closeImpl(JNIEnv * env, jobject thiz, jint fd) {
    jint result;

    for (;;) {
        result = (jint) close(fd);
        
        if ((result != -1) || (errno != EINTR)) {
            break;
        }

        /*
         * If we didn't break above, that means that the close() call
         * returned due to EINTR. We shield Java code from this
         * possibility by trying again.
         */
    }

    return result;
}
// END android-changed


/*
 * Class:     org_apache_harmony_io
 * Method:    truncateImpl
 * Signature: (IJ)I
 */
static jint harmony_io_truncateImpl(JNIEnv * env, jobject thiz, jint fd, 
        jlong size) {

    int rc;
    off_t length = (off_t) size;

    // If file offsets are 32 bit, truncate the newLength to that range
    if(sizeof (off_t) < sizeof (jlong)) {
        if(length > 0x7FFFFFFF) {
            length = 0x7FFFFFFF;
        } else if(length < -0x7FFFFFFF) {
            length = -0x7FFFFFFF;
        }
    }

  rc = ftruncate((int)fd, length);

  return (jint) rc;

}

/*
 * Class:     org_apache_harmony_io
 * Method:    openImpl
 * Signature: ([BI)I
 */
static jint harmony_io_openImpl(JNIEnv * env, jobject obj, jbyteArray path, 
        jint jflags) {
    
    int flags = 0;
    int mode = 0; 
    jint * portFD;
    jsize length;
    char pathCopy[HyMaxPath];

// BEGIN android-changed
// don't want default permissions to allow global access.
    switch(jflags) {
      case 0:
              flags = HyOpenRead;
              mode = 0;
              break;
      case 1:
              flags = HyOpenCreate | HyOpenWrite | HyOpenTruncate;
              mode = 0600;
              break;
      case 16:
              flags = HyOpenRead | HyOpenWrite | HyOpenCreate;
              mode = 0600;
              break;
      case 32:
              flags = HyOpenRead | HyOpenWrite | HyOpenCreate | HyOpenSync;
              mode = 0600;
              break;
      case 256:
              flags = HyOpenWrite | HyOpenCreate | HyOpenAppend; 
              mode = 0600;
              break;
    }
// BEGIN android-changed

    flags = EsTranslateOpenFlags(flags);

    length = env->GetArrayLength (path);
    length = length < HyMaxPath - 1 ? length : HyMaxPath - 1;
    env->GetByteArrayRegion (path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform (pathCopy);

    int cc;
    
    if(pathCopy == NULL) {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
        return -1;
    }

    do {
        cc = open(pathCopy, flags, mode);
    } while(cc < 0 && errno == EINTR);

    if(cc < 0 && errno > 0) {
        cc = -errno;
    }

    return cc;


}

// BEGIN android-deleted
#if 0
/*
 * Answers the number of remaining chars in the stdin.
 *
 * Class:     org_apache_harmony_io
 * Method:    ttyAvailableImpl
 * Signature: ()J
 */
static jlong harmony_io_ttyAvailableImpl(JNIEnv *env, jobject thiz) {
  
    int rc;
    off_t curr, end;

    int avail = 0;

    // when redirected from a file
    curr = lseek(STDIN_FILENO, 0L, 2);    /* don't use tell(), it doesn't exist on all platforms, i.e. linux */
    if(curr != -1) {
        end = lseek(STDIN_FILENO, 0L, 4);
        lseek(STDIN_FILENO, curr, 1);
        if(end >= curr) {
            return (jlong) (end - curr);
        }
    }

    /* ioctl doesn't work for files on all platforms (i.e. SOLARIS) */

    rc = ioctl (STDIN_FILENO, FIONREAD, &avail);

    /* 64 bit platforms use a 32 bit value, using IDATA fails on big endian */
    /* Pass in IDATA because ioctl() is device dependent, some devices may write 64 bits */
    if(rc != -1) {
        return (jlong) *(jint *) & avail;
    }
    return (jlong) 0;
}
#endif
// END android-deleted

// BEGIN android-added
/*
 * Answers the number of remaining bytes in a file descriptor
 * using IOCTL.
 *
 * Class:     org_apache_harmony_io
 * Method:    ioctlAvailable
 * Signature: ()I
 */
static jint harmony_io_ioctlAvailable(JNIEnv *env, jobject thiz, jint fd) {
    int avail = 0;
    int rc = ioctl(fd, FIONREAD, &avail);

    /*
     * On underlying platforms Android cares about (read "Linux"),
     * ioctl(fd, FIONREAD, &avail) is supposed to do the following:
     * 
     * If the fd refers to a regular file, avail is set to
     * the difference between the file size and the current cursor.
     * This may be negative if the cursor is past the end of the file.
     * 
     * If the fd refers to an open socket or the read end of a
     * pipe, then avail will be set to a number of bytes that are
     * available to be read without blocking.
     * 
     * If the fd refers to a special file/device that has some concept
     * of buffering, then avail will be set in a corresponding way.
     * 
     * If the fd refers to a special device that does not have any
     * concept of buffering, then the ioctl call will return a negative
     * number, and errno will be set to ENOTTY.
     * 
     * If the fd refers to a special file masquerading as a regular file,
     * then avail may be returned as negative, in that the special file
     * may appear to have zero size and yet a previous read call may have
     * actually read some amount of data and caused the cursor to be
     * advanced.
     */

    if (rc >= 0) {
        /*
         * Success, but make sure not to return a negative number (see
         * above).
         */ 
        if (avail < 0) {
            avail = 0;
        }
    } else if (errno == ENOTTY) {
        /* The fd is unwilling to opine about its read buffer. */
        avail = 0;
    } else {
        /* Something strange is happening. */
        jniThrowException(env, "java/io/IOException", strerror(errno));
        avail = 0;
    }  

    return (jint) avail;
}
// END android-added

/*
 * Reads the number of bytes from stdin.
 *
 * Class:     org_apache_harmony_io
 * Method:    ttyReadImpl
 * Signature: ([BII)J
 */
static jlong harmony_io_ttyReadImpl(JNIEnv *env, jobject thiz, 
        jbyteArray byteArray, jint offset, jint nbytes) {
  
    jboolean isCopy;
    jbyte *bytes = env->GetByteArrayElements(byteArray, &isCopy);
    jlong result;

    for(;;) {

        result = (jlong) read(STDIN_FILENO, (char *)(bytes + offset), (int) nbytes);

        if ((result != -1) || (errno != EINTR)) {
            break;
        }

        /*
         * If we didn't break above, that means that the read() call
         * returned due to EINTR. We shield Java code from this
         * possibility by trying again. Note that this is different
         * from EAGAIN, which should result in this code throwing
         * an InterruptedIOException.
         */
    }

    env->ReleaseByteArrayElements(byteArray, bytes, 0);

    if (result == 0) {
        return -1;
    }
    
    if (result == -1) {
        if (errno == EAGAIN) {
            jniThrowException(env, "java/io/InterruptedIOException",
                    "Read timed out");
        } else {
            jniThrowException(env, "java/io/IOException", strerror(errno));
        }
    }

    return result;
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "lockImpl",           "(IJJIZ)I",   (void*) harmony_io_lockImpl },
    { "getAllocGranularity","()I",     (void*) harmony_io_getAllocGranularity },
    { "unlockImpl",         "(IJJ)I",     (void*) harmony_io_unlockImpl },
    { "fflushImpl",         "(IZ)I",      (void*) harmony_io_fflushImpl },
    { "seekImpl",           "(IJI)J",     (void*) harmony_io_seekImpl },
    { "readDirectImpl",     "(IIII)J",    (void*) harmony_io_readDirectImpl },
    { "writeDirectImpl",    "(IIII)J",    (void*) harmony_io_writeDirectImpl },
    { "readImpl",           "(I[BII)J",   (void*) harmony_io_readImpl },
    { "writeImpl",          "(I[BII)J",   (void*) harmony_io_writeImpl },
    { "readvImpl",          "(I[I[I[II)J",(void*) harmony_io_readvImpl },
    { "writevImpl",         "(I[I[I[II)J",(void*) harmony_io_writevImpl },
    { "closeImpl",          "(I)I",       (void*) harmony_io_closeImpl },
    { "truncateImpl",       "(IJ)I",      (void*) harmony_io_truncateImpl },
    { "openImpl",           "([BI)I",     (void*) harmony_io_openImpl },
    { "transferImpl",       "(ILjava/io/FileDescriptor;JJ)J", 
    	    (void*) harmony_io_transferImpl },
    // BEGIN android-deleted
    //{ "ttyAvailableImpl",   "()J",        (void*) harmony_io_ttyAvailableImpl },
    // END android-deleted
    // BEGIN android-added
    { "ioctlAvailable",     "(I)I",       (void*) harmony_io_ioctlAvailable },
    // END android added
    { "ttyReadImpl",        "([BII)J",    (void*) harmony_io_ttyReadImpl }
};
int register_org_apache_harmony_luni_platform_OSFileSystem(JNIEnv *_env) {
	return jniRegisterNativeMethods(_env, 
	        "org/apache/harmony/luni/platform/OSFileSystem", gMethods, 
	        NELEM(gMethods));
}

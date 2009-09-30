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

// BEGIN android-note
// This file corresponds to harmony's OSFileSystem.c and OSFileSystemLinux32.c.
// It has been greatly simplified by the assumption that the underlying
// platform is always Linux.
// END android-note

/*
 * Common natives supporting the file system interface.
 */

#define HyMaxPath 1024

/* Values for HyFileOpen */
#define HyOpenRead    1
#define HyOpenWrite   2
#define HyOpenCreate  4
#define HyOpenTruncate  8
#define HyOpenAppend  16
#define HyOpenText    32
/* Use this flag with HyOpenCreate, if this flag is specified then
 * trying to create an existing file will fail
 */
#define HyOpenCreateNew 64
#define HyOpenSync      128
#define SHARED_LOCK_TYPE 1L

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/sendfile.h>
#include <sys/uio.h>

// An equivalent of the glibc macro of the same name.
// We want to hide EINTR from Java by simply retrying directly in
// the native code. We care about all other errors, though.
#define EINTR_RETRY(exp) ({                   \
    typeof (exp) _rc;                         \
    do {                                      \
        _rc = (exp);                          \
    } while (_rc == -1 && errno == EINTR);    \
    _rc; })

static void convertToPlatform(char *path) {
    char *pathIndex;

    pathIndex = path;
    while (*pathIndex != '\0') {
        if (*pathIndex == '\\') {
            *pathIndex = '/';
        }
        pathIndex++;
    }
}

static int EsTranslateOpenFlags(int flags) {
    int realFlags = 0;

    if (flags & HyOpenAppend) {
        realFlags |= O_APPEND;
    }
    if (flags & HyOpenTruncate) {
        realFlags |= O_TRUNC;
    }
    if (flags & HyOpenCreate) {
        realFlags |= O_CREAT;
    }
    if (flags & HyOpenCreateNew) {
        realFlags |= O_EXCL | O_CREAT;
    }
#ifdef O_SYNC
    if (flags & HyOpenSync) {
        realFlags |= O_SYNC;
    }
#endif
    if (flags & HyOpenRead) {
        if (flags & HyOpenWrite) {
            return (O_RDWR | realFlags);
        }
        return (O_RDONLY | realFlags);
    }
    if (flags & HyOpenWrite) {
        return (O_WRONLY | realFlags);
    }
    return -1;
}

// Checks whether we can safely treat the given jlong as an off_t without
// accidental loss of precision.
// TODO: this is bogus; we should use _FILE_OFFSET_BITS=64.
static bool offsetTooLarge(JNIEnv* env, jlong longOffset) {
    if (sizeof(off_t) >= sizeof(jlong)) {
        // We're only concerned about the possibility that off_t is
        // smaller than jlong. off_t is signed, so we don't need to
        // worry about signed/unsigned.
        return false;
    }

    // TODO: use std::numeric_limits<off_t>::max() and min() when we have them.
    assert(sizeof(off_t) == sizeof(int));
    static const off_t off_t_max = INT_MAX;
    static const off_t off_t_min = INT_MIN;

    if (longOffset > off_t_max || longOffset < off_t_min) {
        // "Value too large for defined data type".
        jniThrowIOException(env, EOVERFLOW);
        return true;
    }
    return false;
}

static jlong translateLockLength(jlong length) {
    // FileChannel.tryLock uses Long.MAX_VALUE to mean "lock the whole
    // file", where POSIX would use 0. We can support that special case,
    // even for files whose actual length we can't represent. For other
    // out of range lengths, though, we want our range checking to fire.
    return (length == 0x7fffffffffffffffLL) ? 0 : length;
}

static struct flock flockFromStartAndLength(jlong start, jlong length) {
    struct flock lock;
    memset(&lock, 0, sizeof(lock));

    lock.l_whence = SEEK_SET;
    lock.l_start = start;
    lock.l_len = length;

    return lock;
}

static jint harmony_io_lockImpl(JNIEnv* env, jobject, jint handle,
        jlong start, jlong length, jint typeFlag, jboolean waitFlag) {

    length = translateLockLength(length);
    if (offsetTooLarge(env, start) || offsetTooLarge(env, length)) {
        return -1;
    }

    struct flock lock(flockFromStartAndLength(start, length));

    if ((typeFlag & SHARED_LOCK_TYPE) == SHARED_LOCK_TYPE) {
        lock.l_type = F_RDLCK;
    } else {
        lock.l_type = F_WRLCK;
    }

    int waitMode = (waitFlag) ? F_SETLKW : F_SETLK;
    return EINTR_RETRY(fcntl(handle, waitMode, &lock));
}

static void harmony_io_unlockImpl(JNIEnv* env, jobject, jint handle,
        jlong start, jlong length) {

    length = translateLockLength(length);
    if (offsetTooLarge(env, start) || offsetTooLarge(env, length)) {
        return;
    }

    struct flock lock(flockFromStartAndLength(start, length));
    lock.l_type = F_UNLCK;

    int rc = EINTR_RETRY(fcntl(handle, F_SETLKW, &lock));
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
}

/**
 * Returns the granularity of the starting address for virtual memory allocation.
 * (It's the same as the page size.)
 */
static jint harmony_io_getAllocGranularity(JNIEnv* env, jobject) {
    static int allocGranularity = getpagesize();
    return allocGranularity;
}

static jlong harmony_io_readv(JNIEnv* env, jobject, jint fd,
        jintArray jBuffers, jintArray jOffsets, jintArray jLengths, jint size) {
    iovec* vectors = new iovec[size];
    if (vectors == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError", "native heap");
        return -1;
    }
    jint *buffers = env->GetIntArrayElements(jBuffers, NULL);
    jint *offsets = env->GetIntArrayElements(jOffsets, NULL);
    jint *lengths = env->GetIntArrayElements(jLengths, NULL);
    for (int i = 0; i < size; ++i) {
        vectors[i].iov_base = (void *)((int)(buffers[i]+offsets[i]));
        vectors[i].iov_len = lengths[i];
    }
    long result = readv(fd, vectors, size);
    env->ReleaseIntArrayElements(jBuffers, buffers, JNI_ABORT);
    env->ReleaseIntArrayElements(jOffsets, offsets, JNI_ABORT);
    env->ReleaseIntArrayElements(jLengths, lengths, JNI_ABORT);
    delete[] vectors;
    if (result == -1) {
        jniThrowIOException(env, errno);
    }
    return result;
}

static jlong harmony_io_writev(JNIEnv* env, jobject, jint fd,
        jintArray jBuffers, jintArray jOffsets, jintArray jLengths, jint size) {
    iovec* vectors = new iovec[size];
    if (vectors == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError", "native heap");
        return -1;
    }
    jint *buffers = env->GetIntArrayElements(jBuffers, NULL);
    jint *offsets = env->GetIntArrayElements(jOffsets, NULL);
    jint *lengths = env->GetIntArrayElements(jLengths, NULL);
    for (int i = 0; i < size; ++i) {
        vectors[i].iov_base = (void *)((int)(buffers[i]+offsets[i]));
        vectors[i].iov_len = lengths[i];
    }
    long result = writev(fd, vectors, size);
    env->ReleaseIntArrayElements(jBuffers, buffers, JNI_ABORT);
    env->ReleaseIntArrayElements(jOffsets, offsets, JNI_ABORT);
    env->ReleaseIntArrayElements(jLengths, lengths, JNI_ABORT);
    delete[] vectors;
    if (result == -1) {
        jniThrowIOException(env, errno);
    }
    return result;
}

static jlong harmony_io_transfer(JNIEnv* env, jobject, jint fd, jobject sd,
        jlong offset, jlong count) {

    int socket = jniGetFDFromFileDescriptor(env, sd);
    if (socket == -1) {
        return -1;
    }

    /* Value of offset is checked in jint scope (checked in java layer)
       The conversion here is to guarantee no value lost when converting offset to off_t
     */
    off_t off = offset;

    ssize_t rc = sendfile(socket, fd, &off, count);
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
    return rc;
}

static jlong harmony_io_readDirect(JNIEnv* env, jobject, jint fd,
        jint buf, jint offset, jint nbytes) {
    if (nbytes == 0) {
        return 0;
    }

    jbyte* dst = reinterpret_cast<jbyte*>(buf + offset);
    jlong rc = EINTR_RETRY(read(fd, dst, nbytes));
    if (rc == 0) {
        return -1;
    }
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
    return rc;
}

static jlong harmony_io_writeDirect(JNIEnv* env, jobject, jint fd,
        jint buf, jint offset, jint nbytes) {
    jbyte* src = reinterpret_cast<jbyte*>(buf + offset);
    jlong rc = EINTR_RETRY(write(fd, src, nbytes));
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
    return rc;
}

static jlong harmony_io_readImpl(JNIEnv* env, jobject, jint fd,
        jbyteArray byteArray, jint offset, jint nbytes) {

    if (nbytes == 0) {
        return 0;
    }

    jbyte* bytes = env->GetByteArrayElements(byteArray, NULL);
    jlong rc = EINTR_RETRY(read(fd, bytes + offset, nbytes));
    env->ReleaseByteArrayElements(byteArray, bytes, 0);

    if (rc == 0) {
        return -1;
    }
    if (rc == -1) {
        if (errno == EAGAIN) {
            jniThrowException(env, "java/io/InterruptedIOException",
                    "Read timed out");
        } else {
            jniThrowIOException(env, errno);
        }
    }
    return rc;
}

static jlong harmony_io_writeImpl(JNIEnv* env, jobject, jint fd,
        jbyteArray byteArray, jint offset, jint nbytes) {

    jbyte* bytes = env->GetByteArrayElements(byteArray, NULL);
    jlong result = EINTR_RETRY(write(fd, bytes + offset, nbytes));
    env->ReleaseByteArrayElements(byteArray, bytes, JNI_ABORT);

    if (result == -1) {
        if (errno == EAGAIN) {
            jniThrowException(env, "java/io/InterruptedIOException",
                    "Write timed out");
        } else {
            jniThrowIOException(env, errno);
        }
    }
    return result;
}

static jlong harmony_io_seek(JNIEnv* env, jobject, jint fd, jlong offset,
        jint javaWhence) {
    /* Convert whence argument */
    int nativeWhence = 0;
    switch (javaWhence) {
    case 1:
        nativeWhence = SEEK_SET;
        break;
    case 2:
        nativeWhence = SEEK_CUR;
        break;
    case 4:
        nativeWhence = SEEK_END;
        break;
    default:
        return -1;
    }

    // If the offset is relative, lseek(2) will tell us whether it's too large.
    // We're just worried about too large an absolute offset, which would cause
    // us to lie to lseek(2).
    if (offsetTooLarge(env, offset)) {
        return -1;
    }

    jlong result = lseek(fd, offset, nativeWhence);
    if (result == -1) {
        jniThrowIOException(env, errno);
    }
    return result;
}

// TODO: are we supposed to support the 'metadata' flag? (false => fdatasync.)
static void harmony_io_fflush(JNIEnv* env, jobject, jint fd,
        jboolean metadata) {
    int rc = fsync(fd);
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
}

static jint harmony_io_close(JNIEnv* env, jobject, jint fd) {
    jint rc = EINTR_RETRY(close(fd));
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
    return rc;
}

static jint harmony_io_truncate(JNIEnv* env, jobject, jint fd, jlong length) {
    if (offsetTooLarge(env, length)) {
        return -1;
    }

    int rc = ftruncate(fd, length);
    if (rc == -1) {
        jniThrowIOException(env, errno);
    }
    return rc;
}

static jint harmony_io_openImpl(JNIEnv* env, jobject, jbyteArray path,
        jint jflags) {
    int flags = 0;
    int mode = 0;

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

    // TODO: clean this up when we clean up the java.io.File equivalent.
    jsize length = env->GetArrayLength (path);
    length = length < HyMaxPath - 1 ? length : HyMaxPath - 1;
    char pathCopy[HyMaxPath];
    env->GetByteArrayRegion (path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform (pathCopy);

    jint cc = EINTR_RETRY(open(pathCopy, flags, mode));
    // TODO: chase up the callers of this and check they wouldn't rather
    // have us throw a meaningful IOException right here.
    if (cc < 0 && errno > 0) {
        cc = -errno;
    }
    return cc;
}

static jint harmony_io_ioctlAvailable(JNIEnv*env, jobject, jint fd) {
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
    int avail = 0;
    int rc = ioctl(fd, FIONREAD, &avail);
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
        jniThrowIOException(env, errno);
    }

    return (jint) avail;
}

static jlong harmony_io_ttyReadImpl(JNIEnv* env, jobject thiz,
        jbyteArray byteArray, jint offset, jint nbytes) {
    return harmony_io_readImpl(env, thiz, STDIN_FILENO, byteArray, offset, nbytes);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "close",              "(I)V",       (void*) harmony_io_close },
    { "fflush",             "(IZ)V",      (void*) harmony_io_fflush },
    { "getAllocGranularity","()I",        (void*) harmony_io_getAllocGranularity },
    { "ioctlAvailable",     "(I)I",       (void*) harmony_io_ioctlAvailable },
    { "lockImpl",           "(IJJIZ)I",   (void*) harmony_io_lockImpl },
    { "openImpl",           "([BI)I",     (void*) harmony_io_openImpl },
    { "readDirect",         "(IIII)J",    (void*) harmony_io_readDirect },
    { "readImpl",           "(I[BII)J",   (void*) harmony_io_readImpl },
    { "readv",              "(I[I[I[II)J",(void*) harmony_io_readv },
    { "seek",               "(IJI)J",     (void*) harmony_io_seek },
    { "transfer",           "(ILjava/io/FileDescriptor;JJ)J",
                                          (void*) harmony_io_transfer },
    { "truncate",           "(IJ)V",      (void*) harmony_io_truncate },
    { "ttyReadImpl",        "([BII)J",    (void*) harmony_io_ttyReadImpl },
    { "unlockImpl",         "(IJJ)V",     (void*) harmony_io_unlockImpl },
    { "writeDirect",        "(IIII)J",    (void*) harmony_io_writeDirect },
    { "writeImpl",          "(I[BII)J",   (void*) harmony_io_writeImpl },
    { "writev",             "(I[I[I[II)J",(void*) harmony_io_writev },
};
int register_org_apache_harmony_luni_platform_OSFileSystem(JNIEnv* _env) {
    return jniRegisterNativeMethods(_env,
            "org/apache/harmony/luni/platform/OSFileSystem", gMethods,
            NELEM(gMethods));
}

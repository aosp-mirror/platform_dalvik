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
#include "LocalArray.h"
#include "ScopedByteArray.h"
#include "ScopedFd.h"

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/vfs.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>
#include <utime.h>

// BEGIN android-note: this file has been extensively rewritten to
// remove fixed-length buffers, buffer overruns, duplication, and
// poor choices of where to divide the work between Java and native
// code.

static jbyteArray java_io_File_getCanonImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    // The only thing this native code currently does is truncate the byte[] at
    // the first NUL.
    // TODO: this is completely pointless. we should do this in Java, or do all of getCanonicalPath in native code. (realpath(2)?)
    size_t length = strlen(&path[0]);
    jbyteArray result = env->NewByteArray(length);
    env->SetByteArrayRegion(result, 0, length, path.bytes());
    return result;
}

static jboolean java_io_File_deleteImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    return (remove(&path[0]) == 0);
}

static bool doStat(JNIEnv* env, jbyteArray pathBytes, struct stat& sb) {
    ScopedByteArray path(env, pathBytes);
    return (stat(&path[0], &sb) == 0);
}

static jlong java_io_File_lengthImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct stat sb;
    if (!doStat(env, pathBytes, sb)) {
        // We must return 0 for files that don't exist.
        // TODO: shouldn't we throw an IOException for ELOOP or EACCES?
        return 0;
    }
    
    /*
     * This android-changed code explicitly treats non-regular files (e.g.,
     * sockets and block-special devices) as having size zero. Some synthetic
     * "regular" files may report an arbitrary non-zero size, but
     * in these cases they generally report a block count of zero.
     * So, use a zero block count to trump any other concept of
     * size.
     * 
     * TODO: why do we do this?
     */
    if (!S_ISREG(sb.st_mode) || sb.st_blocks == 0) {
        return 0;
    }
    return sb.st_size;
}

static jlong java_io_File_lastModifiedImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct stat sb;
    if (!doStat(env, pathBytes, sb)) {
        return 0;
    }
    return static_cast<jlong>(sb.st_mtime) * 1000L;
}

static jboolean java_io_File_isDirectoryImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct stat sb;
    return (doStat(env, pathBytes, sb) && S_ISDIR(sb.st_mode));
}

static jboolean java_io_File_isFileImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct stat sb;
    return (doStat(env, pathBytes, sb) && S_ISREG(sb.st_mode));
}

static jboolean java_io_File_existsImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    return (access(&path[0], F_OK) == 0);
}

static jboolean java_io_File_canExecuteImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    return (access(&path[0], X_OK) == 0);
}

static jboolean java_io_File_canReadImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    return (access(&path[0], R_OK) == 0);
}

static jboolean java_io_File_canWriteImpl(JNIEnv* env, jobject recv, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    return (access(&path[0], W_OK) == 0);
}

static jbyteArray java_io_File_getLinkImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);

    // We can't know how big a buffer readlink(2) will need, so we need to
    // loop until it says "that fit".
    size_t bufSize = 512;
    while (true) {
        LocalArray<512> buf(bufSize);
        ssize_t len = readlink(&path[0], &buf[0], buf.size() - 1);
        if (len == -1) {
            // An error occurred.
            return pathBytes;
        }
        if (static_cast<size_t>(len) < buf.size() - 1) {
            // The buffer was big enough.
            // TODO: why do we bother with the NUL termination? (if you change this, remove the "- 1"s above.)
            buf[len] = '\0'; // readlink(2) doesn't NUL-terminate.
            jbyteArray result = env->NewByteArray(len);
            const jbyte* src = reinterpret_cast<const jbyte*>(&buf[0]);
            env->SetByteArrayRegion(result, 0, len, src);
            return result;
        }
        // Try again with a bigger buffer.
        bufSize *= 2;
    }
}

static jboolean java_io_File_setLastModifiedImpl(JNIEnv* env, jobject, jbyteArray pathBytes, jlong ms) {
    ScopedByteArray path(env, pathBytes);
    
    // We want to preserve the access time.
    struct stat sb;
    if (stat(&path[0], &sb) == -1) {
        return JNI_FALSE;
    }
    
    // TODO: we could get microsecond resolution with utimes(3), "legacy" though it is.
    utimbuf times;
    times.actime = sb.st_atime;
    times.modtime = static_cast<time_t>(ms / 1000);
    return (utime(&path[0], &times) == 0);
}

static jboolean doChmod(JNIEnv* env, jbyteArray pathBytes, mode_t mask, bool set) {
    ScopedByteArray path(env, pathBytes);
    struct stat sb;
    if (stat(&path[0], &sb) == -1) {
        return JNI_FALSE;
    }
    mode_t newMode = set ? (sb.st_mode | mask) : (sb.st_mode & ~mask);
    return (chmod(&path[0], newMode) == 0);
}

static jboolean java_io_File_setExecutableImpl(JNIEnv* env, jobject, jbyteArray pathBytes,
        jboolean set, jboolean ownerOnly) {
    return doChmod(env, pathBytes, ownerOnly ? S_IXUSR : (S_IXUSR | S_IXGRP | S_IXOTH), set);
}

static jboolean java_io_File_setReadableImpl(JNIEnv* env, jobject, jbyteArray pathBytes,
        jboolean set, jboolean ownerOnly) {
    return doChmod(env, pathBytes, ownerOnly ? S_IRUSR : (S_IRUSR | S_IRGRP | S_IROTH), set);
}

static jboolean java_io_File_setWritableImpl(JNIEnv* env, jobject, jbyteArray pathBytes,
        jboolean set, jboolean ownerOnly) {
    return doChmod(env, pathBytes, ownerOnly ? S_IWUSR : (S_IWUSR | S_IWGRP | S_IWOTH), set);
}

static bool doStatFs(JNIEnv* env, jbyteArray pathBytes, struct statfs& sb) {
    ScopedByteArray path(env, pathBytes);
    int rc = statfs(&path[0], &sb);
    return (rc != -1);
}

static jlong java_io_File_getFreeSpace(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct statfs sb;
    if (!doStatFs(env, pathBytes, sb)) {
        return 0;
    }
    return sb.f_bfree * sb.f_bsize; // free block count * block size in bytes.
}

static jlong java_io_File_getTotalSpace(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct statfs sb;
    if (!doStatFs(env, pathBytes, sb)) {
        return 0;
    }
    return sb.f_blocks * sb.f_bsize; // total block count * block size in bytes.
}

static jlong java_io_File_getUsableSpace(JNIEnv* env, jobject, jbyteArray pathBytes) {
    struct statfs sb;
    if (!doStatFs(env, pathBytes, sb)) {
        return 0;
    }
    return sb.f_bavail * sb.f_bsize; // non-root free block count * block size in bytes.
}

// Iterates over the filenames in the given directory.
class ScopedReaddir {
public:
    ScopedReaddir(const char* path) {
        mDirStream = opendir(path);
        mIsBad = (mDirStream == NULL);
    }

    ~ScopedReaddir() {
        if (mDirStream != NULL) {
            closedir(mDirStream);
        }
    }

    // Returns the next filename, or NULL.
    const char* next() {
        dirent* result = NULL;
        int rc = readdir_r(mDirStream, &mEntry, &result);
        if (rc != 0) {
            mIsBad = true;
            return NULL;
        }
        return (result != NULL) ? result->d_name : NULL;
    }

    // Has an error occurred on this stream?
    bool isBad() const {
        return mIsBad;
    }

private:
    DIR* mDirStream;
    dirent mEntry;
    bool mIsBad;

    // Disallow copy and assignment.
    ScopedReaddir(const ScopedReaddir&);
    void operator=(const ScopedReaddir&);
};

// DirEntry and DirEntries is a minimal equivalent of std::forward_list
// for the filenames.
struct DirEntry {
    DirEntry(const char* filename) : name(strlen(filename)) {
        strcpy(&name[0], filename);
        next = NULL;
    }
    // On Linux, the ext family all limit the length of a directory entry to
    // less than 256 characters.
    LocalArray<256> name;
    DirEntry* next;
};

class DirEntries {
public:
    DirEntries() : mSize(0), mHead(NULL) {
    }

    ~DirEntries() {
        while (mHead) {
            pop_front();
        }
    }

    bool push_front(const char* name) {
        DirEntry* oldHead = mHead;
        mHead = new DirEntry(name);
        if (mHead == NULL) {
            return false;
        }
        mHead->next = oldHead;
        ++mSize;
        return true;
    }

    const char* front() const {
        return &mHead->name[0];
    }

    void pop_front() {
        DirEntry* popped = mHead;
        if (popped != NULL) {
            mHead = popped->next;
            --mSize;
            delete popped;
        }
    }

    size_t size() const {
        return mSize;
    }

private:
    size_t mSize;
    DirEntry* mHead;

    // Disallow copy and assignment.
    DirEntries(const DirEntries&);
    void operator=(const DirEntries&);
};

// Reads the directory referred to by 'pathBytes', adding each directory entry
// to 'entries'.
static bool readDirectory(JNIEnv* env, jbyteArray pathBytes, DirEntries& entries) {
    ScopedByteArray path(env, pathBytes);
    ScopedReaddir dir(&path[0]);
    if (dir.isBad()) {
        return false;
    }
    const char* filename;
    while ((filename = dir.next()) != NULL) {
        if (strcmp(filename, ".") != 0 && strcmp(filename, "..") != 0) {
            if (!entries.push_front(filename)) {
                jniThrowException(env, "java/lang/OutOfMemoryError", NULL);
                return false;
            }
        }
    }
    return true;
}

static jobjectArray java_io_File_listImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    // Read the directory entries into an intermediate form.
    DirEntries files;
    if (!readDirectory(env, pathBytes, files)) {
        return NULL;
    }
    // Translate the intermediate form into a Java String[].
    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == NULL) {
        return NULL;
    }
    jobjectArray result = env->NewObjectArray(files.size(), stringClass, NULL);
    for (int i = 0; files.size() != 0; files.pop_front(), ++i) {
        jstring javaFilename = env->NewStringUTF(files.front());
        if (env->ExceptionCheck()) {
            return NULL;
        }
        env->SetObjectArrayElement(result, i, javaFilename);
        if (env->ExceptionCheck()) {
            return NULL;
        }
        env->DeleteLocalRef(javaFilename);
    }
    return result;
}

static jboolean java_io_File_mkdirImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    // On Android, we don't want default permissions to allow global access.
    return (mkdir(&path[0], S_IRWXU) == 0);
}

static jboolean java_io_File_createNewFileImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    // On Android, we don't want default permissions to allow global access.
    ScopedFd fd(open(&path[0], O_CREAT | O_EXCL, 0600));
    if (fd.get() != -1) {
        // We created a new file. Success!
        return JNI_TRUE;
    }
    if (errno == EEXIST) {
        // The file already exists.
        return JNI_FALSE;
    }
    jniThrowIOException(env, errno);
    return JNI_FALSE; // Ignored by Java; keeps the C++ compiler happy.
}

static jboolean java_io_File_renameToImpl(JNIEnv* env, jobject, jbyteArray oldPathBytes, jbyteArray newPathBytes) {
    ScopedByteArray oldPath(env, oldPathBytes);
    ScopedByteArray newPath(env, newPathBytes);
    return (rename(&oldPath[0], &newPath[0]) == 0);
}

static JNINativeMethod gMethods[] = {
    { "canExecuteImpl",     "([B)Z", (void*) java_io_File_canExecuteImpl },
    { "canReadImpl",        "([B)Z", (void*) java_io_File_canReadImpl },
    { "canWriteImpl",       "([B)Z", (void*) java_io_File_canWriteImpl },
    { "createNewFileImpl",  "([B)Z", (void*) java_io_File_createNewFileImpl },
    { "deleteImpl",         "([B)Z", (void*) java_io_File_deleteImpl },
    { "existsImpl",         "([B)Z", (void*) java_io_File_existsImpl },
    { "getCanonImpl",       "([B)[B", (void*) java_io_File_getCanonImpl },
    { "getFreeSpaceImpl",   "([B)J", (void*) java_io_File_getFreeSpace },
    { "getLinkImpl",        "([B)[B", (void*) java_io_File_getLinkImpl },
    { "getTotalSpaceImpl",  "([B)J", (void*) java_io_File_getTotalSpace },
    { "getUsableSpaceImpl", "([B)J", (void*) java_io_File_getUsableSpace },
    { "isDirectoryImpl",    "([B)Z", (void*) java_io_File_isDirectoryImpl },
    { "isFileImpl",         "([B)Z", (void*) java_io_File_isFileImpl },
    { "lastModifiedImpl",   "([B)J", (void*) java_io_File_lastModifiedImpl },
    { "lengthImpl",         "([B)J", (void*) java_io_File_lengthImpl },
    { "listImpl",           "([B)[Ljava/lang/String;", (void*) java_io_File_listImpl },
    { "mkdirImpl",          "([B)Z", (void*) java_io_File_mkdirImpl },
    { "renameToImpl",       "([B[B)Z", (void*) java_io_File_renameToImpl },
    { "setExecutableImpl",  "([BZZ)Z", (void*) java_io_File_setExecutableImpl },
    { "setReadableImpl",    "([BZZ)Z", (void*) java_io_File_setReadableImpl },
    { "setWritableImpl",    "([BZZ)Z", (void*) java_io_File_setWritableImpl },
    { "setLastModifiedImpl","([BJ)Z", (void*) java_io_File_setLastModifiedImpl },
};
int register_java_io_File(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/io/File", gMethods, NELEM(gMethods));
}

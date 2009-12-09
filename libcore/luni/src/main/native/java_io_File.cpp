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

#include "AndroidSystemNatives.h"
#include "JNIHelp.h"
#include "LocalArray.h"
#include "ScopedByteArray.h"
#include "ScopedFd.h"

#include <string.h>
#include <fcntl.h>
#include <time.h>
#include <utime.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <errno.h>

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

static jboolean java_io_File_isReadableImpl(JNIEnv* env, jobject, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    return (access(&path[0], R_OK) == 0);
}

static jboolean java_io_File_isWritableImpl(JNIEnv* env, jobject recv, jbyteArray pathBytes) {
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
        if (len < buf.size() - 1) {
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

static jboolean java_io_File_setReadOnlyImpl(JNIEnv* env, jobject recv, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);

    struct stat sb;
    if (stat(&path[0], &sb) == -1) {
        return JNI_FALSE;
    }

    // Strictly, this is set-not-writable (i.e. we leave the execute
    // bits untouched), but that's deliberate.
    return (chmod(&path[0], sb.st_mode & ~0222) == 0);
}

struct ScopedReaddir {
    ScopedReaddir(DIR* dirp) : dirp(dirp) {
    }
    ~ScopedReaddir() {
        if (dirp != NULL) {
            closedir(dirp);
        }
    }
    dirent* next() {
        return readdir(dirp);
    }
    DIR* dirp;
};

// TODO: this is a literal translation of the old code. we should remove the fixed-size buffers here.
#define MaxPath 1024

// TODO: Java doesn't guarantee any specific ordering, and with some file systems you will get results in non-alphabetical order, so I've just done the most convenient thing for the native code, but I wonder if we shouldn't pass down an ArrayList<String> and fill it?
struct LinkedDirEntry {
    static void addFirst(LinkedDirEntry** list, LinkedDirEntry* newEntry) {
        newEntry->next = *list;
        *list = newEntry;
    }

    LinkedDirEntry() : next(NULL) {
    }

    ~LinkedDirEntry() {
        delete next;
    }

    char pathEntry[MaxPath];
    LinkedDirEntry* next;
};

static jobject java_io_File_listImpl(JNIEnv* env, jclass clazz, jbyteArray pathBytes) {
    ScopedByteArray path(env, pathBytes);
    
    ScopedReaddir dir(opendir(&path[0]));
    if (dir.dirp == NULL) {
        // TODO: shouldn't we throw an IOException?
        return NULL;
    }
    
    // TODO: merge this into the loop below.
    dirent* entry = dir.next();
    if (entry == NULL) {
        return NULL;
    }
    char filename[MaxPath];
    strcpy(filename, entry->d_name);

    size_t fileCount = 0;
    LinkedDirEntry* files = NULL;
    while (entry != NULL) {
        if (strcmp(".", filename) != 0 && strcmp("..", filename) != 0) {
            LinkedDirEntry* newEntry = new LinkedDirEntry;
            if (newEntry == NULL) {
                jniThrowException(env, "java/lang/OutOfMemoryError", NULL);
                return NULL;
            }
            strcpy(newEntry->pathEntry, filename);
            
            LinkedDirEntry::addFirst(&files, newEntry);
            ++fileCount;
        }

        entry = dir.next();
        if (entry != NULL) {
            strcpy(filename, entry->d_name);
        }
    }
    
    // TODO: we should kill the ScopedReaddir about here, since we no longer need it.
    
    // TODO: we're supposed to use null to signal errors. we should return "new String[0]" here (or an empty byte[][]).
    if (fileCount == 0) {
        return NULL;
    }
    
    // Create a byte[][].
    // TODO: since the callers all want a String[], why do we return a byte[][]?
    jclass byteArrayClass = env->FindClass("[B");
    if (byteArrayClass == NULL) {
        return NULL;
    }
    jobjectArray answer = env->NewObjectArray(fileCount, byteArrayClass, NULL);
    int arrayIndex = 0;
    for (LinkedDirEntry* file = files; file != NULL; file = file->next) {
        jsize entrylen = strlen(file->pathEntry);
        jbyteArray entrypath = env->NewByteArray(entrylen);
        env->SetByteArrayRegion(entrypath, 0, entrylen, (jbyte *) file->pathEntry);
        env->SetObjectArrayElement(answer, arrayIndex, entrypath);
        env->DeleteLocalRef(entrypath);
        ++arrayIndex;
    }
    return answer;
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
    /* name, signature, funcPtr */
    { "createNewFileImpl",  "([B)Z",  (void*) java_io_File_createNewFileImpl },
    { "deleteImpl",         "([B)Z",  (void*) java_io_File_deleteImpl },
    { "existsImpl",         "([B)Z",  (void*) java_io_File_existsImpl },
    { "getCanonImpl",       "([B)[B", (void*) java_io_File_getCanonImpl },
    { "getLinkImpl",        "([B)[B", (void*) java_io_File_getLinkImpl },
    { "isDirectoryImpl",    "([B)Z",  (void*) java_io_File_isDirectoryImpl },
    { "isFileImpl",         "([B)Z",  (void*) java_io_File_isFileImpl },
    { "isReadableImpl",     "([B)Z",  (void*) java_io_File_isReadableImpl },
    { "isWritableImpl",     "([B)Z",  (void*) java_io_File_isWritableImpl },
    { "lastModifiedImpl",   "([B)J",  (void*) java_io_File_lastModifiedImpl },
    { "lengthImpl",         "([B)J",  (void*) java_io_File_lengthImpl },
    { "listImpl",           "([B)[[B",(void*) java_io_File_listImpl },
    { "mkdirImpl",          "([B)Z",  (void*) java_io_File_mkdirImpl },
    { "renameToImpl",       "([B[B)Z",(void*) java_io_File_renameToImpl },
    { "setLastModifiedImpl","([BJ)Z", (void*) java_io_File_setLastModifiedImpl },
    { "setReadOnlyImpl",    "([B)Z",  (void*) java_io_File_setReadOnlyImpl },
};
int register_java_io_File(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/io/File",
            gMethods, NELEM(gMethods));
}

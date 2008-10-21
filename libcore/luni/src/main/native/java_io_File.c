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

#define MaxPath 1024

#include "JNIHelp.h"

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
#include <assert.h>


/* these were copied from java.io.File */
enum {
    STAT_TYPE_EXISTS = 0x0001,
    STAT_TYPE_DIR = 0x0002,
    STAT_TYPE_FILE = 0x0004
};

static void convertToPlatform(char *path) {
    char *pathIndex;

    pathIndex = path;
    while(*pathIndex != '\0') {
        if(*pathIndex == '\\') {
            *pathIndex = '/';
        }
        pathIndex++;
    }
}

/*
 * private static native byte[][] rootsImpl()
 *
 * Returns the linux root in an array of byte arrays
 */
static jobject java_io_File_rootsImpl(JNIEnv* env, jclass clazz) {
    char rootStrings[3];
    jarray answer;

    rootStrings[0] = '/';
    rootStrings[1] = '\0';
    rootStrings[2] = '\0';

    jclass arrayClass = (*env)->FindClass(env, "[B");
    if (arrayClass == NULL)
        return NULL;

    answer = (*env)->NewObjectArray(env, 1, arrayClass, NULL);
    if (!answer)
        return NULL;

    jbyteArray rootname;

    rootname = (*env)->NewByteArray(env, 3);
    (*env)->SetByteArrayRegion(env, rootname, 0, 3, (jbyte *) rootStrings);
    (*env)->SetObjectArrayElement(env, answer, 0, rootname);
    //(*env)->DeleteLocalRef(env, rootname);

    return answer;
}

static jbyteArray java_io_File_getCanonImpl(JNIEnv * env, jobject recv, 
        jbyteArray path) {
    /* This needs work.  Currently it does no more or less than VAJ-20 ST 
     * implementationbut really should figure out '..', '.', and really 
     * resolve references.
     */
    jbyteArray answer;
    size_t answerlen;
    char *pathIndex;
    char pathCopy[MaxPath];
    jsize length = (jsize) (*env)->GetArrayLength(env, path);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';

    convertToPlatform(pathCopy);

    answerlen = strlen(pathCopy);
    answer = (*env)->NewByteArray(env, answerlen);
    (*env)->SetByteArrayRegion(env, answer, 0, answerlen, (jbyte *) pathCopy);

    return answer;
}

/*
 * native private boolean deleteFileImpl()
 *
 * Returns "true" if the file exists and was successfully deleted.
 */
static jboolean java_io_File_deleteFileImpl(JNIEnv* env, jobject obj, 
        jbyteArray path) {

    int cc;

    if(path == NULL) {
        return JNI_FALSE;       /* exception thrown */
    }

    char pathCopy[MaxPath];
    jsize length = (jsize) (*env)->GetArrayLength(env, path);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);
    
    cc = unlink(pathCopy);
    if(cc < 0) {
        int err = errno;
        LOGD(" unable to unlink '%s' (errno=%d)\n", pathCopy, err);

        /*
         * According to the man pages, Linux uses EISDIR and Mac OS X
         * uses EPERM to indicate a non-super-user attempt to unlink
         * a directory.  Mac OS does have EISDIR in the header file.
         *
         * We should get EACCES if the problem is directory permissions.
         */
        if(err == EISDIR || err == EPERM) {
            cc = rmdir(pathCopy);
            if(cc < 0) {
                /* probably ENOTEMPTY */
                LOGD("   unable to rmdir '%s' (errno=%d)\n",
                    pathCopy, errno);
            }
        }
    }

    return (cc == 0);
}

/*
 * harmony implements this method practically identical to the deleteFileImpl, 
 * except that it uses a diffrent helper method from hyport. Dalvik seems to 
 * just need this one method to delete both
 */
static jboolean java_io_File_deleteDirImpl(JNIEnv * env, jobject recv, 
        jbyteArray path) {
    return java_io_File_deleteFileImpl( env, recv, path);
}

/*
 * native public long lengthImpl()
 *
 * Returns the file length, or 0 if the file does not exist.  The result for
 * a directory is not defined.
 */
static jlong java_io_File_lengthImpl(JNIEnv* env, jobject obj, 
        jbyteArray path) {
    struct stat sb;
    jlong result = 0;
    int cc;

    char pathCopy[MaxPath];
    jsize length = (jsize) (*env)->GetArrayLength(env, path);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    cc = stat(pathCopy, &sb);
    if(cc == 0) {
        // BEGIN android-added
        /*
         * This explicitly treats non-regular files (e.g., sockets and
         * block-special devices) as having size zero. Some synthetic
         * "regular" files may report an arbitrary non-zero size, but
         * in these cases they generally report a block count of zero.
         * So, use a zero block count to trump any other concept of
         * size.
         */
        if (S_ISREG(sb.st_mode) && (sb.st_blocks != 0)) {
            result = sb.st_size;
        } else {
            result = 0;
        }
        // END android-added
        // BEGIN android-deleted
        //result = sb.st_size;
        // END android-deleted
    }

    return result;
}

/*
 * native public long lastModified()
 *
 * Get the last modified date of the file. Measured in milliseconds 
 * from epoch (00:00:00 GMT, January 1, 1970). Returns 0 if the file does
 * not exist
 */
static jlong java_io_File_lastModifiedImpl(JNIEnv* env, jobject obj, 
        jbyteArray path) {
    struct stat sb;
    jlong result = 0;
    int cc;

    char pathCopy[MaxPath];
    jsize length = (jsize) (*env)->GetArrayLength(env, path);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    cc = stat(pathCopy, &sb);
    if(cc == 0) {
        // sb.st_mtime is a time_t which is in seconds since epoch.
        result = sb.st_mtime;
        result *= 1000L;
    }

    return result;
}

/*
 * private static native int stattype(String path)
 */
static jint java_io_File_stattype(JNIEnv* env, jobject recv, 
        jbyteArray pathStr) {

    char pathCopy[MaxPath];
    jsize length = (jsize) (*env)->GetArrayLength(env, pathStr);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, pathStr, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    struct stat sb;
    int cc, type;

    type = 0;
    cc = stat(pathCopy, &sb);

    if(cc == 0) {
        type |= STAT_TYPE_EXISTS;
        if(S_ISDIR(sb.st_mode)) {
            type |= STAT_TYPE_DIR;
        } else if(S_ISREG(sb.st_mode)) {
            type |= STAT_TYPE_FILE;
        }
    }

    return type;
}

static jboolean java_io_File_isDirectoryImpl(JNIEnv* env, jobject recv, 
        jbyteArray pathStr) {
    return ((java_io_File_stattype(env, recv, pathStr) & STAT_TYPE_DIR) 
            == STAT_TYPE_DIR);
}

static jboolean java_io_File_existsImpl(JNIEnv* env, jobject recv, 
        jbyteArray pathStr) {
    return ((java_io_File_stattype(env, recv, pathStr) & STAT_TYPE_EXISTS) 
            == STAT_TYPE_EXISTS);
}

static jboolean java_io_File_isFileImpl(JNIEnv* env, jobject recv, 
        jbyteArray pathStr) {
    return ((java_io_File_stattype(env, recv, pathStr) & STAT_TYPE_FILE) 
            == STAT_TYPE_FILE);
}

static jboolean java_io_File_isHiddenImpl(JNIEnv * env, jobject recv, 
        jbyteArray path) {

    char pathCopy[MaxPath];
    jsize index;
    jsize length = (*env)->GetArrayLength(env, path);
    length = length < MaxPath - 1 ? length : MaxPath - 1;

    if(length == 0) {
        return 0;
    }

    ((*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy));
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    if(!java_io_File_existsImpl(env, recv, path)) {
        return 0;
    }

    for(index = length; index >= 0; index--) {
        if(pathCopy[index] == '.' 
                && (index > 0 && pathCopy[index - 1] == '/')) {
            return -1;
        }
    }

    return 0;
}

static jboolean java_io_File_readable(JNIEnv* env, jobject recv, 
        jbyteArray pathStr) {
    char path[MaxPath];
    struct stat sb;
    int cc, type;

    if(pathStr == NULL) {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
        return -1;
    }
    
    jsize length = (jsize) (*env)->GetArrayLength(env, pathStr);

    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, pathStr, 0, length, (jbyte *)path);
    path[length] = '\0';
    convertToPlatform(path);

    cc = access(path, R_OK);

    return cc == 0;
}

static jboolean java_io_File_writable(JNIEnv* env, jobject recv, 
        jbyteArray pathStr) {
    char path[MaxPath];
    struct stat sb;
    int cc, type;

    if(pathStr == NULL) {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
        return -1;
    }
    
    jsize length = (jsize) (*env)->GetArrayLength(env, pathStr);

    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, pathStr, 0, length, (jbyte *)path);
    path[length] = '\0';
    convertToPlatform(path);
    
    cc = access(path, W_OK);

    return cc == 0;
}

static jboolean java_io_File_isReadOnlyImpl(JNIEnv* env, jobject recv, 
        jbyteArray path) {

    return (java_io_File_readable(env, recv, path) 
            && !java_io_File_writable(env, recv, path));
}

static jboolean java_io_File_isWriteOnlyImpl(JNIEnv* env, jobject recv, 
        jbyteArray path) {

    return (!java_io_File_readable(env, recv, path) 
            && java_io_File_writable(env, recv, path));
}

static jbyteArray java_io_File_getLinkImpl(JNIEnv* env, jobject recv, 
        jbyteArray path) {
    jbyteArray answer;
    jsize answerlen;
    char pathCopy[MaxPath];

    jsize length = (jsize) (*env)->GetArrayLength(env, path);

    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    jboolean test = -1;

    char *link = pathCopy;

    int size = readlink(link, link, MaxPath);
    if(size <= 0) {
        test = 0;
    } else {
        if(size >= MaxPath) {
            link[MaxPath - 1] = 0;
        } else {
            link[size] = 0;
        }
    }

    if(test) {
        answerlen = strlen(pathCopy);
        answer = (*env)->NewByteArray(env, answerlen);
        (*env)->SetByteArrayRegion(env, answer, 0, answerlen,
                                  (jbyte *) pathCopy);
    } else {
        answer = path;
    }

    return answer;
}

static jboolean java_io_File_setLastModifiedImpl(JNIEnv* env, jobject recv, 
        jbyteArray path, jlong time) {
    jboolean result;
    
    jsize length = (*env)->GetArrayLength(env, path);
    char pathCopy[MaxPath];
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    ((*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy));
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    struct stat statbuf;
    struct utimbuf timebuf;
    if(stat(pathCopy, &statbuf)) {
        result = 0;
    } else {
        timebuf.actime = statbuf.st_atime;
        timebuf.modtime = (time_t) (time / 1000);
        result = utime(pathCopy, &timebuf) == 0;
    }

    return result;
}

static jboolean java_io_File_setReadOnlyImpl(JNIEnv* env, jobject recv, 
        jbyteArray path) {
    jsize length = (*env)->GetArrayLength(env, path);
    char pathCopy[MaxPath];
    length = length < MaxPath - 1 ? length : MaxPath - 1;

    ((*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy));

    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    struct stat buffer;
    mode_t mode;
    if(stat(pathCopy, &buffer)) {
        return 0;
    }
    mode = buffer.st_mode;
    mode = mode & 07555;

    return chmod(pathCopy, mode) == 0;
}

static jobject java_io_File_listImpl(JNIEnv* env, jclass clazz, 
        jbyteArray path) {
    
    struct dirEntry {
        char pathEntry[MaxPath];
        struct dirEntry *next;
    } *dirList, *currentEntry;

    jsize length = (*env)->GetArrayLength(env, path);
    char pathCopy[MaxPath];
    char filename[MaxPath];
    jint result = 0, index;
    jint numEntries = 0;
    jarray answer = NULL;
    jclass javaClass = NULL;

    dirList = NULL;
    currentEntry = NULL;

    length = length < MaxPath - 1 ? length : MaxPath - 1;
    ((*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy));
    if(length >= 1 && pathCopy[length - 1] != '\\' 
            && pathCopy[length - 1] != '/') {
        pathCopy[length] = '/';
        length++;
    }
    pathCopy[length] = '\0';
    
    convertToPlatform(pathCopy);

    DIR *dirp = NULL;
    struct dirent *entry;

    dirp = opendir(pathCopy);

    if(dirp == NULL) {
        return NULL;
    }

    entry = readdir(dirp);

    if(entry == NULL) {
        closedir(dirp);
        return NULL;
    }
    strcpy(filename, entry->d_name);

    while(result > -1) {
        if(strcmp(".", filename) != 0 && (strcmp("..", filename) != 0)) {
            if(numEntries > 0) {
                currentEntry->next = 
                        (struct dirEntry *) malloc(sizeof(struct dirEntry));
                currentEntry = currentEntry->next;
            } else {
                dirList = (struct dirEntry *) malloc(sizeof(struct dirEntry));
                currentEntry = dirList;
            }
            if(currentEntry == NULL) {
                closedir(dirp);
                jniThrowException(env, "java/lang/OutOfMemoryError", NULL);
                goto cleanup;
            }
            strcpy(currentEntry->pathEntry, filename);
            numEntries++;
        }

        entry = readdir(dirp);

        if(entry == NULL) {
            result = -1;
        } else {
            strcpy(filename, entry->d_name);
        }
    }
    closedir(dirp);

    if(numEntries == 0) {
        return NULL;
    }

    javaClass = (*env)->FindClass(env, "[B");
    if(javaClass == NULL) {
        return NULL;
    }
    answer = (*env)->NewObjectArray(env, numEntries, javaClass, NULL);

cleanup:
    for(index = 0; index < numEntries; index++)
    {
        jbyteArray entrypath;
        jsize entrylen = strlen(dirList->pathEntry);
        currentEntry = dirList;
        if(answer)
        {
            entrypath = (*env)->NewByteArray(env, entrylen);
            (*env)->SetByteArrayRegion(env, entrypath, 0, entrylen,
                                      (jbyte *) dirList->pathEntry);
            (*env)->SetObjectArrayElement(env, answer, index, entrypath);
            (*env)->DeleteLocalRef(env, entrypath);
        }
        dirList = dirList->next;
        free((void *)currentEntry);
    }

    return answer;
}

static jboolean java_io_File_mkdirImpl(JNIEnv* env, jobject recv, 
        jbyteArray path) {
    jint result;
    char pathCopy[MaxPath];
    jsize length = (*env)->GetArrayLength(env, path);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    ((*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy));
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

// BEGIN android-changed
// don't want default permissions to allow global access.
    result = mkdir(pathCopy, S_IRWXU);
// END android-changed

    if(-1 != result)
    {
      result = 0;
    }

    return result == 0;
}

static jint java_io_File_newFileImpl(JNIEnv* env, jobject recv, 
        jbyteArray path) {
    
    if(path == NULL) {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
        return -1;
    }
    
    jint result;
    jsize length = (*env)->GetArrayLength(env, path);
    char pathCopy[MaxPath];
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    (*env)->GetByteArrayRegion(env, path, 0, length, (jbyte *)pathCopy);
    pathCopy[length] = '\0';
    convertToPlatform(pathCopy);

    /* First check to see if file already exists */
    if(java_io_File_existsImpl(env, recv, path))
    {
        return 1;
    }

    /* Now create the file and close it */
// BEGIN android-changed
// don't want default permissions to allow global access.
    int fd = open(pathCopy, O_EXCL | O_CREAT, 0600);
// END android-changed
    if(fd == -1)
    {
        if(errno == EEXIST) {
            return 1;
        }
        return -1;
    }
    close(fd);

    return 0;
}

static jboolean java_io_File_renameToImpl(JNIEnv* env, jobject recv, 
        jbyteArray pathExist, jbyteArray pathNew) {
    jint result;
    jsize length;
    char pathExistCopy[MaxPath], pathNewCopy[MaxPath];

    length = (*env)->GetArrayLength(env, pathExist);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    ((*env)->GetByteArrayRegion(env, pathExist, 0, length, 
            (jbyte *)pathExistCopy));
    pathExistCopy[length] = '\0';

    length = (*env)->GetArrayLength(env, pathNew);
    length = length < MaxPath - 1 ? length : MaxPath - 1;
    ((*env)->GetByteArrayRegion(env, pathNew, 0, length, 
            (jbyte *)pathNewCopy));
    pathNewCopy[length] = '\0';

    convertToPlatform(pathExistCopy);
    convertToPlatform(pathNewCopy);

    result = rename(pathExistCopy, pathNewCopy);

    return result == 0;
}

static void java_io_File_oneTimeInitialization(JNIEnv * env, jclass clazz)
{
  // dummy to stay compatible to harmony
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "rootsImpl",          "()[[B",  (void*) java_io_File_rootsImpl },
    { "deleteDirImpl",      "([B)Z",  (void*) java_io_File_deleteDirImpl },
    { "deleteFileImpl",     "([B)Z",  (void*) java_io_File_deleteFileImpl },
    { "existsImpl",         "([B)Z",  (void*) java_io_File_existsImpl },
    { "getCanonImpl",       "([B)[B", (void*) java_io_File_getCanonImpl },
    { "isDirectoryImpl",    "([B)Z",  (void*) java_io_File_isDirectoryImpl },
    { "isFileImpl",         "([B)Z",  (void*) java_io_File_isFileImpl },
    { "isHiddenImpl",       "([B)Z",  (void*) java_io_File_isHiddenImpl },
    { "isReadOnlyImpl",     "([B)Z",  (void*) java_io_File_isReadOnlyImpl },
    { "isWriteOnlyImpl",    "([B)Z",  (void*) java_io_File_isWriteOnlyImpl },
    { "getLinkImpl",        "([B)[B", (void*) java_io_File_getLinkImpl },
    { "lastModifiedImpl",   "([B)J",  (void*) java_io_File_lastModifiedImpl },
    { "setReadOnlyImpl",    "([B)Z",  (void*) java_io_File_setReadOnlyImpl },
    { "lengthImpl",         "([B)J",  (void*) java_io_File_lengthImpl },
    { "listImpl",           "([B)[[B",(void*) java_io_File_listImpl },
    { "mkdirImpl",          "([B)Z",  (void*) java_io_File_mkdirImpl },
    { "newFileImpl",        "([B)I",  (void*) java_io_File_newFileImpl },
    { "renameToImpl",       "([B[B)Z",(void*) java_io_File_renameToImpl },
    { "setLastModifiedImpl","([BJ)Z",
        (void*) java_io_File_setLastModifiedImpl },
    { "oneTimeInitialization","()V", 
        (void*) java_io_File_oneTimeInitialization }
};
int register_java_io_File(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/io/File",
                gMethods, NELEM(gMethods));
}

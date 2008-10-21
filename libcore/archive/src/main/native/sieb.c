#include "sieb.h"
#include "JNIHelp.h"
#include "jni.h"

#include <malloc.h>

/*
// Throw java.util.zip.DataFormatException
void throwNewDataFormatException (JNIEnv * env, const char *message)
{
  jniThrowException(env, "java/util/zip/DataFormatException", message);
}
*/

/* mc: Already defined in dalvik/libcore/luni/src/main/native/exceptions.c
void throwNewOutOfMemoryError (JNIEnv * env, const char *message)
{
// Throw java.util.zip.OutOfMemoryError
//  jniThrowException(env, "java/util/zip/OutOfMemoryError", message);
    jniThrowException(env, "java/lang/OutOfMemoryError", message);
}
*/

// Throw java.lang.IllegalStateException
void throwNewIllegalStateException (JNIEnv * env, const char *message)
{
  jniThrowException(env, "java/lang/IllegalStateException", message);
}

// Throw java.lang.IllegalArgumentException
void throwNewIllegalArgumentException (JNIEnv * env, const char *message)
{
  jniThrowException(env, "java/lang/IllegalArgumentException", message);
}



void * sieb_malloc (JNIEnv * env, size_t byteCnt) {
    void * adr = malloc(byteCnt);
    if (adr == 0) {
         if (byteCnt == 0)
             throwNewOutOfMemoryError(env, "sieb_malloc(0) NOT ALLOWED");
         else
             throwNewOutOfMemoryError(env, "sieb_malloc");
    }
    return adr;
}

void sieb_free (JNIEnv * env, void * adr) {
    free(adr);
}



void sieb_convertToPlatform (char *path) {
    char *pathIndex;

    pathIndex = path;
    while (*pathIndex != '\0') {
        if(*pathIndex == '\\') {
            *pathIndex = '/';
        }
        pathIndex++;
    }
}

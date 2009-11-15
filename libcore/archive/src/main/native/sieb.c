#include "sieb.h"
#include "JNIHelp.h"
#include "jni.h"

#include <malloc.h>

// Throw java.lang.OutOfMemoryError
void throwNewOutOfMemoryError (JNIEnv * env, const char *message)
{
    jniThrowException(env, "java/lang/OutOfMemoryError", message);
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

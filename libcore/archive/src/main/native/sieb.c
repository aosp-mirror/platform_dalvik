/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "sieb.h"
#include "JNIHelp.h"
#include "jni.h"

#include <stdlib.h>

// Throw java.lang.OutOfMemoryError
void throwNewOutOfMemoryError(JNIEnv *env, const char *message) {
    jniThrowException(env, "java/lang/OutOfMemoryError", message);
}

void *sieb_malloc(JNIEnv *env, size_t byteCnt) {
    void *adr = malloc(byteCnt);
    if (adr == 0) {
        if (byteCnt == 0) {
            throwNewOutOfMemoryError(env, "sieb_malloc(0) NOT ALLOWED");
        } else {
            throwNewOutOfMemoryError(env, "sieb_malloc");
        }
    }
    return adr;
}

void sieb_free(JNIEnv *env, void *adr) {
    free(adr);
}

void sieb_convertToPlatform(char *path) {
    char *pathIndex;

    pathIndex = path;
    while (*pathIndex != '\0') {
        if (*pathIndex == '\\') {
            *pathIndex = '/';
        }
        pathIndex++;
    }
}

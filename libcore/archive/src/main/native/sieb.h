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

#if !defined(sieb_h)
#define sieb_h

#include "JNIHelp.h"
#include "jni.h"

void throwNewOutOfMemoryError(JNIEnv *env, const char *message);
void *sieb_malloc(JNIEnv *env, size_t byteCnt);
void sieb_free(JNIEnv *env, void *adr);
void sieb_convertToPlatform(char *path);

#endif /* sieb_h */

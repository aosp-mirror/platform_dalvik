/*
 * Copyright (C) 2010 The Android Open Source Project
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

#ifndef _DALVIK_ALLOC_VISIT
#define _DALVIK_ALLOC_VISIT

#include "Dalvik.h"

/*
 * Callback invoked with the address of a reference and a user
 * supplied context argument.
 */
typedef void Visitor(void *addr, void *arg);

/*
 * Visits references in an object.
 */
void dvmVisitObject(Visitor *visitor, Object *obj, void *arg);

/*
 * Visits references in the root set.
 */
void dvmVisitRoots(Visitor *visitor, void *arg);

#endif /* _DALVIK_ALLOC_VISIT */

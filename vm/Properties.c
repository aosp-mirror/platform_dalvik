/*
 * Copyright (C) 2008 The Android Open Source Project
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
/*
 * Set up values for System.getProperties().
 */
#include "Dalvik.h"

#include <cutils/array.h>
#include <stdlib.h>

bool dvmPropertiesStartup(void)
{
    gDvm.properties = arrayCreate();
    if (gDvm.properties == NULL) {
        return false;
    }
    return true;
}

void dvmPropertiesShutdown(void)
{
    size_t size = arraySize(gDvm.properties);
    size_t i;
    for (i = 0; i < size; ++i) {
        free(arrayGet(gDvm.properties, i));
    }
    arrayFree(gDvm.properties);
}

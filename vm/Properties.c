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
#include <sys/utsname.h>
#include <limits.h>
#include <unistd.h>

bool dvmPropertiesStartup(void)
{
    gDvm.properties = arrayCreate();
    if (gDvm.properties == NULL) {
        return false;
    }

    /*
     * TODO: these are currently awkward to do in Java so we sneak them in
     * here.
     */
    struct utsname info;
    uname(&info);
    char* s;
    asprintf(&s, "os.arch=%s", info.machine);
    arrayAdd(gDvm.properties, s);
    asprintf(&s, "os.name=%s", info.sysname);
    arrayAdd(gDvm.properties, s);
    asprintf(&s, "os.version=%s", info.release);
    arrayAdd(gDvm.properties, s);

    char path[PATH_MAX];
    asprintf(&s, "user.dir=%s", getcwd(path, sizeof(path)));
    arrayAdd(gDvm.properties, s);

    return true;
}

void dvmPropertiesShutdown(void) {
    int i = arraySize(gDvm.properties);
    for (; i >= 0; --i) {
        free(arrayGet(gDvm.properties, i));
    }
    arrayFree(gDvm.properties);
}

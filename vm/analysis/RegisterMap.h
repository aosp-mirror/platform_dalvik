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
 * Register map declaration.
 */
#ifndef _DALVIK_REGISTERMAP
#define _DALVIK_REGISTERMAP

typedef struct RegisterMap {
    /* header */
    char    addrWidth;      /* bytes per address, 1 or 2 */
    char    regWidth;       /* bytes per register line, 1+ */
    char    pad0;
    char    pad1;

    /* entries start here; 32-bit align guaranteed */
    u4      entries[1];
} RegisterMap;

/*
 * Generate the register map for a method.
 *
 * Returns a pointer to newly-allocated storage.
 */
RegisterMap* dvmGenerateRegisterMap(const Method* meth);

/*
 * Release the storage associated with a RegisterMap.
 */
void dvmFreeRegisterMap(RegisterMap* pMap);

#endif /*_DALVIK_REGISTERMAP*/

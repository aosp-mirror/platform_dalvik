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
 * Dalvik classfile verification.
 */
#ifndef _DALVIK_DEXVERIFY
#define _DALVIK_DEXVERIFY

/*
 * Global verification mode.  These must be in order from least verification
 * to most.  If we're using "exact GC", we may need to perform some of
 * the verification steps anyway.
 */
typedef enum {
    VERIFY_MODE_UNKNOWN = 0,
    VERIFY_MODE_NONE,
    VERIFY_MODE_REMOTE,
    VERIFY_MODE_ALL
} DexClassVerifyMode;

bool dvmVerificationStartup(void);
void dvmVerificationShutdown(void);

/*
 * Perform verification on all classes loaded from this DEX file.  If
 * enabled, it must happen before optimization.
 */
bool dvmVerifyAllClasses(DexFile* pDexFile);

/*
 * Verify a single class.
 */
bool dvmVerifyClass(ClassObject* clazz);

/*
 * Release the storage associated with a RegisterMap.
 */
void dvmFreeRegisterMap(RegisterMap* pMap);

#endif /*_DALVIK_DEXVERIFY*/

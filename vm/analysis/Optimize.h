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
 * Bytecode optimization declarations.
 */
#ifndef _DALVIK_OPTIMIZE
#define _DALVIK_OPTIMIZE

/*
 * Prep data structures.
 */
InlineSub* dvmCreateInlineSubsTable(void);
void dvmFreeInlineSubsTable(InlineSub* inlineSubs);

/*
 * Entry point from DEX preparation.
 */
void dvmOptimizeClass(ClassObject* clazz, bool essentialOnly);

/*
 * Abbreviated resolution functions, for use by optimization and verification
 * code.
 */
ClassObject* dvmOptResolveClass(ClassObject* referrer, u4 classIdx,
    VerifyError* pFailure);
Method* dvmOptResolveMethod(ClassObject* referrer, u4 methodIdx,
    MethodType methodType, VerifyError* pFailure);
Method* dvmOptResolveInterfaceMethod(ClassObject* referrer, u4 methodIdx);
InstField* dvmOptResolveInstField(ClassObject* referrer, u4 ifieldIdx,
    VerifyError* pFailure);
StaticField* dvmOptResolveStaticField(ClassObject* referrer, u4 sfieldIdx,
    VerifyError* pFailure);

#endif /*_DALVIK_OPTIMIZE*/

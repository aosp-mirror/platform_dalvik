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

#ifndef DALVIK_VM_COMPILER_CODEGEN_X86_CALLOUT_HELPER_H_
#define DALVIK_VM_COMPILER_CODEGEN_X86_CALLOUT_HELPER_H_

#include "Dalvik.h"

/*
 * Declare/comment prototypes of all native callout functions invoked by the
 * JIT'ed code here and use the LOAD_FUNC_ADDR macro to load the address into
 * a register. In this way we have a centralized place to find out all native
 * helper functions and we can grep for LOAD_FUNC_ADDR to find out all the
 * callsites.
 */

/* Load a statically compiled function address as a constant */
#define LOAD_FUNC_ADDR(cUnit, reg, addr) loadConstant(cUnit, reg, addr)

/* Originally declared in Sync.h */
bool dvmUnlockObject(struct Thread* self, struct Object* obj); //OP_MONITOR_EXIT

/* Originally declared in oo/TypeCheck.h */
bool dvmCanPutArrayElement(const ClassObject* elemClass,   // OP_APUT_OBJECT
                           const ClassObject* arrayClass);
int dvmInstanceofNonTrivial(const ClassObject* instance,   // OP_CHECK_CAST &&
                            const ClassObject* clazz);     // OP_INSTANCE_OF

/* Originally declared in oo/Array.h */
ArrayObject* dvmAllocArrayByClass(ClassObject* arrayClass, // OP_NEW_ARRAY
                                  size_t length, int allocFlags);

/* Originally declared in interp/InterpDefs.h */
bool dvmInterpHandleFillArrayData(ArrayObject* arrayObject,// OP_FILL_ARRAY_DATA
                                  const u2* arrayData);

/* Originally declared in alloc/Alloc.h */
Object* dvmAllocObject(ClassObject* clazz, int flags);  // OP_NEW_INSTANCE

/*
 * Functions declared in gDvmInlineOpsTable[] are used for
 * OP_EXECUTE_INLINE & OP_EXECUTE_INLINE_RANGE.
 */
extern "C" double sqrt(double x);  // INLINE_MATH_SQRT

#endif  // DALVIK_VM_COMPILER_CODEGEN_X86_CALLOUT_HELPER_H_

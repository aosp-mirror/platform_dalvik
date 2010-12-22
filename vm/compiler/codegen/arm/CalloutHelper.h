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

#include "Dalvik.h"

#ifndef _DALVIK_VM_COMPILER_CODEGEN_ARM_CALLOUT_HELPER_H
#define _DALVIK_VM_COMPILER_CODEGEN_ARM_CALLOUT_HELPER_H

/*
 * Declare/comment prototypes of all native callout functions invoked by the
 * JIT'ed code here and use the LOAD_FUNC_ADDR macro to load the address into
 * a register. In this way we have a centralized place to find out all native
 * helper functions and we can grep for LOAD_FUNC_ADDR to find out all the
 * callsites.
 */

/* Load a statically compiled function address as a constant */
#define LOAD_FUNC_ADDR(cUnit, reg, addr) loadConstant(cUnit, reg, addr)

/* Conversions */
float __aeabi_i2f(int op1);             // OP_INT_TO_FLOAT
int __aeabi_f2iz(float op1);            // OP_FLOAT_TO_INT
float __aeabi_d2f(double op1);          // OP_DOUBLE_TO_FLOAT
double __aeabi_f2d(float op1);          // OP_FLOAT_TO_DOUBLE
double __aeabi_i2d(int op1);            // OP_INT_TO_DOUBLE
int __aeabi_d2iz(double op1);           // OP_DOUBLE_TO_INT
float __aeabi_l2f(long op1);            // OP_LONG_TO_FLOAT
double __aeabi_l2d(long op1);           // OP_LONG_TO_DOUBLE
s8 dvmJitf2l(float op1);                // OP_FLOAT_TO_LONG
s8 dvmJitd2l(double op1);               // OP_DOUBLE_TO_LONG

/* Single-precision FP arithmetics */
float __aeabi_fadd(float a, float b);   // OP_ADD_FLOAT[_2ADDR]
float __aeabi_fsub(float a, float b);   // OP_SUB_FLOAT[_2ADDR]
float __aeabi_fdiv(float a, float b);   // OP_DIV_FLOAT[_2ADDR]
float __aeabi_fmul(float a, float b);   // OP_MUL_FLOAT[_2ADDR]
float fmodf(float a, float b);          // OP_REM_FLOAT[_2ADDR]

/* Double-precision FP arithmetics */
double __aeabi_dadd(double a, double b); // OP_ADD_DOUBLE[_2ADDR]
double __aeabi_dsub(double a, double b); // OP_SUB_DOUBLE[_2ADDR]
double __aeabi_ddiv(double a, double b); // OP_DIV_DOUBLE[_2ADDR]
double __aeabi_dmul(double a, double b); // OP_MUL_DOUBLE[_2ADDR]
double fmod(double a, double b);         // OP_REM_DOUBLE[_2ADDR]

/* Integer arithmetics */
int __aeabi_idivmod(int op1, int op2);  // OP_REM_INT[_2ADDR|_LIT8|_LIT16]
int __aeabi_idiv(int op1, int op2);     // OP_DIV_INT[_2ADDR|_LIT8|_LIT16]

/* Long long arithmetics - OP_REM_LONG[_2ADDR] & OP_DIV_LONG[_2ADDR] */
long long __aeabi_ldivmod(long long op1, long long op2);

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

/*
 * Switch dispatch offset calculation for OP_PACKED_SWITCH & OP_SPARSE_SWITCH
 * Used in CodegenDriver.c
 * static s8 findPackedSwitchIndex(const u2* switchData, int testVal, int pc);
 * static s8 findSparseSwitchIndex(const u2* switchData, int testVal, int pc);
 */

/*
 * Resolve interface callsites - OP_INVOKE_INTERFACE & OP_INVOKE_INTERFACE_RANGE
 *
 * Originally declared in mterp/common/FindInterface.h and only comment it here
 * due to the INLINE attribute.
 *
 * INLINE Method* dvmFindInterfaceMethodInCache(ClassObject* thisClass,
 *  u4 methodIdx, const Method* method, DvmDex* methodClassDex)
 */

/* Originally declared in alloc/Alloc.h */
Object* dvmAllocObject(ClassObject* clazz, int flags);  // OP_NEW_INSTANCE

/*
 * Functions declared in gDvmInlineOpsTable[] are used for
 * OP_EXECUTE_INLINE & OP_EXECUTE_INLINE_RANGE.
 *
 *      org_apache_harmony_dalvik_NativeTestTarget_emptyInlineMethod
 *      javaLangString_charAt
 *      javaLangString_compareTo
 *      javaLangString_equals
 *      javaLangString_indexOf_I
 *      javaLangString_indexOf_II
 *      javaLangString_length
 *      javaLangMath_abs_int
 *      javaLangMath_abs_long
 *      javaLangMath_abs_float
 *      javaLangMath_abs_double
 *      javaLangMath_min_int
 *      javaLangMath_max_int
 *      javaLangMath_sqrt
 *      javaLangMath_cos
 *      javaLangMath_sin
 */
double sqrt(double x);  // INLINE_MATH_SQRT

/*
 * The following functions are invoked through the compiler templates (declared
 * in compiler/template/armv5te/footer.S:
 *
 *      __aeabi_cdcmple         // CMPG_DOUBLE
 *      __aeabi_cfcmple         // CMPG_FLOAT
 *      dvmLockObject           // MONITOR_ENTER
 */

#endif /* _DALVIK_VM_COMPILER_CODEGEN_ARM_CALLOUT_HELPER_H */

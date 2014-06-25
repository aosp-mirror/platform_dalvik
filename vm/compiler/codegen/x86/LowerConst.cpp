/*
 * Copyright (C) 2010-2011 Intel Corporation
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


/*! \file LowerConst.cpp
    \brief This file lowers the following bytecodes: CONST_XXX

    Functions are called from the lowered native sequence:
    1> const_string_resolve
       INPUT: const pool index in %eax
       OUTPUT: resolved string in %eax
       The only register that is still live after this function is ebx
    2> class_resolve
       INPUT: const pool index in %eax
       OUTPUT: resolved class in %eax
       The only register that is still live after this function is ebx
*/
#include "libdex/DexOpcodes.h"
#include "libdex/DexFile.h"
#include "Lower.h"
#include "NcgAot.h"
#include "enc_wrapper.h"

#define P_GPR_1 PhysicalReg_EBX
#define P_GPR_2 PhysicalReg_ECX

//! LOWER bytecode CONST_STRING without usage of helper function

//! It calls const_string_resolve (%ebx is live across the call)
//! Since the register allocator does not handle control flow within the lowered native sequence,
//!   we define an interface between the lowering module and register allocator:
//!     rememberState, gotoState, transferToState
//!   to make sure at the control flow merge point the state of registers is the same
int const_string_common_nohelper(u4 tmp, u2 vA) {
#if !defined(WITH_JIT)
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;
    scratchRegs[0] = PhysicalReg_SCRATCH_1; scratchRegs[1] = PhysicalReg_SCRATCH_2;
    get_res_strings(3, false);
    move_mem_to_reg(OpndSize_32, tmp*4, 3, false, PhysicalReg_EAX, true);
    compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    conditional_jump(Condition_NE, ".const_string_continue", true);
    rememberState(1);
    export_pc();  //added on 05/2010
    move_imm_to_reg(OpndSize_32, tmp, PhysicalReg_EAX, true);

    call_helper_API(".const_string_resolve");
    transferToState(1);
    insertLabel(".const_string_continue", true);
    set_virtual_reg(vA, OpndSize_32, PhysicalReg_EAX, true);
#else
    /* for trace-based JIT, the string is already resolved since this code has been executed */
    void *strPtr = (void*)
              (currentMethod->clazz->pDvmDex->pResStrings[tmp]);
    assert(strPtr != NULL);
    set_VR_to_imm(vA, OpndSize_32, (int) strPtr );
#endif
    return 0;
}
//! dispatcher to select either const_string_common_helper or const_string_common_nohelper

//!
int const_string_common(u4 tmp, u2 vA) {
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[13]) {
        return const_string_common_helper(tmp, vA);
    }
    else
#endif
    {
        return const_string_common_nohelper(tmp, vA);
    }
}
#undef P_GPR_1
#undef P_GPR_2

/**
 * @brief Generate native code for bytecode const/4
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_4(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_4);
    u2 vA = mir->dalvikInsn.vA;
    s4 tmp = mir->dalvikInsn.vB;
    set_VR_to_imm(vA, OpndSize_32, tmp);
    return 1;
}

/**
 * @brief Generate native code for bytecode const/16
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_16(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_16);
    u2 BBBB = mir->dalvikInsn.vB;
    u2 vA = mir->dalvikInsn.vA;
    set_VR_to_imm(vA, OpndSize_32, (s2)BBBB);
    return 1;
}

/**
 * @brief Generate native code for bytecode const
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;
    set_VR_to_imm(vA, OpndSize_32, (s4)tmp);
    return 1;
}

/**
 * @brief Generate native code for bytecode const/high16
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_high16(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_HIGH16);
    u2 vA = mir->dalvikInsn.vA;
    u2 tmp = mir->dalvikInsn.vB;
    set_VR_to_imm(vA, OpndSize_32, ((s4)tmp)<<16);
    return 1;
}

/**
 * @brief Generate native code for bytecode const-wide/16
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_wide_16(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_WIDE_16);
    u2 vA = mir->dalvikInsn.vA;
    u2 tmp = mir->dalvikInsn.vB;
    set_VR_to_imm(vA, OpndSize_32, (s2)tmp);
    set_VR_to_imm(vA+1, OpndSize_32, ((s2)tmp)>>31);
    return 2;
}

/**
 * @brief Generate native code for bytecode const-wide/32
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_wide_32(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_WIDE_32);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;
    set_VR_to_imm(vA, OpndSize_32, (s4)tmp);
    set_VR_to_imm(vA+1, OpndSize_32, ((s4)tmp)>>31);
    return 2;
}

/**
 * @brief Generate native code for bytecode const-wide
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_wide(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_WIDE);
    u2 vA = mir->dalvikInsn.vA;
    u8 tmp = mir->dalvikInsn.vB_wide;
    set_VR_to_imm(vA, OpndSize_32, (s4)tmp);
    set_VR_to_imm(vA+1, OpndSize_32, (s4)(tmp >> 32));
    return 2;
}

/**
 * @brief Generate native code for bytecode const-wide/high16
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_wide_high16(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_WIDE_HIGH16);
    u2 vA = mir->dalvikInsn.vA;
    u2 tmp = mir->dalvikInsn.vB;
    set_VR_to_imm(vA, OpndSize_32, 0);
    set_VR_to_imm(vA+1, OpndSize_32, ((s4)tmp)<<16);
    return 2;
}

/**
 * @brief Generate native code for bytecode const-string
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_string(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_STRING);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;
    int retval = const_string_common(tmp, vA);
    return retval;
}

/**
 * @brief Generate native code for bytecode const-string/jumbo
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_string_jumbo(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_STRING_JUMBO);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;
    int retval = const_string_common(tmp, vA);
    return retval;
}

#define P_GPR_1 PhysicalReg_EBX
/**
 * @brief Generate native code for bytecode const-class
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_const_class(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_CONST_CLASS);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;
#if !defined(WITH_JIT)
    // It calls class_resolve (%ebx is live across the call)
    // Since the register allocator does not handle control flow within the lowered native sequence,
    //   we define an interface between the lowering module and register allocator:
    //     rememberState, gotoState, transferToState
    //   to make sure at the control flow merge point the state of registers is the same
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;
    scratchRegs[0] = PhysicalReg_SCRATCH_1; scratchRegs[1] = PhysicalReg_SCRATCH_2;
    get_res_classes(3, false);
    move_mem_to_reg(OpndSize_32, tmp*4, 3, false, PhysicalReg_EAX, true);
    compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    conditional_jump(Condition_NE, ".const_class_resolved", true);
    rememberState(1);
    export_pc();
    move_imm_to_reg(OpndSize_32, tmp, PhysicalReg_EAX, true);
    call_helper_API(".class_resolve");
    transferToState(1);
    insertLabel(".const_class_resolved", true);
    set_virtual_reg(vA, OpndSize_32, PhysicalReg_EAX, true);
#else
    /* for trace-based JIT, the class is already resolved since this code has been executed */
    void *classPtr = (void*)
       (currentMethod->clazz->pDvmDex->pResClasses[tmp]);
    assert(classPtr != NULL);
    set_VR_to_imm(vA, OpndSize_32, (int) classPtr );
#endif

    return 0;
}

#undef P_GPR_1


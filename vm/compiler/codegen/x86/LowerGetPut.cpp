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


/*! \file LowerGetPut.cpp
    \brief This file lowers the following bytecodes: XGET|PUT_XXX
*/
#include "libdex/DexOpcodes.h"
#include "libdex/DexFile.h"
#include "Lower.h"
#include "NcgAot.h"
#include "enc_wrapper.h"

#define P_GPR_1 PhysicalReg_EBX
#define P_GPR_2 PhysicalReg_ECX
#define P_GPR_3 PhysicalReg_ESI
#define P_GPR_4 PhysicalReg_EDX

/**
 * @brief Common function for generating native code for aget variants
 * @details Includes null check and bound check.
 * @param flag
 * @param vA destination VR
 * @param vref VR holding reference
 * @param vindex VR holding index
 * @param mirOptFlags optimization flags for current bytecode
 * @return value >= 0 when handled
 */
int aget_common_nohelper(ArrayAccess flag, u2 vA, u2 vref, u2 vindex, int mirOptFlags) {
    ////////////////////////////
    // Request VR free delays before register allocation for the temporaries
    if(!(mirOptFlags & MIR_IGNORE_NULL_CHECK))
        requestVRFreeDelay(vref,VRDELAY_NULLCHECK);
    if(!(mirOptFlags & MIR_IGNORE_RANGE_CHECK)) {
        requestVRFreeDelay(vref,VRDELAY_BOUNDCHECK);
        requestVRFreeDelay(vindex,VRDELAY_BOUNDCHECK);
    }

    get_virtual_reg(vref, OpndSize_32, 1, false); //array
    get_virtual_reg(vindex, OpndSize_32, 2, false); //index

    if(!(mirOptFlags & MIR_IGNORE_NULL_CHECK)) {
        //last argument is the exception number for this bytecode
        nullCheck(1, false, 1, vref); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vref,VRDELAY_NULLCHECK);
    } else {
        updateRefCount2(1, LowOpndRegType_gp, false); //update reference count for tmp1
    }

    if(!(mirOptFlags & MIR_IGNORE_RANGE_CHECK)) {
        boundCheck(vref, 1, false,
                             vindex, 2, false,
                             2);
        cancelVRFreeDelayRequest(vref,VRDELAY_BOUNDCHECK);
        cancelVRFreeDelayRequest(vindex,VRDELAY_BOUNDCHECK);
    } else {
        updateRefCount2(1, LowOpndRegType_gp, false); //update reference count for tmp1
        updateRefCount2(2, LowOpndRegType_gp, false); //update reference count for tmp2
    }

    if(flag == AGET) {
        move_mem_disp_scale_to_reg(OpndSize_32, 1, false, offArrayObject_contents, 2, false, 4, 4, false);
    }
    else if(flag == AGET_WIDE) {
        move_mem_disp_scale_to_reg(OpndSize_64, 1, false, offArrayObject_contents, 2, false, 8, 1, false);
    }
    else if(flag == AGET_CHAR) {
        movez_mem_disp_scale_to_reg(OpndSize_16, 1, false, offArrayObject_contents, 2, false, 2, 4, false);
    }
    else if(flag == AGET_SHORT) {
        moves_mem_disp_scale_to_reg(OpndSize_16, 1, false, offArrayObject_contents, 2, false, 2, 4, false);
    }
    else if(flag == AGET_BOOLEAN) {
        movez_mem_disp_scale_to_reg(OpndSize_8, 1, false, offArrayObject_contents, 2, false, 1, 4, false);
    }
    else if(flag == AGET_BYTE) {
        moves_mem_disp_scale_to_reg(OpndSize_8, 1, false, offArrayObject_contents, 2, false, 1, 4, false);
    }
    if(flag == AGET_WIDE) {
        set_virtual_reg(vA, OpndSize_64, 1, false);
    }
    else {
        set_virtual_reg(vA, OpndSize_32, 4, false);
    }
    //////////////////////////////////
    return 0;
}
#if 0 /* Code is deprecated. If reenabled, needs additional parameter
         for optimization flags*/
//! wrapper to call either aget_common_helper or aget_common_nohelper

//!
int aget_common(int flag, u2 vA, u2 vref, u2 vindex) {
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[7]) {
        return aget_common_helper(flag, vA, vref, vindex);
    } else
#endif
    {
        return aget_common_nohelper(flag, vA, vref, vindex);
    }
}
#endif
#undef P_GPR_1
#undef P_GPR_2
#undef P_GPR_3
#undef P_GPR_4

/**
 * @brief Generate native code for bytecode aget and aget-object
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET
            || mir->dalvikInsn.opcode == OP_AGET_OBJECT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aget_common_nohelper(AGET, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aget-wide
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget_wide(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET_WIDE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aget_common_nohelper(AGET_WIDE, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aget-object
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget_object(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET_OBJECT);
    return op_aget(mir);
}

/**
 * @brief Generate native code for bytecode aget-boolean
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget_boolean(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET_BOOLEAN);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aget_common_nohelper(AGET_BOOLEAN, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aget-byte
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget_byte(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET_BYTE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aget_common_nohelper(AGET_BYTE, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aget-char
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget_char(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET_CHAR);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aget_common_nohelper(AGET_CHAR, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aget-short
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aget_short(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_AGET_SHORT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aget_common_nohelper(AGET_SHORT, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

#define P_GPR_1 PhysicalReg_EBX
#define P_GPR_2 PhysicalReg_ECX
#define P_GPR_3 PhysicalReg_ESI
#define P_GPR_4 PhysicalReg_EDX

/**
 * @brief Common function for generating native code for aput variants
 * @details Includes null check and bound check.
 * @param flag
 * @param vA destination VR
 * @param vref VR holding reference
 * @param vindex VR holding index
 * @param mirOptFlags optimization flags for current bytecode
 * @return value >= 0 when handled
 */
int aput_common_nohelper(ArrayAccess flag, u2 vA, u2 vref, u2 vindex, int mirOptFlags) {
    //////////////////////////////////////
    // Request VR free delays before register allocation for the temporaries.
    // No need to request delay for vA since it will be transferred to temporary
    // after the null check and bound check.
    if(!(mirOptFlags & MIR_IGNORE_NULL_CHECK))
        requestVRFreeDelay(vref,VRDELAY_NULLCHECK);
    if(!(mirOptFlags & MIR_IGNORE_RANGE_CHECK)) {
        requestVRFreeDelay(vref,VRDELAY_BOUNDCHECK);
        requestVRFreeDelay(vindex,VRDELAY_BOUNDCHECK);
    }

    get_virtual_reg(vref, OpndSize_32, 1, false); //array
    get_virtual_reg(vindex, OpndSize_32, 2, false); //index

    if(!(mirOptFlags & MIR_IGNORE_NULL_CHECK)) {
        //last argument is the exception number for this bytecode
        nullCheck(1, false, 1, vref); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vref,VRDELAY_NULLCHECK);
    } else {
        updateRefCount2(1, LowOpndRegType_gp, false); //update reference count for tmp1
    }

    if(!(mirOptFlags & MIR_IGNORE_RANGE_CHECK)) {
        boundCheck(vref, 1, false,
                             vindex, 2, false,
                             2);
        cancelVRFreeDelayRequest(vref,VRDELAY_BOUNDCHECK);
        cancelVRFreeDelayRequest(vindex,VRDELAY_BOUNDCHECK);
    } else {
        updateRefCount2(1, LowOpndRegType_gp, false); //update reference count for tmp1
        updateRefCount2(2, LowOpndRegType_gp, false); //update reference count for tmp2
    }

    if(flag == APUT_WIDE) {
        get_virtual_reg(vA, OpndSize_64, 1, false);
    }
    else {
        get_virtual_reg(vA, OpndSize_32, 4, false);
    }
    if(flag == APUT)
        move_reg_to_mem_disp_scale(OpndSize_32, 4, false, 1, false, offArrayObject_contents, 2, false, 4);
    else if(flag == APUT_WIDE)
        move_reg_to_mem_disp_scale(OpndSize_64, 1, false, 1, false, offArrayObject_contents, 2, false, 8);
    else if(flag == APUT_CHAR || flag == APUT_SHORT)
        move_reg_to_mem_disp_scale(OpndSize_16, 4, false, 1, false, offArrayObject_contents, 2, false, 2);
    else if(flag == APUT_BOOLEAN || flag == APUT_BYTE)
        move_reg_to_mem_disp_scale(OpndSize_8, 4, false, 1, false, offArrayObject_contents, 2, false, 1);
    //////////////////////////////////
    return 0;
}
#if 0 /* Code is deprecated. If reenabled, needs additional parameter
         for optimization flags*/
//! wrapper to call either aput_common_helper or aput_common_nohelper

//!
int aput_common(int flag, u2 vA, u2 vref, u2 vindex) {
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[18]) {
        return aput_common_helper(flag, vA, vref, vindex);
    } else
#endif
    {
        return aput_common_nohelper(flag, vA, vref, vindex);
    }
}
#endif
#undef P_GPR_1
#undef P_GPR_2
#undef P_GPR_3
#undef P_GPR_4

/**
 * @brief Generate native code for bytecode aput
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_APUT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aput_common_nohelper(APUT, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aput-wide
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput_wide(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_APUT_WIDE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aput_common_nohelper(APUT_WIDE, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aput-boolean
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput_boolean(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_APUT_BOOLEAN);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aput_common_nohelper(APUT_BOOLEAN, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aput-byte
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput_byte(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_APUT_BYTE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aput_common_nohelper(APUT_BYTE, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aput-char
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput_char(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_APUT_CHAR);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aput_common_nohelper(APUT_CHAR, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

/**
 * @brief Generate native code for bytecode aput-short
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput_short(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_APUT_SHORT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
    int retval = aput_common_nohelper(APUT_SHORT, vA, vref, vindex,
            mir->OptimizationFlags);
    return retval;
}

#define P_GPR_1 PhysicalReg_EBX //callee-saved valid after CanPutArray
#define P_GPR_2 PhysicalReg_ECX
#define P_GPR_3 PhysicalReg_ESI //callee-saved
#define P_SCRATCH_1 PhysicalReg_EDX
#define P_SCRATCH_2 PhysicalReg_EAX
#define P_SCRATCH_3 PhysicalReg_EDX

void markCard_notNull(int tgtAddrReg, int scratchReg, bool isPhysical);

/**
 * @brief Generate native code for bytecode aput-object
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_aput_object(const MIR * mir) { //type checking
    assert(mir->dalvikInsn.opcode == OP_APUT_OBJECT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vref = mir->dalvikInsn.vB;
    u2 vindex = mir->dalvikInsn.vC;
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[6]) {
        export_pc(); //use %edx
        move_imm_to_reg(OpndSize_32, vA, P_SCRATCH_1, true);
        move_imm_to_reg(OpndSize_32, vref, P_SCRATCH_2, true);
        move_imm_to_reg(OpndSize_32, vindex, P_GPR_2, true);

        spillVirtualReg(vref, LowOpndRegType_gp, true);
        spillVirtualReg(vindex, LowOpndRegType_gp, true);
        spillVirtualReg(vA, LowOpndRegType_gp, true);
        call_helper_API(".aput_obj_helper");
    }
    else
#endif
    {
        ///////////////////////////
        // Request VR free delays before register allocation for the temporaries
        // No need to request delay for vA since it will be transferred to temporary
        // after the null check and bound check.
        if(!(mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK))
            requestVRFreeDelay(vref,VRDELAY_NULLCHECK);
        if(!(mir->OptimizationFlags & MIR_IGNORE_RANGE_CHECK)) {
            requestVRFreeDelay(vref,VRDELAY_BOUNDCHECK);
            requestVRFreeDelay(vindex,VRDELAY_BOUNDCHECK);
        }

        get_virtual_reg(vref, OpndSize_32, 1, false); //array
        export_pc(); //use %edx

        if(!(mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK)) {
            compare_imm_reg(OpndSize_32, 0, 1, false);
            conditional_jump_global_API(Condition_E, "common_errNullObject", false);
            cancelVRFreeDelayRequest(vref,VRDELAY_NULLCHECK);
        } else {
            updateRefCount2(1, LowOpndRegType_gp, false); //update reference count for tmp1
        }

        get_virtual_reg(vindex, OpndSize_32, 2, false); //index
        if(!(mir->OptimizationFlags & MIR_IGNORE_RANGE_CHECK)) {
            compare_mem_reg(OpndSize_32, offArrayObject_length, 1, false, 2, false);
            conditional_jump_global_API(Condition_NC, "common_errArrayIndex", false);
            cancelVRFreeDelayRequest(vref,VRDELAY_BOUNDCHECK);
            cancelVRFreeDelayRequest(vindex,VRDELAY_BOUNDCHECK);
        } else {
            updateRefCount2(1, LowOpndRegType_gp, false); //update reference count for tmp1
            updateRefCount2(2, LowOpndRegType_gp, false); //update reference count for tmp2
        }

        get_virtual_reg(vA, OpndSize_32, 4, false);
        compare_imm_reg(OpndSize_32, 0, 4, false);
        conditional_jump(Condition_E, ".aput_object_skip_check", true);
        rememberState(1);
        move_mem_to_reg(OpndSize_32, offObject_clazz, 4, false, 5, false);
        load_effective_addr(-12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
        move_reg_to_mem(OpndSize_32, 5, false, 0, PhysicalReg_ESP, true);
        move_mem_to_reg(OpndSize_32, offObject_clazz, 1, false, 6, false);
        move_reg_to_mem(OpndSize_32, 6, false, 4, PhysicalReg_ESP, true);

        scratchRegs[0] = PhysicalReg_SCRATCH_1;
        call_dvmCanPutArrayElement(); //scratch??
        load_effective_addr(12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
        compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
        conditional_jump_global_API(Condition_E, "common_errArrayStore", false);

        //NOTE: "2, false" is live through function call
        move_reg_to_mem_disp_scale(OpndSize_32, 4, false, 1, false, offArrayObject_contents, 2, false, 4);
        markCard_notNull(1, 11, false);
        rememberState(2);
        ////TODO NCG O1 + code cache
        unconditional_jump(".aput_object_after_check", true);

        insertLabel(".aput_object_skip_check", true);
        goToState(1);
        //NOTE: "2, false" is live through function call
        move_reg_to_mem_disp_scale(OpndSize_32, 4, false, 1, false, offArrayObject_contents, 2, false, 4);

        transferToState(2);
        insertLabel(".aput_object_after_check", true);
        ///////////////////////////////
    }
    return 0;
}
#undef P_GPR_1
#undef P_GPR_2
#undef P_GPR_3
#undef P_SCRATCH_1
#undef P_SCRATCH_2
#undef P_SCRATCH_3

//////////////////////////////////////////
#define P_GPR_1 PhysicalReg_ECX
#define P_GPR_2 PhysicalReg_EBX //should be callee-saved to avoid overwritten by inst_field_resolve
#define P_GPR_3 PhysicalReg_ESI
#define P_SCRATCH_1 PhysicalReg_EDX

/*
   movl offThread_cardTable(self), scratchReg
   compare_imm_reg 0, valReg (testl valReg, valReg)
   je .markCard_skip
   shrl $GC_CARD_SHIFT, tgtAddrReg
   movb %, (scratchReg, tgtAddrReg)
   NOTE: scratchReg can be accessed with the corresponding byte
         tgtAddrReg will be updated
   for O1, update the corresponding reference count
*/
void markCard(int valReg, int tgtAddrReg, bool targetPhysical, int scratchReg, bool isPhysical) {
   get_self_pointer(PhysicalReg_SCRATCH_6, isScratchPhysical);
   move_mem_to_reg(OpndSize_32, offsetof(Thread, cardTable), PhysicalReg_SCRATCH_6, isScratchPhysical, scratchReg, isPhysical);
   compare_imm_reg(OpndSize_32, 0, valReg, isPhysical);
   conditional_jump(Condition_E, ".markCard_skip", true);
   alu_binary_imm_reg(OpndSize_32, shr_opc, GC_CARD_SHIFT, tgtAddrReg, targetPhysical);
   move_reg_to_mem_disp_scale(OpndSize_8, scratchReg, isPhysical, scratchReg, isPhysical, 0, tgtAddrReg, targetPhysical, 1);
   insertLabel(".markCard_skip", true);
}

void markCard_notNull(int tgtAddrReg, int scratchReg, bool isPhysical) {
   get_self_pointer(PhysicalReg_SCRATCH_2, isScratchPhysical);
   move_mem_to_reg(OpndSize_32, offsetof(Thread, cardTable), PhysicalReg_SCRATCH_2, isScratchPhysical, scratchReg, isPhysical);
   alu_binary_imm_reg(OpndSize_32, shr_opc, GC_CARD_SHIFT, tgtAddrReg, isPhysical);
   move_reg_to_mem_disp_scale(OpndSize_8, scratchReg, isPhysical, scratchReg, isPhysical, 0, tgtAddrReg, isPhysical, 1);
}

void markCard_filled(int tgtAddrReg, bool isTgtPhysical, int scratchReg, bool isScratchPhysical) {
   get_self_pointer(PhysicalReg_SCRATCH_2, false/*isPhysical*/);
   move_mem_to_reg(OpndSize_32, offsetof(Thread, cardTable), PhysicalReg_SCRATCH_2, isScratchPhysical, scratchReg, isScratchPhysical);
   alu_binary_imm_reg(OpndSize_32, shr_opc, GC_CARD_SHIFT, tgtAddrReg, isTgtPhysical);
   move_reg_to_mem_disp_scale(OpndSize_8, scratchReg, isScratchPhysical, scratchReg, isScratchPhysical, 0, tgtAddrReg, isTgtPhysical, 1);
}

/**
 * @brief Common function for generating native code for iget and iput variants
 * @details Includes null check
 * @param referenceIndex instance field index
 * @param flag type of instance access
 * @param vA value register
 * @param vB object register
 * @param isObj true iff mnemonic is object variant
 * @param isVolatile iff mnemonic is volatile variant
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int iget_iput_common_nohelper(u2 referenceIndex, InstanceAccess flag, u2 vA,
        u2 vB, bool isObj, bool isVolatile, const MIR * mir) {
#if !defined(WITH_JIT)
    ///////////////////////////////
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;
    scratchRegs[0] = PhysicalReg_SCRATCH_1; scratchRegs[1] = PhysicalReg_SCRATCH_2;
    get_res_fields(3, false);
    //move_mem_to_reg(OpndSize_32, referenceIndex*4, 3, false, 4, false);
    compare_imm_mem(OpndSize_32, 0, referenceIndex*4, 3, false);
    move_mem_to_reg(OpndSize_32, referenceIndex*4, 3, false, PhysicalReg_EAX, true);
    /*********************************
    compare_imm_reg(OpndSize_32, 0, 4, false);
    **********************************/
    export_pc(); //use %edx
    conditional_jump(Condition_NE, ".iget_iput_resolved", true);
    rememberState(1);
    move_imm_to_reg(OpndSize_32, referenceIndex, PhysicalReg_EAX, true);
    call_helper_API(".inst_field_resolve");
    transferToState(1);
    insertLabel(".iget_iput_resolved", true);
#else
#ifdef WITH_JIT_INLINING
    const Method *method =
            (mir->OptimizationFlags & MIR_CALLEE) ?
                    mir->meta.calleeMethod : currentMethod;
    InstField *pInstField =
            (InstField *) method->clazz->pDvmDex->pResFields[referenceIndex];
#else
    InstField *pInstField = (InstField *)
            currentMethod->clazz->pDvmDex->pResFields[referenceIndex];
#endif
    int fieldOffset;

    assert(pInstField != NULL);
    fieldOffset = pInstField->byteOffset;
    move_imm_to_reg(OpndSize_32, fieldOffset, 8, false);
#endif

    // Request VR delay before transfer to temporary. Only vB needs delay.
    // vA will have non-zero reference count since transfer to temporary for
    // it happens after null check, thus no delay is needed.
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        requestVRFreeDelay(vB,VRDELAY_NULLCHECK);
    }
    get_virtual_reg(vB, OpndSize_32, 7, false);
    //If we can't ignore the NULL check
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        nullCheck(7, false, 2, vB); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vB,VRDELAY_NULLCHECK);
    }

#if !defined(WITH_JIT)
    move_mem_to_reg(OpndSize_32, offInstField_byteOffset, PhysicalReg_EAX, true, 8, false); //byte offest
#endif
    if(flag == IGET) {
        move_mem_scale_to_reg(OpndSize_32, 7, false, 8, false, 1, 9, false);
        set_virtual_reg(vA, OpndSize_32, 9, false);
#ifdef DEBUG_IGET_OBJ
        if(isObj > 0) {
            pushAllRegs();
            load_effective_addr(-16, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
            move_reg_to_mem(OpndSize_32, 9, false, 12, PhysicalReg_ESP, true); //field
            move_reg_to_mem(OpndSize_32, 7, false, 8, PhysicalReg_ESP, true); //object
            move_imm_to_mem(OpndSize_32, referenceIndex, 4, PhysicalReg_ESP, true); //field
            move_imm_to_mem(OpndSize_32, 0, 0, PhysicalReg_ESP, true); //iget
            call_dvmDebugIgetIput();
            load_effective_addr(16, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
            popAllRegs();
        }
#endif
    } else if(flag == IGET_WIDE) {
        if(isVolatile) {
            /* call dvmQuasiAtomicRead64(addr) */
            load_effective_addr(fieldOffset, 7, false, 9, false);
            move_reg_to_mem(OpndSize_32, 9, false, -4, PhysicalReg_ESP, true); //1st argument
            load_effective_addr(-4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
            nextVersionOfHardReg(PhysicalReg_EAX, 2);
            nextVersionOfHardReg(PhysicalReg_EDX, 2);
            scratchRegs[0] = PhysicalReg_SCRATCH_3;
            call_dvmQuasiAtomicRead64();
            load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
            //memory content in %edx, %eax
            set_virtual_reg(vA, OpndSize_32, PhysicalReg_EAX, true);
            set_virtual_reg(vA+1, OpndSize_32, PhysicalReg_EDX, true);
        } else {
            move_mem_scale_to_reg(OpndSize_64, 7, false, 8, false, 1, 1, false); //access field
            set_virtual_reg(vA, OpndSize_64, 1, false);
        }
    } else if(flag == IPUT) {
        get_virtual_reg(vA, OpndSize_32, 9, false);
        move_reg_to_mem_scale(OpndSize_32, 9, false, 7, false, 8, false, 1); //access field
        if(isObj) {
            markCard(9, 7, false, 11, false);
        }
    } else if(flag == IPUT_WIDE) {
        get_virtual_reg(vA, OpndSize_64, 1, false);
        if(isVolatile) {
            /* call dvmQuasiAtomicSwap64(val, addr) */
            load_effective_addr(fieldOffset, 7, false, 9, false);
            move_reg_to_mem(OpndSize_32, 9, false, -4, PhysicalReg_ESP, true); //2nd argument
            move_reg_to_mem(OpndSize_64, 1, false, -12, PhysicalReg_ESP, true); //1st argument
            load_effective_addr(-12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
            scratchRegs[0] = PhysicalReg_SCRATCH_3;
            call_dvmQuasiAtomicSwap64();
            load_effective_addr(12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
        }
        else {
            move_reg_to_mem_scale(OpndSize_64, 1, false, 7, false, 8, false, 1);
        }
    }
    ///////////////////////////
    return 0;
}

#if 0 /* Code is deprecated. If reenabled, needs additional parameter
         for optimization flags*/
//! wrapper to call either iget_iput_common_helper or iget_iput_common_nohelper

//!
int iget_iput_common(int tmp, int flag, u2 vA, u2 vB, int isObj, bool isVolatile) {
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[9]) {
        return iget_iput_common_helper(tmp, flag, vA, vB);
    }
    else
#endif
    {
        return iget_iput_common_nohelper(tmp, flag, vA, vB, isObj, isVolatile);
    }
}
#endif
#undef P_GPR_1
#undef P_GPR_2
#undef P_GPR_3
#undef P_SCRATCH_1

/**
 * @brief Generate native code for bytecodes iget, iget-boolean,
 * iget-byte, iget-char, iget-short, and iget/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET
            || mir->dalvikInsn.opcode == OP_IGET_BOOLEAN
            || mir->dalvikInsn.opcode == OP_IGET_BYTE
            || mir->dalvikInsn.opcode == OP_IGET_CHAR
            || mir->dalvikInsn.opcode == OP_IGET_SHORT
            || mir->dalvikInsn.opcode == OP_IGET_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    u2 referenceIndex = mir->dalvikInsn.vC;
    int retval = iget_iput_common_nohelper(referenceIndex, IGET, vA, vB, false,
            false, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes iget-wide
 * and iget-wide/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_wide(const MIR * mir, bool isVolatile) {
    assert(mir->dalvikInsn.opcode == OP_IGET_WIDE
            || mir->dalvikInsn.opcode == OP_IGET_WIDE_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    u2 referenceIndex = mir->dalvikInsn.vC;
    int retval = iget_iput_common_nohelper(referenceIndex, IGET_WIDE, vA, vB,
            false, isVolatile, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes iget-object
 * and iget-object/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_object(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_OBJECT
            || mir->dalvikInsn.opcode == OP_IGET_OBJECT_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    u2 referenceIndex = mir->dalvikInsn.vC;
    int retval = iget_iput_common_nohelper(referenceIndex, IGET, vA, vB, true,
            false, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecode iget-boolean
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_boolean(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_BOOLEAN);
    return op_iget(mir);
}

/**
 * @brief Generate native code for bytecode iget-byte
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_byte(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_BYTE);
    return op_iget(mir);
}

/**
 * @brief Generate native code for bytecode iget-char
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_char(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_CHAR);
    return op_iget(mir);
}

/**
 * @brief Generate native code for bytecode iget-short
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_short(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_SHORT);
    return op_iget(mir);
}

/**
 * @brief Generate native code for bytecodes iput, iput-boolean,
 * iput-byte, iput-char, iput-short, and iput/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT
            || mir->dalvikInsn.opcode == OP_IPUT_BOOLEAN
            || mir->dalvikInsn.opcode == OP_IPUT_BYTE
            || mir->dalvikInsn.opcode == OP_IPUT_CHAR
            || mir->dalvikInsn.opcode == OP_IPUT_SHORT
            || mir->dalvikInsn.opcode == OP_IPUT_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    u2 referenceIndex = mir->dalvikInsn.vC;
    int retval = iget_iput_common_nohelper(referenceIndex, IPUT, vA, vB, false,
            false, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes iput-wide
 * and iput-wide/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_wide(const MIR * mir, bool isVolatile) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_WIDE
            || mir->dalvikInsn.opcode == OP_IPUT_WIDE_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    u2 referenceIndex = mir->dalvikInsn.vC;
    int retval = iget_iput_common_nohelper(referenceIndex, IPUT_WIDE, vA, vB,
            false, isVolatile, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes iput-object
 * and iput-object/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_object(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_OBJECT
            || mir->dalvikInsn.opcode == OP_IPUT_OBJECT_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    u2 referenceIndex = mir->dalvikInsn.vC;
    int retval = iget_iput_common_nohelper(referenceIndex, IPUT, vA, vB, true,
            false, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecode iput-boolean
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_boolean(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_BOOLEAN);
    return op_iput(mir);
}

/**
 * @brief Generate native code for bytecode iput-byte
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_byte(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_BYTE);
    return op_iput(mir);
}

/**
 * @brief Generate native code for bytecode iput-char
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_char(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_CHAR);
    return op_iput(mir);
}

/**
 * @brief Generate native code for bytecode iput-short
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_short(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_SHORT);
    return op_iput(mir);
}

#define P_GPR_1 PhysicalReg_EBX
#define P_GPR_2 PhysicalReg_ECX
#define P_GPR_3 PhysicalReg_EDX //used by helper only

/**
 * @brief Common function for generating native code for sget and sput variants
 * @details Includes null check
 * @param flag type of static access
 * @param vA value register
 * @param referenceIndex static field index
 * @param isObj true iff mnemonic is object variant
 * @param isVolatile iff mnemonic is volatile variant
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int sget_sput_common(StaticAccess flag, u2 vA, u2 referenceIndex, bool isObj,
        bool isVolatile, const MIR * mir) {
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[5]) {
        return sget_sput_common_helper(flag, vA, referenceIndex, isObj);
    }
    else
#endif
    {
        //call assembly static_field_resolve
        //no exception
        //glue: get_res_fields
        //hard-coded: eax (one version?)
        //////////////////////////////////////////
#if !defined(WITH_JIT)
        scratchRegs[2] = PhysicalReg_EDX; scratchRegs[3] = PhysicalReg_Null;
        scratchRegs[0] = PhysicalReg_SCRATCH_1; scratchRegs[1] = PhysicalReg_SCRATCH_2;
        get_res_fields(3, false);
        move_mem_to_reg(OpndSize_32, referenceIndex*4, 3, false, PhysicalReg_EAX, true);
        compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true); //InstanceField
        conditional_jump(Condition_NE, ".sget_sput_resolved", true);
        rememberState(1);
        move_imm_to_reg(OpndSize_32, referenceIndex, PhysicalReg_EAX, true);

        export_pc(); //use %edx
        call_helper_API(".static_field_resolve");
        transferToState(1);
        insertLabel(".sget_sput_resolved", true);
#else
#ifdef WITH_JIT_INLINING
        const Method *method =
                (mir->OptimizationFlags & MIR_CALLEE) ?
                        mir->meta.calleeMethod : currentMethod;
        void *fieldPtr =
                (void*) (method->clazz->pDvmDex->pResFields[referenceIndex]);
#else
        void *fieldPtr = (void*)
              (currentMethod->clazz->pDvmDex->pResFields[referenceIndex]);
#endif

        /* Usually, fieldPtr should not be null. The interpreter should resolve
         * it before we come here, or not allow this opcode in a trace. However,
         * we can be in a loop trace and this opcode might have been picked up
         * by exhaustTrace. Sending a -1 here will terminate the loop formation
         * and fall back to normal trace, which will not have this opcode.
         */
        if (!fieldPtr)
            return -1;

        move_imm_to_reg(OpndSize_32, (int)fieldPtr, PhysicalReg_EAX, true);
#endif
        if(flag == SGET) {
            move_mem_to_reg(OpndSize_32, offStaticField_value, PhysicalReg_EAX, true, 7, false); //access field
            set_virtual_reg(vA, OpndSize_32, 7, false);
        } else if(flag == SGET_WIDE) {
            if(isVolatile) {
                /* call dvmQuasiAtomicRead64(addr) */
                load_effective_addr(offStaticField_value, PhysicalReg_EAX, true, 9, false);
                move_reg_to_mem(OpndSize_32, 9, false, -4, PhysicalReg_ESP, true); //1st argument
                load_effective_addr(-4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
                nextVersionOfHardReg(PhysicalReg_EAX, 2);
                nextVersionOfHardReg(PhysicalReg_EDX, 2);
                scratchRegs[0] = PhysicalReg_SCRATCH_3;
                call_dvmQuasiAtomicRead64();
                load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
                //memory content in %edx, %eax
                set_virtual_reg(vA, OpndSize_32, PhysicalReg_EAX, true);
                set_virtual_reg(vA+1, OpndSize_32, PhysicalReg_EDX, true);
            }
            else {
                move_mem_to_reg(OpndSize_64, offStaticField_value, PhysicalReg_EAX, true, 1, false); //access field
                set_virtual_reg(vA, OpndSize_64, 1, false);
            }
        } else if(flag == SPUT) {
            get_virtual_reg(vA, OpndSize_32, 7, false);
            move_reg_to_mem(OpndSize_32, 7, false, offStaticField_value, PhysicalReg_EAX, true); //access field
            if(isObj) {
                /* get clazz object, then use clazz object to mark card */
                move_mem_to_reg(OpndSize_32, offField_clazz, PhysicalReg_EAX, true, 12, false);
                markCard(7/*valReg*/, 12, false, 11, false);
            }
        } else if(flag == SPUT_WIDE) {
            get_virtual_reg(vA, OpndSize_64, 1, false);
            if(isVolatile) {
                /* call dvmQuasiAtomicSwap64(val, addr) */
                load_effective_addr(offStaticField_value, PhysicalReg_EAX, true, 9, false);
                move_reg_to_mem(OpndSize_32, 9, false, -4, PhysicalReg_ESP, true); //2nd argument
                move_reg_to_mem(OpndSize_64, 1, false, -12, PhysicalReg_ESP, true); //1st argument
                load_effective_addr(-12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
                scratchRegs[0] = PhysicalReg_SCRATCH_3;
                call_dvmQuasiAtomicSwap64();
                load_effective_addr(12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
            }
            else {
                move_reg_to_mem(OpndSize_64, 1, false, offStaticField_value, PhysicalReg_EAX, true); //access field
            }
        }
        //////////////////////////////////////////////
    }
    return 0;
}
#undef P_GPR_1
#undef P_GPR_2
#undef P_GPR_3

/**
 * @brief Generate native code for bytecodes sget, sget-boolean,
 * sget-byte, sget-char, sget-object, sget-short, and sget/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SGET
            || mir->dalvikInsn.opcode == OP_SGET_BOOLEAN
            || mir->dalvikInsn.opcode == OP_SGET_BYTE
            || mir->dalvikInsn.opcode == OP_SGET_CHAR
            || mir->dalvikInsn.opcode == OP_SGET_OBJECT
            || mir->dalvikInsn.opcode == OP_SGET_SHORT
            || mir->dalvikInsn.opcode == OP_SGET_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 referenceIndex = mir->dalvikInsn.vB;
    int retval = sget_sput_common(SGET, vA, referenceIndex, false, false, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes sget-wide
 * and sget-wide/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget_wide(const MIR * mir, bool isVolatile) {
    assert(mir->dalvikInsn.opcode == OP_SGET_WIDE
            || mir->dalvikInsn.opcode == OP_SGET_WIDE_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 referenceIndex = mir->dalvikInsn.vB;
    int retval = sget_sput_common(SGET_WIDE, vA, referenceIndex, false,
            isVolatile, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes sget-object and
 * sget-object/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget_object(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SGET_OBJECT
            || mir->dalvikInsn.opcode == OP_SGET_OBJECT_VOLATILE);
    return op_sget(mir);
}

/**
 * @brief Generate native code for bytecode sget-boolean
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget_boolean(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SGET_BOOLEAN);
    return op_sget(mir);
}

/**
 * @brief Generate native code for bytecode sget-byte
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget_byte(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SGET_BYTE);
    return op_sget(mir);
}

/**
 * @brief Generate native code for bytecode sget-char
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget_char(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SGET_CHAR);
    return op_sget(mir);
}

/**
 * @brief Generate native code for bytecode sget-short
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sget_short(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SGET_SHORT);
    return op_sget(mir);
}

/**
 * @brief Generate native code for bytecodes sput, sput-boolean,
 * sput-byte, sput-char, sput-object, sput-short, and sput/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput(const MIR * mir, bool isObj) {
    assert(mir->dalvikInsn.opcode == OP_SPUT
            || mir->dalvikInsn.opcode == OP_SPUT_BOOLEAN
            || mir->dalvikInsn.opcode == OP_SPUT_BYTE
            || mir->dalvikInsn.opcode == OP_SPUT_CHAR
            || mir->dalvikInsn.opcode == OP_SPUT_OBJECT
            || mir->dalvikInsn.opcode == OP_SPUT_SHORT
            || mir->dalvikInsn.opcode == OP_SPUT_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 referenceIndex = mir->dalvikInsn.vB;
    int retval = sget_sput_common(SPUT, vA, referenceIndex, isObj, false, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes sput-wide
 * and sput-wide/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput_wide(const MIR * mir, bool isVolatile) {
    assert(mir->dalvikInsn.opcode == OP_SPUT_WIDE
            || mir->dalvikInsn.opcode == OP_SPUT_WIDE_VOLATILE);
    u2 vA = mir->dalvikInsn.vA;
    u2 referenceIndex = mir->dalvikInsn.vB;
    int retval = sget_sput_common(SPUT_WIDE, vA, referenceIndex, false,
            isVolatile, mir);
    return retval;
}

/**
 * @brief Generate native code for bytecodes sput-object and
 * sput-object/volatile
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput_object(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SPUT_OBJECT
            || mir->dalvikInsn.opcode == OP_SPUT_OBJECT_VOLATILE);
    return op_sput(mir, true /*isObj*/);
}

/**
 * @brief Generate native code for bytecode sput-boolean
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput_boolean(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SPUT_BOOLEAN);
    return op_sput(mir, false /*isObj*/);
}

/**
 * @brief Generate native code for bytecode sput-byte
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput_byte(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SPUT_BYTE);
    return op_sput(mir, false /*isObj*/);
}

/**
 * @brief Generate native code for bytecode sput-char
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput_char(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SPUT_CHAR);
    return op_sput(mir, false /*isObj*/);
}

/**
 * @brief Generate native code for bytecode sput-short
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_sput_short(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_SPUT_SHORT);
    return op_sput(mir, false /*isObj*/);
}
#define P_GPR_1 PhysicalReg_EBX
#define P_GPR_2 PhysicalReg_ECX

/**
 * @brief Generate native code for bytecodes iget-quick and iget-object-quick
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_quick(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_QUICK
            || mir->dalvikInsn.opcode == OP_IGET_OBJECT_QUICK);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB; //object
    u2 fieldByteOffset = mir->dalvikInsn.vC;

    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        requestVRFreeDelay(vB,VRDELAY_NULLCHECK); // Request VR delay before transfer to temporary
    }

    get_virtual_reg(vB, OpndSize_32, 1, false);

    //If we can't ignore the NULL check
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        nullCheck(1, false, 1, vB); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vB,VRDELAY_NULLCHECK);
    }

    move_mem_to_reg(OpndSize_32, fieldByteOffset, 1, false, 2, false);
    set_virtual_reg(vA, OpndSize_32, 2, false);
    return 0;
}
#undef P_GPR_1
#undef P_GPR_2
#define P_GPR_1 PhysicalReg_EBX

/**
 * @brief Generate native code for bytecode
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_wide_quick(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_WIDE_QUICK);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB; //object
    u2 fieldByteOffset = mir->dalvikInsn.vC;

    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        requestVRFreeDelay(vB,VRDELAY_NULLCHECK); // Request VR delay before transfer to temporary
    }

    get_virtual_reg(vB, OpndSize_32, 1, false);

    //If we can't ignore the NULL check
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        nullCheck(1, false, 1, vB); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vB,VRDELAY_NULLCHECK);
    }

    move_mem_to_reg(OpndSize_64, fieldByteOffset, 1, false, 1, false);
    set_virtual_reg(vA, OpndSize_64, 1, false);
    return 0;
}
#undef P_GPR_1

/**
 * @brief Generate native code for bytecode
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iget_object_quick(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IGET_OBJECT_QUICK);
    return op_iget_quick(mir);
}
#define P_GPR_1 PhysicalReg_EBX
#define P_GPR_2 PhysicalReg_ECX

/**
 *
 * @param mir
 * @param isObj
 * @return
 */
int iput_quick_common(const MIR * mir, bool isObj) {
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB; //object
    u2 fieldByteOffset = mir->dalvikInsn.vC;

    // Request VR delay before transfer to temporary. Only vB needs delay.
    // vA will have non-zero reference count since transfer to temporary for
    // it happens after null check, thus no delay is needed.
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        requestVRFreeDelay(vB,VRDELAY_NULLCHECK);
    }

    get_virtual_reg(vB, OpndSize_32, 1, false);

    //If we can't ignore the NULL check
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        nullCheck(1, false, 1, vB); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vB,VRDELAY_NULLCHECK);
    }

    get_virtual_reg(vA, OpndSize_32, 2, false);
    move_reg_to_mem(OpndSize_32, 2, false, fieldByteOffset, 1, false);
    if(isObj) {
        markCard(2/*valReg*/, 1, false, 11, false);
    }
    return 0;
}
/**
 * @brief Generate native code for bytecode
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_quick(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_QUICK);
    return iput_quick_common(mir, false /*isObj*/);
}
#undef P_GPR_1
#undef P_GPR_2
#define P_GPR_1 PhysicalReg_EBX

/**
 * @brief Generate native code for bytecode
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_wide_quick(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_WIDE_QUICK);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB; //object
    u2 fieldByteOffset = mir->dalvikInsn.vC;

    // Request VR delay before transfer to temporary. Only vB needs delay.
    // vA will have non-zero reference count since transfer to temporary for
    // it happens after null check, thus no delay is needed.
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        requestVRFreeDelay(vB,VRDELAY_NULLCHECK);
    }

    get_virtual_reg(vB, OpndSize_32, 1, false);

    //If we can't ignore the NULL check
    if((mir->OptimizationFlags & MIR_IGNORE_NULL_CHECK) == 0)
    {
        nullCheck(1, false, 1, vB); //maybe optimized away, if not, call
        cancelVRFreeDelayRequest(vB,VRDELAY_NULLCHECK);
    }

    get_virtual_reg(vA, OpndSize_64, 1, false);
    move_reg_to_mem(OpndSize_64, 1, false, fieldByteOffset, 1, false);
    return 0;
}
#undef P_GPR_1

/**
 * @brief Generate native code for bytecode
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_iput_object_quick(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IPUT_OBJECT_QUICK);
    return iput_quick_common(mir, true /*isObj*/);
}


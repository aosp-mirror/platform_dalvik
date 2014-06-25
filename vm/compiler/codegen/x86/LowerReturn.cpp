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


/*! \file LowerReturn.cpp
    \brief This file lowers the following bytecodes: RETURN

*/
#include "libdex/DexOpcodes.h"
#include "libdex/DexFile.h"
#include "mterp/Mterp.h"
#include "Lower.h"
#include "enc_wrapper.h"
#include "NcgHelper.h"

//4 GPRs and scratch registers used in get_self_pointer, set_glue_method and set_glue_dvmdex
//will jump to "gotoBail" if caller method is NULL or if debugger is active
//what is %edx for each case? for the latter case, it is 1
#define P_GPR_1 PhysicalReg_ECX //must be ecx
#define P_GPR_2 PhysicalReg_EBX
#define P_SCRATCH_1 PhysicalReg_EDX
#define P_OLD_FP PhysicalReg_EAX
/*!
\brief common section to return from a method

If the helper switch is on, this will generate a helper function
*/
int common_returnFromMethod() {
#if defined(WITH_SELF_VERIFICATION)
    constVREndOfBB();
#endif
#if defined(ENABLE_TRACING) && !defined(TRACING_OPTION2)
    insertMapWorklist(offsetPC, mapFromBCtoNCG[offsetPC], 1); //check when helper switch is on
#endif
#ifdef INC_NCG_O0
    ExecutionMode origMode = gDvm.executionMode;
    if(gDvm.helper_switch[0]) {
        insertLabel(".returnFromMethod", false);
        origMode = gDvm.executionMode;
        gDvm.executionMode = kExecutionModeNcgO0;
    }
#endif

#if !defined(WITH_JIT)
    get_self_pointer(2, false);
#if defined(ENABLE_TRACING)
    move_mem_to_reg(OpndSize_32, offGlue_pIntoDebugger, PhysicalReg_Glue, true, 4, false);
#endif

    compare_imm_mem(OpndSize_32, 0, offsetof(Thread, suspendCount), 2, false); //suspendCount
    conditional_jump(Condition_NE, "common_handleSuspend2", true); //called once

    rememberState(1);
#if defined(ENABLE_TRACING)
    compare_imm_mem(OpndSize_32, 0, 0, 4, false);
    conditional_jump(Condition_NE, "common_debuggerActive2", true);
#endif
    rememberState(2);
    unconditional_jump("backto_return", true);

    insertLabel("common_handleSuspend2", true);
    goToState(1);
    move_mem_to_reg(OpndSize_32, offEBP_self, PhysicalReg_EBP, true, 5, false);
    load_effective_addr(-4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    move_reg_to_mem(OpndSize_32, 5, false, 0, PhysicalReg_ESP, true);
    scratchRegs[0] = PhysicalReg_SCRATCH_1;
    call_dvmCheckSuspendPending();
    load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    transferToState(2);
    unconditional_jump("backto_return", true);

    insertLabel("common_debuggerActive2", true);
#ifdef ENABLE_TRACING
    move_imm_to_reg(OpndSize_32, 2*offsetPC, PhysicalReg_EDX, true);
    //%edx: offsetBC (at run time, get method->insns_bytecode, then calculate BCPointer)
    move_mem_to_reg(OpndSize_32, offGlue_method, PhysicalReg_Glue, true, 15, false);
    move_mem_to_reg(OpndSize_32, offMethod_insns_bytecode, 15, false, 16, false);
    alu_binary_reg_reg(OpndSize_32, add_opc, 16, false, PhysicalReg_EDX, true);
    move_imm_to_mem(OpndSize_32, 0, offGlue_entryPoint, PhysicalReg_Glue, true);//kInterpEntryReturn);
    unconditional_jump("common_gotoBail", false); //update glue->rPC with edx
#endif
    insertLabel("backto_return", true);
    goToState(2);
#endif //not WITH_JIT

    scratchRegs[0] = PhysicalReg_SCRATCH_7;
#ifdef WITH_JIT
    get_self_pointer(2, false);
#endif

    //update rFP to caller stack frame
    move_reg_to_reg(OpndSize_32, PhysicalReg_FP, true, 10, false);
    move_mem_to_reg(OpndSize_32, -sizeofStackSaveArea+offStackSaveArea_prevFrame, PhysicalReg_FP, true, PhysicalReg_FP, true); //update rFP
    //get caller method by accessing the stack save area
    move_mem_to_reg(OpndSize_32, -sizeofStackSaveArea+offStackSaveArea_method, PhysicalReg_FP, true, 6, false);
    compare_imm_reg(OpndSize_32, 0, 6, false);
    conditional_jump(Condition_E, "common_gotoBail_0", false);
    get_self_pointer(3, false);
    //update glue->method
    move_reg_to_mem(OpndSize_32, 6, false, offsetof(Thread, interpSave.method), 2, false);
#ifdef INC_NCG_O0
    test_imm_mem(OpndSize_32, ACC_NCG, offMethod_accessFlags, 6, false);
#endif
    //get clazz of caller method
    move_mem_to_reg(OpndSize_32, offMethod_clazz, 6, false, 14, false);
    //update self->frame
    move_reg_to_mem(OpndSize_32, PhysicalReg_FP, true, offThread_curFrame, 3, false);
    //get method->clazz->pDvmDex
    move_mem_to_reg(OpndSize_32, offClassObject_pDvmDex, 14, false, 7, false);
    move_reg_to_mem(OpndSize_32, 7, false, offsetof(Thread, interpSave.methodClassDex), 2, false);

#if defined(WITH_JIT)
    compare_imm_mem(OpndSize_32, 0, offsetof(Thread, suspendCount), 2, false); /* suspendCount */
    move_mem_to_reg(OpndSize_32, -sizeofStackSaveArea+offStackSaveArea_returnAddr, 10, false, PhysicalReg_EBX, true);
    move_imm_to_reg(OpndSize_32, 0, 17, false);
    /* if suspendCount is not zero, clear the chaining cell address */
    conditional_move_reg_to_reg(OpndSize_32, Condition_NZ, 17, false/*src*/, PhysicalReg_EBX, true/*dst*/);
#endif
    move_mem_to_reg(OpndSize_32, -sizeofStackSaveArea+offStackSaveArea_savedPc, 10, false, PhysicalReg_EAX, true);
#if defined(WITH_JIT)
    //if returnAddr is not NULL, the thread is still in code cache
    move_reg_to_mem(OpndSize_32, PhysicalReg_EBX, true, offThread_inJitCodeCache, 3, false);
#endif
    /////////////////////////////////
#ifdef INC_NCG_O0
    //check type of the caller, if it is interpreter, jump back to interpreter
    //the caller should not be a native method
    conditional_jump(Condition_E, ".LreturnToInterp", true);
    alu_binary_imm_reg(OpndSize_32, add_opc, NCG_OFF_FROM_SAVEDPC, PhysicalReg_EAX, true);
#ifdef DEBUG_CALL_STACK
    scratchRegs[0] = PhysicalReg_SCRATCH_5;
    invokeNcg(true); //use %eax
#else
    unconditional_jump_reg(PhysicalReg_EAX, true);
#endif
#endif //not WITH_JIT
    ///////////////////////////////////
    insertLabel(".LreturnToInterp", true); //local label
    //move rPC by 6 (3 bytecode units for INVOKE)
    alu_binary_imm_reg(OpndSize_32, add_opc, 6, PhysicalReg_EAX, true);

#if defined(WITH_JIT)
    //returnAddr in %ebx, if not zero, jump to returnAddr
    compare_imm_reg(OpndSize_32, 0, PhysicalReg_EBX, true);
    conditional_jump(Condition_E, ".LcontinueToInterp", true);
#ifdef DEBUG_CALL_STACK3
    move_reg_to_reg(OpndSize_32, PhysicalReg_EBX, true, PhysicalReg_ESI, true);
    move_imm_to_reg(OpndSize_32, 0xaabb, PhysicalReg_EBX, true);
    scratchRegs[0] = PhysicalReg_EAX;
    call_debug_dumpSwitch(); //%ebx, %eax, %edx
    move_reg_to_reg(OpndSize_32, PhysicalReg_ESI, true, PhysicalReg_EBX, true);
    call_debug_dumpSwitch();
    move_reg_to_reg(OpndSize_32, PhysicalReg_ESI, true, PhysicalReg_EBX, true);
#endif
    unconditional_jump_reg(PhysicalReg_EBX, true);
    insertLabel(".LcontinueToInterp", true);
#endif
    scratchRegs[0] = PhysicalReg_SCRATCH_4;
    /* "IA_NCG=true WITH_JIT=false":
       call to dvmNcgInvokeInterpreter needs to be relocated
       comparing to dvmJitToInterpNoChain,
         dvmNcgInvokeInterpreter does not check dvmJitGetCodeAddr,
         does not check whether to start a trace or not
       We will optimize the case from NCG to JIT'ed code later on */
#if !defined(WITH_JIT)
    /* invokeInterpreter uses %eax as input
       touches %eax once afterwards to reduce the reference count to 0
       uses scratch C_SCRATCH_1 */
    invokeInterpreter(true);
#else
    typedef void (*vmHelper)(int);
    vmHelper funcPtr = dvmJitToInterpNoChainNoProfile; //%eax is the input
    move_imm_to_reg(OpndSize_32, (int)funcPtr, C_SCRATCH_1, isScratchPhysical);
#if defined(WITH_JIT_TUNING)
    /* Return address not in code cache. Indicate that continuing with interpreter.
     */
    move_imm_to_mem(OpndSize_32, kCallsiteInterpreted, 0, PhysicalReg_ESP, true);
#endif
    unconditional_jump_reg(C_SCRATCH_1, isScratchPhysical);
    touchEax();
#endif
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[0]) {
        gDvm.executionMode = origMode;
    }
#endif
    return 0;
}
#undef P_GPR_1
#undef P_GPR_2
#undef P_SCRATCH_1

/**
 * @brief Generate native code for bytecodes return-void
 * and return-void-barrier
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_return_void(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_RETURN_VOID
            || mir->dalvikInsn.opcode == OP_RETURN_VOID_BARRIER);
    int retval;
#ifdef INC_NCG_O0
    if(gDvm.helper_switch[0]) {
        retval = unconditional_jump_global_API(, ".returnFromMethod", false);

    }
    else
#endif
    {
        retval = common_returnFromMethod();
    }
    return retval;
}

/**
 * @brief Generate native code for bytecodes return
 * and return-object
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_return(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_RETURN
            || mir->dalvikInsn.opcode == OP_RETURN_OBJECT);
    u2 vA = mir->dalvikInsn.vA;
    get_virtual_reg(vA, OpndSize_32, 22, false);
    scratchRegs[0] = PhysicalReg_SCRATCH_1;
    set_return_value(OpndSize_32, 22, false);

#ifdef INC_NCG_O0
    if(gDvm.helper_switch[0]) {
        unconditional_jump_global_API(".returnFromMethod", false);

    }
    else
#endif
    {
        common_returnFromMethod();
    }

    return 0;
}

/**
 * @brief Generate native code for bytecode return-wide
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_return_wide(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_RETURN_WIDE);
    u2 vA = mir->dalvikInsn.vA;
    get_virtual_reg(vA, OpndSize_64, 1, false);
    scratchRegs[0] = PhysicalReg_SCRATCH_10; scratchRegs[1] = PhysicalReg_Null;
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;
    set_return_value(OpndSize_64, 1, false);

#ifdef INC_NCG_O0
    if(gDvm.helper_switch[0]) {
        unconditional_jump_global_API(".returnFromMethod", false);

    }
    else
#endif
    {
        common_returnFromMethod();
    }
    return 0;
}

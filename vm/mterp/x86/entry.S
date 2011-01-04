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


    .text
    .global dvmMterpStdRun
    .type   dvmMterpStdRun, %function
/*
 * bool dvmMterpStdRun(MterpGlue* glue)
 *
 * Interpreter entry point.  Returns changeInterp.
 *
 */
dvmMterpStdRun:
    movl    4(%esp), %ecx        # get incoming rGLUE
    push    %ebp                 # save caller base pointer
    push    %ecx                 # save rGLUE at (%ebp)
    movl    %esp, %ebp           # set our %ebp
/*
 * At this point we've allocated two slots on the stack
 * via push and stack is 8-byte aligned.  Allocate space
 * for 8 spill slots, 3 local slots, 5 arg slots + 2 slots for
 * padding to bring us to 16-byte alignment
 */
    subl    $$(FRAME_SIZE-8), %esp

/* Spill callee save regs */
    movl    %edi,EDI_SPILL(%ebp)
    movl    %esi,ESI_SPILL(%ebp)
    movl    %ebx,EBX_SPILL(%ebp)

/* Set up "named" registers */
    movl    offGlue_pc(%ecx),rPC
    movl    offGlue_fp(%ecx),rFP

/* Remember %esp for future "longjmp" */
    movl    %esp,offGlue_bailPtr(%ecx)

/* How to start? */
    movb    offGlue_entryPoint(%ecx),%al

/* Normal start? */
    cmpb    $$kInterpEntryInstr,%al
    jne     .Lnot_instr

   /* Normal case: start executing the instruction at rPC */
    FETCH_INST
    GOTO_NEXT

.Lnot_instr:
    /* Reset to normal case */
    movb   $$kInterpEntryInstr,offGlue_entryPoint(%ecx)
    cmpb   $$kInterpEntryReturn,%al
    je     common_returnFromMethod
    cmpb   $$kInterpEntryThrow,%al
    je     common_exceptionThrown
    movzx  %al,%eax
    movl   %eax,OUT_ARG1(%esp)
    movl   $$.LstrBadEntryPoint,OUT_ARG0(%esp)
    call   printf
    call   dvmAbort
    /* Not reached */


    .global dvmMterpStdBail
    .type   dvmMterpStdBail, %function
/*
 * void dvmMterpStdBail(MterpGlue* glue, bool changeInterp)
 *
 * Restore the stack pointer and PC from the save point established on entry.
 * This is essentially the same as a longjmp, but should be cheaper.  The
 * last instruction causes us to return to whoever called dvmMterpStdRun.
 *
 * We're not going to build a standard frame here, so the arg accesses will
 * look a little strange.
 *
 * On entry:
 *  esp+4 (arg0)  MterpGlue* glue
 *  esp+8 (arg1)  bool changeInterp
 */
dvmMterpStdBail:
    movl    4(%esp),%ecx                 # grab glue
    movl    8(%esp),%eax                 # changeInterp to return reg
    movl    offGlue_bailPtr(%ecx),%esp   # Restore "setjmp" esp
    movl    %esp,%ebp
    addl    $$(FRAME_SIZE-8), %ebp       # Restore %ebp at point of setjmp
    movl    EDI_SPILL(%ebp),%edi
    movl    ESI_SPILL(%ebp),%esi
    movl    EBX_SPILL(%ebp),%ebx
    movl    PREV_FP(%ebp),%ebp           # restore caller's ebp
    addl    $$FRAME_SIZE,%esp                    # strip frame
    ret                                  # return to dvmMterpStdRun's caller


/*
 * Strings
 */
    .section    .rodata
.LstrBadEntryPoint:
    .asciz  "Bad entry point %d\n"


/*
 * FIXME: Should have the config/rebuild mechanism generate this
 * for targets that need it.
 */

/* Jump table */
dvmAsmInstructionJmpTable = .LdvmAsmInstructionJmpTable
.LdvmAsmInstructionJmpTable:
.long .L_OP_NOP
.long .L_OP_MOVE
.long .L_OP_MOVE_FROM16
.long .L_OP_MOVE_16
.long .L_OP_MOVE_WIDE
.long .L_OP_MOVE_WIDE_FROM16
.long .L_OP_MOVE_WIDE_16
.long .L_OP_MOVE_OBJECT
.long .L_OP_MOVE_OBJECT_FROM16
.long .L_OP_MOVE_OBJECT_16
.long .L_OP_MOVE_RESULT
.long .L_OP_MOVE_RESULT_WIDE
.long .L_OP_MOVE_RESULT_OBJECT
.long .L_OP_MOVE_EXCEPTION
.long .L_OP_RETURN_VOID
.long .L_OP_RETURN
.long .L_OP_RETURN_WIDE
.long .L_OP_RETURN_OBJECT
.long .L_OP_CONST_4
.long .L_OP_CONST_16
.long .L_OP_CONST
.long .L_OP_CONST_HIGH16
.long .L_OP_CONST_WIDE_16
.long .L_OP_CONST_WIDE_32
.long .L_OP_CONST_WIDE
.long .L_OP_CONST_WIDE_HIGH16
.long .L_OP_CONST_STRING
.long .L_OP_CONST_STRING_JUMBO
.long .L_OP_CONST_CLASS
.long .L_OP_MONITOR_ENTER
.long .L_OP_MONITOR_EXIT
.long .L_OP_CHECK_CAST
.long .L_OP_INSTANCE_OF
.long .L_OP_ARRAY_LENGTH
.long .L_OP_NEW_INSTANCE
.long .L_OP_NEW_ARRAY
.long .L_OP_FILLED_NEW_ARRAY
.long .L_OP_FILLED_NEW_ARRAY_RANGE
.long .L_OP_FILL_ARRAY_DATA
.long .L_OP_THROW
.long .L_OP_GOTO
.long .L_OP_GOTO_16
.long .L_OP_GOTO_32
.long .L_OP_PACKED_SWITCH
.long .L_OP_SPARSE_SWITCH
.long .L_OP_CMPL_FLOAT
.long .L_OP_CMPG_FLOAT
.long .L_OP_CMPL_DOUBLE
.long .L_OP_CMPG_DOUBLE
.long .L_OP_CMP_LONG
.long .L_OP_IF_EQ
.long .L_OP_IF_NE
.long .L_OP_IF_LT
.long .L_OP_IF_GE
.long .L_OP_IF_GT
.long .L_OP_IF_LE
.long .L_OP_IF_EQZ
.long .L_OP_IF_NEZ
.long .L_OP_IF_LTZ
.long .L_OP_IF_GEZ
.long .L_OP_IF_GTZ
.long .L_OP_IF_LEZ
.long .L_OP_UNUSED_3E
.long .L_OP_UNUSED_3F
.long .L_OP_UNUSED_40
.long .L_OP_UNUSED_41
.long .L_OP_UNUSED_42
.long .L_OP_UNUSED_43
.long .L_OP_AGET
.long .L_OP_AGET_WIDE
.long .L_OP_AGET_OBJECT
.long .L_OP_AGET_BOOLEAN
.long .L_OP_AGET_BYTE
.long .L_OP_AGET_CHAR
.long .L_OP_AGET_SHORT
.long .L_OP_APUT
.long .L_OP_APUT_WIDE
.long .L_OP_APUT_OBJECT
.long .L_OP_APUT_BOOLEAN
.long .L_OP_APUT_BYTE
.long .L_OP_APUT_CHAR
.long .L_OP_APUT_SHORT
.long .L_OP_IGET
.long .L_OP_IGET_WIDE
.long .L_OP_IGET_OBJECT
.long .L_OP_IGET_BOOLEAN
.long .L_OP_IGET_BYTE
.long .L_OP_IGET_CHAR
.long .L_OP_IGET_SHORT
.long .L_OP_IPUT
.long .L_OP_IPUT_WIDE
.long .L_OP_IPUT_OBJECT
.long .L_OP_IPUT_BOOLEAN
.long .L_OP_IPUT_BYTE
.long .L_OP_IPUT_CHAR
.long .L_OP_IPUT_SHORT
.long .L_OP_SGET
.long .L_OP_SGET_WIDE
.long .L_OP_SGET_OBJECT
.long .L_OP_SGET_BOOLEAN
.long .L_OP_SGET_BYTE
.long .L_OP_SGET_CHAR
.long .L_OP_SGET_SHORT
.long .L_OP_SPUT
.long .L_OP_SPUT_WIDE
.long .L_OP_SPUT_OBJECT
.long .L_OP_SPUT_BOOLEAN
.long .L_OP_SPUT_BYTE
.long .L_OP_SPUT_CHAR
.long .L_OP_SPUT_SHORT
.long .L_OP_INVOKE_VIRTUAL
.long .L_OP_INVOKE_SUPER
.long .L_OP_INVOKE_DIRECT
.long .L_OP_INVOKE_STATIC
.long .L_OP_INVOKE_INTERFACE
.long .L_OP_UNUSED_73
.long .L_OP_INVOKE_VIRTUAL_RANGE
.long .L_OP_INVOKE_SUPER_RANGE
.long .L_OP_INVOKE_DIRECT_RANGE
.long .L_OP_INVOKE_STATIC_RANGE
.long .L_OP_INVOKE_INTERFACE_RANGE
.long .L_OP_UNUSED_79
.long .L_OP_UNUSED_7A
.long .L_OP_NEG_INT
.long .L_OP_NOT_INT
.long .L_OP_NEG_LONG
.long .L_OP_NOT_LONG
.long .L_OP_NEG_FLOAT
.long .L_OP_NEG_DOUBLE
.long .L_OP_INT_TO_LONG
.long .L_OP_INT_TO_FLOAT
.long .L_OP_INT_TO_DOUBLE
.long .L_OP_LONG_TO_INT
.long .L_OP_LONG_TO_FLOAT
.long .L_OP_LONG_TO_DOUBLE
.long .L_OP_FLOAT_TO_INT
.long .L_OP_FLOAT_TO_LONG
.long .L_OP_FLOAT_TO_DOUBLE
.long .L_OP_DOUBLE_TO_INT
.long .L_OP_DOUBLE_TO_LONG
.long .L_OP_DOUBLE_TO_FLOAT
.long .L_OP_INT_TO_BYTE
.long .L_OP_INT_TO_CHAR
.long .L_OP_INT_TO_SHORT
.long .L_OP_ADD_INT
.long .L_OP_SUB_INT
.long .L_OP_MUL_INT
.long .L_OP_DIV_INT
.long .L_OP_REM_INT
.long .L_OP_AND_INT
.long .L_OP_OR_INT
.long .L_OP_XOR_INT
.long .L_OP_SHL_INT
.long .L_OP_SHR_INT
.long .L_OP_USHR_INT
.long .L_OP_ADD_LONG
.long .L_OP_SUB_LONG
.long .L_OP_MUL_LONG
.long .L_OP_DIV_LONG
.long .L_OP_REM_LONG
.long .L_OP_AND_LONG
.long .L_OP_OR_LONG
.long .L_OP_XOR_LONG
.long .L_OP_SHL_LONG
.long .L_OP_SHR_LONG
.long .L_OP_USHR_LONG
.long .L_OP_ADD_FLOAT
.long .L_OP_SUB_FLOAT
.long .L_OP_MUL_FLOAT
.long .L_OP_DIV_FLOAT
.long .L_OP_REM_FLOAT
.long .L_OP_ADD_DOUBLE
.long .L_OP_SUB_DOUBLE
.long .L_OP_MUL_DOUBLE
.long .L_OP_DIV_DOUBLE
.long .L_OP_REM_DOUBLE
.long .L_OP_ADD_INT_2ADDR
.long .L_OP_SUB_INT_2ADDR
.long .L_OP_MUL_INT_2ADDR
.long .L_OP_DIV_INT_2ADDR
.long .L_OP_REM_INT_2ADDR
.long .L_OP_AND_INT_2ADDR
.long .L_OP_OR_INT_2ADDR
.long .L_OP_XOR_INT_2ADDR
.long .L_OP_SHL_INT_2ADDR
.long .L_OP_SHR_INT_2ADDR
.long .L_OP_USHR_INT_2ADDR
.long .L_OP_ADD_LONG_2ADDR
.long .L_OP_SUB_LONG_2ADDR
.long .L_OP_MUL_LONG_2ADDR
.long .L_OP_DIV_LONG_2ADDR
.long .L_OP_REM_LONG_2ADDR
.long .L_OP_AND_LONG_2ADDR
.long .L_OP_OR_LONG_2ADDR
.long .L_OP_XOR_LONG_2ADDR
.long .L_OP_SHL_LONG_2ADDR
.long .L_OP_SHR_LONG_2ADDR
.long .L_OP_USHR_LONG_2ADDR
.long .L_OP_ADD_FLOAT_2ADDR
.long .L_OP_SUB_FLOAT_2ADDR
.long .L_OP_MUL_FLOAT_2ADDR
.long .L_OP_DIV_FLOAT_2ADDR
.long .L_OP_REM_FLOAT_2ADDR
.long .L_OP_ADD_DOUBLE_2ADDR
.long .L_OP_SUB_DOUBLE_2ADDR
.long .L_OP_MUL_DOUBLE_2ADDR
.long .L_OP_DIV_DOUBLE_2ADDR
.long .L_OP_REM_DOUBLE_2ADDR
.long .L_OP_ADD_INT_LIT16
.long .L_OP_RSUB_INT
.long .L_OP_MUL_INT_LIT16
.long .L_OP_DIV_INT_LIT16
.long .L_OP_REM_INT_LIT16
.long .L_OP_AND_INT_LIT16
.long .L_OP_OR_INT_LIT16
.long .L_OP_XOR_INT_LIT16
.long .L_OP_ADD_INT_LIT8
.long .L_OP_RSUB_INT_LIT8
.long .L_OP_MUL_INT_LIT8
.long .L_OP_DIV_INT_LIT8
.long .L_OP_REM_INT_LIT8
.long .L_OP_AND_INT_LIT8
.long .L_OP_OR_INT_LIT8
.long .L_OP_XOR_INT_LIT8
.long .L_OP_SHL_INT_LIT8
.long .L_OP_SHR_INT_LIT8
.long .L_OP_USHR_INT_LIT8
.long .L_OP_IGET_VOLATILE
.long .L_OP_IPUT_VOLATILE
.long .L_OP_SGET_VOLATILE
.long .L_OP_SPUT_VOLATILE
.long .L_OP_IGET_OBJECT_VOLATILE
.long .L_OP_IGET_WIDE_VOLATILE
.long .L_OP_IPUT_WIDE_VOLATILE
.long .L_OP_SGET_WIDE_VOLATILE
.long .L_OP_SPUT_WIDE_VOLATILE
.long .L_OP_BREAKPOINT
.long .L_OP_THROW_VERIFICATION_ERROR
.long .L_OP_EXECUTE_INLINE
.long .L_OP_EXECUTE_INLINE_RANGE
.long .L_OP_INVOKE_DIRECT_EMPTY
.long .L_OP_RETURN_VOID_BARRIER
.long .L_OP_IGET_QUICK
.long .L_OP_IGET_WIDE_QUICK
.long .L_OP_IGET_OBJECT_QUICK
.long .L_OP_IPUT_QUICK
.long .L_OP_IPUT_WIDE_QUICK
.long .L_OP_IPUT_OBJECT_QUICK
.long .L_OP_INVOKE_VIRTUAL_QUICK
.long .L_OP_INVOKE_VIRTUAL_QUICK_RANGE
.long .L_OP_INVOKE_SUPER_QUICK
.long .L_OP_INVOKE_SUPER_QUICK_RANGE
.long .L_OP_IPUT_OBJECT_VOLATILE
.long .L_OP_SGET_OBJECT_VOLATILE
.long .L_OP_SPUT_OBJECT_VOLATILE
.long .L_OP_DISPATCH_FF
.long .L_OP_CONST_CLASS_JUMBO
.long .L_OP_CHECK_CAST_JUMBO
.long .L_OP_INSTANCE_OF_JUMBO
.long .L_OP_NEW_INSTANCE_JUMBO
.long .L_OP_NEW_ARRAY_JUMBO
.long .L_OP_FILLED_NEW_ARRAY_JUMBO
.long .L_OP_IGET_JUMBO
.long .L_OP_IGET_WIDE_JUMBO
.long .L_OP_IGET_OBJECT_JUMBO
.long .L_OP_IGET_BOOLEAN_JUMBO
.long .L_OP_IGET_BYTE_JUMBO
.long .L_OP_IGET_CHAR_JUMBO
.long .L_OP_IGET_SHORT_JUMBO
.long .L_OP_IPUT_JUMBO
.long .L_OP_IPUT_WIDE_JUMBO
.long .L_OP_IPUT_OBJECT_JUMBO
.long .L_OP_IPUT_BOOLEAN_JUMBO
.long .L_OP_IPUT_BYTE_JUMBO
.long .L_OP_IPUT_CHAR_JUMBO
.long .L_OP_IPUT_SHORT_JUMBO
.long .L_OP_SGET_JUMBO
.long .L_OP_SGET_WIDE_JUMBO
.long .L_OP_SGET_OBJECT_JUMBO
.long .L_OP_SGET_BOOLEAN_JUMBO
.long .L_OP_SGET_BYTE_JUMBO
.long .L_OP_SGET_CHAR_JUMBO
.long .L_OP_SGET_SHORT_JUMBO
.long .L_OP_SPUT_JUMBO
.long .L_OP_SPUT_WIDE_JUMBO
.long .L_OP_SPUT_OBJECT_JUMBO
.long .L_OP_SPUT_BOOLEAN_JUMBO
.long .L_OP_SPUT_BYTE_JUMBO
.long .L_OP_SPUT_CHAR_JUMBO
.long .L_OP_SPUT_SHORT_JUMBO
.long .L_OP_INVOKE_VIRTUAL_JUMBO
.long .L_OP_INVOKE_SUPER_JUMBO
.long .L_OP_INVOKE_DIRECT_JUMBO
.long .L_OP_INVOKE_STATIC_JUMBO
.long .L_OP_INVOKE_INTERFACE_JUMBO
.long .L_OP_UNUSED_27FF
.long .L_OP_UNUSED_28FF
.long .L_OP_UNUSED_29FF
.long .L_OP_UNUSED_2AFF
.long .L_OP_UNUSED_2BFF
.long .L_OP_UNUSED_2CFF
.long .L_OP_UNUSED_2DFF
.long .L_OP_UNUSED_2EFF
.long .L_OP_UNUSED_2FFF
.long .L_OP_UNUSED_30FF
.long .L_OP_UNUSED_31FF
.long .L_OP_UNUSED_32FF
.long .L_OP_UNUSED_33FF
.long .L_OP_UNUSED_34FF
.long .L_OP_UNUSED_35FF
.long .L_OP_UNUSED_36FF
.long .L_OP_UNUSED_37FF
.long .L_OP_UNUSED_38FF
.long .L_OP_UNUSED_39FF
.long .L_OP_UNUSED_3AFF
.long .L_OP_UNUSED_3BFF
.long .L_OP_UNUSED_3CFF
.long .L_OP_UNUSED_3DFF
.long .L_OP_UNUSED_3EFF
.long .L_OP_UNUSED_3FFF
.long .L_OP_UNUSED_40FF
.long .L_OP_UNUSED_41FF
.long .L_OP_UNUSED_42FF
.long .L_OP_UNUSED_43FF
.long .L_OP_UNUSED_44FF
.long .L_OP_UNUSED_45FF
.long .L_OP_UNUSED_46FF
.long .L_OP_UNUSED_47FF
.long .L_OP_UNUSED_48FF
.long .L_OP_UNUSED_49FF
.long .L_OP_UNUSED_4AFF
.long .L_OP_UNUSED_4BFF
.long .L_OP_UNUSED_4CFF
.long .L_OP_UNUSED_4DFF
.long .L_OP_UNUSED_4EFF
.long .L_OP_UNUSED_4FFF
.long .L_OP_UNUSED_50FF
.long .L_OP_UNUSED_51FF
.long .L_OP_UNUSED_52FF
.long .L_OP_UNUSED_53FF
.long .L_OP_UNUSED_54FF
.long .L_OP_UNUSED_55FF
.long .L_OP_UNUSED_56FF
.long .L_OP_UNUSED_57FF
.long .L_OP_UNUSED_58FF
.long .L_OP_UNUSED_59FF
.long .L_OP_UNUSED_5AFF
.long .L_OP_UNUSED_5BFF
.long .L_OP_UNUSED_5CFF
.long .L_OP_UNUSED_5DFF
.long .L_OP_UNUSED_5EFF
.long .L_OP_UNUSED_5FFF
.long .L_OP_UNUSED_60FF
.long .L_OP_UNUSED_61FF
.long .L_OP_UNUSED_62FF
.long .L_OP_UNUSED_63FF
.long .L_OP_UNUSED_64FF
.long .L_OP_UNUSED_65FF
.long .L_OP_UNUSED_66FF
.long .L_OP_UNUSED_67FF
.long .L_OP_UNUSED_68FF
.long .L_OP_UNUSED_69FF
.long .L_OP_UNUSED_6AFF
.long .L_OP_UNUSED_6BFF
.long .L_OP_UNUSED_6CFF
.long .L_OP_UNUSED_6DFF
.long .L_OP_UNUSED_6EFF
.long .L_OP_UNUSED_6FFF
.long .L_OP_UNUSED_70FF
.long .L_OP_UNUSED_71FF
.long .L_OP_UNUSED_72FF
.long .L_OP_UNUSED_73FF
.long .L_OP_UNUSED_74FF
.long .L_OP_UNUSED_75FF
.long .L_OP_UNUSED_76FF
.long .L_OP_UNUSED_77FF
.long .L_OP_UNUSED_78FF
.long .L_OP_UNUSED_79FF
.long .L_OP_UNUSED_7AFF
.long .L_OP_UNUSED_7BFF
.long .L_OP_UNUSED_7CFF
.long .L_OP_UNUSED_7DFF
.long .L_OP_UNUSED_7EFF
.long .L_OP_UNUSED_7FFF
.long .L_OP_UNUSED_80FF
.long .L_OP_UNUSED_81FF
.long .L_OP_UNUSED_82FF
.long .L_OP_UNUSED_83FF
.long .L_OP_UNUSED_84FF
.long .L_OP_UNUSED_85FF
.long .L_OP_UNUSED_86FF
.long .L_OP_UNUSED_87FF
.long .L_OP_UNUSED_88FF
.long .L_OP_UNUSED_89FF
.long .L_OP_UNUSED_8AFF
.long .L_OP_UNUSED_8BFF
.long .L_OP_UNUSED_8CFF
.long .L_OP_UNUSED_8DFF
.long .L_OP_UNUSED_8EFF
.long .L_OP_UNUSED_8FFF
.long .L_OP_UNUSED_90FF
.long .L_OP_UNUSED_91FF
.long .L_OP_UNUSED_92FF
.long .L_OP_UNUSED_93FF
.long .L_OP_UNUSED_94FF
.long .L_OP_UNUSED_95FF
.long .L_OP_UNUSED_96FF
.long .L_OP_UNUSED_97FF
.long .L_OP_UNUSED_98FF
.long .L_OP_UNUSED_99FF
.long .L_OP_UNUSED_9AFF
.long .L_OP_UNUSED_9BFF
.long .L_OP_UNUSED_9CFF
.long .L_OP_UNUSED_9DFF
.long .L_OP_UNUSED_9EFF
.long .L_OP_UNUSED_9FFF
.long .L_OP_UNUSED_A0FF
.long .L_OP_UNUSED_A1FF
.long .L_OP_UNUSED_A2FF
.long .L_OP_UNUSED_A3FF
.long .L_OP_UNUSED_A4FF
.long .L_OP_UNUSED_A5FF
.long .L_OP_UNUSED_A6FF
.long .L_OP_UNUSED_A7FF
.long .L_OP_UNUSED_A8FF
.long .L_OP_UNUSED_A9FF
.long .L_OP_UNUSED_AAFF
.long .L_OP_UNUSED_ABFF
.long .L_OP_UNUSED_ACFF
.long .L_OP_UNUSED_ADFF
.long .L_OP_UNUSED_AEFF
.long .L_OP_UNUSED_AFFF
.long .L_OP_UNUSED_B0FF
.long .L_OP_UNUSED_B1FF
.long .L_OP_UNUSED_B2FF
.long .L_OP_UNUSED_B3FF
.long .L_OP_UNUSED_B4FF
.long .L_OP_UNUSED_B5FF
.long .L_OP_UNUSED_B6FF
.long .L_OP_UNUSED_B7FF
.long .L_OP_UNUSED_B8FF
.long .L_OP_UNUSED_B9FF
.long .L_OP_UNUSED_BAFF
.long .L_OP_UNUSED_BBFF
.long .L_OP_UNUSED_BCFF
.long .L_OP_UNUSED_BDFF
.long .L_OP_UNUSED_BEFF
.long .L_OP_UNUSED_BFFF
.long .L_OP_UNUSED_C0FF
.long .L_OP_UNUSED_C1FF
.long .L_OP_UNUSED_C2FF
.long .L_OP_UNUSED_C3FF
.long .L_OP_UNUSED_C4FF
.long .L_OP_UNUSED_C5FF
.long .L_OP_UNUSED_C6FF
.long .L_OP_UNUSED_C7FF
.long .L_OP_UNUSED_C8FF
.long .L_OP_UNUSED_C9FF
.long .L_OP_UNUSED_CAFF
.long .L_OP_UNUSED_CBFF
.long .L_OP_UNUSED_CCFF
.long .L_OP_UNUSED_CDFF
.long .L_OP_UNUSED_CEFF
.long .L_OP_UNUSED_CFFF
.long .L_OP_UNUSED_D0FF
.long .L_OP_UNUSED_D1FF
.long .L_OP_UNUSED_D2FF
.long .L_OP_UNUSED_D3FF
.long .L_OP_UNUSED_D4FF
.long .L_OP_UNUSED_D5FF
.long .L_OP_UNUSED_D6FF
.long .L_OP_UNUSED_D7FF
.long .L_OP_UNUSED_D8FF
.long .L_OP_UNUSED_D9FF
.long .L_OP_UNUSED_DAFF
.long .L_OP_UNUSED_DBFF
.long .L_OP_UNUSED_DCFF
.long .L_OP_UNUSED_DDFF
.long .L_OP_UNUSED_DEFF
.long .L_OP_UNUSED_DFFF
.long .L_OP_UNUSED_E0FF
.long .L_OP_UNUSED_E1FF
.long .L_OP_UNUSED_E2FF
.long .L_OP_UNUSED_E3FF
.long .L_OP_UNUSED_E4FF
.long .L_OP_UNUSED_E5FF
.long .L_OP_UNUSED_E6FF
.long .L_OP_UNUSED_E7FF
.long .L_OP_UNUSED_E8FF
.long .L_OP_UNUSED_E9FF
.long .L_OP_UNUSED_EAFF
.long .L_OP_UNUSED_EBFF
.long .L_OP_UNUSED_ECFF
.long .L_OP_UNUSED_EDFF
.long .L_OP_UNUSED_EEFF
.long .L_OP_UNUSED_EFFF
.long .L_OP_UNUSED_F0FF
.long .L_OP_UNUSED_F1FF
.long .L_OP_UNUSED_F2FF
.long .L_OP_UNUSED_F3FF
.long .L_OP_UNUSED_F4FF
.long .L_OP_UNUSED_F5FF
.long .L_OP_UNUSED_F6FF
.long .L_OP_UNUSED_F7FF
.long .L_OP_UNUSED_F8FF
.long .L_OP_UNUSED_F9FF
.long .L_OP_UNUSED_FAFF
.long .L_OP_UNUSED_FBFF
.long .L_OP_UNUSED_FCFF
.long .L_OP_UNUSED_FDFF
.long .L_OP_UNUSED_FEFF
.long .L_OP_THROW_VERIFICATION_ERROR_JUMBO

/*
 * Copyright (C) 2009 The Android Open Source Project
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
#include "Dataflow.h"
#include "Loop.h"
#include "libdex/DexOpcodes.h"

/*
 * Main table containing data flow attributes for each bytecode. The
 * first kNumPackedOpcodes entries are for Dalvik bytecode
 * instructions, where extended opcode at the MIR level are appended
 * afterwards.
 *
 * TODO - many optimization flags are incomplete - they will only limit the
 * scope of optimizations but will not cause mis-optimizations.
 */
int dvmCompilerDataFlowAttributes[kMirOpLast] = {
    // 00 OP_NOP
    DF_NOP,

    // 01 OP_MOVE vA, vB
    DF_DA | DF_UB | DF_IS_MOVE,

    // 02 OP_MOVE_FROM16 vAA, vBBBB
    DF_DA | DF_UB | DF_IS_MOVE,

    // 03 OP_MOVE_16 vAAAA, vBBBB
    DF_DA | DF_UB | DF_IS_MOVE,

    // 04 OP_MOVE_WIDE vA, vB
    DF_DA_WIDE | DF_UB_WIDE | DF_IS_MOVE,

    // 05 OP_MOVE_WIDE_FROM16 vAA, vBBBB
    DF_DA_WIDE | DF_UB_WIDE | DF_IS_MOVE,

    // 06 OP_MOVE_WIDE_16 vAAAA, vBBBB
    DF_DA_WIDE | DF_UB_WIDE | DF_IS_MOVE,

    // 07 OP_MOVE_OBJECT vA, vB
    DF_DA | DF_UB | DF_IS_MOVE,

    // 08 OP_MOVE_OBJECT_FROM16 vAA, vBBBB
    DF_DA | DF_UB | DF_IS_MOVE,

    // 09 OP_MOVE_OBJECT_16 vAAAA, vBBBB
    DF_DA | DF_UB | DF_IS_MOVE,

    // 0A OP_MOVE_RESULT vAA
    DF_DA,

    // 0B OP_MOVE_RESULT_WIDE vAA
    DF_DA_WIDE,

    // 0C OP_MOVE_RESULT_OBJECT vAA
    DF_DA,

    // 0D OP_MOVE_EXCEPTION vAA
    DF_DA,

    // 0E OP_RETURN_VOID
    DF_NOP,

    // 0F OP_RETURN vAA
    DF_UA,

    // 10 OP_RETURN_WIDE vAA
    DF_UA_WIDE,

    // 11 OP_RETURN_OBJECT vAA
    DF_UA,

    // 12 OP_CONST_4 vA, #+B
    DF_DA | DF_SETS_CONST,

    // 13 OP_CONST_16 vAA, #+BBBB
    DF_DA | DF_SETS_CONST,

    // 14 OP_CONST vAA, #+BBBBBBBB
    DF_DA | DF_SETS_CONST,

    // 15 OP_CONST_HIGH16 VAA, #+BBBB0000
    DF_DA | DF_SETS_CONST,

    // 16 OP_CONST_WIDE_16 vAA, #+BBBB
    DF_DA_WIDE | DF_SETS_CONST,

    // 17 OP_CONST_WIDE_32 vAA, #+BBBBBBBB
    DF_DA_WIDE | DF_SETS_CONST,

    // 18 OP_CONST_WIDE vAA, #+BBBBBBBBBBBBBBBB
    DF_DA_WIDE | DF_SETS_CONST,

    // 19 OP_CONST_WIDE_HIGH16 vAA, #+BBBB000000000000
    DF_DA_WIDE | DF_SETS_CONST,

    // 1A OP_CONST_STRING vAA, string@BBBB
    DF_DA,

    // 1B OP_CONST_STRING_JUMBO vAA, string@BBBBBBBB
    DF_DA,

    // 1C OP_CONST_CLASS vAA, type@BBBB
    DF_DA,

    // 1D OP_MONITOR_ENTER vAA
    DF_UA,

    // 1E OP_MONITOR_EXIT vAA
    DF_UA,

    // 1F OP_CHECK_CAST vAA, type@BBBB
    DF_UA,

    // 20 OP_INSTANCE_OF vA, vB, type@CCCC
    DF_DA | DF_UB,

    // 21 OP_ARRAY_LENGTH vA, vB
    DF_DA | DF_UB,

    // 22 OP_NEW_INSTANCE vAA, type@BBBB
    DF_DA,

    // 23 OP_NEW_ARRAY vA, vB, type@CCCC
    DF_DA | DF_UB,

    // 24 OP_FILLED_NEW_ARRAY {vD, vE, vF, vG, vA}
    DF_FORMAT_35C,

    // 25 OP_FILLED_NEW_ARRAY_RANGE {vCCCC .. vNNNN}, type@BBBB
    DF_FORMAT_3RC,

    // 26 OP_FILL_ARRAY_DATA vAA, +BBBBBBBB
    DF_UA,

    // 27 OP_THROW vAA
    DF_UA,

    // 28 OP_GOTO
    DF_NOP,

    // 29 OP_GOTO_16
    DF_NOP,

    // 2A OP_GOTO_32
    DF_NOP,

    // 2B OP_PACKED_SWITCH vAA, +BBBBBBBB
    DF_UA,

    // 2C OP_SPARSE_SWITCH vAA, +BBBBBBBB
    DF_UA,

    // 2D OP_CMPL_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_B | DF_FP_C,

    // 2E OP_CMPG_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_B | DF_FP_C,

    // 2F OP_CMPL_DOUBLE vAA, vBB, vCC
    DF_DA | DF_UB_WIDE | DF_UC_WIDE | DF_FP_B | DF_FP_C,

    // 30 OP_CMPG_DOUBLE vAA, vBB, vCC
    DF_DA | DF_UB_WIDE | DF_UC_WIDE | DF_FP_B | DF_FP_C,

    // 31 OP_CMP_LONG vAA, vBB, vCC
    DF_DA | DF_UB_WIDE | DF_UC_WIDE,

    // 32 OP_IF_EQ vA, vB, +CCCC
    DF_UA | DF_UB,

    // 33 OP_IF_NE vA, vB, +CCCC
    DF_UA | DF_UB,

    // 34 OP_IF_LT vA, vB, +CCCC
    DF_UA | DF_UB,

    // 35 OP_IF_GE vA, vB, +CCCC
    DF_UA | DF_UB,

    // 36 OP_IF_GT vA, vB, +CCCC
    DF_UA | DF_UB,

    // 37 OP_IF_LE vA, vB, +CCCC
    DF_UA | DF_UB,


    // 38 OP_IF_EQZ vAA, +BBBB
    DF_UA,

    // 39 OP_IF_NEZ vAA, +BBBB
    DF_UA,

    // 3A OP_IF_LTZ vAA, +BBBB
    DF_UA,

    // 3B OP_IF_GEZ vAA, +BBBB
    DF_UA,

    // 3C OP_IF_GTZ vAA, +BBBB
    DF_UA,

    // 3D OP_IF_LEZ vAA, +BBBB
    DF_UA,

    // 3E OP_UNUSED_3E
    DF_NOP,

    // 3F OP_UNUSED_3F
    DF_NOP,

    // 40 OP_UNUSED_40
    DF_NOP,

    // 41 OP_UNUSED_41
    DF_NOP,

    // 42 OP_UNUSED_42
    DF_NOP,

    // 43 OP_UNUSED_43
    DF_NOP,

    // 44 OP_AGET vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 45 OP_AGET_WIDE vAA, vBB, vCC
    DF_DA_WIDE | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 46 OP_AGET_OBJECT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 47 OP_AGET_BOOLEAN vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 48 OP_AGET_BYTE vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 49 OP_AGET_CHAR vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 4A OP_AGET_SHORT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_0 | DF_IS_GETTER,

    // 4B OP_APUT vAA, vBB, vCC
    DF_UA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_1 | DF_IS_SETTER,

    // 4C OP_APUT_WIDE vAA, vBB, vCC
    DF_UA_WIDE | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_2 | DF_IS_SETTER,

    // 4D OP_APUT_OBJECT vAA, vBB, vCC
    DF_UA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_1 | DF_IS_SETTER,

    // 4E OP_APUT_BOOLEAN vAA, vBB, vCC
    DF_UA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_1 | DF_IS_SETTER,

    // 4F OP_APUT_BYTE vAA, vBB, vCC
    DF_UA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_1 | DF_IS_SETTER,

    // 50 OP_APUT_CHAR vAA, vBB, vCC
    DF_UA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_1 | DF_IS_SETTER,

    // 51 OP_APUT_SHORT vAA, vBB, vCC
    DF_UA | DF_UB | DF_UC | DF_NULL_N_RANGE_CHECK_1 | DF_IS_SETTER,

    // 52 OP_IGET vA, vB, field@CCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 53 OP_IGET_WIDE vA, vB, field@CCCC
    DF_DA_WIDE | DF_UB | DF_IS_GETTER,

    // 54 OP_IGET_OBJECT vA, vB, field@CCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 55 OP_IGET_BOOLEAN vA, vB, field@CCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 56 OP_IGET_BYTE vA, vB, field@CCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 57 OP_IGET_CHAR vA, vB, field@CCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 58 OP_IGET_SHORT vA, vB, field@CCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 59 OP_IPUT vA, vB, field@CCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 5A OP_IPUT_WIDE vA, vB, field@CCCC
    DF_UA_WIDE | DF_UB | DF_IS_SETTER,

    // 5B OP_IPUT_OBJECT vA, vB, field@CCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 5C OP_IPUT_BOOLEAN vA, vB, field@CCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 5D OP_IPUT_BYTE vA, vB, field@CCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 5E OP_IPUT_CHAR vA, vB, field@CCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 5F OP_IPUT_SHORT vA, vB, field@CCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 60 OP_SGET vAA, field@BBBB
    DF_DA | DF_IS_GETTER,

    // 61 OP_SGET_WIDE vAA, field@BBBB
    DF_DA_WIDE | DF_IS_GETTER,

    // 62 OP_SGET_OBJECT vAA, field@BBBB
    DF_DA | DF_IS_GETTER,

    // 63 OP_SGET_BOOLEAN vAA, field@BBBB
    DF_DA | DF_IS_GETTER,

    // 64 OP_SGET_BYTE vAA, field@BBBB
    DF_DA | DF_IS_GETTER,

    // 65 OP_SGET_CHAR vAA, field@BBBB
    DF_DA | DF_IS_GETTER,

    // 66 OP_SGET_SHORT vAA, field@BBBB
    DF_DA | DF_IS_GETTER,

    // 67 OP_SPUT vAA, field@BBBB
    DF_UA | DF_IS_SETTER,

    // 68 OP_SPUT_WIDE vAA, field@BBBB
    DF_UA_WIDE | DF_IS_SETTER,

    // 69 OP_SPUT_OBJECT vAA, field@BBBB
    DF_UA | DF_IS_SETTER,

    // 6A OP_SPUT_BOOLEAN vAA, field@BBBB
    DF_UA | DF_IS_SETTER,

    // 6B OP_SPUT_BYTE vAA, field@BBBB
    DF_UA | DF_IS_SETTER,

    // 6C OP_SPUT_CHAR vAA, field@BBBB
    DF_UA | DF_IS_SETTER,

    // 6D OP_SPUT_SHORT vAA, field@BBBB
    DF_UA | DF_IS_SETTER,

    // 6E OP_INVOKE_VIRTUAL {vD, vE, vF, vG, vA}
    DF_FORMAT_35C,

    // 6F OP_INVOKE_SUPER {vD, vE, vF, vG, vA}
    DF_FORMAT_35C,

    // 70 OP_INVOKE_DIRECT {vD, vE, vF, vG, vA}
    DF_FORMAT_35C,

    // 71 OP_INVOKE_STATIC {vD, vE, vF, vG, vA}
    DF_FORMAT_35C,

    // 72 OP_INVOKE_INTERFACE {vD, vE, vF, vG, vA}
    DF_FORMAT_35C,

    // 73 OP_UNUSED_73
    DF_NOP,

    // 74 OP_INVOKE_VIRTUAL_RANGE {vCCCC .. vNNNN}
    DF_FORMAT_3RC,

    // 75 OP_INVOKE_SUPER_RANGE {vCCCC .. vNNNN}
    DF_FORMAT_3RC,

    // 76 OP_INVOKE_DIRECT_RANGE {vCCCC .. vNNNN}
    DF_FORMAT_3RC,

    // 77 OP_INVOKE_STATIC_RANGE {vCCCC .. vNNNN}
    DF_FORMAT_3RC,

    // 78 OP_INVOKE_INTERFACE_RANGE {vCCCC .. vNNNN}
    DF_FORMAT_3RC,

    // 79 OP_UNUSED_79
    DF_NOP,

    // 7A OP_UNUSED_7A
    DF_NOP,

    // 7B OP_NEG_INT vA, vB
    DF_DA | DF_UB,

    // 7C OP_NOT_INT vA, vB
    DF_DA | DF_UB,

    // 7D OP_NEG_LONG vA, vB
    DF_DA_WIDE | DF_UB_WIDE,

    // 7E OP_NOT_LONG vA, vB
    DF_DA_WIDE | DF_UB_WIDE,

    // 7F OP_NEG_FLOAT vA, vB
    DF_DA | DF_UB | DF_FP_A | DF_FP_B,

    // 80 OP_NEG_DOUBLE vA, vB
    DF_DA_WIDE | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // 81 OP_INT_TO_LONG vA, vB
    DF_DA_WIDE | DF_UB,

    // 82 OP_INT_TO_FLOAT vA, vB
    DF_DA | DF_UB | DF_FP_A,

    // 83 OP_INT_TO_DOUBLE vA, vB
    DF_DA_WIDE | DF_UB | DF_FP_A,

    // 84 OP_LONG_TO_INT vA, vB
    DF_DA | DF_UB_WIDE,

    // 85 OP_LONG_TO_FLOAT vA, vB
    DF_DA | DF_UB_WIDE | DF_FP_A,

    // 86 OP_LONG_TO_DOUBLE vA, vB
    DF_DA_WIDE | DF_UB_WIDE | DF_FP_A,

    // 87 OP_FLOAT_TO_INT vA, vB
    DF_DA | DF_UB | DF_FP_B,

    // 88 OP_FLOAT_TO_LONG vA, vB
    DF_DA_WIDE | DF_UB | DF_FP_B,

    // 89 OP_FLOAT_TO_DOUBLE vA, vB
    DF_DA_WIDE | DF_UB | DF_FP_A | DF_FP_B,

    // 8A OP_DOUBLE_TO_INT vA, vB
    DF_DA | DF_UB_WIDE | DF_FP_B,

    // 8B OP_DOUBLE_TO_LONG vA, vB
    DF_DA_WIDE | DF_UB_WIDE | DF_FP_B,

    // 8C OP_DOUBLE_TO_FLOAT vA, vB
    DF_DA | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // 8D OP_INT_TO_BYTE vA, vB
    DF_DA | DF_UB,

    // 8E OP_INT_TO_CHAR vA, vB
    DF_DA | DF_UB,

    // 8F OP_INT_TO_SHORT vA, vB
    DF_DA | DF_UB,

    // 90 OP_ADD_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_IS_LINEAR,

    // 91 OP_SUB_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_IS_LINEAR,

    // 92 OP_MUL_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 93 OP_DIV_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 94 OP_REM_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 95 OP_AND_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 96 OP_OR_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 97 OP_XOR_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 98 OP_SHL_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 99 OP_SHR_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 9A OP_USHR_INT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC,

    // 9B OP_ADD_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // 9C OP_SUB_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // 9D OP_MUL_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // 9E OP_DIV_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // 9F OP_REM_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // A0 OP_AND_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // A1 OP_OR_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // A2 OP_XOR_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE,

    // A3 OP_SHL_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC,

    // A4 OP_SHR_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC,

    // A5 OP_USHR_LONG vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC,

    // A6 OP_ADD_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_A | DF_FP_B | DF_FP_C,

    // A7 OP_SUB_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_A | DF_FP_B | DF_FP_C,

    // A8 OP_MUL_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_A | DF_FP_B | DF_FP_C,

    // A9 OP_DIV_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_A | DF_FP_B | DF_FP_C,

    // AA OP_REM_FLOAT vAA, vBB, vCC
    DF_DA | DF_UB | DF_UC | DF_FP_A | DF_FP_B | DF_FP_C,

    // AB OP_ADD_DOUBLE vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE | DF_FP_A | DF_FP_B | DF_FP_C,

    // AC OP_SUB_DOUBLE vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE | DF_FP_A | DF_FP_B | DF_FP_C,

    // AD OP_MUL_DOUBLE vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE | DF_FP_A | DF_FP_B | DF_FP_C,

    // AE OP_DIV_DOUBLE vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE | DF_FP_A | DF_FP_B | DF_FP_C,

    // AF OP_REM_DOUBLE vAA, vBB, vCC
    DF_DA_WIDE | DF_UB_WIDE | DF_UC_WIDE | DF_FP_A | DF_FP_B | DF_FP_C,

    // B0 OP_ADD_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B1 OP_SUB_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B2 OP_MUL_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B3 OP_DIV_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B4 OP_REM_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B5 OP_AND_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B6 OP_OR_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B7 OP_XOR_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B8 OP_SHL_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // B9 OP_SHR_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // BA OP_USHR_INT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB,

    // BB OP_ADD_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // BC OP_SUB_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // BD OP_MUL_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // BE OP_DIV_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // BF OP_REM_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // C0 OP_AND_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // C1 OP_OR_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // C2 OP_XOR_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE,

    // C3 OP_SHL_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB,

    // C4 OP_SHR_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB,

    // C5 OP_USHR_LONG_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB,

    // C6 OP_ADD_FLOAT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB | DF_FP_A | DF_FP_B,

    // C7 OP_SUB_FLOAT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB | DF_FP_A | DF_FP_B,

    // C8 OP_MUL_FLOAT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB | DF_FP_A | DF_FP_B,

    // C9 OP_DIV_FLOAT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB | DF_FP_A | DF_FP_B,

    // CA OP_REM_FLOAT_2ADDR vA, vB
    DF_DA | DF_UA | DF_UB | DF_FP_A | DF_FP_B,

    // CB OP_ADD_DOUBLE_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // CC OP_SUB_DOUBLE_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // CD OP_MUL_DOUBLE_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // CE OP_DIV_DOUBLE_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // CF OP_REM_DOUBLE_2ADDR vA, vB
    DF_DA_WIDE | DF_UA_WIDE | DF_UB_WIDE | DF_FP_A | DF_FP_B,

    // D0 OP_ADD_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D1 OP_RSUB_INT vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D2 OP_MUL_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D3 OP_DIV_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D4 OP_REM_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D5 OP_AND_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D6 OP_OR_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D7 OP_XOR_INT_LIT16 vA, vB, #+CCCC
    DF_DA | DF_UB,

    // D8 OP_ADD_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB | DF_IS_LINEAR,

    // D9 OP_RSUB_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // DA OP_MUL_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // DB OP_DIV_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // DC OP_REM_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // DD OP_AND_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // DE OP_OR_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // DF OP_XOR_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // E0 OP_SHL_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // E1 OP_SHR_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // E2 OP_USHR_INT_LIT8 vAA, vBB, #+CC
    DF_DA | DF_UB,

    // E3 OP_IGET_VOLATILE
    DF_DA | DF_UB,

    // E4 OP_IPUT_VOLATILE
    DF_UA | DF_UB,

    // E5 OP_SGET_VOLATILE
    DF_DA,

    // E6 OP_SPUT_VOLATILE
    DF_UA,

    // E7 OP_IGET_OBJECT_VOLATILE
    DF_DA | DF_UB,

    // E8 OP_IGET_WIDE_VOLATILE
    DF_DA_WIDE | DF_UB,

    // E9 OP_IPUT_WIDE_VOLATILE
    DF_UA_WIDE | DF_UB,

    // EA OP_SGET_WIDE_VOLATILE
    DF_DA_WIDE,

    // EB OP_SPUT_WIDE_VOLATILE
    DF_UA_WIDE,

    // EC OP_BREAKPOINT
    DF_NOP,

    // ED OP_THROW_VERIFICATION_ERROR
    DF_NOP,

    // EE OP_EXECUTE_INLINE
    DF_FORMAT_35C,

    // EF OP_EXECUTE_INLINE_RANGE
    DF_FORMAT_3RC,

    // F0 OP_INVOKE_OBJECT_INIT_RANGE
    DF_NOP,

    // F1 OP_RETURN_VOID_BARRIER
    DF_NOP,

    // F2 OP_IGET_QUICK
    DF_DA | DF_UB | DF_IS_GETTER,

    // F3 OP_IGET_WIDE_QUICK
    DF_DA_WIDE | DF_UB | DF_IS_GETTER,

    // F4 OP_IGET_OBJECT_QUICK
    DF_DA | DF_UB | DF_IS_GETTER,

    // F5 OP_IPUT_QUICK
    DF_UA | DF_UB | DF_IS_SETTER,

    // F6 OP_IPUT_WIDE_QUICK
    DF_UA_WIDE | DF_UB | DF_IS_SETTER,

    // F7 OP_IPUT_OBJECT_QUICK
    DF_UA | DF_UB | DF_IS_SETTER,

    // F8 OP_INVOKE_VIRTUAL_QUICK
    DF_FORMAT_35C,

    // F9 OP_INVOKE_VIRTUAL_QUICK_RANGE
    DF_FORMAT_3RC,

    // FA OP_INVOKE_SUPER_QUICK
    DF_FORMAT_35C,

    // FB OP_INVOKE_SUPER_QUICK_RANGE
    DF_FORMAT_3RC,

    // FC OP_IPUT_OBJECT_VOLATILE
    DF_UA | DF_UB,

    // FD OP_SGET_OBJECT_VOLATILE
    DF_DA,

    // FE OP_SPUT_OBJECT_VOLATILE
    DF_UA,

    // FF OP_DISPATCH_FF
    DF_NOP,

    // 100 OP_CONST_CLASS_JUMBO vAAAA, type@BBBBBBBB
    DF_DA,

    // 101 OP_CHECK_CAST_JUMBO vAAAA, type@BBBBBBBB
    DF_UA,

    // 102 OP_INSTANCE_OF_JUMBO vAAAA, vBBBB, type@CCCCCCCC
    DF_DA | DF_UB,

    // 103 OP_NEW_INSTANCE_JUMBO vAAAA, type@BBBBBBBB
    DF_DA,

    // 104 OP_NEW_ARRAY_JUMBO vAAAA, vBBBB, type@CCCCCCCC
    DF_DA | DF_UB,

    // 105 OP_FILLED_NEW_ARRAY_JUMBO {vCCCC .. vNNNN}, type@BBBBBBBB
    DF_FORMAT_3RC,

    // 106 OP_IGET_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 107 OP_IGET_WIDE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA_WIDE | DF_UB | DF_IS_GETTER,

    // 108 OP_IGET_OBJECT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 109 OP_IGET_BOOLEAN_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 10A OP_IGET_BYTE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 10B OP_IGET_CHAR_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 10C OP_IGET_SHORT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_UB | DF_IS_GETTER,

    // 10D OP_IPUT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 10E OP_IPUT_WIDE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA_WIDE | DF_UB | DF_IS_SETTER,

    // 10F OP_IPUT_OBJECT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 110 OP_IPUT_BOOLEAN_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 111 OP_IPUT_BYTE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 112 OP_IPUT_CHAR_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 113 OP_IPUT_SHORT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_UB | DF_IS_SETTER,

    // 114 OP_SGET_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_IS_GETTER,

    // 115 OP_SGET_WIDE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA_WIDE | DF_IS_GETTER,

    // 116 OP_SGET_OBJECT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_IS_GETTER,

    // 117 OP_SGET_BOOLEAN_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_IS_GETTER,

    // 118 OP_SGET_BYTE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_IS_GETTER,

    // 119 OP_SGET_CHAR_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_IS_GETTER,

    // 11A OP_SGET_SHORT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_DA | DF_IS_GETTER,

    // 11B OP_SPUT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_IS_SETTER,

    // 11C OP_SPUT_WIDE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA_WIDE | DF_IS_SETTER,

    // 11D OP_SPUT_OBJECT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_IS_SETTER,

    // 11E OP_SPUT_BOOLEAN_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_IS_SETTER,

    // 11F OP_SPUT_BYTE_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_IS_SETTER,

    // 120 OP_SPUT_CHAR_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_IS_SETTER,

    // 121 OP_SPUT_SHORT_JUMBO vAAAA, vBBBB, field@CCCCCCCC
    DF_UA | DF_IS_SETTER,

    // 122 OP_INVOKE_VIRTUAL_JUMBO {vCCCC .. vNNNN}, meth@BBBBBBBB
    DF_FORMAT_3RC,

    // 123 OP_INVOKE_SUPER_JUMBO {vCCCC .. vNNNN}, meth@BBBBBBBB
    DF_FORMAT_3RC,

    // 124 OP_INVOKE_DIRECT_JUMBO {vCCCC .. vNNNN}, meth@BBBBBBBB
    DF_FORMAT_3RC,

    // 125 OP_INVOKE_STATIC_JUMBO {vCCCC .. vNNNN}, meth@BBBBBBBB
    DF_FORMAT_3RC,

    // 126 OP_INVOKE_INTERFACE_JUMBO {vCCCC .. vNNNN}, meth@BBBBBBBB
    DF_FORMAT_3RC,

    // 127 OP_UNUSED_27FF
    DF_NOP,

    // 128 OP_UNUSED_28FF
    DF_NOP,

    // 129 OP_UNUSED_29FF
    DF_NOP,

    // 12A OP_UNUSED_2AFF
    DF_NOP,

    // 12B OP_UNUSED_2BFF
    DF_NOP,

    // 12C OP_UNUSED_2CFF
    DF_NOP,

    // 12D OP_UNUSED_2DFF
    DF_NOP,

    // 12E OP_UNUSED_2EFF
    DF_NOP,

    // 12F OP_UNUSED_2FFF
    DF_NOP,

    // 130 OP_UNUSED_30FF
    DF_NOP,

    // 131 OP_UNUSED_31FF
    DF_NOP,

    // 132 OP_UNUSED_32FF
    DF_NOP,

    // 133 OP_UNUSED_33FF
    DF_NOP,

    // 134 OP_UNUSED_34FF
    DF_NOP,

    // 135 OP_UNUSED_35FF
    DF_NOP,

    // 136 OP_UNUSED_36FF
    DF_NOP,

    // 137 OP_UNUSED_37FF
    DF_NOP,

    // 138 OP_UNUSED_38FF
    DF_NOP,

    // 139 OP_UNUSED_39FF
    DF_NOP,

    // 13A OP_UNUSED_3AFF
    DF_NOP,

    // 13B OP_UNUSED_3BFF
    DF_NOP,

    // 13C OP_UNUSED_3CFF
    DF_NOP,

    // 13D OP_UNUSED_3DFF
    DF_NOP,

    // 13E OP_UNUSED_3EFF
    DF_NOP,

    // 13F OP_UNUSED_3FFF
    DF_NOP,

    // 140 OP_UNUSED_40FF
    DF_NOP,

    // 141 OP_UNUSED_41FF
    DF_NOP,

    // 142 OP_UNUSED_42FF
    DF_NOP,

    // 143 OP_UNUSED_43FF
    DF_NOP,

    // 144 OP_UNUSED_44FF
    DF_NOP,

    // 145 OP_UNUSED_45FF
    DF_NOP,

    // 146 OP_UNUSED_46FF
    DF_NOP,

    // 147 OP_UNUSED_47FF
    DF_NOP,

    // 148 OP_UNUSED_48FF
    DF_NOP,

    // 149 OP_UNUSED_49FF
    DF_NOP,

    // 14A OP_UNUSED_4AFF
    DF_NOP,

    // 14B OP_UNUSED_4BFF
    DF_NOP,

    // 14C OP_UNUSED_4CFF
    DF_NOP,

    // 14D OP_UNUSED_4DFF
    DF_NOP,

    // 14E OP_UNUSED_4EFF
    DF_NOP,

    // 14F OP_UNUSED_4FFF
    DF_NOP,

    // 150 OP_UNUSED_50FF
    DF_NOP,

    // 151 OP_UNUSED_51FF
    DF_NOP,

    // 152 OP_UNUSED_52FF
    DF_NOP,

    // 153 OP_UNUSED_53FF
    DF_NOP,

    // 154 OP_UNUSED_54FF
    DF_NOP,

    // 155 OP_UNUSED_55FF
    DF_NOP,

    // 156 OP_UNUSED_56FF
    DF_NOP,

    // 157 OP_UNUSED_57FF
    DF_NOP,

    // 158 OP_UNUSED_58FF
    DF_NOP,

    // 159 OP_UNUSED_59FF
    DF_NOP,

    // 15A OP_UNUSED_5AFF
    DF_NOP,

    // 15B OP_UNUSED_5BFF
    DF_NOP,

    // 15C OP_UNUSED_5CFF
    DF_NOP,

    // 15D OP_UNUSED_5DFF
    DF_NOP,

    // 15E OP_UNUSED_5EFF
    DF_NOP,

    // 15F OP_UNUSED_5FFF
    DF_NOP,

    // 160 OP_UNUSED_60FF
    DF_NOP,

    // 161 OP_UNUSED_61FF
    DF_NOP,

    // 162 OP_UNUSED_62FF
    DF_NOP,

    // 163 OP_UNUSED_63FF
    DF_NOP,

    // 164 OP_UNUSED_64FF
    DF_NOP,

    // 165 OP_UNUSED_65FF
    DF_NOP,

    // 166 OP_UNUSED_66FF
    DF_NOP,

    // 167 OP_UNUSED_67FF
    DF_NOP,

    // 168 OP_UNUSED_68FF
    DF_NOP,

    // 169 OP_UNUSED_69FF
    DF_NOP,

    // 16A OP_UNUSED_6AFF
    DF_NOP,

    // 16B OP_UNUSED_6BFF
    DF_NOP,

    // 16C OP_UNUSED_6CFF
    DF_NOP,

    // 16D OP_UNUSED_6DFF
    DF_NOP,

    // 16E OP_UNUSED_6EFF
    DF_NOP,

    // 16F OP_UNUSED_6FFF
    DF_NOP,

    // 170 OP_UNUSED_70FF
    DF_NOP,

    // 171 OP_UNUSED_71FF
    DF_NOP,

    // 172 OP_UNUSED_72FF
    DF_NOP,

    // 173 OP_UNUSED_73FF
    DF_NOP,

    // 174 OP_UNUSED_74FF
    DF_NOP,

    // 175 OP_UNUSED_75FF
    DF_NOP,

    // 176 OP_UNUSED_76FF
    DF_NOP,

    // 177 OP_UNUSED_77FF
    DF_NOP,

    // 178 OP_UNUSED_78FF
    DF_NOP,

    // 179 OP_UNUSED_79FF
    DF_NOP,

    // 17A OP_UNUSED_7AFF
    DF_NOP,

    // 17B OP_UNUSED_7BFF
    DF_NOP,

    // 17C OP_UNUSED_7CFF
    DF_NOP,

    // 17D OP_UNUSED_7DFF
    DF_NOP,

    // 17E OP_UNUSED_7EFF
    DF_NOP,

    // 17F OP_UNUSED_7FFF
    DF_NOP,

    // 180 OP_UNUSED_80FF
    DF_NOP,

    // 181 OP_UNUSED_81FF
    DF_NOP,

    // 182 OP_UNUSED_82FF
    DF_NOP,

    // 183 OP_UNUSED_83FF
    DF_NOP,

    // 184 OP_UNUSED_84FF
    DF_NOP,

    // 185 OP_UNUSED_85FF
    DF_NOP,

    // 186 OP_UNUSED_86FF
    DF_NOP,

    // 187 OP_UNUSED_87FF
    DF_NOP,

    // 188 OP_UNUSED_88FF
    DF_NOP,

    // 189 OP_UNUSED_89FF
    DF_NOP,

    // 18A OP_UNUSED_8AFF
    DF_NOP,

    // 18B OP_UNUSED_8BFF
    DF_NOP,

    // 18C OP_UNUSED_8CFF
    DF_NOP,

    // 18D OP_UNUSED_8DFF
    DF_NOP,

    // 18E OP_UNUSED_8EFF
    DF_NOP,

    // 18F OP_UNUSED_8FFF
    DF_NOP,

    // 190 OP_UNUSED_90FF
    DF_NOP,

    // 191 OP_UNUSED_91FF
    DF_NOP,

    // 192 OP_UNUSED_92FF
    DF_NOP,

    // 193 OP_UNUSED_93FF
    DF_NOP,

    // 194 OP_UNUSED_94FF
    DF_NOP,

    // 195 OP_UNUSED_95FF
    DF_NOP,

    // 196 OP_UNUSED_96FF
    DF_NOP,

    // 197 OP_UNUSED_97FF
    DF_NOP,

    // 198 OP_UNUSED_98FF
    DF_NOP,

    // 199 OP_UNUSED_99FF
    DF_NOP,

    // 19A OP_UNUSED_9AFF
    DF_NOP,

    // 19B OP_UNUSED_9BFF
    DF_NOP,

    // 19C OP_UNUSED_9CFF
    DF_NOP,

    // 19D OP_UNUSED_9DFF
    DF_NOP,

    // 19E OP_UNUSED_9EFF
    DF_NOP,

    // 19F OP_UNUSED_9FFF
    DF_NOP,

    // 1A0 OP_UNUSED_A0FF
    DF_NOP,

    // 1A1 OP_UNUSED_A1FF
    DF_NOP,

    // 1A2 OP_UNUSED_A2FF
    DF_NOP,

    // 1A3 OP_UNUSED_A3FF
    DF_NOP,

    // 1A4 OP_UNUSED_A4FF
    DF_NOP,

    // 1A5 OP_UNUSED_A5FF
    DF_NOP,

    // 1A6 OP_UNUSED_A6FF
    DF_NOP,

    // 1A7 OP_UNUSED_A7FF
    DF_NOP,

    // 1A8 OP_UNUSED_A8FF
    DF_NOP,

    // 1A9 OP_UNUSED_A9FF
    DF_NOP,

    // 1AA OP_UNUSED_AAFF
    DF_NOP,

    // 1AB OP_UNUSED_ABFF
    DF_NOP,

    // 1AC OP_UNUSED_ACFF
    DF_NOP,

    // 1AD OP_UNUSED_ADFF
    DF_NOP,

    // 1AE OP_UNUSED_AEFF
    DF_NOP,

    // 1AF OP_UNUSED_AFFF
    DF_NOP,

    // 1B0 OP_UNUSED_B0FF
    DF_NOP,

    // 1B1 OP_UNUSED_B1FF
    DF_NOP,

    // 1B2 OP_UNUSED_B2FF
    DF_NOP,

    // 1B3 OP_UNUSED_B3FF
    DF_NOP,

    // 1B4 OP_UNUSED_B4FF
    DF_NOP,

    // 1B5 OP_UNUSED_B5FF
    DF_NOP,

    // 1B6 OP_UNUSED_B6FF
    DF_NOP,

    // 1B7 OP_UNUSED_B7FF
    DF_NOP,

    // 1B8 OP_UNUSED_B8FF
    DF_NOP,

    // 1B9 OP_UNUSED_B9FF
    DF_NOP,

    // 1BA OP_UNUSED_BAFF
    DF_NOP,

    // 1BB OP_UNUSED_BBFF
    DF_NOP,

    // 1BC OP_UNUSED_BCFF
    DF_NOP,

    // 1BD OP_UNUSED_BDFF
    DF_NOP,

    // 1BE OP_UNUSED_BEFF
    DF_NOP,

    // 1BF OP_UNUSED_BFFF
    DF_NOP,

    // 1C0 OP_UNUSED_C0FF
    DF_NOP,

    // 1C1 OP_UNUSED_C1FF
    DF_NOP,

    // 1C2 OP_UNUSED_C2FF
    DF_NOP,

    // 1C3 OP_UNUSED_C3FF
    DF_NOP,

    // 1C4 OP_UNUSED_C4FF
    DF_NOP,

    // 1C5 OP_UNUSED_C5FF
    DF_NOP,

    // 1C6 OP_UNUSED_C6FF
    DF_NOP,

    // 1C7 OP_UNUSED_C7FF
    DF_NOP,

    // 1C8 OP_UNUSED_C8FF
    DF_NOP,

    // 1C9 OP_UNUSED_C9FF
    DF_NOP,

    // 1CA OP_UNUSED_CAFF
    DF_NOP,

    // 1CB OP_UNUSED_CBFF
    DF_NOP,

    // 1CC OP_UNUSED_CCFF
    DF_NOP,

    // 1CD OP_UNUSED_CDFF
    DF_NOP,

    // 1CE OP_UNUSED_CEFF
    DF_NOP,

    // 1CF OP_UNUSED_CFFF
    DF_NOP,

    // 1D0 OP_UNUSED_D0FF
    DF_NOP,

    // 1D1 OP_UNUSED_D1FF
    DF_NOP,

    // 1D2 OP_UNUSED_D2FF
    DF_NOP,

    // 1D3 OP_UNUSED_D3FF
    DF_NOP,

    // 1D4 OP_UNUSED_D4FF
    DF_NOP,

    // 1D5 OP_UNUSED_D5FF
    DF_NOP,

    // 1D6 OP_UNUSED_D6FF
    DF_NOP,

    // 1D7 OP_UNUSED_D7FF
    DF_NOP,

    // 1D8 OP_UNUSED_D8FF
    DF_NOP,

    // 1D9 OP_UNUSED_D9FF
    DF_NOP,

    // 1DA OP_UNUSED_DAFF
    DF_NOP,

    // 1DB OP_UNUSED_DBFF
    DF_NOP,

    // 1DC OP_UNUSED_DCFF
    DF_NOP,

    // 1DD OP_UNUSED_DDFF
    DF_NOP,

    // 1DE OP_UNUSED_DEFF
    DF_NOP,

    // 1DF OP_UNUSED_DFFF
    DF_NOP,

    // 1E0 OP_UNUSED_E0FF
    DF_NOP,

    // 1E1 OP_UNUSED_E1FF
    DF_NOP,

    // 1E2 OP_UNUSED_E2FF
    DF_NOP,

    // 1E3 OP_UNUSED_E3FF
    DF_NOP,

    // 1E4 OP_UNUSED_E4FF
    DF_NOP,

    // 1E5 OP_UNUSED_E5FF
    DF_NOP,

    // 1E6 OP_UNUSED_E6FF
    DF_NOP,

    // 1E7 OP_UNUSED_E7FF
    DF_NOP,

    // 1E8 OP_UNUSED_E8FF
    DF_NOP,

    // 1E9 OP_UNUSED_E9FF
    DF_NOP,

    // 1EA OP_UNUSED_EAFF
    DF_NOP,

    // 1EB OP_UNUSED_EBFF
    DF_NOP,

    // 1EC OP_UNUSED_ECFF
    DF_NOP,

    // 1ED OP_UNUSED_EDFF
    DF_NOP,

    // 1EE OP_UNUSED_EEFF
    DF_NOP,

    // 1EF OP_UNUSED_EFFF
    DF_NOP,

    // 1F0 OP_UNUSED_F0FF
    DF_NOP,

    // 1F1 OP_UNUSED_F1FF
    DF_NOP,

    // 1F2 OP_INVOKE_OBJECT_INIT_JUMBO
    DF_NOP,

    // 1F3 OP_IGET_VOLATILE_JUMBO
    DF_DA | DF_UB,

    // 1F4 OP_IGET_WIDE_VOLATILE_JUMBO
    DF_DA_WIDE | DF_UB,

    // 1F5 OP_IGET_OBJECT_VOLATILE_JUMBO
    DF_DA | DF_UB,

    // 1F6 OP_IPUT_VOLATILE_JUMBO
    DF_UA | DF_UB,

    // 1F7 OP_IPUT_WIDE_VOLATILE_JUMBO
    DF_UA_WIDE | DF_UB,

    // 1F8 OP_IPUT_OBJECT_VOLATILE_JUMBO
    DF_UA | DF_UB,

    // 1F9 OP_SGET_VOLATILE_JUMBO
    DF_DA,

    // 1FA OP_SGET_WIDE_VOLATILE_JUMBO
    DF_DA_WIDE,

    // 1FB OP_SGET_OBJECT_VOLATILE_JUMBO
    DF_DA,

    // 1FC OP_SPUT_VOLATILE_JUMBO
    DF_UA,

    // 1FD OP_SPUT_WIDE_VOLATILE_JUMBO
    DF_UA_WIDE,

    // 1FE OP_SPUT_OBJECT_VOLATILE_JUMBO
    DF_UA,

    // 1FF OP_THROW_VERIFICATION_ERROR_JUMBO
    DF_NOP,

    // Beginning of extended MIR opcodes
    // 200 OP_MIR_PHI
    DF_PHI | DF_DA,
    /*
     * For extended MIR inserted at the MIR2LIR stage, it is okay to have
     * undefined values here.
     */
};

/* Return the Dalvik register/subscript pair of a given SSA register */
int dvmConvertSSARegToDalvik(const CompilationUnit *cUnit, int ssaReg)
{
      return GET_ELEM_N(cUnit->ssaToDalvikMap, int, ssaReg);
}

/*
 * Utility function to convert encoded SSA register value into Dalvik register
 * and subscript pair. Each SSA register can be used to index the
 * ssaToDalvikMap list to get the subscript[31..16]/dalvik_reg[15..0] mapping.
 */
char *dvmCompilerGetDalvikDisassembly(const DecodedInstruction *insn,
                                      const char *note)
{
    char buffer[256];
    Opcode opcode = insn->opcode;
    int dfAttributes = dvmCompilerDataFlowAttributes[opcode];
    int flags;
    char *ret;

    buffer[0] = 0;
    if ((int)opcode >= (int)kMirOpFirst) {
        if ((int)opcode == (int)kMirOpPhi) {
            strcpy(buffer, "PHI");
        }
        else {
            sprintf(buffer, "Opcode %#x", opcode);
        }
        flags = 0;
    } else {
        strcpy(buffer, dexGetOpcodeName(opcode));
        flags = dexGetFlagsFromOpcode(insn->opcode);
    }

    if (note)
        strcat(buffer, note);

    /* For branches, decode the instructions to print out the branch targets */
    if (flags & kInstrCanBranch) {
        InstructionFormat dalvikFormat = dexGetFormatFromOpcode(insn->opcode);
        int offset = 0;
        switch (dalvikFormat) {
            case kFmt21t:
                snprintf(buffer + strlen(buffer), 256, " v%d,", insn->vA);
                offset = (int) insn->vB;
                break;
            case kFmt22t:
                snprintf(buffer + strlen(buffer), 256, " v%d, v%d,",
                         insn->vA, insn->vB);
                offset = (int) insn->vC;
                break;
            case kFmt10t:
            case kFmt20t:
            case kFmt30t:
                offset = (int) insn->vA;
                break;
            default:
                LOGE("Unexpected branch format %d / opcode %#x", dalvikFormat,
                     opcode);
                dvmAbort();
                break;
        }
        snprintf(buffer + strlen(buffer), 256, " (%c%x)",
                 offset > 0 ? '+' : '-',
                 offset > 0 ? offset : -offset);
    } else if (dfAttributes & DF_FORMAT_35C) {
        unsigned int i;
        for (i = 0; i < insn->vA; i++) {
            if (i != 0) strcat(buffer, ",");
            snprintf(buffer + strlen(buffer), 256, " v%d", insn->arg[i]);
        }
    }
    else if (dfAttributes & DF_FORMAT_3RC) {
        snprintf(buffer + strlen(buffer), 256,
                 " v%d..v%d", insn->vC, insn->vC + insn->vA - 1);
    }
    else {
        if (dfAttributes & DF_A_IS_REG) {
            snprintf(buffer + strlen(buffer), 256, " v%d", insn->vA);
        }
        if (dfAttributes & DF_B_IS_REG) {
            snprintf(buffer + strlen(buffer), 256, ", v%d", insn->vB);
        }
        else if ((int)opcode < (int)kMirOpFirst) {
            snprintf(buffer + strlen(buffer), 256, ", (#%d)", insn->vB);
        }
        if (dfAttributes & DF_C_IS_REG) {
            snprintf(buffer + strlen(buffer), 256, ", v%d", insn->vC);
        }
        else if ((int)opcode < (int)kMirOpFirst) {
            snprintf(buffer + strlen(buffer), 256, ", (#%d)", insn->vC);
        }
    }
    int length = strlen(buffer) + 1;
    ret = (char *)dvmCompilerNew(length, false);
    memcpy(ret, buffer, length);
    return ret;
}

char *getSSAName(const CompilationUnit *cUnit, int ssaReg, char *name)
{
    int ssa2DalvikValue = dvmConvertSSARegToDalvik(cUnit, ssaReg);

    sprintf(name, "v%d_%d",
            DECODE_REG(ssa2DalvikValue), DECODE_SUB(ssa2DalvikValue));
    return name;
}

/*
 * Dalvik instruction disassembler with optional SSA printing.
 */
char *dvmCompilerFullDisassembler(const CompilationUnit *cUnit,
                                  const MIR *mir)
{
    char buffer[256];
    char operand0[256], operand1[256];
    const DecodedInstruction *insn = &mir->dalvikInsn;
    int opcode = insn->opcode;
    int dfAttributes = dvmCompilerDataFlowAttributes[opcode];
    char *ret;
    int length;
    OpcodeFlags flags;

    buffer[0] = 0;
    if (opcode >= kMirOpFirst) {
        if (opcode == kMirOpPhi) {
            snprintf(buffer, 256, "PHI %s = (%s",
                     getSSAName(cUnit, mir->ssaRep->defs[0], operand0),
                     getSSAName(cUnit, mir->ssaRep->uses[0], operand1));
            int i;
            for (i = 1; i < mir->ssaRep->numUses; i++) {
                snprintf(buffer + strlen(buffer), 256, ", %s",
                         getSSAName(cUnit, mir->ssaRep->uses[i], operand0));
            }
            snprintf(buffer + strlen(buffer), 256, ")");
        }
        else {
            sprintf(buffer, "Opcode %#x", opcode);
        }
        goto done;
    } else {
        strcpy(buffer, dexGetOpcodeName((Opcode)opcode));
    }

    flags = dexGetFlagsFromOpcode((Opcode)opcode);
    /* For branches, decode the instructions to print out the branch targets */
    if (flags & kInstrCanBranch) {
        InstructionFormat dalvikFormat = dexGetFormatFromOpcode(insn->opcode);
        int delta = 0;
        switch (dalvikFormat) {
            case kFmt21t:
                snprintf(buffer + strlen(buffer), 256, " %s, ",
                         getSSAName(cUnit, mir->ssaRep->uses[0], operand0));
                delta = (int) insn->vB;
                break;
            case kFmt22t:
                snprintf(buffer + strlen(buffer), 256, " %s, %s, ",
                         getSSAName(cUnit, mir->ssaRep->uses[0], operand0),
                         getSSAName(cUnit, mir->ssaRep->uses[1], operand1));
                delta = (int) insn->vC;
                break;
            case kFmt10t:
            case kFmt20t:
            case kFmt30t:
                delta = (int) insn->vA;
                break;
            default:
                LOGE("Unexpected branch format: %d", dalvikFormat);
                dvmAbort();
                break;
        }
        snprintf(buffer + strlen(buffer), 256, " %04x",
                 mir->offset + delta);
    } else if (dfAttributes & (DF_FORMAT_35C | DF_FORMAT_3RC)) {
        unsigned int i;
        for (i = 0; i < insn->vA; i++) {
            if (i != 0) strcat(buffer, ",");
            snprintf(buffer + strlen(buffer), 256, " %s",
                     getSSAName(cUnit, mir->ssaRep->uses[i], operand0));
        }
    } else {
        int udIdx;
        if (mir->ssaRep->numDefs) {

            for (udIdx = 0; udIdx < mir->ssaRep->numDefs; udIdx++) {
                snprintf(buffer + strlen(buffer), 256, " %s",
                         getSSAName(cUnit, mir->ssaRep->defs[udIdx], operand0));
            }
            strcat(buffer, ",");
        }
        if (mir->ssaRep->numUses) {
            /* No leading ',' for the first use */
            snprintf(buffer + strlen(buffer), 256, " %s",
                     getSSAName(cUnit, mir->ssaRep->uses[0], operand0));
            for (udIdx = 1; udIdx < mir->ssaRep->numUses; udIdx++) {
                snprintf(buffer + strlen(buffer), 256, ", %s",
                         getSSAName(cUnit, mir->ssaRep->uses[udIdx], operand0));
            }
        }
        if (opcode < kMirOpFirst) {
            InstructionFormat dalvikFormat =
                dexGetFormatFromOpcode((Opcode)opcode);
            switch (dalvikFormat) {
                case kFmt11n:        // op vA, #+B
                case kFmt21s:        // op vAA, #+BBBB
                case kFmt21h:        // op vAA, #+BBBB00000[00000000]
                case kFmt31i:        // op vAA, #+BBBBBBBB
                case kFmt51l:        // op vAA, #+BBBBBBBBBBBBBBBB
                    snprintf(buffer + strlen(buffer), 256, " #%#x", insn->vB);
                    break;
                case kFmt21c:        // op vAA, thing@BBBB
                case kFmt31c:        // op vAA, thing@BBBBBBBB
                    snprintf(buffer + strlen(buffer), 256, " @%#x", insn->vB);
                    break;
                case kFmt22b:        // op vAA, vBB, #+CC
                case kFmt22s:        // op vA, vB, #+CCCC
                    snprintf(buffer + strlen(buffer), 256, " #%#x", insn->vC);
                    break;
                case kFmt22c:        // op vA, vB, thing@CCCC
                case kFmt22cs:       // [opt] op vA, vB, field offset CCCC
                    snprintf(buffer + strlen(buffer), 256, " @%#x", insn->vC);
                    break;
                    /* No need for special printing */
                default:
                    break;
            }
        }
    }

done:
    length = strlen(buffer) + 1;
    ret = (char *) dvmCompilerNew(length, false);
    memcpy(ret, buffer, length);
    return ret;
}

/*
 * Utility function to convert encoded SSA register value into Dalvik register
 * and subscript pair. Each SSA register can be used to index the
 * ssaToDalvikMap list to get the subscript[31..16]/dalvik_reg[15..0] mapping.
 */
char *dvmCompilerGetSSAString(CompilationUnit *cUnit, SSARepresentation *ssaRep)
{
    char buffer[256];
    char *ret;
    int i;

    buffer[0] = 0;
    for (i = 0; i < ssaRep->numDefs; i++) {
        int ssa2DalvikValue = dvmConvertSSARegToDalvik(cUnit, ssaRep->defs[i]);

        sprintf(buffer + strlen(buffer), "s%d(v%d_%d) ",
                ssaRep->defs[i], DECODE_REG(ssa2DalvikValue),
                DECODE_SUB(ssa2DalvikValue));
    }

    if (ssaRep->numDefs) {
        strcat(buffer, "<- ");
    }

    for (i = 0; i < ssaRep->numUses; i++) {
        int ssa2DalvikValue = dvmConvertSSARegToDalvik(cUnit, ssaRep->uses[i]);
        int len = strlen(buffer);

        if (snprintf(buffer + len, 250 - len, "s%d(v%d_%d) ",
                     ssaRep->uses[i], DECODE_REG(ssa2DalvikValue),
                     DECODE_SUB(ssa2DalvikValue)) >= (250 - len)) {
            strcat(buffer, "...");
            break;
        }
    }

    int length = strlen(buffer) + 1;
    ret = (char *)dvmCompilerNew(length, false);
    memcpy(ret, buffer, length);
    return ret;
}

/* Any register that is used before being defined is considered live-in */
static inline void handleLiveInUse(BitVector *useV, BitVector *defV,
                                   BitVector *liveInV, int dalvikRegId)
{
    dvmCompilerSetBit(useV, dalvikRegId);
    if (!dvmIsBitSet(defV, dalvikRegId)) {
        dvmCompilerSetBit(liveInV, dalvikRegId);
    }
}

/* Mark a reg as being defined */
static inline void handleDef(BitVector *defV, int dalvikRegId)
{
    dvmCompilerSetBit(defV, dalvikRegId);
}

/*
 * Find out live-in variables for natural loops. Variables that are live-in in
 * the main loop body are considered to be defined in the entry block.
 */
bool dvmCompilerFindLocalLiveIn(CompilationUnit *cUnit, BasicBlock *bb)
{
    MIR *mir;
    BitVector *useV, *defV, *liveInV;

    if (bb->dataFlowInfo == NULL) return false;

    useV = bb->dataFlowInfo->useV =
        dvmCompilerAllocBitVector(cUnit->numDalvikRegisters, false);
    defV = bb->dataFlowInfo->defV =
        dvmCompilerAllocBitVector(cUnit->numDalvikRegisters, false);
    liveInV = bb->dataFlowInfo->liveInV =
        dvmCompilerAllocBitVector(cUnit->numDalvikRegisters, false);

    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opcode];
        DecodedInstruction *dInsn = &mir->dalvikInsn;

        if (dfAttributes & DF_HAS_USES) {
            if (dfAttributes & DF_UA) {
                handleLiveInUse(useV, defV, liveInV, dInsn->vA);
            } else if (dfAttributes & DF_UA_WIDE) {
                handleLiveInUse(useV, defV, liveInV, dInsn->vA);
                handleLiveInUse(useV, defV, liveInV, dInsn->vA+1);
            }
            if (dfAttributes & DF_UB) {
                handleLiveInUse(useV, defV, liveInV, dInsn->vB);
            } else if (dfAttributes & DF_UB_WIDE) {
                handleLiveInUse(useV, defV, liveInV, dInsn->vB);
                handleLiveInUse(useV, defV, liveInV, dInsn->vB+1);
            }
            if (dfAttributes & DF_UC) {
                handleLiveInUse(useV, defV, liveInV, dInsn->vC);
            } else if (dfAttributes & DF_UC_WIDE) {
                handleLiveInUse(useV, defV, liveInV, dInsn->vC);
                handleLiveInUse(useV, defV, liveInV, dInsn->vC+1);
            }
        }
        if (dfAttributes & DF_HAS_DEFS) {
            handleDef(defV, dInsn->vA);
            if (dfAttributes & DF_DA_WIDE) {
                handleDef(defV, dInsn->vA+1);
            }
        }
    }
    return true;
}

/* Find out the latest SSA register for a given Dalvik register */
static void handleSSAUse(CompilationUnit *cUnit, int *uses, int dalvikReg,
                         int regIndex)
{
    int encodedValue = cUnit->dalvikToSSAMap[dalvikReg];
    int ssaReg = DECODE_REG(encodedValue);
    uses[regIndex] = ssaReg;
}

/* Setup a new SSA register for a given Dalvik register */
static void handleSSADef(CompilationUnit *cUnit, int *defs, int dalvikReg,
                         int regIndex)
{
    int encodedValue = cUnit->dalvikToSSAMap[dalvikReg];
    int ssaReg = cUnit->numSSARegs++;
    /* Bump up the subscript */
    int dalvikSub = DECODE_SUB(encodedValue) + 1;
    int newD2SMapping = ENCODE_REG_SUB(ssaReg, dalvikSub);

    cUnit->dalvikToSSAMap[dalvikReg] = newD2SMapping;

    int newS2DMapping = ENCODE_REG_SUB(dalvikReg, dalvikSub);
    dvmInsertGrowableList(cUnit->ssaToDalvikMap, newS2DMapping);

    defs[regIndex] = ssaReg;
}

/* Loop up new SSA names for format_35c instructions */
static void dataFlowSSAFormat35C(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int numUses = dInsn->vA;
    int i;

    mir->ssaRep->numUses = numUses;
    mir->ssaRep->uses = (int *)dvmCompilerNew(sizeof(int) * numUses, false);

    for (i = 0; i < numUses; i++) {
        handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->arg[i], i);
    }
}

/* Loop up new SSA names for format_3rc instructions */
static void dataFlowSSAFormat3RC(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int numUses = dInsn->vA;
    int i;

    mir->ssaRep->numUses = numUses;
    mir->ssaRep->uses = (int *)dvmCompilerNew(sizeof(int) * numUses, false);

    for (i = 0; i < numUses; i++) {
        handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vC+i, i);
    }
}

/* Entry function to convert a block into SSA representation */
bool dvmCompilerDoSSAConversion(CompilationUnit *cUnit, BasicBlock *bb)
{
    MIR *mir;

    if (bb->dataFlowInfo == NULL) return false;

    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        mir->ssaRep = (struct SSARepresentation *)
            dvmCompilerNew(sizeof(SSARepresentation), true);

        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opcode];

        int numUses = 0;

        if (dfAttributes & DF_FORMAT_35C) {
            dataFlowSSAFormat35C(cUnit, mir);
            continue;
        }

        if (dfAttributes & DF_FORMAT_3RC) {
            dataFlowSSAFormat3RC(cUnit, mir);
            continue;
        }

        if (dfAttributes & DF_HAS_USES) {
            if (dfAttributes & DF_UA) {
                numUses++;
            } else if (dfAttributes & DF_UA_WIDE) {
                numUses += 2;
            }
            if (dfAttributes & DF_UB) {
                numUses++;
            } else if (dfAttributes & DF_UB_WIDE) {
                numUses += 2;
            }
            if (dfAttributes & DF_UC) {
                numUses++;
            } else if (dfAttributes & DF_UC_WIDE) {
                numUses += 2;
            }
        }

        if (numUses) {
            mir->ssaRep->numUses = numUses;
            mir->ssaRep->uses = (int *)dvmCompilerNew(sizeof(int) * numUses,
                                                      false);
            mir->ssaRep->fpUse = (bool *)dvmCompilerNew(sizeof(bool) * numUses,
                                                false);
        }

        int numDefs = 0;

        if (dfAttributes & DF_HAS_DEFS) {
            numDefs++;
            if (dfAttributes & DF_DA_WIDE) {
                numDefs++;
            }
        }

        if (numDefs) {
            mir->ssaRep->numDefs = numDefs;
            mir->ssaRep->defs = (int *)dvmCompilerNew(sizeof(int) * numDefs,
                                                      false);
            mir->ssaRep->fpDef = (bool *)dvmCompilerNew(sizeof(bool) * numDefs,
                                                        false);
        }

        DecodedInstruction *dInsn = &mir->dalvikInsn;

        if (dfAttributes & DF_HAS_USES) {
            numUses = 0;
            if (dfAttributes & DF_UA) {
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_A;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vA, numUses++);
            } else if (dfAttributes & DF_UA_WIDE) {
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_A;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vA, numUses++);
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_A;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vA+1, numUses++);
            }
            if (dfAttributes & DF_UB) {
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_B;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vB, numUses++);
            } else if (dfAttributes & DF_UB_WIDE) {
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_B;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vB, numUses++);
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_B;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vB+1, numUses++);
            }
            if (dfAttributes & DF_UC) {
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_C;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vC, numUses++);
            } else if (dfAttributes & DF_UC_WIDE) {
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_C;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vC, numUses++);
                mir->ssaRep->fpUse[numUses] = dfAttributes & DF_FP_C;
                handleSSAUse(cUnit, mir->ssaRep->uses, dInsn->vC+1, numUses++);
            }
        }
        if (dfAttributes & DF_HAS_DEFS) {
            mir->ssaRep->fpDef[0] = dfAttributes & DF_FP_A;
            handleSSADef(cUnit, mir->ssaRep->defs, dInsn->vA, 0);
            if (dfAttributes & DF_DA_WIDE) {
                mir->ssaRep->fpDef[1] = dfAttributes & DF_FP_A;
                handleSSADef(cUnit, mir->ssaRep->defs, dInsn->vA+1, 1);
            }
        }
    }

    /*
     * Take a snapshot of Dalvik->SSA mapping at the end of each block. The
     * input to PHI nodes can be derived from the snapshot of all predecessor
     * blocks.
     */
    bb->dataFlowInfo->dalvikToSSAMap =
        (int *)dvmCompilerNew(sizeof(int) * cUnit->method->registersSize,
                              false);

    memcpy(bb->dataFlowInfo->dalvikToSSAMap, cUnit->dalvikToSSAMap,
           sizeof(int) * cUnit->method->registersSize);
    return true;
}

/* Setup a constant value for opcodes thare have the DF_SETS_CONST attribute */
static void setConstant(CompilationUnit *cUnit, int ssaReg, int value)
{
    dvmSetBit(cUnit->isConstantV, ssaReg);
    cUnit->constantValues[ssaReg] = value;
}

bool dvmCompilerDoConstantPropagation(CompilationUnit *cUnit, BasicBlock *bb)
{
    MIR *mir;
    BitVector *isConstantV = cUnit->isConstantV;

    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opcode];

        DecodedInstruction *dInsn = &mir->dalvikInsn;

        if (!(dfAttributes & DF_HAS_DEFS)) continue;

        /* Handle instructions that set up constants directly */
        if (dfAttributes & DF_SETS_CONST) {
            if (dfAttributes & DF_DA) {
                switch (dInsn->opcode) {
                    case OP_CONST_4:
                    case OP_CONST_16:
                    case OP_CONST:
                        setConstant(cUnit, mir->ssaRep->defs[0], dInsn->vB);
                        break;
                    case OP_CONST_HIGH16:
                        setConstant(cUnit, mir->ssaRep->defs[0],
                                    dInsn->vB << 16);
                        break;
                    default:
                        break;
                }
            } else if (dfAttributes & DF_DA_WIDE) {
                switch (dInsn->opcode) {
                    case OP_CONST_WIDE_16:
                    case OP_CONST_WIDE_32:
                        setConstant(cUnit, mir->ssaRep->defs[0], dInsn->vB);
                        setConstant(cUnit, mir->ssaRep->defs[1], 0);
                        break;
                    case OP_CONST_WIDE:
                        setConstant(cUnit, mir->ssaRep->defs[0],
                                    (int) dInsn->vB_wide);
                        setConstant(cUnit, mir->ssaRep->defs[1],
                                    (int) (dInsn->vB_wide >> 32));
                        break;
                    case OP_CONST_WIDE_HIGH16:
                        setConstant(cUnit, mir->ssaRep->defs[0], 0);
                        setConstant(cUnit, mir->ssaRep->defs[1],
                                    dInsn->vB << 16);
                        break;
                    default:
                        break;
                }
            }
        /* Handle instructions that set up constants directly */
        } else if (dfAttributes & DF_IS_MOVE) {
            int i;

            for (i = 0; i < mir->ssaRep->numUses; i++) {
                if (!dvmIsBitSet(isConstantV, mir->ssaRep->uses[i])) break;
            }
            /* Move a register holding a constant to another register */
            if (i == mir->ssaRep->numUses) {
                setConstant(cUnit, mir->ssaRep->defs[0],
                            cUnit->constantValues[mir->ssaRep->uses[0]]);
                if (dfAttributes & DF_DA_WIDE) {
                    setConstant(cUnit, mir->ssaRep->defs[1],
                                cUnit->constantValues[mir->ssaRep->uses[1]]);
                }
            }
        }
    }
    /* TODO: implement code to handle arithmetic operations */
    return true;
}

bool dvmCompilerFindInductionVariables(struct CompilationUnit *cUnit,
                                       struct BasicBlock *bb)
{
    BitVector *isIndVarV = cUnit->loopAnalysis->isIndVarV;
    BitVector *isConstantV = cUnit->isConstantV;
    GrowableList *ivList = cUnit->loopAnalysis->ivList;
    MIR *mir;

    if (bb->blockType != kDalvikByteCode && bb->blockType != kEntryBlock) {
        return false;
    }

    /* If the bb doesn't have a phi it cannot contain an induction variable */
    if (bb->firstMIRInsn == NULL ||
        (int)bb->firstMIRInsn->dalvikInsn.opcode != (int)kMirOpPhi) {
        return false;
    }

    /* Find basic induction variable first */
    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opcode];

        if (!(dfAttributes & DF_IS_LINEAR)) continue;

        /*
         * For a basic induction variable:
         *   1) use[0] should belong to the output of a phi node
         *   2) def[0] should belong to the input of the same phi node
         *   3) the value added/subtracted is a constant
         */
        MIR *phi;
        for (phi = bb->firstMIRInsn; phi; phi = phi->next) {
            if ((int)phi->dalvikInsn.opcode != (int)kMirOpPhi) break;

            if (phi->ssaRep->defs[0] == mir->ssaRep->uses[0] &&
                phi->ssaRep->uses[1] == mir->ssaRep->defs[0]) {
                bool deltaIsConstant = false;
                int deltaValue;

                switch (mir->dalvikInsn.opcode) {
                    case OP_ADD_INT:
                        if (dvmIsBitSet(isConstantV,
                                        mir->ssaRep->uses[1])) {
                            deltaValue =
                                cUnit->constantValues[mir->ssaRep->uses[1]];
                            deltaIsConstant = true;
                        }
                        break;
                    case OP_SUB_INT:
                        if (dvmIsBitSet(isConstantV,
                                        mir->ssaRep->uses[1])) {
                            deltaValue =
                                -cUnit->constantValues[mir->ssaRep->uses[1]];
                            deltaIsConstant = true;
                        }
                        break;
                    case OP_ADD_INT_LIT8:
                        deltaValue = mir->dalvikInsn.vC;
                        deltaIsConstant = true;
                        break;
                    default:
                        break;
                }
                if (deltaIsConstant) {
                    dvmSetBit(isIndVarV, mir->ssaRep->uses[0]);
                    InductionVariableInfo *ivInfo = (InductionVariableInfo *)
                        dvmCompilerNew(sizeof(InductionVariableInfo),
                                       false);

                    ivInfo->ssaReg = mir->ssaRep->uses[0];
                    ivInfo->basicSSAReg = mir->ssaRep->uses[0];
                    ivInfo->m = 1;         // always 1 to basic iv
                    ivInfo->c = 0;         // N/A to basic iv
                    ivInfo->inc = deltaValue;
                    dvmInsertGrowableList(ivList, (intptr_t) ivInfo);
                    cUnit->loopAnalysis->numBasicIV++;
                    break;
                }
            }
        }
    }

    /* Find dependent induction variable now */
    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opcode];

        if (!(dfAttributes & DF_IS_LINEAR)) continue;

        /* Skip already identified induction variables */
        if (dvmIsBitSet(isIndVarV, mir->ssaRep->defs[0])) continue;

        /*
         * For a dependent induction variable:
         *  1) use[0] should be an induction variable (basic/dependent)
         *  2) operand2 should be a constant
         */
        if (dvmIsBitSet(isIndVarV, mir->ssaRep->uses[0])) {
            int srcDalvikReg = dvmConvertSSARegToDalvik(cUnit,
                                                        mir->ssaRep->uses[0]);
            int dstDalvikReg = dvmConvertSSARegToDalvik(cUnit,
                                                        mir->ssaRep->defs[0]);

            bool cIsConstant = false;
            int c = 0;

            switch (mir->dalvikInsn.opcode) {
                case OP_ADD_INT:
                    if (dvmIsBitSet(isConstantV,
                                    mir->ssaRep->uses[1])) {
                        c = cUnit->constantValues[mir->ssaRep->uses[1]];
                        cIsConstant = true;
                    }
                    break;
                case OP_SUB_INT:
                    if (dvmIsBitSet(isConstantV,
                                    mir->ssaRep->uses[1])) {
                        c = -cUnit->constantValues[mir->ssaRep->uses[1]];
                        cIsConstant = true;
                    }
                    break;
                case OP_ADD_INT_LIT8:
                    c = mir->dalvikInsn.vC;
                    cIsConstant = true;
                    break;
                default:
                    break;
            }

            /* Ignore the update to the basic induction variable itself */
            if (DECODE_REG(srcDalvikReg) == DECODE_REG(dstDalvikReg))  {
                cUnit->loopAnalysis->ssaBIV = mir->ssaRep->defs[0];
                cIsConstant = false;
            }

            if (cIsConstant) {
                unsigned int i;
                dvmSetBit(isIndVarV, mir->ssaRep->defs[0]);
                InductionVariableInfo *ivInfo = (InductionVariableInfo *)
                    dvmCompilerNew(sizeof(InductionVariableInfo),
                                   false);
                InductionVariableInfo *ivInfoOld = NULL ;

                for (i = 0; i < ivList->numUsed; i++) {
                    ivInfoOld = (InductionVariableInfo *) ivList->elemList[i];
                    if (ivInfoOld->ssaReg == mir->ssaRep->uses[0]) break;
                }

                /* Guaranteed to find an element */
                assert(i < ivList->numUsed);

                ivInfo->ssaReg = mir->ssaRep->defs[0];
                ivInfo->basicSSAReg = ivInfoOld->basicSSAReg;
                ivInfo->m = ivInfoOld->m;
                ivInfo->c = c + ivInfoOld->c;
                ivInfo->inc = ivInfoOld->inc;
                dvmInsertGrowableList(ivList, (intptr_t) ivInfo);
            }
        }
    }
    return true;
}

/* Setup the basic data structures for SSA conversion */
void dvmInitializeSSAConversion(CompilationUnit *cUnit)
{
    int i;
    int numDalvikReg = cUnit->method->registersSize;

    cUnit->ssaToDalvikMap = (GrowableList *)dvmCompilerNew(sizeof(GrowableList),
                                                           false);
    dvmInitGrowableList(cUnit->ssaToDalvikMap, numDalvikReg);

    /*
     * Initial number of SSA registers is equal to the number of Dalvik
     * registers.
     */
    cUnit->numSSARegs = numDalvikReg;

    /*
     * Initialize the SSA2Dalvik map list. For the first numDalvikReg elements,
     * the subscript is 0 so we use the ENCODE_REG_SUB macro to encode the value
     * into "(0 << 16) | i"
     */
    for (i = 0; i < numDalvikReg; i++) {
        dvmInsertGrowableList(cUnit->ssaToDalvikMap, ENCODE_REG_SUB(i, 0));
    }

    /*
     * Initialize the DalvikToSSAMap map. The low 16 bit is the SSA register id,
     * while the high 16 bit is the current subscript. The original Dalvik
     * register N is mapped to SSA register N with subscript 0.
     */
    cUnit->dalvikToSSAMap = (int *)dvmCompilerNew(sizeof(int) * numDalvikReg,
                                                  false);
    for (i = 0; i < numDalvikReg; i++) {
        cUnit->dalvikToSSAMap[i] = i;
    }

    /*
     * Allocate the BasicBlockDataFlow structure for the entry and code blocks
     */
    GrowableListIterator iterator;

    dvmGrowableListIteratorInit(&cUnit->blockList, &iterator);

    while (true) {
        BasicBlock *bb = (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
        if (bb == NULL) break;
        if (bb->hidden == true) continue;
        if (bb->blockType == kDalvikByteCode ||
            bb->blockType == kEntryBlock ||
            bb->blockType == kExitBlock) {
            bb->dataFlowInfo = (BasicBlockDataFlow *)
                dvmCompilerNew(sizeof(BasicBlockDataFlow),
                               true);
        }
    }
}

/* Clear the visited flag for each BB */
bool dvmCompilerClearVisitedFlag(struct CompilationUnit *cUnit,
                                 struct BasicBlock *bb)
{
    bb->visited = false;
    return true;
}

void dvmCompilerDataFlowAnalysisDispatcher(CompilationUnit *cUnit,
                bool (*func)(CompilationUnit *, BasicBlock *),
                DataFlowAnalysisMode dfaMode,
                bool isIterative)
{
    bool change = true;

    while (change) {
        change = false;

        /* Scan all blocks and perform the operations specified in func */
        if (dfaMode == kAllNodes) {
            GrowableListIterator iterator;
            dvmGrowableListIteratorInit(&cUnit->blockList, &iterator);
            while (true) {
                BasicBlock *bb =
                    (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
                if (bb == NULL) break;
                if (bb->hidden == true) continue;
                change |= (*func)(cUnit, bb);
            }
        }
        /*
         * Scan all reachable blocks and perform the operations specified in
         * func.
         */
        else if (dfaMode == kReachableNodes) {
            int numReachableBlocks = cUnit->numReachableBlocks;
            int idx;
            const GrowableList *blockList = &cUnit->blockList;

            for (idx = 0; idx < numReachableBlocks; idx++) {
                int blockIdx = cUnit->dfsOrder.elemList[idx];
                BasicBlock *bb =
                    (BasicBlock *) dvmGrowableListGetElement(blockList,
                                                             blockIdx);
                change |= (*func)(cUnit, bb);
            }
        }
        /*
         * Scan all reachable blocks by the pre-order in the depth-first-search
         * CFG and perform the operations specified in func.
         */
        else if (dfaMode == kPreOrderDFSTraversal) {
            int numReachableBlocks = cUnit->numReachableBlocks;
            int idx;
            const GrowableList *blockList = &cUnit->blockList;

            for (idx = 0; idx < numReachableBlocks; idx++) {
                int dfsIdx = cUnit->dfsOrder.elemList[idx];
                BasicBlock *bb =
                    (BasicBlock *) dvmGrowableListGetElement(blockList, dfsIdx);
                change |= (*func)(cUnit, bb);
            }
        }
        /*
         * Scan all reachable blocks by the post-order in the depth-first-search
         * CFG and perform the operations specified in func.
         */
        else if (dfaMode == kPostOrderDFSTraversal) {
            int numReachableBlocks = cUnit->numReachableBlocks;
            int idx;
            const GrowableList *blockList = &cUnit->blockList;

            for (idx = numReachableBlocks - 1; idx >= 0; idx--) {
                int dfsIdx = cUnit->dfsOrder.elemList[idx];
                BasicBlock *bb =
                    (BasicBlock *) dvmGrowableListGetElement(blockList, dfsIdx);
                change |= (*func)(cUnit, bb);
            }
        }
        /*
         * Scan all reachable blocks by the post-order in the dominator tree
         * and perform the operations specified in func.
         */
        else if (dfaMode == kPostOrderDOMTraversal) {
            int numReachableBlocks = cUnit->numReachableBlocks;
            int idx;
            const GrowableList *blockList = &cUnit->blockList;

            for (idx = 0; idx < numReachableBlocks; idx++) {
                int domIdx = cUnit->domPostOrderTraversal.elemList[idx];
                BasicBlock *bb =
                    (BasicBlock *) dvmGrowableListGetElement(blockList, domIdx);
                change |= (*func)(cUnit, bb);
            }
        }
        /* If isIterative is false, exit the loop after the first iteration */
        change &= isIterative;
    }
}

/* Main entry point to do SSA conversion for non-loop traces */
void dvmCompilerNonLoopAnalysis(CompilationUnit *cUnit)
{
    dvmCompilerDataFlowAnalysisDispatcher(cUnit, dvmCompilerDoSSAConversion,
                                          kAllNodes,
                                          false /* isIterative */);
}

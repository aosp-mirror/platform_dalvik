/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dx.io;

import com.android.dx.dex.code.DalvOps;
import com.android.dx.util.Hex;

/**
 * Information about each Dalvik opcode.
 */
public final class OpcodeInfo {
    /*
     * TODO: Merge at least most of the info from the Dops class into
     * this one.
     */

    /** non-null; array containing all the information */
    private static final Info[] INFO;

    // BEGIN(opcode-info-defs); GENERATED AUTOMATICALLY BY opcode-gen
    public static final Info NOP =
        new Info(DalvOps.NOP,
            InstructionCodec.FORMAT_10X, null);

    public static final Info MOVE =
        new Info(DalvOps.MOVE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MOVE_FROM16 =
        new Info(DalvOps.MOVE_FROM16,
            InstructionCodec.FORMAT_22X, null);

    public static final Info MOVE_16 =
        new Info(DalvOps.MOVE_16,
            InstructionCodec.FORMAT_32X, null);

    public static final Info MOVE_WIDE =
        new Info(DalvOps.MOVE_WIDE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MOVE_WIDE_FROM16 =
        new Info(DalvOps.MOVE_WIDE_FROM16,
            InstructionCodec.FORMAT_22X, null);

    public static final Info MOVE_WIDE_16 =
        new Info(DalvOps.MOVE_WIDE_16,
            InstructionCodec.FORMAT_32X, null);

    public static final Info MOVE_OBJECT =
        new Info(DalvOps.MOVE_OBJECT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MOVE_OBJECT_FROM16 =
        new Info(DalvOps.MOVE_OBJECT_FROM16,
            InstructionCodec.FORMAT_22X, null);

    public static final Info MOVE_OBJECT_16 =
        new Info(DalvOps.MOVE_OBJECT_16,
            InstructionCodec.FORMAT_32X, null);

    public static final Info MOVE_RESULT =
        new Info(DalvOps.MOVE_RESULT,
            InstructionCodec.FORMAT_11X, null);

    public static final Info MOVE_RESULT_WIDE =
        new Info(DalvOps.MOVE_RESULT_WIDE,
            InstructionCodec.FORMAT_11X, null);

    public static final Info MOVE_RESULT_OBJECT =
        new Info(DalvOps.MOVE_RESULT_OBJECT,
            InstructionCodec.FORMAT_11X, null);

    public static final Info MOVE_EXCEPTION =
        new Info(DalvOps.MOVE_EXCEPTION,
            InstructionCodec.FORMAT_11X, null);

    public static final Info RETURN_VOID =
        new Info(DalvOps.RETURN_VOID,
            InstructionCodec.FORMAT_10X, null);

    public static final Info RETURN =
        new Info(DalvOps.RETURN,
            InstructionCodec.FORMAT_11X, null);

    public static final Info RETURN_WIDE =
        new Info(DalvOps.RETURN_WIDE,
            InstructionCodec.FORMAT_11X, null);

    public static final Info RETURN_OBJECT =
        new Info(DalvOps.RETURN_OBJECT,
            InstructionCodec.FORMAT_11X, null);

    public static final Info CONST_4 =
        new Info(DalvOps.CONST_4,
            InstructionCodec.FORMAT_11N, null);

    public static final Info CONST_16 =
        new Info(DalvOps.CONST_16,
            InstructionCodec.FORMAT_21S, null);

    public static final Info CONST =
        new Info(DalvOps.CONST,
            InstructionCodec.FORMAT_31I, null);

    public static final Info CONST_HIGH16 =
        new Info(DalvOps.CONST_HIGH16,
            InstructionCodec.FORMAT_21H, null);

    public static final Info CONST_WIDE_16 =
        new Info(DalvOps.CONST_WIDE_16,
            InstructionCodec.FORMAT_21S, null);

    public static final Info CONST_WIDE_32 =
        new Info(DalvOps.CONST_WIDE_32,
            InstructionCodec.FORMAT_31I, null);

    public static final Info CONST_WIDE =
        new Info(DalvOps.CONST_WIDE,
            InstructionCodec.FORMAT_51L, null);

    public static final Info CONST_WIDE_HIGH16 =
        new Info(DalvOps.CONST_WIDE_HIGH16,
            InstructionCodec.FORMAT_21H, null);

    public static final Info CONST_STRING =
        new Info(DalvOps.CONST_STRING,
            InstructionCodec.FORMAT_21C, IndexType.STRING_REF);

    public static final Info CONST_STRING_JUMBO =
        new Info(DalvOps.CONST_STRING_JUMBO,
            InstructionCodec.FORMAT_31C, IndexType.STRING_REF);

    public static final Info CONST_CLASS =
        new Info(DalvOps.CONST_CLASS,
            InstructionCodec.FORMAT_21C, IndexType.TYPE_REF);

    public static final Info MONITOR_ENTER =
        new Info(DalvOps.MONITOR_ENTER,
            InstructionCodec.FORMAT_11X, null);

    public static final Info MONITOR_EXIT =
        new Info(DalvOps.MONITOR_EXIT,
            InstructionCodec.FORMAT_11X, null);

    public static final Info CHECK_CAST =
        new Info(DalvOps.CHECK_CAST,
            InstructionCodec.FORMAT_21C, IndexType.TYPE_REF);

    public static final Info INSTANCE_OF =
        new Info(DalvOps.INSTANCE_OF,
            InstructionCodec.FORMAT_22C, IndexType.TYPE_REF);

    public static final Info ARRAY_LENGTH =
        new Info(DalvOps.ARRAY_LENGTH,
            InstructionCodec.FORMAT_12X, null);

    public static final Info NEW_INSTANCE =
        new Info(DalvOps.NEW_INSTANCE,
            InstructionCodec.FORMAT_21C, IndexType.TYPE_REF);

    public static final Info NEW_ARRAY =
        new Info(DalvOps.NEW_ARRAY,
            InstructionCodec.FORMAT_22C, IndexType.TYPE_REF);

    public static final Info FILLED_NEW_ARRAY =
        new Info(DalvOps.FILLED_NEW_ARRAY,
            InstructionCodec.FORMAT_35C, IndexType.TYPE_REF);

    public static final Info FILLED_NEW_ARRAY_RANGE =
        new Info(DalvOps.FILLED_NEW_ARRAY_RANGE,
            InstructionCodec.FORMAT_3RC, IndexType.TYPE_REF);

    public static final Info FILL_ARRAY_DATA =
        new Info(DalvOps.FILL_ARRAY_DATA,
            InstructionCodec.FORMAT_31T, null);

    public static final Info THROW =
        new Info(DalvOps.THROW,
            InstructionCodec.FORMAT_11X, null);

    public static final Info GOTO =
        new Info(DalvOps.GOTO,
            InstructionCodec.FORMAT_10T, null);

    public static final Info GOTO_16 =
        new Info(DalvOps.GOTO_16,
            InstructionCodec.FORMAT_20T, null);

    public static final Info GOTO_32 =
        new Info(DalvOps.GOTO_32,
            InstructionCodec.FORMAT_30T, null);

    public static final Info PACKED_SWITCH =
        new Info(DalvOps.PACKED_SWITCH,
            InstructionCodec.FORMAT_31T, null);

    public static final Info SPARSE_SWITCH =
        new Info(DalvOps.SPARSE_SWITCH,
            InstructionCodec.FORMAT_31T, null);

    public static final Info CMPL_FLOAT =
        new Info(DalvOps.CMPL_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info CMPG_FLOAT =
        new Info(DalvOps.CMPG_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info CMPL_DOUBLE =
        new Info(DalvOps.CMPL_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info CMPG_DOUBLE =
        new Info(DalvOps.CMPG_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info CMP_LONG =
        new Info(DalvOps.CMP_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info IF_EQ =
        new Info(DalvOps.IF_EQ,
            InstructionCodec.FORMAT_22T, null);

    public static final Info IF_NE =
        new Info(DalvOps.IF_NE,
            InstructionCodec.FORMAT_22T, null);

    public static final Info IF_LT =
        new Info(DalvOps.IF_LT,
            InstructionCodec.FORMAT_22T, null);

    public static final Info IF_GE =
        new Info(DalvOps.IF_GE,
            InstructionCodec.FORMAT_22T, null);

    public static final Info IF_GT =
        new Info(DalvOps.IF_GT,
            InstructionCodec.FORMAT_22T, null);

    public static final Info IF_LE =
        new Info(DalvOps.IF_LE,
            InstructionCodec.FORMAT_22T, null);

    public static final Info IF_EQZ =
        new Info(DalvOps.IF_EQZ,
            InstructionCodec.FORMAT_21T, null);

    public static final Info IF_NEZ =
        new Info(DalvOps.IF_NEZ,
            InstructionCodec.FORMAT_21T, null);

    public static final Info IF_LTZ =
        new Info(DalvOps.IF_LTZ,
            InstructionCodec.FORMAT_21T, null);

    public static final Info IF_GEZ =
        new Info(DalvOps.IF_GEZ,
            InstructionCodec.FORMAT_21T, null);

    public static final Info IF_GTZ =
        new Info(DalvOps.IF_GTZ,
            InstructionCodec.FORMAT_21T, null);

    public static final Info IF_LEZ =
        new Info(DalvOps.IF_LEZ,
            InstructionCodec.FORMAT_21T, null);

    public static final Info AGET =
        new Info(DalvOps.AGET,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AGET_WIDE =
        new Info(DalvOps.AGET_WIDE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AGET_OBJECT =
        new Info(DalvOps.AGET_OBJECT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AGET_BOOLEAN =
        new Info(DalvOps.AGET_BOOLEAN,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AGET_BYTE =
        new Info(DalvOps.AGET_BYTE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AGET_CHAR =
        new Info(DalvOps.AGET_CHAR,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AGET_SHORT =
        new Info(DalvOps.AGET_SHORT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT =
        new Info(DalvOps.APUT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT_WIDE =
        new Info(DalvOps.APUT_WIDE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT_OBJECT =
        new Info(DalvOps.APUT_OBJECT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT_BOOLEAN =
        new Info(DalvOps.APUT_BOOLEAN,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT_BYTE =
        new Info(DalvOps.APUT_BYTE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT_CHAR =
        new Info(DalvOps.APUT_CHAR,
            InstructionCodec.FORMAT_23X, null);

    public static final Info APUT_SHORT =
        new Info(DalvOps.APUT_SHORT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info IGET =
        new Info(DalvOps.IGET,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IGET_WIDE =
        new Info(DalvOps.IGET_WIDE,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IGET_OBJECT =
        new Info(DalvOps.IGET_OBJECT,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IGET_BOOLEAN =
        new Info(DalvOps.IGET_BOOLEAN,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IGET_BYTE =
        new Info(DalvOps.IGET_BYTE,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IGET_CHAR =
        new Info(DalvOps.IGET_CHAR,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IGET_SHORT =
        new Info(DalvOps.IGET_SHORT,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT =
        new Info(DalvOps.IPUT,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT_WIDE =
        new Info(DalvOps.IPUT_WIDE,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT_OBJECT =
        new Info(DalvOps.IPUT_OBJECT,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT_BOOLEAN =
        new Info(DalvOps.IPUT_BOOLEAN,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT_BYTE =
        new Info(DalvOps.IPUT_BYTE,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT_CHAR =
        new Info(DalvOps.IPUT_CHAR,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info IPUT_SHORT =
        new Info(DalvOps.IPUT_SHORT,
            InstructionCodec.FORMAT_22C, IndexType.FIELD_REF);

    public static final Info SGET =
        new Info(DalvOps.SGET,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SGET_WIDE =
        new Info(DalvOps.SGET_WIDE,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SGET_OBJECT =
        new Info(DalvOps.SGET_OBJECT,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SGET_BOOLEAN =
        new Info(DalvOps.SGET_BOOLEAN,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SGET_BYTE =
        new Info(DalvOps.SGET_BYTE,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SGET_CHAR =
        new Info(DalvOps.SGET_CHAR,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SGET_SHORT =
        new Info(DalvOps.SGET_SHORT,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT =
        new Info(DalvOps.SPUT,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT_WIDE =
        new Info(DalvOps.SPUT_WIDE,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT_OBJECT =
        new Info(DalvOps.SPUT_OBJECT,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT_BOOLEAN =
        new Info(DalvOps.SPUT_BOOLEAN,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT_BYTE =
        new Info(DalvOps.SPUT_BYTE,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT_CHAR =
        new Info(DalvOps.SPUT_CHAR,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info SPUT_SHORT =
        new Info(DalvOps.SPUT_SHORT,
            InstructionCodec.FORMAT_21C, IndexType.FIELD_REF);

    public static final Info INVOKE_VIRTUAL =
        new Info(DalvOps.INVOKE_VIRTUAL,
            InstructionCodec.FORMAT_35C, IndexType.METHOD_REF);

    public static final Info INVOKE_SUPER =
        new Info(DalvOps.INVOKE_SUPER,
            InstructionCodec.FORMAT_35C, IndexType.METHOD_REF);

    public static final Info INVOKE_DIRECT =
        new Info(DalvOps.INVOKE_DIRECT,
            InstructionCodec.FORMAT_35C, IndexType.METHOD_REF);

    public static final Info INVOKE_STATIC =
        new Info(DalvOps.INVOKE_STATIC,
            InstructionCodec.FORMAT_35C, IndexType.METHOD_REF);

    public static final Info INVOKE_INTERFACE =
        new Info(DalvOps.INVOKE_INTERFACE,
            InstructionCodec.FORMAT_35C, IndexType.METHOD_REF);

    public static final Info INVOKE_VIRTUAL_RANGE =
        new Info(DalvOps.INVOKE_VIRTUAL_RANGE,
            InstructionCodec.FORMAT_3RC, IndexType.METHOD_REF);

    public static final Info INVOKE_SUPER_RANGE =
        new Info(DalvOps.INVOKE_SUPER_RANGE,
            InstructionCodec.FORMAT_3RC, IndexType.METHOD_REF);

    public static final Info INVOKE_DIRECT_RANGE =
        new Info(DalvOps.INVOKE_DIRECT_RANGE,
            InstructionCodec.FORMAT_3RC, IndexType.METHOD_REF);

    public static final Info INVOKE_STATIC_RANGE =
        new Info(DalvOps.INVOKE_STATIC_RANGE,
            InstructionCodec.FORMAT_3RC, IndexType.METHOD_REF);

    public static final Info INVOKE_INTERFACE_RANGE =
        new Info(DalvOps.INVOKE_INTERFACE_RANGE,
            InstructionCodec.FORMAT_3RC, IndexType.METHOD_REF);

    public static final Info NEG_INT =
        new Info(DalvOps.NEG_INT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info NOT_INT =
        new Info(DalvOps.NOT_INT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info NEG_LONG =
        new Info(DalvOps.NEG_LONG,
            InstructionCodec.FORMAT_12X, null);

    public static final Info NOT_LONG =
        new Info(DalvOps.NOT_LONG,
            InstructionCodec.FORMAT_12X, null);

    public static final Info NEG_FLOAT =
        new Info(DalvOps.NEG_FLOAT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info NEG_DOUBLE =
        new Info(DalvOps.NEG_DOUBLE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info INT_TO_LONG =
        new Info(DalvOps.INT_TO_LONG,
            InstructionCodec.FORMAT_12X, null);

    public static final Info INT_TO_FLOAT =
        new Info(DalvOps.INT_TO_FLOAT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info INT_TO_DOUBLE =
        new Info(DalvOps.INT_TO_DOUBLE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info LONG_TO_INT =
        new Info(DalvOps.LONG_TO_INT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info LONG_TO_FLOAT =
        new Info(DalvOps.LONG_TO_FLOAT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info LONG_TO_DOUBLE =
        new Info(DalvOps.LONG_TO_DOUBLE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info FLOAT_TO_INT =
        new Info(DalvOps.FLOAT_TO_INT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info FLOAT_TO_LONG =
        new Info(DalvOps.FLOAT_TO_LONG,
            InstructionCodec.FORMAT_12X, null);

    public static final Info FLOAT_TO_DOUBLE =
        new Info(DalvOps.FLOAT_TO_DOUBLE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DOUBLE_TO_INT =
        new Info(DalvOps.DOUBLE_TO_INT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DOUBLE_TO_LONG =
        new Info(DalvOps.DOUBLE_TO_LONG,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DOUBLE_TO_FLOAT =
        new Info(DalvOps.DOUBLE_TO_FLOAT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info INT_TO_BYTE =
        new Info(DalvOps.INT_TO_BYTE,
            InstructionCodec.FORMAT_12X, null);

    public static final Info INT_TO_CHAR =
        new Info(DalvOps.INT_TO_CHAR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info INT_TO_SHORT =
        new Info(DalvOps.INT_TO_SHORT,
            InstructionCodec.FORMAT_12X, null);

    public static final Info ADD_INT =
        new Info(DalvOps.ADD_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SUB_INT =
        new Info(DalvOps.SUB_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info MUL_INT =
        new Info(DalvOps.MUL_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info DIV_INT =
        new Info(DalvOps.DIV_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info REM_INT =
        new Info(DalvOps.REM_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AND_INT =
        new Info(DalvOps.AND_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info OR_INT =
        new Info(DalvOps.OR_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info XOR_INT =
        new Info(DalvOps.XOR_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SHL_INT =
        new Info(DalvOps.SHL_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SHR_INT =
        new Info(DalvOps.SHR_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info USHR_INT =
        new Info(DalvOps.USHR_INT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info ADD_LONG =
        new Info(DalvOps.ADD_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SUB_LONG =
        new Info(DalvOps.SUB_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info MUL_LONG =
        new Info(DalvOps.MUL_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info DIV_LONG =
        new Info(DalvOps.DIV_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info REM_LONG =
        new Info(DalvOps.REM_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info AND_LONG =
        new Info(DalvOps.AND_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info OR_LONG =
        new Info(DalvOps.OR_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info XOR_LONG =
        new Info(DalvOps.XOR_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SHL_LONG =
        new Info(DalvOps.SHL_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SHR_LONG =
        new Info(DalvOps.SHR_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info USHR_LONG =
        new Info(DalvOps.USHR_LONG,
            InstructionCodec.FORMAT_23X, null);

    public static final Info ADD_FLOAT =
        new Info(DalvOps.ADD_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SUB_FLOAT =
        new Info(DalvOps.SUB_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info MUL_FLOAT =
        new Info(DalvOps.MUL_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info DIV_FLOAT =
        new Info(DalvOps.DIV_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info REM_FLOAT =
        new Info(DalvOps.REM_FLOAT,
            InstructionCodec.FORMAT_23X, null);

    public static final Info ADD_DOUBLE =
        new Info(DalvOps.ADD_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info SUB_DOUBLE =
        new Info(DalvOps.SUB_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info MUL_DOUBLE =
        new Info(DalvOps.MUL_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info DIV_DOUBLE =
        new Info(DalvOps.DIV_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info REM_DOUBLE =
        new Info(DalvOps.REM_DOUBLE,
            InstructionCodec.FORMAT_23X, null);

    public static final Info ADD_INT_2ADDR =
        new Info(DalvOps.ADD_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SUB_INT_2ADDR =
        new Info(DalvOps.SUB_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MUL_INT_2ADDR =
        new Info(DalvOps.MUL_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DIV_INT_2ADDR =
        new Info(DalvOps.DIV_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info REM_INT_2ADDR =
        new Info(DalvOps.REM_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info AND_INT_2ADDR =
        new Info(DalvOps.AND_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info OR_INT_2ADDR =
        new Info(DalvOps.OR_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info XOR_INT_2ADDR =
        new Info(DalvOps.XOR_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SHL_INT_2ADDR =
        new Info(DalvOps.SHL_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SHR_INT_2ADDR =
        new Info(DalvOps.SHR_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info USHR_INT_2ADDR =
        new Info(DalvOps.USHR_INT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info ADD_LONG_2ADDR =
        new Info(DalvOps.ADD_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SUB_LONG_2ADDR =
        new Info(DalvOps.SUB_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MUL_LONG_2ADDR =
        new Info(DalvOps.MUL_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DIV_LONG_2ADDR =
        new Info(DalvOps.DIV_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info REM_LONG_2ADDR =
        new Info(DalvOps.REM_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info AND_LONG_2ADDR =
        new Info(DalvOps.AND_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info OR_LONG_2ADDR =
        new Info(DalvOps.OR_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info XOR_LONG_2ADDR =
        new Info(DalvOps.XOR_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SHL_LONG_2ADDR =
        new Info(DalvOps.SHL_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SHR_LONG_2ADDR =
        new Info(DalvOps.SHR_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info USHR_LONG_2ADDR =
        new Info(DalvOps.USHR_LONG_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info ADD_FLOAT_2ADDR =
        new Info(DalvOps.ADD_FLOAT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SUB_FLOAT_2ADDR =
        new Info(DalvOps.SUB_FLOAT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MUL_FLOAT_2ADDR =
        new Info(DalvOps.MUL_FLOAT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DIV_FLOAT_2ADDR =
        new Info(DalvOps.DIV_FLOAT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info REM_FLOAT_2ADDR =
        new Info(DalvOps.REM_FLOAT_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info ADD_DOUBLE_2ADDR =
        new Info(DalvOps.ADD_DOUBLE_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info SUB_DOUBLE_2ADDR =
        new Info(DalvOps.SUB_DOUBLE_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info MUL_DOUBLE_2ADDR =
        new Info(DalvOps.MUL_DOUBLE_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info DIV_DOUBLE_2ADDR =
        new Info(DalvOps.DIV_DOUBLE_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info REM_DOUBLE_2ADDR =
        new Info(DalvOps.REM_DOUBLE_2ADDR,
            InstructionCodec.FORMAT_12X, null);

    public static final Info ADD_INT_LIT16 =
        new Info(DalvOps.ADD_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info RSUB_INT =
        new Info(DalvOps.RSUB_INT,
            InstructionCodec.FORMAT_22S, null);

    public static final Info MUL_INT_LIT16 =
        new Info(DalvOps.MUL_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info DIV_INT_LIT16 =
        new Info(DalvOps.DIV_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info REM_INT_LIT16 =
        new Info(DalvOps.REM_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info AND_INT_LIT16 =
        new Info(DalvOps.AND_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info OR_INT_LIT16 =
        new Info(DalvOps.OR_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info XOR_INT_LIT16 =
        new Info(DalvOps.XOR_INT_LIT16,
            InstructionCodec.FORMAT_22S, null);

    public static final Info ADD_INT_LIT8 =
        new Info(DalvOps.ADD_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info RSUB_INT_LIT8 =
        new Info(DalvOps.RSUB_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info MUL_INT_LIT8 =
        new Info(DalvOps.MUL_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info DIV_INT_LIT8 =
        new Info(DalvOps.DIV_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info REM_INT_LIT8 =
        new Info(DalvOps.REM_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info AND_INT_LIT8 =
        new Info(DalvOps.AND_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info OR_INT_LIT8 =
        new Info(DalvOps.OR_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info XOR_INT_LIT8 =
        new Info(DalvOps.XOR_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info SHL_INT_LIT8 =
        new Info(DalvOps.SHL_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info SHR_INT_LIT8 =
        new Info(DalvOps.SHR_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info USHR_INT_LIT8 =
        new Info(DalvOps.USHR_INT_LIT8,
            InstructionCodec.FORMAT_22B, null);

    public static final Info CONST_CLASS_JUMBO =
        new Info(DalvOps.CONST_CLASS_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.TYPE_REF);

    public static final Info CHECK_CAST_JUMBO =
        new Info(DalvOps.CHECK_CAST_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.TYPE_REF);

    public static final Info INSTANCE_OF_JUMBO =
        new Info(DalvOps.INSTANCE_OF_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.TYPE_REF);

    public static final Info NEW_INSTANCE_JUMBO =
        new Info(DalvOps.NEW_INSTANCE_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.TYPE_REF);

    public static final Info NEW_ARRAY_JUMBO =
        new Info(DalvOps.NEW_ARRAY_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.TYPE_REF);

    public static final Info FILLED_NEW_ARRAY_JUMBO =
        new Info(DalvOps.FILLED_NEW_ARRAY_JUMBO,
            InstructionCodec.FORMAT_5RC, IndexType.TYPE_REF);

    public static final Info IGET_JUMBO =
        new Info(DalvOps.IGET_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IGET_WIDE_JUMBO =
        new Info(DalvOps.IGET_WIDE_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IGET_OBJECT_JUMBO =
        new Info(DalvOps.IGET_OBJECT_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IGET_BOOLEAN_JUMBO =
        new Info(DalvOps.IGET_BOOLEAN_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IGET_BYTE_JUMBO =
        new Info(DalvOps.IGET_BYTE_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IGET_CHAR_JUMBO =
        new Info(DalvOps.IGET_CHAR_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IGET_SHORT_JUMBO =
        new Info(DalvOps.IGET_SHORT_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_JUMBO =
        new Info(DalvOps.IPUT_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_WIDE_JUMBO =
        new Info(DalvOps.IPUT_WIDE_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_OBJECT_JUMBO =
        new Info(DalvOps.IPUT_OBJECT_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_BOOLEAN_JUMBO =
        new Info(DalvOps.IPUT_BOOLEAN_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_BYTE_JUMBO =
        new Info(DalvOps.IPUT_BYTE_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_CHAR_JUMBO =
        new Info(DalvOps.IPUT_CHAR_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info IPUT_SHORT_JUMBO =
        new Info(DalvOps.IPUT_SHORT_JUMBO,
            InstructionCodec.FORMAT_52C, IndexType.FIELD_REF);

    public static final Info SGET_JUMBO =
        new Info(DalvOps.SGET_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SGET_WIDE_JUMBO =
        new Info(DalvOps.SGET_WIDE_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SGET_OBJECT_JUMBO =
        new Info(DalvOps.SGET_OBJECT_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SGET_BOOLEAN_JUMBO =
        new Info(DalvOps.SGET_BOOLEAN_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SGET_BYTE_JUMBO =
        new Info(DalvOps.SGET_BYTE_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SGET_CHAR_JUMBO =
        new Info(DalvOps.SGET_CHAR_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SGET_SHORT_JUMBO =
        new Info(DalvOps.SGET_SHORT_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_JUMBO =
        new Info(DalvOps.SPUT_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_WIDE_JUMBO =
        new Info(DalvOps.SPUT_WIDE_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_OBJECT_JUMBO =
        new Info(DalvOps.SPUT_OBJECT_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_BOOLEAN_JUMBO =
        new Info(DalvOps.SPUT_BOOLEAN_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_BYTE_JUMBO =
        new Info(DalvOps.SPUT_BYTE_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_CHAR_JUMBO =
        new Info(DalvOps.SPUT_CHAR_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info SPUT_SHORT_JUMBO =
        new Info(DalvOps.SPUT_SHORT_JUMBO,
            InstructionCodec.FORMAT_41C, IndexType.FIELD_REF);

    public static final Info INVOKE_VIRTUAL_JUMBO =
        new Info(DalvOps.INVOKE_VIRTUAL_JUMBO,
            InstructionCodec.FORMAT_5RC, IndexType.METHOD_REF);

    public static final Info INVOKE_SUPER_JUMBO =
        new Info(DalvOps.INVOKE_SUPER_JUMBO,
            InstructionCodec.FORMAT_5RC, IndexType.METHOD_REF);

    public static final Info INVOKE_DIRECT_JUMBO =
        new Info(DalvOps.INVOKE_DIRECT_JUMBO,
            InstructionCodec.FORMAT_5RC, IndexType.METHOD_REF);

    public static final Info INVOKE_STATIC_JUMBO =
        new Info(DalvOps.INVOKE_STATIC_JUMBO,
            InstructionCodec.FORMAT_5RC, IndexType.METHOD_REF);

    public static final Info INVOKE_INTERFACE_JUMBO =
        new Info(DalvOps.INVOKE_INTERFACE_JUMBO,
            InstructionCodec.FORMAT_5RC, IndexType.METHOD_REF);

    // END(opcode-info-defs)

    // Static initialization.
    static {
        INFO = new Info[DalvOps.MAX_VALUE - DalvOps.MIN_VALUE + 1];

        // BEGIN(opcode-info-init); GENERATED AUTOMATICALLY BY opcode-gen
        set(NOP);
        set(MOVE);
        set(MOVE_FROM16);
        set(MOVE_16);
        set(MOVE_WIDE);
        set(MOVE_WIDE_FROM16);
        set(MOVE_WIDE_16);
        set(MOVE_OBJECT);
        set(MOVE_OBJECT_FROM16);
        set(MOVE_OBJECT_16);
        set(MOVE_RESULT);
        set(MOVE_RESULT_WIDE);
        set(MOVE_RESULT_OBJECT);
        set(MOVE_EXCEPTION);
        set(RETURN_VOID);
        set(RETURN);
        set(RETURN_WIDE);
        set(RETURN_OBJECT);
        set(CONST_4);
        set(CONST_16);
        set(CONST);
        set(CONST_HIGH16);
        set(CONST_WIDE_16);
        set(CONST_WIDE_32);
        set(CONST_WIDE);
        set(CONST_WIDE_HIGH16);
        set(CONST_STRING);
        set(CONST_STRING_JUMBO);
        set(CONST_CLASS);
        set(MONITOR_ENTER);
        set(MONITOR_EXIT);
        set(CHECK_CAST);
        set(INSTANCE_OF);
        set(ARRAY_LENGTH);
        set(NEW_INSTANCE);
        set(NEW_ARRAY);
        set(FILLED_NEW_ARRAY);
        set(FILLED_NEW_ARRAY_RANGE);
        set(FILL_ARRAY_DATA);
        set(THROW);
        set(GOTO);
        set(GOTO_16);
        set(GOTO_32);
        set(PACKED_SWITCH);
        set(SPARSE_SWITCH);
        set(CMPL_FLOAT);
        set(CMPG_FLOAT);
        set(CMPL_DOUBLE);
        set(CMPG_DOUBLE);
        set(CMP_LONG);
        set(IF_EQ);
        set(IF_NE);
        set(IF_LT);
        set(IF_GE);
        set(IF_GT);
        set(IF_LE);
        set(IF_EQZ);
        set(IF_NEZ);
        set(IF_LTZ);
        set(IF_GEZ);
        set(IF_GTZ);
        set(IF_LEZ);
        set(AGET);
        set(AGET_WIDE);
        set(AGET_OBJECT);
        set(AGET_BOOLEAN);
        set(AGET_BYTE);
        set(AGET_CHAR);
        set(AGET_SHORT);
        set(APUT);
        set(APUT_WIDE);
        set(APUT_OBJECT);
        set(APUT_BOOLEAN);
        set(APUT_BYTE);
        set(APUT_CHAR);
        set(APUT_SHORT);
        set(IGET);
        set(IGET_WIDE);
        set(IGET_OBJECT);
        set(IGET_BOOLEAN);
        set(IGET_BYTE);
        set(IGET_CHAR);
        set(IGET_SHORT);
        set(IPUT);
        set(IPUT_WIDE);
        set(IPUT_OBJECT);
        set(IPUT_BOOLEAN);
        set(IPUT_BYTE);
        set(IPUT_CHAR);
        set(IPUT_SHORT);
        set(SGET);
        set(SGET_WIDE);
        set(SGET_OBJECT);
        set(SGET_BOOLEAN);
        set(SGET_BYTE);
        set(SGET_CHAR);
        set(SGET_SHORT);
        set(SPUT);
        set(SPUT_WIDE);
        set(SPUT_OBJECT);
        set(SPUT_BOOLEAN);
        set(SPUT_BYTE);
        set(SPUT_CHAR);
        set(SPUT_SHORT);
        set(INVOKE_VIRTUAL);
        set(INVOKE_SUPER);
        set(INVOKE_DIRECT);
        set(INVOKE_STATIC);
        set(INVOKE_INTERFACE);
        set(INVOKE_VIRTUAL_RANGE);
        set(INVOKE_SUPER_RANGE);
        set(INVOKE_DIRECT_RANGE);
        set(INVOKE_STATIC_RANGE);
        set(INVOKE_INTERFACE_RANGE);
        set(NEG_INT);
        set(NOT_INT);
        set(NEG_LONG);
        set(NOT_LONG);
        set(NEG_FLOAT);
        set(NEG_DOUBLE);
        set(INT_TO_LONG);
        set(INT_TO_FLOAT);
        set(INT_TO_DOUBLE);
        set(LONG_TO_INT);
        set(LONG_TO_FLOAT);
        set(LONG_TO_DOUBLE);
        set(FLOAT_TO_INT);
        set(FLOAT_TO_LONG);
        set(FLOAT_TO_DOUBLE);
        set(DOUBLE_TO_INT);
        set(DOUBLE_TO_LONG);
        set(DOUBLE_TO_FLOAT);
        set(INT_TO_BYTE);
        set(INT_TO_CHAR);
        set(INT_TO_SHORT);
        set(ADD_INT);
        set(SUB_INT);
        set(MUL_INT);
        set(DIV_INT);
        set(REM_INT);
        set(AND_INT);
        set(OR_INT);
        set(XOR_INT);
        set(SHL_INT);
        set(SHR_INT);
        set(USHR_INT);
        set(ADD_LONG);
        set(SUB_LONG);
        set(MUL_LONG);
        set(DIV_LONG);
        set(REM_LONG);
        set(AND_LONG);
        set(OR_LONG);
        set(XOR_LONG);
        set(SHL_LONG);
        set(SHR_LONG);
        set(USHR_LONG);
        set(ADD_FLOAT);
        set(SUB_FLOAT);
        set(MUL_FLOAT);
        set(DIV_FLOAT);
        set(REM_FLOAT);
        set(ADD_DOUBLE);
        set(SUB_DOUBLE);
        set(MUL_DOUBLE);
        set(DIV_DOUBLE);
        set(REM_DOUBLE);
        set(ADD_INT_2ADDR);
        set(SUB_INT_2ADDR);
        set(MUL_INT_2ADDR);
        set(DIV_INT_2ADDR);
        set(REM_INT_2ADDR);
        set(AND_INT_2ADDR);
        set(OR_INT_2ADDR);
        set(XOR_INT_2ADDR);
        set(SHL_INT_2ADDR);
        set(SHR_INT_2ADDR);
        set(USHR_INT_2ADDR);
        set(ADD_LONG_2ADDR);
        set(SUB_LONG_2ADDR);
        set(MUL_LONG_2ADDR);
        set(DIV_LONG_2ADDR);
        set(REM_LONG_2ADDR);
        set(AND_LONG_2ADDR);
        set(OR_LONG_2ADDR);
        set(XOR_LONG_2ADDR);
        set(SHL_LONG_2ADDR);
        set(SHR_LONG_2ADDR);
        set(USHR_LONG_2ADDR);
        set(ADD_FLOAT_2ADDR);
        set(SUB_FLOAT_2ADDR);
        set(MUL_FLOAT_2ADDR);
        set(DIV_FLOAT_2ADDR);
        set(REM_FLOAT_2ADDR);
        set(ADD_DOUBLE_2ADDR);
        set(SUB_DOUBLE_2ADDR);
        set(MUL_DOUBLE_2ADDR);
        set(DIV_DOUBLE_2ADDR);
        set(REM_DOUBLE_2ADDR);
        set(ADD_INT_LIT16);
        set(RSUB_INT);
        set(MUL_INT_LIT16);
        set(DIV_INT_LIT16);
        set(REM_INT_LIT16);
        set(AND_INT_LIT16);
        set(OR_INT_LIT16);
        set(XOR_INT_LIT16);
        set(ADD_INT_LIT8);
        set(RSUB_INT_LIT8);
        set(MUL_INT_LIT8);
        set(DIV_INT_LIT8);
        set(REM_INT_LIT8);
        set(AND_INT_LIT8);
        set(OR_INT_LIT8);
        set(XOR_INT_LIT8);
        set(SHL_INT_LIT8);
        set(SHR_INT_LIT8);
        set(USHR_INT_LIT8);
        set(CONST_CLASS_JUMBO);
        set(CHECK_CAST_JUMBO);
        set(INSTANCE_OF_JUMBO);
        set(NEW_INSTANCE_JUMBO);
        set(NEW_ARRAY_JUMBO);
        set(FILLED_NEW_ARRAY_JUMBO);
        set(IGET_JUMBO);
        set(IGET_WIDE_JUMBO);
        set(IGET_OBJECT_JUMBO);
        set(IGET_BOOLEAN_JUMBO);
        set(IGET_BYTE_JUMBO);
        set(IGET_CHAR_JUMBO);
        set(IGET_SHORT_JUMBO);
        set(IPUT_JUMBO);
        set(IPUT_WIDE_JUMBO);
        set(IPUT_OBJECT_JUMBO);
        set(IPUT_BOOLEAN_JUMBO);
        set(IPUT_BYTE_JUMBO);
        set(IPUT_CHAR_JUMBO);
        set(IPUT_SHORT_JUMBO);
        set(SGET_JUMBO);
        set(SGET_WIDE_JUMBO);
        set(SGET_OBJECT_JUMBO);
        set(SGET_BOOLEAN_JUMBO);
        set(SGET_BYTE_JUMBO);
        set(SGET_CHAR_JUMBO);
        set(SGET_SHORT_JUMBO);
        set(SPUT_JUMBO);
        set(SPUT_WIDE_JUMBO);
        set(SPUT_OBJECT_JUMBO);
        set(SPUT_BOOLEAN_JUMBO);
        set(SPUT_BYTE_JUMBO);
        set(SPUT_CHAR_JUMBO);
        set(SPUT_SHORT_JUMBO);
        set(INVOKE_VIRTUAL_JUMBO);
        set(INVOKE_SUPER_JUMBO);
        set(INVOKE_DIRECT_JUMBO);
        set(INVOKE_STATIC_JUMBO);
        set(INVOKE_INTERFACE_JUMBO);
        // END(opcode-info-init)
    }

    /**
     * This class is uninstantiable.
     */
    private OpcodeInfo() {
        // This space intentionally left blank.
    }

    /**
     * Gets the {@link @Info} for the given opcode value.
     *
     * @param opcode {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the
     * opcode value
     * @return non-null; the associated opcode information instance
     */
    public static Info get(int opcode) {
        int idx = opcode - DalvOps.MIN_VALUE;

        try {
            Info result = INFO[idx];
            if (result != null) {
                return result;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // Fall through.
        }

        throw new IllegalArgumentException("bogus opcode: "
                + Hex.u2or4(opcode));
    }

    /**
     * Gets the {@link IndexType} for the given opcode value.
     */
    public static IndexType getIndexType(int opcode) {
        return get(opcode).getIndexType();
    }

    /**
     * Puts the given opcode into the table of all ops.
     *
     * @param opcode non-null; the opcode
     */
    private static void set(Info opcode) {
        int idx = opcode.getOpcode() - DalvOps.MIN_VALUE;
        INFO[idx] = opcode;
    }

    /**
     * Information about an opcode.
     */
    public static class Info {
        private final int opcode;
        private final InstructionCodec format;
        private final IndexType indexType;

        public Info(int opcode, InstructionCodec format, IndexType indexType) {
            this.opcode = opcode;
            this.format = format;
            this.indexType = indexType;
        }

        public int getOpcode() {
            return opcode;
        }

        public InstructionCodec getFormat() {
            return format;
        }

        public IndexType getIndexType() {
            return indexType;
        }
    }
}

/*
 * Copyright (C) 2007 The Android Open Source Project
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

package dalvik.bytecode;

/**
 * This is a list of Dalvik opcodes.
 *
 * (This was converted from //device/dalvik/libdex/OpCode.h)
 */
public interface Opcodes {
    int OP_NOP                          = 0x00;

    int OP_MOVE                         = 0x01;
    int OP_MOVE_FROM16                  = 0x02;
    int OP_MOVE_16                      = 0x03;
    int OP_MOVE_WIDE                    = 0x04;
    int OP_MOVE_WIDE_FROM16             = 0x05;
    int OP_MOVE_WIDE_16                 = 0x06;
    int OP_MOVE_OBJECT                  = 0x07;
    int OP_MOVE_OBJECT_FROM16           = 0x08;
    int OP_MOVE_OBJECT_16               = 0x09;

    int OP_MOVE_RESULT                  = 0x0a;
    int OP_MOVE_RESULT_WIDE             = 0x0b;
    int OP_MOVE_RESULT_OBJECT           = 0x0c;
    int OP_MOVE_EXCEPTION               = 0x0d;

    int OP_RETURN_VOID                  = 0x0e;
    int OP_RETURN                       = 0x0f;
    int OP_RETURN_WIDE                  = 0x10;
    int OP_RETURN_OBJECT                = 0x11;

    int OP_CONST_4                      = 0x12;
    int OP_CONST_16                     = 0x13;
    int OP_CONST                        = 0x14;
    int OP_CONST_HIGH16                 = 0x15;
    int OP_CONST_WIDE_16                = 0x16;
    int OP_CONST_WIDE_32                = 0x17;
    int OP_CONST_WIDE                   = 0x18;
    int OP_CONST_WIDE_HIGH16            = 0x19;
    int OP_CONST_STRING                 = 0x1a;
    int OP_CONST_STRING_JUMBO           = 0x1b;
    int OP_CONST_CLASS                  = 0x1c;

    int OP_MONITOR_ENTER                = 0x1d;
    int OP_MONITOR_EXIT                 = 0x1e;

    int OP_CHECK_CAST                   = 0x1f;
    int OP_INSTANCE_OF                  = 0x20;

    int OP_ARRAY_LENGTH                 = 0x21;

    int OP_NEW_INSTANCE                 = 0x22;
    int OP_NEW_ARRAY                    = 0x23;
    
    int OP_FILLED_NEW_ARRAY             = 0x24;
    int OP_FILLED_NEW_ARRAY_RANGE       = 0x25;
    int OP_FILL_ARRAY_DATA              = 0x26;

    int OP_THROW                        = 0x27;
    int OP_GOTO                         = 0x28;
    int OP_GOTO_16                      = 0x29;
    int OP_GOTO_32                      = 0x2a;
    int OP_PACKED_SWITCH                = 0x2b;
    int OP_SPARSE_SWITCH                = 0x2c;
    
    int OP_CMPL_FLOAT                   = 0x2d;
    int OP_CMPG_FLOAT                   = 0x2e;
    int OP_CMPL_DOUBLE                  = 0x2f;
    int OP_CMPG_DOUBLE                  = 0x30;
    int OP_CMP_LONG                     = 0x31;

    int OP_IF_EQ                        = 0x32;
    int OP_IF_NE                        = 0x33;
    int OP_IF_LT                        = 0x34;
    int OP_IF_GE                        = 0x35;
    int OP_IF_GT                        = 0x36;
    int OP_IF_LE                        = 0x37;
    int OP_IF_EQZ                       = 0x38;
    int OP_IF_NEZ                       = 0x39;
    int OP_IF_LTZ                       = 0x3a;
    int OP_IF_GEZ                       = 0x3b;
    int OP_IF_GTZ                       = 0x3c;
    int OP_IF_LEZ                       = 0x3d;

    int OP_UNUSED_3e                    = 0x3e;
    int OP_UNUSED_3f                    = 0x3f;
    int OP_UNUSED_40                    = 0x40;
    int OP_UNUSED_41                    = 0x41;
    int OP_UNUSED_42                    = 0x42;
    int OP_UNUSED_43                    = 0x43;
    
    int OP_AGET                         = 0x44;
    int OP_AGET_WIDE                    = 0x45;
    int OP_AGET_OBJECT                  = 0x46;
    int OP_AGET_BOOLEAN                 = 0x47;
    int OP_AGET_BYTE                    = 0x48;
    int OP_AGET_CHAR                    = 0x49;
    int OP_AGET_SHORT                   = 0x4a;
    int OP_APUT                         = 0x4b;
    int OP_APUT_WIDE                    = 0x4c;
    int OP_APUT_OBJECT                  = 0x4d;
    int OP_APUT_BOOLEAN                 = 0x4e;
    int OP_APUT_BYTE                    = 0x4f;
    int OP_APUT_CHAR                    = 0x50;
    int OP_APUT_SHORT                   = 0x51;

    int OP_IGET                         = 0x52;
    int OP_IGET_WIDE                    = 0x53;
    int OP_IGET_OBJECT                  = 0x54;
    int OP_IGET_BOOLEAN                 = 0x55;
    int OP_IGET_BYTE                    = 0x56;
    int OP_IGET_CHAR                    = 0x57;
    int OP_IGET_SHORT                   = 0x58;
    int OP_IPUT                         = 0x59;
    int OP_IPUT_WIDE                    = 0x5a;
    int OP_IPUT_OBJECT                  = 0x5b;
    int OP_IPUT_BOOLEAN                 = 0x5c;
    int OP_IPUT_BYTE                    = 0x5d;
    int OP_IPUT_CHAR                    = 0x5e;
    int OP_IPUT_SHORT                   = 0x5f;

    int OP_SGET                         = 0x60;
    int OP_SGET_WIDE                    = 0x61;
    int OP_SGET_OBJECT                  = 0x62;
    int OP_SGET_BOOLEAN                 = 0x63;
    int OP_SGET_BYTE                    = 0x64;
    int OP_SGET_CHAR                    = 0x65;
    int OP_SGET_SHORT                   = 0x66;
    int OP_SPUT                         = 0x67;
    int OP_SPUT_WIDE                    = 0x68;
    int OP_SPUT_OBJECT                  = 0x69;
    int OP_SPUT_BOOLEAN                 = 0x6a;
    int OP_SPUT_BYTE                    = 0x6b;
    int OP_SPUT_CHAR                    = 0x6c;
    int OP_SPUT_SHORT                   = 0x6d;

    int OP_INVOKE_VIRTUAL               = 0x6e;
    int OP_INVOKE_SUPER                 = 0x6f;
    int OP_INVOKE_DIRECT                = 0x70;
    int OP_INVOKE_STATIC                = 0x71;
    int OP_INVOKE_INTERFACE             = 0x72;

    int OP_UNUSED_73                    = 0x73;
    
    int OP_INVOKE_VIRTUAL_RANGE         = 0x74;
    int OP_INVOKE_SUPER_RANGE           = 0x75;
    int OP_INVOKE_DIRECT_RANGE          = 0x76;
    int OP_INVOKE_STATIC_RANGE          = 0x77;
    int OP_INVOKE_INTERFACE_RANGE       = 0x78;

    int OP_UNUSED_79                    = 0x79;
    int OP_UNUSED_7A                    = 0x7a;

    int OP_NEG_INT                      = 0x7b;
    int OP_NOT_INT                      = 0x7c;
    int OP_NEG_LONG                     = 0x7d;
    int OP_NOT_LONG                     = 0x7e;
    int OP_NEG_FLOAT                    = 0x7f;
    int OP_NEG_DOUBLE                   = 0x80;
    int OP_INT_TO_LONG                  = 0x81;
    int OP_INT_TO_FLOAT                 = 0x82;
    int OP_INT_TO_DOUBLE                = 0x83;
    int OP_LONG_TO_INT                  = 0x84;
    int OP_LONG_TO_FLOAT                = 0x85;
    int OP_LONG_TO_DOUBLE               = 0x86;
    int OP_FLOAT_TO_INT                 = 0x87;
    int OP_FLOAT_TO_LONG                = 0x88;
    int OP_FLOAT_TO_DOUBLE              = 0x89;
    int OP_DOUBLE_TO_INT                = 0x8a;
    int OP_DOUBLE_TO_LONG               = 0x8b;
    int OP_DOUBLE_TO_FLOAT              = 0x8c;
    int OP_INT_TO_BYTE                  = 0x8d;
    int OP_INT_TO_CHAR                  = 0x8e;
    int OP_INT_TO_SHORT                 = 0x8f;

    int OP_ADD_INT                      = 0x90;
    int OP_SUB_INT                      = 0x91;
    int OP_MUL_INT                      = 0x92;
    int OP_DIV_INT                      = 0x93;
    int OP_REM_INT                      = 0x94;
    int OP_AND_INT                      = 0x95;
    int OP_OR_INT                       = 0x96;
    int OP_XOR_INT                      = 0x97;
    int OP_SHL_INT                      = 0x98;
    int OP_SHR_INT                      = 0x99;
    int OP_USHR_INT                     = 0x9a;

    int OP_ADD_LONG                     = 0x9b;
    int OP_SUB_LONG                     = 0x9c;
    int OP_MUL_LONG                     = 0x9d;
    int OP_DIV_LONG                     = 0x9e;
    int OP_REM_LONG                     = 0x9f;
    int OP_AND_LONG                     = 0xa0;
    int OP_OR_LONG                      = 0xa1;
    int OP_XOR_LONG                     = 0xa2;
    int OP_SHL_LONG                     = 0xa3;
    int OP_SHR_LONG                     = 0xa4;
    int OP_USHR_LONG                    = 0xa5;

    int OP_ADD_FLOAT                    = 0xa6;
    int OP_SUB_FLOAT                    = 0xa7;
    int OP_MUL_FLOAT                    = 0xa8;
    int OP_DIV_FLOAT                    = 0xa9;
    int OP_REM_FLOAT                    = 0xaa;
    int OP_ADD_DOUBLE                   = 0xab;
    int OP_SUB_DOUBLE                   = 0xac;
    int OP_MUL_DOUBLE                   = 0xad;
    int OP_DIV_DOUBLE                   = 0xae;
    int OP_REM_DOUBLE                   = 0xaf;

    int OP_ADD_INT_2ADDR                = 0xb0;
    int OP_SUB_INT_2ADDR                = 0xb1;
    int OP_MUL_INT_2ADDR                = 0xb2;
    int OP_DIV_INT_2ADDR                = 0xb3;
    int OP_REM_INT_2ADDR                = 0xb4;
    int OP_AND_INT_2ADDR                = 0xb5;
    int OP_OR_INT_2ADDR                 = 0xb6;
    int OP_XOR_INT_2ADDR                = 0xb7;
    int OP_SHL_INT_2ADDR                = 0xb8;
    int OP_SHR_INT_2ADDR                = 0xb9;
    int OP_USHR_INT_2ADDR               = 0xba;

    int OP_ADD_LONG_2ADDR               = 0xbb;
    int OP_SUB_LONG_2ADDR               = 0xbc;
    int OP_MUL_LONG_2ADDR               = 0xbd;
    int OP_DIV_LONG_2ADDR               = 0xbe;
    int OP_REM_LONG_2ADDR               = 0xbf;
    int OP_AND_LONG_2ADDR               = 0xc0;
    int OP_OR_LONG_2ADDR                = 0xc1;
    int OP_XOR_LONG_2ADDR               = 0xc2;
    int OP_SHL_LONG_2ADDR               = 0xc3;
    int OP_SHR_LONG_2ADDR               = 0xc4;
    int OP_USHR_LONG_2ADDR              = 0xc5;

    int OP_ADD_FLOAT_2ADDR              = 0xc6;
    int OP_SUB_FLOAT_2ADDR              = 0xc7;
    int OP_MUL_FLOAT_2ADDR              = 0xc8;
    int OP_DIV_FLOAT_2ADDR              = 0xc9;
    int OP_REM_FLOAT_2ADDR              = 0xca;
    int OP_ADD_DOUBLE_2ADDR             = 0xcb;
    int OP_SUB_DOUBLE_2ADDR             = 0xcc;
    int OP_MUL_DOUBLE_2ADDR             = 0xcd;
    int OP_DIV_DOUBLE_2ADDR             = 0xce;
    int OP_REM_DOUBLE_2ADDR             = 0xcf;

    int OP_ADD_INT_LIT16                = 0xd0;
    int OP_RSUB_INT                     = 0xd1; /* no _LIT16 suffix for this */
    int OP_MUL_INT_LIT16                = 0xd2;
    int OP_DIV_INT_LIT16                = 0xd3;
    int OP_REM_INT_LIT16                = 0xd4;
    int OP_AND_INT_LIT16                = 0xd5;
    int OP_OR_INT_LIT16                 = 0xd6;
    int OP_XOR_INT_LIT16                = 0xd7;

    int OP_ADD_INT_LIT8                 = 0xd8;
    int OP_RSUB_INT_LIT8                = 0xd9;
    int OP_MUL_INT_LIT8                 = 0xda;
    int OP_DIV_INT_LIT8                 = 0xdb;
    int OP_REM_INT_LIT8                 = 0xdc;
    int OP_AND_INT_LIT8                 = 0xdd;
    int OP_OR_INT_LIT8                  = 0xde;
    int OP_XOR_INT_LIT8                 = 0xdf;
    int OP_SHL_INT_LIT8                 = 0xe0;
    int OP_SHR_INT_LIT8                 = 0xe1;
    int OP_USHR_INT_LIT8                = 0xe2;

    int OP_UNUSED_E3                    = 0xe3;
    int OP_UNUSED_E4                    = 0xe4;
    int OP_UNUSED_E5                    = 0xe5;
    int OP_UNUSED_E6                    = 0xe6;
    int OP_UNUSED_E7                    = 0xe7;
    int OP_UNUSED_E8                    = 0xe8;
    int OP_UNUSED_E9                    = 0xe9;
    int OP_UNUSED_EA                    = 0xea;
    int OP_UNUSED_EB                    = 0xeb;
    int OP_UNUSED_EC                    = 0xec;
    int OP_UNUSED_ED                    = 0xed;

    /* optimizer output -- these are never generated by "dx" */
    int OP_EXECUTE_INLINE               = 0xee;
    int OP_UNUSED_EF                    = 0xef; /* OP_EXECUTE_INLINE_RANGE? */

    int OP_INVOKE_DIRECT_EMPTY          = 0xf0;
    int OP_UNUSED_F1                    = 0xf1; /* OP_INVOKE_DIRECT_EMPTY_RANGE? */
    int OP_IGET_QUICK                   = 0xf2;
    int OP_IGET_WIDE_QUICK              = 0xf3;
    int OP_IGET_OBJECT_QUICK            = 0xf4;
    int OP_IPUT_QUICK                   = 0xf5;
    int OP_IPUT_WIDE_QUICK              = 0xf6;
    int OP_IPUT_OBJECT_QUICK            = 0xf7;

    int OP_INVOKE_VIRTUAL_QUICK         = 0xf8;
    int OP_INVOKE_VIRTUAL_QUICK_RANGE   = 0xf9;
    int OP_INVOKE_SUPER_QUICK           = 0xfa;
    int OP_INVOKE_SUPER_QUICK_RANGE     = 0xfb;
    int OP_UNUSED_FC                    = 0xfc; /* OP_INVOKE_DIRECT_QUICK? */
    int OP_UNUSED_FD                    = 0xfd; /* OP_INVOKE_DIRECT_QUICK_RANGE? */
    int OP_UNUSED_FE                    = 0xfe; /* OP_INVOKE_INTERFACE_QUICK? */
    int OP_UNUSED_FF                    = 0xff; /* OP_INVOKE_INTERFACE_QUICK_RANGE*/
}


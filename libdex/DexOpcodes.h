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
 * Dalvik opcode information.
 *
 * IMPORTANT NOTE: The contents of this file are mostly generated
 * automatically by the opcode-gen tool. Any edits to the generated
 * sections will get wiped out the next time the tool is run.
 *
 * See the file opcode-gen/README.txt for information about updating
 * opcodes and instruction formats.
 */

#ifndef LIBDEX_DEXOPCODES_H_
#define LIBDEX_DEXOPCODES_H_

#include "DexFile.h"

/*
 * kMaxOpcodeValue: the highest possible raw (unpacked) opcode value
 *
 * kNumPackedOpcodes: the highest possible packed opcode value of a
 * valid Dalvik opcode, plus one
 *
 * TODO: Change this once the rest of the code is prepared to deal with
 * extended opcodes.
 */
// BEGIN(libdex-maximum-values); GENERATED AUTOMATICALLY BY opcode-gen
#define kMaxOpcodeValue 0xffff
#define kNumPackedOpcodes 0x200
// END(libdex-maximum-values); GENERATED AUTOMATICALLY BY opcode-gen

/*
 * Switch table and array data signatures are a code unit consisting
 * of "NOP" (0x00) in the low-order byte and a non-zero identifying
 * code in the high-order byte. (A true NOP is 0x0000.)
 */
#define kPackedSwitchSignature  0x0100
#define kSparseSwitchSignature  0x0200
#define kArrayDataSignature     0x0300

/*
 * Enumeration of all Dalvik opcodes, where the enumeration value
 * associated with each is the corresponding packed opcode number.
 * This is different than the opcode value from the Dalvik bytecode
 * spec for opcode values >= 0xff; see dexOpcodeFromCodeUnit() below.
 *
 * A note about the "breakpoint" opcode. This instruction is special,
 * in that it should never be seen by anything but the debug
 * interpreter. During debugging it takes the place of an arbitrary
 * opcode, which means operations like "tell me the opcode width so I
 * can find the next instruction" aren't possible. (This is
 * correctable, but probably not useful.)
 */
enum Opcode {
    // BEGIN(libdex-opcode-enum); GENERATED AUTOMATICALLY BY opcode-gen
    OP_NOP                          = 0x00,
    OP_MOVE                         = 0x01,
    OP_MOVE_FROM16                  = 0x02,
    OP_MOVE_16                      = 0x03,
    OP_MOVE_WIDE                    = 0x04,
    OP_MOVE_WIDE_FROM16             = 0x05,
    OP_MOVE_WIDE_16                 = 0x06,
    OP_MOVE_OBJECT                  = 0x07,
    OP_MOVE_OBJECT_FROM16           = 0x08,
    OP_MOVE_OBJECT_16               = 0x09,
    OP_MOVE_RESULT                  = 0x0a,
    OP_MOVE_RESULT_WIDE             = 0x0b,
    OP_MOVE_RESULT_OBJECT           = 0x0c,
    OP_MOVE_EXCEPTION               = 0x0d,
    OP_RETURN_VOID                  = 0x0e,
    OP_RETURN                       = 0x0f,
    OP_RETURN_WIDE                  = 0x10,
    OP_RETURN_OBJECT                = 0x11,
    OP_CONST_4                      = 0x12,
    OP_CONST_16                     = 0x13,
    OP_CONST                        = 0x14,
    OP_CONST_HIGH16                 = 0x15,
    OP_CONST_WIDE_16                = 0x16,
    OP_CONST_WIDE_32                = 0x17,
    OP_CONST_WIDE                   = 0x18,
    OP_CONST_WIDE_HIGH16            = 0x19,
    OP_CONST_STRING                 = 0x1a,
    OP_CONST_STRING_JUMBO           = 0x1b,
    OP_CONST_CLASS                  = 0x1c,
    OP_MONITOR_ENTER                = 0x1d,
    OP_MONITOR_EXIT                 = 0x1e,
    OP_CHECK_CAST                   = 0x1f,
    OP_INSTANCE_OF                  = 0x20,
    OP_ARRAY_LENGTH                 = 0x21,
    OP_NEW_INSTANCE                 = 0x22,
    OP_NEW_ARRAY                    = 0x23,
    OP_FILLED_NEW_ARRAY             = 0x24,
    OP_FILLED_NEW_ARRAY_RANGE       = 0x25,
    OP_FILL_ARRAY_DATA              = 0x26,
    OP_THROW                        = 0x27,
    OP_GOTO                         = 0x28,
    OP_GOTO_16                      = 0x29,
    OP_GOTO_32                      = 0x2a,
    OP_PACKED_SWITCH                = 0x2b,
    OP_SPARSE_SWITCH                = 0x2c,
    OP_CMPL_FLOAT                   = 0x2d,
    OP_CMPG_FLOAT                   = 0x2e,
    OP_CMPL_DOUBLE                  = 0x2f,
    OP_CMPG_DOUBLE                  = 0x30,
    OP_CMP_LONG                     = 0x31,
    OP_IF_EQ                        = 0x32,
    OP_IF_NE                        = 0x33,
    OP_IF_LT                        = 0x34,
    OP_IF_GE                        = 0x35,
    OP_IF_GT                        = 0x36,
    OP_IF_LE                        = 0x37,
    OP_IF_EQZ                       = 0x38,
    OP_IF_NEZ                       = 0x39,
    OP_IF_LTZ                       = 0x3a,
    OP_IF_GEZ                       = 0x3b,
    OP_IF_GTZ                       = 0x3c,
    OP_IF_LEZ                       = 0x3d,
    OP_UNUSED_3E                    = 0x3e,
    OP_UNUSED_3F                    = 0x3f,
    OP_UNUSED_40                    = 0x40,
    OP_UNUSED_41                    = 0x41,
    OP_UNUSED_42                    = 0x42,
    OP_UNUSED_43                    = 0x43,
    OP_AGET                         = 0x44,
    OP_AGET_WIDE                    = 0x45,
    OP_AGET_OBJECT                  = 0x46,
    OP_AGET_BOOLEAN                 = 0x47,
    OP_AGET_BYTE                    = 0x48,
    OP_AGET_CHAR                    = 0x49,
    OP_AGET_SHORT                   = 0x4a,
    OP_APUT                         = 0x4b,
    OP_APUT_WIDE                    = 0x4c,
    OP_APUT_OBJECT                  = 0x4d,
    OP_APUT_BOOLEAN                 = 0x4e,
    OP_APUT_BYTE                    = 0x4f,
    OP_APUT_CHAR                    = 0x50,
    OP_APUT_SHORT                   = 0x51,
    OP_IGET                         = 0x52,
    OP_IGET_WIDE                    = 0x53,
    OP_IGET_OBJECT                  = 0x54,
    OP_IGET_BOOLEAN                 = 0x55,
    OP_IGET_BYTE                    = 0x56,
    OP_IGET_CHAR                    = 0x57,
    OP_IGET_SHORT                   = 0x58,
    OP_IPUT                         = 0x59,
    OP_IPUT_WIDE                    = 0x5a,
    OP_IPUT_OBJECT                  = 0x5b,
    OP_IPUT_BOOLEAN                 = 0x5c,
    OP_IPUT_BYTE                    = 0x5d,
    OP_IPUT_CHAR                    = 0x5e,
    OP_IPUT_SHORT                   = 0x5f,
    OP_SGET                         = 0x60,
    OP_SGET_WIDE                    = 0x61,
    OP_SGET_OBJECT                  = 0x62,
    OP_SGET_BOOLEAN                 = 0x63,
    OP_SGET_BYTE                    = 0x64,
    OP_SGET_CHAR                    = 0x65,
    OP_SGET_SHORT                   = 0x66,
    OP_SPUT                         = 0x67,
    OP_SPUT_WIDE                    = 0x68,
    OP_SPUT_OBJECT                  = 0x69,
    OP_SPUT_BOOLEAN                 = 0x6a,
    OP_SPUT_BYTE                    = 0x6b,
    OP_SPUT_CHAR                    = 0x6c,
    OP_SPUT_SHORT                   = 0x6d,
    OP_INVOKE_VIRTUAL               = 0x6e,
    OP_INVOKE_SUPER                 = 0x6f,
    OP_INVOKE_DIRECT                = 0x70,
    OP_INVOKE_STATIC                = 0x71,
    OP_INVOKE_INTERFACE             = 0x72,
    OP_UNUSED_73                    = 0x73,
    OP_INVOKE_VIRTUAL_RANGE         = 0x74,
    OP_INVOKE_SUPER_RANGE           = 0x75,
    OP_INVOKE_DIRECT_RANGE          = 0x76,
    OP_INVOKE_STATIC_RANGE          = 0x77,
    OP_INVOKE_INTERFACE_RANGE       = 0x78,
    OP_UNUSED_79                    = 0x79,
    OP_UNUSED_7A                    = 0x7a,
    OP_NEG_INT                      = 0x7b,
    OP_NOT_INT                      = 0x7c,
    OP_NEG_LONG                     = 0x7d,
    OP_NOT_LONG                     = 0x7e,
    OP_NEG_FLOAT                    = 0x7f,
    OP_NEG_DOUBLE                   = 0x80,
    OP_INT_TO_LONG                  = 0x81,
    OP_INT_TO_FLOAT                 = 0x82,
    OP_INT_TO_DOUBLE                = 0x83,
    OP_LONG_TO_INT                  = 0x84,
    OP_LONG_TO_FLOAT                = 0x85,
    OP_LONG_TO_DOUBLE               = 0x86,
    OP_FLOAT_TO_INT                 = 0x87,
    OP_FLOAT_TO_LONG                = 0x88,
    OP_FLOAT_TO_DOUBLE              = 0x89,
    OP_DOUBLE_TO_INT                = 0x8a,
    OP_DOUBLE_TO_LONG               = 0x8b,
    OP_DOUBLE_TO_FLOAT              = 0x8c,
    OP_INT_TO_BYTE                  = 0x8d,
    OP_INT_TO_CHAR                  = 0x8e,
    OP_INT_TO_SHORT                 = 0x8f,
    OP_ADD_INT                      = 0x90,
    OP_SUB_INT                      = 0x91,
    OP_MUL_INT                      = 0x92,
    OP_DIV_INT                      = 0x93,
    OP_REM_INT                      = 0x94,
    OP_AND_INT                      = 0x95,
    OP_OR_INT                       = 0x96,
    OP_XOR_INT                      = 0x97,
    OP_SHL_INT                      = 0x98,
    OP_SHR_INT                      = 0x99,
    OP_USHR_INT                     = 0x9a,
    OP_ADD_LONG                     = 0x9b,
    OP_SUB_LONG                     = 0x9c,
    OP_MUL_LONG                     = 0x9d,
    OP_DIV_LONG                     = 0x9e,
    OP_REM_LONG                     = 0x9f,
    OP_AND_LONG                     = 0xa0,
    OP_OR_LONG                      = 0xa1,
    OP_XOR_LONG                     = 0xa2,
    OP_SHL_LONG                     = 0xa3,
    OP_SHR_LONG                     = 0xa4,
    OP_USHR_LONG                    = 0xa5,
    OP_ADD_FLOAT                    = 0xa6,
    OP_SUB_FLOAT                    = 0xa7,
    OP_MUL_FLOAT                    = 0xa8,
    OP_DIV_FLOAT                    = 0xa9,
    OP_REM_FLOAT                    = 0xaa,
    OP_ADD_DOUBLE                   = 0xab,
    OP_SUB_DOUBLE                   = 0xac,
    OP_MUL_DOUBLE                   = 0xad,
    OP_DIV_DOUBLE                   = 0xae,
    OP_REM_DOUBLE                   = 0xaf,
    OP_ADD_INT_2ADDR                = 0xb0,
    OP_SUB_INT_2ADDR                = 0xb1,
    OP_MUL_INT_2ADDR                = 0xb2,
    OP_DIV_INT_2ADDR                = 0xb3,
    OP_REM_INT_2ADDR                = 0xb4,
    OP_AND_INT_2ADDR                = 0xb5,
    OP_OR_INT_2ADDR                 = 0xb6,
    OP_XOR_INT_2ADDR                = 0xb7,
    OP_SHL_INT_2ADDR                = 0xb8,
    OP_SHR_INT_2ADDR                = 0xb9,
    OP_USHR_INT_2ADDR               = 0xba,
    OP_ADD_LONG_2ADDR               = 0xbb,
    OP_SUB_LONG_2ADDR               = 0xbc,
    OP_MUL_LONG_2ADDR               = 0xbd,
    OP_DIV_LONG_2ADDR               = 0xbe,
    OP_REM_LONG_2ADDR               = 0xbf,
    OP_AND_LONG_2ADDR               = 0xc0,
    OP_OR_LONG_2ADDR                = 0xc1,
    OP_XOR_LONG_2ADDR               = 0xc2,
    OP_SHL_LONG_2ADDR               = 0xc3,
    OP_SHR_LONG_2ADDR               = 0xc4,
    OP_USHR_LONG_2ADDR              = 0xc5,
    OP_ADD_FLOAT_2ADDR              = 0xc6,
    OP_SUB_FLOAT_2ADDR              = 0xc7,
    OP_MUL_FLOAT_2ADDR              = 0xc8,
    OP_DIV_FLOAT_2ADDR              = 0xc9,
    OP_REM_FLOAT_2ADDR              = 0xca,
    OP_ADD_DOUBLE_2ADDR             = 0xcb,
    OP_SUB_DOUBLE_2ADDR             = 0xcc,
    OP_MUL_DOUBLE_2ADDR             = 0xcd,
    OP_DIV_DOUBLE_2ADDR             = 0xce,
    OP_REM_DOUBLE_2ADDR             = 0xcf,
    OP_ADD_INT_LIT16                = 0xd0,
    OP_RSUB_INT                     = 0xd1,
    OP_MUL_INT_LIT16                = 0xd2,
    OP_DIV_INT_LIT16                = 0xd3,
    OP_REM_INT_LIT16                = 0xd4,
    OP_AND_INT_LIT16                = 0xd5,
    OP_OR_INT_LIT16                 = 0xd6,
    OP_XOR_INT_LIT16                = 0xd7,
    OP_ADD_INT_LIT8                 = 0xd8,
    OP_RSUB_INT_LIT8                = 0xd9,
    OP_MUL_INT_LIT8                 = 0xda,
    OP_DIV_INT_LIT8                 = 0xdb,
    OP_REM_INT_LIT8                 = 0xdc,
    OP_AND_INT_LIT8                 = 0xdd,
    OP_OR_INT_LIT8                  = 0xde,
    OP_XOR_INT_LIT8                 = 0xdf,
    OP_SHL_INT_LIT8                 = 0xe0,
    OP_SHR_INT_LIT8                 = 0xe1,
    OP_USHR_INT_LIT8                = 0xe2,
    OP_IGET_VOLATILE                = 0xe3,
    OP_IPUT_VOLATILE                = 0xe4,
    OP_SGET_VOLATILE                = 0xe5,
    OP_SPUT_VOLATILE                = 0xe6,
    OP_IGET_OBJECT_VOLATILE         = 0xe7,
    OP_IGET_WIDE_VOLATILE           = 0xe8,
    OP_IPUT_WIDE_VOLATILE           = 0xe9,
    OP_SGET_WIDE_VOLATILE           = 0xea,
    OP_SPUT_WIDE_VOLATILE           = 0xeb,
    OP_BREAKPOINT                   = 0xec,
    OP_THROW_VERIFICATION_ERROR     = 0xed,
    OP_EXECUTE_INLINE               = 0xee,
    OP_EXECUTE_INLINE_RANGE         = 0xef,
    OP_INVOKE_OBJECT_INIT_RANGE     = 0xf0,
    OP_RETURN_VOID_BARRIER          = 0xf1,
    OP_IGET_QUICK                   = 0xf2,
    OP_IGET_WIDE_QUICK              = 0xf3,
    OP_IGET_OBJECT_QUICK            = 0xf4,
    OP_IPUT_QUICK                   = 0xf5,
    OP_IPUT_WIDE_QUICK              = 0xf6,
    OP_IPUT_OBJECT_QUICK            = 0xf7,
    OP_INVOKE_VIRTUAL_QUICK         = 0xf8,
    OP_INVOKE_VIRTUAL_QUICK_RANGE   = 0xf9,
    OP_INVOKE_SUPER_QUICK           = 0xfa,
    OP_INVOKE_SUPER_QUICK_RANGE     = 0xfb,
    OP_IPUT_OBJECT_VOLATILE         = 0xfc,
    OP_SGET_OBJECT_VOLATILE         = 0xfd,
    OP_SPUT_OBJECT_VOLATILE         = 0xfe,
    OP_DISPATCH_FF                  = 0xff,
    OP_CONST_CLASS_JUMBO            = 0x100,
    OP_CHECK_CAST_JUMBO             = 0x101,
    OP_INSTANCE_OF_JUMBO            = 0x102,
    OP_NEW_INSTANCE_JUMBO           = 0x103,
    OP_NEW_ARRAY_JUMBO              = 0x104,
    OP_FILLED_NEW_ARRAY_JUMBO       = 0x105,
    OP_IGET_JUMBO                   = 0x106,
    OP_IGET_WIDE_JUMBO              = 0x107,
    OP_IGET_OBJECT_JUMBO            = 0x108,
    OP_IGET_BOOLEAN_JUMBO           = 0x109,
    OP_IGET_BYTE_JUMBO              = 0x10a,
    OP_IGET_CHAR_JUMBO              = 0x10b,
    OP_IGET_SHORT_JUMBO             = 0x10c,
    OP_IPUT_JUMBO                   = 0x10d,
    OP_IPUT_WIDE_JUMBO              = 0x10e,
    OP_IPUT_OBJECT_JUMBO            = 0x10f,
    OP_IPUT_BOOLEAN_JUMBO           = 0x110,
    OP_IPUT_BYTE_JUMBO              = 0x111,
    OP_IPUT_CHAR_JUMBO              = 0x112,
    OP_IPUT_SHORT_JUMBO             = 0x113,
    OP_SGET_JUMBO                   = 0x114,
    OP_SGET_WIDE_JUMBO              = 0x115,
    OP_SGET_OBJECT_JUMBO            = 0x116,
    OP_SGET_BOOLEAN_JUMBO           = 0x117,
    OP_SGET_BYTE_JUMBO              = 0x118,
    OP_SGET_CHAR_JUMBO              = 0x119,
    OP_SGET_SHORT_JUMBO             = 0x11a,
    OP_SPUT_JUMBO                   = 0x11b,
    OP_SPUT_WIDE_JUMBO              = 0x11c,
    OP_SPUT_OBJECT_JUMBO            = 0x11d,
    OP_SPUT_BOOLEAN_JUMBO           = 0x11e,
    OP_SPUT_BYTE_JUMBO              = 0x11f,
    OP_SPUT_CHAR_JUMBO              = 0x120,
    OP_SPUT_SHORT_JUMBO             = 0x121,
    OP_INVOKE_VIRTUAL_JUMBO         = 0x122,
    OP_INVOKE_SUPER_JUMBO           = 0x123,
    OP_INVOKE_DIRECT_JUMBO          = 0x124,
    OP_INVOKE_STATIC_JUMBO          = 0x125,
    OP_INVOKE_INTERFACE_JUMBO       = 0x126,
    OP_UNUSED_27FF                  = 0x127,
    OP_UNUSED_28FF                  = 0x128,
    OP_UNUSED_29FF                  = 0x129,
    OP_UNUSED_2AFF                  = 0x12a,
    OP_UNUSED_2BFF                  = 0x12b,
    OP_UNUSED_2CFF                  = 0x12c,
    OP_UNUSED_2DFF                  = 0x12d,
    OP_UNUSED_2EFF                  = 0x12e,
    OP_UNUSED_2FFF                  = 0x12f,
    OP_UNUSED_30FF                  = 0x130,
    OP_UNUSED_31FF                  = 0x131,
    OP_UNUSED_32FF                  = 0x132,
    OP_UNUSED_33FF                  = 0x133,
    OP_UNUSED_34FF                  = 0x134,
    OP_UNUSED_35FF                  = 0x135,
    OP_UNUSED_36FF                  = 0x136,
    OP_UNUSED_37FF                  = 0x137,
    OP_UNUSED_38FF                  = 0x138,
    OP_UNUSED_39FF                  = 0x139,
    OP_UNUSED_3AFF                  = 0x13a,
    OP_UNUSED_3BFF                  = 0x13b,
    OP_UNUSED_3CFF                  = 0x13c,
    OP_UNUSED_3DFF                  = 0x13d,
    OP_UNUSED_3EFF                  = 0x13e,
    OP_UNUSED_3FFF                  = 0x13f,
    OP_UNUSED_40FF                  = 0x140,
    OP_UNUSED_41FF                  = 0x141,
    OP_UNUSED_42FF                  = 0x142,
    OP_UNUSED_43FF                  = 0x143,
    OP_UNUSED_44FF                  = 0x144,
    OP_UNUSED_45FF                  = 0x145,
    OP_UNUSED_46FF                  = 0x146,
    OP_UNUSED_47FF                  = 0x147,
    OP_UNUSED_48FF                  = 0x148,
    OP_UNUSED_49FF                  = 0x149,
    OP_UNUSED_4AFF                  = 0x14a,
    OP_UNUSED_4BFF                  = 0x14b,
    OP_UNUSED_4CFF                  = 0x14c,
    OP_UNUSED_4DFF                  = 0x14d,
    OP_UNUSED_4EFF                  = 0x14e,
    OP_UNUSED_4FFF                  = 0x14f,
    OP_UNUSED_50FF                  = 0x150,
    OP_UNUSED_51FF                  = 0x151,
    OP_UNUSED_52FF                  = 0x152,
    OP_UNUSED_53FF                  = 0x153,
    OP_UNUSED_54FF                  = 0x154,
    OP_UNUSED_55FF                  = 0x155,
    OP_UNUSED_56FF                  = 0x156,
    OP_UNUSED_57FF                  = 0x157,
    OP_UNUSED_58FF                  = 0x158,
    OP_UNUSED_59FF                  = 0x159,
    OP_UNUSED_5AFF                  = 0x15a,
    OP_UNUSED_5BFF                  = 0x15b,
    OP_UNUSED_5CFF                  = 0x15c,
    OP_UNUSED_5DFF                  = 0x15d,
    OP_UNUSED_5EFF                  = 0x15e,
    OP_UNUSED_5FFF                  = 0x15f,
    OP_UNUSED_60FF                  = 0x160,
    OP_UNUSED_61FF                  = 0x161,
    OP_UNUSED_62FF                  = 0x162,
    OP_UNUSED_63FF                  = 0x163,
    OP_UNUSED_64FF                  = 0x164,
    OP_UNUSED_65FF                  = 0x165,
    OP_UNUSED_66FF                  = 0x166,
    OP_UNUSED_67FF                  = 0x167,
    OP_UNUSED_68FF                  = 0x168,
    OP_UNUSED_69FF                  = 0x169,
    OP_UNUSED_6AFF                  = 0x16a,
    OP_UNUSED_6BFF                  = 0x16b,
    OP_UNUSED_6CFF                  = 0x16c,
    OP_UNUSED_6DFF                  = 0x16d,
    OP_UNUSED_6EFF                  = 0x16e,
    OP_UNUSED_6FFF                  = 0x16f,
    OP_UNUSED_70FF                  = 0x170,
    OP_UNUSED_71FF                  = 0x171,
    OP_UNUSED_72FF                  = 0x172,
    OP_UNUSED_73FF                  = 0x173,
    OP_UNUSED_74FF                  = 0x174,
    OP_UNUSED_75FF                  = 0x175,
    OP_UNUSED_76FF                  = 0x176,
    OP_UNUSED_77FF                  = 0x177,
    OP_UNUSED_78FF                  = 0x178,
    OP_UNUSED_79FF                  = 0x179,
    OP_UNUSED_7AFF                  = 0x17a,
    OP_UNUSED_7BFF                  = 0x17b,
    OP_UNUSED_7CFF                  = 0x17c,
    OP_UNUSED_7DFF                  = 0x17d,
    OP_UNUSED_7EFF                  = 0x17e,
    OP_UNUSED_7FFF                  = 0x17f,
    OP_UNUSED_80FF                  = 0x180,
    OP_UNUSED_81FF                  = 0x181,
    OP_UNUSED_82FF                  = 0x182,
    OP_UNUSED_83FF                  = 0x183,
    OP_UNUSED_84FF                  = 0x184,
    OP_UNUSED_85FF                  = 0x185,
    OP_UNUSED_86FF                  = 0x186,
    OP_UNUSED_87FF                  = 0x187,
    OP_UNUSED_88FF                  = 0x188,
    OP_UNUSED_89FF                  = 0x189,
    OP_UNUSED_8AFF                  = 0x18a,
    OP_UNUSED_8BFF                  = 0x18b,
    OP_UNUSED_8CFF                  = 0x18c,
    OP_UNUSED_8DFF                  = 0x18d,
    OP_UNUSED_8EFF                  = 0x18e,
    OP_UNUSED_8FFF                  = 0x18f,
    OP_UNUSED_90FF                  = 0x190,
    OP_UNUSED_91FF                  = 0x191,
    OP_UNUSED_92FF                  = 0x192,
    OP_UNUSED_93FF                  = 0x193,
    OP_UNUSED_94FF                  = 0x194,
    OP_UNUSED_95FF                  = 0x195,
    OP_UNUSED_96FF                  = 0x196,
    OP_UNUSED_97FF                  = 0x197,
    OP_UNUSED_98FF                  = 0x198,
    OP_UNUSED_99FF                  = 0x199,
    OP_UNUSED_9AFF                  = 0x19a,
    OP_UNUSED_9BFF                  = 0x19b,
    OP_UNUSED_9CFF                  = 0x19c,
    OP_UNUSED_9DFF                  = 0x19d,
    OP_UNUSED_9EFF                  = 0x19e,
    OP_UNUSED_9FFF                  = 0x19f,
    OP_UNUSED_A0FF                  = 0x1a0,
    OP_UNUSED_A1FF                  = 0x1a1,
    OP_UNUSED_A2FF                  = 0x1a2,
    OP_UNUSED_A3FF                  = 0x1a3,
    OP_UNUSED_A4FF                  = 0x1a4,
    OP_UNUSED_A5FF                  = 0x1a5,
    OP_UNUSED_A6FF                  = 0x1a6,
    OP_UNUSED_A7FF                  = 0x1a7,
    OP_UNUSED_A8FF                  = 0x1a8,
    OP_UNUSED_A9FF                  = 0x1a9,
    OP_UNUSED_AAFF                  = 0x1aa,
    OP_UNUSED_ABFF                  = 0x1ab,
    OP_UNUSED_ACFF                  = 0x1ac,
    OP_UNUSED_ADFF                  = 0x1ad,
    OP_UNUSED_AEFF                  = 0x1ae,
    OP_UNUSED_AFFF                  = 0x1af,
    OP_UNUSED_B0FF                  = 0x1b0,
    OP_UNUSED_B1FF                  = 0x1b1,
    OP_UNUSED_B2FF                  = 0x1b2,
    OP_UNUSED_B3FF                  = 0x1b3,
    OP_UNUSED_B4FF                  = 0x1b4,
    OP_UNUSED_B5FF                  = 0x1b5,
    OP_UNUSED_B6FF                  = 0x1b6,
    OP_UNUSED_B7FF                  = 0x1b7,
    OP_UNUSED_B8FF                  = 0x1b8,
    OP_UNUSED_B9FF                  = 0x1b9,
    OP_UNUSED_BAFF                  = 0x1ba,
    OP_UNUSED_BBFF                  = 0x1bb,
    OP_UNUSED_BCFF                  = 0x1bc,
    OP_UNUSED_BDFF                  = 0x1bd,
    OP_UNUSED_BEFF                  = 0x1be,
    OP_UNUSED_BFFF                  = 0x1bf,
    OP_UNUSED_C0FF                  = 0x1c0,
    OP_UNUSED_C1FF                  = 0x1c1,
    OP_UNUSED_C2FF                  = 0x1c2,
    OP_UNUSED_C3FF                  = 0x1c3,
    OP_UNUSED_C4FF                  = 0x1c4,
    OP_UNUSED_C5FF                  = 0x1c5,
    OP_UNUSED_C6FF                  = 0x1c6,
    OP_UNUSED_C7FF                  = 0x1c7,
    OP_UNUSED_C8FF                  = 0x1c8,
    OP_UNUSED_C9FF                  = 0x1c9,
    OP_UNUSED_CAFF                  = 0x1ca,
    OP_UNUSED_CBFF                  = 0x1cb,
    OP_UNUSED_CCFF                  = 0x1cc,
    OP_UNUSED_CDFF                  = 0x1cd,
    OP_UNUSED_CEFF                  = 0x1ce,
    OP_UNUSED_CFFF                  = 0x1cf,
    OP_UNUSED_D0FF                  = 0x1d0,
    OP_UNUSED_D1FF                  = 0x1d1,
    OP_UNUSED_D2FF                  = 0x1d2,
    OP_UNUSED_D3FF                  = 0x1d3,
    OP_UNUSED_D4FF                  = 0x1d4,
    OP_UNUSED_D5FF                  = 0x1d5,
    OP_UNUSED_D6FF                  = 0x1d6,
    OP_UNUSED_D7FF                  = 0x1d7,
    OP_UNUSED_D8FF                  = 0x1d8,
    OP_UNUSED_D9FF                  = 0x1d9,
    OP_UNUSED_DAFF                  = 0x1da,
    OP_UNUSED_DBFF                  = 0x1db,
    OP_UNUSED_DCFF                  = 0x1dc,
    OP_UNUSED_DDFF                  = 0x1dd,
    OP_UNUSED_DEFF                  = 0x1de,
    OP_UNUSED_DFFF                  = 0x1df,
    OP_UNUSED_E0FF                  = 0x1e0,
    OP_UNUSED_E1FF                  = 0x1e1,
    OP_UNUSED_E2FF                  = 0x1e2,
    OP_UNUSED_E3FF                  = 0x1e3,
    OP_UNUSED_E4FF                  = 0x1e4,
    OP_UNUSED_E5FF                  = 0x1e5,
    OP_UNUSED_E6FF                  = 0x1e6,
    OP_UNUSED_E7FF                  = 0x1e7,
    OP_UNUSED_E8FF                  = 0x1e8,
    OP_UNUSED_E9FF                  = 0x1e9,
    OP_UNUSED_EAFF                  = 0x1ea,
    OP_UNUSED_EBFF                  = 0x1eb,
    OP_UNUSED_ECFF                  = 0x1ec,
    OP_UNUSED_EDFF                  = 0x1ed,
    OP_UNUSED_EEFF                  = 0x1ee,
    OP_UNUSED_EFFF                  = 0x1ef,
    OP_UNUSED_F0FF                  = 0x1f0,
    OP_UNUSED_F1FF                  = 0x1f1,
    OP_INVOKE_OBJECT_INIT_JUMBO     = 0x1f2,
    OP_IGET_VOLATILE_JUMBO          = 0x1f3,
    OP_IGET_WIDE_VOLATILE_JUMBO     = 0x1f4,
    OP_IGET_OBJECT_VOLATILE_JUMBO   = 0x1f5,
    OP_IPUT_VOLATILE_JUMBO          = 0x1f6,
    OP_IPUT_WIDE_VOLATILE_JUMBO     = 0x1f7,
    OP_IPUT_OBJECT_VOLATILE_JUMBO   = 0x1f8,
    OP_SGET_VOLATILE_JUMBO          = 0x1f9,
    OP_SGET_WIDE_VOLATILE_JUMBO     = 0x1fa,
    OP_SGET_OBJECT_VOLATILE_JUMBO   = 0x1fb,
    OP_SPUT_VOLATILE_JUMBO          = 0x1fc,
    OP_SPUT_WIDE_VOLATILE_JUMBO     = 0x1fd,
    OP_SPUT_OBJECT_VOLATILE_JUMBO   = 0x1fe,
    OP_THROW_VERIFICATION_ERROR_JUMBO = 0x1ff,
    // END(libdex-opcode-enum)
};

/*
 * Macro used to generate a computed goto table for use in implementing
 * an interpreter in C.
 */
#define DEFINE_GOTO_TABLE(_name) \
    static const void* _name[kNumPackedOpcodes] = {                      \
        /* BEGIN(libdex-goto-table); GENERATED AUTOMATICALLY BY opcode-gen */ \
        H(OP_NOP),                                                            \
        H(OP_MOVE),                                                           \
        H(OP_MOVE_FROM16),                                                    \
        H(OP_MOVE_16),                                                        \
        H(OP_MOVE_WIDE),                                                      \
        H(OP_MOVE_WIDE_FROM16),                                               \
        H(OP_MOVE_WIDE_16),                                                   \
        H(OP_MOVE_OBJECT),                                                    \
        H(OP_MOVE_OBJECT_FROM16),                                             \
        H(OP_MOVE_OBJECT_16),                                                 \
        H(OP_MOVE_RESULT),                                                    \
        H(OP_MOVE_RESULT_WIDE),                                               \
        H(OP_MOVE_RESULT_OBJECT),                                             \
        H(OP_MOVE_EXCEPTION),                                                 \
        H(OP_RETURN_VOID),                                                    \
        H(OP_RETURN),                                                         \
        H(OP_RETURN_WIDE),                                                    \
        H(OP_RETURN_OBJECT),                                                  \
        H(OP_CONST_4),                                                        \
        H(OP_CONST_16),                                                       \
        H(OP_CONST),                                                          \
        H(OP_CONST_HIGH16),                                                   \
        H(OP_CONST_WIDE_16),                                                  \
        H(OP_CONST_WIDE_32),                                                  \
        H(OP_CONST_WIDE),                                                     \
        H(OP_CONST_WIDE_HIGH16),                                              \
        H(OP_CONST_STRING),                                                   \
        H(OP_CONST_STRING_JUMBO),                                             \
        H(OP_CONST_CLASS),                                                    \
        H(OP_MONITOR_ENTER),                                                  \
        H(OP_MONITOR_EXIT),                                                   \
        H(OP_CHECK_CAST),                                                     \
        H(OP_INSTANCE_OF),                                                    \
        H(OP_ARRAY_LENGTH),                                                   \
        H(OP_NEW_INSTANCE),                                                   \
        H(OP_NEW_ARRAY),                                                      \
        H(OP_FILLED_NEW_ARRAY),                                               \
        H(OP_FILLED_NEW_ARRAY_RANGE),                                         \
        H(OP_FILL_ARRAY_DATA),                                                \
        H(OP_THROW),                                                          \
        H(OP_GOTO),                                                           \
        H(OP_GOTO_16),                                                        \
        H(OP_GOTO_32),                                                        \
        H(OP_PACKED_SWITCH),                                                  \
        H(OP_SPARSE_SWITCH),                                                  \
        H(OP_CMPL_FLOAT),                                                     \
        H(OP_CMPG_FLOAT),                                                     \
        H(OP_CMPL_DOUBLE),                                                    \
        H(OP_CMPG_DOUBLE),                                                    \
        H(OP_CMP_LONG),                                                       \
        H(OP_IF_EQ),                                                          \
        H(OP_IF_NE),                                                          \
        H(OP_IF_LT),                                                          \
        H(OP_IF_GE),                                                          \
        H(OP_IF_GT),                                                          \
        H(OP_IF_LE),                                                          \
        H(OP_IF_EQZ),                                                         \
        H(OP_IF_NEZ),                                                         \
        H(OP_IF_LTZ),                                                         \
        H(OP_IF_GEZ),                                                         \
        H(OP_IF_GTZ),                                                         \
        H(OP_IF_LEZ),                                                         \
        H(OP_UNUSED_3E),                                                      \
        H(OP_UNUSED_3F),                                                      \
        H(OP_UNUSED_40),                                                      \
        H(OP_UNUSED_41),                                                      \
        H(OP_UNUSED_42),                                                      \
        H(OP_UNUSED_43),                                                      \
        H(OP_AGET),                                                           \
        H(OP_AGET_WIDE),                                                      \
        H(OP_AGET_OBJECT),                                                    \
        H(OP_AGET_BOOLEAN),                                                   \
        H(OP_AGET_BYTE),                                                      \
        H(OP_AGET_CHAR),                                                      \
        H(OP_AGET_SHORT),                                                     \
        H(OP_APUT),                                                           \
        H(OP_APUT_WIDE),                                                      \
        H(OP_APUT_OBJECT),                                                    \
        H(OP_APUT_BOOLEAN),                                                   \
        H(OP_APUT_BYTE),                                                      \
        H(OP_APUT_CHAR),                                                      \
        H(OP_APUT_SHORT),                                                     \
        H(OP_IGET),                                                           \
        H(OP_IGET_WIDE),                                                      \
        H(OP_IGET_OBJECT),                                                    \
        H(OP_IGET_BOOLEAN),                                                   \
        H(OP_IGET_BYTE),                                                      \
        H(OP_IGET_CHAR),                                                      \
        H(OP_IGET_SHORT),                                                     \
        H(OP_IPUT),                                                           \
        H(OP_IPUT_WIDE),                                                      \
        H(OP_IPUT_OBJECT),                                                    \
        H(OP_IPUT_BOOLEAN),                                                   \
        H(OP_IPUT_BYTE),                                                      \
        H(OP_IPUT_CHAR),                                                      \
        H(OP_IPUT_SHORT),                                                     \
        H(OP_SGET),                                                           \
        H(OP_SGET_WIDE),                                                      \
        H(OP_SGET_OBJECT),                                                    \
        H(OP_SGET_BOOLEAN),                                                   \
        H(OP_SGET_BYTE),                                                      \
        H(OP_SGET_CHAR),                                                      \
        H(OP_SGET_SHORT),                                                     \
        H(OP_SPUT),                                                           \
        H(OP_SPUT_WIDE),                                                      \
        H(OP_SPUT_OBJECT),                                                    \
        H(OP_SPUT_BOOLEAN),                                                   \
        H(OP_SPUT_BYTE),                                                      \
        H(OP_SPUT_CHAR),                                                      \
        H(OP_SPUT_SHORT),                                                     \
        H(OP_INVOKE_VIRTUAL),                                                 \
        H(OP_INVOKE_SUPER),                                                   \
        H(OP_INVOKE_DIRECT),                                                  \
        H(OP_INVOKE_STATIC),                                                  \
        H(OP_INVOKE_INTERFACE),                                               \
        H(OP_UNUSED_73),                                                      \
        H(OP_INVOKE_VIRTUAL_RANGE),                                           \
        H(OP_INVOKE_SUPER_RANGE),                                             \
        H(OP_INVOKE_DIRECT_RANGE),                                            \
        H(OP_INVOKE_STATIC_RANGE),                                            \
        H(OP_INVOKE_INTERFACE_RANGE),                                         \
        H(OP_UNUSED_79),                                                      \
        H(OP_UNUSED_7A),                                                      \
        H(OP_NEG_INT),                                                        \
        H(OP_NOT_INT),                                                        \
        H(OP_NEG_LONG),                                                       \
        H(OP_NOT_LONG),                                                       \
        H(OP_NEG_FLOAT),                                                      \
        H(OP_NEG_DOUBLE),                                                     \
        H(OP_INT_TO_LONG),                                                    \
        H(OP_INT_TO_FLOAT),                                                   \
        H(OP_INT_TO_DOUBLE),                                                  \
        H(OP_LONG_TO_INT),                                                    \
        H(OP_LONG_TO_FLOAT),                                                  \
        H(OP_LONG_TO_DOUBLE),                                                 \
        H(OP_FLOAT_TO_INT),                                                   \
        H(OP_FLOAT_TO_LONG),                                                  \
        H(OP_FLOAT_TO_DOUBLE),                                                \
        H(OP_DOUBLE_TO_INT),                                                  \
        H(OP_DOUBLE_TO_LONG),                                                 \
        H(OP_DOUBLE_TO_FLOAT),                                                \
        H(OP_INT_TO_BYTE),                                                    \
        H(OP_INT_TO_CHAR),                                                    \
        H(OP_INT_TO_SHORT),                                                   \
        H(OP_ADD_INT),                                                        \
        H(OP_SUB_INT),                                                        \
        H(OP_MUL_INT),                                                        \
        H(OP_DIV_INT),                                                        \
        H(OP_REM_INT),                                                        \
        H(OP_AND_INT),                                                        \
        H(OP_OR_INT),                                                         \
        H(OP_XOR_INT),                                                        \
        H(OP_SHL_INT),                                                        \
        H(OP_SHR_INT),                                                        \
        H(OP_USHR_INT),                                                       \
        H(OP_ADD_LONG),                                                       \
        H(OP_SUB_LONG),                                                       \
        H(OP_MUL_LONG),                                                       \
        H(OP_DIV_LONG),                                                       \
        H(OP_REM_LONG),                                                       \
        H(OP_AND_LONG),                                                       \
        H(OP_OR_LONG),                                                        \
        H(OP_XOR_LONG),                                                       \
        H(OP_SHL_LONG),                                                       \
        H(OP_SHR_LONG),                                                       \
        H(OP_USHR_LONG),                                                      \
        H(OP_ADD_FLOAT),                                                      \
        H(OP_SUB_FLOAT),                                                      \
        H(OP_MUL_FLOAT),                                                      \
        H(OP_DIV_FLOAT),                                                      \
        H(OP_REM_FLOAT),                                                      \
        H(OP_ADD_DOUBLE),                                                     \
        H(OP_SUB_DOUBLE),                                                     \
        H(OP_MUL_DOUBLE),                                                     \
        H(OP_DIV_DOUBLE),                                                     \
        H(OP_REM_DOUBLE),                                                     \
        H(OP_ADD_INT_2ADDR),                                                  \
        H(OP_SUB_INT_2ADDR),                                                  \
        H(OP_MUL_INT_2ADDR),                                                  \
        H(OP_DIV_INT_2ADDR),                                                  \
        H(OP_REM_INT_2ADDR),                                                  \
        H(OP_AND_INT_2ADDR),                                                  \
        H(OP_OR_INT_2ADDR),                                                   \
        H(OP_XOR_INT_2ADDR),                                                  \
        H(OP_SHL_INT_2ADDR),                                                  \
        H(OP_SHR_INT_2ADDR),                                                  \
        H(OP_USHR_INT_2ADDR),                                                 \
        H(OP_ADD_LONG_2ADDR),                                                 \
        H(OP_SUB_LONG_2ADDR),                                                 \
        H(OP_MUL_LONG_2ADDR),                                                 \
        H(OP_DIV_LONG_2ADDR),                                                 \
        H(OP_REM_LONG_2ADDR),                                                 \
        H(OP_AND_LONG_2ADDR),                                                 \
        H(OP_OR_LONG_2ADDR),                                                  \
        H(OP_XOR_LONG_2ADDR),                                                 \
        H(OP_SHL_LONG_2ADDR),                                                 \
        H(OP_SHR_LONG_2ADDR),                                                 \
        H(OP_USHR_LONG_2ADDR),                                                \
        H(OP_ADD_FLOAT_2ADDR),                                                \
        H(OP_SUB_FLOAT_2ADDR),                                                \
        H(OP_MUL_FLOAT_2ADDR),                                                \
        H(OP_DIV_FLOAT_2ADDR),                                                \
        H(OP_REM_FLOAT_2ADDR),                                                \
        H(OP_ADD_DOUBLE_2ADDR),                                               \
        H(OP_SUB_DOUBLE_2ADDR),                                               \
        H(OP_MUL_DOUBLE_2ADDR),                                               \
        H(OP_DIV_DOUBLE_2ADDR),                                               \
        H(OP_REM_DOUBLE_2ADDR),                                               \
        H(OP_ADD_INT_LIT16),                                                  \
        H(OP_RSUB_INT),                                                       \
        H(OP_MUL_INT_LIT16),                                                  \
        H(OP_DIV_INT_LIT16),                                                  \
        H(OP_REM_INT_LIT16),                                                  \
        H(OP_AND_INT_LIT16),                                                  \
        H(OP_OR_INT_LIT16),                                                   \
        H(OP_XOR_INT_LIT16),                                                  \
        H(OP_ADD_INT_LIT8),                                                   \
        H(OP_RSUB_INT_LIT8),                                                  \
        H(OP_MUL_INT_LIT8),                                                   \
        H(OP_DIV_INT_LIT8),                                                   \
        H(OP_REM_INT_LIT8),                                                   \
        H(OP_AND_INT_LIT8),                                                   \
        H(OP_OR_INT_LIT8),                                                    \
        H(OP_XOR_INT_LIT8),                                                   \
        H(OP_SHL_INT_LIT8),                                                   \
        H(OP_SHR_INT_LIT8),                                                   \
        H(OP_USHR_INT_LIT8),                                                  \
        H(OP_IGET_VOLATILE),                                                  \
        H(OP_IPUT_VOLATILE),                                                  \
        H(OP_SGET_VOLATILE),                                                  \
        H(OP_SPUT_VOLATILE),                                                  \
        H(OP_IGET_OBJECT_VOLATILE),                                           \
        H(OP_IGET_WIDE_VOLATILE),                                             \
        H(OP_IPUT_WIDE_VOLATILE),                                             \
        H(OP_SGET_WIDE_VOLATILE),                                             \
        H(OP_SPUT_WIDE_VOLATILE),                                             \
        H(OP_BREAKPOINT),                                                     \
        H(OP_THROW_VERIFICATION_ERROR),                                       \
        H(OP_EXECUTE_INLINE),                                                 \
        H(OP_EXECUTE_INLINE_RANGE),                                           \
        H(OP_INVOKE_OBJECT_INIT_RANGE),                                       \
        H(OP_RETURN_VOID_BARRIER),                                            \
        H(OP_IGET_QUICK),                                                     \
        H(OP_IGET_WIDE_QUICK),                                                \
        H(OP_IGET_OBJECT_QUICK),                                              \
        H(OP_IPUT_QUICK),                                                     \
        H(OP_IPUT_WIDE_QUICK),                                                \
        H(OP_IPUT_OBJECT_QUICK),                                              \
        H(OP_INVOKE_VIRTUAL_QUICK),                                           \
        H(OP_INVOKE_VIRTUAL_QUICK_RANGE),                                     \
        H(OP_INVOKE_SUPER_QUICK),                                             \
        H(OP_INVOKE_SUPER_QUICK_RANGE),                                       \
        H(OP_IPUT_OBJECT_VOLATILE),                                           \
        H(OP_SGET_OBJECT_VOLATILE),                                           \
        H(OP_SPUT_OBJECT_VOLATILE),                                           \
        H(OP_DISPATCH_FF),                                                    \
        H(OP_CONST_CLASS_JUMBO),                                              \
        H(OP_CHECK_CAST_JUMBO),                                               \
        H(OP_INSTANCE_OF_JUMBO),                                              \
        H(OP_NEW_INSTANCE_JUMBO),                                             \
        H(OP_NEW_ARRAY_JUMBO),                                                \
        H(OP_FILLED_NEW_ARRAY_JUMBO),                                         \
        H(OP_IGET_JUMBO),                                                     \
        H(OP_IGET_WIDE_JUMBO),                                                \
        H(OP_IGET_OBJECT_JUMBO),                                              \
        H(OP_IGET_BOOLEAN_JUMBO),                                             \
        H(OP_IGET_BYTE_JUMBO),                                                \
        H(OP_IGET_CHAR_JUMBO),                                                \
        H(OP_IGET_SHORT_JUMBO),                                               \
        H(OP_IPUT_JUMBO),                                                     \
        H(OP_IPUT_WIDE_JUMBO),                                                \
        H(OP_IPUT_OBJECT_JUMBO),                                              \
        H(OP_IPUT_BOOLEAN_JUMBO),                                             \
        H(OP_IPUT_BYTE_JUMBO),                                                \
        H(OP_IPUT_CHAR_JUMBO),                                                \
        H(OP_IPUT_SHORT_JUMBO),                                               \
        H(OP_SGET_JUMBO),                                                     \
        H(OP_SGET_WIDE_JUMBO),                                                \
        H(OP_SGET_OBJECT_JUMBO),                                              \
        H(OP_SGET_BOOLEAN_JUMBO),                                             \
        H(OP_SGET_BYTE_JUMBO),                                                \
        H(OP_SGET_CHAR_JUMBO),                                                \
        H(OP_SGET_SHORT_JUMBO),                                               \
        H(OP_SPUT_JUMBO),                                                     \
        H(OP_SPUT_WIDE_JUMBO),                                                \
        H(OP_SPUT_OBJECT_JUMBO),                                              \
        H(OP_SPUT_BOOLEAN_JUMBO),                                             \
        H(OP_SPUT_BYTE_JUMBO),                                                \
        H(OP_SPUT_CHAR_JUMBO),                                                \
        H(OP_SPUT_SHORT_JUMBO),                                               \
        H(OP_INVOKE_VIRTUAL_JUMBO),                                           \
        H(OP_INVOKE_SUPER_JUMBO),                                             \
        H(OP_INVOKE_DIRECT_JUMBO),                                            \
        H(OP_INVOKE_STATIC_JUMBO),                                            \
        H(OP_INVOKE_INTERFACE_JUMBO),                                         \
        H(OP_UNUSED_27FF),                                                    \
        H(OP_UNUSED_28FF),                                                    \
        H(OP_UNUSED_29FF),                                                    \
        H(OP_UNUSED_2AFF),                                                    \
        H(OP_UNUSED_2BFF),                                                    \
        H(OP_UNUSED_2CFF),                                                    \
        H(OP_UNUSED_2DFF),                                                    \
        H(OP_UNUSED_2EFF),                                                    \
        H(OP_UNUSED_2FFF),                                                    \
        H(OP_UNUSED_30FF),                                                    \
        H(OP_UNUSED_31FF),                                                    \
        H(OP_UNUSED_32FF),                                                    \
        H(OP_UNUSED_33FF),                                                    \
        H(OP_UNUSED_34FF),                                                    \
        H(OP_UNUSED_35FF),                                                    \
        H(OP_UNUSED_36FF),                                                    \
        H(OP_UNUSED_37FF),                                                    \
        H(OP_UNUSED_38FF),                                                    \
        H(OP_UNUSED_39FF),                                                    \
        H(OP_UNUSED_3AFF),                                                    \
        H(OP_UNUSED_3BFF),                                                    \
        H(OP_UNUSED_3CFF),                                                    \
        H(OP_UNUSED_3DFF),                                                    \
        H(OP_UNUSED_3EFF),                                                    \
        H(OP_UNUSED_3FFF),                                                    \
        H(OP_UNUSED_40FF),                                                    \
        H(OP_UNUSED_41FF),                                                    \
        H(OP_UNUSED_42FF),                                                    \
        H(OP_UNUSED_43FF),                                                    \
        H(OP_UNUSED_44FF),                                                    \
        H(OP_UNUSED_45FF),                                                    \
        H(OP_UNUSED_46FF),                                                    \
        H(OP_UNUSED_47FF),                                                    \
        H(OP_UNUSED_48FF),                                                    \
        H(OP_UNUSED_49FF),                                                    \
        H(OP_UNUSED_4AFF),                                                    \
        H(OP_UNUSED_4BFF),                                                    \
        H(OP_UNUSED_4CFF),                                                    \
        H(OP_UNUSED_4DFF),                                                    \
        H(OP_UNUSED_4EFF),                                                    \
        H(OP_UNUSED_4FFF),                                                    \
        H(OP_UNUSED_50FF),                                                    \
        H(OP_UNUSED_51FF),                                                    \
        H(OP_UNUSED_52FF),                                                    \
        H(OP_UNUSED_53FF),                                                    \
        H(OP_UNUSED_54FF),                                                    \
        H(OP_UNUSED_55FF),                                                    \
        H(OP_UNUSED_56FF),                                                    \
        H(OP_UNUSED_57FF),                                                    \
        H(OP_UNUSED_58FF),                                                    \
        H(OP_UNUSED_59FF),                                                    \
        H(OP_UNUSED_5AFF),                                                    \
        H(OP_UNUSED_5BFF),                                                    \
        H(OP_UNUSED_5CFF),                                                    \
        H(OP_UNUSED_5DFF),                                                    \
        H(OP_UNUSED_5EFF),                                                    \
        H(OP_UNUSED_5FFF),                                                    \
        H(OP_UNUSED_60FF),                                                    \
        H(OP_UNUSED_61FF),                                                    \
        H(OP_UNUSED_62FF),                                                    \
        H(OP_UNUSED_63FF),                                                    \
        H(OP_UNUSED_64FF),                                                    \
        H(OP_UNUSED_65FF),                                                    \
        H(OP_UNUSED_66FF),                                                    \
        H(OP_UNUSED_67FF),                                                    \
        H(OP_UNUSED_68FF),                                                    \
        H(OP_UNUSED_69FF),                                                    \
        H(OP_UNUSED_6AFF),                                                    \
        H(OP_UNUSED_6BFF),                                                    \
        H(OP_UNUSED_6CFF),                                                    \
        H(OP_UNUSED_6DFF),                                                    \
        H(OP_UNUSED_6EFF),                                                    \
        H(OP_UNUSED_6FFF),                                                    \
        H(OP_UNUSED_70FF),                                                    \
        H(OP_UNUSED_71FF),                                                    \
        H(OP_UNUSED_72FF),                                                    \
        H(OP_UNUSED_73FF),                                                    \
        H(OP_UNUSED_74FF),                                                    \
        H(OP_UNUSED_75FF),                                                    \
        H(OP_UNUSED_76FF),                                                    \
        H(OP_UNUSED_77FF),                                                    \
        H(OP_UNUSED_78FF),                                                    \
        H(OP_UNUSED_79FF),                                                    \
        H(OP_UNUSED_7AFF),                                                    \
        H(OP_UNUSED_7BFF),                                                    \
        H(OP_UNUSED_7CFF),                                                    \
        H(OP_UNUSED_7DFF),                                                    \
        H(OP_UNUSED_7EFF),                                                    \
        H(OP_UNUSED_7FFF),                                                    \
        H(OP_UNUSED_80FF),                                                    \
        H(OP_UNUSED_81FF),                                                    \
        H(OP_UNUSED_82FF),                                                    \
        H(OP_UNUSED_83FF),                                                    \
        H(OP_UNUSED_84FF),                                                    \
        H(OP_UNUSED_85FF),                                                    \
        H(OP_UNUSED_86FF),                                                    \
        H(OP_UNUSED_87FF),                                                    \
        H(OP_UNUSED_88FF),                                                    \
        H(OP_UNUSED_89FF),                                                    \
        H(OP_UNUSED_8AFF),                                                    \
        H(OP_UNUSED_8BFF),                                                    \
        H(OP_UNUSED_8CFF),                                                    \
        H(OP_UNUSED_8DFF),                                                    \
        H(OP_UNUSED_8EFF),                                                    \
        H(OP_UNUSED_8FFF),                                                    \
        H(OP_UNUSED_90FF),                                                    \
        H(OP_UNUSED_91FF),                                                    \
        H(OP_UNUSED_92FF),                                                    \
        H(OP_UNUSED_93FF),                                                    \
        H(OP_UNUSED_94FF),                                                    \
        H(OP_UNUSED_95FF),                                                    \
        H(OP_UNUSED_96FF),                                                    \
        H(OP_UNUSED_97FF),                                                    \
        H(OP_UNUSED_98FF),                                                    \
        H(OP_UNUSED_99FF),                                                    \
        H(OP_UNUSED_9AFF),                                                    \
        H(OP_UNUSED_9BFF),                                                    \
        H(OP_UNUSED_9CFF),                                                    \
        H(OP_UNUSED_9DFF),                                                    \
        H(OP_UNUSED_9EFF),                                                    \
        H(OP_UNUSED_9FFF),                                                    \
        H(OP_UNUSED_A0FF),                                                    \
        H(OP_UNUSED_A1FF),                                                    \
        H(OP_UNUSED_A2FF),                                                    \
        H(OP_UNUSED_A3FF),                                                    \
        H(OP_UNUSED_A4FF),                                                    \
        H(OP_UNUSED_A5FF),                                                    \
        H(OP_UNUSED_A6FF),                                                    \
        H(OP_UNUSED_A7FF),                                                    \
        H(OP_UNUSED_A8FF),                                                    \
        H(OP_UNUSED_A9FF),                                                    \
        H(OP_UNUSED_AAFF),                                                    \
        H(OP_UNUSED_ABFF),                                                    \
        H(OP_UNUSED_ACFF),                                                    \
        H(OP_UNUSED_ADFF),                                                    \
        H(OP_UNUSED_AEFF),                                                    \
        H(OP_UNUSED_AFFF),                                                    \
        H(OP_UNUSED_B0FF),                                                    \
        H(OP_UNUSED_B1FF),                                                    \
        H(OP_UNUSED_B2FF),                                                    \
        H(OP_UNUSED_B3FF),                                                    \
        H(OP_UNUSED_B4FF),                                                    \
        H(OP_UNUSED_B5FF),                                                    \
        H(OP_UNUSED_B6FF),                                                    \
        H(OP_UNUSED_B7FF),                                                    \
        H(OP_UNUSED_B8FF),                                                    \
        H(OP_UNUSED_B9FF),                                                    \
        H(OP_UNUSED_BAFF),                                                    \
        H(OP_UNUSED_BBFF),                                                    \
        H(OP_UNUSED_BCFF),                                                    \
        H(OP_UNUSED_BDFF),                                                    \
        H(OP_UNUSED_BEFF),                                                    \
        H(OP_UNUSED_BFFF),                                                    \
        H(OP_UNUSED_C0FF),                                                    \
        H(OP_UNUSED_C1FF),                                                    \
        H(OP_UNUSED_C2FF),                                                    \
        H(OP_UNUSED_C3FF),                                                    \
        H(OP_UNUSED_C4FF),                                                    \
        H(OP_UNUSED_C5FF),                                                    \
        H(OP_UNUSED_C6FF),                                                    \
        H(OP_UNUSED_C7FF),                                                    \
        H(OP_UNUSED_C8FF),                                                    \
        H(OP_UNUSED_C9FF),                                                    \
        H(OP_UNUSED_CAFF),                                                    \
        H(OP_UNUSED_CBFF),                                                    \
        H(OP_UNUSED_CCFF),                                                    \
        H(OP_UNUSED_CDFF),                                                    \
        H(OP_UNUSED_CEFF),                                                    \
        H(OP_UNUSED_CFFF),                                                    \
        H(OP_UNUSED_D0FF),                                                    \
        H(OP_UNUSED_D1FF),                                                    \
        H(OP_UNUSED_D2FF),                                                    \
        H(OP_UNUSED_D3FF),                                                    \
        H(OP_UNUSED_D4FF),                                                    \
        H(OP_UNUSED_D5FF),                                                    \
        H(OP_UNUSED_D6FF),                                                    \
        H(OP_UNUSED_D7FF),                                                    \
        H(OP_UNUSED_D8FF),                                                    \
        H(OP_UNUSED_D9FF),                                                    \
        H(OP_UNUSED_DAFF),                                                    \
        H(OP_UNUSED_DBFF),                                                    \
        H(OP_UNUSED_DCFF),                                                    \
        H(OP_UNUSED_DDFF),                                                    \
        H(OP_UNUSED_DEFF),                                                    \
        H(OP_UNUSED_DFFF),                                                    \
        H(OP_UNUSED_E0FF),                                                    \
        H(OP_UNUSED_E1FF),                                                    \
        H(OP_UNUSED_E2FF),                                                    \
        H(OP_UNUSED_E3FF),                                                    \
        H(OP_UNUSED_E4FF),                                                    \
        H(OP_UNUSED_E5FF),                                                    \
        H(OP_UNUSED_E6FF),                                                    \
        H(OP_UNUSED_E7FF),                                                    \
        H(OP_UNUSED_E8FF),                                                    \
        H(OP_UNUSED_E9FF),                                                    \
        H(OP_UNUSED_EAFF),                                                    \
        H(OP_UNUSED_EBFF),                                                    \
        H(OP_UNUSED_ECFF),                                                    \
        H(OP_UNUSED_EDFF),                                                    \
        H(OP_UNUSED_EEFF),                                                    \
        H(OP_UNUSED_EFFF),                                                    \
        H(OP_UNUSED_F0FF),                                                    \
        H(OP_UNUSED_F1FF),                                                    \
        H(OP_INVOKE_OBJECT_INIT_JUMBO),                                       \
        H(OP_IGET_VOLATILE_JUMBO),                                            \
        H(OP_IGET_WIDE_VOLATILE_JUMBO),                                       \
        H(OP_IGET_OBJECT_VOLATILE_JUMBO),                                     \
        H(OP_IPUT_VOLATILE_JUMBO),                                            \
        H(OP_IPUT_WIDE_VOLATILE_JUMBO),                                       \
        H(OP_IPUT_OBJECT_VOLATILE_JUMBO),                                     \
        H(OP_SGET_VOLATILE_JUMBO),                                            \
        H(OP_SGET_WIDE_VOLATILE_JUMBO),                                       \
        H(OP_SGET_OBJECT_VOLATILE_JUMBO),                                     \
        H(OP_SPUT_VOLATILE_JUMBO),                                            \
        H(OP_SPUT_WIDE_VOLATILE_JUMBO),                                       \
        H(OP_SPUT_OBJECT_VOLATILE_JUMBO),                                     \
        H(OP_THROW_VERIFICATION_ERROR_JUMBO),                                 \
        /* END(libdex-goto-table) */                                          \
    };

/*
 * Return the Opcode for a given raw opcode code unit (which may
 * include data payload). The packed index is a zero-based index which
 * can be used to point into various opcode-related tables. The Dalvik
 * opcode space is inherently sparse, in that the opcode unit is 16
 * bits wide, but for most opcodes, eight of those bits are for data.
 */
DEX_INLINE Opcode dexOpcodeFromCodeUnit(u2 codeUnit) {
    /*
     * This will want to become table-driven should the opcode layout
     * get more complicated.
     *
     * Note: This has to match the corresponding code in opcode-gen, so
     * that data tables get generated in a consistent way.
     */
    int lowByte = codeUnit & 0xff;
    if (lowByte != 0xff) {
        return (Opcode) lowByte;
    } else {
        return (Opcode) ((codeUnit >> 8) | 0x100);
    }
}

/*
 * Return the name of an opcode.
 */
const char* dexGetOpcodeName(Opcode op);

#endif  // LIBDEX_DEXOPCODES_H_

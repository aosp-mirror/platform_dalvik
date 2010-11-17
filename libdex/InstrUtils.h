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
 * Dalvik instruction utility functions.
 */
#ifndef _LIBDEX_INSTRUTILS
#define _LIBDEX_INSTRUTILS

#include "DexFile.h"
#include "OpCode.h"

/*
 * Dalvik-defined instruction formats.
 *
 * (This defines InstructionFormat as an unsigned char to reduce the size
 * of the table.  This isn't necessary with some compilers, which use an
 * integer width appropriate for the number of enum values.)
 *
 * If you add or delete a format, you have to change some or all of:
 *  - this enum
 *  - the switch inside dexDecodeInstruction() in InstrUtils.c
 *  - the switch inside dumpInstruction() in DexDump.c
 *  - the switch inside dvmCompilerMIR2LIR() in CodegenDriver.c
 */
typedef unsigned char InstructionFormat;
enum InstructionFormat {
    kFmt00x = 0,    // unknown format (also used for "breakpoint" opcode)
    kFmt10x,        // op
    kFmt12x,        // op vA, vB
    kFmt11n,        // op vA, #+B
    kFmt11x,        // op vAA
    kFmt10t,        // op +AA
    kFmt20bc,       // [opt] op AA, thing@BBBB
    kFmt20t,        // op +AAAA
    kFmt22x,        // op vAA, vBBBB
    kFmt21t,        // op vAA, +BBBB
    kFmt21s,        // op vAA, #+BBBB
    kFmt21h,        // op vAA, #+BBBB00000[00000000]
    kFmt21c,        // op vAA, thing@BBBB
    kFmt23x,        // op vAA, vBB, vCC
    kFmt22b,        // op vAA, vBB, #+CC
    kFmt22t,        // op vA, vB, +CCCC
    kFmt22s,        // op vA, vB, #+CCCC
    kFmt22c,        // op vA, vB, thing@CCCC
    kFmt22cs,       // [opt] op vA, vB, field offset CCCC
    kFmt30t,        // op +AAAAAAAA
    kFmt32x,        // op vAAAA, vBBBB
    kFmt31i,        // op vAA, #+BBBBBBBB
    kFmt31t,        // op vAA, +BBBBBBBB
    kFmt31c,        // op vAA, string@BBBBBBBB
    kFmt35c,        // op {vC,vD,vE,vF,vG}, thing@BBBB
    kFmt35ms,       // [opt] invoke-virtual+super
    kFmt3rc,        // op {vCCCC .. v(CCCC+AA-1)}, thing@BBBB
    kFmt3rms,       // [opt] invoke-virtual+super/range
    kFmt51l,        // op vAA, #+BBBBBBBBBBBBBBBB
    kFmt35mi,       // [opt] inline invoke
    kFmt3rmi,       // [opt] inline invoke/range
    kFmt33x,        // exop vAA, vBB, vCCCC
    kFmt32s,        // exop vAA, vBB, #+CCCC
    kFmt41c,        // exop vAAAA, thing@BBBBBBBB
    kFmt52c,        // exop vAAAA, vBBBB, thing@CCCCCCCC
    kFmt5rc,        // exop {vCCCC .. v(CCCC+AAAA-1)}, thing@BBBBBBBB
};

/*
 * Different kinds of indexed reference, for formats that include such an
 * indexed reference (e.g., 21c and 35c)
 */
typedef unsigned char InstructionIndexType;
enum InstructionIndexType {
    kIndexUnknown = 0,
    kIndexNone,         // has no index
    kIndexVaries,       // "It depends." Used for throw-verification-error
    kIndexTypeRef,      // type reference index
    kIndexStringRef,    // string reference index
    kIndexMethodRef,    // method reference index
    kIndexFieldRef,     // field reference index
    kIndexInlineMethod, // inline method index (for inline linked methods)
    kIndexVtableOffset, // vtable offset (for static linked methods)
    kIndexFieldOffset   // field offset (for static linked fields)
};

/*
 * Holds the contents of a decoded instruction.
 */
typedef struct DecodedInstruction {
    u4      vA;
    u4      vB;
    u8      vB_wide;        /* for kFmt51l */
    u4      vC;
    u4      arg[5];         /* vC/D/E/F/G in invoke or filled-new-array */
    OpCode  opCode;
    InstructionIndexType indexType;
} DecodedInstruction;

/*
 * Instruction width, a value in the range 0 to 5.
 */
typedef unsigned char InstructionWidth;

/*
 * Instruction flags, used by the verifier and JIT to determine where
 * control can flow to next.  Expected to fit in 8 bits.
 */
typedef unsigned char InstructionFlags;
enum InstructionFlags {
    kInstrCanBranch     = 1,        // conditional or unconditional branch
    kInstrCanContinue   = 1 << 1,   // flow can continue to next statement
    kInstrCanSwitch     = 1 << 2,   // switch statement
    kInstrCanThrow      = 1 << 3,   // could cause an exception to be thrown
    kInstrCanReturn     = 1 << 4,   // returns, no additional statements
    kInstrInvoke        = 1 << 5,   // a flavor of invoke
};

/*
 * Struct that includes a pointer to each of the instruction information
 * tables.
 */
typedef struct InstructionInfoTables {
    InstructionFormat*    formats;
    InstructionIndexType* indexTypes;
    InstructionFlags*     flags;
    InstructionWidth*     widths;
} InstructionInfoTables;

/*
 * Global InstructionInfoTables struct.
 */
extern InstructionInfoTables gDexOpcodeInfo;

/*
 * Return the width of the specified instruction, or 0 if not defined.
 */
DEX_INLINE size_t dexGetInstrWidth(const InstructionWidth* widths,
    OpCode opCode)
{
    //assert(/*opCode >= 0 &&*/ opCode < kNumDalvikInstructions);
    return widths[opCode];
}

/*
 * Return the width of the specified instruction, or 0 if not defined.  Also
 * works for special OP_NOP entries, including switch statement data tables
 * and array data.
 */
size_t dexGetInstrOrTableWidth(const u2* insns);

/*
 * Returns the flags for the specified opcode.
 */
DEX_INLINE int dexGetInstrFlags(const InstructionFlags* flags, OpCode opCode)
{
    //assert(/*opCode >= 0 &&*/ opCode < kNumDalvikInstructions);
    return flags[opCode];
}

/*
 * Returns true if the given flags represent a goto (unconditional branch).
 */
DEX_INLINE bool dexIsGoto(int flags)
{
    return (flags & (kInstrCanBranch | kInstrCanContinue)) == kInstrCanBranch;
}

/*
 * Return the instruction format for the specified opcode.
 */
DEX_INLINE InstructionFormat dexGetInstrFormat(const InstructionFormat* fmts,
    OpCode opCode)
{
    //assert(/*opCode >= 0 &&*/ opCode < kNumDalvikInstructions);
    return fmts[opCode];
}

/*
 * Return the instruction index type for the specified opcode.
 */
DEX_INLINE InstructionIndexType dexGetInstrIndexType(
    const InstructionIndexType* types, OpCode opCode)
{
    //assert(/*opCode >= 0 &&*/ opCode < kNumDalvikInstructions);
    return types[opCode];
}

/*
 * Copy pointers to all of the instruction info tables into
 * the given struct.
 */
void dexGetInstructionInfoTables(InstructionInfoTables* info);

/*
 * Decode the instruction pointed to by "insns".
 */
void dexDecodeInstruction(const u2* insns, DecodedInstruction* pDec);

#endif /*_LIBDEX_INSTRUTILS*/

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
 * Table of Dalvik opcode names.
 */
#include "OpCodeNames.h"

#include <assert.h>

/*
 * The following two lines work, but slashes and dashes both turn into
 * underscores, and the strings are all upper case.  The output is easier
 * to read if we do the strings by hand (could probably write a
 * post-processing function easily enough if maintenance becomes annoying).
 */
//#define H(_op) #_op
//DEFINE_GOTO_TABLE(gOpNames)

/*
 * Dalvik opcode names.
 */
static const char* gOpNames[256] = {
    /* 0x00 */
    "nop",
    "move",
    "move/from16",
    "move/16",
    "move-wide",
    "move-wide/from16",
    "move-wide/16",
    "move-object",
    "move-object/from16",
    "move-object/16",
    "move-result",
    "move-result-wide",
    "move-result-object",
    "move-exception",
    "return-void",
    "return",

    /* 0x10 */
    "return-wide",
    "return-object",
    "const/4",
    "const/16",
    "const",
    "const/high16",
    "const-wide/16",
    "const-wide/32",
    "const-wide",
    "const-wide/high16",
    "const-string",
    "const-string/jumbo",
    "const-class",
    "monitor-enter",
    "monitor-exit",
    "check-cast",

    /* 0x20 */
    "instance-of",
    "array-length",
    "new-instance",
    "new-array",
    "filled-new-array",
    "filled-new-array/range",
    "fill-array-data",
    "throw",
    "goto",
    "goto/16",
    "goto/32",
    "packed-switch",
    "sparse-switch",
    "cmpl-float",
    "cmpg-float",
    "cmpl-double",

    /* 0x30 */
    "cmpg-double",
    "cmp-long",
    "if-eq",
    "if-ne",
    "if-lt",
    "if-ge",
    "if-gt",
    "if-le",
    "if-eqz",
    "if-nez",
    "if-ltz",
    "if-gez",
    "if-gtz",
    "if-lez",
    "UNUSED",
    "UNUSED",

    /* 0x40 */
    "UNUSED",
    "UNUSED",
    "UNUSED",
    "UNUSED",
    "aget",
    "aget-wide",
    "aget-object",
    "aget-boolean",
    "aget-byte",
    "aget-char",
    "aget-short",
    "aput",
    "aput-wide",
    "aput-object",
    "aput-boolean",
    "aput-byte",

    /* 0x50 */
    "aput-char",
    "aput-short",
    "iget",
    "iget-wide",
    "iget-object",
    "iget-boolean",
    "iget-byte",
    "iget-char",
    "iget-short",
    "iput",
    "iput-wide",
    "iput-object",
    "iput-boolean",
    "iput-byte",
    "iput-char",
    "iput-short",

    /* 0x60 */
    "sget",
    "sget-wide",
    "sget-object",
    "sget-boolean",
    "sget-byte",
    "sget-char",
    "sget-short",
    "sput",
    "sput-wide",
    "sput-object",
    "sput-boolean",
    "sput-byte",
    "sput-char",
    "sput-short",
    "invoke-virtual",
    "invoke-super",

    /* 0x70 */
    "invoke-direct",
    "invoke-static",
    "invoke-interface",
    "UNUSED",
    "invoke-virtual/range",
    "invoke-super/range",
    "invoke-direct/range",
    "invoke-static/range",
    "invoke-interface/range",
    "UNUSED",
    "UNUSED",
    "neg-int",
    "not-int",
    "neg-long",
    "not-long",
    "neg-float",

    /* 0x80 */
    "neg-double",
    "int-to-long",
    "int-to-float",
    "int-to-double",
    "long-to-int",
    "long-to-float",
    "long-to-double",
    "float-to-int",
    "float-to-long",
    "float-to-double",
    "double-to-int",
    "double-to-long",
    "double-to-float",
    "int-to-byte",
    "int-to-char",
    "int-to-short",

    /* 0x90 */
    "add-int",
    "sub-int",
    "mul-int",
    "div-int",
    "rem-int",
    "and-int",
    "or-int",
    "xor-int",
    "shl-int",
    "shr-int",
    "ushr-int",
    "add-long",
    "sub-long",
    "mul-long",
    "div-long",
    "rem-long",

    /* 0xa0 */
    "and-long",
    "or-long",
    "xor-long",
    "shl-long",
    "shr-long",
    "ushr-long",
    "add-float",
    "sub-float",
    "mul-float",
    "div-float",
    "rem-float",
    "add-double",
    "sub-double",
    "mul-double",
    "div-double",
    "rem-double",

    /* 0xb0 */
    "add-int/2addr",
    "sub-int/2addr",
    "mul-int/2addr",
    "div-int/2addr",
    "rem-int/2addr",
    "and-int/2addr",
    "or-int/2addr",
    "xor-int/2addr",
    "shl-int/2addr",
    "shr-int/2addr",
    "ushr-int/2addr",
    "add-long/2addr",
    "sub-long/2addr",
    "mul-long/2addr",
    "div-long/2addr",
    "rem-long/2addr",

    /* 0xc0 */
    "and-long/2addr",
    "or-long/2addr",
    "xor-long/2addr",
    "shl-long/2addr",
    "shr-long/2addr",
    "ushr-long/2addr",
    "add-float/2addr",
    "sub-float/2addr",
    "mul-float/2addr",
    "div-float/2addr",
    "rem-float/2addr",
    "add-double/2addr",
    "sub-double/2addr",
    "mul-double/2addr",
    "div-double/2addr",
    "rem-double/2addr",

    /* 0xd0 */
    "add-int/lit16",
    "rsub-int",
    "mul-int/lit16",
    "div-int/lit16",
    "rem-int/lit16",
    "and-int/lit16",
    "or-int/lit16",
    "xor-int/lit16",
    "add-int/lit8",
    "rsub-int/lit8",
    "mul-int/lit8",
    "div-int/lit8",
    "rem-int/lit8",
    "and-int/lit8",
    "or-int/lit8",
    "xor-int/lit8",

    /* 0xe0 */
    "shl-int/lit8",
    "shr-int/lit8",
    "ushr-int/lit8",
    "+iget-volatile",
    "+iput-volatile",
    "+sget-volatile",
    "+sput-volatile",
    "+iget-object-volatile",
    "+iget-wide-volatile",
    "+iput-wide-volatile",
    "+sget-wide-volatile",
    "+sput-wide-volatile",
    "^breakpoint",                  // does not appear in DEX files
    "^throw-verification-error",    // does not appear in DEX files
    "+execute-inline",
    "+execute-inline/range",

    /* 0xf0 */
    "+invoke-direct-empty",
    "UNUSED",
    "+iget-quick",
    "+iget-wide-quick",
    "+iget-object-quick",
    "+iput-quick",
    "+iput-wide-quick",
    "+iput-object-quick",
    "+invoke-virtual-quick",
    "+invoke-virtual-quick/range",
    "+invoke-super-quick",
    "+invoke-super-quick/range",
    "+iput-object-volatile",
    "+sget-object-volatile",
    "+sput-object-volatile",
    "UNUSED",
};

/*
 * Return the name of an opcode.
 */
const char* dexGetOpcodeName(OpCode op)
{
    assert(op >= 0 && op < kNumDalvikInstructions);
    return gOpNames[op];
}

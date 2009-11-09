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
#include "libdex/OpCode.h"
#include "dexdump/OpCodeNames.h"

#include "../../CompilerInternals.h"
#include "ArmLIR.h"
#include <unistd.h>             /* for cacheflush */

/*
 * opcode: ArmOpCode enum
 * skeleton: pre-designated bit-pattern for this opcode
 * k0: key to applying ds/de
 * ds: dest start bit position
 * de: dest end bit position
 * k1: key to applying s1s/s1e
 * s1s: src1 start bit position
 * s1e: src1 end bit position
 * k2: key to applying s2s/s2e
 * s2s: src2 start bit position
 * s2e: src2 end bit position
 * operands: number of operands (for sanity check purposes)
 * name: mnemonic name
 * fmt: for pretty-prining
 */
#define ENCODING_MAP(opcode, skeleton, k0, ds, de, k1, s1s, s1e, k2, s2s, s2e, \
                     k3, k3s, k3e, flags, name, fmt, size) \
        {skeleton, {{k0, ds, de}, {k1, s1s, s1e}, {k2, s2s, s2e}, \
                    {k3, k3s, k3e}}, opcode, flags, name, fmt, size}

/* Instruction dump string format keys: !pf, where "!" is the start
 * of the key, "p" is which numeric operand to use and "f" is the
 * print format.
 *
 * [p]ositions:
 *     0 -> operands[0] (dest)
 *     1 -> operands[1] (src1)
 *     2 -> operands[2] (src2)
 *     3 -> operands[3] (extra)
 *
 * [f]ormats:
 *     h -> 4-digit hex
 *     d -> decimal
 *     E -> decimal*4
 *     F -> decimal*2
 *     c -> branch condition (beq, bne, etc.)
 *     t -> pc-relative target
 *     u -> 1st half of bl[x] target
 *     v -> 2nd half ob bl[x] target
 *     R -> register list
 *     s -> single precision floating point register
 *     S -> double precision floating point register
 *     m -> Thumb2 modified immediate
 *     n -> complimented Thumb2 modified immediate
 *     M -> Thumb2 16-bit zero-extended immediate
 *     b -> 4-digit binary
 *
 *  [!] escape.  To insert "!", use "!!"
 */
/* NOTE: must be kept in sync with enum ArmOpcode from ArmLIR.h */
ArmEncodingMap EncodingMap[kArmLast] = {
    ENCODING_MAP(kArm16BitData,    0x0000,
                 kFmtBitBlt, 15, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP, "data", "0x!0h(!0d)", 1),
    ENCODING_MAP(kThumbAdcRR,        0x4140,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES | USES_CCODES,
                 "adcs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbAddRRI3,      0x1c00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "adds", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(kThumbAddRI8,       0x3000,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE0 | SETS_CCODES,
                 "adds", "r!0d, r!0d, #!1d", 1),
    ENCODING_MAP(kThumbAddRRR,       0x1800,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE12 | SETS_CCODES,
                 "adds", "r!0d, r!1d, r!2d", 1),
    ENCODING_MAP(kThumbAddRRLH,     0x4440,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE01,
                 "add", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbAddRRHL,     0x4480,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE01,
                 "add", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbAddRRHH,     0x44c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE01,
                 "add", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbAddPcRel,    0xa000,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | IS_BRANCH,
                 "add", "r!0d, pc, #!1E", 1),
    ENCODING_MAP(kThumbAddSpRel,    0xa800,
                 kFmtBitBlt, 10, 8, kFmtUnused, -1, -1, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF_SP | REG_USE_SP,
                 "add", "r!0d, sp, #!2E", 1),
    ENCODING_MAP(kThumbAddSpI7,      0xb000,
                 kFmtBitBlt, 6, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP | REG_DEF_SP | REG_USE_SP,
                 "add", "sp, #!0d*4", 1),
    ENCODING_MAP(kThumbAndRR,        0x4000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "ands", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbAsrRRI5,      0x1000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "asrs", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(kThumbAsrRR,        0x4100,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "asrs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbBCond,        0xd000,
                 kFmtBitBlt, 7, 0, kFmtBitBlt, 11, 8, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | IS_BRANCH | USES_CCODES,
                 "b!1c", "!0t", 1),
    ENCODING_MAP(kThumbBUncond,      0xe000,
                 kFmtBitBlt, 10, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, NO_OPERAND | IS_BRANCH,
                 "b", "!0t", 1),
    ENCODING_MAP(kThumbBicRR,        0x4380,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "bics", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbBkpt,          0xbe00,
                 kFmtBitBlt, 7, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP | IS_BRANCH,
                 "bkpt", "!0d", 1),
    ENCODING_MAP(kThumbBlx1,         0xf000,
                 kFmtBitBlt, 10, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | IS_BRANCH | REG_DEF_LR,
                 "blx_1", "!0u", 1),
    ENCODING_MAP(kThumbBlx2,         0xe800,
                 kFmtBitBlt, 10, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | IS_BRANCH | REG_DEF_LR,
                 "blx_2", "!0v", 1),
    ENCODING_MAP(kThumbBl1,          0xf000,
                 kFmtBitBlt, 10, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP | IS_BRANCH | REG_DEF_LR,
                 "bl_1", "!0u", 1),
    ENCODING_MAP(kThumbBl2,          0xf800,
                 kFmtBitBlt, 10, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP | IS_BRANCH | REG_DEF_LR,
                 "bl_2", "!0v", 1),
    ENCODING_MAP(kThumbBlxR,         0x4780,
                 kFmtBitBlt, 6, 3, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_UNARY_OP | REG_USE0 | IS_BRANCH | REG_DEF_LR,
                 "blx", "r!0d", 1),
    ENCODING_MAP(kThumbBx,            0x4700,
                 kFmtBitBlt, 6, 3, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP | IS_BRANCH,
                 "bx", "r!0d", 1),
    ENCODING_MAP(kThumbCmnRR,        0x42c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01 | SETS_CCODES,
                 "cmn", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbCmpRI8,       0x2800,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE0 | SETS_CCODES,
                 "cmp", "r!0d, #!1d", 1),
    ENCODING_MAP(kThumbCmpRR,        0x4280,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01 | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbCmpLH,        0x4540,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01 | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbCmpHL,        0x4580,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01 | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbCmpHH,        0x45c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01 | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbEorRR,        0x4040,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "eors", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbLdmia,         0xc800,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE0 | REG_DEF_LIST1,
                 "ldmia", "r!0d!!, <!1R>", 1),
    ENCODING_MAP(kThumbLdrRRI5,      0x6800,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldr", "r!0d, [r!1d, #!2E]", 1),
    ENCODING_MAP(kThumbLdrRRR,       0x5800,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "ldr", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbLdrPcRel,    0x4800,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0 | REG_USE_PC,
                 "ldr", "r!0d, [pc, #!1E]", 1),
    ENCODING_MAP(kThumbLdrSpRel,    0x9800,
                 kFmtBitBlt, 10, 8, kFmtUnused, -1, -1, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0 | REG_USE_SP,
                 "ldr", "r!0d, [sp, #!2E]", 1),
    ENCODING_MAP(kThumbLdrbRRI5,     0x7800,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrb", "r!0d, [r!1d, #2d]", 1),
    ENCODING_MAP(kThumbLdrbRRR,      0x5c00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "ldrb", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbLdrhRRI5,     0x8800,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrh", "r!0d, [r!1d, #!2F]", 1),
    ENCODING_MAP(kThumbLdrhRRR,      0x5a00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "ldrh", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbLdrsbRRR,     0x5600,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "ldrsb", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbLdrshRRR,     0x5e00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "ldrsh", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbLslRRI5,      0x0000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "lsls", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(kThumbLslRR,        0x4080,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "lsls", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbLsrRRI5,      0x0800,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "lsrs", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(kThumbLsrRR,        0x40c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "lsrs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbMovImm,       0x2000,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0 | SETS_CCODES,
                 "movs", "r!0d, #!1d", 1),
    ENCODING_MAP(kThumbMovRR,        0x1c00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "movs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbMovRR_H2H,    0x46c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "mov", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbMovRR_H2L,    0x4640,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "mov", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbMovRR_L2H,    0x4680,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "mov", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbMul,           0x4340,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "muls", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbMvn,           0x43c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "mvns", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbNeg,           0x4240,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "negs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbOrr,           0x4300,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "orrs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbPop,           0xbc00,
                 kFmtBitBlt, 8, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_UNARY_OP | REG_DEF_SP | REG_USE_SP | REG_DEF_LIST0,
                 "pop", "<!0R>", 1),
    ENCODING_MAP(kThumbPush,          0xb400,
                 kFmtBitBlt, 8, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_UNARY_OP | REG_DEF_SP | REG_USE_SP | REG_USE_LIST0,
                 "push", "<!0R>", 1),
    ENCODING_MAP(kThumbRorRR,        0x41c0,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | SETS_CCODES,
                 "rors", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbSbc,           0x4180,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE01 | USES_CCODES | SETS_CCODES,
                 "sbcs", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumbStmia,         0xc000,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0 | REG_USE0 | REG_USE_LIST1,
                 "stmia", "r!0d!!, <!1R>", 1),
    ENCODING_MAP(kThumbStrRRI5,      0x6000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "str", "r!0d, [r!1d, #!2E]", 1),
    ENCODING_MAP(kThumbStrRRR,       0x5000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE012,
                 "str", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbStrSpRel,    0x9000,
                 kFmtBitBlt, 10, 8, kFmtUnused, -1, -1, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE0 | REG_USE_SP,
                 "str", "r!0d, [sp, #!2E]", 1),
    ENCODING_MAP(kThumbStrbRRI5,     0x7000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "strb", "r!0d, [r!1d, #!2d]", 1),
    ENCODING_MAP(kThumbStrbRRR,      0x5400,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE012,
                 "strb", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbStrhRRI5,     0x8000,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 10, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "strh", "r!0d, [r!1d, #!2F]", 1),
    ENCODING_MAP(kThumbStrhRRR,      0x5200,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE012,
                 "strh", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(kThumbSubRRI3,      0x1e00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "subs", "r!0d, r!1d, #!2d]", 1),
    ENCODING_MAP(kThumbSubRI8,       0x3800,
                 kFmtBitBlt, 10, 8, kFmtBitBlt, 7, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE0 | SETS_CCODES,
                 "subs", "r!0d, #!1d", 1),
    ENCODING_MAP(kThumbSubRRR,       0x1a00,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtBitBlt, 8, 6,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE12 | SETS_CCODES,
                 "subs", "r!0d, r!1d, r!2d", 1),
    ENCODING_MAP(kThumbSubSpI7,      0xb080,
                 kFmtBitBlt, 6, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_UNARY_OP | REG_DEF_SP | REG_USE_SP,
                 "sub", "sp, #!0d", 1),
    ENCODING_MAP(kThumbSwi,           0xdf00,
                 kFmtBitBlt, 7, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,                       kFmtUnused, -1, -1, IS_UNARY_OP | IS_BRANCH,
                 "swi", "!0d", 1),
    ENCODING_MAP(kThumbTst,           0x4200,
                 kFmtBitBlt, 2, 0, kFmtBitBlt, 5, 3, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_UNARY_OP | REG_USE01 | SETS_CCODES,
                 "tst", "r!0d, r!1d", 1),
    ENCODING_MAP(kThumb2Vldrs,       0xed900a00,
                 kFmtSfp, 22, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "vldr", "!0s, [r!1d, #!2E]", 2),
    ENCODING_MAP(kThumb2Vldrd,       0xed900b00,
                 kFmtDfp, 22, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "vldr", "!0S, [r!1d, #!2E]", 2),
    ENCODING_MAP(kThumb2Vmuls,        0xee200a00,
                 kFmtSfp, 22, 12, kFmtSfp, 7, 16, kFmtSfp, 5, 0,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vmuls", "!0s, !1s, !2s", 2),
    ENCODING_MAP(kThumb2Vmuld,        0xee200b00,
                 kFmtDfp, 22, 12, kFmtDfp, 7, 16, kFmtDfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vmuld", "!0S, !1S, !2S", 2),
    ENCODING_MAP(kThumb2Vstrs,       0xed800a00,
                 kFmtSfp, 22, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "vstr", "!0s, [r!1d, #!2E]", 2),
    ENCODING_MAP(kThumb2Vstrd,       0xed800b00,
                 kFmtDfp, 22, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "vstr", "!0S, [r!1d, #!2E]", 2),
    ENCODING_MAP(kThumb2Vsubs,        0xee300a40,
                 kFmtSfp, 22, 12, kFmtSfp, 7, 16, kFmtSfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vsub", "!0s, !1s, !2s", 2),
    ENCODING_MAP(kThumb2Vsubd,        0xee300b40,
                 kFmtDfp, 22, 12, kFmtDfp, 7, 16, kFmtDfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vsub", "!0S, !1S, !2S", 2),
    ENCODING_MAP(kThumb2Vadds,        0xee300a00,
                 kFmtSfp, 22, 12, kFmtSfp, 7, 16, kFmtSfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vadd", "!0s, !1s, !2s", 2),
    ENCODING_MAP(kThumb2Vaddd,        0xee300b00,
                 kFmtDfp, 22, 12, kFmtDfp, 7, 16, kFmtDfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vadd", "!0S, !1S, !2S", 2),
    ENCODING_MAP(kThumb2Vdivs,        0xee800a00,
                 kFmtSfp, 22, 12, kFmtSfp, 7, 16, kFmtSfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vdivs", "!0s, !1s, !2s", 2),
    ENCODING_MAP(kThumb2Vdivd,        0xee800b00,
                 kFmtDfp, 22, 12, kFmtDfp, 7, 16, kFmtDfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "vdivd", "!0S, !1S, !2S", 2),
    ENCODING_MAP(kThumb2VcvtIF,       0xeeb80ac0,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vcvt.f32", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2VcvtID,       0xeeb80bc0,
                 kFmtDfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vcvt.f64", "!0S, !1s", 2),
    ENCODING_MAP(kThumb2VcvtFI,       0xeebd0ac0,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vcvt.s32.f32 ", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2VcvtDI,       0xeebd0bc0,
                 kFmtSfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vcvt.s32.f64 ", "!0s, !1S", 2),
    ENCODING_MAP(kThumb2VcvtFd,       0xeeb70ac0,
                 kFmtDfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vcvt.f64.f32 ", "!0S, !1s", 2),
    ENCODING_MAP(kThumb2VcvtDF,       0xeeb70bc0,
                 kFmtSfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vcvt.f32.f64 ", "!0s, !1S", 2),
    ENCODING_MAP(kThumb2Vsqrts,       0xeeb10ac0,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vsqrt.f32 ", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2Vsqrtd,       0xeeb10bc0,
                 kFmtDfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vsqrt.f64 ", "!0S, !1S", 2),
    ENCODING_MAP(kThumb2MovImmShift, 0xf04f0000, /* no setflags encoding */
                 kFmtBitBlt, 11, 8, kFmtModImm, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0,
                 "mov", "r!0d, #!1m", 2),
    ENCODING_MAP(kThumb2MovImm16,       0xf2400000,
                 kFmtBitBlt, 11, 8, kFmtImm16, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0,
                 "mov", "r!0d, #!1M", 2),
    ENCODING_MAP(kThumb2StrRRI12,       0xf8c00000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "str", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(kThumb2LdrRRI12,       0xf8d00000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldr", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(kThumb2StrRRI8Predec,       0xf8400c00,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 8, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "str", "r!0d,[r!1d, #-!2d]", 2),
    ENCODING_MAP(kThumb2LdrRRI8Predec,       0xf8500c00,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 8, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldr", "r!0d,[r!1d, #-!2d]", 2),
    ENCODING_MAP(kThumb2Cbnz,       0xb900, /* Note: does not affect flags */
                 kFmtBitBlt, 2, 0, kFmtImm6, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE0 | IS_BRANCH,
                 "cbnz", "r!0d,!1t", 1),
    ENCODING_MAP(kThumb2Cbz,       0xb100, /* Note: does not affect flags */
                 kFmtBitBlt, 2, 0, kFmtImm6, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE0 | IS_BRANCH,
                 "cbz", "r!0d,!1t", 1),
    ENCODING_MAP(kThumb2AddRRI12,       0xf2000000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtImm12, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1,/* Note: doesn't affect flags */
                 "add", "r!0d,r!1d,#!2d", 2),
    ENCODING_MAP(kThumb2MovRR,       0xea4f0000, /* no setflags encoding */
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 3, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "mov", "r!0d, r!1d", 2),
    ENCODING_MAP(kThumb2Vmovs,       0xeeb00a40,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vmov.f32 ", " !0s, !1s", 2),
    ENCODING_MAP(kThumb2Vmovd,       0xeeb00b40,
                 kFmtDfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vmov.f64 ", " !0S, !1S", 2),
    ENCODING_MAP(kThumb2Ldmia,         0xe8900000,
                 kFmtBitBlt, 19, 16, kFmtBitBlt, 15, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE0 | REG_DEF_LIST1,
                 "ldmia", "r!0d!!, <!1R>", 2),
    ENCODING_MAP(kThumb2Stmia,         0xe8800000,
                 kFmtBitBlt, 19, 16, kFmtBitBlt, 15, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE0 | REG_USE_LIST1,
                 "stmia", "r!0d!!, <!1R>", 2),
    ENCODING_MAP(kThumb2AddRRR,  0xeb100000, /* setflags encoding */
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1,
                 IS_QUAD_OP | REG_DEF0_USE12 | SETS_CCODES,
                 "adds", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2SubRRR,       0xebb00000, /* setflags enconding */
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1,
                 IS_QUAD_OP | REG_DEF0_USE12 | SETS_CCODES,
                 "subs", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2SbcRRR,       0xeb700000, /* setflags encoding */
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1,
                 IS_QUAD_OP | REG_DEF0_USE12 | USES_CCODES | SETS_CCODES,
                 "sbcs", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2CmpRR,       0xebb00f00,
                 kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0, kFmtShift, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_USE01 | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 2),
    ENCODING_MAP(kThumb2SubRRI12,       0xf2a00000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtImm12, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1,/* Note: doesn't affect flags */
                 "sub", "r!0d,r!1d,#!2d", 2),
    ENCODING_MAP(kThumb2MvnImmShift,  0xf06f0000, /* no setflags encoding */
                 kFmtBitBlt, 11, 8, kFmtModImm, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0,
                 "mvn", "r!0d, #!1n", 2),
    ENCODING_MAP(kThumb2Sel,       0xfaa0f080,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE12 | USES_CCODES,
                 "sel", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2Ubfx,       0xf3c00000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtLsb, -1, -1,
                 kFmtBWidth, 4, 0, IS_QUAD_OP | REG_DEF0_USE1,
                 "ubfx", "r!0d, r!1d, #!2d, #!3d", 2),
    ENCODING_MAP(kThumb2Sbfx,       0xf3400000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtLsb, -1, -1,
                 kFmtBWidth, 4, 0, IS_QUAD_OP | REG_DEF0_USE1,
                 "sbfx", "r!0d, r!1d, #!2d, #!3d", 2),
    ENCODING_MAP(kThumb2LdrRRR,    0xf8500000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_DEF0_USE12,
                 "ldr", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2LdrhRRR,    0xf8300000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_DEF0_USE12,
                 "ldrh", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2LdrshRRR,    0xf9300000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_DEF0_USE12,
                 "ldrsh", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2LdrbRRR,    0xf8100000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_DEF0_USE12,
                 "ldrb", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2LdrsbRRR,    0xf9100000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_DEF0_USE12,
                 "ldrsb", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2StrRRR,    0xf8400000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_USE012,
                 "str", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2StrhRRR,    0xf8200000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_USE012,
                 "strh", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2StrbRRR,    0xf8000000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 5, 4, IS_QUAD_OP | REG_USE012,
                 "strb", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(kThumb2LdrhRRI12,       0xf8b00000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrh", "r!0d,[r!1d, #!2d]", 2),
    ENCODING_MAP(kThumb2LdrshRRI12,       0xf9b00000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrsh", "r!0d,[r!1d, #!2d]", 2),
    ENCODING_MAP(kThumb2LdrbRRI12,       0xf8900000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrb", "r!0d,[r!1d, #!2d]", 2),
    ENCODING_MAP(kThumb2LdrsbRRI12,       0xf9900000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrsb", "r!0d,[r!1d, #!2d]", 2),
    ENCODING_MAP(kThumb2StrhRRI12,       0xf8a00000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "strh", "r!0d,[r!1d, #!2d]", 2),
    ENCODING_MAP(kThumb2StrbRRI12,       0xf8800000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 11, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_USE01,
                 "strb", "r!0d,[r!1d, #!2d]", 2),
    ENCODING_MAP(kThumb2Pop,           0xe8bd0000,
                 kFmtBitBlt, 15, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_UNARY_OP | REG_DEF_SP | REG_USE_SP | REG_DEF_LIST0,
                 "pop", "<!0R>", 2),
    ENCODING_MAP(kThumb2Push,          0xe8ad0000,
                 kFmtBitBlt, 15, 0, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_UNARY_OP | REG_DEF_SP | REG_USE_SP | REG_USE_LIST0,
                 "push", "<!0R>", 2),
    ENCODING_MAP(kThumb2CmpRI8, 0xf1b00f00,
                 kFmtBitBlt, 19, 16, kFmtModImm, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_USE0 | SETS_CCODES,
                 "cmp", "r!0d, #!1m", 2),
    ENCODING_MAP(kThumb2AdcRRR,  0xeb500000, /* setflags encoding */
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1,
                 IS_QUAD_OP | REG_DEF0_USE12 | SETS_CCODES,
                 "acds", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(kThumb2AndRRR,  0xea000000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1, IS_QUAD_OP | REG_DEF0_USE12,
                 "and", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(kThumb2BicRRR,  0xea200000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1, IS_QUAD_OP | REG_DEF0_USE12,
                 "bic", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(kThumb2CmnRR,  0xeb000000,
                 kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0, kFmtShift, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "cmn", "r!0d, r!1d, shift !2d", 2),
    ENCODING_MAP(kThumb2EorRRR,  0xea800000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1, IS_QUAD_OP | REG_DEF0_USE12,
                 "eor", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(kThumb2MulRRR,  0xfb00f000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "mul", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2MnvRR,  0xea6f0000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 3, 0, kFmtShift, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "mvn", "r!0d, r!1d, shift !2d", 2),
    ENCODING_MAP(kThumb2RsubRRI8,       0xf1d00000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "rsb", "r!0d,r!1d,#!2m", 2),
    ENCODING_MAP(kThumb2NegRR,       0xf1d00000, /* instance of rsub */
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "neg", "r!0d,r!1d", 2),
    ENCODING_MAP(kThumb2OrrRRR,  0xea400000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtShift, -1, -1, IS_QUAD_OP | REG_DEF0_USE12,
                 "orr", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(kThumb2TstRR,       0xea100f00,
                 kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0, kFmtShift, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_USE01 | SETS_CCODES,
                 "tst", "r!0d, r!1d, shift !2d", 2),
    ENCODING_MAP(kThumb2LslRRR,  0xfa00f000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "lsl", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2LsrRRR,  0xfa20f000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "lsr", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2AsrRRR,  0xfa40f000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "asr", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2RorRRR,  0xfa60f000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "ror", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2LslRRI5,  0xea4f0000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 3, 0, kFmtShift5, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "lsl", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(kThumb2LsrRRI5,  0xea4f0010,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 3, 0, kFmtShift5, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "lsr", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(kThumb2AsrRRI5,  0xea4f0020,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 3, 0, kFmtShift5, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "asr", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(kThumb2RorRRI5,  0xea4f0030,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 3, 0, kFmtShift5, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ror", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(kThumb2BicRRI8,  0xf0200000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "bic", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2AndRRI8,  0xf0000000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "and", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2OrrRRI8,  0xf0400000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "orr", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2EorRRI8,  0xf0800000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "eor", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2AddRRI8,  0xf1100000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "adds", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2AdcRRI8,  0xf1500000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES | USES_CCODES,
                 "adcs", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2SubRRI8,  0xf1b00000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES,
                 "subs", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2SbcRRI8,  0xf1700000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0_USE1 | SETS_CCODES | USES_CCODES,
                 "sbcs", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(kThumb2It,  0xbf00,
                 kFmtBitBlt, 7, 4, kFmtBitBlt, 3, 0, kFmtModImm, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | IS_IT | USES_CCODES,
                 "it:!1b", "!0c", 1),
    ENCODING_MAP(kThumb2Fmstat,  0xeef1fa10,
                 kFmtUnused, -1, -1, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, NO_OPERAND | SETS_CCODES,
                 "fmstat", "", 2),
    ENCODING_MAP(kThumb2Vcmpd,        0xeeb40b40,
                 kFmtDfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01,
                 "vcmp.f64", "!0S, !1S", 2),
    ENCODING_MAP(kThumb2Vcmps,        0xeeb40a40,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_USE01,
                 "vcmp.f32", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2LdrPcRel12,       0xf8df0000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 11, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_TERTIARY_OP | REG_DEF0 | REG_USE_PC,
                 "ldr", "r!0d,[rpc, #!1d]", 2),
    ENCODING_MAP(kThumb2BCond,        0xf0008000,
                 kFmtBrOffset, -1, -1, kFmtBitBlt, 25, 22, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1,
                 IS_BINARY_OP | IS_BRANCH | USES_CCODES,
                 "b!1c", "!0t", 2),
    ENCODING_MAP(kThumb2Vmovd_RR,       0xeeb00b40,
                 kFmtDfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vmov.f64", "!0S, !1S", 2),
    ENCODING_MAP(kThumb2Vmovs_RR,       0xeeb00a40,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vmov.f32", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2Fmrs,       0xee100a10,
                 kFmtBitBlt, 15, 12, kFmtSfp, 7, 16, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "fmrs", "r!0d, !1s", 2),
    ENCODING_MAP(kThumb2Fmsr,       0xee000a10,
                 kFmtSfp, 7, 16, kFmtBitBlt, 15, 12, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "fmsr", "!0s, r!1d", 2),
    ENCODING_MAP(kThumb2Fmrrd,       0xec500b10,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtDfp, 5, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF01_USE2,
                 "fmrrd", "r!0d, r!1d, !2S", 2),
    ENCODING_MAP(kThumb2Fmdrr,       0xec400b10,
                 kFmtDfp, 5, 0, kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE12,
                 "fmdrr", "!0S, r!1d, r!2d", 2),
    ENCODING_MAP(kThumb2Vabsd,       0xeeb00bc0,
                 kFmtDfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vabs.f64", "!0S, !1S", 2),
    ENCODING_MAP(kThumb2Vabss,       0xeeb00ac0,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vabs.f32", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2Vnegd,       0xeeb10b40,
                 kFmtDfp, 22, 12, kFmtDfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vneg.f64", "!0S, !1S", 2),
    ENCODING_MAP(kThumb2Vnegs,       0xeeb10a40,
                 kFmtSfp, 22, 12, kFmtSfp, 5, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0_USE1,
                 "vneg.f32", "!0s, !1s", 2),
    ENCODING_MAP(kThumb2Vmovs_IMM8,       0xeeb00a00,
                 kFmtSfp, 22, 12, kFmtFPImm, 16, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0,
                 "vmov.f32", "!0s, #0x!1h", 2),
    ENCODING_MAP(kThumb2Vmovd_IMM8,       0xeeb00b00,
                 kFmtDfp, 22, 12, kFmtFPImm, 16, 0, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, IS_BINARY_OP | REG_DEF0,
                 "vmov.f64", "!0S, #0x!1h", 2),
    ENCODING_MAP(kThumb2Mla,  0xfb000000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16, kFmtBitBlt, 3, 0,
                 kFmtBitBlt, 15, 12,
                 IS_QUAD_OP | REG_DEF0 | REG_USE1 | REG_USE2 | REG_USE3,
                 "mla", "r!0d, r!1d, r!2d, r!3d", 2),
    ENCODING_MAP(kThumb2Umull,  0xfba00000,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 11, 8, kFmtBitBlt, 19, 16,
                 kFmtBitBlt, 3, 0,
                 IS_QUAD_OP | REG_DEF0 | REG_DEF1 | REG_USE2 | REG_USE3,
                 "umull", "r!0d, r!1d, r!2d, r!3d", 2),
    ENCODING_MAP(kThumb2Ldrex,       0xe8500f00,
                 kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16, kFmtBitBlt, 7, 0,
                 kFmtUnused, -1, -1, IS_TERTIARY_OP | REG_DEF0_USE1,
                 "ldrex", "r!0d,[r!1d, #!2E]", 2),
    ENCODING_MAP(kThumb2Strex,       0xe8400000,
                 kFmtBitBlt, 11, 8, kFmtBitBlt, 15, 12, kFmtBitBlt, 19, 16,
                 kFmtBitBlt, 7, 0, IS_QUAD_OP | REG_DEF0_USE12,
                 "strex", "r!0d,r!1d, [r!2d, #!2E]", 2),
    ENCODING_MAP(kThumb2Clrex,       0xf3bf8f2f,
                 kFmtUnused, -1, -1, kFmtUnused, -1, -1, kFmtUnused, -1, -1,
                 kFmtUnused, -1, -1, NO_OPERAND, "clrex", "", 2),
};

/*
 * The fake NOP of moving r0 to r0 actually will incur data stalls if r0 is
 * not ready. Since r5 (rFP) is not updated often, it is less likely to
 * generate unnecessary stall cycles.
 */
#define PADDING_MOV_R5_R5               0x1C2D

/* Write the numbers in the literal pool to the codegen stream */
static void installDataContent(CompilationUnit *cUnit)
{
    int *dataPtr = (int *) ((char *) cUnit->baseAddr + cUnit->dataOffset);
    ArmLIR *dataLIR = (ArmLIR *) cUnit->wordList;
    while (dataLIR) {
        *dataPtr++ = dataLIR->operands[0];
        dataLIR = NEXT_LIR(dataLIR);
    }
}

/* Returns the size of a Jit trace description */
static int jitTraceDescriptionSize(const JitTraceDescription *desc)
{
    int runCount;
    for (runCount = 0; ; runCount++) {
        if (desc->trace[runCount].frag.runEnd)
           break;
    }
    return sizeof(JitCodeDesc) + ((runCount+1) * sizeof(JitTraceRun));
}

/* Return TRUE if error happens */
static bool assembleInstructions(CompilationUnit *cUnit, intptr_t startAddr)
{
    short *bufferAddr = (short *) cUnit->codeBuffer;
    ArmLIR *lir;

    for (lir = (ArmLIR *) cUnit->firstLIRInsn; lir; lir = NEXT_LIR(lir)) {
        if (lir->opCode < 0) {
            if ((lir->opCode == kArmPseudoPseudoAlign4) &&
                /* 1 means padding is needed */
                (lir->operands[0] == 1)) {
                *bufferAddr++ = PADDING_MOV_R5_R5;
            }
            continue;
        }

        if (lir->isNop) {
            continue;
        }

        if (lir->opCode == kThumbLdrPcRel ||
            lir->opCode == kThumb2LdrPcRel12 ||
            lir->opCode == kThumbAddPcRel ||
            ((lir->opCode == kThumb2Vldrs) && (lir->operands[1] == rpc))) {
            ArmLIR *lirTarget = (ArmLIR *) lir->generic.target;
            intptr_t pc = (lir->generic.offset + 4) & ~3;
            /*
             * Allow an offset (stored in operands[2] to be added to the
             * PC-relative target. Useful to get to a fixed field inside a
             * chaining cell.
             */
            intptr_t target = lirTarget->generic.offset + lir->operands[2];
            int delta = target - pc;
            if (delta & 0x3) {
                LOGE("PC-rel distance is not multiples of 4: %d\n", delta);
                dvmAbort();
            }
            if ((lir->opCode == kThumb2LdrPcRel12) && (delta > 4091)) {
                return true;
            } else if (delta > 1020) {
                return true;
            }
            if (lir->opCode == kThumb2Vldrs) {
                lir->operands[2] = delta >> 2;
            } else {
                lir->operands[1] = (lir->opCode == kThumb2LdrPcRel12) ?
                                    delta : delta >> 2;
            }
        } else if (lir->opCode == kThumb2Cbnz || lir->opCode == kThumb2Cbz) {
            ArmLIR *targetLIR = (ArmLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if (delta > 126 || delta < 0) {
                /*
                 * TODO: allow multiple kinds of assembler failure to allow
                 * change of code patterns when things don't fit.
                 */
                return true;
            } else {
                lir->operands[1] = delta >> 1;
            }
        } else if (lir->opCode == kThumbBCond ||
                   lir->opCode == kThumb2BCond) {
            ArmLIR *targetLIR = (ArmLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if ((lir->opCode == kThumbBCond) && (delta > 254 || delta < -256)) {
                return true;
            }
            lir->operands[0] = delta >> 1;
        } else if (lir->opCode == kThumbBUncond) {
            ArmLIR *targetLIR = (ArmLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if (delta > 2046 || delta < -2048) {
                LOGE("Unconditional branch distance out of range: %d\n", delta);
                dvmAbort();
            }
            lir->operands[0] = delta >> 1;
        } else if (lir->opCode == kThumbBlx1) {
            assert(NEXT_LIR(lir)->opCode == kThumbBlx2);
            /* curPC is Thumb */
            intptr_t curPC = (startAddr + lir->generic.offset + 4) & ~3;
            intptr_t target = lir->operands[1];

            /* Match bit[1] in target with base */
            if (curPC & 0x2) {
                target |= 0x2;
            }
            int delta = target - curPC;
            assert((delta >= -(1<<22)) && (delta <= ((1<<22)-2)));

            lir->operands[0] = (delta >> 12) & 0x7ff;
            NEXT_LIR(lir)->operands[0] = (delta>> 1) & 0x7ff;
        }

        ArmEncodingMap *encoder = &EncodingMap[lir->opCode];
        u4 bits = encoder->skeleton;
        int i;
        for (i = 0; i < 4; i++) {
            u4 operand;
            u4 value;
            operand = lir->operands[i];
            switch(encoder->fieldLoc[i].kind) {
                case kFmtUnused:
                    break;
                case kFmtFPImm:
                    value = ((operand & 0xF0) >> 4) << encoder->fieldLoc[i].end;
                    value |= (operand & 0x0F) << encoder->fieldLoc[i].start;
                    bits |= value;
                    break;
                case kFmtBrOffset:
                    /*
                     * NOTE: branch offsets are not handled here, but
                     * in the main assembly loop (where label values
                     * are known).  For reference, here is what the
                     * encoder handing would be:
                         value = ((operand  & 0x80000) >> 19) << 26;
                         value |= ((operand & 0x40000) >> 18) << 11;
                         value |= ((operand & 0x20000) >> 17) << 13;
                         value |= ((operand & 0x1f800) >> 11) << 16;
                         value |= (operand  & 0x007ff);
                         bits |= value;
                     */
                    break;
                case kFmtShift5:
                    value = ((operand & 0x1c) >> 2) << 12;
                    value |= (operand & 0x03) << 6;
                    bits |= value;
                    break;
                case kFmtShift:
                    value = ((operand & 0x70) >> 4) << 12;
                    value |= (operand & 0x0f) << 4;
                    bits |= value;
                    break;
                case kFmtBWidth:
                    value = operand - 1;
                    bits |= value;
                    break;
                case kFmtLsb:
                    value = ((operand & 0x1c) >> 2) << 12;
                    value |= (operand & 0x03) << 6;
                    bits |= value;
                    break;
                case kFmtImm6:
                    value = ((operand & 0x20) >> 5) << 9;
                    value |= (operand & 0x1f) << 3;
                    bits |= value;
                    break;
                case kFmtBitBlt:
                    value = (operand << encoder->fieldLoc[i].start) &
                            ((1 << (encoder->fieldLoc[i].end + 1)) - 1);
                    bits |= value;
                    break;
                case kFmtDfp: {
                    assert(DOUBLEREG(operand));
                    assert((operand & 0x1) == 0);
                    int regName = (operand & FP_REG_MASK) >> 1;
                    /* Snag the 1-bit slice and position it */
                    value = ((regName & 0x10) >> 4) <<
                            encoder->fieldLoc[i].end;
                    /* Extract and position the 4-bit slice */
                    value |= (regName & 0x0f) <<
                            encoder->fieldLoc[i].start;
                    bits |= value;
                    break;
                }
                case kFmtSfp:
                    assert(SINGLEREG(operand));
                    /* Snag the 1-bit slice and position it */
                    value = (operand & 0x1) <<
                            encoder->fieldLoc[i].end;
                    /* Extract and position the 4-bit slice */
                    value |= ((operand & 0x1e) >> 1) <<
                            encoder->fieldLoc[i].start;
                    bits |= value;
                    break;
                case kFmtImm12:
                case kFmtModImm:
                    value = ((operand & 0x800) >> 11) << 26;
                    value |= ((operand & 0x700) >> 8) << 12;
                    value |= operand & 0x0ff;
                    bits |= value;
                    break;
                case kFmtImm16:
                    value = ((operand & 0x0800) >> 11) << 26;
                    value |= ((operand & 0xf000) >> 12) << 16;
                    value |= ((operand & 0x0700) >> 8) << 12;
                    value |= operand & 0x0ff;
                    bits |= value;
                    break;
                default:
                    assert(0);
            }
        }
        if (encoder->size == 2) {
            *bufferAddr++ = (bits >> 16) & 0xffff;
        }
        *bufferAddr++ = bits & 0xffff;
    }
    return false;
}

/*
 * Translation layout in the code cache.  Note that the codeAddress pointer
 * in JitTable will point directly to the code body (field codeAddress).  The
 * chain cell offset codeAddress - 2, and (if present) executionCount is at
 * codeAddress - 6.
 *
 *      +----------------------------+
 *      | Execution count            |  -> [Optional] 4 bytes
 *      +----------------------------+
 *   +--| Offset to chain cell counts|  -> 2 bytes
 *   |  +----------------------------+
 *   |  | Code body                  |  -> Start address for translation
 *   |  |                            |     variable in 2-byte chunks
 *   |  .                            .     (JitTable's codeAddress points here)
 *   |  .                            .
 *   |  |                            |
 *   |  +----------------------------+
 *   |  | Chaining Cells             |  -> 8 bytes each, must be 4 byte aligned
 *   |  .                            .
 *   |  .                            .
 *   |  |                            |
 *   |  +----------------------------+
 *   +->| Chaining cell counts       |  -> 4 bytes, chain cell counts by type
 *      +----------------------------+
 *      | Trace description          |  -> variable sized
 *      .                            .
 *      |                            |
 *      +----------------------------+
 *      | Literal pool               |  -> 4-byte aligned, variable size
 *      .                            .
 *      .                            .
 *      |                            |
 *      +----------------------------+
 *
 * Go over each instruction in the list and calculate the offset from the top
 * before sending them off to the assembler. If out-of-range branch distance is
 * seen rearrange the instructions a bit to correct it.
 */
void dvmCompilerAssembleLIR(CompilationUnit *cUnit, JitTranslationInfo *info)
{
    LIR *lir;
    ArmLIR *armLIR;
    int offset = 0;
    int i;
    ChainCellCounts chainCellCounts;
    int descSize = jitTraceDescriptionSize(cUnit->traceDesc);

    info->instructionSet = cUnit->instructionSet;

    /* Beginning offset needs to allow space for chain cell offset */
    for (armLIR = (ArmLIR *) cUnit->firstLIRInsn;
         armLIR;
         armLIR = NEXT_LIR(armLIR)) {
        armLIR->generic.offset = offset;
        if (armLIR->opCode >= 0 && !armLIR->isNop) {
            armLIR->size = EncodingMap[armLIR->opCode].size * 2;
            offset += armLIR->size;
        } else if (armLIR->opCode == kArmPseudoPseudoAlign4) {
            if (offset & 0x2) {
                offset += 2;
                armLIR->operands[0] = 1;
            } else {
                armLIR->operands[0] = 0;
            }
        }
        /* Pseudo opcodes don't consume space */
    }

    /* Const values have to be word aligned */
    offset = (offset + 3) & ~3;

    /* Add space for chain cell counts & trace description */
    u4 chainCellOffset = offset;
    ArmLIR *chainCellOffsetLIR = (ArmLIR *) cUnit->chainCellOffsetLIR;
    assert(chainCellOffsetLIR);
    assert(chainCellOffset < 0x10000);
    assert(chainCellOffsetLIR->opCode == kArm16BitData &&
           chainCellOffsetLIR->operands[0] == CHAIN_CELL_OFFSET_TAG);

    /*
     * Replace the CHAIN_CELL_OFFSET_TAG with the real value. If trace
     * profiling is enabled, subtract 4 (occupied by the counter word) from
     * the absolute offset as the value stored in chainCellOffsetLIR is the
     * delta from &chainCellOffsetLIR to &ChainCellCounts.
     */
    chainCellOffsetLIR->operands[0] =
        gDvmJit.profile ? (chainCellOffset - 4) : chainCellOffset;

    offset += sizeof(chainCellCounts) + descSize;

    assert((offset & 0x3) == 0);  /* Should still be word aligned */

    /* Set up offsets for literals */
    cUnit->dataOffset = offset;

    for (lir = cUnit->wordList; lir; lir = lir->next) {
        lir->offset = offset;
        offset += 4;
    }

    cUnit->totalSize = offset;

    if (gDvmJit.codeCacheByteUsed + cUnit->totalSize > CODE_CACHE_SIZE) {
        gDvmJit.codeCacheFull = true;
        cUnit->baseAddr = NULL;
        return;
    }

    /* Allocate enough space for the code block */
    cUnit->codeBuffer = dvmCompilerNew(chainCellOffset, true);
    if (cUnit->codeBuffer == NULL) {
        LOGE("Code buffer allocation failure\n");
        cUnit->baseAddr = NULL;
        return;
    }

    bool assemblerFailure = assembleInstructions(
        cUnit, (intptr_t) gDvmJit.codeCache + gDvmJit.codeCacheByteUsed);

    /*
     * Currently the only reason that can cause the assembler to fail is due to
     * trace length - cut it in half and retry.
     */
    if (assemblerFailure) {
        cUnit->halveInstCount = true;
        return;
    }

    /* Don't go all the way if the goal is just to get the verbose output */
    if (info->discardResult) return;

    cUnit->baseAddr = (char *) gDvmJit.codeCache + gDvmJit.codeCacheByteUsed;
    gDvmJit.codeCacheByteUsed += offset;

    /* Install the code block */
    memcpy((char*)cUnit->baseAddr, cUnit->codeBuffer, chainCellOffset);
    gDvmJit.numCompilations++;

    /* Install the chaining cell counts */
    for (i=0; i< kChainingCellLast; i++) {
        chainCellCounts.u.count[i] = cUnit->numChainingCells[i];
    }
    memcpy((char*)cUnit->baseAddr + chainCellOffset, &chainCellCounts,
           sizeof(chainCellCounts));

    /* Install the trace description */
    memcpy((char*)cUnit->baseAddr + chainCellOffset + sizeof(chainCellCounts),
           cUnit->traceDesc, descSize);

    /* Write the literals directly into the code cache */
    installDataContent(cUnit);

    /* Flush dcache and invalidate the icache to maintain coherence */
    cacheflush((long)cUnit->baseAddr,
               (long)((char *) cUnit->baseAddr + offset), 0);

    /* Record code entry point and instruction set */
    info->codeAddress = (char*)cUnit->baseAddr + cUnit->headerSize;
    /* If applicable, mark low bit to denote thumb */
    if (info->instructionSet != DALVIK_JIT_ARM)
        info->codeAddress = (char*)info->codeAddress + 1;
}

/*
 * Returns the skeleton bit pattern associated with an opcode.  All
 * variable fields are zeroed.
 */
static u4 getSkeleton(ArmOpCode op)
{
    return EncodingMap[op].skeleton;
}

static u4 assembleChainingBranch(int branchOffset, bool thumbTarget)
{
    u4 thumb1, thumb2;

    if (!thumbTarget) {
        thumb1 =  (getSkeleton(kThumbBlx1) | ((branchOffset>>12) & 0x7ff));
        thumb2 =  (getSkeleton(kThumbBlx2) | ((branchOffset>> 1) & 0x7ff));
    } else if ((branchOffset < -2048) | (branchOffset > 2046)) {
        thumb1 =  (getSkeleton(kThumbBl1) | ((branchOffset>>12) & 0x7ff));
        thumb2 =  (getSkeleton(kThumbBl2) | ((branchOffset>> 1) & 0x7ff));
    } else {
        thumb1 =  (getSkeleton(kThumbBUncond) | ((branchOffset>> 1) & 0x7ff));
        thumb2 =  getSkeleton(kThumbOrr);  /* nop -> or r0, r0 */
    }

    return thumb2<<16 | thumb1;
}

/*
 * Perform translation chain operation.
 * For ARM, we'll use a pair of thumb instructions to generate
 * an unconditional chaining branch of up to 4MB in distance.
 * Use a BL, because the generic "interpret" translation needs
 * the link register to find the dalvik pc of teh target.
 *     111HHooooooooooo
 * Where HH is 10 for the 1st inst, and 11 for the second and
 * the "o" field is each instruction's 11-bit contribution to the
 * 22-bit branch offset.
 * If the target is nearby, use a single-instruction bl.
 * If one or more threads is suspended, don't chain.
 */
void* dvmJitChain(void* tgtAddr, u4* branchAddr)
{
    int baseAddr = (u4) branchAddr + 4;
    int branchOffset = (int) tgtAddr - baseAddr;
    u4 newInst;
    bool thumbTarget;

    if (gDvm.sumThreadSuspendCount == 0) {
        assert((branchOffset >= -(1<<22)) && (branchOffset <= ((1<<22)-2)));

        gDvmJit.translationChains++;

        COMPILER_TRACE_CHAINING(
            LOGD("Jit Runtime: chaining 0x%x to 0x%x\n",
                 (int) branchAddr, (int) tgtAddr & -2));

        /*
         * NOTE: normally, all translations are Thumb[2] mode, with
         * a single exception: the default TEMPLATE_INTERPRET
         * pseudo-translation.  If the need ever arises to
         * mix Arm & Thumb[2] translations, the following code should be
         * generalized.
         */
        thumbTarget = (tgtAddr != gDvmJit.interpretTemplate);

        newInst = assembleChainingBranch(branchOffset, thumbTarget);

        *branchAddr = newInst;
        cacheflush((long)branchAddr, (long)branchAddr + 4, 0);
    }

    return tgtAddr;
}

/*
 * This method is called from the invoke templates for virtual and interface
 * methods to speculatively setup a chain to the callee. The templates are
 * written in assembly and have setup method, cell, and clazz at r0, r2, and
 * r3 respectively, so there is a unused argument in the list. Upon return one
 * of the following three results may happen:
 *   1) Chain is not setup because the callee is native. Reset the rechain
 *      count to a big number so that it will take a long time before the next
 *      rechain attempt to happen.
 *   2) Chain is not setup because the callee has not been created yet. Reset
 *      the rechain count to a small number and retry in the near future.
 *   3) Ask all other threads to stop before patching this chaining cell.
 *      This is required because another thread may have passed the class check
 *      but hasn't reached the chaining cell yet to follow the chain. If we
 *      patch the content before halting the other thread, there could be a
 *      small window for race conditions to happen that it may follow the new
 *      but wrong chain to invoke a different method.
 */
const Method *dvmJitToPatchPredictedChain(const Method *method,
                                          void *unused,
                                          PredictedChainingCell *cell,
                                          const ClassObject *clazz)
{
#if defined(WITH_SELF_VERIFICATION)
    /* Disable chaining and prevent this from triggering again for a while */
    cell->counter = PREDICTED_CHAIN_COUNTER_AVOID;
    cacheflush((long) cell, (long) (cell+1), 0);
    goto done;
#else
    /* Don't come back here for a long time if the method is native */
    if (dvmIsNativeMethod(method)) {
        cell->counter = PREDICTED_CHAIN_COUNTER_AVOID;
        cacheflush((long) cell, (long) (cell+1), 0);
        COMPILER_TRACE_CHAINING(
            LOGD("Jit Runtime: predicted chain %p to native method %s ignored",
                 cell, method->name));
        goto done;
    }
    int tgtAddr = (int) dvmJitGetCodeAddr(method->insns);

    /*
     * Compilation not made yet for the callee. Reset the counter to a small
     * value and come back to check soon.
     */
    if ((tgtAddr == 0) || ((void*)tgtAddr == gDvmJit.interpretTemplate)) {
        /*
         * Wait for a few invocations (currently set to be 16) before trying
         * to setup the chain again.
         */
        cell->counter = PREDICTED_CHAIN_COUNTER_DELAY;
        cacheflush((long) cell, (long) (cell+1), 0);
        COMPILER_TRACE_CHAINING(
            LOGD("Jit Runtime: predicted chain %p to method %s%s delayed",
                 cell, method->clazz->descriptor, method->name));
        goto done;
    }

    /*
     * Bump up the counter first just in case other mutator threads are in
     * nearby territory to also attempt to rechain this cell. This is not
     * done in a thread-safe way and doesn't need to be since the consequence
     * of the race condition [rare] is two back-to-back suspend-all attempts,
     * which will be handled correctly.
     */
    cell->counter = PREDICTED_CHAIN_COUNTER_AVOID;

    /* Stop the world */
    dvmSuspendAllThreads(SUSPEND_FOR_IC_PATCH);

    int baseAddr = (int) cell + 4;   // PC is cur_addr + 4
    int branchOffset = tgtAddr - baseAddr;

    COMPILER_TRACE_CHAINING(
        LOGD("Jit Runtime: predicted chain %p from %s to %s (%s) patched",
             cell, cell->clazz ? cell->clazz->descriptor : "NULL",
             clazz->descriptor,
             method->name));

    cell->branch = assembleChainingBranch(branchOffset, true);
    cell->clazz = clazz;
    cell->method = method;
    /*
     * Reset the counter again in case other mutator threads got invoked
     * between the previous rest and dvmSuspendAllThreads call.
     */
    cell->counter = PREDICTED_CHAIN_COUNTER_RECHAIN;

    cacheflush((long) cell, (long) (cell+1), 0);

    /* All done - resume all other threads */
    dvmResumeAllThreads(SUSPEND_FOR_IC_PATCH);
#endif

done:
    return method;
}

/*
 * Unchain a trace given the starting address of the translation
 * in the code cache.  Refer to the diagram in dvmCompilerAssembleLIR.
 * Returns the address following the last cell unchained.  Note that
 * the incoming codeAddr is a thumb code address, and therefore has
 * the low bit set.
 */
u4* dvmJitUnchain(void* codeAddr)
{
    u2* pChainCellOffset = (u2*)((char*)codeAddr - 3);
    u2 chainCellOffset = *pChainCellOffset;
    ChainCellCounts *pChainCellCounts =
          (ChainCellCounts*)((char*)codeAddr + chainCellOffset - 3);
    int cellSize;
    u4* pChainCells;
    u4* pStart;
    u4 thumb1;
    u4 thumb2;
    u4 newInst;
    int i,j;
    PredictedChainingCell *predChainCell;

    /* Get total count of chain cells */
    for (i = 0, cellSize = 0; i < kChainingCellLast; i++) {
        if (i != kChainingCellInvokePredicted) {
            cellSize += pChainCellCounts->u.count[i] * 2;
        } else {
            cellSize += pChainCellCounts->u.count[i] * 4;
        }
    }

    /* Locate the beginning of the chain cell region */
    pStart = pChainCells = ((u4 *) pChainCellCounts) - cellSize;

    /* The cells are sorted in order - walk through them and reset */
    for (i = 0; i < kChainingCellLast; i++) {
        int elemSize = 2; /* Most chaining cell has two words */
        if (i == kChainingCellInvokePredicted) {
            elemSize = 4;
        }

        for (j = 0; j < pChainCellCounts->u.count[i]; j++) {
            int targetOffset;
            switch(i) {
                case kChainingCellNormal:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToInterpNormal);
                    break;
                case kChainingCellHot:
                case kChainingCellInvokeSingleton:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToTraceSelect);
                    break;
                case kChainingCellInvokePredicted:
                    targetOffset = 0;
                    predChainCell = (PredictedChainingCell *) pChainCells;
                    /* Reset the cell to the init state */
                    predChainCell->branch = PREDICTED_CHAIN_BX_PAIR_INIT;
                    predChainCell->clazz = PREDICTED_CHAIN_CLAZZ_INIT;
                    predChainCell->method = PREDICTED_CHAIN_METHOD_INIT;
                    predChainCell->counter = PREDICTED_CHAIN_COUNTER_INIT;
                    break;
#if defined(WITH_SELF_VERIFICATION)
                case kChainingCellBackwardBranch:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToBackwardBranch);
                    break;
#elif defined(WITH_JIT_TUNING)
                case kChainingCellBackwardBranch:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToInterpNormal);
                    break;
#endif
                default:
                    targetOffset = 0; // make gcc happy
                    LOGE("Unexpected chaining type: %d", i);
                    dvmAbort();
            }
            COMPILER_TRACE_CHAINING(
                LOGD("Jit Runtime: unchaining 0x%x", (int)pChainCells));
            /*
             * Thumb code sequence for a chaining cell is:
             *     ldr  r0, rGLUE, #<word offset>
             *     blx  r0
             */
            if (i != kChainingCellInvokePredicted) {
                targetOffset = targetOffset >> 2;  /* convert to word offset */
                thumb1 = 0x6800 | (targetOffset << 6) |
                         (rGLUE << 3) | (r0 << 0);
                thumb2 = 0x4780 | (r0 << 3);
                newInst = thumb2<<16 | thumb1;
                *pChainCells = newInst;
            }
            pChainCells += elemSize;  /* Advance by a fixed number of words */
        }
    }
    return pChainCells;
}

/* Unchain all translation in the cache. */
void dvmJitUnchainAll()
{
    u4* lowAddress = NULL;
    u4* highAddress = NULL;
    unsigned int i;
    if (gDvmJit.pJitEntryTable != NULL) {
        COMPILER_TRACE_CHAINING(LOGD("Jit Runtime: unchaining all"));
        dvmLockMutex(&gDvmJit.tableLock);
        for (i = 0; i < gDvmJit.jitTableSize; i++) {
            if (gDvmJit.pJitEntryTable[i].dPC &&
                   gDvmJit.pJitEntryTable[i].codeAddress &&
                   (gDvmJit.pJitEntryTable[i].codeAddress !=
                    gDvmJit.interpretTemplate)) {
                u4* lastAddress;
                lastAddress =
                      dvmJitUnchain(gDvmJit.pJitEntryTable[i].codeAddress);
                if (lowAddress == NULL ||
                      (u4*)gDvmJit.pJitEntryTable[i].codeAddress < lowAddress)
                    lowAddress = lastAddress;
                if (lastAddress > highAddress)
                    highAddress = lastAddress;
            }
        }
        cacheflush((long)lowAddress, (long)highAddress, 0);
        dvmUnlockMutex(&gDvmJit.tableLock);
    }
}

typedef struct jitProfileAddrToLine {
    u4 lineNum;
    u4 bytecodeOffset;
} jitProfileAddrToLine;


/* Callback function to track the bytecode offset/line number relationiship */
static int addrToLineCb (void *cnxt, u4 bytecodeOffset, u4 lineNum)
{
    jitProfileAddrToLine *addrToLine = (jitProfileAddrToLine *) cnxt;

    /* Best match so far for this offset */
    if (addrToLine->bytecodeOffset >= bytecodeOffset) {
        addrToLine->lineNum = lineNum;
    }
    return 0;
}

char *getTraceBase(const JitEntry *p)
{
    return (char*)p->codeAddress -
        (6 + (p->u.info.instructionSet == DALVIK_JIT_ARM ? 0 : 1));
}

/* Dumps profile info for a single trace */
static int dumpTraceProfile(JitEntry *p)
{
    ChainCellCounts* pCellCounts;
    char* traceBase;
    u4* pExecutionCount;
    u2* pCellOffset;
    JitTraceDescription *desc;
    const Method* method;

    traceBase = getTraceBase(p);

    if (p->codeAddress == NULL) {
        LOGD("TRACEPROFILE 0x%08x 0 NULL 0 0", (int)traceBase);
        return 0;
    }
    if (p->codeAddress == gDvmJit.interpretTemplate) {
        LOGD("TRACEPROFILE 0x%08x 0 INTERPRET_ONLY  0 0", (int)traceBase);
        return 0;
    }

    pExecutionCount = (u4*) (traceBase);
    pCellOffset = (u2*) (traceBase + 4);
    pCellCounts = (ChainCellCounts*) ((char *)pCellOffset + *pCellOffset);
    desc = (JitTraceDescription*) ((char*)pCellCounts + sizeof(*pCellCounts));
    method = desc->method;
    char *methodDesc = dexProtoCopyMethodDescriptor(&method->prototype);
    jitProfileAddrToLine addrToLine = {0, desc->trace[0].frag.startOffset};

    /*
     * We may end up decoding the debug information for the same method
     * multiple times, but the tradeoff is we don't need to allocate extra
     * space to store the addr/line mapping. Since this is a debugging feature
     * and done infrequently so the slower but simpler mechanism should work
     * just fine.
     */
    dexDecodeDebugInfo(method->clazz->pDvmDex->pDexFile,
                       dvmGetMethodCode(method),
                       method->clazz->descriptor,
                       method->prototype.protoIdx,
                       method->accessFlags,
                       addrToLineCb, NULL, &addrToLine);

    LOGD("TRACEPROFILE 0x%08x % 10d [%#x(+%d), %d] %s%s;%s",
         (int)traceBase,
         *pExecutionCount,
         desc->trace[0].frag.startOffset,
         desc->trace[0].frag.numInsts,
         addrToLine.lineNum,
         method->clazz->descriptor, method->name, methodDesc);
    free(methodDesc);

    return *pExecutionCount;
}

/* Create a copy of the trace descriptor of an existing compilation */
JitTraceDescription *dvmCopyTraceDescriptor(const u2 *pc)
{
    JitEntry *jitEntry = dvmFindJitEntry(pc);
    if (jitEntry == NULL) return NULL;

    /* Find out the startint point */
    char *traceBase = getTraceBase(jitEntry);

    /* Then find out the starting point of the chaining cell */
    u2 *pCellOffset = (u2*) (traceBase + 4);
    ChainCellCounts *pCellCounts =
        (ChainCellCounts*) ((char *)pCellOffset + *pCellOffset);

    /* From there we can find out the starting point of the trace descriptor */
    JitTraceDescription *desc =
        (JitTraceDescription*) ((char*)pCellCounts + sizeof(*pCellCounts));

    /* Now make a copy and return */
    int descSize = jitTraceDescriptionSize(desc);
    JitTraceDescription *newCopy = (JitTraceDescription *) malloc(descSize);
    memcpy(newCopy, desc, descSize);
    return newCopy;
}

/* Handy function to retrieve the profile count */
static inline int getProfileCount(const JitEntry *entry)
{
    if (entry->dPC == 0 || entry->codeAddress == 0)
        return 0;
    u4 *pExecutionCount = (u4 *) getTraceBase(entry);

    return *pExecutionCount;
}


/* qsort callback function */
static int sortTraceProfileCount(const void *entry1, const void *entry2)
{
    const JitEntry *jitEntry1 = entry1;
    const JitEntry *jitEntry2 = entry2;

    int count1 = getProfileCount(jitEntry1);
    int count2 = getProfileCount(jitEntry2);
    return (count1 == count2) ? 0 : ((count1 > count2) ? -1 : 1);
}

/* Sort the trace profile counts and dump them */
void dvmCompilerSortAndPrintTraceProfiles()
{
    JitEntry *sortedEntries;
    int numTraces = 0;
    unsigned long counts = 0;
    unsigned int i;

    /* Make sure that the table is not changing */
    dvmLockMutex(&gDvmJit.tableLock);

    /* Sort the entries by descending order */
    sortedEntries = malloc(sizeof(JitEntry) * gDvmJit.jitTableSize);
    if (sortedEntries == NULL)
        goto done;
    memcpy(sortedEntries, gDvmJit.pJitEntryTable,
           sizeof(JitEntry) * gDvmJit.jitTableSize);
    qsort(sortedEntries, gDvmJit.jitTableSize, sizeof(JitEntry),
          sortTraceProfileCount);

    /* Dump the sorted entries */
    for (i=0; i < gDvmJit.jitTableSize; i++) {
        if (sortedEntries[i].dPC != 0) {
            counts += dumpTraceProfile(&sortedEntries[i]);
            numTraces++;
        }
    }
    if (numTraces == 0)
        numTraces = 1;
    LOGD("JIT: Average execution count -> %d",(int)(counts / numTraces));

    free(sortedEntries);
done:
    dvmUnlockMutex(&gDvmJit.tableLock);
    return;
}

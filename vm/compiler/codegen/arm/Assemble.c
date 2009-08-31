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
                     k3, k3s, k3e, operands, name, fmt, size) \
        {skeleton, {{k0, ds, de}, {k1, s1s, s1e}, {k2, s2s, s2e}, \
                    {k3, k3s, k3e}}, opcode, operands, name, fmt, size}

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
 *     D -> decimal+8 (used to convert 3-bit regnum field to high reg)
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
ArmEncodingMap EncodingMap[ARM_LAST] = {
    ENCODING_MAP(ARM_16BIT_DATA,    0x0000,
                 BITBLT, 15, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP,
                 "data", "0x!0h(!0d)", 1),
    ENCODING_MAP(THUMB_ADC,           0x4140,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES | USES_CCODES,
                 "adcs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_ADD_RRI3,      0x1c00,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "adds", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(THUMB_ADD_RI8,       0x3000,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "adds", "r!0d, r!0d, #!1d", 1),
    ENCODING_MAP(THUMB_ADD_RRR,       0x1800,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "adds", "r!0d, r!1d, r!2d", 1),
    ENCODING_MAP(THUMB_ADD_RR_LH,     0x4440,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_ADD_RR_HL,     0x4480,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_ADD_RR_HH,     0x44c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_ADD_PC_REL,    0xa000,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "add", "r!0d, pc, #!1E", 1),
    ENCODING_MAP(THUMB_ADD_SP_REL,    0xa800,
                 BITBLT, 10, 8, UNUSED, -1, -1, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "add", "r!0d, sp, #!2E", 1),
    ENCODING_MAP(THUMB_ADD_SPI7,      0xb000,
                 BITBLT, 6, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | CLOBBER_DEST,
                 "add", "sp, #!0d*4", 1),
    ENCODING_MAP(THUMB_AND_RR,        0x4000,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "ands", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_ASR,           0x1000,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "asrs", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(THUMB_ASRV,          0x4100,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "asrs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_B_COND,        0xd000,
                 BITBLT, 7, 0, BITBLT, 11, 8, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | IS_BRANCH | USES_CCODES,
                 "b!1c", "!0t", 1),
    ENCODING_MAP(THUMB_B_UNCOND,      0xe000,
                 BITBLT, 10, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 NO_OPERAND | IS_BRANCH,
                 "b", "!0t", 1),
    ENCODING_MAP(THUMB_BIC,           0x4380,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "bics", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_BKPT,          0xbe00,
                 BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bkpt", "!0d", 1),
    ENCODING_MAP(THUMB_BLX_1,         0xf000,
                 BITBLT, 10, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | IS_BRANCH,
                 "blx_1", "!0u", 1),
    ENCODING_MAP(THUMB_BLX_2,         0xe800,
                 BITBLT, 10, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | IS_BRANCH,
                 "blx_2", "!0v", 1),
    ENCODING_MAP(THUMB_BL_1,          0xf000,
                 BITBLT, 10, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bl_1", "!0u", 1),
    ENCODING_MAP(THUMB_BL_2,          0xf800,
                 BITBLT, 10, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bl_2", "!0v", 1),
    ENCODING_MAP(THUMB_BLX_R,         0x4780,
                 BITBLT, 6, 3, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "blx", "r!0d", 1),
    ENCODING_MAP(THUMB_BX,            0x4700,
                 BITBLT, 6, 3, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bx", "r!0d", 1),
    ENCODING_MAP(THUMB_CMN,           0x42c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | SETS_CCODES,
                 "cmn", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_CMP_RI8,       0x2800,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | SETS_CCODES,
                 "cmp", "r!0d, #!1d", 1),
    ENCODING_MAP(THUMB_CMP_RR,        0x4280,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_CMP_LH,        0x4540,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | SETS_CCODES,
                 "cmp", "r!0d, r!1D", 1),
    ENCODING_MAP(THUMB_CMP_HL,        0x4580,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | SETS_CCODES,
                 "cmp", "r!0D, r!1d", 1),
    ENCODING_MAP(THUMB_CMP_HH,        0x45c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | SETS_CCODES,
                 "cmp", "r!0D, r!1D", 1),
    ENCODING_MAP(THUMB_EOR,           0x4040,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "eors", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_LDMIA,         0xc800,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | CLOBBER_SRC1,
                 "ldmia", "r!0d!!, <!1R>", 1),
    ENCODING_MAP(THUMB_LDR_RRI5,      0x6800,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [r!1d, #!2E]", 1),
    ENCODING_MAP(THUMB_LDR_RRR,       0x5800,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_LDR_PC_REL,    0x4800,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [pc, #!1E]", 1),
    ENCODING_MAP(THUMB_LDR_SP_REL,    0x9800,
                 BITBLT, 10, 8, UNUSED, -1, -1, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [sp, #!2E]", 1),
    ENCODING_MAP(THUMB_LDRB_RRI5,     0x7800,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrb", "r!0d, [r!1d, #2d]", 1),
    ENCODING_MAP(THUMB_LDRB_RRR,      0x5c00,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrb", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_LDRH_RRI5,     0x8800,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrh", "r!0d, [r!1d, #!2F]", 1),
    ENCODING_MAP(THUMB_LDRH_RRR,      0x5a00,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrh", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_LDRSB_RRR,     0x5600,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrsb", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_LDRSH_RRR,     0x5e00,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrsh", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_LSL,           0x0000,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "lsls", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(THUMB_LSLV,          0x4080,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "lsls", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_LSR,           0x0800,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "lsrs", "r!0d, r!1d, #!2d", 1),
    ENCODING_MAP(THUMB_LSRV,          0x40c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "lsrs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_MOV_IMM,       0x2000,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "movs", "r!0d, #!1d", 1),
    ENCODING_MAP(THUMB_MOV_RR,        0x1c00,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "movs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_MOV_RR_H2H,    0x46c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0D, r!1D", 1),
    ENCODING_MAP(THUMB_MOV_RR_H2L,    0x4640,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, r!1D", 1),
    ENCODING_MAP(THUMB_MOV_RR_L2H,    0x4680,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0D, r!1d", 1),
    ENCODING_MAP(THUMB_MUL,           0x4340,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "muls", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_MVN,           0x43c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "mvns", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_NEG,           0x4240,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "negs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_ORR,           0x4300,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "orrs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_POP,           0xbc00,
                 BITBLT, 8, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP,
                 "pop", "<!0R>", 1),
    ENCODING_MAP(THUMB_PUSH,          0xb400,
                 BITBLT, 8, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP,
                 "push", "<!0R>", 1),
    ENCODING_MAP(THUMB_RORV,           0x41c0,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "rors", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_SBC,           0x4180,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | USES_CCODES | SETS_CCODES,
                 "sbcs", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB_STMIA,         0xc000,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_SRC1,
                 "stmia", "r!0d!!, <!1R>", 1),
    ENCODING_MAP(THUMB_STR_RRI5,      0x6000,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "str", "r!0d, [r!1d, #!2E]", 1),
    ENCODING_MAP(THUMB_STR_RRR,       0x5000,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "str", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_STR_SP_REL,    0x9000,
                 BITBLT, 10, 8, UNUSED, -1, -1, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "str", "r!0d, [sp, #!2E]", 1),
    ENCODING_MAP(THUMB_STRB_RRI5,     0x7000,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "strb", "r!0d, [r!1d, #!2d]", 1),
    ENCODING_MAP(THUMB_STRB_RRR,      0x5400,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "strb", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_STRH_RRI5,     0x8000,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 10, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "strh", "r!0d, [r!1d, #!2F]", 1),
    ENCODING_MAP(THUMB_STRH_RRR,      0x5200,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "strh", "r!0d, [r!1d, r!2d]", 1),
    ENCODING_MAP(THUMB_SUB_RRI3,      0x1e00,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "subs", "r!0d, r!1d, #!2d]", 1),
    ENCODING_MAP(THUMB_SUB_RI8,       0x3800,
                 BITBLT, 10, 8, BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "subs", "r!0d, #!1d", 1),
    ENCODING_MAP(THUMB_SUB_RRR,       0x1a00,
                 BITBLT, 2, 0, BITBLT, 5, 3, BITBLT, 8, 6, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "subs", "r!0d, r!1d, r!2d", 1),
    ENCODING_MAP(THUMB_SUB_SPI7,      0xb080,
                 BITBLT, 6, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | CLOBBER_DEST,
                 "sub", "sp, #!0d", 1),
    ENCODING_MAP(THUMB_SWI,           0xdf00,
                 BITBLT, 7, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "swi", "!0d", 1),
    ENCODING_MAP(THUMB_TST,           0x4200,
                 BITBLT, 2, 0, BITBLT, 5, 3, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP | SETS_CCODES,
                 "tst", "r!0d, r!1d", 1),
    ENCODING_MAP(THUMB2_VLDRS,       0xed900a00,
                 SFP, 22, 12, BITBLT, 19, 16, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vldr", "!0s, [r!1d, #!2E]", 2),
    ENCODING_MAP(THUMB2_VLDRD,       0xed900b00,
                 DFP, 22, 12, BITBLT, 19, 16, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vldr", "!0S, [r!1d, #!2E]", 2),
    ENCODING_MAP(THUMB2_VMULS,        0xee200a00,
                 SFP, 22, 12, SFP, 7, 16, SFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vmuls", "!0s, !1s, !2s", 2),
    ENCODING_MAP(THUMB2_VMULD,        0xee200b00,
                 DFP, 22, 12, DFP, 7, 16, DFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vmuld", "!0S, !1S, !2S", 2),
    ENCODING_MAP(THUMB2_VSTRS,       0xed800a00,
                 SFP, 22, 12, BITBLT, 19, 16, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "vstr", "!0s, [r!1d, #!2E]", 2),
    ENCODING_MAP(THUMB2_VSTRD,       0xed800b00,
                 DFP, 22, 12, BITBLT, 19, 16, BITBLT, 7, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "vstr", "!0S, [r!1d, #!2E]", 2),
    ENCODING_MAP(THUMB2_VSUBS,        0xee300a40,
                 SFP, 22, 12, SFP, 7, 16, SFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vsub", "!0s, !1s, !2s", 2),
    ENCODING_MAP(THUMB2_VSUBD,        0xee300b40,
                 DFP, 22, 12, DFP, 7, 16, DFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vsub", "!0S, !1S, !2S", 2),
    ENCODING_MAP(THUMB2_VADDS,        0xee300a00,
                 SFP, 22, 12, SFP, 7, 16, SFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vadd", "!0s, !1s, !2s", 2),
    ENCODING_MAP(THUMB2_VADDD,        0xee300b00,
                 DFP, 22, 12, DFP, 7, 16, DFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vadd", "!0S, !1S, !2S", 2),
    ENCODING_MAP(THUMB2_VDIVS,        0xee800a00,
                 SFP, 22, 12, SFP, 7, 16, SFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vdivs", "!0s, !1s, !2s", 2),
    ENCODING_MAP(THUMB2_VDIVD,        0xee800b00,
                 DFP, 22, 12, DFP, 7, 16, DFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "vdivd", "!0S, !1S, !2S", 2),
    ENCODING_MAP(THUMB2_VCVTIF,       0xeeb80ac0,
                 SFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vcvt.f32", "!0s, !1s", 2),
    ENCODING_MAP(THUMB2_VCVTID,       0xeeb80bc0,
                 DFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vcvt.f64", "!0S, !1s", 2),
    ENCODING_MAP(THUMB2_VCVTFI,       0xeebd0ac0,
                 SFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vcvt.s32.f32 ", "!0s, !1s", 2),
    ENCODING_MAP(THUMB2_VCVTDI,       0xeebd0bc0,
                 SFP, 22, 12, DFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vcvt.s32.f64 ", "!0s, !1S", 2),
    ENCODING_MAP(THUMB2_VCVTFD,       0xeeb70ac0,
                 DFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vcvt.f64.f32 ", "!0S, !1s", 2),
    ENCODING_MAP(THUMB2_VCVTDF,       0xeeb70bc0,
                 SFP, 22, 12, DFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vcvt.f32.f64 ", "!0s, !1S", 2),
    ENCODING_MAP(THUMB2_VSQRTS,       0xeeb10ac0,
                 SFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vsqrt.f32 ", "!0s, !1s", 2),
    ENCODING_MAP(THUMB2_VSQRTD,       0xeeb10bc0,
                 DFP, 22, 12, DFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vsqrt.f64 ", "!0S, !1S", 2),
    ENCODING_MAP(THUMB2_MOV_IMM_SHIFT, 0xf04f0000, /* no setflags encoding */
                 BITBLT, 11, 8, MODIMM, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, #!1m", 2),
    ENCODING_MAP(THUMB2_MOV_IMM16,       0xf2400000,
                 BITBLT, 11, 8, IMM16, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, #!1M", 2),
    ENCODING_MAP(THUMB2_STR_RRI12,       0xf8c00000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "str", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_LDR_RRI12,       0xf8d00000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_STR_RRI8_PREDEC,       0xf8400c00,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 8, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP,
                 "str", "r!0d,[r!1d, #-!2d]", 2),
    ENCODING_MAP(THUMB2_LDR_RRI8_PREDEC,       0xf8500c00,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 8, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d,[r!1d, #-!2d]", 2),
    ENCODING_MAP(THUMB2_CBNZ,       0xb900,
                 BITBLT, 2, 0, IMM6, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP,  /* Note: does not affect flags */
                 "cbnz", "r!0d,!1t", 1),
    ENCODING_MAP(THUMB2_CBZ,       0xb100,
                 BITBLT, 2, 0, IMM6, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP, /* Note: does not affect flags */
                 "cbz", "r!0d,!1t", 1),
    ENCODING_MAP(THUMB2_ADD_RRI12,       0xf2000000,
                 BITBLT, 11, 8, BITBLT, 19, 16, IMM12, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,/* Note: doesn't affect flags */
                 "add", "r!0d,r!1d,#!2d", 2),
    ENCODING_MAP(THUMB2_MOV_RR,       0xea4f0000, /* no setflags encoding */
                 BITBLT, 11, 8, BITBLT, 3, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, r!1d", 2),
    ENCODING_MAP(THUMB2_VMOVS,       0xeeb00a40,
                 SFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vmov.f32 ", " !0s, !1s", 2),
    ENCODING_MAP(THUMB2_VMOVD,       0xeeb00b40,
                 DFP, 22, 12, DFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vmov.f64 ", " !0S, !1S", 2),
    ENCODING_MAP(THUMB2_LDMIA,         0xe8900000,
                 BITBLT, 19, 16, BITBLT, 15, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | CLOBBER_SRC1,
                 "ldmia", "r!0d!!, <!1R>", 2),
    ENCODING_MAP(THUMB2_STMIA,         0xe8800000,
                 BITBLT, 19, 16, BITBLT, 15, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_SRC1,
                 "stmia", "r!0d!!, <!1R>", 2),
    ENCODING_MAP(THUMB2_ADD_RRR,  0xeb100000, /* setflags encoding */
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST | SETS_CCODES,
                 "adds", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_SUB_RRR,       0xebb00000, /* setflags enconding */
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST | SETS_CCODES,
                 "subs", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_SBC_RRR,       0xeb700000, /* setflags encoding */
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST | USES_CCODES | SETS_CCODES,
                 "sbcs", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_CMP_RR,       0xebb00f00,
                 BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | SETS_CCODES,
                 "cmp", "r!0d, r!1d", 2),
    ENCODING_MAP(THUMB2_SUB_RRI12,       0xf2a00000,
                 BITBLT, 11, 8, BITBLT, 19, 16, IMM12, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,/* Note: doesn't affect flags */
                 "sub", "r!0d,r!1d,#!2d", 2),
    ENCODING_MAP(THUMB2_MVN_IMM_SHIFT,  0xf06f0000, /* no setflags encoding */
                 BITBLT, 11, 8, MODIMM, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mvn", "r!0d, #!1n", 2),
    ENCODING_MAP(THUMB2_SEL,       0xfaa0f080,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | USES_CCODES,
                 "sel", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_UBFX,       0xf3c00000,
                 BITBLT, 11, 8, BITBLT, 19, 16, LSB, -1, -1, BWIDTH, 4, 0,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "ubfx", "r!0d, r!1d, #!2d, #!3d", 2),
    ENCODING_MAP(THUMB2_SBFX,       0xf3400000,
                 BITBLT, 11, 8, BITBLT, 19, 16, LSB, -1, -1, BWIDTH, 4, 0,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "sbfx", "r!0d, r!1d, #!2d, #!3d", 2),
    ENCODING_MAP(THUMB2_LDR_RRR,    0xf8500000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "ldr", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_LDRH_RRR,    0xf8300000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "ldrh", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_LDRSH_RRR,    0xf9300000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "ldrsh", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_LDRB_RRR,    0xf8100000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "ldrb", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_LDRSB_RRR,    0xf9100000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "ldrsb", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_STR_RRR,    0xf8400000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "str", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_STRH_RRR,    0xf8200000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "strh", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_STRB_RRR,    0xf8000000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 3, 0, BITBLT, 5, 4,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "strb", "r!0d,[r!1d, r!2d, LSL #!3d]", 2),
    ENCODING_MAP(THUMB2_LDRH_RRI12,       0xf8b00000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrh", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_LDRSH_RRI12,       0xf9b00000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrsh", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_LDRB_RRI12,       0xf8900000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrb", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_LDRSB_RRI12,       0xf9900000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrsb", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_STRH_RRI12,       0xf8a00000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "strh", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_STRB_RRI12,       0xf8800000,
                 BITBLT, 15, 12, BITBLT, 19, 16, BITBLT, 11, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "strb", "r!0d,[r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_POP,           0xe8bd0000,
                 BITBLT, 15, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP,
                 "pop", "<!0R>", 2),
    ENCODING_MAP(THUMB2_PUSH,          0xe8ad0000,
                 BITBLT, 15, 0, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_UNARY_OP,
                 "push", "<!0R>", 2),
    ENCODING_MAP(THUMB2_CMP_RI8, 0xf1b00f00,
                 BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "cmp", "r!0d, #!1m", 2),
    ENCODING_MAP(THUMB2_ADC_RRR,  0xeb500000, /* setflags encoding */
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST | SETS_CCODES,
                 "acds", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(THUMB2_AND_RRR,  0xea000000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "and", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(THUMB2_BIC_RRR,  0xea200000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "bic", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(THUMB2_CMN_RR,  0xeb000000,
                 BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "cmn", "r!0d, r!1d, shift !2d", 2),
    ENCODING_MAP(THUMB2_EOR_RRR,  0xea800000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "eor", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(THUMB2_MUL_RRR,  0xfb00f000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "mul", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_MVN_RR,  0xea6f0000,
                 BITBLT, 11, 8, BITBLT, 3, 0, SHIFT, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "mvn", "r!0d, r!1d, shift !2d", 2),
    ENCODING_MAP(THUMB2_RSUB_RRI8,       0xf1d00000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "rsb", "r!0d,r!1d,#!2m", 2),
    ENCODING_MAP(THUMB2_NEG_RR,       0xf1d00000, /* instance of rsub */
                 BITBLT, 11, 8, BITBLT, 19, 16, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "neg", "r!0d,r!1d", 2),
    ENCODING_MAP(THUMB2_ORR_RRR,  0xea400000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1,
                 IS_QUAD_OP | CLOBBER_DEST,
                 "orr", "r!0d, r!1d, r!2d, shift !3d", 2),
    ENCODING_MAP(THUMB2_TST_RR,       0xea100f00,
                 BITBLT, 19, 16, BITBLT, 3, 0, SHIFT, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | SETS_CCODES,
                 "tst", "r!0d, r!1d, shift !2d", 2),
    ENCODING_MAP(THUMB2_LSLV_RRR,  0xfa00f000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "lsl", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_LSRV_RRR,  0xfa20f000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "lsr", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_ASRV_RRR,  0xfa40f000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "asr", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_RORV_RRR,  0xfa60f000,
                 BITBLT, 11, 8, BITBLT, 19, 16, BITBLT, 3, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ror", "r!0d, r!1d, r!2d", 2),
    ENCODING_MAP(THUMB2_LSL_RRI5,  0xea4f0000,
                 BITBLT, 11, 8, BITBLT, 3, 0, SHIFT5, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "lsl", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_LSR_RRI5,  0xea4f0010,
                 BITBLT, 11, 8, BITBLT, 3, 0, SHIFT5, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "lsr", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_ASR_RRI5,  0xea4f0020,
                 BITBLT, 11, 8, BITBLT, 3, 0, SHIFT5, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "asr", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_ROR_RRI5,  0xea4f0030,
                 BITBLT, 11, 8, BITBLT, 3, 0, SHIFT5, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ror", "r!0d, r!1d, #!2d", 2),
    ENCODING_MAP(THUMB2_BIC_RRI8,  0xf0200000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "bic", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_AND_RRI8,  0xf0000000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "and", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_ORR_RRI8,  0xf0400000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "orr", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_EOR_RRI8,  0xf0800000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "eor", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_ADD_RRI8,  0xf1100000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "adds", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_ADC_RRI8,  0xf1500000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES | USES_CCODES,
                 "adcs", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_SUB_RRI8,  0xf1b00000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES,
                 "subs", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_SBC_RRI8,  0xf1700000,
                 BITBLT, 11, 8, BITBLT, 19, 16, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | SETS_CCODES | USES_CCODES,
                 "sbcs", "r!0d, r!1d, #!2m", 2),
    ENCODING_MAP(THUMB2_IT,  0xbf00,
                 BITBLT, 7, 4, BITBLT, 3, 0, MODIMM, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | USES_CCODES,
                 "it:!1b", "!0c", 1),
    ENCODING_MAP(THUMB2_FMSTAT,  0xeef1fa10,
                 UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1, UNUSED, -1, -1,
                 NO_OPERAND | SETS_CCODES,
                 "fmstat", "", 2),
    ENCODING_MAP(THUMB2_VCMPD,        0xeeb40b40,
                 DFP, 22, 12, DFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP,
                 "vcmp.f64", "!0S, !1S", 2),
    ENCODING_MAP(THUMB2_VCMPS,        0xeeb40a40,
                 SFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP,
                 "vcmp.f32", "!0s, !1s", 2),
    ENCODING_MAP(THUMB2_LDR_PC_REL12,       0xf8df0000,
                 BITBLT, 15, 12, BITBLT, 11, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d,[rpc, #!1d", 2),
    ENCODING_MAP(THUMB2_B_COND,        0xf0008000,
                 BROFFSET, -1, -1, BITBLT, 25, 22, UNUSED, -1, -1,
                 UNUSED, -1, -1,
                 IS_BINARY_OP | IS_BRANCH | USES_CCODES,
                 "b!1c", "!0t", 2),
    ENCODING_MAP(THUMB2_VMOVD_RR,       0xeeb00b40,
                 DFP, 22, 12, DFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vmov.f64", "!0S, !1S", 2),
    ENCODING_MAP(THUMB2_VMOVD_RR,       0xeeb00a40,
                 SFP, 22, 12, SFP, 5, 0, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "vmov.f32", "!0S, !1S", 2),
    ENCODING_MAP(THUMB2_FMRS,       0xee100a10,
                 BITBLT, 15, 12, SFP, 8, 16, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "fmrs", "r!0d, !1s", 2),
    ENCODING_MAP(THUMB2_FMSR,       0xee000a10,
                 SFP, 8, 16, BITBLT, 15, 12, UNUSED, -1, -1, UNUSED, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "fmsr", "!0s, r!1d", 2),
    ENCODING_MAP(THUMB2_FMRRD,       0xec500b10,
                 BITBLT, 15, 12, BITBLT, 19, 16, DFP, 5, 0, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST | CLOBBER_SRC1,
                 "fmrrd", "r!0d, r!1d, !2S", 2),
    ENCODING_MAP(THUMB2_FMDRR,       0xec400b10,
                 DFP, 5, 0, BITBLT, 15, 12, BITBLT, 19, 16, UNUSED, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "fmdrr", "!0S, r!1d, r!2d", 2),
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
            if ((lir->opCode == ARM_PSEUDO_ALIGN4) &&
                /* 1 means padding is needed */
                (lir->operands[0] == 1)) {
                *bufferAddr++ = PADDING_MOV_R5_R5;
            }
            continue;
        }

        if (lir->isNop) {
            continue;
        }

        if (lir->opCode == THUMB_LDR_PC_REL ||
            lir->opCode == THUMB2_LDR_PC_REL12 ||
            lir->opCode == THUMB_ADD_PC_REL) {
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
            if ((lir->opCode == THUMB2_LDR_PC_REL12) && (delta > 4091)) {
                return true;
            } else if (delta > 1020) {
                return true;
            }
            lir->operands[1] = (lir->opCode == THUMB2_LDR_PC_REL12) ?
                                delta : delta >> 2;
        } else if (lir->opCode == THUMB2_CBNZ || lir->opCode == THUMB2_CBZ) {
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
        } else if (lir->opCode == THUMB_B_COND ||
                   lir->opCode == THUMB2_B_COND) {
            ArmLIR *targetLIR = (ArmLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if ((lir->opCode == THUMB_B_COND) && (delta > 254 || delta < -256)) {
                return true;
            }
            lir->operands[0] = delta >> 1;
        } else if (lir->opCode == THUMB_B_UNCOND) {
            ArmLIR *targetLIR = (ArmLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if (delta > 2046 || delta < -2048) {
                LOGE("Unconditional branch distance out of range: %d\n", delta);
                dvmAbort();
            }
            lir->operands[0] = delta >> 1;
        } else if (lir->opCode == THUMB_BLX_1) {
            assert(NEXT_LIR(lir)->opCode == THUMB_BLX_2);
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
                case UNUSED:
                    break;
                case BROFFSET:
                    value = ((operand  & 0x80000) >> 19) << 26;
                    value |= ((operand & 0x40000) >> 18) << 11;
                    value |= ((operand & 0x20000) >> 17) << 13;
                    value |= ((operand & 0x1f800) >> 11) << 16;
                    value |= (operand  & 0x007ff);
                    break;
                case SHIFT5:
                    value = ((operand & 0x1c) >> 2) << 12;
                    value |= (operand & 0x03) << 6;
                    bits |= value;
                    break;
                case SHIFT:
                    value = ((operand & 0x70) >> 4) << 12;
                    value |= (operand & 0x0f) << 4;
                    bits |= value;
                    break;
                case BWIDTH:
                    value = operand - 1;
                    bits |= value;
                    break;
                case LSB:
                    value = ((operand & 0x1c) >> 2) << 12;
                    value |= (operand & 0x03) << 6;
                    bits |= value;
                    break;
                case IMM6:
                    value = ((operand & 0x20) >> 5) << 9;
                    value |= (operand & 0x1f) << 3;
                    bits |= value;
                    break;
                case BITBLT:
                    value = (operand << encoder->fieldLoc[i].start) &
                            ((1 << (encoder->fieldLoc[i].end + 1)) - 1);
                    bits |= value;
                    break;
                case DFP:
                    /* Snag the 1-bit slice and position it */
                    value = ((operand & 0x10) >> 4) <<
                            encoder->fieldLoc[i].end;
                    /* Extract and position the 4-bit slice */
                    value |= (operand & 0x0f) <<
                            encoder->fieldLoc[i].start;
                    bits |= value;
                    break;
                case SFP:
                    /* Snag the 1-bit slice and position it */
                    value = (operand & 0x1) <<
                            encoder->fieldLoc[i].end;
                    /* Extract and position the 4-bit slice */
                    value |= ((operand & 0x1e) >> 1) <<
                            encoder->fieldLoc[i].start;
                    bits |= value;
                    break;
                case IMM12:
                case MODIMM:
                    value = ((operand & 0x800) >> 11) << 26;
                    value |= ((operand & 0x700) >> 8) << 12;
                    value |= operand & 0x0ff;
                    bits |= value;
                    break;
                case IMM16:
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

    info->codeAddress = NULL;
    info->instructionSet = cUnit->instructionSet;

    /* Beginning offset needs to allow space for chain cell offset */
    for (armLIR = (ArmLIR *) cUnit->firstLIRInsn;
         armLIR;
         armLIR = NEXT_LIR(armLIR)) {
        armLIR->generic.offset = offset;
        if (armLIR->opCode >= 0 && !armLIR->isNop) {
            armLIR->size = EncodingMap[armLIR->opCode].size * 2;
            offset += armLIR->size;
        } else if (armLIR->opCode == ARM_PSEUDO_ALIGN4) {
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
    assert(chainCellOffsetLIR->opCode == ARM_16BIT_DATA &&
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


    cUnit->baseAddr = (char *) gDvmJit.codeCache + gDvmJit.codeCacheByteUsed;
    gDvmJit.codeCacheByteUsed += offset;

    /* Install the code block */
    memcpy((char*)cUnit->baseAddr, cUnit->codeBuffer, chainCellOffset);
    gDvmJit.numCompilations++;

    /* Install the chaining cell counts */
    for (i=0; i< CHAINING_CELL_LAST; i++) {
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
    info->instructionSet = cUnit->instructionSet;
    /* If applicable, mark low bit to denote thumb */
    if (info->instructionSet != DALVIK_JIT_ARM)
        info->codeAddress = (char*)info->codeAddress + 1;
}

static u4 assembleBXPair(int branchOffset)
{
    u4 thumb1, thumb2;

    if ((branchOffset < -2048) | (branchOffset > 2046)) {
        thumb1 =  (0xf000 | ((branchOffset>>12) & 0x7ff));
        thumb2 =  (0xf800 | ((branchOffset>> 1) & 0x7ff));
    } else {
        thumb1 =  (0xe000 | ((branchOffset>> 1) & 0x7ff));
        thumb2 =  0x4300;  /* nop -> or r0, r0 */
    }

    return thumb2<<16 | thumb1;
}

/*
 * Perform translation chain operation.
 * For ARM, we'll use a pair of thumb instructions to generate
 * an unconditional chaining branch of up to 4MB in distance.
 * Use a BL, though we don't really need the link.  The format is
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

    if (gDvm.sumThreadSuspendCount == 0) {
        assert((branchOffset >= -(1<<22)) && (branchOffset <= ((1<<22)-2)));

        gDvmJit.translationChains++;

        COMPILER_TRACE_CHAINING(
            LOGD("Jit Runtime: chaining 0x%x to 0x%x\n",
                 (int) branchAddr, (int) tgtAddr & -2));

        newInst = assembleBXPair(branchOffset);

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
    if (tgtAddr == 0) {
        /*
         * Wait for a few invocations (currently set to be 16) before trying
         * to setup the chain again.
         */
        cell->counter = PREDICTED_CHAIN_COUNTER_DELAY;
        cacheflush((long) cell, (long) (cell+1), 0);
        COMPILER_TRACE_CHAINING(
            LOGD("Jit Runtime: predicted chain %p to method %s delayed",
                 cell, method->name));
        goto done;
    }

    /* Stop the world */
    dvmSuspendAllThreads(SUSPEND_FOR_JIT);

    int baseAddr = (int) cell + 4;   // PC is cur_addr + 4
    int branchOffset = tgtAddr - baseAddr;

    COMPILER_TRACE_CHAINING(
        LOGD("Jit Runtime: predicted chain %p from %s to %s (%s) patched",
             cell, cell->clazz ? cell->clazz->descriptor : "NULL",
             clazz->descriptor,
             method->name));

    cell->branch = assembleBXPair(branchOffset);
    cell->clazz = clazz;
    cell->method = method;
    cell->counter = PREDICTED_CHAIN_COUNTER_RECHAIN;

    cacheflush((long) cell, (long) (cell+1), 0);

    /* All done - resume all other threads */
    dvmResumeAllThreads(SUSPEND_FOR_JIT);
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
    for (i = 0, cellSize = 0; i < CHAINING_CELL_LAST; i++) {
        if (i != CHAINING_CELL_INVOKE_PREDICTED) {
            cellSize += pChainCellCounts->u.count[i] * 2;
        } else {
            cellSize += pChainCellCounts->u.count[i] * 4;
        }
    }

    /* Locate the beginning of the chain cell region */
    pStart = pChainCells = ((u4 *) pChainCellCounts) - cellSize;

    /* The cells are sorted in order - walk through them and reset */
    for (i = 0; i < CHAINING_CELL_LAST; i++) {
        int elemSize = 2; /* Most chaining cell has two words */
        if (i == CHAINING_CELL_INVOKE_PREDICTED) {
            elemSize = 4;
        }

        for (j = 0; j < pChainCellCounts->u.count[i]; j++) {
            int targetOffset;
            switch(i) {
                case CHAINING_CELL_NORMAL:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToInterpNormal);
                    break;
                case CHAINING_CELL_HOT:
                case CHAINING_CELL_INVOKE_SINGLETON:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToTraceSelect);
                    break;
                case CHAINING_CELL_INVOKE_PREDICTED:
                    targetOffset = 0;
                    predChainCell = (PredictedChainingCell *) pChainCells;
                    /* Reset the cell to the init state */
                    predChainCell->branch = PREDICTED_CHAIN_BX_PAIR_INIT;
                    predChainCell->clazz = PREDICTED_CHAIN_CLAZZ_INIT;
                    predChainCell->method = PREDICTED_CHAIN_METHOD_INIT;
                    predChainCell->counter = PREDICTED_CHAIN_COUNTER_INIT;
                    break;
#if defined(WITH_SELF_VERIFICATION)
                case CHAINING_CELL_BACKWARD_BRANCH:
                    targetOffset = offsetof(InterpState,
                          jitToInterpEntries.dvmJitToBackwardBranch);
                    break;
#endif
                default:
                    dvmAbort();
            }
            COMPILER_TRACE_CHAINING(
                LOGD("Jit Runtime: unchaining 0x%x", (int)pChainCells));
            /*
             * Thumb code sequence for a chaining cell is:
             *     ldr  r0, rGLUE, #<word offset>
             *     blx  r0
             */
            if (i != CHAINING_CELL_INVOKE_PREDICTED) {
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
                   gDvmJit.pJitEntryTable[i].codeAddress) {
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

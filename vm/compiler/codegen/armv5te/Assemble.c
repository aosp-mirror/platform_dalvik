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
#include "Armv5teLIR.h"
#include <unistd.h>             /* for cacheflush */

/*
 * opcode: Armv5teOpCode enum
 * skeleton: pre-designated bit-pattern for this opcode
 * ds: dest start bit position
 * de: dest end bit position
 * s1s: src1 start bit position
 * s1e: src1 end bit position
 * s2s: src2 start bit position
 * s2e: src2 end bit position
 * operands: number of operands (for sanity check purposes)
 * name: mnemonic name
 * fmt: for pretty-prining
 */
#define ENCODING_MAP(opcode, skeleton, ds, de, s1s, s1e, s2s, s2e, operands, \
                     name, fmt) \
        {skeleton, {{ds, de}, {s1s, s1e}, {s2s, s2e}}, opcode, operands, name, \
         fmt}

/* Instruction dump string format keys: !pf, where "!" is the start
 * of the key, "p" is which numeric operand to use and "f" is the
 * print format.
 *
 * [p]ositions:
 *     0 -> operands[0] (dest)
 *     1 -> operands[1] (src1)
 *     2 -> operands[2] (src2)
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
 *
 *  [!] escape.  To insert "!", use "!!"
 */
/* NOTE: must be kept in sync with enum Armv5teOpcode from Armv5teLIR.h */
Armv5teEncodingMap EncodingMap[ARMV5TE_LAST] = {
    ENCODING_MAP(ARMV5TE_16BIT_DATA,    0x0000, 15, 0, -1, -1, -1, -1,
                 IS_UNARY_OP,
                 "data", "0x!0h(!0d)"),
    ENCODING_MAP(ARMV5TE_ADC,           0x4140, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "adc", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_ADD_RRI3,      0x1c00, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d, #!2d"),
    ENCODING_MAP(ARMV5TE_ADD_RI8,       0x3000, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!0d, #!1d"),
    ENCODING_MAP(ARMV5TE_ADD_RRR,       0x1800, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d, r!2d"),
    ENCODING_MAP(ARMV5TE_ADD_RR_LH,     0x4440, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add",
                 "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_ADD_RR_HL,     0x4480, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_ADD_RR_HH,     0x44c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_ADD_PC_REL,    0xa000, 10, 8, 7, 0, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "add", "r!0d, pc, #!1E"),
    ENCODING_MAP(ARMV5TE_ADD_SP_REL,    0xa800, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "add", "r!0d, sp, #!1E"),
    ENCODING_MAP(ARMV5TE_ADD_SPI7,      0xb000, 6, 0, -1, -1, -1, -1,
                 IS_UNARY_OP | CLOBBER_DEST,
                 "add", "sp, #!0d*4"),
    ENCODING_MAP(ARMV5TE_AND_RR,        0x4000, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "and", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_ASR,           0x1000, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "asr", "r!0d, r!1d, #!2d"),
    ENCODING_MAP(ARMV5TE_ASRV,          0x4100, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "asr", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_B_COND,        0xd000, 7, 0, 11, 8, -1, -1,
                 IS_BINARY_OP | IS_BRANCH,
                 "!1c", "!0t"),
    ENCODING_MAP(ARMV5TE_B_UNCOND,      0xe000, 10, 0, -1, -1, -1, -1,
                 NO_OPERAND | IS_BRANCH,
                 "b", "!0t"),
    ENCODING_MAP(ARMV5TE_BIC,           0x4380, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "bic", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_BKPT,          0xbe00, 7, 0, -1, -1, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bkpt", "!0d"),
    ENCODING_MAP(ARMV5TE_BLX_1,         0xf000, 10, 0, -1, -1, -1, -1,
                 IS_BINARY_OP | IS_BRANCH,
                 "blx_1", "!0u"),
    ENCODING_MAP(ARMV5TE_BLX_2,         0xe800, 10, 0, -1, -1, -1, -1,
                 IS_BINARY_OP | IS_BRANCH,
                 "blx_2", "!0v"),
    ENCODING_MAP(ARMV5TE_BL_1,          0xf000, 10, 0, -1, -1, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bl_1", "!0u"),
    ENCODING_MAP(ARMV5TE_BL_2,          0xf800, 10, 0, -1, -1, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bl_2", "!0v"),
    ENCODING_MAP(ARMV5TE_BLX_R,         0x4780, 6, 3, -1, -1, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "blx", "r!0d"),
    ENCODING_MAP(ARMV5TE_BX,            0x4700, 6, 3, -1, -1, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "bx", "r!0d"),
    ENCODING_MAP(ARMV5TE_CMN,           0x42c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP,
                 "cmn", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_CMP_RI8,       0x2800, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP,
                 "cmp", "r!0d, #!1d"),
    ENCODING_MAP(ARMV5TE_CMP_RR,        0x4280, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP,
                 "cmp", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_CMP_LH,        0x4540, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP,
                 "cmp", "r!0d, r!1D"),
    ENCODING_MAP(ARMV5TE_CMP_HL,        0x4580, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP,
                 "cmp", "r!0D, r!1d"),
    ENCODING_MAP(ARMV5TE_CMP_HH,        0x45c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP,
                 "cmp", "r!0D, r!1D"),
    ENCODING_MAP(ARMV5TE_EOR,           0x4040, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "eor", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_LDMIA,         0xc800, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST | CLOBBER_SRC1,
                 "ldmia", "r!0d!!, <!1R>"),
    ENCODING_MAP(ARMV5TE_LDR_RRI5,      0x6800, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [r!1d, #!2E]"),
    ENCODING_MAP(ARMV5TE_LDR_RRR,       0x5800, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_LDR_PC_REL,    0x4800, 10, 8, 7, 0, -1, -1,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [pc, #!1E]"),
    ENCODING_MAP(ARMV5TE_LDR_SP_REL,    0x9800, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "ldr", "r!0d, [sp, #!1E]"),
    ENCODING_MAP(ARMV5TE_LDRB_RRI5,     0x7800, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrb", "r!0d, [r!1d, #2d]"),
    ENCODING_MAP(ARMV5TE_LDRB_RRR,      0x5c00, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrb", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_LDRH_RRI5,     0x8800, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrh", "r!0d, [r!1d, #!2F]"),
    ENCODING_MAP(ARMV5TE_LDRH_RRR,      0x5a00, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrh", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_LDRSB_RRR,     0x5600, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrsb", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_LDRSH_RRR,     0x5e00, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "ldrsh", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_LSL,           0x0000, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "lsl", "r!0d, r!1d, #!2d"),
    ENCODING_MAP(ARMV5TE_LSLV,          0x4080, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "lsl", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_LSR,           0x0800, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "lsr", "r!0d, r!1d, #!2d"),
    ENCODING_MAP(ARMV5TE_LSRV,          0x40c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "lsr", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_MOV_IMM,       0x2000, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, #!1d"),
    ENCODING_MAP(ARMV5TE_MOV_RR,        0x1c00, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_MOV_RR_H2H,    0x46c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0D, r!1D"),
    ENCODING_MAP(ARMV5TE_MOV_RR_H2L,    0x4640, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0d, r!1D"),
    ENCODING_MAP(ARMV5TE_MOV_RR_L2H,    0x4680, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mov", "r!0D, r!1d"),
    ENCODING_MAP(ARMV5TE_MUL,           0x4340, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mul", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_MVN,           0x43c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "mvn", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_NEG,           0x4240, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "neg", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_ORR,           0x4300, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "orr", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_POP,           0xbc00, 8, 0, -1, -1, -1, -1,
                 IS_UNARY_OP,
                 "pop", "<!0R>"),
    ENCODING_MAP(ARMV5TE_PUSH,          0xb400, 8, 0, -1, -1, -1, -1,
                 IS_UNARY_OP,
                 "push", "<!0R>"),
    ENCODING_MAP(ARMV5TE_ROR,           0x41c0, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "ror", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_SBC,           0x4180, 2, 0, 5, 3, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "sbc", "r!0d, r!1d"),
    ENCODING_MAP(ARMV5TE_STMIA,         0xc000, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_SRC1,
                 "stmia", "r!0d!!, <!1R>"),
    ENCODING_MAP(ARMV5TE_STR_RRI5,      0x6000, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP,
                 "str", "r!0d, [r!1d, #!2E]"),
    ENCODING_MAP(ARMV5TE_STR_RRR,       0x5000, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP,
                 "str", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_STR_SP_REL,    0x9000, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP,
                 "str", "r!0d, [sp, #!1E]"),
    ENCODING_MAP(ARMV5TE_STRB_RRI5,     0x7000, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP,
                 "strb", "r!0d, [r!1d, #!2d]"),
    ENCODING_MAP(ARMV5TE_STRB_RRR,      0x5400, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP,
                 "strb", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_STRH_RRI5,     0x8000, 2, 0, 5, 3, 10, 6,
                 IS_TERTIARY_OP,
                 "strh", "r!0d, [r!1d, #!2F]"),
    ENCODING_MAP(ARMV5TE_STRH_RRR,      0x5200, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP,
                 "strh", "r!0d, [r!1d, r!2d]"),
    ENCODING_MAP(ARMV5TE_SUB_RRI3,      0x1e00, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "sub", "r!0d, r!1d, #!2d]"),
    ENCODING_MAP(ARMV5TE_SUB_RI8,       0x3800, 10, 8, 7, 0, -1, -1,
                 IS_BINARY_OP | CLOBBER_DEST,
                 "sub", "r!0d, #!1d"),
    ENCODING_MAP(ARMV5TE_SUB_RRR,       0x1a00, 2, 0, 5, 3, 8, 6,
                 IS_TERTIARY_OP | CLOBBER_DEST,
                 "sub", "r!0d, r!1d, r!2d"),
    ENCODING_MAP(ARMV5TE_SUB_SPI7,      0xb080, 6, 0, -1, -1, -1, -1,
                 IS_UNARY_OP | CLOBBER_DEST,
                 "sub", "sp, #!0d"),
    ENCODING_MAP(ARMV5TE_SWI,           0xdf00, 7, 0, -1, -1, -1, -1,
                 IS_UNARY_OP | IS_BRANCH,
                 "swi", "!0d"),
    ENCODING_MAP(ARMV5TE_TST,           0x4200, 2, 0, 5, 3, -1, -1,
                 IS_UNARY_OP,
                 "tst", "r!0d, r!1d"),
};

#define PADDING_MOV_R0_R0               0x1C00

/* Write the numbers in the literal pool to the codegen stream */
static void installDataContent(CompilationUnit *cUnit)
{
    int *dataPtr = (int *) ((char *) cUnit->baseAddr + cUnit->dataOffset);
    Armv5teLIR *dataLIR = (Armv5teLIR *) cUnit->wordList;
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
    Armv5teLIR *lir;

    for (lir = (Armv5teLIR *) cUnit->firstLIRInsn; lir; lir = NEXT_LIR(lir)) {
        if (lir->opCode < 0) {
            if ((lir->opCode == ARMV5TE_PSEUDO_ALIGN4) &&
                /* 1 means padding is needed */
                (lir->operands[0] == 1)) {
                *bufferAddr++ = PADDING_MOV_R0_R0;
            }
            continue;
        }

        if (lir->isNop) {
            continue;
        }

        if (lir->opCode == ARMV5TE_LDR_PC_REL ||
            lir->opCode == ARMV5TE_ADD_PC_REL) {
            Armv5teLIR *lirTarget = (Armv5teLIR *) lir->generic.target;
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
            if (delta > 1023) {
                return true;
            }
            lir->operands[1] = delta >> 2;
        } else if (lir->opCode == ARMV5TE_B_COND) {
            Armv5teLIR *targetLIR = (Armv5teLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if (delta > 254 || delta < -256) {
                return true;
            }
            lir->operands[0] = delta >> 1;
        } else if (lir->opCode == ARMV5TE_B_UNCOND) {
            Armv5teLIR *targetLIR = (Armv5teLIR *) lir->generic.target;
            intptr_t pc = lir->generic.offset + 4;
            intptr_t target = targetLIR->generic.offset;
            int delta = target - pc;
            if (delta > 2046 || delta < -2048) {
                LOGE("Unconditional branch distance out of range: %d\n", delta);
                dvmAbort();
            }
            lir->operands[0] = delta >> 1;
        } else if (lir->opCode == ARMV5TE_BLX_1) {
            assert(NEXT_LIR(lir)->opCode == ARMV5TE_BLX_2);
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

        Armv5teEncodingMap *encoder = &EncodingMap[lir->opCode];
        short bits = encoder->skeleton;
        int i;
        for (i = 0; i < 3; i++) {
            short value;
            if (encoder->fieldLoc[i].end != -1) {
                value = (lir->operands[i] << encoder->fieldLoc[i].start) &
                        ((1 << (encoder->fieldLoc[i].end + 1)) - 1);
                bits |= value;

            }
        }
        *bufferAddr++ = bits;
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
void dvmCompilerAssembleLIR(CompilationUnit *cUnit)
{
    LIR *lir;
    Armv5teLIR *armLIR;
    int offset = 0;
    int i;
    ChainCellCounts chainCellCounts;
    int descSize = jitTraceDescriptionSize(cUnit->traceDesc);

    /* Beginning offset needs to allow space for chain cell offset */
    for (armLIR = (Armv5teLIR *) cUnit->firstLIRInsn;
         armLIR;
         armLIR = NEXT_LIR(armLIR)) {
        armLIR->generic.offset = offset;
        if (armLIR->opCode >= 0 && !armLIR->isNop) {
            offset += 2;
        } else if (armLIR->opCode == ARMV5TE_PSEUDO_ALIGN4) {
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
    Armv5teLIR *chainCellOffsetLIR = (Armv5teLIR *) cUnit->chainCellOffsetLIR;
    assert(chainCellOffsetLIR);
    assert(chainCellOffset < 0x10000);
    assert(chainCellOffsetLIR->opCode == ARMV5TE_16BIT_DATA &&
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

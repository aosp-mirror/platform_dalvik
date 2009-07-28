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
#include "compiler/CompilerInternals.h"

#ifndef _DALVIK_VM_COMPILER_CODEGEN_ARM_ARMLIR_H
#define _DALVIK_VM_COMPILER_CODEGEN_ARM_ARMLIR_H

/*
 * r0, r1, r2, r3, and r7 are always scratch
 * r4PC is scratch if used solely in the compiled land. Otherwise it holds the
 * Dalvik PC.
 * rFP holds the current frame pointer
 * rGLUE holds &InterpState
 */
typedef enum NativeRegisterPool {
    r0 = 0,
    r1 = 1,
    r2 = 2,
    r3 = 3,
    r4PC = 4,
    rFP = 5,
    rGLUE = 6,
    r7 = 7,
    r8 = 8,
    r9 = 9,
    r10 = 10,
    r11 = 11,
    r12 = 12,
    r13 = 13,
    rlr = 14,
    rpc = 15
} NativeRegisterPool;

/* Mask to convert high reg to low for Thumb */
#define THUMB_REG_MASK 0x7

/* Thumb condition encodings */
typedef enum ArmConditionCode {
    ARM_COND_EQ = 0x0,    /* 0000 */
    ARM_COND_NE = 0x1,    /* 0001 */
    ARM_COND_LT = 0xb,    /* 1011 */
    ARM_COND_GE = 0xa,    /* 1010 */
    ARM_COND_GT = 0xc,    /* 1100 */
    ARM_COND_LE = 0xd,    /* 1101 */
    ARM_COND_CS = 0x2,    /* 0010 */
    ARM_COND_MI = 0x4,    /* 0100 */
} ArmConditionCode;

#define isPseudoOpCode(opCode) ((int)(opCode) < 0)

/*
 * The following enum defines the list of supported Thumb instructions by the
 * assembler. Their corresponding snippet positions will be defined in
 * Assemble.c.
 */
typedef enum ArmOpCode {
    ARM_PSEUDO_TARGET_LABEL = -11,
    ARM_PSEUDO_CHAINING_CELL_HOT = -10,
    ARM_PSEUDO_CHAINING_CELL_INVOKE_PREDICTED = -9,
    ARM_PSEUDO_CHAINING_CELL_INVOKE_SINGLETON = -8,
    ARM_PSEUDO_CHAINING_CELL_NORMAL = -7,
    ARM_PSEUDO_DALVIK_BYTECODE_BOUNDARY = -6,
    ARM_PSEUDO_ALIGN4 = -5,
    ARM_PSEUDO_PC_RECONSTRUCTION_CELL = -4,
    ARM_PSEUDO_PC_RECONSTRUCTION_BLOCK_LABEL = -3,
    ARM_PSEUDO_EH_BLOCK_LABEL = -2,
    ARM_PSEUDO_NORMAL_BLOCK_LABEL = -1,
    /************************************************************************/
    ARM_16BIT_DATA,       /* DATA   [0] rd[15..0] */
    THUMB_ADC,            /* adc     [0100000101] rm[5..3] rd[2..0] */
    THUMB_ADD_RRI3,       /* add(1)  [0001110] imm_3[8..6] rn[5..3] rd[2..0]*/
    THUMB_ADD_RI8,        /* add(2)  [00110] rd[10..8] imm_8[7..0] */
    THUMB_ADD_RRR,        /* add(3)  [0001100] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_ADD_RR_LH,      /* add(4)  [01000100] H12[01] rm[5..3] rd[2..0] */
    THUMB_ADD_RR_HL,      /* add(4)  [01001000] H12[10] rm[5..3] rd[2..0] */
    THUMB_ADD_RR_HH,      /* add(4)  [01001100] H12[11] rm[5..3] rd[2..0] */
    THUMB_ADD_PC_REL,     /* add(5)  [10100] rd[10..8] imm_8[7..0] */
    THUMB_ADD_SP_REL,     /* add(6)  [10101] rd[10..8] imm_8[7..0] */
    THUMB_ADD_SPI7,       /* add(7)  [101100000] imm_7[6..0] */
    THUMB_AND_RR,         /* and     [0100000000] rm[5..3] rd[2..0] */
    THUMB_ASR,            /* asr(1)  [00010] imm_5[10..6] rm[5..3] rd[2..0] */
    THUMB_ASRV,           /* asr(2)  [0100000100] rs[5..3] rd[2..0] */
    THUMB_B_COND,         /* b(1)    [1101] cond[11..8] offset_8[7..0] */
    THUMB_B_UNCOND,       /* b(2)    [11100] offset_11[10..0] */
    THUMB_BIC,            /* bic     [0100001110] rm[5..3] rd[2..0] */
    THUMB_BKPT,           /* bkpt    [10111110] imm_8[7..0] */
    THUMB_BLX_1,          /* blx(1)  [111] H[10] offset_11[10..0] */
    THUMB_BLX_2,          /* blx(1)  [111] H[01] offset_11[10..0] */
    THUMB_BL_1,           /* blx(1)  [111] H[10] offset_11[10..0] */
    THUMB_BL_2,           /* blx(1)  [111] H[11] offset_11[10..0] */
    THUMB_BLX_R,          /* blx(2)  [010001111] H2[6..6] rm[5..3] SBZ[000] */
    THUMB_BX,             /* bx      [010001110] H2[6..6] rm[5..3] SBZ[000] */
    THUMB_CMN,            /* cmn     [0100001011] rm[5..3] rd[2..0] */
    THUMB_CMP_RI8,        /* cmp(1)  [00101] rn[10..8] imm_8[7..0] */
    THUMB_CMP_RR,         /* cmp(2)  [0100001010] rm[5..3] rd[2..0] */
    THUMB_CMP_LH,         /* cmp(3)  [01000101] H12[01] rm[5..3] rd[2..0] */
    THUMB_CMP_HL,         /* cmp(3)  [01000110] H12[10] rm[5..3] rd[2..0] */
    THUMB_CMP_HH,         /* cmp(3)  [01000111] H12[11] rm[5..3] rd[2..0] */
    THUMB_EOR,            /* eor     [0100000001] rm[5..3] rd[2..0] */
    THUMB_LDMIA,          /* ldmia   [11001] rn[10..8] reglist [7..0] */
    THUMB_LDR_RRI5,       /* ldr(1)  [01101] imm_5[10..6] rn[5..3] rd[2..0] */
    THUMB_LDR_RRR,        /* ldr(2)  [0101100] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_LDR_PC_REL,     /* ldr(3)  [01001] rd[10..8] imm_8[7..0] */
    THUMB_LDR_SP_REL,     /* ldr(4)  [10011] rd[10..8] imm_8[7..0] */
    THUMB_LDRB_RRI5,      /* ldrb(1) [01111] imm_5[10..6] rn[5..3] rd[2..0] */
    THUMB_LDRB_RRR,       /* ldrb(2) [0101110] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_LDRH_RRI5,      /* ldrh(1) [10001] imm_5[10..6] rn[5..3] rd[2..0] */
    THUMB_LDRH_RRR,       /* ldrh(2) [0101101] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_LDRSB_RRR,      /* ldrsb   [0101011] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_LDRSH_RRR,      /* ldrsh   [0101111] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_LSL,            /* lsl(1)  [00000] imm_5[10..6] rm[5..3] rd[2..0] */
    THUMB_LSLV,           /* lsl(2)  [0100000010] rs[5..3] rd[2..0] */
    THUMB_LSR,            /* lsr(1)  [00001] imm_5[10..6] rm[5..3] rd[2..0] */
    THUMB_LSRV,           /* lsr(2)  [0100000011] rs[5..3] rd[2..0] */
    THUMB_MOV_IMM,        /* mov(1)  [00100] rd[10..8] imm_8[7..0] */
    THUMB_MOV_RR,         /* mov(2)  [0001110000] rn[5..3] rd[2..0] */
    THUMB_MOV_RR_H2H,     /* mov(3)  [01000111] H12[11] rm[5..3] rd[2..0] */
    THUMB_MOV_RR_H2L,     /* mov(3)  [01000110] H12[01] rm[5..3] rd[2..0] */
    THUMB_MOV_RR_L2H,     /* mov(3)  [01000101] H12[10] rm[5..3] rd[2..0] */
    THUMB_MUL,            /* mul     [0100001101] rm[5..3] rd[2..0] */
    THUMB_MVN,            /* mvn     [0100001111] rm[5..3] rd[2..0] */
    THUMB_NEG,            /* neg     [0100001001] rm[5..3] rd[2..0] */
    THUMB_ORR,            /* orr     [0100001100] rm[5..3] rd[2..0] */
    THUMB_POP,            /* pop     [1011110] r[8..8] rl[7..0] */
    THUMB_PUSH,           /* push    [1011010] r[8..8] rl[7..0] */
    THUMB_ROR,            /* ror     [0100000111] rs[5..3] rd[2..0] */
    THUMB_SBC,            /* sbc     [0100000110] rm[5..3] rd[2..0] */
    THUMB_STMIA,          /* stmia   [11000] rn[10..8] reglist [7.. 0] */
    THUMB_STR_RRI5,       /* str(1)  [01100] imm_5[10..6] rn[5..3] rd[2..0] */
    THUMB_STR_RRR,        /* str(2)  [0101000] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_STR_SP_REL,     /* str(3)  [10010] rd[10..8] imm_8[7..0] */
    THUMB_STRB_RRI5,      /* strb(1) [01110] imm_5[10..6] rn[5..3] rd[2..0] */
    THUMB_STRB_RRR,       /* strb(2) [0101010] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_STRH_RRI5,      /* strh(1) [10000] imm_5[10..6] rn[5..3] rd[2..0] */
    THUMB_STRH_RRR,       /* strh(2) [0101001] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_SUB_RRI3,       /* sub(1)  [0001111] imm_3[8..6] rn[5..3] rd[2..0]*/
    THUMB_SUB_RI8,        /* sub(2)  [00111] rd[10..8] imm_8[7..0] */
    THUMB_SUB_RRR,        /* sub(3)  [0001101] rm[8..6] rn[5..3] rd[2..0] */
    THUMB_SUB_SPI7,       /* sub(4)  [101100001] imm_7[6..0] */
    THUMB_SWI,            /* swi     [11011111] imm_8[7..0] */
    THUMB_TST,            /* tst     [0100001000] rm[5..3] rn[2..0] */
    ARM_LAST,
} ArmOpCode;

/* Bit flags describing the behavior of each native opcode */
typedef enum ArmOpFeatureFlags {
    IS_BRANCH =           1 << 1,
    CLOBBER_DEST =        1 << 2,
    CLOBBER_SRC1 =        1 << 3,
    NO_OPERAND =          1 << 4,
    IS_UNARY_OP =         1 << 5,
    IS_BINARY_OP =        1 << 6,
    IS_TERTIARY_OP =      1 << 7,
} ArmOpFeatureFlags;

/* Struct used to define the snippet positions for each Thumb opcode */
typedef struct ArmEncodingMap {
    short skeleton;
    struct {
        int end;
        int start;
    } fieldLoc[3];
    ArmOpCode opCode;
    int flags;
    char *name;
    char* fmt;
    int size;
} ArmEncodingMap;

extern ArmEncodingMap EncodingMap[ARM_LAST];

/*
 * Each instance of this struct holds a pseudo or real LIR instruction:
 * - pesudo ones (eg labels and marks) and will be discarded by the assembler.
 * - real ones will e assembled into Thumb instructions.
 */
typedef struct ArmLIR {
    LIR generic;
    ArmOpCode opCode;
    int operands[3];    // [0..2] = [dest, src1, src2]
    bool isNop;         // LIR is optimized away
    int age;            // default is 0, set lazily by the optimizer
    int size;           // 16-bit unit size (1 for thumb, 1 or 2 for thumb2)
} ArmLIR;

/* Chain cell for predicted method invocation */
typedef struct PredictedChainingCell {
    u4 branch;                  /* Branch to chained destination */
    const ClassObject *clazz;   /* key #1 for prediction */
    const Method *method;       /* key #2 to lookup native PC from dalvik PC */
    u4 counter;                 /* counter to patch the chaining cell */
} PredictedChainingCell;

/* Init values when a predicted chain is initially assembled */
#define PREDICTED_CHAIN_BX_PAIR_INIT     0
#define PREDICTED_CHAIN_CLAZZ_INIT       0
#define PREDICTED_CHAIN_METHOD_INIT      0
#define PREDICTED_CHAIN_COUNTER_INIT     0

/* Used when the callee is not compiled yet */
#define PREDICTED_CHAIN_COUNTER_DELAY    16

/* Rechain after this many mis-predictions have happened */
#define PREDICTED_CHAIN_COUNTER_RECHAIN  1024

/* Used if the resolved callee is a native method */
#define PREDICTED_CHAIN_COUNTER_AVOID    0x7fffffff

/* Utility macros to traverse the LIR/ArmLIR list */
#define NEXT_LIR(lir) ((ArmLIR *) lir->generic.next)
#define PREV_LIR(lir) ((ArmLIR *) lir->generic.prev)

#define NEXT_LIR_LVALUE(lir) (lir)->generic.next
#define PREV_LIR_LVALUE(lir) (lir)->generic.prev

#define CHAIN_CELL_OFFSET_TAG   0xcdab

#endif /* _DALVIK_VM_COMPILER_CODEGEN_ARM_ARMLIR_H */

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
 * r0, r1, r2, r3 are always scratch
 * r4 (rPC) is scratch for Jit, but most be restored when resuming interp
 * r5 (rFP) is reserved [holds Dalvik frame pointer]
 * r6 (rGLUE) is reserved [holds current &interpState]
 * r7 (rINST) is scratch for Jit
 * r8 (rIBASE) is scratch for Jit, but must be restored when resuming interp
 * r9 is always scratch
 * r10 is always scratch
 * r11 (fp) used by gcc unless -fomit-frame-pointer set [available for jit?]
 * r12 is always scratch
 * r13 (sp) is reserved
 * r14 (lr) is scratch for Jit
 * r15 (pc) is reserved
 *
 * For Thumb code use:
 *       r0, r1, r2, r3 to hold operands/results via scoreboard
 *       r4, r7 for temps
 *
 * For Thumb2 code use:
 *       r0, r1, r2, r3, r8, r9, r10, r11 for operands/results via scoreboard
 *       r4, r7, r14 for temps
 *
 * When transitioning from code cache to interp:
 *       restore rIBASE
 *       restore rPC
 *       restore r11 (fp)?
 *
 * Double precision values are stored in consecutive single precision registers
 * such that dr0 -> (sr0,sr1), dr1 -> (sr2,sr3) ... dr16 -> (sr30,sr31)
 */

/* Offset to distingish FP regs */
#define FP_REG_OFFSET 32
/* Offset to distinguish DP FP regs */
#define FP_DOUBLE 64
/* Reg types */
#define FPREG(x) ((x & FP_REG_OFFSET) == FP_REG_OFFSET)
#define LOWREG(x) ((x & 0x7) == x)
#define DOUBLEREG(x) ((x & FP_DOUBLE) == FP_DOUBLE)
#define SINGLEREG(x) (FPREG(x) && !DOUBLEREG(x))
/* Mask to strip off fp flags */
#define FP_REG_MASK (FP_REG_OFFSET-1)
/* Mask to convert high reg to low for Thumb */
#define THUMB_REG_MASK 0x7


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
    rpc = 15,
    fr0  =  0 + FP_REG_OFFSET,
    fr1  =  1 + FP_REG_OFFSET,
    fr2  =  2 + FP_REG_OFFSET,
    fr3  =  3 + FP_REG_OFFSET,
    fr4  =  4 + FP_REG_OFFSET,
    fr5  =  5 + FP_REG_OFFSET,
    fr6  =  6 + FP_REG_OFFSET,
    fr7  =  7 + FP_REG_OFFSET,
    fr8  =  8 + FP_REG_OFFSET,
    fr9  =  9 + FP_REG_OFFSET,
    fr10 = 10 + FP_REG_OFFSET,
    fr11 = 11 + FP_REG_OFFSET,
    fr12 = 12 + FP_REG_OFFSET,
    fr13 = 13 + FP_REG_OFFSET,
    fr14 = 14 + FP_REG_OFFSET,
    fr15 = 15 + FP_REG_OFFSET,
    fr16 = 16 + FP_REG_OFFSET,
    fr17 = 17 + FP_REG_OFFSET,
    fr18 = 18 + FP_REG_OFFSET,
    fr19 = 19 + FP_REG_OFFSET,
    fr20 = 20 + FP_REG_OFFSET,
    fr21 = 21 + FP_REG_OFFSET,
    fr22 = 22 + FP_REG_OFFSET,
    fr23 = 23 + FP_REG_OFFSET,
    fr24 = 24 + FP_REG_OFFSET,
    fr25 = 25 + FP_REG_OFFSET,
    fr26 = 26 + FP_REG_OFFSET,
    fr27 = 27 + FP_REG_OFFSET,
    fr28 = 28 + FP_REG_OFFSET,
    fr29 = 29 + FP_REG_OFFSET,
    fr30 = 30 + FP_REG_OFFSET,
    fr31 = 31 + FP_REG_OFFSET,
    dr0 = fr0 + FP_DOUBLE,
    dr1 = fr2 + FP_DOUBLE,
    dr2 = fr4 + FP_DOUBLE,
    dr3 = fr6 + FP_DOUBLE,
    dr4 = fr8 + FP_DOUBLE,
    dr5 = fr10 + FP_DOUBLE,
    dr6 = fr12 + FP_DOUBLE,
    dr7 = fr14 + FP_DOUBLE,
    dr8 = fr16 + FP_DOUBLE,
    dr9 = fr18 + FP_DOUBLE,
    dr10 = fr20 + FP_DOUBLE,
    dr11 = fr22 + FP_DOUBLE,
    dr12 = fr24 + FP_DOUBLE,
    dr13 = fr26 + FP_DOUBLE,
    dr14 = fr28 + FP_DOUBLE,
    dr15 = fr30 + FP_DOUBLE,
} NativeRegisterPool;

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
    ARM_PSEUDO_TARGET_LABEL = -12,
    ARM_PSEUDO_CHAINING_CELL_BACKWARD_BRANCH = -11,
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
    THUMB2_VLDRS,         /* vldr low  sx [111011011001] rn[19..16] rd[15-12]
                                       [1010] imm_8[7..0] */
    THUMB2_VLDRD,         /* vldr low  dx [111011011001] rn[19..16] rd[15-12]
                                       [1011] imm_8[7..0] */
    THUMB2_VMULS,         /* vmul vd, vn, vm [111011100010] rn[19..16]
                                       rd[15-12] [10100000] rm[3..0] */
    THUMB2_VMULD,         /* vmul vd, vn, vm [111011100010] rn[19..16]
                                       rd[15-12] [10110000] rm[3..0] */
    THUMB2_VSTRS,         /* vstr low  sx [111011011000] rn[19..16] rd[15-12]
                                       [1010] imm_8[7..0] */
    THUMB2_VSTRD,         /* vstr low  dx [111011011000] rn[19..16] rd[15-12]
                                       [1011] imm_8[7..0] */
    THUMB2_VSUBS,         /* vsub vd, vn, vm [111011100011] rn[19..16]
                                       rd[15-12] [10100040] rm[3..0] */
    THUMB2_VSUBD,         /* vsub vd, vn, vm [111011100011] rn[19..16]
                                       rd[15-12] [10110040] rm[3..0] */
    THUMB2_VADDS,         /* vadd vd, vn, vm [111011100011] rn[19..16]
                                       rd[15-12] [10100000] rm[3..0] */
    THUMB2_VADDD,         /* vadd vd, vn, vm [111011100011] rn[19..16]
                                       rd[15-12] [10110000] rm[3..0] */
    THUMB2_VDIVS,         /* vdiv vd, vn, vm [111011101000] rn[19..16]
                                       rd[15-12] [10100000] rm[3..0] */
    THUMB2_VDIVD,         /* vdiv vd, vn, vm [111011101000] rn[19..16]
                                       rd[15-12] [10110000] rm[3..0] */
    THUMB2_VCVTIF,        /* vcvt.F32 vd, vm [1110111010111000] vd[15..12]
                                       [10101100] vm[3..0] */
    THUMB2_VCVTID,        /* vcvt.F64 vd, vm [1110111010111000] vd[15..12]
                                       [10111100] vm[3..0] */
    THUMB2_VCVTFI,        /* vcvt.S32.F32 vd, vm [1110111010111101] vd[15..12]
                                       [10101100] vm[3..0] */
    THUMB2_VCVTDI,        /* vcvt.S32.F32 vd, vm [1110111010111101] vd[15..12]
                                       [10111100] vm[3..0] */
    THUMB2_VCVTFD,        /* vcvt.F64.F32 vd, vm [1110111010110111] vd[15..12]
                                       [10101100] vm[3..0] */
    THUMB2_VCVTDF,        /* vcvt.F32.F64 vd, vm [1110111010110111] vd[15..12]
                                       [10111100] vm[3..0] */
    THUMB2_VSQRTS,        /* vsqrt.f32 vd, vm [1110111010110001] vd[15..12]
                                       [10101100] vm[3..0] */
    THUMB2_VSQRTD,        /* vsqrt.f64 vd, vm [1110111010110001] vd[15..12]
                                       [10111100] vm[3..0] */
    THUMB2_MOV_IMM_SHIFT, /* mov(T2) rd, #<const> [11110] i [00001001111]
                                       imm3 rd[11..8] imm8 */
    THUMB2_MOV_IMM16,     /* mov(T3) rd, #<const> [11110] i [0010100] imm4 [0]
                                       imm3 rd[11..8] imm8 */
    THUMB2_STR_RRI12,     /* str(Imm,T3) rd,[rn,#imm12] [111110001100]
                                       rn[19..16] rt[15..12] imm12[11..0] */
    THUMB2_LDR_RRI12,     /* str(Imm,T3) rd,[rn,#imm12] [111110001100]
                                       rn[19..16] rt[15..12] imm12[11..0] */
    THUMB2_STR_RRI8_PREDEC, /* str(Imm,T4) rd,[rn,#-imm8] [111110000100]
                                       rn[19..16] rt[15..12] [1100] imm[7..0]*/
    THUMB2_LDR_RRI8_PREDEC, /* ldr(Imm,T4) rd,[rn,#-imm8] [111110000101]
                                       rn[19..16] rt[15..12] [1100] imm[7..0]*/
    THUMB2_CBNZ,            /* cbnz rd,<label> [101110] i [1] imm5[7..3]
                                       rn[2..0] */
    THUMB2_CBZ,             /* cbn rd,<label> [101100] i [1] imm5[7..3]
                                       rn[2..0] */
    THUMB2_ADD_RRI12,       /* add rd, rn, #imm12 [11110] i [100000] rn[19..16]
                                       [0] imm3[14..12] rd[11..8] imm8[7..0] */
    THUMB2_MOV_RR,          /* mov rd, rm [11101010010011110000] rd[11..8]
                                       [0000] rm[3..0] */
    THUMB2_VMOVS,           /* vmov.f32 vd, vm [111011101] D [110000]
                                       vd[15..12] 101001] M [0] vm[3..0] */
    THUMB2_VMOVD,           /* vmov.f64 vd, vm [111011101] D [110000]
                                       vd[15..12] 101101] M [0] vm[3..0] */
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

/* Instruction assembly fieldLoc kind */
typedef enum ArmEncodingKind {
    UNUSED,
    BITBLT,        /* Bit string using end/start */
    DFP,           /* Double FP reg */
    SFP,           /* Single FP reg */
    MODIMM,        /* Shifted 8-bit immediate using [26,14..12,7..0] */
    IMM16,         /* Zero-extended immediate using [26,19..16,14..12,7..0] */
    IMM6,          /* Encoded branch target using [9,7..3]0 */
    IMM12,         /* Zero-extended immediate using [26,14..12,7..0] */
} ArmEncodingKind;

/* Struct used to define the snippet positions for each Thumb opcode */
typedef struct ArmEncodingMap {
    u4 skeleton;
    struct {
        ArmEncodingKind kind;
        int end;   /* end for BITBLT, 1-bit slice end for FP regs */
        int start; /* start for BITBLT, 4-bit slice end for FP regs */
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

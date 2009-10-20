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
 * r9 is reserved
 * r10 is always scratch
 * r11 (fp) used by gcc unless -fomit-frame-pointer set [available for jit?]
 * r12 is always scratch
 * r13 (sp) is reserved
 * r14 (lr) is scratch for Jit
 * r15 (pc) is reserved
 *
 * Preserved across C calls: r4, r5, r6, r7, r8, r10, r11
 * Trashed across C calls: r0, r1, r2, r3, r12, r14
 *
 * Floating pointer registers
 * s0-s31
 * d0-d15, where d0={s0,s1}, d1={s2,s3}, ... , d15={s30,s31}
 *
 * s16-s31 (d8-d15) preserved across C calls
 * s0-s15 (d0-d7) trashed across C calls
 *
 * For Thumb code use:
 *       r0, r1, r2, r3 to hold operands/results
 *       r4, r7 for temps
 *
 * For Thumb2 code use:
 *       r0, r1, r2, r3, r8, r9, r10, r11, r12, r14 for operands/results
 *       r4, r7 for temps
 *       s16-s31/d8-d15 for operands/results
 *       s0-s15/d0-d7 for temps
 *
 * When transitioning from code cache to interp:
 *       restore rIBASE
 *       restore rPC
 *       restore r11?
 */

/* Offset to distingish FP regs */
#define FP_REG_OFFSET 32
/* Offset to distinguish DP FP regs */
#define FP_DOUBLE 64
/* Reg types */
#define REGTYPE(x) (x & (FP_REG_OFFSET | FP_DOUBLE))
#define FPREG(x) ((x & FP_REG_OFFSET) == FP_REG_OFFSET)
#define LOWREG(x) ((x & 0x7) == x)
#define DOUBLEREG(x) ((x & FP_DOUBLE) == FP_DOUBLE)
#define SINGLEREG(x) (FPREG(x) && !DOUBLEREG(x))
/* Mask to strip off fp flags */
#define FP_REG_MASK (FP_REG_OFFSET-1)
/* non-existent Dalvik register */
#define vNone   (-1)
/* non-existant physical register */
#define rNone   (-1)

typedef enum ResourceEncodingPos {
    kGPReg0     = 0,
    kRegSP      = 13,
    kRegLR      = 14,
    kRegPC      = 15,
    kFPReg0     = 16,
    kRegEnd     = 48,
    kCCode      = kRegEnd,
    kFPStatus,
    kDalvikReg,
} ResourceEncodingPos;

#define ENCODE_REG_LIST(N)      ((u8) N)
#define ENCODE_REG_SP           (1ULL << kRegSP)
#define ENCODE_REG_LR           (1ULL << kRegLR)
#define ENCODE_REG_PC           (1ULL << kRegPC)
#define ENCODE_CCODE            (1ULL << kCCode)
#define ENCODE_FP_STATUS        (1ULL << kFPStatus)
#define ENCODE_DALVIK_REG       (1ULL << kDalvikReg)
#define ENCODE_ALL              (~0ULL)

#define DECODE_ALIAS_INFO_REG(X)        (X & 0xffff)
#define DECODE_ALIAS_INFO_WIDE(X)       ((X & 0x80000000) ? 1 : 0)

typedef enum OpSize {
    WORD,
    LONG,
    SINGLE,
    DOUBLE,
    UNSIGNED_HALF,
    SIGNED_HALF,
    UNSIGNED_BYTE,
    SIGNED_BYTE,
} OpSize;

typedef enum OpKind {
    OP_MOV,
    OP_MVN,
    OP_CMP,
    OP_LSL,
    OP_LSR,
    OP_ASR,
    OP_ROR,
    OP_NOT,
    OP_AND,
    OP_OR,
    OP_XOR,
    OP_NEG,
    OP_ADD,
    OP_ADC,
    OP_SUB,
    OP_SBC,
    OP_RSUB,
    OP_MUL,
    OP_DIV,
    OP_REM,
    OP_BIC,
    OP_CMN,
    OP_TST,
    OP_BKPT,
    OP_BLX,
    OP_PUSH,
    OP_POP,
    OP_2CHAR,
    OP_2SHORT,
    OP_2BYTE,
    OP_COND_BR,
    OP_UNCOND_BR,
} OpKind;

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
    ARM_COND_CS = 0x2,    /* 0010 */
    ARM_COND_CC = 0x3,    /* 0011 */
    ARM_COND_MI = 0x4,    /* 0100 */
    ARM_COND_PL = 0x5,    /* 0101 */
    ARM_COND_VS = 0x6,    /* 0110 */
    ARM_COND_VC = 0x7,    /* 0111 */
    ARM_COND_HI = 0x8,    /* 1000 */
    ARM_COND_LS = 0x9,    /* 1001 */
    ARM_COND_GE = 0xa,    /* 1010 */
    ARM_COND_LT = 0xb,    /* 1011 */
    ARM_COND_GT = 0xc,    /* 1100 */
    ARM_COND_LE = 0xd,    /* 1101 */
    ARM_COND_AL = 0xe,    /* 1110 */
    ARM_COND_NV = 0xf,    /* 1111 */
} ArmConditionCode;

#define isPseudoOpCode(opCode) ((int)(opCode) < 0)

/*
 * The following enum defines the list of supported Thumb instructions by the
 * assembler. Their corresponding snippet positions will be defined in
 * Assemble.c.
 */
typedef enum ArmOpCode {
    ARM_PSEUDO_BARRIER = -17,
    ARM_PSEUDO_EXTENDED_MIR = -16,
    ARM_PSEUDO_SSA_REP = -15,
    ARM_PSEUDO_ENTRY_BLOCK = -14,
    ARM_PSEUDO_EXIT_BLOCK = -13,
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
    THUMB_ADC_RR,         /* adc     [0100000101] rm[5..3] rd[2..0] */
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
    THUMB_ASR_RRI5,       /* asr(1)  [00010] imm_5[10..6] rm[5..3] rd[2..0] */
    THUMB_ASR_RR,         /* asr(2)  [0100000100] rs[5..3] rd[2..0] */
    THUMB_B_COND,         /* b(1)    [1101] cond[11..8] offset_8[7..0] */
    THUMB_B_UNCOND,       /* b(2)    [11100] offset_11[10..0] */
    THUMB_BIC_RR,         /* bic     [0100001110] rm[5..3] rd[2..0] */
    THUMB_BKPT,           /* bkpt    [10111110] imm_8[7..0] */
    THUMB_BLX_1,          /* blx(1)  [111] H[10] offset_11[10..0] */
    THUMB_BLX_2,          /* blx(1)  [111] H[01] offset_11[10..0] */
    THUMB_BL_1,           /* blx(1)  [111] H[10] offset_11[10..0] */
    THUMB_BL_2,           /* blx(1)  [111] H[11] offset_11[10..0] */
    THUMB_BLX_R,          /* blx(2)  [010001111] rm[6..3] [000] */
    THUMB_BX,             /* bx      [010001110] H2[6..6] rm[5..3] SBZ[000] */
    THUMB_CMN_RR,         /* cmn     [0100001011] rm[5..3] rd[2..0] */
    THUMB_CMP_RI8,        /* cmp(1)  [00101] rn[10..8] imm_8[7..0] */
    THUMB_CMP_RR,         /* cmp(2)  [0100001010] rm[5..3] rd[2..0] */
    THUMB_CMP_LH,         /* cmp(3)  [01000101] H12[01] rm[5..3] rd[2..0] */
    THUMB_CMP_HL,         /* cmp(3)  [01000110] H12[10] rm[5..3] rd[2..0] */
    THUMB_CMP_HH,         /* cmp(3)  [01000111] H12[11] rm[5..3] rd[2..0] */
    THUMB_EOR_RR,         /* eor     [0100000001] rm[5..3] rd[2..0] */
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
    THUMB_LSL_RRI5,       /* lsl(1)  [00000] imm_5[10..6] rm[5..3] rd[2..0] */
    THUMB_LSL_RR,         /* lsl(2)  [0100000010] rs[5..3] rd[2..0] */
    THUMB_LSR_RRI5,       /* lsr(1)  [00001] imm_5[10..6] rm[5..3] rd[2..0] */
    THUMB_LSR_RR,         /* lsr(2)  [0100000011] rs[5..3] rd[2..0] */
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
    THUMB_ROR_RR,         /* ror     [0100000111] rs[5..3] rd[2..0] */
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
    THUMB2_CBNZ,          /* cbnz rd,<label> [101110] i [1] imm5[7..3]
                                       rn[2..0] */
    THUMB2_CBZ,           /* cbn rd,<label> [101100] i [1] imm5[7..3]
                                       rn[2..0] */
    THUMB2_ADD_RRI12,     /* add rd, rn, #imm12 [11110] i [100000] rn[19..16]
                                       [0] imm3[14..12] rd[11..8] imm8[7..0] */
    THUMB2_MOV_RR,        /* mov rd, rm [11101010010011110000] rd[11..8]
                                       [0000] rm[3..0] */
    THUMB2_VMOVS,         /* vmov.f32 vd, vm [111011101] D [110000]
                                       vd[15..12] 101001] M [0] vm[3..0] */
    THUMB2_VMOVD,         /* vmov.f64 vd, vm [111011101] D [110000]
                                       vd[15..12] 101101] M [0] vm[3..0] */
    THUMB2_LDMIA,         /* ldmia  [111010001001[ rn[19..16] mask[15..0] */
    THUMB2_STMIA,         /* stmia  [111010001000[ rn[19..16] mask[15..0] */
    THUMB2_ADD_RRR,       /* add [111010110000] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_SUB_RRR,       /* sub [111010111010] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_SBC_RRR,       /* sbc [111010110110] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_CMP_RR,        /* cmp [111010111011] rn[19..16] [0000] [1111]
                                   [0000] rm[3..0] */
    THUMB2_SUB_RRI12,     /* sub rd, rn, #imm12 [11110] i [01010] rn[19..16]
                                       [0] imm3[14..12] rd[11..8] imm8[7..0] */
    THUMB2_MVN_IMM_SHIFT, /* mov(T2) rd, #<const> [11110] i [00011011110]
                                       imm3 rd[11..8] imm8 */
    THUMB2_SEL,           /* sel rd, rn, rm [111110101010] rn[19-16] rd[11-8]
                                       rm[3-0] */
    THUMB2_UBFX,          /* ubfx rd,rn,#lsb,#width [111100111100] rn[19..16]
                                       [0] imm3[14-12] rd[11-8] w[4-0] */
    THUMB2_SBFX,          /* ubfx rd,rn,#lsb,#width [111100110100] rn[19..16]
                                       [0] imm3[14-12] rd[11-8] w[4-0] */
    THUMB2_LDR_RRR,       /* ldr rt,[rn,rm,LSL #imm] [111110000101] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_LDRH_RRR,      /* ldrh rt,[rn,rm,LSL #imm] [111110000101] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_LDRSH_RRR,     /* ldrsh rt,[rn,rm,LSL #imm] [111110000101] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_LDRB_RRR,      /* ldrb rt,[rn,rm,LSL #imm] [111110000101] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_LDRSB_RRR,     /* ldrsb rt,[rn,rm,LSL #imm] [111110000101] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_STR_RRR,       /* str rt,[rn,rm,LSL #imm] [111110000100] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_STRH_RRR,      /* str rt,[rn,rm,LSL #imm] [111110000010] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_STRB_RRR,      /* str rt,[rn,rm,LSL #imm] [111110000000] rn[19-16]
                                       rt[15-12] [000000] imm[5-4] rm[3-0] */
    THUMB2_LDRH_RRI12,    /* ldrh rt,[rn,#imm12] [111110001011]
                                       rt[15..12] rn[19..16] imm12[11..0] */
    THUMB2_LDRSH_RRI12,   /* ldrsh rt,[rn,#imm12] [111110011011]
                                       rt[15..12] rn[19..16] imm12[11..0] */
    THUMB2_LDRB_RRI12,    /* ldrb rt,[rn,#imm12] [111110001001]
                                       rt[15..12] rn[19..16] imm12[11..0] */
    THUMB2_LDRSB_RRI12,   /* ldrsb rt,[rn,#imm12] [111110011001]
                                       rt[15..12] rn[19..16] imm12[11..0] */
    THUMB2_STRH_RRI12,    /* strh rt,[rn,#imm12] [111110001010]
                                       rt[15..12] rn[19..16] imm12[11..0] */
    THUMB2_STRB_RRI12,    /* strb rt,[rn,#imm12] [111110001000]
                                       rt[15..12] rn[19..16] imm12[11..0] */
    THUMB2_POP,           /* pop     [1110100010111101] list[15-0]*/
    THUMB2_PUSH,          /* push    [1110100010101101] list[15-0]*/
    THUMB2_CMP_RI8,       /* cmp rn, #<const> [11110] i [011011] rn[19-16] [0]
                                       imm3 [1111] imm8[7..0] */
    THUMB2_ADC_RRR,       /* adc [111010110101] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_AND_RRR,       /* and [111010100000] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_BIC_RRR,       /* bic [111010100010] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_CMN_RR,        /* cmn [111010110001] rn[19..16] [0000] [1111]
                                   [0000] rm[3..0] */
    THUMB2_EOR_RRR,       /* eor [111010101000] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_MUL_RRR,       /* mul [111110110000] rn[19..16] [1111] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_MVN_RR,        /* mvn [11101010011011110] rd[11-8] [0000]
                                   rm[3..0] */
    THUMB2_RSUB_RRI8,     /* rsub [111100011100] rn[19..16] [0000] rd[11..8]
                                   imm8[7..0] */
    THUMB2_NEG_RR,        /* actually rsub rd, rn, #0 */
    THUMB2_ORR_RRR,       /* orr [111010100100] rn[19..16] [0000] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_TST_RR,        /* tst [111010100001] rn[19..16] [0000] [1111]
                                   [0000] rm[3..0] */
    THUMB2_LSL_RRR,       /* lsl [111110100000] rn[19..16] [1111] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_LSR_RRR,       /* lsr [111110100010] rn[19..16] [1111] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_ASR_RRR,       /* asr [111110100100] rn[19..16] [1111] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_ROR_RRR,       /* ror [111110100110] rn[19..16] [1111] rd[11..8]
                                   [0000] rm[3..0] */
    THUMB2_LSL_RRI5,      /* lsl [11101010010011110] imm[14.12] rd[11..8]
                                   [00] rm[3..0] */
    THUMB2_LSR_RRI5,      /* lsr [11101010010011110] imm[14.12] rd[11..8]
                                   [01] rm[3..0] */
    THUMB2_ASR_RRI5,      /* asr [11101010010011110] imm[14.12] rd[11..8]
                                   [10] rm[3..0] */
    THUMB2_ROR_RRI5,      /* ror [11101010010011110] imm[14.12] rd[11..8]
                                   [11] rm[3..0] */
    THUMB2_BIC_RRI8,      /* bic [111100000010] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_AND_RRI8,      /* bic [111100000000] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_ORR_RRI8,      /* orr [111100000100] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_EOR_RRI8,      /* eor [111100001000] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_ADD_RRI8,      /* add [111100001000] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_ADC_RRI8,      /* adc [111100010101] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_SUB_RRI8,      /* sub [111100011011] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_SBC_RRI8,      /* sbc [111100010111] rn[19..16] [0] imm3
                                   rd[11..8] imm8 */
    THUMB2_IT,            /* it [10111111] firstcond[7-4] mask[3-0] */
    THUMB2_FMSTAT,        /* fmstat [11101110111100011111101000010000] */
    THUMB2_VCMPD,         /* vcmp [111011101] D [11011] rd[15-12] [1011]
                                   E [1] M [0] rm[3-0] */
    THUMB2_VCMPS,         /* vcmp [111011101] D [11010] rd[15-12] [1011]
                                   E [1] M [0] rm[3-0] */
    THUMB2_LDR_PC_REL12,  /* ldr rd,[pc,#imm12] [1111100011011111] rt[15-12]
                                     imm12[11-0] */
    THUMB2_B_COND,        /* b<c> [1110] S cond[25-22] imm6[21-16] [10]
                                  J1 [0] J2 imm11[10..0] */
    THUMB2_VMOVD_RR,      /* vmov [111011101] D [110000] vd[15-12 [101101]
                                  M [0] vm[3-0] */
    THUMB2_VMOVS_RR,      /* vmov [111011101] D [110000] vd[15-12 [101001]
                                  M [0] vm[3-0] */
    THUMB2_FMRS,          /* vmov [111011100000] vn[19-16] rt[15-12] [1010]
                                  N [0010000] */
    THUMB2_FMSR,          /* vmov [111011100001] vn[19-16] rt[15-12] [1010]
                                  N [0010000] */
    THUMB2_FMRRD,         /* vmov [111011000100] rt2[19-16] rt[15-12]
                                  [101100] M [1] vm[3-0] */
    THUMB2_FMDRR,         /* vmov [111011000101] rt2[19-16] rt[15-12]
                                  [101100] M [1] vm[3-0] */

    ARM_LAST,
} ArmOpCode;

/* Bit flags describing the behavior of each native opcode */
typedef enum ArmOpFeatureFlags {
    kIsBranch = 0,
    kRegDef0,
    kRegDef1,
    kRegDefSP,
    kRegDefLR,
    kRegDefList0,
    kRegDefList1,
    kRegUse0,
    kRegUse1,
    kRegUse2,
    kRegUseSP,
    kRegUsePC,
    kRegUseList0,
    kRegUseList1,
    kNoOperand,
    kIsUnaryOp,
    kIsBinaryOp,
    kIsTertiaryOp,
    kIsQuadOp,
    kIsIT,
    kSetsCCodes,
    kUsesCCodes,
} ArmOpFeatureFlags;

#define IS_BRANCH       (1 << kIsBranch)
#define REG_DEF0        (1 << kRegDef0)
#define REG_DEF1        (1 << kRegDef1)
#define REG_DEF_SP      (1 << kRegDefSP)
#define REG_DEF_LR      (1 << kRegDefLR)
#define REG_DEF_LIST0   (1 << kRegDefList0)
#define REG_DEF_LIST1   (1 << kRegDefList1)
#define REG_USE0        (1 << kRegUse0)
#define REG_USE1        (1 << kRegUse1)
#define REG_USE2        (1 << kRegUse2)
#define REG_USE_SP      (1 << kRegUseSP)
#define REG_USE_PC      (1 << kRegUsePC)
#define REG_USE_LIST0   (1 << kRegUseList0)
#define REG_USE_LIST1   (1 << kRegUseList1)
#define NO_OPERAND      (1 << kNoOperand)
#define IS_UNARY_OP     (1 << kIsUnaryOp)
#define IS_BINARY_OP    (1 << kIsBinaryOp)
#define IS_TERTIARY_OP  (1 << kIsTertiaryOp)
#define IS_QUAD_OP      (1 << kIsQuadOp)
#define IS_IT           (1 << kIsIT)
#define SETS_CCODES     (1 << kSetsCCodes)
#define USES_CCODES     (1 << kUsesCCodes)

/* Common combo register usage patterns */
#define REG_USE01       (REG_USE0 | REG_USE1)
#define REG_USE012      (REG_USE01 | REG_USE2)
#define REG_USE12       (REG_USE1 | REG_USE2)
#define REG_DEF0_USE0   (REG_DEF0 | REG_USE0)
#define REG_DEF0_USE1   (REG_DEF0 | REG_USE1)
#define REG_DEF0_USE01  (REG_DEF0 | REG_USE01)
#define REG_DEF0_USE12  (REG_DEF0 | REG_USE12)
#define REG_DEF01_USE2  (REG_DEF0 | REG_DEF1 | REG_USE2)

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
    SHIFT,         /* Shift descriptor, [14..12,7..4] */
    LSB,           /* least significant bit using [14..12][7..6] */
    BWIDTH,        /* bit-field width, encoded as width-1 */
    SHIFT5,        /* Shift count, [14..12,7..6] */
    BROFFSET,      /* Signed extended [26,11,13,21-16,10-0]:0 */
} ArmEncodingKind;

/* Struct used to define the snippet positions for each Thumb opcode */
typedef struct ArmEncodingMap {
    u4 skeleton;
    struct {
        ArmEncodingKind kind;
        int end;   /* end for BITBLT, 1-bit slice end for FP regs */
        int start; /* start for BITBLT, 4-bit slice end for FP regs */
    } fieldLoc[4];
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
 * - real ones will be assembled into Thumb instructions.
 *
 * Machine resources are encoded into a 64-bit vector, where the encodings are
 * as following:
 * - [ 0..15]: general purpose registers including PC, SP, and LR
 * - [16..47]: floating-point registers where d0 is expanded to s[01] and s0
 *   starts at bit 16
 * - [48]: IT block
 * - [49]: integer condition code
 * - [50]: floatint-point status word
 */
typedef struct ArmLIR {
    LIR generic;
    ArmOpCode opCode;
    int operands[4];    // [0..3] = [dest, src1, src2, extra]
    bool isNop;         // LIR is optimized away
    int age;            // default is 0, set lazily by the optimizer
    int size;           // 16-bit unit size (1 for thumb, 1 or 2 for thumb2)
    int aliasInfo;      // For Dalvik register access disambiguation
    u8 useMask;         // Resource mask for use
    u8 defMask;         // Resource mask for def
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
#define PREDICTED_CHAIN_COUNTER_DELAY    512

/* Rechain after this many mis-predictions have happened */
#define PREDICTED_CHAIN_COUNTER_RECHAIN  8192

/* Used if the resolved callee is a native method */
#define PREDICTED_CHAIN_COUNTER_AVOID    0x7fffffff

/* Utility macros to traverse the LIR/ArmLIR list */
#define NEXT_LIR(lir) ((ArmLIR *) lir->generic.next)
#define PREV_LIR(lir) ((ArmLIR *) lir->generic.prev)

#define NEXT_LIR_LVALUE(lir) (lir)->generic.next
#define PREV_LIR_LVALUE(lir) (lir)->generic.prev

#define CHAIN_CELL_OFFSET_TAG   0xcdab

ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc);

#endif /* _DALVIK_VM_COMPILER_CODEGEN_ARM_ARMLIR_H */

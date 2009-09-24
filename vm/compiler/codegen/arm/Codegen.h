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

#ifndef _DALVIK_VM_COMPILER_CODEGEN_ARM_CODEGEN_H
#define _DALVIK_VM_COMPILER_CODEGEN_ARM_CODEGEN_H

/*
 * Forward declarations for common routines in Codegen.c used by ISA
 * variant code such as ThumbUtilty.c
 */

static void annotateDalvikRegAccess(ArmLIR *lir, int regId, bool isLoad);
static void setupResourceMasks(ArmLIR *lir);
static ArmLIR *newLIR0(CompilationUnit *cUnit, ArmOpCode opCode);
static ArmLIR *newLIR1(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest);
static ArmLIR *newLIR2(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest, int src1);
static ArmLIR *newLIR3(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest, int src1, int src2);
static ArmLIR *newLIR4(CompilationUnit *cUnit, ArmOpCode opCode,
                            int dest, int src1, int src2, int info);
static ArmLIR *scanLiteralPool(CompilationUnit *cUnit, int value,
                                   unsigned int delta);
static ArmLIR *addWordData(CompilationUnit *cUnit, int value, bool inPlace);
static inline ArmLIR *genCheckCommon(CompilationUnit *cUnit, int dOffset,
                                         ArmLIR *branch,
                                         ArmLIR *pcrLabel);
static void genBarrier(CompilationUnit *cUnit);
static RegLocation loadValue(CompilationUnit *cUnit, RegLocation rlSrc,
                             RegisterClass opKind);
static RegLocation loadValueWide(CompilationUnit *cUnit, RegLocation rlSrc,
                             RegisterClass opKind);
static ArmLIR *loadConstant(CompilationUnit *cUnit, int rDest, int value);
static void storeValue(CompilationUnit *cUnit, RegLocation rlDst,
                       RegLocation rlSrc);
static void storeValueWide(CompilationUnit *cUnit, RegLocation rlDst,
                           RegLocation rlSrc);
static void loadValueDirectFixed(CompilationUnit *cUnit, RegLocation rlSrc,
                                 int reg1);
static void loadValueDirectWide(CompilationUnit *cUnit, RegLocation rlSrc,
                                int regLo, int regHi);
static void loadValueDirectWideFixed(CompilationUnit *cUnit, RegLocation rlSrc,
                                     int regLo, int regHi);
static ArmLIR *genNullCheck(CompilationUnit *cUnit, int sReg, int mReg,
                            int dOffset, ArmLIR *pcrLabel);
static ArmLIR *loadWordDisp(CompilationUnit *cUnit, int rBase,
                            int displacement, int rDest);
static ArmLIR *storeWordDisp(CompilationUnit *cUnit, int rBase,
                            int displacement, int rDest);
static RegLocation inlinedTarget(CompilationUnit *cUnit, MIR *mir, bool fpHint);
static RegLocation inlinedTargetWide(CompilationUnit *cUnit, MIR *mir,
                                      bool fpHint);
static ArmLIR *genBoundsCheck(CompilationUnit *cUnit, int rIndex,
                              int rBound, int dOffset, ArmLIR *pcrLabel);
static void handleMonitorPortable(CompilationUnit *cUnit, MIR *mir);
static inline ArmLIR *genRegRegCheck(CompilationUnit *cUnit,
                                     ArmConditionCode cond,
                                     int reg1, int reg2, int dOffset,
                                     ArmLIR *pcrLabel);

/* Routines which must be supplied by the variant-specific code */
static void genDispatchToHandler(CompilationUnit *cUnit, TemplateOpCode opCode);
static bool genInlineSqrt(CompilationUnit *cUnit, MIR *mir);
static bool genInlineCos(CompilationUnit *cUnit, MIR *mir);
static bool genInlineSin(CompilationUnit *cUnit, MIR *mir);
static bool handleConversion(CompilationUnit *cUnit, MIR *mir);
static bool compilerArchVariantInit();
static bool handleArithOpFloat(CompilationUnit *cUnit, MIR *mir,
                               RegLocation rlDest, RegLocation rlSrc1,
                               RegLocation rlSrc2);
static bool handleArithOpDouble(CompilationUnit *cUnit, MIR *mir,
                                RegLocation rlDest, RegLocation rlSrc1,
                                RegLocation rlSrc2);
static bool handleCmpFP(CompilationUnit *cUnit, MIR *mir, RegLocation rlDest,
                        RegLocation rlSrc1, RegLocation rlSrc2);

#endif /* _DALVIK_VM_COMPILER_CODEGEN_ARM_CODEGEN_H */

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

/* Routines which must be supplied by the variant-specific code */
static void genDispatchToHandler(CompilationUnit *cUnit, TemplateOpCode opCode);
bool dvmCompilerArchInit(void);
static bool genInlineSqrt(CompilationUnit *cUnit, MIR *mir);
static bool genInlineCos(CompilationUnit *cUnit, MIR *mir);
static bool genInlineSin(CompilationUnit *cUnit, MIR *mir);
static bool genConversion(CompilationUnit *cUnit, MIR *mir);
static bool genArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                            int vSrc1, int vSrc2);
static bool genArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                             int vSrc1, int vSrc2);
static bool genCmpX(CompilationUnit *cUnit, MIR *mir, int vDest, int vSrc1,
                    int vSrc2);



#endif /* _DALVIK_VM_COMPILER_CODEGEN_ARM_CODEGEN_H */

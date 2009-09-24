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

#ifndef _DALVIK_VM_COMPILERCODEGEN_H_
#define _DALVIK_VM_COMPILERCODEGEN_H_

#include "compiler/CompilerIR.h"

/* Work unit is architecture dependent */
bool dvmCompilerDoWork(CompilerWorkOrder *work);

/* Lower middle-level IR to low-level IR */
void dvmCompilerMIR2LIR(CompilationUnit *cUnit);

/* Assemble LIR into machine code */
void dvmCompilerAssembleLIR(CompilationUnit *cUnit, JitTranslationInfo *info);

/* Implemented in the codegen/<target>/ArchUtility.c */
void dvmCompilerCodegenDump(CompilationUnit *cUnit);

/* Implemented in the codegen/<target>/Assembler.c */
void* dvmJitChain(void *tgtAddr, u4* branchAddr);
u4* dvmJitUnchain(void *codeAddr);
void dvmJitUnchainAll(void);

/* Implemented in codegen/<target>/Ralloc.c */
void dvmCompilerRegAlloc(CompilationUnit *cUnit);

/* Implemented in codegen/<target>/Thumb<version>Util.c */
void dvmCompilerInitializeRegAlloc(CompilationUnit *cUnit);

/* Implemented in codegen/<target>/<target_variant>/ArchVariant.c */
JitInstructionSetType dvmCompilerInstructionSet(CompilationUnit *cUnit);

#endif /* _DALVIK_VM_COMPILERCODEGEN_H_ */

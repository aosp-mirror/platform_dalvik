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
#include "Armv5teLIR.h"

#ifndef _DALVIK_VM_COMPILER_CODEGEN_CODEGEN_H
#define _DALVIK_VM_COMPILER_CODEGEN_CODEGEN_H

bool dvmCompilerGenConversionPortable(CompilationUnit *cUnit, MIR *mir);
bool dvmCompilerGenArithOpFloatPortable(CompilationUnit *cUnit, MIR *mir,
                                        int vDest, int vSrc1, int vSrc2);
bool dvmCompilerGenArithOpDoublePortable(CompilationUnit *cUnit, MIR *mir,
                                         int vDest, int vSrc1, int vSrc2);
void dvmCompilerLoadValueAddress(CompilationUnit *cUnit, int vSrc, int rDest);
void dvmCompilerGenDispatchToHandler(CompilationUnit *cUnit,
                                     TemplateOpCode opCode);
void dvmCompilerLoadValue(CompilationUnit *cUnit, int vSrc, int rDest);
void dvmCompilerStoreValue(CompilationUnit *cUnit, int rSrc, int vDest,
                           int rScratch);

#endif /* _DALVIK_VM_COMPILER_CODEGEN_CODEGEN_H */

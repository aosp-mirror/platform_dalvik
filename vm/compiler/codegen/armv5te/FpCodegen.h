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

#ifndef _DALVIK_VM_COMPILER_CODEGEN_FPCODEGEN_H
#define _DALVIK_VM_COMPILER_CODEGEN_FPCODEGEN_H

bool dvmCompilerGenConversion(CompilationUnit *cUnit, MIR *mir);
bool dvmCompilerGenArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                                int vSrc1, int vSrc2);
bool dvmCompilerGenArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                                 int vSrc1, int vSrc2);
bool dvmCompilerGenCmpX(CompilationUnit *cUnit, MIR *mir, int vDest,
                        int vSrc1, int vSrc2);


#endif /* _DALVIK_VM_COMPILER_CODEGEN_FPCODEGEN_H */

/*
 * Copyright (C) 2010 The Android Open Source Project
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
#define _CODEGEN_C
#define _IA32

#include "Dalvik.h"
#include "interp/InterpDefs.h"
#include "libdex/DexOpcodes.h"
#include "compiler/CompilerInternals.h"
#include "compiler/codegen/x86/X86LIR.h"
#include "mterp/common/FindInterface.h"
//#include "compiler/codegen/x86/Ralloc.h"
#include "compiler/codegen/x86/Codegen.h"
#include "compiler/Loop.h"
#include "ArchVariant.h"

/* Architectural independent building blocks */
//#include "../CodegenCommon.cpp"

/* Architectural independent building blocks */
//#include "../Thumb/Factory.cpp"
/* Factory utilities dependent on arch-specific features */
//#include "../CodegenFactory.cpp"

/* ia32 register allocation */
//#include "../ia32/Ralloc.cpp"

/* MIR2LIR dispatcher and architectural independent codegen routines */
#include "../CodegenDriver.cpp"

/* Architecture manifest */
#include "ArchVariant.cpp"

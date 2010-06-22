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
#define _CODEGEN_C
#define _ARMV5TE_VFP

#include "Dalvik.h"
#include "interp/InterpDefs.h"
#include "libdex/OpCode.h"
#include "libdex/OpCodeNames.h"
#include "compiler/CompilerInternals.h"
#include "compiler/codegen/arm/ArmLIR.h"
#include "mterp/common/FindInterface.h"
#include "compiler/codegen/arm/Ralloc.h"
#include "compiler/codegen/arm/Codegen.h"
#include "compiler/Loop.h"
#include "ArchVariant.h"

/* Architectural independent building blocks */
#include "../CodegenCommon.c"

/* Thumb-specific factory utilities */
#include "../Thumb/Factory.c"
/* Factory utilities dependent on arch-specific features */
#include "../CodegenFactory.c"

/* Thumb-specific codegen routines */
#include "../Thumb/Gen.c"
/* Thumb+VFP codegen routines */
#include "../FP/ThumbVFP.c"

/* Thumb-specific register allocation */
#include "../Thumb/Ralloc.c"

/* MIR2LIR dispatcher and architectural independent codegen routines */
#include "../CodegenDriver.c"

/* Architecture manifest */
#include "ArchVariant.c"

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
#include "interp/InterpDefs.h"
#include "libdex/OpCode.h"
#include "dexdump/OpCodeNames.h"
#include "vm/compiler/CompilerInternals.h"
#include "ArmLIR.h"
#include "vm/mterp/common/FindInterface.h"

#include "armv7-a/ArchVariant.h"

#include "Thumb2Util.c"
#include "Codegen.c"
#include "armv7-a/ArchVariant.c"

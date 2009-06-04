/*
 * Copyright (C) 2008 The Android Open Source Project
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

/*
 * Some declarations used throughout mterp.
 */
#ifndef _DALVIK_MTERP_MTERP
#define _DALVIK_MTERP_MTERP

#include "Dalvik.h"
#include "interp/InterpDefs.h"
#if defined(WITH_JIT)
#include "interp/Jit.h"
#endif

/*
 * Interpreter state, passed into C functions from assembly stubs.  The
 * assembly code exports all registers into the "glue" structure before
 * calling, then extracts them when the call returns.
 */
typedef InterpState MterpGlue;

/*
 * Call this during initialization to verify that the values in asm-constants.h
 * are still correct.
 */
bool dvmCheckAsmConstants(void);

/*
 * Local entry and exit points.  The platform-specific implementation must
 * provide these two.
 *
 * dvmMterpStdRun() returns the "changeInterp" argument from dvmMterpStdBail(),
 * indicating whether we want to bail out of the interpreter or just switch
 * between "standard" and "debug" mode.
 *
 * The "mterp" interpreter is always "standard".
 */
bool dvmMterpStdRun(MterpGlue* glue);
void dvmMterpStdBail(MterpGlue* glue, bool changeInterp);

#endif /*_DALVIK_MTERP_MTERP*/

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
 * Dalvik interpreter public definitions.
 */
#ifndef _DALVIK_INTERP_INTERP
#define _DALVIK_INTERP_INTERP

/*
 * Interpreter entry point.  Call here after setting up the interpreted
 * stack (most code will want to get here via dvmCallMethod().)
 */
void dvmInterpret(Thread* thread, const Method* method, JValue* pResult);

/*
 * Throw an exception for a problem detected by the verifier.
 */
void dvmThrowVerificationError(const DvmDex* pDvmDex, int kind, int ref);

/*
 * Breakpoint optimization table.
 */
void dvmInitBreakpoints();
void dvmAddBreakAddr(Method* method, int instrOffset);
void dvmClearBreakAddr(Method* method, int instrOffset);
bool dvmAddSingleStep(Thread* thread, int size, int depth);
void dvmClearSingleStep(Thread* thread);

#endif /*_DALVIK_INTERP_INTERP*/

/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * Implementation of clz(), which returns the number of leading zero bits,
 * starting at the most significant bit position.  If the argument is zero,
 * the result is undefined.
 *
 * On some platforms, gcc provides a __builtin_clz() function that uses
 * an optimized implementation (e.g. the CLZ instruction on ARM).
 *
 * This gets a little tricky for ARM, because it's only available in ARMv5
 * and above, and even on ARMv5 it's not available for THUMB code.  So we
 * need to tailor this for every source file.
 */
#ifndef _DALVIK_CLZ

#if defined(__arm__) && !defined(__thumb__)
# include <machine/cpu-features.h>
# if defined(__ARM_HAVE_CLZ)
#  define CLZ(x) __builtin_clz(x)
#  define HAVE_BUILTIN_CLZ
# endif
#endif

#ifndef HAVE_BUILTIN_CLZ
# define CLZ(x) dvmClzImpl(x)
int dvmClzImpl(unsigned int x);
#endif

#endif // _DALVIK_CLZ

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
 * Atomic operations
 */
#ifndef _DALVIK_ATOMIC
#define _DALVIK_ATOMIC

#include <utils/Atomic.h>       /* use common Android atomic ops */

/*
 * Memory barrier.  Guarantee that register-resident variables
 * are flushed to memory, and guarantee that instructions before
 * the barrier do not get reordered to appear past it.
 *
 * 'asm volatile ("":::"memory")' is probably overkill, but it's correct.
 * There may be a way to do it that doesn't flush every single register.
 *
 * TODO: look into the wmb() family on Linux and equivalents on other systems.
 */
#define MEM_BARRIER()   do { asm volatile ("":::"memory"); } while (0)

/*
 * Atomic compare-and-swap macro.
 *
 * If *_addr equals "_old", replace it with "_new" and return 1.  Otherwise
 * return 0.  (e.g. x86 "cmpxchgl" instruction.)
 *
 * Underlying function is currently declared:
 * int android_atomic_cmpxchg(int32_t old, int32_t new, volatile int32_t* addr)
 */
#define ATOMIC_CMP_SWAP(_addr, _old, _new) \
            (android_atomic_cmpxchg((_old), (_new), (_addr)) == 0)

#endif /*_DALVIK_ATOMIC*/

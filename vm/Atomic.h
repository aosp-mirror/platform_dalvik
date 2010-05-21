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

#include <cutils/atomic.h>          /* use common Android atomic ops */
#include <cutils/atomic-inline.h>   /* and some uncommon ones */

/*
 * Full memory barrier.  Ensures compiler ordering and SMP behavior.
 */
#define MEM_BARRIER()   android_membar_full()

/*
 * 32-bit atomic compare-and-swap macro.  Performs a memory barrier
 * before the swap (store-release).
 *
 * If *_addr equals "_old", replace it with "_new" and return nonzero.
 *
 * Underlying function is currently declared:
 * int android_atomic_cmpxchg(int32_t old, int32_t new, volatile int32_t* addr)
 */
#define ATOMIC_CMP_SWAP(_addr, _old, _new) \
            (android_atomic_cmpxchg((_old), (_new), (_addr)) == 0)


/*
 * NOTE: Two "quasiatomic" operations on the exact same memory address
 * are guaranteed to operate atomically with respect to each other,
 * but no guarantees are made about quasiatomic operations mixed with
 * non-quasiatomic operations on the same address, nor about
 * quasiatomic operations that are performed on partially-overlapping
 * memory.
 */

/*
 * TODO: rename android_quasiatomic_* to dvmQuasiatomic*.  Don't want to do
 * that yet due to branch merge issues.
 */
int64_t android_quasiatomic_swap_64(int64_t value, volatile int64_t* addr);
int64_t android_quasiatomic_read_64(volatile int64_t* addr);
int android_quasiatomic_cmpxchg_64(int64_t oldvalue, int64_t newvalue,
        volatile int64_t* addr);

#endif /*_DALVIK_ATOMIC*/

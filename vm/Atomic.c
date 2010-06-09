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

#include "Dalvik.h"

#include <cutils/atomic.h>

/*
 * Quasi-atomic 64-bit operations, for platforms that lack the real thing.
 *
 * TODO: unify ARM/x86/sh implementations using the to-be-written
 * spin lock implementation.  We don't want to rely on mutex innards,
 * and it would be great if all platforms were running the same code.
 */

#if defined(HAVE_MACOSX_IPC)

#include <libkern/OSAtomic.h>

#if defined(__ppc__)        \
    || defined(__PPC__)     \
    || defined(__powerpc__) \
    || defined(__powerpc)   \
    || defined(__POWERPC__) \
    || defined(_M_PPC)      \
    || defined(__PPC)
#define NEED_QUASIATOMICS 1
#else

int android_quasiatomic_cmpxchg_64(int64_t oldvalue, int64_t newvalue,
        volatile int64_t* addr) {
    return OSAtomicCompareAndSwap64Barrier(oldvalue, newvalue,
            (int64_t*)addr) == 0;
}

int64_t android_quasiatomic_swap_64(int64_t value, volatile int64_t* addr) {
    int64_t oldValue;
    do {
        oldValue = *addr;
    } while (android_quasiatomic_cmpxchg_64(oldValue, value, addr));
    return oldValue;
}

int64_t android_quasiatomic_read_64(volatile int64_t* addr) {
    return OSAtomicAdd64Barrier(0, addr);
}
#endif

#elif defined(__i386__) || defined(__x86_64__)
#define NEED_QUASIATOMICS 1

#elif __arm__
// Most of the implementation is in atomic-android-arm.s.

// on the device, we implement the 64-bit atomic operations through
// mutex locking. normally, this is bad because we must initialize
// a pthread_mutex_t before being able to use it, and this means
// having to do an initialization check on each function call, and
// that's where really ugly things begin...
//
// BUT, as a special twist, we take advantage of the fact that in our
// pthread library, a mutex is simply a volatile word whose value is always
// initialized to 0. In other words, simply declaring a static mutex
// object initializes it !
//
// another twist is that we use a small array of mutexes to dispatch
// the contention locks from different memory addresses
//

#include <pthread.h>

#define  SWAP_LOCK_COUNT  32U
static pthread_mutex_t  _swap_locks[SWAP_LOCK_COUNT];

#define  SWAP_LOCK(addr)   \
   &_swap_locks[((unsigned)(void*)(addr) >> 3U) % SWAP_LOCK_COUNT]


int64_t android_quasiatomic_swap_64(int64_t value, volatile int64_t* addr) {
    int64_t oldValue;
    pthread_mutex_t*  lock = SWAP_LOCK(addr);

    pthread_mutex_lock(lock);

    oldValue = *addr;
    *addr    = value;

    pthread_mutex_unlock(lock);
    return oldValue;
}

int android_quasiatomic_cmpxchg_64(int64_t oldvalue, int64_t newvalue,
        volatile int64_t* addr) {
    int result;
    pthread_mutex_t*  lock = SWAP_LOCK(addr);

    pthread_mutex_lock(lock);

    if (*addr == oldvalue) {
        *addr  = newvalue;
        result = 0;
    } else {
        result = 1;
    }
    pthread_mutex_unlock(lock);
    return result;
}

int64_t android_quasiatomic_read_64(volatile int64_t* addr) {
    int64_t result;
    pthread_mutex_t*  lock = SWAP_LOCK(addr);

    pthread_mutex_lock(lock);
    result = *addr;
    pthread_mutex_unlock(lock);
    return result;
}

/*****************************************************************************/
#elif __sh__
#define NEED_QUASIATOMICS 1

#else
#error "Unsupported atomic operations for this platform"
#endif


#if NEED_QUASIATOMICS

/* Note that a spinlock is *not* a good idea in general
 * since they can introduce subtle issues. For example,
 * a real-time thread trying to acquire a spinlock already
 * acquired by another thread will never yeld, making the
 * CPU loop endlessly!
 *
 * However, this code is only used on the Linux simulator
 * so it's probably ok for us.
 *
 * The alternative is to use a pthread mutex, but
 * these must be initialized before being used, and
 * then you have the problem of lazily initializing
 * a mutex without any other synchronization primitive.
 *
 * TODO: these currently use sched_yield(), which is not guaranteed to
 * do anything at all.  We need to use dvmIterativeSleep or a wait /
 * notify mechanism if the initial attempt fails.
 */

/* global spinlock for all 64-bit quasiatomic operations */
static int32_t quasiatomic_spinlock = 0;

int android_quasiatomic_cmpxchg_64(int64_t oldvalue, int64_t newvalue,
        volatile int64_t* addr) {
    int result;

    while (android_atomic_acquire_cas(0, 1, &quasiatomic_spinlock)) {
#ifdef HAVE_WIN32_THREADS
        Sleep(0);
#else
        sched_yield();
#endif
    }

    if (*addr == oldvalue) {
        *addr = newvalue;
        result = 0;
    } else {
        result = 1;
    }

    android_atomic_release_store(0, &quasiatomic_spinlock);

    return result;
}

int64_t android_quasiatomic_read_64(volatile int64_t* addr) {
    int64_t result;

    while (android_atomic_acquire_cas(0, 1, &quasiatomic_spinlock)) {
#ifdef HAVE_WIN32_THREADS
        Sleep(0);
#else
        sched_yield();
#endif
    }

    result = *addr;
    android_atomic_release_store(0, &quasiatomic_spinlock);

    return result;
}

int64_t android_quasiatomic_swap_64(int64_t value, volatile int64_t* addr) {
    int64_t result;

    while (android_atomic_acquire_cas(0, 1, &quasiatomic_spinlock)) {
#ifdef HAVE_WIN32_THREADS
        Sleep(0);
#else
        sched_yield();
#endif
    }

    result = *addr;
    *addr = value;
    android_atomic_release_store(0, &quasiatomic_spinlock);

    return result;
}

#endif

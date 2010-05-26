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

/*
 * Atomic operation performance test.
 */
#include "Dalvik.h"

//#define TRIVIAL_COMPARE     /* do something simple instead of an atomic op */

/*
 * Perform operation.  Returns elapsed time.
 */
u8 dvmTestAtomicSpeedSub(int repeatCount)
{
    static int value = 7;
    int* valuePtr = &value;
    u8 start, end;
    int i;
    
#ifdef TRIVIAL_COMPARE
    /* init to arg value so compiler can't pre-determine result */
    int j = repeatCount;
#endif

    assert((repeatCount % 10) == 0);

    start = dvmGetRelativeTimeNsec();

    for (i = repeatCount / 10; i != 0; i--) {
#ifdef TRIVIAL_COMPARE
        // integer add (Dream: 3.4ns -- THUMB has 10 adds, ARM condenses)
        j += i; j += i; j += i; j += i; j += i;
        j += i; j += i; j += i; j += i; j += i;
#else
        // succeed 10x (Dream: 155.9ns)
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);
        (void)ATOMIC_CMP_SWAP(valuePtr, 7, 7);

        // fail 10x (Dream: 158.5ns)
        /*
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        ATOMIC_CMP_SWAP(valuePtr, 6, 7);
        */
#endif
    }

    end = dvmGetRelativeTimeNsec();

#ifdef TRIVIAL_COMPARE
    /* use value so compiler can't eliminate it */
    dvmFprintf(stdout, "%d\n", j);
#else
    dvmFprintf(stdout, ".");
    fflush(stdout);     // not quite right if they intercepted fprintf
#endif
    return end - start;
}

/*
 * Control loop.
 */
bool dvmTestAtomicSpeed(void)
{
    static const int kIterations = 10;
    static const int kRepeatCount = 5 * 1000 * 1000;
    static const int kDelay = 500 * 1000;
    u8 results[kIterations];
    int i;

    for (i = 0; i < kIterations; i++) {
        results[i] = dvmTestAtomicSpeedSub(kRepeatCount);
        usleep(kDelay);
    }

    dvmFprintf(stdout, "\n");
    dvmFprintf(stdout, "Atomic speed test results (%d per iteration):\n",
        kRepeatCount);
    for (i = 0; i < kIterations; i++) {
        dvmFprintf(stdout,
            " %2d: %.3fns\n", i, (double) results[i] / kRepeatCount);
    }

    return true;
}


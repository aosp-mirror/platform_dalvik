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
 * Jit control
 */
#ifndef _DALVIK_INTERP_JIT
#define _DALVIK_INTERP_JIT

#include "InterpDefs.h"

#define JIT_PROF_SIZE 512

#define JIT_MAX_TRACE_LEN 100

/*
 * JitTable hash function.
 */

static inline u4 dvmJitHashMask( const u2* p, u4 mask ) {
    return ((((u4)p>>12)^(u4)p)>>1) & (mask);
}

static inline u4 dvmJitHash( const u2* p ) {
    return dvmJitHashMask( p, gDvmJit.jitTableMask );
}

/*
 * Entries in the JIT's address lookup hash table.
 * Fields which may be updated by multiple threads packed into a
 * single 32-bit word to allow use of atomic update.
 */

typedef struct JitEntryInfo {
    unsigned int           traceRequested:1;   /* already requested a translation */
    unsigned int           isMethodEntry:1;
    unsigned int           inlineCandidate:1;
    unsigned int           profileEnabled:1;
    JitInstructionSetType  instructionSet:4;
    unsigned int           unused:8;
    u2                     chain;              /* Index of next in chain */
} JitEntryInfo;

typedef union JitEntryInfoUnion {
    JitEntryInfo info;
    volatile int infoWord;
} JitEntryInfoUnion;

typedef struct JitEntry {
    JitEntryInfoUnion u;
    u2                chain;              /* Index of next in chain */
    const u2*         dPC;                /* Dalvik code address */
    void*             codeAddress;        /* Code address of native translation */
} JitEntry;

int dvmJitStartup(void);
void dvmJitShutdown(void);
int dvmCheckJit(const u2* pc, Thread* self, InterpState* interpState);
void* dvmJitGetCodeAddr(const u2* dPC);
bool dvmJitCheckTraceRequest(Thread* self, InterpState* interpState);
void dvmJitStopTranslationRequests(void);
void dvmJitStats(void);
bool dvmJitResizeJitTable(unsigned int size);
struct JitEntry *dvmFindJitEntry(const u2* pc);
s8 dvmJitd2l(double d);
s8 dvmJitf2l(float f);
void dvmJitSetCodeAddr(const u2* dPC, void *nPC, JitInstructionSetType set);


#endif /*_DALVIK_INTERP_JIT*/

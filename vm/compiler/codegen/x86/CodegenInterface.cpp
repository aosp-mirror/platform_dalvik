/*
 * Copyright (C) 2010-2013 Intel Corporation
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
#include <sys/mman.h>
#include "Dalvik.h"
#include "libdex/DexOpcodes.h"
#include "compiler/Compiler.h"
#include "compiler/CompilerIR.h"
#include "interp/Jit.h"
#include "libdex/DexFile.h"
#include "Lower.h"
#include "NcgAot.h"
#include "compiler/codegen/CompilerCodegen.h"
#include <cutils/properties.h>
#include "InstructionGeneration.h"
#include "Singleton.h"
#include "ExceptionHandling.h"
#include "Scheduler.h"

/* JIT opcode filtering */
bool jitOpcodeTable[kNumPackedOpcodes];
Opcode jitNotSupportedOpcode[] = {
    OP_INVOKE_OBJECT_INIT_RANGE,
};

/* Init values when a predicted chain is initially assembled */
/* E7FE is branch to self */
#define PREDICTED_CHAIN_BX_PAIR_INIT     0xe7fe

#if defined(WITH_JIT)
/* Target-specific save/restore */
extern "C" void dvmJitCalleeSave(double *saveArea);
extern "C" void dvmJitCalleeRestore(double *saveArea);
#endif

/*
 * Determine the initial instruction set to be used for this trace.
 * Later components may decide to change this.
 */
//JitInstructionSetType dvmCompilerInstructionSet(CompilationUnit *cUnit)
JitInstructionSetType dvmCompilerInstructionSet(void)
{
    return DALVIK_JIT_IA32;
}

JitInstructionSetType dvmCompilerGetInterpretTemplateSet()
{
    return DALVIK_JIT_IA32;
}

/* we don't use template for IA32 */
void *dvmCompilerGetInterpretTemplate()
{
      return NULL;//(void*) ((int)gDvmJit.codeCache);
}

/* Initialize the jitOpcodeTable which records what opcodes are supported
 *  by the JIT compiler.
 */
void dvmInitJitOpcodeTable() {
    unsigned int i;
    memset(jitOpcodeTable, 1, sizeof(jitOpcodeTable));
    for (i = 0; i < sizeof(jitNotSupportedOpcode)/sizeof(Opcode); i++) {
        jitOpcodeTable[((unsigned int)jitNotSupportedOpcode[i])] = false;
    }
    for (i = 0; i < sizeof(jitOpcodeTable)/sizeof(bool); i++) {
        if (jitOpcodeTable[i] == false)
            ALOGV("opcode 0x%x not supported by JIT", i);
    }
}

/* Return true if the opcode is supported by the JIT compiler. */
bool dvmIsOpcodeSupportedByJit(const DecodedInstruction & insn)
{
     /* reject traces containing bytecodes requesting virtual registers exceeding allowed limit */
     if ((insn.opcode == OP_INVOKE_VIRTUAL_RANGE) || (insn.opcode == OP_INVOKE_VIRTUAL_QUICK_RANGE) ||
         (insn.opcode == OP_INVOKE_SUPER_RANGE) || (insn.opcode == OP_INVOKE_SUPER_QUICK_RANGE) ||
         (insn.opcode == OP_INVOKE_DIRECT_RANGE) || (insn.opcode == OP_INVOKE_STATIC_RANGE) ||
         (insn.opcode == OP_INVOKE_INTERFACE_RANGE)){
        int opcodeArgs = (int) (insn.vA);
        if (opcodeArgs > MAX_REG_PER_BYTECODE)
           return false;
     }
    return jitOpcodeTable[((int) insn.opcode)];
}

/* Track the number of times that the code cache is patched */
#if defined(WITH_JIT_TUNING)
#define UPDATE_CODE_CACHE_PATCHES()    (gDvmJit.codeCachePatches++)
#else
#define UPDATE_CODE_CACHE_PATCHES()
#endif

//! default JIT table size used by x86 JIT
#define DEFAULT_X86_ATOM_DALVIK_JIT_TABLE_SIZE 1<<12
//! default JIT threshold used by x86 JIT
#define DEFAULT_X86_ATOM_DALVIK_JIT_THRESHOLD 50
//! default JIT code cache size used by x86 JIT
#define DEFAULT_X86_ATOM_DALVIK_JIT_CODE_CACHE_SIZE 512*1024

//! Initializes target-specific configuration

//! Configures the jit table size, jit threshold, and jit code cache size
//! Initializes status of all threads and the table of supported bytecodes
//! @return true when initialization is successful (NOTE: current
//! implementation always returns true)
bool dvmCompilerArchInit() {
    // Used to get global properties
    char propertyBuffer[PROPERTY_VALUE_MAX];
    unsigned long propertyValue;

    // Used to identify cpu
    int familyAndModelInformation;
    const int familyIdMask = 0xF00;
    const int familyIdShift = 8;
    const int modelMask = 0XF0;
    const int modelShift = 4;
    const int modelWidth = 4;
    const int extendedModelIdMask = 0xF0000;
    const int extendedModelShift = 16;

    // Initialize JIT table size
    if(gDvmJit.jitTableSize == 0 || (gDvmJit.jitTableSize & (gDvmJit.jitTableSize - 1))) {
        // JIT table size has not been initialized yet or is not a power of two
        memset(propertyBuffer, 0, PROPERTY_VALUE_MAX); // zero out buffer so we don't use junk
        property_get("dalvik.jit.table_size", propertyBuffer, NULL);
        propertyValue = strtoul(propertyBuffer, NULL, 10 /*base*/);
        if (errno == ERANGE || propertyValue == 0ul || (propertyValue & (propertyValue - 1ul)))
            /* out of range, conversion failed, trying to use invalid value of 0, or using non-power of two */
            gDvmJit.jitTableSize = DEFAULT_X86_ATOM_DALVIK_JIT_TABLE_SIZE;
        else // property is valid, but we still need to cast from unsigned long to unsigned int
            gDvmJit.jitTableSize = static_cast<unsigned int>(propertyValue);
    }

    // Initialize JIT table mask
    gDvmJit.jitTableMask = gDvmJit.jitTableSize - 1;

    // Initialize JIT threshold
    if(gDvmJit.threshold == 0) { // JIT threshold has not been initialized yet
        memset(propertyBuffer, 0, PROPERTY_VALUE_MAX); // zero out buffer so we don't use junk
        property_get("dalvik.jit.threshold", propertyBuffer, NULL);
        propertyValue = strtoul(propertyBuffer, NULL, 10 /*base*/);
        if (errno == ERANGE || propertyValue == 0ul)
            /* out of range, conversion failed, or trying to use invalid value of 0 */
            gDvmJit.threshold = DEFAULT_X86_ATOM_DALVIK_JIT_THRESHOLD;
        else // property is valid, but we still need to cast from unsigned long to unsigned short
            gDvmJit.threshold = static_cast<unsigned short>(propertyValue);
    }

    // Initialize JIT code cache size
    if(gDvmJit.codeCacheSize == 0) { // JIT code cache size has not been initialized yet
        memset(propertyBuffer, 0, PROPERTY_VALUE_MAX); // zero out buffer so we don't use junk
        property_get("dalvik.jit.code_cache_size", propertyBuffer, NULL);
        propertyValue = strtoul(propertyBuffer, NULL, 10 /*base*/);
        if (errno == ERANGE || propertyValue == 0ul)
            /* out of range, conversion failed, or trying to use invalid value of 0 */
            gDvmJit.codeCacheSize = DEFAULT_X86_ATOM_DALVIK_JIT_CODE_CACHE_SIZE;
        else // property is valid, but we still need to cast from unsigned long to unsigned int
            gDvmJit.codeCacheSize = static_cast<unsigned int>(propertyValue);
    }

    // Print out values used
    ALOGV("JIT threshold set to %hu",gDvmJit.threshold);
    ALOGV("JIT table size set to %u",gDvmJit.jitTableSize);
    ALOGV("JIT code cache size set to %u",gDvmJit.codeCacheSize);

    // Now determine machine model
    asm volatile (
            "movl $1, %%eax\n\t"
            "pushl %%ebx\n\t"
            "cpuid\n\t"
            "popl %%ebx\n\t"
            "movl %%eax, %0"
            : "=r" (familyAndModelInformation)
            :
            : "eax", "ecx", "edx");
    gDvmJit.cpuFamily = (familyAndModelInformation & familyIdMask) >> familyIdShift;
    gDvmJit.cpuModel = (((familyAndModelInformation & extendedModelIdMask)
            >> extendedModelShift) << modelWidth)
            + ((familyAndModelInformation & modelMask) >> modelShift);

#if defined(WITH_SELF_VERIFICATION)
    /* Force into blocking mode */
    gDvmJit.blockingMode = true;
    gDvm.nativeDebuggerActive = true;
#endif

    // Make sure all threads have current values
    dvmJitUpdateThreadStateAll();

    /* Initialize jitOpcodeTable for JIT supported opcode */
    dvmInitJitOpcodeTable();

    return true;
}

void dvmCompilerPatchInlineCache(void)
{
    int i;
    PredictedChainingCell *minAddr, *maxAddr;

    /* Nothing to be done */
    if (gDvmJit.compilerICPatchIndex == 0) return;

    /*
     * Since all threads are already stopped we don't really need to acquire
     * the lock. But race condition can be easily introduced in the future w/o
     * paying attention so we still acquire the lock here.
     */
    dvmLockMutex(&gDvmJit.compilerICPatchLock);

    UNPROTECT_CODE_CACHE(gDvmJit.codeCache, gDvmJit.codeCacheByteUsed);

    //ALOGD("Number of IC patch work orders: %d", gDvmJit.compilerICPatchIndex);

    /* Initialize the min/max address range */
    minAddr = (PredictedChainingCell *)
        ((char *) gDvmJit.codeCache + gDvmJit.codeCacheSize);
    maxAddr = (PredictedChainingCell *) gDvmJit.codeCache;

    for (i = 0; i < gDvmJit.compilerICPatchIndex; i++) {
        ICPatchWorkOrder *workOrder = &gDvmJit.compilerICPatchQueue[i];
        PredictedChainingCell *cellAddr = workOrder->cellAddr;
        PredictedChainingCell *cellContent = &workOrder->cellContent;
        ClassObject *clazz = dvmFindClassNoInit(workOrder->classDescriptor,
                                                workOrder->classLoader);

        assert(clazz->serialNumber == workOrder->serialNumber);

        /* Use the newly resolved clazz pointer */
        cellContent->clazz = clazz;

        if (cellAddr->clazz == NULL) {
            COMPILER_TRACE_CHAINING(
                ALOGI("Jit Runtime: predicted chain %p to %s (%s) initialized",
                      cellAddr,
                      cellContent->clazz->descriptor,
                      cellContent->method->name));
        } else {
            COMPILER_TRACE_CHAINING(
                ALOGI("Jit Runtime: predicted chain %p from %s to %s (%s) "
                      "patched",
                      cellAddr,
                      cellAddr->clazz->descriptor,
                      cellContent->clazz->descriptor,
                      cellContent->method->name));
        }

        /* Patch the chaining cell */
        *cellAddr = *cellContent;
        minAddr = (cellAddr < minAddr) ? cellAddr : minAddr;
        maxAddr = (cellAddr > maxAddr) ? cellAddr : maxAddr;
    }

    PROTECT_CODE_CACHE(gDvmJit.codeCache, gDvmJit.codeCacheByteUsed);

    gDvmJit.compilerICPatchIndex = 0;
    dvmUnlockMutex(&gDvmJit.compilerICPatchLock);
}

/* Target-specific cache clearing */
void dvmCompilerCacheClear(char *start, size_t size)
{
    /* "0xFF 0xFF" is an invalid opcode for x86. */
    memset(start, 0xFF, size);
}

/* for JIT debugging, to be implemented */
void dvmJitCalleeSave(double *saveArea) {
}

void dvmJitCalleeRestore(double *saveArea) {
}

void dvmJitToInterpSingleStep() {
}

JitTraceDescription *dvmCopyTraceDescriptor(const u2 *pc,
                                            const JitEntry *knownEntry) {
    return NULL;
}

void dvmCompilerCodegenDump(CompilationUnit *cUnit) //in ArchUtility.c
{
}

void dvmCompilerArchDump(void)
{
}

char *getTraceBase(const JitEntry *p)
{
    return NULL;
}

void dvmCompilerAssembleLIR(CompilationUnit *cUnit, JitTranslationInfo* info)
{
}

void dvmJitInstallClassObjectPointers(CompilationUnit *cUnit, char *codeAddress)
{
}

void dvmCompilerMethodMIR2LIR(CompilationUnit *cUnit)
{
    // Method-based JIT not supported for x86.
}

void dvmJitScanAllClassPointers(void (*callback)(void *))
{
}

/* Handy function to retrieve the profile count */
static inline int getProfileCount(const JitEntry *entry)
{
    if (entry->dPC == 0 || entry->codeAddress == 0)
        return 0;
    u4 *pExecutionCount = (u4 *) getTraceBase(entry);

    return pExecutionCount ? *pExecutionCount : 0;
}

/* qsort callback function */
static int sortTraceProfileCount(const void *entry1, const void *entry2)
{
    const JitEntry *jitEntry1 = (const JitEntry *)entry1;
    const JitEntry *jitEntry2 = (const JitEntry *)entry2;

    JitTraceCounter_t count1 = getProfileCount(jitEntry1);
    JitTraceCounter_t count2 = getProfileCount(jitEntry2);
    return (count1 == count2) ? 0 : ((count1 > count2) ? -1 : 1);
}

/* Sort the trace profile counts and dump them */
void dvmCompilerSortAndPrintTraceProfiles() //in Assemble.c
{
    JitEntry *sortedEntries;
    int numTraces = 0;
    unsigned long counts = 0;
    unsigned int i;

    /* Make sure that the table is not changing */
    dvmLockMutex(&gDvmJit.tableLock);

    /* Sort the entries by descending order */
    sortedEntries = (JitEntry *)malloc(sizeof(JitEntry) * gDvmJit.jitTableSize);
    if (sortedEntries == NULL)
        goto done;
    memcpy(sortedEntries, gDvmJit.pJitEntryTable,
           sizeof(JitEntry) * gDvmJit.jitTableSize);
    qsort(sortedEntries, gDvmJit.jitTableSize, sizeof(JitEntry),
          sortTraceProfileCount);

    /* Dump the sorted entries */
    for (i=0; i < gDvmJit.jitTableSize; i++) {
        if (sortedEntries[i].dPC != 0) {
            numTraces++;
        }
    }
    if (numTraces == 0)
        numTraces = 1;
    ALOGI("JIT: Average execution count -> %d",(int)(counts / numTraces));

    free(sortedEntries);
done:
    dvmUnlockMutex(&gDvmJit.tableLock);
    return;
}

/**
 * @brief Generates a jump with 32-bit relative immediate that jumps
 * to the target.
 * @details Updates the instruction stream with the jump.
 * @param target absolute address of target.
 */
void unconditional_jump_rel32(void * target) {
    // We will need to figure out the immediate to use for the relative
    // jump, so we need to flush scheduler so that stream is updated.
    // In most cases this won't affect the schedule since the jump would've
    // ended the native BB anyway and would've been scheduled last.
    if(gDvmJit.scheduling)
        singletonPtr<Scheduler>()->signalEndOfNativeBasicBlock();

    // Calculate the address offset between the destination of jump and the
    // function we are jumping to.
    int relOffset = reinterpret_cast<int>(target)
            - reinterpret_cast<int>(stream);

    // Since instruction pointer will already be updated when executing this,
    // subtract size of jump instruction
    relOffset -= getJmpCallInstSize(OpndSize_32, JmpCall_uncond);

    // Generate the unconditional jump now
    unconditional_jump_int(relOffset, OpndSize_32);
}

// works whether instructions for target basic block are generated or not
LowOp* jumpToBasicBlock(char* instAddr, int targetId) {
    stream = instAddr;
    bool unknown;
    OpndSize size;
    if(gDvmJit.scheduling) {
        unconditional_jump_block(targetId);
    } else {
        int relativeNCG = getRelativeNCG(targetId, JmpCall_uncond, &unknown, &size);
        unconditional_jump_int(relativeNCG, size);
    }
    return NULL;
}

LowOp* condJumpToBasicBlock(char* instAddr, ConditionCode cc, int targetId) {
    stream = instAddr;
    bool unknown;
    OpndSize size;
    if(gDvmJit.scheduling) {
        conditional_jump_block(cc, targetId);
    } else {
        int relativeNCG = getRelativeNCG(targetId, JmpCall_cond, &unknown, &size);
        conditional_jump_int(cc, relativeNCG, size);
    }
    return NULL;
}

/*
 * Attempt to enqueue a work order to patch an inline cache for a predicted
 * chaining cell for virtual/interface calls.
 */
static bool inlineCachePatchEnqueue(PredictedChainingCell *cellAddr,
                                    PredictedChainingCell *newContent)
{
    bool result = true;

    /*
     * Make sure only one thread gets here since updating the cell (ie fast
     * path and queueing the request (ie the queued path) have to be done
     * in an atomic fashion.
     */
    dvmLockMutex(&gDvmJit.compilerICPatchLock);

    /* Fast path for uninitialized chaining cell */
    if (cellAddr->clazz == NULL &&
        cellAddr->branch == PREDICTED_CHAIN_BX_PAIR_INIT) {
        UNPROTECT_CODE_CACHE(cellAddr, sizeof(*cellAddr));

        cellAddr->method = newContent->method;
        cellAddr->branch = newContent->branch;
        cellAddr->branch2 = newContent->branch2;

        /*
         * The update order matters - make sure clazz is updated last since it
         * will bring the uninitialized chaining cell to life.
         */
        android_atomic_release_store((int32_t)newContent->clazz,
            (volatile int32_t *)(void*) &cellAddr->clazz);
        //cacheflush((intptr_t) cellAddr, (intptr_t) (cellAddr+1), 0);
        UPDATE_CODE_CACHE_PATCHES();

        PROTECT_CODE_CACHE(cellAddr, sizeof(*cellAddr));

#if 0
        MEM_BARRIER();
        cellAddr->clazz = newContent->clazz;
        //cacheflush((intptr_t) cellAddr, (intptr_t) (cellAddr+1), 0);
#endif
#if defined(WITH_JIT_TUNING)
        gDvmJit.icPatchInit++;
#endif
        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: FAST predicted chain %p to method %s%s %p",
                  cellAddr, newContent->clazz->descriptor, newContent->method->name, newContent->method));
    /* Check if this is a frequently missed clazz */
    } else if (cellAddr->stagedClazz != newContent->clazz) {
        /* Not proven to be frequent yet - build up the filter cache */
        UNPROTECT_CODE_CACHE(cellAddr, sizeof(*cellAddr));

        cellAddr->stagedClazz = newContent->clazz;

        UPDATE_CODE_CACHE_PATCHES();
        PROTECT_CODE_CACHE(cellAddr, sizeof(*cellAddr));

#if defined(WITH_JIT_TUNING)
        gDvmJit.icPatchRejected++;
#endif
    /*
     * Different classes but same method implementation - it is safe to just
     * patch the class value without the need to stop the world.
     */
    } else if (cellAddr->method == newContent->method) {
        UNPROTECT_CODE_CACHE(cellAddr, sizeof(*cellAddr));

        cellAddr->clazz = newContent->clazz;
        /* No need to flush the cache here since the branch is not patched */
        UPDATE_CODE_CACHE_PATCHES();

        PROTECT_CODE_CACHE(cellAddr, sizeof(*cellAddr));

#if defined(WITH_JIT_TUNING)
        gDvmJit.icPatchLockFree++;
#endif
    /*
     * Cannot patch the chaining cell inline - queue it until the next safe
     * point.
     */
    } else if (gDvmJit.compilerICPatchIndex < COMPILER_IC_PATCH_QUEUE_SIZE)  {
        int index = gDvmJit.compilerICPatchIndex++;
        const ClassObject *clazz = newContent->clazz;

        gDvmJit.compilerICPatchQueue[index].cellAddr = cellAddr;
        gDvmJit.compilerICPatchQueue[index].cellContent = *newContent;
        gDvmJit.compilerICPatchQueue[index].classDescriptor = clazz->descriptor;
        gDvmJit.compilerICPatchQueue[index].classLoader = clazz->classLoader;
        /* For verification purpose only */
        gDvmJit.compilerICPatchQueue[index].serialNumber = clazz->serialNumber;

#if defined(WITH_JIT_TUNING)
        gDvmJit.icPatchQueued++;
#endif
        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: QUEUE predicted chain %p to method %s%s",
                  cellAddr, newContent->clazz->descriptor, newContent->method->name));
    } else {
    /* Queue is full - just drop this patch request */
#if defined(WITH_JIT_TUNING)
        gDvmJit.icPatchDropped++;
#endif

        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: DROP predicted chain %p to method %s%s",
                  cellAddr, newContent->clazz->descriptor, newContent->method->name));
    }

    dvmUnlockMutex(&gDvmJit.compilerICPatchLock);
    return result;
}

/*
 * This method is called from the invoke templates for virtual and interface
 * methods to speculatively setup a chain to the callee. The templates are
 * written in assembly and have setup method, cell, and clazz at r0, r2, and
 * r3 respectively, so there is a unused argument in the list. Upon return one
 * of the following three results may happen:
 *   1) Chain is not setup because the callee is native. Reset the rechain
 *      count to a big number so that it will take a long time before the next
 *      rechain attempt to happen.
 *   2) Chain is not setup because the callee has not been created yet. Reset
 *      the rechain count to a small number and retry in the near future.
 *   3) Ask all other threads to stop before patching this chaining cell.
 *      This is required because another thread may have passed the class check
 *      but hasn't reached the chaining cell yet to follow the chain. If we
 *      patch the content before halting the other thread, there could be a
 *      small window for race conditions to happen that it may follow the new
 *      but wrong chain to invoke a different method.
 */
const Method *dvmJitToPatchPredictedChain(const Method *method,
                                          Thread *self,
                                          PredictedChainingCell *cell,
                                          const ClassObject *clazz)
{
    int newRechainCount = PREDICTED_CHAIN_COUNTER_RECHAIN;
    /* Don't come back here for a long time if the method is native */
    if (dvmIsNativeMethod(method)) {
        UNPROTECT_CODE_CACHE(cell, sizeof(*cell));

        /*
         * Put a non-zero/bogus value in the clazz field so that it won't
         * trigger immediate patching and will continue to fail to match with
         * a real clazz pointer.
         */
        cell->clazz = (ClassObject *) PREDICTED_CHAIN_FAKE_CLAZZ;

        UPDATE_CODE_CACHE_PATCHES();
        PROTECT_CODE_CACHE(cell, sizeof(*cell));
        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: predicted chain %p to native method %s ignored",
                  cell, method->name));
        goto done;
    }
    {
    int tgtAddr = (int) dvmJitGetTraceAddr(method->insns);

    /*
     * Compilation not made yet for the callee. Reset the counter to a small
     * value and come back to check soon.
     */
    if ((tgtAddr == 0) ||
        ((void*)tgtAddr == dvmCompilerGetInterpretTemplate())) {
        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: predicted chain %p to method %s%s delayed",
                  cell, method->clazz->descriptor, method->name));
        goto done;
    }

    PredictedChainingCell newCell;

    if (cell->clazz == NULL) {
        newRechainCount = self->icRechainCount;
    }

    int relOffset = (int) tgtAddr - (int)cell;
    OpndSize immSize = estOpndSizeFromImm(relOffset);
    int jumpSize = getJmpCallInstSize(immSize, JmpCall_uncond);
    relOffset -= jumpSize;
    COMPILER_TRACE_CHAINING(
            ALOGI("inlineCachePatchEnqueue chain %p to method %s%s inst size %d",
                  cell, method->clazz->descriptor, method->name, jumpSize));

    // This does not need to go through lowering interface and can encode directly
    // at address because it does not actually update code stream until safe point.
    // Can't use stream here since it is used by the compilation thread.
    encoder_imm(Mnemonic_JMP, immSize, relOffset, (char*) (&newCell)); //update newCell.branch

    newCell.clazz = clazz;
    newCell.method = method;

    /*
     * Enter the work order to the queue and the chaining cell will be patched
     * the next time a safe point is entered.
     *
     * If the enqueuing fails reset the rechain count to a normal value so that
     * it won't get indefinitely delayed.
     */
    inlineCachePatchEnqueue(cell, &newCell);
    }
done:
    self->icRechainCount = newRechainCount;
    return method;
}

/*
 * Unchain a trace given the starting address of the translation
 * in the code cache.  Refer to the diagram in dvmCompilerAssembleLIR.
 * For ARM, it returns the address following the last cell unchained.
 * For IA, it returns NULL since cacheflush is not required for IA.
 */
u4* dvmJitUnchain(void* codeAddr)
{
    /* codeAddr is 4-byte aligned, so is chain cell count offset */
    u2* pChainCellCountOffset = (u2*)((char*)codeAddr - 4);
    u2 chainCellCountOffset = *pChainCellCountOffset;
    /* chain cell counts information is 4-byte aligned */
    ChainCellCounts *pChainCellCounts =
          (ChainCellCounts*)((char*)codeAddr + chainCellCountOffset);
    u2* pChainCellOffset = (u2*)((char*)codeAddr - 2);
    u2 chainCellOffset = *pChainCellOffset;
    u1* pChainCells;
    int i,j;
    PredictedChainingCell *predChainCell;
    int padding;

    /* Locate the beginning of the chain cell region */
    pChainCells = (u1 *)((char*)codeAddr + chainCellOffset);

    /* The cells are sorted in order - walk through them and reset */
    for (i = 0; i < kChainingCellGap; i++) {
        /* for hot, normal, singleton chaining:
               nop  //padding.
               jmp 0
               mov imm32, reg1
               mov imm32, reg2
               call reg2
           after chaining:
               nop
               jmp imm
               mov imm32, reg1
               mov imm32, reg2
               call reg2
           after unchaining:
               nop
               jmp 0
               mov imm32, reg1
               mov imm32, reg2
               call reg2
           Space occupied by the chaining cell in bytes: nop is for padding,
                jump 0, the target 0 is 4 bytes aligned.
           Space for predicted chaining: 5 words = 20 bytes
        */
        int elemSize = 0;
        if (i == kChainingCellInvokePredicted) {
            elemSize = 20;
        }
        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: unchaining type %d count %d", i, pChainCellCounts->u.count[i]));

        for (j = 0; j < pChainCellCounts->u.count[i]; j++) {
            switch(i) {
                case kChainingCellNormal:
                case kChainingCellHot:
                case kChainingCellInvokeSingleton:
                case kChainingCellBackwardBranch:
                    COMPILER_TRACE_CHAINING(
                        ALOGI("Jit Runtime: unchaining of normal, hot, or singleton"));
                    pChainCells = (u1*) (((uint)pChainCells + 4)&(~0x03));
                    elemSize = 4+5+5+2;
                    memset(pChainCells, 0, 4);
                    break;
                case kChainingCellInvokePredicted:
                    COMPILER_TRACE_CHAINING(
                        ALOGI("Jit Runtime: unchaining of predicted"));
                    /* 4-byte aligned */
                    padding = (4 - ((u4)pChainCells & 3)) & 3;
                    pChainCells += padding;
                    predChainCell = (PredictedChainingCell *) pChainCells;
                    /*
                     * There could be a race on another mutator thread to use
                     * this particular predicted cell and the check has passed
                     * the clazz comparison. So we cannot safely wipe the
                     * method and branch but it is safe to clear the clazz,
                     * which serves as the key.
                     */
                    predChainCell->clazz = PREDICTED_CHAIN_CLAZZ_INIT;
                    break;
                default:
                    ALOGE("JIT_ERROR: Unexpected chaining type: %d", i);
                    //Error is beyond the scope of the x86 JIT back-end
                    ALOGE("\t FATAL ERROR. ABORTING!");
                    dvmAbort();  // dvmAbort OK here - can't safely recover
            }
            COMPILER_TRACE_CHAINING(
                ALOGI("Jit Runtime: unchaining 0x%x", (int)pChainCells));
            pChainCells += elemSize;  /* Advance by a fixed number of bytes */
        }
    }
    return NULL;
}

/* Unchain all translation in the cache. */
void dvmJitUnchainAll()
{
    ALOGV("Jit Runtime: unchaining all");
    if (gDvmJit.pJitEntryTable != NULL) {
        COMPILER_TRACE_CHAINING(ALOGI("Jit Runtime: unchaining all"));
        dvmLockMutex(&gDvmJit.tableLock);

        UNPROTECT_CODE_CACHE(gDvmJit.codeCache, gDvmJit.codeCacheByteUsed);

        for (size_t i = 0; i < gDvmJit.jitTableSize; i++) {
            if (gDvmJit.pJitEntryTable[i].dPC &&
                !gDvmJit.pJitEntryTable[i].u.info.isMethodEntry &&
                gDvmJit.pJitEntryTable[i].codeAddress) {
                      dvmJitUnchain(gDvmJit.pJitEntryTable[i].codeAddress);
            }
        }

        PROTECT_CODE_CACHE(gDvmJit.codeCache, gDvmJit.codeCacheByteUsed);

        dvmUnlockMutex(&gDvmJit.tableLock);
        gDvmJit.translationChains = 0;
    }
    gDvmJit.hasNewChain = false;
}

#define P_GPR_1 PhysicalReg_EBX
/* Add an additional jump instruction, keeping jump target 4 bytes aligned. Returns the amount of nop padding used before chaining cell head*/
static int insertJumpHelp()
{
    int rem = (uint)stream % 4;
    int nop_size = 3 - rem;
    dump_nop(nop_size);
    unconditional_jump_int(0, OpndSize_32);
    return nop_size;
}

/* Chaining cell for code that may need warmup. */
/* ARM assembly: ldr r0, [r6, #76] (why a single instruction to access member of glue structure?)
                 blx r0
                 data 0xb23a //bytecode address: 0x5115b23a
                 data 0x5115
   IA32 assembly:
                  jmp  0 //5 bytes
                  movl address, %ebx
                  movl dvmJitToInterpNormal, %eax
                  call %eax
                  <-- return address
*/
static int handleNormalChainingCell(CompilationUnit *cUnit,
                                     unsigned int offset, int blockId, LowOpBlockLabel* labelList)
{
    ALOGV("In handleNormalChainingCell for method %s block %d BC offset %x NCG offset %x",
          cUnit->method->name, blockId, offset, stream - streamMethodStart);
    if(dump_x86_inst)
        ALOGI("LOWER NormalChainingCell at offsetPC %x offsetNCG %x @%p",
              offset, stream - streamMethodStart, stream);
    /* Add one additional "jump 0" instruction, it may be modified during jit chaining. This helps
     * reslove the multithreading issue.
     */
    int nop_size = insertJumpHelp();
    move_imm_to_reg(OpndSize_32, (int) (cUnit->method->insns + offset), P_GPR_1, true);
    scratchRegs[0] = PhysicalReg_EAX;
    call_dvmJitToInterpNormal();
    //move_imm_to_reg(OpndSize_32, (int) (cUnit->method->insns + offset), P_GPR_1, true); /* used when unchaining */
    return nop_size;
}

/*
 * Chaining cell for instructions that immediately following already translated
 * code.
 */
static int handleHotChainingCell(CompilationUnit *cUnit,
                                  unsigned int offset, int blockId, LowOpBlockLabel* labelList)
{
    ALOGV("In handleHotChainingCell for method %s block %d BC offset %x NCG offset %x",
          cUnit->method->name, blockId, offset, stream - streamMethodStart);
    if(dump_x86_inst)
        ALOGI("LOWER HotChainingCell at offsetPC %x offsetNCG %x @%p",
              offset, stream - streamMethodStart, stream);
    /* Add one additional "jump 0" instruction, it may be modified during jit chaining. This helps
     * reslove the multithreading issue.
     */
    int nop_size = insertJumpHelp();
    move_imm_to_reg(OpndSize_32, (int) (cUnit->method->insns + offset), P_GPR_1, true);
    scratchRegs[0] = PhysicalReg_EAX;
    call_dvmJitToInterpTraceSelect();
    //move_imm_to_reg(OpndSize_32, (int) (cUnit->method->insns + offset), P_GPR_1, true); /* used when unchaining */
    return nop_size;
}

/* Chaining cell for branches that branch back into the same basic block */
static int handleBackwardBranchChainingCell(CompilationUnit *cUnit,
                                     unsigned int offset, int blockId, LowOpBlockLabel* labelList)
{
    ALOGV("In handleBackwardBranchChainingCell for method %s block %d BC offset %x NCG offset %x",
          cUnit->method->name, blockId, offset, stream - streamMethodStart);
    if(dump_x86_inst)
        ALOGI("LOWER BackwardBranchChainingCell at offsetPC %x offsetNCG %x @%p",
              offset, stream - streamMethodStart, stream);
    /* Add one additional "jump 0" instruction, it may be modified during jit chaining. This helps
     * reslove the multithreading issue.
     */
    int nop_size = insertJumpHelp();
    move_imm_to_reg(OpndSize_32, (int) (cUnit->method->insns + offset), P_GPR_1, true);
    scratchRegs[0] = PhysicalReg_EAX;
    call_dvmJitToInterpNormal();
    //move_imm_to_reg(OpndSize_32, (int) (cUnit->method->insns + offset), P_GPR_1, true); /* used when unchaining */
    return nop_size;
}

/* Chaining cell for monomorphic method invocations. */
static int handleInvokeSingletonChainingCell(CompilationUnit *cUnit,
                                              const Method *callee, int blockId, LowOpBlockLabel* labelList)
{
    ALOGV("In handleInvokeSingletonChainingCell for method %s block %d callee %s NCG offset %x",
          cUnit->method->name, blockId, callee->name, stream - streamMethodStart);
    if(dump_x86_inst)
        ALOGI("LOWER InvokeSingletonChainingCell at block %d offsetNCG %x @%p",
              blockId, stream - streamMethodStart, stream);
    /* Add one additional "jump 0" instruction, it may be modified during jit chaining. This helps
     * reslove the multithreading issue.
     */
    int nop_size = insertJumpHelp();
    move_imm_to_reg(OpndSize_32, (int) (callee->insns), P_GPR_1, true);
    scratchRegs[0] = PhysicalReg_EAX;
    call_dvmJitToInterpTraceSelect();
    //move_imm_to_reg(OpndSize_32, (int) (callee->insns), P_GPR_1, true); /* used when unchaining */
    return nop_size;
}
#undef P_GPR_1

/* Chaining cell for monomorphic method invocations. */
static void handleInvokePredictedChainingCell(CompilationUnit *cUnit, int blockId)
{
    if(dump_x86_inst)
        ALOGI("LOWER InvokePredictedChainingCell at block %d offsetNCG %x @%p",
              blockId, stream - streamMethodStart, stream);
#ifndef PREDICTED_CHAINING
    //assume rPC for callee->insns in %ebx
    scratchRegs[0] = PhysicalReg_EAX;
#if defined(WITH_JIT_TUNING)
    /* Predicted chaining is not enabled. Fall back to interpreter and
     * indicate that predicted chaining was not done.
     */
    move_imm_to_reg(OpndSize_32, kInlineCacheMiss, PhysicalReg_EDX, true);
#endif
    call_dvmJitToInterpTraceSelectNoChain();
#else
    /* make sure section for predicited chaining cell is 4-byte aligned */
    //int padding = (4 - ((u4)stream & 3)) & 3;
    //stream += padding;
    int* streamData = (int*)stream;
    /* Should not be executed in the initial state */
    streamData[0] = PREDICTED_CHAIN_BX_PAIR_INIT;
    streamData[1] = 0;
    /* To be filled: class */
    streamData[2] = PREDICTED_CHAIN_CLAZZ_INIT;
    /* To be filled: method */
    streamData[3] = PREDICTED_CHAIN_METHOD_INIT;
    /*
     * Rechain count. The initial value of 0 here will trigger chaining upon
     * the first invocation of this callsite.
     */
    streamData[4] = PREDICTED_CHAIN_COUNTER_INIT;
#if 0
    ALOGI("--- DATA @ %p: %x %x %x %x", stream, *((int*)stream), *((int*)(stream+4)),
          *((int*)(stream+8)), *((int*)(stream+12)));
#endif
    stream += 20; //5 *4
#endif
}

/* Extended MIR instructions like PHI */
void handleExtendedMIR(CompilationUnit *cUnit, MIR *mir)
{
    ExecutionMode origMode = gDvm.executionMode;
    gDvm.executionMode = kExecutionModeNcgO0;
    switch ((ExtendedMIROpcode)mir->dalvikInsn.opcode) {
        case kMirOpPhi: {
            break;
        }
        case kMirOpNullCheck: {
            genHoistedNullCheck (cUnit, mir);
            break;
        }
        case kMirOpBoundCheck: {
            genHoistedBoundCheck (cUnit, mir);
            break;
        }
        case kMirOpNullNRangeUpCheck: {
            genHoistedChecksForCountUpLoop(cUnit, mir);
            break;
        }
        case kMirOpNullNRangeDownCheck: {
            genHoistedChecksForCountDownLoop(cUnit, mir);
            break;
        }
        case kMirOpLowerBound: {
            genHoistedLowerBoundCheck(cUnit, mir);
            break;
        }
        case kMirOpPunt: {
            break;
        }
#ifdef WITH_JIT_INLINING_PHASE2
        case kMirOpCheckInlinePrediction: { //handled in ncg_o1_data.c
            genValidationForPredictedInline(cUnit, mir);
            break;
        }
#endif
        default:
            break;
    }
    gDvm.executionMode = origMode;
}

static int genTraceProfileEntry(CompilationUnit *cUnit)
{
    cUnit->headerSize = 6;
    if ((gDvmJit.profileMode == kTraceProfilingContinuous) ||
        (gDvmJit.profileMode == kTraceProfilingDisabled)) {
        return 12;
    } else {
        return 4;
    }

}

#define PRINT_BUFFER_LEN 1024
/* Print the code block in code cache in the range of [startAddr, endAddr)
 * in readable format.
 */
void printEmittedCodeBlock(unsigned char *startAddr, unsigned char *endAddr)
{
    char strbuf[PRINT_BUFFER_LEN];
    unsigned char *addr;
    unsigned char *next_addr;
    int n;

    if (gDvmJit.printBinary) {
        // print binary in bytes
        n = 0;
        for (addr = startAddr; addr < endAddr; addr++) {
            n += snprintf(&strbuf[n], PRINT_BUFFER_LEN-n, "0x%x, ", *addr);
            if (n > PRINT_BUFFER_LEN - 10) {
                ALOGD("## %s", strbuf);
                n = 0;
            }
        }
        if (n > 0)
            ALOGD("## %s", strbuf);
    }

    // print disassembled instructions
    addr = startAddr;
    while (addr < endAddr) {
        next_addr = reinterpret_cast<unsigned char*>
            (decoder_disassemble_instr(reinterpret_cast<char*>(addr),
                                       strbuf, PRINT_BUFFER_LEN));
        if (addr != next_addr) {
            ALOGD("**  %p: %s", addr, strbuf);
        } else {                // check whether this is nop padding
            if (addr[0] == 0x90) {
                ALOGD("**  %p: NOP (1 byte)", addr);
                next_addr += 1;
            } else if (addr[0] == 0x66 && addr[1] == 0x90) {
                ALOGD("**  %p: NOP (2 bytes)", addr);
                next_addr += 2;
            } else if (addr[0] == 0x0f && addr[1] == 0x1f && addr[2] == 0x00) {
                ALOGD("**  %p: NOP (3 bytes)", addr);
                next_addr += 3;
            } else {
                ALOGD("** unable to decode binary at %p", addr);
                break;
            }
        }
        addr = next_addr;
    }
}

// Return true if there are branch inside loop
bool hasBranchInLoop(CompilationUnit *cUnit)
{
    BasicBlock *firstBB = cUnit->entryBlock->fallThrough;
    if(firstBB->taken && firstBB->taken == cUnit->backChainBlock)
       return false;
    if(firstBB->fallThrough && firstBB->fallThrough == cUnit->backChainBlock)
       return false;
    return true;
}

/**
 * @brief Handle fallthrough branch: determine whether we need one or not
 * @param cUnit the CompilationUnit
 * @param bb the BasicBlock
 * @param ptrNextFallThrough pointer to the nextFallThrough if requested (can be 0)
 */
static void handleFallThroughBranch (CompilationUnit *cUnit, BasicBlock *bb, BasicBlock **ptrNextFallThrough)
{
    //Get next fall through
    BasicBlock *nextFallThrough = *ptrNextFallThrough;

    //We need a fallthrough branch if we had a next and it isn't the current BasicBlock
    bool needFallThroughBranch = (nextFallThrough != 0 && bb != nextFallThrough);

    if (needFallThroughBranch == true)
    {
        jumpToBasicBlock (stream, nextFallThrough->id);
    }
    //Clear it
    *ptrNextFallThrough = 0;
}

/**
 * @brief Generate the code for the BasicBlock
 * @param cUnit the CompilationUnit
 * @param bb the BasicBlock
 * @param nextFallThrough a pointer to the next fall through BasicBlock
 * @return whether the generation went well
 */
static bool generateCode (CompilationUnit *cUnit, BasicBlock *bb, BasicBlock **nextFallThrough)
{
    ALOGV("Get ready to handle JIT bb %d type %d hidden %d",
            bb->id, bb->blockType, bb->hidden);

    //If in O1, not the entry block, and actually have an instruction
    if(gDvm.executionMode == kExecutionModeNcgO1 &&
            bb->blockType != kEntryBlock &&
            bb->firstMIRInsn != NULL) {

        //Generate the code
        startOfBasicBlock(bb);
        int cg_ret = codeGenBasicBlockJit(cUnit->method, bb);
        endOfBasicBlock(bb);

        //Error handling, we return false
        if(cg_ret < 0) {
            ALOGI("Could not compile trace for %s%s, offset %d",
                    cUnit->method->clazz->descriptor, cUnit->method->name,
                    cUnit->traceDesc->trace[0].info.frag.startOffset);
            SET_JIT_ERROR(kJitErrorCodegen);
            endOfTrace(true/*freeOnly*/);
            cUnit->baseAddr = NULL;
            PROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);
            return false;
        }
    }
    else {
        //Not O1 or iti s the entry block
        for (MIR *mir = bb->firstMIRInsn; mir; mir = mir->next) {
            // Global variable rPC that's holding the Dalvik PC
            // needs to be updated here because we are iterating
            // through the MIRs of this BB.
            rPC = const_cast<u2 *>(cUnit->method->insns) + mir->offset;
            startOfBasicBlock(bb); //why here for O0
            Opcode dalvikOpCode = mir->dalvikInsn.opcode;

            //If extended, send it off and go to the next instruction
            if((int)dalvikOpCode >= (int)kMirOpFirst) {
                handleExtendedMIR(cUnit, mir);
                continue;
            }

            //A normal instruction is handled here
            InstructionFormat dalvikFormat = dexGetFormatFromOpcode(dalvikOpCode);
            ALOGV("ready to handle bytecode at offset %x: opcode %d format %d",
                    mir->offset, dalvikOpCode, dalvikFormat);

            // Before: A boundary LIR with Atom pseudo-mnemonic named
            //      ATOM_PSEUDO_DALVIK_BYTECODE_BOUNDARY was being created
            //      at this point. The allocation of the Atom LIR used to
            //      update the global variable named lowOpTimeStamp.
            // After: LIRs are now only allocated through the Instruction
            //      scheduling interface and LIRs with only pseudo-mnemonics
            //      are not supported. In order to keep semantics, the
            //      timestamp will be updated here manually since it affects
            //      register allocation.
            lowOpTimeStamp++;

            bool notHandled = true;
            /*
             * Debugging: screen the opcode first to see if it is in the
             * do[-not]-compile list
             */
            bool singleStepMe =
                gDvmJit.includeSelectedOp !=
                ((gDvmJit.opList[dalvikOpCode >> 3] &
                  (1 << (dalvikOpCode & 0x7))) !=
                 0);

            if (singleStepMe == false && cUnit->allSingleStep == false)
            {
                //lower each byte code, update LIR
                notHandled = lowerByteCodeJit(cUnit->method, mir, rPC);

                //Look if the code cache is full
                if(gDvmJit.codeCacheByteUsed + (stream - streamStart) +
                        CODE_CACHE_PADDING > gDvmJit.codeCacheSize) {
                    ALOGE("JIT_ERROR: Code cache full after lowerByteCodeJit (trace uses %uB)", (stream - streamStart));
                    SET_JIT_ERROR(kJitErrorCodeCacheFull);
                    gDvmJit.codeCacheFull = true;
                    cUnit->baseAddr = NULL;
                    endOfTrace(true/*freeOnly*/);
                    PROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);
                    return false;
                }
            }

            //If not handle, error flag setting and return false
            if (notHandled) {
                ALOGE("JIT_ERROR: Opcode 0x%x (%s) / Fmt %d at offset %#06x not handled\n",
                        dalvikOpCode, dexGetOpcodeName(dalvikOpCode), dalvikFormat, mir->offset);
                SET_JIT_ERROR(kJitErrorUnsupportedBytecode);
                cUnit->baseAddr = NULL;
                endOfTrace(true); /* need to free structures */
                return false;
            }
        } // end for
    } // end else //JIT + O0 code generator

    //Ok we are going to skip this if the last instruction is an if
    //This could be skipped if the if was not automatically generating the fallthrough jump
    MIR *lastInsn = bb->lastMIRInsn;
    bool shouldRegister = (lastInsn == 0);

    if (lastInsn != 0)
    {
        Opcode opcode = lastInsn->dalvikInsn.opcode;
        //We don't care about instructions that can branch in general
        shouldRegister = ((dexGetFlagsFromOpcode(opcode) & kInstrCanBranch) == 0);
    }

    //If need be, register it: handleFallThroughBranch will do the rest
    if (shouldRegister == true)
    {
        *nextFallThrough = bb->fallThrough;
    }
    else
    {
        //Otherwise, reset nextFallThrough
        *nextFallThrough = 0;
    }

    //Everything went fine
    return true;
}

/* 4 is the number of additional bytes needed for chaining information for trace:
 * 2 bytes for chaining cell count offset and 2 bytes for chaining cell offset */
#define EXTRA_BYTES_FOR_CHAINING 4

//! \brief Lower middle-level IR ro low-level IR
//!
//! \details Entry function to invoke the backend of the JIT compiler
//!
//! \param cUnit: The current compilation unit
//! \param info: JitTranslationInfo. Holds generated code address on success
static void compilerMIR2LIRJit(CompilationUnit *cUnit, JitTranslationInfo *info)
{
    //Used to determine whether we need a fallthrough jump
    BasicBlock *nextFallThrough = 0;

    dump_x86_inst = cUnit->printMe;

    /* Used to hold the labels of each block */
    LowOpBlockLabel *labelList =
        (LowOpBlockLabel *)dvmCompilerNew(sizeof(LowOpBlockLabel) * cUnit->numBlocks, true); //Utility.c
    GrowableList chainingListByType[kChainingCellLast];
    unsigned int i, padding;

    traceMode = cUnit->jitMode;

    /*
     * Initialize various types chaining lists.
     */
    for (i = 0; i < kChainingCellLast; i++) {
        dvmInitGrowableList(&chainingListByType[i], 2);
    }

    GrowableListIterator iterator;
    dvmGrowableListIteratorInit(&cUnit->blockList, &iterator);

    /* Traces start with a profiling entry point.  Generate it here */
    cUnit->profileCodeSize = genTraceProfileEntry(cUnit);

    //BasicBlock **blockList = cUnit->blockList;
    GrowableList *blockList = &cUnit->blockList;
    BasicBlock *bb;

    info->codeAddress = NULL;
    stream = (char*)gDvmJit.codeCache + gDvmJit.codeCacheByteUsed;

    // TODO: compile into a temporary buffer and then copy into the code cache.
    // That would let us leave the code cache unprotected for a shorter time.
    size_t unprotected_code_cache_bytes =
            gDvmJit.codeCacheSize - gDvmJit.codeCacheByteUsed - CODE_CACHE_PADDING;
    UNPROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);

    streamStart = stream; /* trace start before alignment */
    stream += EXTRA_BYTES_FOR_CHAINING; /* This is needed for chaining. Add the bytes before the alignment */
    stream = (char*)(((unsigned int)stream + 0xF) & ~0xF); /* Align trace to 16-bytes */
    streamMethodStart = stream; /* code start */
    for (i = 0; i < ((unsigned int) cUnit->numBlocks); i++) {
        labelList[i].lop.generic.offset = -1;
    }
    cUnit->exceptionBlockId = -1;
    for (i = 0; i < blockList->numUsed; i++) {
        bb = (BasicBlock *) blockList->elemList[i];
        if(bb->blockType == kExceptionHandling)
            cUnit->exceptionBlockId = i;
    }
    startOfTrace(cUnit->method, labelList, cUnit->exceptionBlockId, cUnit);
    if(gDvm.executionMode == kExecutionModeNcgO1) {
        for (i = 0; i < blockList->numUsed; i++) {
            bb = (BasicBlock *) blockList->elemList[i];
            if(bb->blockType == kDalvikByteCode &&
               bb->firstMIRInsn != NULL) {
                int retCode = preprocessingBB(bb);
                if (retCode < 0) {
                    endOfTrace(true/*freeOnly*/);
                    cUnit->baseAddr = NULL;
                    SET_JIT_ERROR(kJitErrorCodegen);
                    return;
                }
            }
        }
        if (preprocessingTrace() == -1) {
            endOfTrace(true/*freeOnly*/);
            cUnit->baseAddr = NULL;
            SET_JIT_ERROR(kJitErrorCodegen);
            return;
        }
    }

    branchInLoop = cUnit->jitMode == kJitLoop && hasBranchInLoop(cUnit);

    /* Handle the content in each basic block */
    for (bb = (BasicBlock *) (dvmGrowableListIteratorNext (&iterator)),
         i = 0;
         //We stop when bb is 0
         bb != 0;
         //Induction variables: bb goes to next iterator, i is incremented
         bb = (BasicBlock *) (dvmGrowableListIteratorNext (&iterator)),
         i++) {

        //Set label information
        labelList[i].immOpnd.value = bb->startOffset;

        if (bb->blockType >= kChainingCellLast) {
            /*
             * Append the label pseudo LIR first. Chaining cells will be handled
             * separately afterwards.
             */
            dvmCompilerAppendLIR(cUnit, (LIR *) &labelList[i]);
        }

        //Switch depending on the BasicBlock type
        switch (bb->blockType)
        {
            case kEntryBlock:
                //First handle fallthrough branch
                handleFallThroughBranch (cUnit, bb, &nextFallThrough);

                labelList[i].lop.opCode2 = ATOM_PSEUDO_ENTRY_BLOCK;

                //Set label offset
                labelList[i].lop.generic.offset = (stream - streamMethodStart);

                if (generateCode (cUnit, bb, &nextFallThrough) == false)
                {
                    //Generate code set an error for the jit, we can just return
                    return;
                }
                break;
            case kExitBlock:
                //Only do the handle through if there is an instruction in the exit block
                if (bb->firstMIRInsn != 0)
                {
                    //First handle fallthrough branch
                    handleFallThroughBranch (cUnit, bb, &nextFallThrough);
                }

                labelList[i].lop.opCode2 = ATOM_PSEUDO_EXIT_BLOCK;
                //Set label offset
                labelList[i].lop.generic.offset = (stream - streamMethodStart);

                if (generateCode (cUnit, bb, &nextFallThrough) == false)
                {
                    //Generate code set an error for the jit, we can just return
                    return;
                }
                break;
            case kDalvikByteCode:
                //If hidden, we don't generate code
                if (bb->hidden == false)
                {
                    //First handle fallthrough branch
                    handleFallThroughBranch (cUnit, bb, &nextFallThrough);

                    labelList[i].lop.opCode2 = ATOM_PSEUDO_NORMAL_BLOCK_LABEL;
                    //Set label offset
                    labelList[i].lop.generic.offset = (stream - streamMethodStart);

                    if (generateCode (cUnit, bb, &nextFallThrough) == false)
                    {
                        //Generate code set an error for the jit, we can just return
                        return;
                    }
                }
                break;
            case kChainingCellNormal:
                labelList[i].lop.opCode2 = ATOM_PSEUDO_CHAINING_CELL_NORMAL;
                /* Handle the codegen later */
                dvmInsertGrowableList(&chainingListByType[kChainingCellNormal], i);
                break;
            case kChainingCellInvokeSingleton:
                labelList[i].lop.opCode2 = ATOM_PSEUDO_CHAINING_CELL_INVOKE_SINGLETON;
                labelList[i].immOpnd.value = (int) bb->containingMethod;
                /* Handle the codegen later */
                dvmInsertGrowableList(
                        &chainingListByType[kChainingCellInvokeSingleton], i);
                break;
            case kChainingCellInvokePredicted:
                labelList[i].lop.opCode2 = ATOM_PSEUDO_CHAINING_CELL_INVOKE_PREDICTED;
                /* Handle the codegen later */
                dvmInsertGrowableList(&chainingListByType[kChainingCellInvokePredicted], i);
                break;
            case kChainingCellHot:
                labelList[i].lop.opCode2 = ATOM_PSEUDO_CHAINING_CELL_HOT;
                /* Handle the codegen later */
                dvmInsertGrowableList(&chainingListByType[kChainingCellHot], i);
                break;
            case kExceptionHandling:
                //First handle fallthrough branch
                handleFallThroughBranch (cUnit, bb, &nextFallThrough);
                labelList[i].lop.opCode2 = ATOM_PSEUDO_EH_BLOCK_LABEL;
                labelList[i].lop.generic.offset = (stream - streamMethodStart);
                scratchRegs[0] = PhysicalReg_EAX;
                jumpToInterpPunt();
                break;
            case kChainingCellBackwardBranch:
                labelList[i].lop.opCode2 = ATOM_PSEUDO_CHAINING_CELL_BACKWARD_BRANCH;
                /* Handle the codegen later */
                dvmInsertGrowableList(&chainingListByType[kChainingCellBackwardBranch], i);
                break;
            default:
                break;
            }
        }

    char* streamChainingStart = (char*)stream;
    /* Handle the chaining cells in predefined order */
    for (i = 0; i < kChainingCellGap; i++) {
        size_t j;
        int *blockIdList = (int *) chainingListByType[i].elemList;

        cUnit->numChainingCells[i] = chainingListByType[i].numUsed;

        /* No chaining cells of this type */
        if (cUnit->numChainingCells[i] == 0)
            continue;

        //First handle fallthrough branch
        handleFallThroughBranch (cUnit, 0, &nextFallThrough);

        /* Record the first LIR for a new type of chaining cell */
        cUnit->firstChainingLIR[i] = (LIR *) &labelList[blockIdList[0]];
        for (j = 0; j < chainingListByType[i].numUsed; j++) {
            int blockId = blockIdList[j];
            BasicBlock *chainingBlock =
                (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList,
                                                         blockId);

            labelList[blockId].lop.generic.offset = (stream - streamMethodStart);

            /* Insert the pseudo chaining instruction */
            dvmCompilerAppendLIR(cUnit, (LIR *) &labelList[blockId]);

            int nop_size;
            switch (chainingBlock->blockType) {
                case kChainingCellNormal:
                    nop_size = handleNormalChainingCell(cUnit,
                     chainingBlock->startOffset, blockId, labelList);
                    labelList[blockId].lop.generic.offset += nop_size; //skip over nop
                    break;
                case kChainingCellInvokeSingleton:
                    nop_size = handleInvokeSingletonChainingCell(cUnit,
                        chainingBlock->containingMethod, blockId, labelList);
                    labelList[blockId].lop.generic.offset += nop_size; //skip over nop
                    break;
                case kChainingCellInvokePredicted:
                    handleInvokePredictedChainingCell(cUnit, blockId);
                    break;
                case kChainingCellHot:
                    nop_size = handleHotChainingCell(cUnit,
                        chainingBlock->startOffset, blockId, labelList);
                    labelList[blockId].lop.generic.offset += nop_size; //skip over nop
                    break;
                case kChainingCellBackwardBranch:
                    nop_size = handleBackwardBranchChainingCell(cUnit,
                        chainingBlock->startOffset, blockId, labelList);
                    labelList[blockId].lop.generic.offset += nop_size; //skip over nop
                    break;
                default:
                    ALOGE("JIT_ERROR: Bad blocktype %d", chainingBlock->blockType);
                    SET_JIT_ERROR(kJitErrorTraceFormation);
                    cUnit->baseAddr = NULL;
                    endOfTrace(true); /* need to free structures */
                    return;
            }

            if (gDvmJit.codeCacheByteUsed + (stream - streamStart) + CODE_CACHE_PADDING > gDvmJit.codeCacheSize) {
                ALOGE("JIT_ERROR: Code cache full after ChainingCell (trace uses %uB)", (stream - streamStart));
                SET_JIT_ERROR(kJitErrorCodeCacheFull);
                gDvmJit.codeCacheFull = true;
                cUnit->baseAddr = NULL;
                endOfTrace(true); /* need to free structures */
                PROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);
                return;
            }
        }
    }

    // Now that we finished handling all of the MIR BBs, we can dump all exception handling
    // restore state to the code stream
    singletonPtr<ExceptionHandlingRestoreState>()->dumpAllExceptionHandlingRestoreState();

    //In case, handle fallthrough branch
    handleFallThroughBranch (cUnit, 0, &nextFallThrough);

    endOfTrace(false);

    if (gDvmJit.codeCacheFull) {
        // We hit code cache size limit either after dumping exception handling
        // state or after calling endOfTrace. Bail out for this trace!
        ALOGE("JIT_ERROR: Code cache full after endOfTrace (trace uses %uB)", (stream - streamStart));
        SET_JIT_ERROR(kJitErrorCodeCacheFull);
        cUnit->baseAddr = NULL;
        PROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);
        return;
    }

    /* dump section for chaining cell counts, make sure it is 4-byte aligned */
    padding = (4 - ((u4)stream & 3)) & 3;
    stream += padding;
    ChainCellCounts chainCellCounts;
    /* Install the chaining cell counts */
    for (i=0; i< kChainingCellGap; i++) {
        chainCellCounts.u.count[i] = cUnit->numChainingCells[i];
    }
    char* streamCountStart = (char*)stream;
    memcpy((char*)stream, &chainCellCounts, sizeof(chainCellCounts));
    stream += sizeof(chainCellCounts);

    cUnit->baseAddr = streamMethodStart;
    cUnit->totalSize = (stream - streamStart);
    if(gDvmJit.codeCacheByteUsed + cUnit->totalSize + CODE_CACHE_PADDING > gDvmJit.codeCacheSize) {
        ALOGE("JIT_ERROR: Code cache full after ChainingCellCounts (trace uses %uB)", (stream - streamStart));
        SET_JIT_ERROR(kJitErrorCodeCacheFull);
        gDvmJit.codeCacheFull = true;
        cUnit->baseAddr = NULL;
        PROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);
        return;
    }

    /* write chaining cell count offset & chaining cell offset */
    u2* pOffset = (u2*)(streamMethodStart - EXTRA_BYTES_FOR_CHAINING); /* space was already allocated for this purpose */
    *pOffset = streamCountStart - streamMethodStart; /* from codeAddr */
    pOffset[1] = streamChainingStart - streamMethodStart;

    PROTECT_CODE_CACHE(stream, unprotected_code_cache_bytes);

    gDvmJit.codeCacheByteUsed += (stream - streamStart);
    if (cUnit->printMe) {
        unsigned char* codeBaseAddr = (unsigned char *) cUnit->baseAddr;
        unsigned char* codeBaseAddrNext = ((unsigned char *) gDvmJit.codeCache) + gDvmJit.codeCacheByteUsed;
        ALOGD("-------- Built trace for %s%s, JIT code [%p, %p) cache start %p",
              cUnit->method->clazz->descriptor, cUnit->method->name,
              codeBaseAddr, codeBaseAddrNext, gDvmJit.codeCache);
        ALOGD("** %s%s@0x%x:", cUnit->method->clazz->descriptor,
              cUnit->method->name, cUnit->traceDesc->trace[0].info.frag.startOffset);
        printEmittedCodeBlock(codeBaseAddr, codeBaseAddrNext);
    }
    ALOGV("JIT CODE after trace %p to %p size %x START %p", cUnit->baseAddr,
          (char *) gDvmJit.codeCache + gDvmJit.codeCacheByteUsed,
          cUnit->totalSize, gDvmJit.codeCache);

    gDvmJit.numCompilations++;

    info->codeAddress = (char*)cUnit->baseAddr;// + cUnit->headerSize;
}

//! \brief Helper function to call compilerMIR2LIRJit
//!
//! \details Calls dvmCompilerMIR2LIRJit, checks for errors
//! and retries if possible.
//!
//! \param cUnit: The current compilation unit
//! \param info: JitTranslationInfo.
void dvmCompilerMIR2LIR(CompilationUnit *cUnit, JitTranslationInfo *info) {
   //Start the counter
   int numTries = 0;

   //Try to lower MIR
    do {
        //See if we have been here too many times:
        if (numTries > MAX_RETRIES) {
            ALOGI("Too many retries while compiling trace  %s%s, offset %d", cUnit->method->clazz->descriptor,
                cUnit->method->name, cUnit->traceDesc->trace[0].info.frag.startOffset);
            ALOGI("Rejecting Trace");
            return;
        }

        //Ignore errors in previous compilations
        CLEAR_ALL_JIT_ERRORS();

        //Do the trace compilation
        numTries++;
        compilerMIR2LIRJit(cUnit, info);

        //Once done, see if errors happened, and if so
        //see if we can retry and come back
    } while (IS_ANY_JIT_ERROR_SET() && dvmCanFixErrorsAndRetry(cUnit));
}


/*
 * Perform translation chain operation.
 */
void* dvmJitChain(void* tgtAddr, u4* branchAddr)
{
#ifdef JIT_CHAIN
    int relOffset = (int) tgtAddr - (int)branchAddr;

    if ((gDvmJit.pProfTable != NULL) && (gDvm.sumThreadSuspendCount == 0) &&
        (gDvmJit.codeCacheFull == false)) {

        gDvmJit.translationChains++;

        //OpndSize immSize = estOpndSizeFromImm(relOffset);
        //relOffset -= getJmpCallInstSize(immSize, JmpCall_uncond);
        /* Hard coded the jump opnd size to 32 bits, This instruction will replace the "jump 0" in
         * the original code sequence.
         */
        relOffset -= 5;
        //can't use stream here since it is used by the compilation thread
        UNPROTECT_CODE_CACHE(branchAddr, sizeof(*branchAddr));
        dump_imm_update(relOffset, (char*) branchAddr, false); // An update is done instead of an encode
                                                                // because the Jmp instruction is already
                                                                // part of chaining cell.
        PROTECT_CODE_CACHE(branchAddr, sizeof(*branchAddr));

        gDvmJit.hasNewChain = true;

        COMPILER_TRACE_CHAINING(
            ALOGI("Jit Runtime: chaining 0x%x to %p with relOffset %x",
                  (int) branchAddr, tgtAddr, relOffset));
    }
#endif
    return tgtAddr;
}

/*
 * Accept the work and start compiling.  Returns true if compilation
 * is attempted.
 */
bool dvmCompilerDoWork(CompilerWorkOrder *work)
{
    JitTraceDescription *desc;
    bool isCompile;
    bool success = true;

    if (gDvmJit.codeCacheFull) {
        return false;
    }

    switch (work->kind) {
        case kWorkOrderTrace:
            isCompile = true;
            /* Start compilation with maximally allowed trace length */
            desc = (JitTraceDescription *)work->info;
            success = dvmCompileTrace(desc, JIT_MAX_TRACE_LEN, &work->result,
                                        work->bailPtr, 0 /* no hints */);
            break;
        case kWorkOrderTraceDebug: {
            bool oldPrintMe = gDvmJit.printMe;
            gDvmJit.printMe = true;
            isCompile = true;
            /* Start compilation with maximally allowed trace length */
            desc = (JitTraceDescription *)work->info;
            success = dvmCompileTrace(desc, JIT_MAX_TRACE_LEN, &work->result,
                                        work->bailPtr, 0 /* no hints */);
            gDvmJit.printMe = oldPrintMe;
            break;
        }
        case kWorkOrderProfileMode:
            dvmJitChangeProfileMode((TraceProfilingModes)(int)work->info);
            isCompile = false;
            break;
        default:
            isCompile = false;
            ALOGE("JIT_ERROR: Unknown work order type");
            assert(0);  // Bail if debug build, discard otherwise
            ALOGE("\tError ignored");
    }
    if (!success)
        work->result.codeAddress = NULL;
    return isCompile;
}

void dvmCompilerCacheFlush(long start, long end, long flags) {
  /* cacheflush is needed for ARM, but not for IA32 (coherent icache) */
}

//#endif

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
 * Main interpreter entry point and support functions.
 *
 * The entry point selects the "standard" or "debug" interpreter and
 * facilitates switching between them.  The standard interpreter may
 * use the "fast" or "portable" implementation.
 *
 * Some debugger support functions are included here.
 */
#include "Dalvik.h"
#include "interp/InterpDefs.h"


/*
 * ===========================================================================
 *      Debugger support
 * ===========================================================================
 */

// fwd
static BreakpointSet* dvmBreakpointSetAlloc(void);
static void dvmBreakpointSetFree(BreakpointSet* pSet);

/*
 * Initialize global breakpoint structures.
 */
bool dvmBreakpointStartup(void)
{
    gDvm.breakpointSet = dvmBreakpointSetAlloc();
    return (gDvm.breakpointSet != NULL);
}

/*
 * Free resources.
 */
void dvmBreakpointShutdown(void)
{
    dvmBreakpointSetFree(gDvm.breakpointSet);
}


/*
 * This represents a breakpoint inserted in the instruction stream.
 *
 * The debugger may ask us to create the same breakpoint multiple times.
 * We only remove the breakpoint when the last instance is cleared.
 */
typedef struct {
    Method*     method;                 /* method we're associated with */
    u2*         addr;                   /* absolute memory address */
    u1          originalOpCode;         /* original 8-bit opcode value */
    int         setCount;               /* #of times this breakpoint was set */
} Breakpoint;

/*
 * Set of breakpoints.
 */
struct BreakpointSet {
    /* grab lock before reading or writing anything else in here */
    pthread_mutex_t lock;

    /* vector of breakpoint structures */
    int         alloc;
    int         count;
    Breakpoint* breakpoints;
};

/*
 * Initialize a BreakpointSet.  Initially empty.
 */
static BreakpointSet* dvmBreakpointSetAlloc(void)
{
    BreakpointSet* pSet = (BreakpointSet*) calloc(1, sizeof(*pSet));

    dvmInitMutex(&pSet->lock);
    /* leave the rest zeroed -- will alloc on first use */

    return pSet;
}

/*
 * Free storage associated with a BreakpointSet.
 */
static void dvmBreakpointSetFree(BreakpointSet* pSet)
{
    if (pSet == NULL)
        return;

    free(pSet->breakpoints);
    free(pSet);
}

/*
 * Lock the breakpoint set.
 *
 * It's not currently necessary to switch to VMWAIT in the event of
 * contention, because nothing in here can block.  However, it's possible
 * that the bytecode-updater code could become fancier in the future, so
 * we do the trylock dance as a bit of future-proofing.
 */
static void dvmBreakpointSetLock(BreakpointSet* pSet)
{
    if (dvmTryLockMutex(&pSet->lock) != 0) {
        Thread* self = dvmThreadSelf();
        ThreadStatus oldStatus = dvmChangeStatus(self, THREAD_VMWAIT);
        dvmLockMutex(&pSet->lock);
        dvmChangeStatus(self, oldStatus);
    }
}

/*
 * Unlock the breakpoint set.
 */
static void dvmBreakpointSetUnlock(BreakpointSet* pSet)
{
    dvmUnlockMutex(&pSet->lock);
}

/*
 * Return the #of breakpoints.
 */
static int dvmBreakpointSetCount(const BreakpointSet* pSet)
{
    return pSet->count;
}

/*
 * See if we already have an entry for this address.
 *
 * The BreakpointSet's lock must be acquired before calling here.
 *
 * Returns the index of the breakpoint entry, or -1 if not found.
 */
static int dvmBreakpointSetFind(const BreakpointSet* pSet, const u2* addr)
{
    int i;

    for (i = 0; i < pSet->count; i++) {
        Breakpoint* pBreak = &pSet->breakpoints[i];
        if (pBreak->addr == addr)
            return i;
    }

    return -1;
}

/*
 * Retrieve the opcode that was originally at the specified location.
 *
 * The BreakpointSet's lock must be acquired before calling here.
 *
 * Returns "true" with the opcode in *pOrig on success.
 */
static bool dvmBreakpointSetOriginalOpCode(const BreakpointSet* pSet,
    const u2* addr, u1* pOrig)
{
    int idx = dvmBreakpointSetFind(pSet, addr);
    if (idx < 0)
        return false;

    *pOrig = pSet->breakpoints[idx].originalOpCode;
    return true;
}

/*
 * Check the opcode.  If it's a "magic" NOP, indicating the start of
 * switch or array data in the instruction stream, we don't want to set
 * a breakpoint.
 *
 * This can happen because the line number information dx generates
 * associates the switch data with the switch statement's line number,
 * and some debuggers put breakpoints at every address associated with
 * a given line.  The result is that the breakpoint stomps on the NOP
 * instruction that doubles as a data table magic number, and an explicit
 * check in the interpreter results in an exception being thrown.
 *
 * We don't want to simply refuse to add the breakpoint to the table,
 * because that confuses the housekeeping.  We don't want to reject the
 * debugger's event request, and we want to be sure that there's exactly
 * one un-set operation for every set op.
 */
static bool instructionIsMagicNop(const u2* addr)
{
    u2 curVal = *addr;
    return ((curVal & 0xff) == OP_NOP && (curVal >> 8) != 0);
}

/*
 * Add a breakpoint at a specific address.  If the address is already
 * present in the table, this just increments the count.
 *
 * For a new entry, this will extract and preserve the current opcode from
 * the instruction stream, and replace it with a breakpoint opcode.
 *
 * The BreakpointSet's lock must be acquired before calling here.
 *
 * Returns "true" on success.
 */
static bool dvmBreakpointSetAdd(BreakpointSet* pSet, Method* method,
    unsigned int instrOffset)
{
    const int kBreakpointGrowth = 10;
    const u2* addr = method->insns + instrOffset;
    int idx = dvmBreakpointSetFind(pSet, addr);
    Breakpoint* pBreak;

    if (idx < 0) {
        if (pSet->count == pSet->alloc) {
            int newSize = pSet->alloc + kBreakpointGrowth;
            Breakpoint* newVec;

            LOGV("+++ increasing breakpoint set size to %d\n", newSize);

            /* pSet->breakpoints will be NULL on first entry */
            newVec = realloc(pSet->breakpoints, newSize * sizeof(Breakpoint));
            if (newVec == NULL)
                return false;

            pSet->breakpoints = newVec;
            pSet->alloc = newSize;
        }

        pBreak = &pSet->breakpoints[pSet->count++];
        pBreak->method = method;
        pBreak->addr = (u2*)addr;
        pBreak->originalOpCode = *(u1*)addr;
        pBreak->setCount = 1;

        /*
         * Change the opcode.  We must ensure that the BreakpointSet
         * updates happen before we change the opcode.
         *
         * If the method has not been verified, we do NOT insert the
         * breakpoint yet, since that will screw up the verifier.  The
         * debugger is allowed to insert breakpoints in unverified code,
         * but since we don't execute unverified code we don't need to
         * alter the bytecode yet.
         *
         * The class init code will "flush" all pending opcode writes
         * before verification completes.
         */
        assert(*(u1*)addr != OP_BREAKPOINT);
        if (dvmIsClassVerified(method->clazz)) {
            LOGV("Class %s verified, adding breakpoint at %p\n",
                method->clazz->descriptor, addr);
            if (instructionIsMagicNop(addr)) {
                LOGV("Refusing to set breakpoint on %04x at %s.%s + 0x%x\n",
                    *addr, method->clazz->descriptor, method->name,
                    instrOffset);
            } else {
                ANDROID_MEMBAR_FULL();
                dvmDexChangeDex1(method->clazz->pDvmDex, (u1*)addr,
                    OP_BREAKPOINT);
            }
        } else {
            LOGV("Class %s NOT verified, deferring breakpoint at %p\n",
                method->clazz->descriptor, addr);
        }
    } else {
        /*
         * Breakpoint already exists, just increase the count.
         */
        pBreak = &pSet->breakpoints[idx];
        pBreak->setCount++;
    }

    return true;
}

/*
 * Remove one instance of the specified breakpoint.  When the count
 * reaches zero, the entry is removed from the table, and the original
 * opcode is restored.
 *
 * The BreakpointSet's lock must be acquired before calling here.
 */
static void dvmBreakpointSetRemove(BreakpointSet* pSet, Method* method,
    unsigned int instrOffset)
{
    const u2* addr = method->insns + instrOffset;
    int idx = dvmBreakpointSetFind(pSet, addr);

    if (idx < 0) {
        /* breakpoint not found in set -- unexpected */
        if (*(u1*)addr == OP_BREAKPOINT) {
            LOGE("Unable to restore breakpoint opcode (%s.%s +0x%x)\n",
                method->clazz->descriptor, method->name, instrOffset);
            dvmAbort();
        } else {
            LOGW("Breakpoint was already restored? (%s.%s +0x%x)\n",
                method->clazz->descriptor, method->name, instrOffset);
        }
    } else {
        Breakpoint* pBreak = &pSet->breakpoints[idx];
        if (pBreak->setCount == 1) {
            /*
             * Must restore opcode before removing set entry.
             *
             * If the breakpoint was never flushed, we could be ovewriting
             * a value with the same value.  Not a problem, though we
             * could end up causing a copy-on-write here when we didn't
             * need to.  (Not worth worrying about.)
             */
            dvmDexChangeDex1(method->clazz->pDvmDex, (u1*)addr,
                pBreak->originalOpCode);
            ANDROID_MEMBAR_FULL();

            if (idx != pSet->count-1) {
                /* shift down */
                memmove(&pSet->breakpoints[idx], &pSet->breakpoints[idx+1],
                    (pSet->count-1 - idx) * sizeof(pSet->breakpoints[0]));
            }
            pSet->count--;
            pSet->breakpoints[pSet->count].addr = (u2*) 0xdecadead; // debug
        } else {
            pBreak->setCount--;
            assert(pBreak->setCount > 0);
        }
    }
}

/*
 * Flush any breakpoints associated with methods in "clazz".  We want to
 * change the opcode, which might not have happened when the breakpoint
 * was initially set because the class was in the process of being
 * verified.
 *
 * The BreakpointSet's lock must be acquired before calling here.
 */
static void dvmBreakpointSetFlush(BreakpointSet* pSet, ClassObject* clazz)
{
    int i;
    for (i = 0; i < pSet->count; i++) {
        Breakpoint* pBreak = &pSet->breakpoints[i];
        if (pBreak->method->clazz == clazz) {
            /*
             * The breakpoint is associated with a method in this class.
             * It might already be there or it might not; either way,
             * flush it out.
             */
            LOGV("Flushing breakpoint at %p for %s\n",
                pBreak->addr, clazz->descriptor);
            if (instructionIsMagicNop(pBreak->addr)) {
                LOGV("Refusing to flush breakpoint on %04x at %s.%s + 0x%x\n",
                    *pBreak->addr, pBreak->method->clazz->descriptor,
                    pBreak->method->name, pBreak->addr - pBreak->method->insns);
            } else {
                dvmDexChangeDex1(clazz->pDvmDex, (u1*)pBreak->addr,
                    OP_BREAKPOINT);
            }
        }
    }
}


/*
 * Do any debugger-attach-time initialization.
 */
void dvmInitBreakpoints(void)
{
    /* quick sanity check */
    BreakpointSet* pSet = gDvm.breakpointSet;
    dvmBreakpointSetLock(pSet);
    if (dvmBreakpointSetCount(pSet) != 0) {
        LOGW("WARNING: %d leftover breakpoints\n", dvmBreakpointSetCount(pSet));
        /* generally not good, but we can keep going */
    }
    dvmBreakpointSetUnlock(pSet);
}

/*
 * Add an address to the list, putting it in the first non-empty slot.
 *
 * Sometimes the debugger likes to add two entries for one breakpoint.
 * We add two entries here, so that we get the right behavior when it's
 * removed twice.
 *
 * This will only be run from the JDWP thread, and it will happen while
 * we are updating the event list, which is synchronized.  We're guaranteed
 * to be the only one adding entries, and the lock ensures that nobody
 * will be trying to remove them while we're in here.
 *
 * "addr" is the absolute address of the breakpoint bytecode.
 */
void dvmAddBreakAddr(Method* method, unsigned int instrOffset)
{
    BreakpointSet* pSet = gDvm.breakpointSet;
    dvmBreakpointSetLock(pSet);
    dvmBreakpointSetAdd(pSet, method, instrOffset);
    dvmBreakpointSetUnlock(pSet);
}

/*
 * Remove an address from the list by setting the entry to NULL.
 *
 * This can be called from the JDWP thread (because the debugger has
 * cancelled the breakpoint) or from an event thread (because it's a
 * single-shot breakpoint, e.g. "run to line").  We only get here as
 * the result of removing an entry from the event list, which is
 * synchronized, so it should not be possible for two threads to be
 * updating breakpoints at the same time.
 */
void dvmClearBreakAddr(Method* method, unsigned int instrOffset)
{
    BreakpointSet* pSet = gDvm.breakpointSet;
    dvmBreakpointSetLock(pSet);
    dvmBreakpointSetRemove(pSet, method, instrOffset);
    dvmBreakpointSetUnlock(pSet);
}

/*
 * Get the original opcode from under a breakpoint.
 *
 * On SMP hardware it's possible one core might try to execute a breakpoint
 * after another core has cleared it.  We need to handle the case where
 * there's no entry in the breakpoint set.  (The memory barriers in the
 * locks and in the breakpoint update code should ensure that, once we've
 * observed the absence of a breakpoint entry, we will also now observe
 * the restoration of the original opcode.  The fact that we're holding
 * the lock prevents other threads from confusing things further.)
 */
u1 dvmGetOriginalOpCode(const u2* addr)
{
    BreakpointSet* pSet = gDvm.breakpointSet;
    u1 orig = 0;

    dvmBreakpointSetLock(pSet);
    if (!dvmBreakpointSetOriginalOpCode(pSet, addr, &orig)) {
        orig = *(u1*)addr;
        if (orig == OP_BREAKPOINT) {
            LOGE("GLITCH: can't find breakpoint, opcode is still set\n");
            dvmAbort();
        }
    }
    dvmBreakpointSetUnlock(pSet);

    return orig;
}

/*
 * Flush any breakpoints associated with methods in "clazz".
 *
 * We don't want to modify the bytecode of a method before the verifier
 * gets a chance to look at it, so we postpone opcode replacement until
 * after verification completes.
 */
void dvmFlushBreakpoints(ClassObject* clazz)
{
    BreakpointSet* pSet = gDvm.breakpointSet;

    if (pSet == NULL)
        return;

    assert(dvmIsClassVerified(clazz));
    dvmBreakpointSetLock(pSet);
    dvmBreakpointSetFlush(pSet, clazz);
    dvmBreakpointSetUnlock(pSet);
}

/*
 * Add a single step event.  Currently this is a global item.
 *
 * We set up some initial values based on the thread's current state.  This
 * won't work well if the thread is running, so it's up to the caller to
 * verify that it's suspended.
 *
 * This is only called from the JDWP thread.
 */
bool dvmAddSingleStep(Thread* thread, int size, int depth)
{
    StepControl* pCtrl = &gDvm.stepControl;

    if (pCtrl->active && thread != pCtrl->thread) {
        LOGW("WARNING: single-step active for %p; adding %p\n",
            pCtrl->thread, thread);

        /*
         * Keep going, overwriting previous.  This can happen if you
         * suspend a thread in Object.wait, hit the single-step key, then
         * switch to another thread and do the same thing again.
         * The first thread's step is still pending.
         *
         * TODO: consider making single-step per-thread.  Adds to the
         * overhead, but could be useful in rare situations.
         */
    }

    pCtrl->size = size;
    pCtrl->depth = depth;
    pCtrl->thread = thread;

    /*
     * We may be stepping into or over method calls, or running until we
     * return from the current method.  To make this work we need to track
     * the current line, current method, and current stack depth.  We need
     * to be checking these after most instructions, notably those that
     * call methods, return from methods, or are on a different line from the
     * previous instruction.
     *
     * We have to start with a snapshot of the current state.  If we're in
     * an interpreted method, everything we need is in the current frame.  If
     * we're in a native method, possibly with some extra JNI frames pushed
     * on by PushLocalFrame, we want to use the topmost native method.
     */
    const StackSaveArea* saveArea;
    void* fp;
    void* prevFp = NULL;

    for (fp = thread->curFrame; fp != NULL; fp = saveArea->prevFrame) {
        const Method* method;

        saveArea = SAVEAREA_FROM_FP(fp);
        method = saveArea->method;

        if (!dvmIsBreakFrame(fp) && !dvmIsNativeMethod(method))
            break;
        prevFp = fp;
    }
    if (fp == NULL) {
        LOGW("Unexpected: step req in native-only threadid=%d\n",
            thread->threadId);
        return false;
    }
    if (prevFp != NULL) {
        /*
         * First interpreted frame wasn't the one at the bottom.  Break
         * frames are only inserted when calling from native->interp, so we
         * don't need to worry about one being here.
         */
        LOGV("##### init step while in native method\n");
        fp = prevFp;
        assert(!dvmIsBreakFrame(fp));
        assert(dvmIsNativeMethod(SAVEAREA_FROM_FP(fp)->method));
        saveArea = SAVEAREA_FROM_FP(fp);
    }

    /*
     * Pull the goodies out.  "xtra.currentPc" should be accurate since
     * we update it on every instruction while the debugger is connected.
     */
    pCtrl->method = saveArea->method;
    // Clear out any old address set
    if (pCtrl->pAddressSet != NULL) {
        // (discard const)
        free((void *)pCtrl->pAddressSet);
        pCtrl->pAddressSet = NULL;
    }
    if (dvmIsNativeMethod(pCtrl->method)) {
        pCtrl->line = -1;
    } else {
        pCtrl->line = dvmLineNumFromPC(saveArea->method,
                        saveArea->xtra.currentPc - saveArea->method->insns);
        pCtrl->pAddressSet
                = dvmAddressSetForLine(saveArea->method, pCtrl->line);
    }
    pCtrl->frameDepth = dvmComputeVagueFrameDepth(thread, thread->curFrame);
    pCtrl->active = true;

    LOGV("##### step init: thread=%p meth=%p '%s' line=%d frameDepth=%d depth=%s size=%s\n",
        pCtrl->thread, pCtrl->method, pCtrl->method->name,
        pCtrl->line, pCtrl->frameDepth,
        dvmJdwpStepDepthStr(pCtrl->depth),
        dvmJdwpStepSizeStr(pCtrl->size));

    return true;
}

/*
 * Disable a single step event.
 */
void dvmClearSingleStep(Thread* thread)
{
    UNUSED_PARAMETER(thread);

    gDvm.stepControl.active = false;
}


/*
 * Recover the "this" pointer from the current interpreted method.  "this"
 * is always in "in0" for non-static methods.
 *
 * The "ins" start at (#of registers - #of ins).  Note in0 != v0.
 *
 * This works because "dx" guarantees that it will work.  It's probably
 * fairly common to have a virtual method that doesn't use its "this"
 * pointer, in which case we're potentially wasting a register.  However,
 * the debugger doesn't treat "this" as just another argument.  For
 * example, events (such as breakpoints) can be enabled for specific
 * values of "this".  There is also a separate StackFrame.ThisObject call
 * in JDWP that is expected to work for any non-native non-static method.
 *
 * Because we need it when setting up debugger event filters, we want to
 * be able to do this quickly.
 */
Object* dvmGetThisPtr(const Method* method, const u4* fp)
{
    if (dvmIsStaticMethod(method))
        return NULL;
    return (Object*)fp[method->registersSize - method->insSize];
}


#if defined(WITH_TRACKREF_CHECKS)
/*
 * Verify that all internally-tracked references have been released.  If
 * they haven't, print them and abort the VM.
 *
 * "debugTrackedRefStart" indicates how many refs were on the list when
 * we were first invoked.
 */
void dvmInterpCheckTrackedRefs(Thread* self, const Method* method,
    int debugTrackedRefStart)
{
    if (dvmReferenceTableEntries(&self->internalLocalRefTable)
        != (size_t) debugTrackedRefStart)
    {
        char* desc;
        Object** top;
        int count;

        count = dvmReferenceTableEntries(&self->internalLocalRefTable);

        LOGE("TRACK: unreleased internal reference (prev=%d total=%d)\n",
            debugTrackedRefStart, count);
        desc = dexProtoCopyMethodDescriptor(&method->prototype);
        LOGE("       current method is %s.%s %s\n", method->clazz->descriptor,
            method->name, desc);
        free(desc);
        top = self->internalLocalRefTable.table + debugTrackedRefStart;
        while (top < self->internalLocalRefTable.nextEntry) {
            LOGE("  %p (%s)\n",
                 *top,
                 ((*top)->clazz != NULL) ? (*top)->clazz->descriptor : "");
            top++;
        }
        dvmDumpThread(self, false);

        dvmAbort();
    }
    //LOGI("TRACK OK\n");
}
#endif


#ifdef LOG_INSTR
/*
 * Dump the v-registers.  Sent to the ILOG log tag.
 */
void dvmDumpRegs(const Method* method, const u4* framePtr, bool inOnly)
{
    int i, localCount;

    localCount = method->registersSize - method->insSize;

    LOG(LOG_VERBOSE, LOG_TAG"i", "Registers (fp=%p):\n", framePtr);
    for (i = method->registersSize-1; i >= 0; i--) {
        if (i >= localCount) {
            LOG(LOG_VERBOSE, LOG_TAG"i", "  v%-2d in%-2d : 0x%08x\n",
                i, i-localCount, framePtr[i]);
        } else {
            if (inOnly) {
                LOG(LOG_VERBOSE, LOG_TAG"i", "  [...]\n");
                break;
            }
            const char* name = "";
#if 0   // "locals" structure has changed -- need to rewrite this
            int j;
            DexFile* pDexFile = method->clazz->pDexFile;
            const DexCode* pDexCode = dvmGetMethodCode(method);
            int localsSize = dexGetLocalsSize(pDexFile, pDexCode);
            const DexLocal* locals = dvmDexGetLocals(pDexFile, pDexCode);
            for (j = 0; j < localsSize, j++) {
                if (locals[j].registerNum == (u4) i) {
                    name = dvmDexStringStr(locals[j].pName);
                    break;
                }
            }
#endif
            LOG(LOG_VERBOSE, LOG_TAG"i", "  v%-2d      : 0x%08x %s\n",
                i, framePtr[i], name);
        }
    }
}
#endif


/*
 * ===========================================================================
 *      Entry point and general support functions
 * ===========================================================================
 */

/*
 * Construct an s4 from two consecutive half-words of switch data.
 * This needs to check endianness because the DEX optimizer only swaps
 * half-words in instruction stream.
 *
 * "switchData" must be 32-bit aligned.
 */
#if __BYTE_ORDER == __LITTLE_ENDIAN
static inline s4 s4FromSwitchData(const void* switchData) {
    return *(s4*) switchData;
}
#else
static inline s4 s4FromSwitchData(const void* switchData) {
    u2* data = switchData;
    return data[0] | (((s4) data[1]) << 16);
}
#endif

/*
 * Find the matching case.  Returns the offset to the handler instructions.
 *
 * Returns 3 if we don't find a match (it's the size of the packed-switch
 * instruction).
 */
s4 dvmInterpHandlePackedSwitch(const u2* switchData, s4 testVal)
{
    const int kInstrLen = 3;
    u2 size;
    s4 firstKey;
    const s4* entries;

    /*
     * Packed switch data format:
     *  ushort ident = 0x0100   magic value
     *  ushort size             number of entries in the table
     *  int first_key           first (and lowest) switch case value
     *  int targets[size]       branch targets, relative to switch opcode
     *
     * Total size is (4+size*2) 16-bit code units.
     */
    if (*switchData++ != kPackedSwitchSignature) {
        /* should have been caught by verifier */
        dvmThrowException("Ljava/lang/InternalError;",
            "bad packed switch magic");
        return kInstrLen;
    }

    size = *switchData++;
    assert(size > 0);

    firstKey = *switchData++;
    firstKey |= (*switchData++) << 16;

    if (testVal < firstKey || testVal >= firstKey + size) {
        LOGVV("Value %d not found in switch (%d-%d)\n",
            testVal, firstKey, firstKey+size-1);
        return kInstrLen;
    }

    /* The entries are guaranteed to be aligned on a 32-bit boundary;
     * we can treat them as a native int array.
     */
    entries = (const s4*) switchData;
    assert(((u4)entries & 0x3) == 0);

    assert(testVal - firstKey >= 0 && testVal - firstKey < size);
    LOGVV("Value %d found in slot %d (goto 0x%02x)\n",
        testVal, testVal - firstKey,
        s4FromSwitchData(&entries[testVal - firstKey]));
    return s4FromSwitchData(&entries[testVal - firstKey]);
}

/*
 * Find the matching case.  Returns the offset to the handler instructions.
 *
 * Returns 3 if we don't find a match (it's the size of the sparse-switch
 * instruction).
 */
s4 dvmInterpHandleSparseSwitch(const u2* switchData, s4 testVal)
{
    const int kInstrLen = 3;
    u2 size;
    const s4* keys;
    const s4* entries;

    /*
     * Sparse switch data format:
     *  ushort ident = 0x0200   magic value
     *  ushort size             number of entries in the table; > 0
     *  int keys[size]          keys, sorted low-to-high; 32-bit aligned
     *  int targets[size]       branch targets, relative to switch opcode
     *
     * Total size is (2+size*4) 16-bit code units.
     */

    if (*switchData++ != kSparseSwitchSignature) {
        /* should have been caught by verifier */
        dvmThrowException("Ljava/lang/InternalError;",
            "bad sparse switch magic");
        return kInstrLen;
    }

    size = *switchData++;
    assert(size > 0);

    /* The keys are guaranteed to be aligned on a 32-bit boundary;
     * we can treat them as a native int array.
     */
    keys = (const s4*) switchData;
    assert(((u4)keys & 0x3) == 0);

    /* The entries are guaranteed to be aligned on a 32-bit boundary;
     * we can treat them as a native int array.
     */
    entries = keys + size;
    assert(((u4)entries & 0x3) == 0);

    /*
     * Binary-search through the array of keys, which are guaranteed to
     * be sorted low-to-high.
     */
    int lo = 0;
    int hi = size - 1;
    while (lo <= hi) {
        int mid = (lo + hi) >> 1;

        s4 foundVal = s4FromSwitchData(&keys[mid]);
        if (testVal < foundVal) {
            hi = mid - 1;
        } else if (testVal > foundVal) {
            lo = mid + 1;
        } else {
            LOGVV("Value %d found in entry %d (goto 0x%02x)\n",
                testVal, mid, s4FromSwitchData(&entries[mid]));
            return s4FromSwitchData(&entries[mid]);
        }
    }

    LOGVV("Value %d not found in switch\n", testVal);
    return kInstrLen;
}

/*
 * Copy data for a fill-array-data instruction.  On a little-endian machine
 * we can just do a memcpy(), on a big-endian system we have work to do.
 *
 * The trick here is that dexopt has byte-swapped each code unit, which is
 * exactly what we want for short/char data.  For byte data we need to undo
 * the swap, and for 4- or 8-byte values we need to swap pieces within
 * each word.
 */
static void copySwappedArrayData(void* dest, const u2* src, u4 size, u2 width)
{
#if __BYTE_ORDER == __LITTLE_ENDIAN
    memcpy(dest, src, size*width);
#else
    int i;

    switch (width) {
    case 1:
        /* un-swap pairs of bytes as we go */
        for (i = (size-1) & ~1; i >= 0; i -= 2) {
            ((u1*)dest)[i] = ((u1*)src)[i+1];
            ((u1*)dest)[i+1] = ((u1*)src)[i];
        }
        /*
         * "src" is padded to end on a two-byte boundary, but we don't want to
         * assume "dest" is, so we handle odd length specially.
         */
        if ((size & 1) != 0) {
            ((u1*)dest)[size-1] = ((u1*)src)[size];
        }
        break;
    case 2:
        /* already swapped correctly */
        memcpy(dest, src, size*width);
        break;
    case 4:
        /* swap word halves */
        for (i = 0; i < (int) size; i++) {
            ((u4*)dest)[i] = (src[(i << 1) + 1] << 16) | src[i << 1];
        }
        break;
    case 8:
        /* swap word halves and words */
        for (i = 0; i < (int) (size << 1); i += 2) {
            ((int*)dest)[i] = (src[(i << 1) + 3] << 16) | src[(i << 1) + 2];
            ((int*)dest)[i+1] = (src[(i << 1) + 1] << 16) | src[i << 1];
        }
        break;
    default:
        LOGE("Unexpected width %d in copySwappedArrayData\n", width);
        dvmAbort();
        break;
    }
#endif
}

/*
 * Fill the array with predefined constant values.
 *
 * Returns true if job is completed, otherwise false to indicate that
 * an exception has been thrown.
 */
bool dvmInterpHandleFillArrayData(ArrayObject* arrayObj, const u2* arrayData)
{
    u2 width;
    u4 size;

    if (arrayObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        return false;
    }
    assert (!IS_CLASS_FLAG_SET(((Object *)arrayObj)->clazz,
                               CLASS_ISOBJECTARRAY));

    /*
     * Array data table format:
     *  ushort ident = 0x0300   magic value
     *  ushort width            width of each element in the table
     *  uint   size             number of elements in the table
     *  ubyte  data[size*width] table of data values (may contain a single-byte
     *                          padding at the end)
     *
     * Total size is 4+(width * size + 1)/2 16-bit code units.
     */
    if (arrayData[0] != kArrayDataSignature) {
        dvmThrowException("Ljava/lang/InternalError;", "bad array data magic");
        return false;
    }

    width = arrayData[1];
    size = arrayData[2] | (((u4)arrayData[3]) << 16);

    if (size > arrayObj->length) {
        dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;", NULL);
        return false;
    }
    copySwappedArrayData(arrayObj->contents, &arrayData[4], size, width);
    return true;
}

/*
 * Find the concrete method that corresponds to "methodIdx".  The code in
 * "method" is executing invoke-method with "thisClass" as its first argument.
 *
 * Returns NULL with an exception raised on failure.
 */
Method* dvmInterpFindInterfaceMethod(ClassObject* thisClass, u4 methodIdx,
    const Method* method, DvmDex* methodClassDex)
{
    Method* absMethod;
    Method* methodToCall;
    int i, vtableIndex;

    /*
     * Resolve the method.  This gives us the abstract method from the
     * interface class declaration.
     */
    absMethod = dvmDexGetResolvedMethod(methodClassDex, methodIdx);
    if (absMethod == NULL) {
        absMethod = dvmResolveInterfaceMethod(method->clazz, methodIdx);
        if (absMethod == NULL) {
            LOGV("+ unknown method\n");
            return NULL;
        }
    }

    /* make sure absMethod->methodIndex means what we think it means */
    assert(dvmIsAbstractMethod(absMethod));

    /*
     * Run through the "this" object's iftable.  Find the entry for
     * absMethod's class, then use absMethod->methodIndex to find
     * the method's entry.  The value there is the offset into our
     * vtable of the actual method to execute.
     *
     * The verifier does not guarantee that objects stored into
     * interface references actually implement the interface, so this
     * check cannot be eliminated.
     */
    for (i = 0; i < thisClass->iftableCount; i++) {
        if (thisClass->iftable[i].clazz == absMethod->clazz)
            break;
    }
    if (i == thisClass->iftableCount) {
        /* impossible in verified DEX, need to check for it in unverified */
        dvmThrowException("Ljava/lang/IncompatibleClassChangeError;",
            "interface not implemented");
        return NULL;
    }

    assert(absMethod->methodIndex <
        thisClass->iftable[i].clazz->virtualMethodCount);

    vtableIndex =
        thisClass->iftable[i].methodIndexArray[absMethod->methodIndex];
    assert(vtableIndex >= 0 && vtableIndex < thisClass->vtableCount);
    methodToCall = thisClass->vtable[vtableIndex];

#if 0
    /* this can happen when there's a stale class file */
    if (dvmIsAbstractMethod(methodToCall)) {
        dvmThrowException("Ljava/lang/AbstractMethodError;",
            "interface method not implemented");
        return NULL;
    }
#else
    assert(!dvmIsAbstractMethod(methodToCall) ||
        methodToCall->nativeFunc != NULL);
#endif

    LOGVV("+++ interface=%s.%s concrete=%s.%s\n",
        absMethod->clazz->descriptor, absMethod->name,
        methodToCall->clazz->descriptor, methodToCall->name);
    assert(methodToCall != NULL);

    return methodToCall;
}



/*
 * Helpers for dvmThrowVerificationError().
 *
 * Each returns a newly-allocated string.
 */
#define kThrowShow_accessFromClass     1
static char* classNameFromIndex(const Method* method, int ref,
    VerifyErrorRefType refType, int flags)
{
    static const int kBufLen = 256;
    const DvmDex* pDvmDex = method->clazz->pDvmDex;

    if (refType == VERIFY_ERROR_REF_FIELD) {
        /* get class ID from field ID */
        const DexFieldId* pFieldId = dexGetFieldId(pDvmDex->pDexFile, ref);
        ref = pFieldId->classIdx;
    } else if (refType == VERIFY_ERROR_REF_METHOD) {
        /* get class ID from method ID */
        const DexMethodId* pMethodId = dexGetMethodId(pDvmDex->pDexFile, ref);
        ref = pMethodId->classIdx;
    }

    const char* className = dexStringByTypeIdx(pDvmDex->pDexFile, ref);
    char* dotClassName = dvmDescriptorToDot(className);
    if (flags == 0)
        return dotClassName;

    char* result = (char*) malloc(kBufLen);

    if ((flags & kThrowShow_accessFromClass) != 0) {
        char* dotFromName = dvmDescriptorToDot(method->clazz->descriptor);
        snprintf(result, kBufLen, "tried to access class %s from class %s",
            dotClassName, dotFromName);
        free(dotFromName);
    } else {
        assert(false);      // should've been caught above
        result[0] = '\0';
    }

    free(dotClassName);
    return result;
}
static char* fieldNameFromIndex(const Method* method, int ref,
    VerifyErrorRefType refType, int flags)
{
    static const int kBufLen = 256;
    const DvmDex* pDvmDex = method->clazz->pDvmDex;
    const DexFieldId* pFieldId;
    const char* className;
    const char* fieldName;

    if (refType != VERIFY_ERROR_REF_FIELD) {
        LOGW("Expected ref type %d, got %d\n", VERIFY_ERROR_REF_FIELD, refType);
        return NULL;    /* no message */
    }

    pFieldId = dexGetFieldId(pDvmDex->pDexFile, ref);
    className = dexStringByTypeIdx(pDvmDex->pDexFile, pFieldId->classIdx);
    fieldName = dexStringById(pDvmDex->pDexFile, pFieldId->nameIdx);

    char* dotName = dvmDescriptorToDot(className);
    char* result = (char*) malloc(kBufLen);

    if ((flags & kThrowShow_accessFromClass) != 0) {
        char* dotFromName = dvmDescriptorToDot(method->clazz->descriptor);
        snprintf(result, kBufLen, "tried to access field %s.%s from class %s",
            dotName, fieldName, dotFromName);
        free(dotFromName);
    } else {
        snprintf(result, kBufLen, "%s.%s", dotName, fieldName);
    }

    free(dotName);
    return result;
}
static char* methodNameFromIndex(const Method* method, int ref,
    VerifyErrorRefType refType, int flags)
{
    static const int kBufLen = 384;
    const DvmDex* pDvmDex = method->clazz->pDvmDex;
    const DexMethodId* pMethodId;
    const char* className;
    const char* methodName;

    if (refType != VERIFY_ERROR_REF_METHOD) {
        LOGW("Expected ref type %d, got %d\n", VERIFY_ERROR_REF_METHOD,refType);
        return NULL;    /* no message */
    }

    pMethodId = dexGetMethodId(pDvmDex->pDexFile, ref);
    className = dexStringByTypeIdx(pDvmDex->pDexFile, pMethodId->classIdx);
    methodName = dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx);

    char* dotName = dvmDescriptorToDot(className);
    char* result = (char*) malloc(kBufLen);

    if ((flags & kThrowShow_accessFromClass) != 0) {
        char* dotFromName = dvmDescriptorToDot(method->clazz->descriptor);
        char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        snprintf(result, kBufLen,
            "tried to access method %s.%s:%s from class %s",
            dotName, methodName, desc, dotFromName);
        free(dotFromName);
        free(desc);
    } else {
        snprintf(result, kBufLen, "%s.%s", dotName, methodName);
    }

    free(dotName);
    return result;
}

/*
 * Throw an exception for a problem identified by the verifier.
 *
 * This is used by the invoke-verification-error instruction.  It always
 * throws an exception.
 *
 * "kind" indicates the kind of failure encountered by the verifier.  It
 * has two parts, an error code and an indication of the reference type.
 */
void dvmThrowVerificationError(const Method* method, int kind, int ref)
{
    const int typeMask = 0xff << kVerifyErrorRefTypeShift;
    VerifyError errorKind = kind & ~typeMask;
    VerifyErrorRefType refType = kind >> kVerifyErrorRefTypeShift;
    const char* exceptionName = "Ljava/lang/VerifyError;";
    char* msg = NULL;

    switch ((VerifyError) errorKind) {
    case VERIFY_ERROR_NO_CLASS:
        exceptionName = "Ljava/lang/NoClassDefFoundError;";
        msg = classNameFromIndex(method, ref, refType, 0);
        break;
    case VERIFY_ERROR_NO_FIELD:
        exceptionName = "Ljava/lang/NoSuchFieldError;";
        msg = fieldNameFromIndex(method, ref, refType, 0);
        break;
    case VERIFY_ERROR_NO_METHOD:
        exceptionName = "Ljava/lang/NoSuchMethodError;";
        msg = methodNameFromIndex(method, ref, refType, 0);
        break;
    case VERIFY_ERROR_ACCESS_CLASS:
        exceptionName = "Ljava/lang/IllegalAccessError;";
        msg = classNameFromIndex(method, ref, refType,
            kThrowShow_accessFromClass);
        break;
    case VERIFY_ERROR_ACCESS_FIELD:
        exceptionName = "Ljava/lang/IllegalAccessError;";
        msg = fieldNameFromIndex(method, ref, refType,
            kThrowShow_accessFromClass);
        break;
    case VERIFY_ERROR_ACCESS_METHOD:
        exceptionName = "Ljava/lang/IllegalAccessError;";
        msg = methodNameFromIndex(method, ref, refType,
            kThrowShow_accessFromClass);
        break;
    case VERIFY_ERROR_CLASS_CHANGE:
        exceptionName = "Ljava/lang/IncompatibleClassChangeError;";
        msg = classNameFromIndex(method, ref, refType, 0);
        break;
    case VERIFY_ERROR_INSTANTIATION:
        exceptionName = "Ljava/lang/InstantiationError;";
        msg = classNameFromIndex(method, ref, refType, 0);
        break;

    case VERIFY_ERROR_GENERIC:
        /* generic VerifyError; use default exception, no message */
        break;
    case VERIFY_ERROR_NONE:
        /* should never happen; use default exception */
        assert(false);
        msg = strdup("weird - no error specified");
        break;

    /* no default clause -- want warning if enum updated */
    }

    dvmThrowException(exceptionName, msg);
    free(msg);
}

/*
 * Main interpreter loop entry point.  Select "standard" or "debug"
 * interpreter and switch between them as required.
 *
 * This begins executing code at the start of "method".  On exit, "pResult"
 * holds the return value of the method (or, if "method" returns NULL, it
 * holds an undefined value).
 *
 * The interpreted stack frame, which holds the method arguments, has
 * already been set up.
 */
void dvmInterpret(Thread* self, const Method* method, JValue* pResult)
{
    InterpState interpState;
    bool change;
#if defined(WITH_JIT)
    /* Target-specific save/restore */
    extern void dvmJitCalleeSave(double *saveArea);
    extern void dvmJitCalleeRestore(double *saveArea);
    /* Interpreter entry points from compiled code */
    extern void dvmJitToInterpNormal();
    extern void dvmJitToInterpNoChain();
    extern void dvmJitToInterpPunt();
    extern void dvmJitToInterpSingleStep();
    extern void dvmJitToInterpTraceSelectNoChain();
    extern void dvmJitToInterpTraceSelect();
    extern void dvmJitToPatchPredictedChain();
#if defined(WITH_SELF_VERIFICATION)
    extern void dvmJitToInterpBackwardBranch();
#endif

    /*
     * Reserve a static entity here to quickly setup runtime contents as
     * gcc will issue block copy instructions.
     */
    static struct JitToInterpEntries jitToInterpEntries = {
        dvmJitToInterpNormal,
        dvmJitToInterpNoChain,
        dvmJitToInterpPunt,
        dvmJitToInterpSingleStep,
        dvmJitToInterpTraceSelectNoChain,
        dvmJitToInterpTraceSelect,
        dvmJitToPatchPredictedChain,
#if defined(WITH_SELF_VERIFICATION)
        dvmJitToInterpBackwardBranch,
#endif
    };

    /*
     * If the previous VM left the code cache through single-stepping the
     * inJitCodeCache flag will be set when the VM is re-entered (for example,
     * in self-verification mode we single-step NEW_INSTANCE which may re-enter
     * the VM through findClassFromLoaderNoInit). Because of that, we cannot
     * assert that self->inJitCodeCache is NULL here.
     */
#endif


#if defined(WITH_TRACKREF_CHECKS)
    interpState.debugTrackedRefStart =
        dvmReferenceTableEntries(&self->internalLocalRefTable);
#endif
    interpState.debugIsMethodEntry = true;
#if defined(WITH_JIT)
    dvmJitCalleeSave(interpState.calleeSave);
    /* Initialize the state to kJitNot */
    interpState.jitState = kJitNot;

    /* Setup the Jit-to-interpreter entry points */
    interpState.jitToInterpEntries = jitToInterpEntries;

    /*
     * Initialize the threshold filter [don't bother to zero out the
     * actual table.  We're looking for matches, and an occasional
     * false positive is acceptible.
     */
    interpState.lastThreshFilter = 0;

    interpState.icRechainCount = PREDICTED_CHAIN_COUNTER_RECHAIN;
#endif

    /*
     * Initialize working state.
     *
     * No need to initialize "retval".
     */
    interpState.method = method;
    interpState.fp = (u4*) self->curFrame;
    interpState.pc = method->insns;
    interpState.entryPoint = kInterpEntryInstr;

    if (dvmDebuggerOrProfilerActive())
        interpState.nextMode = INTERP_DBG;
    else
        interpState.nextMode = INTERP_STD;

    assert(!dvmIsNativeMethod(method));

    /*
     * Make sure the class is ready to go.  Shouldn't be possible to get
     * here otherwise.
     */
    if (method->clazz->status < CLASS_INITIALIZING ||
        method->clazz->status == CLASS_ERROR)
    {
        LOGE("ERROR: tried to execute code in unprepared class '%s' (%d)\n",
            method->clazz->descriptor, method->clazz->status);
        dvmDumpThread(self, false);
        dvmAbort();
    }

    typedef bool (*Interpreter)(Thread*, InterpState*);
    Interpreter stdInterp;
    if (gDvm.executionMode == kExecutionModeInterpFast)
        stdInterp = dvmMterpStd;
#if defined(WITH_JIT)
    else if (gDvm.executionMode == kExecutionModeJit)
/* If profiling overhead can be kept low enough, we can use a profiling
 * mterp fast for both Jit and "fast" modes.  If overhead is too high,
 * create a specialized profiling interpreter.
 */
        stdInterp = dvmMterpStd;
#endif
    else
        stdInterp = dvmInterpretStd;

    change = true;
    while (change) {
        switch (interpState.nextMode) {
        case INTERP_STD:
            LOGVV("threadid=%d: interp STD\n", self->threadId);
            change = (*stdInterp)(self, &interpState);
            break;
        case INTERP_DBG:
            LOGVV("threadid=%d: interp DBG\n", self->threadId);
            change = dvmInterpretDbg(self, &interpState);
            break;
        default:
            dvmAbort();
        }
    }

    *pResult = interpState.retval;
#if defined(WITH_JIT)
    dvmJitCalleeRestore(interpState.calleeSave);
#endif
}

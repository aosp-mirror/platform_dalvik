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
 * Some debugger support functions are included here.  Ideally their
 * entire existence would be "#ifdef WITH_DEBUGGER", but we're not that
 * aggressive in other parts of the code yet.
 */
#include "Dalvik.h"
#include "interp/InterpDefs.h"


/*
 * ===========================================================================
 *      Debugger support
 * ===========================================================================
 */

/*
 * Initialize the breakpoint address lookup table when the debugger attaches.
 *
 * This shouldn't be necessary -- the global area is initially zeroed out,
 * and the events should be cleaning up after themselves.
 */
void dvmInitBreakpoints(void)
{
#ifdef WITH_DEBUGGER
    memset(gDvm.debugBreakAddr, 0, sizeof(gDvm.debugBreakAddr));
#else
    assert(false);
#endif
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
void dvmAddBreakAddr(Method* method, int instrOffset)
{
#ifdef WITH_DEBUGGER
    const u2* addr = method->insns + instrOffset;
    const u2** ptr = gDvm.debugBreakAddr;
    int i;

    LOGV("BKP: add %p %s.%s (%s:%d)\n",
        addr, method->clazz->descriptor, method->name,
        dvmGetMethodSourceFile(method), dvmLineNumFromPC(method, instrOffset));

    method->debugBreakpointCount++;
    for (i = 0; i < MAX_BREAKPOINTS; i++, ptr++) {
        if (*ptr == NULL) {
            *ptr = addr;
            break;
        }
    }
    if (i == MAX_BREAKPOINTS) {
        /* no room; size is too small or we're not cleaning up properly */
        LOGE("ERROR: max breakpoints exceeded\n");
        assert(false);
    }
#else
    assert(false);
#endif
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
void dvmClearBreakAddr(Method* method, int instrOffset)
{
#ifdef WITH_DEBUGGER
    const u2* addr = method->insns + instrOffset;
    const u2** ptr = gDvm.debugBreakAddr;
    int i;

    LOGV("BKP: clear %p %s.%s (%s:%d)\n",
        addr, method->clazz->descriptor, method->name,
        dvmGetMethodSourceFile(method), dvmLineNumFromPC(method, instrOffset));

    method->debugBreakpointCount--;
    assert(method->debugBreakpointCount >= 0);
    for (i = 0; i < MAX_BREAKPOINTS; i++, ptr++) {
        if (*ptr == addr) {
            *ptr = NULL;
            break;
        }
    }
    if (i == MAX_BREAKPOINTS) {
        /* didn't find it */
        LOGE("ERROR: breakpoint on %p not found\n", addr);
        assert(false);
    }
#else
    assert(false);
#endif
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
#ifdef WITH_DEBUGGER
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
#else
    assert(false);
    return false;
#endif
}

/*
 * Disable a single step event.
 */
void dvmClearSingleStep(Thread* thread)
{
#ifdef WITH_DEBUGGER
    UNUSED_PARAMETER(thread);

    gDvm.stepControl.active = false;
#else
    assert(false);
#endif
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
            int j;
#if 0   // "locals" structure has changed -- need to rewrite this
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
    u2 ident, size;
    const s4* keys;
    const s4* entries;
    int i;

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
     * Run through the list of keys, which are guaranteed to
     * be sorted low-to-high.
     *
     * Most tables have 3-4 entries.  Few have more than 10.  A binary
     * search here is probably not useful.
     */
    for (i = 0; i < size; i++) {
        s4 k = s4FromSwitchData(&keys[i]);
        if (k == testVal) {
            LOGVV("Value %d found in entry %d (goto 0x%02x)\n",
                testVal, i, s4FromSwitchData(&entries[i]));
            return s4FromSwitchData(&entries[i]);
        } else if (k > testVal) {
            break;
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
    /* Interpreter entry points from compiled code */
    extern void dvmJitToInterpNormal();
    extern void dvmJitToInterpNoChain();
    extern void dvmJitToInterpPunt();
    extern void dvmJitToInterpSingleStep();
    extern void dvmJitToTraceSelect();
    extern void dvmJitToPatchPredictedChain();

    /*
     * Reserve a static entity here to quickly setup runtime contents as
     * gcc will issue block copy instructions.
     */
    static struct JitToInterpEntries jitToInterpEntries = {
        dvmJitToInterpNormal,
        dvmJitToInterpNoChain,
        dvmJitToInterpPunt,
        dvmJitToInterpSingleStep,
        dvmJitToTraceSelect,
        dvmJitToPatchPredictedChain,
    };
#endif


#if defined(WITH_TRACKREF_CHECKS)
    interpState.debugTrackedRefStart =
        dvmReferenceTableEntries(&self->internalLocalRefTable);
#endif
#if defined(WITH_PROFILER) || defined(WITH_DEBUGGER)
    interpState.debugIsMethodEntry = true;
#endif
#if defined(WITH_JIT)
    interpState.jitState = gDvmJit.pJitEntryTable ? kJitNormal : kJitOff;

    /* Setup the Jit-to-interpreter entry points */
    interpState.jitToInterpEntries = jitToInterpEntries;

    /*
     * Initialize the threshold filter [don't bother to zero out the
     * actual table.  We're looking for matches, and an occasional
     * false positive is acceptible.
     */
    interpState.lastThreshFilter = 0;
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
#if defined(WITH_PROFILER) || defined(WITH_DEBUGGER) || defined(WITH_JIT)
        case INTERP_DBG:
            LOGVV("threadid=%d: interp DBG\n", self->threadId);
            change = dvmInterpretDbg(self, &interpState);
            break;
#endif
        default:
            dvmAbort();
        }
    }

    *pResult = interpState.retval;
}

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
 * Dalvik classfile verification.  This file contains the verifier entry
 * points and the static constraint checks.
 */
#include "Dalvik.h"
#include "analysis/CodeVerify.h"
#include "libdex/DexCatch.h"
#include "libdex/InstrUtils.h"

//#define static

/* verification failure reporting */
#define LOG_VFY(...)                dvmLogVerifyFailure(NULL, __VA_ARGS__);
#define LOG_VFY_METH(_meth, ...)    dvmLogVerifyFailure(_meth, __VA_ARGS__);


/* fwd */
static bool computeCodeWidths(const Method* meth, InsnFlags* insnFlags,\
    int* pNewInstanceCount);
static bool setTryFlags(const Method* meth, InsnFlags* insnFlags);
static bool verifyMethod(Method* meth, int verifyFlags);
static bool verifyInstructions(const Method* meth, InsnFlags* insnFlags,
    int verifyFlags);
static bool checkNewInstance(const Method* meth, int insnIdx);
static bool checkNewArray(const Method* meth, int insnIdx);


/*
 * Initialize some things we need for verification.
 */
bool dvmVerificationStartup(void)
{
    gDvm.instrWidth = dexCreateInstrWidthTable();
    gDvm.instrFormat = dexCreateInstrFormatTable();
    gDvm.instrFlags = dexCreateInstrFlagsTable();
    return (gDvm.instrWidth != NULL && gDvm.instrFormat!= NULL);
}

/*
 * Initialize some things we need for verification.
 */
void dvmVerificationShutdown(void)
{
    free(gDvm.instrWidth);
    free(gDvm.instrFormat);
    free(gDvm.instrFlags);
}

/*
 * Induce verification on all classes loaded from this DEX file as part
 * of pre-verification and optimization.  This is never called from a
 * normally running VM.
 *
 * Returns "true" when all classes have been processed.
 */
bool dvmVerifyAllClasses(DexFile* pDexFile)
{
    u4 count = pDexFile->pHeader->classDefsSize;
    u4 idx;

    assert(gDvm.optimizing);

    if (gDvm.classVerifyMode == VERIFY_MODE_NONE) {
        LOGV("+++ verification is disabled, skipping all classes\n");
        return true;
    }
    if (gDvm.classVerifyMode == VERIFY_MODE_REMOTE &&
        gDvm.optimizingBootstrapClass)
    {
        LOGV("+++ verification disabled for bootstrap classes\n");
        return true;
    }

    for (idx = 0; idx < count; idx++) {
        const DexClassDef* pClassDef;
        const char* classDescriptor;
        ClassObject* clazz;
        
        pClassDef = dexGetClassDef(pDexFile, idx);
        classDescriptor = dexStringByTypeIdx(pDexFile, pClassDef->classIdx);

        /* all classes are loaded into the bootstrap class loader */
        clazz = dvmLookupClass(classDescriptor, NULL, false);
        if (clazz != NULL) {
            if (clazz->pDvmDex->pDexFile != pDexFile) {
                LOGD("DexOpt: not verifying '%s': multiple definitions\n",
                    classDescriptor);
            } else {
                if (dvmVerifyClass(clazz, VERIFY_DEFAULT)) {
                    assert((clazz->accessFlags & JAVA_FLAGS_MASK) ==
                        pClassDef->accessFlags);
                    ((DexClassDef*)pClassDef)->accessFlags |=
                        CLASS_ISPREVERIFIED;
                }
                /* keep going even if one fails */
            }
        } else {
            LOGV("DexOpt: +++  not verifying '%s'\n", classDescriptor);
        }
    }

    return true;
}

/*
 * Verify a class.
 *
 * By the time we get here, the value of gDvm.classVerifyMode should already
 * have been factored in.  If you want to call into the verifier even
 * though verification is disabled, that's your business.
 *
 * Returns "true" on success.
 */
bool dvmVerifyClass(ClassObject* clazz, int verifyFlags)
{
    int i;

    if (dvmIsClassVerified(clazz)) {
        LOGD("Ignoring duplicate verify attempt on %s\n", clazz->descriptor);
        return true;
    }

    //LOGI("Verify1 '%s'\n", clazz->descriptor);

    // TODO - verify class structure in DEX?

    for (i = 0; i < clazz->directMethodCount; i++) {
        if (!verifyMethod(&clazz->directMethods[i], verifyFlags)) {
            LOG_VFY("Verifier rejected class %s\n", clazz->descriptor);
            return false;
        }
    }
    for (i = 0; i < clazz->virtualMethodCount; i++) {
        if (!verifyMethod(&clazz->virtualMethods[i], verifyFlags)) {
            LOG_VFY("Verifier rejected class %s\n", clazz->descriptor);
            return false;
        }
    }

    return true;
}

/*
 * Perform verification on a single method.
 *
 * We do this in three passes:
 *  (1) Walk through all code units, determining instruction lengths.
 *  (2) Do static checks, including branch target and operand validation.
 *  (3) Do structural checks, including data-flow analysis.
 *
 * Some checks may be bypassed depending on the verification mode.  We can't
 * turn this stuff off completely if we want to do "exact" GC.
 *
 * - operands of getfield, putfield, getstatic, putstatic must be valid
 * - operands of method invocation instructions must be valid
 *
 * - code array must not be empty
 * - (N/A) code_length must be less than 65536
 * - opcode of first instruction begins at index 0
 * - only documented instructions may appear
 * - each instruction follows the last
 * - (below) last byte of last instruction is at (code_length-1)
 */
static bool verifyMethod(Method* meth, int verifyFlags)
{
    bool result = false;
    UninitInstanceMap* uninitMap = NULL;
    InsnFlags* insnFlags = NULL;
    int i, newInstanceCount;

    /*
     * If there aren't any instructions, make sure that's expected, then
     * exit successfully. Note: meth->insns gets set to a native function
     * pointer on first call.
     */
    if (dvmGetMethodInsnsSize(meth) == 0) {
        if (!dvmIsNativeMethod(meth) && !dvmIsAbstractMethod(meth)) {
            LOG_VFY_METH(meth,
                "VFY: zero-length code in concrete non-native method\n");
            goto bail;
        }

        goto success;
    }
        
    /*
     * Allocate and populate an array to hold instruction data.
     *
     * TODO: Consider keeping a reusable pre-allocated array sitting
     * around for smaller methods.
     */
    insnFlags = (InsnFlags*)
        calloc(dvmGetMethodInsnsSize(meth), sizeof(InsnFlags));
    if (insnFlags == NULL)
        goto bail;

    /*
     * Compute the width of each instruction and store the result in insnFlags.
     * Count up the #of occurrences of new-instance instructions while we're
     * at it.
     */
    if (!computeCodeWidths(meth, insnFlags, &newInstanceCount))
        goto bail;

    /*
     * Allocate a map to hold the classes of uninitialized instances.
     */
    uninitMap = dvmCreateUninitInstanceMap(meth, insnFlags, newInstanceCount);
    if (uninitMap == NULL)
        goto bail;

    /*
     * Set the "in try" flags for all instructions guarded by a "try" block.
     */
    if (!setTryFlags(meth, insnFlags))
        goto bail;

    /*
     * Perform static instruction verification.
     */
    if (!verifyInstructions(meth, insnFlags, verifyFlags))
        goto bail;

    /*
     * Do code-flow analysis.  Do this after verifying the branch targets
     * so we don't need to worry about it here.
     *
     * If there are no registers, we don't need to do much in the way of
     * analysis, but we still need to verify that nothing actually tries
     * to use a register.
     */
    if (!dvmVerifyCodeFlow(meth, insnFlags, uninitMap)) {
        //LOGD("+++ %s failed code flow\n", meth->name);
        goto bail;
    }

success:
    result = true;

bail:
    dvmFreeUninitInstanceMap(uninitMap);
    free(insnFlags);
    return result;
}


/*
 * Compute the width of the instruction at each address in the instruction
 * stream.  Addresses that are in the middle of an instruction, or that
 * are part of switch table data, are not set (so the caller should probably
 * initialize "insnFlags" to zero).
 *
 * Logs an error and returns "false" on failure.
 */
static bool computeCodeWidths(const Method* meth, InsnFlags* insnFlags,
    int* pNewInstanceCount)
{
    const int insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns;
    bool result = false;
    int i;

    *pNewInstanceCount = 0;

    for (i = 0; i < insnCount; /**/) {
        int width;

        /*
         * Switch tables are identified with "extended NOP" opcodes.  They
         * contain no executable code, so we can just skip past them.
         */
        if (*insns == kPackedSwitchSignature) {
            width = 4 + insns[1] * 2;
        } else if (*insns == kSparseSwitchSignature) {
            width = 2 + insns[1] * 4;
        /*
         * Array data table is identified with similar extended NOP opcode.
         */
        } else if (*insns == kArrayDataSignature) {
            u4 size = insns[2] | (((u4)insns[3]) << 16);
            width = 4 + (insns[1] * size + 1) / 2;
        } else {
            int instr = *insns & 0xff;
            width = dexGetInstrWidthAbs(gDvm.instrWidth, instr);
            if (width == 0) {
                LOG_VFY_METH(meth,
                    "VFY: invalid post-opt instruction (0x%x)\n", instr);
                goto bail;
            }
            if (width < 0 || width > 5) {
                LOGE("VFY: bizarre width value %d\n", width);
                dvmAbort();
            }

            if (instr == OP_NEW_INSTANCE)
                (*pNewInstanceCount)++;
        }

        if (width > 65535) {
            LOG_VFY_METH(meth, "VFY: insane width %d\n", width);
            goto bail;
        }

        insnFlags[i] |= width;
        i += width;
        insns += width;
    }
    if (i != (int) dvmGetMethodInsnsSize(meth)) {
        LOG_VFY_METH(meth, "VFY: code did not end where expected (%d vs. %d)\n",
            i, dvmGetMethodInsnsSize(meth));
        goto bail;
    }

    result = true;

bail:
    return result;
}

/*
 * Set the "in try" flags for all instructions protected by "try" statements.
 * Also sets the "branch target" flags for exception handlers.
 *
 * Call this after widths have been set in "insnFlags".
 *
 * Returns "false" if something in the exception table looks fishy, but
 * we're expecting the exception table to be somewhat sane.
 */
static bool setTryFlags(const Method* meth, InsnFlags* insnFlags)
{
    u4 insnsSize = dvmGetMethodInsnsSize(meth);
    DexFile* pDexFile = meth->clazz->pDvmDex->pDexFile;
    const DexCode* pCode = dvmGetMethodCode(meth);
    u4 triesSize = pCode->triesSize;
    const DexTry* pTries;
    u4 handlersSize;
    u4 offset;
    u4 i;

    if (triesSize == 0) {
        return true;
    }

    pTries = dexGetTries(pCode);
    handlersSize = dexGetHandlersSize(pCode);

    for (i = 0; i < triesSize; i++) {
        const DexTry* pTry = &pTries[i];
        u4 start = pTry->startAddr;
        u4 end = start + pTry->insnCount;
        u4 addr;

        if ((start >= end) || (start >= insnsSize) || (end > insnsSize)) {
            LOG_VFY_METH(meth,
                "VFY: bad exception entry: startAddr=%d endAddr=%d (size=%d)\n",
                start, end, insnsSize);
            return false;
        }

        if (dvmInsnGetWidth(insnFlags, start) == 0) {
            LOG_VFY_METH(meth,
                "VFY: 'try' block starts inside an instruction (%d)\n",
                start);
            return false;
        }

        for (addr = start; addr < end;
            addr += dvmInsnGetWidth(insnFlags, addr))
        {
            assert(dvmInsnGetWidth(insnFlags, addr) != 0);
            dvmInsnSetInTry(insnFlags, addr, true);
        }
    }

    /* Iterate over each of the handlers to verify target addresses. */
    offset = dexGetFirstHandlerOffset(pCode);
    for (i = 0; i < handlersSize; i++) {
        DexCatchIterator iterator;
        dexCatchIteratorInit(&iterator, pCode, offset);

        for (;;) {
            DexCatchHandler* handler = dexCatchIteratorNext(&iterator);
            u4 addr;
            
            if (handler == NULL) {
                break;
            }

            addr = handler->address;
            if (dvmInsnGetWidth(insnFlags, addr) == 0) {
                LOG_VFY_METH(meth,
                    "VFY: exception handler starts at bad address (%d)\n",
                    addr);
                return false;
            }
        
            dvmInsnSetBranchTarget(insnFlags, addr, true);
        }

        offset = dexCatchIteratorGetEndOffset(&iterator, pCode);
    }

    return true;
}

/*
 * Verify a switch table.  "curOffset" is the offset of the switch
 * instruction.
 */
static bool checkSwitchTargets(const Method* meth, InsnFlags* insnFlags,
    int curOffset)
{
    const int insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns + curOffset;
    const u2* switchInsns;
    int switchCount, tableSize;
    int offsetToSwitch, offsetToKeys, offsetToTargets, targ;
    int offset, absOffset;

    assert(curOffset >= 0 && curOffset < insnCount);

    /* make sure the start of the switch is in range */
    offsetToSwitch = (s2) insns[1];
    if (curOffset + offsetToSwitch < 0 ||
        curOffset + offsetToSwitch + 2 >= insnCount)
    {
        LOG_VFY_METH(meth,
            "VFY: invalid switch start: at %d, switch offset %d, count %d\n",
            curOffset, offsetToSwitch, insnCount);
        return false;
    }

    /* offset to switch table is a relative branch-style offset */
    switchInsns = insns + offsetToSwitch;

    /* make sure the table is 32-bit aligned */
    if ((((u4) switchInsns) & 0x03) != 0) {
        LOG_VFY_METH(meth,
            "VFY: unaligned switch table: at %d, switch offset %d\n",
            curOffset, offsetToSwitch);
        return false;
    }

    switchCount = switchInsns[1];

    if ((*insns & 0xff) == OP_PACKED_SWITCH) {
        /* 0=sig, 1=count, 2/3=firstKey */
        offsetToTargets = 4;
        offsetToKeys = -1;
    } else {
        /* 0=sig, 1=count, 2..count*2 = keys */
        offsetToKeys = 2;
        offsetToTargets = 2 + 2*switchCount;
    }
    tableSize = offsetToTargets + switchCount*2;

    /* make sure the end of the switch is in range */
    if (curOffset + offsetToSwitch + tableSize > insnCount) {
        LOG_VFY_METH(meth,
            "VFY: invalid switch end: at %d, switch offset %d, end %d, count %d\n",
            curOffset, offsetToSwitch, curOffset + offsetToSwitch + tableSize,
            insnCount);
        return false;
    }

    /* for a sparse switch, verify the keys are in ascending order */
    if (offsetToKeys > 0 && switchCount > 1) {
        s4 lastKey;
        
        lastKey = switchInsns[offsetToKeys] |
                  (switchInsns[offsetToKeys+1] << 16);
        for (targ = 1; targ < switchCount; targ++) {
            s4 key = (s4) switchInsns[offsetToKeys + targ*2] |
                    (s4) (switchInsns[offsetToKeys + targ*2 +1] << 16);
            if (key <= lastKey) {
                LOG_VFY_METH(meth,
                    "VFY: invalid packed switch: last key=%d, this=%d\n",
                    lastKey, key);
                return false;
            }

            lastKey = key;
        }
    }

    /* verify each switch target */
    for (targ = 0; targ < switchCount; targ++) {
        offset = (s4) switchInsns[offsetToTargets + targ*2] |
                (s4) (switchInsns[offsetToTargets + targ*2 +1] << 16);
        absOffset = curOffset + offset;

        if (absOffset < 0 || absOffset >= insnCount ||
            !dvmInsnIsOpcode(insnFlags, absOffset))
        {
            LOG_VFY_METH(meth,
                "VFY: invalid switch target %d (-> 0x%x) at 0x%x[%d]\n",
                offset, absOffset, curOffset, targ);
            return false;
        }
        dvmInsnSetBranchTarget(insnFlags, absOffset, true);
    }

    return true;
}

/*
 * Verify an array data table.  "curOffset" is the offset of the fill-array-data
 * instruction.
 */
static bool checkArrayData(const Method* meth, int curOffset)
{
    const int insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns + curOffset;
    const u2* arrayData;
    int valueCount, valueWidth, tableSize;
    int offsetToArrayData;

    assert(curOffset >= 0 && curOffset < insnCount);

    /* make sure the start of the array data table is in range */
    offsetToArrayData = insns[1] | (((s4)insns[2]) << 16);
    if (curOffset + offsetToArrayData < 0 ||
        curOffset + offsetToArrayData + 2 >= insnCount)
    {
        LOG_VFY_METH(meth,
            "VFY: invalid array data start: at %d, data offset %d, count %d\n",
            curOffset, offsetToArrayData, insnCount);
        return false;
    }

    /* offset to array data table is a relative branch-style offset */
    arrayData = insns + offsetToArrayData;

    /* make sure the table is 32-bit aligned */
    if ((((u4) arrayData) & 0x03) != 0) {
        LOG_VFY_METH(meth,
            "VFY: unaligned array data table: at %d, data offset %d\n",
            curOffset, offsetToArrayData);
        return false;
    }

    valueWidth = arrayData[1];
    valueCount = *(u4*)(&arrayData[2]);

    tableSize = 4 + (valueWidth * valueCount + 1) / 2;

    /* make sure the end of the switch is in range */
    if (curOffset + offsetToArrayData + tableSize > insnCount) {
        LOG_VFY_METH(meth,
            "VFY: invalid array data end: at %d, data offset %d, end %d, "
            "count %d\n",
            curOffset, offsetToArrayData, 
            curOffset + offsetToArrayData + tableSize,
            insnCount);
        return false;
    }

    return true;
}

/*
 * Verify that the target of a branch instruction is valid.
 *
 * We don't expect code to jump directly into an exception handler, but
 * it's valid to do so as long as the target isn't a "move-exception"
 * instruction.  We verify that in a later stage.
 *
 * The VM spec doesn't forbid an instruction from branching to itself,
 * but the Dalvik spec declares that only certain instructions can do so.
 */
static bool checkBranchTarget(const Method* meth, InsnFlags* insnFlags,
    int curOffset, bool selfOkay)
{
    const int insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns + curOffset;
    int offset, absOffset;
    bool isConditional;

    if (!dvmGetBranchTarget(meth, insnFlags, curOffset, &offset,
            &isConditional))
        return false;

    if (!selfOkay && offset == 0) {
        LOG_VFY_METH(meth, "VFY: branch offset of zero not allowed at 0x%x\n",
            curOffset);
        return false;
    }

    /*
     * Check for 32-bit overflow.  This isn't strictly necessary if we can
     * depend on the VM to have identical "wrap-around" behavior, but
     * it's unwise to depend on that.
     */
    if (((s8) curOffset + (s8) offset) != (s8)(curOffset + offset)) {
        LOG_VFY_METH(meth, "VFY: branch target overflow 0x%x +%d\n",
            curOffset, offset);
        return false;
    }
    absOffset = curOffset + offset;
    if (absOffset < 0 || absOffset >= insnCount ||
        !dvmInsnIsOpcode(insnFlags, absOffset))
    {
        LOG_VFY_METH(meth,
            "VFY: invalid branch target %d (-> 0x%x) at 0x%x\n",
            offset, absOffset, curOffset);
        return false;
    }
    dvmInsnSetBranchTarget(insnFlags, absOffset, true);

    return true;
}

/*
 * Perform static verification on instructions.
 *
 * As a side effect, this sets the "branch target" flags in InsnFlags.
 *
 * "(CF)" items are handled during code-flow analysis.
 *
 * v3 4.10.1
 * - target of each jump and branch instruction must be valid
 * - targets of switch statements must be valid
 * - (CF) operands referencing constant pool entries must be valid
 * - (CF) operands of getfield, putfield, getstatic, putstatic must be valid
 * - (new) verify operands of "quick" field ops
 * - (CF) operands of method invocation instructions must be valid
 * - (new) verify operands of "quick" method invoke ops
 * - (CF) only invoke-direct can call a method starting with '<'
 * - (CF) <clinit> must never be called explicitly
 * - (CF) operands of instanceof, checkcast, new (and variants) must be valid
 * - new-array[-type] limited to 255 dimensions
 * - can't use "new" on an array class
 * - (?) limit dimensions in multi-array creation
 * - (CF) local variable load/store register values must be in valid range
 *
 * v3 4.11.1.2
 * - branches must be within the bounds of the code array
 * - targets of all control-flow instructions are the start of an instruction
 * - (CF) register accesses fall within range of allocated registers
 * - (N/A) access to constant pool must be of appropriate type
 * - (CF) code does not end in the middle of an instruction
 * - (CF) execution cannot fall off the end of the code
 * - (earlier) for each exception handler, the "try" area must begin and
 *   end at the start of an instruction (end can be at the end of the code)
 * - (earlier) for each exception handler, the handler must start at a valid
 *   instruction
 */
static bool verifyInstructions(const Method* meth, InsnFlags* insnFlags,
    int verifyFlags)
{
    const int insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns;
    int i, width, offset, absOffset;

    /* the start of the method is a "branch target" */
    dvmInsnSetBranchTarget(insnFlags, 0, true);

    for (i = 0; i < insnCount; /**/) {
        width = dvmInsnGetWidth(insnFlags, i);

        switch (*insns & 0xff) {
        case OP_NOP:
            /* plain no-op or switch table data; nothing to do here */
            break;

        case OP_PACKED_SWITCH:
        case OP_SPARSE_SWITCH:
            /* verify the associated table */
            if (!checkSwitchTargets(meth, insnFlags, i))
                return false;
            break;

        case OP_FILL_ARRAY_DATA:
            /* verify the associated table */
            if (!checkArrayData(meth, i))
                return false;
            break;
            
        case OP_GOTO:
        case OP_GOTO_16:
        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            /* check the destination */
            if (!checkBranchTarget(meth, insnFlags, i, false))
                return false;
            break;
        case OP_GOTO_32:
            /* check the destination; self-branch is okay */
            if (!checkBranchTarget(meth, insnFlags, i, true))
                return false;
            break;

        case OP_NEW_INSTANCE:
            if (!checkNewInstance(meth, i))
                return false;
            break;

        case OP_NEW_ARRAY:
            if (!checkNewArray(meth, i))
                return false;
            break;

        case OP_EXECUTE_INLINE:
        case OP_INVOKE_DIRECT_EMPTY:
        case OP_IGET_QUICK:
        case OP_IGET_WIDE_QUICK:
        case OP_IGET_OBJECT_QUICK:
        case OP_IPUT_QUICK:
        case OP_IPUT_WIDE_QUICK:
        case OP_IPUT_OBJECT_QUICK:
        case OP_INVOKE_VIRTUAL_QUICK:
        case OP_INVOKE_VIRTUAL_QUICK_RANGE:
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_SUPER_QUICK_RANGE:
            if ((verifyFlags & VERIFY_ALLOW_OPT_INSTRS) == 0) {
                LOG_VFY("VFY: not expecting optimized instructions\n");
                return false;
            }
            break;

        default:
            /* nothing to do */
            break;
        }

        assert(width > 0);
        i += width;
        insns += width;
    }

    /* make sure the last instruction ends at the end of the insn area */
    if (i != insnCount) {
        LOG_VFY_METH(meth,
            "VFY: code did not end when expected (end at %d, count %d)\n",
            i, insnCount);
        return false;
    }

    return true;
}

/*
 * Perform static checks on a "new-instance" instruction.  Specifically,
 * make sure the class reference isn't for an array class.
 *
 * We don't need the actual class, just a pointer to the class name.
 */
static bool checkNewInstance(const Method* meth, int insnIdx)
{
    DexFile* pDexFile = meth->clazz->pDvmDex->pDexFile;
    DecodedInstruction decInsn;
    const char* classDescriptor;

    dexDecodeInstruction(gDvm.instrFormat, meth->insns + insnIdx, &decInsn);
    classDescriptor = dexStringByTypeIdx(pDexFile, decInsn.vB); // 2nd item

    if (classDescriptor[0] != 'L') {
        LOG_VFY_METH(meth, "VFY: can't call new-instance on type '%s'\n",
            classDescriptor);
        return false;
    }

    return true;
}

/*
 * Perform static checks on a "new-array" instruction.  Specifically, make
 * sure they aren't creating an array of arrays that causes the number of
 * dimensions to exceed 255.
 */
static bool checkNewArray(const Method* meth, int insnIdx)
{
    DexFile* pDexFile = meth->clazz->pDvmDex->pDexFile;
    DecodedInstruction decInsn;
    const char* classDescriptor;

    dexDecodeInstruction(gDvm.instrFormat, meth->insns + insnIdx, &decInsn);
    classDescriptor = dexStringByTypeIdx(pDexFile, decInsn.vC); // 3rd item

    int bracketCount = 0;
    const char* cp = classDescriptor;
    while (*cp++ == '[')
        bracketCount++;

    if (bracketCount == 0) {
        /* The given class must be an array type. */
        LOG_VFY_METH(meth, "VFY: can't new-array class '%s' (not an array)\n",
            classDescriptor);
        return false;
    } else if (bracketCount > 255) {
        /* It is illegal to create an array of more than 255 dimensions. */
        LOG_VFY_METH(meth, "VFY: can't new-array class '%s' (exceeds limit)\n",
            classDescriptor);
        return false;
    }

    return true;
}

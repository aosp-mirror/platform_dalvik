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
 * Dalvik verification subroutines.
 */
#include "Dalvik.h"
#include "analysis/CodeVerify.h"
#include "libdex/DexCatch.h"
#include "libdex/InstrUtils.h"


/*
 * Compute the width of the instruction at each address in the instruction
 * stream.  Addresses that are in the middle of an instruction, or that
 * are part of switch table data, are not set (so the caller should probably
 * initialize "insnFlags" to zero).
 *
 * If "pNewInstanceCount" is not NULL, it will be set to the number of
 * new-instance instructions in the method.
 *
 * Performs some static checks, notably:
 * - opcode of first instruction begins at index 0
 * - only documented instructions may appear
 * - each instruction follows the last
 * - last byte of last instruction is at (code_length-1)
 *
 * Logs an error and returns "false" on failure.
 */
bool dvmComputeCodeWidths(const Method* meth, InsnFlags* insnFlags,
    int* pNewInstanceCount)
{
    size_t insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns;
    bool result = false;
    int newInstanceCount = 0;
    int i;


    for (i = 0; i < (int) insnCount; /**/) {
        size_t width = dexGetInstrOrTableWidthAbs(gDvm.instrWidth, insns);
        if (width == 0) {
            LOG_VFY_METH(meth,
                "VFY: invalid post-opt instruction (0x%04x)\n", *insns);
            goto bail;
        }

        if ((*insns & 0xff) == OP_NEW_INSTANCE)
            newInstanceCount++;

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
    if (pNewInstanceCount != NULL)
        *pNewInstanceCount = newInstanceCount;

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
bool dvmSetTryFlags(const Method* meth, InsnFlags* insnFlags)
{
    u4 insnsSize = dvmGetMethodInsnsSize(meth);
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
bool dvmCheckSwitchTargets(const Method* meth, InsnFlags* insnFlags,
    int curOffset)
{
    const s4 insnCount = dvmGetMethodInsnsSize(meth);
    const u2* insns = meth->insns + curOffset;
    const u2* switchInsns;
    u2 expectedSignature;
    u4 switchCount, tableSize;
    s4 offsetToSwitch, offsetToKeys, offsetToTargets;
    s4 offset, absOffset;
    u4 targ;

    assert(curOffset >= 0 && curOffset < insnCount);

    /* make sure the start of the switch is in range */
    offsetToSwitch = insns[1] | ((s4) insns[2]) << 16;
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
        expectedSignature = kPackedSwitchSignature;
    } else {
        /* 0=sig, 1=count, 2..count*2 = keys */
        offsetToKeys = 2;
        offsetToTargets = 2 + 2*switchCount;
        expectedSignature = kSparseSwitchSignature;
    }
    tableSize = offsetToTargets + switchCount*2;

    if (switchInsns[0] != expectedSignature) {
        LOG_VFY_METH(meth,
            "VFY: wrong signature for switch table (0x%04x, wanted 0x%04x)\n",
            switchInsns[0], expectedSignature);
        return false;
    }

    /* make sure the end of the switch is in range */
    if (curOffset + offsetToSwitch + tableSize > (u4) insnCount) {
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
 * Verify that the target of a branch instruction is valid.
 *
 * We don't expect code to jump directly into an exception handler, but
 * it's valid to do so as long as the target isn't a "move-exception"
 * instruction.  We verify that in a later stage.
 *
 * The VM spec doesn't forbid an instruction from branching to itself,
 * but the Dalvik spec declares that only certain instructions can do so.
 */
bool dvmCheckBranchTarget(const Method* meth, InsnFlags* insnFlags,
    int curOffset, bool selfOkay)
{
    const int insnCount = dvmGetMethodInsnsSize(meth);
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
 * Output a code verifier warning message.  For the pre-verifier it's not
 * a big deal if something fails (and it may even be expected), but if
 * we're doing just-in-time verification it's significant.
 */
void dvmLogVerifyFailure(const Method* meth, const char* format, ...)
{
    va_list ap;
    int logLevel;

    if (gDvm.optimizing) {
        return;
        //logLevel = ANDROID_LOG_DEBUG;
    } else {
        logLevel = ANDROID_LOG_WARN;
    }

    va_start(ap, format);
    LOG_PRI_VA(logLevel, LOG_TAG, format, ap);
    if (meth != NULL) {
        char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
        LOG_PRI(logLevel, LOG_TAG, "VFY:  rejected %s.%s %s\n",
            meth->clazz->descriptor, meth->name, desc);
        free(desc);
    }
}

/*
 * Show a relatively human-readable message describing the failure to
 * resolve a class.
 *
 * TODO: this is somewhat misleading when resolution fails because of
 * illegal access rather than nonexistent class.
 */
void dvmLogUnableToResolveClass(const char* missingClassDescr,
    const Method* meth)
{
    if (gDvm.optimizing)
        return;

    char* dotMissingClass = dvmDescriptorToDot(missingClassDescr);
    char* dotFromClass = dvmDescriptorToDot(meth->clazz->descriptor);
    //char* methodDescr = dexProtoCopyMethodDescriptor(&meth->prototype);

    LOGE("Could not find class '%s', referenced from method %s.%s\n",
        dotMissingClass, dotFromClass, meth->name/*, methodDescr*/);

    free(dotMissingClass);
    free(dotFromClass);
    //free(methodDescr);
}

/*
 * Extract the relative offset from a branch instruction.
 *
 * Returns "false" on failure (e.g. this isn't a branch instruction).
 */
bool dvmGetBranchTarget(const Method* meth, InsnFlags* insnFlags,
    int curOffset, int* pOffset, bool* pConditional)
{
    const u2* insns = meth->insns + curOffset;

    switch (*insns & 0xff) {
    case OP_GOTO:
        *pOffset = ((s2) *insns) >> 8;
        *pConditional = false;
        break;
    case OP_GOTO_32:
        *pOffset = insns[1] | (((u4) insns[2]) << 16);
        *pConditional = false;
        break;
    case OP_GOTO_16:
        *pOffset = (s2) insns[1];
        *pConditional = false;
        break;
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
        *pOffset = (s2) insns[1];
        *pConditional = true;
        break;
    default:
        return false;
        break;
    }

    return true;
}

/*
 * Given a 32-bit constant, return the most-restricted RegType enum entry
 * that can hold the value.
 */
char dvmDetermineCat1Const(s4 value)
{
    if (value < -32768)
        return kRegTypeInteger;
    else if (value < -128)
        return kRegTypeShort;
    else if (value < 0)
        return kRegTypeByte;
    else if (value == 0)
        return kRegTypeZero;
    else if (value == 1)
        return kRegTypeOne;
    else if (value < 128)
        return kRegTypePosByte;
    else if (value < 32768)
        return kRegTypePosShort;
    else if (value < 65536)
        return kRegTypeChar;
    else
        return kRegTypeInteger;
}

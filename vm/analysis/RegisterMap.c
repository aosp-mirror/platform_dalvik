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
 * This code generate "register maps" for Dalvik bytecode.  In a stack-based
 * VM we would call these "stack maps".  They are used to increase the
 * precision in the garbage collector when finding references in the
 * interpreter call stack.
 */
#include "Dalvik.h"
#include "analysis/CodeVerify.h"
#include "analysis/RegisterMap.h"
#include "libdex/DexCatch.h"
#include "libdex/InstrUtils.h"


/*
Notes on RegisterMap vs. verification

Much of this is redundant with the bytecode verifier.  Unfortunately we
can't do this at verification time unless we're willing to store the
results in the optimized DEX file, which increases their size by about 25%
unless we use compression, and for performance reasons we don't want to
just re-run the verifier.

Both type-precise and live-precise information can be generated knowing
only whether or not a register holds a reference.  We don't need to
know what kind of reference or whether the object has been initialized.
Not only can we skip many of the fancy steps in the verifier, we can
initialize from simpler sources, e.g. the initial registers and return
type are set from the "shorty" signature rather than the full signature.

The short-term storage needs are different; for example, the verifier
stores 4-byte RegType values for every address that can be a branch
target, while we store 1-byte SRegType values for every address that
can be a GC point.  There are many more GC points than branch targets.
We could use a common data type here, but for larger methods it can mean
the difference between 300KB and 1.2MB.

*/

/*
 * This is like RegType in the verifier, but simplified.  It holds a value
 * from the reg type enum, or kRegTypeReference.
 */
typedef u1 SRegType;
#define kRegTypeReference kRegTypeMAX

/*
 * We need an extra "pseudo register" to hold the return type briefly.  It
 * can be category 1 or 2, so we need two slots.
 */
#define kExtraRegs  2
#define RESULT_REGISTER(_insnRegCount)  (_insnRegCount)

/*
 * Working state.
 */
typedef struct WorkState {
    /*
     * The method we're working on.
     */
    const Method* method;

    /*
     * Number of instructions in the method.
     */
    int         insnsSize;

    /*
     * Number of registers we track for each instruction.  This is equal
     * to the method's declared "registersSize" plus kExtraRegs.
     */
    int         insnRegCount;

    /*
     * Instruction widths and flags, one entry per code unit.
     */
    InsnFlags*  insnFlags;

    /*
     * Array of SRegType arrays, one entry per code unit.  We only need
     * to create an entry when an instruction starts at this address.
     * We can further reduce this to instructions that are GC points.
     *
     * We could just go ahead and allocate one per code unit, but for
     * larger methods that can represent a significant bit of short-term
     * storage.
     */
    SRegType**  addrRegs;

    /*
     * A single large alloc, with all of the storage needed for addrRegs.
     */
    SRegType*   regAlloc;
} WorkState;

// fwd
static bool generateMap(WorkState* pState, RegisterMap* pMap);
static bool analyzeMethod(WorkState* pState);
static bool handleInstruction(WorkState* pState, SRegType* workRegs,\
    int insnIdx, int* pStartGuess);
static void updateRegisters(WorkState* pState, int nextInsn,\
    const SRegType* workRegs);


/*
 * Set instruction flags.
 */
static bool setInsnFlags(WorkState* pState, int* pGcPointCount)
{
    const Method* meth = pState->method;
    InsnFlags* insnFlags = pState->insnFlags;
    int insnsSize = pState->insnsSize;
    const u2* insns = meth->insns;
    int gcPointCount = 0;
    int offset;

    /* set the widths */
    if (!dvmComputeCodeWidths(meth, pState->insnFlags, NULL))
        return false;

    /* mark "try" regions and exception handler branch targets */
    if (!dvmSetTryFlags(meth, pState->insnFlags))
        return false;

    /* the start of the method is a "branch target" */
    dvmInsnSetBranchTarget(insnFlags, 0, true);

    /*
     * Run through the instructions, looking for switches and branches.
     * Mark their targets.
     *
     * We don't really need to "check" these instructions -- the verifier
     * already did that -- but the additional overhead isn't significant
     * enough to warrant making a second copy of the "Check" function.
     *
     * Mark and count GC points while we're at it.
     */
    for (offset = 0; offset < insnsSize; offset++) {
        static int gcMask = kInstrCanBranch | kInstrCanSwitch |
            kInstrCanThrow | kInstrCanReturn;
        u1 opcode = insns[offset] & 0xff;
        InstructionFlags opFlags = dexGetInstrFlags(gDvm.instrFlags, opcode);

        if (opFlags & kInstrCanBranch) {
            if (!dvmCheckBranchTarget(meth, insnFlags, offset, true))
                return false;
        }
        if (opFlags & kInstrCanSwitch) {
            if (!dvmCheckSwitchTargets(meth, insnFlags, offset))
                return false;
        }

        if ((opFlags & gcMask) != 0) {
            dvmInsnSetGcPoint(pState->insnFlags, offset, true);
            gcPointCount++;
        }
    }

    *pGcPointCount = gcPointCount;
    return true;
}

/*
 * Generate the register map for a method.
 *
 * Returns a pointer to newly-allocated storage.
 */
RegisterMap* dvmGenerateRegisterMap(const Method* meth)
{
    WorkState* pState = NULL;
    RegisterMap* pMap = NULL;
    RegisterMap* result = NULL;
    SRegType* regPtr;

    pState = (WorkState*) calloc(1, sizeof(WorkState));
    if (pState == NULL)
        goto bail;

    pMap = (RegisterMap*) calloc(1, sizeof(RegisterMap));
    if (pMap == NULL)
        goto bail;

    pState->method = meth;
    pState->insnsSize = dvmGetMethodInsnsSize(meth);
    pState->insnRegCount = meth->registersSize + kExtraRegs;

    pState->insnFlags = calloc(sizeof(InsnFlags), pState->insnsSize);
    pState->addrRegs = calloc(sizeof(SRegType*), pState->insnsSize);

    /*
     * Set flags on instructions, and calculate the number of code units
     * that happen to be GC points.
     */
    int gcPointCount;
    if (!setInsnFlags(pState, &gcPointCount))
        goto bail;

    if (gcPointCount == 0) {
        /* the method doesn't allocate or call, and never returns? unlikely */
        LOG_VFY_METH(meth, "Found do-nothing method\n");
        goto bail;
    }

    pState->regAlloc = (SRegType*)
        calloc(sizeof(SRegType), pState->insnsSize * gcPointCount);
    regPtr = pState->regAlloc;

    /*
     * For each instruction that is a GC point, set a pointer into the
     * regAlloc buffer.
     */
    int offset;
    for (offset = 0; offset < pState->insnsSize; offset++) {
        if (dvmInsnIsGcPoint(pState->insnFlags, offset)) {
            pState->addrRegs[offset] = regPtr;
            regPtr += pState->insnRegCount;
        }
    }
    assert(regPtr - pState->regAlloc == pState->insnsSize * gcPointCount);
    assert(pState->addrRegs[0] != NULL);

    /*
     * Compute the register map.
     */
    if (!generateMap(pState, pMap))
        goto bail;

    /* success */
    result = pMap;
    pMap = NULL;

bail:
    if (pState != NULL) {
        free(pState->insnFlags);
        free(pState->addrRegs);
        free(pState->regAlloc);
        free(pState);
    }
    if (pMap != NULL)
        dvmFreeRegisterMap(pMap);
    return result;
}

/*
 * Release the storage associated with a RegisterMap.
 */
void dvmFreeRegisterMap(RegisterMap* pMap)
{
    if (pMap == NULL)
        return;
}


/*
 * Create the RegisterMap using the provided state.
 */
static bool generateMap(WorkState* pState, RegisterMap* pMap)
{
    bool result = false;

    /*
     * Analyze the method and store the results in WorkState.
     */
    if (!analyzeMethod(pState))
        goto bail;

    /*
     * Convert the analyzed data into a RegisterMap.
     */
    // TODO

    result = true;

bail:
    return result;
}

/*
 * Set the register types for the method arguments.  We can pull the values
 * out of the "shorty" signature.
 */
static bool setTypesFromSignature(WorkState* pState)
{
    const Method* meth = pState->method;
    int argReg = meth->registersSize - meth->insSize;   /* first arg */
    SRegType* pRegs = pState->addrRegs[0];
    SRegType* pCurReg = &pRegs[argReg];
    const char* ccp;

    /*
     * Include "this" pointer, if appropriate.
     */
    if (!dvmIsStaticMethod(meth)) {
        *pCurReg++ = kRegTypeReference;
    }

    ccp = meth->shorty +1;      /* skip first byte, which holds return type */
    while (*ccp != 0) {
        switch (*ccp) {
        case 'L':
        case '[':
            *pCurReg++ = kRegTypeReference;
            break;
        case 'Z':
            *pCurReg++ = kRegTypeBoolean;
            break;
        case 'C':
            *pCurReg++ = kRegTypeChar;
            break;
        case 'B':
            *pCurReg++ = kRegTypeByte;
            break;
        case 'I':
            *pCurReg++ = kRegTypeInteger;
            break;
        case 'S':
            *pCurReg++ = kRegTypeShort;
            break;
        case 'F':
            *pCurReg++ = kRegTypeFloat;
            break;
        case 'D':
            *pCurReg++ = kRegTypeDoubleLo;
            *pCurReg++ = kRegTypeDoubleHi;
            break;
        case 'J':
            *pCurReg++ = kRegTypeLongLo;
            *pCurReg++ = kRegTypeLongHi;
            break;
        default:
            assert(false);
            return false;
        }
    }

    assert(pCurReg - pRegs == meth->insSize);
    return true;
}

/*
 * Find the start of the register set for the specified instruction in
 * the current method.
 */
static inline SRegType* getRegisterLine(const WorkState* pState, int insnIdx)
{
    return pState->addrRegs[insnIdx];
}

/*
 * Copy a set of registers.
 */
static inline void copyRegisters(SRegType* dst, const SRegType* src,
    int numRegs)
{
    memcpy(dst, src, numRegs * sizeof(SRegType));
}

/*
 * Compare a set of registers.  Returns 0 if they match.
 */
static inline int compareRegisters(const SRegType* src1, const SRegType* src2,
    int numRegs)
{
    return memcmp(src1, src2, numRegs * sizeof(SRegType));
}

/*
 * Run through the instructions repeatedly until we have exercised all
 * possible paths.
 */
static bool analyzeMethod(WorkState* pState)
{
    const Method* meth = pState->method;
    SRegType workRegs[pState->insnRegCount];
    InsnFlags* insnFlags = pState->insnFlags;
    int insnsSize = pState->insnsSize;
    int insnIdx, startGuess;
    bool result = false;

    /*
     * Initialize the types of the registers that correspond to method
     * arguments.
     */
    if (!setTypesFromSignature(pState))
        goto bail;

    /*
     * Mark the first instruction as "changed".
     */
    dvmInsnSetChanged(insnFlags, 0, true);
    startGuess = 0;

    if (true) {
        IF_LOGI() {
            char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
            LOGI("Now mapping: %s.%s %s (ins=%d regs=%d)\n",
                meth->clazz->descriptor, meth->name, desc,
                meth->insSize, meth->registersSize);
            LOGI(" ------ [0    4    8    12   16   20   24   28   32   36\n");
            free(desc);
        }
    }

    /*
     * Continue until no instructions are marked "changed".
     */
    while (true) {
        /*
         * Find the first marked one.  Use "startGuess" as a way to find
         * one quickly.
         */
        for (insnIdx = startGuess; insnIdx < insnsSize; insnIdx++) {
            if (dvmInsnIsChanged(insnFlags, insnIdx))
                break;
        }

        if (insnIdx == insnsSize) {
            if (startGuess != 0) {
                /* try again, starting from the top */
                startGuess = 0;
                continue;
            } else {
                /* all flags are clear */
                break;
            }
        }

        /*
         * We carry the working set of registers from instruction to
         * instruction.  If this address can be the target of a branch
         * (or throw) instruction, or if we're skipping around chasing
         * "changed" flags, we need to load the set of registers from
         * the table.
         *
         * Because we always prefer to continue on to the next instruction,
         * we should never have a situation where we have a stray
         * "changed" flag set on an instruction that isn't a branch target.
         */
        if (dvmInsnIsBranchTarget(insnFlags, insnIdx)) {
            SRegType* insnRegs = getRegisterLine(pState, insnIdx);
            assert(insnRegs != NULL);
            copyRegisters(workRegs, insnRegs, pState->insnRegCount);

        } else {
#ifndef NDEBUG
            /*
             * Sanity check: retrieve the stored register line (assuming
             * a full table) and make sure it actually matches.
             */
            SRegType* insnRegs = getRegisterLine(pState, insnIdx);
            if (insnRegs != NULL &&
                compareRegisters(workRegs, insnRegs, pState->insnRegCount) != 0)
            {
                char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
                LOG_VFY("HUH? workRegs diverged in %s.%s %s\n",
                        meth->clazz->descriptor, meth->name, desc);
                free(desc);
            }
#endif
        }

        /*
         * Update the register sets altered by this instruction.
         */
        if (!handleInstruction(pState, workRegs, insnIdx, &startGuess)) {
            goto bail;
        }

        dvmInsnSetVisited(insnFlags, insnIdx, true);
        dvmInsnSetChanged(insnFlags, insnIdx, false);
    }

    // TODO - add dead code scan to help validate this code?

    result = true;

bail:
    return result;
}

/*
 * Decode the specified instruction and update the register info.
 */
static bool handleInstruction(WorkState* pState, SRegType* workRegs,
    int insnIdx, int* pStartGuess)
{
    const Method* meth = pState->method;
    const u2* insns = meth->insns + insnIdx;
    InsnFlags* insnFlags = pState->insnFlags;
    bool result = false;

    /*
     * Once we finish decoding the instruction, we need to figure out where
     * we can go from here.  There are three possible ways to transfer
     * control to another statement:
     *
     * (1) Continue to the next instruction.  Applies to all but
     *     unconditional branches, method returns, and exception throws.
     * (2) Branch to one or more possible locations.  Applies to branches
     *     and switch statements.
     * (3) Exception handlers.  Applies to any instruction that can
     *     throw an exception that is handled by an encompassing "try"
     *     block.  (We simplify this to be any instruction that can
     *     throw any exception.)
     *
     * We can also return, in which case there is no successor instruction
     * from this point.
     *
     * The behavior can be determined from the InstrFlags.
     */
    DecodedInstruction decInsn;
    SRegType entryRegs[pState->insnRegCount];
    bool justSetResult = false;
    int branchTarget = 0;

    dexDecodeInstruction(gDvm.instrFormat, insns, &decInsn);
    const int nextFlags = dexGetInstrFlags(gDvm.instrFlags, decInsn.opCode);


    /*
     * Make a copy of the previous register state.  If the instruction
     * throws an exception, we merge *this* into the destination rather
     * than workRegs, because we don't want the result from the "successful"
     * code path (e.g. a check-cast that "improves" a type) to be visible
     * to the exception handler.
     */
    if ((nextFlags & kInstrCanThrow) != 0 && dvmInsnIsInTry(insnFlags, insnIdx))
    {
        copyRegisters(entryRegs, workRegs, pState->insnRegCount);
    }

    switch (decInsn.opCode) {
    case OP_NOP:
        break;

    default: break; // TODO: fill this in

    /*
     * DO NOT add a "default" clause here.  Without it the compiler will
     * complain if an instruction is missing (which is desirable).
     */
    }


    /*
     * If we didn't just set the result register, clear it out.  This
     * isn't so important here, but does help ensure that our output matches
     * the verifier.
     */
    if (!justSetResult) {
        int reg = RESULT_REGISTER(pState->insnRegCount);
        workRegs[reg] = workRegs[reg+1] = kRegTypeUnknown;
    }

    /*
     * Handle "continue".  Tag the next consecutive instruction.
     */
    if ((nextFlags & kInstrCanContinue) != 0) {
        int insnWidth = dvmInsnGetWidth(insnFlags, insnIdx);

        /*
         * We want to update the registers and set the "changed" flag on the
         * next instruction (if necessary).  We aren't storing register
         * changes for all addresses, so for non-GC-point targets we just
         * compare "entry" vs. "work" to see if we've changed anything.
         */
        if (getRegisterLine(pState, insnIdx+insnWidth) != NULL) {
            updateRegisters(pState, insnIdx+insnWidth, workRegs);
        } else {
            /* if not yet visited, or regs were updated, set "changed" */
            if (!dvmInsnIsVisited(insnFlags, insnIdx+insnWidth) ||
                compareRegisters(workRegs, entryRegs,
                    pState->insnRegCount) != 0)
            {
                dvmInsnSetChanged(insnFlags, insnIdx+insnWidth, true);
            }
        }
    }

    /*
     * Handle "branch".  Tag the branch target.
     */
    if ((nextFlags & kInstrCanBranch) != 0) {
        bool isConditional;

        dvmGetBranchTarget(meth, insnFlags, insnIdx, &branchTarget,
                &isConditional);
        assert(isConditional || (nextFlags & kInstrCanContinue) == 0);
        assert(!isConditional || (nextFlags & kInstrCanContinue) != 0);

        updateRegisters(pState, insnIdx+branchTarget, workRegs);
    }

    /*
     * Handle "switch".  Tag all possible branch targets.
     */
    if ((nextFlags & kInstrCanSwitch) != 0) {
        int offsetToSwitch = insns[1] | (((s4)insns[2]) << 16);
        const u2* switchInsns = insns + offsetToSwitch;
        int switchCount = switchInsns[1];
        int offsetToTargets, targ;

        if ((*insns & 0xff) == OP_PACKED_SWITCH) {
            /* 0=sig, 1=count, 2/3=firstKey */
            offsetToTargets = 4;
        } else {
            /* 0=sig, 1=count, 2..count*2 = keys */
            assert((*insns & 0xff) == OP_SPARSE_SWITCH);
            offsetToTargets = 2 + 2*switchCount;
        }

        /* verify each switch target */
        for (targ = 0; targ < switchCount; targ++) {
            int offset, absOffset;

            /* offsets are 32-bit, and only partly endian-swapped */
            offset = switchInsns[offsetToTargets + targ*2] |
                     (((s4) switchInsns[offsetToTargets + targ*2 +1]) << 16);
            absOffset = insnIdx + offset;
            assert(absOffset >= 0 && absOffset < pState->insnsSize);

            updateRegisters(pState, absOffset, workRegs);
        }
    }

    /*
     * Handle instructions that can throw and that are sitting in a
     * "try" block.  (If they're not in a "try" block when they throw,
     * control transfers out of the method.)
     */
    if ((nextFlags & kInstrCanThrow) != 0 && dvmInsnIsInTry(insnFlags, insnIdx))
    {
        DexFile* pDexFile = meth->clazz->pDvmDex->pDexFile;
        const DexCode* pCode = dvmGetMethodCode(meth);
        DexCatchIterator iterator;

        if (dexFindCatchHandler(&iterator, pCode, insnIdx)) {
            while (true) {
                DexCatchHandler* handler = dexCatchIteratorNext(&iterator);
                if (handler == NULL)
                    break;

                /* note we use entryRegs, not workRegs */
                updateRegisters(pState, handler->address, entryRegs);
            }
        }
    }

    /*
     * Update startGuess.  Advance to the next instruction of that's
     * possible, otherwise use the branch target if one was found.  If
     * neither of those exists we're in a return or throw; leave startGuess
     * alone and let the caller sort it out.
     */
    if ((nextFlags & kInstrCanContinue) != 0) {
        *pStartGuess = insnIdx + dvmInsnGetWidth(insnFlags, insnIdx);
    } else if ((nextFlags & kInstrCanBranch) != 0) {
        /* we're still okay if branchTarget is zero */
        *pStartGuess = insnIdx + branchTarget;
    }

    assert(*pStartGuess >= 0 && *pStartGuess < pState->insnsSize &&
        dvmInsnGetWidth(insnFlags, *pStartGuess) != 0);

    result = true;

bail:
    return result;
}


/*
 * Merge two SRegType values.
 *
 * Sets "*pChanged" to "true" if the result doesn't match "type1".
 */
static SRegType mergeTypes(SRegType type1, SRegType type2, bool* pChanged)
{
    SRegType result;

    /*
     * Check for trivial case so we don't have to hit memory.
     */
    if (type1 == type2)
        return type1;

    /*
     * Use the table if we can, and reject any attempts to merge something
     * from the table with a reference type.
     *
     * The uninitialized table entry at index zero *will* show up as a
     * simple kRegTypeUninit value.  Since this cannot be merged with
     * anything but itself, the rules do the right thing.
     */
    if (type1 < kRegTypeMAX) {
        if (type2 < kRegTypeMAX) {
            result = gDvmMergeTab[type1][type2];
        } else {
            /* simple + reference == conflict, usually */
            if (type1 == kRegTypeZero)
                result = type2;
            else
                result = kRegTypeConflict;
        }
    } else {
        if (type2 < kRegTypeMAX) {
            /* reference + simple == conflict, usually */
            if (type2 == kRegTypeZero)
                result = type1;
            else
                result = kRegTypeConflict;
        } else {
            /* merging two references */
            assert(type1 == type2);
            result = type1;
        }
    }

    if (result != type1)
        *pChanged = true;
    return result;
}

/*
 * Control can transfer to "nextInsn".
 *
 * Merge the registers from "workRegs" into "addrRegs" at "nextInsn", and
 * set the "changed" flag on the target address if the registers have changed.
 */
static void updateRegisters(WorkState* pState, int nextInsn,
    const SRegType* workRegs)
{
    const Method* meth = pState->method;
    InsnFlags* insnFlags = pState->insnFlags;
    const int insnRegCount = pState->insnRegCount;
    SRegType* targetRegs = getRegisterLine(pState, nextInsn);

    if (!dvmInsnIsVisitedOrChanged(insnFlags, nextInsn)) {
        /*
         * We haven't processed this instruction before, and we haven't
         * touched the registers here, so there's nothing to "merge".  Copy
         * the registers over and mark it as changed.  (This is the only
         * way a register can transition out of "unknown", so this is not
         * just an optimization.)
         */
        LOGVV("COPY into 0x%04x\n", nextInsn);
        copyRegisters(targetRegs, workRegs, insnRegCount);
        dvmInsnSetChanged(insnFlags, nextInsn, true);
    } else {
        /* merge registers, set Changed only if different */
        LOGVV("MERGE into 0x%04x\n", nextInsn);
        bool changed = false;
        int i;

        for (i = 0; i < insnRegCount; i++) {
            targetRegs[i] = mergeTypes(targetRegs[i], workRegs[i], &changed);
        }

        if (changed)
            dvmInsnSetChanged(insnFlags, nextInsn, true);
    }
}


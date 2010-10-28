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

#include "Dalvik.h"
#include "libdex/DexOpcodes.h"
#include "libdex/DexCatch.h"
#include "interp/Jit.h"
#include "CompilerInternals.h"
#include "Dataflow.h"

static inline bool contentIsInsn(const u2 *codePtr) {
    u2 instr = *codePtr;
    Opcode opcode = instr & 0xff;

    /*
     * Since the low 8-bit in metadata may look like OP_NOP, we need to check
     * both the low and whole sub-word to determine whether it is code or data.
     */
    return (opcode != OP_NOP || instr == 0);
}

/*
 * Parse an instruction, return the length of the instruction
 */
static inline int parseInsn(const u2 *codePtr, DecodedInstruction *decInsn,
                            bool printMe)
{
    // Don't parse instruction data
    if (!contentIsInsn(codePtr)) {
        return 0;
    }

    u2 instr = *codePtr;
    Opcode opcode = dexOpcodeFromCodeUnit(instr);

    dexDecodeInstruction(codePtr, decInsn);
    if (printMe) {
        char *decodedString = dvmCompilerGetDalvikDisassembly(decInsn, NULL);
        LOGD("%p: %#06x %s\n", codePtr, opcode, decodedString);
    }
    return dexGetWidthFromOpcode(opcode);
}

#define UNKNOWN_TARGET 0xffffffff

/*
 * Identify block-ending instructions and collect supplemental information
 * regarding the following instructions.
 */
static inline bool findBlockBoundary(const Method *caller, MIR *insn,
                                     unsigned int curOffset,
                                     unsigned int *target, bool *isInvoke,
                                     const Method **callee)
{
    switch (insn->dalvikInsn.opcode) {
        /* Target is not compile-time constant */
        case OP_RETURN_VOID:
        case OP_RETURN:
        case OP_RETURN_WIDE:
        case OP_RETURN_OBJECT:
        case OP_THROW:
          *target = UNKNOWN_TARGET;
          break;
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_VIRTUAL_RANGE:
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_INTERFACE_RANGE:
        case OP_INVOKE_VIRTUAL_QUICK:
        case OP_INVOKE_VIRTUAL_QUICK_RANGE:
            *isInvoke = true;
            break;
        case OP_INVOKE_SUPER:
        case OP_INVOKE_SUPER_RANGE: {
            int mIndex = caller->clazz->pDvmDex->
                pResMethods[insn->dalvikInsn.vB]->methodIndex;
            const Method *calleeMethod =
                caller->clazz->super->vtable[mIndex];

            if (calleeMethod && !dvmIsNativeMethod(calleeMethod)) {
                *target = (unsigned int) calleeMethod->insns;
            }
            *isInvoke = true;
            *callee = calleeMethod;
            break;
        }
        case OP_INVOKE_STATIC:
        case OP_INVOKE_STATIC_RANGE: {
            const Method *calleeMethod =
                caller->clazz->pDvmDex->pResMethods[insn->dalvikInsn.vB];

            if (calleeMethod && !dvmIsNativeMethod(calleeMethod)) {
                *target = (unsigned int) calleeMethod->insns;
            }
            *isInvoke = true;
            *callee = calleeMethod;
            break;
        }
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_SUPER_QUICK_RANGE: {
            const Method *calleeMethod =
                caller->clazz->super->vtable[insn->dalvikInsn.vB];

            if (calleeMethod && !dvmIsNativeMethod(calleeMethod)) {
                *target = (unsigned int) calleeMethod->insns;
            }
            *isInvoke = true;
            *callee = calleeMethod;
            break;
        }
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_DIRECT_RANGE: {
            const Method *calleeMethod =
                caller->clazz->pDvmDex->pResMethods[insn->dalvikInsn.vB];
            if (calleeMethod && !dvmIsNativeMethod(calleeMethod)) {
                *target = (unsigned int) calleeMethod->insns;
            }
            *isInvoke = true;
            *callee = calleeMethod;
            break;
        }
        case OP_GOTO:
        case OP_GOTO_16:
        case OP_GOTO_32:
            *target = curOffset + (int) insn->dalvikInsn.vA;
            break;

        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
            *target = curOffset + (int) insn->dalvikInsn.vC;
            break;

        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            *target = curOffset + (int) insn->dalvikInsn.vB;
            break;

        default:
            return false;
    }
    return true;
}

static inline bool isGoto(MIR *insn)
{
    switch (insn->dalvikInsn.opcode) {
        case OP_GOTO:
        case OP_GOTO_16:
        case OP_GOTO_32:
            return true;
        default:
            return false;
    }
}

/*
 * Identify unconditional branch instructions
 */
static inline bool isUnconditionalBranch(MIR *insn)
{
    switch (insn->dalvikInsn.opcode) {
        case OP_RETURN_VOID:
        case OP_RETURN:
        case OP_RETURN_WIDE:
        case OP_RETURN_OBJECT:
            return true;
        default:
            return isGoto(insn);
    }
}

/*
 * dvmHashTableLookup() callback
 */
static int compareMethod(const CompilerMethodStats *m1,
                         const CompilerMethodStats *m2)
{
    return (int) m1->method - (int) m2->method;
}

/*
 * Analyze the body of the method to collect high-level information regarding
 * inlining:
 * - is empty method?
 * - is getter/setter?
 * - can throw exception?
 *
 * Currently the inliner only handles getters and setters. When its capability
 * becomes more sophisticated more information will be retrieved here.
 */
static int analyzeInlineTarget(DecodedInstruction *dalvikInsn, int attributes,
                               int offset)
{
    int flags = dexGetFlagsFromOpcode(dalvikInsn->opcode);
    int dalvikOpcode = dalvikInsn->opcode;

    if (flags & kInstrInvoke) {
        attributes &= ~METHOD_IS_LEAF;
    }

    if (!(flags & kInstrCanReturn)) {
        if (!(dvmCompilerDataFlowAttributes[dalvikOpcode] &
              DF_IS_GETTER)) {
            attributes &= ~METHOD_IS_GETTER;
        }
        if (!(dvmCompilerDataFlowAttributes[dalvikOpcode] &
              DF_IS_SETTER)) {
            attributes &= ~METHOD_IS_SETTER;
        }
    }

    /*
     * The expected instruction sequence is setter will never return value and
     * getter will also do. Clear the bits if the behavior is discovered
     * otherwise.
     */
    if (flags & kInstrCanReturn) {
        if (dalvikOpcode == OP_RETURN_VOID) {
            attributes &= ~METHOD_IS_GETTER;
        }
        else {
            attributes &= ~METHOD_IS_SETTER;
        }
    }

    if (flags & kInstrCanThrow) {
        attributes &= ~METHOD_IS_THROW_FREE;
    }

    if (offset == 0 && dalvikOpcode == OP_RETURN_VOID) {
        attributes |= METHOD_IS_EMPTY;
    }

    /*
     * Check if this opcode is selected for single stepping.
     * If so, don't inline the callee as there is no stack frame for the
     * interpreter to single-step through the instruction.
     */
    if (SINGLE_STEP_OP(dalvikOpcode)) {
        attributes &= ~(METHOD_IS_GETTER | METHOD_IS_SETTER);
    }

    return attributes;
}

/*
 * Analyze each method whose traces are ever compiled. Collect a variety of
 * statistics like the ratio of exercised vs overall code and code bloat
 * ratios. If isCallee is true, also analyze each instruction in more details
 * to see if it is suitable for inlining.
 */
CompilerMethodStats *dvmCompilerAnalyzeMethodBody(const Method *method,
                                                  bool isCallee)
{
    const DexCode *dexCode = dvmGetMethodCode(method);
    const u2 *codePtr = dexCode->insns;
    const u2 *codeEnd = dexCode->insns + dexCode->insnsSize;
    int insnSize = 0;
    int hashValue = dvmComputeUtf8Hash(method->name);

    CompilerMethodStats dummyMethodEntry; // For hash table lookup
    CompilerMethodStats *realMethodEntry; // For hash table storage

    /* For lookup only */
    dummyMethodEntry.method = method;
    realMethodEntry =
        (CompilerMethodStats *) dvmHashTableLookup(gDvmJit.methodStatsTable, hashValue,
                                                   &dummyMethodEntry,
                                                   (HashCompareFunc) compareMethod,
                                                   false);

    /* This method has never been analyzed before - create an entry */
    if (realMethodEntry == NULL) {
        realMethodEntry =
            (CompilerMethodStats *) calloc(1, sizeof(CompilerMethodStats));
        realMethodEntry->method = method;

        dvmHashTableLookup(gDvmJit.methodStatsTable, hashValue,
                           realMethodEntry,
                           (HashCompareFunc) compareMethod,
                           true);
    }

    /* This method is invoked as a callee and has been analyzed - just return */
    if ((isCallee == true) && (realMethodEntry->attributes & METHOD_IS_CALLEE))
        return realMethodEntry;

    /*
     * Similarly, return if this method has been compiled before as a hot
     * method already.
     */
    if ((isCallee == false) &&
        (realMethodEntry->attributes & METHOD_IS_HOT))
        return realMethodEntry;

    int attributes;

    /* Method hasn't been analyzed for the desired purpose yet */
    if (isCallee) {
        /* Aggressively set the attributes until proven otherwise */
        attributes = METHOD_IS_LEAF | METHOD_IS_THROW_FREE | METHOD_IS_CALLEE |
                     METHOD_IS_GETTER | METHOD_IS_SETTER;
    } else {
        attributes = METHOD_IS_HOT;
    }

    /* Count the number of instructions */
    while (codePtr < codeEnd) {
        DecodedInstruction dalvikInsn;
        int width = parseInsn(codePtr, &dalvikInsn, false);

        /* Terminate when the data section is seen */
        if (width == 0)
            break;

        if (isCallee) {
            attributes = analyzeInlineTarget(&dalvikInsn, attributes, insnSize);
        }

        insnSize += width;
        codePtr += width;
    }

    /*
     * Only handle simple getters/setters with one instruction followed by
     * return
     */
    if ((attributes & (METHOD_IS_GETTER | METHOD_IS_SETTER)) &&
        (insnSize != 3)) {
        attributes &= ~(METHOD_IS_GETTER | METHOD_IS_SETTER);
    }

    realMethodEntry->dalvikSize = insnSize * 2;
    realMethodEntry->attributes |= attributes;

#if 0
    /* Uncomment the following to explore various callee patterns */
    if (attributes & METHOD_IS_THROW_FREE) {
        LOGE("%s%s is inlinable%s", method->clazz->descriptor, method->name,
             (attributes & METHOD_IS_EMPTY) ? " empty" : "");
    }

    if (attributes & METHOD_IS_LEAF) {
        LOGE("%s%s is leaf %d%s", method->clazz->descriptor, method->name,
             insnSize, insnSize < 5 ? " (small)" : "");
    }

    if (attributes & (METHOD_IS_GETTER | METHOD_IS_SETTER)) {
        LOGE("%s%s is %s", method->clazz->descriptor, method->name,
             attributes & METHOD_IS_GETTER ? "getter": "setter");
    }
    if (attributes ==
        (METHOD_IS_LEAF | METHOD_IS_THROW_FREE | METHOD_IS_CALLEE)) {
        LOGE("%s%s is inlinable non setter/getter", method->clazz->descriptor,
             method->name);
    }
#endif

    return realMethodEntry;
}

/*
 * Crawl the stack of the thread that requesed compilation to see if any of the
 * ancestors are on the blacklist.
 */
static bool filterMethodByCallGraph(Thread *thread, const char *curMethodName)
{
    /* Crawl the Dalvik stack frames and compare the method name*/
    StackSaveArea *ssaPtr = ((StackSaveArea *) thread->curFrame) - 1;
    while (ssaPtr != ((StackSaveArea *) NULL) - 1) {
        const Method *method = ssaPtr->method;
        if (method) {
            int hashValue = dvmComputeUtf8Hash(method->name);
            bool found =
                dvmHashTableLookup(gDvmJit.methodTable, hashValue,
                               (char *) method->name,
                               (HashCompareFunc) strcmp, false) !=
                NULL;
            if (found) {
                LOGD("Method %s (--> %s) found on the JIT %s list",
                     method->name, curMethodName,
                     gDvmJit.includeSelectedMethod ? "white" : "black");
                return true;
            }

        }
        ssaPtr = ((StackSaveArea *) ssaPtr->prevFrame) - 1;
    };
    return false;
}

/*
 * Main entry point to start trace compilation. Basic blocks are constructed
 * first and they will be passed to the codegen routines to convert Dalvik
 * bytecode into machine code.
 */
bool dvmCompileTrace(JitTraceDescription *desc, int numMaxInsts,
                     JitTranslationInfo *info, jmp_buf *bailPtr,
                     int optHints)
{
    const DexCode *dexCode = dvmGetMethodCode(desc->method);
    const JitTraceRun* currRun = &desc->trace[0];
    unsigned int curOffset = currRun->frag.startOffset;
    unsigned int numInsts = currRun->frag.numInsts;
    const u2 *codePtr = dexCode->insns + curOffset;
    int traceSize = 0;  // # of half-words
    const u2 *startCodePtr = codePtr;
    BasicBlock *curBB, *entryCodeBB;
    int numBlocks = 0;
    static int compilationId;
    CompilationUnit cUnit;
    GrowableList *blockList;
#if defined(WITH_JIT_TUNING)
    CompilerMethodStats *methodStats;
#endif

    /* If we've already compiled this trace, just return success */
    if (dvmJitGetCodeAddr(startCodePtr) && !info->discardResult) {
        /*
         * Make sure the codeAddress is NULL so that it won't clobber the
         * existing entry.
         */
        info->codeAddress = NULL;
        return true;
    }

    compilationId++;
    memset(&cUnit, 0, sizeof(CompilationUnit));

#if defined(WITH_JIT_TUNING)
    /* Locate the entry to store compilation statistics for this method */
    methodStats = dvmCompilerAnalyzeMethodBody(desc->method, false);
#endif

    /* Set the recover buffer pointer */
    cUnit.bailPtr = bailPtr;

    /* Initialize the printMe flag */
    cUnit.printMe = gDvmJit.printMe;

    /* Initialize the profile flag */
    cUnit.executionCount = gDvmJit.profile;

    /* Setup the method */
    cUnit.method = desc->method;

    /* Initialize the PC reconstruction list */
    dvmInitGrowableList(&cUnit.pcReconstructionList, 8);

    /* Initialize the basic block list */
    blockList = &cUnit.blockList;
    dvmInitGrowableList(blockList, 8);

    /* Identify traces that we don't want to compile */
    if (gDvmJit.methodTable) {
        int len = strlen(desc->method->clazz->descriptor) +
                  strlen(desc->method->name) + 1;
        char *fullSignature = (char *)dvmCompilerNew(len, true);
        strcpy(fullSignature, desc->method->clazz->descriptor);
        strcat(fullSignature, desc->method->name);

        int hashValue = dvmComputeUtf8Hash(fullSignature);

        /*
         * Doing three levels of screening to see whether we want to skip
         * compiling this method
         */

        /* First, check the full "class;method" signature */
        bool methodFound =
            dvmHashTableLookup(gDvmJit.methodTable, hashValue,
                               fullSignature, (HashCompareFunc) strcmp,
                               false) !=
            NULL;

        /* Full signature not found - check the enclosing class */
        if (methodFound == false) {
            int hashValue = dvmComputeUtf8Hash(desc->method->clazz->descriptor);
            methodFound =
                dvmHashTableLookup(gDvmJit.methodTable, hashValue,
                               (char *) desc->method->clazz->descriptor,
                               (HashCompareFunc) strcmp, false) !=
                NULL;
            /* Enclosing class not found - check the method name */
            if (methodFound == false) {
                int hashValue = dvmComputeUtf8Hash(desc->method->name);
                methodFound =
                    dvmHashTableLookup(gDvmJit.methodTable, hashValue,
                                   (char *) desc->method->name,
                                   (HashCompareFunc) strcmp, false) !=
                    NULL;

                /*
                 * Debug by call-graph is enabled. Check if the debug list
                 * covers any methods on the VM stack.
                 */
                if (methodFound == false && gDvmJit.checkCallGraph == true) {
                    methodFound =
                        filterMethodByCallGraph(info->requestingThread,
                                                desc->method->name);
                }
            }
        }

        /*
         * Under the following conditions, the trace will be *conservatively*
         * compiled by only containing single-step instructions to and from the
         * interpreter.
         * 1) If includeSelectedMethod == false, the method matches the full or
         *    partial signature stored in the hash table.
         *
         * 2) If includeSelectedMethod == true, the method does not match the
         *    full and partial signature stored in the hash table.
         */
        if (gDvmJit.includeSelectedMethod != methodFound) {
            cUnit.allSingleStep = true;
        } else {
            /* Compile the trace as normal */

            /* Print the method we cherry picked */
            if (gDvmJit.includeSelectedMethod == true) {
                cUnit.printMe = true;
            }
        }
    }

    /* Allocate the entry block */
    curBB = dvmCompilerNewBB(kTraceEntryBlock, numBlocks++);
    dvmInsertGrowableList(blockList, (intptr_t) curBB);
    curBB->startOffset = curOffset;

    entryCodeBB = dvmCompilerNewBB(kDalvikByteCode, numBlocks++);
    dvmInsertGrowableList(blockList, (intptr_t) entryCodeBB);
    entryCodeBB->startOffset = curOffset;
    curBB->fallThrough = entryCodeBB;
    curBB = entryCodeBB;

    if (cUnit.printMe) {
        LOGD("--------\nCompiler: Building trace for %s, offset 0x%x\n",
             desc->method->name, curOffset);
    }

    /*
     * Analyze the trace descriptor and include up to the maximal number
     * of Dalvik instructions into the IR.
     */
    while (1) {
        MIR *insn;
        int width;
        insn = (MIR *)dvmCompilerNew(sizeof(MIR), true);
        insn->offset = curOffset;
        width = parseInsn(codePtr, &insn->dalvikInsn, cUnit.printMe);

        /* The trace should never incude instruction data */
        assert(width);
        insn->width = width;
        traceSize += width;
        dvmCompilerAppendMIR(curBB, insn);
        cUnit.numInsts++;

        int flags = dexGetFlagsFromOpcode(insn->dalvikInsn.opcode);

        if (flags & kInstrInvoke) {
            assert(numInsts == 1);
            CallsiteInfo *callsiteInfo =
                (CallsiteInfo *)dvmCompilerNew(sizeof(CallsiteInfo), true);
            callsiteInfo->clazz = (ClassObject *)currRun[1].meta;
            callsiteInfo->method = (Method *)currRun[2].meta;
            insn->meta.callsiteInfo = callsiteInfo;
        }

        /* Instruction limit reached - terminate the trace here */
        if (cUnit.numInsts >= numMaxInsts) {
            break;
        }
        if (--numInsts == 0) {
            if (currRun->frag.runEnd) {
                break;
            } else {
                /* Advance to the next trace description (ie non-meta info) */
                do {
                    currRun++;
                } while (!currRun->frag.isCode);

                /* Dummy end-of-run marker seen */
                if (currRun->frag.numInsts == 0) {
                    break;
                }

                curBB = dvmCompilerNewBB(kDalvikByteCode, numBlocks++);
                dvmInsertGrowableList(blockList, (intptr_t) curBB);
                curOffset = currRun->frag.startOffset;
                numInsts = currRun->frag.numInsts;
                curBB->startOffset = curOffset;
                codePtr = dexCode->insns + curOffset;
            }
        } else {
            curOffset += width;
            codePtr += width;
        }
    }

#if defined(WITH_JIT_TUNING)
    /* Convert # of half-word to bytes */
    methodStats->compiledDalvikSize += traceSize * 2;
#endif

    /*
     * Now scan basic blocks containing real code to connect the
     * taken/fallthrough links. Also create chaining cells for code not included
     * in the trace.
     */
    size_t blockId;
    for (blockId = 0; blockId < blockList->numUsed; blockId++) {
        curBB = (BasicBlock *) dvmGrowableListGetElement(blockList, blockId);
        MIR *lastInsn = curBB->lastMIRInsn;
        /* Skip empty blocks */
        if (lastInsn == NULL) {
            continue;
        }
        curOffset = lastInsn->offset;
        unsigned int targetOffset = curOffset;
        unsigned int fallThroughOffset = curOffset + lastInsn->width;
        bool isInvoke = false;
        const Method *callee = NULL;

        findBlockBoundary(desc->method, curBB->lastMIRInsn, curOffset,
                          &targetOffset, &isInvoke, &callee);

        /* Link the taken and fallthrough blocks */
        BasicBlock *searchBB;

        int flags = dexGetFlagsFromOpcode(lastInsn->dalvikInsn.opcode);

        if (flags & kInstrInvoke) {
            cUnit.hasInvoke = true;
        }

        /* No backward branch in the trace - start searching the next BB */
        size_t searchBlockId;
        for (searchBlockId = blockId+1; searchBlockId < blockList->numUsed;
             searchBlockId++) {
            searchBB = (BasicBlock *) dvmGrowableListGetElement(blockList,
                                                                searchBlockId);
            if (targetOffset == searchBB->startOffset) {
                curBB->taken = searchBB;
            }
            if (fallThroughOffset == searchBB->startOffset) {
                curBB->fallThrough = searchBB;

                /*
                 * Fallthrough block of an invoke instruction needs to be
                 * aligned to 4-byte boundary (alignment instruction to be
                 * inserted later.
                 */
                if (flags & kInstrInvoke) {
                    searchBB->isFallThroughFromInvoke = true;
                }
            }
        }

        /*
         * Some blocks are ended by non-control-flow-change instructions,
         * currently only due to trace length constraint. In this case we need
         * to generate an explicit branch at the end of the block to jump to
         * the chaining cell.
         */
        curBB->needFallThroughBranch =
            ((flags & (kInstrCanBranch | kInstrCanSwitch | kInstrCanReturn |
                       kInstrInvoke)) == 0);

        /* Only form a loop if JIT_OPT_NO_LOOP is not set */
        if (curBB->taken == NULL &&
            curBB->fallThrough == NULL &&
            flags == (kInstrCanBranch | kInstrCanContinue) &&
            fallThroughOffset == entryCodeBB->startOffset &&
            JIT_OPT_NO_LOOP != (optHints & JIT_OPT_NO_LOOP)) {
            BasicBlock *loopBranch = curBB;
            BasicBlock *exitBB;
            BasicBlock *exitChainingCell;

            if (cUnit.printMe) {
                LOGD("Natural loop detected!");
            }
            exitBB = dvmCompilerNewBB(kTraceExitBlock, numBlocks++);
            dvmInsertGrowableList(blockList, (intptr_t) exitBB);
            exitBB->startOffset = targetOffset;
            exitBB->needFallThroughBranch = true;

            loopBranch->taken = exitBB;
#if defined(WITH_SELF_VERIFICATION)
            BasicBlock *backwardCell =
                dvmCompilerNewBB(kChainingCellBackwardBranch, numBlocks++);
            dvmInsertGrowableList(blockList, (intptr_t) backwardCell);
            backwardCell->startOffset = entryCodeBB->startOffset;
            loopBranch->fallThrough = backwardCell;
#elif defined(WITH_JIT_TUNING)
            if (gDvmJit.profile) {
                BasicBlock *backwardCell =
                    dvmCompilerNewBB(kChainingCellBackwardBranch, numBlocks++);
                dvmInsertGrowableList(blockList, (intptr_t) backwardCell);
                backwardCell->startOffset = entryCodeBB->startOffset;
                loopBranch->fallThrough = backwardCell;
            } else {
                loopBranch->fallThrough = entryCodeBB;
            }
#else
            loopBranch->fallThrough = entryCodeBB;
#endif

            /* Create the chaining cell as the fallthrough of the exit block */
            exitChainingCell = dvmCompilerNewBB(kChainingCellNormal,
                                                numBlocks++);
            dvmInsertGrowableList(blockList, (intptr_t) exitChainingCell);
            exitChainingCell->startOffset = targetOffset;

            exitBB->fallThrough = exitChainingCell;

            cUnit.hasLoop = true;
        }

        if (lastInsn->dalvikInsn.opcode == OP_PACKED_SWITCH ||
            lastInsn->dalvikInsn.opcode == OP_SPARSE_SWITCH) {
            int i;
            const u2 *switchData = desc->method->insns + lastInsn->offset +
                             lastInsn->dalvikInsn.vB;
            int size = switchData[1];
            int maxChains = MIN(size, MAX_CHAINED_SWITCH_CASES);

            /*
             * Generate the landing pad for cases whose ranks are higher than
             * MAX_CHAINED_SWITCH_CASES. The code will re-enter the interpreter
             * through the NoChain point.
             */
            if (maxChains != size) {
                cUnit.switchOverflowPad =
                    desc->method->insns + lastInsn->offset;
            }

            s4 *targets = (s4 *) (switchData + 2 +
                    (lastInsn->dalvikInsn.opcode == OP_PACKED_SWITCH ?
                     2 : size * 2));

            /* One chaining cell for the first MAX_CHAINED_SWITCH_CASES cases */
            for (i = 0; i < maxChains; i++) {
                BasicBlock *caseChain = dvmCompilerNewBB(kChainingCellNormal,
                                                         numBlocks++);
                dvmInsertGrowableList(blockList, (intptr_t) caseChain);
                caseChain->startOffset = lastInsn->offset + targets[i];
            }

            /* One more chaining cell for the default case */
            BasicBlock *caseChain = dvmCompilerNewBB(kChainingCellNormal,
                                                     numBlocks++);
            dvmInsertGrowableList(blockList, (intptr_t) caseChain);
            caseChain->startOffset = lastInsn->offset + lastInsn->width;
        /* Fallthrough block not included in the trace */
        } else if (!isUnconditionalBranch(lastInsn) &&
                   curBB->fallThrough == NULL) {
            BasicBlock *fallThroughBB;
            /*
             * If the chaining cell is after an invoke or
             * instruction that cannot change the control flow, request a hot
             * chaining cell.
             */
            if (isInvoke || curBB->needFallThroughBranch) {
                fallThroughBB = dvmCompilerNewBB(kChainingCellHot, numBlocks++);
            } else {
                fallThroughBB = dvmCompilerNewBB(kChainingCellNormal,
                                                 numBlocks++);
            }
            dvmInsertGrowableList(blockList, (intptr_t) fallThroughBB);
            fallThroughBB->startOffset = fallThroughOffset;
            curBB->fallThrough = fallThroughBB;
        }
        /* Target block not included in the trace */
        if (curBB->taken == NULL &&
            (isGoto(lastInsn) || isInvoke ||
            (targetOffset != UNKNOWN_TARGET && targetOffset != curOffset))) {
            BasicBlock *newBB;
            if (isInvoke) {
                /* Monomorphic callee */
                if (callee) {
                    newBB = dvmCompilerNewBB(kChainingCellInvokeSingleton,
                                             numBlocks++);
                    newBB->startOffset = 0;
                    newBB->containingMethod = callee;
                /* Will resolve at runtime */
                } else {
                    newBB = dvmCompilerNewBB(kChainingCellInvokePredicted,
                                             numBlocks++);
                    newBB->startOffset = 0;
                }
            /* For unconditional branches, request a hot chaining cell */
            } else {
#if !defined(WITH_SELF_VERIFICATION)
                newBB = dvmCompilerNewBB(dexIsGoto(flags) ?
                                                  kChainingCellHot :
                                                  kChainingCellNormal,
                                         numBlocks++);
                newBB->startOffset = targetOffset;
#else
                /* Handle branches that branch back into the block */
                if (targetOffset >= curBB->firstMIRInsn->offset &&
                    targetOffset <= curBB->lastMIRInsn->offset) {
                    newBB = dvmCompilerNewBB(kChainingCellBackwardBranch,
                                             numBlocks++);
                } else {
                    newBB = dvmCompilerNewBB(dexIsGoto(flags) ?
                                                      kChainingCellHot :
                                                      kChainingCellNormal,
                                             numBlocks++);
                }
                newBB->startOffset = targetOffset;
#endif
            }
            curBB->taken = newBB;
            dvmInsertGrowableList(blockList, (intptr_t) newBB);
        }
    }

    /* Now create a special block to host PC reconstruction code */
    curBB = dvmCompilerNewBB(kPCReconstruction, numBlocks++);
    dvmInsertGrowableList(blockList, (intptr_t) curBB);

    /* And one final block that publishes the PC and raise the exception */
    curBB = dvmCompilerNewBB(kExceptionHandling, numBlocks++);
    dvmInsertGrowableList(blockList, (intptr_t) curBB);

    if (cUnit.printMe) {
        char* signature =
            dexProtoCopyMethodDescriptor(&desc->method->prototype);
        LOGD("TRACEINFO (%d): 0x%08x %s%s.%s 0x%x %d of %d, %d blocks",
            compilationId,
            (intptr_t) desc->method->insns,
            desc->method->clazz->descriptor,
            desc->method->name,
            signature,
            desc->trace[0].frag.startOffset,
            traceSize,
            dexCode->insnsSize,
            numBlocks);
        free(signature);
    }

    cUnit.traceDesc = desc;
    cUnit.numBlocks = numBlocks;

    /* Set the instruction set to use (NOTE: later components may change it) */
    cUnit.instructionSet = dvmCompilerInstructionSet();

    /* Inline transformation @ the MIR level */
    if (cUnit.hasInvoke && !(gDvmJit.disableOpt & (1 << kMethodInlining))) {
        dvmCompilerInlineMIR(&cUnit);
    }

    cUnit.numDalvikRegisters = cUnit.method->registersSize;

    /* Preparation for SSA conversion */
    dvmInitializeSSAConversion(&cUnit);

    if (cUnit.hasLoop) {
        /*
         * Loop is not optimizable (for example lack of a single induction
         * variable), punt and recompile the trace with loop optimization
         * disabled.
         */
        bool loopOpt = dvmCompilerLoopOpt(&cUnit);
        if (loopOpt == false) {
            if (cUnit.printMe) {
                LOGD("Loop is not optimizable - retry codegen");
            }
            /* Reset the compiler resource pool */
            dvmCompilerArenaReset();
            return dvmCompileTrace(desc, cUnit.numInsts, info, bailPtr,
                                   optHints | JIT_OPT_NO_LOOP);
        }
    }
    else {
        dvmCompilerNonLoopAnalysis(&cUnit);
    }

    dvmCompilerInitializeRegAlloc(&cUnit);  // Needs to happen after SSA naming

    if (cUnit.printMe) {
        dvmCompilerDumpCompilationUnit(&cUnit);
    }

    /* Allocate Registers */
    dvmCompilerRegAlloc(&cUnit);

    /* Convert MIR to LIR, etc. */
    dvmCompilerMIR2LIR(&cUnit);

    /* Convert LIR into machine code. Loop for recoverable retries */
    do {
        dvmCompilerAssembleLIR(&cUnit, info);
        cUnit.assemblerRetries++;
        if (cUnit.printMe && cUnit.assemblerStatus != kSuccess)
            LOGD("Assembler abort #%d on %d",cUnit.assemblerRetries,
                  cUnit.assemblerStatus);
    } while (cUnit.assemblerStatus == kRetryAll);

    if (cUnit.printMe) {
        dvmCompilerCodegenDump(&cUnit);
        LOGD("End %s%s, %d Dalvik instructions",
             desc->method->clazz->descriptor, desc->method->name,
             cUnit.numInsts);
    }

    /* Reset the compiler resource pool */
    dvmCompilerArenaReset();

    if (cUnit.assemblerStatus == kRetryHalve) {
        /* Halve the instruction count and start from the top */
        return dvmCompileTrace(desc, cUnit.numInsts / 2, info, bailPtr,
                               optHints);
    }

    assert(cUnit.assemblerStatus == kSuccess);
#if defined(WITH_JIT_TUNING)
    methodStats->nativeSize += cUnit.totalSize;
#endif

    /* FIXME - to exercise the method parser, uncomment the following code */
#if 0
    bool dvmCompileMethod(const Method *method);
    if (desc->trace[0].frag.startOffset == 0) {
        dvmCompileMethod(desc->method);
    }
#endif

    return info->codeAddress != NULL;
}

/*
 * Since we are including instructions from possibly a cold method into the
 * current trace, we need to make sure that all the associated information
 * with the callee is properly initialized. If not, we punt on this inline
 * target.
 *
 * TODO: volatile instructions will be handled later.
 */
bool dvmCompilerCanIncludeThisInstruction(const Method *method,
                                          const DecodedInstruction *insn)
{
    switch (insn->opcode) {
        case OP_NEW_INSTANCE:
        case OP_CHECK_CAST: {
            ClassObject *classPtr = (ClassObject *)(void*)
              (method->clazz->pDvmDex->pResClasses[insn->vB]);

            /* Class hasn't been initialized yet */
            if (classPtr == NULL) {
                return false;
            }
            return true;
        }
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_CHAR:
        case OP_SGET_BYTE:
        case OP_SGET_SHORT:
        case OP_SGET:
        case OP_SGET_WIDE:
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_CHAR:
        case OP_SPUT_BYTE:
        case OP_SPUT_SHORT:
        case OP_SPUT:
        case OP_SPUT_WIDE: {
            void *fieldPtr = (void*)
              (method->clazz->pDvmDex->pResFields[insn->vB]);

            if (fieldPtr == NULL) {
                return false;
            }
            return true;
        }
        case OP_INVOKE_SUPER:
        case OP_INVOKE_SUPER_RANGE: {
            int mIndex = method->clazz->pDvmDex->
                pResMethods[insn->vB]->methodIndex;
            const Method *calleeMethod = method->clazz->super->vtable[mIndex];
            if (calleeMethod == NULL) {
                return false;
            }
            return true;
        }
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_SUPER_QUICK_RANGE: {
            const Method *calleeMethod = method->clazz->super->vtable[insn->vB];
            if (calleeMethod == NULL) {
                return false;
            }
            return true;
        }
        case OP_INVOKE_STATIC:
        case OP_INVOKE_STATIC_RANGE:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_DIRECT_RANGE: {
            const Method *calleeMethod =
                method->clazz->pDvmDex->pResMethods[insn->vB];
            if (calleeMethod == NULL) {
                return false;
            }
            return true;
        }
        case OP_CONST_CLASS: {
            void *classPtr = (void*)
                (method->clazz->pDvmDex->pResClasses[insn->vB]);

            if (classPtr == NULL) {
                return false;
            }
            return true;
        }
        case OP_CONST_STRING_JUMBO:
        case OP_CONST_STRING: {
            void *strPtr = (void*)
                (method->clazz->pDvmDex->pResStrings[insn->vB]);

            if (strPtr == NULL) {
                return false;
            }
            return true;
        }
        default:
            return true;
    }
}

/* Split an existing block from the specified code offset into two */
static BasicBlock *splitBlock(CompilationUnit *cUnit,
                              unsigned int codeOffset,
                              BasicBlock *origBlock)
{
    MIR *insn = origBlock->firstMIRInsn;
    while (insn) {
        if (insn->offset == codeOffset) break;
        insn = insn->next;
    }
    if (insn == NULL) {
        LOGE("Break split failed");
        dvmAbort();
    }
    BasicBlock *bottomBlock = dvmCompilerNewBB(kDalvikByteCode,
                                               cUnit->numBlocks++);
    dvmInsertGrowableList(&cUnit->blockList, (intptr_t) bottomBlock);

    bottomBlock->startOffset = codeOffset;
    bottomBlock->firstMIRInsn = insn;
    bottomBlock->lastMIRInsn = origBlock->lastMIRInsn;

    /* Only the top half of split blocks is tagged as BA_CATCH_BLOCK */
    bottomBlock->blockAttributes = origBlock->blockAttributes & ~BA_CATCH_BLOCK;

    /* Handle the taken path */
    bottomBlock->taken = origBlock->taken;
    if (bottomBlock->taken) {
        origBlock->taken = NULL;
        dvmCompilerClearBit(bottomBlock->taken->predecessors, origBlock->id);
        dvmCompilerSetBit(bottomBlock->taken->predecessors, bottomBlock->id);
    }

    /* Handle the fallthrough path */
    bottomBlock->fallThrough = origBlock->fallThrough;
    origBlock->fallThrough = bottomBlock;
    dvmCompilerSetBit(bottomBlock->predecessors, origBlock->id);
    if (bottomBlock->fallThrough) {
        dvmCompilerClearBit(bottomBlock->fallThrough->predecessors,
                            origBlock->id);
        dvmCompilerSetBit(bottomBlock->fallThrough->predecessors,
                          bottomBlock->id);
    }

    /* Handle the successor list */
    if (origBlock->successorBlockList.blockListType != kNotUsed) {
        bottomBlock->successorBlockList = origBlock->successorBlockList;
        origBlock->successorBlockList.blockListType = kNotUsed;
        GrowableListIterator iterator;

        dvmGrowableListIteratorInit(&bottomBlock->successorBlockList.blocks,
                                    &iterator);
        while (true) {
            BasicBlock *bb =
                (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
            if (bb == NULL) break;
            dvmCompilerClearBit(bb->predecessors, origBlock->id);
            dvmCompilerSetBit(bb->predecessors, bottomBlock->id);
        }
    }

    origBlock->lastMIRInsn = insn->prev;
    /* Only the bottom half of split blocks is tagged as BA_ENDS_WITH_THROW */
    origBlock->blockAttributes &= ~BA_ENDS_WITH_THROW;

    insn->prev->next = NULL;
    insn->prev = NULL;
    return bottomBlock;
}

/*
 * Given a code offset, find out the block that starts with it. If the offset
 * is in the middle of an existing block, split it into two.
 */
static BasicBlock *findBlock(CompilationUnit *cUnit,
                             unsigned int codeOffset,
                             bool split, bool create)
{
    GrowableList *blockList = &cUnit->blockList;
    BasicBlock *bb;
    unsigned int i;

    for (i = 0; i < blockList->numUsed; i++) {
        bb = (BasicBlock *) blockList->elemList[i];
        if (bb->blockType != kDalvikByteCode) continue;
        if (bb->startOffset == codeOffset) return bb;
        /* Check if a branch jumps into the middle of an existing block */
        if ((split == true) && (codeOffset > bb->startOffset) &&
            (bb->lastMIRInsn != NULL) &&
            (codeOffset <= bb->lastMIRInsn->offset)) {
            BasicBlock *newBB = splitBlock(cUnit, codeOffset, bb);
            return newBB;
        }
    }
    if (create) {
          bb = dvmCompilerNewBB(kDalvikByteCode, cUnit->numBlocks++);
          dvmInsertGrowableList(&cUnit->blockList, (intptr_t) bb);
          bb->startOffset = codeOffset;
          return bb;
    }
    return NULL;
}

/* Dump the CFG into a DOT graph */
void dumpCFG(CompilationUnit *cUnit, const char *dirPrefix)
{
    const Method *method = cUnit->method;
    FILE *file;
    char* signature = dexProtoCopyMethodDescriptor(&method->prototype);
    char *fileName = (char *) dvmCompilerNew(
                                  strlen(dirPrefix) +
                                  strlen(method->clazz->descriptor) +
                                  strlen(method->name) +
                                  strlen(signature) +
                                  strlen(".dot") + 1, true);
    sprintf(fileName, "%s%s%s%s.dot", dirPrefix,
            method->clazz->descriptor, method->name, signature);
    free(signature);

    /*
     * Convert the special characters into a filesystem- and shell-friendly
     * format.
     */
    int i;
    for (i = strlen(dirPrefix); fileName[i]; i++) {
        if (fileName[i] == '/') {
            fileName[i] = '_';
        } else if (fileName[i] == ';') {
            fileName[i] = '#';
        } else if (fileName[i] == '$') {
            fileName[i] = '+';
        } else if (fileName[i] == '(' || fileName[i] == ')') {
            fileName[i] = '@';
        } else if (fileName[i] == '<' || fileName[i] == '>') {
            fileName[i] = '=';
        }
    }
    file = fopen(fileName, "w");
    if (file == NULL) {
        return;
    }
    fprintf(file, "digraph G {\n");

    fprintf(file, "  rankdir=TB\n");

    int numReachableBlocks = cUnit->numReachableBlocks;
    int idx;
    const GrowableList *blockList = &cUnit->blockList;

    for (idx = 0; idx < numReachableBlocks; idx++) {
        int blockIdx = cUnit->dfsOrder.elemList[idx];
        BasicBlock *bb = (BasicBlock *) dvmGrowableListGetElement(blockList,
                                                                  blockIdx);
        if (bb == NULL) break;
        if (bb->blockType == kMethodEntryBlock) {
            fprintf(file, "  entry [shape=Mdiamond];\n");
        } else if (bb->blockType == kMethodExitBlock) {
            fprintf(file, "  exit [shape=Mdiamond];\n");
        } else if (bb->blockType == kDalvikByteCode) {
            fprintf(file, "  block%04x [shape=%s,label = \"{ \\\n",
                    bb->startOffset,
                    bb->blockAttributes & BA_CATCH_BLOCK ?
                      "Mrecord" : "record");
            if (bb->blockAttributes & BA_CATCH_BLOCK) {
                if (bb->exceptionTypeIdx == kDexNoIndex) {
                    fprintf(file, "    {any\\l} |\\\n");
                } else {
                    fprintf(file, "    {throwable type:%x\\l} |\\\n",
                            bb->exceptionTypeIdx);
                }
            }
            const MIR *mir;
            for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
                fprintf(file, "    {%04x %s\\l}%s\\\n", mir->offset,
                        dvmCompilerFullDisassembler(cUnit, mir),
                        mir->next ? " | " : " ");
            }
            fprintf(file, "  }\"];\n\n");
        } else if (bb->blockType == kExceptionHandling) {
            char blockName[BLOCK_NAME_LEN];

            dvmGetBlockName(bb, blockName);
            fprintf(file, "  %s [shape=invhouse];\n", blockName);
        }

        char blockName1[BLOCK_NAME_LEN], blockName2[BLOCK_NAME_LEN];

        if (bb->taken) {
            dvmGetBlockName(bb, blockName1);
            dvmGetBlockName(bb->taken, blockName2);
            fprintf(file, "  %s:s -> %s:n [style=dotted]\n",
                    blockName1, blockName2);
        }
        if (bb->fallThrough) {
            dvmGetBlockName(bb, blockName1);
            dvmGetBlockName(bb->fallThrough, blockName2);
            fprintf(file, "  %s:s -> %s:n\n", blockName1, blockName2);
        }

        if (bb->successorBlockList.blockListType != kNotUsed) {
            fprintf(file, "  succ%04x [shape=%s,label = \"{ \\\n",
                    bb->startOffset,
                    (bb->successorBlockList.blockListType == kCatch) ?
                        "Mrecord" : "record");
            GrowableListIterator iterator;
            dvmGrowableListIteratorInit(&bb->successorBlockList.blocks,
                                        &iterator);

            BasicBlock *destBlock =
                (BasicBlock *) dvmGrowableListIteratorNext(&iterator);

            int succId = 0;
            while (true) {
                if (destBlock == NULL) break;
                BasicBlock *nextBlock =
                    (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
                fprintf(file, "    {<f%d> %04x\\l}%s\\\n",
                        succId++,
                        destBlock->startOffset,
                        (nextBlock != NULL) ? " | " : " ");
                destBlock = nextBlock;
            }
            fprintf(file, "  }\"];\n\n");

            dvmGetBlockName(bb, blockName1);
            fprintf(file, "  %s:s -> succ%04x:n [style=dashed]\n",
                    blockName1, bb->startOffset);

            if (bb->successorBlockList.blockListType == kPackedSwitch ||
                bb->successorBlockList.blockListType == kSparseSwitch) {

                dvmGrowableListIteratorInit(&bb->successorBlockList.blocks,
                                            &iterator);

                succId = 0;
                while (true) {
                    BasicBlock *destBlock =
                        (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
                    if (destBlock == NULL) break;

                    dvmGetBlockName(destBlock, blockName2);
                    fprintf(file, "  succ%04x:f%d:e -> %s:n\n",
                            bb->startOffset, succId++,
                            blockName2);
                }
            }
        }
        fprintf(file, "\n");

        /*
         * If we need to debug the dominator tree, uncomment the following code
         */
#if 0
        dvmGetBlockName(bb, blockName1);
        fprintf(file, "  cfg%s [label=\"%s\", shape=none];\n",
                blockName1, blockName1);
        if (bb->iDom) {
            dvmGetBlockName(bb->iDom, blockName2);
            fprintf(file, "  cfg%s:s -> cfg%s:n\n\n",
                    blockName2, blockName1);
        }
#endif
    }
    fprintf(file, "}\n");
    fclose(file);
}

/* Verify if all the successor is connected with all the claimed predecessors */
static bool verifyPredInfo(CompilationUnit *cUnit, BasicBlock *bb)
{
    BitVectorIterator bvIterator;

    dvmBitVectorIteratorInit(bb->predecessors, &bvIterator);
    while (true) {
        int blockIdx = dvmBitVectorIteratorNext(&bvIterator);
        if (blockIdx == -1) break;
        BasicBlock *predBB = (BasicBlock *)
            dvmGrowableListGetElement(&cUnit->blockList, blockIdx);
        bool found = false;
        if (predBB->taken == bb) {
            found = true;
        } else if (predBB->fallThrough == bb) {
            found = true;
        } else if (predBB->successorBlockList.blockListType != kNotUsed) {
            GrowableListIterator iterator;
            dvmGrowableListIteratorInit(&predBB->successorBlockList.blocks,
                                        &iterator);
            while (true) {
                BasicBlock *succBB =
                    (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
                if (succBB == NULL) break;
                if (succBB == bb) {
                    found = true;
                    break;
                }
            }
        }
        if (found == false) {
            char blockName1[BLOCK_NAME_LEN], blockName2[BLOCK_NAME_LEN];
            dvmGetBlockName(bb, blockName1);
            dvmGetBlockName(predBB, blockName2);
            dumpCFG(cUnit, "/data/tombstones/");
            LOGE("Successor %s not found from %s",
                 blockName1, blockName2);
            dvmAbort();
        }
    }
    return true;
}

/* Identify code range in try blocks and set up the empty catch blocks */
static void processTryCatchBlocks(CompilationUnit *cUnit)
{
    const Method *meth = cUnit->method;
    const DexCode *pCode = dvmGetMethodCode(meth);
    int triesSize = pCode->triesSize;
    int i;
    int offset;

    if (triesSize == 0) {
        return;
    }

    const DexTry *pTries = dexGetTries(pCode);
    BitVector *tryBlockAddr = cUnit->tryBlockAddr;

    /* Mark all the insn offsets in Try blocks */
    for (i = 0; i < triesSize; i++) {
        const DexTry* pTry = &pTries[i];
        /* all in 16-bit units */
        int startOffset = pTry->startAddr;
        int endOffset = startOffset + pTry->insnCount;

        for (offset = startOffset; offset < endOffset; offset++) {
            dvmCompilerSetBit(tryBlockAddr, offset);
        }
    }

    /* Iterate over each of the handlers to enqueue the empty Catch blocks */
    offset = dexGetFirstHandlerOffset(pCode);
    int handlersSize = dexGetHandlersSize(pCode);

    for (i = 0; i < handlersSize; i++) {
        DexCatchIterator iterator;
        dexCatchIteratorInit(&iterator, pCode, offset);

        for (;;) {
            DexCatchHandler* handler = dexCatchIteratorNext(&iterator);

            if (handler == NULL) {
                break;
            }

            BasicBlock *catchBlock = findBlock(cUnit, handler->address,
                                               /* split */
                                               false,
                                               /* create */
                                               true);
            catchBlock->blockAttributes |= BA_CATCH_BLOCK;
            catchBlock->exceptionTypeIdx = handler->typeIdx;
        }

        offset = dexCatchIteratorGetEndOffset(&iterator, pCode);
    }
}

/* Process instructions with the kInstrCanBranch flag */
static void processCanBranch(CompilationUnit *cUnit, BasicBlock *curBlock,
                             MIR *insn, int curOffset, int width, int flags,
                             const u2* codePtr, const u2* codeEnd)
{
    int target = curOffset;
    switch (insn->dalvikInsn.opcode) {
        case OP_GOTO:
        case OP_GOTO_16:
        case OP_GOTO_32:
            target += (int) insn->dalvikInsn.vA;
            break;
        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
            target += (int) insn->dalvikInsn.vC;
            break;
        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            target += (int) insn->dalvikInsn.vB;
            break;
        default:
            LOGE("Unexpected opcode(%d) with kInstrCanBranch set",
                 insn->dalvikInsn.opcode);
            dvmAbort();
    }
    BasicBlock *takenBlock = findBlock(cUnit, target,
                                       /* split */
                                       true,
                                       /* create */
                                       true);
    curBlock->taken = takenBlock;
    dvmCompilerSetBit(takenBlock->predecessors, curBlock->id);

    /* Always terminate the current block for conditional branches */
    if (flags & kInstrCanContinue) {
        BasicBlock *fallthroughBlock = findBlock(cUnit,
                                                 curOffset +  width,
                                                 /* split */
                                                 false,
                                                 /* create */
                                                 true);
        curBlock->fallThrough = fallthroughBlock;
        dvmCompilerSetBit(fallthroughBlock->predecessors, curBlock->id);
    } else if (codePtr < codeEnd) {
        /* Create a fallthrough block for real instructions (incl. OP_NOP) */
        if (contentIsInsn(codePtr)) {
            findBlock(cUnit, curOffset + width,
                      /* split */
                      false,
                      /* create */
                      true);
        }
    }
}

/* Process instructions with the kInstrCanSwitch flag */
static void processCanSwitch(CompilationUnit *cUnit, BasicBlock *curBlock,
                             MIR *insn, int curOffset, int width, int flags)
{
    u2 *switchData= (u2 *) (cUnit->method->insns + curOffset +
                            insn->dalvikInsn.vB);
    int size;
    int *targetTable;
    int i;

    /*
     * Packed switch data format:
     *  ushort ident = 0x0100   magic value
     *  ushort size             number of entries in the table
     *  int first_key           first (and lowest) switch case value
     *  int targets[size]       branch targets, relative to switch opcode
     *
     * Total size is (4+size*2) 16-bit code units.
     */
    if (insn->dalvikInsn.opcode == OP_PACKED_SWITCH) {
        assert(switchData[0] == kPackedSwitchSignature);
        size = switchData[1];
        targetTable = (int *) &switchData[4];
    /*
     * Sparse switch data format:
     *  ushort ident = 0x0200   magic value
     *  ushort size             number of entries in the table; > 0
     *  int keys[size]          keys, sorted low-to-high; 32-bit aligned
     *  int targets[size]       branch targets, relative to switch opcode
     *
     * Total size is (2+size*4) 16-bit code units.
     */
    } else {
        assert(switchData[0] == kSparseSwitchSignature);
        size = switchData[1];
        targetTable = (int *) &switchData[2 + size*2];
    }

    if (curBlock->successorBlockList.blockListType != kNotUsed) {
        LOGE("Successor block list already in use: %d",
             curBlock->successorBlockList.blockListType);
        dvmAbort();
    }
    curBlock->successorBlockList.blockListType =
        (insn->dalvikInsn.opcode == OP_PACKED_SWITCH) ?
        kPackedSwitch : kSparseSwitch;
    dvmInitGrowableList(&curBlock->successorBlockList.blocks, size);

    for (i = 0; i < size; i++) {
        BasicBlock *caseBlock = findBlock(cUnit, curOffset + targetTable[i],
                                          /* split */
                                          true,
                                          /* create */
                                          true);
        dvmInsertGrowableList(&curBlock->successorBlockList.blocks,
                              (intptr_t) caseBlock);
        dvmCompilerSetBit(caseBlock->predecessors, curBlock->id);
    }

    /* Fall-through case */
    BasicBlock *fallthroughBlock = findBlock(cUnit,
                                             curOffset +  width,
                                             /* split */
                                             false,
                                             /* create */
                                             true);
    curBlock->fallThrough = fallthroughBlock;
    dvmCompilerSetBit(fallthroughBlock->predecessors, curBlock->id);
}

/* Process instructions with the kInstrCanThrow flag */
static void processCanThrow(CompilationUnit *cUnit, BasicBlock *curBlock,
                            MIR *insn, int curOffset, int width, int flags,
                            BitVector *tryBlockAddr, const u2 *codePtr,
                            const u2* codeEnd)
{
    const Method *method = cUnit->method;
    const DexCode *dexCode = dvmGetMethodCode(method);

    curBlock->blockAttributes |= BA_ENDS_WITH_THROW;
    /* In try block */
    if (dvmIsBitSet(tryBlockAddr, curOffset)) {
        DexCatchIterator iterator;

        if (!dexFindCatchHandler(&iterator, dexCode, curOffset)) {
            LOGE("Catch block not found in dexfile for insn %x in %s",
                 curOffset, method->name);
            dvmAbort();

        }
        if (curBlock->successorBlockList.blockListType != kNotUsed) {
            LOGE("Successor block list already in use: %d",
                 curBlock->successorBlockList.blockListType);
            dvmAbort();
        }
        curBlock->successorBlockList.blockListType = kCatch;
        dvmInitGrowableList(&curBlock->successorBlockList.blocks, 2);

        for (;;) {
            DexCatchHandler* handler = dexCatchIteratorNext(&iterator);

            if (handler == NULL) {
                break;
            }

            BasicBlock *catchBlock = findBlock(cUnit, handler->address,
                                               /* split */
                                               false,
                                               /* create */
                                               false);

            assert(catchBlock->blockAttributes & BA_CATCH_BLOCK);
            dvmInsertGrowableList(&curBlock->successorBlockList.blocks,
                                  (intptr_t) catchBlock);
            dvmCompilerSetBit(catchBlock->predecessors, curBlock->id);
        }
    } else {
        BasicBlock *ehBlock = dvmCompilerNewBB(kExceptionHandling,
                                               cUnit->numBlocks++);
        curBlock->taken = ehBlock;
        dvmInsertGrowableList(&cUnit->blockList, (intptr_t) ehBlock);
        ehBlock->startOffset = curOffset;
        dvmCompilerSetBit(ehBlock->predecessors, curBlock->id);
    }

    /*
     * Force the current block to terminate.
     *
     * Data may be present before codeEnd, so we need to parse it to know
     * whether it is code or data.
     */
    if (codePtr < codeEnd) {
        /* Create a fallthrough block for real instructions (incl. OP_NOP) */
        if (contentIsInsn(codePtr)) {
            BasicBlock *fallthroughBlock = findBlock(cUnit,
                                                     curOffset + width,
                                                     /* split */
                                                     false,
                                                     /* create */
                                                     true);
            /*
             * OP_THROW and OP_THROW_VERIFICATION_ERROR are unconditional
             * branches.
             */
            if (insn->dalvikInsn.opcode != OP_THROW_VERIFICATION_ERROR &&
                insn->dalvikInsn.opcode != OP_THROW) {
                curBlock->fallThrough = fallthroughBlock;
                dvmCompilerSetBit(fallthroughBlock->predecessors, curBlock->id);
            }
        }
    }
}

/*
 * Similar to dvmCompileTrace, but the entity processed here is the whole
 * method.
 *
 * TODO: implementation will be revisited when the trace builder can provide
 * whole-method traces.
 */
bool dvmCompileMethod(const Method *method)
{
    CompilationUnit cUnit;
    const DexCode *dexCode = dvmGetMethodCode(method);
    const u2 *codePtr = dexCode->insns;
    const u2 *codeEnd = dexCode->insns + dexCode->insnsSize;
    int numBlocks = 0;
    unsigned int curOffset = 0;

    memset(&cUnit, 0, sizeof(cUnit));
    cUnit.method = method;

    /* Initialize the block list */
    dvmInitGrowableList(&cUnit.blockList, 4);

    /* Allocate the bit-vector to track the beginning of basic blocks */
    BitVector *tryBlockAddr = dvmCompilerAllocBitVector(dexCode->insnsSize,
                                                        true /* expandable */);
    cUnit.tryBlockAddr = tryBlockAddr;

    /* Create the default entry and exit blocks and enter them to the list */
    BasicBlock *entryBlock = dvmCompilerNewBB(kMethodEntryBlock, numBlocks++);
    BasicBlock *exitBlock = dvmCompilerNewBB(kMethodExitBlock, numBlocks++);

    cUnit.entryBlock = entryBlock;
    cUnit.exitBlock = exitBlock;

    dvmInsertGrowableList(&cUnit.blockList, (intptr_t) entryBlock);
    dvmInsertGrowableList(&cUnit.blockList, (intptr_t) exitBlock);

    /* Current block to record parsed instructions */
    BasicBlock *curBlock = dvmCompilerNewBB(kDalvikByteCode, numBlocks++);
    curBlock->startOffset = 0;
    dvmInsertGrowableList(&cUnit.blockList, (intptr_t) curBlock);
    entryBlock->fallThrough = curBlock;
    dvmCompilerSetBit(curBlock->predecessors, entryBlock->id);

    /*
     * Store back the number of blocks since new blocks may be created of
     * accessing cUnit.
     */
    cUnit.numBlocks = numBlocks;

    /* Identify code range in try blocks and set up the empty catch blocks */
    processTryCatchBlocks(&cUnit);

    /* Parse all instructions and put them into containing basic blocks */
    while (codePtr < codeEnd) {
        MIR *insn = (MIR *) dvmCompilerNew(sizeof(MIR), true);
        insn->offset = curOffset;
        int width = parseInsn(codePtr, &insn->dalvikInsn, false);
        insn->width = width;

        /* Terminate when the data section is seen */
        if (width == 0)
            break;

        dvmCompilerAppendMIR(curBlock, insn);

        codePtr += width;
        int flags = dexGetFlagsFromOpcode(insn->dalvikInsn.opcode);

        if (flags & kInstrCanBranch) {
            processCanBranch(&cUnit, curBlock, insn, curOffset, width, flags,
                             codePtr, codeEnd);
        } else if (flags & kInstrCanReturn) {
            curBlock->fallThrough = exitBlock;
            dvmCompilerSetBit(exitBlock->predecessors, curBlock->id);
            /*
             * Terminate the current block if there are instructions
             * afterwards.
             */
            if (codePtr < codeEnd) {
                /*
                 * Create a fallthrough block for real instructions
                 * (incl. OP_NOP).
                 */
                if (contentIsInsn(codePtr)) {
                    findBlock(&cUnit, curOffset + width,
                              /* split */
                              false,
                              /* create */
                              true);
                }
            }
        } else if (flags & kInstrCanThrow) {
            processCanThrow(&cUnit, curBlock, insn, curOffset, width, flags,
                            tryBlockAddr, codePtr, codeEnd);
        } else if (flags & kInstrCanSwitch) {
            processCanSwitch(&cUnit, curBlock, insn, curOffset, width, flags);
        }
        curOffset += width;
        BasicBlock *nextBlock = findBlock(&cUnit, curOffset,
                                          /* split */
                                          false,
                                          /* create */
                                          false);
        if (nextBlock) {
            /*
             * The next instruction could be the target of a previously parsed
             * forward branch so a block is already created. If the current
             * instruction is not an unconditional branch, connect them through
             * the fall-through link.
             */
            assert(curBlock->fallThrough == NULL ||
                   curBlock->fallThrough == nextBlock ||
                   curBlock->fallThrough == exitBlock);

            if ((curBlock->fallThrough == NULL) &&
                !dexIsGoto(flags) &&
                !(flags & kInstrCanReturn)) {
                curBlock->fallThrough = nextBlock;
                dvmCompilerSetBit(nextBlock->predecessors, curBlock->id);
            }
            curBlock = nextBlock;
        }
    }

    /* Adjust this value accordingly once inlining is performed */
    cUnit.numDalvikRegisters = cUnit.method->registersSize;

    /* Verify if all blocks are connected as claimed */
    /* FIXME - to be disabled in the future */
    dvmCompilerDataFlowAnalysisDispatcher(&cUnit, verifyPredInfo,
                                          kAllNodes,
                                          false /* isIterative */);


    /* Perform SSA transformation for the whole method */
    dvmCompilerMethodSSATransformation(&cUnit);

    if (cUnit.printMe) dumpCFG(&cUnit, "/data/tombstones/");

    /* Reset the compiler resource pool */
    dvmCompilerArenaReset();

    return false;
}

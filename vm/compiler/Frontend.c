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
#include "libdex/OpCode.h"
#include "interp/Jit.h"
#include "CompilerInternals.h"

/*
 * Parse an instruction, return the length of the instruction
 */
static inline int parseInsn(const u2 *codePtr, DecodedInstruction *decInsn,
                            bool printMe)
{
    u2 instr = *codePtr;
    OpCode opcode = instr & 0xff;
    int insnWidth;

    // Don't parse instruction data
    if (opcode == OP_NOP && instr != 0) {
        return 0;
    } else {
        insnWidth = gDvm.instrWidth[opcode];
        if (insnWidth < 0) {
            insnWidth = -insnWidth;
        }
    }

    dexDecodeInstruction(gDvm.instrFormat, codePtr, decInsn);
    if (printMe) {
        char *decodedString = dvmCompilerGetDalvikDisassembly(decInsn);
        LOGD("%p: %#06x %s\n", codePtr, opcode, decodedString);
    }
    return insnWidth;
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
    switch (insn->dalvikInsn.opCode) {
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
    switch (insn->dalvikInsn.opCode) {
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
    switch (insn->dalvikInsn.opCode) {
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
#if defined(WITH_JIT_TUNING)
static int compareMethod(const CompilerMethodStats *m1,
                         const CompilerMethodStats *m2)
{
    return (int) m1->method - (int) m2->method;
}

/*
 * Analyze each method whose traces are ever compiled. Collect a variety of
 * statistics like the ratio of exercised vs overall code and code bloat
 * ratios.
 */
static CompilerMethodStats *analyzeMethodBody(const Method *method)
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
    realMethodEntry = dvmHashTableLookup(gDvmJit.methodStatsTable, hashValue,
                                         &dummyMethodEntry,
                                         (HashCompareFunc) compareMethod,
                                         false);

    /* Part of this method has been compiled before - just return the entry */
    if (realMethodEntry != NULL) {
        return realMethodEntry;
    }

    /*
     * First time to compile this method - set up a new entry in the hash table
     */
    realMethodEntry =
        (CompilerMethodStats *) calloc(1, sizeof(CompilerMethodStats));
    realMethodEntry->method = method;

    dvmHashTableLookup(gDvmJit.methodStatsTable, hashValue,
                       realMethodEntry,
                       (HashCompareFunc) compareMethod,
                       true);

    /* Count the number of instructions */
    while (codePtr < codeEnd) {
        DecodedInstruction dalvikInsn;
        int width = parseInsn(codePtr, &dalvikInsn, false);

        /* Terminate when the data section is seen */
        if (width == 0)
            break;

        insnSize += width;
        codePtr += width;
    }

    realMethodEntry->dalvikSize = insnSize * 2;
    return realMethodEntry;
}
#endif

/*
 * Crawl the stack of the thread that requesed compilation to see if any of the
 * ancestors are on the blacklist.
 */
bool filterMethodByCallGraph(Thread *thread, const char *curMethodName)
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
                     JitTranslationInfo *info, jmp_buf *bailPtr)
{
    const DexCode *dexCode = dvmGetMethodCode(desc->method);
    const JitTraceRun* currRun = &desc->trace[0];
    unsigned int curOffset = currRun->frag.startOffset;
    unsigned int numInsts = currRun->frag.numInsts;
    const u2 *codePtr = dexCode->insns + curOffset;
    int traceSize = 0;  // # of half-words
    const u2 *startCodePtr = codePtr;
    BasicBlock *startBB, *curBB, *lastBB;
    int numBlocks = 0;
    static int compilationId;
    CompilationUnit cUnit;
#if defined(WITH_JIT_TUNING)
    CompilerMethodStats *methodStats;
#endif

    /* If we've already compiled this trace, just return success */
    if (dvmJitGetCodeAddr(startCodePtr) && !info->discardResult) {
        return true;
    }

    compilationId++;
    memset(&cUnit, 0, sizeof(CompilationUnit));

#if defined(WITH_JIT_TUNING)
    /* Locate the entry to store compilation statistics for this method */
    methodStats = analyzeMethodBody(desc->method);
#endif

    /* Set the recover buffer pointer */
    cUnit.bailPtr = bailPtr;

    /* Initialize the printMe flag */
    cUnit.printMe = gDvmJit.printMe;

    /* Initialize the profile flag */
    cUnit.executionCount = gDvmJit.profile;

    /* Identify traces that we don't want to compile */
    if (gDvmJit.methodTable) {
        int len = strlen(desc->method->clazz->descriptor) +
                  strlen(desc->method->name) + 1;
        char *fullSignature = dvmCompilerNew(len, true);
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
    lastBB = startBB = curBB = dvmCompilerNewBB(kEntryBlock);
    curBB->startOffset = curOffset;
    curBB->id = numBlocks++;

    curBB = dvmCompilerNewBB(kDalvikByteCode);
    curBB->startOffset = curOffset;
    curBB->id = numBlocks++;

    /* Make the first real dalvik block the fallthrough of the entry block */
    startBB->fallThrough = curBB;
    lastBB->next = curBB;
    lastBB = curBB;

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
        insn = dvmCompilerNew(sizeof(MIR), true);
        insn->offset = curOffset;
        width = parseInsn(codePtr, &insn->dalvikInsn, cUnit.printMe);

        /* The trace should never incude instruction data */
        assert(width);
        insn->width = width;
        traceSize += width;
        dvmCompilerAppendMIR(curBB, insn);
        cUnit.numInsts++;
        /* Instruction limit reached - terminate the trace here */
        if (cUnit.numInsts >= numMaxInsts) {
            break;
        }
        if (--numInsts == 0) {
            if (currRun->frag.runEnd) {
                break;
            } else {
                curBB = dvmCompilerNewBB(kDalvikByteCode);
                lastBB->next = curBB;
                lastBB = curBB;
                curBB->id = numBlocks++;
                currRun++;
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
    for (curBB = startBB; curBB; curBB = curBB->next) {
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

        /* No backward branch in the trace - start searching the next BB */
        for (searchBB = curBB->next; searchBB; searchBB = searchBB->next) {
            if (targetOffset == searchBB->startOffset) {
                curBB->taken = searchBB;
            }
            if (fallThroughOffset == searchBB->startOffset) {
                curBB->fallThrough = searchBB;
            }
        }

        int flags = dexGetInstrFlags(gDvm.instrFlags,
                                     lastInsn->dalvikInsn.opCode);

        /*
         * Some blocks are ended by non-control-flow-change instructions,
         * currently only due to trace length constraint. In this case we need
         * to generate an explicit branch at the end of the block to jump to
         * the chaining cell.
         *
         * NOTE: INVOKE_DIRECT_EMPTY is actually not an invoke but a nop
         */
        curBB->needFallThroughBranch =
            ((flags & (kInstrCanBranch | kInstrCanSwitch | kInstrCanReturn |
                       kInstrInvoke)) == 0) ||
            (lastInsn->dalvikInsn.opCode == OP_INVOKE_DIRECT_EMPTY);

        if (curBB->taken == NULL &&
            curBB->fallThrough == NULL &&
            flags == (kInstrCanBranch | kInstrCanContinue) &&
            fallThroughOffset == startBB->startOffset) {
            BasicBlock *loopBranch = curBB;
            BasicBlock *exitBB;
            BasicBlock *exitChainingCell;

            if (cUnit.printMe) {
                LOGD("Natural loop detected!");
            }
            exitBB = dvmCompilerNewBB(kExitBlock);
            lastBB->next = exitBB;
            lastBB = exitBB;

            exitBB->startOffset = targetOffset;
            exitBB->id = numBlocks++;
            exitBB->needFallThroughBranch = true;

            loopBranch->taken = exitBB;
#if defined(WITH_SELF_VERIFICATION)
            BasicBlock *backwardCell =
                dvmCompilerNewBB(kChainingCellBackwardBranch);
            lastBB->next = backwardCell;
            lastBB = backwardCell;

            backwardCell->startOffset = startBB->startOffset;
            backwardCell->id = numBlocks++;
            loopBranch->fallThrough = backwardCell;
#elif defined(WITH_JIT_TUNING)
            if (gDvmJit.profile) {
                BasicBlock *backwardCell =
                    dvmCompilerNewBB(kChainingCellBackwardBranch);
                lastBB->next = backwardCell;
                lastBB = backwardCell;

                backwardCell->startOffset = startBB->startOffset;
                backwardCell->id = numBlocks++;
                loopBranch->fallThrough = backwardCell;
            } else {
                loopBranch->fallThrough = startBB->next;
            }
#else
            loopBranch->fallThrough = startBB->next;
#endif

            /* Create the chaining cell as the fallthrough of the exit block */
            exitChainingCell = dvmCompilerNewBB(kChainingCellNormal);
            lastBB->next = exitChainingCell;
            lastBB = exitChainingCell;

            exitChainingCell->startOffset = targetOffset;
            exitChainingCell->id = numBlocks++;

            exitBB->fallThrough = exitChainingCell;

            cUnit.hasLoop = true;
        }

        if (lastInsn->dalvikInsn.opCode == OP_PACKED_SWITCH ||
            lastInsn->dalvikInsn.opCode == OP_SPARSE_SWITCH) {
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
                    (lastInsn->dalvikInsn.opCode == OP_PACKED_SWITCH ?
                     2 : size * 2));

            /* One chaining cell for the first MAX_CHAINED_SWITCH_CASES cases */
            for (i = 0; i < maxChains; i++) {
                BasicBlock *caseChain = dvmCompilerNewBB(kChainingCellNormal);
                lastBB->next = caseChain;
                lastBB = caseChain;

                caseChain->startOffset = lastInsn->offset + targets[i];
                caseChain->id = numBlocks++;
            }

            /* One more chaining cell for the default case */
            BasicBlock *caseChain = dvmCompilerNewBB(kChainingCellNormal);
            lastBB->next = caseChain;
            lastBB = caseChain;

            caseChain->startOffset = lastInsn->offset + lastInsn->width;
            caseChain->id = numBlocks++;
        /* Fallthrough block not included in the trace */
        } else if (!isUnconditionalBranch(lastInsn) &&
                   curBB->fallThrough == NULL) {
            /*
             * If the chaining cell is after an invoke or
             * instruction that cannot change the control flow, request a hot
             * chaining cell.
             */
            if (isInvoke || curBB->needFallThroughBranch) {
                lastBB->next = dvmCompilerNewBB(kChainingCellHot);
            } else {
                lastBB->next = dvmCompilerNewBB(kChainingCellNormal);
            }
            lastBB = lastBB->next;
            lastBB->id = numBlocks++;
            lastBB->startOffset = fallThroughOffset;
            curBB->fallThrough = lastBB;
        }
        /* Target block not included in the trace */
        if (curBB->taken == NULL &&
            (isGoto(lastInsn) || isInvoke ||
            (targetOffset != UNKNOWN_TARGET && targetOffset != curOffset))) {
            BasicBlock *newBB;
            if (isInvoke) {
                /* Monomorphic callee */
                if (callee) {
                    newBB = dvmCompilerNewBB(kChainingCellInvokeSingleton);
                    newBB->startOffset = 0;
                    newBB->containingMethod = callee;
                /* Will resolve at runtime */
                } else {
                    newBB = dvmCompilerNewBB(kChainingCellInvokePredicted);
                    newBB->startOffset = 0;
                }
            /* For unconditional branches, request a hot chaining cell */
            } else {
#if !defined(WITH_SELF_VERIFICATION)
                newBB = dvmCompilerNewBB(flags & kInstrUnconditional ?
                                                  kChainingCellHot :
                                                  kChainingCellNormal);
                newBB->startOffset = targetOffset;
#else
                /* Handle branches that branch back into the block */
                if (targetOffset >= curBB->firstMIRInsn->offset &&
                    targetOffset <= curBB->lastMIRInsn->offset) {
                    newBB = dvmCompilerNewBB(kChainingCellBackwardBranch);
                } else {
                    newBB = dvmCompilerNewBB(flags & kInstrUnconditional ?
                                                      kChainingCellHot :
                                                      kChainingCellNormal);
                }
                newBB->startOffset = targetOffset;
#endif
            }
            newBB->id = numBlocks++;
            curBB->taken = newBB;
            lastBB->next = newBB;
            lastBB = newBB;
        }
    }

    /* Now create a special block to host PC reconstruction code */
    lastBB->next = dvmCompilerNewBB(kPCReconstruction);
    lastBB = lastBB->next;
    lastBB->id = numBlocks++;

    /* And one final block that publishes the PC and raise the exception */
    lastBB->next = dvmCompilerNewBB(kExceptionHandling);
    lastBB = lastBB->next;
    lastBB->id = numBlocks++;

    if (cUnit.printMe) {
        char* signature = dexProtoCopyMethodDescriptor(&desc->method->prototype);
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

    BasicBlock **blockList;

    cUnit.method = desc->method;
    cUnit.traceDesc = desc;
    cUnit.numBlocks = numBlocks;
    dvmInitGrowableList(&cUnit.pcReconstructionList, 8);
    blockList = cUnit.blockList =
        dvmCompilerNew(sizeof(BasicBlock *) * numBlocks, true);

    int i;

    for (i = 0, curBB = startBB; i < numBlocks; i++) {
        blockList[i] = curBB;
        curBB = curBB->next;
    }
    /* Make sure all blocks are added to the cUnit */
    assert(curBB == NULL);

    /* Preparation for SSA conversion */
    dvmInitializeSSAConversion(&cUnit);


    if (cUnit.hasLoop) {
        dvmCompilerLoopOpt(&cUnit);
    }
    else {
        dvmCompilerNonLoopAnalysis(&cUnit);
    }

    dvmCompilerInitializeRegAlloc(&cUnit);  // Needs to happen after SSA naming

    if (cUnit.printMe) {
        dvmCompilerDumpCompilationUnit(&cUnit);
    }

    /* Set the instruction set to use (NOTE: later components may change it) */
    cUnit.instructionSet = dvmCompilerInstructionSet();

    /* Allocate Registers */
    dvmCompilerRegAlloc(&cUnit);

    /* Convert MIR to LIR, etc. */
    dvmCompilerMIR2LIR(&cUnit);

    /* Convert LIR into machine code. */
    dvmCompilerAssembleLIR(&cUnit, info);

    if (cUnit.printMe) {
        if (cUnit.halveInstCount) {
            LOGD("Assembler aborted");
        } else {
            dvmCompilerCodegenDump(&cUnit);
        }
        LOGD("End %s%s, %d Dalvik instructions",
             desc->method->clazz->descriptor, desc->method->name,
             cUnit.numInsts);
    }

    /* Reset the compiler resource pool */
    dvmCompilerArenaReset();

    /* Success */
    if (!cUnit.halveInstCount) {
#if defined(WITH_JIT_TUNING)
        methodStats->nativeSize += cUnit.totalSize;
#endif
        return info->codeAddress != NULL;

    /* Halve the instruction count and retry again */
    } else {
        return dvmCompileTrace(desc, cUnit.numInsts / 2, info, bailPtr);
    }
}

/*
 * Similar to dvmCompileTrace, but the entity processed here is the whole
 * method.
 *
 * TODO: implementation will be revisited when the trace builder can provide
 * whole-method traces.
 */
bool dvmCompileMethod(const Method *method, JitTranslationInfo *info)
{
    const DexCode *dexCode = dvmGetMethodCode(method);
    const u2 *codePtr = dexCode->insns;
    const u2 *codeEnd = dexCode->insns + dexCode->insnsSize;
    int blockID = 0;
    unsigned int curOffset = 0;

    BasicBlock *firstBlock = dvmCompilerNewBB(kDalvikByteCode);
    firstBlock->id = blockID++;

    /* Allocate the bit-vector to track the beginning of basic blocks */
    BitVector *bbStartAddr = dvmCompilerAllocBitVector(dexCode->insnsSize+1,
                                                       false);
    dvmCompilerSetBit(bbStartAddr, 0);

    /*
     * Sequentially go through every instruction first and put them in a single
     * basic block. Identify block boundaries at the mean time.
     */
    while (codePtr < codeEnd) {
        MIR *insn = dvmCompilerNew(sizeof(MIR), true);
        insn->offset = curOffset;
        int width = parseInsn(codePtr, &insn->dalvikInsn, false);
        bool isInvoke = false;
        const Method *callee;
        insn->width = width;

        /* Terminate when the data section is seen */
        if (width == 0)
            break;
        dvmCompilerAppendMIR(firstBlock, insn);
        /*
         * Check whether this is a block ending instruction and whether it
         * suggests the start of a new block
         */
        unsigned int target = curOffset;

        /*
         * If findBlockBoundary returns true, it means the current instruction
         * is terminating the current block. If it is a branch, the target
         * address will be recorded in target.
         */
        if (findBlockBoundary(method, insn, curOffset, &target, &isInvoke,
                              &callee)) {
            dvmCompilerSetBit(bbStartAddr, curOffset + width);
            if (target != curOffset) {
                dvmCompilerSetBit(bbStartAddr, target);
            }
        }

        codePtr += width;
        /* each bit represents 16-bit quantity */
        curOffset += width;
    }

    /*
     * The number of blocks will be equal to the number of bits set to 1 in the
     * bit vector minus 1, because the bit representing the location after the
     * last instruction is set to one.
     */
    int numBlocks = dvmCountSetBits(bbStartAddr);
    if (dvmIsBitSet(bbStartAddr, dexCode->insnsSize)) {
        numBlocks--;
    }

    CompilationUnit cUnit;
    BasicBlock **blockList;

    memset(&cUnit, 0, sizeof(CompilationUnit));
    cUnit.method = method;
    blockList = cUnit.blockList =
        dvmCompilerNew(sizeof(BasicBlock *) * numBlocks, true);

    /*
     * Register the first block onto the list and start split it into block
     * boundaries from there.
     */
    blockList[0] = firstBlock;
    cUnit.numBlocks = 1;

    int i;
    for (i = 0; i < numBlocks; i++) {
        MIR *insn;
        BasicBlock *curBB = blockList[i];
        curOffset = curBB->lastMIRInsn->offset;

        for (insn = curBB->firstMIRInsn->next; insn; insn = insn->next) {
            /* Found the beginning of a new block, see if it is created yet */
            if (dvmIsBitSet(bbStartAddr, insn->offset)) {
                int j;
                for (j = 0; j < cUnit.numBlocks; j++) {
                    if (blockList[j]->firstMIRInsn->offset == insn->offset)
                        break;
                }

                /* Block not split yet - do it now */
                if (j == cUnit.numBlocks) {
                    BasicBlock *newBB = dvmCompilerNewBB(kDalvikByteCode);
                    newBB->id = blockID++;
                    newBB->firstMIRInsn = insn;
                    newBB->startOffset = insn->offset;
                    newBB->lastMIRInsn = curBB->lastMIRInsn;
                    curBB->lastMIRInsn = insn->prev;
                    insn->prev->next = NULL;
                    insn->prev = NULL;

                    /*
                     * If the insn is not an unconditional branch, set up the
                     * fallthrough link.
                     */
                    if (!isUnconditionalBranch(curBB->lastMIRInsn)) {
                        curBB->fallThrough = newBB;
                    }

                    /* enqueue the new block */
                    blockList[cUnit.numBlocks++] = newBB;
                    break;
                }
            }
        }
    }

    if (numBlocks != cUnit.numBlocks) {
        LOGE("Expect %d vs %d basic blocks\n", numBlocks, cUnit.numBlocks);
        dvmCompilerAbort(&cUnit);
    }

    /* Connect the basic blocks through the taken links */
    for (i = 0; i < numBlocks; i++) {
        BasicBlock *curBB = blockList[i];
        MIR *insn = curBB->lastMIRInsn;
        unsigned int target = insn->offset;
        bool isInvoke;
        const Method *callee;

        findBlockBoundary(method, insn, target, &target, &isInvoke, &callee);

        /* Found a block ended on a branch */
        if (target != insn->offset) {
            int j;
            /* Forward branch */
            if (target > insn->offset) {
                j = i + 1;
            } else {
                /* Backward branch */
                j = 0;
            }
            for (; j < numBlocks; j++) {
                if (blockList[j]->firstMIRInsn->offset == target) {
                    curBB->taken = blockList[j];
                    break;
                }
            }

            /* Don't create dummy block for the callee yet */
            if (j == numBlocks && !isInvoke) {
                LOGE("Target not found for insn %x: expect target %x\n",
                     curBB->lastMIRInsn->offset, target);
                dvmCompilerAbort(&cUnit);
            }
        }
    }

    /* Set the instruction set to use (NOTE: later components may change it) */
    cUnit.instructionSet = dvmCompilerInstructionSet();

    dvmCompilerMIR2LIR(&cUnit);

    dvmCompilerAssembleLIR(&cUnit, info);

    dvmCompilerDumpCompilationUnit(&cUnit);

    dvmCompilerArenaReset();

    return info->codeAddress != NULL;
}

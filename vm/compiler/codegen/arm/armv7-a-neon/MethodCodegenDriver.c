/*
 * Copyright (C) 2011 The Android Open Source Project
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

/* Handle the content in each basic block */
static bool methodBlockCodeGen(CompilationUnit *cUnit, BasicBlock *bb)
{
    MIR *mir;
    ArmLIR *labelList = (ArmLIR *) cUnit->blockLabelList;
    int blockId = bb->id;

    cUnit->curBlock = bb;
    labelList[blockId].operands[0] = bb->startOffset;

    /* Insert the block label */
    labelList[blockId].opcode = kArmPseudoNormalBlockLabel;
    dvmCompilerAppendLIR(cUnit, (LIR *) &labelList[blockId]);

    dvmCompilerClobberAllRegs(cUnit);
    dvmCompilerResetNullCheck(cUnit);

    ArmLIR *headLIR = NULL;

    if (bb->blockType == kMethodEntryBlock) {
        opImm(cUnit, kOpPush, (1 << rlr | 1 << rFP));
        opRegImm(cUnit, kOpSub, rFP,
                 sizeof(StackSaveArea) + cUnit->method->registersSize * 4);

    } else if (bb->blockType == kMethodExitBlock) {
        opImm(cUnit, kOpPop, (1 << rpc | 1 << rFP));
    }

    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {

        dvmCompilerResetRegPool(cUnit);
        if (gDvmJit.disableOpt & (1 << kTrackLiveTemps)) {
            dvmCompilerClobberAllRegs(cUnit);
        }

        if (gDvmJit.disableOpt & (1 << kSuppressLoads)) {
            dvmCompilerResetDefTracking(cUnit);
        }

        Opcode dalvikOpcode = mir->dalvikInsn.opcode;
        InstructionFormat dalvikFormat =
            dexGetFormatFromOpcode(dalvikOpcode);

        ArmLIR *boundaryLIR;

        /*
         * Don't generate the boundary LIR unless we are debugging this
         * trace or we need a scheduling barrier.
         */
        if (headLIR == NULL || cUnit->printMe == true) {
            boundaryLIR =
                newLIR2(cUnit, kArmPseudoDalvikByteCodeBoundary,
                        mir->offset,
                        (int) dvmCompilerGetDalvikDisassembly(
                            &mir->dalvikInsn, ""));
            /* Remember the first LIR for this block */
            if (headLIR == NULL) {
                headLIR = boundaryLIR;
                /* Set the first boundaryLIR as a scheduling barrier */
                headLIR->defMask = ENCODE_ALL;
            }
        }

        /* Don't generate the SSA annotation unless verbose mode is on */
        if (cUnit->printMe && mir->ssaRep) {
            char *ssaString = dvmCompilerGetSSAString(cUnit, mir->ssaRep);
            newLIR1(cUnit, kArmPseudoSSARep, (int) ssaString);
        }

        bool notHandled;
        switch (dalvikFormat) {
            case kFmt10t:
            case kFmt20t:
            case kFmt30t:
                notHandled = handleFmt10t_Fmt20t_Fmt30t(cUnit,
                          mir, bb, labelList);
                break;
            case kFmt10x:
                notHandled = handleFmt10x(cUnit, mir);
                break;
            case kFmt11n:
            case kFmt31i:
                notHandled = handleFmt11n_Fmt31i(cUnit, mir);
                break;
            case kFmt11x:
                notHandled = handleFmt11x(cUnit, mir);
                break;
            case kFmt12x:
                notHandled = handleFmt12x(cUnit, mir);
                break;
            case kFmt20bc:
            case kFmt40sc:
                notHandled = handleFmt20bc_Fmt40sc(cUnit, mir);
                break;
            case kFmt21c:
            case kFmt31c:
            case kFmt41c:
                notHandled = handleFmt21c_Fmt31c_Fmt41c(cUnit, mir);
                break;
            case kFmt21h:
                notHandled = handleFmt21h(cUnit, mir);
                break;
            case kFmt21s:
                notHandled = handleFmt21s(cUnit, mir);
                break;
            case kFmt21t:
                notHandled = handleFmt21t(cUnit, mir, bb, labelList);
                break;
            case kFmt22b:
            case kFmt22s:
                notHandled = handleFmt22b_Fmt22s(cUnit, mir);
                break;
            case kFmt22c:
            case kFmt52c:
                notHandled = handleFmt22c_Fmt52c(cUnit, mir);
                break;
            case kFmt22cs:
                notHandled = handleFmt22cs(cUnit, mir);
                break;
            case kFmt22t:
                notHandled = handleFmt22t(cUnit, mir, bb, labelList);
                break;
            case kFmt22x:
            case kFmt32x:
                notHandled = handleFmt22x_Fmt32x(cUnit, mir);
                break;
            case kFmt23x:
                notHandled = handleFmt23x(cUnit, mir);
                break;
            case kFmt31t:
                notHandled = handleFmt31t(cUnit, mir);
                break;
            case kFmt3rc:
            case kFmt35c:
            case kFmt5rc:
                notHandled = handleFmt35c_3rc_5rc(cUnit, mir, bb,
                                              labelList);
                break;
            case kFmt3rms:
            case kFmt35ms:
                notHandled = handleFmt35ms_3rms(cUnit, mir, bb,
                                                labelList);
                break;
            case kFmt35mi:
            case kFmt3rmi:
                notHandled = handleExecuteInline(cUnit, mir);
                break;
            case kFmt51l:
                notHandled = handleFmt51l(cUnit, mir);
                break;
            default:
                notHandled = true;
                break;
        }

        /* FIXME - to be implemented */
        if (notHandled == true && dalvikOpcode >= kNumPackedOpcodes) {
            notHandled = false;
        }

        if (notHandled) {
            LOGE("%#06x: Opcode 0x%x (%s) / Fmt %d not handled\n",
                 mir->offset,
                 dalvikOpcode, dexGetOpcodeName(dalvikOpcode),
                 dalvikFormat);
            dvmCompilerAbort(cUnit);
            break;
        }
    }

    if (headLIR) {
        /*
         * Eliminate redundant loads/stores and delay stores into later
         * slots
         */
        dvmCompilerApplyLocalOptimizations(cUnit, (LIR *) headLIR,
                                           cUnit->lastLIRInsn);

        /*
         * Generate an unconditional branch to the fallthrough block.
         */
        if (bb->fallThrough) {
            genUnconditionalBranch(cUnit,
                                   &labelList[bb->fallThrough->id]);
        }
    }
    return false;
}

void dvmCompilerMethodMIR2LIR(CompilationUnit *cUnit)
{
    // FIXME - enable method compilation for selected routines here
    if (strcmp(cUnit->method->name, "add")) return;

    /* Used to hold the labels of each block */
    cUnit->blockLabelList =
        (void *) dvmCompilerNew(sizeof(ArmLIR) * cUnit->numBlocks, true);

    dvmCompilerDataFlowAnalysisDispatcher(cUnit, methodBlockCodeGen,
                                          kPreOrderDFSTraversal,
                                          false /* isIterative */);

    dvmCompilerApplyGlobalOptimizations(cUnit);

    // FIXME - temporarily enable verbose printing for all methods
    cUnit->printMe = true;

#if defined(WITH_SELF_VERIFICATION)
    selfVerificationBranchInsertPass(cUnit);
#endif
}

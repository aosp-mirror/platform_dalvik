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
#include "vm/compiler/CompilerInternals.h"
#include "Armv5teLIR.h"

/*
 * Identify unconditional branches that jump to the immediate successor of the
 * branch itself.
 */
static void applyRedundantBranchElimination(CompilationUnit *cUnit)
{
    Armv5teLIR *thisLIR;

    for (thisLIR = (Armv5teLIR *) cUnit->firstLIRInsn;
         thisLIR != (Armv5teLIR *) cUnit->lastLIRInsn;
         thisLIR = NEXT_LIR(thisLIR)) {

        /* Branch to the next instruction */
        if (thisLIR->opCode == ARMV5TE_B_UNCOND) {
            Armv5teLIR *nextLIR = thisLIR;

            while (true) {
                nextLIR = NEXT_LIR(nextLIR);

                /*
                 * Is the branch target the next instruction?
                 */
                if (nextLIR == (Armv5teLIR *) thisLIR->generic.target) {
                    thisLIR->isNop = true;
                    break;
                }

                /*
                 * Found real useful stuff between the branch and the target
                 */
                if (!isPseudoOpCode(nextLIR->opCode) ||
                    nextLIR->opCode == ARMV5TE_PSEUDO_ALIGN4)
                    break;
            }
        }
    }
}

void dvmCompilerApplyGlobalOptimizations(CompilationUnit *cUnit)
{
    applyRedundantBranchElimination(cUnit);
}

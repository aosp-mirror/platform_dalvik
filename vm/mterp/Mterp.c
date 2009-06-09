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
 * Mterp entry point and support functions.
 */
#include "mterp/Mterp.h"

#include <stddef.h>


/*
 * Verify some constants used by the mterp interpreter.
 */
bool dvmCheckAsmConstants(void)
{
    extern char dvmAsmInstructionStart[];
    extern char dvmAsmInstructionEnd[];
    extern char dvmAsmSisterStart[];
    extern char dvmAsmSisterEnd[];

    bool failed = false;

#define ASM_DEF_VERIFY
#include "mterp/common/asm-constants.h"

    if (failed) {
        LOGE("Please correct the values in mterp/common/asm-constants.h\n");
        dvmAbort();
    }

    /*
     * If an instruction overflows the 64-byte handler size limit, it will
     * push everything up and alter the total size.  Check it here.
     */
    const int width = 64;
    int interpSize = dvmAsmInstructionEnd - dvmAsmInstructionStart;
    if (interpSize != 0 && interpSize != 256*width) {
        LOGE("ERROR: unexpected asm interp size %d\n", interpSize);
        LOGE("(did an instruction handler exceed %d bytes?)\n", width);
        dvmAbort();
    }
    int sisterSize = dvmAsmSisterEnd - dvmAsmSisterStart;
    LOGV("mterp: interp is %d bytes, sisters are %d bytes\n",
        interpSize, sisterSize);

    return !failed;
}


/*
 * "Standard" mterp entry point.  This sets up a "glue" structure and then
 * calls into the assembly interpreter implementation.
 *
 * (There is presently no "debug" entry point.)
 */
bool dvmMterpStd(Thread* self, InterpState* glue)
{
    int changeInterp;

    /* configure mterp items */
    glue->self = self;
    glue->methodClassDex = glue->method->clazz->pDvmDex;

    glue->interpStackEnd = self->interpStackEnd;
    glue->pSelfSuspendCount = &self->suspendCount;
#if defined(WITH_JIT)
    glue->pJitProfTable = gDvmJit.pProfTable;
#endif
#if defined(WITH_DEBUGGER)
    glue->pDebuggerActive = &gDvm.debuggerActive;
#endif
#if defined(WITH_PROFILER)
    glue->pActiveProfilers = &gDvm.activeProfilers;
#endif

    IF_LOGVV() {
        char* desc = dexProtoCopyMethodDescriptor(&glue->method->prototype);
        LOGVV("mterp threadid=%d entry %d: %s.%s %s\n",
            dvmThreadSelf()->threadId,
            glue->entryPoint,
            glue->method->clazz->descriptor,
            glue->method->name,
            desc);
        free(desc);
    }
    //LOGI("glue is %p, pc=%p, fp=%p\n", glue, glue->pc, glue->fp);
    //LOGI("first instruction is 0x%04x\n", glue->pc[0]);

    changeInterp = dvmMterpStdRun(glue);
    if (!changeInterp) {
        /* this is a "normal" exit; we're not coming back */
#ifdef LOG_INSTR
        LOGD("|-- Leaving interpreter loop");
#endif
        return false;
    } else {
        /* we're "standard", so switch to "debug" */
        LOGVV("  mterp returned, changeInterp=%d\n", changeInterp);
        glue->nextMode = INTERP_DBG;
        return true;
    }
}

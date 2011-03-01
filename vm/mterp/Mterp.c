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
    bool failed = false;

#ifndef DVM_NO_ASM_INTERP

    extern void* dvmAsmInstructionStart[];
    extern void* dvmAsmInstructionEnd[];

#define ASM_DEF_VERIFY
#include "mterp/common/asm-constants.h"

    if (failed) {
        LOGE("Please correct the values in mterp/common/asm-constants.h\n");
        dvmAbort();
    }

#ifndef DVM_JMP_TABLE_MTERP
    /*
     * If we're using computed goto instruction transitions, make sure
     * none of the handlers overflows the 64-byte limit.  This won't tell
     * which one did, but if any one is too big the total size will
     * overflow.
     */
    const int width = 64;
    int interpSize = (uintptr_t) dvmAsmInstructionEnd -
                     (uintptr_t) dvmAsmInstructionStart;
    if (interpSize != 0 && interpSize != kNumPackedOpcodes*width) {
        LOGE("ERROR: unexpected asm interp size %d\n", interpSize);
        LOGE("(did an instruction handler exceed %d bytes?)\n", width);
        dvmAbort();
    }
#endif

#endif // ndef DVM_NO_ASM_INTERP

    return !failed;
}


/*
 * "Standard" mterp entry point.
 * (There is presently no "debug" entry point.)
 */
bool dvmMterpStd(Thread* self)
{
    int changeInterp;

    /* configure mterp items */
    self->interpSave.methodClassDex = self->interpSave.method->clazz->pDvmDex;

#if defined(WITH_JIT)
    /*
     * FIXME: temporary workaround.  When we have the ability to
     * walk through the thread list to initialize mterp & JIT state,
     * elminate this line.
    */
    self->jitThreshold = gDvmJit.threshold;
#endif

    /* Handle method entry bookkeeping */
    if (self->debugIsMethodEntry) {
        self->debugIsMethodEntry = false;
        TRACE_METHOD_ENTER(self, self->interpSave.method);
    }

    IF_LOGVV() {
        char* desc = dexProtoCopyMethodDescriptor(
                         &self->interpSave.method->prototype);
        LOGVV("mterp threadid=%d entry %d: %s.%s %s\n",
            dvmThreadSelf()->threadId,
            self->entryPoint,
            self->method->clazz->descriptor,
            self->method->name,
            desc);
        free(desc);
    }
    //LOGI("self is %p, pc=%p, fp=%p\n", self, self->interpSave.pc,
    //      self->interpSave.fp);
    //LOGI("first instruction is 0x%04x\n", self->interpSave.pc[0]);

    changeInterp = dvmMterpStdRun(self);

#if defined(WITH_JIT)
    if (self->jitState != kJitSingleStep) {
        self->inJitCodeCache = NULL;
    }
#endif

    if (!changeInterp) {
        /* this is a "normal" exit; we're not coming back */
#ifdef LOG_INSTR
        LOGD("|-- Leaving interpreter loop");
#endif
        return false;
    } else {
        /* we're "standard", so switch to "debug" */
        LOGVV("  mterp returned, changeInterp=%d\n", changeInterp);
        self->nextMode = INTERP_DBG;
        return true;
    }
}

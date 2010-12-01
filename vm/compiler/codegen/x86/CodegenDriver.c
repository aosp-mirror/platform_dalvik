/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * This file contains codegen and support common to all supported
 * X86 variants.  It is included by:
 *
 *        Codegen-$(TARGET_ARCH_VARIANT).c
 *
 * which combines this common code with specific support found in the
 * applicable directory below this one.
 */

static int opcodeCoverage[kNumDalvikInstructions];
static intptr_t templateEntryOffsets[TEMPLATE_LAST_MARK];

/*
 * The following are the first-level codegen routines that analyze the format
 * of each bytecode then either dispatch special purpose codegen routines
 * or produce corresponding Thumb instructions directly.
 */

#if 0
static bool handleFmt10t_Fmt20t_Fmt30t(CompilationUnit *cUnit, MIR *mir,
                                       BasicBlock *bb, X86LIR *labelList)
{
    /* For OP_GOTO, OP_GOTO_16, and OP_GOTO_32 */
    return true;
}

static bool handleFmt10x(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt11n_Fmt31i(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt21h(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt20bc(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt21c_Fmt31c(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt11x(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt12x(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt21s(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt21t(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                         X86LIR *labelList)
{
    return true;
}

static bool handleFmt22b_Fmt22s(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt22c(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt22cs(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt22t(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                         X86LIR *labelList)
{
    return true;
}

static bool handleFmt22x_Fmt32x(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt23x(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt31t(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt35c_3rc(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                             X86LIR *labelList)
{
    return true;
}

static bool handleFmt35ms_3rms(CompilationUnit *cUnit, MIR *mir,
                               BasicBlock *bb, X86LIR *labelList)
{
    return true;
}

/*
 * NOTE: Handles both range and non-range versions (arguments
 * have already been normalized by this point).
 */
static bool handleExecuteInline(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}

static bool handleFmt51l(CompilationUnit *cUnit, MIR *mir)
{
    return true;
}
#endif


void dvmCompilerMIR2LIR(CompilationUnit *cUnit)
{
}

/* Accept the work and start compiling */
bool dvmCompilerDoWork(CompilerWorkOrder *work)
{
    bool res;

    if (gDvmJit.codeCacheFull) {
        return false;
    }

    switch (work->kind) {
        case kWorkOrderTrace:
            /* Start compilation with maximally allowed trace length */
            res = dvmCompileTrace(work->info, JIT_MAX_TRACE_LEN, &work->result,
                                  work->bailPtr, 0 /* no hints */);
            break;
        case kWorkOrderTraceDebug: {
            bool oldPrintMe = gDvmJit.printMe;
            gDvmJit.printMe = true;
            /* Start compilation with maximally allowed trace length */
            res = dvmCompileTrace(work->info, JIT_MAX_TRACE_LEN, &work->result,
                                  work->bailPtr, 0 /* no hints */);
            gDvmJit.printMe = oldPrintMe;
            break;
        }
        default:
            res = false;
            LOGE("Jit: unknown work order type");
            assert(0);  // Bail if debug build, discard otherwise
    }
    return res;
}

/* Architectural-specific debugging helpers go here */
void dvmCompilerArchDump(void)
{
    /* Print compiled opcode in this VM instance */
    int i, start, streak;
    char buf[1024];

    streak = i = 0;
    buf[0] = 0;
    while (opcodeCoverage[i] == 0 && i < kNumDalvikInstructions) {
        i++;
    }
    if (i == kNumDalvikInstructions) {
        return;
    }
    for (start = i++, streak = 1; i < kNumDalvikInstructions; i++) {
        if (opcodeCoverage[i]) {
            streak++;
        } else {
            if (streak == 1) {
                sprintf(buf+strlen(buf), "%x,", start);
            } else {
                sprintf(buf+strlen(buf), "%x-%x,", start, start + streak - 1);
            }
            streak = 0;
            while (opcodeCoverage[i] == 0 && i < kNumDalvikInstructions) {
                i++;
            }
            if (i < kNumDalvikInstructions) {
                streak = 1;
                start = i;
            }
        }
    }
    if (streak) {
        if (streak == 1) {
            sprintf(buf+strlen(buf), "%x", start);
        } else {
            sprintf(buf+strlen(buf), "%x-%x", start, start + streak - 1);
        }
    }
    if (strlen(buf)) {
        LOGD("dalvik.vm.jit.op = %s", buf);
    }
}

/* Common initialization routine for an architecture family */
bool dvmCompilerArchInit()
{
    return dvmCompilerArchVariantInit();
}

void *dvmCompilerGetInterpretTemplate()
{
      return (void*) ((int)gDvmJit.codeCache +
                      templateEntryOffsets[TEMPLATE_INTERPRET]);
}

void dvmCompilerInitializeRegAlloc(CompilationUnit *cUnit)
{
}

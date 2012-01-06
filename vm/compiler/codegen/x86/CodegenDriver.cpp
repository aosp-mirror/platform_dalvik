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

extern X86LIR *loadConstant(CompilationUnit *cUnit, int rDest, int value);
extern X86LIR *loadWordDisp(CompilationUnit *cUnit, int rBase,
                            int displacement, int rDest);
extern void dvmCompilerFlushAllRegs(CompilationUnit *cUnit);
extern void storeWordDisp(CompilationUnit *cUnit, int rBase,
                          int displacement, int rSrc);
extern X86LIR *opReg(CompilationUnit *cUnit, OpKind op, int rDestSrc);

static int opcodeCoverage[kNumPackedOpcodes];
static intptr_t templateEntryOffsets[TEMPLATE_LAST_MARK];

#if 0   // Avoid compiler warnings when x86 disabled during development
/*
 * Bail to the interpreter.  Will not return to this trace.
 * On entry, rPC must be set correctly.
 */
static void genPuntToInterp(CompilationUnit *cUnit, unsigned int offset)
{
    dvmCompilerFlushAllRegs(cUnit);
    loadConstant(cUnit, rPC, (int)(cUnit->method->insns + offset));
    loadWordDisp(cUnit, rEBP, 0, rECX);  // Get glue
    loadWordDisp(cUnit, rECX,
                 offsetof(Thread, jitToInterpEntries.dvmJitToInterpPunt),
                 rEAX);
    opReg(cUnit, kOpUncondBr, rEAX);
}

static void genInterpSingleStep(CompilationUnit *cUnit, MIR *mir)
{
    int flags = dexGetFlagsFromOpcode(mir->dalvikInsn.opcode);
    int flagsToCheck = kInstrCanBranch | kInstrCanSwitch | kInstrCanReturn |
                       kInstrCanThrow;

    //If already optimized out, just ignore
    if (mir->dalvikInsn.opcode == OP_NOP)
        return;

    //Ugly, but necessary.  Flush all Dalvik regs so Interp can find them
    dvmCompilerFlushAllRegs(cUnit);

    if ((mir->next == NULL) || (flags & flagsToCheck)) {
       genPuntToInterp(cUnit, mir->offset);
       return;
    }
    int entryAddr = offsetof(Thread,
                             jitToInterpEntries.dvmJitToInterpSingleStep);
    loadWordDisp(cUnit, rEBP, 0, rECX);  // Get glue
    loadWordDisp(cUnit, rECX, entryAddr, rEAX); // rEAX<- entry address
    /* rPC = dalvik pc */
    loadConstant(cUnit, rPC, (int) (cUnit->method->insns + mir->offset));
    /* rECX = dalvik pc of following instruction */
    loadConstant(cUnit, rECX, (int) (cUnit->method->insns + mir->next->offset));
    /* Pass on the stack */
    storeWordDisp(cUnit, rESP, OUT_ARG0, rECX);
    opReg(cUnit, kOpCall, rEAX);
}
#endif

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
    JitTraceDescription *desc;
    bool res;

    if (gDvmJit.codeCacheFull) {
        return false;
    }

    switch (work->kind) {
        case kWorkOrderTrace:
            /* Start compilation with maximally allowed trace length */
            desc = (JitTraceDescription *)work->info;
            res = dvmCompileTrace(desc, JIT_MAX_TRACE_LEN, &work->result,
                                  work->bailPtr, 0 /* no hints */);
            break;
        case kWorkOrderTraceDebug: {
            bool oldPrintMe = gDvmJit.printMe;
            gDvmJit.printMe = true;
            /* Start compilation with maximally allowed trace length */
            desc = (JitTraceDescription *)work->info;
            res = dvmCompileTrace(desc, JIT_MAX_TRACE_LEN, &work->result,
                                  work->bailPtr, 0 /* no hints */);
            gDvmJit.printMe = oldPrintMe;
            break;
        }
        default:
            res = false;
            ALOGE("Jit: unknown work order type");
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
    while (opcodeCoverage[i] == 0 && i < kNumPackedOpcodes) {
        i++;
    }
    if (i == kNumPackedOpcodes) {
        return;
    }
    for (start = i++, streak = 1; i < kNumPackedOpcodes; i++) {
        if (opcodeCoverage[i]) {
            streak++;
        } else {
            if (streak == 1) {
                sprintf(buf+strlen(buf), "%x,", start);
            } else {
                sprintf(buf+strlen(buf), "%x-%x,", start, start + streak - 1);
            }
            streak = 0;
            while (opcodeCoverage[i] == 0 && i < kNumPackedOpcodes) {
                i++;
            }
            if (i < kNumPackedOpcodes) {
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
        ALOGD("dalvik.vm.jit.op = %s", buf);
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

JitInstructionSetType dvmCompilerGetInterpretTemplateSet()
{
    return DALVIK_JIT_X86;
}

void dvmCompilerInitializeRegAlloc(CompilationUnit *cUnit)
{
}

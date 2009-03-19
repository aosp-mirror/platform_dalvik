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
 * Common subroutines and data.
 */

/*
 * Common code when a backwards branch is taken
 *
 * On entry:
 *   ebx (a.k.a. rINST_FULL) -> PC adjustment in 16-bit words
 */
common_backwardBranch:
    GET_GLUE(%ecx)
    call   common_periodicChecks      # Note: expects rPC to be preserved
    ADVANCE_PC_INDEXED(rINST_FULL)
    FETCH_INST()
    GOTO_NEXT

/*
 * Common invoke code (old-style).
 * TUNING:  Rewrite along lines of new armv5 code?
 *
 * On entry:
 *   eax = Method* methodToCall
 *   ecx = bool methodCallRange
 *   rINST trashed, must reload
 */
common_invokeOld:
    movl     %ecx,OUT_ARG1(%esp)     # arg1<- methodCallRange
    GET_GLUE(%ecx)
    movzwl  (rPC),rINST_FULL         # recover rINST
    movl     %eax,OUT_ARG2(%esp)     # arg2<- method
    movzwl   4(rPC),%eax             # eax<- GFED or CCCC
    SAVE_PC_TO_GLUE(%ecx)
    SAVE_FP_TO_GLUE(%ecx)
    movzbl   rINST_HI,rINST_FULL
    movl     rINST_FULL,OUT_ARG3(%esp)# arg3<- AA
    movl     %ecx,OUT_ARG0(%esp)     # arg0<- GLUE
    movl     %eax,OUT_ARG4(%esp)     # arg4<- GFED/CCCC
    call     dvmMterp_invokeMethod
    jmp      common_resumeAfterGlueCall


/*
 * Do we need the thread to be suspended or have debugger/profiling activity?
 *
 * On entry:
 *   ebx  -> PC adjustment in 16-bit words (must be preserved)
 *   ecx  -> GLUE pointer
 *
 * Note: A call will normally kill %eax, rPC/%edx and %ecx.  To
 *       streamline the normal case, this routine will preserve rPC and
 *       %ecx in addition to the normal caller save regs.  The save/restore
 *       is a bit ugly, but will happen in the relatively uncommon path.
 * TUNING: Might be worthwhile to inline this.
 * TODO: Basic-block style Jit will need a hook here as well.  Fold it into
 *       the suspendCount check so we can get both in 1 shot.
 */
common_periodicChecks:
    movl    offGlue_pSelfSuspendCount(%ecx),%eax    # eax <- &suspendCount
    cmpl    $$0,(%eax)
    jne     1f

#if defined(WITH_DEBUGGER) || defined(WITH_PROFILER)
#if defined(WITH_DEBUGGER)
    movl   offGlue_pDebuggerActive(%ecx),%eax      # eax <- &DebuggerActive
#endif
#if defined(WITH_PROFILER)
    movl   offGlue_pActiveProfilers(%ecx),%ecx     # ecx <- &ActiveProfilers
#endif
#if defined(WITH_DEBUGGER) && defined(WITH_PROFILER)
    movzbl (%eax),%eax             # eax <- debuggerActive (boolean)
    orl    (%ecx),%eax             # eax <- debuggerActive || activeProfilers
#elif defined(WITH_DEBUGGER)
    movzbl (%eax),%eax             # eax <- debuggerActive (boolean)
#elif defined(WITH_PROFILER)
    movl   (%ecx),%eax             # eax <= activeProfilers
#endif
    GET_GLUE(%ecx)                 # restore rGLUE
    testl  %eax,%eax
    jne    3f                      # one or both active - switch interp
#endif

    ret

    /* Check for suspend */
1:
    /*  At this point, the return pointer to the caller of
     *  common_periodicChecks is on the top of stack.  We need to preserve
     *  rPC(edx) and GLUE(ecx).  We'll spill rPC, and reload GLUE.
     *  The outgoing profile is:
     *      bool dvmCheckSuspendPending(Thread* self)
     *  Because we reached here via a call, go ahead and build a new frame.
     */
    EXPORT_PC()                         # need for precise GC
    movl    offGlue_self(%ecx),%eax      # eax<- glue->self
    SPILL(rPC)                      # save edx
    push    %ebp
    movl    %esp,%ebp
    subl    $$24,%esp
    movl    %eax,OUT_ARG0(%esp)
    call    dvmCheckSuspendPending
    addl    $$24,%esp
    pop     %ebp
    UNSPILL(rPC)
    GET_GLUE(%ecx)
    ret

    /* Switch interpreters */
    /* Note: %ebx contains the 16-bit word offset to be applied to rPC to
     * "complete" the interpretation of backwards branches.  In effect, we
     * are completing the interpretation of the branch instruction here,
     * and the new interpreter will resume interpretation at the branch
     * target. However, a switch request recognized during the handling
     * of a return from method instruction results in an immediate abort,
     * and the new interpreter will resume by re-interpreting the return
     * instruction.
     */
3:
    leal    (rPC,%ebx,2),rPC       # adjust pc to show target
    GET_GLUE(%ecx)                 # bail expect GLUE already loaded
    movl    $$1,rINST_FULL         # set changeInterp to true
    jmp     common_gotoBail


/*
 * Common code for handling a return instruction
 */
common_returnFromMethod:
    GET_GLUE(%ecx)
    /* Set entry mode in case we bail */
    movb    $$kInterpEntryReturn,offGlue_entryPoint(%ecx)
    xorl    rINST_FULL,rINST_FULL   # zero offset in case we switch interps
    call    common_periodicChecks   # Note: expects %ecx to be preserved

    SAVEAREA_FROM_FP(%eax,rFP)                    # eax<- saveArea (old)
    movl    offStackSaveArea_prevFrame(%eax),rFP  # rFP<- prevFrame
    movl    (offStackSaveArea_method-sizeofStackSaveArea)(rFP),rINST_FULL
    cmpl    $$0,rINST_FULL                        # break?
    je      common_gotoBail    # break frame, bail out completely

    movl    offStackSaveArea_savedPc(%eax),rPC    # pc<- saveArea->savedPC
    movl    offGlue_self(%ecx),%eax               # eax<- self
    movl    rINST_FULL,offGlue_method(%ecx)  # glue->method = newSave->meethod
    movl    rFP,offThread_curFrame(%eax)     # self->curFrame = fp
    movl    offMethod_clazz(rINST_FULL),%eax # eax<- method->clazz
    FETCH_INST_WORD(3)
    movl    offClassObject_pDvmDex(%eax),%eax # eax<- method->clazz->pDvmDex
    ADVANCE_PC(3)
    movl    %eax,offGlue_methodClassDex(%ecx)
    /* not bailing - restore entry mode to default */
    movb    $$kInterpEntryInstr,offGlue_entryPoint(%ecx)
    GOTO_NEXT

/*
 * Prepare to strip the current frame and "longjump" back to caller of
 * dvmMterpStdRun.
 *
 * on entry:
 *    rINST_FULL holds changeInterp
 *    ecx holds glue pointer
 *
 * expected profile: dvmMterpStdBail(MterpGlue *glue, bool changeInterp)
 */
common_gotoBail:
    SAVE_PC_TO_GLUE(%ecx)                # export state to glue
    SAVE_FP_TO_GLUE(%ecx)
    movl   %ecx,OUT_ARG0(%esp)           # glue in arg0
    movl   rINST_FULL,OUT_ARG1(%esp)     # changeInterp in arg1
    call    dvmMterpStdBail              # bail out....


/*
 * After returning from a "glued" function, pull out the updated values
 * and start executing at the next instruction.
 */
 common_resumeAfterGlueCall:
     GET_GLUE(%ecx)
     LOAD_PC_FROM_GLUE(%ecx)
     LOAD_FP_FROM_GLUE(%ecx)
     FETCH_INST()
     GOTO_NEXT

/*
 * Integer divide or mod by zero
 */
common_errDivideByZero:
    EXPORT_PC()
    movl    $$.LstrArithmeticException,%eax
    movl    %eax,OUT_ARG0(%esp)
    movl    $$.LstrDivideByZero,%eax
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    call    dvmThrowException
    UNSPILL(rPC)
    jmp     common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 */
common_errNegativeArraySize:
    EXPORT_PC()
    movl    $$.LstrNegativeArraySizeException,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    call    dvmThrowException
    UNSPILL(rPC)
    jmp     common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 */
common_errNoSuchMethod:

    EXPORT_PC()
    movl    $$.LstrNoSuchMethodError,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    call    dvmThrowException
    UNSPILL(rPC)
    jmp     common_exceptionThrown

/*
 * Hit a null object when we weren't expecting one.  Export the PC, throw a
 * NullPointerException and goto the exception processing code.
 */
common_errNullObject:
    EXPORT_PC()
    movl    $$.LstrNullPointerException,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    call    dvmThrowException
    UNSPILL(rPC)
    jmp     common_exceptionThrown

/*
 * Array index exceeds max.
 */
common_errArrayIndex:
    EXPORT_PC()
    movl    $$.LstrArrayIndexException,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    call    dvmThrowException
    UNSPILL(rPC)
    jmp     common_exceptionThrown
/*
 * Invalid array value.
 */
common_errArrayStore:
    EXPORT_PC()
    movl    $$.LstrArrayStoreException,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    call    dvmThrowException
    UNSPILL(rPC)
    jmp     common_exceptionThrown

/*
 * Somebody has thrown an exception.  Handle it.
 *
 * If the exception processing code returns to us (instead of falling
 * out of the interpreter), continue with whatever the next instruction
 * now happens to be.
 *
 * This does not return.
 */
common_exceptionThrown:
    GET_GLUE(%ecx)
    SAVE_PC_TO_GLUE(%ecx)
    SAVE_FP_TO_GLUE(%ecx)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmMterp_exceptionThrown
    jmp     common_resumeAfterGlueCall

common_abort:
    movl    $$0xdeadf00d,%eax
    call     *%eax


/*
 * Strings
 */

    .section     .rodata
.LstrNullPointerException:
    .asciz    "Ljava/lang/NullPointerException;"
.LstrArithmeticException:
    .asciz  "Ljava/lang/ArithmeticException;"
.LstrDivideByZero:
    .asciz  "divide by zero"
.LstrArrayIndexException:
    .asciz  "Ljava/lang/ArrayIndexOutOfBoundsException;"
.LstrArrayStoreException:
    .asciz  "Ljava/lang/ArrayStoreException;"
.LstrNegativeArraySizeException:
    .asciz  "Ljava/lang/NegativeArraySizeException;"
.LstrInstantiationError:
    .asciz  "Ljava/lang/InstantiationError;"
.LstrClassCastException:
    .asciz  "Ljava/lang/ClassCastException;"
.LstrNoSuchMethodError:
    .asciz  "Ljava/lang/NoSuchMethodError;"
.LstrInternalError:
    .asciz  "Ljava/lang/InternalError;"
.LstrFilledNewArrayNotImpl:
    .asciz  "filled-new-array only implemented for 'int'"


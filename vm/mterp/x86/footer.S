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

#if defined(WITH_JIT)
/*
 * JIT-related re-entries into the interpreter.  In general, if the
 * exit from a translation can at some point be chained, the entry
 * here requires that control arrived via a call, and that the "rp"
 * on TOS is actually a pointer to a 32-bit cell containing the Dalvik PC
 * of the next insn to handle.  If no chaining will happen, the entry
 * should be reached via a direct jump and rPC set beforehand.
 */

    .global dvmJitToInterpPunt
/*
 * The compiler will generate a jump to this entry point when it is
 * having difficulty translating a Dalvik instruction.  We must skip
 * the code cache lookup & prevent chaining to avoid bouncing between
 * the interpreter and code cache. rPC must be set on entry.
 */
dvmJitToInterpPunt:
#if defined(WITH_JIT_TUNING)
    movl   rPC, OUT_ARG0(%esp)
    call   dvmBumpPunt
#endif
    movl   rSELF, %ecx
    movl   offThread_curHandlerTable(%ecx),rIBASE
    FETCH_INST_R %ecx
    GOTO_NEXT_R %ecx

    .global dvmJitToInterpSingleStep
/*
 * Return to the interpreter to handle a single instruction.
 * Should be reached via a call.
 * On entry:
 *   0(%esp)          <= native return address within trace
 *   rPC              <= Dalvik PC of this instruction
 *   OUT_ARG0+4(%esp) <= Dalvik PC of next instruction
 */
dvmJitToInterpSingleStep:
    pop    %eax
    movl   rSELF, %ecx
    movl   OUT_ARG0(%esp), %edx
    movl   %eax,offThread_jitResumeNPC(%ecx)
    movl   %edx,offThread_jitResumeDPC(%ecx)
    movl   $$kInterpEntryInstr,offThread_entryPoint(%ecx)
    movl   $$1,rINST     # changeInterp <= true
    jmp    common_gotoBail

    .global dvmJitToInterpNoChainNoProfile
/*
 * Return from the translation cache to the interpreter to do method
 * invocation.  Check if the translation exists for the callee, but don't
 * chain to it. rPC must be set on entry.
 */
dvmJitToInterpNoChainNoProfile:
#if defined(WITH_JIT_TUNING)
    call   dvmBumpNoChain
#endif
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr        # is there a translation?
    movl   rSELF,%ecx                # ecx <- self
    movl   %eax,offThread_inJitCodeCache(%ecx)  # set inJitCodeCache flag
    cmpl   $$0, %eax
    jz     1f
    call   *%eax                     # exec translation if we've got one
    # won't return
1:
    movl   rSELF, %ecx
    movl   offThread_curHandlerTable(%ecx),rIBASE
    FETCH_INST_R %ecx
    GOTO_NEXT_R %ecx

/*
 * Return from the translation cache and immediately request a
 * translation fro the exit target, but don't attempt to chain.
 * rPC set on entry.
 */
    .global dvmJitToInterpTraceSelectNoChain
dvmJitToInterpTraceSelectNoChain:
#if defined(WITH_JIT_TUNING)
    call   dvmBumpNoChain
#endif
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr # is there a translation?
    movl   rSELF,%ecx
    cmpl   $$0,%eax
    movl   %eax,offThread_inJitCodeCache(%ecx)  # set inJitCodeCache flag
    jz     1f
    call   *%eax              # jump to tranlation
    # won't return

/* No Translation - request one */
1:
    GET_JIT_PROF_TABLE %ecx %eax
    cmpl   $$0, %eax          # JIT enabled?
    jnz    2f                 # Request one if so
    movl   rSELF, %ecx
    movl   offThread_curHandlerTable(%ecx),rIBASE
    FETCH_INST_R %ecx         # Continue interpreting if not
    GOTO_NEXT_R %ecx
2:
    movl   $$kJitTSelectRequestHot,rINST  # ask for trace select
    jmp    common_selectTrace

/*
 * Return from the translation cache and immediately request a
 * translation for the exit target.  Reached via a call, and
 * (TOS)->rPC.
 */
    .global dvmJitToInterpTraceSelect
dvmJitToInterpTraceSelect:
    pop    rINST           # save chain cell address in callee save reg
    movl   (rINST),rPC
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr # is there a translation?
    cmpl   $$0,%eax
    jz     1b                 # no - ask for one
    movl   %eax,OUT_ARG0(%esp)
# FIXME - need to adjust rINST to beginning of sequence
    movl   rINST,OUT_ARG1(%esp)
    call   dvmJitChain        # Attempt dvmJitChain(codeAddr,chainAddr)
    cmpl   $$0,%eax           # Success?
    jz     toInterpreter      # didn't chain - interpret
    call   *%eax
    # won't return

/*
 * Placeholder entries for x86 JIT
 */
    .global dvmJitToInterpBackwardBranch
dvmJitToInterpBackwardBranch:
    .global dvmJitToInterpNormal
dvmJitToInterpNormal:
    .global dvmJitToInterpNoChain
dvmJitToInterpNoChain:
toInterpreter:
    jmp  common_abort
#endif

/*
 * Common code when a backwards branch is taken
 *
 * On entry:
 *   ebx (a.k.a. rINST) -> PC adjustment in 16-bit words
 */
common_backwardBranch:
    movl    rSELF,%ecx
    call   common_periodicChecks  # rPC and ecx/rSELF preserved
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE %ecx rIBASE
    ADVANCE_PC_INDEXED rINST
    cmpl   $$0,rIBASE
    movl   offThread_curHandlerTable(%ecx),rIBASE
    FETCH_INST
    jz    1f                    # Profiling off - continue
    .global updateProfile
updateProfile:
common_updateProfile:
    # quick & dirty hash
    movl   rPC, %eax
    shrl   $$12, %eax
    xorl   rPC, %eax
    andl   $$((1<<JIT_PROF_SIZE_LOG_2)-1),%eax
    decb   (%edx,%eax)
    jz     2f
1:
    GOTO_NEXT
2:
/*
 * Here, we switch to the debug interpreter to request
 * trace selection.  First, though, check to see if there
 * is already a native translation in place (and, if so,
 * jump to it now.
 */
    GET_JIT_THRESHOLD %ecx rINST  # leaves rSELF in %ecx
    EXPORT_PC
    movb   rINSTbl,(%edx,%eax)   # reset counter
    movl   %ecx,rINST            # preserve rSELF
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr  # already have one?
    movl   %eax,offThread_inJitCodeCache(rINST)   # set the inJitCodeCache flag
    cmpl   $$0,%eax
    jz     1f
    call   *%eax        # FIXME: decide call vs/ jmp!.  No return either way
1:
    movl   $$kJitTSelectRequest,%eax
    # On entry, eax<- jitState, rPC valid
common_selectTrace:

    movl   rSELF,%ecx
    movl   %eax,offThread_jitState(%ecx)
    movl   $$kInterpEntryInstr,offThread_entryPoint(%ecx)
    movl   $$1,rINST
    jmp    common_gotoBail
#else
    movl   offThread_curHandlerTable(%ecx),rIBASE
    ADVANCE_PC_INDEXED rINST
    FETCH_INST
    GOTO_NEXT
#endif



/*
 * Common code for jumbo method invocation.
 *
 * On entry:
 *   eax = Method* methodToCall
 *   rINSTw trashed, must reload
 *   rIBASE trashed, must reload before resuming interpreter
 */

common_invokeMethodJumbo:
.LinvokeNewJumbo:

   /*
    * prepare to copy args to "outs" area of current frame
    */
    movzwl      6(rPC),rINST            # rINST<- BBBB
    movzwl      8(rPC), %ecx            # %ecx<- CCCC
    ADVANCE_PC 2                        # adjust pc to make return similar
    SAVEAREA_FROM_FP %edx               # %edx<- &StackSaveArea
    test        rINST, rINST
    movl        rINST, LOCAL0_OFFSET(%ebp) # LOCAL0_OFFSET(%ebp)<- BBBB
    jz          .LinvokeArgsDone        # no args; jump to args done
    jmp         .LinvokeRangeArgs       # handle args like invoke range

/*
 * Common code for method invocation with range.
 *
 * On entry:
 *   eax = Method* methodToCall
 *   rINSTw trashed, must reload
 *   rIBASE trashed, must reload before resuming interpreter
 */

common_invokeMethodRange:
.LinvokeNewRange:

   /*
    * prepare to copy args to "outs" area of current frame
    */

    movzbl      1(rPC),rINST       # rINST<- AA
    movzwl      4(rPC), %ecx            # %ecx<- CCCC
    SAVEAREA_FROM_FP %edx               # %edx<- &StackSaveArea
    test        rINST, rINST
    movl        rINST, LOCAL0_OFFSET(%ebp) # LOCAL0_OFFSET(%ebp)<- AA
    jz          .LinvokeArgsDone        # no args; jump to args done


   /*
    * %eax=methodToCall, %ecx=CCCC, LOCAL0_OFFSET(%ebp)=count, %edx=&outs (&stackSaveArea)
    * (very few methods have > 10 args; could unroll for common cases)
    */

.LinvokeRangeArgs:
    movl        %ebx, LOCAL1_OFFSET(%ebp)       # LOCAL1_OFFSET(%ebp)<- save %ebx
    lea         (rFP, %ecx, 4), %ecx    # %ecx<- &vCCCC
    shll        $$2, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET(%ebp)<- offset
    subl        LOCAL0_OFFSET(%ebp), %edx       # %edx<- update &outs
    shrl        $$2, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET(%ebp)<- offset
1:
    movl        (%ecx), %ebx            # %ebx<- vCCCC
    lea         4(%ecx), %ecx           # %ecx<- &vCCCC++
    subl        $$1, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET<- LOCAL0_OFFSET--
    movl        %ebx, (%edx)            # *outs<- vCCCC
    lea         4(%edx), %edx           # outs++
    jne         1b                      # loop if count (LOCAL0_OFFSET(%ebp)) not zero
    movl        LOCAL1_OFFSET(%ebp), %ebx       # %ebx<- restore %ebx
    jmp         .LinvokeArgsDone        # continue

   /*
    * %eax is "Method* methodToCall", the method we're trying to call
    * prepare to copy args to "outs" area of current frame
    * rIBASE trashed, must reload before resuming interpreter
    */

common_invokeMethodNoRange:
.LinvokeNewNoRange:
    movzbl      1(rPC),rINST       # rINST<- BA
    movl        rINST, LOCAL0_OFFSET(%ebp) # LOCAL0_OFFSET(%ebp)<- BA
    shrl        $$4, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET(%ebp)<- B
    je          .LinvokeArgsDone        # no args; jump to args done
    movzwl      4(rPC), %ecx            # %ecx<- GFED
    SAVEAREA_FROM_FP %edx               # %edx<- &StackSaveArea

   /*
    * %eax=methodToCall, %ecx=GFED, LOCAL0_OFFSET(%ebp)=count, %edx=outs
    */

.LinvokeNonRange:
    cmp         $$2, LOCAL0_OFFSET(%ebp)        # compare LOCAL0_OFFSET(%ebp) to 2
    movl        %ecx, LOCAL1_OFFSET(%ebp)       # LOCAL1_OFFSET(%ebp)<- GFED
    jl          1f                      # handle 1 arg
    je          2f                      # handle 2 args
    cmp         $$4, LOCAL0_OFFSET(%ebp)        # compare LOCAL0_OFFSET(%ebp) to 4
    jl          3f                      # handle 3 args
    je          4f                      # handle 4 args
5:
    andl        $$15, rINST             # rINSTw<- A
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, rINST, 4), %ecx   # %ecx<- vA
    movl        %ecx, (%edx)            # *outs<- vA
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
4:
    shr         $$12, %ecx              # %ecx<- G
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vG
    movl        %ecx, (%edx)            # *outs<- vG
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
3:
    and         $$0x0f00, %ecx          # %ecx<- 0F00
    shr         $$8, %ecx               # %ecx<- F
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vF
    movl        %ecx, (%edx)            # *outs<- vF
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
2:
    and         $$0x00f0, %ecx          # %ecx<- 00E0
    shr         $$4, %ecx               # %ecx<- E
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vE
    movl        %ecx, (%edx)            # *outs<- vE
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
1:
    and         $$0x000f, %ecx          # %ecx<- 000D
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vD
    movl        %ecx, -4(%edx)          # *--outs<- vD
0:

   /*
    * %eax is "Method* methodToCall", the method we're trying to call
    * find space for the new stack frame, check for overflow
    */

.LinvokeArgsDone:
    movzwl      offMethod_registersSize(%eax), %edx # %edx<- methodToCall->regsSize
    movzwl      offMethod_outsSize(%eax), %ecx # %ecx<- methodToCall->outsSize
    movl        %eax, LOCAL0_OFFSET(%ebp)       # LOCAL0_OFFSET<- methodToCall
    shl         $$2, %edx               # %edx<- update offset
    SAVEAREA_FROM_FP %eax               # %eax<- &StackSaveArea
    subl        %edx, %eax              # %eax<- newFP; (old savearea - regsSize)
    movl        rSELF,%edx              # %edx<- pthread
    movl        %eax, LOCAL1_OFFSET(%ebp)       # LOCAL1_OFFSET(%ebp)<- &outs
    subl        $$sizeofStackSaveArea, %eax # %eax<- newSaveArea (stack save area using newFP)
    movl        offThread_interpStackEnd(%edx), %edx # %edx<- self->interpStackEnd
    movl        %edx, LOCAL2_OFFSET(%ebp)       # LOCAL2_OFFSET<- self->interpStackEnd
    shl         $$2, %ecx               # %ecx<- update offset for outsSize
    movl        %eax, %edx              # %edx<- newSaveArea
    sub         %ecx, %eax              # %eax<- bottom; (newSaveArea - outsSize)
    cmp         LOCAL2_OFFSET(%ebp), %eax       # compare interpStackEnd and bottom
    movl        LOCAL0_OFFSET(%ebp), %eax       # %eax<- restore methodToCall
    jl          .LstackOverflow         # handle frame overflow

   /*
    * set up newSaveArea
    */

#ifdef EASY_GDB
    SAVEAREA_FROM_FP %ecx               # %ecx<- &StackSaveArea
    movl        %ecx, offStackSaveArea_prevSave(%edx) # newSaveArea->prevSave<- &outs
#endif
    movl        rSELF,%ecx                  # %ecx<- pthread
    movl        rFP, offStackSaveArea_prevFrame(%edx) # newSaveArea->prevFrame<- rFP
    movl        rPC, offStackSaveArea_savedPc(%edx) # newSaveArea->savedPc<- rPC
    testl       $$ACC_NATIVE, offMethod_accessFlags(%eax) # check for native call
    movl        %eax, offStackSaveArea_method(%edx) # newSaveArea->method<- method to call
    jne         .LinvokeNative          # handle native call

   /*
    * Update "self" values for the new method
    * %eax=methodToCall, LOCAL1_OFFSET(%ebp)=newFp
    */

    movl        offMethod_clazz(%eax), %edx # %edx<- method->clazz
    movl        offClassObject_pDvmDex(%edx), %edx # %edx<- method->clazz->pDvmDex
    movl        %eax, offThread_method(%ecx) # self->method<- methodToCall
    movl        %edx, offThread_methodClassDex(%ecx) # self->methodClassDex<- method->clazz->pDvmDex
    movl        offMethod_insns(%eax), rPC # rPC<- methodToCall->insns
    movl        LOCAL1_OFFSET(%ebp), rFP # rFP<- newFP
    movl        rFP, offThread_curFrame(%ecx) # self->curFrame<- newFP
    movl        offThread_curHandlerTable(%ecx),rIBASE
    FETCH_INST
    GOTO_NEXT                           # jump to methodToCall->insns

   /*
    * Prep for the native call
    * %eax=methodToCall, LOCAL1_OFFSET(%ebp)=newFP, %edx=newSaveArea
    */

.LinvokeNative:
    movl        rSELF, %ecx
    movl        offThread_jniLocal_topCookie(%ecx), rINST # rINST<- self->localRef->...
    movl        rINST, offStackSaveArea_localRefCookie(%edx) # newSaveArea->localRefCookie<- top
    movl        %edx, LOCAL2_OFFSET(%ebp)  # save newSaveArea
    movl        LOCAL1_OFFSET(%ebp), rINST # rINST<- newFP
    movl        rINST, offThread_curFrame(%ecx)  # self->curFrame<- newFP
    movl        %ecx, OUT_ARG3(%esp)    # push parameter self
    movl        %eax, OUT_ARG2(%esp)    # push parameter methodToCall
    lea         offThread_retval(%ecx), %ecx # %ecx<- &retval
    movl        %ecx, OUT_ARG1(%esp)    # push parameter &retval
    movl        rINST, OUT_ARG0(%esp)    # push parameter newFP
    call        *offMethod_nativeFunc(%eax) # call methodToCall->nativeFunc
7:
    movl        LOCAL2_OFFSET(%ebp), %ecx    # %ecx<- newSaveArea
    movl        rSELF, %eax             # %eax<- self
    movl        offStackSaveArea_localRefCookie(%ecx), %edx # %edx<- old top
    cmp         $$0, offThread_exception(%eax) # check for exception
    movl        rFP, offThread_curFrame(%eax) # self->curFrame<- rFP
    movl        %edx, offThread_jniLocal_topCookie(%eax) # new top <- old top
    jne         common_exceptionThrown  # handle exception
    movl        offThread_curHandlerTable(%eax),rIBASE
    FETCH_INST_OPCODE 3 %ecx
    ADVANCE_PC 3
    GOTO_NEXT_R %ecx                    # jump to next instruction

.LstackOverflow:    # eax=methodToCall
    movl        %eax, OUT_ARG1(%esp)    # push parameter methodToCall
    movl        rSELF,%eax              # %eax<- self
    movl        %eax, OUT_ARG0(%esp)    # push parameter self
    call        dvmHandleStackOverflow  # call: (Thread* self, Method* meth)
    jmp         common_exceptionThrown  # handle exception


/*
 * Do we need the thread to be suspended or have debugger/profiling activity?
 *
 * On entry:
 *   ebx  -> PC adjustment in 16-bit words (must be preserved)
 *   ecx  -> SELF pointer
 *   reentry type, e.g. kInterpEntryInstr stored in rSELF->entryPoint
 *
 * Note: A call will normally kill %eax and %ecx.  To
 *       streamline the normal case, this routine will preserve
 *       %ecx in addition to the normal caller save regs.  The save/restore
 *       is a bit ugly, but will happen in the relatively uncommon path.
 * TODO: Basic-block style Jit will need a hook here as well.  Fold it into
 *       the suspendCount check so we can get both in 1 shot.
 * TUNING: Improve scheduling here & do initial single test for all.
 */
common_periodicChecks:
    cmpl    $$0,offThread_suspendCount(%ecx)     # non-zero suspendCount?
    jne     1f

6:
    movl   offThread_pInterpBreak(%ecx),%eax    # eax <- &interpBreak
    cmpl   $$0,(%eax)              # something interesting happening?
    jne    3f                      # yes - switch interpreters
    ret

    /* Check for suspend */
1:
    /*  At this point, the return pointer to the caller of
     *  common_periodicChecks is on the top of stack.  We need to preserve
     *  SELF(ecx).
     *  The outgoing profile is:
     *      bool dvmCheckSuspendPending(Thread* self)
     *  Because we reached here via a call, go ahead and build a new frame.
     */
    EXPORT_PC                         # need for precise GC
    movl    %ecx,%eax                 # eax<- self
    push    %ebp
    movl    %esp,%ebp
    subl    $$24,%esp
    movl    %eax,OUT_ARG0(%esp)
    call    dvmCheckSuspendPending
    addl    $$24,%esp
    pop     %ebp
    movl    rSELF,%ecx

    /*
     * Need to check to see if debugger or profiler flags got set
     * while we were suspended.
     */
    jmp    6b

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
    movl    rSELF,%ecx             # bail expect SELF already loaded
    movl    $$1,rINST              # set changeInterp to true
    jmp     common_gotoBail


/*
 * Common code for handling a return instruction
 */
common_returnFromMethod:
    movl    rSELF,%ecx
    /* Set entry mode in case we bail */
    movb    $$kInterpEntryReturn,offThread_entryPoint(%ecx)
    xorl    rINST,rINST   # zero offset in case we switch interps
    call    common_periodicChecks   # Note: expects %ecx to be preserved

    SAVEAREA_FROM_FP %eax                         # eax<- saveArea (old)
    movl    offStackSaveArea_prevFrame(%eax),rFP  # rFP<- prevFrame
    movl    (offStackSaveArea_method-sizeofStackSaveArea)(rFP),rINST
    cmpl    $$0,rINST                             # break?
    je      common_gotoBail    # break frame, bail out completely

    movl    offStackSaveArea_savedPc(%eax),rPC    # pc<- saveArea->savedPC
    movl    rINST,offThread_method(%ecx)          # self->method = newSave->meethod
    movl    rFP,offThread_curFrame(%ecx)          # self->curFrame = fp
    movl    offMethod_clazz(rINST),%eax           # eax<- method->clazz
    movl    offThread_curHandlerTable(%ecx),rIBASE
    movl    offClassObject_pDvmDex(%eax),rINST    # rINST<- method->clazz->pDvmDex
    FETCH_INST_OPCODE 3 %eax
    movl    rINST,offThread_methodClassDex(%ecx)
    ADVANCE_PC 3
    /* not bailing - restore entry mode to default */
    movb    $$kInterpEntryInstr,offThread_entryPoint(%ecx)
    GOTO_NEXT_R %eax

/*
 * Prepare to strip the current frame and "longjump" back to caller of
 * dvmMterpStdRun.
 *
 * on entry:
 *    rINST holds changeInterp
 *    ecx holds self pointer
 *
 * expected profile: dvmMterpStdBail(Thread *self, bool changeInterp)
 */
common_gotoBail:
    movl   rPC,offThread_pc(%ecx)     # export state to self
    movl   rFP,offThread_fp(%ecx)
    movl   %ecx,OUT_ARG0(%esp)      # self in arg0
    movl   rINST,OUT_ARG1(%esp)     # changeInterp in arg1
    call   dvmMterpStdBail          # bail out....


/*
 * After returning from a "selfd" function, pull out the updated values
 * and start executing at the next instruction.
 */
 common_resumeAfterGlueCall:
     movl  rSELF, %eax
     movl  offThread_pc(%eax),rPC
     movl  offThread_fp(%eax),rFP
     movl  offThread_curHandlerTable(%eax),rIBASE
     FETCH_INST
     GOTO_NEXT

/*
 * Integer divide or mod by zero
 */
common_errDivideByZero:
    EXPORT_PC
    movl    $$.LstrDivideByZero,%eax
    movl    %eax,OUT_ARG0(%esp)
    call    dvmThrowArithmeticException
    jmp     common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 * On entry, len in eax
 */
common_errNegativeArraySize:
    EXPORT_PC
    movl    %eax,OUT_ARG0(%esp)                  # arg0<- len
    call    dvmThrowNegativeArraySizeException   # (len)
    jmp     common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 * On entry, method name in eax
 */
common_errNoSuchMethod:

    EXPORT_PC
    movl    %eax,OUT_ARG0(%esp)
    call    dvmThrowNoSuchMethodError
    jmp     common_exceptionThrown

/*
 * Hit a null object when we weren't expecting one.  Export the PC, throw a
 * NullPointerException and goto the exception processing code.
 */
common_errNullObject:
    EXPORT_PC
    xorl    %eax,%eax
    movl    %eax,OUT_ARG0(%esp)
    call    dvmThrowNullPointerException
    jmp     common_exceptionThrown

/*
 * Array index exceeds max.
 * On entry:
 *    eax <- array object
 *    ecx <- index
 */
common_errArrayIndex:
    EXPORT_PC
    movl    offArrayObject_length(%eax), %eax
    movl    %eax,OUT_ARG0(%esp)
    movl    %ecx,OUT_ARG1(%esp)
    call    dvmThrowArrayIndexOutOfBoundsException   # args (length, index)
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
    movl    rSELF,%ecx
    movl    rPC,offThread_pc(%ecx)
    movl    rFP,offThread_fp(%ecx)
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
.LstrDivideByZero:
    .asciz  "divide by zero"
.LstrFilledNewArrayNotImplA:
    .asciz  "filled-new-array only implemented for 'int'"


/*
 * ===========================================================================
 *  Common subroutines and data
 * ===========================================================================
 */



    .text
    .align  2

#if defined(WITH_JIT)
#if defined(WITH_SELF_VERIFICATION)
    .global dvmJitToInterpPunt
dvmJitToInterpPunt:
    mov    r2,#kSVSPunt                 @ r2<- interpreter entry point
    mov    r3, #0
    str    r3, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpSingleStep
dvmJitToInterpSingleStep:
    str    lr,[rSELF,#offThread_jitResumeNPC]
    str    r1,[rSELF,#offThread_jitResumeDPC]
    mov    r2,#kSVSSingleStep           @ r2<- interpreter entry point
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpNoChainNoProfile
dvmJitToInterpNoChainNoProfile:
    mov    r0,rPC                       @ pass our target PC
    mov    r2,#kSVSNoProfile            @ r2<- interpreter entry point
    mov    r3, #0                       @ 0 means !inJitCodeCache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpTraceSelectNoChain
dvmJitToInterpTraceSelectNoChain:
    mov    r0,rPC                       @ pass our target PC
    mov    r2,#kSVSTraceSelect          @ r2<- interpreter entry point
    mov    r3, #0                       @ 0 means !inJitCodeCache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpTraceSelect
dvmJitToInterpTraceSelect:
    ldr    r0,[lr, #-1]                 @ pass our target PC
    mov    r2,#kSVSTraceSelect          @ r2<- interpreter entry point
    mov    r3, #0                       @ 0 means !inJitCodeCache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpBackwardBranch
dvmJitToInterpBackwardBranch:
    ldr    r0,[lr, #-1]                 @ pass our target PC
    mov    r2,#kSVSBackwardBranch       @ r2<- interpreter entry point
    mov    r3, #0                       @ 0 means !inJitCodeCache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpNormal
dvmJitToInterpNormal:
    ldr    r0,[lr, #-1]                 @ pass our target PC
    mov    r2,#kSVSNormal               @ r2<- interpreter entry point
    mov    r3, #0                       @ 0 means !inJitCodeCache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return

    .global dvmJitToInterpNoChain
dvmJitToInterpNoChain:
    mov    r0,rPC                       @ pass our target PC
    mov    r2,#kSVSNoChain              @ r2<- interpreter entry point
    mov    r3, #0                       @ 0 means !inJitCodeCache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    b      jitSVShadowRunEnd            @ doesn't return
#else
/*
 * Return from the translation cache to the interpreter when the compiler is
 * having issues translating/executing a Dalvik instruction. We have to skip
 * the code cache lookup otherwise it is possible to indefinitely bouce
 * between the interpreter and the code cache if the instruction that fails
 * to be compiled happens to be at a trace start.
 */
    .global dvmJitToInterpPunt
dvmJitToInterpPunt:
    mov    rPC, r0
#if defined(WITH_JIT_TUNING)
    mov    r0,lr
    bl     dvmBumpPunt;
#endif
    EXPORT_PC()
    mov    r0, #0
    str    r0, [rSELF, #offThread_inJitCodeCache] @ Back to the interp land
    adrl   rIBASE, dvmAsmInstructionStart
    FETCH_INST()
    GET_INST_OPCODE(ip)
    GOTO_OPCODE(ip)

/*
 * Return to the interpreter to handle a single instruction.
 * On entry:
 *    r0 <= PC
 *    r1 <= PC of resume instruction
 *    lr <= resume point in translation
 */
    .global dvmJitToInterpSingleStep
dvmJitToInterpSingleStep:
    str    lr,[rSELF,#offThread_jitResumeNPC]
    str    r1,[rSELF,#offThread_jitResumeDPC]
    mov    r1,#kInterpEntryInstr
    @ enum is 4 byte in aapcs-EABI
    str    r1, [rSELF, #offThread_entryPoint]
    mov    rPC,r0
    EXPORT_PC()

    adrl   rIBASE, dvmAsmInstructionStart
    mov    r2,#kJitSingleStep     @ Ask for single step and then revert
    str    r2,[rSELF,#offThread_jitState]
    mov    r1,#1                  @ set changeInterp to bail to debug interp
    b      common_gotoBail

/*
 * Return from the translation cache and immediately request
 * a translation for the exit target.  Commonly used for callees.
 */
    .global dvmJitToInterpTraceSelectNoChain
dvmJitToInterpTraceSelectNoChain:
#if defined(WITH_JIT_TUNING)
    bl     dvmBumpNoChain
#endif
    mov    r0,rPC
    bl     dvmJitGetTraceAddr       @ Is there a translation?
    str    r0, [rSELF, #offThread_inJitCodeCache] @ set the inJitCodeCache flag
    mov    r1, rPC                  @ arg1 of translation may need this
    mov    lr, #0                   @  in case target is HANDLER_INTERPRET
    cmp    r0,#0                    @ !0 means translation exists
    bxne   r0                       @ continue native execution if so
    b      2f                       @ branch over to use the interpreter

/*
 * Return from the translation cache and immediately request
 * a translation for the exit target.  Commonly used following
 * invokes.
 */
    .global dvmJitToInterpTraceSelect
dvmJitToInterpTraceSelect:
    ldr    rPC,[lr, #-1]           @ get our target PC
    add    rINST,lr,#-5            @ save start of chain branch
    add    rINST, #-4              @  .. which is 9 bytes back
    mov    r0,rPC
    bl     dvmJitGetTraceAddr      @ Is there a translation?
    str    r0, [rSELF, #offThread_inJitCodeCache] @ set the inJitCodeCache flag
    cmp    r0,#0
    beq    2f
    mov    r1,rINST
    bl     dvmJitChain              @ r0<- dvmJitChain(codeAddr,chainAddr)
    mov    r1, rPC                  @ arg1 of translation may need this
    mov    lr, #0                   @ in case target is HANDLER_INTERPRET
    cmp    r0,#0                    @ successful chain?
    bxne   r0                       @ continue native execution
    b      toInterpreter            @ didn't chain - resume with interpreter

/* No translation, so request one if profiling isn't disabled*/
2:
    adrl   rIBASE, dvmAsmInstructionStart
    GET_JIT_PROF_TABLE(r0)
    FETCH_INST()
    cmp    r0, #0
    movne  r2,#kJitTSelectRequestHot   @ ask for trace selection
    bne    common_selectTrace
    GET_INST_OPCODE(ip)
    GOTO_OPCODE(ip)

/*
 * Return from the translation cache to the interpreter.
 * The return was done with a BLX from thumb mode, and
 * the following 32-bit word contains the target rPC value.
 * Note that lr (r14) will have its low-order bit set to denote
 * its thumb-mode origin.
 *
 * We'll need to stash our lr origin away, recover the new
 * target and then check to see if there is a translation available
 * for our new target.  If so, we do a translation chain and
 * go back to native execution.  Otherwise, it's back to the
 * interpreter (after treating this entry as a potential
 * trace start).
 */
    .global dvmJitToInterpNormal
dvmJitToInterpNormal:
    ldr    rPC,[lr, #-1]           @ get our target PC
    add    rINST,lr,#-5            @ save start of chain branch
    add    rINST,#-4               @ .. which is 9 bytes back
#if defined(WITH_JIT_TUNING)
    bl     dvmBumpNormal
#endif
    mov    r0,rPC
    bl     dvmJitGetTraceAddr      @ Is there a translation?
    str    r0, [rSELF, #offThread_inJitCodeCache] @ set the inJitCodeCache flag
    cmp    r0,#0
    beq    toInterpreter            @ go if not, otherwise do chain
    mov    r1,rINST
    bl     dvmJitChain              @ r0<- dvmJitChain(codeAddr,chainAddr)
    mov    r1, rPC                  @ arg1 of translation may need this
    mov    lr, #0                   @  in case target is HANDLER_INTERPRET
    cmp    r0,#0                    @ successful chain?
    bxne   r0                       @ continue native execution
    b      toInterpreter            @ didn't chain - resume with interpreter

/*
 * Return from the translation cache to the interpreter to do method invocation.
 * Check if translation exists for the callee, but don't chain to it.
 */
    .global dvmJitToInterpNoChainNoProfile
dvmJitToInterpNoChainNoProfile:
#if defined(WITH_JIT_TUNING)
    bl     dvmBumpNoChain
#endif
    mov    r0,rPC
    bl     dvmJitGetTraceAddr       @ Is there a translation?
    str    r0, [rSELF, #offThread_inJitCodeCache] @ set the inJitCodeCache flag
    mov    r1, rPC                  @ arg1 of translation may need this
    mov    lr, #0                   @  in case target is HANDLER_INTERPRET
    cmp    r0,#0
    bxne   r0                       @ continue native execution if so
    EXPORT_PC()
    adrl   rIBASE, dvmAsmInstructionStart
    FETCH_INST()
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction

/*
 * Return from the translation cache to the interpreter to do method invocation.
 * Check if translation exists for the callee, but don't chain to it.
 */
    .global dvmJitToInterpNoChain
dvmJitToInterpNoChain:
#if defined(WITH_JIT_TUNING)
    bl     dvmBumpNoChain
#endif
    mov    r0,rPC
    bl     dvmJitGetTraceAddr       @ Is there a translation?
    str    r0, [rSELF, #offThread_inJitCodeCache] @ set the inJitCodeCache flag
    mov    r1, rPC                  @ arg1 of translation may need this
    mov    lr, #0                   @  in case target is HANDLER_INTERPRET
    cmp    r0,#0
    bxne   r0                       @ continue native execution if so
#endif

/*
 * No translation, restore interpreter regs and start interpreting.
 * rSELF & rFP were preserved in the translated code, and rPC has
 * already been restored by the time we get here.  We'll need to set
 * up rIBASE & rINST, and load the address of the JitTable into r0.
 */
toInterpreter:
    EXPORT_PC()
    adrl   rIBASE, dvmAsmInstructionStart
    FETCH_INST()
    GET_JIT_PROF_TABLE(r0)
    @ NOTE: intended fallthrough

/*
 * Common code to update potential trace start counter, and initiate
 * a trace-build if appropriate.  On entry, rPC should point to the
 * next instruction to execute, and rINST should be already loaded with
 * the next opcode word, and r0 holds a pointer to the jit profile
 * table (pJitProfTable).
 */
common_testUpdateProfile:
    cmp     r0,#0
    GET_INST_OPCODE(ip)
    GOTO_OPCODE_IFEQ(ip)       @ if not profiling, fallthrough otherwise */

common_updateProfile:
    eor     r3,rPC,rPC,lsr #12 @ cheap, but fast hash function
    lsl     r3,r3,#(32 - JIT_PROF_SIZE_LOG_2)          @ shift out excess bits
    ldrb    r1,[r0,r3,lsr #(32 - JIT_PROF_SIZE_LOG_2)] @ get counter
    GET_INST_OPCODE(ip)
    subs    r1,r1,#1           @ decrement counter
    strb    r1,[r0,r3,lsr #(32 - JIT_PROF_SIZE_LOG_2)] @ and store it
    GOTO_OPCODE_IFNE(ip)       @ if not threshold, fallthrough otherwise */

/*
 * Here, we switch to the debug interpreter to request
 * trace selection.  First, though, check to see if there
 * is already a native translation in place (and, if so,
 * jump to it now).
 */

    GET_JIT_THRESHOLD(r1)
    strb    r1,[r0,r3,lsr #(32 - JIT_PROF_SIZE_LOG_2)] @ reset counter
    EXPORT_PC()
    mov     r0,rPC
    bl      dvmJitGetTraceAddr          @ r0<- dvmJitGetTraceAddr(rPC)
    str     r0, [rSELF, #offThread_inJitCodeCache] @ set the inJitCodeCache flag
    mov     r1, rPC                     @ arg1 of translation may need this
    mov     lr, #0                      @  in case target is HANDLER_INTERPRET
    cmp     r0,#0
#if !defined(WITH_SELF_VERIFICATION)
    bxne    r0                          @ jump to the translation
    mov     r2,#kJitTSelectRequest      @ ask for trace selection
    @ fall-through to common_selectTrace
#else
    moveq   r2,#kJitTSelectRequest      @ ask for trace selection
    beq     common_selectTrace
    /*
     * At this point, we have a target translation.  However, if
     * that translation is actually the interpret-only pseudo-translation
     * we want to treat it the same as no translation.
     */
    mov     r10, r0                     @ save target
    bl      dvmCompilerGetInterpretTemplate
    cmp     r0, r10                     @ special case?
    bne     jitSVShadowRunStart         @ set up self verification shadow space
    @ Need to clear the inJitCodeCache flag
    mov    r3, #0                       @ 0 means not in the JIT code cache
    str    r3, [rSELF, #offThread_inJitCodeCache] @ back to the interp land
    GET_INST_OPCODE(ip)
    GOTO_OPCODE(ip)
    /* no return */
#endif

/*
 * On entry:
 *  r2 is jit state, e.g. kJitTSelectRequest or kJitTSelectRequestHot
 */
common_selectTrace:

    str     r2,[rSELF,#offThread_jitState]
    mov     r2,#kInterpEntryInstr       @ normal entry reason
    str     r2,[rSELF,#offThread_entryPoint]
    mov     r1,#1                       @ set changeInterp
    b       common_gotoBail

#if defined(WITH_SELF_VERIFICATION)
/*
 * Save PC and registers to shadow memory for self verification mode
 * before jumping to native translation.
 * On entry:
 *    rPC, rFP, rSELF: the values that they should contain
 *    r10: the address of the target translation.
 */
jitSVShadowRunStart:
    mov     r0,rPC                      @ r0<- program counter
    mov     r1,rFP                      @ r1<- frame pointer
    mov     r2,rSELF                    @ r2<- self (Thread) pointer
    mov     r3,r10                      @ r3<- target translation
    bl      dvmSelfVerificationSaveState @ save registers to shadow space
    ldr     rFP,[r0,#offShadowSpace_shadowFP] @ rFP<- fp in shadow space
    bx      r10                         @ jump to the translation

/*
 * Restore PC, registers, and interpreter state to original values
 * before jumping back to the interpreter.
 */
jitSVShadowRunEnd:
    mov    r1,rFP                        @ pass ending fp
    mov    r3,rSELF                      @ pass self ptr for convenience
    bl     dvmSelfVerificationRestoreState @ restore pc and fp values
    ldr    rPC,[rSELF,#offThread_pc]     @ restore PC
    ldr    rFP,[rSELF,#offThread_fp]     @ restore FP
    ldr    r1,[r0,#offShadowSpace_svState] @ get self verification state
    cmp    r1,#0                         @ check for punt condition
    beq    1f
    mov    r2,#kJitSelfVerification      @ ask for self verification
    str    r2,[rSELF,#offThread_jitState]
    mov    r2,#kInterpEntryInstr         @ normal entry reason
    str    r2,[rSELF,#offThread_entryPoint]
    mov    r1,#1                         @ set changeInterp
    b      common_gotoBail

1:                                       @ exit to interpreter without check
    EXPORT_PC()
    adrl   rIBASE, dvmAsmInstructionStart
    FETCH_INST()
    GET_INST_OPCODE(ip)
    GOTO_OPCODE(ip)
#endif

#endif

/*
 * Common code when a backward branch is taken.
 *
 * TODO: we could avoid a branch by just setting r0 and falling through
 * into the common_periodicChecks code, and having a test on r0 at the
 * end determine if we should return to the caller or update & branch to
 * the next instr.
 *
 * On entry:
 *  r9 is PC adjustment *in bytes*
 */
common_backwardBranch:
    mov     r0, #kInterpEntryInstr
    bl      common_periodicChecks
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE(r0)
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    cmp     r0,#0
    bne     common_updateProfile
    GET_INST_OPCODE(ip)
    GOTO_OPCODE(ip)
#else
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#endif


/*
 * Need to see if the thread needs to be suspended or debugger/profiler
 * activity has begun.  If so, we suspend the thread or side-exit to
 * the debug interpreter as appropriate.
 *
 * The common case is no activity on any of these, so we want to figure
 * that out quickly.  If something is up, we can then sort out what.
 *
 * We want to be fast if the VM was built without debugger or profiler
 * support, but we also need to recognize that the system is usually
 * shipped with both of these enabled.
 *
 * TODO: reduce this so we're just checking a single location.
 *
 * On entry:
 *  r0 is reentry type, e.g. kInterpEntryInstr (for debugger/profiling)
 *  r9 is trampoline PC adjustment *in bytes*
 */
common_periodicChecks:
/* TUNING - make this a direct load when interpBreak moved to Thread */
    ldr     r1, [rSELF, #offThread_pInterpBreak] @ r3<- &interpBreak
    /* speculatively thread-specific suspend count */
    ldr     ip, [rSELF, #offThread_suspendCount]
    ldr     r1, [r1]                                @ r1<- interpBreak
    cmp     r1, #0                                  @ anything unusual?
    bxeq    lr                                      @ return if not
    /*
     * One or more interesting events have happened.  Figure out what.
     *
     * r0 still holds the reentry type.
     */
    cmp     ip, #0                      @ want suspend?
    beq     3f                          @ no, must be something else

    stmfd   sp!, {r0, lr}               @ preserve r0 and lr
#if defined(WITH_JIT)
    /*
     * Refresh the Jit's cached copy of profile table pointer.  This pointer
     * doubles as the Jit's on/off switch.
     */
    ldr     r3, [rSELF, #offThread_ppJitProfTable] @ r3<-&gDvmJit.pJitProfTable
    mov     r0, rSELF                  @ r0<- self
    ldr     r3, [r3] @ r3 <- pJitProfTable
    EXPORT_PC()                         @ need for precise GC
    str     r3, [rSELF, #offThread_pJitProfTable] @ refresh Jit's on/off switch
#else
    mov     r0, rSELF                   @ r0<- self
    EXPORT_PC()                         @ need for precise GC
#endif
    bl      dvmCheckSuspendPending      @ do full check, suspend if necessary
    ldmfd   sp!, {r0, lr}               @ restore r0 and lr

    /*
     * Reload the interpBreak flags - they may have changed while we
     * were suspended.
     */
/* TUNING - direct load when InterpBreak moved to Thread */
    ldr     r1, [rSELF, #offThread_pInterpBreak]   @ r1<- &interpBreak
    ldr     r1, [r1]                    @ r1<- interpBreak
3:
    /*
     * TODO: this code is too fragile.  Need a general mechanism
     * to identify what actions to take by submode.  Some profiling modes
     * (instruction count) need to single-step, while method tracing
     * may not.  Debugging with breakpoints can run unfettered, but
     * source-level single-stepping requires Dalvik singlestepping.
     * GC may require a one-shot action and then full-speed resumption.
     */
    ands    r1, #(kSubModeDebuggerActive | kSubModeEmulatorTrace | kSubModeInstCounting)
    bxeq    lr                          @ nothing to do, return

    @ debugger/profiler enabled, bail out; self->entryPoint was set above
    str     r0, [rSELF, #offThread_entryPoint]  @ store r0, need for debug/prof
    add     rPC, rPC, r9                @ update rPC
    mov     r1, #1                      @ "want switch" = true
    b       common_gotoBail             @ side exit


/*
 * The equivalent of "goto bail", this calls through the "bail handler".
 *
 * State registers will be saved to the "thread" area before bailing.
 *
 * On entry:
 *  r1 is "bool changeInterp", indicating if we want to switch to the
 *     other interpreter or just bail all the way out
 */
common_gotoBail:
    SAVE_PC_FP_TO_SELF()                @ export state to "thread"
    mov     r0, rSELF                   @ r0<- self ptr
    b       dvmMterpStdBail             @ call(self, changeInterp)

    @add     r1, r1, #1                  @ using (boolean+1)
    @add     r0, rSELF, #offThread_jmpBuf @ r0<- &self->jmpBuf
    @bl      _longjmp                    @ does not return
    @bl      common_abort


/*
 * Common code for jumbo method invocation.
 * NOTE: this adjusts rPC to account for the difference in instruction width.
 * As a result, the savedPc in the stack frame will not be wholly accurate. So
 * long as that is only used for source file line number calculations, we're
 * okay.
 *
 * On entry:
 *  r0 is "Method* methodToCall", the method we're trying to call
 */
common_invokeMethodJumbo:
.LinvokeNewJumbo:
    @ prepare to copy args to "outs" area of current frame
    add     rPC, rPC, #4                @ adjust pc to make return consistent
    FETCH(r2, 1)                        @ r2<- BBBB (arg count)
    SAVEAREA_FROM_FP(r10, rFP)          @ r10<- stack save area
    cmp     r2, #0                      @ no args?
    beq     .LinvokeArgsDone            @ if no args, skip the rest
    FETCH(r1, 2)                        @ r1<- CCCC
    b       .LinvokeRangeArgs           @ handle args like invoke range

/*
 * Common code for method invocation with range.
 *
 * On entry:
 *  r0 is "Method* methodToCall", the method we're trying to call
 */
common_invokeMethodRange:
.LinvokeNewRange:
    @ prepare to copy args to "outs" area of current frame
    movs    r2, rINST, lsr #8           @ r2<- AA (arg count) -- test for zero
    SAVEAREA_FROM_FP(r10, rFP)          @ r10<- stack save area
    beq     .LinvokeArgsDone            @ if no args, skip the rest
    FETCH(r1, 2)                        @ r1<- CCCC

.LinvokeRangeArgs:
    @ r0=methodToCall, r1=CCCC, r2=count, r10=outs
    @ (very few methods have > 10 args; could unroll for common cases)
    add     r3, rFP, r1, lsl #2         @ r3<- &fp[CCCC]
    sub     r10, r10, r2, lsl #2        @ r10<- "outs" area, for call args
1:  ldr     r1, [r3], #4                @ val = *fp++
    subs    r2, r2, #1                  @ count--
    str     r1, [r10], #4               @ *outs++ = val
    bne     1b                          @ ...while count != 0
    b       .LinvokeArgsDone

/*
 * Common code for method invocation without range.
 *
 * On entry:
 *  r0 is "Method* methodToCall", the method we're trying to call
 */
common_invokeMethodNoRange:
.LinvokeNewNoRange:
    @ prepare to copy args to "outs" area of current frame
    movs    r2, rINST, lsr #12          @ r2<- B (arg count) -- test for zero
    SAVEAREA_FROM_FP(r10, rFP)          @ r10<- stack save area
    FETCH(r1, 2)                        @ r1<- GFED (load here to hide latency)
    beq     .LinvokeArgsDone

    @ r0=methodToCall, r1=GFED, r2=count, r10=outs
.LinvokeNonRange:
    rsb     r2, r2, #5                  @ r2<- 5-r2
    add     pc, pc, r2, lsl #4          @ computed goto, 4 instrs each
    bl      common_abort                @ (skipped due to ARM prefetch)
5:  and     ip, rINST, #0x0f00          @ isolate A
    ldr     r2, [rFP, ip, lsr #6]       @ r2<- vA (shift right 8, left 2)
    mov     r0, r0                      @ nop
    str     r2, [r10, #-4]!             @ *--outs = vA
4:  and     ip, r1, #0xf000             @ isolate G
    ldr     r2, [rFP, ip, lsr #10]      @ r2<- vG (shift right 12, left 2)
    mov     r0, r0                      @ nop
    str     r2, [r10, #-4]!             @ *--outs = vG
3:  and     ip, r1, #0x0f00             @ isolate F
    ldr     r2, [rFP, ip, lsr #6]       @ r2<- vF
    mov     r0, r0                      @ nop
    str     r2, [r10, #-4]!             @ *--outs = vF
2:  and     ip, r1, #0x00f0             @ isolate E
    ldr     r2, [rFP, ip, lsr #2]       @ r2<- vE
    mov     r0, r0                      @ nop
    str     r2, [r10, #-4]!             @ *--outs = vE
1:  and     ip, r1, #0x000f             @ isolate D
    ldr     r2, [rFP, ip, lsl #2]       @ r2<- vD
    mov     r0, r0                      @ nop
    str     r2, [r10, #-4]!             @ *--outs = vD
0:  @ fall through to .LinvokeArgsDone

.LinvokeArgsDone: @ r0=methodToCall
    ldrh    r9, [r0, #offMethod_registersSize]  @ r9<- methodToCall->regsSize
    ldrh    r3, [r0, #offMethod_outsSize]  @ r3<- methodToCall->outsSize
    ldr     r2, [r0, #offMethod_insns]  @ r2<- method->insns
    ldr     rINST, [r0, #offMethod_clazz]  @ rINST<- method->clazz
    @ find space for the new stack frame, check for overflow
    SAVEAREA_FROM_FP(r1, rFP)           @ r1<- stack save area
    sub     r1, r1, r9, lsl #2          @ r1<- newFp (old savearea - regsSize)
    SAVEAREA_FROM_FP(r10, r1)           @ r10<- newSaveArea
@    bl      common_dumpRegs
    ldr     r9, [rSELF, #offThread_interpStackEnd]    @ r9<- interpStackEnd
    sub     r3, r10, r3, lsl #2         @ r3<- bottom (newsave - outsSize)
    cmp     r3, r9                      @ bottom < interpStackEnd?
    ldr     lr, [rSELF, #offThread_pInterpBreak]
    ldr     r3, [r0, #offMethod_accessFlags] @ r3<- methodToCall->accessFlags
    blo     .LstackOverflow             @ yes, this frame will overflow stack

    @ set up newSaveArea
    ldr     lr, [lr]                    @ lr<- active submodes
#ifdef EASY_GDB
    SAVEAREA_FROM_FP(ip, rFP)           @ ip<- stack save area
    str     ip, [r10, #offStackSaveArea_prevSave]
#endif
    str     rFP, [r10, #offStackSaveArea_prevFrame]
    str     rPC, [r10, #offStackSaveArea_savedPc]
#if defined(WITH_JIT)
    mov     r9, #0
    str     r9, [r10, #offStackSaveArea_returnAddr]
#endif
    ands    lr, #kSubModeMethodTrace    @ method tracing?
    beq     1f                          @ skip if not
    stmfd   sp!, {r0-r3}                @ preserve r0-r3
    mov     r1, r6
    @ r0=methodToCall, r1=rSELF
    bl      dvmFastMethodTraceEnter
    ldmfd   sp!, {r0-r3}                @ restore r0-r3
1:
    str     r0, [r10, #offStackSaveArea_method]
    tst     r3, #ACC_NATIVE
    bne     .LinvokeNative

    /*
    stmfd   sp!, {r0-r3}
    bl      common_printNewline
    mov     r0, rFP
    mov     r1, #0
    bl      dvmDumpFp
    ldmfd   sp!, {r0-r3}
    stmfd   sp!, {r0-r3}
    mov     r0, r1
    mov     r1, r10
    bl      dvmDumpFp
    bl      common_printNewline
    ldmfd   sp!, {r0-r3}
    */

    ldrh    r9, [r2]                        @ r9 <- load INST from new PC
    ldr     r3, [rINST, #offClassObject_pDvmDex] @ r3<- method->clazz->pDvmDex
    mov     rPC, r2                         @ publish new rPC

    @ Update state values for the new method
    @ r0=methodToCall, r1=newFp, r3=newMethodClass, r9=newINST
    str     r0, [rSELF, #offThread_method]    @ self->method = methodToCall
    str     r3, [rSELF, #offThread_methodClassDex] @ self->methodClassDex = ...
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE(r0)
    mov     rFP, r1                         @ fp = newFp
    GET_PREFETCHED_OPCODE(ip, r9)           @ extract prefetched opcode from r9
    mov     rINST, r9                       @ publish new rINST
    str     r1, [rSELF, #offThread_curFrame]   @ self->curFrame = newFp
    cmp     r0,#0
    bne     common_updateProfile
    GOTO_OPCODE(ip)                         @ jump to next instruction
#else
    mov     rFP, r1                         @ fp = newFp
    GET_PREFETCHED_OPCODE(ip, r9)           @ extract prefetched opcode from r9
    mov     rINST, r9                       @ publish new rINST
    str     r1, [rSELF, #offThread_curFrame]   @ self->curFrame = newFp
    GOTO_OPCODE(ip)                         @ jump to next instruction
#endif

.LinvokeNative:
    @ Prep for the native call
    @ r0=methodToCall, r1=newFp, r10=newSaveArea
    ldr     lr, [rSELF, #offThread_pInterpBreak]
    ldr     r9, [rSELF, #offThread_jniLocal_topCookie]@r9<-thread->localRef->...
    str     r1, [rSELF, #offThread_curFrame]   @ self->curFrame = newFp
    str     r9, [r10, #offStackSaveArea_localRefCookie] @newFp->localRefCookie=top
    ldr     lr, [lr]                    @ lr<- active submodes

    mov     r2, r0                      @ r2<- methodToCall
    mov     r0, r1                      @ r0<- newFp (points to args)
    add     r1, rSELF, #offThread_retval  @ r1<- &retval
    mov     r3, rSELF                   @ arg3<- self

#ifdef ASSIST_DEBUGGER
    /* insert fake function header to help gdb find the stack frame */
    b       .Lskip
    .type   dalvik_mterp, %function
dalvik_mterp:
    .fnstart
    MTERP_ENTRY1
    MTERP_ENTRY2
.Lskip:
#endif

    ands    lr, #kSubModeMethodTrace    @ method tracing?
    beq     110f                        @ hop if not
    @ r2=JNIMethod, r6=rSELF
    stmfd   sp!, {r2,r6}

    mov     lr, pc                      @ set return addr
    ldr     pc, [r2, #offMethod_nativeFunc] @ pc<- methodToCall->nativeFunc

    @ r0=JNIMethod, r1=rSELF
    ldmfd   sp!, {r0-r1}
    bl      dvmFastNativeMethodTraceExit
    b       220f
110:
    mov     lr, pc                      @ set return addr
    ldr     pc, [r2, #offMethod_nativeFunc] @ pc<- methodToCall->nativeFunc
220:
#if defined(WITH_JIT)
    ldr     r3, [rSELF, #offThread_ppJitProfTable] @ Refresh Jit's on/off status
#endif

    @ native return; r10=newSaveArea
    @ equivalent to dvmPopJniLocals
    ldr     r0, [r10, #offStackSaveArea_localRefCookie] @ r0<- saved top
    ldr     r1, [rSELF, #offThread_exception] @ check for exception
#if defined(WITH_JIT)
    ldr     r3, [r3]                    @ r3 <- gDvmJit.pProfTable
#endif
    str     rFP, [rSELF, #offThread_curFrame]  @ self->curFrame = fp
    cmp     r1, #0                      @ null?
    str     r0, [rSELF, #offThread_jniLocal_topCookie] @ new top <- old top
#if defined(WITH_JIT)
    str     r3, [rSELF, #offThread_pJitProfTable] @ refresh cached on/off switch
#endif
    bne     common_exceptionThrown      @ no, handle exception

    FETCH_ADVANCE_INST(3)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction

.LstackOverflow:    @ r0=methodToCall
    mov     r1, r0                      @ r1<- methodToCall
    mov     r0, rSELF                   @ r0<- self
    bl      dvmHandleStackOverflow
    b       common_exceptionThrown
#ifdef ASSIST_DEBUGGER
    .fnend
    .size   dalvik_mterp, .-dalvik_mterp
#endif


    /*
     * Common code for method invocation, calling through "glue code".
     *
     * TODO: now that we have range and non-range invoke handlers, this
     *       needs to be split into two.  Maybe just create entry points
     *       that set r9 and jump here?
     *
     * On entry:
     *  r0 is "Method* methodToCall", the method we're trying to call
     *  r9 is "bool methodCallRange", indicating if this is a /range variant
     */
     .if    0
.LinvokeOld:
    sub     sp, sp, #8                  @ space for args + pad
    FETCH(ip, 2)                        @ ip<- FEDC or CCCC
    mov     r2, r0                      @ A2<- methodToCall
    mov     r0, rSELF                   @ A0<- self
    SAVE_PC_FP_TO_SELF()                @ export state to "self"
    mov     r1, r9                      @ A1<- methodCallRange
    mov     r3, rINST, lsr #8           @ A3<- AA
    str     ip, [sp, #0]                @ A4<- ip
    bl      dvmMterp_invokeMethod       @ call the C invokeMethod
    add     sp, sp, #8                  @ remove arg area
    b       common_resumeAfterGlueCall  @ continue to next instruction
    .endif



/*
 * Common code for handling a return instruction.
 *
 * This does not return.
 */
common_returnFromMethod:
.LreturnNew:
    mov     r0, #kInterpEntryReturn
    mov     r9, #0
    bl      common_periodicChecks

    ldr     lr, [rSELF, #offThread_pInterpBreak]
    SAVEAREA_FROM_FP(r0, rFP)
    ldr     lr, [lr]                    @ lr<- active submodes
    ldr     r9, [r0, #offStackSaveArea_savedPc] @ r9 = saveArea->savedPc
    ands    lr, #kSubModeMethodTrace    @ method tracing?
    beq     333f
    stmfd   sp!, {r0-r3}                @ preserve r0-r3
    mov     r0, r6
    @ r0=rSELF
    bl      dvmFastJavaMethodTraceExit
    ldmfd   sp!, {r0-r3}                @ restore r0-r3
333:
    ldr     rFP, [r0, #offStackSaveArea_prevFrame] @ fp = saveArea->prevFrame
    ldr     r2, [rFP, #(offStackSaveArea_method - sizeofStackSaveArea)]
                                        @ r2<- method we're returning to
    cmp     r2, #0                      @ is this a break frame?
#if defined(WORKAROUND_CORTEX_A9_745320)
    /* Don't use conditional loads if the HW defect exists */
    beq     101f
    ldr     r10, [r2, #offMethod_clazz] @ r10<- method->clazz
101:
#else
    ldrne   r10, [r2, #offMethod_clazz] @ r10<- method->clazz
#endif
    mov     r1, #0                      @ "want switch" = false
    beq     common_gotoBail             @ break frame, bail out completely

    PREFETCH_ADVANCE_INST(rINST, r9, 3) @ advance r9, update new rINST
    str     r2, [rSELF, #offThread_method]@ self->method = newSave->method
    ldr     r1, [r10, #offClassObject_pDvmDex]   @ r1<- method->clazz->pDvmDex
    str     rFP, [rSELF, #offThread_curFrame]  @ self->curFrame = fp
#if defined(WITH_JIT)
    ldr     r10, [r0, #offStackSaveArea_returnAddr] @ r10 = saveArea->returnAddr
    mov     rPC, r9                     @ publish new rPC
    str     r1, [rSELF, #offThread_methodClassDex]
    str     r10, [rSELF, #offThread_inJitCodeCache]  @ may return to JIT'ed land
    cmp     r10, #0                      @ caller is compiled code
    blxne   r10
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#else
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    mov     rPC, r9                     @ publish new rPC
    str     r1, [rSELF, #offThread_methodClassDex]
    GOTO_OPCODE(ip)                     @ jump to next instruction
#endif

    /*
     * Return handling, calls through "glue code".
     */
     .if    0
.LreturnOld:
    SAVE_PC_FP_TO_SELF()                @ export state
    mov     r0, rSELF                   @ arg to function
    bl      dvmMterp_returnFromMethod
    b       common_resumeAfterGlueCall
    .endif


/*
 * Somebody has thrown an exception.  Handle it.
 *
 * If the exception processing code returns to us (instead of falling
 * out of the interpreter), continue with whatever the next instruction
 * now happens to be.
 *
 * This does not return.
 */
     .global dvmMterpCommonExceptionThrown
dvmMterpCommonExceptionThrown:
common_exceptionThrown:
.LexceptionNew:
    mov     r0, #kInterpEntryThrow
    mov     r9, #0
    bl      common_periodicChecks

    ldr     r9, [rSELF, #offThread_exception] @ r9<- self->exception
    mov     r1, rSELF                   @ r1<- self
    mov     r0, r9                      @ r0<- exception
    bl      dvmAddTrackedAlloc          @ don't let the exception be GCed
    mov     r3, #0                      @ r3<- NULL
    str     r3, [rSELF, #offThread_exception] @ self->exception = NULL

    /* set up args and a local for "&fp" */
    /* (str sp, [sp, #-4]!  would be perfect here, but is discouraged) */
    str     rFP, [sp, #-4]!             @ *--sp = fp
    mov     ip, sp                      @ ip<- &fp
    mov     r3, #0                      @ r3<- false
    str     ip, [sp, #-4]!              @ *--sp = &fp
    ldr     r1, [rSELF, #offThread_method] @ r1<- self->method
    mov     r0, rSELF                   @ r0<- self
    ldr     r1, [r1, #offMethod_insns]  @ r1<- method->insns
    mov     r2, r9                      @ r2<- exception
    sub     r1, rPC, r1                 @ r1<- pc - method->insns
    mov     r1, r1, asr #1              @ r1<- offset in code units

    /* call, r0 gets catchRelPc (a code-unit offset) */
    bl      dvmFindCatchBlock           @ call(self, relPc, exc, scan?, &fp)

    /* fix earlier stack overflow if necessary; may trash rFP */
    ldrb    r1, [rSELF, #offThread_stackOverflowed]
    cmp     r1, #0                      @ did we overflow earlier?
    beq     1f                          @ no, skip ahead
    mov     rFP, r0                     @ save relPc result in rFP
    mov     r0, rSELF                   @ r0<- self
    mov     r1, r9                      @ r1<- exception
    bl      dvmCleanupStackOverflow     @ call(self)
    mov     r0, rFP                     @ restore result
1:

    /* update frame pointer and check result from dvmFindCatchBlock */
    ldr     rFP, [sp, #4]               @ retrieve the updated rFP
    cmp     r0, #0                      @ is catchRelPc < 0?
    add     sp, sp, #8                  @ restore stack
    bmi     .LnotCaughtLocally

    /* adjust locals to match self->curFrame and updated PC */
    SAVEAREA_FROM_FP(r1, rFP)           @ r1<- new save area
    ldr     r1, [r1, #offStackSaveArea_method] @ r1<- new method
    str     r1, [rSELF, #offThread_method]  @ self->method = new method
    ldr     r2, [r1, #offMethod_clazz]      @ r2<- method->clazz
    ldr     r3, [r1, #offMethod_insns]      @ r3<- method->insns
    ldr     r2, [r2, #offClassObject_pDvmDex] @ r2<- method->clazz->pDvmDex
    add     rPC, r3, r0, asl #1             @ rPC<- method->insns + catchRelPc
    str     r2, [rSELF, #offThread_methodClassDex] @ self->pDvmDex = meth...

    /* release the tracked alloc on the exception */
    mov     r0, r9                      @ r0<- exception
    mov     r1, rSELF                   @ r1<- self
    bl      dvmReleaseTrackedAlloc      @ release the exception

    /* restore the exception if the handler wants it */
    FETCH_INST()                        @ load rINST from rPC
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    cmp     ip, #OP_MOVE_EXCEPTION      @ is it "move-exception"?
    streq   r9, [rSELF, #offThread_exception] @ yes, restore the exception
    GOTO_OPCODE(ip)                     @ jump to next instruction

.LnotCaughtLocally: @ r9=exception
    /* fix stack overflow if necessary */
    ldrb    r1, [rSELF, #offThread_stackOverflowed]
    cmp     r1, #0                      @ did we overflow earlier?
    movne   r0, rSELF                   @ if yes: r0<- self
    movne   r1, r9                      @ if yes: r1<- exception
    blne    dvmCleanupStackOverflow     @ if yes: call(self)

    @ may want to show "not caught locally" debug messages here
#if DVM_SHOW_EXCEPTION >= 2
    /* call __android_log_print(prio, tag, format, ...) */
    /* "Exception %s from %s:%d not caught locally" */
    @ dvmLineNumFromPC(method, pc - method->insns)
    ldr     r0, [rSELF, #offThread_method]
    ldr     r1, [r0, #offMethod_insns]
    sub     r1, rPC, r1
    asr     r1, r1, #1
    bl      dvmLineNumFromPC
    str     r0, [sp, #-4]!
    @ dvmGetMethodSourceFile(method)
    ldr     r0, [rSELF, #offThread_method]
    bl      dvmGetMethodSourceFile
    str     r0, [sp, #-4]!
    @ exception->clazz->descriptor
    ldr     r3, [r9, #offObject_clazz]
    ldr     r3, [r3, #offClassObject_descriptor]
    @
    ldr     r2, strExceptionNotCaughtLocally
    ldr     r1, strLogTag
    mov     r0, #3                      @ LOG_DEBUG
    bl      __android_log_print
#endif
    str     r9, [rSELF, #offThread_exception] @ restore exception
    mov     r0, r9                      @ r0<- exception
    mov     r1, rSELF                   @ r1<- self
    bl      dvmReleaseTrackedAlloc      @ release the exception
    mov     r1, #0                      @ "want switch" = false
    b       common_gotoBail             @ bail out


    /*
     * Exception handling, calls through "glue code".
     */
    .if     0
.LexceptionOld:
    SAVE_PC_FP_TO_SELF()                @ export state
    mov     r0, rSELF                   @ arg to function
    bl      dvmMterp_exceptionThrown
    b       common_resumeAfterGlueCall
    .endif


/*
 * After returning from a "glued" function, pull out the updated
 * values and start executing at the next instruction.
 */
common_resumeAfterGlueCall:
    LOAD_PC_FP_FROM_SELF()              @ pull rPC and rFP out of thread
    FETCH_INST()                        @ load rINST from rPC
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction

/*
 * Invalid array index. Note that our calling convention is strange; we use r1
 * and r3 because those just happen to be the registers all our callers are
 * using. We shuffle them here before calling the C function.
 * r1: index
 * r3: size
 */
common_errArrayIndex:
    EXPORT_PC()
    mov     r0, r1
    mov     r1, r3
    bl      dvmThrowAIOOBE
    b       common_exceptionThrown

/*
 * Integer divide or mod by zero.
 */
common_errDivideByZero:
    EXPORT_PC()
    ldr     r0, strArithmeticException
    ldr     r1, strDivideByZero
    bl      dvmThrowException
    b       common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 */
common_errNegativeArraySize:
    EXPORT_PC()
    ldr     r0, strNegativeArraySizeException
    mov     r1, #0
    bl      dvmThrowException
    b       common_exceptionThrown

/*
 * Invocation of a non-existent method.
 */
common_errNoSuchMethod:
    EXPORT_PC()
    ldr     r0, strNoSuchMethodError
    mov     r1, #0
    bl      dvmThrowException
    b       common_exceptionThrown

/*
 * We encountered a null object when we weren't expecting one.  We
 * export the PC, throw a NullPointerException, and goto the exception
 * processing code.
 */
common_errNullObject:
    EXPORT_PC()
    ldr     r0, strNullPointerException
    mov     r1, #0
    bl      dvmThrowException
    b       common_exceptionThrown

/*
 * For debugging, cause an immediate fault.  The source address will
 * be in lr (use a bl instruction to jump here).
 */
common_abort:
    ldr     pc, .LdeadFood
.LdeadFood:
    .word   0xdeadf00d

/*
 * Spit out a "we were here", preserving all registers.  (The attempt
 * to save ip won't work, but we need to save an even number of
 * registers for EABI 64-bit stack alignment.)
 */
    .macro  SQUEAK num
common_squeak\num:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    ldr     r0, strSqueak
    mov     r1, #\num
    bl      printf
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr
    .endm

    SQUEAK  0
    SQUEAK  1
    SQUEAK  2
    SQUEAK  3
    SQUEAK  4
    SQUEAK  5

/*
 * Spit out the number in r0, preserving registers.
 */
common_printNum:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    mov     r1, r0
    ldr     r0, strSqueak
    bl      printf
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr

/*
 * Print a newline, preserving registers.
 */
common_printNewline:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    ldr     r0, strNewline
    bl      printf
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr

    /*
     * Print the 32-bit quantity in r0 as a hex value, preserving registers.
     */
common_printHex:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    mov     r1, r0
    ldr     r0, strPrintHex
    bl      printf
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr

/*
 * Print the 64-bit quantity in r0-r1, preserving registers.
 */
common_printLong:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    mov     r3, r1
    mov     r2, r0
    ldr     r0, strPrintLong
    bl      printf
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr

/*
 * Print full method info.  Pass the Method* in r0.  Preserves regs.
 */
common_printMethod:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bl      dvmMterpPrintMethod
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr

/*
 * Call a C helper function that dumps regs and possibly some
 * additional info.  Requires the C function to be compiled in.
 */
    .if     0
common_dumpRegs:
    stmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bl      dvmMterpDumpArmRegs
    ldmfd   sp!, {r0, r1, r2, r3, ip, lr}
    bx      lr
    .endif

#if 0
/*
 * Experiment on VFP mode.
 *
 * uint32_t setFPSCR(uint32_t val, uint32_t mask)
 *
 * Updates the bits specified by "mask", setting them to the values in "val".
 */
setFPSCR:
    and     r0, r0, r1                  @ make sure no stray bits are set
    fmrx    r2, fpscr                   @ get VFP reg
    mvn     r1, r1                      @ bit-invert mask
    and     r2, r2, r1                  @ clear masked bits
    orr     r2, r2, r0                  @ set specified bits
    fmxr    fpscr, r2                   @ set VFP reg
    mov     r0, r2                      @ return new value
    bx      lr

    .align  2
    .global dvmConfigureFP
    .type   dvmConfigureFP, %function
dvmConfigureFP:
    stmfd   sp!, {ip, lr}
    /* 0x03000000 sets DN/FZ */
    /* 0x00009f00 clears the six exception enable flags */
    bl      common_squeak0
    mov     r0, #0x03000000             @ r0<- 0x03000000
    add     r1, r0, #0x9f00             @ r1<- 0x03009f00
    bl      setFPSCR
    ldmfd   sp!, {ip, pc}
#endif


/*
 * String references, must be close to the code that uses them.
 */
    .align  2
strArithmeticException:
    .word   .LstrArithmeticException
strDivideByZero:
    .word   .LstrDivideByZero
strNegativeArraySizeException:
    .word   .LstrNegativeArraySizeException
strNoSuchMethodError:
    .word   .LstrNoSuchMethodError
strNullPointerException:
    .word   .LstrNullPointerException

strLogTag:
    .word   .LstrLogTag
strExceptionNotCaughtLocally:
    .word   .LstrExceptionNotCaughtLocally

strNewline:
    .word   .LstrNewline
strSqueak:
    .word   .LstrSqueak
strPrintHex:
    .word   .LstrPrintHex
strPrintLong:
    .word   .LstrPrintLong

/*
 * Zero-terminated ASCII string data.
 *
 * On ARM we have two choices: do like gcc does, and LDR from a .word
 * with the address, or use an ADR pseudo-op to get the address
 * directly.  ADR saves 4 bytes and an indirection, but it's using a
 * PC-relative addressing mode and hence has a limited range, which
 * makes it not work well with mergeable string sections.
 */
    .section .rodata.str1.4,"aMS",%progbits,1

.LstrBadEntryPoint:
    .asciz  "Bad entry point %d\n"
.LstrArithmeticException:
    .asciz  "Ljava/lang/ArithmeticException;"
.LstrDivideByZero:
    .asciz  "divide by zero"
.LstrFilledNewArrayNotImpl:
    .asciz  "filled-new-array only implemented for objects and 'int'"
.LstrInternalError:
    .asciz  "Ljava/lang/InternalError;"
.LstrInstantiationError:
    .asciz  "Ljava/lang/InstantiationError;"
.LstrNegativeArraySizeException:
    .asciz  "Ljava/lang/NegativeArraySizeException;"
.LstrNoSuchMethodError:
    .asciz  "Ljava/lang/NoSuchMethodError;"
.LstrNullPointerException:
    .asciz  "Ljava/lang/NullPointerException;"

.LstrLogTag:
    .asciz  "mterp"
.LstrExceptionNotCaughtLocally:
    .asciz  "Exception %s from %s:%d not caught locally\n"

.LstrNewline:
    .asciz  "\n"
.LstrSqueak:
    .asciz  "<%d>"
.LstrPrintHex:
    .asciz  "<0x%x>"
.LstrPrintLong:
    .asciz  "<%lld>"

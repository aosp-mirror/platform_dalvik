%verify "executed"
%verify "unimplemented array type"
    /*
     * Create a new array with elements filled from registers.
     */
    /* filled-new-array/jumbo {vCCCC..v(CCCC+BBBB-1)}, type@AAAAAAAA */
    movl    rGLUE,%eax
    movl    offGlue_methodClassDex(%eax),%eax # eax<- pDvmDex
    movl    2(rPC),%ecx                       # ecx<- AAAAAAAA
    movl    offDvmDex_pResClasses(%eax),%eax  # eax<- pDvmDex->pResClasses
    movl    (%eax,%ecx,4),%eax                # eax<- resolved class
    EXPORT_PC
    testl   %eax,%eax                         # already resolved?
    jne     .L${opcode}_continue              # yes, continue
    # less frequent path, so we'll redo some work
    movl    rGLUE,%eax
    movl    $$0,OUT_ARG2(%esp)                # arg2<- false
    movl    %ecx,OUT_ARG1(%esp)               # arg1<- AAAAAAAA
    movl    offGlue_method(%eax),%eax         # eax<- glue->method
    jmp     .L${opcode}_more
%break

.L${opcode}_more:
    movl    offMethod_clazz(%eax),%eax        # eax<- method->clazz
    movl    %eax,OUT_ARG0(%esp)               # arg0<- clazz
    call    dvmResolveClass                   # eax<- call(clazz,ref,flag)
    testl   %eax,%eax                         # null?
    je      common_exceptionThrown            # yes, handle it

       # note: fall through to .L${opcode}_continue

    /*
     * On entry:
     *    eax holds array class [r0]
     *    ecx is scratch
     */
.L${opcode}_continue:
    movl    offClassObject_descriptor(%eax),%ecx  # ecx<- arrayClass->descriptor
    movl    $$ALLOC_DONT_TRACK,OUT_ARG2(%esp)     # arg2<- flags
    movzbl  1(%ecx),%ecx                          # ecx<- descriptor[1]
    movl    %eax,OUT_ARG0(%esp)                   # arg0<- arrayClass
    movl    rGLUE,%eax
    cmpb    $$'I',%cl                             # supported?
    je      1f
    cmpb    $$'L',%cl
    je      1f
    cmpb    $$'[',%cl
    jne      .L${opcode}_notimpl                  # no, not handled yet
1:
    movl    %ecx,offGlue_retval+4(%eax)           # save type
    movl    rINST,OUT_ARG1(%esp)                  # arg1<- BBBB (length)
    call    dvmAllocArrayByClass     # eax<- call(arrayClass, length, flags)
    movl    rGLUE,%ecx
    testl   %eax,%eax                             # alloc successful?
    je      common_exceptionThrown                # no, handle exception
    movl    %eax,offGlue_retval(%ecx)             # retval.l<- new array
    movzwl  8(rPC),%ecx                           # ecx<- CCCC
    leal    offArrayObject_contents(%eax),%eax    # eax<- newArray->contents

/* at this point:
 *     eax is pointer to tgt
 *     rINST is length
 *     ecx is CCCC
 *  We now need to copy values from registers into the array
 */

    # set up src pointer
    SPILL_TMP2(%esi)
    SPILL_TMP3(%edi)
    leal    (rFP,%ecx,4),%esi # set up src ptr
    movl    %eax,%edi         # set up dst ptr
    movl    rINST,%ecx        # load count register
    rep
    movsd
    UNSPILL_TMP2(%esi)
    UNSPILL_TMP3(%edi)
    movl    rGLUE,%ecx
    movl    offGlue_retval+4(%ecx),%eax      # eax<- type
    FETCH_INST_OPCODE 5 %edx

    cmpb    $$'I',%al                        # Int array?
    je      5f                               # skip card mark if so
    movl    offGlue_retval(%ecx),%eax        # eax<- object head
    movl    offGlue_cardTable(%ecx),%ecx     # card table base
    shrl    $$GC_CARD_SHIFT,%eax             # convert to card num
    movb    %cl,(%ecx,%eax)                  # mark card based on object head
5:
    ADVANCE_PC 5
    GOTO_NEXT_R %edx


    /*
     * Throw an exception indicating that we have not implemented this
     * mode of filled-new-array.
     */
.L${opcode}_notimpl:
    movl    $$.LstrInternalErrorA,%eax
    movl    %eax,OUT_ARG0(%esp)
    movl    $$.LstrFilledNewArrayNotImplA,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

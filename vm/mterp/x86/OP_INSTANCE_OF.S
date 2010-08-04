%verify "executed"
%verify "null object"
%verify "class cast exception thrown, with correct class name"
%verify "class cast exception not thrown on same class"
%verify "class cast exception not thrown on subclass"
%verify "class not resolved"
%verify "class already resolved"
    /*
     * Check to see if an object reference is an instance of a class.
     *
     * Most common situation is a non-null object, being compared against
     * an already-resolved class.
     */
    /* instance-of vA, vB, class@CCCC */
    movzbl  rINST_HI,%eax               # eax<- BA
    sarl    $$4,%eax                    # eax<- B
    GET_VREG(%eax,%eax)                 # eax<- vB (obj)
    GET_GLUE(%ecx)
    testl   %eax,%eax                   # object null?
    movl    offGlue_methodClassDex(%ecx),%ecx  # ecx<- pDvmDex
    SPILL(rPC)
    je      .L${opcode}_store           # null obj, not instance, store it
    movzwl  2(rPC),rPC                  # rPC<- CCCC
    movl    offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    movl    (%ecx,rPC,4),%ecx           # ecx<- resolved class
    movl    offObject_clazz(%eax),%eax  # eax<- obj->clazz
    testl   %ecx,%ecx                   # have we resolved this before?
    je      .L${opcode}_resolve         # not resolved, do it now
.L${opcode}_resolved:  # eax<- obj->clazz, ecx<- resolved class
    cmpl    %eax,%ecx                   # same class (trivial success)?
    je      .L${opcode}_trivial         # yes, trivial finish
    jmp     .L${opcode}_fullcheck       # no, do full check
%break

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  eax holds obj->clazz
     *  ecx holds class resolved from BBBB
     *  rINST_HI has BA
     *  rPC already spilled
     */
.L${opcode}_fullcheck:
    movl    %eax,OUT_ARG0(%esp)
    movl    %ecx,OUT_ARG1(%esp)
    call    dvmInstanceofNonTrivial     # eax<- boolean result
    # fall through to ${opcode}_store

    /*
     * eax holds boolean result
     * rINST_HI holds BA
     */
.L${opcode}_store:
    UNSPILL(rPC)
    movzbl  rINST_HI,%ecx               # ecx<- BA
    FETCH_INST_WORD(2)
    andb    $$0xf,%cl                   # ecl<- A
    ADVANCE_PC(2)
    SET_VREG(%eax,%ecx)                 # vA<- eax
    GOTO_NEXT

    /*
     * Trivial test succeeded, save and bail.
     *  r9 holds A
     */
.L${opcode}_trivial:
    UNSPILL(rPC)
    movzbl  rINST_HI,%ecx               # ecx<- BA
    FETCH_INST_WORD(2)
    andb    $$0xf,%cl                   # ecl<- A
    ADVANCE_PC(2)
    movl    $$1,%eax
    SET_VREG(%eax,%ecx)                  # vA<- true
    GOTO_NEXT

    /*
     * Resolution required.  This is the least-likely path.
     *
     *  rPC holds BBBB
     *  rINST_HI holds BA
     */
.L${opcode}_resolve:
    movl    rPC,OUT_ARG1(%esp)          # arg1<- BBBB
    GET_GLUE(%ecx)
    UNSPILL(rPC)
    movl    offGlue_method(%ecx),%ecx
    movl    $$1,OUT_ARG2(%esp)          # arg2<- true
    movl    offMethod_clazz(%ecx),%ecx  # ecx<- method->clazz
    EXPORT_PC()
    movl    %ecx,OUT_ARG0(%esp)         # arg0<- method->clazz
    call    dvmResolveClass             # eax<- resolved ClassObject ptr
    UNSPILL(rPC)
    testl   %eax,%eax                   # success?
    je      common_exceptionThrown      # no, handle exception
/* Now, we need to sync up with fast path.  We need eax to
 * hold the obj->clazz, and ecx to hold the resolved class
 */
    movl    %eax,%ecx                   # ecx<- resolved class
    movzbl  rINST_HI,%eax               # eax<- BA
    sarl    $$4,%eax                    # eax<- B
    GET_VREG(%eax,%eax)                 # eax<- vB (obj)
    movl    offObject_clazz(%eax),%eax  # eax<- obj->clazz
    jmp     .L${opcode}_resolved

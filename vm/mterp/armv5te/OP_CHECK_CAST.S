%verify "executed"
%verify "null object"
%verify "class cast exception thrown, with correct class name"
%verify "class cast exception not thrown on same class"
%verify "class cast exception not thrown on subclass"
%verify "class not resolved"
%verify "class already resolved"
    /*
     * Check to see if a cast from one class to another is allowed.
     */
    /* check-cast vAA, class@BBBB */
    mov     r3, rINST, lsr #8           @ r3<- AA
    FETCH(r2, 1)                        @ r2<- BBBB
    GET_VREG(r9, r3)                    @ r9<- object
    ldr     r0, [rGLUE, #offGlue_methodClassDex]    @ r0<- pDvmDex
    cmp     r9, #0                      @ is object null?
    ldr     r0, [r0, #offDvmDex_pResClasses]    @ r0<- pDvmDex->pResClasses
    beq     .L${opcode}_okay            @ null obj, cast always succeeds
    ldr     r1, [r0, r2, lsl #2]        @ r1<- resolved class
    ldr     r0, [r9, #offObject_clazz]  @ r0<- obj->clazz
    cmp     r1, #0                      @ have we resolved this before?
    beq     .L${opcode}_resolve         @ not resolved, do it now
.L${opcode}_resolved:
    cmp     r0, r1                      @ same class (trivial success)?
    bne     .L${opcode}_fullcheck       @ no, do full check
.L${opcode}_okay:
    FETCH_ADVANCE_INST(2)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
%break

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  r0 holds obj->clazz
     *  r1 holds class resolved from BBBB
     *  r9 holds object
     */
.L${opcode}_fullcheck:
    bl      dvmInstanceofNonTrivial     @ r0<- boolean result
    cmp     r0, #0                      @ failed?
    bne     .L${opcode}_okay            @ no, success

    @ A cast has failed.  We need to throw a ClassCastException with the
    @ class of the object that failed to be cast.
    EXPORT_PC()                         @ about to throw
    ldr     r3, [r9, #offObject_clazz]  @ r3<- obj->clazz
    ldr     r0, .LstrClassCastExceptionPtr
    ldr     r1, [r3, #offClassObject_descriptor] @ r1<- obj->clazz->descriptor
    bl      dvmThrowExceptionWithClassMessage
    b       common_exceptionThrown

    /*
     * Resolution required.  This is the least-likely path.
     *
     *  r2 holds BBBB
     *  r9 holds object
     */
.L${opcode}_resolve:
    EXPORT_PC()                         @ resolve() could throw
    ldr     r3, [rGLUE, #offGlue_method] @ r3<- glue->method
    mov     r1, r2                      @ r1<- BBBB
    mov     r2, #0                      @ r2<- false
    ldr     r0, [r3, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveClass             @ r0<- resolved ClassObject ptr
    cmp     r0, #0                      @ got null?
    beq     common_exceptionThrown      @ yes, handle exception
    mov     r1, r0                      @ r1<- class resolved from BBB
    ldr     r0, [r9, #offObject_clazz]  @ r0<- obj->clazz
    b       .L${opcode}_resolved        @ pick up where we left off

.LstrClassCastExceptionPtr:
    .word   .LstrClassCastException

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
     *
     * TODO: convert most of this into a common subroutine, shared with
     *       OP_INSTANCE_OF.S.
     */
    /* instance-of/jumbo vBBBB, vCCCC, class@AAAAAAAA */
    FETCH(r3, 4)                        @ r3<- vCCCC
    FETCH(r9, 3)                        @ r9<- vBBBB
    GET_VREG(r0, r3)                    @ r0<- vCCCC (object)
    ldr     r2, [rSELF, #offThread_methodClassDex]    @ r2<- pDvmDex
    cmp     r0, #0                      @ is object null?
    beq     .L${opcode}_store           @ null obj, not an instance, store r0
    FETCH(r1, 1)                        @ r1<- aaaa (lo)
    FETCH(r3, 2)                        @ r3<- AAAA (hi)
    ldr     r2, [r2, #offDvmDex_pResClasses]    @ r2<- pDvmDex->pResClasses
    orr     r3, r1, r3, lsl #16         @ r3<- AAAAaaaa
    ldr     r1, [r2, r3, lsl #2]        @ r1<- resolved class
    ldr     r0, [r0, #offObject_clazz]  @ r0<- obj->clazz
    cmp     r1, #0                      @ have we resolved this before?
    beq     .L${opcode}_resolve         @ not resolved, do it now
    b       .L${opcode}_resolved        @ resolved, continue
%break

    /*
     * Class resolved, determine type of check necessary.  This is common.
     *  r0 holds obj->clazz
     *  r1 holds class resolved from AAAAAAAA
     *  r9 holds BBBB
     */
.L${opcode}_resolved:
    cmp     r0, r1                      @ same class (trivial success)?
    beq     .L${opcode}_trivial         @ yes, trivial finish
    @ fall through to ${opcode}_fullcheck

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  r0 holds obj->clazz
     *  r1 holds class resolved from AAAAAAAA
     *  r9 holds BBBB
     */
.L${opcode}_fullcheck:
    bl      dvmInstanceofNonTrivial     @ r0<- boolean result
    @ fall through to ${opcode}_store

    /*
     * r0 holds boolean result
     * r9 holds BBBB
     */
.L${opcode}_store:
    FETCH_ADVANCE_INST(5)               @ advance rPC, load rINST
    SET_VREG(r0, r9)                    @ vBBBB<- r0
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction

    /*
     * Trivial test succeeded, save and bail.
     *  r9 holds BBBB
     */
.L${opcode}_trivial:
    mov     r0, #1                      @ indicate success
    @ could b ${opcode}_store, but copying is faster and cheaper
    FETCH_ADVANCE_INST(5)               @ advance rPC, load rINST
    SET_VREG(r0, r9)                    @ vBBBB<- r0
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction

    /*
     * Resolution required.  This is the least-likely path.
     *
     *  r3 holds AAAAAAAA
     *  r9 holds BBBB
     */

.L${opcode}_resolve:
    EXPORT_PC()                         @ resolve() could throw
    ldr     r0, [rSELF, #offThread_method]    @ r0<- self->method
    mov     r1, r3                      @ r1<- AAAAAAAA
    mov     r2, #1                      @ r2<- true
    ldr     r0, [r0, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveClass             @ r0<- resolved ClassObject ptr
    cmp     r0, #0                      @ got null?
    beq     common_exceptionThrown      @ yes, handle exception
    FETCH(r3, 4)                        @ r3<- vCCCC
    mov     r1, r0                      @ r1<- class resolved from AAAAAAAA
    GET_VREG(r0, r3)                    @ r0<- vCCCC (object)
    ldr     r0, [r0, #offObject_clazz]  @ r0<- obj->clazz
    b       .L${opcode}_resolved        @ pick up where we left off

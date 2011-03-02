%verify "executed"
%verify "unimplemented array type"
    /*
     * Create a new array with elements filled from registers.
     *
     * TODO: convert most of this into a common subroutine, shared with
     *       OP_FILLED_NEW_ARRAY.S.
     */
    /* filled-new-array/jumbo {vCCCC..v(CCCC+BBBB-1)}, type@AAAAAAAA */
    ldr     r3, [rSELF, #offThread_methodClassDex]    @ r3<- pDvmDex
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r3, [r3, #offDvmDex_pResClasses]    @ r3<- pDvmDex->pResClasses
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    ldr     r0, [r3, r1, lsl #2]        @ r0<- resolved class
    EXPORT_PC()                         @ need for resolve and alloc
    cmp     r0, #0                      @ already resolved?
    bne     .L${opcode}_continue        @ yes, continue on
8:  ldr     r3, [rSELF, #offThread_method] @ r3<- self->method
    mov     r2, #0                      @ r2<- false
    ldr     r0, [r3, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveClass             @ r0<- call(clazz, ref)
    cmp     r0, #0                      @ got null?
    beq     common_exceptionThrown      @ yes, handle exception
    b       .L${opcode}_continue
%break

    /*
     * On entry:
     *  r0 holds array class
     */
.L${opcode}_continue:
    ldr     r3, [r0, #offClassObject_descriptor] @ r3<- arrayClass->descriptor
    mov     r2, #ALLOC_DONT_TRACK       @ r2<- alloc flags
    ldrb    rINST, [r3, #1]             @ rINST<- descriptor[1]
    FETCH(r1, 3)                        @ r1<- BBBB (length)
    cmp     rINST, #'I'                 @ array of ints?
    cmpne   rINST, #'L'                 @ array of objects?
    cmpne   rINST, #'['                 @ array of arrays?
    mov     r9, r1                      @ save length in r9
    bne     .L${opcode}_notimpl         @ no, not handled yet
    bl      dvmAllocArrayByClass        @ r0<- call(arClass, length, flags)
    cmp     r0, #0                      @ null return?
    beq     common_exceptionThrown      @ alloc failed, handle exception

    FETCH(r1, 4)                        @ r1<- CCCC
    str     r0, [rSELF, #offThread_retval]      @ retval.l <- new array
    str     rINST, [rSELF, #offThread_retval+4] @ retval.h <- type
    add     r0, r0, #offArrayObject_contents @ r0<- newArray->contents
    subs    r9, r9, #1                  @ length--, check for neg
    FETCH_ADVANCE_INST(5)               @ advance to next instr, load rINST
    bmi     2f                          @ was zero, bail

    @ copy values from registers into the array
    @ r0=array, r1=CCCC, r9=BBBB (length)
    add     r2, rFP, r1, lsl #2         @ r2<- &fp[CCCC]
1:  ldr     r3, [r2], #4                @ r3<- *r2++
    subs    r9, r9, #1                  @ count--
    str     r3, [r0], #4                @ *contents++ = vX
    bpl     1b

2:  ldr     r0, [rSELF, #offThread_retval]     @ r0<- object
    ldr     r1, [rSELF, #offThread_retval+4]   @ r1<- type
    ldr     r2, [rSELF, #offThread_cardTable]  @ r2<- card table base
    GET_INST_OPCODE(ip)                      @ ip<- opcode from rINST
    cmp     r1, #'I'                         @ Is int array?
    strneb  r2, [r2, r0, lsr #GC_CARD_SHIFT] @ Mark card based on object head
    GOTO_OPCODE(ip)                          @ execute it

    /*
     * Throw an exception indicating that we have not implemented this
     * mode of filled-new-array.
     */
.L${opcode}_notimpl:
    ldr     r0, .L_strFilledNewArrayNotImpl_${opcode}
    bl      dvmThrowInternalError
    b       common_exceptionThrown

    /*
     * Ideally we'd only define this once, but depending on layout we can
     * exceed the range of the load above.
     */

.L_strFilledNewArrayNotImpl_${opcode}:
    .word   .LstrFilledNewArrayNotImpl

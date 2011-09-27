%default { "postbarrier":"@ no-op ", "prebarrier":"@ no-op ", "sqnum":"0" }
%verify "executed"
%verify "null object"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 32-bit instance field put.
     */
    /* iput-object/jumbo vBBBB, vCCCC, field@AAAAAAAA */
    FETCH(r1, 1)                        @ r1<- aaaa (lo)
    FETCH(r2, 2)                        @ r2<- AAAA (hi)
    FETCH(r0, 4)                        @ r0<- CCCC
    ldr     r3, [rSELF, #offThread_methodClassDex]    @ r3<- DvmDex
    orr     r1, r1, r2, lsl #16         @ r1<- AAAAaaaa
    ldr     r2, [r3, #offDvmDex_pResFields] @ r2<- pDvmDex->pResFields
    GET_VREG(r9, r0)                    @ r9<- fp[CCCC], the object pointer
    ldr     r0, [r2, r1, lsl #2]        @ r0<- resolved InstField ptr
    cmp     r0, #0                      @ is resolved entry null?
    bne     .L${opcode}_finish          @ no, already resolved
8:  ldr     r2, [rSELF, #offThread_method]    @ r2<- current method
    EXPORT_PC()                         @ resolve() could throw
    ldr     r0, [r2, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveInstField         @ r0<- resolved InstField ptr
    b       .L${opcode}_resolved        @ resolved, continue
%break

    /*
     * Currently:
     *  r0 holds resolved field
     *  r9 holds object
     */
.L${opcode}_resolved:
     cmp     r0, #0                     @ resolution unsuccessful?
     beq     common_exceptionThrown     @ yes, throw exception
     @ fall through to ${opcode}_finish

    /*
     * Currently:
     *  r0 holds resolved field
     *  r9 holds object
     */
.L${opcode}_finish:
    @bl      common_squeak${sqnum}
    ldr     r3, [r0, #offInstField_byteOffset]  @ r3<- byte offset of field
    FETCH(r1, 3)                        @ r1<- BBBB
    cmp     r9, #0                      @ check object for null
    GET_VREG(r0, r1)                    @ r0<- fp[BBBB]
    ldr     r2, [rSELF, #offThread_cardTable]  @ r2<- card table base
    beq     common_errNullObject        @ object was null
    FETCH_ADVANCE_INST(5)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    $prebarrier                        @ releasing store
    str     r0, [r9, r3]                @ obj.field (32 bits)<- r0
    $postbarrier
    cmp     r0, #0                      @ stored a null reference?
    strneb  r2, [r2, r9, lsr #GC_CARD_SHIFT]  @ mark card if not
    GOTO_OPCODE(ip)                     @ jump to next instruction

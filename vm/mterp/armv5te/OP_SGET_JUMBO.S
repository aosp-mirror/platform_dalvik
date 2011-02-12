%default { "barrier":"@ no-op " }
%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    ldr     r2, [rSELF, #offThread_methodClassDex]    @ r2<- DvmDex
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r2, [r2, #offDvmDex_pResFields] @ r2<- dvmDex->pResFields
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    ldr     r0, [r2, r1, lsl #2]        @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ is resolved entry null?
    beq     .L${opcode}_resolve         @ yes, do resolve
.L${opcode}_finish: @ field ptr in r0
    ldr     r1, [r0, #offStaticField_value] @ r1<- field value
    $barrier                            @ acquiring load
    FETCH(r2, 3)                        @ r2<- BBBB
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    SET_VREG(r1, r2)                    @ fp[BBBB]<- r1
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
%break

    /*
     * Continuation if the field has not yet been resolved.
     *  r1: AAAAAAAA field ref
     */
.L${opcode}_resolve:
    ldr     r2, [rSELF, #offThread_method]    @ r2<- current method
    EXPORT_PC()                         @ resolve() could throw, so export now
    ldr     r0, [r2, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveStaticField       @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ success?
    bne     .L${opcode}_finish          @ yes, finish
    b       common_exceptionThrown      @ no, handle exception

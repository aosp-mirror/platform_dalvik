%default {"volatile":"0"}
%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 64-bit SGET handler.
     */
    /* sget-wide/jumbo vBBBB, field@AAAAAAAA */
    ldr     r2, [rSELF, #offThread_methodClassDex]    @ r2<- DvmDex
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r2, [r2, #offDvmDex_pResFields] @ r2<- dvmDex->pResFields
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    ldr     r0, [r2, r1, lsl #2]        @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ is resolved entry null?
    beq     .L${opcode}_resolve         @ yes, do resolve
.L${opcode}_finish:
    FETCH(r9, 3)                        @ r9<- BBBB
    .if $volatile
    add     r0, r0, #offStaticField_value @ r0<- pointer to data
    bl      dvmQuasiAtomicRead64        @ r0/r1<- contents of field
    .else
    ldrd    r0, [r0, #offStaticField_value] @ r0/r1<- field value (aligned)
    .endif
    add     r9, rFP, r9, lsl #2         @ r9<- &fp[BBBB]
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    stmia   r9, {r0-r1}                 @ vBBBB/vBBBB+1<- r0/r1
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
%break

    /*
     * Continuation if the field has not yet been resolved.
     *  r1: AAAAAAAA field ref
     *
     * Returns StaticField pointer in r0.
     */
.L${opcode}_resolve:
    ldr     r2, [rSELF, #offThread_method]    @ r2<- current method
    EXPORT_PC()                         @ resolve() could throw, so export now
    ldr     r0, [r2, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveStaticField       @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ success?
    bne     .L${opcode}_finish          @ yes, finish
    b       common_exceptionThrown      @ no, handle exception

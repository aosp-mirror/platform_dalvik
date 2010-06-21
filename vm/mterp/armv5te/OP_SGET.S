%default { "barrier":"@ no-op " }
%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    ldr     r2, [rGLUE, #offGlue_methodClassDex]    @ r2<- DvmDex
    FETCH(r1, 1)                        @ r1<- field ref BBBB
    ldr     r2, [r2, #offDvmDex_pResFields] @ r2<- dvmDex->pResFields
    ldr     r0, [r2, r1, lsl #2]        @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ is resolved entry null?
    beq     .L${opcode}_resolve         @ yes, do resolve
.L${opcode}_finish: @ field ptr in r0
    ldr     r1, [r0, #offStaticField_value] @ r1<- field value
    $barrier                            @ acquiring load
    mov     r2, rINST, lsr #8           @ r2<- AA
    FETCH_ADVANCE_INST(2)               @ advance rPC, load rINST
    SET_VREG(r1, r2)                    @ fp[AA]<- r1
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
%break

    /*
     * Continuation if the field has not yet been resolved.
     *  r1: BBBB field ref
     */
.L${opcode}_resolve:
    ldr     r2, [rGLUE, #offGlue_method]    @ r2<- current method
    EXPORT_PC()                         @ resolve() could throw, so export now
    ldr     r0, [r2, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveStaticField       @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ success?
    bne     .L${opcode}_finish          @ yes, finish
    b       common_exceptionThrown      @ no, handle exception

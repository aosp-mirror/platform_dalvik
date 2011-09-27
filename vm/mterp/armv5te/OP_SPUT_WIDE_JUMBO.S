%default {"volatile":"0"}
%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 64-bit SPUT handler.
     */
    /* sput-wide/jumbo vBBBB, field@AAAAAAAA */
    ldr     r0, [rSELF, #offThread_methodClassDex]  @ r0<- DvmDex
    FETCH(r1, 1)                        @ r1<- aaaa (lo)
    FETCH(r2, 2)                        @ r2<- AAAA (hi)
    ldr     r10, [r0, #offDvmDex_pResFields] @ r10<- dvmDex->pResFields
    orr     r1, r1, r2, lsl #16         @ r1<- AAAAaaaa
    FETCH(r9, 3)                        @ r9<- BBBB
    ldr     r2, [r10, r1, lsl #2]       @ r2<- resolved StaticField ptr
    add     r9, rFP, r9, lsl #2         @ r9<- &fp[BBBB]
    cmp     r2, #0                      @ is resolved entry null?
    beq     .L${opcode}_resolve         @ yes, do resolve
.L${opcode}_finish: @ field ptr in r2, BBBB in r9
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    ldmia   r9, {r0-r1}                 @ r0/r1<- vBBBB/vBBBB+1
    GET_INST_OPCODE(r10)                @ extract opcode from rINST
    .if $volatile
    add     r2, r2, #offStaticField_value @ r2<- pointer to data
    bl      dvmQuasiAtomicSwap64Sync    @ stores r0/r1 into addr r2
    .else
    strd    r0, [r2, #offStaticField_value] @ field<- vBBBB/vBBBB+1
    .endif
    GOTO_OPCODE(r10)                    @ jump to next instruction
%break

    /*
     * Continuation if the field has not yet been resolved.
     *  r1:  AAAAAAAA field ref
     *  r9:  &fp[BBBB]
     *  r10: dvmDex->pResFields
     *
     * Returns StaticField pointer in r2.
     */
.L${opcode}_resolve:
    ldr     r2, [rSELF, #offThread_method]    @ r2<- current method
#if defined(WITH_JIT)
    add     r10, r10, r1, lsl #2        @ r10<- &dvmDex->pResFields[field]
#endif
    EXPORT_PC()                         @ resolve() could throw, so export now
    ldr     r0, [r2, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveStaticField       @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ success?
    mov     r2, r0                      @ copy to r2
    beq     common_exceptionThrown      @ no, handle exception
#if defined(WITH_JIT)
    /*
     * If the JIT is actively building a trace we need to make sure
     * that the field is fully resolved before including this instruction.
     */
    bl      common_verifyField
#endif
    b       .L${opcode}_finish          @ resume

%default { "postbarrier":"@ no-op ", "prebarrier":"@ no-op " }
%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 32-bit SPUT handler for objects
     */
    /* sput-object/jumbo vBBBB, field@AAAAAAAA */
    ldr     r2, [rSELF, #offThread_methodClassDex]    @ r2<- DvmDex
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r10, [r2, #offDvmDex_pResFields] @ r10<- dvmDex->pResFields
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    ldr     r0, [r10, r1, lsl #2]       @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ is resolved entry null?
    beq     .L${opcode}_resolve         @ yes, do resolve
.L${opcode}_finish:   @ field ptr in r0
    FETCH(r2, 3)                        @ r2<- BBBB
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    GET_VREG(r1, r2)                    @ r1<- fp[BBBB]
    ldr     r2, [rSELF, #offThread_cardTable]  @ r2<- card table base
    ldr     r9, [r0, #offField_clazz]   @ r9<- field->clazz
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    $prebarrier                        @ releasing store
    b       .L${opcode}_end
%break


.L${opcode}_end:
    str     r1, [r0, #offStaticField_value]  @ field<- vBBBB
    $postbarrier
    cmp     r1, #0                      @ stored a null object?
    strneb  r2, [r2, r9, lsr #GC_CARD_SHIFT]  @ mark card based on obj head
    GOTO_OPCODE(ip)                     @ jump to next instruction

    /* Continuation if the field has not yet been resolved.
     * r1:  AAAAaaaa field ref
     * r10: dvmDex->pResFields
     */
.L${opcode}_resolve:
    ldr     r2, [rSELF, #offThread_method]    @ r9<- current method
#if defined(WITH_JIT)
    add     r10, r10, r1, lsl #2        @ r10<- &dvmDex->pResFields[field]
#endif
    EXPORT_PC()                         @ resolve() could throw, so export now
    ldr     r0, [r2, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveStaticField       @ r0<- resolved StaticField ptr
    cmp     r0, #0                      @ success?
    beq     common_exceptionThrown      @ no, handle exception
#if defined(WITH_JIT)
    /*
     * If the JIT is actively building a trace we need to make sure
     * that the field is fully resolved before including this instruction.
     */
    bl      common_verifyField
#endif
    b       .L${opcode}_finish          @ resume


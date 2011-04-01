%verify "executed"
%verify "Class already resolved"
%verify "Class not yet resolved"
%verify "Class cannot be resolved"
    /* const-class/jumbo vBBBB, Class@AAAAAAAA */
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    ldr     r2, [rSELF, #offThread_methodClassDex]    @ r2<-self>methodClassDex
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r2, [r2, #offDvmDex_pResClasses]   @ r2<- dvmDex->pResClasses
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    FETCH(r9, 3)                        @ r9<- BBBB
    ldr     r0, [r2, r1, lsl #2]        @ r0<- pResClasses[AAAAaaaa]
    cmp     r0, #0                      @ not yet resolved?
    beq     .L${opcode}_resolve
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    SET_VREG(r0, r9)                    @ vBBBB<- r0
    GOTO_OPCODE(ip)                     @ jump to next instruction
%break

    /*
     * Continuation if the Class has not yet been resolved.
     *  r1: AAAAAAAA (Class ref)
     *  r9: target register
     */
.L${opcode}_resolve:
    EXPORT_PC()
    ldr     r0, [rSELF, #offThread_method] @ r0<- self->method
    mov     r2, #1                      @ r2<- true
    ldr     r0, [r0, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveClass             @ r0<- Class reference
    cmp     r0, #0                      @ failed?
    beq     common_exceptionThrown      @ yup, handle the exception
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    SET_VREG(r0, r9)                    @ vBBBB<- r0
    GOTO_OPCODE(ip)                     @ jump to next instruction

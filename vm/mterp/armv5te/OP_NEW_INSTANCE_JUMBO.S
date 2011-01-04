%verify "executed"
%verify "class not resolved"
%verify "class cannot be resolved"
%verify "class not initialized"
%verify "class fails to initialize"
%verify "class already resolved/initialized"
%verify "class is abstract or interface"
%verify "allocation fails"
    /*
     * Create a new instance of a class.
     */
    /* new-instance/jumbo vBBBB, class@AAAAAAAA */
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r3, [rGLUE, #offGlue_methodClassDex]    @ r3<- pDvmDex
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    ldr     r3, [r3, #offDvmDex_pResClasses]    @ r3<- pDvmDex->pResClasses
    ldr     r0, [r3, r1, lsl #2]        @ r0<- resolved class
    EXPORT_PC()                         @ req'd for init, resolve, alloc
    cmp     r0, #0                      @ already resolved?
    beq     .L${opcode}_resolve         @ no, resolve it now
.L${opcode}_resolved:   @ r0=class
    ldrb    r1, [r0, #offClassObject_status]    @ r1<- ClassStatus enum
    cmp     r1, #CLASS_INITIALIZED      @ has class been initialized?
    bne     .L${opcode}_needinit        @ no, init class now
.L${opcode}_initialized: @ r0=class
    mov     r1, #ALLOC_DONT_TRACK       @ flags for alloc call
    bl      dvmAllocObject              @ r0<- new object
    b       .L${opcode}_finish          @ continue
%break

    .balign 32                          @ minimize cache lines
.L${opcode}_finish: @ r0=new object
    FETCH(r3, 3)                        @ r3<- BBBB
    cmp     r0, #0                      @ failed?
    beq     common_exceptionThrown      @ yes, handle the exception
    FETCH_ADVANCE_INST(4)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    SET_VREG(r0, r3)                    @ vBBBB<- r0
    GOTO_OPCODE(ip)                     @ jump to next instruction

    /*
     * Class initialization required.
     *
     *  r0 holds class object
     */
.L${opcode}_needinit:
    mov     r9, r0                      @ save r0
    bl      dvmInitClass                @ initialize class
    cmp     r0, #0                      @ check boolean result
    mov     r0, r9                      @ restore r0
    bne     .L${opcode}_initialized     @ success, continue
    b       common_exceptionThrown      @ failed, deal with init exception

    /*
     * Resolution required.  This is the least-likely path.
     *
     *  r1 holds AAAAAAAA
     */
.L${opcode}_resolve:
    ldr     r3, [rGLUE, #offGlue_method] @ r3<- glue->method
    mov     r2, #0                      @ r2<- false
    ldr     r0, [r3, #offMethod_clazz]  @ r0<- method->clazz
    bl      dvmResolveClass             @ r0<- resolved ClassObject ptr
    cmp     r0, #0                      @ got null?
    bne     .L${opcode}_resolved        @ no, continue
    b       common_exceptionThrown      @ yes, handle exception

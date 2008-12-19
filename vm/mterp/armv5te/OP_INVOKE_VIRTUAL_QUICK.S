%default { "isrange":"0", "routine":"NoRange" }
%verify "executed"
%verify "null object"
    /*
     * Handle an optimized virtual method call.
     *
     * for: [opt] invoke-virtual-quick, invoke-virtual-quick/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    FETCH(r3, 2)                        @ r3<- FEDC or CCCC
    FETCH(r1, 1)                        @ r1<- BBBB
    .if     (!$isrange)
    and     r3, r3, #15                 @ r3<- C (or stays CCCC)
    .endif
    GET_VREG(r2, r3)                    @ r2<- vC ("this" ptr)
    cmp     r2, #0                      @ is "this" null?
    beq     common_errNullObject        @ null "this", throw exception
    ldr     r2, [r2, #offObject_clazz]  @ r2<- thisPtr->clazz
    ldr     r2, [r2, #offClassObject_vtable]    @ r2<- thisPtr->clazz->vtable
    EXPORT_PC()                         @ invoke must export
    ldr     r0, [r2, r1, lsl #2]        @ r3<- vtable[BBBB]
    bl      common_invokeMethod${routine} @ continue on

%verify "executed"
%verify "unknown method"
%verify "null object"
    /*
     * Handle an interface method call.
     */
    /* invoke-interface/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    FETCH(r2, 4)                        @ r2<- CCCC
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    EXPORT_PC()                         @ must export for invoke
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    GET_VREG(r9, r2)                    @ r9<- first arg ("this")
    ldr     r3, [rSELF, #offThread_methodClassDex]    @ r3<- methodClassDex
    cmp     r9, #0                      @ null obj?
    ldr     r2, [rSELF, #offThread_method]  @ r2<- method
    beq     common_errNullObject        @ yes, fail
    ldr     r0, [r9, #offObject_clazz]  @ r0<- thisPtr->clazz
    bl      dvmFindInterfaceMethodInCache @ r0<- call(class, ref, method, dex)
    cmp     r0, #0                      @ failed?
    beq     common_exceptionThrown      @ yes, handle exception
    b       common_invokeMethodJumbo    @ (r0=method, r9="this")

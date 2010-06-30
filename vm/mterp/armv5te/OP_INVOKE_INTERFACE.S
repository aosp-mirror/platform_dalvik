%default { "isrange":"0", "routine":"NoRange" }
%verify "executed"
%verify "unknown method"
%verify "null object"
    /*
     * Handle an interface method call.
     *
     * for: invoke-interface, invoke-interface/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    FETCH(r2, 2)                        @ r2<- FEDC or CCCC
    FETCH(r1, 1)                        @ r1<- BBBB
    .if     (!$isrange)
    and     r2, r2, #15                 @ r2<- C (or stays CCCC)
    .endif
    EXPORT_PC()                         @ must export for invoke
    GET_VREG(r0, r2)                    @ r0<- first arg ("this")
    ldr     r3, [rGLUE, #offGlue_methodClassDex]    @ r3<- methodClassDex
    cmp     r0, #0                      @ null obj?
    ldr     r2, [rGLUE, #offGlue_method]  @ r2<- method
    beq     common_errNullObject        @ yes, fail
    ldr     r0, [r0, #offObject_clazz]  @ r0<- thisPtr->clazz
    bl      dvmFindInterfaceMethodInCache @ r0<- call(class, ref, method, dex)
    cmp     r0, #0                      @ failed?
    beq     common_exceptionThrown      @ yes, handle exception
    b       common_invokeMethod${routine} @ jump to common handler

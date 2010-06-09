%default { "routine":"NoRange" }
%verify "executed"
%verify "unknown method"
    /*
     * Handle a static method call.
     *
     * for: invoke-static, invoke-static/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    ldr     r3, [rGLUE, #offGlue_methodClassDex]    @ r3<- pDvmDex
    FETCH(r1, 1)                        @ r1<- BBBB
    ldr     r3, [r3, #offDvmDex_pResMethods]    @ r3<- pDvmDex->pResMethods
    ldr     r0, [r3, r1, lsl #2]        @ r0<- resolved methodToCall
    cmp     r0, #0                      @ already resolved?
    EXPORT_PC()                         @ must export for invoke
    bne     common_invokeMethod${routine} @ yes, continue on
0:  ldr     r3, [rGLUE, #offGlue_method] @ r3<- glue->method
    ldr     r0, [r3, #offMethod_clazz]  @ r0<- method->clazz
    mov     r2, #METHOD_STATIC          @ resolver method type
    bl      dvmResolveMethod            @ r0<- call(clazz, ref, flags)
    cmp     r0, #0                      @ got null?
    bne     common_invokeMethod${routine} @ no, continue
    b       common_exceptionThrown      @ yes, handle exception

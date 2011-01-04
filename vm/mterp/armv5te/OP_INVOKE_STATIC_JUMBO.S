%verify "executed"
%verify "unknown method"
    /*
     * Handle a static method call.
     */
    /* invoke-static/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    ldr     r3, [rGLUE, #offGlue_methodClassDex]    @ r3<- pDvmDex
    FETCH(r0, 1)                        @ r1<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    ldr     r3, [r3, #offDvmDex_pResMethods]    @ r3<- pDvmDex->pResMethods
    orr     r1, r0, r1, lsl #16         @ r1<- AAAAaaaa
    ldr     r0, [r3, r1, lsl #2]        @ r0<- resolved methodToCall
    cmp     r0, #0                      @ already resolved?
    EXPORT_PC()                         @ must export for invoke
    bne     common_invokeMethodJumbo    @ yes, continue on
0:  ldr     r3, [rGLUE, #offGlue_method] @ r3<- glue->method
    ldr     r0, [r3, #offMethod_clazz]  @ r0<- method->clazz
    mov     r2, #METHOD_STATIC          @ resolver method type
    bl      dvmResolveMethod            @ r0<- call(clazz, ref, flags)
    cmp     r0, #0                      @ got null?
    bne     common_invokeMethodJumbo    @ no, continue
    b       common_exceptionThrown      @ yes, handle exception

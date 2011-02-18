%verify "executed"
%verify "finalizable class"
    /*
     * Invoke Object.<init> on an object.  In practice we know that
     * Object's nullary constructor doesn't do anything, so we just
     * skip it (we know a debugger isn't active).
     */
    FETCH(r0, 2)                        @ r0<- GFED
    and     r1, r0, #15                 @ r1<- D
    GET_VREG(r0, r1)                    @ r0<- "this" ptr
    cmp     r0, #0                      @ check for NULL
    beq     common_errNullObject        @ export PC and throw NPE
    ldr     r1, [r0, #offObject_clazz]  @ r1<- obj->clazz
    ldr     r2, [r1, #offClassObject_accessFlags] @ r2<- clazz->accessFlags
    tst     r2, #CLASS_ISFINALIZABLE    @ is this class finalizable?
    beq     1f                          @ nope, done
    bl      dvmSetFinalizable           @ call dvmSetFinalizable(obj)
1:  FETCH_ADVANCE_INST(3)               @ advance to next instr, load rINST
    GET_INST_OPCODE(ip)                 @ ip<- opcode from rINST
    GOTO_OPCODE(ip)                     @ execute it

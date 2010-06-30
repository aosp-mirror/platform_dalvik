%verify "executed"
%verify "null object"
    /* iget-wide-quick vA, vB, offset@CCCC */
    mov     r2, rINST, lsr #12          @ r2<- B
    GET_VREG(r3, r2)                    @ r3<- object we're operating on
    FETCH(r1, 1)                        @ r1<- field byte offset
    cmp     r3, #0                      @ check object for null
    mov     r2, rINST, lsr #8           @ r2<- A(+)
    beq     common_errNullObject        @ object was null
    add     r9, r3, r1                  @ r9<- object + offset
    ldmia   r9, {r0-r1}                 @ r0/r1<- obj.field (64 bits, aligned)
    and     r2, r2, #15                 @ r2<- A
    FETCH_ADVANCE_INST(2)               @ advance rPC, load rINST
    add     r3, rFP, r2, lsl #2         @ r3<- &fp[A]
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    stmia   r3, {r0-r1}                 @ fp[A]<- r0/r1
    GOTO_OPCODE(ip)                     @ jump to next instruction

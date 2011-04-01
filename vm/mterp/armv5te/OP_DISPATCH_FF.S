%verify "executed"
    mov     ip, rINST, lsr #8           @ ip<- extended opcode
    add     ip, ip, #256                @ add offset for extended opcodes
    GOTO_OPCODE(ip)                     @ go to proper extended handler


/*
 * Inter-instruction transfer stub.  Call out to dvmCheckInst to handle
 * any interesting requests and then jump to the real instruction
 * handler.  Note that the call to dvmCheckInst is done as a tail call.
 */
    adrl   lr, dvmAsmInstructionStart + (${opnum} * 64)
    mov    r0, rPC              @ arg0
    mov    r1, rSELF            @ arg1
    b      dvmCheckInst         @ (dPC,self) tail call to instruction checker

/*
 * Inter-instruction transfer stub.  Call out to dvmCheckInst to handle
 * any interesting requests and then jump to the real instruction
 * handler.  Unlike the Arm handler, we can't do this as a tail call
 * because rIBASE is caller save and we need to reload it.
 */
    movl   rSELF, %eax
    movl   rPC, OUT_ARG0(%esp)
    movl   %eax, OUT_ARG1(%esp)
    call   dvmCheckInst                            # (dPC, self)
    movl   rSELF, %ecx
    movl   offThread_curHandlerTable(%ecx), rIBASE # reload rIBASE
    jmp    *dvmAsmInstructionStart+(${opnum}*4)

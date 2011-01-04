HANDLE_OPCODE(OP_INVOKE_INTERFACE_JUMBO /*{vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA*/)
    GOTO_invoke(invokeInterface, true, true);
OP_END

HANDLE_OPCODE(OP_INVOKE_VIRTUAL_JUMBO /*{vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA*/)
    GOTO_invoke(invokeVirtual, true, true);
OP_END

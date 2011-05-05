HANDLE_OPCODE(OP_INVOKE_DIRECT_JUMBO /*{vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA*/)
    GOTO_invoke(invokeDirect, true, true);
OP_END

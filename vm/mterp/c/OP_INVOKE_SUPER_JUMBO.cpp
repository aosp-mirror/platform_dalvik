HANDLE_OPCODE(OP_INVOKE_SUPER_JUMBO /*{vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA*/)
    GOTO_invoke(invokeSuper, true, true);
OP_END

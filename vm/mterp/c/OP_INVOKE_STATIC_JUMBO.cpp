HANDLE_OPCODE(OP_INVOKE_STATIC_JUMBO /*{vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA*/)
    GOTO_invoke(invokeStatic, true, true);
OP_END

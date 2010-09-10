HANDLE_OPCODE(OP_RETURN_VOID_BARRIER /**/)
    ILOGV("|return-void");
#ifndef NDEBUG
    retval.j = 0xababababULL;   /* placate valgrind */
#endif
    ANDROID_MEMBAR_FULL();      /* TODO: use a store/store barrier */
    GOTO_returnFromMethod();
OP_END

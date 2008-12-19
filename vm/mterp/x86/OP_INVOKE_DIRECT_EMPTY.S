%verify "executed"
    /*
     * invoke-direct-empty is a no-op in a "standard" interpreter.
     */
    FETCH_INST_WORD(3)
    ADVANCE_PC(3)
    GOTO_NEXT

%verify "executed"
    leal      256(rINST),%ecx
    GOTO_NEXT_JUMBO_R %ecx

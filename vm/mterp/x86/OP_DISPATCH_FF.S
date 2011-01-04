%verify "executed"
    leal      256(rINST),%edx
    GOTO_NEXT_JUMBO_R %edx

%verify "executed"
/*
 * Unlike other alt stubs, we don't want to call dvmCheckBefore() here.
 * Instead, just treat this as a trampoline to reach the real alt
 * handler (which will do the dvmCheckBefore() call.
 */
    leal      256(rINST),%ecx
    GOTO_NEXT_JUMBO_R %ecx

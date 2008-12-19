%verify "executed"
    /*
     * Return the length of an array.
     */
   movzbl   rINST_HI,%eax              # eax<- BA
   sarl     $$12,rINST_FULL            # rINST_FULL<- B
   GET_VREG(%ecx,rINST_FULL)           # ecx<- vB (object ref)
   andb     $$0xf,%al                  # eax<- A
   testl    %ecx,%ecx                  # is null?
   je       common_errNullObject
   FETCH_INST_WORD(1)
   movl     offArrayObject_length(%ecx),%ecx
   ADVANCE_PC(1)
   SET_VREG(%ecx,%eax)
   GOTO_NEXT

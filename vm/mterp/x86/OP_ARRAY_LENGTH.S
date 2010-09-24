%verify "executed"
    /*
     * Return the length of an array.
     */
   mov      rINST,%eax                # eax<- BA
   sarl     $$4,rINST                 # rINST<- B
   GET_VREG_R %ecx rINST              # ecx<- vB (object ref)
   andb     $$0xf,%al                 # eax<- A
   testl    %ecx,%ecx                 # is null?
   je       common_errNullObject
   FETCH_INST_OPCODE 1 %edx
   movl     offArrayObject_length(%ecx),%ecx
   ADVANCE_PC 1
   SET_VREG %ecx %eax
   GOTO_NEXT_R %edx

%verify "executed"
%include "x86/binop1.S" {"instr":"subl %eax,%ecx","result":"%ecx","tmp":"%eax"}

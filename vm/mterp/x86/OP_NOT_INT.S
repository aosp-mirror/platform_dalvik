%verify "executed"
%include "x86/unop.S" {"pre0":"testl %eax,%eax","pre1":"sete %al","instr":"movzbl %al,%eax"}

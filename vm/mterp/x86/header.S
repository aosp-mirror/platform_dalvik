/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * 32-bit x86 definitions and declarations.
 */

/*
386 ABI general notes:

Caller save set:
   eax, edx, ecx, st(0)-st(7)
Callee save set:
   ebx, esi, edi, ebp
Return regs:
   32-bit in eax
   64-bit in edx:eax (low-order 32 in eax)
   fp on top of fp stack st(0)

Parameters passed on stack, pushed right-to-left.  On entry to target, first
parm is at 4(%esp).  Traditional entry code is:

functEntry:
    push    %ebp             # save old frame pointer
    mov     %ebp,%esp        # establish new frame pointer
    sub     FrameSize,%esp   # Allocate storage for spill, locals & outs

Once past the prologue, arguments are referenced at ((argno + 2)*4)(%ebp)

Alignment of stack not strictly required, but should be for performance.  We'll
align frame sizes to 16-byte multiples.

If we're not doing variable stack allocation (alloca), the frame pointer can be
eliminated and all arg references adjusted to be esp relative.

Mterp notes:

Some key interpreter variables will be assigned to registers.  Note that each
will also have an associated spill location (mostly used useful for those assigned
to callee save registers).

  nick     reg   purpose
  rPC      edi   interpreted program counter, used for fetching instructions
  rFP      esi   interpreted frame pointer, used for accessing locals and args
  rINSTw   bx    first 16-bit code of current instruction
  rINSTbl  bl    opcode portion of instruction word
  rINSTbh  bh    high byte of inst word, usually contains src/tgt reg names

Notes:
   o High order 16 bits of ebx must be zero on entry to handler
   o rPC, rFP, rINSTw/rINSTbl valid on handler entry and exit
   o eax, edx and ecx are scratch, rINSTw/ebx sometimes scratch
   o rPC is in the caller save set, and will be killed across external calls. Don't
     forget to SPILL/UNSPILL it around call points

*/

#define rGLUE    (%ebp)
#define rPC      %esi
#define rFP      %edi
#define rINST    %ebx
#define rINSTw   %bx
#define rINSTbh  %bh
#define rINSTbl  %bl


/* Frame diagram while executing dvmMterpStdRun, high to low addresses */
#define IN_ARG0        ( 12)
#define CALLER_RP      (  8)
#define PREV_FP        (  4)
#define rGLUE_SPILL    (  0) /* <- dvmMterpStdRun ebp */
/* Spill offsets relative to %ebp */
#define EDI_SPILL      ( -4)
#define ESI_SPILL      ( -8)
#define EBX_SPILL      (-12) /* <- esp following dmMterpStdRun header */
#define rPC_SPILL      (-16)
#define rFP_SPILL      (-20)
#define rINST_SPILL    (-24)
#define TMP_SPILL1     (-28)
#define TMP_SPILL2     (-32)
#define TMP_SPILL3     (-36)
#define LOCAL0_OFFSET  (-40)
#define LOCAL1_OFFSET  (-44)
#define LOCAL2_OFFSET  (-48)
#define LOCAL3_OFFSET  (-52)
/* Out Arg offsets, relative to %sp */
#define OUT_ARG4       ( 16)
#define OUT_ARG3       ( 12)
#define OUT_ARG2       (  8)
#define OUT_ARG1       (  4)
#define OUT_ARG0       (  0)  /* <- dvmMterpStdRun esp */
#define FRAME_SIZE     80

#define SPILL(reg) movl reg##,reg##_SPILL(%ebp)
#define UNSPILL(reg) movl reg##_SPILL(%ebp),reg
#define SPILL_TMP1(reg) movl reg,TMP_SPILL1(%ebp)
#define UNSPILL_TMP1(reg) movl TMP_SPILL1(%ebp),reg
#define SPILL_TMP2(reg) movl reg,TMP_SPILL2(%ebp)
#define UNSPILL_TMP2(reg) movl TMP_SPILL2(%ebp),reg
#define SPILL_TMP3(reg) movl reg,TMP_SPILL3(%ebp)
#define UNSPILL_TMP3(reg) movl TMP_SPILL3(%ebp),reg

/* save/restore the PC and/or FP from the glue struct */
.macro SAVE_PC_FP_TO_GLUE _reg
    movl     rGLUE,\_reg
    movl     rPC,offGlue_pc(\_reg)
    movl     rFP,offGlue_fp(\_reg)
.endm

.macro LOAD_PC_FP_FROM_GLUE
    movl    rGLUE,rFP
    movl    offGlue_pc(rFP),rPC
    movl    offGlue_fp(rFP),rFP
.endm

/* The interpreter assumes a properly aligned stack on entry, and
 * will preserve 16-byte alignment.
 */

/*
 * "export" the PC to the interpreted stack frame, f/b/o future exception
 * objects.  Must * be done *before* something calls dvmThrowException.
 *
 * In C this is "SAVEAREA_FROM_FP(fp)->xtra.currentPc = pc", i.e.
 * fp - sizeof(StackSaveArea) + offsetof(SaveArea, xtra.currentPc)
 *
 * It's okay to do this more than once.
 */
.macro EXPORT_PC
    movl     rPC, (-sizeofStackSaveArea + offStackSaveArea_currentPc)(rFP)
.endm

/*
 * Given a frame pointer, find the stack save area.
 *
 * In C this is "((StackSaveArea*)(_fp) -1)".
 */
.macro SAVEAREA_FROM_FP _reg
    leal    -sizeofStackSaveArea(rFP), \_reg
.endm

/*
 * Fetch the next instruction from rPC into rINSTw.  Does not advance rPC.
 */
.macro FETCH_INST
    movzwl    (rPC),rINST
.endm

/*
 * Fetch the opcode byte and zero-extend it into _reg.  Must be used
 * in conjunction with GOTO_NEXT_R
 */
.macro FETCH_INST_R _reg
    movzbl    (rPC),\_reg
.endm

/*
 * Fetch the opcode byte at _count words offset from rPC and zero-extend
 * it into _reg.  Must be used in conjunction with GOTO_NEXT_R
 */
.macro FETCH_INST_OPCODE _count _reg
    movzbl  \_count*2(rPC),\_reg
.endm

/*
 * Fetch the nth instruction word from rPC into rINSTw.  Does not advance
 * rPC, and _count is in words
 */
.macro FETCH_INST_WORD _count
    movzwl  \_count*2(rPC),rINST
.endm

/*
 * Fetch instruction word indexed (used for branching).
 * Index is in instruction word units.
 */
.macro FETCH_INST_INDEXED _reg
    movzwl  (rPC,\_reg,2),rINST
.endm

/*
 * Advance rPC by instruction count
 */
.macro ADVANCE_PC _count
    leal  2*\_count(rPC),rPC
.endm

/*
 * Advance rPC by branch offset in register
 */
.macro ADVANCE_PC_INDEXED _reg
    leal (rPC,\_reg,2),rPC
.endm

.macro GOTO_NEXT
     movzx   rINSTbl,%eax
     movzbl  rINSTbh,rINST
     jmp     *dvmAsmInstructionJmpTable(,%eax,4)
.endm

   /*
    * Version of GOTO_NEXT that assumes _reg preloaded with opcode.
    * Should be paired with FETCH_INST_R
    */
.macro GOTO_NEXT_R _reg
     movzbl  1(rPC),rINST
     jmp     *dvmAsmInstructionJmpTable(,\_reg,4)
.endm

/*
 * Get/set the 32-bit value from a Dalvik register.
 */
.macro GET_VREG_R _reg _vreg
    movl     (rFP,\_vreg,4),\_reg
.endm

.macro SET_VREG _reg _vreg
    movl     \_reg,(rFP,\_vreg,4)
.endm

.macro GET_VREG_WORD _reg _vreg _offset
    movl     4*(\_offset)(rFP,\_vreg,4),\_reg
.endm

.macro SET_VREG_WORD _reg _vreg _offset
    movl     \_reg,4*(\_offset)(rFP,\_vreg,4)
.endm

#if 1

#define rFinish %edx

/* Macros for x86-atom handlers */
    /*
    * Get the 32-bit value from a dalvik register.
    */

    .macro      GET_VREG _vreg
    movl        (rFP,\_vreg, 4), \_vreg
    .endm

   /*
    * Fetch the next instruction from the specified offset. Advances rPC
    * to point to the next instruction. "_count" is in 16-bit code units.
    *
    * This must come AFTER anything that can throw an exception, or the
    * exception catch may miss. (This also implies that it must come after
    * EXPORT_PC())
    */

    .macro      FETCH_ADVANCE_INST _count
    add         $$(\_count*2), rPC
    movzwl      (rPC), rINST
    .endm

   /*
    * Fetch the next instruction from an offset specified by _reg. Updates
    * rPC to point to the next instruction. "_reg" must specify the distance
    * in bytes, *not* 16-bit code units, and may be a signed value.
    */

    .macro      FETCH_ADVANCE_INST_RB _reg
    addl        \_reg, rPC
    movzwl      (rPC), rINST
    .endm

   /*
    * Fetch a half-word code unit from an offset past the current PC. The
    * "_count" value is in 16-bit code units. Does not advance rPC.
    * For example, given instruction of format: AA|op BBBB, it
    * fetches BBBB.
    */

    .macro      FETCH _count _reg
    movzwl      (\_count*2)(rPC), \_reg
    .endm

   /*
    * Fetch a half-word code unit from an offset past the current PC. The
    * "_count" value is in 16-bit code units. Does not advance rPC.
    * This variant treats the value as signed.
    */

    .macro      FETCHs _count _reg
    movswl      (\_count*2)(rPC), \_reg
    .endm

   /*
    * Fetch the first byte from an offset past the current PC. The
    * "_count" value is in 16-bit code units. Does not advance rPC.
    * For example, given instruction of format: AA|op CC|BB, it
    * fetches BB.
    */

    .macro      FETCH_BB _count _reg
    movzbl      (\_count*2)(rPC), \_reg
    .endm

    /*
    * Fetch the second byte from an offset past the current PC. The
    * "_count" value is in 16-bit code units. Does not advance rPC.
    * For example, given instruction of format: AA|op CC|BB, it
    * fetches CC.
    */

    .macro      FETCH_CC _count _reg
    movzbl      (\_count*2 + 1)(rPC), \_reg
    .endm

   /*
    * Fetch the second byte from an offset past the current PC. The
    * "_count" value is in 16-bit code units. Does not advance rPC.
    * This variant treats the value as signed.
    */

    .macro      FETCH_CCs _count _reg
    movsbl      (\_count*2 + 1)(rPC), \_reg
    .endm


   /*
    * Fetch one byte from an offset past the current PC.  Pass in the same
    * "_count" as you would for FETCH, and an additional 0/1 indicating which
    * byte of the halfword you want (lo/hi).
    */

    .macro      FETCH_B _reg  _count  _byte
    movzbl      (\_count*2+\_byte)(rPC), \_reg
    .endm

   /*
    * Put the instruction's opcode field into the specified register.
    */

    .macro      GET_INST_OPCODE _reg
    movzbl      rINSTbl, \_reg
    .endm

   /*
    * Begin executing the opcode in _reg.
    */

    .macro      GOTO_OPCODE _reg
    shl         $$${handler_size_bits}, \_reg
    addl        $$dvmAsmInstructionStart,\_reg
    jmp         *\_reg
    .endm



   /*
    * Macros pair attempts to speed up FETCH_INST, GET_INST_OPCODE and GOTO_OPCODE
    * by using a jump table. _rFinish should must be the same register for
    * both macros.
    */

    .macro      FFETCH _rFinish
    movzbl      (rPC), \_rFinish
    .endm

    .macro      FGETOP_JMPa _rFinish
    movzbl      1(rPC), rINST
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

   /*
    * Macro pair attempts to speed up FETCH_INST, GET_INST_OPCODE and GOTO_OPCODE
    * by using a jump table. _rFinish and _count should must be the same register for
    * both macros.
    */

    .macro      FFETCH_ADV _count _rFinish
    movzbl      (\_count*2)(rPC), \_rFinish
    .endm

    .macro      FGETOP_JMP _count _rFinish
    movzbl      (\_count*2 + 1)(rPC), rINST
    addl        $$(\_count*2), rPC
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

    .macro      FGETOP_JMP2 _rFinish
    movzbl      1(rPC), rINST
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

    .macro      OLD_JMP_1 _count _rFinish
    movzbl      (\_count*2)(rPC), \_rFinish
    shl         $$${handler_size_bits}, \_rFinish
    .endm

    .macro      OLD_JMP_2 _rFinish
    addl        $$dvmAsmInstructionStart,\_rFinish
    .endm

    .macro      OLD_JMP_3 _count
    addl        $$(\_count*2), rPC
    .endm

    .macro      OLD_JMP_4 _rFinish
    movzbl      1(rPC), rINST
    jmp         *\_rFinish
    .endm

    .macro      OLD_JMP_A_1 _reg _rFinish
    movzbl      (rPC, \_reg), \_rFinish
    shl         $$${handler_size_bits}, \_rFinish
    .endm

    .macro      OLD_JMP_A_2 _rFinish
    addl        $$dvmAsmInstructionStart,\_rFinish
    .endm

    .macro      OLD_JMP_A_3 _reg _rFinish
    addl        \_reg, rPC
    movzbl      1(rPC, \_reg), rINST
    jmp         *\_rFinish
    .endm

   /*
    * Macro pair attempts to speed up FETCH_INST, GET_INST_OPCODE and GOTO_OPCODE
    * by using a jump table. _rFinish and _reg should must be the same register for
    * both macros.
    */

    .macro      FFETCH_ADV_RB _reg _rFinish
    movzbl      (\_reg, rPC), \_rFinish
    .endm

    .macro      FGETOP_RB_JMP _reg _rFinish
    movzbl      1(\_reg, rPC), rINST
    addl        \_reg, rPC
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

   /*
    * Attempts to speed up FETCH_INST, GET_INST_OPCODE using
    * a jump table. This macro should be called before FINISH_JMP where
    * rFinish should be the same register containing the opcode value.
    * This is an attempt to split up FINISH in order to reduce or remove
    * potential stalls due to the wait for rFINISH.
    */

    .macro      FINISH_FETCH _rFinish
    movzbl      (rPC), \_rFinish
    movzbl      1(rPC), rINST
    .endm


   /*
    * Attempts to speed up FETCH_ADVANCE_INST, GET_INST_OPCODE using
    * a jump table. This macro should be called before FINISH_JMP where
    * rFinish should be the same register containing the opcode value.
    * This is an attempt to split up FINISH in order to reduce or remove
    * potential stalls due to the wait for rFINISH.
    */

    .macro      FINISH_FETCH_ADVANCE _count _rFinish
    movzbl      (\_count*2)(rPC), \_rFinish
    movzbl      (\_count*2 + 1)(rPC), rINST
    addl        $$(\_count*2), rPC
    .endm

   /*
    * Attempts to speed up FETCH_ADVANCE_INST_RB, GET_INST_OPCODE using
    * a jump table. This macro should be called before FINISH_JMP where
    * rFinish should be the same register containing the opcode value.
    * This is an attempt to split up FINISH in order to reduce or remove
    * potential stalls due to the wait for rFINISH.
    */

    .macro      FINISH_FETCH_ADVANCE_RB _reg _rFinish
    movzbl      (\_reg, rPC), \_rFinish
    movzbl      1(\_reg, rPC), rINST
    addl        \_reg, rPC
    .endm

   /*
    * Attempts to speed up GOTO_OPCODE using a jump table. This macro should
    * be called after a FINISH_FETCH* instruction where rFinish should be the
    * same register containing the opcode value. This is an attempt to split up
    * FINISH in order to reduce or remove potential stalls due to the wait for rFINISH.
    */

    .macro      FINISH_JMP _rFinish
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

   /*
    * Attempts to speed up FETCH_INST, GET_INST_OPCODE, GOTO_OPCODE by using
    * a jump table. Uses a single macro - but it should be faster if we
    * split up the fetch for rFinish and the jump using rFinish.
    */

    .macro      FINISH_A
    movzbl      (rPC), rFinish
    movzbl      1(rPC), rINST
    jmp         *dvmAsmInstructionJmpTable(,rFinish, 4)
    .endm

   /*
    * Attempts to speed up FETCH_ADVANCE_INST, GET_INST_OPCODE,
    * GOTO_OPCODE by using a jump table. Uses a single macro -
    * but it should be faster if we split up the fetch for rFinish
    * and the jump using rFinish.
    */

    .macro      FINISH _count
    movzbl      (\_count*2)(rPC), rFinish
    movzbl      (\_count*2 + 1)(rPC), rINST
    addl        $$(\_count*2), rPC
    jmp         *dvmAsmInstructionJmpTable(,rFinish, 4)
    .endm

   /*
    * Attempts to speed up FETCH_ADVANCE_INST_RB, GET_INST_OPCODE,
    * GOTO_OPCODE by using a jump table. Uses a single macro -
    * but it should be faster if we split up the fetch for rFinish
    * and the jump using rFinish.
    */

    .macro      FINISH_RB _reg _rFinish
    movzbl      (\_reg, rPC), \_rFinish
    movzbl      1(\_reg, rPC), rINST
    addl        \_reg, rPC
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

#define sReg0 LOCAL0_OFFSET(%ebp)
#define sReg1 LOCAL1_OFFSET(%ebp)
#define sReg2 LOCAL2_OFFSET(%ebp)
#define sReg3 LOCAL3_OFFSET(%ebp)

   /*
    * Hard coded helper values.
    */

.balign 16

.LdoubNeg:
    .quad       0x8000000000000000

.L64bits:
    .quad       0xFFFFFFFFFFFFFFFF

.LshiftMask2:
    .quad       0x0000000000000000
.LshiftMask:
    .quad       0x000000000000003F

.Lvalue64:
    .quad       0x0000000000000040

.LvaluePosInfLong:
    .quad       0x7FFFFFFFFFFFFFFF

.LvalueNegInfLong:
    .quad       0x8000000000000000

.LvalueNanLong:
    .quad       0x0000000000000000

.LintMin:
.long   0x80000000

.LintMax:
.long   0x7FFFFFFF
#endif


/*
 * This is a #include, not a %include, because we want the C pre-processor
 * to expand the macros into assembler assignment statements.
 */
#include "../common/asm-constants.h"

/*
 * This file was generated automatically by gen-mterp.py for 'x86'.
 *
 * --> DO NOT EDIT <--
 */

/* File: x86/header.S */
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
will also have an associated spill location (mostly useful for those assigned
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

*/

#define rSELF    (%ebp)
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
#define rSELF_SPILL    (  0) /* <- dvmMterpStdRun ebp */
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

#if defined(WITH_JIT)
.macro GET_JIT_PROF_TABLE _self _reg
    movl    offThread_pJitProfTable(\_self),\_reg
.endm
.macro GET_JIT_THRESHOLD _self _reg
    movl    offThread_jitThreshold(\_self),\_reg
.endm
#endif

/* save/restore the PC and/or FP from the self struct */
.macro SAVE_PC_FP_TO_SELF _reg
    movl     rSELF,\_reg
    movl     rPC,offThread_pc(\_reg)
    movl     rFP,offThread_fp(\_reg)
.endm

.macro LOAD_PC_FP_FROM_SELF
    movl    rSELF,rFP
    movl    offThread_pc(rFP),rPC
    movl    offThread_fp(rFP),rFP
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
    * Jumbo version of GOTO_NEXT that assumes _reg preloaded with table
    * offset of the jumbo instruction, which is the top half of the extended
    * opcode + 0x100.  Loads rINST with BBBB field, similar to GOTO_NEXT_R
    */
.macro GOTO_NEXT_JUMBO_R _reg
     movzwl  6(rPC),rINST
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
    add         $(\_count*2), rPC
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
    shl         $6, \_reg
    addl        $dvmAsmInstructionStart,\_reg
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
    addl        $(\_count*2), rPC
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

    .macro      FGETOP_JMP2 _rFinish
    movzbl      1(rPC), rINST
    jmp         *dvmAsmInstructionJmpTable(,\_rFinish, 4)
    .endm

    .macro      OLD_JMP_1 _count _rFinish
    movzbl      (\_count*2)(rPC), \_rFinish
    shl         $6, \_rFinish
    .endm

    .macro      OLD_JMP_2 _rFinish
    addl        $dvmAsmInstructionStart,\_rFinish
    .endm

    .macro      OLD_JMP_3 _count
    addl        $(\_count*2), rPC
    .endm

    .macro      OLD_JMP_4 _rFinish
    movzbl      1(rPC), rINST
    jmp         *\_rFinish
    .endm

    .macro      OLD_JMP_A_1 _reg _rFinish
    movzbl      (rPC, \_reg), \_rFinish
    shl         $6, \_rFinish
    .endm

    .macro      OLD_JMP_A_2 _rFinish
    addl        $dvmAsmInstructionStart,\_rFinish
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
    addl        $(\_count*2), rPC
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
    addl        $(\_count*2), rPC
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

#if defined(WITH_JIT)
#include "../common/jit-config.h"
#endif


    .global dvmAsmInstructionStart
    .type   dvmAsmInstructionStart, %function
dvmAsmInstructionStart = .L_OP_NOP
    .text

/* ------------------------------ */
    .balign 64
.L_OP_NOP: /* 0x00 */
/* File: x86/OP_NOP.S */
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE: /* 0x01 */
/* File: x86/OP_MOVE.S */
    /* for move, move-object, long-to-int */
    /* op vA, vB */
    movzbl rINSTbl,%eax          # eax<- BA
    andb   $0xf,%al             # eax<- A
    shrl   $4,rINST            # rINST<- B
    GET_VREG_R %ecx rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG %ecx %eax           # fp[A]<-fp[B]
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_FROM16: /* 0x02 */
/* File: x86/OP_MOVE_FROM16.S */
    /* for: move/from16, move-object/from16 */
    /* op vAA, vBBBB */
    movzx    rINSTbl,%eax              # eax <= AA
    movw     2(rPC),rINSTw             # rINSTw <= BBBB
    GET_VREG_R %ecx rINST              # ecx<- fp[BBBB]
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %ecx %eax                # fp[AA]<- ecx]
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_16: /* 0x03 */
/* File: x86/OP_MOVE_16.S */
    /* for: move/16, move-object/16 */
    /* op vAAAA, vBBBB */
    movzwl    4(rPC),%ecx              # ecx<- BBBB
    movzwl    2(rPC),%eax              # eax<- AAAA
    GET_VREG_R  %ecx %ecx
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    SET_VREG  %ecx %eax
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_WIDE: /* 0x04 */
/* File: x86/OP_MOVE_WIDE.S */
    /* move-wide vA, vB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzbl    rINSTbl,%ecx                # ecx <- BA
    sarl      $4,rINST                   # rINST<- B
    GET_VREG_WORD %eax rINST 0            # eax<- v[B+0]
    GET_VREG_WORD rINST rINST 1           # rINST<- v[B+1]
    andb      $0xf,%cl                   # ecx <- A
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD rINST %ecx 1            # v[A+1]<- rINST
    ADVANCE_PC 1
    SET_VREG_WORD %eax %ecx 0             # v[A+0]<- eax
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_WIDE_FROM16: /* 0x05 */
/* File: x86/OP_MOVE_WIDE_FROM16.S */
    /* move-wide/from16 vAA, vBBBB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzwl    2(rPC),%ecx              # ecx<- BBBB
    movzbl    rINSTbl,%eax             # eax<- AAAA
    GET_VREG_WORD rINST %ecx 0         # rINST<- v[BBBB+0]
    GET_VREG_WORD %ecx %ecx 1          # ecx<- v[BBBB+1]
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG_WORD rINST %eax 0         # v[AAAA+0]<- rINST
    SET_VREG_WORD %ecx %eax 1          # v[AAAA+1]<- eax
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_WIDE_16: /* 0x06 */
/* File: x86/OP_MOVE_WIDE_16.S */
    /* move-wide/16 vAAAA, vBBBB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzwl    4(rPC),%ecx            # ecx<- BBBB
    movzwl    2(rPC),%eax            # eax<- AAAA
    GET_VREG_WORD rINST %ecx 0       # rINSTw_WORD<- v[BBBB+0]
    GET_VREG_WORD %ecx %ecx 1        # ecx<- v[BBBB+1]
    FETCH_INST_OPCODE 3 %edx
    SET_VREG_WORD rINST %eax 0       # v[AAAA+0]<- rINST
    ADVANCE_PC 3
    SET_VREG_WORD %ecx %eax 1        # v[AAAA+1]<- ecx
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_OBJECT: /* 0x07 */
/* File: x86/OP_MOVE_OBJECT.S */
/* File: x86/OP_MOVE.S */
    /* for move, move-object, long-to-int */
    /* op vA, vB */
    movzbl rINSTbl,%eax          # eax<- BA
    andb   $0xf,%al             # eax<- A
    shrl   $4,rINST            # rINST<- B
    GET_VREG_R %ecx rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG %ecx %eax           # fp[A]<-fp[B]
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MOVE_OBJECT_FROM16: /* 0x08 */
/* File: x86/OP_MOVE_OBJECT_FROM16.S */
/* File: x86/OP_MOVE_FROM16.S */
    /* for: move/from16, move-object/from16 */
    /* op vAA, vBBBB */
    movzx    rINSTbl,%eax              # eax <= AA
    movw     2(rPC),rINSTw             # rINSTw <= BBBB
    GET_VREG_R %ecx rINST              # ecx<- fp[BBBB]
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %ecx %eax                # fp[AA]<- ecx]
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MOVE_OBJECT_16: /* 0x09 */
/* File: x86/OP_MOVE_OBJECT_16.S */
/* File: x86/OP_MOVE_16.S */
    /* for: move/16, move-object/16 */
    /* op vAAAA, vBBBB */
    movzwl    4(rPC),%ecx              # ecx<- BBBB
    movzwl    2(rPC),%eax              # eax<- AAAA
    GET_VREG_R  %ecx %ecx
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    SET_VREG  %ecx %eax
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MOVE_RESULT: /* 0x0a */
/* File: x86/OP_MOVE_RESULT.S */
    /* for: move-result, move-result-object */
    /* op vAA */
    movl     rSELF,%eax                    # eax<- rSELF
    movzx    rINSTbl,%ecx                  # ecx<- AA
    movl     offThread_retval(%eax),%eax     # eax<- self->retval.l
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG  %eax %ecx                    # fp[AA]<- retval.l
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_RESULT_WIDE: /* 0x0b */
/* File: x86/OP_MOVE_RESULT_WIDE.S */
    /* move-result-wide vAA */
    movl    rSELF,%ecx
    movl    offThread_retval(%ecx),%eax
    movl    4+offThread_retval(%ecx),%ecx
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0     # v[AA+0] <- eax
    SET_VREG_WORD %ecx rINST 1     # v[AA+1] <- ecx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MOVE_RESULT_OBJECT: /* 0x0c */
/* File: x86/OP_MOVE_RESULT_OBJECT.S */
/* File: x86/OP_MOVE_RESULT.S */
    /* for: move-result, move-result-object */
    /* op vAA */
    movl     rSELF,%eax                    # eax<- rSELF
    movzx    rINSTbl,%ecx                  # ecx<- AA
    movl     offThread_retval(%eax),%eax     # eax<- self->retval.l
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG  %eax %ecx                    # fp[AA]<- retval.l
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MOVE_EXCEPTION: /* 0x0d */
/* File: x86/OP_MOVE_EXCEPTION.S */
    /* move-exception vAA */
    movl    rSELF,%ecx
    movl    offThread_exception(%ecx),%eax # eax<- dvmGetException bypass
    SET_VREG %eax rINST                # fp[AA]<- exception object
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    movl    $0,offThread_exception(%ecx) # dvmClearException bypass
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_RETURN_VOID: /* 0x0e */
/* File: x86/OP_RETURN_VOID.S */
    jmp       common_returnFromMethod

/* ------------------------------ */
    .balign 64
.L_OP_RETURN: /* 0x0f */
/* File: x86/OP_RETURN.S */
    /*
     * Return a 32-bit value.  Copies the return value into the "self"
     * structure, then jumps to the return handler.
     *
     * for: return, return-object
     */
    /* op vAA */
    movl    rSELF,%ecx
    GET_VREG_R %eax rINST               # eax<- vAA
    movl    %eax,offThread_retval(%ecx)   # retval.i <- AA
    jmp     common_returnFromMethod

/* ------------------------------ */
    .balign 64
.L_OP_RETURN_WIDE: /* 0x10 */
/* File: x86/OP_RETURN_WIDE.S */
    /*
     * Return a 64-bit value.  Copies the return value into the "self"
     * structure, then jumps to the return handler.
     */
    /* return-wide vAA */
    movl    rSELF,%ecx
    GET_VREG_WORD %eax rINST 0       # eax<- v[AA+0]
    GET_VREG_WORD rINST rINST 1      # rINST<- v[AA+1]
    movl    %eax,offThread_retval(%ecx)
    movl    rINST,4+offThread_retval(%ecx)
    jmp     common_returnFromMethod

/* ------------------------------ */
    .balign 64
.L_OP_RETURN_OBJECT: /* 0x11 */
/* File: x86/OP_RETURN_OBJECT.S */
/* File: x86/OP_RETURN.S */
    /*
     * Return a 32-bit value.  Copies the return value into the "self"
     * structure, then jumps to the return handler.
     *
     * for: return, return-object
     */
    /* op vAA */
    movl    rSELF,%ecx
    GET_VREG_R %eax rINST               # eax<- vAA
    movl    %eax,offThread_retval(%ecx)   # retval.i <- AA
    jmp     common_returnFromMethod


/* ------------------------------ */
    .balign 64
.L_OP_CONST_4: /* 0x12 */
/* File: x86/OP_CONST_4.S */
    /* const/4 vA, #+B */
    movsx   rINSTbl,%eax              # eax<-ssssssBx
    movl    $0xf,%ecx
    andl    %eax,%ecx                 # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    sarl    $4,%eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_16: /* 0x13 */
/* File: x86/OP_CONST_16.S */
    /* const/16 vAA, #+BBBB */
    movswl  2(rPC),%ecx                # ecx<- ssssBBBB
    movl    rINST,%eax                 # eax<- AA
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %ecx %eax                 # vAA<- ssssBBBB
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST: /* 0x14 */
/* File: x86/OP_CONST.S */
    /* const vAA, #+BBBBbbbb */
    movl      2(rPC),%eax             # grab all 32 bits at once
    movl      rINST,%ecx              # ecx<- AA
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    SET_VREG %eax %ecx                # vAA<- eax
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_HIGH16: /* 0x15 */
/* File: x86/OP_CONST_HIGH16.S */
    /* const/high16 vAA, #+BBBB0000 */
    movzwl     2(rPC),%eax                # eax<- 0000BBBB
    movl       rINST,%ecx                 # ecx<- AA
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    sall       $16,%eax                  # eax<- BBBB0000
    SET_VREG %eax %ecx                    # vAA<- eax
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_WIDE_16: /* 0x16 */
/* File: x86/OP_CONST_WIDE_16.S */
    /* const-wide/16 vAA, #+BBBB */
    movswl    2(rPC),%eax               # eax<- ssssBBBB
    cltd                                # rPC:eax<- ssssssssssssBBBB
    SET_VREG_WORD %edx rINST 1          # store msw
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 0          # store lsw
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_WIDE_32: /* 0x17 */
/* File: x86/OP_CONST_WIDE_32.S */
    /* const-wide/32 vAA, #+BBBBbbbb */
    movl     2(rPC),%eax                # eax<- BBBBbbbb
    cltd                                # rPC:eax<- ssssssssssssBBBB
    SET_VREG_WORD %edx rINST,1          # store msw
    FETCH_INST_OPCODE 3 %edx
    SET_VREG_WORD %eax rINST 0          # store lsw
    ADVANCE_PC 3
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_WIDE: /* 0x18 */
/* File: x86/OP_CONST_WIDE.S */
    /* const-wide vAA, #+HHHHhhhhBBBBbbbb */
    movl      2(rPC),%eax         # eax<- lsw
    movzbl    rINSTbl,%ecx        # ecx<- AA
    movl      6(rPC),rINST        # rINST<- msw
    leal      (rFP,%ecx,4),%ecx   # dst addr
    movl      rINST,4(%ecx)
    FETCH_INST_OPCODE 5 %edx
    movl      %eax,(%ecx)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_WIDE_HIGH16: /* 0x19 */
/* File: x86/OP_CONST_WIDE_HIGH16.S */
    /* const-wide/high16 vAA, #+BBBB000000000000 */
    movzwl     2(rPC),%eax                # eax<- 0000BBBB
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    sall       $16,%eax                  # eax<- BBBB0000
    SET_VREG_WORD %eax rINST 1            # v[AA+1]<- eax
    xorl       %eax,%eax
    SET_VREG_WORD %eax rINST 0            # v[AA+0]<- eax
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_STRING: /* 0x1a */
/* File: x86/OP_CONST_STRING.S */

    /* const/string vAA, String@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax              # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx# ecx<- self->methodClassDex
    movl      offDvmDex_pResStrings(%ecx),%ecx # ecx<- dvmDex->pResStrings
    movl      (%ecx,%eax,4),%eax       # eax<- rResString[BBBB]
    movl      rINST,%ecx
    FETCH_INST_OPCODE 2 %edx
    testl     %eax,%eax                # resolved yet?
    je        .LOP_CONST_STRING_resolve
    SET_VREG  %eax %ecx                # vAA<- rResString[BBBB]
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_STRING_JUMBO: /* 0x1b */
/* File: x86/OP_CONST_STRING_JUMBO.S */

    /* const/string vAA, String@BBBBBBBB */
    movl      rSELF,%ecx
    movl      2(rPC),%eax              # eax<- BBBBBBBB
    movl      offThread_methodClassDex(%ecx),%ecx# ecx<- self->methodClassDex
    movl      offDvmDex_pResStrings(%ecx),%ecx # ecx<- dvmDex->pResStrings
    movl      (%ecx,%eax,4),%eax       # eax<- rResString[BBBB]
    movl      rINST,%ecx
    FETCH_INST_OPCODE 3 %edx
    testl     %eax,%eax                # resolved yet?
    je        .LOP_CONST_STRING_JUMBO_resolve
    SET_VREG  %eax %ecx                # vAA<- rResString[BBBB]
    ADVANCE_PC 3
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_CLASS: /* 0x1c */
/* File: x86/OP_CONST_CLASS.S */

    /* const/class vAA, Class@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax              # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx# ecx<- self->methodClassDex
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- dvmDex->pResClasses
    movl      (%ecx,%eax,4),%eax       # eax<- rResClasses[BBBB]
    movl      rINST,%ecx
    FETCH_INST_OPCODE 2 %edx
    testl     %eax,%eax                # resolved yet?
    je        .LOP_CONST_CLASS_resolve
    SET_VREG  %eax %ecx                # vAA<- rResClasses[BBBB]
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_MONITOR_ENTER: /* 0x1d */
/* File: x86/OP_MONITOR_ENTER.S */
    /*
     * Synchronize on an object.
     */
    /* monitor-enter vAA */
    movl    rSELF,%ecx
    GET_VREG_R %eax rINST               # eax<- vAA
    FETCH_INST_WORD 1
    testl   %eax,%eax                   # null object?
    EXPORT_PC                           # need for precise GC
    jne     .LOP_MONITOR_ENTER_continue
    jmp     common_errNullObject

/* ------------------------------ */
    .balign 64
.L_OP_MONITOR_EXIT: /* 0x1e */
/* File: x86/OP_MONITOR_EXIT.S */
    /*
     * Unlock an object.
     *
     * Exceptions that occur when unlocking a monitor need to appear as
     * if they happened at the following instruction.  See the Dalvik
     * instruction spec.
     */
    /* monitor-exit vAA */
    GET_VREG_R %eax rINST
    movl    rSELF,%ecx
    EXPORT_PC
    testl   %eax,%eax                   # null object?
    je      .LOP_MONITOR_EXIT_errNullObject   # go if so
    movl    %eax,OUT_ARG1(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    jmp     .LOP_MONITOR_EXIT_continue

/* ------------------------------ */
    .balign 64
.L_OP_CHECK_CAST: /* 0x1f */
/* File: x86/OP_CHECK_CAST.S */
    /*
     * Check to see if a cast from one class to another is allowed.
     */
    /* check-cast vAA, class@BBBB */
    movl      rSELF,%ecx
    GET_VREG_R  rINST,rINST             # rINST<- vAA (object)
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    testl     rINST,rINST               # is oject null?
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    je        .LOP_CHECK_CAST_okay          # null obj, cast always succeeds
    movl      (%ecx,%eax,4),%eax        # eax<- resolved class
    movl      offObject_clazz(rINST),%ecx # ecx<- obj->clazz
    testl     %eax,%eax                 # have we resolved this before?
    je        .LOP_CHECK_CAST_resolve       # no, go do it now
.LOP_CHECK_CAST_resolved:
    cmpl      %eax,%ecx                 # same class (trivial success)?
    jne       .LOP_CHECK_CAST_fullcheck     # no, do full check
.LOP_CHECK_CAST_okay:
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_INSTANCE_OF: /* 0x20 */
/* File: x86/OP_INSTANCE_OF.S */
    /*
     * Check to see if an object reference is an instance of a class.
     *
     * Most common situation is a non-null object, being compared against
     * an already-resolved class.
     */
    /* instance-of vA, vB, class@CCCC */
    movl    rINST,%eax                # eax<- BA
    sarl    $4,%eax                    # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB (obj)
    movl    rSELF,%ecx
    testl   %eax,%eax                   # object null?
    movl    offThread_methodClassDex(%ecx),%ecx  # ecx<- pDvmDex
    je      .LOP_INSTANCE_OF_store           # null obj, not instance, store it
    movzwl  2(rPC),%edx                 # edx<- CCCC
    movl    offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    movl    (%ecx,%edx,4),%ecx          # ecx<- resolved class
    movl    offObject_clazz(%eax),%eax  # eax<- obj->clazz
    testl   %ecx,%ecx                   # have we resolved this before?
    je      .LOP_INSTANCE_OF_resolve         # not resolved, do it now
.LOP_INSTANCE_OF_resolved:  # eax<- obj->clazz, ecx<- resolved class
    cmpl    %eax,%ecx                   # same class (trivial success)?
    je      .LOP_INSTANCE_OF_trivial         # yes, trivial finish
    jmp     .LOP_INSTANCE_OF_fullcheck       # no, do full check

/* ------------------------------ */
    .balign 64
.L_OP_ARRAY_LENGTH: /* 0x21 */
/* File: x86/OP_ARRAY_LENGTH.S */
    /*
     * Return the length of an array.
     */
   mov      rINST,%eax                # eax<- BA
   sarl     $4,rINST                 # rINST<- B
   GET_VREG_R %ecx rINST              # ecx<- vB (object ref)
   andb     $0xf,%al                 # eax<- A
   testl    %ecx,%ecx                 # is null?
   je       common_errNullObject
   FETCH_INST_OPCODE 1 %edx
   movl     offArrayObject_length(%ecx),%ecx
   ADVANCE_PC 1
   SET_VREG %ecx %eax
   GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_NEW_INSTANCE: /* 0x22 */
/* File: x86/OP_NEW_INSTANCE.S */
    /*
     * Create a new instance of a class.
     */
    /* new-instance vAA, class@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- pDvmDex
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    EXPORT_PC
    movl      (%ecx,%eax,4),%ecx        # ecx<- resolved class
    testl     %ecx,%ecx                 # resolved?
    je        .LOP_NEW_INSTANCE_resolve       # no, go do it
.LOP_NEW_INSTANCE_resolved:  # on entry, ecx<- class
    cmpb      $CLASS_INITIALIZED,offClassObject_status(%ecx)
    je        .LOP_NEW_INSTANCE_initialized
    jmp       .LOP_NEW_INSTANCE_needinit

/* ------------------------------ */
    .balign 64
.L_OP_NEW_ARRAY: /* 0x23 */
/* File: x86/OP_NEW_ARRAY.S */
    /*
     * Allocate an array of objects, specified with the array class
     * and a count.
     *
     * The verifier guarantees that this is an array class, so we don't
     * check for it here.
     */
    /* new-array vA, vB, class@CCCC */
    movl    rSELF,%ecx
    EXPORT_PC
    movl    offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    movzwl  2(rPC),%eax                       # eax<- CCCC
    movl    offDvmDex_pResClasses(%ecx),%ecx  # ecx<- pDvmDex->pResClasses
    movl    (%ecx,%eax,4),%ecx                # ecx<- resolved class
    movzbl  rINSTbl,%eax
    sarl    $4,%eax                          # eax<- B
    GET_VREG_R %eax %eax                      # eax<- vB (array length)
    andb    $0xf,rINSTbl                     # rINST<- A
    testl   %eax,%eax
    js      common_errNegativeArraySize       # bail, passing len in eax
    testl   %ecx,%ecx                         # already resolved?
    jne     .LOP_NEW_ARRAY_finish                # yes, fast path
    jmp     .LOP_NEW_ARRAY_resolve               # resolve now

/* ------------------------------ */
    .balign 64
.L_OP_FILLED_NEW_ARRAY: /* 0x24 */
/* File: x86/OP_FILLED_NEW_ARRAY.S */
    /*
     * Create a new array with elements filled from registers.
     *
     * for: filled-new-array, filled-new-array/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, type@BBBB */
    movl    rSELF,%eax
    movl    offThread_methodClassDex(%eax),%eax # eax<- pDvmDex
    movzwl  2(rPC),%ecx                       # ecx<- BBBB
    movl    offDvmDex_pResClasses(%eax),%eax  # eax<- pDvmDex->pResClasses
    movl    (%eax,%ecx,4),%eax                # eax<- resolved class
    EXPORT_PC
    testl   %eax,%eax                         # already resolved?
    jne     .LOP_FILLED_NEW_ARRAY_continue              # yes, continue
    # less frequent path, so we'll redo some work
    movl    rSELF,%eax
    movl    $0,OUT_ARG2(%esp)                # arg2<- false
    movl    %ecx,OUT_ARG1(%esp)               # arg1<- BBBB
    movl    offThread_method(%eax),%eax         # eax<- self->method
    jmp     .LOP_FILLED_NEW_ARRAY_more

/* ------------------------------ */
    .balign 64
.L_OP_FILLED_NEW_ARRAY_RANGE: /* 0x25 */
/* File: x86/OP_FILLED_NEW_ARRAY_RANGE.S */
/* File: x86/OP_FILLED_NEW_ARRAY.S */
    /*
     * Create a new array with elements filled from registers.
     *
     * for: filled-new-array, filled-new-array/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, type@BBBB */
    movl    rSELF,%eax
    movl    offThread_methodClassDex(%eax),%eax # eax<- pDvmDex
    movzwl  2(rPC),%ecx                       # ecx<- BBBB
    movl    offDvmDex_pResClasses(%eax),%eax  # eax<- pDvmDex->pResClasses
    movl    (%eax,%ecx,4),%eax                # eax<- resolved class
    EXPORT_PC
    testl   %eax,%eax                         # already resolved?
    jne     .LOP_FILLED_NEW_ARRAY_RANGE_continue              # yes, continue
    # less frequent path, so we'll redo some work
    movl    rSELF,%eax
    movl    $0,OUT_ARG2(%esp)                # arg2<- false
    movl    %ecx,OUT_ARG1(%esp)               # arg1<- BBBB
    movl    offThread_method(%eax),%eax         # eax<- self->method
    jmp     .LOP_FILLED_NEW_ARRAY_RANGE_more


/* ------------------------------ */
    .balign 64
.L_OP_FILL_ARRAY_DATA: /* 0x26 */
/* File: x86/OP_FILL_ARRAY_DATA.S */
    /* fill-array-data vAA, +BBBBBBBB */
    movl    2(rPC),%ecx                # ecx<- BBBBbbbb
    leal    (rPC,%ecx,2),%ecx          # ecx<- PC + BBBBbbbb*2
    GET_VREG_R %eax rINST
    EXPORT_PC
    movl    %eax,OUT_ARG0(%esp)
    movl    %ecx,OUT_ARG1(%esp)
    call    dvmInterpHandleFillArrayData
    FETCH_INST_OPCODE 3 %edx
    testl   %eax,%eax                   # exception thrown?
    je      common_exceptionThrown
    ADVANCE_PC 3
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_THROW: /* 0x27 */
/* File: x86/OP_THROW.S */
    /*
     * Throw an exception object in the current thread.
     */
    /* throw vAA */
    EXPORT_PC
    GET_VREG_R %eax rINST              # eax<- exception object
    movl     rSELF,%ecx                # ecx<- self
    testl    %eax,%eax                 # null object?
    je       common_errNullObject
    movl     %eax,offThread_exception(%ecx) # thread->exception<- obj
    jmp      common_exceptionThrown

/* ------------------------------ */
    .balign 64
.L_OP_GOTO: /* 0x28 */
/* File: x86/OP_GOTO.S */
    /*
     * Unconditional branch, 8-bit offset.
     *
     * The branch distance is a signed code-unit offset, which we need to
     * double to get a byte offset.
     */
    /* goto +AA */
    movsbl  rINSTbl,rINST         # ebx<- ssssssAA
    testl   rINST,rINST           # test for <0
    js      common_backwardBranch
    movl    rINST,%eax
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT

/* ------------------------------ */
    .balign 64
.L_OP_GOTO_16: /* 0x29 */
/* File: x86/OP_GOTO_16.S */
    /*
     * Unconditional branch, 16-bit offset.
     *
     * The branch distance is a signed code-unit offset
     */
    /* goto/16 +AAAA */
    movswl  2(rPC),rINST           # rINST<- ssssAAAA
    testl   rINST,rINST            # test for <0
    js      common_backwardBranch
    movl    rINST,%eax
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT

/* ------------------------------ */
    .balign 64
.L_OP_GOTO_32: /* 0x2a */
/* File: x86/OP_GOTO_32.S */
    /*
     * Unconditional branch, 32-bit offset.
     *
     * The branch distance is a signed code-unit offset.
     *
     * Unlike most opcodes, this one is allowed to branch to itself, so
     * our "backward branch" test must be "<=0" instead of "<0".
     */
    /* goto/32 AAAAAAAA */
    movl    2(rPC),rINST           # rINST<- AAAAAAAA
    cmpl    $0,rINST              # test for <= 0
    jle     common_backwardBranch
    movl    rINST,%eax
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT

/* ------------------------------ */
    .balign 64
.L_OP_PACKED_SWITCH: /* 0x2b */
/* File: x86/OP_PACKED_SWITCH.S */
    /*
     * Handle a packed-switch or sparse-switch instruction.  In both cases
     * we decode it and hand it off to a helper function.
     *
     * We don't really expect backward branches in a switch statement, but
     * they're perfectly legal, so we check for them here.
     *
     * for: packed-switch, sparse-switch
     */
    /* op vAA, +BBBB */
    movl    2(rPC),%ecx           # ecx<- BBBBbbbb
    GET_VREG_R %eax rINST         # eax<- vAA
    leal    (rPC,%ecx,2),%ecx     # ecx<- PC + BBBBbbbb*2
    movl    %eax,OUT_ARG1(%esp)   # ARG1<- vAA
    movl    %ecx,OUT_ARG0(%esp)   # ARG0<- switchData
    call    dvmInterpHandlePackedSwitch
    testl   %eax,%eax
    movl    %eax,rINST            # set up word offset
    jle     common_backwardBranch # check on special actions
    ADVANCE_PC_INDEXED rINST
    FETCH_INST
    GOTO_NEXT

/* ------------------------------ */
    .balign 64
.L_OP_SPARSE_SWITCH: /* 0x2c */
/* File: x86/OP_SPARSE_SWITCH.S */
/* File: x86/OP_PACKED_SWITCH.S */
    /*
     * Handle a packed-switch or sparse-switch instruction.  In both cases
     * we decode it and hand it off to a helper function.
     *
     * We don't really expect backward branches in a switch statement, but
     * they're perfectly legal, so we check for them here.
     *
     * for: packed-switch, sparse-switch
     */
    /* op vAA, +BBBB */
    movl    2(rPC),%ecx           # ecx<- BBBBbbbb
    GET_VREG_R %eax rINST         # eax<- vAA
    leal    (rPC,%ecx,2),%ecx     # ecx<- PC + BBBBbbbb*2
    movl    %eax,OUT_ARG1(%esp)   # ARG1<- vAA
    movl    %ecx,OUT_ARG0(%esp)   # ARG0<- switchData
    call    dvmInterpHandleSparseSwitch
    testl   %eax,%eax
    movl    %eax,rINST            # set up word offset
    jle     common_backwardBranch # check on special actions
    ADVANCE_PC_INDEXED rINST
    FETCH_INST
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_CMPL_FLOAT: /* 0x2d */
/* File: x86/OP_CMPL_FLOAT.S */
/* File: x86/OP_CMPG_DOUBLE.S */
    /* float/double_cmp[gl] vAA, vBB, vCC */
    movzbl    3(rPC),%eax             # eax<- CC
    movzbl    2(rPC),%ecx             # ecx<- BB
    .if 0
    fldl     (rFP,%eax,4)
    fldl     (rFP,%ecx,4)
    .else
    flds     (rFP,%eax,4)
    flds     (rFP,%ecx,4)
    .endif
    xorl     %ecx,%ecx
    fucompp     # z if equal, p set if NaN, c set if st0 < st1
    fnstsw   %ax
    sahf
    movl      rINST,%eax
    FETCH_INST_OPCODE 2 %edx
    jp       .LOP_CMPL_FLOAT_isNaN
    je       .LOP_CMPL_FLOAT_finish
    sbbl     %ecx,%ecx
    jb       .LOP_CMPL_FLOAT_finish
    incl     %ecx
.LOP_CMPL_FLOAT_finish:
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_CMPG_FLOAT: /* 0x2e */
/* File: x86/OP_CMPG_FLOAT.S */
/* File: x86/OP_CMPG_DOUBLE.S */
    /* float/double_cmp[gl] vAA, vBB, vCC */
    movzbl    3(rPC),%eax             # eax<- CC
    movzbl    2(rPC),%ecx             # ecx<- BB
    .if 0
    fldl     (rFP,%eax,4)
    fldl     (rFP,%ecx,4)
    .else
    flds     (rFP,%eax,4)
    flds     (rFP,%ecx,4)
    .endif
    xorl     %ecx,%ecx
    fucompp     # z if equal, p set if NaN, c set if st0 < st1
    fnstsw   %ax
    sahf
    movl      rINST,%eax
    FETCH_INST_OPCODE 2 %edx
    jp       .LOP_CMPG_FLOAT_isNaN
    je       .LOP_CMPG_FLOAT_finish
    sbbl     %ecx,%ecx
    jb       .LOP_CMPG_FLOAT_finish
    incl     %ecx
.LOP_CMPG_FLOAT_finish:
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_CMPL_DOUBLE: /* 0x2f */
/* File: x86/OP_CMPL_DOUBLE.S */
/* File: x86/OP_CMPG_DOUBLE.S */
    /* float/double_cmp[gl] vAA, vBB, vCC */
    movzbl    3(rPC),%eax             # eax<- CC
    movzbl    2(rPC),%ecx             # ecx<- BB
    .if 1
    fldl     (rFP,%eax,4)
    fldl     (rFP,%ecx,4)
    .else
    flds     (rFP,%eax,4)
    flds     (rFP,%ecx,4)
    .endif
    xorl     %ecx,%ecx
    fucompp     # z if equal, p set if NaN, c set if st0 < st1
    fnstsw   %ax
    sahf
    movl      rINST,%eax
    FETCH_INST_OPCODE 2 %edx
    jp       .LOP_CMPL_DOUBLE_isNaN
    je       .LOP_CMPL_DOUBLE_finish
    sbbl     %ecx,%ecx
    jb       .LOP_CMPL_DOUBLE_finish
    incl     %ecx
.LOP_CMPL_DOUBLE_finish:
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_CMPG_DOUBLE: /* 0x30 */
/* File: x86/OP_CMPG_DOUBLE.S */
    /* float/double_cmp[gl] vAA, vBB, vCC */
    movzbl    3(rPC),%eax             # eax<- CC
    movzbl    2(rPC),%ecx             # ecx<- BB
    .if 1
    fldl     (rFP,%eax,4)
    fldl     (rFP,%ecx,4)
    .else
    flds     (rFP,%eax,4)
    flds     (rFP,%ecx,4)
    .endif
    xorl     %ecx,%ecx
    fucompp     # z if equal, p set if NaN, c set if st0 < st1
    fnstsw   %ax
    sahf
    movl      rINST,%eax
    FETCH_INST_OPCODE 2 %edx
    jp       .LOP_CMPG_DOUBLE_isNaN
    je       .LOP_CMPG_DOUBLE_finish
    sbbl     %ecx,%ecx
    jb       .LOP_CMPG_DOUBLE_finish
    incl     %ecx
.LOP_CMPG_DOUBLE_finish:
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CMP_LONG: /* 0x31 */
/* File: x86/OP_CMP_LONG.S */
    /*
     * Compare two 64-bit values.  Puts 0, 1, or -1 into the destination
     * register based on the results of the comparison.
     */
    /* cmp-long vAA, vBB, vCC */
    movzbl    2(rPC),%ecx              # ecx<- BB
    movzbl    3(rPC),%edx              # edx<- CC
    GET_VREG_WORD %eax %ecx,1          # eax<- v[BB+1]
    GET_VREG_WORD %ecx %ecx 0          # ecx<- v[BB+0]
    cmpl      4(rFP,%edx,4),%eax
    jl        .LOP_CMP_LONG_smaller
    jg        .LOP_CMP_LONG_bigger
    sub       (rFP,%edx,4),%ecx
    ja        .LOP_CMP_LONG_bigger
    jb        .LOP_CMP_LONG_smaller
    jmp       .LOP_CMP_LONG_finish

/* ------------------------------ */
    .balign 64
.L_OP_IF_EQ: /* 0x32 */
/* File: x86/OP_IF_EQ.S */
/* File: x86/bincmp.S */
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $2,%eax              # assume not taken
    jne   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_NE: /* 0x33 */
/* File: x86/OP_IF_NE.S */
/* File: x86/bincmp.S */
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $2,%eax              # assume not taken
    je   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_LT: /* 0x34 */
/* File: x86/OP_IF_LT.S */
/* File: x86/bincmp.S */
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $2,%eax              # assume not taken
    jge   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_GE: /* 0x35 */
/* File: x86/OP_IF_GE.S */
/* File: x86/bincmp.S */
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $2,%eax              # assume not taken
    jl   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_GT: /* 0x36 */
/* File: x86/OP_IF_GT.S */
/* File: x86/bincmp.S */
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $2,%eax              # assume not taken
    jle   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_LE: /* 0x37 */
/* File: x86/OP_IF_LE.S */
/* File: x86/bincmp.S */
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $2,%eax              # assume not taken
    jg   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_EQZ: /* 0x38 */
/* File: x86/OP_IF_EQZ.S */
/* File: x86/zcmp.S */
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    cmpl     $0,(rFP,rINST,4)     # compare (vA, 0)
    movswl   2(rPC),rINST         # fetch signed displacement
    movl     $2,%eax             # assume branch not taken
    jne   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_NEZ: /* 0x39 */
/* File: x86/OP_IF_NEZ.S */
/* File: x86/zcmp.S */
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    cmpl     $0,(rFP,rINST,4)     # compare (vA, 0)
    movswl   2(rPC),rINST         # fetch signed displacement
    movl     $2,%eax             # assume branch not taken
    je   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_LTZ: /* 0x3a */
/* File: x86/OP_IF_LTZ.S */
/* File: x86/zcmp.S */
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    cmpl     $0,(rFP,rINST,4)     # compare (vA, 0)
    movswl   2(rPC),rINST         # fetch signed displacement
    movl     $2,%eax             # assume branch not taken
    jge   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_GEZ: /* 0x3b */
/* File: x86/OP_IF_GEZ.S */
/* File: x86/zcmp.S */
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    cmpl     $0,(rFP,rINST,4)     # compare (vA, 0)
    movswl   2(rPC),rINST         # fetch signed displacement
    movl     $2,%eax             # assume branch not taken
    jl   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_GTZ: /* 0x3c */
/* File: x86/OP_IF_GTZ.S */
/* File: x86/zcmp.S */
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    cmpl     $0,(rFP,rINST,4)     # compare (vA, 0)
    movswl   2(rPC),rINST         # fetch signed displacement
    movl     $2,%eax             # assume branch not taken
    jle   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_IF_LEZ: /* 0x3d */
/* File: x86/OP_IF_LEZ.S */
/* File: x86/zcmp.S */
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    cmpl     $0,(rFP,rINST,4)     # compare (vA, 0)
    movswl   2(rPC),rINST         # fetch signed displacement
    movl     $2,%eax             # assume branch not taken
    jg   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3E: /* 0x3e */
/* File: x86/OP_UNUSED_3E.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3F: /* 0x3f */
/* File: x86/OP_UNUSED_3F.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_40: /* 0x40 */
/* File: x86/OP_UNUSED_40.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_41: /* 0x41 */
/* File: x86/OP_UNUSED_41.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_42: /* 0x42 */
/* File: x86/OP_UNUSED_42.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_43: /* 0x43 */
/* File: x86/OP_UNUSED_43.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_AGET: /* 0x44 */
/* File: x86/OP_AGET.S */
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx
    movl     offArrayObject_contents(%eax,%ecx,4),%eax
.LOP_AGET_finish:
    FETCH_INST_OPCODE 2 %edx
    SET_VREG  %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_AGET_WIDE: /* 0x45 */
/* File: x86/OP_AGET_WIDE.S */
    /*
     * Array get, 64 bits.  vAA <- vBB[vCC].
     *
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .LOP_AGET_WIDE_finish        # index < length, OK
    jmp       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx

/* ------------------------------ */
    .balign 64
.L_OP_AGET_OBJECT: /* 0x46 */
/* File: x86/OP_AGET_OBJECT.S */
/* File: x86/OP_AGET.S */
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx
    movl     offArrayObject_contents(%eax,%ecx,4),%eax
.LOP_AGET_OBJECT_finish:
    FETCH_INST_OPCODE 2 %edx
    SET_VREG  %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_AGET_BOOLEAN: /* 0x47 */
/* File: x86/OP_AGET_BOOLEAN.S */
/* File: x86/OP_AGET.S */
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx
    movzbl     offArrayObject_contents(%eax,%ecx,1),%eax
.LOP_AGET_BOOLEAN_finish:
    FETCH_INST_OPCODE 2 %edx
    SET_VREG  %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_AGET_BYTE: /* 0x48 */
/* File: x86/OP_AGET_BYTE.S */
/* File: x86/OP_AGET.S */
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx
    movsbl     offArrayObject_contents(%eax,%ecx,1),%eax
.LOP_AGET_BYTE_finish:
    FETCH_INST_OPCODE 2 %edx
    SET_VREG  %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_AGET_CHAR: /* 0x49 */
/* File: x86/OP_AGET_CHAR.S */
/* File: x86/OP_AGET.S */
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx
    movzwl     offArrayObject_contents(%eax,%ecx,2),%eax
.LOP_AGET_CHAR_finish:
    FETCH_INST_OPCODE 2 %edx
    SET_VREG  %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_AGET_SHORT: /* 0x4a */
/* File: x86/OP_AGET_SHORT.S */
/* File: x86/OP_AGET.S */
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx
    movswl     offArrayObject_contents(%eax,%ecx,2),%eax
.LOP_AGET_SHORT_finish:
    FETCH_INST_OPCODE 2 %edx
    SET_VREG  %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_APUT: /* 0x4b */
/* File: x86/OP_APUT.S */
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects:
                                        #   arrayObj in eax
                                        #   index in ecx
    leal      offArrayObject_contents(%eax,%ecx,4),%eax
.LOP_APUT_finish:
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    movl     %ecx,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_APUT_WIDE: /* 0x4c */
/* File: x86/OP_APUT_WIDE.S */
    /*
     * Array put, 64 bits.  vBB[vCC]<-vAA.
     *
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .LOP_APUT_WIDE_finish        # index < length, OK
    jmp       common_errArrayIndex      # index >= length, bail.  Expects:
                                        #   arrayObj in eax
                                        #   index in ecx

/* ------------------------------ */
    .balign 64
.L_OP_APUT_OBJECT: /* 0x4d */
/* File: x86/OP_APUT_OBJECT.S */
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    GET_VREG_R  rINST rINST             # rINST<- vAA
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .LOP_APUT_OBJECT_continue
    jmp       common_errArrayIndex      # index >= length, bail.  Expects
                                        #    arrayObj in eax
                                        #    index in ecx

/* ------------------------------ */
    .balign 64
.L_OP_APUT_BOOLEAN: /* 0x4e */
/* File: x86/OP_APUT_BOOLEAN.S */
/* File: x86/OP_APUT.S */
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects:
                                        #   arrayObj in eax
                                        #   index in ecx
    leal      offArrayObject_contents(%eax,%ecx,1),%eax
.LOP_APUT_BOOLEAN_finish:
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    movb     %cl,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_APUT_BYTE: /* 0x4f */
/* File: x86/OP_APUT_BYTE.S */
/* File: x86/OP_APUT.S */
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects:
                                        #   arrayObj in eax
                                        #   index in ecx
    leal      offArrayObject_contents(%eax,%ecx,1),%eax
.LOP_APUT_BYTE_finish:
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    movb     %cl,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_APUT_CHAR: /* 0x50 */
/* File: x86/OP_APUT_CHAR.S */
/* File: x86/OP_APUT.S */
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects:
                                        #   arrayObj in eax
                                        #   index in ecx
    leal      offArrayObject_contents(%eax,%ecx,2),%eax
.LOP_APUT_CHAR_finish:
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    movw     %cx,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_APUT_SHORT: /* 0x51 */
/* File: x86/OP_APUT_SHORT.S */
/* File: x86/OP_APUT.S */
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jae       common_errArrayIndex      # index >= length, bail.  Expects:
                                        #   arrayObj in eax
                                        #   index in ecx
    leal      offArrayObject_contents(%eax,%ecx,2),%eax
.LOP_APUT_SHORT_finish:
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    movw     %cx,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_IGET: /* 0x52 */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IGET_WIDE: /* 0x53 */
/* File: x86/OP_IGET_WIDE.S */
    /*
     * 64-bit instance field get.
     *
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_WIDE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # for dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_WIDE_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IGET_OBJECT: /* 0x54 */
/* File: x86/OP_IGET_OBJECT.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_OBJECT_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_OBJECT_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_BOOLEAN: /* 0x55 */
/* File: x86/OP_IGET_BOOLEAN.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_BOOLEAN_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_BOOLEAN_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_BYTE: /* 0x56 */
/* File: x86/OP_IGET_BYTE.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_BYTE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_BYTE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_CHAR: /* 0x57 */
/* File: x86/OP_IGET_CHAR.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_CHAR_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_CHAR_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_SHORT: /* 0x58 */
/* File: x86/OP_IGET_SHORT.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_SHORT_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_SHORT_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT: /* 0x59 */
/* File: x86/OP_IPUT.S */

    /*
     * General 32-bit instance field put.
     *
     * for: iput, iput-object, iput-boolean, iput-byte, iput-char, iput-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # %edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_WIDE: /* 0x5a */
/* File: x86/OP_IPUT_WIDE.S */
    /*
     * 64-bit instance field put.
     *
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_WIDE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_WIDE_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_OBJECT: /* 0x5b */
/* File: x86/OP_IPUT_OBJECT.S */
    /*
     * Object field put.
     *
     * for: iput-object
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_OBJECT_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_OBJECT_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_BOOLEAN: /* 0x5c */
/* File: x86/OP_IPUT_BOOLEAN.S */
/* File: x86/OP_IPUT.S */

    /*
     * General 32-bit instance field put.
     *
     * for: iput, iput-object, iput-boolean, iput-byte, iput-char, iput-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # %edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_BOOLEAN_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_BOOLEAN_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_BYTE: /* 0x5d */
/* File: x86/OP_IPUT_BYTE.S */
/* File: x86/OP_IPUT.S */

    /*
     * General 32-bit instance field put.
     *
     * for: iput, iput-object, iput-boolean, iput-byte, iput-char, iput-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # %edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_BYTE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_BYTE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_CHAR: /* 0x5e */
/* File: x86/OP_IPUT_CHAR.S */
/* File: x86/OP_IPUT.S */

    /*
     * General 32-bit instance field put.
     *
     * for: iput, iput-object, iput-boolean, iput-byte, iput-char, iput-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # %edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_CHAR_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_CHAR_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_SHORT: /* 0x5f */
/* File: x86/OP_IPUT_SHORT.S */
/* File: x86/OP_IPUT.S */

    /*
     * General 32-bit instance field put.
     *
     * for: iput, iput-object, iput-boolean, iput-byte, iput-char, iput-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # %edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_SHORT_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_SHORT_resolve


/* ------------------------------ */
    .balign 64
.L_OP_SGET: /* 0x60 */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_resolve                # if not, make it so
.LOP_SGET_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SGET_WIDE: /* 0x61 */
/* File: x86/OP_SGET_WIDE.S */
    /*
     * 64-bit SGET handler.
     *
     */
    /* sget-wide vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_WIDE_resolve                # if not, make it so
.LOP_SGET_WIDE_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%ecx    # ecx<- lsw
    movl      4+offStaticField_value(%eax),%eax  # eax<- msw
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG_WORD %ecx rINST 0
    SET_VREG_WORD %eax rINST 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SGET_OBJECT: /* 0x62 */
/* File: x86/OP_SGET_OBJECT.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_OBJECT_resolve                # if not, make it so
.LOP_SGET_OBJECT_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_BOOLEAN: /* 0x63 */
/* File: x86/OP_SGET_BOOLEAN.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_BOOLEAN_resolve                # if not, make it so
.LOP_SGET_BOOLEAN_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_BYTE: /* 0x64 */
/* File: x86/OP_SGET_BYTE.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_BYTE_resolve                # if not, make it so
.LOP_SGET_BYTE_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_CHAR: /* 0x65 */
/* File: x86/OP_SGET_CHAR.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_CHAR_resolve                # if not, make it so
.LOP_SGET_CHAR_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_SHORT: /* 0x66 */
/* File: x86/OP_SGET_SHORT.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_SHORT_resolve                # if not, make it so
.LOP_SGET_SHORT_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT: /* 0x67 */
/* File: x86/OP_SPUT.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_resolve                # if not, make it so
.LOP_SPUT_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SPUT_WIDE: /* 0x68 */
/* File: x86/OP_SPUT_WIDE.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-object, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_WIDE_resolve                # if not, make it so
.LOP_SPUT_WIDE_finish:     # field ptr in eax
    GET_VREG_WORD %ecx rINST 0                  # rINST<- lsw
    GET_VREG_WORD rINST rINST 1                 # ecx<- msw
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    movl      rINST,4+offStaticField_value(%eax)
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SPUT_OBJECT: /* 0x69 */
/* File: x86/OP_SPUT_OBJECT.S */
    /*
     * SPUT object handler.
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_OBJECT_resolve                # if not, make it so
.LOP_SPUT_OBJECT_finish:                              # field ptr in eax
    movzbl    rINSTbl,%ecx                       # ecx<- AA
    GET_VREG_R  %ecx %ecx
    jmp       .LOP_SPUT_OBJECT_continue

/* ------------------------------ */
    .balign 64
.L_OP_SPUT_BOOLEAN: /* 0x6a */
/* File: x86/OP_SPUT_BOOLEAN.S */
/* File: x86/OP_SPUT.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_BOOLEAN_resolve                # if not, make it so
.LOP_SPUT_BOOLEAN_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_BYTE: /* 0x6b */
/* File: x86/OP_SPUT_BYTE.S */
/* File: x86/OP_SPUT.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_BYTE_resolve                # if not, make it so
.LOP_SPUT_BYTE_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_CHAR: /* 0x6c */
/* File: x86/OP_SPUT_CHAR.S */
/* File: x86/OP_SPUT.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_CHAR_resolve                # if not, make it so
.LOP_SPUT_CHAR_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_SHORT: /* 0x6d */
/* File: x86/OP_SPUT_SHORT.S */
/* File: x86/OP_SPUT.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_SHORT_resolve                # if not, make it so
.LOP_SPUT_SHORT_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_VIRTUAL: /* 0x6e */
/* File: x86/OP_INVOKE_VIRTUAL.S */

    /*
     * Handle a virtual method call.
     *
     * for: invoke-virtual, invoke-virtual/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%eax
    movzwl    2(rPC),%ecx                 # ecx<- BBBB
    movl      offThread_methodClassDex(%eax),%eax  # eax<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%eax),%eax   # eax<- pDvmDex->pResMethods
    movl      (%eax,%ecx,4),%eax          # eax<- resolved baseMethod
    testl     %eax,%eax                   # already resolved?
    jne       .LOP_INVOKE_VIRTUAL_continue        # yes, continue
    movl      rSELF,%eax
    movl      %ecx,OUT_ARG1(%esp)         # arg1<- ref
    movl      offThread_method(%eax),%eax   # eax<- self->method
    jmp       .LOP_INVOKE_VIRTUAL_more

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_SUPER: /* 0x6f */
/* File: x86/OP_INVOKE_SUPER.S */
    /*
     * Handle a "super" method call.
     *
     * for: invoke-super, invoke-super/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,rINST
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(rINST),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%ecx        # ecx<- resolved baseMethod
    movl      offThread_method(rINST),%eax # eax<- method
    movzwl    4(rPC),rINST              # rINST<- GFED or CCCC
    .if       (!0)
    andl      $0xf,rINST               # rINST<- D (or stays CCCC)
    .endif
    GET_VREG_R  rINST rINST             # rINST<- "this" ptr
    testl     rINST,rINST               # null "this"?
    je        common_errNullObject      # yes, throw
    movl      offMethod_clazz(%eax),%eax # eax<- method->clazz
    testl     %ecx,%ecx                 # already resolved?
    jne       .LOP_INVOKE_SUPER_continue      # yes - go on
    jmp       .LOP_INVOKE_SUPER_resolve

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_DIRECT: /* 0x70 */
/* File: x86/OP_INVOKE_DIRECT.S */
    /*
     * Handle a direct method call.
     *
     * (We could defer the "is 'this' pointer null" test to the common
     * method invocation code, and use a flag to indicate that static
     * calls don't count.  If we do this as part of copying the arguments
     * out we could avoiding loading the first arg twice.)
     *
     * for: invoke-direct, invoke-direct/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax              # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movzwl    4(rPC),%edx              # edx<- GFED or CCCC
    movl      (%ecx,%eax,4),%eax       # eax<- resolved methodToCall
    .if       (!0)
    andl      $0xf,%edx               # edx<- D (or stays CCCC)
    .endif
    testl     %eax,%eax                # already resolved?
    GET_VREG_R  %ecx %edx              # ecx<- "this" ptr
    je        .LOP_INVOKE_DIRECT_resolve      # not resolved, do it now
.LOP_INVOKE_DIRECT_finish:
    testl     %ecx,%ecx                # null "this"?
    jne       common_invokeMethodNoRange  # no, continue on
    jmp       common_errNullObject

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_STATIC: /* 0x71 */
/* File: x86/OP_INVOKE_STATIC.S */
    /*
     * Handle a static method call.
     *
     * for: invoke-static, invoke-static/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%eax        # eax<- resolved methodToCall
    testl     %eax,%eax
    jne       common_invokeMethodNoRange
    movl      rSELF,%ecx
    movl      offThread_method(%ecx),%ecx # ecx<- self->method
    movzwl    2(rPC),%eax
    movl      offMethod_clazz(%ecx),%ecx# ecx<- method->clazz
    movl      %eax,OUT_ARG1(%esp)       # arg1<- BBBB
    movl      %ecx,OUT_ARG0(%esp)       # arg0<- clazz
    jmp       .LOP_INVOKE_STATIC_continue

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_INTERFACE: /* 0x72 */
/* File: x86/OP_INVOKE_INTERFACE.S */
    /*
     * Handle an interface method call.
     *
     * for: invoke-interface, invoke-interface/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movzwl     4(rPC),%eax              # eax<- FEDC or CCCC
    movl       rSELF,%ecx
    .if        (!0)
    andl       $0xf,%eax               # eax<- C (or stays CCCC)
    .endif
    GET_VREG_R   %eax %eax              # eax<- "this"
    EXPORT_PC
    testl      %eax,%eax                # null this?
    je         common_errNullObject     # yes, fail
    movl       offObject_clazz(%eax),%eax# eax<- thisPtr->clazz
    movl       %eax,OUT_ARG0(%esp)                 # arg0<- class
    movl       offThread_methodClassDex(%ecx),%eax   # eax<- methodClassDex
    movl       offThread_method(%ecx),%ecx           # ecx<- method
    movl       %eax,OUT_ARG3(%esp)                 # arg3<- dex
    movzwl     2(rPC),%eax                         # eax<- BBBB
    movl       %ecx,OUT_ARG2(%esp)                 # arg2<- method
    movl       %eax,OUT_ARG1(%esp)                 # arg1<- BBBB
    jmp        .LOP_INVOKE_INTERFACE_continue

/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_73: /* 0x73 */
/* File: x86/OP_UNUSED_73.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_VIRTUAL_RANGE: /* 0x74 */
/* File: x86/OP_INVOKE_VIRTUAL_RANGE.S */
/* File: x86/OP_INVOKE_VIRTUAL.S */

    /*
     * Handle a virtual method call.
     *
     * for: invoke-virtual, invoke-virtual/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%eax
    movzwl    2(rPC),%ecx                 # ecx<- BBBB
    movl      offThread_methodClassDex(%eax),%eax  # eax<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%eax),%eax   # eax<- pDvmDex->pResMethods
    movl      (%eax,%ecx,4),%eax          # eax<- resolved baseMethod
    testl     %eax,%eax                   # already resolved?
    jne       .LOP_INVOKE_VIRTUAL_RANGE_continue        # yes, continue
    movl      rSELF,%eax
    movl      %ecx,OUT_ARG1(%esp)         # arg1<- ref
    movl      offThread_method(%eax),%eax   # eax<- self->method
    jmp       .LOP_INVOKE_VIRTUAL_RANGE_more


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_SUPER_RANGE: /* 0x75 */
/* File: x86/OP_INVOKE_SUPER_RANGE.S */
/* File: x86/OP_INVOKE_SUPER.S */
    /*
     * Handle a "super" method call.
     *
     * for: invoke-super, invoke-super/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,rINST
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(rINST),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%ecx        # ecx<- resolved baseMethod
    movl      offThread_method(rINST),%eax # eax<- method
    movzwl    4(rPC),rINST              # rINST<- GFED or CCCC
    .if       (!1)
    andl      $0xf,rINST               # rINST<- D (or stays CCCC)
    .endif
    GET_VREG_R  rINST rINST             # rINST<- "this" ptr
    testl     rINST,rINST               # null "this"?
    je        common_errNullObject      # yes, throw
    movl      offMethod_clazz(%eax),%eax # eax<- method->clazz
    testl     %ecx,%ecx                 # already resolved?
    jne       .LOP_INVOKE_SUPER_RANGE_continue      # yes - go on
    jmp       .LOP_INVOKE_SUPER_RANGE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_DIRECT_RANGE: /* 0x76 */
/* File: x86/OP_INVOKE_DIRECT_RANGE.S */
/* File: x86/OP_INVOKE_DIRECT.S */
    /*
     * Handle a direct method call.
     *
     * (We could defer the "is 'this' pointer null" test to the common
     * method invocation code, and use a flag to indicate that static
     * calls don't count.  If we do this as part of copying the arguments
     * out we could avoiding loading the first arg twice.)
     *
     * for: invoke-direct, invoke-direct/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax              # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movzwl    4(rPC),%edx              # edx<- GFED or CCCC
    movl      (%ecx,%eax,4),%eax       # eax<- resolved methodToCall
    .if       (!1)
    andl      $0xf,%edx               # edx<- D (or stays CCCC)
    .endif
    testl     %eax,%eax                # already resolved?
    GET_VREG_R  %ecx %edx              # ecx<- "this" ptr
    je        .LOP_INVOKE_DIRECT_RANGE_resolve      # not resolved, do it now
.LOP_INVOKE_DIRECT_RANGE_finish:
    testl     %ecx,%ecx                # null "this"?
    jne       common_invokeMethodRange  # no, continue on
    jmp       common_errNullObject


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_STATIC_RANGE: /* 0x77 */
/* File: x86/OP_INVOKE_STATIC_RANGE.S */
/* File: x86/OP_INVOKE_STATIC.S */
    /*
     * Handle a static method call.
     *
     * for: invoke-static, invoke-static/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%eax        # eax<- resolved methodToCall
    testl     %eax,%eax
    jne       common_invokeMethodRange
    movl      rSELF,%ecx
    movl      offThread_method(%ecx),%ecx # ecx<- self->method
    movzwl    2(rPC),%eax
    movl      offMethod_clazz(%ecx),%ecx# ecx<- method->clazz
    movl      %eax,OUT_ARG1(%esp)       # arg1<- BBBB
    movl      %ecx,OUT_ARG0(%esp)       # arg0<- clazz
    jmp       .LOP_INVOKE_STATIC_RANGE_continue


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_INTERFACE_RANGE: /* 0x78 */
/* File: x86/OP_INVOKE_INTERFACE_RANGE.S */
/* File: x86/OP_INVOKE_INTERFACE.S */
    /*
     * Handle an interface method call.
     *
     * for: invoke-interface, invoke-interface/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movzwl     4(rPC),%eax              # eax<- FEDC or CCCC
    movl       rSELF,%ecx
    .if        (!1)
    andl       $0xf,%eax               # eax<- C (or stays CCCC)
    .endif
    GET_VREG_R   %eax %eax              # eax<- "this"
    EXPORT_PC
    testl      %eax,%eax                # null this?
    je         common_errNullObject     # yes, fail
    movl       offObject_clazz(%eax),%eax# eax<- thisPtr->clazz
    movl       %eax,OUT_ARG0(%esp)                 # arg0<- class
    movl       offThread_methodClassDex(%ecx),%eax   # eax<- methodClassDex
    movl       offThread_method(%ecx),%ecx           # ecx<- method
    movl       %eax,OUT_ARG3(%esp)                 # arg3<- dex
    movzwl     2(rPC),%eax                         # eax<- BBBB
    movl       %ecx,OUT_ARG2(%esp)                 # arg2<- method
    movl       %eax,OUT_ARG1(%esp)                 # arg1<- BBBB
    jmp        .LOP_INVOKE_INTERFACE_RANGE_continue


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_79: /* 0x79 */
/* File: x86/OP_UNUSED_79.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7A: /* 0x7a */
/* File: x86/OP_UNUSED_7A.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_NEG_INT: /* 0x7b */
/* File: x86/OP_NEG_INT.S */
/* File: x86/unop.S */
    /*
     * Generic 32-bit unary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = op eax".
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx           # ecx<- A+
    sarl     $4,rINST             # rINST<- B
    GET_VREG_R %eax rINST           # eax<- vB
    andb     $0xf,%cl              # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    
    negl %eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_NOT_INT: /* 0x7c */
/* File: x86/OP_NOT_INT.S */
/* File: x86/unop.S */
    /*
     * Generic 32-bit unary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = op eax".
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx           # ecx<- A+
    sarl     $4,rINST             # rINST<- B
    GET_VREG_R %eax rINST           # eax<- vB
    andb     $0xf,%cl              # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    
    notl %eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_NEG_LONG: /* 0x7d */
/* File: x86/OP_NEG_LONG.S */
    /* unop vA, vB */
    movzbl    rINSTbl,%ecx        # ecx<- BA
    sarl      $4,%ecx            # ecx<- B
    andb      $0xf,rINSTbl       # rINST<- A
    GET_VREG_WORD %eax %ecx 0     # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1     # ecx<- v[B+1]
    negl      %eax
    adcl      $0,%ecx
    negl      %ecx
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0    # v[A+0]<- eax
    SET_VREG_WORD %ecx rINST 1    # v[A+1]<- ecx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_NOT_LONG: /* 0x7e */
/* File: x86/OP_NOT_LONG.S */
    /* unop vA, vB */
    movzbl    rINSTbl,%ecx       # ecx<- BA
    sarl      $4,%ecx           # ecx<- B
    andb      $0xf,rINSTbl      # rINST<- A
    GET_VREG_WORD %eax %ecx 0    # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1    # ecx<- v[B+1]
    FETCH_INST_OPCODE 1 %edx
    notl      %eax
    notl      %ecx
    SET_VREG_WORD %eax rINST 0   # v[A+0]<- eax
    SET_VREG_WORD %ecx rINST 1   # v[A+1]<- ecx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_NEG_FLOAT: /* 0x7f */
/* File: x86/OP_NEG_FLOAT.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    flds    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fchs
    fstps  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_NEG_DOUBLE: /* 0x80 */
/* File: x86/OP_NEG_DOUBLE.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    fldl    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fchs
    fstpl  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INT_TO_LONG: /* 0x81 */
/* File: x86/OP_INT_TO_LONG.S */
    /* int to long vA, vB */
    movzbl  rINSTbl,%eax                # eax<- +A
    sarl    $4,%eax                    # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    andb    $0xf,rINSTbl               # rINST<- A
    cltd                                # edx:eax<- sssssssBBBBBBBB
    SET_VREG_WORD %edx rINST 1          # v[A+1]<- edx/rPC
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0          # v[A+0]<- %eax
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_INT_TO_FLOAT: /* 0x82 */
/* File: x86/OP_INT_TO_FLOAT.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    fildl    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    fstps  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INT_TO_DOUBLE: /* 0x83 */
/* File: x86/OP_INT_TO_DOUBLE.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    fildl    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    fstpl  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_LONG_TO_INT: /* 0x84 */
/* File: x86/OP_LONG_TO_INT.S */
/* we ignore the high word, making this equivalent to a 32-bit reg move */
/* File: x86/OP_MOVE.S */
    /* for move, move-object, long-to-int */
    /* op vA, vB */
    movzbl rINSTbl,%eax          # eax<- BA
    andb   $0xf,%al             # eax<- A
    shrl   $4,rINST            # rINST<- B
    GET_VREG_R %ecx rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG %ecx %eax           # fp[A]<-fp[B]
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_LONG_TO_FLOAT: /* 0x85 */
/* File: x86/OP_LONG_TO_FLOAT.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    fildll    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    fstps  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_LONG_TO_DOUBLE: /* 0x86 */
/* File: x86/OP_LONG_TO_DOUBLE.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    fildll    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    fstpl  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_FLOAT_TO_INT: /* 0x87 */
/* File: x86/OP_FLOAT_TO_INT.S */
/* File: x86/cvtfp_int.S */
/* On fp to int conversions, Java requires that
 * if the result > maxint, it should be clamped to maxint.  If it is less
 * than minint, it should be clamped to minint.  If it is a nan, the result
 * should be zero.  Further, the rounding mode is to truncate.  This model
 * differs from what is delivered normally via the x86 fpu, so we have
 * to play some games.
 */
    /* float/double to int/long vA, vB */
    movzbl    rINSTbl,%ecx       # ecx<- A+
    sarl      $4,rINST         # rINST<- B
    .if 0
    fldl     (rFP,rINST,4)       # %st0<- vB
    .else
    flds     (rFP,rINST,4)       # %st0<- vB
    .endif
    ftst
    fnstcw   LOCAL0_OFFSET(%ebp)      # remember original rounding mode
    movzwl   LOCAL0_OFFSET(%ebp),%eax
    movb     $0xc,%ah
    movw     %ax,LOCAL0_OFFSET+2(%ebp)
    fldcw    LOCAL0_OFFSET+2(%ebp)    # set "to zero" rounding mode
    FETCH_INST_OPCODE 1 %edx
    andb     $0xf,%cl                # ecx<- A
    .if 0
    fistpll  (rFP,%ecx,4)             # convert and store
    .else
    fistpl   (rFP,%ecx,4)             # convert and store
    .endif
    fldcw    LOCAL0_OFFSET(%ebp)      # restore previous rounding mode
    jmp      .LOP_FLOAT_TO_INT_continue


/* ------------------------------ */
    .balign 64
.L_OP_FLOAT_TO_LONG: /* 0x88 */
/* File: x86/OP_FLOAT_TO_LONG.S */
/* File: x86/cvtfp_int.S */
/* On fp to int conversions, Java requires that
 * if the result > maxint, it should be clamped to maxint.  If it is less
 * than minint, it should be clamped to minint.  If it is a nan, the result
 * should be zero.  Further, the rounding mode is to truncate.  This model
 * differs from what is delivered normally via the x86 fpu, so we have
 * to play some games.
 */
    /* float/double to int/long vA, vB */
    movzbl    rINSTbl,%ecx       # ecx<- A+
    sarl      $4,rINST         # rINST<- B
    .if 0
    fldl     (rFP,rINST,4)       # %st0<- vB
    .else
    flds     (rFP,rINST,4)       # %st0<- vB
    .endif
    ftst
    fnstcw   LOCAL0_OFFSET(%ebp)      # remember original rounding mode
    movzwl   LOCAL0_OFFSET(%ebp),%eax
    movb     $0xc,%ah
    movw     %ax,LOCAL0_OFFSET+2(%ebp)
    fldcw    LOCAL0_OFFSET+2(%ebp)    # set "to zero" rounding mode
    FETCH_INST_OPCODE 1 %edx
    andb     $0xf,%cl                # ecx<- A
    .if 1
    fistpll  (rFP,%ecx,4)             # convert and store
    .else
    fistpl   (rFP,%ecx,4)             # convert and store
    .endif
    fldcw    LOCAL0_OFFSET(%ebp)      # restore previous rounding mode
    jmp      .LOP_FLOAT_TO_LONG_continue


/* ------------------------------ */
    .balign 64
.L_OP_FLOAT_TO_DOUBLE: /* 0x89 */
/* File: x86/OP_FLOAT_TO_DOUBLE.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    flds    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    fstpl  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_DOUBLE_TO_INT: /* 0x8a */
/* File: x86/OP_DOUBLE_TO_INT.S */
/* File: x86/cvtfp_int.S */
/* On fp to int conversions, Java requires that
 * if the result > maxint, it should be clamped to maxint.  If it is less
 * than minint, it should be clamped to minint.  If it is a nan, the result
 * should be zero.  Further, the rounding mode is to truncate.  This model
 * differs from what is delivered normally via the x86 fpu, so we have
 * to play some games.
 */
    /* float/double to int/long vA, vB */
    movzbl    rINSTbl,%ecx       # ecx<- A+
    sarl      $4,rINST         # rINST<- B
    .if 1
    fldl     (rFP,rINST,4)       # %st0<- vB
    .else
    flds     (rFP,rINST,4)       # %st0<- vB
    .endif
    ftst
    fnstcw   LOCAL0_OFFSET(%ebp)      # remember original rounding mode
    movzwl   LOCAL0_OFFSET(%ebp),%eax
    movb     $0xc,%ah
    movw     %ax,LOCAL0_OFFSET+2(%ebp)
    fldcw    LOCAL0_OFFSET+2(%ebp)    # set "to zero" rounding mode
    FETCH_INST_OPCODE 1 %edx
    andb     $0xf,%cl                # ecx<- A
    .if 0
    fistpll  (rFP,%ecx,4)             # convert and store
    .else
    fistpl   (rFP,%ecx,4)             # convert and store
    .endif
    fldcw    LOCAL0_OFFSET(%ebp)      # restore previous rounding mode
    jmp      .LOP_DOUBLE_TO_INT_continue


/* ------------------------------ */
    .balign 64
.L_OP_DOUBLE_TO_LONG: /* 0x8b */
/* File: x86/OP_DOUBLE_TO_LONG.S */
/* File: x86/cvtfp_int.S */
/* On fp to int conversions, Java requires that
 * if the result > maxint, it should be clamped to maxint.  If it is less
 * than minint, it should be clamped to minint.  If it is a nan, the result
 * should be zero.  Further, the rounding mode is to truncate.  This model
 * differs from what is delivered normally via the x86 fpu, so we have
 * to play some games.
 */
    /* float/double to int/long vA, vB */
    movzbl    rINSTbl,%ecx       # ecx<- A+
    sarl      $4,rINST         # rINST<- B
    .if 1
    fldl     (rFP,rINST,4)       # %st0<- vB
    .else
    flds     (rFP,rINST,4)       # %st0<- vB
    .endif
    ftst
    fnstcw   LOCAL0_OFFSET(%ebp)      # remember original rounding mode
    movzwl   LOCAL0_OFFSET(%ebp),%eax
    movb     $0xc,%ah
    movw     %ax,LOCAL0_OFFSET+2(%ebp)
    fldcw    LOCAL0_OFFSET+2(%ebp)    # set "to zero" rounding mode
    FETCH_INST_OPCODE 1 %edx
    andb     $0xf,%cl                # ecx<- A
    .if 1
    fistpll  (rFP,%ecx,4)             # convert and store
    .else
    fistpl   (rFP,%ecx,4)             # convert and store
    .endif
    fldcw    LOCAL0_OFFSET(%ebp)      # restore previous rounding mode
    jmp      .LOP_DOUBLE_TO_LONG_continue


/* ------------------------------ */
    .balign 64
.L_OP_DOUBLE_TO_FLOAT: /* 0x8c */
/* File: x86/OP_DOUBLE_TO_FLOAT.S */
/* File: x86/fpcvt.S */
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $4,rINST         # rINST<- B
    fldl    (rFP,rINST,4)      # %st0<- vB
    andb     $0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    fstps  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INT_TO_BYTE: /* 0x8d */
/* File: x86/OP_INT_TO_BYTE.S */
/* File: x86/unop.S */
    /*
     * Generic 32-bit unary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = op eax".
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx           # ecx<- A+
    sarl     $4,rINST             # rINST<- B
    GET_VREG_R %eax rINST           # eax<- vB
    andb     $0xf,%cl              # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    
    movsbl %al,%eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INT_TO_CHAR: /* 0x8e */
/* File: x86/OP_INT_TO_CHAR.S */
/* File: x86/unop.S */
    /*
     * Generic 32-bit unary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = op eax".
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx           # ecx<- A+
    sarl     $4,rINST             # rINST<- B
    GET_VREG_R %eax rINST           # eax<- vB
    andb     $0xf,%cl              # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    
    movzwl %ax,%eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INT_TO_SHORT: /* 0x8f */
/* File: x86/OP_INT_TO_SHORT.S */
/* File: x86/unop.S */
    /*
     * Generic 32-bit unary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = op eax".
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx           # ecx<- A+
    sarl     $4,rINST             # rINST<- B
    GET_VREG_R %eax rINST           # eax<- vB
    andb     $0xf,%cl              # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    
    
    movswl %ax,%eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_ADD_INT: /* 0x90 */
/* File: x86/OP_ADD_INT.S */
/* File: x86/binop.S */
    /*
     * Generic 32-bit binary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = eax op (rFP,%ecx,4)".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int, sub-int, and-int, or-int,
     *      xor-int, shl-int, shr-int, ushr-int
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax   # eax<- BB
    movzbl   3(rPC),%ecx   # ecx<- CC
    GET_VREG_R %eax %eax   # eax<- vBB
    addl (rFP,%ecx,4),%eax                 # ex: addl    (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_INT: /* 0x91 */
/* File: x86/OP_SUB_INT.S */
/* File: x86/binop.S */
    /*
     * Generic 32-bit binary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = eax op (rFP,%ecx,4)".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int, sub-int, and-int, or-int,
     *      xor-int, shl-int, shr-int, ushr-int
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax   # eax<- BB
    movzbl   3(rPC),%ecx   # ecx<- CC
    GET_VREG_R %eax %eax   # eax<- vBB
    subl   (rFP,%ecx,4),%eax                 # ex: addl    (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_INT: /* 0x92 */
/* File: x86/OP_MUL_INT.S */
    /*
     * 32-bit binary multiplication.
     */
    /* mul vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    imull    (rFP,%ecx,4),%eax      # trashes edx
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_DIV_INT: /* 0x93 */
/* File: x86/OP_DIV_INT.S */
/* File: x86/bindiv.S */

    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    GET_VREG_R %ecx %ecx            # eax<- vBB
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $-1,%ecx
    jne      .LOP_DIV_INT_continue_div
    cmpl     $0x80000000,%eax
    jne      .LOP_DIV_INT_continue_div
    movl     $0x80000000,%eax
    jmp      .LOP_DIV_INT_finish_div



/* ------------------------------ */
    .balign 64
.L_OP_REM_INT: /* 0x94 */
/* File: x86/OP_REM_INT.S */
/* File: x86/bindiv.S */

    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    GET_VREG_R %ecx %ecx            # eax<- vBB
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $-1,%ecx
    jne      .LOP_REM_INT_continue_div
    cmpl     $0x80000000,%eax
    jne      .LOP_REM_INT_continue_div
    movl     $0,%edx
    jmp      .LOP_REM_INT_finish_div



/* ------------------------------ */
    .balign 64
.L_OP_AND_INT: /* 0x95 */
/* File: x86/OP_AND_INT.S */
/* File: x86/binop.S */
    /*
     * Generic 32-bit binary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = eax op (rFP,%ecx,4)".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int, sub-int, and-int, or-int,
     *      xor-int, shl-int, shr-int, ushr-int
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax   # eax<- BB
    movzbl   3(rPC),%ecx   # ecx<- CC
    GET_VREG_R %eax %eax   # eax<- vBB
    andl   (rFP,%ecx,4),%eax                 # ex: addl    (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_OR_INT: /* 0x96 */
/* File: x86/OP_OR_INT.S */
/* File: x86/binop.S */
    /*
     * Generic 32-bit binary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = eax op (rFP,%ecx,4)".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int, sub-int, and-int, or-int,
     *      xor-int, shl-int, shr-int, ushr-int
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax   # eax<- BB
    movzbl   3(rPC),%ecx   # ecx<- CC
    GET_VREG_R %eax %eax   # eax<- vBB
    orl   (rFP,%ecx,4),%eax                 # ex: addl    (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_XOR_INT: /* 0x97 */
/* File: x86/OP_XOR_INT.S */
/* File: x86/binop.S */
    /*
     * Generic 32-bit binary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = eax op (rFP,%ecx,4)".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int, sub-int, and-int, or-int,
     *      xor-int, shl-int, shr-int, ushr-int
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax   # eax<- BB
    movzbl   3(rPC),%ecx   # ecx<- CC
    GET_VREG_R %eax %eax   # eax<- vBB
    xorl   (rFP,%ecx,4),%eax                 # ex: addl    (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHL_INT: /* 0x98 */
/* File: x86/OP_SHL_INT.S */
/* File: x86/binop1.S */
    /*
     * Generic 32-bit binary operation in which both operands loaded to
     * registers (op0 in eax, op1 in ecx).
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    GET_VREG_R %ecx %ecx            # eax<- vBB
    sall    %cl,%eax                          # ex: addl    %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHR_INT: /* 0x99 */
/* File: x86/OP_SHR_INT.S */
/* File: x86/binop1.S */
    /*
     * Generic 32-bit binary operation in which both operands loaded to
     * registers (op0 in eax, op1 in ecx).
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    GET_VREG_R %ecx %ecx            # eax<- vBB
    sarl    %cl,%eax                          # ex: addl    %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_USHR_INT: /* 0x9a */
/* File: x86/OP_USHR_INT.S */
/* File: x86/binop1.S */
    /*
     * Generic 32-bit binary operation in which both operands loaded to
     * registers (op0 in eax, op1 in ecx).
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    GET_VREG_R %ecx %ecx            # eax<- vBB
    shrl    %cl,%eax                          # ex: addl    %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_ADD_LONG: /* 0x9b */
/* File: x86/OP_ADD_LONG.S */
/* File: x86/binopWide.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 0           # edx<- v[BB+0]
    GET_VREG_WORD %eax %eax 1           # eax<- v[BB+1]
    addl (rFP,%ecx,4),%edx         # ex: addl   (rFP,%ecx,4),%edx
    adcl 4(rFP,%ecx,4),%eax         # ex: adcl   4(rFP,%ecx,4),%eax
    SET_VREG_WORD %edx rINST 0          # v[AA+0] <- edx
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 1          # v[AA+1] <- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_LONG: /* 0x9c */
/* File: x86/OP_SUB_LONG.S */
/* File: x86/binopWide.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 0           # edx<- v[BB+0]
    GET_VREG_WORD %eax %eax 1           # eax<- v[BB+1]
    subl (rFP,%ecx,4),%edx         # ex: addl   (rFP,%ecx,4),%edx
    sbbl 4(rFP,%ecx,4),%eax         # ex: adcl   4(rFP,%ecx,4),%eax
    SET_VREG_WORD %edx rINST 0          # v[AA+0] <- edx
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 1          # v[AA+1] <- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_LONG: /* 0x9d */
/* File: x86/OP_MUL_LONG.S */
    /*
     * Signed 64-bit integer multiply.
     *
     * We could definately use more free registers for
     * this code.   We spill rINSTw (ebx),
     * giving us eax, ebc, ecx and edx as computational
     * temps.  On top of that, we'll spill edi (rFP)
     * for use as the vB pointer and esi (rPC) for use
     * as the vC pointer.  Yuck.
     */
    /* mul-long vAA, vBB, vCC */
    movzbl    2(rPC),%eax              # eax<- B
    movzbl    3(rPC),%ecx              # ecx<- C
    SPILL_TMP2(%esi)                   # save Dalvik PC
    SPILL(rFP)
    SPILL(rINST)
    leal      (rFP,%eax,4),%esi        # esi<- &v[B]
    leal      (rFP,%ecx,4),rFP         # rFP<- &v[C]
    movl      4(%esi),%ecx             # ecx<- Bmsw
    imull     (rFP),%ecx               # ecx<- (Bmsw*Clsw)
    movl      4(rFP),%eax              # eax<- Cmsw
    imull     (%esi),%eax              # eax<- (Cmsw*Blsw)
    addl      %eax,%ecx                # ecx<- (Bmsw*Clsw)+(Cmsw*Blsw)
    movl      (rFP),%eax               # eax<- Clsw
    mull      (%esi)                   # eax<- (Clsw*Alsw)
    UNSPILL(rINST)
    UNSPILL(rFP)
    jmp       .LOP_MUL_LONG_continue

/* ------------------------------ */
    .balign 64
.L_OP_DIV_LONG: /* 0x9e */
/* File: x86/OP_DIV_LONG.S */
    /* div vAA, vBB, vCC */
    movzbl    3(rPC),%eax              # eax<- CC
    movzbl    2(rPC),%ecx              # ecx<- BB
    GET_VREG_WORD %edx %eax 0
    GET_VREG_WORD %eax %eax 1
    movl     %edx,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .LOP_DIV_LONG_check_zero
    cmpl     $-1,%eax
    je       .LOP_DIV_LONG_check_neg1
.LOP_DIV_LONG_notSpecial:
    GET_VREG_WORD %edx %ecx 0
    GET_VREG_WORD %ecx %ecx 1
.LOP_DIV_LONG_notSpecial1:
    movl     %eax,OUT_ARG3(%esp)
    movl     %edx,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    jmp      .LOP_DIV_LONG_continue

/* ------------------------------ */
    .balign 64
.L_OP_REM_LONG: /* 0x9f */
/* File: x86/OP_REM_LONG.S */
/* File: x86/OP_DIV_LONG.S */
    /* div vAA, vBB, vCC */
    movzbl    3(rPC),%eax              # eax<- CC
    movzbl    2(rPC),%ecx              # ecx<- BB
    GET_VREG_WORD %edx %eax 0
    GET_VREG_WORD %eax %eax 1
    movl     %edx,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .LOP_REM_LONG_check_zero
    cmpl     $-1,%eax
    je       .LOP_REM_LONG_check_neg1
.LOP_REM_LONG_notSpecial:
    GET_VREG_WORD %edx %ecx 0
    GET_VREG_WORD %ecx %ecx 1
.LOP_REM_LONG_notSpecial1:
    movl     %eax,OUT_ARG3(%esp)
    movl     %edx,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    jmp      .LOP_REM_LONG_continue


/* ------------------------------ */
    .balign 64
.L_OP_AND_LONG: /* 0xa0 */
/* File: x86/OP_AND_LONG.S */
/* File: x86/binopWide.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 0           # edx<- v[BB+0]
    GET_VREG_WORD %eax %eax 1           # eax<- v[BB+1]
    andl (rFP,%ecx,4),%edx         # ex: addl   (rFP,%ecx,4),%edx
    andl 4(rFP,%ecx,4),%eax         # ex: adcl   4(rFP,%ecx,4),%eax
    SET_VREG_WORD %edx rINST 0          # v[AA+0] <- edx
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 1          # v[AA+1] <- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_OR_LONG: /* 0xa1 */
/* File: x86/OP_OR_LONG.S */
/* File: x86/binopWide.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 0           # edx<- v[BB+0]
    GET_VREG_WORD %eax %eax 1           # eax<- v[BB+1]
    orl (rFP,%ecx,4),%edx         # ex: addl   (rFP,%ecx,4),%edx
    orl 4(rFP,%ecx,4),%eax         # ex: adcl   4(rFP,%ecx,4),%eax
    SET_VREG_WORD %edx rINST 0          # v[AA+0] <- edx
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 1          # v[AA+1] <- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_XOR_LONG: /* 0xa2 */
/* File: x86/OP_XOR_LONG.S */
/* File: x86/binopWide.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 0           # edx<- v[BB+0]
    GET_VREG_WORD %eax %eax 1           # eax<- v[BB+1]
    xorl (rFP,%ecx,4),%edx         # ex: addl   (rFP,%ecx,4),%edx
    xorl 4(rFP,%ecx,4),%eax         # ex: adcl   4(rFP,%ecx,4),%eax
    SET_VREG_WORD %edx rINST 0          # v[AA+0] <- edx
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 1          # v[AA+1] <- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHL_LONG: /* 0xa3 */
/* File: x86/OP_SHL_LONG.S */
    /*
     * Long integer shift.  This is different from the generic 32/64-bit
     * binary operations because vAA/vBB are 64-bit but vCC (the shift
     * distance) is 32-bit.  Also, Dalvik requires us to mask off the low
     * 6 bits of the shift distance.  x86 shifts automatically mask off
     * the low 5 bits of %cl, so have to handle the 64 > shiftcount > 31
     * case specially.
     */
    /* shl-long vAA, vBB, vCC */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 1           # ecx<- v[BB+1]
    GET_VREG_R   %ecx %ecx              # ecx<- vCC
    GET_VREG_WORD %eax %eax 0           # eax<- v[BB+0]
    shldl     %eax,%edx
    sall      %cl,%eax
    testb     $32,%cl
    je        2f
    movl      %eax,%edx
    xorl      %eax,%eax
2:
    SET_VREG_WORD %edx rINST 1          # v[AA+1]<- %edx
    FETCH_INST_OPCODE 2 %edx
    jmp       .LOP_SHL_LONG_finish

/* ------------------------------ */
    .balign 64
.L_OP_SHR_LONG: /* 0xa4 */
/* File: x86/OP_SHR_LONG.S */
    /*
     * Long integer shift.  This is different from the generic 32/64-bit
     * binary operations because vAA/vBB are 64-bit but vCC (the shift
     * distance) is 32-bit.  Also, Dalvik requires us to mask off the low
     * 6 bits of the shift distance.  x86 shifts automatically mask off
     * the low 5 bits of %cl, so have to handle the 64 > shiftcount > 31
     * case specially.
     */
    /* shr-long vAA, vBB, vCC */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 1           # edx<- v[BB+1]
    GET_VREG_R   %ecx %ecx              # ecx<- vCC
    GET_VREG_WORD %eax %eax 0           # eax<- v[BB+0]
    shrdl     %edx,%eax
    sarl      %cl,%edx
    testb     $32,%cl
    je        2f
    movl      %edx,%eax
    sarl      $31,%edx
2:
    SET_VREG_WORD %edx rINST 1          # v[AA+1]<- edx
    FETCH_INST_OPCODE 2 %edx
    jmp       .LOP_SHR_LONG_finish

/* ------------------------------ */
    .balign 64
.L_OP_USHR_LONG: /* 0xa5 */
/* File: x86/OP_USHR_LONG.S */
    /*
     * Long integer shift.  This is different from the generic 32/64-bit
     * binary operations because vAA/vBB are 64-bit but vCC (the shift
     * distance) is 32-bit.  Also, Dalvik requires us to mask off the low
     * 6 bits of the shift distance.  x86 shifts automatically mask off
     * the low 5 bits of %cl, so have to handle the 64 > shiftcount > 31
     * case specially.
     */
    /* shr-long vAA, vBB, vCC */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 1           # edx<- v[BB+1]
    GET_VREG_R  %ecx %ecx               # ecx<- vCC
    GET_VREG_WORD %eax %eax 0           # eax<- v[BB+0]
    shrdl     %edx,%eax
    shrl      %cl,%edx
    testb     $32,%cl
    je        2f
    movl      %edx,%eax
    xorl      %edx,%edx
2:
    SET_VREG_WORD %edx rINST 1          # v[AA+1]<- edx
    FETCH_INST_OPCODE 2 %edx
    jmp       .LOP_USHR_LONG_finish

/* ------------------------------ */
    .balign 64
.L_OP_ADD_FLOAT: /* 0xa6 */
/* File: x86/OP_ADD_FLOAT.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    flds    (rFP,%eax,4)         # vCC to fp stack
    fadds   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstps   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_FLOAT: /* 0xa7 */
/* File: x86/OP_SUB_FLOAT.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    flds    (rFP,%eax,4)         # vCC to fp stack
    fsubs   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstps   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_FLOAT: /* 0xa8 */
/* File: x86/OP_MUL_FLOAT.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    flds    (rFP,%eax,4)         # vCC to fp stack
    fmuls   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstps   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_DIV_FLOAT: /* 0xa9 */
/* File: x86/OP_DIV_FLOAT.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    flds    (rFP,%eax,4)         # vCC to fp stack
    fdivs   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstps   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_REM_FLOAT: /* 0xaa */
/* File: x86/OP_REM_FLOAT.S */
    /* rem_float vAA, vBB, vCC */
    movzbl   3(rPC),%ecx            # ecx<- BB
    movzbl   2(rPC),%eax            # eax<- CC
    flds     (rFP,%ecx,4)           # vCC to fp stack
    flds     (rFP,%eax,4)           # vCC to fp stack
    movzbl   rINSTbl,%ecx           # ecx<- AA
    FETCH_INST_OPCODE 2 %edx
1:
    fprem
    fstsw     %ax
    sahf
    jp        1b
    fstp      %st(1)
    ADVANCE_PC 2
    fstps    (rFP,%ecx,4)           # %st to vAA
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_ADD_DOUBLE: /* 0xab */
/* File: x86/OP_ADD_DOUBLE.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    fldl    (rFP,%eax,4)         # vCC to fp stack
    faddl   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstpl   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_DOUBLE: /* 0xac */
/* File: x86/OP_SUB_DOUBLE.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    fldl    (rFP,%eax,4)         # vCC to fp stack
    fsubl   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstpl   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_DOUBLE: /* 0xad */
/* File: x86/OP_MUL_DOUBLE.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    fldl    (rFP,%eax,4)         # vCC to fp stack
    fmull   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstpl   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_DIV_DOUBLE: /* 0xae */
/* File: x86/OP_DIV_DOUBLE.S */
/* File: x86/binflop.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax          # eax<- CC
    movzbl   3(rPC),%ecx          # ecx<- BB
    fldl    (rFP,%eax,4)         # vCC to fp stack
    fdivl   (rFP,%ecx,4)         # ex: faddp
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    fstpl   (rFP,rINST,4)         # %st to vAA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_REM_DOUBLE: /* 0xaf */
/* File: x86/OP_REM_DOUBLE.S */
    /* rem_float vAA, vBB, vCC */
    movzbl   3(rPC),%ecx            # ecx<- BB
    movzbl   2(rPC),%eax            # eax<- CC
    fldl     (rFP,%ecx,4)           # vCC to fp stack
    fldl     (rFP,%eax,4)           # vCC to fp stack
    movzbl   rINSTbl,%ecx           # ecx<- AA
    FETCH_INST_OPCODE 2 %edx
1:
    fprem
    fstsw     %ax
    sahf
    jp        1b
    fstp      %st(1)
    ADVANCE_PC 2
    fstpl    (rFP,%ecx,4)           # %st to vAA
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_ADD_INT_2ADDR: /* 0xb0 */
/* File: x86/OP_ADD_INT_2ADDR.S */
/* File: x86/binop2addr.S */
    /*
     * Generic 32-bit "/2addr" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = r0 op r1".
     * This could be an ARM instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * If "chkzero" is set to 1, we perform a divide-by-zero check on
     * vCC (r1).  Useful for integer division and modulus.
     *
     * For: add-int/2addr, sub-int/2addr, mul-int/2addr, div-int/2addr,
     *      rem-int/2addr, and-int/2addr, or-int/2addr, xor-int/2addr,
     *      shl-int/2addr, shr-int/2addr, ushr-int/2addr, add-float/2addr,
     *      sub-float/2addr, mul-float/2addr, div-float/2addr, rem-float/2addr
     */
    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    FETCH_INST_OPCODE 1 %edx
    andb    $0xf,%cl                  # ecx<- A
    addl     %eax,(rFP,%ecx,4)                             # for ex: addl   %eax,(rFP,%ecx,4)
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_INT_2ADDR: /* 0xb1 */
/* File: x86/OP_SUB_INT_2ADDR.S */
/* File: x86/binop2addr.S */
    /*
     * Generic 32-bit "/2addr" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = r0 op r1".
     * This could be an ARM instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * If "chkzero" is set to 1, we perform a divide-by-zero check on
     * vCC (r1).  Useful for integer division and modulus.
     *
     * For: add-int/2addr, sub-int/2addr, mul-int/2addr, div-int/2addr,
     *      rem-int/2addr, and-int/2addr, or-int/2addr, xor-int/2addr,
     *      shl-int/2addr, shr-int/2addr, ushr-int/2addr, add-float/2addr,
     *      sub-float/2addr, mul-float/2addr, div-float/2addr, rem-float/2addr
     */
    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    FETCH_INST_OPCODE 1 %edx
    andb    $0xf,%cl                  # ecx<- A
    subl     %eax,(rFP,%ecx,4)                             # for ex: addl   %eax,(rFP,%ecx,4)
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_INT_2ADDR: /* 0xb2 */
/* File: x86/OP_MUL_INT_2ADDR.S */
    /* mul vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    andb    $0xf,%cl                  # ecx<- A
    imull   (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 1 %edx
    SET_VREG %eax %ecx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_DIV_INT_2ADDR: /* 0xb3 */
/* File: x86/OP_DIV_INT_2ADDR.S */
/* File: x86/bindiv2addr.S */
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/2addr vA, vB */
    movzx    rINSTbl,%ecx          # eax<- BA
    sarl     $4,%ecx              # ecx<- B
    GET_VREG_R %ecx %ecx           # eax<- vBB
    andb     $0xf,rINSTbl         # rINST<- A
    GET_VREG_R %eax rINST          # eax<- vBB
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $-1,%ecx
    jne      .LOP_DIV_INT_2ADDR_continue_div2addr
    cmpl     $0x80000000,%eax
    jne      .LOP_DIV_INT_2ADDR_continue_div2addr
    movl     $0x80000000,%eax
    jmp      .LOP_DIV_INT_2ADDR_finish_div2addr



/* ------------------------------ */
    .balign 64
.L_OP_REM_INT_2ADDR: /* 0xb4 */
/* File: x86/OP_REM_INT_2ADDR.S */
/* File: x86/bindiv2addr.S */
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/2addr vA, vB */
    movzx    rINSTbl,%ecx          # eax<- BA
    sarl     $4,%ecx              # ecx<- B
    GET_VREG_R %ecx %ecx           # eax<- vBB
    andb     $0xf,rINSTbl         # rINST<- A
    GET_VREG_R %eax rINST          # eax<- vBB
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $-1,%ecx
    jne      .LOP_REM_INT_2ADDR_continue_div2addr
    cmpl     $0x80000000,%eax
    jne      .LOP_REM_INT_2ADDR_continue_div2addr
    movl     $0,%edx
    jmp      .LOP_REM_INT_2ADDR_finish_div2addr



/* ------------------------------ */
    .balign 64
.L_OP_AND_INT_2ADDR: /* 0xb5 */
/* File: x86/OP_AND_INT_2ADDR.S */
/* File: x86/binop2addr.S */
    /*
     * Generic 32-bit "/2addr" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = r0 op r1".
     * This could be an ARM instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * If "chkzero" is set to 1, we perform a divide-by-zero check on
     * vCC (r1).  Useful for integer division and modulus.
     *
     * For: add-int/2addr, sub-int/2addr, mul-int/2addr, div-int/2addr,
     *      rem-int/2addr, and-int/2addr, or-int/2addr, xor-int/2addr,
     *      shl-int/2addr, shr-int/2addr, ushr-int/2addr, add-float/2addr,
     *      sub-float/2addr, mul-float/2addr, div-float/2addr, rem-float/2addr
     */
    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    FETCH_INST_OPCODE 1 %edx
    andb    $0xf,%cl                  # ecx<- A
    andl     %eax,(rFP,%ecx,4)                             # for ex: addl   %eax,(rFP,%ecx,4)
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_OR_INT_2ADDR: /* 0xb6 */
/* File: x86/OP_OR_INT_2ADDR.S */
/* File: x86/binop2addr.S */
    /*
     * Generic 32-bit "/2addr" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = r0 op r1".
     * This could be an ARM instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * If "chkzero" is set to 1, we perform a divide-by-zero check on
     * vCC (r1).  Useful for integer division and modulus.
     *
     * For: add-int/2addr, sub-int/2addr, mul-int/2addr, div-int/2addr,
     *      rem-int/2addr, and-int/2addr, or-int/2addr, xor-int/2addr,
     *      shl-int/2addr, shr-int/2addr, ushr-int/2addr, add-float/2addr,
     *      sub-float/2addr, mul-float/2addr, div-float/2addr, rem-float/2addr
     */
    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    FETCH_INST_OPCODE 1 %edx
    andb    $0xf,%cl                  # ecx<- A
    orl     %eax,(rFP,%ecx,4)                             # for ex: addl   %eax,(rFP,%ecx,4)
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_XOR_INT_2ADDR: /* 0xb7 */
/* File: x86/OP_XOR_INT_2ADDR.S */
/* File: x86/binop2addr.S */
    /*
     * Generic 32-bit "/2addr" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = r0 op r1".
     * This could be an ARM instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * If "chkzero" is set to 1, we perform a divide-by-zero check on
     * vCC (r1).  Useful for integer division and modulus.
     *
     * For: add-int/2addr, sub-int/2addr, mul-int/2addr, div-int/2addr,
     *      rem-int/2addr, and-int/2addr, or-int/2addr, xor-int/2addr,
     *      shl-int/2addr, shr-int/2addr, ushr-int/2addr, add-float/2addr,
     *      sub-float/2addr, mul-float/2addr, div-float/2addr, rem-float/2addr
     */
    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    FETCH_INST_OPCODE 1 %edx
    andb    $0xf,%cl                  # ecx<- A
    xorl     %eax,(rFP,%ecx,4)                             # for ex: addl   %eax,(rFP,%ecx,4)
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHL_INT_2ADDR: /* 0xb8 */
/* File: x86/OP_SHL_INT_2ADDR.S */
/* File: x86/shop2addr.S */
    /*
     * Generic 32-bit "shift/2addr" operation.
     */
    /* shift/2addr vA, vB */
    movzx    rINSTbl,%ecx           # eax<- BA
    sarl     $4,%ecx               # ecx<- B
    GET_VREG_R %ecx %ecx            # eax<- vBB
    andb     $0xf,rINSTbl          # rINST<- A
    GET_VREG_R %eax rINST           # eax<- vAA
    sall    %cl,%eax                          # ex: sarl %cl,%eax
    FETCH_INST_OPCODE 1 %edx
    SET_VREG %eax rINST
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHR_INT_2ADDR: /* 0xb9 */
/* File: x86/OP_SHR_INT_2ADDR.S */
/* File: x86/shop2addr.S */
    /*
     * Generic 32-bit "shift/2addr" operation.
     */
    /* shift/2addr vA, vB */
    movzx    rINSTbl,%ecx           # eax<- BA
    sarl     $4,%ecx               # ecx<- B
    GET_VREG_R %ecx %ecx            # eax<- vBB
    andb     $0xf,rINSTbl          # rINST<- A
    GET_VREG_R %eax rINST           # eax<- vAA
    sarl    %cl,%eax                          # ex: sarl %cl,%eax
    FETCH_INST_OPCODE 1 %edx
    SET_VREG %eax rINST
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_USHR_INT_2ADDR: /* 0xba */
/* File: x86/OP_USHR_INT_2ADDR.S */
/* File: x86/shop2addr.S */
    /*
     * Generic 32-bit "shift/2addr" operation.
     */
    /* shift/2addr vA, vB */
    movzx    rINSTbl,%ecx           # eax<- BA
    sarl     $4,%ecx               # ecx<- B
    GET_VREG_R %ecx %ecx            # eax<- vBB
    andb     $0xf,rINSTbl          # rINST<- A
    GET_VREG_R %eax rINST           # eax<- vAA
    shrl    %cl,%eax                          # ex: sarl %cl,%eax
    FETCH_INST_OPCODE 1 %edx
    SET_VREG %eax rINST
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_ADD_LONG_2ADDR: /* 0xbb */
/* File: x86/OP_ADD_LONG_2ADDR.S */
/* File: x86/binopWide2addr.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop/2addr vA, vB */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_WORD %eax %ecx 0           # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1           # eax<- v[B+1]
    andb      $0xF,rINSTbl             # rINST<- A
    addl %eax,(rFP,rINST,4)         # example: addl   %eax,(rFP,rINST,4)
    adcl %ecx,4(rFP,rINST,4)         # example: adcl   %ecx,4(rFP,rINST,4)
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_LONG_2ADDR: /* 0xbc */
/* File: x86/OP_SUB_LONG_2ADDR.S */
/* File: x86/binopWide2addr.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop/2addr vA, vB */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_WORD %eax %ecx 0           # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1           # eax<- v[B+1]
    andb      $0xF,rINSTbl             # rINST<- A
    subl %eax,(rFP,rINST,4)         # example: addl   %eax,(rFP,rINST,4)
    sbbl %ecx,4(rFP,rINST,4)         # example: adcl   %ecx,4(rFP,rINST,4)
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_LONG_2ADDR: /* 0xbd */
/* File: x86/OP_MUL_LONG_2ADDR.S */
    /*
     * Signed 64-bit integer multiply, 2-addr version
     *
     * We could definately use more free registers for
     * this code.  We must spill %edx (edx) because it
     * is used by imul.  We'll also spill rINST (ebx),
     * giving us eax, ebc, ecx and edx as computational
     * temps.  On top of that, we'll spill %esi (edi)
     * for use as the vA pointer and rFP (esi) for use
     * as the vB pointer.  Yuck.
     */
    /* mul-long/2addr vA, vB */
    movzbl    rINSTbl,%eax             # eax<- BA
    andb      $0xf,%al                # eax<- A
    sarl      $4,rINST                # rINST<- B
    SPILL_TMP2(%esi)
    SPILL(rFP)
    leal      (rFP,%eax,4),%esi        # %esi<- &v[A]
    leal      (rFP,rINST,4),rFP        # rFP<- &v[B]
    movl      4(%esi),%ecx             # ecx<- Amsw
    imull     (rFP),%ecx               # ecx<- (Amsw*Blsw)
    movl      4(rFP),%eax              # eax<- Bmsw
    imull     (%esi),%eax              # eax<- (Bmsw*Alsw)
    addl      %eax,%ecx                # ecx<- (Amsw*Blsw)+(Bmsw*Alsw)
    movl      (rFP),%eax               # eax<- Blsw
    mull      (%esi)                   # eax<- (Blsw*Alsw)
    jmp       .LOP_MUL_LONG_2ADDR_continue

/* ------------------------------ */
    .balign 64
.L_OP_DIV_LONG_2ADDR: /* 0xbe */
/* File: x86/OP_DIV_LONG_2ADDR.S */
    /* div/2addr vA, vB */
    movzbl    rINSTbl,%eax
    shrl      $4,%eax                  # eax<- B
    andb      $0xf,rINSTbl             # rINST<- A
    GET_VREG_WORD %edx %eax 0
    GET_VREG_WORD %eax %eax 1
    movl     %edx,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .LOP_DIV_LONG_2ADDR_check_zero
    cmpl     $-1,%eax
    je       .LOP_DIV_LONG_2ADDR_check_neg1
.LOP_DIV_LONG_2ADDR_notSpecial:
    GET_VREG_WORD %edx rINST 0
    GET_VREG_WORD %ecx rINST 1
.LOP_DIV_LONG_2ADDR_notSpecial1:
    jmp      .LOP_DIV_LONG_2ADDR_continue

/* ------------------------------ */
    .balign 64
.L_OP_REM_LONG_2ADDR: /* 0xbf */
/* File: x86/OP_REM_LONG_2ADDR.S */
/* File: x86/OP_DIV_LONG_2ADDR.S */
    /* div/2addr vA, vB */
    movzbl    rINSTbl,%eax
    shrl      $4,%eax                  # eax<- B
    andb      $0xf,rINSTbl             # rINST<- A
    GET_VREG_WORD %edx %eax 0
    GET_VREG_WORD %eax %eax 1
    movl     %edx,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .LOP_REM_LONG_2ADDR_check_zero
    cmpl     $-1,%eax
    je       .LOP_REM_LONG_2ADDR_check_neg1
.LOP_REM_LONG_2ADDR_notSpecial:
    GET_VREG_WORD %edx rINST 0
    GET_VREG_WORD %ecx rINST 1
.LOP_REM_LONG_2ADDR_notSpecial1:
    jmp      .LOP_REM_LONG_2ADDR_continue


/* ------------------------------ */
    .balign 64
.L_OP_AND_LONG_2ADDR: /* 0xc0 */
/* File: x86/OP_AND_LONG_2ADDR.S */
/* File: x86/binopWide2addr.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop/2addr vA, vB */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_WORD %eax %ecx 0           # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1           # eax<- v[B+1]
    andb      $0xF,rINSTbl             # rINST<- A
    andl %eax,(rFP,rINST,4)         # example: addl   %eax,(rFP,rINST,4)
    andl %ecx,4(rFP,rINST,4)         # example: adcl   %ecx,4(rFP,rINST,4)
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_OR_LONG_2ADDR: /* 0xc1 */
/* File: x86/OP_OR_LONG_2ADDR.S */
/* File: x86/binopWide2addr.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop/2addr vA, vB */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_WORD %eax %ecx 0           # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1           # eax<- v[B+1]
    andb      $0xF,rINSTbl             # rINST<- A
    orl %eax,(rFP,rINST,4)         # example: addl   %eax,(rFP,rINST,4)
    orl %ecx,4(rFP,rINST,4)         # example: adcl   %ecx,4(rFP,rINST,4)
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_XOR_LONG_2ADDR: /* 0xc2 */
/* File: x86/OP_XOR_LONG_2ADDR.S */
/* File: x86/binopWide2addr.S */
    /*
     * Generic 64-bit binary operation.
     */
    /* binop/2addr vA, vB */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_WORD %eax %ecx 0           # eax<- v[B+0]
    GET_VREG_WORD %ecx %ecx 1           # eax<- v[B+1]
    andb      $0xF,rINSTbl             # rINST<- A
    xorl %eax,(rFP,rINST,4)         # example: addl   %eax,(rFP,rINST,4)
    xorl %ecx,4(rFP,rINST,4)         # example: adcl   %ecx,4(rFP,rINST,4)
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHL_LONG_2ADDR: /* 0xc3 */
/* File: x86/OP_SHL_LONG_2ADDR.S */
    /*
     * Long integer shift, 2addr version.  vA is 64-bit value/result, vB is
     * 32-bit shift distance.
     */
    /* shl-long/2addr vA, vB */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    rINSTbl,%ecx             # ecx<- BA
    andb      $0xf,rINSTbl            # rINST<- A
    GET_VREG_WORD %eax rINST 0         # eax<- v[AA+0]
    sarl      $4,%ecx                 # ecx<- B
    GET_VREG_WORD %edx rINST 1         # edx<- v[AA+1]
    GET_VREG_R  %ecx %ecx              # ecx<- vBB
    shldl     %eax,%edx
    sall      %cl,%eax
    testb     $32,%cl
    je        2f
    movl      %eax,%edx
    xorl      %eax,%eax
2:
    SET_VREG_WORD %edx rINST 1         # v[AA+1]<- edx
    jmp       .LOP_SHL_LONG_2ADDR_finish

/* ------------------------------ */
    .balign 64
.L_OP_SHR_LONG_2ADDR: /* 0xc4 */
/* File: x86/OP_SHR_LONG_2ADDR.S */
    /*
     * Long integer shift, 2addr version.  vA is 64-bit value/result, vB is
     * 32-bit shift distance.
     */
    /* shl-long/2addr vA, vB */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    rINSTbl,%ecx         # ecx<- BA
    andb      $0xf,rINSTbl        # rINST<- A
    GET_VREG_WORD %eax rINST 0     # eax<- v[AA+0]
    sarl      $4,%ecx             # ecx<- B
    GET_VREG_WORD %edx rINST 1     # edx<- v[AA+1]
    GET_VREG_R %ecx %ecx           # ecx<- vBB
    shrdl     %edx,%eax
    sarl      %cl,%edx
    testb     $32,%cl
    je        2f
    movl      %edx,%eax
    sarl      $31,%edx
2:
    SET_VREG_WORD %edx rINST 1     # v[AA+1]<- edx
    jmp       .LOP_SHR_LONG_2ADDR_finish

/* ------------------------------ */
    .balign 64
.L_OP_USHR_LONG_2ADDR: /* 0xc5 */
/* File: x86/OP_USHR_LONG_2ADDR.S */
    /*
     * Long integer shift, 2addr version.  vA is 64-bit value/result, vB is
     * 32-bit shift distance.
     */
    /* shl-long/2addr vA, vB */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    rINSTbl,%ecx             # ecx<- BA
    andb      $0xf,rINSTbl            # rINST<- A
    GET_VREG_WORD %eax rINST 0         # eax<- v[AA+0]
    sarl      $4,%ecx                 # ecx<- B
    GET_VREG_WORD %edx rINST 1         # edx<- v[AA+1]
    GET_VREG_R %ecx %ecx               # ecx<- vBB
    shrdl     %edx,%eax
    shrl      %cl,%edx
    testb     $32,%cl
    je        2f
    movl      %edx,%eax
    xorl      %edx,%edx
2:
    SET_VREG_WORD %edx rINST 1         # v[AA+1]<- edx
    jmp       .LOP_USHR_LONG_2ADDR_finish

/* ------------------------------ */
    .balign 64
.L_OP_ADD_FLOAT_2ADDR: /* 0xc6 */
/* File: x86/OP_ADD_FLOAT_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    flds    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fadds   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstps    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_FLOAT_2ADDR: /* 0xc7 */
/* File: x86/OP_SUB_FLOAT_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    flds    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fsubs   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstps    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_FLOAT_2ADDR: /* 0xc8 */
/* File: x86/OP_MUL_FLOAT_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    flds    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fmuls   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstps    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_DIV_FLOAT_2ADDR: /* 0xc9 */
/* File: x86/OP_DIV_FLOAT_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    flds    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fdivs   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstps    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_REM_FLOAT_2ADDR: /* 0xca */
/* File: x86/OP_REM_FLOAT_2ADDR.S */
    /* rem_float/2addr vA, vB */
    movzx   rINSTbl,%ecx                # ecx<- A+
    sarl    $4,rINST                  # rINST<- B
    flds     (rFP,rINST,4)              # vBB to fp stack
    andb    $0xf,%cl                   # ecx<- A
    flds     (rFP,%ecx,4)               # vAA to fp stack
    FETCH_INST_OPCODE 1 %edx
1:
    fprem
    fstsw     %ax
    sahf
    jp        1b
    fstp      %st(1)
    ADVANCE_PC 1
    fstps    (rFP,%ecx,4)               # %st to vA
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_ADD_DOUBLE_2ADDR: /* 0xcb */
/* File: x86/OP_ADD_DOUBLE_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    fldl    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    faddl   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstpl    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SUB_DOUBLE_2ADDR: /* 0xcc */
/* File: x86/OP_SUB_DOUBLE_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    fldl    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fsubl   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstpl    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_DOUBLE_2ADDR: /* 0xcd */
/* File: x86/OP_MUL_DOUBLE_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    fldl    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fmull   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstpl    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_DIV_DOUBLE_2ADDR: /* 0xce */
/* File: x86/OP_DIV_DOUBLE_2ADDR.S */
/* File: x86/binflop2addr.S */
    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINSTbl,%ecx           # ecx<- A+
    andb    $0xf,%cl              # ecx<- A
    fldl    (rFP,%ecx,4)          # vAA to fp stack
    sarl    $4,rINST             # rINST<- B
    fdivl   (rFP,rINST,4)         # ex: faddp
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    fstpl    (rFP,%ecx,4)         # %st to vA
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_REM_DOUBLE_2ADDR: /* 0xcf */
/* File: x86/OP_REM_DOUBLE_2ADDR.S */
    /* rem_float/2addr vA, vB */
    movzx   rINSTbl,%ecx                # ecx<- A+
    sarl    $4,rINST                  # rINST<- B
    fldl     (rFP,rINST,4)              # vBB to fp stack
    andb    $0xf,%cl                   # ecx<- A
    fldl     (rFP,%ecx,4)               # vAA to fp stack
    FETCH_INST_OPCODE 1 %edx
1:
    fprem
    fstsw     %ax
    sahf
    jp        1b
    fstp      %st(1)
    ADVANCE_PC 1
    fstpl    (rFP,%ecx,4)               # %st to vA
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_ADD_INT_LIT16: /* 0xd0 */
/* File: x86/OP_ADD_INT_LIT16.S */
/* File: x86/binopLit16.S */
    /*
     * Generic 32-bit "lit16" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int/lit16, rsub-int,
     *      and-int/lit16, or-int/lit16, xor-int/lit16
     */
    /* binop/lit16 vA, vB, #+CCCC */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $0xf,rINSTbl              # rINST<- A
    addl %ecx,%eax                              # for example: addl %ecx, %eax
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_RSUB_INT: /* 0xd1 */
/* File: x86/OP_RSUB_INT.S */
/* File: x86/binopLit16.S */
    /*
     * Generic 32-bit "lit16" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int/lit16, rsub-int,
     *      and-int/lit16, or-int/lit16, xor-int/lit16
     */
    /* binop/lit16 vA, vB, #+CCCC */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $0xf,rINSTbl              # rINST<- A
    subl %eax,%ecx                              # for example: addl %ecx, %eax
    SET_VREG %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_INT_LIT16: /* 0xd2 */
/* File: x86/OP_MUL_INT_LIT16.S */
    /* mul/lit16 vA, vB, #+CCCC */
    /* Need A in rINST, ssssCCCC in ecx, vB in eax */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $0xf,rINSTbl              # rINST<- A
    imull     %ecx,%eax                 # trashes edx
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_DIV_INT_LIT16: /* 0xd3 */
/* File: x86/OP_DIV_INT_LIT16.S */
/* File: x86/bindivLit16.S */
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/lit16 vA, vB, #+CCCC */
    /* Need A in rINST, ssssCCCC in ecx, vB in eax */
    movzbl   rINSTbl,%eax         # eax<- 000000BA
    sarl     $4,%eax             # eax<- B
    GET_VREG_R %eax %eax          # eax<- vB
    movswl   2(rPC),%ecx          # ecx<- ssssCCCC
    andb     $0xf,rINSTbl        # rINST<- A
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $-1,%ecx
    jne      .LOP_DIV_INT_LIT16_continue_div
    cmpl     $0x80000000,%eax
    jne      .LOP_DIV_INT_LIT16_continue_div
    movl     $0x80000000,%eax
    jmp      .LOP_DIV_INT_LIT16_finish_div



/* ------------------------------ */
    .balign 64
.L_OP_REM_INT_LIT16: /* 0xd4 */
/* File: x86/OP_REM_INT_LIT16.S */
/* File: x86/bindivLit16.S */
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/lit16 vA, vB, #+CCCC */
    /* Need A in rINST, ssssCCCC in ecx, vB in eax */
    movzbl   rINSTbl,%eax         # eax<- 000000BA
    sarl     $4,%eax             # eax<- B
    GET_VREG_R %eax %eax          # eax<- vB
    movswl   2(rPC),%ecx          # ecx<- ssssCCCC
    andb     $0xf,rINSTbl        # rINST<- A
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $-1,%ecx
    jne      .LOP_REM_INT_LIT16_continue_div
    cmpl     $0x80000000,%eax
    jne      .LOP_REM_INT_LIT16_continue_div
    movl     $0,%edx
    jmp      .LOP_REM_INT_LIT16_finish_div



/* ------------------------------ */
    .balign 64
.L_OP_AND_INT_LIT16: /* 0xd5 */
/* File: x86/OP_AND_INT_LIT16.S */
/* File: x86/binopLit16.S */
    /*
     * Generic 32-bit "lit16" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int/lit16, rsub-int,
     *      and-int/lit16, or-int/lit16, xor-int/lit16
     */
    /* binop/lit16 vA, vB, #+CCCC */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $0xf,rINSTbl              # rINST<- A
    andl %ecx,%eax                              # for example: addl %ecx, %eax
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_OR_INT_LIT16: /* 0xd6 */
/* File: x86/OP_OR_INT_LIT16.S */
/* File: x86/binopLit16.S */
    /*
     * Generic 32-bit "lit16" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int/lit16, rsub-int,
     *      and-int/lit16, or-int/lit16, xor-int/lit16
     */
    /* binop/lit16 vA, vB, #+CCCC */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $0xf,rINSTbl              # rINST<- A
    orl     %ecx,%eax                              # for example: addl %ecx, %eax
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_XOR_INT_LIT16: /* 0xd7 */
/* File: x86/OP_XOR_INT_LIT16.S */
/* File: x86/binopLit16.S */
    /*
     * Generic 32-bit "lit16" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int/lit16, rsub-int,
     *      and-int/lit16, or-int/lit16, xor-int/lit16
     */
    /* binop/lit16 vA, vB, #+CCCC */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $0xf,rINSTbl              # rINST<- A
    xor    %ecx,%eax                              # for example: addl %ecx, %eax
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_ADD_INT_LIT8: /* 0xd8 */
/* File: x86/OP_ADD_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    addl %ecx,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_RSUB_INT_LIT8: /* 0xd9 */
/* File: x86/OP_RSUB_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    subl  %eax,%ecx                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %ecx rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_MUL_INT_LIT8: /* 0xda */
/* File: x86/OP_MUL_INT_LIT8.S */
    /* mul/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    imull     %ecx,%eax                # trashes edx
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG  %eax rINST
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_DIV_INT_LIT8: /* 0xdb */
/* File: x86/OP_DIV_INT_LIT8.S */
/* File: x86/bindivLit8.S */
    /*
     * 32-bit div/rem "lit8" binary operation.  Handles special case of
     * op0=minint & op1=-1
     */
    /* div/rem/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax        # eax<- BB
    movsbl    3(rPC),%ecx        # ecx<- ssssssCC
    GET_VREG_R  %eax %eax        # eax<- rBB
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $0x80000000,%eax
    jne      .LOP_DIV_INT_LIT8_continue_div
    cmpl     $-1,%ecx
    jne      .LOP_DIV_INT_LIT8_continue_div
    movl     $0x80000000,%eax
    jmp      .LOP_DIV_INT_LIT8_finish_div



/* ------------------------------ */
    .balign 64
.L_OP_REM_INT_LIT8: /* 0xdc */
/* File: x86/OP_REM_INT_LIT8.S */
/* File: x86/bindivLit8.S */
    /*
     * 32-bit div/rem "lit8" binary operation.  Handles special case of
     * op0=minint & op1=-1
     */
    /* div/rem/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax        # eax<- BB
    movsbl    3(rPC),%ecx        # ecx<- ssssssCC
    GET_VREG_R  %eax %eax        # eax<- rBB
    cmpl     $0,%ecx
    je       common_errDivideByZero
    cmpl     $0x80000000,%eax
    jne      .LOP_REM_INT_LIT8_continue_div
    cmpl     $-1,%ecx
    jne      .LOP_REM_INT_LIT8_continue_div
    movl     $0,%edx
    jmp      .LOP_REM_INT_LIT8_finish_div



/* ------------------------------ */
    .balign 64
.L_OP_AND_INT_LIT8: /* 0xdd */
/* File: x86/OP_AND_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    andl %ecx,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_OR_INT_LIT8: /* 0xde */
/* File: x86/OP_OR_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    orl     %ecx,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_XOR_INT_LIT8: /* 0xdf */
/* File: x86/OP_XOR_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    xor    %ecx,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHL_INT_LIT8: /* 0xe0 */
/* File: x86/OP_SHL_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    sall  %cl,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SHR_INT_LIT8: /* 0xe1 */
/* File: x86/OP_SHR_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    sarl    %cl,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_USHR_INT_LIT8: /* 0xe2 */
/* File: x86/OP_USHR_INT_LIT8.S */
/* File: x86/binopLit8.S */
    /*
     * Generic 32-bit "lit8" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than r0, you can override "result".)
     *
     * For: add-int/lit8, rsub-int/lit8
     *      and-int/lit8, or-int/lit8, xor-int/lit8,
     *      shl-int/lit8, shr-int/lit8, ushr-int/lit8
     */
    /* binop/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    shrl     %cl,%eax                             # ex: addl %ecx,%eax
    FETCH_INST_OPCODE 2 %edx
    SET_VREG   %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_IGET_VOLATILE: /* 0xe3 */
/* File: x86/OP_IGET_VOLATILE.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_VOLATILE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_VOLATILE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_VOLATILE: /* 0xe4 */
/* File: x86/OP_IPUT_VOLATILE.S */
/* File: x86/OP_IPUT.S */

    /*
     * General 32-bit instance field put.
     *
     * for: iput, iput-object, iput-boolean, iput-byte, iput-char, iput-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # %edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_VOLATILE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_VOLATILE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_SGET_VOLATILE: /* 0xe5 */
/* File: x86/OP_SGET_VOLATILE.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_VOLATILE_resolve                # if not, make it so
.LOP_SGET_VOLATILE_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_VOLATILE: /* 0xe6 */
/* File: x86/OP_SPUT_VOLATILE.S */
/* File: x86/OP_SPUT.S */
    /*
     * General 32-bit SPUT handler.
     *
     * for: sput, sput-boolean, sput-byte, sput-char, sput-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_VOLATILE_resolve                # if not, make it so
.LOP_SPUT_VOLATILE_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_IGET_OBJECT_VOLATILE: /* 0xe7 */
/* File: x86/OP_IGET_OBJECT_VOLATILE.S */
/* File: x86/OP_IGET.S */
    /*
     * General 32-bit instance field get.
     *
     * for: iget, iget-object, iget-boolean, iget-byte, iget-char, iget-short
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_OBJECT_VOLATILE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_OBJECT_VOLATILE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_WIDE_VOLATILE: /* 0xe8 */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_IGET_WIDE_VOLATILE     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_IPUT_WIDE_VOLATILE: /* 0xe9 */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_IPUT_WIDE_VOLATILE     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_SGET_WIDE_VOLATILE: /* 0xea */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_SGET_WIDE_VOLATILE     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_SPUT_WIDE_VOLATILE: /* 0xeb */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_SPUT_WIDE_VOLATILE     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_BREAKPOINT: /* 0xec */
/* File: x86/OP_BREAKPOINT.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_THROW_VERIFICATION_ERROR: /* 0xed */
/* File: x86/OP_THROW_VERIFICATION_ERROR.S */
    /*
     * Handle a throw-verification-error instruction.  This throws an
     * exception for an error discovered during verification.  The
     * exception is indicated by AA, with some detail provided by BBBB.
     */
    /* op AA, ref@BBBB */
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                     # eax<- BBBB
    movl     offThread_method(%ecx),%ecx       # ecx<- self->method
    EXPORT_PC
    movl     %eax,OUT_ARG2(%esp)             # arg2<- BBBB
    movl     rINST,OUT_ARG1(%esp)            # arg1<- AA
    movl     %ecx,OUT_ARG0(%esp)             # arg0<- method
    call     dvmThrowVerificationError       # call(method, kind, ref)
    jmp      common_exceptionThrown          # handle exception

/* ------------------------------ */
    .balign 64
.L_OP_EXECUTE_INLINE: /* 0xee */
/* File: x86/OP_EXECUTE_INLINE.S */
    /*
     * Execute a "native inline" instruction.
     *
     * We will be calling through a function table:
     *
     * (*gDvmInlineOpsTable[opIndex].func)(arg0, arg1, arg2, arg3, pResult)
     *
     * Ignores argument count - always loads 4.
     *
     */
    /* [opt] execute-inline vAA, {vC, vD, vE, vF}, inline@BBBB */
    movl      rSELF,%ecx
    EXPORT_PC
    movzwl    2(rPC),%eax               # eax<- BBBB
    leal      offThread_retval(%ecx),%ecx # ecx<- & self->retval
    movl      %ecx,OUT_ARG4(%esp)
    call      .LOP_EXECUTE_INLINE_continue      # make call; will return after
    testl     %eax,%eax                 # successful?
    FETCH_INST_OPCODE 3 %edx
    je        common_exceptionThrown    # no, handle exception
    ADVANCE_PC 3
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_EXECUTE_INLINE_RANGE: /* 0xef */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_EXECUTE_INLINE_RANGE     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_OBJECT_INIT: /* 0xf0 */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_INVOKE_OBJECT_INIT     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_RETURN_VOID_BARRIER: /* 0xf1 */
    /* (stub) */
    SAVE_PC_FP_TO_SELF %ecx          # leaves rSELF in %ecx
    movl %ecx,OUT_ARG0(%esp)         # self is first arg to function
    call      dvmMterp_OP_RETURN_VOID_BARRIER     # do the real work
    mov       rSELF,%ecx
    LOAD_PC_FP_FROM_SELF             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
/* ------------------------------ */
    .balign 64
.L_OP_IGET_QUICK: /* 0xf2 */
/* File: x86/OP_IGET_QUICK.S */
    /* For: iget-quick, iget-object-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_R  %ecx %ecx               # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    cmpl      $0,%ecx                  # is object null?
    je        common_errNullObject
    movl      (%ecx,%eax,1),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    andb      $0xf,rINSTbl             # rINST<- A
    SET_VREG  %eax rINST                # fp[A]<- result
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_IGET_WIDE_QUICK: /* 0xf3 */
/* File: x86/OP_IGET_WIDE_QUICK.S */
    /* For: iget-wide-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_R  %ecx %ecx               # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    cmpl      $0,%ecx                  # is object null?
    je        common_errNullObject
    leal      (%ecx,%eax,1),%eax        # eax<- address of 64-bit source
    movl      (%eax),%ecx               # ecx<- lsw
    movl      4(%eax),%eax              # eax<- msw
    andb      $0xf,rINSTbl             # rINST<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %ecx rINST 0          # v[A+0]<- lsw
    SET_VREG_WORD %eax rINST 1          # v[A+1]<- msw
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_IGET_OBJECT_QUICK: /* 0xf4 */
/* File: x86/OP_IGET_OBJECT_QUICK.S */
/* File: x86/OP_IGET_QUICK.S */
    /* For: iget-quick, iget-object-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_R  %ecx %ecx               # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    cmpl      $0,%ecx                  # is object null?
    je        common_errNullObject
    movl      (%ecx,%eax,1),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    andb      $0xf,rINSTbl             # rINST<- A
    SET_VREG  %eax rINST                # fp[A]<- result
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_QUICK: /* 0xf5 */
/* File: x86/OP_IPUT_QUICK.S */
    /* For: iput-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_R  %ecx %ecx               # vB (object we're operating on)
    andb      $0xf,rINSTbl             # rINST<- A
    GET_VREG_R  rINST,rINST             # rINST<- v[A]
    movzwl    2(rPC),%eax               # eax<- field byte offset
    testl     %ecx,%ecx                 # is object null?
    FETCH_INST_OPCODE 2 %edx
    je        common_errNullObject
    movl      rINST,(%ecx,%eax,1)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_WIDE_QUICK: /* 0xf6 */
/* File: x86/OP_IPUT_WIDE_QUICK.S */
    /* For: iput-wide-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_R  %ecx %ecx               # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    testl      %ecx,%ecx                # is object null?
    je        common_errNullObject
    leal      (%ecx,%eax,1),%ecx        # ecx<- Address of 64-bit target
    andb      $0xf,rINSTbl             # rINST<- A
    GET_VREG_WORD %eax rINST 0          # eax<- lsw
    GET_VREG_WORD rINST rINST 1         # rINST<- msw
    FETCH_INST_OPCODE 2 %edx
    movl      %eax,(%ecx)
    movl      rINST,4(%ecx)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_OBJECT_QUICK: /* 0xf7 */
/* File: x86/OP_IPUT_OBJECT_QUICK.S */
    /* For: iput-object-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINSTbl,%ecx              # ecx<- BA
    sarl      $4,%ecx                  # ecx<- B
    GET_VREG_R  %ecx %ecx               # vB (object we're operating on)
    andb      $0xf,rINSTbl             # rINST<- A
    GET_VREG_R  rINST rINST             # rINST<- v[A]
    movzwl    2(rPC),%eax               # eax<- field byte offset
    testl     %ecx,%ecx                 # is object null?
    je        common_errNullObject
    movl      rINST,(%ecx,%eax,1)
    movl      rSELF,%eax
    jmp       .LOP_IPUT_OBJECT_QUICK_finish

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_VIRTUAL_QUICK: /* 0xf8 */
/* File: x86/OP_INVOKE_VIRTUAL_QUICK.S */
    /*
     * Handle an optimized virtual method call.
     *
     * for: [opt] invoke-virtual-quick, invoke-virtual-quick/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movzwl    4(rPC),%eax               # eax<- FEDC or CCCC
    movzwl    2(rPC),%ecx               # ecx<- BBBB
    .if     (!0)
    andl      $0xf,%eax                # eax<- C (or stays CCCC)
    .endif
    GET_VREG_R  %eax %eax               # eax<- vC ("this" ptr)
    testl     %eax,%eax                 # null?
    je        common_errNullObject      # yep, throw exception
    movl      offObject_clazz(%eax),%eax # eax<- thisPtr->clazz
    movl      offClassObject_vtable(%eax),%eax # eax<- thisPtr->clazz->vtable
    EXPORT_PC                           # might throw later - get ready
    movl      (%eax,%ecx,4),%eax        # eax<- vtable[BBBB]
    jmp       common_invokeMethodNoRange

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_VIRTUAL_QUICK_RANGE: /* 0xf9 */
/* File: x86/OP_INVOKE_VIRTUAL_QUICK_RANGE.S */
/* File: x86/OP_INVOKE_VIRTUAL_QUICK.S */
    /*
     * Handle an optimized virtual method call.
     *
     * for: [opt] invoke-virtual-quick, invoke-virtual-quick/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movzwl    4(rPC),%eax               # eax<- FEDC or CCCC
    movzwl    2(rPC),%ecx               # ecx<- BBBB
    .if     (!1)
    andl      $0xf,%eax                # eax<- C (or stays CCCC)
    .endif
    GET_VREG_R  %eax %eax               # eax<- vC ("this" ptr)
    testl     %eax,%eax                 # null?
    je        common_errNullObject      # yep, throw exception
    movl      offObject_clazz(%eax),%eax # eax<- thisPtr->clazz
    movl      offClassObject_vtable(%eax),%eax # eax<- thisPtr->clazz->vtable
    EXPORT_PC                           # might throw later - get ready
    movl      (%eax,%ecx,4),%eax        # eax<- vtable[BBBB]
    jmp       common_invokeMethodRange


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_SUPER_QUICK: /* 0xfa */
/* File: x86/OP_INVOKE_SUPER_QUICK.S */
    /*
     * Handle an optimized "super" method call.
     *
     * for: [opt] invoke-super-quick, invoke-super-quick/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    4(rPC),%eax               # eax<- GFED or CCCC
    movl      offThread_method(%ecx),%ecx # ecx<- current method
    .if       (!0)
    andl      $0xf,%eax                # eax<- D (or stays CCCC)
    .endif
    movl      offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
    GET_VREG_R  %eax %eax               # eax<- "this"
    movl      offClassObject_super(%ecx),%ecx # ecx<- method->clazz->super
    testl     %eax,%eax                 # null "this"?
    je        common_errNullObject      # "this" is null, throw exception
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offClassObject_vtable(%ecx),%ecx # ecx<- vtable
    EXPORT_PC
    movl      (%ecx,%eax,4),%eax        # eax<- super->vtable[BBBB]
    jmp       common_invokeMethodNoRange

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_SUPER_QUICK_RANGE: /* 0xfb */
/* File: x86/OP_INVOKE_SUPER_QUICK_RANGE.S */
/* File: x86/OP_INVOKE_SUPER_QUICK.S */
    /*
     * Handle an optimized "super" method call.
     *
     * for: [opt] invoke-super-quick, invoke-super-quick/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op vAA, {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    4(rPC),%eax               # eax<- GFED or CCCC
    movl      offThread_method(%ecx),%ecx # ecx<- current method
    .if       (!1)
    andl      $0xf,%eax                # eax<- D (or stays CCCC)
    .endif
    movl      offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
    GET_VREG_R  %eax %eax               # eax<- "this"
    movl      offClassObject_super(%ecx),%ecx # ecx<- method->clazz->super
    testl     %eax,%eax                 # null "this"?
    je        common_errNullObject      # "this" is null, throw exception
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offClassObject_vtable(%ecx),%ecx # ecx<- vtable
    EXPORT_PC
    movl      (%ecx,%eax,4),%eax        # eax<- super->vtable[BBBB]
    jmp       common_invokeMethodRange


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_OBJECT_VOLATILE: /* 0xfc */
/* File: x86/OP_IPUT_OBJECT_VOLATILE.S */
/* File: x86/OP_IPUT_OBJECT.S */
    /*
     * Object field put.
     *
     * for: iput-object
     */
    /* op vA, vB, field@CCCC */
    movl    rSELF,%ecx
    movzwl  2(rPC),%edx                         # edx<- 0000CCCC
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzbl  rINSTbl,%ecx                        # ecx<- BA
    sarl    $4,%ecx                            # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    andb    $0xf,rINSTbl                       # rINST<- A
    GET_VREG_R %ecx %ecx                        # ecx<- fp[B], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_OBJECT_VOLATILE_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_OBJECT_VOLATILE_resolve


/* ------------------------------ */
    .balign 64
.L_OP_SGET_OBJECT_VOLATILE: /* 0xfd */
/* File: x86/OP_SGET_OBJECT_VOLATILE.S */
/* File: x86/OP_SGET.S */
    /*
     * General 32-bit SGET handler.
     *
     * for: sget, sget-object, sget-boolean, sget-byte, sget-char, sget-short
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_OBJECT_VOLATILE_resolve                # if not, make it so
.LOP_SGET_OBJECT_VOLATILE_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_OBJECT_VOLATILE: /* 0xfe */
/* File: x86/OP_SPUT_OBJECT_VOLATILE.S */
/* File: x86/OP_SPUT_OBJECT.S */
    /*
     * SPUT object handler.
     */
    /* op vAA, field@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_OBJECT_VOLATILE_resolve                # if not, make it so
.LOP_SPUT_OBJECT_VOLATILE_finish:                              # field ptr in eax
    movzbl    rINSTbl,%ecx                       # ecx<- AA
    GET_VREG_R  %ecx %ecx
    jmp       .LOP_SPUT_OBJECT_VOLATILE_continue


/* ------------------------------ */
    .balign 64
.L_OP_DISPATCH_FF: /* 0xff */
/* File: x86/OP_DISPATCH_FF.S */
    leal      256(rINST),%edx
    GOTO_NEXT_JUMBO_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CONST_CLASS_JUMBO: /* 0x100 */
/* File: x86/OP_CONST_CLASS_JUMBO.S */
    /* const-class/jumbo vBBBB, Class@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax              # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx# ecx<- self->methodClassDex
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- dvmDex->pResClasses
    movl      (%ecx,%eax,4),%eax       # eax<- rResClasses[AAAAAAAA]
    FETCH_INST_OPCODE 4 %edx
    testl     %eax,%eax                # resolved yet?
    je        .LOP_CONST_CLASS_JUMBO_resolve
    SET_VREG  %eax rINST               # vBBBB<- rResClasses[AAAAAAAA]
    ADVANCE_PC 4
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_CHECK_CAST_JUMBO: /* 0x101 */
/* File: x86/OP_CHECK_CAST_JUMBO.S */
    /*
     * Check to see if a cast from one class to another is allowed.
     */
    /* check-cast/jumbo vBBBB, class@AAAAAAAA */
    movl      rSELF,%ecx
    GET_VREG_R  rINST,rINST             # rINST<- vBBBB (object)
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    testl     rINST,rINST               # is oject null?
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    je        .LOP_CHECK_CAST_JUMBO_okay          # null obj, cast always succeeds
    movl      (%ecx,%eax,4),%eax        # eax<- resolved class
    movl      offObject_clazz(rINST),%ecx # ecx<- obj->clazz
    testl     %eax,%eax                 # have we resolved this before?
    je        .LOP_CHECK_CAST_JUMBO_resolve       # no, go do it now
.LOP_CHECK_CAST_JUMBO_resolved:
    cmpl      %eax,%ecx                 # same class (trivial success)?
    jne       .LOP_CHECK_CAST_JUMBO_fullcheck     # no, do full check
.LOP_CHECK_CAST_JUMBO_okay:
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_INSTANCE_OF_JUMBO: /* 0x102 */
/* File: x86/OP_INSTANCE_OF_JUMBO.S */
    /*
     * Check to see if an object reference is an instance of a class.
     *
     * Most common situation is a non-null object, being compared against
     * an already-resolved class.
     */
    /* instance-of/jumbo vBBBB, vCCCC, class@AAAAAAAA */
    movzwl  8(rPC),%eax                 # eax<- CCCC
    GET_VREG_R %eax %eax                # eax<- vCCCC (obj)
    movl    rSELF,%ecx
    testl   %eax,%eax                   # object null?
    movl    offThread_methodClassDex(%ecx),%ecx  # ecx<- pDvmDex
    je      .LOP_INSTANCE_OF_JUMBO_store           # null obj, not instance, store it
    movl    2(rPC),%edx                 # edx<- AAAAAAAA
    movl    offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    movl    (%ecx,%edx,4),%ecx          # ecx<- resolved class
    movl    offObject_clazz(%eax),%eax  # eax<- obj->clazz
    testl   %ecx,%ecx                   # have we resolved this before?
    je      .LOP_INSTANCE_OF_JUMBO_resolve         # not resolved, do it now
.LOP_INSTANCE_OF_JUMBO_resolved:  # eax<- obj->clazz, ecx<- resolved class
    cmpl    %eax,%ecx                   # same class (trivial success)?
    je      .LOP_INSTANCE_OF_JUMBO_trivial         # yes, trivial finish
    jmp     .LOP_INSTANCE_OF_JUMBO_fullcheck       # no, do full check

/* ------------------------------ */
    .balign 64
.L_OP_NEW_INSTANCE_JUMBO: /* 0x103 */
/* File: x86/OP_NEW_INSTANCE_JUMBO.S */
    /*
     * Create a new instance of a class.
     */
    /* new-instance/jumbo vBBBB, class@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- pDvmDex
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    EXPORT_PC
    movl      (%ecx,%eax,4),%ecx        # ecx<- resolved class
    testl     %ecx,%ecx                 # resolved?
    je        .LOP_NEW_INSTANCE_JUMBO_resolve       # no, go do it
.LOP_NEW_INSTANCE_JUMBO_resolved:  # on entry, ecx<- class
    cmpb      $CLASS_INITIALIZED,offClassObject_status(%ecx)
    je        .LOP_NEW_INSTANCE_JUMBO_initialized
    jmp       .LOP_NEW_INSTANCE_JUMBO_needinit

/* ------------------------------ */
    .balign 64
.L_OP_NEW_ARRAY_JUMBO: /* 0x104 */
/* File: x86/OP_NEW_ARRAY_JUMBO.S */
    /*
     * Allocate an array of objects, specified with the array class
     * and a count.
     *
     * The verifier guarantees that this is an array class, so we don't
     * check for it here.
     */
    /* new-array/jumbo vBBBB, vCCCC, class@AAAAAAAA */
    movl    rSELF,%ecx
    EXPORT_PC
    movl    offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    movl    2(rPC),%eax                       # eax<- AAAAAAAA
    movl    offDvmDex_pResClasses(%ecx),%ecx  # ecx<- pDvmDex->pResClasses
    movl    (%ecx,%eax,4),%ecx                # ecx<- resolved class
    movzwl  8(rPC),%eax                       # eax<- CCCC
    GET_VREG_R %eax %eax                      # eax<- vCCCC (array length)
    testl   %eax,%eax
    js      common_errNegativeArraySize       # bail, passing len in eax
    testl   %ecx,%ecx                         # already resolved?
    jne     .LOP_NEW_ARRAY_JUMBO_finish                # yes, fast path
    jmp     .LOP_NEW_ARRAY_JUMBO_resolve               # resolve now

/* ------------------------------ */
    .balign 64
.L_OP_FILLED_NEW_ARRAY_JUMBO: /* 0x105 */
/* File: x86/OP_FILLED_NEW_ARRAY_JUMBO.S */
    /*
     * Create a new array with elements filled from registers.
     */
    /* filled-new-array/jumbo {vCCCC..v(CCCC+BBBB-1)}, type@AAAAAAAA */
    movl    rSELF,%eax
    movl    offThread_methodClassDex(%eax),%eax # eax<- pDvmDex
    movl    2(rPC),%ecx                       # ecx<- AAAAAAAA
    movl    offDvmDex_pResClasses(%eax),%eax  # eax<- pDvmDex->pResClasses
    movl    (%eax,%ecx,4),%eax                # eax<- resolved class
    EXPORT_PC
    testl   %eax,%eax                         # already resolved?
    jne     .LOP_FILLED_NEW_ARRAY_JUMBO_continue              # yes, continue
    # less frequent path, so we'll redo some work
    movl    rSELF,%eax
    movl    $0,OUT_ARG2(%esp)                # arg2<- false
    movl    %ecx,OUT_ARG1(%esp)               # arg1<- AAAAAAAA
    movl    offThread_method(%eax),%eax         # eax<- self->method
    jmp     .LOP_FILLED_NEW_ARRAY_JUMBO_more

/* ------------------------------ */
    .balign 64
.L_OP_IGET_JUMBO: /* 0x106 */
/* File: x86/OP_IGET_JUMBO.S */
    /*
     * Jumbo 32-bit instance field get.
     *
     * for: iget/jumbo, iget-object/jumbo, iget-boolean/jumbo, iget-byte/jumbo,
     *      iget-char/jumbo, iget-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_JUMBO_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IGET_WIDE_JUMBO: /* 0x107 */
/* File: x86/OP_IGET_WIDE_JUMBO.S */
    /*
     * Jumbo 64-bit instance field get.
     */
    /* iget-wide/jumbo vBBBB, vCCCC, field@AAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_WIDE_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # for dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_WIDE_JUMBO_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IGET_OBJECT_JUMBO: /* 0x108 */
/* File: x86/OP_IGET_OBJECT_JUMBO.S */
/* File: x86/OP_IGET_JUMBO.S */
    /*
     * Jumbo 32-bit instance field get.
     *
     * for: iget/jumbo, iget-object/jumbo, iget-boolean/jumbo, iget-byte/jumbo,
     *      iget-char/jumbo, iget-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_OBJECT_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_OBJECT_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_BOOLEAN_JUMBO: /* 0x109 */
/* File: x86/OP_IGET_BOOLEAN_JUMBO.S */
/* File: x86/OP_IGET_JUMBO.S */
    /*
     * Jumbo 32-bit instance field get.
     *
     * for: iget/jumbo, iget-object/jumbo, iget-boolean/jumbo, iget-byte/jumbo,
     *      iget-char/jumbo, iget-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_BOOLEAN_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_BOOLEAN_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_BYTE_JUMBO: /* 0x10a */
/* File: x86/OP_IGET_BYTE_JUMBO.S */
/* File: x86/OP_IGET_JUMBO.S */
    /*
     * Jumbo 32-bit instance field get.
     *
     * for: iget/jumbo, iget-object/jumbo, iget-boolean/jumbo, iget-byte/jumbo,
     *      iget-char/jumbo, iget-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_BYTE_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_BYTE_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_CHAR_JUMBO: /* 0x10b */
/* File: x86/OP_IGET_CHAR_JUMBO.S */
/* File: x86/OP_IGET_JUMBO.S */
    /*
     * Jumbo 32-bit instance field get.
     *
     * for: iget/jumbo, iget-object/jumbo, iget-boolean/jumbo, iget-byte/jumbo,
     *      iget-char/jumbo, iget-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_CHAR_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_CHAR_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IGET_SHORT_JUMBO: /* 0x10c */
/* File: x86/OP_IGET_SHORT_JUMBO.S */
/* File: x86/OP_IGET_JUMBO.S */
    /*
     * Jumbo 32-bit instance field get.
     *
     * for: iget/jumbo, iget-object/jumbo, iget-boolean/jumbo, iget-byte/jumbo,
     *      iget-char/jumbo, iget-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IGET_SHORT_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)                 # needed by dvmResolveInstField
    movl    rSELF,%edx
    jmp     .LOP_IGET_SHORT_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_JUMBO: /* 0x10d */
/* File: x86/OP_IPUT_JUMBO.S */
    /*
     * Jumbo 32-bit instance field put.
     *
     * for: iput/jumbo, iput-object/jumbo, iput-boolean/jumbo, iput-byte/jumbo,
            iput-char/jumbo, iput-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_JUMBO_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_WIDE_JUMBO: /* 0x10e */
/* File: x86/OP_IPUT_WIDE_JUMBO.S */
    /*
     * Jumbo 64-bit instance field put.
     */
    /* iput-wide/jumbo vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_WIDE_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_WIDE_JUMBO_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_OBJECT_JUMBO: /* 0x10f */
/* File: x86/OP_IPUT_OBJECT_JUMBO.S */
    /*
     * Jumbo object field put.
     */
    /* iput-object/jumbo vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_OBJECT_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_OBJECT_JUMBO_resolve

/* ------------------------------ */
    .balign 64
.L_OP_IPUT_BOOLEAN_JUMBO: /* 0x110 */
/* File: x86/OP_IPUT_BOOLEAN_JUMBO.S */
/* File: x86/OP_IPUT_JUMBO.S */
    /*
     * Jumbo 32-bit instance field put.
     *
     * for: iput/jumbo, iput-object/jumbo, iput-boolean/jumbo, iput-byte/jumbo,
            iput-char/jumbo, iput-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_BOOLEAN_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_BOOLEAN_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_BYTE_JUMBO: /* 0x111 */
/* File: x86/OP_IPUT_BYTE_JUMBO.S */
/* File: x86/OP_IPUT_JUMBO.S */
    /*
     * Jumbo 32-bit instance field put.
     *
     * for: iput/jumbo, iput-object/jumbo, iput-boolean/jumbo, iput-byte/jumbo,
            iput-char/jumbo, iput-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_BYTE_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_BYTE_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_CHAR_JUMBO: /* 0x112 */
/* File: x86/OP_IPUT_CHAR_JUMBO.S */
/* File: x86/OP_IPUT_JUMBO.S */
    /*
     * Jumbo 32-bit instance field put.
     *
     * for: iput/jumbo, iput-object/jumbo, iput-boolean/jumbo, iput-byte/jumbo,
            iput-char/jumbo, iput-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_CHAR_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_CHAR_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_IPUT_SHORT_JUMBO: /* 0x113 */
/* File: x86/OP_IPUT_SHORT_JUMBO.S */
/* File: x86/OP_IPUT_JUMBO.S */
    /*
     * Jumbo 32-bit instance field put.
     *
     * for: iput/jumbo, iput-object/jumbo, iput-boolean/jumbo, iput-byte/jumbo,
            iput-char/jumbo, iput-short/jumbo
     */
    /* exop vBBBB, vCCCC, field@AAAAAAAA */
    movl    rSELF,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .LOP_IPUT_SHORT_JUMBO_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rSELF,%edx
    jmp     .LOP_IPUT_SHORT_JUMBO_resolve


/* ------------------------------ */
    .balign 64
.L_OP_SGET_JUMBO: /* 0x114 */
/* File: x86/OP_SGET_JUMBO.S */
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_JUMBO_resolve                # if not, make it so
.LOP_SGET_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SGET_WIDE_JUMBO: /* 0x115 */
/* File: x86/OP_SGET_WIDE_JUMBO.S */
    /*
     * Jumbo 64-bit SGET handler.
     *
     */
    /* sget-wide/jumbo vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_WIDE_JUMBO_resolve                # if not, make it so
.LOP_SGET_WIDE_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%ecx    # ecx<- lsw
    movl      4+offStaticField_value(%eax),%eax  # eax<- msw
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG_WORD %ecx rINST 0
    SET_VREG_WORD %eax rINST 1
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SGET_OBJECT_JUMBO: /* 0x116 */
/* File: x86/OP_SGET_OBJECT_JUMBO.S */
/* File: x86/OP_SGET_JUMBO.S */
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_OBJECT_JUMBO_resolve                # if not, make it so
.LOP_SGET_OBJECT_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_BOOLEAN_JUMBO: /* 0x117 */
/* File: x86/OP_SGET_BOOLEAN_JUMBO.S */
/* File: x86/OP_SGET_JUMBO.S */
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_BOOLEAN_JUMBO_resolve                # if not, make it so
.LOP_SGET_BOOLEAN_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_BYTE_JUMBO: /* 0x118 */
/* File: x86/OP_SGET_BYTE_JUMBO.S */
/* File: x86/OP_SGET_JUMBO.S */
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_BYTE_JUMBO_resolve                # if not, make it so
.LOP_SGET_BYTE_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_CHAR_JUMBO: /* 0x119 */
/* File: x86/OP_SGET_CHAR_JUMBO.S */
/* File: x86/OP_SGET_JUMBO.S */
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_CHAR_JUMBO_resolve                # if not, make it so
.LOP_SGET_CHAR_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SGET_SHORT_JUMBO: /* 0x11a */
/* File: x86/OP_SGET_SHORT_JUMBO.S */
/* File: x86/OP_SGET_JUMBO.S */
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SGET_SHORT_JUMBO_resolve                # if not, make it so
.LOP_SGET_SHORT_JUMBO_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_JUMBO: /* 0x11b */
/* File: x86/OP_SPUT_JUMBO.S */
    /*
     * Jumbo 32-bit SPUT handler.
     *
     * for: sput/jumbo, sput-boolean/jumbo, sput-byte/jumbo, sput-char/jumbo,
     *      sput-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_JUMBO_resolve                # if not, make it so
.LOP_SPUT_JUMBO_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SPUT_WIDE_JUMBO: /* 0x11c */
/* File: x86/OP_SPUT_WIDE_JUMBO.S */
    /*
     * Jumbo 64-bit SPUT handler.
     */
    /* sput-wide/jumbo vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_WIDE_JUMBO_resolve                # if not, make it so
.LOP_SPUT_WIDE_JUMBO_finish:     # field ptr in eax
    GET_VREG_WORD %ecx rINST 0                  # ecx<- lsw
    GET_VREG_WORD rINST rINST 1                 # rINST<- msw
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    movl      %ecx,offStaticField_value(%eax)
    movl      rINST,4+offStaticField_value(%eax)
    GOTO_NEXT_R %edx

/* ------------------------------ */
    .balign 64
.L_OP_SPUT_OBJECT_JUMBO: /* 0x11d */
/* File: x86/OP_SPUT_OBJECT_JUMBO.S */
    /*
     * Jumbo SPUT object handler.
     */
    /* sput-object/jumbo vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_OBJECT_JUMBO_resolve                # if not, make it so
.LOP_SPUT_OBJECT_JUMBO_finish:                              # field ptr in eax
    GET_VREG_R  %ecx rINST
    jmp       .LOP_SPUT_OBJECT_JUMBO_continue

/* ------------------------------ */
    .balign 64
.L_OP_SPUT_BOOLEAN_JUMBO: /* 0x11e */
/* File: x86/OP_SPUT_BOOLEAN_JUMBO.S */
/* File: x86/OP_SPUT_JUMBO.S */
    /*
     * Jumbo 32-bit SPUT handler.
     *
     * for: sput/jumbo, sput-boolean/jumbo, sput-byte/jumbo, sput-char/jumbo,
     *      sput-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_BOOLEAN_JUMBO_resolve                # if not, make it so
.LOP_SPUT_BOOLEAN_JUMBO_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_BYTE_JUMBO: /* 0x11f */
/* File: x86/OP_SPUT_BYTE_JUMBO.S */
/* File: x86/OP_SPUT_JUMBO.S */
    /*
     * Jumbo 32-bit SPUT handler.
     *
     * for: sput/jumbo, sput-boolean/jumbo, sput-byte/jumbo, sput-char/jumbo,
     *      sput-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_BYTE_JUMBO_resolve                # if not, make it so
.LOP_SPUT_BYTE_JUMBO_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_CHAR_JUMBO: /* 0x120 */
/* File: x86/OP_SPUT_CHAR_JUMBO.S */
/* File: x86/OP_SPUT_JUMBO.S */
    /*
     * Jumbo 32-bit SPUT handler.
     *
     * for: sput/jumbo, sput-boolean/jumbo, sput-byte/jumbo, sput-char/jumbo,
     *      sput-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_CHAR_JUMBO_resolve                # if not, make it so
.LOP_SPUT_CHAR_JUMBO_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_SPUT_SHORT_JUMBO: /* 0x121 */
/* File: x86/OP_SPUT_SHORT_JUMBO.S */
/* File: x86/OP_SPUT_JUMBO.S */
    /*
     * Jumbo 32-bit SPUT handler.
     *
     * for: sput/jumbo, sput-boolean/jumbo, sput-byte/jumbo, sput-char/jumbo,
     *      sput-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .LOP_SPUT_SHORT_JUMBO_resolve                # if not, make it so
.LOP_SPUT_SHORT_JUMBO_finish:     # field ptr in eax
    GET_VREG_R  %ecx rINST
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    movl      %ecx,offStaticField_value(%eax)
    GOTO_NEXT_R %edx


/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_VIRTUAL_JUMBO: /* 0x122 */
/* File: x86/OP_INVOKE_VIRTUAL_JUMBO.S */
    /*
     * Handle a jumbo virtual method call.
     */
    /* invoke-virtual/jumbo vBBBB, {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movl      rSELF,%eax
    movl      2(rPC),%ecx                 # ecx<- AAAAAAAA
    movl      offThread_methodClassDex(%eax),%eax  # eax<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%eax),%eax   # eax<- pDvmDex->pResMethods
    movl      (%eax,%ecx,4),%eax          # eax<- resolved baseMethod
    testl     %eax,%eax                   # already resolved?
    jne       .LOP_INVOKE_VIRTUAL_JUMBO_continue        # yes, continue
    movl      rSELF,%eax
    movl      %ecx,OUT_ARG1(%esp)         # arg1<- ref
    movl      offThread_method(%eax),%eax   # eax<- self->method
    jmp       .LOP_INVOKE_VIRTUAL_JUMBO_more

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_SUPER_JUMBO: /* 0x123 */
/* File: x86/OP_INVOKE_SUPER_JUMBO.S */
    /*
     * Handle a jumbo "super" method call.
     */
    /* invoke-super/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movl      rSELF,rINST
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offThread_methodClassDex(rINST),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%ecx        # ecx<- resolved baseMethod
    movl      offThread_method(rINST),%eax # eax<- method
    movzwl    8(rPC),rINST              # rINST<- CCCC
    GET_VREG_R  rINST rINST             # rINST<- "this" ptr
    testl     rINST,rINST               # null "this"?
    je        common_errNullObject      # yes, throw
    movl      offMethod_clazz(%eax),%eax # eax<- method->clazz
    testl     %ecx,%ecx                 # already resolved?
    jne       .LOP_INVOKE_SUPER_JUMBO_continue      # yes - go on
    jmp       .LOP_INVOKE_SUPER_JUMBO_resolve

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_DIRECT_JUMBO: /* 0x124 */
/* File: x86/OP_INVOKE_DIRECT_JUMBO.S */
    /*
     * Handle a jumbo direct method call.
     *
     * (We could defer the "is 'this' pointer null" test to the common
     * method invocation code, and use a flag to indicate that static
     * calls don't count.  If we do this as part of copying the arguments
     * out we could avoiding loading the first arg twice.)
     */
    /* invoke-direct/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax              # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movzwl    8(rPC),%edx              # edx<- CCCC
    movl      (%ecx,%eax,4),%eax       # eax<- resolved methodToCall
    testl     %eax,%eax                # already resolved?
    GET_VREG_R  %ecx %edx              # ecx<- "this" ptr
    je        .LOP_INVOKE_DIRECT_JUMBO_resolve      # not resolved, do it now
.LOP_INVOKE_DIRECT_JUMBO_finish:
    testl     %ecx,%ecx                # null "this"?
    jne       common_invokeMethodJumbo # no, continue on
    jmp       common_errNullObject

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_STATIC_JUMBO: /* 0x125 */
/* File: x86/OP_INVOKE_STATIC_JUMBO.S */
    /*
     * Handle a jumbo static method call.
     */
    /* invoke-static/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%eax        # eax<- resolved methodToCall
    testl     %eax,%eax
    jne       common_invokeMethodJumbo
    movl      rSELF,%ecx
    movl      offThread_method(%ecx),%ecx # ecx<- self->method
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offMethod_clazz(%ecx),%ecx# ecx<- method->clazz
    movl      %eax,OUT_ARG1(%esp)       # arg1<- AAAAAAAA
    movl      %ecx,OUT_ARG0(%esp)       # arg0<- clazz
    jmp       .LOP_INVOKE_STATIC_JUMBO_continue

/* ------------------------------ */
    .balign 64
.L_OP_INVOKE_INTERFACE_JUMBO: /* 0x126 */
/* File: x86/OP_INVOKE_INTERFACE_JUMBO.S */
    /*
     * Handle a jumbo interface method call.
     */
    /* invoke-interface/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movzwl     8(rPC),%eax              # eax<- CCCC
    movl       rSELF,%ecx
    GET_VREG_R   %eax %eax              # eax<- "this"
    EXPORT_PC
    testl      %eax,%eax                # null this?
    je         common_errNullObject     # yes, fail
    movl       offObject_clazz(%eax),%eax# eax<- thisPtr->clazz
    movl       %eax,OUT_ARG0(%esp)                 # arg0<- class
    movl       offThread_methodClassDex(%ecx),%eax   # eax<- methodClassDex
    movl       offThread_method(%ecx),%ecx           # ecx<- method
    movl       %eax,OUT_ARG3(%esp)                 # arg3<- dex
    movl       2(rPC),%eax                         # eax<- AAAAAAAA
    movl       %ecx,OUT_ARG2(%esp)                 # arg2<- method
    movl       %eax,OUT_ARG1(%esp)                 # arg1<- AAAAAAAA
    jmp        .LOP_INVOKE_INTERFACE_JUMBO_continue

/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_27FF: /* 0x127 */
/* File: x86/OP_UNUSED_27FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_28FF: /* 0x128 */
/* File: x86/OP_UNUSED_28FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_29FF: /* 0x129 */
/* File: x86/OP_UNUSED_29FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_2AFF: /* 0x12a */
/* File: x86/OP_UNUSED_2AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_2BFF: /* 0x12b */
/* File: x86/OP_UNUSED_2BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_2CFF: /* 0x12c */
/* File: x86/OP_UNUSED_2CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_2DFF: /* 0x12d */
/* File: x86/OP_UNUSED_2DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_2EFF: /* 0x12e */
/* File: x86/OP_UNUSED_2EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_2FFF: /* 0x12f */
/* File: x86/OP_UNUSED_2FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_30FF: /* 0x130 */
/* File: x86/OP_UNUSED_30FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_31FF: /* 0x131 */
/* File: x86/OP_UNUSED_31FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_32FF: /* 0x132 */
/* File: x86/OP_UNUSED_32FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_33FF: /* 0x133 */
/* File: x86/OP_UNUSED_33FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_34FF: /* 0x134 */
/* File: x86/OP_UNUSED_34FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_35FF: /* 0x135 */
/* File: x86/OP_UNUSED_35FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_36FF: /* 0x136 */
/* File: x86/OP_UNUSED_36FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_37FF: /* 0x137 */
/* File: x86/OP_UNUSED_37FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_38FF: /* 0x138 */
/* File: x86/OP_UNUSED_38FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_39FF: /* 0x139 */
/* File: x86/OP_UNUSED_39FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3AFF: /* 0x13a */
/* File: x86/OP_UNUSED_3AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3BFF: /* 0x13b */
/* File: x86/OP_UNUSED_3BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3CFF: /* 0x13c */
/* File: x86/OP_UNUSED_3CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3DFF: /* 0x13d */
/* File: x86/OP_UNUSED_3DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3EFF: /* 0x13e */
/* File: x86/OP_UNUSED_3EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_3FFF: /* 0x13f */
/* File: x86/OP_UNUSED_3FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_40FF: /* 0x140 */
/* File: x86/OP_UNUSED_40FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_41FF: /* 0x141 */
/* File: x86/OP_UNUSED_41FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_42FF: /* 0x142 */
/* File: x86/OP_UNUSED_42FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_43FF: /* 0x143 */
/* File: x86/OP_UNUSED_43FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_44FF: /* 0x144 */
/* File: x86/OP_UNUSED_44FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_45FF: /* 0x145 */
/* File: x86/OP_UNUSED_45FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_46FF: /* 0x146 */
/* File: x86/OP_UNUSED_46FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_47FF: /* 0x147 */
/* File: x86/OP_UNUSED_47FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_48FF: /* 0x148 */
/* File: x86/OP_UNUSED_48FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_49FF: /* 0x149 */
/* File: x86/OP_UNUSED_49FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_4AFF: /* 0x14a */
/* File: x86/OP_UNUSED_4AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_4BFF: /* 0x14b */
/* File: x86/OP_UNUSED_4BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_4CFF: /* 0x14c */
/* File: x86/OP_UNUSED_4CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_4DFF: /* 0x14d */
/* File: x86/OP_UNUSED_4DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_4EFF: /* 0x14e */
/* File: x86/OP_UNUSED_4EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_4FFF: /* 0x14f */
/* File: x86/OP_UNUSED_4FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_50FF: /* 0x150 */
/* File: x86/OP_UNUSED_50FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_51FF: /* 0x151 */
/* File: x86/OP_UNUSED_51FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_52FF: /* 0x152 */
/* File: x86/OP_UNUSED_52FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_53FF: /* 0x153 */
/* File: x86/OP_UNUSED_53FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_54FF: /* 0x154 */
/* File: x86/OP_UNUSED_54FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_55FF: /* 0x155 */
/* File: x86/OP_UNUSED_55FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_56FF: /* 0x156 */
/* File: x86/OP_UNUSED_56FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_57FF: /* 0x157 */
/* File: x86/OP_UNUSED_57FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_58FF: /* 0x158 */
/* File: x86/OP_UNUSED_58FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_59FF: /* 0x159 */
/* File: x86/OP_UNUSED_59FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_5AFF: /* 0x15a */
/* File: x86/OP_UNUSED_5AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_5BFF: /* 0x15b */
/* File: x86/OP_UNUSED_5BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_5CFF: /* 0x15c */
/* File: x86/OP_UNUSED_5CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_5DFF: /* 0x15d */
/* File: x86/OP_UNUSED_5DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_5EFF: /* 0x15e */
/* File: x86/OP_UNUSED_5EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_5FFF: /* 0x15f */
/* File: x86/OP_UNUSED_5FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_60FF: /* 0x160 */
/* File: x86/OP_UNUSED_60FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_61FF: /* 0x161 */
/* File: x86/OP_UNUSED_61FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_62FF: /* 0x162 */
/* File: x86/OP_UNUSED_62FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_63FF: /* 0x163 */
/* File: x86/OP_UNUSED_63FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_64FF: /* 0x164 */
/* File: x86/OP_UNUSED_64FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_65FF: /* 0x165 */
/* File: x86/OP_UNUSED_65FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_66FF: /* 0x166 */
/* File: x86/OP_UNUSED_66FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_67FF: /* 0x167 */
/* File: x86/OP_UNUSED_67FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_68FF: /* 0x168 */
/* File: x86/OP_UNUSED_68FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_69FF: /* 0x169 */
/* File: x86/OP_UNUSED_69FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_6AFF: /* 0x16a */
/* File: x86/OP_UNUSED_6AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_6BFF: /* 0x16b */
/* File: x86/OP_UNUSED_6BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_6CFF: /* 0x16c */
/* File: x86/OP_UNUSED_6CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_6DFF: /* 0x16d */
/* File: x86/OP_UNUSED_6DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_6EFF: /* 0x16e */
/* File: x86/OP_UNUSED_6EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_6FFF: /* 0x16f */
/* File: x86/OP_UNUSED_6FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_70FF: /* 0x170 */
/* File: x86/OP_UNUSED_70FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_71FF: /* 0x171 */
/* File: x86/OP_UNUSED_71FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_72FF: /* 0x172 */
/* File: x86/OP_UNUSED_72FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_73FF: /* 0x173 */
/* File: x86/OP_UNUSED_73FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_74FF: /* 0x174 */
/* File: x86/OP_UNUSED_74FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_75FF: /* 0x175 */
/* File: x86/OP_UNUSED_75FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_76FF: /* 0x176 */
/* File: x86/OP_UNUSED_76FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_77FF: /* 0x177 */
/* File: x86/OP_UNUSED_77FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_78FF: /* 0x178 */
/* File: x86/OP_UNUSED_78FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_79FF: /* 0x179 */
/* File: x86/OP_UNUSED_79FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7AFF: /* 0x17a */
/* File: x86/OP_UNUSED_7AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7BFF: /* 0x17b */
/* File: x86/OP_UNUSED_7BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7CFF: /* 0x17c */
/* File: x86/OP_UNUSED_7CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7DFF: /* 0x17d */
/* File: x86/OP_UNUSED_7DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7EFF: /* 0x17e */
/* File: x86/OP_UNUSED_7EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_7FFF: /* 0x17f */
/* File: x86/OP_UNUSED_7FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_80FF: /* 0x180 */
/* File: x86/OP_UNUSED_80FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_81FF: /* 0x181 */
/* File: x86/OP_UNUSED_81FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_82FF: /* 0x182 */
/* File: x86/OP_UNUSED_82FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_83FF: /* 0x183 */
/* File: x86/OP_UNUSED_83FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_84FF: /* 0x184 */
/* File: x86/OP_UNUSED_84FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_85FF: /* 0x185 */
/* File: x86/OP_UNUSED_85FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_86FF: /* 0x186 */
/* File: x86/OP_UNUSED_86FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_87FF: /* 0x187 */
/* File: x86/OP_UNUSED_87FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_88FF: /* 0x188 */
/* File: x86/OP_UNUSED_88FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_89FF: /* 0x189 */
/* File: x86/OP_UNUSED_89FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_8AFF: /* 0x18a */
/* File: x86/OP_UNUSED_8AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_8BFF: /* 0x18b */
/* File: x86/OP_UNUSED_8BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_8CFF: /* 0x18c */
/* File: x86/OP_UNUSED_8CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_8DFF: /* 0x18d */
/* File: x86/OP_UNUSED_8DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_8EFF: /* 0x18e */
/* File: x86/OP_UNUSED_8EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_8FFF: /* 0x18f */
/* File: x86/OP_UNUSED_8FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_90FF: /* 0x190 */
/* File: x86/OP_UNUSED_90FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_91FF: /* 0x191 */
/* File: x86/OP_UNUSED_91FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_92FF: /* 0x192 */
/* File: x86/OP_UNUSED_92FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_93FF: /* 0x193 */
/* File: x86/OP_UNUSED_93FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_94FF: /* 0x194 */
/* File: x86/OP_UNUSED_94FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_95FF: /* 0x195 */
/* File: x86/OP_UNUSED_95FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_96FF: /* 0x196 */
/* File: x86/OP_UNUSED_96FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_97FF: /* 0x197 */
/* File: x86/OP_UNUSED_97FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_98FF: /* 0x198 */
/* File: x86/OP_UNUSED_98FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_99FF: /* 0x199 */
/* File: x86/OP_UNUSED_99FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_9AFF: /* 0x19a */
/* File: x86/OP_UNUSED_9AFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_9BFF: /* 0x19b */
/* File: x86/OP_UNUSED_9BFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_9CFF: /* 0x19c */
/* File: x86/OP_UNUSED_9CFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_9DFF: /* 0x19d */
/* File: x86/OP_UNUSED_9DFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_9EFF: /* 0x19e */
/* File: x86/OP_UNUSED_9EFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_9FFF: /* 0x19f */
/* File: x86/OP_UNUSED_9FFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A0FF: /* 0x1a0 */
/* File: x86/OP_UNUSED_A0FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A1FF: /* 0x1a1 */
/* File: x86/OP_UNUSED_A1FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A2FF: /* 0x1a2 */
/* File: x86/OP_UNUSED_A2FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A3FF: /* 0x1a3 */
/* File: x86/OP_UNUSED_A3FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A4FF: /* 0x1a4 */
/* File: x86/OP_UNUSED_A4FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A5FF: /* 0x1a5 */
/* File: x86/OP_UNUSED_A5FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A6FF: /* 0x1a6 */
/* File: x86/OP_UNUSED_A6FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A7FF: /* 0x1a7 */
/* File: x86/OP_UNUSED_A7FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A8FF: /* 0x1a8 */
/* File: x86/OP_UNUSED_A8FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_A9FF: /* 0x1a9 */
/* File: x86/OP_UNUSED_A9FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_AAFF: /* 0x1aa */
/* File: x86/OP_UNUSED_AAFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_ABFF: /* 0x1ab */
/* File: x86/OP_UNUSED_ABFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_ACFF: /* 0x1ac */
/* File: x86/OP_UNUSED_ACFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_ADFF: /* 0x1ad */
/* File: x86/OP_UNUSED_ADFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_AEFF: /* 0x1ae */
/* File: x86/OP_UNUSED_AEFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_AFFF: /* 0x1af */
/* File: x86/OP_UNUSED_AFFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B0FF: /* 0x1b0 */
/* File: x86/OP_UNUSED_B0FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B1FF: /* 0x1b1 */
/* File: x86/OP_UNUSED_B1FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B2FF: /* 0x1b2 */
/* File: x86/OP_UNUSED_B2FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B3FF: /* 0x1b3 */
/* File: x86/OP_UNUSED_B3FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B4FF: /* 0x1b4 */
/* File: x86/OP_UNUSED_B4FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B5FF: /* 0x1b5 */
/* File: x86/OP_UNUSED_B5FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B6FF: /* 0x1b6 */
/* File: x86/OP_UNUSED_B6FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B7FF: /* 0x1b7 */
/* File: x86/OP_UNUSED_B7FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B8FF: /* 0x1b8 */
/* File: x86/OP_UNUSED_B8FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_B9FF: /* 0x1b9 */
/* File: x86/OP_UNUSED_B9FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_BAFF: /* 0x1ba */
/* File: x86/OP_UNUSED_BAFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_BBFF: /* 0x1bb */
/* File: x86/OP_UNUSED_BBFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_BCFF: /* 0x1bc */
/* File: x86/OP_UNUSED_BCFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_BDFF: /* 0x1bd */
/* File: x86/OP_UNUSED_BDFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_BEFF: /* 0x1be */
/* File: x86/OP_UNUSED_BEFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_BFFF: /* 0x1bf */
/* File: x86/OP_UNUSED_BFFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C0FF: /* 0x1c0 */
/* File: x86/OP_UNUSED_C0FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C1FF: /* 0x1c1 */
/* File: x86/OP_UNUSED_C1FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C2FF: /* 0x1c2 */
/* File: x86/OP_UNUSED_C2FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C3FF: /* 0x1c3 */
/* File: x86/OP_UNUSED_C3FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C4FF: /* 0x1c4 */
/* File: x86/OP_UNUSED_C4FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C5FF: /* 0x1c5 */
/* File: x86/OP_UNUSED_C5FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C6FF: /* 0x1c6 */
/* File: x86/OP_UNUSED_C6FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C7FF: /* 0x1c7 */
/* File: x86/OP_UNUSED_C7FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C8FF: /* 0x1c8 */
/* File: x86/OP_UNUSED_C8FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_C9FF: /* 0x1c9 */
/* File: x86/OP_UNUSED_C9FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_CAFF: /* 0x1ca */
/* File: x86/OP_UNUSED_CAFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_CBFF: /* 0x1cb */
/* File: x86/OP_UNUSED_CBFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_CCFF: /* 0x1cc */
/* File: x86/OP_UNUSED_CCFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_CDFF: /* 0x1cd */
/* File: x86/OP_UNUSED_CDFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_CEFF: /* 0x1ce */
/* File: x86/OP_UNUSED_CEFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_CFFF: /* 0x1cf */
/* File: x86/OP_UNUSED_CFFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D0FF: /* 0x1d0 */
/* File: x86/OP_UNUSED_D0FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D1FF: /* 0x1d1 */
/* File: x86/OP_UNUSED_D1FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D2FF: /* 0x1d2 */
/* File: x86/OP_UNUSED_D2FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D3FF: /* 0x1d3 */
/* File: x86/OP_UNUSED_D3FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D4FF: /* 0x1d4 */
/* File: x86/OP_UNUSED_D4FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D5FF: /* 0x1d5 */
/* File: x86/OP_UNUSED_D5FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D6FF: /* 0x1d6 */
/* File: x86/OP_UNUSED_D6FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D7FF: /* 0x1d7 */
/* File: x86/OP_UNUSED_D7FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D8FF: /* 0x1d8 */
/* File: x86/OP_UNUSED_D8FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_D9FF: /* 0x1d9 */
/* File: x86/OP_UNUSED_D9FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_DAFF: /* 0x1da */
/* File: x86/OP_UNUSED_DAFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_DBFF: /* 0x1db */
/* File: x86/OP_UNUSED_DBFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_DCFF: /* 0x1dc */
/* File: x86/OP_UNUSED_DCFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_DDFF: /* 0x1dd */
/* File: x86/OP_UNUSED_DDFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_DEFF: /* 0x1de */
/* File: x86/OP_UNUSED_DEFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_DFFF: /* 0x1df */
/* File: x86/OP_UNUSED_DFFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E0FF: /* 0x1e0 */
/* File: x86/OP_UNUSED_E0FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E1FF: /* 0x1e1 */
/* File: x86/OP_UNUSED_E1FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E2FF: /* 0x1e2 */
/* File: x86/OP_UNUSED_E2FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E3FF: /* 0x1e3 */
/* File: x86/OP_UNUSED_E3FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E4FF: /* 0x1e4 */
/* File: x86/OP_UNUSED_E4FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E5FF: /* 0x1e5 */
/* File: x86/OP_UNUSED_E5FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E6FF: /* 0x1e6 */
/* File: x86/OP_UNUSED_E6FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E7FF: /* 0x1e7 */
/* File: x86/OP_UNUSED_E7FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E8FF: /* 0x1e8 */
/* File: x86/OP_UNUSED_E8FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_E9FF: /* 0x1e9 */
/* File: x86/OP_UNUSED_E9FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_EAFF: /* 0x1ea */
/* File: x86/OP_UNUSED_EAFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_EBFF: /* 0x1eb */
/* File: x86/OP_UNUSED_EBFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_ECFF: /* 0x1ec */
/* File: x86/OP_UNUSED_ECFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_EDFF: /* 0x1ed */
/* File: x86/OP_UNUSED_EDFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_EEFF: /* 0x1ee */
/* File: x86/OP_UNUSED_EEFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_EFFF: /* 0x1ef */
/* File: x86/OP_UNUSED_EFFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F0FF: /* 0x1f0 */
/* File: x86/OP_UNUSED_F0FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F1FF: /* 0x1f1 */
/* File: x86/OP_UNUSED_F1FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F2FF: /* 0x1f2 */
/* File: x86/OP_UNUSED_F2FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F3FF: /* 0x1f3 */
/* File: x86/OP_UNUSED_F3FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F4FF: /* 0x1f4 */
/* File: x86/OP_UNUSED_F4FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F5FF: /* 0x1f5 */
/* File: x86/OP_UNUSED_F5FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F6FF: /* 0x1f6 */
/* File: x86/OP_UNUSED_F6FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F7FF: /* 0x1f7 */
/* File: x86/OP_UNUSED_F7FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F8FF: /* 0x1f8 */
/* File: x86/OP_UNUSED_F8FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_F9FF: /* 0x1f9 */
/* File: x86/OP_UNUSED_F9FF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_FAFF: /* 0x1fa */
/* File: x86/OP_UNUSED_FAFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_FBFF: /* 0x1fb */
/* File: x86/OP_UNUSED_FBFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_FCFF: /* 0x1fc */
/* File: x86/OP_UNUSED_FCFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_FDFF: /* 0x1fd */
/* File: x86/OP_UNUSED_FDFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_UNUSED_FEFF: /* 0x1fe */
/* File: x86/OP_UNUSED_FEFF.S */
/* File: x86/unused.S */
    jmp     common_abort


/* ------------------------------ */
    .balign 64
.L_OP_THROW_VERIFICATION_ERROR_JUMBO: /* 0x1ff */
/* File: x86/OP_THROW_VERIFICATION_ERROR_JUMBO.S */
    /*
     * Handle a jumbo throw-verification-error instruction.  This throws an
     * exception for an error discovered during verification.  The
     * exception is indicated by BBBB, with some detail provided by AAAAAAAA.
     */
    /* exop BBBB, ref@AAAAAAAA */
    movl     rSELF,%ecx
    movl     2(rPC),%eax                     # eax<- AAAAAAAA
    movl     offThread_method(%ecx),%ecx       # ecx<- self->method
    EXPORT_PC
    movl     %eax,OUT_ARG2(%esp)             # arg2<- AAAAAAAA
    movl     rINST,OUT_ARG1(%esp)            # arg1<- BBBB
    movl     %ecx,OUT_ARG0(%esp)             # arg0<- method
    call     dvmThrowVerificationError       # call(method, kind, ref)
    jmp      common_exceptionThrown          # handle exception


    .balign 64
    .size   .L_OP_NOP, .-.L_OP_NOP
    .global dvmAsmInstructionEnd
dvmAsmInstructionEnd:

/*
 * ===========================================================================
 *  Sister implementations
 * ===========================================================================
 */
    .global dvmAsmSisterStart
    .type   dvmAsmSisterStart, %function
    .text
    .balign 4
dvmAsmSisterStart:

/* continuation for OP_CONST_STRING */

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.LOP_CONST_STRING_resolve:
    movl     rSELF,%eax
    movl     %ecx,rINST                # rINST<- AA
    EXPORT_PC
    movl     offThread_method(%eax),%eax # eax<- self->method
    movzwl   2(rPC),%ecx               # ecx<- BBBB
    movl     offMethod_clazz(%eax),%eax
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveString          # go resolve
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_CONST_STRING_JUMBO */

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.LOP_CONST_STRING_JUMBO_resolve:
    movl     rSELF,%eax
    movl     %ecx,rINST                # rINST<- AA
    EXPORT_PC
    movl     offThread_method(%eax),%eax # eax<- self->method
    movl     2(rPC),%ecx               # ecx<- BBBBBBBB
    movl     offMethod_clazz(%eax),%eax
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveString          # go resolve
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    GOTO_NEXT_R %edx

/* continuation for OP_CONST_CLASS */

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.LOP_CONST_CLASS_resolve:
    movl     rSELF,%eax
    movl     %ecx,rINST                # rINST<- AA
    EXPORT_PC
    movl     offThread_method(%eax),%eax # eax<- self->method
    movl     $1,OUT_ARG2(%esp)        # true
    movzwl   2(rPC),%ecx               # ecx<- BBBB
    movl     offMethod_clazz(%eax),%eax
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveClass           # go resolve
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_MONITOR_ENTER */

.LOP_MONITOR_ENTER_continue:
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmLockObject               # dvmLockObject(self,object)
    ADVANCE_PC 1
    GOTO_NEXT

/* continuation for OP_MONITOR_EXIT */

.LOP_MONITOR_EXIT_continue:
    call    dvmUnlockObject             # unlock(self,obj)
    FETCH_INST_OPCODE 1 %edx
    testl   %eax,%eax                   # success?
    ADVANCE_PC 1
    je      common_exceptionThrown      # no, exception pending
    GOTO_NEXT_R %edx
.LOP_MONITOR_EXIT_errNullObject:
    ADVANCE_PC 1                        # advance before throw
    jmp     common_errNullObject

/* continuation for OP_CHECK_CAST */

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  ecx holds obj->clazz
     *  eax holds class resolved from BBBB
     *  rINST holds object
     */
.LOP_CHECK_CAST_fullcheck:
    movl    %eax,sReg0                 # we'll need the desired class on failure
    movl    %eax,OUT_ARG1(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmInstanceofNonTrivial    # eax<- boolean result
    testl   %eax,%eax                  # failed?
    jne     .LOP_CHECK_CAST_okay           # no, success

    # A cast has failed.  We need to throw a ClassCastException.
    EXPORT_PC
    movl    offObject_clazz(rINST),%eax
    movl    %eax,OUT_ARG0(%esp)                 # arg0<- obj->clazz
    movl    sReg0,%ecx
    movl    %ecx,OUT_ARG1(%esp)                 # arg1<- desired class
    call    dvmThrowClassCastException
    jmp     common_exceptionThrown

    /*
     * Resolution required.  This is the least-likely path, and we're
     * going to have to recreate some data.
     *
     *  rINST holds object
     */
.LOP_CHECK_CAST_resolve:
    movl    rSELF,%ecx
    EXPORT_PC
    movzwl  2(rPC),%eax                # eax<- BBBB
    movl    offThread_method(%ecx),%ecx  # ecx<- self->method
    movl    %eax,OUT_ARG1(%esp)        # arg1<- BBBB
    movl    offMethod_clazz(%ecx),%ecx # ecx<- metho->clazz
    movl    $0,OUT_ARG2(%esp)         # arg2<- false
    movl    %ecx,OUT_ARG0(%esp)        # arg0<- method->clazz
    call    dvmResolveClass            # eax<- resolved ClassObject ptr
    testl   %eax,%eax                  # got null?
    je      common_exceptionThrown     # yes, handle exception
    movl    offObject_clazz(rINST),%ecx  # ecx<- obj->clazz
    jmp     .LOP_CHECK_CAST_resolved       # pick up where we left off

/* continuation for OP_INSTANCE_OF */

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  eax holds obj->clazz
     *  ecx holds class resolved from BBBB
     *  rINST has BA
     */
.LOP_INSTANCE_OF_fullcheck:
    movl    %eax,OUT_ARG0(%esp)
    movl    %ecx,OUT_ARG1(%esp)
    call    dvmInstanceofNonTrivial     # eax<- boolean result
    # fall through to OP_INSTANCE_OF_store

    /*
     * eax holds boolean result
     * rINST holds BA
     */
.LOP_INSTANCE_OF_store:
    FETCH_INST_OPCODE 2 %edx
    andb    $0xf,rINSTbl               # <- A
    ADVANCE_PC 2
    SET_VREG %eax rINST                 # vA<- eax
    GOTO_NEXT_R %edx

    /*
     * Trivial test succeeded, save and bail.
     *  r9 holds A
     */
.LOP_INSTANCE_OF_trivial:
    FETCH_INST_OPCODE 2 %edx
    andb    $0xf,rINSTbl               # <- A
    ADVANCE_PC 2
    movl    $1,%eax
    SET_VREG %eax rINST                 # vA<- true
    GOTO_NEXT_R %edx

    /*
     * Resolution required.  This is the least-likely path.
     *
     *  edx holds BBBB
     *  rINST holds BA
     */
.LOP_INSTANCE_OF_resolve:
    movl    %edx,OUT_ARG1(%esp)         # arg1<- BBBB
    movl    rSELF,%ecx
    movl    offThread_method(%ecx),%ecx
    movl    $1,OUT_ARG2(%esp)          # arg2<- true
    movl    offMethod_clazz(%ecx),%ecx  # ecx<- method->clazz
    EXPORT_PC
    movl    %ecx,OUT_ARG0(%esp)         # arg0<- method->clazz
    call    dvmResolveClass             # eax<- resolved ClassObject ptr
    testl   %eax,%eax                   # success?
    je      common_exceptionThrown      # no, handle exception
/* Now, we need to sync up with fast path.  We need eax to
 * hold the obj->clazz, and ecx to hold the resolved class
 */
    movl    %eax,%ecx                   # ecx<- resolved class
    movl    rINST,%eax                # eax<- BA
    sarl    $4,%eax                    # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB (obj)
    movl    offObject_clazz(%eax),%eax  # eax<- obj->clazz
    jmp     .LOP_INSTANCE_OF_resolved

/* continuation for OP_NEW_INSTANCE */

.LOP_NEW_INSTANCE_initialized:  # on entry, ecx<- class
    /* TODO: remove test for interface/abstract, now done in verifier */
    testl     $(ACC_INTERFACE|ACC_ABSTRACT),offClassObject_accessFlags(%ecx)
    movl      $ALLOC_DONT_TRACK,OUT_ARG1(%esp)
    jne       .LOP_NEW_INSTANCE_abstract
.LOP_NEW_INSTANCE_finish: # ecx=class
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmAllocObject             # eax<- new object
    FETCH_INST_OPCODE 2 %edx
    testl    %eax,%eax                  # success?
    je       common_exceptionThrown     # no, bail out
    SET_VREG %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

    /*
     * Class initialization required.
     *
     *  ecx holds class object
     */
.LOP_NEW_INSTANCE_needinit:
    SPILL_TMP1(%ecx)                    # save object
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmInitClass                # initialize class
    UNSPILL_TMP1(%ecx)                  # restore object
    testl   %eax,%eax                   # success?
    jne     .LOP_NEW_INSTANCE_initialized     # success, continue
    jmp     common_exceptionThrown      # go deal with init exception

    /*
     * Resolution required.  This is the least-likely path.
     *
     */
.LOP_NEW_INSTANCE_resolve:
    movl    rSELF,%ecx
    movzwl  2(rPC),%eax
    movl    offThread_method(%ecx),%ecx   # ecx<- self->method
    movl    %eax,OUT_ARG1(%esp)
    movl    offMethod_clazz(%ecx),%ecx  # ecx<- method->clazz
    movl    $0,OUT_ARG2(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmResolveClass             # call(clazz,off,flags)
    movl    %eax,%ecx                   # ecx<- resolved ClassObject ptr
    testl   %ecx,%ecx                   # success?
    jne     .LOP_NEW_INSTANCE_resolved        # good to go
    jmp     common_exceptionThrown      # no, handle exception

    /*
     * TODO: remove this
     * We can't instantiate an abstract class or interface, so throw an
     * InstantiationError with the class descriptor as the message.
     *
     *  ecx holds class object
     */
.LOP_NEW_INSTANCE_abstract:
    movl    offClassObject_descriptor(%ecx),%eax
    movl    $.LstrInstantiationError,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowExceptionWithClassMessage
    jmp     common_exceptionThrown

/* continuation for OP_NEW_ARRAY */

    /*
     * Resolve class.  (This is an uncommon case.)
     *  ecx holds class (null here)
     *  eax holds array length (vB)
     */
.LOP_NEW_ARRAY_resolve:
    movl    rSELF,%ecx
    SPILL_TMP1(%eax)                   # save array length
    movl    offThread_method(%ecx),%ecx  # ecx<- self->method
    movzwl  2(rPC),%eax                # eax<- CCCC
    movl    offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
    movl    %eax,OUT_ARG1(%esp)
    movl    $0,OUT_ARG2(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmResolveClass            # eax<- call(clazz,ref,flag)
    movl    %eax,%ecx
    UNSPILL_TMP1(%eax)
    testl   %ecx,%ecx                  # successful resolution?
    je      common_exceptionThrown     # no, bail.
# fall through to OP_NEW_ARRAY_finish

    /*
     * Finish allocation
     *
     * ecx holds class
     * eax holds array length (vB)
     */
.LOP_NEW_ARRAY_finish:
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    movl    $ALLOC_DONT_TRACK,OUT_ARG2(%esp)
    call    dvmAllocArrayByClass    # eax<- call(clazz,length,flags)
    FETCH_INST_OPCODE 2 %edx
    testl   %eax,%eax               # failed?
    je      common_exceptionThrown  # yup - go handle
    SET_VREG %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_FILLED_NEW_ARRAY */

.LOP_FILLED_NEW_ARRAY_more:
    movl    offMethod_clazz(%eax),%eax        # eax<- method->clazz
    movl    %eax,OUT_ARG0(%esp)               # arg0<- clazz
    call    dvmResolveClass                   # eax<- call(clazz,ref,flag)
    testl   %eax,%eax                         # null?
    je      common_exceptionThrown            # yes, handle it

       # note: fall through to .LOP_FILLED_NEW_ARRAY_continue

    /*
     * On entry:
     *    eax holds array class [r0]
     *    rINST holds AA or BB [r10]
     *    ecx is scratch
     */
.LOP_FILLED_NEW_ARRAY_continue:
    movl    offClassObject_descriptor(%eax),%ecx  # ecx<- arrayClass->descriptor
    movl    $ALLOC_DONT_TRACK,OUT_ARG2(%esp)     # arg2<- flags
    movzbl  1(%ecx),%ecx                          # ecx<- descriptor[1]
    movl    %eax,OUT_ARG0(%esp)                   # arg0<- arrayClass
    movl    rSELF,%eax
    cmpb    $'I',%cl                             # supported?
    je      1f
    cmpb    $'L',%cl
    je      1f
    cmpb    $'[',%cl
    jne      .LOP_FILLED_NEW_ARRAY_notimpl                  # no, not handled yet
1:
    movl    %ecx,offThread_retval+4(%eax)           # save type
    .if      (!0)
    SPILL_TMP1(rINST)                              # save copy, need "B" later
    sarl    $4,rINST
    .endif
    movl    rINST,OUT_ARG1(%esp)                  # arg1<- A or AA (length)
    call    dvmAllocArrayByClass     # eax<- call(arrayClass, length, flags)
    movl    rSELF,%ecx
    testl   %eax,%eax                             # alloc successful?
    je      common_exceptionThrown                # no, handle exception
    movl    %eax,offThread_retval(%ecx)             # retval.l<- new array
    movzwl  4(rPC),%ecx                           # ecx<- FEDC or CCCC
    leal    offArrayObject_contents(%eax),%eax    # eax<- newArray->contents

/* at this point:
 *     eax is pointer to tgt
 *     rINST is length
 *     ecx is FEDC or CCCC
 *     TMP_SPILL1 is BA
 *  We now need to copy values from registers into the array
 */

    .if 0
    # set up src pointer
    SPILL_TMP2(%esi)
    SPILL_TMP3(%edi)
    leal    (rFP,%ecx,4),%esi # set up src ptr
    movl    %eax,%edi         # set up dst ptr
    movl    rINST,%ecx        # load count register
    rep
    movsd
    UNSPILL_TMP2(%esi)
    UNSPILL_TMP3(%edi)
    movl    rSELF,%ecx
    movl    offThread_retval+4(%ecx),%eax      # eax<- type
    FETCH_INST_OPCODE 3 %edx
    .else
    testl  rINST,rINST
    je     4f
    UNSPILL_TMP1(%edx)        # restore "BA"
    andl   $0x0f,%edx        # edx<- 0000000A
    sall   $16,%edx          # edx<- 000A0000
    orl    %ecx,%edx          # edx<- 000AFEDC
3:
    movl   $0xf,%ecx
    andl   %edx,%ecx          # ecx<- next reg to load
    GET_VREG_R %ecx %ecx
    shrl   $4,%edx
    leal   4(%eax),%eax
    movl   %ecx,-4(%eax)
    sub    $1,rINST
    jne    3b
4:
    movl   rSELF,%ecx
    movl    offThread_retval+4(%ecx),%eax      # eax<- type
    FETCH_INST_OPCODE 3 %edx
    .endif

    cmpb    $'I',%al                        # Int array?
    je      5f                               # skip card mark if so
    movl    offThread_retval(%ecx),%eax        # eax<- object head
    movl    offThread_cardTable(%ecx),%ecx     # card table base
    shrl    $GC_CARD_SHIFT,%eax             # convert to card num
    movb    %cl,(%ecx,%eax)                  # mark card based on object head
5:
    ADVANCE_PC 3
    GOTO_NEXT_R %edx


    /*
     * Throw an exception indicating that we have not implemented this
     * mode of filled-new-array.
     */
.LOP_FILLED_NEW_ARRAY_notimpl:
    movl    $.LstrInternalErrorA,%eax
    movl    %eax,OUT_ARG0(%esp)
    movl    $.LstrFilledNewArrayNotImplA,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

/* continuation for OP_FILLED_NEW_ARRAY_RANGE */

.LOP_FILLED_NEW_ARRAY_RANGE_more:
    movl    offMethod_clazz(%eax),%eax        # eax<- method->clazz
    movl    %eax,OUT_ARG0(%esp)               # arg0<- clazz
    call    dvmResolveClass                   # eax<- call(clazz,ref,flag)
    testl   %eax,%eax                         # null?
    je      common_exceptionThrown            # yes, handle it

       # note: fall through to .LOP_FILLED_NEW_ARRAY_RANGE_continue

    /*
     * On entry:
     *    eax holds array class [r0]
     *    rINST holds AA or BB [r10]
     *    ecx is scratch
     */
.LOP_FILLED_NEW_ARRAY_RANGE_continue:
    movl    offClassObject_descriptor(%eax),%ecx  # ecx<- arrayClass->descriptor
    movl    $ALLOC_DONT_TRACK,OUT_ARG2(%esp)     # arg2<- flags
    movzbl  1(%ecx),%ecx                          # ecx<- descriptor[1]
    movl    %eax,OUT_ARG0(%esp)                   # arg0<- arrayClass
    movl    rSELF,%eax
    cmpb    $'I',%cl                             # supported?
    je      1f
    cmpb    $'L',%cl
    je      1f
    cmpb    $'[',%cl
    jne      .LOP_FILLED_NEW_ARRAY_RANGE_notimpl                  # no, not handled yet
1:
    movl    %ecx,offThread_retval+4(%eax)           # save type
    .if      (!1)
    SPILL_TMP1(rINST)                              # save copy, need "B" later
    sarl    $4,rINST
    .endif
    movl    rINST,OUT_ARG1(%esp)                  # arg1<- A or AA (length)
    call    dvmAllocArrayByClass     # eax<- call(arrayClass, length, flags)
    movl    rSELF,%ecx
    testl   %eax,%eax                             # alloc successful?
    je      common_exceptionThrown                # no, handle exception
    movl    %eax,offThread_retval(%ecx)             # retval.l<- new array
    movzwl  4(rPC),%ecx                           # ecx<- FEDC or CCCC
    leal    offArrayObject_contents(%eax),%eax    # eax<- newArray->contents

/* at this point:
 *     eax is pointer to tgt
 *     rINST is length
 *     ecx is FEDC or CCCC
 *     TMP_SPILL1 is BA
 *  We now need to copy values from registers into the array
 */

    .if 1
    # set up src pointer
    SPILL_TMP2(%esi)
    SPILL_TMP3(%edi)
    leal    (rFP,%ecx,4),%esi # set up src ptr
    movl    %eax,%edi         # set up dst ptr
    movl    rINST,%ecx        # load count register
    rep
    movsd
    UNSPILL_TMP2(%esi)
    UNSPILL_TMP3(%edi)
    movl    rSELF,%ecx
    movl    offThread_retval+4(%ecx),%eax      # eax<- type
    FETCH_INST_OPCODE 3 %edx
    .else
    testl  rINST,rINST
    je     4f
    UNSPILL_TMP1(%edx)        # restore "BA"
    andl   $0x0f,%edx        # edx<- 0000000A
    sall   $16,%edx          # edx<- 000A0000
    orl    %ecx,%edx          # edx<- 000AFEDC
3:
    movl   $0xf,%ecx
    andl   %edx,%ecx          # ecx<- next reg to load
    GET_VREG_R %ecx %ecx
    shrl   $4,%edx
    leal   4(%eax),%eax
    movl   %ecx,-4(%eax)
    sub    $1,rINST
    jne    3b
4:
    movl   rSELF,%ecx
    movl    offThread_retval+4(%ecx),%eax      # eax<- type
    FETCH_INST_OPCODE 3 %edx
    .endif

    cmpb    $'I',%al                        # Int array?
    je      5f                               # skip card mark if so
    movl    offThread_retval(%ecx),%eax        # eax<- object head
    movl    offThread_cardTable(%ecx),%ecx     # card table base
    shrl    $GC_CARD_SHIFT,%eax             # convert to card num
    movb    %cl,(%ecx,%eax)                  # mark card based on object head
5:
    ADVANCE_PC 3
    GOTO_NEXT_R %edx


    /*
     * Throw an exception indicating that we have not implemented this
     * mode of filled-new-array.
     */
.LOP_FILLED_NEW_ARRAY_RANGE_notimpl:
    movl    $.LstrInternalErrorA,%eax
    movl    %eax,OUT_ARG0(%esp)
    movl    $.LstrFilledNewArrayNotImplA,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

/* continuation for OP_CMPL_FLOAT */

.LOP_CMPL_FLOAT_isNaN:
    movl      $-1,%ecx
    jmp       .LOP_CMPL_FLOAT_finish

/* continuation for OP_CMPG_FLOAT */

.LOP_CMPG_FLOAT_isNaN:
    movl      $1,%ecx
    jmp       .LOP_CMPG_FLOAT_finish

/* continuation for OP_CMPL_DOUBLE */

.LOP_CMPL_DOUBLE_isNaN:
    movl      $-1,%ecx
    jmp       .LOP_CMPL_DOUBLE_finish

/* continuation for OP_CMPG_DOUBLE */

.LOP_CMPG_DOUBLE_isNaN:
    movl      $1,%ecx
    jmp       .LOP_CMPG_DOUBLE_finish

/* continuation for OP_CMP_LONG */

.LOP_CMP_LONG_bigger:
    movl      $1,%ecx
    jmp       .LOP_CMP_LONG_finish
.LOP_CMP_LONG_smaller:
    movl      $-1,%ecx
.LOP_CMP_LONG_finish:
    SET_VREG %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_AGET_WIDE */

.LOP_AGET_WIDE_finish:
    leal      offArrayObject_contents(%eax,%ecx,8),%eax
    movl      (%eax),%ecx
    movl      4(%eax),%eax
    SET_VREG_WORD %ecx rINST 0
    SET_VREG_WORD %eax rINST 1
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_APUT_WIDE */

.LOP_APUT_WIDE_finish:
    leal      offArrayObject_contents(%eax,%ecx,8),%eax
    GET_VREG_WORD %ecx rINST 0
    GET_VREG_WORD rINST rINST 1
    movl      rINST,4(%eax)
    FETCH_INST_OPCODE 2 %edx
    movl      %ecx,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_APUT_OBJECT */

    /* On entry:
     *   eax<- array object
     *   ecx<- index
     *   rINST<- vAA
     */
.LOP_APUT_OBJECT_continue:
    leal      offArrayObject_contents(%eax,%ecx,4),%ecx
    testl     rINST,rINST                    # storing null reference?
    je        .LOP_APUT_OBJECT_skip_check
    SPILL_TMP1(%ecx)                         # save target address
    SPILL_TMP2(%eax)                         # save object head
    movl      offObject_clazz(%eax),%eax     # eax<- arrayObj->clazz
    movl      offObject_clazz(rINST),%ecx    # ecx<- obj->clazz
    movl      %eax,OUT_ARG1(%esp)
    movl      %ecx,OUT_ARG0(%esp)
    movl      %ecx,sReg0                     # store the two classes for later
    movl      %eax,sReg1
    call      dvmCanPutArrayElement          # test object type vs. array type
    UNSPILL_TMP1(%ecx)                       # recover target address
    testl     %eax,%eax
    movl      rSELF,%eax
    jne       .LOP_APUT_OBJECT_types_okay

    # The types don't match.  We need to throw an ArrayStoreException.
    EXPORT_PC
    movl      sReg0,%eax                     # restore the two classes...
    movl      %eax,OUT_ARG0(%esp)
    movl      sReg1,%ecx
    movl      %ecx,OUT_ARG1(%esp)
    call      dvmThrowArrayStoreException    # ...and throw
    jmp       common_exceptionThrown

.LOP_APUT_OBJECT_types_okay:
    movl      offThread_cardTable(%eax),%eax   # get card table base
    movl      rINST,(%ecx)                   # store into array
    UNSPILL_TMP2(%ecx)                       # recover object head
    FETCH_INST_OPCODE 2 %edx
    shrl      $GC_CARD_SHIFT,%ecx           # object head to card number
    movb      %al,(%eax,%ecx)                # mark card using object head
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.LOP_APUT_OBJECT_skip_check:
    movl      rINST,(%ecx)
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET */


.LOP_IGET_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_finish
    jmp     common_exceptionThrown

.LOP_IGET_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_WIDE */


.LOP_IGET_WIDE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save objpointer across call
    movl    rPC,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IGET_WIDE_finish
    jmp     common_exceptionThrown

.LOP_IGET_WIDE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    leal    (%ecx,%eax,1),%eax                  # eax<- address of field
    movl    (%eax),%ecx                         # ecx<- lsw
    movl    4(%eax),%eax                        # eax<- msw
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %ecx rINST 0
    SET_VREG_WORD %eax rINST 1
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_OBJECT */


.LOP_IGET_OBJECT_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_OBJECT_finish
    jmp     common_exceptionThrown

.LOP_IGET_OBJECT_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_BOOLEAN */


.LOP_IGET_BOOLEAN_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_BOOLEAN_finish
    jmp     common_exceptionThrown

.LOP_IGET_BOOLEAN_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movzbl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_BYTE */


.LOP_IGET_BYTE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_BYTE_finish
    jmp     common_exceptionThrown

.LOP_IGET_BYTE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movsbl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_CHAR */


.LOP_IGET_CHAR_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_CHAR_finish
    jmp     common_exceptionThrown

.LOP_IGET_CHAR_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movzwl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_SHORT */


.LOP_IGET_SHORT_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_SHORT_finish
    jmp     common_exceptionThrown

.LOP_IGET_SHORT_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movswl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT */


.LOP_IPUT_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_finish
    jmp     common_exceptionThrown

.LOP_IPUT_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                       # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 2 %edx
    movl   rINST,(%ecx,%eax,1)            # obj.field <- v[A](8/16/32 bits)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_WIDE */


.LOP_IPUT_WIDE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  ... which returns InstrField ptr
    jne     .LOP_IPUT_WIDE_finish
    jmp     common_exceptionThrown

.LOP_IPUT_WIDE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   %edx is scratch, but needs to be unspilled
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    leal    (%ecx,%eax,1),%eax                  # eax<- address of field
    GET_VREG_WORD %ecx rINST 0                  # ecx<- lsw
    GET_VREG_WORD rINST rINST 1                 # rINST<- msw
    FETCH_INST_OPCODE 2 %edx
    movl    rINST,4(%eax)
    movl    %ecx,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_OBJECT */


.LOP_IPUT_OBJECT_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_OBJECT_finish
    jmp     common_exceptionThrown

.LOP_IPUT_OBJECT_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   %edx is scratch, but needs to be unspilled
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                      # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl    rINST,(%ecx,%eax)      # obj.field <- v[A](8/16/32 bits)
    movl    rSELF,%eax
    testl   rINST,rINST                         # stored a NULL?
    movl    offThread_cardTable(%eax),%eax        # get card table base
    FETCH_INST_OPCODE 2 %edx
    je      1f                                  # skip card mark if null store
    shrl    $GC_CARD_SHIFT,%ecx                # object head to card number
    movb    %al,(%eax,%ecx)                     # mark card using object head
1:
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_BOOLEAN */


.LOP_IPUT_BOOLEAN_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_BOOLEAN_finish
    jmp     common_exceptionThrown

.LOP_IPUT_BOOLEAN_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                       # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 2 %edx
    movb   rINSTbl,(%ecx,%eax,1)            # obj.field <- v[A](8/16/32 bits)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_BYTE */


.LOP_IPUT_BYTE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_BYTE_finish
    jmp     common_exceptionThrown

.LOP_IPUT_BYTE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                       # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 2 %edx
    movb   rINSTbl,(%ecx,%eax,1)            # obj.field <- v[A](8/16/32 bits)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_CHAR */


.LOP_IPUT_CHAR_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_CHAR_finish
    jmp     common_exceptionThrown

.LOP_IPUT_CHAR_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                       # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 2 %edx
    movw   rINSTw,(%ecx,%eax,1)            # obj.field <- v[A](8/16/32 bits)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_SHORT */


.LOP_IPUT_SHORT_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_SHORT_finish
    jmp     common_exceptionThrown

.LOP_IPUT_SHORT_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                       # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 2 %edx
    movw   rINSTw,(%ecx,%eax,1)            # obj.field <- v[A](8/16/32 bits)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_SGET */

    /*
     * Go resolve the field
     */
.LOP_SGET_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_WIDE */

    /*
     * Go resolve the field
     */
.LOP_SGET_WIDE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_WIDE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_OBJECT */

    /*
     * Go resolve the field
     */
.LOP_SGET_OBJECT_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_OBJECT_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_BOOLEAN */

    /*
     * Go resolve the field
     */
.LOP_SGET_BOOLEAN_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_BOOLEAN_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_BYTE */

    /*
     * Go resolve the field
     */
.LOP_SGET_BYTE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_BYTE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_CHAR */

    /*
     * Go resolve the field
     */
.LOP_SGET_CHAR_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_CHAR_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_SHORT */

    /*
     * Go resolve the field
     */
.LOP_SGET_SHORT_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_SHORT_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT */

    /*
     * Go resolve the field
     */
.LOP_SPUT_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_WIDE */

    /*
     * Go resolve the field
     */
.LOP_SPUT_WIDE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_WIDE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_OBJECT */


.LOP_SPUT_OBJECT_continue:
    movl      %ecx,offStaticField_value(%eax)    # do the store
    testl     %ecx,%ecx                          # stored null object ptr?
    FETCH_INST_OPCODE 2 %edx
    je        1f                                 # skip card mark if null
    movl      rSELF,%ecx
    movl      offField_clazz(%eax),%eax          # eax<- method->clazz
    movl      offThread_cardTable(%ecx),%ecx       # get card table base
    shrl      $GC_CARD_SHIFT,%eax               # head to card number
    movb      %cl,(%ecx,%eax)                    # mark card
1:
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.LOP_SPUT_OBJECT_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_OBJECT_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_BOOLEAN */

    /*
     * Go resolve the field
     */
.LOP_SPUT_BOOLEAN_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_BOOLEAN_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_BYTE */

    /*
     * Go resolve the field
     */
.LOP_SPUT_BYTE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_BYTE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_CHAR */

    /*
     * Go resolve the field
     */
.LOP_SPUT_CHAR_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_CHAR_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_SHORT */

    /*
     * Go resolve the field
     */
.LOP_SPUT_SHORT_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_SHORT_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_INVOKE_VIRTUAL */


.LOP_INVOKE_VIRTUAL_more:
    movl      offMethod_clazz(%eax),%eax  # ecx<- method->clazz
    movl      %eax,OUT_ARG0(%esp)         # arg0<- clazz
    movl      $METHOD_VIRTUAL,OUT_ARG2(%esp) # arg2<- flags
    call      dvmResolveMethod            # eax<- call(clazz, ref, flags)
    testl     %eax,%eax                   # got null?
    jne       .LOP_INVOKE_VIRTUAL_continue        # no, continue
    jmp       common_exceptionThrown      # yes, handle exception

    /* At this point:
     *   eax = resolved base method
     *   ecx = scratch
     */
.LOP_INVOKE_VIRTUAL_continue:
    movzwl    4(rPC),%ecx               # ecx<- GFED or CCCC
    .if       (!0)
    andl      $0xf,%ecx                # ecx<- D (or stays CCCC)
    .endif
    GET_VREG_R  %ecx %ecx               # ecx<- "this"
    movzwl    offMethod_methodIndex(%eax),%eax  # eax<- baseMethod->methodIndex
    testl     %ecx,%ecx                 # null this?
    je        common_errNullObject      # go if so
    movl      offObject_clazz(%ecx),%ecx  # ecx<- thisPtr->clazz
    movl      offClassObject_vtable(%ecx),%ecx # ecx<- thisPtr->clazz->vtable
    movl      (%ecx,%eax,4),%eax        # eax<- vtable[methodIndex]
    jmp       common_invokeMethodNoRange

/* continuation for OP_INVOKE_SUPER */

    /*
     * At this point:
     *  ecx = resolved base method [r0]
     *  eax = method->clazz [r9]
     */
.LOP_INVOKE_SUPER_continue:
    movl    offClassObject_super(%eax),%eax   # eax<- method->clazz->super
    movzwl  offMethod_methodIndex(%ecx),%ecx  # ecx<- baseMthod->methodIndex
    cmpl    offClassObject_vtableCount(%eax),%ecx # compare(methodIndex,vtableCount)
    jae     .LOP_INVOKE_SUPER_nsm           # method not present in superclass
    movl    offClassObject_vtable(%eax),%eax   # eax<- ...clazz->super->vtable
    movl    (%eax,%ecx,4),%eax        # eax<- vtable[methodIndex]
    jmp     common_invokeMethodNoRange


    /* At this point:
     * ecx = null (needs to be resolved base method)
     * eax = method->clazz
    */
.LOP_INVOKE_SUPER_resolve:
    SPILL_TMP1(%eax)                    # method->clazz
    movl    %eax,OUT_ARG0(%esp)         # arg0<- method->clazz
    movzwl  2(rPC),%ecx                 # ecx<- BBBB
    movl    $METHOD_VIRTUAL,OUT_ARG2(%esp)  # arg2<- resolver method type
    movl    %ecx,OUT_ARG1(%esp)         # arg1<- ref
    call    dvmResolveMethod            # eax<- call(clazz, ref, flags)
    testl   %eax,%eax                   # got null?
    movl    %eax,%ecx                   # ecx<- resolved base method
    UNSPILL_TMP1(%eax)                  # restore method->clazz
    jne     .LOP_INVOKE_SUPER_continue        # good to go - continue
    jmp     common_exceptionThrown      # handle exception

    /*
     * Throw a NoSuchMethodError with the method name as the message.
     *  ecx = resolved base method
     */
.LOP_INVOKE_SUPER_nsm:
    movl    offMethod_name(%ecx),%eax
    mov     %eax,OUT_ARG1(%esp)
    jmp     common_errNoSuchMethod

/* continuation for OP_INVOKE_DIRECT */

    /*
     * On entry:
     *   TMP_SPILL  <- "this" register
     * Things a bit ugly on this path, but it's the less
     * frequent one.  We'll have to do some reloading.
     */
.LOP_INVOKE_DIRECT_resolve:
     SPILL_TMP1(%ecx)
     movl     rSELF,%ecx
     movl     offThread_method(%ecx),%ecx  # ecx<- self->method
     movzwl   2(rPC),%eax      # reference (BBBB or CCCC)
     movl     offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
     movl     $METHOD_DIRECT,OUT_ARG2(%esp)
     movl     %eax,OUT_ARG1(%esp)
     movl     %ecx,OUT_ARG0(%esp)
     call     dvmResolveMethod # eax<- call(clazz, ref, flags)
     UNSPILL_TMP1(%ecx)
     testl    %eax,%eax
     jne      .LOP_INVOKE_DIRECT_finish
     jmp      common_exceptionThrown

/* continuation for OP_INVOKE_STATIC */

.LOP_INVOKE_STATIC_continue:
    movl      $METHOD_STATIC,%eax
    movl      %eax,OUT_ARG2(%esp)       # arg2<- flags
    call      dvmResolveMethod          # call(clazz,ref,flags)
    testl     %eax,%eax                 # got null?
    jne       common_invokeMethodNoRange
    jmp       common_exceptionThrown

/* continuation for OP_INVOKE_INTERFACE */

.LOP_INVOKE_INTERFACE_continue:
    call       dvmFindInterfaceMethodInCache # eax<- call(class, ref, method, dex)
    testl      %eax,%eax
    je         common_exceptionThrown
    jmp        common_invokeMethodNoRange

/* continuation for OP_INVOKE_VIRTUAL_RANGE */


.LOP_INVOKE_VIRTUAL_RANGE_more:
    movl      offMethod_clazz(%eax),%eax  # ecx<- method->clazz
    movl      %eax,OUT_ARG0(%esp)         # arg0<- clazz
    movl      $METHOD_VIRTUAL,OUT_ARG2(%esp) # arg2<- flags
    call      dvmResolveMethod            # eax<- call(clazz, ref, flags)
    testl     %eax,%eax                   # got null?
    jne       .LOP_INVOKE_VIRTUAL_RANGE_continue        # no, continue
    jmp       common_exceptionThrown      # yes, handle exception

    /* At this point:
     *   eax = resolved base method
     *   ecx = scratch
     */
.LOP_INVOKE_VIRTUAL_RANGE_continue:
    movzwl    4(rPC),%ecx               # ecx<- GFED or CCCC
    .if       (!1)
    andl      $0xf,%ecx                # ecx<- D (or stays CCCC)
    .endif
    GET_VREG_R  %ecx %ecx               # ecx<- "this"
    movzwl    offMethod_methodIndex(%eax),%eax  # eax<- baseMethod->methodIndex
    testl     %ecx,%ecx                 # null this?
    je        common_errNullObject      # go if so
    movl      offObject_clazz(%ecx),%ecx  # ecx<- thisPtr->clazz
    movl      offClassObject_vtable(%ecx),%ecx # ecx<- thisPtr->clazz->vtable
    movl      (%ecx,%eax,4),%eax        # eax<- vtable[methodIndex]
    jmp       common_invokeMethodRange

/* continuation for OP_INVOKE_SUPER_RANGE */

    /*
     * At this point:
     *  ecx = resolved base method [r0]
     *  eax = method->clazz [r9]
     */
.LOP_INVOKE_SUPER_RANGE_continue:
    movl    offClassObject_super(%eax),%eax   # eax<- method->clazz->super
    movzwl  offMethod_methodIndex(%ecx),%ecx  # ecx<- baseMthod->methodIndex
    cmpl    offClassObject_vtableCount(%eax),%ecx # compare(methodIndex,vtableCount)
    jae     .LOP_INVOKE_SUPER_RANGE_nsm           # method not present in superclass
    movl    offClassObject_vtable(%eax),%eax   # eax<- ...clazz->super->vtable
    movl    (%eax,%ecx,4),%eax        # eax<- vtable[methodIndex]
    jmp     common_invokeMethodRange


    /* At this point:
     * ecx = null (needs to be resolved base method)
     * eax = method->clazz
    */
.LOP_INVOKE_SUPER_RANGE_resolve:
    SPILL_TMP1(%eax)                    # method->clazz
    movl    %eax,OUT_ARG0(%esp)         # arg0<- method->clazz
    movzwl  2(rPC),%ecx                 # ecx<- BBBB
    movl    $METHOD_VIRTUAL,OUT_ARG2(%esp)  # arg2<- resolver method type
    movl    %ecx,OUT_ARG1(%esp)         # arg1<- ref
    call    dvmResolveMethod            # eax<- call(clazz, ref, flags)
    testl   %eax,%eax                   # got null?
    movl    %eax,%ecx                   # ecx<- resolved base method
    UNSPILL_TMP1(%eax)                  # restore method->clazz
    jne     .LOP_INVOKE_SUPER_RANGE_continue        # good to go - continue
    jmp     common_exceptionThrown      # handle exception

    /*
     * Throw a NoSuchMethodError with the method name as the message.
     *  ecx = resolved base method
     */
.LOP_INVOKE_SUPER_RANGE_nsm:
    movl    offMethod_name(%ecx),%eax
    mov     %eax,OUT_ARG1(%esp)
    jmp     common_errNoSuchMethod

/* continuation for OP_INVOKE_DIRECT_RANGE */

    /*
     * On entry:
     *   TMP_SPILL  <- "this" register
     * Things a bit ugly on this path, but it's the less
     * frequent one.  We'll have to do some reloading.
     */
.LOP_INVOKE_DIRECT_RANGE_resolve:
     SPILL_TMP1(%ecx)
     movl     rSELF,%ecx
     movl     offThread_method(%ecx),%ecx  # ecx<- self->method
     movzwl   2(rPC),%eax      # reference (BBBB or CCCC)
     movl     offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
     movl     $METHOD_DIRECT,OUT_ARG2(%esp)
     movl     %eax,OUT_ARG1(%esp)
     movl     %ecx,OUT_ARG0(%esp)
     call     dvmResolveMethod # eax<- call(clazz, ref, flags)
     UNSPILL_TMP1(%ecx)
     testl    %eax,%eax
     jne      .LOP_INVOKE_DIRECT_RANGE_finish
     jmp      common_exceptionThrown

/* continuation for OP_INVOKE_STATIC_RANGE */

.LOP_INVOKE_STATIC_RANGE_continue:
    movl      $METHOD_STATIC,%eax
    movl      %eax,OUT_ARG2(%esp)       # arg2<- flags
    call      dvmResolveMethod          # call(clazz,ref,flags)
    testl     %eax,%eax                 # got null?
    jne       common_invokeMethodRange
    jmp       common_exceptionThrown

/* continuation for OP_INVOKE_INTERFACE_RANGE */

.LOP_INVOKE_INTERFACE_RANGE_continue:
    call       dvmFindInterfaceMethodInCache # eax<- call(class, ref, method, dex)
    testl      %eax,%eax
    je         common_exceptionThrown
    jmp        common_invokeMethodRange

/* continuation for OP_FLOAT_TO_INT */


.LOP_FLOAT_TO_INT_continue:
    .if 0
    movl     $0x80000000,%eax
    xorl     4(rFP,%ecx,4),%eax
    orl      (rFP,%ecx,4),%eax
    .else
    cmpl     $0x80000000,(rFP,%ecx,4)
    .endif
    je       .LOP_FLOAT_TO_INT_special_case # fix up result

.LOP_FLOAT_TO_INT_finish:
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.LOP_FLOAT_TO_INT_special_case:
    fnstsw   %ax
    sahf
    jp       .LOP_FLOAT_TO_INT_isNaN
    adcl     $-1,(rFP,%ecx,4)
    .if 0
    adcl     $-1,4(rFP,%ecx,4)
    .endif
   jmp       .LOP_FLOAT_TO_INT_finish
.LOP_FLOAT_TO_INT_isNaN:
    movl      $0,(rFP,%ecx,4)
    .if 0
    movl      $0,4(rFP,%ecx,4)
    .endif
    jmp       .LOP_FLOAT_TO_INT_finish

/* continuation for OP_FLOAT_TO_LONG */


.LOP_FLOAT_TO_LONG_continue:
    .if 1
    movl     $0x80000000,%eax
    xorl     4(rFP,%ecx,4),%eax
    orl      (rFP,%ecx,4),%eax
    .else
    cmpl     $0x80000000,(rFP,%ecx,4)
    .endif
    je       .LOP_FLOAT_TO_LONG_special_case # fix up result

.LOP_FLOAT_TO_LONG_finish:
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.LOP_FLOAT_TO_LONG_special_case:
    fnstsw   %ax
    sahf
    jp       .LOP_FLOAT_TO_LONG_isNaN
    adcl     $-1,(rFP,%ecx,4)
    .if 1
    adcl     $-1,4(rFP,%ecx,4)
    .endif
   jmp       .LOP_FLOAT_TO_LONG_finish
.LOP_FLOAT_TO_LONG_isNaN:
    movl      $0,(rFP,%ecx,4)
    .if 1
    movl      $0,4(rFP,%ecx,4)
    .endif
    jmp       .LOP_FLOAT_TO_LONG_finish

/* continuation for OP_DOUBLE_TO_INT */


.LOP_DOUBLE_TO_INT_continue:
    .if 0
    movl     $0x80000000,%eax
    xorl     4(rFP,%ecx,4),%eax
    orl      (rFP,%ecx,4),%eax
    .else
    cmpl     $0x80000000,(rFP,%ecx,4)
    .endif
    je       .LOP_DOUBLE_TO_INT_special_case # fix up result

.LOP_DOUBLE_TO_INT_finish:
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.LOP_DOUBLE_TO_INT_special_case:
    fnstsw   %ax
    sahf
    jp       .LOP_DOUBLE_TO_INT_isNaN
    adcl     $-1,(rFP,%ecx,4)
    .if 0
    adcl     $-1,4(rFP,%ecx,4)
    .endif
   jmp       .LOP_DOUBLE_TO_INT_finish
.LOP_DOUBLE_TO_INT_isNaN:
    movl      $0,(rFP,%ecx,4)
    .if 0
    movl      $0,4(rFP,%ecx,4)
    .endif
    jmp       .LOP_DOUBLE_TO_INT_finish

/* continuation for OP_DOUBLE_TO_LONG */


.LOP_DOUBLE_TO_LONG_continue:
    .if 1
    movl     $0x80000000,%eax
    xorl     4(rFP,%ecx,4),%eax
    orl      (rFP,%ecx,4),%eax
    .else
    cmpl     $0x80000000,(rFP,%ecx,4)
    .endif
    je       .LOP_DOUBLE_TO_LONG_special_case # fix up result

.LOP_DOUBLE_TO_LONG_finish:
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.LOP_DOUBLE_TO_LONG_special_case:
    fnstsw   %ax
    sahf
    jp       .LOP_DOUBLE_TO_LONG_isNaN
    adcl     $-1,(rFP,%ecx,4)
    .if 1
    adcl     $-1,4(rFP,%ecx,4)
    .endif
   jmp       .LOP_DOUBLE_TO_LONG_finish
.LOP_DOUBLE_TO_LONG_isNaN:
    movl      $0,(rFP,%ecx,4)
    .if 1
    movl      $0,4(rFP,%ecx,4)
    .endif
    jmp       .LOP_DOUBLE_TO_LONG_finish

/* continuation for OP_DIV_INT */
.LOP_DIV_INT_continue_div:
    cltd
    idivl   %ecx
.LOP_DIV_INT_finish_div:
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_REM_INT */
.LOP_REM_INT_continue_div:
    cltd
    idivl   %ecx
.LOP_REM_INT_finish_div:
    SET_VREG %edx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_MUL_LONG */

.LOP_MUL_LONG_continue:
    leal      (%ecx,%edx),%edx     # full result now in %edx:%eax
    UNSPILL_TMP2(%esi)             # Restore Dalvik PC
    FETCH_INST_OPCODE 2 %ecx       # Fetch next instruction
    movl      %edx,4(rFP,rINST,4)  # v[B+1]<- %edx
    movl      %eax,(rFP,rINST,4)   # v[B]<- %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %ecx

/* continuation for OP_DIV_LONG */

.LOP_DIV_LONG_continue:
    call     __divdi3
.LOP_DIV_LONG_finish:
    SET_VREG_WORD %edx rINST 1
    SET_VREG_WORD %eax rINST 0
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.LOP_DIV_LONG_check_zero:
    testl   %edx,%edx
    jne     .LOP_DIV_LONG_notSpecial
    jmp     common_errDivideByZero
.LOP_DIV_LONG_check_neg1:
    testl   %edx,%eax
    jne     .LOP_DIV_LONG_notSpecial
    GET_VREG_WORD %edx %ecx 0
    GET_VREG_WORD %ecx %ecx 1
    testl    %edx,%edx
    jne      .LOP_DIV_LONG_notSpecial1
    cmpl     $0x80000000,%ecx
    jne      .LOP_DIV_LONG_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $0x80000000,%edx
    jmp      .LOP_DIV_LONG_finish

/* continuation for OP_REM_LONG */

.LOP_REM_LONG_continue:
    call     __moddi3
.LOP_REM_LONG_finish:
    SET_VREG_WORD %edx rINST 1
    SET_VREG_WORD %eax rINST 0
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.LOP_REM_LONG_check_zero:
    testl   %edx,%edx
    jne     .LOP_REM_LONG_notSpecial
    jmp     common_errDivideByZero
.LOP_REM_LONG_check_neg1:
    testl   %edx,%eax
    jne     .LOP_REM_LONG_notSpecial
    GET_VREG_WORD %edx %ecx 0
    GET_VREG_WORD %ecx %ecx 1
    testl    %edx,%edx
    jne      .LOP_REM_LONG_notSpecial1
    cmpl     $0x80000000,%ecx
    jne      .LOP_REM_LONG_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $0,%edx
    jmp      .LOP_REM_LONG_finish

/* continuation for OP_SHL_LONG */

.LOP_SHL_LONG_finish:
    SET_VREG_WORD %eax rINST 0          # v[AA+0]<- %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_SHR_LONG */


.LOP_SHR_LONG_finish:
    SET_VREG_WORD %eax rINST 0          # v[AA+0]<- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_USHR_LONG */


.LOP_USHR_LONG_finish:
    SET_VREG_WORD %eax rINST 0         # v[BB+0]<- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_DIV_INT_2ADDR */
.LOP_DIV_INT_2ADDR_continue_div2addr:
    cltd
    idivl   %ecx
.LOP_DIV_INT_2ADDR_finish_div2addr:
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* continuation for OP_REM_INT_2ADDR */
.LOP_REM_INT_2ADDR_continue_div2addr:
    cltd
    idivl   %ecx
.LOP_REM_INT_2ADDR_finish_div2addr:
    SET_VREG %edx rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* continuation for OP_MUL_LONG_2ADDR */

.LOP_MUL_LONG_2ADDR_continue:
    leal      (%ecx,%edx),%edx         # full result now in %edx:%eax
    movl      %edx,4(%esi)             # v[A+1]<- %edx
    movl      %eax,(%esi)              # v[A]<- %eax
    UNSPILL_TMP2(%esi)
    FETCH_INST_OPCODE 1 %ecx
    UNSPILL(rFP)
    ADVANCE_PC 1
    GOTO_NEXT_R %ecx

/* continuation for OP_DIV_LONG_2ADDR */

.LOP_DIV_LONG_2ADDR_continue:
    movl     %eax,OUT_ARG3(%esp)
    movl     %edx,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    call     __divdi3
.LOP_DIV_LONG_2ADDR_finish:
    SET_VREG_WORD %edx rINST 1
    SET_VREG_WORD %eax rINST 0
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.LOP_DIV_LONG_2ADDR_check_zero:
    testl   %edx,%edx
    jne     .LOP_DIV_LONG_2ADDR_notSpecial
    jmp     common_errDivideByZero
.LOP_DIV_LONG_2ADDR_check_neg1:
    testl   %edx,%eax
    jne     .LOP_DIV_LONG_2ADDR_notSpecial
    GET_VREG_WORD %edx rINST 0
    GET_VREG_WORD %ecx rINST 1
    testl    %edx,%edx
    jne      .LOP_DIV_LONG_2ADDR_notSpecial1
    cmpl     $0x80000000,%ecx
    jne      .LOP_DIV_LONG_2ADDR_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $0x80000000,%edx
    jmp      .LOP_DIV_LONG_2ADDR_finish

/* continuation for OP_REM_LONG_2ADDR */

.LOP_REM_LONG_2ADDR_continue:
    movl     %eax,OUT_ARG3(%esp)
    movl     %edx,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    call     __moddi3
.LOP_REM_LONG_2ADDR_finish:
    SET_VREG_WORD %edx rINST 1
    SET_VREG_WORD %eax rINST 0
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.LOP_REM_LONG_2ADDR_check_zero:
    testl   %edx,%edx
    jne     .LOP_REM_LONG_2ADDR_notSpecial
    jmp     common_errDivideByZero
.LOP_REM_LONG_2ADDR_check_neg1:
    testl   %edx,%eax
    jne     .LOP_REM_LONG_2ADDR_notSpecial
    GET_VREG_WORD %edx rINST 0
    GET_VREG_WORD %ecx rINST 1
    testl    %edx,%edx
    jne      .LOP_REM_LONG_2ADDR_notSpecial1
    cmpl     $0x80000000,%ecx
    jne      .LOP_REM_LONG_2ADDR_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $0,%edx
    jmp      .LOP_REM_LONG_2ADDR_finish

/* continuation for OP_SHL_LONG_2ADDR */


.LOP_SHL_LONG_2ADDR_finish:
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0         # v[AA+0]<- eax
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* continuation for OP_SHR_LONG_2ADDR */


.LOP_SHR_LONG_2ADDR_finish:
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0    # v[AA+0]<- eax
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* continuation for OP_USHR_LONG_2ADDR */


.LOP_USHR_LONG_2ADDR_finish:
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0         # v[AA+0]<- eax
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

/* continuation for OP_DIV_INT_LIT16 */
.LOP_DIV_INT_LIT16_continue_div:
    cltd
    idivl   %ecx
.LOP_DIV_INT_LIT16_finish_div:
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_REM_INT_LIT16 */
.LOP_REM_INT_LIT16_continue_div:
    cltd
    idivl   %ecx
.LOP_REM_INT_LIT16_finish_div:
    SET_VREG %edx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_DIV_INT_LIT8 */
.LOP_DIV_INT_LIT8_continue_div:
    cltd
    idivl   %ecx
.LOP_DIV_INT_LIT8_finish_div:
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_REM_INT_LIT8 */
.LOP_REM_INT_LIT8_continue_div:
    cltd
    idivl   %ecx
.LOP_REM_INT_LIT8_finish_div:
    SET_VREG %edx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_VOLATILE */


.LOP_IGET_VOLATILE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_VOLATILE_finish
    jmp     common_exceptionThrown

.LOP_IGET_VOLATILE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_VOLATILE */


.LOP_IPUT_VOLATILE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_VOLATILE_finish
    jmp     common_exceptionThrown

.LOP_IPUT_VOLATILE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                       # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 2 %edx
    movl   rINST,(%ecx,%eax,1)            # obj.field <- v[A](8/16/32 bits)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_SGET_VOLATILE */

    /*
     * Go resolve the field
     */
.LOP_SGET_VOLATILE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_VOLATILE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_VOLATILE */

    /*
     * Go resolve the field
     */
.LOP_SPUT_VOLATILE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_VOLATILE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_IGET_OBJECT_VOLATILE */


.LOP_IGET_OBJECT_VOLATILE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_OBJECT_VOLATILE_finish
    jmp     common_exceptionThrown

.LOP_IGET_OBJECT_VOLATILE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds A
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- A
    FETCH_INST_OPCODE 2 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_EXECUTE_INLINE */

.LOP_EXECUTE_INLINE_continue:
    /*
     * Extract args, call function.
     *  ecx = #of args (0-4)
     *  eax = call index
     *  @esp = return addr
     *  esp is -4 from normal
     *
     *  Go ahead and load all 4 args, even if not used.
     */
    movzwl    4(rPC),%edx

    movl      $0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $4,%edx
    movl      %ecx,4+OUT_ARG0(%esp)

    movl      $0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $4,%edx
    movl      %ecx,4+OUT_ARG1(%esp)

    movl      $0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $4,%edx
    movl      %ecx,4+OUT_ARG2(%esp)

    movl      $0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $4,%edx
    movl      %ecx,4+OUT_ARG3(%esp)

    sall      $4,%eax      # index *= sizeof(table entry)
    jmp       *gDvmInlineOpsTable(%eax)
    # will return to caller of .LOP_EXECUTE_INLINE_continue

/* continuation for OP_IPUT_OBJECT_QUICK */

.LOP_IPUT_OBJECT_QUICK_finish:
    testl     rINST,rINST               # did we store null?
    FETCH_INST_OPCODE 2 %edx
    movl      offThread_cardTable(%eax),%eax  # get card table base
    je        1f                            # skip card mark if null store
    shrl      $GC_CARD_SHIFT,%ecx          # object head to card number
    movb      %al,(%eax,%ecx)               # mark card based on object head
1:
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_OBJECT_VOLATILE */


.LOP_IPUT_OBJECT_VOLATILE_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_OBJECT_VOLATILE_finish
    jmp     common_exceptionThrown

.LOP_IPUT_OBJECT_VOLATILE_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   %edx is scratch, but needs to be unspilled
     *   rINST holds A
     */
    GET_VREG_R rINST rINST                      # rINST<- v[A]
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl    rINST,(%ecx,%eax)      # obj.field <- v[A](8/16/32 bits)
    movl    rSELF,%eax
    testl   rINST,rINST                         # stored a NULL?
    movl    offThread_cardTable(%eax),%eax        # get card table base
    FETCH_INST_OPCODE 2 %edx
    je      1f                                  # skip card mark if null store
    shrl    $GC_CARD_SHIFT,%ecx                # object head to card number
    movb    %al,(%eax,%ecx)                     # mark card using object head
1:
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

/* continuation for OP_SGET_OBJECT_VOLATILE */

    /*
     * Go resolve the field
     */
.LOP_SGET_OBJECT_VOLATILE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_OBJECT_VOLATILE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_OBJECT_VOLATILE */


.LOP_SPUT_OBJECT_VOLATILE_continue:
    movl      %ecx,offStaticField_value(%eax)    # do the store
    testl     %ecx,%ecx                          # stored null object ptr?
    FETCH_INST_OPCODE 2 %edx
    je        1f                                 # skip card mark if null
    movl      rSELF,%ecx
    movl      offField_clazz(%eax),%eax          # eax<- method->clazz
    movl      offThread_cardTable(%ecx),%ecx       # get card table base
    shrl      $GC_CARD_SHIFT,%eax               # head to card number
    movb      %cl,(%ecx,%eax)                    # mark card
1:
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.LOP_SPUT_OBJECT_VOLATILE_resolve:
    movl     rSELF,%ecx
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_OBJECT_VOLATILE_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_CONST_CLASS_JUMBO */

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.LOP_CONST_CLASS_JUMBO_resolve:
    movl     rSELF,%eax
    EXPORT_PC
    movl     offThread_method(%eax),%eax # eax<- self->method
    movl     $1,OUT_ARG2(%esp)        # true
    movl     2(rPC),%ecx               # ecx<- AAAAAAAA
    movl     offMethod_clazz(%eax),%eax
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveClass           # go resolve
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 4 %edx
    ADVANCE_PC 4
    GOTO_NEXT_R %edx

/* continuation for OP_CHECK_CAST_JUMBO */

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  ecx holds obj->clazz
     *  eax holds class resolved from AAAAAAAA
     *  rINST holds object
     */
.LOP_CHECK_CAST_JUMBO_fullcheck:
    movl    %eax,sReg0                 # we'll need the desired class on failure
    movl    %eax,OUT_ARG1(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmInstanceofNonTrivial    # eax<- boolean result
    testl   %eax,%eax                  # failed?
    jne     .LOP_CHECK_CAST_JUMBO_okay           # no, success

    # A cast has failed.  We need to throw a ClassCastException.
    EXPORT_PC
    movl    offObject_clazz(rINST),%eax
    movl    %eax,OUT_ARG0(%esp)                 # arg0<- obj->clazz
    movl    sReg0,%ecx
    movl    %ecx,OUT_ARG1(%esp)                 # arg1<- desired class
    call    dvmThrowClassCastException
    jmp     common_exceptionThrown

    /*
     * Resolution required.  This is the least-likely path, and we're
     * going to have to recreate some data.
     *
     *  rINST holds object
     */
.LOP_CHECK_CAST_JUMBO_resolve:
    movl    rSELF,%ecx
    EXPORT_PC
    movl    2(rPC),%eax                # eax<- AAAAAAAA
    movl    offThread_method(%ecx),%ecx  # ecx<- self->method
    movl    %eax,OUT_ARG1(%esp)        # arg1<- AAAAAAAA
    movl    offMethod_clazz(%ecx),%ecx # ecx<- metho->clazz
    movl    $0,OUT_ARG2(%esp)         # arg2<- false
    movl    %ecx,OUT_ARG0(%esp)        # arg0<- method->clazz
    call    dvmResolveClass            # eax<- resolved ClassObject ptr
    testl   %eax,%eax                  # got null?
    je      common_exceptionThrown     # yes, handle exception
    movl    offObject_clazz(rINST),%ecx  # ecx<- obj->clazz
    jmp     .LOP_CHECK_CAST_JUMBO_resolved       # pick up where we left off

/* continuation for OP_INSTANCE_OF_JUMBO */

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  eax holds obj->clazz
     *  ecx holds class resolved from BBBB
     *  rINST has BA
     */
.LOP_INSTANCE_OF_JUMBO_fullcheck:
    movl    %eax,OUT_ARG0(%esp)
    movl    %ecx,OUT_ARG1(%esp)
    call    dvmInstanceofNonTrivial     # eax<- boolean result
    # fall through to OP_INSTANCE_OF_JUMBO_store

    /*
     * eax holds boolean result
     * rINST holds BBBB
     */
.LOP_INSTANCE_OF_JUMBO_store:
    FETCH_INST_OPCODE 5 %edx
    ADVANCE_PC 5
    SET_VREG %eax rINST                 # vBBBB<- eax
    GOTO_NEXT_R %edx

    /*
     * Trivial test succeeded, save and bail.
     *  r9 holds BBBB
     */
.LOP_INSTANCE_OF_JUMBO_trivial:
    FETCH_INST_OPCODE 5 %edx
    ADVANCE_PC 5
    movl    $1,%eax
    SET_VREG %eax rINST                 # vBBBB<- true
    GOTO_NEXT_R %edx

    /*
     * Resolution required.  This is the least-likely path.
     *
     *  edx holds AAAAAAAA
     */
.LOP_INSTANCE_OF_JUMBO_resolve:
    movl    %edx,OUT_ARG1(%esp)         # arg1<- AAAAAAAA
    movl    rSELF,%ecx
    movl    offThread_method(%ecx),%ecx
    movl    $1,OUT_ARG2(%esp)          # arg2<- true
    movl    offMethod_clazz(%ecx),%ecx  # ecx<- method->clazz
    EXPORT_PC
    movl    %ecx,OUT_ARG0(%esp)         # arg0<- method->clazz
    call    dvmResolveClass             # eax<- resolved ClassObject ptr
    testl   %eax,%eax                   # success?
    je      common_exceptionThrown      # no, handle exception
/* Now, we need to sync up with fast path.  We need eax to
 * hold the obj->clazz, and ecx to hold the resolved class
 */
    movl    %eax,%ecx                   # ecx<- resolved class
    movzwl  8(rPC),%eax                 # eax<- CCCC
    GET_VREG_R %eax %eax                # eax<- vCCCC (obj)
    movl    offObject_clazz(%eax),%eax  # eax<- obj->clazz
    jmp     .LOP_INSTANCE_OF_JUMBO_resolved

/* continuation for OP_NEW_INSTANCE_JUMBO */

.LOP_NEW_INSTANCE_JUMBO_initialized:  # on entry, ecx<- class
    /* TODO: remove test for interface/abstract, now done in verifier */
    testl     $(ACC_INTERFACE|ACC_ABSTRACT),offClassObject_accessFlags(%ecx)
    movl      $ALLOC_DONT_TRACK,OUT_ARG1(%esp)
    jne       .LOP_NEW_INSTANCE_JUMBO_abstract
.LOP_NEW_INSTANCE_JUMBO_finish: # ecx=class
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmAllocObject             # eax<- new object
    FETCH_INST_OPCODE 4 %edx
    testl    %eax,%eax                  # success?
    je       common_exceptionThrown     # no, bail out
    SET_VREG %eax rINST
    ADVANCE_PC 4
    GOTO_NEXT_R %edx

    /*
     * Class initialization required.
     *
     *  ecx holds class object
     */
.LOP_NEW_INSTANCE_JUMBO_needinit:
    SPILL_TMP1(%ecx)                    # save object
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmInitClass                # initialize class
    UNSPILL_TMP1(%ecx)                  # restore object
    testl   %eax,%eax                   # success?
    jne     .LOP_NEW_INSTANCE_JUMBO_initialized     # success, continue
    jmp     common_exceptionThrown      # go deal with init exception

    /*
     * Resolution required.  This is the least-likely path.
     *
     */
.LOP_NEW_INSTANCE_JUMBO_resolve:
    movl    rSELF,%ecx
    movl    2(rPC),%eax                 # eax<- AAAAAAAA
    movl    offThread_method(%ecx),%ecx   # ecx<- self->method
    movl    %eax,OUT_ARG1(%esp)
    movl    offMethod_clazz(%ecx),%ecx  # ecx<- method->clazz
    movl    $0,OUT_ARG2(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmResolveClass             # call(clazz,off,flags)
    movl    %eax,%ecx                   # ecx<- resolved ClassObject ptr
    testl   %ecx,%ecx                   # success?
    jne     .LOP_NEW_INSTANCE_JUMBO_resolved        # good to go
    jmp     common_exceptionThrown      # no, handle exception

    /*
     * TODO: remove this
     * We can't instantiate an abstract class or interface, so throw an
     * InstantiationError with the class descriptor as the message.
     *
     *  ecx holds class object
     */
.LOP_NEW_INSTANCE_JUMBO_abstract:
    movl    offClassObject_descriptor(%ecx),%eax
    movl    $.LstrInstantiationError,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowExceptionWithClassMessage
    jmp     common_exceptionThrown

/* continuation for OP_NEW_ARRAY_JUMBO */

    /*
     * Resolve class.  (This is an uncommon case.)
     *  ecx holds class (null here)
     *  eax holds array length (vCCCC)
     */
.LOP_NEW_ARRAY_JUMBO_resolve:
    movl    rSELF,%ecx
    SPILL_TMP1(%eax)                   # save array length
    movl    offThread_method(%ecx),%ecx  # ecx<- self->method
    movl    2(rPC),%eax                # eax<- AAAAAAAA
    movl    offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
    movl    %eax,OUT_ARG1(%esp)
    movl    $0,OUT_ARG2(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmResolveClass            # eax<- call(clazz,ref,flag)
    movl    %eax,%ecx
    UNSPILL_TMP1(%eax)
    testl   %ecx,%ecx                  # successful resolution?
    je      common_exceptionThrown     # no, bail.
# fall through to OP_NEW_ARRAY_JUMBO_finish

    /*
     * Finish allocation
     *
     * ecx holds class
     * eax holds array length (vCCCC)
     */
.LOP_NEW_ARRAY_JUMBO_finish:
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    movl    $ALLOC_DONT_TRACK,OUT_ARG2(%esp)
    call    dvmAllocArrayByClass    # eax<- call(clazz,length,flags)
    FETCH_INST_OPCODE 5 %edx
    testl   %eax,%eax               # failed?
    je      common_exceptionThrown  # yup - go handle
    SET_VREG %eax rINST
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_FILLED_NEW_ARRAY_JUMBO */

.LOP_FILLED_NEW_ARRAY_JUMBO_more:
    movl    offMethod_clazz(%eax),%eax        # eax<- method->clazz
    movl    %eax,OUT_ARG0(%esp)               # arg0<- clazz
    call    dvmResolveClass                   # eax<- call(clazz,ref,flag)
    testl   %eax,%eax                         # null?
    je      common_exceptionThrown            # yes, handle it

       # note: fall through to .LOP_FILLED_NEW_ARRAY_JUMBO_continue

    /*
     * On entry:
     *    eax holds array class [r0]
     *    ecx is scratch
     */
.LOP_FILLED_NEW_ARRAY_JUMBO_continue:
    movl    offClassObject_descriptor(%eax),%ecx  # ecx<- arrayClass->descriptor
    movl    $ALLOC_DONT_TRACK,OUT_ARG2(%esp)     # arg2<- flags
    movzbl  1(%ecx),%ecx                          # ecx<- descriptor[1]
    movl    %eax,OUT_ARG0(%esp)                   # arg0<- arrayClass
    movl    rSELF,%eax
    cmpb    $'I',%cl                             # supported?
    je      1f
    cmpb    $'L',%cl
    je      1f
    cmpb    $'[',%cl
    jne      .LOP_FILLED_NEW_ARRAY_JUMBO_notimpl                  # no, not handled yet
1:
    movl    %ecx,offThread_retval+4(%eax)           # save type
    movl    rINST,OUT_ARG1(%esp)                  # arg1<- BBBB (length)
    call    dvmAllocArrayByClass     # eax<- call(arrayClass, length, flags)
    movl    rSELF,%ecx
    testl   %eax,%eax                             # alloc successful?
    je      common_exceptionThrown                # no, handle exception
    movl    %eax,offThread_retval(%ecx)             # retval.l<- new array
    movzwl  8(rPC),%ecx                           # ecx<- CCCC
    leal    offArrayObject_contents(%eax),%eax    # eax<- newArray->contents

/* at this point:
 *     eax is pointer to tgt
 *     rINST is length
 *     ecx is CCCC
 *  We now need to copy values from registers into the array
 */

    # set up src pointer
    SPILL_TMP2(%esi)
    SPILL_TMP3(%edi)
    leal    (rFP,%ecx,4),%esi # set up src ptr
    movl    %eax,%edi         # set up dst ptr
    movl    rINST,%ecx        # load count register
    rep
    movsd
    UNSPILL_TMP2(%esi)
    UNSPILL_TMP3(%edi)
    movl    rSELF,%ecx
    movl    offThread_retval+4(%ecx),%eax      # eax<- type
    FETCH_INST_OPCODE 5 %edx

    cmpb    $'I',%al                        # Int array?
    je      5f                               # skip card mark if so
    movl    offThread_retval(%ecx),%eax        # eax<- object head
    movl    offThread_cardTable(%ecx),%ecx     # card table base
    shrl    $GC_CARD_SHIFT,%eax             # convert to card num
    movb    %cl,(%ecx,%eax)                  # mark card based on object head
5:
    ADVANCE_PC 5
    GOTO_NEXT_R %edx


    /*
     * Throw an exception indicating that we have not implemented this
     * mode of filled-new-array.
     */
.LOP_FILLED_NEW_ARRAY_JUMBO_notimpl:
    movl    $.LstrInternalErrorA,%eax
    movl    %eax,OUT_ARG0(%esp)
    movl    $.LstrFilledNewArrayNotImplA,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

/* continuation for OP_IGET_JUMBO */


.LOP_IGET_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- BBBB
    FETCH_INST_OPCODE 5 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_WIDE_JUMBO */


.LOP_IGET_WIDE_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save objpointer across call
    movl    rPC,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IGET_WIDE_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_WIDE_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    leal    (%ecx,%eax,1),%eax                  # eax<- address of field
    movl    (%eax),%ecx                         # ecx<- lsw
    movl    4(%eax),%eax                        # eax<- msw
    FETCH_INST_OPCODE 5 %edx
    SET_VREG_WORD %ecx rINST 0
    SET_VREG_WORD %eax rINST 1
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_OBJECT_JUMBO */


.LOP_IGET_OBJECT_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_OBJECT_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_OBJECT_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- BBBB
    FETCH_INST_OPCODE 5 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_BOOLEAN_JUMBO */


.LOP_IGET_BOOLEAN_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_BOOLEAN_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_BOOLEAN_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movzbl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- BBBB
    FETCH_INST_OPCODE 5 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_BYTE_JUMBO */


.LOP_IGET_BYTE_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_BYTE_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_BYTE_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movsbl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- BBBB
    FETCH_INST_OPCODE 5 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_CHAR_JUMBO */


.LOP_IGET_CHAR_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_CHAR_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_CHAR_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movzwl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- BBBB
    FETCH_INST_OPCODE 5 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IGET_SHORT_JUMBO */


.LOP_IGET_SHORT_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                  # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  returns InstrField ptr
    jne     .LOP_IGET_SHORT_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IGET_SHORT_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movswl   (%ecx,%eax,1),%ecx                  # ecx<- obj.field (8/16/32 bits)
    movl    rINST,%eax                          # eax<- BBBB
    FETCH_INST_OPCODE 5 %edx
    SET_VREG %ecx %eax
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_JUMBO */


.LOP_IPUT_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    GET_VREG_R rINST rINST                       # rINST<- v[BBBB]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 5 %edx
    movl   rINST,(%ecx,%eax,1)            # obj.field <- v[BBBB](8/16/32 bits)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_WIDE_JUMBO */


.LOP_IPUT_WIDE_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  ... which returns InstrField ptr
    jne     .LOP_IPUT_WIDE_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_WIDE_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   %edx is scratch, but needs to be unspilled
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    leal    (%ecx,%eax,1),%eax                  # eax<- address of field
    GET_VREG_WORD %ecx rINST 0                  # ecx<- lsw
    GET_VREG_WORD rINST rINST 1                 # rINST<- msw
    FETCH_INST_OPCODE 5 %edx
    movl    rINST,4(%eax)
    movl    %ecx,(%eax)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_OBJECT_JUMBO */


.LOP_IPUT_OBJECT_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_OBJECT_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_OBJECT_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   %edx is scratch, but needs to be unspilled
     *   rINST holds BBBB
     */
    GET_VREG_R rINST rINST                      # rINST<- v[BBBB]
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    movl    rINST,(%ecx,%eax)      # obj.field <- v[BBBB](8/16/32 bits)
    movl    rSELF,%eax
    testl   rINST,rINST                         # stored a NULL?
    movl    offThread_cardTable(%eax),%eax        # get card table base
    FETCH_INST_OPCODE 5 %edx
    je      1f                                  # skip card mark if null store
    shrl    $GC_CARD_SHIFT,%ecx                # object head to card number
    movb    %al,(%eax,%ecx)                     # mark card using object head
1:
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_BOOLEAN_JUMBO */


.LOP_IPUT_BOOLEAN_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_BOOLEAN_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_BOOLEAN_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    GET_VREG_R rINST rINST                       # rINST<- v[BBBB]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 5 %edx
    movb   rINSTbl,(%ecx,%eax,1)            # obj.field <- v[BBBB](8/16/32 bits)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_BYTE_JUMBO */


.LOP_IPUT_BYTE_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_BYTE_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_BYTE_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    GET_VREG_R rINST rINST                       # rINST<- v[BBBB]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 5 %edx
    movb   rINSTbl,(%ecx,%eax,1)            # obj.field <- v[BBBB](8/16/32 bits)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_CHAR_JUMBO */


.LOP_IPUT_CHAR_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_CHAR_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_CHAR_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    GET_VREG_R rINST rINST                       # rINST<- v[BBBB]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 5 %edx
    movw   rINSTw,(%ecx,%eax,1)            # obj.field <- v[BBBB](8/16/32 bits)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_IPUT_SHORT_JUMBO */


.LOP_IPUT_SHORT_JUMBO_resolve:
    EXPORT_PC
    movl    offThread_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .LOP_IPUT_SHORT_JUMBO_finish
    jmp     common_exceptionThrown

.LOP_IPUT_SHORT_JUMBO_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    GET_VREG_R rINST rINST                       # rINST<- v[BBBB]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    FETCH_INST_OPCODE 5 %edx
    movw   rINSTw,(%ecx,%eax,1)            # obj.field <- v[BBBB](8/16/32 bits)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx

/* continuation for OP_SGET_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_WIDE_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_WIDE_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_WIDE_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_OBJECT_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_OBJECT_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_OBJECT_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_BOOLEAN_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_BOOLEAN_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_BOOLEAN_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_BYTE_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_BYTE_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_BYTE_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_CHAR_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_CHAR_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_CHAR_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SGET_SHORT_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SGET_SHORT_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SGET_SHORT_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SPUT_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_WIDE_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SPUT_WIDE_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_WIDE_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_OBJECT_JUMBO */


.LOP_SPUT_OBJECT_JUMBO_continue:
    movl      %ecx,offStaticField_value(%eax)    # do the store
    testl     %ecx,%ecx                          # stored null object ptr?
    FETCH_INST_OPCODE 4 %edx
    je        1f                                 # skip card mark if null
    movl      rSELF,%ecx
    movl      offField_clazz(%eax),%eax          # eax<- method->clazz
    movl      offThread_cardTable(%ecx),%ecx       # get card table base
    shrl      $GC_CARD_SHIFT,%eax               # head to card number
    movb      %cl,(%ecx,%eax)                    # mark card
1:
    ADVANCE_PC 4
    GOTO_NEXT_R %edx

.LOP_SPUT_OBJECT_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_OBJECT_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_BOOLEAN_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SPUT_BOOLEAN_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_BOOLEAN_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_BYTE_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SPUT_BYTE_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_BYTE_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_CHAR_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SPUT_CHAR_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_CHAR_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_SPUT_SHORT_JUMBO */

    /*
     * Go resolve the field
     */
.LOP_SPUT_SHORT_JUMBO_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .LOP_SPUT_SHORT_JUMBO_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception

/* continuation for OP_INVOKE_VIRTUAL_JUMBO */


.LOP_INVOKE_VIRTUAL_JUMBO_more:
    movl      offMethod_clazz(%eax),%eax  # ecx<- method->clazz
    movl      %eax,OUT_ARG0(%esp)         # arg0<- clazz
    movl      $METHOD_VIRTUAL,OUT_ARG2(%esp) # arg2<- flags
    call      dvmResolveMethod            # eax<- call(clazz, ref, flags)
    testl     %eax,%eax                   # got null?
    jne       .LOP_INVOKE_VIRTUAL_JUMBO_continue        # no, continue
    jmp       common_exceptionThrown      # yes, handle exception

    /* At this point:
     *   eax = resolved base method
     *   ecx = scratch
     */
.LOP_INVOKE_VIRTUAL_JUMBO_continue:
    movzwl    8(rPC),%ecx               # ecx<- CCCC
    GET_VREG_R  %ecx %ecx               # ecx<- "this"
    movzwl    offMethod_methodIndex(%eax),%eax  # eax<- baseMethod->methodIndex
    testl     %ecx,%ecx                 # null this?
    je        common_errNullObject      # go if so
    movl      offObject_clazz(%ecx),%ecx  # ecx<- thisPtr->clazz
    movl      offClassObject_vtable(%ecx),%ecx # ecx<- thisPtr->clazz->vtable
    movl      (%ecx,%eax,4),%eax        # eax<- vtable[methodIndex]
    jmp       common_invokeMethodJumbo

/* continuation for OP_INVOKE_SUPER_JUMBO */

    /*
     * At this point:
     *  ecx = resolved base method [r0]
     *  eax = method->clazz [r9]
     */
.LOP_INVOKE_SUPER_JUMBO_continue:
    movl    offClassObject_super(%eax),%eax   # eax<- method->clazz->super
    movzwl  offMethod_methodIndex(%ecx),%ecx  # ecx<- baseMthod->methodIndex
    cmpl    offClassObject_vtableCount(%eax),%ecx # compare(methodIndex,vtableCount)
    jae     .LOP_INVOKE_SUPER_JUMBO_nsm           # method not present in superclass
    movl    offClassObject_vtable(%eax),%eax   # eax<- ...clazz->super->vtable
    movl    (%eax,%ecx,4),%eax        # eax<- vtable[methodIndex]
    jmp     common_invokeMethodJumbo


    /* At this point:
     * ecx = null (needs to be resolved base method)
     * eax = method->clazz
    */
.LOP_INVOKE_SUPER_JUMBO_resolve:
    SPILL_TMP1(%eax)                    # method->clazz
    movl    %eax,OUT_ARG0(%esp)         # arg0<- method->clazz
    movl    2(rPC),%ecx                 # ecx<- AAAAAAAA
    movl    $METHOD_VIRTUAL,OUT_ARG2(%esp)  # arg2<- resolver method type
    movl    %ecx,OUT_ARG1(%esp)         # arg1<- ref
    call    dvmResolveMethod            # eax<- call(clazz, ref, flags)
    testl   %eax,%eax                   # got null?
    movl    %eax,%ecx                   # ecx<- resolved base method
    UNSPILL_TMP1(%eax)                  # restore method->clazz
    jne     .LOP_INVOKE_SUPER_JUMBO_continue        # good to go - continue
    jmp     common_exceptionThrown      # handle exception

    /*
     * Throw a NoSuchMethodError with the method name as the message.
     *  ecx = resolved base method
     */
.LOP_INVOKE_SUPER_JUMBO_nsm:
    movl    offMethod_name(%ecx),%eax
    mov     %eax,OUT_ARG1(%esp)
    jmp     common_errNoSuchMethod

/* continuation for OP_INVOKE_DIRECT_JUMBO */

    /*
     * On entry:
     *   TMP_SPILL  <- "this" register
     * Things a bit ugly on this path, but it's the less
     * frequent one.  We'll have to do some reloading.
     */
.LOP_INVOKE_DIRECT_JUMBO_resolve:
     SPILL_TMP1(%ecx)
     movl     rSELF,%ecx
     movl     offThread_method(%ecx),%ecx  # ecx<- self->method
     movl     2(rPC),%eax      # reference AAAAAAAA
     movl     offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
     movl     $METHOD_DIRECT,OUT_ARG2(%esp)
     movl     %eax,OUT_ARG1(%esp)
     movl     %ecx,OUT_ARG0(%esp)
     call     dvmResolveMethod # eax<- call(clazz, ref, flags)
     UNSPILL_TMP1(%ecx)
     testl    %eax,%eax
     jne      .LOP_INVOKE_DIRECT_JUMBO_finish
     jmp      common_exceptionThrown

/* continuation for OP_INVOKE_STATIC_JUMBO */

.LOP_INVOKE_STATIC_JUMBO_continue:
    movl      $METHOD_STATIC,%eax
    movl      %eax,OUT_ARG2(%esp)       # arg2<- flags
    call      dvmResolveMethod          # call(clazz,ref,flags)
    testl     %eax,%eax                 # got null?
    jne       common_invokeMethodJumbo
    jmp       common_exceptionThrown

/* continuation for OP_INVOKE_INTERFACE_JUMBO */

.LOP_INVOKE_INTERFACE_JUMBO_continue:
    call       dvmFindInterfaceMethodInCache # eax<- call(class, ref, method, dex)
    testl      %eax,%eax
    je         common_exceptionThrown
    jmp        common_invokeMethodJumbo

    .size   dvmAsmSisterStart, .-dvmAsmSisterStart
    .global dvmAsmSisterEnd
dvmAsmSisterEnd:

/* File: x86/entry.S */
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


    .text
    .global dvmMterpStdRun
    .type   dvmMterpStdRun, %function
/*
 * bool dvmMterpStdRun(Thread* self)
 *
 * Interpreter entry point.  Returns changeInterp.
 *
 */
dvmMterpStdRun:
    movl    4(%esp), %ecx        # get incoming rSELF
    push    %ebp                 # save caller base pointer
    push    %ecx                 # save rSELF at (%ebp)
    movl    %esp, %ebp           # set our %ebp
/*
 * At this point we've allocated two slots on the stack
 * via push and stack is 8-byte aligned.  Allocate space
 * for 8 spill slots, 3 local slots, 5 arg slots + 2 slots for
 * padding to bring us to 16-byte alignment
 */
    subl    $(FRAME_SIZE-8), %esp

/* Spill callee save regs */
    movl    %edi,EDI_SPILL(%ebp)
    movl    %esi,ESI_SPILL(%ebp)
    movl    %ebx,EBX_SPILL(%ebp)

/* Set up "named" registers */
    movl    offThread_pc(%ecx),rPC
    movl    offThread_fp(%ecx),rFP

/* Remember %esp for future "longjmp" */
    movl    %esp,offThread_bailPtr(%ecx)

/* How to start? */
    movb    offThread_entryPoint(%ecx),%al

/* Normal start? */
    cmpb    $kInterpEntryInstr,%al
    jne     .Lnot_instr

   /* Normal case: start executing the instruction at rPC */
    FETCH_INST
    GOTO_NEXT

.Lnot_instr:
    /* Reset to normal case */
    movb   $kInterpEntryInstr,offThread_entryPoint(%ecx)
    cmpb   $kInterpEntryReturn,%al
    je     common_returnFromMethod
    cmpb   $kInterpEntryThrow,%al
    je     common_exceptionThrown
    movzx  %al,%eax
    movl   %eax,OUT_ARG1(%esp)
    movl   $.LstrBadEntryPoint,OUT_ARG0(%esp)
    call   printf
    call   dvmAbort
    /* Not reached */


    .global dvmMterpStdBail
    .type   dvmMterpStdBail, %function
/*
 * void dvmMterpStdBail(Thread* self, bool changeInterp)
 *
 * Restore the stack pointer and PC from the save point established on entry.
 * This is essentially the same as a longjmp, but should be cheaper.  The
 * last instruction causes us to return to whoever called dvmMterpStdRun.
 *
 * We're not going to build a standard frame here, so the arg accesses will
 * look a little strange.
 *
 * On entry:
 *  esp+4 (arg0)  Thread* self
 *  esp+8 (arg1)  bool changeInterp
 */
dvmMterpStdBail:
    movl    4(%esp),%ecx                 # grab self
    movl    8(%esp),%eax                 # changeInterp to return reg
    movl    offThread_bailPtr(%ecx),%esp   # Restore "setjmp" esp
    movl    %esp,%ebp
    addl    $(FRAME_SIZE-8), %ebp       # Restore %ebp at point of setjmp
    movl    EDI_SPILL(%ebp),%edi
    movl    ESI_SPILL(%ebp),%esi
    movl    EBX_SPILL(%ebp),%ebx
    movl    PREV_FP(%ebp),%ebp           # restore caller's ebp
    addl    $FRAME_SIZE,%esp                    # strip frame
    ret                                  # return to dvmMterpStdRun's caller


/*
 * Strings
 */
    .section    .rodata
.LstrBadEntryPoint:
    .asciz  "Bad entry point %d\n"


/*
 * FIXME: Should have the config/rebuild mechanism generate this
 * for targets that need it.
 */

/* Jump table */
dvmAsmInstructionJmpTable = .LdvmAsmInstructionJmpTable
.LdvmAsmInstructionJmpTable:
.long .L_OP_NOP
.long .L_OP_MOVE
.long .L_OP_MOVE_FROM16
.long .L_OP_MOVE_16
.long .L_OP_MOVE_WIDE
.long .L_OP_MOVE_WIDE_FROM16
.long .L_OP_MOVE_WIDE_16
.long .L_OP_MOVE_OBJECT
.long .L_OP_MOVE_OBJECT_FROM16
.long .L_OP_MOVE_OBJECT_16
.long .L_OP_MOVE_RESULT
.long .L_OP_MOVE_RESULT_WIDE
.long .L_OP_MOVE_RESULT_OBJECT
.long .L_OP_MOVE_EXCEPTION
.long .L_OP_RETURN_VOID
.long .L_OP_RETURN
.long .L_OP_RETURN_WIDE
.long .L_OP_RETURN_OBJECT
.long .L_OP_CONST_4
.long .L_OP_CONST_16
.long .L_OP_CONST
.long .L_OP_CONST_HIGH16
.long .L_OP_CONST_WIDE_16
.long .L_OP_CONST_WIDE_32
.long .L_OP_CONST_WIDE
.long .L_OP_CONST_WIDE_HIGH16
.long .L_OP_CONST_STRING
.long .L_OP_CONST_STRING_JUMBO
.long .L_OP_CONST_CLASS
.long .L_OP_MONITOR_ENTER
.long .L_OP_MONITOR_EXIT
.long .L_OP_CHECK_CAST
.long .L_OP_INSTANCE_OF
.long .L_OP_ARRAY_LENGTH
.long .L_OP_NEW_INSTANCE
.long .L_OP_NEW_ARRAY
.long .L_OP_FILLED_NEW_ARRAY
.long .L_OP_FILLED_NEW_ARRAY_RANGE
.long .L_OP_FILL_ARRAY_DATA
.long .L_OP_THROW
.long .L_OP_GOTO
.long .L_OP_GOTO_16
.long .L_OP_GOTO_32
.long .L_OP_PACKED_SWITCH
.long .L_OP_SPARSE_SWITCH
.long .L_OP_CMPL_FLOAT
.long .L_OP_CMPG_FLOAT
.long .L_OP_CMPL_DOUBLE
.long .L_OP_CMPG_DOUBLE
.long .L_OP_CMP_LONG
.long .L_OP_IF_EQ
.long .L_OP_IF_NE
.long .L_OP_IF_LT
.long .L_OP_IF_GE
.long .L_OP_IF_GT
.long .L_OP_IF_LE
.long .L_OP_IF_EQZ
.long .L_OP_IF_NEZ
.long .L_OP_IF_LTZ
.long .L_OP_IF_GEZ
.long .L_OP_IF_GTZ
.long .L_OP_IF_LEZ
.long .L_OP_UNUSED_3E
.long .L_OP_UNUSED_3F
.long .L_OP_UNUSED_40
.long .L_OP_UNUSED_41
.long .L_OP_UNUSED_42
.long .L_OP_UNUSED_43
.long .L_OP_AGET
.long .L_OP_AGET_WIDE
.long .L_OP_AGET_OBJECT
.long .L_OP_AGET_BOOLEAN
.long .L_OP_AGET_BYTE
.long .L_OP_AGET_CHAR
.long .L_OP_AGET_SHORT
.long .L_OP_APUT
.long .L_OP_APUT_WIDE
.long .L_OP_APUT_OBJECT
.long .L_OP_APUT_BOOLEAN
.long .L_OP_APUT_BYTE
.long .L_OP_APUT_CHAR
.long .L_OP_APUT_SHORT
.long .L_OP_IGET
.long .L_OP_IGET_WIDE
.long .L_OP_IGET_OBJECT
.long .L_OP_IGET_BOOLEAN
.long .L_OP_IGET_BYTE
.long .L_OP_IGET_CHAR
.long .L_OP_IGET_SHORT
.long .L_OP_IPUT
.long .L_OP_IPUT_WIDE
.long .L_OP_IPUT_OBJECT
.long .L_OP_IPUT_BOOLEAN
.long .L_OP_IPUT_BYTE
.long .L_OP_IPUT_CHAR
.long .L_OP_IPUT_SHORT
.long .L_OP_SGET
.long .L_OP_SGET_WIDE
.long .L_OP_SGET_OBJECT
.long .L_OP_SGET_BOOLEAN
.long .L_OP_SGET_BYTE
.long .L_OP_SGET_CHAR
.long .L_OP_SGET_SHORT
.long .L_OP_SPUT
.long .L_OP_SPUT_WIDE
.long .L_OP_SPUT_OBJECT
.long .L_OP_SPUT_BOOLEAN
.long .L_OP_SPUT_BYTE
.long .L_OP_SPUT_CHAR
.long .L_OP_SPUT_SHORT
.long .L_OP_INVOKE_VIRTUAL
.long .L_OP_INVOKE_SUPER
.long .L_OP_INVOKE_DIRECT
.long .L_OP_INVOKE_STATIC
.long .L_OP_INVOKE_INTERFACE
.long .L_OP_UNUSED_73
.long .L_OP_INVOKE_VIRTUAL_RANGE
.long .L_OP_INVOKE_SUPER_RANGE
.long .L_OP_INVOKE_DIRECT_RANGE
.long .L_OP_INVOKE_STATIC_RANGE
.long .L_OP_INVOKE_INTERFACE_RANGE
.long .L_OP_UNUSED_79
.long .L_OP_UNUSED_7A
.long .L_OP_NEG_INT
.long .L_OP_NOT_INT
.long .L_OP_NEG_LONG
.long .L_OP_NOT_LONG
.long .L_OP_NEG_FLOAT
.long .L_OP_NEG_DOUBLE
.long .L_OP_INT_TO_LONG
.long .L_OP_INT_TO_FLOAT
.long .L_OP_INT_TO_DOUBLE
.long .L_OP_LONG_TO_INT
.long .L_OP_LONG_TO_FLOAT
.long .L_OP_LONG_TO_DOUBLE
.long .L_OP_FLOAT_TO_INT
.long .L_OP_FLOAT_TO_LONG
.long .L_OP_FLOAT_TO_DOUBLE
.long .L_OP_DOUBLE_TO_INT
.long .L_OP_DOUBLE_TO_LONG
.long .L_OP_DOUBLE_TO_FLOAT
.long .L_OP_INT_TO_BYTE
.long .L_OP_INT_TO_CHAR
.long .L_OP_INT_TO_SHORT
.long .L_OP_ADD_INT
.long .L_OP_SUB_INT
.long .L_OP_MUL_INT
.long .L_OP_DIV_INT
.long .L_OP_REM_INT
.long .L_OP_AND_INT
.long .L_OP_OR_INT
.long .L_OP_XOR_INT
.long .L_OP_SHL_INT
.long .L_OP_SHR_INT
.long .L_OP_USHR_INT
.long .L_OP_ADD_LONG
.long .L_OP_SUB_LONG
.long .L_OP_MUL_LONG
.long .L_OP_DIV_LONG
.long .L_OP_REM_LONG
.long .L_OP_AND_LONG
.long .L_OP_OR_LONG
.long .L_OP_XOR_LONG
.long .L_OP_SHL_LONG
.long .L_OP_SHR_LONG
.long .L_OP_USHR_LONG
.long .L_OP_ADD_FLOAT
.long .L_OP_SUB_FLOAT
.long .L_OP_MUL_FLOAT
.long .L_OP_DIV_FLOAT
.long .L_OP_REM_FLOAT
.long .L_OP_ADD_DOUBLE
.long .L_OP_SUB_DOUBLE
.long .L_OP_MUL_DOUBLE
.long .L_OP_DIV_DOUBLE
.long .L_OP_REM_DOUBLE
.long .L_OP_ADD_INT_2ADDR
.long .L_OP_SUB_INT_2ADDR
.long .L_OP_MUL_INT_2ADDR
.long .L_OP_DIV_INT_2ADDR
.long .L_OP_REM_INT_2ADDR
.long .L_OP_AND_INT_2ADDR
.long .L_OP_OR_INT_2ADDR
.long .L_OP_XOR_INT_2ADDR
.long .L_OP_SHL_INT_2ADDR
.long .L_OP_SHR_INT_2ADDR
.long .L_OP_USHR_INT_2ADDR
.long .L_OP_ADD_LONG_2ADDR
.long .L_OP_SUB_LONG_2ADDR
.long .L_OP_MUL_LONG_2ADDR
.long .L_OP_DIV_LONG_2ADDR
.long .L_OP_REM_LONG_2ADDR
.long .L_OP_AND_LONG_2ADDR
.long .L_OP_OR_LONG_2ADDR
.long .L_OP_XOR_LONG_2ADDR
.long .L_OP_SHL_LONG_2ADDR
.long .L_OP_SHR_LONG_2ADDR
.long .L_OP_USHR_LONG_2ADDR
.long .L_OP_ADD_FLOAT_2ADDR
.long .L_OP_SUB_FLOAT_2ADDR
.long .L_OP_MUL_FLOAT_2ADDR
.long .L_OP_DIV_FLOAT_2ADDR
.long .L_OP_REM_FLOAT_2ADDR
.long .L_OP_ADD_DOUBLE_2ADDR
.long .L_OP_SUB_DOUBLE_2ADDR
.long .L_OP_MUL_DOUBLE_2ADDR
.long .L_OP_DIV_DOUBLE_2ADDR
.long .L_OP_REM_DOUBLE_2ADDR
.long .L_OP_ADD_INT_LIT16
.long .L_OP_RSUB_INT
.long .L_OP_MUL_INT_LIT16
.long .L_OP_DIV_INT_LIT16
.long .L_OP_REM_INT_LIT16
.long .L_OP_AND_INT_LIT16
.long .L_OP_OR_INT_LIT16
.long .L_OP_XOR_INT_LIT16
.long .L_OP_ADD_INT_LIT8
.long .L_OP_RSUB_INT_LIT8
.long .L_OP_MUL_INT_LIT8
.long .L_OP_DIV_INT_LIT8
.long .L_OP_REM_INT_LIT8
.long .L_OP_AND_INT_LIT8
.long .L_OP_OR_INT_LIT8
.long .L_OP_XOR_INT_LIT8
.long .L_OP_SHL_INT_LIT8
.long .L_OP_SHR_INT_LIT8
.long .L_OP_USHR_INT_LIT8
.long .L_OP_IGET_VOLATILE
.long .L_OP_IPUT_VOLATILE
.long .L_OP_SGET_VOLATILE
.long .L_OP_SPUT_VOLATILE
.long .L_OP_IGET_OBJECT_VOLATILE
.long .L_OP_IGET_WIDE_VOLATILE
.long .L_OP_IPUT_WIDE_VOLATILE
.long .L_OP_SGET_WIDE_VOLATILE
.long .L_OP_SPUT_WIDE_VOLATILE
.long .L_OP_BREAKPOINT
.long .L_OP_THROW_VERIFICATION_ERROR
.long .L_OP_EXECUTE_INLINE
.long .L_OP_EXECUTE_INLINE_RANGE
.long .L_OP_INVOKE_OBJECT_INIT
.long .L_OP_RETURN_VOID_BARRIER
.long .L_OP_IGET_QUICK
.long .L_OP_IGET_WIDE_QUICK
.long .L_OP_IGET_OBJECT_QUICK
.long .L_OP_IPUT_QUICK
.long .L_OP_IPUT_WIDE_QUICK
.long .L_OP_IPUT_OBJECT_QUICK
.long .L_OP_INVOKE_VIRTUAL_QUICK
.long .L_OP_INVOKE_VIRTUAL_QUICK_RANGE
.long .L_OP_INVOKE_SUPER_QUICK
.long .L_OP_INVOKE_SUPER_QUICK_RANGE
.long .L_OP_IPUT_OBJECT_VOLATILE
.long .L_OP_SGET_OBJECT_VOLATILE
.long .L_OP_SPUT_OBJECT_VOLATILE
.long .L_OP_DISPATCH_FF
.long .L_OP_CONST_CLASS_JUMBO
.long .L_OP_CHECK_CAST_JUMBO
.long .L_OP_INSTANCE_OF_JUMBO
.long .L_OP_NEW_INSTANCE_JUMBO
.long .L_OP_NEW_ARRAY_JUMBO
.long .L_OP_FILLED_NEW_ARRAY_JUMBO
.long .L_OP_IGET_JUMBO
.long .L_OP_IGET_WIDE_JUMBO
.long .L_OP_IGET_OBJECT_JUMBO
.long .L_OP_IGET_BOOLEAN_JUMBO
.long .L_OP_IGET_BYTE_JUMBO
.long .L_OP_IGET_CHAR_JUMBO
.long .L_OP_IGET_SHORT_JUMBO
.long .L_OP_IPUT_JUMBO
.long .L_OP_IPUT_WIDE_JUMBO
.long .L_OP_IPUT_OBJECT_JUMBO
.long .L_OP_IPUT_BOOLEAN_JUMBO
.long .L_OP_IPUT_BYTE_JUMBO
.long .L_OP_IPUT_CHAR_JUMBO
.long .L_OP_IPUT_SHORT_JUMBO
.long .L_OP_SGET_JUMBO
.long .L_OP_SGET_WIDE_JUMBO
.long .L_OP_SGET_OBJECT_JUMBO
.long .L_OP_SGET_BOOLEAN_JUMBO
.long .L_OP_SGET_BYTE_JUMBO
.long .L_OP_SGET_CHAR_JUMBO
.long .L_OP_SGET_SHORT_JUMBO
.long .L_OP_SPUT_JUMBO
.long .L_OP_SPUT_WIDE_JUMBO
.long .L_OP_SPUT_OBJECT_JUMBO
.long .L_OP_SPUT_BOOLEAN_JUMBO
.long .L_OP_SPUT_BYTE_JUMBO
.long .L_OP_SPUT_CHAR_JUMBO
.long .L_OP_SPUT_SHORT_JUMBO
.long .L_OP_INVOKE_VIRTUAL_JUMBO
.long .L_OP_INVOKE_SUPER_JUMBO
.long .L_OP_INVOKE_DIRECT_JUMBO
.long .L_OP_INVOKE_STATIC_JUMBO
.long .L_OP_INVOKE_INTERFACE_JUMBO
.long .L_OP_UNUSED_27FF
.long .L_OP_UNUSED_28FF
.long .L_OP_UNUSED_29FF
.long .L_OP_UNUSED_2AFF
.long .L_OP_UNUSED_2BFF
.long .L_OP_UNUSED_2CFF
.long .L_OP_UNUSED_2DFF
.long .L_OP_UNUSED_2EFF
.long .L_OP_UNUSED_2FFF
.long .L_OP_UNUSED_30FF
.long .L_OP_UNUSED_31FF
.long .L_OP_UNUSED_32FF
.long .L_OP_UNUSED_33FF
.long .L_OP_UNUSED_34FF
.long .L_OP_UNUSED_35FF
.long .L_OP_UNUSED_36FF
.long .L_OP_UNUSED_37FF
.long .L_OP_UNUSED_38FF
.long .L_OP_UNUSED_39FF
.long .L_OP_UNUSED_3AFF
.long .L_OP_UNUSED_3BFF
.long .L_OP_UNUSED_3CFF
.long .L_OP_UNUSED_3DFF
.long .L_OP_UNUSED_3EFF
.long .L_OP_UNUSED_3FFF
.long .L_OP_UNUSED_40FF
.long .L_OP_UNUSED_41FF
.long .L_OP_UNUSED_42FF
.long .L_OP_UNUSED_43FF
.long .L_OP_UNUSED_44FF
.long .L_OP_UNUSED_45FF
.long .L_OP_UNUSED_46FF
.long .L_OP_UNUSED_47FF
.long .L_OP_UNUSED_48FF
.long .L_OP_UNUSED_49FF
.long .L_OP_UNUSED_4AFF
.long .L_OP_UNUSED_4BFF
.long .L_OP_UNUSED_4CFF
.long .L_OP_UNUSED_4DFF
.long .L_OP_UNUSED_4EFF
.long .L_OP_UNUSED_4FFF
.long .L_OP_UNUSED_50FF
.long .L_OP_UNUSED_51FF
.long .L_OP_UNUSED_52FF
.long .L_OP_UNUSED_53FF
.long .L_OP_UNUSED_54FF
.long .L_OP_UNUSED_55FF
.long .L_OP_UNUSED_56FF
.long .L_OP_UNUSED_57FF
.long .L_OP_UNUSED_58FF
.long .L_OP_UNUSED_59FF
.long .L_OP_UNUSED_5AFF
.long .L_OP_UNUSED_5BFF
.long .L_OP_UNUSED_5CFF
.long .L_OP_UNUSED_5DFF
.long .L_OP_UNUSED_5EFF
.long .L_OP_UNUSED_5FFF
.long .L_OP_UNUSED_60FF
.long .L_OP_UNUSED_61FF
.long .L_OP_UNUSED_62FF
.long .L_OP_UNUSED_63FF
.long .L_OP_UNUSED_64FF
.long .L_OP_UNUSED_65FF
.long .L_OP_UNUSED_66FF
.long .L_OP_UNUSED_67FF
.long .L_OP_UNUSED_68FF
.long .L_OP_UNUSED_69FF
.long .L_OP_UNUSED_6AFF
.long .L_OP_UNUSED_6BFF
.long .L_OP_UNUSED_6CFF
.long .L_OP_UNUSED_6DFF
.long .L_OP_UNUSED_6EFF
.long .L_OP_UNUSED_6FFF
.long .L_OP_UNUSED_70FF
.long .L_OP_UNUSED_71FF
.long .L_OP_UNUSED_72FF
.long .L_OP_UNUSED_73FF
.long .L_OP_UNUSED_74FF
.long .L_OP_UNUSED_75FF
.long .L_OP_UNUSED_76FF
.long .L_OP_UNUSED_77FF
.long .L_OP_UNUSED_78FF
.long .L_OP_UNUSED_79FF
.long .L_OP_UNUSED_7AFF
.long .L_OP_UNUSED_7BFF
.long .L_OP_UNUSED_7CFF
.long .L_OP_UNUSED_7DFF
.long .L_OP_UNUSED_7EFF
.long .L_OP_UNUSED_7FFF
.long .L_OP_UNUSED_80FF
.long .L_OP_UNUSED_81FF
.long .L_OP_UNUSED_82FF
.long .L_OP_UNUSED_83FF
.long .L_OP_UNUSED_84FF
.long .L_OP_UNUSED_85FF
.long .L_OP_UNUSED_86FF
.long .L_OP_UNUSED_87FF
.long .L_OP_UNUSED_88FF
.long .L_OP_UNUSED_89FF
.long .L_OP_UNUSED_8AFF
.long .L_OP_UNUSED_8BFF
.long .L_OP_UNUSED_8CFF
.long .L_OP_UNUSED_8DFF
.long .L_OP_UNUSED_8EFF
.long .L_OP_UNUSED_8FFF
.long .L_OP_UNUSED_90FF
.long .L_OP_UNUSED_91FF
.long .L_OP_UNUSED_92FF
.long .L_OP_UNUSED_93FF
.long .L_OP_UNUSED_94FF
.long .L_OP_UNUSED_95FF
.long .L_OP_UNUSED_96FF
.long .L_OP_UNUSED_97FF
.long .L_OP_UNUSED_98FF
.long .L_OP_UNUSED_99FF
.long .L_OP_UNUSED_9AFF
.long .L_OP_UNUSED_9BFF
.long .L_OP_UNUSED_9CFF
.long .L_OP_UNUSED_9DFF
.long .L_OP_UNUSED_9EFF
.long .L_OP_UNUSED_9FFF
.long .L_OP_UNUSED_A0FF
.long .L_OP_UNUSED_A1FF
.long .L_OP_UNUSED_A2FF
.long .L_OP_UNUSED_A3FF
.long .L_OP_UNUSED_A4FF
.long .L_OP_UNUSED_A5FF
.long .L_OP_UNUSED_A6FF
.long .L_OP_UNUSED_A7FF
.long .L_OP_UNUSED_A8FF
.long .L_OP_UNUSED_A9FF
.long .L_OP_UNUSED_AAFF
.long .L_OP_UNUSED_ABFF
.long .L_OP_UNUSED_ACFF
.long .L_OP_UNUSED_ADFF
.long .L_OP_UNUSED_AEFF
.long .L_OP_UNUSED_AFFF
.long .L_OP_UNUSED_B0FF
.long .L_OP_UNUSED_B1FF
.long .L_OP_UNUSED_B2FF
.long .L_OP_UNUSED_B3FF
.long .L_OP_UNUSED_B4FF
.long .L_OP_UNUSED_B5FF
.long .L_OP_UNUSED_B6FF
.long .L_OP_UNUSED_B7FF
.long .L_OP_UNUSED_B8FF
.long .L_OP_UNUSED_B9FF
.long .L_OP_UNUSED_BAFF
.long .L_OP_UNUSED_BBFF
.long .L_OP_UNUSED_BCFF
.long .L_OP_UNUSED_BDFF
.long .L_OP_UNUSED_BEFF
.long .L_OP_UNUSED_BFFF
.long .L_OP_UNUSED_C0FF
.long .L_OP_UNUSED_C1FF
.long .L_OP_UNUSED_C2FF
.long .L_OP_UNUSED_C3FF
.long .L_OP_UNUSED_C4FF
.long .L_OP_UNUSED_C5FF
.long .L_OP_UNUSED_C6FF
.long .L_OP_UNUSED_C7FF
.long .L_OP_UNUSED_C8FF
.long .L_OP_UNUSED_C9FF
.long .L_OP_UNUSED_CAFF
.long .L_OP_UNUSED_CBFF
.long .L_OP_UNUSED_CCFF
.long .L_OP_UNUSED_CDFF
.long .L_OP_UNUSED_CEFF
.long .L_OP_UNUSED_CFFF
.long .L_OP_UNUSED_D0FF
.long .L_OP_UNUSED_D1FF
.long .L_OP_UNUSED_D2FF
.long .L_OP_UNUSED_D3FF
.long .L_OP_UNUSED_D4FF
.long .L_OP_UNUSED_D5FF
.long .L_OP_UNUSED_D6FF
.long .L_OP_UNUSED_D7FF
.long .L_OP_UNUSED_D8FF
.long .L_OP_UNUSED_D9FF
.long .L_OP_UNUSED_DAFF
.long .L_OP_UNUSED_DBFF
.long .L_OP_UNUSED_DCFF
.long .L_OP_UNUSED_DDFF
.long .L_OP_UNUSED_DEFF
.long .L_OP_UNUSED_DFFF
.long .L_OP_UNUSED_E0FF
.long .L_OP_UNUSED_E1FF
.long .L_OP_UNUSED_E2FF
.long .L_OP_UNUSED_E3FF
.long .L_OP_UNUSED_E4FF
.long .L_OP_UNUSED_E5FF
.long .L_OP_UNUSED_E6FF
.long .L_OP_UNUSED_E7FF
.long .L_OP_UNUSED_E8FF
.long .L_OP_UNUSED_E9FF
.long .L_OP_UNUSED_EAFF
.long .L_OP_UNUSED_EBFF
.long .L_OP_UNUSED_ECFF
.long .L_OP_UNUSED_EDFF
.long .L_OP_UNUSED_EEFF
.long .L_OP_UNUSED_EFFF
.long .L_OP_UNUSED_F0FF
.long .L_OP_UNUSED_F1FF
.long .L_OP_UNUSED_F2FF
.long .L_OP_UNUSED_F3FF
.long .L_OP_UNUSED_F4FF
.long .L_OP_UNUSED_F5FF
.long .L_OP_UNUSED_F6FF
.long .L_OP_UNUSED_F7FF
.long .L_OP_UNUSED_F8FF
.long .L_OP_UNUSED_F9FF
.long .L_OP_UNUSED_FAFF
.long .L_OP_UNUSED_FBFF
.long .L_OP_UNUSED_FCFF
.long .L_OP_UNUSED_FDFF
.long .L_OP_UNUSED_FEFF
.long .L_OP_THROW_VERIFICATION_ERROR_JUMBO

/* File: x86/footer.S */
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
 * Common subroutines and data.
 */

#if defined(WITH_JIT)
/*
 * JIT-related re-entries into the interpreter.  In general, if the
 * exit from a translation can at some point be chained, the entry
 * here requires that control arrived via a call, and that the "rp"
 * on TOS is actually a pointer to a 32-bit cell containing the Dalvik PC
 * of the next insn to handle.  If no chaining will happen, the entry
 * should be reached via a direct jump and rPC set beforehand.
 */

    .global dvmJitToInterpPunt
/*
 * The compiler will generate a jump to this entry point when it is
 * having difficulty translating a Dalvik instruction.  We must skip
 * the code cache lookup & prevent chaining to avoid bouncing between
 * the interpreter and code cache. rPC must be set on entry.
 */
dvmJitToInterpPunt:
#if defined(WITH_JIT_TUNING)
    movl   rPC, OUT_ARG0(%esp)
    call   dvmBumpPunt
#endif
    FETCH_INST_R %edx
    GOTO_NEXT_R %edx

    .global dvmJitToInterpSingleStep
/*
 * Return to the interpreter to handle a single instruction.
 * Should be reached via a call.
 * On entry:
 *   0(%esp)          <= native return address within trace
 *   rPC              <= Dalvik PC of this instruction
 *   OUT_ARG0+4(%esp) <= Dalvik PC of next instruction
 */
dvmJitToInterpSingleStep:
    pop    %eax
    movl   rSELF, %ecx
    movl   OUT_ARG0(%esp), %edx
    movl   %eax,offThread_jitResumeNPC(%ecx)
    movl   %edx,offThread_jitResumeDPC(%ecx)
    movl   $kInterpEntryInstr,offThread_entryPoint(%ecx)
    movl   $1,rINST     # changeInterp <= true
    jmp    common_gotoBail

    .global dvmJitToInterpNoChainNoProfile
/*
 * Return from the translation cache to the interpreter to do method
 * invocation.  Check if the translation exists for the callee, but don't
 * chain to it. rPC must be set on entry.
 */
dvmJitToInterpNoChainNoProfile:
#if defined(WITH_JIT_TUNING)
    call   dvmBumpNoChain
#endif
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr        # is there a translation?
    movl   rSELF,%ecx                # ecx <- self
    movl   %eax,offThread_inJitCodeCache(%ecx)  # set inJitCodeCache flag
    cmpl   $0, %eax
    jz     1f
    call   *%eax                     # exec translation if we've got one
    # won't return
1:
    FETCH_INST_R %edx
    GOTO_NEXT_R %edx

/*
 * Return from the translation cache and immediately request a
 * translation fro the exit target, but don't attempt to chain.
 * rPC set on entry.
 */
    .global dvmJitToInterpTraceSelectNoChain
dvmJitToInterpTraceSelectNoChain:
#if defined(WITH_JIT_TUNING)
    call   dvmBumpNoChain
#endif
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr # is there a translation?
    movl   rSELF,%ecx
    cmpl   $0,%eax
    movl   %eax,offThread_inJitCodeCache(%ecx)  # set inJitCodeCache flag
    jz     1f
    call   *%eax              # jump to tranlation
    # won't return

/* No Translation - request one */
1:
    GET_JIT_PROF_TABLE %ecx %eax
    cmpl   $0, %eax          # JIT enabled?
    jnz    2f                 # Request one if so
    FETCH_INST_R %edx         # Continue interpreting if not
    GOTO_NEXT_R %edx
2:
    movl   $kJitTSelectRequestHot,rINST  # ask for trace select
    jmp    common_selectTrace

/*
 * Return from the translation cache and immediately request a
 * translation for the exit target.  Reached via a call, and
 * (TOS)->rPC.
 */
    .global dvmJitToInterpTraceSelect
dvmJitToInterpTraceSelect:
    pop    rINST           # save chain cell address in callee save reg
    movl   (rINST),rPC
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr # is there a translation?
    cmpl   $0,%eax
    jz     1b                 # no - ask for one
    movl   %eax,OUT_ARG0(%esp)
# FIXME - need to adjust rINST to beginning of sequence
    movl   rINST,OUT_ARG1(%esp)
    call   dvmJitChain        # Attempt dvmJitChain(codeAddr,chainAddr)
    cmpl   $0,%eax           # Success?
    jz     toInterpreter      # didn't chain - interpret
    call   *%eax
    # won't return

/*
 * Placeholder entries for x86 JIT
 */
    .global dvmJitToInterpBackwardBranch
dvmJitToInterpBackwardBranch:
    .global dvmJitToInterpNormal
dvmJitToInterpNormal:
    .global dvmJitToInterpNoChain
dvmJitToInterpNoChain:
toInterpreter:
    jmp  common_abort
#endif

/*
 * Common code when a backwards branch is taken
 *
 * On entry:
 *   ebx (a.k.a. rINST) -> PC adjustment in 16-bit words
 */
common_backwardBranch:
    movl    rSELF,%ecx
    call   common_periodicChecks  # rPC and ecx/rSELF preserved
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE %ecx %edx
    ADVANCE_PC_INDEXED rINST
    cmpl   $0,%edx
    FETCH_INST
    jz    1f                    # Profiling off - continue
    .global updateProfile
updateProfile:
common_updateProfile:
    # quick & dirty hash
    movl   rPC, %eax
    shrl   $12, %eax
    xorl   rPC, %eax
    andl   $((1<<JIT_PROF_SIZE_LOG_2)-1),%eax
    decb   (%edx,%eax)
    jz     2f
1:
    GOTO_NEXT
2:
/*
 * Here, we switch to the debug interpreter to request
 * trace selection.  First, though, check to see if there
 * is already a native translation in place (and, if so,
 * jump to it now.
 */
    GET_JIT_THRESHOLD %ecx rINST  # leaves rSELF in %ecx
    EXPORT_PC
    movb   rINSTbl,(%edx,%eax)   # reset counter
    movl   %ecx,rINST            # preserve rSELF
    movl   rPC,OUT_ARG0(%esp)
    call   dvmJitGetTraceAddr  # already have one?
    movl   %eax,offThread_inJitCodeCache(rINST)   # set the inJitCodeCache flag
    cmpl   $0,%eax
    jz     1f
    call   *%eax        # FIXME: decide call vs/ jmp!.  No return either way
1:
    movl   $kJitTSelectRequest,%eax
    # On entry, eax<- jitState, rPC valid
common_selectTrace:

    movl   rSELF,%ecx
    movl   %eax,offThread_jitState(%ecx)
    movl   $kInterpEntryInstr,offThread_entryPoint(%ecx)
    movl   $1,rINST
    jmp    common_gotoBail
#else
    ADVANCE_PC_INDEXED rINST
    FETCH_INST
    GOTO_NEXT
#endif



/*
 * Common code for jumbo method invocation.
 *
 * On entry:
 *   eax = Method* methodToCall
 *   rINSTw trashed, must reload
 */

common_invokeMethodJumbo:
.LinvokeNewJumbo:

   /*
    * prepare to copy args to "outs" area of current frame
    */
    movzwl      6(rPC),rINST            # rINST<- BBBB
    movzwl      8(rPC), %ecx            # %ecx<- CCCC
    ADVANCE_PC 2                        # adjust pc to make return similar
    SAVEAREA_FROM_FP %edx               # %edx<- &StackSaveArea
    test        rINST, rINST
    movl        rINST, LOCAL0_OFFSET(%ebp) # LOCAL0_OFFSET(%ebp)<- BBBB
    jz          .LinvokeArgsDone        # no args; jump to args done
    jmp         .LinvokeRangeArgs       # handle args like invoke range

/*
 * Common code for method invocation with range.
 *
 * On entry:
 *   eax = Method* methodToCall
 *   rINSTw trashed, must reload
 */

common_invokeMethodRange:
.LinvokeNewRange:

   /*
    * prepare to copy args to "outs" area of current frame
    */

    movzbl      1(rPC),rINST       # rINST<- AA
    movzwl      4(rPC), %ecx            # %ecx<- CCCC
    SAVEAREA_FROM_FP %edx               # %edx<- &StackSaveArea
    test        rINST, rINST
    movl        rINST, LOCAL0_OFFSET(%ebp) # LOCAL0_OFFSET(%ebp)<- AA
    jz          .LinvokeArgsDone        # no args; jump to args done


   /*
    * %eax=methodToCall, %ecx=CCCC, LOCAL0_OFFSET(%ebp)=count, %edx=&outs (&stackSaveArea)
    * (very few methods have > 10 args; could unroll for common cases)
    */

.LinvokeRangeArgs:
    movl        %ebx, LOCAL1_OFFSET(%ebp)       # LOCAL1_OFFSET(%ebp)<- save %ebx
    lea         (rFP, %ecx, 4), %ecx    # %ecx<- &vCCCC
    shll        $2, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET(%ebp)<- offset
    subl        LOCAL0_OFFSET(%ebp), %edx       # %edx<- update &outs
    shrl        $2, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET(%ebp)<- offset
1:
    movl        (%ecx), %ebx            # %ebx<- vCCCC
    lea         4(%ecx), %ecx           # %ecx<- &vCCCC++
    subl        $1, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET<- LOCAL0_OFFSET--
    movl        %ebx, (%edx)            # *outs<- vCCCC
    lea         4(%edx), %edx           # outs++
    jne         1b                      # loop if count (LOCAL0_OFFSET(%ebp)) not zero
    movl        LOCAL1_OFFSET(%ebp), %ebx       # %ebx<- restore %ebx
    jmp         .LinvokeArgsDone        # continue

   /*
    * %eax is "Method* methodToCall", the method we're trying to call
    * prepare to copy args to "outs" area of current frame
    */

common_invokeMethodNoRange:
.LinvokeNewNoRange:
    movzbl      1(rPC),rINST       # rINST<- BA
    movl        rINST, LOCAL0_OFFSET(%ebp) # LOCAL0_OFFSET(%ebp)<- BA
    shrl        $4, LOCAL0_OFFSET(%ebp)        # LOCAL0_OFFSET(%ebp)<- B
    je          .LinvokeArgsDone        # no args; jump to args done
    movzwl      4(rPC), %ecx            # %ecx<- GFED
    SAVEAREA_FROM_FP %edx               # %edx<- &StackSaveArea

   /*
    * %eax=methodToCall, %ecx=GFED, LOCAL0_OFFSET(%ebp)=count, %edx=outs
    */

.LinvokeNonRange:
    cmp         $2, LOCAL0_OFFSET(%ebp)        # compare LOCAL0_OFFSET(%ebp) to 2
    movl        %ecx, LOCAL1_OFFSET(%ebp)       # LOCAL1_OFFSET(%ebp)<- GFED
    jl          1f                      # handle 1 arg
    je          2f                      # handle 2 args
    cmp         $4, LOCAL0_OFFSET(%ebp)        # compare LOCAL0_OFFSET(%ebp) to 4
    jl          3f                      # handle 3 args
    je          4f                      # handle 4 args
5:
    andl        $15, rINST             # rINSTw<- A
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, rINST, 4), %ecx   # %ecx<- vA
    movl        %ecx, (%edx)            # *outs<- vA
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
4:
    shr         $12, %ecx              # %ecx<- G
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vG
    movl        %ecx, (%edx)            # *outs<- vG
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
3:
    and         $0x0f00, %ecx          # %ecx<- 0F00
    shr         $8, %ecx               # %ecx<- F
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vF
    movl        %ecx, (%edx)            # *outs<- vF
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
2:
    and         $0x00f0, %ecx          # %ecx<- 00E0
    shr         $4, %ecx               # %ecx<- E
    lea         -4(%edx), %edx          # %edx<- update &outs; &outs--
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vE
    movl        %ecx, (%edx)            # *outs<- vE
    movl        LOCAL1_OFFSET(%ebp), %ecx       # %ecx<- GFED
1:
    and         $0x000f, %ecx          # %ecx<- 000D
    movl        (rFP, %ecx, 4), %ecx    # %ecx<- vD
    movl        %ecx, -4(%edx)          # *--outs<- vD
0:

   /*
    * %eax is "Method* methodToCall", the method we're trying to call
    * find space for the new stack frame, check for overflow
    */

.LinvokeArgsDone:
    movzwl      offMethod_registersSize(%eax), %edx # %edx<- methodToCall->regsSize
    movzwl      offMethod_outsSize(%eax), %ecx # %ecx<- methodToCall->outsSize
    movl        %eax, LOCAL0_OFFSET(%ebp)       # LOCAL0_OFFSET<- methodToCall
    shl         $2, %edx               # %edx<- update offset
    SAVEAREA_FROM_FP %eax               # %eax<- &StackSaveArea
    subl        %edx, %eax              # %eax<- newFP; (old savearea - regsSize)
    movl        rSELF,%edx              # %edx<- pthread
    movl        %eax, LOCAL1_OFFSET(%ebp)       # LOCAL1_OFFSET(%ebp)<- &outs
    subl        $sizeofStackSaveArea, %eax # %eax<- newSaveArea (stack save area using newFP)
    movl        offThread_interpStackEnd(%edx), %edx # %edx<- self->interpStackEnd
    movl        %edx, LOCAL2_OFFSET(%ebp)       # LOCAL2_OFFSET<- self->interpStackEnd
    shl         $2, %ecx               # %ecx<- update offset for outsSize
    movl        %eax, %edx              # %edx<- newSaveArea
    sub         %ecx, %eax              # %eax<- bottom; (newSaveArea - outsSize)
    cmp         LOCAL2_OFFSET(%ebp), %eax       # compare interpStackEnd and bottom
    movl        LOCAL0_OFFSET(%ebp), %eax       # %eax<- restore methodToCall
    jl          .LstackOverflow         # handle frame overflow

   /*
    * set up newSaveArea
    */

#ifdef EASY_GDB
    SAVEAREA_FROM_FP %ecx               # %ecx<- &StackSaveArea
    movl        %ecx, offStackSaveArea_prevSave(%edx) # newSaveArea->prevSave<- &outs
#endif
    movl        rFP, offStackSaveArea_prevFrame(%edx) # newSaveArea->prevFrame<- rFP
    movl        rPC, offStackSaveArea_savedPc(%edx) # newSaveArea->savedPc<- rPC
    testl       $ACC_NATIVE, offMethod_accessFlags(%eax) # check for native call
    movl        %eax, offStackSaveArea_method(%edx) # newSaveArea->method<- method to call
    jne         .LinvokeNative          # handle native call

   /*
    * Update "self" values for the new method
    * %eax=methodToCall, LOCAL1_OFFSET(%ebp)=newFp
    */

    movl        offMethod_clazz(%eax), %edx # %edx<- method->clazz
    movl        rSELF,%ecx                  # %ecx<- pthread
    movl        offClassObject_pDvmDex(%edx), %edx # %edx<- method->clazz->pDvmDex
    movl        %eax, offThread_method(%ecx) # self->method<- methodToCall
    movl        %edx, offThread_methodClassDex(%ecx) # self->methodClassDex<- method->clazz->pDvmDex
    movl        offMethod_insns(%eax), rPC # rPC<- methodToCall->insns
    movl        LOCAL1_OFFSET(%ebp), rFP # rFP<- newFP
    movl        rFP, offThread_curFrame(%ecx) # self->curFrame<- newFP
    FETCH_INST
    GOTO_NEXT                           # jump to methodToCall->insns

   /*
    * Prep for the native call
    * %eax=methodToCall, LOCAL1_OFFSET(%ebp)=newFP, %edx=newSaveArea
    */

.LinvokeNative:
    movl        rSELF,%ecx              # %ecx<- pthread
    movl        %eax, OUT_ARG1(%esp)    # push parameter methodToCall
    movl        offThread_jniLocal_topCookie(%ecx), %eax # %eax<- self->localRef->...
    movl        %eax, offStackSaveArea_localRefCookie(%edx) # newSaveArea->localRefCookie<- top
    movl        %edx, OUT_ARG4(%esp)    # save newSaveArea
    movl        LOCAL1_OFFSET(%ebp), %edx # %edx<- newFP
    movl        %edx, offThread_curFrame(%ecx)  # self->self->curFrame<- newFP
    movl        %ecx, OUT_ARG3(%esp)    # save self->self
    movl        %ecx, OUT_ARG2(%esp)    # push parameter self->self
    movl        rSELF,%ecx              # %ecx<- pthread
    movl        OUT_ARG1(%esp), %eax    # %eax<- methodToCall
    lea         offThread_retval(%ecx), %ecx # %ecx<- &retval
    movl        %ecx, OUT_ARG0(%esp)    # push parameter pthread
    push        %edx                    # push parameter newFP

    call        *offMethod_nativeFunc(%eax) # call methodToCall->nativeFunc
    lea         4(%esp), %esp
    movl        OUT_ARG4(%esp), %ecx    # %ecx<- newSaveArea
    movl        OUT_ARG3(%esp), %eax    # %eax<- self->self
    movl        offStackSaveArea_localRefCookie(%ecx), %edx # %edx<- old top
    cmp         $0, offThread_exception(%eax) # check for exception
    movl        rFP, offThread_curFrame(%eax) # self->self->curFrame<- rFP
    movl        %edx, offThread_jniLocal_topCookie(%eax) # new top <- old top
    jne         common_exceptionThrown  # handle exception
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    GOTO_NEXT_R %edx                    # jump to next instruction

.LstackOverflow:    # eax=methodToCall
    movl        %eax, OUT_ARG1(%esp)    # push parameter methodToCall
    movl        rSELF,%eax              # %eax<- self
    movl        %eax, OUT_ARG0(%esp)    # push parameter self
    call        dvmHandleStackOverflow  # call: (Thread* self, Method* meth)
    jmp         common_exceptionThrown  # handle exception


/*
 * Do we need the thread to be suspended or have debugger/profiling activity?
 *
 * On entry:
 *   ebx  -> PC adjustment in 16-bit words (must be preserved)
 *   ecx  -> SELF pointer
 *   reentry type, e.g. kInterpEntryInstr stored in rSELF->entryPoint
 *
 * Note: A call will normally kill %eax and %ecx.  To
 *       streamline the normal case, this routine will preserve
 *       %ecx in addition to the normal caller save regs.  The save/restore
 *       is a bit ugly, but will happen in the relatively uncommon path.
 * TODO: Basic-block style Jit will need a hook here as well.  Fold it into
 *       the suspendCount check so we can get both in 1 shot.
 * TUNING: Improve scheduling here & do initial single test for all.
 */
common_periodicChecks:
    cmpl    $0,offThread_suspendCount(%ecx)     # non-zero suspendCount?
    jne     1f

6:
    movl   offThread_pInterpBreak(%ecx),%eax    # eax <- &interpBreak
    cmpl   $0,(%eax)              # something interesting happening?
    jne    3f                      # yes - switch interpreters
    ret

    /* Check for suspend */
1:
    /*  At this point, the return pointer to the caller of
     *  common_periodicChecks is on the top of stack.  We need to preserve
     *  SELF(ecx).
     *  The outgoing profile is:
     *      bool dvmCheckSuspendPending(Thread* self)
     *  Because we reached here via a call, go ahead and build a new frame.
     */
    EXPORT_PC                         # need for precise GC
    movl    %ecx,%eax                 # eax<- self
    push    %ebp
    movl    %esp,%ebp
    subl    $24,%esp
    movl    %eax,OUT_ARG0(%esp)
    call    dvmCheckSuspendPending
    addl    $24,%esp
    pop     %ebp
    movl    rSELF,%ecx

    /*
     * Need to check to see if debugger or profiler flags got set
     * while we were suspended.
     */
    jmp    6b

    /* Switch interpreters */
    /* Note: %ebx contains the 16-bit word offset to be applied to rPC to
     * "complete" the interpretation of backwards branches.  In effect, we
     * are completing the interpretation of the branch instruction here,
     * and the new interpreter will resume interpretation at the branch
     * target. However, a switch request recognized during the handling
     * of a return from method instruction results in an immediate abort,
     * and the new interpreter will resume by re-interpreting the return
     * instruction.
     */
3:
    leal    (rPC,%ebx,2),rPC       # adjust pc to show target
    movl    rSELF,%ecx             # bail expect SELF already loaded
    movl    $1,rINST              # set changeInterp to true
    jmp     common_gotoBail


/*
 * Common code for handling a return instruction
 */
common_returnFromMethod:
    movl    rSELF,%ecx
    /* Set entry mode in case we bail */
    movb    $kInterpEntryReturn,offThread_entryPoint(%ecx)
    xorl    rINST,rINST   # zero offset in case we switch interps
    call    common_periodicChecks   # Note: expects %ecx to be preserved

    SAVEAREA_FROM_FP %eax                         # eax<- saveArea (old)
    movl    offStackSaveArea_prevFrame(%eax),rFP  # rFP<- prevFrame
    movl    (offStackSaveArea_method-sizeofStackSaveArea)(rFP),rINST
    cmpl    $0,rINST                             # break?
    je      common_gotoBail    # break frame, bail out completely

    movl    offStackSaveArea_savedPc(%eax),rPC    # pc<- saveArea->savedPC
    movl    rINST,offThread_method(%ecx)  # self->method = newSave->meethod
    movl    rFP,offThread_curFrame(%ecx)     # self->curFrame = fp
    movl    offMethod_clazz(rINST),%eax      # eax<- method->clazz
    FETCH_INST_OPCODE 3 %edx
    movl    offClassObject_pDvmDex(%eax),%eax # eax<- method->clazz->pDvmDex
    ADVANCE_PC 3
    movl    %eax,offThread_methodClassDex(%ecx)
    /* not bailing - restore entry mode to default */
    movb    $kInterpEntryInstr,offThread_entryPoint(%ecx)
    GOTO_NEXT_R %edx

/*
 * Prepare to strip the current frame and "longjump" back to caller of
 * dvmMterpStdRun.
 *
 * on entry:
 *    rINST holds changeInterp
 *    ecx holds self pointer
 *
 * expected profile: dvmMterpStdBail(Thread *self, bool changeInterp)
 */
common_gotoBail:
    movl   rPC,offThread_pc(%ecx)     # export state to self
    movl   rFP,offThread_fp(%ecx)
    movl   %ecx,OUT_ARG0(%esp)      # self in arg0
    movl   rINST,OUT_ARG1(%esp)     # changeInterp in arg1
    call   dvmMterpStdBail          # bail out....


/*
 * After returning from a "selfd" function, pull out the updated values
 * and start executing at the next instruction.
 */
 common_resumeAfterGlueCall:
     LOAD_PC_FP_FROM_SELF
     FETCH_INST
     GOTO_NEXT

/*
 * Integer divide or mod by zero
 */
common_errDivideByZero:
    EXPORT_PC
    movl    $.LstrArithmeticException,%eax
    movl    %eax,OUT_ARG0(%esp)
    movl    $.LstrDivideByZero,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 * On entry, len in eax
 */
common_errNegativeArraySize:
    EXPORT_PC
    movl    %eax,OUT_ARG0(%esp)                  # arg0<- len
    call    dvmThrowNegativeArraySizeException   # (len)
    jmp     common_exceptionThrown

/*
 * Attempt to allocate an array with a negative size.
 */
common_errNoSuchMethod:

    EXPORT_PC
    movl    $.LstrNoSuchMethodError,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

/*
 * Hit a null object when we weren't expecting one.  Export the PC, throw a
 * NullPointerException and goto the exception processing code.
 */
common_errNullObject:
    EXPORT_PC
    movl    $.LstrNullPointerException,%eax
    movl    %eax,OUT_ARG0(%esp)
    xorl    %eax,%eax
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowException
    jmp     common_exceptionThrown

/*
 * Array index exceeds max.
 * On entry:
 *    eax <- array object
 *    ecx <- index
 */
common_errArrayIndex:
    EXPORT_PC
    movl    offArrayObject_length(%eax), %eax
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowArrayIndexOutOfBoundsException   # args (index, length)
    jmp     common_exceptionThrown

/*
 * Somebody has thrown an exception.  Handle it.
 *
 * If the exception processing code returns to us (instead of falling
 * out of the interpreter), continue with whatever the next instruction
 * now happens to be.
 *
 * This does not return.
 */
common_exceptionThrown:
    movl    rSELF,%ecx
    movl    rPC,offThread_pc(%ecx)
    movl    rFP,offThread_fp(%ecx)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmMterp_exceptionThrown
    jmp     common_resumeAfterGlueCall

common_abort:
    movl    $0xdeadf00d,%eax
    call     *%eax


/*
 * Strings
 */

    .section     .rodata
.LstrNullPointerException:
    .asciz    "Ljava/lang/NullPointerException;"
.LstrArithmeticException:
    .asciz  "Ljava/lang/ArithmeticException;"
.LstrDivideByZero:
    .asciz  "divide by zero"
.LstrInstantiationError:
    .asciz  "Ljava/lang/InstantiationError;"
.LstrNoSuchMethodError:
    .asciz  "Ljava/lang/NoSuchMethodError;"
.LstrInternalErrorA:
    .asciz  "Ljava/lang/InternalError;"
.LstrFilledNewArrayNotImplA:
    .asciz  "filled-new-array only implemented for 'int'"


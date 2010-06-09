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
  rPC      edx   interpreted program counter, used for fetching instructions
  rFP      esi   interpreted frame pointer, used for accessing locals and args
  rIBASE   edi   Base pointer for instruction dispatch computed goto
  rINST    bx    first 16-bit code of current instruction
  rOPCODE  bl    opcode portion of instruction word
  rINST_HI bh    high byte of instruction word, usually contains src/tgt reg names

Notes:
   o High order 16 bits of ebx must be zero on entry to handler
   o rPC, rFP, rIBASE, rINST/rOPCODE valid on handler entry and exit
   o eax and ecx are scratch, rINST/ebx sometimes scratch
   o rPC is in the caller save set, and will be killed across external calls. Don't
     forget to SPILL/UNSPILL it around call points

*/

#define rPC      %edx
#define rFP      %esi
#define rIBASE   %edi
#define rINST_FULL %ebx
#define rINST    %bx
#define rINST_HI %bh
#define rINST_LO %bl
#define rOPCODE  %bl


/* Frame diagram while executing dvmMterpStdRun, high to low addresses */
#define IN_ARG0        (  8)
#define CALLER_RP      (  4)
#define PREV_FP        (  0) /* <- dvmMterpStdRun ebp */
/* Spill offsets relative to %ebp */
#define EDI_SPILL      ( -4)
#define ESI_SPILL      ( -8)
#define EDX_SPILL      (-12) /* <- esp following dmMterpStdRun header */
#define rPC_SPILL      (-16)
#define rFP_SPILL      (-20)
#define rGLUE_SPILL    (-24)
#define rIBASE_SPILL   (-28)
#define rINST_FULL_SPILL    (-32)
#define TMP_SPILL      (-36)
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

#define SPILL(reg) movl reg##,reg##_SPILL(%ebp)
#define UNSPILL(reg) movl reg##_SPILL(%ebp),reg
#define SPILL_TMP(reg) movl reg,TMP_SPILL(%ebp)
#define UNSPILL_TMP(reg) movl TMP_SPILL(%ebp),reg


/* save/restore the PC and/or FP from the glue struct */
#define LOAD_PC_FROM_GLUE(_glu)     movl    offGlue_pc(_glu),rPC
#define SAVE_PC_TO_GLUE(_glu)       movl    rPC,offGlue_pc(_glu)
#define LOAD_FP_FROM_GLUE(_glu)     movl    offGlue_fp(_glu),rFP
#define SAVE_FP_TO_GLUE(_glu)       movl    rFP,offGlue_fp(_glu)

#define GET_GLUE(_reg)     movl   rGLUE_SPILL(%ebp),_reg

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
#define EXPORT_PC() \
    movl     rPC, (-sizeofStackSaveArea + offStackSaveArea_currentPc)(rFP)

/*
 * Given a frame pointer, find the stack save area.
 *
 * In C this is "((StackSaveArea*)(_fp) -1)".
 */
#define SAVEAREA_FROM_FP(_reg, _fpreg) \
    leal    -sizeofStackSaveArea(_fpreg),_reg

/*
 * Fetch the next instruction from rPC into rINST.  Does not advance rPC.
 */
#define FETCH_INST()            movzwl    (rPC),rINST_FULL

/*
 * Fetch the nth instruction word from rPC into rINST.  Does not advance
 * rPC, and _count is in words
 */
#define FETCH_INST_WORD(_count)  movzwl  _count*2(rPC),rINST_FULL

/*
 * Fetch instruction word indexed (used for branching).
 * Index is in instruction word units.
 */
#define FETCH_INST_INDEXED(_reg) movzwl  (rPC,_reg,2),rINST_FULL

/*
 * Extract the opcode of the instruction in rINST
 */
#define EXTRACT_OPCODE(_reg)   movzx rOPCODE,_reg

/*
 * Advance rPC by instruction count
 */
#define ADVANCE_PC(_count)    leal  2*_count(rPC),rPC

/*
 * Advance rPC by branch offset in register
 */
#define ADVANCE_PC_INDEXED(_reg) leal (rPC,_reg,2),rPC

/*
 * Note: assumes opcode previously fetched and in rINST, and
 *       %eax is killable at this point.
 */
#if 1
.macro GOTO_NEXT
    /* For computed next version */
     movzx    rOPCODE,%eax
     sall     $$$handler_size_bits,%eax
     addl     rIBASE,%eax
     jmp      *%eax
.endm
#else
   /* For jump table version */
.macro GOTO_NEXT
     movzx   rOPCODE,%eax
     jmp     *(rIBASE,%eax,4)
.endm
#endif

/*
 * Get/set the 32-bit value from a Dalvik register.
 */
#define GET_VREG(_reg, _vreg)   movl     (rFP,_vreg,4),_reg
#define SET_VREG(_reg, _vreg)   movl     _reg,(rFP,_vreg,4)
#define GET_VREG_WORD(_reg, _vreg, _offset)   movl     4*(_offset)(rFP,_vreg,4),_reg
#define SET_VREG_WORD(_reg, _vreg, _offset)   movl     _reg,4*(_offset)(rFP,_vreg,4)

/*
 * This is a #include, not a %include, because we want the C pre-processor
 * to expand the macros into assembler assignment statements.
 */
#include "../common/asm-constants.h"

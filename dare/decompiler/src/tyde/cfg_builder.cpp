/*
 * Copyright (C) 2012 The Pennsylvania State University
 * Systems and Internet Infrastructure Security Laboratory
 *
 * Author: Damien Octeau <octeau@cse.psu.edu>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

/**
 * cfg_builder.cpp
 *
 * CFGBuilder builds a control flow graph of a method, including exceptional
 * flow.
 */


#include "tyde/cfg_builder.h"

#include <stack>
#include <vector>

#include "libdex/DexCatch.h"
#include "libdex/InstrUtils.h"

#include "class_file/constant_pool.h"
#include "class_file/code_attribute.h"
#include "dare.h"
#include "tyde/tyde_body.h"
#include "tyde/tyde_instruction.h"


/*static*/ int CFGBuilder::label_ = 0;


/**
 * Build a Control Flow Graph of a method.
 *
 * @param code_attribute The code attribute of a method.
 * @param cp The constant pool of the class containing the method.
 */
#include "class_file/method_info.h"
/*static*/ void CFGBuilder::BuildCFG(CodeAttribute* code_attribute,
    ConstantPool& cp) {
  label_ = 0;
  AddSuccessors(code_attribute->tyde_body());
  AddExceptionSuccessors(code_attribute, cp);
  RemoveDeadTries(code_attribute->tyde_body());
  CheckAndPatchOffsets(code_attribute->tyde_body());
}


/**
 * Find an instruction by absolute offset.
 *
 * @param dcode A Tyde body.
 * @param offset The offset of the instruction we are looking for.
 * @param first The index of the first instruction in the range considered.
 * @param last The last instruction in the range considered.
 * @return A Tyde instruction.
 */
/*static*/ TydeInstruction* CFGBuilder::FindInstructionByOffset(
    const TydeBody& dcode, int offset, int first, int last) {
  int middle = (last + first) / 2;
  if (dcode[middle]->original_offset() == offset)
    return dcode[middle];
  else if (dcode[middle]->original_offset() < offset)
    return FindInstructionByOffset(dcode, offset, middle + 1, last);
  else
    return FindInstructionByOffset(dcode, offset, first, middle - 1);
}

/**
 * Add successors for each instruction in a Tyde body.
 *
 * @param dcode A Tyde body.
 */
/*static*/ void CFGBuilder::AddSuccessors(const TydeBody& dcode) {
  for (int i = 0; i < (int) dcode.size(); ++i) {
    OpcodeFlags opcode_flags = dexGetFlagsFromOpcode(dcode[i]->op());

    if ((opcode_flags & kInstrCanContinue) != 0 && i < (int) dcode.size() - 1)
      dcode[i]->AddSuccessor(dcode[i + 1]);

    if((opcode_flags & kInstrCanBranch) != 0) {
      TydeInstruction* target = FindInstructionByOffset(dcode,
          dcode[i]->original_offset() + (s4) dcode[i]->constant(), 0,
          dcode.size() - 1);
      dcode[i]->AddSuccessor(target);
      if (target->label() < 0)
        target->set_label(label_++);
    }
    else if((opcode_flags & kInstrCanSwitch) != 0) {
      if ((opcode_flags & kInstrCanContinue) != 0
          && i < (int) dcode.size() - 1
          && dcode[i + 1]->label() < 0)
        dcode[i + 1]->set_label(label_++);
      for(int j = 0; j < (int) dcode[i]->targets().size(); ++j) {
        TydeInstruction* target = FindInstructionByOffset(dcode,
            dcode[i]->original_offset() + dcode[i]->targets()[j], 0,
            dcode.size() - 1);
        dcode[i]->AddSuccessor(target);
        if (target->label() < 0)
          target->set_label(label_++);
      }
    }
  }
}

/**
 * Split exception tables considering which instruction might actually throw
 * an exception.
 *
 * @param code_attribute The code attribute of the method whose CFG is being
 *        constructed.
 * @param curr_try The try block being split.
 * @param tries The output vector of try blocks.
 */
/*static*/ void CFGBuilder::AnalyzeThrowable(
    CodeAttribute* code_attribute, const TryItem& curr_try,
    std::vector<TryItem>& tries) {
  const TydeBody& tyde_body = code_attribute->tyde_body();
  TryItem try_item = { NULL, NULL, curr_try.handlers };
  int i = curr_try.start_ptr->index();
  for ( ; i < (int) curr_try.end_ptr->index(); ++i) {
    if ((dexGetFlagsFromOpcode(code_attribute->tyde_body()[i]->op())
        & kInstrCanThrow) != 0 && try_item.start_ptr == NULL) {
      try_item.start_ptr = tyde_body[i];
      if (try_item.start_ptr->label() < 0)
        try_item.start_ptr->set_label(label_++);
    } else if (try_item.start_ptr != NULL && try_item.end_ptr == NULL) {
      try_item.end_ptr = tyde_body[i];
      if (try_item.end_ptr->label() < 0)
        try_item.end_ptr->set_label(label_++);
      tries.push_back(try_item);
      try_item.start_ptr = try_item.end_ptr = NULL;
    }
  }

  // The last instruction can throw.
  if (try_item.start_ptr != NULL && try_item.end_ptr == NULL) {
    try_item.end_ptr = tyde_body[i];
    if (try_item.end_ptr->label() < 0)
      try_item.end_ptr->set_label(label_++);
    tries.push_back(try_item);
  }
}

/**
 * Process exception handlers. Turn offsets into pointers and add caught types
 * to constant pool.
 *
 * @param code_attribute The code attribute of the method whose CFG is being
 *        constructed.
 * @param handler_offset The absolute offset of the descriptor for the
 *        handlers.
 * @param cp The constant pool of the class the code attribute belongs to.
 * @return An encoded catch handler describing all handlers for a try block.
 */
/*static*/ EncodedCatchHandler CFGBuilder::ProcessExceptionHandlers(
    CodeAttribute* code_attribute, int handler_offset, ConstantPool& cp) {
  EncodedCatchHandler result;
  result.catch_all_ptr = NULL;
  DexCatchIterator iterator;
  dexCatchIteratorInit(&iterator, code_attribute->dex_code(), handler_offset);

  for (;;) {
    DexCatchHandler* handler = dexCatchIteratorNext(&iterator);
    const char* descriptor;

    if (handler == NULL) {
      break;
    } else if(handler->typeIdx != kDexNoIndex) {
      // Non catch-all handlers.
      EncodedTypeAddrPair enc;
      enc.ptr = FindInstructionByOffset(code_attribute->tyde_body(),
          handler->address, 0, code_attribute->tyde_body().size() - 1);
      if (enc.ptr->label() < 0)
        enc.ptr->set_label(label_++);
      // The handler catches a specific exception type.
      enc.type_ptr = cp.AddClassCst(handler->typeIdx);

      result.handlers.push_back(enc);
    } else {
      result.catch_all_ptr = FindInstructionByOffset(
          code_attribute->tyde_body(), handler->address, 0,
          code_attribute->tyde_body().size() - 1);
      if (result.catch_all_ptr->label() < 0)
        result.catch_all_ptr->set_label(label_++);
    }
  }

  return result;
}

/**
 * Add exception successors for all instructions of a method.
 *
 * @param code_attribute The code attribute of the method whose CFG is being
 *        constructed.
 * @param cp The constant pool of the class containing the code attribute.
 */
/*static*/ void CFGBuilder::AddExceptionSuccessors(
    CodeAttribute* code_attribute, ConstantPool& cp) {
  int tries_size = code_attribute->dex_code()->triesSize;

  if (tries_size == 0)
    return;

  const DexTry* p_tries = dexGetTries(code_attribute->dex_code());
  std::vector<TryItem> tries;

  // Compute try blocks and handlers.
  for (int i = 0; i < tries_size; ++i) {
    // First determine the current try block.
    TryItem curr_try;
    const DexTry* p_try = &p_tries[i];
    u4 start = p_try->startAddr;
    u4 end = start + p_try->insnCount;
    // First dynamically enclosed instruction.
    curr_try.start_ptr = FindInstructionByOffset(code_attribute->tyde_body(),
        p_try->startAddr, 0, code_attribute->tyde_body().size() - 1);
    // Instruction right after the last dynamically enclosed instruction.
    curr_try.end_ptr = FindInstructionByOffset(code_attribute->tyde_body(),
        p_try->startAddr + p_try->insnCount, 0,
        code_attribute->tyde_body().size() - 1);
    curr_try.handlers = ProcessExceptionHandlers(code_attribute,
        p_try->handlerOff, cp);

    if (Dare::split_exception_tables()) {
      AnalyzeThrowable(code_attribute, curr_try, tries);
    } else {
      if (curr_try.start_ptr->label() < 0)
        curr_try.start_ptr->set_label(label_++);
      if (curr_try.end_ptr->label() < 0)
        curr_try.end_ptr->set_label(label_++);
      tries.push_back(curr_try);
    }
  }

  code_attribute->tyde_body().set_tries(tries);

  // Add CFG edges between try blocks and exception handlers.
  for (int i = 0; i < (int) tries.size(); ++i) {
    TryItem curr_try = tries[i];

    // Index of first dynamically enclosed instruction.
    int first_ins_index = curr_try.start_ptr->index();
    // Index of instruction right after the last dynamically enclosed
    // instruction.
    int last_ins_index = curr_try.end_ptr->index();

    for (int j = 0; j < (int) curr_try.handlers.handlers.size(); ++j) {
      TydeInstruction* handler = curr_try.handlers.handlers[j].ptr;
      for (int k = first_ins_index; k < last_ins_index; ++k) {
        if (Dare::split_exception_tables() ||
            (dexGetFlagsFromOpcode(code_attribute->tyde_body()[k]->op())
                & kInstrCanThrow) != 0) {
          for (int l = 0;
              l < (int) code_attribute->tyde_body()[k]->predecessors().
                  size(); ++l)
            code_attribute->tyde_body()[k]->predecessors()[l]->
                AddExceptionSuccessor(handler);
        }
      }
    }

    if (curr_try.handlers.catch_all_ptr != NULL) {
      TydeInstruction* handler = curr_try.handlers.catch_all_ptr;
      for (int k = first_ins_index; k < last_ins_index; ++k) {
        if (Dare::split_exception_tables() ||
            (dexGetFlagsFromOpcode(code_attribute->tyde_body()[k]->op())
                & kInstrCanThrow) != 0) {
          for (int l = 0;
              l < (int) code_attribute->tyde_body()[k]->predecessors().
                  size(); ++l)
            code_attribute->tyde_body()[k]->predecessors()[l]->
                AddExceptionSuccessor(handler);
        }
      }
    }
  }
}

/**
 * Remove dead try blocks.
 *
 * When code transformations are performed as a results of Dalvik verifier
 * annotations, some code becomes unreachable. In particular, some code
 * protected by try blocks is unreachable. We want to remove these try blocks.
 *
 * @param dcode A Tyde body.
 */
/*static*/ void CFGBuilder::RemoveDeadTries(TydeBody& dcode) {
  ComputeReachability(dcode);

  for (int i = dcode.tries().size() - 1; i >= 0; --i) {
    for (int j = dcode.tries()[i].handlers.handlers.size() - 1; j >= 0; --j) {
      if (!dcode.tries()[i].handlers.handlers[j].ptr->reachable()) {
        LOGW("Removing unreachable catch handler\n");
        dcode.tries()[i].handlers.handlers.erase(
            dcode.tries()[i].handlers.handlers.begin() + j);
      }
    }
    if (dcode.tries()[i].handlers.handlers.empty()
        && (dcode.tries()[i].handlers.catch_all_ptr == NULL
            || !dcode.tries()[i].handlers.catch_all_ptr->reachable())) {
      dcode.tries().erase(dcode.tries().begin() + i);
    }
  }
}

/**
 * Compute the reachability of all instructions of a Tyde body.
 *
 * @param dcode A Tyde body.
 */
/*static*/ void CFGBuilder::ComputeReachability(const TydeBody& dcode) {
  std::vector<bool> visited(dcode.size(), false);
  std::stack<TydeInstruction*> s;
  s.push(dcode[0]);

  while (!s.empty()) {
    TydeInstruction* instruction = s.top();
    s.pop();
    if (visited[instruction->index()])
      continue;
    visited[instruction->index()] = true;
    instruction->set_reachable(true);
    for (int i = 0; i < (int) instruction->successors().size(); ++i)
      s.push(instruction->successors()[i]);
    for (int i = 0; i < (int) instruction->exception_successors().size(); ++i)
      s.push(instruction->exception_successors()[i]);
  }
}

/**
 * Check offsets in branching instructions and patch potentially overflowing
 * two-byte offsets.
 *
 * @param dcode A Tyde body.
 */
/*static*/ void CFGBuilder::CheckAndPatchOffsets(TydeBody& dcode) {
  if ((int) dcode.size() < Dare::offset_limit() || Dare::offset_limit() <= 0) {
    return;
  }

  for (int i = dcode.size() - 1; i >= 0; --i)
    if (dcode[i]->op() >= OP_IF_EQ && dcode[i]->op() <= OP_IF_LEZ)
      if (CheckOffsetAtInstruction(dcode, i))
        dcode.RefreshIndicesAfter(i);
}

/**
 * Check offset in a branching instruction and patch potentially overflowing
 * two-byte offset if necessary.
 *
 * It is necessary that all instruction indices are up-to-date before calling
 * this function (call dcode.RefreshIndicesAfter() if necessary).
 *
 * @param dcode A Tyde body.
 * @param index The index of an instruction to be checked.
 * @return True if the code was modified.
 */
/*static*/ bool CFGBuilder::CheckOffsetAtInstruction(TydeBody& dcode,
      int index) {
  TydeInstruction* branching_instruction = dcode[index];
  TydeInstruction* original_target = branching_instruction->successors()[1];
  s4 relative_offset = (s4) branching_instruction->constant();
  int original_target_index = original_target->index();
  int gap;

  if (relative_offset > 0)
    gap = original_target_index - index;
  else
    gap = index - original_target_index;

  if (gap <= Dare::offset_limit())
    return false;

  // Make the next instruction.
  DecodedInstruction decoded_instruction;
  decoded_instruction.opcode = OP_GOTO;
  TydeInstruction* next = new TydeInstruction(&decoded_instruction,
      branching_instruction->original_offset());
  TydeInstruction* normal_successor = branching_instruction->successors()[0];
  normal_successor->ReplacePredecessor(branching_instruction, next);
  normal_successor->set_label(label_++);
  next->AddSuccessor(normal_successor);
  next->AddPredecessor(branching_instruction);

  // Make the new target instruction.
  TydeInstruction* new_target = new TydeInstruction(&decoded_instruction,
      branching_instruction->original_offset());
  TydeInstruction* target_successor = branching_instruction->successors()[1];
  target_successor->ReplacePredecessor(branching_instruction, new_target);
  new_target->AddSuccessor(target_successor);
  new_target->AddPredecessor(branching_instruction);
  new_target->set_label(label_++);

  // Patch successors and insert new instructions.
  std::vector<TydeInstruction*> slice(2);
  slice[0] = next;
  slice[1] = new_target;

  branching_instruction->set_successors(slice);

  dcode.insert(index + 1, slice);

  return true;
}


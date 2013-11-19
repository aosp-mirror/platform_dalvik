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

/*
 * cfg_builder.h
 *
 * CFGBuilder builds a control flow graph of a method, including exceptional
 * flow.
 */

#ifndef TYDE_CFG_BUILDER_H_
#define TYDE_CFG_BUILDER_H_


#include <vector>

#include "tyde/tyde_body.h"


class TydeInstruction;
class CodeAttribute;
class ConstantPool;


class CFGBuilder {
 public:
  /**
   * Build a Control Flow Graph of a method.
   *
   * @param code_attribute The code attribute of a method.
   * @param cp The constant pool of the class containing the method.
   */
  static void BuildCFG(CodeAttribute* method, ConstantPool& cp);

 private:
  /**
   * Find an instruction by absolute offset.
   *
   * @param dcode A Tyde body.
   * @param offset The offset of the instruction we are looking for.
   * @param first The index of the first instruction in the range considered.
   * @param last The last instruction in the range considered.
   * @return A Tyde instruction.
   */
  static TydeInstruction* FindInstructionByOffset(const TydeBody& dcode,
      int offset, int first, int last);
  /**
   * Add successors for each instruction in a Tyde body.
   *
   * @param dcode A Tyde body.
   */
  static void AddSuccessors(const TydeBody& dcode);
  /**
   * Split exception tables considering which instruction might actually throw
   * an exception.
   *
   * @param code_attribute The code attribute of the method whose CFG is being
   *        constructed.
   * @param curr_try The try block being split.
   * @param tries The output vector of try blocks.
   */
  static void AddExceptionSuccessors(CodeAttribute* method, ConstantPool& cp);
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
  static void AnalyzeThrowable(CodeAttribute* code_attribute,
      const TryItem& curr_try, std::vector<TryItem>& tries);
  /**
   * Add exception successors for all instructions of a method.
   *
   * @param code_attribute The code attribute of the method whose CFG is being
   *        constructed.
   * @param cp The constant pool of the class containing the code attribute.
   */
  static EncodedCatchHandler ProcessExceptionHandlers(
      CodeAttribute* code_attribute, int handler_offset, ConstantPool& cp);
  /**
   * Remove dead try blocks.
   *
   * When code transformations are performed as a results of Dalvik verifier
   * annotations, some code becomes unreachable. In particular, some code
   * protected by try blocks is unreachable. We want to remove these try blocks.
   *
   * @param dcode A Tyde body.
   */
  static void RemoveDeadTries(TydeBody& dcode);
  /**
   * Compute the reachability of all instructions of a Tyde body.
   *
   * @param dcode A Tyde body.
   */
  static void ComputeReachability(const TydeBody& dcode);
  /**
   * Check offsets in branching instructions and patch potentially overflowing
   * two-byte offsets.
   *
   * @param dcode A Tyde body.
   */
  static void CheckAndPatchOffsets(TydeBody& dcode);
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
  static bool CheckOffsetAtInstruction(TydeBody& dcode, int index);

  static int label_;
};


#endif /* TYDE_CFG_BUILDER_H_ */

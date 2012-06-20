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
 * type_solver.cpp
 *
 * The type solver finds legal types for ambiguous registers.
 */

#include "typing/type_solver.h"

#include <stack>
#include <utility>
#include <vector>

#include "libdex/DexFile.h"

#include "class_file/code_attribute.h"
#include "class_file/method_info.h"
#include "timer.h"
#include "typing/type_constant.h"
#include "typing/type_variable.h"

using std::vector;
using std::stack;


#define __  kUnknown
#define _t  kTrioUnknown
#define _f  kFIUnknown
#define _d  kDLUnknown
#define _Z  kBoolean
#define _C  kChar
#define _B  kByte
#define _S  kShort
#define _I  kInt
#define _F  kFloat
#define _J  kLong
#define _D  kDouble
#define _X  kConflict

/**
 * Given a type inequality left <= right, the following table answers two
 * questions:
 *   - Is there a type conflict?
 *   - If not, what should be the type of the right-hand side?
 *
 * This mostly comes from the Android source code. We have relaxed the
 * constraints on integer types. We do not need to keep track of them for our
 * analysis, so it is okay to get something like int <= byte. That would come
 * from the fact that, at parse time, some registers are typed as int even
 * though they might be something <= byte.
 */
/*static*/ const VarType TypeSolver::kPrimitiveMergeTable[kPrimitiveTypeCount][kPrimitiveTypeCount] =
{
    /* l<=r  _  t  f  d  Z  C  B  S  I  F  J  D  X */
    { /*_*/ __,_t,_f,_d,_Z,_C,_B,_S,_I,_F,_J,_D,_X },
    { /*t*/ _t,_t,_f,_X,_Z,_C,_B,_S,_I,_F,_X,_X,_X },
    { /*f*/ _f,_f,_f,_X,_Z,_C,_B,_S,_I,_F,_X,_X,_X },
    { /*d*/ _d,_X,_X,_d,_X,_X,_X,_X,_X,_X,_J,_D,_X },
    { /*Z*/ _Z,_Z,_Z,_X,_Z,_C,_B,_S,_I,_X,_X,_X,_X },
    { /*C*/ _C,_C,_C,_X,_Z,_C,_B,_S,_I,_X,_X,_X,_X },
    { /*B*/ _B,_B,_B,_X,_Z,_C,_B,_S,_I,_X,_X,_X,_X },
    { /*S*/ _S,_S,_S,_X,_Z,_C,_B,_S,_I,_X,_X,_X,_X },
    { /*I*/ _I,_I,_I,_X,_Z,_C,_B,_S,_I,_X,_X,_X,_X },
    { /*F*/ _F,_F,_F,_X,_X,_X,_X,_X,_X,_F,_X,_X,_X },
    { /*J*/ _J,_X,_X,_J,_X,_X,_X,_X,_X,_X,_J,_X,_X },
    { /*D*/ _D,_X,_X,_D,_X,_X,_X,_X,_X,_X,_X,_D,_X },
    { /*X*/ _X,_X,_X,_X,_X,_X,_X,_X,_X,_X,_X,_X,_X },
};

#undef __
#undef _t
#undef _f
#undef _d
#undef _Z
#undef _C
#undef _B
#undef _S
#undef _I
#undef _F
#undef _J
#undef _D
#undef _X

namespace {
const bool display_offending_path = false;
const bool debugging = false;
}


/**
 * Point of entry for the type solver.
 *
 * It works in several steps. First, do some pre-processing. Second, generate
 * type constraints using register assignments and uses. Third, solve
 * constraints. Fourth, determine types for registers which are not
 * sufficiently constrained. Fifth, update the actual instructions which the
 * solved types. Finally, do some post-processing.
 *
 * @param method The method which contains the code to be typed.
 */
void TypeSolver::SolveTypes(MethodInfo* method) {
  class_descriptor_ = method->class_descriptor();
  Init(method);

  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kConstraintGeneration);

  if (debugging)
    out_ << "Typing " << method->name().bytes() << "\n";

  GenerateConstraints(method);

  if (debugging) {
    out_ << "******************************\n";
    out_ << "* BEFORE SOLVING CONSTRAINTS *\n";
    out_ << "******************************\n";
    DisplayConstraints();
  }

  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kConstraintSolution);

  SolveConstraints();
  SolveUnconstrainedTypes();

  if (debugging) {
    out_ << "*****************************\n";
    out_ << "* AFTER SOLVING CONSTRAINTS *\n";
    out_ << "*****************************\n";
    DisplayConstraints();
  }

  UpdateInstructionTypes();

  if (debugging) {
    out_ << "*******************************\n";
    out_ << "* AFTER UPDATING INSTRUCTIONS *\n";
    out_ << "*******************************\n";
    method->code_attribute()->tyde_body().Dump(out_);
    out_ << "\n";
  }

  if (debugging && issue_)
    LOGD("%s", out_.str().c_str());

  Finalize(method->code_attribute()->tyde_body());
}

/**
 * Do some pre-processing for the type solver. It should be called before any
 * analysis is done.
 *
 * This method mostly consists in adding dummy register assignments to account
 * for the method arguments.
 *
 * @param method The method we are currently typing.
 */
void TypeSolver::Init(MethodInfo* method) {
  int incoming = method->code_attribute()->dex_code()->insSize;
  TydeBody& dcode = method->code_attribute()->tyde_body();
  TydeInstruction* first_instruction = dcode[0];

  // Add the other arguments.
  for (int i = method->arguments().size() - 1; i >= 0; --i) {
    int idx = method -> RegisterIndex(i);

    if ((method->access_flags() & ACC_STATIC) != 0) {
      TydeInstruction* dummy_ins = new TydeInstruction(
          nb_registers_ - incoming + idx, method -> arguments()[i]);
      first_instruction->AddPredecessor(dummy_ins);
      dummy_ins->set_index(dcode.size());
      dcode.push_back(dummy_ins);
    } else {
      TydeInstruction* dummy_ins = new TydeInstruction(
          nb_registers_ - incoming + idx + 1, method -> arguments()[i]);
      first_instruction->AddPredecessor(dummy_ins);
      dummy_ins->set_index(dcode.size());
      dcode.push_back(dummy_ins);
    }
  }

  // Add the this reference.
  if ((method -> access_flags() & ACC_STATIC) == 0) {
    TydeInstruction* dummy_ins = new TydeInstruction(
        nb_registers_ - incoming, kNAObject);
    first_instruction->AddPredecessor(dummy_ins);
    dummy_ins->set_index(dcode.size());
    dcode.push_back(dummy_ins);
  }

  ins_count_ = dcode.size();
}

/**
 * Generate the type constraints related to the register assignments and
 * uses in the method.
 *
 * @param method The method we are currently typing.
 */
void TypeSolver::GenerateConstraints(MethodInfo* method) {
  TydeBody& dcode = method->code_attribute()->tyde_body();
  is_visited_current_ = vector<bool>(ins_count_ * nb_registers_, false);

  for (int i = 0; i < (int) method->ambiguous_sources().size(); ++i) {
    ResetVisitedFlags();
    InstructionAndRegister current_ir = method->ambiguous_sources()[i];
    CheckInitialConstraints(current_ir.ins);
    BackDfs(current_ir.ins, current_ir.reg);
  }

  for (int i = 0; i < (int) method->ambiguous_destinations().size(); ++i) {
    ResetVisitedFlags();
    InstructionAndRegister current_ir = method->ambiguous_destinations()[i];
    Dfs(current_ir.ins, current_ir.reg);
  }
}

/**
 * Reset the visited flags before a DFS traversal. To be called before a DFS
 * or back-DFS traversal when computing def-use chains.
 */
void TypeSolver::ResetVisitedFlags() {
  for (int i = 0; i < (int) changed_flags_.size(); ++i)
    is_visited_current_[changed_flags_[i]] = false;
  changed_flags_.clear();
}

/**
 * Perform a DFS traversal of the CFG starting at a given instruction, looking
 * for uses of a given register.
 *
 * @param first_instruction The instruction at which the DFS should start.
 * @param reg The register for which we are looking for uses.
 * @param dim The current array dimension.
 */
void TypeSolver::Dfs(TydeInstruction* first_instruction, int reg,
    int dim /*= 0*/) {
  TypeVariable* type_variable = new TypeVariable(
      first_instruction->destination().type, first_instruction, false, reg);
  type_variable = (TypeVariable*) AddTypeElement(type_variable);
  Dfs(type_variable, reg, first_instruction, dim);
}

/**
 * Helper function for the DFS procedure.
 *
 * @param type_variable The type variable corresponding to the register for
 *        which we are looking for uses.
 * @param reg The register for which we are looking for uses.
 * @param first_instruction The instruction at which the DFS is starting.
 * @param dim The current array dimension.
 */
void TypeSolver::Dfs(TypeVariable* type_variable, int reg,
    TydeInstruction* first_instruction, int dim) {
  std::stack<TydeInstruction*> dfs_stack;
//  out_ << "Dfs from \n " << first_instruction->ToString();

  AddNewSuccessors(dfs_stack, first_instruction);

  while (!dfs_stack.empty()) {
    TydeInstruction* current_instruction = dfs_stack.top();
    dfs_stack.pop();
    if (CheckAndSetVisited(is_visited_current_, current_instruction, reg))
      continue;

//    out_ << current_instruction->ToString() << "\n";
//    current_instruction->Print(1);
    if (current_instruction->IsSource(reg)) {
      AddNewInequality(type_variable, current_instruction, true, reg, dim);
    }
    if (!current_instruction->has_destination()
        || current_instruction->destination().reg != reg) {
      AddNewSuccessors(dfs_stack, current_instruction);
    }
  }
}

/**
 * Perform a back DFS traversal of the CFG starting at a given instruction,
 * looking for assignments of a given register.
 *
 * @param first_instruction The instruction at which the back DFS should
 *        start.
 * @param reg The register for which we are looking for assignments.
 * @param dim The current array dimension.
 */
void TypeSolver::BackDfs(TydeInstruction* first_instruction, int reg,
    int dim /*= 0*/) {
  TypeVariable* type_variable = new TypeVariable(
      first_instruction->GetRegisterType(reg), first_instruction, true, reg);
  type_variable = (TypeVariable*) AddTypeElement(type_variable);

  BackDfs(type_variable, reg, first_instruction, dim);
}

/**
 * Helper function for the back DFS procedure.
 *
 * @param type_variable The type variable corresponding to the register for
 *        which we are looking for an assignment.
 * @param reg The register for which we are looking for assignments.
 * @param first_instruction The instruction at which the back DFS should
 *        start.
 * @param dim The current array dimension.
 */
void TypeSolver::BackDfs(TypeVariable* type_variable, int reg,
    TydeInstruction* first_instruction, int dim) {
  std::stack<TydeInstruction*> dfs_stack;
//  cout << "BackDfs from \n";
//  first_instruction->Print(2);

  AddNewPredecessors(dfs_stack, first_instruction);

  while (!dfs_stack.empty()) {
    TydeInstruction* current_instruction = dfs_stack.top();
//    current_instruction->Print(4);
    dfs_stack.pop();
    if (CheckAndSetVisited(is_visited_current_, current_instruction, reg))
      continue;

    if (current_instruction->has_destination()
        && current_instruction->destination().reg == reg) {
      AddNewInequality(type_variable, current_instruction, false, reg, dim);
    } else {
      AddNewPredecessors(dfs_stack, current_instruction);
    }
  }
}

/**
 * Put all successors of an instruction on top of a stack.
 *
 * @param dfs_stack The stack on which the successors should be put.
 * @param instruction The instruction whose successors we are looking for.
 */
void TypeSolver::AddNewSuccessors(
    std::stack<TydeInstruction*>& dfs_stack,
    TydeInstruction* instruction) {
  for (int i = 0; i < (int) instruction->exception_successors().size(); ++i)
    dfs_stack.push(instruction->exception_successors()[i]);
  for (int i = instruction->successors().size() - 1; i >= 0; --i)
    dfs_stack.push(instruction->successors()[i]);
}

/**
 * Put all predecessors of an instruction on top of a stack.
 *
 * @param dfs_stack The stack on which the predecessors should be put.
 * @param instruction The instruction whose predecessors we are looking for.
 */
void TypeSolver::AddNewPredecessors(
    std::stack<TydeInstruction*>& dfs_stack,
    TydeInstruction* instruction) {
  for (int i = 0; i < (int) instruction->exception_predecessors().size(); ++i)
    dfs_stack.push(instruction->exception_predecessors()[i]);
  for (int i = instruction->predecessors().size() - 1; i >= 0; --i)
    dfs_stack.push(instruction->predecessors()[i]);
}

/**
 * Check if an instruction has already been visited when looking for a given
 * register. Additionally, set the instruction as visited.
 *
 * @param visited_flags The visited flags.
 * @param instruction The instruction we are checking.
 * @param reg The register we are currently looking for.
 * @return True if the instruction has already been visited.
 */
bool TypeSolver::CheckAndSetVisited(VisitedFlags& visited_flags,
    TydeInstruction* instruction, int reg) {
  int index = instruction->index() * nb_registers_ + reg;
  if (visited_flags[index]) {
    return true;
  } else {
    visited_flags[index] = true;
    changed_flags_.push_back(index);
    return false;
  }
}

/**
 * Add an inequality to the constraint set and perform extra work for some
 * specific instructions.
 *
 * @param type_variable The type variable from which the traversal started.
 * @param instruction The instruction corresponding to a def or use of type
 *        variable type_variable.
 * @param is_source True if the def or use of the type_variable at instruction
 *        instruction is a source register.
 * @param reg The register index.
 * @param dim The type variable is dim dimensions below the use or def.
 */
void TypeSolver::AddNewInequality(TypeVariable* type_variable,
    TydeInstruction* instruction, const bool is_source,
    const int reg, int dim) {
  Type type;
  if (is_source)
    type = instruction->GetRegisterType(reg);
  else
    type = instruction->destination().type;

  TypeElement* type_element = NULL;

  if (instruction->op() == OP_MOVE_OBJECT
      || instruction->op() == OP_MOVE_OBJECT_FROM16
      || instruction->op() == OP_MOVE_OBJECT_16) {
    // Set to unknown so that is does not influence array types.
    // For example:
    //   new-array, v0, [J
    //   move-object, v1, v0
    //   aput-wide, v2, v1, v3
    // Leaving the type as kObject would prevent us from using type info from
    // the new-array instruction (since the least supertype of [J and kObject
    // is kObject and not [J).
    type.setType(kBottomObject);
    type_element = new TypeVariable(type, instruction, is_source, reg);
    type_element = AddTypeElement(type_element);

    if (is_source)
      AddTypeInequality(type_variable, type_element);
    else
      AddTypeInequality(type_element, type_variable);

    // Start a new traversal with the new register.
    StartMoveObjectTraversal(type_variable, instruction, is_source, dim);
  } else if (instruction->op() == OP_AGET_OBJECT && is_source
      && reg == instruction->sources()[0].reg) {
    // An aget-object instruction reduces the current array dimension if the
    // register use which was found corresponds to the array reference.
    Dfs(type_variable, instruction->destination().reg, instruction, dim - 1);
  } else if (instruction->op() == OP_AGET_OBJECT && !is_source) {
    // An aget-object instruction increases the current array dimension if the
    // register assignment which was found corresponds to the array component.
    BackDfs(type_variable, instruction->sources()[0].reg,
        instruction, dim + 1);
  } else {
    if (dim > 0) {
      // The array dimension > 0 implies that we have found a relation between
      // an array type and an array component type, at least one of them being
      // an unknown type.

      // First, create a dummy component type.
      int element_reg = nb_registers_;
      int array_reg = reg;
      Type element_type = kUnknown;

      if (type.dim() < dim) {
        if (debugging) {
          out_ << "Dimension of array is too small for type " << type.ToString();
          out_ << "\n";
        }
        LOGW("Dimension of array is too small for type %s\n",
            type.ToString().c_str());
        issue_ = true;
      } else {
        // Second, introduce a type inequality between the dummy component and
        // the type variable which was found by DFS.
        TypeElement* array_type_element = AddTypeElement(new TypeVariable(
            type, instruction, false, array_reg));
        element_type = type;
        element_type.setDim(element_type.dim() - dim);
        TypeElement* element_type_element = AddTypeElement(new TypeVariable(
            element_type, instruction, false, element_reg));
        AddTypeInequality(element_type_element, type_variable);
      }
    } else if (dim < 0) {
      // The array dimension > 0 implies that we have found a relation between
      // an array component type and an array type, at least one of them being
      // an unknown type.

      // First, create a dummy array type variable.
      Type right_array_type = instruction->GetRegisterType(reg);
      right_array_type.setDim(-dim);
      TypeElement* right_array_type_element = AddTypeElement(new TypeVariable(
          right_array_type, instruction, true, reg));

      // Second, introduce a type inequality between the dummy array and the
      // type variable which was found by back DFS.
      AddTypeInequality(type_variable, right_array_type_element);
    } else {
      // For all other cases, just add an inequality.
      if (type.IsUnknown() || type.IsUnknownObject()) {
        type_element = new TypeVariable(type, instruction, is_source, reg);
      } else {
        type_element = new TypeConstant(type, instruction, is_source, reg);
      }

      type_element = AddTypeElement(type_element);

      if (is_source)
        AddTypeInequality(type_variable, type_element);
      else
        AddTypeInequality(type_element, type_variable);
    }
  }
}

/**
 * Check instruction for constraints between its operands and generate
 * them if necessary.
 *
 * @param instruction The instruction for which we want to check for
 *        constraints.
 */
void TypeSolver::CheckInitialConstraints(TydeInstruction* instruction) {
  switch (instruction->op()) {
    case OP_MOVE:
    case OP_MOVE_FROM16:
    case OP_MOVE_16:
    case OP_MOVE_WIDE:
    case OP_MOVE_WIDE_FROM16:
    case OP_MOVE_WIDE_16:
      AddMoveConstraints(instruction);
      break;
    case OP_IF_EQ:
    case OP_IF_NE:
      AddIfConstraints(instruction);
      break;
    case OP_AGET:
    case OP_AGET_WIDE:
      AddAgetConstraints(instruction);
      break;
    case OP_APUT:
    case OP_APUT_WIDE:
      AddAputConstraints(instruction);
      break;
    default:
      break;
  }
}

/**
 * Add a pointer to TypeElement to the set of TypeElements. Delete the object
 * the pointer points to if the object already exists. Return a pointer to the
 * object which is actually in the set (either the argument pointer if the
 * object did not exist, or a pointer to an existing object).
 *
 * @param type_element Pointer to a type element object.
 * @return A pointer to a type element object in the set.
 */
TypeElement* TypeSolver::AddTypeElement(TypeElement* type_element) {
  std::pair<TypeElementSet::iterator, bool> result =
      type_elements_.insert(type_element);
  if (!result.second) delete type_element;

  return *(result.first);
}

/**
 * Add a new type inequality if it does not already exist.
 *
 * @param left_element The left-hand side of the inequality.
 * @param right_element The right-hand side of the inequality.
 */
void TypeSolver::AddTypeInequality(TypeElement* left_element,
    TypeElement* right_element) {
  if (debugging) {
    out_ << "Adding inequality: " << left_element->ToString(true) << " <= ";
    out_ << right_element->ToString(true) << "\n";
  }
  TypeInequality* type_inequality = new TypeInequality(left_element,
      right_element);
  std::pair<TypeInequalitySet::iterator, bool> result =
      i_list_.insert(type_inequality);
  if (!result.second) {
    delete type_inequality;
  } else {
    if (!left_element->is_constant()) {
      c_list_[left_element].push_back(type_inequality);
    }
    if (!right_element->is_constant()) {
      not_satisfied_.insert(type_inequality);
      c_list_[right_element].push_back(type_inequality);
    }
  }
}

/**
 * Add constraints between the operands of a move/move-long instruction.
 *
 * @param instruction A move/move-long instruction.
 */
void TypeSolver::AddMoveConstraints(TydeInstruction* instruction) {
  int source_reg = instruction->sources()[0].reg;
  int destination_reg = instruction->destination().reg;
  Type source_type = instruction->sources()[0].type;
  Type destination_type = instruction->destination().type;

  TypeElement* source_type_element = AddTypeElement(new TypeVariable(
      source_type, instruction, true, source_reg));
  TypeElement* destination_type_element = AddTypeElement(new TypeVariable(
      destination_type, instruction, false, destination_reg));

  // We add an equality constraint. In theory, it should only be an inequality,
  // but we are dealing with primitive types here. For float, double and long,
  // it is necessarily an equality. For int types, it should only be an
  // equality. However, as mentioned above, we don't need to keep track of all
  // integer types, so an equality is fine.
  AddTypeInequality(source_type_element, destination_type_element);
  AddTypeInequality(destination_type_element, source_type_element);
}

/**
 * Add constraints between the operands of an if-eq/if-ne instruction.
 *
 * @param instruction An if-eq/if-ne instruction.
 */
void TypeSolver::AddIfConstraints(TydeInstruction* instruction) {
  int source_reg0 = instruction->sources()[0].reg;
  int source_reg1 = instruction->sources()[1].reg;
  Type source_type0 = instruction->sources()[0].type;
  Type source_type1 = instruction->sources()[1].type;

  TypeElement* source_type_element0 = AddTypeElement(new TypeVariable(
      source_type0, instruction, true, source_reg0));
  TypeElement* source_type_element1 = AddTypeElement(new TypeVariable(
      source_type1, instruction, true, source_reg1));
  AddTypeInequality(source_type_element0, source_type_element1);
  AddTypeInequality(source_type_element1, source_type_element0);
}

/**
 * Add constraints between the operands of an aget/aget-wide instruction.
 *
 * @param instruction An aget/aget-wide instruction.
 */
void TypeSolver::AddAgetConstraints(TydeInstruction* instruction) {
  int element_reg = instruction->destination().reg;
  int array_reg = instruction->sources()[0].reg;
  Type element_type = instruction->destination().type;

  TypeElement* element_type_element = AddTypeElement(new TypeVariable(
      element_type, instruction, false, element_reg));
  Type array_type(element_type.type(), 1, element_type_element);
  TypeElement* array_type_element = AddTypeElement(new TypeVariable(
      array_type, instruction, true, array_reg));
  element_type_element->set_array_type_element(array_type_element);
}

/**
 * Add constraints between the operands of an aput/aput-wide instruction.
 *
 * @param instruction An aput/aput-wide instruction.
 */
void TypeSolver::AddAputConstraints(TydeInstruction* instruction) {
  int element_reg = instruction->sources()[2].reg;
  int array_reg = instruction->sources()[0].reg;
  Type element_type = instruction->sources()[2].type;

  TypeElement* element_type_element = AddTypeElement(new TypeVariable(
      element_type, instruction, true, element_reg));
  Type array_type(element_type.type(), 1, element_type_element);
  TypeElement* array_type_element = AddTypeElement(new TypeVariable(
      array_type, instruction, true, array_reg));
  element_type_element->set_array_type_element(array_type_element);
}

/**
 * In the case of a move-object instruction, start a new DFS/back DFS traversal
 * to look for uses/assignments of the register to/from which the move is done.
 *
 * The idea here is that, in the case of a move-object instruction, we only
 * trigger this traversal if the instruction is encountered during a traversal
 * for another ambiguous register. The new traversal might lead us to some
 * type constant. Otherwise, just knowing that the input and output of the
 * move-object instruction are objects is sufficient, so we don't need to
 * trigger this traversal for the move-object instruction alone.
 *
 * @param type_variable The initial type variable the traversal started from.
 * @param instruction The current move instruction.
 * @param is_source True is the current instruction was found by DFS (and
 *        not back DFS).
 * @param dim The current array dimension.
 */
void TypeSolver::StartMoveObjectTraversal(TypeVariable* type_variable,
    TydeInstruction* instruction, const bool is_source, const int dim) {
  int other_reg;
  Type other_type;

  if (is_source) {
    other_reg = instruction->destination().reg;
  } else {
    other_reg = instruction->sources()[0].reg;
  }

  // Start new DFS.
  if (is_source)
    Dfs(type_variable, other_reg, instruction, dim);
  else
    BackDfs(type_variable, other_reg, instruction, dim);
}

/**
 * Display the system of constraints.
 */
void TypeSolver::DisplayConstraints() {
  for (TypeInequalitySet::iterator it = i_list_.begin();
      it != i_list_.end(); ++it) {
    out_ << (*it)->ToString() << '\n';
  }
}

/**
 * Solve all type 1 constraints (see paper "Retargeting Android applications
 * to Java Bytecode").
 *
 * Type constraints have the following forms:
 *   1) type constant <= type variable.
 *   2) type variable <= type constant.
 *   3) type variable <= type variable.
 */
void TypeSolver::SolveConstraints() {
  while (!not_satisfied_.empty()) {
    std::set<TypeInequality*>::iterator it = not_satisfied_.begin();
    TypeInequality* type_inequality = *it;
    not_satisfied_.erase(it);
    Type merged_type = MergeTypes(type_inequality->left_element(),
        type_inequality->right_element());
    if (merged_type.IsConflict()) {
      if (offending_inequalities_.find(type_inequality) ==
          offending_inequalities_.end()) {
        offending_inequalities_.insert(type_inequality);
        LOGW("Type conflict: %s\n", type_inequality->ToString(true).c_str());
        if (debugging)
          out_ << "Type conflict: " << type_inequality->ToString(true) << "\n";
        issue_ = true;
//        Dare::AddConflictedClass(class_descriptor_);
      }
      if (debugging && display_offending_path) {
        out_ << "Offending path: \n";
        out_ << FindPath(type_inequality->left_element()->instruction(),
            type_inequality->right_element()->instruction(),
            type_inequality->left_element()->reg()) << "\n";
      }
    } else if (merged_type != type_inequality->right_element()->type()) {
      type_inequality->right_element()->set_type(merged_type);
      TypeElementToInequalityMap::iterator it = c_list_.find(
          type_inequality->right_element());

      if (it != c_list_.end()) {
        vector<TypeInequality*>& type_inequalities = it->second;

        for (int i = 0; i < (int) type_inequalities.size(); ++i)
          not_satisfied_.insert(type_inequalities[i]);
      }
    }
  }
}

/**
 * Find the join of two types.
 *
 * @param left A type element.
 * @param right Another type element.
 * @return The join of the two types.
 */
Type TypeSolver::MergeTypes(TypeElement* left, TypeElement* right) {
  if (right->type() == left->type())
    return left->type();

  if (left->type().IsPrimitive()) {
    if (right->type().IsPrimitive() || right->type().IsTrioUnknown()) {
      return Type(kPrimitiveMergeTable[left->type().type()]
                                       [right->type().type()]);
    } else {
      return Type(kConflict);
    }
  } else if (left->type().IsTrioUnknown()) {
    if (right->type().IsPrimitive() || right->type().IsTrioUnknown()) {
      return Type(kPrimitiveMergeTable[left->type().type()]
                                      [right->type().type()]);
    } else {
      // TrioUnknown <= reference
      return right->type();
    }
  } else {
    if (right->type().IsPrimitive()) {
      return Type(kConflict);
    } else {
      return MergeReferenceTypes(left, right);
    }
  }
}

/**
 * Find the join of two reference types.
 *
 * @param left A reference type element.
 * @param right Another reference type element.
 * @return The join of the two reference types.
 */
Type TypeSolver::MergeReferenceTypes(TypeElement* left, TypeElement* right) {
  if (right->type() == left->type())
    return left->type();

  if (left->type().IsArray()) {
    if (right->type().IsArray()) {
      return MergeArrayTypes(left, right);
    } else if (right->type().IsNAObject()) {
      return Type(kObject);
    } else if (right->type().IsTrioUnknown()) {
      return left->type();
    } else if (right->type().IsUnknownObject()) {
      return right->type();
    } else if (right->type().IsBottomObject()) {
      return left->type();
    } else {
      if (debugging) {
        out_ << "Error while merging reference types: unexpected right type ";
        out_ << right->type().ToString() << " for left type ";
        out_ << left->type().ToString() << "\n";
      }
      LOGW("Error while merging reference types: unexpected right type %s "
          "for left type %s\n",
          right->type().ToString().c_str(), left->type().ToString().c_str());
      issue_ = true;
      return Type(kConflict);
    }
  } else if (left->type().IsNAObject()) {
    if (right->type().IsTrioUnknown()) {
      return left->type();
    } else {
      return Type(kObject);
    }
  } else if (left->type().IsTrioUnknown()) {
    return right->type();
  } else if (left->type().IsUnknownObject()) {
    return left->type();
  } else if (left->type().IsBottomObject()) {
    return right->type();
  } else {
    if (debugging) {
      out_ << "Error while merging reference types: unexpected left type ";
      out_ << left->type().ToString() << " with left type ";
      out_ << right->type().ToString() << "\n";
    }
    LOGW("Error while merging reference types: unexpected left type %s with "
        "left type %s\n",
        left->type().ToString().c_str(), right->type().ToString().c_str());
    issue_ = true;
    return Type(kConflict);
  }
}

/**
 * Find the join of two array types.
 *
 * @param left An array type element.
 * @param right An array type element.
 * @return The join of the two array types.
 */
Type TypeSolver::MergeArrayTypes(TypeElement* left, TypeElement* right) {
  if (left->type().IsUnknown() || left->type().IsAObjectUnknown())
    return right->type();

  if (right->type().IsUnknown() || right->type().IsAObjectUnknown())
    return left->type();

  int left_dim = left->type().dim();
  int right_dim = right->type().dim();

  if (left_dim == right_dim) {
    // Case same primitive type should be taken care of in MergeReferenceType.
    if (left->type().IsNAObject()) {
      if (right->type().IsNAObject()) {
        return left->type();
      } else {
        return Type(kObject, left_dim);
      }
    } else if (left->type().IsUnknownObject()) {
      return left->type();
    } else if (left_dim > 1) {
      // Merge [[F and [[D for example.
      return Type(kObject, left_dim - 1);
    } else {
      // Merge [F and [D for example.
      return Type(kObject);
    }
  } else {
    int min_dim = (left_dim < right_dim) ? left_dim : right_dim;
    return Type(kObject, min_dim);
  }
}

/**
 * Solve all type 2 and type 3 constraints.
 */
void TypeSolver::SolveUnconstrainedTypes() {
  // Find all type 2 and type 3 constraints.
  for (TypeInequalitySet::iterator it = i_list_.begin();
      it != i_list_.end(); ++it) {
    TypeElement* left_element = (*it)->left_element();
    if (left_element->type().IsUnknown())
      worklist_.insert(left_element);
  }

  while (!worklist_.empty()) {
    changed_ = false;
    for (TypeElementSet::iterator it = worklist_.begin();
        it != worklist_.end(); ++it) {
      if (FindLeftTypeConstraint(*it)) {
        // A type 2 constraint has been solved. Try to solve other constraints
        // using the newly found type.
        SolveConstraintsForNewLeftType(*it);
        worklist_.erase(it);
        changed_ = true;
        break;
      }
    }

    // If no type has been found, it means that only type 3 constraints remain.
    if (!changed_) {
      TypeElement* left_element = *(worklist_.begin());
      left_element->set_type(FindDefaultType(left_element->type()));
      SolveConstraintsForNewLeftType(left_element);
      worklist_.erase(worklist_.begin());
    }
  }

  // Unconstrained type variables need to get a safe default type.
  for (TypeElementSet::iterator it = type_elements_.begin();
      it != type_elements_.end(); ++it) {
    TypeElement* left_element = *it;
    if (left_element->type().IsUnknown()) {
      left_element->set_type(FindDefaultType(left_element->type()));
    }
  }
}

/**
 * After finding a left-hand side type in an inequality, try to solve other
 * inequalities which have the same type variable on the left-hand side. That
 * corresponds to the case where type 2 constraints are turned into type 1
 * constraints.
 */
void TypeSolver::SolveConstraintsForNewLeftType(TypeElement* left_element) {
  TypeElementToInequalityMap::iterator it = c_list_.find(left_element);
  if (it != c_list_.end()) {
    vector<TypeInequality*>& type_inequalities = it->second;
    for (int i = 0; i < (int) type_inequalities.size(); ++i) {
      not_satisfied_.insert(type_inequalities[i]);
    }
    SolveConstraints();
  }

//  vector<TypeInequality*>* type_inequalities = c_list_.find(left_element);
//  if (type_inequalities != NULL) {
//    for (int i = 0; i < (int) type_inequalities->size(); ++i)
//      not_satisfied_.insert((*type_inequalities)[i]);
//    SolveConstraints();
//  }
}

/**
 * Find a type constraint for a type element.
 *
 * @param left_element The type element for which we are trying to find a type.
 * @return True if a type was found.
 */
bool TypeSolver::FindLeftTypeConstraint(TypeElement* left_element) {
  if (debugging)
    out_ << "Finding type constraint for " << left_element->ToString() << "\n";

  // Find all type inequalities in which the type element appears.
  TypeElementToInequalityMap::iterator it = c_list_.find(left_element);

  if (it != c_list_.end()) {
    vector<TypeInequality*>& type_inequalities = it->second;
//  vector<TypeInequality*>* type_inequalities = c_list_.find(left_element);

//  if (type_inequalities != NULL) {
    // Go through all inequalities and try to find a type.
    for (int i = 0; i < (int) type_inequalities.size(); ++i) {
      TypeElement* right_element = type_inequalities[i]->right_element();
      // If the element appear on the right-hand side, it is a type 3
      // inequality. We won't get any useful information from that.
      if (right_element == left_element)
        continue;

      if (debugging)
        out_ << "Inequality: " << type_inequalities[i]->ToString() << "\n";

      Type candidate_type;
      if (right_element->type().IsIntSubtype()) {
        candidate_type.setType(kBoolean);
      } else if (right_element->type().IsFloat()) {
        candidate_type.setType(kFloat);
      } else if (right_element->type().IsLong()) {
        candidate_type.setType(kLong);
      } else if (right_element->type().IsDouble()) {
        candidate_type.setType(kDouble);
      } else if (right_element->type().IsObject()) {
        candidate_type.setType(kObject);
      }
      if (!candidate_type.IsUnknown()) {
        // If we found a type 2 inequality, we have a potential solution.
        // Check that we are not introducing a type conflict.
        TypeConstant* type_constant = new TypeConstant(candidate_type, NULL,
            true, 0);
        Type merged_type = MergeTypes(type_constant, right_element);
        delete type_constant;
        if (!merged_type.IsConflict()) {
          left_element->set_type(candidate_type);
          return true;
        }
      }
    }
  }

  return false;
}

/**
 * Find a default type given a register ambiguity.
 *
 * @param type The ambiguous type for which we want an unambiguous default
 *        type.
 * @return The default type.
 */
Type TypeSolver::FindDefaultType(const Type& type) {
  switch (type.type()) {
    case kTrioUnknown:
      return Type(kBoolean);
    case kFIUnknown:
      return Type(kBoolean);
    case kDLUnknown:
      return Type(kDouble);
    case kAFIUnknown:
      return Type(kInt, 1);
    case kADLUnknown:
      return Type(kDouble, 1);
    case kACSUnknown:
      return Type(kShort, 1);
    default:
      LOGW("Unknown type to default: %s\n", type.ToString().c_str());
      if (debugging)
        out_ << "Unknown type to default: " << type.ToString() << "\n";
      issue_ = true;
      return Type(kBoolean);
  }
}

/**
 * Update instruction types using the solved types.
 */
void TypeSolver::UpdateInstructionTypes() {
//  type_elements_.iterate(&UpdateInstructionTypesCallback, this);
  for (TypeElementSet::iterator it = type_elements_.begin();
      it != type_elements_.end(); ++it) {
    TypeElement* type_element = (*it);
    if (!type_element->is_constant() && type_element->reg() < nb_registers_) {
      if (type_element->is_source()) {
        type_element->instruction()->SetSourceTypeByRegisterIfUnknown(
            type_element->reg(), type_element->type());
      } else {
        if (type_element->instruction()->destination().type.IsUnknown())
          type_element->instruction()->SetDestinationType(
              type_element->type());
      }
    }
  }
}

/**
 * Find a path between two instructions which is def-clear for a given
 * register. Used for debugging purposes for now.
 *
 * @param start The instruction from which we want to start.
 * @param end The instruction we are trying to reach.
 * @param reg The register for which we want to avoid assignments on the path.
 * @return A string representation of the path.
 */
std::string TypeSolver::FindPath(const TydeInstruction* start,
    const TydeInstruction* end, const int reg) const {
  vector<bool> visited(ins_count_, false);
  std::ostringstream ss;
  ss << std::hex << start->original_offset() << " - ";
  return FindPathHelper(start, end, ss.str(), visited, reg);
}

/**
 * Recursive helper function to find a path between two instructions that is
 * def-clear for a given register.
 *
 * Yet another DFS traversal.
 *
 * @param start The instruction from which we want to start.
 * @param end The instruction we are trying to reach.
 * @param reg The register for which we want to avoid assignments on the path.
 * @param path The path on the current branch.
 * @param visited Array of flags indicating which instructions have already
 *        been visited.
 * @return A string representation of the path.
 */
std::string TypeSolver::FindPathHelper(const TydeInstruction* start,
    const TydeInstruction* end, std::string path,
    vector<bool>& visited, const int reg) const {
  if (visited[start->index()])
    return "";
  visited[start->index()] = true;

  if (start == end)
    return path;

  int new_reg = reg;
  if (start->IsSource(reg) && (start->op() == OP_MOVE_OBJECT
      || start->op() == OP_MOVE_OBJECT_FROM16
      || start->op() == OP_MOVE_OBJECT_16)) {
    new_reg = start->destination().reg;
  }

  for (int i = 0; i < (int) start->successors().size(); ++i) {
    if (start->successors()[i]->has_destination()
        && start->successors()[i]->destination().reg == reg)
      continue;
    std::ostringstream ss;
    ss << std::hex << start->successors()[i]->original_offset() << " - ";
    std::string result = FindPathHelper(start->successors()[i], end,
        path + ss.str(), visited, new_reg);
    if (result != "")
      return result;
  }

  for (int i = 0; i < (int) start->exception_successors().size(); ++i) {
    if (start->exception_successors()[i]->has_destination()
        && start->exception_successors()[i]->destination().reg == reg)
      continue;
    std::ostringstream ss;
    ss << std::hex << start->exception_successors()[i]->original_offset();
    ss << " - ";
    std::string result = FindPathHelper(start->exception_successors()[i], end,
        path + ss.str(), visited, new_reg);
    if (result != "")
      return result;
  }

  return "";
}

/**
 * Clean things up. Should be called as the last step of the type solver.
 *
 * @param dcode The Tyde code we just typed.
 */
void TypeSolver::Finalize(TydeBody& dcode) {
  for (TypeInequalitySet::iterator it = i_list_.begin(); it != i_list_.end();
      ++it)
    delete *it;
  for (TypeElementSet::iterator it = type_elements_.begin();
      it != type_elements_.end(); ++it)
    delete *it;
//  i_list_.iterate(&DeleteTypeInequalitiesCallback, NULL);
//  type_elements_.iterate(&DeleteTypeElementsCallback, NULL);

  TydeInstruction* first_instruction = dcode[0];
  while (first_instruction->predecessors().size() != 0) {
    delete first_instruction->predecessors().back();
    first_instruction->PopPredecessor();
    dcode.pop_back();
  }
}

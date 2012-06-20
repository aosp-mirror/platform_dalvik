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
 * type_solver.h
 *
 * The type solver finds legal types for ambiguous registers.
 */

#ifndef TYPING_TYPE_SOLVER_H_
#define TYPING_TYPE_SOLVER_H_


#include <map>
#include <set>
#include <sstream>
#include <stack>
#include <vector>

#include "typing/type_element.h"
#include "typing/type_inequality.h"


class ConstantPool;
class MethodInfo;
class TydeBody;
class TydeInstruction;
class TypeVariable;


class TypeSolver {
 public:
  TypeSolver(const TydeBody& tyde_body, const int nb_registers)
      : nb_registers_(nb_registers),
        issue_(false),
        class_descriptor_(NULL) {}

  /**
   * Point of entry for the type solver.
   *
   * @param dcode The Tyde bytecode we want to type.
   * @param method The method which contains the code to be typed.
   */
  void SolveTypes(MethodInfo* method);

 private:
  /**
   * Given a type inequality left <= right, this table answers two questions:
   *   - Is there a type conflict?
   *   - If not, what should be the type of the right-hand side?
   */
  static const VarType kPrimitiveMergeTable[kPrimitiveTypeCount]
                                            [kPrimitiveTypeCount];

  struct CustomComparator {
    bool operator()(const TypeElement* lhs, const TypeElement* rhs) const {
      return *lhs < *rhs;
    }
    bool operator()(const TypeInequality* lhs,
        const TypeInequality* rhs) const {
      return *lhs < *rhs;
    }
  };

  typedef std::map<TypeElement*, std::vector<TypeInequality*>,
      CustomComparator> TypeElementToInequalityMap;
  typedef std::set<TypeElement*, CustomComparator> TypeElementSet;
  typedef std::set<TypeInequality*, CustomComparator> TypeInequalitySet;
  typedef std::vector<bool> VisitedFlags;


  /**
   * Do some pre-processing for the type solver. It should be called before
   * any analysis is done.
   *
   * @param method The method we are currently typing.
   */
  void Init(MethodInfo* method);

  /**
   * Generate the type constraints related to the register assignments and
   * uses in the method.
   *
   * @param method The method we are currently typing.
   */
  void GenerateConstraints(MethodInfo* method);

  /**
   * Reset the visited flags before a DFS traversal. To be called before a DFS
   * or back-DFS traversal when computing def-use chains.
   */
  void ResetVisitedFlags();

  /**
   * Perform a DFS traversal of the CFG starting at a given instruction, looking
   * for uses of a given register.
   *
   * @param first_instruction The instruction at which the DFS should start.
   * @param reg The register for which we are looking for uses.
   * @param dim The current array dimension.
   */
  void Dfs(TydeInstruction* first_instruction, int reg, int dim = 0);

  /**
   * Helper function for the DFS procedure.
   *
   * @param type_variable The type variable corresponding to the register for
   *        which we are looking for uses.
   * @param reg The register for which we are looking for uses.
   * @param first_instruction The instruction at which the DFS is starting.
   * @param dim The current array dimension.
   */
  void Dfs(TypeVariable* type_variable, int reg,
      TydeInstruction* first_instruction, int dim);

  /**
   * Perform a back DFS traversal of the CFG starting at a given instruction,
   * looking for assignments of a given register.
   *
   * @param first_instruction The instruction at which the back DFS should
   *        start.
   * @param reg The register for which we are looking for assignments.
   * @param dim The current array dimension.
   */
  void BackDfs(TydeInstruction* first_instruction, int reg, int dim = 0);

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
  void BackDfs(TypeVariable* type_variable, int reg,
      TydeInstruction* first_instruction, int dim);

  /**
   * Put all successors of an instruction on top of a stack.
   *
   * @param dfs_stack The stack on which the successors should be put.
   * @param instruction The instruction whose successors we are looking for.
   */
  void AddNewSuccessors(std::stack<TydeInstruction*>& dfs_stack,
      TydeInstruction* instruction);

  /**
   * Put all predecessors of an instruction on top of a stack.
   *
   * @param dfs_stack The stack on which the predecessors should be put.
   * @param instruction The instruction whose predecessors we are looking for.
   */
  void AddNewPredecessors(std::stack<TydeInstruction*>& dfs_stack,
      TydeInstruction* instruction);

  /**
   * Check if an instruction has already been visited when looking for a given
   * register. Additionally, set the instruction as visited.
   *
   * @param visited_flags The visited flags.
   * @param instruction The instruction we are checking.
   * @param reg The register we are currently looking for.
   * @return True if the instruction has already been visited.
   */
  bool CheckAndSetVisited(VisitedFlags& visited_flags,
      TydeInstruction* instruction, int reg);

  /**
   * Add an inequality to the constraint set and perform extra work for some
   * specific instructions.
   *
   * @param type_variable The type variable from which the traversal started.
   * @param instruction The instruction corresponding to a def or use of type
   *        variable type_variable.
   * @param is_source True if the def or use of the type_variable at
   *        instruction instruction is a source register.
   * @param reg The register index.
   * @param dim The type variable is dim dimensions below the use or def.
   */
  void AddNewInequality(TypeVariable* type_variable,
      TydeInstruction* instruction, const bool is_source,
      const int reg, int dim);

  /**
   * Check instruction for constraints between its operands and generate
   * them if necessary.
   *
   * @param instruction The instruction for which we want to check for
   *        constraints.
   */
  void CheckInitialConstraints(TydeInstruction* instruction);

  /**
   * Add a pointer to TypeElement to the set of TypeElements. Delete the object
   * the pointer points to if the object already exists. Return a pointer to the
   * object which is actually in the set (either the argument pointer if the
   * object did not exist, or a pointer to an existing object).
   *
   * @param type_element Pointer to a type element object.
   * @return A pointer to a type element object in the set.
   */
  TypeElement* AddTypeElement(TypeElement* type_element);

  /**
   * Add a new type inequality if it does not already exist.
   *
   * @param left_element The left-hand side of the inequality.
   * @param right_element The right-hand side of the inequality.
   */
  void AddTypeInequality(TypeElement* left_element,
      TypeElement* right_element);

  /**
   * Add constraints between the operands of a move/move-long instruction.
   *
   * @param instruction A move/move-long instruction.
   */
  void AddMoveConstraints(TydeInstruction* instruction);

  /**
   * Add constraints between the operands of an if-eq/if-ne instruction.
   *
   * @param instruction An if-eq/if-ne instruction.
   */
  void AddIfConstraints(TydeInstruction* instruction);

  /**
   * Add constraints between the operands of an aget/aget-wide instruction.
   *
   * @param instruction An aget/aget-wide instruction.
   */
  void AddAgetConstraints(TydeInstruction* instruction);

  /**
   * Add constraints between the operands of an aput/aput-wide instruction.
   *
   * @param instruction An aput/aput-wide instruction.
   */
  void AddAputConstraints(TydeInstruction* instruction);

  /**
   * In the case of a move instruction, start a new DFS/back DFS traversal to
   * look for uses/assignments of the register to/from which the move is done.
   *
   * @param type_variable The initial type variable the traversal started from.
   * @param instruction The current move instruction.
   * @param is_source True is the current instruction was found by DFS (and
   *        not back DFS).
   * @param dim The current array dimension.
   */
  void StartMoveObjectTraversal(TypeVariable* type_variable,
      TydeInstruction* instruction, const bool is_source, const int dim);

  /**
   * Display the system of constraints.
   */
  void DisplayConstraints();

  /**
   * Solve all type 1 constraints (see paper "Retargeting Android applications
   * to Java Bytecode").
   */
  void SolveConstraints();

  /**
   * Find the join of two types.
   *
   * @param left A type element.
   * @param right Another type element.
   * @return The join of the two types.
   */
  Type MergeTypes(TypeElement* left, TypeElement* right);

  /**
   * Find the join of two reference types.
   *
   * @param left A reference type element.
   * @param right Another reference type element.
   * @return The join of the two reference types.
   */
  Type MergeReferenceTypes(TypeElement* left, TypeElement* right);

  /**
   * Find the join of two array types.
   *
   * @param left An array type element.
   * @param right An array type element.
   * @return The join of the two array types.
   */
  Type MergeArrayTypes(TypeElement* left, TypeElement* right);

  /**
   * Solve all type 2 and type 3 constraints.
   */
  void SolveUnconstrainedTypes();

  /**
   * After finding a left-hand side type in an inequality, try to solve other
   * inequalities which have the same type variable on the left-hand side. That
   * corresponds to the case where type 2 constraints are turned into type 1
   * constraints.
   */
  void SolveConstraintsForNewLeftType(TypeElement* left_element);

  /**
   * Find a type constraint for a type element.
   *
   * @param left_element The type element for which we are trying to find a
   *        type.
   * @return True if a type was found.
   */
  bool FindLeftTypeConstraint(TypeElement* left_element);

  /**
   * Find a default type given a register ambiguity.
   *
   * @param type The ambiguous type for which we want an unambiguous default
   *        type.
   * @return The default type.
   */
  Type FindDefaultType(const Type& type);

  /**
   * Update instruction types using the solved types.
   */
  void UpdateInstructionTypes();

  /**
   * Find a path between two instructions which is def-clear for a given
   * register. Used for debugging purposes for now.
   *
   * @param start The instruction from which we want to start.
   * @param end The instruction we are trying to reach.
   * @param reg The register for which we want to avoid assignments on the path.
   * @return A string representation of the path.
   */
  std::string FindPath(const TydeInstruction* start,
      const TydeInstruction* end, const int reg) const;

  /**
   * Recursive helper function to find a path between two instructions that is
   * def-clear for a given register.
   *
   * @param start The instruction from which we want to start.
   * @param end The instruction we are trying to reach.
   * @param reg The register for which we want to avoid assignments on the path.
   * @param path The path on the current branch.
   * @param visited Array of flags indicating which instructions have already
   *        been visited.
   * @return A string representation of the path.
   */
  std::string FindPathHelper(const TydeInstruction* start,
      const TydeInstruction* end, std::string path,
      std::vector<bool>& visited, const int reg) const;

  /**
   * Clean things up. Should be called as the last step of the type solver.
   *
   * @param dcode The Tyde code we just typed.
   */
  void Finalize(TydeBody& dcode);


  const int nb_registers_;
  VisitedFlags is_visited_current_;
  TypeElementSet type_elements_;
  TypeElementSet worklist_;
  TypeInequalitySet i_list_;
  TypeInequalitySet offending_inequalities_;
  TypeElementToInequalityMap c_list_;
  std::set<TypeInequality*> not_satisfied_;
  bool issue_;
  std::ostringstream out_;
  int ins_count_;
  const char* class_descriptor_;
  bool changed_;
  std::vector<int> changed_flags_;
};


#endif /* TYPING_TYPE_SOLVER_H_ */

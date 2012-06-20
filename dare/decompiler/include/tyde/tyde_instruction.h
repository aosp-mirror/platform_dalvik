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
 * tyde_instruction.h
 *
 * A Tyde instruction. Tyde instructions include typed registers
 * and pointers to predecessors and successors.
 */

#ifndef TYDE_TYDE_INSTRUCTION_H_
#define TYDE_TYDE_INSTRUCTION_H_


#include <string>
#include <vector>

#include "libdex/DexOpcodes.h"
#include "libdex/InstrUtils.h"

#include "int_types.h"
#include "typing/type.h"


struct TypedValue {
  int reg;
  Type type;
};


class ConstantPool;
class ConstantPoolInfo;
class MethodInfo;


class TydeInstruction {
 public:
  TydeInstruction(const DecodedInstruction* dec_ins,
      const int original_offset)
      : op_(dec_ins->opcode),
        has_destination_(false),
        original_offset_(original_offset),
        label_(-1),
        reference_(NULL),
        data_(NULL),
        reachable_(false) {}
  TydeInstruction(const int reg, const Type& type,
      const int original_offset = 0, const char* error_descriptor = NULL);
  ~TydeInstruction() { delete data_; }

  Opcode op() const { return op_; }
  int original_offset() const { return original_offset_; }
  const std::vector<TydeInstruction*>& successors() const {
    return successors_;
  }
  const std::vector<TydeInstruction*>& exception_successors() const {
    return exception_successors_;
  }
  const std::vector<TydeInstruction*>& predecessors() const {
    return predecessors_;
  }
  const std::vector<TydeInstruction*>& exception_predecessors() const {
    return exception_predecessors_;
  }
  const std::vector<TypedValue>& sources() const { return sources_; }
  bool has_destination() const { return has_destination_; }
  const TypedValue& destination() const { return destination_; }
  const ConstantPoolInfo* reference() const { return reference_; }
  void set_reference(const ConstantPoolInfo* ref) { reference_ = ref; }

  void set_index(const int index) { index_ = index; }
  int index() const { return index_; }
  int label() const { return label_; }
  void set_label(int label) { label_ = label; }

  bool reachable() const { return reachable_; }
  void set_reachable(bool reachable) { reachable_ = reachable; }

  u4 constant() const { return ((ConstantData*) data_)->constant; }
  u8 wide_constant() const { return ((WideConstantData*) data_)->constant; }
  int first_key() const { return ((SwitchData*) data_)->keys[0]; }
  const std::vector<int>& targets() const {
    return ((SwitchData*) data_)->targets;
  }
  const std::vector<int>& keys() const {
    return ((SwitchData*) data_)->keys;
  }
  const std::vector<u8>& data() const {
    return ((FillArrayDataData*) data_)->data;
  }
  const std::vector<const ConstantPoolInfo*>& data_ptr() const {
    return ((FillArrayDataData*) data_)->data_ptr;
  }
  const Type& array_type() const {
    return ((FilledNewArrayData*) data_)->array_type;
  }
  const char* error_descriptor() const {
    return ((ErrorDescriptorData*) data_)->error_descriptor;
  }
  void AddDataPtr(const ConstantPoolInfo* ref) {
    ((FillArrayDataData*) data_)->data_ptr.push_back(ref);
  }

  void AddSuccessor(TydeInstruction* ins);
  void AddExceptionSuccessor(TydeInstruction* ins);
  void AddPredecessor(TydeInstruction* ins) {
    predecessors_.push_back(ins);
  }
  void AddExceptionPredecessor(TydeInstruction* ins) {
    exception_predecessors_.push_back(ins);
  }
  void PopPredecessor() { predecessors_.pop_back(); }
  void SetDestinationType(const Type& type) { destination_.type = type; }
  void SetSourceType(const int source_index, const Type& type) {
    sources_[source_index].type = type;
  }

  /**
   * Set source register type.
   *
   * @param reg The source register whose type should be set.
   * @param type The type to which the register should be set.
   */
  void SetSourceTypeByRegister(const int reg, const Type& type);

  /**
   * Source source register type if the current type is unknown.
   *
   * @param reg The source register whose type should be set.
   * @param type The type to which the register should be set.
   */
  void SetSourceTypeByRegisterIfUnknown(const int reg, const Type& type);

  /**
   * Is the register a source register
   *
   * @param reg Register index.
   * @return True if the register is a source register.
   */
  bool IsSource(const int reg) const;

  /**
   * Find the first register index which is unknown.
   *
   * @return The first register index which is unknown.
   */
  int FindUnknownSource() const;

  /**
   * Convert the current instruction to a descriptive string.
   *
   * @return A string describing this instruction.
   */
  std::string ToString() const;

  /**
   * Print this instruction to stdout.
   *
   * @param indent Indent before instruction.
   */
  void Print(const int indent) const;

  /**
   * Get the type of a given source register.
   *
   * @param reg The register whose type we want.
   * @return The type of the given source register if found, an unknown type
   *         otherwise.
   */
  Type GetRegisterType(const int reg) const;

  /**
   * Parse an instruction.
   *
   * @param dec_ins The input Dalvik decoded instruction.
   * @param insn_idx The index of the instruction in the Dalvik bytecode array.
   * @param insns A pointer to the instruction in the bytecode array.
   * @param method A pointer to the method being parsed.
   * @param cp The constant pool of the class being generated.
   */
  void ParseInstruction(const DecodedInstruction* dec_ins, int insn_idx,
      const u2* insns, MethodInfo* method, ConstantPool& cp);

 private:
  /*
   * Types used to store extra data for some instructions.
   */
  struct InstructionData {
    virtual ~InstructionData() {}
  };
  struct WideConstantData : public InstructionData {
    u8 constant;
  };
  struct ConstantData : public InstructionData {
    u4 constant;
  };
  struct SwitchData : public InstructionData {
    std::vector<int> keys;
    std::vector<int> targets;
  };
  struct FillArrayDataData : public InstructionData {
    std::vector<u8> data;
    std::vector<const ConstantPoolInfo*> data_ptr;
  };
  struct FilledNewArrayData : public InstructionData {
    Type array_type;
  };
  struct ErrorDescriptorData : public InstructionData {
    const char* error_descriptor;
  };

  void set_destination(const int register_index, const VarType type,
      const int dim = 0);
  void set_destination(const int register_index, const Type& type);
  void AddSource(const int register_index, const VarType type,
      const int dim = 0);
  void AddSource(const int register_index, const Type& type);

  /**
   * Parse a fill-array-data pseudo-instruction.
   *
   * @param position The current position in dex bytecode.
   * @return The primitive type contained in the array.
   */
  VarType ParseFillArrayData(const u2* position);

  /**
   * Parse a packed-switch-data pseudo-instruction.
   *
   * @param position The current position in dex bytecode.
   */
  void ParsePackedSwitch(const u2* position);

  /**
   * Parse a sparse-switch-data pseudo-instruction.
   *
   * @param position The current position in dex bytecode.
   */
  void ParseSparseSwitch(const u2* position);

  /**
   * Determine if a return value should be popped from the stack after a method
   * invocation.
   *
   * @param next The next instruction.
   * @param return_type The invoked method's return type.
   * @return A void type if no value is returned, a pop type if a value is
   *         returned.
   */
  Type PopReturnValue(const DecodedInstruction& next,
      const Type& return_type);

  Opcode op_;
  bool has_destination_;
  const int original_offset_;
  int label_;
  const ConstantPoolInfo* reference_;
  std::vector<TypedValue> sources_;
  TypedValue destination_;
  int index_;
  std::vector<TydeInstruction*> successors_;
  std::vector<TydeInstruction*> exception_successors_;
  std::vector<TydeInstruction*> predecessors_;
  std::vector<TydeInstruction*> exception_predecessors_;
  InstructionData* data_;
  bool reachable_;
};


#endif /* TYDE_TYDE_INSTRUCTION_H_ */

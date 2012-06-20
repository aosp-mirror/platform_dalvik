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
 * tyde_instruction.cpp
 *
 * Base class for a Tyde instruction. Tyde instructions include typed registers
 * and pointers to predecessors and successors.
 */


#include "tyde/tyde_instruction.h"

#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>

#include "libdex/DexOpcodes.h"

#include "class_file/class_file.h"
#include "class_file/class_info.h"
#include "class_file/code_attribute.h"
#include "class_file/field_ref_info.h"
#include "class_file/interface_method_ref_info.h"
#include "class_file/string_info.h"
#include "class_file/method_info.h"
#include "class_file/method_ref_info.h"
#include "class_file/field_ref_info.h"


TydeInstruction::TydeInstruction(const int reg, const Type& type,
    const int original_offset/* = 0*/,
    const char* error_descriptor/* = NULL*/)
    : original_offset_(original_offset),
      label_(-1),
      reference_(NULL),
      data_(NULL),
      reachable_(false) {
  if (error_descriptor == NULL) {
    op_ = OP_NOP;
  } else {
    op_ = OP_THROW_VERIFICATION_ERROR;
    ErrorDescriptorData* edd = new ErrorDescriptorData;
    data_ = edd;
    edd->error_descriptor = error_descriptor;
  }
  if (!type.IsUnknown()) {
    set_destination(reg, type);
    has_destination_ = true;
  } else {
    has_destination_ = false;
  }
}

void TydeInstruction::AddSuccessor(TydeInstruction* ins) {
  successors_.push_back(ins);
  ins->AddPredecessor(this);
}

void TydeInstruction::AddExceptionSuccessor(TydeInstruction* ins) {
  exception_successors_.push_back(ins);
  ins->AddExceptionPredecessor(this);
}

/**
 * Source source register type.
 *
 * @param reg The source register whose type should be set.
 * @param type The type to which the register should be set.
 */
void TydeInstruction::SetSourceTypeByRegister(const int reg,
    const Type& type) {
  for (int i = 0; i < (int) sources_.size(); ++i)
    if (sources_[i].reg == reg)
      sources_[i].type = type;
}

/**
 * Source source register type if the current type is unknown.
 *
 * @param reg The source register whose type should be set.
 * @param type The type to which the register should be set.
 */
void TydeInstruction::SetSourceTypeByRegisterIfUnknown(const int reg,
    const Type& type) {
  for (int i = 0; i < (int) sources_.size(); ++i)
    if (sources_[i].reg == reg && sources_[i].type.IsUnknown())
      sources_[i].type = type;
}

/**
 * Is the register a source register
 *
 * @param reg Register index.
 * @return True if the register is a source register.
 */
bool TydeInstruction::IsSource(const int reg) const {
  for (int i = 0; i < (int) sources_.size(); ++i)
    if (sources_[i].reg == reg && !sources_[i].type.IsLit())
      return true;
  return false;
}

/**
 * Find the first register index which is unknown.
 *
 * @return The first register index which is unknown.
 */
int TydeInstruction::FindUnknownSource() const {
  for (int i = 0; i < (int) sources_.size(); ++i)
    if (sources_[i].type.IsUnknown())
      return sources_[i].reg;
  return -1;
}

/**
 * Convert the current instruction to a descriptive string.
 *
 * @return A string describing this instruction.
 */
std::string TydeInstruction::ToString() const {
//  if (op_ == OP_NOP)
//    return "";

  std::stringstream result;
  result << std::hex << std::setw(4) << std::setfill('0') << original_offset_;
  result << ": " << dexGetOpcodeName(op_) << " ";
  if (has_destination())
    result << "(v" << destination().reg << ", "
        << destination().type.ToString() << ") ";
  for (int i = 0; i < (int) sources().size(); ++i)
    result << "(v" << sources()[i].reg << ", " << sources()[i].type.ToString()
        << ") ";
  if (!reachable_)
    result << "unreachable";
  result << "\n";

  return result.str();
}

/**
 * Print this instruction to stdout.
 *
 * @param indent Indent before instruction.
 */
void TydeInstruction::Print(const int indent) const {
  for (int i = 0; i < indent; ++i)
    std::cout << "  ";
  std::cout << ToString().c_str() << "\n";
}

/**
 * Get the type of a given source register.
 *
 * @param reg The register whose type we want.
 * @return The type of the given source register if found, an unknown type
 *         otherwise.
 */
Type TydeInstruction::GetRegisterType(const int reg) const {
  for (int i = 0; i < (int) sources_.size(); ++i)
    if (sources_[i].reg == reg)
      return sources_[i].type;
  return Type();
}


void TydeInstruction::set_destination(const int register_index,
    const VarType type, const int dim /*= 0*/) {
  has_destination_ = true;
  TypedValue new_register_and_type = {register_index, Type(type, dim)};
  destination_ = new_register_and_type;
}

void TydeInstruction::set_destination(const int register_index,
    const Type& type) {
  has_destination_ = true;
  TypedValue new_register_and_type = {register_index, type};
  destination_ = new_register_and_type;
}

void TydeInstruction::AddSource(const int register_index,
    const VarType type, const int dim /*= 0*/) {
  TypedValue new_register_and_type = {register_index, Type(type, dim)};
  sources_.push_back(new_register_and_type);
}

void TydeInstruction::AddSource(const int register_index,
    const Type& type) {
  TypedValue new_register_and_type = {register_index, type};
  sources_.push_back(new_register_and_type);
}

// Macro from the Android source code.
#define FETCH(_offset)      (position[(_offset)])


/**
 * Parse a fill-array-data pseudo-instruction.
 *
 * @param position The current position in dex bytecode.
 * @return The primitive type contained in the array.
 */
VarType TydeInstruction::ParseFillArrayData(const u2* position) {
  position += 1; // Move past the identifier
  u2 element_width = FETCH(0);
  position += 1;
  u4 size = (FETCH(0) | ((u4) FETCH(1) << 16));
  position += 2;
  FillArrayDataData* fad = new FillArrayDataData();
  data_ = fad;

  switch (element_width) {
    case 1: {
      const u1* pos = (const u1*) position;
      for (u4 i = 0; i < size; ++i) {
        fad->data.push_back((u8) *pos);
        pos += 1;
      }
      return kByte;
    }
    case 2: {
      for (u4 i = 0; i < size; ++i) {
        fad->data.push_back((u8) FETCH(0));
        position += 1;
      }
      return kACSUnknown;
    }
    case 4: {
      for (u4 i = 0; i < size; ++i) {
        fad->data.push_back((u8) (FETCH(0) | ((u4) FETCH(1) << 16)));
        position += 2;
      }
      return kAFIUnknown;
    }
    case 8: {
      for (u4 i = 0; i < size; ++i) {
        u8 value = (u8)FETCH(0);
        value |= (u8)FETCH(1) << 16;
        value |= (u8)FETCH(2) << 32;
        value |= (u8)FETCH(3) << 48;
        fad->data.push_back(value);
        position += 4;
      }
      return kADLUnknown;
    }
    default: {
      return kUnknown;
    }
  }
}

/**
 * Parse a packed-switch-data pseudo-instruction.
 *
 * @param position The current position in dex bytecode.
 */
void TydeInstruction::ParsePackedSwitch(const u2* position) {
  position += 1; // Move past the identifier
  u2 size = FETCH(0);
  position += 1;
  SwitchData* psd = new SwitchData();
  data_ = psd;
  psd->keys.push_back((s4) (FETCH(0) | ((u4) FETCH(1) << 16)));
  position += 2;

  for(u2 i = 0; i < size; i++) {
    psd->targets.push_back((s4) (FETCH(0) | ((u4) FETCH(1) << 16)));
    position += 2;
  }
}

/**
 * Parse a sparse-switch-data pseudo-instruction.
 *
 * @param position The current position in dex bytecode.
 */
void TydeInstruction::ParseSparseSwitch(const u2* position) {
  position += 1; // Move past the identifier
  u2 size = FETCH(0);
  position += 1;
  SwitchData* ssd = new SwitchData();
  data_ = ssd;

  for(u2 i = 0; i < size; i++) {
    ssd->keys.push_back((s4) (FETCH(0) | ((u4) FETCH(1) << 16)));
    position += 2;
  }
  for(u2 i = 0; i < size; i++) {
    ssd->targets.push_back((s4) (FETCH(0) | ((u4) FETCH(1) << 16)));
    position += 2;
  }
}

/**
 * Parse an instruction.
 *
 * Design note - Parsing instruction used to be done with two successive
 * switches: the first one was on the Dalvik instruction format. The second
 * one was on the opcode. There used to a class for each instruction format.
 * While this was quite neat and clean, it was harder to maintain than the big
 * switch below. It also lead to some code replication across instruction
 * formats. The change to the switch below improved performance and reduced the
 * executable size by 20%. Further, the Dalvik instruction format is irrelevant
 * for the rest of the retargeting process, so it does not really make much
 * sense to have separate types for each format.
 *
 * @param dec_ins The input Dalvik decoded instruction.
 * @param insn_idx The index of the instruction in the Dalvik bytecode array.
 * @param insns A pointer to the instruction in the bytecode array.
 * @param method A pointer to the method being parsed.
 * @param cp The constant pool of the class being generated.
 */
void TydeInstruction::ParseInstruction(const DecodedInstruction* dec_ins,
    int insn_idx, const u2* insns, MethodInfo* method, ConstantPool& cp) {
  VarType tmp_type;
  switch (op_) {
    case OP_NOP:
      break;
    case OP_MOVE:
    case OP_MOVE_16:
    case OP_MOVE_FROM16:
      tmp_type = kFIUnknown;
      goto move_common;
    case OP_MOVE_WIDE:
    case OP_MOVE_WIDE_16:
    case OP_MOVE_WIDE_FROM16:
      tmp_type = kDLUnknown;
move_common:
      {
        set_destination(dec_ins->vA, tmp_type);
        method->AddAmbiguousDestination(this, dec_ins->vA);
        AddSource(dec_ins->vB, tmp_type);
        method->AddAmbiguousSource(this, dec_ins->vB);
      }
      break;
    case OP_MOVE_OBJECT:
    case OP_MOVE_OBJECT_16:
    case OP_MOVE_OBJECT_FROM16:
      set_destination(dec_ins->vA, kObject);
      AddSource(dec_ins->vB, kObject);
      break;

    case OP_MOVE_RESULT:
    case OP_MOVE_RESULT_WIDE:
    case OP_MOVE_RESULT_OBJECT:
      if (method->code_attribute()->tyde_body().back()->op() ==
          OP_FILLED_NEW_ARRAY
          || method->code_attribute()->tyde_body().back()->op() ==
              OP_FILLED_NEW_ARRAY_RANGE
          || method->code_attribute()->tyde_body().back()->op() ==
              OP_FILLED_NEW_ARRAY_JUMBO)
        set_destination(dec_ins->vA, kObject);
      else if (method->code_attribute()->tyde_body().back()->op() ==
          OP_INVOKE_INTERFACE
          || method->code_attribute()->tyde_body().back()->op() ==
              OP_INVOKE_INTERFACE_RANGE
          || method->code_attribute()->tyde_body().back()->op() ==
              OP_INVOKE_INTERFACE_JUMBO)
        set_destination(dec_ins->vA,
            ((InterfaceMethodRefInfo *) method->code_attribute()->
                tyde_body().back()->reference())->return_type());
      else
        set_destination(dec_ins->vA,
            ((MethodRefInfo *) method->code_attribute()->tyde_body().back()->
                reference())->return_type());
      break;
    case OP_MOVE_EXCEPTION:
      set_destination(dec_ins->vA, kObject);
      break;

    case OP_RETURN_VOID:
      break;
    case OP_RETURN:
    case OP_RETURN_WIDE:
    case OP_RETURN_OBJECT:
      AddSource(dec_ins->vA, method->return_type());
      break;

    case OP_CONST:
    case OP_CONST_4:
    case OP_CONST_16:
    case OP_CONST_HIGH16:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vB;
      if (cd->constant == 0) {
//        method->AddAmbiguousTrio(this, dec_ins->vA, false);
        method->AddAmbiguousDestination(this, dec_ins->vA);
        set_destination(dec_ins->vA, kTrioUnknown);
      } else {
        method->AddAmbiguousDestination(this, dec_ins->vA);
        set_destination(dec_ins->vA, kFIUnknown);
      }
      break;
    }
    case OP_CONST_WIDE:
    {
      WideConstantData* wcd = new WideConstantData;
      data_ = wcd;
      wcd->constant = dec_ins->vB_wide;
      method->AddAmbiguousDestination(this, dec_ins->vA);
      set_destination(dec_ins->vA, kDLUnknown);
      break;
    }
    case OP_CONST_WIDE_16:
    case OP_CONST_WIDE_32:
    case OP_CONST_WIDE_HIGH16:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vB;
      method->AddAmbiguousDestination(this, dec_ins->vA);
      set_destination(dec_ins->vA, kDLUnknown);
      break;
    }

    case OP_CONST_STRING:
    case OP_CONST_STRING_JUMBO:
      set_destination(dec_ins->vA, kNAObject);
      reference_ = cp.AddStringCst(dec_ins->vB);
      break;
    case OP_CONST_CLASS:
    case OP_CONST_CLASS_JUMBO:
      set_destination(dec_ins->vA, kObject);
      reference_ = cp.AddClassCst(dec_ins->vB);
      break;

    case OP_MONITOR_ENTER:
    case OP_MONITOR_EXIT:
    case OP_THROW:
      AddSource(dec_ins->vA, kObject);
      break;

    case OP_CHECK_CAST:
    case OP_CHECK_CAST_JUMBO:
      reference_ = cp.AddClassCst(dec_ins->vB);
      AddSource(dec_ins->vA, kObject);
      set_destination(dec_ins->vA,
          Type::ParseType(((ClassInfo*) reference_)->this_name().bytes()));
      break;

    case OP_INSTANCE_OF:
    case OP_INSTANCE_OF_JUMBO:
      has_destination_ = true;
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kObject);
      reference_ = cp.AddClassCst(dec_ins->vC);
      break;

    case OP_ARRAY_LENGTH:
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kObject);
      break;

    case OP_NEW_INSTANCE:
    case OP_NEW_INSTANCE_JUMBO:
      set_destination(dec_ins->vA, kNAObject);
      reference_ = cp.AddClassCst(dec_ins->vB);
      break;
    case OP_NEW_ARRAY:
    case OP_NEW_ARRAY_JUMBO:
    {
      AddSource(dec_ins->vB, kInt);
      std::pair<ClassInfo *, int> array_type = cp.AddArray(dec_ins->vC);
      if (array_type.first == NULL) {
        set_destination(dec_ins->vA, (VarType) array_type.second, 1);
      } else {
        set_destination(dec_ins->vA, Type::ParseType(
            dexStringByTypeIdx(cp.cf()->dex_file(), dec_ins->vC)));
        reference_ = array_type.first;
      }
      break;
    }

    case OP_GOTO:
    case OP_GOTO_16:
    case OP_GOTO_32:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vA;
      break;
    }

    case OP_PACKED_SWITCH:
      AddSource(dec_ins->vA, kInt);
      ParsePackedSwitch(insns + dec_ins->vB);
      break;
    case OP_SPARSE_SWITCH:
      AddSource(dec_ins->vA, kInt);
      ParseSparseSwitch(insns + dec_ins->vB);
      break;
    case OP_FILL_ARRAY_DATA:
      AddSource(dec_ins->vA, ParseFillArrayData(insns + dec_ins->vB), 1);
      method->AddAmbiguousSource(this, dec_ins->vA);
      break;

    case OP_CMPL_FLOAT:
    case OP_CMPG_FLOAT:
      tmp_type = kFloat;
      goto cmp_common;
    case OP_CMPL_DOUBLE:
    case OP_CMPG_DOUBLE:
      tmp_type = kDouble;
      goto cmp_common;
    case OP_CMP_LONG:
      tmp_type = kLong;
cmp_common:
      {
        set_destination(dec_ins->vA, kInt);
        AddSource(dec_ins->vB, tmp_type);
        AddSource(dec_ins->vC, tmp_type);
      }
      break;

    case OP_IF_EQ:
    case OP_IF_NE:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vC;
      AddSource(dec_ins->vA, kTrioUnknown);
//      method->AddAmbiguousTrio(this, dec_ins->vA, true);
      method->AddAmbiguousSource(this, dec_ins->vA);
      AddSource(dec_ins->vB, kTrioUnknown);
//      method->AddAmbiguousTrio(this, dec_ins->vB, true);
      method->AddAmbiguousSource(this, dec_ins->vB);
      break;
    }
    case OP_IF_LT:
    case OP_IF_GE:
    case OP_IF_GT:
    case OP_IF_LE:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vC;
      AddSource(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kInt);
      break;
    }

    case OP_IF_EQZ:
    case OP_IF_NEZ:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vB;
      AddSource(dec_ins->vA, kTrioUnknown);
//      method->AddAmbiguousTrio(this, dec_ins->vA, true);
      method->AddAmbiguousSource(this, dec_ins->vA);
      break;
    }
    case OP_IF_LTZ:
    case OP_IF_GEZ:
    case OP_IF_GTZ:
    case OP_IF_LEZ:
    {
      ConstantData* cd = new ConstantData;
      data_ = cd;
      cd->constant = dec_ins->vB;
      AddSource(dec_ins->vA, kInt);
      break;
    }

    case OP_AGET:
      set_destination(dec_ins->vA, kFIUnknown);
      method->AddAmbiguousDestination(this, dec_ins->vA);
      AddSource(dec_ins->vB, kAFIUnknown);
      method->AddAmbiguousSource(this, dec_ins->vB);
      AddSource(dec_ins->vC, kInt);
      break;
    case OP_AGET_WIDE:
      set_destination(dec_ins->vA, kDLUnknown);
      method->AddAmbiguousDestination(this, dec_ins->vA);
      AddSource(dec_ins->vB, kADLUnknown);
      method->AddAmbiguousSource(this, dec_ins->vB);
      AddSource(dec_ins->vC, kInt);
      break;
    case OP_AGET_OBJECT:
      set_destination(dec_ins->vA, kObject);
      AddSource(dec_ins->vB, kAObjectUnknown, 1);
      AddSource(dec_ins->vC, kInt);
      break;
    case OP_AGET_BOOLEAN:
      tmp_type = kBoolean;
      goto aget_common;
    case OP_AGET_BYTE:
      tmp_type = kByte;
      goto aget_common;
    case OP_AGET_CHAR:
      tmp_type = kChar;
      goto aget_common;
    case OP_AGET_SHORT:
      tmp_type = kShort;
      goto aget_common;
aget_common:
      {
        set_destination(dec_ins->vA, tmp_type);
        AddSource(dec_ins->vB, tmp_type, 1);
        AddSource(dec_ins->vC, kInt);
      }
      break;
    case OP_APUT:
      AddSource(dec_ins->vB, kAFIUnknown);
      method->AddAmbiguousSource(this, dec_ins->vB);
      AddSource(dec_ins->vC, kInt);
      // Add it at the end to have the right order for translation
      AddSource(dec_ins->vA, kFIUnknown);
      method->AddAmbiguousSource(this, dec_ins->vA);
      break;
    case OP_APUT_WIDE:
      AddSource(dec_ins->vB, kADLUnknown);
      method->AddAmbiguousSource(this, dec_ins->vB);
      AddSource(dec_ins->vC, kInt);
      // Add it at the end to have the right order for translation
      AddSource(dec_ins->vA, kDLUnknown);
      method->AddAmbiguousSource(this, dec_ins->vA);
      break;
    case OP_APUT_OBJECT:
      AddSource(dec_ins->vB, kAObjectUnknown, 1);
      AddSource(dec_ins->vC, kInt);
      AddSource(dec_ins->vA, kObject);
      break;
    case OP_APUT_BOOLEAN:
      tmp_type = kBoolean;
      goto aput_common;
    case OP_APUT_BYTE:
      tmp_type = kByte;
      goto aput_common;
    case OP_APUT_CHAR:
      tmp_type = kChar;
      goto aput_common;
    case OP_APUT_SHORT:
      tmp_type = kShort;
      goto aput_common;
aput_common:
      {
        AddSource(dec_ins->vB, tmp_type, 1);
        AddSource(dec_ins->vC, kInt);
        AddSource(dec_ins->vA, tmp_type);
      }
      break;

    case OP_IGET:
    case OP_IGET_JUMBO:
    case OP_IGET_WIDE:
    case OP_IGET_WIDE_JUMBO:
    case OP_IGET_OBJECT:
    case OP_IGET_OBJECT_JUMBO:
      AddSource(dec_ins->vB, kObject);
      reference_ = cp.AddFieldCst(dec_ins->vC);
      set_destination(dec_ins->vA,
          Type::ParseType(((FieldRefInfo*) reference_)
          ->nameandtype().descriptor().bytes()));
      break;
    case OP_IGET_BOOLEAN:
    case OP_IGET_BOOLEAN_JUMBO:
      tmp_type = kBoolean;
      goto iget_common;
    case OP_IGET_BYTE:
    case OP_IGET_BYTE_JUMBO:
      tmp_type = kByte;
      goto iget_common;
    case OP_IGET_CHAR:
    case OP_IGET_CHAR_JUMBO:
      tmp_type = kChar;
      goto iget_common;
    case OP_IGET_SHORT:
    case OP_IGET_SHORT_JUMBO:
      tmp_type = kShort;
iget_common:
      {
        AddSource(dec_ins->vB, kObject);
        reference_ = cp.AddFieldCst(dec_ins->vC);
        set_destination(dec_ins->vA, tmp_type);
      }
      break;
    case OP_IPUT:
    case OP_IPUT_JUMBO:
    case OP_IPUT_WIDE:
    case OP_IPUT_WIDE_JUMBO:
    case OP_IPUT_OBJECT:
    case OP_IPUT_OBJECT_JUMBO:
      reference_ = cp.AddFieldCst(dec_ins->vC);
      AddSource(dec_ins->vB, kObject);
      AddSource(dec_ins->vA, Type::ParseType(((FieldRefInfo *) reference_)
          ->nameandtype().descriptor().bytes()));
      break;
    case OP_IPUT_BOOLEAN:
    case OP_IPUT_BOOLEAN_JUMBO:
      tmp_type = kBoolean;
      goto iput_common;
    case OP_IPUT_BYTE:
    case OP_IPUT_BYTE_JUMBO:
      tmp_type = kByte;
      goto iput_common;
    case OP_IPUT_CHAR:
    case OP_IPUT_CHAR_JUMBO:
      tmp_type = kChar;
      goto iput_common;
    case OP_IPUT_SHORT:
    case OP_IPUT_SHORT_JUMBO:
      tmp_type = kShort;
iput_common:
      {
        reference_ = cp.AddFieldCst(dec_ins->vC);
        AddSource(dec_ins->vB, kObject);
        AddSource(dec_ins->vA, tmp_type);
      }
      break;

    case OP_SGET:
    case OP_SGET_JUMBO:
    case OP_SGET_WIDE:
    case OP_SGET_WIDE_JUMBO:
    case OP_SGET_OBJECT:
    case OP_SGET_OBJECT_JUMBO:
      reference_ = cp.AddFieldCst(dec_ins->vB, true);
      set_destination(dec_ins->vA,
          Type::ParseType(((FieldRefInfo *) reference_)
              ->nameandtype().descriptor().bytes()));
      break;
    case OP_SGET_BOOLEAN:
    case OP_SGET_BOOLEAN_JUMBO:
      tmp_type = kBoolean;
      goto sget_common;
    case OP_SGET_BYTE:
    case OP_SGET_BYTE_JUMBO:
      tmp_type = kByte;
      goto sget_common;
    case OP_SGET_CHAR:
    case OP_SGET_CHAR_JUMBO:
      tmp_type = kChar;
      goto sget_common;
    case OP_SGET_SHORT:
    case OP_SGET_SHORT_JUMBO:
      tmp_type = kShort;
sget_common:
      {
        reference_ = cp.AddFieldCst(dec_ins->vB, true);
        set_destination(dec_ins->vA, tmp_type);
      }
      break;
    case OP_SPUT:
    case OP_SPUT_JUMBO:
    case OP_SPUT_WIDE:
    case OP_SPUT_WIDE_JUMBO:
    case OP_SPUT_OBJECT:
    case OP_SPUT_OBJECT_JUMBO:
      reference_ = cp.AddFieldCst(dec_ins->vB, true);
      AddSource(dec_ins->vA,
          Type::ParseType(((FieldRefInfo *) reference_)
          ->nameandtype().descriptor().bytes()));
      break;
    case OP_SPUT_BOOLEAN:
    case OP_SPUT_BOOLEAN_JUMBO:
      tmp_type = kBoolean;
      goto sput_common;
    case OP_SPUT_BYTE:
    case OP_SPUT_BYTE_JUMBO:
      tmp_type = kByte;
      goto sput_common;
    case OP_SPUT_CHAR:
    case OP_SPUT_CHAR_JUMBO:
      tmp_type = kChar;
      goto sput_common;
    case OP_SPUT_SHORT:
    case OP_SPUT_SHORT_JUMBO:
      tmp_type = kShort;
sput_common:
      {
        reference_ = cp.AddFieldCst(dec_ins->vB, true);
        AddSource(dec_ins->vA, tmp_type);
      }
      break;

    case OP_INVOKE_INTERFACE:
    case OP_INVOKE_INTERFACE_RANGE:
    case OP_INVOKE_INTERFACE_JUMBO:
    {
      InterfaceMethodRefInfo* method = cp.AddInterfaceMethodCst(dec_ins->vB);
      reference_ = method;
      if (op_ == OP_INVOKE_INTERFACE) {
        AddSource(dec_ins->arg[0], kNAObject);
        for (u4 i = 0; i < method->arguments().size(); ++i)
          AddSource(dec_ins->arg[method->RegisterIndex(i) + 1],
              method->arguments()[i]);
      } else {
        AddSource(dec_ins->vC, kNAObject);
        for (u4 i = 0; i < method->arguments().size(); ++i)
          AddSource(dec_ins->vC + method->RegisterIndex(i) + 1,
              method->arguments()[i]);
      }
      if (!method->return_type().IsVoid()) {
        DecodedInstruction next;
        if (op_ == OP_INVOKE_INTERFACE_JUMBO)
          dexDecodeInstruction(insns + 5, &next);
        else
          dexDecodeInstruction(insns + 3, &next);
        Type destination_type = PopReturnValue(next, method->return_type());
        if (!destination_type.IsVoid()) {
          set_destination(0, destination_type);
          has_destination_ = false;
        }
      }
      break;
    }
    case OP_INVOKE_VIRTUAL:
    case OP_INVOKE_VIRTUAL_RANGE:
    case OP_INVOKE_VIRTUAL_JUMBO:
    case OP_INVOKE_SUPER:
    case OP_INVOKE_SUPER_RANGE:
    case OP_INVOKE_SUPER_JUMBO:
    case OP_INVOKE_DIRECT:
    case OP_INVOKE_DIRECT_RANGE:
    case OP_INVOKE_DIRECT_JUMBO:
    {
      MethodRefInfo* method = cp.AddMethodCst(dec_ins->vB);
      reference_ = method;
      if (op_ == OP_INVOKE_VIRTUAL
          || op_ == OP_INVOKE_SUPER
          || op_ == OP_INVOKE_DIRECT) {
        AddSource(dec_ins->arg[0], kNAObject);
        for (u4 i = 0; i < method->arguments().size(); ++i)
          AddSource(dec_ins->arg[method->RegisterIndex(i) + 1],
              method->arguments()[i]);
      } else {
        AddSource(dec_ins->vC, kNAObject);
        for (u4 i = 0; i < method->arguments().size(); ++i)
          AddSource(dec_ins->vC + method->RegisterIndex(i) + 1,
              method->arguments()[i]);
      }
      if (!method->return_type().IsVoid()) {
        DecodedInstruction next;
        if (op_ == OP_INVOKE_VIRTUAL_JUMBO
          || op_ == OP_INVOKE_SUPER_JUMBO
          || op_ == OP_INVOKE_DIRECT_JUMBO)
          dexDecodeInstruction(insns + 5, &next);
        else
          dexDecodeInstruction(insns + 3, &next);
        Type destination_type = PopReturnValue(next, method->return_type());
        if (!destination_type.IsVoid()) {
          set_destination(0, destination_type);
          has_destination_ = false;
        }
      }
      break;
    }
    case OP_INVOKE_STATIC:
    case OP_INVOKE_STATIC_RANGE:
    case OP_INVOKE_STATIC_JUMBO:
    {
      MethodRefInfo* method = cp.AddMethodCst(dec_ins->vB, true);
      reference_ = method;
      if (op_ == OP_INVOKE_STATIC) {
        for (u4 i = 0; i < method->arguments().size(); ++i)
          AddSource(dec_ins->arg[method->RegisterIndex(i)],
              method->arguments()[i]);
      } else {
        for (u4 i = 0; i < method->arguments().size(); i++)
          AddSource(dec_ins->vC + method->RegisterIndex(i),
              method->arguments()[i]);
      }
      if (!method->return_type().IsVoid()) {
        DecodedInstruction next;
        if (op_ == OP_INVOKE_STATIC_JUMBO)
          dexDecodeInstruction(insns + 5, &next);
        else
          dexDecodeInstruction(insns + 3, &next);
        Type destination_type = PopReturnValue(next, method->return_type());
        if (!destination_type.IsVoid()) {
          set_destination(0, destination_type);
          has_destination_ = false;
        }
      }
      break;
    }
    case OP_FILLED_NEW_ARRAY:
    case OP_FILLED_NEW_ARRAY_RANGE:
    case OP_FILLED_NEW_ARRAY_JUMBO:
    {
      std::pair<ClassInfo*, int> array_type = cp.AddArray(dec_ins->vB);
      if (array_type.first != NULL)
        reference_ = array_type.first;
      FilledNewArrayData* fnad = new FilledNewArrayData;
      data_ = fnad;

      VarType type = kUnknown;
      if (array_type.second != 0) {
        type = (VarType) array_type.second;
        fnad->array_type.setType((VarType) array_type.second);
      } else {
        type = kObject;
        fnad->array_type = kObject;
      }
      if (op_ == OP_FILLED_NEW_ARRAY) {
        for (u4 i = 0; i < dec_ins->vA; ++i)
          AddSource(dec_ins->arg[i], type);
      } else {
        for (u4 i = 0; i < dec_ins->vA; i++)
          AddSource(dec_ins->vC + i, type);
      }
      break;
    }

    case OP_NEG_INT:
    case OP_NOT_INT:
      tmp_type = kInt;
      goto unary_common;
    case OP_NEG_LONG:
    case OP_NOT_LONG:
      tmp_type = kLong;
      goto unary_common;
    case OP_NEG_FLOAT:
      tmp_type = kFloat;
      goto unary_common;
    case OP_NEG_DOUBLE:
      tmp_type = kDouble;
unary_common:
      {
        set_destination(dec_ins->vA, tmp_type);
        AddSource(dec_ins->vB, tmp_type);
      }
      break;
    case OP_INT_TO_LONG:
      set_destination(dec_ins->vA, kLong);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_INT_TO_FLOAT:
      set_destination(dec_ins->vA, kFloat);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_INT_TO_DOUBLE:
      set_destination(dec_ins->vA, kDouble);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_LONG_TO_INT:
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kLong);
      break;
    case OP_LONG_TO_FLOAT:
      set_destination(dec_ins->vA, kFloat);
      AddSource(dec_ins->vB, kLong);
      break;
    case OP_LONG_TO_DOUBLE:
      set_destination(dec_ins->vA, kDouble);
      AddSource(dec_ins->vB, kLong);
      break;
    case OP_FLOAT_TO_INT:
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kFloat);
      break;
    case OP_FLOAT_TO_LONG:
      set_destination(dec_ins->vA, kLong);
      AddSource(dec_ins->vB, kFloat);
      break;
    case OP_FLOAT_TO_DOUBLE:
      set_destination(dec_ins->vA, kDouble);
      AddSource(dec_ins->vB, kFloat);
      break;
    case OP_DOUBLE_TO_INT:
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kDouble);
      break;
    case OP_DOUBLE_TO_LONG:
      set_destination(dec_ins->vA, kLong);
      AddSource(dec_ins->vB, kDouble);
      break;
    case OP_DOUBLE_TO_FLOAT:
      set_destination(dec_ins->vA, kFloat);
      AddSource(dec_ins->vB, kDouble);
      break;
    case OP_INT_TO_BYTE:
      set_destination(dec_ins->vA, kByte);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_INT_TO_CHAR:
      set_destination(dec_ins->vA, kChar);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_INT_TO_SHORT:
      set_destination(dec_ins->vA, kShort);
      AddSource(dec_ins->vB, kInt);
      break;

    case OP_ADD_INT:
    case OP_SUB_INT:
    case OP_MUL_INT:
    case OP_REM_INT:
    case OP_AND_INT:
    case OP_OR_INT:
    case OP_XOR_INT:
    case OP_DIV_INT:
    case OP_SHL_INT:
    case OP_SHR_INT:
    case OP_USHR_INT:
      tmp_type = kInt;
      goto binary_common;
    case OP_ADD_LONG:
    case OP_SUB_LONG:
    case OP_MUL_LONG:
    case OP_DIV_LONG:
    case OP_REM_LONG:
    case OP_AND_LONG:
    case OP_OR_LONG:
    case OP_XOR_LONG:
      tmp_type = kLong;
      goto binary_common;
    case OP_SHL_LONG:
    case OP_SHR_LONG:
    case OP_USHR_LONG:
      set_destination(dec_ins->vA, kLong);
      AddSource(dec_ins->vB, kLong);
      AddSource(dec_ins->vC, kInt);
      break;
    case OP_ADD_FLOAT:
    case OP_SUB_FLOAT:
    case OP_MUL_FLOAT:
    case OP_DIV_FLOAT:
    case OP_REM_FLOAT:
      tmp_type = kFloat;
      goto binary_common;
    case OP_ADD_DOUBLE:
    case OP_SUB_DOUBLE:
    case OP_MUL_DOUBLE:
    case OP_DIV_DOUBLE:
    case OP_REM_DOUBLE:
      tmp_type = kDouble;
binary_common:
    {
      set_destination(dec_ins->vA, tmp_type);
      AddSource(dec_ins->vB, tmp_type);
      AddSource(dec_ins->vC, tmp_type);
    }
    break;
    case OP_ADD_INT_2ADDR:
    case OP_SUB_INT_2ADDR:
    case OP_MUL_INT_2ADDR:
    case OP_DIV_INT_2ADDR:
    case OP_REM_INT_2ADDR:
    case OP_AND_INT_2ADDR:
    case OP_OR_INT_2ADDR:
    case OP_XOR_INT_2ADDR:
    case OP_SHL_INT_2ADDR:
    case OP_SHR_INT_2ADDR:
    case OP_USHR_INT_2ADDR:
      tmp_type = kInt;
      goto binary_2addr_common;
    case OP_ADD_LONG_2ADDR:
    case OP_SUB_LONG_2ADDR:
    case OP_MUL_LONG_2ADDR:
    case OP_DIV_LONG_2ADDR:
    case OP_REM_LONG_2ADDR:
    case OP_AND_LONG_2ADDR:
    case OP_OR_LONG_2ADDR:
    case OP_XOR_LONG_2ADDR:
      tmp_type = kLong;
      goto binary_2addr_common;
    case OP_SHL_LONG_2ADDR:
    case OP_SHR_LONG_2ADDR:
    case OP_USHR_LONG_2ADDR:
      set_destination(dec_ins->vA, kLong);
      AddSource(dec_ins->vA, kLong);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_ADD_FLOAT_2ADDR:
    case OP_SUB_FLOAT_2ADDR:
    case OP_MUL_FLOAT_2ADDR:
    case OP_DIV_FLOAT_2ADDR:
    case OP_REM_FLOAT_2ADDR:
      tmp_type = kFloat;
      goto binary_2addr_common;
    case OP_ADD_DOUBLE_2ADDR:
    case OP_SUB_DOUBLE_2ADDR:
    case OP_MUL_DOUBLE_2ADDR:
    case OP_DIV_DOUBLE_2ADDR:
    case OP_REM_DOUBLE_2ADDR:
      tmp_type = kDouble;
binary_2addr_common:
      {
        set_destination(dec_ins->vA, tmp_type);
        AddSource(dec_ins->vA, tmp_type);
        AddSource(dec_ins->vB, tmp_type);
      }
      break;

    case OP_RSUB_INT:
    case OP_RSUB_INT_LIT8:
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vC, kLit);
      AddSource(dec_ins->vB, kInt);
      break;
    case OP_ADD_INT_LIT16:
    case OP_MUL_INT_LIT16:
    case OP_DIV_INT_LIT16:
    case OP_REM_INT_LIT16:
    case OP_AND_INT_LIT16:
    case OP_OR_INT_LIT16:
    case OP_XOR_INT_LIT16:
    case OP_ADD_INT_LIT8:
    case OP_MUL_INT_LIT8:
    case OP_DIV_INT_LIT8:
    case OP_REM_INT_LIT8:
    case OP_SHL_INT_LIT8:
    case OP_SHR_INT_LIT8:
    case OP_USHR_INT_LIT8:
    case OP_AND_INT_LIT8:
    case OP_OR_INT_LIT8:
    case OP_XOR_INT_LIT8:
      set_destination(dec_ins->vA, kInt);
      AddSource(dec_ins->vB, kInt);
      AddSource(dec_ins->vC, kLit);
      break;

    case OP_THROW_VERIFICATION_ERROR:
    case OP_THROW_VERIFICATION_ERROR_JUMBO:
    case OP_EXECUTE_INLINE:
    case OP_EXECUTE_INLINE_RANGE:
    case OP_IGET_QUICK:
    case OP_IGET_WIDE_QUICK:
    case OP_IGET_OBJECT_QUICK:
    case OP_IPUT_QUICK:
    case OP_IPUT_WIDE_QUICK:
    case OP_IPUT_OBJECT_QUICK:
    case OP_INVOKE_VIRTUAL_QUICK:
    case OP_INVOKE_VIRTUAL_QUICK_RANGE:
    case OP_INVOKE_SUPER_QUICK:
    case OP_INVOKE_SUPER_QUICK_RANGE:
    case OP_INVOKE_OBJECT_INIT_RANGE:
    case OP_INVOKE_OBJECT_INIT_JUMBO:
    case OP_RETURN_VOID_BARRIER:
    case OP_IGET_VOLATILE:
    case OP_IGET_VOLATILE_JUMBO:
    case OP_IGET_WIDE_VOLATILE:
    case OP_IGET_WIDE_VOLATILE_JUMBO:
    case OP_IGET_OBJECT_VOLATILE:
    case OP_IGET_OBJECT_VOLATILE_JUMBO:
    case OP_IPUT_VOLATILE:
    case OP_IPUT_VOLATILE_JUMBO:
    case OP_IPUT_WIDE_VOLATILE:
    case OP_IPUT_WIDE_VOLATILE_JUMBO:
    case OP_IPUT_OBJECT_VOLATILE:
    case OP_IPUT_OBJECT_VOLATILE_JUMBO:
    case OP_SGET_VOLATILE:
    case OP_SGET_VOLATILE_JUMBO:
    case OP_SGET_WIDE_VOLATILE:
    case OP_SGET_WIDE_VOLATILE_JUMBO:
    case OP_SGET_OBJECT_VOLATILE:
    case OP_SGET_OBJECT_VOLATILE_JUMBO:
    case OP_SPUT_VOLATILE:
    case OP_SPUT_VOLATILE_JUMBO:
    case OP_SPUT_WIDE_VOLATILE:
    case OP_SPUT_WIDE_VOLATILE_JUMBO:
    case OP_SPUT_OBJECT_VOLATILE:
    case OP_SPUT_OBJECT_VOLATILE_JUMBO:
    case OP_UNUSED_3E:
    case OP_UNUSED_3F:
    case OP_UNUSED_40:
    case OP_UNUSED_41:
    case OP_UNUSED_42:
    case OP_UNUSED_43:
    case OP_UNUSED_73:
    case OP_UNUSED_79:
    case OP_UNUSED_7A:
    case OP_BREAKPOINT:
    case OP_DISPATCH_FF:
    case OP_UNUSED_27FF:
    case OP_UNUSED_28FF:
    case OP_UNUSED_29FF:
    case OP_UNUSED_2AFF:
    case OP_UNUSED_2BFF:
    case OP_UNUSED_2CFF:
    case OP_UNUSED_2DFF:
    case OP_UNUSED_2EFF:
    case OP_UNUSED_2FFF:
    case OP_UNUSED_30FF:
    case OP_UNUSED_31FF:
    case OP_UNUSED_32FF:
    case OP_UNUSED_33FF:
    case OP_UNUSED_34FF:
    case OP_UNUSED_35FF:
    case OP_UNUSED_36FF:
    case OP_UNUSED_37FF:
    case OP_UNUSED_38FF:
    case OP_UNUSED_39FF:
    case OP_UNUSED_3AFF:
    case OP_UNUSED_3BFF:
    case OP_UNUSED_3CFF:
    case OP_UNUSED_3DFF:
    case OP_UNUSED_3EFF:
    case OP_UNUSED_3FFF:
    case OP_UNUSED_40FF:
    case OP_UNUSED_41FF:
    case OP_UNUSED_42FF:
    case OP_UNUSED_43FF:
    case OP_UNUSED_44FF:
    case OP_UNUSED_45FF:
    case OP_UNUSED_46FF:
    case OP_UNUSED_47FF:
    case OP_UNUSED_48FF:
    case OP_UNUSED_49FF:
    case OP_UNUSED_4AFF:
    case OP_UNUSED_4BFF:
    case OP_UNUSED_4CFF:
    case OP_UNUSED_4DFF:
    case OP_UNUSED_4EFF:
    case OP_UNUSED_4FFF:
    case OP_UNUSED_50FF:
    case OP_UNUSED_51FF:
    case OP_UNUSED_52FF:
    case OP_UNUSED_53FF:
    case OP_UNUSED_54FF:
    case OP_UNUSED_55FF:
    case OP_UNUSED_56FF:
    case OP_UNUSED_57FF:
    case OP_UNUSED_58FF:
    case OP_UNUSED_59FF:
    case OP_UNUSED_5AFF:
    case OP_UNUSED_5BFF:
    case OP_UNUSED_5CFF:
    case OP_UNUSED_5DFF:
    case OP_UNUSED_5EFF:
    case OP_UNUSED_5FFF:
    case OP_UNUSED_60FF:
    case OP_UNUSED_61FF:
    case OP_UNUSED_62FF:
    case OP_UNUSED_63FF:
    case OP_UNUSED_64FF:
    case OP_UNUSED_65FF:
    case OP_UNUSED_66FF:
    case OP_UNUSED_67FF:
    case OP_UNUSED_68FF:
    case OP_UNUSED_69FF:
    case OP_UNUSED_6AFF:
    case OP_UNUSED_6BFF:
    case OP_UNUSED_6CFF:
    case OP_UNUSED_6DFF:
    case OP_UNUSED_6EFF:
    case OP_UNUSED_6FFF:
    case OP_UNUSED_70FF:
    case OP_UNUSED_71FF:
    case OP_UNUSED_72FF:
    case OP_UNUSED_73FF:
    case OP_UNUSED_74FF:
    case OP_UNUSED_75FF:
    case OP_UNUSED_76FF:
    case OP_UNUSED_77FF:
    case OP_UNUSED_78FF:
    case OP_UNUSED_79FF:
    case OP_UNUSED_7AFF:
    case OP_UNUSED_7BFF:
    case OP_UNUSED_7CFF:
    case OP_UNUSED_7DFF:
    case OP_UNUSED_7EFF:
    case OP_UNUSED_7FFF:
    case OP_UNUSED_80FF:
    case OP_UNUSED_81FF:
    case OP_UNUSED_82FF:
    case OP_UNUSED_83FF:
    case OP_UNUSED_84FF:
    case OP_UNUSED_85FF:
    case OP_UNUSED_86FF:
    case OP_UNUSED_87FF:
    case OP_UNUSED_88FF:
    case OP_UNUSED_89FF:
    case OP_UNUSED_8AFF:
    case OP_UNUSED_8BFF:
    case OP_UNUSED_8CFF:
    case OP_UNUSED_8DFF:
    case OP_UNUSED_8EFF:
    case OP_UNUSED_8FFF:
    case OP_UNUSED_90FF:
    case OP_UNUSED_91FF:
    case OP_UNUSED_92FF:
    case OP_UNUSED_93FF:
    case OP_UNUSED_94FF:
    case OP_UNUSED_95FF:
    case OP_UNUSED_96FF:
    case OP_UNUSED_97FF:
    case OP_UNUSED_98FF:
    case OP_UNUSED_99FF:
    case OP_UNUSED_9AFF:
    case OP_UNUSED_9BFF:
    case OP_UNUSED_9CFF:
    case OP_UNUSED_9DFF:
    case OP_UNUSED_9EFF:
    case OP_UNUSED_9FFF:
    case OP_UNUSED_A0FF:
    case OP_UNUSED_A1FF:
    case OP_UNUSED_A2FF:
    case OP_UNUSED_A3FF:
    case OP_UNUSED_A4FF:
    case OP_UNUSED_A5FF:
    case OP_UNUSED_A6FF:
    case OP_UNUSED_A7FF:
    case OP_UNUSED_A8FF:
    case OP_UNUSED_A9FF:
    case OP_UNUSED_AAFF:
    case OP_UNUSED_ABFF:
    case OP_UNUSED_ACFF:
    case OP_UNUSED_ADFF:
    case OP_UNUSED_AEFF:
    case OP_UNUSED_AFFF:
    case OP_UNUSED_B0FF:
    case OP_UNUSED_B1FF:
    case OP_UNUSED_B2FF:
    case OP_UNUSED_B3FF:
    case OP_UNUSED_B4FF:
    case OP_UNUSED_B5FF:
    case OP_UNUSED_B6FF:
    case OP_UNUSED_B7FF:
    case OP_UNUSED_B8FF:
    case OP_UNUSED_B9FF:
    case OP_UNUSED_BAFF:
    case OP_UNUSED_BBFF:
    case OP_UNUSED_BCFF:
    case OP_UNUSED_BDFF:
    case OP_UNUSED_BEFF:
    case OP_UNUSED_BFFF:
    case OP_UNUSED_C0FF:
    case OP_UNUSED_C1FF:
    case OP_UNUSED_C2FF:
    case OP_UNUSED_C3FF:
    case OP_UNUSED_C4FF:
    case OP_UNUSED_C5FF:
    case OP_UNUSED_C6FF:
    case OP_UNUSED_C7FF:
    case OP_UNUSED_C8FF:
    case OP_UNUSED_C9FF:
    case OP_UNUSED_CAFF:
    case OP_UNUSED_CBFF:
    case OP_UNUSED_CCFF:
    case OP_UNUSED_CDFF:
    case OP_UNUSED_CEFF:
    case OP_UNUSED_CFFF:
    case OP_UNUSED_D0FF:
    case OP_UNUSED_D1FF:
    case OP_UNUSED_D2FF:
    case OP_UNUSED_D3FF:
    case OP_UNUSED_D4FF:
    case OP_UNUSED_D5FF:
    case OP_UNUSED_D6FF:
    case OP_UNUSED_D7FF:
    case OP_UNUSED_D8FF:
    case OP_UNUSED_D9FF:
    case OP_UNUSED_DAFF:
    case OP_UNUSED_DBFF:
    case OP_UNUSED_DCFF:
    case OP_UNUSED_DDFF:
    case OP_UNUSED_DEFF:
    case OP_UNUSED_DFFF:
    case OP_UNUSED_E0FF:
    case OP_UNUSED_E1FF:
    case OP_UNUSED_E2FF:
    case OP_UNUSED_E3FF:
    case OP_UNUSED_E4FF:
    case OP_UNUSED_E5FF:
    case OP_UNUSED_E6FF:
    case OP_UNUSED_E7FF:
    case OP_UNUSED_E8FF:
    case OP_UNUSED_E9FF:
    case OP_UNUSED_EAFF:
    case OP_UNUSED_EBFF:
    case OP_UNUSED_ECFF:
    case OP_UNUSED_EDFF:
    case OP_UNUSED_EEFF:
    case OP_UNUSED_EFFF:
    case OP_UNUSED_F0FF:
    case OP_UNUSED_F1FF:
      break;
  }
}

/**
 * Determine if a return value should be popped from the stack after a method
 * invocation.
 *
 * @param next The next instruction.
 * @param return_type The invoked method's return type.
 * @return A void type if no value is returned, a pop type if a value is
 *         returned.
 */
Type TydeInstruction::PopReturnValue(
    const DecodedInstruction& next,
    const Type& return_type) {
  if (next.opcode != OP_MOVE_RESULT && next.opcode != OP_MOVE_RESULT_WIDE
      && next.opcode != OP_MOVE_RESULT_OBJECT) {
    if (return_type.IsVoid()) {
      return Type(kVoid);
    } else if (return_type.Width() == 1) {
      return Type(kPop);
    } else {
      return Type(kPop2);
    }
  } else {
    return Type(kVoid);
  }
}

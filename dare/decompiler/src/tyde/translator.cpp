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
 * translator.cpp
 *
 * Tyde to Jasmin translator.
 */

#include "tyde/translator.h"

#include <ostream>
#include <string>
#include <vector>

#include "libdex/InstrUtils.h"

#include "class_file/class_file.h"
#include "class_file/code_attribute.h"
#include "class_file/float_info.h"
#include "class_file/integer_info.h"
#include "class_file/interface_method_ref_info.h"
#include "class_file/method_ref_info.h"
#include "timer.h"
#include "tyde/java_opcode_jasmin.h"
#include "tyde/tyde_instruction.h"

using std::string;
using std::vector;


Translator::Translator(CodeAttribute* code_attribute, std::ostream& out)
    : code_attribute_(code_attribute),
      out_(out),
      registers_(vector<int>(code_attribute->dex_code()->registersSize, 0)) {
  int nb_registers = code_attribute->dex_code()->registersSize;
  int incoming = code_attribute->dex_code()->insSize;

  for (int i = 0; i < incoming; ++i)
    registers_[nb_registers - incoming + i] = i;
  for (int i = incoming; i < nb_registers; ++i)
    registers_[i - incoming] = i;
}

void Translator::Convert() {
//  string method_name = string(code_attribute_->method()->class_descriptor())
//      + code_attribute_->method()->name().bytes()
//      + code_attribute_->method()->descriptor().bytes();

//  LOGD("Converting %s\n", method_name.c_str());
//  code_attribute_->tyde_body_.Dump(std::cout);

  out_ << ".limit locals " << registers_.size() << "\n";
  out_ << ".limit stack 200\n";

  for (u4 i = 0; i < code_attribute_->tyde_body().tries().size(); ++i) {
    for(u4 j = 0; j < code_attribute_->tyde_body().tries()[i].handlers.handlers.size(); ++j) {
      out_ << "  .catch ";
      code_attribute_->tyde_body().tries()[i].handlers.handlers[j].type_ptr->WriteToJasmin(out_);
      out_ << " <from> Label"
          << code_attribute_->tyde_body().tries()[i].start_ptr->label()
          << " <to> Label"
          << code_attribute_->tyde_body().tries()[i].end_ptr->label()
          << " <using> Label"
          << code_attribute_->tyde_body().tries()[i].handlers.handlers[j].ptr->label()
          << "\n";
    }

    if(code_attribute_->tyde_body().tries()[i].handlers.catch_all_ptr != NULL) {
      out_ << "  .catch all <from> Label"
          << code_attribute_->tyde_body().tries()[i].start_ptr->label()
          << " <to> Label"
          << code_attribute_->tyde_body().tries()[i].end_ptr->label()
          << " <using> Label"
          << code_attribute_->tyde_body().tries()[i].handlers.catch_all_ptr->label()
          << "\n";
    }
  }

  for (int i = 0; i < (int) code_attribute_->tyde_body().size(); ++i) {
    if (code_attribute_->tyde_body()[i]->label() != -1)
      out_ << "Label" << code_attribute_->tyde_body()[i]->label() << ":" << "\n";

    TydeInstruction* instruction = code_attribute_->tyde_body()[i];
//      std::cout << instruction->ToString();
    switch (TydeInstrUtils::TydeGetFormatFromOpcode(instruction->op())) {
      case kFmtTuo:
        TranslateFmtTuo(instruction);
        break;
      case kFmtTao:
        TranslateFmtTao(instruction);
        break;
      case kFmtTub:
        TranslateFmtTub(instruction);
        break;
      case kFmtTab:
        TranslateFmtTab(instruction);
        break;
      case kFmtTfna:
        TranslateFmtTfna(instruction);
        break;
      case kFmtTno:
        TranslateFmtTno(instruction);
        break;
      case kFmtTfad:
        TranslateFmtTfad(instruction);
        break;
      case kFmtTss:
        TranslateFmtTss(instruction);
        break;
      case kFmtTps:
        TranslateFmtTps(instruction);
        break;
      case kFmtTtve:
        TranslateFmtTtve(instruction);
        break;
      case kFmtTunk:
        LOGW("Unknown instruction format\n");
        break;
    }
  }
}

/**
 * Translate unambiguous operator instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTuo(const TydeInstruction* ins) {
  TranslateSources(ins);
  if (AddOpcode(ins->op())) {
    AddReference(ins);
    if (ins->op() == OP_INVOKE_INTERFACE
        || ins->op() == OP_INVOKE_INTERFACE_RANGE
        || ins->op() == OP_INVOKE_INTERFACE_JUMBO) {
      InterfaceMethodRefInfo* method =
          (InterfaceMethodRefInfo*) ins->reference();
        int count = 1;
        for (int i = 0; i < (int) method->arguments().size(); ++i)
          count += method->arguments()[i].Width();
        out_ << " " << count;
    }
    out_ << "\n";
  }
  TranslateDestination(ins);
}

/**
 * Translate ambiguous operator instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTao(const TydeInstruction* ins) {
  TranslateSources(ins);
  AddTaoOpcodeAndRef(ins);
  out_ << "\n";
  TranslateDestination(ins);
}

/**
 * Translate unambiguous branching instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTub(const TydeInstruction* ins) {
  TranslateSources(ins);
  AddOpcode(ins->op());
  Opcode opcode = ins->op();
  int label = -1;
  if (opcode == OP_GOTO || opcode == OP_GOTO_16 || opcode == OP_GOTO_32)
    label = ins->successors()[0]->label();
  else
    label = ins->successors()[1]->label();
  if (label == -1) {
    LOGE("Error: label does not exist\n");
    exit(1);
  }
  out_ << " Label" << label << "\n";
}

/**
 * Translate ambiguous branching instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTab(const TydeInstruction* ins) {
  TranslateSources(ins);
  AddTabOpcode(ins);
  int label = ins->successors()[1]->label();
  if (label == -1) {
    LOGE("Label does not exist\n");
    exit(1);
  }
  out_ << " Label" << label << "\n";
}

/**
 * Translate filled-new-array instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTfna(const TydeInstruction* ins) {
  int source_count = ins->sources().size();
  AddIconst(source_count);
  out_ << "\n";
  JavaOpCode op;
  Type array_type = ins->array_type();

  if (array_type.IsObject()) {
    out_ << ANEWARRAY;
    AddReference(ins);
    op = AASTORE;
  } else {
    out_ << NEWARRAY << " " << array_type.ToJavaArrayType();
    if (array_type.IsBoolean() || array_type.IsByte()) {
      op = BASTORE;
    } else if (array_type.IsChar()) {
      op = CASTORE;
    } else if (array_type.IsFloat()) {
      op = FASTORE;
    } else if (array_type.IsShort()) {
      op = SASTORE;
    } else if (array_type.IsInt()) {
      op = IASTORE;
    } else {
      LOGW("Error while translating fna with type: %s\n",
          array_type.ToString().c_str());
      op = IASTORE;
    }
  }
  out_ << "\n";

  Type source_type = (source_count > 0) ?
      ins->sources()[0].type : Type(kUnknown);

  for (int i = 0; i < (int) source_count; ++i) {
    out_ << DUP << "\n";
    AddIconst(i);
    out_ << "\n";
    if (source_type.IsFloat())
      AddFload(registers_[ins->sources()[i].reg]);
    else if (source_type.IsIntSubtype())
      AddIload(registers_[ins->sources()[i].reg]);
    else
      AddAload(registers_[ins->sources()[i].reg]);
    out_ << op << "\n";
  }
}

/**
 * Translate not instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTno(const TydeInstruction* ins) {
  TranslateSources(ins);
  bool wide = ins->sources()[0].type.Width() == 2;
  out_ << (wide ? LDC2_W : LDC) << " -1\n";
  out_ << (wide ? LXOR : IXOR) << "\n";
  TranslateDestination(ins);
}

/**
 * Translate fill-array-data instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTfad(const TydeInstruction* ins) {
  JavaOpCode op;
  Type type = ins->sources()[0].type;

  if (type.IsBooleanArray() || type.IsByteArray()) {
    op = BASTORE;
  } else if (type.IsCharArray()) {
    op = CASTORE;
  } else if (type.IsShortArray()) {
    op = SASTORE;
  } else if (type.IsIntArray()) {
    op = IASTORE;
  } else if (type.IsFloatArray()) {
    op = FASTORE;
  } else if (type.IsLongArray()) {
    op = LASTORE;
  } else if (type.IsDoubleArray()) {
    op = DASTORE;
  } else {
    LOGE("Error while translating format fad: type %s\n",
        ins->sources()[0].type.ToString().c_str());
    exit(1);
  }

  string ldc = (type.Width() == 2) ? LDC2_W : LDC;

  for (u4 i = 0; i < ins->data_ptr().size(); ++i) {
    TranslateSources(ins);
    AddIconst(i);
    out_ << "\n";

    if (op == FASTORE) {
      FloatInfo* float_info = (FloatInfo*) (ins->data_ptr()[i]);
      float f = float_info->value();

      if (f == 0) {
        out_ << FCONST_0 << "\n";
      } else if (f == 1) {
        out_ << FCONST_1 << "\n";
      } else if (f == 2) {
        out_ << FCONST_2 << "\n";
      } else {
        out_ << LDC;
        float_info->WriteToJasmin(out_);
        out_ << "\n";
      }
    } else if (op == BASTORE || op == CASTORE || op == SASTORE
        || op == IASTORE) {
      IntegerInfo* integer_info = (IntegerInfo*) (ins->data_ptr()[i]);
      s4 value = integer_info->value();

      if (CanInlineIntegerLiteral(value)) {
        AddIconst(value);
        out_ << "\n";
      } else {
        out_ << LDC;
        ins->data_ptr()[i]->WriteToJasmin(out_);
        out_ << "\n";
      }
    } else {
      out_ << LDC2_W;
      ins->data_ptr()[i]->WriteToJasmin(out_);
      out_ << "\n";
    }

    out_ << op << "\n";
  }
}

/**
 * Translate a sparse-switch instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTss(const TydeInstruction* ins) {
  TranslateSources(ins);
  out_ << LOOKUPSWITCH << "\n";
  // Value/target pairs.
  for (int  i = 0; i < (int) ins->targets().size(); ++i) {
    if (ins->successors()[i + 1]->label() == -1) {
      LOGE("Label does not exist\n");
      exit(1);
    }
    out_ << "  " << ins->keys()[i] << " : Label"
         << ins->successors()[i + 1]->label() << "\n";
  }
  out_ << "  default : Label" << ins->successors()[0]->label() << "\n";
}

/**
 * Translate a packed-switch instruction.
 *
 * @param ins A Tyde instruction.
 */
void Translator::TranslateFmtTps(const TydeInstruction* ins) {
  TranslateSources(ins);
  out_ << TABLESWITCH << " " << ins->first_key() << "\n";
  for (int i = 0; i < (int) ins->targets().size(); ++i) {
    if (ins->successors()[i + 1]->label() == -1) {
      LOGE("Label does not exist\n");
      exit(1);
    }
    out_ << "  " << "Label" << ins->successors()[i + 1]->label() << "\n";
  }
  out_ << "  default : Label" << ins->successors()[0]->label() << "\n";
}

/**
 * Translate an ambiguous operator opcode and reference.
 *
 * @param ins A Tyde instruction.
 */
void Translator::AddTaoOpcodeAndRef(const TydeInstruction* ins) {
  Opcode opcode = ins->op();
  if (opcode == OP_RETURN) {
    if (ins->sources()[0].type.IsIntSubtype()) {
      out_ << IRETURN;
    } else {
      out_ << FRETURN;
    }
  } else if (opcode == OP_RETURN_WIDE) {
    if (ins->sources()[0].type.IsLong()) {
      out_ << LRETURN;
    } else {
      out_ << DRETURN;
    }
  } else if (opcode == OP_CONST_4 || opcode == OP_CONST_16
      || opcode == OP_CONST || opcode == OP_CONST_HIGH16) {
    if (ins->destination().type.IsIntSubtype()) {
      s4 constant = (s4) ins->constant();
      if (CanInlineIntegerLiteral(constant)) {
        AddIconst((s2) constant);
      } else {
        out_ << LDC;
        AddReference(ins);
      }
    } else if (ins->destination().type.IsFloat()) {
      u4 constant = ins->constant();
      float* f = (float*) &constant;
      if (*f == 0) {
        out_ << FCONST_0;
      } else if (*f == 1) {
        out_ << FCONST_1;
      } else if (*f == 2) {
        out_ << FCONST_2;
      } else {
        out_ << LDC;
        AddReference(ins);
      }
    } else if (ins->destination().type.IsObject() && ins->constant() == 0) {
      out_ << ACONST_NULL;
    } else {
      LOGW("Error while translating ao opcode: "
          "type %s - constant: %i\n",
          ins->destination().type.ToString().c_str(), ins->constant());
      s4 constant = (s4) ins->constant();
      if (CanInlineIntegerLiteral(constant)) {
        AddIconst((s2) constant);
      } else {
        out_ << LDC;
        AddReference(ins);
      }
    }
  } else if (opcode == OP_AGET) {
    Type destination_type = ins->destination().type;
    if (destination_type.IsIntSubtype()) {
      out_ << IALOAD;
    } else if (destination_type.IsFloat()) {
      out_ << FALOAD;
    } else {
      LOGE("Error while translating format 23x with aget\n");
      exit(1);
    }
  } else if (opcode == OP_AGET_WIDE) {
    Type destination_type = ins->destination().type;
    if (destination_type.IsLong()) {
      out_ << LALOAD;
    } else if (destination_type.IsDouble()) {
      out_ << DALOAD;
    } else {
      LOGE("Error while translating format 23x with aget-wide: "
          "type %s %s %s at register: %i %i %i\n",
          ins->sources()[0].type.ToString().c_str(),
          ins->sources()[1].type.ToString().c_str(),
          ins->destination().type.ToString().c_str(), ins->sources()[0].reg,
          ins->sources()[1].reg, ins->destination().reg);
      exit(1);
    }
  } else if (opcode == OP_APUT) {
    Type source_type = ins->sources()[2].type;
    if (source_type.IsIntSubtype()) {
      out_ << IASTORE;
    } else if (source_type.IsFloat()) {
      out_ << FASTORE;
    } else {
      LOGE("Error while translating format 23x with aput: "
          "type %s %s %s at register: %i %i %i\n",
          ins->sources()[0].type.ToString().c_str(),
          ins->sources()[1].type.ToString().c_str(),
          ins->sources()[2].type.ToString().c_str(), ins->sources()[0].reg,
          ins->sources()[1].reg, ins->sources()[2].reg);
      exit(1);
    }
  } else if (ins->op() == OP_APUT_WIDE) {
    Type source_type = ins->sources()[2].type;
    if (source_type.IsLong()) {
      out_ << LASTORE;
    } else if (source_type.IsDouble()) {
      out_ << DASTORE;
    } else {
      LOGE("Error while translating format 23x with aput-wide: "
          "type %s %s %s at register: %i %i %i\n",
          ins->sources()[0].type.ToString().c_str(),
          ins->sources()[1].type.ToString().c_str(),
          ins->sources()[2].type.ToString().c_str(), ins->sources()[0].reg,
          ins->sources()[1].reg, ins->sources()[2].reg);
      exit(1);
    }
  } else if (opcode == OP_NEW_ARRAY || opcode == OP_NEW_ARRAY_JUMBO) {
    if (ins->destination().type.IsPrimitiveArray()) {
      out_ << NEWARRAY;
      out_ << " " << ins->destination().type.ToJavaArrayType();
    } else {
      out_ << ANEWARRAY;
      AddReference(ins);
    }
  }
}

/**
 * Check if an integer can be encoded with two bytes.
 *
 * @param value An integer.
 */
bool Translator::CanInlineIntegerLiteral(s4 value) const {
  return value >= -32768 && value < 32768;
}

/**
 * Translate an ambiguous branching opcode.
 *
 * @param ins A Tyde instruction.
 */
void Translator::AddTabOpcode(const TydeInstruction* ins) {
  Type source_type = ins->sources()[0].type;
  Opcode opcode = ins->op();
  if (source_type.IsObject()) {
    if (opcode == OP_IF_EQZ)
      out_ << IFNULL;
    else if (opcode == OP_IF_NEZ)
      out_ << IFNONNULL;
    else if (opcode == OP_IF_EQ)
      out_ << IF_ACMPEQ;
    else
      out_ << IF_ACMPNE;
  } else if (source_type.IsIntSubtype()) {
    if (opcode == OP_IF_EQZ)
      out_ << IFEQ;
    else if (opcode == OP_IF_NEZ)
      out_ << IFNE;
    else if (opcode == OP_IF_EQ)
      out_ << IF_ICMPEQ;
    else
      out_ << IF_ICMPNE;
  } else {
    LOGE("Error while translating ab opcode: type: %s\n",
        source_type.ToString().c_str());
    exit(1);
  }
}

/**
 * Add an opcode to the current Java instruction.
 *
 * Uses the default mapping between Dalvik and Java opcodes. To be used when
 * it is not ambiguous
 *
 * @param op The Dalvik opcode to translate.
 */
bool Translator::AddOpcode(Opcode op) {
  const char* java_opcode = TydeInstrUtils::TydeGetJavaOpcodeFromOpcode(op);
  bool result = strcmp(java_opcode, "  ") != 0;
  if (result)
    out_ << java_opcode;
  return result;
}

void Translator::AddIload(int index) {
  switch (index) {
    case 0:
      out_ << ILOAD_0;
      break;
    case 1:
      out_ << ILOAD_1;
      break;
    case 2:
      out_ << ILOAD_2;
      break;
    case 3:
      out_ << ILOAD_3;
      break;
    default:
      out_ << ILOAD << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddLload(int index) {
  switch (index) {
    case 0:
      out_ << LLOAD_0;
      break;
    case 1:
      out_ << LLOAD_1;
      break;
    case 2:
      out_ << LLOAD_2;
      break;
    case 3:
      out_ << LLOAD_3;
      break;
    default:
      out_ << LLOAD << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddFload(int index) {
  switch (index) {
    case 0:
      out_ << FLOAD_0;
      break;
    case 1:
      out_ << FLOAD_1;
      break;
    case 2:
      out_ << FLOAD_2;
      break;
    case 3:
      out_ << FLOAD_3;
      break;
    default:
      out_ << FLOAD << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddDload(int index) {
  switch (index) {
    case 0:
      out_ << DLOAD_0;
      break;
    case 1:
      out_ << DLOAD_1;
      break;
    case 2:
      out_ << DLOAD_2;
      break;
    case 3:
      out_ << DLOAD_3;
      break;
    default:
      out_ << DLOAD << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddAload(int index) {
  switch (index) {
    case 0:
      out_ << ALOAD_0;
      break;
    case 1:
      out_ << ALOAD_1;
      break;
    case 2:
      out_ << ALOAD_2;
      break;
    case 3:
      out_ << ALOAD_3;
      break;
    default:
      out_ << ALOAD << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddIstore(int index) {
  switch (index) {
    case 0:
      out_ << ISTORE_0;
      break;
    case 1:
      out_ << ISTORE_1;
      break;
    case 2:
      out_ << ISTORE_2;
      break;
    case 3:
      out_ << ISTORE_3;
      break;
    default:
      out_ << ISTORE << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddLstore(int index) {
  switch (index) {
    case 0:
      out_ << LSTORE_0;
      break;
    case 1:
      out_ << LSTORE_1;
      break;
    case 2:
      out_ << LSTORE_2;
      break;
    case 3:
      out_ << LSTORE_3;
      break;
    default:
      out_ << LSTORE << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddFstore(int index) {
  switch (index) {
    case 0:
      out_ << FSTORE_0;
      break;
    case 1:
      out_ << FSTORE_1;
      break;
    case 2:
      out_ << FSTORE_2;
      break;
    case 3:
      out_ << FSTORE_3;
      break;
    default:
      out_ << FSTORE << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddDstore(int index) {
  switch (index) {
    case 0:
      out_ << DSTORE_0;
      break;
    case 1:
      out_ << DSTORE_1;
      break;
    case 2:
      out_ << DSTORE_2;
      break;
    case 3:
      out_ << DSTORE_3;
      break;
    default:
      out_ << DSTORE << " " << index;
      break;
  }
  out_ << "\n";
}

void Translator::AddAstore(int index) {
  switch (index) {
    case 0:
      out_ << ASTORE_0;
      break;
    case 1:
      out_ << ASTORE_1;
      break;
    case 2:
      out_ << ASTORE_2;
      break;
    case 3:
      out_ << ASTORE_3;
      break;
    default:
      out_ << ASTORE << " " << index;
      break;
  }
  out_ << "\n";
}

/**
 * Add an instruction to push an integer (up to 2 bytes) onto the stack
 *
 * @param value The value of the integer to be pushed onto the stack
 */
void Translator::AddIconst(const s2 value) {
  switch(value) {
    case -1:
      out_ << ICONST_M1;
      break;
    case 0:
      out_ << ICONST_0;
      break;
    case 1:
      out_ << ICONST_1;
      break;
    case 2:
      out_ << ICONST_2;
      break;
    case 3:
      out_ << ICONST_3;
      break;
    case 4:
      out_ << ICONST_4;
      break;
    case 5:
      out_ << ICONST_5;
      break;
    default:
      if(value >= -128 && value <= 127) {
        out_ << BIPUSH;
      } else {
        out_ << SIPUSH;
      }
      out_ << " " << value;
      break;
  }
}

void Translator::TranslateSources(const TydeInstruction* ins) {
  for (u4 i = 0; i < ins->sources().size(); ++i) {
    const Type& source_type = ins->sources()[i].type;
    if (source_type.IsIntSubtype()) {
      AddIload(registers_[ins->sources()[i].reg]);
    } else if (source_type.IsLong()) {
      AddLload(registers_[ins->sources()[i].reg]);
    } else if (source_type.IsFloat()) {
      AddFload(registers_[ins->sources()[i].reg]);
    } else if (source_type.IsDouble()) {
      AddDload(registers_[ins->sources()[i].reg]);
    } else if (source_type.IsObject()) {
      AddAload(registers_[ins->sources()[i].reg]);
    } else if (source_type.IsLit()){
      AddIconst((s4) ins->sources()[i].reg);
      out_ << "\n";
    } else {
      LOGW("Error when translating sources: type %s\n",
          source_type.ToString().c_str());
      AddIload(registers_[ins->sources()[i].reg]);
    }
  }
}

void Translator::TranslateDestination(const TydeInstruction* ins) {
  if (ins->has_destination()) {
    const Type& destination_type = ins->destination().type;
    if (destination_type.IsIntSubtype()) {
      AddIstore(registers_[ins->destination().reg]);
    } else if (destination_type.IsLong()) {
      AddLstore(registers_[ins->destination().reg]);
    } else if (destination_type.IsFloat()) {
      AddFstore(registers_[ins->destination().reg]);
    } else if (destination_type.IsDouble()) {
      AddDstore(registers_[ins->destination().reg]);
    } else if (destination_type.IsObject()) {
      AddAstore(registers_[ins->destination().reg]);
    } else {
      LOGW("Error when translating destination: type %s\n",
          ins->destination().type.ToString().c_str());
      AddIstore(registers_[ins->destination().reg]);
    }
  } else if (ins->destination().type.IsPop()) {
    out_ << POP << "\n";
  } else if (ins->destination().type.IsPop2()) {
    out_ << POP2 << "\n";
  }
}

void Translator::TranslateFmtTtve(const TydeInstruction* ins) {
  out_ << NEW << " " << ins->error_descriptor() << "\n";
  out_ << DUP << "\n";
  out_ << INVOKESPECIAL << " " << ins->error_descriptor() << "/<init>()V\n";
  out_ << ATHROW << "\n";
}

void Translator::AddReference(const TydeInstruction* ins) {
  if (ins->reference() != NULL)
    ins->reference()->WriteToJasmin(out_);
}


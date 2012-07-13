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
 * method_info.h
 *
 * A Java method.
 */

#ifndef CLASS_FILE_METHOD_INFO_H_
#define CLASS_FILE_METHOD_INFO_H_


#include <stdlib.h>

#include <ostream>
#include <vector>

#include "libdex/DexClass.h"

#include "class_file/code_attribute.h"
#include "class_file/flags.h"
#include "class_file/member.h"
#include "class_file/method.h"
#include "timer.h"


struct DexMethod;
class CodeAttribute;
class ConstantPool;
class TydeInstruction;


struct InstructionAndRegister {
  InstructionAndRegister(TydeInstruction* i, int r, bool s)
      : ins(i), reg(r), issource(s) {}

  TydeInstruction* ins;
  int reg;
  bool issource;
};

class MethodInfo : public Method, public Member {
 public:
  /**
   * Constructor for method without code.
   *
   * @param access_flags Access flags.
   * @param name_st The method name.
   * @param descriptor_st The method descriptor.
   * @param class_descriptor The descriptor of the enclosing class.
   */
  MethodInfo(unsigned short access_flags, const char* name_st,
      const char* descriptor_st, const char* class_descriptor)
      : Member(access_flags, name_st, descriptor_st),
        code_attribute_(NULL),
        class_descriptor_(class_descriptor) {}
  /**
   * Constructor for method with code.
   *
   * @param access_flags Access flags.
   * @param name_st The method name.
   * @param descriptor_st The method descriptor.
   * @param class_descriptor The descriptor of the enclosing class.
   */
  MethodInfo(unsigned short access_flags_, const char* name_st,
      const char* descriptor_st, const DexFile* dex_file,
      const DexMethod* dex_method, const char* class_descriptor)
      : Member(access_flags_, name_st, descriptor_st),
        code_attribute_(new CodeAttribute(this,
            dexGetCode(dex_file, dex_method))),
        class_descriptor_(class_descriptor) {}
  ~MethodInfo() {
    free((void *) descriptor_.bytes());
    delete code_attribute_;
  }

  CodeAttribute* code_attribute() const { return code_attribute_; }
  const char* class_descriptor() const { return class_descriptor_; }

  std::vector<InstructionAndRegister>& ambiguous_sources() {
    return ambiguous_sources_;
  }
  std::vector<InstructionAndRegister>& ambiguous_destinations() {
    return ambiguous_destinations_;
  }
  void AddAmbiguousSource(TydeInstruction* ins, int reg) {
    ambiguous_sources_.push_back(InstructionAndRegister(ins, reg, true));
  }
  void AddAmbiguousDestination(TydeInstruction* ins, int reg) {
    ambiguous_destinations_.push_back(InstructionAndRegister(ins, reg, false));
  }
  void Tydify(ConstantPool& cp) {
    if (code_attribute_ != NULL && code_attribute_->is_tyde_needed())
      code_attribute_->Tydify(cp);
  }
  void WriteToJasmin(std::ostream& out) const {
    out << "\n.method ";
    Flags::FlagToStream(access_flags_, kAccessForMethod, out);
    Utf8Info::WriteQuotedToJasmin(name_.bytes(), out);
    out << descriptor_.bytes() << '\n';
    Member::WriteToJasmin(out);
    Timer::getInstance().End();
    Timer::getInstance().Start(Timer::kTranslationPreprocessing);
    if (code_attribute_ != NULL)
      code_attribute_->WriteToJasmin(out);
    Timer::getInstance().End();
    Timer::getInstance().Start(Timer::kTranslationToJasmin);
    out << ".end <method>\n";
  }
  void Dump(std::ostream& out) const {
    out << name_.bytes() << "\n";
    if (code_attribute_ != NULL) code_attribute_->Dump(out);
  }

 private:
  CodeAttribute* code_attribute_;
  const char* class_descriptor_;

  /**
   * An ambiguous source is a register which is ambiguous and used as source.
   */
  std::vector<InstructionAndRegister> ambiguous_sources_;
  /**
   * An ambiguous destination is a register used as destination whose type is
   * ambiguous.
   */
  std::vector<InstructionAndRegister> ambiguous_destinations_;
};


#endif /* CLASS_FILE_METHOD_INFO_H_ */

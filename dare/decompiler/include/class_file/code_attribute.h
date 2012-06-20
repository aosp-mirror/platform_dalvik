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
 * code_attribute.h
 *
 * CodeAttribute describes a code attribute. It contains Tyde code.
 */

#ifndef CLASS_FILE_CODE_ATTRIBUTE_H_
#define CLASS_FILE_CODE_ATTRIBUTE_H_


#include <iosfwd>
#include <vector>

#include "class_file/attribute_info.h"
#include "tyde/tyde_body.h"


struct DexCode;
class ConstantPool;
class MethodInfo;


class CodeAttribute : public AttributeInfo {
 public:
  CodeAttribute(MethodInfo* method, const DexCode* dex_code)
      : method_(method), dex_code_(dex_code), is_tyde_needed_(true) {}
  ~CodeAttribute();

  TydeBody& tyde_body() { return tyde_body_; }
  const TydeBody& tyde_body() const { return tyde_body_; }
  MethodInfo* method() const { return method_; }
  const DexCode* dex_code() const { return dex_code_; }
  bool is_tyde_needed() const { return is_tyde_needed_; }
  void set_is_tyde_needed(const bool is_tyde_needed) {
    is_tyde_needed_ = is_tyde_needed;
  }

  void Tydify(ConstantPool& cp);
  void Dump(std::ostream& out) const;
  void WriteToJasmin(std::ostream& out);

 private:
  std::vector<AttributeInfo*> attributes_;
  TydeBody tyde_body_;
  MethodInfo* method_;
  const DexCode* dex_code_;
  bool is_tyde_needed_;
};


#endif /* CLASS_FILE_CODE_ATTRIBUTE_H_ */

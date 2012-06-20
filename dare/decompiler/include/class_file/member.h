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
 * member.h
 *
 * Base class for a Java member method or variable.
 */

#ifndef CLASS_FILE_MEMBER_H_
#define CLASS_FILE_MEMBER_H_


#include <vector>

#include "libdex/DexFile.h"

#include "class_file/attribute_info.h"
#include "class_file/synthetic_attribute.h"
#include "class_file/utf8_info.h"


class Member {
 public:
  Member(int access_flags_, const char* name_st, const char* descriptor_st)
  : access_flags_(access_flags_), name_(name_st), descriptor_(descriptor_st) {
    if ((access_flags_ & ACC_SYNTHETIC) == ACC_SYNTHETIC)
      AddAttribute(new SyntheticAttribute());
  }
  ~Member() {
    for (int i = 0; i < (int) attributes_.size(); ++i)
      delete attributes_[i];
  }

  int access_flags() const { return access_flags_; }
  const Utf8Info& name() const { return name_; }
  const Utf8Info& descriptor() const { return descriptor_; }

  void AddAttribute(AttributeInfo* attribute) {
    attributes_.push_back(attribute);
  }
  void WriteToJasmin(std::ostream& out) const {
    for (int i = 0; i < (int) attributes_.size(); ++i)
      attributes_[i]->WriteToJasmin(out);
  }

 protected:
  int access_flags_;
  Utf8Info name_;
  Utf8Info descriptor_;
  std::vector<AttributeInfo*> attributes_;
};


#endif /* CLASS_FILE_MEMBER_H_ */

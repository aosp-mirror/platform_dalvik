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
 * field_info.h
 *
 * A Java field.
 */

#ifndef CLASS_FILE_FIELD_INFO_H_
#define CLASS_FILE_FIELD_INFO_H_


#include <iosfwd>

#include "class_file/constant_pool_info.h"
#include "class_file/flags.h"
#include "class_file/member.h"
#include "class_file/utf8_info.h"

class ConstantPoolInfo;


class FieldInfo : public Member {
 public:
  FieldInfo(unsigned short access_flags_, const char* name_st,
             const char* descriptor_st)
      : Member(access_flags_, name_st, descriptor_st),
        value_(NULL) {}

  void set_value(ConstantPoolInfo* value) { value_ = value; }

  void WriteToJasmin(std::ostream& out) const {
    out << ".field ";
    Flags::FlagToStream(access_flags_, kAccessForField, out);
    out << "\"";
    Utf8Info::WriteQuotedToJasmin(name_.bytes(), out);
    out << "\" " << descriptor_.bytes();
    if (value_ != NULL) {
      out << " = ";
      value_->WriteToJasmin(out);
    }
    out << '\n';
  }

 private:
  ConstantPoolInfo* value_;
};


#endif /* CLASS_FILE_FIELD_INFO_H_ */

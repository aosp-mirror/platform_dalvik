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
 * field_ref_info.h
 *
 * A Java field reference.
 */

#ifndef CLASS_FILE_FIELD_REF_INFO_H_
#define CLASS_FILE_FIELD_REF_INFO_H_


#include <ostream>

#include "class_file/fmi_ref_info.h"


class FieldRefInfo : public FMIRefInfo {
 public:
  FieldRefInfo(const char* class_st, const char* name_st,
      const char* descriptor_st, bool is_static)
      : FMIRefInfo(class_st, name_st, descriptor_st),
        is_static_(is_static) {}

  void WriteToJasmin(std::ostream& out) const {
    out << " ";
    class_info_.this_name().WriteToJasmin(out, false);
    out << "/";
    Utf8Info::WriteQuotedToJasmin(nameandtype_.name().bytes(), out);
    out << " " << nameandtype_.descriptor().bytes();
  }

  void WriteStubToJasmin(std::ostream& out) const {
    out << ".field public ";
    if (is_static_) out << "static ";
    out << "\"";
    Utf8Info::WriteQuotedToJasmin(nameandtype_.name().bytes(), out);
    out << "\"" << this->nameandtype_.descriptor().bytes() << '\n';
  }

 private:
  bool is_static_;
};


#endif /* CLASS_FILE_FIELD_REF_INFO_H_ */

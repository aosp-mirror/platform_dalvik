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
 * class_info.h
 *
 * ClassInfo describes a class reference.
 */

#ifndef CLASS_FILE_CLASS_INFO_H_
#define CLASS_FILE_CLASS_INFO_H_


#include <ostream>

#include "class_file/constant_pool_info.h"
#include "class_file/utf8_info.h"


class ClassInfo : public ConstantPoolInfo {
 public:
  ClassInfo(const char* class_name_st, bool is_new_array)
      : this_name_(class_name_st),
        is_array_type_(is_new_array) {}

  const Utf8Info& this_name() const { return this_name_; }

  void WriteToJasmin(std::ostream& out) const {
    out << " ";
    this_name_.WriteToJasmin(out, is_array_type_);
  }

 private:
  Utf8Info this_name_;
  bool is_array_type_;
};


#endif /* CLASS_FILE_CLASS_INFO_H_ */

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
 * nameandtype_info.h
 *
 * A Java name and type descriptor constant.
 */

#ifndef CLASS_FILE_NAMEANDTYPE_INFO_H_
#define CLASS_FILE_NAMEANDTYPE_INFO_H_


#include <ostream>

#include "class_file/constant_pool_info.h"
#include "class_file/utf8_info.h"


class NameAndTypeInfo : public ConstantPoolInfo {
 public:
  NameAndTypeInfo(const char* name_st, const char* descriptor_st)
      : name_(name_st),
        descriptor_(descriptor_st) {}

  const Utf8Info& name() const { return name_; }
  const Utf8Info& descriptor() const { return descriptor_; }

  void WriteToJasmin(std::ostream& out) const {}

 private:
  Utf8Info name_;
  Utf8Info descriptor_;
};


#endif /* CLASS_FILE_NAMEANDTYPE_INFO_H_ */

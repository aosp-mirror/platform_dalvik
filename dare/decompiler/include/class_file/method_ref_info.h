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
 * method_ref_info.h
 *
 * A Java method reference constant.
 */

#ifndef CLASS_FILE_METHOD_REF_INFO_H_
#define CLASS_FILE_METHOD_REF_INFO_H_


#include <stdlib.h>

#include <iosfwd>

#include "class_file/fmi_ref_info.h"
#include "class_file/method.h"


class MethodRefInfo : public FMIRefInfo, public Method {
 public:
  MethodRefInfo(const char* class_st, const char* name_st,
      const char* descriptor_st, bool is_static)
      : FMIRefInfo(class_st, name_st, descriptor_st),
        is_static_(is_static) {}
  ~MethodRefInfo() { free((void *) nameandtype_.descriptor().bytes()); }

  void WriteToJasmin(std::ostream& out) const;
  void WriteStubToJasmin(std::ostream& out) const;

 private:
  bool is_static_;
};


#endif /* CLASS_FILE_METHOD_REF_INFO_H_ */

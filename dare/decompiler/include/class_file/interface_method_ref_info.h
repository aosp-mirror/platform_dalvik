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
 * interface_method_ref_info.h
 *
 * A Java interface method reference.
 */

#ifndef CLASS_FILE_INTERFACE_METHOD_REF_INFO_H_
#define CLASS_FILE_INTERFACE_METHOD_REF_INFO_H_


#include <stdlib.h>

#include <ostream>

#include "class_file/fmi_ref_info.h"
#include "class_file/method.h"


class InterfaceMethodRefInfo : public FMIRefInfo, public Method {
 public:
  InterfaceMethodRefInfo(const char* class_st, const char* name_st,
      const char* descriptor_st)
      : FMIRefInfo(class_st, name_st, descriptor_st) {}
  ~InterfaceMethodRefInfo() {
    free((void*) nameandtype_.descriptor().bytes());
  }

  void WriteToJasmin(std::ostream& out) const {
    out << " ";
    class_info_.this_name().WriteToJasmin(out, false);
    out << "/" << nameandtype_.name().bytes()
        << nameandtype_.descriptor().bytes();
  }

  void WriteStubToJasmin(std::ostream& out) const {
    out << ".method public abstract " << nameandtype_.name().bytes()
        << nameandtype_.descriptor().bytes() << '\n'
        << ".end <method>\n\n";
  }
};


#endif /* CLASS_FILE_INTERFACE_METHOD_REF_INFO_H_ */

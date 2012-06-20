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
 * fmi_ref_info.h
 *
 * Base class for field reference, method reference and interface method
 * reference.
 */

#ifndef CLASS_FILE_FMI_REF_INFO_H_
#define CLASS_FILE_FMI_REF_INFO_H_


#include "class_file/class_info.h"
#include "class_file/constant_pool_info.h"
#include "class_file/nameandtype_info.h"


class FMIRefInfo : public ConstantPoolInfo {
 public:
  FMIRefInfo(const char* class_st, const char* name_st,
      const char* descriptor_st)
      : class_info_(class_st, false),
        nameandtype_(name_st, descriptor_st) {}

  const char* class_name() const { return class_info_.this_name().bytes(); }
  const NameAndTypeInfo& nameandtype() const { return nameandtype_; }

 protected:
  ClassInfo class_info_;
  NameAndTypeInfo nameandtype_;
};


#endif /* CLASS_FILE_FMI_REF_INFO_H_ */

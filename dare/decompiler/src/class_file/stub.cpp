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

/*
 * stub.cpp
 *
 * A class or interface stub.
 */

#include "class_file/stub.h"

#include <stdlib.h>
#include <string.h>

#include <fstream>

#include "class_file/class_file.h"
#include "class_file/constant_pool.h"


/**
 * Write a class stub to file in Jasmin format. Return a pointer to the
 * descriptor of the generated class in dot format. This descriptor should be
 * freed by the calling method.
 *
 * @param directory The directory where the stub should be written.
 * @return The descriptor of the generated class in dot format. To be freed by
 *         the caller.
 */
char* Stub::WriteToJasmin(const std::string& directory) {
  char* class_name_dots = ClassFile::DescriptorClassToDot(class_name_);
  std::ofstream out((directory + "/" + class_name_dots + ".jasmin").c_str());

  if (is_interface_ || !interface_method_refs_.empty())
    out << ".interface public abstract ";
  else
    out << ".class public ";
  const char* past_l = class_name_ + 1;  // start past 'L'
  char* class_name = strdup(past_l);
  class_name[strlen(past_l) - 1] = '\0';
  out << class_name << "\n";

  if (strcmp(class_name, "java/lang/Object") != 0) {
    if (superclass_ != NULL) {
      const char* past_l = superclass_ + 1;  // start past 'L'
      char* superclass_name = strdup(past_l);
      superclass_name[strlen(past_l) - 1] = '\0';
      out << ".super " << superclass_name << "\n\n";
      free(superclass_name);
    } else {
      out << ".super java/lang/Object\n\n";
    }
  } else {
    out << ".no_super\n\n";
  }
  free(class_name);

  for (int i = 0; i < (int) interfaces_.size(); ++i) {
    const char* past_l = interfaces_[i] + 1;  // start past 'L'
    char* class_name = strdup(past_l);
    class_name[strlen(past_l) - 1] = '\0';
    out << ".implements " << class_name << "\n";
    free(class_name);
  }

//  for (std::set<u4>::iterator it = field_refs_.begin();
//      it != field_refs_.end(); ++it)
//    ConstantPool::WriteFieldStubToJasmin(*it, out);
//
//  out << "\n";
//
//  for (std::set<u4>::iterator it = interface_method_refs_.begin();
//      it != interface_method_refs_.end(); ++it)
//    ConstantPool::WriteInterfaceMethodStubToJasmin(*it, out);
//
//  out << "\n";
//
//  for (std::set<u4>::iterator it = method_refs_.begin();
//      it != method_refs_.end(); ++it)
//    ConstantPool::WriteMethodStubToJasmin(*it, out);

  out.close();

  return class_name_dots;
}

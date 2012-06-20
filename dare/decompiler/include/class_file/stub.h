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
 * stub.h
 *
 * A class or interface stub.
 */

#ifndef CLASS_FILE_STUB_H_
#define CLASS_FILE_STUB_H_


#include <iosfwd>
#include <set>
#include <vector>

#include "int_types.h"


class Stub {
 public:
  Stub(const char* class_name, bool is_interface, bool is_necessary,
      const char* superclass = NULL)
      : class_name_(class_name),
        superclass_(superclass),
        is_interface_(is_interface),
        is_necessary_(is_necessary) {}

  void set_superclass(const char* superclass) {
    superclass_ = superclass;
  }
  void set_is_interface(bool is_interface) { is_interface_ = is_interface; }
  void set_interfaces(const std::vector<const char*>& interfaces) {
    interfaces_ = interfaces;
  }
  bool is_necessary() const { return is_necessary_; }
  void set_is_necessary(bool is_necessary) { is_necessary_ = is_necessary; }

  /**
   * Add a field reference. To be called when a field reference is found in the
   * Dalvik bytecode.
   *
   * @param field_index The index of the field in the Dalvik constant pool.
   */
  void AddFieldRef(u4 field_index) { field_refs_.insert(field_index); }

  /**
   * Add an interface method reference. To be called when an interface method
   * reference is found in the Dalvik bytecode.
   *
   * @param method_index The index of the method in the Dalvik constant pool.
   */
  void AddInterfaceMethodRef(u4 method_index) {
    interface_method_refs_.insert(method_index);
  }

  /**
   * Add a method reference. To be called when a method
   * reference is found in the Dalvik bytecode.
   *
   * @param method_index The index of the method in the Dalvik constant pool.
   */
  void AddMethodRef(u4 method_index) { method_refs_.insert(method_index); }

  /**
   * Write a class stub to file in Jasmin format. Return a pointer to the
   * descriptor of the generated class in dot format. This descriptor should be
   * freed by the calling method.
   *
   * @param directory The directory where the stub should be written.
   * @return The descriptor of the generated class in dot format. To be freed by
   *         the caller.
   */
  char* WriteToJasmin(const std::string& directory);

 private:
  const char* class_name_;
  const char* superclass_;
  bool is_interface_;
  bool is_necessary_;
  std::vector<const char*> interfaces_;
  std::set<u4> field_refs_;
  std::set<u4> interface_method_refs_;
  std::set<u4> method_refs_;
};

#endif /* CLASS_FILE_STUB_H_ */

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
 * type_element.h
 *
 * Base class for type constants and type variables.
 */

#ifndef TYPING_TYPE_ELEMENT_H_
#define TYPING_TYPE_ELEMENT_H_


#include <string>

#include "typing/type.h"

class TydeInstruction;


class TypeElement {
 public:
  TypeElement(const bool is_constant, const Type& type,
      TydeInstruction* const instruction, const bool is_source, const int reg)
      : is_constant_(is_constant),
        type_(type),
        instruction_(instruction),
        is_source_(is_source),
        reg_(reg) {}
  virtual ~TypeElement() {}

  void set_type(const Type& type);
  void set_array_type_element(TypeElement* type_element) {
    type_.set_array_type_element(type_element);
  }

  /**
   * Is the type element a constant, that is, a type known at parse time?
   *
   * @return True if the type element is a constant.
   */
  bool is_constant() const { return is_constant_; }
  Type type() const { return type_; }
  TydeInstruction* instruction() const { return instruction_; }
  bool is_source() const { return is_source_; }
  int reg() const { return reg_;}

  bool operator<(const TypeElement& rhs) const;

  virtual std::string ToString(bool verbose = false) const = 0;

 private:
  const bool is_constant_;
  Type type_;
  TydeInstruction* const instruction_;
  const bool is_source_;
  const int reg_;
};


#endif /* TYPING_TYPE_ELEMENT_H_ */

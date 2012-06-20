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
 * type_constant.h
 *
 * A type constant is a type which is fully determined at parse time.
 */

#ifndef TYPING_TYPE_CONSTANT_H_
#define TYPING_TYPE_CONSTANT_H_


#include "tyde/tyde_instruction.h"
#include "typing/type_element.h"


class TypeConstant : public TypeElement {
 public:
  TypeConstant(const Type& type, TydeInstruction* instruction,
      const bool is_source, const u4 reg)
      : TypeElement(true, type, instruction, is_source, reg) {}

  virtual std::string ToString(bool verbose = false) const {
    if (verbose) {
      std::string result = type().ToString() + " at instruction: "
          + instruction()->ToString();
      return result;
    } else {
      return type().ToString();
    }
  }
};


#endif /* TYPING_TYPE_CONSTANT_H_ */

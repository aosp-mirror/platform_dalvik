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
 * method.h
 *
 * Base class for a Java method (reference or member).
 */

#ifndef CLASS_FILE_METHOD_H_
#define CLASS_FILE_METHOD_H_


#include <vector>

#include "typing/type.h"


class Method {
 public:
  void set_return_type(const Type& return_type) { return_type_ = return_type; }
  void set_arguments(const std::vector<Type>& arguments) {
    arguments_ = arguments;
  }
  Type return_type() const { return return_type_; }
  std::vector<Type> arguments() const { return arguments_; }

  /**
   * Compute the first register corresponding to a certain argument of the
   * method.
   *
   * @param arg_idx The index of the argument.
   * @return The register corresponding to the argument.
   */
  int RegisterIndex(int arg_idx) const {
    // Index of the register in the list of the method arguments
    int nb_reg = 0;

    for (int i = 0; i < arg_idx; ++i)
      nb_reg += arguments()[i].Width();

    return nb_reg;
  }

 private:
  Type return_type_;
  std::vector<Type> arguments_;
};


#endif /* CLASS_FILE_METHOD_H_ */

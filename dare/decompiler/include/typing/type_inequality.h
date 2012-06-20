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
 * type_inequality.h
 *
 * A type inequality.
 */

#ifndef TYPING_TYPE_INEQUALITY_H_
#define TYPING_TYPE_INEQUALITY_H_


#include <string>

#include "typing/type_element.h"


class TypeInequality {
 public:
  TypeInequality() : left_element_(NULL), right_element_(NULL) {}
  TypeInequality(TypeElement* left_element, TypeElement* right_element)
      : left_element_(left_element),
        right_element_(right_element) {}

  TypeElement* left_element() const { return left_element_; }
  TypeElement* right_element() const { return right_element_; }

  bool operator<(const TypeInequality& rhs) const {
    if (left_element() < rhs.left_element()) {
      return true;
    } else {
      return (left_element() == rhs.left_element() && right_element()
          < rhs.right_element());
    }
    return false;
  }
  std::string ToString(bool verbose = false) const {
    std::string result;
    result.append(left_element_->ToString(verbose));
    result.append(" <= ");
    result.append(right_element_->ToString(verbose));
    return result;
  }

 private:
  TypeElement* left_element_;
  TypeElement* right_element_;
};


#endif /* TYPING_TYPE_INEQUALITY_H_ */

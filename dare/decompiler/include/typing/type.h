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
 * type.h
 *
 * A type.
 */

#ifndef TYPING_TYPE_H_
#define TYPING_TYPE_H_


#include <stddef.h>

#include <string>

#include "typing/var_type.h"


struct TypeElement;


class Type {
 public:
  Type()
      : type_(kUnknown),
        dim_(0),
        component_type_element_(NULL),
        array_type_element_(NULL) {}
  Type(VarType type, int dim = 0, TypeElement* type_element = NULL)
      : type_(type),
        dim_(dim),
        component_type_element_(type_element),
        array_type_element_(NULL) {}

  VarType type() const { return type_; }
  void setType(VarType type) { type_ = type; }
  int dim() const { return dim_; }
  void setDim(int dim) { dim_ = dim; }
  TypeElement* component_type_element() const {
    return component_type_element_;
  }
  void set_component_type_element(TypeElement* type_element) {
    component_type_element_ = type_element;
  }
  TypeElement* array_type_element() const {
    return array_type_element_;
  }
  void set_array_type_element(TypeElement* type_element) {
    array_type_element_ = type_element;
  }

  /**
   * Turn a type descriptor into a type.
   *
   * @param type Type descriptor.
   * @return The Type object corresponding to the input type descriptor.
   */
  static Type ParseType(const char* type);

  bool operator==(const Type& other) const {
    return type_ == other.type_ && dim_ == other.dim_;
  }
  bool operator!=(const Type& other) const {
    return !(*this == other);
  }

  /**
   * Get the width (in words) of this type (2 for double and long, 1 for
   * other types).
   *
   * @return The width of this type.
   */
  int Width() const;
  bool IsIntSubtype() const {
    return dim_ == 0 && (type_ == kInt || type_ == kShort || type_ == kChar
        || type_ == kBoolean || type_ == kByte);
  }
  bool IsUnknown() const {
    return type_ == kUnknown || type_ == kFIUnknown || type_ == kDLUnknown
        || type_ == kAFIUnknown || type_ == kADLUnknown || type_ == kACSUnknown
        || type_ == kTrioUnknown;
  }
  bool IsLong() const {
    return type_ == kLong && dim_ == 0;
  }
  bool IsFloat() const {
    return type_ == kFloat && dim_ == 0;
  }
  bool IsDouble() const {
    return type_ == kDouble && dim_ == 0;
  }
  bool IsObject() const {
    return (type_ & kObject) == kObject || dim_ > 0;
  }
  bool IsLit() const {
    return type_ == kLit;
  }
  bool IsInt() const {
    return type_ == kInt && dim_ == 0;
  }
  bool IsChar() const {
    return type_ == kChar && dim_ == 0;
  }
  bool IsByte() const {
    return type_ == kByte && dim_ == 0;
  }
  bool IsShort() const {
    return type_ == kShort && dim_ == 0;
  }
  bool IsBoolean() const {
    return type_ == kBoolean && dim_ == 0;
  }
  bool IsVoid() const {
    return type_ == kVoid;
  }
  bool IsPop() const {
    return type_ == kPop;
  }
  bool IsPop2() const {
    return type_ == kPop2;
  }
  bool IsPrimitive() const {
    return (type_ == kBoolean || type_ == kChar || type_ == kByte
        || type_ == kShort || type_ == kInt || type_ == kFloat
        || type_ == kLong || type_ == kDouble || type_ == kFIUnknown
        || type_ == kDLUnknown) && dim_ == 0;
  }
  bool IsTrioUnknown() const {
    return type_ == kTrioUnknown;
  }
  bool IsArray() const {
    return dim_ > 0 || type_ == kAFIUnknown || type_ == kDLUnknown
        || type_ == kACSUnknown;
  }
  bool IsNAObject() const {
    return type_ == kNAObject;
  }
  bool IsPrimitiveArray() const {
    return (type_ == kBoolean || type_ == kChar || type_ == kByte
        || type_ == kShort || type_ == kInt || type_ == kFloat
        || type_ == kLong || type_ == kDouble) && dim_ == 1;
  }
  bool IsBooleanArray() const {
    return type_ == kBoolean && dim_ == 1;
  }
  bool IsCharArray() const {
    return type_ == kChar && dim_ == 1;
  }
  bool IsFloatArray() const {
    return type_ == kFloat && dim_ == 1;
  }
  bool IsByteArray() const {
    return type_ == kByte && dim_ == 1;
  }
  bool IsShortArray() const {
    return type_ == kShort && dim_ == 1;
  }
  bool IsIntArray() const {
    return type_ == kInt && dim_ == 1;
  }
  bool IsLongArray() const {
    return type_ == kLong && dim_ == 1;
  }
  bool IsDoubleArray() const {
    return type_ == kDouble && dim_ == 1;
  }
  bool IsAObjectUnknown() const {
    return type_ == kAObjectUnknown;
  }
  bool IsBottomObject() const {
    return type_ == kBottomObject;
  }
  bool IsUnknownObject() const {
    return type_ == kObject;
  }
  bool IsConflict() const {
    return type_ == kConflict;
  }

  /**
   * Get a string representation of the component type of an array type.
   *
   * @return The component type.
   */
  const char* ToJavaArrayType() const;

  /**
   * Get a string representation of this type.
   *
   * @return A string representation of this type.
   */
  std::string ToString() const;

 private:
  /**
   * Get a string representation of a VarType.
   *
   * @param A VarType.
   * @return A string representation of a VarType.
   */
  static std::string ToString(VarType type);

  VarType type_;
  int dim_;

  /**
   * If this Type is an array type, the component_type_element_ is for the
   * component type.
   */
  TypeElement* component_type_element_;

  /**
   * It this Type is a component type, the array_type_element_ is for the
   * array type.
   */
  TypeElement* array_type_element_;
};


#endif /* TYPING_TYPE_H_ */

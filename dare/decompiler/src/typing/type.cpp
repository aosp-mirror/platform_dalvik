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
 * type.cpp
 *
 * Class containing a type.
 */


#include "typing/type.h"

#include <stdlib.h>

#include "vm/Common.h"


/**
 * Turn a type descriptor into a type.
 *
 * @param type Type descriptor.
 * @return The Type object corresponding to the input type descriptor.
 */
/*static*/ Type Type::ParseType(const char* type) {
  int array_dimension = 0;
  VarType prim_type = kUnknown;
  while (type[array_dimension] == '[')
    ++array_dimension;

  switch (type[array_dimension]) {
    case 'V':
      prim_type = kVoid;
      break;
    case 'Z':
      prim_type = kBoolean;
      break;
    case 'B':
      prim_type = kByte;
      break;
    case 'S':
      prim_type = kShort;
      break;
    case 'C':
      prim_type = kChar;
      break;
    case 'I':
      prim_type = kInt;
      break;
    case 'J':
      prim_type = kLong;
      break;
    case 'F':
      prim_type = kFloat;
      break;
    case 'D':
      prim_type = kDouble;
      break;
    default:
      prim_type = kNAObject;
      break;
  }

  return Type(prim_type, array_dimension);
}

/**
 * Get the width (in words) of this type (2 for double and long, 1 for
 * other types).
 *
 * @return The width of this type.
 */
int Type::Width() const {
  if (dim_ == 0 && (type_ == kLong || type_ == kDouble)) {
    return 2;
  } else if (type_ == kVoid){
    return 0;
  } else {
    return 1;
  }
}

/**
 * Get a string representation of the component type of an array type.
 *
 * @return The component type.
 */
const char* Type::ToJavaArrayType() const {
  switch (type_) {
    case kBoolean:
      return "boolean";
    case kChar:
      return "char";
    case kFloat:
      return "float";
    case kDouble:
      return "double";
    case kByte:
      return "byte";
    case kShort:
      return "short";
    case kInt:
      return "int";
    case kLong:
      return "long";
    default:
      LOGE("Error while determining array type\n");
      exit(1);
  }
}

/**
 * Get a string representation of this type.
 *
 * @return A string representation of this type.
 */
std::string Type::ToString() const {
  std::string result;
  for (int i = 0; i < dim_; ++i)
    result.append("[");
  result.append(ToString(type_));
  return result;
}

/**
 * Get a string representation of a VarType.
 *
 * @param A VarType.
 * @return A string representation of a VarType.
 */
/*static*/ std::string Type::ToString(const VarType type) {
  switch (type) {
    case kBoolean:
      return "boolean";
    case kChar:
      return "char";
    case kByte:
      return "byte";
    case kShort:
      return "short";
    case kInt:
      return "int";
    case kFloat:
      return "float";
    case kLong:
      return "long";
    case kDouble:
      return "double";
    case kLit:
      return "literal";
    case kVoid:
      return "void";
    case kNAObject:
      return "na-object";
    case kObject:
      return "object";
    case kUnknown:
      return "unknown";
    case kTrioUnknown:
      return "trio-unknown";
    case kFIUnknown:
      return "fi-unknown";
    case kDLUnknown:
      return "dl-unknown";
    case kAFIUnknown:
      return "afi-unknown";
    case kADLUnknown:
      return "adl-unknown";
    case kACSUnknown:
      return "acs-unknown";
    default: {
      return "really-unknown";
    }
  }
}

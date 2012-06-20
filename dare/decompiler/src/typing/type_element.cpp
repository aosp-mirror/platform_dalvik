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

#include "typing/type_element.h"


void TypeElement::set_type(const Type& type) {
  TypeElement* old_component_type_element = type_.component_type_element();
  TypeElement* old_array_type_element = type_.array_type_element();
  type_ = type;

  // If updating to an array type, also update the element type.
  if (old_component_type_element != NULL) {
    type_.set_component_type_element(old_component_type_element);
    if (type_.dim() > 0 && old_component_type_element->type().IsUnknown()
        && !type_.IsUnknown()) {
      Type element_type = type_;
      element_type.setDim(type_.dim() - 1);
      old_component_type_element->set_type(element_type);
    }
  }

  // If updating to a component type, also update the array type.
  if (old_array_type_element != NULL) {
    type_.set_array_type_element(old_array_type_element);
    if (old_array_type_element->type().IsUnknown()
        && !type_.IsUnknown()) {
      Type element_type = type_;
      element_type.setDim(type_.dim() + 1);
      old_array_type_element->set_type(element_type);
    }
  }
}

bool TypeElement::operator<(const TypeElement& rhs) const {
  if (instruction() < rhs.instruction()) {
    return true;
  } else if (instruction() == rhs.instruction()) {
    if (!is_constant() && rhs.is_constant()) {
      return true;
    } else if (is_constant() == rhs.is_constant()) {
      if (!is_source() && rhs.is_source()) {
        return true;
      } else {
        return (is_source() == rhs.is_source() && reg() < rhs.reg());
      }
    }
  }
  return false;
}

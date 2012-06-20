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
 * VarType.h
 *
 * A VarType is either a (possibly ambiguous) primitive type or an object type
 * (possibly an ambiguous array). A Type object adds array dimension and array
 * relationship, as well as a convenient interface to types.
 */

#ifndef TYPING_VAR_TYPE_H_
#define TYPING_VAR_TYPE_H_


namespace {
const int kShiftLit = 20;
const int kShiftObject = 24;
const int kShiftVoid = 28;
const int kUnknownArray = 1024;
}


enum VarType {
  kUnknown = 0,
  kTrioUnknown = 1,
  kFIUnknown,
  kDLUnknown,

  // Integer types.
  kBoolean,
  kChar,
  kByte,
  kShort,
  kInt,

  // Other primitive types.
  kFloat,
  kLong,
  kDouble,
  kConflict,
  kPrimitiveTypeCount,

  // Integer literal.
  kLit = 1 << kShiftLit,

  // Pop types.
  kPop,
  kPop2,

  // Object types.
  kObject = 1 << kShiftObject,
  kNAObject = 1 << 12 | kObject,         // Non-array object.
  kAFIUnknown = kUnknownArray | kObject,
  kADLUnknown = (kUnknownArray + 1) | kObject,
  kACSUnknown = (kUnknownArray + 2) | kObject,
  kAObjectUnknown = (kUnknownArray + 3) | kObject,
  kBottomObject = (kObject + 1) | kObject,

  // Used for methods only.
  kVoid = 1 << kShiftVoid,
};


#endif /* TYPING_VAR_TYPE_H_ */

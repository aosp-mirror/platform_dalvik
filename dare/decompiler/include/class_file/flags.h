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
 * flags.h
 *
 * Java flags.
 */

#ifndef CLASS_FILE_FLAGS_H_
#define CLASS_FILE_FLAGS_H_


#include <iosfwd>

#include "int_types.h"


enum AccessFor {
  kAccessForClass = 0,
  kAccessForMethod,
  kAccessForField,
  kAccessForMAX
};


class Flags {
 public:
  static void FlagToStream(u4 flags, AccessFor forWhat, std::ostream& out);

 private:
  static const int kNumFlags = 18;
  static const char* kAccessStrings[kAccessForMAX][kNumFlags];
};


#endif /* CLASS_FILE_FLAGS_H_ */

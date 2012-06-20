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
 * float_info.h
 *
 * A Java float constant.
 */

#ifndef CLASS_FILE_FLOAT_INFO_H_
#define CLASS_FILE_FLOAT_INFO_H_


#include <iomanip>
#include <iostream>
#include <limits>
#include <sstream>
#include <string>

#include "class_file/constant_pool_info.h"


class FloatInfo : public ConstantPoolInfo {
 public:
  FloatInfo(s4 value) : bytes_(value) {}
  void WriteToJasmin(std::ostream& out) const  {
    float* f = (float*) &bytes_;
    std::stringstream s;
    s << *f;
    std::string string_value = s.str();

    if (string_value == "nan")
      out << " +FloatNaN";
    else if (string_value == "inf")
      out << " +FloatInfinity";
    else if (string_value == "-inf")
      out << " -FloatInfinity";
    else {
      out << " " << std::fixed;
      out << std::setprecision(std::numeric_limits<float>::digits10) << *f;
    }
  }

  float value() const {
    float* f = (float*) &bytes_;
    return *f;
  }

 private:
  s4 bytes_;
};


#endif /*CLASS_FILE_FLOAT_INFO_H_*/

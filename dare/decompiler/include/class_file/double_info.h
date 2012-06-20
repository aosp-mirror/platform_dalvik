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
 * double_info.h
 *
 * A double constant.
 */

#ifndef CLASS_FILE_DOUBLE_INFO_H_
#define CLASS_FILE_DOUBLE_INFO_H_


#include <iomanip>
#include <limits>
#include <ostream>
#include <sstream>
#include <string>

#include "class_file/constant_pool_info.h"
#include "int_types.h"


class DoubleInfo : public ConstantPoolInfo {
 public:
  DoubleInfo(s8 value) : bytes_(value) {}

  void WriteToJasmin(std::ostream& out) const {
    double* d = (double*) &bytes_;
    std::stringstream s;
    s << *d;
    std::string string_value = s.str();

    if (string_value == "nan")
      out << " +DoubleNaN";
    else if (string_value == "inf")
      out << " +DoubleInfinity";
    else if (string_value == "-inf")
      out << " -DoubleInfinity";
    else
      out << " " << std::fixed << std::setprecision(
          std::numeric_limits<double>::digits10) << *d;
  }

 private:
  s8 bytes_;
};


#endif /* CLASS_FILE_DOUBLE_INFO_H_ */

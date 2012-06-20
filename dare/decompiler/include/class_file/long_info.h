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
 * long_info.h
 *
 * A Java long constant.
 */

#ifndef CLASS_FILE_LONG_INFO_H_
#define CLASS_FILE_LONG_INFO_H_


#include <ostream>

#include "class_file/constant_pool_info.h"


class LongInfo : public ConstantPoolInfo {
 public:
  LongInfo(s8 value) : bytes_(value) {}

  void WriteToJasmin(std::ostream& out) const  { out << " " << bytes_; }

 private:
  s8 bytes_;
};


#endif /* CLASS_FILE_LONG_INFO_H_ */

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
 * utf8_info.h
 *
 * Data for a Java string.
 */

#ifndef CLASS_FILE_UTF8_INFO_H_
#define CLASS_FILE_UTF8_INFO_H_


#include <iosfwd>

#include "class_file/constant_pool_info.h"


class Utf8Info : public ConstantPoolInfo {
 public:
  Utf8Info(const char* bytes) : bytes_(bytes) {}

  const char* bytes() const { return bytes_; }
  void set_bytes(const char* bytes) { bytes_ = bytes; }

  void WriteToJasmin(std::ostream& out) const;

  /**
   * Write Jasmin representation to output stream. To be used for type
   * descriptors.
   *
   * @param out The output stream.
   * @param is_new_array True if the descriptor represents an array type.
   */
  void WriteToJasmin(std::ostream& out, bool is_new_array) const;

  /**
   * Write Jasmin representation to output stream.
   *
   * @param st The string to write.
   * @param length The length of the string.
   * @param out The output stream.
   */
  static void WriteQuotedToJasmin(const char* st, size_t length,
      std::ostream& out);

  /**
   * Write Jasmin representation to output stream.
   *
   * @param st The string to write.
   * @param out The output stream.
   */
  static void WriteQuotedToJasmin(const char* st, std::ostream& out);

 private:
  /**
   * Turn char into Unicode string.
   *
   * @param ch The input character.
   * @param out The output stream to which the Unicode string should be
   *            written.
   */
  static void GetUnicodeStringFromChar(wchar_t ch, std::ostream& out);

  const char* bytes_;
};


#endif /* CLASS_FILE_UTF8_INFO_H_ */

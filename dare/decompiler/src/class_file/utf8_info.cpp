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
 * Data for a Java string. Some of this is inspired from the Soot source code.
 */

#include "class_file/utf8_info.h"

#include <stdlib.h>
#include <string.h>

#include <locale>
#include <ostream>
#include <sstream>


void Utf8Info::WriteToJasmin(std::ostream& out) const {
  WriteQuotedToJasmin(bytes_, out);
}

/**
 * Write Jasmin representation to file. To be used for type descriptors.
 *
 * @param out The output stream.
 * @param is_new_array True if the descriptor represents an array type.
 */
void Utf8Info::WriteToJasmin(std::ostream& out, bool is_new_array) const {
  if (is_new_array) {
    if(strlen(bytes_) > 1 && bytes_[0] == '[' && bytes_[1] == 'L') {
      WriteQuotedToJasmin(bytes_ + 2, strlen(bytes_) - 3, out);
    } else {
      WriteQuotedToJasmin(bytes_ + 1, strlen(bytes_) - 1, out);
    }
  } else {
    if (bytes_[0] == '[') {
      WriteQuotedToJasmin(bytes_, strlen(bytes_), out);
    } else {
      WriteQuotedToJasmin(bytes_ + 1, strlen(bytes_) - 2, out);
    }
  }
}

/**
 * Write Jasmin representation to output stream (helper).
 *
 * @param st The string to write.
 * @param length The length of the string.
 * @param out The output stream.
 */
/*static*/ void Utf8Info::WriteQuotedToJasmin(const char* st, size_t length,
    std::ostream& out) {
  setlocale(LC_ALL, "");

  for (int i = 0; i < (int) length; ++i) {
    char ch = st[i];
    if (ch == '\\') {
      out << "\\\\";
    } else if (ch == '\'') {
      out << "\\\'";
    } else if (ch == '\"') {
      out << "\\\"";
    } else if (ch == '\n') {
      out << "\\n";
    } else if (ch == '\t') {
      out << "\\t";
    } else if (ch == '\r') {
      out << "\\r";
    } else if(ch == '\f') {
      out << "\\f";
    } else if(ch >= 32 && ch <= 126) {
      out << ch;
    } else {
      wchar_t wc;
      int width = mbtowc(&wc, st + i, MB_CUR_MAX);
      if (width < 0) {
        out << '\0';
        width = 1;
      } else {
        GetUnicodeStringFromChar(wc, out);
      }
      i = i + width - 1;
    }
  }
}

/**
 * Write Jasmin representation to output stream (helper).
 *
 * @param st The string to write.
 * @param out The output stream.
 */
/*static*/ void Utf8Info::WriteQuotedToJasmin(const char* st,
    std::ostream& out) {
  WriteQuotedToJasmin(st, strlen(st), out);
}

/**
 * Turn char into Unicode string.
 *
 * @param ch The input character.
 * @param out The output stream to which the Unicode string should be
 *            written.
 */
/*static*/ void Utf8Info::GetUnicodeStringFromChar(wchar_t ch,
    std::ostream& out) {
  std::string padding;
  std::stringstream s;
  s << std::hex << int(ch) << std::dec;

  switch(s.str().length()) {
    case 1:
      padding = "000";
      break;
    case 2:
      padding = "00";
      break;
    case 3:
      padding = "0";
      break;
    case 4:
      padding = "";
      break;
  }

  out << "\\u" << padding << s.str();
}

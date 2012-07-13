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
 * method_ref_info.cpp
 *
 * A Java method reference constant.
 */

#include "class_file/method_ref_info.h"

#include "class_file/utf8_info.h"
#include "tyde/java_opcode_jasmin.h"
#include "typing/type.h"


void MethodRefInfo::WriteToJasmin(std::ostream& out) const {
  out << " ";
  class_info_.this_name().WriteToJasmin(out, false);
  out << "/";
  Utf8Info::WriteQuotedToJasmin(nameandtype_.name().bytes(), out);
  out << nameandtype_.descriptor().bytes();
}


void MethodRefInfo::WriteStubToJasmin(std::ostream& out) const {
  out << ".method public ";
  if (is_static_) out << "static ";
  Utf8Info::WriteQuotedToJasmin(nameandtype_.name().bytes(), out);
  out << nameandtype_.descriptor().bytes() << '\n';
  out << ".limit locals " << arguments().size() * 2  + 1 << '\n'
      << ".limit stack 2\n";

  Type type = return_type();
  if (type.IsIntSubtype()) {
    out << ICONST_0 << '\n'
        << IRETURN << '\n';
  } else if (type.IsFloat()) {
    out << FCONST_0 << '\n'
        << FRETURN << '\n';
  } else if (type.IsDouble()) {
    out << DCONST_0 << '\n'
        << DRETURN << '\n';
  } else if (type.IsLong()) {
    out << LCONST_0 << '\n'
        << LRETURN << '\n';
  } else if (type.IsVoid()) {
    out << RETURN << '\n';
  } else {
    out << ACONST_NULL << '\n'
        << ARETURN << '\n';
  }

  out << ".end <method>\n";
}

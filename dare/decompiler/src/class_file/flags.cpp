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
 * flags.cpp
 *
 * Java flags. Some of this is from the Android source code.
 */

#include "class_file/flags.h"

#include <ostream>


/*static*/ const char* Flags::kAccessStrings[kAccessForMAX][kNumFlags] = {
  {
    /* class, inner class */
    "public ",           /* 0x0001 */
    "private ",          /* 0x0002 */
    "protected ",        /* 0x0004 */
    "static ",           /* 0x0008 */
    "final ",            /* 0x0010 */
    "? ",                /* 0x0020 */
    "? ",                /* 0x0040 */
    "? ",                /* 0x0080 */
    "? ",                /* 0x0100 */
    "interface ",        /* 0x0200 */
    "abstract ",         /* 0x0400 */
    "? ",                /* 0x0800 */
//    "synthetic ",        /* 0x1000 */
    "",
    "annotation ",       /* 0x2000 */
    "enum ",             /* 0x4000 */
    "? ",                /* 0x8000 */
//    "verified ",         /* 0x10000 */
    "",
//    "optimized ",        /* 0x20000 */
    "",
  },
  {
    /* method */
    "public ",           /* 0x0001 */
    "private ",          /* 0x0002 */
    "protected ",        /* 0x0004 */
    "static ",           /* 0x0008 */
    "final ",            /* 0x0010 */
    "synchronized ",     /* 0x0020 */
//    "bridge ",           /* 0x0040 */
    "",
//    "varargs ",          /* 0x0080 */
    "",
    "native ",           /* 0x0100 */
    "? ",                /* 0x0200 */
    "abstract ",         /* 0x0400 */
//    "strict ",           /* 0x0800 */
    "",
//    "synthetic ",        /* 0x1000 */
    "",
    "? ",                /* 0x2000 */
    "? ",                /* 0x4000 */
//    "miranda ",          /* 0x8000 */
    "",
//    "constructor ",      /* 0x10000 */
    "",
//    "declared_synchronized ", /* 0x20000 */
    "",
  },
  {
    /* field */
    "public ",           /* 0x0001 */
    "private ",          /* 0x0002 */
    "protected ",        /* 0x0004 */
    "static ",           /* 0x0008 */
    "final ",            /* 0x0010 */
    "? ",                /* 0x0020 */
    "volatile ",         /* 0x0040 */
    "transient ",        /* 0x0080 */
    "? ",                /* 0x0100 */
    "? ",                /* 0x0200 */
    "? ",                /* 0x0400 */
    "? ",                /* 0x0800 */
//    "synthetic ",        /* 0x1000 */
    "",
    "? ",                /* 0x2000 */
    "enum ",             /* 0x4000 */
    "? ",                /* 0x8000 */
    "? ",                /* 0x10000 */
    "? ",                /* 0x20000 */
  },
};

/*static*/ void Flags::FlagToStream(u4 flags, AccessFor forWhat,
    std::ostream& out) {
  for (int i = 0; i < kNumFlags; ++i) {
    if (flags & 0x01)
      out << kAccessStrings[forWhat][i];
    flags >>= 1;
  }
}

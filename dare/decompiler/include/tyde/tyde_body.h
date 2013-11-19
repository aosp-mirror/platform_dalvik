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
 * tyde_body.h
 *
 * A Tyde body and related structures.
 */

#ifndef TYDE_TYDE_BODY_H_
#define TYDE_TYDE_BODY_H_


#include "body.h"
#include "tyde/tyde_instruction.h"


class ClassInfo;
struct DexCode;
struct DexMethod;
struct DexFile;

/*
 * Structures for try/catch blocks.
 */
struct EncodedTypeAddrPair {
  TydeInstruction* ptr;
  ClassInfo* type_ptr;
};

struct EncodedCatchHandler {
  TydeInstruction* catch_all_ptr;
  std::vector<EncodedTypeAddrPair> handlers;
};

struct TryItem {
  TydeInstruction* start_ptr;
  TydeInstruction* end_ptr;
  EncodedCatchHandler handlers;
};

class TydeBody : public Body<TydeInstruction*> {
public:
  TydeBody() {}

  std::vector<TryItem>& tries() { return tries_; }
  const std::vector<TryItem>& tries() const { return tries_; }
  void set_tries(const std::vector<TryItem>& tries) { tries_ = tries; }

  void RefreshIndicesAfter(int index) const {
    for (int i = 0; i < (int) size(); ++i)
      operator[](i)->set_index(i);
  }

  void Dump(std::ostream& out) const {
    for (int i = 0; i < (int) size(); ++i)
      out << operator[](i)->ToString();
  }

 private:
  std::vector<TryItem> tries_;
};

#endif /* TYDE_TYDE_BODY_H_ */

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
 * tyde_instr_utils.h
 *
 * Utility things for Tyde instructions.
 */

#ifndef TYDE_TYDE_INSTR_UTILS_H_
#define TYDE_TYDE_INSTR_UTILS_H_


#include "libdex/DexOpcodes.h"


/**
 * Possible Tyde instruction formats.
 */
enum TydeInstructionFormat {
  kFmtTunk = 0,   // unknown format
  kFmtTuo,        // unambiguous operator
  kFmtTao,        // ambiguous operator
  kFmtTub,        // unambiguous branching
  kFmtTab,        // ambiguous branching
  kFmtTfna,       // filled-new-array
  kFmtTno,        // not operators
  kFmtTfad,       // fill-array-data
  kFmtTss,        // sparse-switch
  kFmtTps,        // packed-switch
  kFmtTtve,       // throw verification error
};

class TydeInstrUtils {
 public:
  /**
   * Get the Tyde format of a given opcode.
   *
   * @param opcode A Dalvik opcode.
   * @return A Tyde format.
   */
  static TydeInstructionFormat TydeGetFormatFromOpcode(Opcode opcode) {
    assert((u4) opcode < kNumPackedOpcodes);
    return (TydeInstructionFormat) tyde_format_table_[opcode];
  }
  /**
   * Get the Java opcode corresponding to a given Dalvik opcode.
   *
   * @param opcode A Dalvik opcode.
   * @return A Java opcode.
   */
  static const char* TydeGetJavaOpcodeFromOpcode(Opcode opcode) {
    assert((u4) opcode < kNumPackedOpcodes);
    return tyde_opcode_mapping_[opcode];
  }

 private:
  static u1 tyde_format_table_[kNumPackedOpcodes];
  static const char* tyde_opcode_mapping_[kNumPackedOpcodes];
};


#endif /* TYDE_TYDE_INSTR_UTILS_H_ */

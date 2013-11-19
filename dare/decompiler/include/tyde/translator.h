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
 * translator.h
 *
 * Tyde to Jasmin translator.
 */

#ifndef TYDE_TRANSLATOR_H_
#define TYDE_TRANSLATOR_H_


#include <iosfwd>
#include <vector>

#include "libdex/DexOpcodes.h"

#include "int_types.h"
#include "tyde/tyde_instr_utils.h"


class CodeAttribute;
class TydeInstruction;


/**
 * Converts Tyde to Java bytecode.
 */
class Translator {
 public:
  /**
   * Constructor
   *
   * Initialize some arrays with the size of the Dalvik bytecode, define the
   * mapping between Dalvik registers and Java local variables
   *
   * @param code_attribute Pointer to the Code Attribute to be generated.
   */
  Translator(CodeAttribute* code_attribute, std::ostream& out);

  /**
   * Perform the bytecode translation.
   */
  void Convert();


 private:
  /**
   * Translate unambiguous operator instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTuo(const TydeInstruction* ins);

  /**
   * Translate ambiguous operator instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTao(const TydeInstruction* ins);

  /**
   * Translate unambiguous branching instruction.
   *
   * @param ins A Tyde instruction.
   * @param large_method True if the method is large and therefore requires the
   *        use of goto_w instead of goto.
   */
  void TranslateFmtTub(const TydeInstruction* ins, bool large_method);

  /**
   * Translate ambiguous branching instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTab(const TydeInstruction* ins);

  /**
   * Translate filled-new-array instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTfna(const TydeInstruction* ins);

  /**
   * Translate not instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTno(const TydeInstruction* ins);

  /**
   * Translate fill-array-data instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTfad(const TydeInstruction* ins);

  /**
   * Translate a sparse-switch instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTss(const TydeInstruction* ins);

  /**
   * Translate a packed-switch instruction.
   *
   * @param ins A Tyde instruction.
   */
  void TranslateFmtTps(const TydeInstruction* ins);

  /**
   * Translate an ambiguous opcode and reference.
   *
   * @param ins A Tyde instruction.
   */
  void AddTaoOpcodeAndRef(const TydeInstruction* ins);

  /**
   * Check if an integer can be encoded with two bytes.
   *
   * @param value An integer.
   */
  bool CanInlineIntegerLiteral(s4 value) const;

  /**
   * Translate an ambiguous branching opcode.
   *
   * @param ins A Tyde instruction.
   */
  void AddTabOpcode(const TydeInstruction* ins);

  /**
   * Add an opcode to the current Java instruction.
   *
   * Uses the default mapping between Dalvik and Java opcodes. To be used when
   * it is not ambiguous
   *
   * @param op The Dalvik opcode to translate.
   */
  bool AddOpcode(Opcode op);

  /**
   * Add an iload_x or iload x structure to the current instruction
   *
   * @param index Index of the local variable to be loaded
   */
  void AddIload(int index);

  /**
   * Add an lload_x or lload x structure to the current instruction
   *
   * @param index Index of the first half of the local variable to be loaded
   */
  void AddLload(int index);

  /**
   * Add an fload_x or fload x structure to the current instruction
   *
   * @param index Index of the local variable to be loaded
   */
  void AddFload(int index);

  /**
   * Add a dload_x or dload x structure to the current instruction
   *
   * @param index Index of the first half of the local variable to be loaded
   */
  void AddDload(int index);

  /**
   * Add an aload_x or aload x structure to the current instruction
   *
   * @param index Index of the local variable to be loaded
   */
  void AddAload(int index);

  /**
   * Add an istore_x or istore x structure to the current instruction
   *
   * @param index Index of the local variable to be stored
   */
  void AddIstore(int index);

  /**
   * Add an lstore_x or lstore x structure to the current instruction
   *
   * @param index Index of the first half of the local variable to be stored
   */
  void AddLstore(int index);

  /**
   * Add an fstore_x or fstore x structure to the current instruction
   *
   * @param index Index of the local variable to be loaded
   */
  void AddFstore(int index);

  /**
   * Add a dstore_x or dstore x structure to the current instruction
   *
   * @param index Index of the first half of the local variable to be stored
   */
  void AddDstore(int index);

  /**
   * Add an astore_x or astore x structure to the current instruction
   *
   * @param index Index of the local variable to be stored
   */
  void AddAstore(int index);

  /**
   * Add an instruction to push an integer (up to 2 bytes) onto the stack
   *
   * @param value The value of the integer to be pushed onto the stack
   */
  void AddIconst(s2 value);

  /**
   * Translate the sources of an instruction
   *
   * For each source register, generate a Java load instruction
   *
   * @param ins The instruction to be translated
   */
  void TranslateSources(const TydeInstruction* ins);

  /**
   * Translate the destination of an instruction
   *
   * When there is a destination Dalvik register, generate a Java store
   * instruction
   */
  void TranslateDestination(const TydeInstruction* ins);

  /**
   * Translate an instruction with format 20bc
   *
   * @param ins The instruction to be translated
   */
  void TranslateFmtTtve(const TydeInstruction* ins);

  /**
   * Add a constant pool reference to the current instruction
   *
   * @param reference The constant pool reference to be loaded
   */
  void AddReference(const TydeInstruction* ins);

  /**
   * Pointer to the method whose body is being translated
   */
  CodeAttribute* code_attribute_;

  /**
   * The output stream.
   */
  std::ostream& out_;

  /**
   * Mapping between Dalvik registers and Java local variables.
   */
  std::vector<int> registers_;
};

#endif /* TYDE_TRANSLATOR_H_ */

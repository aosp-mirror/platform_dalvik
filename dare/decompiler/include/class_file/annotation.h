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
 * annotation.h
 *
 * The Annotation class parses annotations in a .dex file.
 */

#ifndef CLASS_FILE_ANNOTATION_H_
#define CLASS_FILE_ANNOTATION_H_


#include <vector>

#include "int_types.h"


class AttributeInfo;
class ConstantPool;
class ConstantPoolInfo;
class ClassFile;
struct DexClassDef;
struct DexFile;


class Annotation {
 public:
  /**
   * Process an annotation value. For now, we just want initial values of
   * static fields.
   *
   * @param cp The constant pool.
   * @param p_ptr Pointer to the annotation.
   * @param constants Array of new constants.
   */
  static void ProcessAnnotationValue(ConstantPool& cp,
      const u1** p_ptr, std::vector<ConstantPoolInfo*>& constants);

  /**
   * Parse class annotations.
   *
   * @param cf The class file for which we are parsing annotations.
   * @param dex_file The DexFile containing the class.
   * @param class_def The definition for the class for which we are parsing
   *        annotations.
   */
  static void ParseClassAnnotations(ClassFile* cf,
      const DexFile* dex_file, const DexClassDef* class_def);

  /**
   * Parse method annotations.
   *
   * @param cf The class file whose method annotations we want to parse.
   * @param dex_file The dex file containing the method annotations to be
   *        parsed.
   * @param class_def The definition of the class for which we want to parse
   *        method annotations.
   */
  static void ParseMethodAnnotations(ClassFile* cp,
      const DexFile* pDexFile, const DexClassDef* pClassDef);

 private:
  /**
   * Read a signed integer.
   *
   * @param ptr Pointer to the integer.
   * @param zwidth The Zero-based byte count.
   * @return The signed integer.
   */
  static s4 ReadSignedInt(const u1* ptr, int zwidth);

  /**
   * Read an unsigned integer.
   *
   * @param ptr Pointer to the integer.
   * @param zwidth Zero-based byte count.
   * @param fillOnRight True if we want to zero-fill from the right.
   * @return The unsigned integer.
   */
  static u4 ReadUnsignedInt(const u1* ptr, int zwidth, bool fillOnRight);

  /**
   * Read a signed long.
   *
   * @param ptr Pointer to the integer.
   * @param zwidth The Zero-based byte count.
   * @return The signed long.
   */
  static s8 ReadSignedLong(const u1* ptr, int zwidth);

  /**
   * Read an unsigned long.
   *
   * @param ptr Pointer to the integer.
   * @param zwidth Zero-based byte count.
   * @param fillOnRight True if we want to zero-fill from the right.
   * @return The unsigned long.
   */
  static u8 ReadUnsignedLong(const u1* ptr, int zwidth, bool fillOnRight);

  /**
   * Turn an annotation into a Java attribute.
   *
   * @param cp The constant pool.
   * @param dex_file The dex file being parsed.
   * @param data Pointer to the annotation being parsed.
   * @return An attribute describing the parsed annotation.
   */
  static AttributeInfo* DexDecodeAnnotation(ConstantPool& cp,
      const DexFile* pDexFile, const u1** data);
};


#endif /* CLASS_FILE_ANNOTATION_H_ */

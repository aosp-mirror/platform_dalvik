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
 * annotation.cpp
 *
 * The Annotation class parses annotations in a .dex file. Largely inspired
 * from the Android source.
 */

#include "class_file/annotation.h"

#include <string.h>

#include "libdex/DexFile.h"
#include "libdex/Leb128.h"

#include "class_file/class_file.h"
#include "class_file/constant_pool.h"
#include "class_file/deprecated_attribute.h"
#include "class_file/exceptions_attribute.h"
#include "class_file/signature_attribute.h"


/**
 * Process an annotation value. For now, we just want initial values of static
 * fields.
 *
 * @param cp The constant pool.
 * @param p_ptr Pointer to the annotation.
 * @param constants Array of new constants.
 */
/*static*/ void Annotation::ProcessAnnotationValue(ConstantPool& cp,
               const u1** p_ptr, std::vector<ConstantPoolInfo*>& constants) {
  ConstantPoolInfo* constant = NULL;
  const u1* ptr = *p_ptr;
  u1 valueType, valueArg;
  int width;

  valueType = *ptr++;
  valueArg = valueType >> kDexAnnotationValueArgShift;
  width = valueArg + 1;       /* assume, correct later */

  switch (valueType & kDexAnnotationValueTypeMask) {
    case kDexAnnotationByte:
    case kDexAnnotationShort:
    case kDexAnnotationChar:
    case kDexAnnotationInt:
      constant = (ConstantPoolInfo*) cp.AddIntCst(ReadSignedInt(ptr,
          valueArg));
      break;
    case kDexAnnotationLong:
      constant = (ConstantPoolInfo*) cp.AddLongCst(ReadSignedLong(ptr,
          valueArg));
      break;
    case kDexAnnotationFloat:
      constant = (ConstantPoolInfo*) cp.AddFloatCst((s4) ReadUnsignedInt(ptr,
          valueArg, true));
      break;
    case kDexAnnotationDouble:
      constant = (ConstantPoolInfo*) cp.AddDoubleCst((s8) ReadUnsignedLong(ptr,
          valueArg, true));
      break;
    case kDexAnnotationBoolean:
      constant = (ConstantPoolInfo*) cp.AddIntCst(valueArg);
      width = 0;
      break;
    case kDexAnnotationString:
      constant = (ConstantPoolInfo*) cp.AddStringCst(ReadUnsignedInt(ptr,
          valueArg, false));
      break;
    case kDexAnnotationType:
      // Assumes that the type is not primitive
      constant = (ConstantPoolInfo*) cp.AddClassCst(ReadUnsignedInt(ptr,
          valueArg, false));
      break;
    case kDexAnnotationMethod:
      break;
    case kDexAnnotationField:
      break;
    case kDexAnnotationEnum:
      break;
    case kDexAnnotationArray:
      /*
       * encoded_array format, which is a size followed by a stream
       * of annotation_value.
       */
    {
      int size = readUnsignedLeb128(&ptr);
      for (int i = 0; i < size; ++i) {
        ProcessAnnotationValue(cp, &ptr, constants);
      }
      width = 0;
    }
      break;
    case kDexAnnotationAnnotation:
      /* encoded_annotation format */
      width = 0;
      break;
    case kDexAnnotationNull:
      constant = NULL;
      constants.push_back(constant);
      width = 0;
      break;
    default:
      LOGE("Bad annotation element value byte 0x%02x (0x%02x)\n",
          valueType, valueType & kDexAnnotationValueTypeMask);
      assert(false);
  }

  ptr += width;

  *p_ptr = ptr;
  if (constant != NULL)
    constants.push_back(constant);
}

/**
 * Parse class annotations.
 *
 * @param cf The class file for which we are parsing annotations.
 * @param dex_file The DexFile constaining the class.
 * @param class_def The definition for the class for which we are parsing
 *        annotations.
 */
/*static*/ void Annotation::ParseClassAnnotations(ClassFile* cf,
               const DexFile* dex_file, const DexClassDef* class_def) {
  const DexAnnotationsDirectoryItem* directory_item =
      dexGetAnnotationsDirectoryItem(dex_file, class_def);
  if (directory_item == NULL)
    return;
  const DexAnnotationSetItem* annotation_set_item =
      dexGetClassAnnotationSet(dex_file, directory_item);
  if (annotation_set_item == NULL)
    return;
  u4 size = annotation_set_item->size;

  for (u4 j = 0; j < size; ++j) {
    const DexAnnotationItem* annotation_item = dexGetAnnotationItem(
        dex_file, annotation_set_item, j);
    const u1** data = (const u1**) &annotation_item;
    ++(*data);
    AttributeInfo* attribute_info = DexDecodeAnnotation(cf->cp(),
        dex_file, data);
    if (attribute_info != NULL)
      cf->add_attribute(attribute_info);
  }
}

/**
 * Parse method annotations.
 *
 * @param cf The class file whose method annotations we want to parse.
 * @param dex_file The dex file containing the method annotations to be parsed.
 * @param class_def The definition of the class for which we want to parse
 *        method annotations.
 */
/*static*/ void Annotation::ParseMethodAnnotations(ClassFile* cf,
               const DexFile* dex_file, const DexClassDef* class_def) {
  const DexAnnotationsDirectoryItem* directory_item =
      dexGetAnnotationsDirectoryItem(dex_file, class_def);
  if (directory_item == NULL)
    return;
  const DexMethodAnnotationsItem* method_annotations_item =
      dexGetMethodAnnotations(dex_file, directory_item);
  if (method_annotations_item == NULL)
    return;
  int method_annotations_size = dexGetMethodAnnotationsSize(
      dex_file, directory_item);
  for (int i = 0; i < method_annotations_size; ++i) {
    u4 method_idx = method_annotations_item[i].methodIdx;
    const DexAnnotationSetItem* annotation_set_item =
        dexGetMethodAnnotationSetItem(dex_file, &method_annotations_item[i]);
    u4 size = annotation_set_item->size;

    for (u4 j = 0; j < size; ++j) {
      const DexAnnotationItem* annotation_item = dexGetAnnotationItem(
          dex_file, annotation_set_item, j);
      const u1** data = (const u1**) &annotation_item;
      ++(*data);
      AttributeInfo* attribute_info = DexDecodeAnnotation(cf->cp(),
          dex_file, data);
      if (attribute_info != NULL)
        cf->AddAttributeToMethod(method_idx, attribute_info);
    }
  }
}

/**
 * Read a signed integer.
 *
 * @param ptr Pointer to the integer.
 * @param zwidth The Zero-based byte count.
 * @return The signed integer.
 */
/*static*/ s4 Annotation::ReadSignedInt(const u1* ptr, int zwidth) {
  s4 val = 0;

  for (int i = zwidth; i >= 0; --i)
    val = ((u4)val >> 8) | (((s4)*ptr++) << 24);
  val >>= (3 - zwidth) * 8;

  return val;
}

/**
 * Read an unsigned integer.
 *
 * @param ptr Pointer to the integer.
 * @param zwidth Zero-based byte count.
 * @param fillOnRight True if we want to zero-fill from the right.
 * @return The unsigned integer.
 */
/*static*/ u4 Annotation::ReadUnsignedInt(const u1* ptr, int zwidth,
    bool fillOnRight) {
  u4 val = 0;

  if (!fillOnRight) {
    for (int i = zwidth; i >= 0; --i)
      val = (val >> 8) | (((u4)*ptr++) << 24);
    val >>= (3 - zwidth) * 8;
  } else {
    for (int i = zwidth; i >= 0; --i)
      val = (val >> 8) | (((u4)*ptr++) << 24);
  }

  return val;
}

/**
 * Read a signed long.
 *
 * @param ptr Pointer to the integer.
 * @param zwidth The Zero-based byte count.
 * @return The signed long.
 */
/*static*/ s8 Annotation::ReadSignedLong(const u1* ptr, int zwidth) {
  s8 val = 0;

  for (int i = zwidth; i >= 0; --i)
    val = ((u8)val >> 8) | (((s8)*ptr++) << 56);
  val >>= (7 - zwidth) * 8;

  return val;
}

/**
 * Read an unsigned long.
 *
 * @param ptr Pointer to the integer.
 * @param zwidth Zero-based byte count.
 * @param fillOnRight True if we want to zero-fill from the right.
 * @return The unsigned long.
 */
/*static*/ u8 Annotation::ReadUnsignedLong(const u1* ptr, int zwidth,
    bool fillOnRight) {
  u8 val = 0;

  if (!fillOnRight) {
    for (int i = zwidth; i >= 0; --i)
      val = (val >> 8) | (((u8)*ptr++) << 56);
    val >>= (7 - zwidth) * 8;
  } else {
    for (int i = zwidth; i >= 0; --i)
      val = (val >> 8) | (((u8)*ptr++) << 56);
  }
  return val;
}

/**
 * Turn an annotation into a Java attribute.
 *
 * @param cp The constant pool.
 * @param dex_file The dex file being parsed.
 * @param data Pointer to the annotation being parsed.
 * @return An attribute describing the parsed annotation.
 */
/*static*/ AttributeInfo* Annotation::DexDecodeAnnotation(ConstantPool& cp,
    const DexFile* dex_file, const u1** data) {
  const char* type = dexStringByTypeIdx(dex_file, readUnsignedLeb128(data));
  int size = readUnsignedLeb128(data);

  if (strcmp(type, "Ldalvik/annotation/Throws;") == 0) {
    std::vector<ConstantPoolInfo*> constants;

    // Process each name/value pair
    for (int i = 0; i < size; ++i) {
      const char* name = dexStringById(dex_file, readUnsignedLeb128(data));
      ProcessAnnotationValue(cp, data, constants);
    }
    return new ExceptionsAttribute(constants);
  } else if (strcmp(type, "Ljava/lang/Deprecated;") == 0) {
    return new DeprecatedAttribute();
  } else if (strcmp(type, "Ldalvik/annotation/Signature;") == 0) {
    std::vector<ConstantPoolInfo*> constants;
    for (int i = 0; i < size; ++i) {
      const char* name = dexStringById(dex_file, readUnsignedLeb128(data));
      ProcessAnnotationValue(cp, data, constants);
    }
    return new SignatureAttribute(constants);
  }

  return NULL;
}

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
 * class_file.h
 *
 * ClassFile describes a class file.
 */

#ifndef CLASS_FILE_CLASS_FILE_H_
#define CLASS_FILE_CLASS_FILE_H_

#include <map>
#include <string>
#include <vector>

#include "class_file/constant_pool.h"


struct DecodedInstruction;
struct DexClassDef;
struct DexCode;
struct DexFile;
struct DexField;
struct DexMethod;
struct DexTypeItem;
class AttributeInfo;
class ClassInfo;
class FieldInfo;
class MethodInfo;
class Utf8Info;

/**
 * An enumeration of problems that can turn up during verification.
 */
enum VerifyError {
  VERIFY_ERROR_NONE = 0,      /* no error; must be zero */
  VERIFY_ERROR_GENERIC,       /* VerifyError */

  VERIFY_ERROR_NO_CLASS,      /* NoClassDefFoundError */
  VERIFY_ERROR_NO_FIELD,      /* NoSuchFieldError */
  VERIFY_ERROR_NO_METHOD,     /* NoSuchMethodError */
  VERIFY_ERROR_ACCESS_CLASS,  /* IllegalAccessError */
  VERIFY_ERROR_ACCESS_FIELD,  /* IllegalAccessError */
  VERIFY_ERROR_ACCESS_METHOD, /* IllegalAccessError */
  VERIFY_ERROR_CLASS_CHANGE,  /* IncompatibleClassChangeError */
  VERIFY_ERROR_INSTANTIATION, /* InstantiationError */
  VERIFY_ERROR_NULL_POINTER,  /* NullPointerException */
};

class ClassFile {
public:
  /**
   * Map a Dalvik code offset to a Dalvik verify error.
   */
  typedef std::map<int, VerifyError> VerifyErrorMap;


  /**
   * Constructor.
   *
   * @param dex_file The input dex file.
   * @param idx The class index.
   */
  ClassFile(const DexFile* dex_file);

  /**
   * Destructor.
   */
  ~ClassFile();


  /**
   * Get constant pool.
   *
   * @return The constant pool.
   */
  ConstantPool& cp() { return cp_; }

  /**
   * Get dex file.
   *
   * @return Pointer to the input dex file.
   */
  const DexFile* dex_file() const { return dex_file_; }

  /**
   * Generate a class.
   *
   * @param class_idx The class index.
   * @param output_dir The directory the class file should be written to.
   */
  void GenerateClass(int class_idx, const char* output_dir);

  /**
   * Add an attribute to the class.
   *
   * @param attribute The attribute to be added.
   */
  void add_attribute(AttributeInfo* attribute) {
    attributes_.push_back(attribute);
  }

  /**
   * Add an attribute to a method.
   *
   * @param method_idx Index of the method.
   * @param attribute Attribute to be added.
   */
  void AddAttributeToMethod(u4 method_idx, AttributeInfo* attribute);

  /**
   * Convert a slash class descriptor to a dot class descriptor. Returned
   * string should be freed after use.
   *
   * @param str The slash class descriptor.
   * @return The dot class descriptor.
   */
  static char* DescriptorClassToDot(const char* str);

  /**
   * Generate all class stubs. To be called after all classes have been
   * processed.
   *
   * @param directory Directory where the stubs should be output.
   * @param class_list A list of classes for which stubs should not be
   *        generated.
   */
  static void GenerateStubs(const std::string& directory) {
    ConstantPool::GenerateStubs(directory);
  }

  /**
   * Clear all static reference objects. Should be called once after all
   * classes have been processed and after all stubs have been generated.
   */
  static void ClearStaticRefs() {
    ConstantPool::ClearStaticRefs();
  }

  /**
   * Process the output of the Dalvik verifier for the current application.
   *
   * @param file_name The file name of the verifier output;
   */
  static void ProcessVerifierOutput(const char* file_name);

  /**
   * Get the set of verify errors for a given method.
   *
   * @param method A fully qualified method descriptor.
   * @return A set of verification errors.
   */
  static VerifyErrorMap* GetVerifyErrorsForMethod(
      const std::string& method);


private:
  typedef std::map<u4, MethodInfo*> MethodInfoMap;
  typedef std::map<std::string, VerifyErrorMap> ClassMethodErrorMap;

  /**
   * Parse a dex class.
   *
   * @param dex_file Pointer to the input dex file.
   * @param idx Class index.
   */
  void ParseClass(const DexFile* dex_file, int idx);

  /**
   * Parse a Dalvik static field and convert it to a Java static field.
   *
   * @param dex_file Pointer to the input dex file.
   * @param field Pointer to the dex field.
   * @return The new Java field object.
   */
  FieldInfo* ParseSField(const DexFile* dex_file, const DexField* field);

  /**
   * Parse a Dalvik instance field and convert it to a Java instance field.
   *
   * @param dex_file Pointer to the input dex file.
   * @param field Pointer to the dex field.
   * @return The new Java field object.
   */
  FieldInfo* ParseIField(const DexFile* dex_file, const DexField* field);

  /**
   * Parse a method.
   *
   * @param dex_file Pointer to the input dex file.
   * @param dex_method Pointer to the dex method.
   * @param class_descriptor The enclosing class descriptor.
   * @return The new Java method (without Java code).
   */
  MethodInfo* ParseMethod(const DexFile* dex_file,
      const DexMethod* pDexMethod, const char* classDescriptor);

  /**
   * Parse the code of a method.
   *
   * @param dex_file Pointer to the input dex file.
   * @param dex_method Pointer to the dex method.
   * @param method Pointer to the Java method.
   */
  void ParseCode(const DexFile* dex_file,
      const DexMethod* dex_method, MethodInfo* method);

  /**
   * Create the Java class reference corresponding to an interface implemented
   * by the current class.
   *
   * @param dex_file Pointer to the input dex file.
   * @param type_item Pointer to the interface type item.
   */
  ClassInfo* ParseInterface(const DexFile* dex_file,
      const DexTypeItem* type_item);

  /**
   * Parse initial static values.
   *
   * @param dex_file Pointer to the input dex file.
   * @param class_def Pointer to the current class definition.
   * @param fields_size Fields size.
   */
  void ParseStaticValues(const DexFile* dex_file, const DexClassDef* pClassDef,
      u4 fields_size);

  /**
   * Fetch 2 bytes.
   *
   * @param src Source pointer.
   * @return Fetched bytes.
   */
  u2 get2LE(unsigned char const* src) const {
    return src[0] | (src[1] << 8);
  }

  int nb_interfaces_implemented_;
  int nb_fields_defined_;
  ClassInfo** interfaces_implemented_;
  FieldInfo** fields_defined_;
  const DexFile* dex_file_;
  Utf8Info* source_;
  ClassInfo* this_class_;
  ClassInfo* superclass_;
  ConstantPool cp_;
  MethodInfoMap methods_defined_;
  std::vector<AttributeInfo*> attributes_;
  static ClassMethodErrorMap verifier_data_;
};


#endif /* CLASS_FILE_CLASS_FILE_H_ */


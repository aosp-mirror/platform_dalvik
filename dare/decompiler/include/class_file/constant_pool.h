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
 * constant_pool.h
 *
 * A Java constant pool.
 */

#ifndef CLASS_FILE_CONSTANT_POOL_H_
#define CLASS_FILE_CONSTANT_POOL_H_


#include <iosfwd>
#include <map>
#include <utility>
#include <vector>

#include <string.h>

#include "int_types.h"


class TydeInstruction;
class ClassFile;
class ClassInfo;
class DoubleInfo;
class FieldInfo;
class FloatInfo;
class FieldRefInfo;
class IntegerInfo;
class InterfaceMethodRefInfo;
class LongInfo;
class MethodInfo;
class MethodRefInfo;
class StringInfo;
class Stub;
class TydeBody;

class ConstantPool {
public:
  ~ConstantPool();

  ClassFile* cf() { return cf_; }
  void set_cf(ClassFile* cf) { cf_ = cf; }

  /**
   * Detect numerical constants in the Dalvik bytecode, and create
   * the appropriate data structures for the constant pool.
   *
   * The type inference has to have been done before calling this method.
   *
   * @param code Tyde code.
   */
  void GetNumericalConstantsUsed(const TydeBody& code);

  /**
   * Add new class constant in the Java constant pool if not already there.
   *
   * @param string_index Index of the class type in the dex constant pool.
   * @return Pointer to the newly inserted or to the existing class constant.
   */
  ClassInfo* AddClassCst(u4 string_index);

  /**
   * Add new string constant in the Java constant pool if not already there.
   *
   * @param string_index Index of the string in the dex constant pool.
   * @return Pointer to the newly inserted or to the existing string constant.
   */
  StringInfo* AddStringCst(u4 string_index);

  /**
   * Add new method reference constant in the Java constant pool if not already
   * there.
   *
   * @param method_index Index of the method reference in the dex constant
   *        pool.
   * @return Pointer to the newly inserted or to the existing method reference
   *         constant.
   */
  MethodRefInfo* AddMethodCst(u4 method_index, bool is_static = false);

  /**
   * Add new interface method reference constant in the Java constant pool if
   * not already there.
   *
   * @param method_index Index of the method in the dex constant pool.
   * @return Pointer to the newly inserted or to the existing interface method
   *         reference constant.
   */
  InterfaceMethodRefInfo* AddInterfaceMethodCst(u4 method_index);

  /**
   * Add new field reference constant in the Java constant pool if not already
   * there.
   *
   * @param field_index Index of the field reference in the dex constant pool.
   * @param is_static True if the field is a static one.
   * @return Pointer to the newly inserted or to the existing field reference.
   */
  FieldRefInfo* AddFieldCst(u4 field_index, bool is_static = false);

  /**
   * Add a new array if not already there.
   *
   * @param type_index Index of the type in the dex constant pool.
   * @return Pair whose first element is a pointer to a reference array (or
   *         NULL for a primitive array) and whose second element is a
   *         primitive code (or -1 for a reference array).
   */
  std::pair<ClassInfo*, int> AddArray(u4 type_index);

  /**
   * Add new int constant to the Java constant pool if not already there.
   *
   * @param value Integer value.
   * @return Pointer to the newly inserted or to the existing int constant.
   */
  IntegerInfo* AddIntCst(s4 value);

  /**
   * Add new float constant to the Java constant pool if not already there.
   *
   * @param value Float value.
   * @return Pointer to the newly inserted or to the existing float constant.
   */
  FloatInfo* AddFloatCst(s4 value);

  /**
   * Add new long constant to the Java constant pool if not already there.
   *
   * @param value Long value.
   * @return Pointer to the newly inserted or to the existing long constant.
   */
  LongInfo* AddLongCst(s8 value);

  /**
   * Add new double constant to the Java constant pool if not already there.
   *
   * @param value Double value.
   * @return Pointer to the newly inserted or to the existing double constant.
   */
  DoubleInfo* AddDoubleCst(s8 value);

  /**
   * Find a class stub by class name. Create a new class stub if non-existent.
   *
   * @param class_name The name of the class for which we want a stub.
   * @param is_necessary True if the stub is necessary for Java verification.
   * @param is_interface True if the class is an interface.
   * @param superclass The name of the superclass.
   * @param interfaces List of classes implemented by the class.
   * @return Pointer to the requested class stub.
   */
  static Stub* AddClassStub(const char* class_name, bool is_necessary,
      bool is_interface = false, const char* superclass = NULL,
      const std::vector<const char*>* interfaces = NULL);

  /**
   * Analyze a method argument descriptor and add a class stub if necessary.
   *
   * @param argument A method argument descriptor.
   */
  static void AddStubForType(const char* argument);

  /**
   * Generate all class stubs for the application. To be called after all
   * classes have been processed.
   *
   * @param directory Directory where the stubs should be output.
   * @param class_list A list of classes for which stubs should not be
   *        generated.
   */
  static void GenerateStubs(const std::string& directory);

  /**
   * Write a field stub to output stream in Jasmin format.
   *
   * @param index The field index.
   * @param out The output stream.
   */
  static void WriteFieldStubToJasmin(u4 index, std::ostream& out);

  /**
   * Write an interface method stub to output stream in Jasmin format.
   *
   * @param index The method index.
   * @param out The output stream.
   */
  static void WriteInterfaceMethodStubToJasmin(u4 index,
      std::ostream& out);

  /**
   * Write a method stub to output stream in Jasmin format.
   *
   * @param index The method index.
   * @param out The output stream.
   */
  static void WriteMethodStubToJasmin(u4 index, std::ostream& out);

  /**
   * Clear all static reference objects. To be called after all classes
   * have been processed and after all stubs have been generated.
   */
  static void ClearStaticRefs();


 private:
  struct CStringOp {
    bool operator() (const char* lhs, const char* rhs) const {
      return strcmp(lhs, rhs) < 0;
    }
  };

  typedef std::map<u4, ClassInfo*> ClassInfoMap;
  typedef std::map<u4, FieldRefInfo*> FieldRefInfoMap;
  typedef std::map<u4, InterfaceMethodRefInfo*> InterfaceMethodRefInfoMap;
  typedef std::map<u4, MethodRefInfo*> MethodRefInfoMap;
  typedef std::map<const char*, Stub*, CStringOp> StubMap;
  typedef std::map<u4, ClassInfo*> ClassArrayInfoMap;
  typedef std::map<u4, StringInfo*> StringInfoMap;
  typedef std::map<s4, IntegerInfo*> IntegerInfoMap;
  typedef std::map<s4, FloatInfo*> FloatInfoMap;
  typedef std::map<s8, LongInfo*> LongInfoMap;
  typedef std::map<s8, DoubleInfo*> DoubleInfoMap;

  /**
   * Determine if a string designates a type which is a 1D array of primitive
   * elements.
   *
   * @param type String descriptor for the array type.
   * @return A pair whose element is true if it is a primitive array and whose
   *         second element is the primitive code (or 0 for a reference array).
   */
  std::pair<bool, int> IsPrimitiveTypeArray(const char* type);

  /**
   * Add a new class array if not already there.
   *
   * @param type_index Index of the type in the dex constant pool.
   * @return Pointer to the newly created or existing constant.
   */
  ClassInfo* AddClassArrayCst(u4 type_index);

  /**
   * Add a field to a class stub. Create the required class stub if necessary.
   *
   * @param field_index The index of the field in the dex constant pool.
   * @param class_name The name of the class the fields belongs to.
   */
  static void AddFieldStub(u4 field_index, const char* class_name);

  /**
   * Add a method to an interface stub. Create the required interface stub if
   * necessary.
   *
   * @param method_index The index of the method in the dex constant pool.
   * @param class_name The name of the class the method belongs to.
   */
  static void AddInterfaceMethodStub(u4 field_index, const char* class_name);

  /**
   * Add a method to a class stub. Create the required class stub if necessary.
   *
   * @param method_index The index of the method in the dex constant pool.
   * @param class_name The name of the class the method belongs to.
   */
  static void AddMethodStub(u4 field_index, const char* class_name);


  /**
   * The class file this constant pool belongs to.
   */
  ClassFile* cf_;

  /**
   * Mappings between dex index/value and Java constant.
   */
  static FieldRefInfoMap fields_used_;
  static InterfaceMethodRefInfoMap interface_methods_used_;
  static MethodRefInfoMap methods_used_;
  static ClassInfoMap classes_instantiated_;
  static StubMap stubs_;

  ClassArrayInfoMap class_arrays_instantiated_;
  StringInfoMap strings_cst_used_;
  IntegerInfoMap integers_used_;
  FloatInfoMap floats_used_;
  LongInfoMap longs_used_;
  DoubleInfoMap doubles_used_;
};


#endif /* CLASS_FILE_CONSTANT_POOL_H_ */

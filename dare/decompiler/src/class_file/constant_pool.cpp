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
 * constant_pool.cpp
 *
 * Implementation of ConstantPool.
 */

#include "class_file/constant_pool.h"

#include <fstream>
#include <ostream>
#include <utility>
#include <vector>

#include "libdex/DexProto.h"

#include "class_file/class_file.h"
#include "class_file/class_info.h"
#include "class_file/double_info.h"
#include "class_file/field_ref_info.h"
#include "class_file/float_info.h"
#include "class_file/integer_info.h"
#include "class_file/interface_method_ref_info.h"
#include "class_file/long_info.h"
#include "class_file/method_ref_info.h"
#include "class_file/string_info.h"
#include "class_file/stub.h"
#include "tyde/tyde_body.h"
#include "tyde/tyde_instruction.h"

using std::vector;
using std::pair;
using std::make_pair;


/*static*/ ConstantPool::ClassInfoMap ConstantPool::classes_instantiated_;
/*static*/ ConstantPool::FieldRefInfoMap ConstantPool::fields_used_;
/*static*/ ConstantPool::InterfaceMethodRefInfoMap
    ConstantPool::interface_methods_used_;
/*static*/ ConstantPool::MethodRefInfoMap ConstantPool::methods_used_;
/*static*/ ConstantPool::StubMap ConstantPool::stubs_;


ConstantPool::~ConstantPool() {
  for (StringInfoMap::iterator it = strings_cst_used_.begin();
      it != strings_cst_used_.end(); ++it)
    delete it->second ;
  for (IntegerInfoMap::iterator it = integers_used_.begin();
      it != integers_used_.end(); ++it)
    delete it->second ;
  for (FloatInfoMap::iterator it = floats_used_.begin();
      it != floats_used_.end(); ++it)
    delete it->second ;
  for (LongInfoMap::iterator it = longs_used_.begin();
      it != longs_used_.end(); ++it)
    delete it->second ;
  for (DoubleInfoMap::iterator it = doubles_used_.begin();
      it != doubles_used_.end(); ++it)
    delete it->second ;
  for (ClassArrayInfoMap::iterator it = class_arrays_instantiated_.begin();
      it != class_arrays_instantiated_.end(); ++it)
    delete it->second;
}


/**
 * Detect doubles and longs in the Dalvik bytecode, and create
 * the appropriate data structures for the constant pool.
 *
 * The type inference has to have been done before calling this method.
 *
 * @param code Tyde code.
 */
void ConstantPool::GetNumericalConstantsUsed(const TydeBody& code) {
  for(u4 i = 0; i < code.size(); ++i) {
    if (code[i]->op() == OP_CONST) {
      if (code[i]->destination().type.IsFloat())
        code[i]->set_reference(AddFloatCst((s4) code[i]->constant()));
      else
        code[i]->set_reference(AddIntCst((s4) code[i]->constant()));
    } else if(code[i]->op() == OP_CONST_HIGH16) {
      s4 value = code[i]->constant() << 16;
      if(code[i]->destination().type.IsFloat())
        code[i]->set_reference(AddFloatCst(value));
      else
        code[i]->set_reference(AddIntCst(value));
    } else if ((code[i]->op() == OP_CONST_4
        || code[i]->op() == OP_CONST_16)
        && code[i]->destination().type.IsFloat()) {
      code[i]->set_reference(AddFloatCst((s4) code[i]->constant()));
    } else if(code[i]->op() == OP_CONST_WIDE) {
      if(code[i]->destination().type.IsDouble())
        code[i]->set_reference(AddDoubleCst(code[i]->wide_constant()));
      else
        code[i]->set_reference(AddLongCst(code[i]->wide_constant()));
    } else if(code[i]->op() == OP_CONST_WIDE_HIGH16) {
      s8 value = ((s8) code[i]->constant()) << 48;
      if(code[i]->destination().type.IsDouble())
        code[i]->set_reference(AddDoubleCst(value));
      else
        code[i]->set_reference(AddLongCst(value));
    } else if(code[i]->op() == OP_CONST_WIDE_16
        || code[i]->op() == OP_CONST_WIDE_32) {
      if(code[i]->destination().type.IsDouble())
        code[i]->set_reference(AddDoubleCst((s8) code[i]->constant()));
      else
        code[i]->set_reference(AddLongCst((s8) code[i]->constant()));
    } else if(code[i]->op() == OP_FILL_ARRAY_DATA) {
      Type type = code[i]->sources()[0].type;
      TydeInstruction* instruction = code[i];
      if (type.IsIntArray()) {
        for (u4 j = 0; j < instruction->data().size(); ++j)
          instruction->AddDataPtr(AddIntCst(instruction->data()[j]));
      } else if (type.IsFloatArray()) {
        for(u4 j = 0; j < instruction->data().size(); ++j)
          instruction->AddDataPtr(AddFloatCst(instruction->data()[j]));
      } else if (type.IsLongArray()) {
        for(u4 j = 0; j < instruction->data().size(); ++j)
          instruction->AddDataPtr(AddLongCst(instruction->data()[j]));
      } else if (type.IsDoubleArray()) {
        for(u4 j = 0; j < instruction->data().size(); ++j)
          instruction->AddDataPtr(AddDoubleCst(instruction->data()[j]));
      }
    }
  }
}

/**
 * Add new class constant in the Java constant pool if not already there.
 *
 * @param string_index Index of the class type in the dex constant pool.
 * @return Pointer to the newly inserted or to the existing class constant.
 */
ClassInfo* ConstantPool::AddClassCst(u4 type_index) {
  ClassInfoMap::iterator it = classes_instantiated_.find(type_index);
  if (it != classes_instantiated_.end()) {
    return it->second;
  } else {
    ClassInfo* class_info = new ClassInfo(dexStringByTypeIdx(cf_->dex_file(),
        type_index), false);
    classes_instantiated_[type_index] = class_info;
    const char* class_name = class_info->this_name().bytes();
    // Get rid of array part.
    int i = 0;
    while (class_name[i] == '[')
      ++i;
    // Only reference types. For example, avoid [B.
    if (class_name[i] == 'L')
      AddClassStub(class_name + i, false);
    return class_info;
  }
}

/**
 * Add new string constant in the Java constant pool if not already there.
 *
 * @param string_index Index of the string in the dex constant pool.
 * @return Pointer to the newly inserted or to the existing string constant.
 */
StringInfo* ConstantPool::AddStringCst(u4 string_index) {
  StringInfoMap::iterator it = strings_cst_used_.find(string_index);
  return ((it != strings_cst_used_.end()) ? it->second
      : strings_cst_used_[string_index] = new StringInfo(
          dexStringById(cf_->dex_file(), string_index)));
}

/**
 * Add new method reference constant in the Java constant pool if not already
 * there.
 *
 * @param method_index Index of the method reference in the dex constant
 *        pool.
 * @return Pointer to the newly inserted or to the existing method reference
 *         constant.
 */
MethodRefInfo* ConstantPool::AddMethodCst(u4 method_index,
    bool is_static /*=false*/) {
  MethodRefInfoMap::iterator it = methods_used_.find(method_index);

  if (it != methods_used_.end()) {
    AddMethodStub(method_index, it->second->class_name());
    return it->second;
  } else {
    const char* backDescriptor;
    const char* name;
    char* typeDescriptor = NULL;
    vector<Type> arguments;
    MethodRefInfo* method;
    const DexMethodId* pMethodId = dexGetMethodId(cf_->dex_file(),
        method_index);
    DexProto proto = { cf_->dex_file(), pMethodId->protoIdx };

    name = dexStringById(cf_->dex_file(), pMethodId->nameIdx);

    // This gets freed in the destructor for MethodRefInfo.
    typeDescriptor = dexCopyDescriptorFromMethodId(cf_->dex_file(), pMethodId);
    backDescriptor = dexStringByTypeIdx(cf_->dex_file(), pMethodId->classIdx);

    DexParameterIterator iterator;
    dexParameterIteratorInit(&iterator, &proto);

    const char* param = dexParameterIteratorNextDescriptor(&iterator);

    while(param != NULL) {
      arguments.push_back(Type::ParseType(param));
      ConstantPool::AddStubForType(param);
      param = dexParameterIteratorNextDescriptor(&iterator);
    }

    method = new MethodRefInfo(backDescriptor, name, typeDescriptor,
        is_static);
    method->set_arguments(arguments);
    method->set_return_type(Type::ParseType(dexProtoGetReturnType(&proto)));

    AddMethodStub(method_index, backDescriptor);
    return methods_used_[method_index] = method;
  }
}

/**
 * Add new interface method reference constant in the Java constant pool if
 * not already there.
 *
 * @param method_index Index of the method in the dex constant pool.
 * @return Pointer to the newly inserted or to the existing interface method
 *         reference constant.
 */
InterfaceMethodRefInfo* ConstantPool::AddInterfaceMethodCst(
    u4 method_index) {
  InterfaceMethodRefInfoMap::iterator it = interface_methods_used_.find(
      method_index);

  if (it != interface_methods_used_.end()) {
    AddInterfaceMethodStub(method_index, it->second->class_name());
    return it->second;
  } else {
    const char* backDescriptor;
    const char* name;
    char* typeDescriptor = NULL;
    vector<Type> arguments;
    InterfaceMethodRefInfo* method;
    const DexMethodId* pMethodId = dexGetMethodId(cf_->dex_file(),
        method_index);
    DexProto proto = { cf_->dex_file(), pMethodId->protoIdx };

    name = dexStringById(cf_->dex_file(), pMethodId->nameIdx);

    // This gets freed in the destructor for InterfaceMethodRefInfo
    typeDescriptor = dexCopyDescriptorFromMethodId(cf_->dex_file(), pMethodId);
    backDescriptor = dexStringByTypeIdx(cf_->dex_file(), pMethodId->classIdx);

    DexParameterIterator iterator;
    dexParameterIteratorInit(&iterator, &proto);

    const char* param = dexParameterIteratorNextDescriptor(&iterator);

    while(param != NULL) {
      arguments.push_back(Type::ParseType(param));
      ConstantPool::AddStubForType(param);
      param = dexParameterIteratorNextDescriptor(&iterator);
    }

    method = new InterfaceMethodRefInfo(backDescriptor, name, typeDescriptor);
    method->set_arguments(arguments);
    method->set_return_type(Type::ParseType(dexProtoGetReturnType(&proto)));

    AddInterfaceMethodStub(method_index, backDescriptor);
    return interface_methods_used_[method_index] = method;
  }
}

/**
 * Add new field reference constant in the Java constant pool if not already
 * there.
 *
 * @param field_index Index of the field reference in the dex constant pool.
 * @param is_static True if the field is a static one.
 * @return Pointer to the newly inserted or to the existing field reference.
 */
FieldRefInfo* ConstantPool::AddFieldCst(u4 field_index,
    bool is_static /*= false*/) {
  FieldRefInfoMap::iterator it = fields_used_.find(field_index);

  if (it != fields_used_.end()) {
    return it->second;
  } else {
    const DexFieldId* pFieldId = dexGetFieldId(cf_->dex_file(), field_index);
    const char* class_name = dexStringByTypeIdx(cf_->dex_file(),
        pFieldId->classIdx);
    AddFieldStub(field_index, class_name);
    AddStubForType(dexStringByTypeIdx(cf_->dex_file(), pFieldId->typeIdx));
    return fields_used_[field_index] = new FieldRefInfo(class_name,
        dexStringById(cf_->dex_file(), pFieldId->nameIdx),
        dexStringByTypeIdx(cf_->dex_file(), pFieldId->typeIdx), is_static);
  }
}

/**
 * Add a new array if not already there.
 *
 * @param type_index Index of the type in the dex constant pool.
 * @return Pair whose first element is a pointer to a reference array (or
 *         NULL for a primitive array) and whose second element is a
 *         primitive code (or -1 for a reference array).
 */
pair<ClassInfo*, int> ConstantPool::AddArray(u4 type_index) {
  const char* current_type_st = dexStringByTypeIdx(cf_->dex_file(), type_index);
  pair<bool, int> prim_type = IsPrimitiveTypeArray(current_type_st);

  if(prim_type.first == false) {
    return make_pair(AddClassArrayCst(type_index), -1);
  } else {
    return make_pair((ClassInfo*) NULL, prim_type.second);
  }
}

/**
 * Add new int constant to the Java constant pool if not already there.
 *
 * @param value Integer value.
 * @return Pointer to the newly inserted or to the existing int constant.
 */
IntegerInfo* ConstantPool::AddIntCst(s4 value) {
  IntegerInfoMap::iterator it = integers_used_.find(value);
  return ((it != integers_used_.end()) ? it->second
      : integers_used_[value] = new IntegerInfo(value));
}

/**
 * Add new float constant to the Java constant pool if not already there.
 *
 * @param value Float value.
 * @return Pointer to the newly inserted or to the existing float constant.
 */
FloatInfo* ConstantPool::AddFloatCst(s4 value) {
  FloatInfoMap::iterator it = floats_used_.find(value);
  return ((it != floats_used_.end()) ? it->second
      : floats_used_[value] = new FloatInfo(value));
}

/**
 * Add new long constant to the Java constant pool if not already there.
 *
 * @param value Long value.
 * @return Pointer to the newly inserted or to the existing long constant.
 */
LongInfo* ConstantPool::AddLongCst(s8 value) {
  LongInfoMap::iterator it = longs_used_.find(value);
  return ((it != longs_used_.end()) ? it->second
      : longs_used_[value] = new LongInfo(value));
}

/**
 * Add new double constant to the Java constant pool if not already there.
 *
 * @param value Double value.
 * @return Pointer to the newly inserted or to the existing double constant.
 */
DoubleInfo* ConstantPool::AddDoubleCst(s8 value) {
  DoubleInfoMap::iterator it = doubles_used_.find(value);
  return ((it != doubles_used_.end()) ? it->second
      : doubles_used_[value] = new DoubleInfo(value));
}

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
/*static*/ Stub* ConstantPool::AddClassStub(const char* class_name,
    bool is_necessary, bool is_interface /*=false*/,
    const char* superclass /*= NULL*/,
    const std::vector<const char*>* interfaces /*= NULL*/) {
  StubMap::iterator it = stubs_.find(class_name);

  if (it != stubs_.end()) {
    Stub* stub = it->second;
    if (is_interface)
      stub->set_is_interface(true);
    if (superclass != NULL)
      stub->set_superclass(superclass);
    if (interfaces != NULL)
      stub->set_interfaces(*interfaces);
    if (is_necessary)
      stub->set_is_necessary(true);
    return stub;
  } else {
    Stub* stub = new Stub(class_name, is_interface, is_necessary, superclass);
    if (interfaces != NULL)
      stub->set_interfaces(*interfaces);
    stubs_.insert(make_pair(class_name, stub));
    return stub;
  }
}

/**
 * Analyze a type descriptor and add a class stub if necessary.
 *
 * @param argument A class descriptor.
 */
/*static*/ void ConstantPool::AddStubForType(const char* argument) {
  int i = 0;
  // Move past array dimensions (if there is any).
  while (argument[i] == '[')
    ++i;
  // Add class stub if necessary.
  if (argument[i] == 'L')
    AddClassStub(argument + i, true);
}

/**
 * Generate all class stubs for the application and clean up. To be called
 * after all classes have been processed.
 *
 * @param directory Directory where the stubs should be output.
 * @param class_list A list of classes for which stubs should not be
 *        generated.
 */
/*static*/ void ConstantPool::GenerateStubs(const std::string& directory) {
  AddClassStub("Ljava/lang/Object;", false);
  std::ofstream stub_list((directory + "/stubs.txt").c_str(),
      std::ios::app);
  for (StubMap::iterator it = stubs_.begin(); it != stubs_.end(); ++it) {
    if (it->second->is_necessary()) {
      char* stub_name_dots = it->second->WriteToJasmin(directory);
      stub_list << stub_name_dots << '\n';
      free(stub_name_dots);
    }
  }
  stub_list.close();
}

/**
 * Write a field stub to output stream in Jasmin format.
 *
 * @param index The field index.
 * @param out The output stream.
 */
/*static*/ void ConstantPool::WriteFieldStubToJasmin(u4 index,
    std::ostream& out) {
  FieldRefInfoMap::iterator it = fields_used_.find(index);
  if (it == fields_used_.end()) return;

  it->second->WriteStubToJasmin(out);
}

/**
 * Write an interface method stub to output stream in Jasmin format.
 *
 * @param index The field index.
 * @param out The output stream.
 */
/*static*/ void ConstantPool::WriteInterfaceMethodStubToJasmin(u4 index,
    std::ostream& out) {
  InterfaceMethodRefInfoMap::iterator it = interface_methods_used_.find(index);
  if (it == interface_methods_used_.end()) return;

  it->second->WriteStubToJasmin(out);
}

/**
 * Write a method stub to output stream in Jasmin format.
 *
 * @param index The field index.
 * @param out The output stream.
 */
/*static*/ void ConstantPool::WriteMethodStubToJasmin(u4 index,
    std::ostream& out) {
  MethodRefInfoMap::iterator it = methods_used_.find(index);
  if (it == methods_used_.end()) return;

  it->second->WriteStubToJasmin(out);
}

/**
 * Clear all static reference objects. To be called after after all classes
 * have been processed and after all stubs have been generated.
 */
/*static*/ void ConstantPool::ClearStaticRefs() {
  for (ClassInfoMap::iterator it = classes_instantiated_.begin();
      it != classes_instantiated_.end(); ++it)
    delete it->second;
  for (FieldRefInfoMap::iterator it = fields_used_.begin();
      it != fields_used_.end(); ++it)
    delete it->second;
  for (InterfaceMethodRefInfoMap::iterator it = interface_methods_used_.begin();
      it != interface_methods_used_.end(); ++it)
    delete it->second ;
  for (MethodRefInfoMap::iterator it = methods_used_.begin();
      it != methods_used_.end(); ++it)
    delete it->second ;
  for (StubMap::iterator it = stubs_.begin(); it != stubs_.end(); ++it)
    delete it->second;
}

/**
 * Determine if a string designates a type which is a 1D array of primitive
 * elements.
 *
 * @param type String descriptor for the array type.
 * @return A pair whose element is true if it is a primitive array and whose
 *         second element is the primitive code (or 0 for a reference array).
 */
pair<bool, int> ConstantPool::IsPrimitiveTypeArray(const char* type) {
  if(type[0] == '[' && type[1] == 'Z')
    return make_pair(true, kBoolean);

  if(type[0] == '[' && type[1] == 'B')
    return make_pair(true, kByte);

  if(type[0] == '[' && type[1] == 'S')
    return make_pair(true, kShort);

  if(type[0] == '[' && type[1] == 'C')
    return make_pair(true, kChar);

  if(type[0] == '[' && type[1] == 'I')
    return make_pair(true, kInt);

  if(type[0] == '[' && type[1] == 'J')
    return make_pair(true, kLong);

  if(type[0] == '[' && type[1] == 'F')
    return make_pair(true, kFloat);

  if(type[0] == '[' && type[1] == 'D')
    return make_pair(true, kDouble);

  return make_pair(false, 0);
}

/**
 * Add a new class array if not already there.
 *
 * @param type_index Index of the type in the dex constant pool.
 * @return Pointer to the newly created or existing constant.
 */
ClassInfo* ConstantPool::AddClassArrayCst(u4 type_index) {
  ClassArrayInfoMap::iterator it = class_arrays_instantiated_.find(type_index);

  const char* class_name = dexStringByTypeIdx(cf_->dex_file(), type_index);
  int i = 0;
  while (class_name[i] == '[')
    ++i;
  AddClassStub(class_name + i, false);

  if (it != class_arrays_instantiated_.end()) {
    return it->second;
  } else {
    return class_arrays_instantiated_[type_index] = new ClassInfo(
        dexStringByTypeIdx(cf_->dex_file(), type_index), true);
  }
}

/**
 * Add a field to a class stub. Create the required class stub if necessary.
 *
 * @param field_index The index of the field in the dex constant pool.
 * @param class_name The name of the class the fields belongs to.
 */
/*static*/ void ConstantPool::AddFieldStub(u4 field_index,
    const char* class_name) {
  if (class_name[0] != '[')
    AddClassStub(class_name, false)->AddFieldRef(field_index);
}

/**
 * Add a method to an interface stub. Create the required interface stub if
 * necessary.
 *
 * @param method_index The index of the method in the dex constant pool.
 * @param class_name The name of the class the method belongs to.
 */
/*static*/ void ConstantPool::AddInterfaceMethodStub(u4 method_index,
    const char* class_name) {
  if (class_name[0] != '[')
    AddClassStub(class_name, false)->AddInterfaceMethodRef(method_index);
}

/**
 * Add a method to a class stub. Create the required class stub if necessary.
 *
 * @param method_index The index of the method in the dex constant pool.
 * @param class_name The name of the class the method belongs to.
 */
/*static*/ void ConstantPool::AddMethodStub(u4 method_index,
    const char* class_name) {
  if (class_name[0] != '[')
    AddClassStub(class_name, false)->AddMethodRef(method_index);
}

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
 * class_file.cpp
 * Author: Damien Octeau
 *
 * Create a new class file. Part of the code follows the dexdump code.
 */

#include "class_file/class_file.h"

#include <string.h>

#include <fstream>
#include <string>
#include <utility>

#include "libdex/DexFile.h"
#include "libdex/DexClass.h"
#include "libdex/DexProto.h"
#include "libdex/InstrUtils.h"

#include "class_file/annotation.h"
#include "class_file/class_info.h"
#include "class_file/code_attribute.h"
#include "class_file/method_info.h"
#include "class_file/field_info.h"
#include "class_file/synthetic_attribute.h"
#include "timer.h"
#include "tyde/tyde_instruction.h"
#include "typing/type.h"

using std::ofstream;
using std::string;
using std::vector;


/*static*/ ClassFile::ClassMethodErrorMap ClassFile::verifier_data_;


namespace {
bool output_verifier_info = false;
bool output_tyde = false;
}


/**
 * Constructor.
 *
 * @param dex_file The input dex file.
 * @param idx The class index.
 */
ClassFile::ClassFile(const DexFile* dex_file)
    : nb_interfaces_implemented_(0),
      nb_fields_defined_(0),
      interfaces_implemented_(NULL),
      fields_defined_(NULL),
      dex_file_(dex_file),
      source_(NULL),
      this_class_(NULL),
      superclass_(NULL) {
  cp_.set_cf(this);
}

/**
 * Destructor.
 */
ClassFile::~ClassFile() {
  delete source_;
  delete this_class_;
  delete superclass_;

  for (int i = 0; i < (int) attributes_.size(); ++i)
    delete attributes_[i];
  for(int i = 0; i < nb_interfaces_implemented_; ++i)
    delete interfaces_implemented_[i];
  for(int i = 0; i < nb_fields_defined_; ++i)
    delete(fields_defined_[i]);
  for (MethodInfoMap::iterator it = methods_defined_.begin();
      it != methods_defined_.end(); ++it)
    delete it->second;

  delete[] interfaces_implemented_;
  delete[] fields_defined_;
}


/**
 * Generate a class.
 *
 * @param class_idx The class index.
 * @param output_dir The directory the class file should be written to.
 */
void ClassFile::GenerateClass(int class_idx, const char* output_dir) {
  const DexClassDef* class_def = dexGetClassDef(dex_file_, class_idx);

  const char* class_descriptor = dexStringByTypeIdx(dex_file_,
      class_def->classIdx);

  printf("Processing class #%i: %s\n", class_idx, class_descriptor);

  ClassFile::ClassMethodErrorMap::iterator it = verifier_data_.find(
      class_descriptor);

  if (it != verifier_data_.end() && it->second.find(-1) != it->second.end()) {
    LOGW("Class %s is unavailable - probably because its superclass is "
        "unavailable\n", class_descriptor);

    std::vector<const char*> interfaces_ci;
    const DexTypeList* interfaces = dexGetInterfacesList(dex_file_, class_def);
    if (interfaces != NULL) {
      for (int i = 0; i < (int) interfaces->size; ++i) {
        const char* interface_name = dexStringByTypeIdx(dex_file_,
            dexGetTypeItem(interfaces, i)->typeIdx);
        ConstantPool::AddClassStub(interface_name, true, true);
        interfaces_ci.push_back(interface_name);
      }
    }
    const char* superclass = NULL;
    if (class_def->superclassIdx != kDexNoIndex) {
      superclass = dexStringByTypeIdx(dex_file_,
          class_def->superclassIdx);
      ConstantPool::AddClassStub(superclass, true,
          class_def->accessFlags & ACC_INTERFACE);
    }

    ConstantPool::AddClassStub(class_descriptor, true,
        class_def->accessFlags & ACC_INTERFACE,
        superclass,
        &interfaces_ci);
    Timer::getInstance().End();
    Timer::getInstance().Start(Timer::kParsing);
    return;
  }

  // This will, among other things, record most of the constants
  // needed for the new .class constant pool.
  ParseClass(dex_file_, class_idx);

  for (MethodInfoMap::iterator it = methods_defined_.begin();
      it != methods_defined_.end(); ++it)
    it->second->Tydify(cp_);


  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kOutputToFile);

  string out_dir(output_dir);
  char* class_name_dup =
      DescriptorClassToDot(this_class_->this_name().bytes());
  ofstream out((out_dir + "/" + class_name_dup + ".jasmin").c_str());

  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kTranslationToJasmin);

  if (source_ != NULL) {
    out << ".source ";
    source_->WriteToJasmin(out);
    out << '\n';
  }

  if ((class_def->accessFlags & ACC_INTERFACE) != 0)
    out << ".interface ";
  else
    out << ".class ";
  Flags::FlagToStream(class_def->accessFlags, kAccessForClass, out);
  out << " ";
  this_class_->this_name().WriteToJasmin(out, false);
  out << '\n';
  if (superclass_ != NULL) {
    out << ".super ";
    superclass_->this_name().WriteToJasmin(out, false);
  } else {
    out << ".no_super";
  }
  out <<'\n';

  for (int i = 0; i < nb_interfaces_implemented_; ++i) {
    out << ".implements ";
    interfaces_implemented_[i]->WriteToJasmin(out);
    out << '\n';
  }

  for (int i = 0; i < (int) attributes_.size(); ++i)
    attributes_[i]->WriteToJasmin(out);

  for (int i = 0; i < nb_fields_defined_; ++i)
    fields_defined_[i]->WriteToJasmin(out);

  for (MethodInfoMap::iterator it = methods_defined_.begin();
      it != methods_defined_.end(); ++it)
    it->second->WriteToJasmin(out);


  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kOutputToFile);

  ofstream class_list((out_dir + "classes.txt").c_str(), std::ios::app);
  class_list << class_name_dup << '\n';
  class_list.close();
  out << std::endl;
  out.close();

  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kParsing);

  if (output_tyde) {
    ofstream tyde_file((out_dir + "/" + class_name_dup + ".tyde").c_str());
    for (MethodInfoMap::iterator it = methods_defined_.begin();
        it != methods_defined_.end(); ++it)
      it->second->Dump(tyde_file);
    tyde_file.close();
  }

  free(class_name_dup);
}

/**
 * Add an attribute to a method.
 *
 * @param method_idx Index of the method.
 * @param attribute Attribute to be added.
 */
void ClassFile::AddAttributeToMethod(u4 method_idx, AttributeInfo* attribute) {
  methods_defined_[method_idx]->AddAttribute(attribute);
}

/**
* Convert a slash class descriptor to a dot class descriptor. Returned string
* should be freed after use.
*
* @param str The slash class descriptor.
* @return The dot class descriptor.
*/
/*static*/ char* ClassFile::DescriptorClassToDot(const char* str) {
  const char* lastSlash = str + 1;  // start past 'L'
  char* newStr = strdup(lastSlash);
  newStr[strlen(lastSlash)-1] = '\0';

  for (char* cp = newStr; *cp != '\0'; cp++)
    if (*cp == '/')
      *cp = '.';

  return newStr;
}

/**
 * Process the output of the Dalvik verifier for the current application.
 *
 * @param file_name The file name of the verifier output;
 */
/*static*/ void ClassFile::ProcessVerifierOutput(const char* file_name) {
  std::ifstream in(file_name);

  while (in) {
    string method_descriptor, temp;
    // First part: class descriptor.
    in >> method_descriptor;
    if (method_descriptor == "")
      break;
    // Second part: method name.
    in >> temp;
    if (temp == "<<") {
      verifier_data_[method_descriptor].insert(
          std::pair<int, VerifyError>(-1, VERIFY_ERROR_GENERIC));
    } else {
      method_descriptor += temp;
      // Third part: method signature. This fully qualified name is unique.
      in >> temp;
      method_descriptor += temp;

      string complete_failure;
      in >> complete_failure;

      if (complete_failure == "y") {
        verifier_data_[method_descriptor].insert(
            std::pair<int, VerifyError>(-1, VERIFY_ERROR_GENERIC));
      } else {
        int i;
        int j;
        in >> std::hex >> i;
        in >> j;
        std::pair<int, VerifyError> p(i, (VerifyError) j);
        verifier_data_[method_descriptor].insert(p);
      }
    }
  }

  in.close();

  if (output_verifier_info) {
    LOGI("***** Verifier Information *****\n");
    for (ClassMethodErrorMap::iterator it = verifier_data_.begin();
        it != verifier_data_.end(); ++it) {
      for (VerifyErrorMap::iterator it2 = it->second.begin();
          it2 != it->second.end(); ++it2) {
        LOGI("%s %i %i\n", it->first.c_str(), it2->first, it2->second);
      }
    }
    LOGI("\n");
  }
}

/**
 * Get the set of verify errors for a given method.
 *
 * @param method A fully qualified method descriptor.
 * @return A set of verification errors.
 */
/*static*/ ClassFile::VerifyErrorMap* ClassFile::GetVerifyErrorsForMethod(
    const std::string& method) {
  ClassMethodErrorMap::iterator it = verifier_data_.find(method);
  if (it == verifier_data_.end()) {
    return NULL;
  } else {
    return &(it->second);
  }
}

/**
 * Parse a dex class.
 *
 * Parse the class descriptor, the superclass, the implemented interfaces, the
 * fields, the methods (including the method code). The actual code
 * translation is done later.
 *
 * @param dex_file Pointer to the dex file.
 * @param idx Index of the class.
 */
void ClassFile::ParseClass(const DexFile* dex_file, int idx) {
  const DexTypeList* interfaces;
  const DexClassDef* class_def;
  DexClassData* class_data = NULL;
  const u1* encoded_data;
  const char* class_descriptor;
  const char* superclass_descriptor;
  const char* source_string;
  int i;

  class_def = dexGetClassDef(dex_file, idx);
  encoded_data = dexGetClassData(dex_file, class_def);
  class_data = dexReadAndVerifyClassData(&encoded_data, NULL);

  if (class_data == NULL) {
    LOGE("Trouble reading class data (#%d)\n", idx);
    goto bail;
  }

  source_string = dexGetSourceFile(dex_file, class_def);
  if (source_string != NULL)
    source_ = new Utf8Info(source_string);

  class_descriptor = dexStringByTypeIdx(dex_file, class_def->classIdx);
  this_class_ = new ClassInfo(class_descriptor, false);

  if (!(class_descriptor[0] == 'L' &&
      class_descriptor[strlen(class_descriptor)-1] == ';'))
    /* arrays and primitives should not be defined explicitly */
    LOGW("Malformed class name '%s'\n", class_descriptor);

  if (class_def->superclassIdx != kDexNoIndex) {
    superclass_descriptor =
      dexStringByTypeIdx(dex_file, class_def->superclassIdx);
    superclass_ = new ClassInfo(superclass_descriptor, false);
  }

  interfaces = dexGetInterfacesList(dex_file, class_def);
  if (interfaces != NULL) {
    nb_interfaces_implemented_ = interfaces->size;
    interfaces_implemented_ = new ClassInfo*[nb_interfaces_implemented_];
    for (i = 0; i < (int) nb_interfaces_implemented_; ++i)
      interfaces_implemented_[i] = ParseInterface(dex_file,
          dexGetTypeItem(interfaces, i));
  } else {
    nb_interfaces_implemented_ = 0;
    interfaces_implemented_ = NULL;
  }

  if ((class_def->accessFlags & ACC_SYNTHETIC) == ACC_SYNTHETIC)
    add_attribute(new SyntheticAttribute());

  nb_fields_defined_ = class_data->header.staticFieldsSize
      + class_data->header.instanceFieldsSize;
  fields_defined_ = new FieldInfo*[nb_fields_defined_];
  for (i = 0; i < (int) class_data->header.staticFieldsSize; ++i)
    fields_defined_[i] = ParseSField(dex_file, &class_data->staticFields[i]);
  ParseStaticValues(dex_file, class_def, class_data->header.staticFieldsSize);
  for (i = 0; i < (int) class_data->header.instanceFieldsSize; ++i)
    fields_defined_[class_data->header.staticFieldsSize + i] =
        ParseIField(dex_file, &class_data->instanceFields[i]);

  for (i = 0; i < (int) class_data->header.directMethodsSize; ++i)
    methods_defined_[class_data->directMethods[i].methodIdx] =
        ParseMethod(dex_file, &class_data->directMethods[i],
            class_descriptor);
  for (i = 0; i < (int) class_data->header.virtualMethodsSize; ++i)
    methods_defined_[class_data->virtualMethods[i].methodIdx] =
        ParseMethod(dex_file, &class_data->virtualMethods[i],
            class_descriptor);
  Annotation::ParseClassAnnotations(this, dex_file, class_def);
  Annotation::ParseMethodAnnotations(this, dex_file, class_def);

bail:
  free(class_data);
}

/**
* Parse a Dalvik static field and convert it to a Java static field.
*
* @param dex_file Pointer to the input dex file.
* @param field Pointer to the dex field.
* @return The new Java field object.
*/
FieldInfo* ClassFile::ParseSField(const DexFile* dex_file,
    const DexField* field) {
  const DexFieldId* field_id = dexGetFieldId(dex_file, field->fieldIdx);
  const char* name = dexStringById(dex_file, field_id->nameIdx);
  const char* type_descriptor = dexStringByTypeIdx(dex_file, field_id->typeIdx);

  return new FieldInfo(field->accessFlags, name, type_descriptor);
}

/**
* Parse a Dalvik instance field and convert it to a Java instance field.
*
* @param dex_file Pointer to the input dex file.
* @param field Pointer to the dex field.
* @return The new Java field object.
*/
FieldInfo* ClassFile::ParseIField(const DexFile* dex_file,
    const DexField* field) {
  return ParseSField(dex_file, field);
}

/**
 * Parse a method.
 *
 * Parse name, arguments, return value and code.
 *
 * @param dex_file Pointer to the input dex file.
 * @param dex_method Pointer to the dex method.
 * @param class_descriptor The enclosing class descriptor.
 * @return The new Java method (without Java code).
 */
MethodInfo* ClassFile::ParseMethod(const DexFile* dex_file,
    const DexMethod* dex_method, const char* class_descriptor) {
  const char* name;
  char* typeDescriptor = NULL;
  vector<Type> arguments;
  MethodInfo* method;

  // Parse name and descriptor
  const DexMethodId *pMethodId = dexGetMethodId(dex_file, dex_method->methodIdx);
  DexProto proto = { dex_file, pMethodId->protoIdx };

  name = dexStringById(dex_file, pMethodId->nameIdx);
  // This gets freed later in the Method_info destructor
  typeDescriptor = dexCopyDescriptorFromMethodId(dex_file, pMethodId);

  DexParameterIterator iterator;
  dexParameterIteratorInit(&iterator, &proto);

  const char* param = dexParameterIteratorNextDescriptor(&iterator);

  while(param != NULL) {
    arguments.push_back(Type::ParseType(param));
    ConstantPool::AddStubForType(param);
    param = dexParameterIteratorNextDescriptor(&iterator);
  }

  // Create the new Java method, using the name and descriptor
  if (dex_method->codeOff == 0) {
    method = new MethodInfo(dex_method->accessFlags, name, typeDescriptor,
        class_descriptor);
    method->set_arguments(arguments);
    method->set_return_type(Type::ParseType(dexProtoGetReturnType(&proto)));
  } else {
    method = new MethodInfo(dex_method->accessFlags, name, typeDescriptor,
        dex_file, dex_method, class_descriptor);
    method->set_arguments(arguments);
    method->set_return_type(Type::ParseType(dexProtoGetReturnType(&proto)));
    ParseCode(dex_file, dex_method, method);
  }

  return method;
}

/**
 * Parse the code of a method.
 *
 * @param dex_file Pointer to the input dex file.
 * @param dex_method Pointer to the dex method.
 * @param method Pointer to the Java method.
 */
void ClassFile::ParseCode(const DexFile* dex_file,
    const DexMethod* dex_method, MethodInfo * method) {
  const DexCode* dex_code = dexGetCode(dex_file, dex_method);
  assert(dex_code->insnsSize > 0);
  const u2* insns = dex_code->insns;
  int insn_idx = 0;
  bool just_rewrote = false;

  // Dummy instruction for when a try block starts at the first instruction.
  TydeInstruction* augIns0 = new TydeInstruction(0, kUnknown,
      -1);
  augIns0->set_index(method->code_attribute()->tyde_body().size());
  method->code_attribute()->tyde_body().push_back(augIns0);

  string method_name = string(method->class_descriptor())
      + method->name().bytes() + method->descriptor().bytes();
  ClassFile::VerifyErrorMap* verify_errors =
      ClassFile::GetVerifyErrorsForMethod(method_name);

  if (verify_errors != NULL
      && verify_errors->find(-1) != verify_errors->end()) {
    TydeInstruction* verify_ins = new TydeInstruction(0, kUnknown,
        0, "java/lang/VerifyError");
    verify_ins->set_index(method->code_attribute()->tyde_body().size());
    method->code_attribute()->tyde_body().push_back(verify_ins);
    method->code_attribute()->set_is_tyde_needed(false);
  } else {
    while (insn_idx < (int) dex_code->insnsSize) {
      int insnWidth;
      Opcode opcode;
      u2 instr;
      // Decode every instruction
      DecodedInstruction dec_insn;
      dexDecodeInstruction(insns, &dec_insn);

      instr = get2LE((const u1*)insns);
      if (instr == kPackedSwitchSignature) {
        insnWidth = 4 + get2LE((const u1*)(insns+1)) * 2;
      } else if (instr == kSparseSwitchSignature) {
        insnWidth = 2 + get2LE((const u1*)(insns+1)) * 4;
      } else if (instr == kArrayDataSignature) {
        int width = get2LE((const u1*)(insns+1));
        int size = get2LE((const u1*)(insns+2)) |
            (get2LE((const u1*)(insns+3))<<16);
        // The plus 1 is to round up for odd size and width
        insnWidth = 4 + ((size * width) + 1) / 2;
      } else {
        opcode = static_cast<Opcode> (instr & 0xff);
        insnWidth = dexGetWidthFromOpcode(opcode);
        if (insnWidth == 0) {
          fprintf(stderr,
              "GLITCH: zero-width instruction at idx=0x%04x\n", insn_idx);
          break;
        }
      }

      ClassFile::VerifyErrorMap::iterator it;

      if (verify_errors != NULL
          && (it = verify_errors->find(insn_idx)) != verify_errors->end()
          && it->second != VERIFY_ERROR_NONE) {
        const char* class_descriptor = "java/lang/VerifyError";

        switch (it->second) {
          case VERIFY_ERROR_NO_CLASS:
            class_descriptor =  "java/lang/NoClassDefFoundError";
            break;
          case VERIFY_ERROR_NO_FIELD:
            class_descriptor = "java/lang/NoSuchFieldError";
            break;
          case VERIFY_ERROR_NO_METHOD:
            class_descriptor = "java/lang/NoSuchMethodError";
            break;
          case VERIFY_ERROR_ACCESS_CLASS:
          case VERIFY_ERROR_ACCESS_FIELD:
          case VERIFY_ERROR_ACCESS_METHOD:
            class_descriptor = "java/lang/IllegalAccessError";
            break;
          case VERIFY_ERROR_CLASS_CHANGE:
            class_descriptor = "java/lang/IncompatibleClassChangeError";
            break;
          case VERIFY_ERROR_INSTANTIATION:
            class_descriptor = "java/lang/InstantiationError";
            break;
          case VERIFY_ERROR_NULL_POINTER:
            class_descriptor = "java/lang/NullPointerException";
            break;
          case VERIFY_ERROR_GENERIC:
          case VERIFY_ERROR_NONE:
            break;
        }
        TydeInstruction* verify_ins = new TydeInstruction(0, kUnknown,
            insn_idx, class_descriptor);
        verify_ins->set_index(method->code_attribute()->tyde_body().size());
        method->code_attribute()->tyde_body().push_back(verify_ins);
        just_rewrote = true;
      } else {
        // Augment every instruction to make further processing easier
        // Augmented instructions eventually contain additional information
        // (type, etc.).
        if (just_rewrote && dec_insn.opcode >= OP_MOVE_RESULT
            && dec_insn.opcode <= OP_MOVE_RESULT_OBJECT)
          dec_insn.opcode = OP_NOP;

        just_rewrote = false;
        TydeInstruction* aug_ins = new TydeInstruction(&dec_insn, insn_idx);
        aug_ins->ParseInstruction(&dec_insn, insn_idx, insns, method, cp_);

        aug_ins->set_index(method->code_attribute()->tyde_body().size());
        method->code_attribute()->tyde_body().push_back(aug_ins);
      }

      insns += insnWidth;
      insn_idx += insnWidth;
    }
  }

  // We add a NOP at the end; it is useful when a try block stops at the end
  // of the code. It does not change the semantics of the retargeted output.
  TydeInstruction* augIns = new TydeInstruction(0, kUnknown,
      insn_idx);
  augIns->set_index(method->code_attribute()->tyde_body().size());
  method->code_attribute()->tyde_body().push_back(augIns);
}

/**
* Create the Java class reference corresponding to an interface implemented
* by the current class.
*
* @param dex_file Pointer to the input dex file.
* @param type_item Pointer to the interface type item.
*/
ClassInfo* ClassFile::ParseInterface(const DexFile* dex_file,
    const DexTypeItem* type_item) {
  const char* interface_name = dexStringByTypeIdx(dex_file, type_item->typeIdx);
  ConstantPool::AddClassStub(interface_name, true, true);
  return new ClassInfo(interface_name, false);
}

/**
* Parse initial static values.
*
* @param dex_file Pointer to the input dex file.
* @param class_def Pointer to the current class definition.
* @param fields_size Fields size.
*/
void ClassFile::ParseStaticValues(const DexFile* dex_file,
    const DexClassDef* class_def, u4 fields_size) {
  // Get array of static values
  const DexEncodedArray* static_values = dexGetStaticValuesList(dex_file, class_def);
  u4 idx = 0;

  if (static_values != NULL) {
    // Parse encoded array items
    const u1** ptr = (const u1**) &static_values;
    int size = readUnsignedLeb128(ptr);

    while (size > 0) {
      std::vector<ConstantPoolInfo*> constants;
      Annotation::ProcessAnnotationValue(cp_, ptr, constants);
      if (constants.size() > 0)
        fields_defined_[idx++]->set_value((ConstantPoolInfo*) constants[0]);
      --size;
    }
  }

  while (idx < fields_size) {
    if (strcmp(fields_defined_[idx]->descriptor().bytes(), "I") == 0
        || strcmp(fields_defined_[idx]->descriptor().bytes(), "B") == 0
        || strcmp(fields_defined_[idx]->descriptor().bytes(), "C") == 0
        || strcmp(fields_defined_[idx]->descriptor().bytes(), "Z") == 0
        || strcmp(fields_defined_[idx]->descriptor().bytes(), "S") == 0)
      fields_defined_[idx++]->set_value((ConstantPoolInfo*) cp_.AddIntCst(0));
    else if (strcmp(fields_defined_[idx]->descriptor().bytes(), "J") == 0)
      fields_defined_[idx++]->set_value((ConstantPoolInfo*) cp_.AddLongCst(0));
    else if (strcmp(fields_defined_[idx]->descriptor().bytes(), "F") == 0)
      fields_defined_[idx++]->set_value(
          (ConstantPoolInfo*) cp_.AddFloatCst(0));
    else if (strcmp(fields_defined_[idx]->descriptor().bytes(), "D") == 0)
      fields_defined_[idx++]->set_value(
          (ConstantPoolInfo*) cp_.AddDoubleCst(0));
    else
      ++idx;
  }
}

/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "ScopedLocalFrame.h"

namespace android {
    extern int register_dalvik_system_TouchDex(JNIEnv* env);
}

extern int register_com_ibm_icu4jni_converters_NativeConverter(JNIEnv* env);
extern int register_com_ibm_icu4jni_lang_UCharacter(JNIEnv* env);
extern int register_com_ibm_icu4jni_regex_NativeRegEx(JNIEnv* env);
extern int register_com_ibm_icu4jni_text_NativeBreakIterator(JNIEnv* env);
extern int register_com_ibm_icu4jni_text_NativeCollator(JNIEnv* env);
extern int register_com_ibm_icu4jni_text_NativeDecimalFormat(JNIEnv* env);
extern int register_com_ibm_icu4jni_text_NativeIDN(JNIEnv* env);
extern int register_com_ibm_icu4jni_text_NativeNormalizer(JNIEnv* env);
extern int register_com_ibm_icu4jni_util_Resources(JNIEnv* env);
extern int register_java_io_Console(JNIEnv* env);
extern int register_java_io_File(JNIEnv* env);
extern "C" int register_java_io_FileDescriptor(JNIEnv* env);
extern "C" int register_java_io_ObjectInputStream(JNIEnv* env);
extern "C" int register_java_io_ObjectOutputStream(JNIEnv* env);
extern "C" int register_java_io_ObjectStreamClass(JNIEnv* env);
extern "C" int register_java_lang_Double(JNIEnv* env);
extern "C" int register_java_lang_Float(JNIEnv* env);
extern "C" int register_java_lang_Math(JNIEnv* env);
extern int register_java_lang_ProcessManager(JNIEnv* env);
extern "C" int register_java_lang_StrictMath(JNIEnv* env);
extern int register_java_lang_System(JNIEnv* env);
extern int register_java_net_InetAddress(JNIEnv* env);
extern int register_java_net_NetworkInterface(JNIEnv* env);
extern "C" int register_java_util_zip_Adler32(JNIEnv* env);
extern "C" int register_java_util_zip_CRC32(JNIEnv* env);
extern "C" int register_java_util_zip_Deflater(JNIEnv* env);
extern "C" int register_java_util_zip_Inflater(JNIEnv* env);
extern "C" int register_org_apache_harmony_dalvik_NativeTestTarget(JNIEnv* env);
extern int register_org_apache_harmony_luni_platform_OSFileSystem(JNIEnv* env);
extern int register_org_apache_harmony_luni_platform_OSMemory(JNIEnv* env);
extern int register_org_apache_harmony_luni_platform_OSNetworkSystem(JNIEnv* env);
extern "C" int register_org_apache_harmony_luni_util_NumberConvert(JNIEnv* env);
extern "C" int register_org_apache_harmony_luni_util_fltparse(JNIEnv* env);
extern int register_org_apache_harmony_text_BidiWrapper(JNIEnv* env);
extern int register_org_apache_harmony_xml_ExpatParser(JNIEnv* env);
extern int register_org_apache_harmony_xnet_provider_jsse_NativeCrypto(JNIEnv* env);
extern "C" int register_org_openssl_NativeBN(JNIEnv* env);

// DalvikVM calls this on startup, so we can statically register all our native methods.
extern "C" int registerCoreLibrariesJni(JNIEnv* env) {
    ScopedLocalFrame localFrame(env);

    bool result =
            register_com_ibm_icu4jni_converters_NativeConverter(env) != -1 &&
            register_com_ibm_icu4jni_lang_UCharacter(env) != -1 &&
            register_com_ibm_icu4jni_regex_NativeRegEx(env) != -1 &&
            register_com_ibm_icu4jni_text_NativeBreakIterator(env) != -1 &&
            register_com_ibm_icu4jni_text_NativeCollator(env) != -1 &&
            register_com_ibm_icu4jni_text_NativeDecimalFormat(env) != -1 &&
            register_com_ibm_icu4jni_text_NativeIDN(env) != -1 &&
            register_com_ibm_icu4jni_text_NativeNormalizer(env) != -1 &&
            register_com_ibm_icu4jni_util_Resources(env) != -1 &&
            register_java_io_Console(env) != -1 &&
            register_java_io_File(env) != -1 &&
            register_java_io_FileDescriptor(env) != -1 &&
            register_java_io_ObjectInputStream(env) != -1 &&
            register_java_io_ObjectOutputStream(env) != -1 &&
            register_java_io_ObjectStreamClass(env) != -1 &&
            register_java_lang_Double(env) != -1 &&
            register_java_lang_Float(env) != -1 &&
            register_java_lang_Math(env) != -1 &&
            register_java_lang_ProcessManager(env) != -1 &&
            register_java_lang_StrictMath(env) != -1 &&
            register_java_lang_System(env) != -1 &&
            register_java_net_InetAddress(env) != -1 &&
            register_java_net_NetworkInterface(env) != -1 &&
            register_java_util_zip_Adler32(env) != -1 &&
            register_java_util_zip_CRC32(env) != -1 &&
            register_java_util_zip_Deflater(env) != -1 &&
            register_java_util_zip_Inflater(env) != -1 &&
            register_org_apache_harmony_luni_platform_OSFileSystem(env) != -1 &&
            register_org_apache_harmony_luni_platform_OSMemory(env) != -1 &&
            register_org_apache_harmony_luni_platform_OSNetworkSystem(env) != -1 &&
            register_org_apache_harmony_luni_util_NumberConvert(env) != -1 &&
            register_org_apache_harmony_luni_util_fltparse(env) != -1 &&
            register_org_apache_harmony_text_BidiWrapper(env) != -1 &&
            register_org_apache_harmony_xnet_provider_jsse_NativeCrypto(env) != -1 &&
            register_org_openssl_NativeBN(env) != -1 &&
            // Initialize the Android classes last, as they have dependencies on the "corer" core classes.
            android::register_dalvik_system_TouchDex(env) != -1 &&
            register_org_apache_harmony_dalvik_NativeTestTarget(env) != -1 &&
            register_org_apache_harmony_xml_ExpatParser(env) != -1;

    return result ? 0 : -1;
}

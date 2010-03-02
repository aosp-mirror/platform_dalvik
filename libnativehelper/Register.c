/*
 * Copyright 2006 The Android Open Source Project
 *
 * JNI helper functions.
 */
#include "jni.h"
#include "AndroidSystemNatives.h"

#include <stdio.h>

/*
 * Register all methods for system classes.
 *
 * Remember to add the declarations to include/nativehelper/JavaSystemNatives.h.
 */
int jniRegisterSystemMethods(JNIEnv* env)
{
    int result = -1;

    (*env)->PushLocalFrame(env, 128);

    if (register_org_apache_harmony_dalvik_NativeTestTarget(env) != 0)
        goto bail;
    
    if (register_java_io_File(env) != 0)
        goto bail;
    if (register_java_io_FileDescriptor(env) != 0)
        goto bail;
    if (register_java_io_ObjectOutputStream(env) != 0)
        goto bail;
    if (register_java_io_ObjectInputStream(env) != 0)
        goto bail;
    if (register_java_io_ObjectStreamClass(env) != 0)
        goto bail;

    if (register_java_lang_Float(env) != 0)
        goto bail;
    if (register_java_lang_Double(env) != 0)
        goto bail;
    if (register_java_lang_Math(env) != 0)
        goto bail;
    if (register_java_lang_ProcessManager(env) != 0)
        goto bail;
    if (register_java_lang_StrictMath(env) != 0)
        goto bail;
    if (register_java_lang_System(env) != 0)
        goto bail;

    if (register_org_apache_harmony_luni_platform_OSFileSystem(env) != 0)
        goto bail;
    if (register_org_apache_harmony_luni_platform_OSMemory(env) != 0)
        goto bail;
    if (register_org_apache_harmony_luni_platform_OSNetworkSystem(env) != 0)
        goto bail;
    if (register_org_apache_harmony_luni_util_fltparse(env) != 0)
        goto bail;
    if (register_org_apache_harmony_luni_util_NumberConvert(env) != 0)
        goto bail;
    if (register_org_apache_harmony_text_BidiWrapper(env) != 0)
        goto bail;

    if (register_org_openssl_NativeBN(env) != 0)
        goto bail;
    if (register_org_apache_harmony_xnet_provider_jsse_NativeCrypto(env) != 0)
        goto bail;

    if (register_java_util_zip_Adler32(env) != 0)
        goto bail;
    if (register_java_util_zip_CRC32(env) != 0)
        goto bail;
    if (register_java_util_zip_Deflater(env) != 0)
        goto bail;
    if (register_java_util_zip_Inflater(env) != 0)
        goto bail;

    if (register_java_net_InetAddress(env) != 0)
        goto bail;
    if (register_java_net_NetworkInterface(env) != 0)
        goto bail;

    if (register_com_ibm_icu4jni_text_NativeNormalizer(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_text_NativeBreakIterator(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_text_NativeDecimalFormat(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_text_NativeCollator(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_converters_NativeConverter(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_regex_NativeRegEx(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_lang_UCharacter(env) != 0)
        goto bail;
    if (register_com_ibm_icu4jni_util_Resources(env) != 0)
        goto bail;

    /*
     * Initialize the Android classes last, as they have dependencies
     * on the "corer" core classes.
     */

    if (register_dalvik_system_TouchDex(env) != 0)
        goto bail;

    if (register_org_apache_harmony_xml_ExpatParser(env) != 0)
        goto bail;
    
    result = 0;

bail:
    (*env)->PopLocalFrame(env, NULL);
    return result;
}

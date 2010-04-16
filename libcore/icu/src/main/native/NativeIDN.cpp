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

#define LOG_TAG "NativeIDN"

#include "ErrorCode.h"
#include "JNIHelp.h"
#include "ScopedJavaUnicodeString.h"
#include "unicode/uidna.h"

static bool isLabelSeparator(const UChar ch) {
    switch (ch) {
    case 0x3002: // ideographic full stop
    case 0xff0e: // fullwidth full stop
    case 0xff61: // halfwidth ideographic full stop
        return true;
    default:
        return false;
    }
}

static jstring convertImpl(JNIEnv* env, jclass, jstring s, jint flags, jboolean toAscii) {
    ScopedJavaUnicodeString sus(env, s);
    const UChar* src = sus.unicodeString().getBuffer();
    const size_t srcLength = sus.unicodeString().length();
    UChar dst[256];
    UErrorCode status = U_ZERO_ERROR;
    size_t resultLength = toAscii
            ? uidna_IDNToASCII(src, srcLength, &dst[0], sizeof(dst), flags, NULL, &status)
            : uidna_IDNToUnicode(src, srcLength, &dst[0], sizeof(dst), flags, NULL, &status);
    if (U_FAILURE(status)) {
        jniThrowException(env, "java/lang/IllegalArgumentException", u_errorName(status));
        return NULL;
    }
    if (!toAscii) {
        // ICU only translates separators to ASCII for toASCII.
        // Java expects the translation for toUnicode too.
        // We may as well do this here, while the string is still mutable.
        for (size_t i = 0; i < resultLength; ++i) {
            if (isLabelSeparator(dst[i])) {
                dst[i] = '.';
            }
        }
    }
    return env->NewString(&dst[0], resultLength);
}

static JNINativeMethod gMethods[] = {
    {"convertImpl", "(Ljava/lang/String;IZ)Ljava/lang/String;", (void*) convertImpl},
};
extern "C" int register_com_ibm_icu4jni_text_NativeIDN(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/text/NativeIDN", gMethods, NELEM(gMethods));
}

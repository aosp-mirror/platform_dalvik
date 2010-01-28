/*
 * Copyright (C) 2006 The Android Open Source Project
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

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include "unicode/uchar.h"
#include <math.h>
#include <stdlib.h>

static jint digitImpl(JNIEnv*, jclass, jint codePoint, jint radix) {
    return u_digit(codePoint, radix);
}

static jint getTypeImpl(JNIEnv*, jclass, jint codePoint) {
    return u_charType(codePoint);
}

static jbyte getDirectionalityImpl(JNIEnv*, jclass, jint codePoint) {
    return u_charDirection(codePoint);
}

static jboolean isMirroredImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isMirrored(codePoint);
}

static jint getNumericValueImpl(JNIEnv*, jclass, jint codePoint){
    // The letters A-Z in their uppercase ('\u0041' through '\u005A'), 
    //                          lowercase ('\u0061' through '\u007A'), 
    //             and full width variant ('\uFF21' through '\uFF3A' 
    //                                 and '\uFF41' through '\uFF5A') forms 
    // have numeric values from 10 through 35. This is independent of the 
    // Unicode specification, which does not assign numeric values to these 
    // char values.
    if (codePoint >= 0x41 && codePoint <= 0x5A) {
        return codePoint - 0x37;
    }
    if (codePoint >= 0x61 && codePoint <= 0x7A) {
        return codePoint - 0x57;
    }
    if (codePoint >= 0xFF21 && codePoint <= 0xFF3A) {
        return codePoint - 0xFF17;
    }
    if (codePoint >= 0xFF41 && codePoint <= 0xFF5A) {
        return codePoint - 0xFF37;
    }

    double result = u_getNumericValue(codePoint);

    if (result == U_NO_NUMERIC_VALUE) {
        return -1;
    } else if (result < 0 || floor(result + 0.5) != result) {
        return -2;
    }

    return result;
} 
    
static jboolean isDefinedImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isdefined(codePoint);
} 

static jboolean isDigitImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isdigit(codePoint);
} 

static jboolean isIdentifierIgnorableImpl(JNIEnv*, jclass, jint codePoint) {
    // Java also returns TRUE for U+0085 Next Line (it omits U+0085 from whitespace ISO controls)
    if(codePoint == 0x0085) {
        return JNI_TRUE;
    }
    return u_isIDIgnorable(codePoint);
} 

static jboolean isLetterImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isalpha(codePoint);
} 

static jboolean isLetterOrDigitImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isalnum(codePoint);
} 

static jboolean isSpaceCharImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isJavaSpaceChar(codePoint);
} 

static jboolean isTitleCaseImpl(JNIEnv*, jclass, jint codePoint) {
    return u_istitle(codePoint);
} 

static jboolean isUnicodeIdentifierPartImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isIDPart(codePoint);
} 

static jboolean isUnicodeIdentifierStartImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isIDStart(codePoint);
} 

static jboolean isWhitespaceImpl(JNIEnv*, jclass, jint codePoint) {
    // Java omits U+0085
    if(codePoint == 0x0085) {
        return JNI_FALSE;
    }
    return u_isWhitespace(codePoint);
} 

static jint toLowerCaseImpl(JNIEnv*, jclass, jint codePoint) {
    return u_tolower(codePoint);
} 

static jint toTitleCaseImpl(JNIEnv*, jclass, jint codePoint) {
    return u_totitle(codePoint);
} 

static jint toUpperCaseImpl(JNIEnv*, jclass, jint codePoint) {
    return u_toupper(codePoint);
} 

static jboolean isUpperCaseImpl(JNIEnv*, jclass, jint codePoint) {
    return u_isupper(codePoint);
} 

static jboolean isLowerCaseImpl(JNIEnv*, jclass, jint codePoint) {
    return u_islower(codePoint);
} 

static int forNameImpl(JNIEnv* env, jclass, jstring blockName) {
    if (blockName == NULL) {
        jniThrowNullPointerException(env, NULL);
        return -1;
    }
    const char* bName = env->GetStringUTFChars(blockName, NULL);
    int result = u_getPropertyValueEnum(UCHAR_BLOCK, bName);
    env->ReleaseStringUTFChars(blockName, bName);
    return result;
}

static int ofImpl(JNIEnv*, jclass, jint codePoint) {
    return ublock_getCode(codePoint);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "digit", "(II)I", (void*) digitImpl },
    { "forName", "(Ljava/lang/String;)I", (void*) forNameImpl },
    { "getDirectionality", "(I)B", (void*) getDirectionalityImpl },
    { "getNumericValue", "(I)I", (void*) getNumericValueImpl },
    { "getType", "(I)I", (void*) getTypeImpl },
    { "isDefined", "(I)Z", (void*) isDefinedImpl },
    { "isDigit", "(I)Z", (void*) isDigitImpl },
    { "isIdentifierIgnorable", "(I)Z", (void*) isIdentifierIgnorableImpl },
    { "isLetter", "(I)Z", (void*) isLetterImpl },
    { "isLetterOrDigit", "(I)Z", (void*) isLetterOrDigitImpl },
    { "isLowerCase", "(I)Z", (void*) isLowerCaseImpl },
    { "isMirrored", "(I)Z", (void*) isMirroredImpl },
    { "isSpaceChar", "(I)Z", (void*) isSpaceCharImpl },
    { "isTitleCase", "(I)Z", (void*) isTitleCaseImpl },
    { "isUnicodeIdentifierPart", "(I)Z", (void*) isUnicodeIdentifierPartImpl },
    { "isUnicodeIdentifierStart", "(I)Z", (void*) isUnicodeIdentifierStartImpl },
    { "isUpperCase", "(I)Z", (void*) isUpperCaseImpl },
    { "isWhitespace", "(I)Z", (void*) isWhitespaceImpl },
    { "of", "(I)I", (void*) ofImpl },
    { "toLowerCase", "(I)I", (void*) toLowerCaseImpl },
    { "toTitleCase", "(I)I", (void*) toTitleCaseImpl },
    { "toUpperCase", "(I)I", (void*) toUpperCaseImpl },
}; 

int register_com_ibm_icu4jni_lang_UCharacter(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/lang/UCharacter",
                gMethods, NELEM(gMethods));
}

/*
 * Copyright 2006 The Android Open Source Project 
 *
 * Internal native functions.  All of the functions defined here make
 * direct use of VM functions or data structures, so they can't be written
 * with JNI and shouldn't really be in a shared library.
 *
 * All functions here either complete quickly or are used to enter a wait
 * state, so we don't set the thread status to THREAD_NATIVE when executing
 * these methods.  This means that the GC will wait for these functions
 * to finish.  DO NOT perform long operations or blocking I/O in here.
 *
 * In some cases we're following the division of labor defined by GNU
 * ClassPath, e.g. java.lang.Thread has "Thread" and "VMThread", with
 * the VM-specific behavior isolated in VMThread.
 */

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include "unicode/uchar.h"
#include <stdlib.h>
#include <math.h>

static jint digitImpl(JNIEnv *env, jclass clazz, jint codePoint, jint radix) {
    return u_digit(codePoint, radix);
}

static jint getTypeImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_charType(codePoint);
}

static jbyte getDirectionalityImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_charDirection (codePoint);
}

static jboolean isMirroredImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isMirrored (codePoint);
}

static jint getNumericValueImpl(JNIEnv *env, jclass clazz, jint codePoint){
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
    
static jboolean isDefinedValueImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isdefined(codePoint);
} 

static jboolean isDigitImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isdigit(codePoint);
} 

static jboolean isIdentifierIgnorableImpl(JNIEnv *env, jclass clazz, 
        jint codePoint) {

    // Java also returns TRUE for U+0085 Next Line (it omits U+0085 from whitespace ISO controls)
    if(codePoint == 0x0085) {
        return JNI_TRUE;
    }

    return u_isIDIgnorable(codePoint);
} 

static jboolean isLetterImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isalpha(codePoint);
} 

static jboolean isLetterOrDigitImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isalnum(codePoint);
} 

static jboolean isSpaceCharImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isJavaSpaceChar(codePoint);
} 

static jboolean isTitleCaseImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_istitle(codePoint);
} 

static jboolean isUnicodeIdentifierPartImpl(JNIEnv *env, jclass clazz, 
        jint codePoint) {
    return u_isIDPart(codePoint);
} 

static jboolean isUnicodeIdentifierStartImpl(JNIEnv *env, jclass clazz, 
        jint codePoint) {
    return u_isIDStart(codePoint);
} 

static jboolean isWhitespaceImpl(JNIEnv *env, jclass clazz, jint codePoint) {

    // Java omits U+0085
    if(codePoint == 0x0085) {
        return JNI_FALSE;
    }

    return u_isWhitespace(codePoint);
} 

static jint toLowerCaseImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_tolower(codePoint);
} 

static jint toTitleCaseImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_totitle(codePoint);
} 

static jint toUpperCaseImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_toupper(codePoint);
} 

static jboolean isUpperCaseImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_isupper(codePoint);
} 

static jboolean isLowerCaseImpl(JNIEnv *env, jclass clazz, jint codePoint) {
    return u_islower(codePoint);
} 

static int forName(JNIEnv *env, jclass clazz, jstring blockName) {
    const char *bName = (*env)->GetStringUTFChars(env, blockName, NULL);
    int result =  u_getPropertyValueEnum(UCHAR_BLOCK, bName);
    (*env)->ReleaseStringUTFChars(env, blockName, bName);
    return result;
}

static int codeBlock(JNIEnv *env, jclass clazz, jint codePoint) {
    return ublock_getCode(codePoint);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "digitImpl", "(II)I", (void*) digitImpl },
    { "getTypeImpl", "(I)I", (void*) getTypeImpl },
    { "getDirectionalityImpl", "(I)B", (void*) getDirectionalityImpl },
    { "isMirroredImpl", "(I)Z", (void*) isMirroredImpl },
    { "getNumericValueImpl", "(I)I", (void*) getNumericValueImpl },
    { "isDefinedValueImpl", "(I)Z", (void*) isDefinedValueImpl },
    { "isDigitImpl", "(I)Z", (void*) isDigitImpl },
    { "isIdentifierIgnorableImpl", "(I)Z", (void*) isIdentifierIgnorableImpl },
    { "isLetterImpl", "(I)Z", (void*) isLetterImpl },
    { "isLetterOrDigitImpl", "(I)Z", (void*) isLetterOrDigitImpl },
    { "isSpaceCharImpl", "(I)Z", (void*) isSpaceCharImpl },
    { "isTitleCaseImpl", "(I)Z", (void*) isTitleCaseImpl },
    { "isUnicodeIdentifierPartImpl", "(I)Z",
            (void*) isUnicodeIdentifierPartImpl },
    { "isUnicodeIdentifierStartImpl", "(I)Z",
            (void*) isUnicodeIdentifierStartImpl },
    { "isWhitespaceImpl", "(I)Z", (void*) isWhitespaceImpl },
    { "toLowerCaseImpl", "(I)I", (void*) toLowerCaseImpl },
    { "toTitleCaseImpl", "(I)I", (void*) toTitleCaseImpl },
    { "toUpperCaseImpl", "(I)I", (void*) toUpperCaseImpl },
    { "isUpperCaseImpl", "(I)Z", (void*) isUpperCaseImpl },
    { "isLowerCaseImpl", "(I)Z", (void*) isLowerCaseImpl },
    { "forname", "(Ljava/lang/String;)I", (void*) forName },
    { "codeblock", "(I)I", (void*) codeBlock }
}; 

int register_com_ibm_icu4jni_lang_UCharacter(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/lang/UCharacter",
                gMethods, NELEM(gMethods));
}



/*
 * Copyright (C) 2008 The Android Open Source Project
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
#include "unicode/numfmt.h"
#include "unicode/rbnf.h"
#include "unicode/fmtable.h"
#include "unicode/ustring.h"
#include "unicode/locid.h"
#include "ErrorCode.h"
#include <stdlib.h>
#include <string.h>

static UBool icuError(JNIEnv *env, UErrorCode errorcode)
{
  const char  *emsg = u_errorName(errorcode);
  jclass  exception;

  if (errorcode > U_ZERO_ERROR && errorcode < U_ERROR_LIMIT) {
    switch (errorcode) {
      case U_ILLEGAL_ARGUMENT_ERROR :
        exception = env->FindClass("java/lang/IllegalArgumentException");
        break;
      case U_INDEX_OUTOFBOUNDS_ERROR :
      case U_BUFFER_OVERFLOW_ERROR :
        exception = env->FindClass("java/lang/ArrayIndexOutOfBoundsException");
        break;
      case U_UNSUPPORTED_ERROR :
        exception = env->FindClass("java/lang/UnsupportedOperationException");
        break;
      default :
        exception = env->FindClass("java/lang/RuntimeException");
    }

    return (env->ThrowNew(exception, emsg) != 0);
  }
  return 0;
}

static jint openRBNFImpl1(JNIEnv* env, jclass clazz, 
        jint type, jstring locale) {

    // LOGI("ENTER openRBNFImpl1");

    // the errorcode returned by unum_open
    UErrorCode status = U_ZERO_ERROR;

    // prepare the locale string for the call to unum_open
    const char *localeChars = env->GetStringUTFChars(locale, NULL);

    URBNFRuleSetTag style;
    if(type == 0) {
        style = URBNF_SPELLOUT;
    } else if(type == 1) {
        style = URBNF_ORDINAL;
    } else if(type == 2) {
        style = URBNF_DURATION;
    } else if(type == 3) {
        style = URBNF_COUNT;
    } else {
        printf("error");
    }
    
    Locale loc = Locale::createFromName(localeChars);

    // open a default type number format
    RuleBasedNumberFormat *fmt = new RuleBasedNumberFormat(style, loc, status);

    // release the allocated strings
    env->ReleaseStringUTFChars(locale, localeChars);

    // check for an error
    if ( icuError(env, status) != FALSE) {
        return 0;
    }

    // return the handle to the number format
    return (long) fmt;

}

static jint openRBNFImpl2(JNIEnv* env, jclass clazz, 
        jstring rule, jstring locale) {

    // LOGI("ENTER openRBNFImpl2");

    // the errorcode returned by unum_open
    UErrorCode status = U_ZERO_ERROR;

    // prepare the pattern string for the call to unum_open
    const UChar *ruleChars = env->GetStringChars(rule, NULL);
    int ruleLen = env->GetStringLength(rule);

    // prepare the locale string for the call to unum_open
    const char *localeChars = env->GetStringUTFChars(locale, NULL);

    // open a rule based number format
    UNumberFormat *fmt = unum_open(UNUM_PATTERN_RULEBASED, ruleChars, ruleLen, 
                localeChars, NULL, &status);

    // release the allocated strings
    env->ReleaseStringChars(rule, ruleChars);
    env->ReleaseStringUTFChars(locale, localeChars);

    // check for an error
    if ( icuError(env, status) != FALSE) {
        return 0;
    }

    // return the handle to the number format
    return (long) fmt;

}

static void closeRBNFImpl(JNIEnv *env, jclass clazz, jint addr) {

    // LOGI("ENTER closeRBNFImpl");

    // get the pointer to the number format    
    RuleBasedNumberFormat *fmt = (RuleBasedNumberFormat *)(int)addr;

    // close this number format
    delete fmt;
}

static jstring formatLongRBNFImpl(JNIEnv *env, jclass clazz, jint addr, jlong value, 
        jobject field, jstring fieldType, jobject attributes) {

    // LOGI("ENTER formatLongRBNFImpl");

    const char * fieldPositionClassName = "java/text/FieldPosition";
    const char * stringBufferClassName = "java/lang/StringBuffer";
    jclass fieldPositionClass = env->FindClass(fieldPositionClassName);
    jclass stringBufferClass = env->FindClass(stringBufferClassName);
    jmethodID setBeginIndexMethodID = env->GetMethodID(fieldPositionClass, 
            "setBeginIndex", "(I)V");
    jmethodID setEndIndexMethodID = env->GetMethodID(fieldPositionClass, 
            "setEndIndex", "(I)V");
    jmethodID appendMethodID = env->GetMethodID(stringBufferClass, 
            "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");

    const char * fieldName = NULL;

    if(fieldType != NULL) {
        fieldName = env->GetStringUTFChars(fieldType, NULL);
    }

    uint32_t reslenneeded;
    int64_t val = value;
    UChar *result = NULL;

    FieldPosition fp;
    fp.setField(FieldPosition::DONT_CARE);

    UErrorCode status = U_ZERO_ERROR;

    RuleBasedNumberFormat *fmt = (RuleBasedNumberFormat *)(int)addr;

    UnicodeString res;

    fmt->format(val, res, fp);

    reslenneeded = res.extract(NULL, 0, status);

    if(status==U_BUFFER_OVERFLOW_ERROR) {
        status=U_ZERO_ERROR;

        result = (UChar*)malloc(sizeof(UChar) * (reslenneeded + 1));    

        res.extract(result, reslenneeded + 1, status);
    }
    if (icuError(env, status) != FALSE) {
        free(result);
        return NULL;
    }

    if(fieldType != NULL) {
        env->ReleaseStringUTFChars(fieldType, fieldName);
    }

    jstring resulting = env->NewString(result, reslenneeded);

    free(result);

    return resulting;
}

static jstring formatDoubleRBNFImpl(JNIEnv *env, jclass clazz, jint addr, jdouble value, 
        jobject field, jstring fieldType, jobject attributes) {

    // LOGI("ENTER formatDoubleRBNFImpl");

    const char * fieldPositionClassName = "java/text/FieldPosition";
    const char * stringBufferClassName = "java/lang/StringBuffer";
    jclass fieldPositionClass = env->FindClass(fieldPositionClassName);
    jclass stringBufferClass = env->FindClass(stringBufferClassName);
    jmethodID setBeginIndexMethodID = env->GetMethodID(fieldPositionClass, 
            "setBeginIndex", "(I)V");
    jmethodID setEndIndexMethodID = env->GetMethodID(fieldPositionClass, 
            "setEndIndex", "(I)V");
    jmethodID appendMethodID = env->GetMethodID(stringBufferClass, 
            "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");

    const char * fieldName = NULL;

    if(fieldType != NULL) {
        fieldName = env->GetStringUTFChars(fieldType, NULL);
    }

    uint32_t reslenneeded;
    double val = value;
    UChar *result = NULL;

    FieldPosition fp;
    fp.setField(FieldPosition::DONT_CARE);

    UErrorCode status = U_ZERO_ERROR;

    RuleBasedNumberFormat *fmt = (RuleBasedNumberFormat *)(int)addr;

    UnicodeString res;

    fmt->format(val, res, fp);

    reslenneeded = res.extract(NULL, 0, status);

    if(status==U_BUFFER_OVERFLOW_ERROR) {
        status=U_ZERO_ERROR;

        result = (UChar*)malloc(sizeof(UChar) * (reslenneeded + 1));    

        res.extract(result, reslenneeded + 1, status);
    }
    if (icuError(env, status) != FALSE) {
        free(result);
        return NULL;
    }

    if(fieldType != NULL) {
        env->ReleaseStringUTFChars(fieldType, fieldName);
    }

    jstring resulting = env->NewString(result, reslenneeded);

    free(result);

    return resulting;
}

static jobject parseRBNFImpl(JNIEnv *env, jclass clazz, jint addr, jstring text, 
        jobject position, jboolean lenient) {

    // LOGI("ENTER parseRBNFImpl");

    const char * parsePositionClassName = "java/text/ParsePosition";
    const char * longClassName = "java/lang/Long";
    const char * doubleClassName = "java/lang/Double";


    UErrorCode status = U_ZERO_ERROR;

    UNumberFormat *fmt = (UNumberFormat *)(int)addr;

    jchar *str = (UChar *)env->GetStringChars(text, NULL);
    int strlength = env->GetStringLength(text);

    jclass parsePositionClass = env->FindClass(parsePositionClassName);
    jclass longClass =  env->FindClass(longClassName);
    jclass doubleClass =  env->FindClass(doubleClassName);

    jmethodID getIndexMethodID = env->GetMethodID(parsePositionClass, 
            "getIndex", "()I");
    jmethodID setIndexMethodID = env->GetMethodID(parsePositionClass, 
            "setIndex", "(I)V");
    jmethodID setErrorIndexMethodID = env->GetMethodID(parsePositionClass, 
            "setErrorIndex", "(I)V");

    jmethodID longInitMethodID = env->GetMethodID(longClass, "<init>", "(J)V");
    jmethodID dblInitMethodID = env->GetMethodID(doubleClass, "<init>", "(D)V");

    int parsePos = env->CallIntMethod(position, getIndexMethodID, NULL);

    // make sure the ParsePosition is valid. Actually icu4c would parse a number 
    // correctly even if the parsePosition is set to -1, but since the RI fails 
    // for that case we have to fail too
    if(parsePos < 0 || parsePos > strlength) {
        return NULL;
    }

    Formattable res;

    const UnicodeString src((UChar*)str, strlength, strlength);
    ParsePosition pp;
    
    pp.setIndex(parsePos);
    
    if(lenient) {
        unum_setAttribute(fmt, UNUM_LENIENT_PARSE, JNI_TRUE);
    }
    
    ((const NumberFormat*)fmt)->parse(src, res, pp);

    if(lenient) {
        unum_setAttribute(fmt, UNUM_LENIENT_PARSE, JNI_FALSE);
    }
    
    env->ReleaseStringChars(text, str);

    if(pp.getErrorIndex() == -1) {
        parsePos = pp.getIndex();
    } else {
        env->CallVoidMethod(position, setErrorIndexMethodID, 
                (jint) pp.getErrorIndex());        
        return NULL;
    }

    Formattable::Type numType;
    numType = res.getType();
    UErrorCode fmtStatus;

    double resultDouble;
    long resultLong;
    int64_t resultInt64;

    switch(numType) {
        case Formattable::kDouble:
            resultDouble = res.getDouble();
            env->CallVoidMethod(position, setIndexMethodID, (jint) parsePos);
            return env->NewObject(doubleClass, dblInitMethodID, 
                    (jdouble) resultDouble);
        case Formattable::kLong:
            resultLong = res.getLong();
            env->CallVoidMethod(position, setIndexMethodID, (jint) parsePos);
            return env->NewObject(longClass, longInitMethodID, 
                    (jlong) resultLong);
        case Formattable::kInt64:
            resultInt64 = res.getInt64();
            env->CallVoidMethod(position, setIndexMethodID, (jint) parsePos);
            return env->NewObject(longClass, longInitMethodID, 
                    (jlong) resultInt64);
        default:
            break;
    }

    return NULL;
}

static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    {"openRBNFImpl", "(ILjava/lang/String;)I", (void*) openRBNFImpl1},
    {"openRBNFImpl", "(Ljava/lang/String;Ljava/lang/String;)I", 
            (void*) openRBNFImpl2},
    {"closeRBNFImpl", "(I)V", (void*) closeRBNFImpl},
    {"formatRBNFImpl", 
            "(IJLjava/text/FieldPosition;Ljava/lang/String;Ljava/lang/StringBuffer;)Ljava/lang/String;", 
            (void*) formatLongRBNFImpl},
    {"formatRBNFImpl",
            "(IDLjava/text/FieldPosition;Ljava/lang/String;Ljava/lang/StringBuffer;)Ljava/lang/String;", 
            (void*) formatDoubleRBNFImpl},
    {"parseRBNFImpl", 
            "(ILjava/lang/String;Ljava/text/ParsePosition;Z)Ljava/lang/Number;", 
            (void*) parseRBNFImpl},
};
int register_com_ibm_icu4jni_text_NativeRBNF(JNIEnv* env) {
    return jniRegisterNativeMethods(env, 
            "com/ibm/icu4jni/text/RuleBasedNumberFormat", gMethods, 
            NELEM(gMethods));
}

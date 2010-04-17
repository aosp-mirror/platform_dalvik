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

#define LOG_TAG "NativeDecimalFormat"

#include "JNIHelp.h"
#include "cutils/log.h"
#include "unicode/unum.h"
#include "unicode/numfmt.h"
#include "unicode/decimfmt.h"
#include "unicode/fmtable.h"
#include "unicode/ustring.h"
#include "digitlst.h"
#include "ErrorCode.h"
#include "ScopedJavaUnicodeString.h"
#include <stdlib.h>
#include <string.h>

static DecimalFormat* toDecimalFormat(jint addr) {
    return reinterpret_cast<DecimalFormat*>(static_cast<uintptr_t>(addr));
}

static DecimalFormatSymbols* makeDecimalFormatSymbols(JNIEnv* env,
        jstring currencySymbol0, jchar decimalSeparator, jchar digit,
        jchar groupingSeparator0, jstring infinity0,
        jstring internationalCurrencySymbol0, jchar minusSign,
        jchar monetaryDecimalSeparator, jstring nan0, jchar patternSeparator,
        jchar percent, jchar perMill, jchar zeroDigit) {
    ScopedJavaUnicodeString currencySymbol(env, currencySymbol0);
    ScopedJavaUnicodeString infinity(env, infinity0);
    ScopedJavaUnicodeString internationalCurrencySymbol(env, internationalCurrencySymbol0);
    ScopedJavaUnicodeString nan(env, nan0);
    UnicodeString groupingSeparator(groupingSeparator0);

    DecimalFormatSymbols* result = new DecimalFormatSymbols;
    result->setSymbol(DecimalFormatSymbols::kCurrencySymbol, currencySymbol.unicodeString());
    result->setSymbol(DecimalFormatSymbols::kDecimalSeparatorSymbol, UnicodeString(decimalSeparator));
    result->setSymbol(DecimalFormatSymbols::kDigitSymbol, UnicodeString(digit));
    result->setSymbol(DecimalFormatSymbols::kGroupingSeparatorSymbol, groupingSeparator);
    result->setSymbol(DecimalFormatSymbols::kMonetaryGroupingSeparatorSymbol, groupingSeparator);
    result->setSymbol(DecimalFormatSymbols::kInfinitySymbol, infinity.unicodeString());
    result->setSymbol(DecimalFormatSymbols::kIntlCurrencySymbol, internationalCurrencySymbol.unicodeString());
    result->setSymbol(DecimalFormatSymbols::kMinusSignSymbol, UnicodeString(minusSign));
    result->setSymbol(DecimalFormatSymbols::kMonetarySeparatorSymbol, UnicodeString(monetaryDecimalSeparator));
    result->setSymbol(DecimalFormatSymbols::kNaNSymbol, nan.unicodeString());
    result->setSymbol(DecimalFormatSymbols::kPatternSeparatorSymbol, UnicodeString(patternSeparator));
    result->setSymbol(DecimalFormatSymbols::kPercentSymbol, UnicodeString(percent));
    result->setSymbol(DecimalFormatSymbols::kPerMillSymbol, UnicodeString(perMill));
    result->setSymbol(DecimalFormatSymbols::kZeroDigitSymbol, UnicodeString(zeroDigit));
    return result;
}

static void setDecimalFormatSymbols(JNIEnv* env, jclass, jint addr,
        jstring currencySymbol, jchar decimalSeparator, jchar digit,
        jchar groupingSeparator, jstring infinity,
        jstring internationalCurrencySymbol, jchar minusSign,
        jchar monetaryDecimalSeparator, jstring nan, jchar patternSeparator,
        jchar percent, jchar perMill, jchar zeroDigit) {
    DecimalFormatSymbols* symbols = makeDecimalFormatSymbols(env,
            currencySymbol, decimalSeparator, digit, groupingSeparator,
            infinity, internationalCurrencySymbol, minusSign,
            monetaryDecimalSeparator, nan, patternSeparator, percent, perMill,
            zeroDigit);
    toDecimalFormat(addr)->adoptDecimalFormatSymbols(symbols);
}

static jint openDecimalFormatImpl(JNIEnv* env, jclass clazz, jstring pattern0,
        jstring currencySymbol, jchar decimalSeparator, jchar digit,
        jchar groupingSeparator, jstring infinity,
        jstring internationalCurrencySymbol, jchar minusSign,
        jchar monetaryDecimalSeparator, jstring nan, jchar patternSeparator,
        jchar percent, jchar perMill, jchar zeroDigit) {
    if (pattern0 == NULL) {
        jniThrowNullPointerException(env, NULL);
        return 0;
    }
    UErrorCode status = U_ZERO_ERROR;
    UParseError parseError;
    ScopedJavaUnicodeString pattern(env, pattern0);
    DecimalFormatSymbols* symbols = makeDecimalFormatSymbols(env,
            currencySymbol, decimalSeparator, digit, groupingSeparator,
            infinity, internationalCurrencySymbol, minusSign,
            monetaryDecimalSeparator, nan, patternSeparator, percent, perMill,
            zeroDigit);
    DecimalFormat* fmt = new DecimalFormat(pattern.unicodeString(), symbols, parseError, status);
    if (fmt == NULL) {
        delete symbols;
    }
    icu4jni_error(env, status);
    return static_cast<jint>(reinterpret_cast<uintptr_t>(fmt));
}

static void closeDecimalFormatImpl(JNIEnv* env, jclass, jint addr) {
    delete toDecimalFormat(addr);
}

static void setRoundingMode(JNIEnv* env, jclass, jint addr, jint mode, jdouble increment) {
    DecimalFormat* fmt = toDecimalFormat(addr);
    fmt->setRoundingMode(static_cast<DecimalFormat::ERoundingMode>(mode));
    fmt->setRoundingIncrement(increment);
}

static void setSymbol(JNIEnv* env, jclass, jint addr, jint symbol, jstring s) {
    const UChar* chars = env->GetStringChars(s, NULL);
    const int32_t charCount = env->GetStringLength(s);
    UErrorCode status = U_ZERO_ERROR;
    UNumberFormat* fmt = reinterpret_cast<UNumberFormat*>(static_cast<uintptr_t>(addr));
    unum_setSymbol(fmt, static_cast<UNumberFormatSymbol>(symbol), chars, charCount, &status);
    icu4jni_error(env, status);
    env->ReleaseStringChars(s, chars);
}

static void setAttribute(JNIEnv *env, jclass clazz, jint addr, jint symbol,
        jint value) {

    UNumberFormat *fmt = (UNumberFormat *)(int)addr;

    unum_setAttribute(fmt, (UNumberFormatAttribute) symbol, value);
}

static jint getAttribute(JNIEnv *env, jclass clazz, jint addr, jint symbol) {

    UNumberFormat *fmt = (UNumberFormat *)(int)addr;

    int res = unum_getAttribute(fmt, (UNumberFormatAttribute) symbol);

    return res;
}

static void setTextAttribute(JNIEnv *env, jclass clazz, jint addr, jint symbol,
        jstring text) {

    // the errorcode returned by unum_setTextAttribute
    UErrorCode status = U_ZERO_ERROR;

    // get the pointer to the number format
    UNumberFormat *fmt = (UNumberFormat *)(int)addr;

    const UChar *textChars = env->GetStringChars(text, NULL);
    int textLen = env->GetStringLength(text);

    unum_setTextAttribute(fmt, (UNumberFormatTextAttribute) symbol, textChars,
            textLen, &status);

    env->ReleaseStringChars(text, textChars);

    icu4jni_error(env, status);
}

static jstring getTextAttribute(JNIEnv *env, jclass clazz, jint addr,
        jint symbol) {

    uint32_t resultlength, reslenneeded;

    // the errorcode returned by unum_getTextAttribute
    UErrorCode status = U_ZERO_ERROR;

    // get the pointer to the number format
    UNumberFormat *fmt = (UNumberFormat *)(int)addr;

    UChar* result = NULL;
    resultlength=0;

    // find out how long the result will be
    reslenneeded=unum_getTextAttribute(fmt, (UNumberFormatTextAttribute) symbol,
            result, resultlength, &status);

    result = NULL;
    if(status==U_BUFFER_OVERFLOW_ERROR) {
        status=U_ZERO_ERROR;
        resultlength=reslenneeded+1;
        result=(UChar*)malloc(sizeof(UChar) * resultlength);
        reslenneeded=unum_getTextAttribute(fmt,
                (UNumberFormatTextAttribute) symbol, result, resultlength,
                &status);
    }
    if (icu4jni_error(env, status) != FALSE) {
        return NULL;
    }

    jstring res = env->NewString(result, reslenneeded);

    free(result);

    return res;
}

static void applyPatternImpl(JNIEnv *env, jclass clazz, jint addr, jboolean localized, jstring pattern0) {
    if (pattern0 == NULL) {
        jniThrowNullPointerException(env, NULL);
        return;
    }
    ScopedJavaUnicodeString pattern(env, pattern0);
    DecimalFormat* fmt = toDecimalFormat(addr);
    UErrorCode status = U_ZERO_ERROR;
    if (localized) {
        fmt->applyLocalizedPattern(pattern.unicodeString(), status);
    } else {
        fmt->applyPattern(pattern.unicodeString(), status);
    }
    icu4jni_error(env, status);
}

static jstring toPatternImpl(JNIEnv *env, jclass, jint addr, jboolean localized) {
    DecimalFormat* fmt = toDecimalFormat(addr);
    UnicodeString pattern;
    if (localized) {
        fmt->toLocalizedPattern(pattern);
    } else {
        fmt->toPattern(pattern);
    }
    return env->NewString(pattern.getBuffer(), pattern.length());
}

template <typename T>
static jstring format(JNIEnv *env, jint addr, jobject field, jstring fieldType, jobject attributes, T val) {
    UErrorCode status = U_ZERO_ERROR;

    DecimalFormat::AttributeBuffer attrBuffer;
    attrBuffer.buffer = NULL;
    DecimalFormat::AttributeBuffer* attrBufferPtr = NULL;
    if (attributes != NULL || (fieldType != NULL && field != NULL)) {
        attrBufferPtr = &attrBuffer;
        // ICU requires that this is dynamically allocated and non-zero size.
        // ICU grows it in chunks of 128 bytes, so that's a reasonable initial size.
        attrBuffer.bufferSize = 128;
        attrBuffer.buffer = new char[attrBuffer.bufferSize];
        attrBuffer.buffer[0] = '\0';
    }

    FieldPosition fp;
    fp.setField(FieldPosition::DONT_CARE);

    UnicodeString str;
    DecimalFormat* fmt = toDecimalFormat(addr);
    fmt->format(val, str, fp, attrBufferPtr);

    if (attrBufferPtr && strlen(attrBuffer.buffer) > 0) {
        // check if we want to get all attributes
        if (attributes != NULL) {
            jstring attrString = env->NewStringUTF(attrBuffer.buffer + 1);  // cut off the leading ';'
            jclass stringBufferClass = env->FindClass("java/lang/StringBuffer");
            jmethodID appendMethodID = env->GetMethodID(stringBufferClass, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
            env->CallObjectMethod(attributes, appendMethodID, attrString);
        }

        // check if we want one special attribute returned in the given FieldPos
        if (fieldType != NULL && field != NULL) {
            const char* fieldName = env->GetStringUTFChars(fieldType, NULL);

            const char* delimiter = ";";
            char* context = NULL;
            char* resattr = strtok_r(attrBuffer.buffer, delimiter, &context);

            while (resattr != NULL && strcmp(resattr, fieldName) != 0) {
                resattr = strtok_r(NULL, delimiter, &context);
            }

            if (resattr != NULL && strcmp(resattr, fieldName) == 0) {
                resattr = strtok_r(NULL, delimiter, &context);
                int begin = (int) strtol(resattr, NULL, 10);
                resattr = strtok_r(NULL, delimiter, &context);
                int end = (int) strtol(resattr, NULL, 10);

                jclass fieldPositionClass = env->FindClass("java/text/FieldPosition");
                jmethodID setBeginIndexMethodID = env->GetMethodID(fieldPositionClass, "setBeginIndex", "(I)V");
                jmethodID setEndIndexMethodID = env->GetMethodID(fieldPositionClass, "setEndIndex", "(I)V");
                env->CallVoidMethod(field, setBeginIndexMethodID, (jint) begin);
                env->CallVoidMethod(field, setEndIndexMethodID, (jint) end);
            }
            env->ReleaseStringUTFChars(fieldType, fieldName);
        }
    }

    jstring result = env->NewString(str.getBuffer(), str.length());
    delete[] attrBuffer.buffer;
    return result;
}

static jstring formatLong(JNIEnv* env, jclass, jint addr, jlong value,
        jobject field, jstring fieldType, jobject attributes) {
    int64_t longValue = value;
    return format(env, addr, field, fieldType, attributes, longValue);
}

static jstring formatDouble(JNIEnv* env, jclass, jint addr, jdouble value,
        jobject field, jstring fieldType, jobject attributes) {
    double doubleValue = value;
    return format(env, addr, field, fieldType, attributes, doubleValue);
}

static jstring formatDigitList(JNIEnv *env, jclass clazz, jint addr, jstring value,
        jobject field, jstring fieldType, jobject attributes, jint scale) {

    // const char * valueUTF = env->GetStringUTFChars(value, NULL);
    // LOGI("ENTER formatDigitList: %s, scale: %d", valueUTF, scale);
    // env->ReleaseStringUTFChars(value, valueUTF);

    if (scale < 0) {
        icu4jni_error(env, U_ILLEGAL_ARGUMENT_ERROR);
        return NULL;
    }

    const char * fieldName = NULL;
    if(fieldType != NULL) {
        fieldName = env->GetStringUTFChars(fieldType, NULL);
    }

    uint32_t reslenneeded;

    // prepare digit list

    const char *valueChars = env->GetStringUTFChars(value, NULL);

    bool isInteger = (scale == 0);
    bool isPositive = (*valueChars != '-');

    // skip the '-' if the number is negative
    const char *digits = (isPositive ? valueChars : valueChars + 1);
    int length = strlen(digits);

    DecimalFormat* fmt = toDecimalFormat(addr);

    // The length of our digit list buffer must be the actual string length + 3,
    // because ICU will append some additional characters at the head and at the
    // tail of the string, in order to keep strtod() happy:
    //
    // - The sign "+" or "-" is appended at the head
    // - The exponent "e" and the "\0" terminator is appended at the tail
    //
    // In retrospect, the changes to ICU's DigitList that were necessary for
    // big numbers look a bit hacky. It would make sense to rework all this
    // once ICU 4.x has been integrated into Android. Ideally, big number
    // support would make it into ICU itself, so we don't need our private
    // fix anymore.
    DigitList digitList(length + 3);
    digitList.fCount = length;
    strcpy(digitList.fDigits, digits);
    env->ReleaseStringUTFChars(value, valueChars);

    digitList.fDecimalAt = digitList.fCount - scale;
    digitList.fIsPositive = isPositive;
    digitList.fRoundingMode = fmt->getRoundingMode();
    digitList.round(fmt->getMaximumFractionDigits() + digitList.fDecimalAt);

    UChar *result = NULL;

    FieldPosition fp;
    fp.setField(FieldPosition::DONT_CARE);
    fp.setBeginIndex(0);
    fp.setEndIndex(0);

    UErrorCode status = U_ZERO_ERROR;

    DecimalFormat::AttributeBuffer *attrBuffer = NULL;
    attrBuffer = (DecimalFormat::AttributeBuffer *) calloc(sizeof(DecimalFormat::AttributeBuffer), 1);
    attrBuffer->bufferSize = 128;
    attrBuffer->buffer = (char *) calloc(129 * sizeof(char), 1);

    UnicodeString res;

    fmt->subformat(res, fp, attrBuffer, digitList, isInteger);

    reslenneeded = res.extract(NULL, 0, status);

    if(status==U_BUFFER_OVERFLOW_ERROR) {
        status=U_ZERO_ERROR;

        result = (UChar*)malloc(sizeof(UChar) * (reslenneeded + 1));

        res.extract(result, reslenneeded + 1, status);

        if (icu4jni_error(env, status) != FALSE) {
            if(fieldType != NULL) {
                env->ReleaseStringUTFChars(fieldType, fieldName);
            }
            free(result);
            free(attrBuffer->buffer);
            free(attrBuffer);
            return NULL;
        }

    } else {
        if(fieldType != NULL) {
            env->ReleaseStringUTFChars(fieldType, fieldName);
        }
        free(attrBuffer->buffer);
        free(attrBuffer);
        return NULL;
    }

    int attrLength = (strlen(attrBuffer->buffer) + 1 );

    if(attrLength > 1) {

        // check if we want to get all attributes
        if(attributes != NULL) {
            // prepare the classes and method ids
            const char * stringBufferClassName = "java/lang/StringBuffer";
            jclass stringBufferClass = env->FindClass(stringBufferClassName);
            jmethodID appendMethodID = env->GetMethodID(stringBufferClass,
                    "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");

            jstring attrString = env->NewStringUTF(attrBuffer->buffer + 1);  // cut off the leading ';'
            env->CallObjectMethod(attributes, appendMethodID, attrString);
        }

        // check if we want one special attribute returned in the given FieldPos
        if(fieldName != NULL && field != NULL) {
            const char *delimiter = ";";
            int begin;
            int end;
            char * resattr;
            resattr = strtok(attrBuffer->buffer, delimiter);

            while(resattr != NULL && strcmp(resattr, fieldName) != 0) {
                resattr = strtok(NULL, delimiter);
            }

            if(resattr != NULL && strcmp(resattr, fieldName) == 0) {

                // prepare the classes and method ids
                const char * fieldPositionClassName =
                        "java/text/FieldPosition";
                jclass fieldPositionClass = env->FindClass(
                        fieldPositionClassName);
                jmethodID setBeginIndexMethodID = env->GetMethodID(
                        fieldPositionClass, "setBeginIndex", "(I)V");
                jmethodID setEndIndexMethodID = env->GetMethodID(
                       fieldPositionClass, "setEndIndex", "(I)V");


                resattr = strtok(NULL, delimiter);
                begin = (int) strtol(resattr, NULL, 10);
                resattr = strtok(NULL, delimiter);
                end = (int) strtol(resattr, NULL, 10);

                env->CallVoidMethod(field, setBeginIndexMethodID, (jint) begin);
                env->CallVoidMethod(field, setEndIndexMethodID, (jint) end);
            }
        }
    }

    if(fieldType != NULL) {
        env->ReleaseStringUTFChars(fieldType, fieldName);
    }

    jstring resulting = env->NewString(result, reslenneeded);

    free(attrBuffer->buffer);
    free(attrBuffer);
    free(result);
    // const char * resultUTF = env->GetStringUTFChars(resulting, NULL);
    // LOGI("RETURN formatDigitList: %s", resultUTF);
    // env->ReleaseStringUTFChars(resulting, resultUTF);

    return resulting;
}

static jobject parse(JNIEnv *env, jclass clazz, jint addr, jstring text,
        jobject position) {
    // TODO: cache these?
    jclass parsePositionClass = env->FindClass("java/text/ParsePosition");
    jclass longClass =  env->FindClass("java/lang/Long");
    jclass doubleClass =  env->FindClass("java/lang/Double");
    jclass bigDecimalClass = env->FindClass("java/math/BigDecimal");
    jclass bigIntegerClass = env->FindClass("java/math/BigInteger");

    jmethodID getIndexMethodID = env->GetMethodID(parsePositionClass,
            "getIndex", "()I");
    jmethodID setIndexMethodID = env->GetMethodID(parsePositionClass,
            "setIndex", "(I)V");
    jmethodID setErrorIndexMethodID = env->GetMethodID(parsePositionClass,
            "setErrorIndex", "(I)V");

    jmethodID longInitMethodID = env->GetMethodID(longClass, "<init>", "(J)V");
    jmethodID dblInitMethodID = env->GetMethodID(doubleClass, "<init>", "(D)V");
    jmethodID bigDecimalInitMethodID = env->GetMethodID(bigDecimalClass, "<init>", "(Ljava/math/BigInteger;I)V");
    jmethodID bigIntegerInitMethodID = env->GetMethodID(bigIntegerClass, "<init>", "(Ljava/lang/String;)V");
    jmethodID doubleValueMethodID = env->GetMethodID(bigDecimalClass, "doubleValue", "()D");

    // make sure the ParsePosition is valid. Actually icu4c would parse a number
    // correctly even if the parsePosition is set to -1, but since the RI fails
    // for that case we have to fail too
    int parsePos = env->CallIntMethod(position, getIndexMethodID, NULL);
    const int strlength = env->GetStringLength(text);
    if(parsePos < 0 || parsePos > strlength) {
        return NULL;
    }
    
    ParsePosition pp;
    pp.setIndex(parsePos);

    DigitList digits;
    
    UNumberFormat *fmt = (UNumberFormat *)(int)addr;
    Formattable res;
    bool resultAssigned;
    jchar *str = (UChar *)env->GetStringChars(text, NULL);
    const UnicodeString src((UChar*)str, strlength, strlength);
    ((const DecimalFormat*)fmt)->parse(src, resultAssigned, res, pp, FALSE, digits);
    env->ReleaseStringChars(text, str);

    if(pp.getErrorIndex() == -1) {
        parsePos = pp.getIndex();
    } else {
        env->CallVoidMethod(position, setErrorIndexMethodID,
                (jint) pp.getErrorIndex());
        return NULL;
    }

    Formattable::Type numType = res.getType();
    UErrorCode fmtStatus;

    double resultDouble;
    long resultLong;
    int64_t resultInt64;
    jstring resultStr;
    jobject resultObject1, resultObject2;

    if (resultAssigned)
    {
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
            return NULL;
        }
    }
    else
    {
        int scale = digits.fCount - digits.fDecimalAt;
        // ATTENTION: Abuse of Implementation Knowlegde!
        digits.fDigits[digits.fCount] = 0;
        if (digits.fIsPositive) {
            resultStr = env->NewStringUTF(digits.fDigits);
        } else {
            if (digits.fCount == 0) {
                env->CallVoidMethod(position, setIndexMethodID, (jint) parsePos);
                return env->NewObject(doubleClass, dblInitMethodID, (jdouble)-0);
            } else {
                // ATTENTION: Abuse of Implementation Knowlegde!
                *(digits.fDigits - 1) = '-';
                resultStr = env->NewStringUTF(digits.fDigits - 1);
            }
        }

        env->CallVoidMethod(position, setIndexMethodID, (jint) parsePos);

        resultObject1 = env->NewObject(bigIntegerClass, bigIntegerInitMethodID, resultStr);
        resultObject2 = env->NewObject(bigDecimalClass, bigDecimalInitMethodID, resultObject1, scale);
        return resultObject2;
    }
}

static jint cloneDecimalFormatImpl(JNIEnv *env, jclass, jint addr) {
    DecimalFormat* fmt = toDecimalFormat(addr);
    return static_cast<jint>(reinterpret_cast<uintptr_t>(fmt->clone()));
}

static JNINativeMethod gMethods[] = {
    {"applyPatternImpl", "(IZLjava/lang/String;)V", (void*) applyPatternImpl},
    {"cloneDecimalFormatImpl", "(I)I", (void*) cloneDecimalFormatImpl},
    {"closeDecimalFormatImpl", "(I)V", (void*) closeDecimalFormatImpl},
    {"format", "(IDLjava/text/FieldPosition;Ljava/lang/String;Ljava/lang/StringBuffer;)Ljava/lang/String;", (void*) formatDouble},
    {"format", "(IJLjava/text/FieldPosition;Ljava/lang/String;Ljava/lang/StringBuffer;)Ljava/lang/String;", (void*) formatLong},
    {"format", "(ILjava/lang/String;Ljava/text/FieldPosition;Ljava/lang/String;Ljava/lang/StringBuffer;I)Ljava/lang/String;", (void*) formatDigitList},
    {"getAttribute", "(II)I", (void*) getAttribute},
    {"getTextAttribute", "(II)Ljava/lang/String;", (void*) getTextAttribute},
    {"openDecimalFormatImpl", "(Ljava/lang/String;Ljava/lang/String;CCCLjava/lang/String;Ljava/lang/String;CCLjava/lang/String;CCCC)I", (void*) openDecimalFormatImpl},
    {"parse", "(ILjava/lang/String;Ljava/text/ParsePosition;)Ljava/lang/Number;", (void*) parse},
    {"setAttribute", "(III)V", (void*) setAttribute},
    {"setDecimalFormatSymbols", "(ILjava/lang/String;CCCLjava/lang/String;Ljava/lang/String;CCLjava/lang/String;CCCC)V", (void*) setDecimalFormatSymbols},
    {"setSymbol", "(IILjava/lang/String;)V", (void*) setSymbol},
    {"setRoundingMode", "(IID)V", (void*) setRoundingMode},
    {"setTextAttribute", "(IILjava/lang/String;)V", (void*) setTextAttribute},
    {"toPatternImpl", "(IZ)Ljava/lang/String;", (void*) toPatternImpl},
};
int register_com_ibm_icu4jni_text_NativeDecimalFormat(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/text/NativeDecimalFormat", gMethods,
            NELEM(gMethods));
}

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

#include <stdio.h>

#include "unicode/uregex.h"
#include "unicode/utypes.h"
#include "unicode/parseerr.h"

#include <jni.h>
#include <JNIHelp.h>
//#include <android_runtime/AndroidRuntime.h>

static const jchar EMPTY_STRING = 0;
 
static void throwPatternSyntaxException(JNIEnv* env, UErrorCode status, jstring pattern, UParseError error)
{
    jclass clazz = env->FindClass("java/util/regex/PatternSyntaxException");
    jmethodID method = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;I)V");
    
    jstring message = env->NewStringUTF(u_errorName(status));
    jthrowable except = (jthrowable)(env->NewObject(clazz, method, message, pattern, error.offset));
    env->Throw(except);
}

static void throwRuntimeException(JNIEnv* env, UErrorCode status)
{
    jniThrowException(env, "java/lang/RuntimeException", u_errorName(status));
}

static URegularExpression* open(JNIEnv* env, jclass clazz, jstring pattern, jint flags)
{
    flags = flags | UREGEX_ERROR_ON_UNKNOWN_ESCAPES;
    
    jchar const * patternRaw;
    int patternLen = env->GetStringLength(pattern);
    if (patternLen == 0) {
        patternRaw = &EMPTY_STRING;
        patternLen = -1;
    } else {
        patternRaw = env->GetStringChars(pattern, NULL);
    }

    UErrorCode status = U_ZERO_ERROR;
    UParseError error;
    error.offset = -1;
    
    URegularExpression* result = uregex_open(patternRaw, patternLen, flags, &error, &status);
    if (patternLen != -1) {
        env->ReleaseStringChars(pattern, patternRaw);
    }    
    if (!U_SUCCESS(status)) {
        throwPatternSyntaxException(env, status, pattern, error);
    }
    
    return result;
}

static URegularExpression* _clone(JNIEnv* env, jclass clazz, jint regex)
{
    UErrorCode status = U_ZERO_ERROR;

    URegularExpression* result = uregex_clone((URegularExpression*)regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static void _close(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    uregex_close(regex);
}

static void setText(JNIEnv* env, jclass clazz, URegularExpression* regex, jstring text)
{
    jchar const * textRaw;
    int textLen = env->GetStringLength(text);
    if (textLen == 0) {
        textLen = -1;
        textRaw = &EMPTY_STRING;
    } else {
        textRaw = env->GetStringChars(text, NULL);
    }
    
    UErrorCode status = U_ZERO_ERROR;
    
    uregex_setText(regex, textRaw, textLen, &status);
    if (textLen != -1) {
        env->ReleaseStringChars(text, textRaw);
    }    
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jboolean matches(JNIEnv* env, jclass clazz, URegularExpression* regex, jint startIndex)
{
    UErrorCode status = U_ZERO_ERROR;
    
    jboolean result = uregex_matches(regex, startIndex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    
    return result;
}

static jboolean lookingAt(JNIEnv* env, jclass clazz, URegularExpression* regex, jint startIndex)
{
    UErrorCode status = U_ZERO_ERROR;
    
    jboolean result = uregex_lookingAt(regex, startIndex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    
    return result;
}

static jboolean find(JNIEnv* env, jclass clazz, URegularExpression* regex, jint startIndex)
{
    UErrorCode status = U_ZERO_ERROR;
    
    jboolean result = uregex_find(regex, startIndex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static jboolean findNext(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;

    jboolean result = uregex_findNext(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    
    return result;
}

static jint groupCount(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;

    jint result = uregex_groupCount(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static void startEnd(JNIEnv* env, jclass clazz, URegularExpression* regex, jintArray offsets)
{
    UErrorCode status = U_ZERO_ERROR;
    
    jint * offsetsRaw = env->GetIntArrayElements(offsets, NULL);

    int groupCount = uregex_groupCount(regex, &status);
    for (int i = 0; i <= groupCount && U_SUCCESS(status); i++) {
        offsetsRaw[2 * i + 0] = uregex_start(regex, i, &status);
        offsetsRaw[2 * i + 1] = uregex_end(regex, i, &status);
    }
    
    env->ReleaseIntArrayElements(offsets, offsetsRaw, 0);
    
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static void setRegion(JNIEnv* env, jclass clazz, URegularExpression* regex, jint start, jint end)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_setRegion(regex, start, end, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jint regionStart(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;
    int result = uregex_regionStart(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static jint regionEnd(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;
    int result = uregex_regionEnd(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static void useTransparentBounds(JNIEnv* env, jclass clazz, URegularExpression* regex, jboolean value)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_useTransparentBounds(regex, value, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jboolean hasTransparentBounds(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_hasTransparentBounds(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static void useAnchoringBounds(JNIEnv* env, jclass clazz, URegularExpression* regex, jboolean value)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_useAnchoringBounds(regex, value, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jboolean hasAnchoringBounds(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_hasAnchoringBounds(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static jboolean hitEnd(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_hitEnd(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static jboolean requireEnd(JNIEnv* env, jclass clazz, URegularExpression* regex)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_requireEnd(regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static void reset(JNIEnv* env, jclass clazz, URegularExpression* regex, jint position)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_reset(regex, position, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

/*
 * JNI registration.
 */
static JNINativeMethod sMethods[] = {
    /* name, signature, funcPtr */
    { "open",                 "(Ljava/lang/String;I)I", (void*)open       },
    { "clone",                "(I)I",                   (void*)_clone     },
    { "close",                "(I)V",                   (void*)_close     },
    { "setText",              "(ILjava/lang/String;)V", (void*)setText    },
    { "matches",              "(II)Z",                  (void*)matches    },
    { "lookingAt",            "(II)Z",                  (void*)lookingAt  },
    { "find",                 "(II)Z",                  (void*)find       },
    { "findNext",             "(I)Z",                   (void*)findNext   },
    { "groupCount",           "(I)I",                   (void*)groupCount },
    { "startEnd",             "(I[I)V",                 (void*)startEnd   },
    { "setRegion",            "(III)V",                 (void*)setRegion  },
    { "regionStart",          "(I)I",                   (void*)regionStart },
    { "regionEnd",            "(I)I",                   (void*)regionEnd  },
    { "useTransparentBounds", "(IZ)V",                  (void*)useTransparentBounds },
    { "hasTransparentBounds", "(I)Z",                   (void*)hasTransparentBounds },
    { "useAnchoringBounds",   "(IZ)V",                  (void*)useAnchoringBounds },
    { "hasAnchoringBounds",   "(I)Z",                   (void*)hasAnchoringBounds },
    { "hitEnd",               "(I)Z",                   (void*)hitEnd },
    { "requireEnd",           "(I)Z",                   (void*)requireEnd },
    { "reset",                "(II)V",                  (void*)reset }
};

extern "C" int register_com_ibm_icu4jni_regex_NativeRegEx(JNIEnv* env)
{
    jclass clazz;

    clazz = env->FindClass("com/ibm/icu4jni/regex/NativeRegEx");
    if (clazz == NULL) {
        return -1;
    }

    if (env->RegisterNatives(clazz, sMethods, NELEM(sMethods)) < 0) {
        return -1;
    }
    
    return 0;
}

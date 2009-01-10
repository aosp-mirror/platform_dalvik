/*
 * Copyright (C) 2007 The Android Open Source Project
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "unicode/uregex.h"
#include "unicode/utypes.h"
#include "unicode/parseerr.h"

#include <jni.h>
#include <JNIHelp.h>

static jchar EMPTY_STRING = 0;

/**
 * A data structure that ties together an ICU regular expression and the
 * character data it refers to (but does not have a copy of), so we can
 * manage memory properly.
 */
typedef struct RegExDataStruct {
    // A pointer to the ICU regular expression
    URegularExpression* regex;
    // A pointer to (a copy of) the input text that *we* manage
    jchar* text;
} RegExData;

static void throwPatternSyntaxException(JNIEnv* env, UErrorCode status,
                                        jstring pattern, UParseError error)
{
    jclass clazz = env->FindClass("java/util/regex/PatternSyntaxException");
    jmethodID method = env->GetMethodID(clazz, "<init>",
                                    "(Ljava/lang/String;Ljava/lang/String;I)V");

    jstring message = env->NewStringUTF(u_errorName(status));
    jthrowable except = (jthrowable)(env->NewObject(clazz, method, message,
                                                    pattern, error.offset));
    env->Throw(except);
}

static void throwRuntimeException(JNIEnv* env, UErrorCode status)
{
    jniThrowException(env, "java/lang/RuntimeException", u_errorName(status));
}

static void _close(JNIEnv* env, jclass clazz, RegExData* data)
{
    if (data->regex != NULL) {
        uregex_close(data->regex);
    }

    if (data->text != NULL && data->text != &EMPTY_STRING) {
        free(data->text);
    }

    free(data);
}

static RegExData* open(JNIEnv* env, jclass clazz, jstring pattern, jint flags)
{
    flags = flags | UREGEX_ERROR_ON_UNKNOWN_ESCAPES;

    RegExData* data = (RegExData*)calloc(sizeof(RegExData), 1);

    UErrorCode status = U_ZERO_ERROR;
    UParseError error;
    error.offset = -1;

    jchar const * patternRaw;
    int patternLen = env->GetStringLength(pattern);
    if (patternLen == 0) {
        data->regex = uregex_open(&EMPTY_STRING, -1, flags, &error, &status);
    } else {
        jchar const * patternRaw = env->GetStringChars(pattern, NULL);
        data->regex = uregex_open(patternRaw, patternLen, flags, &error,
                                  &status);
        env->ReleaseStringChars(pattern, patternRaw);
    }

    if (!U_SUCCESS(status)) {
        _close(env, clazz, data);
        throwPatternSyntaxException(env, status, pattern, error);
        data = NULL;
    }

    return data;
}

static RegExData* _clone(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;

    URegularExpression* clonedRegex = uregex_clone(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    RegExData* result = (RegExData*)calloc(sizeof(RegExData), 1);
    result->regex = clonedRegex;

    return result;
}

static void setText(JNIEnv* env, jclass clazz, RegExData* data, jstring text)
{
    UErrorCode status = U_ZERO_ERROR;

    uregex_setText(data->regex, &EMPTY_STRING, 0, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
        return;
    }

    if (data->text != NULL && data->text != &EMPTY_STRING) {
        free(data->text);
        data->text = NULL;
    }

    int textLen = env->GetStringLength(text);
    if (textLen == 0) {
        data->text = &EMPTY_STRING;
    } else {
        jchar const * textRaw = env->GetStringChars(text, NULL);
        data->text = (jchar*)malloc((textLen + 1) * sizeof(jchar));
        memcpy(data->text, textRaw, textLen * sizeof(jchar));
        data->text[textLen] = 0;
        env->ReleaseStringChars(text, textRaw);
    }

    uregex_setText(data->regex, data->text, textLen, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jboolean matches(JNIEnv* env, jclass clazz, RegExData* data,
                        jint startIndex)
{
    UErrorCode status = U_ZERO_ERROR;

    jboolean result = uregex_matches(data->regex, startIndex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static jboolean lookingAt(JNIEnv* env, jclass clazz, RegExData* data,
                          jint startIndex)
{
    UErrorCode status = U_ZERO_ERROR;

    jboolean result = uregex_lookingAt(data->regex, startIndex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static jboolean find(JNIEnv* env, jclass clazz, RegExData* data,
                     jint startIndex)
{
    UErrorCode status = U_ZERO_ERROR;

    jboolean result = uregex_find(data->regex, startIndex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static jboolean findNext(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;

    jboolean result = uregex_findNext(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static jint groupCount(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;

    jint result = uregex_groupCount(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }

    return result;
}

static void startEnd(JNIEnv* env, jclass clazz, RegExData* data,
                     jintArray offsets)
{
    UErrorCode status = U_ZERO_ERROR;

    jint * offsetsRaw = env->GetIntArrayElements(offsets, NULL);

    int groupCount = uregex_groupCount(data->regex, &status);
    for (int i = 0; i <= groupCount && U_SUCCESS(status); i++) {
        offsetsRaw[2 * i + 0] = uregex_start(data->regex, i, &status);
        offsetsRaw[2 * i + 1] = uregex_end(data->regex, i, &status);
    }

    env->ReleaseIntArrayElements(offsets, offsetsRaw, 0);

    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static void setRegion(JNIEnv* env, jclass clazz, RegExData* data, jint start,
                      jint end)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_setRegion(data->regex, start, end, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jint regionStart(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;
    int result = uregex_regionStart(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static jint regionEnd(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;
    int result = uregex_regionEnd(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static void useTransparentBounds(JNIEnv* env, jclass clazz, RegExData* data,
                                 jboolean value)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_useTransparentBounds(data->regex, value, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jboolean hasTransparentBounds(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_hasTransparentBounds(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static void useAnchoringBounds(JNIEnv* env, jclass clazz, RegExData* data,
                               jboolean value)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_useAnchoringBounds(data->regex, value, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
}

static jboolean hasAnchoringBounds(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_hasAnchoringBounds(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static jboolean hitEnd(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_hitEnd(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static jboolean requireEnd(JNIEnv* env, jclass clazz, RegExData* data)
{
    UErrorCode status = U_ZERO_ERROR;
    jboolean result = uregex_requireEnd(data->regex, &status);
    if (!U_SUCCESS(status)) {
        throwRuntimeException(env, status);
    }
    return result;
}

static void reset(JNIEnv* env, jclass clazz, RegExData* data, jint position)
{
    UErrorCode status = U_ZERO_ERROR;
    uregex_reset(data->regex, position, &status);
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
    { "useTransparentBounds", "(IZ)V",            (void*)useTransparentBounds },
    { "hasTransparentBounds", "(I)Z",             (void*)hasTransparentBounds },
    { "useAnchoringBounds",   "(IZ)V",            (void*)useAnchoringBounds },
    { "hasAnchoringBounds",   "(I)Z",             (void*)hasAnchoringBounds },
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

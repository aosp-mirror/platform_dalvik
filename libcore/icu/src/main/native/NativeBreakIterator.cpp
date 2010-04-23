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

#define LOG_TAG "NativeBreakIterator"

#include "JNIHelp.h"
#include "ErrorCode.h"
#include "ScopedUtfChars.h"
#include "unicode/ubrk.h"
#include "unicode/putil.h"
#include <stdlib.h>

static jint getIterator(JNIEnv* env, jstring locale, UBreakIteratorType type) {
    UErrorCode status = U_ZERO_ERROR;
    ScopedUtfChars localeChars(env, locale);
    if (!localeChars.c_str()) {
        return 0;
    }
    UBreakIterator* it = ubrk_open(type, localeChars.c_str(), NULL, 0, &status);
    icu4jni_error(env, status);
    return reinterpret_cast<uintptr_t>(it);
}

static jint getCharacterInstanceImpl(JNIEnv* env, jclass clazz, jstring locale) {
    return getIterator(env, locale, UBRK_CHARACTER);
}

static jint getLineInstanceImpl(JNIEnv* env, jclass, jstring locale) {
    return getIterator(env, locale, UBRK_LINE);
}

static jint getSentenceInstanceImpl(JNIEnv* env, jclass, jstring locale) {
    return getIterator(env, locale, UBRK_SENTENCE);
}

static jint getWordInstanceImpl(JNIEnv* env, jclass, jstring locale) {
    return getIterator(env, locale, UBRK_WORD);
}

static UBreakIterator* breakIterator(jint address) {
    return reinterpret_cast<UBreakIterator*>(static_cast<uintptr_t>(address));
}

static void closeBreakIteratorImpl(JNIEnv* env, jclass, jint address) {
    ubrk_close(breakIterator(address));
}

static jint cloneImpl(JNIEnv* env, jclass, jint address) {
    UErrorCode status = U_ZERO_ERROR;
    jint bufferSize = U_BRK_SAFECLONE_BUFFERSIZE;
    UBreakIterator* it = ubrk_safeClone(breakIterator(address), NULL, &bufferSize, &status);
    icu4jni_error(env, status);
    return reinterpret_cast<uintptr_t>(it);
}

static void setTextImpl(JNIEnv* env, jclass, jint address, jstring text) {
    UErrorCode status = U_ZERO_ERROR;
    const UChar* chars = env->GetStringChars(text, NULL);
    ubrk_setText(breakIterator(address), chars, env->GetStringLength(text), &status);
    env->ReleaseStringChars(text, chars);
    icu4jni_error(env, status);
}

static jboolean isBoundaryImpl(JNIEnv*, jclass, jint address, jint offset) {
    return ubrk_isBoundary(breakIterator(address), offset);
}

static jint nextImpl(JNIEnv* env, jclass, jint address, jint n) {
    UBreakIterator* bi = breakIterator(address);
    if (n < 0) {
        while (n++ < -1) {
            ubrk_previous(bi);
        }
        return ubrk_previous(bi);
    } else if (n == 0) {
        return ubrk_current(bi);
    } else {
        while (n-- > 1) {
            ubrk_next(bi);
        }
        return ubrk_next(bi);
    }
    return -1;
}

static jint precedingImpl(JNIEnv*, jclass, jint address, jint offset) {
    return ubrk_preceding(breakIterator(address), offset);
}

static jint firstImpl(JNIEnv*, jclass, jint address) {
    return ubrk_first(breakIterator(address));
}

static jint followingImpl(JNIEnv*, jclass, jint address, jint offset) {
    return ubrk_following(breakIterator(address), offset);
}

static jint currentImpl(JNIEnv*, jclass, jint address) {
    return ubrk_current(breakIterator(address));
}

static jint previousImpl(JNIEnv*, jclass, jint address) {
    return ubrk_previous(breakIterator(address));
}

static jint lastImpl(JNIEnv*, jclass, jint address) {
    return ubrk_last(breakIterator(address));
}

static JNINativeMethod gMethods[] = {
    { "cloneImpl", "(I)I", (void*) cloneImpl },
    { "closeBreakIteratorImpl", "(I)V", (void*) closeBreakIteratorImpl },
    { "currentImpl", "(I)I", (void*) currentImpl },
    { "firstImpl", "(I)I", (void*) firstImpl },
    { "followingImpl", "(II)I", (void*) followingImpl },
    { "getCharacterInstanceImpl", "(Ljava/lang/String;)I", (void*) getCharacterInstanceImpl },
    { "getLineInstanceImpl", "(Ljava/lang/String;)I", (void*) getLineInstanceImpl },
    { "getSentenceInstanceImpl", "(Ljava/lang/String;)I", (void*) getSentenceInstanceImpl },
    { "getWordInstanceImpl", "(Ljava/lang/String;)I", (void*) getWordInstanceImpl },
    { "isBoundaryImpl", "(II)Z", (void*) isBoundaryImpl },
    { "lastImpl", "(I)I", (void*) lastImpl },
    { "nextImpl", "(II)I", (void*) nextImpl },
    { "precedingImpl", "(II)I", (void*) precedingImpl },
    { "previousImpl", "(I)I", (void*) previousImpl },
    { "setTextImpl", "(ILjava/lang/String;)V", (void*) setTextImpl },
};
int register_com_ibm_icu4jni_text_NativeBreakIterator(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/text/NativeBreakIterator",
            gMethods, NELEM(gMethods));
}

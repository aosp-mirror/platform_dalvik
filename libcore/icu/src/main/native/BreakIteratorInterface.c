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
#include "ErrorCode.h"
#include "unicode/ubrk.h"
#include "unicode/putil.h"
#include <stdlib.h>

static jobjectArray getAvailableLocalesImpl(JNIEnv *env, jclass clazz) {
    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    if (stringClass == NULL) {
        return NULL;
    }
    size_t count = ubrk_countAvailable();
    jobjectArray result = (*env)->NewObjectArray(env, count, stringClass, NULL);
    size_t i = 0;
    for (; i < count; ++i) {
        jstring s = (*env)->NewStringUTF(env, ubrk_getAvailable(i));
        (*env)->SetObjectArrayElement(env, result, i, s);
        (*env)->DeleteLocalRef(env, s);
    }
    return result;
}

static jint getCharacterInstanceImpl(JNIEnv *env, jclass clazz, jstring locale) {

    UErrorCode status = U_ZERO_ERROR;

    const char *localeChars = (*env)->GetStringUTFChars(env, locale, 0);

    UBreakIterator *iter = ubrk_open(UBRK_CHARACTER, localeChars, NULL, 0, &status);

    (*env)->ReleaseStringUTFChars(env, locale, localeChars);
    
    if ( icu4jni_error(env, status) != FALSE) {
        return 0;
    }

    return (long) iter;
}

static jint getLineInstanceImpl(JNIEnv *env, jclass clazz, jstring locale) {
 
    UErrorCode status = U_ZERO_ERROR;

    const char *localeChars = (*env)->GetStringUTFChars(env, locale, 0);

    enum UBreakIteratorType type = UBRK_LINE;

    UBreakIterator *iter = ubrk_open(type, localeChars, NULL, 0, &status);

    (*env)->ReleaseStringUTFChars(env, locale, localeChars);
    
    if ( icu4jni_error(env, status) != FALSE) {
        return 0;
    }

    return (long) iter;
}

static jint getSentenceInstanceImpl(JNIEnv *env, jclass clazz, jstring locale) {

    UErrorCode status = U_ZERO_ERROR;

    const char *localeChars = (*env)->GetStringUTFChars(env, locale, 0);

    enum UBreakIteratorType type = UBRK_SENTENCE;

    UBreakIterator *iter = ubrk_open(type, localeChars, NULL, 0, &status);

    (*env)->ReleaseStringUTFChars(env, locale, localeChars);
    
    if ( icu4jni_error(env, status) != FALSE) {
        return 0;
    }

    return (long) iter;
}

static jint getWordInstanceImpl(JNIEnv *env, jclass clazz, jstring locale) {

    UErrorCode status = U_ZERO_ERROR;

    const char *localeChars = (*env)->GetStringUTFChars(env, locale, 0);

    enum UBreakIteratorType type = UBRK_WORD;

    UBreakIterator *iter = ubrk_open(type, localeChars, NULL, 0, &status);

    (*env)->ReleaseStringUTFChars(env, locale, localeChars);
    
    if ( icu4jni_error(env, status) != FALSE) {
        return 0;
    }

    return (long) iter;
}

static void closeBreakIteratorImpl(JNIEnv *env, jclass clazz, jint address) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    ubrk_close(bi);
}

static jint cloneImpl(JNIEnv *env, jclass clazz, jint address) {

    UErrorCode status = U_ZERO_ERROR;

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    jint buffersize = U_BRK_SAFECLONE_BUFFERSIZE;

    UBreakIterator *iter = ubrk_safeClone(bi, NULL, &buffersize, &status);

    if (icu4jni_error(env, status) != FALSE) {
        return 0;
    }

    return (long) iter;
}

static void setTextImpl(JNIEnv *env, jclass clazz, jint address, jstring text) {

    UErrorCode status = U_ZERO_ERROR;

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    const UChar *strUChars = (*env)->GetStringChars(env, text, NULL);
    int strLen = (*env)->GetStringLength(env, text);

    ubrk_setText(bi, strUChars, strLen, &status);

    (*env)->ReleaseStringChars(env, text, strUChars);

    icu4jni_error(env, status);
}

static jboolean isBoundaryImpl(JNIEnv *env, jclass clazz, jint address, jint offset) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_isBoundary(bi, offset);
}

static jint nextImpl(JNIEnv *env, jclass clazz, jint address, jint n) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    if(n < 0) {
        while(n++ < -1) {
            ubrk_previous(bi);
        }
        return ubrk_previous(bi);
    } else if(n == 0) {
        return ubrk_current(bi);
    } else {
        while(n-- > 1) {
            ubrk_next(bi);
        }
        return ubrk_next(bi);
    }

    return -1;
}

static jint precedingImpl(JNIEnv *env, jclass clazz, jint address, jint offset) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_preceding(bi, offset);
}

static jint firstImpl(JNIEnv *env, jclass clazz, jint address) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_first(bi);
}

static jint followingImpl(JNIEnv *env, jclass clazz, jint address, jint offset) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_following(bi, offset);
}

static jint currentImpl(JNIEnv *env, jclass clazz, jint address) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_current(bi);
}

static jint previousImpl(JNIEnv *env, jclass clazz, jint address) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_previous(bi);
}

static jint lastImpl(JNIEnv *env, jclass clazz, jint address) {

    UBreakIterator *bi = (UBreakIterator *)(long)address;

    return ubrk_last(bi);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getAvailableLocalesImpl", "()[Ljava/lang/String;", (void*) getAvailableLocalesImpl },
    { "getCharacterInstanceImpl", "(Ljava/lang/String;)I", 
            (void*) getCharacterInstanceImpl },
    { "getLineInstanceImpl", "(Ljava/lang/String;)I", 
            (void*) getLineInstanceImpl },
    { "getSentenceInstanceImpl", "(Ljava/lang/String;)I", 
            (void*) getSentenceInstanceImpl },
    { "getWordInstanceImpl", "(Ljava/lang/String;)I", 
            (void*) getWordInstanceImpl },
    { "closeBreakIteratorImpl", "(I)V", 
            (void*) closeBreakIteratorImpl },
    { "cloneImpl", "(I)I", 
            (void*) cloneImpl },
    { "setTextImpl", "(ILjava/lang/String;)V", 
            (void*) setTextImpl },
    { "isBoundaryImpl", "(II)Z", 
            (void*) isBoundaryImpl },
    { "nextImpl", "(II)I", 
            (void*) nextImpl },
    { "precedingImpl", "(II)I", 
            (void*) precedingImpl },
    { "firstImpl", "(I)I", 
            (void*) firstImpl },
    { "lastImpl", "(I)I", 
            (void*) lastImpl },
    { "currentImpl", "(I)I", 
            (void*) currentImpl },
    { "followingImpl", "(II)I", 
            (void*) followingImpl },
    { "previousImpl", "(I)I", 
            (void*) previousImpl },
};
int register_com_ibm_icu4jni_text_NativeBreakIterator(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/text/NativeBreakIterator",
                gMethods, NELEM(gMethods));
}

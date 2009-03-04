//
//  java_lang_Character.cpp
//  Android
//
//  Copyright 2006 The Android Open Source Project
//
#include "JNIHelp.h"
#include "AndroidSystemNatives.h"

//#define LOG_TAG "Character"
//#include "utils/Log.h"
#include "utils/AndroidUnicode.h"

#include <stdlib.h>


using namespace android;

/*
 * native private static int nativeGetData(int c)
 */
static jint getData(JNIEnv* env, jclass clazz, jint val)
{
    return Unicode::getPackedData(val);
}

/*
 * native private static int nativeToLower(int c)
 */
static jint toLower(JNIEnv* env, jclass clazz, jint val)
{
    return Unicode::toLower(val);
}

/*
 * native private static int nativeToUpper(int c)
 */
static jint toUpper(JNIEnv* env, jclass clazz, jint val)
{
    return Unicode::toUpper(val);
}

/*
 * native private static int nativeNumericValue(int c)
 */
static jint numericValue(JNIEnv* env, jclass clazz, jint val)
{
    return Unicode::getNumericValue(val);
}

/*
 * native private static int nativeToTitle(int c)
 */
static jint toTitle(JNIEnv* env, jclass clazz, jint val)
{
    return Unicode::toTitle(val);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "nativeGetData",          "(I)I",     (void*) getData },
    { "nativeToLower",          "(I)I",     (void*) toLower },
    { "nativeToUpper",          "(I)I",     (void*) toUpper },
    { "nativeNumericValue",     "(I)I",     (void*) numericValue },
    { "nativeToTitle",          "(I)I",     (void*) toTitle },
};
int register_java_lang_Character(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/lang/Character",
        gMethods, NELEM(gMethods));
}


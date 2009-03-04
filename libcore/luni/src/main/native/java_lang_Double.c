//
//  java_lang_Double.c
//  Android
//
//  Copyright 2005 The Android Open Source Project
//
#include "JNIHelp.h"

#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

typedef union {
    uint64_t    bits;
    double      d;
} Double;

#define NaN (0x7ff8000000000000ULL)

/*
 * public static native long doubleToLongBits(double value)
 */
static jlong doubleToLongBits(JNIEnv* env, jclass clazz, jdouble val)
{
    Double   d;

    d.d = val;

    //  For this method all values in the NaN range are
    //  normalized to the canonical NaN value.

    if (isnan(d.d))
        d.bits = NaN;

    return d.bits;
}

/*
 * public static native long doubleToRawLongBits(double value)
 */
static jlong doubleToRawLongBits(JNIEnv* env, jclass clazz, jdouble val)
{
    Double   d;

    d.d = val;

    return d.bits;
}

/*
 * public static native double longBitsToDouble(long bits)
 */
static jdouble longBitsToDouble(JNIEnv* env, jclass clazz, jlong val)
{
    Double   d;

    d.bits = val;

    return d.d;
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "doubleToLongBits",       "(D)J",     doubleToLongBits },
    { "doubleToRawLongBits",    "(D)J",     doubleToRawLongBits },
    { "longBitsToDouble",       "(J)D",     longBitsToDouble },
};
int register_java_lang_Double(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/lang/Double",
                gMethods, NELEM(gMethods));
}


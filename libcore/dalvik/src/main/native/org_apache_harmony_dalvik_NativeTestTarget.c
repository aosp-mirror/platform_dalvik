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

#include "JNIHelp.h"

/*
 * public static void emptyJniStaticMethod0()
 *
 * For benchmarks, a do-nothing JNI method with no arguments.
 */
static void emptyJniStaticMethod0(JNIEnv* env, jclass clazz)
{
    // This space intentionally left blank.
}

/*
 * public static void emptyJniStaticMethod6(int a, int b, int c,
 *   int d, int e, int f)
 *
 * For benchmarks, a do-nothing JNI method with six arguments.
 */
static void emptyJniStaticMethod6(JNIEnv* env, jclass clazz,
    int a, int b, int c, int d, int e, int f)
{
    // This space intentionally left blank.
}

/*
 * public static void emptyJniStaticMethod6L(String a, String[] b,
 *   int[][] c, Object d, Object[] e, Object[][][][] f)
 *
 * For benchmarks, a do-nothing JNI method with six arguments.
 */
static void emptyJniStaticMethod6L(JNIEnv* env, jclass clazz,
    jobject a, jarray b, jarray c, jobject d, jarray e, jarray f)
{
    // This space intentionally left blank.
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "emptyJniStaticMethod0",  "()V",  emptyJniStaticMethod0 },
    { "emptyJniStaticMethod6",  "(IIIIII)V", emptyJniStaticMethod6 },
    { "emptyJniStaticMethod6L",
      "(Ljava/lang/String;[Ljava/lang/String;[[I"
      "Ljava/lang/Object;[Ljava/lang/Object;[[[[Ljava/lang/Object;)V",
      emptyJniStaticMethod6L },
};

int register_org_apache_harmony_dalvik_NativeTestTarget(JNIEnv* env)
{
    int result = jniRegisterNativeMethods(env,
            "org/apache/harmony/dalvik/NativeTestTarget",
            gMethods, NELEM(gMethods));
    if (result != 0) {
        /* print warning, but allow to continue */
        LOGW("WARNING: NativeTestTarget not registered\n");
        (*env)->ExceptionClear(env);
    }
    return 0;
}


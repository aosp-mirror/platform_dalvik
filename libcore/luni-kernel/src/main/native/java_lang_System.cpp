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

#include "AndroidSystemNatives.h"
#include "JNIHelp.h"

#include <stdlib.h>
#include <string.h>


/*
 * public static native String getEnvByName(String name)
 *
 * (Calling it plain "getenv" might confuse GDB if you try to put a breakpoint
 * on the libc version.)
 */
static jstring java_getEnvByName(JNIEnv* env, jclass, jstring nameStr) {
    jstring valueStr = NULL;

    if (nameStr != NULL) {
        const char* name = env->GetStringUTFChars(nameStr, NULL);
        const char* val = getenv(name);
        if (val != NULL) {
            valueStr = env->NewStringUTF(val);
        }
        env->ReleaseStringUTFChars(nameStr, name);
    } else {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
    }

    return valueStr;
}

/*
 * Pointer to complete environment, from Posix.
 */
extern char** environ;

/*
 * public static native String getEnvByIndex()
 *
 * (Calling it plain "getenv" might confuse GDB if you try to put a breakpoint
 * on the libc version.)
 */
static jstring java_getEnvByIndex(JNIEnv* env, jclass, jint index) {
    jstring valueStr = NULL;

    char* entry = environ[index];
    if (entry != NULL) {
        valueStr = env->NewStringUTF(entry);
    }

    return valueStr;
}

/*
 * public static native String setFieldImpl()
 *
 * Sets a field via JNI. Used for the standard streams, which are r/o
 * otherwise.
 */
static void java_setFieldImpl(JNIEnv* env, jclass clazz,
        jstring name, jstring sig, jobject object) {
    const char* fieldName = env->GetStringUTFChars(name, NULL);
    const char* fieldSig = env->GetStringUTFChars(sig, NULL);

    jfieldID fieldID = env->GetStaticFieldID(clazz, fieldName, fieldSig);
    env->SetStaticObjectField(clazz, fieldID, object);
    
    env->ReleaseStringUTFChars(name, fieldName);
    env->ReleaseStringUTFChars(sig, fieldSig);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getEnvByName",   "(Ljava/lang/String;)Ljava/lang/String;",                    (void*) java_getEnvByName  },
    { "getEnvByIndex",  "(I)Ljava/lang/String;",                                     (void*) java_getEnvByIndex },
    { "setFieldImpl",   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V", (void*) java_setFieldImpl },
};

int register_java_lang_System(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/lang/System",
            gMethods, NELEM(gMethods));
}

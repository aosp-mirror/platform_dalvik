/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"

static jobject java_io_osc_getFieldSignature(JNIEnv * env, jclass clazz,
                                                  jobject reflectField) {
    jclass lookupClass;
    jmethodID mid;

    lookupClass = (*env)->FindClass(env, "java/lang/reflect/Field");
    if(!lookupClass) {
        return NULL;
    }

    mid = (*env)->GetMethodID(env, lookupClass, "getSignature",
            "()Ljava/lang/String;");
    if(!mid)
    {
        return NULL;
    }

    jclass fieldClass = (*env)->GetObjectClass(env, reflectField);
    
    return (*env)->CallNonvirtualObjectMethod(env, reflectField, 
            fieldClass, mid);
}

static jobject java_io_osc_getMethodSignature(JNIEnv * env, jclass clazz,
                                                   jobject reflectMethod)
{
    jclass lookupClass;
    jmethodID mid;

    lookupClass = (*env)->FindClass(env, "java/lang/reflect/Method");
    if(!lookupClass) {
        return NULL;
    }

    mid = (*env)->GetMethodID(env, lookupClass, "getSignature",
            "()Ljava/lang/String;");
    if(!mid) {
        return NULL;
    }
  
    jclass methodClass = (*env)->GetObjectClass(env, reflectMethod);
    return (*env)->CallNonvirtualObjectMethod(env, reflectMethod, 
            methodClass, mid);
}

static jobject java_io_osc_getConstructorSignature(JNIEnv * env,
                                                        jclass clazz,
                                                        jobject
                                                        reflectConstructor)
{
    jclass lookupClass;
    jmethodID mid;

    lookupClass = (*env)->FindClass(env, "java/lang/reflect/Constructor");
    if(!lookupClass) {
        return NULL;
    }

    mid = (*env)->GetMethodID(env, lookupClass, "getSignature",
            "()Ljava/lang/String;");
    if(!mid) {
        return NULL;
    }

    jclass constructorClass = (*env)->GetObjectClass(env, reflectConstructor);
    return (*env)->CallNonvirtualObjectMethod(env, reflectConstructor,
                                             constructorClass, mid);
}

static jboolean java_io_osc_hasClinit(JNIEnv * env, jclass clazz,
                                          jobject targetClass) {
    jmethodID mid = (*env)->GetStaticMethodID(env, targetClass, 
            "<clinit>", "()V");
    (*env)->ExceptionClear(env);

    /* 
     * Can I just return mid and rely on typecast to convert to jboolean ? 
     * Safe implementation for now 
     */
    if(mid == 0) {
      /* No <clinit>... */
      return (jboolean) 0;
    } else {
      return (jboolean) 1;
    }
}

static void java_io_osc_oneTimeInitialization(JNIEnv * env, jclass clazz) {
  // dummy to stay compatible to harmony
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getFieldSignature",       
    	"(Ljava/lang/reflect/Field;)Ljava/lang/String;",
    	(void*) java_io_osc_getFieldSignature },
    { "getMethodSignature",      
    	"(Ljava/lang/reflect/Method;)Ljava/lang/String;",
    	(void*) java_io_osc_getMethodSignature },
    { "getConstructorSignature", 
    	"(Ljava/lang/reflect/Constructor;)Ljava/lang/String;",
    	(void*) java_io_osc_getConstructorSignature },
    { "hasClinit",               "(Ljava/lang/Class;)Z",
    	(void*) java_io_osc_hasClinit },
    { "oneTimeInitialization",   "()V",
    	(void*) java_io_osc_oneTimeInitialization }
};
int register_java_io_ObjectStreamClass(JNIEnv* env) {
	return jniRegisterNativeMethods(env, "java/io/ObjectStreamClass",
                gMethods, NELEM(gMethods));
}

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

static void java_setFieldBool (JNIEnv * env, jclass clazz, 
                                         jobject targetObject, 
                                         jclass declaringClass,
                                         jstring fieldName, 
                                         jboolean newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "Z");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetBooleanField(env, targetObject, fid, newValue);
    }
}

static void java_setFieldChar (JNIEnv * env, jclass clazz, 
                                         jobject targetObject, 
                                         jclass declaringClass,
                                         jstring fieldName, 
                                         jchar newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "C");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetCharField(env, targetObject, fid, newValue);
    }
}

static void java_setFieldInt (JNIEnv * env, jclass clazz, 
                                         jobject targetObject, 
                                         jclass declaringClass,
                                         jstring fieldName, 
                                         jint newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "I");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetIntField(env, targetObject, fid, newValue);
    }
}

static void java_setFieldFloat (JNIEnv * env, jclass clazz, 
                                         jobject targetObject, 
                                         jclass declaringClass,
                                         jstring fieldName, 
                                         jfloat newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "F");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetFloatField(env, targetObject, fid, newValue);
    }
}

static void java_setFieldDouble (JNIEnv * env, jclass clazz, 
                                         jobject targetObject, 
                                         jclass declaringClass,
                                         jstring fieldName, 
                                         jdouble newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "D");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetDoubleField(env, targetObject, fid, newValue);
    }

}

static void java_setFieldShort (JNIEnv * env, jclass clazz, 
                                         jobject targetObject, 
                                         jclass declaringClass,
                                         jstring fieldName, 
                                         jshort newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "S");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetShortField(env, targetObject, fid, newValue);
    }

}

static void java_setFieldLong (JNIEnv * env, jclass clazz,  
                                         jobject targetObject,  
                                         jclass declaringClass, 
                                         jstring fieldName,  
                                         jlong newValue) {
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "J");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /*
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetLongField(env, targetObject, fid, newValue);
    }
}

static jobject java_newInstance (JNIEnv * env, jclass clazz, 
                                         jclass instantiationClass, 
                                         jclass constructorClass) {
    jmethodID mid =
    (*env)->GetMethodID(env, constructorClass, "<init>", "()V");

    if(mid == 0) {
      /* Cant newInstance,No empty constructor... */
      return (jobject) 0;
    } else {
        /* Instantiate an object of a given class */
        return (jobject) (*env)->NewObject(env, instantiationClass, mid);
    }

}

static void java_setFieldByte (JNIEnv * env, jclass clazz,  
                                         jobject targetObject,  
                                         jclass declaringClass, 
                                         jstring fieldName,  
                                         jbyte newValue){
    const char *fieldNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "B");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* Two options now. Maybe getFieldID caused an exception, or maybe it returned the real value */
    if(fid != 0) {
        (*env)->SetByteField(env, targetObject, fid, newValue);
    }
}

static void java_setFieldObj (JNIEnv * env, jclass clazz,
                                            jobject targetObject,
                                            jclass declaringClass,
                                            jstring fieldName,
                                            jstring fieldTypeName,
                                            jobject newValue) {
    const char *fieldNameInC, *fieldTypeNameInC;
    jfieldID fid;
    if(targetObject == NULL) {
        return;
    }
    fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    fieldTypeNameInC = (*env)->GetStringUTFChars(env, fieldTypeName, NULL);
    fid = (*env)->GetFieldID(env, declaringClass, 
            fieldNameInC, fieldTypeNameInC);
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);
    (*env)->ReleaseStringUTFChars(env, fieldTypeName, fieldTypeNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid != 0) {
        (*env)->SetObjectField(env, targetObject, fid, newValue);
    }
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;J)V",
        (void*) java_setFieldLong },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;S)V",
        (void*) java_setFieldShort },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;D)V",
        (void*) java_setFieldDouble },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Z)V",
        (void*) java_setFieldBool },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;B)V",
        (void*) java_setFieldByte },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;F)V",
        (void*) java_setFieldFloat },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;C)V",
        (void*) java_setFieldChar },
    { "setField",          
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;I)V", 
        (void*) java_setFieldInt },
    { "newInstance",       
        "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;",
        (void*) java_newInstance },
    { "objSetField",       
        "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V",
        (void*) java_setFieldObj }

};
int register_java_io_ObjectInputStream(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/io/ObjectInputStream",
                gMethods, NELEM(gMethods));
}

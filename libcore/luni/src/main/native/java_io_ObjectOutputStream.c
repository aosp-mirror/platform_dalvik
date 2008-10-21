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

static jlong java_getFieldLong(JNIEnv * env, jclass clazz,
                                              jobject targetObject,
                                              jclass declaringClass,
                                              jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "J");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid == 0) {
    	// Field not found. I believe we must throw an exception here
        return (jlong) 0L;
    } else {
        return (*env)->GetLongField (env, targetObject, fid);
    }
}

static jshort java_getFieldShort(JNIEnv * env, jclass clazz,
                                               jobject targetObject,
                                               jclass declaringClass,
                                               jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "S");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jshort) 0;
    } else {
        return (*env)->GetShortField (env, targetObject, fid);
    }
}

static jdouble java_getFieldDouble(JNIEnv * env, jclass clazz,
                                                jobject targetObject,
                                                jclass declaringClass,
                                                jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "D");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jdouble) 0.0;
    } else {
        return (*env)->GetDoubleField (env, targetObject, fid);
    }
}

static jboolean java_getFieldBool(JNIEnv * env, jclass clazz,
                                              jobject targetObject,
                                              jclass declaringClass,
                                              jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "Z");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jboolean) 0;
    } else {
        return (*env)->GetBooleanField (env, targetObject, fid);
    }
}

static jbyte java_getFieldByte(JNIEnv * env, jclass clazz,
                                              jobject targetObject,
                                              jclass declaringClass,
                                              jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "B");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jbyte) 0;
    } else {
        return (*env)->GetByteField (env, targetObject, fid);
    }
}

static jfloat java_getFieldFloat(JNIEnv * env, jclass clazz,
                                               jobject targetObject,
                                               jclass declaringClass,
                                               jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "F");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value 
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jfloat) 0.0f;
    }
    else {
        return (*env)->GetFloatField (env, targetObject, fid);
    }

}

static jchar java_getFieldChar(JNIEnv * env, jclass clazz,
                                              jobject targetObject,
                                              jclass declaringClass,
                                              jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "C");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception, 
     * or maybe it returned the real value
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jchar) 0;
    } else  {
        return (*env)->GetCharField(env, targetObject, fid);
    }
}

static jobject java_getFieldObj(JNIEnv * env, jclass clazz,
                                             jobject targetObject,
                                             jclass declaringClass,
                                             jstring fieldName,
                                             jstring fieldTypeName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    const char *fieldTypeNameInC =
    (*env)->GetStringUTFChars(env, fieldTypeName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, 
            fieldNameInC, fieldTypeNameInC);
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);
    (*env)->ReleaseStringUTFChars(env, fieldTypeName, fieldTypeNameInC);

    /* 
     * Two options now. Maybe getFieldID caused an exception,
     * or maybe it returned the real value 
     */
    if(fid == 0) {
        // Field not found. I believe we must throw an exception here
        return (jobject) 0;
    } else {
        return (*env)->GetObjectField (env, targetObject, fid);
    }
}

static jint java_getFieldInt(JNIEnv * env, jclass clazz,
                                             jobject targetObject,
                                             jclass declaringClass,
                                             jstring fieldName) {
    const char *fieldNameInC = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, declaringClass, fieldNameInC, "I");
    (*env)->ReleaseStringUTFChars(env, fieldName, fieldNameInC);

    /* 
     * Two options now. Maybe getFieldID caused 
     * an exception, or maybe it returned the real value 
     */
     if(fid == 0) {
       // Field not found. I believe we must throw an exception here
        return (jint) 0;
    } else {
        return (*env)->GetIntField(env, targetObject, fid);
    }
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getFieldLong",   
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)J",
    	(void*) java_getFieldLong },
    { "getFieldShort",  
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)S",
    	(void*) java_getFieldShort },
    { "getFieldDouble", 
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)D",
    	(void*) java_getFieldDouble },
    { "getFieldBool",   
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)Z",
    	(void*) java_getFieldBool },
    { "getFieldByte",   
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)B",
    	(void*) java_getFieldByte },
    { "getFieldFloat",  
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)F",
    	(void*) java_getFieldFloat },
    { "getFieldChar",   
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)C",
    	(void*) java_getFieldChar },
    { "getFieldObj",    
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
    	(void*) java_getFieldObj },
    { "getFieldInt",    
    	"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)I",
    	(void*) java_getFieldInt },

};
int register_java_io_ObjectOutputStream(JNIEnv* env) {
	return jniRegisterNativeMethods(env, "java/io/ObjectOutputStream",
                gMethods, NELEM(gMethods));
}


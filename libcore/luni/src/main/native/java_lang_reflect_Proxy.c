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

#if 0   // BEGIN android-changed

static jclass java_lang_reflect_defineClassImpl(JNIEnv * env, 
        jclass recvClass, jobject classLoader, jstring className, 
        jbyteArray classBytes) {
    const char *name;
    jbyte *bytes;
    jclass returnClass;
    jint length;

    name = (*env)->GetStringUTFChars (env, className, NULL);
    if (!name)
    {
        jniThrowException(env, "java/lang/VirtualMachineError", NULL);
        return 0;
    };
    bytes = (*env)->GetByteArrayElements (env, classBytes, NULL);
    if (!bytes)
    {
        (*env)->ReleaseStringUTFChars (env, className, name);
        jniThrowException(env, "java/lang/VirtualMachineError", NULL);
        return 0;
    }
    length = (*env)->GetArrayLength (env, classBytes);

    returnClass = (*env)->DefineClass (env, name, classLoader, bytes, length);

    (*env)->ReleaseByteArrayElements (env, classBytes, bytes, JNI_COMMIT);
    (*env)->ReleaseStringUTFChars (env, className, name);
    return returnClass;
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "defineClassImpl", 
            "(Ljava/lang/ClassLoader;Ljava/lang/String;[B)Ljava/lang/Class;",  
            (void*) java_lang_reflect_defineClassImpl }
};

int register_java_lang_reflect_Proxy(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/lang/reflect/Proxy",
                gMethods, NELEM(gMethods));
}

#endif  // END android-changed

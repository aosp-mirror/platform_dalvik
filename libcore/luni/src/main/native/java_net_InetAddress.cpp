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

#define LOG_TAG "InetAddress"

#include "JNIHelp.h"
#include "utils/Log.h"
#include "jni.h"

#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <netdb.h>

#include <cutils/properties.h>
#include <cutils/adb_networking.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>


static jstring InetAddress_gethostname(JNIEnv* env, jobject obj)
{
    char name[256];
    int r = gethostname(name, 256);
    if (r == 0) {
        return env->NewStringUTF(name);
    } else {
        return NULL;
    }
}

static void throwNullPointerException(JNIEnv *env)
{
    const char *className = "java/lang/NullPointerException";

    jclass exClass = env->FindClass(className);

    if (exClass == NULL) {
        LOGE("Unable to find class %s", className);
    } else {
        env->ThrowNew(exClass, NULL);
    }
}


static jboolean InetAddress_gethostbyname(JNIEnv* env, jobject obj, jstring nameStr, jbyteArray addr)
{
    if (addr == NULL || 4 != env->GetArrayLength(addr)) {
        return false;
    }

    if (nameStr == NULL) {
        throwNullPointerException(env);
        return false;
    }
    
    jboolean ret;
    const char* name = env->GetStringUTFChars(nameStr, NULL);

    char useAdbNetworkingProperty[PROPERTY_VALUE_MAX];
    char adbConnected[PROPERTY_VALUE_MAX];

    property_get ("android.net.use-adb-networking", 
            useAdbNetworkingProperty, "");

    property_get ("adb.connected", 
            adbConnected, "");

    if ((strlen(useAdbNetworkingProperty) > 0) 
            && (strlen(adbConnected) > 0) ) {
        // Any non-empty string value for use-adb-networking is considered "set"
        union {
            struct in_addr a;
            jbyte j[4];
        } outaddr;

        //LOGI("ADB networking: +gethostbyname '%s'", name);
        int err;
        err = adb_networking_gethostbyname(name, &(outaddr.a));
#if 0
        LOGI("ADB networking: -gethostbyname err %d addr 0x%08x %u.%u.%u.%u", 
                err, (unsigned int)outaddr.a.s_addr, 
                outaddr.j[0],outaddr.j[1],
                outaddr.j[2],outaddr.j[3]);
#endif

        if (err < 0) {
            ret = false;
        } else {
            ret = true;
            env->SetByteArrayRegion(addr, 0, 4, outaddr.j);            
        }
    } else {
        // normal case...no adb networking
        struct hostent* ent = gethostbyname(name);
                
        if (ent != NULL  && ent->h_length > 0) {
            jbyte v[4];
            memcpy(v, ent->h_addr, 4);
            env->SetByteArrayRegion(addr, 0, 4, v);
            ret = true;
        } else {
            ret = false;
        }
    }

    env->ReleaseStringUTFChars(nameStr, name);

    return ret;

}


static jstring InetAddress_gethostbyaddr(JNIEnv* env, jobject obj, jstring addrStr)
{
    if (addrStr == NULL) {
        throwNullPointerException(env);
        return false;
    }
    
    jstring result;
    const char* addr = env->GetStringUTFChars(addrStr, NULL);

    struct hostent* ent = gethostbyaddr(addr, strlen(addr), AF_INET);
            
    if (ent != NULL  && ent->h_name != NULL) {
        result = env->NewStringUTF(ent->h_name);
    } else {
        result = NULL;
    }

    env->ReleaseStringUTFChars(addrStr, addr);

    return result;
}


static jobjectArray InetAddress_getaliasesbyname(JNIEnv* env, jobject obj, jstring nameStr)
{
    if (nameStr == NULL) {
        throwNullPointerException(env);
        return NULL;
    }

    jclass clazz = env->FindClass("java/lang/String");
    if(clazz == NULL) {
        jniThrowException(env, "java/lang/ClassNotFoundException", "couldn't find class java.lang.String");
        return NULL;
    }

    jobjectArray result;

    const char* name = env->GetStringUTFChars(nameStr, NULL);

    struct hostent* ent = gethostbyname(name);
       
    if (ent != NULL) {
        // Count aliases
        int count = 0;
        while (ent->h_aliases[count] != NULL) {
             count++;
        }
     
        // Create an array of String objects and fill it.
        result = env->NewObjectArray(count, clazz, NULL);
        int i;
        for (i = 0; i < count; i++) {
            env->SetObjectArrayElement(result, i, env->NewStringUTF(ent->h_aliases[i]));
        }
    } else {
        result = env->NewObjectArray(0, clazz, NULL);
    }

    env->ReleaseStringUTFChars(nameStr, name);

    return result;
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getaliasesbyname", "(Ljava/lang/String;)[Ljava/lang/String;",
      (void*) InetAddress_getaliasesbyname },
    { "gethostbyaddr",    "(Ljava/lang/String;)Ljava/lang/String;",
      (void*) InetAddress_gethostbyaddr },
    { "gethostbyname",    "(Ljava/lang/String;[B)Z",
      (void*) InetAddress_gethostbyname },
    { "gethostname",      "()Ljava/lang/String;",
      (void*) InetAddress_gethostname  }
};

extern "C" int register_java_net_InetAddress(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/net/InetAddress",
                gMethods, NELEM(gMethods));
}

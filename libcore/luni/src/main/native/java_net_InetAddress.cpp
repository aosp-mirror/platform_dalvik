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
#include <errno.h>

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

static void throwNullPointerException(JNIEnv* env)
{
    const char* className = "java/lang/NullPointerException";

    jclass exClass = env->FindClass(className);

    if (exClass == NULL) {
        LOGE("Unable to find class %s", className);
    } else {
        env->ThrowNew(exClass, NULL);
    }
}

static jbyteArray getHostByNameAdb(JNIEnv* env, const char* name)
{
    struct in_addr outaddr;
    jbyteArray out = NULL;

#if 0
    LOGI("ADB networking: -gethostbyname err %d addr 0x%08x %u.%u.%u.%u",
            err, (unsigned int)outaddr.a.s_addr,
            outaddr.j[0],outaddr.j[1],
            outaddr.j[2],outaddr.j[3]);
#endif

    if (adb_networking_gethostbyname(name, &outaddr) >= 0) {
        out = env->NewByteArray(4);
        env->SetByteArrayRegion(out, 0, 4, (jbyte*) &outaddr.s_addr);
    }

    return out;
}

static jbyteArray getHostByNameGetAddrInfo(JNIEnv* env, const char* name, jboolean preferIPv6Address)
{
    struct addrinfo hints, *res = NULL;
    jbyteArray out = NULL;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = preferIPv6Address ? AF_UNSPEC : AF_INET;

    int ret = getaddrinfo(name, NULL, &hints, &res);
    if (ret == 0  && res) {
        struct sockaddr* saddr = res[0].ai_addr;
        size_t addrlen = 0;
        void* rawaddr;

        switch (res[0].ai_family) {
            // Find the raw address length and start pointer.
            case AF_INET6:
                addrlen = 16;
                rawaddr = &((struct sockaddr_in6*) saddr)->sin6_addr.s6_addr;
                break;
            case AF_INET:
                addrlen = 4;
                rawaddr = &((struct sockaddr_in*) saddr)->sin_addr.s_addr;
                break;
            default:
                // Do nothing. addrlength = 0, so we will return NULL.
                break;
        }

        if (addrlen) {
            out = env->NewByteArray(addrlen);
            env->SetByteArrayRegion(out, 0, addrlen, (jbyte*) rawaddr);
        }
    } else if (ret == EAI_SYSTEM && errno == EACCES) {
        /* No permission to use network */
        jniThrowException(
                env, "java/lang/SecurityException",
                "Permission denied (maybe missing INTERNET permission)");
    }

    if (res) {
        freeaddrinfo(res);
    }

    return out;
}

jbyteArray InetAddress_gethostbyname(JNIEnv* env, jobject obj, jstring nameStr, jboolean preferIPv6Address)
{
    if (nameStr == NULL) {
        throwNullPointerException(env);
        return 0;
    }

    const char* name = env->GetStringUTFChars(nameStr, NULL);
    jbyteArray out = NULL;

    char useAdbNetworkingProperty[PROPERTY_VALUE_MAX];
    char adbConnected[PROPERTY_VALUE_MAX];
    property_get ("android.net.use-adb-networking",
            useAdbNetworkingProperty, "");
    property_get ("adb.connected",
            adbConnected, "");

    // Any non-empty string value for use-adb-networking is considered "set"
    if ((strlen(useAdbNetworkingProperty) > 0)
            && (strlen(adbConnected) > 0) ) {
        out = getHostByNameAdb(env, name);
    } else {
        out = getHostByNameGetAddrInfo(env, name, preferIPv6Address);
    }

    if (!out) {
        LOGI("Unknown host %s, throwing UnknownHostException", name);
        jniThrowException(env, "java/net/UnknownHostException", name);
    }
    env->ReleaseStringUTFChars(nameStr, name);
    return out;
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
    if (clazz == NULL) {
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
    { "gethostbyname",    "(Ljava/lang/String;Z)[B",
      (void*) InetAddress_gethostbyname },
    { "gethostname",      "()Ljava/lang/String;",
      (void*) InetAddress_gethostname  }
};

extern "C" int register_java_net_InetAddress(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/net/InetAddress",
                gMethods, NELEM(gMethods));
}

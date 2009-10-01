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

#define LOG_DNS 0

#include "JNIHelp.h"
#include "utils/Log.h"
#include "jni.h"

#include <stdio.h>
#include <string.h>
#include <netdb.h>
#include <errno.h>

#include <cutils/properties.h>
#include <cutils/adb_networking.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>


static jclass byteArrayClass = NULL;

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

#if LOG_DNS
static void logIpString(struct addrinfo* ai, const char* name)
{
    char ipString[INET6_ADDRSTRLEN];
    int result = getnameinfo(ai->ai_addr, ai->ai_addrlen, ipString,
                             sizeof(ipString), NULL, 0, NI_NUMERICHOST);
    if (result == 0) {
        LOGD("%s: %s (family %d, proto %d)", name, ipString, ai->ai_family,
             ai->ai_protocol);
    } else {
        LOGE("%s: getnameinfo: %s", name, gai_strerror(result));
    }
}
#else
static inline void logIpString(struct addrinfo* ai, const char* name)
{
}
#endif

static jobjectArray getAllByNameUsingAdb(JNIEnv* env, const char* name)
{
    struct in_addr outaddr;
    jobjectArray addressArray = NULL;
    jbyteArray byteArray;

#if 0
    LOGI("ADB networking: -gethostbyname err %d addr 0x%08x %u.%u.%u.%u",
            err, (unsigned int)outaddr.a.s_addr,
            outaddr.j[0],outaddr.j[1],
            outaddr.j[2],outaddr.j[3]);
#endif

    if (adb_networking_gethostbyname(name, &outaddr) >= 0) {
        addressArray = env->NewObjectArray(1, byteArrayClass, NULL);
        byteArray = env->NewByteArray(4);
        if (addressArray && byteArray) {
            env->SetByteArrayRegion(byteArray, 0, 4, (jbyte*) &outaddr.s_addr);
            env->SetObjectArrayElement(addressArray, 1, byteArray);
        }
    } else {
        jniThrowException(env, "java/net/UnknownHostException", "adb error");
    }

    return addressArray;
}

static jobjectArray getAllByNameUsingDns(JNIEnv* env, const char* name, 
                                         jboolean preferIPv4Stack)
{
    struct addrinfo hints, *addressList = NULL, *addrInfo;
    jobjectArray addressArray = NULL;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_flags = AI_ADDRCONFIG;
    /*
     * If we don't specify a socket type, every address will appear twice, once
     * for SOCK_STREAM and one for SOCK_DGRAM. Since we do not return the family
     * anyway, just pick one.
     */
    hints.ai_socktype = SOCK_STREAM;

    int result = getaddrinfo(name, NULL, &hints, &addressList);
    if (result == 0 && addressList) {
        // Count results so we know how to size the output array.
        int addressCount = 0;
        for (addrInfo = addressList; addrInfo; addrInfo = addrInfo->ai_next) {
            if (addrInfo->ai_family == AF_INET ||
                addrInfo->ai_family == AF_INET6) {
                addressCount++;
            }
        }

        // Prepare output array.
        addressArray = env->NewObjectArray(addressCount, byteArrayClass, NULL);
        if (addressArray == NULL) {
            // Appropriate exception will be thrown.
            LOGE("getAllByNameUsingDns: could not allocate output array");
            freeaddrinfo(addrInfo);
            return NULL;
        }

        // Examine returned addresses one by one, save them in the output array.
        int index = 0;
        for (addrInfo = addressList; addrInfo; addrInfo = addrInfo->ai_next) {
            struct sockaddr* address = addrInfo->ai_addr;
            size_t addressLength = 0;
            void* rawAddress;

            switch (addrInfo->ai_family) {
                // Find the raw address length and start pointer.
                case AF_INET6:
                    addressLength = 16;
                    rawAddress =
                        &((struct sockaddr_in6*) address)->sin6_addr.s6_addr;
                    logIpString(addrInfo, name);
                    break;
                case AF_INET:
                    addressLength = 4;
                    rawAddress =
                        &((struct sockaddr_in*) address)->sin_addr.s_addr;
                    logIpString(addrInfo, name);
                    break;
                default:
                    // Unknown address family. Skip this address.
                    LOGE("getAllByNameUsingDns: Unknown address family %d",
                         addrInfo->ai_family);
                    continue;
            }

            // Convert each IP address into a Java byte array.
            jbyteArray bytearray = env->NewByteArray(addressLength);
            if (bytearray == NULL) {
                // Out of memory error will be thrown on return.
                LOGE("getAllByNameUsingDns: Can't allocate %d-byte array",
                     addressLength);
                addressArray = NULL;
                break;
            }
            env->SetByteArrayRegion(bytearray, 0, addressLength,
                                    (jbyte*) rawAddress);
            env->SetObjectArrayElement(addressArray, index, bytearray);
            env->DeleteLocalRef(bytearray);
            index++;
        }
    } else if (result == EAI_SYSTEM && errno == EACCES) {
        /* No permission to use network */
        jniThrowException(
            env, "java/lang/SecurityException",
            "Permission denied (maybe missing INTERNET permission)");
    } else {
        jniThrowException(env, "java/net/UnknownHostException",
                gai_strerror(result));
    }

    if (addressList) {
        freeaddrinfo(addressList);
    }

    return addressArray;
}

jobjectArray InetAddress_getallbyname(JNIEnv* env, jobject obj,
                                      jstring javaName,
                                      jboolean preferIPv4Stack)
{
    if (javaName == NULL) {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
        return NULL;
    }

    const char* name = env->GetStringUTFChars(javaName, NULL);
    jobjectArray out = NULL;

    char useAdbNetworkingProperty[PROPERTY_VALUE_MAX];
    char adbConnected[PROPERTY_VALUE_MAX];
    property_get("android.net.use-adb-networking",
            useAdbNetworkingProperty, "");
    property_get("adb.connected",
            adbConnected, "");

    // Any non-empty string value for use-adb-networking is considered "set"
    if ((strlen(useAdbNetworkingProperty) > 0)
            && (strlen(adbConnected) > 0) ) {
        out = getAllByNameUsingAdb(env, name);
    } else {
        out = getAllByNameUsingDns(env, name, preferIPv4Stack);
    }

    env->ReleaseStringUTFChars(javaName, name);
    return out;
}


/**
 * Looks up the name corresponding to an IP address.
 *
 * @param javaAddress: a byte array containing the raw IP address bytes. Must be
 *         4 or 16 bytes long.
 * @return the hostname.
 * @throws UnknownHostException: the IP address has no associated hostname.
 */
static jstring InetAddress_gethostbyaddr(JNIEnv* env, jobject obj,
                                         jbyteArray javaAddress)
{
    if (javaAddress == NULL) {
        jniThrowException(env, "java/lang/NullPointerException", NULL);
        return NULL;
    }

    // Convert the raw address bytes into a socket address structure.
    struct sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));

    size_t socklen;
    const size_t addressLength = env->GetArrayLength(javaAddress);
    if (addressLength == 4) {
        struct sockaddr_in* sin = reinterpret_cast<sockaddr_in*>(&ss);
        sin->sin_family = AF_INET;
        socklen = sizeof(struct sockaddr_in);
        jbyte* dst = reinterpret_cast<jbyte*>(&sin->sin_addr.s_addr);
        env->GetByteArrayRegion(javaAddress, 0, 4, dst);
    } else if (addressLength == 16) {
        struct sockaddr_in6 *sin6 = reinterpret_cast<sockaddr_in6*>(&ss);
        sin6->sin6_family = AF_INET6;
        socklen = sizeof(struct sockaddr_in6);
        jbyte* dst = reinterpret_cast<jbyte*>(&sin6->sin6_addr.s6_addr);
        env->GetByteArrayRegion(javaAddress, 0, 16, dst);
    } else {
        // The caller already throws an exception in this case. Don't worry
        // about it here.
        return NULL;
    }

    // Look up the host name from the IP address.
    char name[NI_MAXHOST];
    int ret = getnameinfo(reinterpret_cast<sockaddr*>(&ss), socklen,
                          name, sizeof(name), NULL, 0, NI_NAMEREQD);
    if (ret != 0) {
        jniThrowException(env, "java/net/UnknownHostException", gai_strerror(ret));
        return NULL;
    }

    return env->NewStringUTF(name);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "gethostbyaddr",    "([B)Ljava/lang/String;",
      (void*) InetAddress_gethostbyaddr },
    { "getallbyname",     "(Ljava/lang/String;Z)[[B",
      (void*) InetAddress_getallbyname },
    { "gethostname",      "()Ljava/lang/String;",
      (void*) InetAddress_gethostname  },
};

extern "C" int register_java_net_InetAddress(JNIEnv* env)
{
    jclass tempClass = env->FindClass("[B");
    if (tempClass) {
        byteArrayClass = (jclass) env->NewGlobalRef(tempClass);
    }
    if (!byteArrayClass) {
        LOGE("register_java_net_InetAddress: cannot allocate byte array class");
        return -1;
    }
    return jniRegisterNativeMethods(env, "java/net/InetAddress",
                gMethods, NELEM(gMethods));
}

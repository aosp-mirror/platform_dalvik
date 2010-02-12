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

#include "AndroidSystemNatives.h"
#include "JNIHelp.h"
#include "jni.h"

#include <errno.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <unistd.h>

#include <net/if.h> // Note: Can't appear before <sys/socket.h> on OS X.

#ifdef HAVE_ANDROID_OS
#include "ifaddrs-android.h"
#else
#include <ifaddrs.h>
#endif

// Ensures we always call freeifaddrs(3) to clean up after getifaddrs(3).
class ScopedInterfaceAddresses {
public:
    ScopedInterfaceAddresses() : list(NULL) {
    }
    
    bool init() {
        int rc = getifaddrs(&list);
        return (rc != -1);
    }
    
    ~ScopedInterfaceAddresses() {
        freeifaddrs(list);
    }
    
    ifaddrs* list;
};

// TODO: add a header file for shared utilities like this.
extern jobject socketAddressToInetAddress(JNIEnv* env, sockaddr_storage* sockAddress);

// TODO(enh): move to JNIHelp.h
static void jniThrowSocketException(JNIEnv* env) {
    char buf[BUFSIZ];
    jniThrowException(env, "java/net/SocketException",
            jniStrError(errno, buf, sizeof(buf)));
}

static jobject makeInterfaceAddress(JNIEnv* env, jint interfaceIndex, const char* name, sockaddr_storage* ss) {
    jclass clazz = env->FindClass("java/net/InterfaceAddress");
    if (clazz == NULL) {
        return NULL;
    }
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(ILjava/lang/String;Ljava/net/InetAddress;)V");
    if (constructor == NULL) {
        return NULL;
    }
    jobject javaName = env->NewStringUTF(name);
    if (javaName == NULL) {
        return NULL;
    }
    jobject javaAddress = socketAddressToInetAddress(env, ss);
    if (javaAddress == NULL) {
        return NULL;
    }
    return env->NewObject(clazz, constructor, interfaceIndex, javaName, javaAddress);
}

static jobjectArray getInterfaceAddresses(JNIEnv* env, jclass) {
    // Get the list of interface addresses.
    ScopedInterfaceAddresses addresses;
    if (!addresses.init()) {
        jniThrowSocketException(env);
        return NULL;
    }
    
    // Count how many there are.
    int interfaceAddressCount = 0;
    for (ifaddrs* ifa = addresses.list; ifa != NULL; ifa = ifa->ifa_next) {
        ++interfaceAddressCount;
    }
    
    // Build the InterfaceAddress[]...
    jclass interfaceAddressClass = env->FindClass("java/net/InterfaceAddress");
    if (interfaceAddressClass == NULL) {
        return NULL;
    }
    jobjectArray result = env->NewObjectArray(interfaceAddressCount, interfaceAddressClass, NULL);
    if (result == NULL) {
        return NULL;
    }
    
    // And fill it in...
    int arrayIndex = 0;
    for (ifaddrs* ifa = addresses.list; ifa != NULL; ifa = ifa->ifa_next) {
        // We're only interested in IP addresses.
        int family = ifa->ifa_addr->sa_family;
        if (family != AF_INET && family != AF_INET6) {
            continue;
        }
        // Until we implement Java 6's NetworkInterface.isUp,
        // we only want interfaces that are up.
        if ((ifa->ifa_flags & IFF_UP) == 0) {
            continue;
        }
        // Find the interface's index, and skip this address if
        // the interface has gone away.
        int interfaceIndex = if_nametoindex(ifa->ifa_name);
        if (interfaceIndex == 0) {
            continue;
        }
        // Make a new InterfaceAddress, and insert it into the array.
        sockaddr_storage* ss = reinterpret_cast<sockaddr_storage*>(ifa->ifa_addr);
        jobject element = makeInterfaceAddress(env, interfaceIndex, ifa->ifa_name, ss);
        if (element == NULL) {
            return NULL;
        }
        env->SetObjectArrayElement(result, arrayIndex, element);
        if (env->ExceptionCheck()) {
            return NULL;
        }
        ++arrayIndex;
    }
    return result;
}

static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getInterfaceAddresses", "()[Ljava/net/InterfaceAddress;", (void*) getInterfaceAddresses },
};
int register_java_net_NetworkInterface(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/net/NetworkInterface",
            gMethods, NELEM(gMethods));
}

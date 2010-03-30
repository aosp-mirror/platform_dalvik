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
#include "ScopedFd.h"

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

private:
    // Disallow copy and assignment.
    ScopedInterfaceAddresses(const ScopedInterfaceAddresses&);
    void operator=(const ScopedInterfaceAddresses&);
};

// TODO: add a header file for shared utilities like this.
extern jobject socketAddressToInetAddress(JNIEnv* env, sockaddr_storage* sockAddress);

// TODO(enh): move to JNIHelp.h
static void jniThrowSocketException(JNIEnv* env) {
    char buf[BUFSIZ];
    jniThrowException(env, "java/net/SocketException",
            jniStrError(errno, buf, sizeof(buf)));
}

static jobject makeInterfaceAddress(JNIEnv* env, jint interfaceIndex, ifaddrs* ifa) {
    jclass clazz = env->FindClass("java/net/InterfaceAddress");
    if (clazz == NULL) {
        return NULL;
    }
    jmethodID constructor = env->GetMethodID(clazz, "<init>",
            "(ILjava/lang/String;Ljava/net/InetAddress;Ljava/net/InetAddress;)V");
    if (constructor == NULL) {
        return NULL;
    }
    jobject javaName = env->NewStringUTF(ifa->ifa_name);
    if (javaName == NULL) {
        return NULL;
    }
    sockaddr_storage* addr = reinterpret_cast<sockaddr_storage*>(ifa->ifa_addr);
    jobject javaAddress = socketAddressToInetAddress(env, addr);
    if (javaAddress == NULL) {
        return NULL;
    }
    sockaddr_storage* mask = reinterpret_cast<sockaddr_storage*>(ifa->ifa_netmask);
    jobject javaMask = socketAddressToInetAddress(env, mask);
    if (javaMask == NULL) {
        return NULL;
    }
    return env->NewObject(clazz, constructor, interfaceIndex, javaName, javaAddress, javaMask);
}

static jobjectArray getAllInterfaceAddressesImpl(JNIEnv* env, jclass) {
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
        jobject element = makeInterfaceAddress(env, interfaceIndex, ifa);
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

static bool doIoctl(JNIEnv* env, jstring name, int request, ifreq& ifr) {
    // Copy the name into the ifreq structure, if there's room...
    jsize nameLength = env->GetStringLength(name);
    if (nameLength >= IFNAMSIZ) {
        errno = ENAMETOOLONG;
        jniThrowSocketException(env);
        return false;
    }
    memset(&ifr, 0, sizeof(ifr));
    env->GetStringUTFRegion(name, 0, nameLength, ifr.ifr_name);

    // ...and do the ioctl.
    ScopedFd fd(socket(AF_INET, SOCK_DGRAM, 0));
    if (fd.get() == -1) {
        jniThrowSocketException(env);
        return false;
    }
    int rc = ioctl(fd.get(), request, &ifr);
    if (rc == -1) {
        jniThrowSocketException(env);
        return false;
    }
    return true;
}

static jboolean hasFlag(JNIEnv* env, jstring name, int flag) {
    ifreq ifr;
    doIoctl(env, name, SIOCGIFFLAGS, ifr); // May throw.
    return (ifr.ifr_flags & flag) != 0;
}

static jbyteArray getHardwareAddressImpl(JNIEnv* env, jclass, jstring name, jint index) {
    ifreq ifr;
    if (!doIoctl(env, name, SIOCGIFHWADDR, ifr)) {
        return NULL;
    }
    jbyte bytes[IFHWADDRLEN];
    bool isEmpty = true;
    for (int i = 0; i < IFHWADDRLEN; ++i) {
        bytes[i] = ifr.ifr_hwaddr.sa_data[i];
        if (bytes[i] != 0) {
            isEmpty = false;
        }
    }
    if (isEmpty) {
        return NULL;
    }
    jbyteArray result = env->NewByteArray(IFHWADDRLEN);
    env->SetByteArrayRegion(result, 0, IFHWADDRLEN, bytes);
    return result;
}

static jint getMTUImpl(JNIEnv* env, jclass, jstring name, jint index) {
    ifreq ifr;
    doIoctl(env, name, SIOCGIFMTU, ifr); // May throw.
    return ifr.ifr_mtu;
}

static jboolean isLoopbackImpl(JNIEnv* env, jclass, jstring name, jint index) {
    return hasFlag(env, name, IFF_LOOPBACK);
}

static jboolean isPointToPointImpl(JNIEnv* env, jclass, jstring name, jint index) {
    return hasFlag(env, name, IFF_POINTOPOINT); // Unix API typo!
}

static jboolean isUpImpl(JNIEnv* env, jclass, jstring name, jint index) {
    return hasFlag(env, name, IFF_UP);
}

static jboolean supportsMulticastImpl(JNIEnv* env, jclass, jstring name, jint index) {
    return hasFlag(env, name, IFF_MULTICAST);
}

static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getAllInterfaceAddressesImpl", "()[Ljava/net/InterfaceAddress;", (void*) getAllInterfaceAddressesImpl },
    { "getHardwareAddressImpl", "(Ljava/lang/String;I)[B", (void*) getHardwareAddressImpl },
    { "getMTUImpl", "(Ljava/lang/String;I)I", (void*) getMTUImpl },
    { "isLoopbackImpl", "(Ljava/lang/String;I)Z", (void*) isLoopbackImpl },
    { "isPointToPointImpl", "(Ljava/lang/String;I)Z", (void*) isPointToPointImpl },
    { "isUpImpl", "(Ljava/lang/String;I)Z", (void*) isUpImpl },
    { "supportsMulticastImpl", "(Ljava/lang/String;I)Z", (void*) supportsMulticastImpl },
};
int register_java_net_NetworkInterface(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/net/NetworkInterface",
            gMethods, NELEM(gMethods));
}

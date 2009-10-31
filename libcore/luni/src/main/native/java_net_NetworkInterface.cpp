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
#include <net/if.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <unistd.h>

// A smart pointer that closes the given fd on going out of scope.
// TODO: make this generally available.
class scoped_fd {
public:
    explicit scoped_fd(int fd) : fd(fd) {
    }

    ~scoped_fd() {
        close(fd);
    }

    int get() const {
        return fd;
    }

private:
    int fd;
};

// TODO: add a header file for shared utilities like this.
extern jobject socketAddressToInetAddress(JNIEnv* env, sockaddr_storage* sockAddress);

class NetworkInterfaceGetter {
public:
    NetworkInterfaceGetter() : interfaces(NULL) {
        // Initialize this so we can be responsible for deleting it.
        ifc.ifc_buf = NULL;
    }

    ~NetworkInterfaceGetter() {
        delete[] ifc.ifc_buf;
    }

    jobjectArray getNetworkInterfaces(JNIEnv* env);

private:
    jobjectArray interfaces;
    ifconf ifc;
};

// TODO: move to JNIHelp?
static void jniThrowOutOfMemoryError(JNIEnv* env) {
    jniThrowException(env, "java/lang/OutOfMemoryError", "native heap");
}

// TODO(enh): move to JNIHelp.h
static void jniThrowSocketException(JNIEnv* env) {
    char buf[BUFSIZ];
    jniThrowException(env, "java/net/SocketException",
            jniStrError(errno, buf, sizeof(buf)));
}

// Creates an InetAddress[] of size 'addressCount' from the ifc_req structs
// starting at index 'startIndex' in 'ifc.ifc_req'.
static jobjectArray MakeInetAddressArray(JNIEnv* env,
        const ifconf& ifc, size_t startIndex, size_t addressCount) {
    jclass inetAddressClass = env->FindClass("java/net/InetAddress");
    if (inetAddressClass == NULL) {
        return NULL;
    }
    jobjectArray addresses = env->NewObjectArray(addressCount, inetAddressClass, NULL);
    if (addresses == NULL) {
        return NULL;
    }
    for (size_t i = startIndex; i < startIndex + addressCount; ++i) {
        sockaddr_storage* sockAddress =
                reinterpret_cast<sockaddr_storage*>(&ifc.ifc_req[i].ifr_addr);
        jobject element = socketAddressToInetAddress(env, sockAddress);
        if (element == NULL) {
            return NULL;
        }
        env->SetObjectArrayElement(addresses, i - startIndex, element);
        if (env->ExceptionCheck()) {
            return NULL;
        }
    }
    return addresses;
}

// Creates a NetworkInterface with the given 'name', array of 'addresses',
// and 'id'.
static jobject MakeNetworkInterface(JNIEnv* env,
        jstring name, jobjectArray addresses, jint id) {
    jclass networkInterfaceClass = env->FindClass("java/net/NetworkInterface");
    if (networkInterfaceClass == NULL) {
        return NULL;
    }
    jmethodID networkInterfaceConstructor =
            env->GetMethodID(networkInterfaceClass, "<init>",
                    "(Ljava/lang/String;Ljava/lang/String;[Ljava/net/InetAddress;I)V");
    if (networkInterfaceConstructor == NULL) {
        return NULL;
    }
    return env->NewObject(networkInterfaceClass, networkInterfaceConstructor,
                          name, name, addresses, id);
}

jobjectArray NetworkInterfaceGetter::getNetworkInterfaces(JNIEnv* env) {
    scoped_fd fd(socket(PF_INET, SOCK_DGRAM, 0));
    if (fd.get() < 0) {
        jniThrowSocketException(env);
        return NULL;
    }

    // Get the list of interfaces.
    // Keep trying larger buffers until the result fits.
    int len = 32 * sizeof(ifreq);
    for (;;) {
        // TODO: std::vector or boost::scoped_array would make this less awful.
        if (ifc.ifc_buf != NULL) {
            delete[] ifc.ifc_buf;
            ifc.ifc_buf = NULL;
        }
        char* data = new char[len];
        if (data == NULL) {
            jniThrowOutOfMemoryError(env);
            return NULL;
        }
        ifc.ifc_len = len;
        ifc.ifc_buf = data;
        if (ioctl(fd.get(), SIOCGIFCONF, &ifc) != 0) {
            jniThrowSocketException(env);
            return NULL;
        }
        if (ifc.ifc_len < len) {
            break;
        }
        // The returned data was likely truncated.
        // Expand the buffer and try again.
        len += 32 * sizeof(ifreq);
    }

    // Count the number of distinct interfaces.
    // Multiple addresses for a given interface have the same interface name.
    // This whole function assumes that all an interface's addresses will be
    // listed adjacent to one another.
    size_t totalAddressCount = ifc.ifc_len / sizeof(ifreq);
    size_t interfaceCount = 0;
    const char* lastName = NULL;
    for (size_t i = 0; i < totalAddressCount; ++i) {
        const char* name = ifc.ifc_req[i].ifr_name;
        if (lastName == NULL || strncmp(lastName, name, IFNAMSIZ) != 0) {
            ++interfaceCount;
        }
        lastName = name;
    }

    // Build the NetworkInterface[]...
    jclass networkInterfaceClass = env->FindClass("java/net/NetworkInterface");
    if (networkInterfaceClass == NULL) {
        return NULL;
    }
    interfaces = env->NewObjectArray(interfaceCount, networkInterfaceClass, NULL);
    if (interfaces == NULL) {
        return NULL;
    }

    // Fill in the NetworkInterface[].
    size_t arrayIndex = 0;
    for (size_t i = 0; i < totalAddressCount; ++i) {
        // Get the index for this interface.
        // (This is an id the kernel uses, unrelated to our array indexes.)
        int id = ifc.ifc_req[i].ifr_ifindex;

        // Get the name for this interface. There only seems to be one name so
        // we use it for both name and the display name (as does the RI).
        jstring name = env->NewStringUTF(ifc.ifc_req[i].ifr_name);
        if (name == NULL) {
            return NULL;
        }

        // Check how many addresses this interface has.
        size_t addressCount = 0;
        for (size_t j = i; j < totalAddressCount; ++j) {
            if (strncmp(ifc.ifc_req[i].ifr_name, ifc.ifc_req[j].ifr_name, IFNAMSIZ) == 0) {
                if (ifc.ifc_req[j].ifr_addr.sa_family == AF_INET) {
                    ++addressCount;
                }
            } else {
                break;
            }
        }

        // Get this interface's addresses as an InetAddress[].
        jobjectArray addresses = MakeInetAddressArray(env, ifc, i, addressCount);
        if (addresses == NULL) {
            return NULL;
        }
        // Create the NetworkInterface object and add it to the NetworkInterface[].
        jobject interface = MakeNetworkInterface(env, name, addresses, id);
        if (interface == NULL) {
            return NULL;
        }
        env->SetObjectArrayElement(interfaces, arrayIndex++, interface);
        if (env->ExceptionCheck()) {
            return NULL;
        }

        // Skip over this interface's addresses to the next *interface*.
        i += addressCount - 1;
    }
    return interfaces;
}

/**
 * Returns an array of zero or more NetworkInterface objects, one for each
 * network interface.
 */
static jobjectArray getNetworkInterfacesImpl(JNIEnv* env, jclass) {
    NetworkInterfaceGetter getter;
    return getter.getNetworkInterfaces(env);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getNetworkInterfacesImpl", "()[Ljava/net/NetworkInterface;", (void*) getNetworkInterfacesImpl },
};
int register_java_net_NetworkInterface(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/net/NetworkInterface",
            gMethods, NELEM(gMethods));
}

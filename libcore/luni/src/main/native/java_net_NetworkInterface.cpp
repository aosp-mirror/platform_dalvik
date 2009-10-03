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

// Structure containing one network interface's details.
// TODO: construct Java NetworkInterface objects as we go, and lose this.
struct NetworkInterface_struct {
    NetworkInterface_struct()
    : name(NULL), interfaceIndex(-1), addresses(NULL)
    {
    }

    jstring name;
    int interfaceIndex;
    jobjectArray addresses;
};

// Structure containing all the network interfaces.
struct NetworkInterfaceArray_struct {
    NetworkInterfaceArray_struct() : interfaceCount(0), interfaces(NULL) {
        // Initialize this so we can be responsible for deleting it.
        ifc.ifc_buf = NULL;
    }

    ~NetworkInterfaceArray_struct() {
        delete[] interfaces;
        delete[] ifc.ifc_buf;
    }

    size_t interfaceCount;
    NetworkInterface_struct* interfaces;
    ifconf ifc;
};

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

// TODO: move to JNIHelp?
static void jniThrowOutOfMemoryError(JNIEnv* env) {
    jniThrowException(env, "java/lang/OutOfMemoryError", "native heap");
}

static void jniThrowSocketException(JNIEnv* env) {
    jniThrowException(env, "java/net/SocketException", strerror(errno));
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

// TODO: 'array' is a poor name, but this should probably be broken into
// methods on NetworkInterfaceArray_struct itself.
static void sockGetNetworkInterfaces(JNIEnv* env, NetworkInterfaceArray_struct* array) {
    scoped_fd fd(socket(PF_INET, SOCK_DGRAM, 0));
    if (fd.get() < 0) {
        jniThrowSocketException(env);
        return;
    }

    // Get the list of interfaces.
    // Keep trying larger buffers until the result fits.
    ifconf& ifc(array->ifc);
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
            return;
        }
        ifc.ifc_len = len;
        ifc.ifc_buf = data;
        if (ioctl(fd.get(), SIOCGIFCONF, &ifc) != 0) {
            jniThrowSocketException(env);
            return;
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
    size_t totalInterfaces = ifc.ifc_len / sizeof(ifreq);
    char* lastName = NULL;
    for (size_t i = 0; i < totalInterfaces; ++i) {
        if (lastName == NULL || strncmp(lastName, ifc.ifc_req[i].ifr_name, IFNAMSIZ) != 0) {
            ++(array->interfaceCount);
        }
        lastName = ifc.ifc_req[i].ifr_name;
    }

    // Translate into an intermediate form.
    // TODO: we should go straight to the final form.
    array->interfaces = new NetworkInterface_struct[array->interfaceCount];
    if (array->interfaces == NULL) {
        jniThrowOutOfMemoryError(env);
        return;
    }
    for (size_t i = 0; i < totalInterfaces; ++i) {
        NetworkInterface_struct& interface = array->interfaces[i];

        // Get the index for this interface.
        interface.interfaceIndex = ifc.ifc_req[i].ifr_ifindex;

        // Get the name for this interface.
        // There only seems to be one name so we use it for both name and the
        // display name (as does the RI).
        interface.name = env->NewStringUTF(ifc.ifc_req[i].ifr_name);
        if (interface.name == NULL) {
            return;
        }

        // Check how many addresses this interface has.
        size_t addressCount = 0;
        for (size_t j = i; j < totalInterfaces; ++j) {
            if (strncmp(ifc.ifc_req[i].ifr_name, ifc.ifc_req[j].ifr_name, IFNAMSIZ) == 0) {
                if (ifc.ifc_req[j].ifr_addr.sa_family == AF_INET) {
                    ++addressCount;
                }
            } else {
                break;
            }
        }

        // Get this interface's addresses as an InetAddress[].
        interface.addresses = MakeInetAddressArray(env, array->ifc, i, addressCount);
        if (interface.addresses == NULL) {
            return;
        }

        // Skip over this interface's addresses to the next *interface*.
        i += addressCount - 1;
    }
}

/**
 * Returns an array of zero or more NetworkInterface objects, one for each
 * network interface.
 */
static jobjectArray getNetworkInterfacesImpl(JNIEnv* env, jclass) {
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

    NetworkInterfaceArray_struct networkInterfaceArray;
    sockGetNetworkInterfaces(env, &networkInterfaceArray);
    if (env->ExceptionCheck()) {
        return NULL;
    }

    // Build the NetworkInterface[] and fill it in.
    jobjectArray networkInterfaces =
            env->NewObjectArray(networkInterfaceArray.interfaceCount,
                    networkInterfaceClass, NULL);
    if (networkInterfaces == NULL) {
        return NULL;
    }
    for (size_t i = 0; i < networkInterfaceArray.interfaceCount; ++i) {
        const NetworkInterface_struct& interface =
            networkInterfaceArray.interfaces[i];
        // Create the NetworkInterface object and add it to the result.
        jobject currentInterface = env->NewObject(networkInterfaceClass,
                networkInterfaceConstructor, interface.name, interface.name,
                interface.addresses, interface.interfaceIndex);
        if (currentInterface == NULL) {
            return NULL;
        }
        env->SetObjectArrayElement(networkInterfaces, i, currentInterface);
        if (env->ExceptionCheck()) {
            return NULL;
        }
    }
    return networkInterfaces;
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

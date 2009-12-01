/*
 * Copyright (C) 2009 The Android Open Source Project
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

package java.net;

/**
 * Identifies one of a network interface's addresses.
 * These are passed back from the JNI behind NetworkInterface.getNetworkInterfaces.
 * Multiple addresses for the same interface are collected together on the Java side.
 */
class InterfaceAddress {
    // An IPv4 or IPv6 address.
    final InetAddress address;

    // The kernel's interface index for the network interface this address
    // is currently assigned to. Values start at 1, because 0 means "unknown"
    // or "any", depending on context.
    final int index;

    // The network interface's name. "lo" or "eth0", for example.
    final String name;

    InterfaceAddress(int index, String name, InetAddress address) {
        this.index = index;
        this.name = name;
        this.address = address;
    }
}

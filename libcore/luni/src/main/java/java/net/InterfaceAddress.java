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
 *
 * @hide
 * @since 1.6
 */
public class InterfaceAddress {
    /**
     * The kernel's interface index for the network interface this address
     * is currently assigned to. Values start at 1, because 0 means "unknown"
     * or "any", depending on context.
     */
    final int index;

    /**
     * The network interface's name. "lo" or "eth0", for example.
     */
    final String name;

    /**
     * An IPv4 or IPv6 address.
     */
    final InetAddress address;

    /**
     * The IPv4 broadcast address, or null for IPv6.
     */
    private final InetAddress broadcastAddress;

    private final short prefixLength;

    InterfaceAddress(int index, String name, InetAddress address, InetAddress mask) {
        assert ((address instanceof Inet4Address) == (mask instanceof Inet4Address));
        this.index = index;
        this.name = name;
        this.address = address;
        this.broadcastAddress = makeBroadcastAddress(address, mask);
        this.prefixLength = countPrefixLength(mask);
    }

    private static InetAddress makeBroadcastAddress(InetAddress address, InetAddress mask) {
        if (!(address instanceof Inet4Address)) {
            return null;
        }
        byte[] broadcast = new byte[4];
        byte[] maskBytes = mask.ipaddress;
        byte[] addrBytes = address.ipaddress;
        if (maskBytes[0] != 0) {
            for (int i = 0; i < broadcast.length; ++i) {
                broadcast[i] = (byte) (addrBytes[i] | ~maskBytes[i]);
            }
        }
        return new Inet4Address(broadcast);
    }

    private static short countPrefixLength(InetAddress mask) {
        short count = 0;
        for (byte b : mask.ipaddress) {
            for (int i = 0; i < 8; ++i) {
                if ((b & (1 << i)) != 0) {
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * Tests whether this object is equal to another one. Returns true if
     * the address, broadcast address and prefix length are all equal.
     *
     * @param obj the object to be compared.
     * @return true if 'obj' is equal to this InterfaceAddress, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }
        if (!(obj instanceof InterfaceAddress)) {
            return false;
        }
        InterfaceAddress rhs = (InterfaceAddress) obj;
        return ((address == null) ? rhs.address == null : address.equals(rhs.address)) &&
                (rhs.prefixLength == prefixLength) &&
                ((broadcastAddress == null) ? rhs.broadcastAddress == null : broadcastAddress.equals(rhs.broadcastAddress));
    }

    @Override
    public int hashCode() {
        int hashCode = address == null ? 0 : -address.hashCode();
        hashCode += broadcastAddress == null ? 0 : broadcastAddress.hashCode();
        hashCode += prefixLength;
        return hashCode;
    }

    /**
     * Returns a string representation for this interface address.
     * The string is of the form: InetAddress / prefix length [ broadcast address ].
     *
     * @return a string representation of this interface address.
     */
    @Override
    public String toString() {
        return address + "/" + prefixLength + " [" + broadcastAddress + "]";
    }

    /**
     * Returns the InetAddress for this address.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Returns the subnet-directed broadcast address if this is an IPv4 interface, null otherwise.
     */
    public InetAddress getBroadcast() {
        return broadcastAddress;
    }

    /**
     * Returns the network prefix length in bits.
     * (In IPv4 parlance, this is known as the subnet mask,
     * but this method applies to IPv6 addresses too.)
     */
    public short getNetworkPrefixLength() {
        return prefixLength;
    }
}

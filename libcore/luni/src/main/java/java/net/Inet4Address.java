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

package java.net;

import java.io.ObjectStreamException;

public final class Inet4Address extends InetAddress {

    private static final long serialVersionUID = 3286316764910316507L;

    Inet4Address(byte[] address) {
        ipaddress = address;
    }

    Inet4Address(byte[] address, String name) {
        ipaddress = address;
        hostName = name;
    }

    /**
     * Returns true if the address is a multicast address. Valid IPv4 multicast
     * addresses are prefixed with 1110 = 0xE
     * 
     * @return boolean
     */
    @Override
    public boolean isMulticastAddress() {
        return (ipaddress[0] & 0xF0) == 0xE0;
    }

    /**
     * Returns if the address is the ANY Address
     * 
     * @return boolean
     */
    @Override
    public boolean isAnyLocalAddress() {
        for (int i = 0; i < ipaddress.length; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the address is a loopback address. Loopback ipv4
     * addresses are prefixed with: 011111111 = 127
     * 
     * @return boolean
     */
    @Override
    public boolean isLoopbackAddress() {
        return (ipaddress[0] & 255) == 127;
    }

    /**
     * Returns whether this address has link-local scope.
     * 
     * RFC 3484 Default Address Selection for Internet Protocol version 6 (IPv6)
     * states IPv4 auto-configuration addresses, prefix 169.254/16, IPv4
     * loopback addresses, prefix 127/8, are assigned link-local scope.
     * 
     * @return boolean
     */
    @Override
    public boolean isLinkLocalAddress() {
        // The reference implementation does not return true for loopback
        // addresses even though RFC 3484 says to do so
        return (((ipaddress[0] & 255) == 169) && ((ipaddress[1] & 255) == 254));
    }

    /**
     * Returns whether this address has site-local scope. RFC 3484 Default
     * Address Selection for Internet Protocol version 6 (IPv6) states IPv4
     * private addresses, prefixes 10/8, 172.16/12, and 192.168/16, are assigned
     * site-local scope.
     * 
     * @return boolean
     */
    @Override
    public boolean isSiteLocalAddress() {
        return ((ipaddress[0] & 255) == 10) || ((ipaddress[0] & 255) == 172)
                && (((ipaddress[1] & 255) > 15) && (ipaddress[1] & 255) < 32)
                || ((ipaddress[0] & 255) == 192)
                && ((ipaddress[1] & 255) == 168);
    }

    /**
     * Returns true if an address is a global multicast address. Valid MCGlobal
     * IPv4 addresses are 224.0.1.0 - 238.255.255.255
     * 
     * @return boolean true, if the address is in the global multicast group,
     *         false otherwise
     */
    @Override
    public boolean isMCGlobal() {

        // Check if we have a prefix of 1110
        if (!isMulticastAddress()) {
            return false;
        }

        int address = InetAddress.bytesToInt(ipaddress, 0);
        /*
         * Now check the boundaries of the global space if we have an address
         * that is prefixed by something less than 111000000000000000000001
         * (fortunately we don't have to worry about sign after shifting 8 bits
         * right) it is not multicast. ( < 224.0.1.0)
         */
        if (address >>> 8 < 0xE00001) {
            return false;
        }

        /*
         * Now check the high boundary which is prefixed by 11101110 = 0xEE. If
         * the value is higher than this than it is not MCGlobal ( >
         * 238.255.255.255 )
         */
        if (address >>> 24 > 0xEE) {
            return false;
        }

        return true;
    }

    /**
     * Returns false for all IPv4 addresses. There are no valid IPv4 Node-local
     * addresses
     * 
     * @return boolean
     */
    @Override
    public boolean isMCNodeLocal() {
        return false;
    }

    /**
     * Returns true if the address is a link-local address.The valid range for
     * IPv4 link-local addresses is: 224.0.0.0 to 239.0.0.255 Hence a mask of
     * 111000000000000000000000 = 0xE00000
     * 
     * @return boolean
     */
    @Override
    public boolean isMCLinkLocal() {
        return InetAddress.bytesToInt(ipaddress, 0) >>> 8 == 0xE00000;
    }

    /**
     * Returns true if the address is a site-local address.The valid range for
     * IPv4 site-local addresses is: 239.255.0.0 to 239.255.255.255 Hence a mask
     * of 11101111 11111111 = 0xEFFF.
     * 
     * @return boolean
     */
    @Override
    public boolean isMCSiteLocal() {
        return (InetAddress.bytesToInt(ipaddress, 0) >>> 16) == 0xEFFF;
    }

    /**
     * Returns true if the address is a organization-local address. The valid
     * range for IPv4 org-local addresses is: 239.192.0.0 to 239.195.255.255
     * Hence masks of 11101111 11000000 to 11101111 11000011 are valid. 0xEFC0
     * to 0xEFC3
     * 
     * @return true if org local address, false otherwise
     */
    @Override
    public boolean isMCOrgLocal() {
        int prefix = InetAddress.bytesToInt(ipaddress, 0) >>> 16;
        return prefix >= 0xEFC0 && prefix <= 0xEFC3;
    }

    /**
     * Returns a String representation of the IP address.
     * 
     * @return Host address
     */
    @Override
    public String getHostAddress() {
        String hostAddress = ""; //$NON-NLS-1$
        for (int i = 0; i < 4; i++) {
            hostAddress += ipaddress[i] & 255;
            if (i != 3) {
                hostAddress += "."; //$NON-NLS-1$
            }
        }
        return hostAddress;
    }

    /**
     * Overrides the basic hashcode function.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return InetAddress.bytesToInt(ipaddress, 0);
    }

    /**
     * Returns true if obj is of the same type as the IPv4 address and they have
     * the same IP address, false otherwise.
     * 
     * @return true if equal and false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    private Object writeReplace() throws ObjectStreamException {
        return new InetAddress(ipaddress, hostName);
    }
}

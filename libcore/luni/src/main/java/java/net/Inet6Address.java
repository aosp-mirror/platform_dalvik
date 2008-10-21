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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Enumeration;

import org.apache.harmony.luni.util.Inet6Util;
import org.apache.harmony.luni.util.Msg;

public final class Inet6Address extends InetAddress {

    private static final long serialVersionUID = 6880410070516793377L;

    static final byte[] any_bytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0 };

    static final byte[] localhost_bytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1 };

    static final InetAddress ANY = new Inet6Address(any_bytes);

    static final InetAddress LOOPBACK = new Inet6Address(localhost_bytes,
            "localhost"); //$NON-NLS-1$

    int scope_id;

    boolean scope_id_set;

    boolean scope_ifname_set;

    String ifname;

    /*
     * scoped interface.
     */
    transient NetworkInterface scopedIf;

    Inet6Address(byte address[]) {
        ipaddress = address;
        scope_id = 0;
    }

    Inet6Address(byte address[], String name) {
        hostName = name;
        ipaddress = address;
        scope_id = 0;
    }

    /**
     * Constructs an InetAddress, representing the <code>address</code> and
     * <code>hostName</code> and <code>scope_id</code>
     * 
     * @param address
     *            network address
     * @param name
     *            Name associated with the address
     * @param scope_id
     *            The scope id for link or site local addresses
     */
    Inet6Address(byte address[], String name, int scope_id) {
        hostName = name;
        ipaddress = address;
        this.scope_id = scope_id;
        if (scope_id != 0) {
            scope_id_set = true;
        }
    }

    /**
     * Constructs an IPv6 address according to the given <code>host</code>,
     * <code>addr</code> and <code>scope_id</code>.
     * 
     * @param host
     *            hostname associated with the address
     * @param addr
     *            network address
     * @param scope_id
     *            the scope id for link or site local addresses
     * @return an Inet6Address instance
     * @throws UnknownHostException
     *             if the address is null or of invalid length
     */
    public static Inet6Address getByAddress(String host, byte[] addr,
            int scope_id) throws UnknownHostException {
        if (null == addr || 16 != addr.length) {
            // KA020=Illegal IPv6 address
            throw new UnknownHostException(Msg.getString("KA020")); //$NON-NLS-1$
        }
        if (scope_id < 0) {
            scope_id = 0;
        }
        return new Inet6Address(addr, host, scope_id);
    }

    /**
     * Constructs an IPv6 address according to the given <code>host</code>,
     * <code>addr</code> and <code>nif</code>. <code>scope_id</code> is
     * set according to the given <code>nif</code> and the
     * <code>addr<code> type(e.g. site local or link local).
     * 
     * @param host
     *            host name associated with the address
     * @param addr
     *            network address
     * @param nif
     *            the Network Interface that this address is associated with.
     * @return an Inet6Address instance
     * @throws UnknownHostException
     *             if the address is null or of invalid length, or the
     *             interface doesn't have a numeric scope id for the given
     *             address type.
     */
    public static Inet6Address getByAddress(String host, byte[] addr,
            NetworkInterface nif) throws UnknownHostException {

        Inet6Address address = Inet6Address.getByAddress(host, addr, 0);

        // if nif is null, nothing needs to be set.
        if (null == nif) {
            return address;
        }

        // find the first address which matches the type addr,
        // then set the scope_id, ifname and scopedIf.
        Enumeration<InetAddress> addressList = nif.getInetAddresses();
        while (addressList.hasMoreElements()) {
            InetAddress ia = addressList.nextElement();
            if (ia.getAddress().length == 16) {
                Inet6Address v6ia = (Inet6Address) ia;
                boolean isSameType = v6ia.compareLocalType(address);
                if (isSameType) {
                    address.scope_id_set = true;
                    address.scope_id = v6ia.scope_id;
                    address.scope_ifname_set = true;
                    address.ifname = nif.getName();
                    address.scopedIf = nif;
                    break;
                }
            }
        }
        // if no address matches the type of addr, throws an
        // UnknownHostException.
        if (!address.scope_id_set) {
            // KA021=Scope id is not found for the given address
            throw new UnknownHostException(Msg.getString("KA021")); //$NON-NLS-1$
        }
        return address;
    }

    /**
     * Returns true if one of following cases is true: 1. both addresses are
     * site local; 2. both addresses are link local; 3. ia is neither site local
     * nor link local;
     */
    private boolean compareLocalType(Inet6Address ia) {
        if (ia.isSiteLocalAddress() && isSiteLocalAddress()) {
            return true;
        }
        if (ia.isLinkLocalAddress() && isLinkLocalAddress()) {
            return true;
        }
        if (!ia.isSiteLocalAddress() && !ia.isLinkLocalAddress()) {
            return true;
        }
        return false;
    }

    /**
     * Constructs an InetAddress, representing the <code>address</code> and
     * <code>hostName</code> and <code>scope_id</code>
     * 
     * @param address
     *            network address
     * @param scope_id
     *            The scope id for link or site local addresses
     */
    Inet6Address(byte address[], int scope_id) {
        ipaddress = address;
        this.scope_id = scope_id;
        if (scope_id != 0) {
            scope_id_set = true;
        }
    }

    /**
     * Answer true if the InetAddress is an IP multicast address.
     * 
     * Valid IPv6 multicast address have the binary prefixed with 11111111 or FF
     * (hex).
     * 
     * @return boolean true, if the address is in the multicast group, false
     *         otherwise
     */
    @Override
    public boolean isMulticastAddress() {
        // Multicast addresses are prefixed with 11111111 (255)
        return ipaddress[0] == -1;
    }

    /**
     * Answer true if the InetAddress is the unspecified address "::".
     * 
     * @return boolean true, if the address is in the multicast group, false
     *         otherwise
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
     * Answer true if the InetAddress is the loopback address
     * 
     * The valid IPv6 loopback address is ::1
     * 
     * @return boolean true if the address is the loopback, false otherwise
     */
    @Override
    public boolean isLoopbackAddress() {

        // The last word must be 1
        if (ipaddress[15] != 1) {
            return false;
        }

        // All other words must be 0
        for (int i = 0; i < 15; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Answer true if the InetAddress is a link-local address.
     * 
     * A valid IPv6 link-local address is prefixed with 1111111010
     * 
     * @return boolean true, if it is a link-local address, false otherwise
     */
    @Override
    public boolean isLinkLocalAddress() {

        // the first 10 bits need to be 1111111010 (1018)
        return (ipaddress[0] == -2) && ((ipaddress[1] & 255) >>> 6) == 2;
    }

    /**
     * Answer true if the InetAddress is a site-local address.
     * 
     * A valid IPv6 site-local address is prefixed with 1111111011
     * 
     * @return boolean true, if it is a site-local address, false otherwise
     */
    @Override
    public boolean isSiteLocalAddress() {

        // the first 10 bits need to be 1111111011 (1019)
        return (ipaddress[0] == -2) && ((ipaddress[1] & 255) >>> 6) == 3;
    }

    /**
     * Answer true if the InetAddress is a global multicast address.
     * 
     * A valid IPv6 global multicast address is 11111111xxxx1110 (i.e. FF0E)
     * 
     * @return boolean true, if it is a global multicast address, false
     *         otherwise
     */
    @Override
    public boolean isMCGlobal() {
        // the first byte should be 0xFF and the lower 4 bits
        // of the second byte should be 0xE
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 14;
    }

    /**
     * Answer true if the InetAddress is a node-local multicast address.
     * 
     * A valid IPv6 node-local multicast address is prefixed with
     * 11111111xxxx0001
     * 
     * @return boolean true, if it is a node-local multicast address, false
     *         otherwise
     */
    @Override
    public boolean isMCNodeLocal() {
        // the first byte should be 0xFF and the lower 4 bits
        // of the second byte should be 0x1
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 1;
    }

    /**
     * Answer true if the InetAddress is a link-local multicast address.
     * 
     * A valid IPv6 link-local multicast address is prefixed with
     * 11111111xxxx0010
     * 
     * @return boolean true, if it is a link-local multicast address, false
     *         otherwise
     */
    @Override
    public boolean isMCLinkLocal() {
        // the first byte should be 0xFF and the lower 4 bits
        // of the second byte should be 0x2
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 2;
    }

    /**
     * Answer true if the InetAddress is a site-local multicast address.
     * 
     * A valid IPv6 site-local multicast address is prefixed with
     * 11111111xxxx0101
     * 
     * @return boolean true, if it is a site-local multicast address, false
     *         otherwise
     */
    @Override
    public boolean isMCSiteLocal() {
        // the first byte should be 0xFF and the lower 4 bits
        // of the second byte should be 0x5
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 5;
    }

    /**
     * Answer true if the InetAddress is a org-local multicast address.
     * 
     * A valid IPv6 org-local multicast address is prefixed with
     * 11111111xxxx1000
     * 
     * @return boolean true, if it is a org-local multicast address, false
     *         otherwise
     */
    @Override
    public boolean isMCOrgLocal() {
        // the first byte should be 0xFF and the lower 4 bits
        // of the second byte should be 0x8
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 8;
    }

    @Override
    public String getHostAddress() {
        return Inet6Util.createIPAddrStringFromByteArray(ipaddress);
    }

    /**
     * Returns the <code>scope id</code> of this address if it is associated
     * with an interface. Otherwise returns zero.
     * 
     * @return the scope_id.
     */
    public int getScopeId() {
        if (scope_id_set) {
            return scope_id;
        }
        return 0;
    }

    /**
     * Returns the network interface if this address is instanced with a scoped
     * network interface. Otherwise returns null.
     * 
     * @return the scoped network interface.
     */
    public NetworkInterface getScopedInterface() {
        if (scope_ifname_set) {
            return scopedIf;
        }
        return null;
    }

    /**
     * Returns the hashcode of the receiver.
     * 
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        /* Returns the low order int as the hash code */
        return bytesToInt(ipaddress, 12);
    }

    /**
     * Returns true if obj is of the same type as the IPv6 address and they have
     * the same IP address, false otherwise. the scope id does not seem to be
     * part of the comparison
     * 
     * @return String
     * 
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * An IPv4 compatible address is prefixed with 96 bits of 0's. The last
     * 32-bits are varied corresponding with the 32-bit IPv4 address space.
     */
    public boolean isIPv4CompatibleAddress() {
        for (int i = 0; i < 12; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("ipaddress", new byte[0].getClass()), //$NON-NLS-1$
            new ObjectStreamField("scope_id", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("scope_id_set", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("scope_ifname_set", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("ifname", String.class), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        if (ipaddress == null) {
            fields.put("ipaddress", null); //$NON-NLS-1$
        } else {
            fields.put("ipaddress", ipaddress); //$NON-NLS-1$
        }

        fields.put("scope_id", scope_id); //$NON-NLS-1$
        fields.put("scope_id_set", scope_id_set); //$NON-NLS-1$
        fields.put("scope_ifname_set", scope_ifname_set); //$NON-NLS-1$
        fields.put("ifname", ifname); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        ipaddress = (byte[]) fields.get("ipaddress", null); //$NON-NLS-1$
        scope_id = fields.get("scope_id", 0); //$NON-NLS-1$
        scope_id_set = fields.get("scope_id_set", false); //$NON-NLS-1$
        ifname = (String) fields.get("ifname", null); //$NON-NLS-1$
        scope_ifname_set = fields.get("scope_ifname_set", false); //$NON-NLS-1$
        if (scope_ifname_set && null != ifname) {
            scopedIf = NetworkInterface.getByName(ifname);
        }
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * address.
     * 
     * @return String the description, as host/address
     */
    @Override
    public String toString() {
        if (ifname != null) {
            return super.toString() + "%" + ifname; //$NON-NLS-1$
        }
        if (scope_id != 0) {
            return super.toString() + "%" + scope_id; //$NON-NLS-1$
        }
        return super.toString();
    }
}

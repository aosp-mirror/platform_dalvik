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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.harmony.luni.util.Msg;

/**
 * This class is used to represent a network interface of the local device. An
 * interface is defined by its address and a platform dependent name. The class
 * provides methods to get all information about the available interfaces of the
 * system or to identify the local interface of a joined multicast group.
 */
public final class NetworkInterface extends Object {

    private static final int CHECK_CONNECT_NO_PORT = -1;

    static final int NO_INTERFACE_INDEX = 0;
    static final int UNSET_INTERFACE_INDEX = -1;

    private final String name;
    private final String displayName;
    private final List<InterfaceAddress> interfaceAddresses = new LinkedList<InterfaceAddress>();

    private final List<InetAddress> addresses = new LinkedList<InetAddress>();

    // The interface index is a positive integer which is non-negative. Where
    // value is zero then we do not have an index for the interface (which
    // occurs in systems which only support IPV4)
    private int interfaceIndex;

    private NetworkInterface parent = null;

    private final List<NetworkInterface> children = new LinkedList<NetworkInterface>();

    // BEGIN android-changed: we pay this extra complexity on the Java side
    // in return for vastly simpler native code.
    private static native InterfaceAddress[] getAllInterfaceAddressesImpl() throws SocketException;

    private static NetworkInterface[] getNetworkInterfacesImpl() throws SocketException {
        Map<String, NetworkInterface> networkInterfaces = new LinkedHashMap<String, NetworkInterface>();
        for (InterfaceAddress ia : getAllInterfaceAddressesImpl()) {
            if (ia != null) { // The array may contain harmless null elements.
                String name = ia.name;
                NetworkInterface ni = networkInterfaces.get(name);
                if (ni == null) {
                    ni = new NetworkInterface(name, name, new InetAddress[] { ia.address }, ia.index);
                    ni.interfaceAddresses.add(ia);
                    networkInterfaces.put(name, ni);
                } else {
                    ni.addresses.add(ia.address);
                    ni.interfaceAddresses.add(ia);
                }
            }
        }
        return networkInterfaces.values().toArray(new NetworkInterface[networkInterfaces.size()]);
    }
    // END android-changed

    /**
     * This constructor is used by the native method in order to construct the
     * NetworkInterface objects in the array that it returns.
     * 
     * @param name
     *            internal name associated with the interface.
     * @param displayName
     *            a user interpretable name for the interface.
     * @param addresses
     *            the Internet addresses associated with the interface.
     * @param interfaceIndex
     *            an index for the interface. Only set for platforms that
     *            support IPV6.
     */
    NetworkInterface(String name, String displayName, InetAddress[] addresses,
            int interfaceIndex) {
        this.name = name;
        this.displayName = displayName;
        this.interfaceIndex = interfaceIndex;
        if (addresses != null) {
            for (InetAddress address : addresses) {
                this.addresses.add(address);
            }
        }
    }

    /**
     * Returns the index for the network interface. Unless the system supports
     * IPV6 this will be 0.
     * 
     * @return the index
     */
    int getIndex() {
        return interfaceIndex;
    }

    /**
     * Returns the first address for the network interface. This is used in the
     * natives when we need one of the addresses for the interface and any one
     * will do
     * 
     * @return the first address if one exists, otherwise null.
     */
    InetAddress getFirstAddress() {
        if (addresses.size() >= 1) {
            return addresses.get(0);
        }
        return null;
    }

    /**
     * Gets the name associated with this network interface.
     * 
     * @return the name of this {@code NetworkInterface} instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a list of addresses bound to this network interface.
     * 
     * @return the address list of the represented network interface.
     */
    public Enumeration<InetAddress> getInetAddresses() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || addresses.isEmpty()) {
            return Collections.enumeration(addresses);
        }
        // TODO: Android should ditch SecurityManager and the associated pollution.
        List<InetAddress> result = new ArrayList<InetAddress>(addresses.size());
        for (InetAddress address : addresses) {
            try {
                sm.checkConnect(address.getHostName(), CHECK_CONNECT_NO_PORT);
            } catch (SecurityException e) {
                continue;
            }
            result.add(address);
        }
        return Collections.enumeration(result);
    }

    /**
     * Gets the human-readable name associated with this network interface.
     * 
     * @return the display name of this network interface or the name if the
     *         display name is not available.
     */
    public String getDisplayName() {
        /*
         * we should return the display name unless it is blank in this case
         * return the name so that something is displayed.
         */
        if (!(displayName.equals(""))) { //$NON-NLS-1$
            return displayName;
        }
        return name;
    }

    /**
     * Gets the specific network interface according to a given name.
     * 
     * @param interfaceName
     *            the name to identify the searched network interface.
     * @return the network interface with the specified name if one exists or
     *         {@code null} otherwise.
     * @throws SocketException
     *             if an error occurs while getting the network interface
     *             information.
     * @throws NullPointerException
     *             if the given interface's name is {@code null}.
     */
    public static NetworkInterface getByName(String interfaceName) throws SocketException {
        if (interfaceName == null) {
            throw new NullPointerException(Msg.getString("K0330")); //$NON-NLS-1$
        }
        for (NetworkInterface networkInterface : getNetworkInterfacesList()) {
            if (networkInterface.name.equals(interfaceName)) {
                return networkInterface;
            }
        }
        return null;
    }

    /**
     * Gets the specific network interface according to the given address.
     *
     * @param address
     *            the address to identify the searched network interface.
     * @return the network interface with the specified address if one exists or
     *         {@code null} otherwise.
     * @throws SocketException
     *             if an error occurs while getting the network interface
     *             information.
     * @throws NullPointerException
     *             if the given interface address is invalid.
     */
    public static NetworkInterface getByInetAddress(InetAddress address) throws SocketException {
        if (address == null) {
            throw new NullPointerException(Msg.getString("K0331")); //$NON-NLS-1$
        }
        for (NetworkInterface networkInterface : getNetworkInterfacesList()) {
            if (networkInterface.addresses.contains(address)) {
                return networkInterface;
            }
        }
        return null;
    }

    /**
     * Gets a list of all network interfaces available on the local system or
     * {@code null} if no interface is available.
     * 
     * @return the list of {@code NetworkInterface} instances representing the
     *         available interfaces.
     * @throws SocketException
     *             if an error occurs while getting the network interface
     *             information.
     */
    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        return Collections.enumeration(getNetworkInterfacesList());
    }

    private static List<NetworkInterface> getNetworkInterfacesList() throws SocketException {
        NetworkInterface[] interfaces = getNetworkInterfacesImpl();

        for (NetworkInterface netif : interfaces) {
            // Ensure that current NetworkInterface is bound to at least
            // one InetAddress before processing
            for (InetAddress addr : netif.addresses) {
                if (addr.ipaddress.length == 16) {
                    if (addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
                        ((Inet6Address) addr).scopedIf = netif;
                        ((Inet6Address) addr).ifname = netif.name;
                        ((Inet6Address) addr).scope_ifname_set = true;
                    }
                }
            }
        }

        List<NetworkInterface> result = new ArrayList<NetworkInterface>();
        boolean[] peeked = new boolean[interfaces.length];
        for (int counter = 0; counter < interfaces.length; counter++) {
            // If this interface has been touched, continue.
            if (peeked[counter]) {
                continue;
            }
            int counter2 = counter;
            // Checks whether the following interfaces are children.
            for (; counter2 < interfaces.length; counter2++) {
                if (peeked[counter2]) {
                    continue;
                }
                if (interfaces[counter2].name.startsWith(interfaces[counter].name + ":")) {
                    // Tagged as peeked
                    peeked[counter2] = true;
                    interfaces[counter].children.add(interfaces[counter2]);
                    interfaces[counter2].parent = interfaces[counter];
                    interfaces[counter].addresses.addAll(interfaces[counter2].addresses);
                }
            }
            // Tagged as peeked
            result.add(interfaces[counter]);
            peeked[counter] = true;
        }
        return result;
    }

    /**
     * Compares the specified object to this {@code NetworkInterface} and
     * returns whether they are equal or not. The object must be an instance of
     * {@code NetworkInterface} with the same name, {@code displayName} and list
     * of network interfaces to be equal.
     * 
     * @param obj
     *            the object to compare with this instance.
     * @return {@code true} if the specified object is equal to this {@code
     *         NetworkInterface}, {@code false} otherwise.
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface rhs = (NetworkInterface) obj;
        // TODO: should the order of the addresses matter (we use List.equals)?
        return interfaceIndex == rhs.interfaceIndex &&
                name.equals(rhs.name) && displayName.equals(rhs.displayName) &&
                addresses.equals(rhs.addresses);
    }

    /**
     * Returns the hash code for this {@code NetworkInterface}. Since the
     * name should be unique for each network interface the hash code is
     * generated using this name.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Gets a string containing a concise, human-readable description of this
     * network interface.
     * 
     * @return the textual representation for this network interface.
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(25);
        string.append("["); //$NON-NLS-1$
        string.append(name);
        string.append("]["); //$NON-NLS-1$
        string.append(displayName);
        // BEGIN android-added: the RI shows this, and it's useful for IPv6 users.
        string.append("]["); //$NON-NLS-1$
        string.append(interfaceIndex);
        // END android-added
        string.append("]"); //$NON-NLS-1$

        /*
         * get the addresses through this call to make sure we only reveal those
         * that we should
         */
        Enumeration<InetAddress> theAddresses = getInetAddresses();
        if (theAddresses != null) {
            while (theAddresses.hasMoreElements()) {
                InetAddress nextAddress = theAddresses.nextElement();
                string.append("["); //$NON-NLS-1$
                string.append(nextAddress.toString());
                string.append("]"); //$NON-NLS-1$
            }
        }
        return string.toString();
    }

    /**
     * Returns a List the InterfaceAddresses for this network interface.
     * <p>
     * If there is a security manager, its checkConnect method is called with
     * the InetAddress for each InterfaceAddress. Only InterfaceAddresses where
     * the checkConnect doesn't throw a SecurityException will be returned.
     * 
     * @return a List of the InterfaceAddresses for this network interface.
     * @since 1.6
     * @hide
     */
    public List<InterfaceAddress> getInterfaceAddresses() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return Collections.unmodifiableList(interfaceAddresses);
        }
        // TODO: Android should ditch SecurityManager and the associated pollution.
        List<InterfaceAddress> result = new ArrayList<InterfaceAddress>(interfaceAddresses.size());
        for (InterfaceAddress ia : interfaceAddresses) {
            try {
                sm.checkConnect(ia.getAddress().getHostName(), CHECK_CONNECT_NO_PORT);
            } catch (SecurityException e) {
                continue;
            }
            result.add(ia);
        }
        return result;
    }

    /**
     * Returns an {@code Enumeration} of all the sub-interfaces of this network interface.
     * Sub-interfaces are also known as virtual interfaces.
     * <p>
     * For example, {@code eth0:1} would be a sub-interface of {@code eth0}.
     * 
     * @return an Enumeration of all the sub-interfaces of this network interface
     * @since 1.6
     * @hide
     */
    public Enumeration<NetworkInterface> getSubInterfaces() {
        return Collections.enumeration(children);
    }

    /**
     * Returns the parent NetworkInterface of this interface if this is a
     * sub-interface, or null if it's a physical (non virtual) interface.
     * 
     * @return the NetworkInterface this interface is attached to.
     * @since 1.6
     * @hide
     */
    public NetworkInterface getParent() {
        return parent;
    }

    /**
     * Returns true if this network interface is up.
     * 
     * @return true if the interface is up.
     * @throws SocketException if an I/O error occurs.
     * @since 1.6
     * @hide
     */
    public boolean isUp() throws SocketException {
        if (addresses.isEmpty()) {
            return false;
        }
        return isUpImpl(name, interfaceIndex);
    }
    private static native boolean isUpImpl(String n, int index) throws SocketException;

    /**
     * Returns true if this network interface is a loopback interface.
     * 
     * @return true if the interface is a loopback interface.
     * @throws SocketException if an I/O error occurs.
     * @since 1.6
     * @hide
     */
    public boolean isLoopback() throws SocketException {
        if (addresses.isEmpty()) {
            return false;
        }
        return isLoopbackImpl(name, interfaceIndex);
    }
    private static native boolean isLoopbackImpl(String n, int index) throws SocketException;

    /**
     * Returns true if this network interface is a point-to-point interface.
     * (For example, a PPP connection using a modem.)
     * 
     * @return true if the interface is point-to-point.
     * @throws SocketException if an I/O error occurs.
     * @since 1.6
     * @hide
     */
    public boolean isPointToPoint() throws SocketException {
        if (addresses.isEmpty()) {
            return false;
        }
        return isPointToPointImpl(name, interfaceIndex);
    }
    private static native boolean isPointToPointImpl(String n, int index) throws SocketException;

    /**
     * Returns true if this network interface supports multicast.
     * 
     * @throws SocketException if an I/O error occurs.
     * @since 1.6
     * @hide
     */
    public boolean supportsMulticast() throws SocketException {
        if (addresses.isEmpty()) {
            return false;
        }
        return supportsMulticastImpl(name, interfaceIndex);
    }
    private static native boolean supportsMulticastImpl(String n, int index) throws SocketException;

    /**
     * Returns the hardware address of the interface, if it has one, and the
     * user has the necessary privileges to access the address.
     * 
     * @return a byte array containing the address or null if the address
     *         doesn't exist or is not accessible.
     * @throws SocketException if an I/O error occurs.
     * @since 1.6
     * @hide
     */
    public byte[] getHardwareAddress() throws SocketException {
        if (addresses.isEmpty()) {
            return new byte[0];
        }
        return getHardwareAddressImpl(name, interfaceIndex);
    }
    private static native byte[] getHardwareAddressImpl(String n, int index) throws SocketException;

    /**
     * Returns the Maximum Transmission Unit (MTU) of this interface.
     * 
     * @return the value of the MTU for the interface.
     * @throws SocketException if an I/O error occurs.
     * @since 1.6
     * @hide
     */
    public int getMTU() throws SocketException {
        if (addresses.isEmpty()) {
            return 0;
        }
        return getMTUImpl(name, interfaceIndex);
    }
    private static native int getMTUImpl(String n, int index) throws SocketException;

    /**
     * Returns true if this interface is a virtual interface (also called
     * a sub-interface). Virtual interfaces are, on some systems, interfaces
     * created as a child of a physical interface and given different settings
     * (like address or MTU). Usually the name of the interface will the name of
     * the parent followed by a colon (:) and a number identifying the child,
     * since there can be several virtual interfaces attached to a single
     * physical interface.
     * 
     * @return true if this interface is a virtual interface.
     * @since 1.6
     * @hide
     */
    public boolean isVirtual() {
        return parent != null;
    }
}

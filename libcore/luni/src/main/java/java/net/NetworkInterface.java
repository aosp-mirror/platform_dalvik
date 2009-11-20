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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
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

    private String name;

    private String displayName;

    InetAddress addresses[];

    // The interface index is a positive integer which is non-negative. Where
    // value is zero then we do not have an index for the interface (which
    // occurs in systems which only support IPV4)
    private int interfaceIndex;

    private int hashCode;

    // BEGIN android-changed: we pay this extra complexity on the Java side
    // in return for vastly simpler native code.
    private static native InterfaceAddress[] getInterfaceAddresses() throws SocketException;

    private static NetworkInterface[] getNetworkInterfacesImpl() throws SocketException {
        Map<String, NetworkInterface> networkInterfaces = new LinkedHashMap<String, NetworkInterface>();
        for (InterfaceAddress ia : getInterfaceAddresses()) {
            if (ia != null) { // The array may contain harmless null elements.
                String name = ia.name;
                NetworkInterface ni = networkInterfaces.get(name);
                if (ni == null) {
                    ni = new NetworkInterface(name, name, new InetAddress[] { ia.address }, ia.index);
                    networkInterfaces.put(name, ni);
                } else {
                    ni.addInterfaceAddress(ia.address);
                }
            }
        }
        return networkInterfaces.values().toArray(new NetworkInterface[networkInterfaces.size()]);
    }

    private void addInterfaceAddress(InetAddress address) {
        InetAddress[] newAddresses = new InetAddress[addresses.length + 1];
        System.arraycopy(addresses, 0, newAddresses, 0, addresses.length);
        newAddresses[addresses.length] = address;
        addresses = newAddresses;
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
    NetworkInterface(String name, String displayName, InetAddress addresses[],
            int interfaceIndex) {
        this.name = name;
        this.displayName = displayName;
        this.addresses = addresses;
        this.interfaceIndex = interfaceIndex;
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
        if ((addresses != null) && (addresses.length >= 1)) {
            return addresses[0];
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
        /*
         * create new vector from which Enumeration to be returned can be
         * generated set the initial capacity to be the number of addresses for
         * the network interface which is the maximum required size
         */

        /*
         * return an empty enumeration if there are no addresses associated with
         * the interface
         */
        if (addresses == null) {
            return new Vector<InetAddress>(0).elements();
        }

        /*
         * for those configuration that support the security manager we only
         * return addresses for which checkConnect returns true
         */
        Vector<InetAddress> accessibleAddresses = new Vector<InetAddress>(
                addresses.length);

        /*
         * get the security manager. If one does not exist just return the full
         * list
         */
        SecurityManager security = System.getSecurityManager();
        if (security == null) {
            return (new Vector<InetAddress>(Arrays.asList(addresses)))
                    .elements();
        }

        /*
         * ok security manager exists so check each address and return those
         * that pass
         */
        for (InetAddress element : addresses) {
            if (security != null) {
                try {
                    /*
                     * since we don't have a port in this case we pass in
                     * NO_PORT
                     */
                    security.checkConnect(element.getHostName(),
                            CHECK_CONNECT_NO_PORT);
                    accessibleAddresses.add(element);
                } catch (SecurityException e) {
                }
            }
        }

        Enumeration<InetAddress> theAccessibleElements = accessibleAddresses
                .elements();
        if (theAccessibleElements.hasMoreElements()) {
            return accessibleAddresses.elements();
        }

        return new Vector<InetAddress>(0).elements();
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
    public static NetworkInterface getByName(String interfaceName)
            throws SocketException {

        if (interfaceName == null) {
            throw new NullPointerException(Msg.getString("K0330")); //$NON-NLS-1$
        }

        /*
         * get the list of interfaces, and then loop through the list to look
         * for one with a matching name
         */
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface netif = interfaces.nextElement();
                if (netif.getName().equals(interfaceName)) {
                    return netif;
                }
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
    public static NetworkInterface getByInetAddress(InetAddress address)
            throws SocketException {

        if (address == null) {
            throw new NullPointerException(Msg.getString("K0331")); //$NON-NLS-1$
        }

        /*
         * get the list of interfaces, and then loop through the list. For each
         * interface loop through the associated set of internet addresses and
         * see if one matches. If so return that network interface
         */
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface netif = interfaces.nextElement();
                /*
                 * to be compatible use the raw addresses without any security
                 * filtering
                 */
                // Enumeration netifAddresses = netif.getInetAddresses();
                if ((netif.addresses != null) && (netif.addresses.length != 0)) {
                    Enumeration<InetAddress> netifAddresses = (new Vector<InetAddress>(
                            Arrays.asList(netif.addresses))).elements();
                    if (netifAddresses != null) {
                        while (netifAddresses.hasMoreElements()) {
                            if (address.equals(netifAddresses.nextElement())) {
                                return netif;
                            }
                        }
                    }
                }
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
    public static Enumeration<NetworkInterface> getNetworkInterfaces()
            throws SocketException {
        NetworkInterface[] interfaces = getNetworkInterfacesImpl();
        if (interfaces == null) {
            return null;
        }

        for (NetworkInterface netif : interfaces) {
            // Ensure that current NetworkInterface is bound to at least
            // one InetAddress before processing
            if (netif.addresses != null) {
                for (InetAddress addr : netif.addresses) {
                    if (16 == addr.ipaddress.length) {
                        if (addr.isLinkLocalAddress()
                                || addr.isSiteLocalAddress()) {
                            ((Inet6Address) addr).scopedIf = netif;
                            ((Inet6Address) addr).ifname = netif.name;
                            ((Inet6Address) addr).scope_ifname_set = true;
                        }
                    }
                }
            }
        }

        return (new Vector<NetworkInterface>(Arrays.asList(interfaces)))
                .elements();
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
        // Return true if it is the exact same object.
        if (obj == this) {
            return true;
        }

        // Ensure it is the right type.
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }

        /*
         * Make sure that some simple checks pass. If the name is not the same
         * then we are sure it is not the same one. We don't check the hashcode
         * as it is generated from the name which we check
         */
        NetworkInterface netif = (NetworkInterface) obj;

        if (netif.getIndex() != interfaceIndex) {
            return false;
        }

        if (!(name.equals("")) && (!netif.getName().equals(name))) { //$NON-NLS-1$
            return false;
        }

        if ((name.equals("")) && (!netif.getName().equals(displayName))) { //$NON-NLS-1$
            return false;
        }

        // Now check that the collection of internet addresses are equal.
        Enumeration<InetAddress> netifAddresses = netif.getInetAddresses();
        Enumeration<InetAddress> localifAddresses = getInetAddresses();

        // Check for both null (same), or one null (not same).
        if (netifAddresses == null) {
            return localifAddresses == null;
        }
        if (localifAddresses == null) {
            return false;
        }

        // Both are not null, check InetAddress elements.
        while (netifAddresses.hasMoreElements()
                && localifAddresses.hasMoreElements()) {
            if (!(localifAddresses.nextElement()).equals(
                    netifAddresses.nextElement())) {
                return false;
            }
        }

        /*
         * Now make sure that they had the same number of addresses, if not they
         * are not the same interface.
         */
        return !netifAddresses.hasMoreElements()
                && !localifAddresses.hasMoreElements();
    }

    /**
     * Gets the hashcode for this {@code NetworkInterface} instance. Since the
     * name should be unique for each network interface the hashcode is
     * generated using this name.
     * 
     * @return the hashcode value for this {@code NetworkInterface} instance.
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = name.hashCode();
        }
        return hashCode;
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
}

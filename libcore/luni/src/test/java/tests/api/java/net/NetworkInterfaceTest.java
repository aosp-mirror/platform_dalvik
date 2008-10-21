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

package tests.api.java.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetworkInterfaceTest extends junit.framework.TestCase {

	// private member variables used for tests
	boolean atLeastOneInterface = false;

	boolean atLeastTwoInterfaces = false;

	private NetworkInterface networkInterface1 = null;

	private NetworkInterface sameAsNetworkInterface1 = null;

	private NetworkInterface networkInterface2 = null;

	/**
	 * @tests java.net.NetworkInterface#getName()
	 */
	public void test_getName() {
		if (atLeastOneInterface) {
			assertNotNull("validate that non null name is returned",
					networkInterface1.getName());
			assertFalse("validate that non-zero length name is generated",
					networkInterface1.getName().equals(""));
		}
		if (atLeastTwoInterfaces) {
			assertFalse(
					"Validate strings are different for different interfaces",
					networkInterface1.getName().equals(
							networkInterface2.getName()));
		}
	}

	/**
	 * @tests java.net.NetworkInterface#getInetAddresses()
	 */
	public void test_getInetAddresses() {

		// security manager that allows us to check that we only return the
		// addresses that we should
		class mySecurityManager extends SecurityManager {

			ArrayList disallowedNames = null;

			public mySecurityManager(ArrayList addresses) {
				disallowedNames = new ArrayList();
				for (int i = 0; i < addresses.size(); i++) {
					disallowedNames.add(((InetAddress) addresses.get(i))
							.getHostName());
					disallowedNames.add(((InetAddress) addresses.get(i))
							.getHostAddress());
				}
			}

			public void checkConnect(String host, int port) {

				if (host == null) {
					throw new NullPointerException("host was null)");
				}

				for (int i = 0; i < disallowedNames.size(); i++) {
					if (((String) disallowedNames.get(i)).equals(host)) {
						throw new SecurityException("not allowed");
					}
				}
			}

		}

		if (atLeastOneInterface) {
			Enumeration theAddresses = networkInterface1.getInetAddresses();
			if (theAddresses != null) {
				while (theAddresses.hasMoreElements()) {
					InetAddress theAddress = (InetAddress) theAddresses
							.nextElement();
					assertTrue("validate that address is not null",
							null != theAddress);
				}
			}
		}

		if (atLeastTwoInterfaces) {
			Enumeration theAddresses = networkInterface2.getInetAddresses();
			if (theAddresses != null) {
				while (theAddresses.hasMoreElements()) {
					InetAddress theAddress = (InetAddress) theAddresses
							.nextElement();
					assertTrue("validate that address is not null",
							null != theAddress);
				}
			}
		}

		// create the list of ok and not ok addresses to return
		if (atLeastOneInterface) {
			ArrayList okAddresses = new ArrayList();
			Enumeration addresses = networkInterface1.getInetAddresses();
			int index = 0;
			ArrayList notOkAddresses = new ArrayList();
			if (addresses != null) {
				while (addresses.hasMoreElements()) {
					InetAddress theAddress = (InetAddress) addresses
							.nextElement();
					if (index != 0) {
						okAddresses.add(theAddress);
					} else {
						notOkAddresses.add(theAddress);
					}
					index++;
				}
			}

			// do the same for network interface 2 it it exists
			if (atLeastTwoInterfaces) {
				addresses = networkInterface2.getInetAddresses();
				index = 0;
				if (addresses != null) {
					while (addresses.hasMoreElements()) {
						InetAddress theAddress = (InetAddress) addresses
								.nextElement();
						if (index != 0) {
							okAddresses.add(theAddress);
						} else {
							notOkAddresses.add(theAddress);
						}
						index++;
					}
				}
			}

			// set the security manager that will make the first address not
			// visible
			System.setSecurityManager(new mySecurityManager(notOkAddresses));

			// validate not ok addresses are not returned
			for (int i = 0; i < notOkAddresses.size(); i++) {
				Enumeration reducedAddresses = networkInterface1
						.getInetAddresses();
				if (reducedAddresses != null) {
					while (reducedAddresses.hasMoreElements()) {
						InetAddress nextAddress = (InetAddress) reducedAddresses
								.nextElement();
						assertTrue(
								"validate that address without permission is not returned",
								!nextAddress.equals(notOkAddresses.get(i)));
					}
				}
				if (atLeastTwoInterfaces) {
					reducedAddresses = networkInterface2.getInetAddresses();
					if (reducedAddresses != null) {
						while (reducedAddresses.hasMoreElements()) {
							InetAddress nextAddress = (InetAddress) reducedAddresses
									.nextElement();
							assertTrue(
									"validate that address without permission is not returned",
									!nextAddress.equals(notOkAddresses.get(i)));
						}
					}
				}
			}

			// validate that ok addresses are returned
			for (int i = 0; i < okAddresses.size(); i++) {
				boolean addressReturned = false;
				Enumeration reducedAddresses = networkInterface1
						.getInetAddresses();
				if (reducedAddresses != null) {
					while (reducedAddresses.hasMoreElements()) {
						InetAddress nextAddress = (InetAddress) reducedAddresses
								.nextElement();
						if (nextAddress.equals(okAddresses.get(i))) {
							addressReturned = true;
						}
					}
				}
				if (atLeastTwoInterfaces) {
					reducedAddresses = networkInterface2.getInetAddresses();
					if (reducedAddresses != null) {
						while (reducedAddresses.hasMoreElements()) {
							InetAddress nextAddress = (InetAddress) reducedAddresses
									.nextElement();
							if (nextAddress.equals(okAddresses.get(i))) {
								addressReturned = true;
							}
						}
					}
				}
				assertTrue("validate that address with permission is returned",
						addressReturned);
			}

			// validate that we can get the interface by specifying the address.
			// This is to be compatible
			for (int i = 0; i < notOkAddresses.size(); i++) {
				try {
					assertNotNull(
							"validate we cannot get the NetworkInterface with an address for which we have no privs",
							NetworkInterface
									.getByInetAddress((InetAddress) notOkAddresses
											.get(i)));
				} catch (Exception e) {
					fail("get NetworkInterface for address with no perm - exception");
				}
			}

			// validate that we can get the network interface for the good
			// addresses
			try {
				for (int i = 0; i < okAddresses.size(); i++) {
					assertNotNull(
							"validate we cannot get the NetworkInterface with an address fro which we have no privs",
							NetworkInterface
									.getByInetAddress((InetAddress) okAddresses
											.get(i)));
				}
			} catch (Exception e) {
				fail("get NetworkInterface for address with perm - exception");
			}

			System.setSecurityManager(null);
		}
	}

	/**
	 * @tests java.net.NetworkInterface#getDisplayName()
	 */
	public void test_getDisplayName() {
		if (atLeastOneInterface) {
			assertNotNull("validate that non null display name is returned",
					networkInterface1.getDisplayName());
			assertFalse(
					"validate that non-zero lengtj display name is generated",
					networkInterface1.getDisplayName().equals(""));
		}
		if (atLeastTwoInterfaces) {
			assertFalse(
					"Validate strings are different for different interfaces",
					networkInterface1.getDisplayName().equals(
							networkInterface2.getDisplayName()));
		}
	}

	/**
	 * @tests java.net.NetworkInterface#getByName(java.lang.String)
	 */
	public void test_getByNameLjava_lang_String() {
		try {
			assertNull("validate null handled ok",
                                   NetworkInterface.getByName(null));
			fail("getByName did not throw NullPointerException for null argument");
		} catch (NullPointerException e) {
		} catch (Exception e) {
			fail("getByName, null inetAddress - raised exception : "
					+ e.getMessage());
		}

		try {
			assertNull("validate handled ok if we ask for name not associated with any interface",
                                   NetworkInterface.getByName("8not a name4"));
		} catch (Exception e) {
			fail("getByName, unknown inetAddress - raised exception : "
					+ e.getMessage());
		}

		// for each address in an interface validate that we get the right
		// interface for that name
		if (atLeastOneInterface) {
			String theName = networkInterface1.getName();
			if (theName != null) {
				try {
					assertTrue(
							"validate that Interface can be obtained with its name",
							NetworkInterface.getByName(theName).equals(
									networkInterface1));
				} catch (Exception e) {
					fail("validate to get network interface using name - socket exception");
				}
			}
		}

		// validate that we get the right interface with the second interface as
		// well (ie we just don't always get the first interface
		if (atLeastTwoInterfaces) {
			String theName = networkInterface2.getName();
			if (theName != null) {
				try {
					assertTrue(
							"validate that Interface can be obtained with its name",
							NetworkInterface.getByName(theName).equals(
									networkInterface2));
				} catch (Exception e) {
					fail("validate to get network interface using name - socket exception");
				}
			}
		}
	}

	/**
	 * @tests java.net.NetworkInterface#getByInetAddress(java.net.InetAddress)
	 */
	public void test_getByInetAddressLjava_net_InetAddress() {

		byte addressBytes[] = new byte[4];
		addressBytes[0] = 0;
		addressBytes[1] = 0;
		addressBytes[2] = 0;
		addressBytes[3] = 0;

		try {
			assertNull("validate null handled ok",
                                   NetworkInterface.getByInetAddress(null));
			fail("should not get here if getByInetAddress throws "
					+ "NullPointerException if null passed in");
		} catch (NullPointerException e) {
		} catch (Exception e) {
			fail("getByInetAddress, null inetAddress should have raised NPE"
					+ " but instead threw a : " + e.getMessage());
		}

		try {
			assertNull("validate handled ok if we ask for address not associated with any interface",
                                   NetworkInterface.getByInetAddress(InetAddress
							.getByAddress(addressBytes)));
		} catch (Exception e) {
			fail("getByInetAddress, unknown inetAddress threw exception : " + e);
		}

		// for each address in an interface validate that we get the right
		// interface for that address
		if (atLeastOneInterface) {
			Enumeration addresses = networkInterface1.getInetAddresses();
			if (addresses != null) {
				while (addresses.hasMoreElements()) {
					InetAddress theAddress = (InetAddress) addresses
							.nextElement();
					try {
						assertTrue(
								"validate that Interface can be obtained with any one of its addresses",
								NetworkInterface.getByInetAddress(theAddress)
										.equals(networkInterface1));
					} catch (Exception e) {
						fail("validate to get address using inetAddress " +
								"threw exception : " + e);
					}
				}
			}
		}

		// validate that we get the right interface with the second interface as
		// well (ie we just don't always get the first interface
		if (atLeastTwoInterfaces) {
			Enumeration addresses = networkInterface2.getInetAddresses();
			if (addresses != null) {
				while (addresses.hasMoreElements()) {
					InetAddress theAddress = (InetAddress) addresses
							.nextElement();
					try {
						assertTrue(
								"validate that Interface can be obtained with any one of its addresses",
								NetworkInterface.getByInetAddress(theAddress)
										.equals(networkInterface2));
					} catch (Exception e) {
						fail("validate to get address using inetAddress "
								+ "threw exception : " + e);
					}
				}
			}
		}
	}

	/**
	 * @tests java.net.NetworkInterface#getNetworkInterfaces()
	 */
	public void test_getNetworkInterfaces() {

		// really this is tested by all of the other calls but just make sure we
		// can call it and get a list of interfaces if they exist
		try {
			Enumeration theInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (Exception e) {
			fail("get Network Interfaces - raised exception : "
					+ e.getMessage());
		}
	}

	/**
	 * @tests java.net.NetworkInterface#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean
		// java.net.SocketPermission.equals(java.lang.Object)
		if (atLeastOneInterface) {
			assertTrue("If objects are the same true is returned",
					networkInterface1.equals(sameAsNetworkInterface1));
			assertFalse("Validate Null handled ok", networkInterface1
					.equals(null));
		}
		if (atLeastTwoInterfaces) {
			assertFalse("If objects are different false is returned",
					networkInterface1.equals(networkInterface2));
		}
	}

	/**
	 * @tests java.net.NetworkInterface#hashCode()
	 */
	public void test_hashCode() {

		if (atLeastOneInterface) {
			assertTrue(
					"validate that hash codes are the same for two calls on the same object",
					networkInterface1.hashCode() == networkInterface1
							.hashCode());
			assertTrue(
					"validate that hash codes are the same for two objects for which equals is true",
					networkInterface1.hashCode() == sameAsNetworkInterface1
							.hashCode());
		}
	}

	/**
	 * @tests java.net.NetworkInterface#toString()
	 */
	public void test_toString() {
		if (atLeastOneInterface) {
			assertNotNull("validate that non null string is generated",
					networkInterface1.toString());
			assertFalse("validate that non-zero length string is generated",
					networkInterface1.toString().equals(""));
		}
		if (atLeastTwoInterfaces) {
			assertFalse(
					"Validate strings are different for different interfaces",
					networkInterface1.toString().equals(
							networkInterface2.toString()));
		}
	}

	protected void setUp() {

		Enumeration theInterfaces = null;
		try {
			theInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (Exception e) {
			fail("Exception occurred getting network interfaces : " + e);
		}
		
		// Set up NetworkInterface instance members. Note that because the call
		// to NetworkInterface.getNetworkInterfaces() returns *all* of the 
		// interfaces on the test machine it is possible that one or more of 
		// them will not currently be bound to an InetAddress. e.g. a laptop
		// running connected by a wire to the local network may also have a 
		// wireless interface that is not active and so has no InetAddress 
		// bound to it. For these tests only work with NetworkInterface objects 
		// that are bound to an InetAddress.   
		if ((theInterfaces != null) && (theInterfaces.hasMoreElements())) {
			while ((theInterfaces.hasMoreElements())
					&& (atLeastOneInterface == false)) {
				NetworkInterface theInterface = (NetworkInterface) theInterfaces
						.nextElement();
				if (theInterface.getInetAddresses() != null) {
					// Ensure that the current NetworkInterface has at least
					// one InetAddress bound to it.  
					Enumeration addrs = theInterface.getInetAddresses();
					if ((addrs != null) && (addrs.hasMoreElements())) {
						atLeastOneInterface = true;
						networkInterface1 = theInterface;
					}// end if 
				}
			}

			while ((theInterfaces.hasMoreElements())
					&& (atLeastTwoInterfaces == false)) {
				NetworkInterface theInterface = (NetworkInterface) theInterfaces
						.nextElement();
				if (theInterface.getInetAddresses() != null) {
					// Ensure that the current NetworkInterface has at least
					// one InetAddress bound to it.  
					Enumeration addrs = theInterface.getInetAddresses();
					if ((addrs != null) && (addrs.hasMoreElements())) {
						atLeastTwoInterfaces = true;
						networkInterface2 = theInterface;
					}// end if 
				}
			}

			// Only set sameAsNetworkInterface1 if we succeeded in finding 
			// at least one good NetworkInterface
			if (atLeastOneInterface) {
				Enumeration addresses = networkInterface1.getInetAddresses();
				if (addresses != null) {
					try {
						if (addresses.hasMoreElements()) {
							sameAsNetworkInterface1 = NetworkInterface
							.getByInetAddress((InetAddress) addresses
									.nextElement());
						}
					} catch (SocketException e) {
						fail("SocketException occurred : " + e);
					}
				}
			}// end if atLeastOneInterface
		}
	}

	protected void tearDown() {
		System.setSecurityManager(null);
	}
}

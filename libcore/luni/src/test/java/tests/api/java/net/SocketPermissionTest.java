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
import java.net.SocketPermission;
import java.net.UnknownHostException;
import java.security.PermissionCollection;

import tests.support.Support_Configuration;

public class SocketPermissionTest extends junit.framework.TestCase {

	String starName = "*." + Support_Configuration.DomainAddress;

	String wwwName = Support_Configuration.HomeAddress;

	SocketPermission star_Resolve = new SocketPermission(starName, "resolve");

	SocketPermission star_All = new SocketPermission(starName,
			"listen,accept,connect");

	SocketPermission www_All = new SocketPermission(wwwName,
			"connect,listen,accept");

	SocketPermission copyOfWww_All = new SocketPermission(wwwName,
			"connect,listen,accept");

	/**
	 * @tests java.net.SocketPermission#SocketPermission(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_String() {
		// Test for method java.net.SocketPermission(java.lang.String,
		// java.lang.String)
		assertTrue("Incorrect name", star_Resolve.getName().equals(starName));
		assertEquals("Incorrect actions", 
				"resolve", star_Resolve.getActions());

		SocketPermission sp1 = new SocketPermission("", "connect");
		assertEquals("Wrong name1", "localhost", sp1.getName());
		SocketPermission sp2 = new SocketPermission(":80", "connect");
		assertEquals("Wrong name2", ":80", sp2.getName());
        
		// regression for HARMONY-1462
        SocketPermission sp3 = new SocketPermission("localhost:*", "listen");
        assertEquals("Wrong name3", "localhost:*", sp3.getName());
        // for all ports
        SocketPermission spAllPorts = new SocketPermission("localhost:0-65535",
                "listen");
        assertTrue("Port range error", sp3.implies(spAllPorts));
        assertTrue("Port range error", spAllPorts.implies(sp3));
	}

	/**
	 * @tests java.net.SocketPermission#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean
		// java.net.SocketPermission.equals(java.lang.Object)
		assertTrue("Different names but returned equal", !star_All
				.equals(www_All));
		assertTrue("Different actions but returned equal", !star_Resolve
				.equals(star_All));
		assertTrue("Same but returned unequal", www_All.equals(copyOfWww_All));
		assertTrue("Returned true when compared to a String", !www_All
				.equals(www_All.toString()));

		SocketPermission sp1 = new SocketPermission("TEST1.com",
				"resolve,connect");
		SocketPermission sp2 = new SocketPermission("test1.com",
				"resolve,connect");
		assertTrue("Different cases should be equal", sp1.equals(sp2));

        // Regression for HARMONY-1524
        assertFalse(sp1.equals(null));
        
        // Regression for HARMONY-3333
        sp1 = new SocketPermission("TEST1.com:333", "resolve");
        sp2 = new SocketPermission("test1.com:444", "resolve");
        assertTrue("Different cases should be equal", sp1.equals(sp2));
	}

	/**
	 * @tests java.net.SocketPermission#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object_subtest0() {
		SocketPermission sp1 = new SocketPermission(
				Support_Configuration.InetTestAddress, "resolve,connect");
		SocketPermission sp2 = new SocketPermission(
				Support_Configuration.InetTestIP, "resolve,connect");
		assertTrue("Same IP address should be equal", sp1.equals(sp2));

	}

	/**
	 * @tests java.net.SocketPermission#getActions()
	 */
	public void test_getActions() {
		// Test for method java.lang.String
		// java.net.SocketPermission.getActions()
		assertEquals("Incorrect actions", 
				"resolve", star_Resolve.getActions());
		assertEquals("Incorrect actions/not in canonical form", "connect,listen,accept,resolve", star_All
				.getActions());
	}

	/**
	 * @tests java.net.SocketPermission#implies(java.security.Permission)
	 */
	public void test_impliesLjava_security_Permission() {
		// Test for method boolean
		// java.net.SocketPermission.implies(java.security.Permission)
		assertTrue("All should imply resolve", star_All.implies(star_Resolve));
		
        // regression for HARMONY-1200
        assertFalse("Null should not be implied", star_All.implies((SocketPermission)null));
        
		assertTrue("Equals should imply eachother", www_All
				.implies(copyOfWww_All));
		assertTrue("Wild should imply normal", star_All.implies(www_All));
		assertTrue("Normal shouldn't imply wildcard", !www_All
				.implies(star_Resolve));
		assertTrue("Resolve shouldn't imply all", !star_Resolve
				.implies(star_All));
		SocketPermission p1 = new SocketPermission(wwwName + ":80-81",
				"connect");
		SocketPermission p2 = new SocketPermission(wwwName + ":80", "connect");
		assertTrue("Port 80 is implied by 80-81", p1.implies(p2));
		p1 = new SocketPermission(wwwName + ":79-80", "connect");
		assertTrue("Port 80 is implied by 79-80", p1.implies(p2));
		p1 = new SocketPermission(wwwName + ":79-81", "connect");
		assertTrue("Port 80 is implied by 79-81", p1.implies(p2));
		p2 = new SocketPermission(wwwName + ":79-80", "connect");
		assertTrue("Port 79-80 is implied by 79-81", p1.implies(p2));
		p2 = new SocketPermission(wwwName, "resolve");
		assertTrue(
				"Any identical host should imply resolve regardless of the ports",
				p1.implies(p2));

		SocketPermission sp1 = new SocketPermission("www.Ibm.com", "resolve");
		SocketPermission sp2 = new SocketPermission("www.IBM.com", "resolve");
		assertTrue("SocketPermission is case sensitive", sp1.implies(sp2));

		SocketPermission sp3 = new SocketPermission("*.ibm.com", "resolve");
		assertTrue("SocketPermission wildcard is case sensitive", sp3
				.implies(sp2));

		InetAddress host = null;
		try {
			host = InetAddress.getByName(Support_Configuration.UnresolvedIP);
		} catch (UnknownHostException e) {
		}
		
		SocketPermission perm1 = new SocketPermission(
				Support_Configuration.UnresolvedIP, "connect");
		SocketPermission perm2 = new SocketPermission(
				Support_Configuration.UnresolvedIP + ":80", "connect");
		assertTrue("should imply port 80", perm1.implies(perm2));
		PermissionCollection col = perm1.newPermissionCollection();
		col.add(perm1);
		assertTrue("collection should imply port 80", col.implies(perm2));

	}

	/**
	 * @tests java.net.SocketPermission#newPermissionCollection()
	 */
	public void test_newPermissionCollection() {
		// Test for method java.security.PermissionCollection
		// java.net.SocketPermission.newPermissionCollection()
		java.security.PermissionCollection pc = star_Resolve
				.newPermissionCollection();
		pc.add(star_Resolve);
		pc.add(www_All);
		assertTrue("Should imply all on " + wwwName, pc.implies(www_All));
		assertTrue("Should imply resolve on " + starName, pc
				.implies(star_Resolve));
		assertTrue("Should not imply all on " + starName, !pc.implies(star_All));

		// wipe out pc
		pc = star_Resolve.newPermissionCollection();
		pc.add(star_All);
		assertTrue("Should imply resolve on " + starName, pc
				.implies(star_Resolve));
		assertTrue("Should imply all on " + wwwName, pc.implies(www_All));

		pc = star_Resolve.newPermissionCollection();
		SocketPermission p1 = new SocketPermission(wwwName + ":79-80",
				"connect");
		pc.add(p1);
		SocketPermission p2 = new SocketPermission(wwwName, "resolve");
		assertTrue(
				"Any identical host should imply resolve regardless of the ports",
				pc.implies(p2));
		assertTrue("A different host should not imply resolve", !pc
				.implies(star_Resolve));
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}

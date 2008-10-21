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

import java.net.PasswordAuthentication;

public class PasswordAuthenticationTest extends junit.framework.TestCase {

	/**
	 * @tests java.net.PasswordAuthentication#PasswordAuthentication(java.lang.String,
	 *        char[])
	 */
	public void test_ConstructorLjava_lang_String$C() {
		// Test for method java.net.PasswordAuthentication(java.lang.String,
		// char [])
		char[] password = new char[] { 'd', 'r', 'o', 'w', 's', 's', 'a', 'p' };
		final String name = "Joe Blow";
		PasswordAuthentication pa = new PasswordAuthentication(name, password);
		char[] returnedPassword = pa.getPassword();
		assertTrue("Incorrect name", pa.getUserName().equals(name));
		assertTrue("Password was not cloned", returnedPassword != password);
		assertTrue("Passwords not equal length",
				returnedPassword.length == password.length);
		for (int counter = password.length - 1; counter >= 0; counter--)
			assertTrue("Passwords not equal",
					returnedPassword[counter] == password[counter]);
	}

	/**
	 * @tests java.net.PasswordAuthentication#getPassword()
	 */
	public void test_getPassword() {
		// Test for method char [] java.net.PasswordAuthentication.getPassword()
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.PasswordAuthentication#getUserName()
	 */
	public void test_getUserName() {
		// Test for method java.lang.String
		// java.net.PasswordAuthentication.getUserName()
		assertTrue("Used to test", true);
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

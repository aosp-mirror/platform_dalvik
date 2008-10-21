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

package tests.api.java.io;

import java.io.InvalidClassException;

public class InvalidClassExceptionTest extends junit.framework.TestCase {

	/**
	 * @tests java.io.InvalidClassException#InvalidClassException(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		final String message = "A message";
		try {
			if (true)
				throw new java.io.InvalidClassException(message);
		} catch (InvalidClassException e) {
			// correct
			assertTrue("Incorrect message read", e.getMessage().equals(message));
			return;
		}
		fail("Failed to throw exception");
	}

	/**
	 * @tests java.io.InvalidClassException#InvalidClassException(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_String() {
		// Test for method java.io.InvalidClassException(java.lang.String,
		// java.lang.String)
		final String message = "A message";
		final String className = "Object";
		try {
			if (true)
				throw new java.io.InvalidClassException(className, message);
		} catch (InvalidClassException e) {
			// correct
			String returnedMessage = e.getMessage();
			assertTrue("Incorrect message read: " + e.getMessage(),
					returnedMessage.indexOf(className) >= 0
							&& returnedMessage.indexOf(message) >= 0);
			return;
		}
		fail("Failed to throw exception");
	}

	/**
	 * @tests java.io.InvalidClassException#getMessage()
	 */
	public void test_getMessage() {
		// Test for method java.lang.String
		// java.io.InvalidClassException.getMessage()
		// used to test
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

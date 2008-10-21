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

import java.io.WriteAbortedException;

public class WriteAbortedExceptionTest extends junit.framework.TestCase {

	/**
	 * @tests java.io.WriteAbortedException#WriteAbortedException(java.lang.String,
	 *        java.lang.Exception)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_Exception() {
		// Test for method java.io.WriteAbortedException(java.lang.String,
		// java.lang.Exception)
		try {
			if (true)
				throw new WriteAbortedException("HelloWorld",
						new WriteAbortedException("ByeWorld", null));
		} catch (WriteAbortedException e) {
			return;
		}
		fail("Failed to generate expected Exception");
	}

	/**
	 * @tests java.io.WriteAbortedException#getMessage()
	 */
	public void test_getMessage() {
		// Test for method java.lang.String
		// java.io.WriteAbortedException.getMessage()
		try {
			if (true)
				throw new WriteAbortedException("HelloWorld",
						new WriteAbortedException("ByeWorld", null));
		} catch (WriteAbortedException e) {
			assertTrue("WriteAbortedException::getMessage() failed"
					+ e.getMessage(), e.getMessage().equals(
					"HelloWorld; java.io.WriteAbortedException: ByeWorld"));
			return;
		}
		fail("Failed to generate expected Exception");
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

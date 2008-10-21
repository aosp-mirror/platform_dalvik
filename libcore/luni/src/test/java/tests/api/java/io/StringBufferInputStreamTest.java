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

import java.io.StringBufferInputStream;

public class StringBufferInputStreamTest extends junit.framework.TestCase {

	StringBufferInputStream sbis;

	/**
	 * @tests java.io.StringBufferInputStream#StringBufferInputStream(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.io.StringBufferInputStream(java.lang.String)
	}

	/**
	 * @tests java.io.StringBufferInputStream#available()
	 */
	public void test_available() {
		// Test for method int java.io.StringBufferInputStream.available()
		assertEquals("Returned incorrect number of available bytes", 11, sbis
				.available());
	}

	/**
	 * @tests java.io.StringBufferInputStream#read()
	 */
	public void test_read() {
		// Test for method int java.io.StringBufferInputStream.read()
		byte[] buf = new byte[5];
		sbis.skip(6);
		sbis.read(buf, 0, 5);
		assertEquals("Returned incorrect chars", "World", new String(buf));
	}

	/**
	 * @tests java.io.StringBufferInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() {
		// Test for method int java.io.StringBufferInputStream.read(byte [],
		// int, int)
		assertEquals("Read returned incorrect char", 'H', sbis.read());
	}

	/**
	 * @tests java.io.StringBufferInputStream#reset()
	 */
	public void test_reset() {
		// Test for method void java.io.StringBufferInputStream.reset()
		long s = sbis.skip(6);
		assertEquals("Unable to skip correct umber of chars", 6, s);
		sbis.reset();
		assertEquals("Failed to reset", 'H', sbis.read());
	}

	/**
	 * @tests java.io.StringBufferInputStream#skip(long)
	 */
	public void test_skipJ() {
		// Test for method long java.io.StringBufferInputStream.skip(long)
		long s = sbis.skip(6);
		assertEquals("Unable to skip correct umber of chars", 6, s);
		assertEquals("Skip positioned at incorrect char", 'W', sbis.read());
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		sbis = new StringBufferInputStream("Hello World");
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}

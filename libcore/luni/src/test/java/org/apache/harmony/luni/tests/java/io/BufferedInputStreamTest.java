/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.tests.java.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class BufferedInputStreamTest extends TestCase {

	/**
	 * @tests java.io.BufferedInputStream#mark(int)
	 */
	public void test_markI() throws IOException {
		BufferedInputStream buf = new BufferedInputStream(
				new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 }), 2);
		buf.mark(3);
		byte[] bytes = new byte[3];
		int result = buf.read(bytes);
		assertEquals(3, result);
		assertEquals("Assert 0:", 0, bytes[0]);
		assertEquals("Assert 1:", 1, bytes[1]);
		assertEquals("Assert 2:", 2, bytes[2]);
		assertEquals("Assert 3:", 3, buf.read());

		buf = new BufferedInputStream(
				new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 }), 2);
		buf.mark(3);
		bytes = new byte[4];
		result = buf.read(bytes);
		assertEquals(4, result);
		assertEquals("Assert 4:", 0, bytes[0]);
		assertEquals("Assert 5:", 1, bytes[1]);
		assertEquals("Assert 6:", 2, bytes[2]);
		assertEquals("Assert 7:", 3, bytes[3]);
		assertEquals("Assert 8:", 4, buf.read());
		assertEquals("Assert 9:", -1, buf.read());

		buf = new BufferedInputStream(
				new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 }), 2);
		buf.mark(Integer.MAX_VALUE);
		buf.read();
		buf.close();
	}
	
	/*
	 * @tests java.io.BufferedInputStream(InputStream)
	 */
	public void test_ConstructorLjava_io_InputStream() {
		try {
			BufferedInputStream str = new BufferedInputStream(null);
			str.read();
			fail("Expected an IOException");
		} catch (IOException e) {
			// Expected
		}
	}
	/*
	 * @tests java.io.BufferedInputStream(InputStream)
	 */
	public void test_ConstructorLjava_io_InputStreamI() {
		try {
			BufferedInputStream str = new BufferedInputStream(null, 1);
			str.read();
			fail("Expected an IOException");
		} catch (IOException e) {
			// Expected
		}
	}
}

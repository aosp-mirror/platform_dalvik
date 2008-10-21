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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

import junit.framework.TestCase;

public class PushBackInputStreamTest extends TestCase {

    /*
     * @tests java.io.PushBackInputStream(InputStream)
     */
    public void test_ConstructorLjava_io_InputStream() {
        try {
            PushbackInputStream str = new PushbackInputStream(null);
            str.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }
    
    /*
     * @tests java.io.PushBackInputStream(InputStream, int)
     */
    public void test_ConstructorLjava_io_InputStreamL() {
        try {
            PushbackInputStream str = new PushbackInputStream(null, 1);
            str.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }
	/**
	 * @tests java.io.PushbackInputStream#unread(byte[], int, int)
	 */
	public void test_unread$BII() {
		// Regression for HARMONY-49
		try {
			PushbackInputStream pb = new PushbackInputStream(
					new ByteArrayInputStream(new byte[] { 0 }), 2);
			pb.unread(new byte[1], 0, 5);
			fail("Assert 0: should throw IOE");
		} catch (IOException e) {
			// expected
		}
	}

	public void test_reset() {
		PushbackInputStream pb = new PushbackInputStream(
				new ByteArrayInputStream(new byte[] { 0 }), 2);
		try {
			pb.reset();
			fail("Should throw IOException");
		} catch (IOException e) {
			// expected
		}
	}

	public void test_mark() {
		PushbackInputStream pb = new PushbackInputStream(
				new ByteArrayInputStream(new byte[] { 0 }), 2);
		pb.mark(Integer.MAX_VALUE);
		pb.mark(0);
		pb.mark(-1);
		pb.mark(Integer.MIN_VALUE);
	}

}

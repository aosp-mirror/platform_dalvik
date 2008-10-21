/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;
import java.io.Writer;

import junit.framework.TestCase;

public class WriterTest extends TestCase {

	/**
	 * @tests java.io.Writer#write(String)
	 */
	public void test_writeLjava_lang_String() throws IOException {
		// Regression for HARMONY-51
		Object lock = new Object();
		Writer wr = new MockWriter(lock);
		// FIXME This test should be added to the exclusion list until
		// Thread.holdsLock works on IBM VME
//		wr.write("Some string");
		wr.close();
	}

	class MockWriter extends Writer {
		final Object myLock;

		MockWriter(Object lock) {
			super(lock);
			myLock = lock;
		}

		@Override
        public synchronized void close() throws IOException {
			// do nothing
		}

		@Override
        public synchronized void flush() throws IOException {
			// do nothing
		}

		@Override
        public void write(char[] arg0, int arg1, int arg2) throws IOException {
			assertTrue(Thread.holdsLock(myLock));
		}
	}
}

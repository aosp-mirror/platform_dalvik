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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.SyncFailedException;

public class SyncFailedExceptionTest extends junit.framework.TestCase {

	/**
	 * @tests java.io.SyncFailedException#SyncFailedException(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.io.SyncFailedException(java.lang.String)
		File f = null;
		try {
			f = new File(System.getProperty("user.dir"), "synfail.tst");
			FileOutputStream fos = new FileOutputStream(f.getPath());
			FileDescriptor fd = fos.getFD();
			fos.close();
			fd.sync();
		} catch (SyncFailedException e) {
			f.delete();
			return;
		} catch (Exception e) {
			fail("Exception during test : " + e.getMessage());
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

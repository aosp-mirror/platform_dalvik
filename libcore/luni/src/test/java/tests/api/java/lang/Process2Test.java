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

package tests.api.java.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tests.support.Support_Exec;

public class Process2Test extends junit.framework.TestCase {
	/**
	 * @tests java.lang.Process#getInputStream(), 
	 *        java.lang.Process#getErrorStream()
	 *        java.lang.Process#getOutputStream()
	 * Tests if these methods return buffered streams.
	 */
	public void test_isBufferedStreams() {
		// Regression test for HARMONY-2735.
		try {
			Object[] execArgs = Support_Exec.execJava2(new String[0], null, true);
			Process p = (Process) execArgs[0];
			InputStream in = p.getInputStream();
                  assertTrue("getInputStream() returned non-buffered stream: " + in, (in instanceof BufferedInputStream));
                  in = p.getErrorStream();
                  assertTrue("getErrorStream() returned non-buffered stream: " + in, (in instanceof BufferedInputStream));
                  OutputStream out = p.getOutputStream();
                  assertTrue("getOutputStream() returned non-buffered stream: " + out, (out instanceof BufferedOutputStream));
                  in.close();
                  out.close();
                  p.destroy();
		} catch (Exception ex) {
			fail("Unexpected exception got: " + ex);
		}
	}
}

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tests.support.Support_Exec;

public class ProcessTest extends junit.framework.TestCase {

	/**
	 * @tests java.lang.Process#getInputStream()
	 */
	public void test_getInputStream() {
		try {
			// Test for:
			Object[] execArgs = Support_Exec.execJava2(
					new String[] { "tests.support.Support_AvailTest" }, null,
					true);
			Process proc = (Process) execArgs[0];

			OutputStream os = proc.getOutputStream();

			// first number indicates total stream length
			// second number indicates length of data after second space
			// this will allow us to verify length at start, middle, and end
			os.write("10 5 abcde".getBytes());
			os.close();

			InputStream is = proc.getInputStream();
			StringBuffer msg = new StringBuffer("");
			while (true) {
				int c = is.read();
				if (c == -1)
					break;
				msg.append((char) c);
			}
			is.close();
			proc.waitFor();
			Support_Exec.checkStderr(execArgs);
			proc.destroy();
			assertEquals("true", msg.toString(), msg.toString());
		} catch (IOException e) {
			fail("IOException executing avail test: " + e);
		} catch (InterruptedException e) {
			fail("InterruptedException executing avail test: " + e);
		}
	}

	/**
	 * @tests java.lang.Process#getOutputStream()
	 */
	public void test_getOutputStream() {
		try {
			Object[] execArgs = Support_Exec
					.execJava2(
							new String[] { "tests.support.Support_ProcessReadWriteTest" },
							null, true);
			Process proc = (Process) execArgs[0];

			OutputStream os = proc.getOutputStream();

			// send data, and check if it is echoed back correctly
			String str1 = "Some data for testing communication between processes\n";
			String str2 = "More data that serves the same purpose.\n";
			String str3 = "Here is some more data.\n";
			os.write(str1.getBytes());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			os.write(str2.getBytes());
			os.write(str3.getBytes());
			os.close();

			InputStream is = proc.getInputStream();
			StringBuffer msg = new StringBuffer("");
			while (true) {
				int c = is.read();
				if (c == -1)
					break;
				msg.append((char) c);
			}
			is.close();
			proc.waitFor();
			Support_Exec.checkStderr(execArgs);
			proc.destroy();
			String org = str1 + str2 + str3;
			String recvd = msg.toString();
			if (!recvd.equals(org)) {
				System.out.println("Sent:");
				for (int i = 0; i < org.length(); i++) {
					if (i != 0 && i % 16 == 0)
						System.out.println();
					System.out.print(Integer.toHexString(org.charAt(i)) + " ");
				}
				System.out.println();
				System.out.println("Received:");
				for (int i = 0; i < recvd.length(); i++) {
					if (i != 0 && i % 16 == 0)
						System.out.println();
					System.out
							.print(Integer.toHexString(recvd.charAt(i)) + " ");
				}
				System.out.println();
			}
			assertTrue("Data returned did not match data sent. Received: '"
					+ recvd + "' sent: '" + org + "'", recvd.equals(org));
		} catch (IOException e) {
			fail("IOException executing avail test: " + e);
		} catch (InterruptedException e) {
			fail("InterruptedException executing avail test: " + e);
		}
	}

	protected void setUp() {
	}

	protected void tearDown() {
	}

	protected void doneSuite() {
	}
}

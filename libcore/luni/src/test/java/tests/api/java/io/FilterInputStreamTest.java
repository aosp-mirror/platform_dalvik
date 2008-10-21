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

import java.io.IOException;

import tests.support.Support_PlatformFile;

public class FilterInputStreamTest extends junit.framework.TestCase {

	static class MyFilterInputStream extends java.io.FilterInputStream {
		public MyFilterInputStream(java.io.InputStream is) {
			super(is);
		}
	}

	private String fileName;

	private java.io.InputStream is;

	byte[] ibuf = new byte[4096];

	public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

	/**
	 * @tests java.io.FilterInputStream#available()
	 */
	public void test_available() {
		// Test for method int java.io.FilterInputStream.available()
		try {
			assertTrue("Returned incorrect number of available bytes", is
					.available() == fileString.length());
		} catch (Exception e) {
			fail("Exception during available test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FilterInputStream#close()
	 */
	public void test_close() {
		// Test for method void java.io.FilterInputStream.close()
		try {
			is.close();
		} catch (java.io.IOException e) {
			fail("Exception attempting to close stream : " + e.getMessage());
		}

		try {
			is.read();
		} catch (java.io.IOException e) {
			return;
		}
		fail("Able to read from closed stream");
	}

	/**
	 * @tests java.io.FilterInputStream#mark(int)
	 */
	public void test_markI() {
		// Test for method void java.io.FilterInputStream.mark(int)
		assertTrue("Mark not supported by parent InputStream", true);
	}

	/**
	 * @tests java.io.FilterInputStream#markSupported()
	 */
	public void test_markSupported() {
		// Test for method boolean java.io.FilterInputStream.markSupported()
		assertTrue("markSupported returned true", !is.markSupported());
	}

	/**
	 * @tests java.io.FilterInputStream#read()
	 */
	public void test_read() {
		// Test for method int java.io.FilterInputStream.read()
		try {
			int c = is.read();
			assertTrue("read returned incorrect char", c == fileString
					.charAt(0));
		} catch (Exception e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FilterInputStream#read(byte[])
	 */
	public void test_read$B() {
		// Test for method int java.io.FilterInputStream.read(byte [])
		byte[] buf1 = new byte[100];
		try {
			is.read(buf1);
			assertTrue("Failed to read correct data", new String(buf1, 0,
					buf1.length).equals(fileString.substring(0, 100)));
		} catch (Exception e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FilterInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() {
		// Test for method int java.io.FilterInputStream.read(byte [], int, int)
		byte[] buf1 = new byte[100];
		try {
			is.skip(3000);
			is.mark(1000);
			is.read(buf1, 0, buf1.length);
			assertTrue("Failed to read correct data", new String(buf1, 0,
					buf1.length).equals(fileString.substring(3000, 3100)));
		} catch (Exception e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FilterInputStream#reset()
	 */
	public void test_reset() {
		// Test for method void java.io.FilterInputStream.reset()
        try {
            is.reset();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
	}

	/**
	 * @tests java.io.FilterInputStream#skip(long)
	 */
	public void test_skipJ() {
		// Test for method long java.io.FilterInputStream.skip(long)
		byte[] buf1 = new byte[10];
		try {
			is.skip(1000);
			is.read(buf1, 0, buf1.length);
			assertTrue("Failed to skip to correct position", new String(buf1,
					0, buf1.length).equals(fileString.substring(1000, 1010)));
		} catch (Exception e) {
			fail("Exception during skip test");
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		try {
			fileName = System.getProperty("user.dir");
			String separator = System.getProperty("file.separator");
			if (fileName.charAt(fileName.length() - 1) == separator.charAt(0))
				fileName = Support_PlatformFile.getNewPlatformFile(fileName,
						"input.tst");
			else
				fileName = Support_PlatformFile.getNewPlatformFile(fileName
						+ separator, "input.tst");
			java.io.OutputStream fos = new java.io.FileOutputStream(fileName);
			fos.write(fileString.getBytes());
			fos.close();
			is = new MyFilterInputStream(new java.io.FileInputStream(fileName));
		} catch (java.io.IOException e) {
			System.out.println("Exception during setup");
			e.printStackTrace();
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		try {
			is.close();
		} catch (Exception e) {
			System.out.println("Exception during BIS tearDown");
		}
		new java.io.File(fileName).delete();
	}
}

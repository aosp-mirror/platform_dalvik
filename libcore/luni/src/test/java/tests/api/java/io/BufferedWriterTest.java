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

import java.io.BufferedWriter;
import java.io.IOException;

import tests.support.Support_StringWriter;

public class BufferedWriterTest extends junit.framework.TestCase {

	BufferedWriter bw;

	Support_StringWriter sw;

	public String testString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

	/**
	 * @tests java.io.BufferedWriter#BufferedWriter(java.io.Writer)
	 */
	public void test_ConstructorLjava_io_Writer() {
		// Test for method java.io.BufferedWriter(java.io.Writer)

		sw = new Support_StringWriter();
		bw = new BufferedWriter(sw);
		sw.write("Hi");
		assertEquals("Constructor failed", "Hi", sw.toString());

	}

	/**
	 * @tests java.io.BufferedWriter#BufferedWriter(java.io.Writer, int)
	 */
	public void test_ConstructorLjava_io_WriterI() {
		// Test for method java.io.BufferedWriter(java.io.Writer, int)
		assertTrue("Used in tests", true);
	}

	/**
	 * @tests java.io.BufferedWriter#close()
	 */
	public void test_close() {
		// Test for method void java.io.BufferedWriter.close()
		try {
			bw.close();
			bw.write(testString);
		} catch (IOException e) {
		}
		assertTrue("Write after close", !sw.toString().equals(testString));
	}

	/**
	 * @tests java.io.BufferedWriter#flush()
	 */
	public void test_flush() {
		// Test for method void java.io.BufferedWriter.flush()
		try {
			bw.write("This should not cause a flush");
			assertTrue("Bytes written without flush", sw.toString().equals(""));
			bw.flush();
			assertEquals("Bytes not flushed", 
					"This should not cause a flush", sw.toString());
		} catch (Exception e) {
			fail("Exception during flush test");
		}
	}

	/**
	 * @tests java.io.BufferedWriter#newLine()
	 */
	public void test_newLine() {
		// Test for method void java.io.BufferedWriter.newLine()
		try {
			String separator = System.getProperty("line.separator");
			bw.write("Hello");
			bw.newLine();
			bw.write("World");
			bw.flush();
			assertTrue("Incorrect string written: " + sw.toString(), sw
					.toString().equals("Hello" + separator + "World"));
		} catch (Exception e) {
			fail("Exception during write test");
		}
	}

	/**
	 * @tests java.io.BufferedWriter#write(char[], int, int)
	 */
	public void test_write$CII() {
		// Test for method void java.io.BufferedWriter.write(char [], int, int)
		try {
			char[] testCharArray = testString.toCharArray();
			bw.write(testCharArray, 500, 1000);
			bw.flush();
			assertTrue("Incorrect string written", sw.toString().equals(
					testString.substring(500, 1500)));
		} catch (Exception e) {
			fail("Exception during write test");
		}
	}
	
	/**
	 * @tests java.io.BufferedWriter#write(char[], int, int)
	 */
	public void test_write_$CII_Exception() throws IOException {
		BufferedWriter bWriter = new BufferedWriter(sw);
		char[] nullCharArray = null;

		try {
			bWriter.write(nullCharArray, -1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		try {
			bWriter.write(nullCharArray, -1, 0);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		try {
			bWriter.write(nullCharArray, 0, -1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		try {
			bWriter.write(nullCharArray, 0, 0);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		char[] testCharArray = testString.toCharArray();

		bWriter.write(testCharArray, 0, 0);

		bWriter.write(testCharArray, testCharArray.length, 0);
		
		try {
			bWriter.write(testCharArray, testCharArray.length + 1, 0);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		bWriter.close();

		try {
			bWriter.write(nullCharArray, -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
	}

	/**
	 * @tests java.io.BufferedWriter#write(int)
	 */
	public void test_writeI() {
		// Test for method void java.io.BufferedWriter.write(int)
		try {
			bw.write('T');
			assertTrue("Char written without flush", sw.toString().equals(""));
			bw.flush();
			assertEquals("Incorrect char written", "T", sw.toString());
		} catch (Exception e) {
			fail("Exception during write test");
		}
	}

	/**
	 * @tests java.io.BufferedWriter#write(java.lang.String, int, int)
	 */
	public void test_writeLjava_lang_StringII() {
		// Test for method void java.io.BufferedWriter.write(java.lang.String,
		// int, int)
		try {
			bw.write(testString);
			bw.flush();
			assertTrue("Incorrect string written", sw.toString().equals(
					testString));
		} catch (Exception e) {
			fail("Exception during write test");
		}
	}
	
	/**
	 * @tests java.io.BufferedWriter#write(java.lang.String, int, int)
	 */
	public void test_write_LStringII_Exception() throws IOException {
		BufferedWriter bWriter = new BufferedWriter(sw);

		bWriter.write((String) null , -1, -1);		
		bWriter.write((String) null , -1, 0);
        bWriter.write((String) null , 0 , -1);
        bWriter.write((String) null , 0 , 0);
        
		try {
			bWriter.write((String) null , -1, 1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		bWriter.write(testString, 0, 0);
		bWriter.write(testString, testString.length(), 0);		
		bWriter.write(testString, testString.length() + 1, 0);
		
		try {
			bWriter.write(testString, testString.length() + 1, 1);
			fail("should throw StringIndexOutOfBoundsException");
		} catch (StringIndexOutOfBoundsException e) {
			// expected
		}

		bWriter.close();

		try {
			bWriter.write((String) null , -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
		
		try {
			bWriter.write((String) null , -1, 1);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
		
		try {
			bWriter.write(testString , -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		sw = new Support_StringWriter();
		bw = new BufferedWriter(sw, 500);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {

		try {
			bw.close();
		} catch (Exception e) {
		}
	}
}

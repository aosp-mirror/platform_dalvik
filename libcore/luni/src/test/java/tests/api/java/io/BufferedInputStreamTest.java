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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.TestCase;
import tests.support.Support_PlatformFile;

public class BufferedInputStreamTest extends TestCase {

	public String fileName;

	private BufferedInputStream is;

	private FileInputStream isFile;

	byte[] ibuf = new byte[4096];

	public String fileString = "Test_All_Tests\nTest_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

	/**
	 * @throws IOException 
	 * @tests java.io.BufferedInputStream#BufferedInputStream(java.io.InputStream,
	 *        int)
	 */
	public void test_ConstructorLjava_io_InputStreamI() throws IOException {
		// Test for method java.io.BufferedInputStream(java.io.InputStream, int)
		boolean exceptionFired = false;
		try {
			// Create buffer with exact size of file
			is = new BufferedInputStream(isFile, this.fileString
					.length());
			// Ensure buffer gets filled by evaluating one read
			is.read();
			// Close underlying FileInputStream, all but 1 buffered bytes should
			// still be available.
			isFile.close();
			// Read the remaining buffered characters, no IOException should
			// occur.
			is.skip(this.fileString.length() - 2);
			is.read();
			try {
				// is.read should now throw an exception because it will have to
				// be filled.
				is.read();
			} catch (IOException e) {
				exceptionFired = true;
			}
			assertTrue("Exception should have been triggered by read()",
					exceptionFired);
		} catch (IOException e) {
			fail("Exception during test_1_Constructor");
		}
		
		// regression test for harmony-2407
		new testBufferedInputStream(null);
		assertNotNull(testBufferedInputStream.buf);
		testBufferedInputStream.buf = null;
		new testBufferedInputStream(null, 100);
		assertNotNull(testBufferedInputStream.buf);
	}
	
	static class testBufferedInputStream extends BufferedInputStream {
		static byte[] buf;
		testBufferedInputStream(InputStream is) throws IOException {
			super(is);
			buf = super.buf;
		}

		testBufferedInputStream(InputStream is, int size) throws IOException {
			super(is, size);
			buf = super.buf;
		}
	}

	/**
	 * @tests java.io.BufferedInputStream#available()
	 */
	public void test_available() {
		// Test for method int java.io.BufferedInputStream.available()
		try {
			assertTrue("Returned incorrect number of available bytes", is
					.available() == fileString.length());
		} catch (IOException e) {
			fail("Exception during available test");
		}

		// Test that a closed stream throws an IOE for available()
		BufferedInputStream bis = new BufferedInputStream(
				new ByteArrayInputStream(new byte[] { 'h', 'e', 'l', 'l', 'o',
						' ', 't', 'i', 'm' }));
		int available;
		try {
			available = bis.available();
			bis.close();
		} catch (IOException ex) {
			fail();
			return; // never reached.
		}
		assertTrue(available != 0);

		try {
			bis.available();
			fail("Expected test to throw IOE.");
		} catch (IOException ex) {
			// expected
		} catch (Throwable ex) {
			fail("Expected test to throw IOE not "
					+ ex.getClass().getName());
		}
	}

	/**
	 * @throws IOException 
	 * @tests java.io.BufferedInputStream#close()
	 */
	public void test_close() throws IOException {
		// Test for method void java.io.BufferedInputStream.close()
		new BufferedInputStream(isFile);
		new BufferedInputStream(isFile);
		
		//regression for HARMONY-667
        BufferedInputStream buf = new BufferedInputStream(null, 5);
        buf.close();                           
	}

	/**
	 * @tests java.io.BufferedInputStream#mark(int)
	 */
	public void test_markI() {
		// Test for method void java.io.BufferedInputStream.mark(int)
		byte[] buf1 = new byte[100];
		byte[] buf2 = new byte[100];
		try {
			is.skip(3000);
			is.mark(1000);
			is.read(buf1, 0, buf1.length);
			is.reset();
			is.read(buf2, 0, buf2.length);
			is.reset();
			assertTrue("Failed to mark correct position", new String(buf1, 0,
					buf1.length).equals(new String(buf2, 0, buf2.length)));

		} catch (IOException e) {
			fail("Exception during mark test");
		}

		byte[] bytes = new byte[256];
		for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
		InputStream in = new BufferedInputStream(
				new ByteArrayInputStream(bytes), 12);
		try {
			in.skip(6);
			in.mark(14);
			in.read(new byte[14], 0, 14);
			in.reset();
			assertTrue("Wrong bytes", in.read() == 6 && in.read() == 7);
		} catch (IOException e) {
			fail("Exception during mark test 2");
		}

		in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12);
		try {
			in.skip(6);
			in.mark(8);
			in.skip(7);
			in.reset();
			assertTrue("Wrong bytes 2", in.read() == 6 && in.read() == 7);
		} catch (IOException e) {
			fail("Exception during mark test 3");
		}
	}

	/**
	 * @tests java.io.BufferedInputStream#markSupported()
	 */
	public void test_markSupported() {
		// Test for method boolean java.io.BufferedInputStream.markSupported()
		assertTrue("markSupported returned incorrect value", is.markSupported());
	}

	/**
	 * @tests java.io.BufferedInputStream#read()
	 */
	public void test_read() {
		// Test for method int java.io.BufferedInputStream.read()

		try {

			int c = is.read();
			assertTrue("read returned incorrect char", c == fileString
					.charAt(0));
		} catch (IOException e) {
			fail("Exception during read test" + e.toString());
		}

		byte[] bytes = new byte[256];
		for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
		InputStream in = new BufferedInputStream(
				new ByteArrayInputStream(bytes), 12);
		try {
			assertEquals("Wrong initial byte", 0, in.read()); // Fill the
			// buffer
			byte[] buf = new byte[14];
			in.read(buf, 0, 14); // Read greater than the buffer
			assertTrue("Wrong block read data", new String(buf, 0, 14)
					.equals(new String(bytes, 1, 14)));
			assertEquals("Wrong bytes", 15, in.read()); // Check next byte
		} catch (IOException e) {
			fail("Exception during read test 2");
		}
	}

	/**
	 * @tests java.io.BufferedInputStream#read(byte[], int, int)
	 */
	public void test_read$BII_Exception() throws IOException {
		BufferedInputStream bis = new BufferedInputStream(null);

		try {
			bis.read(null, -1, -1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			//expected
		}

		try {
			bis.read(new byte[0], -1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			bis.read(new byte[0], 1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			bis.read(new byte[0], 1, 1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		bis.close();
		
		try {
			bis.read(null, -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			//expected
		}
	}
	
	/**
	 * @tests java.io.BufferedInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() {
		// Test for method int java.io.BufferedInputStream.read(byte [], int,
		// int)
		byte[] buf1 = new byte[100];
		try {
			is.skip(3000);
			is.mark(1000);
			is.read(buf1, 0, buf1.length);
			assertTrue("Failed to read correct data", new String(buf1, 0,
					buf1.length).equals(fileString.substring(3000, 3100)));

		} catch (IOException e) {
			fail("Exception during read test");
		}

		BufferedInputStream bufin = new BufferedInputStream(new InputStream() {
			int size = 2, pos = 0;

			byte[] contents = new byte[size];

			@Override
            public int read() throws IOException {
				if (pos >= size) {
                    throw new IOException("Read past end of data");
                }
				return contents[pos++];
			}

			@Override
            public int read(byte[] buf, int off, int len) throws IOException {
				if (pos >= size) {
                    throw new IOException("Read past end of data");
                }
				int toRead = len;
				if (toRead > available()) {
                    toRead = available();
                }
				System.arraycopy(contents, pos, buf, off, toRead);
				pos += toRead;
				return toRead;
			}

			@Override
            public int available() {
				return size - pos;
			}
		});
		try {
			bufin.read();
			int result = bufin.read(new byte[2], 0, 2);
			assertTrue("Incorrect result: " + result, result == 1);
		} catch (IOException e) {
			fail("Unexpected: " + e);
		}
	}

	/**
	 * @tests java.io.BufferedInputStream#reset()
	 */
	public void test_reset() {
		// Test for method void java.io.BufferedInputStream.reset()

		byte[] buf1 = new byte[10];
		byte[] buf2 = new byte[10];
		try {
			is.mark(2000);
			is.read(buf1, 0, 10);
			is.reset();
			is.read(buf2, 0, 10);
			is.reset();
			assertTrue("Reset failed", new String(buf1, 0, buf1.length)
					.equals(new String(buf2, 0, buf2.length)));

			BufferedInputStream bIn = new BufferedInputStream(
					new ByteArrayInputStream("1234567890".getBytes()));
			bIn.mark(10);
			for (int i = 0; i < 11; i++) {
				bIn.read();
			}
			bIn.reset();

		} catch (IOException e) {
			fail("Exception during reset test");
		}
	}
	
    /**
     * @tests java.io.BufferedInputStream#reset()
	 */
	public void test_reset_Exception() throws IOException {
		BufferedInputStream bis = new BufferedInputStream(null);
		
		//throws IOExcepiton with message "Mark has been invalidated"
		try {
			bis.reset();
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
		
		//does not throw IOException
		bis.mark(1);
		bis.reset();
		
		bis.close();

		//throws IOException with message "stream is closed"
		try {
			bis.reset();
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
	}


	/**
	 * @tests java.io.BufferedInputStream#skip(long)
	 */
	public void test_skipJ() {
		// Test for method long java.io.BufferedInputStream.skip(long)
		byte[] buf1 = new byte[10];
		try {
			is.mark(2000);
			is.skip(1000);
			is.read(buf1, 0, buf1.length);
			is.reset();
			assertTrue("Failed to skip to correct position", new String(buf1,
					0, buf1.length).equals(fileString.substring(1000, 1010)));
		} catch (IOException e) {
			fail("Exception during skip test");
		}

		//regression for HARMONY-667
        BufferedInputStream buf = new BufferedInputStream(null, 5);
        try {
            buf.skip(10);
            fail("Should throw IOException");
        } catch (IOException e) {
            //expected
        }                         
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	@Override
    protected void setUp() {

		try {
			fileName = System.getProperty("user.dir");
			String separator = System.getProperty("file.separator");
			if (fileName.charAt(fileName.length() - 1) == separator.charAt(0)) {
                fileName = Support_PlatformFile.getNewPlatformFile(fileName,
						"input.tst");
            } else {
                fileName = Support_PlatformFile.getNewPlatformFile(fileName
						+ separator, "input.tst");
            }
			OutputStream fos = new FileOutputStream(fileName);
			fos.write(fileString.getBytes());
			fos.close();
			isFile = new FileInputStream(fileName);
			is = new BufferedInputStream(isFile);
		} catch (IOException e) {
			System.out.println("Exception during setup");
			e.printStackTrace();
		}

	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	@Override
    protected void tearDown() {

		try {
			is.close();
		} catch (Exception e) {
			System.out.println("Exception during BIS tearDown");
		}
		try {
			File f = new File(fileName);
			f.delete();
		} catch (Exception e) {
			System.out.println("Exception during BIS tearDown");
		}
	}
}

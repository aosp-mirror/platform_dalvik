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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataOutputStreamTest extends junit.framework.TestCase {

	private DataOutputStream os;

	private DataInputStream dis;

	private ByteArrayOutputStream bos;

	String unihw = "\u0048\u0065\u006C\u006C\u006F\u0020\u0057\u006F\u0072\u006C\u0064";

	public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

	/**
	 * @tests java.io.DataOutputStream#DataOutputStream(java.io.OutputStream)
	 */
	public void test_ConstructorLjava_io_OutputStream() {
		// Test for method java.io.DataOutputStream(java.io.OutputStream)
		assertTrue("Used in all tests", true);
	}

	/**
	 * @tests java.io.DataOutputStream#flush()
	 */
	public void test_flush() {
		// Test for method void java.io.DataOutputStream.flush()
		try {
			os.writeInt(9087589);
			os.flush();
			openDataInputStream();
			int c = dis.readInt();
			dis.close();
			assertEquals("Failed to flush correctly", 9087589, c);
		} catch (IOException e) {
			fail("Exception during flush test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#size()
	 */
	public void test_size() {
		// Test for method int java.io.DataOutputStream.size()

		try {
			os.write(fileString.getBytes(), 0, 150);
			os.close();
			openDataInputStream();
			byte[] rbuf = new byte[150];
			dis.read(rbuf, 0, 150);
			dis.close();
			assertEquals("Incorrect size returned", 150, os.size());
		} catch (IOException e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#write(byte[], int, int)
	 */
	public void test_write$BII() {
		// Test for method void java.io.DataOutputStream.write(byte [], int,
		// int)
		try {
			os.write(fileString.getBytes(), 0, 150);
			os.close();
			openDataInputStream();
			byte[] rbuf = new byte[150];
			dis.read(rbuf, 0, 150);
			dis.close();
			assertTrue("Incorrect bytes written", new String(rbuf, 0, 150)
					.equals(fileString.substring(0, 150)));
		} catch (IOException e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#write(int)
	 */
	public void test_writeI() {
		// Test for method void java.io.DataOutputStream.write(int)
		try {
			os.write((int) 't');
			os.close();
			openDataInputStream();
			int c = dis.read();
			dis.close();
			assertTrue("Incorrect int written", (int) 't' == c);
		} catch (IOException e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeBoolean(boolean)
	 */
	public void test_writeBooleanZ() {
		// Test for method void java.io.DataOutputStream.writeBoolean(boolean)
		try {
			os.writeBoolean(true);
			os.close();
			openDataInputStream();
			boolean c = dis.readBoolean();
			dis.close();
			assertTrue("Incorrect boolean written", c);
		} catch (IOException e) {
			fail("Exception during writeBoolean test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeByte(int)
	 */
	public void test_writeByteI() {
		// Test for method void java.io.DataOutputStream.writeByte(int)
		try {
			os.writeByte((byte) 127);
			os.close();
			openDataInputStream();
			byte c = dis.readByte();
			dis.close();
			assertTrue("Incorrect byte written", c == (byte) 127);
		} catch (IOException e) {
			fail("Exception during writeByte test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeBytes(java.lang.String)
	 */
	public void test_writeBytesLjava_lang_String() throws IOException {
		// Test for method void
		// java.io.DataOutputStream.writeBytes(java.lang.String)
		try {
			os.write(fileString.getBytes());
			os.close();
			openDataInputStream();
			byte[] rbuf = new byte[4000];
			dis.read(rbuf, 0, fileString.length());
			dis.close();
			assertTrue("Incorrect bytes written", new String(rbuf, 0,
					fileString.length()).equals(fileString));
		} catch (IOException e) {
			fail("Exception during writeBytes test : " + e.getMessage());
		}
		// regression test for HARMONY-1101
		new DataOutputStream(null).writeBytes("");
	}

	/**
	 * @tests java.io.DataOutputStream#writeChar(int)
	 */
	public void test_writeCharI() {
		// Test for method void java.io.DataOutputStream.writeChar(int)
		try {
			os.writeChar('T');
			os.close();
			openDataInputStream();
			char c = dis.readChar();
			dis.close();
			assertEquals("Incorrect char written", 'T', c);
		} catch (IOException e) {
			fail("Exception during writeChar test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeChars(java.lang.String)
	 */
	public void test_writeCharsLjava_lang_String() {
		// Test for method void
		// java.io.DataOutputStream.writeChars(java.lang.String)
		try {
			os.writeChars("Test String");
			os.close();
			openDataInputStream();
			char[] chars = new char[50];
			int i, a = dis.available() / 2;
			for (i = 0; i < a; i++)
				chars[i] = dis.readChar();
			assertEquals("Incorrect chars written", "Test String", new String(chars, 0, i)
					);
		} catch (IOException e) {
			fail("Exception during writeChars test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeDouble(double)
	 */
	public void test_writeDoubleD() {
		// Test for method void java.io.DataOutputStream.writeDouble(double)
		try {
			os.writeDouble(908755555456.98);
			os.close();
			openDataInputStream();
			double c = dis.readDouble();
			dis.close();
			assertEquals("Incorrect double written", 908755555456.98, c);
		} catch (IOException e) {
			fail("Exception during writeDouble test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeFloat(float)
	 */
	public void test_writeFloatF() {
		// Test for method void java.io.DataOutputStream.writeFloat(float)
		try {
			os.writeFloat(9087.456f);
			os.close();
			openDataInputStream();
			float c = dis.readFloat();
			dis.close();
			assertTrue("Incorrect float written", c == 9087.456f);
		} catch (IOException e) {
			fail("Exception during writeFloattest : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeInt(int)
	 */
	public void test_writeIntI() {
		// Test for method void java.io.DataOutputStream.writeInt(int)
		try {
			os.writeInt(9087589);
			os.close();
			openDataInputStream();
			int c = dis.readInt();
			dis.close();
			assertEquals("Incorrect int written", 9087589, c);
		} catch (IOException e) {
			fail("Exception during writeInt test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeLong(long)
	 */
	public void test_writeLongJ() {
		// Test for method void java.io.DataOutputStream.writeLong(long)
		try {
			os.writeLong(908755555456L);
			os.close();
			openDataInputStream();
			long c = dis.readLong();
			dis.close();
			assertEquals("Incorrect long written", 908755555456L, c);
		} catch (IOException e) {
			fail("Exception during writeLong test" + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeShort(int)
	 */
	public void test_writeShortI() {
		// Test for method void java.io.DataOutputStream.writeShort(int)
		try {
			os.writeShort((short) 9087);
			os.close();
			openDataInputStream();
			short c = dis.readShort();
			dis.close();
			assertEquals("Incorrect short written", 9087, c);
		} catch (IOException e) {
			fail("Exception during writeShort test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataOutputStream#writeUTF(java.lang.String)
	 */
	public void test_writeUTFLjava_lang_String() {
		// Test for method void
		// java.io.DataOutputStream.writeUTF(java.lang.String)
		try {
			os.writeUTF(unihw);
			os.close();
			openDataInputStream();
			assertTrue("Failed to write string in UTF format",
					dis.available() == unihw.length() + 2);
			assertTrue("Incorrect string returned", dis.readUTF().equals(unihw));
		} catch (Exception e) {
			fail("Exception during writeUTF" + e.getMessage());
		}
	}

	private void openDataInputStream() throws IOException {
		dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		bos = new ByteArrayOutputStream();
		os = new DataOutputStream(bos);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		try {
			if (os != null)
				os.close();
			if (dis != null)
				dis.close();
		} catch (IOException e) {
		}
	}
}

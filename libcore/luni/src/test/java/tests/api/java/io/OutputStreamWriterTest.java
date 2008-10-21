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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import junit.framework.TestCase;

/**
 * 
 */
public class OutputStreamWriterTest extends TestCase {

	private static final int UPPER = 0xd800;

	private static final int BUFFER_SIZE = 10000;

	private ByteArrayOutputStream out;

	private OutputStreamWriter writer;

	static private final String source = "This is a test message with Unicode character. \u4e2d\u56fd is China's name in Chinese";

	static private final String[] MINIMAL_CHARSETS = new String[] { "US-ASCII",
			"ISO-8859-1", "UTF-16BE", "UTF-16LE", "UTF-16", "UTF-8" };

	OutputStreamWriter osw;

	InputStreamReader isr;

	private ByteArrayOutputStream fos;

	String testString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		out = new ByteArrayOutputStream();
		writer = new OutputStreamWriter(out, "utf-8");

		fos = new ByteArrayOutputStream();
		osw = new OutputStreamWriter(fos);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		try {
			writer.close();

			if (isr != null)
				isr.close();
			osw.close();
		} catch (Exception e) {
		}

		super.tearDown();
	}

	public void testClose() throws Exception {
		writer.flush();
		writer.close();
		try {
			writer.flush();
			fail();
		} catch (IOException e) {
		}
	}

	public void testFlush() throws Exception {
		writer.write(source);
		writer.flush();
		String result = out.toString("utf-8");
		assertEquals(source, result);
	}

	/*
	 * Class under test for void write(char[], int, int)
	 */
	public void testWritecharArrayintint() throws IOException {
		char[] chars = source.toCharArray();
		
		//throws IndexOutOfBoundsException if offset is negative
		try {
			writer.write((char[]) null, -1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		//throws NullPointerException though count is negative 
		try {
			writer.write((char[]) null, 1, -1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			//expected
		}
	
		try {
			writer.write((char[]) null, 1, 1);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			writer.write(new char[0], 0, 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			writer.write(chars, -1, 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			writer.write(chars, 0, -1);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			writer.write(chars, 1, chars.length);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		writer.write(chars, 1, 2);
		writer.flush();
		assertEquals("hi", out.toString("utf-8"));
		writer.write(chars, 0, chars.length);
		writer.flush();
		assertEquals("hi" + source, out.toString("utf-8"));
			
		writer.close();
        //after the stream is closed ,should throw IOException first
		try {
			writer.write((char[]) null, -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			//expected
		}

	}

	/*
	 * Class under test for void write(int)
	 */
	public void testWriteint() throws IOException {
		writer.write(1);
		writer.flush();
		String str = new String(out.toByteArray(), "utf-8");
		assertEquals("\u0001", str);

		writer.write(2);
		writer.flush();
		str = new String(out.toByteArray(), "utf-8");
		assertEquals("\u0001\u0002", str);

		writer.write(-1);
		writer.flush();
		str = new String(out.toByteArray(), "utf-8");
		assertEquals("\u0001\u0002\uffff", str);

		writer.write(0xfedcb);
		writer.flush();
		str = new String(out.toByteArray(), "utf-8");
		assertEquals("\u0001\u0002\uffff\uedcb", str);
		
		writer.close();
		 //after the stream is closed ,should throw IOException
		try {
			writer.write(1);
            fail("should throw IOException");
		} catch (IOException e) {
			//expected
		}
		
		
	}

	/*
	 * Class under test for void write(String, int, int)
	 */
	public void testWriteStringintint() throws IOException {
		try {
			writer.write((String) null, 1, 1);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			writer.write("", 0, 1);
			fail();
		} catch (StringIndexOutOfBoundsException e) {
		}
		try {
			writer.write("abc", -1, 1);
			fail();
		} catch (StringIndexOutOfBoundsException e) {
		}
		try {
			writer.write("abc", 0, -1);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			writer.write("abc", 1, 3);
			fail();
		} catch (StringIndexOutOfBoundsException e) {
		}
		
		//throws IndexOutOfBoundsException before NullPointerException if count is negative
		try {
			writer.write((String) null, -1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		//throws NullPointerException before StringIndexOutOfBoundsException 
		try {
			writer.write((String) null, -1, 0);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			//expected
		}
		
		writer.write("abc", 1, 2);
		writer.flush();
		assertEquals("bc", out.toString("utf-8"));
		writer.write(source, 0, source.length());
		writer.flush();
		assertEquals("bc" + source, out.toString("utf-8"));
		
		writer.close();
        //throws IndexOutOfBoundsException first if count is negative
		try {
			writer.write((String) null, 0, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		try {
			writer.write((String) null, -1, 0);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			//expected
		}
		
		try {
			writer.write("abc", -1, 0);
			fail("should throw StringIndexOutOfBoundsException");
		} catch (StringIndexOutOfBoundsException e) {
			//expected
		}
		
		//throws IOException
		try {
			writer.write("abc", 0, 1);
			fail("should throw IOException");
		} catch (IOException e) {
			//expected
		}

	}

	/*
	 * Class under test for void OutputStreamWriter(OutputStream)
	 */
	public void testOutputStreamWriterOutputStream() throws IOException {
		try {
			writer = new OutputStreamWriter(null);
			fail();
		} catch (NullPointerException e) {
		}
		OutputStreamWriter writer2 = new OutputStreamWriter(out);
		writer2.close();
	}

	/*
	 * Class under test for void OutputStreamWriter(OutputStream, String)
	 */
	public void testOutputStreamWriterOutputStreamString() throws IOException {
		try {
			writer = new OutputStreamWriter(null, "utf-8");
			fail();
		} catch (NullPointerException e) {
		}
		try {
			writer = new OutputStreamWriter(out, "");
			fail();
		} catch (UnsupportedEncodingException e) {
		}
		try {
			writer = new OutputStreamWriter(out, "badname");
			fail();
		} catch (UnsupportedEncodingException e) {
		}
		try {
			writer = new OutputStreamWriter(out, (String) null);
			fail();
		} catch (NullPointerException e) {
		}
		OutputStreamWriter writer2 = new OutputStreamWriter(out, "ascii");
		assertEquals(Charset.forName("ascii"), Charset.forName(writer2
				.getEncoding()));
		writer2.close();
	}

	/*
	 * Class under test for void OutputStreamWriter(OutputStream)
	 */
	public void testOutputStreamWriterOutputStreamCharset() throws IOException {
		Charset cs = Charset.forName("ascii");
		try {
			writer = new OutputStreamWriter(null, cs);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			writer = new OutputStreamWriter(out, (Charset) null);
			fail();
		} catch (NullPointerException e) {
		}
		OutputStreamWriter writer2 = new OutputStreamWriter(out, cs);
		assertEquals(cs, Charset.forName(writer2.getEncoding()));
		writer2.close();
	}

	/*
	 * Class under test for void OutputStreamWriter(OutputStream, String)
	 */
	public void testOutputStreamWriterOutputStreamCharsetEncoder()
			throws IOException {
		Charset cs = Charset.forName("ascii");
		CharsetEncoder enc = cs.newEncoder();
		try {
			writer = new OutputStreamWriter(null, enc);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			writer = new OutputStreamWriter(out, (CharsetEncoder) null);
			fail();
		} catch (NullPointerException e) {
		}
		OutputStreamWriter writer2 = new OutputStreamWriter(out, cs);
		assertEquals(cs, Charset.forName(writer2.getEncoding()));
		writer2.close();
	}

	public void testGetEncoding() {
		Charset cs = Charset.forName("utf-8");
		assertEquals(cs, Charset.forName(writer.getEncoding()));
	}

	public void testHandleEarlyEOFChar_1() {
		String str = "All work and no play makes Jack a dull boy\n"; //$NON-NLS-1$
		int NUMBER = 2048;
		int j = 0;
		int len = str.length() * NUMBER;
		/* == 88064 *//* NUMBER compulsively written copies of the same string */
		char[] strChars = new char[len];
		for (int i = 0; i < NUMBER; ++i) {
			for (int k = 0; k < str.length(); ++k) {
				strChars[j++] = str.charAt(k);
			}
		}
		File f = null;
		FileWriter fw = null;
		try {
			f = File.createTempFile("ony", "by_one");
			fw = new FileWriter(f);
			fw.write(strChars);
			fw.close();
			InputStreamReader in = null;
			FileInputStream fis = new FileInputStream(f);
			in = new InputStreamReader(fis);
			int b;
			int errors = 0;
			for (int offset = 0; offset < strChars.length; ++offset) {
				b = in.read();
				if (b == -1) {
					fail("Early EOF at offset " + offset + "\n");
					return;
				}
			}
			assertEquals(0, errors);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testHandleEarlyEOFChar_2() throws IOException {
		int capacity = 65536;
		byte[] bytes = new byte[capacity];
		byte[] bs = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = bs[i / 8192];
		}
		String inputStr = new String(bytes);
		int len = inputStr.length();
		File f = File.createTempFile("FileWriterBugTest ", null); //$NON-NLS-1$
		FileWriter writer = new FileWriter(f);
		writer.write(inputStr);
		writer.close();
		long flen = f.length();

		FileReader reader = new FileReader(f);
		char[] outChars = new char[capacity];
		int outCount = reader.read(outChars);
		String outStr = new String(outChars, 0, outCount);

		f.deleteOnExit();
		assertEquals(len, flen);
		assertEquals(inputStr, outStr);

	}

	public void testSingleCharIO() throws Exception {
		InputStreamReader isr = null;
		for (int i = 0; i < MINIMAL_CHARSETS.length; ++i) {
			try {
				out = new ByteArrayOutputStream();
				writer = new OutputStreamWriter(out, MINIMAL_CHARSETS[i]);

				int upper = UPPER;
				switch (i) {
				case 0:
					upper = 128;
					break;
				case 1:
					upper = 256;
					break;
				}

				for (int c = 0; c < upper; ++c) {
					writer.write(c);
				}
				writer.flush();
				byte[] result = out.toByteArray();

				isr = new InputStreamReader(new ByteArrayInputStream(result),
						MINIMAL_CHARSETS[i]);
				for (int expected = 0; expected < upper; ++expected) {
					assertEquals("Error when reading bytes in "
							+ MINIMAL_CHARSETS[i], expected, isr.read());
				}
			} finally {
				try {
					isr.close();
				} catch (Exception e) {
				}
				try {
					writer.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public void testBlockIO() throws Exception {
		InputStreamReader isr = null;
		char[] largeBuffer = new char[BUFFER_SIZE];
		for (int i = 0; i < MINIMAL_CHARSETS.length; ++i) {
			try {
				out = new ByteArrayOutputStream();
				writer = new OutputStreamWriter(out, MINIMAL_CHARSETS[i]);

				int upper = UPPER;
				switch (i) {
				case 0:
					upper = 128;
					break;
				case 1:
					upper = 256;
					break;
				}

				int m = 0;
				for (int c = 0; c < upper; ++c) {
					largeBuffer[m++] = (char) c;
					if (m == BUFFER_SIZE) {
						writer.write(largeBuffer);
						m = 0;
					}
				}
				writer.write(largeBuffer, 0, m);
				writer.flush();
				byte[] result = out.toByteArray();

				isr = new InputStreamReader(new ByteArrayInputStream(result),
						MINIMAL_CHARSETS[i]);
				int expected = 0, read = 0, j = 0;
				while (expected < upper) {
					if (j == read) {
						read = isr.read(largeBuffer);
						j = 0;
					}
					assertEquals("Error when reading bytes in "
							+ MINIMAL_CHARSETS[i], expected++, largeBuffer[j++]);
				}
			} finally {
				try {
					isr.close();
				} catch (Exception e) {
				}
				try {
					writer.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * @tests java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
	 */
	public void test_ConstructorLjava_io_OutputStream() {
		// Test for method java.io.OutputStreamWriter(java.io.OutputStream)
		assertTrue("Used in tests", true);
	}

	/**
	 * @tests java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_io_OutputStreamLjava_lang_String() {
		// Test for method java.io.OutputStreamWriter(java.io.OutputStream,
		// java.lang.String)
		try {
			osw = new OutputStreamWriter(fos, "8859_1");
		} catch (UnsupportedEncodingException e) {
			fail("Unable to create output stream : " + e.getMessage());
		}
		try {
			osw = new OutputStreamWriter(fos, "Bogus");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		fail("Failed to throw Unsupported Encoding exception");
	}

	/**
	 * @tests java.io.OutputStreamWriter#close()
	 */
	public void test_close() {
		// Test for method void java.io.OutputStreamWriter.close()
		boolean exception = false;
		try {
			osw.close();
			osw.write(testString, 0, testString.length());
		} catch (IOException e) {
			exception = true;
		}
		assertTrue("Chars written after close", exception);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			OutputStreamWriter writer = new OutputStreamWriter(bout,
					"ISO2022JP");
			writer.write(new char[] { 'a' });
			writer.close();
			// the default is ASCII, there should not be any mode changes
			String converted = new String(bout.toByteArray(), "ISO8859_1");
			assertTrue("invalid conversion 1: " + converted, converted
					.equals("a"));

			bout.reset();
			writer = new OutputStreamWriter(bout, "ISO2022JP");
			writer.write(new char[] { '\u3048' });
			writer.flush();
			// the byte sequence should not switch to ASCII mode until the
			// stream is closed
			converted = new String(bout.toByteArray(), "ISO8859_1");
			assertTrue("invalid conversion 2: " + converted, converted
					.equals("\u001b$B$("));
			writer.close();
			converted = new String(bout.toByteArray(), "ISO8859_1");
			assertTrue("invalid conversion 3: " + converted, converted
					.equals("\u001b$B$(\u001b(B"));

			bout.reset();
			writer = new OutputStreamWriter(bout, "ISO2022JP");
			writer.write(new char[] { '\u3048' });
			writer.write(new char[] { '\u3048' });
			writer.close();
			// there should not be a mode switch between writes
			assertEquals("invalid conversion 4", "\u001b$B$($(\u001b(B", new String(bout.toByteArray(),
					"ISO8859_1"));
		} catch (UnsupportedEncodingException e) {
			// Can't test missing converter
			System.out.println(e);
		} catch (IOException e) {
			fail("Unexpected: " + e);
		}
	}

	/**
	 * @tests java.io.OutputStreamWriter#flush()
	 */
	public void test_flush() {
		// Test for method void java.io.OutputStreamWriter.flush()
		try {
			char[] buf = new char[testString.length()];
			osw.write(testString, 0, testString.length());
			osw.flush();
			openInputStream();
			isr.read(buf, 0, buf.length);
			assertTrue("Chars not flushed", new String(buf, 0, buf.length)
					.equals(testString));
		} catch (Exception e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.OutputStreamWriter#getEncoding()
	 */
	public void test_getEncoding() {
		// Test for method java.lang.String
		// java.io.OutputStreamWriter.getEncoding()
		try {
			osw = new OutputStreamWriter(fos, "8859_1");
		} catch (UnsupportedEncodingException e) {
			assertEquals("Returned incorrect encoding", 
					"8859_1", osw.getEncoding());
		}
	}

	/**
	 * @tests java.io.OutputStreamWriter#write(char[], int, int)
	 */
	public void test_write$CII() {
		// Test for method void java.io.OutputStreamWriter.write(char [], int,
		// int)
		try {
			char[] buf = new char[testString.length()];
			osw.write(testString, 0, testString.length());
			osw.close();
			openInputStream();
			isr.read(buf, 0, buf.length);
			assertTrue("Incorrect chars returned", new String(buf, 0,
					buf.length).equals(testString));
		} catch (Exception e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.OutputStreamWriter#write(int)
	 */
	public void test_writeI() {
		// Test for method void java.io.OutputStreamWriter.write(int)
		try {
			osw.write('T');
			osw.close();
			openInputStream();
			int c = isr.read();
			assertEquals("Incorrect char returned", 'T', (char) c);
		} catch (Exception e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.OutputStreamWriter#write(java.lang.String, int, int)
	 */
	public void test_writeLjava_lang_StringII() {
		// Test for method void
		// java.io.OutputStreamWriter.write(java.lang.String, int, int)

		try {
			char[] buf = new char[testString.length()];
			osw.write(testString, 0, testString.length());
			osw.close();
			openInputStream();
			isr.read(buf);
			assertTrue("Incorrect chars returned", new String(buf, 0,
					buf.length).equals(testString));
		} catch (Exception e) {
			fail("Exception during write test : " + e.getMessage());
		}
	}

	private void openInputStream() {
		isr = new InputStreamReader(new ByteArrayInputStream(fos.toByteArray()));
	}

}

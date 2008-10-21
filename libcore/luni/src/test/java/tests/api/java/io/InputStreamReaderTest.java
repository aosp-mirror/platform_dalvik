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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * 
 */
public class InputStreamReaderTest extends TestCase {

	private final String source = "This is a test message with Unicode character. \u4e2d\u56fd is China's name in Chinese";

	private InputStream in;

	private InputStreamReader reader;

	private InputStreamReader is;

	private InputStream fis;

	public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

	static class LimitedByteArrayInputStream extends ByteArrayInputStream {
        
		// A ByteArrayInputStream that only returns a single byte per read
		byte[] bytes;

		int count;

		public LimitedByteArrayInputStream(int type) {
			super(new byte[0]);
			switch (type) {
			case 0:
				bytes = new byte[] { 0x61, 0x72 };
				break;
			case 1:
				bytes = new byte[] { (byte) 0xff, (byte) 0xfe, 0x61, 0x72 };
				break;
			case 2:
				bytes = new byte[] { '\u001b', '$', 'B', '6', 'e', 'B', 'h',
						'\u001b', '(', 'B' };
				break;
			}
			count = bytes.length;
		}

		public int read() {
			if (count == 0)
				return -1;
			count--;
			return bytes[bytes.length - count];
		}

		public int read(byte[] buffer, int offset, int length) {
			if (count == 0)
				return -1;
			if (length == 0)
				return 0;
			buffer[offset] = bytes[bytes.length - count];
			count--;
			return 1;
		}

		public int available() {
			return count;
		}
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		try {
			in = new ByteArrayInputStream(source.getBytes("UTF-8"));
			reader = new InputStreamReader(in, "UTF-8");

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(bos);
			char[] buf = new char[fileString.length()];
			fileString.getChars(0, fileString.length(), buf, 0);
			osw.write(buf);
			osw.close();
			fis = new ByteArrayInputStream(bos.toByteArray());
			is = new InputStreamReader(fis);
		} catch (Exception e) {
			fail("Exception during setUp : " + e.getMessage());
		}
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		try {
			in.close();
			is.close();
			fis.close();
		} catch (IOException e) {
		}

		super.tearDown();
	}

	public void testClose() throws IOException {
		reader.close();
		try {
			reader.ready();
			fail("Should throw IOException");
		} catch (IOException e) {
		}
		reader.close();
	}

	/*
	 * Class under test for int read()
	 */
	public void testRead() throws IOException {
		assertEquals('T', (char) reader.read());
		assertEquals('h', (char) reader.read());
		assertEquals('i', (char) reader.read());
		assertEquals('s', (char) reader.read());
		assertEquals(' ', (char) reader.read());
		reader.read(new char[source.length() - 5], 0, source.length() - 5);
		assertEquals(-1, reader.read());
	}

    /*
     * Class under test for int read()
     * Regression for Harmony-411
     */
    public void testRead1() throws IOException {
        // if the decoder is constructed by InputStreamReader itself, the decoder's 
        // default error action is REPLACE
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
                new byte[] { -32, -96 }), "UTF-8");
        assertEquals("read() return incorrect value", 65533, isr.read());
        
        InputStreamReader isr2 = new InputStreamReader(new ByteArrayInputStream(
                new byte[] { -32, -96 }), Charset.forName("UTF-8"));
        assertEquals("read() return incorrect value", 65533, isr2.read());
        
        // if the decoder is passed in, keep its status intacted
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        InputStreamReader isr3 = new InputStreamReader(new ByteArrayInputStream(
                new byte[] { -32, -96 }), decoder);
        try{
           isr3.read();
           fail("Should throw MalformedInputException");
        }catch(MalformedInputException e){
            //expected
        }
        
        CharsetDecoder decoder2 = Charset.forName("UTF-8").newDecoder();
        decoder2.onMalformedInput(CodingErrorAction.IGNORE);
        InputStreamReader isr4 = new InputStreamReader(new ByteArrayInputStream(
                new byte[] { -32, -96 }), decoder2);
        assertEquals("read() return incorrect value", -1, isr4.read());
        
        CharsetDecoder decoder3 = Charset.forName("UTF-8").newDecoder();
        decoder3.onMalformedInput(CodingErrorAction.REPLACE);
        InputStreamReader isr5 = new InputStreamReader(new ByteArrayInputStream(
                new byte[] { -32, -96 }), decoder3);
        assertEquals("read() return incorrect value", 65533, isr5.read());
    }

	/*
	 * Class under test for int read(char[], int, int)
	 */
	public void testReadcharArrayintint() throws IOException {
		//throws IndexOutOfBoundsException before NullPointerException
		try {
			reader.read(null, -1, 1);
			fail("Should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		//throws NullPointerException before IndexOutOfBoundsException
		try {
			reader.read(null, 0, -1);
			fail("Should throw NullPointerException");
		} catch (NullPointerException e) {
			//expected
		}
		
		try {
			reader.read(null, 0, 1);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			reader.read(new char[3], -1, 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			reader.read(new char[3], 0, -1);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			reader.read(new char[3], 1, 3);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		assertEquals(0, reader.read(new char[3], 3, 0));
		char[] chars = new char[source.length()];
		assertEquals(0, reader.read(chars, 0, 0));
		assertEquals(0, chars[0]);
		assertEquals(3, reader.read(chars, 0, 3));
		assertEquals(5, reader.read(chars, 3, 5));
		assertEquals(source.length() - 8, reader.read(chars, 8,
				chars.length - 8));
		assertTrue(Arrays.equals(chars, source.toCharArray()));
		assertEquals(-1, reader.read(chars, 0, chars.length));
		assertTrue(Arrays.equals(chars, source.toCharArray()));
	}

	public void testReadcharArrayintint2() throws IOException {
		char[] chars = new char[source.length()];
		assertEquals(source.length() - 3, reader.read(chars, 0,
				chars.length - 3));
		assertEquals(3, reader.read(chars, 0, 10));
	}

	public void testReady() throws IOException {
		assertTrue(reader.ready());
		reader.read(new char[source.length()]);
		assertFalse(reader.ready());
	}

	public void testSpecialCharsetReading() throws IOException {
		reader.close();
		in = this.getClass().getClassLoader().getResourceAsStream(
				"tests/api/java/io/testfile-utf8.txt");
		reader = new InputStreamReader(in, "utf-8");
		int c;
		StringBuffer sb = new StringBuffer();
		while ((c = reader.read()) != -1) {
			sb.append((char) c);
		}
		// delete BOM
		assertEquals(source, sb.deleteCharAt(0).toString());

		sb.setLength(0);
		reader.close();
		in = this.getClass().getClassLoader().getResourceAsStream(
				"tests/api/java/io/testfile.txt");
		try {
			reader = new InputStreamReader(in, "gb18030");
		} catch (UnsupportedEncodingException e) {
			System.out
					.println("GB18030 is not supported, abort test InputStreamReaderTest.testSpecialCharsetReading().");
		}
		while ((c = reader.read()) != -1) {
			sb.append((char) c);
		}
		assertEquals(source, sb.toString());
	}

	public void testAfterClose() throws IOException {
		reader.close();
		in = new BufferedInputStream(this.getClass().getClassLoader()
				.getResourceAsStream("tests/api/java/io/testfile-utf8.txt"));
		reader = new InputStreamReader(in, "utf-8");
		in.close();
		try {
			int count = reader.read(new char[1]);
			fail("count:" + count);
		} catch (IOException e) {
		}
		try {
			reader.read();
			fail();
		} catch (IOException e) {
		}

		assertFalse(reader.ready());
		Charset cs = Charset.forName("utf-8");
		assertEquals(cs, Charset.forName(reader.getEncoding()));
		reader.close();
	}

	/*
	 * Class under test for void InputStreamReader(InputStream)
	 */
	public void testInputStreamReaderInputStream() throws IOException {
		try {
			reader = new InputStreamReader(null);
			fail();
		} catch (NullPointerException e) {
		}
		InputStreamReader reader2 = new InputStreamReader(in);
		reader2.close();
	}

	/*
	 * Class under test for void InputStreamReader(InputStream, String)
	 */
	public void testInputStreamReaderInputStreamString() throws IOException {
		try {
			reader = new InputStreamReader(null, "utf-8");
			fail();
		} catch (NullPointerException e) {
		}
		try {
			reader = new InputStreamReader(in, (String) null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			reader = new InputStreamReader(in, "");
			fail();
		} catch (UnsupportedEncodingException e) {
		}
		try {
			reader = new InputStreamReader(in, "badname");
			fail();
		} catch (UnsupportedEncodingException e) {
		}
		InputStreamReader reader2 = new InputStreamReader(in, "utf-8");
		assertEquals(Charset.forName(reader2.getEncoding()), Charset
				.forName("utf-8"));
		reader2.close();
		reader2 = new InputStreamReader(in, "utf8");
		assertEquals(Charset.forName(reader2.getEncoding()), Charset
				.forName("utf-8"));
		reader2.close();
	}

	/*
	 * Class under test for void InputStreamReader(InputStream, CharsetDecoder)
	 */
	public void testInputStreamReaderInputStreamCharsetDecoder()
			throws Exception {
		CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
		try {
			reader = new InputStreamReader(null, decoder);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			reader = new InputStreamReader(in, (CharsetDecoder) null);
			fail();
		} catch (NullPointerException e) {
		}
        InputStreamReader reader2 = new InputStreamReader(in, decoder);
		assertEquals(Charset.forName(reader2.getEncoding()), decoder.charset());
		reader2.close();
	}

	/*
	 * Class under test for void InputStreamReader(InputStream, Charset)
	 */
	public void testInputStreamReaderInputStreamCharset() throws IOException {
		Charset cs = Charset.forName("utf-8");
		try {
			reader = new InputStreamReader(null, cs);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			reader = new InputStreamReader(in, (Charset) null);
			fail();
		} catch (NullPointerException e) {
		}
		InputStreamReader reader2 = new InputStreamReader(in, cs);
		assertEquals(Charset.forName(reader2.getEncoding()), cs);
		reader2.close();
	}

	public void testInputStreamReaderSuccessiveReads() throws IOException {
		byte[] data = new byte[8192 * 2];
		Arrays.fill(data, (byte) 116); // 116 = ISO-8859-1 value for 't'
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		InputStreamReader isr = new InputStreamReader(bis, "ISO-8859-1");

		// One less than the InputStreamReader.BUFFER_SIZE
		char[] buf = new char[8191];
		int bytesRead = isr.read(buf, 0, buf.length);
		if (bytesRead == -1) {
			throw new RuntimeException();
		}
		bytesRead = isr.read(buf, 0, buf.length);
		if (bytesRead == -1) {
			throw new RuntimeException();
		}
	}

	/**
	 * @tests java.io.InputStreamReader#InputStreamReader(java.io.InputStream)
	 */
	public void test_ConstructorLjava_io_InputStream() {
		// Test for method java.io.InputStreamReader(java.io.InputStream)
		assertTrue("Used to test other methods", true);
	}

	/**
	 * @tests java.io.InputStreamReader#InputStreamReader(java.io.InputStream,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_io_InputStreamLjava_lang_String() {
		// Test for method java.io.InputStreamReader(java.io.InputStream,
		// java.lang.String)
		try {
			is = new InputStreamReader(fis, "8859_1");
		} catch (UnsupportedEncodingException e) {
			fail("Unable to create input stream : " + e.getMessage());
		}

		try {
			is = new InputStreamReader(fis, "Bogus");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		fail("Failed to throw Unsupported Encoding exception");
	}

	/**
	 * @tests java.io.InputStreamReader#close()
	 */
	public void test_close() {
		// Test for method void java.io.InputStreamReader.close()
		try {
			is.close();
		} catch (IOException e) {
			fail("Failed to close reader : " + e.getMessage());
		}
		try {
            is.read();
            fail("Should throw IOException");
        } catch (IOException e) {
            // Exception means read failed due to close
        } 

	}

	/**
	 * @tests java.io.InputStreamReader#getEncoding()
	 */
	public void test_getEncoding() {
		// Test for method java.lang.String
		// java.io.InputStreamReader.getEncoding()
		try {
			is = new InputStreamReader(fis, "8859_1");
		} catch (UnsupportedEncodingException e) {
			assertEquals("Returned incorrect encoding", 
					"8859_1", is.getEncoding());
		}
	}

	/**
	 * @tests java.io.InputStreamReader#read()
	 */
	public void test_read() throws IOException{
		// Test for method int java.io.InputStreamReader.read()
		try {
			int c = is.read();
			assertTrue("returned incorrect char", (char) c == fileString
					.charAt(0));
			InputStreamReader reader = new InputStreamReader(
					new ByteArrayInputStream(new byte[] { (byte) 0xe8,
							(byte) 0x9d, (byte) 0xa5 }), "UTF8");
			assertTrue("wrong double byte char", reader.read() == '\u8765');
		} catch (IOException e) {
			fail("Exception during read test : " + e.getMessage());
		}
        
        // Regression for HARMONY-166
        InputStream in;
        InputStreamReader reader;

        in = new LimitedByteArrayInputStream(0);
        reader = new InputStreamReader(in, "UTF-16BE");
        assertEquals("Incorrect byte UTF-16BE", '\u6172', reader.read());

        in = new LimitedByteArrayInputStream(0);
        reader = new InputStreamReader(in, "UTF-16LE");
        assertEquals("Incorrect byte UTF-16BE", '\u7261', reader.read());

        in = new LimitedByteArrayInputStream(1);
        reader = new InputStreamReader(in, "UTF-16");
        assertEquals("Incorrect byte UTF-16BE", '\u7261', reader.read());

        in = new LimitedByteArrayInputStream(2);
        reader = new InputStreamReader(in, "ISO2022JP");
        assertEquals("Incorrect byte ISO2022JP 1", '\u4e5d', reader.read());
        assertEquals("Incorrect byte ISO2022JP 2", '\u7b2c', reader.read());
	}

	/**
     * @tests java.io.InputStreamReader#read(char[], int, int)
     */
	public void test_read$CII() {
		// Test for method int java.io.InputStreamReader.read(char [], int, int)
		try {
			char[] rbuf = new char[100];
			char[] sbuf = new char[100];
			fileString.getChars(0, 100, sbuf, 0);
			is.read(rbuf, 0, 100);
			for (int i = 0; i < rbuf.length; i++)
				assertTrue("returned incorrect chars", rbuf[i] == sbuf[i]);
		} catch (IOException e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.InputStreamReader#ready()
	 */
	public void test_ready() {
		// Test for method boolean java.io.InputStreamReader.ready()
		try {
			assertTrue("Ready test failed", is.ready());
			is.read();
			assertTrue("More chars, but not ready", is.ready());
		} catch (IOException e) {
			fail("Exception during ready test : " + e.getMessage());
		}
	}
    
    
}

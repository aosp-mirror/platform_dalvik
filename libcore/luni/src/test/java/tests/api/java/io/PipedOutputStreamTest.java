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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedOutputStreamTest extends junit.framework.TestCase {

	static class PReader implements Runnable {
		PipedInputStream reader;

		public PipedInputStream getReader() {
			return reader;
		}

		public PReader(PipedOutputStream out) {
			try {
				reader = new PipedInputStream(out);
			} catch (Exception e) {
				System.out.println("Couldn't start reader");
			}
		}

		public int available() {
			try {
				return reader.available();
			} catch (Exception e) {
				return -1;
			}
		}

		public void run() {
			try {
				while (true) {
					Thread.sleep(1000);
					Thread.yield();
				}
			} catch (InterruptedException e) {
			}
		}

		public String read(int nbytes) {
			byte[] buf = new byte[nbytes];
			try {
				reader.read(buf, 0, nbytes);
				return new String(buf);
			} catch (IOException e) {
				System.out.println("Exception reading info");
				return "ERROR";
			}
		}
	}

	Thread rt;

	PReader reader;

	PipedOutputStream out;

	/**
	 * @tests java.io.PipedOutputStream#PipedOutputStream()
	 */
	public void test_Constructor() {
		// Test for method java.io.PipedOutputStream()
		// Used in tests
	}

	/**
	 * @tests java.io.PipedOutputStream#PipedOutputStream(java.io.PipedInputStream)
	 */
	public void test_ConstructorLjava_io_PipedInputStream() {
		// Test for method java.io.PipedOutputStream(java.io.PipedInputStream)

		try {
			out = new PipedOutputStream(new PipedInputStream());
			out.write('b');
		} catch (Exception e) {
			fail("Exception during constructor test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.PipedOutputStream#close()
	 */
	public void test_close() {
		// Test for method void java.io.PipedOutputStream.close()
		try {
			out = new PipedOutputStream();
			rt = new Thread(reader = new PReader(out));
			rt.start();
			out.close();
		} catch (IOException e) {
			fail("Exception during close : " + e.getMessage());
		}
	}
    
    /**
     * @tests java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public void test_connectLjava_io_PipedInputStream_Exception() throws IOException {
        out = new PipedOutputStream();
        out.connect(new PipedInputStream());
        try {
            out.connect(null);
            fail("should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
    }

	/**
	 * @tests java.io.PipedOutputStream#connect(java.io.PipedInputStream)
	 */
	public void test_connectLjava_io_PipedInputStream() {
		// Test for method void
		// java.io.PipedOutputStream.connect(java.io.PipedInputStream)
		try {
			out = new PipedOutputStream();
			rt = new Thread(reader = new PReader(out));
			rt.start();
			out.connect(new PipedInputStream());
		} catch (IOException e) {
			// Correct
			return;
		}
		fail(
				"Failed to throw exception attempting connect on already connected stream");

	}

	/**
	 * @tests java.io.PipedOutputStream#flush()
	 */
	public void test_flush() {
		// Test for method void java.io.PipedOutputStream.flush()
		try {
			out = new PipedOutputStream();
			rt = new Thread(reader = new PReader(out));
			rt.start();
			out.write("HelloWorld".getBytes(), 0, 10);
			assertTrue("Bytes written before flush", reader.available() != 0);
			out.flush();
			assertEquals("Wrote incorrect bytes", 
					"HelloWorld", reader.read(10));
		} catch (IOException e) {
			fail("IOException during write test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.PipedOutputStream#write(byte[], int, int)
	 */
	public void test_write$BII() {
		// Test for method void java.io.PipedOutputStream.write(byte [], int,
		// int)
		try {
			out = new PipedOutputStream();
			rt = new Thread(reader = new PReader(out));
			rt.start();
			out.write("HelloWorld".getBytes(), 0, 10);
			out.flush();
			assertEquals("Wrote incorrect bytes", 
					"HelloWorld", reader.read(10));
		} catch (IOException e) {
			fail("IOException during write test : " + e.getMessage());
		}
	}

    /**
     * @tests java.io.PipedOutputStream#write(byte[], int, int)
     * Regression for HARMONY-387
     */
    public void test_write$BII_2() throws IOException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = null;
        try{
            pos = new PipedOutputStream(pis);
            pos.write(new byte[0], -1, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals(
                    "IndexOutOfBoundsException rather than a subclass expected",
                    IndexOutOfBoundsException.class, t.getClass());
        }
    }

	/**
	 * @tests java.io.PipedOutputStream#write(int)
	 */
	public void test_writeI() {
		// Test for method void java.io.PipedOutputStream.write(int)
		try {
			out = new PipedOutputStream();
			rt = new Thread(reader = new PReader(out));
			rt.start();
			out.write('c');
			out.flush();
			assertEquals("Wrote incorrect byte", "c", reader.read(1));
		} catch (IOException e) {
			fail("IOException during write test : " + e.getMessage());
		}
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
		if (rt != null)
			rt.interrupt();
	}
}

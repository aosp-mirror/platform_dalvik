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
import java.io.EOFException;
import java.io.IOException;

public class DataInputStreamTest extends junit.framework.TestCase {

	private DataOutputStream os;

	private DataInputStream dis;

	private ByteArrayOutputStream bos;

	String unihw = "\u0048\u0065\u006C\u006C\u006F\u0020\u0057\u006F\u0072\u006C\u0064";

	public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_DataInputStream\n";

	/**
	 * @tests java.io.DataInputStream#DataInputStream(java.io.InputStream)
	 */
	public void test_ConstructorLjava_io_InputStream() {
		// Test for method java.io.DataInputStream(java.io.InputStream)
		try {
			os.writeChar('t');
			os.close();
			openDataInputStream();
		} catch (IOException e) {
			fail("IOException during constructor test : " + e.getMessage());
		} finally {
			try {
				dis.close();
			} catch (IOException e) {
				fail("IOException during constructor test : " + e.getMessage());
			}
		}
	}

	/**
	 * @tests java.io.DataInputStream#read(byte[])
	 */
	public void test_read$B() {
		// Test for method int java.io.DataInputStream.read(byte [])
		try {
			os.write(fileString.getBytes());
			os.close();
			openDataInputStream();
			byte rbytes[] = new byte[fileString.length()];
			dis.read(rbytes);
			assertTrue("Incorrect data read", new String(rbytes, 0, fileString
					.length()).equals(fileString));
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() {
		// Test for method int java.io.DataInputStream.read(byte [], int, int)
		try {
			os.write(fileString.getBytes());
			os.close();
			openDataInputStream();
			byte rbytes[] = new byte[fileString.length()];
			dis.read(rbytes, 0, rbytes.length);
			assertTrue("Incorrect data read", new String(rbytes, 0, fileString
					.length()).equals(fileString));
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readBoolean()
	 */
	public void test_readBoolean() {
		// Test for method boolean java.io.DataInputStream.readBoolean()
		try {
			os.writeBoolean(true);
			os.close();
			openDataInputStream();
			assertTrue("Incorrect boolean written", dis.readBoolean());
		} catch (IOException e) {
			fail("readBoolean test failed : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readByte()
	 */
	public void test_readByte() {
		// Test for method byte java.io.DataInputStream.readByte()
		try {
			os.writeByte((byte) 127);
			os.close();
			openDataInputStream();
			assertTrue("Incorrect byte read", dis.readByte() == (byte) 127);
		} catch (IOException e) {
			fail("IOException during readByte test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readChar()
	 */
	public void test_readChar() {
		// Test for method char java.io.DataInputStream.readChar()
		try {
			os.writeChar('t');
			os.close();
			openDataInputStream();
			assertEquals("Incorrect char read", 't', dis.readChar());
		} catch (IOException e) {
			fail("IOException during readChar test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readDouble()
	 */
	public void test_readDouble() {
		// Test for method double java.io.DataInputStream.readDouble()
		try {
			os.writeDouble(2345.76834720202);
			os.close();
			openDataInputStream();
			assertEquals("Incorrect double read",
					2345.76834720202, dis.readDouble());
		} catch (IOException e) {
			fail("IOException during readDouble test" + e.toString());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readFloat()
	 */
	public void test_readFloat() {
		// Test for method float java.io.DataInputStream.readFloat()
		try {
			os.writeFloat(29.08764f);
			os.close();
			openDataInputStream();
			assertTrue("Incorrect float read", dis.readFloat() == 29.08764f);
		} catch (IOException e) {
			fail("readFloat test failed : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readFully(byte[])
	 */
	public void test_readFully$B() {
		// Test for method void java.io.DataInputStream.readFully(byte [])
		try {
			os.write(fileString.getBytes());
			os.close();
			openDataInputStream();
			byte rbytes[] = new byte[fileString.length()];
			dis.readFully(rbytes);
			assertTrue("Incorrect data read", new String(rbytes, 0, fileString
					.length()).equals(fileString));
		} catch (IOException e) {
			fail("IOException during readFully test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readFully(byte[], int, int)
	 */
	public void test_readFully$BII() {
		// Test for method void java.io.DataInputStream.readFully(byte [], int,
		// int)
		try {
			os.write(fileString.getBytes());
			os.close();
			openDataInputStream();
			byte rbytes[] = new byte[fileString.length()];
			dis.readFully(rbytes, 0, fileString.length());
			assertTrue("Incorrect data read", new String(rbytes, 0, fileString
					.length()).equals(fileString));
		} catch (IOException e) {
			fail("IOException during readFully test : " + e.getMessage());
		}
	}
    
    /**
     * @tests java.io.DataInputStream#readFully(byte[], int, int)
     */
    public void test_readFully$BII_Exception() throws IOException {
        DataInputStream is =  new DataInputStream(new ByteArrayInputStream(new byte[fileString.length()]));

        byte[] byteArray = new byte[fileString.length()]; 
        
        try {
            is.readFully(byteArray, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(byteArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(byteArray, 1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        is.readFully(byteArray, -1, 0);
        is.readFully(byteArray, 0, 0);
        is.readFully(byteArray, 1, 0);
        
        try {
            is.readFully(byteArray, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        is.readFully(byteArray, 0, 1);
        is.readFully(byteArray, 1, 1);
        try {
            is.readFully(byteArray, 0, Integer.MAX_VALUE);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
    
    /**
     * @tests java.io.DataInputStream#readFully(byte[], int, int)
     */
    public void test_readFully$BII_NullArray() throws IOException {
        DataInputStream is =  new DataInputStream(new ByteArrayInputStream(new byte[fileString.length()]));
        
        byte[] nullByteArray = null;
       
        try {
            is.readFully(nullByteArray, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        is.readFully(nullByteArray, -1, 0);
        is.readFully(nullByteArray, 0, 0);
        is.readFully(nullByteArray, 1, 0);
        
        try {
            is.readFully(nullByteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            is.readFully(nullByteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 0, Integer.MAX_VALUE);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    /**
     * @tests java.io.DataInputStream#readFully(byte[], int, int)
     */
    public void test_readFully$BII_NullStream() throws IOException {
        DataInputStream is = new DataInputStream(null);
        byte[] byteArray = new byte[fileString.length()]; 
           
        try {
            is.readFully(byteArray, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(byteArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(byteArray, 1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        is.readFully(byteArray, -1, 0);
        is.readFully(byteArray, 0, 0);
        is.readFully(byteArray, 1, 0);
        
        try {
            is.readFully(byteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            is.readFully(byteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            is.readFully(byteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            is.readFully(byteArray, 0, Integer.MAX_VALUE);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    /**
     * @tests java.io.DataInputStream#readFully(byte[], int, int)
     */
    public void test_readFully$BII_NullStream_NullArray() throws IOException {
        DataInputStream is = new DataInputStream(null);
        byte[] nullByteArray = null;
        
        try {
            is.readFully(nullByteArray, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        is.readFully(nullByteArray, -1, 0);
        is.readFully(nullByteArray, 0, 0);
        is.readFully(nullByteArray, 1, 0);
        
        try {
            is.readFully(nullByteArray, -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            is.readFully(nullByteArray, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            is.readFully(nullByteArray, 0, Integer.MAX_VALUE);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
    }

	/**
	 * @tests java.io.DataInputStream#readInt()
	 */
	public void test_readInt() {
		// Test for method int java.io.DataInputStream.readInt()
		try {
			os.writeInt(768347202);
			os.close();
			openDataInputStream();
			assertEquals("Incorrect int read", 768347202, dis.readInt());
		} catch (IOException e) {
			fail("IOException during readInt test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readLine()
	 */
	public void test_readLine() {
		// Test for method java.lang.String java.io.DataInputStream.readLine()
		try {
			os.writeBytes("Hello");
			os.close();
			openDataInputStream();
			String line = dis.readLine();
			assertTrue("Incorrect line read: " + line, line.equals("Hello"));
		} catch (IOException e) {
			fail("IOException during readLine test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readLong()
	 */
	public void test_readLong() {
		// Test for method long java.io.DataInputStream.readLong()
		try {
			os.writeLong(9875645283333L);
			os.close();
			openDataInputStream();
			assertEquals("Incorrect long read", 9875645283333L, dis.readLong());
		} catch (IOException e) {
			fail("read long test failed : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readShort()
	 */
	public void test_readShort() {
		// Test for method short java.io.DataInputStream.readShort()
		try {
			os.writeShort(9875);
			os.close();
			openDataInputStream();
			assertTrue("Incorrect short read", dis.readShort() == (short) 9875);
		} catch (IOException e) {
			fail("Exception during read short test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readUnsignedByte()
	 */
	public void test_readUnsignedByte() {
		// Test for method int java.io.DataInputStream.readUnsignedByte()
		try {
			os.writeByte((byte) -127);
			os.close();
			openDataInputStream();
			assertEquals("Incorrect byte read", 129, dis.readUnsignedByte());
		} catch (IOException e) {
			fail("IOException during readUnsignedByte test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readUnsignedShort()
	 */
	public void test_readUnsignedShort() {
		// Test for method int java.io.DataInputStream.readUnsignedShort()
		try {
			os.writeShort(9875);
			os.close();
			openDataInputStream();
			assertEquals("Incorrect short read", 9875, dis.readUnsignedShort());
		} catch (IOException e) {
			fail("Exception during readShort test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readUTF()
	 */
	public void test_readUTF() {
		// Test for method java.lang.String java.io.DataInputStream.readUTF()
		try {
			os.writeUTF(unihw);
			os.close();
			openDataInputStream();
			assertTrue("Failed to write string in UTF format",
					dis.available() == unihw.length() + 2);
			assertTrue("Incorrect string read", dis.readUTF().equals(unihw));
		} catch (Exception e) {
			fail("Exception during readUTF : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#readUTF(java.io.DataInput)
	 */
	public void test_readUTFLjava_io_DataInput() {
		// Test for method java.lang.String
		// java.io.DataInputStream.readUTF(java.io.DataInput)
		try {
			os.writeUTF(unihw);
			os.close();
			openDataInputStream();
			assertTrue("Failed to write string in UTF format",
					dis.available() == unihw.length() + 2);
			assertTrue("Incorrect string read", DataInputStream.readUTF(dis)
					.equals(unihw));
		} catch (Exception e) {
			fail("Exception during readUTF : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.DataInputStream#skipBytes(int)
	 */
	public void test_skipBytesI() {
		// Test for method int java.io.DataInputStream.skipBytes(int)
		try {
			byte fileBytes[] = fileString.getBytes();
			os.write(fileBytes);
			os.close();
			openDataInputStream();
			dis.skipBytes(100);
			byte rbytes[] = new byte[fileString.length()];
			dis.read(rbytes, 0, 50);
			dis.close();
			assertTrue("Incorrect data read", new String(rbytes, 0, 50)
					.equals(fileString.substring(100, 150)));
		} catch (IOException e) {
			fail("IOException during skipBytes test 1 : " + e.getMessage());
		}
		try {
			// boolean eofException = false; //what is this var for?
			int skipped = 0;
			openDataInputStream();
			try {
				skipped = dis.skipBytes(50000);
			} catch (EOFException e) {
				// eofException = true;
			}
			;
			assertTrue("Skipped should report " + fileString.length() + " not "
					+ skipped, skipped == fileString.length());
		} catch (IOException e) {
			fail("IOException during skipBytes test 2 : " + e.getMessage());
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
			os.close();
		} catch (Exception e) {
		}
        try {
            dis.close();
        } catch (Exception e) {
        }
	}
}

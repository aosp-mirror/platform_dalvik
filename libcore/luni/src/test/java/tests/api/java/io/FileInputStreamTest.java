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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.security.Permission;

import tests.support.Support_PlatformFile;

public class FileInputStreamTest extends junit.framework.TestCase {

	public String fileName;

	private java.io.InputStream is;

	byte[] ibuf = new byte[4096];

	public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

	/**
	 * @tests java.io.FileInputStream#FileInputStream(java.io.File)
	 */
	public void test_ConstructorLjava_io_File() {
		// Test for method java.io.FileInputStream(java.io.File)
		try {
			java.io.File f = new java.io.File(fileName);
			is = new java.io.FileInputStream(f);
			is.close();
		} catch (Exception e) {
			fail("Failed to create FileInputStream : " + e.getMessage());
		}

	}

	/**
	 * @tests java.io.FileInputStream#FileInputStream(java.io.FileDescriptor)
	 */
	public void test_ConstructorLjava_io_FileDescriptor() {
		// Test for method java.io.FileInputStream(java.io.FileDescriptor)
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			FileInputStream fis = new FileInputStream(fos.getFD());
			fos.close();
			fis.close();
		} catch (Exception e) {
			fail("Exception during constrcutor test: " + e.toString());
		}
	}

	/**
	 * @tests java.io.FileInputStream#FileInputStream(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.io.FileInputStream(java.lang.String)
		try {
			is = new java.io.FileInputStream(fileName);
			is.close();
		} catch (Exception e) {
			fail("Failed to create FileInputStream : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FileInputStream#available()
	 */
	public void test_available() {
		// Test for method int java.io.FileInputStream.available()
		try {
			is = new java.io.FileInputStream(fileName);
			assertTrue("Returned incorrect number of available bytes", is
					.available() == fileString.length());
		} catch (Exception e) {
			fail("Exception during available test : " + e.getMessage());
		} finally {
			try {
				is.close();
			} catch (java.io.IOException e) {
			}
		}
	}

	/**
	 * @tests java.io.FileInputStream#close()
	 */
	public void test_close() {
		// Test for method void java.io.FileInputStream.close()

		try {
			is = new java.io.FileInputStream(fileName);
			is.close();
		} catch (java.io.IOException e) {
			fail("Exception attempting to close stream : " + e.getMessage());
		}
		;

		try {
			is.read();
		} catch (java.io.IOException e) {
			return;
		}
		fail("Able to read from closed stream");
	}

	/**
	 * @tests java.io.FileInputStream#getFD()
	 */
	public void test_getFD() {
		// Test for method java.io.FileDescriptor
		// java.io.FileInputStream.getFD()
		try {

			FileInputStream fis = new FileInputStream(fileName);
			assertTrue("Returned invalid fd", fis.getFD().valid());
			fis.close();
			assertTrue("Returned invalid fd", !fis.getFD().valid());
		} catch (FileNotFoundException e) {
			fail("Could not find : " + fileName);
		}

		catch (IOException e) {
			fail("Exception during test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FileInputStream#read()
	 */
	public void test_read() {
		// Test for method int java.io.FileInputStream.read()
		try {
			is = new java.io.FileInputStream(fileName);
			int c = is.read();
			is.close();
			assertTrue("read returned incorrect char", c == fileString
					.charAt(0));
		} catch (Exception e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FileInputStream#read(byte[])
	 */
	public void test_read$B() {
		// Test for method int java.io.FileInputStream.read(byte [])
		byte[] buf1 = new byte[100];
		try {
			is = new java.io.FileInputStream(fileName);
			is.skip(3000);
			is.read(buf1);
			is.close();
			assertTrue("Failed to read correct data", new String(buf1, 0,
					buf1.length).equals(fileString.substring(3000, 3100)));

		} catch (Exception e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.FileInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() {
		// Test for method int java.io.FileInputStream.read(byte [], int, int)
		byte[] buf1 = new byte[100];
		try {
			is = new java.io.FileInputStream(fileName);
			is.skip(3000);
			is.read(buf1, 0, buf1.length);
			is.close();
			assertTrue("Failed to read correct data", new String(buf1, 0,
					buf1.length).equals(fileString.substring(3000, 3100)));

		} catch (Exception e) {
			fail("Exception during read test : " + e.getMessage());
		}
	}
    
    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read_$BII_IOException() throws IOException {
        byte[] buf = new byte[1000];
        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }

        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }

        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }

        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, 0, 1001);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }

        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, 1001, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }

        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, 500, 501);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }
        
        try {
            is = new java.io.FileInputStream(fileName);
            is.close();
            is.read(buf, 0, 100);
            fail("should throw IOException");
        } catch (IOException e) {
            // Expected
        } finally {
            is.close();
        }
        
        try {
            is = new java.io.FileInputStream(fileName);
            is.close();
            is.read(buf, 0, 0);
        } finally {
            is.close();
        }
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read_$BII_NullPointerException() throws IOException {
        byte[] buf = null;
        try {
            is = new java.io.FileInputStream(fileName);
            is.read(buf, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } finally {
            is.close();
        }
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read_$BII_IndexOutOfBoundsException() throws IOException {
        byte[] buf = new byte[1000];
        try {
            is = new java.io.FileInputStream(fileName);
            is.close();
            is.read(buf, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } finally {
            is.close();
        }
    }

	/**
	 * @tests java.io.FileInputStream#skip(long)
	 */
	public void test_skipJ() {
		// Test for method long java.io.FileInputStream.skip(long)
		byte[] buf1 = new byte[10];
		try {
			is = new java.io.FileInputStream(fileName);
			is.skip(1000);
			is.read(buf1, 0, buf1.length);
			is.close();
			assertTrue("Failed to skip to correct position", new String(buf1,
					0, buf1.length).equals(fileString.substring(1000, 1010)));
		} catch (Exception e) {
			fail("Exception during skip test " + e.getMessage());
		}
	}

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int))
     */
    public void test_regressionNNN() throws IOException {
        // Regression for HARMONY-434
        FileInputStream fis = new java.io.FileInputStream(fileName);

        try {
            fis.read(new byte[1], -1, 1);
            fail("IndexOutOfBoundsException must be thrown if off <0");
        } catch (IndexOutOfBoundsException e) {}

        try {
            fis.read(new byte[1], 0, -1);
            fail("IndexOutOfBoundsException must be thrown if len <0");
        } catch (IndexOutOfBoundsException e) {}

        try {
            fis.read(new byte[1], 0, 5);
            fail("IndexOutOfBoundsException must be thrown if off+len > b.lengh");
        } catch (IndexOutOfBoundsException e) {}

        try {
            fis.read(new byte[10], Integer.MAX_VALUE, 5);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}

        try {
            fis.read(new byte[10], 5, Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
        fis.close();
    }
    
    /**
     * @tests java.io.FileInputStream#FileInputStream(String)
     */
    public void test_Constructor_LString_WithSecurityManager() throws IOException {
        SecurityManager old = System.getSecurityManager();
        try {
            MockSecurityManager msm = new MockSecurityManager();
            System.setSecurityManager(msm);
            new FileInputStream((String)null);
            fail("should throw SecurityException");
        } catch (SecurityException e) {
            //expected
        } finally {
            System.setSecurityManager(old);
        }
    }
    
    /**
     * @tests java.io.FileInputStream#skip(long)
     */
    public void test_skipNegativeArgumentJ() throws IOException{
        
        FileInputStream fis = new java.io.FileInputStream(fileName);

        try {
            fis.skip(-5);
            fail("IOException must be thrown if number of bytes to skip <0");
        } catch (IOException e) {
            // Expected IOException
        } catch (Exception e) {
            fail("IOException expected, but found: " + e.getMessage());
        }
        
        fis.close();
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
		new File(fileName).delete();

	}
}

class MockSecurityManager extends SecurityManager {  
    public void checkPermission(Permission permission) {
        if (permission instanceof FilePermission) {
           if (permission.getActions().indexOf("read") == 0)
               throw new SecurityException();
        }
    }
    
    public void checkRead(String file) {
       if( null == file) {
            file = "";
        }
       checkPermission(new FilePermission(file,"read"));
    }  
}

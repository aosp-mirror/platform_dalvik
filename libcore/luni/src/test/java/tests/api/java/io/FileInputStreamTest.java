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
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import tests.support.Support_PlatformFile;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(
        value = FileInputStream.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "finalize",
                    args = {},
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "Hard to test since it requires that the " +
                            "garbage collector runs; add test later."
            )
        }
) 
public class FileInputStreamTest extends junit.framework.TestCase {

    public String fileName;

    private FileInputStream is;

    byte[] ibuf = new byte[4096];

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    /**
     * @tests FileInputStream#FileInputStream(File)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileInputStream",
        args = {File.class}
    )     
    public void test_ConstructorLjava_io_File() {
        // Test for method FileInputStream(File)
        try {
            File f = new File(fileName);
            is = new FileInputStream(f);
            is.close();
        } catch (Exception e) {
            fail("Failed to create FileInputStream : " + e.getMessage());
        }
        File f2 = new File("ImprobableFile.42");
        try {
            is = new FileInputStream(f2);
            is.close();
            f2.delete();
            fail("FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * @tests FileInputStream#FileInputStream(FileDescriptor)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileInputStream",
        args = {FileDescriptor.class}
    )         
    public void test_ConstructorLjava_io_FileDescriptor() {
        // Test for method FileInputStream(FileDescriptor)
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            FileInputStream fis = new FileInputStream(fos.getFD());
            fos.close();
            fis.close();
        } catch (Exception e) {
            fail("Exception during constrcutor test: " + e.toString());
        }
        try {
            FileInputStream fis = new FileInputStream((FileDescriptor) null);
            fis.close();
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.   
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * @tests FileInputStream#FileInputStream(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileInputStream",
        args = {java.lang.String.class}
    )    
    public void test_ConstructorLjava_lang_String() {
        // Test for method FileInputStream(java.lang.String)
        try {
            is = new FileInputStream(fileName);
            is.close();
        } catch (Exception e) {
            fail("Failed to create FileInputStream : " + e.getMessage());
        }
        try {
            is = new FileInputStream("ImprobableFile.42");
            is.close();
            new File("ImprobableFile.42").delete();
            fail("FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * @tests FileInputStream#available()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "available",
        args = {}
    )         
    public void test_available() throws IOException {
        is = new FileInputStream(fileName);
        assertEquals("Test 1: Returned incorrect number of available bytes;", 
                fileString.length(), is.available());
        is.close();
        try {
            is.available();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests FileInputStream#close()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No IOException test since this is only thrown" +
                "by a native method.",
        method = "close",
        args = {}
    )         
    public void test_close() throws IOException {
        is = new FileInputStream(fileName);
        is.close();

        try {
            is.read();
            fail("Test 1: Read from closed stream succeeded.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests FileInputStream#getChannel()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getChannel",
        args = {}
    )     
    public void test_getChannel() {
        // Test for method FileChannel FileInputStream.getChannel()
        FileChannel channel;
        byte[] buffer = new byte[100];
        byte[] stringBytes;
        final int offset = 5;
        boolean equal = true;
        
        try {
            FileInputStream fis = new FileInputStream(fileName);
            channel = fis.getChannel();
            assertNotNull(channel);
            assertTrue("Channel is closed.", channel.isOpen());
            
            // Check that the channel is associated with the input stream.
            channel.position(offset);
            fis.read(buffer, 0, 10);
            stringBytes = fileString.getBytes();
            for (int i = 0; i < 10; i++) {
                equal &= (buffer[i] == stringBytes[i + offset]);
            }
            assertTrue("Channel is not associated with this stream.", equal);
            
            fis.close();
            assertFalse("Channel has not been closed.", channel.isOpen());
        } catch (FileNotFoundException e) {
            fail("Could not find : " + fileName);
        }

        catch (IOException e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    
    /**
     * @tests FileInputStream#getFD()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No IOException check since it is never thrown.",
        method = "getFD",
        args = {}
    )     
    public void test_getFD() {
        // Test for method FileDescriptor
        // FileInputStream.getFD()
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
     * @tests FileInputStream#read()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "read",
        args = {}
    )     
    public void test_read() throws IOException {
        is = new FileInputStream(fileName);
        int c = is.read();
        assertEquals("Test 1: Read returned incorrect char;", 
                fileString.charAt(0), c);
        
        is.close();
        try {
            is.read();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests FileInputStream#read(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "read",
        args = {byte[].class}
    )        
    public void test_read$B() throws IOException {
        byte[] buf1 = new byte[100];
        is = new FileInputStream(fileName);
        is.skip(3000);
        is.read(buf1);
        is.close();
        assertTrue("Test 1: Failed to read correct data.", 
                new String(buf1, 0, buf1.length).equals(
                        fileString.substring(3000, 3100)));

        is.close();
        try {
            is.read(buf1);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests FileInputStream#read(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )      
    public void test_read$BII() {
        // Test for method int FileInputStream.read(byte [], int, int)
        byte[] buf1 = new byte[100];
        try {
            is = new FileInputStream(fileName);
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
     * @tests FileInputStream#read(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Illegal argument checks.",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )      
    public void test_read$BII_Exception() throws IOException {
        byte[] buf = null;
        try {
            is = new FileInputStream(fileName);
            is.read(buf, 0, 0);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        } finally {
            is.close();
        }

        buf = new byte[1000];        
        try {
            is = new FileInputStream(fileName);
            is.read(buf, -1, 0);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        } finally {
            is.close();
        }

        try {
            is = new FileInputStream(fileName);
            is.read(buf, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        } finally {
            is.close();
        }

        try {
            is = new FileInputStream(fileName);
            is.read(buf, -1, -1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        } finally {
            is.close();
        }

        try {
            is = new FileInputStream(fileName);
            is.read(buf, 0, 1001);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        } finally {
            is.close();
        }

        try {
            is = new FileInputStream(fileName);
            is.read(buf, 1001, 0);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        } finally {
            is.close();
        }

        try {
            is = new FileInputStream(fileName);
            is.read(buf, 500, 501);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        } finally {
            is.close();
        }
        
        try {
            is = new FileInputStream(fileName);
            is.close();
            is.read(buf, 0, 100);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        } finally {
            is.close();
        }
    }

    /**
     * @tests FileInputStream#skip(long)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "skip",
        args = {long.class}
    )       
    public void test_skipJ() throws IOException {
        byte[] buf1 = new byte[10];
        is = new FileInputStream(fileName);
        is.skip(1000);
        is.read(buf1, 0, buf1.length);
        assertTrue("Test 1: Failed to skip to correct position.", 
                new String(buf1, 0, buf1.length).equals(
                        fileString.substring(1000, 1010)));

        is.close();
        try {
            is.read();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests FileInputStream#skip(long)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that skip(long) method throws IOException if " +
                "this method is called with negative argument.",
        method = "skip",
        args = {long.class}
    )     
    public void test_skipNegativeArgumentJ() throws IOException{
        
        FileInputStream fis = new FileInputStream(fileName);

        try {
            fis.skip(-5);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected IOException
        } catch (Exception e) {
            fail("Test 2: IOException expected but found: " + e.getMessage());
        }
        
        fis.close();
    } 
    
    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        try {
            fileName = System.getProperty("java.io.tmpdir");
            String separator = System.getProperty("file.separator");
            if (fileName.charAt(fileName.length() - 1) == separator.charAt(0))
                fileName = Support_PlatformFile.getNewPlatformFile(fileName,
                        "input.tst");
            else
                fileName = Support_PlatformFile.getNewPlatformFile(fileName
                        + separator, "input.tst");
            java.io.OutputStream fos = new FileOutputStream(fileName);
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
        try {
            if (is != null) {
                is.close();
            }
            new File(fileName).delete();
        } catch (IOException e) {
            // Ignored.
        }
    }
}

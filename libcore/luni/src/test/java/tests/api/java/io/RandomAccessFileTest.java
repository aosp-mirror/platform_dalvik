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

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;

@TestTargetClass(RandomAccessFile.class) 
public class RandomAccessFileTest extends junit.framework.TestCase {

    public String fileName;

    public boolean ufile = true;

    java.io.RandomAccessFile raf;

    java.io.File f;

    String unihw = "\u0048\u0065\u006C\u0801\u006C\u006F\u0020\u0057\u0081\u006F\u0072\u006C\u0064";

    static final String testString = "Lorem ipsum dolor sit amet,\n" +
    "consectetur adipisicing elit,\nsed do eiusmod tempor incididunt ut" +
    "labore et dolore magna aliqua.\n";
    static final int testLength = testString.length();

    /**
     * @tests java.io.RandomAccessFile#RandomAccessFile(java.io.File,
     *        java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "RandomAccessFile",
        args = {java.io.File.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_io_FileLjava_lang_String() throws Exception {
        RandomAccessFile raf = null;
        File tmpFile = new File(fileName);

        try {
            raf = new java.io.RandomAccessFile(tmpFile, "r");
            fail("Test 1: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        } catch (IllegalArgumentException e) {
            fail("Test 2: Unexpected IllegalArgumentException: " + e.getMessage());
        }

        tmpFile.createNewFile();
        
        try {
            // Checking the remaining valid mode parameters.
            try {
                raf = new java.io.RandomAccessFile(tmpFile, "rwd");
            } catch (IllegalArgumentException e) {
                fail("Test 3: Unexpected IllegalArgumentException: " + e.getMessage());
            }
            raf.close();
            try {
                raf = new java.io.RandomAccessFile(tmpFile, "rws");
            } catch (IllegalArgumentException e) {
                fail("Test 4: Unexpected IllegalArgumentException: " + e.getMessage());
            }
            raf.close();
            try {
                raf = new java.io.RandomAccessFile(tmpFile, "rw");
            } catch (IllegalArgumentException e) {
                fail("Test 5: Unexpected IllegalArgumentException: " + e.getMessage());
            }
            raf.close();
            
            // Checking an invalid mode parameter.
            try {
                raf = new java.io.RandomAccessFile(tmpFile, "i");
                fail("Test 6: IllegalArgumentException expected.");
            } catch (IllegalArgumentException e) {
                // Expected.
            }
        } finally {
            if (raf != null ) raf.close();
            tmpFile.delete();
        }
    }

    /**
     * @tests java.io.RandomAccessFile#RandomAccessFile(java.lang.String,
     *        java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "RandomAccessFile",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String()
            throws IOException {
        RandomAccessFile raf = null;
        File tmpFile = new File(fileName);

        try {
            raf = new java.io.RandomAccessFile(fileName, "r");
            fail("Test 1: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        } catch (IllegalArgumentException e) {
            fail("Test 2: Unexpected IllegalArgumentException: " + e.getMessage());
        }

        try {
            // Checking the remaining valid mode parameters.
            try {
                raf = new java.io.RandomAccessFile(fileName, "rwd");
            } catch (IllegalArgumentException e) {
                fail("Test 3: Unexpected IllegalArgumentException: " + e.getMessage());
            }
            raf.close();
            try {
                raf = new java.io.RandomAccessFile(fileName, "rws");
            } catch (IllegalArgumentException e) {
                fail("Test 4: Unexpected IllegalArgumentException: " + e.getMessage());
            }
            raf.close();
            try {
                raf = new java.io.RandomAccessFile(fileName, "rw");
            } catch (IllegalArgumentException e) {
                fail("Test 5: Unexpected IllegalArgumentException: " + e.getMessage());
            }
            raf.close();
            
            // Checking an invalid mode parameter.
            try {
                raf = new java.io.RandomAccessFile(fileName, "i");
                fail("Test 6: IllegalArgumentException expected.");
            } catch (IllegalArgumentException e) {
                // Expected.
            }
            
            // Checking for NoWritableChannelException.
            raf = new java.io.RandomAccessFile(fileName, "r");
            FileChannel fcr = raf.getChannel();

            try {
                fcr.lock(0L, Long.MAX_VALUE, false);
                fail("Test 7: NonWritableChannelException expected.");
            } catch (NonWritableChannelException e) {
                // Expected.
            }
            
        } finally {
            if (raf != null ) raf.close();
            if (tmpFile.exists()) tmpFile.delete();
        }
    }

    /**
     * @tests java.io.RandomAccessFile#close()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No IOException checking, requires native code that can be forced to throw such an exception.",
        method = "close",
        args = {}
    )
    public void test_close() {
        // Test for method void java.io.RandomAccessFile.close()
        try {
            RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
            raf.close();
            raf.write("Test".getBytes(), 0, 4);
            fail("Failed to close file properly.");
        } catch (IOException e) {}
    }

    /**
     * @tests java.io.RandomAccessFile#getChannel()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getChannel",
        args = {}
    )
    public void test_getChannel() throws IOException {

        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        FileChannel fc = raf.getChannel();
        
        // Indirect test: If the file's file pointer moves then the position
        // in the channel has to move accordingly.
        assertTrue("Test 1: Channel position expected to be 0.", fc.position() == 0);
        
        raf.write(testString.getBytes());
        assertEquals("Test 2: Unexpected channel position.", 
                testLength, fc.position());
        assertTrue("Test 3: Channel position is not equal to file pointer.", 
                fc.position() == raf.getFilePointer());
        raf.close();
    }
    
    /**
     * @tests java.io.RandomAccessFile#getFD()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No IOException checking since this exception is not thrown.",
        method = "getFD",
        args = {}
    )
    public void test_getFD() throws IOException {
        // Test for method java.io.FileDescriptor
        // java.io.RandomAccessFile.getFD()

        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        assertTrue("Test 1: Returned invalid fd.", raf.getFD().valid());

        raf.close();
        assertFalse("Test 2: Returned valid fd after close", raf.getFD().valid());
    }

    /**
     * @tests java.io.RandomAccessFile#getFilePointer()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFilePointer",
        args = {}
    )
    public void test_getFilePointer() throws IOException {
        // Test for method long java.io.RandomAccessFile.getFilePointer()
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.write(testString.getBytes(), 0, testLength);
        assertEquals("Test 1: Incorrect filePointer returned. ", testLength, raf
                .getFilePointer());
        raf.close();
        try {
            raf.getFilePointer();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#length()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "length",
        args = {}
    )
    public void test_length() throws IOException {
        // Test for method long java.io.RandomAccessFile.length()
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.write(testString.getBytes());
        assertEquals("Test 1: Incorrect length returned. ", testLength, 
                raf.length());
        raf.close();
        try {
            raf.length();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#read()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "write",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "read",
            args = {}
        )
    })
    public void test_read_write() throws IOException {
        int i;
        byte[] testBuf = testString.getBytes();
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        for (i = 0; i < testString.length(); i++) {
            try {
                raf.write(testBuf[i]);
            } catch (Exception e) {
                fail("Test 1: Unexpected exception while writing: " 
                        + e.getMessage());
            }
        }
        
        raf.seek(0);

        for (i = 0; i < testString.length(); i++) {
            assertEquals(String.format("Test 2: Incorrect value written or read at index %d; ", i), 
                    testBuf[i], raf.read());
        }
        
        assertTrue("Test 3: End of file indicator (-1) expected.", raf.read() == -1);
        
        raf.close();
        try {
            raf.write(42);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.read();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#read(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class}
    )
    public void test_read$B() throws IOException {
        FileOutputStream fos = new java.io.FileOutputStream(fileName);
        fos.write(testString.getBytes(), 0, testLength);
        fos.close();

        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "r");
        byte[] rbuf = new byte[testLength + 10];
        
        int bytesRead = raf.read(rbuf);
        assertEquals("Test 1: Incorrect number of bytes read. ", 
                testLength, bytesRead);
        assertEquals("Test 2: Incorrect bytes read. ", testString,
                new String(rbuf, 0, testLength));
        
        bytesRead = raf.read(rbuf);
        assertTrue("Test 3: EOF (-1) expected. ", bytesRead == -1);

        raf.close();
        try {
            bytesRead = raf.read(rbuf);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#read(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII() throws IOException {
        int bytesRead;
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        byte[] rbuf = new byte[4000];
        
        FileOutputStream fos = new java.io.FileOutputStream(fileName);
        fos.write(testString.getBytes(), 0, testLength);
        fos.close();
        
        // Read half of the file contents.
        bytesRead = raf.read(rbuf, 10, testLength / 2);
        assertEquals("Test 1: Incorrect number of bytes read. ", 
                testLength / 2, bytesRead);
        assertEquals("Test 2: Incorrect bytes read. ", 
                testString.substring(0, testLength / 2),
                new String(rbuf, 10, testLength / 2));
        
        // Read the rest of the file contents.
        bytesRead = raf.read(rbuf, 0, testLength);
        assertEquals("Test 3: Incorrect number of bytes read. ", 
                testLength - (testLength / 2), bytesRead);
        assertEquals("Test 4: Incorrect bytes read. ", 
                testString.substring(testLength / 2, (testLength / 2) + bytesRead),
                new String(rbuf, 0, bytesRead));
        
        // Try to read even more.
        bytesRead = raf.read(rbuf, 0, 1);
        assertTrue("Test 5: EOF (-1) expected. ", bytesRead == -1);

        // Illegal parameter value tests.
        try {
            raf.read(rbuf, -1, 1);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            raf.read(rbuf, 0, -1);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            raf.read(rbuf, 2000, 2001);
            fail("Test 8: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        
        // IOException test.
        raf.close();
        try {
            bytesRead = raf.read(rbuf, 0, 1);
            fail("Test 9: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readBoolean()
     * @tests java.io.RandomAccessFile#writeBoolean(boolean)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeBoolean",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readBoolean",
            args = {}
        )
    })
    public void test_read_writeBoolean() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeBoolean(true);
        raf.writeBoolean(false);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                true, raf.readBoolean());
        assertEquals("Test 2: Incorrect value written or read;", 
                false, raf.readBoolean());
        
        try {
            raf.readBoolean();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeBoolean(false);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readBoolean();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readByte()
     * @tests java.io.RandomAccessFile#writeByte(byte)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeByte",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readByte",
            args = {}
        )
    })
    public void test_read_writeByte() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeByte(Byte.MIN_VALUE);
        raf.writeByte(11);
        raf.writeByte(Byte.MAX_VALUE);
        raf.writeByte(Byte.MIN_VALUE - 1);
        raf.writeByte(Byte.MAX_VALUE + 1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                Byte.MIN_VALUE, raf.readByte());
        assertEquals("Test 2: Incorrect value written or read;", 
                11, raf.readByte());
        assertEquals("Test 3: Incorrect value written or read;", 
                Byte.MAX_VALUE, raf.readByte());
        assertEquals("Test 4: Incorrect value written or read;", 
                127, raf.readByte());
        assertEquals("Test 5: Incorrect value written or read;", 
                -128, raf.readByte());
        
        try {
            raf.readByte();
            fail("Test 6: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeByte(13);
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readByte();
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readChar()
     * @tests java.io.RandomAccessFile#writeChar(char)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeChar",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readChar",
            args = {}
        )
    })
    public void test_read_writeChar() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeChar(Character.MIN_VALUE);
        raf.writeChar('T');
        raf.writeChar(Character.MAX_VALUE);
        raf.writeChar(Character.MIN_VALUE - 1);
        raf.writeChar(Character.MAX_VALUE + 1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                Character.MIN_VALUE, raf.readChar());
        assertEquals("Test 2: Incorrect value written or read;", 
                'T', raf.readChar());
        assertEquals("Test 3: Incorrect value written or read;", 
                Character.MAX_VALUE, raf.readChar());
        assertEquals("Test 4: Incorrect value written or read;", 
                0xffff, raf.readChar());
        assertEquals("Test 5: Incorrect value written or read;", 
                0, raf.readChar());
        
        try {
            raf.readChar();
            fail("Test 6: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeChar('E');
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readChar();
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readDouble()
     * @tests java.io.RandomAccessFile#writeDouble(double)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeDouble",
            args = {double.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readDouble",
            args = {}
        )
    })
    public void test_read_writeDouble() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeDouble(Double.MAX_VALUE);
        raf.writeDouble(424242.4242);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                Double.MAX_VALUE, raf.readDouble());
        assertEquals("Test 2: Incorrect value written or read;", 
                424242.4242, raf.readDouble());
        
        try {
            raf.readDouble();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeDouble(Double.MIN_VALUE);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readDouble();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readFloat()
     * @tests java.io.RandomAccessFile#writeFloat(double)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeFloat",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readFloat",
            args = {}
        )
    })
    public void test_read_writeFloat() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeFloat(Float.MAX_VALUE);
        raf.writeFloat(555.55f);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read. ", 
                Float.MAX_VALUE, raf.readFloat());
        assertEquals("Test 2: Incorrect value written or read. ", 
                555.55f, raf.readFloat());
        
        try {
            raf.readFloat();
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeFloat(Float.MIN_VALUE);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readFloat();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readInt()
     * @tests java.io.RandomAccessFile#writeInt(char)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeInt",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readInt",
            args = {}
        )
    })
    public void test_read_writeInt() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeInt(Integer.MIN_VALUE);
        raf.writeInt('T');
        raf.writeInt(Integer.MAX_VALUE);
        raf.writeInt(Integer.MIN_VALUE - 1);
        raf.writeInt(Integer.MAX_VALUE + 1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                Integer.MIN_VALUE, raf.readInt());
        assertEquals("Test 2: Incorrect value written or read;", 
                'T', raf.readInt());
        assertEquals("Test 3: Incorrect value written or read;", 
                Integer.MAX_VALUE, raf.readInt());
        assertEquals("Test 4: Incorrect value written or read;", 
                0x7fffffff, raf.readInt());
        assertEquals("Test 5: Incorrect value written or read;", 
                0x80000000, raf.readInt());
        
        try {
            raf.readInt();
            fail("Test 6: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeInt('E');
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readInt();
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readLong()
     * @tests java.io.RandomAccessFile#writeLong(char)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeLong",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readLong",
            args = {}
        )
    })
    public void test_read_writeLong() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeLong(Long.MIN_VALUE);
        raf.writeLong('T');
        raf.writeLong(Long.MAX_VALUE);
        raf.writeLong(Long.MIN_VALUE - 1);
        raf.writeLong(Long.MAX_VALUE + 1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                Long.MIN_VALUE, raf.readLong());
        assertEquals("Test 2: Incorrect value written or read;", 
                'T', raf.readLong());
        assertEquals("Test 3: Incorrect value written or read;", 
                Long.MAX_VALUE, raf.readLong());
        assertEquals("Test 4: Incorrect value written or read;", 
                0x7fffffffffffffffl, raf.readLong());
        assertEquals("Test 5: Incorrect value written or read;", 
                0x8000000000000000l, raf.readLong());
        
        try {
            raf.readLong();
            fail("Test 6: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeLong('E');
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readLong();
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readShort()
     * @tests java.io.RandomAccessFile#writeShort(short)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeShort",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readShort",
            args = {}
        )
    })
    public void test_read_writeShort() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeShort(Short.MIN_VALUE);
        raf.writeShort('T');
        raf.writeShort(Short.MAX_VALUE);
        raf.writeShort(Short.MIN_VALUE - 1);
        raf.writeShort(Short.MAX_VALUE + 1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                Short.MIN_VALUE, raf.readShort());
        assertEquals("Test 2: Incorrect value written or read;", 
                'T', raf.readShort());
        assertEquals("Test 3: Incorrect value written or read;", 
                Short.MAX_VALUE, raf.readShort());
        assertEquals("Test 4: Incorrect value written or read;", 
                0x7fff, raf.readShort());
        assertEquals("Test 5: Incorrect value written or read;", 
                (short) 0x8000, raf.readShort());
        
        try {
            raf.readShort();
            fail("Test 6: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeShort('E');
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readShort();
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readUTF()
     * @tests java.io.RandomAccessFile#writeShort(char)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeUTF",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readUTF",
            args = {}
        )
    })
    public void test_read_writeUTF() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeUTF(unihw);
        raf.seek(0);
        assertEquals("Test 1: Incorrect UTF string written or read;", 
                unihw, raf.readUTF());
        
        try {
            raf.readUTF();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.writeUTF("Already closed.");
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readUTF();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#writeBytes(java.lang.String)
     * @tests java.io.RandomAccessFile#readFully(byte[])
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeBytes",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readFully",
            args = {byte[].class}
        )
    })
    public void test_readFully$B_writeBytesLjava_lang_String() throws IOException {
        byte[] buf = new byte[testLength];
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeBytes(testString);
        raf.seek(0);
        
        try {
            raf.readFully(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        
        raf.readFully(buf);
        assertEquals("Test 2: Incorrect bytes written or read;", 
                testString, new String(buf));

        try {
            raf.readFully(buf);
            fail("Test 3: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();        
        try {
            raf.writeBytes("Already closed.");
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        try {
            raf.readFully(buf);
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#writeBytes(java.lang.String)
     * @tests java.io.RandomAccessFile#readFully(byte[], int, int)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "writeBytes",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Tests against golden file missing.",
            method = "readFully",
            args = {byte[].class, int.class, int.class}
        )
    })
    public void test_readFully$BII() throws IOException {
        byte[] buf = new byte[testLength];
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeBytes(testString);
        raf.seek(0);
        
        try {
            raf.readFully(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        
        raf.readFully(buf, 5, testLength - 10);
        for (int i = 0; i < 5; i++) {
            assertEquals("Test 2: Incorrect bytes read;", 0, buf[i]);
        }
        assertEquals("Test 3: Incorrect bytes written or read;", 
                testString.substring(0, testLength - 10), 
                new String(buf, 5, testLength - 10));

        // Reading past the end of the file.
        try {
            raf.readFully(buf, 3, testLength - 6);
            fail("Test 4: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }

        // Passing invalid arguments.
        try {
            raf.readFully(buf, -1, 1);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            raf.readFully(buf, 0, -1);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            raf.readFully(buf, 2, testLength);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        
        // Reading from a closed file.
        raf.close();        
        try {
            raf.readFully(buf);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readUnsignedByte()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Tests against golden file missing.",
        method = "readUnsignedByte",
        args = {}
    )
    public void test_readUnsignedByte() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeByte(-1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                255, raf.readUnsignedByte());
        
        try {
            raf.readUnsignedByte();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.readUnsignedByte();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readUnsignedShort()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Tests against golden file missing.",
        method = "readUnsignedShort",
        args = {}
    )
    public void test_readUnsignedShort() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeShort(-1);
        raf.seek(0);
        
        assertEquals("Test 1: Incorrect value written or read;", 
                65535, raf.readUnsignedShort());
        
        try {
            raf.readUnsignedShort();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.readUnsignedShort();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#readLine()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "readLine",
        args = {}
    )
    public void test_readLine() throws IOException {
        // Test for method java.lang.String java.io.RandomAccessFile.readLine()
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        String s = "Goodbye\nCruel\nWorld\n";
        raf.write(s.getBytes(), 0, s.length());
        raf.seek(0);

        assertEquals("Test 1: Incorrect line read;", "Goodbye", raf.readLine());
        assertEquals("Test 2: Incorrect line read;", "Cruel", raf.readLine());
        assertEquals("Test 3: Incorrect line read;", "World", raf.readLine());
        assertNull("Test 4: Incorrect line read; null expected.", raf.readLine());
        
        raf.close();
        try {
            raf.readLine();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
       
    }

    /**
     * @tests java.io.RandomAccessFile#seek(long)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "seek",
        args = {long.class}
    )
    public void test_seekJ() throws IOException {
        // Test for method void java.io.RandomAccessFile.seek(long)
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        
        try {
            raf.seek(-1);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // BEGIN android-added
        try {
            // Android uses 32-bit off_t, so anything larger than a signed 32-bit int won't work.
            raf.seek(((long) Integer.MAX_VALUE) + 1);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // END android-added

        raf.write(testString.getBytes(), 0, testLength);
        raf.seek(12);
        assertEquals("Test 3: Seek failed to set file pointer.", 12,
                raf.getFilePointer());
        
        raf.close();
        try {
            raf.seek(1);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#skipBytes(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "skipBytes",
        args = {int.class}
    )
    public void test_skipBytesI() throws IOException {
        byte[] buf = new byte[5];
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeBytes("HelloWorld");
        raf.seek(0);
        
        assertTrue("Test 1: Nothing should be skipped if parameter is less than zero",
                raf.skipBytes(-1) == 0);
        
        assertEquals("Test 4: Incorrect number of bytes skipped; ", 
                5, raf.skipBytes(5));

        raf.readFully(buf);
        assertEquals("Test 3: Failed to skip bytes.", 
                "World", new String(buf, 0, 5));

        raf.seek(0);
        assertEquals("Test 4: Incorrect number of bytes skipped; ", 
                10, raf.skipBytes(20));

        raf.close();
        try {
            raf.skipBytes(1);
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.RandomAccessFile#skipBytes(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLength",
        args = {long.class}
    )
    public void test_setLengthJ() throws IOException {
        int bytesRead;
        long truncLength = (long) (testLength * 0.75);
        byte[] rbuf = new byte[testLength + 10];
        
        // Setup the test file.
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.write(testString.getBytes());
        assertEquals("Test 1: Incorrect file length;", 
                testLength, raf.length());
        
        // Truncate the file.
        raf.setLength(truncLength);
        assertTrue("Test 2: File pointer not moved to the end of the truncated file.", 
                raf.getFilePointer() == truncLength);
        
        raf.close();
        raf = new java.io.RandomAccessFile(fileName, "rw");
        assertEquals("Test 3: Incorrect file length;", 
                truncLength, raf.length());
        bytesRead = raf.read(rbuf);
        assertEquals("Test 4: Incorrect number of bytes read;", 
                truncLength, bytesRead);
        assertEquals("Test 5: Incorrect bytes read. ", 
                testString.substring(0, bytesRead),
                new String(rbuf, 0, bytesRead));
        
        // Expand the file.
        raf.setLength(testLength + 2);
        assertTrue("Test 6: File pointer incorrectly moved.", 
                raf.getFilePointer() == truncLength);
        assertEquals("Test 7: Incorrect file length;", 
                testLength + 2, raf.length());
        
        // BEGIN android-added
        // Exception testing.
        try {
            // Android uses 32-bit off_t, so anything larger than a signed 32-bit int won't work.
            raf.setLength(((long) Integer.MAX_VALUE) + 1);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        // END android-added

        // Exception testing.
        try {
            raf.setLength(-1);
            fail("Test 9: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        
        raf.close();
        try {
            raf.setLength(truncLength);
            fail("Test 10: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }
    
    /**
     * @tests java.io.RandomAccessFile#write(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class}
    )
    public void test_write$B() throws IOException {
        byte[] rbuf = new byte[4000];
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        
        byte[] nullByteArray = null;
        try {
            raf.write(nullByteArray);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }   
        
        try {
            raf.write(testString.getBytes());
        } catch (Exception e) {
            fail("Test 2: Unexpected exception: " + e.getMessage());
        }
        
        raf.close();

        try {
            raf.write(new byte[0]);
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException: " + e.getMessage());
        }
        
        try {
            raf.write(testString.getBytes());
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        
        FileInputStream fis = new java.io.FileInputStream(fileName);
        fis.read(rbuf, 0, testLength);
        assertEquals("Incorrect bytes written", testString, new String(rbuf, 0,
                testLength));    
    }

    /**
     * @tests java.io.RandomAccessFile#write(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII() throws Exception {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        byte[] rbuf = new byte[4000];
        byte[] testBuf = null;
        int bytesRead;
        
        try {
            raf.write(testBuf, 1, 1);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        
        testBuf = testString.getBytes();
        
        try {
            raf.write(testBuf, -1, 10);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
            assertEquals(
                    "Test 2: IndexOutOfBoundsException rather than a subclass expected.",
                    IndexOutOfBoundsException.class, e.getClass());
        }
        
        try {
            raf.write(testBuf, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
            assertEquals(
                    "Test 3: IndexOutOfBoundsException rather than a subclass expected.",
                    IndexOutOfBoundsException.class, e.getClass());
        }
        
        try {
            raf.write(testBuf, 5, testLength);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
            assertEquals(
                    "Test 4: IndexOutOfBoundsException rather than a subclass expected.",
                    IndexOutOfBoundsException.class, e.getClass());
        }
        
        // Positive test: The following write should not fail.
        try {
            raf.write(testBuf, 3, testLength - 5);
        } catch (Exception e) {
            fail("Test 5: Unexpected exception: " + e.getMessage());
        }
        
        raf.close();
        
        // Writing nothing to a closed file should not fail either.
        try {
            raf.write(new byte[0]);
        } catch (IOException e) {
            fail("Test 6: Unexpected IOException: " + e.getMessage());
        }
        
        // Writing something to a closed file should fail.
        try {
            raf.write(testString.getBytes());
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        
        FileInputStream fis = new java.io.FileInputStream(fileName);
        bytesRead = fis.read(rbuf, 0, testLength);
        assertEquals("Test 8: Incorrect number of bytes written or read;",
                testLength - 5, bytesRead);
        assertEquals("Test 9: Incorrect bytes written or read; ", 
                testString.substring(3, testLength - 2), 
                new String(rbuf, 0, bytesRead));    
    }
    
    /**
     * @tests java.io.RandomAccessFile#writeChars(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Tests against golden file missing.",
        method = "writeChars",
        args = {java.lang.String.class}
    )
    public void test_writeCharsLjava_lang_String() throws IOException {
        RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
        raf.writeChars(unihw);
        char[] hchars = new char[unihw.length()];
        unihw.getChars(0, unihw.length(), hchars, 0);
        raf.seek(0);
        for (int i = 0; i < hchars.length; i++)
            assertEquals("Test 1: Incorrect character written or read at index " + i + ";",
                    hchars[i], raf.readChar());
        raf.close();
        try {
            raf.writeChars("Already closed.");
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();

        f = File.createTempFile("raf", "tst");
        if (!f.delete()) {
            fail("Unable to delete test file : " + f);
        }
        fileName = f.getAbsolutePath();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        if (f.exists()) {
            f.delete();
        }
        super.tearDown();
    }

}

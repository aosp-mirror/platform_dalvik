/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.archive.tests.java.util.zip;

import tests.support.resource.Support_Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;
import java.util.zip.ZipEntry;

public class ZipEntryTest extends junit.framework.TestCase {

// BEGIN android-added
    public byte[] getAllBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] buf = new byte[666];
        int iRead; int off;
        while (is.available() > 0) {
            iRead = is.read(buf, 0, buf.length);
            if (iRead > 0)
                bs.write(buf, 0, iRead);
        }
        return bs.toByteArray();
    }
// END android-added

    // zip file hyts_ZipFile.zip must be included as a resource
    java.util.zip.ZipEntry zentry;

    java.util.zip.ZipFile zfile;

    private static final String platformId = System.getProperty(
            "com.ibm.oti.configuration", "JDK")
            + System.getProperty("java.vm.version");

    static final String tempFileName = platformId + "zfzezi.zip";

    long orgSize;

    long orgCompressedSize;

    long orgCrc;

    long orgTime;

    String orgComment;

    /**
     * @tests java.util.zip.ZipEntry#ZipEntry(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.util.zip.ZipEntry(java.lang.String)
        zentry = zfile.getEntry("File3.txt");
        assertNotNull("Failed to create ZipEntry", zentry);
        try {
            zentry = zfile.getEntry(null);
            fail("NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 65535; i++) {
            s.append('a');
        }
        try {
            zentry = new ZipEntry(s.toString());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException During Test.");
        }
        try {
            s.append('a');
            zentry = new ZipEntry(s.toString());
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            String n = null;
            zentry = new ZipEntry(n);
            fail("NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.util.zip.ZipEntry#getComment()
     */
    public void test_getComment() {
        // Test for method java.lang.String java.util.zip.ZipEntry.getComment()
        ZipEntry zipEntry = new ZipEntry("zippy.zip");
        assertNull("Incorrect Comment Returned.", zipEntry.getComment());
        zipEntry.setComment("This Is A Comment");
        assertEquals("Incorrect Comment Returned.", 
                "This Is A Comment", zipEntry.getComment());
    }

    /**
     * @tests java.util.zip.ZipEntry#getCompressedSize()
     */
    public void test_getCompressedSize() {
        // Test for method long java.util.zip.ZipEntry.getCompressedSize()
        assertTrue("Incorrect compressed size returned", zentry
                .getCompressedSize() == orgCompressedSize);
    }

    /**
     * @tests java.util.zip.ZipEntry#getCrc()
     */
    public void test_getCrc() {
        // Test for method long java.util.zip.ZipEntry.getCrc()
        assertTrue("Failed to get Crc", zentry.getCrc() == orgCrc);
    }

    /**
     * @tests java.util.zip.ZipEntry#getExtra()
     */
    public void test_getExtra() {
        // Test for method byte [] java.util.zip.ZipEntry.getExtra()
        assertNull("Incorrect extra information returned",
                zentry.getExtra());
        byte[] ba = { 'T', 'E', 'S', 'T' };
        zentry = new ZipEntry("test.tst");
        zentry.setExtra(ba);
        assertTrue("Incorrect Extra Information Returned.",
                zentry.getExtra() == ba);
    }

    /**
     * @tests java.util.zip.ZipEntry#getMethod()
     */
    public void test_getMethod() {
        // Test for method int java.util.zip.ZipEntry.getMethod()
        zentry = zfile.getEntry("File1.txt");
        assertTrue("Incorrect compression method returned",
                zentry.getMethod() == java.util.zip.ZipEntry.STORED);
        zentry = zfile.getEntry("File3.txt");
        assertTrue("Incorrect compression method returned",
                zentry.getMethod() == java.util.zip.ZipEntry.DEFLATED);
        zentry = new ZipEntry("test.tst");
        assertEquals("Incorrect Method Returned.", -1, zentry.getMethod());
    }

    /**
     * @tests java.util.zip.ZipEntry#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.util.zip.ZipEntry.getName()
        assertEquals("Incorrect name returned - Note return result somewhat ambiguous in spec",
                "File1.txt", zentry.getName());
    }

    /**
     * @tests java.util.zip.ZipEntry#getSize()
     */
    public void test_getSize() {
        // Test for method long java.util.zip.ZipEntry.getSize()
        assertTrue("Incorrect size returned", zentry.getSize() == orgSize);
    }

    /**
     * @tests java.util.zip.ZipEntry#getTime()
     */
    public void test_getTime() {
        // Test for method long java.util.zip.ZipEntry.getTime()
        assertTrue("Failed to get time", zentry.getTime() == orgTime);
    }

    /**
     * @tests java.util.zip.ZipEntry#isDirectory()
     */
    public void test_isDirectory() {
        // Test for method boolean java.util.zip.ZipEntry.isDirectory()
        assertTrue("Entry should not answer true to isDirectory", !zentry
                .isDirectory());
        zentry = new ZipEntry("Directory/");
        assertTrue("Entry should answer true to isDirectory", zentry
                .isDirectory());
    }

    /**
     * @tests java.util.zip.ZipEntry#setComment(java.lang.String)
     */
    public void test_setCommentLjava_lang_String() {
        // Test for method void
        // java.util.zip.ZipEntry.setComment(java.lang.String)
        zentry = zfile.getEntry("File1.txt");
        zentry.setComment("Set comment using api");
        assertEquals("Comment not correctly set", 
                "Set comment using api", zentry.getComment());
        String n = null;
        zentry.setComment(n);
        assertNull("Comment not correctly set", zentry.getComment());
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 0xFFFF; i++) {
            s.append('a');
        }
        try {
            zentry.setComment(s.toString());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException During Test.");
        }
        try {
            s.append('a');
            zentry.setComment(s.toString());
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * @tests java.util.zip.ZipEntry#setCompressedSize(long)
     */
    public void test_setCompressedSizeJ() {
        // Test for method void java.util.zip.ZipEntry.setCompressedSize(long)
        zentry.setCompressedSize(orgCompressedSize + 10);
        assertTrue("Set compressed size failed",
                zentry.getCompressedSize() == (orgCompressedSize + 10));
        zentry.setCompressedSize(0);
        assertEquals("Set compressed size failed",
                0, zentry.getCompressedSize());
        zentry.setCompressedSize(-25);
        assertEquals("Set compressed size failed",
                -25, zentry.getCompressedSize());
        zentry.setCompressedSize(4294967296l);
        assertTrue("Set compressed size failed",
                zentry.getCompressedSize() == 4294967296l);
    }

    /**
     * @tests java.util.zip.ZipEntry#setCrc(long)
     */
    public void test_setCrcJ() {
        // Test for method void java.util.zip.ZipEntry.setCrc(long)
        zentry.setCrc(orgCrc + 100);
        assertTrue("Failed to set Crc", zentry.getCrc() == (orgCrc + 100));
        zentry.setCrc(0);
        assertEquals("Failed to set Crc", 0, zentry.getCrc());
        try {
            zentry.setCrc(-25);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            zentry.setCrc(4294967295l);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            zentry.setCrc(4294967296l);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * @tests java.util.zip.ZipEntry#setExtra(byte[])
     */
    public void test_setExtra$B() {
        // Test for method void java.util.zip.ZipEntry.setExtra(byte [])
        zentry = zfile.getEntry("File1.txt");
        zentry.setExtra("Test setting extra information".getBytes());
        assertEquals("Extra information not written properly", "Test setting extra information", new String(zentry
                .getExtra(), 0, zentry.getExtra().length)
                );
        zentry = new ZipEntry("test.tst");
        byte[] ba = new byte[0xFFFF];
        try {
            zentry.setExtra(ba);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            ba = new byte[0xFFFF + 1];
            zentry.setExtra(ba);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }

        // One constructor
        ZipEntry zeInput = new ZipEntry("InputZIP");
        byte[] extraB = { 'a', 'b', 'd', 'e' };
        zeInput.setExtra(extraB);
        assertEquals(extraB, zeInput.getExtra());
        assertEquals(extraB[3], zeInput.getExtra()[3]);
        assertEquals(extraB.length, zeInput.getExtra().length);

        // test another constructor
        ZipEntry zeOutput = new ZipEntry(zeInput);
        assertEquals(zeInput.getExtra()[3], zeOutput.getExtra()[3]);
        assertEquals(zeInput.getExtra().length, zeOutput.getExtra().length);
        assertEquals(extraB[3], zeOutput.getExtra()[3]);
        assertEquals(extraB.length, zeOutput.getExtra().length);
    }

    /**
     * @tests java.util.zip.ZipEntry#setMethod(int)
     */
    public void test_setMethodI() {
        // Test for method void java.util.zip.ZipEntry.setMethod(int)
        zentry = zfile.getEntry("File3.txt");
        zentry.setMethod(ZipEntry.STORED);
        assertTrue("Failed to set compression method",
                zentry.getMethod() == ZipEntry.STORED);
        zentry.setMethod(ZipEntry.DEFLATED);
        assertTrue("Failed to set compression method",
                zentry.getMethod() == ZipEntry.DEFLATED);
        try {
            int error = 1;
            zentry = new ZipEntry("test.tst");
            zentry.setMethod(error);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * @tests java.util.zip.ZipEntry#setSize(long)
     */
    public void test_setSizeJ() {
        // Test for method void java.util.zip.ZipEntry.setSize(long)
        zentry.setSize(orgSize + 10);
        assertTrue("Set size failed", zentry.getSize() == (orgSize + 10));
        zentry.setSize(0);
        assertEquals("Set size failed", 0, zentry.getSize());
        try {
            zentry.setSize(-25);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            zentry.setCrc(4294967295l);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            zentry.setCrc(4294967296l);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * @tests java.util.zip.ZipEntry#setTime(long)
     */
    public void test_setTimeJ() {
        // Test for method void java.util.zip.ZipEntry.setTime(long)
        zentry.setTime(orgTime + 10000);
        assertTrue("Test 1: Failed to set time: " + zentry.getTime(), zentry
                .getTime() == (orgTime + 10000));
        zentry.setTime(orgTime - 10000);
        assertTrue("Test 2: Failed to set time: " + zentry.getTime(), zentry
                .getTime() == (orgTime - 10000));
        TimeZone zone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("EST"));
            zentry.setTime(0);
            assertTrue("Test 3: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            assertTrue("Test 3a: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315532800000L);
            zentry.setTime(0);
            TimeZone.setDefault(TimeZone.getTimeZone("EST"));
            assertTrue("Test 3b: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);

            zentry.setTime(-25);
            assertTrue("Test 4: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
            zentry.setTime(4354837200000L);
            assertTrue("Test 5: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
        } finally {
            TimeZone.setDefault(zone);
        }
    }

    /**
     * @tests java.util.zip.ZipEntry#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.util.zip.ZipEntry.toString()
        assertTrue("Returned incorrect entry name", zentry.toString().indexOf(
                "File1.txt") >= 0);
    }

    /**
     * @tests java.util.zip.ZipEntry#ZipEntry(java.util.zip.ZipEntry)
     */
    public void test_ConstructorLjava_util_zip_ZipEntry() {
        // Test for method java.util.zip.ZipEntry(util.zip.ZipEntry)
        zentry.setSize(2);
        zentry.setCompressedSize(4);
        zentry.setComment("Testing");
        ZipEntry zentry2 = new ZipEntry(zentry);
        assertEquals("ZipEntry Created With Incorrect Size.",
                2, zentry2.getSize());
        assertEquals("ZipEntry Created With Incorrect Compressed Size.", 4, zentry2
                .getCompressedSize());
        assertEquals("ZipEntry Created With Incorrect Comment.", "Testing", zentry2
                .getComment());
        assertTrue("ZipEntry Created With Incorrect Crc.",
                zentry2.getCrc() == orgCrc);
        assertTrue("ZipEntry Created With Incorrect Time.",
                zentry2.getTime() == orgTime);
    }

    /**
     * @tests java.util.zip.ZipEntry#clone()
     */
    public void test_clone() {
        // Test for method java.util.zip.ZipEntry.clone()
        Object obj = zentry.clone();
        assertTrue("toString()", obj.toString().equals(zentry.toString()));
        assertTrue("hashCode()", obj.hashCode() == zentry.hashCode());

        // One constructor
        ZipEntry zeInput = new ZipEntry("InputZIP");
        byte[] extraB = { 'a', 'b', 'd', 'e' };
        zeInput.setExtra(extraB);
        assertEquals(extraB, zeInput.getExtra());
        assertEquals(extraB[3], zeInput.getExtra()[3]);
        assertEquals(extraB.length, zeInput.getExtra().length);

        // test Clone()
        ZipEntry zeOutput = (ZipEntry) zeInput.clone();
        assertEquals(zeInput.getExtra()[3], zeOutput.getExtra()[3]);
        assertEquals(zeInput.getExtra().length, zeOutput.getExtra().length);
        assertEquals(extraB[3], zeOutput.getExtra()[3]);
        assertEquals(extraB.length, zeOutput.getExtra().length);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */

    @Override
    protected void setUp() {
        java.io.File f = null;
        try {
            // BEGIN android-changed
            // Create a local copy of the file since some tests want to alter
            // information.
            f = new java.io.File(tempFileName);
            // Create absolute filename as ZipFile does not resolve using
            // user.dir
            f = new java.io.File(f.getAbsolutePath());
            f.delete();
            java.io.InputStream is = Support_Resources
                    .getStream("hyts_ZipFile.zip");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
            byte[] rbuf = getAllBytesFromStream(is);
            // END android-changed
            fos.write(rbuf, 0, rbuf.length);
            is.close();
            fos.close();
            zfile = new java.util.zip.ZipFile(f);
            zentry = zfile.getEntry("File1.txt");
            orgSize = zentry.getSize();
            orgCompressedSize = zentry.getCompressedSize();
            orgCrc = zentry.getCrc();
            orgTime = zentry.getTime();
            orgComment = zentry.getComment();
        } catch (Exception e) {
            System.out.println("Exception during ZipFile setup <"
                    + f.getAbsolutePath() + ">: ");
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
            if (zfile != null) {
                zfile.close();
            }
            java.io.File f = new java.io.File(tempFileName);
            f.delete();
        } catch (java.io.IOException e) {
            System.out.println("Exception during tearDown");
        }
    }

}

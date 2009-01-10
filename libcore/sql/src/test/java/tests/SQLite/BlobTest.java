/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.SQLite;

import SQLite.Blob;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.OutputStream;

@TestTargetClass(Blob.class)
public class BlobTest extends TestCase {
    
    private static Blob testBlob = null;
    
    private byte[] blobInput= null;
    
    private static InputStream file = null;

    
    public BlobTest(String name) {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        testBlob = new Blob();
        
        // can not fill Blob with data at this point...
        /*
        File resources = Support_Resources.createTempFolder();
        BufferedReader r = null;
        try {
            Class c = Class.forName(this.getClass().getName());
            assertNotNull(c);
            file = Class.forName(this.getClass().getName())
                    .getResourceAsStream("/blob.c");
            r = new BufferedReader(new InputStreamReader(file));
        } catch (NullPointerException e) {
            fail("Should not throw NullPointerException reading file"
                    + e.getMessage());
        }
        OutputStream out = testBlob.getOutputStream();
        String s = null;
        while ((s = r.readLine()) != null) {
            out.write(r.readLine().getBytes());
        }
        out.flush();
        out.close();
        testBlob.close();
        */
    }

    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
        testBlob.close();
    }
    /**
     * @tests Blob#Blob()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "constructor test",
        method = "Blob",
        args = {}
    )
    public void _testBlob() {
        Blob b = new Blob();
        assertNotNull(b);
        //assertEquals(0, b.size);
    }

    /**
     * @tests Blob#finalize()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "method test",
        method = "finalize",
        args = {}
    )
    public void _testFinalize() {
        fail("Not yet implemented");
    }

    /**
     * @tests Blob.getInputStream()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "getInputStream",
        args = {}
    )
    public void testGetInputStream() {
        InputStream in = testBlob.getInputStream();
        assertNotNull(in);
        try {
            in.read();
            fail("Read operation unsupported");
        } catch (Throwable e) {
            //ok
        }
        
        /*
        byte[] defaultByteArray = null;
        BufferedReader actual = new BufferedReader(new InputStreamReader(
                testBlob.getInputStream()));
        byte[] b1;
        byte[] b2;
        try {
            BufferedReader shouldBe = new BufferedReader(new InputStreamReader(
                    this.file));
            while (((b1 = actual.readLine().getBytes()) != null)
                    && ((b2 = shouldBe.readLine().getBytes()) != null)) {
                assertEquals(b2, b1);
            }
            assertEquals("both finished", shouldBe.readLine(), actual
                    .readLine());
        } catch (IOException e) {
            fail("Error in test setup: " + e.toString());
            e.printStackTrace();
        }
        */
    }

    /**
     * @tests Blob#getOutputStream()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "getOutputStream",
        args = {}
    )
    public void testGetOutputStream() {
        OutputStream out = testBlob.getOutputStream();
        assertNotNull(out);
        try {
           out.write(null);
           fail("Write operation unsupported");
        } catch (Throwable e) {
            assertEquals("Write operation unsupported", e.getMessage());
        }
    }

    /**
     * @tests Blob#close()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "method test",
        method = "close",
        args = {}
    )
    public void _testClose() {
        try {
        testBlob.close();
        testBlob.close();
        testBlob.getInputStream();
        //assertEquals(0, testBlob.size);
        } catch (Throwable e) {
            fail("Tests failed");
        }
    }

    // these tests show that read and write are unsupported -> blob is unsupported
//    /**
//     * @tests Blob#write(byte[], int, int, int)
//     */
//    @TestTargetNew(
//        level = TestLevel.COMPLETE,
//        notes = "method test",
//        method = "write",
//        args = {byte[].class, int.class, int.class, int.class}
//    )
//    public void testWrite() {
//        try {
//            testBlob.write(null, 0, 0, 0);
//            fail("Write operation unsupported");
//        } catch (Throwable e) {
//            //ok
//        }
//    }
//
//    /**
//     * @tests Blob#read()
//     */
//    @TestTargetNew(
//        level = TestLevel.COMPLETE,
//        notes = "method test",
//        method = "read",
//        args = {}
//    )
//    public void testRead() {
//        Blob b = new Blob();
//        try {
//            testBlob.read(null, 0, 0, 0);
//            fail("Read operation unsupported");
//        } catch (Throwable e) {
//            //ok
//        }
//    }
    
}

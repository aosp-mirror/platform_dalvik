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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

import junit.framework.TestCase;

public class DeflaterInputStreamTest extends TestCase {

    String testStr = "Hi,this is a test";

    InputStream is;

    /**
     * @tests DeflaterInputStream#available()
     */
    public void testAvailable() throws IOException {
        byte[] buf = new byte[1024];
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        assertEquals(120, dis.read());
        assertEquals(1, dis.available());
        assertEquals(22, dis.read(buf, 0, 1024));
        assertEquals(1, dis.available());
        assertEquals(-1, dis.read());
        assertEquals(0, dis.available());
        dis.close();
        try {
            dis.available();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests DeflaterInputStream#close()
     */
    public void testClose() throws IOException {
        byte[] buf = new byte[1024];
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        dis.close();
        try {
            dis.available();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf, 0, 1024);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        // can close after close
        dis.close();
    }

    /**
     * @tests DeflaterInputStream#mark()
     */
    public void testMark() throws IOException {
        // mark do nothing
        DeflaterInputStream dis = new DeflaterInputStream(is);
        dis.mark(-1);
        dis.mark(0);
        dis.mark(1);
        dis.close();
        dis.mark(1);
    }

    /**
     * @tests DeflaterInputStream#markSupported()
     */
    public void testMarkSupported() throws IOException {
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertFalse(dis.markSupported());
        dis.close();
        assertFalse(dis.markSupported());
    }

    /**
     * @tests DeflaterInputStream#read()
     */
    public void testRead() throws IOException {
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        assertEquals(120, dis.read());
        assertEquals(1, dis.available());
        assertEquals(156, dis.read());
        assertEquals(1, dis.available());
        assertEquals(243, dis.read());
        assertEquals(1, dis.available());
        dis.close();
        try {
            dis.read();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests DeflaterInputStream#read(byte[],int,int)
     */
    public void testReadByteArrayIntInt() throws IOException {
        byte[] buf1 = new byte[256];
        byte[] buf2 = new byte[256];
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(23, dis.read(buf1, 0, 256));
        dis = new DeflaterInputStream(is);
        assertEquals(8, dis.read(buf2, 0, 256));
        is = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
        dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        assertEquals(120, dis.read());
        assertEquals(1, dis.available());
        assertEquals(22, dis.read(buf2, 0, 256));
        assertEquals(1, dis.available());
        assertEquals(-1, dis.read());
        assertEquals(0, dis.available());
        try {
            dis.read(buf1, 0, 512);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            dis.read(null, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            dis.read(null, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            dis.read(null, -1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            dis.read(buf1, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            dis.read(buf1, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        dis.close();
        try {
            dis.read(buf1, 0, 512);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf1, 0, 1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(null, 0, 0);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(null, -1, 0);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(null, -1, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf1, -1, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf1, 0, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests DeflaterInputStream#reset()
     */
    public void testReset() throws IOException {
        DeflaterInputStream dis = new DeflaterInputStream(is);
        try {
            dis.reset();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        dis.close();
        try {
            dis.reset();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests DeflaterInputStream#skip()
     */
    public void testSkip() throws IOException {
        byte[] buf = new byte[1024];
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        dis.skip(1);
        assertEquals(1, dis.available());
        assertEquals(22, dis.read(buf, 0, 1024));
        assertEquals(1, dis.available());
        dis.skip(1);
        assertEquals(0, dis.available());
        is = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
        dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        dis.skip(56);
        assertEquals(0, dis.available());
        assertEquals(-1, dis.read(buf, 0, 1024));
        try {
            dis.skip(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(0, dis.available());
        // can still skip
        dis.skip(1);
        dis.close();
        try {
            dis.skip(1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.skip(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        is = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
        dis = new DeflaterInputStream(is);
        assertEquals(23, dis.skip(Long.MAX_VALUE));
        assertEquals(0, dis.available());
    }

    /**
     * @tests DeflaterInputStream#DeflaterInputStream(InputStream)
     */
    public void testDeflaterInputStreamInputStream() {
        // ok
        new DeflaterInputStream(is);
        // fail
        try {
            new DeflaterInputStream(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests DeflaterInputStream#DeflaterInputStream(InputStream,Deflater)
     */
    public void testDeflaterInputStreamInputStreamDeflater() {
        // ok
        new DeflaterInputStream(is, new Deflater());
        // fail
        try {
            new DeflaterInputStream(is, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DeflaterInputStream(null, new Deflater());
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests DeflaterInputStream#DeflaterInputStream(InputStream,Deflater,int)
     */
    public void testDeflaterInputStreamInputStreamDeflaterInt() {
        // ok
        new DeflaterInputStream(is, new Deflater(), 1024);
        // fail
        try {
            new DeflaterInputStream(is, null, 1024);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DeflaterInputStream(null, new Deflater(), 1024);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DeflaterInputStream(is, new Deflater(), -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            new DeflaterInputStream(null, new Deflater(), -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DeflaterInputStream(is, null, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        is = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
    }

    @Override
    protected void tearDown() throws Exception {
        is.close();
        super.tearDown();
    }
}

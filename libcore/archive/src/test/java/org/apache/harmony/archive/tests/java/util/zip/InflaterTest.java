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

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.Adler32;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

import tests.support.resource.Support_Resources;

@TestTargetClass(Inflater.class)
public class InflaterTest extends junit.framework.TestCase {
    byte outPutBuff1[] = new byte[500];

    byte outPutDiction[] = new byte[500];

    /**
     * @tests java.util.zip.Inflater#end()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "end",
        args = {}
    )
    public void test_end() {
        // test method of java.util.zip.inflater.end()
        byte byteArray[] = {5, 2, 3, 7, 8};

        int r = 0;
        Inflater inflate = new Inflater();
        inflate.setInput(byteArray);
        inflate.end();
        try {
            inflate.reset();
            inflate.setInput(byteArray);
        } catch (NullPointerException e) {
            r = 1;
        }
        assertEquals("inflate can still be used after end is called", 1, r);

        Inflater i = new Inflater();
        i.end();
        // check for exception
        i.end();
    }

    /**
     * @tests java.util.zip.Inflater#finished()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "finished",
        args = {}
    )
    public void test_finished() {
        // test method of java.util.zip.inflater.finished()
        byte byteArray[] = {1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5'};
        Inflater inflate = new Inflater(false);
        byte outPutInf[] = new byte[500];
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuff1);
                }

                inflate.inflate(outPutInf);
            }
            assertTrue(
                    "the method finished() returned false when no more data needs to be decompressed",
                    inflate.finished());
        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        }
        for (int i = 0; i < byteArray.length; i++) {
            assertTrue(
                    "Final decompressed data does not equal the original data",
                    byteArray[i] == outPutInf[i]);
        }
        assertEquals(
                "final decompressed data contained more bytes than original - finished()",
                0, outPutInf[byteArray.length]);
    }

    /**
     * @tests java.util.zip.Inflater#getAdler()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAdler",
        args = {}
    )
    public void test_getAdler() {
        // test method of java.util.zip.inflater.getAdler()
        byte dictionaryArray[] = {'e', 'r', 't', 'a', 'b', 2, 3};

        Inflater inflateDiction = new Inflater();
        inflateDiction.setInput(outPutDiction);
        if (inflateDiction.needsDictionary() == true) {
            // getting the checkSum value through the Adler32 class
            Adler32 adl = new Adler32();
            adl.update(dictionaryArray);
            long checkSumR = adl.getValue();
            assertTrue(
                    "the checksum value returned by getAdler() is not the same as the checksum returned by creating the adler32 instance",
                    checkSumR == inflateDiction.getAdler());
        }
    }

    /**
     * @tests java.util.zip.Inflater#getRemaining()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRemaining",
        args = {}
    )
    public void test_getRemaining() {
        // test method of java.util.zip.inflater.getRemaining()
        byte byteArray[] = {1, 3, 5, 6, 7};
        Inflater inflate = new Inflater();
        assertEquals(
                "upon creating an instance of inflate, getRemaining returned a non zero value",
                0, inflate.getRemaining());
        inflate.setInput(byteArray);
        assertTrue(
                "getRemaining returned zero when there is input in the input buffer",
                inflate.getRemaining() != 0);
    }

    /**
     * @tests java.util.zip.Inflater#getTotalIn()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTotalIn",
        args = {}
    )
    public void test_getTotalIn() {
        // test method of java.util.zip.inflater.getTotalIn()
        // creating the decompressed data
        byte outPutBuf[] = new byte[500];
        byte byteArray[] = {1, 3, 4, 7, 8};
        byte outPutInf[] = new byte[500];
        int x = 0;
        Deflater deflate = new Deflater(1);
        deflate.setInput(byteArray);
        while (!(deflate.needsInput())) {
            x += deflate.deflate(outPutBuf, x, outPutBuf.length - x);
        }
        deflate.finish();
        while (!(deflate.finished())) {
            x = x + deflate.deflate(outPutBuf, x, outPutBuf.length - x);
        }

        Inflater inflate = new Inflater();
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuf);
                }

                inflate.inflate(outPutInf);
            }
        } catch (DataFormatException e) {
            fail("Input to inflate is invalid or corrupted - getTotalIn");
        }
        // System.out.print(deflate.getTotalOut() + " " + inflate.getTotalIn());
        assertTrue(
                "the total byte in outPutBuf did not equal the byte returned in getTotalIn",
                inflate.getTotalIn() == deflate.getTotalOut());

        Inflater inflate2 = new Inflater();
        int offSet = 0;// seems only can start as 0
        int length = 4;
        try {
            // seems no while loops allowed
            if (inflate2.needsInput()) {
                inflate2.setInput(outPutBuff1, offSet, length);
            }

            inflate2.inflate(outPutInf);

        } catch (DataFormatException e) {
            fail("Input to inflate is invalid or corrupted - getTotalIn");
        }
        // System.out.print(inflate2.getTotalIn() + " " + length);
        assertTrue(
                "total byte dictated by length did not equal byte returned in getTotalIn",
                inflate2.getTotalIn() == length);
    }

    /**
     * @tests java.util.zip.Inflater#getTotalOut()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTotalOut",
        args = {}
    )
    public void test_getTotalOut() {
        // test method of java.util.zip.inflater.Inflater()
        // creating the decompressed data
        byte outPutBuf[] = new byte[500];
        byte byteArray[] = {1, 3, 4, 7, 8};
        int y = 0;
        int x = 0;
        Deflater deflate = new Deflater(1);
        deflate.setInput(byteArray);
        while (!(deflate.needsInput())) {
            x += deflate.deflate(outPutBuf, x, outPutBuf.length - x);
        }
        deflate.finish();
        while (!(deflate.finished())) {
            x = x + deflate.deflate(outPutBuf, x, outPutBuf.length - x);
        }

        Inflater inflate = new Inflater();
        byte outPutInf[] = new byte[500];
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuf);
                }

                y += inflate.inflate(outPutInf);
            }
        } catch (DataFormatException e) {
            fail("Input to inflate is invalid or corrupted - getTotalIn");
        }

        assertTrue(
                "the sum of the bytes returned from inflate does not equal the bytes of getTotalOut()",
                y == inflate.getTotalOut());
        assertTrue(
                "the total number of bytes to be compressed does not equal the total bytes decompressed",
                inflate.getTotalOut() == deflate.getTotalIn());

        // testing inflate(byte,int,int)
        inflate.reset();
        y = 0;
        int offSet = 0;// seems only can start as 0
        int length = 4;
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuf);
                }

                y += inflate.inflate(outPutInf, offSet, length);
            }
        } catch (DataFormatException e) {
            System.out
                    .println("Input to inflate is invalid or corrupted - getTotalIn");
        }
        assertTrue(
                "the sum of the bytes returned from inflate does not equal the bytes of getTotalOut()",
                y == inflate.getTotalOut());
        assertTrue(
                "the total number of bytes to be compressed does not equal the total bytes decompressed",
                inflate.getTotalOut() == deflate.getTotalIn());
    }

    /**
     * @tests java.util.zip.Inflater#inflate(byte[])
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "DataFormatException checking missed.",
        method = "inflate",
        args = {byte[].class}
    )
    public void test_inflate$B() {
        // test method of java.util.zip.inflater.inflate(byte)

        byte byteArray[] = {1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5'};
        byte outPutInf[] = new byte[500];
        Inflater inflate = new Inflater();
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuff1);
                }
                inflate.inflate(outPutInf);
            }
        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        }
        for (int i = 0; i < byteArray.length; i++) {
            assertTrue(
                    "Final decompressed data does not equal the original data",
                    byteArray[i] == outPutInf[i]);
        }
        assertEquals(
                "final decompressed data contained more bytes than original - inflateB",
                0, outPutInf[byteArray.length]);
        // testing for an empty input array
        byte outPutBuf[] = new byte[500];
        byte emptyArray[] = new byte[11];
        int x = 0;
        Deflater defEmpty = new Deflater(3);
        defEmpty.setInput(emptyArray);
        while (!(defEmpty.needsInput())) {
            x += defEmpty.deflate(outPutBuf, x, outPutBuf.length - x);
        }
        defEmpty.finish();
        while (!(defEmpty.finished())) {
            x += defEmpty.deflate(outPutBuf, x, outPutBuf.length - x);
        }
        assertTrue(
                "the total number of byte from deflate did not equal getTotalOut - inflate(byte)",
                x == defEmpty.getTotalOut());
        assertTrue(
                "the number of input byte from the array did not correspond with getTotalIn - inflate(byte)",
                defEmpty.getTotalIn() == emptyArray.length);
        Inflater infEmpty = new Inflater();
        try {
            while (!(infEmpty.finished())) {
                if (infEmpty.needsInput()) {
                    infEmpty.setInput(outPutBuf);
                }
                infEmpty.inflate(outPutInf);
            }
        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        }
        for (int i = 0; i < emptyArray.length; i++) {
            assertTrue(
                    "Final decompressed data does not equal the original data",
                    emptyArray[i] == outPutInf[i]);
            assertEquals("Final decompressed data does not equal zero", 0,
                    outPutInf[i]);
        }
        assertEquals(
                "Final decompressed data contains more element than original data",
                0, outPutInf[emptyArray.length]);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "inflate",
        args = {byte[].class}
    )
    public void test_inflate$B1() {
        byte codedData[] = {
                120, -38, 75, -54, 73, -52, 80, 40, 46, 41, -54, -52, 75, 87,
                72, -50, -49, 43, 73, -52, -52, 43, 86, 72, 2, 10, 34, 99,
                -123, -60, -68, 20, -80, 32, 0, -101, -69, 17, 84};
        String codedString = "blah string contains blahblahblahblah and blah";

        Inflater infl1 = new Inflater();
        Inflater infl2 = new Inflater();

        byte[] result = new byte[100];
        int decLen = 0;

        infl1.setInput(codedData, 0, codedData.length);
        try {
            decLen = infl1.inflate(result);
        } catch (DataFormatException e) {
            fail("Unexpected DataFormatException");
        }

        infl1.end();
        assertEquals(codedString, new String(result, 0, decLen));
        codedData[5] = 0;

        infl2.setInput(codedData, 0, codedData.length);
        try {
            decLen = infl2.inflate(result);
            fail("Expected DataFormatException");
        } catch (DataFormatException e) {
            // expected
        }

        infl2.end();
    }

    /**
     * @tests java.util.zip.Inflater#Inflater()
     */
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Inflater",
            args = {}
    )
    public void test_Constructor() {
        // test method of java.util.zip.inflater.Inflater()
        Inflater inflate = new Inflater();
        assertNotNull("failed to create the instance of inflater",
                        inflate);
    }

    /**
     * @tests java.util.zip.Inflater#inflate(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "DataFormatException checking missed.",
        method = "inflate",
        args = {byte[].class, int.class, int.class}
    )
    public void test_inflate$BII() {
        // test method of java.util.zip.inflater.inflate(byte,int,int)

        byte byteArray[] = {1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5'};
        byte outPutInf[] = new byte[100];
        int y = 0;
        Inflater inflate = new Inflater();
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuff1);
                }
                y += inflate.inflate(outPutInf, y, outPutInf.length - y);
            }
        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        }
        for (int i = 0; i < byteArray.length; i++) {
            assertTrue(
                    "Final decompressed data does not equal the original data",
                    byteArray[i] == outPutInf[i]);
        }
        assertEquals(
                "final decompressed data contained more bytes than original - inflateB",
                0, outPutInf[byteArray.length]);

        // test boundary checks
        inflate.reset();
        int r = 0;
        int offSet = 0;
        int lengthError = 101;
        try {
            if (inflate.needsInput()) {
                inflate.setInput(outPutBuff1);
            }
            inflate.inflate(outPutInf, offSet, lengthError);

        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 1;
        }
        assertEquals("out of bounds error did not get caught", 1, r);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "inflate",
        args = {byte[].class, int.class, int.class}
    )
    public void test_inflate$BII1() {
        byte codedData[] = {
                120, -38, 75, -54, 73, -52, 80, 40, 46, 41, -54, -52, 75, 87,
                72, -50, -49, 43, 73, -52, -52, 43, 86, 72, 2, 10, 34, 99,
                -123, -60, -68, 20, -80, 32, 0, -101, -69, 17, 84};
        String codedString = "blah string";

        Inflater infl1 = new Inflater();
        Inflater infl2 = new Inflater();

        byte[] result = new byte[100];
        int decLen = 0;

        infl1.setInput(codedData, 0, codedData.length);
        try {
            decLen = infl1.inflate(result, 10, 11);
        } catch (DataFormatException e) {
            fail("Unexpected DataFormatException");
        }

        infl1.end();
        assertEquals(codedString, new String(result, 10, decLen));
        codedData[5] = 0;

        infl2.setInput(codedData, 0, codedData.length);
        try {
            decLen = infl2.inflate(result, 10, 11);
            fail("Expected DataFormatException");
        } catch (DataFormatException e) {
            // expected
        }

        infl2.end();
    }

    /**
     * @tests java.util.zip.Inflater#Inflater(boolean)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Inflater",
        args = {boolean.class}
    )
    public void test_ConstructorZ() {
        // test method of java.util.zip.inflater.Inflater(boolean)
        // note does not throw exception if deflater has a header, but inflater
        // doesn't or vice versa.
        byte byteArray[] = {1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5'};
        Inflater inflate = new Inflater(true);
        assertNotNull("failed to create the instance of inflater", inflate);
        byte outPutInf[] = new byte[500];
        int r = 0;
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuff1);
                }

                inflate.inflate(outPutInf);
            }
            for (int i = 0; i < byteArray.length; i++) {
                assertEquals(
                        "the output array from inflate should contain 0 because the header of inflate and deflate did not match, but this failed",
                        0, outPutBuff1[i]);
            }
        } catch (DataFormatException e) {
            r = 1;
        }
        assertEquals(
                "Error: exception should be thrown because of header inconsistency",
                1, r);

    }

    /**
     * @tests java.util.zip.Inflater#needsDictionary()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "needsDictionary",
        args = {}
    )
    public void test_needsDictionary() {
        // test method of java.util.zip.inflater.needsDictionary()
        // note: this flag is set after inflate is called
        byte outPutInf[] = new byte[500];

        // testing with dictionary set.
        Inflater inflateDiction = new Inflater();
        if (inflateDiction.needsInput()) {
            inflateDiction.setInput(outPutDiction);
        }
        try {
            assertEquals("should return 0 because needs dictionary", 0,
                    inflateDiction.inflate(outPutInf));
        } catch (DataFormatException e) {
            fail("Should not cause exception");
        }
        assertTrue(
                "method needsDictionary returned false when dictionary was used in deflater",
                inflateDiction.needsDictionary());

        // testing without dictionary
        Inflater inflate = new Inflater();
        try {
            inflate.setInput(outPutBuff1);
            inflate.inflate(outPutInf);
            assertFalse(
                    "method needsDictionary returned true when dictionary was not used in deflater",
                    inflate.needsDictionary());
        } catch (DataFormatException e) {
            fail("Input to inflate is invalid or corrupted - needsDictionary");
        }

        // Regression test for HARMONY-86
        Inflater inf = new Inflater();
        assertFalse(inf.needsDictionary());
        assertEquals(0, inf.getTotalIn());
        assertEquals(0, inf.getTotalOut());
        assertEquals(0, inf.getBytesRead());
        assertEquals(0, inf.getBytesWritten());
    }

    /**
     * @tests java.util.zip.Inflater#needsInput()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "needsInput",
        args = {}
    )
    public void test_needsInput() {
        // test method of java.util.zip.inflater.needsInput()
        Inflater inflate = new Inflater();
        assertTrue(
                "needsInput give the wrong boolean value as a result of no input buffer",
                inflate.needsInput());

        byte byteArray[] = {2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9};
        inflate.setInput(byteArray);
        assertFalse(
                "methodNeedsInput returned true when the input buffer is full",
                inflate.needsInput());

        inflate.reset();
        byte byteArrayEmpty[] = new byte[0];
        inflate.setInput(byteArrayEmpty);
        assertTrue(
                "needsInput give wrong boolean value as a result of an empty input buffer",
                inflate.needsInput());
    }

    /**
     * @tests java.util.zip.Inflater#reset()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() {
        // test method of java.util.zip.inflater.reset()
        byte byteArray[] = {1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5'};
        byte outPutInf[] = new byte[100];
        int y = 0;
        Inflater inflate = new Inflater();
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuff1);
                }
                y += inflate.inflate(outPutInf, y, outPutInf.length - y);
            }
        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        }
        for (int i = 0; i < byteArray.length; i++) {
            assertTrue(
                    "Final decompressed data does not equal the original data",
                    byteArray[i] == outPutInf[i]);
        }
        assertEquals(
                "final decompressed data contained more bytes than original - reset",
                0, outPutInf[byteArray.length]);

        // testing that resetting the inflater will also return the correct
        // decompressed data

        inflate.reset();
        try {
            while (!(inflate.finished())) {
                if (inflate.needsInput()) {
                    inflate.setInput(outPutBuff1);
                }
                inflate.inflate(outPutInf);
            }
        } catch (DataFormatException e) {
            fail("Invalid input to be decompressed");
        }
        for (int i = 0; i < byteArray.length; i++) {
            assertTrue(
                    "Final decompressed data does not equal the original data",
                    byteArray[i] == outPutInf[i]);
        }
        assertEquals(
                "final decompressed data contained more bytes than original - reset",
                0, outPutInf[byteArray.length]);

    }


    /**
     * @tests java.util.zip.Inflater#setInput(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setInput",
        args = {byte[].class}
    )
    public void test_setInput$B() {
        // test method of java.util.zip.inflater.setInput(byte)
        byte byteArray[] = {2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9};
        Inflater inflate = new Inflater();
        inflate.setInput(byteArray);
        assertTrue("setInputB did not deliver any byte to the input buffer",
                inflate.getRemaining() != 0);
    }

    /**
     * @tests java.util.zip.Inflater#setInput(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setInput",
        args = {byte[].class, int.class, int.class}
    )
    public void test_setInput$BII() {
        // test method of java.util.zip.inflater.setInput(byte,int,int)
        byte byteArray[] = {2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9};
        int offSet = 6;
        int length = 6;
        Inflater inflate = new Inflater();
        inflate.setInput(byteArray, offSet, length);
        assertTrue(
                "setInputBII did not deliver the right number of bytes to the input buffer",
                inflate.getRemaining() == length);
        // boundary check
        inflate.reset();
        int r = 0;
        try {
            inflate.setInput(byteArray, 100, 100);
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 1;
        }
        assertEquals("boundary check is not present for setInput", 1, r);
    }

    @Override
    protected void setUp() {
        try {
            java.io.InputStream infile = Support_Resources
                    .getStream("hyts_compressD.txt");
            BufferedInputStream inflatIP = new BufferedInputStream(infile);
            inflatIP.read(outPutBuff1, 0, outPutBuff1.length);
            inflatIP.close();

            java.io.InputStream infile2 = Support_Resources
                    .getStream("hyts_compDiction.txt");
            BufferedInputStream inflatIP2 = new BufferedInputStream(infile2);
            inflatIP2.read(outPutDiction, 0, outPutDiction.length);
            inflatIP2.close();

        } catch (FileNotFoundException e) {
            fail("input file to test InflaterInputStream constructor is not found");
        } catch (ZipException e) {
            fail("read() threw an zip exception while testing constructor");
        } catch (IOException e) {
            fail("read() threw an exception while testing constructor");
        }
    }

    @Override
    protected void tearDown() {
    }

    /**
     * @tests java.util.zip.Deflater#getBytesRead()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBytesRead",
        args = {}
    )
    public void test_getBytesRead() throws DataFormatException,
            UnsupportedEncodingException {
        // Regression test for HARMONY-158
        Deflater def = new Deflater();
        Inflater inf = new Inflater();
        assertEquals(0, def.getTotalIn());
        assertEquals(0, def.getTotalOut());
        assertEquals(0, def.getBytesRead());
        // Encode a String into bytes
        String inputString = "blahblahblah??";
        byte[] input = inputString.getBytes("UTF-8");

        // Compress the bytes
        byte[] output = new byte[100];
        def.setInput(input);
        def.finish();
        def.deflate(output);
        inf.setInput(output);
        int compressedDataLength = inf.inflate(input);
        assertEquals(16, inf.getTotalIn());
        assertEquals(compressedDataLength, inf.getTotalOut());
        assertEquals(16, inf.getBytesRead());
    }

    /**
     * @tests java.util.zip.Deflater#getBytesRead()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBytesWritten",
        args = {}
    )
    public void test_getBytesWritten() throws DataFormatException,
            UnsupportedEncodingException {
        // Regression test for HARMONY-158
        Deflater def = new Deflater();
        Inflater inf = new Inflater();
        assertEquals(0, def.getTotalIn());
        assertEquals(0, def.getTotalOut());
        assertEquals(0, def.getBytesWritten());
        // Encode a String into bytes
        String inputString = "blahblahblah??";
        byte[] input = inputString.getBytes("UTF-8");

        // Compress the bytes
        byte[] output = new byte[100];
        def.setInput(input);
        def.finish();
        def.deflate(output);
        inf.setInput(output);
        int compressedDataLength = inf.inflate(input);
        assertEquals(16, inf.getTotalIn());
        assertEquals(compressedDataLength, inf.getTotalOut());
        assertEquals(14, inf.getBytesWritten());
    }

    /**
     * @tests java.util.zip.Deflater#inflate(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test",
        method = "inflate",
        args = {byte[].class, int.class, int.class}
    )
    public void testInflate() throws Exception {
        // Regression for HARMONY-81
        Inflater inf = new Inflater();
        int res = inf.inflate(new byte[0], 0, 0);

        assertEquals(0, res);

        // Regression for HARMONY-2508
        Inflater inflater = new Inflater();
        byte[] b = new byte[1024];
        assertEquals(0, inflater.inflate(b));
        inflater.end();

        // Regression for HARMONY-2510
        inflater = new Inflater();
        byte[] input = new byte[] {-1};
        inflater.setInput(input);
        try {
            inflater.inflate(b);
            fail("should throw DataFormateException");
        } catch (DataFormatException e) {
            // expected
        }
    }

    /**
     * @tests java.util.zip.Deflater#inflate(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not be checked. Should be tested in DX test package.",
        method = "finalize",
        args = {}
    )
    public void testFinalize() throws Exception {
    }

    /**
     * @tests java.util.zip.Inflater#setDictionary(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks setDictionary. Can be splitted for 2 separate tests for Deflater & Inflater. Can be used as a base for setDictionary with additional params.",
        method = "setDictionary",
        args = {byte[].class}
    )
    public void testsetDictionary$B() throws Exception {
        int i = 0;
        String inputString = "blah string contains blahblahblahblah and blah";
        String dictionary1 = "blah";
        String dictionary2 = "1234";

        byte[] outputNo = new byte[100];
        byte[] output1 = new byte[100];
        byte[] output2 = new byte[100];
        Deflater defDictNo = new Deflater(9);
        Deflater defDict1 = new Deflater(9);
        Deflater defDict2 = new Deflater(9);

        defDict1.setDictionary(dictionary1.getBytes());
        defDict2.setDictionary(dictionary2.getBytes());

        defDictNo.setInput(inputString.getBytes());
        defDict1.setInput(inputString.getBytes());
        defDict2.setInput(inputString.getBytes());

        defDictNo.finish();
        defDict1.finish();
        defDict2.finish();

        int dataLenNo = defDictNo.deflate(outputNo);
        int dataLen1 = defDict1.deflate(output1);
        int dataLen2 = defDict2.deflate(output2);

        boolean passNo1 = false;
        boolean passNo2 = false;
        boolean pass12 = false;

        for (i = 0; i < (dataLenNo < dataLen1 ? dataLenNo : dataLen1); i++) {
            if (outputNo[i] != output1[i]) {
                passNo1 = true;
                break;
            }
        }
        for (i = 0; i < (dataLenNo < dataLen1 ? dataLenNo : dataLen2); i++) {
            if (outputNo[i] != output2[i]) {
                passNo2 = true;
                break;
            }
        }
        for (i = 0; i < (dataLen1 < dataLen2 ? dataLen1 : dataLen2); i++) {
            if (output1[i] != output2[i]) {
                pass12 = true;
                break;
            }
        }

        assertTrue(
                "Compressed data the same for stream with dictionary and without it.",
                passNo1);
        assertTrue(
                "Compressed data the same for stream with dictionary and without it.",
                passNo2);
        assertTrue(
                "Compressed data the same for stream with different dictionaries.",
                pass12);

        Inflater inflNo = new Inflater();
        Inflater infl1 = new Inflater();
        Inflater infl2 = new Inflater();

        byte[] result = new byte[100];
        int decLen;

        inflNo.setInput(outputNo, 0, dataLenNo);
        decLen = inflNo.inflate(result);

        assertFalse(inflNo.needsDictionary());
        inflNo.end();
        assertEquals(inputString, new String(result, 0, decLen));

        infl1.setInput(output1, 0, dataLen1);
        decLen = infl1.inflate(result);

        assertTrue(infl1.needsDictionary());
        infl1.setDictionary(dictionary1.getBytes());
        decLen = infl1.inflate(result);
        infl1.end();
        assertEquals(inputString, new String(result, 0, decLen));

        infl2.setInput(output2, 0, dataLen2);
        decLen = infl2.inflate(result);

        assertTrue(infl2.needsDictionary());
        infl2.setDictionary(dictionary2.getBytes());
        decLen = infl2.inflate(result);
        infl2.end();
        assertEquals(inputString, new String(result, 0, decLen));


        inflNo = new Inflater();
        infl1 = new Inflater();
        inflNo.setInput(outputNo, 0, dataLenNo);
        try {
            infl1.setDictionary(dictionary1.getBytes());
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ee) {
            // expected.
        }
        inflNo.end();

        infl1.setInput(output1, 0, dataLen1);
        decLen = infl1.inflate(result);

        assertTrue(infl1.needsDictionary());
        try {
            infl1.setDictionary(dictionary2.getBytes());
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ee) {
            // expected.
        }
        infl1.end();
    }

    /**
     * @tests java.util.zip.Inflater#setDictionary(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks setDictionary. Can be splitted for 2 separate tests for Deflater & Inflater. Can be used as a base for setDictionary with additional params.",
        method = "setDictionary",
        args = {byte[].class, int.class, int.class}
    )
    public void testsetDictionary$BII() throws Exception {
        int i = 0;
        String inputString = "blah string contains blahblahblahblah and blah";
        String dictionary1 = "blah";
        String dictionary2 = "blahblahblah";

        byte[] output1 = new byte[100];
        byte[] output2 = new byte[100];
        byte[] output3 = new byte[100];

        Deflater defDict1 = new Deflater(9);
        Deflater defDict2 = new Deflater(9);
        Deflater defDict3 = new Deflater(9);

        defDict1.setDictionary(dictionary1.getBytes());
        defDict2.setDictionary(dictionary2.getBytes());
        defDict3.setDictionary(dictionary2.getBytes(), 4, 4);

        defDict1.setInput(inputString.getBytes());
        defDict2.setInput(inputString.getBytes());
        defDict3.setInput(inputString.getBytes());

        defDict1.finish();
        defDict2.finish();
        defDict3.finish();

        int dataLen1 = defDict1.deflate(output1);
        int dataLen2 = defDict2.deflate(output2);
        int dataLen3 = defDict3.deflate(output3);

        boolean pass12 = false;
        boolean pass23 = false;
        boolean pass13 = true;

        for (i = 0; i < (dataLen1 < dataLen2 ? dataLen1 : dataLen2); i++) {
            if (output1[i] != output2[i]) {
                pass12 = true;
                break;
            }
        }
        for (i = 0; i < (dataLen2 < dataLen3 ? dataLen2 : dataLen3); i++) {
            if (output2[i] != output3[i]) {
                pass23 = true;
                break;
            }
        }
        for (i = 0; i < (dataLen1 < dataLen3 ? dataLen1 : dataLen3); i++) {
            if (output1[i] != output3[i]) {
                pass13 = false;
                break;
            }
        }

        assertTrue(
                "Compressed data the same for stream with different dictionaries.",
                pass12);
        assertTrue(
                "Compressed data the same for stream with different dictionaries.",
                pass23);
        assertTrue(
                "Compressed data the differs for stream with the same dictionaries.",
                pass13);

        Inflater infl1 = new Inflater();
        Inflater infl2 = new Inflater();
        Inflater infl3 = new Inflater();

        byte[] result = new byte[100];
        int decLen;

        infl1.setInput(output1, 0, dataLen1);
        decLen = infl1.inflate(result);

        assertTrue(infl1.needsDictionary());
        infl1.setDictionary(dictionary2.getBytes(), 4, 4);
        decLen = infl1.inflate(result);
        infl1.end();
        assertEquals(inputString, new String(result, 0, decLen));

        infl2.setInput(output2, 0, dataLen2);
        decLen = infl2.inflate(result);

        assertTrue(infl2.needsDictionary());
        try {
            infl2.setDictionary(dictionary1.getBytes());
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ee) {
            // expected
        }
        infl2.end();

        infl3.setInput(output3, 0, dataLen3);
        decLen = infl3.inflate(result);

        assertTrue(infl3.needsDictionary());
        infl3.setDictionary(dictionary1.getBytes());
        decLen = infl3.inflate(result);
        infl3.end();
        assertEquals(inputString, new String(result, 0, decLen));

    }
}

/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package tests.api.java.nio.charset;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * TODO type def
 */
@TestTargetClass(java.nio.charset.CharsetEncoder.class)
public class UTF16LECharsetEncoderTest extends AbstractCharsetEncoderTestCase {

    // charset for utf-16le
    private static final Charset CS = Charset.forName("utf-16le");

    /*
     * @see CharsetEncoderTest#setUp()
     */
    protected void setUp() throws Exception {
        cs = CS;
        specifiedReplacement = new byte[] { -3, -1 };

        unibytes = new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0,
                114, 0 };

        // unibytesWithRep = new byte[] {(byte)0xfd, (byte)0xff, 32, 0, 98, 0,
        // 117, 0, 102, 0, 102, 0, 101, 0, 114 ,0};

        super.setUp();
    }

    /*
     * @see CharsetEncoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Empty constructor test.",
        method = "CharsetEncoder",
        args = {java.nio.charset.Charset.class, float.class, float.class}
    )
    public void testCharsetEncoderCharsetfloatfloat() {
        // this constructor is invalid for UTF16LE CharsetEncoder
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {char.class}
    )
    public void testCanEncodechar() throws CharacterCodingException {
        // normal case for utfCS
        assertTrue(encoder.canEncode('\u0077'));
        assertTrue(encoder.canEncode('\uc2a3'));

        // for non-mapped char
        assertTrue(encoder.canEncode('\uc2c0'));
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    public void testCanEncodeCharSequence() {
        // normal case for utfCS
        assertTrue(encoder.canEncode("\u0077"));
        assertTrue(encoder.canEncode("\uc2a3"));
        assertTrue(encoder.canEncode(""));

        // for non-mapped char
        assertTrue(encoder.canEncode("\uc2c0"));

        // surrogate char for unicode
        // 1st byte: d800-dbff
        // 2nd byte: dc00-dfff
        // valid surrogate pair
        assertTrue(encoder.canEncode("\ud800\udc00"));
        // invalid surrogate pair
        assertFalse(encoder.canEncode("\ud800\udb00"));
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test. IllegalStateException checking missed.",
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    public void testCanEncodeICUBug() {
        assertFalse(encoder.canEncode("\ud800"));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "averageBytesPerChar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "maxBytesPerChar",
            args = {}
        )
    })
    public void testSpecificDefaultValue() {
        assertEquals(2, encoder.averageBytesPerChar(), 0.001);
        assertEquals(2, encoder.maxBytesPerChar(), 0.001);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLegalReplacement",
        args = {byte[].class}
    )
    public void testIsLegalReplacementEmptyArray() {
        assertTrue(encoder.isLegalReplacement(new byte[0]));
    }

    CharBuffer getMalformedCharBuffer() {
        return CharBuffer.wrap("\ud800 buffer");
    }

    CharBuffer getUnmapCharBuffer() {
        return null;
    }

    CharBuffer getExceptionCharBuffer() {
        return null;
    }

    protected byte[] getIllegalByteArray() {
        return new byte[] { 0x00 };
    }

    protected byte[] getLegalByteArray() {
        return new byte[] { (byte) 0xd8, 0x00 };
    }

}

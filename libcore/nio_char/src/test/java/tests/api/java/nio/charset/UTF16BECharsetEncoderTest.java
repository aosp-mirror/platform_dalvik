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
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestLevel;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * TODO type def
 */
@TestTargetClass(java.nio.charset.CharsetEncoder.class)
public class UTF16BECharsetEncoderTest extends CharsetEncoderTest {

    // charset for utf-16be
    private static final Charset CS = Charset.forName("utf-16be");

    /*
     * @see CharsetEncoderTest#setUp()
     */
    protected void setUp() throws Exception {
        cs = CS;
        specifiedReplacement = new byte[] { -1, -3 };
        unibytes = new byte[] { 0, 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101,
                0, 114 };

        // unibytesWithRep = new byte[] {(byte)0xff, (byte)0xfd,0, 32, 0, 98, 0,
        // 117, 0, 102, 0, 102, 0, 101, 0, 114};

        super.setUp();
    }

    /*
     * @see CharsetEncoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

@TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test.",
      targets = {
        @TestTarget(
          methodName = "CharsetEncoder",
          methodArgs = {java.nio.charset.Charset.class, float.class, float.class}
        )
    })
    public void testCharsetEncoderCharsetfloatfloat() {
        // this constructor is invalid for UTF16LE CharsetEncoder
    }

@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "IllegalStateException checking missed.",
      targets = {
        @TestTarget(
          methodName = "canEncode",
          methodArgs = {char.class}
        )
    })
    public void testCanEncodechar() throws CharacterCodingException {
        // normal case for utfCS
        assertTrue(encoder.canEncode('\u0077'));
        assertTrue(encoder.canEncode('\uc2a3'));

        // for non-mapped char
        assertTrue(encoder.canEncode('\uc2c0'));

    }

@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "IllegalStateException checking missed.",
      targets = {
        @TestTarget(
          methodName = "canEncode",
          methodArgs = {java.lang.CharSequence.class}
        )
    })
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

@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Regression test. IllegalStateException checking missed.",
      targets = {
        @TestTarget(
          methodName = "canEncode",
          methodArgs = {java.lang.CharSequence.class}
        )
    })
    public void testCanEncodeICUBug() {
        assertFalse(encoder.canEncode("\ud800"));
    }

@TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "maxBytesPerChar",
          methodArgs = {}
        )
    })
    public void testSpecificDefaultValue() {
        // ??? TODO NIO adapt to the actually used UTF16BE charset Encoder
        // assertEquals(2, encoder.averageBytesPerChar(), 0.001);
        assertEquals(2, encoder.maxBytesPerChar(), 0.001);
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

@TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isLegalReplacement",
          methodArgs = {byte[].class}
        )
    })
    public void testIsLegalReplacementEmptyArray() {
        assertTrue(encoder.isLegalReplacement(new byte[0]));
    }

    protected byte[] getIllegalByteArray() {
        // FIXME: different here
        // cannot replace by 0xd8d8, but RI can
        // return new byte[]{(byte)0xd8, (byte)0xd8};
        return new byte[] { 0 };
    }

    protected byte[] getLegalByteArray() {
        return new byte[] { (byte) 0x00, (byte) 0xd8 };
    }
}

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

package org.apache.harmony.nio_char.tests.java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;

import junit.framework.TestCase;

public class CharsetEncoderTest extends TestCase {

    /**
     * @tests java.nio.charset.CharsetEncoder.CharsetEncoder(
     *        java.nio.charset.Charset, float, float)
     */
    public void test_ConstructorLjava_nio_charset_CharsetFF() {
        // Regression for HARMONY-141
        try {
            Charset cs = Charset.forName("UTF-8"); //$NON-NLS-1$
            new MockCharsetEncoderForHarmony141(cs, 1.1f, 1);
            fail("Assert 0: Should throw IllegalArgumentException."); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            Charset cs = Charset.forName("ISO8859-1"); //$NON-NLS-1$
            new MockCharsetEncoderForHarmony141(cs, 1.1f, 1,
                    new byte[] { 0x1a });
            fail("Assert 1: Should throw IllegalArgumentException."); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.charset.CharsetEncoder.CharsetEncoder(
     *        java.nio.charset.Charset, float, float)
     */
    public void test_ConstructorLjava_nio_charset_CharsetNull() {
        // Regression for HARMONY-491
        CharsetEncoder ech = new MockCharsetEncoderForHarmony491(null, 1, 1);
        assertNull(ech.charset());
    }

    /**
     * Helper for constructor tests
     */

    public static class MockCharsetEncoderForHarmony141 extends CharsetEncoder {

        protected MockCharsetEncoderForHarmony141(Charset cs,
                float averageBytesPerChar, float maxBytesPerChar) {
            super(cs, averageBytesPerChar, maxBytesPerChar);
        }

        public MockCharsetEncoderForHarmony141(Charset cs,
                float averageBytesPerChar, float maxBytesPerChar,
                byte[] replacement) {
            super(cs, averageBytesPerChar, maxBytesPerChar, replacement);
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            return null;
        }
    }

    public static class MockCharsetEncoderForHarmony491 extends CharsetEncoder {

        public MockCharsetEncoderForHarmony491(Charset arg0, float arg1,
                float arg2) {
            super(arg0, arg1, arg2);
        }

        protected CoderResult encodeLoop(CharBuffer arg0, ByteBuffer arg1) {
            return null;
        }

        public boolean isLegalReplacement(byte[] arg0) {
            return true;
        }
    }

    /*
     * Test malfunction encode(CharBuffer)
     */
    public void test_EncodeLjava_nio_CharBuffer() throws Exception {
        MockMalfunctionCharset cs = new MockMalfunctionCharset("mock", null);
        try {
            cs.encode(CharBuffer.wrap("AB"));
            fail("should throw CoderMalfunctionError");// NON-NLS-1$
        } catch (CoderMalfunctionError e) {
            // expected
        }
    }

    /*
     * Mock charset class with malfunction decode & encode.
     */
    static final class MockMalfunctionCharset extends Charset {

        public MockMalfunctionCharset(String canonicalName, String[] aliases) {
            super(canonicalName, aliases);
        }

        public boolean contains(Charset cs) {
            return false;
        }

        public CharsetDecoder newDecoder() {
            return Charset.forName("UTF-8").newDecoder();
        }

        public CharsetEncoder newEncoder() {
            return new MockMalfunctionEncoder(this);
        }
    }

    /*
     * Mock encoder. encodeLoop always throws unexpected exception.
     */
    static class MockMalfunctionEncoder extends java.nio.charset.CharsetEncoder {

        public MockMalfunctionEncoder(Charset cs) {
            super(cs, 1, 3, new byte[] { (byte) '?' });
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            throw new BufferOverflowException();
        }
    }

    /*
     * Test reserve bytes encode(CharBuffer,ByteBuffer,boolean)
     */
    public void test_EncodeLjava_nio_CharBufferLjava_nio_ByteBufferB() {
        CharsetEncoder encoder = Charset.forName("utf-8").newEncoder();
        CharBuffer in1 = CharBuffer.wrap("\ud800");
        CharBuffer in2 = CharBuffer.wrap("\udc00");
        ByteBuffer out = ByteBuffer.allocate(4);
        encoder.reset();
        CoderResult result = encoder.encode(in1, out, false);
        assertEquals(4, out.remaining());
        assertTrue(result.isUnderflow());
        result = encoder.encode(in2, out, true);
        assertEquals(4, out.remaining());
        assertTrue(result.isMalformed());
    }
}

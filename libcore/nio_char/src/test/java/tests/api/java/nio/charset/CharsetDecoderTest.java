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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
@TestTargetClass(CharsetDecoder.class)
/**
 * API unit test for java.nio.charset.CharsetDecoder
 */
public class CharsetDecoderTest extends AbstractCharsetDecoderTestCase {

    protected static final int MAX_BYTES = 3;

    protected static final double AVER_BYTES = 0.5;


    protected void setUp() throws Exception {
        cs = new CharsetEncoderTest.MockCharset("mock", new String[0]);
        unibytes = new byte[] { 32, 98, 117, 102, 102, 101, 114 };
        super.setUp();
    }


    /*
     * test constructor
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CharsetDecoder",
            args = {java.nio.charset.Charset.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "charset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "averageCharsPerByte",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "maxCharsPerByte",
            args = {}
        )
    })
    public void testCharsetDecoder() {
        // default value
        decoder = new MockCharsetDecoder(cs, (float) AVER_BYTES, MAX_BYTES);

        // normal case
        CharsetDecoder ec = new MockCharsetDecoder(cs, 1, MAX_BYTES);
        assertSame(ec.charset(), cs);
        assertEquals(1.0, ec.averageCharsPerByte(), 0.0);
        assertTrue(ec.maxCharsPerByte() == MAX_BYTES);

        /*
         * ------------------------ Exceptional cases -------------------------
         */
        // Normal case: null charset
        ec = new MockCharsetDecoder(null, 1, MAX_BYTES);
        assertNull(ec.charset());
        assertEquals(1.0, ec.averageCharsPerByte(), 0.0);
        assertTrue(ec.maxCharsPerByte() == MAX_BYTES);

        ec = new MockCharsetDecoder(new CharsetEncoderTest.MockCharset("mock",
                new String[0]), 1, MAX_BYTES);

                // Commented out since the comment is wrong since MAX_BYTES > 1
        // // OK: average length less than max length
        // ec = new MockCharsetDecoder(cs, MAX_BYTES, 1);
        // assertTrue(ec.averageCharsPerByte() == MAX_BYTES);
        // assertTrue(ec.maxCharsPerByte() == 1);

        // Illegal Argument: zero length
        try {
            ec = new MockCharsetDecoder(cs, 0, MAX_BYTES);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetDecoder(cs, 1, 0);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // Illegal Argument: negative length
        try {
            ec = new MockCharsetDecoder(cs, -1, MAX_BYTES);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetDecoder(cs, 1, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implFlush",
        args = {java.nio.CharBuffer.class}
    )
    public void testImplFlush() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        assertEquals(CoderResult.UNDERFLOW, ((MockCharsetDecoder) decoder)
                .pubImplFlush(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implOnMalformedInput",
        args = {java.nio.charset.CodingErrorAction.class}
    )
    public void testImplOnMalformedInput() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        assertEquals(CoderResult.UNDERFLOW, ((MockCharsetDecoder) decoder)
                .pubImplFlush(null));

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implOnUnmappableCharacter",
        args = {java.nio.charset.CodingErrorAction.class}
    )
    public void testImplOnUnmappableCharacter() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        ((MockCharsetDecoder) decoder).pubImplOnUnmappableCharacter(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implReplaceWith",
        args = {java.lang.String.class}
    )
    public void testImplReplaceWith() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        ((MockCharsetDecoder) decoder).pubImplReplaceWith(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implReset",
        args = {}
    )
    public void testImplReset() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        ((MockCharsetDecoder) decoder).pubImplReset();
    }
    
    
    boolean deCodeLoopCalled = false;
    
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "decodeLoop",
        args = { ByteBuffer.class, CharBuffer.class}
    )
    public void testEncodeLoop() throws Exception {
        try {
            decoder = new MockCharsetDecoder(Charset.forName("US-ASCII"), 1,
                    MAX_BYTES) {
                @Override
                protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                    deCodeLoopCalled = true;
                    return super.decodeLoop(in, out);
                }
            };
            decoder.decode(ByteBuffer.wrap(new byte[]{ 'a','b','c'}));
        } catch (UnsupportedCharsetException e) {
            fail("us-ascii not supported");
        }
        assertTrue(deCodeLoopCalled);
    }

    /*
     * mock decoder
     */
    public static class MockCharsetDecoder extends CharsetDecoder {
        public MockCharsetDecoder(Charset cs, float ave, float max) {
            super(cs, ave, max);
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            int inPosition = in.position();
            byte[] input = new byte[in.remaining()];
            in.get(input);
            String result = new String(input);
            if (result.startsWith("malform")) {
                // reset the cursor to the error position
                in.position(inPosition + "malform".length());
                // set the error length
                return CoderResult.malformedForLength("malform".length());
            } else if (result.startsWith("unmap")) {
                // reset the cursor to the error position
                in.position(inPosition);
                // set the error length
                return CoderResult.unmappableForLength("unmap".length());
            } else if (result.startsWith("runtime")) {
                // reset the cursor to the error position
                in.position(0);
                // set the error length
                throw new RuntimeException("runtime");
            }
            int inLeft = input.length;
            int outLeft = out.remaining();
            CoderResult r = CoderResult.UNDERFLOW;
            int length = inLeft;
            if (outLeft < inLeft) {
                r = CoderResult.OVERFLOW;
                length = outLeft;
                in.position(inPosition + outLeft);
            }
            for (int i = 0; i < length; i++) {
                out.put((char) input[i]);
            }
            return r;
        }

        protected CoderResult implFlush(CharBuffer out) {
            CoderResult result = super.implFlush(out);
            if (out.remaining() >= 5) {
                // TODO
                // out.put("flush");
                result = CoderResult.UNDERFLOW;
            } else {
                // out.put("flush", 0, out.remaining());
                result = CoderResult.OVERFLOW;
            }
            return result;
        }

        public CoderResult pubImplFlush(CharBuffer out) {
            return super.implFlush(out);
        }

        public void pubImplOnMalformedInput(CodingErrorAction newAction) {
            super.implOnMalformedInput(newAction);
        }

        public void pubImplOnUnmappableCharacter(CodingErrorAction newAction) {
            super.implOnUnmappableCharacter(newAction);
        }

        public void pubImplReplaceWith(String newReplacement) {
            super.implReplaceWith(newReplacement);
        }

        public void pubImplReset() {
            super.implReset();
        }
    }

}

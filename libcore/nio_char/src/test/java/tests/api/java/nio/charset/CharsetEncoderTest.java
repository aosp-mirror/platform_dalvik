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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
@TestTargetClass(CharsetEncoder.class)
/**
 * API unit test for java.nio.charset.CharsetEncoder
 */
public class CharsetEncoderTest extends AbstractCharsetEncoderTestCase {

    static final int MAX_BYTES = 3;

    static final float AVER_BYTES = 0.5f;

    // default for Charset abstract class
    byte[] defaultReplacement = new byte[] { 63 };

    // specific for Charset implementation subclass
    byte[] specifiedReplacement = new byte[] { 26 };


    protected void setUp() throws Exception {
        cs = new MockCharset("CharsetEncoderTest_mock", new String[0]);
        unibytes = new byte[] { 32, 98, 117, 102, 102, 101, 114 };
        super.setUp();
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
        assertTrue(encoder.averageBytesPerChar() == AVER_BYTES);
        assertTrue(encoder.maxBytesPerChar() == MAX_BYTES);
    }
    
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implOnMalformedInput",
        args = {java.nio.charset.CodingErrorAction.class}
    )
    public void testImplOnMalformedInput() {
        encoder = new MockCharsetEncoder(cs, 1, 3);
        assertEquals(CoderResult.UNDERFLOW, ((MockCharsetEncoder) encoder)
                .pubImplFlush(null));

    }
    
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implOnUnmappableCharacter",
        args = {java.nio.charset.CodingErrorAction.class}
    )
    public void testImplOnUnmappableCharacter() {
        encoder = new MockCharsetEncoder(cs, 1, 3);
        ((MockCharsetEncoder) encoder).pubImplOnUnmappableCharacter(null);
    }    

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "onMalformedInput & onUnmappableCharacter requires check for IllegalArgumentException",
            method = "malformedInputAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "onMalformedInput & onUnmappableCharacter requires check for IllegalArgumentException",
            method = "unmappableCharacterAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "onMalformedInput & onUnmappableCharacter requires check for IllegalArgumentException",
            method = "onMalformedInput",
            args = {java.nio.charset.CodingErrorAction.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "onMalformedInput & onUnmappableCharacter requires check for IllegalArgumentException",
            method = "onUnmappableCharacter",
            args = {java.nio.charset.CodingErrorAction.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "onMalformedInput & onUnmappableCharacter requires check for IllegalArgumentException",
            method = "replacement",
            args = {}
        )
    })
    public void testDefaultValue() {
        assertEquals(CodingErrorAction.REPORT, encoder.malformedInputAction());
        assertEquals(CodingErrorAction.REPORT, encoder
                .unmappableCharacterAction());
        assertSame(encoder, encoder.onMalformedInput(CodingErrorAction.IGNORE));
        assertSame(encoder, encoder
                .onUnmappableCharacter(CodingErrorAction.IGNORE));
        if (encoder instanceof MockCharsetEncoder) {
            assertTrue(Arrays.equals(encoder.replacement(), defaultReplacement));
        } else {
            assertTrue(Arrays.equals(encoder.replacement(),
                    specifiedReplacement));
        }

    }
    
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implReset",
        args = {}
    )
    public void testImplReset() {
        encoder = new MockCharsetEncoder(cs, 1, 3);
        ((MockCharsetEncoder) encoder).pubImplReset();
    }

    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implFlush",
        args = {ByteBuffer.class}
    )
    public void testImplFlush() {
        encoder = new MockCharsetEncoder(cs, 1, 3);
        assertEquals(CoderResult.UNDERFLOW, ((MockCharsetEncoder) encoder)
                .pubImplFlush(null));
    }


    /*
     * Class under test for constructor CharsetEncoder(Charset, float, float)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "charset",
            args = {}
        ),
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
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "malformedInputAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unmappableCharacterAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CharsetEncoder",
            args = {java.nio.charset.Charset.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "replacement",
            args = {}
        )
    })
    public void testCharsetEncoderCharsetfloatfloat() {
        // default value
        encoder = new MockCharsetEncoder(cs, (float) AVER_BYTES, MAX_BYTES);
        assertSame(encoder.charset(), cs);
        assertTrue(encoder.averageBytesPerChar() == AVER_BYTES);
        assertTrue(encoder.maxBytesPerChar() == MAX_BYTES);
        assertEquals(CodingErrorAction.REPORT, encoder.malformedInputAction());
        assertEquals(CodingErrorAction.REPORT, encoder
                .unmappableCharacterAction());
        assertEquals(new String(encoder.replacement()), new String(
                defaultReplacement));
        assertSame(encoder, encoder.onMalformedInput(CodingErrorAction.IGNORE));
        assertSame(encoder, encoder
                .onUnmappableCharacter(CodingErrorAction.IGNORE));

        // normal case
        CharsetEncoder ec = new MockCharsetEncoder(cs, 1, MAX_BYTES);
        assertSame(ec.charset(), cs);
        assertEquals(1.0, ec.averageBytesPerChar(), 0);
        assertTrue(ec.maxBytesPerChar() == MAX_BYTES);

        /*
         * ------------------------ Exceptional cases -------------------------
         */
        // NullPointerException: null charset
        try {
            ec = new MockCharsetEncoder(null, 1, MAX_BYTES);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }

        ec = new MockCharsetEncoder(new MockCharset("mock", new String[0]), 1,
                MAX_BYTES);

                // Commented out since the comment is wrong since MAX_BYTES > 1
        // // OK: average length less than max length
        // ec = new MockCharsetEncoder(cs, MAX_BYTES, 1);
        // assertTrue(ec.averageBytesPerChar() == MAX_BYTES);
        // assertTrue(ec.maxBytesPerChar() == 1);

        // Illegal Argument: zero length
        try {
            ec = new MockCharsetEncoder(cs, 0, MAX_BYTES);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetEncoder(cs, 1, 0);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // Illegal Argument: negative length
        try {
            ec = new MockCharsetEncoder(cs, -1, MAX_BYTES);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetEncoder(cs, 1, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /*
     * Class under test for constructor CharsetEncoder(Charset, float, float,
     * byte[])
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CharsetEncoder",
            args = {java.nio.charset.Charset.class, float.class, float.class, byte[].class}
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
            method = "averageBytesPerChar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "maxBytesPerChar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "replacement",
            args = {}
        )
    })
    public void testCharsetEncoderCharsetfloatfloatbyteArray() {
        byte[] ba = getLegalByteArray();
        // normal case
        CharsetEncoder ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, ba);
        assertSame(ec.charset(), cs);
        assertEquals(1.0, ec.averageBytesPerChar(), 0.0);
        assertTrue(ec.maxBytesPerChar() == MAX_BYTES);
        assertSame(ba, ec.replacement());

        /*
         * ------------------------ Exceptional cases -------------------------
         */
        // NullPointerException: null charset
        try {
            ec = new MockCharsetEncoder(null, 1, MAX_BYTES, ba);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }

        // Illegal Argument: null byte array
        try {
            ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        // Illegal Argument: empty byte array
        try {
            ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, new byte[0]);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        // Illegal Argument: byte array is longer than max length
        try {
            ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, new byte[] { 1, 2,
                    MAX_BYTES, 4 });
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

                // Commented out since the comment is wrong since MAX_BYTES > 1
                // This test throws IllegalArgumentException on Harmony and RI
        // // OK: average length less than max length
        // ec = new MockCharsetEncoder(cs, MAX_BYTES, ba.length, ba);
        // assertTrue(ec.averageBytesPerChar() == MAX_BYTES);
        // assertTrue(ec.maxBytesPerChar() == ba.length);

        // Illegal Argument: zero length
        try {
            ec = new MockCharsetEncoder(cs, 0, MAX_BYTES, ba);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetEncoder(cs, 1, 0, ba);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // Illegal Argument: negative length
        try {
            ec = new MockCharsetEncoder(cs, -1, MAX_BYTES, ba);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetEncoder(cs, 1, -1, ba);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }



    /*
     * Class under test for Charset charset()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "",
        method = "CharsetEncoder",
        args = {java.nio.charset.Charset.class, float.class, float.class}
    )
    public void testCharset() {
        try {
            encoder = new MockCharsetEncoder(Charset.forName("gbk"), 1,
                    MAX_BYTES);
            // assertSame(encoder.charset(), Charset.forName("gbk"));
        } catch (UnsupportedCharsetException e) {
            // System.err
            //         .println("Don't support GBK encoding, ignore current test");
        }
    }


    boolean enCodeLoopCalled = false;
    
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "encodeLoop",
        args = { CharBuffer.class, ByteBuffer.class}
    )
    public void testEncodeLoop() throws Exception {
        try {
            encoder = new MockCharsetEncoder(Charset.forName("US-ASCII"), 1,
                    MAX_BYTES) {
                @Override
                protected CoderResult encodeLoop(CharBuffer arg0,
                        ByteBuffer arg1) {
                    enCodeLoopCalled = true;
                    return super.encodeLoop(arg0, arg1);
                }
            };
            encoder.encode(CharBuffer.wrap("hallo"));
        } catch (UnsupportedCharsetException e) {
            fail("us-ascii not supported");
        }
        assertTrue(enCodeLoopCalled);
    }
    
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implReplaceWith",
        args = { byte[].class}
    )
    public void testImplReplaceWith() {
        encoder = new MockCharsetEncoder(cs, 1, 3);
        ((MockCharsetEncoder) encoder).pubImplReplaceWith(null);
    }    


    
    protected byte[] getLegalByteArray() {
        return new byte[] { 'a' };
    }

    protected byte[] getIllegalByteArray() {
        return new byte[155];
    }

    /*
     * Mock subclass of CharsetEncoder For protected method test
     */
    public static class MockCharsetEncoder extends CharsetEncoder {

        boolean flushed = false;

        public boolean isFlushed() {
            boolean result = flushed;
            flushed = false;
            return result;
        }

        public boolean isLegalReplacement(byte[] ba) {
            if (ba.length == 155) {// specified magic number, return false
                return false;
            }
            return super.isLegalReplacement(ba);
        }

        public MockCharsetEncoder(Charset cs, float aver, float max) {
            super(cs, aver, max);
        }

        public MockCharsetEncoder(Charset cs, float aver, float max,
                byte[] replacement) {
            super(cs, aver, max, replacement);
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            int inPosition = in.position();
            char[] input = new char[in.remaining()];
            in.get(input);
            String result = new String(input);
            if (result.startsWith("malform")) {
                // reset the cursor to the error position
                in.position(inPosition);
                // in.position(0);
                // set the error length
                return CoderResult.malformedForLength("malform".length());
            } else if (result.startsWith("unmap")) {
                // reset the cursor to the error position
                in.position(inPosition);
                // in.position(0);
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
                out.put((byte) input[i]);
            }
            return r;
        }

        protected CoderResult implFlush(ByteBuffer out) {
            CoderResult result = super.implFlush(out);
            int length = 0;
            if (out.remaining() >= 5) {
                length = 5;
                result = CoderResult.UNDERFLOW;
                flushed = true;
                // for (int i = 0; i < length; i++) {
                // out.put((byte)'f');
                // }
            } else {
                length = out.remaining();
                result = CoderResult.OVERFLOW;
            }
            return result;
        }

        protected void implReplaceWith(byte[] ba) {
            assertSame(ba, replacement());
        }
        
        public void pubImplReplaceWith(byte[] newReplacement) {
            super.implReplaceWith(newReplacement);
        }
        
        public CoderResult pubImplFlush(ByteBuffer out) {
            return super.implFlush(out);
        }
        
        public void pubImplOnUnmappableCharacter(CodingErrorAction newAction) {
            super.implOnUnmappableCharacter(newAction);
        }
        
        public void pubImplReset() {
            super.implReset();
        }

    }

    /*
     * mock charset for test encoder initialization
     */
    public static class MockCharset extends Charset {
        protected MockCharset(String arg0, String[] arg1) {
            super(arg0, arg1);
        }

        public boolean contains(Charset arg0) {
            return false;
        }

        public CharsetDecoder newDecoder() {
            return new CharsetDecoderTest.MockCharsetDecoder(this,
                    (float) AVER_BYTES, MAX_BYTES);
        }

        public CharsetEncoder newEncoder() {
            return new MockCharsetEncoder(this, (float) AVER_BYTES, MAX_BYTES);
        }
    }

}

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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnmappableCharacterException;

/**
 * test case specific activity of iso-8859-1 charset encoder
 */
@TestTargetClass(java.nio.charset.CharsetEncoder.class)
public class ISOCharsetEncoderTest extends CharsetEncoderTest {

    // charset for iso-8859-1
    private static final Charset CS = Charset.forName("iso-8859-1");

    /*
     * @see CharsetEncoderTest#setUp()
     */
    protected void setUp() throws Exception {
        cs = CS;
        super.setUp();
    }

    /*
     * @see CharsetEncoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
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
        // normal case for isoCS
        assertTrue(encoder.canEncode("\u0077"));
        assertFalse(encoder.canEncode("\uc2a3"));
        assertFalse(encoder.canEncode("\ud800\udc00"));
        try {
            encoder.canEncode(null);
        } catch (NullPointerException e) {
        }
        assertTrue(encoder.canEncode(""));
    }

@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Regression test. IllegalStateException checking missed.",
      targets = {
        @TestTarget(
          methodName = "canEncode",
          methodArgs = {char.class}
        ), @TestTarget(
          methodName = "canEncode",
          methodArgs = {java.lang.CharSequence.class}
        )
    })
    public void testCanEncodeICUBug() {
        assertFalse(encoder.canEncode((char) '\ud800'));
        assertFalse(encoder.canEncode((String) "\ud800"));
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
        assertTrue(encoder.canEncode('\u0077'));
        assertFalse(encoder.canEncode('\uc2a3'));
    }

@TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "averageBytesPerChar",
          methodArgs = {}
        ), @TestTarget(
          methodName = "maxBytesPerChar",
          methodArgs = {}
        )
    })
    public void testSpecificDefaultValue() {
        assertEquals(1, encoder.averageBytesPerChar(), 0.001);
        assertEquals(1, encoder.maxBytesPerChar(), 0.001);
    }

    CharBuffer getMalformedCharBuffer() {
        return CharBuffer.wrap("\ud800 buffer");
    }

    CharBuffer getUnmapCharBuffer() {
        return CharBuffer.wrap("\ud800\udc00 buffer");
    }

    CharBuffer getExceptionCharBuffer() {
        return null;
    }

    protected byte[] getIllegalByteArray() {
        return null;
    }

@TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Checks also: flush & encode, but not covers exceptions.",
      targets = {
        @TestTarget(
          methodName = "onMalformedInput",
          methodArgs = {java.nio.charset.CodingErrorAction.class}
        ), @TestTarget(
          methodName = "onUnmappableCharacter",
          methodArgs = {java.nio.charset.CodingErrorAction.class}
        ), @TestTarget(
          methodName = "reset",
          methodArgs = {}
        )
    })
    public void testMultiStepEncode() throws CharacterCodingException {
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            encoder.encode(CharBuffer.wrap("\ud800\udc00"));
            fail("should unmappable");
        } catch (UnmappableCharacterException e) {
        }
        encoder.reset();
        ByteBuffer out = ByteBuffer.allocate(10);
        assertTrue(encoder.encode(CharBuffer.wrap("\ud800"), out, true)
                .isMalformed());
        encoder.flush(out);
        encoder.reset();
        out = ByteBuffer.allocate(10);
        assertSame(CoderResult.UNDERFLOW, encoder.encode(CharBuffer
                .wrap("\ud800"), out, false));
        assertTrue(encoder.encode(CharBuffer.wrap("\udc00"), out, true)
                .isMalformed());
    }
}

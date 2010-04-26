/*
 * Copyright (C) 2009 The Android Open Source Project
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

package tests.api.java.nio.charset;

import junit.framework.TestCase;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


/* See bug 1844104.
 * Checks for ICU encoder/decoder buffer corruption.
 */
@TestTargetClass(CharsetDecoder.class)
public class CharsetEncoderDecoderBufferTest extends TestCase {

    /* Checks for a buffer corruption that happens in ICU
     * (CharsetDecoderICU) when a decode operation
     * is done first with an out-buffer with hasArray()==true, and next with an out-buffer with
     * hasArray()==false. In that situation ICU may overwrite the first out-buffer.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "decode",
            args = {}
        )
    })
    public void testDecoderOutputBuffer() {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

        char[] cBuf = new char[10];
        CharBuffer out = CharBuffer.wrap(cBuf);
        assertTrue(out.hasArray());
        decoder.decode(ByteBuffer.wrap(new byte[]{(byte)'a', (byte)'b', (byte)'c', (byte)'d'}),
                       out, false);

        assertEquals("abcd", new String(cBuf, 0, 4));
        assertEquals(0, cBuf[4]);
        assertEquals(0, cBuf[5]);

        byte[] bBuf = new byte[10];
        out = ByteBuffer.wrap(bBuf).asCharBuffer();
        assertFalse(out.hasArray());
        decoder.decode(ByteBuffer.wrap(new byte[]{(byte)'x'}), out, true);

        assertEquals('x', bBuf[1]);
        assertEquals(0, bBuf[3]);

        // check if the first buffer was corrupted by the second decode
        assertEquals("abcd", new String(cBuf, 0, 4));
        assertEquals(0, cBuf[4]);
        assertEquals(0, cBuf[5]);
    }

    /* Checks for a buffer corruption that happens in ICU
     * (CharsetDecoderICU) when a decode operation
     * is done first with an in-buffer with hasArray()==true, and next with an in-buffer with
     * hasArray()==false. In that situation ICU may overwrite the array of the first in-buffer.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "decode",
            args = {}
        )
    })
    public void testDecoderInputBuffer() {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        CharBuffer out = CharBuffer.wrap(new char[10]);

        byte[] inArray = {(byte)'a', (byte)'b'};
        ByteBuffer inWithArray = ByteBuffer.wrap(inArray);
        assertTrue(inWithArray.hasArray());
        decoder.decode(inWithArray, out, false);
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);

        ByteBuffer inWithoutArray = ByteBuffer.allocateDirect(1);
        inWithoutArray.put(0, (byte)'x');
        assertFalse(inWithoutArray.hasArray());
        decoder.decode(inWithoutArray, out, true);

        // check whether the first buffer was corrupted by the second decode
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);
    }

    /* Checks for a buffer corruption that happens in ICU
     * (CharsetEncoderICU) when an encode operation
     * is done first with an out-buffer with hasArray()==true, and next with an out-buffer with
     * hasArray()==false. In that situation ICU may overwrite the first out-buffer.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "encode",
            args = {}
        )
    })
    public void testEncoderOutputBuffer() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();

        byte[] buffer = new byte[10];
        ByteBuffer out = ByteBuffer.wrap(buffer);

        assertTrue(out.hasArray());
        encoder.encode(CharBuffer.wrap("ab"), out, false);

        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals(0, buffer[2]);

        out = ByteBuffer.allocateDirect(10);
        assertFalse(out.hasArray());
        encoder.encode(CharBuffer.wrap("x"), out, true);

        // check whether the second decode corrupted the first buffer
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals(0, buffer[2]);
    }

    /* Checks for a buffer corruption that happens in ICU
     * (CharsetEncoderICU) when an encode operation
     * is done first with an in-buffer with hasArray()==true, and next with an in-buffer with
     * hasArray()==false. In that situation ICU may overwrite the array of the first in-buffer.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "encode",
            args = {}
        )
    })
    public void testEncoderInputBuffer() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer out = ByteBuffer.wrap(new byte[10]);

        char[] inArray = {'a', 'b'};
        CharBuffer inWithArray = CharBuffer.wrap(inArray);
        assertTrue(inWithArray.hasArray());
        encoder.encode(inWithArray, out, false);

        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);

        CharBuffer inWithoutArray = CharBuffer.wrap("x");
        assertFalse(inWithoutArray.hasArray());
        encoder.encode(inWithoutArray, out, true);

        // check whether the second decode corrupted the first buffer
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);
    }
}

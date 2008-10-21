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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * 
 */
public class UTF16CharsetDecoderTest extends CharsetDecoderTest {

    boolean bigEndian = true;

    protected void setUp() throws Exception {
        cs = Charset.forName("utf-16");
        unibytes = new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0,
                114, 0 };
        bom = "\ufeff";

        // unibytes = new byte[] {-1, -2, 0, 32, 0, 98, 0, 117, 0, 102, 0, 102,
        // 0, 101, 0, 114};
        super.setUp();
    }

    /*
     * @see CharsetDecoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    byte[] getUnibytes() {
        // FIXME: different here
        // if don't specified BOM
        // ICU default is LE
        // JDK default is BE

        // maybe start with 0xFEFF, which means big endian
        // 0xFFFE, which means little endian
        if (bigEndian) {
            return new byte[] { -1, -2, 32, 0, 98, 0, 117, 0, 102, 0, 102, 0,
                    101, 0, 114, 0 };
        } else {
            unibytes = new byte[] { 0, 32, 0, 98, 0, 117, 0, 102, 0, 102, 0,
                    101, 0, 114 };
            return new byte[] { -2, -1, 0, 32, 0, 98, 0, 117, 0, 102, 0, 102,
                    0, 101, 0, 114 };
        }
    }

    public void testMultiStepDecode() throws CharacterCodingException {
        if (!cs.name().equals("mock")) {
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            CharBuffer out = CharBuffer.allocate(10);
            assertTrue(decoder.decode(
                    ByteBuffer.wrap(new byte[] { -1, -2, 32, 0, 98 }), out,
                    true).isMalformed());

            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertSame(CoderResult.UNDERFLOW, decoder.decode(ByteBuffer
                    .wrap(new byte[] { -1, -2, 32, 0 }), out, false));
            assertTrue(decoder.decode(ByteBuffer.wrap(new byte[] { 98 }), out,
                    true).isMalformed());

            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertSame(CoderResult.UNDERFLOW, decoder.decode(ByteBuffer
                    .wrap(new byte[] { -1, -2, 32, 0, 98 }), out, false));
            assertFalse(decoder
                    .decode(ByteBuffer.wrap(new byte[] {}), out, true)
                    .isMalformed());

            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertFalse(decoder.decode(
                    ByteBuffer.wrap(new byte[] { -1, -2, 32, 0, 98, 0 }), out,
                    true).isError());

            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertSame(CoderResult.UNDERFLOW, decoder.decode(ByteBuffer
                    .wrap(new byte[] { -1, -2, 32, 0, 98 }), out, false));
            assertTrue(decoder.decode(ByteBuffer.wrap(new byte[] { 0 }), out,
                    true).isMalformed());

        }
    }

    public void testLittleEndian() throws CharacterCodingException,
            UnsupportedEncodingException {
        bigEndian = false;
        implTestDecodeByteBufferCharBufferboolean();
        decoder.reset();
        implTestDecodeByteBuffer();
        bigEndian = true;
    }

    // FIXME: give up this tests
    // public void testDefaultCharsPerByte() {
    // // assertEquals(1, decoder.averageCharsPerByte());
    // // assertEquals(1, decoder.maxCharsPerByte());
    // assertEquals(decoder.averageCharsPerByte(), 0.5, 0.001);
    // assertEquals(decoder.maxCharsPerByte(), 2, 0.001);
    // }

    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        return null;
    }

    ByteBuffer getMalformByteBuffer() throws UnsupportedEncodingException {
        return null;
        // FIXME: different here, RI can parse 0xd8d8
        // ByteBuffer buffer = ByteBuffer.allocate(100);
        // buffer.put((byte) -1);
        // buffer.put((byte) -2);
        // buffer.put((byte) 0xdc);
        // buffer.put((byte) 0xdc);
        // buffer.put(unibytes);
        // buffer.flip();
        // return buffer;
    }

    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
}

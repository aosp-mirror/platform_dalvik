/*
 * Copyright (C) 2010 The Android Open Source Project
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

package java.lang;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import junit.framework.TestCase;

public class StringTest extends TestCase {
    public void testIsEmpty() {
        assertTrue("".isEmpty());
        assertFalse("x".isEmpty());
    }

    // The evil decoder keeps hold of the CharBuffer it wrote to.
    private static final class EvilCharsetDecoder extends CharsetDecoder {
        private static char[] chars;
        public EvilCharsetDecoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            chars = out.array();
            int inLength = in.remaining();
            for (int i = 0; i < inLength; ++i) {
                in.put((byte) 'X');
                out.put('Y');
            }
            return CoderResult.UNDERFLOW;
        }
        public static void corrupt() {
            for (int i = 0; i < chars.length; ++i) {
                chars[i] = '$';
            }
        }
    }

    // The evil encoder tries to write to the CharBuffer it was given to
    // read from.
    private static final class EvilCharsetEncoder extends CharsetEncoder {
        public EvilCharsetEncoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            int inLength = in.remaining();
            for (int i = 0; i < inLength; ++i) {
                in.put('x');
                out.put((byte) 'y');
            }
            return CoderResult.UNDERFLOW;
        }
    }

    private static final Charset EVIL_CHARSET = new Charset("evil", null) {
        public boolean contains(Charset charset) { return false; }
        public CharsetEncoder newEncoder() { return new EvilCharsetEncoder(this); }
        public CharsetDecoder newDecoder() { return new EvilCharsetDecoder(this); }
    };

    public void testGetBytes() {
        Charset cs = Charset.forName("UTF-8");
        byte[] expected = new byte[] {(byte) 'h', (byte) 'i'};
        assertTrue(Arrays.equals(expected, "hi".getBytes(cs)));
    }

    public void testGetBytes_MaliciousCharset() {
        try {
            String s = "hi";
            // Check that our encoder can't write to the input CharBuffer
            // it was given.
            s.getBytes(EVIL_CHARSET);
            fail(); // We shouldn't have got here!
        } catch (ReadOnlyBufferException expected) {
            // We caught you trying to be naughty!
        }
    }

    public void testStringFromCharset() {
        Charset cs = Charset.forName("UTF-8");
        byte[] bytes = new byte[] {(byte) 'h', (byte) 'i'};
        assertEquals("hi", new String(bytes, cs));
    }

    public void testStringFromCharset_MaliciousCharset() {
        Charset cs = EVIL_CHARSET;
        byte[] bytes = new byte[] {(byte) 'h', (byte) 'i'};
        final String result = new String(bytes, cs);
        assertEquals("YY", result); // (Our decoder always outputs 'Y's.)
        // Check that even if the decoder messes with the output CharBuffer
        // after we've created a string from it, it doesn't affect the string.
        EvilCharsetDecoder.corrupt();
        assertEquals("YY", result);
    }
}

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

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CharsetDecoderTest extends junit.framework.TestCase {
    private static final String CHARSET = "UTF-16";
    
    private static final String SAMPLE_STRING = "Android";
    
    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_ByteArray_decode_no_offset() throws Exception {
        CharsetDecoder decoder = getCharsetDecoderUnderTest();
        byte[] arr = getEncodedByteArrayFixture();
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, 0, arr.length).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        assertFalse(coderResult.toString(), coderResult.isError());
        decoder.flush(outBuffer);
        outBuffer.flip();
        assertEquals(SAMPLE_STRING, outBuffer.toString().trim());
    }
    
    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_ByteArray_decode_with_offset() throws Exception {
        CharsetDecoder decoder = getCharsetDecoderUnderTest();
        byte[] arr = getEncodedByteArrayFixture();
        arr = prependByteToByteArray(arr, new Integer(1).byteValue());
        int offset = 1;
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, offset, arr.length - offset).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length - offset);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        assertFalse(coderResult.toString(), coderResult.isError());
        decoder.flush(outBuffer);
        outBuffer.flip();
        assertEquals(SAMPLE_STRING, outBuffer.toString().trim());
    }
    
    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_ByteArray_decode_with_offset_using_facade_method() throws Exception {
        CharsetDecoder decoder = getCharsetDecoderUnderTest();
        byte[] arr = getEncodedByteArrayFixture();
        arr = prependByteToByteArray(arr, new Integer(1).byteValue());
        int offset = 1;
        CharBuffer outBuffer = decoder.decode(ByteBuffer.wrap(arr, offset, arr.length - offset));
        assertEquals(SAMPLE_STRING, outBuffer.toString().trim());
    }
    
    private static byte[] prependByteToByteArray(byte[] arr, byte b) {
        byte[] result = new byte[arr.length + 1];
        result[0] = b;
        System.arraycopy(arr, 0, result, 1, arr.length);
        return result;
    }
    
    private static CharsetDecoder getCharsetDecoderUnderTest() {
        return Charset.forName(CHARSET).newDecoder();
    }
    
    private byte[] getEncodedByteArrayFixture() throws CharacterCodingException {
        CharsetEncoder encoder = Charset.forName(CHARSET).newEncoder();
        return encoder.encode(CharBuffer.wrap(SAMPLE_STRING)).array();
    }
}

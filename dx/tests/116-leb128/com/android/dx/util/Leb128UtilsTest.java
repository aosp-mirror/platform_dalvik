/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dx.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;

public final class Leb128UtilsTest extends TestCase {

    public void testDecodeUnsignedLeb() throws IOException {
        assertEquals(0, Leb128Utils.readUnsignedLeb128(newDataInput((byte) 0)));
        assertEquals(1, Leb128Utils.readUnsignedLeb128(newDataInput((byte) 1)));
        assertEquals(127, Leb128Utils.readUnsignedLeb128(newDataInput((byte) 0x7F)));
        assertEquals(16256, Leb128Utils.readUnsignedLeb128(newDataInput((byte) 0x80, (byte) 0x7F)));
    }

    public void testEncodeUnsignedLeb() throws IOException {
        assertEquals(new byte[] { 0 }, encodeUnsignedLeb(0));
        assertEquals(new byte[] { 1 }, encodeUnsignedLeb(1));
        assertEquals(new byte[] { 0x7F }, encodeUnsignedLeb(127));
        assertEquals(new byte[] { (byte) 0x80, 0x7F }, encodeUnsignedLeb(16256));
        assertEquals(new byte[] { (byte) 0xb4, 0x07 }, encodeUnsignedLeb(0x3b4));
        assertEquals(new byte[] { (byte) 0x8c, 0x08 }, encodeUnsignedLeb(0x40c));
    }

    public void testDecodeSignedLeb() throws IOException {
        assertEquals(0, Leb128Utils.readSignedLeb128(newDataInput((byte) 0)));
        assertEquals(1, Leb128Utils.readSignedLeb128(newDataInput((byte) 1)));
        assertEquals(-1, Leb128Utils.readSignedLeb128(newDataInput((byte) 0x7F)));
        assertEquals(0x3C, Leb128Utils.readSignedLeb128(newDataInput((byte) 0x3C)));
        assertEquals(-128, Leb128Utils.readSignedLeb128(newDataInput((byte) 0x80, (byte) 0x7F)));
    }

    public void testEncodeSignedLeb() throws IOException {
        assertEquals(new byte[] { 0 }, encodeSignedLeb(0));
        assertEquals(new byte[] { 1 }, encodeSignedLeb(1));
        assertEquals(new byte[] { 0x7F }, encodeSignedLeb(-1));
        assertEquals(new byte[] { (byte) 0x80, 0x7F }, encodeSignedLeb(-128));
    }

    private byte[] encodeSignedLeb(int value) {
        byte[] buffer = new byte[5];
        int length = Leb128Utils.writeSignedLeb128(buffer, 0, value);
        return Arrays.copyOfRange(buffer, 0, length);
    }

    private byte[] encodeUnsignedLeb(int value) {
        byte[] buffer = new byte[5];
        int length = Leb128Utils.writeUnsignedLeb128(buffer, 0, value);
        return Arrays.copyOfRange(buffer, 0, length);
    }

    public DataInputStream newDataInput(byte... bytes) {
        return new DataInputStream(new ByteArrayInputStream(bytes));
    }

    private void assertEquals(byte[] expected, byte[] actual) {
        assertTrue(Arrays.equals(expected, actual));
    }
}

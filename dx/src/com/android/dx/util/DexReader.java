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

import com.android.dx.dex.DexException;
import com.android.dx.dex.SizeOf;
import com.android.dx.dex.TableOfContents;
import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * All int offsets are unsigned.
 */
public final class DexReader implements Closeable {
    private final String name;
    private final byte[] fileContents;
    private final TableOfContents tableOfContents;
    private int position = 0;

    private final DataInput asDataInput = new DataInputStub() {
        public byte readByte() throws IOException {
            return DexReader.this.readByte();
        }
    };

    /**
     * Creates a new DexReader that reads ints and shorts in little-endian byte
     * order.
     */
    public DexReader(File file) throws IOException {
        name = file.getPath();

        FileInputStream in = new FileInputStream(file);
        int length = (int) file.length();
        fileContents = new byte[length];

        int count = 0;
        while (count < length) {
            int bytesRead = in.read(fileContents, count, length - count);
            if (bytesRead == -1) {
                throw new IOException("Expected " + length + " bytes but was " + count);
            }
            count += bytesRead;
        }
        in.close();

        tableOfContents = new TableOfContents(this);
    }

    public TableOfContents getTableOfContents() {
        return tableOfContents;
    }

    public int getPosition() throws IOException {
        return position;
    }

    public void seek(int offset) throws IOException {
        position = offset;
    }

    public void close() throws IOException {}

    public int readInt() throws IOException {
        int result = (fileContents[position] & 0xff)
                | (fileContents[position + 1] & 0xff) << 8
                | (fileContents[position + 2] & 0xff) << 16
                | (fileContents[position + 3] & 0xff) << 24;
        position += 4;
        return result;
    }

    public short readShort() throws IOException {
        int result = (fileContents[position] & 0xff)
                | (fileContents[position + 1] & 0xff) << 8;
        position += 2;
        return (short) result;
    }

    public byte readByte() throws IOException {
        return (byte) (fileContents[position++] & 0xff);
    }

    public byte[] readByteArray(int length) throws IOException {
        byte[] result = Arrays.copyOfRange(fileContents, position, position + length);
        position += length;
        return result;
    }

    public short[] readShortArray(int length) throws IOException {
        short[] result = new short[length];
        for (int i = 0; i < length; i++) {
            result[i] = readShort();
        }
        return result;
    }

    public int readUleb128() throws IOException {
        return Leb128Utils.readUnsignedLeb128(asDataInput);
    }

    public int readSleb128() throws IOException {
        return Leb128Utils.readSignedLeb128(asDataInput);
    }

    public short[] readTypeList(int offset) throws IOException {
        if (offset == 0) {
            return new short[0];
        }
        int savedPosition = position;
        position = offset;
        int size = readInt();
        short[] parameters = new short[size];
        for (int i = 0; i < size; i++) {
            parameters[i] = readShort();
        }
        position = savedPosition;
        return parameters;
    }

    public String readStringDataItem() throws IOException {
        int expectedLength = readUleb128();
        String result = Mutf8.decode(asDataInput, new char[expectedLength]);
        if (result.length() != expectedLength) {
            throw new DexException("Declared length " + expectedLength + " doesn't match decoded "
                    + "length of " + result.length());
        }
        return result;
    }

    /**
     * Reads a string at the given index. This method does not disturb the seek position.
     */
    public String readString(int index) throws IOException {
        int savedPosition = position;
        seek(tableOfContents.stringIds.off + (index * SizeOf.STRING_ID_ITEM));
        int stringDataOff = readInt();
        seek(stringDataOff);
        String result = readStringDataItem();
        position = savedPosition;
        return result;
    }

    @Override public String toString() {
        return name;
    }

    private static class DataInputStub implements DataInput {
        public byte readByte() throws IOException {
            throw new UnsupportedOperationException();
        }
        public void readFully(byte[] buffer) throws IOException {
            throw new UnsupportedOperationException();
        }
        public void readFully(byte[] buffer, int offset, int count) throws IOException {
            throw new UnsupportedOperationException();
        }
        public int skipBytes(int i) throws IOException {
            throw new UnsupportedOperationException();
        }
        public boolean readBoolean() throws IOException {
            throw new UnsupportedOperationException();
        }
        public int readUnsignedByte() throws IOException {
            throw new UnsupportedOperationException();
        }
        public short readShort() throws IOException {
            throw new UnsupportedOperationException();
        }
        public int readUnsignedShort() throws IOException {
            throw new UnsupportedOperationException();
        }
        public char readChar() throws IOException {
            throw new UnsupportedOperationException();
        }
        public int readInt() throws IOException {
            throw new UnsupportedOperationException();
        }
        public long readLong() throws IOException {
            throw new UnsupportedOperationException();
        }
        public float readFloat() throws IOException {
            throw new UnsupportedOperationException();
        }
        public double readDouble() throws IOException {
            throw new UnsupportedOperationException();
        }
        public String readLine() throws IOException {
            throw new UnsupportedOperationException();
        }
        public String readUTF() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}

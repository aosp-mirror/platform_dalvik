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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * All int offsets are unsigned.
 */
public final class DexReader implements Closeable {
    private final String name;
    private final RandomAccessFile randomAccessFile;
    private final TableOfContents tableOfContents;

    /**
     * Creates a new DexReader that reads ints and shorts in little-endian byte
     * order.
     */
    public DexReader(File file) throws IOException {
        this.name = file.getPath();
        this.randomAccessFile = new RandomAccessFile(file, "r");
        this.tableOfContents = new TableOfContents(this);
    }

    public TableOfContents getTableOfContents() {
        return tableOfContents;
    }

    public int getPosition() throws IOException {
        return (int) randomAccessFile.getFilePointer();
    }

    public void seek(int offset) throws IOException {
        randomAccessFile.seek(offset & 0xFFFFFFFFL);
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }

    public int readInt() throws IOException {
        int v = randomAccessFile.readInt();
        return Integer.reverseBytes(v);
    }

    public short readShort() throws IOException {
        short v = randomAccessFile.readShort();
        return Short.reverseBytes(v);
    }

    public byte readByte() throws IOException {
        return randomAccessFile.readByte();
    }

    public byte[] readByteArray(int length) throws IOException {
        byte[] result = new byte[length];
        randomAccessFile.readFully(result);
        return result;
    }

    public short[] readShortArray(int length) throws IOException {
        short[] result = new short[length];
        for (int i = 0; i < length; i++) {
            result[i] = readShort();
        }
        return result;
    }

    public int readUnsignedLeb128() throws IOException {
        return Leb128Utils.readUnsignedLeb128(randomAccessFile);
    }

    public int readSignedLeb128() throws IOException {
        return Leb128Utils.readSignedLeb128(randomAccessFile);
    }

    public short[] readTypeList(int offset) throws IOException {
        if (offset == 0) {
            return new short[0];
        }
        long position = randomAccessFile.getFilePointer();
        randomAccessFile.seek(offset);
        int size = readInt();
        short[] parameters = new short[size];
        for (int i = 0; i < size; i++) {
            parameters[i] = readShort();
        }
        randomAccessFile.seek(position);
        return parameters;
    }

    public String readStringDataItem() throws IOException {
        int expectedLength = readUnsignedLeb128();
        String result = Mutf8.decode(randomAccessFile, new char[expectedLength]);
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
        long position = randomAccessFile.getFilePointer();
        seek(tableOfContents.stringIds.off + (index * SizeOf.STRING_ID_ITEM));
        int stringDataOff = readInt();
        seek(stringDataOff);
        String result = readStringDataItem();
        randomAccessFile.seek(position);
        return result;
    }

    @Override public String toString() {
        return name;
    }
}

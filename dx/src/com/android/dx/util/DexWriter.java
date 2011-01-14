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
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes a dex file in sections.
 */
public final class DexWriter implements Closeable {
    private final List<Section> sections = new ArrayList<Section>();
    private final RandomAccessFile randomAccessFile;
    private int length;

    /**
     * Creates a new DexWriter that writes ints and shorts in little-endian byte
     * order.
     */
    public DexWriter(File file) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(file, "rw");
    }

    public Section newSection(int maxByteCount, String name) throws IOException {
        Section result = new Section(name, length, maxByteCount);
        sections.add(result);
        length = fourByteAlign(length + maxByteCount);
        return result;
    }

    public int getLength() {
        return length;
    }

    public void close() throws IOException {
        for (Section s : sections) {
            s.flush();
        }
        randomAccessFile.setLength(length);
        randomAccessFile.close();
    }

    public static int fourByteAlign(int position) {
        return (position + 3) & ~3;
    }

    public final class Section {
        private final String name;
        private final int offset;
        private final int maxByteCount;
        private final byte[] buffer;
        private int bufferedByteCount;
        private int byteCount;

        private Section(String name, int offset, int maxByteCount) {
            this.name = name;
            this.offset = offset;
            this.maxByteCount = maxByteCount;
            this.buffer = new byte[Math.min(8192, maxByteCount)];
        }

        public int getCursor() throws IOException {
            return offset + byteCount + bufferedByteCount;
        }

        public void flush() throws IOException {
            if (bufferedByteCount == 0) {
                return;
            }
            if (byteCount + bufferedByteCount > maxByteCount) {
                throw new DexException("Expected size " + maxByteCount
                        + " exceeded by " + name + ": " + byteCount + bufferedByteCount);
            }
            randomAccessFile.seek(offset + byteCount);
            randomAccessFile.write(buffer, 0, bufferedByteCount);
            byteCount += bufferedByteCount;
            bufferedByteCount = 0;
        }

        private void ensureCapacity(int byteCount) throws IOException {
            if (bufferedByteCount + byteCount > buffer.length) {
                flush();
            }
        }

        /**
         * Writes 0x00 until the position is aligned to a multiple of 4.
         */
        public void alignToFourBytes() throws IOException {
            int unalignedCount = bufferedByteCount;
            bufferedByteCount = DexWriter.fourByteAlign(byteCount + bufferedByteCount) - byteCount;
            for (int i = unalignedCount; i < bufferedByteCount; i++) {
                buffer[i] = 0;
            }
        }

        public void assertFourByteAligned() throws IOException {
            if ((getCursor() & 3) != 0) {
                throw new IllegalStateException("Not four byte aligned!");
            }
        }

        public void write(byte[] bytes) throws IOException {
            int offset = 0;
            while (offset < bytes.length) {
                ensureCapacity(1);
                int toCopy = Math.min(bytes.length - offset, buffer.length - bufferedByteCount);
                System.arraycopy(bytes, offset, buffer, bufferedByteCount, toCopy);
                bufferedByteCount += toCopy;
                offset += toCopy;
            }
        }

        public void writeByte(int b) throws IOException {
            ensureCapacity(1);
            buffer[bufferedByteCount] = (byte) b;
            bufferedByteCount++;
        }

        public void writeShort(short i) throws IOException {
            ensureCapacity(2);
            buffer[bufferedByteCount    ] = (byte) i;
            buffer[bufferedByteCount + 1] = (byte) (i >>> 8);
            bufferedByteCount += 2;
        }

        public void write(short[] shorts) throws IOException {
            for (short s : shorts) {
                writeShort(s);
            }
        }

        public void writeInt(int i) throws IOException {
            ensureCapacity(4);
            buffer[bufferedByteCount    ] = (byte) i;
            buffer[bufferedByteCount + 1] = (byte) (i >>>  8);
            buffer[bufferedByteCount + 2] = (byte) (i >>> 16);
            buffer[bufferedByteCount + 3] = (byte) (i >>> 24);
            bufferedByteCount += 4;
        }

        public void writeUleb128(int i) throws IOException {
            ensureCapacity(5);
            bufferedByteCount += Leb128Utils.writeUnsignedLeb128(buffer, bufferedByteCount, i);
        }

        public void writeSleb128(int i) throws IOException {
            ensureCapacity(5);
            bufferedByteCount += Leb128Utils.writeSignedLeb128(buffer, bufferedByteCount, i);
        }

        public void writeStringDataItem(String value) throws IOException {
            int length = value.length();
            writeUleb128(length);
            write(Mutf8.encode(value));
            writeByte(0);
        }
    }
}

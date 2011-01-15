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

package com.android.dx.io;

import com.android.dx.dex.DexException;
import com.android.dx.dex.SizeOf;
import com.android.dx.dex.TableOfContents;
import com.android.dx.util.Leb128Utils;
import com.android.dx.util.Mutf8;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * The bytes of a dex file in memory for reading and writing. All int offsets
 * are unsigned.
 */
public final class DexBuffer {
    private byte[] data;
    private final TableOfContents tableOfContents = new TableOfContents();
    private int length;

    public void loadFrom(InputStream in) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];

        int count;
        while ((count = in.read(buffer)) != -1) {
            bytesOut.write(buffer, 0, count);
        }

        this.data = bytesOut.toByteArray();
        tableOfContents.readFrom(this);
    }

    public void loadFrom(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        loadFrom(in);
        in.close();
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(data);
    }

    public void writeTo(File dexOut) throws IOException {
        OutputStream out = new FileOutputStream(dexOut);
        writeTo(out);
        out.close();
    }

    public TableOfContents getTableOfContents() {
        return tableOfContents;
    }

    public Section open(int position) {
        return new Section(position);
    }

    public Section appendSection(int maxByteCount, String name) throws IOException {
        Section result = new Section(name, length, length + maxByteCount);
        length = fourByteAlign(length + maxByteCount);
        return result;
    }

    public void noMoreSections() {
        data = new byte[length];
    }

    public int getLength() {
        return length;
    }

    private static int fourByteAlign(int position) {
        return (position + 3) & ~3;
    }

    public byte[] getBytes() {
        return data;
    }

    public final class Section {
        private final String name;
        private int position;
        private final int limit;

        private Section(String name, int position, int limit) {
            this.name = name;
            this.position = position;
            this.limit = limit;
        }

        private Section(int position) {
            this("section", position, data.length);
        }

        private final DataInput asDataInput = new DataInputStub() {
            public byte readByte() {
                return Section.this.readByte();
            }
        };

        public int getPosition() {
            return position;
        }

        public int readInt() {
            int result = (data[position] & 0xff)
                    | (data[position + 1] & 0xff) << 8
                    | (data[position + 2] & 0xff) << 16
                    | (data[position + 3] & 0xff) << 24;
            position += 4;
            return result;
        }

        public short readShort() {
            int result = (data[position] & 0xff)
                    | (data[position + 1] & 0xff) << 8;
            position += 2;
            return (short) result;
        }

        public byte readByte() {
            return (byte) (data[position++] & 0xff);
        }

        public byte[] readByteArray(int length) {
            byte[] result = Arrays.copyOfRange(data, position, position + length);
            position += length;
            return result;
        }

        public short[] readShortArray(int length) {
            short[] result = new short[length];
            for (int i = 0; i < length; i++) {
                result[i] = readShort();
            }
            return result;
        }

        public int readUleb128() {
            try {
                return Leb128Utils.readUnsignedLeb128(asDataInput);
            } catch (IOException e) {
                throw new DexException(e);
            }
        }

        public int readSleb128() {
            try {
                return Leb128Utils.readSignedLeb128(asDataInput);
            } catch (IOException e) {
                throw new DexException(e);
            }
        }

        public short[] readTypeList(int offset) {
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

        public String readStringDataItem() {
            try {
                int expectedLength = readUleb128();
                String result = Mutf8.decode(asDataInput, new char[expectedLength]);
                if (result.length() != expectedLength) {
                    throw new DexException("Declared length " + expectedLength
                            + " doesn't match decoded length of " + result.length());
                }
                return result;
            } catch (IOException e) {
                throw new DexException(e);
            }
        }

        /**
         * Reads a string at the given index. This method does not disturb the position.
         */
        public String readString(int index) {
            int savedPosition = position;
            position = tableOfContents.stringIds.off + (index * SizeOf.STRING_ID_ITEM);
            position = readInt();
            String result = readStringDataItem();
            position = savedPosition;
            return result;
        }

        public int readType(int index) {
            if (index < 0 || index >= tableOfContents.typeIds.size) {
                throw new IllegalArgumentException("type index out of range: "
                        + index + " " + tableOfContents.typeIds.size);
            }
            int savedPosition = position;
            position = tableOfContents.typeIds.off + (index * SizeOf.TYPE_ID_ITEM);
            int result = readInt();
            position = savedPosition;
            return result;
        }

        public String readTypeName(int index) {
            return readString(readType(index));
        }

        public FieldId readFieldId() {
            short declaringClassIndex = readShort();
            short typeIndex = readShort();
            int nameIndex = readInt();
            return new FieldId(DexBuffer.this, declaringClassIndex, typeIndex, nameIndex);
        }

        public MethodId readMethodId() {
            short declaringClassIndex = readShort();
            short protoIndex = readShort();
            int nameIndex = readInt();
            return new MethodId(DexBuffer.this, declaringClassIndex, protoIndex, nameIndex);
        }

        public ProtoId readProtoId(int index) {
            int savedPosition = position;
            position = tableOfContents.protoIds.off + (SizeOf.PROTO_ID_ITEM * index);
            ProtoId result = readProtoId();
            position = savedPosition;
            return result;
        }

        public ProtoId readProtoId() {
            int shortyIndex = readInt();
            int returnTypeIndex = readInt();
            int parametersOff = readInt();
            short[] parameters = readTypeList(parametersOff);
            return new ProtoId(DexBuffer.this, shortyIndex, returnTypeIndex, parameters);
        }

        public ClassDef readClassDef() {
            int offset = getPosition();
            int type = readInt();
            int accessFlags = readInt();
            int supertype = readInt();
            int interfacesOffset = readInt();
            short[] interfaces = readTypeList(interfacesOffset);
            int sourceFileIndex = readInt();
            int annotationsOffset = readInt();
            int classDataOffset = readInt();
            int staticValuesOffset = readInt();
            return new ClassDef(DexBuffer.this, offset, type, accessFlags, supertype,
                    interfacesOffset, interfaces, sourceFileIndex, annotationsOffset,
                    classDataOffset, staticValuesOffset);
        }

        private void checkPosition() {
            if (position > limit) {
                throw new DexException("Section limit " + limit + " exceeded by " + name);
            }
        }

        /**
         * Writes 0x00 until the position is aligned to a multiple of 4.
         */
        public void alignToFourBytes() throws IOException {
            int unalignedCount = position;
            position = DexBuffer.fourByteAlign(position);
            for (int i = unalignedCount; i < position; i++) {
                data[i] = 0;
            }
        }

        public void assertFourByteAligned() throws IOException {
            if ((position & 3) != 0) {
                throw new IllegalStateException("Not four byte aligned!");
            }
        }

        public void write(byte[] bytes) throws IOException {
            System.arraycopy(bytes, 0, data, position, bytes.length);
            position += bytes.length;
            checkPosition();
        }

        public void writeByte(int b) throws IOException {
            data[position++] = (byte) b;
            checkPosition();
        }

        public void writeShort(short i) throws IOException {
            data[position    ] = (byte) i;
            data[position + 1] = (byte) (i >>> 8);
            position += 2;
            checkPosition();
        }

        public void write(short[] shorts) throws IOException {
            for (short s : shorts) {
                writeShort(s);
            }
        }

        public void writeInt(int i) throws IOException {
            data[position    ] = (byte) i;
            data[position + 1] = (byte) (i >>>  8);
            data[position + 2] = (byte) (i >>> 16);
            data[position + 3] = (byte) (i >>> 24);
            position += 4;
            checkPosition();
        }

        public void writeUleb128(int i) throws IOException {
            position += Leb128Utils.writeUnsignedLeb128(data, position, i);
            checkPosition();
        }

        public void writeSleb128(int i) throws IOException {
            position += Leb128Utils.writeSignedLeb128(data, position, i);
            checkPosition();
        }

        public void writeStringDataItem(String value) throws IOException {
            int length = value.length();
            writeUleb128(length);
            write(Mutf8.encode(value));
            writeByte(0);
        }

        public Section open(int position) {
            return new Section(position);
        }
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

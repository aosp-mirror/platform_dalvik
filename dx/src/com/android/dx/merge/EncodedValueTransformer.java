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

package com.android.dx.merge;

import com.android.dx.io.DexBuffer;
import com.android.dx.util.Unsigned;

final class EncodedValueTransformer {
    private static final int ENCODED_BYTE = 0x00;
    private static final int ENCODED_SHORT = 0x02;
    private static final int ENCODED_CHAR = 0x03;
    private static final int ENCODED_INT = 0x04;
    private static final int ENCODED_LONG = 0x06;
    private static final int ENCODED_FLOAT = 0x10;
    private static final int ENCODED_DOUBLE = 0x11;
    private static final int ENCODED_STRING = 0x17;
    private static final int ENCODED_TYPE = 0x18;
    private static final int ENCODED_FIELD = 0x19;
    private static final int ENCODED_ENUM = 0x1b;
    private static final int ENCODED_METHOD = 0x1a;
    private static final int ENCODED_ARRAY = 0x1c;
    private static final int ENCODED_ANNOTATION = 0x1d;
    private static final int ENCODED_NULL = 0x1e;
    private static final int ENCODED_BOOLEAN = 0x1f;

    private final IndexMap indexMap;
    private final DexBuffer.Section in;
    private final DexBuffer.Section out;

    public EncodedValueTransformer(IndexMap indexMap, DexBuffer.Section in, DexBuffer.Section out) {
        this.indexMap = indexMap;
        this.in = in;
        this.out = out;
    }

    public void transformArray() {
        int size = in.readUleb128(); // size
        out.writeUleb128(size);
        for (int i = 0; i < size; i++) {
            transformValue();
        }
    }

    public void transformAnnotation() {
        out.writeUleb128(indexMap.adjustType(in.readUleb128())); // type idx

        int size = in.readUleb128(); // size
        out.writeUleb128(size);

        for (int i = 0; i < size; i++) {
            out.writeUleb128(indexMap.adjustString(in.readUleb128())); // name idx
            transformValue();
        }
    }

    public void transformValue() {
        int argAndType = in.readByte() & 0xff;
        int type = argAndType & 0x1f;
        int arg = (argAndType & 0xe0) >> 5;
        int size = arg + 1;

        switch (type) {
        case ENCODED_BYTE:
        case ENCODED_SHORT:
        case ENCODED_CHAR:
        case ENCODED_INT:
        case ENCODED_LONG:
        case ENCODED_FLOAT:
        case ENCODED_DOUBLE:
            out.writeByte(argAndType);
            copyBytes(in, out, size);
            break;

        case ENCODED_STRING:
            int indexIn = readIndex(in, size);
            int indexOut = indexMap.adjustString(indexIn);
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;
        case ENCODED_TYPE:
            indexIn = readIndex(in, size);
            indexOut = indexMap.adjustType(indexIn);
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;
        case ENCODED_FIELD:
        case ENCODED_ENUM:
            indexIn = readIndex(in, size);
            indexOut = indexMap.adjustField(indexIn);
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;
        case ENCODED_METHOD:
            indexIn = readIndex(in, size);
            indexOut = indexMap.adjustMethod(indexIn);
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;

        case ENCODED_ARRAY:
            out.writeByte(argAndType);
            transformArray();
            break;

        case ENCODED_ANNOTATION:
            out.writeByte(argAndType);
            transformAnnotation();
            break;

        case ENCODED_NULL:
        case ENCODED_BOOLEAN:
            out.writeByte(argAndType);
            break;
        }
    }

    private int readIndex(DexBuffer.Section in, int byteCount) {
        int result = 0;
        int shift = 0;
        for (int i = 0; i < byteCount; i++) {
            result += (in.readByte() & 0xff) << shift;
            shift += 8;
        }
        return result;
    }

    private void writeTypeAndSizeAndIndex(int type, int index, DexBuffer.Section out) {
        int byteCount;
        if (Unsigned.compare(index, 0xff) <= 0) {
            byteCount = 1;
        } else if (Unsigned.compare(index, 0xffff) <= 0) {
            byteCount = 2;
        } else if (Unsigned.compare(index, 0xffffff) <= 0) {
            byteCount = 3;
        } else {
            byteCount = 4;
        }
        int argAndType = ((byteCount - 1) << 5) | type;
        out.writeByte(argAndType);

        for (int i = 0; i < byteCount; i++) {
            out.writeByte(index & 0xff);
            index >>>= 8;
        }
    }

    private void copyBytes(DexBuffer.Section in, DexBuffer.Section out, int size) {
        for (int i = 0; i < size; i++) {
            out.writeByte(in.readByte());
        }
    }
}

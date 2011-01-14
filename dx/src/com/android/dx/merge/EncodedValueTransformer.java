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

import com.android.dx.util.DexReader;
import com.android.dx.util.DexWriter;
import com.android.dx.util.Unsigned;
import java.io.IOException;

public final class EncodedValueTransformer {

    private final IndexMap indexMap;
    private final DexReader in;
    private final DexWriter.Section out;

    public EncodedValueTransformer(IndexMap indexMap, DexReader in, DexWriter.Section out) {
        this.indexMap = indexMap;
        this.in = in;
        this.out = out;
    }

    public void transformArray() throws IOException {
        int size = in.readUnsignedLeb128(); // size
        out.writeUnsignedLeb128(size);
        for (int i = 0; i < size; i++) {
            transformValue();
        }
    }

    public void transformAnnotation() throws IOException {
        out.writeUnsignedLeb128(indexMap.typeIds[in.readUnsignedLeb128()]); // type idx

        int size = in.readUnsignedLeb128(); // size
        out.writeUnsignedLeb128(size);

        for (int i = 0; i < size; i++) {
            out.writeUnsignedLeb128(indexMap.stringIds[in.readUnsignedLeb128()]); // name idx
            transformValue();
        }
    }

    public void transformValue() throws IOException {
        int argAndType = in.readByte() & 0xff;
        int type = argAndType & 0x1f;
        int arg = (argAndType & 0xe0) >> 5;
        int size = arg + 1;

        switch (type) {
        case 0x00: // byte
        case 0x02: // short
        case 0x03: // char
        case 0x04: // int
        case 0x06: // long
        case 0x10: // float
        case 0x11: // double
            out.writeByte(argAndType);
            copyBytes(in, out, size);
            break;

        case 0x17: // string
            int indexIn = readIndex(in, size);
            int indexOut = indexMap.stringIds[indexIn];
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;
        case 0x18: // type
            indexIn = readIndex(in, size);
            indexOut = indexMap.typeIds[indexIn];
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;
        case 0x19: // field
        case 0x1b: // enum
            indexIn = readIndex(in, size);
            indexOut = indexMap.fieldIds[indexIn];
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;
        case 0x1a: // method
            indexIn = readIndex(in, size);
            indexOut = indexMap.methodIds[indexIn];
            writeTypeAndSizeAndIndex(type, indexOut, out);
            break;

        case 0x1c: // array
            out.writeByte(argAndType);
            transformArray();
            break;

        case 0x1d: // annotation
            out.writeByte(argAndType);
            transformAnnotation();
            break;

        case 0x1e: // null
        case 0x1f: // boolean
            out.writeByte(argAndType);
            break;
        }
    }

    private int readIndex(DexReader in, int byteCount) throws IOException {
        int result = 0;
        int shift = 0;
        for (int i = 0; i < byteCount; i++) {
            result += (in.readByte() & 0xff) << shift;
            shift += 8;
        }
        return result;
    }

    private void writeTypeAndSizeAndIndex(int type, int index, DexWriter.Section out)
            throws IOException {
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

    private void copyBytes(DexReader in, DexWriter.Section out, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            out.writeByte(in.readByte());
        }
    }
}

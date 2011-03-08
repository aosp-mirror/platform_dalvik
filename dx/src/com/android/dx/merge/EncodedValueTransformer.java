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
import com.android.dx.io.EncodedValueReader;
import com.android.dx.util.Unsigned;

public final class EncodedValueTransformer extends EncodedValueReader {
    private final IndexMap indexMap;
    private final DexBuffer.Section out;

    public EncodedValueTransformer(DexBuffer.Section in, IndexMap indexMap, DexBuffer.Section out) {
        super(in);
        this.indexMap = indexMap;
        this.out = out;
    }

    protected void visitArray(int size) {
        out.writeUleb128(size);
    }

    protected void visitAnnotation(int typeIndex, int size) {
        out.writeUleb128(indexMap.adjustType(typeIndex));
        out.writeUleb128(size);
    }

    protected void visitAnnotationName(int index) {
        out.writeUleb128(indexMap.adjustString(index));
    }

    protected void visitPrimitive(int argAndType, int type, int arg, int size) {
        out.writeByte(argAndType);
        copyBytes(in, out, size);
    }

    protected void visitString(int type, int index) {
        writeTypeAndSizeAndIndex(type, indexMap.adjustString(index), out);
    }

    protected void visitType(int type, int index) {
        writeTypeAndSizeAndIndex(type, indexMap.adjustType(index), out);
    }

    protected void visitField(int type, int index) {
        writeTypeAndSizeAndIndex(type, indexMap.adjustField(index), out);
    }

    protected void visitMethod(int type, int index) {
        writeTypeAndSizeAndIndex(type, indexMap.adjustMethod(index), out);
    }

    protected void visitArrayValue(int argAndType) {
        out.writeByte(argAndType);
    }

    protected void visitAnnotationValue(int argAndType) {
        out.writeByte(argAndType);
    }

    protected void visitEncodedBoolean(int argAndType) {
        out.writeByte(argAndType);
    }

    protected void visitEncodedNull(int argAndType) {
        out.writeByte(argAndType);
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

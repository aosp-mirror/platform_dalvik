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

public final class MethodId implements Comparable<MethodId> {
    private short classIndex;
    private short protoIndex;
    private int nameIndex;

    public MethodId(DexReader in) throws IOException {
        classIndex = in.readShort();
        protoIndex = in.readShort();
        nameIndex = in.readInt();
    }

    /**
     * Maps from one set of indices to another.
     */
    public void adjust(IndexMap indexMap) {
        classIndex = (short) indexMap.typeIds[classIndex];
        protoIndex = (short) indexMap.protoIds[protoIndex];
        nameIndex = (short) indexMap.stringIds[nameIndex];
    }

    public int compareTo(MethodId other) {
        if (classIndex != other.classIndex) {
            return Unsigned.compare(classIndex, other.classIndex);
        }
        if (nameIndex != other.nameIndex) {
            return Unsigned.compare(nameIndex, other.nameIndex);
        }
        return Unsigned.compare(protoIndex, other.protoIndex);
    }

    public void writeTo(DexWriter.Section out) throws IOException {
        out.writeShort(classIndex);
        out.writeShort(protoIndex);
        out.writeInt(nameIndex);
    }
}

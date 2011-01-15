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

import com.android.dx.util.Unsigned;
import java.io.IOException;

public final class MethodId implements Comparable<MethodId> {
    private final DexBuffer buffer;
    private final short declaringClassIndex;
    private final short protoIndex;
    private final int nameIndex;

    public MethodId(DexBuffer buffer, short declaringClassIndex, short protoIndex, int nameIndex) {
        this.buffer = buffer;
        this.declaringClassIndex = declaringClassIndex;
        this.protoIndex = protoIndex;
        this.nameIndex = nameIndex;
    }

    public short getDeclaringClassIndex() {
        return declaringClassIndex;
    }

    public short getProtoIndex() {
        return protoIndex;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public int compareTo(MethodId other) {
        if (declaringClassIndex != other.declaringClassIndex) {
            return Unsigned.compare(declaringClassIndex, other.declaringClassIndex);
        }
        if (nameIndex != other.nameIndex) {
            return Unsigned.compare(nameIndex, other.nameIndex);
        }
        return Unsigned.compare(protoIndex, other.protoIndex);
    }

    public void writeTo(DexBuffer.Section out) throws IOException {
        out.writeShort(declaringClassIndex);
        out.writeShort(protoIndex);
        out.writeInt(nameIndex);
    }

    @Override public String toString() {
        if (buffer == null) {
            return declaringClassIndex + " " + protoIndex + " " + nameIndex;
        }
        DexBuffer.Section in = buffer.open(0);
        return in.readTypeName(declaringClassIndex)
                + " " + in.readProtoId(protoIndex)
                + " " + in.readString(nameIndex);
    }
}

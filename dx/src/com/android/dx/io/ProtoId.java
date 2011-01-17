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
import java.util.Arrays;

public final class ProtoId implements Comparable<ProtoId> {
    private final DexBuffer buffer;
    private final int shortyIndex;
    private final int returnTypeIndex;
    private final short[] parameters;

    public ProtoId(DexBuffer buffer, int shortyIndex, int returnTypeIndex, short[] parameters) {
        this.buffer = buffer;
        this.shortyIndex = shortyIndex;
        this.returnTypeIndex = returnTypeIndex;
        this.parameters = parameters;
    }

    public int compareTo(ProtoId other) {
        if (returnTypeIndex != other.returnTypeIndex) {
            return Unsigned.compare(returnTypeIndex, other.returnTypeIndex);
        }
        for (int i = 0; i < parameters.length && i < other.parameters.length; i++) {
            if (parameters[i] != other.parameters[i]) {
                return Unsigned.compare(parameters[i], other.parameters[i]);
            }
        }
        return Unsigned.compare(parameters.length, other.parameters.length);
    }

    public int getShortyIndex() {
        return shortyIndex;
    }

    public int getReturnTypeIndex() {
        return returnTypeIndex;
    }

    public short[] getParameters() {
        return parameters;
    }

    public void writeTo(DexBuffer.Section out, int typeListOffset) {
        out.writeInt(shortyIndex);
        out.writeInt(returnTypeIndex);
        out.writeInt(typeListOffset);
    }

    @Override public String toString() {
        if (buffer == null) {
            return shortyIndex + " " + returnTypeIndex + " " + Arrays.toString(parameters);
        }

        StringBuilder result = new StringBuilder()
                .append(buffer.strings().get(shortyIndex))
                .append(": ")
                .append(buffer.typeNames().get(returnTypeIndex))
                .append(" (");
        int j = 0;
        for (short parameter : parameters) {
            if (j > 0) {
                result.append(", ");
            }
            result.append(buffer.typeNames().get(parameter));
            j++;
        }
        result.append(")");
        return result.toString();
    }
}

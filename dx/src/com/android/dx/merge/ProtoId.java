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

public final class ProtoId implements Comparable<ProtoId> {
    private int shorty;
    private int returnType;
    private short[] parameters;

    public ProtoId(DexReader in) throws IOException {
        shorty = in.readInt();
        returnType = in.readInt();
        int parametersOff = in.readInt();
        parameters = in.readTypeList(parametersOff);
    }

    /**
     * Maps from one set of indices to another.
     */
    public void adjust(IndexMap indexMap) {
        shorty = indexMap.stringIds[shorty];
        returnType = indexMap.typeIds[returnType];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = (short) indexMap.typeIds[parameters[i]];
        }
    }

    public int compareTo(ProtoId other) {
        if (returnType != other.returnType) {
            return Unsigned.compare(returnType, other.returnType);
        }
        for (int i = 0; i < parameters.length && i < other.parameters.length; i++) {
            if (parameters[i] != other.parameters[i]) {
                return Unsigned.compare(parameters[i], other.parameters[i]);
            }
        }
        return Unsigned.compare(parameters.length, other.parameters.length);
    }

    public short[] getParameters() {
        return parameters;
    }

    public void writeTo(DexWriter.Section out, int typeListOffset) throws IOException {
        out.writeInt(shorty);
        out.writeInt(returnType);
        out.writeInt(typeListOffset);
    }
}

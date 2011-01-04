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
import java.io.IOException;
import java.util.Comparator;

/**
 * Name and structure of a type. Used to order types such that each type is
 * preceded by its supertype and implemented interfaces.
 */
public final class SortableType {
    public static final Comparator<SortableType> NULLS_LAST_ORDER = new Comparator<SortableType>() {
        public int compare(SortableType a, SortableType b) {
            if (a == b) {
                return 0;
            }
            if (b == null) {
                return -1;
            }
            if (a == null) {
                return 1;
            }
            if (a.depth != b.depth) {
                return a.depth - b.depth;
            }
            return a.type - b.type;
        }
    };

    private final DexReader reader;
    private final int offset;
    private int type;
    private int supertype;
    private final short[] interfaces;
    private int depth = -1;

    public SortableType(DexReader reader) throws IOException {
        this.reader = reader;
        this.offset = reader.getPosition();
        this.type = reader.readInt(); // class idx
        reader.readInt(); // access flags
        this.supertype = reader.readInt(); // superclass idx
        int interfacesOff = reader.readInt(); // interface off
        this.interfaces = reader.readTypeList(interfacesOff);
        reader.readInt(); // source file index
        reader.readInt(); // annotations off
        reader.readInt(); // class data off
        reader.readInt(); // static values off
    }

    public void adjust(IndexMap indexMap) {
        type = indexMap.typeIds[type];
        if (supertype != DexMerger.NO_INDEX) {
            supertype = indexMap.typeIds[supertype];
        }
        indexMap.adjustTypeList(interfaces);
    }

    public DexReader prepareReader() throws IOException {
        reader.seek(offset);
        return reader;
    }

    public int getType() {
        return type;
    }

    /**
     * Assigns this type's depth if the depths of its supertype and implemented
     * interfaces are known. Returns false if the depth couldn't be computed
     * yet.
     */
    public boolean tryAssignDepth(SortableType[] types) {
        int max;
        if (supertype == DexMerger.NO_INDEX) {
            max = 0; // this is Object.class or an interface
        } else {
            SortableType sortableSupertype = types[supertype];
            if (sortableSupertype == null) {
                max = 1; // unknown, so assume it's a root.
            } else if (sortableSupertype.depth == -1) {
                return false;
            } else {
                max = sortableSupertype.depth;
            }
        }

        for (short interfaceIndex : interfaces) {
            SortableType implemented = types[interfaceIndex];
            if (implemented == null) {
                max = Math.max(max, 1); // unknown, so assume it's a root.
            } else if (implemented.depth == -1) {
                return false;
            } else {
                max = Math.max(max, implemented.depth);
            }
        }

        depth = max + 1;
        return true;
    }

    public boolean isDepthAssigned() {
        return depth != -1;
    }
}

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

import com.android.dx.dex.TableOfContents;
import com.android.dx.io.ClassDef;
import com.android.dx.io.DexBuffer;
import com.android.dx.io.FieldId;
import com.android.dx.io.MethodId;
import com.android.dx.io.ProtoId;
import java.util.HashMap;

/**
 * Maps the index offsets from one dex file to those in another. For example, if
 * you have string #5 in the old dex file, its position in the new dex file is
 * {@code strings[5]}.
 */
public final class IndexMap {
    private final DexBuffer target;
    public final int[] stringIds;
    public final short[] typeIds;
    public final short[] protoIds;
    public final short[] fieldIds;
    public final short[] methodIds;
    public final HashMap<Integer, Integer> typeListOffsets;

    public IndexMap(DexBuffer target, TableOfContents tableOfContents) {
        this.target = target;
        this.stringIds = new int[tableOfContents.stringIds.size];
        this.typeIds = new short[tableOfContents.typeIds.size];
        this.protoIds = new short[tableOfContents.protoIds.size];
        this.fieldIds = new short[tableOfContents.fieldIds.size];
        this.methodIds = new short[tableOfContents.methodIds.size];
        this.typeListOffsets = new HashMap<Integer, Integer>();

        /*
         * A type list at offset 0 is always the empty type list. Always map
         * this to itself.
         */
        this.typeListOffsets.put(0, 0);
    }

    public int adjustString(int stringIndex) {
        return stringIndex == ClassDef.NO_INDEX ? ClassDef.NO_INDEX : stringIds[stringIndex];
    }

    public short adjustType(int typeIndex) {
        return (typeIndex == ClassDef.NO_INDEX) ? ClassDef.NO_INDEX : typeIds[typeIndex];
    }

    public TypeList adjustTypeList(TypeList typeList) {
        if (typeList == TypeList.EMPTY) {
            return typeList;
        }
        short[] types = typeList.getTypes().clone();
        for (int i = 0; i < types.length; i++) {
            types[i] = adjustType(types[i]);
        }
        return new TypeList(target, types);
    }

    public short adjustProto(int protoIndex) {
        return protoIds[protoIndex];
    }

    public short adjustField(int fieldIndex) {
        return fieldIds[fieldIndex];
    }

    public short adjustMethod(int methodIndex) {
        return methodIds[methodIndex];
    }

    public int adjustTypeListOffset(int typeListOffset) {
        return typeListOffsets.get(typeListOffset);
    }

    public MethodId adjust(MethodId methodId) {
        return new MethodId(target,
                adjustType(methodId.getDeclaringClassIndex()),
                adjustProto(methodId.getProtoIndex()),
                adjustString(methodId.getNameIndex()));
    }

    public FieldId adjust(FieldId fieldId) {
        return new FieldId(target,
                adjustType(fieldId.getDeclaringClassIndex()),
                adjustType(fieldId.getTypeIndex()),
                adjustString(fieldId.getNameIndex()));

    }

    public ProtoId adjust(ProtoId protoId) {
        return new ProtoId(target,
                adjustString(protoId.getShortyIndex()),
                adjustType(protoId.getReturnTypeIndex()),
                adjustTypeListOffset(protoId.getParametersOffset()));
    }

    public ClassDef adjust(ClassDef classDef) {
        return new ClassDef(target, classDef.getOffset(), adjustType(classDef.getTypeIndex()),
                classDef.getAccessFlags(), adjustType(classDef.getSupertypeIndex()),
                adjustTypeListOffset(classDef.getInterfacesOffset()), classDef.getSourceFileIndex(),
                classDef.getAnnotationsOffset(), classDef.getClassDataOffset(),
                classDef.getStaticValuesOffset());
    }

    public SortableType adjust(SortableType sortableType) {
        return new SortableType(sortableType.getBuffer(), adjust(sortableType.getClassDef()));
    }
}

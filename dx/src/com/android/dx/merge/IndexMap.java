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

/**
 * Maps the index offsets from one dex file to those in another. For example, if
 * you have string #5 in the old dex file, its position in the new dex file is
 * {@code strings[5]}.
 */
public final class IndexMap {
    public final int[] stringIds;
    public final int[] typeIds;
    public final int[] protoIds;
    public final int[] fieldIds;
    public final int[] methodIds;

    public IndexMap(TableOfContents tableOfContents) {
        stringIds = new int[tableOfContents.stringIds.size];
        typeIds = new int[tableOfContents.typeIds.size];
        protoIds = new int[tableOfContents.protoIds.size];
        fieldIds = new int[tableOfContents.fieldIds.size];
        methodIds = new int[tableOfContents.methodIds.size];
    }

    public void adjustTypeList(short[] typeList) {
        for (int i = 0; i < typeList.length; i++) {
            typeList[i] = (short) typeIds[typeList[i]];
        }
    }
}

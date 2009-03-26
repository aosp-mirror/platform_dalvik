/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.dexdeps;

public class FieldRef {
    private String mDeclClass, mFieldType, mFieldName;

    /**
     * Initializes a new field reference.
     */
    public FieldRef(String declClass, String fieldType, String fieldName) {
        mDeclClass = declClass;
        mFieldType = fieldType;
        mFieldName = fieldName;
    }

    public String getDeclClassName() {
        return mDeclClass;
    }

    public String getTypeName() {
        return mFieldType;
    }

    public String getName() {
        return mFieldName;
    }
}


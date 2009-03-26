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

public class MethodRef {
    private String mDeclClass, mDescriptor, mMethodName;

    /**
     * Initializes a new field reference.
     */
    public MethodRef(String declClass, String descriptor, String methodName) {
        mDeclClass = declClass;
        mDescriptor = descriptor;
        mMethodName = methodName;
    }

    /**
     * Gets the name of the method's declaring class.
     */
    public String getDeclClassName() {
        return mDeclClass;
    }

    /**
     * Gets the method's descriptor.
     */
    public String getDescriptor() {
        return mDescriptor;
    }

    /**
     * Gets the method's name.
     */
    public String getName() {
        return mMethodName;
    }

    /**
     * Gets the method arguments as an array of type strings.
     */
    public String[] getArguments() {
        // TODO
        return null;
    }

    /**
     * Gets the method's return type.
     */
    public String getReturnType() {
        // TODO
        return null;
    }
}



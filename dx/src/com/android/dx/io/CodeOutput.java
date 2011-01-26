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

/**
 * Output stream of code units, for writing out Dalvik bytecode.
 */
public interface CodeOutput {
    /**
     * Write a code unit.
     */
    public void write(short codeUnit);

    /**
     * Write two code units.
     */
    public void write(short u0, short u1);

    /**
     * Write three code units.
     */
    public void write(short u0, short u1, short u2);

    /**
     * Write four code units.
     */
    public void write(short u0, short u1, short u2, short u3);

    /**
     * Write five code units.
     */
    public void write(short u0, short u1, short u2, short u3, short u4);
}

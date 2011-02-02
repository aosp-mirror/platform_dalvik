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
 * Implementation of {@code CodeOutput} that writes to a {@code short[]}.
 */
public final class ShortArrayCodeOutput implements CodeOutput {
    /** array to write to */
    private final short[] array;

    /** next index within {@link #array} to write */
    private int cursor;

    /**
     * Constructs an instance.
     */
    public ShortArrayCodeOutput(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize < 0");
        }

        this.array = new short[maxSize];
        this.cursor = 0;
    }

    /**
     * Gets the array. The returned array contains exactly the data
     * written (e.g. no leftover space at the end).
     */
    public short[] getArray() {
        if (cursor == array.length) {
            return array;
        }

        short[] result = new short[cursor];
        System.arraycopy(array, 0, result, 0, cursor);
        return result;
    }

    /** @inheritDoc */
    public void write(short codeUnit) {
        array[cursor++] = codeUnit;
    }

    /** @inheritDoc */
    public void write(short u0, short u1) {
        write(u0);
        write(u1);
    }

    /** @inheritDoc */
    public void write(short u0, short u1, short u2) {
        write(u0);
        write(u1);
        write(u2);
    }

    /** @inheritDoc */
    public void write(short u0, short u1, short u2, short u3) {
        write(u0);
        write(u1);
        write(u2);
        write(u3);
    }

    /** @inheritDoc */
    public void write(short u0, short u1, short u2, short u3, short u4) {
        write(u0);
        write(u1);
        write(u2);
        write(u3);
        write(u4);
    }

    /** @inheritDoc */
    public void write(short[] data) {
        for (short unit : data) {
            write(unit);
        }
    }
}

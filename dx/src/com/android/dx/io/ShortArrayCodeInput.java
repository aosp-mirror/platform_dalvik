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

import java.io.EOFException;

/**
 * Implementation of {@code CodeInput} that reads from a {@code short[]}.
 */
public final class ShortArrayCodeInput {
    /** source array to read from */
    private final short[] array;

    /** next index within {@link #array} to read from */
    private int cursor;

    /**
     * Constructs an instance.
     */
    public ShortArrayCodeInput(short[] array) {
        if (array == null) {
            throw new NullPointerException("array == null");
        }

        this.array = array;
        this.cursor = 0;
    }

    /** @inheritDoc */
    public int cursor() {
        return cursor;
    }

    /** @inheritDoc */
    public int read() throws EOFException {
        try {
            return array[cursor++];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new EOFException();
        }
    }

    /** @inheritDoc */
    public int readInt() throws EOFException {
        int short0 = read();
        int short1 = read();

        return short0 | (short1 << 16);
    }

    /** @inheritDoc */
    public long readLong() throws EOFException {
        long short0 = read();
        long short1 = read();
        long short2 = read();
        long short3 = read();

        return short0 | (short1 << 16) | (short2 << 32) | (short3 << 48);
    }
}

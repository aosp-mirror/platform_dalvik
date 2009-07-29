/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util.concurrent;

import java.lang.reflect.Array;

/**
 * Arrays.copyOf and Arrays.copyOfRange backported from Java 6.
 */
class Java6Arrays {

    static <T> T[] copyOf(T[] original, int newLength) {
        if (null == original) {
            throw new NullPointerException();
        }
        if (0 <= newLength) {
            return copyOfRange(original, 0, newLength);
        }
        throw new NegativeArraySizeException();
    }

    static <T, U> T[] copyOf(U[] original, int newLength,
            Class<? extends T[]> newType) {
        if (0 <= newLength) {
            return copyOfRange(original, 0, newLength, newType);
        }
        throw new NegativeArraySizeException();
    }

    @SuppressWarnings("unchecked")
    static <T> T[] copyOfRange(T[] original, int start, int end) {
        if (original.length >= start && 0 <= start) {
            if (start <= end) {
                int length = end - start;
                int copyLength = Math.min(length, original.length - start);
                T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), length);
                System.arraycopy(original, start, copy, 0, copyLength);
                return copy;
            }
            throw new IllegalArgumentException();
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @SuppressWarnings("unchecked")
    static <T, U> T[] copyOfRange(U[] original, int start, int end,
            Class<? extends T[]> newType) {
        if (start <= end) {
            if (original.length >= start && 0 <= start) {
                int length = end - start;
                int copyLength = Math.min(length, original.length - start);
                T[] copy = (T[]) Array.newInstance(newType.getComponentType(),
                        length);
                System.arraycopy(original, start, copy, 0, copyLength);
                return copy;
            }
            throw new ArrayIndexOutOfBoundsException();
        }
        throw new IllegalArgumentException();
    }

}

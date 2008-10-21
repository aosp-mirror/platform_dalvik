/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.dx.util;

/**
 * Utilities for treating <code>int[]</code>s as bit sets.
 */
public final class Bits {
    /**
     * This class is uninstantiable.
     */
    private Bits() {
        // This space intentionally left blank.
    }

    /**
     * Constructs a bit set to contain bits up to the given index (exclusive).
     * 
     * @param max &gt;= 0; the maximum bit index (exclusive)
     * @return non-null; an appropriately-constructed instance
     */
    public static int[] makeBitSet(int max) {
        int size = (max + 0x1f) >> 5;
        return new int[size];
    }

    /**
     * Gets the maximum index (exclusive) for the given bit set.
     * 
     * @param bits non-null; bit set in question
     * @return &gt;= 0; the maximum index (exclusive) that may be set
     */
    public static int getMax(int[] bits) {
        return bits.length * 0x20;
    }

    /**
     * Gets the value of the bit at the given index.
     * 
     * @param bits non-null; bit set to operate on
     * @param idx &gt;= 0, &lt; getMax(set); which bit
     * @return the value of the indicated bit
     */
    public static boolean get(int[] bits, int idx) {
        int arrayIdx = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        return (bits[arrayIdx] & bit) != 0;
    }

    /**
     * Sets the given bit to the given value.
     * 
     * @param bits non-null; bit set to operate on
     * @param idx &gt;= 0, &lt; getMax(set); which bit
     * @param value the new value for the bit
     */
    public static void set(int[] bits, int idx, boolean value) {
        int arrayIdx = idx >> 5;
        int bit = 1 << (idx & 0x1f);

        if (value) {
            bits[arrayIdx] |= bit;
        } else {
            bits[arrayIdx] &= ~bit;
        }
    }

    /**
     * Sets the given bit to <code>true</code>.
     * 
     * @param bits non-null; bit set to operate on
     * @param idx &gt;= 0, &lt; getMax(set); which bit
     */
    public static void set(int[] bits, int idx) {
        int arrayIdx = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        bits[arrayIdx] |= bit;
    }

    /**
     * Sets the given bit to <code>false</code>.
     * 
     * @param bits non-null; bit set to operate on
     * @param idx &gt;= 0, &lt; getMax(set); which bit
     */
    public static void clear(int[] bits, int idx) {
        int arrayIdx = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        bits[arrayIdx] &= ~bit;
    }

    /**
     * Returns whether or not the given bit set is empty, that is, whether
     * no bit is set to <code>true</code>.
     * 
     * @param bits non-null; bit set to operate on
     * @return <code>true</code> iff all bits are <code>false</code>
     */
    public static boolean isEmpty(int[] bits) {
        int len = bits.length;

        for (int i = 0; i < len; i++) {
            if (bits[i] != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the number of bits set to <code>true</code> in the given bit set.
     * 
     * @param bits non-null; bit set to operate on
     * @return &gt;= 0; the bit count (aka population count) of the set
     */
    public static int bitCount(int[] bits) {
        int len = bits.length;
        int count = 0;

        for (int i = 0; i < len; i++) {
            count += Integer.bitCount(bits[i]);
        }

        return count;
    }

    /**
     * Returns whether any bits are set to <code>true</code> in the
     * specified range.
     * 
     * @param bits non-null; bit set to operate on
     * @param start &gt;= 0; index of the first bit in the range (inclusive)
     * @param end &gt;= 0; index of the last bit in the range (exclusive)
     * @return <code>true</code> if any bit is set to <code>true</code> in
     * the indicated range
     */
    public static boolean anyInRange(int[] bits, int start, int end) {
        int idx = findFirst(bits, start);
        return (idx >= 0) && (idx < end);
    }

    /**
     * Finds the lowest-order bit set at or after the given index in the
     * given bit set.
     * 
     * @param bits non-null; bit set to operate on
     * @param idx &gt;= 0; minimum index to return
     * @return &gt;= -1; lowest-order bit set at or after <code>idx</code>,
     * or <code>-1</code> if there is no appropriate bit index to return
     */
    public static int findFirst(int[] bits, int idx) {
        int len = bits.length;
        int minBit = idx & 0x1f;

        for (int arrayIdx = idx >> 5; arrayIdx < len; arrayIdx++) {
            int word = bits[arrayIdx];
            if (word != 0) {
                int bitIdx = findFirst(word, minBit);
                if (bitIdx >= 0) {
                    return (arrayIdx << 5) + bitIdx;
                }
            }
            minBit = 0;
        }

        return -1;
    }

    /**
     * Finds the lowest-order bit set at or after the given index in the
     * given <code>int</code>.
     * 
     * @param value the value in question
     * @param idx 0..31 the minimum bit index to return
     * @return &gt;= -1; lowest-order bit set at or after <code>idx</code>,
     * or <code>-1</code> if there is no appropriate bit index to return
     */
    public static int findFirst(int value, int idx) {
        value &= ~((1 << idx) - 1); // Mask off too-low bits.
        int result = Integer.numberOfTrailingZeros(value);
        return (result == 32) ? -1 : result;
    }

    /**
     * Ors bit array <code>b</code> into bit array <code>a</code>.
     * <code>a.length</code> must be greater than or equal to
     * <code>b.length</code>.
     *
     * @param a non-null; int array to be ored with other argument. This
     * argument is modified.
     * @param b non-null; int array to be ored into <code>a</code>. This
     * argument is not modified.
     */
    public static void or(int[] a, int[] b) {
        for (int i = 0; i < b.length; i++) {
            a[i] |= b[i];
        }
    }

    public static String toHuman(int[] bits) {
        StringBuilder sb = new StringBuilder();

        boolean needsComma = false;

        sb.append('{');

        int bitsLength = 32 * bits.length;
        for (int i = 0; i < bitsLength; i++) {
            if (Bits.get(bits, i)) {
                if (needsComma) {
                    sb.append(',');
                }
                needsComma = true;
                sb.append(i);
            }
        }
        sb.append('}');

        return sb.toString();
    }
}

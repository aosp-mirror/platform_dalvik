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

package java.util;

import java.io.Serializable;

import org.apache.harmony.luni.util.Msg;

/**
 * The {@code BitSet} class implements a bit field. Each element in a
 * {@code BitSet} can be on(1) or off(0). A {@code BitSet} is created with a
 * given size and grows if this size is exceeded. Growth is always rounded to a
 * 64 bit boundary.
 *  
 * @since Android 1.0
 */
public class BitSet implements Serializable, Cloneable {
    private static final long serialVersionUID = 7997698588986878753L;

    // Size in bits of the data type being used in the bits array
    private static final int ELM_SIZE = 64;

    private long[] bits;

    /**
     * Create a new {@code BitSet} with size equal to 64 bits.
     * 
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     * @since Android 1.0
     */
    public BitSet() {
        this(64);
    }

    /**
     * Create a new {@code BitSet} with size equal to nbits. If nbits is not a
     * multiple of 64, then create a {@code BitSet} with size nbits rounded to
     * the next closest multiple of 64.
     * 
     * @param nbits
     *            the size of the bit set.
     * @throws NegativeArraySizeException
     *             if {@code nbits} is negative.
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     * @since Android 1.0
     */
    public BitSet(int nbits) {
        if (nbits >= 0) {
            bits = new long[(nbits / ELM_SIZE) + (nbits % ELM_SIZE > 0 ? 1 : 0)];
        } else {
            throw new NegativeArraySizeException();
        }
    }

    /**
     * Private constructor called from get(int, int) method
     * 
     * @param bits
     *            the size of the bit set
     */
    private BitSet(long[] bits) {
        this.bits = bits;
    }

    /**
     * Creates a copy of this {@code BitSet}.
     * 
     * @return a copy of this {@code BitSet}.
     * @since Android 1.0
     */
    @Override
    public Object clone() {
        try {
            BitSet clone = (BitSet) super.clone();
            clone.bits = bits.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Compares the argument to this {@code BitSet} and returns whether they are
     * equal. The object must be an instance of {@code BitSet} with the same
     * bits set.
     * 
     * @param obj
     *            the {@code BitSet} object to compare.
     * @return a {@code boolean} indicating whether or not this {@code BitSet} and
     *         {@code obj} are equal.
     * @see #hashCode
     * @since Android 1.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BitSet) {
            long[] bsBits = ((BitSet) obj).bits;
            int length1 = bits.length, length2 = bsBits.length;
            // If one of the BitSets is larger than the other, check to see if
            // any of
            // its extra bits are set. If so return false.
            if (length1 <= length2) {
                for (int i = 0; i < length1; i++) {
                    if (bits[i] != bsBits[i]) {
                        return false;
                    }
                }
                for (int i = length1; i < length2; i++) {
                    if (bsBits[i] != 0) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < length2; i++) {
                    if (bits[i] != bsBits[i]) {
                        return false;
                    }
                }
                for (int i = length2; i < length1; i++) {
                    if (bits[i] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Increase the size of the internal array to accommodate {@code pos} bits.
     * The new array max index will be a multiple of 64.
     * 
     * @param pos
     *            the index the new array needs to be able to access.
     * @since Android 1.0
     */
    private void growBits(int pos) {
        pos++; // Inc to get correct bit count
        long[] tempBits = new long[(pos / ELM_SIZE)
                + (pos % ELM_SIZE > 0 ? 1 : 0)];
        System.arraycopy(bits, 0, tempBits, 0, bits.length);
        bits = tempBits;
    }

    /**
     * Computes the hash code for this {@code BitSet}. If two {@code BitSet}s are equal
     * the have to return the same result for {@code hashCode()}.
     * 
     * @return the {@code int} representing the hash code for this bit
     *         set.
     * @see #equals
     * @see java.util.Hashtable
     * @since Android 1.0
     */
    @Override
    public int hashCode() {
        long x = 1234;
        // for (int i = 0, length = bits.length; i < length; i+=2)
        // x ^= (bits[i] + ((long)bits[i+1] << 32)) * (i/2 + 1);
        for (int i = 0, length = bits.length; i < length; i++) {
            x ^= bits[i] * (i + 1);
        }
        return (int) ((x >> 32) ^ x);
    }

    /**
     * Retrieves the bit at index {@code pos}. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to be retrieved.
     * @return {@code true} if the bit at {@code pos} is set,
     *         {@code false} otherwise.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     * @since Android 1.0
     */
    public boolean get(int pos) {
        if (pos >= 0) {
            if (pos < bits.length * ELM_SIZE) {
                return (bits[pos / ELM_SIZE] & (1L << (pos % ELM_SIZE))) != 0;
            }
            return false;
        }
        // Negative index specified
        throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
    }

    /**
     * Retrieves the bits starting from {@code pos1} to {@code pos2} and returns
     * back a new bitset made of these bits. Grows the {@code BitSet} if
     * {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @return new bitset of the range specified.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #get(int)
     * @since Android 1.0
     */
    public BitSet get(int pos1, int pos2) {
        if (pos1 >= 0 && pos2 >= 0 && pos2 >= pos1) {
            int last = (bits.length * ELM_SIZE);
            if (pos1 >= last || pos1 == pos2) {
                return new BitSet(0);
            }
            if (pos2 > last) {
                pos2 = last;
            }

            int idx1 = pos1 / ELM_SIZE;
            int idx2 = (pos2 - 1) / ELM_SIZE;
            long factor1 = (~0L) << (pos1 % ELM_SIZE);
            long factor2 = (~0L) >>> (ELM_SIZE - (pos2 % ELM_SIZE));

            if (idx1 == idx2) {
                long result = (bits[idx1] & (factor1 & factor2)) >>> (pos1 % ELM_SIZE);
                return new BitSet(new long[] { result });
            }
            long[] newbits = new long[idx2 - idx1 + 1];
            // first fill in the first and last indexes in the new bitset
            newbits[0] = bits[idx1] & factor1;
            newbits[newbits.length - 1] = bits[idx2] & factor2;

            // fill in the in between elements of the new bitset
            for (int i = 1; i < idx2 - idx1; i++) {
                newbits[i] = bits[idx1 + i];
            }

            // shift all the elements in the new bitset to the right by pos1
            // % ELM_SIZE
            int numBitsToShift = pos1 % ELM_SIZE;
            if (numBitsToShift != 0) {
                for (int i = 0; i < newbits.length; i++) {
                    // shift the current element to the right regardless of
                    // sign
                    newbits[i] = newbits[i] >>> (numBitsToShift);

                    // apply the last x bits of newbits[i+1] to the current
                    // element
                    if (i != newbits.length - 1) {
                        newbits[i] |= newbits[i + 1] << (ELM_SIZE - (numBitsToShift));
                    }
                }
            }
            return new BitSet(newbits);
        }
        throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
    }

    /**
     * Sets the bit at index {@code pos} to 1. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to set.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #clear(int)
     * @see #clear()
     * @see #clear(int, int)
     * @since Android 1.0
     */
    public void set(int pos) {
        if (pos >= 0) {
            if (pos >= bits.length * ELM_SIZE) {
                growBits(pos);
            }
            bits[pos / ELM_SIZE] |= 1L << (pos % ELM_SIZE);
        } else {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
        }
    }

    /**
     * Sets the bit at index {@code pos} to {@code val}. Grows the
     * {@code BitSet} if {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to set.
     * @param val
     *            value to set the bit.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #set(int)
     * @since Android 1.0
     */
    public void set(int pos, boolean val) {
        if (val) {
            set(pos);
        } else {
            clear(pos);
        }
    }

    /**
     * Sets the bits starting from {@code pos1} to {@code pos2}. Grows the
     * {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #set(int)
     * @since Android 1.0
     */
    public void set(int pos1, int pos2) {
        if (pos1 >= 0 && pos2 >= 0 && pos2 >= pos1) {
            if (pos1 == pos2) {
                return;
            }
            if (pos2 >= bits.length * ELM_SIZE) {
                growBits(pos2);
            }

            int idx1 = pos1 / ELM_SIZE;
            int idx2 = (pos2 - 1) / ELM_SIZE;
            long factor1 = (~0L) << (pos1 % ELM_SIZE);
            long factor2 = (~0L) >>> (ELM_SIZE - (pos2 % ELM_SIZE));

            if (idx1 == idx2) {
                bits[idx1] |= (factor1 & factor2);
            } else {
                bits[idx1] |= factor1;
                bits[idx2] |= factor2;
                for (int i = idx1 + 1; i < idx2; i++) {
                    bits[i] |= (~0L);
                }
            }
        } else {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
        }
    }

    /**
     * Sets the bits starting from {@code pos1} to {@code pos2} to the given
     * {@code val}. Grows the {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @param val
     *            value to set these bits.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #set(int,int)
     * @since Android 1.0
     */
    public void set(int pos1, int pos2, boolean val) {
        if (val) {
            set(pos1, pos2);
        } else {
            clear(pos1, pos2);
        }
    }

    /**
     * Clears all the bits in this {@code BitSet}.
     * 
     * @see #clear(int)
     * @see #clear(int, int)
     * @since Android 1.0
     */
    public void clear() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = 0L;
        }
    }

    /**
     * Clears the bit at index {@code pos}. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to clear.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #clear(int, int)
     * @since Android 1.0
     */
    public void clear(int pos) {
        if (pos >= 0) {
            if (pos < bits.length * ELM_SIZE) {
                bits[pos / ELM_SIZE] &= ~(1L << (pos % ELM_SIZE));
            }
        } else {
            // Negative index specified
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
        }
    }

    /**
     * Clears the bits starting from {@code pos1} to {@code pos2}. Grows the
     * {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #clear(int)
     * @since Android 1.0
     */
    public void clear(int pos1, int pos2) {
        if (pos1 >= 0 && pos2 >= 0 && pos2 >= pos1) {
            int last = (bits.length * ELM_SIZE);
            if (pos1 >= last || pos1 == pos2) {
                return;
            }
            if (pos2 > last) {
                pos2 = last;
            }

            int idx1 = pos1 / ELM_SIZE;
            int idx2 = (pos2 - 1) / ELM_SIZE;
            long factor1 = (~0L) << (pos1 % ELM_SIZE);
            long factor2 = (~0L) >>> (ELM_SIZE - (pos2 % ELM_SIZE));

            if (idx1 == idx2) {
                bits[idx1] &= ~(factor1 & factor2);
            } else {
                bits[idx1] &= ~factor1;
                bits[idx2] &= ~factor2;
                for (int i = idx1 + 1; i < idx2; i++) {
                    bits[i] = 0L;
                }
            }
        } else {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
        }
    }

    /**
     * Flips the bit at index {@code pos}. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to flip.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #flip(int, int)
     * @since Android 1.0
     */
    public void flip(int pos) {
        if (pos >= 0) {
            if (pos >= bits.length * ELM_SIZE) {
                growBits(pos);
            }
            bits[pos / ELM_SIZE] ^= 1L << (pos % ELM_SIZE);
        } else {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
        }
    }

    /**
     * Flips the bits starting from {@code pos1} to {@code pos2}. Grows the
     * {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #flip(int)
     * @since Android 1.0
     */
    public void flip(int pos1, int pos2) {
        if (pos1 >= 0 && pos2 >= 0 && pos2 >= pos1) {
            if (pos1 == pos2) {
                return;
            }
            if (pos2 >= bits.length * ELM_SIZE) {
                growBits(pos2);
            }

            int idx1 = pos1 / ELM_SIZE;
            int idx2 = (pos2 - 1) / ELM_SIZE;
            long factor1 = (~0L) << (pos1 % ELM_SIZE);
            long factor2 = (~0L) >>> (ELM_SIZE - (pos2 % ELM_SIZE));

            if (idx1 == idx2) {
                bits[idx1] ^= (factor1 & factor2);
            } else {
                bits[idx1] ^= factor1;
                bits[idx2] ^= factor2;
                for (int i = idx1 + 1; i < idx2; i++) {
                    bits[i] ^= (~0L);
                }
            }
        } else {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
        }
    }

    /**
     * Checks if these two {@code BitSet}s have at least one bit set to true in the same
     * position.
     * 
     * @param bs
     *            {@code BitSet} used to calculate the intersection.
     * @return {@code true} if bs intersects with this {@code BitSet},
     *         {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean intersects(BitSet bs) {
        long[] bsBits = bs.bits;
        int length1 = bits.length, length2 = bsBits.length;

        if (length1 <= length2) {
            for (int i = 0; i < length1; i++) {
                if ((bits[i] & bsBits[i]) != 0L) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < length2; i++) {
                if ((bits[i] & bsBits[i]) != 0L) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs the logical AND of this {@code BitSet} with another 
     * {@code BitSet}. The values of this {@code BitSet} are changed accordingly.
     * 
     * @param bs
     *            {@code BitSet} to AND with.
     * @see #or
     * @see #xor
     * @since Android 1.0
     */

    public void and(BitSet bs) {
        long[] bsBits = bs.bits;
        int length1 = bits.length, length2 = bsBits.length;
        if (length1 <= length2) {
            for (int i = 0; i < length1; i++) {
                bits[i] &= bsBits[i];
            }
        } else {
            for (int i = 0; i < length2; i++) {
                bits[i] &= bsBits[i];
            }
            for (int i = length2; i < length1; i++) {
                bits[i] = 0;
            }
        }
    }

    /**
     * Clears all bits in the receiver which are also set in the parameter
     * {@code BitSet}. The values of this {@code BitSet} are changed accordingly.
     * 
     * @param bs
     *            {@code BitSet} to ANDNOT with.
     * @since Android 1.0
     */
    public void andNot(BitSet bs) {
        long[] bsBits = bs.bits;
        int range = bits.length < bsBits.length ? bits.length : bsBits.length;
        for (int i = 0; i < range; i++) {
            bits[i] &= ~bsBits[i];
        }
    }

    /**
     * Performs the logical OR of this {@code BitSet} with another {@code BitSet}.
     * The values of this {@code BitSet} are changed accordingly.
     * 
     * @param bs
     *            {@code BitSet} to OR with.
     * @see #xor
     * @see #and
     * @since Android 1.0
     */
    public void or(BitSet bs) {
        int nbits = bs.length();
        int length = nbits / ELM_SIZE + (nbits % ELM_SIZE > 0 ? 1 : 0);
        if (length > bits.length) {
            growBits(nbits - 1);
        }
        long[] bsBits = bs.bits;
        for (int i = 0; i < length; i++) {
            bits[i] |= bsBits[i];
        }
    }

    /**
     * Performs the logical XOR of this {@code BitSet} with another {@code BitSet}.
     * The values of this {@code BitSet} are changed accordingly.
     * 
     * @param bs
     *            {@code BitSet} to XOR with.
     * @see #or
     * @see #and
     * @since Android 1.0
     */
    public void xor(BitSet bs) {
        int nbits = bs.length();
        int length = nbits / ELM_SIZE + (nbits % ELM_SIZE > 0 ? 1 : 0);
        if (length > bits.length) {
            growBits(nbits - 1);
        }
        long[] bsBits = bs.bits;
        for (int i = 0; i < length; i++) {
            bits[i] ^= bsBits[i];
        }

    }

    /**
     * Returns the number of bits this {@code BitSet} has.
     * 
     * @return the number of bits contained in this {@code BitSet}.
     * @see #length
     * @since Android 1.0
     */
    public int size() {
        return bits.length * ELM_SIZE;
    }

    /**
     * Returns the number of bits up to and including the highest bit set.
     * 
     * @return the length of the {@code BitSet}.
     * @since Android 1.0
     */
    public int length() {
        int idx = bits.length - 1;
        while (idx >= 0 && bits[idx] == 0) {
            --idx;
        }
        if (idx == -1) {
            return 0;
        }
        int i = ELM_SIZE - 1;
        long val = bits[idx];
        while ((val & (1L << i)) == 0 && i > 0) {
            i--;
        }
        return idx * ELM_SIZE + i + 1;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a comma delimited list of the indices of all bits that are set.
     * @since Android 1.0
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(bits.length / 2);
        int bitCount = 0;
        sb.append('{');
        boolean comma = false;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == 0) {
                bitCount += ELM_SIZE;
                continue;
            }
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((bits[i] & (1L << j)) != 0)) {
                    if (comma) {
                        sb.append(", "); //$NON-NLS-1$
                    }
                    sb.append(bitCount);
                    comma = true;
                }
                bitCount++;
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns the position of the first bit that is {@code true} on or after {@code pos}.
     * 
     * @param pos
     *            the starting position (inclusive).
     * @return -1 if there is no bits that are set to {@code true} on or after {@code pos}.
     * @since Android 1.0
     */
    public int nextSetBit(int pos) {
        if (pos >= 0) {
            if (pos >= bits.length * ELM_SIZE) {
                return -1;
            }

            int idx = pos / ELM_SIZE;
            // first check in the same bit set element
            if (bits[idx] != 0L) {
                for (int j = pos % ELM_SIZE; j < ELM_SIZE; j++) {
                    if (((bits[idx] & (1L << j)) != 0)) {
                        return idx * ELM_SIZE + j;
                    }
                }

            }
            idx++;
            while (idx < bits.length && bits[idx] == 0L) {
                idx++;
            }
            if (idx == bits.length) {
                return -1;
            }

            // we know for sure there is a bit set to true in this element
            // since the bitset value is not 0L
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((bits[idx] & (1L << j)) != 0)) {
                    return idx * ELM_SIZE + j;
                }
            }

            return -1;
        }
        throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
    }

    /**
     * Returns the position of the first bit that is {@code false} on or after {@code pos}.
     * 
     * @param pos
     *            the starting position (inclusive).
     * @return the position of the next bit set to {@code false}, even if it is further
     *         than this {@code BitSet}'s size.
     * @since Android 1.0
     */
    public int nextClearBit(int pos) {
        if (pos >= 0) {
            int bssize = bits.length * ELM_SIZE;
            if (pos >= bssize) {
                return pos;
            }

            int idx = pos / ELM_SIZE;
            // first check in the same bit set element
            if (bits[idx] != (~0L)) {
                for (int j = pos % ELM_SIZE; j < ELM_SIZE; j++) {
                    if (((bits[idx] & (1L << j)) == 0)) {
                        return idx * ELM_SIZE + j;
                    }
                }

            }
            idx++;
            while (idx < bits.length && bits[idx] == (~0L)) {
                idx++;
            }
            if (idx == bits.length) {
                return bssize;
            }

            // we know for sure there is a bit set to true in this element
            // since the bitset value is not 0L
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((bits[idx] & (1L << j)) == 0)) {
                    return idx * ELM_SIZE + j;
                }
            }

            return bssize;
        }
        throw new IndexOutOfBoundsException(Msg.getString("K0006")); //$NON-NLS-1$
    }

    /**
     * Returns true if all the bits in this {@code BitSet} are set to false.
     * 
     * @return {@code true} if the {@code BitSet} is empty,
     *         {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean isEmpty() {
        for (int idx = 0; idx < bits.length; idx++) {
            if (bits[idx] != 0L) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the number of bits that are {@code true} in this {@code BitSet}.
     * 
     * @return the number of {@code true} bits in the set.
     * @since Android 1.0
     */
    public int cardinality() {
        int count = 0;
        for (int idx = 0; idx < bits.length; idx++) {
            long temp = bits[idx];
            if (temp != 0L) {
                for (int i = 0; i < ELM_SIZE; i++) {
                    if ((temp & (1L << i)) != 0L) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}

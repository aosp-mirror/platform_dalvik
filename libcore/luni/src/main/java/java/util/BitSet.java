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
 * The BitSet class implements a bit field. Each element in a BitSet can be
 * on(1) or off(0). A BitSet is created with a given size and grows when this
 * size is exceeded. Growth is always rounded to a 64 bit boundary.
 */
public class BitSet implements Serializable, Cloneable {
    private static final long serialVersionUID = 7997698588986878753L;

    // Size in bits of the data type being used in the bits array
    private static final int ELM_SIZE = 64;

    private long[] bits;

    /**
     * Create a new BitSet with size equal to 64 bits
     * 
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     */
    public BitSet() {
        this(64);
    }

    /**
     * Create a new BitSet with size equal to nbits. If nbits is not a multiple
     * of 64, then create a BitSet with size nbits rounded to the next closest
     * multiple of 64.
     * 
     * @param nbits
     *            the size of the bit set
     * @throws NegativeArraySizeException
     *             if nbits < 0.
     * 
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
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
     * Create a copy of this BitSet
     * 
     * @return A copy of this BitSet.
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
     * Compares the argument to this BitSet and answer if they are equal. The
     * object must be an instance of BitSet with the same bits set.
     * 
     * @param obj
     *            the <code>BitSet</code> object to compare
     * @return A boolean indicating whether or not this BitSet and obj are equal
     * 
     * @see #hashCode
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
     * Increase the size of the internal array to accommodate pos bits. The new
     * array max index will be a multiple of 64
     * 
     * @param pos
     *            the index the new array needs to be able to access
     */
    private void growBits(int pos) {
        pos++; // Inc to get correct bit count
        long[] tempBits = new long[(pos / ELM_SIZE)
                + (pos % ELM_SIZE > 0 ? 1 : 0)];
        System.arraycopy(bits, 0, tempBits, 0, bits.length);
        bits = tempBits;
    }

    /**
     * Computes the hash code for this BitSet.
     * 
     * @return The <code>int</code> representing the hash code for this bit
     *         set.
     * 
     * @see #equals
     * @see java.util.Hashtable
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
     * Retrieve the bit at index pos. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            the index of the bit to be retrieved
     * @return <code>true</code> if the bit at <code>pos</code> is set,
     *         <code>false</code> otherwise
     * @throws IndexOutOfBoundsException
     *             when <code>pos</code> < 0
     * 
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
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
     * Retrieves the bits starting from pos1 to pos2 and returns back a new
     * bitset made of these bits. Grows the BitSet if pos2 > size.
     * 
     * @param pos1
     *            beginning position
     * @param pos2
     *            ending position
     * @return new bitset
     * @throws IndexOutOfBoundsException
     *             when pos1 or pos2 is negative, or when pos2 is not smaller
     *             than pos1
     * 
     * @see #get(int)
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
     * Sets the bit at index pos to 1. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            the index of the bit to set
     * @throws IndexOutOfBoundsException
     *             when pos < 0
     * 
     * @see #clear(int)
     * @see #clear()
     * @see #clear(int, int)
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
     * Sets the bit at index pos to the value. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            the index of the bit to set
     * @param val
     *            value to set the bit
     * @throws IndexOutOfBoundsException
     *             when pos < 0
     * 
     * @see #set(int)
     */
    public void set(int pos, boolean val) {
        if (val) {
            set(pos);
        } else {
            clear(pos);
        }
    }

    /**
     * Sets the bits starting from pos1 to pos2. Grows the BitSet if pos2 >
     * size.
     * 
     * @param pos1
     *            beginning position
     * @param pos2
     *            ending position
     * @throws IndexOutOfBoundsException
     *             when pos1 or pos2 is negative, or when pos2 is not smaller
     *             than pos1
     * 
     * @see #set(int)
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
     * Sets the bits starting from pos1 to pos2 to the given boolean value.
     * Grows the BitSet if pos2 > size.
     * 
     * @param pos1
     *            beginning position
     * @param pos2
     *            ending position
     * @param val
     *            value to set these bits
     * 
     * @throws IndexOutOfBoundsException
     *             when pos1 or pos2 is negative, or when pos2 is not smaller
     *             than pos1
     * @see #set(int,int)
     */
    public void set(int pos1, int pos2, boolean val) {
        if (val) {
            set(pos1, pos2);
        } else {
            clear(pos1, pos2);
        }
    }

    /**
     * Clears all the bits in this bitset.
     * 
     * @see #clear(int)
     * @see #clear(int, int)
     */
    public void clear() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = 0L;
        }
    }

    /**
     * Clears the bit at index pos. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            the index of the bit to clear
     * @throws IndexOutOfBoundsException
     *             when pos < 0
     * 
     * @see #clear(int, int)
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
     * Clears the bits starting from pos1 to pos2. Grows the BitSet if pos2 >
     * size.
     * 
     * @param pos1
     *            beginning position
     * @param pos2
     *            ending position
     * @throws IndexOutOfBoundsException
     *             when pos1 or pos2 is negative, or when pos2 is not smaller
     *             than pos1
     * 
     * @see #clear(int)
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
     * Flips the bit at index pos. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            the index of the bit to flip
     * 
     * @throws IndexOutOfBoundsException
     *             when pos < 0
     * @see #flip(int, int)
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
     * Flips the bits starting from pos1 to pos2. Grows the BitSet if pos2 >
     * size.
     * 
     * @param pos1
     *            beginning position
     * @param pos2
     *            ending position
     * @throws IndexOutOfBoundsException
     *             when pos1 or pos2 is negative, or when pos2 is not smaller
     *             than pos1
     * 
     * @see #flip(int)
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
     * Checks if these two bitsets have at least one bit set to true in the same
     * position.
     * 
     * @param bs
     *            BitSet used to calculate intersect
     * @return <code>true</code> if bs intersects with this BitSet,
     *         <code>false</code> otherwise
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
     * Performs the logical AND of this BitSet with another BitSet.
     * 
     * @param bs
     *            BitSet to AND with
     * 
     * @see #or
     * @see #xor
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
     * BitSet.
     * 
     * @param bs
     *            BitSet to ANDNOT with
     */
    public void andNot(BitSet bs) {
        long[] bsBits = bs.bits;
        int range = bits.length < bsBits.length ? bits.length : bsBits.length;
        for (int i = 0; i < range; i++) {
            bits[i] &= ~bsBits[i];
        }
    }

    /**
     * Performs the logical OR of this BitSet with another BitSet.
     * 
     * @param bs
     *            BitSet to OR with
     * 
     * @see #xor
     * @see #and
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
     * Performs the logical XOR of this BitSet with another BitSet.
     * 
     * @param bs
     *            BitSet to XOR with
     * 
     * @see #or
     * @see #and
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
     * Returns the number of bits this bitset has.
     * 
     * @return The number of bits contained in this BitSet.
     * 
     * @see #length
     */
    public int size() {
        return bits.length * ELM_SIZE;
    }

    /**
     * Returns the number of bits up to and including the highest bit set.
     * 
     * @return the length of the BitSet
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
     * @return A comma delimited list of the indices of all bits that are set.
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
     * Returns the position of the first bit that is true on or after pos
     * 
     * @param pos
     *            the starting position (inclusive)
     * @return -1 if there is no bits that are set to true on or after pos.
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
     * Returns the position of the first bit that is false on or after pos
     * 
     * @param pos
     *            the starting position (inclusive)
     * @return the position of the next bit set to false, even if it is further
     *         than this bitset's size.
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
     * Returns true if all the bits in this bitset are set to false.
     * 
     * @return <code>true</code> if the BitSet is empty, <code>false</code>
     *         otherwise
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
     * Returns the number of bits that are true in this bitset.
     * 
     * @return the number of true bits in the set
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

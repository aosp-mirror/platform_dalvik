/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.support;

public class Support_BitSet {
    private long[] bits;

    private static final int ELM_SIZE = 64; // Size in bits of the data type

    // being used in the bits array

    /**
     * Create a new BitSet with size equal to 64 bits
     * 
     * @return The number of bits contained in this BitSet.
     * 
     * @see #clear
     * @see #set
     */
    public Support_BitSet() {
        this(64);
    }

    /**
     * Create a new BitSet with size equal to nbits. If nbits is not a multiple
     * of 64, then create a BitSet with size nbits rounded to the next closest
     * multiple of 64.
     * 
     * @exception NegativeArraySizeException
     *                if nbits < 0.
     * @see #clear
     * @see #set
     */
    public Support_BitSet(int nbits) {
        if (nbits >= 0) {
            bits = new long[(nbits / ELM_SIZE) + (nbits % ELM_SIZE > 0 ? 1 : 0)];
        } else {
            throw new NegativeArraySizeException();
        }
    }

    /**
     * Clears the bit at index pos. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            int
     * 
     * @exception IndexOutOfBoundsException
     *                when pos < 0
     * @see #set
     */
    public void clear(int pos) {
        if (pos >= 0) {
            if (pos < bits.length * ELM_SIZE) {
                bits[pos / ELM_SIZE] &= ~(1L << (pos % ELM_SIZE));
            } else {
                growBits(pos); // Bit is cleared for free if we have to grow
            }
        } else {
            throw new IndexOutOfBoundsException("Negative index specified");
        }
    }

    /**
     * Retrieve the bit at index pos. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            int
     * @return A boolean value indicating whether the bit at pos has been set.
     *         Answers false if pos > size().
     * 
     * @exception IndexOutOfBoundsException
     *                when pos < 0
     * @see #set
     * @see #clear
     */
    public boolean get(int pos) {
        if (pos >= 0) {
            if (pos < bits.length * ELM_SIZE) {
                return (bits[pos / ELM_SIZE] & (1L << (pos % ELM_SIZE))) != 0;
            }
            return false;
        }
        throw new IndexOutOfBoundsException("Negative index specified");
    }

    /**
     * Increase the size of the internal array to accomodate pos bits. The new
     * array max index will be a multiple of 64
     * 
     * @param pos
     *            int The index the new array needs to be able to access
     */
    private void growBits(int pos) {
        pos++; // Inc to get correct bit count
        long[] tempBits = new long[(pos / ELM_SIZE)
                + (pos % ELM_SIZE > 0 ? 1 : 0)];
        System.arraycopy(bits, 0, tempBits, 0, bits.length);
        bits = tempBits;
    }

    /**
     * Sets the bit at index pos to 1. Grows the BitSet if pos > size.
     * 
     * @param pos
     *            int
     * 
     * @exception IndexOutOfBoundsException
     *                when pos < 0
     * @see #clear
     */
    public void set(int pos) {
        if (pos >= 0) {
            if (pos >= bits.length * ELM_SIZE) {
                growBits(pos);
            }
            bits[pos / ELM_SIZE] |= 1L << (pos % ELM_SIZE);
        } else {
            throw new IndexOutOfBoundsException("Negative index specified");
        }
    }

    /**
     * Clears the bit at index pos.
     * 
     * @return The number of bits contained in this BitSet.
     * 
     * @see #BitSet
     * @see #clear
     * @see #set
     */
    public int size() {
        return bits.length * ELM_SIZE;
    }

    /**
     * Answers a string containing a concise, human-readable description of the
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
        for (long element : bits) {
            if (element == 0) {
                bitCount += ELM_SIZE;
                continue;
            }
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((element & (1L << j)) != 0)) {
                    if (comma) {
                        sb.append(", ");
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
     * Returns the number of bits up to and including the highest bit set.
     * 
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
}

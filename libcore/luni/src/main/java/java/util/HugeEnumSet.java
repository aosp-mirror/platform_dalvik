/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.util;


/**
 * This is a concrete subclass of EnumSet designed specifically for enum type
 * with more than 64 elements.
 * 
 */
@SuppressWarnings("serial")
final class HugeEnumSet<E extends Enum<E>> extends EnumSet<E> {
    
    final private E[] enums;
    
    private long[] bits;
    
    private int size;
    
    private static final int BIT_IN_LONG = 64;
    
    // BEGIN android-changed
    /**
     * Constructs an instance.
     * 
     * @param elementType non-null; type of the elements
     * @param enums non-null; prepopulated array of constants in ordinal
     * order
     */
    HugeEnumSet(Class<E> elementType, E[] enums) {
        super(elementType);
        this.enums = enums;
        bits = new long[(enums.length + BIT_IN_LONG - 1) / BIT_IN_LONG];
        Arrays.fill(bits, 0);
    }
    // END android-changed
    
    private class HugeEnumSetIterator implements Iterator<E> {

        private long[] unProcessedBits;

        private int bitsPosition = 0;

        /*
         * Mask for current element.
         */
        private long currentElementMask = 0;

        boolean canProcess = true;

        private HugeEnumSetIterator() {
            unProcessedBits = new long[bits.length];
            System.arraycopy(bits, 0, unProcessedBits, 0, bits.length);
            bitsPosition = unProcessedBits.length;
            findNextNoneZeroPosition(0);
            if (bitsPosition == unProcessedBits.length) {
                canProcess = false;
            }
        }

        private void findNextNoneZeroPosition(int start) {
            for (int i = start; i < unProcessedBits.length; i++) {
                if (0 != bits[i]) {
                    bitsPosition = i;
                    break;
                }
            }
        }

        public boolean hasNext() {
            return canProcess;
        }

        public E next() {
            if (!canProcess) {
                throw new NoSuchElementException();
            }
            currentElementMask = unProcessedBits[bitsPosition]
                    & (-unProcessedBits[bitsPosition]);
            unProcessedBits[bitsPosition] -= currentElementMask;
            if (0 == unProcessedBits[bitsPosition]) {
                int oldBitsPosition = bitsPosition;
                findNextNoneZeroPosition(bitsPosition + 1);
                if (bitsPosition == oldBitsPosition) {
                    canProcess = false;
                }
            }
            return enums[Long.numberOfTrailingZeros(currentElementMask)
                    + bitsPosition * BIT_IN_LONG];
        }

        public void remove() {
            if (currentElementMask == 0) {
                throw new IllegalStateException();
            }
            bits[bitsPosition] &= ~currentElementMask;
            size--;
            currentElementMask = 0;
        }
    }
    
    @Override
    public boolean add(E element) {
        if (!isValidType(element.getDeclaringClass())) {
            throw new ClassCastException();
        }
        calculateElementIndex(element);

        bits[bitsIndex] |= (1l << elementInBits);
        if (oldBits == bits[bitsIndex]) {
            return false;
        }
        size++;
        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (0 == collection.size() || this == collection) {
            return false;
        }
        if (collection instanceof EnumSet) {
            EnumSet set = (EnumSet) collection;
            if (!isValidType(set.elementClass)) {
                throw new ClassCastException();
            }
            HugeEnumSet hugeSet = (HugeEnumSet) set;
            boolean addSuccessful = false;
            for (int i = 0; i < bits.length; i++) {
                oldBits = bits[i];
                bits[i] |= hugeSet.bits[i];
                if (oldBits != bits[i]) {
                    addSuccessful = true;
                    size = size - Long.bitCount(oldBits)
                            + Long.bitCount(bits[i]);
                }
            }
            return addSuccessful;
        }
        return super.addAll(collection);
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        Arrays.fill(bits, 0);
        size = 0;
    }
    
    @Override
    protected void complement() {
        if (0 != enums.length) {
            bitsIndex = enums.length / BIT_IN_LONG;

            size = 0;
            int bitCount = 0;
            for (int i = 0; i <= bitsIndex; i++) {
                bits[i] = ~bits[i];
                bitCount = Long.bitCount(bits[i]);
                size += bitCount;
            }
            bits[bitsIndex] &= (-1l >>> (BIT_IN_LONG - enums.length
                    % BIT_IN_LONG));
            size -= bitCount;
            bitCount = Long.bitCount(bits[bitsIndex]);
            size += bitCount;
        }
    }
    
    @Override
    public boolean contains(Object object) {
        if (null == object) {
            return false;
        }
        if (!isValidType(object.getClass())) {
            return false;
        }
        calculateElementIndex((E)object);
        return (bits[bitsIndex] & (1l << elementInBits)) != 0;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public HugeEnumSet<E> clone() {
        Object set = super.clone();
        if (null != set) {
            ((HugeEnumSet<E>) set).bits = bits.clone();
            return (HugeEnumSet<E>) set;
        }
        return null;
    }
    
    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.size() == 0) {
            return true;
        }
        if (collection instanceof HugeEnumSet) {
            HugeEnumSet set = (HugeEnumSet) collection;
            if(isValidType(set.elementClass )) {
                for(int i = 0; i < bits.length; i++) {
                    if((bits[i] & set.bits[i]) != set.bits[i]){
                        return false;
                    }
                    
                }
                return true;
            }
        }
        return !(collection instanceof EnumSet) && super.containsAll(collection);
    }
    
    @Override
    public boolean equals(Object object) {
        if (null == object) {
            return false;
        }
        if (!isValidType(object.getClass())) {
            return super.equals(object);
        }
        return Arrays.equals(bits, ((HugeEnumSet) object).bits);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new HugeEnumSetIterator();
    }
    
    @Override
    public boolean remove(Object object) {
        if (!contains(object)) {
            return false;
        }
        bits[bitsIndex] -= (1l << elementInBits);
        size--;
        return true;
    }
    
    @Override
    public boolean removeAll(Collection<?> collection) {
        if (0 == collection.size()) {
            return false;
        }
        
        if (collection instanceof EnumSet) {
            EnumSet<E> set = (EnumSet<E>) collection;
            if (!isValidType(set.elementClass)) {
                return false;
            }
            boolean removeSuccessful = false;
            long mask = 0;
            for (int i = 0; i < bits.length; i++) {
                oldBits = bits[i];
                mask = bits[i] & ((HugeEnumSet<E>) set).bits[i];
                if (mask != 0) {
                    bits[i] -= mask;
                    size = (size - Long.bitCount(oldBits) + Long
                            .bitCount(bits[i]));
                    removeSuccessful = true;
                }
            }
            return removeSuccessful;
        }
        return super.removeAll(collection);
    }
    
    @Override
    public boolean retainAll(Collection<?> collection) {
        if (collection instanceof EnumSet) {
            EnumSet<E> set = (EnumSet<E>) collection;
            if (!isValidType(set.elementClass)) {
                clear();
                return true;
            }

            boolean retainSuccessful = false;
            oldBits = 0;
            for (int i = 0; i < bits.length; i++) {
                oldBits = bits[i];
                bits[i] &= ((HugeEnumSet<E>) set).bits[i];
                if (oldBits != bits[i]) {
                    size = size - Long.bitCount(oldBits)
                            + Long.bitCount(bits[i]);
                    retainSuccessful = true;
                }
            }
            return retainSuccessful;
        }
        return super.retainAll(collection);
    }
    
    @Override
    void setRange(E start, E end) {
        calculateElementIndex(start);
        int startBitsIndex = bitsIndex;
        int startElementInBits = elementInBits;
        calculateElementIndex(end);
        int endBitsIndex = bitsIndex;
        int endElementInBits = elementInBits;
        long range = 0;
        if (startBitsIndex == endBitsIndex) {
            range = (-1l >>> (BIT_IN_LONG -(endElementInBits - startElementInBits + 1))) << startElementInBits;
            size -= Long.bitCount(bits[bitsIndex]);
            bits[bitsIndex] |= range;
            size += Long.bitCount(bits[bitsIndex]);
        } else {
            range = (-1l >>> startElementInBits) << startElementInBits;
            size -= Long.bitCount(bits[startBitsIndex]);
            bits[startBitsIndex] |= range;
            size += Long.bitCount(bits[startBitsIndex]);

            // endElementInBits + 1 is the number of consecutive ones.
            // 63 - endElementInBits is the following zeros of the right most one.
            range = -1l >>> (BIT_IN_LONG - (endElementInBits + 1)) << (63 - endElementInBits);
            size -= Long.bitCount(bits[endBitsIndex]);
            bits[endBitsIndex] |= range;
            size += Long.bitCount(bits[endBitsIndex]);
            for(int i = (startBitsIndex + 1); i <= (endBitsIndex - 1); i++) {
                size -= Long.bitCount(bits[i]);
                bits[i] = -1l;
                size += Long.bitCount(bits[i]);
            }
        }
    }
    
    private void calculateElementIndex(E element) {
        int elementOrdinal = element.ordinal();
        bitsIndex = elementOrdinal / BIT_IN_LONG;
        elementInBits = elementOrdinal % BIT_IN_LONG;
        oldBits = bits[bitsIndex];
    }
    
    private int bitsIndex;

    private int elementInBits;

    private long oldBits;
}

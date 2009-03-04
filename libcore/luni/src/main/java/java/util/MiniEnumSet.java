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
 * with less than or equal to 64 elements.
 * 
 */
@SuppressWarnings("serial")
final class MiniEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final int MAX_ELEMENTS = 64;
    
    private int size;
    
    private final E[] enums;    
    
    private long bits;
    
    // BEGIN android-changed
    /**
     * Constructs an instance.
     * 
     * @param elementType non-null; type of the elements
     * @param enums non-null; prepopulated array of constants in ordinal
     * order
     */
    MiniEnumSet(Class<E> elementType, E[] enums) {
        super(elementType);
        this.enums = enums;
    }
    // END android-changed
    
    private class MiniEnumSetIterator implements Iterator<E> {

        private long unProcessedBits;

        /*
         * Mask for current element.
         */
        private long currentElementMask;

        private boolean canProcess = true;

        private MiniEnumSetIterator() {
            unProcessedBits = bits;
            if (0 == unProcessedBits) {
                canProcess = false;
            }
        }

        public boolean hasNext() {
            return canProcess;
        }

        public E next() {
            if (!canProcess) {
                throw new NoSuchElementException();
            }
            currentElementMask = unProcessedBits & (-unProcessedBits);
            unProcessedBits -= currentElementMask;
            if (0 == unProcessedBits) {
                canProcess = false;
            }
            return enums[Long.numberOfTrailingZeros(currentElementMask)];
        }

        public void remove() {
            if ( currentElementMask == 0 ) {
                throw new IllegalStateException();
            }
            bits &= ~currentElementMask;
            size = Long.bitCount(bits);
            currentElementMask = 0;
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new MiniEnumSetIterator();
    }

    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        bits = 0;
        size = 0;
    }
    
    @Override
    public boolean add(E element) {
        if (!isValidType(element.getDeclaringClass())) {
            throw new ClassCastException();
        }
        long mask = 1l << element.ordinal();
        if ((bits & mask) == mask) {
            return false;
        }
        bits |= mask;

        size++;
        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (0 == collection.size()) {
            return false;
        }
        if (collection instanceof EnumSet) {
            EnumSet<?> set = (EnumSet)collection;
            if (!isValidType(set.elementClass)) {
                throw new ClassCastException();
            }
            MiniEnumSet<?> miniSet = (MiniEnumSet<?>) set;
            long oldBits = bits;
            bits |= miniSet.bits;
            size = Long.bitCount(bits);
            return (oldBits != bits);
        }
        return super.addAll(collection);
    }
    
    @Override
    public boolean contains(Object object) {
        if (null == object) {
            return false;
        }
        if (!isValidType(object.getClass())) {
            return false;
        }
        Enum<?> element = (Enum<?>) object;
        int ordinal = element.ordinal();
        return (bits & (1l << ordinal)) != 0;
    }
    
    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.size() == 0) {
            return true;
        }
        if (collection instanceof MiniEnumSet) {
            MiniEnumSet<?> set = (MiniEnumSet<?>) collection;
            return isValidType(set.elementClass ) && ((bits & set.bits) == set.bits);
        }
        return !(collection instanceof EnumSet) && super.containsAll(collection);  
    }
    
    @Override
    public boolean removeAll(Collection<?> collection) {
        if (0 == collection.size()) {
            return false;
        }
        if (collection instanceof EnumSet) {
            EnumSet<E> set = (EnumSet<E>) collection;
            boolean removeSuccessful = false;
            if (isValidType(set.elementClass)) {
                long mask = bits & ((MiniEnumSet<E>) set).bits;
                if (mask != 0) {
                    bits -= mask;
                    size = Long.bitCount(bits);
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
            long oldBits = bits;
            bits &= ((MiniEnumSet<E>)set).bits;
            if (oldBits != bits) {
                size = Long.bitCount(bits);
                retainSuccessful = true;
            }
            return retainSuccessful;
        }
        return super.retainAll(collection);
    }
    
    @Override
    public boolean remove(Object object) {
        if (!contains(object)) {
            return false;
        }
        Enum<?> element = (Enum<?>) object;
        int ordinal = element.ordinal();
        bits -= (1l << ordinal);
        size--;
        return true;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EnumSet)) {
            return super.equals(object);
        }
        EnumSet<?> set =(EnumSet<?>)object; 
        if( !isValidType(set.elementClass) ) {
            return size == 0 && set.size() == 0;
        }
        return bits == ((MiniEnumSet<?>)set).bits;
    }
    
    @Override
    void complement() {
        if (0 != enums.length) {
            bits = ~bits;
            bits &= (-1l >>> (MAX_ELEMENTS - enums.length));
            size = enums.length - size;
        }
    }
    
    @Override
    void setRange(E start, E end) {
        int length = end.ordinal() - start.ordinal() + 1;
        long range = (-1l >>> (MAX_ELEMENTS - length)) << start.ordinal();
        bits |= range;
        size = Long.bitCount(bits);
    }
}

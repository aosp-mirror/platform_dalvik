package org.bouncycastle.asn1;

import java.util.Enumeration;
import java.util.ConcurrentModificationException;

// BEGIN android-note
/*
 * This is a new class that was synthesized from the observed
 * requirement for a lookup table that preserves order. Since in
 * practice the element count is typically very low, we just use a
 * flat list rather than doing any hashing / bucketing.
 */
// END android-note

/**
 * Ordered lookup table. Instances of this class will keep up to four
 * key-value pairs directly, resorting to an external collection only
 * if more elements than that need to be stored.
 */
public final class OrderedTable {
    /** null-ok; key #0 */
    private DERObjectIdentifier key0;
    
    /** null-ok; key #1 */
    private DERObjectIdentifier key1;

    /** null-ok; key #2 */
    private DERObjectIdentifier key2;

    /** null-ok; key #3 */
    private DERObjectIdentifier key3;
    
    /** null-ok; value #0 */
    private Object value0;
    
    /** null-ok; value #1 */
    private Object value1;

    /** null-ok; value #2 */
    private Object value2;

    /** null-ok; value #3 */
    private Object value3;
    
    /**
     * null-ok; array of additional keys and values, alternating
     * key then value, etc. 
     */
    private Object[] rest;

    /** &gt;= 0; number of elements in the list */
    private int size;

    // Note: Default public constructor.

    /**
     * Adds an element.
     * 
     * @param key non-null; the key
     * @param value non-null; the value
     */
    public void add(DERObjectIdentifier key, Object value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        if (value == null) {
            throw new NullPointerException("value == null");
        }

        int sz = size;

        switch (sz) {
            case 0: {
                key0 = key;
                value0 = value;
                break;
            }
            case 1: {
                key1 = key;
                value1 = value;
                break;
            }
            case 2: {
                key2 = key;
                value2 = value;
                break;
            }
            case 3: {
                key3 = key;
                value3 = value;
                break;
            }
            case 4: {
                // Do initial allocation of rest.
                rest = new Object[10];
                rest[0] = key;
                rest[1] = value;
                break;
            }
            default: {
                int index = (sz - 4) * 2;
                int index1 = index + 1;
                if (index1 >= rest.length) {
                    // Grow rest.
                    Object[] newRest = new Object[index1 * 2 + 10];
                    System.arraycopy(rest, 0, newRest, 0, rest.length);
                    rest = newRest;
                }
                rest[index] = key;
                rest[index1] = value;
                break;
            }
        }
        
        size = sz + 1;
    }

    /**
     * Gets the number of elements in this instance.
     */
    public int size() {
        return size;
    }

    /**
     * Look up the given key, returning the associated value if found.
     * 
     * @param key non-null; the key to look up
     * @return null-ok; the associated value
     */
    public Object get(DERObjectIdentifier key) {
        int keyHash = key.hashCode();
        int sz = size;

        for (int i = 0; i < size; i++) {
            DERObjectIdentifier probe = getKey(i);
            if ((probe.hashCode() == keyHash) &&
                    probe.equals(key)) {
                return getValue(i);
            }
        }

        return null;
    }
    
    /**
     * Gets the nth key.
     * 
     * @param n index
     * @return non-null; the nth key
     */
    public DERObjectIdentifier getKey(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }

        switch (n) {
            case 0: return key0;
            case 1: return key1;
            case 2: return key2;
            case 3: return key3;
            default: return (DERObjectIdentifier) rest[(n - 4) * 2];
        }
    }

    /**
     * Gets the nth value.
     * 
     * @param n index
     * @return non-null; the nth value
     */
    public Object getValue(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }

        switch (n) {
            case 0: return value0;
            case 1: return value1;
            case 2: return value2;
            case 3: return value3;
            default: return rest[((n - 4) * 2) + 1];
        }
    }

    /**
     * Gets an enumeration of the keys, in order.
     * 
     * @return non-null; an enumeration of the keys
     */
    public Enumeration getKeys() {
        return new KeyEnumeration();
    }

    /**
     * Associated enumeration class.
     */
    private class KeyEnumeration implements Enumeration {
        /** original size; used for modification detection */
        private final int origSize = size;

        /** &gt;= 0; current cursor */
        private int at = 0;

        /** {@inheritDoc} */
        public boolean hasMoreElements() {
            if (size != origSize) {
                throw new ConcurrentModificationException();
            }

            return at < origSize;
        }

        /** {@inheritDoc} */
        public Object nextElement() {
            if (size != origSize) {
                throw new ConcurrentModificationException();
            }

            return getKey(at++);
        }
    }
}

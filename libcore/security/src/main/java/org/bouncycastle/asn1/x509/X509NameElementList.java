package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.DERObjectIdentifier;

// BEGIN android-note
// This class was extracted from X509Name as a way to keep the element
// list in a more controlled fashion. Also, unlike the original code,
// this class imposes a 32 element limit. We have never observed an
// instance created with more than 10, so the limit seems reasonable.
// END android-note

/**
 * List of elements of an X509 name. Each element has a key, a value, and
 * an "added" flag.
 */
public class X509NameElementList {
    /** null-ok; key #0 */
    private DERObjectIdentifier key0;
    
    /** null-ok; key #1 */
    private DERObjectIdentifier key1;

    /** null-ok; key #2 */
    private DERObjectIdentifier key2;

    /** null-ok; key #3 */
    private DERObjectIdentifier key3;
    
    /** null-ok; value #0 */
    private String value0;
    
    /** null-ok; value #1 */
    private String value1;

    /** null-ok; value #2 */
    private String value2;

    /** null-ok; value #3 */
    private String value3;
    
    /**
     * null-ok; array of additional keys and values, alternating
     * key then value, etc. 
     */
    private Object[] rest;

    /** bit vector (in int form) for all the "added" bits */
    private int added;

    /** &gt;= 0; number of elements in the list */
    private int size;

    // Note: Default public constructor.
    
    /**
     * Adds an element. The "added" flag is set to false for the element.
     * 
     * @param key non-null; the key
     * @param value non-null; the value
     */
    public void add(DERObjectIdentifier key, String value) {
        add(key, value, false);
    }

    /**
     * Adds an element.
     * 
     * @param key non-null; the key
     * @param value non-null; the value
     * @param added the added bit
     */
    public void add(DERObjectIdentifier key, String value, boolean added) {
        if (size >= 32) {
            throw new UnsupportedOperationException(
                    "no more than 32 elements");
        }

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
            case 9: {
                // Grow to accommodate 28 pairs in the array.
                Object[] newRest = new Object[56];
                System.arraycopy(rest, 0, newRest, 0, 10);
                rest = newRest;
                // Fall through.
            }
            default: {
                int index = (sz - 4) * 2;
                rest[index] = key;
                rest[index + 1] = value;
                break;
            }
        }

        if (added) {
            this.added |= (1 << sz);
        }
        
        size = sz + 1;
    }

    /**
     * Sets the "added" flag on the most recently added element.
     */
    public void setLastAddedFlag() {
        added |= 1 << (size - 1);
    }

    /**
     * Gets the number of elements in this instance.
     */
    public int size() {
        return size;
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
    public String getValue(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }

        switch (n) {
            case 0: return value0;
            case 1: return value1;
            case 2: return value2;
            case 3: return value3;
            default: return (String) rest[((n - 4) * 2) + 1];
        }
    }

    /**
     * Gets the nth added flag bit.
     * 
     * @param n index
     * @return the nth added flag bit
     */
    public boolean getAdded(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }

        return (added & (1 << n)) != 0;
    }

    /**
     * Constructs and returns a new instance which consists of the
     * elements of this one in reverse order
     * 
     * @return non-null; the reversed instance
     */
    public X509NameElementList reverse() {
        X509NameElementList result = new X509NameElementList();
            
        for (int i = size - 1; i >= 0; i--) {
            result.add(getKey(i), getValue(i), getAdded(i));
        }

        return result;
    }
}

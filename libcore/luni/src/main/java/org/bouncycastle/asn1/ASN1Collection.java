package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.ConcurrentModificationException;

// BEGIN android-note
/*
 * This is a new class that was synthesized from ASN1Sequence and
 * ASN1Set, but with extra smarts about efficiently storing its
 * elements.
 */
// END android-note

/**
 * Base class for collection-like <code>DERObject</code>s. Instances
 * of this class will keep up to four elements directly, resorting to
 * an external collection only if more elements than that need to be
 * stored.
 */
public abstract class ASN1Collection
    extends DERObject
{
    /** &gt;= 0; size of the collection */
    private int size;

    /** null-ok; element #0 */
    private DEREncodable obj0;

    /** null-ok; element #1 */
    private DEREncodable obj1;

    /** null-ok; element #2 */
    private DEREncodable obj2;

    /** null-ok; element #3 */
    private DEREncodable obj3;

    /** null-ok; elements #4 and higher */
    private DEREncodable[] rest;

    /**
     * Returns the object at the postion indicated by index.
     *
     * @param index the index (starting at zero) of the object
     * @return the object at the postion indicated by index
     */
    public final DEREncodable getObjectAt(int index) {
        if ((index < 0) || (index >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
                    
        switch (index) {
            case 0: return obj0;
            case 1: return obj1;
            case 2: return obj2;
            case 3: return obj3;
            default: return rest[index - 4];
        }
    }

    /**
     * Returns the number of objects in this instance.
     *
     * @return the number of objects in this instance
     */
    public final int size() {
        return size;
    }

    /** {@inheritDoc} */
    public final int hashCode() {
        Enumeration e = this.getObjects();
        int hashCode = 0;

        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            
            if (o != null) {
                hashCode ^= o.hashCode();
            }
        }

        return hashCode;
    }

    /**
     * Adds a new element to this instance.
     * 
     * @param obj non-null; the instance to add
     */
    protected void addObject(DEREncodable obj) {
        if (obj == null) {
            throw new NullPointerException("obj == null");
        }

        int sz = size;
        
        switch (sz) {
            case 0: obj0 = obj; break;
            case 1: obj1 = obj; break;
            case 2: obj2 = obj; break;
            case 3: obj3 = obj; break;
            case 4: {
                // Initial allocation of rest.
                rest = new DEREncodable[5];
                rest[0] = obj;
                break;
            }
            default: {
                int index = sz - 4;
                if (index >= rest.length) {
                    // Grow rest.
                    DEREncodable[] newRest = new DEREncodable[index * 2 + 10];
                    System.arraycopy(rest, 0, newRest, 0, rest.length);
                    rest = newRest;
                }
                rest[index] = obj;
                break;
            }
        }

        size++;
    }

    /**
     * Sets the element at a given index (used by {@link #sort}).
     * 
     * @param obj non-null; the object to set
     * @param index &gt;= 0; the index
     */
    private void setObjectAt(DEREncodable obj, int index) {
        switch (index) {
            case 0: obj0 = obj; break;
            case 1: obj1 = obj; break;
            case 2: obj2 = obj; break;
            case 3: obj3 = obj; break;
            default: {
                rest[index - 4] = obj;
                break;
            }
        }
    }

    /**
     * Encodes this instance to the given stream.
     * 
     * @param out non-null; stream to encode to
     */
    /*package*/ abstract void encode(DEROutputStream out) throws IOException;

    /**
     * Gets an enumeration of all the objects in this collection.
     * 
     * @return non-null; the enumeration
     */
    public final Enumeration getObjects() {
        return new ASN1CollectionEnumeration();
    }

    /**
     * Associated enumeration class.
     */
    private class ASN1CollectionEnumeration implements Enumeration {
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

            switch (at++) {
                case 0: return obj0;
                case 1: return obj1;
                case 2: return obj2;
                case 3: return obj3;
                default: return rest[at - 5];
            }
        }
    }

    /**
     * Sorts the elements in this instance.
     */
    protected void sort() {
        if (size <= 1) {
            return;
        }

        boolean swapped = true;

        // TODO: This is bubble sort. Probably not the best choice.
        while (swapped) {
            int index = 0;
            byte[] a = getEncoded(getObjectAt(0));
                
            swapped = false;
                
            while (index != size - 1) {
                int nextIndex = index + 1;
                byte[] b = getEncoded(getObjectAt(nextIndex));

                if (lessThanOrEqual(a, b)) {
                    a = b;
                } else {
                    DEREncodable o = getObjectAt(index);
                    
                    setObjectAt(getObjectAt(nextIndex), index);
                    setObjectAt(o, nextIndex);

                    swapped = true;
                }

                index++;
            }
        }
    }
    
    /**
     * Returns true if a <= b (arrays are assumed padded with zeros).
     */
    private static boolean lessThanOrEqual(byte[] a, byte[] b) {
        if (a.length <= b.length) {
            for (int i = 0; i != a.length; i++) {
                int l = a[i] & 0xff;
                int r = b[i] & 0xff;
                 
                if (r > l) {
                    return true;
                } else if (l > r) {
                    return false;
                }
            }

            return true;
        } else {
            for (int i = 0; i != b.length; i++) {
                 int l = a[i] & 0xff;
                 int r = b[i] & 0xff;
                 
                 if (r > l) {
                     return true;
                 } else if (l > r) {
                     return false;
                 }
             }

             return false;
         }
    }

    /**
     * Gets the encoded form of an object.
     * 
     * @param obj non-null; object to encode
     * @return non-null; the encoded form
     */
    private static byte[] getEncoded(DEREncodable obj) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ASN1OutputStream aOut = new ASN1OutputStream(bOut);

        try {
            aOut.writeObject(obj);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "cannot encode object added to collection");
        }

        return bOut.toByteArray();
    }

    /** {@inheritDoc} */
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            if (i != 0) sb.append(", ");
            sb.append(getObjectAt(i));
        }
        sb.append(']');
        return sb.toString();
    }
}

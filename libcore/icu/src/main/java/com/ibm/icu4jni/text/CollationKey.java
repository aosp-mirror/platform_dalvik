/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*
*******************************************************************************
*/

package com.ibm.icu4jni.text;

/**
 * A concrete implementation of the abstract java.text.CollationKey.
 */
public final class CollationKey extends java.text.CollationKey {
    /**
     * The key.
     */
    private final byte[] bytes;

    /**
     * Cached hash value.
     */
    private int hashCode;

    CollationKey(String source, byte[] bytes) {
        super(source);
        this.bytes = bytes;
    }

    public int compareTo(java.text.CollationKey other) {
        // Get the bytes from the other collation key.
        final byte[] rhsBytes;
        if (other instanceof CollationKey) {
            rhsBytes = ((CollationKey) other).bytes;
        } else {
            rhsBytes = other.toByteArray();
        }

        if (bytes == null || bytes.length == 0) {
            if (rhsBytes == null || rhsBytes.length == 0) {
                return 0;
            }
            return -1;
        } else {
            if (rhsBytes == null || rhsBytes.length == 0) {
                return 1;
            }
        }

        int count = Math.min(bytes.length, rhsBytes.length);
        for (int i = 0; i < count; ++i) {
            int s = bytes[i] & 0xff;
            int t = rhsBytes[i] & 0xff;
            if (s < t) {
                return -1;
            }
            if (s > t) {
                return 1;
            }
        }
        if (bytes.length < rhsBytes.length) {
            return -1;
        }
        if (bytes.length > rhsBytes.length) {
            return 1;
        }
        return 0;
    }

    /**
     * Checks if target object is equal to this object.
     * Target is first casted to CollationKey and bitwise compared.
     * @param target comparison object
     * @return true if both objects are equal, false otherwise
     * @stable ICU 2.4
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof CollationKey)) {
            return false;
        }
        return compareTo((CollationKey) object) == 0;
    }

    /**
     * Creates a hash code for this CollationKey.
     * Compute the hash by iterating sparsely over about 32 (up to 63) bytes
     * spaced evenly through the string.  For each byte, multiply the previous
     * hash value by a prime number and add the new byte in, like a linear
     * congruential random number generator, producing a pseudo-random
     * deterministic value well distributed over the output range.
     * @return hash value of collation key. Hash value is never 0.
     * @stable ICU 2.4
     */
    public int hashCode() {
        if (hashCode == 0) {
            if (bytes != null && bytes.length != 0) {
                int len = bytes.length;
                int inc = ((len - 32) / 32) + 1;
                for (int i = 0; i < len;) {
                    hashCode = (hashCode * 37) + bytes[i];
                    i += inc;
                }
            }
            if (hashCode == 0) {
                hashCode = 1;
            }
        }
        return hashCode;
    }

    public byte[] toByteArray() {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return bytes.clone();
    }
}

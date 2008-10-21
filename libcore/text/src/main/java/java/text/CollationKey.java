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

package java.text;

/**
 * CollationKey represents the collation order of a particular String for a
 * specific Collator. CollationKeys can be compared to determine the relative
 * ordering of their source Strings. This is useful when the Strings must be
 * compared multiple times, as in sorting.
 */
public final class CollationKey implements Comparable<CollationKey> {

    private String source;

    private com.ibm.icu4jni.text.CollationKey icuKey;

    CollationKey(String source, com.ibm.icu4jni.text.CollationKey key) {
        this.source = source;
        this.icuKey = key;
    }

    /**
     * Compare the receiver to the specified CollationKey to determine the
     * relative ordering.
     * 
     * @param value
     *            a CollationKey
     * @return an int < 0 if this CollationKey is less than the specified
     *         CollationKey, 0 if they are equal, and > 0 if this CollationKey
     *         is greater
     */
    public int compareTo(CollationKey value) {
        return icuKey.compareTo(value.icuKey);
    }

    /**
     * Compares the specified object to this CollationKey and answer if they are
     * equal. The object must be an instance of CollationKey and have the same
     * source string and collation key. The instances of CollationKey must have
     * been created by the same Collator.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this CollationKey, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CollationKey)) {
            return false;
        }
        CollationKey collationKey = (CollationKey) object;
        return icuKey.equals(collationKey.icuKey);
    }

    /**
     * Answer the String from which this CollationKey was created.
     * 
     * @return a String
     */
    public String getSourceString() {
        return this.source;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    @Override
    public int hashCode() {
        return icuKey.hashCode();
    }

    /**
     * Answer the collation key as a byte array.
     * 
     * @return an array of bytes
     */
    public byte[] toByteArray() {
        return icuKey.toByteArray();
    }
}

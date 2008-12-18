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
/**
*******************************************************************************
* Copyright (C) 1996-2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

// BEGIN android-note
// The class javadoc and some of the method descriptions are copied from ICU4J
// source files. Changes have been made to the copied descriptions.
// The icu license header was added to this file. 
// The icu implementation used was changed from icu4j to icu4jni.
// END android-note

package java.text;
/**
 * Represents a string under the rules of a specific {@code Collator} object.
 * Comparing two {@code CollationKey} instances returns the relative order of
 * the strings they represent.
 * <p>
 * Since the rule set of collators can differ, the sort orders of the same
 * string under two different {@code Collator} instances might differ. Hence
 * comparing collation keys generated from different {@code Collator} instances
 * can give incorrect results.
 * </p>
 * <p>
 * Both the method {@code CollationKey.compareTo(CollationKey)} and the method
 * {@code Collator.compare(String, String)} compares two strings and returns
 * their relative order. The performance characteristics of these two approaches
 * can differ.
 * </p>
 * <p>
 * During the construction of a {@code CollationKey}, the entire source string
 * is examined and processed into a series of bits terminated by a null, that
 * are stored in the {@code CollationKey}. When
 * {@code CollationKey.compareTo(CollationKey)} executes, it performs bitwise
 * comparison on the bit sequences. This can incur startup cost when creating
 * the {@code CollationKey}, but once the key is created, binary comparisons
 * are fast. This approach is recommended when the same strings are to be
 * compared over and over again.
 * </p>
 * <p>
 * On the other hand, implementations of
 * {@code Collator.compare(String, String)} can examine and process the strings
 * only until the first characters differ in order. This approach is
 * recommended if the strings are to be compared only once.
 * </p>
 * <p>
 * The following example shows how collation keys can be used to sort a
 * list of strings:
 * </p>
 * <blockquote>
 * 
 * <pre>
 * // Create an array of CollationKeys for the Strings to be sorted.
 * Collator myCollator = Collator.getInstance();
 * CollationKey[] keys = new CollationKey[3];
 * keys[0] = myCollator.getCollationKey(&quot;Tom&quot;);
 * keys[1] = myCollator.getCollationKey(&quot;Dick&quot;);
 * keys[2] = myCollator.getCollationKey(&quot;Harry&quot;);
 * sort(keys);
 * <br>
 * //...
 * <br>
 * // Inside body of sort routine, compare keys this way
 * if( keys[i].compareTo( keys[j] ) &gt; 0 )
 *    // swap keys[i] and keys[j]
 * <br>
 * //...
 * <br>
 * // Finally, when we've returned from sort.
 * System.out.println(keys[0].getSourceString());
 * System.out.println(keys[1].getSourceString());
 * System.out.println(keys[2].getSourceString());
 * </pre>
 * 
 * </blockquote>
 * 
 * @see Collator
 * @see RuleBasedCollator
 * @since Android 1.0
 */
public final class CollationKey implements Comparable<CollationKey> {

    private String source;

    private com.ibm.icu4jni.text.CollationKey icuKey;

    CollationKey(String source, com.ibm.icu4jni.text.CollationKey key) {
        this.source = source;
        this.icuKey = key;
    }

    /**
     * Compares this object to the specified collation key object to determine
     * their relative order.
     * 
     * @param value
     *            the collation key object to compare this object to.
     * @return a negative value if this {@code CollationKey} is less than the
     *         specified {@code CollationKey}, 0 if they are equal and a
     *         positive value if this {@code CollationKey} is greater.
     * @since Android 1.0
     */
    public int compareTo(CollationKey value) {
        return icuKey.compareTo(value.icuKey);
    }

    /**
     * Compares the specified object to this {@code CollationKey} and indicates
     * if they are equal. The object must be an instance of {@code CollationKey}
     * and have the same source string and collation key. Both instances of
     * {@code CollationKey} must have been created by the same {@code Collator}.
     * 
     * @param object
     *            the object to compare to this object.
     * @return {@code true} if {@code object} is equal to this collation key;
     *         {@code false} otherwise.
     * @see #hashCode
     * @since Android 1.0
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
     * Returns the string from which this collation key was created.
     * 
     * @return the source string of this collation key.
     * @since Android 1.0
     */
    public String getSourceString() {
        return this.source;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     * 
     * @return the receiver's hash.
     * 
     * @see #equals
     * @since Android 1.0
     */
    @Override
    public int hashCode() {
        return icuKey.hashCode();
    }

    /**
     * Returns the collation key as a byte array.
     * 
     * @return an array of bytes.
     * @since Android 1.0
     */
    public byte[] toByteArray() {
        return icuKey.toByteArray();
    }
}

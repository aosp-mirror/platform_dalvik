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

import java.security.AccessController;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import org.apache.harmony.luni.util.PriviAction;

/**
 * Collator is an abstract class which is the root of classes which provide
 * Locale specific String comparison to determine their ordering with respect to
 * each other.
 */
public abstract class Collator implements Comparator<Object>, Cloneable {

    static final int EQUAL = 0;

    static final int GREATER = 1;

    static final int LESS = -1;

    /**
     * Constant used to specify the decomposition rule.
     */
    public static final int NO_DECOMPOSITION = 0;

    /**
     * Constant used to specify the decomposition rule.
     */
    public static final int CANONICAL_DECOMPOSITION = 1;

    /**
     * Constant used to specify the decomposition rule.
     */
    public static final int FULL_DECOMPOSITION = 2;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int PRIMARY = 0;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int SECONDARY = 1;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int TERTIARY = 2;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int IDENTICAL = 3;

    private static int CACHE_SIZE;

    static {
        // CACHE_SIZE includes key and value, so needs to be double
        String cacheSize = AccessController
                .doPrivileged(new PriviAction<String>("collator.cache")); //$NON-NLS-1$
        if (cacheSize != null) {
            try {
                CACHE_SIZE = Integer.parseInt(cacheSize);
            } catch (NumberFormatException e) {
                CACHE_SIZE = 6;
            }
        } else {
            CACHE_SIZE = 6;
        }
    }

    private static Vector<Collator> cache = new Vector<Collator>(CACHE_SIZE);

    // Wrapper class of ICU4J Collator
    com.ibm.icu4jni.text.Collator icuColl;

    Collator(com.ibm.icu4jni.text.Collator wrapper) {
        this.icuColl = wrapper;
    }

    /**
     * Constructs a new instance of this Collator.
     */
    protected Collator() {
        super();
    }

    /**
     * Returns a new Collator with the same decomposition rule and strength
     * value as this Collator.
     * 
     * @return a shallow copy of this Collator
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            Collator clone = (Collator) super.clone();
            clone.icuColl = (com.ibm.icu4jni.text.Collator) this.icuColl.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Compares the two objects to determine their relative ordering. The
     * objects must be Strings.
     * 
     * @param object1
     *            the first String to compare
     * @param object2
     *            the second String to compare
     * @return an int < 0 if object1 is less than object2, 0 if they are equal,
     *         and > 0 if object1 is greater than object2
     * 
     * @exception ClassCastException
     *                when the objects are not Strings
     */
    public int compare(Object object1, Object object2) {
        return compare((String) object1, (String) object2);
    }

    /**
     * Compares the two Strings to determine their relative ordering.
     * 
     * @param string1
     *            the first String to compare
     * @param string2
     *            the second String to compare
     * @return an int < 0 if string1 is less than string2, 0 if they are equal,
     *         and > 0 if string1 is greater than string2
     */
    public abstract int compare(String string1, String string2);

    /**
     * Compares the specified object to this Collator and answer if they are
     * equal. The object must be an instance of Collator and have the same
     * strength and decomposition values.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this Collator, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Collator)) {
            return false;
        }
        Collator collator = (Collator) object;
        return this.icuColl == null ? collator.icuColl == null : this.icuColl
                .equals(collator.icuColl);
    }

    /**
     * Compares the two Strings using the collation rules to determine if they
     * are equal.
     * 
     * @param string1
     *            the first String to compare
     * @param string2
     *            the second String to compare
     * @return true if the strings are equal using the collation rules, false
     *         otherwise
     */
    public boolean equals(String string1, String string2) {
        return compare(string1, string2) == 0;
    }

    /**
     * Gets the list of installed Locales which support Collator.
     * 
     * @return an array of Locale
     */
    public static Locale[] getAvailableLocales() {
        return com.ibm.icu4jni.text.Collator.getAvailableLocales();
    }

    /**
     * Returns a CollationKey for the specified String for this Collator with
     * the current decomposition rule and strength value.
     * 
     * @param string
     *            the collation key.
     * @return a CollationKey
     */
    public abstract CollationKey getCollationKey(String string);

    /**
     * Returns the decomposition rule for this Collator.
     * 
     * @return the decomposition rule, either NO_DECOMPOSITION,
     *         CANONICAL_DECOMPOSITION or FULL_DECOMPOSITION
     */
    public int getDecomposition() {
        return decompositionMode_ICU_Java(this.icuColl.getDecomposition());
    }

    /**
     * Returns a Collator instance which is appropriate for the default Locale.
     * 
     * @return a Collator
     */
    public static Collator getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a Collator instance which is appropriate for the specified
     * Locale.
     * 
     * @param locale
     *            the Locale
     * @return a Collator
     */
    public static Collator getInstance(Locale locale) {
        String key = locale.toString();
        for (int i = cache.size() - 1; i >= 0; i -= 2) {
            if (cache.elementAt(i).equals(key)) {
                return (Collator) (cache.elementAt(i - 1)).clone();
            }
        }

        return new RuleBasedCollator(com.ibm.icu4jni.text.Collator
                .getInstance(locale));
    }

    /**
     * Returns the strength value for this Collator.
     * 
     * @return the strength value, either PRIMARY, SECONDARY, TERTIARY, or
     *         IDENTICAL
     */
    public int getStrength() {
        return strength_ICU_Java(this.icuColl.getStrength());
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals(Object)
     * @see #equals(String, String)
     */
    @Override
    public abstract int hashCode();

    /**
     * Sets the decomposition rule for this Collator.
     * 
     * @param value
     *            the decomposition rule, either NO_DECOMPOSITION,
     *            CANONICAL_DECOMPOSITION or FULL_DECOMPOSITION
     * 
     * @exception IllegalArgumentException
     *                when the decomposition rule is not valid
     */
    public void setDecomposition(int value) {
        this.icuColl.setDecomposition(decompositionMode_Java_ICU(value));
    }

    /**
     * Sets the strength value for this Collator.
     * 
     * @param value
     *            the strength value, either PRIMARY, SECONDARY, TERTIARY, or
     *            IDENTICAL
     * 
     * @exception IllegalArgumentException
     *                when the strength value is not valid
     */
    public void setStrength(int value) {
        this.icuColl.setStrength(strength_Java_ICU(value));
    }

    private int decompositionMode_Java_ICU(int mode) {
        int icuDecomp = mode;
        switch (mode) {
            case Collator.CANONICAL_DECOMPOSITION:
                icuDecomp = com.ibm.icu4jni.text.Collator.CANONICAL_DECOMPOSITION;
                break;
            case Collator.NO_DECOMPOSITION:
                icuDecomp = com.ibm.icu4jni.text.Collator.NO_DECOMPOSITION;
                break;
        }
        return icuDecomp;
    }

    private int decompositionMode_ICU_Java(int mode) {
        int javaMode = mode;
        switch (mode) {
            case com.ibm.icu4jni.text.Collator.NO_DECOMPOSITION:
                javaMode = Collator.NO_DECOMPOSITION;
                break;
            case com.ibm.icu4jni.text.Collator.CANONICAL_DECOMPOSITION:
                javaMode = Collator.CANONICAL_DECOMPOSITION;
                break;
        }
        return javaMode;
    }

    private int strength_Java_ICU(int value) {
        int icuValue = value;
        switch (value) {
            case Collator.PRIMARY:
                icuValue = com.ibm.icu4jni.text.Collator.PRIMARY;
                break;
            case Collator.SECONDARY:
                icuValue = com.ibm.icu4jni.text.Collator.SECONDARY;
                break;
            case Collator.TERTIARY:
                icuValue = com.ibm.icu4jni.text.Collator.TERTIARY;
                break;
            case Collator.IDENTICAL:
                icuValue = com.ibm.icu4jni.text.Collator.IDENTICAL;
                break;
        }
        return icuValue;

    }

    private int strength_ICU_Java(int value) {
        int javaValue = value;
        switch (value) {
            case com.ibm.icu4jni.text.Collator.PRIMARY:
                javaValue = Collator.PRIMARY;
                break;
            case com.ibm.icu4jni.text.Collator.SECONDARY:
                javaValue = Collator.SECONDARY;
                break;
            case com.ibm.icu4jni.text.Collator.TERTIARY:
                javaValue = Collator.TERTIARY;
                break;
            case com.ibm.icu4jni.text.Collator.IDENTICAL:
                javaValue = Collator.IDENTICAL;
                break;
        }
        return javaValue;
    }
}

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

import org.apache.harmony.text.internal.nls.Messages;

/**
 * <code>RuleBasedCollator</code> is a concrete subclass of
 * <code>Collator</code>. It allows customization of the
 * <code>Collator</code> via user-specified rule sets.
 * <code>RuleBasedCollator</code> is designed to be fully compliant to the <a
 * href="http://www.unicode.org/unicode/reports/tr10/"> Unicode Collation
 * Algorithm (UCA) </a> and conforms to ISO 14651.
 * </p>
 * <p>
 * Create a <code>RuleBasedCollator</code> from a locale by calling the
 * <code>getInstance(Locale)</code> factory method in the base class
 * <code>Collator</code>.<code>Collator.getInstance(Locale)</code> creates
 * a <code>RuleBasedCollator</code> object based on the collation rules
 * defined by the argument locale. If a customized collation is required, use
 * the <code>RuleBasedCollator(String)</code> constructor with the appropriate
 * rules. The customized <code>RuleBasedCollator</code> will base its ordering
 * on UCA, while re-adjusting the attributes and orders of the characters in the
 * specified rule accordingly.
 * </p>
 * 
 */
public class RuleBasedCollator extends Collator {

    RuleBasedCollator(com.ibm.icu4jni.text.Collator wrapper) {
        super(wrapper);
    }

    /**
     * Constructs a new instance of <code>RuleBasedCollator</code> using the
     * specified <code>rules</code>.
     * 
     * @param rules
     *            the collation rules.
     * @throws ParseException
     *             when the rules contains an invalid collation rule syntax.
     */
    public RuleBasedCollator(String rules) throws ParseException {
        if (rules == null) {
            throw new NullPointerException();
        }
        if (rules.length() == 0) {
            // text.06=Build rules empty
            throw new ParseException(Messages.getString("text.06"), 0); //$NON-NLS-1$
        }

        try {
            this.icuColl = new com.ibm.icu4jni.text.RuleBasedCollator(rules);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                throw (ParseException) e;
            }
            /*
             * -1 means it's not a ParseException. Maybe IOException thrown when
             * an error occured while reading internal data.
             */
            throw new ParseException(e.getMessage(), -1);
        }
    }

    /**
     * Obtains a <code>CollationElementIterator</code> for the given
     * <code>CharacterIterator</code>. The source iterator's integrity will
     * be preserved since a new copy will be created for use.
     * 
     * @param source
     *            the specified source
     * @return a <code>CollationElementIterator</code> for the source.
     */
    public CollationElementIterator getCollationElementIterator(
            CharacterIterator source) {
        if (source == null) {
            throw new NullPointerException();
        }
        return new CollationElementIterator(
                ((com.ibm.icu4jni.text.RuleBasedCollator) this.icuColl)
                        .getCollationElementIterator(source));
    }

    /**
     * Obtains a <code>CollationElementIterator</code> for the given String.
     * 
     * @param source
     *            the specified source
     * @return a <code>CollationElementIterator</code> for the given String
     */
    public CollationElementIterator getCollationElementIterator(String source) {
        if (source == null) {
            throw new NullPointerException();
        }
        return new CollationElementIterator(
                ((com.ibm.icu4jni.text.RuleBasedCollator) this.icuColl)
                        .getCollationElementIterator(source));
    }

    /**
     * Obtains the collation rules of the <code>RuleBasedCollator</code>.
     * 
     * @return the collation rules.
     */
    public String getRules() {
        return ((com.ibm.icu4jni.text.RuleBasedCollator) this.icuColl).getRules();
    }

    /**
     * Obtains the cloned object of the <code>RuleBasedCollator</code>
     * 
     * @return the cloned object of the <code>RuleBasedCollator</code>
     */
    @Override
    public Object clone() {
        RuleBasedCollator clone = (RuleBasedCollator) super.clone();
        return clone;
    }

    /**
     * Compares the <code>source</code> text <code>String</code> to the
     * <code>target</code> text <code>String</code> according to the
     * collation rules, strength and decomposition mode for this
     * <code>RuleBasedCollator</code>. See the <code>Collator</code> class
     * description for an example of use.
     * <p>
     * General recommendation: If comparisons are to be done to the same String
     * multiple times, it would be more efficient to generate
     * <code>CollationKeys</code> for the <code>String</code> s and use
     * <code>CollationKey.compareTo(CollationKey)</code> for the comparisons.
     * If the each Strings are compared to only once, using the method
     * RuleBasedCollator.compare(String, String) will have a better performance.
     * </p>
     * 
     * @param source
     *            the source text
     * @param target
     *            the target text
     * @return an integer which may be a negative value, zero, or else a
     *         positive value depending on whether <code>source</code> is less
     *         than, equivalent to, or greater than <code>target</code>.
     */
    @Override
    public int compare(String source, String target) {
        if (source == null || target == null) {
            // text.08=one of arguments is null
            throw new NullPointerException(Messages.getString("text.08")); //$NON-NLS-1$
        }
        return this.icuColl.compare(source, target);
    }

    /**
     * Obtains the <code>CollationKey</code> for the given source text.
     * 
     * @param source
     *            the specified source text
     * @return the <code>CollationKey</code> for the given source text.
     */
    @Override
    public CollationKey getCollationKey(String source) {
        com.ibm.icu4jni.text.CollationKey icuKey = this.icuColl
                .getCollationKey(source);
        if (icuKey == null) {
            return null;
        }
        return new CollationKey(source, icuKey);
    }

    /**
     * Obtains a unique hash code for the <code>RuleBasedCollator</code>
     * 
     * @return the hash code for the <code>RuleBasedCollator</code>
     */
    @Override
    public int hashCode() {
        return ((com.ibm.icu4jni.text.RuleBasedCollator) this.icuColl).getRules()
                .hashCode();
    }

    /**
     * Compares the equality of two <code>RuleBasedCollator</code> objects.
     * <code>RuleBasedCollator</code> objects are equal if they have the same
     * collation rules and the same attributes.
     * 
     * @param obj
     *            the other object.
     * @return <code>true</code> if this <code>RuleBasedCollator</code> has
     *         exactly the same collation behaviour as obj, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Collator)) {
            return false;
        }
        return super.equals(obj);
    }
}

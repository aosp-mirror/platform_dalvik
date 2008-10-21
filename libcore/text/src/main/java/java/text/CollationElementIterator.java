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

// BEGIN android-note
// Fixed Unicode escape sequences to make the file 7-bit clean.
// END android-note

/**
 * <p>
 * <code>CollationElementIterator</code> is created by a
 * <code>RuleBasedCollator</code> to iterate through a string. The return
 * result of each iteration is a 32-bit collation element that defines the
 * ordering priority of the next character or sequence of characters in the
 * source string.
 * </p>
 * <p>
 * For illustration, consider the following in Spanish:
 * </p>
 * 
 * <p>
 * <code>
 * "ca" -> the first collation element is collation_element('c') and second
 * collation element is collation_element('a').
 * </code>
 * </p>
 * 
 * <p>
 * <code>
 * Since "ch" in Spanish sorts as one entity, the below example returns one
 * collation element for the two characters 'c' and 'h'
 * </code>
 * </p>
 * 
 * <p>
 * <code>
 * "cha" -> the first collation element is collation_element('ch') and second
 * collation element is collation_element('a').
 * </code>
 * </p>
 * <p>
 * And in German,
 * </p>
 * 
 * <p>
 * <code>
 * Since the character '&#92;u0086' is a composed character of 'a' and 'e', the iterator
 * returns two collation elements for the single character '&#92;u0086'
 * </code>
 * </p>
 * <p>
 * <code>
 * "&#92;u0086b" -> the first
 * collation element is collation_element('a'), the second collation element is
 * collation_element('e'), and the third collation element is
 * collation_element('b').
 * </code>
 * </p>
 * 
 */
public final class CollationElementIterator {

    /**
     * This constant is returned by the iterator in the methods
     * <code>next()</code> and <code>previous()</code> when the end or the
     * beginning of the source string has been reached, and there are no more
     * valid collation elements to return.
     */
    public static final int NULLORDER = -1;

    private com.ibm.icu4jni.text.CollationElementIterator icuIterator;

    CollationElementIterator(com.ibm.icu4jni.text.CollationElementIterator iterator) {
        this.icuIterator = iterator;
    }

    /**
     * Obtains the maximum length of any expansion sequence that ends with the
     * specified collation element. If there is no expansion with this collation
     * element as the last element, returns <code>1</code>.
     * 
     * @param order
     *            a collation element that has been previously obtained from a
     *            call to either the {@link #next()} or {@link #previous()}
     *            method.
     * @return the maximum length of any expansion sequence ending with the
     *         specified collation element.
     */
    public int getMaxExpansion(int order) {
        return this.icuIterator.getMaxExpansion(order);
    }

    /**
     * Obtains the character offset in the source string corresponding to the
     * next collation element. This value could be any of: <ui>
     * <li>The index of the first character in the source string that matches
     * the value of the next collation element. (This means that if
     * setOffset(offset) sets the index in the middle of a contraction,
     * getOffset() returns the index of the first character in the contraction,
     * which may not be equal to the original offset that was set. Hence calling
     * getOffset() immediately after setOffset(offset) does not guarantee that
     * the original offset set will be returned.)</li>
     * <li>If normalization is on, the index of the immediate subsequent
     * character, or composite character with the first character, having a
     * combining class of 0.</li>
     * <li>The length of the source string, if iteration has reached the end.
     * </li>
     * <ui>
     * 
     * @return The position of the collation element in the source string that
     *         will be returned in the next invocation of the {@link #next()}
     *         method.
     */
    public int getOffset() {
        return this.icuIterator.getOffset();
    }

    /**
     * Obtains the next collation element in the source string.
     * 
     * @return the next collation element or <code>NULLORDER</code> if the end
     *         of the iteration has been reached.
     */
    public int next() {
        return this.icuIterator.next();
    }

    /**
     * Obtains the previous collation element in the source string.
     * 
     * @return the previous collation element, or <code>NULLORDER</code> when
     *         the start of the iteration has been reached.
     */
    public int previous() {
        return this.icuIterator.previous();
    }

    /**
     * Obtains the primary order of the specified collation element, i.e. the
     * first 16 bits. This value is unsigned.
     * 
     * @param order
     * @return the element's 16 bits primary order.
     */
    public static final int primaryOrder(int order) {
        return com.ibm.icu4jni.text.CollationElementIterator.primaryOrder(order);
    }

    /**
     * Repositions the cursor to point at the first element of the current
     * string. The next call to <code>next()</code> or <code>previous()</code>
     * will return the first and last collation element in the string,
     * respectively.
     * <p>
     * If the <code>RuleBasedCollator</code> used by this iterator has had its
     * attributes changed, calling <code>reset()</code> will reinitialize the
     * iterator to use the new attributes.
     * </p>
     */
    public void reset() {
        this.icuIterator.reset();
    }

    /**
     * Obtains the secondary order of the specified collation element, i.e. the
     * 16th to 23th bits, inclusive. This value is unsigned.
     * 
     * @param order
     * @return the 8 bit secondary order of the element
     */
    public static final short secondaryOrder(int order) {
        return (short) com.ibm.icu4jni.text.CollationElementIterator
                .secondaryOrder(order);
    }

    /**
     * Points the iterator at the collation element associated with the
     * character in the source string which is found at the supplied offset.
     * After this call completes, an invocation of the {@link #next()} method
     * will return this collation element.
     * <p>
     * If <code>newOffset</code> corresponds to a character which is part of a
     * sequence that maps to a single collation element the iterator is adjusted
     * to the start of that sequence. As a result of this, any subsequent call
     * made to <code>getOffset()</code> may not return the same value set by
     * this method.
     * </p>
     * <p>
     * If the decomposition mode is on, and offset is in the middle of a
     * decomposable range of source text, the iterator may not return a correct
     * result for the next forwards or backwards iteration. The user must ensure
     * that the offset is not in the middle of a decomposable range.
     * </p>
     * 
     * @param newOffset
     *            the character offset into the original source string to set.
     *            Note that this is not an offset into the corresponding
     *            sequence of collation elements.
     */
    public void setOffset(int newOffset) {
        this.icuIterator.setOffset(newOffset);
    }

    /**
     * Sets a new source string iterator for iteration, and reset the offset to
     * the beginning of the text.
     * 
     * @param source
     *            the new source string iterator for iteration.
     */
    public void setText(CharacterIterator source) {
        this.icuIterator.setText(source);
    }

    /**
     * Sets a new source string for iteration, and reset the offset to the
     * beginning of the text.
     * 
     * @param source
     *            the new source string for iteration
     */
    public void setText(String source) {
        this.icuIterator.setText(source);
    }

    /**
     * Obtains the tertiary order of the specified collation element, i.e. the
     * last 8 bits. This value is unsigned.
     * 
     * @param order
     * @return the 8 bits tertiary order of the element
     */
    public static final short tertiaryOrder(int order) {
        return (short) com.ibm.icu4jni.text.CollationElementIterator
                .tertiaryOrder(order);
    }
}

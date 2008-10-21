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

import java.util.Locale;

/**
 * This class is used to locate the boundaries of text. Instance of this class
 * can be got by some factory methods:
 * <ul>
 * <li>
 * <code>getCharacterInstance()<code> returns a BreakIterator that iterate the 
 * logical characters without worrying about how the character is stored. For 
 * example, some character may be stored in more than one Unicode code point 
 * according to Unicode specification, this character can handle the logical 
 * characters with multi code points.</li>
 * <li>
 * <code>getWordInstance()<code> returns a <code>BreakIterator</code> that 
 * iterate the word-breaks. The beginning and end of each word(including numbers) 
 * is treated as boundary position. Whitespace and punctuation are kept separate 
 * from real words.</li>
 * <li>
 * <code>getSentenceInstance()</code> returns a BreakIterator that iterate the 
 * sentence-breaks.</li>
 * <li><code>getLineInstance()</code> returns a BreakIterator that iterate the 
 * line-breaks which can be used to wrap lines. This iterator can handle whitespaces, 
 * hyphens and punctuations.  
 * </ul>
 * 
 * <code>BreakIterator</code> uses <code>CharacterIterator</code> to perform the 
 * analysis, so that any storage which provides <code>CharacterIterator</code> 
 * interface.
 * 
 * @see CharacterIterator
 */
public abstract class BreakIterator implements Cloneable {

    /*
     * -----------------------------------------------------------------------
     * constants
     * -----------------------------------------------------------------------
     */
    /**
     * This constant is returned by iterate methods like previous() or next() if
     * they have returned all valid boundaries.
     */
    public static final int DONE = -1;

    private static final int LONG_LENGTH = 8;

    private static final int INT_LENGTH = 4;

    private static final int SHORT_LENGTH = 2;

    /*
     * -----------------------------------------------------------------------
     * variables
     * -----------------------------------------------------------------------
     */
    // the wrapped ICU implementation
    com.ibm.icu4jni.text.BreakIterator wrapped;

    /*
     * -----------------------------------------------------------------------
     * constructors
     * -----------------------------------------------------------------------
     */
    /**
     * Default constructor, just for invocation by subclass.
     */
    protected BreakIterator() {
        super();
    }

    /*
     * wrapping constructor
     */
    BreakIterator(com.ibm.icu4jni.text.BreakIterator iterator) {
        wrapped = iterator;
    }

    /*
     * -----------------------------------------------------------------------
     * methods
     * -----------------------------------------------------------------------
     */
    /**
     * Return all supported locales.
     * 
     * @return all supported locales
     */
    public static Locale[] getAvailableLocales() {
        return com.ibm.icu4jni.text.BreakIterator.getAvailableLocales();
    }

    /**
     * Return a new instance of BreakIterator used to iterate characters using
     * default locale.
     * 
     * @return a new instance of BreakIterator used to iterate characters using
     *         default locale.
     */
    public static BreakIterator getCharacterInstance() {
        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getCharacterInstance());
    }

    /**
     * Return a new instance of BreakIterator used to iterate characters using
     * given locale.
     * 
     * @param where
     *            the given locale
     * @return a new instance of BreakIterator used to iterate characters using
     *         given locale.
     */
    public static BreakIterator getCharacterInstance(Locale where) {
        if (where == null) {
            throw new NullPointerException();
        }

        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getCharacterInstance(where));
    }

    /**
     * Return a new instance of BreakIterator used to iterate line-breaks using
     * default locale.
     * 
     * @return a new instance of BreakIterator used to iterate line-breaks using
     *         default locale.
     */
    public static BreakIterator getLineInstance() {
        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getLineInstance());
    }

    /**
     * Return a new instance of BreakIterator used to iterate line-breaks using
     * given locale.
     * 
     * @param where
     *            the given locale
     * @return a new instance of BreakIterator used to iterate line-breaks using
     *         given locale.
     */
    public static BreakIterator getLineInstance(Locale where) {
        if (where == null) {
            throw new NullPointerException();
        }

        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getLineInstance(where));
    }

    /**
     * Return a new instance of BreakIterator used to iterate sentence-breaks
     * using default locale.
     * 
     * @return a new instance of BreakIterator used to iterate sentence-breaks
     *         using default locale.
     */
    public static BreakIterator getSentenceInstance() {
        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getSentenceInstance());
    }

    /**
     * Return a new instance of BreakIterator used to iterate sentence-breaks
     * using given locale.
     * 
     * @param where
     *            the given locale
     * @return a new instance of BreakIterator used to iterate sentence-breaks
     *         using given locale.
     */
    public static BreakIterator getSentenceInstance(Locale where) {
        if (where == null) {
            throw new NullPointerException();
        }

        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getSentenceInstance(where));
    }

    /**
     * Return a new instance of BreakIterator used to iterate word-breaks using
     * default locale.
     * 
     * @return a new instance of BreakIterator used to iterate word-breaks using
     *         default locale.
     */
    public static BreakIterator getWordInstance() {
        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getWordInstance());
    }

    /**
     * Return a new instance of BreakIterator used to iterate word-breaks using
     * given locale.
     * 
     * @param where
     *            the given locale
     * @return a new instance of BreakIterator used to iterate word-breaks using
     *         given locale.
     */
    public static BreakIterator getWordInstance(Locale where) {
        if (where == null) {
            throw new NullPointerException();
        }

        return new RuleBasedBreakIterator(com.ibm.icu4jni.text.BreakIterator
                .getWordInstance(where));
    }

    /**
     * Return true if the given offset is a boundary position. If this method
     * returns true, the current iteration position is set to the given
     * position; if the function returns false, the current iteration position
     * is set as though following() had been called.
     * 
     * @param offset
     *            the given offset to check
     * @return true if the given offset is a boundary position
     */
    public boolean isBoundary(int offset) {
        return wrapped.isBoundary(offset);
    }

    /**
     * Return the position of last boundary precede the given offset, and set
     * current position to returned value, or <code>DONE</code> if the given
     * offset specifies the starting position.
     * <p>
     * <code>IllegalArgumentException</code> will be thrown if given offset is
     * invalid.
     * </p>
     * 
     * @param offset
     *            the given start position to be searched for
     * @return the position of last boundary precede the given offset
     */
    public int preceding(int offset) {
        return wrapped.preceding(offset);
    }

    /**
     * Set the new text string to be analyzed, the current position will be
     * reset to beginning of this new string, and the old string will lost.
     * 
     * @param newText
     *            the new text string to be analyzed
     */
    public void setText(String newText) {
        wrapped.setText(newText);
    }

    /*
     * -----------------------------------------------------------------------
     * abstract methods
     * -----------------------------------------------------------------------
     */
    /**
     * Return this iterator's current position.
     * 
     * @return this iterator's current position
     */
    public abstract int current();

    /**
     * Set this iterator's current position to the first boundary, and return
     * this position.
     * 
     * @return the position of first boundary
     */
    public abstract int first();

    /**
     * Set the position of the first boundary following the given offset, and
     * return this position. If there is no boundary after the given offset,
     * return DONE.
     * <p>
     * <code>IllegalArgumentException</code> will be thrown if given offset is
     * invalid.
     * </p>
     * 
     * @param offset
     *            the given position to be searched for
     * @return the position of the first boundary following the given offset
     */
    public abstract int following(int offset);

    /**
     * Return a <code>CharacterIterator</code> which represents the text being
     * analyzed. Please note that the returned value is probably the internal
     * iterator used by this object, so that if the invoker want to modify the
     * status of the returned iterator, a clone operation at first is
     * recommended.
     * 
     * @return a <code>CharacterIterator</code> which represents the text
     *         being analyzed.
     */
    public abstract CharacterIterator getText();

    /**
     * Set this iterator's current position to the last boundary, and return
     * this position.
     * 
     * @return the position of last boundary
     */
    public abstract int last();

    /**
     * Set this iterator's current position to the next boundary after current
     * position, and return this position. Return <code>DONE</code> if no
     * boundary found after current position.
     * 
     * @return the position of last boundary
     */
    public abstract int next();

    /**
     * Set this iterator's current position to the next boundary after the given
     * position, and return this position. Return <code>DONE</code> if no
     * boundary found after the given position.
     * 
     * @param n
     *            the given position.
     * @return the position of last boundary
     */
    public abstract int next(int n);

    /**
     * Set this iterator's current position to the previous boundary before
     * current position, and return this position. Return <code>DONE</code> if
     * no boundary found before current position.
     * 
     * @return the position of last boundary
     */
    public abstract int previous();

    /**
     * Set new text to be analyzed by given <code>CharacterIterator</code>.
     * The position will be reset to the beginning of the new text, and other
     * status of this iterator will be kept.
     * 
     * @param newText
     *            the given <code>CharacterIterator</code> refer to the text
     *            to be analyzed
     */
    public abstract void setText(CharacterIterator newText);

    /*
     * -----------------------------------------------------------------------
     * methods override Object
     * -----------------------------------------------------------------------
     */
    /**
     * Create copy of this iterator, all status including current position is
     * kept.
     * 
     * @return copy of this iterator
     */
    @Override
    public Object clone() {
        try {
            BreakIterator cloned = (BreakIterator) super.clone();
            cloned.wrapped = (com.ibm.icu4jni.text.BreakIterator) wrapped.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Get a long value from the given byte array, start from given offset.
     * 
     * @param buf
     *            the bytes to be converted
     * @param offset
     *            the start position of conversion
     * @return the converted long value
     */
    protected static long getLong(byte[] buf, int offset) {
        if (null == buf) {
            throw new NullPointerException();
        }
        if (offset < 0 || buf.length - offset < LONG_LENGTH) {
            throw new ArrayIndexOutOfBoundsException();
        }
        long result = 0;
        for (int i = offset; i < offset + LONG_LENGTH; i++) {
            result = (result << 8) | (buf[i] & 0xff);
        }
        return result;
    }

    /**
     * Get an int value from the given byte array, start from given offset.
     * 
     * @param buf
     *            the bytes to be converted
     * @param offset
     *            the start position of conversion
     * @return the converted int value
     */
    protected static int getInt(byte[] buf, int offset) {
        if (null == buf) {
            throw new NullPointerException();
        }
        if (offset < 0 || buf.length - INT_LENGTH < offset) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int result = 0;
        for (int i = offset; i < offset + INT_LENGTH; i++) {
            result = (result << 8) | (buf[i] & 0xff);
        }
        return result;
    }

    /**
     * Get a short value from the given byte array, start from given offset.
     * 
     * @param buf
     *            the bytes to be converted
     * @param offset
     *            the start position of conversion
     * @return the converted short value
     */
    protected static short getShort(byte[] buf, int offset) {
        if (null == buf) {
            throw new NullPointerException();
        }
        if (offset < 0 || buf.length - SHORT_LENGTH < offset) {
            throw new ArrayIndexOutOfBoundsException();
        }
        short result = 0;
        for (int i = offset; i < offset + SHORT_LENGTH; i++) {
            result = (short) ((result << 8) | (buf[i] & 0xff));
        }
        return result;
    }
}

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.regex;

import java.io.Serializable;
import java.util.ArrayList;
import com.ibm.icu4jni.regex.NativeRegEx;

/**
 * Represents a pattern used for matching, searching, or replacing strings.
 * {@code Pattern}s are specified in terms of regular expressions and compiled
 * using an instance of this class. They are then used in conjunction with a
 * {@link Matcher} to perform the actual search.
 * <p/>
 * A typical use case looks like this:
 * <p/>
 * <pre>
 * Pattern p = Pattern.compile("Hello, A[a-z]*!");
 *  
 * Matcher m = p.matcher("Hello, Android!");
 * boolean b1 = m.matches(); // true
 *  
 * m.setInput("Hello, Robot!");
 * boolean b2 = m.matches(); // false
 * </pre>
 * <p/>
 * The above code could also be written in a more compact fashion, though this
 * variant is less efficient, since {@code Pattern} and {@code Matcher} objects
 * are created on the fly instead of being reused.
 * fashion:
 * <pre>
 *     boolean b1 = Pattern.matches("Hello, A[a-z]*!", "Hello, Android!"); // true
 *     boolean b2 = Pattern.matches("Hello, A[a-z]*!", "Hello, Robot!");   // false
 * </pre>
 * <p/>
 * Please consult the <a href="package.html">package documentation</a> for an
 * overview of the regular expression syntax used in this class as well as
 * Android-specific implementation details.
 * 
 * @see Matcher
 * @since Android 1.0
 */
public final class Pattern implements Serializable {
    
    private static final long serialVersionUID = 5073258162644648461L;
    
    /**
     * This constant specifies that a pattern matches Unix line endings ('\n')
     * only against the '.', '^', and '$' meta characters.
     * 
     * @since Android 1.0
     */
    public static final int UNIX_LINES = 0x01;

    /**
     * This constant specifies that a {@code Pattern} is matched
     * case-insensitively. That is, the patterns "a+" and "A+" would both match
     * the string "aAaAaA".
     * <p>
     * Note: For Android, the {@code CASE_INSENSITIVE} constant
     * (currently) always includes the meaning of the {@link #UNICODE_CASE}
     * constant. So if case insensitivity is enabled, this automatically extends
     * to all Unicode characters. The {@code UNICODE_CASE} constant itself has
     * no special consequences.
     * 
     * @since Android 1.0
     */
    public static final int CASE_INSENSITIVE = 0x02;

    /**
     * This constant specifies that a {@code Pattern} may contain whitespace or
     * comments. Otherwise comments and whitespace are taken as literal
     * characters.
     * 
     * @since Android 1.0
     */
    public static final int COMMENTS = 0x04;

    /**
     * This constant specifies that the meta characters '^' and '$' match only
     * the beginning and end end of an input line, respectively. Normally, they
     * match the beginning and the end of the complete input.
     * 
     * @since Android 1.0
     */
    public static final int MULTILINE = 0x08;

    /**
     * This constant specifies that the whole {@code Pattern} is to be taken
     * literally, that is, all meta characters lose their meanings.
     * 
     * @since Android 1.0
     */
    public static final int LITERAL = 0x10;

    /**
     * This constant specifies that the '.' meta character matches arbitrary
     * characters, including line endings, which is normally not the case.
     * 
     * @since Android 1.0
     */
    public static final int DOTALL = 0x20;

    /**
     * This constant specifies that a {@code Pattern} is matched
     * case-insensitively with regard to all Unicode characters. It is used in
     * conjunction with the {@link #CASE_INSENSITIVE} constant to extend its
     * meaning to all Unicode characters.
     * <p>
     * Note: For Android, the {@code CASE_INSENSITIVE} constant
     * (currently) always includes the meaning of the {@code UNICODE_CASE}
     * constant. So if case insensitivity is enabled, this automatically extends
     * to all Unicode characters. The {@code UNICODE_CASE} constant then has no
     * special consequences.
     * 
     * @since Android 1.0
     */
    public static final int UNICODE_CASE = 0x40;

    /**
     * This constant specifies that a character in a {@code Pattern} and a
     * character in the input string only match if they are canonically
     * equivalent. It is (currently) not supported in Android.
     * 
     * @since Android 1.0
     */
    public static final int CANON_EQ = 0x80;

    /**
     * Holds the regular expression.
     */
    private String pattern;
    
    /**
     * Holds the flags used when compiling this pattern.
     */
    private int flags;

    /**
     * Holds a handle (a pointer, actually) for the native ICU pattern.
     */
    transient int mNativePattern;
    
    /**
     * Holds the number of groups in the pattern.
     */
    transient int mGroupCount;
    
    /**
     * Compiles a regular expression, creating a new Pattern instance in the
     * process. This is actually a convenience method that calls {@link
     * #compile(String, int)} with a {@code flags} value of zero.
     * 
     * @param pattern
     *            the regular expression.
     * 
     * @return the new {@code Pattern} instance.
     * 
     * @throws PatternSyntaxException
     *             if the regular expression is syntactically incorrect.
     * 
     * @since Android 1.0
     */
    public static Pattern compile(String pattern) throws PatternSyntaxException {
        return new Pattern(pattern, 0);
    }

    /**
     * Compiles a regular expression, creating a new {@code Pattern} instance in
     * the process. Allows to set some flags that modify the behavior of the
     * {@code Pattern}.
     * 
     * @param pattern
     *            the regular expression.
     * @param flags
     *            the flags to set. Basically, any combination of the constants
     *            defined in this class is valid.
     *            <p>
     *            Note: Currently, the {@link #CASE_INSENSITIVE} and
     *            {@link #UNICODE_CASE} constants have slightly special behavior
     *            in Android, and the {@link #CANON_EQ} constant is not
     *            supported at all.
     * 
     * @return the new {@code Pattern} instance.
     * 
     * @throws PatternSyntaxException
     *             if the regular expression is syntactically incorrect.
     * 
     * @see #CANON_EQ
     * @see #CASE_INSENSITIVE
     * @see #COMMENTS
     * @see #DOTALL
     * @see #LITERAL
     * @see #MULTILINE
     * @see #UNICODE_CASE
     * @see #UNIX_LINES
     * 
     * @since Android 1.0
     */
    public static Pattern compile(String pattern, int flags) throws PatternSyntaxException {
        return new Pattern(pattern, flags);
    }

    /**
     * Creates a new {@code Pattern} instance from a given regular expression
     * and flags.
     * 
     * @param pattern
     *            the regular expression.
     * @param flags
     *            the flags to set. Any combination of the constants defined in
     *            this class is valid.
     * 
     * @throws PatternSyntaxException
     *             if the regular expression is syntactically incorrect.
     */
    private Pattern(String pattern, int flags) throws PatternSyntaxException {
        if ((flags & CANON_EQ) != 0) {
            throw new UnsupportedOperationException("CANON_EQ flag not supported");
        }
        
        this.pattern = pattern;
        this.flags = flags;
        
        compileImpl(pattern, flags);
    }
    
    /**
     * Compiles the given regular expression using the given flags. Used
     * internally only.
     * 
     * @param pattern
     *            the regular expression.
     * @param flags
     *            the flags.
     */
    private void compileImpl(String pattern, int flags) throws PatternSyntaxException {
        if (pattern == null) {
            throw new NullPointerException();
        }
        
        if ((flags & LITERAL) != 0) {
            pattern = quote(pattern);
        }
        
        // These are the flags natively supported by ICU.
        // They even have the same value in native code.
        flags = flags & (CASE_INSENSITIVE | COMMENTS | MULTILINE | DOTALL | UNIX_LINES);
        
        mNativePattern = NativeRegEx.open(pattern, flags);
        mGroupCount = NativeRegEx.groupCount(mNativePattern);
    }

    /**
     * Returns the regular expression that was compiled into this
     * {@code Pattern}.
     * 
     * @return the regular expression.
     * 
     * @since Android 1.0
     */
    public String pattern() {
        return pattern;
    }
    
    /**
     * Returns the flags that have been set for this {@code Pattern}.
     *  
     * @return the flags that have been set. A combination of the constants
     *         defined in this class.
     *         
     * @see #CANON_EQ
     * @see #CASE_INSENSITIVE
     * @see #COMMENTS
     * @see #DOTALL
     * @see #LITERAL
     * @see #MULTILINE
     * @see #UNICODE_CASE
     * @see #UNIX_LINES
     *         
     * @since Android 1.0
     */
    public int flags() {
        return flags;
    }

    /**
     * Returns a {@link Matcher} for the {@code Pattern} and a given input. The
     * {@code Matcher} can be used to match the {@code Pattern} against the
     * whole input, find occurrences of the {@code Pattern} in the input, or
     * replace parts of the input.
     * 
     * @param input
     *            the input to process.
     * 
     * @return the resulting {@code Matcher}.
     * 
     * @since Android 1.0
     */
    public Matcher matcher(CharSequence input) {
        return new Matcher(this, input);
    }

    /**
     * Tries to match a given regular expression against a given input. This is
     * actually nothing but a convenience method that compiles the regular
     * expression into a {@code Pattern}, builds a {@link Matcher} for it, and
     * then does the match. If the same regular expression is used for multiple
     * operations, it is recommended to compile it into a {@code Pattern}
     * explicitly and request a reusable {@code Matcher}.
     * 
     * @param regex
     *            the regular expression.
     * @param input
     *            the input to process.
     * 
     * @return true if and only if the {@code Pattern} matches the input.
     * 
     * @see Pattern#compile(java.lang.String, int)
     * @see Matcher#matches()
     * 
     * @since Android 1.0
     */
    static public boolean matches(String regex, CharSequence input) {
        return new Matcher(new Pattern(regex, 0), input).matches();
    }

    /**
     * Splits a given input around occurrences of a regular expression. This is
     * a convenience method that is equivalent to calling the method
     * {@link #split(java.lang.CharSequence, int)} with a limit of 0.
     * 
     * @param input
     *            the input sequence.
     * 
     * @return the resulting array.
     * 
     * @since Android 1.0
     */
    public String[] split(CharSequence input) {
        return split(input, 0);
    }

    /**
     * Splits the given input sequence around occurrences of the {@code Pattern}.
     * The function first determines all occurrences of the {@code Pattern}
     * inside the input sequence. It then builds an array of the
     * &quot;remaining&quot; strings before, in-between, and after these
     * occurrences. An additional parameter determines the maximal number of
     * entries in the resulting array and the handling of trailing empty
     * strings.
     * 
     * @param inputSeq
     *            the input sequence.
     * @param limit
     *            Determines the maximal number of entries in the resulting
     *            array.
     *            <ul>
     *            <li>For n &gt; 0, it is guaranteed that the resulting array
     *            contains at most n entries.
     *            <li>For n &lt; 0, the length of the resulting array is
     *            exactly the number of occurrences of the {@code Pattern} +1.
     *            All entries are included.
     *            <li>For n == 0, the length of the resulting array is at most
     *            the number of occurrences of the {@code Pattern} +1. Empty
     *            strings at the end of the array are not included.
     *            </ul>
     * 
     * @return the resulting array.
     * 
     * @since Android 1.0
     */
    public String[] split(CharSequence inputSeq, int limit) {
        int maxLength = limit <= 0 ? Integer.MAX_VALUE : limit;

        String input = inputSeq.toString();
        ArrayList<String> list = new ArrayList<String>();

        Matcher matcher = new Matcher(this, inputSeq);
        int savedPos = 0;
        
        // Add text preceding each occurrence, if enough space. Only do this for
        // non-empty input sequences, because otherwise we'd add the "trailing
        // empty string" twice.
        if (inputSeq.length() != 0) {
            while(matcher.find() && list.size() + 1 < maxLength) {
                list.add(input.substring(savedPos, matcher.start()));
                savedPos = matcher.end();
            }
        }
        
        // Add trailing text if enough space.
        if (list.size() < maxLength) {
            if (savedPos < input.length()) {
                list.add(input.substring(savedPos));
            } else {
                list.add("");
            }
        }
        
        // Remove trailing spaces, if limit == 0 is requested.
        if (limit == 0) {
            int i = list.size() - 1;
            // Don't remove 1st element, since array must not be empty.
            while(i > 0 && "".equals(list.get(i))) {
                list.remove(i);
                i--;
            }
        }
        
        return list.toArray(new String[list.size()]);
    }

    /**
     * Quotes a given string using "\Q" and "\E", so that all other
     * meta-characters lose their special meaning. If the string is used for a
     * {@code Pattern} afterwards, it can only be matched literally.
     * 
     * @param s
     *            the string to quote.
     * 
     * @return the quoted string.
     * 
     * @since Android 1.0
     */
    public static String quote(String s) {
        StringBuffer sb = new StringBuffer().append("\\Q");
        int apos = 0;
        int k;
        while ((k = s.indexOf("\\E", apos)) >= 0) {
            sb.append(s.substring(apos, k + 2)).append("\\\\E\\Q");
            apos = k + 2;
        }

        return sb.append(s.substring(apos)).append("\\E").toString();
    }
    
    @Override
    public String toString() {
        return pattern;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativePattern != 0) {
                NativeRegEx.close(mNativePattern);
            }
        }
        finally {
            super.finalize();
        }
    }

    /**
     * Provides serialization support
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        compileImpl(pattern, flags);
    }

}

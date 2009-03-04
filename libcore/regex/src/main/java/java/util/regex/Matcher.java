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

import com.ibm.icu4jni.regex.NativeRegEx;

/**
 * Provides a means of matching regular expressions against a given input,
 * finding occurrences of regular expressions in a given input, or replacing
 * parts of a given input. A {@code Matcher} instance has an associated {@link
 * Pattern} instance and an input text. A typical use case is to
 * iteratively find all occurrences of the {@code Pattern}, until the end of
 * the input is reached, as the following example illustrates:
 * 
 * <p/>
 * 
 * <pre>
 * Pattern p = Pattern.compile("[A-Za-z]+");
 *  
 * Matcher m = p.matcher("Hello, Android!");
 * while (m.find()) {
 *     System.out.println(m.group()); // prints "Hello" and "Android"
 * }
 * </pre>
 * 
 * <p/>
 * 
 * The {@code Matcher} has a state that results from the previous operations.
 * For example, it knows whether the most recent attempt to find the
 * {@code Pattern} was successful and at which position the next attempt would
 * resume the search. Depending on the application's needs, it may become
 * necessary to explicitly {@link #reset()} this state from time to time.
 * 
 * @since Android 1.0
 */
public final class Matcher implements MatchResult {

    /**
     * Holds the pattern, that is, the compiled regular expression.
     */
    private Pattern pattern;

    /**
     * Holds the handle for the native version of the pattern.
     */
    private int nativePattern;
    
    /**
     * Holds the input text.
     */
    private String input = "";

    /**
     * Holds the start of the region, or 0 if the matching should start at the
     * beginning of the text.
     */
    private int regionStart;
    
    /**
     * Holds the end of the region, or input.length() if the matching should
     * go until the end of the input.
     */
    private int regionEnd;

    /**
     * Reflects whether we just reset the matcher or whether we already
     * started some find/replace operations.
     */
    private boolean searching;
    
    /**
     * Holds the position where the next find operation will take place. 
     */
    private int findPos;
    
    /**
     * Holds the position where the next append operation will take place. 
     */
    private int appendPos;
    
    /**
     * Reflects whether a match has been found during the most recent find
     * operation.
     */
    private boolean matchFound;

    /**
     * Holds the offsets for the most recent match.
     */
    private int[] matchOffsets;

    /**
     * Reflects whether the bounds of the region are anchoring.
     */
    private boolean anchoringBounds = true;
    
    /**
     * Reflects whether the bounds of the region are transparent.
     */
    private boolean transparentBounds;

    /**
     * Creates a matcher for a given combination of pattern and input. Both
     * elements can be changed later on.
     * 
     * @param pattern
     *            the pattern to use.
     * @param input
     *            the input to use.
     */
    Matcher(Pattern pattern, CharSequence input) {
        usePattern(pattern);
        reset(input);
    }

    /**
     * Resets the Matcher. A new input sequence and a new region can be
     * specified. Results of a previous find get lost. The next attempt to find
     * an occurrence of the Pattern in the string will start at the beginning of
     * the region. This is the internal version of reset() to which the several
     * public versions delegate.
     * 
     * @param input
     *            the input sequence.
     * @param start
     *            the start of the region.
     * @param end
     *            the end of the region.
     * 
     * @return the matcher itself.
     */
    private Matcher reset(CharSequence input, int start, int end) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        
        if (start < 0 || end < 0 || start > input.length() || 
                end > input.length() || start > end) {
            throw new IllegalArgumentException();
        }

        // Maybe should have a reset() here, but it makes thing worse...
        // NativeRegEx.reset(nativePattern, 0);
        
        if (!input.equals(this.input)) {
            this.input = input.toString();
            
            NativeRegEx.setText(nativePattern, this.input);
            
            regionStart = 0;
            regionEnd = input.length();
        }

        if (start != regionStart || end != regionEnd) {
            regionStart = start;
            regionEnd = end;

            NativeRegEx.setRegion(nativePattern, regionStart, regionEnd);
        }

        searching = false;
        matchFound = false;
        findPos = regionStart;
        appendPos = 0;
        
        return this;
    }

    /**
     * Resets the {@code Matcher}. This results in the region being set to the
     * whole input. Results of a previous find get lost. The next attempt to
     * find an occurrence of the {@link Pattern} in the string will start at the
     * beginning of the input.
     * 
     * @return the {@code Matcher} itself.
     * 
     * @since Android 1.0
     */
    public Matcher reset() {
        return reset(input, 0, input.length());
    }

    /**
     * Provides a new input and resets the {@code Matcher}. This results in the
     * region being set to the whole input. Results of a previous find get lost.
     * The next attempt to find an occurrence of the {@link Pattern} in the
     * string will start at the beginning of the input.
     * 
     * @param input
     *            the new input sequence.
     * 
     * @return the {@code Matcher} itself.
     * 
     * @since Android 1.0
     */
    public Matcher reset(CharSequence input) {
        return reset(input, 0, input.length());
    }

    /**
     * Sets a new pattern for the {@code Matcher}. Results of a previous find
     * get lost. The next attempt to find an occurrence of the {@link Pattern}
     * in the string will start at the beginning of the input.
     * 
     * @param pattern
     *            the new {@code Pattern}.
     * 
     * @return the {@code Matcher} itself.
     * 
     * @since Android 1.0
     */
    public Matcher usePattern(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        }
        
        this.pattern = pattern;
        
        if (nativePattern != 0) {
            NativeRegEx.close(nativePattern);
        }
        nativePattern = NativeRegEx.clone(pattern.mNativePattern);

        if (input != null) {
            NativeRegEx.setText(nativePattern, input);
            NativeRegEx.setRegion(nativePattern, regionStart, regionEnd);
            NativeRegEx.useAnchoringBounds(nativePattern, anchoringBounds);
            NativeRegEx.useTransparentBounds(nativePattern, transparentBounds);
        }
        
        matchOffsets = new int[(this.pattern.mGroupCount + 1) * 2];
        matchFound = false;
        return this;
    }

    /**
     * Returns the {@link Pattern} instance used inside this matcher.
     * 
     * @return the {@code Pattern} instance.
     * 
     * @since Android 1.0
     */
    public Pattern pattern() {
        return pattern;
    }

    /**
     * Returns the number of groups in the results, which is always equal to
     * the number of groups in the original regular expression.
     * 
     * @return the number of groups.
     * 
     * @since Android 1.0
     */
    public int groupCount() {
        return pattern.mGroupCount;
    }
    
    /**
     * Resets this matcher and sets a region. Only characters inside the region
     * are considered for a match.
     * 
     * @param start
     *            the first character of the region.
     * @param end
     *            the first character after the end of the region.
     * @return the {@code Matcher} itself.
     * @since Android 1.0
     */
    public Matcher region(int start, int end) {
        return reset(input, start, end);
    }

    /**
     * Returns this matcher's region start, that is, the first character that is
     * considered for a match.
     * 
     * @return the start of the region.
     * @since Android 1.0
     */
    public int regionStart() {
        return regionStart;
    }

    /**
     * Returns this matcher's region end, that is, the first character that is
     * not considered for a match.
     * 
     * @return the end of the region.
     * @since Android 1.0
     */
    public int regionEnd() {
        return regionEnd;
    }

    /**
     * Determines whether this matcher has anchoring bounds enabled or not. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     * 
     * @param value
     *            the new value for anchoring bounds.
     * @return the {@code Matcher} itself.
     * @since Android 1.0
     */
    public Matcher useAnchoringBounds(boolean value) {
        anchoringBounds = value;
        NativeRegEx.useAnchoringBounds(nativePattern, value);
        return this;
    }
    
    /**
     * Indicates whether this matcher has anchoring bounds enabled. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     * 
     * @return true if (and only if) the {@code Matcher} uses anchoring bounds.
     * @since Android 1.0
     */
    public boolean hasAnchoringBounds() {
        return anchoringBounds;
    }

    /**
     * Determines whether this matcher has transparent bounds enabled or not.
     * When transparent bounds are enabled, the parts of the input outside the
     * region are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     * 
     * @param value
     *            the new value for transparent bounds.
     * @return the {@code Matcher} itself.
     * @since Android 1.0
     */
    public Matcher useTransparentBounds(boolean value) {
        transparentBounds = value;
        NativeRegEx.useTransparentBounds(nativePattern, value);
        return this;
    }
    
    /**
     * Indicates whether this matcher has transparent bounds enabled. When
     * transparent bounds are enabled, the parts of the input outside the region
     * are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     * 
     * @return true if (and only if) the {@code Matcher} uses anchoring bounds.
     * @since Android 1.0
     */
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }
    
    /**
     * Makes sure that a successful match has been made. Is invoked internally
     * from various places in the class.
     * 
     * @throws IllegalStateException
     *             if no successful match has been made.
     * 
     * @since Android 1.0
     */
    private void ensureMatch() throws IllegalStateException {
        if (!matchFound) {
            throw new IllegalStateException("No successful match so far");
        }
    }
    
    /**
     * Returns the next occurrence of the {@link Pattern} in the input. If a
     * previous match was successful, the method continues the search from the
     * first character following that match in the input. Otherwise it searches
     * either from the region start (if one has been set), or from position 0.
     * 
     * @return true if (and only if) a match has been found.
     * @since Android 1.0
     */
    public boolean find() {
        if (!searching) {
            searching = true;
            matchFound = NativeRegEx.find(nativePattern, -1);
        } else {
            matchFound = NativeRegEx.findNext(nativePattern);
        }

        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        
        return matchFound;
    }

    /**
     * Returns the next occurrence of the {@link Pattern} in the input. The
     * method starts the search from the given character in the input.
     * 
     * @param start
     *            The index in the input at which the find operation is to
     *            begin. If this is less than the start of the region, it is
     *            automatically adjusted to that value. If it is beyond the end
     *            of the region, the method will fail.
     * @return true if (and only if) a match has been found.
     * @since Android 1.0
     */
    public boolean find(int start) {
        findPos = start;
        
        if (findPos < regionStart) {
            findPos = regionStart;
        } else if (findPos >= regionEnd) {
            matchFound = false;
            return false;
        }
        
        matchFound = NativeRegEx.find(nativePattern, findPos); 
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        
        return matchFound;
    }

    /**
     * Tries to match the {@link Pattern} against the entire region (or the
     * entire input, if no region has been set).
     * 
     * @return true if (and only if) the {@code Pattern} matches the entire
     *         region.
     * 
     * @since Android 1.0
     */
    public boolean matches() {
        matchFound = NativeRegEx.matches(nativePattern, -1); 
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        
        return matchFound;
    }

    /**
     * Tries to match the {@link Pattern}, starting from the beginning of the
     * region (or the beginning of the input, if no region has been set).
     * Doesn't require the {@code Pattern} to match against the whole region.
     * 
     * @return true if (and only if) the {@code Pattern} matches.
     * 
     * @since Android 1.0
     */
    public boolean lookingAt() {
        matchFound = NativeRegEx.lookingAt(nativePattern, -1); 
        if (matchFound) {
            NativeRegEx.startEnd(nativePattern, matchOffsets);
            findPos = matchOffsets[1];
        }
        
        return matchFound;
    }

    /**
     * Returns the index of the first character of the text that matched the
     * whole regular expression.
     * 
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public int start() throws IllegalStateException {
        return start(0);
    }

    /**
     * Returns the index of the first character of the text that matched a given
     * group.
     * 
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public int start(int group) throws IllegalStateException {
        ensureMatch();
        return matchOffsets[group * 2];
    }

    /**
     * Returns the index of the first character following the text that matched
     * the whole regular expression.
     * 
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public int end() {
        return end(0);
    }

    /**
     * Returns the index of the first character following the text that matched
     * a given group.
     * 
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public int end(int group) {
        ensureMatch();
        return matchOffsets[(group * 2) + 1];
    }

    /**
     * Returns the text that matched the whole regular expression.
     * 
     * @return the text.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public String group() {
        return group(0);
    }

    /**
     * Returns the text that matched a given group of the regular expression.
     * 
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the text that matched the group.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public String group(int group) {
        ensureMatch();
        int from = matchOffsets[group * 2];
        int to = matchOffsets[(group * 2) + 1];
        if (from == -1 || to == -1) {
            return null;
        } else {
            return input.substring(from, to);
        }
    }

    /**
     * Indicates whether the last match hit the end of the input.
     * 
     * @return true if (and only if) the last match hit the end of the input.
     * @since Android 1.0
     */
    public boolean hitEnd() {
        return NativeRegEx.hitEnd(nativePattern);
    }
    
    /**
     * Indicates whether more input might change a successful match into an
     * unsuccessful one.
     * 
     * @return true if (and only if) more input might change a successful match
     *         into an unsuccessful one.
     * @since Android 1.0
     */
    public boolean requireEnd() {
        return NativeRegEx.requireEnd(nativePattern);
    }
    
    /**
     * Converts the current match into a separate {@link MatchResult} instance
     * that is independent from this matcher. The new object is unaffected when
     * the state of this matcher changes.
     * 
     * @return the new {@code MatchResult}.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public MatchResult toMatchResult() {
        ensureMatch();
        return new MatchResultImpl(input, matchOffsets);
    }

    /**
     * Appends a literal part of the input plus a replacement for the current
     * match to a given {@link StringBuffer}. The literal part is exactly the
     * part of the input between the previous match and the current match. The
     * method can be used in conjunction with {@link #find()} and
     * {@link #appendTail(StringBuffer)} to walk through the input and replace
     * all occurrences of the {@code Pattern} with something else.
     * 
     * @param buffer
     *            the {@code StringBuffer} to append to.
     * @param replacement
     *            the replacement text.
     * @return the {@code Matcher} itself.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public Matcher appendReplacement(StringBuffer buffer, String replacement)
            throws IllegalStateException {
        
        buffer.append(input.substring(appendPos, start()));
        appendEvaluated(buffer, replacement);
        appendPos = end();
        
        return this;
    }

    /**
     * Appends the (unmatched) remainder of the input to the given
     * {@link StringBuffer}. The method can be used in conjunction with
     * {@link #find()} and {@link #appendReplacement(StringBuffer, String)} to
     * walk through the input and replace all matches of the {@code Pattern}
     * with something else.
     * 
     * @param buffer
     *            the {@code StringBuffer} to append to.
     * @return the {@code StringBuffer}.
     * @throws IllegalStateException
     *             if no successful match has been made.
     * @since Android 1.0
     */
    public StringBuffer appendTail(StringBuffer buffer) {
        if (appendPos < regionEnd) {
            buffer.append(input.substring(appendPos, regionEnd));
        }
        
        return buffer;
    }

    /**
     * Internal helper method to append a given string to a given string buffer.
     * If the string contains any references to groups, these are replaced by
     * the corresponding group's contents.
     * 
     * @param buffer
     *            the string buffer.
     * @param s
     *            the string to append.
     */
    private void appendEvaluated(StringBuffer buffer, String s) {
        boolean escape = false;
        boolean dollar = false;
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar) {
                buffer.append(group(c - '0'));
                dollar = false;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
            }
        }
        
        // This seemingly stupid piece of code reproduces a JDK bug. 
        if (escape) {
            throw new ArrayIndexOutOfBoundsException(s.length());
        }
    }
    
    /**
     * Replaces all occurrences of this matcher's pattern in the input with a
     * given string.
     * 
     * @param replacement
     *            the replacement text.
     * @return the modified input string.
     * @since Android 1.0
     */
    public String replaceAll(String replacement) {
        StringBuffer buffer = new StringBuffer(input.length());
        
        findPos = 0;
        appendPos = 0;
        matchFound = false;
        searching = false;
        
        while (find()) {
            appendReplacement(buffer, replacement);
        }
        
        return appendTail(buffer).toString();
    }

    /**
     * Replaces the first occurrence of this matcher's pattern in the input with
     * a given string.
     * 
     * @param replacement
     *            the replacement text.
     * @return the modified input string.
     * @since Android 1.0
     */
    public String replaceFirst(String replacement) {
        StringBuffer buffer = new StringBuffer(input.length());

        findPos = 0;
        appendPos = 0;
        matchFound = false;
        searching = false;
        
        if (find()) {
            appendReplacement(buffer, replacement);
        }
        
        return appendTail(buffer).toString();
    }

    /**
     * Returns a replacement string for the given one that has all backslashes
     * and dollar signs escaped.
     * 
     * @param s
     *            the input string.
     * @return the input string, with all backslashes and dollar signs having
     *         been escaped.
     * @since Android 1.0
     */
    public static String quoteReplacement(String s) {
        StringBuffer buffer = new StringBuffer(s.length());
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                buffer.append('\\');
            }
            buffer.append(c);
        }
        
        return buffer.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (nativePattern != 0) {
                NativeRegEx.close(nativePattern);
            }
        }
        finally {
            super.finalize();
        }
    }
    
}

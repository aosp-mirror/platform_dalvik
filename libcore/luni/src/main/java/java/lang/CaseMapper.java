/*
 * Copyright (C) 2010 The Android Open Source Project
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

package java.lang;

import java.util.Locale;

/**
 * Performs case operations as described by http://unicode.org/reports/tr21/tr21-5.html.
 */
class CaseMapper {
    // Intention-revealing constants for various important characters.
    private static final char LATIN_CAPITAL_I = 'I';
    private static final char LATIN_SMALL_I = 'i';
    private static final char LATIN_CAPITAL_I_WITH_DOT = '\u0130';
    private static final char LATIN_SMALL_DOTLESS_I = '\u0131';
    private static final char COMBINING_DOT_ABOVE = '\u0307';
    private static final char GREEK_CAPITAL_SIGMA = '\u03a3';
    private static final char GREEK_SMALL_FINAL_SIGMA = '\u03c2';
    
    /**
     * Our current GC makes short-lived objects more expensive than we'd like. When that's fixed,
     * this class should be changed so that you instantiate it with the String and its value,
     * offset, and count fields.
     */
    private CaseMapper() {
    }
    
    /**
     * Implements String.toLowerCase. We need 's' so that we can return the original String instance
     * if nothing changes. We need 'value', 'offset', and 'count' because they're not otherwise
     * accessible.
     */
    public static String toLowerCase(Locale locale, String s, char[] value, int offset, int count) {
        String languageCode = locale.getLanguage();
        boolean turkishOrAzeri = languageCode.equals("tr") || languageCode.equals("az");
        
        char[] newValue = null;
        int newCount = 0;
        for (int i = offset, end = offset + count; i < end; ++i) {
            char ch = value[i];
            char newCh = ch;
            if (turkishOrAzeri && ch == LATIN_CAPITAL_I_WITH_DOT) {
                newCh = LATIN_SMALL_I;
            } else if (turkishOrAzeri && ch == LATIN_CAPITAL_I && !followedBy(value, offset, count, i, COMBINING_DOT_ABOVE)) {
                newCh = LATIN_SMALL_DOTLESS_I;
            } else if (turkishOrAzeri && ch == COMBINING_DOT_ABOVE && precededBy(value, offset, count, i, LATIN_CAPITAL_I)) {
                continue; // (We've already converted the preceding I, so we don't need to create newValue.)
            } else if (ch == GREEK_CAPITAL_SIGMA && isFinalSigma(value, offset, count, i)) {
                newCh = GREEK_SMALL_FINAL_SIGMA;
            } else {
                newCh = Character.toLowerCase(ch);
            }
            if (newValue == null && ch != newCh) {
                newValue = new char[count]; // The result can't be longer than the input.
                newCount = i - offset;
                System.arraycopy(value, offset, newValue, 0, newCount);
            }
            if (newValue != null) {
                newValue[newCount++] = newCh;
            }
        }
        return newValue != null ? new String(0, newCount, newValue) : s;
    }
    
    private static boolean followedBy(char[] value, int offset, int count, int index, char ch) {
        return index + 1 < offset + count && value[index + 1] == ch;
    }
    
    private static boolean precededBy(char[] value, int offset, int count, int index, char ch) {
        return index > offset && value[index - 1] == ch;
    }
    
    /**
     * True if 'index' is preceded by a sequence consisting of a cased letter and a case-ignorable
     * sequence, and 'index' is not followed by a sequence consisting of an ignorable sequence and
     * then a cased letter.
     */
    private static boolean isFinalSigma(char[] value, int offset, int count, int index) {
        // TODO: we don't skip case-ignorable sequences like we should.
        // TODO: we should add a more direct way to test for a cased letter.
        if (index <= offset) {
            return false;
        }
        char previous = value[index - 1];
        if (!(Character.isLowerCase(previous) || Character.isUpperCase(previous) || Character.isTitleCase(previous))) {
            return false;
        }
        if (index + 1 >= offset + count) {
            return true;
        }
        char next = value[index + 1];
        if (Character.isLowerCase(next) || Character.isUpperCase(next) || Character.isTitleCase(next)) {
            return false;
        }
        return true;
    }
}

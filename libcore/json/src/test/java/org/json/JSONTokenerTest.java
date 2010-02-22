/**
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json;

import junit.framework.TestCase;

/**
 * This black box test was written without inspecting the non-free org.json sourcecode.
 */
public class JSONTokenerTest extends TestCase {

    public void testNulls() throws JSONException {
        // bogus behaviour: JSONTokener accepts null, only to fail later on almost all APIs.
        new JSONTokener(null).back();

        try {
            new JSONTokener(null).more();
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).next();
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).next(3);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).next('A');
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).nextClean();
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).nextString('"');
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).nextTo('A');
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).nextTo("ABC");
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).nextValue();
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).skipPast("ABC");
            fail();
        } catch (NullPointerException e) {
        }

        try {
            new JSONTokener(null).skipTo('A');
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals("foo! at character 0 of null",
                new JSONTokener(null).syntaxError("foo!").getMessage());

        assertEquals(" at character 0 of null", new JSONTokener(null).toString());
    }

    public void testEmptyString() throws JSONException {
        JSONTokener backTokener = new JSONTokener("");
        backTokener.back();
        assertEquals(" at character 0 of ", backTokener.toString());
        assertFalse(new JSONTokener("").more());
        assertEquals('\0', new JSONTokener("").next());
        try {
            new JSONTokener("").next(3);
            fail();
        } catch (JSONException expected) {
        }
        try {
            new JSONTokener("").next('A');
            fail();
        } catch (JSONException e) {
        }
        assertEquals('\0', new JSONTokener("").nextClean());
        try {
            new JSONTokener("").nextString('"');
            fail();
        } catch (JSONException e) {
        }
        assertEquals("", new JSONTokener("").nextTo('A'));
        assertEquals("", new JSONTokener("").nextTo("ABC"));
        try {
            new JSONTokener("").nextValue();
            fail();
        } catch (JSONException e) {
        }
        new JSONTokener("").skipPast("ABC");
        assertEquals('\0', new JSONTokener("").skipTo('A'));
        assertEquals("foo! at character 0 of ",
                new JSONTokener("").syntaxError("foo!").getMessage());
        assertEquals(" at character 0 of ", new JSONTokener("").toString());
    }

    public void testCharacterNavigation() throws JSONException {
        JSONTokener abcdeTokener = new JSONTokener("ABCDE");
        assertEquals('A', abcdeTokener.next());
        assertEquals('B', abcdeTokener.next('B'));
        assertEquals("CD", abcdeTokener.next(2));
        try {
            abcdeTokener.next(2);
            fail();
        } catch (JSONException e) {
        }
        assertEquals('E', abcdeTokener.nextClean());
        assertEquals('\0', abcdeTokener.next());
        try {
            // bogus behaviour: returning an empty string should be valid
            abcdeTokener.next(0);
            fail();
        } catch (JSONException e) {
        }
        assertFalse(abcdeTokener.more());
        abcdeTokener.back();
        assertTrue(abcdeTokener.more());
        assertEquals('E', abcdeTokener.next());
    }

    public void testBackNextAndMore() throws JSONException {
        JSONTokener abcTokener = new JSONTokener("ABC");
        assertTrue(abcTokener.more());
        abcTokener.next();
        abcTokener.next();
        assertTrue(abcTokener.more());
        abcTokener.next();
        assertFalse(abcTokener.more());
        abcTokener.back();
        assertTrue(abcTokener.more());
        abcTokener.next();
        assertFalse(abcTokener.more());
        abcTokener.back();
        abcTokener.back();
        abcTokener.back();
        abcTokener.back(); // bogus behaviour: you can back up before the beginning of a String
        assertEquals('A', abcTokener.next());
    }

    public void testNextMatching() throws JSONException {
        JSONTokener abcdTokener = new JSONTokener("ABCD");
        assertEquals('A', abcdTokener.next('A'));
        try {
            abcdTokener.next('C'); // although it failed, this op consumes a character of input
            fail();
        } catch (JSONException e) {
        }
        assertEquals('C', abcdTokener.next('C'));
        assertEquals('D', abcdTokener.next('D'));
        try {
            abcdTokener.next('E');
            fail();
        } catch (JSONException e) {
        }
    }

    public void testNextN() throws JSONException {
        JSONTokener abcdeTokener = new JSONTokener("ABCDEF");
        assertEquals("", abcdeTokener.next(0));
        try {
            abcdeTokener.next(7);
            fail();
        } catch (JSONException e) {
        }
        assertEquals("ABC", abcdeTokener.next(3));
        try {
            abcdeTokener.next(4);
            fail();
        } catch (JSONException e) {
        }
        try {
            // bogus behaviour: there should be 3 characters left, but there must be an off-by-one
            // error in the implementation.
            assertEquals("DEF", abcdeTokener.next(3));
            fail();
        } catch (JSONException e) {
        }
        assertEquals("DE", abcdeTokener.next(2));
        assertEquals('F', abcdeTokener.next());
        try {
            // bogus behaviour: returning an empty string should be valid
            abcdeTokener.next(0);
            fail();
        } catch (JSONException e) {
        }
        abcdeTokener.back();
        abcdeTokener.back();
        abcdeTokener.back();
        assertEquals("DE", abcdeTokener.next(2));
        assertEquals('F', abcdeTokener.next());
    }

    public void testNextCleanComments() throws JSONException {
        JSONTokener tokener = new JSONTokener(
                "  A  /*XX*/B/*XX//XX\n//XX\nXX*/C//X//X//X\nD/*X*///X\n");
        assertEquals('A', tokener.nextClean());
        assertEquals('B', tokener.nextClean());
        assertEquals('C', tokener.nextClean());
        assertEquals('D', tokener.nextClean());
        assertEquals('\0', tokener.nextClean());
    }

    public void testNextCleanTrailingOpenComment() throws JSONException {
        try {
            new JSONTokener("  /* ").nextClean();
            fail();
        } catch (JSONException e) {
        }
        assertEquals('\0', new JSONTokener("  // ").nextClean());
    }

    public void testNextCleanNewlineDelimiters() throws JSONException {
        assertEquals('B', new JSONTokener("  // \r\n  B ").nextClean());
        assertEquals('B', new JSONTokener("  // \n  B ").nextClean());
        assertEquals('B', new JSONTokener("  // \r  B ").nextClean());
    }

    /**
     * Tests which characters tokener treats as ignorable whitespace. See Kevin Bourrillion's
     * <a href="https://spreadsheets.google.com/pub?key=pd8dAQyHbdewRsnE5x5GzKQ">list
     * of whitespace characters</a>.
     */
    public void testNextCleanWhitespace() throws JSONException {
        // This behaviour contradicts the JSON spec. It claims the only space
        // characters are space, tab, newline and carriage return. But it treats
        // many characters like whitespace! These are the same whitespace
        // characters used by String.trim(), with the exception of '\0'.
        assertEquals("character tabulation",      'A', new JSONTokener("\u0009A").nextClean());
        assertEquals("line feed",                 'A', new JSONTokener("\nA").nextClean());
        assertEquals("line tabulation",           'A', new JSONTokener("\u000bA").nextClean());
        assertEquals("form feed",                 'A', new JSONTokener("\u000cA").nextClean());
        assertEquals("carriage return",           'A', new JSONTokener("\rA").nextClean());
        assertEquals("information separator 4",   'A', new JSONTokener("\u001cA").nextClean());
        assertEquals("information separator 3",   'A', new JSONTokener("\u001dA").nextClean());
        assertEquals("information separator 2",   'A', new JSONTokener("\u001eA").nextClean());
        assertEquals("information separator 1",   'A', new JSONTokener("\u001fA").nextClean());
        assertEquals("space",                     'A', new JSONTokener("\u0020A").nextClean());
        for (char c = '\u0002'; c < ' '; c++) {
            assertEquals('A', new JSONTokener(new String(new char[] { ' ', c, 'A' })).nextClean());
        }

        // These characters are neither whitespace in the JSON spec nor the implementation
        assertEquals("null",                      '\u0000', new JSONTokener("\u0000A").nextClean());
        assertEquals("next line",                 '\u0085', new JSONTokener("\u0085A").nextClean());
        assertEquals("non-breaking space",        '\u00a0', new JSONTokener("\u00a0A").nextClean());
        assertEquals("ogham space mark",          '\u1680', new JSONTokener("\u1680A").nextClean());
        assertEquals("mongolian vowel separator", '\u180e', new JSONTokener("\u180eA").nextClean());
        assertEquals("en quad",                   '\u2000', new JSONTokener("\u2000A").nextClean());
        assertEquals("em quad",                   '\u2001', new JSONTokener("\u2001A").nextClean());
        assertEquals("en space",                  '\u2002', new JSONTokener("\u2002A").nextClean());
        assertEquals("em space",                  '\u2003', new JSONTokener("\u2003A").nextClean());
        assertEquals("three-per-em space",        '\u2004', new JSONTokener("\u2004A").nextClean());
        assertEquals("four-per-em space",         '\u2005', new JSONTokener("\u2005A").nextClean());
        assertEquals("six-per-em space",          '\u2006', new JSONTokener("\u2006A").nextClean());
        assertEquals("figure space",              '\u2007', new JSONTokener("\u2007A").nextClean());
        assertEquals("punctuation space",         '\u2008', new JSONTokener("\u2008A").nextClean());
        assertEquals("thin space",                '\u2009', new JSONTokener("\u2009A").nextClean());
        assertEquals("hair space",                '\u200a', new JSONTokener("\u200aA").nextClean());
        assertEquals("zero-width space",          '\u200b', new JSONTokener("\u200bA").nextClean());
        assertEquals("left-to-right mark",        '\u200e', new JSONTokener("\u200eA").nextClean());
        assertEquals("right-to-left mark",        '\u200f', new JSONTokener("\u200fA").nextClean());
        assertEquals("line separator",            '\u2028', new JSONTokener("\u2028A").nextClean());
        assertEquals("paragraph separator",       '\u2029', new JSONTokener("\u2029A").nextClean());
        assertEquals("narrow non-breaking space", '\u202f', new JSONTokener("\u202fA").nextClean());
        assertEquals("medium mathematical space", '\u205f', new JSONTokener("\u205fA").nextClean());
        assertEquals("ideographic space",         '\u3000', new JSONTokener("\u3000A").nextClean());
    }

    public void testNextString() throws JSONException {
        assertEquals("", new JSONTokener("'").nextString('\''));
        assertEquals("", new JSONTokener("\"").nextString('\"'));
        assertEquals("ABC", new JSONTokener("ABC'DEF").nextString('\''));
        assertEquals("ABC", new JSONTokener("ABC'''DEF").nextString('\''));

        // nextString permits slash-escaping of arbitrary characters! 
        assertEquals("ABC", new JSONTokener("A\\B\\C'DEF").nextString('\''));

        JSONTokener tokener = new JSONTokener(" 'abc' 'def' \"ghi\"");
        tokener.next();
        assertEquals('\'', tokener.next());
        assertEquals("abc", tokener.nextString('\''));
        tokener.next();
        assertEquals('\'', tokener.next());
        assertEquals("def", tokener.nextString('\''));
        tokener.next();
        assertEquals('"', tokener.next());
        assertEquals("ghi", tokener.nextString('\"'));
        assertFalse(tokener.more());
    }

    public void testNextStringNoDelimiter() throws JSONException {
        try {
            new JSONTokener("").nextString('\'');
            fail();
        } catch (JSONException e) {
        }

        JSONTokener tokener = new JSONTokener(" 'abc");
        tokener.next();
        tokener.next();
        try {
            tokener.next('\'');
            fail();
        } catch (JSONException e) {
        }
    }

    public void testNextStringEscapedQuote() throws JSONException {
        try {
            new JSONTokener("abc\\").nextString('"');
            fail();
        } catch (JSONException e) {
        }

        // we're mixing Java escaping like \" and JavaScript escaping like \\\"
        // which makes these tests extra tricky to read!
        assertEquals("abc\"def", new JSONTokener("abc\\\"def\"ghi").nextString('"'));
        assertEquals("abc\\def", new JSONTokener("abc\\\\def\"ghi").nextString('"'));
        assertEquals("abc/def", new JSONTokener("abc\\/def\"ghi").nextString('"'));
        assertEquals("abc\bdef", new JSONTokener("abc\\bdef\"ghi").nextString('"'));
        assertEquals("abc\fdef", new JSONTokener("abc\\fdef\"ghi").nextString('"'));
        assertEquals("abc\ndef", new JSONTokener("abc\\ndef\"ghi").nextString('"'));
        assertEquals("abc\rdef", new JSONTokener("abc\\rdef\"ghi").nextString('"'));
        assertEquals("abc\tdef", new JSONTokener("abc\\tdef\"ghi").nextString('"'));
    }

    public void testNextStringUnicodeEscaped() throws JSONException {
        // we're mixing Java escaping like \\ and JavaScript escaping like \\u
        assertEquals("abc def", new JSONTokener("abc\\u0020def\"ghi").nextString('"'));
        assertEquals("abcU0020def", new JSONTokener("abc\\U0020def\"ghi").nextString('"'));

        // JSON requires 4 hex characters after a unicode escape
        try {
            new JSONTokener("abc\\u002\"").nextString('"');
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONTokener("abc\\u").nextString('"');
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONTokener("abc\\u    \"").nextString('"');
            fail();
        } catch (NumberFormatException e) {
        }
        assertEquals("abc\"def", new JSONTokener("abc\\u0022def\"ghi").nextString('"'));
        try {
            new JSONTokener("abc\\u000G\"").nextString('"');
            fail();
        } catch (NumberFormatException e) {
        }
    }

    public void testNextStringNonQuote() throws JSONException {
        assertEquals("AB", new JSONTokener("ABC").nextString('C'));
        assertEquals("ABCD", new JSONTokener("AB\\CDC").nextString('C'));
        assertEquals("AB\nC", new JSONTokener("AB\\nCn").nextString('n'));
    }

    public void testNextTo() throws JSONException {
        assertEquals("ABC", new JSONTokener("ABCDEFG").nextTo("DHI"));
        assertEquals("ABCDEF", new JSONTokener("ABCDEF").nextTo(""));

        JSONTokener tokener = new JSONTokener("ABC\rDEF\nGHI\r\nJKL");
        assertEquals("ABC", tokener.nextTo("M"));
        assertEquals('\r', tokener.next());
        assertEquals("DEF", tokener.nextTo("M"));
        assertEquals('\n', tokener.next());
        assertEquals("GHI", tokener.nextTo("M"));
        assertEquals('\r', tokener.next());
        assertEquals('\n', tokener.next());
        assertEquals("JKL", tokener.nextTo("M"));

        tokener = new JSONTokener("ABCDEFGHI");
        assertEquals("ABC", tokener.nextTo("DEF"));
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('D', tokener.next());
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('E', tokener.next());
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('F', tokener.next());
        assertEquals("GHI", tokener.nextTo("DEF"));
        assertEquals("", tokener.nextTo("DEF"));

        tokener = new JSONTokener(" \t \fABC \t DEF");
        assertEquals("ABC", tokener.nextTo("DEF"));
        assertEquals('D', tokener.next());

        tokener = new JSONTokener(" \t \fABC \n DEF");
        assertEquals("ABC", tokener.nextTo("\n"));
        assertEquals("", tokener.nextTo("\n"));

        // Bogus behaviour: the tokener stops after \0 always
        tokener = new JSONTokener(" \0\t \fABC \n DEF");
        assertEquals("", tokener.nextTo("D"));
        assertEquals('\t', tokener.next());
        assertEquals("ABC", tokener.nextTo("D"));
        tokener = new JSONTokener("ABC\0DEF");
        assertEquals("ABC", tokener.nextTo("\0"));
        assertEquals("DEF", tokener.nextTo("\0"));

        tokener = new JSONTokener("");
        try {
            tokener.nextTo(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSkipPast() {
        JSONTokener tokener = new JSONTokener("ABCDEF");
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("EF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener("ABCDEF");
        tokener.skipPast("ABCDEF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener("ABCDEF");
        tokener.skipPast("G");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener("ABC\0ABC");
        tokener.skipPast("ABC");
        assertEquals('\0', tokener.next());
        assertEquals('A', tokener.next());

        tokener = new JSONTokener("\0ABC");
        tokener.skipPast("ABC");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener("ABC\nDEF");
        tokener.skipPast("DEF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener("ABC");
        tokener.skipPast("ABCDEF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener("ABCDABCDABCD");
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());

        tokener = new JSONTokener("");
        try {
            tokener.skipPast(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSkipTo() {
        JSONTokener tokener = new JSONTokener("ABCDEF");
        tokener.skipTo('A');
        assertEquals('A', tokener.next());
        tokener.skipTo('D');
        assertEquals('D', tokener.next());
        tokener.skipTo('G');
        assertEquals('E', tokener.next());
        tokener.skipTo('A');
        assertEquals('F', tokener.next());

        tokener = new JSONTokener("ABC\0DEF");
        tokener.skipTo('F');
        // bogus behaviour: skipTo gives up when it sees '\0'
        assertEquals('A', tokener.next());

        tokener = new JSONTokener("ABC\nDEF");
        tokener.skipTo('F');
        assertEquals('F', tokener.next());

        tokener = new JSONTokener("ABCfDEF");
        tokener.skipTo('F');
        assertEquals('F', tokener.next());

        tokener = new JSONTokener("ABC/* DEF */");
        tokener.skipTo('D');
        assertEquals('D', tokener.next());
    }

    public void testDehexchar() {
        assertEquals( 0, JSONTokener.dehexchar('0'));
        assertEquals( 1, JSONTokener.dehexchar('1'));
        assertEquals( 2, JSONTokener.dehexchar('2'));
        assertEquals( 3, JSONTokener.dehexchar('3'));
        assertEquals( 4, JSONTokener.dehexchar('4'));
        assertEquals( 5, JSONTokener.dehexchar('5'));
        assertEquals( 6, JSONTokener.dehexchar('6'));
        assertEquals( 7, JSONTokener.dehexchar('7'));
        assertEquals( 8, JSONTokener.dehexchar('8'));
        assertEquals( 9, JSONTokener.dehexchar('9'));
        assertEquals(10, JSONTokener.dehexchar('A'));
        assertEquals(11, JSONTokener.dehexchar('B'));
        assertEquals(12, JSONTokener.dehexchar('C'));
        assertEquals(13, JSONTokener.dehexchar('D'));
        assertEquals(14, JSONTokener.dehexchar('E'));
        assertEquals(15, JSONTokener.dehexchar('F'));
        assertEquals(10, JSONTokener.dehexchar('a'));
        assertEquals(11, JSONTokener.dehexchar('b'));
        assertEquals(12, JSONTokener.dehexchar('c'));
        assertEquals(13, JSONTokener.dehexchar('d'));
        assertEquals(14, JSONTokener.dehexchar('e'));
        assertEquals(15, JSONTokener.dehexchar('f'));

        for (int c = 0; c <= 0xFFFF; c++) {
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
                continue;
            }
            assertEquals("dehexchar " + c, -1, JSONTokener.dehexchar((char) c));
        }
    }

    public void testNextValue() {
        fail("TODO");
    }
}

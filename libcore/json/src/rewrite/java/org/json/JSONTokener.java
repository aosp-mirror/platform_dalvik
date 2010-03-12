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

package org.json;

// Note: this class was written without inspecting the non-free org.json sourcecode.

/**
 *
 */
public class JSONTokener {

    /** The input JSON. */
    private final String in;

    /**
     * The index of the next character to be returned by {@link #next()}. When
     * the input is exhausted, this equals the input's length.
     */
    private int pos;

    public JSONTokener(String in) {
        this.in = in;
    }

    public Object nextValue() throws JSONException {
        int c = nextCleanInternal();
        switch (c) {
            case -1:
                throw syntaxError("End of input");

            case '{':
                return readObject();

            case '[':
                return readArray();

            case '\'':
            case '"':
                return nextString((char) c);

            default:
                pos--;
                return readLiteral();
        }
    }

    private int nextCleanInternal() throws JSONException {
        while (pos < in.length()) {
            int c = in.charAt(pos++);
            switch (c) {
                case '\t':
                case ' ':
                case '\n':
                case '\r':
                    continue;

                case '/':
                    if (pos == in.length()) {
                        return c;
                    }

                    char peek = in.charAt(pos);
                    if (peek != '*' && peek != '/') {
                        return c;
                    }

                    skipComment();
                    continue;

                default:
                    return c;
            }
        }

        return -1;
    }

    /**
     * Advances the position until it is beyond the current comment. The opening
     * slash '/' should have already been read, and character at the current
     * position be an asterisk '*' for a C-style comment or a slash '/' for an
     * end-of-line comment.
     *
     * @throws JSONException if a C-style comment was not terminated.
     */
    private void skipComment() throws JSONException {
        if (in.charAt(pos++) == '*') {
            int commentEnd = in.indexOf("*/", pos);
            if (commentEnd == -1) {
                throw syntaxError("Unterminated comment");
            }
            pos = commentEnd + 2;

        } else {
            /*
             * Skip to the next newline character. If the line is terminated by
             * "\r\n", the '\n' will be consumed as whitespace by the caller.
             */
            for (; pos < in.length(); pos++) {
                char c = in.charAt(pos);
                if (c == '\r' || c == '\n') {
                    pos++;
                    break;
                }
            }
        }
    }

    /**
     *
     *
     * @throws NumberFormatException if any unicode escape sequences are
     *     malformed.
     */
    public String nextString(char quote) throws JSONException {
        /*
         * For strings that are free of escape sequences, we can just extract
         * the result as a substring of the input. But if we encounter an escape
         * sequence, we need to use a StringBuilder to compose the result.
         */
        StringBuilder builder = null;

        /* the index of the first character not yet appended to the builder. */
        int start = pos;

        while (pos < in.length()) {
            int c = in.charAt(pos++);
            if (c == quote) {
                if (builder == null) {
                    // a new string avoids leaking memory
                    return new String(in.substring(start, pos - 1));
                } else {
                    builder.append(in, start, pos - 1);
                    return builder.toString();
                }
            }

            if (c == '\\') {
                if (pos == in.length()) {
                    throw syntaxError("Unterminated escape sequence");
                }
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(in, start, pos - 1);
                builder.append(readEscapeCharacter());
                start = pos;
            }
        }

        throw syntaxError("Unterminated string");
    }

    /**
     * Unescapes the character identified by the character or characters that
     * immediately follow a backslash. The backslash '\' should have already
     * been read. This supports both unicode escapes "u000A" and two-character
     * escapes "\n".
     *
     * @throws NumberFormatException if any unicode escape sequences are
     *     malformed.
     */
    private char readEscapeCharacter() throws JSONException {
        char escaped = in.charAt(pos++);
        switch (escaped) {
            case 'u':
                if (pos + 4 > in.length()) {
                    throw syntaxError("Unterminated escape sequence");
                }
                String hex = in.substring(pos, pos + 4);
                pos += 4;
                return (char) Integer.parseInt(hex, 16);

            case 't':
                return '\t';

            case 'b':
                return '\b';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 'f':
                return '\f';

            case '\'':
            case '"':
            case '\\':
            default:
                return escaped;
        }
    }

    /**
     * Reads a null, boolean, numeric or unquoted string literal value. Numeric
     * values will be returned as an Integer, Long, or Double, in that order of
     * preference.
     */
    private Object readLiteral() throws JSONException {
        String literal = nextToInternal("{}[]/\\:,=;# \t\f");

        if (literal.length() == 0) {
            throw syntaxError("Expected literal value");
        } else if ("null".equalsIgnoreCase(literal)) {
            return JSONObject.NULL;
        } else if ("true".equalsIgnoreCase(literal)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(literal)) {
            return Boolean.FALSE;
        }

        /* try to parse as an integral type... */
        if (literal.indexOf('.') == -1) {
            int base = 10;
            String number = literal;
            if (number.startsWith("0x") || number.startsWith("0X")) {
                number = number.substring(2);
                base = 16;
            } else if (number.startsWith("0") && number.length() > 1) {
                number = number.substring(1);
                base = 8;
            }
            try {
                long longValue = Long.parseLong(number, base);
                if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            } catch (NumberFormatException e) {
                /*
                 * This only happens for integral numbers greater than
                 * Long.MAX_VALUE, numbers in exponential form (5e-10) and
                 * unquoted strings. Fall through to try floating point.
                 */
            }
        }

        /* ...next try to parse as a floating point... */
        try {
            return Double.valueOf(literal);
        } catch (NumberFormatException e) {
        }

        /* ... finally give up. We have an unquoted string */
        return new String(literal); // a new string avoids leaking memory
    }

    /**
     * Returns text from the current position until the first of any of the
     * given characters or a newline character, excluding that character. The
     * position is advanced to the excluded character.
     */
    private String nextToInternal(String excluded) {
        int start = pos;
        for (; pos < in.length(); pos++) {
            char c = in.charAt(pos);
            if (c == '\r' || c == '\n' || excluded.indexOf(c) != -1) {
                return in.substring(start, pos);
            }
        }
        return in.substring(start);
    }

    /**
     * Reads a sequence of key/value pairs and the trailing closing brace '}' of
     * an object. The opening brace '{' should have already been read.
     */
    private JSONObject readObject() throws JSONException {
        JSONObject result = new JSONObject();

        /* Peek to see if this is the empty object. */
        int first = nextCleanInternal();
        if (first == '}') {
            return result;
        } else if (first != -1) {
            pos--;
        }

        while (true) {
            Object name = nextValue();
            if (!(name instanceof String)) {
                if (name == null) {
                    throw syntaxError("Names cannot be null");
                } else {
                    throw syntaxError("Names must be strings, but " + name
                            + " is of type " + name.getClass().getName());
                }
            }

            /*
             * Expect the name/value separator to be either a colon ':', an
             * equals sign '=', or an arrow "=>". The last two are bogus but we
             * include them because that's what the original implementation did.
             */
            int separator = nextCleanInternal();
            if (separator != ':' && separator != '=') {
                throw syntaxError("Expected ':' after " + name);
            }
            if (pos < in.length() && in.charAt(pos) == '>') {
                pos++;
            }

            result.put((String) name, nextValue());

            switch (nextCleanInternal()) {
                case '}':
                    return result;
                case ';':
                case ',':
                    continue;
                default:
                    throw syntaxError("Unterminated object");
            }
        }
    }

    /**
     * Reads a sequence of values and the trailing closing brace ']' of an
     * array. The opening brace '[' should have already been read. Note that
     * "[]" yields an empty array, but "[,]" returns a two-element array
     * equivalent to "[null,null]".
     */
    private JSONArray readArray() throws JSONException {
        JSONArray result = new JSONArray();

        /* to cover input that ends with ",]". */
        boolean hasTrailingSeparator = false;

        while (true) {
            switch (nextCleanInternal()) {
                case -1:
                    throw syntaxError("Unterminated array");
                case ']':
                    if (hasTrailingSeparator) {
                        result.put(null);
                    }
                    return result;
                case ',':
                case ';':
                    /* A separator without a value first means "null". */
                    result.put(null);
                    hasTrailingSeparator = true;
                    continue;
                default:
                    pos--;
            }

            result.put(nextValue());

            switch (nextCleanInternal()) {
                case ']':
                    return result;
                case ',':
                case ';':
                    hasTrailingSeparator = true;
                    continue;
                default:
                    throw syntaxError("Unterminated array");
            }
        }
    }

    public JSONException syntaxError(String text) {
        return new JSONException(text + this);
    }

    @Override public String toString() {
        // consistent with the original implementation
        return " at character " + pos + " of " + in;
    }

    /*
     * Legacy APIs.
     *
     * None of the methods below are on the critical path of parsing JSON
     * documents. They exist only because they were exposed by the original
     * implementation and may be used by some clients.
     */

    public boolean more() {
        return pos < in.length();
    }

    public char next() {
        return pos < in.length() ? in.charAt(pos++) : '\0';
    }

    public char next(char c) throws JSONException {
        char result = next();
        if (result != c) {
            throw syntaxError("Expected " + c + " but was " + result);
        }
        return result;
    }

    public char nextClean() throws JSONException {
        int nextCleanInt = nextCleanInternal();
        return nextCleanInt == -1 ? '\0' : (char) nextCleanInt;
    }

    /**
     * TODO: note about how this method returns a substring, and could cause a memory leak
     */
    public String next(int length) throws JSONException {
        if (pos + length > in.length()) {
            throw syntaxError(length + " is out of bounds");
        }
        String result = in.substring(pos, pos + length);
        pos += length;
        return result;
    }

    /**
     * TODO: note about how this method returns a substring, and could cause a memory leak
     */
    public String nextTo(String excluded) {
        if (excluded == null) {
            throw new NullPointerException();
        }
        return nextToInternal(excluded).trim();
    }

    /**
     * TODO: note about how this method returns a substring, and could cause a memory leak
     */
    public String nextTo(char excluded) {
        return nextToInternal(String.valueOf(excluded)).trim();
    }

    public void skipPast(String thru) {
        int thruStart = in.indexOf(thru, pos);
        pos = thruStart == -1 ? in.length() : (thruStart + thru.length());
    }

    public char skipTo(char to) {
        for (int i = pos, length = in.length(); i < length; i++) {
            if (in.charAt(i) == to) {
                pos = i;
                return to;
            }
        }
        return '\0';
    }

    public void back() {
        if (--pos == -1) {
            pos = 0;
        }
    }

    public static int dehexchar(char hex) {
        if (hex >= '0' && hex <= '9') {
            return hex - '0';
        } else if (hex >= 'A' && hex <= 'F') {
            return hex - 'A' + 10;
        } else if (hex >= 'a' && hex <= 'f') {
            return hex - 'a' + 10;
        } else {
            return -1;
        }
    }
}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

/**
 * StreamTokenizer takes a stream and a set of tokens and parses them one at a
 * time. The different types of tokens that can be found are numbers,
 * identifiers, quoted strings, and different comment styles.
 */
public class StreamTokenizer {
    /**
     * Contains a number if the current token is a number (<code>ttype</code>
     * is <code>TT_NUMBER</code>)
     */
    public double nval;

    /**
     * Contains a string if the current token is a word (<code>ttype</code>
     * is <code>TT_WORD</code>)
     */
    public String sval;

    /**
     * After calling <code>nextToken</code>, the field <code>ttype</code>
     * contains the type of token that has been read. When a single character is
     * read, it's integer value is used. For a quoted string, the value is the
     * quoted character. If not one of those, then it is one of the following:
     * <UL>
     * <LI> <code>TT_WORD</code> - the token is a word.</LI>
     * <LI> <code>TT_NUMBER</code> - the token is a number.</LI>
     * <LI> <code>TT_EOL</code> - the end of line has been reached. Depends on
     * whether <code>eolIsSignificant</code> is <code>true</code>.</LI>
     * <LI> <code>TT_EOF</code> - the end of the stream has been reached.</LI>
     * </UL>
     */

    /**
     * The constant representing end of stream.
     */
    public static final int TT_EOF = -1;

    /**
     * The constant representing end of line.
     */
    public static final int TT_EOL = '\n';

    /**
     * The constant representing a number token.
     */
    public static final int TT_NUMBER = -2;

    /**
     * The constant representing a word token.
     */
    public static final int TT_WORD = -3;

    /**
     * Internal representation of unknown state.
     */
    private static final int TT_UNKNOWN = -4;

    /**
     * The token type
     */
    public int ttype = TT_UNKNOWN;

    /**
     * Internal character meanings, 0 implies TOKEN_ORDINARY
     */
    private byte tokenTypes[] = new byte[256];

    private static final byte TOKEN_COMMENT = 1;

    private static final byte TOKEN_QUOTE = 2;

    private static final byte TOKEN_WHITE = 4;

    private static final byte TOKEN_WORD = 8;

    private static final byte TOKEN_DIGIT = 16;

    private int lineNumber = 1;

    private boolean forceLowercase;

    private boolean isEOLSignificant;

    private boolean slashStarComments;

    private boolean slashSlashComments;

    private boolean pushBackToken;

    private boolean lastCr;

    /* One of these will have the stream */
    private InputStream inStream;

    private Reader inReader;

    private int peekChar = -2;

    /**
     * Private constructor to initialize the default values according to the
     * specification.
     */
    private StreamTokenizer() {
        /*
         * Initialize the default state per specification. All byte values 'A'
         * through 'Z', 'a' through 'z', and '\u00A0' through '\u00FF' are
         * considered to be alphabetic.
         */
        wordChars('A', 'Z');
        wordChars('a', 'z');
        wordChars(160, 255);
        /**
         * All byte values '\u0000' through '\u0020' are considered to be white
         * space.
         */
        whitespaceChars(0, 32);
        /**
         * '/' is a comment character. Single quote '\'' and double quote '"'
         * are string quote characters.
         */
        commentChar('/');
        quoteChar('"');
        quoteChar('\'');
        /**
         * Numbers are parsed.
         */
        parseNumbers();
        /**
         * Ends of lines are treated as white space, not as separate tokens.
         * C-style and C++-style comments are not recognized. These are the
         * defaults and are not needed in constructor.
         */
    }

    /**
     * Construct a new StreamTokenizer on the InputStream is. This usage of this
     * method should be replaced with the constructor which takes a Reader.
     * 
     * @param is
     *            The InputStream to parse tokens on.
     * 
     * @deprecated Use {@link #StreamTokenizer(Reader)}
     */
    @Deprecated
    public StreamTokenizer(InputStream is) {
        this();
        if (is == null) {
            throw new NullPointerException();
        }
        inStream = is;
    }

    // BEGIN android-changed
    // copied from a newer version of harmony
    /**
     * Construct a new StreamTokenizer on the Reader <code>r</code>.
     * Initialize the default state per specification.
     * <UL>
     * <LI>All byte values 'A' through 'Z', 'a' through 'z', and '&#92;u00A0'
     * through '&#92;u00FF' are considered to be alphabetic.</LI>
     * <LI>All byte values '&#92;u0000' through '&#92;u0020' are considered to
     * be white space. '/' is a comment character.</LI>
     * <LI>Single quote '\'' and double quote '"' are string quote characters.</LI>
     * <LI>Numbers are parsed.</LI>
     * <LI>Ends of lines are considered to be white space rather than separate
     * tokens.</LI>
     * <LI>C-style and C++-style comments are not recognized.</LI>
     * </UL>
     * These are the defaults and are not needed in constructor.
     * 
     * @param r
     *            The InputStream to parse tokens on.
     */
    // END android-changed
    public StreamTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        inReader = r;
    }

    /**
     * Set the character <code>ch</code> to be regarded as a comment
     * character.
     * 
     * @param ch
     *            The character to be considered a comment character.
     */
    public void commentChar(int ch) {
        if (0 <= ch && ch < tokenTypes.length) {
            tokenTypes[ch] = TOKEN_COMMENT;
        }
    }

    /**
     * Set a boolean indicating whether or not end of line is significant and
     * should be returned as <code>TT_EOF</code> in <code>ttype</code>.
     * 
     * @param flag
     *            <code>true</code> if EOL is significant, <code>false</code>
     *            otherwise.
     */
    public void eolIsSignificant(boolean flag) {
        isEOLSignificant = flag;
    }

    /**
     * Answer the current line number.
     * 
     * @return the current line number.
     */
    public int lineno() {
        return lineNumber;
    }

    /**
     * Set a boolean indicating whether or not tokens should be uppercased when
     * present in <code>sval</code>.
     * 
     * @param flag
     *            <code>true</code> if <code>sval</code> should be forced
     *            uppercase, <code>false</code> otherwise.
     */
    public void lowerCaseMode(boolean flag) {
        forceLowercase = flag;
    }

    /**
     * Answer the next token type.
     * 
     * @return The next token to be parsed.
     * 
     * @throws IOException
     *             If an IO error occurs while getting the token
     */
    public int nextToken() throws IOException {
        if (pushBackToken) {
            pushBackToken = false;
            if (ttype != TT_UNKNOWN) {
                return ttype;
            }
        }
        sval = null; // Always reset sval to null
        int currentChar = peekChar == -2 ? read() : peekChar;

        if (lastCr && currentChar == '\n') {
            lastCr = false;
            currentChar = read();
        }
        if (currentChar == -1) {
            return (ttype = TT_EOF);
        }

        byte currentType = currentChar > 255 ? TOKEN_WORD
                : tokenTypes[currentChar];
        while ((currentType & TOKEN_WHITE) != 0) {
            /**
             * Skip over white space until we hit a new line or a real token
             */
            if (currentChar == '\r') {
                lineNumber++;
                if (isEOLSignificant) {
                    lastCr = true;
                    peekChar = -2;
                    return (ttype = TT_EOL);
                }
                if ((currentChar = read()) == '\n') {
                    currentChar = read();
                }
            } else if (currentChar == '\n') {
                lineNumber++;
                if (isEOLSignificant) {
                    peekChar = -2;
                    return (ttype = TT_EOL);
                }
                currentChar = read();
            } else {
                // Advance over this white space character and try again.
                currentChar = read();
            }
            if (currentChar == -1) {
                return (ttype = TT_EOF);
            }
            currentType = currentChar > 255 ? TOKEN_WORD
                    : tokenTypes[currentChar];
        }

        /**
         * Check for digits before checking for words since digits can be
         * contained within words.
         */
        if ((currentType & TOKEN_DIGIT) != 0) {
            StringBuilder digits = new StringBuilder(20);
            boolean haveDecimal = false, checkJustNegative = currentChar == '-';
            while (true) {
                if (currentChar == '.') {
                    haveDecimal = true;
                }
                digits.append((char) currentChar);
                currentChar = read();
                if ((currentChar < '0' || currentChar > '9')
                        && (haveDecimal || currentChar != '.')) {
                    break;
                }
            }
            peekChar = currentChar;
            if (checkJustNegative && digits.length() == 1) {
                // Didn't get any other digits other than '-'
                return (ttype = '-');
            }
            try {
                nval = Double.valueOf(digits.toString()).doubleValue();
            } catch (NumberFormatException e) {
                // Unsure what to do, will write test.
                nval = 0;
            }
            return (ttype = TT_NUMBER);
        }
        // Check for words
        if ((currentType & TOKEN_WORD) != 0) {
            StringBuffer word = new StringBuffer(20);
            while (true) {
                word.append((char) currentChar);
                currentChar = read();
                if (currentChar == -1
                        || (currentChar < 256 && (tokenTypes[currentChar] & (TOKEN_WORD | TOKEN_DIGIT)) == 0)) {
                    break;
                }
            }
            peekChar = currentChar;
            sval = forceLowercase ? word.toString().toLowerCase() : word
                    .toString();
            return (ttype = TT_WORD);
        }
        // Check for quoted character
        if (currentType == TOKEN_QUOTE) {
            int matchQuote = currentChar;
            StringBuffer quoteString = new StringBuffer();
            int peekOne = read();
            while (peekOne >= 0 && peekOne != matchQuote && peekOne != '\r'
                    && peekOne != '\n') {
                boolean readPeek = true;
                if (peekOne == '\\') {
                    int c1 = read();
                    // Check for quoted octal IE: \377
                    if (c1 <= '7' && c1 >= '0') {
                        int digitValue = c1 - '0';
                        c1 = read();
                        if (c1 > '7' || c1 < '0') {
                            readPeek = false;
                        } else {
                            digitValue = digitValue * 8 + (c1 - '0');
                            c1 = read();
                            // limit the digit value to a byte
                            if (digitValue > 037 || c1 > '7' || c1 < '0') {
                                readPeek = false;
                            } else {
                                digitValue = digitValue * 8 + (c1 - '0');
                            }
                        }
                        if (!readPeek) {
                            // We've consumed one to many
                            quoteString.append((char) digitValue);
                            peekOne = c1;
                        } else {
                            peekOne = digitValue;
                        }
                    } else {
                        switch (c1) {
                            case 'a':
                                peekOne = 0x7;
                                break;
                            case 'b':
                                peekOne = 0x8;
                                break;
                            case 'f':
                                peekOne = 0xc;
                                break;
                            case 'n':
                                peekOne = 0xA;
                                break;
                            case 'r':
                                peekOne = 0xD;
                                break;
                            case 't':
                                peekOne = 0x9;
                                break;
                            case 'v':
                                peekOne = 0xB;
                                break;
                            default:
                                peekOne = c1;
                        }
                    }
                }
                if (readPeek) {
                    quoteString.append((char) peekOne);
                    peekOne = read();
                }
            }
            if (peekOne == matchQuote) {
                peekOne = read();
            }
            peekChar = peekOne;
            ttype = matchQuote;
            sval = quoteString.toString();
            return ttype;
        }
        // Do comments, both "//" and "/*stuff*/"
        if (currentChar == '/' && (slashSlashComments || slashStarComments)) {
            if ((currentChar = read()) == '*' && slashStarComments) {
                int peekOne = read();
                while (true) {
                    currentChar = peekOne;
                    peekOne = read();
                    if (currentChar == -1) {
                        peekChar = -1;
                        return (ttype = TT_EOF);
                    }
                    if (currentChar == '\r') {
                        if (peekOne == '\n') {
                            peekOne = read();
                        }
                        lineNumber++;
                    } else if (currentChar == '\n') {
                        lineNumber++;
                    } else if (currentChar == '*' && peekOne == '/') {
                        peekChar = read();
                        return nextToken();
                    }
                }
            } else if (currentChar == '/' && slashSlashComments) {
                // Skip to EOF or new line then return the next token
                while ((currentChar = read()) >= 0 && currentChar != '\r'
                        && currentChar != '\n') {
                    // Intentionally empty
                }
                peekChar = currentChar;
                return nextToken();
            } else if (currentType != TOKEN_COMMENT) {
                // Was just a slash by itself
                peekChar = currentChar;
                return (ttype = '/');
            }
        }
        // Check for comment character
        if (currentType == TOKEN_COMMENT) {
            // Skip to EOF or new line then return the next token
            while ((currentChar = read()) >= 0 && currentChar != '\r'
                    && currentChar != '\n') {
                // Intentionally empty
            }
            peekChar = currentChar;
            return nextToken();
        }

        peekChar = read();
        return (ttype = currentChar);
    }

    /**
     * Set the character <code>ch</code> to be regarded as an ordinary
     * character.
     * 
     * @param ch
     *            The character to be considered an ordinary comment character.
     */
    public void ordinaryChar(int ch) {
        if (0 <= ch && ch < tokenTypes.length) {
            tokenTypes[ch] = 0;
        }
    }

    /**
     * Set the characters ranging from <code>low</code> to <code>hi</code>
     * to be regarded as ordinary characters.
     * 
     * @param low
     *            The starting range for ordinary characters.
     * @param hi
     *            The ending range for ordinary characters.
     */
    public void ordinaryChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi > tokenTypes.length) {
            hi = tokenTypes.length - 1;
        }
        for (int i = low; i <= hi; i++) {
            tokenTypes[i] = 0;
        }
    }

    /**
     * Indicate that numbers should be parsed.
     */
    public void parseNumbers() {
        for (int i = '0'; i <= '9'; i++) {
            tokenTypes[i] |= TOKEN_DIGIT;
        }
        tokenTypes['.'] |= TOKEN_DIGIT;
        tokenTypes['-'] |= TOKEN_DIGIT;
    }

    /**
     * Indicate that the current token should be pushed back and returned the
     * next time <code>nextToken()</code> is called.
     */
    public void pushBack() {
        pushBackToken = true;
    }

    /**
     * Set the character <code>ch</code> to be regarded as a quote character.
     * 
     * @param ch
     *            The character to be considered a quote comment character.
     */
    public void quoteChar(int ch) {
        if (0 <= ch && ch < tokenTypes.length) {
            tokenTypes[ch] = TOKEN_QUOTE;
        }
    }

    private int read() throws IOException {
        // Call the read for the appropriate stream
        if (inStream == null) {
            return inReader.read();
        }
        return inStream.read();
    }

    /**
     * Reset all characters so that they are ordinary.
     */
    public void resetSyntax() {
        for (int i = 0; i < 256; i++) {
            tokenTypes[i] = 0;
        }
    }

    /**
     * Set a boolean indicating whether or not slash slash comments should be
     * recognized. The comment ends at a new line.
     * 
     * @param flag
     *            <code>true</code> if <code>//</code> should be recognized
     *            as the start of a comment, <code>false</code> otherwise.
     */
    public void slashSlashComments(boolean flag) {
        slashSlashComments = flag;
    }

    /**
     * Set a boolean indicating whether or not slash star comments should be
     * recognized. Slash-star comments cannot be nested and end when a
     * star-slash combination is found.
     * 
     * @param flag
     *            <code>true</code> if <code>/*</code> should be recognized
     *            as the start of a comment, <code>false</code> otherwise.
     */
    public void slashStarComments(boolean flag) {
        slashStarComments = flag;
    }

    /**
     * Answer the state of this tokenizer in a readable format.
     * 
     * @return The current state of this tokenizer.
     */
    @Override
    public String toString() {
        // Values determined through experimentation
        StringBuilder result = new StringBuilder();
        result.append("Token["); //$NON-NLS-1$
        switch (ttype) {
            case TT_EOF:
                result.append("EOF"); //$NON-NLS-1$
                break;
            case TT_EOL:
                result.append("EOL"); //$NON-NLS-1$
                break;
            case TT_NUMBER:
                result.append("n="); //$NON-NLS-1$
                result.append(nval);
                break;
            case TT_WORD:
                result.append(sval);
                break;
            default:
                // BEGIN android-changed
                // copied from a newer version of harmony
                if (ttype == TT_UNKNOWN || tokenTypes[ttype] == TOKEN_QUOTE) {
                    result.append(sval);
                } else {
                    result.append('\'');
                    result.append((char) ttype);
                    result.append('\'');
                }
                // END android-changed
        }
        result.append("], line "); //$NON-NLS-1$
        result.append(lineNumber);
        return result.toString();
    }

    /**
     * Set the characters ranging from <code>low</code> to <code>hi</code>
     * to be regarded as whitespace characters.
     * 
     * @param low
     *            The starting range for whitespace characters.
     * @param hi
     *            The ending range for whitespace characters.
     */
    public void whitespaceChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi > tokenTypes.length) {
            hi = tokenTypes.length - 1;
        }
        for (int i = low; i <= hi; i++) {
            tokenTypes[i] = TOKEN_WHITE;
        }
    }

    /**
     * Set the characters ranging from <code>low</code> to <code>hi</code>
     * to be regarded as word characters.
     * 
     * @param low
     *            The starting range for word characters.
     * @param hi
     *            The ending range for word characters.
     */
    public void wordChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi > tokenTypes.length) {
            hi = tokenTypes.length - 1;
        }
        for (int i = low; i <= hi; i++) {
            tokenTypes[i] |= TOKEN_WORD;
        }
    }
}

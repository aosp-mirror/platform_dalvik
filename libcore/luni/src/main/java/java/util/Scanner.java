/* Licensed to the Apache Software Foundation (ASF) under one or more
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
package java.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser that parses a text string to primitive types with the help of
 * regular expression. It supports localized number and various radixes.
 * 
 * The input is broken into tokens by the delimiter pattern, which is whitespace
 * by default. The primitive types can be got via corresponding next methods. If
 * the token is not in valid format, an InputMissmatchException is thrown.
 * 
 * For example: Scanner s = new Scanner("1A true");
 * System.out.println(s.nextInt(16)); System.out.println(s.nextBoolean()); The
 * result: 26 true
 * 
 * A scanner can find or skip specific pattern with no regard to the delimiter.
 * All these methods and the various next and hasNext methods may block.
 * 
 * Scanner is not thread-safe without external synchronization
 */
public final class Scanner implements Iterator<String> {

    //  Default delimiting pattern.
    private static final Pattern DEFAULT_DELIMITER = Pattern
            .compile("\\p{javaWhitespace}+"); //$NON-NLS-1$
    
    // The boolean's pattern.
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile(
            "true|false", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    
    
    // Pattern used to recognize line terminator.
    private static final Pattern LINE_TERMINATOR;
    
    // Pattern used to recognize multiple line terminators.
    private static final Pattern MULTI_LINE_TERMINATOR;

    // Pattern used to recognize a line with a line terminator.
    private static final Pattern LINE_PATTERN;

    static {
        String terminator = "\n|\r\n|\r|\u0085|\u2028|\u2029";  //$NON-NLS-1$
        
        LINE_TERMINATOR = Pattern.compile(terminator);
        
        StringBuilder multiTerminator = new StringBuilder();
        MULTI_LINE_TERMINATOR = Pattern
            .compile(multiTerminator.append("(") //$NON-NLS-1$
                    .append(terminator)
                    .append(")+").toString()); //$NON-NLS-1$
        StringBuilder line = new StringBuilder();
        LINE_PATTERN = Pattern
            .compile(line.append(".*(") //$NON-NLS-1$
                    .append(terminator)
                    .append(")|.+(") //$NON-NLS-1$
                    .append(terminator)
                    .append(")?").toString()); //$NON-NLS-1$
    }
    
    // The pattern matches anything.
    private static final Pattern ANY_PATTERN = Pattern.compile("(?s).*"); //$NON-NLS-1$

    private static final int DIPLOID = 2;

    // Default radix.
    private static final int DEFAULT_RADIX = 10;

    private static final int DEFAULT_TRUNK_SIZE = 1024;

    // The input source of scanner.
    private Readable input;

    private CharBuffer buffer;

    private Pattern delimiter = DEFAULT_DELIMITER;

    private Matcher matcher;

    private int integerRadix = DEFAULT_RADIX;

    private Locale locale = Locale.getDefault();

    // The position where find begins.
    private int findStartIndex = 0;

    // The last find start position.
    private int preStartIndex = findStartIndex;

    // The length of the buffer.
    private int bufferLength = 0;

    // Record the status of this scanner. True if the scanner 
    // is closed.
    private boolean closed = false;

    private IOException lastIOException;
    
    private boolean matchSuccessful = false;
    
    private DecimalFormat decimalFormat;
    
    // Records whether the underlying readable has more input.
    private boolean inputExhausted = false;
    
    private Object cacheHasNextValue = null;
    
    private int cachehasNextIndex = -1;
    
    private enum DataType{
        /*
         * Stands for Integer
         */
        INT,
        /*
         * Stands for Float
         */
        FLOAT;
    }

    /**
     * Constructs a scanner that uses File as its input. The default charset is
     * applied when reading the file.
     * 
     * @param src
     *            the file to be scanned
     * @throws FileNotFoundException
     *             if the specified file is not found
     */
    public Scanner(File src) throws FileNotFoundException {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Constructs a scanner that uses File as its input. The specified charset
     * is applied when reading the file.
     * 
     * @param src
     *            the file to be scanned
     * @param charsetName
     *            the name of the encoding type of the file
     * @throws FileNotFoundException
     *             if the specified file is not found
     * @throws IllegalArgumentException
     *            if the specified coding does not exist
     */
    public Scanner(File src, String charsetName) throws FileNotFoundException {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00a")); //$NON-NLS-1$
        }
        FileInputStream fis = new FileInputStream(src);
        if (null == charsetName) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA009")); //$NON-NLS-1$
        }
        try {
            input = new InputStreamReader(fis, charsetName);
        } catch (UnsupportedEncodingException e) {
            try {
                fis.close();
            } catch (IOException ioException) {
                // ignore
            }
            throw new IllegalArgumentException(e.getMessage());
        }
        initialization();
    }

    /**
     * Constructs a scanner that uses String as its input.
     * 
     * @param src
     *            the string to be scanned
     */
    public Scanner(String src) {
        input = new StringReader(src);
        initialization();
    }

    /**
     * Constructs a scanner that uses InputStream as its input. The default
     * charset is applied when decoding the input.
     * 
     * @param src
     *            the input stream to be scanned
     */
    public Scanner(InputStream src) {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Constructs a scanner that uses InputStream as its input. The specified
     * charset is applied when decoding the input.
     * 
     * @param src
     *            the input stream to be scanned
     * @param charsetName
     *            the encoding type of the input stream
     * @throws IllegalArgumentException
     *            if the specified character set is not found
     */
    public Scanner(InputStream src, String charsetName) {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00b")); //$NON-NLS-1$
        }
        try {
            input = new InputStreamReader(src, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        initialization();
    }

    /**
     * Constructs a scanner that uses Readable as its input.
     * 
     * @param src
     *            the Readable to be scanned
     */
    public Scanner(Readable src) {
        if (null == src) {
            throw new NullPointerException();
        }
        input = src;
        initialization();
    }

    /**
     * Constructs a scanner that uses ReadableByteChannel as its input. The
     * default charset is applied when decoding the input.
     * 
     * @param src
     *            the ReadableByteChannel to be scanned
     */
    public Scanner(ReadableByteChannel src) {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Constructs a scanner that uses ReadableByteChannel as its input. The
     * specified charset is applied when decoding the input.
     * 
     * @param src
     *            the ReadableByteChannel to be scanned
     * @param charsetName
     *            the encoding type of the content in the ReadableByteChannel
     * @throws IllegalArgumentException
     *            if the specified character set is not found           
     */
    public Scanner(ReadableByteChannel src, String charsetName) {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00d")); //$NON-NLS-1$
        }
        if (null == charsetName) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA009")); //$NON-NLS-1$
        }
        input = Channels.newReader(src, charsetName);
        initialization();
    }

    /**
     * Closes the underlying input if the input implements Closeable. If the
     * scanner has been closed, this method will take no effect. The scanning
     * operation after calling this method will throw IllegalStateException
     * 
     */
    public void close() {
        if (closed) {
            return;
        }
        if (input instanceof Closeable) {
            try {
                ((Closeable) input).close();
            } catch (IOException e) {
                lastIOException = e;
            }
        }
        closed = true;
    }

    /**
     * Returns the <code>Pattern</code> in use by this scanner.
     * 
     * @return the <code>Pattern</code> presently in use by this scanner
     */
    public Pattern delimiter() {
        return delimiter;
    }

    /**
     * Tries to find the pattern in input. Delimiters are ignored. If the
     * pattern is found before line terminator, the matched string will be
     * returned, and the scanner will advance to the end of the matched string.
     * Otherwise, null will be returned and the scanner will not advance the
     * input. When waiting for input, the scanner may be blocked.
     * 
     * All the input may be cached if no line terminator exists in the buffer.
     * 
     * @param pattern
     *            the pattern used to match input
     * @return the matched string
     * @throws IllegalStateException
     *             if the scanner is closed
     */
    public String findInLine(Pattern pattern) {
        checkClosed();
        checkNull(pattern);
        int horizonLineSeparator = 0;

        matcher.usePattern(MULTI_LINE_TERMINATOR);
        matcher.region(findStartIndex, bufferLength);

        boolean findComplete = false;
        int terminatorLength = 0;
        while (!findComplete) {
            if (matcher.find()) {
                horizonLineSeparator = matcher.start();
                terminatorLength = matcher.end() - matcher.start();
                findComplete = true;
            } else {
                if (!inputExhausted) {
                    readMore();
                    resetMatcher();
                } else {
                    horizonLineSeparator = bufferLength;
                    findComplete = true;
                }
            }
        }

        matcher.usePattern(pattern);

        /*
         * TODO The following 2 statements are used to deal with regex's
         * bug. java.util.regex.Matcher.region(int start, int end)
         * implementation does not have any effects when called. They will be
         * removed once the bug is fixed.
         */
        int oldLimit = buffer.limit();
        buffer.limit(horizonLineSeparator);
        // ========== To deal with regex bug ====================

        matcher.region(findStartIndex, horizonLineSeparator);
        if (matcher.find()) {
            // The scanner advances past the input that matched
            findStartIndex = matcher.end();
            // If the matched pattern is immediately followed by line terminator. 
            if(horizonLineSeparator == matcher.end()) {
                findStartIndex += terminatorLength;
            }
            matchSuccessful = true;

            // ========== To deal with regex bug ====================
            buffer.limit(oldLimit);
            // ========== To deal with regex bug ====================

            return matcher.group();
        }

        // ========== To deal with regex bug ====================
        buffer.limit(oldLimit);
        // ========== To deal with regex bug ====================

        matchSuccessful = false;
        return null;
    }

    /**
     * Tries to find the pattern compiled from the specified string. The
     * delimiter will be ignored. It is the same as invoke
     * findInLine(Pattern.compile(pattern))
     * 
     * @param pattern
     *            a string used to construct a pattern which in turn used to
     *            match input
     * @return the matched string
     * @throws IllegalStateException
     *             if the scanner is closed
     */
    public String findInLine(String pattern) {
        return findInLine(Pattern.compile(pattern));
    }

    /**
     * Tries to find the pattern in input from current position to the specified
     * horizon. Delimiters are ignored. If the pattern is found, the matched
     * string will be returned, and the scanner will advance to the end of the
     * matched string. Otherwise, null will be returned and scanner will not
     * advance the input. When waiting for input, the scanner may be blocked.
     * 
     * Scanner will never search exceed horizon code points from current
     * position. The position of horizon does have effects on the result of
     * match. For example, when input is "123", and current position is at zero,
     * findWithinHorizon(Pattern.compile("\\p{Digit}{3}"), 2) will return null.
     * While findWithinHorizon(Pattern.compile("\\p{Digit}{3}"), 3) will return
     * "123". Horizon is treated as a transparent, non-anchoring bound. (refer
     * to {@link Matcher#useTransparentBounds} and
     * {@link Matcher#useAnchoringBounds})
     * 
     * Horizon whose value is zero will be ignored and the whole input will be
     * used for search. Under this situation, all the input may be cached.
     * 
     * An IllegalArgumentException will be thrown out if horizon is less than
     * zero.
     * 
     * @param pattern
     *            the pattern used to scan
     * @param horizon
     *            the search limit
     * @return the matched string
     * @throws IllegalStateException
     *             if the scanner is closed
     * @throws IllegalArgumentException
     *             if horizon is less than zero
     */
    public String findWithinHorizon(Pattern pattern, int horizon) {
        checkClosed();
        checkNull(pattern);
        if (horizon < 0) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA00e")); //$NON-NLS-1$
        }
        matcher.usePattern(pattern);

        String result = null;
        int findEndIndex = 0;
        int horizonEndIndex = 0;
        if (horizon == 0) {
            horizonEndIndex = Integer.MAX_VALUE;
        } else {
            horizonEndIndex = findStartIndex + horizon;
        }
        while (true) {
            findEndIndex = bufferLength;

            // If horizon > 0, then search up to
            // min( bufferLength, findStartIndex + horizon).
            // Otherwise search until readable is exhausted.
            findEndIndex = Math.min(horizonEndIndex, bufferLength);
            // If horizon == 0, consider horizon as always outside buffer.
            boolean isHorizonInBuffer = (horizonEndIndex <= bufferLength);
            // First, try to find pattern within buffer. If pattern can not be
            // found in buffer, then expand the buffer and try again,
            // util horizonEndIndex is exceeded or no more input left.
            matcher.region(findStartIndex, findEndIndex);
            if (matcher.find()) {
                if (isHorizonInBuffer || inputExhausted) {
                    result = matcher.group();
                    break;
                }
            } else {
                // Pattern is not found in buffer while horizonEndIndex is
                // within buffer, or input is exhausted. Under this situation,
                // it can be judged that find fails.
                if (isHorizonInBuffer || inputExhausted) {
                    break;
                }
            }

            // Expand buffer and reset matcher if needed.
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            }
        }
        if (null != result) {
            findStartIndex = matcher.end();
            matchSuccessful = true;
        } else {
            matchSuccessful = false;
        }
        return result;
    }

    /**
     * Tries to find the pattern in input from current position to the specified
     * horizon. Delimiters are ignored.
     * 
     * It is the same as invoke findWithinHorizon(Pattern.compile(pattern)).
     * 
     * @param pattern
     *            the pattern used to scan
     * @param horizon
     *            the search limit
     * @return the matched string
     * @throws IllegalStateException
     *             if the scanner is closed
     * @throws IllegalArgumentException
     *             if horizon is less than zero
     */
    public String findWithinHorizon(String pattern, int horizon) {
        return findWithinHorizon(Pattern.compile(pattern), horizon);
    }

    /**
     * Returns true if this scanner has next token. This method may be blocked
     * when it is waiting for input to scan. This scanner does not advance past
     * the input.
     * 
     * @return true 
     *             iff this scanner has next token
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNext() {
        return hasNext(ANY_PATTERN);
    }

    /**
     * Returns true if this scanner's next token matches the specified pattern.
     * This method may be blocked when it is waiting for input to scan. This
     * scanner does not advance past the input that matched the pattern.
     * 
     * @param pattern
     *            the specified pattern to scan
     * @return 
     *            true iff this scanner's next token matches the specified pattern
     * @throws IllegalStateException
     *            if the scanner has been closed
     */
    public boolean hasNext(Pattern pattern) {
        checkClosed();
        checkNull(pattern);
        matchSuccessful = false;
        saveCurrentStatus();
        //if the next token exists, set the match region, otherwise return false
        if (!setTokenRegion()) {
            recoverPreviousStatus();
            return false;
        }
        matcher.usePattern(pattern);
        boolean hasNext = false;
        //check whether next token matches the specified pattern
        if (matcher.matches()) {
            cachehasNextIndex = findStartIndex;
            matchSuccessful = true;
            hasNext = true;
        }
        recoverPreviousStatus();
        return hasNext;
    }


    /**
     * Returns true if this scanner's next token matches the pattern constructed
     * from the specified string. This method may be blocked when it is waiting
     * for input to scan. This scanner does not advance past the input that
     * matched the pattern.
     * 
     * The invocation of this method in the form hasNext(pattern) behaves in the
     * same way as the invocation of hasNext(Pattern.compile(pattern)).
     * 
     * @param pattern
     *            the string specifying the pattern to scan for
     * @return true 
     *            iff this scanner's next token matches the specified pattern
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNext(String pattern) {
        return hasNext(Pattern.compile(pattern));
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * BigDecimal. The scanner does not advance past the input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid BigDecimal
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextBigDecimal() {
        Pattern floatPattern = getFloatPattern();
        boolean isBigDecimalValue = false;
        if (hasNext(floatPattern)) {
            String floatString = matcher.group();
            floatString = removeLocaleInfoFromFloat(floatString);
            try {
                cacheHasNextValue = new BigDecimal(floatString);
                isBigDecimalValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isBigDecimalValue;
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * BigInteger in the default radix. The scanner does not advance past the
     * input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid BigInteger
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextBigInteger() {
        return hasNextBigInteger(integerRadix);
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * BigInteger in the specified radix. The scanner does not advance past the
     * input.
     * 
     * @param radix
     *            the radix used to translate the token into a BigInteger
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid BigInteger
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextBigInteger(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isBigIntegerValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, DataType.INT);
            try {
                cacheHasNextValue = new BigInteger(intString, radix);
                isBigIntegerValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isBigIntegerValue;
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * boolean value. The scanner does not advance past the input that matched.
     * 
     * @return true 
     *             iff the next token in this scanner's input can be translated
     *         into a valid boolean value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextBoolean() {
        return hasNext(BOOLEAN_PATTERN);
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * byte value in the default radix. The scanner does not advance past the
     * input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid byte value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextByte() {
        return hasNextByte(integerRadix);
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * byte value in the specified radix. The scanner does not advance past the
     * input.
     * 
     * @param radix
     *            the radix used to translate the token into a byte value
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid byte value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextByte(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isByteValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, DataType.INT);
            try {
                cacheHasNextValue = Byte.valueOf(intString, radix);
                isByteValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isByteValue;
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * double value. The scanner does not advance past the input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid double value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextDouble() {
        Pattern floatPattern = getFloatPattern();
        boolean isDoubleValue = false;
        if (hasNext(floatPattern)) {
            String floatString = matcher.group();
            floatString = removeLocaleInfoFromFloat(floatString);
            try {
                cacheHasNextValue = Double.valueOf(floatString);
                isDoubleValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isDoubleValue;
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * float value. The scanner does not advance past the input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid float value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextFloat() {
        Pattern floatPattern = getFloatPattern();
        boolean isFloatValue = false;
        if (hasNext(floatPattern)) {
            String floatString = matcher.group();
            floatString = removeLocaleInfoFromFloat(floatString);
            try {
                cacheHasNextValue = Float.valueOf(floatString);
                isFloatValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isFloatValue;
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * int value in the default radix. The scanner does not advance past the
     * input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid int value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextInt() {
        return hasNextInt(integerRadix);
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * int value in the specified radix. The scanner does not advance past the
     * input.
     * 
     * @param radix
     *            the radix used to translate the token into an int value
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid int value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextInt(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isIntValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, DataType.INT);
            try {
            	cacheHasNextValue = Integer.valueOf(intString, radix);
                isIntValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isIntValue;
    }
    
    /**
     * Returns true if there is another line in the input. Otherwise, returns
     * false. When waiting for input, the scanner may be blocked. No matter true
     * or false, the scanner will not advance any input.
     * 
     * @return true if there is another line in the input. Otherwise, false will
     *         be returned.
     * @throws IllegalStateException
     *             if the scanner is closed
     */
    public boolean hasNextLine() {
        checkClosed();
        matcher.usePattern(LINE_PATTERN);
        matcher.region(findStartIndex, bufferLength);

        boolean hasNextLine = false;
        while (true) {
            if (matcher.find()) {
                if (inputExhausted || matcher.end() != bufferLength) {
                    matchSuccessful = true;
                    hasNextLine = true;
                    break;
                }
            } else {
                if (inputExhausted) {
                    matchSuccessful = false;
                    break;
                }
            }
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            }
        }
        return hasNextLine;
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * long value in the default radix. The scanner does not advance past the
     * input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid long value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextLong() {
        return hasNextLong(integerRadix);
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * long value in the specified radix. The scanner does not advance past the
     * input.
     * 
     * @param radix
     *            the radix used to translate the token into a long value
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid long value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextLong(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isLongValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, DataType.INT);
            try {
                cacheHasNextValue = Long.valueOf(intString, radix);
                isLongValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isLongValue;
    }


    /**
     * Returns true if this scanner's next token can be translated into a valid
     * short value in the default radix. The scanner does not advance past the
     * input.
     * 
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid short value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextShort() {
        return hasNextShort(integerRadix);
    }

    /**
     * Returns true if this scanner's next token can be translated into a valid
     * short value in the specified radix. The scanner does not advance past the
     * input.
     * 
     * @param radix
     *            the radix used to translate the token into a short value
     * @return true iff the next token in this scanner's input can be translated
     *         into a valid short value
     * @throws IllegalStateException
     *             if the scanner has been closed
     */
    public boolean hasNextShort(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isShortValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, DataType.INT);
            try {
            	cacheHasNextValue = Short.valueOf(intString, radix);
                isShortValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isShortValue;
    }

    /**
     * Returns the last IOException thrown when reading the underlying input. If
     * no exception is thrown, return null.
     * 
     * @return the last IOException thrown
     */
    public IOException ioException() {
        return lastIOException;
    }

    /**
     * Return the locale of this scanner.
     * 
     * @return 
     *             the locale of this scanner
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Returns the match result of this scanner's last match operation.This
     * method throws IllegalStateException if no match operation has been
     * performed, or if the last match was unsuccessful.
     * 
     * The various nextXXX methods of Scanner provide a match result if they do
     * not complete with throwing an exception. For example, after an invocation
     * of the nextBoolean() method which returned a boolean value, this method
     * returns a match result for the search of the Boolean regular expression
     * defined above. In the same way,the findInLine(java.lang.String),
     * findWithinHorizon(java.lang.String, int), and
     * skip(java.util.regex.Pattern) methods will provide a match result if they
     * are successful.
     * 
     * @return the match result of the last match operation
     * @throws IllegalStateException
     *             if the match result is not available
     */
    public MatchResult match() {
        if (!matchSuccessful) {
            throw new IllegalStateException();
        }
        return matcher.toMatchResult();
    }

    /**
     * Finds and Returns the next complete token which is prefixed and postfixed
     * by input that matches the delimiter pattern. This method may be blocked
     * when it is waiting for input to scan, even if a previous invocation of
     * hasNext() returned true. If this match successes, the scanner advances
     * past the next complete token.
     * 
     * @return 
     *             the next complete token
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     */
    public String next() {
        return next(ANY_PATTERN);
    }

    /**
     * Returns the next token which is prefixed and postfixed by input that
     * matches the delimiter pattern if this token matches the specified
     * pattern. This method may be blocked when it is waiting for input to scan,
     * even if a previous invocation of hasNext(Pattern) returned true. If this
     * match successes, the scanner advances past the next token that matched
     * the pattern.
     * 
     * @param pattern
     *            the specified pattern to scan
     * @return 
     *             the next token
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     */
    public String next(Pattern pattern) {
        checkClosed();
        checkNull(pattern);
        matchSuccessful = false;
        saveCurrentStatus();
        if (!setTokenRegion()) {
            recoverPreviousStatus();
            // if setting match region fails
            throw new NoSuchElementException();
        }
        matcher.usePattern(pattern);
        if (!matcher.matches()) {
            recoverPreviousStatus();
            throw new InputMismatchException();

        }
        matchSuccessful = true;
        return matcher.group();
    }

    /**
     * Returns the next token which is prefixed and postfixed by input that
     * matches the delimiter pattern if this token matches the pattern
     * constructed from the sepcified string. This method may be blocked when it
     * is waiting for input to scan. If this match successes, the scanner
     * advances past the next token that matched the pattern.
     * 
     * The invocation of this method in the form next(pattern) behaves in the
     * same way as the invocation of next(Pattern.compile(pattern)).
     * 
     * @param pattern
     *            the string specifying the pattern to scan for
     * @return 
     *             the next token
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     */
    public String next(String pattern) {
        return next(Pattern.compile(pattern));
    }

    /**
     * Translates the next token in this scanner's input into a BigDecimal and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextBigDecimal()
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * If the next token matches the Float regular expression successfully, the
     * token is translated into a BigDecimal as following steps. At first all
     * locale specific prefixes ,group separators, and locale specific suffixes
     * are removed. Then non-ASCII digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}}, a negative sign (-) is added if the
     * locale specific negative prefixes and suffixes were present. At last the
     * resulting String is passed to {@link BigDecimal#BigDecimal(String)}}.
     * 
     * @return the BigDecimal scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             BigDecimal
     */
    public BigDecimal nextBigDecimal() {
    	checkClosed();
		Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof BigDecimal) {
			findStartIndex = cachehasNextIndex;
			return (BigDecimal) obj;
		}
        Pattern floatPattern = getFloatPattern();
        String floatString = next(floatPattern);
        floatString = removeLocaleInfoFromFloat(floatString);
        BigDecimal bigDecimalValue;
        try {
            bigDecimalValue = new BigDecimal(floatString);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return bigDecimalValue;
    }

    /**
     * Translates the next token in this scanner's input into a BigInteger and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextBigInteger()
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * The invocation of this method in the form nextBigInteger() behaves in the
     * same way as the invocation of nextBigInteger(radix), the radix is the
     * default radix of this scanner.
     * 
     * @return the BigInteger scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             BigInteger, or it is out of range
     */
    public BigInteger nextBigInteger() {
        return nextBigInteger(integerRadix);
    }

    /**
     * Translates the next token in this scanner's input into a BigInteger and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextBigInteger(radix)
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * If the next token matches the Integer regular expression successfully,
     * the token is translated into a BigInteger as following steps. At first
     * all locale specific prefixes ,group separators, and locale specific
     * suffixes are removed. Then non-ASCII digits are mapped into ASCII digits
     * via {@link Character#digit(char, int)}}, a negative sign (-) is added if
     * the locale specific negative prefixes and suffixes were present. At last
     * the resulting String is passed to
     * {@link BigInteger#BigInteger(String, int)}} with the specified radix.
     * 
     * @param radix
     *            the radix used to translate the token into a BigInteger
     * @return the int value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             BigInteger, or it is out of range
     */
    public BigInteger nextBigInteger(int radix) {
    	checkClosed();
		Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof BigInteger) {
			findStartIndex = cachehasNextIndex;
			return (BigInteger) obj;
		}
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, DataType.INT);
        BigInteger bigIntegerValue;
        try {
            bigIntegerValue = new BigInteger(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return bigIntegerValue;
    }

    /**
     * Translates the next token in this scanner's input into a boolean value and
     * returns this value. This method will throw InputMismatchException if the
     * next token can not be interpreted as a boolean value with a case
     * insensitive pattern created from the string "true|false". If this match
     * succeeds, the scanner advances past the input that matched.
     * 
     * @return the boolean value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid boolean
     *             value
     */
    public boolean nextBoolean() {
        return Boolean.parseBoolean(next(BOOLEAN_PATTERN));
    }
    
    /**
     * Translates the next token in this scanner's input into a byte value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextByte() returned
     * true. If this match succeeds, the scanner advances past the input that
     * matched.
     * 
     * The invocation of this method in the form nextByte() behaves in the same
     * way as the invocation of nextByte(radix), the radix is the default radix
     * of this scanner.
     * 
     * @return the byte value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid byte
     *             value, or it is out of range
     */
    public byte nextByte() {
        return nextByte(integerRadix);
    }

    /**
     * Translates the next token in this scanner's input into a byte value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextByte(radix)
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * If the next token matches the Integer regular expression successfully,
     * the token is translated into a byte value as following steps. At first
     * all locale specific prefixes ,group separators, and locale specific
     * suffixes are removed. Then non-ASCII digits are mapped into ASCII digits
     * via {@link Character#digit(char, int)}}, a negative sign (-) is added if
     * the locale specific negative prefixes and suffixes were present. At last
     * the resulting String is passed to {@link Byte#parseByte(String, int)}}
     * with the specified radix.
     * 
     * @param radix
     *            the radix used to translate the token into byte value
     * @return the byte value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid byte
     *             value, or it is out of range
     */
    @SuppressWarnings("boxing")
    public byte nextByte(int radix) {
    	checkClosed();
    	Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof Byte) {
			findStartIndex = cachehasNextIndex;
			return (Byte) obj;
		}
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, DataType.INT);
        byte byteValue = 0;
        try {
            byteValue = Byte.parseByte(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return byteValue;
    }

    /**
     * Translates the next token in this scanner's input into a double value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextDouble() returned
     * true. If this match succeeds, the scanner advances past the input that
     * matched.
     * 
     * If the next token matches the Float regular expression successfully, the
     * token is translated into a double value as following steps. At first all
     * locale specific prefixes ,group separators, and locale specific suffixes
     * are removed. Then non-ASCII digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}}, a negative sign (-) is added if the
     * locale specific negative prefixes and suffixes were present. At last the
     * resulting String is passed to {@link Double#parseDouble(String)}}.If the
     * token matches the localized NaN or infinity strings, it is also passed to
     * {@link Double#parseDouble(String)}}.
     * 
     * @return the double value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid double
     *             value
     */
    @SuppressWarnings("boxing")
    public double nextDouble() {
    	checkClosed();
    	Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof Double) {
			findStartIndex = cachehasNextIndex;
			return (Double) obj;
		}
        Pattern floatPattern = getFloatPattern();
        String floatString = next(floatPattern);
        floatString = removeLocaleInfoFromFloat(floatString);
        double doubleValue = 0;
        try {
            doubleValue = Double.parseDouble(floatString);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return doubleValue;
    }

    /**
     * Translates the next token in this scanner's input into a float value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextFloat() returned
     * true. If this match succeeds, the scanner advances past the input that
     * matched.
     * 
     * If the next token matches the Float regular expression successfully, the
     * token is translated into a float value as following steps. At first all
     * locale specific prefixes ,group separators, and locale specific suffixes
     * are removed. Then non-ASCII digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}}, a negative sign (-) is added if the
     * locale specific negative prefixes and suffixes were present. At last the
     * resulting String is passed to {@link Float#parseFloat(String)}}.If the
     * token matches the localized NaN or infinity strings, it is also passed to
     * {@link Float#parseFloat(String)}}.
     * 
     * @return the float value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid float
     *             value
     */
    @SuppressWarnings("boxing")
    public float nextFloat() {
    	checkClosed();
    	Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof Float) {
			findStartIndex = cachehasNextIndex;
			return (Float) obj;
		}
        Pattern floatPattern = getFloatPattern();
        String floatString = next(floatPattern);
        floatString = removeLocaleInfoFromFloat(floatString);
        float floatValue = 0;
        try {
            floatValue = Float.parseFloat(floatString);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return floatValue;
    }

    /**
     * Translates the next token in this scanner's input into an int value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextInt() returned
     * true. If this match succeeds, the scanner advances past the input that
     * matched.
     * 
     * The invocation of this method in the form nextInt() behaves in the same
     * way as the invocation of nextInt(radix), the radix is the default radix
     * of this scanner.
     * 
     * @return the int value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid int
     *             value
     */
    public int nextInt() {
        return nextInt(integerRadix);
    }

    /**
     * Translates the next token in this scanner's input into an int value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextInt(radix)
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * If the next token matches the Integer regular expression successfully,
     * the token is translated into an int value as following steps. At first
     * all locale specific prefixes ,group separators, and locale specific
     * suffixes are removed. Then non-ASCII digits are mapped into ASCII digits
     * via Character.digit, a negative sign (-) is added if the locale specific
     * negative prefixes and suffixes were present. At last the resulting String
     * is passed to Integer.parseInt with the specified radix.
     * 
     * @param radix
     *            the radix used to translate the token into an int value
     * @return the int value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid int
     *             value
     */
    @SuppressWarnings("boxing")
    public int nextInt(int radix) {
    	checkClosed();
    	Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof Integer) {
			findStartIndex = cachehasNextIndex;
			return (Integer) obj;
		}
        Pattern integerPattern = getIntegerPattern(radix);
        String intString=next(integerPattern);
        intString = removeLocaleInfo(intString, DataType.INT);
        int intValue = 0;
        try {
            intValue = Integer.parseInt(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return intValue;
    }

    /**
     * Returns the skipped input and advances the scanner to the beginning of
     * the next line. The returned result will exclude any line terminator.
     * 
     * When searching, if no line terminator is found, then a large amount of
     * input will be cached. If no line at all can be found, a
     * NoSuchElementException will be thrown out.
     * 
     * @return the skipped line
     * @throws IllegalStateException
     *             if the scanner is closed
     * @throws NoSuchElementException
     *             if no line can be found, e.g. when input is an empty string
     */
    public String nextLine() {
        checkClosed();

        matcher.usePattern(LINE_PATTERN);
        matcher.region(findStartIndex, bufferLength);
        String result = null;
        while (true) {
            if (matcher.find()) {
                if (inputExhausted || matcher.end() != bufferLength) {
                    matchSuccessful = true;
                    findStartIndex = matcher.end();
                    result = matcher.group();
                    break;
                }
            } else {
                if (inputExhausted) {
                    matchSuccessful = false;
                    throw new NoSuchElementException();
                }
            }
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            } 
        }
        // Find text without line terminator here.
        if (null != result) {
            Matcher terminatorMatcher = LINE_TERMINATOR.matcher(result);
            if (terminatorMatcher.find()) {
                result = result.substring(0, terminatorMatcher.start());
            }
        }
        return result;
    }

    /**
     * Translates the next token in this scanner's input into a long value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextLong() returned
     * true. If this match succeeds, the scanner advances past the input that
     * matched.
     * 
     * The invocation of this method in the form nextLong() behaves in the same
     * way as the invocation of nextLong(radix), the radix is the default radix
     * of this scanner.
     * 
     * @return the long value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid long
     *             value, or it is out of range
     */
    public long nextLong() {
        return nextLong(integerRadix);
    }

    /**
     * Translates the next token in this scanner's input into a long value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextLong(radix)
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * If the next token matches the Integer regular expression successfully,
     * the token is translated into a long value as following steps. At first
     * all locale specific prefixes, group separators, and locale specific
     * suffixes are removed. Then non-ASCII digits are mapped into ASCII digits
     * via {@link Character#digit(char, int)}}, a negative sign (-) is added if
     * the locale specific negative prefixes and suffixes were present. At last
     * the resulting String is passed to {@link Long#parseLong(String, int)}}
     * with the specified radix.
     * 
     * @param radix
     *            the radix used to translate the token into a long value
     * @return the long value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid long
     *             value, or it is out of range
     */
    @SuppressWarnings("boxing")
    public long nextLong(int radix) {
    	checkClosed();
    	Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof Long) {
			findStartIndex = cachehasNextIndex;
			return (Long) obj;
		}
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, DataType.INT);
        long longValue = 0;
        try {
            longValue = Long.parseLong(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return longValue;
    }

    /**
     * Translates the next token in this scanner's input into a short value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextShort() returned
     * true. If this match succeeds, the scanner advances past the input that
     * matched.
     * 
     * The invocation of this method in the form nextShort() behaves in the same
     * way as the invocation of nextShort(radix), the radix is the default radix
     * of this scanner.
     * 
     * @return the short value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid short
     *             value, or it is out of range
     */
    public short nextShort() {
        return nextShort(integerRadix);
    }

    /**
     * Translates the next token in this scanner's input into a short value and
     * returns this value. This method may be blocked when it is waiting for
     * input to scan, even if a previous invocation of hasNextShort(radix)
     * returned true. If this match succeeds, the scanner advances past the
     * input that matched.
     * 
     * If the next token matches the Integer regular expression successfully,
     * the token is translated into a short value as following steps. At first
     * all locale specific prefixes, group separators, and locale specific
     * suffixes are removed. Then non-ASCII digits are mapped into ASCII digits
     * via {@link Character#digit(char, int)}}, a negative sign (-) is added if
     * the locale specific negative prefixes and suffixes were present. At last
     * the resulting String is passed to {@link Short#parseShort(String, int)}}
     * with the specified radix.
     * 
     * @param radix
     *            the radix used to translate the token into short value
     * @return the short value scanned from the input
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid short
     *             value, or it is out of range
     */
    @SuppressWarnings("boxing")
    public short nextShort(int radix) {
    	checkClosed();
    	Object obj = cacheHasNextValue;
		cacheHasNextValue = null;
		if (obj instanceof Short) {
			findStartIndex = cachehasNextIndex;
			return (Short) obj;
		}
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, DataType.INT);
        short shortValue = 0;
        try {
            shortValue = Short.parseShort(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return shortValue;
    }

    /**
     * Return the radix of this scanner.
     * 
     * @return
     *            the radix of this scanner
     */
    public int radix() {
        return integerRadix;
    }

    /**
     * Tries to use specified pattern to match input from the current position.
     * The delimiter will be ignored. If matches, the matched input will be
     * skipped. If an anchored match of the specified pattern succeeds, input
     * will also be skipped. Otherwise, a NoSuchElementException will be thrown
     * out.
     * 
     * Patterns that can match a lot of input may cause the scanner to read in a
     * large amount of input.
     * 
     * Uses a pattern that matches nothing( sc.skip(Pattern.compile("[ \t]*")) )
     * will suppress NoSuchElementException.
     * 
     * @param pattern
     *            used to skip over input
     * @return the scanner itself
     * @throws IllegalStateException
     *             if the scanner is closed
     * @throws NoSuchElementException
     *             if the specified pattern match fails
     */
    public Scanner skip(Pattern pattern) {
        checkClosed();
        checkNull(pattern);
        matcher.usePattern(pattern);
        matcher.region(findStartIndex, bufferLength);
        while (true) {
            if (matcher.lookingAt()) {
                boolean matchInBuffer = matcher.end() < bufferLength
                        || (matcher.end() == bufferLength && inputExhausted);
                if (matchInBuffer) {
                    matchSuccessful = true;
                    findStartIndex = matcher.end();
                    break;
                }
            } else {
                if (inputExhausted) {
                    matchSuccessful = false;
                    throw new NoSuchElementException();
                }
            }
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            }
        }
        return this;
    }

    /**
     * Tries to use the specified string to construct a pattern. And then uses
     * the constructed pattern to match input from the current position. The
     * delimiter will be ignored.
     * 
     * It is the same as invoke skip(Pattern.compile(pattern))
     * 
     * @param pattern
     *            the string used to construct a pattern which in turn used to
     *            match input
     * @return the matched input
     * @throws IllegalStateException
     *             if the scanner is closed
     */
    public Scanner skip(String pattern) {
        return skip(Pattern.compile(pattern));
    }

    /**
     * Returns a string. The string is used to represent this scanner. Contained
     * information may be helpful for debugging. The format of the string is
     * unspecified.
     * 
     * @return a string to represent this scanner
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass()).append(": ") //$NON-NLS-1$
                .append("{(delimiter:") //$NON-NLS-1$
                .append(delimiter).append(")(findStartIndex=") //$NON-NLS-1$
                .append(findStartIndex).append(")(match succeed=") //$NON-NLS-1$
                .append(matchSuccessful).append(")(closed=") //$NON-NLS-1$
                .append(closed).append(")}"); //$NON-NLS-1$
        return stringBuilder.toString();
    }

    /**
     * Set the delimiting pattern of this scanner
     * 
     * @param pattern
     *            the delimiting pattern to use
     * @return this scanner
     */
    public Scanner useDelimiter(Pattern pattern) {
        delimiter = pattern;
        return this;
    }

    /**
     * Set the delimiting pattern of this scanner with a pattern compiled from
     * the supplied string value
     * 
     * @param pattern
     *            a string from which a <code>Pattern</code> can be compiled
     * @return this scanner
     */
    public Scanner useDelimiter(String pattern) {
        return useDelimiter(Pattern.compile(pattern));
    }

    /**
     * 
     * Set the locale of this scanner to a specified locale. 
     *
     * @param l
     *              the specified locale to use
     * @return
     *              this scanner
     */
    public Scanner useLocale(Locale l) {
        if (null == l) {
            throw new NullPointerException();
        }
        this.locale = l;
        return this;
    }

    /**
     * 
     * Set the radix of this scanner to a specified radix.
     * 
     * @param radix
     *             the specified radix to use
     * @return
     *             this scanner
     */
    public Scanner useRadix(int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA008", radix)); //$NON-NLS-1$
        }
        this.integerRadix = radix;
        return this;
    }

    /**
     * 
     * The operation of remove is not supported by this implementation of
     * Iterator.
     * 
     * @throws UnsupportedOperationException 
     *            if this method is invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /*
     * Initial some components.
     */
    private void initialization() {
        buffer = CharBuffer.allocate(DEFAULT_TRUNK_SIZE);
        buffer.limit(0);
        matcher = delimiter.matcher(buffer);
    }
    
    /*
     * Check the scanner's state, if it is closed, IllegalStateException will be
     * thrown.
     */
    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException();
        }
    }
    
    /*
     * Check the inputed pattern. If it is null, then a NullPointerException
     * will be thrown out.
     */
    private void checkNull(Pattern pattern) {
        if (null == pattern) {
            throw new NullPointerException();
        }
    }

    /*
     * Change the matcher's string after reading input
     */
    private void resetMatcher() {
        if (null == matcher) {
            matcher = delimiter.matcher(buffer);
        } else {
            matcher.reset(buffer);
        }
        matcher.region(findStartIndex, bufferLength);
    }

    /*
     * Save the matcher's last find position
     */
    private void saveCurrentStatus() {
        preStartIndex = findStartIndex;
    }

    /*
     * Change the matcher's status to  last find position
     */
    private void recoverPreviousStatus() {
        findStartIndex = preStartIndex;
    }
    
    /*
     * Get integer's pattern
     */
    private Pattern getIntegerPattern(int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA00e", radix)); //$NON-NLS-1$
        }
        decimalFormat = (DecimalFormat) NumberFormat.getInstance(locale);
        
        String allAvailableDigits="0123456789abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$ 
        String ASCIIDigit=allAvailableDigits.substring(0, radix);
        String nonZeroASCIIDigit=allAvailableDigits.substring(1, radix);

        StringBuilder digit = new StringBuilder("((?i)[").append(ASCIIDigit) //$NON-NLS-1$ 
                .append("]|\\p{javaDigit})"); //$NON-NLS-1$
        StringBuilder nonZeroDigit = new StringBuilder("((?i)[").append( //$NON-NLS-1$
                nonZeroASCIIDigit).append("]|([\\p{javaDigit}&&[^0]]))"); //$NON-NLS-1$
        StringBuilder numeral = getNumeral(digit, nonZeroDigit);

        StringBuilder integer = new StringBuilder("(([-+]?(").append(numeral) //$NON-NLS-1$
                .append(")))|(").append(addPositiveSign(numeral)).append(")|(") //$NON-NLS-1$ //$NON-NLS-2$
                .append(addNegativeSign(numeral)).append(")"); //$NON-NLS-1$

        Pattern integerPattern = Pattern.compile(integer.toString());
        return integerPattern;
    }

    /*
     * Get pattern of float
     */
    private Pattern getFloatPattern() {
        decimalFormat = (DecimalFormat) NumberFormat.getInstance(locale);

        StringBuilder digit = new StringBuilder("([0-9]|(\\p{javaDigit}))"); //$NON-NLS-1$
        StringBuilder nonZeroDigit = new StringBuilder("[\\p{javaDigit}&&[^0]]"); //$NON-NLS-1$
        StringBuilder numeral = getNumeral(digit, nonZeroDigit);

        String decimalSeparator = "\\" + decimalFormat.getDecimalFormatSymbols()//$NON-NLS-1$
                        .getDecimalSeparator();
        StringBuilder decimalNumeral = new StringBuilder("(").append(numeral) //$NON-NLS-1$
                .append("|").append(numeral) //$NON-NLS-1$
                .append(decimalSeparator).append(digit).append("*+|").append( //$NON-NLS-1$
                        decimalSeparator).append(digit).append("++)"); //$NON-NLS-1$
        StringBuilder exponent = new StringBuilder("([eE][+-]?").append(digit) //$NON-NLS-1$
                .append("+)?"); //$NON-NLS-1$

        StringBuilder decimal = new StringBuilder("(([-+]?").append( //$NON-NLS-1$
                decimalNumeral).append("(").append(exponent).append("?)") //$NON-NLS-1$ //$NON-NLS-2$
                .append(")|(").append(addPositiveSign(decimalNumeral)).append( //$NON-NLS-1$
                        "(").append(exponent).append("?)").append(")|(") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .append(addNegativeSign(decimalNumeral)).append("(").append( //$NON-NLS-1$
                        exponent).append("?)").append("))"); //$NON-NLS-1$ //$NON-NLS-2$

        StringBuilder hexFloat = new StringBuilder("([-+]?0[xX][0-9a-fA-F]*") //$NON-NLS-1$
                .append("\\.").append(//$NON-NLS-1$
                        "[0-9a-fA-F]+([pP][-+]?[0-9]+)?)"); //$NON-NLS-1$
        String localNaN = decimalFormat.getDecimalFormatSymbols().getNaN();
        String localeInfinity = decimalFormat.getDecimalFormatSymbols()
                .getInfinity();
        StringBuilder nonNumber = new StringBuilder("(NaN|\\Q").append(localNaN) //$NON-NLS-1$
                .append("\\E|Infinity|\\Q").append(localeInfinity).append("\\E)"); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuilder singedNonNumber = new StringBuilder("((([-+]?(").append( //$NON-NLS-1$
                nonNumber).append(")))|(").append(addPositiveSign(nonNumber)) //$NON-NLS-1$
                .append(")|(").append(addNegativeSign(nonNumber)).append("))"); //$NON-NLS-1$ //$NON-NLS-2$

        StringBuilder floatString = new StringBuilder().append(decimal).append(
                "|").append(hexFloat).append("|").append(singedNonNumber); //$NON-NLS-1$ //$NON-NLS-2$
        Pattern floatPattern = Pattern.compile(floatString.toString());
        return floatPattern;
    }

    private StringBuilder getNumeral(StringBuilder digit,
            StringBuilder nonZeroDigit) {
        String groupSeparator = "\\"//$NON-NLS-1$
                + decimalFormat.getDecimalFormatSymbols()
                        .getGroupingSeparator();
        StringBuilder groupedNumeral = new StringBuilder("(").append( //$NON-NLS-1$
                nonZeroDigit).append(digit).append("?").append(digit).append( //$NON-NLS-1$
                "?(").append(groupSeparator).append(digit).append(digit) //$NON-NLS-1$
                .append(digit).append(")+)"); //$NON-NLS-1$
        StringBuilder numeral = new StringBuilder("((").append(digit).append( //$NON-NLS-1$
                "++)|").append(groupedNumeral).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return numeral;
    }

    /*
     * Add the locale specific positive prefixes and suffixes to the pattern
     */
    private StringBuilder addPositiveSign(StringBuilder unSignNumeral) {
        String positivePrefix = ""; //$NON-NLS-1$
        String positiveSuffix = ""; //$NON-NLS-1$
        if (!decimalFormat.getPositivePrefix().equals("")) { //$NON-NLS-1$
            positivePrefix = "\\Q" + decimalFormat.getPositivePrefix()+"\\E"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!decimalFormat.getPositiveSuffix().equals("")) { //$NON-NLS-1$
            positiveSuffix = "\\Q" + decimalFormat.getPositiveSuffix()+"\\E"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        StringBuilder signedNumeral = new StringBuilder()
                .append(positivePrefix).append(unSignNumeral).append(
                        positiveSuffix);
        return signedNumeral;
    }

    /*
     * Add the locale specific negative prefixes and suffixes to the pattern
     */
    private StringBuilder addNegativeSign(StringBuilder unSignNumeral) {
        String negativePrefix = ""; //$NON-NLS-1$
        String negativeSuffix = ""; //$NON-NLS-1$
        if (!decimalFormat.getNegativePrefix().equals("")) { //$NON-NLS-1$
            negativePrefix = "\\Q" + decimalFormat.getNegativePrefix()+"\\E"; //$NON-NLS-1$//$NON-NLS-2$
        }
        if (!decimalFormat.getNegativeSuffix().equals("")) { //$NON-NLS-1$
            negativeSuffix = "\\Q" + decimalFormat.getNegativeSuffix()+"\\E"; //$NON-NLS-1$//$NON-NLS-2$
        }
        StringBuilder signedNumeral = new StringBuilder()
                .append(negativePrefix).append(unSignNumeral).append(
                        negativeSuffix);
        return signedNumeral;
    }

    /*
     * Remove locale related information from float String
     */
    private String removeLocaleInfoFromFloat(String floatString) {
        // If the token is HexFloat
        if (-1 != floatString.indexOf('x')
                || -1 != floatString.indexOf('X')) {
            return floatString;
        }
        
        int exponentIndex;
        String decimalNumeralString;
        String exponentString;
        // If the token is scientific notation
        if (-1 != (exponentIndex = floatString.indexOf('e'))
                || -1 != (exponentIndex = floatString.indexOf('E'))) {
            decimalNumeralString = floatString.substring(0, exponentIndex);
            exponentString = floatString.substring(exponentIndex + 1,
                    floatString.length());
            decimalNumeralString = removeLocaleInfo(decimalNumeralString,
                    DataType.FLOAT);
            return decimalNumeralString + "e" + exponentString; //$NON-NLS-1$ 
        }
        return removeLocaleInfo(floatString, DataType.FLOAT);
    }
    
    /*
     * Remove the locale specific prefixes, group separators, and locale
     * specific suffixes from input string
     */
    private String removeLocaleInfo(String token, DataType type) {
        StringBuilder tokenBuilder = new StringBuilder(token);
        boolean negative = removeLocaleSign(tokenBuilder);
        // Remove group separator
        String groupSeparator = String.valueOf(decimalFormat
                .getDecimalFormatSymbols().getGroupingSeparator());
        int separatorIndex = -1;
        while (-1 != (separatorIndex = tokenBuilder.indexOf(groupSeparator))) {
            tokenBuilder.delete(separatorIndex, separatorIndex + 1);
        }
        // Remove decimal separator
        String decimalSeparator = String.valueOf(decimalFormat
                .getDecimalFormatSymbols().getDecimalSeparator());
        separatorIndex = tokenBuilder.indexOf(decimalSeparator);
        StringBuilder result = new StringBuilder(""); //$NON-NLS-1$
        if (DataType.INT == type) {
            for (int i = 0; i < tokenBuilder.length(); i++) {
                if (-1 != Character.digit(tokenBuilder.charAt(i),
                        Character.MAX_RADIX)) {
                    result.append(tokenBuilder.charAt(i));
                }
            }
        }
        if (DataType.FLOAT == type) {
            if (tokenBuilder.toString().equals(decimalFormat.getDecimalFormatSymbols()
                    .getNaN())) {
                result.append("NaN");//$NON-NLS-1$ 
            } else if (tokenBuilder.toString().equals(decimalFormat
                    .getDecimalFormatSymbols().getInfinity())) {
                result.append("Infinity");//$NON-NLS-1$ 
            } else {
                for (int i = 0; i < tokenBuilder.length(); i++) {
                    if (-1 != Character.digit(tokenBuilder.charAt(i), 10)) {
                        result.append(Character.digit(tokenBuilder.charAt(i),
                                10));
                    }
                }
            }
        }
        // Token is NaN or Infinity
        if (0 == result.length()) {
            result = tokenBuilder;
        }
        if (-1 != separatorIndex) {
            result.insert(separatorIndex, "."); //$NON-NLS-1$
        }
        // If input is negative
        if (negative) {
            result.insert(0, '-');
        }
        return result.toString();
    }
    /*
     * remove positive and negative sign from the parameter stringBuilder, and
     * return whether the input string is negative
     */
    private boolean removeLocaleSign(StringBuilder tokenBuilder) {
        String positivePrefix = decimalFormat.getPositivePrefix();
        String positiveSuffix = decimalFormat.getPositiveSuffix();
        String negativePrefix = decimalFormat.getNegativePrefix();
        String negativeSuffix = decimalFormat.getNegativeSuffix();

        if (0 == tokenBuilder.indexOf("+")) { //$NON-NLS-1$
            tokenBuilder.delete(0, 1);
        }
        if (!positivePrefix.equals("") //$NON-NLS-1$
                && 0 == tokenBuilder.indexOf(positivePrefix)) {
            tokenBuilder.delete(0, positivePrefix.length());
        }
        if (!positiveSuffix.equals("") //$NON-NLS-1$
                && -1 != tokenBuilder.indexOf(positiveSuffix)) {
            tokenBuilder.delete(
                    tokenBuilder.length() - positiveSuffix.length(),
                    tokenBuilder.length());
        }
        boolean negative = false;
        if (0 == tokenBuilder.indexOf("-")) { //$NON-NLS-1$
            tokenBuilder.delete(0, 1);
            negative = true;
        }
        if (!negativePrefix.equals("") //$NON-NLS-1$
                && 0 == tokenBuilder.indexOf(negativePrefix)) {
            tokenBuilder.delete(0, negativePrefix.length());
            negative = true;
        }
        if (!negativeSuffix.equals("") //$NON-NLS-1$
                && -1 != tokenBuilder.indexOf(negativeSuffix)) {
            tokenBuilder.delete(
                    tokenBuilder.length() - negativeSuffix.length(),
                    tokenBuilder.length());
            negative = true;
        }
        return negative;
    }

    /*
     * Find the prefixed delimiter and posefixed delimiter in the input resource
     * and set the start index and end index of Matcher region. If postfixed
     * delimiter does not exist, the end index is set to be end of input.
     */
    private boolean setTokenRegion() {
        // The position where token begins
        int tokenStartIndex = 0;
        // The position where token ends
        int tokenEndIndex = 0;
        // Use delimiter pattern
        matcher.usePattern(delimiter);
        matcher.region(findStartIndex, bufferLength);

        tokenStartIndex = findPreDelimiter();
        if (setHeadTokenRegion(tokenStartIndex)) {
            return true;
        }
        tokenEndIndex = findPostDelimiter();
        // If the second delimiter is not found
        if (-1 == tokenEndIndex) {
            // Just first Delimiter Exists
            if (findStartIndex == bufferLength) {
                return false;
            }
            tokenEndIndex = bufferLength;
            findStartIndex = bufferLength;
        }

        matcher.region(tokenStartIndex, tokenEndIndex);
        return true;
    }

    /*
     * Find prefixed delimiter
     */
    private int findPreDelimiter() {
        int tokenStartIndex;
        boolean findComplete = false;
        while (!findComplete) {
            if (matcher.find()) {
                findComplete = true;
                // If just delimiter remains
                if (matcher.start() == findStartIndex
                        && matcher.end() == bufferLength) {
                    // If more input resource exists
                    if (!inputExhausted) {
                        readMore();
                        resetMatcher();
                        findComplete = false;
                    }
                }
            } else {
                if (!inputExhausted) {
                    readMore();
                    resetMatcher();
                } else {
                    return -1;
                }
            }
        }
        tokenStartIndex = matcher.end();
        findStartIndex = matcher.end();
        return tokenStartIndex;
    }

    /*
     * Handle some special cases
     */
    private boolean setHeadTokenRegion(int findIndex) {
        int tokenStartIndex;
        int tokenEndIndex;
        boolean setSuccess = false;
        // If no delimiter exists, but something exites in this scanner
        if (-1 == findIndex && preStartIndex != bufferLength) {
            tokenStartIndex = preStartIndex;
            tokenEndIndex = bufferLength;
            findStartIndex = bufferLength;
            matcher.region(tokenStartIndex, tokenEndIndex);
            setSuccess = true;
        }
        // If the first delimiter of scanner is not at the find start position
        if (-1 != findIndex && preStartIndex != matcher.start()) {
            tokenStartIndex = preStartIndex;
            tokenEndIndex = matcher.start();
            findStartIndex = matcher.start();
            // set match region and return
            matcher.region(tokenStartIndex, tokenEndIndex);
            setSuccess = true;
        }
        return setSuccess;
    }

    /*
     * Find postfixed delimiter
     */
    private int findPostDelimiter() {
        int tokenEndIndex = 0;
        boolean findComplete = false;
        while (!findComplete) {
            if (matcher.find()) {
                findComplete = true;
                if (matcher.start() == findStartIndex
                        && matcher.start() == matcher.end()) {
                    findComplete = false;
                }
            } else {
                if (!inputExhausted) {
                    readMore();
                    resetMatcher();
                } else {
                    return -1;
                }
            }
        }
        tokenEndIndex = matcher.start();
        findStartIndex = matcher.start();
        return tokenEndIndex;
    }

    /*
     * Read more data from underlying Readable. If nothing is available or I/O
     * operation fails, global boolean variable inputExhausted will be set to
     * true, otherwise set to false.
     */
    private void readMore() {
        int oldPosition = buffer.position();
        int oldBufferLength = bufferLength;
        // Increase capacity if empty space is not enough
        if (bufferLength >= buffer.capacity()) {
            expandBuffer();
        }

        // Read input resource
        int readCount = 0;
        try {
            buffer.limit(buffer.capacity());
            buffer.position(oldBufferLength);
            while ((readCount = input.read(buffer)) == 0) {
                // nothing to do here
            }
        } catch (IOException e) {
            // Consider the scenario: readable puts 4 chars into
            // buffer and then an IOException is thrown out. In this case, buffer is
            // actually grown, but readable.read() will never return.
            bufferLength = buffer.position();
            /*
             * Uses -1 to record IOException occurring, and no more input can be
             * read.
             */
            readCount = -1;
            lastIOException = e;
        }


        buffer.flip();
        buffer.position(oldPosition);
        if (-1 == readCount) {
            inputExhausted = true;
        } else {
            bufferLength = readCount + bufferLength;
        }
    }

    // Expand the size of internal buffer.
    private void expandBuffer() {
        int oldPosition = buffer.position();
        int oldCapacity = buffer.capacity();
        int oldLimit = buffer.limit();
        int newCapacity = oldCapacity * DIPLOID;
        char[] newBuffer = new char[newCapacity];
        System.arraycopy(buffer.array(), 0, newBuffer, 0, oldLimit);
        buffer = CharBuffer.wrap(newBuffer, 0, newCapacity);
        buffer.position(oldPosition);
        buffer.limit(oldLimit);
    }
}

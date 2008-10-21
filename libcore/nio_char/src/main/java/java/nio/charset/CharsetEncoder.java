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

package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.harmony.niochar.internal.nls.Messages;

/**
 * An converter that can convert 16-bit Unicode character sequence to byte
 * sequence in some charset .
 * <p>
 * The input character sequence is wrapped by
 * {@link java.nio.CharBuffer CharBuffer} and the output character sequence is
 * {@link java.nio.ByteBuffer ByteBuffer}. A encoder instance should be used in
 * following sequence, which is referred to as a encoding operation:
 * <ol>
 * <li>Invoking the {@link #reset() reset} method to reset the encoder if the
 * encoder has been used;</li>
 * <li>Invoking the {@link #encode(CharBuffer, ByteBuffer, boolean) encode}
 * method until the additional input is not needed, the <code>endOfInput</code>
 * parameter must be set to false, the input buffer must be filled and the
 * output buffer must be flushed between invocations;</li>
 * <li>Invoking the {@link #encode(CharBuffer, ByteBuffer, boolean) encode}
 * method last time, and the the <code>endOfInput</code> parameter must be set
 * to true</li>
 * <li>Invoking the {@link #flush(ByteBuffer) flush} method to flush the
 * output.</li>
 * </ol>
 * </p>
 * <p>
 * The {@link #encode(CharBuffer, ByteBuffer, boolean) encode} method will
 * convert as many characters as possible, and the process won't stop except the
 * input characters has been run out of, the output buffer has been filled or
 * some error has happened. A {@link CoderResult CoderResult} instance will be
 * returned to indicate the stop reason, and the invoker can identify the result
 * and choose further action, which can include filling the input buffer,
 * flushing the output buffer, recovering from error and trying again.
 * </p>
 * <p>
 * There are two common encoding errors. One is named as malformed and it is
 * returned when the input content is illegal 16-bit Unicode character sequence,
 * the other is named as unmappable character and occurs when there is a problem
 * mapping the input to a valid byte sequence in the specific charset.
 * </p>
 * <p>
 * The two errors can be handled in three ways, the default one is to report the
 * error to the invoker by a {@link CoderResult CoderResult} instance, and the
 * alternatives are to ignore it or to replace the erroneous input with the
 * replacement byte array. The replacement byte array is {(byte)'?'} by default
 * and can be changed by invoking {@link #replaceWith(byte[]) replaceWith}
 * method. The invoker of this encoder can choose one way by specifying a
 * {@link CodingErrorAction CodingErrorAction} instance for each error type via
 * {@link #onMalformedInput(CodingErrorAction) onMalformedInput} method and
 * {@link #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter}
 * method.
 * </p>
 * <p>
 * This class is abstract class and encapsulate many common operations of
 * encoding process for all charsets. encoder for specific charset should extend
 * this class and need only implement
 * {@link #encodeLoop(CharBuffer, ByteBuffer) encodeLoop} method for basic
 * encoding loop. If a subclass maintains internal state, it should override the
 * {@link #implFlush(ByteBuffer) implFlush} method and
 * {@link #implReset() implReset} method in addition.
 * </p>
 * <p>
 * This class is not thread-safe.
 * </p>
 * 
 * @see java.nio.charset.Charset
 * @see java.nio.charset.CharsetDecoder
 */
public abstract class CharsetEncoder {
    /*
     * --------------------------------------- Consts
     * ---------------------------------------
     */
    /*
     * internal status consts
     */
    private static final int INIT = 0;

    private static final int ONGOING = 1;

    private static final int END = 2;

    private static final int FLUSH = 3;

    /*
     * --------------------------------------- Instance variables
     * ---------------------------------------
     */
    // the Charset which creates this encoder
    private Charset cs;

    // average bytes per character created by this encoder
    private float averBytes;

    // maximum bytes per character can be created by this encoder
    private float maxBytes;

    // replacement byte array
    private byte[] replace;

    // internal status
    private int status;

    // action for malformed input
    private CodingErrorAction malformAction;

    // action for unmapped char input
    private CodingErrorAction unmapAction;

    // decoder instance for this encoder's charset, used for replacement value
    // checking
    private CharsetDecoder decoder;

    /*
     * --------------------------------------- Constructors
     * ---------------------------------------
     */

    /**
     * Construct a new <code>CharsetEncoder</code> using given
     * <code>Charset</code>, average number and maximum number of bytes
     * created by this encoder for one input character.
     * 
     * @param cs
     *            this encoder's <code>Charset</code>, which create this
     *            encoder
     * @param averageBytesPerChar
     *            average number of bytes created by this encoder for one input
     *            character, must be positive
     * @param maxBytesPerChar
     *            maximum number of bytes which can be created by this encoder
     *            for one input character, must be positive
     * @throws IllegalArgumentException
     *             if <code>maxBytesPerChar</code> or
     *             <code>averageBytePerChar</code> is negative
     */
    protected CharsetEncoder(Charset cs, float averageBytesPerChar,
            float maxBytesPerChar) {
        this(cs, averageBytesPerChar, maxBytesPerChar,
                new byte[] { (byte) '?' });
    }

    /**
     * Construct a new <code>CharsetEncoder</code> using given
     * <code>Charset</code>, replace byte array, average number and maximum
     * number of bytes created by this encoder for one input character.
     * 
     * @param cs
     *            the this encoder's <code>Charset</code>, which create this
     *            encoder
     * @param averageBytesPerChar
     *            average number of bytes created by this encoder for single
     *            input character, must be positive
     * @param maxBytesPerChar
     *            maximum number of bytes which can be created by this encoder
     *            for single input character, must be positive
     * @param replacement
     *            the replacement byte array, cannot be null or empty, its
     *            length cannot larger than <code>maxBytesPerChar</code>, and
     *            must be legal replacement, which can be justified by
     *            {@link #isLegalReplacement(byte[]) isLegalReplacement}
     * @throws IllegalArgumentException
     *             if any parameters are invalid
     */
    protected CharsetEncoder(Charset cs, float averageBytesPerChar,
            float maxBytesPerChar, byte[] replacement) {
        if (averageBytesPerChar <= 0 || maxBytesPerChar <= 0) {
            // niochar.02=Bytes number for one character must be positive.
            throw new IllegalArgumentException(Messages.getString("niochar.02")); //$NON-NLS-1$
        }
        if (averageBytesPerChar > maxBytesPerChar) {
            // niochar.03=averageBytesPerChar is greater than maxBytesPerChar.
            throw new IllegalArgumentException(Messages.getString("niochar.03")); //$NON-NLS-1$
        }
        this.cs = cs;
        averBytes = averageBytesPerChar;
        maxBytes = maxBytesPerChar;
        status = INIT;
        malformAction = CodingErrorAction.REPORT;
        unmapAction = CodingErrorAction.REPORT;
        replaceWith(replacement);
    }

    /*
     * --------------------------------------- Methods
     * ---------------------------------------
     */
    /**
     * get the average number of bytes created by this encoder for single input
     * character
     * 
     * @return the average number of bytes created by this encoder for single
     *         input character
     */
    public final float averageBytesPerChar() {
        return averBytes;
    }

    /**
     * Check if given character can be encoded by this encoder.
     * 
     * Note that this method can change the internal status of this encoder, so
     * it should not be called when another encode process is ongoing, otherwise
     * it will throw <code>IllegalStateException</code>.
     * 
     * This method can be overridden for performance improvement.
     * 
     * @param c
     *            the given encoder
     * @return true if given character can be encoded by this encoder
     * @throws IllegalStateException
     *             if another encode process is ongoing so that current internal
     *             status is neither RESET or FLUSH
     */
    public boolean canEncode(char c) {
        return implCanEncode(CharBuffer.wrap(new char[] { c }));
    }

    // implementation of canEncode
    private boolean implCanEncode(CharBuffer cb) {
        if (status == FLUSH) {
            status = INIT;
        }
        if (status != INIT) {
            // niochar.0B=Another encoding process is ongoing\!
            throw new IllegalStateException(Messages.getString("niochar.0B")); //$NON-NLS-1$
        }
        CodingErrorAction malformBak = malformAction;
        CodingErrorAction unmapBak = unmapAction;
        onMalformedInput(CodingErrorAction.REPORT);
        onUnmappableCharacter(CodingErrorAction.REPORT);
        boolean result = true;
        try {
            this.encode(cb);
        } catch (CharacterCodingException e) {
            result = false;
        }
        onMalformedInput(malformBak);
        onUnmappableCharacter(unmapBak);
        reset();
        return result;
    }

    /**
     * Check if given <code>CharSequence</code> can be encoded by this
     * encoder.
     * 
     * Note that this method can change the internal status of this encoder, so
     * it should not be called when another encode process is ongoing, otherwise
     * it will throw <code>IllegalStateException</code>.
     * 
     * This method can be overridden for performance improvement.
     * 
     * @param sequence
     *            the given <code>CharSequence</code>
     * @return true if given <code>CharSequence</code> can be encoded by this
     *         encoder
     * @throws IllegalStateException
     *             if current internal status is neither RESET or FLUSH
     */
    public boolean canEncode(CharSequence sequence) {
        CharBuffer cb;
        if (sequence instanceof CharBuffer) {
            cb = ((CharBuffer) sequence).duplicate();
        } else {
            cb = CharBuffer.wrap(sequence);
        }
        return implCanEncode(cb);
    }

    /**
     * Get the <code>Charset</code> which creates this encoder.
     * 
     * @return the <code>Charset</code> which creates this encoder
     */
    public final Charset charset() {
        return cs;
    }

    /**
     * This is a facade method for encoding operation.
     * <p>
     * This method encodes the remaining character sequence of the given
     * character buffer into a new byte buffer. This method performs a complete
     * encoding operation, resets at first, then encodes, and flushes at last.
     * </p>
     * <p>
     * This method should not be invoked if another encode operation is ongoing.
     * </p>
     * 
     * @param in
     *            the input buffer
     * @return a new <code>ByteBuffer</code> containing the the bytes produced
     *         by this encoding operation. The buffer's limit will be the
     *         position of last byte in buffer, and the position will be zero
     * @throws IllegalStateException
     *             if another encoding operation is ongoing
     * @throws MalformedInputException
     *             if illegal input character sequence for this charset
     *             encountered, and the action for malformed error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}
     * @throws UnmappableCharacterException
     *             if legal but unmappable input character sequence for this
     *             charset encountered, and the action for unmappable character
     *             error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}.
     *             Unmappable means the Unicode character sequence at the input
     *             buffer's current position cannot be mapped to a equivalent
     *             byte sequence.
     * @throws CharacterCodingException
     *             if other exception happened during the encode operation
     */
    public final ByteBuffer encode(CharBuffer in)
            throws CharacterCodingException {
        if (in.remaining() == 0) {
            return ByteBuffer.allocate(0);
        }
        reset();
        int length = (int) (in.remaining() * averBytes);
        ByteBuffer output = ByteBuffer.allocate(length);
        CoderResult result = null;
        while (true) {
            result = encode(in, output, false);
            checkCoderResult(result);
            if (result.isUnderflow()) {
                break;
            } else if (result.isOverflow()) {
                output = allocateMore(output);
            }
        }
        result = encode(in, output, true);
        checkCoderResult(result);

        while (true) {
            result = flush(output);
            checkCoderResult(result);
            if (result.isOverflow()) {
                output = allocateMore(output);
            } else {
                break;
            }
        }

        output.flip();
        if (result.isMalformed()) {
            throw new MalformedInputException(result.length());
        } else if (result.isUnmappable()) {
            throw new UnmappableCharacterException(result.length());
        }
        status = FLUSH;
        return output;
    }

    /*
     * checks the result whether it needs to throw CharacterCodingException.
     */
    private void checkCoderResult(CoderResult result)
            throws CharacterCodingException {
        if (result.isMalformed() && malformAction == CodingErrorAction.REPORT) {
            throw new MalformedInputException(result.length());
        } else if (result.isUnmappable()
                && unmapAction == CodingErrorAction.REPORT) {
            throw new UnmappableCharacterException(result.length());
        }
    }

    // allocate more spaces to the given ByteBuffer
    private ByteBuffer allocateMore(ByteBuffer output) {
        if (output.capacity() == 0) {
            return ByteBuffer.allocate(1);
        }
        ByteBuffer result = ByteBuffer.allocate(output.capacity() * 2);
        output.flip();
        result.put(output);
        return result;
    }

    /**
     * Encodes characters starting at the current position of the given input
     * buffer, and writes the equivalent byte sequence into the given output
     * buffer from its current position.
     * <p>
     * The buffers' position will be changed with the reading and writing
     * operation, but their limits and marks will be kept intact.
     * </p>
     * <p>
     * A <code>CoderResult</code> instance will be returned according to
     * following rules:
     * <ul>
     * <li>A {@link CoderResult#malformedForLength(int) malformed input} result
     * indicates that some malformed input error encountered, and the erroneous
     * characters start at the input buffer's position and their number can be
     * got by result's {@link CoderResult#length() length}. This kind of result
     * can be returned only if the malformed action is
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}. </li>
     * <li>{@link CoderResult#UNDERFLOW CoderResult.UNDERFLOW} indicates that
     * as many characters as possible in the input buffer has been encoded. If
     * there is no further input and no characters left in the input buffer then
     * this task is complete. If this is not the case then the client should
     * call this method again supplying some more input characters.</li>
     * <li>{@link CoderResult#OVERFLOW CoderResult.OVERFLOW} indicates that the
     * output buffer has been filled, while there are still some characters
     * remaining in the input buffer. This method should be invoked again with a
     * non-full output buffer </li>
     * <li>A {@link CoderResult#unmappableForLength(int) unmappable character}
     * result indicates that some unmappable character error was encountered,
     * and the erroneous characters start at the input buffer's position and
     * their number can be got by result's {@link CoderResult#length() length}.
     * This kind of result can be returned only on
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}. </li>
     * </ul>
     * </p>
     * <p>
     * The <code>endOfInput</code> parameter indicates that if the invoker can
     * provider further input. This parameter is true if and only if the
     * characters in current input buffer are all inputs for this encoding
     * operation. Note that it is common and won't cause error that the invoker
     * sets false and then finds no more input available; while it may cause
     * error that the invoker always sets true in several consecutive
     * invocations so that any remaining input will be treated as malformed
     * input.
     * </p>
     * <p>
     * This method invokes
     * {@link #encodeLoop(CharBuffer, ByteBuffer) encodeLoop} method to
     * implement basic encode logic for specific charset.
     * </p>
     * 
     * @param in
     *            the input buffer
     * @param out
     *            the output buffer
     * @param endOfInput
     *            true if all the input characters have been provided
     * @return a <code>CoderResult</code> instance indicating the result
     * @throws IllegalStateException
     *             if the encoding operation has already started or no more
     *             input needed in this encoding progress.
     * @throws CoderMalfunctionError
     *             If the {@link #encodeLoop(CharBuffer, ByteBuffer) encodeLoop}
     *             method threw an <code>BufferUnderflowException</code> or
     *             <code>BufferUnderflowException</code>
     */
    public final CoderResult encode(CharBuffer in, ByteBuffer out,
            boolean endOfInput) {
        if ((status == FLUSH) || (!endOfInput && status == END)) {
            throw new IllegalStateException();
        }

        CoderResult result;
        while (true) {
            try {
                result = encodeLoop(in, out);
            } catch (BufferOverflowException e) {
                throw new CoderMalfunctionError(e);
            } catch (BufferUnderflowException e) {
                throw new CoderMalfunctionError(e);
            }
            if (result.isUnderflow()) {
                int remaining = in.remaining();
                status = endOfInput ? END : ONGOING;
                if (endOfInput && remaining > 0) {
                    result = CoderResult.malformedForLength(remaining);
                } else {
                    return result;
                }
            }
            if (result.isOverflow()) {
                status = endOfInput ? END : ONGOING;
                return result;
            }
            CodingErrorAction action = malformAction;
            if (result.isUnmappable()) {
                action = unmapAction;
            }
            // If the action is IGNORE or REPLACE, we should continue
            // encoding.
            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replace.length) {
                    return CoderResult.OVERFLOW;
                }
                out.put(replace);
            } else {
                if (action != CodingErrorAction.IGNORE) {
                    return result;
                }
            }
            in.position(in.position() + result.length());
        }
    }

    /**
     * Encode characters into bytes. This method is called by
     * {@link #encode(CharBuffer, ByteBuffer, boolean) encode}.
     * 
     * This method will implement the essential encoding operation, and it won't
     * stop encoding until either all the input characters are read, the output
     * buffer is filled, or some exception encountered. And then it will return
     * a <code>CoderResult</code> object indicating the result of current
     * encoding operation. The rules to construct the <code>CoderResult</code>
     * is same as the {@link #encode(CharBuffer, ByteBuffer, boolean) encode}.
     * When exception encountered in the encoding operation, most implementation
     * of this method will return a relevant result object to
     * {@link #encode(CharBuffer, ByteBuffer, boolean) encode} method, and some
     * performance optimized implementation may handle the exception and
     * implement the error action itself.
     * 
     * The buffers are scanned from their current positions, and their positions
     * will be modified accordingly, while their marks and limits will be
     * intact. At most {@link CharBuffer#remaining() in.remaining()} characters
     * will be read, and {@link ByteBuffer#remaining() out.remaining()} bytes
     * will be written.
     * 
     * Note that some implementation may pre-scan the input buffer and return
     * <code>CoderResult.UNDERFLOW</code> until it receives sufficient input.
     * 
     * @param in
     *            the input buffer
     * @param out
     *            the output buffer
     * @return a <code>CoderResult</code> instance indicating the result
     */
    protected abstract CoderResult encodeLoop(CharBuffer in, ByteBuffer out);

    /**
     * Flush this encoder.
     * 
     * This method will call {@link #implFlush(ByteBuffer) implFlush}. Some
     * encoders may need to write some bytes to the output buffer when they have
     * read all input characters, subclasses can overridden
     * {@link #implFlush(ByteBuffer) implFlush} to perform writing action.
     * 
     * The maximum number of written bytes won't larger than
     * {@link ByteBuffer#remaining() out.remaining()}. If some encoder want to
     * write more bytes than output buffer's remaining spaces, then
     * <code>CoderResult.OVERFLOW</code> will be returned, and this method
     * must be called again with a byte buffer has more spaces. Otherwise this
     * method will return <code>CoderResult.UNDERFLOW</code>, which means one
     * encoding process has been completed successfully.
     * 
     * During the flush, the output buffer's position will be changed
     * accordingly, while its mark and limit will be intact.
     * 
     * @param out
     *            the given output buffer
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>
     * @throws IllegalStateException
     *             if this encoder hasn't read all input characters during one
     *             encoding process, which means neither after calling
     *             {@link #encode(CharBuffer) encode(CharBuffer)} nor after
     *             calling {@link #encode(CharBuffer, ByteBuffer, boolean) 
     *             encode(CharBuffer, ByteBuffer, boolean)} with true value for
     *             the last boolean parameter
     */
    public final CoderResult flush(ByteBuffer out) {
        if (status != END && status != INIT) {
            throw new IllegalStateException();
        }
        CoderResult result = implFlush(out);
        if (result == CoderResult.UNDERFLOW) {
            status = FLUSH;
        }
        return result;
    }

    /**
     * Flush this encoder. Default implementation does nothing and always return
     * <code>CoderResult.UNDERFLOW</code>, and this method can be overridden
     * if needed.
     * 
     * @param out
     *            the output buffer
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>
     */
    protected CoderResult implFlush(ByteBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * Notify that this encoder's <code>CodingErrorAction</code> specified for
     * malformed input error has been changed. Default implementation does
     * nothing, and this method can be overridden if needed.
     * 
     * @param newAction
     *            The new action
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) {
        // default implementation is empty
    }

    /**
     * Notify that this encoder's <code>CodingErrorAction</code> specified for
     * unmappable character error has been changed. Default implementation does
     * nothing, and this method can be overridden if needed.
     * 
     * @param newAction
     *            The new action
     */
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        // default implementation is empty
    }

    /**
     * Notify that this encoder's replacement has been changed. Default
     * implementation does nothing, and this method can be overridden if needed.
     * 
     * @param newReplacement
     *            the new replacement string
     */
    protected void implReplaceWith(byte[] newReplacement) {
        // default implementation is empty
    }

    /**
     * Reset this encoder's charset related state. Default implementation does
     * nothing, and this method can be overridden if needed.
     */
    protected void implReset() {
        // default implementation is empty
    }

    /**
     * Check if the given argument is legal as this encoder's replacement byte
     * array.
     * 
     * The given byte array is legal if and only if it can be decode into
     * sixteen bits Unicode characters.
     * 
     * This method can be overridden for performance improvement.
     * 
     * @param repl
     *            the given byte array to be checked
     * @return true if the the given argument is legal as this encoder's
     *         replacement byte array.
     */
    public boolean isLegalReplacement(byte[] repl) {
        if (decoder == null) {
            decoder = cs.newDecoder();
        }

        CodingErrorAction malform = decoder.malformedInputAction();
        CodingErrorAction unmap = decoder.unmappableCharacterAction();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer in = ByteBuffer.wrap(repl);
        CharBuffer out = CharBuffer.allocate((int) (repl.length * decoder
                .maxCharsPerByte()));
        CoderResult result = decoder.decode(in, out, true);
        decoder.onMalformedInput(malform);
        decoder.onUnmappableCharacter(unmap);
        return !result.isError();
    }

    /**
     * Gets this encoder's <code>CodingErrorAction</code> when malformed input
     * occurred during encoding process.
     * 
     * @return this encoder's <code>CodingErrorAction</code> when malformed
     *         input occurred during encoding process.
     */
    public CodingErrorAction malformedInputAction() {
        return malformAction;
    }

    /**
     * Get the maximum number of bytes which can be created by this encoder for
     * one input character, must be positive
     * 
     * @return the maximum number of bytes which can be created by this encoder
     *         for one input character, must be positive
     */
    public final float maxBytesPerChar() {
        return maxBytes;
    }

    /**
     * Set this encoder's action on malformed input error.
     * 
     * This method will call the
     * {@link #implOnMalformedInput(CodingErrorAction) implOnMalformedInput}
     * method with the given new action as argument.
     * 
     * @param newAction
     *            the new action on malformed input error
     * @return this encoder
     * @throws IllegalArgumentException
     *             if the given newAction is null
     */
    public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
        if (null == newAction) {
            // niochar.0C=Action on malformed input error cannot be null\!
            throw new IllegalArgumentException(Messages.getString("niochar.0C")); //$NON-NLS-1$
        }
        malformAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * Set this encoder's action on unmappable character error.
     * 
     * This method will call the
     * {@link #implOnUnmappableCharacter(CodingErrorAction) implOnUnmappableCharacter}
     * method with the given new action as argument.
     * 
     * @param newAction
     *            the new action on unmappable character error
     * @return this encoder
     * @throws IllegalArgumentException
     *             if the given newAction is null
     */
    public final CharsetEncoder onUnmappableCharacter(
            CodingErrorAction newAction) {
        if (null == newAction) {
            // niochar.0D=Action on unmappable character error cannot be null\!
            throw new IllegalArgumentException(Messages.getString("niochar.0D")); //$NON-NLS-1$
        }
        unmapAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    /**
     * Get the replacement byte array, which is never null or empty, and it is
     * legal
     * 
     * @return the replacement byte array, cannot be null or empty, and it is
     *         legal
     */
    public final byte[] replacement() {
        return replace;
    }

    /**
     * Set new replacement value.
     * 
     * This method first checks the given replacement's validity, then changes
     * the replacement value, and at last calls
     * {@link #implReplaceWith(byte[]) implReplaceWith} method with the given
     * new replacement as argument.
     * 
     * @param replacement
     *            the replacement byte array, cannot be null or empty, its
     *            length cannot larger than <code>maxBytesPerChar</code>, and
     *            must be legal replacement, which can be justified by
     *            <code>isLegalReplacement(byte[] repl)</code>
     * @return this encoder
     * @throws IllegalArgumentException
     *             if the given replacement cannot satisfy the requirement
     *             mentioned above
     */
    public final CharsetEncoder replaceWith(byte[] replacement) {
        if (null == replacement || 0 == replacement.length
                || maxBytes < replacement.length
                || !isLegalReplacement(replacement)) {
            // niochar.0E=Replacement is illegal
            throw new IllegalArgumentException(Messages.getString("niochar.0E")); //$NON-NLS-1$
        }
        replace = replacement;
        implReplaceWith(replacement);
        return this;
    }

    /**
     * Reset this encoder. This method will reset internal status, and then call
     * <code>implReset()</code> to reset any status related to specific
     * charset.
     * 
     * @return this encoder
     */
    public final CharsetEncoder reset() {
        status = INIT;
        implReset();
        return this;
    }

    /**
     * Gets this encoder's <code>CodingErrorAction</code> when unmappable
     * character occurred during encoding process.
     * 
     * @return this encoder's <code>CodingErrorAction</code> when unmappable
     *         character occurred during encoding process.
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmapAction;
    }
}

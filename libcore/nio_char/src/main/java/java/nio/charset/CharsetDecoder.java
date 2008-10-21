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
 * An converter that can convert bytes sequence in some charset to 16-bit
 * Unicode character sequence.
 * <p>
 * The input byte sequence is wrapped by {@link java.nio.ByteBuffer ByteBuffer}
 * and the output character sequence is {@link java.nio.CharBuffer CharBuffer}.
 * A decoder instance should be used in following sequence, which is referred to
 * as a decoding operation:
 * <ol>
 * <li>Invoking the {@link #reset() reset} method to reset the decoder if the
 * decoder has been used;</li>
 * <li>Invoking the {@link #decode(ByteBuffer, CharBuffer, boolean) decode}
 * method until the additional input is not needed, the <code>endOfInput</code>
 * parameter must be set to false, the input buffer must be filled and the
 * output buffer must be flushed between invocations;</li>
 * <li>Invoking the {@link #decode(ByteBuffer, CharBuffer, boolean) decode}
 * method last time, and the the <code>endOfInput</code> parameter must be set
 * to true</li>
 * <li>Invoking the {@link #flush(CharBuffer) flush} method to flush the
 * output.</li>
 * </ol>
 * </p>
 * <p>
 * The {@link #decode(ByteBuffer, CharBuffer, boolean) decode} method will
 * convert as many bytes as possible, and the process won't stop except the
 * input bytes has been run out of, the output buffer has been filled or some
 * error has happened. A {@link CoderResult CoderResult} instance will be
 * returned to indicate the stop reason, and the invoker can identify the result
 * and choose further action, which can include filling the input buffer,
 * flushing the output buffer, recovering from error and trying again.
 * </p>
 * <p>
 * There are two common decoding errors. One is named as malformed and it is
 * returned when the input byte sequence is illegal for current specific
 * charset, the other is named as unmappable character and it is returned when a
 * problem occurs mapping a legal input byte sequence to its Unicode character
 * equivalent.
 * </p>
 * <p>
 * The two errors can be handled in three ways, the default one is to report the
 * error to the invoker by a {@link CoderResult CoderResult} instance, and the
 * alternatives are to ignore it or to replace the erroneous input with the
 * replacement string. The replacement string is "\uFFFD" by default and can be
 * changed by invoking {@link #replaceWith(String) replaceWith} method. The
 * invoker of this decoder can choose one way by specifying a
 * {@link CodingErrorAction CodingErrorAction} instance for each error type via
 * {@link #onMalformedInput(CodingErrorAction) onMalformedInput} method and
 * {@link #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter}
 * method.
 * </p>
 * <p>
 * This class is abstract class and encapsulate many common operations of
 * decoding process for all charsets. Decoder for specific charset should extend
 * this class and need only implement
 * {@link #decodeLoop(ByteBuffer, CharBuffer) decodeLoop} method for basic
 * decoding loop. If a subclass maintains internal state, it should override the
 * {@link #implFlush(CharBuffer) implFlush} method and
 * {@link #implReset() implReset} method in addition.
 * </p>
 * <p>
 * This class is not thread-safe.
 * </p>
 * 
 * @see java.nio.charset.Charset
 * @see java.nio.charset.CharsetEncoder
 */
public abstract class CharsetDecoder {
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
    // average number of chars for one byte
    private float averChars;

    // maximum number of chars for one byte
    private float maxChars;

    // charset for this decoder
    private Charset cs;

    // specify the action if malformed input error encountered
    private CodingErrorAction malformAction;

    // specify the action if unmappable character error encountered
    private CodingErrorAction unmapAction;

    // the replacement string
    private String replace;

    // the current status
    private int status;

    /*
     * --------------------------------------- Constructor
     * ---------------------------------------
     */
    /**
     * Construct a new <code>CharsetDecoder</code> using given
     * <code>Charset</code>, average number and maximum number of characters
     * created by this decoder for one input byte, and the default replacement
     * string "\uFFFD".
     * 
     * @param charset
     *            this decoder's <code>Charset</code>, which create this
     *            decoder
     * @param averageCharsPerByte
     *            average number of characters created by this decoder for one
     *            input byte, must be positive
     * @param maxCharsPerByte
     *            maximum number of characters created by this decoder for one
     *            input byte, must be positive
     * @throws IllegalArgumentException
     *             if <code>averageCharsPerByte</code> or
     *             <code>maxCharsPerByte</code> is negative
     */
    protected CharsetDecoder(Charset charset, float averageCharsPerByte,
            float maxCharsPerByte) {
        if (averageCharsPerByte <= 0 || maxCharsPerByte <= 0) {
            // niochar.00=Characters number for one byte must be positive.
            throw new IllegalArgumentException(Messages.getString("niochar.00")); //$NON-NLS-1$
        }
        if (averageCharsPerByte > maxCharsPerByte) {
            // niochar.01=averageCharsPerByte is greater than maxCharsPerByte
            throw new IllegalArgumentException(Messages.getString("niochar.01")); //$NON-NLS-1$
        }
        averChars = averageCharsPerByte;
        maxChars = maxCharsPerByte;
        cs = charset;
        status = INIT;
        malformAction = CodingErrorAction.REPORT;
        unmapAction = CodingErrorAction.REPORT;
        replace = "\ufffd"; //$NON-NLS-1$
    }

    /*
     * --------------------------------------- Methods
     * ---------------------------------------
     */
    /**
     * get the average number of characters created by this decoder for single
     * input byte
     * 
     * @return the average number of characters created by this decoder for
     *         single input byte
     */
    public final float averageCharsPerByte() {
        return averChars;
    }

    /**
     * Get the <code>Charset</code> which creates this decoder.
     * 
     * @return the <code>Charset</code> which creates this decoder
     */
    public final Charset charset() {
        return cs;
    }

    /**
     * This is a facade method for decoding operation.
     * <p>
     * This method decodes the remaining byte sequence of the given byte buffer
     * into a new character buffer. This method performs a complete decoding
     * operation, resets at first, then decodes, and flushes at last.
     * </p>
     * <p>
     * This method should not be invoked if another decode operation is ongoing.
     * </p>
     * 
     * @param in
     *            the input buffer
     * @return a new <code>CharBuffer</code> containing the the characters
     *         produced by this decoding operation. The buffer's limit will be
     *         the position of last character in buffer, and the position will
     *         be zero
     * @throws IllegalStateException
     *             if another decoding operation is ongoing
     * @throws MalformedInputException
     *             if illegal input byte sequence for this charset encountered,
     *             and the action for malformed error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}
     * @throws UnmappableCharacterException
     *             if legal but unmappable input byte sequence for this charset
     *             encountered, and the action for unmappable character error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}.
     *             Unmappable means the byte sequence at the input buffer's
     *             current position cannot be mapped to a Unicode character
     *             sequence.
     * @throws CharacterCodingException
     *             if other exception happened during the decode operation
     */
    public final CharBuffer decode(ByteBuffer in)
            throws CharacterCodingException {
        reset();
        int length = (int) (in.remaining() * averChars);
        CharBuffer output = CharBuffer.allocate(length);
        CoderResult result = null;
        while (true) {
            result = decode(in, output, false);
            checkCoderResult(result);
            if (result.isUnderflow()) {
                break;
            } else if (result.isOverflow()) {
                output = allocateMore(output);
            }
        }
        result = decode(in, output, true);
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

    /*
     * original output is full and doesn't have remaining. allocate more space
     * to new CharBuffer and return it, the contents in the given buffer will be
     * copied into the new buffer.
     */
    private CharBuffer allocateMore(CharBuffer output) {
        if (output.capacity() == 0) {
            return CharBuffer.allocate(1);
        }
        CharBuffer result = CharBuffer.allocate(output.capacity() * 2);
        output.flip();
        result.put(output);
        return result;
    }

    /**
     * Decodes bytes starting at the current position of the given input buffer,
     * and writes the equivalent character sequence into the given output buffer
     * from its current position.
     * <p>
     * The buffers' position will be changed with the reading and writing
     * operation, but their limits and marks will be kept intact.
     * </p>
     * <p>
     * A <code>CoderResult</code> instance will be returned according to
     * following rules:
     * <ul>
     * <li>{@link CoderResult#OVERFLOW CoderResult.OVERFLOW} indicates that
     * even though not all of the input has been processed, the buffer the
     * output is being written to has reached its capacity. In the event of this
     * code being returned this method should be called once more with an
     * <code>out</code> argument that has not already been filled.</li>
     * <li>{@link CoderResult#UNDERFLOW CoderResult.UNDERFLOW} indicates that
     * as many bytes as possible in the input buffer have been decoded. If there
     * is no further input and no remaining bytes in the input buffer then this
     * operation may be regarded as complete. Otherwise, this method should be
     * called once more with additional input.</li>
     * <li>A {@link CoderResult#malformedForLength(int) malformed input} result
     * indicates that some malformed input error encountered, and the erroneous
     * bytes start at the input buffer's position and their number can be got by
     * result's {@link CoderResult#length() length}. This kind of result can be
     * returned only if the malformed action is
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}. </li>
     * <li>A {@link CoderResult#unmappableForLength(int) unmappable character}
     * result indicates that some unmappable character error encountered, and
     * the erroneous bytes start at the input buffer's position and their number
     * can be got by result's {@link CoderResult#length() length}. This kind of
     * result can be returned only if the unmappable character action is
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}. </li>
     * </ul>
     * </p>
     * <p>
     * The <code>endOfInput</code> parameter indicates that if the invoker can
     * provider further input. This parameter is true if and only if the bytes
     * in current input buffer are all inputs for this decoding operation. Note
     * that it is common and won't cause error that the invoker sets false and
     * then finds no more input available; while it may cause error that the
     * invoker always sets true in several consecutive invocations so that any
     * remaining input will be treated as malformed input.
     * </p>
     * <p>
     * This method invokes
     * {@link #decodeLoop(ByteBuffer, CharBuffer) decodeLoop} method to
     * implement basic decode logic for specific charset.
     * </p>
     * 
     * @param in
     *            the input buffer
     * @param out
     *            the output buffer
     * @param endOfInput
     *            true if all the input characters have been provided
     * @return a <code>CoderResult</code> instance which indicates the reason
     *         of termination
     * @throws IllegalStateException
     *             if decoding has started or no more input is needed in this
     *             decoding progress.
     * @throws CoderMalfunctionError
     *             if the {@link #decodeLoop(ByteBuffer, CharBuffer) decodeLoop}
     *             method threw an <code>BufferUnderflowException</code> or
     *             <code>BufferOverflowException</code>
     */
    public final CoderResult decode(ByteBuffer in, CharBuffer out,
            boolean endOfInput) {
        /*
         * status check
         */
        if ((status == FLUSH) || (!endOfInput && status == END)) {
            throw new IllegalStateException();
        }

        CoderResult result = null;

        // begin to decode
        while (true) {
            CodingErrorAction action = null;
            try {
                result = decodeLoop(in, out);
            } catch (BufferOverflowException ex) {
                // unexpected exception
                throw new CoderMalfunctionError(ex);
            } catch (BufferUnderflowException ex) {
                // unexpected exception
                throw new CoderMalfunctionError(ex);
            }

            /*
             * result handling
             */
            if (result.isUnderflow()) {
                int remaining = in.remaining();
                status = endOfInput ? END : ONGOING;
                if (endOfInput && remaining > 0) {
                    result = CoderResult.malformedForLength(remaining);
                    in.position(in.position() + result.length());
                } else {
                    return result;
                }
            }
            if (result.isOverflow()) {
                return result;
            }
            // set coding error handle action
            action = malformAction;
            if (result.isUnmappable()) {
                action = unmapAction;
            }
            // If the action is IGNORE or REPLACE, we should continue decoding.
            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replace.length()) {
                    return CoderResult.OVERFLOW;
                }
                out.put(replace);
            } else {
                if (action != CodingErrorAction.IGNORE)
                    return result;
            }
            if (!result.isMalformed()) {
                in.position(in.position() + result.length());
            }
        }
    }

    /**
     * Decode bytes into characters. This method is called by
     * {@link #decode(ByteBuffer, CharBuffer, boolean) decode} method.
     * 
     * This method will implement the essential decoding operation, and it won't
     * stop decoding until either all the input bytes are read, the output
     * buffer is filled, or some exception encountered. And then it will return
     * a <code>CoderResult</code> object indicating the result of current
     * decoding operation. The rules to construct the <code>CoderResult</code>
     * is same as the {@link #decode(ByteBuffer, CharBuffer, boolean) decode}.
     * When exception encountered in the decoding operation, most implementation
     * of this method will return a relevant result object to
     * {@link #decode(ByteBuffer, CharBuffer, boolean) decode} method, and some
     * performance optimized implementation may handle the exception and
     * implement the error action itself.
     * 
     * The buffers are scanned from their current positions, and their positions
     * will be modified accordingly, while their marks and limits will be
     * intact. At most {@link ByteBuffer#remaining() in.remaining()} characters
     * will be read, and {@link CharBuffer#remaining() out.remaining()} bytes
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
    protected abstract CoderResult decodeLoop(ByteBuffer in, CharBuffer out);

    /**
     * Get the charset detected by this decoder, this method is optional.
     * <p>
     * If implementing an auto-detecting charset, then this decoder returns the
     * detected charset from this method when it is available. The returned
     * charset will be the same for the rest of the decode operation.
     * </p>
     * <p>
     * If insufficient bytes have been read to determine the charset,
     * <code>IllegalStateException</code> will be thrown.
     * </p>
     * <p>
     * The default implementation always throws
     * <code>UnsupportedOperationException</code>, so it should be overridden
     * by subclass if needed.
     * </p>
     * 
     * @return the charset detected by this decoder, or null if it is not yet
     *         determined
     * @throws UnsupportedOperationException
     *             if this decoder does not implement an auto-detecting charset
     * @throws IllegalStateException
     *             if insufficient bytes have been read to determine the charset
     */
    public Charset detectedCharset() {
        throw new UnsupportedOperationException();
    }

    /**
     * Flush this decoder.
     * 
     * This method will call {@link #implFlush(CharBuffer) implFlush}. Some
     * decoders may need to write some characters to the output buffer when they
     * have read all input bytes, subclasses can overridden
     * {@link #implFlush(CharBuffer) implFlush} to perform writing action.
     * 
     * The maximum number of written bytes won't larger than
     * {@link CharBuffer#remaining() out.remaining()}. If some decoder want to
     * write more bytes than output buffer's remaining spaces, then
     * <code>CoderResult.OVERFLOW</code> will be returned, and this method
     * must be called again with a character buffer that has more spaces.
     * Otherwise this method will return <code>CoderResult.UNDERFLOW</code>,
     * which means one decoding process has been completed successfully.
     * 
     * During the flush, the output buffer's position will be changed
     * accordingly, while its mark and limit will be intact.
     * 
     * @param out
     *            the given output buffer
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>
     * @throws IllegalStateException
     *             if this decoder hasn't read all input bytes during one
     *             decoding process, which means neither after calling
     *             {@link #decode(ByteBuffer) decode(ByteBuffer)} nor after
     *             calling {@link #decode(ByteBuffer, CharBuffer, boolean)
     *             decode(ByteBuffer, CharBuffer, boolean)} with true value for
     *             the last boolean parameter
     */
    public final CoderResult flush(CharBuffer out) {
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
     * Flush this decoder. Default implementation does nothing and always return
     * <code>CoderResult.UNDERFLOW</code>, and this method can be overridden
     * if needed.
     * 
     * @param out
     *            the output buffer
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>
     */
    protected CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * Notify that this decoder's <code>CodingErrorAction</code> specified for
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
     * Notify that this decoder's <code>CodingErrorAction</code> specified for
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
     * Notify that this decoder's replacement has been changed. Default
     * implementation does nothing, and this method can be overridden if needed.
     * 
     * @param newReplacement
     *            the new replacement string
     */
    protected void implReplaceWith(String newReplacement) {
        // default implementation is empty
    }

    /**
     * Reset this decoder's charset related state. Default implementation does
     * nothing, and this method can be overridden if needed.
     */
    protected void implReset() {
        // default implementation is empty
    }

    /**
     * Get if this decoder implements an auto-detecting charset.
     * 
     * @return <code>true</code> if this decoder implements an auto-detecting
     *         charset
     */
    public boolean isAutoDetecting() {
        return false;
    }

    /**
     * Get if this decoder has detected a charset, this method is optional.
     * <p>
     * If this decoder implements an auto-detecting charset, then this method
     * may start to return true during decoding operation to indicate that a
     * charset has been detected in the input bytes and that the charset can be
     * retrieved by invoking {@link #detectedCharset() detectedCharset} method.
     * </p>
     * <p>
     * Note that a decoder that implements an auto-detecting charset may still
     * succeed in decoding a portion of the given input even when it is unable
     * to detect the charset. For this reason users should be aware that a
     * <code>false</code> return value does not indicate that no decoding took
     * place.
     * </p>
     * <p>
     * The default implementation always throws an
     * <code>UnsupportedOperationException</code>; it should be overridden by
     * subclass if needed.
     * </p>
     * 
     * @return <code>true</code> this decoder has detected a charset
     * @throws UnsupportedOperationException
     *             if this decoder doesn't implement an auto-detecting charset
     */
    public boolean isCharsetDetected() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets this decoder's <code>CodingErrorAction</code> when malformed input
     * occurred during decoding process.
     * 
     * @return this decoder's <code>CodingErrorAction</code> when malformed
     *         input occurred during decoding process.
     */
    public CodingErrorAction malformedInputAction() {
        return malformAction;
    }

    /**
     * Get the maximum number of characters which can be created by this decoder
     * for one input byte, must be positive
     * 
     * @return the maximum number of characters which can be created by this
     *         decoder for one input byte, must be positive
     */
    public final float maxCharsPerByte() {
        return maxChars;
    }

    /**
     * Set this decoder's action on malformed input error.
     * 
     * This method will call the
     * {@link #implOnMalformedInput(CodingErrorAction) implOnMalformedInput}
     * method with the given new action as argument.
     * 
     * @param newAction
     *            the new action on malformed input error
     * @return this decoder
     * @throws IllegalArgumentException
     *             if the given newAction is null
     */
    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException();
        }
        malformAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * Set this decoder's action on unmappable character error.
     * 
     * This method will call the
     * {@link #implOnUnmappableCharacter(CodingErrorAction) implOnUnmappableCharacter}
     * method with the given new action as argument.
     * 
     * @param newAction
     *            the new action on unmappable character error
     * @return this decoder
     * @throws IllegalArgumentException
     *             if the given newAction is null
     */
    public final CharsetDecoder onUnmappableCharacter(
            CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException();
        }
        unmapAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    /**
     * Get the replacement string, which is never null or empty
     * 
     * @return the replacement string, cannot be null or empty
     */
    public final String replacement() {
        return replace;
    }

    /**
     * Set new replacement value.
     * 
     * This method first checks the given replacement's validity, then changes
     * the replacement value, and at last calls
     * {@link #implReplaceWith(String) implReplaceWith} method with the given
     * new replacement as argument.
     * 
     * @param newReplacement
     *            the replacement string, cannot be null or empty
     * @return this decoder
     * @throws IllegalArgumentException
     *             if the given replacement cannot satisfy the requirement
     *             mentioned above
     */
    public final CharsetDecoder replaceWith(String newReplacement) {
        if (null == newReplacement || newReplacement.length() == 0) {
            // niochar.06=Replacement string cannot be null or empty.
            throw new IllegalArgumentException(Messages.getString("niochar.06")); //$NON-NLS-1$
        }
        if (newReplacement.length() > maxChars) {
            // niochar.07=Replacement string's length cannot be larger than max
            // characters per byte.
            throw new IllegalArgumentException(Messages.getString("niochar.07")); //$NON-NLS-1$
        }
        replace = newReplacement;
        implReplaceWith(newReplacement);
        return this;
    }

    /**
     * Reset this decoder. This method will reset internal status, and then call
     * <code>implReset()</code> to reset any status related to specific
     * charset.
     * 
     * @return this decoder
     */
    public final CharsetDecoder reset() {
        status = INIT;
        implReset();
        return this;
    }

    /**
     * Gets this decoder's <code>CodingErrorAction</code> when unmappable
     * character occurred during decoding process.
     * 
     * @return this decoder's <code>CodingErrorAction</code> when unmappable
     *         character occurred during decoding process.
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmapAction;
    }
}

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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.security.AccessController;

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * OutputStreamWriter is a class for turning a character output stream into a
 * byte output stream. The conversion of Unicode characters to their byte
 * equivalents is determined by the converter used. By default, the encoding is
 * ISO8859_1 (ISO-Latin-1) but can be changed by calling the constructor which
 * takes an encoding.
 * 
 * @see InputStreamReader
 */

public class OutputStreamWriter extends Writer {

    private OutputStream out;

    private CharsetEncoder encoder;

    private ByteBuffer bytes = ByteBuffer.allocate(8192);

    /**
     * Constructs a new OutputStreamWriter using <code>out</code> as the
     * OutputStream to write converted characters to. The default character
     * encoding is used (see class description).
     * 
     * @param out
     *            the non-null OutputStream to write converted bytes to.
     */
    public OutputStreamWriter(OutputStream out) {
        super(out);
        this.out = out;
        String encoding = AccessController
                .doPrivileged(new PriviAction<String>(
                        "file.encoding", "ISO8859_1")); //$NON-NLS-1$ //$NON-NLS-2$
        encoder = Charset.forName(encoding).newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /**
     * Constructs a new OutputStreamWriter using <code>out</code> as the
     * OutputStream to write converted characters to and <code>enc</code> as
     * the character encoding. If the encoding cannot be found, an
     * UnsupportedEncodingException error is thrown.
     * 
     * @param out
     *            the non-null OutputStream to write converted bytes to.
     * @param enc
     *            the non-null String describing the desired character encoding.
     * 
     * @throws UnsupportedEncodingException
     *             if the encoding cannot be found.
     */
    public OutputStreamWriter(OutputStream out, final String enc)
            throws UnsupportedEncodingException {
        super(out);
        if (enc == null) {
            throw new NullPointerException();
        }
        this.out = out;
        try {
            encoder = Charset.forName(enc).newEncoder();
        } catch (Exception e) {
            throw new UnsupportedEncodingException(enc);
        }
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /**
     * Constructs a new OutputStreamWriter using <code>out</code> as the
     * OutputStream to write converted characters to and <code>cs</code> as
     * the character encoding.
     * 
     * 
     * @param out
     *            the non-null OutputStream to write converted bytes to.
     * @param cs
     *            the non-null Charset which specify the character encoding.
     */
    public OutputStreamWriter(OutputStream out, Charset cs) {
        super(out);
        this.out = out;
        encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /**
     * Constructs a new OutputStreamWriter using <code>out</code> as the
     * OutputStream to write converted characters to and <code>enc</code> as
     * the character encoding.
     * 
     * @param out
     *            the non-null OutputStream to write converted bytes to.
     * @param enc
     *            the non-null CharsetEncoder which used to character encoding.
     */
    public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out);
        enc.charset();
        this.out = out;
        encoder = enc;
    }

    /**
     * Close this OutputStreamWriter. This implementation first flushes the
     * buffer and the target OutputStream. The OutputStream is then closed and
     * the resources for the buffer and converter are freed.
     * <p>
     * Only the first invocation of this method has any effect. Subsequent calls
     * do no work.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this
     *             OutputStreamWriter.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (encoder != null) {
                encoder.flush(bytes);
                flush();
                out.flush();
                out.close();
                encoder = null;
                bytes = null;
            }
        }
    }

    /**
     * Flush this OutputStreamWriter. This implementation ensures all buffered
     * bytes are written to the target OutputStream. After writing the bytes,
     * the target OutputStream is then flushed.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this
     *             OutputStreamWriter.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            checkStatus();
            int position;
            if ((position = bytes.position()) > 0) {
                bytes.flip();
                out.write(bytes.array(), 0, position);
                bytes.clear();
            }
            out.flush();
        }
    }

    private void checkStatus() throws IOException {
        if (encoder == null) {
            // K005d=Writer is closed.
            throw new IOException(Msg.getString("K005d")); //$NON-NLS-1$
        }
    }

    /**
     * Answer the String which identifies the encoding used to convert
     * characters to bytes. The value <code>null</code> is returned if this
     * Writer has been closed.
     * 
     * @return the String describing the converter or null if this Writer is
     *         closed.
     */

    public String getEncoding() {
        if (encoder == null) {
            return null;
        }
        return InputStreamReader.HistoricalNamesUtil.getHistoricalName(encoder
                .charset().name());
    }

    /**
     * Writes <code>count</code> characters starting at <code>offset</code>
     * in <code>buf</code> to this Writer. The characters are immediately
     * converted to bytes by the character converter and stored in a local
     * buffer. If the buffer becomes full as a result of this write, this Writer
     * is flushed.
     * 
     * @param buf
     *            the non-null array containing characters to write.
     * @param offset
     *            offset in buf to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws IOException
     *             If this OutputStreamWriter has already been closed or some
     *             other IOException occurs.
     * @throws IndexOutOfBoundsException
     *             If offset or count is outside of bounds.
     */
    @Override
    public void write(char[] buf, int offset, int count) throws IOException {
        synchronized (lock) {
            checkStatus();
            if (offset < 0 || offset > buf.length - count || count < 0) {
                throw new IndexOutOfBoundsException();
            }
            CharBuffer chars = CharBuffer.wrap(buf, offset, count);
            convert(chars);
        }
    }

    private void convert(CharBuffer chars) throws IOException {
        CoderResult result = encoder.encode(chars, bytes, true);
        while (true) {
            if (result.isError()) {
                throw new IOException(result.toString());
            } else if (result.isOverflow()) {
                // flush the output buffer
                flush();
                result = encoder.encode(chars, bytes, true);
                continue;
            }
            break;
        }
    }

    /**
     * Writes out the character <code>oneChar</code> to this Writer. The
     * low-order 2 bytes are immediately converted to bytes by the character
     * converter and stored in a local buffer. If the buffer becomes full as a
     * result of this write, this Writer is flushed.
     * 
     * @param oneChar
     *            the character to write
     * 
     * @throws IOException
     *             If this OutputStreamWriter has already been closed or some
     *             other IOException occurs.
     */
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            checkStatus();
            CharBuffer chars = CharBuffer.wrap(new char[] { (char) oneChar });
            convert(chars);
        }
    }

    /**
     * Writes <code>count</code> characters starting at <code>offset</code>
     * in <code>str</code> to this Writer. The characters are immediately
     * converted to bytes by the character converter and stored in a local
     * buffer. If the buffer becomes full as a result of this write, this Writer
     * is flushed.
     * 
     * @param str
     *            the non-null String containing characters to write.
     * @param offset
     *            offset in str to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws IOException
     *             If this OutputStreamWriter has already been closed or some
     *             other IOException occurs.
     * @throws IndexOutOfBoundsException
     *             If count is negative
     * @throws StringIndexOutOfBoundsException
     *             If offset is negative or offset + count is outside of bounds
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            // avoid int overflow
            if (count < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (offset > str.length() - count || offset < 0) {
                throw new StringIndexOutOfBoundsException();
            }
            checkStatus();
            CharBuffer chars = CharBuffer.wrap(str, offset, count + offset);
            convert(chars);
        }
    }
}

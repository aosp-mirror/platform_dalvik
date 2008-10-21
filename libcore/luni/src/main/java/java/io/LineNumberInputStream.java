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
 * LineNumberInputStream is a filter class which counts the number of line
 * terminators from the data read from the target InputStream. A line delimiter
 * sequence is determined by '\r', '\n', or '\r\n'. When using <code>read</code>,
 * the sequence is always translated into '\n'.
 * 
 * @deprecated Use {@link LineNumberReader}
 */
@Deprecated
public class LineNumberInputStream extends FilterInputStream {

    private int lineNumber;

    private int markedLineNumber = -1;

    private int lastChar = -1;

    private int markedLastChar;

    /**
     * Constructs a new LineNumberInputStream on the InputStream <code>in</code>.
     * All reads are now filtered through this stream and line numbers will be
     * counted for all data read from this Stream.
     * 
     * @param in
     *            The non-null InputStream to count line numbers.
     */
    public LineNumberInputStream(InputStream in) {
        super(in);
    }

    /**
     * Returns a int representing the number of bytes that are available before
     * this LineNumberInputStream will block. This method returns the number of
     * bytes available in the target stream. Since the target input stream may
     * just be a sequence of <code>\r\n</code> characters and this filter only
     * returns <code>\n<code> then <code>available</code> can only
     * guarantee <code>target.available()/2</code> characters.
     *
     * @return int the number of bytes available before blocking.
     *
     * @throws IOException If an error occurs in this stream.
     */
    @Override
    public int available() throws IOException {
        return in.available() / 2 + (lastChar == -1 ? 0 : 1);
    }

    /**
     * Returns a int representing the current line number for this
     * LineNumberInputStream.
     * 
     * @return int the current line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Set a Mark position in this LineNumberInputStream. The parameter
     * <code>readLimit</code> indicates how many bytes can be read before a
     * mark is invalidated. Sending reset() will reposition the Stream back to
     * the marked position provided <code>readLimit</code> has not been
     * surpassed. The lineNumber count will also be reset to the last marked
     * lineNumber count.
     * <p>
     * This implementation sets a mark in the target stream.
     * 
     * @param readlimit
     *            The number of bytes to be able to read before invalidating the
     *            mark.
     */
    @Override
    public void mark(int readlimit) {
        in.mark(readlimit);
        markedLineNumber = lineNumber;
        markedLastChar = lastChar;
    }

    /**
     * Reads a single byte from this LineNumberInputStream and returns the
     * result as an int. The low-order byte is returned or -1 of the end of
     * stream was encountered. This implementation returns a byte from the
     * target stream. The line number count is incremented if a line terminator
     * is encountered. A line delimiter sequence is determined by '\r', '\n', or
     * '\r\n'. In this method, the sequence is always translated into '\n'.
     * 
     * @return int The byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    @Override
    public int read() throws IOException {
        int currentChar = lastChar;
        if (currentChar == -1) {
            currentChar = in.read();
        } else {
            lastChar = -1;
        }
        switch (currentChar) {
            case '\r':
                currentChar = '\n';
                lastChar = in.read();
                if (lastChar == '\n') {
                    lastChar = -1;
                }
                // fall through
            case '\n':
                lineNumber++;
        }
        return currentChar;
    }

    /**
     * Reads at most <code>length</code> bytes from this LineNumberInputStream
     * and stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered. This implementation
     * reads bytes from the target stream. The line number count is incremented
     * if a line terminator is encountered. A line delimiter sequence is
     * determined by '\r', '\n', or '\r\n'. In this method, the sequence is
     * always translated into '\n'.
     * 
     * @param buffer
     *            the non-null byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return The number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     * @throws NullPointerException
     *             If <code>buffer</code> is <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>offset</code> or <code>count</code> are out of
     *             bounds.
     */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || length < 0
                || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int i = 0; i < length; i++) {
            int currentChar;
            try {
                currentChar = read();
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            if (currentChar == -1) {
                return i == 0 ? -1 : i;
            }
            buffer[offset + i] = (byte) currentChar;
        }
        return length;
    }

    /**
     * Reset this LineNumberInputStream to the last marked location. If the
     * <code>readlimit</code> has been passed or no <code>mark</code> has
     * been set, throw IOException. This implementation resets the target
     * stream. It also resets the line count to what is was when this Stream was
     * marked.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    @Override
    public void reset() throws IOException {
        in.reset();
        lineNumber = markedLineNumber;
        lastChar = markedLastChar;
    }

    /**
     * Sets the lineNumber of this LineNumberInputStream to the specified
     * <code>lineNumber</code>. Note that this may have side effects on the
     * line number associated with the last marked position.
     * 
     * @param lineNumber
     *            the new lineNumber value.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Skips <code>count</code> number of bytes in this InputStream.
     * Subsequent <code>read()</code>'s will not return these bytes unless
     * <code>reset()</code> is used. This implementation skips
     * <code>count</code> number of bytes in the target stream and increments
     * the lineNumber count as bytes are skipped.
     * 
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    @Override
    public long skip(long count) throws IOException {
        if (count <= 0) {
            return 0;
        }
        for (int i = 0; i < count; i++) {
            int currentChar = read();
            if (currentChar == -1) {
                return i;
            }
        }
        return count;
    }
}

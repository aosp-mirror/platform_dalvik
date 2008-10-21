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

import org.apache.harmony.luni.util.Msg;

/**
 * ByteArrayOutputStream is a class whose underlying stream is represented by a
 * byte array. As bytes are written to this stream, the local byte array may be
 * expanded to hold more bytes.
 * 
 * @see ByteArrayInputStream
 */
public class ByteArrayOutputStream extends OutputStream {
    /**
     * The byte array containing the bytes written.
     */
    protected byte[] buf;

    /**
     * The number of bytes written.
     */
    protected int count;

    /**
     * Constructs a new ByteArrayOutputStream with a default size of 32 bytes.
     * If more than 32 bytes are written to this instance, the underlying byte
     * array will expand to accommodate.
     * 
     */
    public ByteArrayOutputStream() {
        super();
        buf = new byte[32];
    }

    /**
     * Constructs a new ByteArrayOutputStream with a default size of
     * <code>size</code> bytes. If more than <code>size</code> bytes are
     * written to this instance, the underlying byte array will expand to
     * accommodate.
     * 
     * @param size
     *            an non-negative integer representing the initial size for the
     *            underlying byte array.
     */
    public ByteArrayOutputStream(int size) {
        super();
        if (size >= 0) {
            buf = new byte[size];
        } else {
            throw new IllegalArgumentException(Msg.getString("K005e")); //$NON-NLS-1$
        }
    }

    /**
     * Close this ByteArrayOutputStream. This implementation releases System
     * resources used for this stream.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this OutputStream.
     */
    @Override
    public void close() throws IOException {
        /**
         * Although the spec claims "A closed stream cannot perform output
         * operations and cannot be reopened.", this implementation must do
         * nothing.
         */
        super.close();
    }

    private void expand(int i) {
        /* Can the buffer handle @i more bytes, if not expand it */
        if (count + i <= buf.length) {
            return;
        }

        byte[] newbuf = new byte[(count + i) * 2];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
    }

    /**
     * Reset this ByteArrayOutputStream to the beginning of the underlying byte
     * array. All subsequent writes will overwrite any bytes previously stored
     * in this stream.
     * 
     */
    public synchronized void reset() {
        count = 0;
    }

    /**
     * Returns the total number of bytes written to this stream thus far.
     * 
     * @return the number of bytes written to this Stream.
     */
    public int size() {
        return count;
    }

    /**
     * Answer the contents of this ByteArrayOutputStream as a byte array. Any
     * changes made to the receiver after returning will not be reflected in the
     * byte array returned to the caller.
     * 
     * @return this streams current contents as a byte array.
     */
    public synchronized byte[] toByteArray() {
        byte[] newArray = new byte[count];
        System.arraycopy(buf, 0, newArray, 0, count);
        return newArray;
    }

    /**
     * Answer the contents of this ByteArrayOutputStream as a String. Any
     * changes made to the receiver after returning will not be reflected in the
     * String returned to the caller.
     * 
     * @return this streams current contents as a String.
     */

    @Override
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Answer the contents of this ByteArrayOutputStream as a String. Each byte
     * <code>b</code> in this stream is converted to a character
     * <code>c</code> using the following function:
     * <code>c == (char)(((hibyte & 0xff) << 8) | (b & 0xff))</code>. This
     * method is deprecated and either {@link #toString()}, or {@link #toString(String)}
     * should be used.
     * 
     * @param hibyte
     *            the high byte of each resulting Unicode character
     * @return this streams current contents as a String with the high byte set
     *         to <code>hibyte</code>
     * 
     * @deprecated Use {@link #toString()}
     */
    @Deprecated
    public String toString(int hibyte) {
        char[] newBuf = new char[size()];
        for (int i = 0; i < newBuf.length; i++) {
            newBuf[i] = (char) (((hibyte & 0xff) << 8) | (buf[i] & 0xff));
        }
        return new String(newBuf);
    }

    /**
     * Answer the contents of this ByteArrayOutputStream as a String converted
     * using the encoding declared in <code>enc</code>.
     * 
     * @param enc
     *            A String representing the encoding to use when translating
     *            this stream to a String.
     * @return this streams current contents as a String.
     * 
     * @throws UnsupportedEncodingException
     *             If declared encoding is not supported
     */
    public String toString(String enc) throws UnsupportedEncodingException {
        return new String(buf, 0, count, enc);
    }

    /**
     * Writes <code>count</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at offset <code>index</code> to the
     * ByteArrayOutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param len
     *            number of bytes in buffer to write
     * 
     * @throws NullPointerException
     *             If buffer is null.
     * @throws IndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    @Override
    public synchronized void write(byte[] buffer, int offset, int len) {
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || len < 0
                || len > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        if (len == 0) {
            return;
        }

        /* Expand if necessary */
        expand(len);
        System.arraycopy(buffer, offset, buf, this.count, len);
        this.count += len;
    }

    /**
     * Writes the specified byte <code>oneByte</code> to the OutputStream.
     * Only the low order byte of <code>oneByte</code> is written.
     * 
     * @param oneByte
     *            the byte to be written
     */
    @Override
    public synchronized void write(int oneByte) {
        if (count == buf.length) {
            expand(1);
        }
        buf[count++] = (byte) oneByte;
    }

    /**
     * Take the contents of this stream and write it to the output stream
     * <code>out</code>.
     * 
     * @param out
     *            An OutputStream on which to write the contents of this stream.
     * 
     * @throws IOException
     *             If an error occurs when writing to output stream
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }
}

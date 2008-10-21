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
 * ByteArrayInputStream is used for streaming over a byte array.
 * 
 * @see ByteArrayOutputStream
 */
public class ByteArrayInputStream extends InputStream {
    /**
     * The <code>byte</code> array containing the bytes to stream over.
     */
    protected byte[] buf;

    /**
     * The current position within the byte array.
     */
    protected int pos;

    /**
     * The current mark position. Initially set to 0 or the <code>offset</code>
     * parameter within the constructor.
     */
    protected int mark;

    /**
     * The total number of bytes initially available in the byte array
     * <code>buf</code>.
     */
    protected int count;

    /**
     * Constructs a new ByteArrayInputStream on the byte array <code>buf</code>.
     * 
     * @param buf
     *            the byte array to stream over
     */
    public ByteArrayInputStream(byte buf[]) {
        this.mark = 0;
        this.buf = buf;
        this.count = buf.length;
    }

    /**
     * Constructs a new ByteArrayInputStream on the byte array <code>buf</code>
     * with the position set to <code>offset</code> and the number of bytes
     * available set to <code>offset</code> + <code>length</code>.
     * 
     * @param buf
     *            the byte array to stream over
     * @param offset
     *            the offset in <code>buf</code> to start streaming at
     * @param length
     *            the number of bytes available to stream over.
     */
    public ByteArrayInputStream(byte buf[], int offset, int length) {
        this.buf = buf;
        pos = offset;
        mark = offset;
        count = offset + length > buf.length ? buf.length : offset + length;
    }

    /**
     * Returns a int representing then number of bytes that are available before
     * this ByteArrayInputStream will block. This method returns the number of
     * bytes yet to be read from the underlying byte array.
     * 
     * @return the number of bytes available before blocking.
     */
    @Override
    public synchronized int available() {
        return count - pos;
    }

    /**
     * Close the ByteArrayInputStream. This implementation frees up resources
     * associated with this stream.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this InputStream.
     */
    @Override
    public void close() throws IOException {
        // Do nothing on close, this matches JDK behaviour.
    }

    /**
     * Set a Mark position in this ByteArrayInputStream. The parameter
     * <code>readLimit</code> is ignored. Sending reset() will reposition the
     * stream back to the marked position.
     * 
     * @param readlimit
     *            ignored.
     */
    @Override
    public synchronized void mark(int readlimit) {
        mark = pos;
    }

    /**
     * Returns a boolean indicating whether or not this ByteArrayInputStream
     * supports mark() and reset(). This implementation returns
     * <code>true</code>.
     * 
     * @return <code>true</code> indicates this stream supports mark/reset,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single byte from this ByteArrayInputStream and returns the result
     * as an int. The low-order byte is returned or -1 of the end of stream was
     * encountered. This implementation returns the next available byte from the
     * target byte array.
     * 
     * @return the byte read or -1 if end of stream.
     */
    @Override
    public synchronized int read() {
        return pos < count ? buf[pos++] & 0xFF : -1;
    }

    /**
     * Reads at most <code>len</code> bytes from this ByteArrayInputStream and
     * stores them in byte array <code>b</code> starting at offset
     * <code>off</code>. Answer the number of bytes actually read or -1 if no
     * bytes were read and end of stream was encountered. This implementation
     * reads bytes from the target byte array.
     * 
     * @param b
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>b</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>b</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     */
    @Override
    public synchronized int read(byte b[], int offset, int length) {
        if (b == null) {
            throw new NullPointerException();
        }
        // avoid int overflow
        if (offset < 0 || offset > b.length || length < 0
                || length > b.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        // Are there any bytes available?
        if (this.pos >= this.count) {
            return -1;
        }
        if (length == 0) {
            return 0;
        }

        int copylen = this.count - pos < length ? this.count - pos : length;
        System.arraycopy(buf, pos, b, offset, copylen);
        pos += copylen;
        return copylen;
    }

    /**
     * Reset this ByteArrayInputStream to the last marked location. This
     * implementation resets the position to either the marked position, the
     * start position supplied in the constructor or <code>0</code> if neither
     * is provided.
     */
    @Override
    public synchronized void reset() {
        pos = mark;
    }

    /**
     * Skips <code>count</code> number of bytes in this InputStream.
     * Subsequent <code>read()</code>'s will not return these bytes unless
     * <code>reset()</code> is used. This implementation skips
     * <code>count</code> number of bytes in the target stream.
     * 
     * @param n
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     */
    @Override
    public synchronized long skip(long n) {
        if (n <= 0) {
            return 0;
        }
        int temp = pos;
        pos = this.count - pos < n ? this.count : (int) (pos + n);
        return pos - temp;
    }
}

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
 * InputStream is an abstract class for all byte input streams. It provides
 * basic method implementations for reading bytes from a stream.
 * 
 * @see OutputStream
 */
public abstract class InputStream extends Object implements Closeable {

    private static byte[] skipBuf;

    /**
     * This constructor does nothing interesting. Provided for signature
     * compatibility.
     */
    public InputStream() {
        /* empty */
    }

    /**
     * Returns a int representing then number of bytes that are available before
     * this InputStream will block. This method always returns 0. Subclasses
     * should override and indicate the correct number of bytes available.
     * 
     * @return the number of bytes available before blocking.
     * 
     * @throws IOException
     *             If an error occurs in this InputStream.
     */
    public int available() throws IOException {
        return 0;
    }

    /**
     * Close the InputStream. Concrete implementations of this class should free
     * any resources during close. This implementation does nothing.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this InputStream.
     */
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Set a Mark position in this InputStream. The parameter
     * <code>readLimit</code> indicates how many bytes can be read before a
     * mark is invalidated. Sending reset() will reposition the Stream back to
     * the marked position provided <code>readLimit</code> has not been
     * surpassed.
     * <p>
     * This default implementation does nothing and concrete subclasses must
     * provide their own implementations.
     * 
     * @param readlimit
     *            the number of bytes to be able to read before invalidating the
     *            mark.
     */
    public void mark(int readlimit) {
        /* empty */
    }

    /**
     * Returns a boolean indicating whether or not this InputStream supports
     * mark() and reset(). This class provides a default implementation which
     * returns false.
     * 
     * @return <code>true</code> if mark() and reset() are supported,
     *         <code>false</code> otherwise.
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * Reads a single byte from this InputStream and returns the result as an
     * int. The low-order byte is returned or -1 of the end of stream was
     * encountered. This abstract implementation must be provided by concrete
     * subclasses.
     * 
     * @return the byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public abstract int read() throws IOException;

    /**
     * Reads bytes from the Stream and stores them in byte array <code>b</code>.
     * Answer the number of bytes actually read or -1 if no bytes were read and
     * end of stream was encountered.
     * 
     * @param b
     *            the byte array in which to store the read bytes.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads at most <code>length</code> bytes from the Stream and stores them
     * in byte array <code>b</code> starting at <code>offset</code>. Answer
     * the number of bytes actually read or -1 if no bytes were read and end of
     * stream was encountered.
     * 
     * @param b
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>b</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>b</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public int read(byte b[], int offset, int length) throws IOException {
        // avoid int overflow, check null b
        if (offset < 0 || offset > b.length || length < 0
                || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException();
        }
        for (int i = 0; i < length; i++) {
            int c;
            try {
                if ((c = read()) == -1) {
                    return i == 0 ? -1 : i;
                }
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            b[offset + i] = (byte) c;
        }
        return length;
    }

    /**
     * Reset this InputStream to the last marked location. If the
     * <code>readlimit</code> has been passed or no <code>mark</code> has
     * been set, throw IOException. This implementation throws IOException and
     * concrete subclasses should provide proper implementations.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public synchronized void reset() throws IOException {
        throw new IOException();
    }

    /**
     * Skips <code>n</code> number of bytes in this InputStream. Subsequent
     * <code>read()</code>'s will not return these bytes unless
     * <code>reset()</code> is used. This method may perform multiple reads to
     * read <code>n</code> bytes. This default implementation reads
     * <code>n</code> bytes into a temporary buffer. Concrete subclasses
     * should provide their own implementation.
     * 
     * @param n
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long skipped = 0;
        int toRead = n < 4096 ? (int) n : 4096;
        if (skipBuf == null || skipBuf.length < toRead) {
            skipBuf = new byte[toRead];
        }
        while (skipped < n) {
            int read = read(skipBuf, 0, toRead);
            if (read == -1) {
                return skipped;
            }
            skipped += read;
            if (read < toRead) {
                return skipped;
            }
            if (n - skipped < toRead) {
                toRead = (int) (n - skipped);
            }
        }
        return skipped;
    }
}

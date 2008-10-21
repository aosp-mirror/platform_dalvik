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
 * StringBufferInputStream is a class for to allow a String to be used as an
 * InputStream.
 * 
 * @deprecated Use {@link StringReader}
 */
@Deprecated
public class StringBufferInputStream extends InputStream {
    /**
     * The String containing the data to read.
     */
    protected String buffer;

    /**
     * The total number of characters inside the buffer.
     */
    protected int count;

    /**
     * The current position within the String buffer.
     */
    protected int pos;

    /**
     * Constructs a new StringBufferInputStream on the String <code>str</code>.
     * 
     * @param str
     *            the String to read characters from.
     */
    public StringBufferInputStream(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        buffer = str;
        count = str.length();
    }

    /**
     * Returns an int representing then number of characters that are available
     * to read.
     * 
     * @return the number of characters available.
     * 
     */
    @Override
    public synchronized int available() {
        return count - pos;
    }

    /**
     * Reads a single byte from this InputStream and returns the result as an
     * int. The low-order byte is returned or -1 of the end of stream was
     * encountered.
     * 
     * @return the byte read or -1 if end of stream.
     */
    @Override
    public synchronized int read() {
        return pos < count ? buffer.charAt(pos++) & 0xFF : -1;
    }

    /**
     * Reads at most <code>length</code> bytes from this InputStream and
     * stores them in byte array <code>b</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered.
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
        // According to 22.7.6 should return -1 before checking other
        // parameters.
        if (pos >= count) {
            return -1;
        }
        if (b == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        // avoid int overflow
        if (offset < 0 || offset > b.length || length < 0
                || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }

        int copylen = count - pos < length ? count - pos : length;
        for (int i = 0; i < copylen; i++) {
            b[offset + i] = (byte) buffer.charAt(pos + i);
        }
        pos += copylen;
        return copylen;
    }

    /**
     * Reset this InputStream to position 0. Reads/Skips will now take place
     * from this position.
     * 
     */
    @Override
    public synchronized void reset() {
        pos = 0;
    }

    /**
     * Skips <code>count</code> number of characters in this InputStream.
     * Subsequent <code>read()</code>'s will not return these characters
     * unless <code>reset()</code> is used.
     * 
     * @param n
     *            the number of characters to skip.
     * @return the number of characters actually skipped.
     */
    @Override
    public synchronized long skip(long n) {
        if (n <= 0) {
            return 0;
        }

        int numskipped;
        if (this.count - pos < n) {
            numskipped = this.count - pos;
            pos = this.count;
        } else {
            numskipped = (int) n;
            pos += n;
        }
        return numskipped;
    }
}

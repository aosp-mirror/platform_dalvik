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
 * PushbackInputStream is a filter class which allows bytes read to be pushed
 * back into the stream so that they can be reread. Parsers may find this
 * useful. There is a progammable limit to the number of bytes which may be
 * pushed back. If the buffer of pushed back bytes is empty, bytes are read from
 * the source input stream.
 */
public class PushbackInputStream extends FilterInputStream {
    /**
     * The <code>byte</code> array containing the bytes to read.
     */
    protected byte[] buf;

    /**
     * The current position within the byte array <code>buf</code>. A value
     * equal to buf.length indicates no bytes available. A value of 0 indicates
     * the buffer is full.
     */
    protected int pos;

    /**
     * Constructs a new PushbackInputStream on the InputStream <code>in</code>.
     * The size of the pushback buffer is set to the default, or 1 byte.
     * 
     * @param in
     *            the InputStream to allow pushback operations on.
     */
    public PushbackInputStream(InputStream in) {
        super(in);
        buf = (in == null) ? null : new byte[1];
        pos = 1;
    }

    /**
     * Constructs a new PushbackInputStream on the InputStream <code>in</code>.
     * The size of the pushback buffer is set to <code>size</code>.
     * 
     * @param in
     *            the InputStream to allow pushback operations on.
     * @param size
     *            the size of the pushback buffer (<code>size>=0</code>).
     */
    public PushbackInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); //$NON-NLS-1$
        }
        buf = (in == null) ? null : new byte[size];
        pos = size;
    }

    /**
     * Returns a int representing then number of bytes that are available before
     * this PushbackInputStream will block. This method returns the number of
     * bytes available in the pushback buffer plus those available in the target
     * stream.
     * 
     * @return int the number of bytes available before blocking.
     * 
     * @throws java.io.IOException
     *             If an error occurs in this stream.
     */
    @Override
    public int available() throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        return buf.length - pos + in.available();
    }

    /**
     * Close this PushbackInputStream. This implementation closes the target
     * stream.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this stream.
     */
    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
            buf = null;
        }
    }

    /**
     * Returns a boolean indicating whether or not this PushbackInputStream
     * supports mark() and reset(). This implementation always returns false
     * since PushbackInputStreams do not support mark/reset.
     * 
     * @return boolean indicates whether or not mark() and reset() are
     *         supported.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Reads a single byte from this PushbackInputStream and returns the result
     * as an int. The low-order byte is returned or -1 of the end of stream was
     * encountered. If the pushback buffer does not contain any available bytes
     * then a byte from the target input stream is returned.
     * 
     * @return int The byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If an IOException occurs.
     */
    @Override
    public int read() throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        // Is there a pushback byte available?
        if (pos < buf.length) {
            return (buf[pos++] & 0xFF);
        }
        // Assume read() in the InputStream will return low-order byte or -1
        // if end of stream.
        return in.read();
    }

    /**
     * Reads at most <code>length</code> bytes from this PushbackInputStream
     * and stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered. This implementation
     * reads bytes from the pushback buffer first, then the target stream if
     * more bytes are required to satisfy <code>count</code>.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If an IOException occurs.
     */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        if (buffer == null) {
            throw new NullPointerException();
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || length < 0
                || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int copiedBytes = 0, copyLength = 0, newOffset = offset;
        // Are there pushback bytes available?
        if (pos < buf.length) {
            copyLength = (buf.length - pos >= length) ? length : buf.length
                    - pos;
            System.arraycopy(buf, pos, buffer, newOffset, copyLength);
            newOffset += copyLength;
            copiedBytes += copyLength;
            // Use up the bytes in the local buffer
            pos += copyLength;
        }
        // Have we copied enough?
        if (copyLength == length) {
            return length;
        }
        int inCopied = in.read(buffer, newOffset, length - copiedBytes);
        if (inCopied > 0) {
            return inCopied + copiedBytes;
        }
        if (copiedBytes == 0) {
            return inCopied;
        }
        return copiedBytes;
    }

    /**
     * Skips <code>count</code> number of bytes in this PushbackInputStream.
     * Subsequent <code>read()</code>'s will not return these bytes unless
     * <code>reset()</code> is used. This implementation skips
     * <code>count</code> number of bytes in the buffer and/or the target
     * stream.
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
        if (in == null) {
            throw new IOException(Msg.getString("K0059")); //$NON-NLS-1$
        }
        if (count <= 0) {
            return 0;
        }
        int numSkipped = 0;
        if (pos < buf.length) {
            numSkipped += (count < buf.length - pos) ? count : buf.length - pos;
            pos += numSkipped;
        }
        if (numSkipped < count) {
            numSkipped += in.skip(count - numSkipped);
        }
        return numSkipped;
    }

    /**
     * Push back all the bytes in <code>buffer</code>. The bytes are pushed
     * so that they would be read back buffer[0], buffer[1], etc. If the push
     * back buffer cannot handle the entire contents of <code>buffer</code>,
     * an IOException will be thrown. Some of the buffer may already be in the
     * buffer after the exception is thrown.
     * 
     * @param buffer
     *            the byte array containing bytes to push back into the stream.
     * 
     * @throws IOException
     *             If the pushback buffer becomes, or is, full.
     */
    public void unread(byte[] buffer) throws IOException {
        unread(buffer, 0, buffer.length);
    }

    /**
     * Push back <code>length</code> number of bytes in <code>buffer</code>
     * starting at <code>offset</code>. The bytes are pushed so that they
     * would be read back buffer[offset], buffer[offset+1], etc. If the push
     * back buffer cannot handle the bytes copied from <code>buffer</code>,
     * an IOException will be thrown. Some of the bytes may already be in the
     * buffer after the exception is thrown.
     * 
     * @param buffer
     *            the byte array containing bytes to push back into the stream.
     * @param offset
     *            the location to start taking bytes to push back.
     * @param length
     *            the number of bytes to push back.
     * 
     * @throws IOException
     *             If the pushback buffer becomes, or is, full.
     */
    public void unread(byte[] buffer, int offset, int length)
            throws IOException {
        if (length > pos) {
            // Pushback buffer full
            throw new IOException(Msg.getString("K007e")); //$NON-NLS-1$
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || length < 0
                || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int i = offset + length - 1; i >= offset; i--) {
            unread(buffer[i]);
        }
    }

    /**
     * Push back one <code>byte</code>. Takes the byte <code>oneByte</code>
     * and puts in in the local buffer of bytes to read back before accessing
     * the target input stream.
     * 
     * @param oneByte
     *            the byte to push back into the stream.
     * 
     * @throws IOException
     *             If the pushback buffer is already full.
     */
    public void unread(int oneByte) throws IOException {
        if (buf == null) {
            throw new IOException();
        }
        if (pos == 0) {
            throw new IOException(Msg.getString("K007e")); //$NON-NLS-1$
        }
        buf[--pos] = (byte) oneByte;
    }

    /**
     * Make a mark of the current position in the stream but the mark method
     * does nothing.
     * 
     * @param readlimit
     *            the maximum number of bytes that are able to be read before
     *            the mark becomes invalid
     */
    @Override
    public void mark(int readlimit) {
        return;
    }

    /**
     * Reset current position to the mark made previously int the stream, but
     * the reset method will throw IOException and do nothing else if called.
     * 
     * @throws IOException
     *             If the method is called
     */
    @Override
    public void reset() throws IOException {
        throw new IOException();
    }
}

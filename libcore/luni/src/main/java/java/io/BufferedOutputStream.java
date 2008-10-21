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

// BEGIN android-added
import java.util.logging.Logger;
// END android-added

/**
 * BufferedOutputStream is a class which takes an output stream and
 * <em>buffers</em> the writes to that stream. In this way, costly interaction
 * with the original output stream can be minimized by writing buffered amounts
 * of data infrequently. The drawback is that extra space is required to hold
 * the buffer and copying takes place when writing that buffer.
 * 
 * @see BufferedInputStream
 */
public class BufferedOutputStream extends FilterOutputStream {
    /**
     * The buffer containing the bytes to be written to the target OutputStream.
     */
    protected byte[] buf;

    /**
     * The total number of bytes inside the byte array <code>buf</code>.
     */
    protected int count;

    /**
     * Constructs a new BufferedOutputStream on the OutputStream
     * <code>out</code>. The default buffer size (8Kb) is allocated and all
     * writes are now filtered through this stream.
     * 
     * @param out
     *            the OutputStream to buffer writes on.
     */
    public BufferedOutputStream(OutputStream out) {
        super(out);
        buf = new byte[8192];

        // BEGIN android-added
        /*
         * For Android, we want to discourage the use of this
         * constructor (with its arguably too-large default), so we
         * note its use in the log. We don't disable it, nor do we
         * alter the default, however, because we still aim to behave
         * compatibly, and the default value, though not documented,
         * is established by convention.
         */
        Logger.global.info(
                "Default buffer size used in BufferedOutputStream " +
                "constructor. It would be " +
                "better to be explicit if a 8k buffer is required.");
        // END android-added
    }

    /**
     * Constructs a new BufferedOutputStream on the OutputStream
     * <code>out</code>. The buffer size is set to <code>size</code> and
     * all writes are now filtered through this stream.
     * 
     * @param out
     *            the OutputStream to buffer writes on.
     * @param size
     *            the size of the buffer in bytes.
     * @throws IllegalArgumentException
     *             the size is <= 0
     */
    public BufferedOutputStream(OutputStream out, int size) {
        super(out);
        if (size <= 0) {
            // K0058=size must be > 0
            throw new IllegalArgumentException(Msg.getString("K0058")); //$NON-NLS-1$
        }
        buf = new byte[size];
    }

    /**
     * Flush this BufferedOutputStream to ensure all pending data is written out
     * to the target OutputStream. In addition, the target stream is also
     * flushed.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this
     *             BufferedOutputStream.
     */
    @Override
    public synchronized void flush() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
        }
        count = 0;
        out.flush();
    }

    /**
     * Writes <code>count</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at <code>offset</code> to this
     * BufferedOutputStream. If there is room in the buffer to hold the bytes,
     * they are copied in. If not, the buffered bytes plus the bytes in
     * <code>buffer</code> are written to the target stream, the target is
     * flushed, and the buffer is cleared.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param length
     *            number of bytes in buffer to write
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             BufferedOutputStream.
     * @throws NullPointerException
     *             If buffer is null.
     * @throws ArrayIndexOutOfBoundsException
     *             If offset or count is outside of bounds.
     */
    @Override
    public synchronized void write(byte[] buffer, int offset, int length)
            throws IOException {
        if (buffer == null) {
            // K0047=buffer is null
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        if (offset < 0 || offset > buffer.length - length || length < 0) {
            // K002f=Arguments out of bounds
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        if (count == 0 && length >= buf.length) {
            out.write(buffer, offset, length);
            return;
        }
        int available = buf.length - count;
        if (length < available) {
            available = length;
        }
        if (available > 0) {
            System.arraycopy(buffer, offset, buf, count, available);
            count += available;
        }
        if (count == buf.length) {
            out.write(buf, 0, buf.length);
            count = 0;
            if (length > available) {
                offset += available;
                available = length - available;
                if (available >= buf.length) {
                    out.write(buffer, offset, available);
                } else {
                    System.arraycopy(buffer, offset, buf, count, available);
                    count += available;
                }
            }
        }
    }

    /**
     * Writes the specified byte <code>oneByte</code> to this
     * BufferedOutputStream. Only the low order byte of <code>oneByte</code>
     * is written. If there is room in the buffer, the byte is copied in and the
     * count incremented. Otherwise, the buffer plus <code>oneByte</code> are
     * written to the target stream, the target is flushed, and the buffer is
     * reset.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             BufferedOutputStream.
     */
    @Override
    public synchronized void write(int oneByte) throws IOException {
        if (count == buf.length) {
            out.write(buf, 0, count);
            count = 0;
        }
        buf[count++] = (byte) oneByte;
    }
}

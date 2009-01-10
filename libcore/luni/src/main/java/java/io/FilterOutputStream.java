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
 * Wraps an existing {@link OutputStream} and performs some transformation on
 * the output data while it is being written. Transformations can be anything
 * from a simple byte-wise filtering output data to an on-the-fly compression or
 * decompression of the underlying stream. Output streams that wrap another
 * output stream and provide some additional functionality on top of it usually
 * inherit from this class.
 * 
 * @see FilterOutputStream
 * 
 * @since Android 1.0
 */
public class FilterOutputStream extends OutputStream {

    /**
     * The target output stream for this filter stream.
     * 
     * @since Android 1.0
     */
    protected OutputStream out;

    /**
     * Constructs a new {@code FilterOutputStream} with {@code out} as its
     * target stream.
     * 
     * @param out
     *            the target stream that this stream writes to.
     * @since Android 1.0
     */
    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Closes this stream. This implementation closes the target stream.
     * 
     * @throws IOException
     *             if an error occurs attempting to close this stream.
     * @since Android 1.0
     */
    @Override
    public void close() throws IOException {
        try {
            flush();
        } catch (IOException e) {
            // Ignored
        }
        /* Make sure we clean up this stream if exception fires */
        out.close();
    }

    /**
     * Ensures that all pending data is sent out to the target stream. This
     * implementation flushes the target stream.
     * 
     * @throws IOException
     *             if an error occurs attempting to flush this stream.
     * @since Android 1.0
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Writes the entire contents of the byte array {@code buffer} to this
     * stream. This implementation writes the {@code buffer} to the target
     * stream.
     * 
     * @param buffer
     *            the buffer to be written.
     * @throws IOException
     *             if an I/O error occurs while writing to this stream.
     * @since Android 1.0
     */
    @Override
    public void write(byte[] buffer) throws IOException {
        // BEGIN android-note
        // changed array notation to be consistent with the rest of harmony
        // END android-note
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * {@code offset} to the target stream.
     * 
     * @param buffer
     *            the buffer to write.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes in {@code buffer} to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code buffer}.
     * @throws IOException
     *             if an I/O error occurs while writing to this stream.
     * @since Android 1.0
     */
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        // BEGIN android-note
        // changed array notation to be consistent with the rest of harmony
        // END android-note
        // avoid int overflow, force null buffer check first
        // BEGIN android-changed
        // Exception priorities (in case of multiple errors) differ from
        // RI, but are spec-compliant.
        // removed redundant check, made implicit null check explicit,
        // used (offset | count) < 0 instead of (offset < 0) || (count < 0)
        // to safe one operation
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        // END android-changed
        for (int i = 0; i < count; i++) {
            // Call write() instead of out.write() since subclasses could
            // override the write() method.
            write(buffer[offset + i]);
        }
    }

    /**
     * Writes one byte to the target stream. Only the low order byte of the
     * integer {@code oneByte} is written.
     * 
     * @param oneByte
     *            the byte to be written.
     * @throws IOException
     *             if an I/O error occurs while writing to this stream.
     * @since Android 1.0
     */
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
    }
}

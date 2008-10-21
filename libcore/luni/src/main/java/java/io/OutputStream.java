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
 * OutputStream is an abstract class for all byte output streams. It provides
 * basic method implementations for writing bytes to a stream.
 * 
 * @see InputStream
 */
public abstract class OutputStream implements Closeable, Flushable {

    /**
     * Default constructor.
     */
    public OutputStream() {
        super();
    }

    /**
     * Close this OutputStream. Concrete implementations of this class should
     * free any resources during close. This implementation does nothing.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this OutputStream.
     */
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Flush this OutputStream. Concrete implementations of this class should
     * ensure any pending writes to the underlying stream are written out when
     * this method is envoked. This implementation does nothing.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this OutputStream.
     */
    public void flush() throws IOException {
        /* empty */
    }

    /**
     * Writes the entire contents of the byte array <code>buffer</code> to
     * this OutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this OutputStream.
     */
    public void write(byte buffer[]) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes <code>count</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at <code>offset</code> to this
     * OutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param count
     *            number of bytes in buffer to write
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this OutputStream.
     * @throws IndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    public void write(byte buffer[], int offset, int count) throws IOException {
        // avoid int overflow, check null buffer
        if (offset < 0 || offset > buffer.length || count < 0
                || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        for (int i = offset; i < offset + count; i++) {
            write(buffer[i]);
        }
    }

    /**
     * Writes the specified byte <code>oneByte</code> to this OutputStream.
     * Only the low order byte of <code>oneByte</code> is written.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this OutputStream.
     */
    public abstract void write(int oneByte) throws IOException;
}

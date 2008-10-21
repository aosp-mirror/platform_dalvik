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

import java.security.AccessController;

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

// BEGIN android-added
import java.util.logging.Logger;
// END android-added

/**
 * BufferedWriter is for writing buffered character output. Characters written
 * to this Writer are buffered internally before being committed to the target
 * Writer.
 * 
 * @see BufferedReader
 */
public class BufferedWriter extends Writer {

    private Writer out;

    private char buf[];

    private int pos;

    private final String lineSeparator = AccessController
            .doPrivileged(new PriviAction<String>("line.separator")); //$NON-NLS-1$

    /**
     * Constructs a new BufferedReader with <code>out</code> as the Writer on
     * which to buffer write operations. The buffer size is set to the default,
     * which is 8K.
     * 
     * @param out
     *            The Writer to buffer character writing on
     */
    public BufferedWriter(Writer out) {
        super(out);
        this.out = out;
        buf = new char[8192];

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
                "Default buffer size used in BufferedWriter " +
                "constructor. It would be " +
                "better to be explicit if a 8k-char buffer is required.");
        // END android-added
    }

    /**
     * Constructs a new BufferedReader with <code>out</code> as the Writer on
     * which buffer write operations. The buffer size is set to
     * <code>size</code>.
     * 
     * @param out
     *            The Writer to buffer character writing on.
     * @param size
     *            The size of the buffer to use.
     */
    public BufferedWriter(Writer out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); //$NON-NLS-1$
        }
        this.out = out;
        this.buf = new char[size];
    }

    /**
     * Close this BufferedWriter. The contents of the buffer are flushed, the
     * target writer is closed, and the buffer is released. Only the first
     * invocation of close has any effect.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this Writer.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (!isClosed()) {
                flush();
                out.close();
                buf = null;
                out = null;
            }
        }
    }

    /**
     * Flush this BufferedWriter. The contents of the buffer are committed to
     * the target writer and it is then flushed.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this Writer.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); //$NON-NLS-1$
            }
            if (pos > 0) {
                out.write(buf, 0, pos);
            }
            pos = 0;
            out.flush();
        }
    }

    /**
     * Answer a boolean indicating whether or not this BufferedWriter is closed.
     * 
     * @return <code>true</code> if this reader is closed, <code>false</code>
     *         otherwise
     */
    private boolean isClosed() {
        return out == null;
    }

    /**
     * Write a newline to thie Writer. A newline is determined by the System
     * property "line.separator". The target writer may or may not be flushed
     * when a newline is written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this Writer.
     */
    public void newLine() throws IOException {
        write(lineSeparator, 0, lineSeparator.length());
    }

    /**
     * Writes out <code>count</code> characters starting at
     * <code>offset</code> in <code>buf</code> to this BufferedWriter. If
     * <code>count</code> is greater than this Writers buffer then flush the
     * contents and also write the characters directly to the target Writer.
     * 
     * @param cbuf
     *            the non-null array containing characters to write.
     * @param offset
     *            offset in buf to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws IOException
     *             If this Writer has already been closed or some other
     *             IOException occurs.
     * @throws IndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    @Override
    public void write(char[] cbuf, int offset, int count) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); //$NON-NLS-1$
            }
            if (offset < 0 || offset > cbuf.length - count || count < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (pos == 0 && count >= this.buf.length) {
                out.write(cbuf, offset, count);
                return;
            }
            int available = this.buf.length - pos;
            if (count < available) {
                available = count;
            }
            if (available > 0) {
                System.arraycopy(cbuf, offset, this.buf, pos, available);
                pos += available;
            }
            if (pos == this.buf.length) {
                out.write(this.buf, 0, this.buf.length);
                pos = 0;
                if (count > available) {
                    offset += available;
                    available = count - available;
                    if (available >= this.buf.length) {
                        out.write(cbuf, offset, available);
                        return;
                    }

                    System.arraycopy(cbuf, offset, this.buf, pos, available);
                    pos += available;
                }
            }
        }
    }

    /**
     * Writes the character <code>oneChar</code> BufferedWriter. If the buffer
     * is filled by writing this character, flush this Writer. Only the lower 2
     * bytes are written.
     * 
     * @param oneChar
     *            The Character to write out.
     * 
     * @throws IOException
     *             If this Writer has already been closed or some other
     *             IOException occurs.
     */
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); //$NON-NLS-1$
            }
            if (pos >= buf.length) {
                out.write(buf, 0, buf.length);
                pos = 0;
            }
            buf[pos++] = (char) oneChar;
        }
    }

    /**
     * Writes out <code>count</code> characters starting at
     * <code>offset</code> in <code>str</code> to this BufferedWriter. If
     * <code>count</code> is greater than this Writers buffer then flush the
     * contents and also write the characters directly to the target Writer.
     * 
     * @param str
     *            the non-null String containing characters to write
     * @param offset
     *            offset in str to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws IOException
     *             If this Writer has already been closed or some other
     *             IOException occurs.
     * @throws ArrayIndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005d")); //$NON-NLS-1$
            }
            if (count <= 0) {
                return;
            }
            if (offset > str.length() - count || offset < 0) {
                throw new StringIndexOutOfBoundsException();
            }
            if (pos == 0 && count >= buf.length) {
                char[] chars = new char[count];
                str.getChars(offset, offset + count, chars, 0);
                out.write(chars, 0, count);
                return;
            }
            int available = buf.length - pos;
            if (count < available) {
                available = count;
            }
            if (available > 0) {
                str.getChars(offset, offset + available, buf, pos);
                pos += available;
            }
            if (pos == buf.length) {
                out.write(this.buf, 0, this.buf.length);
                pos = 0;
                if (count > available) {
                    offset += available;
                    available = count - available;
                    if (available >= buf.length) {
                        char[] chars = new char[count];
                        str.getChars(offset, offset + available, chars, 0);
                        out.write(chars, 0, available);
                        return;
                    }
                    str.getChars(offset, offset + available, buf, pos);
                    pos += available;
                }
            }
        }
    }
}

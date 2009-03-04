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
 * A specialized {@link Reader} that reads characters from a {@code String} in
 * a sequential manner.
 * 
 * @see StringWriter
 * 
 * @since Android 1.0
 */
public class StringReader extends Reader {
    private String str;

    private int markpos = -1;

    private int pos;

    private int count;

    /**
     * Construct a new {@code StringReader} with {@code str} as source. The size
     * of the reader is set to the {@code length()} of the string and the Object
     * to synchronize access through is set to {@code str}.
     * 
     * @param str
     *            the source string for this reader.
     * @since Android 1.0
     */
    public StringReader(String str) {
        super(str);
        this.str = str;
        this.count = str.length();
    }

    /**
     * Closes this reader. Once it is closed, read operations on this reader
     * will throw an {@code IOException}. Only the first invocation of this
     * method has any effect.
     * 
     * @since Android 1.0
     */
    @Override
    public void close() {
        synchronized (lock) {
            if (isClosed()) {
                return;
            }
            str = null;
        }
    }

    /**
     * Returns a boolean indicating whether this reader is closed.
     * 
     * @return {@code true} if closed, otherwise {@code false}.
     */
    private boolean isClosed() {
        return str == null;
    }

    /**
     * Sets a mark position in this reader. The parameter {@code readLimit} is
     * ignored for this class. Calling {@code reset()} will reposition the
     * reader back to the marked position.
     * 
     * @param readLimit
     *            ignored for {@code StringReader} instances.
     * @throws IllegalArgumentException
     *             if {@code readLimit < 0}.
     * @throws IOException
     *             if this reader is closed.
     * @see #markSupported()
     * @see #reset()
     * @since Android 1.0
     */
    @Override
    public void mark(int readLimit) throws IOException {
        if (readLimit < 0) {
            throw new IllegalArgumentException();
        }

        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); //$NON-NLS-1$
            }
            markpos = pos;
        }
    }

    /**
     * Indicates whether this reader supports the {@code mark()} and {@code
     * reset()} methods. This implementation returns {@code true}.
     * 
     * @return always {@code true}.
     * @since Android 1.0
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single character from the source string and returns it as an
     * integer with the two higher-order bytes set to 0. Returns -1 if the end
     * of the source string has been reached.
     * 
     * @return the character read or -1 if the end of the source string has been
     *         reached.
     * @throws IOException
     *             if this reader is closed.
     * @since Android 1.0
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); //$NON-NLS-1$
            }
            if (pos != count) {
                return str.charAt(pos++);
            }
            return -1;
        }
    }

    /**
     * Reads at most {@code len} characters from the source string and stores
     * them at {@code offset} in the character array {@code buf}. Returns the
     * number of characters actually read or -1 if the end of the source string
     * has been reached.
     * 
     * @param buf
     *            the character array to store the characters read.
     * @param offset
     *            the initial position in {@code buffer} to store the characters
     *            read from this reader.
     * @param len
     *            the maximum number of characters to read.
     * @return the number of characters read or -1 if the end of the reader has
     *         been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code len < 0}, or if
     *             {@code offset + len} is greater than the size of {@code buf}.
     * @throws IOException
     *             if this reader is closed.
     * @since Android 1.0
     */
    @Override
    public int read(char[] buf, int offset, int len) throws IOException {
        // BEGIN android-note
        // changed array notation to be consistent with the rest of harmony
        // END android-note
        // avoid int overflow
        // BEGIN android-changed
        // Exception priorities (in case of multiple errors) differ from
        // RI, but are spec-compliant.
        // removed redundant check, added null check, used (offset | len) < 0
        // instead of (offset < 0) || (len < 0) to safe one operation
        if (buf == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        if ((offset | len) < 0 || len > buf.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        // END android-changed
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); //$NON-NLS-1$
            }
            if (pos == this.count) {
                return -1;
            }
            int end = pos + len > this.count ? this.count : pos + len;
            str.getChars(pos, end, buf, offset);
            int read = end - pos;
            pos = end;
            return read;
        }
    }

    /**
     * Indicates whether this reader is ready to be read without blocking. This
     * implementation always returns {@code true}.
     * 
     * @return always {@code true}.
     * @throws IOException
     *             if this reader is closed.
     * @see #read()
     * @see #read(char[], int, int)
     * @since Android 1.0
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); //$NON-NLS-1$
            }
            return true;
        }
    }

    /**
     * Resets this reader's position to the last {@code mark()} location.
     * Invocations of {@code read()} and {@code skip()} will occur from this new
     * location. If this reader has not been marked, it is reset to the
     * beginning of the source string.
     * 
     * @throws IOException
     *             if this reader is closed.
     * @see #mark(int)
     * @see #markSupported()
     * @since Android 1.0
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); //$NON-NLS-1$
            }
            pos = markpos != -1 ? markpos : 0;
        }
    }

    /**
     * Skips {@code amount} characters in the source string. Subsequent calls of
     * {@code read} methods will not return these characters unless {@code
     * reset()} is used.
     * 
     * @param ns
     *            the maximum number of characters to skip.
     * @return the number of characters actually skipped or 0 if {@code ns < 0}.
     * @throws IOException
     *             if this reader is closed.
     * @see #mark(int)
     * @see #markSupported()
     * @see #reset()
     * @since Android 1.0
     */
    @Override
    public long skip(long ns) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K0083")); //$NON-NLS-1$
            }
            if (ns <= 0) {
                return 0;
            }
            long skipped = 0;
            if (ns < this.count - pos) {
                pos = pos + (int) ns;
                skipped = ns;
            } else {
                skipped = this.count - pos;
                pos = this.count;
            }
            return skipped;
        }
    }
}

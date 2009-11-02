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

// BEGIN android-note
// We've made several changes including:
//  - throw an IOException when a pipe is closed during a write
//  - fix shallow concurrency problems, always lock on 'this'
//  - improved consistency with PipedInputStream
// END android-note

import org.apache.harmony.luni.util.Msg;

/**
 * Receives information on a communications pipe. When two threads want to pass
 * data back and forth, one creates a piped writer and the other creates a piped
 * reader.
 *
 * @see PipedWriter
 */
public class PipedReader extends Reader {

    private Thread lastReader;

    private Thread lastWriter;

    private boolean isClosed;

    /**
     * The circular buffer through which data is passed. Data is read from the
     * range {@code [out, in)} and written to the range {@code [in, out)}.
     * Data in the buffer is either sequential: <pre>
     *     { - - - X X X X X X X - - - - - }
     *             ^             ^
     *             |             |
     *            out           in</pre>
     * ...or wrapped around the buffer's end: <pre>
     *     { X X X X - - - - - - - - X X X }
     *               ^               ^
     *               |               |
     *              in              out</pre>
     * When the buffer is empty, {@code in == -1}. Reading when the buffer is
     * empty will block until data is available. When the buffer is full,
     * {@code in == out}. Writing when the buffer is full will block until free
     * space is available.
     */
    private char[] buffer;

    /**
     * The index in {@code buffer} where the next character will be written.
     */
    private int in = -1;

    /**
     * The index in {@code buffer} where the next character will be read.
     */
    private int out;

    /**
     * The size of the default pipe in characters
     */
    private static final int PIPE_SIZE = 1024;

    /**
     * Indicates if this pipe is connected
     */
    boolean isConnected;

    /**
     * Constructs a new unconnected {@code PipedReader}. The resulting reader
     * must be connected to a {@code PipedWriter} before data may be read from
     * it.
     */
    public PipedReader() {}

    /**
     * Constructs a new {@code PipedReader} connected to the {@link PipedWriter}
     * {@code out}. Any data written to the writer can be read from the this
     * reader.
     *
     * @param out
     *            the {@code PipedWriter} to connect to.
     * @throws IOException
     *             if {@code out} is already connected.
     */
    public PipedReader(PipedWriter out) throws IOException {
        connect(out);
    }

    /**
     * Closes this reader. This implementation releases the buffer used for
     * the pipe and notifies all threads waiting to read or write.
     *
     * @throws IOException
     *             if an error occurs while closing this reader.
     */
    @Override
    public synchronized void close() throws IOException {
        buffer = null;
        notifyAll();
    }

    /**
     * Connects this {@code PipedReader} to a {@link PipedWriter}. Any data
     * written to the writer becomes readable in this reader.
     *
     * @param src
     *            the writer to connect to.
     * @throws IOException
     *             if this reader is closed or already connected, or if {@code
     *             src} is already connected.
     */
    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }

    /**
     * Establishes the connection to the PipedWriter.
     *
     * @throws IOException
     *             If this Reader is already connected.
     */
    synchronized void establishConnection() throws IOException {
        if (isConnected) {
            throw new IOException(Msg.getString("K007a")); //$NON-NLS-1$
        }
        buffer = new char[PIPE_SIZE];
        isConnected = true;
    }

    /**
     * Reads a single character from this reader and returns it as an integer
     * with the two higher-order bytes set to 0. Returns -1 if the end of the
     * reader has been reached. If there is no data in the pipe, this method
     * blocks until data is available, the end of the reader is detected or an
     * exception is thrown.
     * <p>
     * Separate threads should be used to read from a {@code PipedReader} and to
     * write to the connected {@link PipedWriter}. If the same thread is used,
     * a deadlock may occur.
     *
     * @return the character read or -1 if the end of the reader has been
     *         reached.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        char[] carray = new char[1];
        int result = read(carray, 0, 1);
        return result != -1 ? carray[0] : result;
    }

    /**
     * Reads at most {@code count} characters from this reader and stores them
     * in the character array {@code buffer} starting at {@code offset}. If
     * there is no data in the pipe, this method blocks until at least one byte
     * has been read, the end of the reader is detected or an exception is
     * thrown.
     * <p>
     * Separate threads should be used to read from a {@code PipedReader} and to
     * write to the connected {@link PipedWriter}. If the same thread is used, a
     * deadlock may occur.
     *
     * @param buffer
     *            the character array in which to store the characters read.
     * @param offset
     *            the initial position in {@code bytes} to store the characters
     *            read from this reader.
     * @param count
     *            the maximum number of characters to store in {@code buffer}.
     * @return the number of characters read or -1 if the end of the reader has
     *         been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is greater than the size of {@code buffer}.
     * @throws InterruptedIOException
     *             if the thread reading from this reader is interrupted.
     * @throws IOException
     *             if this reader is closed or not connected to a writer, or if
     *             the thread writing to the connected writer is no longer
     *             alive.
     */
    @Override
    public synchronized int read(char[] buffer, int offset, int count) throws IOException {
        if (!isConnected) {
            throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
        }
        if (this.buffer == null) {
            throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
        }
        // avoid int overflow
        // BEGIN android-changed
        // Exception priorities (in case of multiple errors) differ from
        // RI, but are spec-compliant.
        // made implicit null check explicit,
        // used (offset | count) < 0 instead of (offset < 0) || (count < 0)
        // to safe one operation
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        // END android-changed
        if (count == 0) {
            return 0;
        }
        /**
         * Set the last thread to be reading on this PipedReader. If
         * lastReader dies while someone is waiting to write an IOException
         * of "Pipe broken" will be thrown in receive()
         */
        lastReader = Thread.currentThread();
        try {
            boolean first = true;
            while (in == -1) {
                // Are we at end of stream?
                if (isClosed) {
                    return -1;
                }
                if (!first && lastWriter != null && !lastWriter.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
                }
                first = false;
                // Notify callers of receive()
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }

        int copyLength = 0;
        /* Copy chars from out to end of buffer first */
        if (out >= in) {
            copyLength = count > this.buffer.length - out ? this.buffer.length - out
                    : count;
            System.arraycopy(this.buffer, out, buffer, offset, copyLength);
            out += copyLength;
            if (out == this.buffer.length) {
                out = 0;
            }
            if (out == in) {
                // empty buffer
                in = -1;
                out = 0;
            }
        }

        /*
         * Did the read fully succeed in the previous copy or is the buffer
         * empty?
         */
        if (copyLength == count || in == -1) {
            return copyLength;
        }

        int charsCopied = copyLength;
        /* Copy bytes from 0 to the number of available bytes */
        copyLength = in - out > count - copyLength ? count - copyLength
                : in - out;
        System.arraycopy(this.buffer, out, buffer, offset + charsCopied,
                copyLength);
        out += copyLength;
        if (out == in) {
            // empty buffer
            in = -1;
            out = 0;
        }
        return charsCopied + copyLength;
    }

    /**
     * Indicates whether this reader is ready to be read without blocking.
     * Returns {@code true} if this reader will not block when {@code read} is
     * called, {@code false} if unknown or blocking will occur. This
     * implementation returns {@code true} if the internal buffer contains
     * characters that can be read.
     *
     * @return always {@code false}.
     * @throws IOException
     *             if this reader is closed or not connected, or if some other
     *             I/O error occurs.
     * @see #read()
     * @see #read(char[], int, int)
     */
    @Override
    public synchronized boolean ready() throws IOException {
        if (!isConnected) {
            throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
        }
        return in != -1;
    }

    /**
     * Receives a char and stores it into the PipedReader. This called by
     * PipedWriter.write() when writes occur.
     * <P>
     * If the buffer is full and the thread sending #receive is interrupted, the
     * InterruptedIOException will be thrown.
     *
     * @param oneChar
     *            the char to store into the pipe.
     *
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    synchronized void receive(char oneChar) throws IOException {
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
        }
        if (lastReader != null && !lastReader.isAlive()) {
            throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
        }
        /*
        * Set the last thread to be writing on this PipedWriter. If
        * lastWriter dies while someone is waiting to read an IOException
        * of "Pipe broken" will be thrown in read()
        */
        lastWriter = Thread.currentThread();
        try {
            while (buffer != null && out == in) {
                notifyAll();
                // BEGIN android-changed
                wait(1000);
                // END android-changed
                if (lastReader != null && !lastReader.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
                }
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
        }
        if (in == -1) {
            in = 0;
        }
        buffer[in++] = oneChar;
        if (in == buffer.length) {
            in = 0;
        }
    }

    /**
     * Receives a char array and stores it into the PipedReader. This called by
     * PipedWriter.write() when writes occur.
     * <P>
     * If the buffer is full and the thread sending #receive is interrupted, the
     * InterruptedIOException will be thrown.
     *
     * @param chars
     *            the char array to store into the pipe.
     * @param offset
     *            offset to start reading from
     * @param count
     *            total characters to read
     *
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    synchronized void receive(char[] chars, int offset, int count) throws IOException {
        if (chars == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        if ((offset | count) < 0 || count > chars.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
        }
        if (lastReader != null && !lastReader.isAlive()) {
            throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
        }
        /**
         * Set the last thread to be writing on this PipedWriter. If
         * lastWriter dies while someone is waiting to read an IOException
         * of "Pipe broken" will be thrown in read()
         */
        lastWriter = Thread.currentThread();
        while (count > 0) {
            try {
                while (buffer != null && out == in) {
                    notifyAll();
                    // BEGIN android-changed
                    wait(1000);
                    // END android-changed
                    if (lastReader != null && !lastReader.isAlive()) {
                        throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
                    }
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
            if (buffer == null) {
                throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
            }
            if (in == -1) {
                in = 0;
            }
            if (in >= out) {
                int length = buffer.length - in;
                if (count < length) {
                    length = count;
                }
                System.arraycopy(chars, offset, buffer, in, length);
                offset += length;
                count -= length;
                in += length;
                if (in == buffer.length) {
                    in = 0;
                }
            }
            if (count > 0 && in != out) {
                int length = out - in;
                if (count < length) {
                    length = count;
                }
                System.arraycopy(chars, offset, buffer, in, length);
                offset += length;
                count -= length;
                in += length;
            }
        }
    }

    synchronized void done() {
        isClosed = true;
        notifyAll();
    }
}

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
 * Receives information on a communications pipe. When two threads want to pass
 * data back and forth, one creates a piped writer and the other creates a piped
 * reader.
 * 
 * @see PipedWriter
 * 
 * @since Android 1.0
 */
public class PipedReader extends Reader {

    private Thread lastReader;

    private Thread lastWriter;

    private boolean isClosed;

    /**
     * The circular buffer through which data is passed.
     */
    private char data[];

    /**
     * The index in {@code buffer} where the next character will be
     * written.
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
    private boolean isConnected;

    /**
     * Constructs a new unconnected {@code PipedReader}. The resulting reader
     * must be connected to a {@code PipedWriter} before data may be read from
     * it.
     * 
     * @see PipedWriter
     * @since Android 1.0
     */
    public PipedReader() {
        data = new char[PIPE_SIZE];
    }

    /**
     * Constructs a new {@code PipedReader} connected to the {@link PipedWriter}
     * {@code out}. Any data written to the writer can be read from the this
     * reader.
     * 
     * @param out
     *            the {@code PipedWriter} to connect to.
     * @throws IOException
     *             if {@code out} is already connected.
     * @since Android 1.0
     */
    public PipedReader(PipedWriter out) throws IOException {
        this();
        connect(out);
    }

    /**
     * Closes this reader. This implementation releases the buffer used for
     * the pipe and notifies all threads waiting to read or write.
     * 
     * @throws IOException
     *             if an error occurs while closing this reader.
     * @since Android 1.0
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            /* No exception thrown if already closed */
            if (data != null) {
                /* Release buffer to indicate closed. */
                data = null;
            }
        }
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
     * @since Android 1.0
     */
    public void connect(PipedWriter src) throws IOException {
        synchronized (lock) {
            src.connect(this);
        }
    }

    /**
     * Establishes the connection to the PipedWriter.
     * 
     * @throws IOException
     *             If this Reader is already connected.
     */
    void establishConnection() throws IOException {
        synchronized (lock) {
            if (data == null) {
                throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
            }
            if (isConnected) {
                throw new IOException(Msg.getString("K007a")); //$NON-NLS-1$
            }
            isConnected = true;
        }
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
     * </p>
     * 
     * @return the character read or -1 if the end of the reader has been
     *         reached.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     * @since Android 1.0
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
     * </p>
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
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            if (!isConnected) {
                throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
            }
            if (data == null) {
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
                    lock.notifyAll();
                    lock.wait(1000);
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }

            int copyLength = 0;
            /* Copy chars from out to end of buffer first */
            if (out >= in) {
                copyLength = count > data.length - out ? data.length - out
                        : count;
                System.arraycopy(data, out, buffer, offset, copyLength);
                out += copyLength;
                if (out == data.length) {
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
            System.arraycopy(data, out, buffer, offset + charsCopied,
                    copyLength);
            out += copyLength;
            if (out == in) {
                // empty buffer
                in = -1;
                out = 0;
            }
            return charsCopied + copyLength;
        }
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
     * @since Android 1.0
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (!isConnected) {
                throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
            }
            if (data == null) {
                throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
            }
            return in != -1;
        }
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
    void receive(char oneChar) throws IOException {
        synchronized (lock) {
            if (data == null) {
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
                while (data != null && out == in) {
                    lock.notifyAll();
                    // BEGIN android-changed
                    lock.wait(1000);
                    // END android-changed
                    if (lastReader != null && !lastReader.isAlive()) {
                        throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
                    }
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
            if (data != null) {
                if (in == -1) {
                    in = 0;
                }
                data[in++] = oneChar;
                if (in == data.length) {
                    in = 0;
                }
                return;
            }
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
    void receive(char[] chars, int offset, int count) throws IOException {
        synchronized (lock) {
            if (data == null) {
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
                    while (data != null && out == in) {
                        lock.notifyAll();
                        // BEGIN android-changed
                        lock.wait(1000);
                        // END android-changed
                        if (lastReader != null && !lastReader.isAlive()) {
                            throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
                        }
                    }
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
                if (data == null) {
                    break;
                }
                if (in == -1) {
                    in = 0;
                }
                if (in >= out) {
                    int length = data.length - in;
                    if (count < length) {
                        length = count;
                    }
                    System.arraycopy(chars, offset, data, in, length);
                    offset += length;
                    count -= length;
                    in += length;
                    if (in == data.length) {
                        in = 0;
                    }
                }
                if (count > 0 && in != out) {
                    int length = out - in;
                    if (count < length) {
                        length = count;
                    }
                    System.arraycopy(chars, offset, data, in, length);
                    offset += length;
                    count -= length;
                    in += length;
                }
            }
            if (count == 0) {
                return;
            }
        }
    }

    void done() {
        synchronized (lock) {
            isClosed = true;
            lock.notifyAll();
        }
    }

    void flush() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}

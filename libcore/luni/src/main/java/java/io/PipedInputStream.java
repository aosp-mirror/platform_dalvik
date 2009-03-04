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
 * Receives information from a communications pipe. When two threads want to
 * pass data back and forth, one creates a piped output stream and the other one
 * creates a piped input stream.
 * 
 * @see PipedOutputStream
 * 
 * @since Android 1.0
 */
public class PipedInputStream extends InputStream {

    private Thread lastReader, lastWriter;

    private boolean isClosed = false;

    /**
     * The circular buffer through which data is passed.
     * 
     * @since Android 1.0
     */
    protected byte buffer[];

    /**
     * The index in {@code buffer} where the next byte will be written.
     * 
     * @since Android 1.0
     */
    protected int in = -1;

    /**
     * The index in {@code buffer} where the next byte will be read.
     * 
     * @since Android 1.0
     */
    protected int out = 0;

    /**
     * The size of the default pipe in bytes.
     * 
     * @since Android 1.0
     */
    protected static final int PIPE_SIZE = 1024;

    /**
     * Indicates if this pipe is connected.
     */
    boolean isConnected = false;

    /**
     * Constructs a new unconnected {@code PipedInputStream}. The resulting
     * stream must be connected to a {@link PipedOutputStream} before data may
     * be read from it.
     * 
     * @since Android 1.0
     */
    public PipedInputStream() {
        /* empty */
    }

    /**
     * Constructs a new {@code PipedInputStream} connected to the
     * {@link PipedOutputStream} {@code out}. Any data written to the output
     * stream can be read from the this input stream.
     * 
     * @param out
     *            the piped output stream to connect to.
     * @throws IOException
     *             if this stream or {@code out} are already connected.
     * @since Android 1.0
     */
    public PipedInputStream(PipedOutputStream out) throws IOException {
        connect(out);
    }

    /**
     * Returns the number of bytes that are available before this stream will
     * block. This implementation returns the number of bytes written to this
     * pipe that have not been read yet.
     * 
     * @return the number of bytes available before blocking.
     * @throws IOException
     *             if an error occurs in this stream.
     * @since Android 1.0
     */
    @Override
    public synchronized int available() throws IOException {
        if (buffer == null || in == -1) {
            return 0;
        }
        return in <= out ? buffer.length - out + in : in - out;
    }

    /**
     * Closes this stream. This implementation releases the buffer used for the
     * pipe and notifies all threads waiting to read or write.
     * 
     * @throws IOException
     *             if an error occurs while closing this stream.
     * @since Android 1.0
     */
    @Override
    public void close() throws IOException {
        synchronized (this) {
            /* No exception thrown if already closed */
            if (buffer != null) {
                /* Release buffer to indicate closed. */
                buffer = null;
            }
        }
    }

    /**
     * Connects this {@code PipedInputStream} to a {@link PipedOutputStream}.
     * Any data written to the output stream becomes readable in this input
     * stream.
     * 
     * @param src
     *            the source output stream.
     * @throws IOException
     *             if either stream is already connected.
     * @since Android 1.0
     */
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of this stream has been
     * reached. If there is no data in the pipe, this method blocks until data
     * is available, the end of the stream is detected or an exception is
     * thrown.
     * <p>
     * Separate threads should be used to read from a {@code PipedInputStream}
     * and to write to the connected {@link PipedOutputStream}. If the same
     * thread is used, a deadlock may occur.
     * </p>
     * 
     * @return the byte read or -1 if the end of the source stream has been
     *         reached.
     * @throws IOException
     *             if this stream is closed or not connected to an output
     *             stream, or if the thread writing to the connected output
     *             stream is no longer alive.
     * @since Android 1.0
     */
    @Override
    public synchronized int read() throws IOException {
        if (!isConnected) {
            throw new IOException(Msg.getString("K0074")); //$NON-NLS-1$
        }
        if (buffer == null) {
            throw new IOException(Msg.getString("K0075")); //$NON-NLS-1$
        }
        /**
         * Set the last thread to be reading on this PipedInputStream. If
         * lastReader dies while someone is waiting to write an IOException of
         * "Pipe broken" will be thrown in receive()
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

        byte result = buffer[out++];
        if (out == buffer.length) {
            out = 0;
        }
        if (out == in) {
            // empty buffer
            in = -1;
            out = 0;
        }
        return result & 0xff;
    }

    /**
     * Reads at most {@code count} bytes from this stream and stores them in the
     * byte array {@code bytes} starting at {@code offset}. Blocks until at
     * least one byte has been read, the end of the stream is detected or an
     * exception is thrown.
     * <p>
     * Separate threads should be used to read from a {@code PipedInputStream}
     * and to write to the connected {@link PipedOutputStream}. If the same
     * thread is used, a deadlock may occur.
     * </p>
     * 
     * @param bytes
     *            the array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code bytes} to store the bytes
     *            read from this stream.
     * @param count
     *            the maximum number of bytes to store in {@code bytes}.
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is greater than the size of {@code bytes}.
     * @throws InterruptedIOException
     *             if the thread reading from this stream is interrupted.
     * @throws IOException
     *             if this stream is closed or not connected to an output
     *             stream, or if the thread writing to the connected output
     *             stream is no longer alive.
     * @throws NullPointerException
     *             if {@code bytes} is {@code null}.
     */
    @Override
    public synchronized int read(byte[] bytes, int offset, int count)
            throws IOException {
        // BEGIN android-changed
        if (bytes == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }

        // Exception priorities (in case of multiple errors) differ from
        // RI, but are spec-compliant.
        // removed redundant check, used (offset | count) < 0
        // instead of (offset < 0) || (count < 0) to safe one operation
        if ((offset | count) < 0 || count > bytes.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        // END android-changed

        if (count == 0) {
            return 0;
        }

        if (!isConnected) {
            throw new IOException(Msg.getString("K0074")); //$NON-NLS-1$
        }

        if (buffer == null) {
            throw new IOException(Msg.getString("K0075")); //$NON-NLS-1$
        }

        /**
         * Set the last thread to be reading on this PipedInputStream. If
         * lastReader dies while someone is waiting to write an IOException of
         * "Pipe broken" will be thrown in receive()
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
        /* Copy bytes from out to end of buffer first */
        if (out >= in) {
            copyLength = count > (buffer.length - out) ? buffer.length - out
                    : count;
            System.arraycopy(buffer, out, bytes, offset, copyLength);
            out += copyLength;
            if (out == buffer.length) {
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

        int bytesCopied = copyLength;
        /* Copy bytes from 0 to the number of available bytes */
        copyLength = in - out > (count - bytesCopied) ? count - bytesCopied
                : in - out;
        System.arraycopy(buffer, out, bytes, offset + bytesCopied, copyLength);
        out += copyLength;
        if (out == in) {
            // empty buffer
            in = -1;
            out = 0;
        }
        return bytesCopied + copyLength;
    }

    /**
     * Receives a byte and stores it in this stream's {@code buffer}. This
     * method is called by {@link PipedOutputStream#write(int)}. The least
     * significant byte of the integer {@code oneByte} is stored at index
     * {@code in} in the {@code buffer}.
     * <p>
     * This method blocks as long as {@code buffer} is full.
     * </p>
     * 
     * @param oneByte
     *            the byte to store in this pipe.
     * @throws InterruptedIOException
     *             if the {@code buffer} is full and the thread that has called
     *             this method is interrupted.
     * @throws IOException
     *             if this stream is closed or the thread that has last read
     *             from this stream is no longer alive.
     */
    protected synchronized void receive(int oneByte) throws IOException {
        if (buffer == null || isClosed) {
            throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
        }
        if (lastReader != null && !lastReader.isAlive()) {
            throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
        }
        /**
         * Set the last thread to be writing on this PipedInputStream. If
         * lastWriter dies while someone is waiting to read an IOException of
         * "Pipe broken" will be thrown in read()
         */
        lastWriter = Thread.currentThread();
        try {
            while (buffer != null && out == in) {
                notifyAll();
                wait(1000);
                if (lastReader != null && !lastReader.isAlive()) {
                    throw new IOException(Msg.getString("K0076")); //$NON-NLS-1$
                }
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
        if (buffer != null) {
            if (in == -1) {
                in = 0;
            }
            buffer[in++] = (byte) oneByte;
            if (in == buffer.length) {
                in = 0;
            }
            return;
        }
    }

    synchronized void done() {
        isClosed = true;
        notifyAll();
    }
}

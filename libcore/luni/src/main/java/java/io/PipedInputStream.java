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
 * PipedInputStream is a class which receives information on a communications
 * pipe. When two threads want to pass data back and forth, one creates a piped
 * output stream and the other creates a piped input stream.
 * 
 * @see PipedOutputStream
 */
public class PipedInputStream extends InputStream {

    private Thread lastReader, lastWriter;

    private boolean isClosed = false;

    /**
     * The circular buffer through which data is passed.
     */
    protected byte buffer[];

    /**
     * The index in <code>buffer</code> where the next byte will be written.
     */
    protected int in = -1;

    /**
     * The index in <code>buffer</code> where the next byte will be read.
     */
    protected int out = 0;

    /**
     * The size of the default pipe in bytes
     */
    protected static final int PIPE_SIZE = 1024;

    /**
     * Indicates if this pipe is connected
     */
    boolean isConnected = false;

    /**
     * Constructs a new unconnected PipedInputStream. The resulting Stream must
     * be connected to a PipedOutputStream before data may be read from it.
     * 
     */
    public PipedInputStream() {
        /* empty */
    }

    /**
     * Constructs a new PipedInputStream connected to the PipedOutputStream
     * <code>out</code>. Any data written to the output stream can be read
     * from the this input stream.
     * 
     * @param out
     *            the PipedOutputStream to connect to.
     * 
     * @throws IOException
     *             if this or <code>out</code> are already connected.
     */
    public PipedInputStream(PipedOutputStream out) throws IOException {
        connect(out);
    }

    /**
     * Returns a int representing the number of bytes that are available before
     * this PipedInputStream will block. This method returns the number of bytes
     * written to the pipe but not read yet up to the size of the pipe.
     * 
     * @return int the number of bytes available before blocking.
     * 
     * @throws IOException
     *             If an error occurs in this stream.
     */
    @Override
    public synchronized int available() throws IOException {
        if (buffer == null || in == -1) {
            return 0;
        }
        return in <= out ? buffer.length - out + in : in - out;
    }

    /**
     * Close this PipedInputStream. This implementation releases the buffer used
     * for the pipe and notifies all threads waiting to read or write.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this stream.
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
     * Connects this PipedInputStream to a PipedOutputStream. Any data written
     * to the OutputStream becomes readable in this InputStream.
     * 
     * @param src
     *            the source PipedOutputStream.
     * 
     * @throws IOException
     *             If either stream is already connected.
     */
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    /**
     * Reads a single byte from this PipedInputStream and returns the result as
     * an int. The low-order byte is returned or -1 of the end of stream was
     * encountered. If there is no data in the pipe, this method blocks until
     * there is data available. Separate threads should be used for the reader
     * of the PipedInputStream and the PipedOutputStream. There may be
     * undesirable results if more than one Thread interacts a input or output
     * pipe.
     * 
     * @return int The byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
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
     * Reads at most <code>count</code> bytes from this PipedInputStream and
     * stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered. Separate threads
     * should be used for the reader of the PipedInputStream and the
     * PipedOutputStream. There may be undesirable results if more than one
     * Thread interacts a input or output pipe.
     * 
     * @param bytes
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param count
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    @Override
    public synchronized int read(byte[] bytes, int offset, int count)
            throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }

        if (offset < 0 || offset > bytes.length || count < 0
                || count > bytes.length - offset) {
            throw new IndexOutOfBoundsException();
        }

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
     * Receives a byte and stores it into the PipedInputStream. This called by
     * PipedOutputStream.write() when writes occur. The lowest-order byte is
     * stored at index <code>in</code> in the <code>buffer</code>.
     * <P>
     * If the buffer is full and the thread sending #receive is interrupted, the
     * InterruptedIOException will be thrown.
     * 
     * @param oneByte
     *            the byte to store into the pipe.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
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

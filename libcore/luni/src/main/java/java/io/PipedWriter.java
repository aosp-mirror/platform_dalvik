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
 * PipedWriter is a class which places information on a communications pipe.
 * When two threads want to pass data back and forth, one creates a piped writer
 * and the other creates a piped reader.
 * 
 * @see PipedReader
 */
public class PipedWriter extends Writer {
    /**
     * The destination PipedReader
     */
    private PipedReader dest;

    private boolean closed;

    /**
     * Constructs a new unconnected PipedWriter. The resulting Stream must be
     * connected to a PipedReader before data may be written to it.
     */
    public PipedWriter() {
        super();
    }

    /**
     * Constructs a new PipedWriter connected to the PipedReader
     * <code>dest</code>. Any data written to this Writer can be read from
     * the <code>dest</code>.
     * 
     * @param dest
     *            the PipedReader to connect to.
     * 
     * @throws java.io.IOException
     *             if <code>dest</code> is already connected.
     */
    public PipedWriter(PipedReader dest) throws IOException {
        super(dest);
        connect(dest);
    }

    /**
     * Close this PipedWriter. Any data buffered in the corresponding
     * PipedReader can be read, then -1 will be returned to the reader. If this
     * Writer is not connected, this method does nothing.
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to close this PipedWriter.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            /* Is the pipe connected? */
            if (dest != null) {
                dest.done();
                dest = null;
            }
            closed = true;
        }
    }

    /**
     * Connects this PipedWriter to a PipedReader. Any data written to this
     * Writer becomes readable in the Reader.
     * 
     * @param stream
     *            the destination PipedReader.
     * 
     * @throws java.io.IOException
     *             If this Writer or the dest is already connected.
     */
    public void connect(PipedReader stream) throws IOException {
        synchronized (lock) {
            if (this.dest != null) {
                throw new IOException(Msg.getString("K0079")); //$NON-NLS-1$
            }
            if (closed) {
                throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
            }
            stream.establishConnection();
            this.dest = stream;
        }
    }

    /**
     * Notifies the readers on the PipedReader that characters can be read. This
     * method does nothing if this Writer is not connected.
     * 
     * @throws java.io.IOException
     *             If an IO error occurs during the flush.
     */
    @Override
    public void flush() throws IOException {
        if (dest != null) {
            dest.flush();
        }
    }

    /**
     * Writes <code>count</code> <code>chars</code> from the char array
     * <code>buffer</code> starting at offset <code>index</code> to this
     * PipedWriter. The written data can now be read from the destination
     * PipedReader. Separate threads should be used for the reader of the
     * PipedReader and the PipedWriter. There may be undesirable results if more
     * than one Thread interacts a input or output pipe.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get chars
     * @param count
     *            number of chars in buffer to write
     * 
     * @throws java.io.IOException
     *             If the receiving thread was terminated without closing the
     *             pipe. This case is not currently handled correctly.
     * @throws java.io.InterruptedIOException
     *             If the pipe is full and the current thread is interrupted
     *             waiting for space to write data. This case is not currently
     *             handled correctly.
     * @throws java.lang.NullPointerException
     *             If the receiver has not been connected yet.
     * @throws java.lang.IllegalArgumentException
     *             If any of the arguments are out of bounds.
     */
    @Override
    public void write(char buffer[], int offset, int count) throws IOException {
        synchronized (lock) {
            if (closed) {
                throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
            }
            if (dest == null) {
                throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
            }
            if (buffer == null) {
                throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
            }

            // avoid int overflow
            if (offset < 0 || offset > buffer.length || count < 0
                    || count > buffer.length - offset) {
                throw new IndexOutOfBoundsException();
            }
            dest.receive(buffer, offset, count);
        }
    }

    /**
     * Writes the character <code>c</code> to this PipedWriter. The written
     * data can now be read from the destination PipedReader. Separate threads
     * should be used for the reader of the PipedReader and the PipedWriter.
     * There may be undesirable results if more than one Thread interacts a
     * input or output pipe.
     * 
     * @param c
     *            the character to be written
     * 
     * @throws java.io.IOException
     *             If the receiving thread was terminated without closing the
     *             pipe. This case is not currently handled correctly.
     * @throws java.io.InterruptedIOException
     *             If the pipe is full and the current thread is interrupted
     *             waiting for space to write data. This case is not currently
     *             handled correctly.
     * @throws java.lang.NullPointerException
     *             If the receiver has not been connected yet.
     */
    @Override
    public void write(int c) throws IOException {
        synchronized (lock) {
            if (closed) {
                throw new IOException(Msg.getString("K0078")); //$NON-NLS-1$
            }
            if (dest == null) {
                throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
            }
            dest.receive((char) c);
        }
    }
}

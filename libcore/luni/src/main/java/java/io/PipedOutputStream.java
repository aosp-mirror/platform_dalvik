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
 * PipedOutputStream is a class which places information on a communications
 * pipe. When two threads want to pass data back and forth, one creates a piped
 * output stream and the other creates a piped input stream.
 * 
 * @see PipedInputStream
 */
public class PipedOutputStream extends OutputStream {

    /**
     * The destination PipedInputStream
     */
    private PipedInputStream dest;

    /**
     * Constructs a new unconnected PipedOutputStream. The resulting Stream must
     * be connected to a PipedInputStream before data may be written to it.
     */
    public PipedOutputStream() {
        super();
    }

    /**
     * Constructs a new PipedOutputStream connected to the PipedInputStream
     * <code>dest</code>. Any data written to this stream can be read from
     * the <code>dest</code>.
     * 
     * @param dest
     *            the PipedInputStream to connect to.
     * 
     * @throws IOException
     *             if <code>dest</code> is already connected.
     */
    public PipedOutputStream(PipedInputStream dest) throws IOException {
        super();
        connect(dest);
    }

    /**
     * Close this PipedOutputStream. Any data buffered in the corresponding
     * PipedInputStream can be read, then -1 will be returned to the reader. If
     * this OutputStream is not connected, this method does nothing.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this
     *             PipedOutputStream.
     */
    @Override
    public void close() throws IOException {
        // Is the pipe connected?
        if (dest != null) {
            dest.done();
            dest = null;
        }
    }

    /**
     * Connects this PipedOutputStream to a PipedInputStream. Any data written
     * to this OutputStream becomes readable in the InputStream.
     * 
     * @param stream
     *            the destination PipedInputStream.
     * 
     * @throws IOException
     *             If this Stream or the dest is already connected.
     */
    public void connect(PipedInputStream stream) throws IOException {
        if (null == stream) {
            throw new NullPointerException();
        }
        if (this.dest != null) {
            throw new IOException(Msg.getString("K0079")); //$NON-NLS-1$
        }
        synchronized (stream) {
            if (stream.isConnected) {
                throw new IOException(Msg.getString("K007a")); //$NON-NLS-1$
            }
            stream.buffer = new byte[PipedInputStream.PIPE_SIZE];
            stream.isConnected = true;
            this.dest = stream;
        }
    }

    /**
     * Notifies the readers on the PipedInputStream that bytes can be read. This
     * method does nothing if this Stream is not connected.
     * 
     * @throws IOException
     *             If an IO error occurs during the flush.
     */
    @Override
    public void flush() throws IOException {
        if (dest != null) {
            synchronized (dest) {
                dest.notifyAll();
            }
        }
    }

    /**
     * Writes <code>count</code> <code>bytes</code> from this byte array
     * <code>buffer</code> starting at offset <code>index</code> to this
     * PipedOutputStream. The written data can now be read from the destination
     * PipedInputStream. Separate threads should be used for the reader of the
     * PipedInputStream and the PipedOutputStream. There may be undesirable
     * results if more than one Thread interacts a input or output pipe.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param count
     *            number of bytes in buffer to write
     * 
     * @throws IOException
     *             If the receiving thread was terminated without closing the
     *             pipe. This case is not currently handled correctly.
     * @throws InterruptedIOException
     *             If the pipe is full and the current thread is interrupted
     *             waiting for space to write data. This case is not currently
     *             handled correctly.
     * @throws NullPointerException
     *             If the receiver has not been connected yet.
     * @throws IllegalArgumentException
     *             If any of the arguments are out of bounds.
     */
    @Override
    public void write(byte buffer[], int offset, int count) throws IOException {
        if (dest == null) {
            // K007b=Pipe Not Connected
            throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
        }
        super.write(buffer, offset, count);
    }

    /**
     * Writes the specified byte <code>oneByte</code> to this
     * PipedOutputStream. Only the low order byte of <code>oneByte</code> is
     * written. The data can now be read from the destination PipedInputStream.
     * Separate threads should be used for the reader of the PipedInputStream
     * and the PipedOutputStream. There may be undesirable results if more than
     * one Thread interacts a input or output pipe.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If the receiving thread was terminated without closing the
     *             pipe. This case is not currently handled correctly.
     * @throws InterruptedIOException
     *             If the pipe is full and the current thread is interrupted
     *             waiting for space to write data. This case is not currently
     *             handled correctly.
     * @throws NullPointerException
     *             If the receiver has not been connected yet.
     */
    @Override
    public void write(int oneByte) throws IOException {
        if (dest == null) {
            throw new IOException(Msg.getString("K007b")); //$NON-NLS-1$
        }
        dest.receive(oneByte);
    }
}

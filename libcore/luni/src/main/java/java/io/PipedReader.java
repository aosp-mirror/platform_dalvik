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
 * PipedReader is a class which receives information on a communications pipe.
 * When two threads want to pass data back and forth, one creates a piped writer
 * and the other creates a piped reader.
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
     * The index in <code>buffer</code> where the next character will be
     * written.
     */
    private int in = -1;

    /**
     * The index in <code>buffer</code> where the next character will be read.
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
     * Constructs a new unconnected PipedReader. The resulting Reader must be
     * connected to a PipedWriter before data may be read from it.
     */
    public PipedReader() {
        data = new char[PIPE_SIZE];
    }

    /**
     * Constructs a new PipedReader connected to the PipedWriter
     * <code>out</code>. Any data written to the writer can be read from the
     * this reader.
     * 
     * @param out
     *            the PipedWriter to connect to.
     * 
     * @throws IOException
     *             if this or <code>out</code> are already connected.
     */
    public PipedReader(PipedWriter out) throws IOException {
        this();
        connect(out);
    }

    /**
     * Close this PipedReader. This implementation releases the buffer used for
     * the pipe and notifies all threads waiting to read or write.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this reader.
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
     * Connects this PipedReader to a PipedWriter. Any data written to the
     * Writer becomes available in this Reader.
     * 
     * @param src
     *            the source PipedWriter.
     * 
     * @throws IOException
     *             If either Writer or Reader is already connected.
     */
    public void connect(PipedWriter src) throws IOException {
        synchronized (lock) {
            src.connect(this);
        }
    }

    /**
     * Establish the connection to the PipedWriter.
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
     * Reads the next character from this Reader. Answer the character actually
     * read or -1 if no character was read and end of reader was encountered.
     * Separate threads should be used for the reader of the PipedReader and the
     * PipedWriter. There may be undesirable results if more than one Thread
     * interacts a reader or writer pipe.
     * 
     * @return int the character read -1 if end of reader.
     * 
     * @throws IOException
     *             If the reader is already closed or another IOException
     *             occurs.
     */
    @Override
    public int read() throws IOException {
        char[] carray = new char[1];
        int result = read(carray, 0, 1);
        return result != -1 ? carray[0] : result;
    }

    /**
     * Reads at most <code>count</code> character from this PipedReader and
     * stores them in char array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of characters actually read or
     * -1 if no characters were read and end of stream was encountered. Separate
     * threads should be used for the reader of the PipedReader and the
     * PipedWriter. There may be undesirable results if more than one Thread
     * interacts a reader or writer pipe.
     * 
     * @param buffer
     *            the character array in which to store the read characters.
     * @param offset
     *            the offset in <code>buffer</code> to store the read
     *            characters.
     * @param count
     *            the maximum number of characters to store in
     *            <code>buffer</code>.
     * @return int the number of characters actually read or -1 if end of
     *         reader.
     * 
     * @throws IOException
     *             If the reader is already closed or another IOException
     *             occurs.
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
            if (offset < 0 || count > buffer.length - offset || count < 0) {
                throw new IndexOutOfBoundsException();
            }
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
     * Answer a boolean indicating whether or not this Reader is ready to be
     * read. Returns true if the buffer contains characters to be read.
     * 
     * @return boolean <code>true</code> if there are characters ready,
     *         <code>false</code> otherwise.
     * 
     * @throws IOException
     *             If the reader is already closed or another IOException
     *             occurs.
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
                    wait(1000);
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
                        wait(1000);
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

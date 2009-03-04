/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.zip;


import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.harmony.archive.internal.nls.Messages;

/**
 * This class provides an implementation of {@code FilterInputStream} that
 * uncompresses data that was compressed using the <i>DEFLATE</i> algorithm
 * (see <a href="http://www.gzip.org/algorithm.txt">specification</a>).
 * Basically it wraps the {@code Inflater} class and takes care of the
 * buffering.
 * 
 * @see Inflater
 * @see DeflaterOutputStream
 * @since Android 1.0
 */
public class InflaterInputStream extends FilterInputStream {

    /**
     * The inflater used for this stream.
     * 
     * @since Android 1.0
     */
    protected Inflater inf;

    /**
     * The input buffer used for decompression.
     * 
     * @since Android 1.0
     */
    protected byte[] buf;

    /**
     * The length of the buffer.
     * 
     * @since Android 1.0
     */
    protected int len;

    boolean closed;

    boolean eof;

    static final int BUF_SIZE = 512;

    // BEGIN android-added
    int nativeEndBufSize = 0;
    // END android-added

    /**
     * This is the most basic constructor. You only need to pass the {@code
     * InputStream} from which the compressed data is to be read from. Default
     * settings for the {@code Inflater} and internal buffer are be used. In
     * particular the Inflater expects a ZLIB header from the input stream.
     * 
     * @param is
     *            the {@code InputStream} to read data from.
     * @since Android 1.0
     */
    public InflaterInputStream(InputStream is) {
        this(is, new Inflater(), BUF_SIZE);
    }

    /**
     * This constructor lets you pass a specifically initialized Inflater,
     * for example one that expects no ZLIB header.
     * 
     * @param is
     *            the {@code InputStream} to read data from.
     * @param inf
     *            the specific {@code Inflater} for uncompressing data.
     * @since Android 1.0 
     */
    public InflaterInputStream(InputStream is, Inflater inf) {
        this(is, inf, BUF_SIZE);
    }

    /**
     * This constructor lets you specify both the {@code Inflater} as well as
     * the internal buffer size to be used.
     * 
     * @param is
     *            the {@code InputStream} to read data from.
     * @param inf
     *            the specific {@code Inflater} for uncompressing data.
     * @param bsize
     *            the size to be used for the internal buffer.
     * @since Android 1.0
     */
    public InflaterInputStream(InputStream is, Inflater inf, int bsize) {
        super(is);
        if (is == null || inf == null) {
            throw new NullPointerException();
        }
        if (bsize <= 0) {
            throw new IllegalArgumentException();
        }
        this.inf = inf;
        // BEGIN android-changed
        if (is instanceof ZipFile.RAFStream) {
            nativeEndBufSize = bsize;
        } else {
            buf = new byte[bsize];
        }
        // END android-changed
    }

    /**
     * Reads a single byte of decompressed data.
     * 
     * @return the byte read.
     * @throws IOException
     *             if an error occurs reading the byte.
     * @since Android 1.0
     */
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) == -1) {
            return -1;
        }
        return b[0] & 0xff;
    }

    /**
     * Reads up to {@code nbytes} of decompressed data and stores it in
     * {@code buffer} starting at {@code off}.
     * 
     * @param buffer
     *            the buffer to write data to.
     * @param off
     *            offset in buffer to start writing.
     * @param nbytes
     *            number of bytes to read.
     * @return Number of uncompressed bytes read
     * @throws IOException
     *             if an IOException occurs.
     * @since Android 1.0
     */
    @Override
    public int read(byte[] buffer, int off, int nbytes) throws IOException {
        /* archive.1E=Stream is closed */
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); //$NON-NLS-1$
        }

        if (null == buffer) {
            throw new NullPointerException();
        }

        if (off < 0 || nbytes < 0 || off + nbytes > buffer.length) {
            throw new IndexOutOfBoundsException();
        }

        if (nbytes == 0) {
            return 0;
        }

        if (inf.finished()) {
            eof = true;
            return -1;
        }

        // avoid int overflow, check null buffer
        if (off <= buffer.length && nbytes >= 0 && off >= 0
                && buffer.length - off >= nbytes) {
            do {
                if (inf.needsInput()) {
                    fill();
                }
                int result;
                try {
                    result = inf.inflate(buffer, off, nbytes);
                } catch (DataFormatException e) {
                    if (len == -1) {
                        throw new EOFException();
                    }
                    throw (IOException)(new IOException().initCause(e));
                }
                if (result > 0) {
                    return result;
                } else if (inf.finished()) {
                    eof = true;
                    return -1;
                } else if (inf.needsDictionary()) {
                    return -1;
                } else if (len == -1) {
                    throw new EOFException();
                // If result == 0, fill() and try again
                }
            } while (true);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    
    /**
     * Fills the input buffer with data to be decompressed.
     * 
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    protected void fill() throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); //$NON-NLS-1$
        }
        // BEGIN android-changed
        if (nativeEndBufSize > 0) {
            ZipFile.RAFStream is = (ZipFile.RAFStream)in;
            synchronized (is.mSharedRaf) {
                long len = is.mLength - is.mOffset;
                if (len > nativeEndBufSize) len = nativeEndBufSize;
                int cnt = inf.setFileInput(is.mSharedRaf.getFD(), is.mOffset, (int)nativeEndBufSize);
                is.skip(cnt);
            }
        } else {
            if ((len = in.read(buf)) > 0) {
                inf.setInput(buf, 0, len);
            }
        }
        // END android-changed
    }

    /**
     * Skips up to n bytes of uncompressed data.
     * 
     * @param nbytes
     *            the number of bytes to skip.
     * @return the number of uncompressed bytes skipped.
     * @throws IOException
     *             if an error occurs skipping.
     * @since Android 1.0
     */
    @Override
    public long skip(long nbytes) throws IOException {
        if (nbytes >= 0) {
            long count = 0, rem = 0;
            while (count < nbytes) {
                int x = read(buf, 0,
                        (rem = nbytes - count) > buf.length ? buf.length
                                : (int) rem);
                if (x == -1) {
                    eof = true;
                    return count;
                }
                count += x;
            }
            return count;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns whether data can be read from this stream.
     * 
     * @return 0 if this stream has been closed, 1 otherwise.
     * @throws IOException
     *             If an error occurs.
     * @since Android 1.0
     */
    @Override
    public int available() throws IOException {
        if (closed) {
            // archive.1E=Stream is closed
            throw new IOException(Messages.getString("archive.1E")); //$NON-NLS-1$
        }
        if (eof) {
            return 0;
        }
        return 1;
    }

    /**
     * Closes the input stream.
     * 
     * @throws IOException
     *             If an error occurs closing the input stream.
     * @since Android 1.0
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            inf.end();
            closed = true;
            eof = true;
            super.close();
        }
    }
    
    /**
     * This implementation overrides the super type implementation to do nothing
     * at all.
     * 
     * @param readlimit
     *            of no use.
     * @since Android 1.0
     */
    @Override
    @SuppressWarnings("unused")
    public void mark(int readlimit) {
        // do nothing
    }

    /**
     * Reset the position of the stream to the last marked position. This
     * implementation overrides the supertype implementation and always throws
     * an {@link IOException IOException} when called.
     * 
     * @throws IOException
     *             if the method is called
     * @since Android 1.0
     */
    @Override
    public void reset() throws IOException{
        throw new IOException();
    }
    
    /**
     * Returns whether the receiver implements {@code mark} semantics. This type
     * does not support {@code mark()}, so always responds {@code false}.
     * 
     * @return false, always
     * @since Android 1.0
     */
    @Override
    public boolean markSupported() {
        return false;
    }

}

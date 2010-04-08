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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@code InputStream} filter to compress data. Callers read
 * compressed data in the "deflate" format from the uncompressed
 * underlying stream.
 * @since 1.6
 * @hide
 */
public class DeflaterInputStream extends FilterInputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    protected final Deflater deflater;
    protected final byte[] buf;

    private boolean closed = false;
    private boolean available = true;

    /**
     * Constructs a {@code DeflaterInputStream} with a new {@code Deflater} and an
     * implementation-defined default internal buffer size. {@code in} is a source of
     * uncompressed data, and this stream will be a source of compressed data.
     * 
     * @param in the source {@code InputStream}
     */
    public DeflaterInputStream(InputStream in) {
        this(in, new Deflater(), DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a {@code DeflaterInputStream} with the given {@code Deflater} and an
     * implementation-defined default internal buffer size. {@code in} is a source of
     * uncompressed data, and this stream will be a source of compressed data.
     * 
     * @param in the source {@code InputStream}
     * @param deflater the {@code Deflater} to be used for compression
     */
    public DeflaterInputStream(InputStream in, Deflater deflater) {
        this(in, deflater, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a {@code DeflaterInputStream} with the given {@code Deflater} and
     * given internal buffer size. {@code in} is a source of
     * uncompressed data, and this stream will be a source of compressed data.
     * 
     * @param in the source {@code InputStream}
     * @param deflater the {@code Deflater} to be used for compression
     * @param bufferSize the length in bytes of the internal buffer
     */
    public DeflaterInputStream(InputStream in, Deflater deflater, int bufferSize) {
        super(in);
        if (in == null || deflater == null) {
            throw new NullPointerException();
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.deflater = deflater;
        this.buf = new byte[bufferSize];
    }

    /**
     * Closes the underlying input stream and discards any remaining uncompressed
     * data.
     */
    @Override
    public void close() throws IOException {
        closed = true;
        deflater.end();
        in.close();
    }

    /**
     * Reads a byte from the compressed input stream. The result will be a byte of compressed
     * data corresponding to an uncompressed byte or bytes read from the underlying stream.
     * 
     * @return the byte or -1 if the end of the stream has been reached.
     */
    @Override
    public int read() throws IOException {
        byte[] result = new byte[1];
        if (read(result, 0, 1) == -1) {
            return -1;
        }
        return result[0] & 0xff;
    }

    /**
     * Reads compressed data into a byte buffer. The result will be bytes of compressed
     * data corresponding to an uncompressed byte or bytes read from the underlying stream.
     * 
     * @param b
     *            the byte buffer that compressed data will be read into.
     * @param off
     *            the offset in the byte buffer where compressed data will start
     *            to be read into.
     * @param len
     *            the length of the compressed data that is expected to read.
     * @return the number of bytes read or -1 if the end of the compressed input
     *         stream has been reached.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }

        if (!available) {
            return -1;
        }

        int count = 0;
        while (count < len && !deflater.finished()) {
            if (deflater.needsInput()) {
                // read data from input stream
                int byteCount = in.read(buf);
                if (byteCount == -1) {
                    deflater.finish();
                } else {
                    deflater.setInput(buf, 0, byteCount);
                }
            }
            int byteCount = deflater.deflate(buf, 0, Math.min(buf.length, len - count));
            if (byteCount == -1) {
                break;
            }
            System.arraycopy(buf, 0, b, off + count, byteCount);
            count += byteCount;
        }
        if (count == 0) {
            count = -1;
            available = false;
        }
        return count;
    }

    /**
     * {@inheritDoc}
     * <p>Note: if {@code n > Integer.MAX_VALUE}, this stream will only attempt to
     * skip {@code Integer.MAX_VALUE} bytes.
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        if (n > Integer.MAX_VALUE) {
            n = Integer.MAX_VALUE;
        }
        checkClosed();

        int remaining = (int) n;
        byte[] tmp = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
        while (remaining > 0) {
            int count = read(tmp, 0, Math.min(remaining, tmp.length));
            if (count == -1) {
                break;
            }
            remaining -= count;
        }
        return n - remaining;
    }

    /**
     * Returns 0 when when this stream has exhausted its input; and 1 otherwise.
     * A result of 1 does not guarantee that further bytes can be returned,
     * with or without blocking.
     * 
     * <p>Although consistent with the RI, this behavior is inconsistent with
     * {@link InputStream#available()}, and violates the <a
     * href="http://en.wikipedia.org/wiki/Liskov_substitution_principle">Liskov
     * Substitution Principle</a>. This method should not be used.
     * 
     * @return 0 if no further bytes are available. Otherwise returns 1,
     *         which suggests (but does not guarantee) that additional bytes are
     *         available.
     * @throws IOException if this stream is closed or an error occurs
     */
    @Override
    public int available() throws IOException {
        checkClosed();
        return available ? 1 : 0;
    }

    /**
     * Returns false because {@code DeflaterInputStream} does not support
     * {@code mark}/{@code reset}.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * This operation is not supported and does nothing.
     */
    @Override
    public void mark(int limit) {
    }

    /**
     * This operation is not supported and throws {@code IOException}.
     */
    @Override
    public void reset() throws IOException {
        throw new IOException();
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
    }
}

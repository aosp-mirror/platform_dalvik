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

package java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.harmony.nio.internal.IOUtil;

/**
 * This class provides several utilities to get I/O streams from channels.
 * 
 * @since Android 1.0
 */
public final class Channels {

    // -------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------
    /*
     * Not intended to be instantiated.
     */
    private Channels() {
        super();
    }

    // -------------------------------------------------------------------
    // Public Methods
    // -------------------------------------------------------------------

    /**
     * Returns an input stream on the given channel. The resulting stream has
     * the following properties:
     * <ul>
     * <li>If the stream is closed, then the underlying channel is closed as
     * well.</li>
     * <li>It is thread safe.</li>
     * <li>It throws an {@link IllegalBlockingModeException} if the channel is
     * in non-blocking mode and {@code read} is called.</li>
     * <li>Neither {@code mark} nor {@code reset} is supported.</li>
     * <li>It is not buffered.</li>
     * </ul>
     * 
     * @param channel
     *            the channel to be wrapped by an InputStream.
     * @return an InputStream that takes bytes from the given byte channel.
     * @since Android 1.0
     */
    public static InputStream newInputStream(ReadableByteChannel channel) {
        return new ReadableByteChannelInputStream(channel);
    }

    /**
     * Returns an output stream on the given channel. The resulting stream has
     * the following properties:
     * <ul>
     * <li>If the stream is closed, then the underlying channel is closed as
     * well.</li>
     * <li>It is thread safe.</li>
     * <li>It throws an {@link IllegalBlockingModeException} if the channel is
     * in non-blocking mode and {@code write} is called.</li>
     * <li>It is not buffered.</li>
     * </ul>
     * 
     * @param channel
     *            the channel to be wrapped by an OutputStream.
     * @return an OutputStream that puts bytes onto the given byte channel.
     * @since Android 1.0
     */
    public static OutputStream newOutputStream(WritableByteChannel channel) {
        return new WritableByteChannelOutputStream(channel);
    }

    /**
     * Returns a readable channel on the given input stream. The resulting
     * channel has the following properties:
     * <ul>
     * <li>If the channel is closed, then the underlying stream is closed as
     * well.</li>
     * <li>It is not buffered.</li>
     * </ul>
     * 
     * @param inputStream
     *            the stream to be wrapped by a byte channel.
     * @return a byte channel that reads bytes from the input stream.
     * @since Android 1.0
     */
    public static ReadableByteChannel newChannel(InputStream inputStream) {
        return new ReadableByteChannelImpl(inputStream);
    }

    /**
     * Returns a writable channel on the given output stream.
     * 
     * The resulting channel has following properties:
     * <ul>
     * <li>If the channel is closed, then the underlying stream is closed as
     * well.</li>
     * <li>It is not buffered.</li>
     * </ul>
     * 
     * @param outputStream
     *            the stream to be wrapped by a byte channel.
     * @return a byte channel that writes bytes to the output stream.
     * @since Android 1.0
     */
    public static WritableByteChannel newChannel(OutputStream outputStream) {
        return new WritableByteChannelImpl(outputStream);
    }

    /**
     * Returns a reader that decodes bytes from a channel.
     * 
     * @param channel
     *            the Channel to be read.
     * @param decoder
     *            the Charset decoder to be used.
     * @param minBufferCapacity
     *            The minimum size of the byte buffer, -1 means to use the
     *            default size.
     * @return the reader.
     * @since Android 1.0
     */
    public static Reader newReader(ReadableByteChannel channel,
            CharsetDecoder decoder, int minBufferCapacity) {
        return new ByteChannelReader(
                new ReaderInputStream(channel), decoder,
                minBufferCapacity);
    }

    /**
     * Returns a reader that decodes bytes from a channel. This method creates a
     * reader with a buffer of default size.
     * 
     * @param channel
     *            the Channel to be read.
     * @param charsetName
     *            the name of the charset.
     * @return the reader.
     * @throws java.nio.charset.UnsupportedCharsetException
     *             if the given charset name is not supported.
     * @since Android 1.0
     */
    public static Reader newReader(ReadableByteChannel channel,
            String charsetName) {
        return newReader(channel, Charset.forName(charsetName).newDecoder(), -1);
    }

    /**
     * Returns a writer that encodes characters with the specified
     * {@code encoder} and sends the bytes to the specified channel.
     * 
     * @param channel
     *            the Channel to write to.
     * @param encoder
     *            the CharsetEncoder to be used.
     * @param minBufferCapacity
     *            the minimum size of the byte buffer, -1 means to use the
     *            default size.
     * @return the writer.
     * @since Android 1.0
     */
    public static Writer newWriter(WritableByteChannel channel,
            CharsetEncoder encoder, int minBufferCapacity) {
        return new ByteChannelWriter(new WritableByteChannelOutputStream(
                channel), encoder, minBufferCapacity);
    }

    /**
     * Returns a writer that encodes characters with the specified
     * {@code encoder} and sends the bytes to the specified channel. This method
     * creates a writer with a buffer of default size.
     * 
     * @param channel
     *            the Channel to be written to.
     * @param charsetName
     *            the name of the charset.
     * @return the writer.
     * @throws java.nio.charset.UnsupportedCharsetException
     *             if the given charset name is not supported.
     * @since Android 1.0
     */
    public static Writer newWriter(WritableByteChannel channel,
            String charsetName) {
        return newWriter(channel, Charset.forName(charsetName).newEncoder(), -1);
    }

    // -------------------------------------------------------------------
    // share routine
    // -------------------------------------------------------------------
    /*
     * wrap a byte array to a ByteBuffer
     */
    static ByteBuffer wrapByteBuffer(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int newLimit = offset + length <= buffer.capacity() ? offset + length
                : buffer.capacity();
        buffer.limit(newLimit);
        buffer.position(offset);
        return buffer;
    }

    // -------------------------------------------------------------------
    // Wrapper classes
    // -------------------------------------------------------------------

    private static class ChannelInputStream extends InputStream {

        protected ReadableByteChannel channel;

        public ChannelInputStream(ReadableByteChannel aChannel) {
            super();
            channel = aChannel;
        }

        /*
         * @see java.io.InputStream#read()
         */
        @Override
        public synchronized int read() throws IOException {
            byte[] oneByte = new byte[1];
            int n = read(oneByte);
            if (n == 1) {
                // reads a single byte 0-255
                return oneByte[0] & 0xff;
            }
            return -1;
        }

        /*
         * @see java.io.InputStream#close()
         */
        @Override
        public synchronized void close() throws IOException {
            channel.close();
        }
    }

    /*
     * Wrapper class used for newInputStream(ReadableByteChannel channel)
     */
    private static class ReadableByteChannelInputStream extends
            ChannelInputStream {

        /*
         * @param someChannel
         */
        public ReadableByteChannelInputStream(ReadableByteChannel aChannel) {
            super(aChannel);
        }

        /*
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public synchronized int read(byte[] target, int offset, int length)
                throws IOException {
            // avoid int overflow, check null target
            if (length + offset > target.length || length < 0 || offset < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (0 == length) {
                return 0;
            }
            if (channel instanceof SelectableChannel) {
                if (!((SelectableChannel) channel).isBlocking()) {
                    throw new IllegalBlockingModeException();
                }
            }
            ByteBuffer buffer = ByteBuffer.wrap(target, offset, length);
            return channel.read(buffer);
        }
    }

    /*
     * Wrapper class used for newReader(ReadableByteChannel channel,
     * CharsetDecoder decoder, int minBufferCapacity)
     */
    private static class ReaderInputStream extends ChannelInputStream {

        /*
         * @param someChannel
         */
        public ReaderInputStream(ReadableByteChannel aChannel) {
            super(aChannel);
        }

        /*
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public synchronized int read(byte[] target, int offset, int length)
                throws IOException {
            // avoid int overflow, check null target
            if (length + offset > target.length || length < 0 || offset < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (0 == length) {
                return 0;
            }
            ByteBuffer buffer = ByteBuffer.wrap(target, offset, length);
            return channel.read(buffer);
        }
    }

    /*
     * Wrapper class used for newOutputStream(WritableByteChannel channel)
     */
    private static class WritableByteChannelOutputStream extends OutputStream {

        private WritableByteChannel channel;

        /*
         * @param someChannel
         */
        public WritableByteChannelOutputStream(WritableByteChannel aChannel) {
            super();
            channel = aChannel;
        }

        /*
         * @see java.io.OutputStream#write(int)
         */
        public synchronized void write(int oneByte) throws IOException {
            byte[] wrappedByte = new byte[1];
            wrappedByte[0] = (byte) oneByte;
            write(wrappedByte);
        }

        /*
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        public synchronized void write(byte[] source, int offset, int length)
                throws IOException {
            // avoid int overflow, check null source
            if (length + offset > source.length || length < 0 || offset < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (0 == length) {
                return;
            }
            if (channel instanceof SelectableChannel) {
                if (!((SelectableChannel) channel).isBlocking()) {
                    throw new IllegalBlockingModeException();
                }
            }
            ByteBuffer buffer = ByteBuffer.wrap(source, offset, length);
            channel.write(buffer);
        }

        /*
         * @see java.io.OutputStream#close()
         */
        public synchronized void close() throws IOException {
            channel.close();
        }
    }

    /*
     * Wrapper class used for newChannel(InputStream inputStream)
     */
    private static class ReadableByteChannelImpl extends
            AbstractInterruptibleChannel implements ReadableByteChannel {
        private InputStream inputStream;

        ReadableByteChannelImpl(InputStream aInputStream) {
            super();
            inputStream = aInputStream;
        }

        /*
         * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
         */
        public synchronized int read(ByteBuffer target) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            int bytesRemain = target.remaining();
            byte[] bytes = new byte[bytesRemain];
            int readCount = 0;
            try {
                begin();
                readCount = inputStream.read(bytes);
            } finally {
                end(readCount >= 0);
            }
            if (readCount > 0) {
                target.put(bytes, 0, readCount);
            }
            return readCount;
        }

        /*
         * @see java.nio.channels.spi.AbstractInterruptibleChannel#implCloseChannel()
         */
        protected void implCloseChannel() throws IOException {
            inputStream.close();
        }
    }

    /*
     * Wrapper class used for newChannel(OutputStream outputStream)
     */
    private static class WritableByteChannelImpl extends
            AbstractInterruptibleChannel implements WritableByteChannel {
        private OutputStream outputStream;

        WritableByteChannelImpl(OutputStream aOutputStream) {
            super();
            outputStream = aOutputStream;
        }

        /*
         * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
         */
        public synchronized int write(ByteBuffer source) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            int bytesRemain = source.remaining();
            if (bytesRemain == 0) {
                return 0;
            }
            byte[] buf = new byte[bytesRemain];
            source.get(buf);
            try {
                begin();
                outputStream.write(buf, 0, bytesRemain);
            } finally {
                end(bytesRemain >= 0);
            }
            return bytesRemain;
        }

        /*
         * @see java.nio.channels.spi.AbstractInterruptibleChannel#implCloseChannel()
         */
        protected void implCloseChannel() throws IOException {
            outputStream.close();
        }
    }

    /*
     * Wrapper class used for newReader(ReadableByteChannel channel,
     * CharsetDecoder decoder, int minBufferCapacity)
     */
    private static class ByteChannelReader extends Reader {

        private InputStream inputStream;

        private static final int BUFFER_SIZE = 8192;

        CharsetDecoder decoder;

        ByteBuffer bytes;

        CharBuffer chars;

        /*
         * @param inputStream @param dec @param minBufferCapacity
         */
        public ByteChannelReader(InputStream aInputStream,
                CharsetDecoder aDecoder, int minBufferCapacity) {
            super(aInputStream);
            aDecoder.reset();
            inputStream = aInputStream;
            int bufferSize = Math.max(minBufferCapacity, BUFFER_SIZE);
            bytes = ByteBuffer.allocate(bufferSize);
            chars = CharBuffer.allocate(bufferSize);
            decoder = aDecoder;
            chars.limit(0);
        }

        /*
         * @see java.io.Reader#close()
         */
        public void close() throws IOException {
            synchronized (lock) {
                decoder = null;
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            }
        }

        /*
         * @see java.io.Reader#ready()
         */
        public boolean ready() {
            synchronized (lock) {
                if (null == inputStream) {
                    return false;
                }
                try {
                    return chars.limit() > chars.position()
                            || inputStream.available() > 0;
                } catch (IOException e) {
                    return false;
                }
            }
        }

        /*
         * @see java.io.Reader#read()
         */
        public int read() throws IOException {
            return IOUtil.readInputStreamReader(inputStream,
                    bytes, chars, decoder, lock);
        }

        /*
         * @see java.io.Reader#read(char[], int, int)
         */
        public int read(char[] buf, int offset, int length) throws IOException {
            return IOUtil.readInputStreamReader(buf, offset,
                    length, inputStream, bytes, chars, decoder, lock);
        }
    }

    /*
     * Wrapper class used for newWriter(WritableByteChannel channel,
     * CharsetEncoder encoder, int minBufferCapacity)
     */
    private static class ByteChannelWriter extends Writer {

        private static final int BUFFER_SIZE = 8192;

        private OutputStream outputStream;

        private CharsetEncoder encoder;

        private ByteBuffer byteBuf;

        /*
         * @param outputStream @param enc @param minBufferCap
         */
        public ByteChannelWriter(OutputStream aOutputStream,
                CharsetEncoder aEncoder, int minBufferCap) {
            super(aOutputStream);
            aEncoder.charset();
            outputStream = aOutputStream;
            byteBuf = ByteBuffer.allocate(Math.max(minBufferCap, BUFFER_SIZE));
            encoder = aEncoder;
        }

        /*
         * @see java.io.Writer#close()
         */
        public void close() throws IOException {
            synchronized (lock) {
                if (encoder != null) {
                    flush();
                    outputStream.flush();
                    outputStream.close();
                    encoder = null;
                    byteBuf = null;
                }
            }
        }

        /*
         * @see java.io.Writer#flush()
         */
        public void flush() throws IOException {
            IOUtil.flushOutputStreamWriter(outputStream,
                    byteBuf, encoder, lock);
        }

        /*
         * @see java.io.Writer#write(char[], int, int)
         */
        public void write(char[] buf, int offset, int count) throws IOException {
            IOUtil.writeOutputStreamWriter(buf, offset, count,
                    outputStream, byteBuf, encoder, lock);
        }

        /*
         * @see java.io.Writer#write(int)
         */
        public void write(int oneChar) throws IOException {
            IOUtil.writeOutputStreamWriter(oneChar,
                    outputStream, byteBuf, encoder, lock);
        }

        /*
         * @see java.io.Writer#write(java.lang.String, int, int)
         */
        public void write(String str, int offset, int count) throws IOException {
            IOUtil.writeOutputStreamWriter(str, offset, count,
                    outputStream, byteBuf, encoder, lock);
        }
    }

}

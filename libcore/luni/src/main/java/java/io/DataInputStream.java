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
import org.apache.harmony.luni.util.Util;

/**
 * Wraps an existing {@link InputStream} and reads typed data from it. 
 * Typically, this stream has been written by a DataOutputStream. Types that can
 * be read include byte, 16-bit short, 32-bit int, 32-bit float, 64-bit long,
 * 64-bit double, byte strings, and strings encoded in
 * {@link DataInput modified UTF-8}.
 * 
 * @see DataOutputStream
 * 
 * @since Android 1.0
 */
public class DataInputStream extends FilterInputStream implements DataInput {

    /**
     * Constructs a new DataInputStream on the InputStream {@code in}. All
     * reads are then filtered through this stream. Note that data read by this
     * stream is not in a human readable format and was most likely created by a
     * DataOutputStream.
     * 
     * @param in
     *            the source InputStream the filter reads from.
     * @see DataOutputStream
     * @see RandomAccessFile
     * @since Android 1.0
     */
    public DataInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads bytes from this stream into the byte array {@code buffer}. Returns
     * the number of bytes that have been read.
     * 
     * @param buffer
     *            the buffer to read bytes into.
     * @return the number of bytes that have been read or -1 if the end of the
     *         stream has been reached.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     * @since Android 1.0
     */
    @Override
    public final int read(byte[] buffer) throws IOException {
        return in.read(buffer, 0, buffer.length);
    }

    /**
     * Reads at most {@code length} bytes from this stream and stores them in
     * the byte array {@code buffer} starting at {@code offset}. Returns the
     * number of bytes that have been read or -1 if no bytes have been read and
     * the end of the stream has been reached.
     * 
     * @param buffer
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes
     *            read from this stream.
     * @param length
     *            the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes that have been read or -1 if the end of the
     *         stream has been reached.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     * @since Android 1.0
     */
    @Override
    public final int read(byte[] buffer, int offset, int length)
            throws IOException {
        return in.read(buffer, offset, length);
    }

    /**
     * Reads a boolean from this stream.
     * 
     * @return the next boolean value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before one byte
     *             has been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeBoolean(boolean)
     * @since Android 1.0
     */
    public final boolean readBoolean() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }

    /**
     * Reads an 8-bit byte value from this stream.
     * 
     * @return the next byte value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before one byte
     *             has been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeByte(int)
     * @since Android 1.0
     */
    public final byte readByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }

    /**
     * Reads a 16-bit character value from this stream.
     * 
     * @return the next char value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before two bytes
     *             have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeChar(int)
     * @since Android 1.0
     */
    public final char readChar() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        }
        return (char) ((b1 << 8) + b2);
    }

    /**
     * Reads a 64-bit double value from this stream.
     * 
     * @return the next double value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before eight
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeDouble(double)
     * @since Android 1.0
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads a 32-bit float value from this stream.
     * 
     * @return the next float value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before four
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeFloat(float)
     * @since Android 1.0
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads bytes from this stream into the byte array {@code buffer}. This
     * method will block until {@code buffer.length} number of bytes have been
     * read.
     * 
     * @param buffer
     *            to read bytes into.
     * @throws EOFException
     *             if the end of the source stream is reached before enough
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     * @since Android 1.0
     */
    public final void readFully(byte[] buffer) throws IOException {
        readFully(buffer, 0, buffer.length);
    }

    /**
     * Reads bytes from this stream and stores them in the byte array {@code
     * buffer} starting at the position {@code offset}. This method blocks until
     * {@code length} bytes have been read. If {@code length} is zero, then this
     * method returns without reading any bytes.
     * 
     * @param buffer
     *            the byte array into which the data is read.
     * @param offset
     *            the offset in {@code buffer} from where to store the bytes
     *            read.
     * @param length
     *            the maximum number of bytes to read.
     * @throws EOFException
     *             if the end of the source stream is reached before enough
     *             bytes have been read.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if {@code
     *             offset + length} is greater than the size of {@code buffer}.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @throws NullPointerException
     *             if {@code buffer} or the source stream are null.
     * @see java.io.DataInput#readFully(byte[], int, int)
     * @since Android 1.0
     */
    public final void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        // BEGIN android-removed
        // if (length < 0) {
        //     throw new IndexOutOfBoundsException();
        // }
        // END android-removed
        if (length == 0) {
            return;
        }
        if (in == null) {
            throw new NullPointerException(Msg.getString("KA00b")); //$NON-NLS-1$
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        // BEGIN android-changed
        // Exception priorities (in case of multiple errors) differ from
        // RI, but are spec-compliant.
        // used (offset | length) < 0 instead of separate (offset < 0) and
        // (length < 0) check to safe one operation
        if ((offset | length) < 0 || offset > buffer.length - length) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        // END android-changed
        while (length > 0) {
            int result = in.read(buffer, offset, length);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            length -= result;
        }
    }

    /**
     * Reads a 32-bit integer value from this stream.
     * 
     * @return the next int value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before four
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeInt(int)
     * @since Android 1.0
     */
    public final int readInt() throws IOException {
        // BEGIN android-changed
        byte[] buf = new byte[4];
        int nread = 0;
        while (nread < 4) {
          int nbytes = in.read(buf, nread, 4 - nread);
          if (nbytes == -1) {
              throw new EOFException();
          }
          nread += nbytes;
        }
        int b1 = buf[0] & 0xff;
        int b2 = buf[1] & 0xff;
        int b3 = buf[2] & 0xff;
        int b4 = buf[3] & 0xff;
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
        // END android-changed
    }

    /**
     * Returns a string that contains the next line of text available from the
     * source stream. A line is represented by zero or more characters followed
     * by {@code '\n'}, {@code '\r'}, {@code "\r\n"} or the end of the stream.
     * The string does not include the newline sequence.
     * 
     * @return the contents of the line or {@code null} if no characters were
     *         read before the end of the source stream has been reached.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @deprecated Use {@link BufferedReader}
     * @since Android 1.0
     */
    @Deprecated
    public final String readLine() throws IOException {
        StringBuffer line = new StringBuffer(80); // Typical line length
        boolean foundTerminator = false;
        while (true) {
            int nextByte = in.read();
            switch (nextByte) {
                case -1:
                    if (line.length() == 0 && !foundTerminator) {
                        return null;
                    }
                    return line.toString();
                case (byte) '\r':
                    if (foundTerminator) {
                        ((PushbackInputStream) in).unread(nextByte);
                        return line.toString();
                    }
                    foundTerminator = true;
                    /* Have to be able to peek ahead one byte */
                    if (!(in.getClass() == PushbackInputStream.class)) {
                        in = new PushbackInputStream(in);
                    }
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        ((PushbackInputStream) in).unread(nextByte);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }

    /**
     * Reads a 64-bit long value from this stream.
     * 
     * @return the next long value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before eight
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeLong(long)
     * @since Android 1.0
     */
    public final long readLong() throws IOException {
        int i1 = readInt();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        }
        return (((long) i1) << 32) + ((long) b1 << 24) + (b2 << 16) + (b3 << 8)
                + b4;
    }

    /**
     * Reads a 16-bit short value from this stream.
     * 
     * @return the next short value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before two bytes
     *             have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeShort(int)
     * @since Android 1.0
     */
    public final short readShort() throws IOException {
        // BEGIN android-changed
        byte[] buf = new byte[2];
        int nread = 0;
        while (nread < 2) {
          int nbytes = in.read(buf, nread, 2 - nread);
          if (nbytes == -1) {
              throw new EOFException();
          }
          nread += nbytes;
        }
        int b1 = buf[0] & 0xff;
        int b2 = buf[1] & 0xff;
        return (short) ((b1 << 8) + b2);
        // END android-changed
    }

    /**
     * Reads an unsigned 8-bit byte value from this stream and returns it as an
     * int.
     * 
     * @return the next unsigned byte value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream has been reached before one
     *             byte has been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeByte(int)
     * @since Android 1.0
     */
    public final int readUnsignedByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }

    /**
     * Reads a 16-bit unsigned short value from this stream and returns it as an
     * int.
     * 
     * @return the next unsigned short value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before two bytes
     *             have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeShort(int)
     * @since Android 1.0
     */
    public final int readUnsignedShort() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        if ((b1 | b2) < 0) {
            throw new EOFException();
        }
        return ((b1 << 8) + b2);
    }

    /**
     * Reads an string encoded in {@link DataInput modified UTF-8} from this
     * stream.
     * 
     * @return the next {@link DataInput MUTF-8} encoded string read from the
     *         source stream.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeUTF(java.lang.String)
     * @since Android 1.0
     */
    public final String readUTF() throws IOException {
        int utfSize = readUnsignedShort();
        return decodeUTF(utfSize);
    }

    // BEGIN android-removed
    // static final int MAX_BUF_SIZE = 8192;
    //
    // private static class CacheLock {
    // }
    //
    // static final Object cacheLock = new CacheLock();
    //
    // static boolean useShared = true;
    //
    // static byte[] byteBuf = new byte[0];
    //
    // static char[] charBuf = new char[0];
    // END android-removed

    String decodeUTF(int utfSize) throws IOException {
        // BEGIN android-removed
        // byte[] buf;
        // char[] out = null;
        // boolean makeBuf = true;
        //
        // /*
        //  * Try to avoid the synchronization -- if we get a stale value for
        //  * useShared then there is no foul below, but those that sync on the
        //  * lock must see the right value.
        //  */
        // if (utfSize <= MAX_BUF_SIZE && useShared) {
        //     synchronized (cacheLock) {
        //         if (useShared) {
        //             useShared = false;
        //             makeBuf = false;
        //         }
        //     }
        // }
        // if (makeBuf) {
        //     buf = new byte[utfSize];
        //     out = new char[utfSize];
        // } else {
        //     /*
        //      * Need to 'sample' byteBuf and charBuf before using them because
        //      * they are not protected by the cacheLock. They may get out of sync
        //      * with the static and one another, but that is ok because we
        //      * explicitly check and fix their length after sampling.
        //      */
        //     buf = byteBuf;
        //     if (buf.length < utfSize) {
        //         buf = byteBuf = new byte[utfSize];
        //     }
        //     out = charBuf;
        //     if (out.length < utfSize) {
        //         out = charBuf = new char[utfSize];
        //     }
        // }
        // END android-removed
        // BEGIN android-added
        byte[] buf = new byte[utfSize];
        char[] out = new char[utfSize];
        // END android-added
        readFully(buf, 0, utfSize);
        String result;
        result = Util.convertUTF8WithBuf(buf, out, 0, utfSize);
        // BEGIN android-removed
        // if (!makeBuf) {
        //     /*
        //      * Do not synchronize useShared on cacheLock, it will make it back
        //      * to main storage at some point, and no harm until it does.
        //      */
        //     useShared = true;
        // }
        //END android-removed
        return result;
    }

    /**
     * Reads a string encoded in {@link DataInput modified UTF-8} from the
     * {@code DataInput} stream {@code in}.
     * 
     * @param in
     *            the input stream to read from.
     * @return the next {@link DataInput MUTF-8} encoded string from the source
     *         stream.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutputStream#writeUTF(java.lang.String)
     * @since Android 1.0
     */
    public static final String readUTF(DataInput in) throws IOException {
        return in.readUTF();
    }

    /**
     * Skips {@code count} number of bytes in this stream. Subsequent {@code
     * read()}s will not return these bytes unless {@code reset()} is used.
     * 
     * This method will not throw an {@link EOFException} if the end of the
     * input is reached before {@code count} bytes where skipped.
     * 
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if a problem occurs during skipping.
     * @see #mark(int)
     * @see #reset()
     * @since Android 1.0
     */
    public final int skipBytes(int count) throws IOException {
        int skipped = 0;
        long skip;
        while (skipped < count && (skip = in.skip(count - skipped)) != 0) {
            skipped += skip;
        }
        // BEGIN android-removed
        // if (skipped < 0) {
        //     throw new EOFException();
        // }
        // END android-removed
        return skipped;
    }
}

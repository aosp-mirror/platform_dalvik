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
 * DataInputStream is a filter class which can read typed data from a Stream.
 * Typically, this stream has been written by a DataOutputStream. Types that can
 * be read include byte, 16-bit short, 32-bit int, 32-bit float, 64-bit long,
 * 64-bit double, byte strings, and UTF Strings.
 * 
 * @see DataOutputStream
 */
public class DataInputStream extends FilterInputStream implements DataInput {

    /**
     * Constructs a new DataInputStream on the InputStream <code>in</code>.
     * All reads can now be filtered through this stream. Note that data read by
     * this Stream is not in a human readable format and was most likely created
     * by a DataOutputStream.
     * 
     * @param in
     *            the target InputStream to filter reads on.
     * 
     * @see DataOutputStream
     * @see RandomAccessFile
     */
    public DataInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads bytes from the source stream into the byte array
     * <code>buffer</code>. The number of bytes actually read is returned.
     * 
     * @param buffer
     *            the buffer to read bytes into
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    @Override
    public final int read(byte[] buffer) throws IOException {
        return in.read(buffer, 0, buffer.length);
    }

    /**
     * Read at most <code>length</code> bytes from this DataInputStream and
     * stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
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
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeBoolean(boolean)
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
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeByte(int)
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
     * @return the next <code>char</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeChar(int)
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
     * Reads a 64-bit <code>double</code> value from this stream.
     * 
     * @return the next <code>double</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeDouble(double)
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads a 32-bit <code>float</code> value from this stream.
     * 
     * @return the next <code>float</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeFloat(float)
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads bytes from this stream into the byte array <code>buffer</code>.
     * This method will block until <code>buffer.length</code> number of bytes
     * have been read.
     * 
     * @param buffer
     *            to read bytes into
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public final void readFully(byte[] buffer) throws IOException {
        readFully(buffer, 0, buffer.length);
    }

    /**
     * Reads bytes from this stream and stores them in the byte array
     * <code>buffer</code> starting at the position <code>offset</code>.
     * This method blocks until <code>count</code> bytes have been read.
     * 
     * @param buffer
     *            the byte array into which the data is read
     * @param offset
     *            the offset the operation start at
     * @param length 
     *            the maximum number of bytes to read
     * 
     * @throws IOException
     *             if a problem occurs while reading from this stream
     * @throws EOFException
     *             if reaches the end of the stream before enough bytes have
     *             been read
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    public final void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        if (length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return;
        }
        if (in == null) {
            throw new NullPointerException(Msg.getString("KA00b")); //$NON-NLS-1$
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        if (offset < 0 || offset > buffer.length - length) {
            throw new IndexOutOfBoundsException();
        }
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
     * @return the next <code>int</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeInt(int)
     */
    public final int readInt() throws IOException {
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
    }

    /**
     * Returns a <code>String</code> representing the next line of text
     * available in this BufferedReader. A line is represented by 0 or more
     * characters followed by <code>'\n'</code>, <code>'\r'</code>,
     * <code>"\n\r"</code> or end of stream. The <code>String</code> does
     * not include the newline sequence.
     * 
     * @return the contents of the line or null if no characters were read
     *         before end of stream.
     * 
     * @throws IOException
     *             If the DataInputStream is already closed or some other IO
     *             error occurs.
     * 
     * @deprecated Use {@link BufferedReader}
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
     * Reads a 64-bit <code>long</code> value from this stream.
     * 
     * @return the next <code>long</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeLong(long)
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
     * Reads a 16-bit <code>short</code> value from this stream.
     * 
     * @return the next <code>short</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeShort(int)
     */
    public final short readShort() throws IOException {
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
    }

    /**
     * Reads an unsigned 8-bit <code>byte</code> value from this stream and
     * returns it as an int.
     * 
     * @return the next unsigned byte value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeByte(int)
     */
    public final int readUnsignedByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }

    /**
     * Reads a 16-bit unsigned <code>short</code> value from this stream and
     * returns it as an int.
     * 
     * @return the next unsigned <code>short</code> value from the source
     *         stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeShort(int)
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
     * Reads a UTF format String from this Stream.
     * 
     * @return the next UTF String from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeUTF(java.lang.String)
     */
    public final String readUTF() throws IOException {
        int utfSize = readUnsignedShort();
        return decodeUTF(utfSize);
    }

// BEGIN android-removed
//  static final int MAX_BUF_SIZE = 8192;
//
//  private static class CacheLock {
//  }
//
//  static final Object cacheLock = new CacheLock();
//
//  static boolean useShared = true;
//
//  static byte[] byteBuf = new byte[0];
//
//  static char[] charBuf = new char[0];
// END android-removed

    String decodeUTF(int utfSize) throws IOException {
// BEGIN android-removed
//        byte[] buf;
//        char[] out = null;
//        boolean makeBuf = true;
//
//        /*
//         * Try to avoid the synchronization -- if we get a stale value for
//         * useShared then there is no foul below, but those that sync on the
//         * lock must see the right value.
//         */
//        if (utfSize <= MAX_BUF_SIZE && useShared) {
//            synchronized (cacheLock) {
//                if (useShared) {
//                    useShared = false;
//                    makeBuf = false;
//                }
//            }
//        }
//        if (makeBuf) {
//            buf = new byte[utfSize];
//            out = new char[utfSize];
//        } else {
//            /*
//             * Need to 'sample' byteBuf and charBuf before using them because
//             * they are not protected by the cacheLock. They may get out of sync
//             * with the static and one another, but that is ok because we
//             * explicitly check and fix their length after sampling.
//             */
//            buf = byteBuf;
//            if (buf.length < utfSize) {
//                buf = byteBuf = new byte[utfSize];
//            }
//            out = charBuf;
//            if (out.length < utfSize) {
//                out = charBuf = new char[utfSize];
//            }
//        }
// END android-removed
// BEGIN android-added
        byte[] buf = new byte[utfSize];
        char[] out = new char[utfSize];
// END android-added
        readFully(buf, 0, utfSize);
        String result;
        result = Util.convertUTF8WithBuf(buf, out, 0, utfSize);
// BEGIN android-removed
//           if (!makeBuf) {
//           /*
//            * Do not synchronize useShared on cacheLock, it will make it back
//            * to main storage at some point, and no harm until it does.
//            */
//           useShared = true;
//  }
//END android-removed
        return result;
    }

    /**
     * Reads a UTF format String from the DataInput Stream <code>in</code>.
     * 
     * @param in
     *            the input stream to read from
     * @return the next UTF String from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeUTF(java.lang.String)
     */
    public static final String readUTF(DataInput in) throws IOException {
        return in.readUTF();
    }

    /**
     * Skips <code>count</code> number of bytes in this stream. Subsequent
     * <code>read()</code>'s will not return these bytes unless
     * <code>reset()</code> is used.
     * 
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public final int skipBytes(int count) throws IOException {
        int skipped = 0;
        long skip;
        while (skipped < count && (skip = in.skip(count - skipped)) != 0) {
            skipped += skip;
        }
        if (skipped < 0) {
            throw new EOFException();
        }
        return skipped;
    }
}

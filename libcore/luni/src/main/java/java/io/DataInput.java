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

/**
 * DataInput is an interface which declares methods for reading in typed data
 * from a Stream. Typically, this stream has been written by a class which
 * implements DataOutput. Types that can be read include byte, 16-bit short,
 * 32-bit int, 32-bit float, 64-bit long, 64-bit double, byte strings, and UTF
 * Strings.
 * 
 * @see DataInputStream
 * @see RandomAccessFile
 */
public interface DataInput {
    /**
     * Reads a boolean from this stream.
     * 
     * @return the next boolean value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeBoolean(boolean)
     */
    public abstract boolean readBoolean() throws IOException;

    /**
     * Reads an 8-bit byte value from this stream.
     * 
     * @return the next byte value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeByte(int)
     */
    public abstract byte readByte() throws IOException;

    /**
     * Reads a 16-bit character value from this stream.
     * 
     * @return the next <code>char</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeChar(int)
     */
    public abstract char readChar() throws IOException;

    /**
     * Reads a 64-bit <code>double</code> value from this stream.
     * 
     * @return the next <code>double</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeDouble(double)
     */
    public abstract double readDouble() throws IOException;

    /**
     * Reads a 32-bit <code>float</code> value from this stream.
     * 
     * @return the next <code>float</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeFloat(float)
     */
    public abstract float readFloat() throws IOException;

    /**
     * Reads bytes from this stream into the byte array <code>buffer</code>.
     * This method will block until <code>buffer.length</code> number of bytes
     * have been read.
     * 
     * @param buffer
     *            the buffer to read bytes into
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public abstract void readFully(byte[] buffer) throws IOException;

    /**
     * Read bytes from this stream and stores them in byte array
     * <code>buffer</code> starting at offset <code>offset</code>. This
     * method blocks until <code>count</code> number of bytes have been read.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param count
     *            the maximum number of bytes to store in <code>buffer</code>.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public abstract void readFully(byte[] buffer, int offset, int count)
            throws IOException;

    /**
     * Reads a 32-bit integer value from this stream.
     * 
     * @return the next <code>int</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeInt(int)
     */
    public abstract int readInt() throws IOException;

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
     *             If a problem occurs reading from this stream.
     */
    public abstract String readLine() throws IOException;

    /**
     * Reads a 64-bit <code>long</code> value from this stream.
     * 
     * @return the next <code>long</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeLong(long)
     */
    public abstract long readLong() throws IOException;

    /**
     * Reads a 16-bit <code>short</code> value from this stream.
     * 
     * @return the next <code>short</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeShort(int)
     */
    public abstract short readShort() throws IOException;

    /**
     * Reads an unsigned 8-bit <code>byte</code> value from this stream and
     * returns it as an int.
     * 
     * @return the next unsigned byte value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeByte(int)
     */
    public abstract int readUnsignedByte() throws IOException;

    /**
     * Reads a 16-bit unsigned <code>short</code> value from this stream and
     * returns it as an int.
     * 
     * @return the next unsigned <code>short</code> value from the source
     *         stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeShort(int)
     */
    public abstract int readUnsignedShort() throws IOException;

    /**
     * Reads a UTF format String from this Stream.
     * 
     * @return the next UTF String from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this stream.
     * 
     * @see DataOutput#writeUTF(java.lang.String)
     */
    public abstract String readUTF() throws IOException;

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
     *             If a problem occurs reading from this stream.
     */
    public abstract int skipBytes(int count) throws IOException;
}

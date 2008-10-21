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
 * DataOutput is an interface which declares methods for writing typed data to a
 * Stream. Typically, this stream can be read in by a class which implements
 * DataInput. Types that can be written include byte, 16-bit short, 32-bit int,
 * 32-bit float, 64-bit long, 64-bit double, byte strings, and UTF Strings.
 * 
 * @see DataOutputStream
 * @see RandomAccessFile
 */
public interface DataOutput {

    /**
     * Writes the entire contents of the byte array <code>buffer</code> to the
     * OutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readFully(byte[])
     * @see DataInput#readFully(byte[], int, int)
     */
    public abstract void write(byte buffer[]) throws IOException;

    /**
     * Writes <code>count</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at offset <code>index</code> to the
     * OutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param count
     *            number of bytes in buffer to write
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readFully(byte[])
     * @see DataInput#readFully(byte[], int, int)
     */
    public abstract void write(byte buffer[], int offset, int count)
            throws IOException;

    /**
     * Writes the specified <code>byte</code> to the OutputStream.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readByte()
     */
    public abstract void write(int oneByte) throws IOException;

    /**
     * Writes a boolean to this output stream.
     * 
     * @param val
     *            the boolean value to write to the OutputStream
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readBoolean()
     */
    public abstract void writeBoolean(boolean val) throws IOException;

    /**
     * Writes a 8-bit byte to this output stream.
     * 
     * @param val
     *            the byte value to write to the OutputStream
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readByte()
     * @see DataInput#readUnsignedByte()
     */
    public abstract void writeByte(int val) throws IOException;

    /**
     * Writes the low order 8-bit bytes from a String to this output stream.
     * 
     * @param str
     *            the String containing the bytes to write to the OutputStream
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readFully(byte[])
     * @see DataInput#readFully(byte[],int,int)
     */
    public abstract void writeBytes(String str) throws IOException;

    /**
     * Writes the specified 16-bit character to the OutputStream. Only the lower
     * 2 bytes are written with the higher of the 2 bytes written first. This
     * represents the Unicode value of val.
     * 
     * @param oneByte
     *            the character to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readChar()
     */
    public abstract void writeChar(int oneByte) throws IOException;

    /**
     * Writes the specified 16-bit characters contained in str to the
     * OutputStream. Only the lower 2 bytes of each character are written with
     * the higher of the 2 bytes written first. This represents the Unicode
     * value of each character in str.
     * 
     * @param str
     *            the String whose characters are to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readChar()
     */
    public abstract void writeChars(String str) throws IOException;

    /**
     * Writes a 64-bit double to this output stream. The resulting output is the
     * 8 bytes resulting from calling Double.doubleToLongBits().
     * 
     * @param val
     *            the double to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readDouble()
     */
    public abstract void writeDouble(double val) throws IOException;

    /**
     * Writes a 32-bit float to this output stream. The resulting output is the
     * 4 bytes resulting from calling Float.floatToIntBits().
     * 
     * @param val
     *            the float to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readFloat()
     */
    public abstract void writeFloat(float val) throws IOException;

    /**
     * Writes a 32-bit int to this output stream. The resulting output is the 4
     * bytes, highest order first, of val.
     * 
     * @param val
     *            the int to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readInt()
     */
    public abstract void writeInt(int val) throws IOException;

    /**
     * Writes a 64-bit long to this output stream. The resulting output is the 8
     * bytes, highest order first, of val.
     * 
     * @param val
     *            the long to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readLong()
     */
    public abstract void writeLong(long val) throws IOException;

    /**
     * Writes the specified 16-bit short to the OutputStream. Only the lower 2
     * bytes are written with the higher of the 2 bytes written first.
     * 
     * @param val
     *            the short to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readShort()
     * @see DataInput#readUnsignedShort()
     */
    public abstract void writeShort(int val) throws IOException;

    /**
     * Writes the specified String out in UTF format.
     * 
     * @param str
     *            the String to be written in UTF format.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this stream.
     * 
     * @see DataInput#readUTF()
     */
    public abstract void writeUTF(String str) throws IOException;
}

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
 * DataOutputStream is a filter class which can write typed data to a Stream.
 * Typically, this stream can be read in by a DataInputStream. Types that can be
 * written include byte, 16-bit short, 32-bit int, 32-bit float, 64-bit long,
 * 64-bit double, byte strings, and UTF Strings.
 * 
 * @see DataInputStream
 */
public class DataOutputStream extends FilterOutputStream implements DataOutput {

    /** The number of bytes written out so far */
    protected int written;

    /**
     * Constructs a new DataOutputStream on the OutputStream <code>out</code>.
     * All writes can now be filtered through this stream. Note that data
     * written by this Stream is not in a human readable format but can be
     * reconstructed by using a DataInputStream on the resulting output.
     * 
     * @param out
     *            the target OutputStream to filter writes on.
     */
    public DataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Flush this DataOutputStream to ensure all pending data is sent out to the
     * target OutputStream. This implementation flushes the target OutputStream.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this DataOutputStream.
     */
    @Override
    public void flush() throws IOException {
        super.flush();
    }

    /**
     * Returns the total number of bytes written to this stream thus far.
     * 
     * @return the number of bytes written to this DataOutputStream.
     */
    public final int size() {
        if (written < 0) {
            written = Integer.MAX_VALUE;
        }
        return written;
    }

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
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readFully(byte[])
     * @see DataInput#readFully(byte[], int, int)
     */
    @Override
    public void write(byte buffer[], int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        out.write(buffer, offset, count);
        written += count;
    }

    /**
     * Writes the specified <code>byte</code> to the OutputStream.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readByte()
     */
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
        written++;
    }

    /**
     * Writes a boolean to this output stream.
     * 
     * @param val
     *            the boolean value to write to the OutputStream
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readBoolean()
     */
    public final void writeBoolean(boolean val) throws IOException {
        out.write(val ? 1 : 0);
        written++;
    }

    /**
     * Writes a 8-bit byte to this output stream.
     * 
     * @param val
     *            the byte value to write to the OutputStream
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readByte()
     * @see DataInput#readUnsignedByte()
     */
    public final void writeByte(int val) throws IOException {
        out.write(val);
        written++;
    }

    /**
     * Writes the low order 8-bit bytes from a String to this output stream.
     * 
     * @param str
     *            the String containing the bytes to write to the OutputStream
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readFully(byte[])
     * @see DataInput#readFully(byte[],int,int)
     */
    public final void writeBytes(String str) throws IOException {
        if (str.length() == 0) {
            return;
        }
        byte bytes[] = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) str.charAt(index);
        }
        out.write(bytes);
        written += bytes.length;
    }

    /**
     * Writes the specified 16-bit character to the OutputStream. Only the lower
     * 2 bytes are written with the higher of the 2 bytes written first. This
     * represents the Unicode value of val.
     * 
     * @param val
     *            the character to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readChar()
     */
    public final void writeChar(int val) throws IOException {
        out.write(val >> 8);
        out.write(val);
        written += 2;
    }

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
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readChar()
     */
    public final void writeChars(String str) throws IOException {
        byte newBytes[] = new byte[str.length() * 2];
        for (int index = 0; index < str.length(); index++) {
            int newIndex = index == 0 ? index : index * 2;
            newBytes[newIndex] = (byte) (str.charAt(index) >> 8);
            newBytes[newIndex + 1] = (byte) str.charAt(index);
        }
        out.write(newBytes);
        written += newBytes.length;
    }

    /**
     * Writes a 64-bit double to this output stream. The resulting output is the
     * 8 bytes resulting from calling Double.doubleToLongBits().
     * 
     * @param val
     *            the double to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readDouble()
     */
    public final void writeDouble(double val) throws IOException {
        writeLong(Double.doubleToLongBits(val));
    }

    /**
     * Writes a 32-bit float to this output stream. The resulting output is the
     * 4 bytes resulting from calling Float.floatToIntBits().
     * 
     * @param val
     *            the float to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readFloat()
     */
    public final void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }

    /**
     * Writes a 32-bit int to this output stream. The resulting output is the 4
     * bytes, highest order first, of val.
     * 
     * @param val
     *            the int to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readInt()
     */
    public final void writeInt(int val) throws IOException {
        out.write(val >> 24);
        out.write(val >> 16);
        out.write(val >> 8);
        out.write(val);
        written += 4;
    }

    /**
     * Writes a 64-bit long to this output stream. The resulting output is the 8
     * bytes, highest order first, of val.
     * 
     * @param val
     *            the long to be written.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readLong()
     */
    public final void writeLong(long val) throws IOException {
        writeInt((int) (val >> 32));
        writeInt((int) val);
    }

    /**
     * Writes the specified 16-bit short to the OutputStream. Only the lower 2
     * bytes are written with the higher of the 2 bytes written first.
     * 
     * @param val
     *            the short to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readShort()
     * @see DataInput#readUnsignedShort()
     */
    public final void writeShort(int val) throws IOException {
        writeChar(val);
    }
    
// BEGIN android-added
    static final int MAX_BUF_SIZE = 8192;
// END android-added
    
    /**
     * Writes the specified String out in UTF format.
     * 
     * @param str
     *            the String to be written in UTF format.
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see DataInput#readUTF()
     */
    public final void writeUTF(String str) throws IOException {
        int length = str.length();
// BEGIN android-changed
        if (length <= MAX_BUF_SIZE / 3) {
// END android-changed
            int size = length * 3;
            byte[] utfBytes;
// BEGIN android-removed
//            boolean makeBuf = true;
//            synchronized (DataInputStream.byteBuf) {
//                if (DataInputStream.useShared) {
//                    DataInputStream.useShared = false;
//                    makeBuf = false;
//                }
//            }
//            if (makeBuf) {
// END android-removed
               utfBytes = new byte[size];
// BEGIN android-removed
//            } else {
//                if (DataInputStream.byteBuf.length < size) {
//                    DataInputStream.byteBuf = new byte[size];
//                }
//                utfBytes = DataInputStream.byteBuf;
//            }
// END android-removed
            int utfIndex = 0;
            for (int i = 0; i < length; i++) {
                int charValue = str.charAt(i);
                if (charValue > 0 && charValue <= 127) {
                    utfBytes[utfIndex++] = (byte) charValue;
                } else if (charValue <= 2047) {
                    utfBytes[utfIndex++] = (byte) (0xc0 | (0x1f & (charValue >> 6)));
                    utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
                } else {
                    utfBytes[utfIndex++] = (byte) (0xe0 | (0x0f & (charValue >> 12)));
                    utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & (charValue >> 6)));
                    utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
                }
            }
            writeShort(utfIndex);
            write(utfBytes, 0, utfIndex);
// BEGIN android-removed
//            if (!makeBuf) {
//                DataInputStream.useShared = true;
//            }
// END android-removed
        } else {
            long utfCount;
            if (length <= 65535 && (utfCount = countUTFBytes(str)) <= 65535) {
                writeShort((int) utfCount);
                writeUTFBytes(str, utfCount);
            } else {
                throw new UTFDataFormatException(Msg.getString("K0068")); //$NON-NLS-1$
            }
        }
    }

    long countUTFBytes(String str) {
        int utfCount = 0, length = str.length();
        for (int i = 0; i < length; i++) {
            int charValue = str.charAt(i);
            if (charValue > 0 && charValue <= 127) {
                utfCount++;
            } else if (charValue <= 2047) {
                utfCount += 2;
            } else {
                utfCount += 3;
            }
        }
        return utfCount;
    }

    void writeUTFBytes(String str, long count) throws IOException {
        boolean single = true;
        int size = (int) count;
// BEGIN android-changed
        if (count > MAX_BUF_SIZE) {
// END android-changed
            single = false;
// BEGIN android-changed
            size = MAX_BUF_SIZE;
// END android-changed
        }
        byte[] utfBytes;
// BEGIN android-removed
//             boolean makeBuf = true;
//             if (DataInputStream.useShared) {
//                 synchronized (DataInputStream.cacheLock) {
//                     if (DataInputStream.useShared) {
//                         DataInputStream.useShared = false;
//                         makeBuf = false;
//                     }
//                 }
//             }
//             if (makeBuf) {
// END android-removed
                 utfBytes = new byte[size];
// BEGIN android-removed
//             } else {
//                 // byteBuf is not protected by the cacheLock, so sample it first
//                 utfBytes = DataInputStream.byteBuf;
//                 if (utfBytes.length < size) {
//                     utfBytes = DataInputStream.byteBuf = new byte[size];
//                 }
// END android-removed

        int utfIndex = 0, i = 0, length = str.length();
        int end = length;
        while (i < length) {
            if (!single) {
                end = i + ((utfBytes.length - utfIndex) / 3);
                if (end > length) {
                    end = length;
                }
            }
            for (int j = i; j < end; j++) {
                int charValue = str.charAt(j);
                if (charValue > 0 && charValue <= 127) {
                    utfBytes[utfIndex++] = (byte) charValue;
                } else if (charValue <= 2047) {
                    utfBytes[utfIndex++] = (byte) (0xc0 | (0x1f & (charValue >> 6)));
                    utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
                } else {
                    utfBytes[utfIndex++] = (byte) (0xe0 | (0x0f & (charValue >> 12)));
                    utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & (charValue >> 6)));
                    utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
                }
            }
            if (single || utfIndex > utfBytes.length - 300) {
                write(utfBytes, 0, utfIndex);
                if (single) {
                    return;
                }
                utfIndex = 0;
            }
            i = end;
        }
        if (utfIndex > 0) {
            write(utfBytes, 0, utfIndex);
        }
// BEGIN android-removed
//        if (!makeBuf) {
//            // Update the useShared flag optimistically (see DataInputStream
//            // equivalent)
//            DataInputStream.useShared = true;
//        }
// END android-removed
    }
}

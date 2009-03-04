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
 * Wraps an existing {@link OutputStream} and writes typed data to it.
 * Typically, this stream can be read in by DataInputStream. Types that can be
 * written include byte, 16-bit short, 32-bit int, 32-bit float, 64-bit long,
 * 64-bit double, byte strings, and {@link DataInput MUTF-8} encoded strings.
 * 
 * @see DataInputStream
 * 
 * @since Android 1.0
 */
public class DataOutputStream extends FilterOutputStream implements DataOutput {

    /**
     * The number of bytes written out so far.
     * 
     * @since Android 1.0
     */
    protected int written;

    /**
     * Constructs a new {@code DataOutputStream} on the {@code OutputStream}
     * {@code out}. Note that data written by this stream is not in a human
     * readable form but can be reconstructed by using a {@link DataInputStream}
     * on the resulting output.
     * 
     * @param out
     *            the target stream for writing.
     * @since Android 1.0
     */
    public DataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Flushes this stream to ensure all pending data is sent out to the target
     * stream. This implementation then also flushes the target stream.
     * 
     * @throws IOException
     *             if an error occurs attempting to flush this stream.
     * @since Android 1.0
     */
    @Override
    public void flush() throws IOException {
        super.flush();
    }

    /**
     * Returns the total number of bytes written to the target stream so far.
     * 
     * @return the number of bytes written to the target stream.
     * @since Android 1.0
     */
    public final int size() {
        if (written < 0) {
            written = Integer.MAX_VALUE;
        }
        return written;
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * {@code offset} to the target stream.
     * 
     * @param buffer
     *            the buffer to write to the target stream.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes from the {@code buffer} to write.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     * @see DataInputStream#readFully(byte[])
     * @see DataInputStream#readFully(byte[], int, int)
     * @since Android 1.0
     */
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        // BEGIN android-note
        // changed array notation to be consistent with the rest of harmony
        // END android-note
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        out.write(buffer, offset, count);
        written += count;
    }

    /**
     * Writes a byte to the target stream. Only the least significant byte of
     * the integer {@code oneByte} is written.
     * 
     * @param oneByte
     *            the byte to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readByte()
     * @since Android 1.0
     */
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
        written++;
    }

    /**
     * Writes a boolean to the target stream.
     * 
     * @param val
     *            the boolean value to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readBoolean()
     * @since Android 1.0
     */
    public final void writeBoolean(boolean val) throws IOException {
        out.write(val ? 1 : 0);
        written++;
    }

    /**
     * Writes an 8-bit byte to the target stream. Only the least significant
     * byte of the integer {@code val} is written.
     * 
     * @param val
     *            the byte value to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readByte()
     * @see DataInputStream#readUnsignedByte()
     * @since Android 1.0
     */
    public final void writeByte(int val) throws IOException {
        out.write(val);
        written++;
    }

    /**
     * Writes the low order bytes from a string to the target stream.
     * 
     * @param str
     *            the string containing the bytes to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readFully(byte[])
     * @see DataInputStream#readFully(byte[],int,int)
     * @since Android 1.0
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
     * Writes a 16-bit character to the target stream. Only the two lower bytes
     * of the integer {@code val} are written, with the higher one written
     * first. This corresponds to the Unicode value of {@code val}.
     * 
     * @param val
     *            the character to write to the target stream
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readChar()
     * @since Android 1.0
     */
    public final void writeChar(int val) throws IOException {
        out.write(val >> 8);
        out.write(val);
        written += 2;
    }

    /**
     * Writes the 16-bit characters contained in {@code str} to the target
     * stream.
     * 
     * @param str
     *            the string that contains the characters to write to this
     *            stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readChar()
     * @since Android 1.0
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
     * Writes a 64-bit double to the target stream. The resulting output is the
     * eight bytes resulting from calling Double.doubleToLongBits().
     * 
     * @param val
     *            the double to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readDouble()
     * @since Android 1.0
     */
    public final void writeDouble(double val) throws IOException {
        writeLong(Double.doubleToLongBits(val));
    }

    /**
     * Writes a 32-bit float to the target stream. The resulting output is the
     * four bytes resulting from calling Float.floatToIntBits().
     * 
     * @param val
     *            the float to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readFloat()
     * @since Android 1.0
     */
    public final void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }

    /**
     * Writes a 32-bit int to the target stream. The resulting output is the
     * four bytes, highest order first, of {@code val}.
     * 
     * @param val
     *            the int to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readInt()
     * @since Android 1.0
     */
    public final void writeInt(int val) throws IOException {
        out.write(val >> 24);
        out.write(val >> 16);
        out.write(val >> 8);
        out.write(val);
        written += 4;
    }

    /**
     * Writes a 64-bit long to the target stream. The resulting output is the
     * eight bytes, highest order first, of {@code val}.
     * 
     * @param val
     *            the long to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readLong()
     * @since Android 1.0
     */
    public final void writeLong(long val) throws IOException {
        writeInt((int) (val >> 32));
        writeInt((int) val);
    }

    /**
     * Writes the specified 16-bit short to the target stream. Only the lower
     * two bytes of the integer {@code val} are written, with the higher one
     * written first.
     * 
     * @param val
     *            the short to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readShort()
     * @see DataInputStream#readUnsignedShort()
     * @since Android 1.0
     */
    public final void writeShort(int val) throws IOException {
        writeChar(val);
    }
    
    // BEGIN android-added
    static final int MAX_BUF_SIZE = 8192;
    // END android-added
    
    /**
     * Writes the specified encoded in {@link DataInput modified UTF-8} to this
     * stream.
     * 
     * @param str
     *            the string to write to the target stream encoded in
     *            {@link DataInput modified UTF-8}.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @throws UTFDataFormatException
     *             if the encoded string is longer than 65535 bytes.
     * @see DataInputStream#readUTF()
     * @since Android 1.0
     */
    public final void writeUTF(String str) throws IOException {
        int length = str.length();
        // BEGIN android-changed
        if (length <= MAX_BUF_SIZE / 3) {
            int size = length * 3;
            byte[] utfBytes = new byte[size];
            // boolean makeBuf = true;
            // synchronized (DataInputStream.byteBuf) {
            //     if (DataInputStream.useShared) {
            //         DataInputStream.useShared = false;
            //         makeBuf = false;
            //     }
            // }
            // if (makeBuf) {
            //     utfBytes = new byte[size];
            // } else {
            //     if (DataInputStream.byteBuf.length < size) {
            //         DataInputStream.byteBuf = new byte[size];
            //     }
            //     utfBytes = DataInputStream.byteBuf;
            // }
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
            // if (!makeBuf) {
            //     DataInputStream.useShared = true;
            // }
        } else {
            long utfCount;
            if (length <= 65535 && (utfCount = countUTFBytes(str)) <= 65535) {
                writeShort((int) utfCount);
                writeUTFBytes(str, utfCount);
            } else {
                throw new UTFDataFormatException(Msg.getString("K0068")); //$NON-NLS-1$
            }
        }
        // END android-changed
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
            single = false;
            size = MAX_BUF_SIZE;
        }
        byte[] utfBytes = new byte[size];
        // END android-changed
        // BEGIN android-removed
        // boolean makeBuf = true;
        // if (DataInputStream.useShared) {
        //     synchronized (DataInputStream.cacheLock) {
        //         if (DataInputStream.useShared) {
        //             DataInputStream.useShared = false;
        //             makeBuf = false;
        //         }
        //     }
        // }
        // if (makeBuf) {
        //     utfBytes = new byte[size];
        // } else {
        //     // byteBuf is not protected by the cacheLock, so sample it first
        //     utfBytes = DataInputStream.byteBuf;
        //     if (utfBytes.length < size) {
        //         utfBytes = DataInputStream.byteBuf = new byte[size];
        //     }
        // }
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
        // if (!makeBuf) {
        //     // Update the useShared flag optimistically (see DataInputStream
        //     // equivalent)
        //     DataInputStream.useShared = true;
        // }
        // END android-removed
    }
}

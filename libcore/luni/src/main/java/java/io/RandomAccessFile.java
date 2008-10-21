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

import java.nio.channels.FileChannel;

import org.apache.harmony.luni.platform.IFileSystem;
import org.apache.harmony.luni.platform.Platform;
import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.Util;

import org.apache.harmony.nio.FileChannelFactory;

/**
 * RandomAccessFile is a class which allows positioning of the next read
 * anywhere in the file. This is useful for reading specific locations of files
 * or following links within a file. Most input classes only support forward
 * skipping.
 */
public class RandomAccessFile implements DataInput, DataOutput, Closeable {
    /**
     * The FileDescriptor representing this RandomAccessFile.
     */
    private FileDescriptor fd;

    private boolean syncMetadata = false;

    // The unique file channel associated with this FileInputStream (lazily
    // initialized).
    private FileChannel channel;

    private IFileSystem fileSystem = Platform.getFileSystem();

    private boolean isReadOnly;

    private static class RepositionLock {
    }

    private Object repositionLock = new RepositionLock();

    /**
     * Constructs a new RandomAccessFile on the File <code>file</code> and
     * opens it according to the access String in <code>mode</code>. The
     * access mode may be one of <code>"r"</code> for read access only, or
     * <code>"rw"</code> for read/write access.
     * 
     * @param file
     *            the File to open.
     * @param mode
     *            "r" for read only, or "rw" for read/write.
     * 
     * @throws FileNotFoundException
     *             If the <code>mode</code> is incorrect or the File cannot be
     *             opened in the requested <code>mode</code>.
     * 
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     * @see java.lang.SecurityManager#checkWrite(FileDescriptor)
     */
    public RandomAccessFile(File file, String mode)
            throws FileNotFoundException {
        super();

        int options = 0;
        
        fd = new FileDescriptor();
       
        if (mode.equals("r")) { //$NON-NLS-1$
            isReadOnly = true;
            fd.readOnly = true;
            options = IFileSystem.O_RDONLY;
        } else if (mode.equals("rw") || mode.equals("rws") || mode.equals("rwd")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            isReadOnly = false;
            options = IFileSystem.O_RDWR;

            if (mode.equals("rws")) { //$NON-NLS-1$
                // Sync file and metadata with every write
                syncMetadata = true;
            } else if (mode.equals("rwd")) { //$NON-NLS-1$
                // Sync file, but not necessarily metadata
                options = IFileSystem.O_RDWRSYNC;
            }
        } else {
            throw new IllegalArgumentException(Msg.getString("K0081")); //$NON-NLS-1$
        }

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(file.getPath());
            if (!isReadOnly) {
                security.checkWrite(file.getPath());
            }
        }
        
        fd.descriptor = fileSystem.open(file.properPath(true), options);
        channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                options);

        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            try {
                fd.sync();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

    /**
     * Constructs a new RandomAccessFile on the file named <code>fileName</code>
     * and opens it according to the access String in <code>mode</code>. The
     * file may be absolute or relative to the System property
     * <code>"user.dir"</code>. The access mode may be one of
     * <code>"r"</code> for read access only, or <code>"rw"</code> for
     * read/write access.
     * 
     * @param fileName
     *            the filename of the file to open.
     * @param mode
     *            "r" for read only, or "rw" for read/write.
     * 
     * @throws FileNotFoundException
     *             If the <code>mode</code> is incorrect or the file cannot be
     *             opened in the requested <code>mode</code>.
     * 
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     * @see java.lang.SecurityManager#checkWrite(FileDescriptor)
     */
    public RandomAccessFile(String fileName, String mode)
            throws FileNotFoundException {
        this(new File(fileName), mode);
    }

    /**
     * Close this RandomAccessFile.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this RandomAccessFile.
     */
    public void close() throws IOException {
        synchronized (channel) {
            if (channel.isOpen()) {
                channel.close();
            }
        }
        synchronized (this) {
            if (fd != null && fd.descriptor >= 0) {
                fileSystem.close(fd.descriptor);
                fd.descriptor = -1;
            }
        }
    }

    /**
     * Returns the FileChannel equivalent to this stream.
     * <p>
     * The file channel is write-only and has an initial position within the
     * file that is the same as the current position of this FileOutputStream
     * within the file. All changes made to the underlying file descriptor state
     * via the channel are visible by the output stream and vice versa.
     * </p>
     * 
     * @return the file channel representation for this FileOutputStream.
     */
    public final synchronized FileChannel getChannel() {
        return channel;
    }

    /**
     * Returns the FileDescriptor representing the operating system resource for
     * this RandomAccessFile.
     * 
     * @return the FileDescriptor for this RandomAccessFile.
     * 
     * @throws IOException
     *             If an error occurs attempting to get the FileDescriptor of
     *             this RandomAccessFile.
     */
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }

    /**
     * Returns the current position within this RandomAccessFile. All reads and
     * writes take place at the current file pointer position.
     * 
     * @return the current file pointer position.
     * 
     * @throws IOException
     *             If an error occurs attempting to get the file pointer
     *             position of this RandomAccessFile.
     */
    public long getFilePointer() throws IOException {
        openCheck();
        return fileSystem.seek(fd.descriptor, 0L, IFileSystem.SEEK_CUR);
    }

    /**
     * Checks to see if the file is currently open. Returns silently if it is,
     * and throws an exception if it is not.
     * 
     * @throws IOException
     *             the receiver is closed.
     */
    private synchronized void openCheck() throws IOException {
        if (fd.descriptor < 0) {
            throw new IOException();
        }
    }

    /**
     * Returns the current length of this RandomAccessFile in bytes.
     * 
     * @return the current file length in bytes.
     * 
     * @throws IOException
     *             If an error occurs attempting to get the file length of this
     *             RandomAccessFile.
     */
    public long length() throws IOException {
        openCheck();
        synchronized (repositionLock) {
            long currentPosition = fileSystem.seek(fd.descriptor, 0L,
                    IFileSystem.SEEK_CUR);
            long endOfFilePosition = fileSystem.seek(fd.descriptor, 0L,
                    IFileSystem.SEEK_END);
            fileSystem.seek(fd.descriptor, currentPosition,
                    IFileSystem.SEEK_SET);
            return endOfFilePosition;
        }
    }

    /**
     * Reads a single byte from this RandomAccessFile and returns the result as
     * an int. The low-order byte is returned or -1 of the end of file was
     * encountered.
     * 
     * @return the byte read or -1 if end of file.
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this
     *             RandomAccessFile.
     * 
     * @see #write(byte[])
     * @see #write(byte[], int, int)
     * @see #write(int)
     */
    public int read() throws IOException {
        openCheck();
        byte[] bytes = new byte[1];
        synchronized (repositionLock) {
            long readed = fileSystem.read(fd.descriptor, bytes, 0, 1);
            return readed == -1 ? -1 : bytes[0] & 0xff;
        }
    }

    /**
     * Reads bytes from this RandomAccessFile into the byte array
     * <code>buffer</code>. The number of bytes actually read is returned.
     * 
     * @param buffer
     *            the buffer to read bytes into
     * @return the number of bytes actually read or -1 if end of file.
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this
     *             RandomAccessFile.
     * 
     * @see #write(byte[])
     * @see #write(byte[], int, int)
     * @see #write(int)
     */
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads at most <code>count</code> bytes from this RandomAccessFile and
     * stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of file was encountered.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param count
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of file.
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this
     *             RandomAccessFile.
     * 
     * @see #write(byte[])
     * @see #write(byte[], int, int)
     * @see #write(int)
     */
    public int read(byte[] buffer, int offset, int count) throws IOException {
        // have to have four comparisions to not miss integer overflow cases
        if (count > buffer.length - offset || count < 0 || offset < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (0 == count) {
            return 0;
        }
        openCheck();
        synchronized (repositionLock) {
            return (int) fileSystem.read(fd.descriptor, buffer, offset, count);
        }
    }

    /**
     * Reads a boolean from this stream.
     * 
     * @return boolean the next boolean value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeBoolean(boolean)
     */
    public final boolean readBoolean() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }

    /**
     * Reads an 8-bit byte value from this stream.
     * 
     * @return byte the next byte value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeByte(int)
     */
    public final byte readByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }

    /**
     * Reads a 16-bit character value from this stream.
     * 
     * @return char the next <code>char</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeChar(int)
     */
    public final char readChar() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return (char) (((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff));
    }

    /**
     * Reads a 64-bit <code>double</code> value from this stream.
     * 
     * @return double the next <code>double</code> value from the source
     *         stream.
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
     * @return float the next <code>float</code> value from the source stream.
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
     *            the buffer to read bytes into
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
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public final void readFully(byte[] buffer, int offset, int count)
            throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || count < 0
                || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        while (count > 0) {
            int result = read(buffer, offset, count);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            count -= result;
        }
    }

    /**
     * Reads a 32-bit integer value from this stream.
     * 
     * @return int the next <code>int</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeInt(int)
     */
    public final int readInt() throws IOException {
        byte[] buffer = new byte[4];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((buffer[0] & 0xff) << 24) + ((buffer[1] & 0xff) << 16)
                + ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
    }

    /**
     * Returns a <code>String</code> representing the next line of text
     * available in this BufferedReader. A line is represented by 0 or more
     * characters followed by <code>'\n'</code>, <code>'\r'</code>,
     * <code>"\n\r"</code> or end of stream. The <code>String</code> does
     * not include the newline sequence.
     * 
     * @return String the contents of the line or null if no characters were
     *         read before end of stream.
     * 
     * @throws IOException
     *             If the BufferedReader is already closed or some other IO
     *             error occurs.
     */
    public final String readLine() throws IOException {
        StringBuilder line = new StringBuilder(80); // Typical line length
        boolean foundTerminator = false;
        long unreadPosition = 0;
        while (true) {
            int nextByte = read();
            switch (nextByte) {
                case -1:
                    return line.length() != 0 ? line.toString() : null;
                case (byte) '\r':
                    if (foundTerminator) {
                        seek(unreadPosition);
                        return line.toString();
                    }
                    foundTerminator = true;
                    /* Have to be able to peek ahead one byte */
                    unreadPosition = getFilePointer();
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        seek(unreadPosition);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }

    /**
     * Reads a 64-bit <code>long</code> value from this stream.
     * 
     * @return long the next <code>long</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeLong(long)
     */
    public final long readLong() throws IOException {
        byte[] buffer = new byte[8];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((long) (((buffer[0] & 0xff) << 24) + ((buffer[1] & 0xff) << 16)
                + ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff)) << 32)
                + ((long) (buffer[4] & 0xff) << 24)
                + ((buffer[5] & 0xff) << 16)
                + ((buffer[6] & 0xff) << 8)
                + (buffer[7] & 0xff);
    }

    /**
     * Reads a 16-bit <code>short</code> value from this stream.
     * 
     * @return short the next <code>short</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeShort(int)
     */
    public final short readShort() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return (short) (((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff));
    }

    /**
     * Reads an unsigned 8-bit <code>byte</code> value from this stream and
     * returns it as an int.
     * 
     * @return int the next unsigned byte value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeByte(int)
     */
    public final int readUnsignedByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }

    /**
     * Reads a 16-bit unsigned <code>short</code> value from this stream and
     * returns it as an int.
     * 
     * @return int the next unsigned <code>short</code> value from the source
     *         stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeShort(int)
     */
    public final int readUnsignedShort() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
    }

    /**
     * Reads a UTF format String from this Stream.
     * 
     * @return String the next UTF String from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeUTF(java.lang.String)
     */
    public final String readUTF() throws IOException {
        int utfSize = readUnsignedShort();
        if (utfSize == 0) {
            return ""; //$NON-NLS-1$
        }
        byte[] buf = new byte[utfSize];
        if (read(buf, 0, buf.length) != buf.length) {
            throw new EOFException();
        }
        return Util.convertFromUTF8(buf, 0, utfSize);
    }

    /**
     * Seeks to the position <code>pos</code> in this RandomAccessFile. All
     * read/write/skip methods sent will be relative to <code>pos</code>.
     * 
     * @param pos
     *            the desired file pointer position
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            // seek position is negative
            throw new IOException(Msg.getString("K0347")); //$NON-NLS-1$
        }
        openCheck();
        synchronized (repositionLock) {
            fileSystem.seek(fd.descriptor, pos, IFileSystem.SEEK_SET);
        }
    }

    /**
     * Set the length of this file to be <code>newLength</code>. If the
     * current file is smaller, it will be expanded and the filePosition will be
     * set to the new file length. If the <code>newLength</code> is smaller
     * then the file will be truncated.
     * 
     * @param newLength
     *            the desired file length
     * 
     * @throws IOException
     *             If the stream is already closed or another IOException
     *             occurs.
     */
    public void setLength(long newLength) throws IOException {
        openCheck();
        if (newLength < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (repositionLock) {
            long position = fileSystem.seek(fd.descriptor, 0,
                    IFileSystem.SEEK_CUR);
            fileSystem.truncate(fd.descriptor, newLength);
            seek(position > newLength ? newLength : position);
        }

        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            fd.sync();
        }
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
    public int skipBytes(int count) throws IOException {
        if (count > 0) {
            long currentPos = getFilePointer(), eof = length();
            int newCount = (int) ((currentPos + count > eof) ? eof - currentPos
                    : count);
            seek(currentPos + newCount);
            return newCount;
        }
        return 0;
    }

    /**
     * Writes the entire contents of the byte array <code>buffer</code> to
     * this RandomAccessFile starting at the current file pointer.
     * 
     * @param buffer
     *            the buffer to be written.
     * 
     * @throws IOException
     *             If an error occurs trying to write to this RandomAccessFile.
     * 
     * @see #read()
     * @see #read(byte[])
     * @see #read(byte[], int, int)
     */
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes <code>count</code> bytes from the byte array <code>buffer</code>
     * starting at <code>offset</code> to this RandomAccessFile starting at
     * the current file pointer..
     * 
     * @param buffer
     *            the bytes to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param count
     *            number of bytes in buffer to write
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             RandomAccessFile.
     * @throws IndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     * 
     * @see #read()
     * @see #read(byte[])
     * @see #read(byte[], int, int)
     */
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (count > buffer.length - offset || count < 0 || offset < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (count == 0) {
            return;
        }
        synchronized (repositionLock) {
            fileSystem.write(fd.descriptor, buffer, offset, count);
        }

        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            fd.sync();
        }
    }

    /**
     * Writes the specified byte <code>oneByte</code> to this RandomAccessFile
     * starting at the current file pointer. Only the low order byte of
     * <code>oneByte</code> is written.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             RandomAccessFile.
     * 
     * @see #read()
     * @see #read(byte[])
     * @see #read(byte[], int, int)
     */
    public void write(int oneByte) throws IOException {
        openCheck();
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (oneByte & 0xff);
        synchronized (repositionLock) {
            fileSystem.write(fd.descriptor, bytes, 0, 1);
        }

        // if we are in "rws" mode, attempt to sync file+metadata
        if (syncMetadata) {
            fd.sync();
        }
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
        write(val ? 1 : 0);
    }

    /**
     * Writes a 8-bit byte to this output stream.
     * 
     * @param val
     *            the byte value to write to the OutputStream
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this
     *             DataOutputStream.
     * 
     * @see #readByte()
     * @see #readUnsignedByte()
     */
    public final void writeByte(int val) throws IOException {
        write(val & 0xFF);
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
     * @see #read(byte[])
     * @see #read(byte[],int,int)
     * @see #readFully(byte[])
     * @see #readFully(byte[],int,int)
     */
    public final void writeBytes(String str) throws IOException {
        byte bytes[] = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) (str.charAt(index) & 0xFF);
        }
        write(bytes);
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
        byte[] buffer = new byte[2];
        buffer[0] = (byte) (val >> 8);
        buffer[1] = (byte) val;
        write(buffer, 0, buffer.length);
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
            newBytes[newIndex] = (byte) ((str.charAt(index) >> 8) & 0xFF);
            newBytes[newIndex + 1] = (byte) (str.charAt(index) & 0xFF);
        }
        write(newBytes);
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
        byte[] buffer = new byte[4];
        buffer[0] = (byte) (val >> 24);
        buffer[1] = (byte) (val >> 16);
        buffer[2] = (byte) (val >> 8);
        buffer[3] = (byte) val;
        write(buffer, 0, buffer.length);
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
        byte[] buffer = new byte[8];
        int t = (int) (val >> 32);
        buffer[0] = (byte) (t >> 24);
        buffer[1] = (byte) (t >> 16);
        buffer[2] = (byte) (t >> 8);
        buffer[3] = (byte) t;
        buffer[4] = (byte) (val >> 24);
        buffer[5] = (byte) (val >> 16);
        buffer[6] = (byte) (val >> 8);
        buffer[7] = (byte) val;
        write(buffer, 0, buffer.length);
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
        if (utfCount > 65535) {
            throw new UTFDataFormatException(Msg.getString("K0068")); //$NON-NLS-1$
        }
        byte utfBytes[] = new byte[utfCount + 2];
        int utfIndex = 2;
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
        utfBytes[0] = (byte) (utfCount >> 8);
        utfBytes[1] = (byte) utfCount;
        write(utfBytes);
    }
}

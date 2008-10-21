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
import org.apache.harmony.nio.FileChannelFactory;

/**
 * FileOutputStream is a class whose underlying stream is represented by a file
 * in the operating system. The bytes that are written to this stream are passed
 * directly to the underlying operating system equivalent function. Since
 * overhead may be high in writing to the OS, FileOutputStreams are usually
 * wrapped with a BufferedOutputStream to reduce the number of times the OS is
 * called.
 * <p>
 * <code>BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream("aFile.txt"));</code>
 * 
 * @see FileInputStream
 */
public class FileOutputStream extends OutputStream implements Closeable {

    /**
     * The FileDescriptor representing this FileOutputStream.
     */
    FileDescriptor fd;

    boolean innerFD;

    // The unique file channel associated with this FileInputStream (lazily
    // initialized).
    private FileChannel channel;

    private IFileSystem fileSystem = Platform.getFileSystem();

    /**
     * Constructs a new FileOutputStream on the File <code>file</code>. If
     * the file exists, it is written over. See the constructor which can append
     * to the file if so desired.
     * 
     * @param file
     *            the File on which to stream reads.
     * 
     * @throws FileNotFoundException
     *             If the <code>file</code> cannot be opened for writing.
     * 
     * @see java.lang.SecurityManager#checkWrite(FileDescriptor)
     */
    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }

    /**
     * Constructs a new FileOutputStream on the File <code>file</code>. If
     * the file exists, it is written over. The parameter <code>append</code>
     * determines whether or not the file is opened and appended to or just
     * opened empty.
     * 
     * @param file
     *            the File on which to stream reads.
     * @param append
     *            a boolean indicating whether or not to append to an existing
     *            file.
     * 
     * @throws FileNotFoundException
     *             If the <code>file</code> cannot be opened for writing.
     * 
     * @see java.lang.SecurityManager#checkWrite(FileDescriptor)
     * @see java.lang.SecurityManager#checkWrite(String)
     */
    public FileOutputStream(File file, boolean append)
            throws FileNotFoundException {
        super();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(file.getPath());
        }
        fd = new FileDescriptor();
        fd.descriptor = fileSystem.open(file.properPath(true),
                append ? IFileSystem.O_APPEND : IFileSystem.O_WRONLY);
        innerFD = true;
        channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                append ? IFileSystem.O_APPEND : IFileSystem.O_WRONLY);
    }

    /**
     * Constructs a new FileOutputStream on the FileDescriptor <code>fd</code>.
     * The file must already be open, therefore no <code>FileIOException</code>
     * will be thrown.
     * 
     * @param fd
     *            the FileDescriptor on which to stream writes.
     * 
     * @see java.lang.SecurityManager#checkWrite(FileDescriptor)
     */
    public FileOutputStream(FileDescriptor fd) {
        super();
        if (fd == null) {
            throw new NullPointerException(Msg.getString("K006c")); //$NON-NLS-1$
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(fd);
        }
        this.fd = fd;
        innerFD = false;
        channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                IFileSystem.O_WRONLY);
    }

    /**
     * Constructs a new FileOutputStream on the file named <code>fileName</code>.
     * If the file exists, it is written over. See the constructor which can
     * append to the file if so desired. The <code>fileName</code> may be
     * absolute or relative to the System property <code>"user.dir"</code>.
     * 
     * @param filename
     *            the file on which to stream writes.
     * 
     * @throws FileNotFoundException
     *             If the <code>filename</code> cannot be opened for writing.
     */
    public FileOutputStream(String filename) throws FileNotFoundException {
        this(filename, false);
    }

    /**
     * Constructs a new FileOutputStream on the file named <code>filename</code>.
     * If the file exists, it is written over. The parameter <code>append</code>
     * determines whether or not the file is opened and appended to or just
     * opened empty. The <code>filename</code> may be absolute or relative to
     * the System property <code>"user.dir"</code>.
     * 
     * @param filename
     *            the file on which to stream writes.
     * @param append
     *            a boolean indicating whether or not to append to an existing
     *            file.
     * 
     * @throws FileNotFoundException
     *             If the <code>filename</code> cannot be opened for writing.
     */
    public FileOutputStream(String filename, boolean append)
            throws FileNotFoundException {
        this(new File(filename), append);
    }

    /**
     * Close the FileOutputStream. This implementation closes the underlying OS
     * resources allocated to represent this stream.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this FileOutputStream.
     */
    @Override
    public void close() throws IOException {
        if (fd == null) {
            // if fd is null, then the underlying file is not opened, so nothing
            // to close
            return;
        }

        if (channel != null) {
            synchronized (channel) {
                if (channel.isOpen() && fd.descriptor >= 0) {
                    channel.close();
                }
            }
        }

        synchronized (this) {
            if (fd.descriptor >= 0 && innerFD) {
                fileSystem.close(fd.descriptor);
                fd.descriptor = -1;
            }
        }
    }

    /**
     * Frees any resources allocated to represent this FileOutputStream before
     * it is garbage collected. This method is called from the Java Virtual
     * Machine.
     * 
     * @throws IOException
     *             If an error occurs attempting to finalize this
     *             FileOutputStream.
     */
    @Override
    protected void finalize() throws IOException {
        close();
    }

    /**
     * Returns the FileChannel equivalent to this output stream.
     * <p>
     * The file channel is write-only and has an initial position within the
     * file that is the same as the current position of this FileOutputStream
     * within the file. All changes made to the underlying file descriptor state
     * via the channel are visible by the output stream and vice versa.
     * </p>
     * 
     * @return the file channel representation for this FileOutputStream.
     */
    public FileChannel getChannel() {
        return channel;
    }

    /**
     * Returns a FileDescriptor which represents the lowest level representation
     * of a OS stream resource.
     * 
     * @return a FileDescriptor representing this FileOutputStream.
     * 
     * @throws IOException
     *             If the Stream is already closed and there is no
     *             FileDescriptor.
     */
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }

    /**
     * Writes the entire contents of the byte array <code>buffer</code> to
     * this FileOutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             FileOutputStream.
     */
    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes <code>count</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at <code>offset</code> to this
     * FileOutputStream.
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
     *             FileOutputStream.
     * @throws IndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     * @throws NullPointerException
     *             If buffer is <code>null</code>.
     */
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (count < 0 || offset < 0 || offset > buffer.length
                || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        if (count == 0) {
            return;
        }

        openCheck();
        fileSystem.write(fd.descriptor, buffer, offset, count);
    }

    /**
     * Writes the specified byte <code>oneByte</code> to this
     * FileOutputStream. Only the low order byte of <code>oneByte</code> is
     * written.
     * 
     * @param oneByte
     *            the byte to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             FileOutputStream.
     */
    @Override
    public void write(int oneByte) throws IOException {
        openCheck();
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) oneByte;
        fileSystem.write(fd.descriptor, byteArray, 0, 1);
    }

    private synchronized void openCheck() throws IOException {
        if (fd.descriptor < 0) {
            throw new IOException();
        }
    }
}

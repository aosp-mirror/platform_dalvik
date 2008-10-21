/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.nio.channels;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;

/**
 * An abstract channel type for interaction with a platform file.
 * <p>
 * A FileChannel defines the methods for reading, writing, memory mapping, and
 * manipulating the logical state of a platform file. This type does not have a
 * method for opening files, since this behaviour has been delegated to the
 * <code>FileInputStream</code>, <code>FileOutputStream</code>, and
 * <code>RandomAccessFile</code> types.
 * </p>
 * <p>
 * FileChannels created from a FileInputStream, or a RandomAccessFile created in
 * mode "r", are read-only. FileChannels created from a FileOutputStream are
 * write-only. FileChannels created from a RandomAccessFile created in mode "rw"
 * are read/write. FileChannels created from a RandomAccessFile that was opened
 * in append-mode will also be in append-mode -- meaning that each write will be
 * proceeded by a seek to the end of file. Some platforms will seek and write
 * atomically, others will not.
 * </p>
 * <p>
 * FileChannels has a virtual pointer into the file which is referred to as a
 * file <em>position</em>. The position can be manipulated by repositioning
 * it within the file, and its current position can be queried.
 * </p>
 * <p>
 * FileChannels also have an associated <em>size</em>. The size of the file
 * is the number of bytes that it currently contains. The size can be
 * manipulated by adding more bytes to the end of the file (which increases the
 * size) or truncating the file (which decreases the size). The current size can
 * also be queried.
 * </p>
 * <p>
 * FileChannels have operations beyond the simple read, write, and close. They
 * can also:
 * <ul>
 * <li>request that cached data be forced onto the disk</li>
 * <li>lock ranges of bytes associated with the file</li>
 * <li>transfer data directly to another channel in a manner that has the
 * potential to be optimized by the platform</li>
 * <li>memory-mapping files into NIO buffers to provide efficient manipulation
 * of file data</li>
 * <li>read and write to the file at absolute byte offsets in a fashion that
 * does not modify the current position</li>
 * </ul>
 * </p>
 * <p>
 * FileChannels are thread-safe. Only one operation involving manipulation of
 * the file position may be in-flight at once. Subsequent calls to such
 * operations will block, and one of those blocked will be freed to continue
 * when the first operation has completed. There is no ordered queue or fairness
 * applied to the blocked threads.
 * </p>
 * <p>
 * It is undefined whether operations that do not manipulate the file position
 * will also block when there are any other operations in-flight.
 * </p>
 * <p>
 * The logical view of the underlying file is consistent across all FileChannels
 * and IO streams opened on the same file by the same JVM process. Therefore
 * modifications performed via a channel will be visible to the stream, and vice
 * versa; including modifications to the file position, content, size, etc.
 * </p>
 * 
 */
public abstract class FileChannel extends AbstractInterruptibleChannel
        implements GatheringByteChannel, ScatteringByteChannel, ByteChannel {

    /**
     * A type of file mapping modes.
     * 
     */
    public static class MapMode {
        /**
         * Private mapping mode (equivalent to copy on write).
         */
        public static final MapMode PRIVATE = new MapMode("PRIVATE"); //$NON-NLS-1$

        /**
         * Read-only mapping mode.
         */
        public static final MapMode READ_ONLY = new MapMode("READ_ONLY"); //$NON-NLS-1$

        /**
         * Read-write mapping mode.
         */
        public static final MapMode READ_WRITE = new MapMode("READ_WRITE"); //$NON-NLS-1$

        // The string used to display the mapping mode.
        private final String displayName;

        /*
         * Private constructor prevents others creating new modes.
         */
        private MapMode(String displayName) {
            super();
            this.displayName = displayName;
        }

        /**
         * Returns a string version of the mapping mode useful for debugging
         * etc.
         * 
         * @return the mode string.
         */
        public String toString() {
            return displayName;
        }
    }

    /**
     * Protected default constructor.
     */
    protected FileChannel() {
        super();
    }

    /**
     * Request that all updates to the channel are committed to the storage
     * device.
     * <p>
     * When this method returns all modifications made to the platform file
     * underlying this channel will be committed to a local storage device. If
     * the file is not hosted locally, such as a networked file system, then
     * applications cannot be certain that the modifications have been
     * committed.
     * </p>
     * <p>
     * There are no assurances given that changes made to the file using methods
     * defined elsewhere will be committed. For example, changes made via a
     * mapped byte buffer may not be committed.
     * </p>
     * <p>
     * The <code>metadata</code> parameter indicated whether the update should
     * include the file's metadata such as last modification time, last access
     * time, etc. Note that passing <code>true</code> may invoke an underlying
     * write to the operating system (if the platform is maintaining metadata
     * such as last access time), even if the channel is opened read-only.
     * 
     * @param metadata
     *            true if the file metadata should be flushed in addition to the
     *            file content, and false otherwise.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws IOException
     *             some other problem occurred.
     */
    public abstract void force(boolean metadata) throws IOException;

    /**
     * Obtain an exclusive lock on this file.
     * <p>
     * This is a convenience method for acquiring a maximum length lock on a
     * file. It is equivalent to:
     * 
     * <pre>
     * fileChannel.lock(0L, Long.MAX_VALUE, false)
     * </pre>
     * 
     * @return the lock object representing the locked file area.
     * @throws ClosedChannelException
     *             the file channel is closed.
     * @throws NonWritableChannelException
     *             this channel was not opened for writing.
     * @throws OverlappingFileLockException
     *             Either a lock is already held that overlaps this lock
     *             request, or another thread is waiting to acquire a lock that
     *             will overlap with this request.
     * @throws FileLockInterruptionException
     *             The calling thread was interrupted while waiting to acquire
     *             the lock.
     * @throws AsynchronousCloseException
     *             The channel was closed while the calling thread was waiting
     *             to acquire the lock.
     * @throws IOException
     *             some other problem occurred obtaining the requested lock.
     */
    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }

    /**
     * Obtain a lock on a specified region of the file.
     * <p>
     * This is the blocking version of lock acquisition, see also the
     * <code>tryLock()</code> methods.
     * </p>
     * <p>
     * Attempts to acquire an overlapping lock region will fail. The attempt
     * will fail if the overlapping lock has already been obtained, or if
     * another thread is currently waiting to acquire the overlapping lock.
     * </p>
     * <p>
     * If the request is not for an overlapping lock, the thread calling this
     * method will block until the lock is obtained (likely by no contention or
     * another process releasing a lock), or this thread being interrupted or
     * the channel closed.
     * </p>
     * <p>
     * If the lock is obtained successfully then the FileLock object returned
     * represents the lock for subsequent operations on the locked region.
     * </p>
     * <p>
     * If the thread is interrupted while waiting for the lock, the thread is
     * set to the interrupted state, and throws a
     * <code>FileLockInterruptionException</code>. If the channel is closed
     * while the thread is waiting to obtain the lock then the thread throws a
     * <code>AsynchronousCloseException</code>.
     * </p>
     * <p>
     * There is no requirement for the position and size to be within the
     * current start and length of the file.
     * </p>
     * <p>
     * Some platforms do not support shared locks, and if a request is made for
     * a shared lock on such a platform this method will attempt to acquire an
     * exclusive lock instead. It is undefined whether the lock obtained is
     * advisory or mandatory.
     * </p>
     * 
     * @param position
     *            the starting position for the lock region
     * @param size
     *            the length of the lock, in bytes
     * @param shared
     *            a flag indicating whether an attempt should be made to acquire
     *            a shared lock.
     * @return the file lock object
     * @throws IllegalArgumentException
     *             if the parameters are invalid.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws OverlappingFileLockException
     *             if the requested region overlaps an existing lock or pending
     *             lock request.
     * @throws NonReadableChannelException
     *             if the channel is not open in read-mode and shared is true.
     * @throws NonWritableChannelException
     *             if the channel is not open in write mode and shared is false.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws FileLockInterruptionException
     *             if the thread is interrupted while in the state of waiting
     *             on the desired file lock.
     * @throws IOException
     *             if some other IO problem occurs.
     */
    public abstract FileLock lock(long position, long size, boolean shared)
            throws IOException;

    /**
     * Maps the file into memory.There can be three modes:Read-only,Read/write
     * and Private.
     * 
     * After mapping, the memory and the file channel do not affect each other.
     * 
     * Note : mapping a file into memory is usually expensive.
     * 
     * @param mode
     *            one of three modes to map
     * @param position
     *            the starting position of the file
     * @param size
     *            the size to map
     * @return the mapped byte buffer
     * 
     * @throws NonReadableChannelException
     *             If the file is not opened for reading but the given mode is
     *             "READ_ONLY"
     * @throws NonWritableChannelException
     *             If the file is not opened for writing but the mode is not
     *             "READ_ONLY"
     * @throws IllegalArgumentException
     *             If the given parameters of position and size are not correct
     * @throws IOException
     *             If any I/O error occurs
     */
    public abstract MappedByteBuffer map(FileChannel.MapMode mode,
            long position, long size) throws IOException;

    /**
     * Returns the current value of the file position pointer.
     * 
     * @return the current position as a positive integer number of bytes from
     *         the start of the file.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws IOException
     *             if some other IO problem occurs.
     */
    public abstract long position() throws IOException;

    /**
     * Sets the file position pointer to a new value.
     * <p>
     * The argument is the number of bytes counted from the start of the file.
     * The position cannot be set to a value that is negative. The new position
     * can be set beyond the current file size. If set beyond the current file
     * size, attempts to read will return end of file, and writes will succeed,
     * but fill-in the bytes between the current end of file and the position
     * with the required number of (unspecified) byte values.
     * 
     * @param offset
     *            the new file position, in bytes.
     * @return the receiver.
     * @throws IllegalArgumentException
     *             if the new position is negative.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws IOException
     *             if some other IO problem occurs.
     */
    public abstract FileChannel position(long offset) throws IOException;

    /**
     * Reads bytes from the channel into the given byte buffer.
     * <p>
     * The bytes are read starting at the current file position, and after some
     * number of bytes are read (up to the remaining number of bytes in the
     * buffer) the file position is increased by the number of bytes actually
     * read.
     * 
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     */
    public abstract int read(ByteBuffer buffer) throws IOException;

    /**
     * Reads bytes from the file channel into the given buffer starting from the
     * given file position.
     * <p>
     * The bytes are read starting at the given file position (up to the
     * remaining number of bytes in the buffer). The number of bytes actually
     * read is returned.
     * </p>
     * <p>
     * If the position is beyond the current end of file, then no bytes are
     * read.
     * </p>
     * <p>
     * Note that file position is unmodified by this method.
     * </p>
     * 
     * @param buffer
     *            the buffer to receive the bytes
     * @param position
     *            the (non-negative) position at which to read the bytes.
     * @return the number of bytes actually read.
     * @throws IllegalArgumentException
     *             if <code>position</code> is less than <code>-1</code>.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws NonReadableChannelException
     *             if the channel was not opened in read-mode.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             some other IO error occurred.
     */
    public abstract int read(ByteBuffer buffer, long position)
            throws IOException;

    /**
     * Reads bytes from the channel into all the given byte buffers.
     * <p>
     * The bytes are read starting at the current file position, and after some
     * number of bytes are read (up to the remaining number of bytes in all the
     * buffers) the file position is increased by the number of bytes actually
     * read.
     * </p>
     * <p>
     * This method behaves exactly like:
     * 
     * <pre>
     * read(buffers, 0, buffers.length);
     * </pre>
     * 
     * </p>
     * 
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[])
     */
    public final long read(ByteBuffer[] buffers) throws IOException {
        return read(buffers, 0, buffers.length);
    }

    /**
     * Reads bytes from the file channel into a subset of the given byte
     * buffers.
     * 
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[],
     *      int, int)
     */
    public abstract long read(ByteBuffer[] buffers, int start, int number)
            throws IOException;

    /**
     * Returns the size of the file underlying this channel, in bytes.
     * 
     * @return the size of the file in bytes.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws IOException
     *             if a problem occurs getting the size of the file.
     */
    public abstract long size() throws IOException;

    /**
     * Transfers bytes into this channel's file from the given readable byte
     * channel. It may be very efficient.
     * 
     * By invoking this method, it will read form the source channel and write
     * into the file channel.
     * 
     * Note: no guarantee whether all bytes may be transferred. And it does not
     * modify the position of the channel.
     * 
     * @param src
     *            the source channel to read
     * @param position
     *            the non-negative position to begin
     * @param count
     *            the non-negative bytes to be transferred
     * @return the number of bytes that are transferred.
     * 
     * @throws IllegalArgumentException
     *             If the parameters are not correct
     * @throws NonReadableChannelException
     *             If the source channel is not readable
     * @throws NonWritableChannelException
     *             If this channel is not writable
     * @throws ClosedChannelException
     *             If either channel has already been closed
     * @throws AsynchronousCloseException
     *             If either channel is closed by other threads during this operation
     * @throws ClosedByInterruptException
     *             If the thread is interrupted during this operation
     * @throws IOException
     *             If any I/O error occurs
     */
    public abstract long transferFrom(ReadableByteChannel src, long position,
            long count) throws IOException;

    /**
     * Transfers data from the file to the given channel. It may be very
     * efficient.
     * 
     * By invoking this method, it will read form the file and write into the
     * writable channel.
     * 
     * Note: no guarantee whether all bytes may be transfered.And it does not
     * modify the position of the channel.
     * 
     * @param position
     *            the non-negative position to begin
     * @param count
     *            the non-negative bytes to be transferred
     * @param target
     *            the target channel to write into
     * @return the number of bytes that were transferred.
     * 
     * @throws IllegalArgumentException
     *             If the parameters are not correct
     * @throws NonReadableChannelException
     *             If this channel is not readable
     * @throws NonWritableChannelException
     *             If the target channel is not writable
     * @throws ClosedChannelException
     *             If either channel has already been closed
     * @throws AsynchronousCloseException
     *             If either channel is closed by other threads during this
     *             operation
     * @throws ClosedByInterruptException
     *             If the thread is interrupted during this operation
     * @throws IOException
     *             If any I/O error occurs
     */
    public abstract long transferTo(long position, long count,
            WritableByteChannel target) throws IOException;

    /**
     * Truncates the file underlying this channel to a given size.
     * <p>
     * Any bytes beyond the given size are removed from the file. If there are
     * no bytes beyond the given size then the file contents are unmodified.
     * </p>
     * <p>
     * If the file position is currently greater than the given size, then it is
     * set to be the given size.
     * </p>
     * 
     * @param size
     *            the maximum size of the underlying file
     * @throws IllegalArgumentException
     *             the requested size is negative.
     * @throws ClosedChannelException
     *             the channel is closed.
     * @throws NonWritableChannelException
     *             the channel cannot be written.
     * @throws IOException
     *             some other IO problem occurred.
     * @return this channel
     */
    public abstract FileChannel truncate(long size) throws IOException;

    /**
     * Attempts to acquire an exclusive lock on this file without blocking.
     * <p>
     * This is a convenience method for attempting to acquire a maximum length
     * lock on the file. It is equivalent to:
     * 
     * <pre>
     * fileChannel.tryLock(0L, Long.MAX_VALUE, false)
     * </pre>
     * 
     * </p>
     * <p>
     * The method returns <code>null</code> if the acquisition would result in
     * an overlapped lock with another OS process.
     * </p>
     * 
     * @return the file lock object, or <code>null</code> if the lock would
     *         overlap an existing exclusive lock in another OS process.
     * @throws ClosedChannelException
     *             the file channel is closed.
     * @throws OverlappingFileLockException
     *             Either a lock is already held that overlaps this lock
     *             request, or another thread is waiting to acquire a lock that
     *             will overlap with this request.
     * @throws IOException
     *             if any I/O error occurs
     */
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

    /**
     * Attempts to acquire an exclusive lock on this file without blocking.
     * <p>
     * The method returns <code>null</code> if the acquisition would result in
     * an overlapped lock with another OS process.
     * </p>
     * 
     * @param position
     *            the starting position
     * @param size
     *            the size of file to lock
     * @param shared
     *            true if share
     * @return the file lock object, or <code>null</code> if the lock would
     *         overlap an existing exclusive lock in another OS process.
     * 
     * @throws IllegalArgumentException
     *             If any parameters are bad
     * @throws ClosedChannelException
     *             the file channel is closed.
     * @throws OverlappingFileLockException
     *             Either a lock is already held that overlaps this lock
     *             request, or another thread is waiting to acquire a lock that
     *             will overlap with this request.
     * @throws IOException
     *             if any I/O error occurs
     */
    public abstract FileLock tryLock(long position, long size, boolean shared)
            throws IOException;

    /**
     * Writes bytes from the given byte buffer into the file channel.
     * <p>
     * The bytes are written starting at the current file position, and after
     * some number of bytes are written (up to the remaining number of bytes in
     * the buffer) the file position is increased by the number of bytes
     * actually written.
     * 
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     * 
     * @param src
     *            the source buffer to write
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * Writes bytes from the given buffer to the file channel starting at the
     * given file position.
     * <p>
     * The bytes are written starting at the given file position (up to the
     * remaining number of bytes in the buffer). The number of bytes actually
     * written is returned.
     * </p>
     * <p>
     * If the position is beyond the current end of file, then the file is first
     * extended up to the given position by the required number of unspecified
     * byte values.
     * </p>
     * <p>
     * Note that file position is unmodified by this method.
     * </p>
     * 
     * @param buffer
     *            the buffer containing the bytes to be written.
     * @param position
     *            the (non-negative) position at which to write the bytes.
     * @return the number of bytes actually written.
     * @throws IllegalArgumentException
     *             if <code>position</code> is less than <code>-1</code>.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws NonWritableChannelException
     *             if the channel was not opened in write-mode.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             some other IO error occurred.
     */
    public abstract int write(ByteBuffer buffer, long position)
            throws IOException;

    /**
     * Writes bytes from all the given byte buffers into the file channel.
     * <p>
     * The bytes are written starting at the current file position, and after
     * some number of bytes are written (up to the remaining number of bytes in
     * all the buffers) the file position is increased by the number of bytes
     * actually written.
     * <p>
     * This method behaves exactly like:
     * 
     * <pre>
     * write(buffers, 0, buffers.length);
     * </pre>
     * 
     * </p>
     * 
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[])
     */
    public final long write(ByteBuffer[] buffers) throws IOException {
        return write(buffers, 0, buffers.length);
    }

    /**
     * 
     * 
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[],
     *      int, int)
     */
    public abstract long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException;

}

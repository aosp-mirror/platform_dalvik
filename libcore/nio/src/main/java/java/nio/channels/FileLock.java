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

// BEGIN android-note
//   - fixed bad htm in javadoc comments -joeo
// END android-note

/**
 * A <code>FileLock</code> represents a locked region of a file.
 * <p>
 * Locks have certain properties that enable collaborating processes to avoid
 * the lost update problem, or reading inconsistent data.
 * </p>
 * <p>
 * logically, a file lock can be 'exclusive' or 'shared'. Multiple processes can
 * hold shared locks on the same region of a file, but only a single process can
 * hold an exclusive lock on a given region of a file and no other process can
 * simultaneously hold a shared lock overlapping the exclusive lock. An
 * application can determine whether a FileLock is shared or exclusive via the
 * <code>isShared()</code> API.
 * </p>
 * <p>
 * Locks held by a particular process cannot overlap one another. Applications
 * can determine whether a proposed lock will overlap by using the
 * <code>overlaps(long, long)</code>) API.  Locks held in
 * other processes may overlap locks held in this process.</p>
 * <p>
 * Locks are shared amongst all threads in the acquiring process, and are therefore unsuitable for
 * intra-process synchronization.</p>
 * <p>
 * Once a lock is acquired it is immutable in all its state except <code>isValid()</code>.  The lock
 * will initially be valid, but may be rendered invalid by explicit removal of the lock, using <code>
 * release()</code>, or implicitly by closing the channel or exiting the process (terminating the JVM).</p>
 * <p>
 * <em>Platform dependencies</em></p>
 * <p>
 * Locks are intended to be true platform operating system file locks, and therefore locks held by the
 * JVM process will be visible to other OS processes.</p>
 * <p>
 * The characteristics of the underlying OS locks will show through in the Java implementation.  For
 * example, some platforms' locks are 'mandatory' -- meaning the operating system enforces the locks
 * on processes that attempt to access locked regions of file; whereas other platforms' locks are
 * only 'advisory' -- meaning that processes are required to collaborate on ensuring locks are acquired
 * and there is a potential for processes not to play well.  The only safe answer is to assume that
 * the platform is adopting advisory locks an always acquire shared locks when reading a region of file.</p>
 * <p>
 * On some platforms the presence of a lock will prevent the file being memory mapped.  On some platforms
 * closing a channel on a given file handle will release all the locks held on that file -- even if there
 * are other channels open on the same file (their locks will be released).  The safe option here is to
 * ensure that you only acquire locks on a single channel for a particular file, and that becomes the
 * synchronization point.</p>
 * <p>
 * Further care should be exercised when locking files maintained on network file systems since they often
 * have further limitations.</p>  
 *
 */
public abstract class FileLock {

    // The underlying file channel.
    private final FileChannel channel;

    // The lock starting position.
    private final long position;

    // The lock length in bytes
    private final long size;

    // If true then shared, if false then exclusive
    private final boolean shared;

    /**
     * Constructor for a new file lock instance for a given channel. The
     * constructor enforces the starting position, stretch, and shared status of
     * the lock.
     * 
     * @param channel
     *            underlying file channel that holds the lock.
     * @param position
     *            starting point for the lock.
     * @param size
     *            length of lock in number of bytes.
     * @param shared
     *            shared status of lock (true is shared, false is exclusive).
     */
    protected FileLock(FileChannel channel, long position, long size,
            boolean shared) {
        super();
        if (position < 0 || size < 0 || position + size < 0) {
            throw new IllegalArgumentException();
        }
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    /**
     * Returns the lock's FileChannel.
     * 
     * @return the channel.
     */
    public final FileChannel channel() {
        return channel;
    }

    /**
     * Returns the lock's starting position in the file.
     * 
     * @return the lock position.
     */
    public final long position() {
        return position;
    }

    /**
     * Returns the length of the file lock in bytes.
     * 
     * @return the size of file lock in bytes.
     */
    public final long size() {
        return size;
    }

    /**
     * Returns true if the file lock is shared with other processes and false if
     * it is not.
     * 
     * @return true if the lock is a shared lock, and false if it is exclusive.
     */
    public final boolean isShared() {
        return shared;
    }

    /**
     * Returns true if the receiver's lock region overlapps the region described
     * in the parameter list,and returns false otherwise.
     * 
     * @param start
     *            the starting position for the comparative lock.
     * @param length
     *            the length of the comparative lock.
     * @return true if there is an overlap, and false otherwise.
     */
    public final boolean overlaps(long start, long length) {
        final long end = position + size - 1;
        final long newEnd = start + length - 1;
        if (end < start || position > newEnd) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether the receiver is a valid file lock or not. The lock is
     * valid unless the underlying channel has been closed or it has been
     * explicitly released.
     * 
     * @return true if the lock is valid, and false otherwise.
     */
    public abstract boolean isValid();

    /**
     * Releases this particular lock on the file. If the lock is invalid then
     * this method has no effect. Once released the lock becomes invalid.
     * 
     * @throws ClosedChannelException
     *             if the channel is already closed when an attempt to release
     *             the lock is made.
     * @throws IOException
     *             some other IO exception occurred.
     */
    public abstract void release() throws IOException;

    /**
     * Returns a string that shows the details of the lock suitable for display
     * to an end user.
     * 
     * @return the display string.
     */
    public final String toString() {
        StringBuffer buffer = new StringBuffer(64); // Guess length of string
        buffer.append("FileLock: [position="); //$NON-NLS-1$
        buffer.append(position);
        buffer.append(", size="); //$NON-NLS-1$
        buffer.append(size);
        buffer.append(", shared="); //$NON-NLS-1$
        buffer.append(Boolean.toString(shared));
        buffer.append("]"); //$NON-NLS-1$
        return buffer.toString();
    }
}

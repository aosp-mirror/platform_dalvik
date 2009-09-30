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

// BEGIN android-note
// address length was changed from long to int for performance reasons.
// END android-note

package org.apache.harmony.luni.platform;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This is the portable implementation of the file system interface.
 *
 */
class OSFileSystem implements IFileSystem {

    private static final OSFileSystem singleton = new OSFileSystem();

    public static OSFileSystem getOSFileSystem() {
        return singleton;
    }

    private OSFileSystem() {
        super();
    }

    private final void validateLockArgs(int type, long start, long length) {
        if ((type != IFileSystem.SHARED_LOCK_TYPE)
                && (type != IFileSystem.EXCLUSIVE_LOCK_TYPE)) {
            throw new IllegalArgumentException("Illegal lock type requested."); //$NON-NLS-1$
        }

        // Start position
        if (start < 0) {
            throw new IllegalArgumentException(
                    "Lock start position must be non-negative"); //$NON-NLS-1$
        }

        // Length of lock stretch
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Lock length must be non-negative"); //$NON-NLS-1$
        }
    }

    private native int lockImpl(int fileDescriptor, long start, long length,
            int type, boolean wait);

    /**
     * Returns the granularity for virtual memory allocation.
     * Note that this value for Windows differs from the one for the
     * page size (64K and 4K respectively).
     */
    public native int getAllocGranularity();

    public boolean lock(int fileDescriptor, long start, long length, int type,
            boolean waitFlag) throws IOException {
        // Validate arguments
        validateLockArgs(type, start, length);
        int result = lockImpl(fileDescriptor, start, length, type, waitFlag);
        return result != -1;
    }

    // BEGIN android-changed
    private native void unlockImpl(int fileDescriptor, long start, long length) throws IOException;

    public void unlock(int fileDescriptor, long start, long length)
            throws IOException {
        // Validate arguments
        validateLockArgs(IFileSystem.SHARED_LOCK_TYPE, start, length);
        unlockImpl(fileDescriptor, start, length);
    }

    public native void fflush(int fileDescriptor, boolean metadata) throws IOException;

    /*
     * File position seeking.
     */
    public native long seek(int fd, long offset, int whence) throws IOException;

    /*
     * Direct read/write APIs work on addresses.
     */
    public native long readDirect(int fileDescriptor, int address, int offset, int length);

    public native long writeDirect(int fileDescriptor, int address, int offset, int length)
            throws IOException;

    /*
     * Indirect read/writes work on byte[]'s
     */
    private native long readImpl(int fileDescriptor, byte[] bytes, int offset,
            int length) throws IOException;

    public long read(int fileDescriptor, byte[] bytes, int offset, int length)
            throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        return readImpl(fileDescriptor, bytes, offset, length);
    }

    private native long writeImpl(int fileDescriptor, byte[] bytes,
            int offset, int length) throws IOException;

    public long write(int fileDescriptor, byte[] bytes, int offset, int length)
            throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        return writeImpl(fileDescriptor, bytes, offset, length);
    }
    // END android-changed

    /*
     * Scatter/gather calls.
     */
    public native long readv(int fileDescriptor, int[] addresses,
            int[] offsets, int[] lengths, int size) throws IOException;

    public native long writev(int fileDescriptor, int[] addresses, int[] offsets,
            int[] lengths, int size) throws IOException;

    // BEGIN android-changed
    public native void close(int fileDescriptor) throws IOException;

    public native void truncate(int fileDescriptor, long size) throws IOException;
    // END android-changed

    public int open(byte[] fileName, int mode) throws FileNotFoundException {
        if (fileName == null) {
            throw new NullPointerException();
        }
        int handler = openImpl(fileName, mode);
        if (handler < 0) {
            try {
                throw new FileNotFoundException(new String(fileName, "UTF-8"));
            } catch (java.io.UnsupportedEncodingException e) {
                // UTF-8 should always be supported, so throw an assertion
                FileNotFoundException fnfe = new FileNotFoundException(new String(fileName));
                e.initCause(fnfe);
                throw new AssertionError(e);
            }
        }
        return handler;
    }

    private native int openImpl(byte[] fileName, int mode);

    // BEGIN android-changed
    public native long transfer(int fd, FileDescriptor sd, long offset, long count)
            throws IOException;
    // END android-changed

    // BEGIN android-deleted
    // public long ttyAvailable() throws IOException {
    //     long nChar = ttyAvailableImpl();
    //     if (nChar < 0) {
    //         throw new IOException();
    //     }
    //     return nChar;
    // }
    //
    // private native long ttyAvailableImpl();
    // END android-deleted

    // BEGIN android-deleted
    // public long ttyRead(byte[] bytes, int offset, int length) throws IOException {
    //    if (bytes == null) {
    //        throw new NullPointerException();
    //    }
    //    return ttyReadImpl(bytes, offset, length);
    // }
    // private native long ttyReadImpl(byte[] bytes, int offset, int length) throws IOException;
    // END android-deleted

    // BEGIN android-added
    public native int ioctlAvailable(int fileDescriptor) throws IOException;
    // END android-added
}

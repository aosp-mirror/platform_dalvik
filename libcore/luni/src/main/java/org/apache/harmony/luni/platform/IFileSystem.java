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

package org.apache.harmony.luni.platform;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * TODO Type description
 * 
 */
public interface IFileSystem extends ISystemComponent {

    public final int SHARED_LOCK_TYPE = 1;

    public final int EXCLUSIVE_LOCK_TYPE = 2;

    public final int SEEK_SET = 1;

    public final int SEEK_CUR = 2;

    public final int SEEK_END = 4;

    public final int O_RDONLY = 0x00000000;

    public final int O_WRONLY = 0x00000001;

    public final int O_RDWR = 0x00000010;

    public final int O_RDWRSYNC = 0x00000020;
    
    public final int O_APPEND = 0x00000100;

    public final int O_CREAT = 0x00001000;

    public final int O_EXCL = 0x00010000;

    public final int O_NOCTTY = 0x00100000;

    public final int O_NONBLOCK = 0x01000000;

    public final int O_TRUNC = 0x10000000;

    public long read(int fileDescriptor, byte[] bytes, int offset, int length)
            throws IOException;

    public long write(int fileDescriptor, byte[] bytes, int offset, int length)
            throws IOException;

    public long readv(int fileDescriptor, int[] addresses, int[] offsets,
            int[] lengths, int size) throws IOException;

    public long writev(int fileDescriptor, int[] addresses, int[] offsets,
            int[] lengths, int size) throws IOException;

    // Required to support direct byte buffers
    public long readDirect(int fileDescriptor, int address, int offset,
            int length) throws IOException;

    public long writeDirect(int fileDescriptor, int address, int offset,
            int length) throws IOException;

    public boolean lock(int fileDescriptor, long start, long length, int type,
            boolean waitFlag) throws IOException;

    public void unlock(int fileDescriptor, long start, long length)
            throws IOException;

    public long seek(int fileDescriptor, long offset, int whence)
            throws IOException;

    public void fflush(int fileDescriptor, boolean metadata)
            throws IOException;

    public void close(int fileDescriptor) throws IOException;

    public void truncate(int fileDescriptor, long size) throws IOException;

    /**
     * Returns the granularity for virtual memory allocation.
     */
    public int getAllocGranularity() throws IOException;

    public int open(byte[] fileName, int mode) throws FileNotFoundException;

    public long transfer(int fileHandler, FileDescriptor socketDescriptor,
            long offset, long count) throws IOException;

    // BEGIN android-deleted
    // public long ttyAvailable() throws IOException;
    // END android-deleted
    
    public long ttyRead(byte[] bytes, int offset, int length) throws IOException;
    
    // BEGIN android-added
    public int ioctlAvailable(int fileDescriptor) throws IOException;
    // END android-added
    
}

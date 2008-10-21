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
 * FileDescriptor is the lowest level representation of a File, Device, or
 * Socket. You can create any of the IO classes which take a FileDescriptor as
 * an argument by querying an open Socket or File for the FileDescriptor.
 * <p>
 * The FileDescriptor class also contains static fields representing Standard
 * Input, Output and Error. You can use these directly if desired but it is
 * recommended you go through System.in, System.out, and System.err streams
 * respectively.
 * <p>
 * Applications should not create new FileDescriptors.
 * 
 * @see FileInputStream#getFD()
 * @see FileOutputStream#getFD()
 * @see RandomAccessFile#getFD()
 */
public final class FileDescriptor {

    /** FileDescriptor representing Standard In */
    public static final FileDescriptor in = new FileDescriptor();

    /** FileDescriptor representing Standard Out */
    public static final FileDescriptor out = new FileDescriptor();

    /** FileDescriptor representing Standard Error */
    public static final FileDescriptor err = new FileDescriptor();

    /**
     * Represents a link to any underlying OS resources for this FileDescriptor.
     * A value of -1 indicates that this FileDescriptor is invalid.
     */
    int descriptor = -1;
    
    boolean readOnly = false; 

    private static native void oneTimeInitialization();

    static {
        in.descriptor = 0;
        out.descriptor = 1;
        err.descriptor = 2;

        oneTimeInitialization();
    }

    /**
     * Constructs a new FileDescriptor containing an invalid handle. This
     * constructor does nothing interesting. Provided for signature
     * compatibility.
     */
    public FileDescriptor() {
        super();
    }

    /**
     * Ensures that data which is buffered within the underlying implementation
     * is written out to the appropriate device before returning.
     * 
     * @throws SyncFailedException
     *             when the operation fails
     */
    public void sync() throws SyncFailedException {
        // if the descriptor is a read-only one, do nothing
        if (!readOnly) {
            syncImpl();
        }
    }
    
    private native void syncImpl() throws SyncFailedException;

    /**
     * Returns a boolean indicating whether or not this FileDescriptor is valid.
     * 
     * @return <code>true</code> if this FileDescriptor is valid,
     *         <code>false</code> otherwise
     */
    public native boolean valid();
}

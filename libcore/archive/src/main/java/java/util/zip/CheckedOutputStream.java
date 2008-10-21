/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.util.zip;


import java.io.IOException;
import java.io.OutputStream;

/**
 * The CheckedOutputStream class is used to maintain a running Checksum of all
 * data written to a stream.
 */
public class CheckedOutputStream extends java.io.FilterOutputStream {

    private final Checksum check;

    /**
     * Constructs a new CheckedOutputStream on OutputStream os. The Checksum
     * will be calculated using the algorithm implemented by csum.
     * 
     * @param os
     *            OutputStream to calculate checksum from
     * @param cs
     *            Type of Checksum to calculate
     */
    public CheckedOutputStream(OutputStream os, Checksum cs) {
        super(os);
        check = cs;
    }

    /**
     * Returns the Checksum calculated on the stream thus far.
     * 
     * @return A java.util.zip.Checksum
     */
    public Checksum getChecksum() {
        return check;
    }

    /**
     * Writes byte value val to the underlying stream. The Checksum is updated
     * with val.
     * 
     * @param val
     *            Value of the byte to write out
     * 
     * @throws IOException
     *             if an IO error has occured
     */
    @Override
    public void write(int val) throws IOException {
        out.write(val);
        check.update(val);
    }

    /**
     * Writes nbytes of data from buf starting at offset off to the underlying
     * stream. The Checksum is updated with the bytes written.
     * 
     * @param buf
     *            data to write out
     * @param off
     *            the start offset of the data
     * @param nbytes
     *            number of bytes to write out
     * 
     * @throws IOException
     *             if an IO error has occured
     */
    @Override
    public void write(byte[] buf, int off, int nbytes) throws IOException {
        out.write(buf, off, nbytes);
        check.update(buf, off, nbytes);
    }
}

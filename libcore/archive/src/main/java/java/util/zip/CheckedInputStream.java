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
import java.io.InputStream;

/**
 * The CheckedInputStream class is used to maintain a running Checksum of all
 * data read from a stream.
 */
public class CheckedInputStream extends java.io.FilterInputStream {

    private final Checksum check;

    /**
     * Constructs a new CheckedInputStream on InputStream is. The Checksum will
     * be calculated using the algorithm implemented by csum.
     * 
     * @param is
     *            InputStream to calculate checksum from
     * @param csum
     *            Type of Checksum to calculate
     */
    public CheckedInputStream(InputStream is, Checksum csum) {
        super(is);
        check = csum;
    }

    /**
     * Reads a byte of data from the underlying stream and recomputes the
     * Checksum with the byte data.
     * 
     * @return -1 if end of stream, a single byte value otherwise
     */
    @Override
    public int read() throws IOException {
        int x = in.read();
        if (x != -1) {
            check.update(x);
        }
        return x;
    }

    /**
     * Reads up to nbytes of data from the underlying stream, storing it in buf,
     * starting at offset off. The Checksum is updated with the bytes read.
     * 
     * @return Number of bytes read, -1 if end of stream
     */
    @Override
    public int read(byte[] buf, int off, int nbytes) throws IOException {
        int x = in.read(buf, off, nbytes);
        if (x != -1) {
            check.update(buf, off, x);
        }
        return x;
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
     * Skip upto nbytes of data on the underlying stream. Any skipped bytes are
     * added to the running Checksum value.
     * 
     * @param nbytes
     *            long Number of bytes to skip
     * @return Number of bytes skipped
     */
    @Override
    public long skip(long nbytes) throws IOException {
        if (nbytes < 1) {
            return 0;
        }
        long skipped = 0;
        byte[] b = new byte[2048];
        int x, v;
        while (skipped != nbytes) {
            x = in.read(b, 0,
                    (v = (int) (nbytes - skipped)) > b.length ? b.length : v);
            if (x == -1) {
                return skipped;
            }
            check.update(b, 0, x);
            skipped += x;
        }
        return skipped;
    }
}

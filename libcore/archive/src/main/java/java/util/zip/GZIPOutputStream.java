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
 * The {@code GZIPOutputStream} class is used to write data to a stream in the
 * GZIP storage format.
 * 
 * @since Android 1.0
 */
public class GZIPOutputStream extends DeflaterOutputStream {

    /**
     * The checksum algorithm used when treating uncompressed data.
     * 
     * @since Android 1.0
     */
    protected CRC32 crc = new CRC32();

    /**
     * Construct a new {@code GZIPOutputStream} to write data in GZIP format to
     * the underlying stream.
     * 
     * @param os
     *            the {@code OutputStream} to write data to.
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    public GZIPOutputStream(OutputStream os) throws IOException {
        this(os, BUF_SIZE);
    }

    /**
     * Construct a new {@code GZIPOutputStream} to write data in GZIP format to
     * the underlying stream. Set the internal compression buffer to size
     * {@code size}.
     * 
     * @param os
     *            the {@code OutputStream} to write to.
     * @param size
     *            the internal buffer size.
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    public GZIPOutputStream(OutputStream os, int size) throws IOException {
        super(os, new Deflater(Deflater.DEFAULT_COMPRESSION, true), size);
        writeShort(GZIPInputStream.GZIP_MAGIC);
        out.write(Deflater.DEFLATED);
        out.write(0); // flags
        writeLong(0); // mod time
        out.write(0); // extra flags
        out.write(0); // operating system
    }

    /**
     * Indicates to the stream that all data has been written out, and any GZIP
     * terminal data can now be written.
     * 
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    @Override
    public void finish() throws IOException {
        super.finish();
        writeLong(crc.getValue());
        writeLong(crc.tbytes);
    }

    @Override
    public void write(byte[] buffer, int off, int nbytes) throws IOException {
        super.write(buffer, off, nbytes);
        crc.update(buffer, off, nbytes);
    }

    private int writeShort(int i) throws IOException {
        out.write(i & 0xFF);
        out.write((i >> 8) & 0xFF);
        return i;
    }

    private long writeLong(long i) throws IOException {
        // Write out the long value as an unsigned int
        out.write((int) (i & 0xFF));
        out.write((int) (i >> 8) & 0xFF);
        out.write((int) (i >> 16) & 0xFF);
        out.write((int) (i >> 24) & 0xFF);
        return i;
    }
}

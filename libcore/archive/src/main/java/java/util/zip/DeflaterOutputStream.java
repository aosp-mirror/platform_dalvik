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


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.harmony.archive.internal.nls.Messages;

/**
 * The DeflaterOutputStream class implements a stream filter for the writing of
 * compressed data to a stream. Compression is performed by an instance of
 * Deflater.
 */
public class DeflaterOutputStream extends FilterOutputStream {
	static final int BUF_SIZE = 512;

	protected byte[] buf;

	protected Deflater def;

	boolean done = false;

	/**
	 * Constructs a new DeflaterOutputStream instance using os as the underlying
	 * stream. The provided Deflater instance will be used to compress data.
	 * 
	 * @param os
	 *            OutputStream to receive compressed data
	 * @param def
	 *            Deflater to perform compression
	 */
	public DeflaterOutputStream(OutputStream os, Deflater def) {
		this(os, def, BUF_SIZE);
	}

	/**
	 * Constructs a new DeflaterOutputStream instance using os as the underlying
	 * stream.
	 * 
	 * @param os
	 *            OutputStream to receive compressed data
	 */
	public DeflaterOutputStream(OutputStream os) {
		this(os, new Deflater());
	}

	/**
	 * Constructs a new DeflaterOutputStream instance using os as the underlying
	 * stream. The provided Deflater instance will be used to compress data. The
	 * internal buffer for storing compressed data will be of size bsize.
	 * 
	 * @param os
	 *            OutputStream to receive compressed data
	 * @param def
	 *            Deflater to perform compression
	 * @param bsize
	 *            size of internal compression buffer
	 */
	public DeflaterOutputStream(OutputStream os, Deflater def, int bsize) {
		super(os);
		if (os == null || def == null) {
            throw new NullPointerException();
        }
		if (bsize <= 0) {
            throw new IllegalArgumentException();
        }
		this.def = def;
		buf = new byte[bsize];
	}

	/**
	 * Compress the data in the input buffer and write it to the underlying
	 * stream.
	 * 
	 * @exception java.io.IOException
	 *                If an error occurs during deflation.
	 */
	protected void deflate() throws IOException {
		int x = 0;
		do {
			x = def.deflate(buf);
			out.write(buf, 0, x);
		} while (!def.needsInput());
	}

	/**
	 * Writes any unwritten compressed data to the underlying stream, the closes
	 * all underlying streams. This stream can no longer be used after close()
	 * has been called.
	 * 
	 * @exception java.io.IOException
	 *                If an error occurs during close.
	 */
	@Override
    public void close() throws IOException {
		if (!def.finished()) {
            finish();
        }
		def.end();
		out.close();
	}

	/**
	 * Write any unwritten data to the underlying stream. Do not close the
	 * stream. This allows subsequent Deflater's to write to the same stream.
	 * This Deflater cannot be used again.
	 * 
	 * @exception java.io.IOException
	 *                If an error occurs.
	 */
	public void finish() throws IOException {
		if (done) {
            return;
        }
		def.finish();
		int x = 0;
		while (!def.finished()) {
			if (def.needsInput()) {
                def.setInput(buf, 0, 0);
            }
			x = def.deflate(buf);
			out.write(buf, 0, x);
		}
		done = true;
	}

	@Override
    public void write(int i) throws IOException {
		byte[] b = new byte[1];
		b[0] = (byte) i;
		write(b, 0, 1);
	}

	/**
	 * Compress nbytes of data from buf starting at off and write it to the
	 * underlying stream.
	 * 
	 * @param buffer
	 *            Buffer of data to compress
	 * @param off
	 *            offset in buffer to extract data from
	 * @param nbytes
	 *            Number of bytes of data to compress and write
	 * 
	 * @exception java.io.IOException
	 *                If an error occurs during writing.
	 */
	@Override
    public void write(byte[] buffer, int off, int nbytes) throws IOException {
		if (done) {
            throw new IOException(Messages.getString("archive.26")); //$NON-NLS-1$
        }
		// avoid int overflow, check null buf
		if (off <= buffer.length && nbytes >= 0 && off >= 0
				&& buffer.length - off >= nbytes) {
			if (!def.needsInput()) {
                throw new IOException();
            }
			def.setInput(buffer, off, nbytes);
			deflate();
		} else {
            throw new ArrayIndexOutOfBoundsException();
        }
	}
}

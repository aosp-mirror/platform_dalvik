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


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.harmony.archive.internal.nls.Messages;

/**
 * The GZIPInputStream class is used to read data stored in the GZIP format.
 */
public class GZIPInputStream extends java.util.zip.InflaterInputStream {
	protected CRC32 crc = new CRC32();

	protected boolean eos = false;

	public final static int GZIP_MAGIC = 0x8b1f;

	private static final int FHCRC = 2;

	private static final int FEXTRA = 4;

	private static final int FNAME = 8;

	private static final int FCOMMENT = 16;

	/**
	 * Construct a GZIPInputStream to read from GZIP data from the underlying
	 * stream
	 * 
	 * @param is
	 *            InputStream to read data from
	 */
	public GZIPInputStream(InputStream is) throws IOException {
		this(is, BUF_SIZE);
	}

	/**
	 * Construct a GZIPInputStream to read from GZIP data from the underlying
	 * stream. Set the internal buffer size to size
	 * 
	 * @param is
	 *            InputStream to read data from
	 * @param size
	 *            Internal read buffer size
	 */
	public GZIPInputStream(InputStream is, int size) throws IOException {
		super(is, new Inflater(true), size);
		byte[] header = new byte[10];
		readFully(header, 0, header.length);
		if (getShort(header, 0) != GZIP_MAGIC) {
            throw new IOException(Messages.getString("archive.1F")); //$NON-NLS-1$;
        }
		int flags = header[3];
		boolean hcrc = (flags & FHCRC) != 0;
		if (hcrc) {
            crc.update(header, 0, header.length);
        }
		if ((flags & FEXTRA) != 0) {
			readFully(header, 0, 2);
			if (hcrc) {
                crc.update(header, 0, 2);
            }
			int length = getShort(header, 0);
			while (length > 0) {
				int max = length > buf.length ? buf.length : length;
				int result = in.read(buf, 0, max);
				if (result == -1) {
                    throw new EOFException();
                }
				if (hcrc) {
                    crc.update(buf, 0, result);
                }
				length -= result;
			}
		}
		if ((flags & FNAME) != 0) {
            readZeroTerminated(hcrc);
        }
		if ((flags & FCOMMENT) != 0) {
            readZeroTerminated(hcrc);
        }
		if (hcrc) {
			readFully(header, 0, 2);
			int crc16 = getShort(header, 0);
			if ((crc.getValue() & 0xffff) != crc16) {
                throw new IOException(Messages.getString("archive.20")); //$NON-NLS-1$
            }
			crc.reset();
		}
	}

	private long getLong(byte[] buffer, int off) {
		long l = 0;
		l |= (buffer[off] & 0xFF);
		l |= (buffer[off + 1] & 0xFF) << 8;
		l |= (buffer[off + 2] & 0xFF) << 16;
		l |= ((long) (buffer[off + 3] & 0xFF)) << 24;
		return l;
	}

	private int getShort(byte[] buffer, int off) {
		return (buffer[off] & 0xFF) | ((buffer[off + 1] & 0xFF) << 8);
	}

	/**
	 * Reads and decompresses GZIP data from the underlying stream into buf.
	 * 
	 * @param buffer
	 *            Buffer to receive data
	 * @param off
	 *            Offset in buffer to store data
	 * @param nbytes
	 *            Number of bytes to read
	 */
	@Override
    public int read(byte[] buffer, int off, int nbytes) throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); //$NON-NLS-1$
        }
        if(eof){
            return -1;
        }
		// avoid int overflow, check null buffer
		if (off <= buffer.length && nbytes >= 0 && off >= 0
				&& buffer.length - off >= nbytes) {
			int val = super.read(buffer, off, nbytes);
			if (val != -1) {
                crc.update(buffer, off, val);
            } else if (!eos) {
				eos = true;
				// Get non-compressed bytes read by fill
				
// BEGIN android-removed
//				int size = 0;
//				byte[] b = new byte[8];
//				int inB = inf.getTotalIn();
//				if (inB > 0) {
//					int diff = inB % buf.length;
//					if (diff != 0 || len != buf.length) {
//						size = len - diff;
//						if (size > b.length) {
//                            size = b.length;
//                        }
//						System.arraycopy(buf, diff, b, 0, size);
//					}
//				}
//				readFully(b, size, b.length - size);
// END android-removed
				
// BEGIN android-added (from newer Harmony)
                int size = inf.getRemaining();
                final int trailerSize = 8; // crc (4 bytes) + total out (4
                                            // bytes)
                byte[] b = new byte[trailerSize];
                int copySize = (size > trailerSize) ? trailerSize : size;

                System.arraycopy(buf, len - size, b, 0, copySize);
                readFully(b, copySize, trailerSize - copySize);				
// END android-added (from newer Harmony)
				
				if (getLong(b, 0) != crc.getValue()) {
                    throw new IOException(Messages.getString("archive.20")); //$NON-NLS-1$
                }
				if ((int) getLong(b, 4) != inf.getTotalOut()) {
                    throw new IOException(Messages.getString("archive.21")); //$NON-NLS-1$
                }
			}
			return val;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Closes this stream and any underlying streams.
	 */
	@Override
    public void close() throws IOException {
		eos = true;
		super.close();
	}

	private void readFully(byte[] buffer, int offset, int length)
			throws IOException {
		int result;
		while (length > 0) {
			result = in.read(buffer, offset, length);
			if (result == -1) {
                throw new EOFException();
            }
			offset += result;
			length -= result;
		}
	}

	private void readZeroTerminated(boolean hcrc) throws IOException {
		int result;
		while ((result = in.read()) > 0) {
			if (hcrc) {
                crc.update(result);
            }
		}
		if (result == -1) {
            throw new EOFException();
        }
		// Add the zero
		if (hcrc) {
            crc.update(result);
        }
	}
}

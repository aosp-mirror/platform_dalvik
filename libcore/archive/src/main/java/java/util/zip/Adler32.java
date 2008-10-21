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


/**
 * The Adler32 class is used to compute the Adler32 Checksum from a set of data.
 */
public class Adler32 implements java.util.zip.Checksum {

	private long adler = 1;

	/**
	 * Returns the Adler32 checksum for all input received
	 * 
	 * @return The checksum for this instance
	 */
	public long getValue() {
		return adler;
	}

	/**
	 * Reset this instance to its initial checksum
	 */
	public void reset() {
		adler = 1;
	}

	/**
	 * Update this Adler32 checksum using val.
	 * 
	 * @param i
	 *            byte to update checksum with
	 */
	public void update(int i) {
		adler = updateByteImpl(i, adler);
	}

	/**
	 * Update this Adler32 checksum using the contents of buf.
	 * 
	 * @param buf
	 *            bytes to update checksum with
	 */
	public void update(byte[] buf) {
		update(buf, 0, buf.length);
	}

	/**
	 * Update this Adler32 checksum with the contents of buf, starting from
	 * offset and using nbytes of data.
	 * 
	 * @param buf
	 *            buffer to obtain dat from
	 * @param off
	 *            offset i buf to copy from
	 * @param nbytes
	 *            number of bytes from buf to use
	 */
	public void update(byte[] buf, int off, int nbytes) {
		// avoid int overflow, check null buf
		if (off <= buf.length && nbytes >= 0 && off >= 0
				&& buf.length - off >= nbytes) {
            adler = updateImpl(buf, off, nbytes, adler);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
	}

	private native long updateImpl(byte[] buf, int off, int nbytes, long adler1);

	private native long updateByteImpl(int val, long adler1);
}

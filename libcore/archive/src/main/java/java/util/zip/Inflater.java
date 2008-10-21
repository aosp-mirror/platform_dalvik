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

import org.apache.harmony.archive.internal.nls.Messages;

import java.io.FileDescriptor;


/**
 * The Inflater class is used to decompress bytes using the DEFLATE compression
 * algorithm. Inflation is performed by the ZLIB compression library.
 * 
 * @see DeflaterOutputStream
 * @see Inflater
 */
public class Inflater {

	private boolean finished; // Set by the inflateImpl native

	private boolean needsDictionary; // Set by the inflateImpl native

	private long streamHandle = -1;

	int inRead;
    
    int inLength;

	// Fill in the JNI id caches
	private static native void oneTimeInitialization();

	static {
		oneTimeInitialization();
	}
    
    private static final byte MAGIC_NUMBER = 120;
    private boolean gotFirstByte = false;
    private boolean pass_magic_number_check = true;
    
	/**
	 * Release any resources associated with this Inflater. Any unused
	 * input/output is discarded. This is also called by the finalize method.
	 */
	public synchronized void end() {
		if (streamHandle != -1) {
			endImpl(streamHandle);
			inRead = 0;
			inLength = 0;
			streamHandle = -1;
		}
	}

	private native synchronized void endImpl(long handle);

	@Override
    protected void finalize() {
		end();
	}

	/**
	 * Indicates if the Inflater has inflated the entire deflated stream. If
	 * deflated bytes remain and needsInput returns true this method will return
	 * false. This method should be called after all deflated input is supplied
	 * to the Inflater.
	 * 
	 * @return True if all input has been inflated, false otherwise
	 */
	public synchronized boolean finished() {
		return finished;
	}

	/**
	 * Returns the Adler32 checksum of either all bytes inflated, or the
	 * checksum of the preset dictionary if one has been supplied.
	 * 
	 * @return The Adler32 checksum associated with this Inflater.
	 */
	public synchronized int getAdler() {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
		return getAdlerImpl(streamHandle);
	}

	private native synchronized int getAdlerImpl(long handle);

	/**
	 * Returns the number of bytes of current input remaining to be read by the
	 * inflater
	 * 
	 * @return Number of bytes of unread input.
	 */
	public synchronized int getRemaining() {
		return inLength - inRead;
	}

	/**
	 * Returns total number of bytes of input read by the Inflater.
	 * 
	 * @return Total bytes read
	 */
	public synchronized int getTotalIn() {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
		long totalIn = getTotalInImpl(streamHandle);
		return (totalIn <= Integer.MAX_VALUE ? (int) totalIn
				: Integer.MAX_VALUE);
	}

	private synchronized native long getTotalInImpl(long handle);

	/**
	 * Returns total number of bytes of input output by the Inflater.
	 * 
	 * @return Total bytes output
	 */
	public synchronized int getTotalOut() {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
		long totalOut = getTotalOutImpl(streamHandle);
		return (totalOut <= Integer.MAX_VALUE ? (int) totalOut
				: Integer.MAX_VALUE);
	}

	private native synchronized long getTotalOutImpl(long handle);

	/**
	 * Inflates bytes from current input and stores them in buf.
	 * 
	 * @param buf
	 *            Buffer to output inflated bytes
	 * @return Number of bytes inflated
	 * @exception DataFormatException
	 *                If the underlying stream is corrupted or was not DEFLATED
	 * 
	 */
	public int inflate(byte[] buf) throws DataFormatException {
		return inflate(buf, 0, buf.length);
	}

	/**
	 * Inflates up to nbytes bytes from current input and stores them in buf
	 * starting at off.
	 * 
	 * @param buf
	 *            Buffer to output inflated bytes
	 * @param off
	 *            Offset in buffer into which to store inflated bytes
	 * @param nbytes
	 *            Number of inflated bytes to store
	 * @exception DataFormatException
	 *                If the underlying stream is corrupted or was not DEFLATED
	 * @return Number of bytes inflated
	 */
	public synchronized int inflate(byte[] buf, int off, int nbytes)
			throws DataFormatException {
		// avoid int overflow, check null buf
		if (off <= buf.length && nbytes >= 0 && off >= 0
				&& buf.length - off >= nbytes) {
            if (nbytes == 0)
                return 0;

			if (streamHandle == -1) {
                throw new IllegalStateException();
            }
            
            if (!pass_magic_number_check) {
                throw new DataFormatException();
            }

            if (needsInput()) {
                return 0;
            }
            
			boolean neededDict = needsDictionary;
			needsDictionary = false;
			int result = inflateImpl(buf, off, nbytes, streamHandle);
			if (needsDictionary && neededDict) {
                throw new DataFormatException(Messages.getString("archive.27")); //$NON-NLS-1$
            }
			return result;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	private native synchronized int inflateImpl(byte[] buf, int off,
			int nbytes, long handle);

	/**
	 * Constructs a new Inflater instance.
	 */
	public Inflater() {
		this(false);
	}

	/**
	 * Constructs a new Inflater instance. If noHeader is true the Inflater will
	 * not attempt to read a ZLIB header.
	 * 
	 * @param noHeader
	 *            If true, read a ZLIB header from input.
	 */
	public Inflater(boolean noHeader) {
		streamHandle = createStream(noHeader);
	}

	/**
	 * Indicates whether the input bytes were compressed with a preset
	 * dictionary. This method should be called prior to inflate() to determine
	 * if a dictionary is required. If so setDictionary() should be called with
	 * the appropriate dictionary prior to calling inflate().
	 * 
	 * @return true if a preset dictionary is required for inflation.
	 * @see #setDictionary(byte[])
	 * @see #setDictionary(byte[], int, int)
	 */
	public synchronized boolean needsDictionary() {
		return needsDictionary;
	}

	public synchronized boolean needsInput() {
		return inRead == inLength;
	}

	/**
	 * Resets the Inflater.
	 */
	public synchronized void reset() {
		if (streamHandle == -1) {
            throw new NullPointerException();
        }
		finished = false;
		needsDictionary = false;
		inLength = inRead = 0;
		resetImpl(streamHandle);
	}

	private native synchronized void resetImpl(long handle);

	/**
	 * Sets the preset dictionary to be used for inflation to buf.
	 * needsDictionary() can be called to determine whether the current input
	 * was deflated using a preset dictionary.
	 * 
	 * @param buf
	 *            The buffer containing the dictionary bytes
	 * @see #needsDictionary
	 */
	public synchronized void setDictionary(byte[] buf) {
		setDictionary(buf, 0, buf.length);
	}

	public synchronized void setDictionary(byte[] buf, int off, int nbytes) {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
		// avoid int overflow, check null buf
		if (off <= buf.length && nbytes >= 0 && off >= 0
				&& buf.length - off >= nbytes) {
            setDictionaryImpl(buf, off, nbytes, streamHandle);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
	}

	private native synchronized void setDictionaryImpl(byte[] buf, int off,
			int nbytes, long handle);

	/**
	 * Sets the current input to buf. This method should only be called if
	 * needsInput() returns true.
	 * 
	 * @param buf
	 *            input buffer
	 * @see #needsInput
	 */
	public synchronized void setInput(byte[] buf) {
		setInput(buf, 0, buf.length);
	}

	/**
	 * Sets the current input to the region of buf starting at off and ending at
	 * nbytes - 1. This method should only be called if needsInput() returns
	 * true.
	 * 
	 * @param buf
	 *            input buffer
	 * @param off
	 *            offset to read from in buffer
	 * @param nbytes
	 *            number of bytes to read
	 * @see #needsInput
	 */
	public synchronized void setInput(byte[] buf, int off, int nbytes) {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
		// avoid int overflow, check null buf
		if (off <= buf.length && nbytes >= 0 && off >= 0
				&& buf.length - off >= nbytes) {
			inRead = 0;
			inLength = nbytes;
			setInputImpl(buf, off, nbytes, streamHandle);
		} else {
            throw new ArrayIndexOutOfBoundsException();
        }

// BEGIN android-added
// Note: pass_magic_number_check is set to false when setInput is called
//       for the first time and for a single byte.
//       Since setInput is called only by InflaterInputStream.fill with an
//       arbitrary byte len this check seems quite useless.
// FIXME: We should find out whether the first byte has to be the magic number
//        in all cases and correct the check as well as place it in
//        setFileInput accordingly.
//        And at a first glance it doesn't look like the first byte has to be 120.
// END android-added
		if(!gotFirstByte && nbytes>0)
        {
           pass_magic_number_check = (buf[off] == MAGIC_NUMBER || nbytes > 1);           
           gotFirstByte = true;
        }
	}


    /**
     * Sets the current input to the region within a file starting at off and ending at
     * nbytes - 1. This method should only be called if needsInput() returns
     * true.
     * 
     * @param file
     *            input file
     * @param off
     *            offset to read from in buffer
     * @param nbytes
     *            number of bytes to read
     * @see #needsInput
     */
    synchronized int setFileInput(FileDescriptor fd, long off, int nbytes) {
        if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        inRead = 0;
        inLength = setFileInputImpl(fd, off, nbytes, streamHandle);
        return inLength;
    }

	/**
	 * Returns a long int of total number of bytes of input read by the
	 * Inflater.
	 * This method performs the same as getTotalIn except it returns a 
	 * long value instead of an integer
	 * 
	 * @return Total bytes read
	 */
	public synchronized long getBytesRead() {
		// Throw NPE here
		if (streamHandle == -1) {
            throw new NullPointerException();
        }
		return getTotalInImpl(streamHandle);
	}

	/**
	 * Returns a long int of total number of bytes of input output by the
	 * Inflater.
	 * This method performs the same as getTotalOut except it returns a 
	 * long value instead of an integer
	 * 
	 * @return Total bytes output
	 */
	public synchronized long getBytesWritten() {
		// Throw NPE here
		if (streamHandle == -1) {
            throw new NullPointerException();
        }
		return getTotalOutImpl(streamHandle);
	}

	private native synchronized void setInputImpl(byte[] buf, int off,
			int nbytes, long handle);

    private native synchronized int setFileInputImpl(FileDescriptor fd, long off,
            int nbytes, long handle);

	private native long createStream(boolean noHeader1);
}

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
 * The Deflater class is used to compress bytes using the DEFLATE compression
 * algorithm. Deflation is performed by the ZLIB compression library.
 * 
 * @see DeflaterOutputStream
 * @see Inflater
 */
public class Deflater {

	public static final int BEST_COMPRESSION = 9;

	public static final int BEST_SPEED = 1;

	public static final int DEFAULT_COMPRESSION = -1;

	public static final int DEFAULT_STRATEGY = 0;

	public static final int DEFLATED = 8;

	public static final int FILTERED = 1;

	public static final int HUFFMAN_ONLY = 2;

	public static final int NO_COMPRESSION = 0;

	private static final int Z_NO_FLUSH = 0;

	private static final int Z_FINISH = 4;
    
    // Fill in the JNI id caches
    private static native void oneTimeInitialization();
    
    // A stub buffer used when deflate() called while inputBuffer has not been set.
    private static final byte[] STUB_INPUT_BUFFER = new byte[0];

    static {
        oneTimeInitialization();
    }

	private int flushParm = Z_NO_FLUSH;

	private boolean finished;

	private int compressLevel = DEFAULT_COMPRESSION;

	private int strategy = DEFAULT_STRATEGY;

	private long streamHandle = -1;

	private byte[] inputBuffer;

	private int inRead;
    
    private int inLength;
    
    /**
     * Constructs a new Deflater instance with default compression level and
     * strategy.
     */
    public Deflater() {
        this(DEFAULT_COMPRESSION, false);
    }
    
    /**
     * Constructs a new Deflater instance with compression level level and
     * default compression strategy. THe compression level provided must be
     * between 0 and 9.
     * 
     * @param level
     *            the compression level to use
     */
    public Deflater(int level) {
        this(level, false);
    }

    /**
     * Constructs a new Deflater instance with compression level level and
     * default compression strategy. If the noHeader parameter is specified then
     * no ZLIB header will be written as part of the compressed output. The
     * compression level specified must be between 0 and 9.
     * 
     * @param level
     *            the compression level to use
     * @param noHeader
     *            if true do not write the ZLIB header
     */
    public Deflater(int level, boolean noHeader) {
        super();
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw new IllegalArgumentException();
        }
        compressLevel = level;
        streamHandle = createStream(compressLevel, strategy, noHeader);
    }

	/**
	 * Deflates data into the supplied buffer
	 * 
	 * @param buf
	 *            buffer to store compressed data
	 * 
	 * @return number of bytes of compressed data stored
	 * 
	 */
	public int deflate(byte[] buf) {
		return deflate(buf, 0, buf.length);
	}

	/**
	 * Deflates data into the supplied buffer using the region from off to
	 * nbytes - 1.
	 * 
	 * @param buf
	 *            buffer to store compressed data
	 * @param off
	 *            offset inf buf to start storing data
	 * @param nbytes
	 *            number of bytes of compressed data to store in buf
	 * 
	 * @return number of bytes of compressed data stored
	 * 
	 */
	public synchronized int deflate(byte[] buf, int off, int nbytes) {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
        // avoid int overflow, check null buf
        if (off <= buf.length && nbytes >= 0 && off >= 0
                && buf.length - off >= nbytes) {
            // put a stub buffer, no effect.
            if (null == inputBuffer) {
                setInput(STUB_INPUT_BUFFER);
            }
            return deflateImpl(buf, off, nbytes, streamHandle, flushParm);
        }
		throw new ArrayIndexOutOfBoundsException();
	}

	private synchronized native int deflateImpl(byte[] buf, int off,
			int nbytes, long handle, int flushParm1);

	private synchronized native void endImpl(long handle);

	/**
	 * Frees all resources held onto by this Deflater. Any unused input or output
	 * is discarded. This is also called from the finalize method.
	 * 
	 * @see #finalize
	 */
	public synchronized void end() {
		if (streamHandle != -1) {
			endImpl(streamHandle);
			inputBuffer = null;
			streamHandle = -1;
		}
	}

	@Override
    protected void finalize() {
		end();
	}

	/**
	 * Indicates to the Deflater that all uncompressed input has been provided
	 * to it.
	 * 
	 * @see #finished
	 */
	public synchronized void finish() {
		flushParm = Z_FINISH;
	}

	/**
	 * Returns whether or not all provided data has been successfully
	 * compressed.
	 * 
	 * @return true if all data has been compressed, false otherwise
	 */
	public synchronized boolean finished() {
		return finished;
	}

	/**
	 * Returns the Adler32 checksum of uncompressed data currently read. If a
	 * preset dictionary is used getAdler() will return the Adler32 checksum of
	 * the dictionary used.
	 * 
	 * @return The Adler32 checksum of uncompressed data or preset dictionary if
	 *         used
	 * 
	 * @see #setDictionary(byte[])
	 * @see #setDictionary(byte[], int, int)
	 */
	public synchronized int getAdler() {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }

		return getAdlerImpl(streamHandle);
	}

	private synchronized native int getAdlerImpl(long handle);

	/**
	 * Returns the total number of bytes of input consumed by the deflater.
	 * 
	 * @return number of bytes of input read.
	 */
	public synchronized int getTotalIn() {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }

		return (int)getTotalInImpl(streamHandle);
	}

	private synchronized native long getTotalInImpl(long handle);

	/**
	 * Returns the total number of compressed bytes output by this Deflater.
	 * 
	 * @return number of compressed bytes output.
	 */
	public synchronized int getTotalOut() {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }

		return (int)getTotalOutImpl(streamHandle);
	}

	private synchronized native long getTotalOutImpl(long handle);

	/**
	 * Indicates whether or not all bytes of uncompressed input have been
	 * consumed by the Deflater. If needsInput() returns true setInput() must be
	 * called before deflation can continue. If all bytes of uncompressed data
	 * have been provided to the Deflater finish() must be called to ensure the
	 * compressed data is output.
	 * 
	 * @return True if input is required for deflation to continue, false
	 *         otherwise
	 * @see #finished()
	 * @see #setInput(byte[])
	 * @see #setInput(byte[], int, int)
	 */
	public synchronized boolean needsInput() {
		if (inputBuffer == null) {
            return true;
        }
		return inRead == inLength;
	}

	/**
	 * Resets the <code>Deflater</code> to accept new input without affecting
	 * any previously made settings for the compression strategy or level. This
	 * operation <i>must</i> be called after <code>finished()</code> returns
	 * <code>true</code> if the <code>Deflater</code> is to be reused.
	 * 
	 * @see #finished
	 */
	public synchronized void reset() {
		if (streamHandle == -1) {
            throw new NullPointerException();
        }

		flushParm = Z_NO_FLUSH;
		finished = false;
		resetImpl(streamHandle);
		inputBuffer = null;
	}

	private synchronized native void resetImpl(long handle);

	public void setDictionary(byte[] buf) {
		setDictionary(buf, 0, buf.length);
	}

	/**
	 * Sets the dictionary to be used for compression by this Deflater.
	 * setDictionary() can only be called if this Deflater supports the writing
	 * of ZLIB headers. This is the default behaviour but can be overridden
	 * using Deflater(int, boolean).
	 * 
	 * @see Deflater#Deflater(int, boolean)
	 */
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

	private synchronized native void setDictionaryImpl(byte[] buf, int off,
			int nbytes, long handle);

	/**
	 * Sets the input buffer the Deflater will use to extract uncompressed bytes
	 * for later compression.
	 */
	public void setInput(byte[] buf) {
		setInput(buf, 0, buf.length);
	}

	/**
	 * Sets the input buffer the Deflater will use to extract uncompressed bytes
	 * for later compression. Input will be taken from the buffer region
	 * starting at off and ending at nbytes - 1.
	 */
	public synchronized void setInput(byte[] buf, int off, int nbytes) {
		if (streamHandle == -1) {
            throw new IllegalStateException();
        }
		// avoid int overflow, check null buf
		if (off <= buf.length && nbytes >= 0 && off >= 0
				&& buf.length - off >= nbytes) {
			inLength = nbytes;
			inRead = 0;
			if (inputBuffer == null) {
                setLevelsImpl(compressLevel, strategy, streamHandle);
            }
			inputBuffer = buf;
			setInputImpl(buf, off, nbytes, streamHandle);
		} else {
            throw new ArrayIndexOutOfBoundsException();
        }
	}

	private synchronized native void setLevelsImpl(int level, int strategy,
			long handle);

	private synchronized native void setInputImpl(byte[] buf, int off,
			int nbytes, long handle);

	/**
	 * Sets the compression level to be used when compressing data. The
	 * compression level must be a value between 0 and 9. This value must be set
	 * prior to calling setInput().
	 * 
	 * @param level
	 *            compression level to use
	 * @exception IllegalArgumentException
	 *                If the compression level is invalid.
	 */
	public synchronized void setLevel(int level) {
		if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw new IllegalArgumentException();
        }
		if (inputBuffer != null) {
            throw new IllegalStateException();
        }
		compressLevel = level;
	}

	/**
	 * Sets the compression strategy to be used. The strategy must be one of
	 * FILTERED, HUFFMAN_ONLY or DEFAULT_STRATEGY.This value must be set prior
	 * to calling setInput().
	 * 
	 * @param strategy
	 *            compression strategy to use
	 * @exception IllegalArgumentException
	 *                If the strategy specified is not one of FILTERED,
	 *                HUFFMAN_ONLY or DEFAULT_STRATEGY.
	 */
	public synchronized void setStrategy(int strategy) {
		if (strategy < DEFAULT_STRATEGY || strategy > HUFFMAN_ONLY) {
            throw new IllegalArgumentException();
        }
		if (inputBuffer != null) {
            throw new IllegalStateException();
        }
		this.strategy = strategy;
	}
	
    /**
	 * Returns a long int of total number of bytes read by the Deflater. This
	 * method performs the same as getTotalIn except it returns a long value
	 * instead of an integer
	 * 
	 * @return bytes exactly read by deflater
	 */
	public synchronized long getBytesRead() {
		// Throw NPE here
		if (streamHandle == -1) {
            throw new NullPointerException();
        }
		return getTotalInImpl(streamHandle);
	}

	/**
	 * Returns a long int of total number of bytes of read by the Deflater. This
	 * method performs the same as getTotalOut except it returns a long value
	 * instead of an integer
	 * 
	 * @return bytes exactly write by deflater
	 */
    public synchronized long getBytesWritten() {
        // Throw NPE here
        if (streamHandle == -1) {
            throw new NullPointerException();
        }
        return getTotalOutImpl(streamHandle);
    }

	private native long createStream(int level, int strategy1, boolean noHeader1);
}

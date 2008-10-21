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
 * FilterReader is a class which takes a Reader and <em>filters</em> the input
 * in some way. The filtered view may be a buffered view or one which
 * uncompresses data before returning characters read.
 * 
 * @see FilterWriter
 */
public abstract class FilterReader extends Reader {

    /**
     * The target Reader which is being filtered.
     */
    protected Reader in;

    /**
     * Constructs a new FilterReader on the Reader <code>in</code>. All reads
     * are now filtered through this Reader.
     * 
     * @param in
     *            The non-null Reader to filter reads on.
     */
    protected FilterReader(Reader in) {
        super(in);
        this.in = in;
    }

    /**
     * Close this FilterReader. This implementation closes the target Reader.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this Reader.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            in.close();
        }
    }

    /**
     * Set a Mark position in this FilterReader. The parameter
     * <code>readLimit</code> indicates how many characters can be read before
     * a mark is invalidated. Sending reset() will reposition the Reader back to
     * the marked position provided <code>readLimit</code> has not been
     * surpassed.
     * <p>
     * This implementation sets a mark in the target Reader.
     * 
     * @param readlimit
     *            the number of characters to be able to read before
     *            invalidating the mark.
     * 
     * @throws IOException
     *             If an error occurs attempting mark this Reader.
     */
    @Override
    public synchronized void mark(int readlimit) throws IOException {
        synchronized (lock) {
            in.mark(readlimit);
        }
    }

    /**
     * Returns a boolean indicating whether or not this FilterReader supports
     * mark() and reset(). This implementation returns whether or not the target
     * Reader supports marking.
     * 
     * @return indicates whether or not mark() and reset() are supported.
     */
    @Override
    public boolean markSupported() {
        synchronized (lock) {
            return in.markSupported();
        }
    }

    /**
     * Reads a single char from this FilterReader and returns the result as an
     * int. The 2 lowest order bytes are returned or -1 of the end of reader was
     * encountered. This implementation returns a char from the target Reader.
     * 
     * @return The byte read or -1 if end of reader.
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this Reader.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            return in.read();
        }
    }

    /**
     * Reads at most <code>count</code> chars from this FilterReader and
     * stores them in char array <code>buffer</code> starting at offset
     * <code>offset</code>. Answer the number of chars actually read or -1 if
     * no chars were read and end of reader was encountered. This implementation
     * reads chars from the target reader.
     * 
     * @param buffer
     *            the char array in which to store the read chars.
     * @param offset
     *            the offset in <code>buffer</code> to store the read chars.
     * @param count
     *            the maximum number of chars to store in <code>buffer</code>.
     * @return the number of chars actually read or -1 if end of reader.
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this Reader.
     */
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            return in.read(buffer, offset, count);
        }
    }

    /**
     * Returns a <code>boolean</code> indicating whether or not this Reader is
     * ready to be read without blocking. If the result is <code>true</code>,
     * the next <code>read()</code> will not block. If the result is
     * <code>false</code> this Reader may or may not block when
     * <code>read()</code> is sent.
     * 
     * @return <code>true</code> if the receiver will not block when
     *         <code>read()</code> is called, <code>false</code> if unknown
     *         or blocking will occur.
     * 
     * @throws IOException
     *             If the Reader is already closed or some other IO error
     *             occurs.
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            return in.ready();
        }
    }

    /**
     * Reset this Readers position to the last <code>mark()</code> location.
     * Invocations of <code>read()/skip()</code> will occur from this new
     * location. If this Reader was not marked, the implementation of
     * <code>reset()</code> is implementation specific. See the comment for
     * the specific Reader subclass for implementation details. The default
     * action is to throw <code>IOException</code>.
     * 
     * @throws IOException
     *             if a problem occurred or the target Reader does not support
     *             <code>mark()/reset()</code>.
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            in.reset();
        }
    }

    /**
     * Skips <code>count</code> number of characters in this Reader.
     * Subsequent <code>read()</code>'s will not return these characters
     * unless <code>reset()</code> is used. The default implementation is to
     * skip chars in the filtered Reader.
     * 
     * @param count
     *            the maximum number of characters to skip.
     * @return the number of characters actually skipped.
     * 
     * @throws IOException
     *             If the Reader is already closed or some other IO error
     *             occurs.
     */
    @Override
    public long skip(long count) throws IOException {
        synchronized (lock) {
            return in.skip(count);
        }
    }
}

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

import java.util.Enumeration;
import java.util.Vector;

/**
 * SequenceInputStream is used for streaming over a sequence of streams
 * concatenated together. Reads are taken from the first stream until it ends,
 * then the next stream is used until the last stream returns end of file.
 * 
 */
public class SequenceInputStream extends InputStream {
    /**
     * An enumeration which will return types of InputStream.
     */
    private Enumeration<? extends InputStream> e;

    /**
     * The current input stream.
     */
    private InputStream in;

    /**
     * Constructs a new SequenceInputStream using the two streams
     * <code>s1</code> and <code>s2</code> as the sequence of streams to
     * read from.
     * 
     * @param s1
     *            the first stream to get bytes from
     * @param s2
     *            the second stream to get bytes from
     */
    public SequenceInputStream(InputStream s1, InputStream s2) {
        if (s1 == null) {
            throw new NullPointerException();
        }
        Vector<InputStream> inVector = new Vector<InputStream>(1);
        inVector.addElement(s2);
        e = inVector.elements();
        in = s1;
    }

    /**
     * Constructs a new SequenceInputStream using the elements returned from
     * Enumeration <code>e</code> as the stream sequence. The types returned
     * from nextElement() must be of InputStream.
     * 
     * @param e
     *            the Enumeration of InputStreams to get bytes from
     */
    public SequenceInputStream(Enumeration<? extends InputStream> e) {
        this.e = e;
        if (e.hasMoreElements()) {
            in = e.nextElement();
            if (in == null) {
                throw new NullPointerException();
            }
        }
    }

    /**
     * Returns a int representing then number of bytes that are available before
     * this InputStream will block.
     * 
     * @return the number of bytes available before blocking.
     * 
     * @throws IOException
     *             If an error occurs in this InputStream.
     */
    @Override
    public int available() throws IOException {
        if (e != null && in != null) {
            return in.available();
        }
        return 0;
    }

    /**
     * Close the SequenceInputStream. All streams in this sequence are closed
     * before returning from this method. This stream cannot be used for input
     * once it has been closed.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this FileInputStream.
     */
    @Override
    public void close() throws IOException {
        while (in != null) {
            nextStream();
        }
        e = null;
    }

    /**
     * Sets up the next InputStream or leaves it alone if there are none left.
     * 
     * @throws IOException
     */
    private void nextStream() throws IOException {
        if (in != null) {
            in.close();
        }
        if (e.hasMoreElements()) {
            in = e.nextElement();
            if (in == null) {
                throw new NullPointerException();
            }
        } else {
            in = null;
        }
    }

    /**
     * Reads a single byte from this SequenceInputStream and returns the result
     * as an int. The low-order byte is returned or -1 of the end of stream was
     * encountered. The current stream is read from. If it reaches the end of
     * file, the next stream is read from.
     * 
     * @return the byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If an error occurs while reading the stream
     */
    @Override
    public int read() throws IOException {
        while (in != null) {
            int result = in.read();
            if (result >= 0) {
                return result;
            }
            nextStream();
        }
        return -1;
    }

    /**
     * Reads at most <code>count</code> bytes from this SequenceInputStream
     * and stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param count
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If an error occurs while reading the stream
     */
    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (in == null) {
            return -1;
        }
        if (buffer == null) {
            throw new NullPointerException();
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length - count || count < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (in != null) {
            int result = in.read(buffer, offset, count);
            if (result >= 0) {
                return result;
            }
            nextStream();
        }
        return -1;
    }
}

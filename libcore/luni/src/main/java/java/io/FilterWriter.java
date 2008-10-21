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
 * FilterWriter is a class which takes a Writer and <em>filters</em> the
 * output in some way. The filtered view may be a buffered output or one which
 * compresses data before actually writing the bytes.
 * 
 * @see FilterWriter
 */
public abstract class FilterWriter extends Writer {

    /**
     * The Writer being filtered.
     */
    protected Writer out;

    /**
     * Constructs a new FilterWriter on the Writer <code>out</code>. All
     * writes are now filtered through this Writer.
     * 
     * @param out
     *            the target Writer to filter writes on.
     */
    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    /**
     * Close this FilterWriter. Closes the Writer <code>out</code> by default.
     * This will close any downstream Writers as well. Any additional processing
     * required by concrete subclasses should be provided in their own
     * <code>close</code> implementation.
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to close this FilterWriter.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            out.close();
        }
    }

    /**
     * Flush this FilteredWriter to ensure all pending data is sent out to the
     * target Writer. This implementation flushes the target Writer.
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to flush this FilterWriter.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            out.flush();
        }
    }

    /**
     * Writes <code>count</code> <code>chars</code> from the char array
     * <code>buffer</code> starting at offset <code>index</code> to this
     * FilterWriter. This implementation writes the <code>buffer</code> to the
     * target Writer.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get chars
     * @param count
     *            number of chars in buffer to write
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this FilterWriter.
     */
    @Override
    public void write(char buffer[], int offset, int count) throws IOException {
        synchronized (lock) {
            out.write(buffer, offset, count);
        }
    }

    /**
     * Writes the specified char <code>oneChar</code> to this FilterWriter.
     * Only the 2 low order bytes of <code>oneChar</code> is written. This
     * implementation writes the char to the target Writer.
     * 
     * @param oneChar
     *            the char to be written
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this FilterWriter.
     */
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            out.write(oneChar);
        }
    }

    /**
     * Writes <code>count</code> <code>chars</code> from the String
     * <code>str</code> starting at offset <code>index</code> to this
     * FilterWriter. This implementation writes the <code>str</code> to the
     * target Writer.
     * 
     * @param str
     *            the String to be written.
     * @param offset
     *            offset in str to get chars.
     * @param count
     *            number of chars in str to write.
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this FilterWriter.
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            out.write(str, offset, count);
        }
    }
}

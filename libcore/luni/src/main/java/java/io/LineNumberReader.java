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
 * LineNumberReader is a buffered character input reader which counts line
 * numbers as data is being read. The line number starts at 0 and is incremented
 * any time '\r', '\n', or '\r\n' is read.
 * 
 * @see BufferedWriter
 */
public class LineNumberReader extends BufferedReader {

    private int lineNumber;

    private int markedLineNumber = -1;

    private boolean lastWasCR;

    private boolean markedLastWasCR;

    /**
     * Constructs a new buffered LineNumberReader on the Reader <code>in</code>.
     * The default buffer size (8K) is allocated and all reads can now be
     * filtered through this LineNumberReader.
     * 
     * @param in
     *            the Reader to buffer reads on.
     */
    public LineNumberReader(Reader in) {
        super(in);
    }

    /**
     * Constructs a new buffered LineNumberReader on the Reader <code>in</code>.
     * The buffer size is specified by the parameter <code>size</code> and all
     * reads can now be filtered through this LineNumberReader.
     * 
     * @param in
     *            the Reader to buffer reads on.
     * @param size
     *            the size of buffer to allocate.
     */
    public LineNumberReader(Reader in, int size) {
        super(in, size);
    }

    /**
     * Returns a int representing the current line number for this
     * LineNumberReader.
     * 
     * @return int the current line number.
     */
    public int getLineNumber() {
        synchronized (lock) {
            return lineNumber;
        }
    }

    /**
     * Set a Mark position in this LineNumberReader. The parameter
     * <code>readLimit</code> indicates how many characters can be read before
     * a mark is invalidated. Sending reset() will reposition the reader back to
     * the marked position provided <code>readLimit</code> has not been
     * surpassed. The lineNumber associated with this marked position will also
     * be saved and restored when reset() is sent provided
     * <code>readLimit</code> has not been surpassed.
     * 
     * @param readlimit
     *            an int representing how many characters must be read before
     *            invalidating the mark.
     * 
     * @throws IOException
     *             If an error occurs attempting mark this LineNumberReader.
     */
    @Override
    public void mark(int readlimit) throws IOException {
        synchronized (lock) {
            super.mark(readlimit);
            markedLineNumber = lineNumber;
            markedLastWasCR = lastWasCR;
        }
    }

    /**
     * Reads a single char from this LineNumberReader and returns the result as
     * an int. The low-order 2 bytes are returned or -1 of the end of reader was
     * encountered. This implementation returns a char from the target reader.
     * The line number count is incremented if a line terminator is encountered.
     * A line delimiter sequence is determined by '\r', '\n', or '\r\n'. In this
     * method, the sequence is always translated into '\n'.
     * 
     * @return int The char read or -1 if end of reader.
     * 
     * @throws IOException
     *             If the reader is already closed or another IOException
     *             occurs.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            int ch = super.read();
            if (ch == '\n' && lastWasCR) {
                ch = super.read();
            }
            lastWasCR = false;
            switch (ch) {
                case '\r':
                    ch = '\n';
                    lastWasCR = true;
                    // fall through
                case '\n':
                    lineNumber++;
            }
            return ch;
        }
    }

    /**
     * Reads at most <code>count</code> chars from this LineNumberReader and
     * stores them in char array <code>buffer</code> starting at offset
     * <code>offset</code>. Answer the number of chars actually read or -1 if
     * no chars were read and end of reader was encountered. This implementation
     * reads chars from the target stream. The line number count is incremented
     * if a line terminator is encountered. A line delimiter sequence is
     * determined by '\r', '\n', or '\r\n'. In this method, the sequence is
     * always translated into '\n'.
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
     *             If the reader is already closed or another IOException
     *             occurs.
     */

    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            int read = super.read(buffer, offset, count);
            if (read == -1) {
                return -1;
            }
            for (int i = 0; i < read; i++) {
                char ch = buffer[offset + i];
                if (ch == '\r') {
                    lineNumber++;
                    lastWasCR = true;
                } else if (ch == '\n') {
                    if (!lastWasCR) {
                        lineNumber++;
                    }
                    lastWasCR = false;
                } else {
                    lastWasCR = false;
                }
            }
            return read;
        }
    }

    /**
     * Returns a <code>String</code> representing the next line of text
     * available in this LineNumberReader. A line is represented by 0 or more
     * characters followed by <code>'\n'</code>, <code>'\r'</code>,
     * <code>"\n\r"</code> or end of stream. The <code>String</code> does
     * not include the newline sequence.
     * 
     * @return String the contents of the line or null if no characters were
     *         read before end of stream.
     * 
     * @throws IOException
     *             If the LineNumberReader is already closed or some other IO
     *             error occurs.
     */
    @Override
    public String readLine() throws IOException {
        synchronized (lock) {
            /* Typical Line Length */
            StringBuilder result = new StringBuilder(80);
            while (true) {
                int character = read();
                if (character == -1) {
                    return result.length() != 0 ? result.toString() : null;
                }
                if (character == '\n') {
                    return result.toString();
                }
                result.append((char) character);
            }
        }
    }

    /**
     * Reset this LineNumberReader to the last marked location. If the
     * <code>readlimit</code> has been passed or no <code>mark</code> has
     * been set, throw IOException. This implementation resets the target
     * reader. It also resets the line count to what is was when this reader was
     * marked.
     * 
     * @throws IOException
     *             If the reader is already closed or another IOException
     *             occurs.
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            super.reset();
            lineNumber = markedLineNumber;
            lastWasCR = markedLastWasCR;
        }
    }

    /**
     * Sets the lineNumber of this LineNumberReader to the specified
     * <code>lineNumber</code>. Note that this may have side effects on the
     * line number associated with the last marked position.
     * 
     * @param lineNumber
     *            the new lineNumber value.
     */
    public void setLineNumber(int lineNumber) {
        synchronized (lock) {
            this.lineNumber = lineNumber;
        }
    }

    /**
     * Skips <code>count</code> number of chars in this LineNumberReader.
     * Subsequent <code>read()</code>'s will not return these chars unless
     * <code>reset()</code> is used. This implementation skips
     * <code>count</code> number of chars in the target stream and increments
     * the lineNumber count as chars are skipped.
     * 
     * @param count
     *            the number of chars to skip.
     * @return the number of chars actually skipped.
     * 
     * @throws IOException
     *             If the reader is already closed or another IOException
     *             occurs.
     */
    @Override
    public long skip(long count) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            for (int i = 0; i < count; i++) {
                if (read() == -1) {
                    return i;
                }
            }
            return count;
        }
    }
}

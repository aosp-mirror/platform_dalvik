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
 * StringWriter is an class for writing Character Streams to a StringBuffer. The
 * characters written can then be returned as a String. This is used for
 * capturing output sent to a Writer by substituting a StringWriter.
 * 
 * @see StringReader
 */
public class StringWriter extends Writer {

    private StringBuffer buf;

    /**
     * Constructs a new StringWriter which has a StringBuffer allocated with the
     * default size of 16 characters. The StringBuffer is also the
     * <code>lock</code> used to synchronize access to this Writer.
     */
    public StringWriter() {
        super();
        buf = new StringBuffer(16);
        lock = buf;
    }

    /**
     * Constructs a new StringWriter which has a StringBuffer allocated with the
     * size of <code>initialSize</code> characters. The StringBuffer is also
     * the <code>lock</code> used to synchronize access to this Writer.
     * 
     * @param initialSize
     *            the intial number of characters
     */
    public StringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException();
        }
        buf = new StringBuffer(initialSize);
        lock = buf;
    }

    /**
     * Close this Writer. This is the concrete implementation required. This
     * particular implementation does nothing.
     * 
     * @throws IOException
     *             If an IO error occurs closing this StringWriter.
     */
    @Override
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Flush this Writer. This is the concrete implementation required. This
     * particular implementation does nothing.
     */
    @Override
    public void flush() {
        /* empty */
    }

    /**
     * Answer the contents of this StringWriter as a StringBuffer. Any changes
     * made to the StringBuffer by the receiver or the caller are reflected in
     * this StringWriter.
     * 
     * @return this StringWriters local StringBuffer.
     */
    public StringBuffer getBuffer() {
        synchronized (lock) {
            return buf;
        }
    }

    /**
     * Answer the contents of this StringWriter as a String. Any changes made to
     * the StringBuffer by the receiver after returning will not be reflected in
     * the String returned to the caller.
     * 
     * @return this StringWriters current contents as a String.
     */
    @Override
    public String toString() {
        synchronized (lock) {
            return buf.toString();
        }
    }

    /**
     * Writes <code>count</code> characters starting at <code>offset</code>
     * in <code>cbuf</code> to this StringWriter.
     * 
     * @param cbuf
     *            the non-null array containing characters to write.
     * @param offset
     *            offset in buf to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws ArrayIndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    @Override
    public void write(char[] cbuf, int offset, int count) {
        // avoid int overflow
        if (offset < 0 || offset > cbuf.length || count < 0
                || count > cbuf.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (lock) {
            this.buf.append(cbuf, offset, count);
        }
    }

    /**
     * Writes the specified character <code>oneChar</code> to this
     * StringWriter. This implementation writes the low order two bytes to the
     * Stream.
     * 
     * @param oneChar
     *            The character to write
     */
    @Override
    public void write(int oneChar) {
        synchronized (lock) {
            buf.append((char) oneChar);
        }
    }

    /**
     * Writes the characters from the String <code>str</code> to this
     * StringWriter.
     * 
     * @param str
     *            the non-null String containing the characters to write.
     */
    @Override
    public void write(String str) {
        synchronized (lock) {
            buf.append(str);
        }
    }

    /**
     * Writes <code>count</code> number of characters starting at
     * <code>offset</code> from the String <code>str</code> to this
     * StringWriter.
     * 
     * @param str
     *            the non-null String containing the characters to write.
     * @param offset
     *            the starting point to retrieve characters.
     * @param count
     *            the number of characters to retrieve and write.
     * 
     * @throws ArrayIndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    @Override
    public void write(String str, int offset, int count) {
        String sub = str.substring(offset, offset + count);
        synchronized (lock) {
            buf.append(sub);
        }
    }

    /**
     * Append a char <code>c</code>to the StringWriter. The
     * StringWriter.append(<code>c</code>) works the same way as
     * StringWriter.write(<code>c</code>).
     * 
     * @param c
     *            The character appended to the StringWriter.
     * @return The StringWriter.
     */
    @Override
    public StringWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * Append a CharSequence <code>csq</code> to the StringWriter. The
     * StringWriter.append(<code>csq</code>) works the same way as
     * StringWriter.write(<code>csq</code>.toString()). If <code>csq</code>
     * is null, then "null" will be substituted for <code>csq</code>.
     * 
     * @param csq
     *            The CharSequence appended to the StringWriter.
     * @return The StringWriter
     */
    @Override
    public StringWriter append(CharSequence csq) {
        if (null == csq) {
            append(TOKEN_NULL, 0, TOKEN_NULL.length());
        } else {
            append(csq, 0, csq.length());
        }
        return this;
    }

    /**
     * Append a subsequence of a CharSequence <code>csq</code> to the
     * StringWriter. The first char and the last char of the subsequnce is
     * specified by the parameter <code>start</code> and <code>end</code>.
     * The StringWriter.append(<code>csq</code>) works the same way as
     * StringWriter.write(<code>csq</code>.subSequence(<code>start</code>,<code>end</code>).toString).If
     * <code>csq</code> is null, then "null" will be substituted for
     * <code>csq</code>. s
     * 
     * @param csq
     *            The CharSequence appended to the StringWriter.
     * @param start
     *            The index of the first char in the CharSequence appended to
     *            the StringWriter.
     * @param end
     *            The index of the char after the last one in the CharSequence
     *            appended to the StringWriter.
     * @return The StringWriter.
     * @throws IndexOutOfBoundsException
     *             If start is less than end, end is greater than the length of
     *             the CharSequence, or start or end is negative.
     */
    @Override
    public StringWriter append(CharSequence csq, int start, int end) {
        if (null == csq) {
            csq = TOKEN_NULL;
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}

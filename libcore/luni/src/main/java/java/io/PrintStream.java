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

import java.nio.charset.Charset;
import java.security.AccessController;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * PrintStream is a class which takes an OutputStream and provides convenience
 * methods for printing common data types in a human readable format on the
 * stream. This is not to be confused with DataOutputStream which is used for
 * encoding common data types so that they can be read back in. No IOExceptions
 * are thrown by this class. Instead, callers should call checkError() to see if
 * a problem has been encountered in this Stream.
 * 
 */
public class PrintStream extends FilterOutputStream implements Appendable,
        Closeable {

    private static final String TOKEN_NULL = "null"; //$NON-NLS-1$

    /**
     * indicates whether or not this PrintStream has incurred an error.
     */
    private boolean ioError;

    /**
     * indicates whether or not this PrintStream should flush its contents after
     * printing a new line.
     */
    private boolean autoflush;

    private String encoding;

    private final String lineSeparator = AccessController
            .doPrivileged(new PriviAction<String>("line.separator")); //$NON-NLS-1$

    // private Formatter formatter;

    /**
     * Constructs a new PrintStream on the OutputStream <code>out</code>. All
     * writes to the target can now take place through this PrintStream. By
     * default, the PrintStream is set to not autoflush when a newline is
     * encountered.
     * 
     * @param out
     *            the OutputStream to provide convenience methods on.
     */
    public PrintStream(OutputStream out) {
        super(out);
        if (out == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Constructs a new PrintStream on the OutputStream <code>out</code>. All
     * writes to the target can now take place through this PrintStream. The
     * PrintStream is set to not autoflush if <code>autoflush</code> is
     * <code>true</code>.
     * 
     * @param out
     *            the OutputStream to provide convenience methods on.
     * @param autoflush
     *            indicates whether or not to flush contents upon encountering a
     *            newline sequence.
     */
    public PrintStream(OutputStream out, boolean autoflush) {
        super(out);
        if (out == null) {
            throw new NullPointerException();
        }
        this.autoflush = autoflush;
    }

    /**
     * Constructs a new PrintStream on the OutputStream <code>out</code>. All
     * writes to the target can now take place through this PrintStream. The
     * PrintStream is set to not autoflush if <code>autoflush</code> is
     * <code>true</code>.
     * 
     * @param out
     *            the OutputStream to provide convenience methods on.
     * @param autoflush
     *            indicates whether or not to flush contents upon encountering a
     *            newline sequence.
     * @param enc
     *            the non-null String describing the desired character encoding.
     * 
     * @throws UnsupportedEncodingException
     *             If the chosen encoding is not supported
     */
    public PrintStream(OutputStream out, boolean autoflush, String enc)
            throws UnsupportedEncodingException {
        super(out);
        if (out == null || enc == null) {
            throw new NullPointerException();
        }
        this.autoflush = autoflush;
        if (!Charset.isSupported(enc)) {
            throw new UnsupportedEncodingException(enc);
        }
        encoding = enc;
    }

    /**
     * Constructs a new PrintStream on the file <code>file</code>. All writes
     * to the target can now take place through this PrintStream. Its encoding
     * character set is the default charset in the VM.
     * 
     * @param file
     *            the file to provide convenience methods on.
     * @throws FileNotFoundException
     *             if the file does not exist or cannot be opened to write. Or
     *             the file cannot be created or any problem when open the file
     *             to write.
     * @throws SecurityException
     *             if the security manager exists and denies the write to the
     *             file.
     */
    public PrintStream(File file) throws FileNotFoundException {
        super(new FileOutputStream(file));
    }

    /**
     * Constructs a new PrintStream on the file <code>file</code>. All writes
     * to the target can now take place through this PrintStream. Its encoding
     * character set name is <code>csn</code>.
     * 
     * @param file
     *            the file to provide convenience methods on.
     * @param csn
     *            the character set name
     * @throws FileNotFoundException
     *             if the file does not exist or cannot be opened to write. Or
     *             the file cannot be created or any problem when open the file
     *             to write.
     * @throws SecurityException
     *             if the security manager exists and denies the write to the
     *             file.
     * @throws UnsupportedEncodingException
     *             if the chosen character set is not supported
     */
    public PrintStream(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        super(new FileOutputStream(file));
        if (csn == null) {
            throw new NullPointerException();
        }
        if (!Charset.isSupported(csn)) {
            throw new UnsupportedEncodingException();
        }
        encoding = csn;
    }

    /**
     * Constructs a new PrintStream on the file the name of which is<code>fileName</code>.
     * All writes to the target can now take place through this PrintStream. Its
     * encoding character set is the default charset in the VM.
     * 
     * @param fileName
     *            the file to provide convenience methods on.
     * @throws FileNotFoundException
     *             if the file does not exist or cannot be opened to write. Or
     *             the file cannot be created or any problem when open the file
     *             to write.
     * @throws SecurityException
     *             if the security manager exists and denies the write to the
     *             file.
     */
    public PrintStream(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    /**
     * Constructs a new PrintStream on the file the name of which is<code>fileName</code>.
     * All writes to the target can now take place through this PrintStream. Its
     * encoding character set name is <code>csn</code>.
     * 
     * @param fileName
     *            the file to provide convenience methods on.
     * @param csn
     *            the character set name
     * @throws FileNotFoundException
     *             if the file does not exist or cannot be opened to write. Or
     *             the file cannot be created or any problem when open the file
     *             to write.
     * @throws SecurityException
     *             if the security manager exists and denies the write to the
     *             file.
     * @throws UnsupportedEncodingException
     *             if the chosen character set is not supported
     */
    public PrintStream(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new File(fileName), csn);
    }

    /**
     * Returns a boolean indicating whether or not this PrintStream has
     * encountered an error. If so, the receiver should probably be closed since
     * further writes will not actually take place. A side effect of calling
     * checkError is that the target OutputStream is flushed.
     * 
     * @return <code>true</code> if an error occurred in this PrintStream,
     *         <code>false</code> otherwise.
     */
    public boolean checkError() {
        if (out != null) {
            flush();
        }
        return ioError;
    }

    /**
     * Close this PrintStream. This implementation flushes and then closes the
     * target stream. If an error occurs, set an error in this PrintStream to
     * <code>true</code>.
     */
    @Override
    public synchronized void close() {
        flush();
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                setError();
            }
        }
    }

    /**
     * Flush this PrintStream to ensure all pending data is sent out to the
     * target OutputStream. This implementation flushes the target OutputStream.
     * If an error occurs, set an error in this PrintStream to <code>true</code>.
     */
    @Override
    public synchronized void flush() {
        if (out != null) {
            try {
                out.flush();
                return;
            } catch (IOException e) {
                // Ignored, fall through to setError
            }
        }
        setError();
    }

    /**
     * Writes a string formatted by an intermediate <code>Formatter</code> to
     * this stream using the given format string and arguments.
     * <p>
     * The method uses the default for the current JVM instance locale, as if it
     * is specified by the <code>Locale.getDefault()</code> call.
     * 
     * @param format
     *            A format string.
     * @param args
     *            The arguments list. If there are more arguments than those
     *            specified by the format string, then the additional arguments
     *            are ignored.
     * @return This stream.
     * @throws IllegalFormatException
     *             If the format string is illegal or incompatible with the
     *             arguments or the arguments are less than those required by
     *             the format string or any other illegal situation.
     * @throws NullPointerException
     *             If the given format is null.
     */
    public PrintStream format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    /**
     * Writes a string formatted by an intermediate <code>Formatter</code> to
     * this stream using the given format string and arguments.
     * 
     * @param l
     *            The locale used in the method. If locale is null, then no
     *            localization will be applied.
     * @param format
     *            A format string.
     * @param args
     *            The arguments list. If there are more arguments than those
     *            specified by the format string, then the additional arguments
     *            are ignored.
     * @return This stream.
     * @throws IllegalFormatException
     *             If the format string is illegal or incompatible with the
     *             arguments or the arguments are less than those required by
     *             the format string or any other illegal situation.
     * @throws NullPointerException
     *             If the given format is null.
     */
    public PrintStream format(Locale l, String format, Object... args) {
        if (format == null) {
            throw new NullPointerException(Msg.getString("K0351")); //$NON-NLS-1$
        }
        new Formatter(this, l).format(format, args);
        return this;
    }

    /**
     * Prints a formatted string. The behavior of this method is the same as
     * this stream's <code>format(String format, Object... args)</code>
     * method.
     * <p>
     * The method uses the default for the current JVM instance locale, as if it
     * is specified by the <code>Locale.getDefault()</code> call.
     * 
     * @param format
     *            A format string.
     * @param args
     *            The arguments list. If there are more arguments than those
     *            specified by the format string, then the additional arguments
     *            are ignored.
     * @return This stream.
     * @throws IllegalFormatException
     *             If the format string is illegal or incompatible with the
     *             arguments or the arguments are less than those required by
     *             the format string or any other illegal situation.
     * @throws NullPointerException
     *             If the given format is null.
     */
    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }

    /**
     * Prints a formatted string. The behavior of this method is the same as
     * this writer's
     * <code>format(Locale l, String format, Object... args)</code> method.
     * 
     * @param l
     *            The locale used in the method. If locale is null, then no
     *            localization will be applied.
     * @param format
     *            A format string.
     * @param args
     *            The arguments list. If there are more arguments than those
     *            specified by the format string, then the additional arguments
     *            are ignored.
     * @return This stream.
     * @throws IllegalFormatException
     *             If the format string is illegal or incompatible with the
     *             arguments or the arguments are less than those required by
     *             the format string or any other illegal situation.
     * @throws NullPointerException
     *             If the given format is null.
     */
    public PrintStream printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }

    /**
     * Put the line separator String onto the print stream.
     */
    private void newline() {
        print(lineSeparator);
    }

    /**
     * Prints the String representation of the character array parameter
     * <code>charArray</code> to the target OutputStream.
     * 
     * @param charArray
     *            the character array to print on this PrintStream.
     */
    public void print(char[] charArray) {
        print(new String(charArray, 0, charArray.length));
    }

    /**
     * Prints the String representation of the character parameter
     * <code>ch</code> to the target OutputStream.
     * 
     * @param ch
     *            the character to print on this PrintStream.
     */
    public void print(char ch) {
        print(String.valueOf(ch));
    }

    /**
     * Prints the String representation of the <code>double</code> parameter
     * <code>dnum</code> to the target OutputStream.
     * 
     * @param dnum
     *            the <code>double</code> to print on this PrintStream.
     */
    public void print(double dnum) {
        print(String.valueOf(dnum));
    }

    /**
     * Prints the String representation of the <code>float</code> parameter
     * <code>fnum</code> to the target OutputStream.
     * 
     * @param fnum
     *            the <code>float</code> to print on this PrintStream.
     */
    public void print(float fnum) {
        print(String.valueOf(fnum));
    }

    /**
     * Obtains the <code>int</code> argument as a <code>String</code> and
     * prints it to the target {@link OutputStream}.
     * 
     * @param inum
     *            the <code>int</code> to print on this PrintStream.
     */
    public void print(int inum) {
        print(String.valueOf(inum));
    }

    /**
     * Prints the String representation of the <code>long</code> parameter
     * <code>lnum</code> to the target OutputStream.
     * 
     * @param lnum
     *            the <code>long</code> to print on this PrintStream.
     */
    public void print(long lnum) {
        print(String.valueOf(lnum));
    }

    /**
     * Prints the String representation of the Object parameter <code>obj</code>
     * to the target OutputStream.
     * 
     * @param obj
     *            the Object to print on this PrintStream.
     */
    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    /**
     * Prints the String representation of the <code>String</code> parameter
     * <code>str</code> to the target OutputStream.
     * 
     * @param str
     *            the <code>String</code> to print on this PrintStream.
     */
    public synchronized void print(String str) {
        if (out == null) {
            setError();
            return;
        }
        if (str == null) {
            print("null"); //$NON-NLS-1$
            return;
        }

        try {
            if (encoding == null) {
                write(str.getBytes());
            } else {
                write(str.getBytes(encoding));
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Prints the String representation of the <code>boolean</code> parameter
     * <code>bool</code> to the target OutputStream.
     * 
     * @param bool
     *            the <code>boolean</code> to print on this PrintStream.
     */
    public void print(boolean bool) {
        print(String.valueOf(bool));
    }

    /**
     * Prints the String representation of the System property
     * <code>"line.separator"</code> to the target OutputStream.
     * 
     */
    public void println() {
        newline();
    }

    /**
     * Prints the String representation of the character array parameter
     * <code>charArray</code> to the target OutputStream followed by the
     * System property <code>"line.separator"</code>.
     * 
     * @param charArray
     *            the character array to print on this PrintStream.
     */
    public void println(char[] charArray) {
        println(new String(charArray, 0, charArray.length));
    }

    /**
     * Prints the String representation of the character parameter
     * <code>ch</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param ch
     *            the character to print on this PrintStream.
     */
    public void println(char ch) {
        println(String.valueOf(ch));
    }

    /**
     * Prints the String representation of the <code>double</code> parameter
     * <code>dnum</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param dnum
     *            the double to print on this PrintStream.
     */
    public void println(double dnum) {
        println(String.valueOf(dnum));
    }

    /**
     * Prints the String representation of the <code>float</code> parameter
     * <code>fnum</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param fnum
     *            the float to print on this PrintStream.
     */
    public void println(float fnum) {
        println(String.valueOf(fnum));
    }

    /**
     * Obtains the <code>int</code> argument as a <code>String</code> and
     * prints it to the target {@link OutputStream} followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param inum
     *            the int to print on this PrintStream.
     */
    public void println(int inum) {
        println(String.valueOf(inum));
    }

    /**
     * Prints the String representation of the <code>long</code> parameter
     * <code>lnum</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param lnum
     *            the long to print on this PrintStream.
     */
    public void println(long lnum) {
        println(String.valueOf(lnum));
    }

    /**
     * Prints the String representation of the <code>Object</code> parameter
     * <code>obj</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param obj
     *            the <code>Object</code> to print on this PrintStream.
     */
    public void println(Object obj) {
        println(String.valueOf(obj));
    }

    /**
     * Prints the String representation of the <code>String</code> parameter
     * <code>str</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param str
     *            the <code>String</code> to print on this PrintStream.
     */
    public synchronized void println(String str) {
        print(str);
        newline();
    }

    /**
     * Prints the String representation of the <code>boolean</code> parameter
     * <code>bool</code> to the target OutputStream followed by the System
     * property <code>"line.separator"</code>.
     * 
     * @param bool
     *            the boolean to print on this PrintStream.
     */
    public void println(boolean bool) {
        println(String.valueOf(bool));
    }

    protected void setError() {
        ioError = true;
    }

    /**
     * Writes <code>count</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at <code>offset</code> to this
     * PrintStream. This implementation writes the <code>buffer</code> to the
     * target OutputStream and if this PrintStream is set to autoflush, flushes
     * it. If an error occurs, set an error in this PrintStream to
     * <code>true</code>.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param count
     *            number of bytes in buffer to write
     * 
     * @throws IndexOutOfBoundsException
     *             If offset or count are outside of bounds.
     */
    @Override
    public void write(byte[] buffer, int offset, int count) {
        if (buffer == null) {
            throw new NullPointerException();
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || count < 0
                || count > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        synchronized (this) {
            if (out == null) {
                setError();
                return;
            }
            try {
                out.write(buffer, offset, count);
                if (autoflush) {
                    flush();
                }
            } catch (IOException e) {
                setError();
            }
        }
    }

    /**
     * Writes the specified byte <code>oneByte</code> to this PrintStream.
     * Only the low order byte of <code>oneByte</code> is written. This
     * implementation writes <code>oneByte</code> to the target OutputStream.
     * If <code>oneByte</code> is equal to the character <code>'\n'</code>
     * and this PrintSteam is set to autoflush, the target OutputStream is
     * flushed.
     * 
     * @param oneByte
     *            the byte to be written
     */
    @Override
    public synchronized void write(int oneByte) {
        if (out == null) {
            setError();
            return;
        }
        try {
            out.write(oneByte);
            if (autoflush && (oneByte & 0xFF) == '\n') {
                flush();
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Append a char <code>c</code> to the PrintStream. The
     * PrintStream.append(<code>c</code>) works the same way as
     * PrintStream.print(<code>c</code>).
     * 
     * @param c
     *            The character appended to the PrintStream.
     * @return The PrintStream.
     */
    public PrintStream append(char c) {
        print(c);
        return this;
    }

    /**
     * Append a CharSequence <code>csq</code> to the PrintStream. The
     * PrintStream.append(<code>csq</code>) works the same way as
     * PrintStream.print(<code>csq</code>.toString()). If <code>csq</code>
     * is null, then a CharSequence just contains then "null" will be
     * substituted for <code>csq</code>.
     * 
     * @param csq
     *            The CharSequence appended to the PrintStream.
     * @return The PrintStream.
     */
    public PrintStream append(CharSequence csq) {
        if (null == csq) {
            print(TOKEN_NULL);
        } else {
            print(csq.toString());
        }
        return this;
    }

    /**
     * Append a subsequence of a CharSequence <code>csq</code> to the
     * PrintStream. The first char and the last char of the subsequnce is
     * specified by the parameter <code>start</code> and <code>end</code>.
     * The PrintStream.append(<code>csq</code>) works the same way as
     * PrintStream.print (<code>csq</code>csq.subSequence(<code>start</code>,
     * <code>end</code>).toString). If <code>csq</code> is null, then
     * "null" will be substituted for <code>csq</code>.
     * 
     * @param csq
     *            The CharSequence appended to the PrintStream.
     * @param start
     *            The index of the first char in the CharSequence appended to
     *            the PrintStream.
     * @param end
     *            The index of the char after the last one in the CharSequence
     *            appended to the PrintStream.
     * @return The PrintStream.
     * @throws IndexOutOfBoundsException
     *             If start is less than end, end is greater than the length of
     *             the CharSequence, or start or end is negative.
     */
    public PrintStream append(CharSequence csq, int start, int end) {
        if (null == csq) {
            print(TOKEN_NULL.substring(start, end));
        } else {
            print(csq.subSequence(start, end).toString());
        }
        return this;
    }
}

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
 * Wraps an existing {@link OutputStream} and provides convenience methods for
 * writing common data types in a human readable format. This is not to be
 * confused with DataOutputStream which is used for encoding common data types
 * so that they can be read back in. No {@code IOException} is thrown by this
 * class. Instead, callers should use {@link #checkError()} to see if a problem
 * has occurred in this stream.
 * 
 * @since Android 1.0
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
     * Constructs a new {@code PrintStream} with {@code out} as its target
     * stream. By default, the new print stream does not automatically flush its
     * contents to the target stream when a newline is encountered.
     * 
     * @param out
     *            the target output stream.
     * @throws NullPointerException
     *             if {@code out} is {@code null}.
     * @since Android 1.0
     */
    public PrintStream(OutputStream out) {
        super(out);
        if (out == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Constructs a new {@code PrintStream} with {@code out} as its target
     * stream. The parameter {@code autoflush} determines if the print stream
     * automatically flushes its contents to the target stream when a newline is
     * encountered.
     * 
     * @param out
     *            the target output stream.
     * @param autoflush
     *            indicates whether to flush contents upon encountering a
     *            newline sequence.
     * @throws NullPointerException
     *             if {@code out} is {@code null}.
     * @since Android 1.0
     */
    public PrintStream(OutputStream out, boolean autoflush) {
        super(out);
        if (out == null) {
            throw new NullPointerException();
        }
        this.autoflush = autoflush;
    }

    /**
     * Constructs a new {@code PrintStream} with {@code out} as its target
     * stream and using the character encoding {@code enc} while writing. The
     * parameter {@code autoflush} determines if the print stream automatically
     * flushes its contents to the target stream when a newline is encountered.
     * 
     * @param out
     *            the target output stream.
     * @param autoflush
     *            indicates whether or not to flush contents upon encountering a
     *            newline sequence.
     * @param enc
     *            the non-null string describing the desired character encoding.
     * @throws NullPointerException
     *             if {@code out} or {@code enc} are {@code null}.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code enc} is not supported.
     * @since Android 1.0
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
     * Constructs a new {@code PrintStream} with {@code file} as its target. The
     * virtual machine's default character set is used for character encoding.
     * 
     * @param file
     *            the target file. If the file already exists, its contents are
     *            removed, otherwise a new file is created.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     * @throws SecurityException
     *             if a security manager exists and it denies writing to the
     *             target file.
     * @since Android 1.0
     */
    public PrintStream(File file) throws FileNotFoundException {
        super(new FileOutputStream(file));
    }

    /**
     * Constructs a new {@code PrintStream} with {@code file} as its target. The
     * character set named {@code csn} is used for character encoding.
     * 
     * @param file
     *            the target file. If the file already exists, its contents are
     *            removed, otherwise a new file is created.
     * @param csn
     *            the name of the character set used for character encoding.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     * @throws NullPointerException
     *             if {@code csn} is {@code null}.
     * @throws SecurityException
     *             if a security manager exists and it denies writing to the
     *             target file.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code csn} is not supported.
     * @since Android 1.0
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
     * Constructs a new {@code PrintStream} with the file identified by
     * {@code fileName} as its target. The virtual machine's default character
     * set is used for character encoding.
     * 
     * @param fileName
     *            the target file's name. If the file already exists, its
     *            contents are removed, otherwise a new file is created.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     * @throws SecurityException
     *             if a security manager exists and it denies writing to the
     *             target file.
     * @since Android 1.0
     */
    public PrintStream(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    /**
     * Constructs a new {@code PrintStream} with the file identified by
     * {@code fileName} as its target. The character set named {@code csn} is
     * used for character encoding.
     * 
     * @param fileName
     *            the target file's name. If the file already exists, its
     *            contents are removed, otherwise a new file is created.
     * @param csn
     *            the name of the character set used for character encoding.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     * @throws NullPointerException
     *             if {@code csn} is {@code null}.
     * @throws SecurityException
     *             if a security manager exists and it denies writing to the
     *             target file.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code csn} is not supported.
     * @since Android 1.0
     */
    public PrintStream(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new File(fileName), csn);
    }

    /**
     * Flushes this stream and returns the value of the error flag.
     * 
     * @return {@code true} if either an {@code IOException} has been thrown
     *         previously or if {@code setError()} has been called;
     *         {@code false} otherwise.
     * @see #setError()         
     * @since Android 1.0
     */
    public boolean checkError() {
        if (out != null) {
            flush();
        }
        return ioError;
    }

    /**
     * Closes this print stream. Flushes this stream and then closes the target
     * stream. If an I/O error occurs, this stream's error state is set to
     * {@code true}.
     * 
     * @since Android 1.0
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
     * Ensures that all pending data is sent out to the target stream. It also
     * flushes the target stream. If an I/O error occurs, this stream's error
     * state is set to {@code true}.
     * 
     * @since Android 1.0
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
     * Writes a string formatted by an intermediate {@code Formatter} to the
     * target stream using the specified format string and arguments. For the
     * locale, the default value of the current virtual machine instance is
     * used.
     * 
     * @param format
     *            the format string used for {@link java.util.Formatter#format}.
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by the {@code format} string,
     *            then the additional arguments are ignored.
     * @return this stream.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException
     *             if {@code format} is {@code null}.
     * @since Android 1.0
     */
    public PrintStream format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    /**
     * Writes a string formatted by an intermediate {@link Formatter} to this
     * stream using the specified locale, format string and arguments.
     * 
     * @param l
     *            the locale used in the method. No localization will be applied
     *            if {@code l} is {@code null}.
     * @param format
     *            the format string used for {@link java.util.Formatter#format}.
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by the {@code format} string,
     *            then the additional arguments are ignored.
     * @return this stream.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException
     *             if {@code format} is {@code null}.
     * @since Android 1.0
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
     * this stream's {@code #format(String, Object...)} method. For the locale,
     * the default value of the current virtual machine instance is used.
     * 
     * @param format
     *            the format string used for
     *            {@link java.util.Formatter#format}.
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by the {@code format} string,
     *            then the additional arguments are ignored.
     * @return this stream.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException
     *             if {@code format} is {@code null}.
     * @since Android 1.0
     */
    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }

    /**
     * Prints a formatted string. The behavior of this method is the same as
     * this stream's {@code #format(Locale, String, Object...)} method.
     * 
     * @param l
     *            the locale used in the method. No localization will be applied
     *            if {@code l} is {@code null}.
     * @param format
     *            the format string used for {@link java.util.Formatter#format}.
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by the {@code format} string,
     *            then the additional arguments are ignored.
     * @return this stream.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException
     *             if {@code format} is {@code null}.
     * @since Android 1.0
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
     * Prints the string representation of the specified character array
     * to the target stream.
     * 
     * @param charArray
     *            the character array to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */
    public void print(char[] charArray) {
        print(new String(charArray, 0, charArray.length));
    }

    /**
     * Prints the string representation of the specified character to the target
     * stream.
     * 
     * @param ch
     *            the character to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */
    public void print(char ch) {
        print(String.valueOf(ch));
    }

    /**
     * Prints the string representation of the specified double to the target
     * stream.
     * 
     * @param dnum
     *            the double value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */
    public void print(double dnum) {
        print(String.valueOf(dnum));
    }

    /**
     * Prints the string representation of the specified float to the target
     * stream.
     * 
     * @param fnum
     *            the float value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void print(float fnum) {
        print(String.valueOf(fnum));
    }

    /**
     * Prints the string representation of the specified integer to the target
     * stream.
     * 
     * @param inum
     *            the integer value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void print(int inum) {
        print(String.valueOf(inum));
    }

    /**
     * Prints the string representation of the specified long to the target
     * stream.
     * 
     * @param lnum
     *            the long value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void print(long lnum) {
        print(String.valueOf(lnum));
    }

    /**
     * Prints the string representation of the specified object to the target
     * stream.
     * 
     * @param obj
     *            the object to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    /**
     * Prints a string to the target stream. The string is converted to an array
     * of bytes using the encoding chosen during the construction of this
     * stream. The bytes are then written to the target stream with
     * {@code write(int)}.
     * <p>
     * If an I/O error occurs, this stream's error state is set to {@code true}.
     * </p>
     * 
     * @param str
     *            the string to print to the target stream.
     * @see #write(int)
     * @since Android 1.0
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
     * Prints the string representation of the specified boolean to the target
     * stream.
     * 
     * @param bool
     *            the boolean value to print the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void print(boolean bool) {
        print(String.valueOf(bool));
    }

    /**
     * Prints the string representation of the system property
     * {@code "line.separator"} to the target stream.
     * 
     * @since Android 1.0
     */
    public void println() {
        newline();
    }

    /**
     * Prints the string representation of the specified character array
     * followed by the system property {@code "line.separator"} to the target
     * stream.
     * 
     * @param charArray
     *            the character array to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */
    public void println(char[] charArray) {
        println(new String(charArray, 0, charArray.length));
    }

    /**
     * Prints the string representation of the specified character followed by
     * the system property {@code "line.separator"} to the target stream.
     * 
     * @param ch
     *            the character to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */
    public void println(char ch) {
        println(String.valueOf(ch));
    }

    /**
     * Prints the string representation of the specified double followed by the
     * system property {@code "line.separator"} to the target stream.
     * 
     * @param dnum
     *            the double value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */
    public void println(double dnum) {
        println(String.valueOf(dnum));
    }

    /**
     * Prints the string representation of the specified float followed by the
     * system property {@code "line.separator"} to the target stream.
     * 
     * @param fnum
     *            the float value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
   public void println(float fnum) {
        println(String.valueOf(fnum));
    }

   /**
     * Prints the string representation of the specified integer followed by the
     * system property {@code "line.separator"} to the target stream.
     * 
     * @param inum
     *            the integer value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void println(int inum) {
        println(String.valueOf(inum));
    }

    /**
     * Prints the string representation of the specified long followed by the
     * system property {@code "line.separator"} to the target stream.
     * 
     * @param lnum
     *            the long value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void println(long lnum) {
        println(String.valueOf(lnum));
    }

    /**
     * Prints the string representation of the specified object followed by the
     * system property {@code "line.separator"} to the target stream.
     * 
     * @param obj
     *            the object to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void println(Object obj) {
        println(String.valueOf(obj));
    }

    /**
     * Prints a string followed by the system property {@code "line.separator"}
     * to the target stream. The string is converted to an array of bytes using
     * the encoding chosen during the construction of this stream. The bytes are
     * then written to the target stream with {@code write(int)}.
     * <p>
     * If an I/O error occurs, this stream's error state is set to {@code true}.
     * </p>
     * 
     * @param str
     *            the string to print to the target stream.
     * @see #write(int)
     * @since Android 1.0
     */
    public synchronized void println(String str) {
        print(str);
        newline();
    }

    /**
     * Prints the string representation of the specified boolean followed by the
     * system property {@code "line.separator"} to the target stream.
     * 
     * @param bool
     *            the boolean value to print to the target stream.
     * @see #print(String)
     * @since Android 1.0
     */   
    public void println(boolean bool) {
        println(String.valueOf(bool));
    }

    /**
     * Sets the error flag of this print stream to {@code true}.
     * 
     * @since Android 1.0
     */
    protected void setError() {
        ioError = true;
    }

    /**
     * Writes {@code count} bytes from {@code buffer} starting at {@code offset}
     * to the target stream. If autoflush is set, this stream gets flushed after
     * writing the buffer.
     * <p>
     * This stream's error flag is set to {@code true} if this stream is closed
     * or an I/O error occurs.
     * </p>
     * 
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes in {@code buffer} to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is bigger than the length of {@code buffer}.
     * @see #flush()
     * @since Android 1.0
     */
    @Override
    public void write(byte[] buffer, int offset, int count) {
        // BEGIN android-changed
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); //$NON-NLS-1$
        }
        // avoid int overflow
        // Exception priorities (in case of multiple errors) differ from
        // RI, but are spec-compliant.
        // removed redundant check, used (offset | count) < 0
        // instead of (offset < 0) || (count < 0) to safe one operation
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        // END android-changed
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
     * Writes one byte to the target stream. Only the least significant byte of
     * the integer {@code oneByte} is written. This stream is flushed if
     * {@code oneByte} is equal to the character {@code '\n'} and this stream is
     * set to autoflush.
     * <p>
     * This stream's error flag is set to {@code true} if it is closed or an I/O
     * error occurs.
     * </p>
     * 
     * @param oneByte
     *            the byte to be written
     * @since Android 1.0
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
     * Appends the character {@code c} to the target stream. This method works
     * the same way as {@link #print(char)}.
     * 
     * @param c
     *            the character to append to the target stream.
     * @return this stream.
     * @since Android 1.0
     */
    public PrintStream append(char c) {
        print(c);
        return this;
    }

    /**
     * Appends the character sequence {@code csq} to the target stream. This
     * method works the same way as {@code PrintStream.print(csq.toString())}.
     * If {@code csq} is {@code null}, then the string "null" is written to the
     * target stream.
     * 
     * @param csq
     *            the character sequence appended to the target stream.
     * @return this stream.
     * @since Android 1.0
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
     * Appends a subsequence of the character sequence {@code csq} to the target
     * stream. This method works the same way as {@code
     * PrintStream.print(csq.subsequence(start, end).toString())}. If {@code
     * csq} is {@code null}, then the specified subsequence of the string "null"
     * will be written to the target stream.
     * 
     * @param csq
     *            the character sequence appended to the target stream.
     * @param start
     *            the index of the first char in the character sequence appended
     *            to the target stream.
     * @param end
     *            the index of the character following the last character of the
     *            subsequence appended to the target stream.
     * @return this stream.
     * @throws IndexOutOfBoundsException
     *             if {@code start > end}, {@code start < 0}, {@code end < 0} or
     *             either {@code start} or {@code end} are greater or equal than
     *             the length of {@code csq}.
     * @since Android 1.0
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

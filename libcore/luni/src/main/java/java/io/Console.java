/* Licensed to the Apache Software Foundation (ASF) under one or more
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
package java.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Formatter;

/**
 * Provides access to the console, if available. The system-wide instance can
 * be accessed via {@link java.lang.System.console}.
 * @since 1.6
 * @hide
 */
public final class Console implements Flushable {
    private static final Object CONSOLE_LOCK = new Object();

    private static final Console console = makeConsole();
    private static native boolean isatty(int fd);

    private final ConsoleReader reader;
    private final PrintWriter writer;

    /**
     * Secret accessor for {@code System.console}.
     * @hide
     */
    public static Console getConsole() {
        return console;
    }

    private static Console makeConsole() {
        if (!isatty(0) || !isatty(1)) {
            return null;
        }
        try {
            return new Console();
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    private Console() throws IOException {
        this.reader = new ConsoleReader(System.in);
        this.writer = new ConsoleWriter(System.out);
    }

    public void flush() {
        writer.flush();
    }

    /**
     * Writes a formatted string to the console using
     * the specified format string and arguments.
     *
     * @param fmt the format string.
     * @param args the arguments used by the formatter.
     * @return the console instance.
     */
    public Console format(String fmt, Object... args) {
        Formatter f = new Formatter(writer);
        f.format(fmt, args);
        f.flush();
        return this;
    }

    /**
     * Equivalent to {@code format(fmt, args)}.
     */
    public Console printf(String fmt, Object... args) {
        return format(fmt, args);
    }

    /**
     * Returns the {@link Reader} associated with this console.
     */
    public Reader reader() {
        return reader;
    }

    /**
     * Reads a line from the console.
     *
     * @return the line, or null at EOF.
     */
    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /**
     * Reads a line from this console, using the specified prompt.
     * The prompt is given as a format string and optional arguments.
     * Note that this can be a source of errors: if it is possible that your
     * prompt contains {@code %} characters, you must use the format string {@code "%s"}
     * and pass the actual prompt as a parameter.
     *
     * @param fmt the format string.
     * @param args the arguments used by the formatter.
     * @return the line, or null at EOF.
     */
    public String readLine(String fmt, Object... args) {
        synchronized (CONSOLE_LOCK) {
            format(fmt, args);
            return readLine();
        }
    }

    /**
     * Reads a password from the console. The password will not be echoed to the display.
     *
     * @return a character array containing the password, or null at EOF.
     */
    public char[] readPassword() {
        synchronized (CONSOLE_LOCK) {
            int previousState = setEcho(false, 0);
            try {
                String password = readLine();
                writer.println(); // We won't have echoed the user's newline.
                return (password == null) ? null : password.toCharArray();
            } finally {
                setEcho(true, previousState);
            }
        }
    }

    private static int setEcho(boolean on, int previousState) {
        try {
            return setEchoImpl(on, previousState);
        } catch (IOException ex) {
            throw new IOError(ex);
        }
    }
    private static native int setEchoImpl(boolean on, int previousState) throws IOException;

    /**
     * Reads a password from the console. The password will not be echoed to the display.
     * A formatted prompt is also displayed.
     *
     * @param fmt the format string.
     * @param args the arguments used by the formatter.
     * @return a character array containing the password, or null at EOF.
     */
    public char[] readPassword(String fmt, Object... args) {
        synchronized (CONSOLE_LOCK) {
            format(fmt, args);
            return readPassword();
        }
    }

    /**
     * Returns the {@link Writer} associated with this console.
     */
    public PrintWriter writer() {
        return writer;
    }

    private static class ConsoleReader extends BufferedReader {
        public ConsoleReader(InputStream in) throws IOException {
            super(new InputStreamReader(in, System.getProperty("file.encoding")), 256);
            lock = CONSOLE_LOCK;
        }

        @Override
        public void close() {
            // Console.reader cannot be closed.
        }
    }

    private static class ConsoleWriter extends PrintWriter {
        public ConsoleWriter(OutputStream out) {
            super(out, true);
            lock = CONSOLE_LOCK;
        }

        @Override
        public void close() {
            // Console.writer cannot be closed.
            flush();
        }
    }
}

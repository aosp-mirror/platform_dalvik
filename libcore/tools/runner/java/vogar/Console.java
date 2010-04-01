/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vogar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Controls, formats and emits output to the command line. Command line output
 * can be generated both by java.util.logging and by direct calls to this class.
 */
public class Console {

    private final boolean stream;
    private final boolean color;
    private final String indent;

    private String currentName;
    private CurrentLine currentLine = CurrentLine.NEW;
    private final StringBuilder bufferedOutput = new StringBuilder();

    public Console(boolean stream, String indent, boolean color) {
        this.stream = stream;
        this.indent = indent;
        this.color = color;
    }

    public void configureJavaLogging(boolean verbose) {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override public String format(LogRecord r) {
                return logRecordToString(r);
            }
        });

        Logger logger = Logger.getLogger("vogar");
        logger.setLevel(verbose ? Level.FINE : Level.INFO);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    /**
     * Formats an alternating sequence of regular log messages and messages
     * streamed from a foreign process.
     */
    private String logRecordToString(LogRecord logRecord) {
        String message = logRecord.getMessage();

        if (logRecord.getThrown() != null) {
            StringWriter writer = new StringWriter();
            writer.write(message);
            writer.write("\n");
            logRecord.getThrown().printStackTrace(new PrintWriter(writer));
            message = writer.toString();
        }

        newLine();
        return message + "\n";
    }

    public void action(String name) {
        newLine();
        System.out.print("Action " + name);
        currentName = name;
        currentLine = CurrentLine.NAME;
    }

    /**
     * Prints the beginning of the named outcome.
     */
    public void outcome(String name) {
        // if the outcome and action names are the same, omit the outcome name
        if (name.equals(currentName)) {
            return;
        }

        currentName = name;
        newLine();
        System.out.print(indent + name);
        currentLine = CurrentLine.NAME;
    }

    /**
     * Appends the action output immediately to the stream when streaming is on,
     * or to a buffer when streaming is off. Buffered output will be held and
     * printed only if the outcome is unsuccessful.
     */
    public void streamOutput(String output) {
        if (stream) {
            printOutput(output);
        } else {
            bufferedOutput.append(output);
        }
    }

    /**
     * Writes the action's outcome.
     */
    public void printResult(Result result, boolean ok) {
        if (ok) {
            String prefix = (currentLine == CurrentLine.NAME) ? " " : "\n" + indent;
            System.out.println(prefix + green("OK (" + result + ")"));

        } else {
            if (bufferedOutput.length() > 0) {
                printOutput(bufferedOutput.toString());
                bufferedOutput.delete(0, bufferedOutput.length());
            }

            newLine();
            System.out.println(indent + red("FAIL (" + result + ")"));
        }

        currentName = null;
        currentLine = CurrentLine.NEW;
    }

    /**
     * Prints the action output with appropriate indentation.
     */
    private void printOutput(String streamedOutput) {
        String[] lines = messageToLines(streamedOutput);

        if (currentLine != CurrentLine.STREAMED_OUTPUT) {
            newLine();
            System.out.print(indent);
            System.out.print(indent);
        }
        System.out.print(lines[0]);
        currentLine = CurrentLine.STREAMED_OUTPUT;

        for (int i = 1; i < lines.length; i++) {
            newLine();

            if (lines[i].length() > 0) {
                System.out.print(indent);
                System.out.print(indent);
                System.out.print(lines[i]);
                currentLine = CurrentLine.STREAMED_OUTPUT;
            }
        }
    }

    /**
     * Inserts a linebreak if necessary.
     */
    private void newLine() {
        if (currentLine == CurrentLine.NEW) {
            return;
        }

        System.out.println();
        currentLine = CurrentLine.NEW;
    }

    /**
     * Status of a currently-in-progress line of output.
     */
    enum CurrentLine {

        /**
         * The line is blank.
         */
        NEW,

        /**
         * The line contains streamed application output. Additional streamed
         * output may be appended without additional line separators or
         * indentation.
         */
        STREAMED_OUTPUT,

        /**
         * The line contains the name of an action or outcome. The outcome's
         * result (such as "OK") can be appended without additional line
         * separators or indentation.
         */
        NAME,
    }

    /**
     * Returns an array containing the lines of the given text.
     */
    private String[] messageToLines(String message) {
        // pass Integer.MAX_VALUE so split doesn't trim trailing empty strings.
        return message.split("\r\n|\r|\n", Integer.MAX_VALUE);
    }

    private String green(String message) {
        return color ? ("\u001b[32;1m" + message + "\u001b[0m") : message;
    }

    private String red(String message) {
        return color ? ("\u001b[31;1m" + message + "\u001b[0m") : message;
    }
}

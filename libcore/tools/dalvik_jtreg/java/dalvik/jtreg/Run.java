// Copyright 2009 Google Inc. All Rights Reserved.

package dalvik.jtreg;

import com.sun.javatest.TestDescription;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * The outcome of a test run.
 */
public final class Run {

    private final String qualifiedName;
    private final String title;
    private final Result result;
    private final List<String> outputLines;

    public Run(TestDescription testDescription, Result result, List<String> outputLines) {
        this.qualifiedName = TestDescriptions.qualifiedName(testDescription);
        this.title = testDescription.getTitle();
        this.result = result;
        this.outputLines = outputLines;
    }

    public Run(TestDescription testDescription, Result result, Exception e) {
        this.qualifiedName = TestDescriptions.qualifiedName(testDescription);
        this.title = testDescription.getTitle();
        this.result = result;
        this.outputLines = throwableToLines(e);
    }

    private static List<String> throwableToLines(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        t.printStackTrace(out);
        return Arrays.asList(writer.toString().split("\\n"));
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(qualifiedName).append(" ").append(result)
                .append("\n  \"").append(title).append("\"");

        for (String output : outputLines) {
            builder.append("\n  ").append(output);
        }

        return builder.toString();
    }

    public enum Result {
        SKIPPED,
        COMPILE_FAILED,
        EXEC_FAILED,
        ERROR,
        SUCCESS
    }
}

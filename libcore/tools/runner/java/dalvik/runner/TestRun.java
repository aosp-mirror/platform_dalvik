/*
 * Copyright (C) 2009 The Android Open Source Project
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

package dalvik.runner;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * A test run and its outcome. This class tracks the complete lifecycle of a
 * single test run:
 * <ol>
 *   <li>the test source code (test directory, java file, test class)
 *   <li>the test identity (suite name, test name, qualified name)
 *   <li>the code to execute (test classes, user dir)
 *   <li>the result of execution (expected result, result, output lines)
 * </ol>
 */
public final class TestRun {

    private final File testDirectory;
    private final File testJava;
    private final String testClass;
    private final Class<? extends Runner> runnerClass;
    private final File runnerJava;
    private final Classpath runnerClasspath;

    private final String suiteName;
    private final String testName;
    private final String qualifiedName;
    private final String description;

    private boolean testCompiled;
    private File userDir = new File(System.getProperty("user.dir"));

    private ExpectedResult expectedResult = ExpectedResult.SUCCESS;
    private Result result;
    private List<String> outputLines;

    public TestRun(File testDirectory, File testJava, String testClass,
            String suiteName, String testName, String qualifiedName,
            String description, Class<? extends Runner> runnerClass,
            File runnerJava, Classpath runnerClasspath) {
        this.qualifiedName = qualifiedName;
        this.suiteName = suiteName;
        this.testName = testName;
        this.testDirectory = testDirectory;
        this.testJava = testJava;
        this.description = description;
        this.testClass = testClass;
        this.runnerClass = runnerClass;
        this.runnerJava = runnerJava;
        this.runnerClasspath = runnerClasspath;
    }

    /**
     * Returns the local directory containing this test's java file.
     */
    public File getTestDirectory() {
        return testDirectory;
    }

    public File getTestJava() {
        return testJava;
    }

    /**
     * Returns the executable test's classname, such as java.lang.IntegerTest
     * or BitTwiddle.
     */
    public String getTestClass() {
        return testClass;
    }

    /**
     * Returns the test suite name, such as java.lang.Integer or
     * java.lang.IntegerTest.
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * Returns the specific test name, such as BitTwiddle or testBitTwiddle.
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Returns a unique identifier for this test.
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Returns an English description of this test, or null if no such
     * description is known.
     */
    public String getDescription() {
        return description;
    }

    public void setExpectedResult(ExpectedResult expectedResult) {
        this.expectedResult = expectedResult;
    }

    /**
     * Set when the test is successfully compiled.
     */
    public void setTestCompiled(boolean testCompiled) {
        this.testCompiled = testCompiled;
    }

    public boolean getTestCompiled() {
        return testCompiled;
    }

    /**
     * Initializes the directory from which local files can be read by the test.
     */
    public void setUserDir(File base) {
        this.userDir = base;
    }

    public File getUserDir() {
        return userDir;
    }

    /**
     * Returns true if this test is ready for execution. Such tests have their
     * classpath prepared and have not yet been assigned a result.
     */
    public boolean isRunnable() {
        return testCompiled && result == null;
    }

    public void setResult(Result result, Throwable e) {
        setResult(result, throwableToLines(e));
    }

    public void setResult(Result result, List<String> outputLines) {
        if (this.result != null) {
            throw new IllegalStateException("result already set");
        }

        this.result = result;
        this.outputLines = outputLines;
    }

    private static List<String> throwableToLines(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        t.printStackTrace(out);
        return Arrays.asList(writer.toString().split("\\n"));
    }

    public Result getResult() {
        return result;
    }

    public List<String> getOutputLines() {
        return outputLines;
    }

    public Class<? extends Runner> getRunnerClass() {
        return runnerClass;
    }

    public File getRunnerJava() {
        return runnerJava;
    }

    public Classpath getRunnerClasspath() {
        return runnerClasspath;
    }

    /**
     * Returns true if the outcome of this run matches what was expected.
     */
    public boolean isExpectedResult() {
        return result == expectedResult.getResult() && matchesExpectedPattern();
    }

    /**
     * Returns true if the test's output matches the expected output.
     */
    private boolean matchesExpectedPattern() {
        return expectedResult.getPattern()
                .matcher(Strings.join(outputLines, "\n"))
                .matches();
    }

    /**
     * Returns the failure message for this failed test run. This message is
     * intended to help to diagnose why the test result didn't match what was
     * expected.
     */
    public String getFailureMessage() {
        StringBuilder builder = new StringBuilder();

        if (expectedResult.getResult() != Result.SUCCESS
                && expectedResult.getResult() != result) {
            builder.append("Expected result: ")
                    .append(expectedResult.getResult())
                    .append("\n");
        }

        if (!matchesExpectedPattern()) {
            builder.append("Expected output to match \"")
                    .append(expectedResult.getPattern().pattern())
                    .append("\"\n");
        }

        for (String output : outputLines) {
            builder.append(output).append("\n");
        }

        return builder.toString();
    }

    @Override public String toString() {
        return qualifiedName;
    }
}

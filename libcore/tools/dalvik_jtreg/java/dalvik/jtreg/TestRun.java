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

package dalvik.jtreg;

import com.sun.javatest.TestDescription;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * A test run and its outcome.
 */
public final class TestRun {

    private final TestDescription testDescription;
    private final String qualifiedName;
    private final ExpectedResult expectedResult;

    private File base;
    private File deviceDex;

    private Result result;
    private List<String> outputLines;


    public TestRun(String qualifiedName, TestDescription testDescription,
            ExpectedResult expectedResult) {
        this.qualifiedName = qualifiedName;
        this.testDescription = testDescription;
        this.expectedResult = expectedResult;
    }

    public TestDescription getTestDescription() {
        return testDescription;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Initializes the on-device base directory from which the test program
     * shall be executed, and the dex file containing that program.
     */
    public void initInstalledFiles(File base, File deviceDex) {
        if (this.base != null) {
            throw new IllegalStateException();
        }

        this.base = base;
        this.deviceDex = deviceDex;
    }

    /**
     * Returns true if this test is ready for execution on a device.
     */
    public boolean isRunnable() {
        return base != null && deviceDex != null;
    }

    /**
     * Returns the test's base directory, from which local files can be read by
     * the test.
     */
    public File getBase() {
        return base;
    }

    /**
     * Returns the jar file containing the test code.
     */
    public File getDeviceDex() {
        return deviceDex;
    }

    public void initResult(Result result, Exception e) {
        initResult(result, throwableToLines(e));
    }

    public void initResult(Result result, List<String> outputLines) {
        if (this.result != null) {
            throw new IllegalStateException();
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

    public ExpectedResult getExpectedResult() {
        return expectedResult;
    }

}

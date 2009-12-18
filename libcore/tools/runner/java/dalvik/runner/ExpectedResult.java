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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * The expected outcome of a test execution. This is typically encoded in a
 * properties file named by the test name and the {@code .expected} suffix; for
 * example, {@code java.util.Arrays.CopyMethods.expected}.
 */
class ExpectedResult {

    /**
     * Property identifier for the test's expected result, such as {@code
     * EXEC_FAILED}. This property is required.
     */
    static final String RESULT = "result";

    /**
     * Property identifier for a regular expression that is the expected output
     * will match. This property is optional.
     */
    static final String PATTERN = "pattern";

    /**
     * The expectation of a general successful test run.
     */
    static final ExpectedResult SUCCESS = new ExpectedResult(Result.SUCCESS, ".*");

    private final Result result;
    private final String pattern;

    private ExpectedResult(File expectationFile) throws IOException {
        Properties properties = new Properties();
        FileInputStream in = new FileInputStream(expectationFile);
        properties.load(in);
        in.close();

        result = Result.valueOf(properties.getProperty(RESULT));
        pattern = properties.getProperty(PATTERN);
    }

    private ExpectedResult(Result result, String pattern) {
        this.result = result;
        this.pattern = pattern;
    }

    public Result getResult() {
        return result;
    }

    public String getPattern() {
        return pattern;
    }

    public static ExpectedResult forRun(
            Set<File> searchDirectories, TestRun testRun) throws IOException {
        for (File expectationDir : searchDirectories) {
            File expectationFile = new File(
                    expectationDir, testRun.getQualifiedName() + ".expected");
            if (expectationFile.exists()) {
                return new ExpectedResult(expectationFile);
            }
        }

        return SUCCESS;
    }
}

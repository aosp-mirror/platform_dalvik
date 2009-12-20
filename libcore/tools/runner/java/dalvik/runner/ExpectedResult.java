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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The expected outcome of a test execution. This is typically encoded in the
 * expectations text file, which has the following format:
 * <pre>
 * test java.io.StreamTokenizer.Reset
 * result UNSUPPORTED
 * pattern .*should get token \[, but get -1.*
 *
 * # should we fix this?
 * test java.util.Arrays.CopyMethods
 * result COMPILE_FAILED
 * pattern .*cannot find symbol.*
 * </pre>
 */
class ExpectedResult {

    private static final Logger logger = Logger.getLogger(ExpectedResult.class.getName());

    /** Matches lines in the file containing a key and value pair. */
    private static final Pattern KEY_VALUE_PAIR_PATTERN = Pattern.compile("(\\w+)\\s+(.+)");

    /** The pattern to use when no expected output is specified */
    private static final Pattern MATCH_ALL_PATTERN
            = Pattern.compile(".*", Pattern.MULTILINE | Pattern.DOTALL);

    /** The expectation of a general successful test run. */
    static final ExpectedResult SUCCESS = new ExpectedResult(Result.SUCCESS, null);

    /** The test's expected result, such as {@code EXEC_FAILED}. */
    private final Result result;

    /** The pattern the expected output will match. */
    private final Pattern pattern;

    private ExpectedResult(Result result, String pattern) {
        if (result == null) {
            throw new IllegalArgumentException();
        }

        this.result = result;
        this.pattern = pattern != null
                ? Pattern.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL)
                : MATCH_ALL_PATTERN;
    }

    public Result getResult() {
        return result;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public static Map<String, ExpectedResult> parse(File expectationsFile)
            throws IOException {
        logger.fine("loading expectations file " + expectationsFile);

        BufferedReader reader = new BufferedReader(new FileReader(expectationsFile));
        try {
            Map<String, ExpectedResult> results = new HashMap<String, ExpectedResult>();
            Matcher keyValuePairMatcher = KEY_VALUE_PAIR_PATTERN.matcher("");

            // the fields of interest for the current element
            String qualifiedName = null;
            Result result = null;
            String pattern = null;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.length() == 0 || line.startsWith("#")) {
                    continue; // skip comment and blank lines
                }

                keyValuePairMatcher.reset(line);
                if (!keyValuePairMatcher.matches()) {
                    throw new IllegalArgumentException("Unexpected line " + line
                            + " in file " + expectationsFile);
                }

                String key = keyValuePairMatcher.group(1);
                String value = keyValuePairMatcher.group(2);
                if (key.equals("result") && result == null) {
                    result = Result.valueOf(value);

                } else if (key.equals("pattern") && pattern == null) {
                    pattern = value;

                } else if (key.equals("test")) {
                    // when we encounter a new qualified name, the previous
                    // element is complete. Add it to the results.
                    if (qualifiedName != null) {
                        ExpectedResult expectation = new ExpectedResult(result, pattern);
                        ExpectedResult previous = results.put(qualifiedName, expectation);
                        if (previous != null) {
                            throw new IllegalArgumentException(
                                    "Duplicate expectations for " + qualifiedName);
                        }

                        result = null;
                        pattern = null;
                    }

                    qualifiedName = value;

                } else {
                    throw new IllegalArgumentException("Unexpected key " + key
                            + " in file " + expectationsFile);
                }
            }

            // add the last element in the file
            if (qualifiedName != null) {
                ExpectedResult expectation = new ExpectedResult(result, pattern);
                ExpectedResult previous = results.put(qualifiedName, expectation);
                if (previous != null) {
                    throw new IllegalArgumentException(
                            "Duplicate expectations for " + qualifiedName);
                }
            }

            logger.fine("loaded " + results.size() + " expectations.");
            return results;
        } finally {
            reader.close();
        }
    }
}

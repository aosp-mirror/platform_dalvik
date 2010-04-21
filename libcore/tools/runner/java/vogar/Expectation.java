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

import java.util.regex.Pattern;

/**
 * The expected result of an action execution. This is typically encoded in the
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
final class Expectation {

    /** The pattern to use when no expected output is specified */
    private static final Pattern MATCH_ALL_PATTERN
            = Pattern.compile(".*", Pattern.MULTILINE | Pattern.DOTALL);

    /** The expectation of a general successful run. */
    static final Expectation SUCCESS = new Expectation(Result.SUCCESS, null);

    /** The action's expected result, such as {@code EXEC_FAILED}. */
    private final Result result;

    /** The pattern the expected output will match. */
    private final Pattern pattern;

    public Expectation(Result result, String pattern) {
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

    /**
     * Returns true if {@code outcome} matches this expectation.
     */
    public boolean matches(Outcome outcome) {
        return result == outcome.getResult() && patternMatches(outcome);
    }

    private boolean patternMatches(Outcome outcome) {
        return pattern.matcher(Strings.join(outcome.getOutputLines(), "\n")).matches();
    }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A database of expected outcomes. Entries in this database come in two forms.
 * <ul>
 *   <li>Outcome expectations name an outcome (or its prefix, such as
 *       "java.util"), its expected result, and an optional pattern to match
 *       the expected output.
 *   <li>Failure expectations include a pattern that may match the output of any
 *       outcome. These expectations are useful for hiding failures caused by
 *       cross-cutting features that aren't supported.
 * </ul>
 *
 * <p>If an outcome matches both an outcome expectation and a failure
 * expectation, the outcome expectation will be returned.
 */
final class ExpectationStore {

    private static final Logger logger = Logger.getLogger(ExpectationStore.class.getName());

    /** Matches lines in the file containing a key and value pair. */
    private static final Pattern KEY_VALUE_PAIR_PATTERN = Pattern.compile("(\\w+)\\s+(.+)");

    private final Map<String, Expectation> outcomes = new HashMap<String, Expectation>();
    private final Map<String, Expectation> failures = new HashMap<String, Expectation>();

    private ExpectationStore() {}

    /**
     * Finds the expected result for the specified action or outcome name. This
     * returns a value for all names, even if no explicit expectation was set.
     */
    public Expectation get(String name) {
        Expectation byName = getByName(name);
        return byName != null ? byName : Expectation.SUCCESS;
    }

    /**
     * Finds the expected result for the specified outcome after it has
     * completed. Unlike {@code get()}, this also takes into account the
     * outcome's output.
     */
    public Expectation get(Outcome outcome) {
        Expectation byName = getByName(outcome.getName());
        if (byName != null) {
            return byName;
        }

        for (Map.Entry<String, Expectation> entry : failures.entrySet()) {
            if (entry.getValue().matches(outcome)) {
                return entry.getValue();
            }
        }

        return Expectation.SUCCESS;
    }

    private Expectation getByName(String name) {
        while (true) {
            Expectation expectation = outcomes.get(name);
            if (expectation != null) {
                return expectation;
            }

            int dot = name.lastIndexOf('.');
            if (dot == -1) {
                return null;
            }

            name = name.substring(0, dot);
        }
    }

    public static ExpectationStore parse(Set<File> expectationFiles) throws IOException {
        ExpectationStore result = new ExpectationStore();
        for (File f : expectationFiles) {
            if (f.exists()) {
                result.parse(f);
            }
        }
        return result;
    }

    public void parse(File expectationsFile) throws IOException {
        logger.fine("loading expectations file " + expectationsFile);

        BufferedReader reader = new BufferedReader(new FileReader(expectationsFile));
        int count = 0;
        try {
            Matcher keyValuePairMatcher = KEY_VALUE_PAIR_PATTERN.matcher("");

            // the fields of interest for the current element
            String type = null;
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

                } else if (key.equals("test") || key.equals("failure")) {
                    // when we encounter a new qualified name, the previous
                    // element is complete. Add it to the results.
                    if (qualifiedName != null) {
                        count++;
                        put(type, qualifiedName, result, pattern);
                        result = null;
                        pattern = null;
                    }
                    type = key;
                    qualifiedName = value;

                } else {
                    throw new IllegalArgumentException("Unexpected key " + key
                            + " in file " + expectationsFile);
                }
            }

            // add the last element in the file
            if (qualifiedName != null) {
                count++;
                put(type, qualifiedName, result, pattern);
            }

            logger.fine("loaded " + count + " expectations from " + expectationsFile);
        } finally {
            reader.close();
        }
    }

    void put(String type, String qualifiedName, Result result, String pattern) {
        Expectation expectation = new Expectation(result, pattern);
        Map<String, Expectation> map = "test".equals(type) ? outcomes : failures;
        if (map.put(qualifiedName, expectation) != null) {
            throw new IllegalArgumentException(
                    "Duplicate expectations for " + qualifiedName);
        }
    }
}

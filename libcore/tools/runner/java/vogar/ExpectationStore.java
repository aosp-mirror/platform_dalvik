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
 * A database of expected outcomes.
 */
final class ExpectationStore {

    private static final Logger logger = Logger.getLogger(ExpectationStore.class.getName());

    /** Matches lines in the file containing a key and value pair. */
    private static final Pattern KEY_VALUE_PAIR_PATTERN = Pattern.compile("(\\w+)\\s+(.+)");

    private final Map<String, Expectation> expectedResults;

    private ExpectationStore(Map<String, Expectation> expectedResults) {
        this.expectedResults = expectedResults;
    }

    /**
     * Finds the expected result for the specified action or outcome. This
     * returns a value for all names, even if no explicit expectation was set.
     */
    public Expectation get(String name) {
        while (true) {
            Expectation expectation = expectedResults.get(name);
            if (expectation != null) {
                return expectation;
            }

            int dot = name.lastIndexOf('.');
            if (dot == -1) {
                return Expectation.SUCCESS;
            }

            name = name.substring(0, dot);
        }
    }

    public static ExpectationStore parse(Set<File> expectationFiles) throws IOException {
        Map<String, Expectation> expectedResults = new HashMap<String, Expectation>();
        for (File f : expectationFiles) {
            if (f.exists()) {
                expectedResults.putAll(parse(f));
            }
        }
        return new ExpectationStore(expectedResults);
    }


    public static Map<String, Expectation> parse(File expectationsFile)
            throws IOException {
        logger.fine("loading expectations file " + expectationsFile);

        BufferedReader reader = new BufferedReader(new FileReader(expectationsFile));
        try {
            Map<String, Expectation> results = new HashMap<String, Expectation>();
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
                        Expectation expectation = new Expectation(result, pattern);
                        Expectation previous = results.put(qualifiedName, expectation);
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
                Expectation expectation = new Expectation(result, pattern);
                Expectation previous = results.put(qualifiedName, expectation);
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

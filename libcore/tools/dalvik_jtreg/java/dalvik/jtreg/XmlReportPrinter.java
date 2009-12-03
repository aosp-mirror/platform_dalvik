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

import org.kxml2.io.KXmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


/**
 * Writes JUnit results to a series of XML files in a format consistent with
 * Ant's XMLJUnitResultFormatter.
 *
 * <p>Unlike Ant's formatter, this class does not report the execution time of
 * tests.
 *
 * TODO: unify this and com.google.coretests.XmlReportPrinter
 */
public class XmlReportPrinter {

    /**
     * Test results of these types are omitted from the report, as if the tests
     * never existed. Equivalent to the core test runner's @BrokenTest.
     */
    private static final EnumSet<Result> IGNORED_RESULTS
            = EnumSet.of(Result.COMPILE_FAILED);

    private static final EnumSet<Result> FAILED_RESULTS
            = EnumSet.of(Result.EXEC_FAILED);
    private static final EnumSet<Result> ERROR_RESULTS
            = EnumSet.complementOf(EnumSet.of(Result.EXEC_FAILED, Result.SUCCESS));

    private static final String TESTSUITE = "testsuite";
    private static final String TESTCASE = "testcase";
    private static final String ERROR = "error";
    private static final String FAILURE = "failure";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TIME = "time";
    private static final String ATTR_ERRORS = "errors";
    private static final String ATTR_FAILURES = "failures";
    private static final String ATTR_TESTS = "tests";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_MESSAGE = "message";
    private static final String PROPERTIES = "properties";
    private static final String ATTR_CLASSNAME = "classname";
    private static final String TIMESTAMP = "timestamp";
    private static final String HOSTNAME = "hostname";

    /** the XML namespace */
    private static final String ns = null;

    /**
     * Populates the directory with the report data from the completed tests.
     */
    public int generateReports(File directory, Collection<TestRun> results) {
        Map<String, Suite> suites = testsToSuites(results);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmt);
        dateFormat.setLenient(true);
        String timestamp = dateFormat.format(new Date());

        for (Suite suite : suites.values()) {
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(new File(directory, "TEST-" + suite.name + ".xml"));

                KXmlSerializer serializer = new KXmlSerializer();
                serializer.setOutput(stream, "UTF-8");
                serializer.startDocument("UTF-8", null);
                serializer.setFeature(
                        "http://xmlpull.org/v1/doc/features.html#indent-output", true);
                suite.print(serializer, timestamp);
                serializer.endDocument();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        return suites.size();
    }

    private Map<String, Suite> testsToSuites(Collection<TestRun> testRuns) {
        Map<String, Suite> result = new LinkedHashMap<String, Suite>();
        for (TestRun testRun : testRuns) {
            if (IGNORED_RESULTS.contains(testRun.getResult())) {
                continue;
            }

            String suiteName = testRun.getSuiteName();
            Suite suite = result.get(suiteName);
            if (suite == null) {
                suite = new Suite(suiteName);
                result.put(suiteName, suite);
            }

            suite.tests.add(testRun);
            if (FAILED_RESULTS.contains(testRun.getResult())) {
                suite.failuresCount++;
            } else if (ERROR_RESULTS.contains(testRun.getResult())) {
                suite.errorsCount++;
            }
        }
        return result;
    }

    static class Suite {
        private final String name;
        private final List<TestRun> tests = new ArrayList<TestRun>();
        private int failuresCount;
        private int errorsCount;

        Suite(String name) {
            this.name = name;
        }

        void print(KXmlSerializer serializer, String timestamp) throws IOException {
            serializer.startTag(ns, TESTSUITE);
            serializer.attribute(ns, ATTR_NAME, name);
            serializer.attribute(ns, ATTR_TESTS, Integer.toString(tests.size()));
            serializer.attribute(ns, ATTR_FAILURES, Integer.toString(failuresCount));
            serializer.attribute(ns, ATTR_ERRORS, Integer.toString(errorsCount));
            serializer.attribute(ns, ATTR_TIME, "0");
            serializer.attribute(ns, TIMESTAMP, timestamp);
            serializer.attribute(ns, HOSTNAME, "localhost");
            serializer.startTag(ns, PROPERTIES);
            serializer.endTag(ns, PROPERTIES);

            for (TestRun testRun : tests) {
                print(serializer, testRun);
            }

            serializer.endTag(ns, TESTSUITE);
        }

        void print(KXmlSerializer serializer, TestRun testRun) throws IOException {
            serializer.startTag(ns, TESTCASE);
            serializer.attribute(ns, ATTR_NAME, testRun.getTestName());
            serializer.attribute(ns, ATTR_CLASSNAME, testRun.getSuiteName());
            serializer.attribute(ns, ATTR_TIME, "0");

            String result = ERROR_RESULTS.contains(testRun.getResult()) ? ERROR
                    : FAILED_RESULTS.contains(testRun.getResult()) ? FAILURE
                    : null;

            if (result != null) {
                serializer.startTag(ns, result);
                String title = testRun.getDescription();
                if (title != null && title.length() > 0) {
                    serializer.attribute(ns, ATTR_MESSAGE, title);
                }
                serializer.attribute(ns, ATTR_TYPE, testRun.getResult().toString());
                String text = sanitize(Strings.join(testRun.getOutputLines(), "\n"));
                serializer.text(text);
                serializer.endTag(ns, result);
            }

            serializer.endTag(ns, TESTCASE);
        }

        /**
         * Returns the text in a format that is safe for use in an XML document.
         */
        private String sanitize(String text) {
            return text.replace("\0", "<\\0>");
        }
    }
}
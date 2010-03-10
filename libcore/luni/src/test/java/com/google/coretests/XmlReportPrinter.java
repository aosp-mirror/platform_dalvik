/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.google.coretests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import org.kxml2.io.KXmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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
 */
public class XmlReportPrinter {

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

    /** the test suites, which each contain tests */
    private final Map<String, Suite> suites = new LinkedHashMap<String, Suite>();

    /**
     * Create a report printer that prints the specified test suite. Since the
     * CoreTestSuite nulls-out tests after they're run (to limit memory
     * consumption), it is necessary to create the report printer prior to test
     * execution.
     */
    public XmlReportPrinter(CoreTestSuite allTests) {
        // partition the tests by suite to be consistent with Ant's printer
        for (Enumeration<Test> e = allTests.tests(); e.hasMoreElements(); ) {
            TestId test = new TestId(e.nextElement());

            // create the suite's entry in the map if necessary
            Suite suite = suites.get(test.className);
            if (suite == null) {
                suite = new Suite(test.className);
                suites.put(test.className, suite);
            }

            suite.tests.add(test);
        }
    }

    public void setResults(TestResult result) {
        populateFailures(true, result.errors());
        populateFailures(false, result.failures());
    }

    /**
     * Populate the list of failures in each of the suites.
     */
    private void populateFailures(boolean errors, Enumeration<TestFailure> failures) {
        while (failures.hasMoreElements()) {
            TestFailure failure = failures.nextElement();
            TestId test = new TestId(failure.failedTest());
            Suite suite = suites.get(test.className);

            if (suite == null) {
                throw new IllegalStateException( "received a failure for a "
                        + "test that wasn't in the original test suite!");
            }

            if (errors) {
                suite.errors.put(test, failure);
            } else {
                suite.failures.put(test, failure);
            }
        }
    }

    public int generateReports(String directory) {
        File parent = new File(directory);
        parent.mkdirs();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmt);
        dateFormat.setLenient(true);
        String timestamp = dateFormat.format(new Date());

        for (Suite suite : suites.values()) {
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(new File(parent, "TEST-" + suite.name + ".xml"));

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
                        ignored.printStackTrace();
                    }
                }
            }
        }

        return suites.size();
    }

    static class Suite {
        private final String name;
        private final List<TestId> tests = new ArrayList<TestId>();
        private final Map<TestId, TestFailure> failures = new HashMap<TestId, TestFailure>();
        private final Map<TestId, TestFailure> errors = new HashMap<TestId, TestFailure>();

        Suite(String name) {
            this.name = name;
        }

        void print(KXmlSerializer serializer, String timestamp) throws IOException {
            serializer.startTag(ns, TESTSUITE);
            serializer.attribute(ns, ATTR_NAME, name);
            serializer.attribute(ns, ATTR_TESTS, Integer.toString(tests.size()));
            serializer.attribute(ns, ATTR_FAILURES, Integer.toString(failures.size()));
            serializer.attribute(ns, ATTR_ERRORS, Integer.toString(errors.size()));
            serializer.attribute(ns, ATTR_TIME, "0");

            serializer.attribute(ns, TIMESTAMP, timestamp);
            serializer.attribute(ns, HOSTNAME, "localhost");
            serializer.startTag(ns, PROPERTIES);
            serializer.endTag(ns, PROPERTIES);

            for (TestId testId : tests) {
                TestFailure error = errors.get(testId);
                TestFailure failure = failures.get(testId);

                if (error != null) {
                    testId.printFailure(serializer, ERROR, error.thrownException());
                } else if (failure != null) {
                    testId.printFailure(serializer, FAILURE, failure.thrownException());
                } else {
                    testId.printSuccess(serializer);
                }
            }

            serializer.endTag(ns, TESTSUITE);
        }
    }

    private static class TestId {
        private final String name;
        private final String className;

        TestId(Test test) {
            this.name = test instanceof TestCase
                    ? ((TestCase) test).getName()
                    : test.toString();
            this.className = test.getClass().getName();
        }

        void printSuccess(KXmlSerializer serializer) throws IOException {
            serializer.startTag(ns, TESTCASE);
            printAttributes(serializer);
            serializer.endTag(ns, TESTCASE);
        }

        void printFailure(KXmlSerializer serializer, String type, Throwable t)
                throws IOException {
            serializer.startTag(ns, TESTCASE);
            printAttributes(serializer);

            serializer.startTag(ns, type);
            String message = t.getMessage();
            if (message != null && message.length() > 0) {
                serializer.attribute(ns, ATTR_MESSAGE, t.getMessage());
            }
            serializer.attribute(ns, ATTR_TYPE, t.getClass().getName());
            serializer.text(sanitize(BaseTestRunner.getFilteredTrace(t)));
            serializer.endTag(ns, type);

            serializer.endTag(ns, TESTCASE);
        }

        void printAttributes(KXmlSerializer serializer) throws IOException {
            serializer.attribute(ns, ATTR_NAME, name);
            serializer.attribute(ns, ATTR_CLASSNAME, className);
            serializer.attribute(ns, ATTR_TIME, "0");
        }

        @Override public boolean equals(Object o) {
            return o instanceof TestId
                    && ((TestId) o).name.equals(name)
                    && ((TestId) o).className.equals(className);
        }

        @Override public int hashCode() {
            return name.hashCode() ^ className.hashCode();
        }

        /**
         * Returns the text in a format that is safe for use in an XML document.
         */
        private static String sanitize(String text) {
            return text.replace("\0", "<\\0>");
        }
    }
}

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
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.regtest.RegressionTestSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Runs a directory's worth of jtreg tests on a device.
 */
final class JtregRunner {

    private static final Logger logger = Logger.getLogger(JtregRunner.class.getName());

    private final File localTemp;
    private final File directoryToScan;
    private final Set<File> expectationDirs;
    private final Vm vm;
    private final File xmlReportsDirectory;

    public JtregRunner(File localTemp, File directoryToScan, Vm vm,
            Set<File> expectationDirs, File xmlReportsDirectory) {
        this.localTemp = localTemp;
        this.directoryToScan = directoryToScan;
        this.expectationDirs = expectationDirs;
        this.vm = vm;
        this.xmlReportsDirectory = xmlReportsDirectory;
    }

    /**
     * Builds and executes all tests in the test directory.
     */
    public void buildAndRunAllTests() throws Exception {
        localTemp.mkdirs();

        List<TestDescription> tests = findTests(directoryToScan);
        final BlockingQueue<TestRun> readyToRun = new ArrayBlockingQueue<TestRun>(4);

        // build and install tests in a background thread. Using lots of
        // threads helps for packages that contain many unsupported tests
        ExecutorService builders = Executors.newFixedThreadPool(8);
        for (final TestDescription testDescription : tests) {
            builders.submit(new Runnable() {
                public void run() {
                    String qualifiedName = TestDescriptions.qualifiedName(testDescription);
                    TestRun testRun;
                    try {
                        ExpectedResult expectedResult = ExpectedResult.forRun(expectationDirs, qualifiedName);
                        testRun = new TestRun(qualifiedName, testDescription, expectedResult);
                        buildAndInstall(testRun);
                    } catch (Throwable throwable) {
                        testRun = new TestRun(qualifiedName, testDescription, ExpectedResult.SUCCESS);
                        testRun.setResult(Result.ERROR, throwable);
                    }
                    try {
                        readyToRun.put(testRun);
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Unexpected interruption", e);
                    }
                }
            });
        }
        builders.shutdown();

        vm.prepare();

        int unsupportedTests = 0;

        List<TestRun> runs = new ArrayList<TestRun>(tests.size());
        for (int i = 0; i < tests.size(); i++) {
            TestRun testRun = readyToRun.take();
            runs.add(testRun);

            if (testRun.getResult() == Result.UNSUPPORTED) {
                logger.fine("skipping " + testRun.getQualifiedName());
                unsupportedTests++;
                continue;
            }

            if (testRun.isRunnable()) {
                vm.runTest(testRun);
            }

            printResult(testRun);
        }

        if (unsupportedTests > 0) {
            logger.info("Skipped " + unsupportedTests + " unsupported tests.");
        }

        if (xmlReportsDirectory != null) {
            logger.info("Printing XML Reports... ");
            int numFiles = new XmlReportPrinter().generateReports(xmlReportsDirectory, runs);
            logger.info(numFiles + " XML files written.");
        }
    }

    /**
     * Returns the tests in {@code directoryToScan}.
     */
    List<TestDescription> findTests(File directoryToScan) throws Exception {
        logger.info("Scanning " + directoryToScan + " for tests.");
        File workDirectory = new File(localTemp, "JTwork");
        workDirectory.mkdirs();

        /*
         * This code is capable of extracting test descriptions using jtreg 4.0
         * and its bundled copy of jtharness. As a command line tool, jtreg's
         * API wasn't intended for this style of use. As a consequence, this
         * code is fragile and may be incompatible with newer versions of jtreg.
         */
        TestSuite testSuite = new RegressionTestSuite(directoryToScan);
        WorkDirectory wd = WorkDirectory.convert(workDirectory, testSuite);
        TestResultTable resultTable = wd.getTestResultTable();

        List<TestDescription> result = new ArrayList<TestDescription>();
        for (Iterator i = resultTable.getIterator(); i.hasNext(); ) {
            TestResult testResult = (TestResult) i.next();
            result.add(testResult.getDescription());
        }
        logger.info("Found " + result.size() + " tests.");
        return result;
    }

    private void buildAndInstall(TestRun testRun) {
        vm.buildAndInstall(testRun);
    }

    private void printResult(TestRun testRun) {
        ExpectedResult expected = testRun.getExpectedResult();
        boolean patternSuccess;

        if (expected.getPattern() != null) {
            Pattern pattern = Pattern.compile(expected.getPattern(),
                    Pattern.MULTILINE | Pattern.DOTALL);
            patternSuccess = pattern.matcher(Strings.join(testRun.getOutputLines(), "\n")).matches();
        } else {
            patternSuccess = true;
        }

        if (expected.getResult() == testRun.getResult() && patternSuccess) {
            logger.info("OK " + testRun.getQualifiedName() + " (" + testRun.getResult() + ")");
            return;
        }

        logger.info("FAIL " + testRun.getQualifiedName() + " (" + testRun.getResult() + ")");
        logger.info("  \"" + testRun.getTestDescription().getTitle() + "\"");

        if (expected.getResult() != Result.SUCCESS
                && expected.getResult() != testRun.getResult()) {
            logger.info("  Expected result: " + expected.getResult());
        }

        if (!patternSuccess) {
            logger.info("  Expected output to match \"" + expected.getPattern() + "\"");
        }

        for (String output : testRun.getOutputLines()) {
            logger.info("  " + output);
        }
    }
}

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Compiles, installs, runs and reports tests.
 */
final class Driver {

    private static final Logger logger = Logger.getLogger(Driver.class.getName());

    private final File localTemp;
    private final Set<File> expectationDirs;
    private final JtregFinder jtregFinder;
    private final JUnitFinder junitFinder;
    private final CaliperFinder caliperFinder;
    private final Vm vm;
    private final File xmlReportsDirectory;

    /**
     * The number of tests that weren't run because they aren't supported by
     * this runner.
     */
    private int unsupportedTests = 0;

    public Driver(File localTemp, Vm vm, Set<File> expectationDirs,
            File xmlReportsDirectory, JtregFinder jtregFinder,
            JUnitFinder junit, CaliperFinder caliperFinder) {
        this.localTemp = localTemp;
        this.expectationDirs = expectationDirs;
        this.vm = vm;
        this.xmlReportsDirectory = xmlReportsDirectory;
        this.jtregFinder = jtregFinder;
        this.junitFinder = junit;
        this.caliperFinder = caliperFinder;
    }

    /**
     * Builds and executes all tests in the test directory.
     */
    public void buildAndRunAllTests(Collection<File> testFiles) throws Exception {
        localTemp.mkdirs();

        final BlockingQueue<TestRun> readyToRun = new ArrayBlockingQueue<TestRun>(4);

        Set<TestRun> tests = new LinkedHashSet<TestRun>();
        for (File testFile : testFiles) {
            Set<TestRun> testsForFile = Collections.emptySet();

            if (testFile.isDirectory()) {
                testsForFile = jtregFinder.findTests(testFile);
                logger.fine("found " + testsForFile.size() + " jtreg tests for " + testFile);
            }
            if (testsForFile.isEmpty()) {
                testsForFile = junitFinder.findTests(testFile);
                logger.fine("found " + testsForFile.size() + " JUnit tests for " + testFile);
            }
            if (testsForFile.isEmpty()) {
                testsForFile = caliperFinder.findTests(testFile);
                logger.fine("found " + testsForFile.size() + " Caliper benchmarks for " + testFile);
            }
            tests.addAll(testsForFile);
        }

        logger.info("Running " + tests.size() + " tests.");

        // build and install tests in a background thread. Using lots of
        // threads helps for packages that contain many unsupported tests
        ExecutorService builders = Threads.threadPerCpuExecutor();
        int t = 0;
        for (final TestRun testRun : tests) {
            final int runIndex = t++;
            builders.submit(new Runnable() {
                public void run() {
                    try {
                        testRun.setExpectedResult(ExpectedResult.forRun(expectationDirs, testRun));
                        vm.buildAndInstall(testRun);
                    } catch (Throwable throwable) {
                        testRun.setResult(Result.ERROR, throwable);
                    }
                    try {
                        logger.fine("installed test " + runIndex + "; "
                                + readyToRun.size() + " are ready to run");
                        readyToRun.put(testRun);
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Unexpected interruption", e);
                    }
                }
            });
        }
        builders.shutdown();

        vm.prepare();

        List<TestRun> runs = new ArrayList<TestRun>(tests.size());
        for (int i = 0; i < tests.size(); i++) {
            logger.fine("executing test " + i + "; "
                    + readyToRun.size() + " are ready to run");

            // if it takes 5 minutes for build and install, something is broken
            TestRun testRun = readyToRun.poll(300, TimeUnit.SECONDS);
            if (testRun == null) {
                throw new IllegalStateException(
                        "Expected " + tests.size() + " tests but found only " + i);
            }

            runs.add(testRun);
            execute(testRun);
            vm.cleanup(testRun);
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
     * Executes a single test and then prints the result.
     */
    private void execute(TestRun testRun) {
        if (testRun.getResult() == Result.UNSUPPORTED) {
            logger.fine("skipping " + testRun.getQualifiedName());
            unsupportedTests++;
            return;
        }

        if (testRun.isRunnable()) {
            vm.runTest(testRun);
        }

        printResult(testRun);
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
        String description = testRun.getDescription();
        if (description != null) {
            logger.info("  \"" + description + "\"");
        }

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

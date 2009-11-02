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
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Runs a directory's worth of jtreg tests on a device.
 */
public final class JtregRunner {

    private static final Logger logger = Logger.getLogger(JtregRunner.class.getName());

    private final File localTemp = new File("/tmp/" + UUID.randomUUID());
    private final File deviceTemp = new File("/data/jtreg" + UUID.randomUUID());

    private final Adb adb = new Adb();
    private final File directoryToScan;
    private final TestToDex testToDex;

    private Integer debugPort;
    private Set<File> expectationDirs = new LinkedHashSet<File>();

    private File deviceTestRunner;

    public JtregRunner(File sdkJar, File directoryToScan) {
        this.directoryToScan = directoryToScan;
        this.testToDex = new TestToDex(sdkJar, localTemp);
    }

    /**
     * Builds and executes all tests in the test directory.
     */
    public void buildAndRunAllTests() throws Exception {
        localTemp.mkdirs();

        List<TestDescription> tests = testToDex.findTests(directoryToScan);
        final BlockingQueue<TestRun> readyToRun = new ArrayBlockingQueue<TestRun>(4);

        // build and install tests in a background thread. Using lots of
        // threads helps for packages that contain many unsupported tests
        ExecutorService builders = Executors.newFixedThreadPool(8);
        for (final TestDescription testDescription : tests) {
            builders.submit(new Runnable() {
                public void run() {
                    try {
                        String qualifiedName = TestDescriptions.qualifiedName(testDescription);
                        ExpectedResult expectedResult = ExpectedResult.forRun(expectationDirs, qualifiedName);
                        TestRun testRun = new TestRun(qualifiedName, testDescription, expectedResult);
                        buildAndInstall(testRun);
                        readyToRun.put(testRun);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        builders.shutdown();

        prepareDevice();

        int unsupportedTests = 0;

        while (!builders.isTerminated() || !readyToRun.isEmpty()) {
            TestRun testRun = readyToRun.take();

            if (testRun.getResult() == Result.UNSUPPORTED) {
                logger.fine("skipping " + testRun.getQualifiedName());
                unsupportedTests++;
                continue;
            }

            if (testRun.isRunnable()) {
                runTest(testRun);
            }

            printResult(testRun);
        }

        if (unsupportedTests > 0) {
            logger.info("Skipped " + unsupportedTests + " unsupported tests.");
        }
    }

    /**
     * Initializes the temporary directories and test harness necessary to run
     * tests on a device.
     */
    private void prepareDevice() {
        adb.mkdir(deviceTemp);
        File testRunnerJar = testToDex.writeTestRunnerJar();
        adb.push(testRunnerJar, deviceTemp);
        deviceTestRunner = new File(deviceTemp, testRunnerJar.getName());
        if (debugPort != null) {
            adb.forwardTcp(debugPort, debugPort);
        }
        logger.info("Prepared device.");
    }

    /**
     * Creates a dex file for the given test and push it out to the device.
     *
     * @return true if building and installing completed successfully.
     */
    private void buildAndInstall(TestRun testRun) {
        TestDescription testDescription = testRun.getTestDescription();
        String qualifiedName = testRun.getQualifiedName();
        logger.fine("building " + testRun.getQualifiedName());

        File base = new File(deviceTemp, qualifiedName);
        adb.mkdir(base);

        File dex;
        try {
            dex = testToDex.dexify(testDescription);
            if (dex == null) {
                testRun.initResult(Result.UNSUPPORTED, Collections.<String>emptyList());
                return;
            }
        } catch (CommandFailedException e) {
            testRun.initResult(Result.COMPILE_FAILED, e.getOutputLines());
            return;
        } catch (IOException e) {
            testRun.initResult(Result.ERROR, e);
            return;
        }

        logger.fine("installing " + testRun.getQualifiedName());
        adb.push(testDescription.getDir(), base);
        adb.push(dex, deviceTemp);
        testRun.initInstalledFiles(base, new File(deviceTemp, dex.getName()));
    }

    /**
     * Runs the specified test on the device.
     */
    private void runTest(TestRun testRun) {
        if (!testRun.isRunnable()) {
            throw new IllegalArgumentException();
        }

        logger.fine("running " + testRun.getQualifiedName());
        Dalvikvm vm = new Dalvikvm()
                .classpath(testRun.getDeviceDex(), deviceTestRunner)
                .args("-Duser.dir=" + testRun.getBase());
        if (debugPort != null) {
            vm.args("-Xrunjdwp:transport=dt_socket,address="
                    + debugPort + ",server=y,suspend=y");
        }
        List<String> output = vm.exec("dalvik.jtreg.TestRunner");

        if (output.isEmpty()) {
            testRun.initResult(Result.ERROR,
                    Collections.singletonList("No output returned!"));
        }

        Result result = "SUCCESS".equals(output.get(output.size() - 1))
                ? Result.SUCCESS
                : Result.EXEC_FAILED;
        testRun.initResult(result, output.subList(0, output.size() - 1));
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

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: JTRegRunner [options]... <android_jar> <directoryWithTests>");
            System.out.println();
            System.out.println("  android_jar: the API jar file to compile against. Usually");
            System.out.println("      this is <SDK>/platforms/android-<X.X>/android.jar where");
            System.out.println("      <SDK> is the path to an Android SDK path and <X.X> is a");
            System.out.println("      release version like 1.5.");
            System.out.println();
            System.out.println("  directoryWithTests: a directory to scan for test cases;");
            System.out.println("      typically this is 'platform_v6/jdk/test' if 'platform_v6'");
            System.out.println("      contains the sources of a platform implementation.");
            System.out.println();
            System.out.println("OPTIONS");
            System.out.println();
            System.out.println("  --debug <port>: enable Java debugging on the specified port.");
            System.out.println("      This port must be free both on the device and on the local");
            System.out.println("      system.");
            System.out.println();
            System.out.println("  --expectations <directory>: use the specified directory when");
            System.out.println("      looking for test expectations. The directory should include");
            System.out.println("      <test>.expected files describing expected results.");
            System.out.println();
            System.out.println("  --verbose: turn on verbose output");
            System.out.println();
            return;
        }

        prepareLogging();

        File sdkJar = new File(args[args.length - 2]);
        if (!sdkJar.exists()) {
            throw new RuntimeException("Could not find SDK jar: " + sdkJar);
        }

        File directoryToScan = new File(args[args.length - 1]);
        if (!directoryToScan.isDirectory()) {
            throw new RuntimeException("Invalid test directory: " + directoryToScan);
        }

        JtregRunner jtregRunner = new JtregRunner(sdkJar, directoryToScan);

        for (int i = 0; i < args.length - 2; i++) {
            if ("--debug".equals(args[i])) {
                jtregRunner.debugPort = Integer.valueOf(args[++i]);

            } else if ("--verbose".equals(args[i])) {
                Logger.getLogger("dalvik.jtreg").setLevel(Level.FINE);

            } else if ("--expectations".equals(args[i])) {
                File expectationDir = new File(args[++i]);
                if (!expectationDir.isDirectory()) {
                    throw new RuntimeException("Invalid expectation directory: " + directoryToScan);
                }
                jtregRunner.expectationDirs.add(expectationDir);

            } else {
                throw new RuntimeException("Unrecognized option: " + args[i]);
            }
        }

        jtregRunner.buildAndRunAllTests();
    }

    private static void prepareLogging() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override public String format(LogRecord r) {
                return r.getMessage() + "\n";
            }
        });
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }
}

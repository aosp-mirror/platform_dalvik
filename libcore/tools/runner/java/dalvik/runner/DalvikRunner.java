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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Command line interface for running benchmarks and tests on dalvik.
 */
public final class DalvikRunner {

    private final File localTemp;
    private File sdkJar;
    private Integer debugPort;
    private long timeoutSeconds;
    private Set<File> expectationFiles = new LinkedHashSet<File>();
    private File xmlReportsDirectory;
    private String javaHome;
    private boolean clean = true;
    private String deviceRunnerDir = "/sdcard/dalvikrunner";
    private List<File> testFiles = new ArrayList<File>();

    private DalvikRunner() {
        localTemp = new File("/tmp/" + UUID.randomUUID());
        timeoutSeconds = 10 * 60; // default is ten minutes
        sdkJar = new File("/home/dalvik-prebuild/android-sdk-linux/platforms/android-2.0/android.jar");
        expectationFiles.add(new File("dalvik/libcore/tools/runner/expectations.txt"));
    }

    private void prepareLogging() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override public String format(LogRecord r) {
                return r.getMessage() + "\n";
            }
        });
        Logger logger = Logger.getLogger("dalvik.runner");
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    private boolean parseArgs(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if ("--debug".equals(args[i])) {
                debugPort = Integer.valueOf(args[++i]);

            } else if ("--device-runner-dir".equals(args[i])) {
                deviceRunnerDir = args[++i];

            } else if ("--expectations".equals(args[i])) {
                expectationFiles.add(new File(args[++i]));

            } else if ("--java-home".equals(args[i])) {
                javaHome = args[++i];
                if (!new File(javaHome, "/bin/java").exists()) {
                    System.out.println("Invalid java home: " + javaHome);
                    return false;
                }

            } else if ("--timeout-seconds".equals(args[i])) {
                timeoutSeconds = Long.valueOf(args[++i]);

            } else if ("--sdk".equals(args[i])) {
                sdkJar = new File(args[++i]);
                if (!sdkJar.exists()) {
                    System.out.println("Could not find SDK jar: " + sdkJar);
                    return false;
                }

            } else if ("--skip-clean".equals(args[i])) {
                clean = false;

            } else if ("--verbose".equals(args[i])) {
                Logger.getLogger("dalvik.runner").setLevel(Level.FINE);

            } else if ("--xml-reports-directory".equals(args[i])) {
                xmlReportsDirectory = new File(args[++i]);
                if (!xmlReportsDirectory.isDirectory()) {
                    System.out.println("Invalid XML reports directory: " + xmlReportsDirectory);
                    return false;
                }

            } else if (args[i].startsWith("-")) {
                System.out.println("Unrecognized option: " + args[i]);
                return false;

            } else {
                testFiles.add(new File(args[i]));
            }
        }

        if (testFiles.isEmpty()) {
            System.out.println("No tests provided.");
            return false;
        }

        return true;
    }

    private void printUsage() {
        System.out.println("Usage: DalvikRunner [options]... <tests>...");
        System.out.println();
        System.out.println("  <tests>: a .java file containing a jtreg test, JUnit test,");
        System.out.println("      Caliper benchmark, or a directory of such tests.");
        System.out.println();
        System.out.println("OPTIONS");
        System.out.println();
        System.out.println("  --debug <port>: enable Java debugging on the specified port.");
        System.out.println("      This port must be free both on the device and on the local");
        System.out.println("      system.");
        System.out.println();
        System.out.println("  ----device-runner-dir <directory>: use the specified directory for");
        System.out.println("      on-device temporary files and code.");
        System.out.println("      Default is: " + deviceRunnerDir);
        System.out.println();
        System.out.println("  --expectations <file>: include the specified file when looking for");
        System.out.println("      test expectations. The file should include qualified test names");
        System.out.println("      and the corresponding expected output.");
        System.out.println("      Default is: " + expectationFiles);
        System.out.println();
        System.out.println("  --java-home <java_home>: execute the tests on the local workstation");
        System.out.println("      using the specified java home directory. This does not impact");
        System.out.println("      which javac gets used. When unset, tests are run on a device");
        System.out.println("      using adb.");
        System.out.println();
        System.out.println("  --sdk <android jar>: the API jar file to compile against.");
        System.out.println("      Usually this is <SDK>/platforms/android-<X.X>/android.jar");
        System.out.println("      where <SDK> is the path to an Android SDK path and <X.X> is");
        System.out.println("      a release version like 1.5.");
        System.out.println("      Default is: " + sdkJar);
        System.out.println();
        System.out.println("  --skip-clean: leave temporary files in their place. Useful when");
        System.out.println("      coupled with --verbose if you'd like to manually re-run");
        System.out.println("      commands afterwards.");
        System.out.println();
        System.out.println("  --timeout-seconds <seconds>: maximum execution time of each");
        System.out.println("      test before the runner aborts it.");
        System.out.println("      Default is: " + timeoutSeconds);
        System.out.println();
        System.out.println("  --xml-reports-directory <path>: directory to emit JUnit-style");
        System.out.println("      XML test results.");
        System.out.println();
        System.out.println("  --verbose: turn on verbose output");
        System.out.println();
    }

    private void run() throws Exception {
        Vm vm = javaHome != null
                ? new JavaVm(debugPort, timeoutSeconds, sdkJar, localTemp, javaHome, clean)
                : new DeviceDalvikVm(debugPort, timeoutSeconds, sdkJar, localTemp,
                        clean, deviceRunnerDir);
        JtregFinder jtregFinder = new JtregFinder(localTemp);
        JUnitFinder jUnitFinder = new JUnitFinder();
        CaliperFinder caliperFinder = new CaliperFinder();
        Driver driver = new Driver(localTemp,
                vm, expectationFiles, xmlReportsDirectory, jtregFinder,
                jUnitFinder, caliperFinder);
        driver.loadExpectations();
        driver.buildAndRunAllTests(testFiles);
        vm.shutdown();
    }

    public static void main(String[] args) throws Exception {
        DalvikRunner dalvikRunner = new DalvikRunner();
        if (!dalvikRunner.parseArgs(args)) {
            dalvikRunner.printUsage();
            return;
        }
        dalvikRunner.prepareLogging();
        dalvikRunner.run();
    }
}

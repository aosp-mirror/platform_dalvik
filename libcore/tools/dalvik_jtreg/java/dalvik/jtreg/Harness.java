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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Command line interface for running jtreg tests.
 */
public final class Harness {

    private final File localTemp;
    private File sdkJar;
    private Integer debugPort;
    private long timeoutSeconds;
    private Set<File> expectationDirs = new LinkedHashSet<File>();
    private File xmlReportsDirectory;
    private String javaHome;
    private File directoryToScan;

    private Harness() {
        localTemp = new File("/tmp/" + UUID.randomUUID());
        timeoutSeconds = 10 * 60; // default is ten minutes
        sdkJar = new File("/home/dalvik-prebuild/android-sdk-linux/platforms/android-2.0/android.jar");
        expectationDirs.add(new File("dalvik/libcore/tools/dalvik_jtreg/expectations"));
    }

    private void prepareLogging() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override public String format(LogRecord r) {
                return r.getMessage() + "\n";
            }
        });
        Logger logger = Logger.getLogger("dalvik.jtreg");
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    private boolean parseArgs(String[] args) throws Exception {
        if (args.length == 0) {
            return false;
        }

        int i = 0;
        for (; i < args.length - 1; i++) {
            if ("--debug".equals(args[i])) {
                debugPort = Integer.valueOf(args[++i]);

            } else if ("--expectations".equals(args[i])) {
                File expectationDir = new File(args[++i]);
                if (!expectationDir.isDirectory()) {
                    System.out.println("Invalid expectation directory: " + expectationDir);
                    return false;
                }
                expectationDirs.add(expectationDir);

            } else if ("--javaHome".equals(args[i])) {
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

            } else if ("--verbose".equals(args[i])) {
                Logger.getLogger("dalvik.jtreg").setLevel(Level.FINE);

            } else if ("--xml-reports-directory".equals(args[i])) {
                xmlReportsDirectory = new File(args[++i]);
                if (!xmlReportsDirectory.isDirectory()) {
                    System.out.println("Invalid XML reports directory: " + xmlReportsDirectory);
                    return false;
                }

            } else {
                System.out.println("Unrecognized option: " + args[i]);
                return false;
            }
        }

        if (i > args.length - 1) {
            System.out.println("Missing required test directory option");
            return false;
        }

        directoryToScan = new File(args[i]);
        if (!directoryToScan.isDirectory()) {
            System.out.println("Invalid test directory: " + directoryToScan);
            return false;
        }

        return true;
    }

    private void printUsage() {
        System.out.println("Usage: JTRegRunner [options]... <tests directory>");
        System.out.println();
        System.out.println("  <tests directory>: a directory to scan for test cases;");
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
        System.out.println("      Default is: " + expectationDirs);
        System.out.println();
        System.out.println("  --javaHome <java_home>: execute the tests on the local workstation");
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
                ? new JavaVm(debugPort, timeoutSeconds, sdkJar, localTemp, javaHome)
                : new DeviceDalvikVm(debugPort, timeoutSeconds, sdkJar, localTemp);
        JtregRunner jtregRunner = new JtregRunner(localTemp, directoryToScan,
                vm, expectationDirs, xmlReportsDirectory);
        jtregRunner.buildAndRunAllTests();
        vm.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Harness harness = new Harness();
        if (!harness.parseArgs(args)) {
            harness.printUsage();
            return;
        }
        harness.prepareLogging();
        harness.parseArgs(args);
        harness.run();
    }
}

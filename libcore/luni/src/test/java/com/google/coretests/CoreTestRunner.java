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
package com.google.coretests;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;

/**
 * A special TestRunner implementation that is able to filter out annotated
 * tests and handles our known failures properly (expects them to fail).
 * Handy when running the Core Libraries tests on Android, the bare-metal
 * Dalvik VM, or the RI.
 */
public class CoreTestRunner extends TestRunner {

    /**
     * Reflects our environment.
     */
    private static boolean IS_DALVIK = "Dalvik".equals(
            System.getProperty("java.vm.name"));
    
    /**
     * Defines the default flags for running on Dalvik.
     */
    private static final int DEFAULT_FLAGS_DALVIK =
            CoreTestSuite.RUN_ANDROID_ONLY | 
            CoreTestSuite.RUN_NORMAL_TESTS |
            CoreTestSuite.RUN_KNOWN_FAILURES |
            CoreTestSuite.RUN_SIDE_EFFECTS |
            CoreTestSuite.INVERT_KNOWN_FAILURES;
    
    /**
     * Defines the default flags for running on an RI.
     */
    private static final int DEFAULT_FLAGS_NON_DALVIK =
            CoreTestSuite.RUN_NORMAL_TESTS |
            CoreTestSuite.RUN_KNOWN_FAILURES |
            CoreTestSuite.RUN_SIDE_EFFECTS;
       
    /**
     * Holds the flags specified by the user on the command line.
     */
    private int fFlags;
    
    /**
     * Holds the timeout value specified by the user on the command line.
     */
    private int fTimeout; 

    private int fStep = 1;
    
    /**
     * Creates a new instance of our CoreTestRunner.
     */
    public CoreTestRunner() {
        super();
    }

    @Override
    protected TestResult createTestResult() {
        return new CoreTestResult(fFlags, fTimeout);
    }

    protected ResultPrinter createPrinter() {
        return new CoreTestPrinter(System.out, fFlags);
    }
    
    /**
     * Provides our main entry point.
     */
    public static void main(String args[]) {
        Logger.global.setLevel(Level.OFF);
        
        System.out.println(
                "--------------------------------------------------");
        System.out.println("Android Core Libraries Test Suite");
        System.out.println("Version 1.0");
        System.out.println(
                "Copyright (c) 2009 The Android Open Source Project");
        System.out.println("");
        
        CoreTestRunner testRunner = new CoreTestRunner();
        try {
            TestResult r = testRunner.start(args);
            
            System.out.println(
            "--------------------------------------------------");
            
            if (!r.wasSuccessful()) {
                System.exit(FAILURE_EXIT);
            } else {
                System.exit(SUCCESS_EXIT);
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(EXCEPTION_EXIT);
        }
        
    }

    @Override
    public TestResult doRun(Test suite, boolean wait) {
        setPrinter(createPrinter());
        
        /*
         * Make sure the original suite is unreachable after we have
         * created the new one, so GC can dispose terminated tests.
         */
        suite = new CoreTestSuite(suite, fFlags, fStep, null);
        
        return super.doRun(suite, wait);
    }
    
    /**
     * Prints a help screen on the console.
     */
    private void showHelp() {
        System.out.println("Usage: run-core-tests [OPTION]... [TEST]...");
        System.out.println();
        System.out.println("Where each TEST is a class name, optionally followed");
        System.out.println("by \"#\" and a method name, and each OPTION is one of");
        System.out.println("the following:");
        System.out.println();
        System.out.println("    --include-all");
        System.out.println("    --exclude-all");
        System.out.println("    --include-android-only");
        System.out.println("    --exclude-android-only");
        System.out.println("    --include-broken-tests");
        System.out.println("    --exclude-broken-tests");
        System.out.println("    --include-known-failures");
        System.out.println("    --exclude-known-failures");
        System.out.println("    --include-normal-tests");
        System.out.println("    --exclude-normal-tests");
        System.out.println("    --include-side-effects");
        System.out.println("    --exclude-side-effects");
        System.out.println();
        System.out.println("    --known-failures-must-fail");
        System.out.println("    --known-failures-must-pass");
        System.out.println("    --timeout <seconds>");
        // System.out.println("    --find-side-effect <test>");
        System.out.println("    --isolate-all");
        System.out.println("    --isolate-none");
        System.out.println("    --verbose");
        System.out.println("    --help");
        System.out.println();
        System.out.println("Default parameters are:");
        System.out.println();
        
        if (IS_DALVIK) {
            System.out.println("    --include-android-only");
            System.out.println("    --exclude-broken-tests");
            System.out.println("    --include-known-failures");
            System.out.println("    --include-normal-tests");
            System.out.println("    --include-side-effects");
            System.out.println("    --known-failures-must-fail");
        } else {
            System.out.println("    --exclude-android-only");
            System.out.println("    --exclude-broken-tests");
            System.out.println("    --include-known-failures");
            System.out.println("    --include-normal-tests");
            System.out.println("    --include-side-effects");
            System.out.println("    --known-failures-must-pass");
        }
        
        System.out.println();
    }

    /**
     * Tries to create a Test instance from the given strings. The strings might
     * either specify a class only or a class plus a method name, separated by
     * a "#".
     */
    private Test createTest(List<String> testCases) throws Exception {
        TestSuite result = new TestSuite();
        for (String testCase : testCases) {
            int p = testCase.indexOf("#");
            if (p != -1) {
                String testName = testCase.substring(p + 1);
                testCase = testCase.substring(0, p);
                
                result.addTest(TestSuite.createTest(Class.forName(testCase), testName));
            } else {
                result.addTest(getTest(testCase));
            }
        }
        return result;
    }
    
    @Override
    protected TestResult start(String args[]) throws Exception {
        List<String> testNames = new ArrayList<String>();
        // String victimName = null;
        
        boolean wait = false;
        
        if (IS_DALVIK) {
            fFlags = DEFAULT_FLAGS_DALVIK;
        } else {
            fFlags = DEFAULT_FLAGS_NON_DALVIK;
        }
        
        for (int i= 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                if (args[i].equals("--wait")) {
                    wait = true;
                } else if (args[i].equals("--include-all")) {
                    fFlags = fFlags | CoreTestSuite.RUN_ALL_TESTS;
                } else if (args[i].equals("--exclude-all")) {
                    fFlags = fFlags & ~CoreTestSuite.RUN_ALL_TESTS;
                } else if (args[i].equals("--include-android-only")) {
                    fFlags = fFlags | CoreTestSuite.RUN_ANDROID_ONLY;
                } else if (args[i].equals("--exclude-android-only")) {
                    fFlags = fFlags & ~CoreTestSuite.RUN_ANDROID_ONLY;
                } else if (args[i].equals("--include-broken-tests")) {
                    fFlags = fFlags | CoreTestSuite.RUN_BROKEN_TESTS;
                } else if (args[i].equals("--exclude-broken-tests")) {
                    fFlags = fFlags & ~CoreTestSuite.RUN_BROKEN_TESTS;
                } else if (args[i].equals("--include-known-failures")) {
                    fFlags = fFlags | CoreTestSuite.RUN_KNOWN_FAILURES;
                } else if (args[i].equals("--exclude-known-failures")) {
                    fFlags = fFlags & ~CoreTestSuite.RUN_KNOWN_FAILURES;
                } else if (args[i].equals("--include-normal-tests")) {
                    fFlags = fFlags | CoreTestSuite.RUN_NORMAL_TESTS;
                } else if (args[i].equals("--exclude-normal-tests")) {
                    fFlags = fFlags & ~CoreTestSuite.RUN_NORMAL_TESTS;
                } else if (args[i].equals("--include-side-effects")) {
                    fFlags = fFlags | CoreTestSuite.RUN_SIDE_EFFECTS;
                } else if (args[i].equals("--exclude-side-effects")) {
                    fFlags = fFlags & ~CoreTestSuite.RUN_SIDE_EFFECTS;
                } else if (args[i].equals("--known-failures-must-fail")) {
                    fFlags = fFlags | CoreTestSuite.INVERT_KNOWN_FAILURES;
                } else if (args[i].equals("--known-failures-must-pass")) {
                    fFlags = fFlags & ~CoreTestSuite.INVERT_KNOWN_FAILURES;
                } else if (args[i].equals("--timeout")) {
                    fTimeout = Integer.parseInt(args[++i]);
                } else if (args[i].equals("--reverse")) {
                    fFlags = fFlags | CoreTestSuite.REVERSE;
                } else if (args[i].equals("--step")) {
                    fStep = Integer.parseInt(args[++i]);
                } else if (args[i].equals("--isolate-all")) {
                    fFlags = (fFlags | CoreTestSuite.ISOLATE_ALL) & 
                                   ~CoreTestSuite.ISOLATE_NONE;
                } else if (args[i].equals("--isolate-none")) {
                    fFlags = (fFlags | CoreTestSuite.ISOLATE_NONE) & 
                                   ~CoreTestSuite.ISOLATE_ALL;
                } else if (args[i].equals("--verbose")) {
                    fFlags = fFlags | CoreTestSuite.VERBOSE;
                // } else if (args[i].equals("--find-side-effect")) {
                //    victimName = args[++i];
                } else if (args[i].equals("--dry-run")) {
                    fFlags = fFlags | CoreTestSuite.DRY_RUN;
                } else if (args[i].equals("--help")) {
                    showHelp();
                    System.exit(1);
                } else {
                    unknownArgument(args[i]);
                }
            } else if (args[i].startsWith("-")) {
                unknownArgument(args[i]);
            } else {
                testNames.add(args[i]);
            }
        }
        
        if (IS_DALVIK) {
            System.out.println("Using Dalvik VM version " + 
                    System.getProperty("java.vm.version"));
        } else {
            System.out.println("Using Java VM version " + 
                    System.getProperty("java.version"));
        }
        System.out.println();

        try {
            return doRun(createTest(testNames), wait);
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new Exception("Could not create and run test suite: " + e);
        }
    }
    
    private static void unknownArgument(String arg) {
        System.err.println("Unknown argument " + arg + ", try --help");
        System.exit(1);
    }
}

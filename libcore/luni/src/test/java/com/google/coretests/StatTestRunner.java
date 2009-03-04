/*
 * Copyright (C) 2008 The Android Open Source Project
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

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import junit.runner.StandardTestSuiteLoader;
import junit.runner.TestSuiteLoader;

import java.io.PrintStream;

/**
 * A command line based tool to run tests.
 * <pre>
 * java junit.textui.TestRunner [-wait] TestCaseClass
 * </pre>
 * TestRunner expects the name of a TestCase class as argument.
 * If this class defines a static <code>suite</code> method it 
 * will be invoked and the returned test is run. Otherwise all 
 * the methods starting with "test" having no arguments are run.
 * <p>
 * When the wait command line argument is given TestRunner
 * waits until the users types RETURN.
 * <p>
 * TestRunner prints a trace as the tests are executed followed by a
 * summary at the end. 
 */
public class StatTestRunner extends BaseTestRunner {
    private ResultPrinter fPrinter;
    private PerfStatCollector fPerfStatCollector;
    
    public static final int SUCCESS_EXIT= 0;
    public static final int FAILURE_EXIT= 1;
    public static final int EXCEPTION_EXIT= 2;

    public static final String DEFAULT_DATABASE = "sqlite:/coretests.db";
    public static final String DEFAULT_DRIVER = "SQLite.JDBCDriver";
    
    public static String connectionURL;
    public static String jdbcDriver;

    /**
     * Constructs a TestRunner.
     */
    public StatTestRunner() {
        this(System.out);
    }

    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    public StatTestRunner(PrintStream writer) {
        this(new ResultPrinter(writer));
    }
    
    /**
     * Constructs a TestRunner using the given ResultPrinter all the output
     */
    public StatTestRunner(ResultPrinter printer) {
        fPrinter= printer;
        fPerfStatCollector = new PerfStatCollector(printer.getWriter());
    }
    
    /**
     * Runs a suite extracted from a TestCase subclass.
     */
    static public void run(Class testClass) {
        run(new TestSuite(testClass));
    }

    /**
     * Runs a single test and collects its results.
     * This method can be used to start a test run
     * from your program.
     * <pre>
     * public static void main (String[] args) {
     *     test.textui.TestRunner.run(suite());
     * }
     * </pre>
     */
    static public TestResult run(Test test) {
        StatTestRunner runner= new StatTestRunner();
        try {
            return runner.doRun(test, false);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Runs a single test and waits until the user
     * types RETURN.
     */
    static public void runAndWait(Test suite) {
        StatTestRunner aTestRunner= new StatTestRunner();
        try {
            aTestRunner.doRun(suite, true);
        }
        catch (Exception e) {}
    }

    /**
     * Always use the StandardTestSuiteLoader. Overridden from
     * BaseTestRunner.
     */
    public TestSuiteLoader getLoader() {
        return new StandardTestSuiteLoader();
    }

    public void testFailed(int status, Test test, Throwable t) {
    }
    
    public void testStarted(String testName) {
    }
    
    public void testEnded(String testName) {
    }

    public TestResult doRun(Test suite, boolean wait) throws Exception {
        StatsStore.open(jdbcDriver, connectionURL);
        TestResult result = new TestResult();
        result.addListener(fPrinter);
        result.addListener(fPerfStatCollector);
        long startTime= System.currentTimeMillis();
        StatsStore.now = startTime;
        suite.run(result);
        long endTime= System.currentTimeMillis();
        long runTime= endTime-startTime;
        fPrinter.print(result, runTime);
        fPerfStatCollector.digest();
        StatsStore.close();

        pause(wait);
        return result;
    }

    protected void pause(boolean wait) {
        if (!wait) return;
        fPrinter.printWaitPrompt();
        try {
            System.in.read();
        }
        catch(Exception e) {
        }
    }
    
    public static void main(String args[]) {
        StatTestRunner aTestRunner= new StatTestRunner();
        try {
            TestResult r= aTestRunner.start(args);
            if (!r.wasSuccessful())
                System.exit(FAILURE_EXIT);
            System.exit(SUCCESS_EXIT);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(EXCEPTION_EXIT);
        }
    }

    /**
     * Starts a test run. Analyzes the command line arguments
     * and runs the given test suite.
     */
    protected TestResult start(String args[]) throws Exception {
        String testCase= "";
        boolean wait= false;

        jdbcDriver = System.getProperty("android.coretests.driver", DEFAULT_DRIVER); 
        connectionURL = System.getProperty("android.coretests.database", "jdbc:" + DEFAULT_DATABASE); 
            
        for (int i= 0; i < args.length; i++) {
            if (args[i].equals("--all"))
                fPerfStatCollector.listAll = true;
            else if (args[i].equals("--bad"))
                fPerfStatCollector.listBad = true;
            else if (args[i].equals("--nobig"))
                fPerfStatCollector.bigMarking = false;
            else if (args[i].equals("--s")) {
                fPerfStatCollector.thresholdDuration =
                    Integer.valueOf(args[++i]);
            } else if (args[i].equals("-wait"))
                wait= true;
            else if (args[i].equals("-c")) 
                testCase= extractClassName(args[++i]);
            else if (args[i].equals("-v"))
                System.err.println("JUnit "+Version.id()+" (plus Android performance stats)");
            else
                testCase= args[i];
        }
        
        if (testCase.equals("")) 
            throw new Exception("Usage: TestRunner [-wait] testCaseName, where name is the name of the TestCase class");

        try {
            Test suite= getTest(testCase);
            return doRun(suite, wait);
        }
        catch (Exception e) {
            throw new Exception("Exception: " + e);
        }
    }
        
    protected void runFailed(String message) {
        System.err.println(message);
        System.exit(FAILURE_EXIT);
    }
    
    public void setPrinter(ResultPrinter printer) {
        fPrinter= printer;
    }
        
    
}

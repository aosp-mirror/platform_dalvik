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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

/**
 * A wrapper around a single test that allows to execute the test either in the
 * same thread, in a separate thread, or even in a different process. 
 */
public class CoreTestRunnable implements Runnable {

    private static boolean IS_DALVIK = "Dalvik".equals(
            System.getProperty("java.vm.name"));
    
    /**
     * The test case we are supposed to run.
     */
    private TestCase fTest;
    
    /**
     * The TestResult we need to update after the run.
     */
    private TestResult fResult;
    
    /**
     * The Protectable that JUnit has created for us.
     */
    private Protectable fProtectable;

    /**
     * Reflects whether we need to invert the test result, which is used for
     * treating known failures.
     */
    private boolean fInvert;
    
    /**
     * Reflects whether we need to isolate the test, which means we run it in
     * a separate process. 
     */
    private boolean fIsolate;
    
    /**
     * If we are isolating the test case, this holds the process that is running
     * it.
     */
    private Process fProcess;

    /**
     * Creates a new CoreTestRunnable for the given parameters.
     */
    public CoreTestRunnable(TestCase test, TestResult result,
            Protectable protectable, boolean invert, boolean isolate) {
        
        this.fTest = test;
        this.fProtectable = protectable;
        this.fResult = result;
        this.fInvert = invert;
        this.fIsolate = isolate;
    }

    /**
     * Executes the test and stores the results. May be run from a secondary
     * Thread.
     */
    public void run() {
        try {
            if (fIsolate) {
                runExternally();
            } else {
                runInternally();
            }
            
            if (fInvert) {
                fInvert = false;
                throw new AssertionFailedError(
                        "@KnownFailure expected to fail, but succeeded");
            }
        } catch (AssertionFailedError e) {
            if (!fInvert) {
                fResult.addFailure(fTest, e);
            }
        } catch (ThreadDeath e) { // don't catch ThreadDeath by accident
            throw e;
        } catch (Throwable e) {
            if (!fInvert) {
                fResult.addError(fTest, e);
            }
        }
    }

    /**
     * Tells the test case to stop. Only used with isolation. We need to kill
     * the external process in this case.
     */
    public void stop() {
        if (fProcess != null) {
            fProcess.destroy();
        }
    }

    /**
     * Runs the test case in the same process. This is basically what a
     * run-of-the-mill JUnit does, except we might also do it in a secondary
     * thread.
     */
    private void runInternally() throws Throwable {
        fProtectable.protect();        
    }
    
    /**
     * Runs the test case in a different process. This is what we do for
     * isolating test cases that have side effects or do suffer from them.
     */
    private void runExternally() throws Throwable {
        Throwable throwable = null;
        
        File file = File.createTempFile("isolation", ".tmp");
        
        fProcess = Runtime.getRuntime().exec(
                (IS_DALVIK ? "dalvikvm" : "java") +
                " -classpath " + System.getProperty("java.class.path") +
                " -Djava.home=" + System.getProperty("java.home") +
                " -Duser.home=" + System.getProperty("user.home") +
                " -Djava.io.tmpdir=" + System.getProperty("user.home") +
                " com.google.coretests.CoreTestIsolator" +
                " " + fTest.getClass().getName() +
                " " + fTest.getName() +
                " " + file.getAbsolutePath());
        
        int result = fProcess.waitFor();
        
        if (result != TestRunner.SUCCESS_EXIT) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                throwable = (Throwable)ois.readObject();
                ois.close();
            } catch (Exception ex) {
                throwable = new RuntimeException("Error isolating test", ex);
            }
        }
        
        file.delete();
        
        if (throwable != null) {
            throw throwable;
        }
    }
    
}

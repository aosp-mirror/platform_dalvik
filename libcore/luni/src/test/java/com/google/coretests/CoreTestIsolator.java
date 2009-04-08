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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A minimalistic TestRunner implementation that silently executes a single test
 * method and writes a possible stack trace to a temporary file. Used for
 * isolating tests. 
 */
public class CoreTestIsolator extends TestRunner {

    /**
     * Creates a new CoreTestIsolator. The superclass needs to be able to build
     * a proper ResultPrinter, so we pass it a null device for printing stuff.
     */
    public CoreTestIsolator() {
        super(new PrintStream(new OutputStream() {
            @Override
            public void write(int oneByte) throws IOException {
                // Null device
            }
        }));
    }

    @Override
    protected TestResult createTestResult() {
        return new TestResult();
    }

    /**
     * Provides the main entry point. First and second argument are class and
     * method name, respectively. Third argument is the temporary file name for
     * the result. Exits with one of the usual JUnit exit values. 
     */
    public static void main(String args[]) {
        Logger.global.setLevel(Level.OFF);
        
        CoreTestIsolator testRunner = new CoreTestIsolator();
        try {
            TestResult r = testRunner.start(args);
            
            if (!r.wasSuccessful()) {
                // Store failure or error - we know there must be one
                Throwable failure = r.failureCount() != 0 ? 
                        ((TestFailure)r.failures().nextElement()).
                                thrownException() :
                        ((TestFailure)r.errors().nextElement()).
                                thrownException();

                saveStackTrace(failure, args[2]);
                
                System.exit(FAILURE_EXIT);
            } else {
                // Nothing to see here, please get along
                System.exit(SUCCESS_EXIT);
            }
        } catch(Exception e) {
            // Let main TestRunner know about execution problem
            saveStackTrace(e, args[2]);
            System.exit(EXCEPTION_EXIT);
        }
        
    }

    /**
     * Saves a given stack trace to a given file.
     */
    private static void saveStackTrace(Throwable throwable, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
    
            oos.writeObject(throwable);
            
            oos.flush();
            oos.close();
        } catch (IOException ex) {
            // Ignore
        }
    }

    @Override
    protected TestResult start(String args[]) {
        try {
            Test suite = TestSuite.createTest(Class.forName(args[0]), args[1]);
            return doRun(suite);
        }
        catch(Exception e) {
            throw new RuntimeException("Unable to launch test", e);
        }
    }
    
}

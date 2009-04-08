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

import java.lang.reflect.Method;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import dalvik.annotation.KnownFailure;
import dalvik.annotation.SideEffect;

/**
 * A special TestResult implementation that is able to filter out annotated
 * tests and handles our known failures properly (expects them to fail).
 * Handy when running the Core Libraries tests on Android, the bare-metal
 * Dalvik VM, or the RI.  
 */
public class CoreTestResult extends TestResult {

    /**
     * The flags the user specified for this test run. 
     */
    protected int fFlags;

    /**
     * The timeout the user specified for this test run. 
     */
    protected int fTimeout;
    
    /**
     * The total number of tests in the original suite.
     */
    protected int fTotalTestCount;
    
    /**
     * The number of Android-only tests in the original suite.
     */
    protected int fAndroidOnlyCount;
    
    /**
     * The number of broken tests in the original suite.
     */
    protected int fBrokenTestCount;
    
    /**
     * The number of known failures in the original suite.
     */
    protected int fKnownFailureCount;

    /**
     * The number of side-effective tests in the original suite.
     */
    protected int fSideEffectCount;
    
    /**
     * The number of normal (non-annotated) tests in the original suite.
     */
    protected int fNormalTestCount;

    /**
     * The number of ignored tests, that is, the number of tests that were
     * excluded from this suite due to their annotations.
     */
    protected int fIgnoredCount;

    /**
     * Creates a new CoreTestResult with the given flags and timeout.
     */
    public CoreTestResult(int flags, int timeout) {
        super();
    
        fFlags = flags;
        fTimeout = timeout;
    }

    /**
     * Checks whether the given TestCase method has the given annotation. 
     */
    @SuppressWarnings("unchecked")
    boolean hasAnnotation(TestCase test, Class clazz) {
        try {
            Method method = test.getClass().getMethod(test.getName());
            return method.getAnnotation(clazz) != null;
        } catch (Exception e) {
            // Ignore
        }
        
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void runProtected(final Test test, Protectable p) {
        if ((fFlags & CoreTestSuite.DRY_RUN) == 0) {
            if (test instanceof TestCase) {
                TestCase testCase = (TestCase)test;
                
                // Check whether we need to invert the test result (known failures) 
                boolean invert = hasAnnotation(testCase, KnownFailure.class) &&
                        (fFlags & CoreTestSuite.INVERT_KNOWN_FAILURES) != 0;
    
                // Check whether we need to isolate the test (side effects)
                boolean isolate = hasAnnotation(testCase, SideEffect.class) &&
                        (fFlags & CoreTestSuite.ISOLATE_NONE) == 0 ||
                        (fFlags & CoreTestSuite.ISOLATE_ALL) != 0;
                
                CoreTestRunnable runnable = new CoreTestRunnable(
                        testCase, this, p, invert, isolate);
                
                if (fTimeout > 0) {
                    Thread thread = new Thread(runnable);
                    thread.start();
                    try {
                        thread.join(fTimeout * 1000);
                    } catch (InterruptedException ex) {
                        // Ignored
                    }
                    if (thread.isAlive()) {
                        runnable.stop();
                        thread.stop();
                        try {
                            thread.join(fTimeout * 1000);
                        } catch (InterruptedException ex) {
                            // Ignored
                        }
        
                        addError(test, new CoreTestTimeout("Test timed out"));
                    }
                } else {
                    runnable.run();
                }
            }        
        }
    }

    /**
     * Updates the statistics in this TestResult. Called from the TestSuite,
     * since, once the original suite has been filtered, we don't actually see
     * these tests here anymore.
     */
    void updateStats(int total, int androidOnly, int broken, int knownFailure,
            int normal, int ignored, int sideEffect) {

        this.fTotalTestCount += total;
        this.fAndroidOnlyCount += androidOnly;
        this.fBrokenTestCount += broken;
        this.fKnownFailureCount += knownFailure;
        this.fNormalTestCount += normal;
        this.fIgnoredCount += ignored;
        this.fSideEffectCount += sideEffect;
    }
}

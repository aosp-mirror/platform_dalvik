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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import dalvik.annotation.AndroidOnly;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.KnownFailure;
import dalvik.annotation.SideEffect;

/**
 * A special TestSuite implementation that flattens the hierarchy of a given
 * JUnit TestSuite and removes tests after executing them. This is so the core
 * tests actually have a chance to succeed, since they do consume quite some
 * memory and many tests do not (have a chance to) cleanup properly after
 * themselves. The class also implements our filtering mechanism for tests, so
 * it becomes easy to only include or exclude tests based on their annotations
 * (like, say, execute all Android-only tests that are not known to be broken).
 */
public class CoreTestSuite implements Test {

    /**
     * Include all normal tests in the suite. 
     */
    public static final int RUN_NORMAL_TESTS = 1;
    
    /**
     * Include all broken tests in the suite. 
     */
    public static final int RUN_BROKEN_TESTS = 2;

    /**
     * Include all known failures in the suite. 
     */
    public static final int RUN_KNOWN_FAILURES = 4;
    
    /**
     * Include all Android-only tests in the suite. 
     */
    public static final int RUN_ANDROID_ONLY = 8;

    /**
     * Include side-effective tests in the suite. 
     */
    public static final int RUN_SIDE_EFFECTS = 16;
    
    /**
     * Include all tests in the suite. 
     */
    public static final int RUN_ALL_TESTS = 
            RUN_NORMAL_TESTS | RUN_BROKEN_TESTS | 
            RUN_KNOWN_FAILURES | RUN_SIDE_EFFECTS | RUN_ANDROID_ONLY;
    
    /**
     * Special treatment for known failures: they are expected to fail, so we
     * throw an Exception if they succeed and accept them failing. 
     */
    public static final int INVERT_KNOWN_FAILURES = 32;

    /**
     * Run each test in its own VM.
     */
    public static final int ISOLATE_ALL = 64;

    /**
     * Run no test in its own VM.
     */
    public static final int ISOLATE_NONE = 128;

    /**
     * Be verbose.
     */
    public static final int VERBOSE = 256;

    public static final int REVERSE = 512;

    public static final int DRY_RUN = 1024;
    
    /**
     * The total number of tests in the original suite.
     */
    protected int fTotalCount;
    
    /**
     * The number of Android-only tests in the original suite.
     */
    protected int fAndroidOnlyCount;
    
    /**
     * The number of broken tests in the original suite.
     */
    protected int fBrokenCount;
    
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
    protected int fNormalCount;

    /**
     * The number of ignored tests, that is, the number of tests that were
     * excluded from this suite due to their annotations.
     */
    protected int fIgnoredCount;
    
    /**
     * Contains the actual test cases in a reverse-ordered, flat list.
     */
    private Vector<Test> fTests = new Vector<Test>();

    private TestCase fVictim;

    private int fStep;
    
    private int fFlags;
    
    /**
     * Creates a new CoreTestSuite for the given ordinary JUnit Test (which may
     * be a TestCase or TestSuite). The CoreTestSuite will be a flattened and
     * potentially filtered subset of the original JUnit Test. The flags
     * determine the way we filter. 
     */
    public CoreTestSuite(Test suite, int flags, int step, TestCase victim) {
        super();
        
        fStep = step;
        addAndFlatten(suite, flags);
        fVictim = victim;
        fFlags = flags;
    }

    /**
     * Adds the given ordinary JUnit Test (which may be a TestCase or TestSuite)
     * to this CoreTestSuite. Note we are storing the tests in reverse order,
     * so it's easier to remove a finished test from the end of the list.
     */
    private void addAndFlatten(Test test, int flags) {
        if (test instanceof TestSuite) {
            TestSuite suite = (TestSuite)test;
            
            if ((flags & REVERSE) != 0) {
                for (int i = suite.testCount() - 1; i >= 0; i--) {
                    addAndFlatten(suite.testAt(i), flags);
                }
            } else {
                for (int i = 0; i < suite.testCount(); i++) {
                    addAndFlatten(suite.testAt(i), flags);
                }
            }
        } else if (test instanceof TestCase) {
            TestCase caze = (TestCase)test;
            boolean ignoreMe = false;

            boolean isAndroidOnly = hasAnnotation(caze, 
                    AndroidOnly.class);
            boolean isBrokenTest = hasAnnotation(caze, 
                    BrokenTest.class);
            boolean isKnownFailure = hasAnnotation(caze, 
                    KnownFailure.class);
            boolean isSideEffect = hasAnnotation(caze, 
                    SideEffect.class);
            boolean isNormalTest = 
                    !(isAndroidOnly || isBrokenTest || isKnownFailure ||
                      isSideEffect);

            if (isAndroidOnly) {
                fAndroidOnlyCount++;
            }

            if (isBrokenTest) {
                fBrokenCount++;
            }
            
            if (isKnownFailure) {
                fKnownFailureCount++;
            }
            
            if (isNormalTest) {
                fNormalCount++;
            }

            if (isSideEffect) {
                fSideEffectCount++;
            }
            
            if ((flags & RUN_ANDROID_ONLY) == 0 && isAndroidOnly) { 
                ignoreMe = true;
            }
            
            if ((flags & RUN_BROKEN_TESTS) == 0 && isBrokenTest) { 
                ignoreMe = true;
            }

            if ((flags & RUN_KNOWN_FAILURES) == 0 && isKnownFailure) {
                ignoreMe = true;
            }
            
            if (((flags & RUN_NORMAL_TESTS) == 0) && isNormalTest) {
                ignoreMe = true;
            }
            
            if (((flags & RUN_SIDE_EFFECTS) == 0) && isSideEffect) {
                ignoreMe = true;
            }
                
            this.fTotalCount++;
            
            if (!ignoreMe) {
                fTests.add(test);
            } else {
                this.fIgnoredCount++;
            }
        } else {
            System.out.println("Warning: Don't know how to handle " + 
                    test.getClass().getName() + " " + test.toString());
        }
    }

    /**
     * Checks whether the given TestCase class has the given annotation.
     */
    @SuppressWarnings("unchecked")
    private boolean hasAnnotation(TestCase test, Class clazz) {
        try {
            Method method = test.getClass().getMethod(test.getName());
            return method.getAnnotation(clazz) != null;
        } catch (Exception e) {
            // Ignore
        }
        
        return false;
    }
    
    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        // Run tests
        int i = 0;
        
        while (fTests.size() != 0 && !result.shouldStop()) {
            TestCase test = (TestCase)fTests.elementAt(i);
            
            Thread.currentThread().setContextClassLoader(
                    test.getClass().getClassLoader());
            
            test.run(result);

            /*
            if (fVictim != null) {
                TestResult dummy = fVictim.run();
                
                if (dummy.failureCount() != 0) {
                    result.addError(fTests.elementAt(i), new RuntimeException(
                            "Probable side effect",  
                            ((TestFailure)dummy.failures().nextElement()).
                            thrownException()));
                } else if (dummy.errorCount() != 0) {
                    result.addError(fTests.elementAt(i), new RuntimeException(
                            "Probable side effect",  
                            ((TestFailure)dummy.errors().nextElement()).
                            thrownException()));
                }
            }
            */

            fTests.remove(i);

            if (fTests.size() != 0) {
                i = (i + fStep - 1) % fTests.size();
            }

        }

        // Forward overall stats to TestResult, so ResultPrinter can see it.
        if (result instanceof CoreTestResult) {
            ((CoreTestResult)result).updateStats(
                    fTotalCount, fAndroidOnlyCount, fBrokenCount,
                    fKnownFailureCount, fNormalCount, fIgnoredCount,
                    fSideEffectCount);
        }
    }

    /**
     * Nulls all reference fields in the given test object. This method helps
     * us with those test classes that don't have an explicit tearDown()
     * method. Normally the garbage collector should take care of everything,
     * but it can't hurt to support it a bit.
     */
    private void cleanup(TestCase test) {
        Field[] fields = test.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (!f.getType().isPrimitive() &&
                    (f.getModifiers() & Modifier.STATIC) == 0) {
                try {
                    f.setAccessible(true);
                    f.set(test, null);
                } catch (Exception ex) {
                    // Nothing we can do about it.
                }
            }
        }
    }
    
    /**
     * Returns the tests as an enumeration. Note this is empty once the tests
     * have been executed.
     */
    @SuppressWarnings("unchecked")
    public Enumeration tests() {
        return fTests.elements();
    }

    /**
     * Returns the number of tests in the suite. Note this is zero once the
     * tests have been executed.
     */
    public int countTestCases() {
        return fTests.size();
    }

}

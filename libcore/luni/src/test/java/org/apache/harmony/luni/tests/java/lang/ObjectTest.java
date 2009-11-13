/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.luni.tests.java.lang;

import java.util.Vector;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.SideEffect;

@TestTargetClass(Object.class) 
public class ObjectTest extends junit.framework.TestCase {

    public boolean isCalled = false;
    /**
     * Test objects.
     */
    Object obj1 = new Object();

    Object obj2 = new Object();

    /**
     * Generic state indicator.
     */
    int status = 0;

    int ready = 0;
    TestThread1 thr1;
    TestThread2 thr2;    

    /**
     * @tests java.lang.Object#Object()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Object",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.lang.Object()
        assertNotNull("Constructor failed !!!", new Object());
    }

    /**
     * @tests java.lang.Object#equals(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean java.lang.Object.equals(java.lang.Object)
        assertTrue("Same object should be equal", obj1.equals(obj1));
        assertTrue("Different objects should not be equal", !obj1.equals(obj2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "finalize",
        args = {}
    )
    @SideEffect("Causes OutOfMemoryError to test finalization")
    public void test_finalize() {
        isCalled = false;
        class TestObject extends Object {

            Vector<StringBuffer> v = new Vector<StringBuffer>();
            public void add() {
                v.add(new StringBuffer(10000));
            }
            
            protected void finalize() throws Throwable {
                isCalled = true;
                super.finalize();
            }
        }

        TestObject to = new TestObject();
        
        try {
            while(true) {
                to.add();
            }
        } catch(OutOfMemoryError oome) {
            //expected
            to = null;
        }
        System.gc();
        System.runFinalization();
        assertTrue(isCalled);
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        MockCloneableObject mco = new MockCloneableObject();
        try {
            assertFalse(mco.equals(mco.clone()));
            assertEquals(mco.getClass(), mco.clone().getClass());
        } catch(CloneNotSupportedException cnse) {
            fail("CloneNotSupportedException was thrown.");
        }
        
        MockObject mo = new MockObject();
        try {
            mo.clone();
            fail("CloneNotSupportedException was not thrown.");
        } catch(CloneNotSupportedException cnse) {
            //expected
        }
    }
    class MockCloneableObject extends Object implements Cloneable {
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }        
    }
    
    class MockObject extends Object {
        
        boolean isCalled = false;
        
        public void finalize() throws Throwable {
            super.finalize();
            isCalled = true;
        }
        
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }        
    }
    
    /**
     * @tests java.lang.Object#getClass()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClass",
        args = {}
    )
    public void test_getClass() {
        // Test for method java.lang.Class java.lang.Object.getClass()
        String classNames[] = { "java.lang.Object", "java.lang.Throwable",
                "java.lang.StringBuffer" };
        Class<?> classToTest = null;
        Object instanceToTest = null;

        status = 0;
        for (int i = 0; i < classNames.length; ++i) {
            try {
                classToTest = Class.forName(classNames[i]);
                instanceToTest = classToTest.newInstance();
                assertTrue("Instance didn't match creator class.",
                        instanceToTest.getClass() == classToTest);
                assertTrue("Instance didn't match class with matching name.",
                        instanceToTest.getClass() == Class
                                .forName(classNames[i]));
            } catch (Exception ex) {
                fail("Unexpected exception : " + ex.getMessage());
            }
        }
    }

    /**
     * @tests java.lang.Object#hashCode()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        // Test for method int java.lang.Object.hashCode()
        assertTrue("Same object should have same hash.",
                obj1.hashCode() == obj1.hashCode());
        assertTrue("Same object should have same hash.",
                obj2.hashCode() == obj2.hashCode());
    }

    /**
     * @tests java.lang.Object#notify()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "notify",
        args = {}
    )
    public void test_notify() {
        // Test for method void java.lang.Object.notify()

        // Inner class to run test thread.
        class TestThread implements Runnable {
            public void run() {
                synchronized (obj1) {
                    try {
                        ready += 1;
                        obj1.wait();// Wait for ever.
                        status += 1;
                    } catch (InterruptedException ex) {
                        status = -1000;
                    }
                }
            }
        }
        ;

        // Start of test code.

        // Warning:
        // This code relies on each thread getting serviced within
        // 200 mSec of when it is notified. Although this
        // seems reasonable, it could lead to false-failures.

        ready = 0;
        status = 0;
        final int readyWaitSecs = 3;

        final int threadCount = 20;
        for (int i = 0; i < threadCount; ++i) {
            new Thread(new TestThread()).start();
        }
        synchronized (obj1) {
            try {

                // Wait up to readyWaitSeconds for all threads to be waiting on
                // monitor
                for (int i = 0; i < readyWaitSecs; i++) {
                    obj1.wait(1000, 0);
                    if (ready == threadCount) {
                        break;
                    }
                }

                // Check pre-conditions of testing notifyAll
                assertTrue("Not all launched threads are waiting. (ready = "
                        + ready + ")", ready == threadCount);
                assertTrue("Thread woke too early. (status = " + status + ")",
                        status == 0);

                for (int i = 1; i <= threadCount; ++i) {
                    obj1.notify();
                    obj1.wait(200, 0);
                    assertTrue("Out of sync. (expected " + i + " but got "
                            + status + ")", status == i);
                }

            } catch (InterruptedException ex) {
                fail(
                        "Unexpectedly got an InterruptedException. (status = "
                                + status + ")");
            }
        }
        
        try {
            Object obj = new Object();
            obj.notify();
            fail("IllegalMonitorStateException was not thrown.");
        } catch(IllegalMonitorStateException imse) {
            //expected
        }
    }

    /**
     * @tests java.lang.Object#notifyAll()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "notifyAll",
        args = {}
    )
    public void test_notifyAll() {
        // Test for method void java.lang.Object.notifyAll()

        // Inner class to run test thread.
        class TestThread implements Runnable {
            public void run() {
                synchronized (obj1) {
                    try {
                        ready += 1;
                        obj1.wait();// Wait for ever.
                        status += 1;
                    } catch (InterruptedException ex) {
                        status = -1000;
                    }
                }
            }
        }
        ;

        // Start of test code.

        // Warning:
        // This code relies on all threads getting serviced within
        // 5 seconds of when they are notified. Although this
        // seems reasonable, it could lead to false-failures.

        status = 0;
        ready = 0;
        final int readyWaitSecs = 3;
        final int threadCount = 20;
        for (int i = 0; i < threadCount; ++i) {
            new Thread(new TestThread()).start();
        }

        synchronized (obj1) {

            try {

                // Wait up to readyWaitSeconds for all threads to be waiting on
                // monitor
                for (int i = 0; i < readyWaitSecs; i++) {
                    obj1.wait(1000, 0);
                    if (ready == threadCount) {
                        break;
                    }
                }

                // Check pre-conditions of testing notifyAll
                assertTrue("Not all launched threads are waiting. (ready = "
                        + ready + ")", ready == threadCount);
                assertTrue("At least one thread woke too early. (status = "
                        + status + ")", status == 0);

                obj1.notifyAll();

                obj1.wait(5000, 0);

                assertTrue(
                        "At least one thread did not get notified. (status = "
                                + status + ")", status == threadCount);

            } catch (InterruptedException ex) {
                fail(
                        "Unexpectedly got an InterruptedException. (status = "
                                + status + ")");
            }

        }
        
        try {
            Object obj = new Object();
            obj.notifyAll();
            fail("IllegalMonitorStateException was not thrown.");
        } catch(IllegalMonitorStateException imse) {
            //expected
        }
    }

    /**
     * @tests java.lang.Object#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        // Test for method java.lang.String java.lang.Object.toString()
        assertNotNull("Object toString returned null.", obj1.toString());
    }

    /**
     * @tests java.lang.Object#wait()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "wait",
        args = {}
    )
    public void test_wait() {
        // Test for method void java.lang.Object.wait()

        // Inner class to run test thread.
        class TestThread implements Runnable {
            public void run() {
                synchronized (obj1) {
                    try {
                        obj1.wait();// Wait for ever.
                        status = 1;
                    } catch (InterruptedException ex) {
                        status = -1;
                    }
                }
            }
        }
        

        // Start of test code.

        // Warning:
        // This code relies on threads getting serviced within
        // 1 second of when they are notified. Although this
        // seems reasonable, it could lead to false-failures.

        status = 0;
        new Thread(new TestThread()).start();
        synchronized (obj1) {
            try {
                obj1.wait(1000, 0);
                assertTrue("Thread woke too early. (status = " + status + ")",
                        status == 0);
                obj1.notifyAll();
                obj1.wait(1000, 0);
                assertTrue("Thread did not get notified. (status = " + status
                        + ")", status == 1);
            } catch (InterruptedException ex) {
                fail(
                        "Unexpectedly got an InterruptedException. (status = "
                                + status + ")");
            }
        }
        
        try {
            Object obj = new Object();
            obj.wait();
            fail("IllegalMonitorStateException was not thrown.");
        } catch(IllegalMonitorStateException imse) {
            //expected
        } catch(InterruptedException ex) {
            fail("InterruptedException was thrown.");
        }

       try {
           thr1 = new TestThread1(TestThread1.CASE_WAIT);
           thr2 = new TestThread2();
           thr1.start();
           thr2.start();           
           thr2.join();
           thr1.join();
           thr1 = null;
           thr2 = null;
        } catch(InterruptedException e) {
            fail("InterruptedException was thrown.");
        }
        assertEquals(3, status);
    }
    
    class TestThread1 extends Thread {
        
        static final int CASE_WAIT = 0;
        static final int CASE_WAIT_LONG = 1;
        static final int CASE_WAIT_LONG_INT = 2;
        
        int testCase = CASE_WAIT;
        
        public TestThread1(int option) {
            testCase = option;
        }
        
        public void run() {
            synchronized (obj1) {
                try {
                    switch(testCase) {
                        case CASE_WAIT:
                            obj1.wait();// Wait for ever.
                            break;
                        case CASE_WAIT_LONG:
                            obj1.wait(5000L);
                            break;
                        case CASE_WAIT_LONG_INT:
                            obj1.wait(10000L, 999999);
                            break;
                    }
                    
                } catch (InterruptedException ex) {
                    status = 3;
                }
            }
        }
    }
    
    class TestThread2 extends Thread {
        public void run() {
            thr1.interrupt();
        }
    }   

    /**
     * @tests java.lang.Object#wait(long)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "wait",
        args = {long.class}
    )
    public void test_waitJ() {
        // Test for method void java.lang.Object.wait(long)

        // Start of test code.

        final int loopCount = 20;
        final int allowableError = 100; // millesconds
        final int delay = 200; // milliseconds
        synchronized (obj1) {
            try {
                int count = 0;
                long[][] toLong = new long[3][3];
                for (int i = 0; i < loopCount; ++i) {
                    long before = System.currentTimeMillis();
                    obj1.wait(delay, 0);
                    long after = System.currentTimeMillis();
                    long error = (after - before - delay);
                    if (error < 0)
                        error = -error;
                    if (i > 0 && error > allowableError) {
                        // Allow jit to warm up before testing
                        if (count < toLong.length) {
                            toLong[count][0] = i;
                            toLong[count][1] = before;
                            toLong[count][2] = after;
                            count++;
                        }
                        if (error > (1000 + delay) || count == toLong.length) {
                            StringBuffer sb = new StringBuffer();
                            for (int j = 0; j < count; j++) {
                                sb
                                        .append("wakeup time too inaccurate, iteration ");
                                sb.append(toLong[j][0]);
                                sb.append(", before: ");
                                sb.append(toLong[j][1]);
                                sb.append(" after: ");
                                sb.append(toLong[j][2]);
                                sb.append(" diff: ");
                                sb.append(toLong[j][2] - toLong[j][1]);
                                sb.append("\n");
                            }
                            fail(sb.toString());
                        }
                    }
                }
            } catch (InterruptedException ex) {
                fail(
                        "Unexpectedly got an InterruptedException. (status = "
                                + status + ")");
            }
        }
        
        try {
            Object obj = new Object();
            obj.wait(5000L);
            fail("IllegalMonitorStateException was not thrown.");
        } catch(IllegalMonitorStateException imse) {
            //expected
        } catch(InterruptedException ex) {
            fail("InterruptedException was thrown.");
        }

       try {
           thr1 = new TestThread1(TestThread1.CASE_WAIT_LONG);
           thr2 = new TestThread2();
           thr1.start();
           thr2.start();           
           thr2.join();
           thr1.join();        
           thr1 = null;
           thr2 = null;           
        } catch(InterruptedException e) {
            fail("InterruptedException was thrown.");
        }
        assertEquals(3, status);
    }

    /**
     * @tests java.lang.Object#wait(long, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "wait",
        args = {long.class, int.class}
    )
    public void test_waitJI() {
        // Test for method void java.lang.Object.wait(long, int)

        // Inner class to run test thread.
        class TestThread implements Runnable {
            public void run() {
                synchronized (obj1) {
                    try {
                        obj1.wait(0, 1); // Don't wait very long.
                        status = 1;
                        obj1.wait(0, 0); // Wait for ever.
                        status = 2;
                    } catch (InterruptedException ex) {
                        status = -1;
                    }
                }
            }
        }
        

        // Start of test code.

        // Warning:
        // This code relies on threads getting serviced within
        // 1 second of when they are notified. Although this
        // seems reasonable, it could lead to false-failures.

        status = 0;
        new Thread(new TestThread()).start();
        synchronized (obj1) {
            try {
                obj1.wait(1000, 0);
                assertTrue("Thread did not wake after 1 ms. (status = "
                        + status + ")", status == 1);
                obj1.notifyAll();
                obj1.wait(1000, 0);
                assertTrue("Thread did not get notified. (status = " + status
                        + ")", status == 2);
            } catch (InterruptedException ex) {
                fail(
                        "Unexpectedly got an InterruptedException. (status = "
                                + status + ")");
            }
        }
        
        try {
            Object obj = new Object();
            obj.wait(5000L, 1);
            fail("IllegalMonitorStateException was not thrown.");
        } catch(IllegalMonitorStateException imse) {
            //expected
        } catch(InterruptedException ex) {
            fail("InterruptedException was thrown.");
        }

       try {
           thr1 = new TestThread1(TestThread1.CASE_WAIT_LONG_INT);
           thr2 = new TestThread2();
           thr1.start();
           thr2.start();           
           thr2.join();
           thr1.join();
           thr1 = null;
           thr2 = null;           
        } catch(InterruptedException e) {
            fail("InterruptedException was thrown.");
        }
        assertEquals(3, status);

    }
}

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
package tests.api.java.lang.ref;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.AssertionFailedError;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

@TestTargetClass(Reference.class) 
public class ReferenceTest extends junit.framework.TestCase {
    Object tmpA, tmpB, obj;

    volatile WeakReference wr;

    /* 
     * For test_subclass().
     */
    static TestWeakReference twr;
    static AssertionFailedError error;
    static boolean testObjectFinalized;
    static class TestWeakReference<T> extends WeakReference<T> {
        public volatile boolean clearSeen = false;
        public volatile boolean enqueueSeen = false;

        public TestWeakReference(T referent) {
            super(referent);
        }

        public TestWeakReference(T referent, ReferenceQueue<? super T> q) {
            super(referent, q);
        }

        public void clear() {
            super.clear();
            clearSeen = true;
            if (testObjectFinalized) {
                error = new AssertionFailedError("Clear should happen " +
                        "before finalization.");
                throw error;
            }
            if (enqueueSeen) {
                error = new AssertionFailedError("Clear should happen " +
                        "before enqueue.");
                throw error;
            }
        }

        public boolean enqueue() {
            enqueueSeen = true;
            if (!clearSeen) {
                error = new AssertionFailedError("Clear should happen " +
                        "before enqueue.");
                throw error;
            }

            /* Do this last;  it may notify the main test thread,
             * and anything we'd do after it (e.g., setting clearSeen)
             * wouldn't be seen.
             */
            return super.enqueue();
        }
    }

    protected void doneSuite() {
        tmpA = tmpB = obj = null;
    }

    /**
     * @tests java.lang.ref.Reference#clear()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "clear",
          methodArgs = {}
        )
    })
    public void test_clear() {
        tmpA = new Object();
        tmpB = new Object();
        SoftReference sr = new SoftReference(tmpA, new ReferenceQueue());
        WeakReference wr = new WeakReference(tmpB, new ReferenceQueue());
        assertTrue("Start: Object not cleared.", (sr.get() != null)
                && (wr.get() != null));
        sr.clear();
        wr.clear();
        assertTrue("End: Object cleared.", (sr.get() == null)
                && (wr.get() == null));
        // Must reference tmpA and tmpB so the jit does not optimize them away
        assertTrue("should always pass", tmpA != sr.get() && tmpB != wr.get());
    }

    /**
     * @tests java.lang.ref.Reference#enqueue()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "enqueue",
          methodArgs = {}
        )
    })
    public void test_enqueue() {
        ReferenceQueue rq = new ReferenceQueue();
        obj = new Object();
        Reference ref = new SoftReference(obj, rq);
        assertTrue("Enqueue failed.", (!ref.isEnqueued())
                && ((ref.enqueue()) && (ref.isEnqueued())));
        assertTrue("Not properly enqueued.", rq.poll().get() == obj);
        // This fails...
        assertTrue("Should remain enqueued.", !ref.isEnqueued());
        assertTrue("Can not enqueue twice.", (!ref.enqueue())
                && (rq.poll() == null));

        rq = new ReferenceQueue();
        obj = new Object();
        ref = new WeakReference(obj, rq);
        assertTrue("Enqueue failed2.", (!ref.isEnqueued())
                && ((ref.enqueue()) && (ref.isEnqueued())));
        assertTrue("Not properly enqueued2.", rq.poll().get() == obj);
        assertTrue("Should remain enqueued2.", !ref.isEnqueued()); // This
        // fails.
        assertTrue("Can not enqueue twice2.", (!ref.enqueue())
                && (rq.poll() == null));
    }

    /**
     * @tests java.lang.ref.Reference#enqueue()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies positive functionality.",
      targets = {
        @TestTarget(
          methodName = "get",
          methodArgs = {}
        )
    })
    public void test_general() {
        // Test the general/overall functionality of Reference.

        class TestObject {
            public boolean finalized;

            public TestObject() {
                finalized = false;
            }

            protected void finalize() {
                finalized = true;
            }
        }

        final ReferenceQueue rq = new ReferenceQueue();

        class TestThread extends Thread {
            public void run() {
                // Create the object in a separate thread to ensure it will be
                // gc'ed
                Object testObj = new TestObject();
                wr = new WeakReference(testObj, rq);
                testObj = null;
            }
        }

        Reference ref;

        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.remove();
            assertNotNull("Object not garbage collected1.", ref);
            assertTrue("Unexpected ref1", ref == wr);
            assertNull("Object could not be reclaimed1.", wr.get());
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }

        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.poll();
            assertNotNull("Object not garbage collected.", ref);
            assertTrue("Unexpected ref2", ref == wr);
            assertNull("Object could not be reclaimed.", ref.get());
            // Reference wr so it does not get collected
            assertNull("Object could not be reclaimed.", wr.get());
        } catch (Exception e) {
            fail("Exception : " + e.getMessage());
        }
    }

    /**
     * Makes sure that overridden versions of clear() and enqueue()
     * get called, and that clear/enqueue/finalize happen in the
     * right order for WeakReferences.
     *
     * @tests java.lang.ref.Reference#clear()
     * @tests java.lang.ref.Reference#enqueue()
     * @tests java.lang.Object#finalize()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Makes sure that overridden versions of clear() and enqueue() " + 
            " get called, and that clear/enqueue/finalize happen in the " + 
            " right order for WeakReferences.",
      targets = {
        @TestTarget(
          methodName = "clear",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "enqueue",
          methodArgs = {}
        )

    })
    public void _test_subclass() {
        error = null;
        testObjectFinalized = false;
        twr = null;

        class TestObject {
            public TestWeakReference testWeakReference = null;

            public void setTestWeakReference(TestWeakReference twr) {
                testWeakReference = twr;
            }

            protected void finalize() {
                testObjectFinalized = true;
                if (!testWeakReference.clearSeen) {
                    error = new AssertionFailedError("Clear should happen " +
                            "before finalize.");
                    throw error;
                }
            }
        }

        final ReferenceQueue rq = new ReferenceQueue();

        class TestThread extends Thread {
            public void run() {
                // Create the object in a separate thread to ensure it will be
                // gc'ed
                TestObject testObj = new TestObject();
                twr = new TestWeakReference(testObj, rq);
                testObj.setTestWeakReference(twr);
                testObj = null;
            }
        }

        Reference ref;

        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.remove(5000L);    // Give up after five seconds.

            assertNotNull("Object not garbage collected.", ref);
            assertTrue("Unexpected reference.", ref == twr);
            assertNull("Object could not be reclaimed.", twr.get());
            assertTrue("Overridden clear() should have been called.",
                    twr.clearSeen);
            assertTrue("Overridden enqueue() should have been called.",
                    twr.enqueueSeen);
            assertTrue("finalize() should have been called.",
                    testObjectFinalized);
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }

    }

    /**
     * @tests java.lang.ref.Reference#get()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't check that get() can return null.",
      targets = {
        @TestTarget(
          methodName = "get",
          methodArgs = {}
        )
    })
    public void test_get() {
        // SM.
        obj = new Object();
        Reference ref = new WeakReference(obj, new ReferenceQueue());
        assertTrue("Get succeeded.", ref.get() == obj);
    }

    /**
     * @tests java.lang.ref.Reference#isEnqueued()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isEnqueued",
          methodArgs = {}
        )
    })
    public void test_isEnqueued() {
        ReferenceQueue rq = new ReferenceQueue();
        obj = new Object();
        Reference ref = new SoftReference(obj, rq);
        assertTrue("Should start off not enqueued.", !ref.isEnqueued());
        ref.enqueue();
        assertTrue("Should now be enqueued.", ref.isEnqueued());
        ref.enqueue();
        assertTrue("Should still be enqueued.", ref.isEnqueued());
        rq.poll();
        // This fails ...
        assertTrue("Should now be not enqueued.", !ref.isEnqueued());
    }

    /* Contrives a situation where the only reference to a string
     * is a WeakReference from an object that is being finalized.
     * Checks to make sure that the referent of the WeakReference
     * is still pointing to a valid object.
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Contrives a situation where the only reference to a string " + 
            " is a WeakReference from an object that is being finalized. " + 
            " Checks to make sure that the referent of the WeakReference " + 
            " is still pointing to a valid object.",
      targets = {
        @TestTarget(
          methodName = "get",
          methodArgs = {}
        )
    })
    public void test_finalizeReferenceInteraction() {
        error = null;
        testObjectFinalized = false;
    
        class TestObject {
            WeakReference<String> stringRef;

            public TestObject(String referent) {
                stringRef = new WeakReference<String>(referent);
            }

            protected void finalize() {
                try {
                    /* If a VM bug has caused the referent to get
                     * freed without the reference getting cleared,
                     * looking it up, assigning it to a local and
                     * doing a GC should cause some sort of exception.
                     */
                    String s = stringRef.get();
                    System.gc();
                    testObjectFinalized = true;
                } catch (Throwable t) {
                    error = new AssertionFailedError("something threw '" + t +
                            "' in finalize()");
                }
            }
        }

        class TestThread extends Thread {
            public void run() {
                // Create the object in a separate thread to ensure it will be
                // gc'ed
                TestObject testObj = new TestObject(new String("sup /b/"));
            }
        }

        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();

            if (error != null) {
                throw error;
            }
            assertTrue("finalize() should have been called.",
                    testObjectFinalized);
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }
    }


    protected void setUp() {
    }

    protected void tearDown() {
    }
}

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
package org.apache.harmony.luni.tests.java.lang.ref;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import junit.framework.AssertionFailedError;

public class ReferenceTest extends junit.framework.TestCase {
    Object tmpA, tmpB, obj;

    volatile WeakReference wr;
    static AssertionFailedError error;
    static boolean testObjectFinalized;

    protected void doneSuite() {
        tmpA = tmpB = obj = null;
    }

    /**
     * @tests java.lang.ref.Reference#clear()
     */
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
     * @tests java.lang.ref.Reference#get()
     */
    public void test_get() {
        // SM.
        obj = new Object();
        Reference ref = new WeakReference(obj, new ReferenceQueue());
        assertTrue("Get succeeded.", ref.get() == obj);
    }

    /**
     * @tests java.lang.ref.Reference#isEnqueued()
     */
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

    protected void setUp() {
    }

    protected void tearDown() {
    }
}

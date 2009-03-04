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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Vector;

@TestTargetClass(SoftReference.class) 
public class SoftReferenceTest extends junit.framework.TestCase {
    static Boolean bool;
    SoftReference r;

    protected void doneSuite() {
        bool = null;
    }

    /**
     * @tests java.lang.ref.SoftReference#SoftReference(java.lang.Object,
     *        java.lang.ref.ReferenceQueue)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SoftReference",
        args = {java.lang.Object.class, java.lang.ref.ReferenceQueue.class}
    )
    public void test_ConstructorLjava_lang_ObjectLjava_lang_ref_ReferenceQueue() {
        ReferenceQueue rq = new ReferenceQueue();
        bool = new Boolean(true);
        try {
            SoftReference sr = new SoftReference(bool, rq);
            assertTrue("Initialization failed.", ((Boolean) sr.get())
                    .booleanValue());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }

        boolean exception = false;
        try {
            new SoftReference(bool, null);
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue("Should not throw NullPointerException", !exception);
    }

    /**
     * @tests java.lang.ref.SoftReference#SoftReference(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SoftReference",
        args = {java.lang.Object.class}
    )
    public void test_ConstructorLjava_lang_Object() {
        bool = new Boolean(true);
        try {
            SoftReference sr = new SoftReference(bool);
            assertTrue("Initialization failed.", ((Boolean) sr.get())
                    .booleanValue());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.ref.SoftReference#get()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verified that get() can return null.",
        method = "get",
        args = {}
    )
    public void test_get() {
        bool = new Boolean(false);
        SoftReference sr = new SoftReference(bool);
        assertTrue("Same object not returned.", bool == sr.get());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {}
    )
    public void test_get_SoftReference() {

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
                Object testObj = new TestObject();
                r = new SoftReference(testObj, rq);
            }
        }
        Reference ref;
        try {
            TestThread t = new TestThread();
            t.start();
            t.join();
            Vector<StringBuffer> v = new Vector<StringBuffer>();     
            try {
                while(true) {
                    v.add(new StringBuffer(10000));
                }
            } catch(OutOfMemoryError ofme) {
                v = null;
            }
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }

        assertNull("get() should return null " +
                "if OutOfMemoryError is thrown.", r.get());

        try {
            TestThread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.poll();
            assertNotNull("Object not garbage collected.", ref);
            assertNull("Object is not null.", ref.get());
            assertNotNull("Object could not be reclaimed.", r.get());
        } catch (Exception e) {
            fail("Exception : " + e.getMessage());
        }
    }
    
    protected void setUp() {
    }

    protected void tearDown() {
    }
}

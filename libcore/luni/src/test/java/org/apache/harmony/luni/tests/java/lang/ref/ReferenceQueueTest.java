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

public class ReferenceQueueTest extends junit.framework.TestCase {
    static Boolean b;

    static Integer integer;

    protected void doneSuite() {
        b = null;
        integer = null;
    }

    public class ChildThread implements Runnable {
        public ChildThread() {
        }

        public void run() {
            try {
                rq.wait(1000);
            } catch (Exception e) {
            }
            synchronized (rq) {
                // store in a static so it won't be gc'ed because the jit
                // optimized it out
                integer = new Integer(667);
                SoftReference sr = new SoftReference(integer, rq);
                sr.enqueue();
                rq.notify();
            }
        }
    }

    ReferenceQueue rq;

    /**
     * @tests java.lang.ref.ReferenceQueue#poll()
     */
    public void test_poll() {
        // store in a static so it won't be gc'ed because the jit
        // optimized it out
        b = new Boolean(true);
        SoftReference sr = new SoftReference(b, rq);
        sr.enqueue();
        try {
            assertTrue("Remove failed.", ((Boolean) rq.poll().get())
                    .booleanValue());
        } catch (Exception e) {
            fail("Exception during the test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.ref.ReferenceQueue#remove()
     */
    public void test_remove() {
        // store in a static so it won't be gc'ed because the jit
        // optimized it out
        b = new Boolean(true);
        SoftReference sr = new SoftReference(b, rq);
        sr.enqueue();
        try {
            assertTrue("Remove failed.", ((Boolean) rq.remove().get())
                    .booleanValue());
        } catch (Exception e) {
            fail("Exception during the test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.ref.ReferenceQueue#remove(long)
     */
    public void test_removeJ() {
        try {
            assertNull("Queue should be empty. (poll)", rq.poll());
            assertNull("Queue should be empty. (remove(1))",
                    rq.remove((long) 1));
            Thread ct = new Thread(new ChildThread());
            ct.start();
            Reference ret = rq.remove(0L);
            assertNotNull("Delayed remove failed.", ret);
        } catch (InterruptedException e) {
            fail("InterruptedExeException during test : " + e.getMessage());
        }
        catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.ref.ReferenceQueue#ReferenceQueue()
     */
    public void test_Constructor() {
        assertTrue("Used for testing.", true);
    }

    protected void setUp() {
        rq = new ReferenceQueue();
    }

    protected void tearDown() {
    }
}

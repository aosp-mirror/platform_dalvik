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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftReferenceTest extends junit.framework.TestCase {
    static Boolean bool;

    protected void doneSuite() {
        bool = null;
    }

    /**
     * @tests java.lang.ref.SoftReference#SoftReference(java.lang.Object,
     *        java.lang.ref.ReferenceQueue)
     */
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
    public void test_get() {
        bool = new Boolean(false);
        SoftReference sr = new SoftReference(bool);
        assertTrue("Same object not returned.", bool == sr.get());
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }
}

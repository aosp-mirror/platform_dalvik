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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

@TestTargetClass(SoftReference.class) 
public class SoftReferenceTest extends junit.framework.TestCase {
    static Boolean bool;

    protected void doneSuite() {
        bool = null;
    }

    /**
     * @tests java.lang.ref.SoftReference#SoftReference(java.lang.Object,
     *        java.lang.ref.ReferenceQueue)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "SoftReference",
          methodArgs = {Object.class, java.lang.ref.ReferenceQueue.class}
        )
    })
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
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "SoftReference",
          methodArgs = {Object.class}
        )
    })
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
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify that this method returns null if this " +
            "reference object has been cleared.",
      targets = {
        @TestTarget(
          methodName = "get",
          methodArgs = {}
        )
    })
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

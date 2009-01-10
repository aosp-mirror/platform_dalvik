/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.prefs.tests.java.util.prefs;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.util.prefs.BackingStoreException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * 
 * 
 */
@TestTargetClass(BackingStoreException.class)
public class BackingStoreExceptionTest extends TestCase {

    /*
     * Class under test for void BackingStoreException(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BackingStoreException",
        args = {java.lang.String.class}
    )
    public void testBackingStoreExceptionString() {
        BackingStoreException e = new BackingStoreException("msg");
        assertNull(e.getCause());
        assertEquals("msg", e.getMessage());
    }

    /*
     * Class under test for void BackingStoreException(Throwable)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BackingStoreException",
        args = {java.lang.Throwable.class}
    )
    public void testBackingStoreExceptionThrowable() {
        Throwable t = new Throwable("msg");
        BackingStoreException e = new BackingStoreException(t);
        assertTrue(e.getMessage().indexOf(t.getClass().getName()) >= 0);
        assertTrue(e.getMessage().indexOf("msg") >= 0);
        assertEquals(t, e.getCause());
    }

    /**
     * @tests serialization/deserialization.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new BackingStoreException("msg"));
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new BackingStoreException("msg"));
    }
}

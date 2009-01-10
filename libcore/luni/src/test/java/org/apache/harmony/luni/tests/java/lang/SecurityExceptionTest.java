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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;
import org.apache.harmony.testframework.serialization.SerializationTest;

@TestTargetClass(SecurityException.class) 
public class SecurityExceptionTest extends TestCase {
    
    /**
     * @tests java.lang.SecurityException#SecurityException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {}
    )
    public void test_Constructor() {
        SecurityException e = new SecurityException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.SecurityException#SecurityException(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        SecurityException e = new SecurityException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    
    /**
     * @tests java.lang.SecurityException#SecurityException(String, Throwable)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_StringLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        SecurityException e = new SecurityException("fixture", npe);
        assertSame("fixture", e.getMessage());
        assertSame(npe, e.getCause());
    }

    /**
     * @tests java.lang.SecurityException#SecurityException(Throwable)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecurityException",
        args = {java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        SecurityException e = new SecurityException(npe);
        assertSame(npe, e.getCause());
    }

    /**
     * @tests serialization/deserialization.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )    
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new SecurityException());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new SecurityException());
    }
}

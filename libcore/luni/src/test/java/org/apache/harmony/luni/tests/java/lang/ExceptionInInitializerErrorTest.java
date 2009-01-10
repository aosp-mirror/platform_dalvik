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

@TestTargetClass(ExceptionInInitializerError.class) 
public class ExceptionInInitializerErrorTest extends junit.framework.TestCase {

    /**
     * @tests java.lang.ExceptionInInitializerError#ExceptionInInitializerError()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ExceptionInInitializerError",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        )
    })
    public void test_Constructor() {
        ExceptionInInitializerError e = new ExceptionInInitializerError();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.ExceptionInInitializerError#ExceptionInInitializerError(java.lang.String)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ExceptionInInitializerError",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        )
    })
    public void test_ConstructorLjava_lang_String() {
        ExceptionInInitializerError e = new ExceptionInInitializerError("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.ExceptionInInitializerExceptionInInitializerError#ExceptionInInitializerError(java.lang.Throwable)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ExceptionInInitializerError",
            args = {java.lang.Throwable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getException",
            args = {}
        )
    })
    public void test_ConstructorLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException("fixture");
        ExceptionInInitializerError e = new ExceptionInInitializerError(npe);
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertSame(npe, e.getException());
        assertSame(npe, e.getCause());
    }

}

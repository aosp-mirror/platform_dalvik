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

package org.apache.harmony.annotation.tests.java.lang.annotation;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.lang.annotation.AnnotationFormatError;

/**
 * Test case of java.lang.annotation.AnnotationFormatError
 */
@TestTargetClass(AnnotationFormatError.class) 
public class AnnotationFormatErrorTest extends TestCase {
    /**
     * @tests java.lang.annotation.AnnotationFormatError#AnnotationFormatError(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AnnotationFormatError",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_String() {
        AnnotationFormatError e = new AnnotationFormatError("some message");
        assertEquals("some message", e.getMessage());
    }

    /**
     * @tests java.lang.annotation.AnnotationFormatError#AnnotationFormatError(Throwable)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AnnotationFormatError",
        args = {java.lang.Throwable.class}
    )
    public void test_constructorLjava_lang_Throwable() {
        IllegalArgumentException iae = new IllegalArgumentException();
        AnnotationFormatError e = new AnnotationFormatError(iae);
        assertSame(iae, e.getCause());
    }

    /**
     * @tests java.lang.annotation.AnnotationFormatError#AnnotationFormatError(String,Throwable)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AnnotationFormatError",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_StringLjava_lang_Throwable() {
        IllegalArgumentException iae = new IllegalArgumentException();
        AnnotationFormatError e = new AnnotationFormatError("some message", iae);
        assertEquals("some message", e.getMessage());
        assertSame(iae, e.getCause());
    }
}

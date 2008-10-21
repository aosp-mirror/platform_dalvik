/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.tests.java.lang;

import junit.framework.TestCase;

public class StackTraceElementTest extends TestCase {
    private StackTraceElementOriginal original;

    @Override
    protected void setUp() throws Exception {
        original = new StackTraceElementOriginal();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @tests java.lang.StackTraceElement#StackTraceElement(java.lang.String,
     *     java.lang.String, java.lang.String, int)
     */
    public void
    test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringI() {
        StackTraceElement ste2 = null;
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste1 = e.getStackTrace()[0];
            ste2 = new StackTraceElement(ste1.getClassName(),
                    ste1.getMethodName(),
                    ste1.getFileName(),
                    ste1.getLineNumber());
            assertEquals("Incorrect value of class name",
                    ste1.getClassName(), ste2.getClassName());
            assertEquals("Incorrect value of method name",
                    ste1.getMethodName(), ste2.getMethodName());
            assertEquals("Incorrect value of file name",
                    ste1.getFileName(), ste2.getFileName());
            assertEquals("Incorrect value of line number",
                    ste1.getLineNumber(), ste2.getLineNumber());
        }
        assertNotNull("Incorrect stack trace object", ste2);
        try {
            new StackTraceElement(null,
                    ste2.getMethodName(),
                    ste2.getFileName(),
                    ste2.getLineNumber());
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new StackTraceElement(ste2.getClassName(),
                    null,
                    ste2.getFileName(),
                    ste2.getLineNumber());
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new StackTraceElement(ste2.getClassName(),
                    ste2.getMethodName(),
                    null,
                    ste2.getLineNumber());
        } catch (NullPointerException e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste1 = e.getStackTrace()[0];
            StackTraceElement ste2 =
                new StackTraceElement(ste1.getClassName(),
                        ste1.getMethodName(),
                        ste1.getFileName(),
                        ste1.getLineNumber());
            assertEquals("Objects are equaled", ste1, ste2);
        }
    }

    /**
     * @tests java.lang.StackTraceElement#getClassName()
     */
    public void test_getClassName() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect class name",
                    getClass().getPackage().getName() +
                    ".StackTraceElementOriginal",
                    e.getStackTrace()[0].getClassName());
            assertEquals("Incorrect class name",
                    getClass().getPackage().getName() +
                    ".StackTraceElementTest",
                    e.getStackTrace()[1].getClassName());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#getFileName()
     */
    public void test_getFileName() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect file name",
                    "StackTraceElementOriginal.java",
                    e.getStackTrace()[0].getFileName());
            assertEquals("Incorrect file name",
                    "StackTraceElementTest.java",
                    e.getStackTrace()[1].getFileName());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#getLineNumber()
     */
    public void test_getLineNumber() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect line number",
                    24, e.getStackTrace()[0].getLineNumber());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#getMethodName()
     */
    public void test_getMethodName() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect method name",
                    "pureJavaMethod",
                    e.getStackTrace()[0].getMethodName());
            assertEquals("Incorrect method name",
                    "test_getMethodName",
                    e.getStackTrace()[1].getMethodName());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#hashCode()
     */
    public void test_hashCode() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste1 = e.getStackTrace()[0];
            StackTraceElement ste2 =
                new StackTraceElement(ste1.getClassName(),
                        ste1.getMethodName(),
                        ste1.getFileName(),
                        ste1.getLineNumber());
            assertEquals("Incorrect value of hash code",
                    ste1.hashCode(), ste2.hashCode());
            assertFalse("Incorrect value of hash code",
                    ste1.hashCode() == e.getStackTrace()[1].hashCode());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#isNativeMethod()
     */
    public void test_isNativeMethod() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertFalse("Incorrect method type",
                    e.getStackTrace()[0].isNativeMethod());
        }
        try {
            original.pureNativeMethod(new Object());
        } catch (Error e) {
            assertTrue("Incorrect method type",
                    e.getStackTrace()[0].isNativeMethod());
        }
    }

    /**
     * @tests java.lang.StackTraceElement#toString()
     */
    public void test_toString() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste = e.getStackTrace()[0];
            assertTrue("String representation doesn't contain a package name",
                    ste.toString().contains(getClass().getPackage().getName()));
            assertTrue("String representation doesn't contain a class name",
                    ste.toString().contains("StackTraceElementOriginal"));
            assertTrue("String representation doesn't contain a file name",
                    ste.toString().contains("StackTraceElementOriginal.java"));
            assertTrue("String representation doesn't contain a line number",
                    ste.toString().contains("24"));
            assertTrue("String representation doesn't contain a method name",
                    ste.toString().contains("pureJavaMethod"));
        }
    }
}

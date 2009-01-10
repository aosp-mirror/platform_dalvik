/*
 * Copyright (C) 2008 The Android Open Source Project
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

package tests.SQLite;

import SQLite.FunctionContext;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

@TestTargetClass(FunctionContext.class)
public class FunctionContextTest extends TestCase {
    
    /**
     * @param name
     */
    public FunctionContextTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        
    }
    
    /**
     * Test method for {@link SQLite.FunctionContext#FunctionContext()}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "constructor test",
        method = "FunctionContext",
        args = {}
    )
    public void testFunctionContext() {
        fail("Not yet implemented");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link SQLite.FunctionContext#set_result(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "set_result",
        args = {java.lang.String.class}
    )
    public void testSet_resultString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.FunctionContext#set_result(int)}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "set_result",
        args = {int.class}
    )
    public void testSet_resultInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.FunctionContext#set_result(double)}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "set_result",
        args = {double.class}
    )
    public void testSet_resultDouble() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.FunctionContext#set_error(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "set_error",
        args = {java.lang.String.class}
    )
    public void testSet_error() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.FunctionContext#set_result(byte[])}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "set_result",
        args = {byte[].class}
    )
    public void testSet_resultByteArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.FunctionContext#set_result_zeroblob(int)}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "set_result_zeroblob",
        args = {int.class}
    )
    public void testSet_result_zeroblob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.FunctionContext#count()}.
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "method test",
        method = "count",
        args = {}
    )
    public void testCount() {
        fail("Not yet implemented");
    }

}

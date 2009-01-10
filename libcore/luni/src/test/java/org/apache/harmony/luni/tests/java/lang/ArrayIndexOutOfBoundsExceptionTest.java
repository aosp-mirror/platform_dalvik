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

@TestTargetClass(ArrayIndexOutOfBoundsException.class) 
public class ArrayIndexOutOfBoundsExceptionTest extends TestCase {

    /**
     * @tests java.lang.ArrayIndexOutOfBoundsException#ArrayIndexOutOfBoundsException(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayIndexOutOfBoundsException",
        args = {int.class}
    )
    public void test_ConstructorI() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(-1);
        assertNotNull(e.getMessage());
        assertTrue("Unable to find index value in 'message' property.", e.getMessage().indexOf(
                "-1", 0) >= 0);

    }

    /**
     * @tests java.lang.ArrayIndexOutOfBoundsException#ArrayIndexOutOfBoundsException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayIndexOutOfBoundsException",
        args = {}
    )
    public void test_Constructor() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException();
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.ArrayIndexOutOfBoundsException#ArrayIndexOutOfBoundsException(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ArrayIndexOutOfBoundsException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}

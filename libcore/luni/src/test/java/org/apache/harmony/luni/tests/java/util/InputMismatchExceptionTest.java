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
package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import org.apache.harmony.testframework.serialization.SerializationTest;

@TestTargetClass(InputMismatchException.class) 
public class InputMismatchExceptionTest extends TestCase {

    private static final String ERROR_MESSAGE = "for serialization test"; //$NON-NLS-1$

    /**
     * @tests java.util.InputMismatchException#InputMismatchException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InputMismatchException",
        args = {}
    )
    @SuppressWarnings("cast")
    public void test_Constructor() {
        InputMismatchException exception = new InputMismatchException();
        assertNotNull(exception);
        assertTrue(exception instanceof NoSuchElementException);
        assertTrue(exception instanceof Serializable);
    }

    /**
     * @tests java.util.InputMismatchException#InputMismatchException(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InputMismatchException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        InputMismatchException exception = new InputMismatchException(
                ERROR_MESSAGE);
        assertNotNull(exception);
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    /**
     * @tests serialization/deserialization.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new InputMismatchException(ERROR_MESSAGE));
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new InputMismatchException(
                ERROR_MESSAGE));
    }
}

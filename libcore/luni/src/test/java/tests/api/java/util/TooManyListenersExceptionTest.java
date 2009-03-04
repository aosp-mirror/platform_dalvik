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

package tests.api.java.util;

import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass; 

import java.util.TooManyListenersException;

@TestTargetClass(TooManyListenersException.class) 
public class TooManyListenersExceptionTest extends junit.framework.TestCase {

    /**
     * @tests java.util.TooManyListenersException#TooManyListenersException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TooManyListenersException",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.util.TooManyListenersException()
        try {
            throw new TooManyListenersException();
        } catch (TooManyListenersException e) {
            assertNull(
                    "Message thrown with exception constructed with no message",
                    e.getMessage());
        }
    }

    /**
     * @tests java.util.TooManyListenersException#TooManyListenersException(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TooManyListenersException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.util.TooManyListenersException(java.lang.String)
        try {
            throw new TooManyListenersException("Gah");
        } catch (TooManyListenersException e) {
            assertEquals("Incorrect message thrown with exception", "Gah", e
                    .getMessage());
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}

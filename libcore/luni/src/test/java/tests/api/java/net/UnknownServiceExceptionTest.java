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

package tests.api.java.net;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.net.URL;
import java.net.UnknownServiceException;

@TestTargetClass(UnknownServiceException.class) 
public class UnknownServiceExceptionTest extends junit.framework.TestCase {

    /**
     * @tests java.net.UnknownServiceException#UnknownServiceException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownServiceException",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.net.UnknownServiceException()
        try {
            new URL("file:///moo.txt").openConnection().getOutputStream();
        } catch (UnknownServiceException e) {
            // correct
            return;
        } catch (Exception e) {
            fail("Wrong exception during test : " + e.getMessage());
        }
        fail("Exception not thrown");
    }

    /**
     * @tests java.net.UnknownServiceException#UnknownServiceException(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownServiceException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.net.UnknownServiceException(java.lang.String)
        try {
            if (true)
                throw new UnknownServiceException("HelloWorld");
        } catch (UnknownServiceException e) {
            assertTrue("Wrong exception message: " + e.toString(), e
                    .getMessage().equals("HelloWorld"));
            return;
        }
        fail("Constructor failed");
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

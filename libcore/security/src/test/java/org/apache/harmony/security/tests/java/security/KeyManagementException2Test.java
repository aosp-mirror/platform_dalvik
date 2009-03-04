/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.security.tests.java.security;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.security.KeyManagementException;

@TestTargetClass(KeyManagementException.class)
public class KeyManagementException2Test extends junit.framework.TestCase {

    /**
     * @tests java.security.KeyManagementException#KeyManagementException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.security.KeyManagementException()
        KeyManagementException e = new KeyManagementException();
        assertEquals("Failed toString test for constructed instance",
                "java.security.KeyManagementException", e.toString());
    }

    /**
     * @tests java.security.KeyManagementException#KeyManagementException(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Checking with null and empty string parameter missed",
        method = "KeyManagementException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        // Test for method
        // java.security.KeyManagementException(java.lang.String)
        KeyManagementException e = new KeyManagementException("test message");
        assertEquals("Failed toString test for constructed instance",
                "java.security.KeyManagementException: test message",
                e.toString());
    }
}
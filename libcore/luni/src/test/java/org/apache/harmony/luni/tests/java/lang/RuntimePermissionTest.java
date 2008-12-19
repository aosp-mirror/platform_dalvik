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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(RuntimePermission.class) 
public class RuntimePermissionTest extends junit.framework.TestCase {

    /**
     * @tests java.lang.RuntimePermission#RuntimePermission(java.lang.String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "RuntimePermission",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.lang.RuntimePermission(java.lang.String)
        RuntimePermission r = new RuntimePermission("createClassLoader");
        assertEquals("Returned incorrect name", 
                "createClassLoader", r.getName());

    }

    /**
     * @tests java.lang.RuntimePermission#RuntimePermission(java.lang.String,
     *        java.lang.String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "RuntimePermission",
          methodArgs = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        // Test for method java.lang.RuntimePermission(java.lang.String,
        // java.lang.String)
        RuntimePermission r = new RuntimePermission("createClassLoader", null);
        assertEquals("Returned incorrect name", 
                "createClassLoader", r.getName());
    }
}

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

import java.security.SecurityPermission;

public class SecurityPermission2Test extends junit.framework.TestCase {

    /**
     * @tests java.security.SecurityPermission#SecurityPermission(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.security.SecurityPermission(java.lang.String)
        assertEquals("create securityPermission constructor(string) failed",
                "SecurityPermission(string)", new SecurityPermission("SecurityPermission(string)").getName()
                        );

    }

    /**
     * @tests java.security.SecurityPermission#SecurityPermission(java.lang.String,
     *        java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        // Test for method java.security.SecurityPermission(java.lang.String,
        // java.lang.String)
        SecurityPermission sp = new SecurityPermission("security.file", "write");
        assertEquals("creat securityPermission constructor(string,string) failed",
                "security.file", sp.getName());

    }
}
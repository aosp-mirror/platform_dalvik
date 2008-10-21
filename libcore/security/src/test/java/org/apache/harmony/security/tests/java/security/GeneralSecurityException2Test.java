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

import java.security.GeneralSecurityException;

public class GeneralSecurityException2Test extends junit.framework.TestCase {

    /**
     * @tests java.security.GeneralSecurityException#GeneralSecurityException()
     */
    public void test_Constructor() {
        // Test for method java.security.GeneralSecurityException()
        GeneralSecurityException e = new GeneralSecurityException();
        assertNotNull("Constructor returned null instance", e);
        assertEquals("Failed toString test for constructed instance", "java.security.GeneralSecurityException", e
                .toString());
    }

    /**
     * @tests java.security.GeneralSecurityException#GeneralSecurityException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method
        // java.security.GeneralSecurityException(java.lang.String)
        GeneralSecurityException e = new GeneralSecurityException(
                "test message");
        assertNotNull("Constructor returned null instance", e);
        assertEquals("Failed toString test for constructed instance", 
                        "java.security.GeneralSecurityException: test message", e
                .toString());
    }
}
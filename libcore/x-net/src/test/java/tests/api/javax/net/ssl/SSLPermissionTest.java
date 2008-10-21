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

package tests.api.javax.net.ssl;

import javax.net.ssl.SSLPermission;

import junit.framework.TestCase;

/**
 * Tests for <code>SSLPermission</code> class constructors.
 *  
 */
public class SSLPermissionTest extends TestCase {

    /*
     * Class under test for void SSLPermission(String)
     */
    public void test_ConstructorLjava_lang_String() {
    	try {
    		SSLPermission p = new SSLPermission("name");
    		assertEquals("Incorrect permission name", "name", p.getName());
        } catch (Exception e) {
        	fail("Unexpected exception " + e.toString());
        }
    }

    /*
     * Class under test for void SSLPermission(String, String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
    	try {
    		SSLPermission p = new SSLPermission("name", "value");
    		assertEquals("Incorrect permission name", "name", p.getName());
    		assertEquals("Incorrect default permission actions",
    				"", p.getActions());
        } catch (Exception e) {
        	fail("Unexpected exception " + e.toString());
        }
    }
}

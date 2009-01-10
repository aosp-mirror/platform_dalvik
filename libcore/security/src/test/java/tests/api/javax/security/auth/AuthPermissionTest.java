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

package tests.api.javax.security.auth;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import javax.security.auth.AuthPermission;


/**
 * Tests for <code>AuthPermission</code> class constructors and methods.
 * 
 */
@TestTargetClass(AuthPermission.class) 
public class AuthPermissionTest extends TestCase {

    /**
     * @tests javax.security.auth.AuthPermission#AuthPermission(String name) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AuthPermission",
        args = {String.class}
    )
    public void test_Constructor_01() {
        String[] strParam = {"", null};
        
        try {
            AuthPermission ap = new AuthPermission("AuthPermissionName");
            assertNotNull("Null object returned", ap);
            assertEquals("AuthPermissionName", ap.getName());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        for (int i = 0; i < strParam.length; i++) {
            try {
                AuthPermission ap = new AuthPermission(strParam[i]);
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * @tests javax.security.auth.AuthPermission#AuthPermission(String name, String actions) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AuthPermission",
        args = {String.class, String.class}
    )
    public void test_Constructor_02() {
        String[] strParam = {"", null};
        String[] actionParam = {"", null, "ActionName"};
        
        try {
            AuthPermission ap = new AuthPermission("AuthPermissionName", null);
            assertNotNull("Null object returned", ap);
            assertEquals("AuthPermissionName", ap.getName());
            assertEquals("", ap.getActions());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        for (int i = 0; i < strParam.length; i++) {
            try {
                AuthPermission ap = new AuthPermission(strParam[i], null);
            } catch (Exception e) {
            }
        }
        
        for (int i = 0; i < actionParam.length; i++) {
            try {
                AuthPermission ap = new AuthPermission("AuthPermissionName", actionParam[i]);
                assertNotNull("Null object returned", ap);
                assertEquals("", ap.getActions());
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
}

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

import javax.security.auth.login.LoginException;

/**
 * Tests for <code>LoginException</code> class constructors and methods.
 * 
 */
@TestTargetClass(LoginException.class) 
public class LoginExceptionTest extends TestCase {
    
    public static void main(String[] args) {
    }

    /**
     * Constructor for LoginExceptionTest.
     * 
     * @param arg0
     */
    public LoginExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };


    /**
     * @tests javax.security.auth.login.LoginException#LoginException() 
     * Assertion: constructs LoginException with no detail message
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LoginException",
        args = {}
    )
    public void testLoginException01() {
        LoginException lE = new LoginException();
        assertNull("getMessage() must return null.", lE.getMessage());
        assertNull("getCause() must return null", lE.getCause());
    }
    
    /**
     * @tests javax.security.auth.login.LoginException#LoginException(String msg) 
     * Assertion: constructs with not null parameter.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "LoginException",
        args = {String.class}
    )
    public void testLoginException02() {
        LoginException lE;
        for (int i = 0; i < msgs.length; i++) {
            lE = new LoginException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), lE.getMessage(), msgs[i]);
            assertNull("getCause() must return null", lE.getCause());
        }
    }

    /**
     * @tests javax.security.auth.login.LoginException#LoginException(String msg) 
     * Assertion: constructs with null parameter.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "LoginException",
        args = {String.class}
    )
    public void testLoginException03() {
        String msg = null;
        LoginException lE = new LoginException(msg);
        assertNull("getMessage() must return null.", lE.getMessage());
        assertNull("getCause() must return null", lE.getCause());
    }
}

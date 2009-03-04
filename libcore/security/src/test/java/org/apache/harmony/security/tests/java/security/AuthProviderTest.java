/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.security.AuthProvider;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import junit.framework.TestCase;
@TestTargetClass(AuthProvider.class)
public class AuthProviderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @tests java.security.AuthProvider#AuthProvider(String, double, String)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AuthProvider",
            args = {java.lang.String.class, double.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "login",
            args = {javax.security.auth.Subject.class, javax.security.auth.callback.CallbackHandler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "logout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setCallbackHandler",
            args = {javax.security.auth.callback.CallbackHandler.class}
        )
    })
    public void testConstructor01() {
        AuthProviderStub ap = new AuthProviderStub("name", 1.0, "info");
        CallbackHandler handler = null;
        String[] str = {"", null, "!@#$%^&*()"};
        double[] version = {0.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.NEGATIVE_INFINITY};
        
        assertEquals("name", ap.getName());
        assertEquals(1.0, ap.getVersion());
        assertEquals("info", ap.getInfo());
        assertNotNull(ap.getServices());
        assertTrue(ap.getServices().isEmpty());
        
        for (int i = 0; i < str.length; i++) {
            for (int j = 0; j < version.length; j++) {
                try {
                    ap = new AuthProviderStub(str[i], version[j], str[i]);
                } catch (Exception ex) {
                    fail("Unexpected exception was thrown");
                }
            }
        }
        
        try {
            ap.setCallbackHandler(handler);
            ap.login(null, handler);
            ap.logout();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    
    public class AuthProviderStub extends AuthProvider {
        public AuthProviderStub(String name, double version, String info) {
            super( name,  version, info);
        }
        public void login(Subject subject, CallbackHandler handler) {}
        public void logout() {}
        public void setCallbackHandler(CallbackHandler handler){}

    }
}

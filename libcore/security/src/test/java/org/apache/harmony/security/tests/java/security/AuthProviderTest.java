/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.security.tests.java.security;

import java.security.AuthProvider;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import junit.framework.TestCase;

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
    
    public void testConstructor() {
        AuthProviderStub ap = new AuthProviderStub("name", 1.0, "info");
        assertEquals("name", ap.getName());
        assertEquals(1.0, ap.getVersion());
        assertEquals("info", ap.getInfo());
        assertNotNull(ap.getServices());
        assertTrue(ap.getServices().isEmpty());
        
        try {
            new AuthProviderStub(null, -1.0, null);
        } catch (Exception e) {
            fail("unexpected exception");
        }
    }
    
    private class AuthProviderStub extends AuthProvider {
        public AuthProviderStub(String name, double version, String info) {
            super( name,  version, info);
        }
        public void login(Subject subject, CallbackHandler handler) {
            
        }
        public void logout() {
            
        }
        public void setCallbackHandler(CallbackHandler handler){
            
        }

    }
}

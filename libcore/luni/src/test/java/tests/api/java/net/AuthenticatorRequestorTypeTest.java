/*
 * Copyright (C) 2008 The Android Open Source Project
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

package tests.api.java.net;

import junit.framework.TestCase;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.net.Authenticator;

@TestTargetClass(Authenticator.RequestorType.class) 
public class AuthenticatorRequestorTypeTest extends TestCase {
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )   
    public void test_valueOfLjava_lang_String() {
        assertEquals(Authenticator.RequestorType.PROXY, 
                Authenticator.RequestorType.valueOf("PROXY"));
        assertEquals(Authenticator.RequestorType.SERVER, 
                Authenticator.RequestorType.valueOf("SERVER"));
        try {
            Authenticator.RequestorType.valueOf("TEST");
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
            //expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )    
    public void test_values () {
        Authenticator.RequestorType[] expectedTypes = {
                Authenticator.RequestorType.PROXY,
                Authenticator.RequestorType.SERVER
        };
        
        Authenticator.RequestorType[] types = 
            Authenticator.RequestorType.values();
        assertEquals(expectedTypes.length, types.length);
  
        for(int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], types[i]);
        }
    }
}

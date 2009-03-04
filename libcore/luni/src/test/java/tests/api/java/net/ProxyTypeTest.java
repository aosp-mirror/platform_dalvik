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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.net.Proxy;

@TestTargetClass(Proxy.Type.class) 
public class ProxyTypeTest extends TestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void test_valueOf() {
        Proxy.Type [] types = {Proxy.Type.DIRECT, Proxy.Type.HTTP, 
                Proxy.Type.SOCKS};
        
        String [] strTypes = {"DIRECT", "HTTP", "SOCKS"}; 
        
        for(int i = 0; i < strTypes.length; i++) {
            assertEquals(types[i], Proxy.Type.valueOf(strTypes[i]));
        }
        
        String [] incTypes = {"", "direct", "http", "socks", " HTTP"};
        
        for(String str:incTypes) {
            try {
                Proxy.Type.valueOf(str);
                fail("IllegalArgumentException was not thrown.");
            } catch(IllegalArgumentException iae) {
                //expected
            }
        }
        
        try {
            Proxy.Type.valueOf(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
            //expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_values() {
        Proxy.Type [] types = { Proxy.Type.DIRECT, Proxy.Type.HTTP, 
                Proxy.Type.SOCKS };
        
        Proxy.Type [] result = Proxy.Type.values();
        
        assertEquals(types.length, result.length);
        
        for(int i = 0; i < result.length; i++) {
         assertEquals(types[i], result[i]);   
        }
    }
}

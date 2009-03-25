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
package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.KeyStoreBuilderParameters;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

@TestTargetClass(KeyStoreBuilderParameters.class) 
public class KeyStoreBuilderParametersTest extends TestCase {
    
    /**
     * @tests javax.net.ssl.KeyStoreBuilderParameters#KeyStoreBuilderParameters(KeyStore.Builder builder)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyStoreBuilderParameters",
        args = {java.security.KeyStore.Builder.class}
    )
    public void test_Constructor01() {
        KeyStore.Builder bld = null;
        
        //Null parameter
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(bld);
            assertNotNull(ksp.getParameters());
        } catch (NullPointerException npe) {
            fail("NullPointerException should not be thrown");
        }
        
        //Not null parameter
        KeyStore.ProtectionParameter pp = new ProtectionParameterImpl();
        bld = KeyStore.Builder.newInstance("testType", null, pp);
        assertNotNull("Null object KeyStore.Builder", bld);
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(bld);
            assertNotNull(ksp.getParameters());
        } catch (Exception e) {
            fail("Unexpected exception was thrown");
        }
    }
    
    /**
     * @tests javax.net.ssl.KeyStoreBuilderParameters#KeyStoreBuilderParameters(List parameters)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyStoreBuilderParameters",
        args = {java.util.List.class}
    )
    public void test_Constructor02() {
               
        //Null parameter
        List<String> ls = null;
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(ls);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException npe) {
            //expected
        }
        
        //Empty parameter
        List<String> lsEmpty = new ArrayList<String>();
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(lsEmpty);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        
        //Not null parameter
        List<String> lsFiled = new ArrayList<String>();;
        lsFiled.add("Parameter1");
        lsFiled.add("Parameter2");
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(lsFiled);
            assertTrue("Not instanceof KeyStoreBuilderParameters object", 
                       ksp instanceof KeyStoreBuilderParameters); 
        } catch (Exception e) {
            fail("Unexpected exception was thrown");
        }
    }
    
    /**
     * @tests javax.net.ssl.KeyStoreBuilderParameters#getParameters()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParameters",
        args = {}
    )
    public void test_getParameters() {
        String[] param = {"Parameter1", "Parameter2", "Parameter3"};
        List<String> ls = new ArrayList<String>();
        for (int i = 0; i < param.length; i++) {
            ls.add(param[i]);
        }
        KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(ls);
        try {
            List<String> res_list = ksp.getParameters();
            try {
                res_list.add("test");
            } catch (UnsupportedOperationException e) {
                // expected
            }
            Object[] res = res_list.toArray(); 
            if (res.length == param.length) {
                for (int i = 0; i < res.length; i++) {
                    if (!param[i].equals(res[i])) {
                        fail("Parameters not equal");
                    }
                }
            } else {
                fail("Incorrect number of parameters");
            }
        } catch (Exception e) {
            fail("Unexpected exception was thrown");
        }
    }
    
    class ProtectionParameterImpl implements KeyStore.ProtectionParameter {
        ProtectionParameterImpl(){}
    }
}


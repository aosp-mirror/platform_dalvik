/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.security.interfaces;
import dalvik.annotation.AndroidOnly;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.security.KeyPairGenerator;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;

@TestTargetClass(ECKey.class)
public class ECKeyTest extends TestCase {
    
    /**
     * @tests java.security.interfaces.ECKey
     * #getParams()
     * test covers following use cases
     *   Case 1: check private key
     *   Case 2: check public key
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParams",
        args = {}
    )
    @AndroidOnly("EC is not supported for android. " + 
                 "EC is not define in RI.")
    public void test_getParams() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", Util.prov);                       
        gen.initialize(Util.ecParam);        
        ECKey key = null;
        
        // Case 1: check private key
        key = (ECKey) gen.generateKeyPair().getPrivate();
        assertECParameterSpecEquals(Util.ecParam, key.getParams());
        
        // Case 2: check public key
        key = (ECKey) gen.generateKeyPair().getPublic();                       
        assertECParameterSpecEquals(Util.ecParam, key.getParams());                       
    }

    private void assertECParameterSpecEquals(ECParameterSpec expected, ECParameterSpec actual) {
        assertEquals("cofactors don't match", expected.getCofactor(), actual.getCofactor());
        assertEquals("curves don't match", expected.getCurve(), actual.getCurve());
        assertEquals("generator don't match", expected.getGenerator(), actual.getGenerator());
        assertEquals("order don't match", expected.getOrder(), actual.getOrder());
        
    }
}

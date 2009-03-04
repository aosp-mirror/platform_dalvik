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
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.AndroidOnly;

import junit.framework.TestCase;

import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;

@TestTargetClass(ECPrivateKey.class)
public class ECPrivateKeyTest extends TestCase {
    
    /**
     * @tests java.security.interfaces.ECPrivateKey
     * #getS()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getS",
        args = {}
    )
    @SuppressWarnings("serial")
    @AndroidOnly("EC is not supported for android. " + 
                 "EC is not define in RI.")
    public void test_getS() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", Util.prov);
        gen.initialize(Util.ecParam, new SecureRandom(new MySecureRandomSpi(),
                null) {
        });
        ECPrivateKey key = (ECPrivateKey) gen.generateKeyPair().getPrivate();
        assertEquals("Invalid S", Util.RND_RET, key.getS());
    }

}

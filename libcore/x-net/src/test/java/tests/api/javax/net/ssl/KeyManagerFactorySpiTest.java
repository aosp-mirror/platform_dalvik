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

package tests.api.javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;

import junit.framework.TestCase;

public class KeyManagerFactorySpiTest extends TestCase {

    private class MockKeyManagerFactorySpi extends KeyManagerFactorySpi {
        public MockKeyManagerFactorySpi() {
            super();
        }

        /** 
         * @see javax.net.ssl.KeyManagerFactorySpi#engineGetKeyManagers()
         */
        @Override
        protected KeyManager[] engineGetKeyManagers() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.KeyManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
         */
        @Override
        protected void engineInit(ManagerFactoryParameters arg0) throws InvalidAlgorithmParameterException {
            // it is a fake
        }

        /**
         * @see javax.net.ssl.KeyManagerFactorySpi#engineInit(java.security.KeyStore, char[])
         */
        @Override
        protected void engineInit(KeyStore arg0, char[] arg1) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
            // it is a fake
        }
    } 
    
    /**
     * @tests javax.net.ssl.KeyManagerFactorySpi#KeyManagerFactorySpi()
     */
    public void test_Constructor() {
        try {
            new MockKeyManagerFactorySpi();
        } catch (Exception e) {
            fail("Unexpected Exception " + e.toString());
        }
    }
}

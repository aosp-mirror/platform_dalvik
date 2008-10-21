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

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

import junit.framework.TestCase;

public class TrustManagerFactorySpiTest extends TestCase {

    private class MockTrustManagerFactorySpi extends TrustManagerFactorySpi {
        public MockTrustManagerFactorySpi() {
            super();
        }

        /**
         * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
         */
        @Override
        protected TrustManager[] engineGetTrustManagers() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
         */
        @Override
        protected void engineInit(KeyStore ks) throws KeyStoreException {
            // it is a fake
        }

        /**
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
         */
        @Override
        protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            // it is a fake
        }
    }
    
    /**
     * @tests javax.net.ssl.TrustManagerFactorySpi#TrustManagerFactorySpi()
     */
    public void test_Constructor() {
        try {
            new MockTrustManagerFactorySpi();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}

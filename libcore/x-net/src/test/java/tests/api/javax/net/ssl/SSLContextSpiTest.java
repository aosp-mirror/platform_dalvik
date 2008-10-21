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

import java.security.KeyManagementException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import junit.framework.TestCase;

public class SSLContextSpiTest extends TestCase {

    private class MockSSLContextSpi extends SSLContextSpi {
        public MockSSLContextSpi() {
            super();
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineCreateSSLEngine()
         */
        @Override
        protected SSLEngine engineCreateSSLEngine() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineCreateSSLEngine(java.lang.String, int)
         */
        @Override
        protected SSLEngine engineCreateSSLEngine(String host, int port) {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineGetClientSessionContext()
         */
        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineGetServerSessionContext()
         */
        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineGetServerSocketFactory()
         */
        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineGetSocketFactory()
         */
        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLContextSpi#engineInit(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom)
         */
        @Override
        protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
            // it is a fake
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLContextSpi#SSLContextSpi()
     */
    public void test_Constructor() {
        try {
            new MockSSLContextSpi();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}

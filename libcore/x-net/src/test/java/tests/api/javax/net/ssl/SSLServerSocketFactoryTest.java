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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

import junit.framework.TestCase;

public class SSLServerSocketFactoryTest extends TestCase {
    
    private class MockSSLServerSocketFactory extends SSLServerSocketFactory {
        public MockSSLServerSocketFactory() {
            super();
        }

        /**
         * @see javax.net.ssl.SSLServerSocketFactory#getDefaultCipherSuites()
         */
        @Override
        public String[] getDefaultCipherSuites() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLServerSocketFactory#getSupportedCipherSuites()
         */
        @Override
        public String[] getSupportedCipherSuites() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ServerSocketFactory#createServerSocket(int)
         */
        @Override
        public ServerSocket createServerSocket(int arg0) throws IOException {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ServerSocketFactory#createServerSocket(int, int)
         */
        @Override
        public ServerSocket createServerSocket(int arg0, int arg1) throws IOException {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ServerSocketFactory#createServerSocket(int, int, java.net.InetAddress)
         */
        @Override
        public ServerSocket createServerSocket(int arg0, int arg1, InetAddress arg2) throws IOException {
            // it is a fake
            return null;
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocketFactory#SSLServerSocketFactory()
     */
    public void test_Constructor() {
        try {
            new MockSSLServerSocketFactory();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests javax.net.ssl.SSLServerSocketFactory#getDefault()
     */
    public void test_getDefault() {
        assertNotNull("Incorrect default socket factory",
                SSLServerSocketFactory.getDefault());
    }
}

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
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import junit.framework.TestCase;

public class SSLSocketFactoryTest extends TestCase {

    private class MockSSLSocketFactory extends SSLSocketFactory {
        public MockSSLSocketFactory() {
            super();
        }

        /**
         * @see javax.net.ssl.SSLSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
         */
        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
         */
        @Override
        public String[] getDefaultCipherSuites() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
         */
        @Override
        public String[] getSupportedCipherSuites() {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
         */
        @Override
        public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
         */
        @Override
        public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
         */
        @Override
        public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
            // it is a fake
            return null;
        }

        /**
         * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int, java.net.InetAddress, int)
         */
        @Override
        public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
            // it is a fake
            return null;
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocketFactory#SSLSocketFactory()
     */
    public void test_Constructor() {
        try {
            new MockSSLSocketFactory();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocketFactory#getDefault()
     */
    public void test_getDefault() {
        assertNotNull("Incorrect default socket factory",
                SSLSocketFactory.getDefault());
    }
}

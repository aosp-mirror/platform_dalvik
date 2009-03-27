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
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

import junit.framework.TestCase;

@TestTargetClass(SSLServerSocketFactory.class)
public class SSLServerSocketFactoryTest extends TestCase {

    private class MockSSLServerSocketFactory extends SSLServerSocketFactory {
        public MockSSLServerSocketFactory() {
            super();
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return null;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return null;
        }

        @Override
        public ServerSocket createServerSocket(int arg0) throws IOException {
            return null;
        }

        @Override
        public ServerSocket createServerSocket(int arg0, int arg1)
                throws IOException {
            return null;
        }

        @Override
        public ServerSocket createServerSocket(int arg0, int arg1,
                InetAddress arg2) throws IOException {
            return null;
        }
    }

    /**
     * @tests javax.net.ssl.SSLServerSocketFactory#SSLServerSocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLServerSocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            MockSSLServerSocketFactory ssf = new MockSSLServerSocketFactory();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests javax.net.ssl.SSLServerSocketFactory#getDefault()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public void test_getDefault() {
        assertNotNull("Incorrect default socket factory",
                SSLServerSocketFactory.getDefault());
    }

    /**
     * @tests javax.net.ssl.SSLServerSocketFactory#getDefaultCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultCipherSuites",
        args = {}
    )
    public void test_getDefaultCipherSuites() {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
                .getDefault();
        try {
            assertTrue(ssf.getDefaultCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.SSLServerSocketFactory#getSupportedCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
                .getDefault();
        try {
            assertTrue(ssf.getSupportedCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}

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

package tests.api.java.net;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTests {

    public static void run() {
        TestRunner.main(new String[] { AllTests.class.getName() });
    }

    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for java.net");

        // add net testsuites here
        suite.addTestSuite(AuthenticatorTest.class);
        suite.addTestSuite(BindExceptionTest.class);
        suite.addTestSuite(ConnectExceptionTest.class);
        suite.addTestSuite(CookieHandlerTest.class);
        suite.addTestSuite(DatagramPacketTest.class);
        suite.addTestSuite(DatagramSocketImplTest.class);
        suite.addTestSuite(DatagramSocketTest.class);
        suite.addTestSuite(ExcludedProxyTest.class);
        suite.addTestSuite(JarURLConnectionTest.class);
        suite.addTestSuite(MalformedURLExceptionTest.class);
        suite.addTestSuite(MulticastSocketTest.class);
        suite.addTestSuite(NetPermissionTest.class);
        suite.addTestSuite(NetworkInterfaceTest.class);
        suite.addTestSuite(NoRouteToHostExceptionTest.class);
        suite.addTestSuite(PasswordAuthenticationTest.class);
        suite.addTestSuite(ProtocolExceptionTest.class);
        suite.addTestSuite(ProxySelectorTest.class);
        suite.addTestSuite(ProxyTest.class);
        suite.addTestSuite(ResponseCacheTest.class);
        suite.addTestSuite(ServerSocketTest.class);
        suite.addTestSuite(SocketExceptionTest.class);
        suite.addTestSuite(SocketImplTest.class);
        suite.addTestSuite(SocketPermissionTest.class);
        suite.addTestSuite(SocketTest.class);
        suite.addTestSuite(UnknownHostExceptionTest.class);
        suite.addTestSuite(UnknownServiceExceptionTest.class);
        suite.addTestSuite(URISyntaxExceptionTest.class);
        suite.addTestSuite(URITest.class);
        suite.addTestSuite(URLClassLoaderTest.class);
        suite.addTestSuite(URLDecoderTest.class);
        suite.addTestSuite(URLEncoderTest.class);
        
        return suite;
    }
}

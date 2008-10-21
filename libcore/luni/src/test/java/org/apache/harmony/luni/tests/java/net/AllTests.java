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

package org.apache.harmony.luni.tests.java.net;

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
        suite.addTestSuite(ContentHandlerTest.class);
        suite.addTestSuite(HttpRetryExceptionTest.class);
        suite.addTestSuite(Inet4AddressTest.class);
        suite.addTestSuite(Inet6AddressTest.class);
        suite.addTestSuite(InetAddressTest.class);
        suite.addTestSuite(InetSocketAddressTest.class);
        suite.addTestSuite(URITest.class);
        suite.addTestSuite(URLConnectionTest.class);
        suite.addTestSuite(URLEncoderTest.class);
        suite.addTestSuite(URLTest.class);
        
        return suite;
    }
}

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

package tests.security;

import dalvik.annotation.TestTargetClass;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@TestTargetClass(java.security.Permissions.class)
/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 */
public class SecurityPermissionsTest extends TestCase {
    
    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for security permissions");

        suite.addTestSuite(tests.security.permissions.JavaIoFileInputStreamTest.class);
        suite.addTestSuite(tests.security.permissions.JavaIoFileOutputStreamTest.class);
        suite.addTestSuite(tests.security.permissions.JavaIoRandomAccessFileTest.class);
        suite.addTestSuite(tests.security.permissions.JavaIoFileTest.class);
        suite.addTestSuite(tests.security.permissions.JavaIoObjectInputStreamTest.class);
        suite.addTestSuite(tests.security.permissions.JavaIoObjectOutputStreamTest.class);
        suite.addTestSuite(tests.security.permissions.JavaLangSystemTest.class);
        suite.addTestSuite(tests.security.permissions.JavaLangClassTest.class);
        suite.addTestSuite(tests.security.permissions.JavaLangClassLoaderTest.class);
        suite.addTestSuite(tests.security.permissions.JavaSecurityPolicyTest.class);
        suite.addTestSuite(tests.security.permissions.JavaSecuritySecurityTest.class);
        suite.addTestSuite(tests.security.permissions.JavaUtilLocale.class);
        suite.addTestSuite(tests.security.permissions.JavaNetServerSocketTest.class);
        suite.addTestSuite(tests.security.permissions.JavaNetDatagramSocketTest.class);
        suite.addTestSuite(tests.security.permissions.JavaNetMulticastSocketTest.class);

        return suite;
    }
    
}


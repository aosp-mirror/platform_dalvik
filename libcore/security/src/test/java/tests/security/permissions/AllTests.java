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

package tests.security.permissions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(java.security.Permissions.class)
/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 */
public class AllTests extends TestCase {
    
    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for security permissions");

        suite.addTestSuite(JavaIoFileInputStreamTest.class);
        suite.addTestSuite(JavaIoFileOutputStreamTest.class);
        suite.addTestSuite(JavaIoFileTest.class);
        suite.addTestSuite(JavaIoObjectInputStreamTest.class);
        suite.addTestSuite(JavaIoObjectOutputStreamTest.class);
        suite.addTestSuite(JavaIoRandomAccessFileTest.class);
        suite.addTestSuite(JavaLangClassLoaderTest.class);
        suite.addTestSuite(JavaLangClassTest.class);
        suite.addTestSuite(JavaLangRuntimeTest.class);
        suite.addTestSuite(JavaLangSystemTest.class);
        suite.addTestSuite(JavaLangThreadTest.class);
        suite.addTestSuite(JavaNetDatagramSocketTest.class);
        suite.addTestSuite(JavaNetMulticastSocketTest.class);
        suite.addTestSuite(JavaNetServerSocketTest.class);
        suite.addTestSuite(JavaNetSocketTest.class);
        suite.addTestSuite(JavaSecurityPolicyTest.class);
        suite.addTestSuite(JavaSecuritySecurityTest.class);
        suite.addTestSuite(JavaUtilLocale.class);
        suite.addTestSuite(JavaUtilZipZipFile.class);
        suite.addTestSuite(JavaxSecurityAuthSubjectDomainCombiner.class);
        suite.addTestSuite(JavaxSecurityAuthSubject.class);
        suite.addTestSuite(JavaLangReflectAccessibleObjectTest.class);
        
        return suite;
    }

}


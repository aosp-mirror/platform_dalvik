/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.archive.tests.java.util.jar;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for java.util.jar package.
 */
public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Suite org.apache.harmony.archive.tests.java.util.jar");
        // $JUnit-BEGIN$
        suite.addTestSuite(AttributesNameTest.class);
        suite.addTestSuite(AttributesTest.class);
        suite.addTestSuite(JarEntryTest.class);
        suite.addTestSuite(JarExceptionTest.class);
        suite.addTestSuite(JarExecTest.class);
        suite.addTestSuite(JarFileTest.class);
        suite.addTestSuite(JarInputStreamTest.class);
        suite.addTestSuite(JarOutputStreamTest.class);
        suite.addTestSuite(ManifestTest.class);
        suite.addTestSuite(ZipExecTest.class);
        // $JUnit-END$
        return suite;
    }
}

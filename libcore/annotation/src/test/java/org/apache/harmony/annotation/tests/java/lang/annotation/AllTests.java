/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.annotation.tests.java.lang.annotation;

import org.apache.harmony.nio_char.tests.java.nio.charset.ASCIICharsetEncoderTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for java.nio.charset package.
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for java.lang.annotation");
        // $JUnit-BEGIN$

        suite.addTestSuite(AnnotationFormatErrorTest.class);
        suite.addTestSuite(AnnotationTypeMismatchExceptionTest.class);
        suite.addTestSuite(ElementTypeTest.class);
        suite.addTestSuite(IncompleteAnnotationExceptionTest.class);
        suite.addTestSuite(RetentionPolicyTest.class);

        // $JUnit-END$
        return suite;
    }
}

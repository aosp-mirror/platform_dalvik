/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package tests.security.spec;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.security.spec.MGF1ParameterSpec;

/**
 * Test for MGF1ParameterSpec class
 * 
 */
@TestTargetClass(MGF1ParameterSpec.class)
public class MGF1ParameterSpecTest extends TestCase {

    /**
     * Meaningless algorithm name just for testing purposes
     */
    private static final String testAlgName = "TEST";

    /**
     * Constructor for MGF1ParameterSpecTest.
     * @param arg0
     */
    public MGF1ParameterSpecTest(String arg0) {
        super(arg0);
    }

    //
    // Tests
    //

    /**
     * Test #1 for <code>MGF1ParameterSpec</code> constructor<br>
     * Assertion: constructs new <code>MGF1ParameterSpec</code>
     * object using valid parameter
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies positive case. Empty/invalid parameter checking missed.",
      targets = {
        @TestTarget(
          methodName = "MGF1ParameterSpec",
          methodArgs = {java.lang.String.class}
        )
    })
    public final void testMGF1ParameterSpec01() {
        new MGF1ParameterSpec(testAlgName);
    }

    /**
     * Test #2 for <code>MGF1ParameterSpec</code> constructor<br>
     * Assertion: <code>NullPointerException</code> if parameter is <code>null</code>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies null as a parameter. Empty/invalid parameter checking missed.",
      targets = {
        @TestTarget(
          methodName = "MGF1ParameterSpec",
          methodArgs = {java.lang.String.class}
        )
    })
    public final void testMGF1ParameterSpec02() {
        try {
            new MGF1ParameterSpec(null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
    }

    /**
     * Test for <code>getDigestAlgorithm</code> method<br>
     * Assertion: returns the algorithm name of the message
     * digest used by the mask generation function
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getDigestAlgorithm",
          methodArgs = {}
        )
    })
    public final void testGetDigestAlgorithm() {
        MGF1ParameterSpec aps = new MGF1ParameterSpec(testAlgName);
        assertTrue(testAlgName.equals(aps.getDigestAlgorithm()));
    }

    /**
     * Test for public static fields and <code>getDigestAlgorithm</code> method<br>
     * Assertion: returns the algorithm name of the message
     * digest used by the mask generation function
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Field testing",
      targets = {
        @TestTarget(
          methodName = "",
          methodArgs = {}
        )
    })
    public final void testFieldsGetDigestAlgorithm() {
        assertEquals("SHA-1", MGF1ParameterSpec.SHA1.getDigestAlgorithm());
        assertEquals("SHA-256", MGF1ParameterSpec.SHA256.getDigestAlgorithm());
        assertEquals("SHA-384", MGF1ParameterSpec.SHA384.getDigestAlgorithm());
        assertEquals("SHA-512", MGF1ParameterSpec.SHA512.getDigestAlgorithm());
    }
}

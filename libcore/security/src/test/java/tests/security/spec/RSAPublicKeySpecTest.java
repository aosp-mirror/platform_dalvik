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

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Tests for <code>RSAPublicKeySpec</code> class fields and methods
 * 
 */
@TestTargetClass(RSAPublicKeySpec.class)
public class RSAPublicKeySpecTest extends TestCase {

    /**
     * Constructor for RSAPublicKeySpecTest.
     * @param name
     */
    public RSAPublicKeySpecTest(String name) {
        super(name);
    }


    /**
     * Test #1 for <code>RSAPublicKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPublicKeySpec</code>
     * object using valid parameters
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies constructor with valid parameters.",
      targets = {
        @TestTarget(
          methodName = "RSAPublicKeySpec",
          methodArgs = {java.math.BigInteger.class, java.math.BigInteger.class}
        )
    })
    public final void testRSAPublicKeySpec01() {
        KeySpec ks =
            new RSAPublicKeySpec(BigInteger.valueOf(1234567890L),
                                 BigInteger.valueOf(3L));
        
        assertTrue(ks instanceof RSAPublicKeySpec);
    }

    /**
     * Test #2 for <code>RSAPublicKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPublicKeySpec</code>
     * object using valid parameters
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as parameters.",
      targets = {
        @TestTarget(
          methodName = "RSAPublicKeySpec",
          methodArgs = {java.math.BigInteger.class, java.math.BigInteger.class}
        )
    })
    public final void testRSAPublicKeySpec02() {
        KeySpec ks =
            new RSAPublicKeySpec(null, null);

        assertTrue(ks instanceof RSAPublicKeySpec);
    }
    
    /**
     * Test for <code>getModulus()</code> method<br>
     * Assertion: returns modulus
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getModulus",
          methodArgs = {}
        )
    })
    public final void testGetModulus() {
        RSAPublicKeySpec rpks =
            new RSAPublicKeySpec(BigInteger.valueOf(1234567890L),
                                 BigInteger.valueOf(3L));
        assertTrue(BigInteger.valueOf(1234567890L).equals(rpks.getModulus()));
    }

    /**
     * Test for <code>getPublicExponent()</code> method<br>
     * Assertion: returns public exponent
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPublicExponent",
          methodArgs = {}
        )
    })
    public final void testGetPublicExponent() {
        RSAPublicKeySpec rpks =
            new RSAPublicKeySpec(BigInteger.valueOf(3L),
                                 BigInteger.valueOf(1234567890L));
        assertTrue(BigInteger.valueOf(1234567890L).equals(rpks.getPublicExponent()));
    }

}

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
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;

/**
 * Tests for <code>RSAPrivateCrtKeySpec</code> class fields and methods
 * 
 */
@TestTargetClass(RSAPrivateCrtKeySpec.class)
public class RSAPrivateCrtKeySpecTest extends TestCase {

    /**
     * Constructor for RSAPrivateCrtKeySpecTest.
     * @param name
     */
    public RSAPrivateCrtKeySpecTest(String name) {
        super(name);
    }

    /**
     * Test #1 for <code>RSAPrivateCrtKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPrivateCrtKeySpec</code>
     * object using valid parameters
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies constructor with valid parameters.",
      targets = {
        @TestTarget(
          methodName = "RSAPrivateCrtKeySpec",
          methodArgs = {java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class}
        )
    })
    public final void testRSAPrivateCrtKeySpec01() {
        KeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(ks instanceof RSAPrivateCrtKeySpec);
    }

    /**
     * Test #2 for <code>RSAPrivateCrtKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPrivateCrtKeySpec</code>
     * object using valid parameters
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies constructor with valid parameters.",
      targets = {
        @TestTarget(
          methodName = "RSAPrivateCrtKeySpec",
          methodArgs = {java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class}
        )
    })
    public final void testRSAPrivateCrtKeySpec02() {
        KeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(ks instanceof RSAPrivateKeySpec);
    }

    /**
     * Test #3 for <code>RSAPrivateCrtKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPrivateCrtKeySpec</code>
     * object using valid parameters
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as parameters.",
      targets = {
        @TestTarget(
          methodName = "RSAPrivateCrtKeySpec",
          methodArgs = {java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class, 
                  java.math.BigInteger.class, java.math.BigInteger.class}
        )
    })
    public final void testRSAPrivateCrtKeySpec03() {
        new RSAPrivateCrtKeySpec(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
    
    /**
     * Test for <code>getCrtCoefficient()</code> method<br>
     * Assertion: returns crt coefficient
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getCrtCoefficient",
          methodArgs = {}
        )
    })
    public final void testGetCrtCoefficient() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L));
        assertTrue(BigInteger.valueOf(5L).equals(ks.getCrtCoefficient()));
    }

    /**
     * Test for <code>getPrimeExponentP()</code> method<br>
     * Assertion: returns prime exponent P
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPrimeExponentP",
          methodArgs = {}
        )
    })
    public final void testGetPrimeExponentP() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeExponentP()));
    }

    /**
     * Test for <code>getPrimeExponentQ()</code> method<br>
     * Assertion: returns prime exponent Q
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPrimeExponentQ",
          methodArgs = {}
        )
    })
    public final void testGetPrimeExponentQ() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeExponentQ()));
    }

    /**
     * Test for <code>getPrimeP()</code> method<br>
     * Assertion: returns prime P
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPrimeP",
          methodArgs = {}
        )
    })
    public final void testGetPrimeP() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeP()));
    }

    /**
     * Test for <code>getPrimeQ()</code> method<br>
     * Assertion: returns prime Q
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPrimeQ",
          methodArgs = {}
        )
    })
    public final void testGetPrimeQ() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeQ()));
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
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPublicExponent()));
    }

    //
    // Tests for inherited methods
    //
    
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
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getModulus()));
    }

    /**
     * Test for <code>getPrivateExponent()</code> method<br>
     * Assertion: returns private exponent
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPrivateExponent",
          methodArgs = {}
        )
    })
    public final void testGetPrivateExponent() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrivateExponent()));
    }

}

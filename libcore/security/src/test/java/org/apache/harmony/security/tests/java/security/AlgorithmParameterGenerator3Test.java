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

package org.apache.harmony.security.tests.java.security;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.DSAParameterSpec;

@TestTargetClass(AlgorithmParameterGenerator.class)
public class AlgorithmParameterGenerator3Test extends junit.framework.TestCase {

    /**
     * @tests java.security.AlgorithmParameterGenerator#generateParameters()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "generateParameters",
        args = {}
    )
    public void test_generateParameters() throws Exception {

        //fail("Takes ages. Problem with SecureRandom and stub math ?");
        
        // Test for method java.security.AlgorithmParameters
        // java.security.AlgorithmParameterGenerator.generateParameters()
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        gen.init(1024);

        // WARNING - The next line can take MINUTES to run
        AlgorithmParameters params = gen.generateParameters();
        assertNotNull("params is null", params);
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#getAlgorithm()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() throws Exception {
        // Test for method java.lang.String
        // java.security.AlgorithmParameterGenerator.getAlgorithm()
        String alg = AlgorithmParameterGenerator.getInstance("DSA")
                .getAlgorithm();
        assertEquals("getAlgorithm ok", "DSA", alg);
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#getInstance(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies getInstance with parameter",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws Exception {
        // Test for method java.security.AlgorithmParameterGenerator
        // java.security.AlgorithmParameterGenerator.getInstance(java.lang.String)
        AlgorithmParameterGenerator.getInstance("DSA");
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#getInstance(java.lang.String,
     *        java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Test NoSuchAlgorithmException is missed",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() throws Exception {
        // Test for method java.security.AlgorithmParameterGenerator
        // java.security.AlgorithmParameterGenerator.getInstance(java.lang.String,
        // java.lang.String)

        // Opting for DSA here as it is pretty widely supported
               Provider[] provs = Security
                       .getProviders("AlgorithmParameterGenerator.DSA");

        for (int i = 0; i < provs.length; i++) {
                AlgorithmParameterGenerator.getInstance("DSA", provs[i].getName());
            }// end for


        // exception case - should throw IllegalArgumentException for null
        // provider
        try {
            AlgorithmParameterGenerator.getInstance("DSA", (String) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // exception case - should throw IllegalArgumentException for empty
        // provider
        try {
            AlgorithmParameterGenerator.getInstance("DSA", "");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // exception case - should throw NoSuchProviderException
        try {
            AlgorithmParameterGenerator.getInstance("DSA", "IDontExist");
            fail("Should have thrown NoSuchProviderException");
        } catch (NoSuchProviderException e) {
            // Expected
        }
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#getProvider()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies provider with null parameter",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() throws Exception {
        // Test for method java.security.Provider
        // java.security.AlgorithmParameterGenerator.getProvider()
            // checks that no exception is thrown
        Provider p = AlgorithmParameterGenerator.getInstance("DSA")
                .getProvider();
        assertNotNull("provider is null", p);
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#init(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class}
    )
    public void test_initI() throws Exception {
        // Test for method void
        // java.security.AlgorithmParameterGenerator.init(int)
            // checks that no exception is thrown
        int[] valid = {512, 576, 640, 960, 1024};
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        
        for (int i = 0; i < valid.length; i++) {
            try {
                gen.init(valid[i]);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid parameter" + valid[i]);
                
            }
        }
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#init(int,
     *        java.security.SecureRandom)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.SecureRandom.class}
    )
    public void test_initILjava_security_SecureRandom() throws Exception {
        // Test for method void
        // java.security.AlgorithmParameterGenerator.init(int,
        // java.security.SecureRandom)
            // checks that no exception is thrown
        int[] valid = {512, 576, 640, 960, 1024};
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");

        for (int i = 0; i < valid.length; i++) {
            try {
                gen.init(valid[i], new SecureRandom());
                gen.init(valid[i], null);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid parameter" + valid[i]);
                
            }
        }
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#init(java.security.spec.AlgorithmParameterSpec)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies InvalidAlgorithmParameterException exception only",
        method = "init",
        args = {java.security.spec.AlgorithmParameterSpec.class}
    )
    public void test_initLjava_security_spec_AlgorithmParameterSpec() throws Exception {
        // Test for method void
        // java.security.AlgorithmParameterGenerator.init(java.security.spec.AlgorithmParameterSpec)
        // checks that InvalidAlgorithmParameterException is thrown
        DSAParameterSpec spec = new DSAParameterSpec(BigInteger.ONE,
                BigInteger.ONE, BigInteger.ONE);
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        try {
            gen.init(spec);
            fail("No expected InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException e) {
            //expected
        }
    }

    /**
     * @tests java.security.AlgorithmParameterGenerator#init(java.security.spec.AlgorithmParameterSpec,
     *        java.security.SecureRandom)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies InvalidAlgorithmParameterException exception only",
        method = "init",
        args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
    )
    public void test_initLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom() throws Exception {
        // Test for method void
        // java.security.AlgorithmParameterGenerator.init(java.security.spec.AlgorithmParameterSpec,
        // java.security.SecureRandom)
        // checks that InvalidAlgorithmParameterException  is thrown
        DSAParameterSpec spec = new DSAParameterSpec(BigInteger.ONE,
                BigInteger.ONE, BigInteger.ONE);
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        try {
            gen.init(spec, new SecureRandom());
            fail("No expected InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException e) {
            //expected
        }
    }
}

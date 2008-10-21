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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;

import org.apache.harmony.security.tests.support.SpiEngUtils;

import junit.framework.TestCase;


/**
 * Tests for KeyPairGenerator class
 * 
 */

public class KeyPairGenerator3Test extends TestCase {

    /**
     * Constructor for KeyPairGenerator3Test.
     * 
     * @param arg0
     */
    public KeyPairGenerator3Test(String arg0) {
        super(arg0);
    }

    private static String validProviderName = null;

    public static Provider validProvider = null;

    private static boolean DSASupported = false;
    
    private static String NotSupportMsg = KeyPairGenerator1Test.NotSupportMsg;

    static {
        validProvider = SpiEngUtils.isSupport(
                KeyPairGenerator1Test.validAlgName,
                KeyPairGenerator1Test.srvKeyPairGenerator);
        DSASupported = (validProvider != null);
        validProviderName = (DSASupported ? validProvider.getName() : null);
    }

    protected KeyPairGenerator[] createKPGen() {
        if (!DSASupported) {
            fail(KeyPairGenerator1Test.validAlgName
                    + " algorithm is not supported");
            return null;
        }
        KeyPairGenerator[] kpg = new KeyPairGenerator[3];
        try {
            kpg[0] = KeyPairGenerator
                    .getInstance(KeyPairGenerator1Test.validAlgName);
            kpg[1] = KeyPairGenerator.getInstance(
                    KeyPairGenerator1Test.validAlgName, validProvider);
            kpg[2] = KeyPairGenerator.getInstance(
                    KeyPairGenerator1Test.validAlgName, validProviderName);
            return kpg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * Test for <code>generateKeyPair()</code> and <code>genKeyPair()</code>
     * methods
     * Assertion: KeyPairGenerator was initialized before the invocation 
     * of these methods
     */
    public void testGenKeyPair01() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator[] kpg = createKPGen();
        assertNotNull("KeyPairGenerator objects were not created", kpg);
        KeyPair kp, kp1;
        SecureRandom rr = new SecureRandom();
        for (int i = 0; i < kpg.length; i++) {
            kpg[i].initialize(512, rr);
            kp = kpg[i].generateKeyPair();
            kp1 = kpg[i].genKeyPair();
            assertFalse("Incorrect private key", kp.getPrivate().equals(
                    kp1.getPrivate()));
            assertFalse("Incorrect public key", kp.getPublic().equals(
                    kp1.getPublic()));
        }
    }
    
    /**
     * Test for <code>generateKeyPair()</code> and <code>genKeyPair()</code>
     * methods
     * Assertion: these methods are used without previously initialization
     */
    public void testGenKeyPair02() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator[] kpg = createKPGen();
        assertNotNull("KeyPairGenerator objects were not created", kpg);
        KeyPair kp, kp1;   
        for (int i = 0; i < kpg.length; i++) {
            kp = kpg[i].generateKeyPair();
            kp1 = kpg[i].genKeyPair();
            assertFalse("Incorrect private key", kp.getPrivate().equals(
                kp1.getPrivate()));
            assertFalse("Incorrect public key", kp.getPublic().equals(
                kp1.getPublic()));
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(KeyPairGenerator3Test.class);
    }
    
}

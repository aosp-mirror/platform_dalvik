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

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.security.Security;

import junit.framework.TestCase;
@TestTargetClass(SecureRandom.class)
public class SecureRandom2Test extends TestCase {

    private static final byte[] SEED_BYTES = { (byte) 33, (byte) 15, (byte) -3,
            (byte) 22, (byte) 77, (byte) -16, (byte) -33, (byte) 56 };

    private static final int SEED_SIZE = 539;

    private static final long SEED_VALUE = 5335486759L;

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void testGetProvider() {
        SecureRandom sr1 = new SecureRandom();
        assertNotNull(sr1.getProvider());

        SecureRandom sr2 = new SecureRandom(SEED_BYTES);
        assertNotNull(sr2.getProvider());

        MyProvider p = new MyProvider();

        MySecureRandom sr3 = new MySecureRandom(new MySecureRandomSpi(), p);
        assertEquals(p, sr3.getProvider());
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            assertNotNull(random.getProvider());
        } catch (NoSuchAlgorithmException e) {
            fail("Unexpected NoSuchAlgorithmException");
        }
    }

    /**
     * @tests java.security.SecureRandom#SecureRandom()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.security.SecureRandom()
        try {
            new SecureRandom();
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }
    }

    /**
     * @tests java.security.SecureRandom#SecureRandom(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {byte[].class}
    )
    public void test_Constructor$B() {
        // Test for method java.security.SecureRandom(byte [])
        try {
            new SecureRandom(SEED_BYTES);
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }
        
        try {
            new SecureRandom(null);
            fail("NullPointerException was not thrown for NULL parameter");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * @tests java.security.SecureRandom#SecureRandom(java.security.SecureRandomSpi,
     *        java.security.Provider)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {java.security.SecureRandomSpi.class, java.security.Provider.class}
    )
    public void test_ConstructorLjava_security_SecureRandomSpi_java_security_Provider() {
        try {
            new MySecureRandom(null, null);
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }

        try {
            MyProvider p = new MyProvider();
            MySecureRandom sr = new MySecureRandom(new MySecureRandomSpi(), p);
            assertEquals("unknown", sr.getAlgorithm());
            assertEquals(p, sr.getProvider());
            
            sr = new MySecureRandom(new MySecureRandomSpi(), null);
            sr = new MySecureRandom(null, p);
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }

    }

    /**
     * @tests java.security.SecureRandom#generateSeed(int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "generateSeed",
        args = {int.class}
    )
    public void test_generateSeedI() {
        // Test for method byte [] java.security.SecureRandom.generateSeed(int)
        byte[] seed = new SecureRandom().generateSeed(SEED_SIZE);
        assertEquals("seed has incorrect size", SEED_SIZE, seed.length);
        
        try {
            new SecureRandom().generateSeed(-42);
            fail("expected an exception");
        } catch (Exception e) {
            // ok
        }
        
    }
    /**
     * @tests java.security.SecureRandom#getInstance(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() {
        // Test for method java.security.SecureRandom
        // java.security.SecureRandom.getInstance(java.lang.String)
        try {
            SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find a SHA1PRNG algorithm");
        }
        
        try {
            SecureRandom.getInstance("MD2");
            fail("NoSuchAlgorithmException should be thrown for MD2 algorithm");
        } catch (NoSuchAlgorithmException e) {
            //expected
        }
    }

    /**
     * @tests java.security.SecureRandom#getInstance(java.lang.String,
     *        java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException, NoSuchProviderException, IllegalArgumentException checking missed",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() {
        // Test for method java.security.SecureRandom
        // java.security.SecureRandom.getInstance(java.lang.String,
        // java.lang.String)
        try {
            Provider[] providers = Security
                    .getProviders("SecureRandom.SHA1PRNG");
            if (providers != null) {
                for (int i = 0; i < providers.length; i++) {
                    SecureRandom
                            .getInstance("SHA1PRNG", providers[i].getName());
                }// end for
            } else {
                fail("No providers support SHA1PRNG");
            }
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find a SHA1PRNG algorithm");
        } catch (NoSuchProviderException e) {
            fail("getInstance did not find the provider for SHA1PRNG");
        }
    }

    /**
     * @tests java.security.SecureRandom#getSeed(int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification of negative parameter missed",
        method = "getSeed",
        args = {int.class}
    )
    public void test_getSeedI() {
        // Test for method byte [] java.security.SecureRandom.getSeed(int)
        byte[] seed = SecureRandom.getSeed(SEED_SIZE);
        assertEquals("seed has incorrect size", SEED_SIZE, seed.length);
        
        try {
            new SecureRandom().getSeed(-42);
            fail("expected an exception");
        } catch (Exception e) {
            // ok
        }
    }

    /**
     * @tests java.security.SecureRandom#nextBytes(byte[])
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Null array checking missed",
        method = "nextBytes",
        args = {byte[].class}
    )
    public void test_nextBytes$B() {
        // Test for method void java.security.SecureRandom.nextBytes(byte [])
        byte[] bytes = new byte[313];
        try {
            new SecureRandom().nextBytes(bytes);
        } catch (Exception e) {
            fail("next bytes not ok : " + e);
        }
        
        try {
            new SecureRandom().nextBytes(null);
            fail("expected exception");
        } catch (Exception e) {
            // ok
        }
    }

    /**
     * @tests java.security.SecureRandom#setSeed(byte[])
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Null array checking missed",
        method = "setSeed",
        args = {byte[].class}
    )
    public void test_setSeed$B() {
        // Test for method void java.security.SecureRandom.setSeed(byte [])
        try {
            new SecureRandom().setSeed(SEED_BYTES);
        } catch (Exception e) {
            fail("seed generation with bytes failed : " + e);
        }
        
        try {
            new SecureRandom().setSeed(null);
            fail("expected exception");
        } catch (Exception e) {
            // ok
        }
    }

    /**
     * @tests java.security.SecureRandom#setSeed(long)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setSeed",
        args = {long.class}
    )
    public void test_setSeedJ() {
        // Test for method void java.security.SecureRandom.setSeed(long)
        try {
            new SecureRandom().setSeed(SEED_VALUE);
        } catch (Exception e) {
            fail("seed generation with long failed : " + e);
        }
        
        try {
            new SecureRandom().setSeed(-1);
        } catch (Exception e) {
            fail("unexpected exception: " + e);
        }
    }

    /**
     * @tests java.security.SecureRandom#getAlgorithm()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() {
        // Regression for HARMONY-750

        SecureRandomSpi spi = new SecureRandomSpi() {

            protected void engineSetSeed(byte[] arg) {
            }

            protected void engineNextBytes(byte[] arg) {
            }

            protected byte[] engineGenerateSeed(int arg) {
                return null;
            }
        };

        SecureRandom sr = new SecureRandom(spi, null) {
        };

        assertEquals("unknown", sr.getAlgorithm());
        
        
    }

    // Regression Test for HARMONY-3552.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {int.class}
    )
    public void test_nextJ() throws Exception {
        MySecureRandom mySecureRandom = new MySecureRandom(
                new MySecureRandomSpi(), null);
        int numBits = 29;
        int random = mySecureRandom.getNext(numBits);
        assertEquals(numBits, Integer.bitCount(random));

        numBits = 0;
        random = mySecureRandom.getNext(numBits);
        assertEquals(numBits, Integer.bitCount(random));

        numBits = 40;
        random = mySecureRandom.getNext(numBits);
        assertEquals(32, Integer.bitCount(random));

        numBits = -1;
        random = mySecureRandom.getNext(numBits);
        assertEquals(0, Integer.bitCount(random));
        
        
    }

    class MySecureRandom extends SecureRandom {
        private static final long serialVersionUID = 1L;

        public MySecureRandom(SecureRandomSpi secureRandomSpi, Provider provider) {
            super(secureRandomSpi, provider);
        }

        public int getNext(int numBits) {
            return super.next(numBits);
        }
    }

    class MySecureRandomSpi extends SecureRandomSpi {
        private static final long serialVersionUID = 1L;

        @Override
        protected byte[] engineGenerateSeed(int arg0) {
            return null;
        }

        @Override
        protected void engineNextBytes(byte[] bytes) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) 0xFF;
            }
        }

        @Override
        protected void engineSetSeed(byte[] arg0) {
            return;
        }
    }

    class MyProvider extends Provider {

        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
            put("MessageDigest.abc", "SomeClassName");
            put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
        }

        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }

        public void putService(Provider.Service s) {
            super.putService(s);
        }

        public void removeService(Provider.Service s) {
            super.removeService(s);
        }

    }
}

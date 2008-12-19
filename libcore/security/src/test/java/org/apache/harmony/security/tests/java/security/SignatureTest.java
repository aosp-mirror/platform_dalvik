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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;

import java.security.Signature;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;

import org.apache.harmony.security.tests.support.MySignature1;

import junit.framework.TestCase;
@TestTargetClass(Signature.class)
/**
 * Tests for <code>Signature</code> constructor and methods
 * 
 */
public class SignatureTest extends TestCase {

    /*
     * Class under test for Signature(String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "Signature",
          methodArgs = {String.class}
        )
    })
    public void testConstructor() {
        String [] algorithms = { "SHA256WITHRSA", "NONEWITHDSA", "SHA384WITHRSA",
            "MD2WITHRSA", "MD5ANDSHA1WITHRSA", "SHA512WITHRSA",
            "SHA1WITHRSA", "SHA1WITHDSA", "MD5WITHRSA" };
        for (int i = 0; i < algorithms.length; i ++) {
            MySignature1 s = new MySignature1(algorithms[i]);
            assertEquals(algorithms[i],s.getAlgorithm());
            assertNull(s.getProvider());
            assertEquals(0, s.getState());
        }
        
        MySignature1 s1 = new MySignature1(null);
        assertNull(s1.getAlgorithm());
        assertNull(s1.getProvider());
        assertEquals(0, s1.getState());
    
        MySignature1 s2 = new MySignature1("ABCD@#&^%$)(*&");
        assertEquals("ABCD@#&^%$)(*&", s2.getAlgorithm());
        assertNull(s2.getProvider());
        assertEquals(0, s2.getState());
    }
    
    /*
     * Class under test for Object clone()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Just exception case was tested",
      targets = {
        @TestTarget(
          methodName = "clone",
          methodArgs = {}
        )
    })
    public void testClone() {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.clone();
            fail("No expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {    
        }    
    }

    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getProvider",
          methodArgs = {}
        )
    })
    public void testGetProvider() {
        MySignature1 s = new MySignature1("ABC");
        
        assertEquals("state", MySignature1.UNINITIALIZED, s.getState());
        assertNull("provider", s.getProvider());
    }

    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getAlgorithm",
          methodArgs = {}
        )
    })
    public void testGetAlgorithm() {
        MySignature1 s = new MySignature1("ABC");

        assertEquals("state", MySignature1.UNINITIALIZED, s.getState());
        assertEquals("algorithm", "ABC", s.getAlgorithm());
    }

    /*
     * Class under test for void initVerify(PublicKey)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "InvalidKeyException checking missed",
      targets = {
        @TestTarget(
          methodName = "initVerify",
          methodArgs = {PublicKey.class}
        )
    })
    public void testInitVerifyPublicKey() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initVerify(new MyPublicKey());
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("initVerify() failed", s.runEngineInitVerify);
    }

    /*
     * Class under test for void initVerify(Certificate)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "InvalidKeyException checking missed",
      targets = {
        @TestTarget(
          methodName = "initVerify",
          methodArgs = {java.security.cert.Certificate.class}
        )
    })
    public void testInitVerifyCertificate() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initVerify(new MyCertificate());
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("initVerify() failed", s.runEngineInitVerify);
    }

    /*
     * Class under test for void initSign(PrivateKey)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "InvalidKeyException checking missed",
      targets = {
        @TestTarget(
          methodName = "initSign",
          methodArgs = {PrivateKey.class}
        )
    })
    public void testInitSignPrivateKey() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initSign(new MyPrivateKey());
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("initSign() failed", s.runEngineInitSign);
    }

    /*
     * Class under test for void initSign(PrivateKey, SecureRandom)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "InvalidKeyException checking missed",
      targets = {
        @TestTarget(
          methodName = "initSign",
          methodArgs = {PrivateKey.class, SecureRandom.class}
        )
    })
    public void testInitSignPrivateKeySecureRandom() throws InvalidKeyException {
        MySignature1 s = new MySignature1("ABC");

        s.initSign(new MyPrivateKey(), new SecureRandom());
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("initSign() failed", s.runEngineInitSign);
    }

    /*
     * Class under test for byte[] sign()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification of returned value missed",
      targets = {
        @TestTarget(
          methodName = "sign",
          methodArgs = {}
        )
    })
    public void testSign() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.sign();
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initVerify(new MyPublicKey());
        
        try {
            s.sign();
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }
        
        s.initSign(new MyPrivateKey());
        s.sign();
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("sign() failed", s.runEngineSign);
    }

    /*
     * Class under test for sign(byte[], offset, len)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification with different values of offset and len missed",
      targets = {
        @TestTarget(
          methodName = "sign",
          methodArgs = {byte[].class, int.class, int.class}
        )
    })
    public void testSignbyteintint() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] outbuf = new byte [10];
        try {
            s.sign(outbuf, 0, outbuf.length);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initVerify(new MyPublicKey());
        
        try {
            s.sign(outbuf, 0, outbuf.length);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }
        
        s.initSign(new MyPrivateKey());
        assertEquals(s.getBufferLength(), s.sign(outbuf, 0, outbuf.length));
        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("sign() failed", s.runEngineSign);
    }

    
    /*
     * Class under test for boolean verify(byte[])
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification of returned value missed",
      targets = {
        @TestTarget(
          methodName = "verify",
          methodArgs = {byte[].class}
        )
    })
    public void testVerifybyteArray() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.verify(b);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initSign(new MyPrivateKey());
        try {
            s.verify(b);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }
        
        s.initVerify(new MyPublicKey());
        s.verify(b);
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("verify() failed", s.runEngineVerify);
    }

    /*
     * Class under test for boolean verify(byte[], int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification of returned value missed. " +
                  "Verification of different parameters offset and length missed.",
      targets = {
        @TestTarget(
          methodName = "verify",
          methodArgs = {byte[].class, int.class, int.class}
        )
    })
    public void testVerifybyteArrayintint() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.verify(b, 0, 3);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initSign(new MyPrivateKey());

        try {
            s.verify(b, 0, 3);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }
        
        s.initVerify(new MyPublicKey());
        
        try {
            s.verify(b, 0, 5);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {        
        }
        
        s.verify(b, 0, 3);
        assertEquals("state", MySignature1.VERIFY, s.getState());
        assertTrue("verify() failed", s.runEngineVerify);
    }

    /*
     * Class under test for void update(byte)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Boundary testing missed. SignatureException checking missed.",
      targets = {
        @TestTarget(
          methodName = "update",
          methodArgs = {byte.class}
        )
    })
    public void testUpdatebyte() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.update((byte)1);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initVerify(new MyPublicKey());
        s.update((byte) 1);
        s.initSign(new MyPrivateKey());
        s.update((byte) 1);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate1);
    }

    /*
     * Class under test for void update(byte[])
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Null array/exception checking missed.",
      targets = {
        @TestTarget(
          methodName = "update",
          methodArgs = {byte[].class}
        )
    })
    public void testUpdatebyteArray() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.update(b);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initVerify(new MyPublicKey());
        s.update(b);
        s.initSign(new MyPrivateKey());
        s.update(b);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate2);
    }

    /*
     * Class under test for void update(byte[], int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification of different values off and len missed",
      targets = {
        @TestTarget(
          methodName = "update",
          methodArgs = {byte[].class, int.class, int.class}
        )
    })
    public void testUpdatebyteArrayintint() throws Exception {
        MySignature1 s = new MySignature1("ABC");
        byte[] b = {1, 2, 3, 4};
        try {
            s.update(b, 0, 3);
            fail("No expected SignatureException");
        } catch (SignatureException e) {        
        }

        s.initVerify(new MyPublicKey());
        s.update(b, 0, 3);
        s.initSign(new MyPrivateKey());
        s.update(b, 0, 3);

        assertEquals("state", MySignature1.SIGN, s.getState());
        assertTrue("update() failed", s.runEngineUpdate2);
    }

    /*
     * Class under test for void setParameter(String, Object)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "InvalidParameterException checking missed",
      targets = {
        @TestTarget(
          methodName = "setParameter",
          methodArgs = {String.class, Object.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void testSetParameterStringObject() {
        MySignature1 s = new MySignature1("ABC");
        s.setParameter("aaa", new Object());
    }

    /*
     * Class under test for void setParameter(AlgorithmParameterSpec)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification with valid parameter missed",
      targets = {
        @TestTarget(
          methodName = "setParameter",
          methodArgs = {java.security.spec.AlgorithmParameterSpec.class}
        )
    })
    public void testSetParameterAlgorithmParameterSpec() throws InvalidAlgorithmParameterException {
        MySignature1 s = new MySignature1("ABC");
        try {
            s.setParameter((java.security.spec.AlgorithmParameterSpec)null);
            fail("No expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e){    
        }
    }
    @SuppressWarnings("deprecation")
    public void testGetParameter() {
        MySignature1 s = new MySignature1("ABC");
        s.getParameter("aaa");
    }
    
    private class MyKey implements Key {
        public String getFormat() {
            return "123";
        }
        public byte[] getEncoded() {
            return null;
        }
        public String getAlgorithm() {
            return "aaa";
        }        
    }
    
    private class MyPublicKey extends MyKey implements PublicKey {}

    private class MyPrivateKey extends MyKey implements PrivateKey {}
    
    private class MyCertificate extends java.security.cert.Certificate {    
        public  MyCertificate() {
            super("MyCertificateType");
        }
        
        public PublicKey getPublicKey() {
            return new MyPublicKey();
        }
        
        public byte[] getEncoded() {
            return null;
        }
        public void verify(PublicKey key) {}
        
        public void verify(PublicKey key, String sigProvider) {}
        
        public String toString() {
            return "MyCertificate";
        }
    }
}

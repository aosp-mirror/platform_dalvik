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

package tests.security.cert;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.ObjectStreamException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import org.apache.harmony.security.tests.support.cert.MyCertificate;

/**
 * Tests for <code>Certificate</code> fields and methods
 * 
 */
@TestTargetClass(Certificate.class)
public class CertificateTest extends TestCase {
    /**
     * Meaningless cert encoding just for testing purposes
     */
    private static final byte[] testEncoding = new byte[] { (byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5 };

    /**
     * Constructor for CertificateTest.
     * @param name
     */
    public CertificateTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test for <code>Certificate(String type)</code> method<br>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Null parameter checking missed",
      targets = {
        @TestTarget(
          methodName = "Certificate",
          methodArgs = {java.lang.String.class}
        )
    })
    public final void testCertificate() {
        try {
            Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
            assertTrue(Arrays.equals(testEncoding, c1.getEncoded()));
            assertEquals("TEST", c1.getPublicKey().getAlgorithm());
            assertTrue(Arrays.equals(
                    new byte[] { (byte) 1, (byte) 2, (byte) 3 }, c1
                            .getPublicKey().getEncoded()));
            assertEquals("TEST_FORMAT", c1.getPublicKey().getFormat());
            assertEquals("TEST_TYPE", c1.getType());
        } catch (CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException " + e.getMessage());
        }
    }

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: returns hash of the <code>Certificate</code> instance
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify different objects.",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public final void testHashCode() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        Certificate c2 = new MyCertificate("TEST_TYPE", testEncoding);

        assertTrue(c1.hashCode() == c2.hashCode());
    }

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: hash code of equal objects should be the same
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify different objects (the same as previous test).",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public final void testHashCodeEqualsObject() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        Certificate c2 = new MyCertificate("TEST_TYPE", testEncoding);

        assertTrue((c1.hashCode() == c2.hashCode()) && c1.equals(c2));
    }


    /**
     * Test for <code>getType()</code> method<br>
     * Assertion: returns this certificate type 
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getType",
          methodArgs = {}
        )
    })
    public final void testGetType() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertEquals("TEST_TYPE", c1.getType());
    }

    /**
     * Test #1 for <code>equals(Object)</code> method<br>
     * Assertion: object equals to itself 
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies positive case.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject01() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertTrue(c1.equals(c1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object equals to other <code>Certificate</code>
     * instance with the same state
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies positive case.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
      )
    })    
    public final void testEqualsObject02() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        Certificate c2 = new MyCertificate("TEST_TYPE", testEncoding);
        assertTrue(c1.equals(c2) && c2.equals(c1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to <code>null</code>
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies equals method with null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject03() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertFalse(c1.equals(null));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to other which is not
     * instance of <code>Certificate</code>
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies negative case.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject04() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertFalse(c1.equals("TEST_TYPE"));
    }

    //
    // the following tests just call methods
    // that are abstract in <code>Certificate</code>
    // (So they just like signature tests)
    //

    /**
     * This test just calls <code>getEncoded()</code> method<br>
     * @throws CertificateEncodingException
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify CertificateEncodingException.",
      targets = {
        @TestTarget(
          methodName = "getEncoded",
          methodArgs = {}
        )
    })
    public final void testGetEncoded() throws CertificateEncodingException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.getEncoded();
    }

    /**
     * This test just calls <code>verify(PublicKey)</code> method<br>
     * 
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies only null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "verify",
          methodArgs = {java.security.PublicKey.class}
        )
    })
    public final void testVerifyPublicKey()
        throws InvalidKeyException,
               CertificateException,
               NoSuchAlgorithmException,
               NoSuchProviderException,
               SignatureException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.verify(null);
    }

    /**
     * This test just calls <code>verify(PublicKey,String)</code> method<br>
     *
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies only null as parameters.",
      targets = {
        @TestTarget(
          methodName = "verify",
          methodArgs = {java.security.PublicKey.class, java.lang.String.class}
        )
    })
    public final void testVerifyPublicKeyString()
        throws InvalidKeyException,
               CertificateException,
               NoSuchAlgorithmException,
               NoSuchProviderException,
               SignatureException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.verify(null, null);
    }

    /**
     * This test just calls <code>toString()</code> method<br>
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "toString",
          methodArgs = {}
        )
    })
    public final void testToString() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.toString();
    }

    /**
     * This test just calls <code>testGetPublicKey()</code> method<br>
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getPublicKey",
          methodArgs = {}
        )
    })
    public final void testGetPublicKey() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.getPublicKey();
    }

    /**
     * This test just calls <code>writeReplace()</code> method<br>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "DOesn't verify ObjectStreamException.",
      targets = {
        @TestTarget(
          methodName = "writeReplace",
          methodArgs = {}
        )
    })
    public final void testWriteReplace() {
        MyCertificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        
        try {
            Object obj = c1.writeReplace();
            assertTrue(obj.toString().contains(
                    "java.security.cert.Certificate$CertificateRep"));
        } catch (ObjectStreamException e) {
            fail("Unexpected ObjectStreamException " + e.getMessage());
        }
    }

}

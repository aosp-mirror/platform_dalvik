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
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;

import org.apache.harmony.security.tests.support.cert.MyCertPath;

/**
 * Tests for <code>CertPath</code> fields and methods
 * 
 */
@TestTargetClass(CertPath.class)
public class CertPathTest extends TestCase {
    /**
     * Meaningless cert path encoding just for testing purposes
     */
    private static final byte[] testEncoding = new byte[] {
            (byte)1, (byte)2, (byte)3, (byte)4, (byte)5
    };

    /**
     * Constructor for CertPathTest.
     * @param name
     */
    public CertPathTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test for <code>CertPath(String type)</code> method<br>
     * Assertion: returns hash of the <code>Certificate</code> instance
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "String/null parameters checking missed",
      targets = {
        @TestTarget(
          methodName = "CertPath",
          methodArgs = {java.lang.String.class}
        )
    })
    public final void testCertPath() {
        try {
            CertPath cp1 = new MyCertPath(testEncoding);
            assertEquals("MyEncoding", cp1.getType());
            assertTrue(Arrays.equals(testEncoding, cp1.getEncoded()));
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
      purpose = "Doesn't verify hash codes of non equal objects.",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public final void testHashCode() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);

        assertTrue(cp1.hashCode() == cp2.hashCode());
    }

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: hash code of equal objects should be the same
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify hash codes of non equal objects.",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public final void testHashCodeEqualsObject() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        assertTrue((cp1.hashCode() == cp2.hashCode()) && cp1.equals(cp2));
    }

    /**
     * Test for <code>getType()</code> method<br>
     * Assertion: returns cert path type
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
        assertEquals("MyEncoding", new MyCertPath(testEncoding).getType());
    }

    /**
     * Test #1 for <code>equals(Object)</code> method<br>
     * Assertion: object equals to itself 
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that object equals to itself.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject01() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertTrue(cp1.equals(cp1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object equals to other <code>CertPath</code>
     * instance with the same state
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that CertPath object equals to other CertPath " +
            "with the same state.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject02() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        assertTrue(cp1.equals(cp2) && cp2.equals(cp1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to <code>null</code>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject03() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertFalse(cp1.equals(null));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to other which is not
     * instance of <code>CertPath</code>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies non equal objects.",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public final void testEqualsObject04() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertFalse(cp1.equals("MyEncoding"));
    }

    /**
     * Test for <code>toString()</code> method<br>
     * Assertion: returns string representation of
     * <code>CertPath</code> object
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
        CertPath cp1 = new MyCertPath(testEncoding);
        assertNotNull(cp1.toString());
    }

    //
    // the following tests just call methods
    // that are abstract in <code>CertPath</code>
    // (So they just like signature tests)
    //

    /**
     * This test just calls <code>getCertificates()</code> method<br>
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "getCertificates",
          methodArgs = {}
        )
    })
    public final void testGetCertificates() {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getCertificates();
    }

    /**
     * This test just calls <code>getEncoded()</code> method<br>
     *
     * @throws CertificateEncodingException
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "getEncoded",
          methodArgs = {}
        )
    })
    public final void testGetEncoded() throws CertificateEncodingException {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncoded();
    }

    /**
     * This test just calls <code>getEncoded(String)</code> method<br>
     *
     * @throws CertificateEncodingException
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "getEncoded",
          methodArgs = {java.lang.String.class}
        )
    })
    public final void testGetEncodedString() throws CertificateEncodingException {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncoded("MyEncoding");
    }

    /**
     * This test just calls <code>getEncodings()</code> method<br>
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "getEncodings",
          methodArgs = {}
        )
    })
    public final void testGetEncodings() {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncodings();
    }

    /**
     * This test just calls <code>writeReplace()</code> method<br>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify ObjectStreamException.",
      targets = {
        @TestTarget(
          methodName = "writeReplace",
          methodArgs = {}
        )
    })
    public final void testWriteReplace() {
        try {
            MyCertPath cp1 = new MyCertPath(testEncoding);
            Object obj = cp1.writeReplace();
            assertTrue(obj.toString().contains(
                    "java.security.cert.CertPath$CertPathRep"));
        } catch (ObjectStreamException e) {
            fail("Unexpected ObjectStreamException " + e.getMessage());
        }
    }
}

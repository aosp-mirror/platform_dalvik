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
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;

import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Date;

import org.apache.harmony.security.tests.support.TestCertUtils;


@TestTargetClass(CodeSource.class)
public class CodeSource2Test extends junit.framework.TestCase {

    /**
     * @throws Exception
     * @tests java.security.CodeSource#CodeSource(java.net.URL,
     *        java.security.cert.Certificate[])
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies constructor with valid URL and null certificate array",
      targets = {
        @TestTarget(
          methodName = "CodeSource",
          methodArgs = {URL.class, Certificate[].class}
        )
    })
    public void test_ConstructorLjava_net_URL$Ljava_security_cert_Certificate()
            throws Exception {
        // Test for method java.security.CodeSource(java.net.URL,
        // java.security.cert.Certificate [])
        new CodeSource(new java.net.URL("file:///test"), (Certificate[]) null);
    }

    /**
     * @throws Exception
     * @tests java.security.CodeSource#CodeSource(java.net.URL,
     *        java.security.CodeSigner[])
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "CodeSource",
          methodArgs = {URL.class, CodeSigner[].class}
        )
    })
    public void test_ConstructorLjava_net_URL$Ljava_security_CodeSigner() {
        // Test for method java.security.CodeSource(java.net.URL,
        // java.security.cert.CodeSigner [])
        try {
            new CodeSource(new URL("file:///test"), (CodeSigner[]) null);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }

        try {
            new CodeSource(null, (CodeSigner[]) null);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }

        CertPath cpath = TestCertUtils.genCertPath(3, 0);
        Date now = new Date();

        Timestamp ts = new Timestamp(now, cpath);
        CodeSigner cs = new CodeSigner(cpath, ts);
        try {
            CodeSource codeSource = new CodeSource(new URL("file:///test"), new CodeSigner[] { cs });
            assertNotNull(codeSource.getCertificates());
            assertNotNull(codeSource.getCodeSigners());
            assertTrue(Arrays.equals(new CodeSigner[] { cs }, codeSource.getCodeSigners()));
            assertEquals(new URL("file:///test"), codeSource.getLocation());
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }

    /**
     * @tests java.security.CodeSource#equals(java.lang.Object)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "CodeSource object was created with CodeSource(URL url, Certificate[] certs) only",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {Object.class}
        )
    })
    public void test_equalsLjava_lang_Object() throws Exception {
        // Test for method boolean
        // java.security.CodeSource.equals(java.lang.Object)
        CodeSource cs1 = new CodeSource(new java.net.URL("file:///test"),
                (Certificate[]) null);
        CodeSource cs2 = new CodeSource(new java.net.URL("file:///test"),
                (Certificate[]) null);
        assertTrue("Identical objects were not equal()!", cs1.equals(cs2));
    }

    /**
     * @tests java.security.CodeSource#hashCode()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public void test_hashCode() throws Exception {
        URL url = new java.net.URL("file:///test");
        CodeSource cs = new CodeSource(url, (Certificate[]) null);
        assertTrue("Did not get expected hashCode!", cs.hashCode() == url
                .hashCode());
    }

    /**
     * @tests java.security.CodeSource#getCertificates()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies case for null certificates only",
      targets = {
        @TestTarget(
          methodName = "getCertificates",
          methodArgs = {}
        )
    })
    public void test_getCertificates() throws Exception {
        CodeSource cs = new CodeSource(new java.net.URL("file:///test"),
                (Certificate[]) null);
        assertNull("Should have gotten null certificate list.", cs
                .getCertificates());
    }

    /**
     * @tests java.security.CodeSource#getLocation()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getLocation",
          methodArgs = {}
        )
    })
    public void test_getLocation() throws Exception {
        // Test for method java.net.URL java.security.CodeSource.getLocation()
        CodeSource cs = new CodeSource(new java.net.URL("file:///test"),
                (Certificate[]) null);
        assertEquals("Did not get expected location!", "file:/test", cs
                .getLocation().toString());
    }

    /**
     * @tests java.security.CodeSource#implies(java.security.CodeSource)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "implies",
          methodArgs = {CodeSource.class}
        )
    })
    public void test_impliesLjava_security_CodeSource() throws Exception {
        // Test for method boolean
        // java.security.CodeSource.implies(java.security.CodeSource)
        CodeSource cs1 = new CodeSource(new URL("file:/d:/somedir"),
                (Certificate[]) null);
        CodeSource cs2 = new CodeSource(new URL("file:/d:/somedir/"),
                (Certificate[]) null);
        assertTrue("Does not add /", cs1.implies(cs2));

        cs1 = new CodeSource(new URL("file", null, -1, "/d:/somedir/"),
                (Certificate[]) null);
        cs2 = new CodeSource(new URL("file:/d:/somedir/"), (Certificate[]) null);
        assertTrue("null host should imply host", cs1.implies(cs2));
        assertTrue("host should not imply null host", !cs2.implies(cs1));
    }

}
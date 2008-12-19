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

import org.apache.harmony.security.tests.support.spec.MyEncodedKeySpec;

import java.security.spec.EncodedKeySpec;
import java.util.Arrays;

/**
 * Tests for <code>EncodedKeySpec</code> class fields and methods.
 * 
 */
@TestTargetClass(EncodedKeySpec.class)
public class EncodedKeySpecTest extends TestCase {

    /**
     * Constructor for EncodedKeySpecTest.
     * 
     * @param name
     */
    public EncodedKeySpecTest(String name) {
        super(name);
    }

    /**
     * Tests for constructor <code>EncodedKeySpec(byte[])</code><br>
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Null parameter checking missed",
      targets = {
        @TestTarget(
          methodName = "EncodedKeySpec",
          methodArgs = {byte[].class}
        )
    })
    public final void testEncodedKeySpec() {
        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec eks = new MyEncodedKeySpec(encodedKey);

        assertTrue("wrong encoded key was returned", Arrays.equals(encodedKey,
                eks.getEncoded()));
        assertEquals("wrong name of encoding format", "My", eks.getFormat());
    }

    /**
     * Tests that <code>getEncoded()</code> method returns valid byte array
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getEncoded",
          methodArgs = {}
        )
    })
    public final void testGetEncoded() {

        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec meks = new MyEncodedKeySpec(encodedKey);

        /* Get encoded key */
        byte[] ek = meks.getEncoded();

        /* Check returned array */
        boolean result = true;
        for (int i = 0; i < encodedKey.length; i++) {
            if (encodedKey[i] != ek[i]) {
                /* indicate failure */
                result = false;
            }
        }
        /* passed */
        assertTrue(result);
    }

    /**
     * Tests that internal state of the object can not be modified by modifying
     * initial array value
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that internal state of the object can not be modified " +
            "by modifying initial array value.",
      targets = {
        @TestTarget(
          methodName = "EncodedKeySpec",
          methodArgs = {byte[].class}
        ),
        @TestTarget(
          methodName = "getEncoded",
          methodArgs = {}
        )
    })
    public final void testIsStatePreserved1() {
        /* Create initial byte array */
        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };

        EncodedKeySpec meks = new MyEncodedKeySpec(encodedKey);

        /* Modify initial array's value */
        encodedKey[3] = (byte) 5;

        /* Get encoded key */
        byte[] ek = meks.getEncoded();

        /* Check that byte value has not been changed */
        assertTrue(ek[3] == (byte) 4);
    }

    /**
     * Tests that internal state of the object can not be modified using
     * returned value of <code>getEncoded()</code> method
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that internal state of the object can not be modified " +
            "by modifying initial array value.",
            targets = {
              @TestTarget(
                methodName = "EncodedKeySpec",
                methodArgs = {byte[].class}
              ),
              @TestTarget(
                methodName = "getEncoded",
                methodArgs = {}
              )
    })
    public final void testIsStatePreserved2() {

        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec meks = new MyEncodedKeySpec(encodedKey);

        /* Get encoded key */
        byte[] ek = meks.getEncoded();
        /* Modify returned value */
        ek[3] = (byte) 5;
        /* Get encoded key again */
        byte[] ek1 = meks.getEncoded();

        /* Check that byte value has not been changed */
        assertTrue(ek1[3] == (byte) 4);
    }

}

/* Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.MissingFormatWidthException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(MissingFormatWidthException.class) 
public class MissingFormatWidthExceptionTest extends TestCase {

    /**
     * @tests java.util.MissingFormatWidthException#MissingFormatWidthException(String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies NullPointerException.",
      targets = {
        @TestTarget(
          methodName = "MissingFormatWidthException",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_missingFormatWidthException() {
        try {
            new MissingFormatWidthException(null);
            fail("should throw NullPointerExcepiton");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.MissingFormatWidthException#getFormatSpecifier()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getFormatSpecifier",
          methodArgs = {}
        )
    })
    public void test_getFormatSpecifier() {
        String s = "MYTESTSTRING";
        MissingFormatWidthException missingFormatWidthException = new MissingFormatWidthException(
                s);
        assertEquals(s, missingFormatWidthException.getFormatSpecifier());

    }

    /**
     * @tests java.util.MissingFormatWidthException#getMessage()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getMessage",
          methodArgs = {}
        )
    })
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        MissingFormatWidthException missingFormatWidthException = new MissingFormatWidthException(
                s);
        assertTrue(null != missingFormatWidthException.getMessage());

    }

    // comparator for comparing MissingFormatWidthException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            MissingFormatWidthException initEx = (MissingFormatWidthException) initial;
            MissingFormatWidthException desrEx = (MissingFormatWidthException) deserialized;

            assertEquals("FormatSpecifier", initEx.getFormatSpecifier(), desrEx
                    .getFormatSpecifier());
        }
    };

    /**
     * @tests serialization/deserialization.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "!SerializationSelf",
          methodArgs = {}
        )
    })
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new MissingFormatWidthException(
                "MYTESTSTRING"), exComparator);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "!SerializationGolden",
          methodArgs = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new MissingFormatWidthException(
                "MYTESTSTRING"), exComparator);
    }
}

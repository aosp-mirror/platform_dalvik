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
import java.util.UnknownFormatConversionException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(UnknownFormatConversionException.class) 
public class UnknownFormatConversionExceptionTest extends TestCase {

    /**
     * @tests java.util.UnknownFormatConversionException#UnknownFormatConversionException(String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "UnknownFormatConversionException",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_unknownFormatConversionException() {

        // RI 5.0 will not throw NullPointerException, it is the bug according
        // to spec.
        try {
            new UnknownFormatConversionException(null);
        } catch (NullPointerException e) {
            fail("should not throw NullPointerExcepiton");
        }
    }

    /**
     * @tests java.util.UnknownFormatConversionException#getConversion()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getConversion",
          methodArgs = {}
        )
    })
    public void test_getConversion() {
        String s = "MYTESTSTRING";
        UnknownFormatConversionException UnknownFormatConversionException = new UnknownFormatConversionException(
                s);
        assertEquals(s, UnknownFormatConversionException.getConversion());
    }

    /**
     * @tests java.util.UnknownFormatConversionException#getMessage()
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
        UnknownFormatConversionException UnknownFormatConversionException = new UnknownFormatConversionException(
                s);
        assertTrue(null != UnknownFormatConversionException.getMessage());
    }

    // comparator for comparing UnknownFormatConversionException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            UnknownFormatConversionException initEx = (UnknownFormatConversionException) initial;
            UnknownFormatConversionException desrEx = (UnknownFormatConversionException) deserialized;

            assertEquals("Conversion", initEx.getConversion(), desrEx
                    .getConversion());
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

        SerializationTest.verifySelf(new UnknownFormatConversionException(
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

        SerializationTest.verifyGolden(this,
                new UnknownFormatConversionException("MYTESTSTRING"),
                exComparator);
    }
}

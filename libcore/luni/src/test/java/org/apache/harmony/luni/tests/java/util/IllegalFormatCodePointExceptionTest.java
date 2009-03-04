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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.IllegalFormatCodePointException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(IllegalFormatCodePointException.class) 
public class IllegalFormatCodePointExceptionTest extends TestCase {

    /**
     * @tests java.util.IllegalFormatCodePointException.IllegalFormatCodePointException(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatCodePointException",
        args = {int.class}
    )
    public void test_illegalFormatCodePointException() {
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                -1);
        assertTrue(null != illegalFormatCodePointException);
    }

    /**
     * @tests java.util.IllegalFormatCodePointException.getCodePoint()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCodePoint",
        args = {}
    )
    public void test_getCodePoint() {
        int codePoint = 12345;
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                codePoint);
        assertEquals(codePoint, illegalFormatCodePointException.getCodePoint());
    }

    /**
     * @tests java.util.IllegalFormatCodePointException.getMessage()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        int codePoint = 12345;
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                codePoint);
        assertTrue(null != illegalFormatCodePointException.getMessage());
    }

    // comparator for IllegalFormatCodePointException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatCodePointException initEx = (IllegalFormatCodePointException) initial;
            IllegalFormatCodePointException desrEx = (IllegalFormatCodePointException) deserialized;

            assertEquals("CodePoint", initEx.getCodePoint(), desrEx
                    .getCodePoint());
        }
    };

    /**
     * @tests serialization/deserialization.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(
                new IllegalFormatCodePointException(12345), exComparator);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new IllegalFormatCodePointException(12345), exComparator);
    }
}

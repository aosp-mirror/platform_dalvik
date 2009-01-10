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
import java.util.IllegalFormatWidthException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(IllegalFormatWidthException.class) 
public class IllegalFormatWidthExceptionTest extends TestCase {

    /**
     * @tests java.util.IllegalFormatWidthException#IllegalFormatWidthException(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatWidthException",
        args = {int.class}
    )
    public void test_illegalFormatWidthException() {
        int width = Integer.MAX_VALUE;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertEquals(width, illegalFormatWidthException.getWidth());

    }

    /**
     * @tests java.util.IllegalFormatWidthException#getWidth()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getWidth",
        args = {}
    )
    public void test_getWidth() {
        int width = 12345;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertEquals(width, illegalFormatWidthException.getWidth());

    }

    /**
     * @tests java.util.IllegalFormatWidthException#getMessage()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        int width = 12345;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertTrue(null != illegalFormatWidthException.getMessage());

    }

    // comparator for IllegalFormatWidthException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatWidthException initEx = (IllegalFormatWidthException) initial;
            IllegalFormatWidthException desrEx = (IllegalFormatWidthException) deserialized;

            assertEquals("Width", initEx.getWidth(), desrEx.getWidth());
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

        SerializationTest.verifySelf(new IllegalFormatWidthException(12345),
                exComparator);
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

        SerializationTest.verifyGolden(this, new IllegalFormatWidthException(
                12345), exComparator);
    }
}

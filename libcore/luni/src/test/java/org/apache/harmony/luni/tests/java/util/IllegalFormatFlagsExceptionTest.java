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
import java.util.IllegalFormatFlagsException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(IllegalFormatFlagsException.class) 
public class IllegalFormatFlagsExceptionTest extends TestCase {

    /**
     * @tests java.util.IllegalFormatFlagsException#IllegalFormatFlagsException(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatFlagsException",
        args = {java.lang.String.class}
    )
    public void test_illegalFormatFlagsException() {
        try {
            new IllegalFormatFlagsException(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        assertNotNull(new IllegalFormatFlagsException("String"));
    }

    /**
     * @tests java.util.IllegalFormatFlagsException.getFlags()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFlags",
        args = {}
    )
    public void test_getFlags() {
        String flags = "TESTFLAGS";
        IllegalFormatFlagsException illegalFormatFlagsException = new IllegalFormatFlagsException(
                flags);
        assertEquals(flags, illegalFormatFlagsException.getFlags());
    }

    /**
     * @tests java.util.IllegalFormatFlagsException.getMessage()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String flags = "TESTFLAGS";
        IllegalFormatFlagsException illegalFormatFlagsException = new IllegalFormatFlagsException(
                flags);
        assertTrue(null != illegalFormatFlagsException.getMessage());

    }

    // comparator for IllegalFormatFlagsException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatFlagsException initEx = (IllegalFormatFlagsException) initial;
            IllegalFormatFlagsException desrEx = (IllegalFormatFlagsException) deserialized;

            assertEquals("Flags", initEx.getFlags(), desrEx.getFlags());
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

        SerializationTest.verifySelf(new IllegalFormatFlagsException(
                "TESTFLAGS"), exComparator);
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

        SerializationTest.verifyGolden(this, new IllegalFormatFlagsException(
                "TESTFLAGS"), exComparator);
    }
}

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

package org.apache.harmony.nio_char.tests.java.nio.charset;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;

import java.io.Serializable;
import java.nio.charset.UnsupportedCharsetException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(UnsupportedCharsetException.class)
/**
 * Test class UnsupportedCharsetException.
 */
public class UnsupportedCharsetExceptionTest extends TestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "UnsupportedCharsetException",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCharsetName",
            args = {}
        )
    })
    public void testConstructor() {
        UnsupportedCharsetException ex = new UnsupportedCharsetException(
                "impossible");
        assertTrue(ex instanceof IllegalArgumentException);
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "impossible");
        assertTrue(ex.getMessage().indexOf("impossible") != -1);

        ex = new UnsupportedCharsetException("ascii");
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "ascii");
        assertTrue(ex.getMessage().indexOf("ascii") != -1);

        ex = new UnsupportedCharsetException("");
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "");
        ex.getMessage();

        ex = new UnsupportedCharsetException(null);
        assertNull(ex.getCause());
        assertNull(ex.getCharsetName());
        assertTrue(ex.getMessage().indexOf("null") != -1);
    }

    // comparator for UnsupportedCharsetException objects
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            // FIXME?: getMessage() returns more helpful string but
            // this leads to incompatible message in serial form
            //
            // do common checks for all throwable objects
            // SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
            //        deserialized);

            UnsupportedCharsetException initEx = (UnsupportedCharsetException) initial;
            UnsupportedCharsetException desrEx = (UnsupportedCharsetException) deserialized;

            assertEquals("CharsetName", initEx.getCharsetName(), desrEx
                    .getCharsetName());
        }
    };

    /**
     * @tests serialization/deserialization compatibility.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new UnsupportedCharsetException(
                "charsetName"), COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new UnsupportedCharsetException(
                "charsetName"), COMPARATOR);
    }
}

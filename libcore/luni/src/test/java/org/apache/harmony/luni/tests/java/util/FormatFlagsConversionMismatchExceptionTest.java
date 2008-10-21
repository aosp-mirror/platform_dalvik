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

import java.io.Serializable;
import java.util.FormatFlagsConversionMismatchException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class FormatFlagsConversionMismatchExceptionTest extends TestCase {

    /**
     * @tests java.util.FormatFlagsConversionMismatchException#FormatFlagsConversionMismatchException(String,
     *        char)
     */
    public void test_formatFlagsConversionMismatchException() {
        try {
            new FormatFlagsConversionMismatchException(null, ' ');
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }

    }

    /**
     * @tests java.util.FormatFlagsConversionMismatchException#getFlags()
     */
    public void test_getFlags() {
        String flags = "MYTESTFLAGS";
        char conversion = 'T';
        FormatFlagsConversionMismatchException formatFlagsConversionMismatchException = new FormatFlagsConversionMismatchException(
                flags, conversion);
        assertEquals(flags, formatFlagsConversionMismatchException.getFlags());
    }

    /**
     * @tests java.util.FormatFlagsConversionMismatchException#getConversion()
     */
    public void test_getConversion() {
        String flags = "MYTESTFLAGS";
        char conversion = 'T';
        FormatFlagsConversionMismatchException formatFlagsConversionMismatchException = new FormatFlagsConversionMismatchException(
                flags, conversion);
        assertEquals(conversion, formatFlagsConversionMismatchException
                .getConversion());

    }

    /**
     * @tests java.util.FormatFlagsConversionMismatchException#getMessage()
     */
    public void test_getMessage() {
        String flags = "MYTESTFLAGS";
        char conversion = 'T';
        FormatFlagsConversionMismatchException formatFlagsConversionMismatchException = new FormatFlagsConversionMismatchException(
                flags, conversion);
        assertTrue(null != formatFlagsConversionMismatchException.getMessage());

    }

    // comparator for FormatFlagsConversionMismatchException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            FormatFlagsConversionMismatchException initEx = (FormatFlagsConversionMismatchException) initial;
            FormatFlagsConversionMismatchException desrEx = (FormatFlagsConversionMismatchException) deserialized;

            assertEquals("Flags", initEx.getFlags(), desrEx.getFlags());
            assertEquals("Conversion", initEx.getConversion(), desrEx
                    .getConversion());
        }
    };

    /**
     * @tests serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(
                new FormatFlagsConversionMismatchException("MYTESTFLAGS", 'T'),
                exComparator);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new FormatFlagsConversionMismatchException("MYTESTFLAGS", 'T'),
                exComparator);
    }
}

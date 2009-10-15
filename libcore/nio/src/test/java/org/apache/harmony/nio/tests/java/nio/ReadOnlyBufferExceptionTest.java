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
package org.apache.harmony.nio.tests.java.nio;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.nio.ReadOnlyBufferException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

@TestTargetClass(ReadOnlyBufferException.class)
public class ReadOnlyBufferExceptionTest extends TestCase {

    /**
     * @tests serialization/deserialization compatibility.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "ReadOnlyBufferException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new ReadOnlyBufferException());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationGolden",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "ReadOnlyBufferException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new ReadOnlyBufferException());
    }

    /**
     *@tests {@link java.nio.ReadOnlyBufferException#ReadOnlyBufferException()}
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ReadOnlyBufferException",
        args = {}
    )
    public void test_Constructor() {
        ReadOnlyBufferException exception = new ReadOnlyBufferException();
        assertNull(exception.getMessage());
        assertNull(exception.getLocalizedMessage());
        assertNull(exception.getCause());
    }
}

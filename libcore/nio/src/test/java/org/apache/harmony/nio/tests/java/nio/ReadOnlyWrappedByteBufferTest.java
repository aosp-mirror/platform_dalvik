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

import java.nio.ReadOnlyBufferException;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(java.nio.ByteBuffer.class)
public class ReadOnlyWrappedByteBufferTest extends WrappedByteBufferTest {

    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "array",
        args = {}
    )
    public void testArray() {
        try {
            buf.array();
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "arrayOffset",
        args = {}
    )
    public void testArrayOffset() {
        try {
            buf.arrayOffset();
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isReadOnly method for read only wrapped ByteBuffer.",
        method = "isReadOnly",
        args = {}
    )
    public void testIsReadOnly() {
        assertTrue(buf.isReadOnly());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies hasArray method for read only wrapped ByteBuffer.",
        method = "hasArray",
        args = {}
    )
    public void testHasArray() {
        assertFalse(buf.hasArray());
        try {
            buf.array();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        super.readOnlyHashCode(false);
    }


}

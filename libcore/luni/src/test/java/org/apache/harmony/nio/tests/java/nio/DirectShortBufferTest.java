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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.IntBuffer;

@TestTargetClass(java.nio.ShortBuffer.class)
public class DirectShortBufferTest extends ShortBufferTest {
    public void setUp(){
        capacity = BUFFER_LENGTH;
        buf = ByteBuffer.allocateDirect(BUFFER_LENGTH*2).asShortBuffer();
        loadTestData1(buf);
        baseBuf = buf;
    }
    
    public void tearDown(){
        buf = null;
        baseBuf = null;
    }

    /**
     * Regression for http://code.google.com/p/android/issues/detail?id=3279 
     */
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "put",
            args = {short[].class, int.class, int.class}
    )
    public void testPutWhenOffsetIsNonZero() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(40);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();

        short[] source = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        shortBuffer.put(source, 2, 2);
        shortBuffer.put(source, 4, 2);
        assertEquals(4, shortBuffer.get(0));
        assertEquals(5, shortBuffer.get(1));
        assertEquals(6, shortBuffer.get(2));
        assertEquals(7, shortBuffer.get(3));
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies hasArray method for direct ShortBuffer.",
        method = "hasArray",
        args = {}
    )
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies array method for direct ShortBuffer.",
        method = "array",
        args = {}
    )
    public void testArray() {
        try {
            buf.array();
            fail("Should throw UnsupportedOperationException"); //$NON-NLS-1$
        } catch (UnsupportedOperationException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies arrayOffset method for direct ShortBuffer.",
        method = "arrayOffset",
        args = {}
    )
    public void testArrayOffset() {
        try {
            buf.arrayOffset();
            fail("Should throw UnsupportedOperationException"); //$NON-NLS-1$
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isDirect method for direct ShortBuffer.",
        method = "isDirect",
        args = {}
    )
    public void testIsDirect() {
        assertTrue(buf.isDirect());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies order method for direct ShortBuffer.",
        method = "order",
        args = {}
    )
    public void testOrder() {
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test for ShortToByteBufferAdapter",
        clazz = ByteBuffer.class,
        method = "asShortBuffer",
        args = {}
    )
    public void testRangeChecks() {
        short[] myShorts = new short[BUFFER_LENGTH];

        for (short i = 0; i < BUFFER_LENGTH; i++) {
            myShorts[i] = (short) (1000 + i);
        }

        buf.position(0);
        buf.put(myShorts, 0, BUFFER_LENGTH);
        buf.position(0);
        buf.put(myShorts, 0, BUFFER_LENGTH);

        try {
            buf.put(myShorts, 0, 1); // should fail
            fail("BufferOverflowException expected but not thrown");
        } catch (BufferOverflowException boe) {
            // expected
        }

        try {
            buf.position(0);
            buf.put(myShorts, 0, BUFFER_LENGTH + 1); // should fail
            fail("BufferOverflowException expected but not thrown");
        } catch (IndexOutOfBoundsException ioobe) {
            // expected
        }

        try {
            buf.position(BUFFER_LENGTH - 1);
            buf.put(myShorts, 0, 2); // should fail
            fail("BufferOverflowException expected but not thrown");
        } catch (BufferOverflowException boe) {
            // expected
        }
    }
}

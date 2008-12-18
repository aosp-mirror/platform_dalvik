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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;

@TestTargetClass(java.nio.LongBuffer.class)
public class ReadOnlyLongBufferTest extends LongBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies isReadOnly method for read only LongBuffer.",
      targets = {
        @TestTarget(
          methodName = "isReadOnly",
          methodArgs = {}
        )
    })
    public void testIsReadOnly() {
        assertTrue(buf.isReadOnly());
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that hasArray method returns false for read only " +
            "LongBuffer.",
      targets = {
        @TestTarget(
          methodName = "hasArray",
          methodArgs = {}
        )
    })
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "array",
          methodArgs = {}
        )
    })
    public void testArray() {
        try {
            buf.array();
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            //expected
        }
    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public void testHashCode() {
        LongBuffer duplicate = buf.duplicate();
        assertEquals(buf.hashCode(), duplicate.hashCode());
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies UnsupportedOperationException.",
      targets = {
        @TestTarget(
          methodName = "arrayOffset",
          methodArgs = {}
        )
    })
    public void testArrayOffset() {
        try {
            buf.arrayOffset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "compact",
          methodArgs = {}
        )
    })
    public void testCompact() {
        try {
            buf.compact();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "put",
          methodArgs = {long.class}
        )
    })
    public void testPutlong() {
        try {
            buf.put(0);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "put",
          methodArgs = {long[].class}
        )
    })
    public void testPutlongArray() {
        long array[] = new long[1];
        try {
            buf.put(array);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put((long[]) null);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "put",
          methodArgs = {long[].class, int.class, int.class}
        )
    })
    public void testPutlongArrayintint() {
        long array[] = new long[1];
        try {
            buf.put(array, 0, array.length);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put((long[]) null, 0, 1);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put(new long[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put(array, -1, array.length);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "put",
          methodArgs = {java.nio.LongBuffer.class}
        )
    })
    public void testPutLongBuffer() {
        LongBuffer other = LongBuffer.allocate(1);
        try {
            buf.put(other);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put((LongBuffer) null);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put(buf);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies ReadOnlyBufferException.",
      targets = {
        @TestTarget(
          methodName = "put",
          methodArgs = {int.class, long.class}
        )
    })
    public void testPutintlong() {
        try {
            buf.put(0, (long) 0);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
        try {
            buf.put(-1, (long) 0);
            fail("Should throw ReadOnlyBufferException"); //$NON-NLS-1$
        } catch (ReadOnlyBufferException e) {
            // expected
        }
    }
}

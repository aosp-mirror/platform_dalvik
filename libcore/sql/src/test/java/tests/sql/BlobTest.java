/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.sql;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import java.sql.Blob;

/**
 * @author andrea@google.com (Your Name Here)
 *
 */
@TestTargetClass(Blob.class)
public class BlobTest extends TestCase {

    /**
     * Test method for {@link java.sql.Blob#getBinaryStream()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBinaryStream",
      args = {}
      )
    public void testGetBinaryStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#getBytes(long, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBytes",
      args = {long.class, int.class}
      )
    public void testGetBytes() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#length()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "length",
      args = {}
      )
    public void testLength() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#position(java.sql.Blob, long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "position",
      args = {Blob.class, long.class}
      )
    public void testPositionBlobLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#position(byte[], long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method =  "position",
      args = {byte[].class, long.class}
    )
    public void testPositionByteArrayLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#setBinaryStream(long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setBinaryStream",
      args = {long.class}
      )
    public void testSetBinaryStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#setBytes(long, byte[])}.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method  = "setBytes",
            args = {long.class, byte[].class}
    )
    public void testSetBytesLongByteArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#setBytes(long, byte[], int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setBytes",
      args = {long.class, byte[].class, int.class, int.class}
      )
    public void testSetBytesLongByteArrayIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Blob#truncate(long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "truncate",
      args = {long.class}
      )
    public void testTruncate() {
        fail("Not yet implemented");
    }

}

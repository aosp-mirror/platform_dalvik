/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLData;
import java.sql.SQLOutput;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;

@TestTargetClass(SQLOutput.class)
public class SQLOutputTest extends TestCase {

    /**
     * Test method for {@link java.sql.SQLOutput#writeString(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeString",
      args = {String.class}
      )
    public void testWriteString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeBoolean(boolean)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeBoolean",
      args = {boolean.class}
      )
    public void testWriteBoolean() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeByte(byte)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeByte",
      args = {byte.class}
      )
    public void testWriteByte() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeShort(short)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeShort",
      args = {short.class}
      )
    public void testWriteShort() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeInt(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeInt",
      args = {int.class}
      )
    public void testWriteInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeLong(long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeLong",
      args = {long.class}
      )
    public void testWriteLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeFloat(float)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeFloat",
      args = {float.class}
      )
    public void testWriteFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeDouble(double)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeDouble",
      args = {double.class}
      )
    public void testWriteDouble() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link java.sql.SQLOutput#writeBigDecimal(java.math.BigDecimal)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeBigDecimal",
      args = {BigDecimal.class}
      )
    public void testWriteBigDecimal() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeBytes(byte[])}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeBytes",
      args = {byte[].class}
    )
    public void testWriteBytes() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeDate(java.sql.Date)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeDate",
      args = {Date.class}
      )
    public void testWriteDate() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeTime(java.sql.Time)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeTime",
      args = {Time.class}
      )
    public void testWriteTime() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link java.sql.SQLOutput#writeTimestamp(java.sql.Timestamp)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeTimestamp",
      args = {Timestamp.class}
      )
    public void testWriteTimestamp() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link java.sql.SQLOutput#writeCharacterStream(java.io.Reader)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeCharacterStream",
      args = {Reader.class}
      )
    public void testWriteCharacterStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link java.sql.SQLOutput#writeAsciiStream(java.io.InputStream)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeAsciiStream",
      args = {InputStream.class}
      )
    public void testWriteAsciiStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link java.sql.SQLOutput#writeBinaryStream(java.io.InputStream)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeBinaryStream",
      args = {InputStream.class}
      )
    public void testWriteBinaryStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeObject(java.sql.SQLData)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeObject",
      args = {SQLData.class}
      )
    public void testWriteObject() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeRef(java.sql.Ref)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeRef",
      args = {Ref.class}
      )
    public void testWriteRef() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeBlob(java.sql.Blob)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeBlob",
      args = {Blob.class}
      )
    public void testWriteBlob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeClob(java.sql.Clob)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeClob",
      args = {Clob.class}
      )
    public void testWriteClob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeStruct(java.sql.Struct)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeStruct",
      args = {Struct.class}
      )
    public void testWriteStruct() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeArray(java.sql.Array)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeArray",
      args = {Array.class}
      )
    public void testWriteArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLOutput#writeURL(java.net.URL)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "writeURL",
      args = {URL.class}
      )
    public void testWriteURL() {
        fail("Not yet implemented");
    }

}

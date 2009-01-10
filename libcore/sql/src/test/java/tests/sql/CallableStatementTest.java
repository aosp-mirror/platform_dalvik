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
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * @author andrea@google.com (Your Name Here)
 *
 */
@TestTargetClass(CallableStatement.class)
public class CallableStatementTest extends TestCase {

    /**
     * Test method for {@link java.sql.CallableStatement#getArray(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getArray",
          args = {int.class}
      )
    public void testGetArrayInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getArray(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getArray",
      args = {String.class}
    )
    public void testGetArrayString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBigDecimal(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBigDecimal",
      args = {int.class}
    )
    public void testGetBigDecimalInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBigDecimal(int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBigDecimal",
      args = {int.class, int.class}
    )
    public void testGetBigDecimalIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBigDecimal(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBigDecimal",
      args = {String.class}
    )
    public void testGetBigDecimalString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBlob(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBlob",
      args = {int.class}
    )
    public void testGetBlobInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBlob(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBlob",
      args = {String.class}
    )
    public void testGetBlobString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBoolean(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBoolean",
      args = {int.class}
    )
    public void testGetBooleanInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBoolean(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBoolean",
      args = {String.class}
    )
    public void testGetBooleanString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getByte(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getByte",
      args = {int.class}
    )
    public void testGetByteInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getByte(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getByte",
      args = {String.class}
    )
    public void testGetByteString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBytes(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBytes",
      args = {int.class}
    )
    public void testGetBytesInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getBytes(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBytes",
      args = {String.class}
    )
    public void testGetBytesString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getClob(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getClob",
      args = {int.class}
    )
    public void testGetClobInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getClob(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getClob",
      args = {String.class}
    )
    public void testGetClobString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getDate(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getDate",
      args = {int.class}
    )
    public void testGetDateInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getDate(int, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getDate",
      args = {int.class, Calendar.class}
    )
    public void testGetDateIntCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getDate(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getDate",
      args = {String.class}
    )
    public void testGetDateString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getDate(java.lang.String, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getDate",
      args = {String.class, Calendar.class}
    )
    public void testGetDateStringCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getDouble(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getDouble",
      args = {int.class}
    )
    public void testGetDoubleInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getDouble(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getDouble",
      args = {String.class}
    )
    public void testGetDoubleString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getFloat(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getFloat",
      args = {int.class}
    )
    public void testGetFloatInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getFloat(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getFloat",
      args = {String.class}
    )
    public void testGetFloatString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getInt(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getInt",
      args = {int.class}
    )
    public void testGetIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getInt(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getInt",
      args = {String.class}
    )
    public void testGetIntString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getLong(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getLong",
      args = {int.class}
    )
    public void testGetLongInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getLong(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getLong",
      args = {String.class}
    )
    public void testGetLongString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getObject(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getObject",
      args = {int.class}
    )
    public void testGetObjectInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getObject(int, java.util.Map)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getObject",
      args = {int.class, Map.class}
    )
    public void testGetObjectIntMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getObject(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getObject",
      args = {String.class}
    )
    public void testGetObjectString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getObject(java.lang.String, java.util.Map)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getObject",
      args = {String.class, Map.class}
    )
    public void testGetObjectStringMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getRef(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getRef",
      args = {int.class}
    )
    public void testGetRefInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getRef(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getRef",
      args = {String.class}
    )
    public void testGetRefString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getShort(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getShort",
      args = {int.class}
    )
    public void testGetShortInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getShort(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getShort",
      args = {String.class}
    )
    public void testGetShortString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getString(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getString",
      args = {int.class}
    )
    public void testGetStringInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getString(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getString",
      args = {String.class}
    )
    public void testGetStringString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTime(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTime",
      args = {int.class}
    )
    public void testGetTimeInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTime(int, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTime",
      args = {int.class, Calendar.class}
    )
    public void testGetTimeIntCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTime(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTime",
      args = {String.class}
    )
    public void testGetTimeString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTime(java.lang.String, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTime",
      args = {String.class, Calendar.class}
    )
    public void testGetTimeStringCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTimestamp(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTimestamp",
      args = {int.class}
    )
    public void testGetTimestampInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTimestamp(int, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTimestamp",
      args = {int.class, Calendar.class}
    )
    public void testGetTimestampIntCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTimestamp(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTimestamp",
      args = {String.class}
    )
    public void testGetTimestampString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getTimestamp(java.lang.String, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getTimestamp",
      args = {String.class, Calendar.class}
    )
    public void testGetTimestampStringCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getURL(int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getURL",
      args = {int.class}
    )
    public void testGetURLInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#getURL(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getURL",
      args = {String.class}
    )
    public void testGetURLString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#registerOutParameter(int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "registerOutParameter",
      args = {int.class, int.class}
    )
    public void testRegisterOutParameterIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#registerOutParameter(int, int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "registerOutParameter",
      args = {int.class, int.class, int.class}
    )
    public void testRegisterOutParameterIntIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#registerOutParameter(int, int, java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "registerOutParameter",
      args = {int.class, int.class, String.class}
    )
    public void testRegisterOutParameterIntIntString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#registerOutParameter(java.lang.String, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "registerOutParameter",
      args = {String.class, int.class}
    )
    public void testRegisterOutParameterStringInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#registerOutParameter(java.lang.String, int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "registerOutParameter",
      args = {String.class, int.class, int.class}
    )
    public void testRegisterOutParameterStringIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#registerOutParameter(java.lang.String, int, java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "registerOutParameter",
      args = {String.class, int.class, String.class}
    )
    public void testRegisterOutParameterStringIntString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setAsciiStream",
      args = {String.class, InputStream.class, int.class}
    )
    public void testSetAsciiStreamStringInputStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setBigDecimal(java.lang.String, java.math.BigDecimal)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setBigDecimal",
      args = {String.class, BigDecimal.class}
    )
    public void testSetBigDecimalStringBigDecimal() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setBinaryStream",
      args = {String.class, InputStream.class, int.class}
    )
    public void testSetBinaryStreamStringInputStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setBoolean(java.lang.String, boolean)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setBoolean",
      args = {String.class, boolean.class}
    )
    public void testSetBooleanStringBoolean() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setByte(java.lang.String, byte)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setByte",
      args = {String.class, byte.class}
    )
    public void testSetByteStringByte() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setBytes(java.lang.String, byte[])}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setBytes",
      args = {String.class, byte[].class}
    )
    public void testSetBytesStringByteArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setCharacterStream",
      args = {String.class, Reader.class, int.class}
    )
    public void testSetCharacterStreamStringReaderInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setDate",
      args = {String.class, Date.class}
    )
    public void testSetDateStringDate() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setDate",
      args = {String.class, Date.class, Calendar.class}
    )
    public void testSetDateStringDateCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setDouble(java.lang.String, double)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setDouble",
      args = {String.class, double.class}
    )
    public void testSetDoubleStringDouble() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setFloat(java.lang.String, float)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setFloat",
      args = {String.class, float.class}
    )
    public void testSetFloatStringFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setInt(java.lang.String, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setInt",
      args = {String.class, int.class}
    )
    public void testSetIntStringInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setLong(java.lang.String, long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setLong",
      args = {String.class, long.class}
    )
    public void testSetLongStringLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setNull(java.lang.String, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setNull",
      args = {String.class, int.class}
    )
    public void testSetNullStringInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setNull(java.lang.String, int, java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setNull",
      args = {String.class, int.class, String.class}
    )
    public void testSetNullStringIntString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setObject",
      args = {String.class, Object.class}
    )
    public void testSetObjectStringObject() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setObject",
      args = {String.class, Object.class, int.class}
    )
    public void testSetObjectStringObjectInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object, int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setObject",
      args = {String.class, Object.class, int.class, int.class}
    )
    public void testSetObjectStringObjectIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setShort(java.lang.String, short)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setShort",
      args = {String.class, short.class}
    )
    public void testSetShortStringShort() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setString(java.lang.String, java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setString",
      args = {String.class, String.class}
    )
    public void testSetStringStringString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setTime",
      args = {String.class, Time.class}
    )
    public void testSetTimeStringTime() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setTime",
      args = {String.class, Time.class, Calendar.class}
    )
    public void testSetTimeStringTimeCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setTimestamp",
      args = {String.class, Timestamp.class}
    )
    public void testSetTimestampStringTimestamp() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp, java.util.Calendar)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setTimestamp",
      args = {String.class, Timestamp.class, Calendar.class}
    )
    public void testSetTimestampStringTimestampCalendar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#setURL(java.lang.String, java.net.URL)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "setURL",
      args = {String.class, URL.class}
    )
    public void testSetURLStringURL() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.CallableStatement#wasNull()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "wasNull",
      args = {}
    )
    public void testWasNull() {
        fail("Not yet implemented");
    }

}

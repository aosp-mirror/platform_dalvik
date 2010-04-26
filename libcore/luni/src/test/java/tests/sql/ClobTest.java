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

import java.sql.Clob;

/**
 * @author andrea@google.com (Your Name Here)
 *
 */
@TestTargetClass(Clob.class)
public class ClobTest extends TestCase {

    /**
     * Test method for {@link java.sql.Clob#getAsciiStream()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getAsciiStream",
          args = {}
      )
    public void testGetAsciiStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#getCharacterStream()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getCharacterStream",
          args = {}
      )
    public void testGetCharacterStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#getSubString(long, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getSubString",
          args = {long.class, int.class}
      )
    public void testGetSubString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#length()}.
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
     * Test method for {@link java.sql.Clob#position(java.sql.Clob, long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "position",
          args = {Clob.class, long.class}
      )
    public void testPositionClobLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#position(java.lang.String, long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "position",
          args = {String.class, long.class}
      )
    public void testPositionStringLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#setAsciiStream(long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "setAsciiStream",
          args = {long.class}
      )
    public void testSetAsciiStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#setCharacterStream(long)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "setCharacterStream",
          args = {long.class}
      )
    public void testSetCharacterStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#setString(long, java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "setString",
          args = {long.class, String.class}
      )
    public void testSetStringLongString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#setString(long, java.lang.String, int, int)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "setString",
          args = {long.class, String.class, int.class, int.class}
      )
    public void testSetStringLongStringIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Clob#truncate(long)}.
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

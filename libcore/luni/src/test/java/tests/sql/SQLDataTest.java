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

import java.sql.SQLData;
import java.sql.SQLInput;
import java.sql.SQLOutput;


@TestTargetClass(SQLData.class)
public class SQLDataTest extends TestCase {

    /**
     * Test method for {@link java.sql.SQLData#getSQLTypeName()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getSQLTypeName",
          args = {}
        )
    public void testGetSQLTypeName() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLData#readSQL(java.sql.SQLInput, java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "readSQL",
          args = {SQLInput.class, String.class}
        )
    public void testReadSQL() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.SQLData#writeSQL(java.sql.SQLOutput)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "writeSQL",
          args = {SQLOutput.class}
        )
    public void testWriteSQL() {
        fail("Not yet implemented");
    }

}

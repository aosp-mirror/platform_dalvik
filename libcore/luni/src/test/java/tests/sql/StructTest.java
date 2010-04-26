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

import java.sql.Struct;
import java.util.Map;

@TestTargetClass(Struct.class)
public class StructTest extends TestCase {

    /**
     * Test method for {@link java.sql.Struct#getSQLTypeName()}.
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
     * Test method for {@link java.sql.Struct#getAttributes()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
        
          method = "getAttributes",
          args = {}
    )
    public void testGetAttributes() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Struct#getAttributes(java.util.Map)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getAttributes",
          args = {Map.class}
    )
    public void testGetAttributesMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }

}

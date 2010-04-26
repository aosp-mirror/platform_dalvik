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

import java.sql.Ref;
import java.util.Map;

/**
 * @author andrea@google.com (Your Name Here)
 *
 */
@TestTargetClass(Ref.class)
public class RefTest extends TestCase {

    /**
     * Test method for {@link java.sql.Ref#getBaseTypeName()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getBaseTypeName",
      args = {}
      )
    public void testGetBaseTypeName() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Ref#getObject()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getObject",
      args = {}
      )
    public void testGetObject() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Ref#getObject(java.util.Map)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getObject",
      args = {Map.class}
      )
    public void testGetObjectMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.Ref#setObject(java.lang.Object)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "setObject",
          args = {Object.class}
    )
    public void testSetObject() {
        fail("Not yet implemented");
    }

}

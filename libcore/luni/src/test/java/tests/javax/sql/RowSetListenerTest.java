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

package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;

/**
 *
 */
@TestTargetClass(RowSetListener.class)
public class RowSetListenerTest extends TestCase {

    /**
     * Test method for {@link javax.sql.RowSetListener#cursorMoved(javax.sql.RowSetEvent)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "cursorMoved",
        args = {javax.sql.RowSetEvent.class}
    )
    public void testCursorMoved() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link javax.sql.RowSetListener#rowChanged(javax.sql.RowSetEvent)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowChanged",
        args = {javax.sql.RowSetEvent.class}
    )
    public void testRowChanged() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link javax.sql.RowSetListener#rowSetChanged(javax.sql.RowSetEvent)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowSetChanged",
        args = {javax.sql.RowSetEvent.class}
    )
    public void testRowSetChanged() {
        fail("Not yet implemented");
    }

}

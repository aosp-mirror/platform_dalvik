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

package tests.SQLite;

import SQLite.Constants;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

@TestTargetClass(Constants.class)
public class ConstantsTest extends TestCase {

    /**
     * @tests Constants#Constants()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "constructor test",
        method = "Constants",
        args = {}
    )
    public void testConstants() {
        Constants c = new Constants();
            
        assertNotNull(c);
        assertEquals(c.SQLITE_OK, 0);
        assertEquals(c.SQLITE_ERROR, 1);
        assertEquals(c.SQLITE_INTERNAL, 2);
        assertEquals(c.SQLITE_PERM, 3);
        assertEquals(c.SQLITE_ABORT, 4);
        assertEquals(c.SQLITE_BUSY, 5);
        assertEquals(c.SQLITE_LOCKED, 6);
        assertEquals(c.SQLITE_NOMEM, 7);
        assertEquals(c.SQLITE_READONLY, 8);
        assertEquals(c.SQLITE_INTERRUPT, 9);
        assertEquals(c.SQLITE_IOERR, 10);
        assertEquals(c.SQLITE_CORRUPT, 11);
        assertEquals(c.SQLITE_NOTFOUND, 12);
        assertEquals(c.SQLITE_FULL, 13);
        assertEquals(c.SQLITE_CANTOPEN, 14);
        assertEquals(c.SQLITE_PROTOCOL, 15);
        assertEquals(c.SQLITE_EMPTY, 16);
        assertEquals(c.SQLITE_SCHEMA, 17);
        assertEquals(c.SQLITE_TOOBIG, 18);
        assertEquals(c.SQLITE_CONSTRAINT, 19);
        assertEquals(c.SQLITE_MISMATCH, 20);
        assertEquals(c.SQLITE_MISUSE, 21);
        assertEquals(c.SQLITE_NOLFS, 22);
        assertEquals(c.SQLITE_AUTH, 23);
        assertEquals(c.SQLITE_FORMAT, 24);
        assertEquals(c.SQLITE_RANGE, 25);
        assertEquals(c.SQLITE_NOTADB, 26);
        assertEquals(c.SQLITE_ROW, 100);
        assertEquals(c.SQLITE_DONE, 101);
        assertEquals(c.SQLITE_INTEGER, 1);
        assertEquals(c.SQLITE_FLOAT, 2);
        assertEquals(c.SQLITE_BLOB, 4);
        assertEquals(c.SQLITE_NULL, 5);
        assertEquals(c.SQLITE3_TEXT, 3);
        assertEquals(c.SQLITE_NUMERIC, -1);
        assertEquals(c.SQLITE_TEXT, 3);
        assertEquals(c.SQLITE2_TEXT, -2);
        assertEquals(c.SQLITE_ARGS, -3);
        assertEquals(c.SQLITE_COPY, 0);
        assertEquals(c.SQLITE_CREATE_INDEX, 1);
        assertEquals(c.SQLITE_CREATE_TABLE, 2);
        assertEquals(c.SQLITE_CREATE_TEMP_INDEX, 3);
        assertEquals(c.SQLITE_CREATE_TEMP_TABLE, 4);
        assertEquals(c.SQLITE_CREATE_TEMP_TRIGGER, 5);
        assertEquals(c.SQLITE_CREATE_TEMP_VIEW, 6);
        assertEquals(c.SQLITE_CREATE_TRIGGER, 7);
        assertEquals(c.SQLITE_CREATE_VIEW, 8);
        assertEquals(c.SQLITE_DELETE, 9);
        assertEquals(c.SQLITE_DROP_INDEX, 10);
        assertEquals(c.SQLITE_DROP_TABLE, 11);
        assertEquals(c.SQLITE_DROP_TEMP_INDEX, 12);
        assertEquals(c.SQLITE_DROP_TEMP_TABLE, 13);
        assertEquals(c.SQLITE_DROP_TEMP_TRIGGER, 14);
        assertEquals(c.SQLITE_DROP_TEMP_VIEW, 15);
        assertEquals(c.SQLITE_DROP_TRIGGER, 16);
        assertEquals(c.SQLITE_DROP_VIEW, 17);
        assertEquals(c.SQLITE_INSERT, 18);
        assertEquals(c.SQLITE_PRAGMA, 19);
        assertEquals(c.SQLITE_READ, 20);
        assertEquals(c.SQLITE_SELECT, 21);
        assertEquals(c.SQLITE_TRANSACTION, 22);
        assertEquals(c.SQLITE_UPDATE, 23);
        assertEquals(c.SQLITE_ATTACH, 24);
        assertEquals(c.SQLITE_DETACH, 25);
        assertEquals(c.SQLITE_DENY, 1);
        assertEquals(c.SQLITE_IGNORE, 2);
    }
}

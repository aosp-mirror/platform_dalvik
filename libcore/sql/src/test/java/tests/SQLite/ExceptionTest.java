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

import SQLite.Exception;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

public class ExceptionTest extends TestCase {

    public ExceptionTest(String name) {
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        super.setUp();
    }

    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
    }
    
    /**
     * @tests {@link Exception#Exception(String)}
     */
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "constructor test",
        method = "Exception",
        args = {java.lang.String.class}
    )
    public void testException() {
        fail("not yet implemented");
    }

}

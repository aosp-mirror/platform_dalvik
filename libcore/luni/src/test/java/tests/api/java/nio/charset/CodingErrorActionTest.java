/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.nio.charset;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;

import java.nio.charset.CodingErrorAction;

import junit.framework.TestCase;
@TestTargetClass(CodingErrorAction.class)
/**
 * Test class java.nio.charset.CodingErrorAction
 */
public class CodingErrorActionTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test the constants.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verify constant",
        method = "!Constants",
        args = {}
    )
    public void testIGNORE() {
        assertNotNull(CodingErrorAction.IGNORE);
        assertNotNull(CodingErrorAction.REPLACE);
        assertNotNull(CodingErrorAction.REPORT);
        assertNotSame(CodingErrorAction.IGNORE, CodingErrorAction.REPLACE);
        assertNotSame(CodingErrorAction.IGNORE, CodingErrorAction.REPORT);
        assertNotSame(CodingErrorAction.REPLACE, CodingErrorAction.REPORT);
    }

    /*
     * Test the method toString().
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verify constant",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertTrue(CodingErrorAction.IGNORE.toString().indexOf("IGNORE") != -1);
        assertTrue(CodingErrorAction.REPLACE.toString().indexOf("REPLACE") != -1);
        assertTrue(CodingErrorAction.REPORT.toString().indexOf("REPORT") != -1);
    }
}

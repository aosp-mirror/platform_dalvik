/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.sql.tests.java.sql;

import java.sql.DriverPropertyInfo;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * JUnit Testcase for the java.sql.DriverPropertyInfo class
 * 
 */

public class DriverPropertyInfoTest extends TestCase {

    /*
     * Public statics test
     */
    public void testPublicStatics() {

    } // end method testPublicStatics

    /*
     * Constructor test
     */
    public void testDriverPropertyInfoStringString() {

        DriverPropertyInfo aDriverPropertyInfo = new DriverPropertyInfo(
                validName, validValue);

        assertNotNull(aDriverPropertyInfo);

        aDriverPropertyInfo = new DriverPropertyInfo(null, null);

    } // end method testDriverPropertyInfoStringString

    /*
     * Public fields test
     */
    static String validName = "testname";

    static String validValue = "testvalue";

    static String[] updateChoices = { "Choice1", "Choice2", "Choice3" };

    static String updateValue = "updateValue";

    static boolean updateRequired = true;

    static String updateDescription = "update description";

    static String updateName = "updateName";

    public void testPublicFields() {

        // Constructor here...
        DriverPropertyInfo aDriverPropertyInfo = new DriverPropertyInfo(
                validName, validValue);

        assertTrue(Arrays.equals(testChoices, aDriverPropertyInfo.choices));
        assertEquals(testValue, aDriverPropertyInfo.value);
        assertEquals(testRequired, aDriverPropertyInfo.required);
        assertEquals(testDescription, aDriverPropertyInfo.description);
        assertEquals(testName, aDriverPropertyInfo.name);

        aDriverPropertyInfo.choices = updateChoices;
        aDriverPropertyInfo.value = updateValue;
        aDriverPropertyInfo.required = updateRequired;
        aDriverPropertyInfo.description = updateDescription;
        aDriverPropertyInfo.name = updateName;

        assertTrue(Arrays.equals(updateChoices, aDriverPropertyInfo.choices));
        assertEquals(updateValue, aDriverPropertyInfo.value);
        assertEquals(updateRequired, aDriverPropertyInfo.required);
        assertEquals(updateDescription, aDriverPropertyInfo.description);
        assertEquals(updateName, aDriverPropertyInfo.name);

    } // end method testPublicFields

    // Default values...
    static String[] testChoices = null;

    static java.lang.String testValue = validValue;

    static boolean testRequired = false;

    static java.lang.String testDescription = null;

    static java.lang.String testName = validName;

} // end class DriverPropertyInfoTest

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

package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.Formatter.BigDecimalLayoutForm;

@TestTargetClass(java.util.    Formatter.BigDecimalLayoutForm.class) 
public class FormatterTest extends TestCase {

    /**
     * @tests java.util.Formatter.BigDecimalLayoutForm#values()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_values() {
        BigDecimalLayoutForm[] vals = BigDecimalLayoutForm.values();
        assertEquals("Invalid length of enum values", 2, vals.length);
        assertEquals("Wrong scientific value in enum", 
                                      BigDecimalLayoutForm.SCIENTIFIC, vals[0]);
        assertEquals("Wrong dec float value in enum", 
                                   BigDecimalLayoutForm.DECIMAL_FLOAT, vals[1]);
    }
    
    /**
     * @tests java.util.Formatter.BigDecimalLayoutForm#valueOf(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void test_valueOfLjava_lang_String() {
        BigDecimalLayoutForm sci = BigDecimalLayoutForm.valueOf("SCIENTIFIC");
        assertEquals("Wrong scientific value in enum", BigDecimalLayoutForm.SCIENTIFIC, sci);

        BigDecimalLayoutForm decFloat = BigDecimalLayoutForm.valueOf("DECIMAL_FLOAT");
        assertEquals("Wrong dec float value from valueOf ", BigDecimalLayoutForm.DECIMAL_FLOAT, decFloat);

        try {
            decFloat = BigDecimalLayoutForm.valueOf("Wrong format");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }
}

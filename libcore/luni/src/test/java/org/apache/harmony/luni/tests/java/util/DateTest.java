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

import java.util.Date;

import junit.framework.TestCase;

public class DateTest extends TestCase {

    /**
     * @tests java.util.Date#parse(String)
     */
    @SuppressWarnings("deprecation")
    public void test_parseLjava_lang_String() {
        // Regression for HARMONY-102
        assertEquals("Assert 0: parse failure",
                -5400000, Date.parse("Sat, 1 Jan 1970 +0130 00:00:00"));
        assertEquals("Assert 1: parse failure",
                858600000, Date.parse("00:00:00 GMT +0130 Sat, 11 Jan 1970"));
    }

}

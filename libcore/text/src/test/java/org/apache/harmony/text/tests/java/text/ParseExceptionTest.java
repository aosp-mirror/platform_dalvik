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
package org.apache.harmony.text.tests.java.text;

import java.text.DateFormat;
import java.text.ParseException;

public class ParseExceptionTest extends junit.framework.TestCase {

    /**
     * @tests java.text.ParseException#ParseException(java.lang.String, int)
     */
    public void test_ConstructorLjava_lang_StringI() {
        // Test for method java.text.ParseException(java.lang.String, int)
        // SM
        try {
            DateFormat df = DateFormat.getInstance();
            df.parse("HelloWorld");
        } catch (ParseException e) {
            return;
        }
        fail("ParseException not created/thrown.");
    }

    /**
     * @tests java.text.ParseException#getErrorOffset()
     */
    public void test_getErrorOffset() {
        // Test for method int java.text.ParseException.getErrorOffset()
        // SM
        try {
            DateFormat df = DateFormat.getInstance();
            df.parse("1999HelloWorld");
        } catch (ParseException e) {
            assertEquals("getErrorOffsetFailed.", 4, e.getErrorOffset());
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}

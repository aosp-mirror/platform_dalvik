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

package org.apache.harmony.luni.tests.util;

import org.apache.harmony.luni.util.MsgHelp;

import junit.framework.TestCase;

public class MsgHelpTest extends TestCase {

    public MsgHelpTest(String name) {
        super(name);
    }

    /*
     * Test method for 'org.apache.harmony.luni.util.MsgHelp.format(String,
     * Object[])'
     */
    public void testFormatLjava_lang_String$Ljava_lang_Object() {
        assertEquals("empty", MsgHelp.format("empty", new Object[0]));

        assertEquals("<null>", MsgHelp.format("{0}", new Object[1]));
        assertEquals("<missing argument>", MsgHelp.format("{0}", new Object[0]));
        assertEquals("fixture {} fixture", MsgHelp.format("{0} \\{} {0}",
                new Object[] { "fixture" }));

        assertEquals("<null> fixture", MsgHelp.format("{0} {1}", new Object[] {
                null, "fixture" }));
        assertEquals("<null> fixture <missing argument>", MsgHelp.format(
                "{0} {1} {2}", new Object[] { null, "fixture" }));
        assertEquals("<null> fixture", MsgHelp.format("{0} {1}", new Object[] {
                null, "fixture", "extra" }));

        assertEquals("0 1 2 3 4 5 6 7 8 9", MsgHelp.format(
                "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9}", new Object[] { "0",
                        "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        assertEquals("9 8 7 6 5 4 3 2 1 0", MsgHelp.format(
                "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9}", new Object[] { "9",
                        "8", "7", "6", "5", "4", "3", "2", "1", "0" }));

        assertEquals("0 1 2 3 4 5 6 7 8 9 {10}", MsgHelp.format(
                "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10}",
                new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8",
                        "9", "10" }));
        
        try {
            MsgHelp.format(null, new Object[0]);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
        
        try {
            MsgHelp.format("fixture", null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

}
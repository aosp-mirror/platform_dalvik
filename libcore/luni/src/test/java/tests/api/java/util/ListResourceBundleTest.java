/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.java.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

public class ListResourceBundleTest extends junit.framework.TestCase {

    /**
     * @tests java.util.ListResourceBundle#getKeys()
     */
    public void test_getKeys() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        Enumeration keys = bundle.getKeys();
        Vector result = new Vector();
        while (keys.hasMoreElements()) {
            result.addElement(keys.nextElement());
        }
        assertTrue("Missing key parent1", result.contains("parent1"));
        assertTrue("Missing key parent2", result.contains("parent2"));
        assertTrue("Missing key parent3", result.contains("parent3"));
        assertTrue("Missing key parent4", result.contains("parent4"));
        assertTrue("Missing key child1", result.contains("child1"));
        assertTrue("Missing key child2", result.contains("child2"));
        assertTrue("Missing key child3", result.contains("child3"));
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }
}

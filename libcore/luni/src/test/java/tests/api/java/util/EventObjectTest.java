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

import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass; 

import java.util.EventObject;

@TestTargetClass(EventObject.class) 
public class EventObjectTest extends junit.framework.TestCase {

    Object myObject;

    EventObject myEventObject;

    /**
     * @tests java.util.EventObject#EventObject(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "See setUp method.",
        method = "EventObject",
        args = {java.lang.Object.class}
    )
    public void test_ConstructorLjava_lang_Object() {
        try {
            new EventObject(null);
            fail ("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    /**
     * @tests java.util.EventObject#getSource()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSource",
        args = {}
    )
    public void test_getSource() {
        // Test for method java.lang.Object java.util.EventObject.getSource()
        assertTrue("Wrong source returned",
                myEventObject.getSource() == myObject);
    }

    /**
     * @tests java.util.EventObject#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        // Test for method java.lang.String java.util.EventObject.toString()
        assertTrue("Incorrect toString returned: " + myEventObject.toString(),
                myEventObject.toString().indexOf(
                        "java.util.EventObject[source=java.lang.Object@") == 0);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        myObject = new Object();
        myEventObject = new EventObject(myObject);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}

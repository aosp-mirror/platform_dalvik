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

package org.apache.harmony.luni.tests.java.lang;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(Number.class) 
public class NumberTest extends junit.framework.TestCase {
    
    class MockNumber extends Number {

        @Override
        public double doubleValue() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float floatValue() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int intValue() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long longValue() {
            // TODO Auto-generated method stub
            return 0;
        }
        
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Number",
        args = {}
    )
    public void test_Number() {
        MockNumber number = new MockNumber();
        assertEquals(0, number.longValue());
        assertEquals(0, number.shortValue());
    }
    
    /**
     * @tests java.lang.Number#byteValue()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "byteValue",
        args = {}
    )
    public void test_byteValue() {
        int number = 1231243;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
        number = 0;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
        number = -1;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
        number = -84109328;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
    }

    /**
     * @tests java.lang.Number#shortValue()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "shortValue",
        args = {}
    )
    public void test_shortValue() {
        int number = 1231243;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
        number = 0;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
        number = -1;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
        number = -84109328;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());

    }
}

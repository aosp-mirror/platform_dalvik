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

package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@TestTargetClass(Map.Entry.class)
public class MapEntryTest extends TestCase {
    Map.Entry me = null;
    HashMap   hm = null;
    Iterator  i  = null;
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKey",
        args = {}
    )
    public void testGetKey() {
        assertTrue(hm.containsKey(me.getKey()));
        hm.clear();
        try {
            me.getKey();
            //expected
        } catch (IllegalStateException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {}
    )
    public void testGetValue() {
        assertTrue(hm.containsValue(me.getValue()));
        hm.clear();
        try {
            me.getValue();
            //expected
        } catch (IllegalStateException e) {
            //expected
        }
    }

    class Mock_HashMap extends HashMap {
        @Override
        public Object put(Object key, Object val) {
            if (val == null) throw new NullPointerException();
            if (val.getClass() == Double.class) throw new ClassCastException();
            if (((String)val).equals("Wrong element")) throw new IllegalArgumentException();
            throw new UnsupportedOperationException();
        }
        
        public Object fakePut(Object key, Object val) {
            return super.put(key, val);
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "RI does not throw following exceptions: UnsupportedOperationException,ClassCastException, IllegalArgumentException and NullPointerException",
        method = "setValue",
        args = {java.lang.Object.class}
    )
    public void testSetValue() {
        Mock_HashMap mhm = new Mock_HashMap();
        
        mhm.fakePut(new Integer(1), "One");
        mhm.fakePut(new Integer(2), "Two");
        
        i = mhm.entrySet().iterator();
        me = (Map.Entry)i.next();
        
        me.setValue("Wrong element");
        
        hm.clear();
        try {
            me.setValue("");
            //expected
        } catch (IllegalStateException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        Map.Entry me1 = (Map.Entry)i.next();
        assertFalse(me.equals(me1));
        assertFalse(me.equals(this));
        me1 = me;
        assertTrue(me.equals(me1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        Map.Entry me1 = (Map.Entry)i.next();
        assertTrue(me.hashCode() != me1.hashCode());
    }

    protected void setUp() throws Exception {
        hm = new HashMap();
        
        hm.put(new Integer(1), "one");
        hm.put(new Integer(2), "two");
        
        i = hm.entrySet().iterator();
        me = (Map.Entry)i.next();
        
        super.setUp();
    }
}

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

package org.apache.harmony.prefs.tests.java.util.prefs;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.NotSerializableException;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * 
 */
@TestTargetClass(PreferenceChangeEvent.class)
public class PreferenceChangeEventTest extends TestCase {

    PreferenceChangeEvent event;

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Checks exception.",
        method = "PreferenceChangeEvent",
        args = {java.util.prefs.Preferences.class, java.lang.String.class, java.lang.String.class}
    )
    public void testPreferenceChangeEventException() {
        try {
            event = new PreferenceChangeEvent(null, "key", "value");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PreferenceChangeEvent",
        args = {java.util.prefs.Preferences.class, java.lang.String.class, java.lang.String.class}
    )
    public void testConstructorNullValue() {
        event = new PreferenceChangeEvent(Preferences.userRoot(), "key", null);
        assertEquals("key", event.getKey());
        assertNull(event.getNewValue());
        assertSame(Preferences.userRoot(), event.getNode());
        assertSame(Preferences.userRoot(), event.getSource());

        event = new PreferenceChangeEvent(Preferences.userRoot(), "", null);
        assertEquals("", event.getKey());
        assertNull(event.getNewValue());
        assertSame(Preferences.userRoot(), event.getNode());
        assertSame(Preferences.userRoot(), event.getSource());

        event = new PreferenceChangeEvent(Preferences.userRoot(), null, "value");
        assertNull(event.getKey());
        assertEquals("value", event.getNewValue());
        assertSame(Preferences.userRoot(), event.getNode());
        assertSame(Preferences.userRoot(), event.getSource());

        event = new PreferenceChangeEvent(Preferences.userRoot(), null, "");
        assertNull(event.getKey());
        assertEquals("", event.getNewValue());
        assertSame(Preferences.userRoot(), event.getNode());
        assertSame(Preferences.userRoot(), event.getSource());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PreferenceChangeEvent",
        args = {java.util.prefs.Preferences.class, java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor() {
        event = new PreferenceChangeEvent(Preferences.userRoot(), "key",
                "value");
        assertEquals("key", event.getKey());
        assertEquals("value", event.getNewValue());
        assertSame(Preferences.userRoot(), event.getNode());
        assertSame(Preferences.userRoot(), event.getSource());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!Serialization",
        args = {}
    )
    public void testSerialization() throws Exception {
        event = new PreferenceChangeEvent(Preferences.userRoot(), "key",
                "value");
        try {
            SerializationTest.copySerializable(event);
            fail("No expected NotSerializableException");
        } catch (NotSerializableException e) {
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test is correct, functionality checked in separate Mock class.",
        method = "getKey",
        args = {}
    )
    public void testGetKey() {
        AbstractPreferences parent = (AbstractPreferences) Preferences
                .userNodeForPackage(Preferences.class);

        AbstractPreferences pref = (AbstractPreferences) parent.node("mock");

        MockPreferenceChangeListener pl = new MockPreferenceChangeListener(
                MockPreferenceChangeListener.TEST_GET_KEY);
        pref.addPreferenceChangeListener(pl);
        try {
            pref.putInt("key_int", Integer.MAX_VALUE);
            assertEquals(1, pl.getChanged());
            assertTrue(pl.getResult());
            pl.reset();
        } finally {
            pref.removePreferenceChangeListener(pl);
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test is correct, functionality checked in separate Mock class.",
        method = "getNewValue",
        args = {}
    )
    public void testGetNewValue() {
        AbstractPreferences parent = (AbstractPreferences) Preferences
                .userNodeForPackage(Preferences.class);

        AbstractPreferences pref = (AbstractPreferences) parent.node("mock");

        MockPreferenceChangeListener pl = new MockPreferenceChangeListener(
                MockPreferenceChangeListener.TEST_GET_NEW_VALUE);
        pref.addPreferenceChangeListener(pl);
        try {
            pref.putInt("key_int", Integer.MAX_VALUE);
            assertEquals(1, pl.getChanged());
            assertTrue(pl.getResult());
            pl.reset();
            
            pref.putInt("key_int", Integer.MAX_VALUE);
            assertEquals(1, pl.getChanged());
            assertTrue(pl.getResult());
            pl.reset();
        } finally {
            pref.removePreferenceChangeListener(pl);
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test is correct, functionality checked in separate Mock class.",
        method = "getNode",
        args = {}
    )
    public void testGetNode() {
        AbstractPreferences parent = (AbstractPreferences) Preferences
                .userNodeForPackage(Preferences.class);

        AbstractPreferences pref = (AbstractPreferences) parent.node("mock");

        MockPreferenceChangeListener pl = new MockPreferenceChangeListener(
                MockPreferenceChangeListener.TEST_GET_NODE);
        pref.addPreferenceChangeListener(pl);
        try {
            pref.putInt("key_int", Integer.MAX_VALUE);
            assertEquals(1, pl.getChanged());
            assertTrue(pl.getResult());
            pl.reset();

        } finally {
            pref.removePreferenceChangeListener(pl);
        }
    }

}

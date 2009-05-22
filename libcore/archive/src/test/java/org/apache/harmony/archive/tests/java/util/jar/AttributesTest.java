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

package org.apache.harmony.archive.tests.java.util.jar;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import junit.framework.TestCase;

@TestTargetClass(Attributes.class)
public class AttributesTest extends TestCase {
    private Attributes a;

    @Override
    protected void setUp() {
        a = new Attributes();
        a.putValue("1", "one");
        a.putValue("2", "two");
        a.putValue("3", "three");
        a.putValue("4", "four");
    }

    /**
     * @tests java.util.jar.Attributes#Attributes(java.util.jar.Attributes)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Attributes",
        args = {java.util.jar.Attributes.class}
    )
    public void test_ConstructorLjava_util_jar_Attributes() {
        Attributes a2 = new Attributes(a);
        assertEquals(a, a2);
        a.putValue("1", "one(1)");
        assertTrue("equal", !a.equals(a2));
    }

    /**
     * @tests java.util.jar.Attributes#clear()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        a.clear();
        assertNull("a) All entries should be null after clear", a.get("1"));
        assertNull("b) All entries should be null after clear", a.get("2"));
        assertNull("c) All entries should be null after clear", a.get("3"));
        assertNull("d) All entries should be null after clear", a.get("4"));
        assertTrue("Should not contain any keys", !a.containsKey("1"));
    }

    /**
     * @tests java.util.jar.Attributes#containsKey(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsKey",
        args = {java.lang.Object.class}
    )
    public void test_containsKeyLjava_lang_Object() {
        assertTrue("a) Should have returned false", !a.containsKey(new Integer(
                1)));
        assertTrue("b) Should have returned false", !a.containsKey("0"));
        assertTrue("Should have returned true", a
                .containsKey(new Attributes.Name("1")));
    }

    /**
     * @tests java.util.jar.Attributes#containsValue(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsValue",
        args = {java.lang.Object.class}
    )
    public void test_containsValueLjava_lang_Object() {
        assertTrue("Should have returned false", !a.containsValue("One"));
        assertTrue("Should have returned true", a.containsValue("one"));
    }

    /**
     * @tests java.util.jar.Attributes#entrySet()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
    public void test_entrySet() {
        Set<Map.Entry<Object, Object>> entrySet = a.entrySet();
        Set<Object> keySet = new HashSet<Object>();
        Set<Object> valueSet = new HashSet<Object>();
        Iterator<?> i;
        assertEquals(4, entrySet.size());
        i = entrySet.iterator();
        while (i.hasNext()) {
            java.util.Map.Entry<?, ?> e;
            e = (Map.Entry<?, ?>) i.next();
            keySet.add(e.getKey());
            valueSet.add(e.getValue());
        }
        assertTrue("a) Should contain entry", valueSet.contains("one"));
        assertTrue("b) Should contain entry", valueSet.contains("two"));
        assertTrue("c) Should contain entry", valueSet.contains("three"));
        assertTrue("d) Should contain entry", valueSet.contains("four"));
        assertTrue("a) Should contain key", keySet
                .contains(new Attributes.Name("1")));
        assertTrue("b) Should contain key", keySet
                .contains(new Attributes.Name("2")));
        assertTrue("c) Should contain key", keySet
                .contains(new Attributes.Name("3")));
        assertTrue("d) Should contain key", keySet
                .contains(new Attributes.Name("4")));
    }

    /**
     * @tests java.util.jar.Attributes#get(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {java.lang.String.class}
    )
    public void test_getLjava_lang_Object() {
        assertEquals("a) Incorrect value returned", "one", a.getValue("1"));
        assertNull("b) Incorrect value returned", a.getValue("0"));

        try {
            a.getValue("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
            // expected
        }
    }

    /**
     * @tests java.util.jar.Attributes#isEmpty()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        assertTrue("Should not be empty", !a.isEmpty());
        a.clear();
        assertTrue("a) Should be empty", a.isEmpty());
        a = new Attributes();
        assertTrue("b) Should be empty", a.isEmpty());
    }

    /**
     * @tests java.util.jar.Attributes#keySet()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
    public void test_keySet() {
        Set<?> s = a.keySet();
        assertEquals(4, s.size());
        assertTrue("a) Should contain entry", s.contains(new Attributes.Name(
                "1")));
        assertTrue("b) Should contain entry", s.contains(new Attributes.Name(
                "2")));
        assertTrue("c) Should contain entry", s.contains(new Attributes.Name(
                "3")));
        assertTrue("d) Should contain entry", s.contains(new Attributes.Name(
                "4")));
    }

    /**
     * @tests java.util.jar.Attributes#putAll(java.util.Map)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAllLjava_util_Map() {
        Attributes b = new Attributes();
        b.putValue("3", "san");
        b.putValue("4", "shi");
        b.putValue("5", "go");
        b.putValue("6", "roku");
        a.putAll(b);
        assertEquals("Should not have been replaced", "one", a.getValue("1"));
        assertEquals("Should have been replaced", "san", a.getValue("3"));
        assertEquals("Should have been added", "go", a.getValue("5"));
        Attributes atts = new Attributes();
        assertNull("Assert 0: ", atts.put(Attributes.Name.CLASS_PATH,
                "tools.jar"));
        assertNull("Assert 1: ", atts
                .put(Attributes.Name.MANIFEST_VERSION, "1"));
        Attributes atts2 = new Attributes();
        atts2.putAll(atts);
        assertEquals("Assert 2:", "tools.jar", atts2
                .get(Attributes.Name.CLASS_PATH));
        assertEquals("Assert 3: ", "1", atts2
                .get(Attributes.Name.MANIFEST_VERSION));
        try {
            atts.putAll(Collections.EMPTY_MAP);
            fail("Assert 4: no class cast from attrib parameter");
        } catch (ClassCastException e) {
            // Expected
        }
    }

    /**
     * @tests java.util.jar.Attributes#putAll(java.util.Map)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAllLjava_util_Map2() {
        // Regression for HARMONY-464
        try {
            new Attributes().putAll((Map) null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
        // verify that special care for null is done in the Attributes.putAll()
        // method
        try {
            new Attributes() {
                @Override
                public void putAll(Map<?, ?> attrib) {
                    map.putAll(attrib);
                }
            }.putAll((Map<?, ?>) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.util.jar.Attributes#remove(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        a.remove(new Attributes.Name("1"));
        a.remove(new Attributes.Name("3"));
        assertNull("Should have been removed", a.getValue("1"));
        assertEquals("Should not have been removed", "four", a.getValue("4"));
    }

    /**
     * @tests java.util.jar.Attributes#size()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        assertEquals("Incorrect size returned", 4, a.size());
        a.clear();
        assertEquals(0, a.size());
    }

    /**
     * @tests java.util.jar.Attributes#values()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_values() {
        Collection<?> valueCollection = a.values();
        assertTrue("a) Should contain entry", valueCollection.contains("one"));
        assertTrue("b) Should contain entry", valueCollection.contains("two"));
        assertTrue("c) Should contain entry", valueCollection.contains("three"));
        assertTrue("d) Should contain entry", valueCollection.contains("four"));
    }

    /**
     * @tests java.util.jar.Attributes#clone()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        Attributes a2 = (Attributes) a.clone();
        assertEquals(a, a2);
        a.putValue("1", "one(1)");
        assertTrue("equal", !a.equals(a2));
    }

    /**
     * @tests java.util.jar.Attributes#equals(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        Attributes.Name n1 = new Attributes.Name("name"), n2 = new Attributes.Name(
                "Name");
        assertEquals(n1, n2);
        Attributes a1 = new Attributes();
        a1.putValue("one", "1");
        a1.putValue("two", "2");
        Attributes a2 = new Attributes();
        a2.putValue("One", "1");
        a2.putValue("TWO", "2");
        assertEquals(a1, a2);
    }

    /**
     * @tests java.util.jar.Attributes.put(java.lang.Object, java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test. Checks ClassCastException",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        Attributes atts = new Attributes();
        assertNull("Assert 0: ", atts.put(Attributes.Name.CLASS_PATH,
                "tools.jar"));
        assertEquals("Assert 1: ", "tools.jar", atts
                .getValue(Attributes.Name.CLASS_PATH));
        // Regression for HARMONY-79
        try {
            atts.put("not a name", "value");
            fail("Assert 2: no class cast from key parameter");
        } catch (ClassCastException e) {
            // Expected
        }
        try {
            atts.put(Attributes.Name.CLASS_PATH, Boolean.TRUE);
            fail("Assert 3: no class cast from value parameter");
        } catch (ClassCastException e) {
            // Expected
        }
    }

    /**
     * @tests java.util.jar.Attributes.put(java.lang.Object, java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "ClassCastException checking missed.",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_putLjava_lang_ObjectLjava_lang_Object_Null() {

        Attributes attribute = new Attributes();

        assertFalse(attribute.containsKey(null));
        assertFalse(attribute.containsValue(null));
        attribute.put(null, null);
        attribute.put(null, null);
        assertEquals(1, attribute.size());
        assertTrue(attribute.containsKey(null));
        assertTrue(attribute.containsValue(null));
        assertNull(attribute.get(null));

        String value = "It's null";
        attribute.put(null, value);
        assertEquals(1, attribute.size());
        assertEquals(value, attribute.get(null));

        Attributes.Name name = new Attributes.Name("null");
        attribute.put(name, null);
        assertEquals(2, attribute.size());
        assertNull(attribute.get(name));
    }

    /**
     * @tests java.util.jar.Attributes.hashCode()
     */
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "hashCode",
            args = {}
    )
    public void test_hashCode_consistent_with_map() {
        MockAttributes mockAttr = new MockAttributes();
        mockAttr.putValue("1", "one");
        assertEquals(mockAttr.getMap().hashCode(), mockAttr.hashCode());
    }

    private static class MockAttributes extends Attributes {
        public Map<Object, Object> getMap() {
            return map;
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Attributes",
        args = {}
    )
    public void test_Constructor() {
        Attributes attr = new Attributes();
        assertTrue(attr.size() >= 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Attributes",
        args = {int.class}
    )
    public void test_ConstructorI() {
        Attributes attr = new Attributes(10);
        assertTrue(attr.size() >= 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {java.lang.Object.class}
    )
    public void test_getLjava_lang_Object_true() {
        assertEquals("a) Incorrect value returned", "one", a
                .get(new Attributes.Name("1")));
        assertNull("b) Incorrect value returned", a.get("0"));
        assertNull("b) Incorrect value returned", a.get("1"));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {java.util.jar.Attributes.Name.class}
    )
    public void test_getValueLjava_util_jar_Attributes_Name() {
        assertEquals("a) Incorrect value returned", "one", a
                .getValue(new Attributes.Name("1")));
        assertNull("b) Incorrect value returned", a
                .getValue(new Attributes.Name("0")));
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        Attributes b = (Attributes) a.clone();
        b.putValue("33", "Thirty three");
        assertNotSame(a.hashCode(), b.hashCode());
        b = (Attributes) a.clone();
        b.clear();
        assertNotSame(a.hashCode(), b.hashCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "putValue",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_putValueLjava_lang_StringLjava_lang_String() {
        Attributes b = new Attributes();
        b.put(new Attributes.Name("1"), "one");
        b.putValue("2", "two");
        b.put(new Attributes.Name("3"), "three");
        b.putValue("4", "four");

        assertTrue(a.equals(b));

        try {
            b.putValue(null, "null");
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            // expected
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 0x10000; i++) {
            sb.append('3');
        }
        try {
            b.putValue(new String(sb), "wrong name");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
            // expected
        }
    }
}

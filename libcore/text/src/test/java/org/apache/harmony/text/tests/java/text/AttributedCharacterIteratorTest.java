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

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@TestTargetClass(AttributedCharacterIterator.class) 
public class AttributedCharacterIteratorTest extends junit.framework.TestCase {
    
    AttributedCharacterIterator it;
    String string = "test test";

    /**z dxthf jgznm rff
     * @tests java.text.AttributedCharacterIterator#current()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "current",
        args = {}
    )
    public void test_current() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        assertEquals("Wrong first", 'T', it.current());
        it.next();
        assertEquals("Wrong second", 'e', it.current());
        for (int i = 0; i < 9; i++)
            it.next();
        assertEquals("Wrong last", 'g', it.current());
        it.next();
        assertTrue("Wrong final", it.current() == CharacterIterator.DONE);

        it = attrString.getIterator(null, 2, 8);
        assertEquals("Wrong first2", 's', it.current());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#first()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "first",
        args = {}
    )
    public void test_first() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        assertEquals("Wrong first1", 'T', it.first());
        it = attrString.getIterator(null, 0, 3);
        assertEquals("Wrong first2", 'T', it.first());
        it = attrString.getIterator(null, 2, 8);
        assertEquals("Wrong first3", 's', it.first());
        it = attrString.getIterator(null, 11, 11);
        assertTrue("Wrong first4", it.first() == CharacterIterator.DONE);
    }

    /**
     * @tests java.text.AttributedCharacterIterator#getBeginIndex()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBeginIndex",
        args = {}
    )
    public void test_getBeginIndex() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator(null, 2, 6);
        assertEquals("Wrong begin index", 2, it.getBeginIndex());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#getEndIndex()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEndIndex",
        args = {}
    )
    public void test_getEndIndex() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator(null, 2, 6);
        assertEquals("Wrong begin index", 6, it.getEndIndex());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#getIndex()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIndex",
        args = {}
    )
    public void test_getIndex() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        assertEquals("Wrong first", 0, it.getIndex());
        it.next();
        assertEquals("Wrong second", 1, it.getIndex());
        for (int i = 0; i < 9; i++)
            it.next();
        assertEquals("Wrong last", 10, it.getIndex());
        it.next();
        assertEquals("Wrong final", 11, it.getIndex());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#last()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "last",
        args = {}
    )
    public void test_last() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        assertEquals("Wrong last1", 'g', it.last());
        it = attrString.getIterator(null, 0, 3);
        assertEquals("Wrong last2", 's', it.last());
        it = attrString.getIterator(null, 2, 8);
        assertEquals("Wrong last3", 'r', it.last());
        it = attrString.getIterator(null, 0, 0);
        assertTrue("Wrong last4", it.last() == CharacterIterator.DONE);
    }

    /**
     * @tests java.text.AttributedCharacterIterator#next()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {}
    )
    public void test_next() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        assertEquals("Wrong first", 'e', it.next());
        for (int i = 0; i < 8; i++)
            it.next();
        assertEquals("Wrong last", 'g', it.next());
        assertTrue("Wrong final", it.next() == CharacterIterator.DONE);

        it = attrString.getIterator(null, 2, 8);
        assertEquals("Wrong first2", 't', it.next());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#previous()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "previous",
        args = {}
    )
    public void test_previous() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        it.setIndex(11);
        assertEquals("Wrong first", 'g', it.previous());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#setIndex(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIndex",
        args = {int.class}
    )
    public void test_setIndexI() {
        String test = "Test 23ring";
        AttributedString attrString = new AttributedString(test);
        AttributedCharacterIterator it = attrString.getIterator();
        it.setIndex(5);
        assertEquals("Wrong first", '2', it.current());
    }

    /**
     * @tests java.text.AttributedCharacterIterator#getRunLimit(java.text.AttributedCharacterIterator$Attribute)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRunLimit",
        args = {java.util.Set.class}
    )
    public void test_getRunLimitLSet() {
        AttributedString as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "a", 2,
                3);
        AttributedCharacterIterator it = as.getIterator();
        HashSet<AttributedCharacterIterator.Attribute>  attr = 
            new HashSet<AttributedCharacterIterator.Attribute>();
        attr.add(AttributedCharacterIterator.Attribute.LANGUAGE);
        assertEquals("non-null value limit",
                2, it.getRunLimit(attr));

        as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, null,
                2, 3);
        it = as.getIterator();
        assertEquals("null value limit",
                4, it.getRunLimit(attr));
        
        attr.add(AttributedCharacterIterator.Attribute.READING);
        assertEquals("null value limit",
                4, it.getRunLimit(attr));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAllAttributeKeys",
        args = {}
    )
    public void test_getAllAttributeKeys() {
        AttributedString as = new AttributedString("test");
        AttributedCharacterIterator it = as.getIterator();
        Set<AttributedCharacterIterator.Attribute> emptyAttributes = 
            it.getAllAttributeKeys();
        assertTrue(emptyAttributes.isEmpty());
        
        int attrCount = 10;
        for(int i = 0 ; i < attrCount; i++) {
            as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                    "a");
        }
        it = as.getIterator();
        Set<AttributedCharacterIterator.Attribute> attributes = 
            it.getAllAttributeKeys();
        for(AttributedCharacterIterator.Attribute attr:attributes) {
            assertEquals(AttributedCharacterIterator.Attribute.LANGUAGE, attr);
        }
    } 

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAttribute",
        args = {java.text.AttributedCharacterIterator.Attribute.class}
    )
    public void test_getAttributeLAttributedCharacterIterator_Attribute() {
        
        Object attribute = 
            it.getAttribute(AttributedCharacterIterator.Attribute.LANGUAGE);
        assertEquals("ENGLISH", attribute);
        
        attribute = 
            it.getAttribute(AttributedCharacterIterator.Attribute.READING);
        assertEquals("READ", attribute);
        
        assertNull(it.getAttribute(AttributedCharacterIterator.
                Attribute.INPUT_METHOD_SEGMENT));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAttributes",
        args = {}
    )
    public void test_getAttributes() {
        Map<AttributedCharacterIterator.Attribute, Object> attributes = 
            it.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals("ENGLISH", 
                attributes.get(AttributedCharacterIterator.Attribute.LANGUAGE));
        assertEquals("READ", 
                attributes.get(AttributedCharacterIterator.Attribute.READING));
        
        AttributedString as = new AttributedString("test");
        assertTrue(as.getIterator().getAttributes().isEmpty());
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getRunLimit",
        args = {}
    )
    public void test_getRunLimit() {
        int limit = it.getRunLimit();
        assertEquals(string.length(), limit);
        
        AttributedString as = new AttributedString("");
        assertEquals(0, as.getIterator().getRunLimit());
        
        as = new AttributedString(new AttributedString("test text").
                getIterator(), 2, 7);
        
        AttributedCharacterIterator it = as.getIterator();
        assertEquals(5, it.getRunLimit());
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRunLimit",
        args = {java.text.AttributedCharacterIterator.Attribute.class}
    )
    public void test_getRunLimitLAttribute() {
        AttributedString as = new AttributedString("");
        assertEquals(0, as.getIterator().getRunLimit(
                AttributedCharacterIterator.Attribute.LANGUAGE));
        
        as = new AttributedString("text");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                "ENGLISH");
        
        as.addAttribute(AttributedCharacterIterator.Attribute.READING, 
        "READ", 1, 3);
        
        assertEquals(4, as.getIterator().getRunLimit(
                AttributedCharacterIterator.Attribute.LANGUAGE));
        
        assertEquals(1, as.getIterator().getRunLimit(
                AttributedCharacterIterator.Attribute.READING));        
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getRunStart",
        args = {}
    )
    public void test_getRunStart() {
        assertEquals(0, it.getRunStart());
        
        AttributedString as = new AttributedString("");
        assertEquals(0, as.getIterator().getRunStart());
        
        as = new AttributedString(new AttributedString("test text").
                getIterator(), 2, 7);
        
        AttributedCharacterIterator it = as.getIterator();
        
        assertEquals(0, it.getRunStart());
        
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                "GERMAN", 1, 2);
        as.addAttribute(AttributedCharacterIterator.Attribute.READING, 
                "READ", 1, 3);
        assertEquals(0, as.getIterator().getRunStart());
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRunStart",
        args = {java.text.AttributedCharacterIterator.Attribute.class}
    )
    public void test_getRunStartLAttribute() {
        assertEquals(0, it.getRunStart(
                AttributedCharacterIterator.Attribute.LANGUAGE));
        
        AttributedString as = new AttributedString("test text");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                            "GERMAN", 2, 5);
        as.addAttribute(AttributedCharacterIterator.Attribute.READING, 
                            "READ", 2, 7);
        
        assertEquals(0, as.getIterator().getRunStart(
                AttributedCharacterIterator.Attribute.LANGUAGE));
        assertEquals(0, as.getIterator().getRunStart(
                AttributedCharacterIterator.Attribute.READING));      
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRunStart",
        args = {java.util.Set.class}
    )
    public void test_getRunStartLjava_util_Set() {
        AttributedString as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "a", 2,
                3);
        AttributedCharacterIterator it = as.getIterator();
        HashSet<AttributedCharacterIterator.Attribute>  attr = 
            new HashSet<AttributedCharacterIterator.Attribute>();
        attr.add(AttributedCharacterIterator.Attribute.LANGUAGE);
        assertEquals(0, it.getRunStart(attr));

        as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                "ENGLISH",1, 3);
        it = as.getIterator();
        assertEquals(0, it.getRunStart(attr));
        
        attr.add(AttributedCharacterIterator.Attribute.READING);
        assertEquals(0, it.getRunStart(attr));      
        
        
    }
    
    protected void setUp() {
        
        AttributedString as = new AttributedString(string);
             
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                "GERMAN");
        as.addAttribute(AttributedCharacterIterator.Attribute.READING, 
                "READ");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, 
                "ENGLISH");        
        
        it = as.getIterator();
    }

    protected void tearDown() {
    }
}

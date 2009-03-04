/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.org.xml.sax.helpers;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

@TestTargetClass(XMLReaderFactory.class)
public class XMLReaderFactoryTest extends TestCase {

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "createXMLReader",
        args = { },
        notes = "Checks everything except META-INF case"
    )
    public void testCreateXMLReader() {
        // Property not set at all
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // Expected
        }

        // Unknown class
        System.setProperty("org.xml.sax.driver", "foo.bar.XMLReader");
        
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // Expected
        }
        
        // Non-accessible class
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.NoAccessXMLReader");
        
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // Expected
        }
        
        // Non-instantiable class
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.NoInstanceXMLReader");
        
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // Expected
        }
        
        // Non-XMLReader class
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.NoSubclassXMLReader");
        
        try {
            XMLReaderFactory.createXMLReader();
        } catch (ClassCastException e) {
            // Expected
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Good one, finally
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.DoNothingXMLReader");
        
        try {
            XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "createXMLReader",
        args = { String.class }
    )
    public void testMakeParserString() {
        // No class
        try {
            XMLReaderFactory.createXMLReader(null);
        } catch (NullPointerException e) {
            // Expected
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // Unknown class
        try {
            XMLReaderFactory.createXMLReader("foo.bar.XMLReader");
        } catch (SAXException e) {
            // Expected
        }
        
        // Non-accessible class
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.NoAccessXMLReader");
        } catch (SAXException e) {
            // Expected
        }
        
        // Non-instantiable class
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.NoInstanceXMLReader");
        } catch (SAXException e) {
            // Expected
        }
        
        // Non-Parser class
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.NoSubclassXMLReader");
        } catch (SAXException e) {
            // Expected
        }
        
        // Good one, finally
        try {
            XMLReaderFactory.createXMLReader(
                    "tests.api.org.xml.sax.support.DoNothingXMLReader");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }

    }

}

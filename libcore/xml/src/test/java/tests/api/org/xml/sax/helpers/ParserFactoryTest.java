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

import org.xml.sax.helpers.ParserFactory;

@SuppressWarnings("deprecation")
@TestTargetClass(ParserFactory.class)
public class ParserFactoryTest extends TestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "makeParser",
        args = { },
        notes = "Checks everything except META-INF case"
    )
    public void testMakeParser() {
        // Property not set at all
        try {
            ParserFactory.makeParser();
        } catch (NullPointerException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // Unknown class
        System.setProperty("org.xml.sax.parser", "foo.bar.SAXParser");
        
        try {
            ParserFactory.makeParser();
        } catch (ClassNotFoundException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Non-accessible class
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.NoAccessParser");
        
        try {
            ParserFactory.makeParser();
        } catch (IllegalAccessException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Non-instantiable class
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.NoInstanceParser");
        
        try {
            ParserFactory.makeParser();
        } catch (InstantiationException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Non-Parser class
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.NoSubclassParser");
        
        try {
            ParserFactory.makeParser();
        } catch (ClassCastException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Good one, finally
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.DoNothingParser");
        
        try {
            ParserFactory.makeParser();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "makeParser",
        args = { String.class }
    )
    public void testMakeParserString() {
        // No class
        try {
            ParserFactory.makeParser(null);
        } catch (NullPointerException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // Unknown class
        try {
            ParserFactory.makeParser("foo.bar.SAXParser");
        } catch (ClassNotFoundException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Non-accessible class
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.NoAccessParser");
        } catch (IllegalAccessException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Non-instantiable class
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.NoInstanceParser");
        } catch (InstantiationException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Non-Parser class
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.NoSubclassParser");
        } catch (ClassCastException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        
        // Good one, finally
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.DoNothingParser");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

    }

}

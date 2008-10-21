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

import java.util.prefs.InvalidPreferencesFormatException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * 
 */
public class InvalidPreferencesFormatExceptionTest extends TestCase {

    /*
     * Class under test for void InvalidPreferencesFormatException(String)
     */
    public void testInvalidPreferencesFormatExceptionString() {
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException(
                "msg");
        assertNull(e.getCause());
        assertEquals("msg", e.getMessage());
    }

    /*
     * Class under test for void InvalidPreferencesFormatException(String,
     * Throwable)
     */
    public void testInvalidPreferencesFormatExceptionStringThrowable() {
        Throwable t = new Throwable("root");
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException(
                "msg", t);
        assertSame(t, e.getCause());
        assertTrue(e.getMessage().indexOf("root") < 0);
        assertTrue(e.getMessage().indexOf(t.getClass().getName()) < 0);
        assertTrue(e.getMessage().indexOf("msg") >= 0);
    }

    /*
     * Class under test for void InvalidPreferencesFormatException(Throwable)
     */
    public void testInvalidPreferencesFormatExceptionThrowable() {
        Throwable t = new Throwable("root");
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException(
                t);
        assertSame(t, e.getCause());
        assertTrue(e.getMessage().indexOf("root") >= 0);
        assertTrue(e.getMessage().indexOf(t.getClass().getName()) >= 0);
    }

    /**
     * @tests serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new InvalidPreferencesFormatException(
                "msg"));
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new InvalidPreferencesFormatException("msg"));
    }
}

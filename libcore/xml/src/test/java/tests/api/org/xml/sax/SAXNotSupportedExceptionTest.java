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

package tests.api.org.xml.sax;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import org.xml.sax.SAXNotSupportedException;

@TestTargetClass(SAXNotSupportedException.class)
public class SAXNotSupportedExceptionTest extends TestCase {

    public static final String ERR = "Houston, we have a problem";

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotSupportedException",
        args = { }
    )
    public void testSAXNotSupportedException() {
        SAXNotSupportedException e = new SAXNotSupportedException();
        assertNull(e.getMessage());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotSupportedException",
        args = { String.class }
    )
    public void testSAXNotSupportedException_String() {
        SAXNotSupportedException e = new SAXNotSupportedException(ERR);
        assertEquals(ERR, e.getMessage());
        
        e = new SAXNotSupportedException(null);
        assertNull(e.getMessage());
    }
    
}

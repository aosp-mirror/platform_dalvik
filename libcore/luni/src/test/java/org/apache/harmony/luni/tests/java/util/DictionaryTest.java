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

import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;

@TestTargetClass(Dictionary.class)
public class DictionaryTest extends TestCase {
    
    class Mock_Dictionary extends Dictionary {

        @Override
        public Enumeration elements() {
            return null;
        }

        @Override
        public Object get(Object arg0) {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Enumeration keys() {
            return null;
        }

        @Override
        public Object put(Object arg0, Object arg1) {
            return null;
        }

        @Override
        public Object remove(Object arg0) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Dictionary",
        args = {}
    )
    public void testDictionary() {
        Dictionary md = new Mock_Dictionary();
        assertNotNull(md);
    }

}

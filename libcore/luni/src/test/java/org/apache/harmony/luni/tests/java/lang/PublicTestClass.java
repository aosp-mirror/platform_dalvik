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

package org.apache.harmony.luni.tests.java.lang;

import java.io.Serializable;

interface TestInterface {
    
    public static int TEST_INTERFACE_FIELD = 0; 

    int getCount();
    void setCount(int value);
}

@TestAnnotation("org.apache.harmony.luni.tests.java.lang.PublicTestClass")
public class PublicTestClass implements TestInterface, Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;

    public static String TEST_FIELD = "test field"; 
    
    Object clazz;
    
    public PublicTestClass() {
        class LocalClass { }
        
        clazz = new LocalClass();
    }
    
    public Object getLocalClass() {
        class LocalClass {}
        Object returnedObject = new LocalClass();
        return returnedObject;
    }

    int count = 0; 
    
    public int getCount() {
        return count;
    }

    public void setCount(int value) {
        count = value;        
    }
    
    private class PrivateClass1 {
        
        public String toString() {
            return "PrivateClass0";
        }
    }
    
    public class PrivateClass2 {
        
        public String toString() {
            return "PrivateClass1";
        }
    }    
}

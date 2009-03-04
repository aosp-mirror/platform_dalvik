/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.luni.tests.java.lang;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

@TestTargetClass(ClassNotFoundException.class) 
public class ClassNotFoundExceptionTest extends TestCase {

    /**
     * @tests java.lang.ClassNotFoundException#ClassNotFoundException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassNotFoundException",
        args = {}
    )
    public void test_Constructor() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.ClassNotFoundException#ClassNotFoundException(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassNotFoundException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ClassNotFoundException e = new ClassNotFoundException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassNotFoundException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_Throwable() {
        String testMessage = "Test Message";
        Throwable thr = new Throwable();
        ClassNotFoundException cnfe = new ClassNotFoundException(testMessage, thr);
        assertEquals(testMessage, cnfe.getMessage());
        assertEquals(thr, cnfe.getException());
        
        cnfe = new ClassNotFoundException(null, thr);
        assertNull(cnfe.getMessage());
        assertEquals(thr, cnfe.getException());
        
        cnfe = new ClassNotFoundException(testMessage, null);
        assertNull(cnfe.getException());
        assertEquals(testMessage, cnfe.getMessage());
        
        cnfe = new ClassNotFoundException(null, null);
        assertNull(cnfe.getMessage());
        assertNull(cnfe.getException());
    } 

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getCause());
        
        e = new ClassNotFoundException("Message");
        assertNull(e.getCause());
        
        NullPointerException cause = new NullPointerException();
        Throwable thr = new Throwable(cause);
        e = new ClassNotFoundException("Message", thr);
        assertEquals(thr, e.getCause());
        
        e = new ClassNotFoundException("Message", null);
        assertEquals(null, e.getCause());       
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getException",
        args = {}
    )
    public void test_getException() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertNull(e.getException());
              
        e = new ClassNotFoundException("Message");
        assertNull(e.getException());
              
        NullPointerException cause = new NullPointerException();
        Throwable thr = new Throwable(cause);
        e = new ClassNotFoundException("Message", thr);
        assertEquals(thr, e.getException());
              
        e = new ClassNotFoundException("Message", null);
        assertEquals(null, e.getException());       
    }
}

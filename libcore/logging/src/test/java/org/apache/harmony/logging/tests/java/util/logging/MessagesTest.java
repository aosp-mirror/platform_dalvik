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


package org.apache.harmony.logging.tests.java.util.logging;

import junit.framework.TestCase;

import org.apache.harmony.logging.internal.nls.Messages;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.ErrorManager;

import dalvik.annotation.AndroidOnly;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;



@TestTargetClass(Messages.class) 
public class MessagesTest extends TestCase{


        private Messages m = null;
        
        public void setUp() throws Exception{
            super.setUp();
            m = new Messages();
        
        }
        
        public void tearDown() throws Exception{
            super.tearDown();
        }
        
        
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Just check signature, cannot make use of mock, method depend on luni",
        method = "getString",
        args = {java.lang.String.class}
    )
    @AndroidOnly("harmony specific")
    public void testGetString_String() {
            m.getString(new String());
    }
        
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Juste check signature, cannot make use of mock, depend on luni",
        method = "getString",
        args = {java.lang.String.class, java.lang.Object.class}
    )
    @AndroidOnly("harmony specific")    
    public void testGetString_StringObject() {
        m.getString(new String(), new Object());
    }
        
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Juste check signature, cannot make use of mock, depend on luni",
        method = "getString",
        args = {java.lang.String.class, int.class}
    )
    @AndroidOnly("harmony specific")
    public void testGetString_StringInt() {
        m.getString(new String(), 0);
    }
        
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Juste check signature, cannot make use of mock, depend on luni",
        method = "getString",
        args = {java.lang.String.class, char.class}
    )
    @AndroidOnly("harmony specific")
    public void testGetString_StringChar() {
        m.getString(new String(), 'a');
    }
        
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Juste check signature, cannot make use of mock, depend on luni",
        method = "getString",
        args = {java.lang.String.class, java.lang.Object.class, java.lang.Object.class}
    )
    @AndroidOnly("harmony specific")
    public void testGetString_StringObjectObject() {
            m.getString(new String(), new Object(), new Object() );
    }
    
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Juste check signature, cannot make use of mock, depend on luni",
        method = "getString",
        args = {java.lang.String.class, java.lang.Object[].class}
    )
    @AndroidOnly("harmony specific")
    public void testGetString_StringObjectArray() {
            m.getString(new String(), new Object[1]);
    }
      

}

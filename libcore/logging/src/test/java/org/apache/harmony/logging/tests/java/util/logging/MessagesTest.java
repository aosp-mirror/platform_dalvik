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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
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
        
        
        @TestInfo(
          level = TestLevel.COMPLETE,
          purpose = "Just check signature, cannot make use of mock, " +
                "method depend on luni",
          targets = {
            @TestTarget(
              methodName = "getString",
              methodArgs = {java.lang.String.class}
            )
        })
        public void testGetString_String() {
                m.getString(new String());
        }
        
        @TestInfo(
                level = TestLevel.COMPLETE,
                purpose = "Juste check signature, cannot make use of mock, depend on luni",
                targets = {
                  @TestTarget(
                    methodName = "getString",
                    methodArgs = {java.lang.String.class, Object.class}
                  )
              })
        public void testGetString_StringObject() {
                m.getString(new String(),new Object());
 
        }
        
        @TestInfo(
                level = TestLevel.PARTIAL_OK,
                purpose = "Juste check signature, cannot make use of mock, depend on luni",
                targets = {
                  @TestTarget(
                    methodName = "getString",
                    methodArgs = {java.lang.String.class, int.class}
                  )
              })
        public void testGetString_StringInt() {
                m.getString(new String(),0);
        }
        
        @TestInfo(
                level = TestLevel.PARTIAL_OK,
                purpose = "Juste check signature, cannot make use of mock, depend on luni",
                targets = {
                  @TestTarget(
                    methodName = "getString",
                    methodArgs = {java.lang.String.class, char.class}
                  )
              })
        public void testGetString_StringChar() {
                m.getString(new String(), 'a');
        }
        
        @TestInfo(
                level = TestLevel.PARTIAL_OK,
                purpose = "Juste check signature, cannot make use of mock, depend on luni",
                targets = {
                  @TestTarget(
                    methodName = "getString",
                    methodArgs = {java.lang.String.class, Object.class, Object.class}
                  )
              })
        public void testGetString_StringObjectObject() {
                m.getString(new String(), new Object(), new Object() );
        }
        @TestInfo(
                level = TestLevel.PARTIAL_OK,
                purpose = "Juste check signature, cannot make use of mock, depend on luni",
                targets = {
                  @TestTarget(
                    methodName = "getString",
                    methodArgs = {java.lang.String.class, Object[].class}
                  )
              })
        public void testGetString_StringObjectArray() {
                m.getString(new String(), new Object[1]);
        }
        
      

}

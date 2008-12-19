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

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestLevel;

import junit.framework.TestCase;

import tests.util.CallVerificationStack;

import java.util.List;
import java.util.logging.LoggingMXBean;
/**
 * This testcase verifies the signature of the interface Filter.
 * 
 */
@TestTargetClass(LoggingMXBean.class) 
public class LoggingMXBeanTest extends TestCase {
    
    private MockLoggingMXBean m = null;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
         m = new MockLoggingMXBean();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
       
    
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getLoggerLevel",
          methodArgs = {String.class}
        )
    })
    public void testGetLoggerLevel() {
        assertNull(m.getLoggerLevel(null));
    }

    
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "",
            targets = {
              @TestTarget(
                methodName = "getLoggerNames",
                methodArgs = {}
              )
          })
          public void testGetLoggerNames() {
                assertNull(m.getLoggerNames());
          }
    
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "",
            targets = {
              @TestTarget(
                methodName = "getParentLoggerName",
                methodArgs = {String.class}
              )
          })
          public void testGetParentLoggerName() {
              assertNull(m.getParentLoggerName(null));
          }
    
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "",
            targets = {
              @TestTarget(
                methodName = "setLoggerLevel",
                methodArgs = {String.class, String.class}
              )
          })
          public void testSetLoggerLevel() {
            try{
                m.setLoggerLevel(null,null);
            }
            catch (Exception e){
                throw new AssertionError();
            }
          }

    /*
     * This inner class implements the interface Filter to verify the signature.
     */
    private class MockLoggingMXBean implements LoggingMXBean {

        public String getLoggerLevel(String loggerName) {
            return null;
        }

        public List<String> getLoggerNames() {
            return null;
        }

        public String getParentLoggerName(String loggerName) {
            return null;
        }

        public void setLoggerLevel(String loggerName, String levelName) {
           
        }


    }
}

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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.util.logging.LoggingPermission;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

@TestTargetClass(LoggingPermission.class) 
public class LoggingPermissionTest extends TestCase {

    /**
     * @tests serialization/deserialization compatibility.
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "",
            targets = {
                    
              @TestTarget(
                methodName = "!SerializationSelf",
                methodArgs = {}
              )
          })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new LoggingPermission("control", ""));
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "",
            targets = {
                    
              @TestTarget(
                methodName = "!SerializationGolden",
                methodArgs = {}
              )
          })
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new LoggingPermission("control",
                ""));
    }

    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "",
            targets = {
                    
              @TestTarget(
                methodName = "LoggingPermission",
                methodArgs = {String.class, String.class}
              )
          })
    public void testLoggingPermission() {
        try {
            new LoggingPermission(null, null);
            fail("should throw IllegalArgumentException");
        } catch (NullPointerException e) {
        }
        try {
            new LoggingPermission("", null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LoggingPermission("bad name", null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LoggingPermission("Control", null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LoggingPermission("control",
                    "bad action");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        
        new LoggingPermission("control", "");
        
        new LoggingPermission("control", null);
    }

}

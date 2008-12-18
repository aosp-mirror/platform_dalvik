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

package org.apache.harmony.luni.tests.java.io;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.io.ObjectStreamConstants;
import junit.framework.TestCase;
@TestTargetClass(ObjectStreamConstants.class)
public class ObjectStreamConstantsTest extends TestCase {

    /**
     * @tests java.io.ObjectStreamConstants#TC_ENUM
     */
    @TestInfo(
              level = TestLevel.COMPLETE,
              purpose = "Constant test, still many constants not tested",
              targets = {
                @TestTarget(
                  methodName = "!Constants",
                  methodArgs = {}
                )
            })
    public void test_Constants() {
        assertEquals(126, ObjectStreamConstants.TC_ENUM);
        assertEquals(16, ObjectStreamConstants.SC_ENUM);
        assertEquals(126, ObjectStreamConstants.TC_MAX);
    }
}

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

package org.apache.harmony.archive.tests.java.util.jar;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;

import java.util.jar.Attributes;
import junit.framework.TestCase;

@TestTargetClass(Attributes.Name.class) 
public class AttributesNameTest extends TestCase {

    /**
     * @tests java.util.jar.Attributes.Name#Name(java.lang.String)
     */
@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Regression test. Checks IllegalArgumentException.",
      targets = {
        @TestTarget(
          methodName = "Name",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_AttributesName_Constructor() {
        // Regression for HARMONY-85
        try {
            new Attributes.Name(
                    "01234567890123456789012345678901234567890123456789012345678901234567890");
            fail("Assert 0: should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}

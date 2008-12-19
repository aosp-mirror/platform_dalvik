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

package org.apache.harmony.text.tests.java.text;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.text.Annotation;

@TestTargetClass(Annotation.class)
public class AnnotationTest extends TestCase {

    /**
     * @tests java.text.Annotation(Object)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "Annotation",
          methodArgs = {java.lang.Object.class}
        )
    })
    public void testAnnotation() {
        assertNotNull(new Annotation(null));
        assertNotNull(new Annotation("value"));
    }

    /**
     * @tests java.text.Annotation.getValue()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getValue",
          methodArgs = {}
        )
    })
    public void testGetValue() {
        Annotation a = new Annotation(null);
        assertNull(a.getValue());
        a = new Annotation("value");
        assertEquals("value", a.getValue());
    }

    /**
     * @tests java.text.Annotation.toString()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "toString",
          methodArgs = {}
        )
    })
    public void testToString() {
        Annotation ant = new Annotation("HelloWorld");
        assertEquals("toString error.",
                "java.text.Annotation[value=HelloWorld]", ant.toString());
        assertNotNull(new Annotation(null).toString());
        assertNotNull(new Annotation("value").toString());
    }
}

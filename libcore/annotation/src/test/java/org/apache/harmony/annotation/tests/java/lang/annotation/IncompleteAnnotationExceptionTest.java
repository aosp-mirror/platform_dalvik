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

package org.apache.harmony.annotation.tests.java.lang.annotation;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.lang.annotation.IncompleteAnnotationException;

@TestTargetClass(IncompleteAnnotationException.class)
public class IncompleteAnnotationExceptionTest extends TestCase {

    /*
     * Class under test for void IncompleteAnnotationException(String)
     * Regression for HARMONY-2477
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies NullPointerException.",
      targets = {
        @TestTarget(
          methodName = "IncompleteAnnotationException",
          methodArgs = {java.lang.Class.class, java.lang.String.class}
        )
    })
    public void testNullType() {
        try {
            new IncompleteAnnotationException(null, "str");
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * @throws Exception
     * @tests java.lang.annotation.IncompleteAnnotationException#IncompleteAnnotationException(Class,
     *        String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "IncompleteAnnotationException",
          methodArgs = {java.lang.Class.class, java.lang.String.class}
        )
    })
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_Class_Ljava_lang_String()
            throws Exception {
        Class clazz = String.class;
        String elementName = "some element";
        IncompleteAnnotationException e = new IncompleteAnnotationException(
                clazz, elementName);
        assertNotNull("can not instanciate IncompleteAnnotationException", e);
        assertSame("wrong annotation type", clazz, e.annotationType());
        assertSame("wrong element name", elementName, e.elementName());
    }
}

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

package org.apache.harmony.luni.tests.java.lang;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

@TestTargetClass(EnumConstantNotPresentException.class) 
public class EnumConstantNotPresentExceptionTest extends TestCase {

    public enum Fixture {
        ONE,TWO,THREE
    }

    /**
     * @test java.lang.EnumConstantNotPresentException#EnumConstantNotPresentException(Class<?
     * extends Enum>, String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EnumConstantNotPresentException",
        args = {java.lang.Class.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_ClassLjava_lang_String() {
        String tm = "Test Message";
        EnumConstantNotPresentException ecnpe = new 
                    EnumConstantNotPresentException(Fixture.class, tm);

        assertEquals("Constant name is incorrect: " + ecnpe.constantName() + 
                " instead of " + tm, tm, ecnpe.constantName());
        
        try {
            new EnumConstantNotPresentException(null, "");
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @test java.lang.EnumConstantNotPresentException#enumType()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "enumType",
        args = {}
    )
    public void test_enumType() {
        EnumConstantNotPresentException e = new EnumConstantNotPresentException(Fixture.class, "FOUR");
        assertEquals(Fixture.class, e.enumType());
    }

    /**
     * @test java.lang.EnumConstantNotPresentException#constantName()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "constantName",
        args = {}
    )
    public void test_constantName() {
        EnumConstantNotPresentException e = new EnumConstantNotPresentException(Fixture.class, "FOUR");
        assertEquals("FOUR", e.constantName());
    }

}

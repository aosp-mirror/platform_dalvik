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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

@TestTargetClass(Compiler.class) 
public class CompilerTest extends TestCase {

    /**
     * @tests java.lang.Compiler#command(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "command",
        args = {java.lang.Object.class}
    )
    public void test_commandLjava_lang_Object() {
       
        if(System.getProperty("java.compiler") != null) {
            try {
                assertNull("Incorrect behavior.", Compiler.command(new Object()));
            } catch (Exception e) {
                fail("Exception during test : " + e.getMessage());
            }
            // NullPointerException is not specified.
            Compiler.command(null);
        } else {
            Compiler.command("");
        }
    }

    /**
     * @tests java.lang.Compiler#compileClass(java.lang.Class)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compileClass",
        args = {java.lang.Class.class}
    )
    public void test_compileClassLjava_lang_Class() {
        try {
            // Do not test return value, may return true or false depending on
            // if the jit is enabled. Make the call to ensure it doesn't crash.
            Compiler.compileClass(Compiler.class);
        } catch (Exception e) {
            fail("Exception during test.");
        }
        
        // NullPointerException is not specified.
        Compiler.compileClass((Class) null);
    }

    /**
     * @tests java.lang.Compiler#compileClasses(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compileClasses",
        args = {java.lang.String.class}
    )
    public void test_compileClassesLjava_lang_String() {
        try {
            // Do not test return value, may return true or false depending on
            // if the jit is enabled. Make the call to ensure it doesn't crash.
            Compiler.compileClasses("Compiler");
        } catch (Exception e) {
            fail("Exception during test.");
        }
        
        // NullPointerException is not specified.
        Compiler.compileClasses((String) null);
    }

    /**
     * @tests java.lang.Compiler#disable()
     */
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Doesn't verify that disable() method causes the Compiler to cease operation.",
        method = "disable",
        args = {}
    )
    public void test_disable() {
        try {
            Compiler.disable();
            Compiler.compileClass(Compiler.class);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.Compiler#enable()
     */
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Doesn't verify that enable() method causes the Compiler to resume operation.",
        method = "enable",
        args = {}
    )
    public void test_enable() {
        try {
            Compiler.disable();
            Compiler.enable();
            Compiler.compileClass(Compiler.class);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
}

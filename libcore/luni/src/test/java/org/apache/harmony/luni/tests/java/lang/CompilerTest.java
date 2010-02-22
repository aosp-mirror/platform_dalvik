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

import junit.framework.TestCase;

public class CompilerTest extends TestCase {

    /**
     * @tests java.lang.Compiler#command(java.lang.Object)
     */
    public void test_commandLjava_lang_Object() {
        assertNull("Incorrect behavior.", Compiler.command(new Object()));
    }

    /**
     * @tests java.lang.Compiler#compileClass(java.lang.Class)
     */
    public void test_compileClassLjava_lang_Class() {
        // Do not test return value, may return true or false depending on
        // if the jit is enabled. Make the call to ensure it doesn't crash.
        Compiler.compileClass(Compiler.class);
    }

    /**
     * @tests java.lang.Compiler#compileClasses(java.lang.String)
     */
    public void test_compileClassesLjava_lang_String() {
        // Do not test return value, may return true or false depending on
        // if the jit is enabled. Make the call to ensure it doesn't crash.
            Compiler.compileClasses("Compiler");
    }

    /**
     * @tests java.lang.Compiler#disable()
     */
    public void test_disable() {
        Compiler.disable();
        Compiler.compileClass(Compiler.class);
    }

    /**
     * @tests java.lang.Compiler#enable()
     */
    public void test_enable() {
        Compiler.disable();
        Compiler.enable();
        Compiler.compileClass(Compiler.class);
    }
}

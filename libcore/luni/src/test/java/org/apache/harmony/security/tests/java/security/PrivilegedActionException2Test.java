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

package org.apache.harmony.security.tests.java.security;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.IOException;
import java.security.PrivilegedActionException;

@TestTargetClass(PrivilegedActionException.class)
public class PrivilegedActionException2Test extends junit.framework.TestCase {
    
    private static Throwable tCause = new Throwable("Test cause");

    /**
     * @tests java.security.PrivilegedActionException#PrivilegedActionException(java.lang.Exception)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrivilegedActionException",
        args = {java.lang.Exception.class}
    )
    public void test_ConstructorLjava_lang_Exception() {
        Exception e = new Exception("test exception");
        PrivilegedActionException pe = new PrivilegedActionException(e);
        assertEquals("Did not encapsulate test exception!", e, pe
                .getException());

        // try it with a null exception
        pe = new PrivilegedActionException(null);
        assertNull("Did not encapsulate null test exception properly!", pe
                .getException());
    }

    /**
     * @tests java.security.PrivilegedActionException#getException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getException",
        args = {}
    )
    public void test_getException() {
        Exception e = new IOException("test IOException");
        PrivilegedActionException pe = new PrivilegedActionException(e);
        assertEquals("Did not encapsulate test IOException!", e, pe
                .getException());
    }
    
    /**
     * @tests java.security.PrivilegedActionException#getCause()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() {
        Exception ex = new Exception("Test message", tCause);
        PrivilegedActionException pe = new PrivilegedActionException(ex);
        
        try {
            Throwable res = pe.getCause();
            if (!res.equals(ex)) {
                fail("Method getCause() returned incorrect value");
            }
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
}
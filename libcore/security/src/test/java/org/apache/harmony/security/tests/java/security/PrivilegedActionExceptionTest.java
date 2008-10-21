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

/**
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import java.security.PrivilegedActionException;

import junit.framework.TestCase;

/**
 * Unit test for java.security.PrivilegedActionException.
 * 
 */

public class PrivilegedActionExceptionTest extends TestCase {
    /**
     * Entry point for standalone runs.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PrivilegedActionExceptionTest.class);
    }

    /**
     * Tests PrivilegedActionException(Exception)
     */
    public void testPrivilegedActionException() {
        new PrivilegedActionException(null);
        Exception ex = new Exception();
        new PrivilegedActionException(ex);
    }

    /**
     * Tests PrivilegedActionException.getException()
     */
    public void testGetException() {
        assertNull(new PrivilegedActionException(null).getException());
        Exception ex = new Exception();
        assertSame(new PrivilegedActionException(ex).getException(), ex);
    }

    /**
     * Tests PrivilegedActionException.toString()
     */
    public void testToString() {
        assertNotNull(new PrivilegedActionException(null).toString());
        assertNotNull(new PrivilegedActionException(new Exception()).toString());
    }

}
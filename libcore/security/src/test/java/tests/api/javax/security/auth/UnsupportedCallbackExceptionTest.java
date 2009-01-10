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

package tests.api.javax.security.auth;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.Callback;

/**
 * Tests for <code>UnsupportedCallbackException</code> class constructors and methods.
 * 
 */
@TestTargetClass(UnsupportedCallbackException.class) 
public class UnsupportedCallbackExceptionTest extends TestCase {
    
    public static void main(String[] args) {
    }

    /**
     * Constructor for UnsupportedCallbackExceptionTest.
     * 
     * @param arg0
     */
    public UnsupportedCallbackExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };


    /**
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#UnsupportedCallbackException(Callback callback)
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#getCallback() 
     * Assertion: constructs with null parameter.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "UnsupportedCallbackException",
            args = {Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getCallback",
            args = {}
        )
    })
    public void testUnsupportedCallbackException01() {
        Callback c = null;
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(c);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertNull("getCallback() must return null", ucE.getCallback());
    }
    
    /**
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#UnsupportedCallbackException(Callback callback)
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#getCallback() 
     * Assertion: constructs with not null parameter.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "UnsupportedCallbackException",
            args = {Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getCallback",
            args = {}
        )
    })
    public void testUnsupportedCallbackException02() {
        myCallback c = new myCallback();
        assertNotNull("Callback object is null", c);
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(c);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertEquals("Incorrect callback object was returned", c, ucE.getCallback());
    }
    
    /**
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#UnsupportedCallbackException(Callback callback, String msg) 
     * Assertion: constructs with null callback parameter and null message.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException03() {
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(null, null);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertNull("getCallback() must return null.", ucE.getCallback());
    }
    
    /**
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#UnsupportedCallbackException(Callback callback, String msg) 
     * Assertion: constructs with null callback parameter and not null message.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException04() {
        UnsupportedCallbackException ucE;
        for (int i = 0; i < msgs.length; i++) {
            ucE = new UnsupportedCallbackException(null, msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), ucE.getMessage(), msgs[i]);
            assertNull("getCallback() must return null.", ucE.getCallback());
        }
    }
    
    /**
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#UnsupportedCallbackException(Callback callback, String msg) 
     * Assertion: constructs with not null callback parameter and null message.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException05() {
        myCallback c = new myCallback();
        assertNotNull("Callback object is null", c);
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(c, null);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertEquals("Incorrect callback object was returned", c, ucE.getCallback());
    }
    
    /**
     * @tests javax.security.auth.callback.UnsupportedCallbackExceptionTest#UnsupportedCallbackException(Callback callback, String msg) 
     * Assertion: constructs with not null parameters.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException06() {
        myCallback c = new myCallback();
        assertNotNull("Callback object is null", c);
        UnsupportedCallbackException ucE;
        for (int i = 0; i < msgs.length; i++) {
            ucE = new UnsupportedCallbackException(c, msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), ucE.getMessage(), msgs[i]);
            assertEquals("Incorrect callback object was returned", c, ucE.getCallback());
        }
    }
}

class myCallback implements Callback {
    myCallback(){
    }
}


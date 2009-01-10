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

package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;

import junit.framework.TestCase;

import org.apache.harmony.xnet.tests.support.mySSLSession;


/**
 * Tests for <code>HostnameVerifier</code> class constructors and methods.
 * 
 */
@TestTargetClass(HostnameVerifier.class) 
public class HostnameVerifierTest extends TestCase {
    
    class myHostnameVerifier implements HostnameVerifier {
        
        myHostnameVerifier() {
        }
        
        public boolean verify(String hostname, SSLSession session) {
            if (hostname == session.getPeerHost()) {
                return true;
            } else return false;
        }
    }
    
    /**
     * @tests javax.net.ssl.HostnameVerifier#verify(String hostname, SSLSession session) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    public final void test_verify() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        myHostnameVerifier hv = new myHostnameVerifier(); 
        try {
            assertFalse(hv.verify("hostname", session));
            assertTrue(hv.verify("localhost", session));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}

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

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

import junit.framework.TestCase;

import org.apache.harmony.xnet.tests.support.mySSLSession;
import org.apache.harmony.xnet.tests.support.mySSLSocket;


/**
 * Tests for <code>HandshakeCompletedListener</code> class constructors and methods.
 * 
 */
@TestTargetClass(HandshakeCompletedListener.class) 
public class HandshakeCompletedListenerTest extends TestCase {
    
    class myHandshakeCompletedListener implements HandshakeCompletedListener {
        
        private boolean completeDone;
        
        myHandshakeCompletedListener() {
            completeDone = false;
        }
        
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            if (event != null)  completeDone = true;
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedListener#handshakeCompleted(HandshakeCompletedEvent event) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "handshakeCompleted",
        args = {HandshakeCompletedEvent.class}
    )
    public final void test_handshakeCompleted() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        myHandshakeCompletedListener hcl = new myHandshakeCompletedListener(); 
        try {
            hcl.handshakeCompleted(event);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

}

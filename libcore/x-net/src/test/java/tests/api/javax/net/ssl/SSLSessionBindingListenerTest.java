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

import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;

import org.apache.harmony.xnet.tests.support.mySSLSession;

import junit.framework.TestCase;

/**
 * Tests for SSLSessionBindingListener class
 * 
 */
@TestTargetClass(SSLSessionBindingListener.class) 
public class SSLSessionBindingListenerTest extends TestCase {
    
    public class mySSLSessionBindingListener implements SSLSessionBindingListener {
        
        private boolean boundDone = false;
        private boolean unboundDone = false;
        
        mySSLSessionBindingListener() {
        }
        
        public void valueBound(SSLSessionBindingEvent event) {
            if (event != null) boundDone = true;
        }
        public void valueUnbound(SSLSessionBindingEvent event) {
            if (event != null) unboundDone = true;
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSessionBindingListener#valueBound(SSLSessionBindingEvent event)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueBound",
        args = {SSLSessionBindingEvent.class}
    )
    public void test_valueBound() {
        mySSLSession ss = new mySSLSession("localhost", 1080, null);
        mySSLSessionBindingListener sbl = new mySSLSessionBindingListener();
        SSLSessionBindingEvent event = new SSLSessionBindingEvent(ss, "Name_01");
        try {
            sbl.valueBound(event);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSessionBindingListener#valueUnbound(SSLSessionBindingEvent event)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueUnbound",
        args = {SSLSessionBindingEvent.class}
    )
    public void test_valueUnbound() {
        mySSLSession ss = new mySSLSession("localhost", 1080, null);
        mySSLSessionBindingListener sbl = new mySSLSessionBindingListener();
        SSLSessionBindingEvent event = new SSLSessionBindingEvent(ss, "Name_01");
        try {
            sbl.valueUnbound(event);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

}

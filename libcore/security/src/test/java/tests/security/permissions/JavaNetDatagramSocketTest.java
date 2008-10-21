/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.security.permissions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import junit.framework.TestCase;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.net.DatagramSocket
 */
public class JavaNetDatagramSocketTest extends TestCase {
    
    SecurityManager old;

    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    
    public void test_ctor() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            String host = null;
            int port = 0;
            void reset(){
                called = false;
                host = null;
                port = 0;
            }
            @Override
            public void checkListen(int port) {
                called = true;
                this.port = port;
                super.checkListen(port);
            }
            @Override
            public void checkAccept(String host, int port) {
                this.host = host;
                this.port = port;
                super.checkAccept(host, port);
            }
        }
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        DatagramSocket s1 = new DatagramSocket(8881);
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", 8881, s.port);
        
        s.reset();
        DatagramSocket s2 = new DatagramSocket();
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", 0, s.port);
        
        s.reset();
        DatagramSocket s3 = new DatagramSocket(new InetSocketAddress(8882));
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", 8882, s.port);
        
        s.reset();
        DatagramSocket s4 = new DatagramSocket(8883, InetAddress.getLocalHost());
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", 8883, s.port);
        
        
        DatagramPacket p = new DatagramPacket(new byte[256], 0, 256);
        
        s1.setSoTimeout(0);
        s.reset();
        assert(s1.getInetAddress()==null);
        s1.receive(p);
        assertTrue("java.net.DatagramSocket.receive must call checkAccept on security manager", s.called);
    }
    
}


/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import javax.net.ssl.*;
import java.net.*;
import java.lang.String;
import java.lang.IllegalStateException;
import java.io.IOException;


import junit.framework.TestCase;
import org.apache.harmony.xnet.tests.support.mySSLSocket;

import tests.support.Support_PortManager;

@TestTargetClass(SSLSocket.class) 
public class SSLSocketTest extends TestCase {

    public class HandshakeCL implements HandshakeCompletedListener {
        HandshakeCL() {
            super();
        }
        public void handshakeCompleted(HandshakeCompletedEvent event) {
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#SSLSocket() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocket",
        args = {}
    )
    public void testConstructor_01() {
        try {
            mySSLSocket ssl = new mySSLSocket();
            assertNotNull(ssl);
            assertTrue(ssl instanceof SSLSocket);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#SSLSocket(InetAddress address, int port) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocket",
        args = {java.net.InetAddress.class, int.class}
    )
    public void testConstructor_02() {
        mySSLSocket ssl;
        int sport = startServer("Cons InetAddress,I");
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLSocket(InetAddress.getLocalHost(), sport);
            assertNotNull(ssl);
            assertEquals(sport, ssl.getPort());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        try {
            ssl = new mySSLSocket(InetAddress.getLocalHost(), 8081);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
            //expected
        }
        
        for (int i = 0; i < invalidPort.length; i++) {
            try {
                ssl = new mySSLSocket(InetAddress.getLocalHost(), invalidPort[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPort[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPort[i]);
            }
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#SSLSocket(InetAddress address, int port, 
     *                                          InetAddress clientAddress, int clientPort) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocket",
        args = {java.net.InetAddress.class, int.class, java.net.InetAddress.class, int.class}
    )
    public void testConstructor_03() {
        mySSLSocket ssl;
        int sport = startServer("Cons InetAddress,I,InetAddress,I");
        int portNumber = Support_PortManager.getNextPort();
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLSocket(InetAddress.getLocalHost(), sport,
                                  InetAddress.getLocalHost(), portNumber);
            assertNotNull(ssl);
            assertEquals(sport, ssl.getPort());
            assertEquals(portNumber, ssl.getLocalPort());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        try {
            ssl = new mySSLSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
            //expected
        }
        
        for (int i = 0; i < invalidPort.length; i++) {
            try {
                ssl = new mySSLSocket(InetAddress.getLocalHost(), invalidPort[i],
                                      InetAddress.getLocalHost(), portNumber);
                fail("IllegalArgumentException wasn't thrown for " + invalidPort[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPort[i]);
            }
            try {
                ssl = new mySSLSocket(InetAddress.getLocalHost(), sport,
                                      InetAddress.getLocalHost(), invalidPort[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPort[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPort[i]);
            }
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#SSLSocket(String host, int port) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocket",
        args = {java.lang.String.class, int.class}
    )
    public void testConstructor_04() {
        mySSLSocket ssl;
        int sport = startServer("Cons String,I");
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLSocket(InetAddress.getLocalHost().getHostName(), sport);
            assertNotNull(ssl);
            assertEquals(sport, ssl.getPort());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        try {
            ssl = new mySSLSocket("localhost", 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
            //expected
        }
        
        for (int i = 0; i < invalidPort.length; i++) {
            try {
                ssl = new mySSLSocket(InetAddress.getLocalHost().getHostName(), invalidPort[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPort[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPort[i]);
            }
        }
        
        try {
            ssl = new mySSLSocket("bla-bla", sport);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException uhp) {
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
    } 
    
    /**
     * @tests javax.net.ssl.SSLSocket#SSLSocket(String host, int port, InetAddress clientAddress, 
     *           int clientPort) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocket",
        args = {java.lang.String.class, int.class, java.net.InetAddress.class, int.class}
    )
    public void testConstructor_05() {
        mySSLSocket ssl;
        int sport = startServer("Cons String,I,InetAddress,I");
        int portNumber = Support_PortManager.getNextPort();
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLSocket(InetAddress.getLocalHost().getHostName(), sport,
                                  InetAddress.getLocalHost(), portNumber);
            assertNotNull(ssl);
            assertEquals(sport, ssl.getPort());
            assertEquals(portNumber, ssl.getLocalPort());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        try {
            ssl = new mySSLSocket("localhost", 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
            //expected
        }
        
        for (int i = 0; i < invalidPort.length; i++) {
            portNumber = Support_PortManager.getNextPort();
            try {
                ssl = new mySSLSocket(InetAddress.getLocalHost().getHostName(), invalidPort[i],
                                      InetAddress.getLocalHost(), portNumber);
                fail("IllegalArgumentException wasn't thrown for " + invalidPort[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPort[i]);
            }
            try {
                ssl = new mySSLSocket(InetAddress.getLocalHost().getHostName(), sport,
                                      InetAddress.getLocalHost(), invalidPort[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPort[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPort[i]);
            }
        }
        
        portNumber = Support_PortManager.getNextPort();
        try {
            ssl = new mySSLSocket("bla-bla", sport, InetAddress.getLocalHost(), portNumber);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException uhp) {
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#addHandshakeCompletedListener(HandshakeCompletedListener listener) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addHandshakeCompletedListener",
        args = {javax.net.ssl.HandshakeCompletedListener.class}
    )
    public void test_addHandshakeCompletedListener() {
        mySSLSocket ssl = new mySSLSocket();
        HandshakeCompletedListener ls = new HandshakeCL();
        try {
            ssl.addHandshakeCompletedListener(null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        try {
            ssl.addHandshakeCompletedListener(ls);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#removeHandshakeCompletedListener(HandshakeCompletedListener listener) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandshakeCompletedListener",
        args = {javax.net.ssl.HandshakeCompletedListener.class}
    )
    public void test_removeHandshakeCompletedListener() {
        mySSLSocket ssl = new mySSLSocket();
        HandshakeCompletedListener ls = new HandshakeCL();
        try {
            ssl.removeHandshakeCompletedListener(null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        try {
            ssl.removeHandshakeCompletedListener(ls);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#setEnableSessionCreation(boolean flag) 
     * @tests javax.net.ssl.SSLSocket#getEnableSessionCreation()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEnableSessionCreation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnableSessionCreation",
            args = {boolean.class}
        )
    })
    public void test_EnableSessionCreation() {
        mySSLSocket ssl = new mySSLSocket();
        try {
            assertTrue(ssl.getEnableSessionCreation());
            ssl.setEnableSessionCreation(false);
            assertFalse(ssl.getEnableSessionCreation());
            ssl.setEnableSessionCreation(true);
            assertTrue(ssl.getEnableSessionCreation());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#setNeedClientAuth(boolean need) 
     * @tests javax.net.ssl.SSLSocket#getNeedClientAuthCreation()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setNeedClientAuth",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getNeedClientAuth",
            args = {}
        )
    })
    public void test_NeedClientAuth() {
        mySSLSocket ssl = new mySSLSocket(1);
        try {
            try {
                ssl.setNeedClientAuth(true);
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
            
            try {
                ssl.getNeedClientAuth();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
            ssl = new mySSLSocket(0);
            ssl.setNeedClientAuth(true);
            assertTrue(ssl.getNeedClientAuth());
            ssl.setNeedClientAuth(false);
            assertFalse(ssl.getNeedClientAuth());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#setWantClientAuth(boolean want) 
     * @tests javax.net.ssl.SSLSocket#getWantClientAuthCreation()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setWantClientAuth",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getWantClientAuth",
            args = {}
        )
    })
    public void test_WantClientAuth() {
        mySSLSocket ssl = new mySSLSocket(1);
        try {
            try {
                ssl.setWantClientAuth(true);
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
            
            try {
                ssl.getWantClientAuth();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
            ssl = new mySSLSocket(0);
            ssl.setWantClientAuth(true);
            assertTrue(ssl.getWantClientAuth());
            ssl.setWantClientAuth(false);
            assertFalse(ssl.getWantClientAuth());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#getSupportedProtocols()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedProtocols",
        args = {}
    )
    public void test_getSupportedProtocols() {
        String[] pr = {"Protocol_1", "Protocol_2", "Protocol_3"};
        mySSLSocket ssl = new mySSLSocket();
        try {
            try {
                ssl.getSupportedProtocols();
            } catch (NullPointerException npe) {
                //expected
            }
            ssl = new mySSLSocket(null, null);
            try {
                ssl.getSupportedProtocols();
            } catch (NullPointerException npe) {
                //expected
            }
            ssl = new mySSLSocket(new String[0], null);
            assertNull("Not NULL result", ssl.getSupportedProtocols());
            ssl = new mySSLSocket(pr, null);
            String[] res = ssl.getSupportedProtocols();
            assertNotNull("NULL result", res);
            if (!res.equals(pr)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#getEnabledProtocols()
     * @tests javax.net.ssl.SSLSocket#setEnabledProtocols(String[] protocols)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnabledProtocols",
            args = {java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEnabledProtocols",
            args = {}
        )
    })
    public void test_EnabledProtocols() {
        String[] pr1 = {"Protocol_1", "Protocol_2", "Protocol_3"};
        String[] pr2 = {"Protocol_1", "Protocol_2"};
        mySSLSocket ssl = new mySSLSocket(pr1, null);
        try {
            try {
                ssl.setEnabledProtocols(null);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            try {
                ssl.setEnabledProtocols(pr2);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            ssl.setEnabledProtocols(pr1);
            String[] res = ssl.getEnabledProtocols();
            assertNotNull("NULL result", res);
            if (!res.equals(pr1)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#getSession()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {}
    )
    public void test_getSession() {
        mySSLSocket ssl = new mySSLSocket();
        try {
            assertNull(ssl.getSession());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#getSupportedCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        String[] pr = {"Suite_1", "Suite_2", "Suite_3"};
        mySSLSocket ssl = new mySSLSocket();
        try {
            try {
                ssl.getSupportedCipherSuites();
            } catch (NullPointerException npe) {
                //expected
            }
            ssl = new mySSLSocket(null, null);
            try {
                ssl.getSupportedCipherSuites();
            } catch (NullPointerException npe) {
                //expected
            }
            ssl = new mySSLSocket(new String[0], new String[0]);
            assertNull("Not NULL result", ssl.getSupportedCipherSuites());
            ssl = new mySSLSocket(null, pr);
            String[] res = ssl.getSupportedCipherSuites();
            assertNotNull("NULL result", res);
            if (!res.equals(pr)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#getEnabledCipherSuites()
     * @tests javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[] suites)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEnabledCipherSuites",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnabledCipherSuites",
            args = {java.lang.String[].class}
        )
    })
    public void test_EnabledCipherSuites() {
        String[] pr1 = {"Suite_1", "Suite_2", "Suite_3"};
        String[] pr2 = {"Suite_1", "Suite_2"};
        mySSLSocket ssl = new mySSLSocket(null, pr1);
        try {
            try {
                ssl.setEnabledCipherSuites(null);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            try {
                ssl.setEnabledCipherSuites(pr2);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            ssl.setEnabledCipherSuites(pr1);
            String[] res = ssl.getEnabledCipherSuites();
            assertNotNull("NULL result", res);
            if (!res.equals(pr1)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#getUseClientMode()
     * @tests javax.net.ssl.SSLSocket#setUseClientMode(boolean mode)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseClientMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseClientMode",
            args = {boolean.class}
        )
    })
    public void test_UseClientMode() {
        String[] pr = {"Protocol_1", "Protocol_3"};
        mySSLSocket ssl = new mySSLSocket(0, pr, null);
        try {
            assertFalse(ssl.getUseClientMode());
            ssl.setUseClientMode(true);
            assertTrue(ssl.getUseClientMode());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
        ssl = new mySSLSocket(1, pr, null);
        try {
            assertTrue(ssl.getUseClientMode());
            ssl.setUseClientMode(false);
            assertFalse(ssl.getUseClientMode());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
        try {
            ssl.startHandshake();
            ssl.setUseClientMode(false);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (IOException ioe) {
            fail(ioe + " was thrown for method startHandshake()");
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocket#startHandshake()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "startHandshake",
        args = {}
    )
    public void test_startHandshake() {
        String[] pr = {"Protocol_1", "Protocol_3"};
        String[] pr1 = {"Protocol_1", "Protocol_3", "Protocol_2"};
        mySSLSocket ssl = new mySSLSocket(pr, null);
        try {
            ssl.startHandshake();
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
        ssl = new mySSLSocket(pr1, null);
        try {
            ssl.startHandshake();
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IOException");
        }
    }
    
    protected int startServer(String name) {
        int portNumber = Support_PortManager.getNextPort();
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(portNumber);
        } catch (IOException e) {
            fail(name + ": " + e);
        }
        return ss.getLocalPort();
    }
}


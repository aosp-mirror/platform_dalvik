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
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import javax.net.ssl.*;

import java.net.*;
import java.io.*;
import java.lang.String;

import junit.framework.TestCase;

import tests.support.Support_PortManager;

@TestTargetClass(SSLServerSocket.class) 
public class SSLServerSocketTest extends TestCase {

    /**
     * Additional class for SSLServerSocket constructor verification
     */
    class mySSLServerSocket extends SSLServerSocket {

        private boolean sslFlag = true;
        private boolean sslNeed = false;
        private boolean sslWant = false;
        private boolean sslMode = true;
        private String[] supportProtocol = null;
        private String[] supportSuites = null;
        
        public mySSLServerSocket(String[] protocols, String[] suites) throws IOException {
            super();
            supportProtocol = protocols;
            supportSuites = suites;            
        }
        public mySSLServerSocket() throws IOException{
            super();
        }
        public mySSLServerSocket(int port) throws IOException{
            super(port);
        }
        public mySSLServerSocket(int port, int backlog) throws IOException{
            super(port, backlog);
        }
        public mySSLServerSocket(int port, int backlog, InetAddress address) throws IOException{
            super(port, backlog, address);
        }
        
        public String[] getSupportedCipherSuites() {
            if (supportSuites == null) {
                throw new NullPointerException();
            }
            if (supportSuites.length == 0) {
                return null;
            } else return supportSuites;
        }
        public void setEnabledCipherSuites(String[] suites) {
            if (suites == null) {
                throw new IllegalArgumentException("null parameter");
            }
            if (!suites.equals(supportSuites)) {
                throw new IllegalArgumentException("incorrect suite");
            }
        }
        public String[] getEnabledCipherSuites() {
            return supportSuites;
        }
        
        public String[] getSupportedProtocols() {
            if (supportProtocol == null) {
                throw new NullPointerException();
            }
            if (supportProtocol.length == 0) {
                return null;
            } else return supportProtocol;
        }
        public String[] getEnabledProtocols() {
            return supportProtocol;
        }
        public void setEnabledProtocols(String[] protocols) {
            if (protocols == null) {
                throw new IllegalArgumentException("null protocol");
            }
            if (!protocols.equals(supportProtocol)) {
                throw new IllegalArgumentException("incorrect protocol");
            }
        }
        
        public void setEnableSessionCreation(boolean flag) {
            sslFlag = flag;
        }
        public boolean getEnableSessionCreation() {
            return sslFlag;
        }
        
        public void setNeedClientAuth(boolean need) {
            sslNeed = need;
        }
        public boolean getNeedClientAuth() {
            return sslNeed;
        }
        
        public boolean getUseClientMode() {
            return sslMode;
        }
        public void setUseClientMode(boolean mode) {
            sslMode = mode;
        }
        
        public boolean getWantClientAuth() {
            return sslWant;
        }
        public void setWantClientAuth(boolean mode) {
            sslWant = mode;
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#SSLServerSocket() 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException wasn't implemented",
        method = "SSLServerSocket",
        args = {}
    )
    public void testConstructor_01() {
        try {
            mySSLServerSocket ssl = new mySSLServerSocket();
            assertNotNull(ssl);
            assertTrue(ssl instanceof SSLServerSocket);
        } catch (Exception ex) {
            fail("Unexpected exception was thrown " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#SSLServerSocket(int port) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLServerSocket",
        args = {int.class}
    )
    public void testConstructor_02() {
        mySSLServerSocket ssl;
        int portNumber = Support_PortManager.getNextPort();
        int[] port_invalid = {-1, 65536, Integer.MIN_VALUE, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLServerSocket(portNumber);
            assertNotNull(ssl);
            assertEquals(portNumber, ssl.getLocalPort());
        } catch (Exception ex) {
            fail("Unexpected exception was thrown " + ex);
        }
        
        for (int i = 0; i < port_invalid.length; i++) {
            try {
                ssl = new mySSLServerSocket(port_invalid[i]);
                fail("IllegalArgumentException should be thrown");
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException");
            }
        }
        
        try {
            ssl = new mySSLServerSocket(portNumber);
            new mySSLServerSocket(portNumber);
        } catch (IOException ioe) {
        } catch (Exception ex) {
            fail("Unexpected exception was thrown " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#SSLServerSocket(int port, int backlog) 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Invalid values for backlog weren't checked",
        method = "SSLServerSocket",
        args = {int.class, int.class}
    )
    public void testConstructor_03() {
        mySSLServerSocket ssl;
        int portNumber = Support_PortManager.getNextPort();
        int[] port_invalid = {-1, Integer.MIN_VALUE, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLServerSocket(portNumber, 1);
            assertNotNull(ssl);
            assertEquals(portNumber, ssl.getLocalPort());
        } catch (Exception ex) {
            fail("Unexpected exception was thrown");
        }
        
        for (int i = 0; i < port_invalid.length; i++) {
            try {
                ssl = new mySSLServerSocket(port_invalid[i], 1);
                fail("IllegalArgumentException should be thrown");
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException");
            }
        }
        
        portNumber = Support_PortManager.getNextPort();
        try {
            ssl = new mySSLServerSocket(portNumber, 1);
            new mySSLServerSocket(portNumber, 1);
            fail("IOException should be thrown");
        } catch (IOException ioe) {
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#SSLServerSocket(int port, int backlog, InetAddress address) 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Invalid values for backlog weren\'t checked",
        method = "SSLServerSocket",
        args = {int.class, int.class, InetAddress.class}
    )
    public void testConstructor_04() {
        mySSLServerSocket ssl;
        InetAddress ia = null;
        int portNumber = Support_PortManager.getNextPort();
        int[] port_invalid = {-1, 65536, Integer.MIN_VALUE, Integer.MAX_VALUE};
        
        try {
            ssl = new mySSLServerSocket(portNumber, 0, ia);
            assertNotNull(ssl);
            assertEquals(portNumber, ssl.getLocalPort());
        } catch (Exception ex) {
            fail("Unexpected exception was thrown");
        }
        
        portNumber = Support_PortManager.getNextPort();
        try {
            ssl = new mySSLServerSocket(portNumber, 0, InetAddress.getLocalHost());
            assertNotNull(ssl);
            assertEquals(portNumber, ssl.getLocalPort());
        } catch (Exception ex) {
            fail("Unexpected exception was thrown");
        }
       
        for (int i = 0; i < port_invalid.length; i++) {
            try {
                ssl = new mySSLServerSocket(port_invalid[i], 1, InetAddress.getLocalHost());
                fail("IllegalArgumentException should be thrown");
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException");
            }
        }
        
        portNumber = Support_PortManager.getNextPort();
        try {
           ssl = new mySSLServerSocket(portNumber, 0, InetAddress.getLocalHost());
           new mySSLServerSocket(portNumber, 0, InetAddress.getLocalHost());
           fail("IOException should be thrown for");
        } catch (IOException ioe) {
        }
    } 
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#getSupportedCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        String[] pr = {"Suite_1", "Suite_2", "Suite_3"};
        try {
            mySSLServerSocket sss = new mySSLServerSocket();
            try {
                sss.getSupportedCipherSuites();
            } catch (NullPointerException npe) {
                //expected
            }
            sss = new mySSLServerSocket(null, pr);
            try {
                sss.getSupportedCipherSuites();
            } catch (NullPointerException npe) {
                //expected
            }
            sss = new mySSLServerSocket(new String[0], new String[0]);
            assertNull("Not NULL result", sss.getSupportedCipherSuites());
            sss = new mySSLServerSocket(null, pr);
            String[] res = sss.getSupportedCipherSuites();
            assertNotNull("NULL result", res);
            if (!res.equals(pr)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#getEnabledCipherSuites()
     * @tests javax.net.ssl.SSLServerSocket#setEnabledCipherSuites(String[] suites)
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
            args = {String[].class}
        )
    }) 
    public void test_EnabledCipherSuites() {
        String[] pr1 = {"Suite_1", "Suite_2", "Suite_3"};
        String[] pr2 = {"Suite_1", "Suite_2"};
        try {
            mySSLServerSocket sss = new mySSLServerSocket(null, pr1);
            try {
                sss.setEnabledCipherSuites(null);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            try {
                sss.setEnabledCipherSuites(pr2);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            sss.setEnabledCipherSuites(pr1);
            String[] res = sss.getEnabledCipherSuites();
            assertNotNull("NULL result", res);
            if (!res.equals(pr1)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#getSupportedProtocols()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedProtocols",
        args = {}
    )
    public void test_getSupportedProtocols() {
        String[] pr = {"Protocol_1", "Protocol_2", "Protocol_3"};
        try {
            mySSLServerSocket sss = new mySSLServerSocket();
            try {
                sss.getSupportedProtocols();
            } catch (NullPointerException npe) {
                //expected
            }
            sss = new mySSLServerSocket(null, null);
            try {
                sss.getSupportedProtocols();
            } catch (NullPointerException npe) {
                //expected
            }
            sss = new mySSLServerSocket(new String[0], null);
            assertNull("Not NULL result", sss.getSupportedProtocols());
            sss = new mySSLServerSocket(pr, null);
            String[] res = sss.getSupportedProtocols();
            assertNotNull("NULL result", res);
            if (!res.equals(pr)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#getEnabledProtocols()
     * @tests javax.net.ssl.SSLServerSocket#setEnabledProtocols(String[] protocols)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnabledProtocols",
            args = {String[].class}
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
        try {
            mySSLServerSocket sss = new mySSLServerSocket(pr1, null);
            try {
                sss.setEnabledProtocols(null);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            try {
                sss.setEnabledProtocols(pr2);
            } catch (IllegalArgumentException iae) {
                //expected
            }
            sss.setEnabledProtocols(pr1);
            String[] res = sss.getEnabledProtocols();
            assertNotNull("NULL result", res);
            if (!res.equals(pr1)) {
                fail("Returned result was incorrect");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#setEnableSessionCreation(boolean flag) 
     * @tests javax.net.ssl.SSLServerSocket#getEnableSessionCreation()
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
        try {
            mySSLServerSocket sss = new mySSLServerSocket();
            assertTrue(sss.getEnableSessionCreation());
            sss.setEnableSessionCreation(false);
            assertFalse(sss.getEnableSessionCreation());
            sss.setEnableSessionCreation(true);
            assertTrue(sss.getEnableSessionCreation());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#setNeedClientAuth(boolean need) 
     * @tests javax.net.ssl.SSLServerSocket#getNeedClientAuthCreation()
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
        try {
            mySSLServerSocket sss = new mySSLServerSocket();
            sss.setNeedClientAuth(true);
            assertTrue(sss.getNeedClientAuth());
            sss.setNeedClientAuth(false);
            assertFalse(sss.getNeedClientAuth());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#getUseClientMode()
     * @tests javax.net.ssl.SSLServerSocket#setUseClientMode(boolean mode)
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
        try {
            mySSLServerSocket sss = new mySSLServerSocket();
            sss.setUseClientMode(false);
            assertFalse(sss.getUseClientMode());
            sss.setUseClientMode(true);
            assertTrue(sss.getUseClientMode());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLServerSocket#setWantClientAuth(boolean want) 
     * @tests javax.net.ssl.SSLServerSocket#getWantClientAuthCreation()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getWantClientAuth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setWantClientAuth",
            args = {boolean.class}
        )
    })
    public void test_WantClientAuth() {
        try {
            mySSLServerSocket sss = new mySSLServerSocket();
            sss.setWantClientAuth(true);
            assertTrue(sss.getWantClientAuth());
            sss.setWantClientAuth(false);
            assertFalse(sss.getWantClientAuth());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}

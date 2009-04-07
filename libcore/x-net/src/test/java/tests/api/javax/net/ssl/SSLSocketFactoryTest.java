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

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import junit.framework.TestCase;

import tests.support.Support_PortManager;

@TestTargetClass(SSLSocketFactory.class) 
public class SSLSocketFactoryTest extends TestCase {
    
	private ServerSocket ss;
	
    protected int startServer(String name) {
        int portNumber = Support_PortManager.getNextPort();
        try {
            ss = new ServerSocket(portNumber);
        } catch (IOException e) {
            fail(name + ": " + e);
        }
        return ss.getLocalPort();
    }

    /**
     * @tests javax.net.ssl.SSLSocketFactory#SSLSocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            SocketFactory sf = SSLSocketFactory.getDefault();
            assertTrue(sf instanceof SSLSocketFactory);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocketFactory#getDefault()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public void test_getDefault() {
        assertNotNull("Incorrect default socket factory",
                SSLSocketFactory.getDefault());
    }
    
    /**
     * @tests javax.net.ssl.SSLSocketFactory#createSocket(Socket s, String host, int port, boolean autoClose)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {java.net.Socket.class, java.lang.String.class, int.class, boolean.class}
    )
    public void test_createSocket() {
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        int sport = startServer("test_createSocket()");
        int[] invalid = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        String[] str = {"bla-bla"};
        try {
        	Socket st = new Socket("localhost", sport);
           Socket s = sf.createSocket(st, "localhost", sport, false);
           assertFalse(s.isClosed());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
        	Socket st = new Socket("localhost", sport);
            Socket s = sf.createSocket(st, "localhost", sport, true);
            s.close();
            assertTrue(st.isClosed());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            sf.createSocket(null, "localhost", sport, true);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        } catch (NullPointerException e) {
        	// expected
        }
        for (int i = 0; i < invalid.length; i++) {
            try {
                Socket s = sf.createSocket(new Socket(), "localhost", 1080, false);
                fail("IOException wasn't thrown");
            } catch (IOException ioe) {
                //expected
            }
        }
        for (int i = 0; i < str.length; i++) {
            try {
            	Socket st = new Socket(str[i], sport);
                Socket s = sf.createSocket(st, str[i], sport, false);
                fail("UnknownHostException wasn't thrown: " + str[i]);
            } catch (UnknownHostException uhe) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of UnknownHostException");
            }
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultCipherSuites",
        args = {}
    )
    public void test_getDefaultCipherSuites() {
        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            assertTrue("no default cipher suites returned",
                    sf.getDefaultCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            assertTrue("no supported cipher suites returned",
                    sf.getSupportedCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
}

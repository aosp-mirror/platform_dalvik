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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package tests.api.javax.net;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import junit.framework.TestCase;

import tests.support.Support_PortManager;


/**
 * Tests for <code>SocketFactory</code> class methods.
 */
@TestTargetClass(SocketFactory.class) 
public class SocketFactoryTest extends TestCase {
    
    /**
     * @tests javax.net.SocketFactory#SocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            MySocketFactory sf = new MySocketFactory();
            assertNotNull(sf);
            assertTrue(sf instanceof SocketFactory);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
    /**
     * @tests javax.net.SocketFactory#createSocket()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException check missed",
        method = "createSocket",
        args = {}
    )
    public final void test_createSocket_01() {
        SocketFactory sf = SocketFactory.getDefault();
        
        try {
            Socket s = sf.createSocket();
            assertNotNull(s);
            assertEquals(-1, s.getLocalPort());
            assertEquals(0, s.getPort());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        MySocketFactory msf = new MySocketFactory();
        try {
            msf.createSocket();
            fail("No expected SocketException");
        } catch (SocketException e) {        
        } catch (IOException e) {
            fail(e.toString());
        }
    }
    
    /**
     * @tests javax.net.SocketFactory#createSocket(String host, int port)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {String.class, int.class}
    )
    public final void test_createSocket_02() {
        MySocketFactory sf = new MySocketFactory();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons String,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport);
            assertNotNull(s);
            assertTrue("Failed to create socket", s.getPort() == sport);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        try {
            Socket s = sf.createSocket("bla-bla", sport);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException uhe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
        
        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }

        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        }
        
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket("localhost", 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        }
    }
    
    /**
     * @tests javax.net.SocketFactory#createSocket(InetAddress host, int port)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {InetAddress.class, int.class}
    )
    public final void test_createSocket_03() {
        MySocketFactory sf = new MySocketFactory();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons InetAddress,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), sport);
            assertNotNull(s);
            assertTrue("Failed to create socket", s.getPort() == sport);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }

        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        }
        
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket(InetAddress.getLocalHost(), 8081);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        } 
    }
    
    /**
     * @tests javax.net.SocketFactory#createSocket(InetAddress address, int port,
     *                                             InetAddress localAddress, int localPort)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {InetAddress.class, int.class, InetAddress.class, int.class}
    )
    public final void test_createSocket_04() {
        MySocketFactory sf = new MySocketFactory();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons InetAddress,I,InetAddress,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                                       InetAddress.getLocalHost(), portNumber);
            assertNotNull(s);
            assertTrue("1: Failed to create socket", s.getPort() == sport);
            assertTrue("2: Failed to create socket", s.getLocalPort() == portNumber);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost(), invalidPorts[i],
                                           InetAddress.getLocalHost(), portNumber);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
            
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                                           InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }

        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                                       InetAddress.getLocalHost(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        }
        
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        }     
    }
    
    /**
     * @tests javax.net.SocketFactory#createSocket(String host, int port,
     *                                             InetAddress localHost, int localPort)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {String.class, int.class, InetAddress.class, int.class}
    )
    public final void test_createSocket_05() {
        MySocketFactory sf = new MySocketFactory();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons String,I,InetAddress,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport,
                                       InetAddress.getLocalHost(), portNumber);
            assertNotNull(s);
            assertTrue("1: Failed to create socket", s.getPort() == sport);
            assertTrue("2: Failed to create socket", s.getLocalPort() == portNumber);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        portNumber = Support_PortManager.getNextPort();
        try {
            Socket s = sf.createSocket("bla-bla", sport, InetAddress.getLocalHost(), portNumber);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException uhe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
        
        for (int i = 0; i < invalidPorts.length; i++) {
            portNumber = Support_PortManager.getNextPort();
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), invalidPorts[i],
                                           InetAddress.getLocalHost(), portNumber);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport,
                                           InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }

        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket("localhost", 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        }
    }
    
    /**
     * @tests javax.net.SocketFactory#getDefault()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public final void test_getDefault() {
        SocketFactory sf = SocketFactory.getDefault();
        Socket s;
        try {
            s = sf.createSocket("localhost", 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket("localhost", 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081);
            s.close();
        } catch (IOException e) {
        } 
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
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

class MySocketFactory extends SocketFactory {
    
    public MySocketFactory() {
        super();
    }
    
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket s = null;
        try {
            s = new Socket(host, port);
        } catch (java.net.ConnectException ce) {
            throw new IOException("error occurs");
        }
        return s;
    }
    
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        Socket s = null;
        try {
            s = new Socket(host, port, localHost, localPort);
        } catch (java.net.ConnectException ce) {
            throw new IOException("error occurs");
        }
        return s;
    }
    
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket s = null;
        try {
            s = new Socket(host, port);
        } catch (java.net.ConnectException ce) {
            throw new IOException("error occurs");
        }
        return s;
     }
    
    public Socket createSocket(InetAddress address, int port, 
                               InetAddress localAddress, int localPort) throws IOException {
        Socket s = null;
        try {
            s = new Socket(address, port, localAddress, localPort);
        } catch (java.net.ConnectException ce) {
            throw new IOException("error occurs");
        }
        return s;
     }

}

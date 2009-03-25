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
import java.net.SocketException;
import javax.net.ServerSocketFactory;

import junit.framework.TestCase;

import tests.support.Support_PortManager;

 
/**
 * Tests for <code>ServerSocketFactory</code> class constructors and methods.
 */
@TestTargetClass(ServerSocketFactory.class)
public class ServerSocketFactoryTest extends TestCase {

    /**
     * @tests javax.net.SocketFactory#SocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ServerSocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            ServerSocketFactory sf = new MyServerSocketFactory();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests javax.net.ServerSocketFactory#createServerSocket()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException checking missed",
        method = "createServerSocket",
        args = {}
    )
    public final void test_createServerSocket_01() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        try {
            ServerSocket ss = sf.createServerSocket();
            assertNotNull(ss);
        } catch (SocketException e) {        
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    
    /**
     * @tests javax.net.ServerSocketFactory#createServerSocket(int port)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createServerSocket",
        args = {int.class}
    )
    public final void test_createServerSocket_02() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        
        try {
            ServerSocket ss = sf.createServerSocket(portNumber);
            assertNotNull(ss);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
        
        try {
            sf.createServerSocket(portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IOException");
        }
        
        try {
            sf.createServerSocket(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ioe) {
            //expected
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ServerSocketFactory#createServerSocket(int port, int backlog)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createServerSocket",
        args = {int.class, int.class}
    )
    public final void test_createServerSocket_03() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        
        try {
            ServerSocket ss = sf.createServerSocket(portNumber, 0);
            assertNotNull(ss);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
        
        try {
            sf.createServerSocket(portNumber, 0);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IOException");
        }
        
        try {
            sf.createServerSocket(65536, 0);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ioe) {
            //expected
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ServerSocketFactory#createServerSocket(int port, int backlog, InetAddress ifAddress)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createServerSocket",
        args = {int.class, int.class, InetAddress.class}
    )
    public final void test_createServerSocket_04() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        
        try {
            ServerSocket ss = sf.createServerSocket(portNumber, 0, InetAddress.getLocalHost());
            assertNotNull(ss);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
        
        try {
            sf.createServerSocket(portNumber, 0, InetAddress.getLocalHost());
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
            //expected
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IOException");
        }
        
        try {
            sf.createServerSocket(Integer.MAX_VALUE, 0, InetAddress.getLocalHost());
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ioe) {
            //expected
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }

    /**
     * @tests javax.net.ServerSocketFactory#getDefault()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public final void test_getDefault() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        ServerSocket s;
        try {
            s = sf.createServerSocket(0);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createServerSocket(0, 50);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createServerSocket(0, 50, InetAddress.getLocalHost());
            s.close();
        } catch (IOException e) {
        } 
    }
}
class MyServerSocketFactory extends ServerSocketFactory {
    
    public MyServerSocketFactory() {
        super();
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return null;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        return null;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress address) throws IOException {
        return null;
    }
}
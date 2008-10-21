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

package tests.api.java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import tests.support.Support_Configuration;
import tests.support.Support_Exec;

public class ServerSocketTest extends SocketTestCase {

    boolean interrupted;

    ServerSocket s;

    Socket sconn;

    Thread t;

    static class SSClient implements Runnable {
        Socket cs;

        int port;

        public SSClient(int prt) {
            port = prt;
        }

        public void run() {
            try {
                // Go to sleep so the server can setup and wait for connection
                Thread.sleep(1000);
                cs = new Socket(InetAddress.getLocalHost().getHostName(), port);
                // Sleep again to allow server side processing. Thread is
                // stopped by server.
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            } catch (Throwable e) {
                System.out
                        .println("Error establishing client: " + e.toString());
            } finally {
                try {
                    if (cs != null)
                        cs.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * @tests java.net.ServerSocket#ServerSocket()
     */
    public void test_Constructor() {
        // Test for method java.net.ServerSocket(int)
        assertTrue("Used during tests", true);
    }

    /**
     * @tests java.net.ServerSocket#ServerSocket(int)
     */
    public void test_ConstructorI() {
        // Test for method java.net.ServerSocket(int)
        assertTrue("Used during tests", true);
    }

    /**
     * @tests java.net.ServerSocket#ServerSocket(int)
     */
    public void test_ConstructorI_SocksSet() throws IOException {
        // Harmony-623 regression test
        ServerSocket ss = null;
        Properties props = (Properties) System.getProperties().clone();
        try {
            System.setProperty("socksProxyHost", "127.0.0.1");
            System.setProperty("socksProxyPort", "12345");
            ss = new ServerSocket(0);
        } finally {
            System.setProperties(props);
            if (null != ss) {
                ss.close();
            }
        }
    }

    /**
     * @tests java.net.ServerSocket#ServerSocket(int, int)
     */
    public void test_ConstructorII() throws IOException {
        try {
            s = new ServerSocket(0, 10);
            s.setSoTimeout(2000);
            startClient(s.getLocalPort());
            sconn = s.accept();
        } catch (InterruptedIOException e) {
            return;
        }

        ServerSocket s1 = new ServerSocket(0);
        try {
            try {
                ServerSocket s2 = new ServerSocket(s1.getLocalPort());
                s2.close();
                fail("Was able to create two serversockets on same port");
            } catch (BindException e) {
                // Expected
            }
        } finally {
            s1.close();
        }

        s1 = new ServerSocket(0);
        int allocatedPort = s1.getLocalPort();
        s1.close();
        s1 = new ServerSocket(allocatedPort);
        s1.close();
    }

    /**
     * @tests java.net.ServerSocket#ServerSocket(int, int, java.net.InetAddress)
     */
    public void test_ConstructorIILjava_net_InetAddress()
            throws UnknownHostException, IOException {
        s = new ServerSocket(0, 10, InetAddress.getLocalHost());
        try {
            s.setSoTimeout(5000);
            startClient(s.getLocalPort());
            sconn = s.accept();
            assertNotNull("Was unable to accept connection", sconn);
            sconn.close();
        } finally {
            s.close();
        }
    }

    /**
     * @tests java.net.ServerSocket#accept()
     */
    public void test_accept() throws IOException {
        s = new ServerSocket(0);
        try {
            s.setSoTimeout(5000);
            startClient(s.getLocalPort());
            sconn = s.accept();
            int localPort1 = s.getLocalPort();
            int localPort2 = sconn.getLocalPort();
            sconn.close();
            assertEquals("Bad local port value", localPort1, localPort2);
        } finally {
            s.close();
        }

        try {
            interrupted = false;
            final ServerSocket ss = new ServerSocket(0);
            ss.setSoTimeout(12000);
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        ss.accept();
                    } catch (InterruptedIOException e) {
                        interrupted = true;
                    } catch (IOException e) {
                    }
                }
            };
            Thread thread = new Thread(runnable, "ServerSocket.accept");
            thread.start();
            try {
                do {
                    Thread.sleep(500);
                } while (!thread.isAlive());
            } catch (InterruptedException e) {
            }
            ss.close();
            int c = 0;
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (interrupted) {
                    fail("accept interrupted");
                }
                if (++c > 4) {
                    fail("accept call did not exit");
                }
            } while (thread.isAlive());

            interrupted = false;
            ServerSocket ss2 = new ServerSocket(0);
            ss2.setSoTimeout(500);
            Date start = new Date();
            try {
                ss2.accept();
            } catch (InterruptedIOException e) {
                interrupted = true;
            }
            assertTrue("accept not interrupted", interrupted);
            Date finish = new Date();
            int delay = (int) (finish.getTime() - start.getTime());
            assertTrue("timeout too soon: " + delay + " " + start.getTime()
                    + " " + finish.getTime(), delay >= 490);
            ss2.close();
        } catch (IOException e) {
            fail("Unexpected IOException : " + e.getMessage());
        }
    }

    /**
     * @tests java.net.ServerSocket#close()
     */
    public void test_close() throws IOException {
        try {
            s = new ServerSocket(0);
            try {
                s.close();
                s.accept();
                fail("Close test failed");
            } catch (SocketException e) {
                // expected;
            }
        } finally {
            s.close();
        }
    }

    /**
     * @tests java.net.ServerSocket#getInetAddress()
     */
    public void test_getInetAddress() throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        s = new ServerSocket(0, 10, addr);
        try {
            assertEquals("Returned incorrect InetAdrees", addr, s
                    .getInetAddress());
        } finally {
            s.close();
        }
    }

    /**
     * @tests java.net.ServerSocket#getLocalPort()
     */
    public void test_getLocalPort() throws IOException {
        // Try a specific port number, but don't complain if we don't get it
        int portNumber = 63024; // I made this up
        try {
            try {
                s = new ServerSocket(portNumber);
            } catch (BindException e) {
                // we could not get the port, give up
                return;
            }
            assertEquals("Returned incorrect port", portNumber, s
                    .getLocalPort());
        } finally {
            s.close();
        }
    }

    /**
     * @tests java.net.ServerSocket#getSoTimeout()
     */
    public void test_getSoTimeout() throws IOException {
        s = new ServerSocket(0);
        try {
            s.setSoTimeout(100);
            assertEquals("Returned incorrect sotimeout", 100, s.getSoTimeout());
        } finally {
            s.close();
        }
    }

    /**
     * @tests java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     */
    public void test_setSocketFactoryLjava_net_SocketImplFactory() {
        // Test for method void
        // java.net.ServerSocket.setSocketFactory(java.net.SocketImplFactory)

        // TODO : Implementation
    }

    /**
     * @tests java.net.ServerSocket#setSoTimeout(int)
     */
    public void test_setSoTimeoutI() throws IOException {
        // Timeout should trigger and throw InterruptedIOException
        try {
            s = new ServerSocket(0);
            s.setSoTimeout(100);
            s.accept();
        } catch (InterruptedIOException e) {
            try {
                assertEquals("Set incorrect sotimeout", 100, s.getSoTimeout());
                return;
            } catch (Exception x) {
                fail("Exception during setSOTimeout: " + e.toString());
            }
        } catch (IOException iox) {
            fail("IOException during setSotimeout: " + iox.toString());
        }

        // Timeout should not trigger in this case
        s = new ServerSocket(0);
        startClient(s.getLocalPort());
        s.setSoTimeout(10000);
        sconn = s.accept();
    }

    /**
     * @tests java.net.ServerSocket#toString()
     */
    public void test_toString() throws Exception {
        s = new ServerSocket(0);
        try {
            int portNumber = s.getLocalPort();
            assertEquals("ServerSocket[addr=0.0.0.0/0.0.0.0,port=0,localport="
                    + portNumber + "]", s.toString());
        } finally {
            s.close();
        }
    }

    /**
     * @tests java.net.ServerSocket#bind(java.net.SocketAddress)
     */
    public void test_bindLjava_net_SocketAddress() throws IOException {
        class mySocketAddress extends SocketAddress {
            public mySocketAddress() {
            }
        }
        // create servers socket, bind it and then validate basic state
        ServerSocket theSocket = new ServerSocket();
        InetSocketAddress theAddress = new InetSocketAddress(InetAddress
                .getLocalHost(), 0);
        theSocket.bind(theAddress);
        int portNumber = theSocket.getLocalPort();
        assertTrue(
                "Returned incorrect InetSocketAddress(2):"
                        + theSocket.getLocalSocketAddress().toString()
                        + "Expected: "
                        + (new InetSocketAddress(InetAddress.getLocalHost(),
                                portNumber)).toString(), theSocket
                        .getLocalSocketAddress().equals(
                                new InetSocketAddress(InetAddress
                                        .getLocalHost(), portNumber)));
        assertTrue("Server socket not bound when it should be:", theSocket
                .isBound());

        // now make sure that it is actually bound and listening on the
        // address we provided
        Socket clientSocket = new Socket();
        InetSocketAddress clAddress = new InetSocketAddress(InetAddress
                .getLocalHost(), portNumber);
        clientSocket.connect(clAddress);
        Socket servSock = theSocket.accept();

        assertEquals(clAddress, clientSocket.getRemoteSocketAddress());
        theSocket.close();
        servSock.close();
        clientSocket.close();

        // validate we can specify null for the address in the bind and all
        // goes ok
        theSocket = new ServerSocket();
        theSocket.bind(null);
        theSocket.close();

        // Address that we have already bound to
        theSocket = new ServerSocket();
        ServerSocket theSocket2 = new ServerSocket();
        try {
            theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0);
            theSocket.bind(theAddress);
            SocketAddress localAddress = theSocket.getLocalSocketAddress();
            theSocket2.bind(localAddress);
            fail("No exception binding to address that is not available");
        } catch (IOException ex) {
        }
        theSocket.close();
        theSocket2.close();

        // validate we get io address when we try to bind to address we
        // cannot bind to
        theSocket = new ServerSocket();
        try {
            theSocket.bind(new InetSocketAddress(InetAddress
                    .getByAddress(Support_Configuration.nonLocalAddressBytes),
                    0));
            fail("No exception was thrown when binding to bad address");
        } catch (IOException ex) {
        }
        theSocket.close();

        // now validate case where we pass in an unsupported subclass of
        // SocketAddress
        theSocket = new ServerSocket();
        try {
            theSocket.bind(new mySocketAddress());
            fail("No exception when binding using unsupported SocketAddress subclass");
        } catch (IllegalArgumentException ex) {
        }
        theSocket.close();
    }

    /**
     * @tests java.net.ServerSocket#bind(java.net.SocketAddress,int)
     */
    public void test_bindLjava_net_SocketAddressI() throws IOException {
        class mySocketAddress extends SocketAddress {

            public mySocketAddress() {
            }
        }

        // create servers socket, bind it and then validate basic state
        ServerSocket theSocket = new ServerSocket();
        InetSocketAddress theAddress = new InetSocketAddress(InetAddress
                .getLocalHost(), 0);
        theSocket.bind(theAddress, 5);
        int portNumber = theSocket.getLocalPort();
        assertTrue(
                "Returned incorrect InetSocketAddress(2):"
                        + theSocket.getLocalSocketAddress().toString()
                        + "Expected: "
                        + (new InetSocketAddress(InetAddress.getLocalHost(),
                                portNumber)).toString(), theSocket
                        .getLocalSocketAddress().equals(
                                new InetSocketAddress(InetAddress
                                        .getLocalHost(), portNumber)));
        assertTrue("Server socket not bound when it should be:", theSocket
                .isBound());

        // now make sure that it is actually bound and listening on the
        // address we provided
        SocketAddress localAddress = theSocket.getLocalSocketAddress();
        Socket clientSocket = new Socket();
        clientSocket.connect(localAddress);
        Socket servSock = theSocket.accept();

        assertTrue(clientSocket.getRemoteSocketAddress().equals(localAddress));
        theSocket.close();
        servSock.close();
        clientSocket.close();

        // validate we can specify null for the address in the bind and all
        // goes ok
        theSocket = new ServerSocket();
        theSocket.bind(null, 5);
        theSocket.close();

        // Address that we have already bound to
        theSocket = new ServerSocket();
        ServerSocket theSocket2 = new ServerSocket();
        try {
            theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0);
            theSocket.bind(theAddress, 5);
            SocketAddress inuseAddress = theSocket.getLocalSocketAddress();
            theSocket2.bind(inuseAddress, 5);
            fail("No exception binding to address that is not available");
        } catch (IOException ex) {
            // expected
        }
        theSocket.close();
        theSocket2.close();

        // validate we get ioException when we try to bind to address we
        // cannot bind to
        theSocket = new ServerSocket();
        try {
            theSocket.bind(new InetSocketAddress(InetAddress
                    .getByAddress(Support_Configuration.nonLocalAddressBytes),
                    0), 5);
            fail("No exception was thrown when binding to bad address");
        } catch (IOException ex) {
        }
        theSocket.close();

        // now validate case where we pass in an unsupported subclass of
        // SocketAddress
        theSocket = new ServerSocket();
        try {
            theSocket.bind(new mySocketAddress(), 5);
            fail("Binding using unsupported SocketAddress subclass should have thrown exception");
        } catch (IllegalArgumentException ex) {
        }
        theSocket.close();

        // now validate that backlog is respected. We have to do a test that
        // checks if it is a least a certain number as some platforms make
        // it higher than we request. Unfortunately non-server versions of
        // windows artificially limit the backlog to 5 and 5 is the
        // historical default so it it not a great test.
        theSocket = new ServerSocket();
        theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0);
        theSocket.bind(theAddress, 4);
        localAddress = theSocket.getLocalSocketAddress();
        Socket theSockets[] = new Socket[4];
        int i = 0;
        try {
            for (i = 0; i < 4; i++) {
                theSockets[i] = new Socket();
                theSockets[i].connect(localAddress);
            }
        } catch (ConnectException ex) {
            fail("Backlog does not seem to be respected in bind:" + i + ":"
                    + ex.toString());
        }

        for (i = 0; i < 4; i++) {
            theSockets[i].close();
        }

        theSocket.close();
        servSock.close();
    }

    /**
     * @tests java.net.ServerSocket#getLocalSocketAddress()
     */
    public void test_getLocalSocketAddress() {
        // set up server connect and then validate that we get the right
        // response for the local address
        try {
            ServerSocket theSocket = new ServerSocket(0, 5, InetAddress
                    .getLocalHost());
            int portNumber = theSocket.getLocalPort();
            assertTrue("Returned incorrect InetSocketAddress(1):"
                    + theSocket.getLocalSocketAddress().toString()
                    + "Expected: "
                    + (new InetSocketAddress(InetAddress.getLocalHost(),
                            portNumber)).toString(), theSocket
                    .getLocalSocketAddress().equals(
                            new InetSocketAddress(InetAddress.getLocalHost(),
                                    portNumber)));
            theSocket.close();

            // now create a socket that is not bound and validate we get the
            // right answer
            theSocket = new ServerSocket();
            assertNull(
                    "Returned incorrect InetSocketAddress -unbound socket- Expected null",
                    theSocket.getLocalSocketAddress());

            // now bind the socket and make sure we get the right answer
            theSocket
                    .bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
            int localPort = theSocket.getLocalPort();
            assertEquals("Returned incorrect InetSocketAddress(2):", theSocket
                    .getLocalSocketAddress(), new InetSocketAddress(InetAddress
                    .getLocalHost(), localPort));
            theSocket.close();
        } catch (Exception e) {
            fail("Exception during getLocalSocketAddress test: " + e);
        }
    }

    /**
     * @tests java.net.ServerSocket#isBound()
     */
    public void test_isBound() throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        ServerSocket serverSocket = new ServerSocket();
        assertFalse("Socket indicated bound when it should be (1)",
                serverSocket.isBound());

        // now bind and validate bound ok
        serverSocket.bind(new InetSocketAddress(addr, 0));
        assertTrue("Socket indicated  not bound when it should be (1)",
                serverSocket.isBound());
        serverSocket.close();

        // now do with some of the other constructors
        serverSocket = new ServerSocket(0);
        assertTrue("Socket indicated  not bound when it should be (2)",
                serverSocket.isBound());
        serverSocket.close();

        serverSocket = new ServerSocket(0, 5, addr);
        assertTrue("Socket indicated  not bound when it should be (3)",
                serverSocket.isBound());
        serverSocket.close();

        serverSocket = new ServerSocket(0, 5);
        assertTrue("Socket indicated  not bound when it should be (4)",
                serverSocket.isBound());
        serverSocket.close();
    }

    /**
     * @tests java.net.ServerSocket#isClosed()
     */
    public void test_isClosed() throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        ServerSocket serverSocket = new ServerSocket(0, 5, addr);

        // validate isClosed returns expected values
        assertFalse("Socket should indicate it is not closed(1):", serverSocket
                .isClosed());
        serverSocket.close();
        assertTrue("Socket should indicate it is closed(1):", serverSocket
                .isClosed());

        // now do with some of the other constructors
        serverSocket = new ServerSocket(0);
        assertFalse("Socket should indicate it is not closed(1):", serverSocket
                .isClosed());
        serverSocket.close();
        assertTrue("Socket should indicate it is closed(1):", serverSocket
                .isClosed());

        serverSocket = new ServerSocket(0, 5, addr);
        assertFalse("Socket should indicate it is not closed(1):", serverSocket
                .isClosed());
        serverSocket.close();
        assertTrue("Socket should indicate it is closed(1):", serverSocket
                .isClosed());

        serverSocket = new ServerSocket(0, 5);
        assertFalse("Socket should indicate it is not closed(1):", serverSocket
                .isClosed());
        serverSocket.close();
        assertTrue("Socket should indicate it is closed(1):", serverSocket
                .isClosed());
    }

    /**
     * @tests java.net.ServerSocket#setReuseAddress(boolean)
     */
    public void test_setReuseAddressZ() {
        try {
            // set up server and connect
            InetSocketAddress anyAddress = new InetSocketAddress(InetAddress
                    .getLocalHost(), 0);
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(false);
            serverSocket.bind(anyAddress);
            SocketAddress theAddress = serverSocket.getLocalSocketAddress();

            // make a connection to the server, then close the server
            Socket theSocket = new Socket();
            theSocket.connect(theAddress);
            Socket stillActiveSocket = serverSocket.accept();
            serverSocket.close();

            // now try to rebind the server which should fail with
            // setReuseAddress to false. On windows platforms the bind is
            // allowed even then reUseAddress is false so our test uses
            // the platform to determine what the expected result is.
            String platform = System.getProperty("os.name");
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(false);
                serverSocket.bind(theAddress);
                if ((!platform.startsWith("Windows"))) {
                    fail("No exception when setReuseAddress is false and we bind:"
                            + theAddress.toString());
                }
            } catch (IOException ex) {
                if (platform.startsWith("Windows")) {
                    fail("Got unexpected exception when binding with setReuseAddress false on windows platform:"
                            + theAddress.toString() + ":" + ex.toString());
                }
            }
            stillActiveSocket.close();
            theSocket.close();

            // now test case were we set it to true
            anyAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0);
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(anyAddress);
            theAddress = serverSocket.getLocalSocketAddress();

            // make a connection to the server, then close the server
            theSocket = new Socket();
            theSocket.connect(theAddress);
            stillActiveSocket = serverSocket.accept();
            serverSocket.close();

            // now try to rebind the server which should pass with
            // setReuseAddress to true
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(theAddress);
            } catch (IOException ex) {
                fail("Unexpected exception when setReuseAddress is true and we bind:"
                        + theAddress.toString() + ":" + ex.toString());
            }
            stillActiveSocket.close();
            theSocket.close();
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_REUSEADDR);

            // now test default case were we expect this to work regardless of
            // the value set
            anyAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0);
            serverSocket = new ServerSocket();
            serverSocket.bind(anyAddress);
            theAddress = serverSocket.getLocalSocketAddress();

            // make a connection to the server, then close the server
            theSocket = new Socket();
            theSocket.connect(theAddress);
            stillActiveSocket = serverSocket.accept();
            serverSocket.close();

            // now try to rebind the server which should pass
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(theAddress);
            } catch (IOException ex) {
                fail("Unexpected exception when setReuseAddress is the default case and we bind:"
                        + theAddress.toString() + ":" + ex.toString());
            }
            stillActiveSocket.close();
            theSocket.close();

            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_REUSEADDR);
        } catch (Exception e) {
            handleException(e, SO_REUSEADDR);
        }
    }

    /**
     * @tests java.net.ServerSocket#getReuseAddress()
     */
    public void test_getReuseAddress() {
        try {
            ServerSocket theSocket = new ServerSocket();
            theSocket.setReuseAddress(true);
            assertTrue("getReuseAddress false when it should be true",
                    theSocket.getReuseAddress());
            theSocket.setReuseAddress(false);
            assertFalse("getReuseAddress true when it should be False",
                    theSocket.getReuseAddress());
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_REUSEADDR);
        } catch (Exception e) {
            handleException(e, SO_REUSEADDR);
        }
    }

    /**
     * @tests java.net.ServerSocket#setReceiveBufferSize(int)
     */
    public void test_setReceiveBufferSizeI() {
        try {
            // now validate case where we try to set to 0
            ServerSocket theSocket = new ServerSocket();
            try {
                theSocket.setReceiveBufferSize(0);
                fail("No exception when receive buffer size set to 0");
            } catch (IllegalArgumentException ex) {
            }
            theSocket.close();

            // now validate case where we try to set to a negative value
            theSocket = new ServerSocket();
            try {
                theSocket.setReceiveBufferSize(-1000);
                fail("No exception when receive buffer size set to -1000");
            } catch (IllegalArgumentException ex) {
            }
            theSocket.close();

            // now just try to set a good value to make sure it is set and there
            // are not exceptions
            theSocket = new ServerSocket();
            theSocket.setReceiveBufferSize(1000);
            theSocket.close();
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_RCVBUF);
        } catch (Exception e) {
            handleException(e, SO_RCVBUF);
        }

    }

    /*
     * @tests java.net.ServerSocket#getReceiveBufferSize()
     */
    public void test_getReceiveBufferSize() {
        try {
            ServerSocket theSocket = new ServerSocket();

            // since the value returned is not necessary what we set we are
            // limited in what we can test
            // just validate that it is not 0 or negative
            assertFalse("get Buffer size returns 0:", 0 == theSocket
                    .getReceiveBufferSize());
            assertFalse("get Buffer size returns  a negative value:",
                    0 > theSocket.getReceiveBufferSize());
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_RCVBUF);
        } catch (Exception e) {
            handleException(e, SO_RCVBUF);
        }
    }

    /**
     * @tests java.net.ServerSocket#getChannel()
     */
    public void test_getChannel() throws Exception {
        assertNull(new ServerSocket().getChannel());
    }

    /*
     * @tests java.net.ServerSocket#setPerformancePreference()
     */
    public void test_setPerformancePreference_Int_Int_Int() throws Exception {
        ServerSocket theSocket = new ServerSocket();
        theSocket.setPerformancePreferences(1, 1, 1);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {

        try {
            if (s != null)
                s.close();
            if (sconn != null)
                sconn.close();
            if (t != null)
                t.interrupt();
        } catch (Exception e) {
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void startClient(int port) {
        t = new Thread(new SSClient(port), "SSClient");
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Exception during startClinet()" + e.toString());
        }
    }

    /**
     * @tests java.net.ServerSocket#implAccept
     */
    public void test_implAcceptLjava_net_Socket() throws Exception {
        // regression test for Harmony-1235
        try {
            new MockServerSocket().mockImplAccept(new MockSocket(
                    new MockSocketImpl()));
        } catch (SocketException e) {
            // expected
        }
    }
    
    /**
     * Regression for HARMONY-3265
     * @throws Exception
     */
    public void test_ServerSocket_init() throws Exception {
        String[] args = new String[]{"tests.api.java.net.TestServerSocketInit"};
        Support_Exec.execJava(args, null, true);
    }

    static class MockSocketImpl extends SocketImpl {
        protected void create(boolean arg0) throws IOException {
            // empty
        }

        protected void connect(String arg0, int arg1) throws IOException {
            // empty
        }

        protected void connect(InetAddress arg0, int arg1) throws IOException {
            // empty
        }

        protected void connect(SocketAddress arg0, int arg1) throws IOException {
            // empty
        }

        protected void bind(InetAddress arg0, int arg1) throws IOException {
            // empty
        }

        protected void listen(int arg0) throws IOException {
            // empty
        }

        protected void accept(SocketImpl arg0) throws IOException {
            // empty
        }

        protected InputStream getInputStream() throws IOException {
            return null;
        }

        protected OutputStream getOutputStream() throws IOException {
            return null;
        }

        protected int available() throws IOException {
            return 0;
        }

        protected void close() throws IOException {
            // empty
        }

        protected void sendUrgentData(int arg0) throws IOException {
            // empty
        }

        public void setOption(int arg0, Object arg1) throws SocketException {
            // empty
        }

        public Object getOption(int arg0) throws SocketException {
            return null;
        }
    }

    static class MockSocket extends Socket {
        public MockSocket(SocketImpl impl) throws SocketException {
            super(impl);
        }
    }

    static class MockServerSocket extends ServerSocket {
        public MockServerSocket() throws Exception {
            super();
        }

        public void mockImplAccept(Socket s) throws Exception {
            super.implAccept(s);
        }
    }
    
    public void test_LocalPort() throws IOException {
        ServerSocket ss1 = new ServerSocket(4242);
        assertEquals(ss1.getLocalPort(), 4242);
        ss1.close();
        
        ServerSocket ss2 = new ServerSocket();
        ss2.bind(new InetSocketAddress("127.0.0.1", 4343));        
        assertEquals(ss2.getLocalPort(), 4343);
        ss2.close();
        
        ServerSocket ss3 = new ServerSocket(0);
        assertTrue(ss3.getLocalPort() != 0);
        ss3.close();
    }
}

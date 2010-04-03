/*
 * Copyright (C) 2010 The Android Open Source Project
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

package javax.net.ssl;

import dalvik.annotation.KnownFailure;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import junit.framework.TestCase;

public class SSLSocketTest extends TestCase {

    public void test_SSLSocket_getSupportedCipherSuites() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();
        final String[] cs = ssl.getSupportedCipherSuites();
        assertNotNull(cs);
        assertTrue(cs.length != 0);
    }

    public void test_SSLSocket_getEnabledCipherSuites() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();
        final String[] cs = ssl.getEnabledCipherSuites();
        assertNotNull(cs);
        assertTrue(cs.length != 0);
    }

    @KnownFailure("Should support disabling all cipher suites")
    public void test_SSLSocket_setEnabledCipherSuites() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();

        try {
            ssl.setEnabledCipherSuites(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            ssl.setEnabledCipherSuites(new String[1]);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            ssl.setEnabledCipherSuites(new String[] { "Bogus" } );
            fail();
        } catch (IllegalArgumentException e) {
        }

        ssl.setEnabledCipherSuites(new String[0]);
        ssl.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
        ssl.setEnabledCipherSuites(ssl.getSupportedCipherSuites());
    }

    public void test_SSLSocket_getSupportedProtocols() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();
        final String[] p = ssl.getSupportedProtocols();
        assertNotNull(p);
        assertTrue(p.length != 0);
    }

    public void test_SSLSocket_getEnabledProtocols() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();
        final String[] p = ssl.getEnabledProtocols();
        assertNotNull(p);
        assertTrue(p.length != 0);
    }

    @KnownFailure("Should thow IllegalArgumentException not NullPointerException on null enabled protocols argument")
    public void test_SSLSocket_setEnabledProtocols() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();

        try {
            ssl.setEnabledProtocols(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            ssl.setEnabledProtocols(new String[1]);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            ssl.setEnabledProtocols(new String[] { "Bogus" } );
            fail();
        } catch (IllegalArgumentException e) {
        }
        ssl.setEnabledProtocols(new String[0]);
        ssl.setEnabledProtocols(ssl.getEnabledProtocols());
        ssl.setEnabledProtocols(ssl.getSupportedProtocols());
    }

    @KnownFailure("session of unconnected socket should not be valid")
    public void test_SSLSocket_getSession() throws Exception {
        final SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLSocket ssl = (SSLSocket) sf.createSocket();
        final SSLSession session = ssl.getSession();
        assertNotNull(session);
        assertFalse(session.isValid());
    }

    @KnownFailure("Implementation should not start handshake in ServerSocket.accept")
    public void test_SSLSocket_startHandshake() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        if (!SSLContextTest.IS_RI) {
            /*
             * The following hangs in accept in the Dalvik
             * implementation because accept is also incorrectly
             * starting the handhake.
            */
            c.serverSocket.setSoTimeout(1 * 1000);
            /*
             * That workaround doesn't seem to work so...
             *
             * See test_SSLSocket_startHandshake_workaround for
             * redundant version of this test that works around this
             * issue.
             */
            fail();
        }
        final SSLSocket server = (SSLSocket) c.serverSocket.accept();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    server.startHandshake();
                    assertNotNull(server.getSession());
                    try {
                        server.getSession().getPeerCertificates();
                        fail();
                    } catch (SSLPeerUnverifiedException e) {
                    }
                    Certificate[] localCertificates = server.getSession().getLocalCertificates();
                    assertNotNull(localCertificates);
                    assertEquals(1, localCertificates.length);
                    assertNotNull(localCertificates[0]);
                    assertNotNull(localCertificates[0].equals(c.keyStore.getCertificate(c.privateAlias)));
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        client.startHandshake();
        assertNotNull(client.getSession());
        assertNull(client.getSession().getLocalCertificates());
        Certificate[] peerCertificates = client.getSession().getPeerCertificates();
        assertNotNull(peerCertificates);
        assertEquals(1, peerCertificates.length);
        assertNotNull(peerCertificates[0]);
        assertNotNull(peerCertificates[0].equals(c.keyStore.getCertificate(c.publicAlias)));
        thread.join();
    }

    @KnownFailure("local certificates should be null as it should not have been requested by server")
    public void test_SSLSocket_startHandshake_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    server.startHandshake();
                    assertNotNull(server.getSession());
                    try {
                        server.getSession().getPeerCertificates();
                        fail();
                    } catch (SSLPeerUnverifiedException e) {
                    }
                    Certificate[] localCertificates = server.getSession().getLocalCertificates();
                    assertNotNull(localCertificates);
                    assertEquals(1, localCertificates.length);
                    assertNotNull(localCertificates[0]);
                    assertNotNull(localCertificates[0].equals(c.keyStore.getCertificate(c.privateAlias)));
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        client.startHandshake();
        assertNotNull(client.getSession());
        assertNull(client.getSession().getLocalCertificates());
        Certificate[] peerCertificates = client.getSession().getPeerCertificates();
        assertNotNull(peerCertificates);
        assertEquals(1, peerCertificates.length);
        assertNotNull(peerCertificates[0]);
        assertNotNull(peerCertificates[0].equals(c.keyStore.getCertificate(c.publicAlias)));
        thread.join();
    }

    @KnownFailure("Should throw SSLException on server, not IOException on client")
    public void test_SSLSocket_startHandshake_noKeyStore_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create(null, null, null, null);
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    c.serverSocket.accept();
                    fail();
                } catch (SSLException e) {
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        if (!SSLContextTest.IS_RI) {
            client.startHandshake();
        }
        thread.join();
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround
     */
    @KnownFailure("local certificates should be null as it should not have been requested by server")
    public void test_SSLSocket_HandshakeCompletedListener_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    server.startHandshake();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        client.addHandshakeCompletedListener(new HandshakeCompletedListener() {
            public void handshakeCompleted(final HandshakeCompletedEvent event) {
                try {
                    SSLSession session = event.getSession();
                    String cipherSuite = event.getCipherSuite();
                    Certificate[] localCertificates = event.getLocalCertificates();
                    Certificate[] peerCertificates = event.getPeerCertificates();
                    javax.security.cert.X509Certificate[] peerCertificateChain = event.getPeerCertificateChain();
                    Principal peerPrincipal = event.getPeerPrincipal();
                    Principal localPrincipal = event.getLocalPrincipal();
                    Socket socket = event.getSocket();

                    if (false) {
                        System.out.println("Session=" + session);
                        System.out.println("CipherSuite=" + cipherSuite);
                        System.out.println("LocalCertificates=" + localCertificates);
                        System.out.println("PeerCertificates=" + peerCertificates);
                        System.out.println("PeerCertificateChain=" + peerCertificateChain);
                        System.out.println("PeerPrincipal=" + peerPrincipal);
                        System.out.println("LocalPrincipal=" + localPrincipal);
                        System.out.println("Socket=" + socket);
                    }

                    assertNotNull(session);
                    byte[] id = session.getId();
                    assertNotNull(id);
                    assertEquals(32, id.length);
                    assertNotNull(c.sslContext.getClientSessionContext().getSession(id));
                    assertNotNull(c.sslContext.getServerSessionContext().getSession(id));

                    assertNotNull(cipherSuite);
                    assertTrue(Arrays.asList(client.getEnabledCipherSuites()).contains(cipherSuite));
                    assertTrue(Arrays.asList(c.serverSocket.getEnabledCipherSuites()).contains(cipherSuite));

                    final Enumeration e = c.keyStore.aliases();
                    Certificate certificate = null;
                    Key key = null;
                    while (e.hasMoreElements()) {
                        String alias = (String) e.nextElement();
                        if (c.keyStore.isCertificateEntry(alias)) {
                            assertNull(certificate);
                            certificate = c.keyStore.getCertificate(alias);
                        } else if (c.keyStore.isKeyEntry(alias)) {
                            assertNull(key);
                            key = c.keyStore.getKey(alias, c.keyStorePassword);
                        }
                        else {
                            fail();
                        }
                    }
                    assertNotNull(certificate);
                    assertNotNull(key);

                    assertTrue(X509Certificate.class.isAssignableFrom(certificate.getClass()));
                    final X509Certificate x509certificate = (X509Certificate) certificate;

                    assertNull(localCertificates);

                    assertNotNull(peerCertificates);
                    assertEquals(1, peerCertificates.length);
                    assertNotNull(peerCertificates[0]);
                    assertEquals(peerCertificates[0], x509certificate);

                    assertNotNull(peerCertificateChain);
                    assertEquals(1, peerCertificateChain.length);
                    assertNotNull(peerCertificateChain[0]);
                    assertEquals(x509certificate.getSubjectDN().getName(),
                                 peerCertificateChain[0].getSubjectDN().getName());

                    assertNotNull(peerPrincipal);
                    assertEquals(x509certificate.getSubjectDN().getName(),
                                 peerPrincipal.getName());

                    assertNull(localPrincipal);

                    assertNotNull(socket);
                    assertSame(client, socket);

                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        client.startHandshake();
        thread.join();
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround.
     * Technically this test shouldn't even need a second thread.
     */
    public void test_SSLSocket_getUseClientMode_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    assertFalse(server.getUseClientMode());
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        if (!SSLContextTest.IS_RI) {
            client.startHandshake();
        }
        assertTrue(client.getUseClientMode());
        thread.join();
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround.
     * Technically this test shouldn't even need a second thread.
     */
    @KnownFailure("This test relies on socket timeouts which are not working. It also fails because ServerSocket.accept is handshaking")
    public void test_SSLSocket_setUseClientMode_workaround() throws Exception {
        // client is client, server is server
        test_SSLSocket_setUseClientMode_workaround(true, false);
        // client is server, server is client
        test_SSLSocket_setUseClientMode_workaround(true, false);
        // both are client
        try {
            test_SSLSocket_setUseClientMode_workaround(true, true);
            fail();
        } catch (SSLProtocolException e) {
        }

        // both are server
        try {
            test_SSLSocket_setUseClientMode_workaround(false, false);
            fail();
        } catch (SocketTimeoutException e) {
        }
    }

    private void test_SSLSocket_setUseClientMode_workaround(final boolean clientClientMode,
                                                            final boolean serverClientMode)
            throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final SSLProtocolException[] sslProtocolException = new SSLProtocolException[1];
        final SocketTimeoutException[] socketTimeoutException = new SocketTimeoutException[1];
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    if (!serverClientMode) {
                        server.setSoTimeout(1 * 1000);
                        if (!SSLContextTest.IS_RI) {
                            /* as above setSoTimeout isn't working in dalvikvm */
                            fail();
                        }
                    }
                    server.setUseClientMode(serverClientMode);
                    server.startHandshake();
                } catch (SSLProtocolException e) {
                    sslProtocolException[0] = e;
                } catch (SocketTimeoutException e) {
                    socketTimeoutException[0] = e;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        if (!clientClientMode) {
            client.setSoTimeout(1 * 1000);
            if (!SSLContextTest.IS_RI) {
                /* as above setSoTimeout isn't working in dalvikvm */
                fail();
            }
        }
        client.setUseClientMode(clientClientMode);
        client.startHandshake();
        thread.join();
        if (sslProtocolException[0] != null) {
            throw sslProtocolException[0];
        }
        if (socketTimeoutException[0] != null) {
            throw socketTimeoutException[0];
        }
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround
     */
    public void test_SSLSocket_clientAuth_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    assertFalse(server.getWantClientAuth());
                    assertFalse(server.getNeedClientAuth());

                    // confirm turning one on by itself
                    server.setWantClientAuth(true);
                    assertTrue(server.getWantClientAuth());
                    assertFalse(server.getNeedClientAuth());

                    // confirm turning setting on toggles the other
                    server.setNeedClientAuth(true);
                    assertFalse(server.getWantClientAuth());
                    assertTrue(server.getNeedClientAuth());

                    // confirm toggling back
                    server.setWantClientAuth(true);
                    assertTrue(server.getWantClientAuth());
                    assertFalse(server.getNeedClientAuth());

                    server.startHandshake();

                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        client.startHandshake();
        assertNotNull(client.getSession().getLocalCertificates());
        assertEquals(1, client.getSession().getLocalCertificates().length);
        thread.join();
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround
     * Technically this test shouldn't even need a second thread.
     */
    public void test_SSLSocket_getEnableSessionCreation_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    assertTrue(server.getEnableSessionCreation());
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        assertTrue(client.getEnableSessionCreation());
        if (!SSLContextTest.IS_RI) {
            client.startHandshake();
        }
        thread.join();
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround
     */
    @KnownFailure("Server side session creation disabling does not work, should throw SSLException, not fail")
    public void test_SSLSocket_setEnableSessionCreation_server_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    server.setEnableSessionCreation(false);
                    try {
                        server.startHandshake();
                        fail();
                    } catch (SSLException e) {
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        try {
            client.startHandshake();
            fail();
        } catch (SSLException e) {
        }
        thread.join();
    }

    /**
     * Marked workaround because it avoid accepting on main thread like test_SSLSocket_startHandshake_workaround
     */
    @KnownFailure("Should throw SSLException on server, not IOException")
    public void test_SSLSocket_setEnableSessionCreation_client_workaround() throws Exception {
        final SSLContextTest.Helper c = SSLContextTest.Helper.create();
        final Thread thread = new Thread(new Runnable () {
            public void run() {
                try {
                    final SSLSocket server = (SSLSocket) c.serverSocket.accept();
                    try {
                        server.startHandshake();
                        fail();
                    } catch (SSLException e) {
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        final SSLSocket client = (SSLSocket) c.sslContext.getSocketFactory().createSocket(c.host, c.port);
        client.setEnableSessionCreation(false);
        try {
            client.startHandshake();
            fail();
        } catch (SSLException e) {
            if (!SSLContextTest.IS_RI) {
                fail();
            }
        }
        thread.join();
    }

    /**
     * SSLSocketTest.Helper is a convenience class for other tests that
     * want a pair of connected and handshaked client and server
     * SSLSocketsfor testing so they don't have to duplicate the
     * logic.
     */
    public static final class Helper {
        public final SSLContextTest.Helper c;
        public final SSLSocket server;
        public final SSLSocket client;

        private Helper (final SSLContextTest.Helper c,
                        final SSLSocket server,
                        final SSLSocket client) {
            this.c = c;
            this.server = server;
            this.client = client;
        }

        /**
         * based on test_SSLSocket_startHandshake_workaround, should
         * be written to non-workaround form when possible
         */
        public static Helper create_workaround () {
            final SSLContextTest.Helper c = SSLContextTest.Helper.create();
            final SSLSocket[] sockets = connect_workaround(c, null);
            return new Helper(c, sockets[0], sockets[1]);
        }

        /**
         * Create a new connected server/client socket pair within a
         * existing SSLContext. Optional clientCipherSuites allows
         * forcing new SSLSession to test SSLSessionContext caching
         */
        public static SSLSocket[] connect_workaround (final SSLContextTest.Helper c,
                                                      String[] clientCipherSuites) {
            try {
                final SSLSocket[] server = new SSLSocket[1];
                final Thread thread = new Thread(new Runnable () {
                    public void run() {
                        try {
                            server[0] = (SSLSocket) c.serverSocket.accept();
                            server[0].startHandshake();
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
                final SSLSocket client = (SSLSocket)
                    c.sslContext.getSocketFactory().createSocket(c.host, c.port);
                if (clientCipherSuites != null) {
                    client.setEnabledCipherSuites(clientCipherSuites);
                }
                client.startHandshake();
                thread.join();
                return new SSLSocket[] { server[0], client };
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void test_SSLSocketTest_Test_create() {
        Helper test = Helper.create_workaround();
        assertNotNull(test.c);
        assertNotNull(test.server);
        assertNotNull(test.client);
        assertNotNull(test.server.isConnected());
        assertNotNull(test.client.isConnected());
        assertNotNull(test.server.getSession());
        assertNotNull(test.client.getSession());
    }
}

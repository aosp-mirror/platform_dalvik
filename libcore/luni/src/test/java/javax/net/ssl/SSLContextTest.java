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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Hashtable;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import junit.framework.TestCase;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class SSLContextTest extends TestCase {

    public static final boolean IS_RI = !"Dalvik Core Library".equals(System.getProperty("java.specification.name"));
    public static final String PROVIDER_NAME = (IS_RI) ? "SunJSSE" : "HarmonyJSSE";

    public void test_SSLContext_getInstance() throws Exception {
        try {
            SSLContext.getInstance(null);
            fail();
        } catch (NullPointerException e) {
        }
        assertNotNull(SSLContext.getInstance("SSL"));
        assertNotNull(SSLContext.getInstance("SSLv3"));
        assertNotNull(SSLContext.getInstance("TLS"));
        assertNotNull(SSLContext.getInstance("TLSv1"));

        assertNotSame(SSLContext.getInstance("TLS"),
                      SSLContext.getInstance("TLS"));

        try {
            SSLContext.getInstance(null, (String) null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLContext.getInstance(null, "");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLContext.getInstance("TLS", (String) null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLContext.getInstance(null, PROVIDER_NAME);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void test_SSLContext_getProtocol() throws Exception {
        assertProtocolExistsForName("SSL");
        assertProtocolExistsForName("TLS");
    }

    private void assertProtocolExistsForName(String protocolName) throws Exception {
        String protocol = SSLContext.getInstance(protocolName).getProtocol();
        assertNotNull(protocol);
        assertEquals(protocolName, protocol);
    }

    public void test_SSLContext_getProvider() throws Exception {
        Provider provider = SSLContext.getInstance("TLS").getProvider();
        assertNotNull(provider);
        assertEquals(PROVIDER_NAME, provider.getName());
    }

    public void test_SSLContext_init() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
    }

    public void test_SSLContext_getSocketFactory() throws Exception {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.getSocketFactory();
            fail();
        } catch (IllegalStateException e) {
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        SocketFactory sf = sslContext.getSocketFactory();
        assertNotNull(sf);
        assertTrue(SSLSocketFactory.class.isAssignableFrom(sf.getClass()));
    }

    public void test_SSLContext_getServerSocketFactory() throws Exception {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.getServerSocketFactory();
            fail();
        } catch (IllegalStateException e) {
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        ServerSocketFactory ssf = sslContext.getServerSocketFactory();
        assertNotNull(ssf);
        assertTrue(SSLServerSocketFactory.class.isAssignableFrom(ssf.getClass()));
    }

    public void test_SSLContext_createSSLEngine() throws Exception {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.createSSLEngine();
            fail();
        } catch (IllegalStateException e) {
        }
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.createSSLEngine(null, -1);
            fail();
        } catch (IllegalStateException e) {
        }
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            SSLEngine se = sslContext.createSSLEngine();
            assertNotNull(se);
        }
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            SSLEngine se = sslContext.createSSLEngine(null, -1);
            assertNotNull(se);
        }
    }

    @KnownFailure("Should be able to ask SSLContext for SSLSessionContext's before called SSLContext.init")
    public void test_SSLContext_getServerSessionContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        SSLSessionContext sessionContext = sslContext.getServerSessionContext();
        assertNotNull(sessionContext);

        assertNotSame(SSLContext.getInstance("TLS").getServerSessionContext(),
                      sessionContext);
    }

    @KnownFailure("Should be able to ask SSLContext for SSLSessionContext's before called SSLContext.init")
    public void test_SSLContext_getClientSessionContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        SSLSessionContext sessionContext = sslContext.getClientSessionContext();
        assertNotNull(sessionContext);

        assertNotSame(SSLContext.getInstance("TLS").getClientSessionContext(),
                      sessionContext);
    }

    /**
     * SSLContextTest.Helper is a convenience class for other tests that
     * want a canned SSLContext and related state for testing so they
     * don't have to duplicate the logic.
     */
    public static final class Helper {

        static {
            if (SSLContextTest.IS_RI) {
                Security.addProvider(new BouncyCastleProvider());
            }
        }

        public final KeyStore keyStore;
        public final char[] keyStorePassword;
        public final String publicAlias;
        public final String privateAlias;
        public final SSLContext sslContext;
        public final SSLServerSocket serverSocket;
        public final InetAddress host;
        public final int port;

        private Helper(final KeyStore keyStore,
                       final char[] keyStorePassword,
                       final String publicAlias,
                       final String privateAlias,
                       final SSLContext sslContext,
                       final SSLServerSocket serverSocket,
                       final InetAddress host,
                       final int port) {
            this.keyStore = keyStore;
            this.keyStorePassword = keyStorePassword;
            this.publicAlias = publicAlias;
            this.privateAlias = privateAlias;
            this.sslContext = sslContext;
            this.serverSocket = serverSocket;
            this.host = host;
            this.port = port;
        }

        public static Helper create() {
            try {
                final char[] keyStorePassword = null;
                final String publicAlias = "public";
                final String privateAlias = "private";
                return create(createKeyStore(keyStorePassword, publicAlias, privateAlias),
                              null,
                              publicAlias,
                              privateAlias);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static Helper create(final KeyStore keyStore,
                                    final char[] keyStorePassword,
                                    final String publicAlias,
                                    final String privateAlias) {
            try {
                final SSLContext sslContext = createSSLContext(keyStore, keyStorePassword);

                final SSLServerSocket serverSocket = (SSLServerSocket)
                    sslContext.getServerSocketFactory().createServerSocket(0);
                final InetSocketAddress sa = (InetSocketAddress) serverSocket.getLocalSocketAddress();
                final InetAddress host = sa.getAddress();
                final int port = sa.getPort();

                return new Helper(keyStore, keyStorePassword, publicAlias, privateAlias,
                                  sslContext, serverSocket, host, port);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Create a BKS KeyStore containing an RSAPrivateKey with alias
         * "private" and a X509Certificate based on the matching
         * RSAPublicKey stored under the alias name publicAlias.
         *
         * The private key will have a certificate chain including the
         * certificate stored under the alias name privateAlias. The
         * certificate will be signed by the private key. The certificate
         * Subject and Issuer Common-Name will be the local host's
         * canonical hostname. The certificate will be valid for one day
         * before and one day after the time of creation.
         *
         * The KeyStore is optionally password protected by the
         * keyStorePassword argument, which can be null if a password is
         * not desired.
         *
         * Based on:
         * org.bouncycastle.jce.provider.test.SigTest
         * org.bouncycastle.jce.provider.test.CertTest
         */
        public static KeyStore createKeyStore(final char[] keyStorePassword,
                                              String publicAlias,
                                              String privateAlias)
                throws Exception {

            // 1.) we make the keys
            final int keysize = 1024;
            final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(keysize, new SecureRandom());
            final KeyPair kp = kpg.generateKeyPair();
            final RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();
            final RSAPublicKey publicKey  = (RSAPublicKey)kp.getPublic();

            // 2.) use keys to make certficate

            // note that there doesn't seem to be a standard way to make a
            // certificate using java.* or javax.*. The CertificateFactory
            // interface assumes you want to read in a stream of bytes a
            // factory specific format. So here we use Bouncy Castle's
            // X509V3CertificateGenerator and related classes.

            final Hashtable attributes = new Hashtable();
            attributes.put(X509Principal.CN, InetAddress.getLocalHost().getCanonicalHostName());
            final X509Principal dn = new X509Principal(attributes);

            final long millisPerDay = 24 * 60 * 60 * 1000;
            final long now = System.currentTimeMillis();
            final Date start = new Date(now - millisPerDay);
            final Date end = new Date(now + millisPerDay);
            final BigInteger serial = BigInteger.valueOf(1);

            final X509V3CertificateGenerator x509cg = new X509V3CertificateGenerator();
            x509cg.setSubjectDN(dn);
            x509cg.setIssuerDN(dn);
            x509cg.setNotBefore(start);
            x509cg.setNotAfter(end);
            x509cg.setPublicKey(publicKey);
            x509cg.setSignatureAlgorithm("sha1WithRSAEncryption");
            x509cg.setSerialNumber(serial);
            final X509Certificate x509c = x509cg.generateX509Certificate(privateKey);
            final X509Certificate[] x509cc = new X509Certificate[] { x509c };


            // 3.) put certificate and private key to make a key store
            final KeyStore ks = KeyStore.getInstance("BKS");
            ks.load(null, null);
            ks.setKeyEntry(privateAlias, privateKey, keyStorePassword, x509cc);
            ks.setCertificateEntry(publicAlias, x509c);
            return ks;
        }

        /**
         * Create a SSLContext with a KeyManager using the private key and
         * certificate chain from the given KeyStore and a TrustManager
         * using the certificates authorities from the same KeyStore.
         */
        public static final SSLContext createSSLContext(final KeyStore keyStore, final char[] keyStorePassword)
                throws Exception {
            final String kmfa = KeyManagerFactory.getDefaultAlgorithm();
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfa);
            kmf.init(keyStore, keyStorePassword);

            final String tmfa = TrustManagerFactory.getDefaultAlgorithm();
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfa);
            tmf.init(keyStore);

            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            return context;
        }
    }

    public void test_SSLContextTest_Helper_create() {
        Helper helper = Helper.create();
        assertNotNull(helper);
        assertNotNull(helper.keyStore);
        assertNull(helper.keyStorePassword);
        assertNotNull(helper.publicAlias);
        assertNotNull(helper.privateAlias);
        assertNotNull(helper.sslContext);
        assertNotNull(helper.serverSocket);
        assertNotNull(helper.host);
        assertTrue(helper.port != 0);
    }
}

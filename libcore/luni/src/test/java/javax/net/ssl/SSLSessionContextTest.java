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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import junit.framework.TestCase;

public class SSLSessionContextTest extends TestCase {

    public static final void assertSSLSessionContextSize(int expected, SSLSessionContext s) {
        assertEquals(expected, Collections.list(s.getIds()).size());
    }

    public void test_SSLSessionContext_getIds() {
        SSLContextTest.Helper c = SSLContextTest.Helper.create();
        assertSSLSessionContextSize(0, c.sslContext.getClientSessionContext());
        assertSSLSessionContextSize(0, c.sslContext.getServerSessionContext());

        SSLSocketTest.Helper s = SSLSocketTest.Helper.create_workaround();
        assertSSLSessionContextSize(1, s.c.sslContext.getClientSessionContext());
        assertSSLSessionContextSize(1, s.c.sslContext.getServerSessionContext());
        Enumeration clientIds = s.c.sslContext.getClientSessionContext().getIds();
        Enumeration serverIds = s.c.sslContext.getServerSessionContext().getIds();
        byte[] clientId = (byte[]) clientIds.nextElement();
        byte[] serverId = (byte[]) serverIds.nextElement();
        assertEquals(32, clientId.length);
        assertEquals(32, serverId.length);
        assertTrue(Arrays.equals(clientId, serverId));
    }

    @KnownFailure("Should throw NullPointerException on getSession(null)")
    public void test_SSLSessionContext_getSession() {
        SSLContextTest.Helper c = SSLContextTest.Helper.create();
        try {
            c.sslContext.getClientSessionContext().getSession(null);
            fail();
        } catch (NullPointerException e) {
        }
        assertNull(c.sslContext.getClientSessionContext().getSession(new byte[0]));
        assertNull(c.sslContext.getClientSessionContext().getSession(new byte[1]));

        SSLSocketTest.Helper s = SSLSocketTest.Helper.create_workaround();
        SSLSessionContext client = s.c.sslContext.getClientSessionContext();
        SSLSessionContext server = s.c.sslContext.getServerSessionContext();
        byte[] clientId = (byte[]) client.getIds().nextElement();
        byte[] serverId = (byte[]) server.getIds().nextElement();
        assertNotNull(client.getSession(clientId));
        assertNotNull(server.getSession(serverId));
        assertTrue(Arrays.equals(clientId, client.getSession(clientId).getId()));
        assertTrue(Arrays.equals(serverId, server.getSession(serverId).getId()));
    }

    @KnownFailure("Should return 0 for unlimited, not 10 entries")
    public void test_SSLSessionContext_getSessionCacheSize() {
        SSLContextTest.Helper c = SSLContextTest.Helper.create();
        assertEquals(0, c.sslContext.getClientSessionContext().getSessionCacheSize());
        assertEquals(0, c.sslContext.getServerSessionContext().getSessionCacheSize());

        SSLSocketTest.Helper s = SSLSocketTest.Helper.create_workaround();
        assertEquals(0, s.c.sslContext.getClientSessionContext().getSessionCacheSize());
        assertEquals(0, s.c.sslContext.getServerSessionContext().getSessionCacheSize());
    }

    @KnownFailure("Should return 0 for unlimited, not 10 entries")
    public void test_SSLSessionContext_setSessionCacheSize_basic() {
        SSLContextTest.Helper c = SSLContextTest.Helper.create();
        assertBasicSetSessionCacheSizeBehavior(c.sslContext.getClientSessionContext());
        assertBasicSetSessionCacheSizeBehavior(c.sslContext.getServerSessionContext());
    }

    private static void assertBasicSetSessionCacheSizeBehavior(SSLSessionContext s) {
        try {
            s.setSessionCacheSize(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        assertEquals(0, s.getSessionCacheSize());
        s.setSessionCacheSize(1);
        assertEquals(1, s.getSessionCacheSize());
    }

    @KnownFailure("Should return 0 for unlimited, not 10 entries")
    public void test_SSLSessionContext_setSessionCacheSize_dynamic() {

        SSLSocketTest.Helper s = SSLSocketTest.Helper.create_workaround();
        SSLSessionContext client = s.c.sslContext.getClientSessionContext();
        SSLSessionContext server = s.c.sslContext.getServerSessionContext();
        assertEquals(0, client.getSessionCacheSize());
        assertEquals(0, server.getSessionCacheSize());
        assertSSLSessionContextSize(1, client);
        assertSSLSessionContextSize(1, server);

        LinkedList<String> uniqueCipherSuites
            = new LinkedList(Arrays.asList(s.server.getEnabledCipherSuites()));
        uniqueCipherSuites.remove(s.client.getSession().getCipherSuite());

        // only use RSA cipher suites which will work with our TrustProvider
        Iterator<String> i = uniqueCipherSuites.iterator();
        while (i.hasNext()) {
            String cipherSuite = i.next();
            if (cipherSuite.startsWith("SSL_RSA_")) {
                continue;
            }
            if (cipherSuite.startsWith("TLS_RSA_")) {
                continue;
            }
            if (cipherSuite.startsWith("TLS_DHE_RSA_")) {
                continue;
            }
            if (cipherSuite.startsWith("SSL_DHE_RSA_")) {
                continue;
            }
            i.remove();
        }

        /*
         * having more than 5 uniqueCipherSuites is a test
         * requirement, not a requirement of the interface or
         * implementation. It simply allows us to make sure that we
         * will not get a cached session ID since we'll have to
         * renegotiate a new session due to the new cipher suite
         * requirement. even this test only really needs three if it
         * reused the unique cipher suites every time it resets the
         * session cache.
         */
        assertTrue(uniqueCipherSuites.size() > 5);

        SSLSocketTest.Helper.connect_workaround(s.c,
                                              new String[] { uniqueCipherSuites.remove() }); // 1
        assertSSLSessionContextSize(2, client);
        assertSSLSessionContextSize(2, server);
        SSLSocketTest.Helper.connect_workaround(s.c,
                                              new String[] { uniqueCipherSuites.remove() }); // 2
        assertSSLSessionContextSize(3, client);
        assertSSLSessionContextSize(3, server);

        client.setSessionCacheSize(1);
        server.setSessionCacheSize(1);
        assertEquals(1, client.getSessionCacheSize());
        assertEquals(1, server.getSessionCacheSize());
        assertSSLSessionContextSize(1, client);
        assertSSLSessionContextSize(1, server);
        SSLSocketTest.Helper.connect_workaround(s.c,
                                              new String[] { uniqueCipherSuites.remove() }); // 3
        assertSSLSessionContextSize(1, client);
        assertSSLSessionContextSize(1, server);

        client.setSessionCacheSize(2);
        server.setSessionCacheSize(2);
        SSLSocketTest.Helper.connect_workaround(s.c,
                                              new String[] { uniqueCipherSuites.remove() }); // 4
        assertSSLSessionContextSize(2, client);
        assertSSLSessionContextSize(2, server);
        SSLSocketTest.Helper.connect_workaround(s.c,
                                              new String[] { uniqueCipherSuites.remove() }); // 5
        assertSSLSessionContextSize(2, client);
        assertSSLSessionContextSize(2, server);
    }

    @KnownFailure("Should return 86400 seconds (1 day), not 0 for unlimited")
    public void test_SSLSessionContext_getSessionTimeout() {
        SSLContextTest.Helper c = SSLContextTest.Helper.create();
        assertEquals(86400, c.sslContext.getClientSessionContext().getSessionTimeout());
        assertEquals(86400, c.sslContext.getServerSessionContext().getSessionTimeout());

        SSLSocketTest.Helper s = SSLSocketTest.Helper.create_workaround();
        assertEquals(86400, s.c.sslContext.getClientSessionContext().getSessionTimeout());
        assertEquals(86400, s.c.sslContext.getServerSessionContext().getSessionTimeout());
    }

    @KnownFailure("Should return 86400 seconds (1 day), not 0 for unlimited")
    public void test_SSLSessionContext_setSessionTimeout() throws Exception {
        SSLContextTest.Helper c = SSLContextTest.Helper.create();
        assertEquals(86400, c.sslContext.getClientSessionContext().getSessionTimeout());
        assertEquals(86400, c.sslContext.getServerSessionContext().getSessionTimeout());
        c.sslContext.getClientSessionContext().setSessionTimeout(0);
        c.sslContext.getServerSessionContext().setSessionTimeout(0);
        assertEquals(0, c.sslContext.getClientSessionContext().getSessionTimeout());
        assertEquals(0, c.sslContext.getServerSessionContext().getSessionTimeout());

        try {
            c.sslContext.getClientSessionContext().setSessionTimeout(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            c.sslContext.getServerSessionContext().setSessionTimeout(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        SSLSocketTest.Helper s = SSLSocketTest.Helper.create_workaround();
        assertSSLSessionContextSize(1, s.c.sslContext.getClientSessionContext());
        assertSSLSessionContextSize(1, s.c.sslContext.getServerSessionContext());
        Thread.sleep(1 * 1000);
        s.c.sslContext.getClientSessionContext().setSessionTimeout(1);
        s.c.sslContext.getServerSessionContext().setSessionTimeout(1);
        assertSSLSessionContextSize(0, s.c.sslContext.getClientSessionContext());
        assertSSLSessionContextSize(0, s.c.sslContext.getServerSessionContext());
    }
}

/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.net;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.NetPermission;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import tests.support.Support_PortManager;
import tests.support.Support_TestWebData;
import tests.support.Support_TestWebServer;

@TestTargetClass(value = ResponseCache.class)
public class ResponseCacheTest extends TestCase {

    
    
    /**
     * @tests java.net.ResponseCache#getDefault()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_GetDefault() throws Exception {
        assertNull(ResponseCache.getDefault());
    }

    /**
     * @tests java.net.ResponseCache#setDefault(ResponseCache)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "This is a complete subset of tests for setDefault method.",
            method = "setDefault",
            args = {java.net.ResponseCache.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "This is a complete subset of tests for setDefault method.",
            method = "ResponseCache",
            args = {}
        )
    })
    public void test_SetDefaultLjava_net_ResponseCache_Normal()
            throws Exception {
        ResponseCache rc1 = new MockResponseCache();
        ResponseCache rc2 = new MockResponseCache();
        ResponseCache.setDefault(rc1);
        assertSame(ResponseCache.getDefault(), rc1);
        ResponseCache.setDefault(rc2);
        assertSame(ResponseCache.getDefault(), rc2);
        ResponseCache.setDefault(null);
        assertNull(ResponseCache.getDefault());
    }

    /**
     * @tests java.net.ResponseCache#getDefault()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_GetDefault_Security() {
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSM());
        } catch (SecurityException e) {
            System.err.println("No setSecurityManager permission.");
            System.err.println("test_setDefaultLjava_net_ResponseCache_NoPermission is not tested");
            return;
        }
        try {
            ResponseCache.getDefault();
            fail("should throw SecurityException");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(old);
        }
    }

    /**
     * @tests java.net.ResponseCache#setDefault(ResponseCache)
     */
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "This is a complete subset of tests for setDefault method.",
        method = "setDefault",
        args = {java.net.ResponseCache.class}
    )
    public void test_setDefaultLjava_net_ResponseCache_NoPermission() {
        ResponseCache rc = new MockResponseCache();
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSM());
        } catch (SecurityException e) {
            System.err.println("No setSecurityManager permission.");
            System.err.println("test_setDefaultLjava_net_ResponseCache_NoPermission is not tested");
            return;
        }
        try {
            ResponseCache.setDefault(rc);
            fail("should throw SecurityException");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(old);
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {URI.class, String.class, Map.class}
    )
    public void test_get() throws Exception {
        String uri = "http://localhost/";
        URL url  = new URL(uri);
        TestResponseCache cache = new TestResponseCache(uri, true);
        ResponseCache.setDefault(cache);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setUseCaches(true);
        httpCon.connect();
        try {
            Thread.sleep(5000);
        } catch(Exception e) {}
        
        InputStream is = httpCon.getInputStream();
        byte[] array = new byte [10];
        is.read(array);
        assertEquals(url.toURI(), cache.getWasCalled);
        assertEquals("Cache test", new String(array));
        is.close();
        httpCon.disconnect();

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "put",
        args = {URI.class, URLConnection.class}
    )
    public void test_put() throws Exception {
        // Create test ResponseCache
        TestResponseCache cache = new TestResponseCache(
                "http://localhost/not_cached", false);
        ResponseCache.setDefault(cache);

        // Start Server
        int port = Support_PortManager.getNextPort();
        Support_TestWebServer s = new Support_TestWebServer();
        try {
            s.initServer(port, 10000, false);
            Thread.currentThread().sleep(2500);
    
            // Create connection to server
            URL url  = new URL("http://localhost:" + port + "/test1");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setUseCaches(true);
            httpCon.connect();
            Thread.currentThread().sleep(2500);
    
            // Check that a call to the cache was made.
            assertEquals(url.toURI(), cache.getWasCalled);
            // Make the HttpConnection get the content. It should try to
            // put it into the cache.
            httpCon.getContent();
            // Check if put was called
            assertEquals(url.toURI(), cache.putWasCalled);
    
            // get the 
            InputStream is = httpCon.getInputStream();
    
            byte[] array = new byte[Support_TestWebData.test1.length];
            is.read(array);
            assertTrue(Arrays.equals(Support_TestWebData.tests[0], array));
            is.close();
            httpCon.disconnect();
        } finally {
            s.close();
        }
    }
    
    /*
     * MockResponseCache for testSetDefault(ResponseCache)
     */
    class MockResponseCache extends ResponseCache {

        public CacheResponse get(URI arg0, String arg1, Map arg2)
                throws IOException {
            return null;
        }

        public CacheRequest put(URI arg0, URLConnection arg1)
                throws IOException {
            return null;
        }
    }

    /*
     * MockSecurityMaanger. It denies NetPermission("getResponseCache") and
     * NetPermission("setResponseCache").
     */
    class MockSM extends SecurityManager {
        public void checkPermission(Permission permission) {
            if (permission instanceof NetPermission) {
                if ("setResponseCache".equals(permission.getName())) {
                    throw new SecurityException();
                }
            }

            if (permission instanceof NetPermission) {
                if ("getResponseCache".equals(permission.getName())) {
                    
                    throw new SecurityException();
                }
            }

            if (permission instanceof RuntimePermission) {
                if ("setSecurityManager".equals(permission.getName())) {
                    return;
                }
            }
        }
    }
    
    class TestCacheResponse extends CacheResponse {
        InputStream is = null;
        Map<String, List<String>> headers = null;
        
        public TestCacheResponse(String filename) {
            String path = getClass().getPackage().getName().replace(".", "/");
            is = getClass().getResourceAsStream("/" + path + "/" + filename);
        }

        @Override
        public InputStream getBody() {
           return is;
        }

        @Override
         public Map getHeaders() {
           return null;
         }
    }

    class TestCacheRequest extends CacheRequest {

        @Override
        public OutputStream getBody() {
            return null;
        }

        @Override
        public void abort() {
        }
    }
    
    class TestResponseCache extends ResponseCache {

        URI uri1 = null;
        boolean testGet = false;

        public URI getWasCalled = null;
        public URI putWasCalled = null;

        TestResponseCache(String uri, boolean testGet) {
            try {
                uri1  = new URI(uri);            
            } catch (URISyntaxException e) {
            }
            this.testGet = testGet;
        }

        @Override
        public CacheResponse get(URI uri, String rqstMethod, Map rqstHeaders) {
            getWasCalled = uri;
            if (testGet && uri.equals(uri1)) {
                return new TestCacheResponse("file1.cache");
            }
            return null;
        }

        @Override
        public CacheRequest put(URI uri, URLConnection conn) {
            putWasCalled = uri;
            if (!testGet && uri.equals(uri1)) {
                return new TestCacheRequest();
            }
            return null;
        }
    }
}

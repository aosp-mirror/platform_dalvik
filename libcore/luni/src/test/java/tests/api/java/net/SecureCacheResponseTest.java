/*
 * Copyright (C) 2008 The Android Open Source Project
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

package tests.api.java.net;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import tests.api.java.net.ResponseCacheTest.TestCacheRequest;
import tests.api.java.net.ResponseCacheTest.TestCacheResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.SecureCacheResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;

@TestTargetClass(SecureCacheResponse.class) 
public class SecureCacheResponseTest extends TestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SecureCacheResponse",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCipherSuite",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLocalCertificateChain",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLocalPrincipal",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPeerPrincipal",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getServerCertificateChain",
            args = {}
        )
    })
    public void test_Constructor() throws Exception {
        TestSecureCacheResponse scr = new TestSecureCacheResponse();
        assertNull(scr.getCipherSuite());
        assertNull(scr.getLocalCertificateChain());
        assertNull(scr.getLocalPrincipal());
        assertNull(scr.getPeerPrincipal());
        assertNull(scr.getServerCertificateChain());
        assertNull(scr.getBody());
        assertNull(scr.getHeaders());
    }
    
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "SecureCacheResponse",
        args = {}
    )
    public void test_additional() throws Exception {
            
            URL url  = new URL("http://google.com");
            ResponseCache.setDefault(new TestResponseCache());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setUseCaches(true);
            httpCon.connect();
            try {
                Thread.sleep(5000);
            } catch(Exception e) {}
            httpCon.disconnect();
      
    }
    
    class TestSecureCacheResponse extends SecureCacheResponse {

        @Override
        public String getCipherSuite() {
            return null;
        }

        @Override
        public List<Certificate> getLocalCertificateChain() {
            return null;
        }

        @Override
        public Principal getLocalPrincipal() {
            return null;
        }

        @Override
        public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
            return null;
        }

        @Override
        public List<Certificate> getServerCertificateChain() throws SSLPeerUnverifiedException {
            return null;
        }

        @Override
        public InputStream getBody() throws IOException {
            return null;
        }

        @Override
        public Map<String, List<String>> getHeaders() throws IOException {
            return null;
        }
        
    }
    
    class TestResponseCache extends ResponseCache {
        
        URI uri1 = null;    
    
        public TestSecureCacheResponse get(URI uri, String rqstMethod, Map rqstHeaders)
                                                          throws IOException {
            

          try {
            uri1  = new URI("http://google.com");
          } catch (URISyntaxException e) {
          }  
          if (uri.equals(uri)) {
            return new TestSecureCacheResponse();
          }
          return null;
        }

       public CacheRequest put(URI uri, URLConnection conn)
          throws IOException {
          return null;
        }
    }
}

package tests.api.java.net;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;

@TestTargetClass(URLStreamHandler.class) 
public class URLStreamHandlerTest extends TestCase {
    
    MockURLStreamHandler handler = null;

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {URL.class, URL.class}
    )
    public void test_equalsLjava_net_URLLjava_net_URL() {
        try {
            URL url1 = new URL("ftp://test_url/test?a=b&c=%D0+%D1");
            URL url2 = new URL("http://test_url/test?a=b&c=%D0+%D1");
            assertFalse(url1.equals(url2));
            
            URL url3 = new URL("http://test_url+/test?a=b&c=%D0+%D1");
            assertFalse(handler.equals(url1,url2));
            
            try {
                assertFalse(handler.equals(null, url1));
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
                //expected
            }
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultPort",
        args = {}
    )    
    public void test_getDefaultPort() {
        assertEquals(-1, handler.getDefaultPort());
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostAddress",
        args = {URL.class}
    )
    public void test_getHostAddress() throws MalformedURLException,
                                        UnknownHostException {
        URL url1 = new URL("ftp://test_url/test?a=b&c=%D0+%D1");
        assertNull(handler.getHostAddress(url1));
        
        URL url2 = new URL("http://test:pwd@host/test?a=b&c=%D0+%D1");
        assertNull("testHost", handler.getHostAddress(url2));handler.getHostAddress(url2);
        
        URL url3 = new URL("http://localhost/test");
        assertEquals(InetAddress.getLocalHost(), handler.getHostAddress(url3));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {URL.class}
    )
    public void test_hashCodeLjava_net_URL() {
        try {
            URL url1 = new URL("ftp://test_url/test?a=b&c=%D0+%D1");
            URL url2 = new URL("http://test_url/test?a=b&c=%D0+%D1");
            assertTrue(handler.hashCode(url1) != handler.hashCode(url2));
            
            URL url3 = new URL("http://test_url+/test?a=b&c=%D0+%D1");
            assertFalse(handler.equals(url1,url2));
            
            try {
                handler.hashCode(null);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
                //expected
            }
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }        
    }
     
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hostsEqual",
        args = {URL.class, URL.class}
    )
    public void test_hostsEqualLjava_net_URLLjava_net_URL() throws 
                                                    MalformedURLException {
        URL url1 = new URL("ftp://localhost:21/*test");
        URL url2 = new URL("http://127.0.0.1/_test");
        assertTrue(handler.hostsEqual(url1, url2));
        
        URL url3 = new URL("http://foo/_test_goo");
        assertFalse(handler.hostsEqual(url1, url3));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "openConnection",
        args = { URL.class }
    )
    public void test_openConnectionLjava_net_URL() throws IOException {
        // abstract method, it doesn't check anything
        assertNull(handler.openConnection(null));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "openConnection",
        args = {URL.class, Proxy.class}
    )
    public void test_openConnectionLjava_net_URLLjava_net_Proxy() {
        try {
            handler.openConnection(null, null);
            fail("UnsupportedOperationException was not thrown.");
        } catch(UnsupportedOperationException  uoe) {
            //expected
        } catch (IOException e) {
            fail("IOException was thrown.");
        }
    }
    
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Completed testing of this method requres set up " +
                "URLStreamHandlerFactory that can be done at most once.",
        method = "parseURL",
        args = {URL.class, String.class, int.class, int.class}
    )
    public void test_parseURLLjava_net_URLLjava_lang_StringII() 
                                                throws MalformedURLException {
        String str  = "http://test.org/foo?a=123&b=%D5D6D7&c=++&d=";
        URL url = new URL("http://test.org");
        
        try {
            handler.parseURL(url, str, 0, str.length());
            fail("SecurityException should be thrown.");
        } catch(SecurityException se) {
            //SecurityException is expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sameFile",
        args = {URL.class, URL.class}
    )    
    public void test_sameFile() throws MalformedURLException {
        URL url1  = new URL("http://test:pwd@localhost:80/foo/foo1.c");
        URL url2  = new URL("http://test:pwd@127.0.01:80/foo/foo1.c");
        URL url3  = new URL("http://test:pwd@127.0.01:80/foo/foo2.c"); 
        URL url4  = new URL("ftp://test:pwd@127.0.01:21/foo/foo2.c");
        URL url5  = new URL("ftp://test:pwd@127.0.01:21/foo/foo1/foo2.c");
        URL url6  = new URL("http://test/foo/foo1.c");
        
        assertTrue("Test case 1", handler.sameFile(url1, url2));
        assertFalse("Test case 2", handler.sameFile(url3, url2));
        assertFalse("Test case 3", handler.sameFile(url3, url4));
        assertFalse("Test case 4", handler.sameFile(url4, url5));
        assertFalse("Test case 5", handler.sameFile(url1, url6));
    }
    
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Completed testing of this method requres set up " +
                "URLStreamHandlerFactory that can be done at most once.",
        method = "setURL",
        args = {java.net.URL.class, java.lang.String.class, 
                java.lang.String.class, int.class, java.lang.String.class, 
                java.lang.String.class}
    )       
    public void test_setURL1() throws MalformedURLException {
        URL url = new URL("http://test.org");
        
        try {
            handler.setURL(url, "http", "localhost", 80, "foo.c", "ref");
            fail("SecurityException should be thrown.");
        } catch(SecurityException se) {
            //SecurityException is expected
        }        
    }
    
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Completed testing of this method requres set up " +
                 "URLStreamHandlerFactory that can be done at most once.",
        method = "setURL",
        args = {java.net.URL.class, java.lang.String.class, 
                java.lang.String.class, int.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class}
    )       
    public void test_setURL2() throws MalformedURLException {
        URL url = new URL("http://test.org");
         
        try {
            handler.setURL(url, "http", "localhost", 80, "authority", 
                    "user", "foo.c", "query", "ref");
            fail("SecurityException should be thrown.");
        } catch(SecurityException se) {
            //SecurityException is expected
        }        
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toExternalForm",
        args = {URL.class}
    )
    public void test_toExternalForm() throws MalformedURLException {
        URL [] urls = { new URL("ftp://test_url/test?a=b&c=%D0+%D1"),
                        new URL("http://test_url/test?a=b&c=%D0+%D1"),
                        new URL("http://test:pwd@localhost:80/foo/foo1.c")};
        
        for(URL url:urls) {
            assertEquals("Test case for " + url.toString(),
                    url.toString(), handler.toExternalForm(url));
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "URLStreamHandler",
        args = {}
    )
    public void test_Constructor() {
        MockURLStreamHandler msh = new MockURLStreamHandler();
        assertEquals(-1, msh.getDefaultPort());
    }
    
    public void setUp() {
        handler = new MockURLStreamHandler();
    }
    
    class MockURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL arg0) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }
        
        public boolean equals(URL u1, URL u2) {
            return super.equals(u1, u2);
        }
        
        public int getDefaultPort() {
            return super.getDefaultPort();
        }
        
        public InetAddress getHostAddress(URL u) {
            return super.getHostAddress(u);
        }
        
        public int hashCode(URL u) {
            return super.hashCode(u);
        }
        
        public boolean hostsEqual(URL u1, URL u2) {
            return super.hostsEqual(u1, u2);
        }
        
        public URLConnection openConnection(URL u, Proxy p) throws IOException {
            return super.openConnection(u, p);
        }

        public void parseURL(URL u, String spec, int start, int limit) {
            super.parseURL(u, spec, start, limit);
        }
        
        public boolean sameFile(URL u1, URL u2) {
            return super.sameFile(u1, u2);
        }
        
        public void setURL(URL u,
                String protocol,
                String host,
                int port,
                String file,
                String ref) {
            super.setURL(u, protocol, host, port, file, ref);
        }
        
        public void setURL(URL u,
                String protocol,
                String host,
                int port,
                String authority,
                String userInfo,
                String path,
                String query,
                String ref) {
            super.setURL(u, protocol, host, port, authority, 
                    userInfo, path, query, ref);
        }
        
        public String toExternalForm(URL u) {
            return super.toExternalForm(u);
        }
    }
}

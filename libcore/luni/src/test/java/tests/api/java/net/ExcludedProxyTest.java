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
import java.io.OutputStream;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

import tests.support.Support_Configuration;
import tests.support.resource.Support_Resources;
import junit.framework.TestCase;

/*
 * This test is designed for collecting all the testcases which needs a proxy
 * server. They will be moved to corresponding test class when ProxyHandler of
 * Jetty is ready in the future.
 * 
 */

public class ExcludedProxyTest extends TestCase {
    /**
     * @tests java.net.HttpURLConnection#usingProxy()
     */
    public void test_usingProxy() throws Exception {
        try {
            System.setProperty("http.proxyHost",
                    Support_Configuration.ProxyServerTestHost);

            URL u1 = new URL("http://" + Support_Configuration.HomeAddress);
            URLConnection conn1 = u1.openConnection();
            conn1.getInputStream();

            boolean exception = false;
            try {
                System.setProperty("http.proxyPort", "81");
                URL u3 = new URL("http://"
                        + Support_Configuration.InetTestAddress);
                URLConnection conn3 = u3.openConnection();
                conn3.getInputStream();
                fail("Should throw IOException");
            } catch (IOException e) {
                // expected
            }

            System.setProperty("http.proxyPort", "80");

            URL u2 = new URL("http://"
                    + Support_Configuration.ProxyServerTestHost
                    + "/cgi-bin/test.pl");
            java.net.HttpURLConnection conn2 = (java.net.HttpURLConnection) u2
                    .openConnection();
            conn2.setDoOutput(true);
            conn2.setRequestMethod("POST");
            OutputStream out2 = conn2.getOutputStream();
            String posted2 = "this is a test";
            out2.write(posted2.getBytes());
            out2.close();
            conn2.getResponseCode();
            InputStream is2 = conn2.getInputStream();
            String response2 = "";
            byte[] b2 = new byte[1024];
            int count2 = 0;
            while ((count2 = is2.read(b2)) > 0)
                response2 += new String(b2, 0, count2);
            assertTrue("Response to POST method invalid", response2
                    .equals(posted2));

            String posted4 = "just a test";
            URL u4 = new URL("http://"
                    + Support_Configuration.ProxyServerTestHost
                    + "/cgi-bin/test.pl");
            java.net.HttpURLConnection conn4 = (java.net.HttpURLConnection) u4
                    .openConnection();
            conn4.setDoOutput(true);
            conn4.setRequestMethod("POST");
            conn4.setRequestProperty("Content-length", String.valueOf(posted4
                    .length()));
            OutputStream out = conn4.getOutputStream();
            out.write(posted4.getBytes());
            out.close();
            conn4.getResponseCode();
            InputStream is = conn4.getInputStream();
            String response = "";
            byte[] b4 = new byte[1024];
            int count = 0;
            while ((count = is.read(b4)) > 0)
                response += new String(b4, 0, count);
            assertTrue("Response to POST method invalid", response
                    .equals(posted4));
        } finally {
            System.setProperties(null);
        }
    }
    
    /**
     * @tests java.net.SocketImpl#SocketImpl()
     */
    public void test_Constructor() {
        try {
            try {
                System.setProperty("socksProxyHost",
                        Support_Configuration.SocksServerTestHost);
                System.setProperty("socksProxyPort", String
                        .valueOf(Support_Configuration.SocksServerTestPort));
                Socket s = new Socket(Support_Configuration.HomeAddress, 80);
                OutputStream os = s.getOutputStream();
                os.write("GET / HTTP/1.0\r\n\r\n".getBytes());
                s.getInputStream();
            } catch (IOException e) {
                fail("Could not open socket: "
                        + Support_Configuration.HomeAddress + " " + e);
            }
            boolean exception = false;
            try {
                System.setProperty("socksProxyHost",
                        Support_Configuration.SocksServerTestHost);
                System
                        .setProperty(
                                "socksProxyPort",
                                String
                                        .valueOf(Support_Configuration.SocksServerTestPort + 1));
                Socket s = new Socket(Support_Configuration.HomeAddress, 80);
                OutputStream os = s.getOutputStream();
                os.write("GET / HTTP/1.0\r\n\r\n".getBytes());
                s.getInputStream();
            } catch (IOException e) {
                exception = true;
            }
            assertTrue("Exception should have been thrown", exception);
        } finally {
            System.setProperties(null);
        }
    }
    
    /**
     * @tests java.net.URL#openConnection(Proxy)
     */
    public void test_openConnectionLjava_net_Proxy() throws IOException {
        SocketAddress addr1 = new InetSocketAddress(
                Support_Configuration.ProxyServerTestHost, 808);
        SocketAddress addr2 = new InetSocketAddress(
                Support_Configuration.ProxyServerTestHost, 1080);
        Proxy proxy1 = new Proxy(Proxy.Type.HTTP, addr1);
        Proxy proxy2 = new Proxy(Proxy.Type.SOCKS, addr2);
        Proxy proxyList[] = { proxy1, proxy2 };
        for (int i = 0; i < proxyList.length; ++i) {
            String posted = "just a test";
            URL u = new URL("http://"
                    + Support_Configuration.ProxyServerTestHost
                    + "/cgi-bin/test.pl");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u
                    .openConnection(proxyList[i]);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-length", String.valueOf(posted
                    .length()));
            OutputStream out = conn.getOutputStream();
            out.write(posted.getBytes());
            out.close();
            conn.getResponseCode();
            InputStream is = conn.getInputStream();
            String response = "";
            byte[] b = new byte[1024];
            int count = 0;
            while ((count = is.read(b)) > 0) {
                response += new String(b, 0, count);
            }
            assertTrue("Response to POST method invalid", response
                    .equals(posted));
        }
        
        URL httpUrl = new URL("http://abc.com");
        URL jarUrl = new URL("jar:"
                + Support_Resources.getResourceURL("/JUC/lf.jar!/plus.bmp"));
        URL ftpUrl = new URL("ftp://" + Support_Configuration.FTPTestAddress
                + "/nettest.txt");
        URL fileUrl = new URL("file://abc");
        URL[] urlList = { httpUrl, jarUrl, ftpUrl, fileUrl };
        for (int i = 0; i < urlList.length; ++i) {
            try {
                urlList[i].openConnection(null);
            } catch (IllegalArgumentException iae) {
                // expected
            }
        }
        // should not throw exception
        fileUrl.openConnection(Proxy.NO_PROXY);
    }
}

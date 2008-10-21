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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetPermission;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Permission;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

public class ProxySelectorTest extends TestCase {

	private static final String HTTP_PROXY_HOST = "127.0.0.1";

	private static final int HTTP_PROXY_PORT = 80;

	private static final String HTTPS_PROXY_HOST = "127.0.0.2";

	private static final int HTTPS_PROXY_PORT = 443;

	private static final String FTP_PROXY_HOST = "127.0.0.3";

	private static final int FTP_PROXY_PORT = 80;

	private static final String SOCKS_PROXY_HOST = "127.0.0.4";

	private static final int SOCKS_PROXY_PORT = 1080;

	private static URI httpUri;

	private static URI ftpUri;

	private static URI httpsUri;

	private static URI tcpUri;
	
	private List proxyList;
	
	private ProxySelector selector = ProxySelector.getDefault();
	
	static {
		try {
			httpUri = new URI("http://test.com");
			ftpUri = new URI("ftp://test.com");
			httpsUri = new URI("https://test.com");
			tcpUri = new URI("socket://host.com");
		} catch (URISyntaxException e) {

		}
	}

	/*
	 * Original system properties must be restored after running each test case.
	 */
	private Properties orignalSystemProperties;

	/**
	 * @tests java.net.ProxySelector#getDefault()
	 */
	public void test_getDefault() {
		ProxySelector selector1 = ProxySelector.getDefault();
		assertNotNull(selector1);

		ProxySelector selector2 = ProxySelector.getDefault();
		assertSame(selector1, selector2);
	}

	/**
	 * @tests java.net.ProxySelector#getDefault()
	 */
	public void test_getDefault_Security() {
		SecurityManager orignalSecurityManager = System.getSecurityManager();
		try {
			System.setSecurityManager(new MockSecurityManager());
		} catch (SecurityException e) {
			System.err.println("No setSecurityManager permission.");
			System.err.println("test_getDefault_Security is not tested");
			return;
		}
		try {
			ProxySelector.getDefault();
			fail("should throw SecurityException");
		} catch (SecurityException e) {
			// expected
		} finally {
			System.setSecurityManager(orignalSecurityManager);
		}
	}

	/**
	 * @tests java.net.ProxySelector#setDefault(ProxySelector)}
	 */
	public void test_setDefaultLjava_net_ProxySelector() {
		ProxySelector originalSelector = ProxySelector.getDefault();
		try {
			ProxySelector newSelector = new MockProxySelector();
			ProxySelector.setDefault(newSelector);
			assertSame(newSelector, ProxySelector.getDefault());
			// use null to unset
			ProxySelector.setDefault(null);
			assertSame(null, ProxySelector.getDefault());
		} finally {
			ProxySelector.setDefault(originalSelector);
		}
	}

	/**
	 * @tests java.net.ProxySelector#setDefault(ProxySelector)}
	 */
	public void test_setDefaultLjava_net_ProxySelector_Security() {
		ProxySelector originalSelector = ProxySelector.getDefault();
		SecurityManager orignalSecurityManager = System.getSecurityManager();
		try {
			System.setSecurityManager(new MockSecurityManager());
		} catch (SecurityException e) {
			System.err.println("No setSecurityManager permission.");
			System.err
					.println("test_setDefaultLjava_net_ProxySelector_Security is not tested");
			return;
		}
		try {
			ProxySelector.setDefault(new MockProxySelector());
			fail("should throw SecurityException");
		} catch (SecurityException e) {
			// expected
		} finally {
			System.setSecurityManager(orignalSecurityManager);
			ProxySelector.setDefault(originalSelector);
		}
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectExact()
			throws URISyntaxException {
		// no proxy, return a proxyList only contains NO_PROXY
		proxyList = selector.select(httpUri);
		assertProxyEquals(proxyList,Proxy.NO_PROXY);

		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
		System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));

		proxyList = selector.select(httpUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTP_PROXY_HOST,HTTP_PROXY_PORT);

		proxyList = selector.select(httpsUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTPS_PROXY_HOST,HTTPS_PROXY_PORT);
		
		proxyList = selector.select(ftpUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,FTP_PROXY_HOST,FTP_PROXY_PORT);
		
		proxyList = selector.select(tcpUri);
		assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);

	}
    
    /**
     * @tests java.net.ProxySelector#select(URI)
     */
    public void test_selectLjava_net_URI_SelectExact_NullHost()
            throws URISyntaxException {
        // regression test for Harmony-1063
        httpUri = new URI("http://a@");
        ftpUri = new URI("ftp://a@");
        httpsUri = new URI("https://a@");
        tcpUri = new URI("socket://a@");
        // no proxy, return a proxyList only contains NO_PROXY
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList, Proxy.NO_PROXY);

        // set http proxy
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        // set https proxy
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        // set ftp proxy
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        // set socks proxy
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));

        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList, Proxy.Type.HTTP, HTTP_PROXY_HOST,
                HTTP_PROXY_PORT);

        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList, Proxy.Type.HTTP, HTTPS_PROXY_HOST,
                HTTPS_PROXY_PORT);

        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList, Proxy.Type.HTTP, FTP_PROXY_HOST,
                FTP_PROXY_PORT);

        proxyList = selector.select(tcpUri);
        assertProxyEquals(proxyList, Proxy.Type.SOCKS, SOCKS_PROXY_HOST,
                SOCKS_PROXY_PORT);

    }

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectExact_DefaultPort()
			throws URISyntaxException {
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);

		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);

		proxyList = selector.select(httpUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTP_PROXY_HOST,HTTP_PROXY_PORT);

		proxyList = selector.select(httpsUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTPS_PROXY_HOST,HTTPS_PROXY_PORT);
		
		proxyList = selector.select(ftpUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,FTP_PROXY_HOST,FTP_PROXY_PORT);
		
		proxyList = selector.select(tcpUri);
		assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);

	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectExact_InvalidPort()
			throws URISyntaxException {
		final String INVALID_PORT = "abc";
		
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", INVALID_PORT);
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", INVALID_PORT);
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", INVALID_PORT);
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
		System.setProperty("socksproxyPort", INVALID_PORT);

		proxyList = selector.select(httpUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTP_PROXY_HOST,HTTP_PROXY_PORT);

		proxyList = selector.select(httpsUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTPS_PROXY_HOST,HTTPS_PROXY_PORT);

		proxyList = selector.select(ftpUri);
		assertProxyEquals(proxyList,Proxy.Type.HTTP,FTP_PROXY_HOST,FTP_PROXY_PORT);

		proxyList = selector.select(tcpUri);
		assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	// RI may fail this test case. 
	// Uncomment this test case when regex.jar is ready.
	/*
	public void test_selectLjava_net_URI_Select_NonProxyHosts()
			throws URISyntaxException {
		// RI's bug. Some RIs may fail this test case. 
		URI[] httpUris = { new URI("http://test.com"),
				new URI("http://10.10.1.2"), new URI("http://a"),
				new URI("http://def.abc.com") };
		URI[] ftpUris = { new URI("ftp://test.com"),
				new URI("ftp://10.10.1.2"), new URI("ftp://a"),
				new URI("ftp://def.abc.com") };
		
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.nonProxyHosts", "a|b|tes*|10.10.*|*.abc.com");
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.nonProxyHosts", "a|b|tes*|10.10.*|*.abc.com");

		for (int i = 0; i < httpUris.length; i++) {
			proxyList = selector.select(httpUris[i]);
			assertProxyEquals(proxyList,Proxy.NO_PROXY);
		}

		for (int i = 0; i < ftpUris.length; i++) {
			proxyList = selector.select(ftpUris[i]);
			assertProxyEquals(proxyList,Proxy.NO_PROXY);
		}
	}*/

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectLikeHTTP()
			throws URISyntaxException {
		System.setProperty("http.proxyHost", "");
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
		System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));

		proxyList = selector.select(httpUri);
		assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectNoHTTP()
			throws URISyntaxException {
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));

		proxyList = selector.select(httpUri);
		assertProxyEquals(proxyList,Proxy.NO_PROXY);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectLikeHTTPS()
			throws URISyntaxException {
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set https proxy host empty
		System.setProperty("http.proxyHost", "");
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
		System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));

		proxyList = selector.select(httpsUri);
		assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectNoHTTPS()
			throws URISyntaxException {
		// set https proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));

		proxyList = selector.select(httpsUri);
		assertProxyEquals(proxyList,Proxy.NO_PROXY);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectLikeFTP()
			throws URISyntaxException {
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set ftp host empty
		System.setProperty("ftp.proxyHost", "");
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
		System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));

		proxyList = selector.select(ftpUri);
		assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectNoFTP()
			throws URISyntaxException {
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));

		proxyList = selector.select(ftpUri);
		assertProxyEquals(proxyList,Proxy.NO_PROXY);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_SelectNoSOCKS()
			throws URISyntaxException {
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
		// set socks proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));

		proxyList = selector.select(tcpUri);
		assertProxyEquals(proxyList,Proxy.NO_PROXY);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_connectionFailedLjava_net_URILjava_net_SocketAddressLjava_io_IOException()
			throws URISyntaxException {
		// set http proxy
		System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
		System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
		// set https proxy
		System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
		System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
		// set ftp proxy
		System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
		System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
		// set socks proxy
		System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
		System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));

		List proxyList1 = selector.select(httpUri);
		assertNotNull(proxyList1);
		assertEquals(1, proxyList1.size());
		Proxy proxy1 = (Proxy) proxyList1.get(0);
		selector
				.connectFailed(httpUri, proxy1.address(), new SocketException());

		List proxyList2 = selector.select(httpUri);
		assertNotNull(proxyList2);
		assertEquals(1, proxyList2.size());
		Proxy proxy2 = (Proxy) proxyList2.get(0);
		// Default implemention doesn't change the proxy list
		assertEquals(proxy1, proxy2);
	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_connectionFailedLjava_net_URILjava_net_SocketAddressLjava_io_IOException_IllegalArguement()
			throws URISyntaxException {
		SocketAddress sa = InetSocketAddress.createUnresolved("127.0.0.1", 0);
		try {
			selector.connectFailed(null, sa, new SocketException());
			fail("should throw IllegalArgumentException if any argument is null.");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			selector.connectFailed(httpUri, null, new SocketException());
			fail("should throw IllegalArgumentException if any argument is null.");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			selector.connectFailed(httpUri, sa, null);
			fail("should throw IllegalArgumentException if any argument is null.");
		} catch (IllegalArgumentException e) {
			// expected
		}

	}

	/**
	 * @tests java.net.ProxySelector#select(URI)
	 */
	public void test_selectLjava_net_URI_IllegalArgument()
			throws URISyntaxException {
		URI[] illegalUris = { new URI("abc"), new URI("http"), null };
		for (int i = 0; i < illegalUris.length; i++) {
			try {
				selector.select(illegalUris[i]);
				fail("should throw IllegalArgumentException");
			} catch (IllegalArgumentException e) {
				// expected
			}
		}
	}

	/*
	 * asserts whether selectedProxyList contains one and only one element,
	 * and the element equals proxy.
	 */
	private void assertProxyEquals(List selectedProxyList, Proxy proxy) {
		assertNotNull(selectedProxyList);
		assertEquals(1, selectedProxyList.size());
		assertEquals((Proxy) selectedProxyList.get(0), proxy);
	}
	
	/*
	 * asserts whether selectedProxyList contains one and only one element,
	 * and the element equals proxy which is represented by arguments "type",
	 * "host","port".
	 */
	private void assertProxyEquals(List selectedProxyList, Proxy.Type type,
			String host, int port) {
		SocketAddress sa = InetSocketAddress.createUnresolved(host, port);
		Proxy proxy = new Proxy(type, sa);
		assertProxyEquals(selectedProxyList, proxy);
	}
	
	/*
	 * Mock selector for setDefault test
	 */
	static class MockProxySelector extends ProxySelector {

		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

		}

		public List <Proxy> select(URI uri) {
			return null;
		}
	}

	/*
	 * MockSecurityMaanger. It denies NetPermission("getProxySelector") and
	 * NetPermission("setProxySelector").
	 */
	class MockSecurityManager extends SecurityManager {
		public void checkPermission(Permission permission) {
			if (permission instanceof NetPermission) {
				if ("getProxySelector".equals(permission.getName())) {
					throw new SecurityException();
				}
			}

			if (permission instanceof NetPermission) {
				if ("setProxySelector".equals(permission.getName())) {
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

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// save original system properties
		orignalSystemProperties = (Properties) System.getProperties().clone();
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		// restore orignal system properties
		System.setProperties(orignalSystemProperties);
		super.tearDown();
	}
}

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
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.NetPermission;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.security.Permission;
import java.util.Map;

import junit.framework.TestCase;

public class ResponseCacheTest extends TestCase {

	/**
	 * @tests java.net.ResponseCache#getDefault()
	 */
	public void test_GetDefault() throws Exception {
		assertNull(ResponseCache.getDefault());
	}

	/**
	 * @tests java.net.ResponseCache#setDefault(ResponseCache)
	 */
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
}

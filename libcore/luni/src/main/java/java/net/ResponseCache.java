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
package java.net;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ResponseCache implements URLConnection caches. System default cache can be
 * registered by invoking ResponseCache.<code>setDefault</code>(ResponseCache),
 * and can be retrieved by invoking ResponseCache.<code>getDefault</code>.
 * If URLConnection#useCaches is set, <code>URLConnection</code> class will
 * use <code>ResponseCache</code> to store and get resources. Whether the
 * resource is cached depends on <code>ResponseCache</code> implementation. If
 * a request resource is cached, protocol handler will fecth it from the cache.
 * If the protocol handler fails to get resource from the cache, it turns to get
 * the resource from its original location.
 */
public abstract class ResponseCache {

    /*
     * _defaultResponseCache is used to store default response cache.
     */
    private static ResponseCache _defaultResponseCache = null;

    /*
     * "getResponseCache" permission. getDefault method requires this
     * permission.
     */
    private static NetPermission getResponseCachepermission = new NetPermission(
            "getResponseCache"); //$NON-NLS-1$

    /*
     * "setResponseCache" permission. setDefault method requires this
     * permission.
     */
    private static NetPermission setResponseCachepermission = new NetPermission(
            "setResponseCache"); //$NON-NLS-1$

    /*
     * check getResponseCache permission. getDefault method requires
     * "getResponseCache" permission if a security manager is installed.
     */
    private static void checkGetResponseCachePermission() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(getResponseCachepermission);
        }
    }

    /*
     * check setResponseCache permission. setDefault method requires
     * "setResponseCache" permission if a security manager is installed.
     */
    private static void checkSetResponseCachePermission() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(setResponseCachepermission);
        }
    }

    /**
     * Constructor method.
     */
    public ResponseCache() {
        super();
    }

    /**
     * Gets system default response cache.
     * 
     * @return default <code>ResponseCache</code>.
     * @throws SecurityException
     *             If a security manager is installed and it doesn't have
     *             <code>NetPermission</code>("getResponseCache").
     */
    public static ResponseCache getDefault() {
        checkGetResponseCachePermission();
        return _defaultResponseCache;
    }

    /**
     * Sets the system default response cache when responseCache is not null.
     * Otherwise, the method unsets the system default response cache. This
     * setting may be ignored by some non-standard protocols.
     * 
     * @param responseCache
     *            Set default <code>ResponseCache</code>. If responseCache is
     *            null, it unsets the cache.
     * @throws SecurityException
     *             If a security manager is installed and it doesn't have
     *             <code>NetPermission</code>("setResponseCache").
     */
    public static void setDefault(ResponseCache responseCache) {
        checkSetResponseCachePermission();
        _defaultResponseCache = responseCache;
    }

    /**
     * Gets the cached response according to requesting uri,method and headers.
     * 
     * @param uri
     *            A <code>URL</code> represents requesting uri.
     * @param rqstMethod
     *            A <code>String</code> represents requesting method.
     * @param rqstHeaders
     *            A <code>Map</code> from request header field names to lists
     *            of field values represents requesting headers.
     * @return A <code>CacheResponse</code> object if the request is available
     *         in the cache. Otherwise, this method returns null.
     * @throws IOException
     *             If an I/O error is encountered.
     * @throws IllegalArgumentException
     *             If any one of the parameters is null
     */
    public abstract CacheResponse get(URI uri, String rqstMethod,
            Map<String, List<String>> rqstHeaders) throws IOException;

    /**
     * Protocol handler calls this method after retrieving resources. The
     * <code>ResponseCache</code> decides whether the resource should be
     * cached. If the resource needs to be cached, this method will return a
     * <code>CacheRequest</code> with a <code>WriteableByteChannel</code>,
     * and then, protocol handler will use this channel to write the resource
     * data into the cache. Otherwise, if the resource doesn't need to be
     * cached, it returns null.
     * 
     * @param uri
     * @param conn
     * @return a <code>CacheRequest</code> which contains
     *         <code>WriteableByteChannel</code> if the resource is cached.
     *         Otherwise, it returns null.
     * @throws IOException
     *             If an I/O error is encountered.
     * @throws IllegalArgumentException
     *             If any one of the parameters is null.
     */
    public abstract CacheRequest put(URI uri, URLConnection conn)
            throws IOException;
}

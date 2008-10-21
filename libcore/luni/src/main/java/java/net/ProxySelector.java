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

/**
 * <p>
 * Selects applicable proxies when connecting to network resouce represented by
 * a <code>URI</code>. An implementation of <code>ProxySelector</code>
 * should be a concrete subclass of <code>ProxySelector</code>. Method
 * <code>select</code> returns a list of proxies according to the
 * <code>uri</code>. If a connection can't be established, the caller should
 * notify proxy selector by invoking <code>connectFailed</code> method.
 * </p>
 * <p>
 * A proxy selector can be registered/unregistered by calling
 * <code>setDefault</code> method and retrieved by calling
 * <code>getDefault</code> method.
 * </p>
 * 
 */
public abstract class ProxySelector {

    private static ProxySelector defaultSelector = new ProxySelectorImpl();

    /*
     * "getProxySelector" permission. getDefault method requires this
     * permission.
     */
    private final static NetPermission getProxySelectorPermission = new NetPermission(
            "getProxySelector"); //$NON-NLS-1$

    /*
     * "setProxySelector" permission. setDefault method requires this
     * permission.
     */
    private final static NetPermission setProxySelectorPermission = new NetPermission(
            "setProxySelector"); //$NON-NLS-1$

    /**
     * Constructor method.
     */
    public ProxySelector() {
        super();
    }

    /**
     * Gets system default <code>ProxySelector</code>.
     * 
     * @return system default <code>ProxySelector</code>.
     * @throws SecurtiyException
     *             If a security manager is installed and it doesn't have
     *             <code>NetPermission("getProxySelector")</code>.
     */
    public static ProxySelector getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(getProxySelectorPermission);
        }
        return defaultSelector;
    }

    /**
     * Sets system default <code>ProxySelector</code>. Unsets system default
     * <code>ProxySelector</code> if <code>selector</code> is null.
     * 
     * @throws SecurtiyException
     *             If a security manager is installed and it doesn't have
     *             <code>NetPermission("setProxySelector")</code>.
     */
    public static void setDefault(ProxySelector selector) {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(setProxySelectorPermission);
        }
        defaultSelector = selector;
    }

    /**
     * Gets applicable proxies based on the accessing protocol of
     * <code>uri</code>. The format of URI is defined as below:
     * <li>http URI stands for http connection.</li>
     * <li>https URI stands for https connection.</li>
     * <li>ftp URI stands for ftp connection.</li>
     * <li>socket:://ip:port URI stands for tcp client sockets connection.</li>
     * 
     * @param uri
     *            the destination <code>URI</code> object.
     * @return a list contains all applicable proxies. If no proxy is available,
     *         returns a list only contains one element
     *         <code>Proxy.NO_PROXY</code>.
     * @throws IllegalArgumentException
     *             If any argument is null.
     */
    public abstract List<Proxy> select(URI uri);

    /**
     * If the connection can not be established to the proxy server, this method
     * will be called. An implementation may adjust proxy the sequence of
     * proxies returned by <code>select(String, String)</code>.
     * 
     * @param uri
     *            the <code>URI</code> that the connection fails to connect
     *            to.
     * @param sa
     *            <code>SocketAddress</code> of the proxy.
     * @param ioe
     *            The <code>IOException</code> which is thrown during
     *            connection establishment.
     * @throws IllegalArgumentException
     *             If any argument is null.
     */
    public abstract void connectFailed(URI uri, SocketAddress sa,
            IOException ioe);
}

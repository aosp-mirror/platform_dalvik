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

package java.net;

/**
 * This class is able to obtain authentication info for a connection, usually
 * from user. First the application has to set the default authenticator which
 * extends <code>Authenticator</code> by
 * <code>setDefault(Authenticator a)</code>.
 * <p>
 * It should override <code>getPasswordAuthentication()</code> which dictates
 * how the authentication info should be obtained.
 * 
 * @see #setDefault
 * @see #getPasswordAuthentication
 */
public abstract class Authenticator {

    // the default authenticator that needs to be set
    private static Authenticator thisAuthenticator;

    private static final NetPermission requestPasswordAuthenticationPermission = new NetPermission(
            "requestPasswordAuthentication"); //$NON-NLS-1$

    private static final NetPermission setDefaultAuthenticatorPermission = new NetPermission(
            "setDefaultAuthenticator"); //$NON-NLS-1$

    // the requester connection info
    private String host;

    private InetAddress addr;

    private int port;

    private String protocol;

    private String prompt;

    private String scheme;

    private URL url;

    private RequestorType rt;

    /**
     * This method is responsible for retrieving the username and password for
     * the sender. The implementation varies. The subclass has to overwrite
     * this.
     * <p>
     * It returns null by default.
     * 
     * @return java.net.PasswordAuthentication The password authentication that
     *         it obtains
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        return null;
    }

    /**
     * Returns the port of the connection that requests authorization.
     * 
     * @return int the port of the connection
     */
    protected final int getRequestingPort() {
        return this.port;
    }

    /**
     * Returns the address of the connection that requests authorization or null
     * if unknown.
     * 
     * @return InetAddress the address of the connection
     */
    protected final InetAddress getRequestingSite() {
        return this.addr;
    }

    /**
     * Returns the realm (prompt string) of the connection that requires
     * authorization.
     * 
     * @return java.lang.String the prompt string of the connection
     */
    protected final String getRequestingPrompt() {
        return this.prompt;
    }

    /**
     * Returns the protocol of the connection that requests authorization.
     * 
     * @return java.lang.String the protocol of connection
     */
    protected final String getRequestingProtocol() {
        return this.protocol;
    }

    /**
     * Returns the scheme of the connection that requires authorization. Eg.
     * Basic
     * 
     * @return java.lang.String the scheme of the connection
     */
    protected final String getRequestingScheme() {
        return this.scheme;
    }

    /**
     * If the permission check of the security manager does not result in a
     * security exception, this method invokes the methods of the registered
     * authenticator to get the authentication info.
     * 
     * @return java.net.PasswordAuthentication the authentication info
     * 
     * @param rAddr
     *            java.net.InetAddress the address of the connection that
     *            requests authentication
     * @param rPort
     *            int the port of the connection that requests authentication
     * @param rProtocol
     *            java.lang.String the protocol of the connection that requests
     *            authentication
     * @param rPrompt
     *            java.lang.String the realm of the connection that requests
     *            authentication
     * @param rScheme
     *            java.lang.String the scheme of the connection that requests
     *            authentication
     * @throws SecurityException
     *             if requestPasswordAuthenticationPermission is denied
     */
    public static synchronized PasswordAuthentication requestPasswordAuthentication(
            InetAddress rAddr, int rPort, String rProtocol, String rPrompt,
            String rScheme) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(requestPasswordAuthenticationPermission);
        }
        if (thisAuthenticator == null) {
            return null;
        }
        // set the requester info so it knows what it is requesting
        // authentication for
        thisAuthenticator.addr = rAddr;
        thisAuthenticator.port = rPort;
        thisAuthenticator.protocol = rProtocol;
        thisAuthenticator.prompt = rPrompt;
        thisAuthenticator.scheme = rScheme;
        thisAuthenticator.rt = RequestorType.SERVER;

        // returns the authentication info obtained by the registered
        // Authenticator
        return thisAuthenticator.getPasswordAuthentication();
    }

    /**
     * This method sets <code>a</code> to be the default authenticator. It
     * will be called whenever the realm that the URL is pointing to requires
     * authorization. If there is a security manager set then the caller must
     * have the NetPermission "setDefaultAuthenticator".
     * 
     * @param a
     *            java.net.Authenticator The authenticator to be set.
     * @throws SecurityException
     *             if requestPasswordAuthenticationPermission is denied
     */
    public static void setDefault(Authenticator a) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(setDefaultAuthenticatorPermission);
        }
        thisAuthenticator = a;
    }

    /**
     * If the permission check of the security manager does not result in a
     * security exception, this method invokes the methods of the registered
     * authenticator to get the authentication info.
     * 
     * @return java.net.PasswordAuthentication the authentication info
     * 
     * @param rHost
     *            java.lang.String the host name of the connection that requests
     *            authentication
     * @param rAddr
     *            java.net.InetAddress the address of the connection that
     *            requests authentication
     * @param rPort
     *            int the port of the connection that requests authentication
     * @param rProtocol
     *            java.lang.String the protocol of the connection that requests
     *            authentication
     * @param rPrompt
     *            java.lang.String the realm of the connection that requests
     *            authentication
     * @param rScheme
     *            java.lang.String the scheme of the connection that requests
     *            authentication
     * @throws SecurityException
     *             if requestPasswordAuthenticationPermission is denied
     */
    public static synchronized PasswordAuthentication requestPasswordAuthentication(
            String rHost, InetAddress rAddr, int rPort, String rProtocol,
            String rPrompt, String rScheme) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(requestPasswordAuthenticationPermission);
        }
        if (thisAuthenticator == null) {
            return null;
        }
        // set the requester info so it knows what it is requesting
        // authentication for
        thisAuthenticator.host = rHost;
        thisAuthenticator.addr = rAddr;
        thisAuthenticator.port = rPort;
        thisAuthenticator.protocol = rProtocol;
        thisAuthenticator.prompt = rPrompt;
        thisAuthenticator.scheme = rScheme;
        thisAuthenticator.rt = RequestorType.SERVER;

        // returns the authentication info obtained by the registered
        // Authenticator
        return thisAuthenticator.getPasswordAuthentication();
    }

    /**
     * Return the host name of the connection that requests authentication, or
     * null if unknown.
     */
    protected final String getRequestingHost() {
        return host;
    }

    /**
     * If the permission check of the security manager does not result in a
     * security exception, this method invokes the methods of the registered
     * authenticator to get the authentication info.
     * 
     * @return java.net.PasswordAuthentication the authentication info
     * 
     * @param rHost
     *            java.lang.String the host name of the connection that requests
     *            authentication
     * @param rAddr
     *            java.net.InetAddress the address of the connection that
     *            requests authentication
     * @param rPort
     *            int the port of the connection that requests authentication
     * @param rProtocol
     *            java.lang.String the protocol of the connection that requests
     *            authentication
     * @param rPrompt
     *            java.lang.String the realm of the connection that requests
     *            authentication
     * @param rScheme
     *            java.lang.String the scheme of the connection that requests
     *            authentication
     * @param rURL
     *            java.net.URL the url of the connection that requests
     *            authentication
     * @param reqType
     *            java.net.Authenticator.RequestorType the RequestorType of the
     *            connection that requests authentication
     * @throws SecurityException
     *             if requestPasswordAuthenticationPermission is denied
     */
    public static PasswordAuthentication requestPasswordAuthentication(
            String rHost, InetAddress rAddr, int rPort, String rProtocol,
            String rPrompt, String rScheme, URL rURL,
            Authenticator.RequestorType reqType) {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(requestPasswordAuthenticationPermission);
        }
        if (null == thisAuthenticator) {
            return null;
        }
        // sets the requester info so it knows what it is requesting
        // authentication for
        thisAuthenticator.host = rHost;
        thisAuthenticator.addr = rAddr;
        thisAuthenticator.port = rPort;
        thisAuthenticator.protocol = rProtocol;
        thisAuthenticator.prompt = rPrompt;
        thisAuthenticator.scheme = rScheme;
        thisAuthenticator.url = rURL;
        thisAuthenticator.rt = reqType;

        // returns the authentication info obtained by the registered
        // Authenticator
        return thisAuthenticator.getPasswordAuthentication();

    }

    /**
     * returns the URL of the authentication resulted in this request.
     * 
     * @return the url of request
     */
    protected URL getRequestingURL() {
        return url;
    }

    /**
     * returns the type of this request, it can be proxy or server
     * 
     * @return RequestorType of request
     */
    protected Authenticator.RequestorType getRequestorType() {
        return rt;
    }

    /**
     * an enum class of requestor type
     */
    public enum RequestorType {

        /**
         * type of proxy server
         */
        PROXY,

        /**
         * type of origin server
         */
        SERVER
    }
}

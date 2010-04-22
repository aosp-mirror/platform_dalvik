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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import org.apache.harmony.luni.platform.INetworkSystem;
import org.apache.harmony.luni.platform.Platform;

/**
 * This class represents an instance of a URI as defined by RFC 2396.
 */
public final class URI implements Comparable<URI>, Serializable {

    private final static INetworkSystem NETWORK_SYSTEM = Platform.getNetworkSystem();

    private static final long serialVersionUID = -6052424284110960213l;

    static final String UNRESERVED = "_-!.~\'()*";
    static final String PUNCTUATION = ",;:$&+=";
    static final String RESERVED = PUNCTUATION + "?/[]@";
    static final String SOME_LEGAL = UNRESERVED + PUNCTUATION;
    static final String ALL_LEGAL = UNRESERVED + RESERVED;

    private String string;
    private transient String scheme;
    private transient String schemeSpecificPart;
    private transient String authority;
    private transient String userInfo;
    private transient String host;
    private transient int port = -1;
    private transient String path;
    private transient String query;
    private transient String fragment;
    private transient boolean opaque;
    private transient boolean absolute;
    private transient boolean serverAuthority = false;

    private transient int hash = -1;

    private URI() {}

    /**
     * Creates a new URI instance according to the given string {@code uri}.
     *
     * @param uri
     *            the textual URI representation to be parsed into a URI object.
     * @throws URISyntaxException
     *             if the given string {@code uri} doesn't fit to the
     *             specification RFC2396 or could not be parsed correctly.
     */
    public URI(String uri) throws URISyntaxException {
        parseURI(uri, false);
    }

    /**
     * Creates a new URI instance using the given arguments. This constructor
     * first creates a temporary URI string from the given components. This
     * string will be parsed later on to create the URI instance.
     * <p>
     * {@code [scheme:]scheme-specific-part[#fragment]}
     *
     * @param scheme
     *            the scheme part of the URI.
     * @param ssp
     *            the scheme-specific-part of the URI.
     * @param frag
     *            the fragment part of the URI.
     * @throws URISyntaxException
     *             if the temporary created string doesn't fit to the
     *             specification RFC2396 or could not be parsed correctly.
     */
    public URI(String scheme, String ssp, String frag)
            throws URISyntaxException {
        StringBuilder uri = new StringBuilder();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }
        if (ssp != null) {
            // QUOTE ILLEGAL CHARACTERS
            uri.append(quoteComponent(ssp, ALL_LEGAL));
        }
        if (frag != null) {
            uri.append('#');
            // QUOTE ILLEGAL CHARACTERS
            uri.append(quoteComponent(frag, ALL_LEGAL));
        }

        parseURI(uri.toString(), false);
    }

    /**
     * Creates a new URI instance using the given arguments. This constructor
     * first creates a temporary URI string from the given components. This
     * string will be parsed later on to create the URI instance.
     * <p>
     * {@code [scheme:][user-info@]host[:port][path][?query][#fragment]}
     *
     * @param scheme
     *            the scheme part of the URI.
     * @param userInfo
     *            the user information of the URI for authentication and
     *            authorization.
     * @param host
     *            the host name of the URI.
     * @param port
     *            the port number of the URI.
     * @param path
     *            the path to the resource on the host.
     * @param query
     *            the query part of the URI to specify parameters for the
     *            resource.
     * @param fragment
     *            the fragment part of the URI.
     * @throws URISyntaxException
     *             if the temporary created string doesn't fit to the
     *             specification RFC2396 or could not be parsed correctly.
     */
    public URI(String scheme, String userInfo, String host, int port,
            String path, String query, String fragment)
            throws URISyntaxException {

        if (scheme == null && userInfo == null && host == null && path == null
                && query == null && fragment == null) {
            this.path = "";
            return;
        }

        if (scheme != null && path != null && path.length() > 0
                && path.charAt(0) != '/') {
            throw new URISyntaxException(path, "Relative path");
        }

        StringBuilder uri = new StringBuilder();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }

        if (userInfo != null || host != null || port != -1) {
            uri.append("//");
        }

        if (userInfo != null) {
            // QUOTE ILLEGAL CHARACTERS in userInfo
            uri.append(quoteComponent(userInfo, SOME_LEGAL));
            uri.append('@');
        }

        if (host != null) {
            // check for IPv6 addresses that hasn't been enclosed
            // in square brackets
            if (host.indexOf(':') != -1 && host.indexOf(']') == -1
                    && host.indexOf('[') == -1) {
                host = "[" + host + "]";
            }
            uri.append(host);
        }

        if (port != -1) {
            uri.append(':');
            uri.append(port);
        }

        if (path != null) {
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(path, "/@" + SOME_LEGAL));
        }

        if (query != null) {
            uri.append('?');
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(query, ALL_LEGAL));
        }

        if (fragment != null) {
            // QUOTE ILLEGAL CHARS
            uri.append('#');
            uri.append(quoteComponent(fragment, ALL_LEGAL));
        }

        parseURI(uri.toString(), true);
    }

    /**
     * Creates a new URI instance using the given arguments. This constructor
     * first creates a temporary URI string from the given components. This
     * string will be parsed later on to create the URI instance.
     * <p>
     * {@code [scheme:]host[path][#fragment]}
     *
     * @param scheme
     *            the scheme part of the URI.
     * @param host
     *            the host name of the URI.
     * @param path
     *            the path to the resource on the host.
     * @param fragment
     *            the fragment part of the URI.
     * @throws URISyntaxException
     *             if the temporary created string doesn't fit to the
     *             specification RFC2396 or could not be parsed correctly.
     */
    public URI(String scheme, String host, String path, String fragment)
            throws URISyntaxException {
        this(scheme, null, host, -1, path, null, fragment);
    }

    /**
     * Creates a new URI instance using the given arguments. This constructor
     * first creates a temporary URI string from the given components. This
     * string will be parsed later on to create the URI instance.
     * <p>
     * {@code [scheme:][//authority][path][?query][#fragment]}
     *
     * @param scheme
     *            the scheme part of the URI.
     * @param authority
     *            the authority part of the URI.
     * @param path
     *            the path to the resource on the host.
     * @param query
     *            the query part of the URI to specify parameters for the
     *            resource.
     * @param fragment
     *            the fragment part of the URI.
     * @throws URISyntaxException
     *             if the temporary created string doesn't fit to the
     *             specification RFC2396 or could not be parsed correctly.
     */
    public URI(String scheme, String authority, String path, String query,
            String fragment) throws URISyntaxException {
        if (scheme != null && path != null && path.length() > 0
                && path.charAt(0) != '/') {
            throw new URISyntaxException(path, "Relative path");
        }

        StringBuilder uri = new StringBuilder();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }
        if (authority != null) {
            uri.append("//");
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(authority, "@[]" + SOME_LEGAL));
        }

        if (path != null) {
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(path, "/@" + SOME_LEGAL));
        }
        if (query != null) {
            // QUOTE ILLEGAL CHARS
            uri.append('?');
            uri.append(quoteComponent(query, ALL_LEGAL));
        }
        if (fragment != null) {
            // QUOTE ILLEGAL CHARS
            uri.append('#');
            uri.append(quoteComponent(fragment, ALL_LEGAL));
        }

        parseURI(uri.toString(), false);
    }

    private void parseURI(String uri, boolean forceServer) throws URISyntaxException {
        String temp = uri;
        // assign uri string to the input value per spec
        string = uri;
        int index, index1, index2, index3;
        // parse into Fragment, Scheme, and SchemeSpecificPart
        // then parse SchemeSpecificPart if necessary

        // Fragment
        index = temp.indexOf('#');
        if (index != -1) {
            // remove the fragment from the end
            fragment = temp.substring(index + 1);
            validateFragment(uri, fragment, index + 1);
            temp = temp.substring(0, index);
        }

        // Scheme and SchemeSpecificPart
        index = index1 = temp.indexOf(':');
        index2 = temp.indexOf('/');
        index3 = temp.indexOf('?');

        // if a '/' or '?' occurs before the first ':' the uri has no
        // specified scheme, and is therefore not absolute
        if (index != -1 && (index2 >= index || index2 == -1)
                && (index3 >= index || index3 == -1)) {
            // the characters up to the first ':' comprise the scheme
            absolute = true;
            scheme = temp.substring(0, index);
            if (scheme.length() == 0) {
                throw new URISyntaxException(uri, "Scheme expected", index);
            }
            validateScheme(uri, scheme, 0);
            schemeSpecificPart = temp.substring(index + 1);
            if (schemeSpecificPart.length() == 0) {
                throw new URISyntaxException(uri, "Scheme-specific part expected", index + 1);
            }
        } else {
            absolute = false;
            schemeSpecificPart = temp;
        }

        if (scheme == null || schemeSpecificPart.length() > 0
                && schemeSpecificPart.charAt(0) == '/') {
            opaque = false;
            // the URI is hierarchical

            // Query
            temp = schemeSpecificPart;
            index = temp.indexOf('?');
            if (index != -1) {
                query = temp.substring(index + 1);
                temp = temp.substring(0, index);
                validateQuery(uri, query, index2 + 1 + index);
            }

            // Authority and Path
            if (temp.startsWith("//")) {
                index = temp.indexOf('/', 2);
                if (index != -1) {
                    authority = temp.substring(2, index);
                    path = temp.substring(index);
                } else {
                    authority = temp.substring(2);
                    if (authority.length() == 0 && query == null
                            && fragment == null) {
                        throw new URISyntaxException(uri, "Authority expected", uri.length());
                    }

                    path = "";
                    // nothing left, so path is empty (not null, path should
                    // never be null)
                }

                if (authority.length() == 0) {
                    authority = null;
                } else {
                    validateAuthority(uri, authority, index1 + 3);
                }
            } else { // no authority specified
                path = temp;
            }

            int pathIndex = 0;
            if (index2 > -1) {
                pathIndex += index2;
            }
            if (index > -1) {
                pathIndex += index;
            }
            validatePath(uri, path, pathIndex);
        } else { // if not hierarchical, URI is opaque
            opaque = true;
            validateSsp(uri, schemeSpecificPart, index2 + 2 + index);
        }

        parseAuthority(forceServer);
    }

    private void validateScheme(String uri, String scheme, int index)
            throws URISyntaxException {
        // first char needs to be an alpha char
        char ch = scheme.charAt(0);
        if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))) {
            throw new URISyntaxException(uri, "Illegal character in scheme", 0);
        }

        try {
            URIEncoderDecoder.validateSimple(scheme, "+-.");
        } catch (URISyntaxException e) {
            throw new URISyntaxException(uri, "Illegal character in scheme", index + e.getIndex());
        }
    }

    private void validateSsp(String uri, String ssp, int index)
            throws URISyntaxException {
        try {
            URIEncoderDecoder.validate(ssp, ALL_LEGAL);
        } catch (URISyntaxException e) {
            throw new URISyntaxException(uri,
                    e.getReason() + " in schemeSpecificPart", index + e.getIndex());
        }
    }

    private void validateAuthority(String uri, String authority, int index)
            throws URISyntaxException {
        try {
            URIEncoderDecoder.validate(authority, "@[]" + SOME_LEGAL);
        } catch (URISyntaxException e) {
            throw new URISyntaxException(uri, e.getReason() + " in authority", index + e.getIndex());
        }
    }

    private void validatePath(String uri, String path, int index)
            throws URISyntaxException {
        try {
            URIEncoderDecoder.validate(path, "/@" + SOME_LEGAL);
        } catch (URISyntaxException e) {
            throw new URISyntaxException(uri, e.getReason() + " in path", index + e.getIndex());
        }
    }

    private void validateQuery(String uri, String query, int index)
            throws URISyntaxException {
        try {
            URIEncoderDecoder.validate(query, ALL_LEGAL);
        } catch (URISyntaxException e) {
            throw new URISyntaxException(uri, e.getReason() + " in query", index + e.getIndex());

        }
    }

    private void validateFragment(String uri, String fragment, int index)
            throws URISyntaxException {
        try {
            URIEncoderDecoder.validate(fragment, ALL_LEGAL);
        } catch (URISyntaxException e) {
            throw new URISyntaxException(uri, e.getReason() + " in fragment", index + e.getIndex());
        }
    }

    /**
     * Parse the authority string into its component parts: user info,
     * host, and port. This operation doesn't apply to registry URIs, and
     * calling it on such <i>may</i> result in a syntax exception.
     *
     * @param forceServer true to always throw if the authority cannot be
     *     parsed. If false, this method may still throw for some kinds of
     *     errors; this unpredictable behaviour is consistent with the RI.
     */
    private void parseAuthority(boolean forceServer) throws URISyntaxException {
        if (authority == null) {
            return;
        }

        String tempUserInfo = null;
        String temp = authority;
        int index = temp.indexOf('@');
        int hostIndex = 0;
        if (index != -1) {
            // remove user info
            tempUserInfo = temp.substring(0, index);
            validateUserInfo(authority, tempUserInfo, 0);
            temp = temp.substring(index + 1); // host[:port] is left
            hostIndex = index + 1;
        }

        index = temp.lastIndexOf(':');
        int endIndex = temp.indexOf(']');

        String tempHost;
        int tempPort = -1;
        if (index != -1 && endIndex < index) {
            // determine port and host
            tempHost = temp.substring(0, index);

            if (index < (temp.length() - 1)) { // port part is not empty
                try {
                    tempPort = Integer.parseInt(temp.substring(index + 1));
                    if (tempPort < 0) {
                        if (forceServer) {
                            throw new URISyntaxException(authority,
                                    "Invalid port number", hostIndex + index + 1);
                        }
                        return;
                    }
                } catch (NumberFormatException e) {
                    if (forceServer) {
                        throw new URISyntaxException(authority,
                                "Invalid port number", hostIndex + index + 1);
                    }
                    return;
                }
            }
        } else {
            tempHost = temp;
        }

        if (tempHost.equals("")) {
            if (forceServer) {
                throw new URISyntaxException(authority, "Expected host", hostIndex);
            }
            return;
        }

        if (!isValidHost(forceServer, tempHost)) {
            return;
        }

        // this is a server based uri,
        // fill in the userInfo, host and port fields
        userInfo = tempUserInfo;
        host = tempHost;
        port = tempPort;
        serverAuthority = true;
    }

    private void validateUserInfo(String uri, String userInfo, int index)
            throws URISyntaxException {
        for (int i = 0; i < userInfo.length(); i++) {
            char ch = userInfo.charAt(i);
            if (ch == ']' || ch == '[') {
                throw new URISyntaxException(uri, "Illegal character in userInfo", index + i);
            }
        }
    }

    /**
     * Returns true if {@code host} is a well-formed host name or IP address.
     *
     * @param forceServer true to always throw if the host cannot be parsed. If
     *     false, this method may still throw for some kinds of errors; this
     *     unpredictable behaviour is consistent with the RI.
     */
    private boolean isValidHost(boolean forceServer, String host) throws URISyntaxException {
        if (host.startsWith("[")) {
            // IPv6 address
            if (!host.endsWith("]")) {
                throw new URISyntaxException(host,
                        "Expected a closing square bracket for IPv6 address", 0);
            }
            try {
                byte[] bytes = NETWORK_SYSTEM.ipStringToByteArray(host);
                /*
                 * The native IP parser may return 4 bytes for addresses like
                 * "[::FFFF:127.0.0.1]". This is allowed, but we must not accept
                 * IPv4-formatted addresses in square braces like "[127.0.0.1]".
                 */
                if (bytes.length == 16 || bytes.length == 4 && host.contains(":")) {
                    return true;
                }
            } catch (UnknownHostException e) {
            }
            throw new URISyntaxException(host, "Malformed IPv6 address");
        }

        // '[' and ']' can only be the first char and last char
        // of the host name
        if (host.indexOf('[') != -1 || host.indexOf(']') != -1) {
            throw new URISyntaxException(host, "Illegal character in host name", 0);
        }

        int index = host.lastIndexOf('.');
        if (index < 0 || index == host.length() - 1
                || !Character.isDigit(host.charAt(index + 1))) {
            // domain name
            if (isValidDomainName(host)) {
                return true;
            }
            if (forceServer) {
                throw new URISyntaxException(host, "Illegal character in host name", 0);
            }
            return false;
        }

        // IPv4 address
        try {
            if (NETWORK_SYSTEM.ipStringToByteArray(host).length == 4) {
                return true;
            }
        } catch (UnknownHostException e) {
        }

        if (forceServer) {
            throw new URISyntaxException(host, "Malformed IPv4 address", 0);
        }
        return false;
    }

    private boolean isValidDomainName(String host) {
        try {
            URIEncoderDecoder.validateSimple(host, "-.");
        } catch (URISyntaxException e) {
            return false;
        }

        String lastLabel = null;
        StringTokenizer st = new StringTokenizer(host, ".");
        while (st.hasMoreTokens()) {
            lastLabel = st.nextToken();
            if (lastLabel.startsWith("-") || lastLabel.endsWith("-")) {
                return false;
            }
        }

        if (lastLabel == null) {
            return false;
        }

        if (!lastLabel.equals(host)) {
            char ch = lastLabel.charAt(0);
            if (ch >= '0' && ch <= '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Quote illegal chars for each component, but not the others
     * 
     * @param component java.lang.String the component to be converted
     * @param legalSet the legal character set allowed in the component
     * @return java.lang.String the converted string
     */
    private String quoteComponent(String component, String legalSet) {
        try {
            /*
             * Use a different encoder than URLEncoder since: 1. chars like "/",
             * "#", "@" etc needs to be preserved instead of being encoded, 2.
             * UTF-8 char set needs to be used for encoding instead of default
             * platform one
             */
            return URIEncoderDecoder.quoteIllegal(component, legalSet);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Compares this URI with the given argument {@code uri}. This method will
     * return a negative value if this URI instance is less than the given
     * argument and a positive value if this URI instance is greater than the
     * given argument. The return value {@code 0} indicates that the two
     * instances represent the same URI. To define the order the single parts of
     * the URI are compared with each other. String components will be ordered
     * in the natural case-sensitive way. A hierarchical URI is less than an
     * opaque URI and if one part is {@code null} the URI with the undefined
     * part is less than the other one.
     *
     * @param uri
     *            the URI this instance has to compare with.
     * @return the value representing the order of the two instances.
     */
    public int compareTo(URI uri) {
        int ret;

        // compare schemes
        if (scheme == null && uri.scheme != null) {
            return -1;
        } else if (scheme != null && uri.scheme == null) {
            return 1;
        } else if (scheme != null && uri.scheme != null) {
            ret = scheme.compareToIgnoreCase(uri.scheme);
            if (ret != 0) {
                return ret;
            }
        }

        // compare opacities
        if (!opaque && uri.opaque) {
            return -1;
        } else if (opaque && !uri.opaque) {
            return 1;
        } else if (opaque && uri.opaque) {
            ret = schemeSpecificPart.compareTo(uri.schemeSpecificPart);
            if (ret != 0) {
                return ret;
            }
        } else {

            // otherwise both must be hierarchical

            // compare authorities
            if (authority != null && uri.authority == null) {
                return 1;
            } else if (authority == null && uri.authority != null) {
                return -1;
            } else if (authority != null && uri.authority != null) {
                if (host != null && uri.host != null) {
                    // both are server based, so compare userInfo, host, port
                    if (userInfo != null && uri.userInfo == null) {
                        return 1;
                    } else if (userInfo == null && uri.userInfo != null) {
                        return -1;
                    } else if (userInfo != null && uri.userInfo != null) {
                        ret = userInfo.compareTo(uri.userInfo);
                        if (ret != 0) {
                            return ret;
                        }
                    }

                    // userInfo's are the same, compare hostname
                    ret = host.compareToIgnoreCase(uri.host);
                    if (ret != 0) {
                        return ret;
                    }

                    // compare port
                    if (port != uri.port) {
                        return port - uri.port;
                    }
                } else { // one or both are registry based, compare the whole
                    // authority
                    ret = authority.compareTo(uri.authority);
                    if (ret != 0) {
                        return ret;
                    }
                }
            }

            // authorities are the same
            // compare paths
            ret = path.compareTo(uri.path);
            if (ret != 0) {
                return ret;
            }

            // compare queries

            if (query != null && uri.query == null) {
                return 1;
            } else if (query == null && uri.query != null) {
                return -1;
            } else if (query != null && uri.query != null) {
                ret = query.compareTo(uri.query);
                if (ret != 0) {
                    return ret;
                }
            }
        }

        // everything else is identical, so compare fragments
        if (fragment != null && uri.fragment == null) {
            return 1;
        } else if (fragment == null && uri.fragment != null) {
            return -1;
        } else if (fragment != null && uri.fragment != null) {
            ret = fragment.compareTo(uri.fragment);
            if (ret != 0) {
                return ret;
            }
        }

        // identical
        return 0;
    }

    /**
     * Returns the URI formed by parsing {@code uri}. This method behaves
     * identically to the string constructor but throws a different exception
     * on failure. The constructor fails with a checked {@link
     * URISyntaxException}; this method fails with an unchecked {@link
     * IllegalArgumentException}.
     */
    public static URI create(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private URI duplicate() {
        URI clone = new URI();
        clone.absolute = absolute;
        clone.authority = authority;
        clone.fragment = fragment;
        clone.host = host;
        clone.opaque = opaque;
        clone.path = path;
        clone.port = port;
        clone.query = query;
        clone.scheme = scheme;
        clone.schemeSpecificPart = schemeSpecificPart;
        clone.userInfo = userInfo;
        clone.serverAuthority = serverAuthority;
        return clone;
    }

    /*
     * Takes a string that may contain hex sequences like %F1 or %2b and
     * converts the hex values following the '%' to lowercase
     */
    private String convertHexToLowerCase(String s) {
        StringBuilder result = new StringBuilder("");
        if (s.indexOf('%') == -1) {
            return s;
        }

        int index, prevIndex = 0;
        while ((index = s.indexOf('%', prevIndex)) != -1) {
            result.append(s.substring(prevIndex, index + 1));
            result.append(s.substring(index + 1, index + 3).toLowerCase());
            index += 3;
            prevIndex = index;
        }
        return result.toString();
    }

    /**
     * Returns true if {@code first} and {@code second} are equal after
     * unescaping hex sequences like %F1 and %2b.
     */
    private boolean escapedEquals(String first, String second) {
        if (first.indexOf('%') != second.indexOf('%')) {
            return first.equals(second);
        }

        int index, prevIndex = 0;
        while ((index = first.indexOf('%', prevIndex)) != -1
                && second.indexOf('%', prevIndex) == index) {
            boolean match = first.substring(prevIndex, index).equals(
                    second.substring(prevIndex, index));
            if (!match) {
                return false;
            }

            match = first.substring(index + 1, index + 3).equalsIgnoreCase(
                    second.substring(index + 1, index + 3));
            if (!match) {
                return false;
            }

            index += 3;
            prevIndex = index;
        }
        return first.substring(prevIndex).equals(second.substring(prevIndex));
    }

    /**
     * Compares this URI instance with the given argument {@code o} and
     * determines if both are equal. Two URI instances are equal if all single
     * parts are identical in their meaning.
     *
     * @param o
     *            the URI this instance has to be compared with.
     * @return {@code true} if both URI instances point to the same resource,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof URI)) {
            return false;
        }
        URI uri = (URI) o;

        if (uri.fragment == null && fragment != null || uri.fragment != null
                && fragment == null) {
            return false;
        } else if (uri.fragment != null && fragment != null) {
            if (!escapedEquals(uri.fragment, fragment)) {
                return false;
            }
        }

        if (uri.scheme == null && scheme != null || uri.scheme != null
                && scheme == null) {
            return false;
        } else if (uri.scheme != null && scheme != null) {
            if (!uri.scheme.equalsIgnoreCase(scheme)) {
                return false;
            }
        }

        if (uri.opaque && opaque) {
            return escapedEquals(uri.schemeSpecificPart,
                    schemeSpecificPart);
        } else if (!uri.opaque && !opaque) {
            if (!escapedEquals(path, uri.path)) {
                return false;
            }

            if (uri.query != null && query == null || uri.query == null
                    && query != null) {
                return false;
            } else if (uri.query != null && query != null) {
                if (!escapedEquals(uri.query, query)) {
                    return false;
                }
            }

            if (uri.authority != null && authority == null
                    || uri.authority == null && authority != null) {
                return false;
            } else if (uri.authority != null && authority != null) {
                if (uri.host != null && host == null || uri.host == null
                        && host != null) {
                    return false;
                } else if (uri.host == null && host == null) {
                    // both are registry based, so compare the whole authority
                    return escapedEquals(uri.authority, authority);
                } else { // uri.host != null && host != null, so server-based
                    if (!host.equalsIgnoreCase(uri.host)) {
                        return false;
                    }

                    if (port != uri.port) {
                        return false;
                    }

                    if (uri.userInfo != null && userInfo == null
                            || uri.userInfo == null && userInfo != null) {
                        return false;
                    } else if (uri.userInfo != null && userInfo != null) {
                        return escapedEquals(userInfo, uri.userInfo);
                    } else {
                        return true;
                    }
                }
            } else {
                // no authority
                return true;
            }

        } else {
            // one is opaque, the other hierarchical
            return false;
        }
    }

    /**
     * Gets the decoded authority part of this URI.
     *
     * @return the decoded authority part or {@code null} if undefined.
     */
    public String getAuthority() {
        return decode(authority);
    }

    /**
     * Gets the decoded fragment part of this URI.
     * 
     * @return the decoded fragment part or {@code null} if undefined.
     */
    public String getFragment() {
        return decode(fragment);
    }

    /**
     * Gets the host part of this URI.
     * 
     * @return the host part or {@code null} if undefined.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the decoded path part of this URI.
     * 
     * @return the decoded path part or {@code null} if undefined.
     */
    public String getPath() {
        return decode(path);
    }

    /**
     * Gets the port number of this URI.
     * 
     * @return the port number or {@code -1} if undefined.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the decoded query part of this URI.
     * 
     * @return the decoded query part or {@code null} if undefined.
     */
    public String getQuery() {
        return decode(query);
    }

    /**
     * Gets the authority part of this URI in raw form.
     * 
     * @return the encoded authority part or {@code null} if undefined.
     */
    public String getRawAuthority() {
        return authority;
    }

    /**
     * Gets the fragment part of this URI in raw form.
     * 
     * @return the encoded fragment part or {@code null} if undefined.
     */
    public String getRawFragment() {
        return fragment;
    }

    /**
     * Gets the path part of this URI in raw form.
     * 
     * @return the encoded path part or {@code null} if undefined.
     */
    public String getRawPath() {
        return path;
    }

    /**
     * Gets the query part of this URI in raw form.
     * 
     * @return the encoded query part or {@code null} if undefined.
     */
    public String getRawQuery() {
        return query;
    }

    /**
     * Gets the scheme-specific part of this URI in raw form.
     * 
     * @return the encoded scheme-specific part or {@code null} if undefined.
     */
    public String getRawSchemeSpecificPart() {
        return schemeSpecificPart;
    }

    /**
     * Gets the user-info part of this URI in raw form.
     * 
     * @return the encoded user-info part or {@code null} if undefined.
     */
    public String getRawUserInfo() {
        return userInfo;
    }

    /**
     * Gets the scheme part of this URI.
     * 
     * @return the scheme part or {@code null} if undefined.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Gets the decoded scheme-specific part of this URI.
     * 
     * @return the decoded scheme-specific part or {@code null} if undefined.
     */
    public String getSchemeSpecificPart() {
        return decode(schemeSpecificPart);
    }

    /**
     * Gets the decoded user-info part of this URI.
     * 
     * @return the decoded user-info part or {@code null} if undefined.
     */
    public String getUserInfo() {
        return decode(userInfo);
    }

    /**
     * Gets the hashcode value of this URI instance.
     *
     * @return the appropriate hashcode value.
     */
    @Override
    public int hashCode() {
        if (hash == -1) {
            hash = getHashString().hashCode();
        }
        return hash;
    }

    /**
     * Indicates whether this URI is absolute, which means that a scheme part is
     * defined in this URI.
     * 
     * @return {@code true} if this URI is absolute, {@code false} otherwise.
     */
    public boolean isAbsolute() {
        return absolute;
    }

    /**
     * Indicates whether this URI is opaque or not. An opaque URI is absolute
     * and has a scheme-specific part which does not start with a slash
     * character. All parts except scheme, scheme-specific and fragment are
     * undefined.
     * 
     * @return {@code true} if the URI is opaque, {@code false} otherwise.
     */
    public boolean isOpaque() {
        return opaque;
    }

    /*
     * normalize path, and return the resulting string
     */
    private String normalize(String path) {
        // count the number of '/'s, to determine number of segments
        int index = -1;
        int pathLength = path.length();
        int size = 0;
        if (pathLength > 0 && path.charAt(0) != '/') {
            size++;
        }
        while ((index = path.indexOf('/', index + 1)) != -1) {
            if (index + 1 < pathLength && path.charAt(index + 1) != '/') {
                size++;
            }
        }

        String[] segList = new String[size];
        boolean[] include = new boolean[size];

        // break the path into segments and store in the list
        int current = 0;
        int index2;
        index = (pathLength > 0 && path.charAt(0) == '/') ? 1 : 0;
        while ((index2 = path.indexOf('/', index + 1)) != -1) {
            segList[current++] = path.substring(index, index2);
            index = index2 + 1;
        }

        // if current==size, then the last character was a slash
        // and there are no more segments
        if (current < size) {
            segList[current] = path.substring(index);
        }

        // determine which segments get included in the normalized path
        for (int i = 0; i < size; i++) {
            include[i] = true;
            if (segList[i].equals("..")) {
                int remove = i - 1;
                // search back to find a segment to remove, if possible
                while (remove > -1 && !include[remove]) {
                    remove--;
                }
                // if we find a segment to remove, remove it and the ".."
                // segment
                if (remove > -1 && !segList[remove].equals("..")) {
                    include[remove] = false;
                    include[i] = false;
                }
            } else if (segList[i].equals(".")) {
                include[i] = false;
            }
        }

        // put the path back together
        StringBuilder newPath = new StringBuilder();
        if (path.startsWith("/")) {
            newPath.append('/');
        }

        for (int i = 0; i < segList.length; i++) {
            if (include[i]) {
                newPath.append(segList[i]);
                newPath.append('/');
            }
        }

        // if we used at least one segment and the path previously ended with
        // a slash and the last segment is still used, then delete the extra
        // trailing '/'
        if (!path.endsWith("/") && segList.length > 0
                && include[segList.length - 1]) {
            newPath.deleteCharAt(newPath.length() - 1);
        }

        String result = newPath.toString();

        // check for a ':' in the first segment if one exists,
        // prepend "./" to normalize
        index = result.indexOf(':');
        index2 = result.indexOf('/');
        if (index != -1 && (index < index2 || index2 == -1)) {
            newPath.insert(0, "./");
            result = newPath.toString();
        }
        return result;
    }

    /**
     * Normalizes the path part of this URI.
     *
     * @return an URI object which represents this instance with a normalized
     *         path.
     */
    public URI normalize() {
        if (opaque) {
            return this;
        }
        String normalizedPath = normalize(path);
        // if the path is already normalized, return this
        if (path.equals(normalizedPath)) {
            return this;
        }
        // get an exact copy of the URI re-calculate the scheme specific part
        // since the path of the normalized URI is different from this URI.
        URI result = duplicate();
        result.path = normalizedPath;
        result.setSchemeSpecificPart();
        return result;
    }

    /**
     * Tries to parse the authority component of this URI to divide it into the
     * host, port, and user-info. If this URI is already determined as a
     * ServerAuthority this instance will be returned without changes.
     *
     * @return this instance with the components of the parsed server authority.
     * @throws URISyntaxException
     *             if the authority part could not be parsed as a server-based
     *             authority.
     */
    public URI parseServerAuthority() throws URISyntaxException {
        if (!serverAuthority) {
            parseAuthority(true);
        }
        return this;
    }

    /**
     * Makes the given URI {@code relative} to a relative URI against the URI
     * represented by this instance.
     *
     * @param relative
     *            the URI which has to be relativized against this URI.
     * @return the relative URI.
     */
    public URI relativize(URI relative) {
        if (relative.opaque || opaque) {
            return relative;
        }

        if (scheme == null ? relative.scheme != null : !scheme
                .equals(relative.scheme)) {
            return relative;
        }

        if (authority == null ? relative.authority != null : !authority
                .equals(relative.authority)) {
            return relative;
        }

        // normalize both paths
        String thisPath = normalize(path);
        String relativePath = normalize(relative.path);

        /*
         * if the paths aren't equal, then we need to determine if this URI's
         * path is a parent path (begins with) the relative URI's path
         */
        if (!thisPath.equals(relativePath)) {
            // if this URI's path doesn't end in a '/', add one
            if (!thisPath.endsWith("/")) {
                thisPath = thisPath + '/';
            }
            /*
             * if the relative URI's path doesn't start with this URI's path,
             * then just return the relative URI; the URIs have nothing in
             * common
             */
            if (!relativePath.startsWith(thisPath)) {
                return relative;
            }
        }

        URI result = new URI();
        result.fragment = relative.fragment;
        result.query = relative.query;
        // the result URI is the remainder of the relative URI's path
        result.path = relativePath.substring(thisPath.length());
        result.setSchemeSpecificPart();
        return result;
    }

    /**
     * Resolves the given URI {@code relative} against the URI represented by
     * this instance.
     *
     * @param relative
     *            the URI which has to be resolved against this URI.
     * @return the resolved URI.
     */
    public URI resolve(URI relative) {
        if (relative.absolute || opaque) {
            return relative;
        }

        URI result;
        if (relative.path.equals("") && relative.scheme == null
                && relative.authority == null && relative.query == null
                && relative.fragment != null) {
            // if the relative URI only consists of fragment,
            // the resolved URI is very similar to this URI,
            // except that it has the fragment from the relative URI.
            result = duplicate();
            result.fragment = relative.fragment;
            // no need to re-calculate the scheme specific part,
            // since fragment is not part of scheme specific part.
            return result;
        }

        if (relative.authority != null) {
            // if the relative URI has authority,
            // the resolved URI is almost the same as the relative URI,
            // except that it has the scheme of this URI.
            result = relative.duplicate();
            result.scheme = scheme;
            result.absolute = absolute;
        } else {
            // since relative URI has no authority,
            // the resolved URI is very similar to this URI,
            // except that it has the query and fragment of the relative URI,
            // and the path is different.
            result = duplicate();
            result.fragment = relative.fragment;
            result.query = relative.query;
            if (relative.path.startsWith("/")) {
                result.path = relative.path;
            } else {
                // resolve a relative reference
                int endIndex = path.lastIndexOf('/') + 1;
                result.path = normalize(path.substring(0, endIndex)
                        + relative.path);
            }
            // re-calculate the scheme specific part since
            // query and path of the resolved URI is different from this URI.
            result.setSchemeSpecificPart();
        }
        return result;
    }

    /**
     * Helper method used to re-calculate the scheme specific part of the
     * resolved or normalized URIs
     */
    private void setSchemeSpecificPart() {
        // ssp = [//authority][path][?query]
        StringBuilder ssp = new StringBuilder();
        if (authority != null) {
            ssp.append("//" + authority);
        }
        if (path != null) {
            ssp.append(path);
        }
        if (query != null) {
            ssp.append("?" + query);
        }
        schemeSpecificPart = ssp.toString();
        // reset string, so that it can be re-calculated correctly when asked.
        string = null;
    }

    /**
     * Creates a new URI instance by parsing the given string {@code relative}
     * and resolves the created URI against the URI represented by this
     * instance.
     *
     * @param relative
     *            the given string to create the new URI instance which has to
     *            be resolved later on.
     * @return the created and resolved URI.
     */
    public URI resolve(String relative) {
        return resolve(create(relative));
    }

    /**
     * Encode unicode chars that are not part of US-ASCII char set into the
     * escaped form
     * 
     * i.e. The Euro currency symbol is encoded as "%E2%82%AC".
     */
    private String encodeNonAscii(String s) {
        try {
            /*
             * Use a different encoder than URLEncoder since: 1. chars like "/",
             * "#", "@" etc needs to be preserved instead of being encoded, 2.
             * UTF-8 char set needs to be used for encoding instead of default
             * platform one 3. Only other chars need to be converted
             */
            return URIEncoderDecoder.encodeOthers(s);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private String decode(String s) {
        if (s == null) {
            return s;
        }

        try {
            return URIEncoderDecoder.decode(s);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Returns the textual string representation of this URI instance using the
     * US-ASCII encoding.
     *
     * @return the US-ASCII string representation of this URI.
     */
    public String toASCIIString() {
        return encodeNonAscii(toString());
    }

    /**
     * Returns the textual string representation of this URI instance.
     *
     * @return the textual string representation of this URI.
     */
    @Override
    public String toString() {
        if (string == null) {
            StringBuilder result = new StringBuilder();
            if (scheme != null) {
                result.append(scheme);
                result.append(':');
            }
            if (opaque) {
                result.append(schemeSpecificPart);
            } else {
                if (authority != null) {
                    result.append("//");
                    result.append(authority);
                }

                if (path != null) {
                    result.append(path);
                }

                if (query != null) {
                    result.append('?');
                    result.append(query);
                }
            }

            if (fragment != null) {
                result.append('#');
                result.append(fragment);
            }

            string = result.toString();
        }
        return string;
    }

    /*
     * Form a string from the components of this URI, similarly to the
     * toString() method. But this method converts scheme and host to lowercase,
     * and converts escaped octets to lowercase.
     */
    private String getHashString() {
        StringBuilder result = new StringBuilder();
        if (scheme != null) {
            result.append(scheme.toLowerCase());
            result.append(':');
        }
        if (opaque) {
            result.append(schemeSpecificPart);
        } else {
            if (authority != null) {
                result.append("//");
                if (host == null) {
                    result.append(authority);
                } else {
                    if (userInfo != null) {
                        result.append(userInfo + "@");
                    }
                    result.append(host.toLowerCase());
                    if (port != -1) {
                        result.append(":" + port);
                    }
                }
            }

            if (path != null) {
                result.append(path);
            }

            if (query != null) {
                result.append('?');
                result.append(query);
            }
        }

        if (fragment != null) {
            result.append('#');
            result.append(fragment);
        }

        return convertHexToLowerCase(result.toString());
    }

    /**
     * Converts this URI instance to a URL.
     *
     * @return the created URL representing the same resource as this URI.
     * @throws MalformedURLException
     *             if an error occurs while creating the URL or no protocol
     *             handler could be found.
     */
    public URL toURL() throws MalformedURLException {
        if (!absolute) {
            throw new IllegalArgumentException("URI is not absolute: " + toString());
        }
        return new URL(toString());
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        try {
            parseURI(string, false);
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException,
            ClassNotFoundException {
        // call toString() to ensure the value of string field is calculated
        toString();
        out.defaultWriteObject();
    }
}

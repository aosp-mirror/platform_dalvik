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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

public class URITest extends TestCase {

    private URI[] uris;

    private URI[] getUris() throws URISyntaxException {
        if (uris != null) {
            return uris;
        }

        uris = new URI[] {
                // single arg constructor
                new URI(
                        "http://user%60%20info@host/a%20path?qu%60%20ery#fr%5E%20ag"),
                // escaped octets for illegal chars
                new URI(
                        "http://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g"),
                // escaped octets for unicode chars
                new URI(
                        "ascheme://user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery#fr\u00E4\u00E8g"),
                // unicode chars equivalent to = new
                // URI("ascheme://user\u00df\u00a3info@host:0/a\u0080path?qu\u00a9\u00aeery#fr\u00e4\u00e8g"),

                // multiple arg constructors
                new URI("http", "user%60%20info", "host", 80, "/a%20path", //$NON-NLS-4$
                        "qu%60%20ery", "fr%5E%20ag"),
                // escaped octets for illegal
                new URI("http", "user%C3%9F%C2%A3info", "host", -1,
                        "/a%E2%82%ACpath", "qu%C2%A9%C2%AEery",
                        "fr%C3%A4%C3%A8g"),
                // escaped octets for unicode
                new URI("ascheme", "user\u00DF\u00A3info", "host", 80,
                        "/a\u20ACpath", "qu\u00A9\u00AEery", "fr\u00E4\u00E8g"),
                // unicode chars equivalent to = new
                // URI("ascheme", "user\u00df\u00a3info", "host", 80,
                // "/a\u0080path", "qu\u00a9\u00aeery", "fr\u00e4\u00e8g"),
                new URI("http", "user` info", "host", 81, "/a path", "qu` ery",
                        "fr^ ag"), // illegal chars
                new URI("http", "user%info", "host", 0, "/a%path", "que%ry",
                        "f%rag"),
                // % as illegal char, not escaped octet

                // urls with undefined components
                new URI("mailto", "user@domain.com", null),
                // no host, path, query or fragment
                new URI("../adirectory/file.html#"),
                // relative path with empty fragment;
                new URI("news", "comp.infosystems.www.servers.unix", null), //
                new URI(null, null, null, "fragment"), // only fragment
                new URI("telnet://server.org"), // only host
                new URI("http://reg:istry?query"),
                // malformed hostname, therefore registry-based,
                // with query
                new URI("file:///c:/temp/calculate.pl?"),
        // empty authority, non empty path, empty query
        };
        return uris;
    }

    /**
     * @tests java.net.URI#URI(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // tests for public URI(String uri) throws URISyntaxException

        String[] constructorTests = new String[] {
                "http://user@www.google.com:45/search?q=helpinfo#somefragment",
                // http with authority, query and fragment
                "ftp://ftp.is.co.za/rfc/rfc1808.txt", // ftp
                "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles", // gopher
                "mailto:mduerst@ifi.unizh.ch", // mailto
                "news:comp.infosystems.www.servers.unix", // news
                "telnet://melvyl.ucop.edu/", // telnet
                "http://123.24.17.98/test", // IPv4 authority
                "http://www.google.com:80/test",// domain name authority
                "http://joe@[3ffe:2a00:100:7031::1]:80/test",
                // IPv6 authority, with userinfo and port
                "/relative", // relative starting with /
                "//relative", // relative starting with //
                "relative", // relative with no /
                "#fragment",// relative just with fragment
                "http://user@host:80", // UI, host,port
                "http://user@host", // ui, host
                "http://host", // host
                "http://host:80", // host,port
                "http://joe@:80", // ui, port (becomes registry-based)
                "file:///foo/bar", // empty authority, non empty path
                "ht?tp://hoe@host:80", // miscellaneous tests
                "mai/lto:hey?joe#man", "http://host/a%20path#frag",
                // path with an escaped octet for space char
                "http://host/a%E2%82%ACpath#frag",
                // path with escaped octet for unicode char, not USASCII
                "http://host/a\u20ACpath#frag",
                // path with unicode char, not USASCII equivalent to
                // = "http://host/a\u0080path#frag",
                "http://host%20name/", // escaped octets in host (becomes
                // registry based)
                "http://host\u00DFname/", // unicodechar in host (becomes
                // registry based)
                // equivalent to = "http://host\u00dfname/",
                "ht123-+tp://www.google.com:80/test", // legal chars in scheme
        };

        for (int i = 0; i < constructorTests.length; i++) {
            try {
                new URI(constructorTests[i]);
            } catch (URISyntaxException e) {
                fail("Failed to construct URI for: " + constructorTests[i]
                        + " : " + e);
            }
        }

        String[] constructorTestsInvalid = new String[] {
                "http:///a path#frag", // space char in path, not in escaped
                // octet form, with no host
                "http://host/a[path#frag", // an illegal char, not in escaped
                // octet form, should throw an
                // exception
                "http://host/a%path#frag", // invalid escape sequence in path
                "http://host/a%#frag", // incomplete escape sequence in path

                "http://host#a frag", // space char in fragment, not in
                // escaped octet form, no path
                "http://host/a#fr#ag", // illegal char in fragment
                "http:///path#fr%ag", // invalid escape sequence in fragment,
                // with no host
                "http://host/path#frag%", // incomplete escape sequence in
                // fragment

                "http://host/path?a query#frag", // space char in query, not
                // in escaped octet form
                "http://host?query%ag", // invalid escape sequence in query, no
                // path
                "http:///path?query%", // incomplete escape sequence in query,
                // with no host

                "mailto:user^name@fklkf.com" // invalid char in scheme
        // specific part
        };

        int[] constructorTestsInvalidIndices = new int[] { 9, 13, 13, 13, 13,
                16, 15, 21, 18, 17, 18, 11 };

        for (int i = 0; i < constructorTestsInvalid.length; i++) {
            try {
                new URI(constructorTestsInvalid[i]);
                fail("Failed to throw URISyntaxException for: "
                        + constructorTestsInvalid[i]);
            } catch (URISyntaxException e) {
                assertTrue("Wrong index in URISytaxException for: "
                        + constructorTestsInvalid[i] + " expected: "
                        + constructorTestsInvalidIndices[i] + ", received: "
                        + e.getIndex(),
                        e.getIndex() == constructorTestsInvalidIndices[i]);
            }
        }

        String invalid2[] = {
                // authority validation
                "http://user@[3ffe:2x00:100:7031::1]:80/test", // malformed
                // IPv6 authority
                "http://[ipv6address]/apath#frag", // malformed ipv6 address
                "http://[ipv6address/apath#frag", // malformed ipv6 address
                "http://ipv6address]/apath#frag", // illegal char in host name
                "http://ipv6[address/apath#frag",
                "http://ipv6addr]ess/apath#frag",
                "http://ipv6address[]/apath#frag",
                // illegal char in username...
                "http://us[]er@host/path?query#frag", "http://host name/path", // illegal
                // char
                // in
                // authority
                "http://host^name#fragment", // illegal char in authority
                "telnet://us er@hostname/", // illegal char in authority
                // missing components
                "//", // Authority expected
                "ascheme://", // Authority expected
                "ascheme:", // Scheme-specific part expected
                // scheme validation
                "a scheme://reg/", // illegal char
                "1scheme://reg/", // non alpha char as 1st char
                "asche\u00dfme:ssp", // unicode char , not USASCII
                "asc%20heme:ssp" // escape octets
        };

        for (int i = 0; i < invalid2.length; i++) {
            try {
                new URI(invalid2[i]);
                fail("Failed to throw URISyntaxException for: " + invalid2[i]);
            } catch (URISyntaxException e) {
            }
        }
    }

    /**
     * @tests java.net.URI#URI(java.lang.String)
     */
    public void test_URI_String() {
        try {
            URI myUri = new URI(":abc@mymail.com");
            fail("TestA, URISyntaxException expected, but not received.");
        } catch (URISyntaxException e) {
            assertEquals("TestA, Wrong URISyntaxException index, ", 0, e
                    .getIndex());
        }

        try {
            URI uri = new URI("path[one");
            fail("TestB, URISyntaxException expected, but not received.");
        } catch (URISyntaxException e1) {
            assertEquals("TestB, Wrong URISyntaxException index, ", 4, e1
                    .getIndex());
        }

        try {
            URI uri = new URI(" ");
            fail("TestC, URISyntaxException expected, but not received.");
        } catch (URISyntaxException e2) {
            assertEquals("TestC, Wrong URISyntaxException index, ", 0, e2
                    .getIndex());
        }
    }

    /**
     * @tests java.net.URI#URI(java.lang.String, java.lang.String,
     *        java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_String() {
        // tests for public URI(String scheme, String ssp, String frag) throws
        // URISyntaxException

        URI uri;
        try {
            uri = new URI("mailto", "mduerst@ifi.unizh.ch", null);
            assertNull("wrong userinfo", uri.getUserInfo());
            assertNull("wrong hostname", uri.getHost());
            assertNull("wrong authority", uri.getAuthority());
            assertEquals("wrong port number", -1, uri.getPort());
            assertNull("wrong path", uri.getPath());
            assertNull("wrong query", uri.getQuery());
            assertNull("wrong fragment", uri.getFragment());
            assertEquals("wrong SchemeSpecificPart", "mduerst@ifi.unizh.ch",
                    uri.getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            fail("Unexpected Exception: " + e);
        }

        // scheme specific part can not be null
        try {
            uri = new URI("mailto", null, null);
            fail("Expected URISyntaxException");
        } catch (URISyntaxException e) {
        }

        // scheme needs to start with an alpha char
        try {
            uri = new URI("3scheme", "//authority/path", "fragment");
            fail("Expected URISyntaxException");
        } catch (URISyntaxException e) {
        }

        // scheme can not be empty string
        try {
            uri = new URI("", "//authority/path", "fragment");
            fail("Expected URISyntaxException");
        } catch (URISyntaxException e) {
        }
    }

    /**
     * @tests java.net.URI#URI(java.lang.String, java.lang.String,
     *        java.lang.String, int, java.lang.String, java.lang.String,
     *        java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringILjava_lang_StringLjava_lang_StringLjava_lang_String() {
        // tests for public URI(String scheme, String userinfo, String host, int
        // port, String path,
        // String query, String fragment) throws URISyntaxException

        // check for URISyntaxException for invalid Server Authority
        construct1("http", "user", "host\u00DFname", -1, "/file", "query",
                "fragment"); // unicode chars in host name
        // equivalent to construct1("http", "user", "host\u00dfname", -1,
        // "/file",
        // "query", "fragment");
        construct1("http", "user", "host%20name", -1, "/file", "query",
                "fragment"); // escaped octets in host name
        construct1("http", "user", "host name", -1, "/file", "query",
                "fragment"); // illegal char in host name
        construct1("http", "user", "host]name", -1, "/file", "query",
                "fragment"); // illegal char in host name

        // missing host name
        construct1("http", "user", "", 80, "/file", "query", "fragment");

        // missing host name
        construct1("http", "user", "", -1, "/file", "query", "fragment");

        // malformed ipv4 address
        construct1("telnet", null, "256.197.221.200", -1, null, null, null);

        // malformed ipv4 address
        construct1("ftp", null, "198.256.221.200", -1, null, null, null);

        // These tests fail on other implementations...
        // construct1("http", "user", null, 80, "/file", "query", "fragment");
        // //missing host name
        // construct1("http", "user", null, -1, "/file", "query", "fragment");
        // //missing host name

        // check for URISyntaxException for invalid scheme
        construct1("ht\u00DFtp", "user", "hostname", -1, "/file", "query",
                "fragment"); // unicode chars in scheme
        // equivalent to construct1("ht\u00dftp", "user", "hostname", -1,
        // "/file",
        // "query", "fragment");

        construct1("ht%20tp", "user", "hostname", -1, "/file", "query",
                "fragment"); // escaped octets in scheme
        construct1("ht tp", "user", "hostname", -1, "/file", "query",
                "fragment"); // illegal char in scheme
        construct1("ht]tp", "user", "hostname", -1, "/file", "query",
                "fragment"); // illegal char in scheme

        // relative path with scheme
        construct1("http", "user", "hostname", -1, "relative", "query",
                "fragment"); // unicode chars in scheme

        // functional test
        URI uri;
        try {
            uri = new URI("http", "us:e@r", "hostname", 85, "/file/dir#/qu?e/",
                    "qu?er#y", "frag#me?nt");
            assertEquals("wrong userinfo", "us:e@r", uri.getUserInfo());
            assertEquals("wrong hostname", "hostname", uri.getHost());
            assertEquals("wrong port number", 85, uri.getPort());
            assertEquals("wrong path", "/file/dir#/qu?e/", uri.getPath());
            assertEquals("wrong query", "qu?er#y", uri.getQuery());
            assertEquals("wrong fragment", "frag#me?nt", uri.getFragment());
            assertEquals("wrong SchemeSpecificPart",
                    "//us:e@r@hostname:85/file/dir#/qu?e/?qu?er#y", uri
                            .getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            fail("Unexpected Exception: " + e);
        }
    }

    /*
     * helper method checking if the 7 arg constructor throws URISyntaxException
     * for a given set of parameters
     */
    private void construct1(String scheme, String userinfo, String host,
            int port, String path, String query, String fragment) {
        try {
            URI uri = new URI(scheme, userinfo, host, port, path, query,
                    fragment);
            fail("Expected URISyntaxException not thrown for URI: "
                    + uri.toString());
        } catch (URISyntaxException e) {
            // this constructor throws URISyntaxException for malformed server
            // based authorities
        }
    }

    /**
     * @throws URISyntaxException
     * @tests java.net.URI#URI(java.lang.String, java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws URISyntaxException {
        // relative path
        try {
            URI myUri = new URI("http", "www.joe.com", "relative", "jimmy");
            fail("URISyntaxException expected but not received.");
        } catch (URISyntaxException e) {
            // Expected
        }

        // valid parameters for this constructor
        URI uri;

        uri = new URI("http", "www.joe.com", "/path", "jimmy");

        // illegal char in path
        uri = new URI("http", "www.host.com", "/path?q", "somefragment");

        // empty fragment
        uri = new URI("ftp", "ftp.is.co.za", "/rfc/rfc1808.txt", "");

        // path with escaped octet for unicode char, not USASCII
        uri = new URI("http", "host", "/a%E2%82%ACpath", "frag");

        // frag with unicode char, not USASCII
        // equivalent to = uri = new URI("http", "host", "/apath",
        // "\u0080frag");
        uri = new URI("http", "host", "/apath", "\u20ACfrag");

        // Regression test for Harmony-1693
        new URI(null, null, null, null);

        // regression for Harmony-1346
        try {
            uri = new URI("http", ":2:3:4:5:6:7:8", "/apath", "\u20ACfrag");
            fail("Should throw URISyntaxException");
        } catch (URISyntaxException e) {
            // Expected
        }
    }

    /**
     * @throws URISyntaxException
     * @tests java.net.URI#URI(java.lang.String, java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws URISyntaxException {
        // URISyntaxException on relative path
        try {
            URI myUri = new URI("http", "www.joe.com", "relative", "query",
                    "jimmy");
            fail("URISyntaxException expected but not received.");
        } catch (URISyntaxException e) {
            // Expected
        }

        // test if empty authority is parsed into undefined host, userinfo and
        // port and if unicode chars and escaped octets in components are
        // preserved, illegal chars are quoted
        URI uri = new URI("ht12-3+tp", "", "/p#a%E2%82%ACth", "q^u%25ery",
                "f/r\u00DFag");

        assertEquals("wrong scheme", "ht12-3+tp", uri.getScheme());
        assertNull("wrong authority", uri.getUserInfo());
        assertNull("wrong userinfo", uri.getUserInfo());
        assertNull("wrong hostname", uri.getHost());
        assertEquals("wrong port number", -1, uri.getPort());
        assertEquals("wrong path", "/p#a%E2%82%ACth", uri.getPath());
        assertEquals("wrong query", "q^u%25ery", uri.getQuery());
        assertEquals("wrong fragment", "f/r\u00DFag", uri.getFragment());
        // equivalent to = assertTrue("wrong fragment",
        // uri.getFragment().equals("f/r\u00dfag"));
        assertEquals("wrong SchemeSpecificPart", "///p#a%E2%82%ACth?q^u%25ery",
                uri.getSchemeSpecificPart());
        assertEquals("wrong RawSchemeSpecificPart",
                "///p%23a%25E2%2582%25ACth?q%5Eu%2525ery", uri
                        .getRawSchemeSpecificPart());
        assertEquals(
                "incorrect toString()",
                "ht12-3+tp:///p%23a%25E2%2582%25ACth?q%5Eu%2525ery#f/r\u00dfag",
                uri.toString());
        assertEquals("incorrect toASCIIString()",

        "ht12-3+tp:///p%23a%25E2%2582%25ACth?q%5Eu%2525ery#f/r%C3%9Fag", uri
                .toASCIIString());
    }

    /**
     * @throws URISyntaxException
     * @tests java.net.URI#URI(java.lang.String, java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     */
    public void test_fiveArgConstructor() throws URISyntaxException {
        // accept [] as part of valid ipv6 host name
        URI uri = new URI("ftp", "[0001:1234::0001]", "/dir1/dir2", "query",
                "frag");
        assertEquals("Returned incorrect host", "[0001:1234::0001]", uri
                .getHost());

        // do not accept [] as part of invalid ipv6 address
        try {
            uri = new URI("ftp", "[www.abc.com]", "/dir1/dir2", "query", "frag");
            fail("Expected URISyntaxException for invalid ipv6 address");
        } catch (URISyntaxException e) {
            // Expected
        }

        // do not accept [] as part of user info
        try {
            uri = new URI("ftp", "[user]@host", "/dir1/dir2", "query", "frag");
            fail("Expected URISyntaxException invalid user info");
        } catch (URISyntaxException e) {
            // Expected
        }
    }

    /**
     * @tests java.net.URI#compareTo(java.lang.Object)
     */
    public void test_compareToLjava_lang_Object() {
        // compareTo tests

        String[][] compareToData = new String[][] {
                // scheme tests
                { "http:test", "" }, // scheme null, scheme not null
                { "", "http:test" }, // reverse
                { "http:test", "ftp:test" }, // schemes different
                { "/test", "/test" }, // schemes null
                { "http://joe", "http://joe" }, // schemes same
                { "http://joe", "hTTp://joe" }, // schemes same ignoring case

                // opacity : one opaque, the other not
                { "http:opaque", "http://nonopaque" },
                { "http://nonopaque", "http:opaque" },
                { "mailto:abc", "mailto:abc" }, // same ssp
                { "mailto:abC", "mailto:Abc" }, // different, by case
                { "mailto:abc", "mailto:def" }, // different by letter
                { "mailto:abc#ABC", "mailto:abc#DEF" },
                { "mailto:abc#ABC", "mailto:abc#ABC" },
                { "mailto:abc#DEF", "mailto:abc#ABC" },

                // hierarchical tests..

                // diffrent authorities
                { "//www.test.com/test", "//www.test2.com/test" },

                { "/nullauth", "//nonnullauth/test" }, // one null authority
                { "//nonnull", "/null" },
                { "/hello", "/hello" }, // both authorities null
                // different userinfo
                { "http://joe@test.com:80", "http://test.com" },
                { "http://jim@test.com", "http://james@test.com" },
                // different hostnames
                { "http://test.com", "http://toast.com" },
                { "http://test.com:80", "test.com:87" }, // different ports
                { "http://test.com", "http://test.com:80" },
                // different paths
                { "http://test.com:91/dir1", "http://test.com:91/dir2" },
                // one null host
                { "http:/hostless", "http://hostfilled.com/hostless" },

                // queries
                { "http://test.com/dir?query", "http://test.com/dir?koory" },
                { "/test?query", "/test" },
                { "/test", "/test?query" },
                { "/test", "/test" },

                // fragments
                { "ftp://test.com/path?query#frag", "ftp://test.com/path?query" },
                { "ftp://test.com/path?query", "ftp://test.com/path?query#frag" },
                { "#frag", "#frag" }, { "p", "" },

                { "http://www.google.com", "#test" } // miscellaneous
        };

        int[] compareToResults = { 1, -1, 2, 0, 0, 0, 1, -1, 0, 32, -3, -3, 0,
                3, -4, -1, 1, 0, 1, 8, -10, -12, -81, -1, -1, 6, 1, -1, 0, 1,
                -1, 0, 1, 1, };

        // test compareTo functionality
        for (int i = 0; i < compareToResults.length; i++) {
            try {
                URI b = new URI(compareToData[i][0]);
                URI r = new URI(compareToData[i][1]);
                if (b.compareTo(r) != compareToResults[i]) {
                    fail("Test " + i + ": " + compareToData[i][0]
                            + " compared to " + compareToData[i][1] + " -> "
                            + b.compareTo(r) + " rather than "
                            + compareToResults[i]);
                }
            } catch (Exception e) {
                fail("Error in compareTo test of " + compareToData[i][0]
                        + " compared to " + compareToData[i][1] + ": " + e);
            }
        }
    }

    /**
     * @throws URISyntaxException
     * @tests java.net.URI#compareTo(java.lang.Object)
     */
    public void test_compareTo2() throws URISyntaxException {
        URI uri, uri2;

        // test URIs with host names with different casing
        uri = new URI("http://AbC.cOm/root/news");
        uri2 = new URI("http://aBc.CoM/root/news");
        assertEquals("TestA", 0, uri.compareTo(uri2));
        assertEquals("TestB", 0, uri.compareTo(uri2));

        // test URIs with one undefined component
        uri = new URI("http://abc.com:80/root/news");
        uri2 = new URI("http://abc.com/root/news");
        assertTrue("TestC", uri.compareTo(uri2) > 0);
        assertTrue("TestD", uri2.compareTo(uri) < 0);

        // test URIs with one undefined component
        uri = new URI("http://user@abc.com/root/news");
        uri2 = new URI("http://abc.com/root/news");
        assertTrue("TestE", uri.compareTo(uri2) > 0);
        assertTrue("TestF", uri2.compareTo(uri) < 0);
    }

    /**
     * @tests java.net.URI#create(java.lang.String)
     */
    public void test_createLjava_lang_String() {
        try {
            URI myUri = URI.create("a scheme://reg/");
            fail("IllegalArgumentException expected but not received.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * @tests java.net.URI#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        String[][] equalsData = new String[][] {
                { "", "" }, // null frags
                { "/path", "/path#frag" },
                { "#frag", "#frag2" },
                { "#frag", "#FRag" },

                // case insensitive on hex escapes
                { "#fr%4F", "#fr%4f" },

                { "scheme:test", "scheme2:test" }, // scheme stuff
                { "test", "http:test" },
                { "http:test", "test" },
                { "SCheme:test", "schEMe:test" },

                // hierarchical/opaque mismatch
                { "mailto:jim", "mailto://jim" },
                { "mailto://test", "mailto:test" },

                // opaque
                { "mailto:name", "mailto:name" },
                { "mailtO:john", "mailto:jim" },

                // test hex case insensitivity on ssp
                { "mailto:te%4Fst", "mailto:te%4fst" },

                { "mailto:john#frag", "mailto:john#frag2" },

                // hierarchical
                { "/test", "/test" }, // paths
                { "/te%F4st", "/te%f4st" },
                { "/TEst", "/teSt" },
                { "", "/test" },

                // registry based because they don't resolve properly to
                // server-based add more tests here
                { "//host.com:80err", "//host.com:80e" },
                { "//host.com:81e%Abrr", "//host.com:81e%abrr" },

                { "/test", "//auth.com/test" },
                { "//test.com", "/test" },

                { "//test.com", "//test.com" }, // hosts

                // case insensitivity for hosts
                { "//HoSt.coM/", "//hOsT.cOm/" },
                { "//te%ae.com", "//te%aE.com" },
                { "//test.com:80", "//test.com:81" },
                { "//joe@test.com:80", "//test.com:80" },
                { "//jo%3E@test.com:82", "//jo%3E@test.com:82" },
                { "//test@test.com:85", "//test@test.com" }, };

        boolean[] equalsResults = new boolean[] { true, false, false, false,
                true, false, false, false, true, false, false, true, false,
                true, false, true, true, false, false, false, true, false,
                false, true, true, true, false, false, true, false, };

        // test equals functionality
        for (int i = 0; i < equalsResults.length; i++) {
            try {
                URI b = new URI(equalsData[i][0]);
                URI r = new URI(equalsData[i][1]);
                if (b.equals(r) != equalsResults[i]) {
                    fail("Error: " + equalsData[i][0] + " == "
                            + equalsData[i][1] + "? -> " + b.equals(r)
                            + " expected " + equalsResults[i]);
                }
            } catch (Exception e) {
                fail("Exception during equals testing data " + equalsData[i][0]
                        + " == " + equalsData[i][1] + "? " + e);
            }
        }

    }

    /**
     * @throws URISyntaxException
     * @tests java.net.URI#equals(java.lang.Object)
     */
    public void test_equals2() throws URISyntaxException {
        // test URIs with empty string authority
        URI uri = new URI("http:///~/dictionary");
        URI uri2 = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(),
                uri.getQuery(), uri.getFragment());
        assertTrue(uri2.equals(uri));

        // test URIs with port number
        uri = new URI("http://abc.com%E2%82%AC:88/root/news");
        uri2 = new URI("http://abc.com%E2%82%AC/root/news");
        assertFalse(uri.equals(uri2));
        assertFalse(uri2.equals(uri));

        // test URIs with host names with different casing
        uri = new URI("http://AbC.cOm/root/news");
        uri2 = new URI("http://aBc.CoM/root/news");
        assertTrue(uri.equals(uri2));
        assertTrue(uri2.equals(uri));
    }

    /**
     * @tests java.net.URI#getAuthority()
     */
    public void test_getAuthority() throws Exception {
        URI[] uris = getUris();

        String[] getAuthorityResults = {
                "user` info@host",
                "user\u00DF\u00A3info@host:80", // =
                // "user\u00df\u00a3info@host:80",
                "user\u00DF\u00A3info@host:0", // =
                // "user\u00df\u00a3info@host:0",
                "user%60%20info@host:80",
                "user%C3%9F%C2%A3info@host",
                "user\u00DF\u00A3info@host:80", // =
                // "user\u00df\u00a3info@host:80",
                "user` info@host:81", "user%info@host:0", null, null, null,
                null, "server.org", "reg:istry", null, };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getAuthority();
            if (getAuthorityResults[i] != result
                    && !getAuthorityResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getAuthority() returned: " + result
                        + ", expected: " + getAuthorityResults[i]);
            }
        }
        // regression test for HARMONY-1119
        assertNull(new URI(null, null, null, 127, null, null, null)
                .getAuthority());
    }

    /**
     * @tests java.net.URI#getAuthority()
     */
    public void test_getAuthority2() throws Exception {
        // tests for URIs with empty string authority component

        URI uri = new URI("file:///tmp/");
        assertNull("Authority not null for URI: " + uri, uri.getAuthority());
        assertNull("Host not null for URI " + uri, uri.getHost());
        assertEquals("testA, toString() returned incorrect value",
                "file:///tmp/", uri.toString());

        uri = new URI("file", "", "/tmp", "frag");
        assertNull("Authority not null for URI: " + uri, uri.getAuthority());
        assertNull("Host not null for URI " + uri, uri.getHost());
        assertEquals("testB, toString() returned incorrect value",
                "file:///tmp#frag", uri.toString());

        uri = new URI("file", "", "/tmp", "query", "frag");
        assertNull("Authority not null for URI: " + uri, uri.getAuthority());
        assertNull("Host not null for URI " + uri, uri.getHost());
        assertEquals("test C, toString() returned incorrect value",
                "file:///tmp?query#frag", uri.toString());

        // after normalization the host string info may be lost since the
        // uri string is reconstructed
        uri = new URI("file", "", "/tmp/a/../b/c", "query", "frag");
        URI uri2 = uri.normalize();
        assertNull("Authority not null for URI: " + uri2, uri.getAuthority());
        assertNull("Host not null for URI " + uri2, uri.getHost());
        assertEquals("test D, toString() returned incorrect value",
                "file:///tmp/a/../b/c?query#frag", uri.toString());
        assertEquals("test E, toString() returned incorrect value",
                "file:/tmp/b/c?query#frag", uri2.toString());

        // the empty string host will give URISyntaxException
        // for the 7 arg constructor
        try {
            uri = new URI("file", "user", "", 80, "/path", "query", "frag");
            fail("Expected URISyntaxException");
        } catch (URISyntaxException e) {
            // Expected
        }
    }

    /**
     * @tests java.net.URI#getFragment()
     */
    public void test_getFragment() throws Exception {
        URI[] uris = getUris();

        String[] getFragmentResults = { "fr^ ag", "fr\u00E4\u00E8g", // =
                // "fr\u00e4\u00e8g",
                "fr\u00E4\u00E8g", // = "fr\u00e4\u00e8g",
                "fr%5E%20ag", "fr%C3%A4%C3%A8g", "fr\u00E4\u00E8g", // =
                // "fr\u00e4\u00e8g",
                "fr^ ag", "f%rag", null, "", null, "fragment", null, null, null };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getFragment();
            if (getFragmentResults[i] != result
                    && !getFragmentResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getFragment() returned: " + result
                        + ", expected: " + getFragmentResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getHost()
     */
    public void test_getHost() throws Exception {
        URI[] uris = getUris();

        String[] getHostResults = { "host", "host", "host", "host", "host",
                "host", "host", "host", null, null, null, null, "server.org",
                null, null };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getHost();
            if (getHostResults[i] != result
                    && !getHostResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getHost() returned: " + result + ", expected: "
                        + getHostResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getPath()
     */
    public void test_getPath() throws Exception {
        URI[] uris = getUris();

        String[] getPathResults = { "/a path",
                "/a\u20ACpath", // = "/a\u0080path",
                "/a\u20ACpath", // = "/a\u0080path",
                "/a%20path", "/a%E2%82%ACpath",
                "/a\u20ACpath", // = "/a\u0080path",
                "/a path", "/a%path", null, "../adirectory/file.html", null,
                "", "", "", "/c:/temp/calculate.pl" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getPath();
            if (getPathResults[i] != result
                    && !getPathResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getPath() returned: " + result + ", expected: "
                        + getPathResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getPort()
     */
    public void test_getPort() throws Exception {
        URI[] uris = getUris();

        int[] getPortResults = { -1, 80, 0, 80, -1, 80, 81, 0, -1, -1, -1, -1,
                -1, -1, -1 };

        for (int i = 0; i < uris.length; i++) {
            int result = uris[i].getPort();
            assertTrue("Error: For URI \"" + uris[i].toString()
                    + "\", getPort() returned: " + result + ", expected: "
                    + getPortResults[i], result == getPortResults[i]);
        }
    }

    /**
     * @tests java.net.URI#getPort()
     */
    public void test_getPort2() throws Exception {
        // if port value is negative, the authority should be
        // consider registry based.

        URI uri = new URI("http://myhost:-8096/site/index.html");
        assertEquals("TestA, returned wrong port value,", -1, uri.getPort());
        assertNull("TestA, returned wrong host value,", uri.getHost());
        try {
            uri.parseServerAuthority();
            fail("TestA, Expected URISyntaxException");
        } catch (URISyntaxException e) {
            // Expected
        }

        uri = new URI("http", "//myhost:-8096", null);
        assertEquals("TestB returned wrong port value,", -1, uri.getPort());
        assertNull("TestB returned wrong host value,", uri.getHost());
        try {
            uri.parseServerAuthority();
            fail("TestB, Expected URISyntaxException");
        } catch (URISyntaxException e) {
            // Expected
        }
    }

    /**
     * @tests java.net.URI#getQuery()
     */
    public void test_getQuery() throws Exception {
        URI[] uris = getUris();

        String[] getQueryResults = { "qu` ery", "qu\u00A9\u00AEery", // =
                // "qu\u00a9\u00aeery",
                "qu\u00A9\u00AEery", // = "qu\u00a9\u00aeery",
                "qu%60%20ery", "qu%C2%A9%C2%AEery", "qu\u00A9\u00AEery", // =
                // "qu\u00a9\u00aeery",
                "qu` ery", "que%ry", null, null, null, null, null, "query", "" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getQuery();
            if (getQueryResults[i] != result
                    && !getQueryResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getQuery() returned: " + result + ", expected: "
                        + getQueryResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getRawAuthority()
     */
    public void test_getRawAuthority() throws Exception {
        URI[] uris = getUris();

        String[] getRawAuthorityResults = {
                "user%60%20info@host",
                "user%C3%9F%C2%A3info@host:80",
                "user\u00DF\u00A3info@host:0", // =
                // "user\u00df\u00a3info@host:0",
                "user%2560%2520info@host:80",
                "user%25C3%259F%25C2%25A3info@host",
                "user\u00DF\u00A3info@host:80", // =
                // "user\u00df\u00a3info@host:80",
                "user%60%20info@host:81", "user%25info@host:0", null, null,
                null, null, "server.org", "reg:istry", null };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getRawAuthority();
            if (getRawAuthorityResults[i] != result
                    && !getRawAuthorityResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getRawAuthority() returned: " + result
                        + ", expected: " + getRawAuthorityResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getRawFragment()
     */
    public void test_getRawFragment() throws Exception {
        URI[] uris = getUris();

        String[] getRawFragmentResults = { "fr%5E%20ag",
                "fr%C3%A4%C3%A8g",
                "fr\u00E4\u00E8g", // = "fr\u00e4\u00e8g",
                "fr%255E%2520ag", "fr%25C3%25A4%25C3%25A8g",
                "fr\u00E4\u00E8g", // =
                // "fr\u00e4\u00e8g",
                "fr%5E%20ag", "f%25rag", null, "", null, "fragment", null,
                null, null };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getRawFragment();
            if (getRawFragmentResults[i] != result
                    && !getRawFragmentResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getRawFragment() returned: " + result
                        + ", expected: " + getRawFragmentResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getRawPath()
     */
    public void test_getRawPath() throws Exception {
        URI[] uris = getUris();

        String[] getRawPathResults = { "/a%20path",
                "/a%E2%82%ACpath",
                "/a\u20ACpath", // = "/a\u0080path",
                "/a%2520path", "/a%25E2%2582%25ACpath",
                "/a\u20ACpath", // =
                // "/a\u0080path",
                "/a%20path", "/a%25path", null, "../adirectory/file.html",
                null, "", "", "", "/c:/temp/calculate.pl" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getRawPath();
            if (getRawPathResults[i] != result
                    && !getRawPathResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getRawPath() returned: " + result
                        + ", expected: " + getRawPathResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getRawQuery()
     */
    public void test_getRawQuery() throws Exception {
        URI[] uris = getUris();

        String[] getRawQueryResults = {
                "qu%60%20ery",
                "qu%C2%A9%C2%AEery",
                "qu\u00A9\u00AEery", // = "qu\u00a9\u00aeery",
                "qu%2560%2520ery",
                "qu%25C2%25A9%25C2%25AEery",
                "qu\u00A9\u00AEery", // = "qu\u00a9\u00aeery",
                "qu%60%20ery", "que%25ry", null, null, null, null, null,
                "query", "" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getRawQuery();
            if (getRawQueryResults[i] != result
                    && !getRawQueryResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getRawQuery() returned: " + result
                        + ", expected: " + getRawQueryResults[i]);
            }
        }

    }

    /**
     * @tests java.net.URI#getRawSchemeSpecificPart()
     */
    public void test_getRawSchemeSpecificPart() throws Exception {
        URI[] uris = getUris();

        String[] getRawSspResults = {
                "//user%60%20info@host/a%20path?qu%60%20ery",
                "//user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery",
                "//user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery", // =
                // "//user\u00df\u00a3info@host:0/a\u0080path?qu\u00a9\u00aeery"
                "//user%2560%2520info@host:80/a%2520path?qu%2560%2520ery",
                "//user%25C3%259F%25C2%25A3info@host/a%25E2%2582%25ACpath?qu%25C2%25A9%25C2%25AEery",
                "//user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery", // =
                // "//user\u00df\u00a3info@host:80/a\u0080path?qu\u00a9\u00aeery"
                "//user%60%20info@host:81/a%20path?qu%60%20ery",
                "//user%25info@host:0/a%25path?que%25ry", "user@domain.com",
                "../adirectory/file.html", "comp.infosystems.www.servers.unix",
                "", "//server.org", "//reg:istry?query",
                "///c:/temp/calculate.pl?" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getRawSchemeSpecificPart();
            if (!getRawSspResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getRawSchemeSpecificPart() returned: " + result
                        + ", expected: " + getRawSspResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getRawUserInfo()
     */
    public void test_getRawUserInfo() throws URISyntaxException {
        URI[] uris = getUris();

        String[] getRawUserInfoResults = {
                "user%60%20info",
                "user%C3%9F%C2%A3info",
                "user\u00DF\u00A3info", // = "user\u00df\u00a3info",
                "user%2560%2520info",
                "user%25C3%259F%25C2%25A3info",
                "user\u00DF\u00A3info", // = "user\u00df\u00a3info",
                "user%60%20info", "user%25info", null, null, null, null, null,
                null, null };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getRawUserInfo();
            if (getRawUserInfoResults[i] != result
                    && !getRawUserInfoResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getRawUserInfo() returned: " + result
                        + ", expected: " + getRawUserInfoResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getScheme()
     */
    public void test_getScheme() throws Exception {
        URI[] uris = getUris();

        String[] getSchemeResults = { "http", "http", "ascheme", "http",
                "http", "ascheme", "http", "http", "mailto", null, "news",
                null, "telnet", "http", "file" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getScheme();
            if (getSchemeResults[i] != result
                    && !getSchemeResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getScheme() returned: " + result
                        + ", expected: " + getSchemeResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#getSchemeSpecificPart()
     */
    public void test_getSchemeSpecificPart() throws Exception {
        URI[] uris = getUris();

        String[] getSspResults = {
                "//user` info@host/a path?qu` ery",
                "//user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery", // =
                // "//user\u00df\u00a3info@host:80/a\u0080path?qu\u00a9\u00aeery",
                "//user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery", // =
                // "//user\u00df\u00a3info@host:0/a\u0080path?qu\u00a9\u00aeery",
                "//user%60%20info@host:80/a%20path?qu%60%20ery",
                "//user%C3%9F%C2%A3info@host/a%E2%82%ACpath?qu%C2%A9%C2%AEery",
                "//user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery", // =
                // "//user\u00df\u00a3info@host:80/a\u0080path?qu\u00a9\u00aeery",
                "//user` info@host:81/a path?qu` ery",
                "//user%info@host:0/a%path?que%ry", "user@domain.com",
                "../adirectory/file.html", "comp.infosystems.www.servers.unix",
                "", "//server.org", "//reg:istry?query",
                "///c:/temp/calculate.pl?" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getSchemeSpecificPart();
            if (!getSspResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getSchemeSpecificPart() returned: " + result
                        + ", expected: " + getSspResults[i]);
            }
        }

    }

    /**
     * @tests java.net.URI#getUserInfo()
     */
    public void test_getUserInfo() throws Exception {
        URI[] uris = getUris();

        String[] getUserInfoResults = {
                "user` info",
                "user\u00DF\u00A3info", // =
                // "user\u00df\u00a3info",
                "user\u00DF\u00A3info", // = "user\u00df\u00a3info",
                "user%60%20info",
                "user%C3%9F%C2%A3info",
                "user\u00DF\u00A3info", // = "user\u00df\u00a3info",
                "user` info", "user%info", null, null, null, null, null, null,
                null };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].getUserInfo();
            if (getUserInfoResults[i] != result
                    && !getUserInfoResults[i].equals(result)) {
                fail("Error: For URI \"" + uris[i].toString()
                        + "\", getUserInfo() returned: " + result
                        + ", expected: " + getUserInfoResults[i]);
            }
        }
    }

    /**
     * @tests java.net.URI#hashCode()
     */
    public void test_hashCode() throws Exception {
        String[][] hashCodeData = new String[][] {
                { "", "" }, // null frags
                { "/path", "/path#frag" },
                { "#frag", "#frag2" },
                { "#frag", "#FRag" },

                { "#fr%4F", "#fr%4F" }, // case insensitive on hex escapes

                { "scheme:test", "scheme2:test" }, // scheme
                { "test", "http:test" },
                { "http:test", "test" },

                // case insensitivity for scheme
                { "SCheme:test", "schEMe:test" },

                // hierarchical/opaque mismatch
                { "mailto:jim", "mailto://jim" },
                { "mailto://test", "mailto:test" },

                // opaque
                { "mailto:name", "mailto:name" },
                { "mailtO:john", "mailto:jim" },
                { "mailto:te%4Fst", "mailto:te%4Fst" },
                { "mailto:john#frag", "mailto:john#frag2" },

                // hierarchical
                { "/test/", "/test/" }, // paths
                { "/te%F4st", "/te%F4st" },
                { "/TEst", "/teSt" },
                { "", "/test" },

                // registry based because they don't resolve properly to
                // server-based
                // add more tests here
                { "//host.com:80err", "//host.com:80e" },
                { "//host.com:81e%Abrr", "//host.com:81e%Abrr" },
                { "//Host.com:80e", "//hoSt.com:80e" },

                { "/test", "//auth.com/test" },
                { "//test.com", "/test" },

                { "//test.com", "//test.com" }, // server based

                // case insensitivity for host
                { "//HoSt.coM/", "//hOsT.cOm/" },
                { "//te%aE.com", "//te%aE.com" },
                { "//test.com:80", "//test.com:81" },
                { "//joe@test.com:80", "//test.com:80" },
                { "//jo%3E@test.com:82", "//jo%3E@test.com:82" },
                { "//test@test.com:85", "//test@test.com" }, };

        boolean[] hashCodeResults = new boolean[] { true, false, false, false,
                true, false, false, false, true, false, false, true, false,
                true, false, true, true, false, false, false, true, false,
                false, false, true, true, true, false, false, true, false, };

        for (int i = 0; i < hashCodeResults.length; i++) {
            URI b = new URI(hashCodeData[i][0]);
            URI r = new URI(hashCodeData[i][1]);
            assertEquals("Error in hashcode equals results for" + b.toString()
                    + " " + r.toString(), hashCodeResults[i], b.hashCode() == r
                    .hashCode());
        }

    }

    /**
     * @tests java.net.URI#isAbsolute()
     */
    public void test_isAbsolute() throws URISyntaxException {
        String[] isAbsoluteData = new String[] { "mailto:user@ca.ibm.com",
                "urn:isbn:123498989h", "news:software.ibm.com",
                "http://www.amazon.ca", "file:///d:/temp/results.txt",
                "scheme:ssp", "calculate.pl?isbn=123498989h",
                "?isbn=123498989h", "//www.amazon.ca", "a.html", "#top",
                "//pc1/", "//user@host/path/file" };

        boolean results[] = new boolean[] { true, true, true, true, true, true,
                false, false, false, false, false, false, false };

        for (int i = 0; i < isAbsoluteData.length; i++) {
            boolean result = new URI(isAbsoluteData[i]).isAbsolute();
            assertEquals("new URI(" + isAbsoluteData[i] + ").isAbsolute()",
                    results[i], result);
        }
    }

    /**
     * @tests java.net.URI#isOpaque()
     */
    public void test_isOpaque() throws URISyntaxException {
        String[] isOpaqueData = new String[] { "mailto:user@ca.ibm.com",
                "urn:isbn:123498989h", "news:software.ibm.com",
                "http://www.amazon.ca", "file:///d:/temp/results.txt",
                "scheme:ssp", "calculate.pl?isbn=123498989h",
                "?isbn=123498989h", "//www.amazon.ca", "a.html", "#top",
                "//pc1/", "//user@host/path/file" };

        boolean results[] = new boolean[] { true, true, true, false, false,
                true, false, false, false, false, false, false, false };

        for (int i = 0; i < isOpaqueData.length; i++) {
            boolean result = new URI(isOpaqueData[i]).isOpaque();
            assertEquals("new URI(" + isOpaqueData[i] + ").isOpaque()",
                    results[i], result);
        }
    }

    /**
     * @tests java.net.URI#normalize()
     */
    public void test_normalize() throws Exception {

        String[] normalizeData = new String[] {
                // normal
                "/",
                "/a",
                "/a/b",
                "/a/b/c",
                // single, '.'
                "/.", "/./", "/./.", "/././",
                "/./a",
                "/./a/",
                "/././a",
                "/././a/",
                "/a/.",
                "/a/./",
                "/a/./.",
                "/a/./b",
                // double, '..'
                "/a/..", "/a/../", "/a/../b", "/a/../b/..", "/a/../b/../",
                "/a/../b/../c", "/..", "/../", "/../..", "/../../", "/../a",
                "/../a/", "/../../a", "/../../a/", "/a/b/../../c",
                "/a/b/../..",
                "/a/b/../../",
                "/a/b/../../c",
                "/a/b/c/../../../d",
                "/a/b/..",
                "/a/b/../",
                "/a/b/../c",
                // miscellaneous
                "/a/b/.././../../c/./d/../e",
                "/a/../../.c././../././c/d/../g/..",
                // '.' in the middle of segments
                "/a./b", "/.a/b", "/a.b/c", "/a/b../c",
                "/a/..b/c",
                "/a/b..c/d",
                // no leading slash, miscellaneous
                "", "a", "a/b", "a/b/c", "../", ".", "..", "../g",
                "g/a/../../b/c/./g", "a/b/.././../../c/./d/../e",
                "a/../../.c././../././c/d/../g/..", };

        String[] normalizeResults = new String[] { "/", "/a", "/a/b", "/a/b/c",
                "/", "/", "/", "/", "/a", "/a/", "/a", "/a/", "/a/", "/a/",
                "/a/", "/a/b", "/", "/", "/b", "/", "/", "/c", "/..", "/../",
                "/../..", "/../../", "/../a", "/../a/", "/../../a",
                "/../../a/", "/c", "/", "/", "/c", "/d", "/a/", "/a/", "/a/c",
                "/../c/e", "/../c/", "/a./b", "/.a/b", "/a.b/c", "/a/b../c",
                "/a/..b/c", "/a/b..c/d", "", "a", "a/b", "a/b/c", "../", "",
                "..", "../g", "b/c/g", "../c/e", "../c/", };

        for (int i = 0; i < normalizeData.length; i++) {
            URI test = new URI(normalizeData[i]);
            String result = test.normalize().toString();
            assertEquals("Normalized incorrectly, ", normalizeResults[i],
                    result.toString());
        }
    }

    /**
     * @tests java.net.URI#normalize()
     */
    public void test_normalize2() throws URISyntaxException {
        URI uri1 = null, uri2 = null;
        uri1 = new URI("file:/D:/one/two/../../three");
        uri2 = uri1.normalize();

        assertEquals("Normalized to incorrect URI", "file:/D:/three", uri2
                .toString());
        assertTrue("Resolved URI is not absolute", uri2.isAbsolute());
        assertFalse("Resolved URI is opaque", uri2.isOpaque());
        assertEquals("Resolved URI has incorrect scheme  specific part",
                "/D:/three", uri2.getRawSchemeSpecificPart());
    }

    /**
     * @tests java.net.URI#normalize()
     */
    public void test_normalize3() throws URISyntaxException {
        // return same URI if it has a normalized path already
        URI uri1 = null, uri2 = null;
        uri1 = new URI("http://host/D:/one/two/three");
        uri2 = uri1.normalize();
        assertSame("Failed to return same URI after normalization", uri1, uri2);

        // try with empty path
        uri1 = new URI("http", "host", null, "fragment");
        uri2 = uri1.normalize();
        assertSame("Failed to return same URI after normalization", uri1, uri2);
    }

    /**
     * @tests java.net.URI#parseServerAuthority()
     */
    public void test_parseServerAuthority() throws URISyntaxException {
        // registry based uris
        URI[] uris = null;
        uris = new URI[] {
                // port number not digits
                new URI("http://foo:bar/file#fragment"),
                new URI("http", "//foo:bar/file", "fragment"),

                // unicode char in the hostname = new
                // URI("http://host\u00dfname/")
                new URI("http://host\u00DFname/"),

                new URI("http", "//host\u00DFname/", null),
                // = new URI("http://host\u00dfname/", null),

                // escaped octets in host name
                new URI("http://host%20name/"),
                new URI("http", "//host%20name/", null),

                // missing host name, port number
                new URI("http://joe@:80"),

                // missing host name, no port number
                new URI("http://user@/file?query#fragment"),

                new URI("//host.com:80err"), // malformed port number
                new URI("//host.com:81e%Abrr"),

                // malformed ipv4 address
                new URI("telnet", "//256.197.221.200", null),

                new URI("telnet://198.256.221.200"),
                new URI("//te%ae.com"), // misc ..
                new URI("//:port"), new URI("//:80"),

                // last label has to start with alpha char
                new URI("//fgj234fkgj.jhj.123."),

                new URI("//fgj234fkgj.jhj.123"),

                // '-' cannot be first or last charcter in a label
                new URI("//-domain.name"), new URI("//domain.name-"),
                new URI("//domain-"),

                // illegal char in host name
                new URI("//doma*in"),

                // host expected
                new URI("http://:80/"), new URI("http://user@/"),

                // ipv6 address not enclosed in "[]"
                new URI("http://3ffe:2a00:100:7031:22:1:80:89/"),

                // expected ipv6 addresses to be enclosed in "[]"
                new URI("http", "34::56:78", "/path", "query", "fragment"),

                // expected host
                new URI("http", "user@", "/path", "query", "fragment") };
        // these URIs do not have valid server based authorities,
        // but single arg, 3 and 5 arg constructors
        // parse them as valid registry based authorities

        // exception should occur when parseServerAuthority is
        // requested on these uris
        for (int i = 0; i < uris.length; i++) {
            try {
                URI uri = uris[i].parseServerAuthority();
                fail("URISyntaxException expected but not received for URI: "
                        + uris[i].toString());
            } catch (URISyntaxException e) {
                // Expected
            }
        }

        // valid Server based authorities
        new URI("http", "3ffe:2a00:100:7031:2e:1:80:80", "/path", "fragment")
                .parseServerAuthority();
        new URI("http", "host:80", "/path", "query", "fragment")
                .parseServerAuthority();
        new URI("http://[::3abc:4abc]:80/").parseServerAuthority();
        new URI("http", "34::56:78", "/path", "fragment")
                .parseServerAuthority();
        new URI("http", "[34:56::78]:80", "/path", "fragment")
                .parseServerAuthority();

        // invalid authorities (neither server nor registry)
        try {
            URI uri = new URI("http://us[er@host:80/");
            fail("Expected URISyntaxException for URI " + uri.toString());
        } catch (URISyntaxException e) {
            // Expected
        }

        try {
            URI uri = new URI("http://[ddd::hgghh]/");
            fail("Expected URISyntaxException for URI " + uri.toString());
        } catch (URISyntaxException e) {
            // Expected
        }

        try {
            URI uri = new URI("http", "[3ffe:2a00:100:7031:2e:1:80:80]a:80",
                    "/path", "fragment");
            fail("Expected URISyntaxException for URI " + uri.toString());
        } catch (URISyntaxException e) {
            // Expected
        }

        try {
            URI uri = new URI("http", "host:80", "/path", "fragment");
            fail("Expected URISyntaxException for URI " + uri.toString());
        } catch (URISyntaxException e) {
            // Expected
        }
        
        // regression test for HARMONY-1126
        assertNotNull(URI.create("file://C:/1.txt").parseServerAuthority());
    }

    /**
     * @tests java.net.URI#relativize(java.net.URI)
     */
    public void test_relativizeLjava_net_URI() {
        // relativization tests
        String[][] relativizeData = new String[][] {
                // first is base, second is the one to relativize
                { "http://www.google.com/dir1/dir2", "mailto:test" }, // rel =
                // opaque
                { "mailto:test", "http://www.google.com" }, // base = opaque

                // different authority
                { "http://www.eclipse.org/dir1",
                        "http://www.google.com/dir1/dir2" },

                // different scheme
                { "http://www.google.com", "ftp://www.google.com" },

                { "http://www.google.com/dir1/dir2/",
                        "http://www.google.com/dir3/dir4/file.txt" },
                { "http://www.google.com/dir1/",
                        "http://www.google.com/dir1/dir2/file.txt" },
                { "./dir1/", "./dir1/hi" },
                { "/dir1/./dir2", "/dir1/./dir2/hi" },
                { "/dir1/dir2/..", "/dir1/dir2/../hi" },
                { "/dir1/dir2/..", "/dir1/dir2/hi" },
                { "/dir1/dir2/", "/dir1/dir3/../dir2/text" },
                { "//www.google.com", "//www.google.com/dir1/file" },
                { "/dir1", "/dir1/hi" }, { "/dir1/", "/dir1/hi" }, };

        // expected results
        String[] relativizeResults = new String[] { "mailto:test",
                "http://www.google.com", "http://www.google.com/dir1/dir2",
                "ftp://www.google.com",
                "http://www.google.com/dir3/dir4/file.txt", "dir2/file.txt",
                "hi", "hi", "hi", "dir2/hi", "text", "dir1/file", "hi", "hi", };

        for (int i = 0; i < relativizeData.length; i++) {
            try {
                URI b = new URI(relativizeData[i][0]);
                URI r = new URI(relativizeData[i][1]);
                if (!b.relativize(r).toString().equals(relativizeResults[i])) {
                    fail("Error: relativize, " + relativizeData[i][0] + ", "
                            + relativizeData[i][1] + " returned: "
                            + b.relativize(r) + ", expected:"
                            + relativizeResults[i]);
                }
            } catch (URISyntaxException e) {
                fail("Exception on relativize test on data "
                        + relativizeData[i][0] + ", " + relativizeData[i][1]
                        + ": " + e);
            }
        }
    }

    /**
     * @tests java.net.URI#relativize(java.net.URI)
     */
    public void test_relativize2() throws Exception {
        URI a = new URI("http://host/dir");
        URI b = new URI("http://host/dir/file?query");
        assertEquals("relativized incorrectly,", new URI("file?query"), a
                .relativize(b));

        // one URI with empty host
        a = new URI("file:///~/dictionary");
        b = new URI("file://tools/~/dictionary");
        assertEquals("relativized incorrectly,", new URI(
                "file://tools/~/dictionary"), a.relativize(b));
        assertEquals("relativized incorrectly,",
                new URI("file:///~/dictionary"), b.relativize(a));

        // two URIs with empty hosts
        b = new URI("file:///~/therasus");
        assertEquals("relativized incorrectly,", new URI("file:///~/therasus"),
                a.relativize(b));
        assertEquals("relativized incorrectly,",
                new URI("file:///~/dictionary"), b.relativize(a));
    }

    /**
     * @tests java.net.URI#resolve(java.net.URI)
     */
    public void test_resolve() throws URISyntaxException {
        URI uri1 = null, uri2 = null;
        uri1 = new URI("file:/D:/one/two/three");
        uri2 = uri1.resolve(new URI(".."));

        assertEquals("Resolved to incorrect URI", "file:/D:/one/", uri2
                .toString());
        assertTrue("Resolved URI is not absolute", uri2.isAbsolute());
        assertFalse("Resolved URI is opaque", uri2.isOpaque());
        assertEquals("Resolved URI has incorrect scheme  specific part",
                "/D:/one/", uri2.getRawSchemeSpecificPart());
    }

    /**
     * @tests java.net.URI#resolve(java.net.URI)
     */
    public void test_resolveLjava_net_URI() {
        // resolution tests
        String[][] resolveData = new String[][] {
                // authority in given URI
                { "http://www.test.com/dir",
                        "//www.test.com/hello?query#fragment" },
                // no authority, absolute path
                { "http://www.test.com/dir", "/abspath/file.txt" },
                // no authority, relative paths
                { "/", "dir1/file.txt" }, { "/dir1", "dir2/file.txt" },
                { "/dir1/", "dir2/file.txt" }, { "", "dir1/file.txt" },
                { "dir1", "dir2/file.txt" }, { "dir1/", "dir2/file.txt" },
                // normalization required
                { "/dir1/dir2/../dir3/./", "dir4/./file.txt" },
                // allow a standalone fragment to be resolved
                { "http://www.google.com/hey/joe?query#fragment", "#frag2" },
                // return given when base is opaque
                { "mailto:idontexist@uk.ibm.com", "dir1/dir2" },
                // return given when given is absolute
                { "http://www.google.com/hi/joe", "http://www.oogle.com" }, };

        // expected results
        String[] resolveResults = new String[] {
                "http://www.test.com/hello?query#fragment",
                "http://www.test.com/abspath/file.txt", "/dir1/file.txt",
                "/dir2/file.txt", "/dir1/dir2/file.txt", "dir1/file.txt",
                "dir2/file.txt", "dir1/dir2/file.txt",
                "/dir1/dir3/dir4/file.txt",
                "http://www.google.com/hey/joe?query#frag2", "dir1/dir2",
                "http://www.oogle.com", };

        for (int i = 0; i < resolveResults.length; i++) {
            try {
                URI b = new URI(resolveData[i][0]);
                URI r = new URI(resolveData[i][1]);
                URI result = b.resolve(r);
                if (!result.toString().equals(resolveResults[i])) {
                    fail("Error: resolve, " + resolveData[i][0] + ", "
                            + resolveData[i][1] + " returned: " + b.resolve(r)
                            + ", expected:" + resolveResults[i]);
                }
                if (!b.isOpaque()) {
                    assertEquals(b + " and " + result
                            + " incorrectly differ in absoluteness", b
                            .isAbsolute(), result.isAbsolute());
                }
            } catch (URISyntaxException e) {
                fail("Exception on resolve test on data " + resolveData[i][0]
                        + ", " + resolveData[i][1] + ": " + e);
            }
        }
    }

    /**
     * @tests java.net.URI#toASCIIString()
     */
    public void test_toASCIIString() throws Exception {
        URI[] uris = getUris();

        String[] toASCIIStringResults0 = new String[] {
                "http://user%60%20info@host/a%20path?qu%60%20ery#fr%5E%20ag",
                "http://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
                "ascheme://user%C3%9F%C2%A3info@host:0/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
                "http://user%2560%2520info@host:80/a%2520path?qu%2560%2520ery#fr%255E%2520ag",
                "http://user%25C3%259F%25C2%25A3info@host/a%25E2%2582%25ACpath?qu%25C2%25A9%25C2%25AEery#fr%25C3%25A4%25C3%25A8g",
                "ascheme://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
                "http://user%60%20info@host:81/a%20path?qu%60%20ery#fr%5E%20ag",
                "http://user%25info@host:0/a%25path?que%25ry#f%25rag",
                "mailto:user@domain.com", "../adirectory/file.html#",
                "news:comp.infosystems.www.servers.unix", "#fragment",
                "telnet://server.org", "http://reg:istry?query",
                "file:///c:/temp/calculate.pl?" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].toASCIIString();
            assertTrue("Error: For URI \"" + uris[i].toString()
                    + "\", toASCIIString() returned: " + result
                    + ", expected: " + toASCIIStringResults0[i], result
                    .equals(toASCIIStringResults0[i]));
        }

        String[] toASCIIStringData = new String[] {
                "http://www.test.com/\u00DF/dir/",
                "http://www.test.com/\u20AC/dir", "http://www.\u20AC.com/dir",
                "http://www.test.com/\u20AC/dir/file#fragment",
                "mailto://user@domain.com", "mailto://user\u00DF@domain.com", };

        String[] toASCIIStringResults = new String[] {
                "http://www.test.com/%C3%9F/dir/",
                "http://www.test.com/%E2%82%AC/dir",
                "http://www.%E2%82%AC.com/dir",
                "http://www.test.com/%E2%82%AC/dir/file#fragment",
                "mailto://user@domain.com", "mailto://user%C3%9F@domain.com", };

        for (int i = 0; i < toASCIIStringData.length; i++) {
            URI test = new URI(toASCIIStringData[i]);
            String result = test.toASCIIString();
            assertTrue("Error: new URI(\"" + toASCIIStringData[i]
                    + "\").toASCIIString() returned: " + result
                    + ", expected: " + toASCIIStringResults[i], result
                    .equals(toASCIIStringResults[i]));
        }
    }

    /**
     * @tests java.net.URI#toString()
     */
    public void test_toString() throws Exception {
        URI[] uris = getUris();

        String[] toStringResults = {
                "http://user%60%20info@host/a%20path?qu%60%20ery#fr%5E%20ag",
                "http://user%C3%9F%C2%A3info@host:80/a%E2%82%ACpath?qu%C2%A9%C2%AEery#fr%C3%A4%C3%A8g",
                "ascheme://user\u00DF\u00A3info@host:0/a\u20ACpath?qu\u00A9\u00AEery#fr\u00E4\u00E8g",
                // =
                // "ascheme://user\u00df\u00a3info@host:0/a\u0080path?qu\u00a9\u00aeery#fr\u00e4\u00e8g",
                "http://user%2560%2520info@host:80/a%2520path?qu%2560%2520ery#fr%255E%2520ag",
                "http://user%25C3%259F%25C2%25A3info@host/a%25E2%2582%25ACpath?qu%25C2%25A9%25C2%25AEery#fr%25C3%25A4%25C3%25A8g",
                "ascheme://user\u00DF\u00A3info@host:80/a\u20ACpath?qu\u00A9\u00AEery#fr\u00E4\u00E8g",
                // =
                // "ascheme://user\u00df\u00a3info@host:80/a\u0080path?qu\u00a9\u00aeery#fr\u00e4\u00e8g",
                "http://user%60%20info@host:81/a%20path?qu%60%20ery#fr%5E%20ag",
                "http://user%25info@host:0/a%25path?que%25ry#f%25rag",
                "mailto:user@domain.com", "../adirectory/file.html#",
                "news:comp.infosystems.www.servers.unix", "#fragment",
                "telnet://server.org", "http://reg:istry?query",
                "file:///c:/temp/calculate.pl?" };

        for (int i = 0; i < uris.length; i++) {
            String result = uris[i].toString();
            assertTrue("Error: For URI \"" + uris[i].toString()
                    + "\", toString() returned: " + result + ", expected: "
                    + toStringResults[i], result.equals(toStringResults[i]));
        }
    }

    /**
     * @tests java.net.URI#toURL()
     */
    public void test_toURL() throws Exception {
        String absoluteuris[] = new String[] { "mailto:user@ca.ibm.com",
                "urn:isbn:123498989h", "news:software.ibm.com",
                "http://www.amazon.ca", "file:///d:/temp/results.txt",
                "scheme:ssp", };

        String relativeuris[] = new String[] { "calculate.pl?isbn=123498989h",
                "?isbn=123498989h", "//www.amazon.ca", "a.html", "#top",
                "//pc1/", "//user@host/path/file" };

        for (int i = 0; i < absoluteuris.length; i++) {
            try {
                new URI(absoluteuris[i]).toURL();
            } catch (MalformedURLException e) {
                // not all the URIs can be translated into valid URLs
            }
        }

        for (int i = 0; i < relativeuris.length; i++) {
            try {
                new URI(relativeuris[i]).toURL();
                fail("Expected IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }
    }
}

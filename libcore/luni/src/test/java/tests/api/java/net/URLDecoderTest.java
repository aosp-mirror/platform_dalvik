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

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import tests.support.Support_Configuration;

@TestTargetClass(URLDecoder.class) 
public class URLDecoderTest extends junit.framework.TestCase {

    /**
     * @tests java.net.URLDecoder#URLDecoder()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "URLDecoder",
        args = {}
    )
    public void test_Constructor() throws Exception {
        URLDecoder ud = new URLDecoder();
        assertNotNull("Constructor failed.", ud);
    }

    /**
     * @tests java.net.URLDecoder#decode(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "decode",
        args = {java.lang.String.class}
    )
    public void test_decodeLjava_lang_String() throws Exception {
        // Test for method java.lang.String
        // java.net.URLDecoder.decode(java.lang.String)
        final String URL = "http://" + Support_Configuration.HomeAddress;
        final String URL2 = "telnet://justWantToHaveFun.com:400";
        final String URL3 = "file://myServer.org/a file with spaces.jpg";
        assertTrue("1. Incorrect encoding/decoding", URLDecoder.decode(
                URLEncoder.encode(URL)).equals(URL));
        assertTrue("2. Incorrect encoding/decoding", URLDecoder.decode(
                URLEncoder.encode(URL2)).equals(URL2));
        assertTrue("3. Incorrect encoding/decoding", URLDecoder.decode(
                URLEncoder.encode(URL3)).equals(URL3));
    }

    /**
     * @tests java.net.URLDecoder#decode(java.lang.String, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "decode",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_decodeLjava_lang_String_Ljava_lang_String() {
        // Regression for HARMONY-467

        String enc = "UTF-8";
        
        String [] urls = { "http://" + Support_Configuration.HomeAddress + 
                           "/test?hl=en&q=te+st", 
                           "file://a+b/c/d.e-f*g_+l",
                           "jar:file://a.jar+!/b.c/",
                           "ftp://test:pwd@localhost:2121/%D0%9C",
                           "%D0%A2%D0%B5%D1%81%D1%82+URL+for+test"}; 
        
        String [] expected = {"http://" + Support_Configuration.HomeAddress + 
                              "/test?hl=en&q=te st",
                              "file://a b/c/d.e-f*g_ l",
                              "jar:file://a.jar !/b.c/"};

        for(int i = 0; i < urls.length - 2; i++) {
            try {
                assertEquals(expected[i], URLDecoder.decode(urls[i], enc));
            } catch (UnsupportedEncodingException e) {
                fail("UnsupportedEncodingException: " + e.getMessage());
            }
        }
        
        try {
            URLDecoder.decode(urls[urls.length - 2], enc);
            URLDecoder.decode(urls[urls.length - 1], enc);
        } catch (UnsupportedEncodingException e) {
            fail("UnsupportedEncodingException: " + e.getMessage());
        }
        
        try {
            URLDecoder.decode("", "");
            fail("UnsupportedEncodingException expected");
        } catch (UnsupportedEncodingException e) {
            //expected
        }
    }
}

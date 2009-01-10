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
import java.net.URLDecoder;
import java.net.URLEncoder;

import tests.support.Support_Configuration;

@TestTargetClass(URLEncoder.class) 
public class URLEncoderTest extends junit.framework.TestCase {

    /**
     * @tests java.net.URLEncoder#encode(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "encode",
        args = {java.lang.String.class}
    )
    public void test_encodeLjava_lang_String() {
        // Test for method java.lang.String
        // java.net.URLEncoder.encode(java.lang.String)
        final String URL = "http://" + Support_Configuration.HomeAddress;
        final String URL2 = "telnet://justWantToHaveFun.com:400";
        final String URL3 = "file://myServer.org/a file with spaces.jpg";
        try {
            assertTrue("1. Incorrect encoding/decoding", URLDecoder.decode(
                    URLEncoder.encode(URL)).equals(URL));
            assertTrue("2. Incorrect encoding/decoding", URLDecoder.decode(
                    URLEncoder.encode(URL2)).equals(URL2));
            assertTrue("3. Incorrect encoding/decoding", URLDecoder.decode(
                    URLEncoder.encode(URL3)).equals(URL3));
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "encode",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_encodeLjava_lang_StringLjava_lang_String() {
       
        String enc = "UTF-8";
        
        String [] urls = {"http://" + Support_Configuration.HomeAddress + 
                              "/test?hl=en&q=te st",
                              "file://a b/c/d.e-f*g_ l",
                              "jar:file://a.jar !/b.c/\u1052",
                              "ftp://test:pwd@localhost:2121/%D0%9C"};
        
        String [] expected = { "http%3A%2F%2Fjcltest.apache.org%2Ftest%3Fhl%" +
                "3Den%26q%3Dte+st", 
                "file%3A%2F%2Fa+b%2Fc%2Fd.e-f*g_+l",
                "jar%3Afile%3A%2F%2Fa.jar+%21%2Fb.c%2F%E1%81%92"};        

        for(int i = 0; i < urls.length-1; i++) {
            try {
                String encodedString = URLEncoder.encode(urls[i], enc);
                assertEquals(expected[i], encodedString);
                assertEquals(urls[i], URLDecoder.decode(encodedString, enc));
            } catch (UnsupportedEncodingException e) {
                fail("UnsupportedEncodingException: " + e.getMessage());
            }
        }
        
        try {
            String encodedString = URLEncoder.encode(urls[urls.length - 1], enc);
            assertEquals(urls[urls.length - 1], URLDecoder.decode(encodedString, enc));
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

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}

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

package org.apache.harmony.luni.tests.java.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

public class URLConnectionTest extends TestCase {

    /**
     * @tests java.net.URLConnection#addRequestProperty(String, String)
     */
    public void test_addRequestProperty() throws MalformedURLException,
            IOException {

        MockURLConnection u = new MockURLConnection(new URL(
                "http://www.apache.org"));
        try {
            // Regression for HARMONY-604
            u.addRequestProperty(null, "someValue");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        u.connect();
        try {
            // state of connection is checked first
            // so no NPE in case of null 'field' param
            u.addRequestProperty(null, "someValue");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * @tests java.net.URLConnection#setRequestProperty(String, String)
     */
    public void test_setRequestProperty() throws MalformedURLException,
            IOException {

        MockURLConnection u = new MockURLConnection(new URL(
                "http://www.apache.org"));
        try {
            u.setRequestProperty(null, "someValue");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        u.connect();
        try {
            // state of connection is checked first
            // so no NPE in case of null 'field' param
            u.setRequestProperty(null, "someValue");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * @tests java.net.URLConnection#setUseCaches(boolean)
     */
    public void test_setUseCachesZ() throws MalformedURLException, IOException {

        // Regression for HARMONY-71
        MockURLConnection u = new MockURLConnection(new URL(
                "http://www.apache.org"));
        u.connect();
        try {
            u.setUseCaches(true);
            fail("Assert 0: expected an IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * @tests java.net.URLConnection#setAllowUserInteraction(boolean)
     */
    public void test_setAllowUserInteractionZ() throws MalformedURLException,
            IOException {

        // Regression for HARMONY-72
        MockURLConnection u = new MockURLConnection(new URL(
                "http://www.apache.org"));
        u.connect();
        try {
            u.setAllowUserInteraction(false);
            fail("Assert 0: expected an IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    static class MockURLConnection extends URLConnection {

        public MockURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() {
            connected = true;
        }
    }
}
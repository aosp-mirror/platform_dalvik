/*
 * Copyright (C) 2009 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tests.support.Support_PortManager;
import tests.support.Support_TestWebServer;

import junit.framework.Test;
import junit.framework.TestSuite;

public class URLConnectionTest extends junit.framework.TestCase {
    private int port;
    private Support_TestWebServer server;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        port = Support_PortManager.getNextPort();
        server = new Support_TestWebServer();
        server.initServer(port, false);
    }
    
    @Override
    public void tearDown()throws Exception {
        super.tearDown();
        server.close();
    }
    
    private String readFirstLine() throws Exception {
        URLConnection connection = new URL("http://localhost:" + port + "/test1").openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result = in.readLine();
        in.close();
        return result;
    }
    
    // Check that if we don't read to the end of a response, the next request on the
    // recycled connection doesn't get the unread tail of the first request's response.
    // http://code.google.com/p/android/issues/detail?id=2939
    public void test_2939() throws Exception {
        server.setChunked(true);
        server.setMaxChunkSize(8);
        assertTrue(readFirstLine().equals("<html>"));
        assertTrue(readFirstLine().equals("<html>"));
    }
}

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

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

public class URITest extends TestCase {
    /**
     * @tests java.net.URI(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws URISyntaxException {
        // Regression test for HARMONY-23
        try {
            new URI("%3");
            fail("Assert 0: URI constructor failed to throw exception on invalid input.");
        } catch (URISyntaxException e) {
            // Expected
            assertEquals("Assert 1: Wrong index in URISyntaxException.", 0, e.getIndex());
        }
        
        // Regression test for HARMONY-25
        // if port value is negative, the authority should be considered registry-based.
        URI uri = new URI("http://host:-8096/path/index.html");
        assertEquals("Assert 2: returned wrong port value,", -1, uri.getPort());
        assertNull("Assert 3: returned wrong host value,", uri.getHost());
        try {
            uri.parseServerAuthority();
            fail("Assert 4: Expected URISyntaxException");
        } catch (URISyntaxException e){
            // Expected
        }
        
        uri = new URI("http","//myhost:-8096", null);
        assertEquals("Assert 5: returned wrong port value,", -1, uri.getPort());
        assertNull("Assert 6: returned wrong host value,", uri.getHost());
        try {
            uri.parseServerAuthority();
            fail("Assert 7: Expected URISyntaxException");
        } catch (URISyntaxException e){
            // Expected
        }
    }
    
    /**
     * @tests java.net.URI(java.lang.String, java.lang.String, java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_String() {
        // scheme can not be empty string    
        try {
            new URI("","//authority/path", "fragment");
            fail ("Assert 0: Expected URISyntaxException with empty URI scheme");    
        } catch(URISyntaxException e) {
            // Expected
            assertEquals("Assert 1: Wrong index in URISyntaxException.", 0, e.getIndex());
        }
    }
    
    /**
     * @tests java.net.URI#relativize(java.net.URI)
     */
    public void test_relativizeLjava_net_URI() throws URISyntaxException{
        URI a = new URI("http://host/dir");
        URI b = new URI("http://host/dir/file?query");        
        assertEquals("Assert 0: URI relativized incorrectly,",
                new URI("file?query"), a.relativize(b));        
    
        // One URI with empty host
        a = new URI("file:///~/first");
        b = new URI("file://tools/~/first");
        assertEquals("Assert 1: URI relativized incorrectly,",
                new URI("file://tools/~/first"), a.relativize(b));        
        assertEquals("Assert 2: URI relativized incorrectly,",
                new URI("file:///~/first"), b.relativize(a));        

        // Both URIs with empty hosts
        b = new URI("file:///~/second");
        assertEquals("Assert 3: URI relativized incorrectly,",
                new URI("file:///~/second"), a.relativize(b));
        assertEquals("Assert 4: URI relativized incorrectly,",
                new URI("file:///~/first"), b.relativize(a));
    }
    
    public void test_relativizeBasedOneEclipseCoreResources() throws URISyntaxException {
        URI one = new URI("file:/C:/test/ws");
        URI two = new URI("file:/C:/test/ws");
        
        URI empty = new URI("");
        assertEquals(empty, one.relativize(two));
        
        one = new URI("file:/C:/test/ws");
        two = new URI("file:/C:/test/ws/p1");
        URI result = new URI("p1");
        assertEquals(result, one.relativize(two));
        
        one = new URI("file:/C:/test/ws/");
        assertEquals(result, one.relativize(two));
    }
    
    /**
     * @tests java.net.URI#compareTo(java.net.URI)
     */
    public void test_compareToLjava_net_URI() throws URISyntaxException{
        URI uri1, uri2;

        // URIs whose host names have different casing
        uri1 = new URI("http://MixedCaseHost/path/resource");
        uri2 = new URI("http://mixedcasehost/path/resource");
        assertEquals("Assert 0: host name equality failure", 0, uri1.compareTo(uri2));
        assertEquals("Assert 1: host name equality failure", 0, uri1.compareTo(uri2));

        // URIs with one undefined component (port)
        uri1 = new URI("http://anyhost:80/path/resource");
        uri2 = new URI("http://anyhost/path/resource");
        assertTrue("Assert 2: comparison failure", uri1.compareTo(uri2) > 0);
        assertTrue("Assert 3: comparison failure", uri2.compareTo(uri1) < 0);
        
        // URIs with one undefined component (user-info)
        uri1 = new URI("http://user-info@anyhost/path/resource");
        uri2 = new URI("http://anyhost/path/resource");
        assertTrue("Assert 4: comparison failure", uri1.compareTo(uri2) > 0);
        assertTrue("Assert 5: comparison failure", uri2.compareTo(uri1) < 0);
    }
}

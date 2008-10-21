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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import junit.framework.TestCase;

public class URLEncoderTest extends TestCase {
    
    /**
     * @tests URLEncoder#encode(String, String)
     */
    public void test_encodeLjava_lang_StringLjava_lang_String() throws Exception {
        // Regression for HARMONY-24
        try {
            URLEncoder.encode("str","unknown_enc");
            fail("Assert 0: Should throw UEE for invalid encoding");
        } catch (UnsupportedEncodingException e) {
            // expected
        } 
        //Regression for HARMONY-1233
        try {
            URLEncoder.encode(null, "harmony");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }
}

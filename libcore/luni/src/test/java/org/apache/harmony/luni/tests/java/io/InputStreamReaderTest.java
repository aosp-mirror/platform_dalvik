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

package org.apache.harmony.luni.tests.java.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class InputStreamReaderTest extends TestCase {
    public void testGetEncoding_StreamClosed() throws IOException {
        InputStreamReader in = null;
        byte b[] = new byte[5];
        in = new InputStreamReader(new ByteArrayInputStream(b), "UTF-16BE");
        in.close();
        String result = in.getEncoding();
        assertNull(result);
    }

    /**
     * @tests java.io.InputStreamReader#read()
     */


    public void testGetEncoding_NotHistorical() {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(System.in, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            // ok
        }
        String result = in.getEncoding();
        assertEquals("UnicodeBigUnmarked", result);

    }

}

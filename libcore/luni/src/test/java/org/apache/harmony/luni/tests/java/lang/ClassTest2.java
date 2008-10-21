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

package org.apache.harmony.luni.tests.java.lang;

import java.io.IOException;
import java.io.InputStream;

public class ClassTest2 extends junit.framework.TestCase {

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

    /**
     *  Tests loading a resource with a relative name.
     */
    public void testGetResourceAsStream1() throws IOException {
        Class clazz = getClass();
        
        InputStream stream = clazz.getResourceAsStream("HelloWorld.txt");
        assert(stream != null);
        
        byte[] buffer = new byte[20];
        int length = stream.read(buffer);
        String s = new String(buffer, 0, length);
        assert("Hello, World.".equals(s));

        stream.close();
    }
    
    /**
     *  Tests loading a resource with a global name.
     */
    public void testGetResourceAsStream2() throws IOException {
        Class clazz = getClass();
        
        InputStream stream = clazz.getResourceAsStream("/org/apache/harmony/luni/tests/java/lang/HelloWorld.txt");
        assert(stream != null);
        
        byte[] buffer = new byte[20];
        int length = stream.read(buffer);
        String s = new String(buffer, 0, length);
        assert("Hello, World.".equals(s));

        stream.close();
    }
}

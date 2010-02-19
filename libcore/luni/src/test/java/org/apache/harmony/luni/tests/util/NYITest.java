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

package org.apache.harmony.luni.tests.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.harmony.luni.util.NotImplementedException;

/**
 * Testing the NYI framework code.
 */
public class NYITest extends TestCase {

    public void testNYI() throws UnsupportedEncodingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(400);
        PrintStream stream = new PrintStream(bos, true, "UTF-8");
        new NotImplementedException(stream);
        String message = new String(bos.toByteArray(), "UTF-8");
        assertFalse(message.indexOf("NYITest") == -1);
    }
}

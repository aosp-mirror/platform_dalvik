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

import org.apache.harmony.luni.util.Base64;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Base64 encoder/decoder test.
 */
public class Base64Test extends TestCase {

    /**
     * Checks the result on empty parameter.
     */
    public static void testDecodeEmpty() throws Exception {
        // Regression for HARMONY-1513
        byte[] result = Base64.decode(new byte[0]);
        assertEquals("The length of the result differs from expected",
                0, result.length);
    }

    public static Test suite() {
        return new TestSuite(Base64Test.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}


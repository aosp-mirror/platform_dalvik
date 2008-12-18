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

package tests.api.java.io;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestLevel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

@TestTargetClass(UnsupportedEncodingException.class) 
public class UnsupportedEncodingExceptionTest extends junit.framework.TestCase {

    /**
     * @tests java.io.UnsupportedEncodingException#UnsupportedEncodingException()
     */
    @TestInfo(
              level = TestLevel.PARTIAL,
              purpose = "Test does not checks constructor functionality (doing this indirectly and which constructor used non obvious)",
              targets = {
                @TestTarget(
                  methodName = "UnsupportedEncodingException",
                  methodArgs = {}
                )
            })
    public void test_Constructor() {
        // Test for method java.io.UnsupportedEncodingException()
        try {
            new OutputStreamWriter(new ByteArrayOutputStream(), "BogusEncoding");
        } catch (UnsupportedEncodingException e) {
            return;
        } catch (Exception e) {
            fail("Exception during UnsupportedEncodingException test"
                    + e.toString());
        }
        fail("Failed to generate expected exception");
    }

    /**
     * @tests java.io.UnsupportedEncodingException#UnsupportedEncodingException(java.lang.String)
     */
    @TestInfo(
              level = TestLevel.PARTIAL,
              purpose = "Test does not checks constructor functionality (doing this indirectly and which constructor used non obvious)",
              targets = {
                @TestTarget(
                  methodName = "UnsupportedEncodingException",
                  methodArgs = {java.lang.String.class}
                )
            })
    public void test_ConstructorLjava_lang_String() {
        // Test for method
        // java.io.UnsupportedEncodingException(java.lang.String)
        try {
            new OutputStreamWriter(new ByteArrayOutputStream(), "BogusEncoding");
        } catch (UnsupportedEncodingException e) {
            return;
        } catch (Exception e) {
            fail("Exception during UnsupportedEncodingException test"
                    + e.toString());
        }
        fail("Failed to generate expected exception");
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

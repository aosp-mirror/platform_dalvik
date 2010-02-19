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
package org.apache.harmony.luni.tests.java.util;

import java.io.NotSerializableException;
import java.util.InvalidPropertiesFormatException;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class InvalidPropertiesFormatExceptionTest extends
        junit.framework.TestCase {

    /**
     * @tests java.util.InvalidPropertiesFormatException#SerializationTest()
     */
    public void test_Serialization() throws Exception {
        InvalidPropertiesFormatException ipfe = new InvalidPropertiesFormatException(
                "Hey, this is InvalidPropertiesFormatException");
        try {
            SerializationTest.verifySelf(ipfe);
        } catch (NotSerializableException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.util.InvalidPropertiesFormatException#InvalidPropertiesFormatException(Throwable)}
     */
    public void test_Constructor_Ljava_lang_Throwable() {
        Throwable throwable = new Throwable();
        InvalidPropertiesFormatException exception = new InvalidPropertiesFormatException(
                throwable);
        assertEquals("the casue did not equals argument passed in constructor",
                throwable, exception.getCause());
    }

}

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

package org.apache.harmony.nio.tests.java.nio;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(java.nio.ByteBuffer.class)
public class ReadOnlyWrappedByteBufferTest extends WrappedByteBufferTest {

    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies isReadOnly method for read only wrapped " +
            "ByteBuffer.",
      targets = {
        @TestTarget(
          methodName = "isReadOnly",
          methodArgs = {}
        )
    })
    public void testIsReadOnly() {
        assertTrue(buf.isReadOnly());
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies hasArray method for read only wrapped ByteBuffer.",
      targets = {
        @TestTarget(
          methodName = "hasArray",
          methodArgs = {}
        )
    })
    public void testHasArray() {
        assertFalse(buf.hasArray());
    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public void testHashCode() {
        super.readOnlyHashCode();
    }


}

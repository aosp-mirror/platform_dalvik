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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;
@TestTargetClass(FileOutputStream.class)
public class FileOutputStreamTest extends TestCase {

    /**
     * @tests java.io.FileOutputStream#write(byte[], int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Checks NullPointerException",
      targets = {
        @TestTarget(
          methodName = "write",
          methodArgs = {byte[].class, int.class, int.class}
        )
    })
    public void test_write$BII() throws Exception {
        // Regression test for HARMONY-285
        File file = new File("FileOutputStream.tmp");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(null, 0, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}

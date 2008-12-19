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

package tests.api.java.io;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

/**
 * TODO Type description
 */
@TestTargetClass(RandomAccessFile.class) 
public class OpenRandomFileTest extends TestCase {

    public static void main(String[] args) {
        new OpenRandomFileTest().testOpenEmptyFile();
    }

    public OpenRandomFileTest() {
        super();
    }
    @TestInfo(
              level = TestLevel.PARTIAL,
              purpose = "Exceptions checking missed.",
              targets = {
                @TestTarget(
                  methodName = "RandomAccessFile",
                  methodArgs = {java.lang.String.class, java.lang.String.class}
                )
            })
    public void testOpenNonEmptyFile() {
        try {
            File file = File.createTempFile("test", "tmp");
            assertTrue(file.exists());
            file.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
            fos.close();

            String fileName = file.getCanonicalPath();
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.close();
        } catch (IOException ex) {
            fail(ex.getLocalizedMessage());
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Exceptions checking missed.",
      targets = {
        @TestTarget(
          methodName = "RandomAccessFile",
          methodArgs = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testOpenEmptyFile() {
        try {
            File file = File.createTempFile("test", "tmp");
            assertTrue(file.exists());
            file.deleteOnExit();

            String fileName = file.getCanonicalPath();
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.close();
        } catch (IOException ex) {
            fail(ex.getLocalizedMessage());
        }
    }
}

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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.TestCase;
  
public class RandomAccessFileTest extends TestCase {

	/**
	 * @tests java.io.RandomAccessFile#RandomAccessFile(java.io.File, java.lang.String)
	 */
	public void test_ConstructorLjava_io_FileLjava_lang_String() throws IOException {
		// Regression for HARMONY-50
        File f = File.createTempFile("xxx", "yyy");
        f.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(f, "rws");
        raf.close();

        f = File.createTempFile("xxx", "yyy");
        f.deleteOnExit();
        raf = new RandomAccessFile(f, "rwd");
        raf.close();            
    }
}

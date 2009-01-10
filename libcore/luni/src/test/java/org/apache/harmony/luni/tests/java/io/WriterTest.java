/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;
import java.io.Writer;

import junit.framework.TestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(Writer.class) 
public class WriterTest extends TestCase {

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Writer",
            args = {}
        )
    public void test_Writer() {
        MockWriter w = new MockWriter();
        assertTrue("Test 1: Lock has not been set correctly.", w.lockSet(w));
    }
    
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Writer",
            args = {java.lang.Object.class}
        )
    public void test_WriterLjava_lang_Object() {
        Object o = new Object();
        MockWriter w;
        
        try {
            w = new MockWriter(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        w = new MockWriter(o);
        assertTrue("Test 2: Lock has not been set correctly.", w.lockSet(o));
    }

    class MockWriter extends Writer {
        final Object myLock;

        MockWriter() {
            super();
            myLock = this;
        }
        
        MockWriter(Object lock) {
            super(lock);
            myLock = lock;
        }

        @Override
        public synchronized void close() throws IOException {
            // do nothing
        }

        @Override
        public synchronized void flush() throws IOException {
            // do nothing
        }

        @Override
        public void write(char[] arg0, int arg1, int arg2) throws IOException {
            assertTrue(Thread.holdsLock(myLock));
        }

        public boolean lockSet(Object o) {
            return (lock == o);
        }
    }
}

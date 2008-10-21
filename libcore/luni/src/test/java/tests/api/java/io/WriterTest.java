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

import java.io.IOException;
import java.io.Writer;

import junit.framework.TestCase;

public class WriterTest extends TestCase {

    /**
     * @tests java.io.Writer#append(char)
     */
    public void test_appendChar() throws IOException {
        char testChar = ' ';
        MockWriter writer = new MockWriter(20);
        writer.append(testChar);
        assertEquals(String.valueOf(testChar), String.valueOf(writer
                .getContents()));
        writer.close();
    }

    /**
     * @tests java.io.Writer#append(CharSequence)
     */
    public void test_appendCharSequence() throws IOException {
        String testString = "My Test String";
        MockWriter writer = new MockWriter(20);
        writer.append(testString);
        assertEquals(testString, String.valueOf(writer.getContents()));
        writer.close();

    }

    /**
     * @tests java.io.Writer#append(CharSequence, int, int)
     */
    public void test_appendCharSequenceIntInt() throws IOException {
        String testString = "My Test String";
        MockWriter writer = new MockWriter(20);
        writer.append(testString, 1, 3);
        assertEquals(testString.substring(1, 3), String.valueOf(writer
                .getContents()));
        writer.close();

    }

    class MockWriter extends Writer {
        private char[] contents;

        private int length;

        private int offset;

        MockWriter(int capacity) {
            contents = new char[capacity];
            length = capacity;
            offset = 0;
        }

        public synchronized void close() throws IOException {
            flush();
            contents = null;
        }

        public synchronized void flush() throws IOException {
            // do nothing
        }

        public void write(char[] buffer, int offset, int count)
                throws IOException {
            if (null == contents) {
                throw new IOException();
            }
            if (offset < 0 || count < 0 || offset >= buffer.length) {
                throw new IndexOutOfBoundsException();
            }
            count = Math.min(count, buffer.length - offset);
            count = Math.min(count, this.length - this.offset);
            for (int i = 0; i < count; i++) {
                contents[this.offset + i] = buffer[offset + i];
            }
            this.offset += count;

        }

        public char[] getContents() {
            char[] result = new char[offset];
            for (int i = 0; i < offset; i++) {
                result[i] = contents[i];
            }
            return result;
        }
    }

}

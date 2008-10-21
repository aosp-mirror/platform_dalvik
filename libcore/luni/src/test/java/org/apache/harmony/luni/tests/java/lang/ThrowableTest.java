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
package org.apache.harmony.luni.tests.java.lang;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

public class ThrowableTest extends TestCase {

    /**
     * @tests java.lang.Throwable#Throwable()
     */
    public void test_Constructor() {
        Throwable e = new Throwable();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.Throwable#Throwable(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        Throwable e = new Throwable("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.Throwable#fillInStackTrace()
     */
    public void test_fillInStackTrace() {
        // Test for method java.lang.Throwable
        // java.lang.Throwable.fillInStackTrace()
        class Test implements Runnable {
            public int x;

            public Test(int x) {
                this.x = x;
            }

            public void anotherMethod() {
                if (true)
                    throw new IndexOutOfBoundsException();
            }

            public void run() {
                if (x == 0)
                    throw new IndexOutOfBoundsException();
                try {
                    anotherMethod();
                } catch (IndexOutOfBoundsException e) {
                    e.fillInStackTrace();
                    throw e;
                }
            }
        }
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bao);
        try {
            new Test(0).run();
        } catch (Throwable e) {
            e.printStackTrace(ps);
        }
        ps.flush();
        String s = fixStacktrace(new String(bao.toByteArray(), 0, bao.size()));

        bao.reset();
        try {
            new Test(1).run();
        } catch (Throwable e) {
            e.printStackTrace(ps);
        }
        ps.close();
        String s2 = fixStacktrace(new String(bao.toByteArray(), 0, bao.size()));
        assertTrue("Invalid stackTrace? length: " + s2.length() + "\n" + s2, s2
                .length() > 300);
        assertTrue("Incorrect stackTrace printed: \n" + s2
                + "\n\nCompared with:\n" + s, s2.equals(s));
    }

    private String fixStacktrace(String trace) {
        // remove linenumbers
        StringBuffer sb = new StringBuffer();
        int lastIndex = 0;
        while (lastIndex < trace.length()) {
            int index = trace.indexOf('\n', lastIndex);
            if (index == -1)
                index = trace.length();
            String line = trace.substring(lastIndex, index);
            lastIndex = index + 1;

            index = line.indexOf("(");
            if (index > -1) {
                line = line.substring(0, index);
            }
            // Usually the construction of the exception is removed
            // however if running with the JIT, it may not be removed
            if (line.indexOf("java.lang.Throwable") > -1)
                continue;
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * @tests java.lang.Throwable#printStackTrace()
     */
    public void test_printStackTrace() {
        // Test for method void java.lang.Throwable.printStackTrace()
        Throwable x = new ClassNotFoundException("A Test Message");
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bao);
        PrintStream err = System.err;
        System.setErr(ps);
        x.printStackTrace();
        System.setErr(err);
        ps.close();
        String s = new String(bao.toByteArray(), 0, bao.size());
        assertTrue("Incorrect stackTrace printed:\n" + s, s != null
                && s.length() > 400);
    }

    /**
     * @tests java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    public void test_printStackTraceLjava_io_PrintStream() {
        // Test for method void
        // java.lang.Throwable.printStackTrace(java.io.PrintStream)
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bao);
        Throwable x = new java.net.UnknownHostException("A Message");
        x.printStackTrace(ps);
        ps.close();
        String s = new String(bao.toByteArray(), 0, bao.size());
        assertTrue("Incorrect stackTrace printed:\n" + s, s != null
                && s.length() > 400);
    }

    /**
     * @tests java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    public void test_printStackTraceLjava_io_PrintWriter() {
        // Test for method void
        // java.lang.Throwable.printStackTrace(java.io.PrintWriter)
        // SM
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bao);
        Throwable x = new java.net.UnknownHostException("A Message");
        x.printStackTrace(pw);
        pw.close();
        String s = new String(bao.toByteArray(), 0, bao.size());
        assertTrue("Incorrect stackTrace printed:\n" + s, s != null
                && s.length() > 400);
    }

    /**
     * @tests java.lang.Throwable#toString()
     */
    public void test_toString() {
        Throwable e = new Throwable("Throw");
        assertEquals("java.lang.Throwable: Throw", e.toString());

    }
}

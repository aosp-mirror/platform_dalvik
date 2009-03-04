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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

@TestTargetClass(Throwable.class) 
public class ThrowableTest extends TestCase {

    /**
     * @tests java.lang.Throwable#Throwable()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Throwable",
        args = {}
    )
    public void test_Constructor() {
        Throwable e = new Throwable();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.Throwable#Throwable(java.lang.String)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Throwable",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        )
    })
    public void test_ConstructorLjava_lang_String() {
        Throwable e = new Throwable("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Throwable",
            args = {java.lang.String.class, java.lang.Throwable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        )
    })
    public void test_ConstructorLStringLThrowable() {
        String message = "Test message";
        NullPointerException npe = new NullPointerException();
        Throwable thr = new Throwable(message, npe);
        assertEquals("message is incorrect.", message, thr.getMessage());
        assertEquals("cause is incorrect.", npe, thr.getCause());        
        
        thr = new Throwable(null, npe);
        assertNull("message is not null.", thr.getMessage());
        assertEquals("cause is incorrect.", npe, thr.getCause());  
        
        thr = new Throwable(message, null);
        assertEquals("message is incorrect.", message, thr.getMessage());
        assertNull("cause is not null.", thr.getCause());    
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Throwable",
            args = {java.lang.Throwable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        )
    })
    public void test_ConstructorLThrowable() {
        
        NullPointerException npe = new NullPointerException();
        Throwable thr = new Throwable(npe);
        
        assertEquals("Returned cause is incorrect.", npe, thr.getCause());
        
        thr = new Throwable((Throwable) null);
        assertNull("The cause is not null.", thr.getCause());
    }

    /**
     * @tests java.lang.Throwable#fillInStackTrace()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "fillInStackTrace",
        args = {}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printStackTrace",
        args = {}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printStackTrace",
        args = {java.io.PrintStream.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printStackTrace",
        args = {java.io.PrintWriter.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        Throwable e = new Throwable("Throw");
        assertEquals("java.lang.Throwable: Throw", e.toString());

    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalizedMessage",
        args = {}
    )
    public void test_getLocalizedMessage() {
        String testMessage = "Test message";
        Throwable e = new Throwable(testMessage);
        assertEquals("Returned incorrect localized message.", 
                testMessage, e.getLocalizedMessage());
        
        TestThrowable tt = new TestThrowable(testMessage);
        assertEquals("localized message", tt.getLocalizedMessage());
    }
    
    class TestThrowable extends Throwable {
        
        public TestThrowable(String message) {
            super(message);
        }
        
        public String getLocalizedMessage() {
            return "localized message";
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getStackTrace",
        args = {}
    )
    public void test_getStackTrace() {
        String message = "Test message";
        NullPointerException npe = new NullPointerException();
        Throwable thr = new Throwable(message, npe);
        StackTraceElement[] ste = thr.getStackTrace();
        assertNotNull("Returned stack trace is empty", ste.length != 0);
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "initCause",
        args = {java.lang.Throwable.class}
    )
    public void test_initCause() {
        String message = "Test message";
        NullPointerException npe = new NullPointerException();
        IllegalArgumentException iae = new IllegalArgumentException();
        Throwable thr = new Throwable();
        thr.initCause(iae);
        assertEquals("getCause returns incorrect cause.", iae, thr.getCause());
        
        thr = new Throwable("message");
        thr.initCause(npe);
        assertEquals("getCause returns incorrect cause.", npe, thr.getCause());        
        
        thr = new Throwable(message, npe);        
        try {
            thr.initCause(iae);
            fail("IllegalStateException was not thrown.");
        } catch(IllegalStateException ise) {
            //expected
        }
        
        thr = new Throwable(npe);
        try {
            thr.initCause(iae);
            fail("IllegalStateException was not thrown.");
        } catch(IllegalStateException ise) {
            //expected
        }
        
        thr = new Throwable();
        try {
            thr.initCause(thr);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException ise) {
            //expected
        }        
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setStackTrace",
        args = {java.lang.StackTraceElement[].class}
    )
    public void test_setStackTrace() {
        NullPointerException npe = new NullPointerException();
        Throwable thr = new Throwable(npe);
        StackTraceElement[] ste = thr.getStackTrace();
        Throwable thr1 = new Throwable(npe);
        thr1.setStackTrace(ste);
        assertEquals(ste.length, thr1.getStackTrace().length);
        
        try {
            thr.setStackTrace(null);
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException np) {
            //expected
        }
    }
}

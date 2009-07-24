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

package org.apache.harmony.luni.tests.java.lang;

import dalvik.annotation.AndroidOnly;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@TestTargetClass(ProcessBuilder.class) 
public class ProcessBuilderTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ProcessBuilder",
        args = {java.lang.String[].class}
    )
    public void testProcessBuilderStringArray() {

    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ProcessBuilder",
        args = {java.util.List.class}
    )
    public void testProcessBuilderListOfString() {
        List<String> list = Arrays.asList("command1", "command2", "command3");
        ProcessBuilder pb = new ProcessBuilder(list);
        assertEquals(list, pb.command());
        
        try {
            new ProcessBuilder((List<String>) null);
            fail("no null pointer exception");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "command",
        args = {}
    )
    public void testCommand() {
        ProcessBuilder pb = new ProcessBuilder("command");
        assertEquals(1, pb.command().size());
        assertEquals("command", pb.command().get(0));

        // Regression for HARMONY-2675
        pb = new ProcessBuilder("AAA");
        pb.command("BBB","CCC");
        List<String> list = pb.command();
        list.add("DDD");
        String[] command = new String[3];
        list.toArray(command);
        assertTrue(Arrays.equals(new String[]{"BBB","CCC","DDD"}, command));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "command",
        args = {java.lang.String[].class}
    )
    public void testCommandStringArray() {
        ProcessBuilder pb = new ProcessBuilder("command");
        ProcessBuilder pbReturn = pb.command("cmd");
        assertSame(pb, pbReturn);
        assertEquals(1, pb.command().size());
        assertEquals("cmd", pb.command().get(0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "command",
        args = {java.util.List.class}
    )
    public void testCommandListOfString() {
        ProcessBuilder pb = new ProcessBuilder("command");
        List<String> newCmd = new ArrayList<String>();
        newCmd.add("cmd");
        ProcessBuilder pbReturn = pb.command(newCmd);
        assertSame(pb, pbReturn);
        assertEquals(1, pb.command().size());
        assertEquals("cmd", pb.command().get(0));
        
        newCmd.add("arg");
        assertEquals(2, pb.command().size());
        assertEquals("cmd", pb.command().get(0));
        assertEquals("arg", pb.command().get(1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "directory",
        args = {}
    )
    public void testDirectory() {
        ProcessBuilder pb = new ProcessBuilder("command");
        assertNull(pb.directory());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "directory",
        args = {java.io.File.class}
    )
    public void testDirectoryFile() {
        ProcessBuilder pb = new ProcessBuilder("command");
        File dir = new File(System.getProperty("java.io.tmpdir"));
        ProcessBuilder pbReturn = pb.directory(dir);
        assertSame(pb, pbReturn);
        assertEquals(dir, pb.directory());
        
        pbReturn = pb.directory(null);
        assertSame(pb, pbReturn);
        assertNull(pb.directory());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "environment",
        args = {}
    )
    @AndroidOnly("SystemEnvironment.clear() method throws UnsupportedOperationException.")
    public void testEnvironment() {
        ProcessBuilder pb = new ProcessBuilder("command");
        Map<String, String> env = pb.environment();
        assertEquals(System.getenv().size(), env.size());
        try {
            env.clear();
            env = pb.environment();
            assertTrue(env.isEmpty());
            try {
                env.put(null,"");
                fail("should throw NPE.");
            } catch (NullPointerException e) {
                // expected;
            }
            try {
                env.put("",null);
                fail("should throw NPE.");
            } catch (NullPointerException e) {
                // expected;
            }
            try {
                env.get(null);
                fail("should throw NPE.");
            } catch (NullPointerException e) {
                // expected;
            }
            try {
                env.get(new Object());
                fail("should throw ClassCastException.");
            } catch (ClassCastException e) {
                // expected;
            }
        } catch(UnsupportedOperationException e) {
            //expected: Android specific
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "redirectErrorStream",
        args = {}
    )
    public void testRedirectErrorStream() {
        ProcessBuilder pb = new ProcessBuilder("command");
        assertFalse(pb.redirectErrorStream());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Doesn't check false.",
        method = "redirectErrorStream",
        args = {boolean.class}
    )
    public void testRedirectErrorStreamBoolean() {
        ProcessBuilder pb = new ProcessBuilder("command");
        ProcessBuilder pbReturn = pb.redirectErrorStream(true);
        assertSame(pb, pbReturn);
        assertTrue(pb.redirectErrorStream());
    }

    /**
     * @throws IOException
     * @tests {@link java.lang.ProcessBuilder#start()}
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException is not checked.",
        method = "start",
        args = {}
    )
    @SuppressWarnings("nls")
    public void testStart() throws IOException {
        String cmd = "Dalvik".equals(System.getProperty("java.vm.name")) ?
                "dalvikvm" : "java";
        ProcessBuilder pb = new ProcessBuilder(cmd, "-version");

        // Call the test target
        Process process = pb.start();
        InputStream in = process.getInputStream();
        InputStream err = process.getErrorStream();

        while (true) {
            try {
                process.waitFor();
                break;
            } catch (InterruptedException e) {
                // Ignored
            }
        }

        byte[] buf = new byte[1024];
        if (in.available() > 0) {
            assertTrue(in.read(buf) > 0);
        } else {
            assertTrue(err.read(buf) > 0);
        }
        
        List<String> list = Arrays.asList(null, null, null);
        ProcessBuilder pbn = new ProcessBuilder(list);
        try {
            pbn.start();
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException npe) {
            //expected
        }
        
        List<String> emptyList = Arrays.asList();
        ProcessBuilder pbe = new ProcessBuilder(emptyList);
        try {
            pbe.start();
            fail("IndexOutOfBoundsException  is not thrown.");
        } catch(IndexOutOfBoundsException npe) {
            //expected
        }
        
        SecurityManager sm = new SecurityManager() {

            public void checkPermission(Permission perm) {
            }
            
            public void checkExec(String cmd) {
                throw new SecurityException(); 
            }
        };

        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            pb.start();
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(oldSm);
        }      
        
        pb.directory(new File(System.getProperty("java.class.path")));
    }
}

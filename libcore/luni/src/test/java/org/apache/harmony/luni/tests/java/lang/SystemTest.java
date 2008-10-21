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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.Map;
import java.util.Properties;

public class SystemTest extends junit.framework.TestCase {

    static boolean flag = false;

    static boolean ranFinalize = false;

    /**
     * @tests java.lang.System#setIn(java.io.InputStream)
     */
    public void test_setInLjava_io_InputStream() {
        InputStream orgIn = System.in;
        InputStream in = new ByteArrayInputStream(new byte[0]);
        System.setIn(in);
        assertTrue("in not set", System.in == in);
        System.setIn(orgIn);
    }

    /**
     * @tests java.lang.System#setOut(java.io.PrintStream)
     */
    public void test_setOutLjava_io_PrintStream() {
        PrintStream orgOut = System.out;
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        System.setOut(out);
        assertTrue("out not set", System.out == out);
        System.setOut(orgOut);
    }

    /**
     * @tests java.lang.System#setErr(java.io.PrintStream)
     */
    public void test_setErrLjava_io_PrintStream() {
        PrintStream orgErr = System.err;
        PrintStream err = new PrintStream(new ByteArrayOutputStream());
        System.setErr(err);
        assertTrue("err not set", System.err == err);
        System.setErr(orgErr);
    }

    /**
     * @tests java.lang.System#arraycopy(java.lang.Object, int,
     *        java.lang.Object, int, int)
     */
    public void test_arraycopyLjava_lang_ObjectILjava_lang_ObjectII() {
        // Test for method void java.lang.System.arraycopy(java.lang.Object,
        // int, java.lang.Object, int, int)
        Integer a[] = new Integer[20];
        Integer b[] = new Integer[20];
        int i = 0;
        while (i < a.length) {
            a[i] = new Integer(i);
            ++i;
        }
        System.arraycopy(a, 0, b, 0, a.length);
        for (i = 0; i < a.length; i++)
            assertTrue("Copied elements incorrectly", a[i].equals(b[i]));

        /* Non primitive array types don't need to be identical */
        String[] source1 = new String[] { "element1" };
        Object[] dest1 = new Object[1];
        System.arraycopy(source1, 0, dest1, 0, dest1.length);
        assertTrue("Invalid copy 1", dest1[0] == source1[0]);

        char[][] source = new char[][] { { 'H', 'e', 'l', 'l', 'o' },
                { 'W', 'o', 'r', 'l', 'd' } };
        char[][] dest = new char[2][];
        System.arraycopy(source, 0, dest, 0, dest.length);
        assertTrue("Invalid copy 2", dest[0] == source[0]
                && dest[1] == source[1]);
    }

    /**
     * @tests java.lang.System#currentTimeMillis()
     */
    public void test_currentTimeMillis() {
        // Test for method long java.lang.System.currentTimeMillis()
        try {
            long firstRead = System.currentTimeMillis();
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
            }
            long secondRead = System.currentTimeMillis();
            assertTrue("Incorrect times returned: " + firstRead + ", "
                    + secondRead, firstRead < secondRead);
        } catch (Exception e) {
            System.out.println("Exception during test: " + e.toString());
        }
    }

    /**
     * @tests java.lang.System#exit(int)
     */
    public void test_exitI() {
        // Test for method void java.lang.System.exit(int)
        // Tested in destructive test: Test_System_Exit ???
    }

    /**
     * @tests java.lang.System#getProperties()
     */
    public void test_getProperties() {
        // Test for method java.util.Properties java.lang.System.getProperties()
        Properties p = System.getProperties();
        assertTrue("Incorrect properties returned", p.getProperty(
                "java.version").indexOf("1.", 0) >= 0);

        // Ensure spec'ed properties are non-null. See System.getProperties()
        // spec.
        String[] props = { "java.version", "java.vendor", "java.vendor.url",
                "java.home", "java.vm.specification.version",
                "java.vm.specification.vendor", "java.vm.specification.name",
                "java.vm.version", "java.vm.vendor", "java.vm.name",
                "java.specification.name", "java.specification.vendor",
                "java.specification.name", "java.class.version",
                "java.class.path", "java.ext.dirs", "os.name", "os.arch",
                "os.version", "file.separator", "path.separator",
                "line.separator", "user.name", "user.home", "user.dir", };
        for (int i = 0; i < props.length; i++) {
            assertNotNull(props[i], System.getProperty(props[i]));
        }
    }

    /**
     * @tests java.lang.System#getProperty(java.lang.String)
     */
    public void test_getPropertyLjava_lang_String() {
        // Test for method java.lang.String
        // java.lang.System.getProperty(java.lang.String)
        assertTrue("Failed to return correct property value", System
                .getProperty("java.version").indexOf("1.", 0) >= 0);

        boolean is8859_1 = true;
        String encoding = System.getProperty("file.encoding");
        byte[] bytes = new byte[128];
        char[] chars = new char[128];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i + 128);
            chars[i] = (char) (i + 128);
        }
        String charResult = new String(bytes);
        byte[] byteResult = new String(chars).getBytes();
        if (charResult.length() == 128 && byteResult.length == 128) {
            for (int i = 0; i < bytes.length; i++) {
                if (charResult.charAt(i) != (char) (i + 128)
                        || byteResult[i] != (byte) (i + 128))
                    is8859_1 = false;
            }
        } else
            is8859_1 = false;
        String[] possibles = new String[] { "ISO8859_1", "8859_1", "ISO8859-1",
                "ISO-8859-1", "ISO_8859-1", "ISO_8859-1:1978", "ISO-IR-100",
                "LATIN1", "CSISOLATIN1" };
        boolean found8859_1 = false;
        for (int i = 0; i < possibles.length; i++) {
            if (possibles[i].equals(encoding)) {
                found8859_1 = true;
                break;
            }
        }
        assertTrue("Wrong encoding: " + encoding, !is8859_1 || found8859_1);
    }

    /**
     * @tests java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public void test_getPropertyLjava_lang_StringLjava_lang_String() {
        // Test for method java.lang.String
        // java.lang.System.getProperty(java.lang.String, java.lang.String)
        assertTrue("Failed to return correct property value: "
                + System.getProperty("java.version", "99999"), System
                .getProperty("java.version", "99999").indexOf("1.", 0) >= 0);
        assertEquals("Failed to return correct property value", "bogus", System
                .getProperty("bogus.prop", "bogus"));
    }

    /**
     * @tests java.lang.System#setProperty(java.lang.String, java.lang.String)
     */
    public void test_setPropertyLjava_lang_StringLjava_lang_String() {
        // Test for method java.lang.String
        // java.lang.System.setProperty(java.lang.String, java.lang.String)

        assertNull("Failed to return null", System.setProperty("testing",
                "value1"));
        assertTrue("Failed to return old value", System.setProperty("testing",
                "value2") == "value1");
        assertTrue("Failed to find value",
                System.getProperty("testing") == "value2");

        boolean exception = false;
        try {
            System.setProperty("", "default");
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue("Expected IllegalArgumentException", exception);
    }

    /**
     * @tests java.lang.System#getSecurityManager()
     */
    public void test_getSecurityManager() {
        // Test for method java.lang.SecurityManager
        // java.lang.System.getSecurityManager()
        assertNull("Returned incorrect SecurityManager", System
                .getSecurityManager());
    }

    /**
     * @tests java.lang.System#identityHashCode(java.lang.Object)
     */
    public void test_identityHashCodeLjava_lang_Object() {
        // Test for method int
        // java.lang.System.identityHashCode(java.lang.Object)
        Object o = new Object();
        String s = "Gabba";
        assertEquals("Nonzero returned for null",
                0, System.identityHashCode(null));
        assertTrue("Nonequal has returned for Object", System
                .identityHashCode(o) == o.hashCode());
        assertTrue("Same as usual hash returned for String", System
                .identityHashCode(s) != s.hashCode());
    }

    /**
     * @throws IOException
     * @tests java.lang.System#inheritedChannel()
     */
    public void test_inheritedChannel() throws IOException {
        Channel iChannel = System.inheritedChannel();
        assertNull("Incorrect value of channel", iChannel);
        SelectorProvider sp = SelectorProvider.provider();
        assertEquals("Incorrect value of channel",
                sp.inheritedChannel(), iChannel);
        try {
            SecurityManager localManager = new MockSecurityManager();
            System.setSecurityManager(localManager);
            try {
                System.inheritedChannel();
                fail("Expected SecurityException was not thrown");
            } catch (SecurityException e) {
                // expected
            }
        } finally {
            System.setSecurityManager(null);
        }
    }

    /**
     * @tests java.lang.System#runFinalization()
     */
    public void test_runFinalization() {
        // Test for method void java.lang.System.runFinalization()

        flag = true;
        createInstance();
        int count = 10;
        // the gc below likely bogosifies the test, but will have to do for
        // the moment
        while (!ranFinalize && count-- > 0) {
            System.gc();
            System.runFinalization();
        }
        assertTrue("Failed to run finalization", ranFinalize);
    }

    /**
     * @tests java.lang.System#runFinalizersOnExit(boolean)
     */
    @SuppressWarnings("deprecation")
    public void test_runFinalizersOnExitZ() {
        // Can we call the method at least?
        try {
            System.runFinalizersOnExit(false);
        } catch (Throwable t) {
            fail("Failed to set runFinalizersOnExit");
        }
    }

    /**
     * @tests java.lang.System#setProperties(java.util.Properties)
     */
    public void test_setPropertiesLjava_util_Properties() {
        // Test for method void
        // java.lang.System.setProperties(java.util.Properties)

        Properties orgProps = System.getProperties();
        java.util.Properties tProps = new java.util.Properties();
        tProps.put("test.prop", "this is a test property");
        tProps.put("bogus.prop", "bogus");
        System.setProperties(tProps);
        try {
            assertEquals("Failed to set properties", "this is a test property", System.getProperties()
                    .getProperty("test.prop"));
        } finally {
            // restore the original properties
            System.setProperties(orgProps);
        }
    }

    //Regression Test for Harmony-2356
    public void testEnvUnmodifiable() {
        Map map = System.getenv();
        try {
            map.containsKey(null);
            fail("Should throw NullPointerExcepiton.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            map.containsKey(new Integer(10));
            fail("Should throw ClassCastException.");
        } catch (ClassCastException e) {
            // expected
        }

        try {
            map.containsValue(null);
            fail("Should throw NullPointerExcepiton.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            map.containsValue(new Integer(10));
            fail("Should throw ClassCastException.");
        } catch (ClassCastException e) {
            // expected
        }

        try {
            map.get(null);
            fail("Should throw NullPointerExcepiton.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            map.get(new Integer(10));
            fail("Should throw ClassCastException.");
        } catch (ClassCastException e) {
            // expected
        }

        try {
            map.put(null, "AAA");
            fail("Should throw UnsupportedOperationExcepiton.");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            map.put("AAA", new Integer(10));
            fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            map.put("AAA", "BBB");
            fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            map.clear();
            fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            map.remove(null);
            fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // expected
        }

    }

    public void test_setSecurityManagerLjava_lang_SecurityManager() {
        assertEquals("Incorrect SecurityManager",
                null, System.getSecurityManager());
        try {
            SecurityManager localManager = new MockSecurityManager();
            System.setSecurityManager(localManager);
            assertEquals("Incorrect SecurityManager",
                    localManager, System.getSecurityManager());
        } finally {
            System.setSecurityManager(null);
        }
    }

    @Override
    protected void setUp() {
        flag = false;
        ranFinalize = false;
    }

    protected SystemTest createInstance() {
        return new SystemTest("FT");
    }

    @Override
    protected void finalize() {
        if (flag)
            ranFinalize = true;
    }

    public SystemTest() {
    }

    public SystemTest(String name) {
        super(name);
    }

    private class MockSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            if (perm.equals(new RuntimePermission("inheritedChannel")))
                throw new SecurityException("Incorrect permission");
        }
    }
}

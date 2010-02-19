/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

import java.io.InputStream;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;

import junit.framework.TestCase;

public class ClassLoaderTest extends TestCase {

    public static volatile int flag;

    /**
     * Tests that Classloader.defineClass() assigns appropriate 
     * default domains to the defined classes.
     */
    public void test_defineClass_defaultDomain() throws Exception {
        // Regression for HARMONY-765 
        DynamicPolicy plc = new DynamicPolicy();
        Policy back = Policy.getPolicy();
        try {
            Policy.setPolicy(plc);

            Class<?> a = new Ldr().define();

            Permission p = new SecurityPermission("abc");
            assertFalse("impossible! misconfiguration?", a.getProtectionDomain().implies(p));

            plc.pc = p.newPermissionCollection();
            plc.pc.add(p);
            assertTrue("default domain is not dynamic", a.getProtectionDomain().implies(p));
        } finally {
            Policy.setPolicy(back);
        }
    }
    
    static class SyncTestClassLoader extends ClassLoader {
        Object lock;
        volatile int numFindClassCalled;
        
        SyncTestClassLoader(Object o) {
            this.lock = o;
            numFindClassCalled = 0;
        }

        /*
         * Byte array of bytecode equivalent to the following source code:
         * public class TestClass {
         * }
         */
        private byte[] classData = new byte[] {
            -54, -2, -70, -66, 0, 0, 0, 49, 0, 13,
            10, 0, 3, 0, 10, 7, 0, 11, 7, 0,
            12, 1, 0, 6, 60, 105, 110, 105, 116, 62,
            1, 0, 3, 40, 41, 86, 1, 0, 4, 67,
            111, 100, 101, 1, 0, 15, 76, 105, 110, 101,
            78, 117, 109, 98, 101, 114, 84, 97, 98, 108,
            101, 1, 0, 10, 83, 111, 117, 114, 99, 101,
            70, 105, 108, 101, 1, 0, 14, 84, 101, 115,
            116, 67, 108, 97, 115, 115, 46, 106, 97, 118,
            97, 12, 0, 4, 0, 5, 1, 0, 9, 84,
            101, 115, 116, 67, 108, 97, 115, 115, 1, 0,
            16, 106, 97, 118, 97, 47, 108, 97, 110, 103,
            47, 79, 98, 106, 101, 99, 116, 0, 33, 0,
            2, 0, 3, 0, 0, 0, 0, 0, 1, 0,
            1, 0, 4, 0, 5, 0, 1, 0, 6, 0,
            0, 0, 29, 0, 1, 0, 1, 0, 0, 0,
            5, 42, -73, 0, 1, -79, 0, 0, 0, 1,
            0, 7, 0, 0, 0, 6, 0, 1, 0, 0,
            0, 1, 0, 1, 0, 8, 0, 0, 0, 2,
            0, 9 };

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                while (flag != 2) {
                    synchronized (lock) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException ie) {}

            if (name.equals("TestClass")) {
                numFindClassCalled++;
                return defineClass(null, classData, 0, classData.length);
            }
            throw new ClassNotFoundException("Class " + name + " not found.");
        }
    }
    
    static class SyncLoadTestThread extends Thread {
        volatile boolean started;
        ClassLoader cl;
        Class<?> cls;
        
        SyncLoadTestThread(ClassLoader cl) {
            this.cl = cl;
        }
        
        @Override
        public void run() {
            try {
                started = true;
                cls = Class.forName("TestClass", false, cl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Regression test for HARMONY-1939:
     * 2 threads simultaneously run Class.forName() method for the same classname 
     * and the same classloader. It is expected that both threads succeed but
     * class must be defined just once.  
     */
    public void test_loadClass_concurrentLoad() throws Exception 
    {    
        Object lock = new Object();
        SyncTestClassLoader cl = new SyncTestClassLoader(lock);
        SyncLoadTestThread tt1 = new SyncLoadTestThread(cl);
        SyncLoadTestThread tt2 = new SyncLoadTestThread(cl);
        flag = 1;
        tt1.start();
        tt2.start();

        while (!tt1.started && !tt2.started) {
            Thread.sleep(100);
        }

        flag = 2;
        synchronized (lock) {
            lock.notifyAll();
        }
        tt1.join();
        tt2.join();
        
        assertSame("Bad or redefined class", tt1.cls, tt2.cls);
        assertEquals("Both threads tried to define class", 1, cl.numFindClassCalled);
    }

    /**
     * @tests java.lang.ClassLoader#getResource(java.lang.String)
     */
    public void test_getResourceLjava_lang_String() {
        // Test for method java.net.URL
        // java.lang.ClassLoader.getResource(java.lang.String)
        java.net.URL u = ClassLoader.getSystemClassLoader().getResource("hyts_Foo.c");
        assertNotNull("Unable to find resource", u);
        java.io.InputStream is = null;
        try {
            is = u.openStream();
            assertNotNull("Resource returned is invalid", is);
            is.close();
        } catch (java.io.IOException e) {
            fail("IOException getting stream for resource : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    public void test_getResourceAsStreamLjava_lang_String() {
        // Test for method java.io.InputStream
        // java.lang.ClassLoader.getResourceAsStream(java.lang.String)
        // Need better test...

        java.io.InputStream is = null;
        assertNotNull("Failed to find resource: hyts_Foo.c", (is = ClassLoader
                .getSystemClassLoader().getResourceAsStream("hyts_Foo.c")));
        try {
            is.close();
        } catch (java.io.IOException e) {
            fail("Exception during getResourceAsStream: " + e.toString());
        }
    }

    /**
     * @tests java.lang.ClassLoader#getSystemClassLoader()
     */
    public void test_getSystemClassLoader() {
        // Test for method java.lang.ClassLoader
        // java.lang.ClassLoader.getSystemClassLoader()
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        java.io.InputStream is = cl.getResourceAsStream("hyts_Foo.c");
        assertNotNull("Failed to find resource from system classpath", is);
        try {
            is.close();
        } catch (java.io.IOException e) {
        }

    }

    /**
     * @tests java.lang.ClassLoader#getSystemResource(java.lang.String)
     */
    public void test_getSystemResourceLjava_lang_String() {
        // Test for method java.net.URL
        // java.lang.ClassLoader.getSystemResource(java.lang.String)
        // Need better test...
        assertNotNull("Failed to find resource: hyts_Foo.c", ClassLoader
                .getSystemResource("hyts_Foo.c"));
    }
    
    
    //Regression Test for JIRA-2047
    public void test_getResourceAsStream_withSharpChar() throws Exception {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(
				ClassTest.FILENAME);
		assertNotNull(in);
		in.close();
	}
}

class DynamicPolicy extends Policy {

    public PermissionCollection pc;

    @Override
    public PermissionCollection getPermissions(ProtectionDomain pd) {
        return pc;
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return pc;
    }

    @Override
    public void refresh() {
    }
}

class A {
}

class Ldr extends ClassLoader {
    @SuppressWarnings("deprecation")
    public Class<?> define() throws Exception {
        Package p = getClass().getPackage();
        // Class loader paths use '/' character as separator
        String path = p == null ? "" : p.getName().replace('.', '/') + '/';
        InputStream is = getResourceAsStream(path + "A.class");
        byte[] buf = new byte[512];
        int len = is.read(buf);
        return defineClass(buf, 0, len);
    }
}

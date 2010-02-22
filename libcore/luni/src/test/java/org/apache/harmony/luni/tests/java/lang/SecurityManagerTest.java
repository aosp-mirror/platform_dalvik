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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AllPermission;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.security.Security;

import junit.framework.TestCase;
import tests.support.Support_Exec;

/**
 * Test case for java.lang.SecurityManager
 */
public class SecurityManagerTest extends TestCase {
    MutableSecurityManager mutableSM = null;

    MockSecurityManager mockSM = null;

    SecurityManager originalSM = null;

    /**
     * @tests java.lang.SecurityManager#checkPackageAccess(String)
     */
    public void test_checkPackageAccessLjava_lang_String() {
        final String old = Security.getProperty("package.access");
        Security.setProperty("package.access", "a.,bbb, c.d.");

        mutableSM
                .denyPermission(new RuntimePermission("accessClassInPackage.*"));

        try {
            mutableSM.checkPackageAccess("z.z.z");
            mutableSM.checkPackageAccess("aa");
            mutableSM.checkPackageAccess("bb");
            mutableSM.checkPackageAccess("c");

            try {
                mutableSM.checkPackageAccess("a");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

            try {
                mutableSM.checkPackageAccess("bbb");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

            try {
                mutableSM.checkPackageAccess("c.d.e");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

            Security.setProperty("package.access", "QWERTY");
            mutableSM.checkPackageAccess("a");
            mutableSM.checkPackageAccess("qwerty");
            try {
                mutableSM.checkPackageAccess("QWERTY");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

        } finally {
            Security.setProperty("package.access", old == null ? "" : old);
        }
    }

    /**
     * @tests java.lang.SecurityManager#checkPackageDefinition(String)
     */
    public void test_checkPackageDefinitionLjava_lang_String() {
        final String old = Security.getProperty("package.definition");
        Security.setProperty("package.definition", "a.,bbb, c.d.");

        mutableSM
                .denyPermission(new RuntimePermission("defineClassInPackage.*"));

        try {
            mutableSM.checkPackageDefinition("z.z.z");
            mutableSM.checkPackageDefinition("aa");
            mutableSM.checkPackageDefinition("bb");
            mutableSM.checkPackageDefinition("c");

            try {
                mutableSM.checkPackageDefinition("a");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

            try {
                mutableSM.checkPackageDefinition("bbb");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

            try {
                mutableSM.checkPackageDefinition("c.d.e");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

            Security.setProperty("package.definition", "QWERTY");
            mutableSM.checkPackageDefinition("a");
            mutableSM.checkPackageDefinition("qwerty");
            try {
                mutableSM.checkPackageDefinition("QWERTY");
                fail("This should throw a SecurityException.");
            } catch (SecurityException ok) {
            }

        } finally {
            Security.setProperty("package.definition", old == null ? "" : old);
        }
    }

    /**
     * @tests java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
     */
    public void test_checkMemberAccessLjava_lang_ClassI() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM
                .denyPermission(new RuntimePermission("accessDeclaredMembers"));
        System.setSecurityManager(mutableSM);
        try {
            getClass().getDeclaredFields();

            try {
                Object.class.getDeclaredFields();
                fail("This should throw a SecurityException.");
            } catch (SecurityException e) {
            }

        } finally {
            System.setSecurityManager(null);
        }
    }

    /**
     * @tests java.lang.SecurityManager#checkPermission(java.security.Permission)
     */
    /* BEGIN android-removed: we don't have Support_Exec.execJava.
    public void test_checkPermissionLjava_security_Permission()
            throws Exception {

        // tmp user home to avoid presence of ${user.home}/.java.policy
        String tmpUserHome = System.getProperty("java.io.tmpdir")
                + File.separatorChar + "tmpUserHomeForSecurityManagerTest";
        File dir = new File(tmpUserHome);
        if (!dir.exists()) {
            dir.mkdirs();
            dir.deleteOnExit();
        }
        String javaPolycy = tmpUserHome + File.separatorChar + ".java.policy";
        assertFalse("There should be no java policy file: " + javaPolycy,
                new File(javaPolycy).exists());

        String[] arg = new String[] { "-Duser.home=" + tmpUserHome,
                checkPermissionLjava_security_PermissionTesting.class.getName() };

        Support_Exec.execJava(arg, null, true);
    }
    */

    private static class checkPermissionLjava_security_PermissionTesting {
        public static void main(String[] args) {
            MutableSecurityManager sm = new MutableSecurityManager();
            sm.addPermission(MutableSecurityManager.SET_SECURITY_MANAGER);
            System.setSecurityManager(sm);
            try {
                try {
                    System.getSecurityManager().checkPermission(
                            new RuntimePermission("createClassLoader"));
                    fail("This should throw a SecurityException");
                } catch (SecurityException e) {
                }
            } finally {
                System.setSecurityManager(null);
            }
        }
    }

    /**
     * @tests java.lang.SecurityManager#checkAccess(java.lang.Thread)
     */
    public void test_checkAccessLjava_lang_Thread() throws InterruptedException {
        // Regression for HARMONY-66
        Thread t = new Thread() {
            @Override
            public void run() {
            };
        };
        t.start();
        t.join();
        new SecurityManager().checkAccess(t);
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkAccept(String, int)}
     */
    @SuppressWarnings("nls")
    public void test_checkAcceptLjava_lang_String_int() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                "accept, connect, listen"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkAccept("localhost", 1024);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkConnect(String, int, Object)}
     */
    @SuppressWarnings("nls")
    public void test_checkConnectLjava_lang_String_int_Ljava_lang_Object() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                "accept, connect, listen"));
        System.setSecurityManager(mutableSM);
        ProtectionDomain pDomain = this.getClass().getProtectionDomain();
        ProtectionDomain[] pd = { pDomain };
        AccessControlContext acc = new AccessControlContext(pd);
        try {
            mutableSM.checkConnect("localhost", 1024, acc);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkExec(String)}
     */
    @SuppressWarnings("nls")
    public void test_checkExecLjava_lang_String() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM
                .denyPermission(new FilePermission("<<ALL FILES>>", "execute"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkExec("java");
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkExit(int)}
     */
    @SuppressWarnings("nls")
    public void test_checkExit_int() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new RuntimePermission("exitVM"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkExit(0);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkLink(String)}
     */
    @SuppressWarnings("nls")
    public void test_checkLinkLjava_lang_String() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new RuntimePermission("loadLibrary.harmony"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkLink("harmony");
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkListen(int)}
     */
    @SuppressWarnings("nls")
    public void test_checkListen_int() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM
                .denyPermission(new SocketPermission("localhost:80", "listen"));
        System.setSecurityManager(mutableSM);

        try {
            mutableSM.checkListen(80);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission("localhost:1024-",
                "listen"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkListen(0);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @throws UnknownHostException
     * @tests {@link java.lang.SecurityManager#checkMulticast(java.net.InetAddress)}
     */
    @SuppressWarnings("nls")
    public void test_checkMulticastLjava_net_InetAddress()
            throws UnknownHostException {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission(InetAddress.getByName(
                "localhost").getHostAddress(), "accept,connect"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkMulticast(InetAddress.getByName("localhost"));
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @throws UnknownHostException
     * @tests {@link java.lang.SecurityManager#checkMulticast(java.net.InetAddress,byte)}
     */
    @SuppressWarnings( { "nls", "deprecation" })
    public void test_checkMulticastLjava_net_InetAddress_int()
            throws UnknownHostException {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new SocketPermission(InetAddress.getByName(
                "localhost").getHostAddress(), "accept,connect"));
        System.setSecurityManager(mutableSM);
        try {
            // the second parameter is the TTL(time to live)
            mutableSM.checkMulticast(InetAddress.getByName("localhost"),
                    (byte) 0);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * 
     * @tests {@link java.lang.SecurityManager#checkPermission(Permission, Object)}
     */
    @SuppressWarnings("nls")
    public void test_checkPermissionLjava_security_PermissionLjava_lang_Object() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        Permission denyp = new SocketPermission("localhost:1024-",
                "accept, connect, listen");
        mutableSM.denyPermission(denyp);
        System.setSecurityManager(mutableSM);
        ProtectionDomain pDomain = this.getClass().getProtectionDomain();
        ProtectionDomain[] pd = { pDomain };
        AccessControlContext acc = new AccessControlContext(pd);
        try {
            mutableSM.checkPermission(denyp, acc);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkPrintJobAccess()}
     */
    @SuppressWarnings("nls")
    public void test_checkPrintJobAccess() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new RuntimePermission("queuePrintJob"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkPrintJobAccess();
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkRead(FileDescriptor)}
     */
    @SuppressWarnings("nls")
    public void test_checkReadLjava_io_FileDescriptor() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new RuntimePermission("readFileDescriptor"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkRead(new FileDescriptor());
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkRead(String,Object)}
     */
    @SuppressWarnings("nls")
    public void test_checkReadLjava_lang_StringLjava_lang_Object() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new FilePermission("<<ALL FILES>>", "read"));
        ProtectionDomain pDomain = this.getClass().getProtectionDomain();
        ProtectionDomain[] pd = { pDomain };
        AccessControlContext acc = new AccessControlContext(pd);
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkRead("aa", acc);
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#checkSetFactory()}
     */
    @SuppressWarnings("nls")
    public void test_checkSetFactory() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new RuntimePermission("setFactory"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkSetFactory();
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#getInCheck()}
     */
    public void test_getIncheck() {
        mockSM.setInCheck(false);
        assertFalse(mockSM.getInCheck());
        mockSM.setInCheck(true);
        assertTrue(mockSM.getInCheck());
    }

    /**
     * @tests {@link java.lang.SecurityManager#getSecurityContext()}
     */
    @SuppressWarnings("nls")
    public void test_getSecurityContext() {
        // enable all but one check
        mutableSM.addPermission(new AllPermission());
        mutableSM.denyPermission(new FilePermission("<<ALL FILES>>", "read"));
        System.setSecurityManager(mutableSM);
        try {
            mutableSM.checkRead("aa", mutableSM.getSecurityContext());
            fail("This should throw a SecurityException.");
        } catch (SecurityException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.lang.SecurityManager#classDepth(String)}
     */
    @SuppressWarnings("nls")
    public void test_classDepthLjava_lang_String() {
        assertEquals(-1, mockSM.classDepth("nothing"));
    }

    /**
     * @tests {@link java.lang.SecurityManager#classLoaderDepth()}
     */
    public void test_classLoaderDepth() {
        assertEquals(-1, mockSM.classLoaderDepth());
    }

    /**
     * @tests {@link java.lang.SecurityManager#currentClassLoader()}
     */
    public void test_currentClassLoader() {
        assertNull(mockSM.currentClassLoader());
    }

    /**
     * @tests {@link java.lang.SecurityManager#currentLoadedClass()}
     */
    public void test_currentLoadedClass() {
        assertNull(mockSM.currentLoadedClass());
    }

    /**
     * @tests {@link java.lang.SecurityManager#inClass(String)}
     */
    @SuppressWarnings("nls")
    public void test_inClassLjava_lang_String() {
        assertFalse(mockSM.inClass("nothing"));
        assertTrue(mockSM.inClass(MockSecurityManager.class.getName()));
    }

    /**
     * @tests {@link java.lang.SecurityManager#inClassLoader()}
     */
    public void test_inClassLoader() {
        assertFalse(mockSM.inClassLoader());
    }

    /**
     * @tests {@link java.lang.SecurityManager#inClassLoader()}
     */
    public void test_getClassContext() {
        assertEquals("MockSecurityManager should be the first in the classes stack",
                mockSM.getClassContext()[0], MockSecurityManager.class);
    }

    // set some protected method to public for testing
    class MockSecurityManager extends SecurityManager {

        public void setInCheck(boolean inCheck) {
            super.inCheck = inCheck;
        }

        @Override
        public int classDepth(String name) {
            return super.classDepth(name);
        }

        @Override
        public int classLoaderDepth() {
            return super.classLoaderDepth();
        }

        @Override
        public ClassLoader currentClassLoader() {
            return super.currentClassLoader();
        }

        @Override
        public Class<?> currentLoadedClass() {
            return super.currentLoadedClass();
        }

        @Override
        public Class[] getClassContext() {
            return super.getClassContext();
        }

        @Override
        public boolean inClass(String name) {
            return super.inClass(name);
        }

        @Override
        public boolean inClassLoader() {
            return super.inClassLoader();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mutableSM = new MutableSecurityManager();
        mockSM = new MockSecurityManager();
        originalSM = System.getSecurityManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setSecurityManager(originalSM);
    }
}

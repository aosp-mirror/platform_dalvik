/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.security;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.BasicPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import junit.framework.TestCase;

@TestTargetClass(AccessController.class)
public class AccessControllerTest extends TestCase {
    
    private static void setProtectionDomain(Class<?> c, ProtectionDomain pd){
        Field fields[] = Class.class.getDeclaredFields();
        for(Field f : fields){
            if("pd".equals(f.getName())){
                f.setAccessible(true);
                try {
                    f.set(c, pd);
                } catch (IllegalArgumentException e) {
                    fail("Protection domain could not be set");
                } catch (IllegalAccessException e) {
                    fail("Protection domain could not be set");
                }
                break;
            }
        }
    }

    SecurityManager old;
    TestPermission p;
    CodeSource codeSource;
    PermissionCollection c0, c1, c2;
    
    public static void main(String[] args) throws Exception {
        AccessControllerTest t = new AccessControllerTest();
        t.setUp();
        t.test_do_privileged1();
        t.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        codeSource = null;
        p = new TestPermission();
        c0 = p.newPermissionCollection();
        c1 = p.newPermissionCollection();
        c2 = p.newPermissionCollection();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkPermission throws a SecurityException " +
                    "if a particular permission is not set in the protection domain " +
                    "of a class on the call stack.",
            method = "checkPermission",
            args = {Permission.class}
        )
    })
    public void test_do_privileged1() throws Exception {
        // add TestPermission to T1 and T2 only
        c1.add(p);
        c2.add(p);
        setProtectionDomain(T0.class, new ProtectionDomain(codeSource, c0));
        setProtectionDomain(T1.class, new ProtectionDomain(codeSource, c1));
        setProtectionDomain(T2.class, new ProtectionDomain(codeSource, c2));
        
        System.setSecurityManager(new SecurityManager());
        try {
            T0.f0();
            fail("expected java.security.AccessControlException");
        }
        catch(java.security.AccessControlException e){
            // expected behavior
        }
        catch(Exception e){
            fail("expected java.security.AccessControlException, got "+e.getClass().getName());
        }
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkPermission does not throw a SecurityException " +
                    "if all classes on the call stack refer to a protection domain " +
                    "which contains the necessary permissions.",
            method = "checkPermission",
            args = {Permission.class}
        )
    })
    public void test_do_privileged2() {
        // add TestPermission to T0, T1, T2
        c0.add(p);
        c1.add(p);
        c2.add(p);
        setProtectionDomain(T0.class, new ProtectionDomain(codeSource, c0));
        setProtectionDomain(T1.class, new ProtectionDomain(codeSource, c1));
        setProtectionDomain(T2.class, new ProtectionDomain(codeSource, c2));

        System.setSecurityManager(new SecurityManager());
        try {
            String res = T0.f0();
            assertEquals("ok", res);
        }
        catch(java.security.AccessControlException e){
            fail("expected no java.security.AccessControlException");
        }
        catch(Exception e){
            fail("expected no exception, got "+e.getClass().getName());
        }
    }

    @TestTargets({
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Verifies that checkPermission does not throw a SecurityException " +
                        "if a method call is performed with doPrivileged, even if not all " +
                        "classes beyond the doPrivileged call have the necessary permissions " +
                        "set in their protection domains.",
                method = "checkPermission",
                args = {Permission.class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Verifies that checkPermission does not throw a SecurityException " +
                        "if a method call is performed with doPrivileged, even if not all " +
                        "classes beyond the doPrivileged call have the necessary permissions " +
                        "set in their protection domains.",
                method = "doPrivileged",
                args = {PrivilegedAction.class}
        )
    })
    public void test_do_privileged3() {
        // add TestPermission to T1 and T2, and call it with doPrivileged from T1
        c1.add(p);
        c2.add(p);
        setProtectionDomain(T0.class, new ProtectionDomain(codeSource, c0));
        setProtectionDomain(T1.class, new ProtectionDomain(codeSource, c1));
        setProtectionDomain(T2.class, new ProtectionDomain(codeSource, c2));
        
        System.setSecurityManager(new SecurityManager());
        try {
            String res = T0.f0_priv();
            assertEquals("ok", res);
        }
        catch(java.security.AccessControlException e){
            fail("expected no java.security.AccessControlException");
        }
        catch(Exception e){
            fail("expected no exception, got "+e.getClass().getName());
        }
    }
    
    static class T0 {
        static String f0(){
            return T1.f1();
        }
        static String f0_priv(){
            return T1.f1_priv();
        }
    }
    
    static class T1 {
        static String f1(){
            return T2.f2();
        }
        static String f1_priv(){
            return AccessController.doPrivileged(
                new PrivilegedAction<String>(){
                    public String run() {
                        return T2.f2();
                    }
                }
            );
        }
    }
    
    static class T2 {
        static String f2(){
            SecurityManager s = System.getSecurityManager();
            assertNotNull(s);
            s.checkPermission(new TestPermission());
            return "ok";
        }
    }
    
    static class TestPermission extends BasicPermission {
        private static final long serialVersionUID = 1L;

        public TestPermission(){ super("TestPermission"); }
    
        @Override
        public boolean implies(Permission permission) {
            return permission instanceof TestPermission;
        }
    }
    
}

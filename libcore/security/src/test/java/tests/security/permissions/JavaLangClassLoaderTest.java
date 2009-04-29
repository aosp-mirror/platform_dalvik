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

package tests.security.permissions;

import dalvik.annotation.AndroidOnly;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.system.DexFile;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Permission;

import tests.support.Support_ClassLoader;
import tests.support.resource.Support_Resources;

/*
 * This class tests the security permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.lang.ClassLoader
 */
@TestTargetClass(java.lang.ClassLoader.class)
public class JavaLangClassLoaderTest extends TestCase {
    
    SecurityManager old;

    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader constructor calls checkCreateClassLoader on security manager.",
            method = "ClassLoader",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader constructor calls checkCreateClassLoader on security manager.",
            method = "ClassLoader",
            args = {java.lang.ClassLoader.class}
        )
    })
    public void test_ClassLoaderCtor () {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            void reset(){
                called = false;
            }
            @Override
            public void checkCreateClassLoader(){
                called = true;
            }
            @Override
            public void checkPermission(Permission p) {
                
            }
        }
        
        // class MyClassLoader defined package visible constructors
        class MyClassLoader extends ClassLoader { 
            MyClassLoader(){super();}
            MyClassLoader(ClassLoader parent){super(parent);}            
        }

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        ClassLoader c1 = new MyClassLoader();
        assertTrue("ClassLoader ctor must call checkCreateClassLoader on security manager", s.called);

        s.reset();
        ClassLoader c2 = new MyClassLoader(c1);
        assertTrue("ClassLoader ctor must call checkCreateClassLoader on security manager", s.called);
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader.getSystemClassLoader() calls checkPermission on security manager.",
            method = "getSystemClassLoader",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader.getParent() calls checkPermission on security manager.",
            method = "getParent",
            args = {}
        )
    })
    @AndroidOnly("uses DexFile")
    @BrokenTest("Endless loop in ClassLoader. Actually a known failure.")
    public void test_getSystemClassLoader () throws IOException,
            IllegalAccessException, InstantiationException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof RuntimePermission &&
                        "getClassLoader".equals(permission.getName())){
                    called = true;
                }
            }
        }
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        File tempFile = Support_Resources.createTempFile(".jar");
        tempFile.delete();
        
        /*
         * The testdex.jar contains the following two classes:
         * 
         * package tests.security.permissions.resources;
         * 
         * public class TestClass1 {
         * 
         *      public TestClass1() {
         *          ClassLoader.getSystemClassLoader();
         *      }
         *  }
         *
         * package tests.security.permissions.resources;
         * 
         *  public class TestClass2 {
         *
         *      public TestClass2 () {
         *          getClass().getClassLoader().getParent();
         *      }
         *  }
         */
        
        InputStream is = Support_Resources.getResourceStream("testdex.jar");
        Support_Resources.copyLocalFileto(tempFile, is);
        DexFile dexfile = new DexFile(tempFile);
        ClassLoader pcl = Support_ClassLoader.getInstance(new URL(""),
                ClassLoader.getSystemClassLoader());
        
        Class<?> testClass = dexfile.loadClass(
                "tests/security/permissions/resources/TestClass1", pcl);
        
        assertNotNull("failed to load TestlClass1", testClass);
        
        s.reset();
        testClass.newInstance();
        
        assertTrue("ClassLoader.getSystemClassLoader() must call "
                + "checkPermission on security manager", s.called);
       
        testClass = dexfile.loadClass(
                "tests/security/permissions/resources/TestClass2", pcl);
        
        assertNotNull("failed to load TestClass2", testClass);
        s.reset();
        
        testClass.newInstance();

        assertTrue("Method getParent on a class loader must call "
                + "checkPermission on security manager", s.called);
    }
}


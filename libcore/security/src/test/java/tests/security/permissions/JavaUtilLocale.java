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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.security.Permission;
import java.util.Locale;
import java.util.PropertyPermission;
/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.util.Locale
 */
@TestTargetClass(java.util.Locale.class)
public class JavaUtilLocale extends TestCase {
    
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
    
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that java.util.Locale.setDefault(Locale) method calls checkPermission on security manager.",
        method = "setDefault",
        args = {java.util.Locale.class}
    )
    public void test_setDefault() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof PropertyPermission 
                        && "user.language".equals(permission.getName())
                        && "write".equals(permission.getActions())){
                    called = true;              
                }
                super.checkPermission(permission);
            }
        }
        
        Locale loc = Locale.getDefault();
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Locale.setDefault(loc);
        assertTrue("java.util.Locale.setDefault(Locale) must call checkPermission on security permissions", s.called);
    }
}

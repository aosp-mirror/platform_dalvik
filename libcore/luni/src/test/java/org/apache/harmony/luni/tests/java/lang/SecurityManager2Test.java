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

import java.security.Permission;

import junit.framework.TestCase;
import tests.support.Support_Exec;

public class SecurityManager2Test extends TestCase {

    public void test_SecurityManager_via_SystemProperty() throws Exception {
        String[] arg = new String[] {
                "-Djava.security.manager=" + MySecurityManager.class.getName(),
                TestForSystemProperty.class.getName() };

        Support_Exec.execJava(arg, null, true);
    }

    public static class TestForSystemProperty {

        public static void main(String[] args) {
            assertEquals(MySecurityManager.class, System.getSecurityManager()
                    .getClass());
        }
    }

    /**
     * Custom security manager
     */
    public static class MySecurityManager extends SecurityManager {
        public void checkPermission(Permission perm) {
        }
    }
}

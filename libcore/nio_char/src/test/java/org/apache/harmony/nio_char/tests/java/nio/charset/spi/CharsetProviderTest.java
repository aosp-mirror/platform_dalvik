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

package org.apache.harmony.nio_char.tests.java.nio.charset.spi;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.security.Permission;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Test class java.nio.charset.spi.CharsetProvider.
 */
public class CharsetProviderTest extends TestCase {

    /*
     * Test the security check in the constructor.
     */
    public void testConstructor() {
        // with sufficient privilege
        new MockCharsetProvider();

        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        // set a normal value
        try {
            new MockCharsetProvider();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Test the signature.
     */
    static class MockCharsetProvider extends CharsetProvider {

        public Charset charsetForName(String charsetName) {
            return null;
        }

        public Iterator charsets() {
            return null;
        }
    }

    /*
     * Used to grant all permissions except charset provider access.
     */
    public static class MockSecurityManager extends SecurityManager {

        public MockSecurityManager() {
        }

        public void checkPermission(Permission perm) {
            // grant all permissions except logging control
            if (perm instanceof RuntimePermission) {
                RuntimePermission rp = (RuntimePermission) perm;
                if (rp.getName().equals("charsetProvider")) {
                    throw new SecurityException();
                }
            }
        }

        public void checkPermission(Permission perm, Object context) {
            // grant all permissions except logging control
            if (perm instanceof RuntimePermission) {
                RuntimePermission rp = (RuntimePermission) perm;
                if (rp.getName().equals("charsetProvider")) {
                    throw new SecurityException();
                }
            }
        }
    }

}

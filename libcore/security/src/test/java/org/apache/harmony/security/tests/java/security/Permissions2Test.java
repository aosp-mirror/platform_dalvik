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

package org.apache.harmony.security.tests.java.security;

import java.io.File;
import java.io.FilePermission;
import java.security.Permissions;
import java.util.Enumeration;

public class Permissions2Test extends junit.framework.TestCase {
    FilePermission readAllFiles = new FilePermission("<<ALL FILES>>", "read");

    FilePermission alsoReadAllFiles = new FilePermission("<<ALL FILES>>",
            "read");

    FilePermission allInCurrent = new FilePermission("*",
            "read, write, execute,delete");

    FilePermission readInCurrent = new FilePermission("*", "read");

    FilePermission readInFile = new FilePermission("aFile.file", "read");

    /**
     * @tests java.security.Permissions#Permissions()
     */
    public void test_Constructor() {
        // Test for method java.security.Permissions()
        new Permissions();
    }

    /**
     * @tests java.security.Permissions#add(java.security.Permission)
     */
    public void test_addLjava_security_Permission() {
        // Test for method void
        // java.security.Permissions.add(java.security.Permission)
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[7];
        perm[0] = readAllFiles;
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = alsoReadAllFiles;
        perm[5] = readInFile;
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }

        Enumeration e = perms.elements();
        FilePermission perm2[] = new FilePermission[10];
        int i = 0;
        while (e.hasMoreElements()) {
            perm2[i] = (FilePermission) e.nextElement();
            i++;
        }
        assertEquals("Permissions.elements did not return the correct number "
                + "of permission - called in add() test ", i, perm.length);
    }

    /**
     * @tests java.security.Permissions#elements()
     */
    public void test_elements() {
        // Test for method java.util.Enumeration
        // java.security.Permissions.elements()
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[7];
        perm[0] = readAllFiles;
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = alsoReadAllFiles;
        perm[5] = readInFile;
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }
        Enumeration e = perms.elements();
        FilePermission perm2[] = new FilePermission[10];
        int i = 0;
        while (e.hasMoreElements()) {
            perm2[i] = (FilePermission) e.nextElement();
            i++;
        }
        assertEquals("Permissions.elements did not return the correct "
                + "number of permission - called in element() test", i,
                perm.length);
    }

    /**
     * @tests java.security.Permissions#implies(java.security.Permission)
     */
    public void test_impliesLjava_security_Permission() {
        // Test for method boolean
        // java.security.Permissions.implies(java.security.Permission)
        char s = File.separatorChar;
        FilePermission perm[] = new FilePermission[7];
        perm[0] = new FilePermission("test1.file", "write");
        perm[1] = allInCurrent;
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = new FilePermission(s + "windows" + "*", "delete");
        perm[5] = readInFile;
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }
        assertTrue("Returned true for non-subset of files", !perms
                .implies(new FilePermission("<<ALL FILES>>", "execute")));
        assertTrue("Returned true for non-subset of action", !perms
                .implies(new FilePermission(s + "tmp" + s + "test" + s + "*",
                        "execute")));
        assertTrue("Returned false for subset of actions", perms
                .implies(new FilePermission("*", "write")));
        assertTrue("Returned false for subset of files", perms
                .implies(new FilePermission(s + "tmp" + s + "test" + s
                        + "test.file", "read")));
        assertTrue("Returned false for subset of files and actions", perms
                .implies(new FilePermission(s + "tmp" + s + "test" + s
                        + "test2.file", "write")));
    }
}
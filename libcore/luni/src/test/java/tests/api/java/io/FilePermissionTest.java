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

package tests.api.java.io;

import java.io.File;
import java.io.FilePermission;
import java.security.PermissionCollection;

public class FilePermissionTest extends junit.framework.TestCase {

	FilePermission readAllFiles = new FilePermission("<<ALL FILES>>", "read");

	FilePermission alsoReadAllFiles = new FilePermission("<<ALL FILES>>",
			"read");

	FilePermission allInCurrent = new FilePermission("*",
			"read, write, execute,delete");

	FilePermission readInCurrent = new FilePermission("*", "read");

	FilePermission readInFile = new FilePermission("aFile.file", "read");

	/**
	 * @tests java.io.FilePermission#FilePermission(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_String() {
		// Test for method java.io.FilePermission(java.lang.String,
		// java.lang.String)
		assertTrue("Used to test", true);
		FilePermission constructFile = new FilePermission("test constructor",
				"write");
		assertEquals("action given to the constructor did not correspond - constructor failed",
				"write", constructFile.getActions());
		assertTrue(
				"name given to the construcotr did not correspond - construcotr failed",
				constructFile.getName() == "test constructor");

        // Regression test for HARMONY-1050
        try {
            new FilePermission(null, "drink");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            new FilePermission(null, "read");
            fail("Expected NPE");
        } catch (NullPointerException e) {
            // Expected
        }
        
        try {
            new FilePermission(null, null);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            // Expected
        }
	}

	/**
	 * @tests java.io.FilePermission#getActions()
	 */
	public void test_getActions() {
		// Test for method java.lang.String java.io.FilePermission.getActions()
		assertEquals("getActions should have returned only read", "read", readAllFiles
				.getActions());
		assertEquals("getActions should have returned all actions", "read,write,execute,delete", allInCurrent
				.getActions());
	}

	/**
	 * @tests java.io.FilePermission#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// test for method java.io.FilePermission.equals()
		assertTrue(
				"returned false when two instance of FilePermission is equal",
				readAllFiles.equals(alsoReadAllFiles));
		assertTrue(
				"returned true when two instance	of FilePermission is not equal",
				!(readInCurrent.equals(readInFile)));
	}

	/**
	 * @tests java.io.FilePermission#implies(java.security.Permission)
	 */
	public void test_impliesLjava_security_Permission() {
		// Test for method boolean
		// java.io.FilePermission.implies(java.security.Permission)
		assertTrue("Returned true for non-subset of actions", !readAllFiles
				.implies(allInCurrent));
		assertTrue("Returned true for non-subset of files", !allInCurrent
				.implies(readAllFiles));
		assertTrue("Returned false for subset of actions", allInCurrent
				.implies(readInCurrent));
		assertTrue("Returned false for subset of files", readAllFiles
				.implies(readInCurrent));
		assertTrue("Returned false for subset of files and actions",
				allInCurrent.implies(readInFile));
		assertTrue("Returned false for equal FilePermissions", readAllFiles
				.implies(alsoReadAllFiles));

		FilePermission fp3 = new FilePermission("/bob/*".replace('/',
				File.separatorChar), "read,write");
		FilePermission fp4 = new FilePermission("/bob/".replace('/',
				File.separatorChar), "write");
		assertTrue("returned true for same dir using * and not *", !fp3
				.implies(fp4));
		FilePermission fp5 = new FilePermission("/bob/file".replace('/',
				File.separatorChar), "write");
		assertTrue("returned false for same dir using * and file", fp3
				.implies(fp5));

		FilePermission fp6 = new FilePermission("/bob/".replace('/',
				File.separatorChar), "read,write");
		FilePermission fp7 = new FilePermission("/bob/*".replace('/',
				File.separatorChar), "write");
		assertTrue("returned false for same dir using not * and *", !fp6
				.implies(fp7));
		assertTrue("returned false for same subdir", fp6.implies(fp4));

		FilePermission fp8 = new FilePermission("/".replace('/',
				File.separatorChar), "read,write");
		FilePermission fp9 = new FilePermission("/".replace('/',
				File.separatorChar), "write");
		assertTrue("returned false for same dir", fp8.implies(fp9));

		FilePermission fp10 = new FilePermission("/".replace('/',
				File.separatorChar), "read,write");
		FilePermission fp11 = new FilePermission("/".replace('/',
				File.separatorChar), "write");
		assertTrue("returned false for same dir", fp10.implies(fp11));

		FilePermission fp12 = new FilePermission("/*".replace('/',
				File.separatorChar), "read,write");
		assertTrue("returned false for same dir using * and dir", !fp12
				.implies(fp10));
	}

	/**
	 * @tests java.io.FilePermission#newPermissionCollection()
	 */
	public void test_newPermissionCollection() {
		// test for method java.io.FilePermission.newPermissionCollection
		char s = File.separatorChar;
		FilePermission perm[] = new FilePermission[4];
		perm[0] = readAllFiles;
		perm[1] = allInCurrent;
		perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
				"read,write");
		perm[3] = new FilePermission(s + "tmp" + s + "test" + s
				+ "collection.file", "read");

		PermissionCollection collect = perm[0].newPermissionCollection();
		for (int i = 0; i < perm.length; i++) {
			collect.add(perm[i]);
		}
		assertTrue("returned false for subset of files", collect
				.implies(new FilePermission("*", "write")));
		assertTrue("returned false for subset of name and action", collect
				.implies(new FilePermission(s + "tmp", "read")));
		assertTrue("returned true for non subset of file and action", collect
				.implies(readInFile));

		FilePermission fp1 = new FilePermission("/tmp/-".replace('/',
				File.separatorChar), "read");
		PermissionCollection fpc = fp1.newPermissionCollection();
		fpc.add(fp1);
		fpc.add(new FilePermission("/tmp/scratch/foo/*".replace('/',
				File.separatorChar), "write"));
		FilePermission fp2 = new FilePermission("/tmp/scratch/foo/file"
				.replace('/', File.separatorChar), "read,write");
		assertTrue("collection does not collate", fpc.implies(fp2));
	}

	/**
	 * @tests java.io.FilePermission#hashCode()
	 */
	public void test_hashCode() {
		// test method java.io.FilePermission.hasCode()
		assertTrue(
				"two equal filePermission instances returned different hashCode",
				readAllFiles.hashCode() == alsoReadAllFiles.hashCode());
		assertTrue(
				"two filePermission instances with same permission name returned same hashCode",
				readInCurrent.hashCode() != allInCurrent.hashCode());

	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}

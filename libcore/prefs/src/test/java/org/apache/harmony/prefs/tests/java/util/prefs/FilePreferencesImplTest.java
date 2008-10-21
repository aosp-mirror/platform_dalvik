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

package org.apache.harmony.prefs.tests.java.util.prefs;

import java.io.FilePermission;
import java.io.IOException;
import java.security.Permission;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

public class FilePreferencesImplTest extends TestCase {

    private String prevFactory;
	private Preferences uroot;
	private Preferences sroot;
    
    public FilePreferencesImplTest() {
        super();
    }
    
    protected void setUp() throws Exception {
     //   prevFactory = System.getProperty("java.util.prefs.PreferencesFactory");
    //    System.setProperty("java.util.prefs.PreferencesFactory", "java.util.prefs.FilePreferencesFactoryImpl");
        
	//	uroot = (AbstractPreferences) Preferences.userRoot();
        uroot = Preferences.userRoot();
        sroot = Preferences.systemRoot();
    }
    
    protected void tearDown() throws Exception {
        if (prevFactory != null)
            System.setProperty("java.util.prefs.PreferencesFactory", prevFactory);
        
        uroot = null;
        sroot = null;
    }

	public void testPutGet() throws IOException, BackingStoreException {
		uroot.put("ukey1", "value1");
		assertEquals("value1", uroot.get("ukey1", null));
		String[] names = uroot.keys();
		assertTrue(names.length >= 1);

		uroot.put("ukey2", "value3");
		assertEquals("value3", uroot.get("ukey2", null));
		uroot.put("\u4e2d key1", "\u4e2d value1");
		assertEquals("\u4e2d value1", uroot.get("\u4e2d key1", null));
		names = uroot.keys();
		assertEquals(3, names.length);

		uroot.clear();
		names = uroot.keys();
		assertEquals(0, names.length);

		sroot.put("skey1", "value1");
		assertEquals("value1", sroot.get("skey1", null));
		sroot.put("\u4e2d key1", "\u4e2d value1");
		assertEquals("\u4e2d value1", sroot.get("\u4e2d key1", null));
	}

	public void testChildNodes() throws Exception {
		Preferences child1 = uroot.node("child1");
		Preferences child2 = uroot.node("\u4e2d child2");
		Preferences grandchild = child1.node("grand");
        assertNotNull(grandchild);

		String[] childNames = uroot.childrenNames();
		assertEquals(4, childNames.length);
		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}

		childNames = child1.childrenNames();
		assertEquals(1, childNames.length);
		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}

		childNames = child2.childrenNames();
		assertEquals(0, childNames.length);
		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}

		child1.removeNode();
		childNames = uroot.childrenNames();
		assertEquals(3, childNames.length);
		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}
		// child2.removeNode();
		// childNames = uroot.childrenNames();
		// assertEquals(0, childNames.length);

		child1 = sroot.node("child1");
		child2 = sroot.node("child2");
		grandchild = child1.node("grand");

		childNames = sroot.childrenNames();

		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}
	//	assertEquals(2, childNames.length);

		childNames = child1.childrenNames();
		assertEquals(1, childNames.length);
		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}

		childNames = child2.childrenNames();
		assertEquals(0, childNames.length);
		for (int i = 0; i < childNames.length; i++) {
			System.out.println(childNames[i]);
		}
	}

	public void testSecurityException() throws BackingStoreException {
		Preferences child1 = uroot.node("child1");
		MockFileSecurityManager manager = new MockFileSecurityManager();
		manager.install();
		try {
			try {
				uroot.node("securityNode");
				fail("should throw security exception");
			} catch (SecurityException e) {
			}
			try {
				// need FilePermission(delete);
				child1.removeNode();
				fail("should throw security exception");
			} catch (SecurityException e) {
			}
			try {
				uroot.childrenNames();
				fail("should throw security exception");
			} catch (SecurityException e) {
			}
			uroot.keys();
			uroot.put("securitykey", "value1");
			uroot.remove("securitykey");
			try {
				uroot.flush();
				fail("should throw security exception");
			} catch (SecurityException e) {
			} catch (BackingStoreException e) {
				assertTrue(e.getCause() instanceof SecurityException);
			}
			try {
				uroot.sync();
				fail("should throw security exception");
			} catch (SecurityException e) {
			} catch (BackingStoreException e) {
				assertTrue(e.getCause() instanceof SecurityException);
			}
		} finally {
			manager.restoreDefault();
		}
	}

	static class MockFileSecurityManager extends SecurityManager {

		SecurityManager dflt;

		public MockFileSecurityManager() {
			super();
			dflt = System.getSecurityManager();
		}

		public void install() {
			System.setSecurityManager(this);
		}

		public void restoreDefault() {
			System.setSecurityManager(dflt);
		}

		public void checkPermission(Permission perm) {
			if (perm instanceof FilePermission) {
				throw new SecurityException();
			} else if (dflt != null) {
				dflt.checkPermission(perm);
			}
		}

		public void checkPermission(Permission perm, Object ctx) {
			if (perm instanceof FilePermission) {
				System.out.println(perm.getActions());
				throw new SecurityException();
			} else if (dflt != null) {
				dflt.checkPermission(perm, ctx);
			}
		}

	}
}

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * 
 */
public class PreferencesTest extends TestCase {

	MockSecurityManager manager = new MockSecurityManager();

	MockInputStream stream = null;
	
	final static String longKey;
	
	final static String longValue;

	InputStream in;
	static {
		StringBuffer key = new StringBuffer(Preferences.MAX_KEY_LENGTH);
		for (int i = 0; i < Preferences.MAX_KEY_LENGTH; i++) {
			key.append('a');
		}
		longKey = key.toString();
		
		StringBuffer value = new StringBuffer(Preferences.MAX_VALUE_LENGTH);
		for (int i = 0; i < Preferences.MAX_VALUE_LENGTH; i++) {
			value.append('a');
		}
		longValue = value.toString();
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		in = new ByteArrayInputStream(
				"<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\"><preferences><root type=\"user\"><map></map></root></preferences>"
						.getBytes("UTF-8"));
		stream = new MockInputStream(in);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		stream.close();
	}

	public void testSystemNodeForPackage() throws BackingStoreException {
		Preferences p = null;
		try {
			p = Preferences.systemNodeForPackage(Object.class);
		} catch (SecurityException e) {
			// may be caused by absence of privileges on the underlying OS
			return;
		}
		assertEquals("/java/lang", p.absolutePath());
		assertTrue(p instanceof AbstractPreferences);
		Preferences root = Preferences.systemRoot();
		Preferences parent = root.node("java");
		assertSame(parent, p.parent());
		assertFalse(p.isUserNode());
		assertEquals("lang", p.name());
		assertEquals("System Preference Node: " + p.absolutePath(), p
				.toString());
		try {
			assertEquals(0, p.childrenNames().length);
		} catch (BackingStoreException e) {
			// could be thrown according to specification
		}
		try {
			assertEquals(0, p.keys().length);
		} catch (BackingStoreException e) {
			// could be thrown according to specification
		}

		try {
			p = Preferences.userNodeForPackage(null);
			fail("NullPointerException has not been thrown");
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testSystemRoot() throws BackingStoreException {
		Preferences p = Preferences.systemRoot();
		assertTrue(p instanceof AbstractPreferences);
		assertEquals("/", p.absolutePath());
		assertSame(null, p.parent());
		assertFalse(p.isUserNode());
		assertEquals("", p.name());
		assertEquals("System Preference Node: " + p.absolutePath(), p
				.toString());
		// assertEquals(0, p.childrenNames().length);
		// assertEquals(0, p.keys().length);
	}

	public void testConsts() {
		assertEquals(80, Preferences.MAX_KEY_LENGTH);
		assertEquals(80, Preferences.MAX_NAME_LENGTH);
		assertEquals(8192, Preferences.MAX_VALUE_LENGTH);
	}

	public void testUserNodeForPackage() throws BackingStoreException {
		Preferences p = Preferences.userNodeForPackage(Object.class);
		assertEquals("/java/lang", p.absolutePath());
		assertTrue(p instanceof AbstractPreferences);
		Preferences root = Preferences.userRoot();
		Preferences parent = root.node("java");
		assertSame(parent, p.parent());
		assertTrue(p.isUserNode());
		assertEquals("lang", p.name());
		assertEquals("User Preference Node: " + p.absolutePath(), p.toString());
		assertEquals(0, p.childrenNames().length);
		assertEquals(0, p.keys().length);

		try {
			p = Preferences.userNodeForPackage(null);
			fail();
		} catch (NullPointerException e) {
		}
	}

	public void testUserRoot() throws BackingStoreException {
		Preferences p = Preferences.userRoot();
		assertTrue(p instanceof AbstractPreferences);
		assertEquals("/", p.absolutePath());
		assertSame(null, p.parent());
		assertTrue(p.isUserNode());
		assertEquals("", p.name());
		assertEquals("User Preference Node: " + p.absolutePath(), p.toString());
		// assertEquals(0, p.childrenNames().length);
		// assertEquals(p.keys().length, 0);
	}

	public void testImportPreferences() throws Exception {
		Preferences prefs = null;
		try {
			prefs = Preferences.userNodeForPackage(PreferencesTest.class);
			// assertEquals(0, prefs.childrenNames().length);
			// assertFalse(prefs.nodeExists("mock/child/grandson"));

			prefs.put("prefskey", "oldvalue");
			prefs.put("prefskey2", "oldvalue2");
			in = getClass().getResourceAsStream("/prefs/java/util/prefs/userprefs.xml");
			Preferences.importPreferences(in);

			prefs = Preferences.userNodeForPackage(PreferencesTest.class);
			assertEquals(1, prefs.childrenNames().length);
			assertTrue(prefs.nodeExists("mock/child/grandson"));
			assertEquals("newvalue", prefs.get("prefskey", null));
			assertEquals("oldvalue2", prefs.get("prefskey2", null));
			assertEquals("newvalue3", prefs.get("prefskey3", null));

			in = PreferencesTest.class
					.getResourceAsStream("/prefs/java/util/prefs/userprefs-badform.xml");
			try {
				Preferences.importPreferences(in);
				fail();
			} catch (InvalidPreferencesFormatException e) {
			}

			in = PreferencesTest.class
					.getResourceAsStream("/prefs/java/util/prefs/userprefs-badtype.xml");
			try {
				Preferences.importPreferences(in);
				fail();
			} catch (InvalidPreferencesFormatException e) {
			}

	//		in = PreferencesTest.class
	//				.getResourceAsStream("/prefs/java/util/prefs/userprefs-badencoding.xml");
	//		try {
	//			Preferences.importPreferences(in);
	//			fail();
	//		} catch (InvalidPreferencesFormatException e) {
	//		}

			in = PreferencesTest.class
					.getResourceAsStream("/prefs/java/util/prefs/userprefs-higherversion.xml");
			try {
				Preferences.importPreferences(in);
				fail();
			} catch (InvalidPreferencesFormatException e) {
			}

			in = PreferencesTest.class
					.getResourceAsStream("/prefs/java/util/prefs/userprefs-ascii.xml");
			Preferences.importPreferences(in);
			prefs = Preferences.userNodeForPackage(PreferencesTest.class);
		} finally {
			try {
				prefs = Preferences.userRoot().node("tests");
				prefs.removeNode();
			} catch (Exception e) {
			}
		}
	}

	public void testImportPreferencesException() throws Exception {
		try {
			Preferences.importPreferences(null);
			fail();
		} catch (MalformedURLException e) {
		}

		byte[] source = new byte[0];
		InputStream in = new ByteArrayInputStream(source);
		try {
			Preferences.importPreferences(in);
			fail();
		} catch (InvalidPreferencesFormatException e) {
		}

		stream.setResult(MockInputStream.exception);
		try {
			Preferences.importPreferences(stream);
			fail();
		} catch (IOException e) {
		}

		stream.setResult(MockInputStream.runtimeException);
		try {
			Preferences.importPreferences(stream);
			fail();
		} catch (RuntimeException e) {
		}
	}

	public void testSecurity() throws InvalidPreferencesFormatException,
			IOException {
		try {
			manager.install();
			try {
				Preferences.userRoot();
				fail();
			} catch (SecurityException e) {
			}
			try {
				Preferences.systemRoot();
				fail();
			} catch (SecurityException e) {
			}
			try {
				Preferences.userNodeForPackage(null);
				fail();
			} catch (SecurityException e) {
			}

			try {
				Preferences.systemNodeForPackage(null);
				fail();
			} catch (SecurityException e) {
			}

			try {
				Preferences.importPreferences(stream);
				fail();
			} catch (SecurityException e) {
			}
		} finally {
			manager.restoreDefault();
		}
	}

	public void testAbstractMethods() {
		Preferences p = new MockPreferences();
		p.absolutePath();
		try {
			p.childrenNames();
		} catch (BackingStoreException e4) {
		}
		try {
			p.clear();
		} catch (BackingStoreException e5) {
		}
		try {
			p.exportNode(null);
		} catch (IOException e6) {
		} catch (BackingStoreException e6) {
		}
		try {
			p.exportSubtree(null);
		} catch (IOException e7) {
		} catch (BackingStoreException e7) {
		}
		try {
			p.flush();
		} catch (BackingStoreException e8) {
		}
		p.get(null, null);
		p.getBoolean(null, false);
		p.getByteArray(null, null);
		p.getFloat(null, 0.1f);
		p.getDouble(null, 0.1);
		p.getInt(null, 1);
		p.getLong(null, 1l);
		p.isUserNode();
		try {
			p.keys();
		} catch (BackingStoreException e) {
		}
		p.name();
		p.node(null);
		try {
			p.nodeExists(null);
		} catch (BackingStoreException e1) {
		}
		p.parent();
		p.put(null, null);
		p.putBoolean(null, false);
		p.putByteArray(null, null);
		p.putDouble(null, 1);
		p.putFloat(null, 1f);
		p.putInt(null, 1);
		p.putLong(null, 1l);
		p.remove(null);
		try {
			p.removeNode();
		} catch (BackingStoreException e2) {
		}
		p.addNodeChangeListener(null);
		p.addPreferenceChangeListener(null);
		p.removeNodeChangeListener(null);
		p.removePreferenceChangeListener(null);
		try {
			p.sync();
		} catch (BackingStoreException e3) {
		}
		p.toString();
	}

	public void testConstructor() {
		MockPreferences mp = new MockPreferences();
		assertEquals(mp.getClass(), MockPreferences.class);
	}

	public void testToString() {
		Preferences p1 = Preferences.userNodeForPackage(Preferences.class);
		assertNotNull(p1.toString());

		Preferences p2 = Preferences.systemRoot();
		assertNotNull(p2.toString());

		Preferences p3 = Preferences.userRoot();
		assertNotNull(p3.toString());
	}
	/**
	 * @test java.util.prefs.Preferences#absolutePath()
	 *
	 */
	public void testAbsolutePath() {
		Preferences p = Preferences.userNodeForPackage(Preferences.class);
		assertEquals("/java/util/prefs", p.absolutePath());

	}

	/**
	 * @test java.util.prefs.Preferences#childrenNames()
	 *
	 */
	public void testChildrenNames() throws BackingStoreException {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);

		Preferences child1 = pref.node("child1");

		pref.node("child2");
		pref.node("child3");
		child1.node("subchild1");

		assertSame(pref, child1.parent());
		assertEquals(4, pref.childrenNames().length);
		assertEquals("child1", pref.childrenNames()[0]);
		assertEquals(1, child1.childrenNames().length);
		assertEquals("subchild1", child1.childrenNames()[0]);
	}

	/**
	 * @test java.util.prefs.Preferences#clear()
	 *
	 */
	public void testClear() throws BackingStoreException {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		pref.put("testClearKey", "testClearValue");
		pref.put("testClearKey1", "testClearValue1");
		assertEquals("testClearValue", pref.get("testClearKey", null));
		assertEquals("testClearValue1", pref.get("testClearKey1", null));
		pref.clear();
		assertNull(pref.get("testClearKey", null));
		assertNull(pref.get("testClearKey1", null));
	}	

	/**
	 * @test java.util.prefs.Preferences#get(String key, String def)
	 *
	 */
	public void testGet() throws BackingStoreException {
		Preferences root = Preferences.userNodeForPackage(Preferences.class);
		Preferences pref = root.node("mock");
		assertNull(pref.get("", null));
		assertEquals("default", pref.get("key", "default"));
		assertNull(pref.get("key", null));
		pref.put("testGetkey", "value");
		assertNull(pref.get("testGetKey", null));
		assertEquals("value", pref.get("testGetkey", null));

		try {
			pref.get(null, "abc");
			fail();
		} catch (NullPointerException e) {
		}
		pref.get("", "abc");
		pref.get("key", null);
		pref.get("key", "");
		pref.putFloat("floatKey", 1.0f);
		assertEquals("1.0", pref.get("floatKey", null));

		pref.removeNode();
		try {
			pref.get("key", "abc");
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.get(null, "abc");
			fail();
		} catch (NullPointerException e) {
		}
	}

	/**
	 * @test java.util.prefs.Preferences#getBoolean(String key, boolean def)
	 *
	 */
	public void testGetBoolean() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.getBoolean(null, false);
			fail();
		} catch (NullPointerException e) {
		}

		pref.put("testGetBooleanKey", "false");
		pref.put("testGetBooleanKey2", "value");
		assertFalse(pref.getBoolean("testGetBooleanKey", true));
		assertTrue(pref.getBoolean("testGetBooleanKey2", true));
	}
	
	/**
	 * @test java.util.prefs.Preferences#getByteArray(String key, byte[] def)
	 *
	 */
	public void testGetByteArray() throws UnsupportedEncodingException {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.getByteArray(null, new byte[0]);
			fail();
		} catch (NullPointerException e) {
		}
		byte[] b64Array = new byte[] { 0x59, 0x57, 0x4a, 0x6a };// BASE64

		pref.put("testGetByteArrayKey", "abc=");
		pref.put("testGetByteArrayKey2", new String(b64Array));
		pref.put("invalidKey", "<>?");
		assertTrue(Arrays.equals(new byte[] { 105, -73 }, pref.getByteArray(
				"testGetByteArrayKey", new byte[0])));
		assertTrue(Arrays.equals(new byte[] { 'a', 'b', 'c' }, pref
				.getByteArray("testGetByteArrayKey2", new byte[0])));
		assertTrue(Arrays.equals(new byte[0], pref.getByteArray("invalidKey",
				new byte[0])));

		pref.putByteArray("testGetByteArrayKey3", b64Array);
		pref.putByteArray("testGetByteArrayKey4", "abc".getBytes());
		assertTrue(Arrays.equals(b64Array, pref.getByteArray(
				"testGetByteArrayKey3", new byte[0])));
		assertTrue(Arrays.equals("abc".getBytes(), pref.getByteArray(
				"testGetByteArrayKey4", new byte[0])));
	}
	
	/**
	 * @test java.util.prefs.Preferences#getDouble(String key, double def)
	 *
	 */
	public void testGetDouble() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.getDouble(null, 0);
			fail();
		} catch (NullPointerException e) {
		}

		pref.put("testGetDoubleKey", "1");
		pref.put("testGetDoubleKey2", "value");
		pref.putDouble("testGetDoubleKey3", 1);
		pref.putInt("testGetDoubleKey4", 1);
		assertEquals(1.0, pref.getDouble("testGetDoubleKey", 0.0), 0);
		assertEquals(0.0, pref.getDouble("testGetDoubleKey2", 0.0), 0);
		assertEquals(1.0, pref.getDouble("testGetDoubleKey3", 0.0), 0);
		assertEquals(1.0, pref.getDouble("testGetDoubleKey4", 0.0), 0);
	}

	/**
	 * @test java.util.prefs.Preferences#getFloat(String key, float def)
	 *
	 */
	public void testGetFloat() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.getFloat(null, 0f);
			fail();
		} catch (NullPointerException e) {
		}
		pref.put("testGetFloatKey", "1");
		pref.put("testGetFloatKey2", "value");
		assertEquals(1f, pref.getFloat("testGetFloatKey", 0f), 0); //$NON-NLS-1$
		assertEquals(0f, pref.getFloat("testGetFloatKey2", 0f), 0);
	}

	/**
	 * @test java.util.prefs.Preferences#getInt(String key, int def)
	 *
	 */
	public void testGetInt() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.getInt(null, 0);
			fail();
		} catch (NullPointerException e) {
		}

		pref.put("testGetIntKey", "1");
		pref.put("testGetIntKey2", "value");
		assertEquals(1, pref.getInt("testGetIntKey", 0));
		assertEquals(0, pref.getInt("testGetIntKey2", 0));
	}

	/**
	 * @test java.util.prefs.Preferences#getLong(String key, long def)
	 *
	 */
	public void testGetLong() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.getLong(null, 0);
			fail();
		} catch (NullPointerException e) {
		}

		pref.put("testGetLongKey", "1");
		pref.put("testGetLongKey2", "value");
		assertEquals(1, pref.getInt("testGetLongKey", 0));
		assertEquals(0, pref.getInt("testGetLongKey2", 0));
	}
	
	/**
	 * @test java.util.prefs.Preferences#isUserNode()
	 *
	 */
	public void testIsUserNode() {
		Preferences pref1 = Preferences.userNodeForPackage(Preferences.class);
		assertTrue(pref1.isUserNode());

		Preferences pref2 = Preferences.systemNodeForPackage(Preferences.class);
		assertFalse(pref2.isUserNode());
	}
	
	/**
	 * @test java.util.prefs.Preferences#keys()
	 *
	 */
	public void testKeys() throws BackingStoreException {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		pref.clear();
		
		pref.put("key0", "value");
		pref.put("key1", "value1");
		pref.put("key2", "value2");
		pref.put("key3", "value3");

		String[] keys = pref.keys();
		assertEquals(4, keys.length);
		for (int i = 0; i < keys.length; i++) {
			assertEquals(0, keys[i].indexOf("key"));
			assertEquals(4, keys[i].length());
		}
	}

	/**
	 * @test java.util.prefs.Preferences#name()
	 *
	 */
	public void testName() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		Preferences child = pref.node("mock");
		assertEquals("mock", child.name());
	}
	
	/**
	 * @test java.util.prefs.Preferences#node(String pathName)
	 *
	 */
	public void testNode() throws BackingStoreException {
		StringBuffer name = new StringBuffer(Preferences.MAX_NAME_LENGTH);
		for (int i = 0; i < Preferences.MAX_NAME_LENGTH; i++) {
			name.append('a');
		}
		String longName = name.toString();
		
		Preferences root = Preferences.userRoot();
		Preferences parent = Preferences
				.userNodeForPackage(Preferences.class);
		Preferences pref = parent.node("mock");
		
		try {
			pref.node(null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			pref.node("/java/util/prefs/");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			pref.node("/java//util/prefs");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			pref.node(longName + "a");
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertNotNull(pref.node(longName));

		assertSame(root, pref.node("/"));

		Preferences prefs = pref.node("/java/util/prefs");
		assertSame(prefs, parent);

		assertSame(pref, pref.node(""));

		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences child = (MockAbstractPreferences) ((MockAbstractPreferences) pref)
				.publicChildSpi("child");
		assertSame(child, pref.node("child"));

		Preferences child2 = pref.node("child2");
		assertSame(child2, ((MockAbstractPreferences) pref)
				.publicChildSpi("child2"));

		Preferences grandchild = pref.node("child/grandchild");
		assertSame(grandchild, child.childSpi("grandchild"));
		assertSame(grandchild, child.cachedChildrenImpl()[0]);
		grandchild.removeNode();
		assertNotSame(grandchild, pref.node("child/grandchild"));

		grandchild = pref.node("child3/grandchild");
		AbstractPreferences[] childs = ((MockAbstractPreferences) pref)
				.cachedChildrenImpl();
		Preferences child3 = child;
		for (int i = 0; i < childs.length; i++) {
			if (childs[i].name().equals("child3")) {
				child3 = childs[i];
				break;
			}
		}
		assertSame(child3, grandchild.parent());
	}

	/**
	 * @test java.util.prefs.Preferences#nodeExists(String pathName)
	 *
	 */
	public void testNodeExists() throws BackingStoreException {

		Preferences parent = Preferences
				.userNodeForPackage(Preferences.class);
		Preferences pref = parent.node("mock");
		
		try {
			pref.nodeExists(null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			pref.nodeExists("/java/util/prefs/");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			pref.nodeExists("/java//util/prefs");
			fail();
		} catch (IllegalArgumentException e) {
		}

		assertTrue(pref.nodeExists("/"));

		assertTrue(pref.nodeExists("/java/util/prefs"));

		assertTrue(pref.nodeExists(""));

		assertFalse(pref.nodeExists("child"));
		Preferences grandchild = pref.node("child/grandchild");
		assertTrue(pref.nodeExists("child"));
		assertTrue(pref.nodeExists("child/grandchild"));
		grandchild.removeNode();
		assertTrue(pref.nodeExists("child"));
		assertFalse(pref.nodeExists("child/grandchild"));
		assertFalse(grandchild.nodeExists(""));

		assertFalse(pref.nodeExists("child2/grandchild"));
		pref.node("child2/grandchild");
		assertTrue(pref.nodeExists("child2/grandchild"));
	}

	/**
	 * @test java.util.prefs.Preferences#parent()
	 *
	 */
	public void testParent() {
		Preferences parent = Preferences
				.userNodeForPackage(Preferences.class);
		Preferences pref = parent.node("mock");
		
		assertSame(parent, pref.parent());
		
	}
	
	/**
	 * @test java.util.prefs.Preferences#put(String key, String value)
	 *
	 */
	public void testPut() throws BackingStoreException {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		pref.put("", "emptyvalue");
		assertEquals("emptyvalue", pref.get("", null));
		pref.put("testPutkey", "value1");
		assertEquals("value1", pref.get("testPutkey", null));
		pref.put("testPutkey", "value2");
		assertEquals("value2", pref.get("testPutkey", null));

		pref.put("", "emptyvalue");
		assertEquals("emptyvalue", pref.get("", null));

		try {
			pref.put(null, "value");
			fail();
		} catch (NullPointerException e) {
		}
		try {
			pref.put("key", null);
			fail();
		} catch (NullPointerException e) {
		}
		pref.put(longKey, longValue);
		try {
			pref.put(longKey + 1, longValue);
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			pref.put(longKey, longValue + 1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		pref.removeNode();
		try {
			pref.put(longKey, longValue + 1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			pref.put(longKey, longValue);
			fail();
		} catch (IllegalStateException e) {
		}
	}

	/**
	 * @test java.util.prefs.Preferences#putBoolean(String key, boolean value)
	 *
	 */
	public void testPutBoolean() {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		try {
			pref.putBoolean(null, false);
			fail();
		} catch (NullPointerException e) {
		}
		pref.putBoolean(longKey, false);
		try {
			pref.putBoolean(longKey + "a", false);
			fail();
		} catch (IllegalArgumentException e) {
		}
		pref.putBoolean("testPutBooleanKey", false);
		assertEquals("false", pref.get("testPutBooleanKey", null));
		assertFalse(pref.getBoolean("testPutBooleanKey", true));
	}
	
	/**
	 * @test java.util.prefs.Preferences#putDouble(String key, double value)
	 *
	 */
	public void testPutDouble() {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		try {
			pref.putDouble(null, 3);
			fail();
		} catch (NullPointerException e) {
		}
		pref.putDouble(longKey, 3);
		try {
			pref.putDouble(longKey + "a", 3);
			fail();
		} catch (IllegalArgumentException e) {
		}
		pref.putDouble("testPutDoubleKey", 3);
		assertEquals("3.0", pref.get("testPutDoubleKey", null));
		assertEquals(3, pref.getDouble("testPutDoubleKey", 0), 0);
	}

	/**
	 * @test java.util.prefs.Preferences#putFloat(String key, float value)
	 *
	 */
	public void testPutFloat() {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		try {
			pref.putFloat(null, 3f);
			fail();
		} catch (NullPointerException e) {
		}
		pref.putFloat(longKey, 3f);
		try {
			pref.putFloat(longKey + "a", 3f);
			fail();
		} catch (IllegalArgumentException e) {
		}
		pref.putFloat("testPutFloatKey", 3f);
		assertEquals("3.0", pref.get("testPutFloatKey", null));
		assertEquals(3f, pref.getFloat("testPutFloatKey", 0), 0);
	}

	/**
	 * @test java.util.prefs.Preferences#putInt(String key, int value)
	 *
	 */
	public void testPutInt() {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		try {
			pref.putInt(null, 3);
			fail();
		} catch (NullPointerException e) {
		}
		pref.putInt(longKey, 3);
		try {
			pref.putInt(longKey + "a", 3);
			fail();
		} catch (IllegalArgumentException e) {
		}
		pref.putInt("testPutIntKey", 3);
		assertEquals("3", pref.get("testPutIntKey", null));
		assertEquals(3, pref.getInt("testPutIntKey", 0));
	}

	/**
	 * @test java.util.prefs.Preferences#putLong(String key, long value)
	 *
	 */
	public void testPutLong() {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		try {
			pref.putLong(null, 3L);
			fail();
		} catch (NullPointerException e) {
		}
		pref.putLong(longKey, 3L);
		try {
			pref.putLong(longKey + "a", 3L);
			fail();
		} catch (IllegalArgumentException e) {
		}
		pref.putLong("testPutLongKey", 3L);
		assertEquals("3", pref.get("testPutLongKey", null));
		assertEquals(3L, pref.getLong("testPutLongKey", 0));
	}
	
	/**
	 * @test java.util.prefs.Preferences#putByteArray(String key, byte[] value)
	 *
	 */
	public void testPutByteArray() {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		try {
			pref.putByteArray(null, new byte[0]);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			pref.putByteArray("testPutByteArrayKey4", null);
			fail();
		} catch (NullPointerException e) {
		}

		pref.putByteArray(longKey, new byte[0]);
		try {
			pref.putByteArray(longKey + "a", new byte[0]);
			fail();
		} catch (IllegalArgumentException e) {
		}
		byte[] longArray = new byte[(int) (Preferences.MAX_VALUE_LENGTH * 0.74)];
		byte[] longerArray = new byte[(int) (Preferences.MAX_VALUE_LENGTH * 0.75) + 1];
		pref.putByteArray(longKey, longArray);
		try {
			pref.putByteArray(longKey, longerArray);
			fail();
		} catch (IllegalArgumentException e) {
		}

		pref.putByteArray("testPutByteArrayKey", new byte[0]);
		assertEquals("", pref.get("testPutByteArrayKey", null));
		assertTrue(Arrays.equals(new byte[0], pref.getByteArray(
				"testPutByteArrayKey", null)));

		pref.putByteArray("testPutByteArrayKey3", new byte[] { 'a', 'b', 'c' });
		assertEquals("YWJj", pref.get("testPutByteArrayKey3", null));
		assertTrue(Arrays.equals(new byte[] { 'a', 'b', 'c' }, pref
				.getByteArray("testPutByteArrayKey3", null)));
	}
	
	/**
	 * @test java.util.prefs.Preferences#remove(String key)
	 *
	 */
	public void testRemove() throws BackingStoreException {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		pref.remove("key");

		pref.put("key", "value");
		assertEquals("value", pref.get("key", null));
		pref.remove("key");
		assertNull(pref.get("key", null));

		pref.remove("key");

		try {
			pref.remove(null);
		} catch (NullPointerException e) {
		}

		pref.removeNode();
		try {
			pref.remove("key");
			fail();
		} catch (IllegalStateException e) {
		}
	}

	/**
	 * @test java.util.prefs.Preferences#removeNode()
	 *
	 */
	public void testRemoveNode() throws BackingStoreException {
		Preferences pref = Preferences
		.userNodeForPackage(Preferences.class);
		Preferences child = pref.node("child");
		Preferences child1 = pref.node("child1");
		Preferences grandchild = child.node("grandchild");

		pref.removeNode();

		assertFalse(child.nodeExists(""));
		assertFalse(child1.nodeExists(""));
		assertFalse(grandchild.nodeExists(""));
		assertFalse(pref.nodeExists(""));
	}
	
	
	/**
	 * @test java.util.prefs.Preferences#addNodeChangeListener(NodeChangeListener ncl)
	 *
	 */
	public void testAddNodeChangeListener() throws BackingStoreException {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.addNodeChangeListener(null);
			fail();
		} catch (NullPointerException e) {
		}

		Preferences child1 = null;
		Preferences child2 = null;
		Preferences child3 = null;
		
		MockNodeChangeListener nl = null;
		// To get existed node doesn't create the change event
		try {
			nl = new MockNodeChangeListener();
			pref.addNodeChangeListener(nl);
			child1 = pref.node("mock1");
			assertEquals(1, nl.getAdded());
			nl.reset();
			child2 = pref.node("mock1");
			assertEquals(0, nl.getAdded());
			nl.reset();
		} finally {
			pref.removeNodeChangeListener(nl);
			child1.removeNode();
		}
		// same listener can be added twice, and must be removed twice
		try {
			nl = new MockNodeChangeListener();
			pref.addNodeChangeListener(nl);
			pref.addNodeChangeListener(nl);
			child1 = pref.node("mock2");
			assertEquals(2, nl.getAdded());
			nl.reset();
		} finally {
			pref.removeNodeChangeListener(nl);
			pref.removeNodeChangeListener(nl);
			child1.removeNode();
		}
		// test remove event
		try {
			nl = new MockNodeChangeListener();
			pref.addNodeChangeListener(nl);
			child1 = pref.node("mock3");
			child1.removeNode();
			assertEquals(1, nl.getRemoved());
			nl.reset();
		} finally {
			pref.removeNodeChangeListener(nl);
		}
		// test remove event with two listeners
		try {
			nl = new MockNodeChangeListener();
			pref.addNodeChangeListener(nl);
			pref.addNodeChangeListener(nl);
			child1 = pref.node("mock6");
			child1.removeNode();
			assertEquals(2, nl.getRemoved());
			nl.reset();
		} finally {
			pref.removeNodeChangeListener(nl);
			pref.removeNodeChangeListener(nl);
		}
		// test add/remove indirect children, or remove several children at the
		// same time
		try {
			nl = new MockNodeChangeListener();
			child1 = pref.node("mock4");
			child1.addNodeChangeListener(nl);
			child2 = pref.node("mock4/mock5");
			assertEquals(1, nl.getAdded());
			nl.reset();
			child3 = pref.node("mock4/mock5/mock6");
			assertEquals(0, nl.getAdded());
			nl.reset();

			child3.removeNode();
			assertEquals(0, nl.getRemoved());
			nl.reset();

			child3 = pref.node("mock4/mock7");
			assertEquals(1, nl.getAdded());
			nl.reset();

			child1.removeNode();
			assertEquals(2, nl.getRemoved());
			nl.reset();
		} finally {
			try {
				child1.removeNode();
			} catch (Exception e) {
			}
		}

	}
	
	/**
	 * @test java.util.prefs.Preferences#addPreferenceChangeListener(PreferenceChangeListener pcl)
	 *
	 */
	public void testAddPreferenceChangeListener() {
		
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		MockPreferenceChangeListener pl = null;
		
		// TODO: start from here
		try {
			pref.addPreferenceChangeListener(null);
			fail();
		} catch (NullPointerException e) {
		}

		// To get existed node doesn't create the change event
		try {
			pl = new MockPreferenceChangeListener();
			pref.addPreferenceChangeListener(pl);
			pref.putInt("mock1", 123);
			assertEquals(1, pl.getChanged());
			pref.putLong("long_key", Long.MAX_VALUE);
			assertEquals(2, pl.getChanged());
			pl.reset();
			try {
				pref.clear();
				assertEquals(2, pl.getChanged());
			} catch(BackingStoreException bse) {
				pl.reset();
				fail("BackingStoreException is thrown");
			}
			pl.reset();
		} finally {
			pref.removePreferenceChangeListener(pl);
			//child1.removeNode();
		}
				      
		// same listener can be added twice, and must be removed twice
		try {
			pl = new MockPreferenceChangeListener();
			pref.addPreferenceChangeListener(pl);
			pref.addPreferenceChangeListener(pl);
			pref.putFloat("float_key", Float.MIN_VALUE);
			assertEquals(2, pl.getChanged());
			pl.reset();
		} finally {
			pref.removePreferenceChangeListener(pl);
			pref.removePreferenceChangeListener(pl);

		}
		// test remove event
		try {
			pl = new MockPreferenceChangeListener();
			pref.addPreferenceChangeListener(pl);
			pref.putDouble("double_key", Double.MAX_VALUE);
			assertEquals(1, pl.getChanged());
			try {
				pref.clear();
				assertEquals(3, pl.getChanged());
			} catch(BackingStoreException bse) {
				fail("BackingStoreException is thrown");
			}
			pl.reset();
		} finally {
			pref.removePreferenceChangeListener(pl);
		}
		// test remove event with two listeners
		try {
			pl = new MockPreferenceChangeListener();
			pref.addPreferenceChangeListener(pl);
			pref.addPreferenceChangeListener(pl);
			pref.putByteArray("byte_array_key", new byte [] {1 ,2 , 3});
			try {
				pref.clear();
				assertEquals(4, pl.getChanged());
			} catch(BackingStoreException bse) {
				fail("BackingStoreException is thrown");
			}
			pl.reset();
		} finally {
			pref.removePreferenceChangeListener(pl);
			pref.removePreferenceChangeListener(pl);
		}

	}
	
	/**
	 * @test java.util.prefs.Preferences#removeNodeChangeListener(NodeChangeListener ncl)
	 *
	 */
	public void testRemoveNodeChangeListener() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.removeNodeChangeListener(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
		MockNodeChangeListener l1 = new MockNodeChangeListener();
		MockNodeChangeListener l2 = new MockNodeChangeListener();
		pref.addNodeChangeListener(l1);
		pref.addNodeChangeListener(l1);

		pref.removeNodeChangeListener(l1);
		pref.removeNodeChangeListener(l1);
		try {
			pref.removeNodeChangeListener(l1);
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			pref.removeNodeChangeListener(l2);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * @test java.util.prefs.Preferences#removePreferenceChangeListener(PreferenceChangeListener pcl)
	 *
	 */
	public void testRemovePreferenceChangeListener() {
		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
		try {
			pref.removePreferenceChangeListener(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
		MockPreferenceChangeListener l1 = new MockPreferenceChangeListener();
		MockPreferenceChangeListener l2 = new MockPreferenceChangeListener();
		pref.addPreferenceChangeListener(l1);
		pref.addPreferenceChangeListener(l1);
		try {
			pref.removePreferenceChangeListener(l2);
			fail();
		} catch (IllegalArgumentException e) {
		}
		pref.removePreferenceChangeListener(l1);
		pref.removePreferenceChangeListener(l1);
		try {
			pref.removePreferenceChangeListener(l1);
			fail();
		} catch (IllegalArgumentException e) {
		}

	}
	
	static class MockInputStream extends InputStream {

		static final int normal = 0;

		static final int exception = 1;

		static final int runtimeException = 2;

		int result = normal;

		InputStream wrapper;

		public void setResult(int i) {
			result = i;
		}

		private void checkException() throws IOException {
			switch (result) {
			case normal:
				return;
			case exception:
				throw new IOException("test");
			case runtimeException:
				throw new RuntimeException("test");
			}
		}

		public MockInputStream(InputStream in) {
			wrapper = in;
		}

		public int read() throws IOException {
			checkException();
			return wrapper.read();
		}
	}

	static class MockPreferences extends Preferences {

		public MockPreferences() {
			super();
		}

		public String absolutePath() {
			return null;
		}

		public String[] childrenNames() throws BackingStoreException {
			return null;
		}

		public void clear() throws BackingStoreException {
		}

		public void exportNode(OutputStream ostream) throws IOException,
				BackingStoreException {
		}

		public void exportSubtree(OutputStream ostream) throws IOException,
				BackingStoreException {
		}

		public void flush() throws BackingStoreException {
		}

		public String get(String key, String deflt) {
			return null;
		}

		public boolean getBoolean(String key, boolean deflt) {
			return false;
		}

		public byte[] getByteArray(String key, byte[] deflt) {
			return null;
		}

		public double getDouble(String key, double deflt) {
			return 0;
		}

		public float getFloat(String key, float deflt) {
			return 0;
		}

		public int getInt(String key, int deflt) {
			return 0;
		}

		public long getLong(String key, long deflt) {
			return 0;
		}

		public boolean isUserNode() {
			return false;
		}

		public String[] keys() throws BackingStoreException {
			return null;
		}

		public String name() {
			return null;
		}

		public Preferences node(String name) {
			return null;
		}

		public boolean nodeExists(String name) throws BackingStoreException {
			return false;
		}

		public Preferences parent() {
			return null;
		}

		public void put(String key, String value) {

		}

		public void putBoolean(String key, boolean value) {

		}

		public void putByteArray(String key, byte[] value) {

		}

		public void putDouble(String key, double value) {

		}

		public void putFloat(String key, float value) {

		}

		public void putInt(String key, int value) {

		}

		public void putLong(String key, long value) {

		}

		public void remove(String key) {

		}

		public void removeNode() throws BackingStoreException {

		}

		public void addNodeChangeListener(NodeChangeListener ncl) {

		}

		public void addPreferenceChangeListener(PreferenceChangeListener pcl) {

		}

		public void removeNodeChangeListener(NodeChangeListener ncl) {

		}

		public void removePreferenceChangeListener(PreferenceChangeListener pcl) {

		}

		public void sync() throws BackingStoreException {

		}

		public String toString() {
			return null;
		}

	}

}

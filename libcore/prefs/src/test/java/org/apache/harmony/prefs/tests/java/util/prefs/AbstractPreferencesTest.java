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

package org.apache.harmony.prefs.tests.java.util.prefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TODO: refine this test to adapt all implementations
 * 
 */
public class AbstractPreferencesTest extends TestCase {

	AbstractPreferences pref;

	static AbstractPreferences root = null;

	static AbstractPreferences parent = null;

	final static String longKey;

	final static String longValue;

	final static String longName;

	MockNodeChangeListener nl;

	MockPreferenceChangeListener pl;

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

		StringBuffer name = new StringBuffer(Preferences.MAX_NAME_LENGTH);
		for (int i = 0; i < Preferences.MAX_NAME_LENGTH; i++) {
			name.append('a');
		}
		longName = name.toString();
	}

	protected void setUp() throws Exception {
		super.setUp();
		root = (AbstractPreferences) Preferences.userRoot();
		parent = (AbstractPreferences) Preferences
				.userNodeForPackage(Preferences.class);
		// FIXME: change here is dangerous
		// pref = new MockAbstractPreferences((AbstractPreferences) parent,
		// "mock");

		pref = (AbstractPreferences) parent.node("mock");
	}

	protected void tearDown() throws Exception {
		try {
			if (pref instanceof MockAbstractPreferences) {
				((MockAbstractPreferences) pref)
						.setResult(MockAbstractPreferences.NORMAL);
			}
			pref.removeNode();
		} catch (Exception e) {
		}
		super.tearDown();
	}

	public void testConstructor() throws BackingStoreException {
		try {
			pref = new MockAbstractPreferences(
					(AbstractPreferences) Preferences.userRoot(), "mo/ck");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			pref = new MockAbstractPreferences(null, "mock");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			new MockAbstractPreferences(null, " ");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			new MockAbstractPreferences(pref, "");
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			new MockAbstractPreferences(pref, null);
			fail();
		} catch (NullPointerException e) {
		}
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		new MockAbstractPreferences(pref, " ");

		Preferences p2 = new MockAbstractPreferences(null, "");
		assertNotSame(p2, Preferences.systemRoot());
		assertNotSame(p2, Preferences.userRoot());
		assertFalse(p2.isUserNode());

		p2 = new MockAbstractPreferences((AbstractPreferences) Preferences
				.userRoot(), "mock");
		assertNotSame(p2, pref);
		p2.removeNode();
	}

	public void testProtectedFields() throws BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = new MockAbstractPreferences(pref, "newNode");
		assertFalse(p.getNewNode());
		assertSame(p.getLock().getClass(), Object.class);

		p = (MockAbstractPreferences) pref.node("child");
		assertTrue(p.getNewNode());

		p = (MockAbstractPreferences) ((MockAbstractPreferences) pref)
				.publicChildSpi("child2");
		assertTrue(p.getNewNode());
	}

	public void testToString() {
		assertEquals("User Preference Node: " + pref.absolutePath(), pref
				.toString());

		pref = new MockAbstractPreferences((AbstractPreferences) Preferences
				.systemRoot(), "mock");
		assertEquals("System Preference Node: " + pref.absolutePath(), pref
				.toString());
	}

	public void testAbsolutePath() {
		assertEquals("/java/util/prefs/mock", pref.absolutePath());

		pref = new MockAbstractPreferences(pref, " ");
		assertEquals("/java/util/prefs/mock/ ", pref.absolutePath());
	}

	public void testChildrenNames() throws BackingStoreException {
		assertEquals(0, pref.childrenNames().length);

		Preferences child1 = pref.node("child1");

		pref.node("child2");
		pref.node("child3");
		child1.node("subchild1");

		assertSame(pref, child1.parent());
		assertEquals(3, pref.childrenNames().length);
		assertEquals("child1", pref.childrenNames()[0]);
		assertEquals(1, child1.childrenNames().length);
		assertEquals("subchild1", child1.childrenNames()[0]);
	}

	public void testClear() throws BackingStoreException {
		pref.put("testClearKey", "testClearValue");
		pref.put("testClearKey1", "testClearValue1");
		assertEquals("testClearValue", pref.get("testClearKey", null));
		assertEquals("testClearValue1", pref.get("testClearKey1", null));
		pref.clear();
		assertNull(pref.get("testClearKey", null));
		assertNull(pref.get("testClearKey1", null));
	}

	public void testGet() throws BackingStoreException {
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

	public void testGetBoolean() {
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

	public void testPutByteArray() {
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

	public void testGetByteArray() throws UnsupportedEncodingException {
		try {
			pref.getByteArray(null, new byte[0]);
			fail();
		} catch (NullPointerException e) {
		}
		byte[] b64Array = new byte[] { 0x59, 0x57, 0x4a, 0x6a };// BASE64
		// encoding for
		// "abc"

		pref.put("testGetByteArrayKey", "abc=");
		pref.put("testGetByteArrayKey2", new String(b64Array));
		pref.put("invalidKey", "<>?");
		// assertTrue(Arrays.equals(new byte[0], p.getByteArray(
		// "testGetByteArrayKey", new byte[0])));
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

	public void testGetDouble() {
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

	public void testGetFloat() {
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

	public void testGetInt() {
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

	public void testGetLong() {
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

	public void testIsUserNode() {
		assertTrue(pref.isUserNode());

		pref = new MockAbstractPreferences((AbstractPreferences) Preferences
				.systemRoot(), "mock");
		assertFalse(pref.isUserNode());
	}

	// TODO, how to test the "stored defaults"
	// TODO, how to test the multi-thread
	public void testKeys() throws BackingStoreException {
		assertEquals(0, pref.keys().length);

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

	public void testName() {
		assertEquals("mock", pref.name());

		pref = new MockAbstractPreferences(pref, " ");
		assertEquals(" ", pref.name());
	}

	public void testCharCase() throws BackingStoreException {
		assertSame(pref.node("samechild"), pref.node("samechild"));
		assertNotSame(pref.node("sameChild"), pref.node("samechild"));
		assertNotSame(pref.node("child"), pref.node("Child"));
		assertNotSame(pref.node("child"), pref.node("Child"));
		assertNotSame(pref.node("child"), pref.node(" child"));
		String[] names = pref.childrenNames();
		assertEquals(5, names.length);
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			assertTrue("samechild".equals(name) || "sameChild".equals(name)
					|| "child".equals(name) || "Child".equals(name)
					|| " child".equals(name));
		}

		Preferences mock1 = pref.node("mock1");
		mock1.put("key", "1value");
		mock1.put("KEY", "2value");
		mock1.put("/K/E/Y", "7value");
		mock1.put("/K/E\\Y\\abc~@!#$%^&*(\\", "8value");

		assertEquals("8value", mock1.get("/K/E\\Y\\abc~@!#$%^&*(\\", null));
		assertNull(mock1.get("/k/e/y", null));
		assertEquals("7value", mock1.get("/K/E/Y", null));
		assertEquals("1value", mock1.get("key", null));

		String[] keys = mock1.keys();
		assertEquals(4, keys.length);
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			assertTrue("key".equals(key) || "KEY".equals(key)
					|| "/K/E/Y".equals(key)
					|| "/K/E\\Y\\abc~@!#$%^&*(\\".equals(key));
		}
	}

	public void testNode() throws BackingStoreException {
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

	public void testNodeExists() throws BackingStoreException {
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

	public void testParent() {
		assertSame(parent, pref.parent());
		AbstractPreferences child1 = new MockAbstractPreferences(pref, "child1");
		assertSame(pref, child1.parent());
		assertNull(root.parent());
	}

	public void testPut() throws BackingStoreException {
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

	public void testPutBoolean() {
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

	public void testPutDouble() {
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

	public void testPutFloat() {
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

	public void testPutInt() {
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

	public void testPutLong() {
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

	public void testRemove() throws BackingStoreException {
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

	public void testRemoveNode() throws BackingStoreException {
		Preferences child = pref.node("child");
		Preferences child1 = pref.node("child1");
		Preferences grandchild = child.node("grandchild");

		pref.removeNode();

		assertFalse(child.nodeExists(""));
		assertFalse(child1.nodeExists(""));
		assertFalse(grandchild.nodeExists(""));
		assertFalse(pref.nodeExists(""));
	}

	public void testAddNodeChangeListener() throws BackingStoreException {
		try {
			pref.addNodeChangeListener(null);
			fail();
		} catch (NullPointerException e) {
		}

		Preferences child1 = null;
		Preferences child2 = null;
		Preferences child3 = null;
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

	public void testAddPreferenceChangeListener() {
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
	}

	public void testRemoveNodeChangeListener() {
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

	public void testRemovePreferenceChangeListener() {
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

	public void testSync() throws BackingStoreException {

		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.resetSyncTimes();
		p.sync();
		assertEquals(1, p.getSyncTimes());

		p.resetSyncTimes();
		MockAbstractPreferences child = (MockAbstractPreferences) p
				.node("child");
		MockAbstractPreferences child2 = new MockAbstractPreferences(p,
				"child2");
		p.childs.put("child2", child2);
		assertEquals(1, p.cachedChildrenImpl().length);
		assertSame(child, p.cachedChildrenImpl()[0]);
		p.sync();
		assertEquals(1, p.getSyncTimes());
		assertEquals(1, child.getSyncTimes());
		assertEquals(0, child2.getSyncTimes());

		p.resetSyncTimes();
		child.resetSyncTimes();
		child.sync();
		assertEquals(0, p.getSyncTimes());
		assertEquals(1, child.getSyncTimes());

		p.resetSyncTimes();
		child.resetSyncTimes();
		MockAbstractPreferences grandson = (MockAbstractPreferences) child
				.node("grandson");
		child.sync();
		assertEquals(0, p.getSyncTimes());
		assertEquals(1, child.getSyncTimes());
		assertEquals(1, grandson.getSyncTimes());
	}

	public void testFlush() throws BackingStoreException {
		
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.resetFlushedTimes();
		p.flush();
		assertEquals(1, p.getFlushedTimes());

		p.resetFlushedTimes();
		MockAbstractPreferences child = (MockAbstractPreferences) p
				.node("child");
		MockAbstractPreferences child2 = new MockAbstractPreferences(p,
				"child2");
		p.childs.put("child2", child2);
		assertEquals(1, p.cachedChildrenImpl().length);
		assertSame(child, p.cachedChildrenImpl()[0]);
		p.flush();
		assertEquals(1, p.getFlushedTimes());
		assertEquals(1, child.getFlushedTimes());
		assertEquals(0, child2.getFlushedTimes());

		p.resetFlushedTimes();
		child.resetFlushedTimes();
		child.flush();
		assertEquals(0, p.getFlushedTimes());
		assertEquals(1, child.getFlushedTimes());

		p.resetFlushedTimes();
		child.resetFlushedTimes();
		MockAbstractPreferences grandson = (MockAbstractPreferences) child
				.node("grandson");
		child.flush();
		assertEquals(0, p.getFlushedTimes());
		assertEquals(1, child.getFlushedTimes());
		assertEquals(1, grandson.getFlushedTimes());

		p.resetFlushedTimes();
		child.resetFlushedTimes();
		grandson.resetFlushedTimes();
		child.removeNode();
		child.flush();
		assertEquals(0, p.getFlushedTimes());
		assertEquals(1, child.getFlushedTimes());
		assertEquals(0, grandson.getFlushedTimes());
	}

	public void testGetChild() throws BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		assertNull(p.getChildImpl("child"));
		MockAbstractPreferences child = new MockAbstractPreferences(p, "child");
		p.childs.put("child", child);
		assertSame(child, p.getChildImpl("child"));
		assertNull(p.getChildImpl("child "));

		assertNull(p.getChildImpl("child/grandson"));
		child.childs.put("grandson", new MockAbstractPreferences(child,
				"grandson"));
		assertNull(p.getChildImpl("child/grandson"));

		assertNull(p.getChildImpl(null));
		assertNull(p.getChildImpl(""));
		assertNull(p.getChildImpl(" "));
		assertNull(p.getChildImpl("abc//abc"));
		assertNull(p.getChildImpl("child/"));
		assertNull(p.getChildImpl(longName + "a"));

		child.removeNode();
		assertNull(p.getChildImpl("child"));
	}

	public void testIsRemoved() throws BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		assertFalse(p.isRemovedImpl());
		p.removeNode();
		assertTrue(p.isRemovedImpl());
	}

	public void testExportNode() throws Exception {
		try {
			pref.exportNode(null);
			fail();
		} catch (NullPointerException e) {
			// Expected
		}

		pref.putBoolean("key", false);
		Preferences child = pref.node("child<");
		child.put("key2", "value2<");
		Preferences grandson = child.node("grandson");
		grandson.put("key3", "value3");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    child.exportNode(out);

		byte[] result = out.toString().getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(result);

                try {
		    parseXmlStream(in, true);
                } catch (Exception ee) {
                    fail("Exception " + ee + " does not expected");
                }
	}

	private static Document parseXmlStream(InputStream input, boolean validating)
			throws SAXException, IOException, ParserConfigurationException {
		// Create a builder factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validating);

		// Create the builder and parse the file
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(input);
		return doc;
	}

	public void testExportSubtree() throws Exception {
		try {
			pref.exportSubtree(null);
			fail();
		} catch (NullPointerException e) {
			// Expected
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pref.putBoolean("key", false);
		Preferences child = pref.node("child");
		child.put("key2", "value2");
		Preferences grandson = child.node("grandson");
		grandson.put("key3", "value3");
		child.node("grandson2");
		Preferences grandgrandson = grandson.node("grandgrandson");
		grandgrandson.put("key4", "value4");
		child.exportSubtree(out);

		byte[] result = out.toByteArray();
		// System.out.println(new String(result, "utf-8"));
		ByteArrayInputStream in = new ByteArrayInputStream(result);

                try {
		    parseXmlStream(in, true);
                } catch (Exception ee) {
                    fail("Exception " + ee + " does not expected");
                }
	}

	public void testCachedChildren() throws Exception {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		assertEquals(0, p.cachedChildrenImpl().length);

		MockAbstractPreferences child = (MockAbstractPreferences) p
				.getChildImpl("child");
		assertNull(child);

		child = new MockAbstractPreferences(p, "child");
		assertSame(child, p.getChildImpl("child"));

		assertEquals(0, p.cachedChildrenImpl().length);

		p.node("child");
		assertSame(child, p.cachedChildrenImpl()[0]);

		MockAbstractPreferences grandchild = new MockAbstractPreferences(child,
				"grandchild");
		assertSame(grandchild, child.getChildImpl("grandchild"));
		assertNull(p.getChildImpl("grandchild"));

		assertEquals(1, p.cachedChildrenImpl().length);
		assertEquals(0, child.cachedChildrenImpl().length);

		p.node("child/grandchild");
		assertSame(child, p.cachedChildrenImpl()[0]);
		assertSame(grandchild, child.cachedChildrenImpl()[0]);
		assertEquals(1, p.cachedChildrenImpl().length);
		assertEquals(1, child.cachedChildrenImpl().length);

		p.childs.put("child2", new MockAbstractPreferences(p, "child2"));
		p.nodeExists("child2/grandchild");
		assertSame(child, p.cachedChildrenImpl()[0]);
		assertSame(grandchild, child.cachedChildrenImpl()[0]);
		assertEquals(1, p.cachedChildrenImpl().length);
		assertEquals(1, child.cachedChildrenImpl().length);
	}

	public void testAbstractMethod() {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		((MockAbstractPreferences) pref).protectedAbstractMethod();
	}

	public Object invokeNonPublicMethod(AbstractPreferences obj, String name,
			Class[] params, Object[] paramValues) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Method method = obj.getClass().getMethod(name, params);
		method.setAccessible(true);
		return method.invoke(obj, paramValues);
	}

	public void testBackingStoreException() throws IOException,
			BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.setResult(MockAbstractPreferences.backingException);
		try {
			p.childrenNames();
			fail();
		} catch (BackingStoreException e) {
		}
		p.put("exceptionkey", "value");
		p.absolutePath();
		p.toString();
		assertEquals("exception default", p.get("key", "exception default"));
		p.remove("key");
		try {
			p.clear();
			fail();
		} catch (BackingStoreException e) {
		}
		p.putInt("key", 3);
		p.getInt("key", 3);
		p.putLong("key", 3l);
		p.getLong("key", 3l);
		p.putDouble("key", 3);
		p.getDouble("key", 3);
		p.putBoolean("key", true);
		p.getBoolean("key", true);
		p.putFloat("key", 3f);
		p.getFloat("key", 3f);
		p.putByteArray("key", new byte[0]);
		p.getByteArray("key", new byte[0]);
		try {
			p.keys();
			fail();
		} catch (BackingStoreException e) {
		}

		try {
			p.keys();
			fail();
		} catch (BackingStoreException e) {
		}
		try {
			p.childrenNames();
			fail();
		} catch (BackingStoreException e) {
		}
		p.parent();
		p.node("");
		p.nodeExists("");
		try {
			p.removeNode();
			fail();
		} catch (BackingStoreException e) {
		}
		p.name();
		p.absolutePath();
		p.isUserNode();
		MockPreferenceChangeListener mockPreferenceChangeListener = new MockPreferenceChangeListener();
		p.addPreferenceChangeListener(mockPreferenceChangeListener);
		p.removePreferenceChangeListener(mockPreferenceChangeListener);
		MockNodeChangeListener mockNodeChangeListener = new MockNodeChangeListener();
		p.addNodeChangeListener(mockNodeChangeListener);
		p.removeNodeChangeListener(mockNodeChangeListener);
		p.toString();
		try {
			p.sync();
			fail();
		} catch (BackingStoreException e) {
		}
		try {
			p.flush();
			fail();
		} catch (BackingStoreException e) {
		}
		try {
			p.exportNode(new ByteArrayOutputStream());
			fail();
		} catch (BackingStoreException e) {
		}
		try {
			p.exportSubtree(new ByteArrayOutputStream());
			fail();
		} catch (BackingStoreException e) {
		}
		p.isRemovedImpl();
		try {
			p.getChildImpl(null);
			fail();
		} catch (BackingStoreException e) {
		}
		p.cachedChildrenImpl();
	}

	public void testRuntimeException() throws IOException,
			BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.setResult(MockAbstractPreferences.runtimeException);
		try {
			p.childrenNames();
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.put("exceptionkey", "value");
			fail();
		} catch (MockRuntimeException e) {
		}
		p.absolutePath();
		p.toString();
		assertEquals("exception default", p.get("key", "exception default"));
		try {
			p.remove("key");
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.clear();
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.putInt("key", 3);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.getInt("key", 3);
		try {
			p.putLong("key", 3l);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.getLong("key", 3l);
		try {
			p.putDouble("key", 3);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.getDouble("key", 3);
		try {
			p.putBoolean("key", true);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.getBoolean("key", true);
		try {
			p.putFloat("key", 3f);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.getFloat("key", 3f);
		try {
			p.putByteArray("key", new byte[0]);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.getByteArray("key", new byte[0]);
		try {
			p.keys();
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.keys();
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.childrenNames();
			fail();
		} catch (MockRuntimeException e) {
		}
		p.parent();
		p.node("");
		p.nodeExists("");
		try {
			p.removeNode();
			fail();
		} catch (MockRuntimeException e) {
		}
		p.name();
		p.absolutePath();
		p.isUserNode();
		MockPreferenceChangeListener pcl = new MockPreferenceChangeListener();
		p.addPreferenceChangeListener(pcl);
		p.removePreferenceChangeListener(pcl);
		MockNodeChangeListener ncl = new MockNodeChangeListener();
		p.addNodeChangeListener(ncl);
		p.removeNodeChangeListener(ncl);
		p.toString();
		try {
			p.sync();
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.flush();
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.exportNode(new ByteArrayOutputStream());
			fail();
		} catch (MockRuntimeException e) {
		}
		try {
			p.exportSubtree(new ByteArrayOutputStream());
			fail();
		} catch (MockRuntimeException e) {
		}
		p.isRemovedImpl();
		try {
			p.getChildImpl(null);
			fail();
		} catch (MockRuntimeException e) {
		}
		p.cachedChildrenImpl();
	}

	public void testSPIReturnNull() throws IOException, BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.setResult(MockAbstractPreferences.returnNull);
		try {
			p.childrenNames();
			fail();
		} catch (NullPointerException e) {
		}
		p.absolutePath();
		p.toString();
		p.put("nullkey", "value");
		assertEquals("null default", p.get("key", "null default"));
		p.remove("key");
		try {
			p.clear();
			fail();
		} catch (NullPointerException e) {
		}
		p.putInt("key", 3);
		p.getInt("key", 3);
		p.putLong("key", 3l);
		p.getLong("key", 3l);
		p.putDouble("key", 3);
		p.getDouble("key", 3);
		p.putBoolean("key", true);
		p.getBoolean("key", true);
		p.putFloat("key", 3f);
		p.getFloat("key", 3f);
		p.putByteArray("key", new byte[0]);
		p.getByteArray("key", new byte[0]);
		p.keys();
		try {
			p.childrenNames();
			fail();
		} catch (NullPointerException e) {
		}
		p.parent();
		p.node("");
		p.nodeExists("");
		try {
			p.removeNode();
			fail();
		} catch (NullPointerException e) {
		}
		p.name();
		p.absolutePath();
		p.isUserNode();
		MockPreferenceChangeListener mockPreferenceChangeListener = new MockPreferenceChangeListener();
		p.addPreferenceChangeListener(mockPreferenceChangeListener);
		p.removePreferenceChangeListener(mockPreferenceChangeListener);
		MockNodeChangeListener mockNodeChangeListener = new MockNodeChangeListener();
		p.addNodeChangeListener(mockNodeChangeListener);
		p.removeNodeChangeListener(mockNodeChangeListener);
		p.toString();
		p.sync();
		p.flush();
		try {
			p.exportNode(System.out);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.exportSubtree(System.out);
			fail();
		} catch (NullPointerException e) {
		}
		p.isRemovedImpl();
		try {
			p.getChildImpl("");
			fail();
		} catch (NullPointerException e) {
		}
		p.cachedChildrenImpl();
	}

	public void testIllegalStateException() throws IOException,
			BackingStoreException {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		pref.removeNode();
		// after remove node, every methods, except name(), absolutePath(),
		// isUserNode(), flush() or nodeExists(""),
		// will throw illegal state exception
		pref.nodeExists("");
		pref.name();
		pref.absolutePath();
		pref.isUserNode();
		pref.toString();
		pref.flush();
		try {
			pref.nodeExists("child");
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.childrenNames();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.remove(null);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.clear();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.get("key", "null default");
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.put("nullkey", "value");
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.putInt("key", 3);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.getInt("key", 3);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.putLong("key", 3l);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.getLong("key", 3l);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.putDouble("key", 3);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.getDouble("key", 3);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.putBoolean("key", true);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.getBoolean("key", true);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.putFloat("key", 3f);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.getFloat("key", 3f);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.putByteArray("key", new byte[0]);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.getByteArray("key", new byte[0]);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.keys();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.keys();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.childrenNames();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.parent();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.node(null);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.removeNode();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref
					.addPreferenceChangeListener(new MockPreferenceChangeListener());
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref
					.removePreferenceChangeListener(new MockPreferenceChangeListener());
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.addNodeChangeListener(new MockNodeChangeListener());
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.removeNodeChangeListener(new MockNodeChangeListener());
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.sync();
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.exportNode(null);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			pref.exportSubtree(null);
			fail();
		} catch (IllegalStateException e) {
		}
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.isRemovedImpl();
		p.cachedChildrenImpl();
		try {
			p.getChildImpl(null);
			fail();
		} catch (IllegalStateException e) {
		}
	}

	public void testNullAndIllegalStateException() throws Exception {
		if (!(pref instanceof MockAbstractPreferences)) {
			return;
		}
		MockAbstractPreferences p = (MockAbstractPreferences) pref;
		p.removeNode();
		try {
			p.get(null, "null default");
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.put(null, "value");
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.putInt(null, 3);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.getInt(null, 3);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.putLong(null, 3l);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.getLong(null, 3l);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.putDouble(null, 3);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.getDouble(null, 3);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.putBoolean(null, true);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.getBoolean(null, true);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.putFloat(null, 3f);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.getFloat(null, 3f);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.putByteArray(null, new byte[0]);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.getByteArray(null, new byte[0]);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.addPreferenceChangeListener(null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.removePreferenceChangeListener(null);
			fail();
		} catch (IllegalStateException e) {
		}
		try {
			p.addNodeChangeListener(null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			p.removeNodeChangeListener(null);
			fail();
		} catch (IllegalStateException e) {
		}
	}
 
	/**
	 * @test java.util.prefs.AbstractPreferences#childrenNamesSpi()
	 *
	 */
	public void testChildrenNamesSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		try {
			assertEquals(0, p.childrenNamesSpi().length);
		} catch(java.util.prefs.BackingStoreException bse) {
			fail("java.util.prefs.BackingStoreException is thrown: " + 
					bse.toString());
		}
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#childSpi()
	 *
	 */

	public void testChildSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		Preferences child = p.node("mock1");
		assertEquals(child, p.childSpi("mock1"));
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#flushSpi()
	 *
	 */

	public void testFlushSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		try {
			p.flushSpi();
		} catch(Exception e) {
			fail("Unexpected exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#getSpi()
	 *
	 */

	public void testGetSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		try {
			assertNull(p.getSpi(""));
			p.put("key", "default");
			assertEquals("default", p.getSpi("key"));
		} catch(Exception e) {
			fail("Unexpected exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#keysSpi()
	 *
	 */

	public void testKeysSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		try {
			p.put("key1", "default");
			p.putInt("key2", 123);
			assertEquals(2, p.keysSpi().length);
			assertEquals("key2", p.keysSpi()[0]);
			assertEquals("key1", p.keysSpi()[1]);
		} catch(Exception e) {
			fail("Unexpected exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#putSpi()
	 *
	 */

	public void testPutSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		try {
			p.putSpi("key1", "default");
			p.putSpi("key2", "123");
			assertEquals(2, p.keysSpi().length);
			assertEquals("key2", p.keysSpi()[0]);
			assertEquals("key1", p.keysSpi()[1]);
		} catch(Exception e) {
			fail("Unexpected exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#removeSpi()
	 *
	 */

	public void testRemoveSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		p.put("key1", "value1");
		try {
			p.removeSpi("key1");

			assertNull(p.getSpi("key1"));
		} catch(Exception e) {
			fail("Unexpected exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * @test java.util.prefs.AbstractPreferences#syncSpi()
	 *
	 */

	public void testSyncSpi() {
		MockAbstractPreferences p = new MockAbstractPreferences(
				(AbstractPreferences) Preferences.userRoot(), "mock");
		p.put("key1", "value1");
		try {
			p.syncSpi();
		} catch(Exception e) {
			fail("Unexpected exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * Regression for HARMONY-828
	 */
	public void testLongPath() throws Exception {
		assertFalse(pref
				.nodeExists("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"));
	}

}
